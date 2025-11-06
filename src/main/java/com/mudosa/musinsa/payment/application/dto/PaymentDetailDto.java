package com.mudosa.musinsa.payment.application.dto;

import com.mudosa.musinsa.payment.domain.model.PaymentStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@Getter
public class PaymentDetailDto {
    private BigDecimal totalAmount;
    private PaymentStatus paymentStatus;
    private String pgProvider;
    private LocalDateTime approvedAt;
    private String method;
}
