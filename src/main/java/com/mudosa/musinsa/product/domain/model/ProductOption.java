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
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "product_option")
public class ProductOption extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_option_id")
    private Long productOptionId;

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

    // 외부 노출 생성 메서드 + 필수 값 검증
    public static ProductOption create(Product product, Money productPrice, Inventory inventory) {
        if (productPrice == null || productPrice.isLessThanOrEqual(Money.ZERO)) {
            throw new BusinessException(ErrorCode.PRODUCT_PRICE_REQUIRED);
        }
        if (inventory == null) {
            throw new BusinessException(ErrorCode.INVENTORY_REQUIRED);
        }

        return new ProductOption(product, productPrice, inventory);
    }

    @Builder
    private ProductOption(Product product, Money productPrice, Inventory inventory) {
        this.product = product;
        this.productPrice = productPrice;
        this.inventory = inventory;
    }

    // 옵션 값 매핑을 추가하고 현재 옵션과 연결한다.
    public void addOptionValue(ProductOptionValue optionValue) {
        if (optionValue == null) {
            throw new BusinessException(ErrorCode.OPTION_VALUE_REQUIRED);
        }
        optionValue.attachTo(this);
        this.productOptionValues.add(optionValue);
    }

    // 상품 애그리거트에서만 호출해 양방향 연관을 설정한다.
    void setProduct(Product product) {
        this.product = product;
        this.productOptionValues.forEach(value -> value.attachTo(this));
    }

    // 주문 과정에서 옵션 재고를 차감한다.
    public void decreaseStock(int quantity) {
        if (quantity <= 0) {
            throw new BusinessException(ErrorCode.INVALID_INVENTORY_UPDATE_VALUE, "차감 수량은 1 이상이어야 합니다.");
        }
        try {
            this.inventory.decrease(quantity);
        } catch (IllegalStateException ex) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_STOCK);
        }
    }

    // 주문 취소 등으로 옵션 재고를 복구한다.
    public void restoreStock(int quantity) {
        if (quantity <= 0) {
            throw new BusinessException(ErrorCode.RECOVER_VALUE_INVALID, "복구 수량은 1 이상이어야 합니다.");
        }
        this.inventory.increase(quantity);
    }

    // 옵션이 판매 가능한 상태인지 확인한다.
    public void validateAvailable() {
        if (this.inventory.getStockQuantity() == null
            || this.inventory.getStockQuantity().getValue() <= 0) {
            throw new BusinessException(ErrorCode.PRODUCT_OPTION_OUT_OF_STOCK);
        }
    }

    // 현재 재고 수량을 반환한다.
    public Integer getStockQuantity() {
        return this.getInventory().getStockQuantity().getValue();
    }

    // 해당 재고가 요청 수량을 충족하는지 확인한다.
    public boolean hasEnoughStock(Integer quantity) {
        return this.getStockQuantity() >= quantity;
    }
}
