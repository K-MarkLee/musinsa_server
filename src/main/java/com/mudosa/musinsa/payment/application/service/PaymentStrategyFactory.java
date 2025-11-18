package com.mudosa.musinsa.payment.application.service;

import com.mudosa.musinsa.exception.BusinessException;
import com.mudosa.musinsa.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class PaymentStrategyFactory {

    private final Map<String, PaymentStrategy> strategyMap;

    public PaymentStrategyFactory(List<PaymentStrategy> strategies) {
        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(
                        PaymentStrategy::getProviderName,
                        strategy -> strategy
                ));
    }

    public PaymentStrategy getStrategy(String pgProvider) {
        PaymentStrategy strategy = strategyMap.get(pgProvider);
        if (strategy == null) {
            throw new BusinessException(ErrorCode.PAYMENT_PG_NOT_FOUND);
        }
        return strategy;
    }
}
