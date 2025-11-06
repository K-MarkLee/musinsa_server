package com.mudosa.musinsa.payment.application.service;

import com.mudosa.musinsa.payment.application.dto.PaymentConfirmRequest;
import com.mudosa.musinsa.payment.application.dto.PaymentConfirmResponse;
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
        log.info("PG사 결제 승인 시작 - pgProvider: {}, paymentKey: {}", 
            request.getPgProvider(), request.getPaymentKey());
        
        String pgProvider = request.getPgProvider();
        PaymentStrategy strategy = strategyFactory.getStrategy(pgProvider);

        PaymentResponseDto response = strategy.confirmPayment(request);
        
        log.info("PG사 결제 승인 완료 - pgProvider: {}, paymentKey: {}", 
            pgProvider, response.getPaymentKey());
        
        return response;
    }
}
