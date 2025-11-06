package com.mudosa.musinsa.payment.application.service;

import com.mudosa.musinsa.exception.BusinessException;
import com.mudosa.musinsa.exception.ErrorCode;
import com.mudosa.musinsa.payment.application.dto.PaymentDetailDto;
import com.mudosa.musinsa.payment.domain.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentFetchService {
    private final PaymentRepository paymentRepository;
    public PaymentDetailDto fetchPaymentDetail(Long orderId){
        return paymentRepository.findByOrderId(orderId).map(p-> PaymentDetailDto
                .builder()
                .paymentStatus(p.getStatus())
                .approvedAt(p.getApprovedAt())
                .method(p.getMethod())
                .pgProvider(p.getPgProvider())
                .totalAmount(p.getAmount())
                .build()).orElseThrow(
                () -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND)
        );

    }
}
