package com.nexus.sion.feature.payment.command.application.controller;

import com.mudosa.musinsa.common.dto.ApiResponse;
import com.nexus.sion.feature.payment.command.application.dto.request.PaymentConfirmRequest;
import com.nexus.sion.feature.payment.command.application.dto.response.PaymentConfirmResponse;
import com.nexus.sion.feature.payment.command.application.service.TossPaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 결제 관련 API 컨트롤러
 */
@Slf4j
@Tag(name = "Payment", description = "결제 API")
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

	private final TossPaymentService tossPaymentService;

	@Operation(summary = "결제 승인", description = "토스페이먼츠 결제 승인을 요청합니다")
	@PostMapping("/confirm")
	public ResponseEntity<ApiResponse<PaymentConfirmResponse>> confirmPayment(
			@Valid @RequestBody PaymentConfirmRequest request) {

		log.info("결제 승인 요청 - orderId: {}", request.getOrderId());

		PaymentConfirmResponse response = tossPaymentService.confirmPayment(request);

		return ResponseEntity.status(HttpStatus.OK)
				.body(ApiResponse.success(response, "결제 승인에 성공했습니다"));
	}
}
