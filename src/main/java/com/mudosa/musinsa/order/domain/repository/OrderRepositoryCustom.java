package com.mudosa.musinsa.order.domain.repository;

import com.mudosa.musinsa.order.application.dto.OrderFlatDto;
import com.mudosa.musinsa.order.application.dto.OrderItem;
import com.mudosa.musinsa.order.application.dto.response.OrderInfo;

import java.util.List;

public interface OrderRepositoryCustom {
    List<OrderItem> findOrderItems(String orderNo);
    List<OrderFlatDto> findFlatOrderListWithDetails(Long userId);
}
