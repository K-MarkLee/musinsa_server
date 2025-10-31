package com.mudosa.musinsa.product.domain.repository;

import com.mudosa.musinsa.product.domain.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findAllByUserId(Long userId);

    @Modifying
    @Query("DELETE FROM CartItem c WHERE c.user.id = :userId AND c.productOption.productOptionId IN :productOptionIds")
    int deleteByUserIdAndProductOptionIdIn(
            @Param("userId") Long userId,
            @Param("productOptionIds") List<Long> productOptionIds
    );;
}
