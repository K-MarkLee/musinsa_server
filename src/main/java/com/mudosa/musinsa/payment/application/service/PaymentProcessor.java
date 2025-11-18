package com.mudosa.musinsa.payment.application.service;

import com.mudosa.musinsa.payment.application.dto.request.PaymentConfirmRequest;
import com.mudosa.musinsa.payment.application.dto.PaymentResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentProcessor {

    private final PaymentStrategyFactory strategyFactory;

    public PaymentResponseDto processPayment(PaymentConfirmRequest request) {
        
        String pgProvider = request.getPgProvider();
        PaymentStrategy strategy = strategyFactory.getStrategy(pgProvider);

        return strategy.confirmPayment(request);
    }
}
