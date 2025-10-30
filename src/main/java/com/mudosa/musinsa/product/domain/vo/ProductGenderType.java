package com.mudosa.musinsa.product.domain.vo;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 상품의 성별 구분을 표현하는 값 타입이다.
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductGenderType {
    // DDL ENUM('MEN','WOMEN','ALL') 과 일치하도록 문자열로 저장
    @Enumerated(EnumType.STRING)
    private Type value;
    
    // 유효한 성별 타입만 허용하도록 검증한다.
    public ProductGenderType(Type value) {
        if (value == null) {
            throw new IllegalArgumentException("성별 타입은 null일 수 없습니다.");
        }
        this.value = value;
    }
    
    public enum Type {
        MEN, WOMEN, ALL
    }
    
    // ENUM 값을 문자열로 노출해 로깅 및 디버깅을 돕는다.
    @Override
    public String toString() {
        return value.name();
    }
}