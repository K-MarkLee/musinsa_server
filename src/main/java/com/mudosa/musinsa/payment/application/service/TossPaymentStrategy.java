package com.mudosa.musinsa.payment.application.service;

import com.mudosa.musinsa.common.client.ClientRequest;
import com.mudosa.musinsa.common.client.ClientResponse;
import com.mudosa.musinsa.common.client.ExternalApiClient;
import com.mudosa.musinsa.common.client.HttpHeadersBuilder;
import com.mudosa.musinsa.exception.BusinessException;
import com.mudosa.musinsa.exception.ErrorCode;
import com.mudosa.musinsa.exception.ExternalApiException;
import com.mudosa.musinsa.payment.application.dto.request.PaymentConfirmRequest;
import com.mudosa.musinsa.payment.application.dto.PaymentResponseDto;
import com.mudosa.musinsa.payment.application.dto.request.TossPaymentConfirmRequest;
import com.mudosa.musinsa.payment.application.dto.response.TossPaymentConfirmResponse;
import com.mudosa.musinsa.payment.domain.model.PgProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.*;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class TossPaymentStrategy implements PaymentStrategy {

	private static final String TOSS_PAYMENTS_CONFIRM_URL =
			"https://api.tosspayments.com/v1/payments/confirm";

	private static final PgProvider PROVIDER_NAME = PgProvider.TOSS;

	@Value("${tosspayments.secret-key}")
	private String tossPaymentsSecretKey;

	private final ExternalApiClient apiClient;
	private final HttpHeadersBuilder headersBuilder;

	@Lazy
	@Autowired
	private TossPaymentStrategy self;

	@Override
	public boolean supports(PaymentContext context) {
		return context.getPgProvider() == PROVIDER_NAME
				&& context.getAmount().compareTo(BigDecimal.valueOf(100_000)) <= 0
				&& context.getPaymentType() == PaymentType.NORMAL;
	}

	public PaymentResponseDto confirmPayment(PaymentConfirmRequest request) {
		TossPaymentConfirmRequest tossRequest = request.toTossRequest();

		TossPaymentConfirmResponse tossResponse = self.callTossApi(tossRequest);

		return PaymentResponseDto.from(tossResponse);
	}

	@Retryable(
			retryFor = ExternalApiException.class,
			noRetryFor = BusinessException.class,
			maxAttempts = 3,
			backoff = @Backoff(delay = 1000, multiplier = 2.0, maxDelay = 10000)
	)
	public TossPaymentConfirmResponse callTossApi(TossPaymentConfirmRequest request) {
		//헤더 생성
		HttpHeaders headers = headersBuilder.basicAuth(tossPaymentsSecretKey);

		//API 요청 구성
		ClientRequest<TossPaymentConfirmRequest> apiRequest =
				ClientRequest.<TossPaymentConfirmRequest>post(
								TOSS_PAYMENTS_CONFIRM_URL,
								TossPaymentConfirmResponse.class
						)
						.body(request)
						.headers(headers)
						.build();

		try{
			//API 호출
			ClientResponse<TossPaymentConfirmResponse> response =
					apiClient.execute(apiRequest);

			//응답 검증
			if (response.getBody() == null) {
				throw new BusinessException(ErrorCode.PAYMENT_APPROVAL_FAILED);
			}

			return response.getBody();

		}catch (ExternalApiException e) {
			if (e.getHttpStatus() == HttpStatus.REQUEST_TIMEOUT) {
				log.error("[Toss] 타임아웃 발생 - orderNo: {}", request.getOrderId());
				throw new BusinessException(
						ErrorCode.PAYMENT_TIMEOUT,
						"결제 처리 시간이 초과되었습니다"
				);
			}

			throw e;
		}
	}

	@Recover
	public TossPaymentConfirmResponse recover(
			ExternalApiException e,
			TossPaymentConfirmRequest request
	) {
		log.error("[Toss] 모든 재시도 실패 - orderNo: {}, error: {}",
				request.getOrderId(),
				e.getMessage());
		throw new BusinessException(
				ErrorCode.PAYMENT_APPROVAL_FAILED,
				e.getMessage()
		);
	}

	@Recover
	public TossPaymentConfirmResponse recover(
			BusinessException e,
			TossPaymentConfirmRequest request
	) {
		log.error("[Toss] 응답 검증 실패 - orderNo: {}, error: {}",
				request.getOrderId(),
				e.getMessage());
		throw e;
	}
}
