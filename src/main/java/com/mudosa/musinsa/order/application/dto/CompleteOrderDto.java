package com.mudosa.musinsa.order.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
public class CompleteOrderDto {
    private Long orderId;
    private Map<Long, Integer> quantityMap;

    @Builder
    private CompleteOrderDto(Long orderId, Map<Long, Integer> quantityMap) {
        this.orderId = orderId;
        this.quantityMap = quantityMap;
    }
}
