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

    @NotNull(message = "주문 ID는 필수입니다")
    private String orderNo;

    @NotNull(message = "결제 금액은 필수입니다")
    private Long amount;

    @NotNull(message = "사용자 ID는 필수입니다")
    private Long userId;

    private String pgProvider = "TOSS";  // "TOSS", "KAKAO" 등

    private Long couponId;  // 적용할 쿠폰 ID (선택)

    public TossPaymentConfirmRequest toTossRequest() {
        TossPaymentConfirmRequest request = new TossPaymentConfirmRequest();
        request.setPaymentKey(this.paymentKey);
        request.setOrderId(this.orderNo);
        request.setAmount(this.amount);
        return request;
    }
}
