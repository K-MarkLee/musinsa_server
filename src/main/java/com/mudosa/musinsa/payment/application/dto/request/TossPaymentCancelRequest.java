package com.mudosa.musinsa.payment.application.dto.request;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TossPaymentCancelRequest {
    private String paymentKey;
    private String cancelReason;

    public static TossPaymentCancelRequest toTossCancelRequest(PaymentCancelRequest request) {
        return TossPaymentCancelRequest.builder()
                .paymentKey(request.getPaymentTransactionId())
                .cancelReason(request.getCancelReason())
                .build();
    }
}
