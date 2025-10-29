package com.mudosa.musinsa.product.domain.model;

import com.mudosa.musinsa.common.domain.model.BaseEntity;
import com.mudosa.musinsa.user.domain.model.User;
import com.mudosa.musinsa.common.vo.Money;
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


    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "unit_price", nullable = false, precision = 10, scale = 2))
    private Money unitPrice;

    @Builder
    public CartItem(User user, ProductOption productOption, Integer quantity, Money unitPrice) {
        // 엔티티 기본 무결성 검증
        if (user == null) {
            throw new IllegalArgumentException("사용자는 필수입니다.");
        }
        if (productOption == null) {
            throw new IllegalArgumentException("상품 옵션은 필수입니다.");
        }
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("수량은 1개 이상이어야 합니다.");
        }
        if (unitPrice == null || unitPrice.isLessThanOrEqual(Money.ZERO)) {
            throw new IllegalArgumentException("단가는 0원보다 커야 합니다.");
        }

        this.user = user;
        this.productOption = productOption;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

}