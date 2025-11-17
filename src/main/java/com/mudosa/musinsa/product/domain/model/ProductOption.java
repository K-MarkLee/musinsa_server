package com.mudosa.musinsa.product.domain.model;

import com.mudosa.musinsa.common.domain.model.BaseEntity;
import com.mudosa.musinsa.common.vo.Money;
import com.mudosa.musinsa.exception.BusinessException;
import com.mudosa.musinsa.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;
import java.util.Objects;

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

    // 옵션 생성 시 필수 값 검증 후 연관 엔티티를 초기화한다.
    public static ProductOption create(Product product, Money productPrice, Inventory inventory) {
        return new ProductOption(product, productPrice, inventory);
    }

    ProductOption(Product product, Money productPrice, Inventory inventory) {
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

    // 상품과의 연관을 제거해 고아 제거가 정상 동작하도록 한다.
    void detachFromProduct() {
        this.product = null;
        this.productOptionValues.forEach(ProductOptionValue::refreshIdentifiers);
    }

    // 주문 과정에서 옵션 재고를 차감한다.
    public void decreaseStock(int quantity) {
        if (quantity <= 0) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "차감 수량은 1 이상이어야 합니다.");
        }

        try {
            this.inventory.decrease(quantity);
        } catch (IllegalStateException ex) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_STOCK, ex.getMessage());
        }
    }

    // 주문 취소 등으로 옵션 재고를 복구한다.
    public void restoreStock(int quantity) {
        if (quantity <= 0) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "복구 수량은 1 이상이어야 합니다.");
        }

        this.inventory.increase(quantity);
    }

    // 옵션이 판매 가능한 상태인지 확인한다.
    public void validateAvailable() {
        if (this.inventory.getStockQuantity() == null
            || this.inventory.getStockQuantity().getValue() <= 0) {
            throw new BusinessException(ErrorCode.PRODUCT_OPTION_NOT_AVAILABLE);
        }
    }

    List<Long> normalizedOptionValueIds() {
        return this.productOptionValues.stream()
            .map(ProductOptionValue::getOptionValue)
            .filter(Objects::nonNull)
            .map(OptionValue::getOptionValueId)
            .filter(Objects::nonNull)
            .sorted(Comparator.naturalOrder())
            .toList();
    }

    public Integer getStockQuantity() {
        return this.getInventory().getStockQuantity().getValue();
    }

    public boolean hasEnoughStock(Integer quantity) {
        return this.getStockQuantity() >= quantity;
    }
}