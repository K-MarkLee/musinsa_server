package com.mudosa.musinsa.payment.application.service;

import com.mudosa.musinsa.payment.application.dto.request.PaymentCancelRequest;
import com.mudosa.musinsa.payment.application.dto.request.PaymentConfirmRequest;
import com.mudosa.musinsa.payment.domain.model.PgProvider;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class PaymentContext {
    private PgProvider pgProvider;
    private PaymentType paymentType;
    private BigDecimal amount;
    private String currency;
    private String country;
    private boolean isRecurring;

    @Builder
    public PaymentContext(PgProvider pgProvider, PaymentType paymentType, BigDecimal amount, String currency, String country, boolean isRecurring) {
        this.pgProvider = pgProvider;
        this.paymentType = paymentType;
        this.amount = amount;
        this.currency = currency;
        this.country = country;
        this.isRecurring = isRecurring;
    }

    public static PaymentContext from(PaymentConfirmRequest request) {
        return PaymentContext.builder()
                .pgProvider(request.getPgProvider())
                .paymentType(PaymentType.NORMAL)
                .amount(new BigDecimal(request.getAmount()))
                .currency("KRW")
                .country("KR")
                .isRecurring(false)
                .build();
    }

    public static PaymentContext forCancel(PaymentCancelRequest request) {
        return PaymentContext.builder()
                .pgProvider(PgProvider.TOSS)
                .build();
    }
}
