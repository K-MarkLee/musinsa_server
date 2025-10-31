package com.mudosa.musinsa.order.domain.repository;

import com.mudosa.musinsa.order.domain.model.Orders;
import com.mudosa.musinsa.product.domain.model.ProductOption;
import org.apache.ibatis.annotations.Param;
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
    Optional<Orders> findByOrderNoWithOrderProducts(@Param("orderNo") String orderNo);

    @Query("""
        SELECT DISTINCT o 
        FROM Orders o 
        JOIN FETCH o.user u
        JOIN FETCH o.orderProducts op
        JOIN FETCH op.productOption po
        JOIN FETCH po.product p
        WHERE o.orderNo = :orderNo
    """)
    Optional<Orders> findByOrderNoWithUserAndProducts(@Param("orderNo") String orderNo);

    @Query("""
        SELECT DISTINCT po
        FROM ProductOption po
        LEFT JOIN FETCH po.productOptionValues pov
        LEFT JOIN FETCH pov.optionValue ov
        LEFT JOIN FETCH ov.optionName on
        WHERE po.productOptionId IN :productOptionIds
    """)
    List<ProductOption> findProductOptionsWithValues(@Param("productOptionIds") List<Long> productOptionIds);
}
