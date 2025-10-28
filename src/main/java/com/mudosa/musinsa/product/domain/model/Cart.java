package com.mudosa.musinsa.product.domain.model;

import com.mudosa.musinsa.common.domain.model.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "cart")
public class Cart extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_id")
    private Long cartId;
    
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;
    
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CartItem> cartItems = new ArrayList<>();
    
    @Builder
    public Cart(Long userId) {
        this.userId = userId;
    }
    
    // 도메인 로직: 사용자 변경
    public void changeUser(Long userId) {
        if (userId != null) this.userId = userId;
    }
    
    // 도메인 로직: 특정 사용자의 장바구니 여부 확인
    public boolean belongsToUser(Long userId) {
        return this.userId != null && this.userId.equals(userId);
    }
    
    // 도메인 로직: 장바구니 상품 추가
    public void addCartItem(CartItem cartItem) {
        if (cartItem != null) {
            this.cartItems.add(cartItem);
        }
    }
    
    // 도메인 로직: 장바구니 상품 제거
    public void removeCartItem(CartItem cartItem) {
        if (cartItem != null) {
            this.cartItems.remove(cartItem);
        }
    }
    
    // 도메인 로직: 장바구니가 비었는지 확인
    public boolean isEmpty() {
        return this.cartItems.isEmpty();
    }
}