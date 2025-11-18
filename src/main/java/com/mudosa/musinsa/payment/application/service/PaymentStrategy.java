package com.mudosa.musinsa.payment.application.service;

import com.mudosa.musinsa.payment.application.dto.request.PaymentConfirmRequest;
import com.mudosa.musinsa.payment.application.dto.PaymentResponseDto;

public interface PaymentStrategy {
    PaymentResponseDto confirmPayment(PaymentConfirmRequest request);
    String getProviderName();
}
