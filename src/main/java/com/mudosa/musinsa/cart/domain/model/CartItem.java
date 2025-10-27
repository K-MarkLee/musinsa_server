package com.mudosa.musinsa.cart.domain.model;

import com.mudosa.musinsa.common.domain.model.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "cart_item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CartItem extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_item_id")
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId; // User 애그리거트 참조
    
    @Column(name = "product_option_id", nullable = false)
    private Long productOptionId; // ProductOption 애그리거트 참조
    
    @Column(name = "quantity", nullable = false)
    private Integer quantity;
    
    /**
     * 장바구니 아이템 생성
     */
    public static CartItem create(Long userId, Long productOptionId, Integer quantity) {
        CartItem cartItem = new CartItem();
        cartItem.userId = userId;
        cartItem.productOptionId = productOptionId;
        cartItem.quantity = quantity;
        return cartItem;
    }
    
    /**
     * 수량 변경
     */
    public void updateQuantity(Integer quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("수량은 1개 이상이어야 합니다.");
        }
        this.quantity = quantity;
    }
}
