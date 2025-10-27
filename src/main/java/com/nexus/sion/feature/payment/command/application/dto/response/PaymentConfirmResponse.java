package com.nexus.sion.feature.payment.command.application.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 토스페이먼츠 결제 승인 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentConfirmResponse {

	@JsonProperty("mId")
	private String mId;

	private String version;
	private String paymentKey;
	private String status;
	private String lastTransactionKey;
	private String orderId;
	private String orderName;
	private LocalDateTime requestedAt;
	private LocalDateTime approvedAt;
	private Boolean useEscrow;
	private Boolean cultureExpense;

	private CardInfo card;
	private String type;
	private String country;
	private String currency;

	private Long totalAmount;
	private Long balanceAmount;
	private Long suppliedAmount;
	private Long vat;
	private Long taxFreeAmount;
	private String method;

	private ReceiptInfo receipt;

	@Getter
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class CardInfo {
		private String issuerCode;
		private String acquirerCode;
		private String number;
		private Integer installmentPlanMonths;
		private Boolean isInterestFree;
		private String approveNo;
		private Boolean useCardPoint;
		private String cardType;
		private String ownerType;
		private String acquireStatus;
		private Long amount;
	}

	@Getter
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class ReceiptInfo {
		private String url;
	}
}
