package com.mudosa.musinsa.order.controller;

import com.mudosa.musinsa.common.dto.ApiResponse;
import com.mudosa.musinsa.exception.ErrorCode;
import com.mudosa.musinsa.order.application.OrderService;
import com.mudosa.musinsa.order.application.dto.OrderCreateRequest;
import com.mudosa.musinsa.order.application.dto.OrderCreateResponse;
import com.mudosa.musinsa.order.application.dto.OrderDetailResponse;
import com.mudosa.musinsa.order.application.dto.PendingOrderResponse;
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
import org.springframework.web.bind.annotation.*;

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
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<OrderCreateResponse>> createOrder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody OrderCreateRequest request)
    {
        Long userId = userDetails.getUserId();

        log.info("[Order] 주문 생성 요청, userId: {}", userId);

        OrderCreateResponse response = orderService.createPendingOrder(request, userId);

        if (response.hasInsufficientStock()) {
            log.warn("[Order] 재고 부족으로 주문 생성 실패, userId: {}, 부족한 상품 수: {}", 
                    userId, response.getInsufficientStockItems().size());
            
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.failure(
                            ErrorCode.ORDER_INSUFFICIENT_STOCK.getCode(),
                            ErrorCode.ORDER_INSUFFICIENT_STOCK.getMessage(),
                            response
                    ));
        }

        log.info("[Order] 주문 생성 완료, orderId: {}, orderNo: {}", 
                response.getOrderId(), response.getOrderNo());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(
            summary = "주문 조회",
            description = "생성한 주문을 조회합니다."
    )
    @GetMapping("/pending")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PendingOrderResponse>> fetchOrder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(value="orderNo") String orderNo
    ){
        Long userId = userDetails.getUserId();
        
        log.info("[Order] 주문 조회 요청, userId: {}, orderNo: {}", userId, orderNo);

        PendingOrderResponse response = orderService.fetchPendingOrder(orderNo);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(
            summary = "주문 상세 조회",
            description = "주문 상세 정보를 조회합니다 (PENDING, COMPLETED 모두 가능)"
    )
    @GetMapping("/{orderNo}")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> fetchOrderDetail(
            @PathVariable String orderNo
    ){
        log.info("[Order] 주문 상세 조회 요청, orderNo: {}", orderNo);

        OrderDetailResponse response = orderService.fetchOrderDetail(orderNo);

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
