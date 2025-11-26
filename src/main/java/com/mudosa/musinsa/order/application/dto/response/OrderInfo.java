package com.mudosa.musinsa.order.application.dto.response;

import com.mudosa.musinsa.order.application.dto.OrderItem;
import com.mudosa.musinsa.order.domain.model.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class OrderInfo {
    private String orderNo;
    private OrderStatus orderStatus;
    private LocalDateTime registeredAt;
    private BigDecimal totalPrice;
    private List<OrderItem> orderItems;
}
