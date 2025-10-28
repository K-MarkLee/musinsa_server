package com.mudosa.musinsa.payment.application.service;

import com.mudosa.musinsa.payment.application.dto.PaymentConfirmRequest;
import com.mudosa.musinsa.payment.application.dto.PaymentConfirmResponse;
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

	//TODO: provider에 대한 정보를 이름만 추상메서드로 정의한게 과연 맞을까?
	@Override
	public String getProviderName() {
		return PROVIDER_NAME;
	}

	@Override
	public PaymentConfirmResponse confirmPayment(PaymentConfirmRequest request) {
		log.info("[KAKAO FAKE] 결제 승인 요청 - orderId: {}", request.getOrderId());

		// Fake 응답 반환 (실제 API 호출 없음)
		return PaymentConfirmResponse.builder()
				.paymentKey(request.getPaymentKey())
				.orderId(request.getOrderId())
				.status("DONE")
				.pgProvider(PROVIDER_NAME)
				.build();
	}
}
