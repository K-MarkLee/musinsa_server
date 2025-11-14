package com.mudosa.musinsa.payment.controller;

import com.mudosa.musinsa.common.dto.ApiResponse;
import com.mudosa.musinsa.exception.ErrorCode;
import com.mudosa.musinsa.payment.application.dto.PaymentConfirmRequest;
import com.mudosa.musinsa.payment.application.dto.PaymentConfirmResponse;
import com.mudosa.musinsa.payment.application.service.PaymentService;
import com.mudosa.musinsa.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = "Payment", description = "결제 API")
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

	private final PaymentService paymentService;

	@Operation(
			summary = "결제 승인",
			description = "결제 승인 요청")
	@PostMapping("/confirm")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<ApiResponse<PaymentConfirmResponse>> confirmPayment(
			@AuthenticationPrincipal CustomUserDetails userDetails,
			@Valid @RequestBody PaymentConfirmRequest request) {

		Long userId = userDetails.getUserId();

		log.info("[Payment] 결제 승인 요청, userId: {}, orderId: {}",
			userId, request.getOrderNo());

		PaymentConfirmResponse response = paymentService.confirmPaymentAndCompleteOrder(request);
		
		// 재고 부족인 경우 BAD_REQUEST로 응답
		if (response.hasInsufficientStock()) {
			log.warn("[Payment] 재고 부족으로 결제 실패, userId: {}", userId);
			
			return ResponseEntity
					.status(HttpStatus.BAD_REQUEST)
					.body(ApiResponse.failure(
							ErrorCode.INSUFFICIENT_STOCK.getCode(),
							ErrorCode.INSUFFICIENT_STOCK.getMessage(),
							response
					));
		}


		return ResponseEntity.ok(ApiResponse.success(response));
	}

	/* 결제 취소 */

	/* 결제 실패 */

}
