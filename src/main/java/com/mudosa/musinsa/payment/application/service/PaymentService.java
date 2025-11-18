package com.mudosa.musinsa.payment.application.service;

import com.mudosa.musinsa.exception.BusinessException;
import com.mudosa.musinsa.exception.ErrorCode;
import com.mudosa.musinsa.order.application.OrderService;
import com.mudosa.musinsa.payment.application.dto.*;
import com.mudosa.musinsa.payment.application.dto.request.PaymentConfirmRequest;
import com.mudosa.musinsa.payment.application.dto.response.PaymentConfirmResponse;
import com.mudosa.musinsa.payment.domain.model.Payment;
import com.mudosa.musinsa.payment.domain.repository.PaymentRepository;
import com.mudosa.musinsa.payment.application.event.PaymentApprovedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
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
    private final ApplicationEventPublisher eventPublisher;

    public PaymentConfirmResponse confirmPaymentAndCompleteOrder(PaymentConfirmRequest request, Long userId) {

        Long paymentId = null;
        Long orderId = null;

        try{
            //TX1: 결제 생성
            PaymentCreationResult creationResult = createPaymentTransaction(request.toPaymentCreateRequest(), userId);

            paymentId = creationResult.getPaymentId();
            orderId = creationResult.getOrderId();

            //트랜잭션 아님: PG 승인 요청
            PaymentResponseDto pgResponse = paymentProcessor.processPayment(request);

            //TX2: 결제 승인
            approvePayment(paymentId, userId, pgResponse, orderId);

            return PaymentConfirmResponse.builder()
                    .orderNo(request.getOrderNo())
                    .build();

        }catch(BusinessException e){
            log.error("결제 프로세스 실패 - BusinessException: {}", e.getMessage());
            handleBusinessException(paymentId, orderId, e, userId);
            throw e;

        }catch (Exception e){
            log.error("✗ 결제 프로세스 실패 - 예상치 못한 오류", e);
            handleUnexpectedException(paymentId, orderId, e, userId);
            throw new BusinessException(ErrorCode.PAYMENT_APPROVAL_FAILED,
                    "결제 처리 중 시스템 오류가 발생했습니다");
        }
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected PaymentCreationResult createPaymentTransaction(PaymentCreateDto request, Long userId) {
        // 주문 완료(재고 차감, 주문 상태 변경)
        Long orderId = orderService.completeOrder(
                request.getOrderNo()
        );

        // 결제 생성
        Payment payment = Payment.create(
                orderId,
                request.getTotalAmount(),
                request.getPgProvider(),
                userId
        );

        paymentRepository.save(payment);

        return PaymentCreationResult.builder()
                .paymentId(payment.getId())
                .orderId(orderId)
                .userId(userId)
                .build();
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void approvePayment(Long paymentId, Long userId, PaymentResponseDto paymentResponseDto, Long orderId) {
        //장바구니 삭제
        orderService.deleteCartItems(orderId, userId);

        //결제 조회
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        String pgTransactionId = paymentResponseDto.getPaymentKey();

        //결제 상태 변경
        payment.approve(pgTransactionId, userId, paymentResponseDto.getApprovedAt(), paymentResponseDto.getMethod());
        paymentRepository.save(payment);

        //정산 이벤트 발행
        eventPublisher.publishEvent(
                new PaymentApprovedEvent(paymentId, pgTransactionId, userId)
        );
    }


    private void handleBusinessException(
            Long paymentId,
            Long orderId,
            BusinessException e,
            Long userId) {

        log.error("BusinessException 처리 시작 - paymentId: {}, orderId: {}, error: {}",
                paymentId, orderId, e.getErrorCode());

        // 결제 생성 전 실패

        // 주문 완료 단계에서 실패

        // PG 승인 단계에서 실패


    }

    private void handleUnexpectedException(
            Long paymentId,
            Long orderId,
            Exception e,
            Long userId) {




    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void failPayment(Long paymentId, String errorMessage, Long userId) {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        payment.fail(errorMessage, userId);
        paymentRepository.save(payment);

        log.warn("결제 실패 처리 완료");
    }




}
