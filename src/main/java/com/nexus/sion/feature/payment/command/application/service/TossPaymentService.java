package com.nexus.sion.feature.payment.command.application.service;

import com.mudosa.musinsa.exception.BusinessException;
import com.mudosa.musinsa.exception.ErrorCode;
import com.nexus.sion.feature.payment.command.application.dto.request.PaymentConfirmRequest;
import com.nexus.sion.feature.payment.command.application.dto.response.PaymentConfirmResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * 토스페이먼츠 결제 승인 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TossPaymentService {

	private static final String TOSS_PAYMENTS_CONFIRM_URL =
			"https://api.tosspayments.com/v1/payments/confirm";

	private final RestTemplate restTemplate;

	@Value("${tosspayments.secret-key}")
	private String tossPaymentsSecretKey;

	/**
	 * 토스페이먼츠 결제 승인 요청
	 *
	 * @param request 결제 승인 요청 정보
	 * @return 결제 승인 응답
	 */
	public PaymentConfirmResponse confirmPayment(PaymentConfirmRequest request) {
		try {
			// Basic 인증 헤더 생성 (시크릿 키 + ':' 를 Base64 인코딩)
			String encodedAuth = createBasicAuthHeader(tossPaymentsSecretKey);

			// HTTP 헤더 설정
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.set(HttpHeaders.AUTHORIZATION, "Basic " + encodedAuth);

			// HTTP 엔티티 생성
			HttpEntity<PaymentConfirmRequest> entity = new HttpEntity<>(request, headers);

			// 토스페이먼츠 API 호출
			log.info(
					"토스페이먼츠 결제 승인 요청 - orderId: {}, paymentKey: {}, amount: {}",
					request.getOrderId(),
					request.getPaymentKey(),
					request.getAmount());

			ResponseEntity<PaymentConfirmResponse> response =
					restTemplate.exchange(
							TOSS_PAYMENTS_CONFIRM_URL,
							HttpMethod.POST,
							entity,
							PaymentConfirmResponse.class);

			PaymentConfirmResponse responseBody = response.getBody();

			if (responseBody == null) {
				log.error("토스페이먼츠 결제 승인 응답이 null입니다");
				throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
			}

			log.info(
					"토스페이먼츠 결제 승인 성공 - orderId: {}, status: {}",
					responseBody.getOrderId(),
					responseBody.getStatus());

			return responseBody;

		} catch (HttpClientErrorException e) {
			log.error("토스페이먼츠 결제 승인 실패 - status: {}, body: {}", e.getStatusCode(), e.getResponseBodyAsString());
			throw new BusinessException(ErrorCode.PAYMENT_APPROVAL_FAILED);
		} catch (Exception e) {
			log.error("토스페이먼츠 결제 승인 중 예외 발생", e);
			throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * Basic 인증 헤더 생성
	 * 시크릿 키 뒤에 ':' 를 추가하고 Base64로 인코딩
	 *
	 * @param secretKey 토스페이먼츠 시크릿 키
	 * @return Base64로 인코딩된 인증 문자열
	 */
	private String createBasicAuthHeader(String secretKey) {
		String auth = secretKey + ":";
		return Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
	}
}
