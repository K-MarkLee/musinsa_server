package com.mudosa.musinsa.order.application.dto;

import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderCreateResponse {
    private Long orderId;
    private String orderNo;

    @Builder
    private OrderCreateResponse(Long orderId, String orderNo) {
        this.orderId = orderId;
        this.orderNo = orderNo;
    }

    public static OrderCreateResponse of(Long orderId, String orderNo) {
        return OrderCreateResponse
                .builder()
                .orderId(orderId)
                .orderNo(orderNo)
                .build();
    }
}
