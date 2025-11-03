package com.mudosa.musinsa.payment.application.service;

import com.mudosa.musinsa.exception.BusinessException;
import com.mudosa.musinsa.exception.ErrorCode;
import com.mudosa.musinsa.payment.application.dto.*;

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


@Slf4j
@Service
@RequiredArgsConstructor
public class TossPaymentStrategy implements PaymentStrategy {

	private static final String TOSS_PAYMENTS_CONFIRM_URL =
			"https://api.tosspayments.com/v1/payments/confirm";

	private final RestTemplate restTemplate;

	private static final String PROVIDER_NAME = "TOSS";

	@Value("${tosspayments.secret-key}")
	private String tossPaymentsSecretKey;

	@Override
	public String getProviderName() {
		return PROVIDER_NAME;
	}

	public PaymentResponseDto confirmPayment(PaymentConfirmRequest request) {
		TossPaymentConfirmRequest tossRequest = request.toTossRequest();

		TossPaymentConfirmResponse tossResponse = callTossApi(tossRequest);

		return PaymentResponseDto.builder()
				.paymentKey(tossResponse.getPaymentKey())
				.orderNo(tossResponse.getOrderId())
				.status(tossResponse.getStatus())
				.method(tossResponse.getMethod())
				.totalAmount(tossResponse.getTotalAmount())
				.pgProvider("TOSS")
				.approvedAt(tossResponse.getApprovedAt())
				.build();
	}

	private TossPaymentConfirmResponse callTossApi(TossPaymentConfirmRequest request) {
		try {
			String encodedAuth = createBasicAuthHeader(tossPaymentsSecretKey);

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.set(HttpHeaders.AUTHORIZATION, "Basic " + encodedAuth);

			HttpEntity<TossPaymentConfirmRequest> entity = new HttpEntity<>(request, headers);

			log.info(
					"[TossPayments] 결제 승인 요청 - orderId: {}, paymentKey: {}, amount: {}",
					request.getOrderId(),
					request.getPaymentKey(),
					request.getAmount());

			ResponseEntity<TossPaymentConfirmResponse> response =
					restTemplate.exchange(
							TOSS_PAYMENTS_CONFIRM_URL,
							HttpMethod.POST,
							entity,
							TossPaymentConfirmResponse.class);

			TossPaymentConfirmResponse responseBody = response.getBody();

			if (responseBody == null) {
				log.error("[TossPayments] 결제 승인 응답이 null입니다");
				throw new BusinessException(ErrorCode.PAYMENT_APPROVAL_FAILED);
			}

			log.info(
					"[TossPayments] 결제 승인 성공 - orderId: {}, status: {}, method: {}",
					responseBody.getOrderId(),
					responseBody.getStatus());

			return responseBody;

		} catch (HttpClientErrorException e) {
			log.error(
					"[TossPayments] 결제 승인 실패 - status: {}, body: {}",
					e.getStatusCode(),
					e.getResponseBodyAsString());
			throw new BusinessException(ErrorCode.PAYMENT_APPROVAL_FAILED, e.getMessage());
		} catch (Exception e) {
			log.error("[TossPayments] 결제 승인 중 예외 발생", e);
			throw new BusinessException(ErrorCode.PAYMENT_APPROVAL_FAILED, e.getMessage());
		}
	}

	private String createBasicAuthHeader(String secretKey) {
		String auth = secretKey + ":";
		return Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
	}
}
