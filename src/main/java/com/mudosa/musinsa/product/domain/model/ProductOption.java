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

    //ProductOption이 Inventory를 바라봐야함
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_id", nullable = false, unique = true)
    private Inventory inventory;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "product_price", nullable = false, precision = 10, scale = 2))
    private Money productPrice;

    @OneToMany(mappedBy = "productOption", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProductOptionValue> productOptionValue = new ArrayList<>();

    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable;

    @Builder
    public ProductOption(Product product, Money productPrice, Inventory inventory) {
        // 엔티티 기본 무결성 검증
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
    }

    // 패키지 private: 상품 참조 설정 (Product 애그리거트에서만 사용)
    void setProduct(Product product) {
        this.product = product;
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