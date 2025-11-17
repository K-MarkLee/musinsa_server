package com.mudosa.musinsa.order.domain.repository;

import com.mudosa.musinsa.order.application.dto.PendingOrderItem;

import java.util.List;

public interface OrderRepositoryCustom {
    List<PendingOrderItem> findOrderItems(String orderNo);
}
