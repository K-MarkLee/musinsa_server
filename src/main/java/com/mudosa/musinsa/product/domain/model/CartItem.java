package com.mudosa.musinsa.product.domain.model;

import com.mudosa.musinsa.common.domain.model.BaseEntity;
import com.mudosa.musinsa.exception.BusinessException;
import com.mudosa.musinsa.exception.ErrorCode;
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

    // 외부 노출 생성 메서드 + 필수 값 검증
    public static CartItem create(User user, ProductOption productOption, Integer quantity) {
        if (user == null) {
            throw new BusinessException(ErrorCode.CART_ITEM_USER_REQUIRED);
        }
        if (productOption == null) {
            throw new BusinessException(ErrorCode.CART_ITEM_PRODUCT_OPTION_REQUIRED);
        }
        if (quantity == null || quantity <= 0) {
            throw new BusinessException(ErrorCode.CART_ITEM_QUANTITY_INVALID);
        }
        return new CartItem(user, productOption, quantity);
    }
    
    @Builder
    private CartItem(User user, ProductOption productOption, Integer quantity) {
        this.user = user;
        this.productOption = productOption;
        this.quantity = quantity;
    }

    // 수량을 변경할 때 1 이상인지 검증한다.
    public void changeQuantity(int quantity) {
        if (quantity <= 0) {
            throw new BusinessException(ErrorCode.CART_ITEM_QUANTITY_INVALID);
        }
        this.quantity = quantity;
    }

}