package com.nexus.sion.feature.payment.command.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 토스페이먼츠 결제 승인 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentConfirmRequest {

	@NotBlank(message = "결제 키는 필수입니다")
	private String paymentKey;

	@NotBlank(message = "주문 ID는 필수입니다")
	private String orderId;

	@NotNull(message = "결제 금액은 필수입니다")
	private Long amount;
}
