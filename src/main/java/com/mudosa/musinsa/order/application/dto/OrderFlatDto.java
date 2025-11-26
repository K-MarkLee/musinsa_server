package com.mudosa.musinsa.order.application.dto;

import com.mudosa.musinsa.order.domain.model.OrderStatus;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class OrderFlatDto {
    private String orderNo;
    private OrderStatus orderStatus;
    private LocalDateTime registeredAt;
    private BigDecimal totalPrice;

    private Long productOptionId;
    private String brandName;
    private String productName;
    private BigDecimal itemAmount;
    private Integer quantity;
    private String imageUrl;
    private String size;
    private String color;

    @QueryProjection
    public OrderFlatDto(
            String orderNo, OrderStatus orderStatus, LocalDateTime registeredAt, BigDecimal totalPrice,
            Long productOptionId, String brandName, String productName, BigDecimal itemAmount,
            Integer quantity, String imageUrl, String size, String color) {
        this.orderNo = orderNo;
        this.orderStatus = orderStatus;
        this.registeredAt = registeredAt;
        this.totalPrice = totalPrice;
        this.productOptionId = productOptionId;
        this.brandName = brandName;
        this.productName = productName;
        this.itemAmount = itemAmount;
        this.quantity = quantity;
        this.imageUrl = imageUrl;
        this.size = size;
        this.color = color;
    }
}
