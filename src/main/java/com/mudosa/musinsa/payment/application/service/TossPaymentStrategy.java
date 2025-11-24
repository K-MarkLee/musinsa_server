package com.mudosa.musinsa.payment.application.service;

import com.mudosa.musinsa.payment.application.dto.request.*;
import com.mudosa.musinsa.payment.application.dto.PaymentResponseDto;
import com.mudosa.musinsa.payment.application.dto.response.TossPaymentCancelResponse;
import com.mudosa.musinsa.payment.application.dto.response.TossPaymentConfirmResponse;
import com.mudosa.musinsa.payment.domain.model.PgProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TossPaymentStrategy implements PaymentStrategy {

	private static final PgProvider PROVIDER_NAME = PgProvider.TOSS;
	private final TossPaymentService tossPaymentService;

	@Override
	public boolean supports(PaymentContext context) {
		return context.getPgProvider() == PROVIDER_NAME;
	}

	@Override
	public PaymentResponseDto confirmPayment(PaymentConfirmRequest request) {
		TossPaymentConfirmRequest tossRequest = request.toTossRequest();
		TossPaymentConfirmResponse tossResponse = tossPaymentService.callTossApi(tossRequest);
		return PaymentResponseDto.from(tossResponse);
	}

	@Override
	public PaymentCancelResponseDto cancelPayment(PaymentCancelRequest request) {
		TossPaymentCancelRequest tossRequest = TossPaymentCancelRequest.toTossCancelRequest(request);
		TossPaymentCancelResponse tossResponse = tossPaymentService.callTossCancelApi(tossRequest);
		return PaymentCancelResponseDto.from(tossResponse);
	}
}
