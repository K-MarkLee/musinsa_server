package com.mudosa.musinsa.payment.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mudosa.musinsa.payment.application.dto.PaymentConfirmRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("결제 컨트롤러 통합 테스트")
class PaymentControllerIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;


	@Test
	@DisplayName("[통합] POST /api/v1/payments/confirm - paymentKey 누락")
	void confirmPayment_MissingPaymentKey() throws Exception {
		// given
		PaymentConfirmRequest request = PaymentConfirmRequest.builder()
				.orderId("ORDER_20250124_001")
				.amount(15000L)
				.pgProvider("TOSS")
				.build();

		// when & then
		mockMvc.perform(post("/api/v1/payments/confirm")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andDo(print())
				.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("[통합] POST /api/v1/payments/confirm - amount가 null")
	void confirmPayment_NullAmount() throws Exception {
		// given
		PaymentConfirmRequest request = PaymentConfirmRequest.builder()
				.paymentKey("test_payment_key_123")
				.orderId("ORDER_20250124_001")
				.pgProvider("TOSS")
				.build();

		// when & then
		mockMvc.perform(post("/api/v1/payments/confirm")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andDo(print())
				.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("[통합] POST /api/v1/payments/confirm - 지원하지 않는 PG사 (400)")
	void confirmPayment_UnsupportedPgProvider() throws Exception {
		// given
		PaymentConfirmRequest request = PaymentConfirmRequest.builder()
				.paymentKey("test_payment_key_123")
				.orderId("ORDER_20250124_001")
				.amount(15000L)
				.pgProvider("INVALID_PG")
				.build();

		// when & then
		mockMvc.perform(post("/api/v1/payments/confirm")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andDo(print())
				.andExpect(status().is4xxClientError());
	}
}
