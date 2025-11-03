package com.mudosa.musinsa.order.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailItem {
    private Long productOptionId;
    private String productOptionName;
    private String brandName;
    private BigDecimal amount;
    private Integer quantity;
    private String size;
    private String color;
    private String imageUrl;
}
