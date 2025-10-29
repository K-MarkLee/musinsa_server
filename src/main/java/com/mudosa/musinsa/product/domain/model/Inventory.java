package com.mudosa.musinsa.product.domain.model;

import com.mudosa.musinsa.common.domain.model.BaseEntity;
import com.mudosa.musinsa.exception.BusinessException;
import com.mudosa.musinsa.exception.ErrorCode;
import com.mudosa.musinsa.product.domain.vo.StockQuantity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "inventory")
public class Inventory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inventory_id")
    private Long inventoryId;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "stock_quantity"))
    private StockQuantity stockQuantity;

    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable;

    @Builder
    public Inventory(StockQuantity stockQuantity, Boolean isAvailable) {
        // 엔티티 기본 무결성 검증
        if (stockQuantity == null) {
            throw new IllegalArgumentException("재고 수량은 필수입니다.");
        }

        this.stockQuantity = stockQuantity;
        // 재고가 0이면 무조건 unavailable, 재고가 있으면 지정된 값 또는 true
        this.isAvailable = stockQuantity.getValue() > 0 ?
                Boolean.TRUE.equals(isAvailable) : false;
    }

    public void decrease(int quantity) {
        if (this.stockQuantity.getValue() < quantity) {
            throw new BusinessException(
                    ErrorCode.INSUFFICIENT_STOCK,
                    String.format("재고가 부족합니다. 요청: %d, 현재: %d", quantity, this.stockQuantity.getValue()));
        }
        this.stockQuantity.decrease(quantity);
        if (this.stockQuantity.getValue() == 0) {
            this.isAvailable = false;
        }
    }

    public void increase(int quantity) {
        this.stockQuantity.increase(quantity);
        if (this.stockQuantity.getValue() > 0) {
            this.isAvailable = true;
        }
    }


}