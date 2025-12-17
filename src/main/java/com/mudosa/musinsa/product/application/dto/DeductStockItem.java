package com.mudosa.musinsa.product.application.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class DeductStockItem {
    private Long productOptionId;
    private Integer quantity;

    @Builder
    private DeductStockItem(Long productOptionId, Integer quantity) {
        this.productOptionId = productOptionId;
        this.quantity = quantity;
    }
}
