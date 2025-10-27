package com.mudosa.musinsa.payment.controller;

import com.mudosa.musinsa.common.dto.ApiResponse;
import com.mudosa.musinsa.payment.application.dto.PaymentConfirmRequest;
import com.mudosa.musinsa.payment.application.dto.PaymentConfirmResponse;
import com.mudosa.musinsa.payment.application.dto.TossPaymentConfirmRequest;
import com.mudosa.musinsa.payment.application.dto.TossPaymentConfirmResponse;
import com.mudosa.musinsa.payment.application.service.PaymentProcessor;
import com.mudosa.musinsa.payment.application.service.PaymentStrategy;
import com.mudosa.musinsa.payment.application.service.TossPaymentStrategy;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@Tag(name = "Payment", description = "결제 API")
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

	private final PaymentProcessor paymentProcessor;

	@Operation(
			summary = "결제 승인",
			description = "결제 승인을 요청합니다. 결제창에서 결제 인증 완료 후 호출해야 합니다.")
	@PostMapping("/confirm")
	public ResponseEntity<ApiResponse<PaymentConfirmResponse>> confirmPayment(
			@Valid @RequestBody PaymentConfirmRequest request) {

		log.info("[Payment] 결제 승인 요청 - orderId: {}", request.getOrderId());

		PaymentConfirmResponse response = paymentProcessor.processPayment(request);

		log.info("[Payment] 결제 승인 완료 - orderId: {}, status: {}", 
				response.getOrderId(), 
				response.getStatus());

		return ResponseEntity.ok(ApiResponse.success(response));
	}
}
