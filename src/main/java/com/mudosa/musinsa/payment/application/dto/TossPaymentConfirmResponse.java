package com.mudosa.musinsa.payment.application.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TossPaymentConfirmResponse{
	private String paymentKey;
	private String status;
	private String lastTransactionKey;
	private String orderId;
	private LocalDateTime approvedAt;
	private String method;
	private Long totalAmount;
}
