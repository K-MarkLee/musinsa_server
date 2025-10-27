package com.mudosa.musinsa.product.domain.vo;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CategoryPath {
    private String value;
    
    private static final int MAX_LENGTH = 255;
    private static final String SEPARATOR = ">";
    
    public CategoryPath(String value) {
        validate(value);
        this.value = value;
    }
    
    private void validate(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("카테고리 경로는 비어있을 수 없습니다.");
        }
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("카테고리 경로는 " + MAX_LENGTH + "자를 초과할 수 없습니다.");
        }
        // 계층 구조 검증 (예: "상의>티셔츠>슬랙스")
        if (!value.contains(SEPARATOR)) {
            throw new IllegalArgumentException("카테고리 경로는 '" + SEPARATOR + "'로 계층 구조여야 합니다.");
        }
    }
    
    // 최상위 카테고리명 추출
    public String getTopCategoryName() {
        String[] categories = value.split(SEPARATOR);
        return categories.length > 0 ? categories[0].trim() : "";
    }
    
    // 전체 경로에서 마지막 카테고리명 추출
    public String getLastCategoryName() {
        String[] categories = value.split(SEPARATOR);
        return categories.length > 0 ? categories[categories.length - 1].trim() : "";
    }
    
    // 계층 깊이 (무조건 2계층)
    public int getDepth() {
        return 2;
    }
    
    @Override
    public String toString() {
        return value;
    }
}