package com.mudosa.musinsa.payment.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentConfirmResponse {
    private String paymentKey;
    private String orderId;
    private String status;
    private Long amount;
    private String pgProvider;
    private String method;
    private LocalDateTime approvedAt;
}
