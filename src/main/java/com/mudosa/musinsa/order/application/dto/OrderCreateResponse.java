package com.mudosa.musinsa.order.application.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderCreateResponse {
    private Long orderId;
    private String orderNo;
    private List<InsufficientStockItem> insufficientStockItems;

    public static OrderCreateResponse success(Long orderId, String orderNo) {
        return new OrderCreateResponse(orderId, orderNo, null);
    }

    public static OrderCreateResponse insufficientStock(List<InsufficientStockItem> insufficientStockItems) {
        return new OrderCreateResponse(null, null, insufficientStockItems);
    }

    public boolean hasInsufficientStock() {
        return insufficientStockItems != null && !insufficientStockItems.isEmpty();
    }

}
