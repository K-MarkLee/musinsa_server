package com.mudosa.musinsa.product.domain.vo;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CategoryName {
    private String value;
    
    private static final int MAX_LENGTH = 100;
    
    public CategoryName(String value) {
        validate(value);
        this.value = value;
    }
    
    private void validate(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("카테고리명은 비어있을 수 없습니다.");
        }
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("카테고리명은 " + MAX_LENGTH + "자를 초과할 수 없습니다.");
        }
    }
    
    @Override
    public String toString() {
        return value;
    }
}