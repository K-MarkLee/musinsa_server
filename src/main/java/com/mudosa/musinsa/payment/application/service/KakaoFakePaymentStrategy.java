package com.mudosa.musinsa.payment.application.service;

import com.mudosa.musinsa.payment.application.dto.request.PaymentConfirmRequest;
import com.mudosa.musinsa.payment.application.dto.PaymentResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Profile("test")  // ← 테스트 환경에서만 활성화
@RequiredArgsConstructor
public class KakaoFakePaymentStrategy implements PaymentStrategy {

	private static final String PROVIDER_NAME = "KAKAO";


	@Override
	public String getProviderName() {
		return PROVIDER_NAME;
	}

	@Override
	public PaymentResponseDto confirmPayment(PaymentConfirmRequest request) {
		log.info("[KAKAO FAKE] 결제 승인 요청 - orderId: {}", request.getOrderNo());

		// Fake 응답 반환 (실제 API 호출 없음)
		return PaymentResponseDto.builder()
				.status("DONE")
				.pgProvider(PROVIDER_NAME)
				.build();
	}
}
