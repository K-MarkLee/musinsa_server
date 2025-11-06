package com.mudosa.musinsa.payment.application.dto;

import com.mudosa.musinsa.order.application.dto.InsufficientStockItem;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)

public class PaymentCreationResult {
    private Long paymentId;
    private Long orderId;
    private Long userId;
    private boolean isSuccess;
    private List<InsufficientStockItem> insufficientStockItems;

    public static PaymentCreationResult success(
            Long paymentId, Long orderId, Long userId) {
        return new PaymentCreationResult(
                paymentId, orderId, userId, true, null
        );
    }

    public static PaymentCreationResult insufficientStock(
            Long orderId,
            List<InsufficientStockItem> items) {
        return new PaymentCreationResult(
                null, orderId, null, false, items
        );
    }

    public boolean hasInsufficientStock() {
        return !isSuccess;
    }
}
