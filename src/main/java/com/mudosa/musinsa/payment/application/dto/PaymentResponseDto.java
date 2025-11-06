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
public class PaymentResponseDto {
    private String orderNo;
    private String paymentKey;
    private String status;
    private LocalDateTime approvedAt;
    private String method;
    private Long totalAmount;
    private String pgProvider;
}
