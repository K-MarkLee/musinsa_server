package com.mudosa.musinsa.payment.application.service;

import com.mudosa.musinsa.payment.application.dto.request.PaymentConfirmRequest;
import com.mudosa.musinsa.payment.application.dto.PaymentResponseDto;
import com.mudosa.musinsa.payment.domain.model.PgProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoFakePaymentStrategy implements PaymentStrategy {

	private static final PgProvider PROVIDER_NAME = PgProvider.KAKAO;

	@Override
	public boolean supports(PaymentContext context) {
		return false;
	}

	@Override
	public PaymentResponseDto confirmPayment(PaymentConfirmRequest request) {
		log.info("[KAKAO FAKE] 결제 승인 요청 - orderId: {}", request.getOrderNo());

		// Fake 응답 반환 (실제 API 호출 없음)
		return PaymentResponseDto.builder()
		        .pgProvider(PROVIDER_NAME.name())
		        .build();
	}
}
