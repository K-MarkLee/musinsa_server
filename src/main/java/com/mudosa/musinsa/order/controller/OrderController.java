package com.mudosa.musinsa.order.controller;

import com.mudosa.musinsa.common.dto.ApiResponse;
import com.mudosa.musinsa.exception.ErrorCode;
import com.mudosa.musinsa.order.application.OrderService;
import com.mudosa.musinsa.order.application.dto.OrderCreateRequest;
import com.mudosa.musinsa.order.application.dto.OrderCreateResponse;
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

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/api/v1/orders")
@Tag(name="Order", description = "주문 API")
public class OrderController {
    private final OrderService orderService;

    @Operation(
            summary = "주문 생성",
            description = "주문을 생성합니다"
    )
    @PostMapping
    public ResponseEntity<ApiResponse<OrderCreateResponse>> createOrder(
            @Valid @RequestBody OrderCreateRequest request)
    {
        log.info("[Order] 주문 생성 요청, userId: {}",
                request.getUserId());

        OrderCreateResponse response = orderService.createPendingOrder(request);

        // 재고 부족인 경우 BAD_REQUEST로 응답
        if (response.hasInsufficientStock()) {
            log.warn("[Order] 재고 부족으로 주문 생성 실패, userId: {}, 부족한 상품 수: {}", 
                    request.getUserId(), response.getInsufficientStockItems().size());
            
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
}
