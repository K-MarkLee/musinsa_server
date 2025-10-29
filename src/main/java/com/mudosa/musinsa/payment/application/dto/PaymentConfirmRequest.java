package com.mudosa.musinsa.payment.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentConfirmRequest {
    @NotBlank(message = "결제 키는 필수입니다")
    private String paymentKey;

    @NotNull(message = "결제 ID는 필수입니다")
    private Long paymentId;

    @NotNull(message = "주문 ID는 필수입니다")
    private Long orderId;

    @NotNull(message = "결제 금액은 필수입니다")
    private Long amount;

    @NotNull(message = "사용자 ID는 필수입니다")
    private Long userId;

    private String pgProvider = "TOSS";  // "TOSS", "KAKAO" 등
}
