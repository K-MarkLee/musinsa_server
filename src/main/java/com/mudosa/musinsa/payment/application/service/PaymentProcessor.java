package com.mudosa.musinsa.payment.application.service;

import com.mudosa.musinsa.payment.application.dto.PaymentConfirmRequest;
import com.mudosa.musinsa.payment.application.dto.PaymentConfirmResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentProcessor {

    private final PaymentStrategyFactory strategyFactory;

    @Transactional
    public PaymentConfirmResponse processPayment(PaymentConfirmRequest request) {
        /* 전략 패턴으로 결제 승인 처리 */
        String pgProvider = request.getPgProvider();

        // Factory에서 적절한 전략 선택
        PaymentStrategy strategy = strategyFactory.getStrategy(pgProvider);

        // 선택된 전략으로 결제 처리
        PaymentConfirmResponse response = strategy.confirmPayment(request);

        /* 결제정보 저장 */

        /* 결제로그 저장 */

        /* 주문 상태 변경 */

        /* 재고 차감 */

        return response;
    }
}