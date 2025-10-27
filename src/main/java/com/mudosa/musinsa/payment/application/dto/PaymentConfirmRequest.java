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

    @NotBlank(message = "주문 ID는 필수입니다")
    private String orderId;

    @NotNull(message = "결제 금액은 필수입니다")
    private Long amount;

    private String pgProvider;  // "TOSS", "KAKAO" 등
    
    private Boolean isFromCart; // 장바구니에서 온 주문인지 여부 (기본값: false)
}
