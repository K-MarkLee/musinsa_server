package com.mudosa.musinsa.payment.application.dto.request;

import com.mudosa.musinsa.payment.application.dto.response.TossPaymentCancelResponse;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PaymentCancelResponseDto {
    private String paymentKey;
    private LocalDateTime cancelledAt;
    private String orderNo;

    public static PaymentCancelResponseDto from(TossPaymentCancelResponse response) {
        return PaymentCancelResponseDto.builder()
                .paymentKey(response.getPaymentKey())
                .cancelledAt(response.getApprovedAt())
                .orderNo(response.getOrderId())
                .build();
    }
}
