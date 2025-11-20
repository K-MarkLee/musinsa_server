package com.mudosa.musinsa.order.controller;

import com.mudosa.musinsa.common.dto.ApiResponse;
import com.mudosa.musinsa.order.application.OrderService;
import com.mudosa.musinsa.order.application.dto.*;
import com.mudosa.musinsa.order.application.dto.request.OrderCreateRequest;
import com.mudosa.musinsa.order.application.dto.response.OrderCreateResponse;
import com.mudosa.musinsa.order.application.dto.response.OrderDetailResponse;
import com.mudosa.musinsa.order.application.dto.response.OrderListResponse;
import com.mudosa.musinsa.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;


@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/api/orders")
@Tag(name="Order", description = "주문 API")
public class OrderController {
    private final OrderService orderService;

    @Operation(
            summary = "주문 생성",
            description = "주문을 생성합니다"
    )
    @PostMapping
    public ResponseEntity<ApiResponse<OrderCreateResponse>> createOrder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody OrderCreateRequest request)
    {
        Long userId = userDetails.getUserId();

        OrderCreateResponse response = orderService.createPendingOrder(request, userId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(
            summary = "주문서 조회",
            description = "생성한 주문을 조회합니다."
    )
    @GetMapping("/{orderNo}/pending")
    public ResponseEntity<ApiResponse<PendingOrderResponse>> fetchOrder(
            @PathVariable String orderNo
    ){
        PendingOrderResponse response = orderService.fetchPendingOrder(orderNo);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

}
