package com.mudosa.musinsa.payment.application.service;

import com.mudosa.musinsa.payment.application.dto.PaymentConfirmRequest;
import com.mudosa.musinsa.payment.application.dto.PaymentConfirmResponse;

public interface PaymentStrategy {
    PaymentConfirmResponse confirmPayment(PaymentConfirmRequest request);
    String getProviderName();
}
