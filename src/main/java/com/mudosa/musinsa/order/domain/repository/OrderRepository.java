package com.mudosa.musinsa.order.domain.repository;

import com.mudosa.musinsa.order.domain.model.Orders;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Orders, Long> {

    @Query("""
        SELECT DISTINCT o 
        FROM Orders o 
        JOIN FETCH o.orderProducts op
        JOIN FETCH op.productOption po
        JOIN FETCH po.inventory inv
        WHERE o.orderNo = :orderNo
    """)
    Optional<Orders> findByOrderNoWithOrderProducts(@Param("orderNo") String orderNo);
}
