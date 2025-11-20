package com.mudosa.musinsa.payment.application.dto;

import com.mudosa.musinsa.payment.application.dto.response.TossPaymentConfirmResponse;
import com.mudosa.musinsa.payment.domain.model.PgProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponseDto {
    private String orderNo;
    private String paymentKey;
    private LocalDateTime approvedAt;
    private String method;
    private Long totalAmount;
    private String pgProvider;

    public static PaymentResponseDto from(TossPaymentConfirmResponse tossResponse){
        return PaymentResponseDto.builder()
                .paymentKey(tossResponse.getPaymentKey())
                .orderNo(tossResponse.getOrderId())
                .method(tossResponse.getMethod())
                .totalAmount(tossResponse.getTotalAmount())
                .pgProvider(PgProvider.TOSS.name())
                .approvedAt(tossResponse.getApprovedAt())
                .build();
    }
}
