package com.mudosa.musinsa.payment.application.dto;

import com.mudosa.musinsa.order.application.dto.InsufficientStockItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentConfirmResponse {
    private String paymentKey;
    private String status;
    private Long amount;
    private String pgProvider;
    private String method;
    private LocalDateTime approvedAt;
    
    // 재고 부족 정보
    private List<InsufficientStockItem> insufficientStockItems;


    public static PaymentConfirmResponse insufficientStock(
            List<InsufficientStockItem> items) {
        return PaymentConfirmResponse.builder()
                .status("INSUFFICIENT_STOCK")
                .insufficientStockItems(items)
                .build();
    }
    
    public boolean hasInsufficientStock() {
        return insufficientStockItems != null && !insufficientStockItems.isEmpty();
    }
}
