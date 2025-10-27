package com.mudosa.musinsa.product.domain.vo;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StockQuantity {
    private Integer value;
    
    public StockQuantity(Integer value) {
        validate(value);
        this.value = value;
    }
    
    private void validate(Integer value) {
        if (value == null) {
            throw new IllegalArgumentException("재고 수량은 null일 수 없습니다.");
        }
        if (value < 0) {
            throw new IllegalArgumentException("재고 수량은 음수일 수 없습니다.");
        }
    }
    
    @Override
    public String toString() {
        return value.toString();
    }
}