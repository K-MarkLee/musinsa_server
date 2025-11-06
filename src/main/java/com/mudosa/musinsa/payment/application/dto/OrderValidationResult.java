package com.mudosa.musinsa.payment.application.dto;

import com.mudosa.musinsa.order.application.dto.InsufficientStockItem;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderValidationResult {
    private Long orderId;
    private Long userId;
    private BigDecimal finalAmount;
    private BigDecimal discountAmount;
    private boolean isValid;

    private List<InsufficientStockItem> insufficientStockItems;

    public static OrderValidationResult success(
            Long orderId,
            Long userId,
            BigDecimal finalAmount,
            BigDecimal discountAmount) {
        return new OrderValidationResult(
                orderId, userId, finalAmount, discountAmount,
                true, null
        );
    }

    public boolean hasInsufficientStock() {
        return !isValid;
    }

    public static OrderValidationResult insufficientStock(
            Long orderId,
            List<InsufficientStockItem> items) {
        return new OrderValidationResult(
                orderId, null, null, null,
                false, items
        );
    }
}
