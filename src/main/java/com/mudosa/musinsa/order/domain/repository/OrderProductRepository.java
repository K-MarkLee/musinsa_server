package com.mudosa.musinsa.order.domain.repository;

import com.mudosa.musinsa.order.domain.model.OrderProduct;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderProductRepository extends JpaRepository<OrderProduct, Long> {
}
