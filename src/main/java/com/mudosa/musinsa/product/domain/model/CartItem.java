package com.mudosa.musinsa.product.domain.model;

import com.mudosa.musinsa.common.domain.model.BaseEntity;
import com.mudosa.musinsa.common.vo.Money;
import com.mudosa.musinsa.user.domain.model.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "cart_item")
public class CartItem extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_item_id")
    private Long cartItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_option_id", nullable = false)
    private ProductOption productOption;
    
    @Column(name = "quantity", nullable = false)
    private Integer quantity;
    
    // 장바구니 항목을 생성하며 필수 값을 검증한다.
    @Builder
    public CartItem(User user, ProductOption productOption, Integer quantity) {
        if (user == null) {
            throw new IllegalArgumentException("사용자는 필수입니다.");
        }
        if (productOption == null) {
            throw new IllegalArgumentException("상품 옵션은 필수입니다.");
        }
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("수량은 1개 이상이어야 합니다.");
        }
        
        this.user = user;
        this.productOption = productOption;
        this.quantity = quantity;
    }

    /**
     * Lombok 빌더에 남아 있는 unitPrice 세터를 안전하게 무시하기 위한 훅이다.
     * 테스트 코드에서는 옵션 가격을 전달하지만, 엔티티는 스냅샷을 저장하지 않고 옵션에서 직접 조회한다.
     */
    public static class CartItemBuilder {
        public CartItemBuilder unitPrice(Money ignored) {
            return this;
        }
    }

    // 수량을 변경할 때 1 이상인지 검증한다.
    public void changeQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("수량은 1개 이상이어야 합니다.");
        }
        this.quantity = quantity;
    }

}