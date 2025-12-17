package com.mudosa.musinsa.payment.application.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor
public class PaymentConfirmResponse {
    private String orderNo;

    @Builder
    public PaymentConfirmResponse(String orderNo) {
        this.orderNo = orderNo;
    }
}
