package com.mudosa.musinsa.payment.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mudosa.musinsa.exception.BusinessException;
import com.mudosa.musinsa.exception.ErrorCode;
import com.mudosa.musinsa.payment.application.dto.PaymentConfirmRequest;
import com.mudosa.musinsa.payment.application.dto.PaymentConfirmResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
@DisplayName("결제 프로세서 단위 테스트")
class PaymentProcessorTest {

	@Mock
	private PaymentStrategyFactory strategyFactory;

	@Mock
	private PaymentStrategy paymentStrategy;

	@InjectMocks
	private PaymentProcessor paymentProcessor;

	@Test
	@DisplayName("결제 처리 성공 - TOSS 전략 선택 및 실행")
	void processPayment_Success() {
		// given
		PaymentConfirmRequest request = PaymentConfirmRequest.builder()
				.paymentKey("test_key")
				.orderId("ORDER_001")
				.amount(10000L)
				.pgProvider("TOSS")
				.build();

		PaymentConfirmResponse expectedResponse = PaymentConfirmResponse.builder()
				.paymentKey("test_key")
				.orderId("ORDER_001")
				.status("DONE")
				.pgProvider("TOSS")
				.build();

		when(strategyFactory.getStrategy("TOSS")).thenReturn(paymentStrategy);
		when(paymentStrategy.confirmPayment(request)).thenReturn(expectedResponse);

		// when
		PaymentConfirmResponse response = paymentProcessor.processPayment(request);

		// then
		assertThat(response).isNotNull();
		assertThat(response.getPaymentKey()).isEqualTo("test_key");
		assertThat(response.getOrderId()).isEqualTo("ORDER_001");
		assertThat(response.getStatus()).isEqualTo("DONE");
		assertThat(response.getPgProvider()).isEqualTo("TOSS");

		verify(strategyFactory).getStrategy("TOSS");
		verify(paymentStrategy).confirmPayment(request);
	}

	@Test
	@DisplayName("결제 처리 실패 - 존재하지 않는 PG사")
	void processPayment_Fail_InvalidPgProvider() {
		// given
		PaymentConfirmRequest request = PaymentConfirmRequest.builder()
				.paymentKey("test_key")
				.orderId("ORDER_001")
				.amount(10000L)
				.pgProvider("INVALID_PG")
				.build();

		when(strategyFactory.getStrategy("INVALID_PG"))
				.thenThrow(new BusinessException(ErrorCode.PAYMENT_PG_NOT_FOUND));

		// when & then
		assertThatThrownBy(() -> paymentProcessor.processPayment(request))
				.isInstanceOf(BusinessException.class);

		verify(strategyFactory).getStrategy("INVALID_PG");
	}

	@Test
	@DisplayName("결제 처리 실패 - 전략 실행 중 예외 발생")
	void processPayment_Fail_StrategyException() {
		// given
		PaymentConfirmRequest request = PaymentConfirmRequest.builder()
				.paymentKey("test_key")
				.orderId("ORDER_001")
				.amount(10000L)
				.pgProvider("TOSS")
				.build();

		when(strategyFactory.getStrategy("TOSS")).thenReturn(paymentStrategy);
		when(paymentStrategy.confirmPayment(any()))
				.thenThrow(new BusinessException(ErrorCode.PAYMENT_APPROVAL_FAILED));

		// when & then
		assertThatThrownBy(() -> paymentProcessor.processPayment(request))
				.isInstanceOf(BusinessException.class);

		verify(strategyFactory).getStrategy("TOSS");
		verify(paymentStrategy).confirmPayment(request);
	}
}
