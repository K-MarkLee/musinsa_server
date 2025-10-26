package com.mudosa.musinsa.product.domain.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 재고 수량 Value Object
 * DDL: INT NOT NULL
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class StockQuantity {

    @Column(name = "stock_quantity", nullable = false)
    private Integer value;

    private StockQuantity(Integer value) {
        if (value == null) {
            throw new IllegalArgumentException("재고 수량은 필수입니다.");
        }
        if (value < 0) {
            throw new IllegalArgumentException("재고 수량은 0개 이상이어야 합니다.");
        }
        if (value > 999999) {
            throw new IllegalArgumentException("재고 수량은 999,999개를 초과할 수 없습니다.");
        }
        this.value = value;
    }

    
    public static StockQuantity of(Integer value) {
        return new StockQuantity(value);
    }

    public static StockQuantity of(int value) {
        return new StockQuantity(value);
    }

    public Integer getValue() {
        return value;
    }

    public boolean isZero() {
        return this.value == 0;
    }

    public boolean isPositive() {
        return this.value > 0;
    }

    public StockQuantity add(StockQuantity other) {
        return new StockQuantity(this.value + other.value);
    }

    public StockQuantity subtract(StockQuantity other) {
        if (this.value < other.value) {
            throw new IllegalArgumentException("재고 수량이 부족합니다.");
        }
        return new StockQuantity(this.value - other.value);
    }

    public boolean isGreaterThan(StockQuantity other) {
        return this.value > other.value;
    }

    public boolean isLessThan(StockQuantity other) {
        return this.value < other.value;
    }

    public boolean isGreaterThanOrEqual(StockQuantity other) {
        return this.value >= other.value;
    }

    public boolean isLessThanOrEqual(StockQuantity other) {
        return this.value <= other.value;
    }

    @Override
    public String toString() {
        return value + "개";
    }
}