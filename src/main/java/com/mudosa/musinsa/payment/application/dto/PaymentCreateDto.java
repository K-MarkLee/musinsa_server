package com.mudosa.musinsa.payment.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class PaymentCreateDto {
    private String pgProvider;
    private BigDecimal totalAmount;
    private String orderNo;

    @Builder
    public PaymentCreateDto(String pgProvider, BigDecimal totalAmount, String orderNo) {
        this.pgProvider = pgProvider;
        this.totalAmount = totalAmount;
        this.orderNo = orderNo;
    }
}
