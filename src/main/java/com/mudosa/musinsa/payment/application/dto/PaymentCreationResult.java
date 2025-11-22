package com.mudosa.musinsa.payment.application.dto;

import com.mudosa.musinsa.order.application.dto.InsufficientStockItem;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class PaymentCreationResult {
    private Long paymentId;
    private Long orderId;
    private Long userId;

    @Builder
    public PaymentCreationResult(Long paymentId, Long orderId, Long userId) {
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.userId = userId;
    }
}
