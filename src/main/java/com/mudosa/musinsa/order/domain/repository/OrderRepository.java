package com.mudosa.musinsa.order.domain.repository;

import com.mudosa.musinsa.order.domain.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Order Repository
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    List<Order> findByUserId(Long userId);
    
    List<Order> findByBrandId(Long brandId);
}
