package com.mudosa.musinsa.order.domain.repository;

import com.mudosa.musinsa.order.domain.model.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
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
    Optional<Orders> findByOrderNoWithOrderProducts( String orderNo);

    @Query("""
        SELECT DISTINCT o 
        FROM Orders o 
        JOIN FETCH o.user u
        JOIN FETCH o.orderProducts op
        JOIN FETCH op.productOption po
        JOIN FETCH po.product p
        WHERE o.orderNo = :orderNo
    """)
    Optional<Orders> findByOrderNoWithUserAndProducts(String orderNo);

    Optional<Orders> findByOrderNo(String orderNo);

    @Query("""
        SELECT DISTINCT o
        FROM Orders o
        JOIN FETCH o.user u
        JOIN FETCH o.orderProducts op
        JOIN FETCH op.productOption po
        JOIN FETCH po.product p
        WHERE o.orderNo = :orderNo
    """)
    Optional<Orders> findOrderWithDetails(String orderNo);

    @Query("""
        SELECT DISTINCT o
        FROM Orders o
        JOIN FETCH o.orderProducts op
        JOIN FETCH op.productOption po
        JOIN FETCH po.product p
        JOIN FETCH p.brand b
        WHERE o.id = :orderId
    """)
    Optional<Orders> findByIdWithProductsAndBrand(Long orderId);
}
