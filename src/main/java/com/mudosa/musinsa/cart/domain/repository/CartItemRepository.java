package com.mudosa.musinsa.cart.domain.repository;

import com.mudosa.musinsa.cart.domain.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * CartItem Repository
 */
@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    
    /**
     * 사용자 ID로 장바구니 아이템 조회
     */
    List<CartItem> findByUserId(Long userId);
    
    /**
     * 사용자 ID와 상품 옵션 ID 리스트로 장바구니 아이템 삭제
     */
    @Modifying
    @Query("DELETE FROM CartItem c WHERE c.userId = :userId AND c.productOptionId IN :productOptionIds")
    void deleteByUserIdAndProductOptionIdIn(
        @Param("userId") Long userId, 
        @Param("productOptionIds") List<Long> productOptionIds
    );
}
