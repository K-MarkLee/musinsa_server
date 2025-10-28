package com.mudosa.musinsa.product.domain.model;

import com.mudosa.musinsa.common.domain.model.BaseEntity;
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
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_option_id", nullable = false)
    private ProductOption productOption;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id", nullable = false)
    private Image image;
    
    @Column(name = "quantity", nullable = false)
    private Integer quantity;
    
    // 상품 가격 비정규화
    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private Money unitPrice;
    
    @Builder
    public CartItem(Cart cart, ProductOption productOption, Image image, Integer quantity, Money unitPrice) {
        this.cart = cart;
        this.productOption = productOption;
        this.image = image;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    
    // 도메인 로직: 정보 수정
    public void modify(ProductOption productOption, Image image, Integer quantity, Money unitPrice) {
        if (productOption != null) this.productOption = productOption;
        if (image != null) this.image = image;
        if (quantity != null) this.quantity = quantity;
        if (unitPrice != null) this.unitPrice = unitPrice;
    }
    
    // 도메인 로직: 수량 변경
    public void changeQuantity(Integer quantity) {
        if (quantity != null) this.quantity = quantity;
    }
    
    // 도메인 로직: 단가 변경
    public void changeUnitPrice(Money unitPrice) {
        if (unitPrice != null) this.unitPrice = unitPrice;
    }
    
    // 도메인 로직: 특정 장바구니 소속 여부 확인
    public boolean belongsToCart(Cart cart) {
        return this.cart != null && this.cart.equals(cart);
    }
    
    // 도메인 로직: 특정 상품 옵션 소속 여부 확인
    public boolean belongsToProductOption(ProductOption productOption) {
        return this.productOption != null && this.productOption.equals(productOption);
    }
    
    // 도메인 로직: 총 금액 계산
    public Money calculateTotalPrice() {
        return this.unitPrice.multiply(this.quantity);
    }
}