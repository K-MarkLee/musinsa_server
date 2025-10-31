package com.mudosa.musinsa.payment.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "of")
public class PaymentCreationResult {
    private Long paymentId;
    private Long orderId;
    private Long userId;

}
