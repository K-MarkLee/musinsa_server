package com.mudosa.musinsa.order.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class PendingOrderItem{
    Long productOptionId;
    String brandName;
    String productOptionName;
    BigDecimal amount;
    Integer quantity;
    String imageUrl;
    String size;
    String color;
}
