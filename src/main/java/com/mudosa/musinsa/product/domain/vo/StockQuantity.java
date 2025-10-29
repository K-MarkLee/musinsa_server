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

    public void decrease(int quantity){
        // 음수 재고를 방지하기 위한 방어 로직
        if (quantity <= 0) {
            throw new IllegalArgumentException("차감 수량은 0 이하일 수 없습니다.");
        }
        if (this.value - quantity < 0) {
            throw new IllegalArgumentException("재고 수량은 음수가 될 수 없습니다.");
        }
        this.value -= quantity;
    }

    public void increase(int quantity){
        // 잘못된 요청으로 상태가 깨지지 않도록 검증
        if (quantity <= 0) {
            throw new IllegalArgumentException("증가 수량은 0 이하일 수 없습니다.");
        }
        this.value += quantity;
    }
}