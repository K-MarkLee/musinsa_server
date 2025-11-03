package com.mudosa.musinsa.payment.application.dto;

import com.mudosa.musinsa.order.application.dto.InsufficientStockItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;


@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentConfirmResponse {
    private String orderNo;
    private List<InsufficientStockItem> insufficientStockItems;

    public static PaymentConfirmResponse success(String orderNo) {
        return PaymentConfirmResponse.builder()
                .orderNo(orderNo)
                .build();
    }

    public static PaymentConfirmResponse insufficientStock(
            List<InsufficientStockItem> items) {
        return PaymentConfirmResponse.builder()
                .insufficientStockItems(items)
                .build();
    }

    public boolean hasInsufficientStock() {
        return insufficientStockItems != null && !insufficientStockItems.isEmpty();
    }
}
