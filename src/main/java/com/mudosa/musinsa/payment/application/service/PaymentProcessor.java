package com.mudosa.musinsa.payment.application.service;

import com.mudosa.musinsa.payment.application.dto.request.PaymentConfirmRequest;
import com.mudosa.musinsa.payment.application.dto.PaymentResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentProcessor {

    private final PaymentStrategyFactory strategyFactory;

    public PaymentResponseDto processPayment(PaymentConfirmRequest request) {
        PaymentContext context = PaymentContext.from(request);

        PaymentStrategy strategy = strategyFactory.getStrategy(context);

        return strategy.confirmPayment(request);
    }
}
