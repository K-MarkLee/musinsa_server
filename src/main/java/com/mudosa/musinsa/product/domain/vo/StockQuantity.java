package com.mudosa.musinsa.product.domain.vo;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 재고 수량을 감싸 유효성 검증과 증감 로직을 제공하는 값 타입이다.
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StockQuantity {
    private Integer value;
    
    // 생성 시 null 또는 음수를 방지하고 값을 고정한다.
    public StockQuantity(Integer value) {
        validate(value);
        this.value = value;
    }
    
    // 숫자 범위를 점검해 잘못된 재고 입력을 막는다.
    private void validate(Integer value) {
        if (value == null) {
            throw new IllegalArgumentException("재고 수량은 null일 수 없습니다.");
        }
        if (value < 0) {
            throw new IllegalArgumentException("재고 수량은 음수일 수 없습니다.");
        }
    }
    
    // 재고 수량을 문자열로 반환한다.
    @Override
    public String toString() {
        return value.toString();
    }

    public void decrease(int quantity){
        if (quantity <= 0) {
            throw new IllegalArgumentException("차감 수량은 0 이하일 수 없습니다.");
        }
        this.value -= quantity;
    }

    // 요청 수량만큼 재고를 증가시키며 0 이하 입력을 거부한다.
    public void increase(int quantity){
        // 잘못된 요청으로 상태가 깨지지 않도록 검증
        if (quantity <= 0) {
            throw new IllegalArgumentException("증가 수량은 0 이하일 수 없습니다.");
        }
        this.value += quantity;
    }
}