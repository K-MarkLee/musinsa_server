package com.mudosa.musinsa.payment.application.service;

import com.mudosa.musinsa.exception.BusinessException;
import com.mudosa.musinsa.exception.ErrorCode;
import com.mudosa.musinsa.order.application.OrderService;
import com.mudosa.musinsa.payment.application.dto.PaymentCreateDto;
import com.mudosa.musinsa.payment.application.dto.PaymentCreationResult;
import com.mudosa.musinsa.payment.application.dto.PaymentResponseDto;
import com.mudosa.musinsa.payment.domain.model.Payment;
import com.mudosa.musinsa.payment.domain.model.PaymentEventType;
import com.mudosa.musinsa.payment.domain.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentConfirmService {

    private final OrderService orderService;
    private final PaymentRepository paymentRepository;

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
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void failPayment(Long paymentId, String errorMessage, Long userId, Long orderId) {
        //주문 및 재고 롤백
        orderService.rollbackOrder(orderId);

        //결제 상태 변경
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        payment.fail(errorMessage, userId);

        paymentRepository.save(payment);
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
    protected void manualPaymentCheck(Long paymentId, Long userId){
        Payment payment = paymentRepository.findById(paymentId).orElse(null);
        payment.addLog(PaymentEventType.REQUIRES_MANUAL_CHECK,
                "PG 승인 후 예상치 못한 오류 발생", userId);
        paymentRepository.save(payment);
    }
}
