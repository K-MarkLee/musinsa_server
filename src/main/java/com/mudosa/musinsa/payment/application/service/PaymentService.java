package com.mudosa.musinsa.payment.application.service;

import com.mudosa.musinsa.exception.BusinessException;
import com.mudosa.musinsa.exception.ErrorCode;
import com.mudosa.musinsa.order.application.OrderService;
import com.mudosa.musinsa.payment.application.dto.OrderValidationResult;
import com.mudosa.musinsa.payment.application.dto.PaymentConfirmRequest;
import com.mudosa.musinsa.payment.application.dto.PaymentConfirmResponse;
import com.mudosa.musinsa.payment.application.dto.PaymentCreationResult;
import com.mudosa.musinsa.payment.domain.model.Payment;
import com.mudosa.musinsa.payment.domain.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;


@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderService orderService;
    private final PaymentProcessor paymentProcessor;

    public PaymentConfirmResponse confirmPaymentAndCompleteOrder(PaymentConfirmRequest request) {
        log.info("결제 승인 시작, orderId: {}, amount: {}",
           request.getOrderNo(), request.getAmount());

        Long paymentId = null;
        Long orderId = null;

        try{
            /* 1. 결제 생성 */
            PaymentCreationResult creationResult = createPaymentTransaction(request);
            paymentId = creationResult.getPaymentId();
            orderId = creationResult.getOrderId();

            /* 2. 주문 완료 처리 */
            orderService.completeOrder(orderId);

            /* 3. PG 승인 요청 */
            PaymentConfirmResponse pgResponse = paymentProcessor.processPayment(request);

            /* 4. 결제 승인 처리 */
            approvePayment(paymentId, pgResponse.getPaymentKey(), request.getUserId());

            return pgResponse;

        }catch(BusinessException e){
            log.error("결제 프로세스 실패 - BusinessException: {}", e.getMessage());
            handleBusinessException(paymentId, orderId, e, request.getUserId());
            throw e;

        }catch (Exception e){
            log.error("✗ 결제 프로세스 실패 - 예상치 못한 오류", e);
            handleUnexpectedException(paymentId, orderId, e, request.getUserId());
            throw new BusinessException(ErrorCode.PAYMENT_APPROVAL_FAILED,
                    "결제 처리 중 시스템 오류가 발생했습니다");
        }
    }


    /* 결제 생성 */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PaymentCreationResult createPaymentTransaction(PaymentConfirmRequest request) {
        log.info("→ TX1 시작: 결제 생성 트랜잭션");

        // 1. 주문 검증 및 계산
        OrderValidationResult validationResult = orderService.validateAndPrepareOrder(
                request.getOrderNo(),
                request.getUserId(),
                BigDecimal.valueOf(request.getAmount())
        );

        log.info("주문 검증 완료 - orderId: {}, finalAmount: {}, discount: {}",
                validationResult.getOrderId(),
                validationResult.getFinalAmount(),
                validationResult.getDiscountAmount());


        // 3. Payment 엔티티 생성
        Payment payment = Payment.create(
                validationResult.getOrderId(),
                validationResult.getFinalAmount(),
                request.getPgProvider(),
                request.getUserId()
        );

        payment = paymentRepository.save(payment);

        log.info("← TX1 커밋: 결제 생성 완료 - paymentId={}", payment.getId());

        return PaymentCreationResult.of(
                payment.getId(),
                validationResult.getOrderId(),
                validationResult.getUserId()
        );
    }


    /* 결제 실패 처리 */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void failPayment(Long paymentId, String errorMessage, Long userId) {
        log.warn("결제 실패 처리 - paymentId={}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        payment.fail(errorMessage, userId);
        paymentRepository.save(payment);

        log.warn("결제 실패 처리 완료");
    }

    /* PG사 결제 승인 후 결제 상태 변경 */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void approvePayment(Long paymentId, String pgTransactionId, Long userId) {
        log.info("→ TX3 시작: 결제 승인 트랜잭션 - paymentId={}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        payment.approve(pgTransactionId, userId);
        paymentRepository.save(payment);

        log.info("← TX3 커밋: 결제 승인 완료 - paymentId={}, pgTxId={}",
                paymentId, pgTransactionId);
    }

    /* 결제 실패 처리 */
    private void handleBusinessException(
            Long paymentId,
            Long orderId,
            BusinessException e,
            Long userId) {

        log.error("BusinessException 처리 시작 - paymentId: {}, orderId: {}, error: {}",
                paymentId, orderId, e.getErrorCode());

        // 결제 생성 전 실패임 => 보상 트랜잭션 불필요
        if (paymentId == null) {
            log.info("결제 생성 단계 실패 - 보상 트랜잭션 불필요");
            return;
        }

        // 주문 완료 단계에서 실패인 경우
        if (e.getErrorCode() == ErrorCode.INSUFFICIENT_STOCK ||
                e.getErrorCode() == ErrorCode.ORDER_ALREADY_COMPLETED) {

            log.warn("주문 완료 실패 - 재고 부족 또는 중복 처리");
            failPayment(paymentId, e.getMessage(), userId);
            return;
        }

        // PG 승인 단계에서 실패 => 주문 롤백 처리 해야함
        if (orderId != null && isOrderCompleted(orderId)) {
            log.warn("PG 승인 실패 - 주문 롤백 시작");
            try {
                orderService.rollbackOrder(orderId);
                failPayment(paymentId, e.getMessage(), userId);
                log.info("보상 트랜잭션 성공");
            } catch (Exception rollbackError) {
                log.error("보상 트랜잭션 실패 - 수동 처리 필요", rollbackError);
                // TODO: 관리자 알림 발송
            }
        } else {
            // 주문 완료 전 실패
            failPayment(paymentId, e.getMessage(), userId);
        }
    }

    /* 예상하지 못한 결제 실패의 경우 */
    private void handleUnexpectedException(
            Long paymentId,
            Long orderId,
            Exception e,
            Long userId) {

        log.error("예상치 못한 오류 처리 시작", e);

        if (paymentId == null) {
            return;
        }

        if (orderId != null && isOrderCompleted(orderId)) {
            orderService.rollbackOrder(orderId);
        }
        failPayment(paymentId, "시스템 오류: " + e.getMessage(), userId);
    }
    
    /* 주문 완료 여부 확인 */
    private boolean isOrderCompleted(Long orderId) {
        return orderService.isOrderCompleted(orderId);
    }

}
