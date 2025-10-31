package com.mudosa.musinsa.order.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class InsufficientStockItem {
    private Long productOptionId;
    private Integer requestedQuantity;
    private Integer availableQuantity;
}
