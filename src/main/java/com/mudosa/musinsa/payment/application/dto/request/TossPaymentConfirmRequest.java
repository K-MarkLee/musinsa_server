package com.mudosa.musinsa.payment.application.dto.request;

import lombok.*;

@Getter
public class TossPaymentConfirmRequest{

	private String paymentKey;

	private String orderId;

	private Long amount;

	@Builder
	private TossPaymentConfirmRequest(String paymentKey, String orderId, Long amount) {
		this.paymentKey = paymentKey;
		this.orderId = orderId;
		this.amount = amount;
	}
}
