package com.mudosa.musinsa.product.domain.vo;

import com.mudosa.musinsa.exception.BusinessException;
import com.mudosa.musinsa.exception.ErrorCode;

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
            throw new BusinessException(ErrorCode.STOCK_QUANTITY_CANNOT_BE_NULL);
        }
        if (value < 0) {
            throw new BusinessException(ErrorCode.STOCK_QUANTITY_CANNOT_BE_NEGATIVE);
        }
    }
    
    // 재고 수량을 문자열로 반환한다.
    @Override
    public String toString() {
        return value.toString();
    }

    // 요청 수량만큼 재고를 감소시키며 음수를 허용하지 않는다.
    public void decrease(int quantity){
        // 음수 재고를 방지하기 위한 방어 로직
        if (quantity <= 0) {
            throw new BusinessException(ErrorCode.STOCK_QUANTITY_CANNOT_BE_LESS_THAN_ONE);
        }
        if (this.value - quantity < 0) {
            throw new BusinessException(ErrorCode.STOCK_QUANTITY_OUT_OF_STOCK);
        }
        this.value -= quantity;
    }

    // 요청 수량만큼 재고를 증가시키며 0 이하 입력을 거부한다.
    public void increase(int quantity){
        // 잘못된 요청으로 상태가 깨지지 않도록 검증
        if (quantity <= 0) {
            throw new BusinessException(ErrorCode.STOCK_QUANTITY_INVALID,"증가 수량은 0 이하일 수 없습니다.");
        }
        this.value += quantity;
    }
}