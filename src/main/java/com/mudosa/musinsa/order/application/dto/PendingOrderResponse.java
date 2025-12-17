package com.mudosa.musinsa.order.application.dto;

import java.math.BigDecimal;
import java.util.List;

public record PendingOrderResponse(
    String orderNo,
    BigDecimal totalPrice,
    BigDecimal totalDiscount,
    List<OrderItem> orderProducts,
    String userName,
    String userAddress,
    String userContactNumber
){}
