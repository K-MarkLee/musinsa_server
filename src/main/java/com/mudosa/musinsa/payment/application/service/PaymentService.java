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

    @Transactional
    public PaymentConfirmResponse confirmPaymentAndCompleteOrder(PaymentConfirmRequest request) {
        log.info("결제 승인 시작 - paymentId: {}, orderId: {}, amount: {}", 
            request.getPaymentId(), request.getOrderId(), request.getAmount());

        Payment payment = paymentRepository.findById(request.getPaymentId())
            .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        try {
            payment.validatePending();
        } catch (IllegalStateException e) {
            log.error("이미 승인된 결제 - paymentId: {}", request.getPaymentId());
            throw new BusinessException(ErrorCode.PAYMENT_ALREADY_APPROVED);
        }

        try {
            payment.validateAmount(BigDecimal.valueOf(request.getAmount()));
        } catch (IllegalArgumentException e) {
            log.error("결제 금액 불일치 - paymentId: {}, 요청: {}, 실제: {}", 
                request.getPaymentId(), request.getAmount(), payment.getAmount());
            throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        try {
            // 주문 완료 처리 (장바구니 여부 전달)
            boolean isFromCart = request.getIsFromCart() != null ? request.getIsFromCart() : false;
            orderService.completeOrder(payment.getOrderId(), isFromCart);
            
            log.info("주문 완료 처리 성공 - orderId: {}", payment.getOrderId());

            PaymentConfirmResponse response = paymentProcessor.processPayment(request);
            
            log.info("토스 결제 승인 성공 - paymentKey: {}, status: {}", 
                response.getPaymentKey(), response.getStatus());

            payment.approve(response.getPaymentKey(), LocalDateTime.now());
            paymentRepository.save(payment);
            
            log.info("결제 승인 완료 - paymentId: {}, orderId: {}", 
                payment.getId(), payment.getOrderId());

            return response;

        } catch (BusinessException e) {
            if (e.getErrorCode() == ErrorCode.INSUFFICIENT_STOCK ||
                e.getErrorCode() == ErrorCode.ORDER_ALREADY_COMPLETED) {
                
                log.error("주문 처리 실패 - orderId: {}, error: {}", 
                    payment.getOrderId(), e.getMessage());
                
                payment.fail(e.getMessage());
                paymentRepository.save(payment);
                
                throw e;
            }
            
            log.error("토스 API 승인 실패 - orderId: {}, 보상 트랜잭션 시작",
                payment.getOrderId());
            
            try {
                orderService.rollbackOrder(payment.getOrderId());
                
            } catch (Exception rollbackException) {
                log.error("보상 트랜잭션 실패 - orderId: {}", 
                    payment.getOrderId(), rollbackException);
                // TODO: 수동 처리 필요 - 관리자 알림 발송
            }
            
            payment.fail(e.getMessage());
            paymentRepository.save(payment);
            
            throw new BusinessException(ErrorCode.PAYMENT_APPROVAL_FAILED);

        } catch (Exception e) {
            log.error("결제 승인 중 예외 발생 - orderId: {}",
                payment.getOrderId(), e);
            
            payment.fail(e.getMessage());
            paymentRepository.save(payment);
            
            throw new BusinessException(ErrorCode.PAYMENT_APPROVAL_FAILED);
        }
    }
}
