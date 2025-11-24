package com.mudosa.musinsa.payment.application.dto.response;

import com.mudosa.musinsa.payment.application.dto.request.PaymentCancelResponseDto;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PaymentCancelResponse {
    private String paymentKey;
    private LocalDateTime cancelledAt;

    public PaymentCancelResponse(PaymentCancelResponseDto responseDto) {
        this.paymentKey = responseDto.getPaymentKey();
        this.cancelledAt = responseDto.getCancelledAt();
    }
}
