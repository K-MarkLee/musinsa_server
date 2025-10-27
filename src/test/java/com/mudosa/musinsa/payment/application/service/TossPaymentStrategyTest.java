package com.mudosa.musinsa.payment.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mudosa.musinsa.exception.BusinessException;
import com.mudosa.musinsa.payment.application.dto.PaymentConfirmRequest;
import com.mudosa.musinsa.payment.application.dto.PaymentConfirmResponse;
import com.mudosa.musinsa.payment.application.dto.TossPaymentConfirmRequest;
import com.mudosa.musinsa.payment.application.dto.TossPaymentConfirmResponse;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
@DisplayName("Toss 결제 전략 단위 테스트")
class TossPaymentStrategyTest {

	@Mock
	private RestTemplate restTemplate;

	@InjectMocks
	private TossPaymentStrategy tossPaymentStrategy;

	private static final String TEST_SECRET_KEY = "test_sk_key";

	@BeforeEach
	void setUp() {
		// @Value 주입을 ReflectionTestUtils로 대체
		ReflectionTestUtils.setField(tossPaymentStrategy, "tossPaymentsSecretKey", TEST_SECRET_KEY);
	}

	@Test
	@DisplayName("결제 승인 성공 - 정상적인 응답 반환")
	void confirmPayment_Success() {
		// given
		PaymentConfirmRequest request = PaymentConfirmRequest.builder()
				.paymentKey("test_payment_key_123")
				.orderId("ORDER_20250124_001")
				.amount(15000L)
				.pgProvider("TOSS")
				.build();

		TossPaymentConfirmResponse tossResponse = TossPaymentConfirmResponse.builder()
				.paymentKey("test_payment_key_123")
				.orderId("ORDER_20250124_001")
				.status("DONE")
				.totalAmount(15000L)
				.approvedAt(LocalDateTime.now())
				.build();

		when(restTemplate.exchange(
				any(String.class),
				eq(HttpMethod.POST),
				any(HttpEntity.class),
				eq(TossPaymentConfirmResponse.class)))
				.thenReturn(ResponseEntity.ok(tossResponse));

		// when
		PaymentConfirmResponse response = tossPaymentStrategy.confirmPayment(request);

		// then
		assertThat(response).isNotNull();
		assertThat(response.getPaymentKey()).isEqualTo("test_payment_key_123");
		assertThat(response.getOrderId()).isEqualTo("ORDER_20250124_001");
		assertThat(response.getStatus()).isEqualTo("DONE");
		assertThat(response.getPgProvider()).isEqualTo("TOSS");

		verify(restTemplate).exchange(
				any(String.class),
				eq(HttpMethod.POST),
				any(HttpEntity.class),
				eq(TossPaymentConfirmResponse.class));
	}

	@Test
	@DisplayName("결제 승인 실패 - Toss API 응답이 null")
	void confirmPayment_Fail_NullResponse() {
		// given
		PaymentConfirmRequest request = PaymentConfirmRequest.builder()
				.paymentKey("test_payment_key_123")
				.orderId("ORDER_20250124_001")
				.amount(15000L)
				.pgProvider("TOSS")
				.build();

		when(restTemplate.exchange(
				any(String.class),
				eq(HttpMethod.POST),
				any(HttpEntity.class),
				eq(TossPaymentConfirmResponse.class)))
				.thenReturn(ResponseEntity.ok(null));

		// when & then
		assertThatThrownBy(() -> tossPaymentStrategy.confirmPayment(request))
				.isInstanceOf(BusinessException.class)
				.hasMessageContaining("결제 승인");
	}

	@Test
	@DisplayName("결제 승인 실패 - Toss API HttpClientErrorException 발생")
	void confirmPayment_Fail_HttpClientError() {
		// given
		PaymentConfirmRequest request = PaymentConfirmRequest.builder()
				.paymentKey("invalid_key")
				.orderId("ORDER_20250124_001")
				.amount(15000L)
				.pgProvider("TOSS")
				.build();

		when(restTemplate.exchange(
				any(String.class),
				eq(HttpMethod.POST),
				any(HttpEntity.class),
				eq(TossPaymentConfirmResponse.class)))
				.thenThrow(HttpClientErrorException.BadRequest.create(
						org.springframework.http.HttpStatus.BAD_REQUEST,
						"Bad Request",
						org.springframework.http.HttpHeaders.EMPTY,
						new byte[0],
						null));

		// when & then
		assertThatThrownBy(() -> tossPaymentStrategy.confirmPayment(request))
				.isInstanceOf(BusinessException.class);
	}

	@Test
	@DisplayName("PG사 이름 반환 확인")
	void getProviderName() {
		// when
		String providerName = tossPaymentStrategy.getProviderName();

		// then
		assertThat(providerName).isEqualTo("TOSS");
	}

	@Test
	@DisplayName("결제 승인 요청 - 금액이 0원")
	void confirmPayment_ZeroAmount() {
		// given
		PaymentConfirmRequest request = PaymentConfirmRequest.builder()
				.paymentKey("test_payment_key_123")
				.orderId("ORDER_20250124_001")
				.amount(0L)
				.pgProvider("TOSS")
				.build();

		TossPaymentConfirmResponse tossResponse = TossPaymentConfirmResponse.builder()
				.paymentKey("test_payment_key_123")
				.orderId("ORDER_20250124_001")
				.status("DONE")
				.totalAmount(0L)
				.approvedAt(LocalDateTime.now())
				.build();

		when(restTemplate.exchange(
				any(String.class),
				eq(HttpMethod.POST),
				any(HttpEntity.class),
				eq(TossPaymentConfirmResponse.class)))
				.thenReturn(ResponseEntity.ok(tossResponse));

		// when
		PaymentConfirmResponse response = tossPaymentStrategy.confirmPayment(request);

		// then
		assertThat(response).isNotNull();
		assertThat(response.getStatus()).isEqualTo("DONE");
	}
}
