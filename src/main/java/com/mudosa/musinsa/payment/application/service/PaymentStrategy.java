package com.mudosa.musinsa.payment.application.service;

import com.mudosa.musinsa.payment.application.dto.request.PaymentCancelRequest;
import com.mudosa.musinsa.payment.application.dto.request.PaymentCancelResponseDto;
import com.mudosa.musinsa.payment.application.dto.request.PaymentConfirmRequest;
import com.mudosa.musinsa.payment.application.dto.PaymentResponseDto;
import com.mudosa.musinsa.payment.domain.model.PgProvider;

public interface PaymentStrategy {
    PaymentResponseDto confirmPayment(PaymentConfirmRequest request);
    boolean supports(PaymentContext context);
    PaymentCancelResponseDto cancelPayment(PaymentCancelRequest request);
}
