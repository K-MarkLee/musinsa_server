package com.mudosa.musinsa.payment.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor(staticName = "of")
public class OrderValidationResult {
    private Long orderId;
    private Long userId;
    private BigDecimal finalAmount;
    private BigDecimal discountAmount;
}
