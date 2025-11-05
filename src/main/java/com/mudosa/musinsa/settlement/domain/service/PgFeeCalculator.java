package com.mudosa.musinsa.settlement.domain.service;

import com.mudosa.musinsa.common.vo.Money;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * PG 수수료 계산 도메인 서비스
 */
@Slf4j
@Component
public class PgFeeCalculator {

    private static final BigDecimal CARD_FEE_RATE = new BigDecimal("0.034");        // 3.4%
    private static final BigDecimal VIRTUAL_ACCOUNT_FEE = new BigDecimal("400");    // 건당 400원
    private static final BigDecimal EASY_PAYMENT_FEE_RATE = new BigDecimal("0.034"); // 3.4%
    private static final BigDecimal MOBILE_FEE_RATE = new BigDecimal("0.035");       // 3.5%
    private static final BigDecimal BANK_TRANSFER_FEE_RATE = new BigDecimal("0.020"); // 2.0%
    private static final BigDecimal DEFAULT_FEE_RATE = new BigDecimal("0.034");      // 기본 3.4%

    public Money calculate(String paymentMethod, Money transactionAmount) {
        if (paymentMethod == null) {
            log.warn("결제수단이 null입니다. 기본 수수료(3.4%) 적용");
            return transactionAmount.multiply(DEFAULT_FEE_RATE);
        }

        return switch (paymentMethod) {
            case "카드" -> transactionAmount.multiply(CARD_FEE_RATE);
            case "가상계좌" -> new Money(VIRTUAL_ACCOUNT_FEE);
            case "간편결제" -> transactionAmount.multiply(EASY_PAYMENT_FEE_RATE);
            case "휴대폰" -> transactionAmount.multiply(MOBILE_FEE_RATE);
            case "계좌이체" -> transactionAmount.multiply(BANK_TRANSFER_FEE_RATE);
            default -> {
                log.warn("알 수 없는 결제수단: {}. 기본 수수료(3.4%) 적용", paymentMethod);
                yield transactionAmount.multiply(DEFAULT_FEE_RATE);
            }
        };
    }
}