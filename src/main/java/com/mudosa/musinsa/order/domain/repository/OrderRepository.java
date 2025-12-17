package com.mudosa.musinsa.order.domain.repository;

import com.mudosa.musinsa.order.domain.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, OrderRepositoryCustom {

    @Query("""
        SELECT DISTINCT o 
        FROM Order o 
        JOIN FETCH o.orderProducts op
        JOIN FETCH op.productOption po
        JOIN FETCH po.inventory inv
        WHERE o.orderNo = :orderNo
    """)
    Optional<Order> findByOrderNoWithOrderProducts(String orderNo);

    Optional<Order> findByOrderNo(String orderNo);

    @Query("""
        SELECT DISTINCT o
        FROM Order o
        JOIN FETCH o.orderProducts op
        JOIN FETCH op.productOption po
        JOIN FETCH po.product p
        JOIN FETCH p.brand b
        WHERE o.id = :orderId
    """)
    Optional<Order> findByIdWithProductsAndBrand(Long orderId);

    List<Order> findAllByUserId(Long userId);
}
