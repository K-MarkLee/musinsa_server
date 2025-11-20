package com.mudosa.musinsa.payment.application.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class TossPaymentConfirmResponse{
	private String paymentKey;
	private String status;
	private String lastTransactionKey;
	private String orderId;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
	private LocalDateTime approvedAt;
	private String method;
	private Long totalAmount;
}
