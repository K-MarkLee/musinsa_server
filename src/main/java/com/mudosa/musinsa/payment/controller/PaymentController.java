package com.mudosa.musinsa.payment.controller;

import com.mudosa.musinsa.common.dto.ApiResponse;
import com.mudosa.musinsa.exception.ErrorCode;
import com.mudosa.musinsa.payment.application.dto.PaymentConfirmRequest;
import com.mudosa.musinsa.payment.application.dto.PaymentConfirmResponse;
import com.mudosa.musinsa.payment.application.service.PaymentService;
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

@Slf4j
@Tag(name = "Payment", description = "결제 API")
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

	private final PaymentService paymentService;

	@Operation(
			summary = "결제 승인",
			description = "결제 승인 요청")
	@PostMapping("/confirm")
	public ResponseEntity<ApiResponse<PaymentConfirmResponse>> confirmPayment(
			 @Valid @RequestBody PaymentConfirmRequest request) {

		log.info("[Payment] 결제 승인 요청, orderId: {}",
			request.getOrderNo());

		PaymentConfirmResponse response = paymentService.confirmPaymentAndCompleteOrder(request);
		
		// 재고 부족인 경우 BAD_REQUEST로 응답
		if (response.hasInsufficientStock()) {
			log.warn("[Payment] 재고 부족으로 결제 실패");
			
			return ResponseEntity
					.status(HttpStatus.BAD_REQUEST)
					.body(ApiResponse.failure(
							ErrorCode.INSUFFICIENT_STOCK.getCode(),
							ErrorCode.INSUFFICIENT_STOCK.getMessage(),
							response
					));
		}

		log.info("[Payment] 결제 승인 완료, status: {}",
				response.getStatus());

		return ResponseEntity.ok(ApiResponse.success(response));
	}
}
