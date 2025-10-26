package com.mudosa.musinsa.product.domain.model;

import com.mudosa.musinsa.common.domain.model.BaseEntity;
import com.mudosa.musinsa.product.domain.vo.StockQuantity;
import jakarta.persistence.*;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "inventory", 
       indexes = {
           @Index(name = "idx_inventory_prodopt_avail", columnList = "product_option_id, is_available")
       },
       uniqueConstraints = {
           @UniqueConstraint(name = "uniq_inventory_prodopt", columnNames = {"product_option_id"})
       })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Inventory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inventory_id")
    private Long inventoryId;

    @Column(name = "product_option_id", nullable = false, unique = true)  // FK (ProductOption)
    private Long productOptionId;

    @Embedded
    private StockQuantity stockQuantity;

    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable;

    // 연관관계 - ProductOption 연결
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_option_id", insertable = false, updatable = false)
    private ProductOption productOption;

    // 생성 메서드
    public static Inventory create(Long productOptionId, StockQuantity stockQuantity) {
        return new Inventory(productOptionId, stockQuantity, true);
    }

    // 생성 메서드 (isAvailable 제어 가능) 예: 예약 상품 및 이벤트 상품
    public static Inventory create(Long productOptionId, StockQuantity stockQuantity, Boolean isAvailable) {
        return new Inventory(productOptionId, stockQuantity, isAvailable);
    }

    // 비즈니스 메서드
    public void updateStockQuantity(StockQuantity stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    // 상품 옵션 변경시 재고 연동 메서드
    public void updateProductOption(Long productOptionId) {
        this.productOptionId = productOptionId;
    }

    public void activate() {
        this.isAvailable = true;
    }

    public void deactivate() {
        this.isAvailable = false;
    }

    // 재고 관리 비즈니스 메서드
    public void increaseStock(StockQuantity quantity) {
        this.stockQuantity = this.stockQuantity.add(quantity);
    }

    public void decreaseStock(StockQuantity quantity) {
        this.stockQuantity = this.stockQuantity.subtract(quantity);
    }

    public boolean hasStock(StockQuantity quantity) {
        return this.stockQuantity.isGreaterThanOrEqual(quantity);
    }

    public boolean isOutOfStock() {
        return this.stockQuantity.isZero();
    }

    public boolean isLowStock(StockQuantity threshold) {
        return this.stockQuantity.isLessThan(threshold);
    }

    // 연관관계 메서드
    public void setProductOption(ProductOption productOption) {
        this.productOption = productOption;
    }

    // JPA를 위한 protected 생성자
    protected Inventory(Long productOptionId, StockQuantity stockQuantity, Boolean isAvailable) {
        this.productOptionId = productOptionId;
        this.stockQuantity = stockQuantity;
        this.isAvailable = isAvailable;
    }
}