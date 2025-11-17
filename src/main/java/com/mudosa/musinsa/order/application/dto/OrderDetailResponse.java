package com.mudosa.musinsa.order.application.dto;

import com.mudosa.musinsa.payment.domain.model.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailResponse {
    //주문 정보
    private String orderNo;
    private String orderStatus;
    private BigDecimal totalProductAmount;
    private BigDecimal discountAmount;
    private LocalDateTime orderedAt;

    //사용자 정보
    private String userName;
    private String userAddress;
    private String userContactNumber;

    //상품 정보
    private List<OrderDetailItem> orderProducts;

    //결제 정보
    private BigDecimal paymentFinalAmount;
    private String paymentMethod;
    private String pgProvider;
    private LocalDateTime approvedAt;
    private PaymentStatus paymentStatus;
}
