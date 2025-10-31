package com.mudosa.musinsa.product.domain.model;

import com.mudosa.musinsa.common.domain.model.BaseEntity;
import com.mudosa.musinsa.common.vo.Money;
import com.mudosa.musinsa.exception.BusinessException;
import com.mudosa.musinsa.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

// 상품 옵션과 가격, 재고를 관리하는 엔티티이다.
@Entity
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "product_option")
public class ProductOption extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_option_id")
    private Long productOptionId;

    //TODO: ProductOption에서 Product를 바라봐야 하는 이유
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "inventory_id", nullable = false, unique = true)
    private Inventory inventory;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "product_price", nullable = false, precision = 10, scale = 2))
    private Money productPrice;
    
    // 옵션 값 매핑을 애그리거트 내부에서 함께 관리
    @OneToMany(mappedBy = "productOption", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ProductOptionValue> productOptionValues = new ArrayList<>();
    
    // 옵션 생성 시 필수 값 검증 후 연관 엔티티를 초기화한다.
    @Builder
    public ProductOption(Product product, Money productPrice, Inventory inventory,
                         List<ProductOptionValue> productOptionValues) {
        // 필수 파라미터를 확인해 무결성을 보장한다.
        if (product == null) {
            throw new IllegalArgumentException("상품은 옵션에 필수입니다.");
        }
        if (productPrice == null || productPrice.isLessThanOrEqual(Money.ZERO)) {
            throw new IllegalArgumentException("상품 가격은 0원보다 커야 합니다.");
        }
        if (inventory == null) {
            throw new IllegalArgumentException("재고 정보는 옵션에 필수입니다.");
        }

        this.product = product;
        this.productPrice = productPrice;
        this.inventory = inventory;
        if (productOptionValues != null) {
            productOptionValues.forEach(this::addOptionValue);
        }
    }
    
    // 상품 애그리거트에서만 호출해 양방향 연관을 설정한다.
    void setProduct(Product product) {
        this.product = product;
        this.productOptionValues.forEach(value -> value.attachTo(this));
    }

    // 옵션 값 매핑을 추가하고 현재 옵션과 연결한다.
    public void addOptionValue(ProductOptionValue optionValue) {
        if (optionValue == null) {
            return;
        }
        optionValue.attachTo(this);
        this.productOptionValues.add(optionValue);
    }

    /* 재고 차감 */
    public void decreaseStock(Integer quantity) {
        if (this.inventory == null) {
            throw new BusinessException(ErrorCode.INVENTORY_NOT_FOUND);
        }

        this.inventory.decrease(quantity);
    }

    /* 재고 복구 */
    public void restoreStock(Integer quantity) {
        if (this.inventory == null) {
            throw new BusinessException(ErrorCode.INVENTORY_NOT_FOUND);
        }

        this.inventory.increase(quantity);
    }

    public void validateAvailable() {
        if (!this.isAvailable) {
            throw new BusinessException(ErrorCode.PRODUCT_OPTION_NOT_AVAILABLE);
        }

        if (this.inventory == null || !this.inventory.getIsAvailable()) {
            throw new BusinessException(ErrorCode.INVENTORY_NOT_AVAILABLE);
        }
    }
}