package com.mudosa.musinsa.payment.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
public class TossPaymentConfirmRequest{

	@NotBlank(message = "결제 키는 필수입니다")
	private String paymentKey;

	@NotBlank(message = "주문 ID는 필수입니다")
	private String orderId;

	@NotNull(message = "결제 금액은 필수입니다")
	private Long amount;
}
