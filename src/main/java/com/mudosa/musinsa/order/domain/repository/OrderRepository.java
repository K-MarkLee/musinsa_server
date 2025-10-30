package com.mudosa.musinsa.order.domain.repository;

import com.mudosa.musinsa.order.domain.model.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Orders, Long> {

    Optional<Orders> findByOrderNo(String orderNo);
}
