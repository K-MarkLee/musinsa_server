package com.mudosa.musinsa.payment.application.dto;

import com.mudosa.musinsa.payment.domain.model.PgProvider;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class PaymentCreateDto {
    private PgProvider pgProvider;
    private BigDecimal totalAmount;
    private String orderNo;

    @Builder
    public PaymentCreateDto(PgProvider pgProvider, BigDecimal totalAmount, String orderNo) {
        this.pgProvider = pgProvider;
        this.totalAmount = totalAmount;
        this.orderNo = orderNo;
    }
}
