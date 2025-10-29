package com.mudosa.musinsa.payment.application.service;

import com.mudosa.musinsa.exception.BusinessException;
import com.mudosa.musinsa.exception.ErrorCode;
import com.mudosa.musinsa.order.application.OrderService;
import com.mudosa.musinsa.payment.application.dto.PaymentConfirmRequest;
import com.mudosa.musinsa.payment.application.dto.PaymentConfirmResponse;
import com.mudosa.musinsa.payment.domain.model.Payment;
import com.mudosa.musinsa.payment.domain.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderService orderService;
    private final PaymentProcessor paymentProcessor;

    //TODO: Payment 준비 작업과 트랜잭션 분리 vs API 분리
    @Transactional
    public PaymentConfirmResponse confirmPaymentAndCompleteOrder(PaymentConfirmRequest request) {
        log.info("결제 승인 시작 - paymentId: {}, orderId: {}, amount: {}", 
            request.getPaymentId(), request.getOrderId(), request.getAmount());

        /* 1. Payment 조회 */
        Payment payment = paymentRepository.findById(request.getPaymentId())
            .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        /* 2. Payment 검증 */
        try {
            payment.validatePending();

            payment.validateAmount(BigDecimal.valueOf(request.getAmount()));

            payment.requestApproval(request.getUserId());
            paymentRepository.save(payment); // => 로그 저장
        } catch (BusinessException e) {
            log.error("결제 검증 실패 - paymentId: {}, error: {}",
                    payment.getId(), e.getMessage());

            payment.fail(e.getMessage(), request.getUserId());
            paymentRepository.save(payment);

            throw e;
        }

        try {
            /* 3. 주문 완료 처리(재고차감/주문상태 변경/장바구니 삭제/쿠폰 사용 처리) -> Payment 도메인과 분리 */
            orderService.completeOrder(payment.getOrderId());
            
            log.info("주문 완료 처리 성공 - orderId: {}", payment.getOrderId());

            /* 4. 결제 API 승인 */
            PaymentConfirmResponse response = paymentProcessor.processPayment(request);

            log.info("토스 결제 승인 성공 - paymentKey: {}, status: {}", 
                response.getPaymentKey(), response.getStatus());

            /* 5. Payment 상태 업데이트 + 로그 저장 */
            payment.approve(response.getPaymentKey(), request.getUserId());
            paymentRepository.save(payment);

            log.info("결제 승인 완료 - paymentId: {}, orderId: {}",
                payment.getId(), payment.getOrderId());

            return response;

        } catch (BusinessException e) {
            log.error("결제 프로세스 실패 - orderId: {}, error: {}",
                    payment.getOrderId(), e.getMessage());

            // 주문 처리는 성공했지만 PG 승인 실패한 경우
            if (e.getErrorCode() != ErrorCode.INSUFFICIENT_STOCK &&
                    e.getErrorCode() != ErrorCode.ORDER_ALREADY_COMPLETED) {

                log.warn("PG 승인 실패 - 보상 트랜잭션 시작 - orderId: {}",
                        payment.getOrderId());

                try {
                    orderService.rollbackOrder(payment.getOrderId());
                    log.info("보상 트랜잭션 성공 - orderId: {}", payment.getOrderId());

                } catch (Exception rollbackException) {
                    log.error("보상 트랜잭션 실패 - orderId: {}",
                            payment.getOrderId(), rollbackException);
                }
            }
            
            // 주문 처리 실패한 경우 -> 결제 실패만 처리
            // 왜? 주문 처리가 되어야만 주문 처리가 되도록 동기적으로 처리하고 있기 때문에 주문 완료 처리 실패 시 결제 취소를 안해도됨
            payment.fail(e.getMessage(), request.getUserId());
            paymentRepository.save(payment);

            throw new BusinessException(
                    ErrorCode.PAYMENT_APPROVAL_FAILED,
                    e.getMessage()
            );

        } catch (Exception e) {
            log.error("결제 승인 중 예외 발생 - orderId: {}",
                    payment.getOrderId(), e);

            // 예상치 못한 오류에 대한 주문 롤백 처리
            try {
                orderService.rollbackOrder(payment.getOrderId());
            } catch (Exception rollbackException) {
                log.error("보상 트랜잭션 실패 - orderId: {}",
                        payment.getOrderId(), rollbackException);
            }

            //예상치 못한 오류에 대한 결제 실패 처리
            payment.fail("시스템 오류: " + e.getMessage(), request.getUserId());
            paymentRepository.save(payment);

            throw new BusinessException(
                    ErrorCode.PAYMENT_APPROVAL_FAILED,
                    "결제 처리 중 시스템 오류가 발생했습니다"
            );
        }
    }
}
