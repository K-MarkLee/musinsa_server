package com.mudosa.musinsa.product.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class CategoryTest {
    
    @Test
    @DisplayName("부모 카테고리와 자식 카테고리를 생성했을 때 child.buildPath()는 '상의>티셔츠'를 반환해야 한다")
    void buildPath_createsCorrectPath() {
        // given
        Category root = Category.builder()
                .categoryName("상의")
                .build();
        
        Category child = Category.builder()
                .categoryName("티셔츠")
                .parent(root)
                .build();
        
        // when
        String path = child.buildPath();
        
    // then
    org.assertj.core.api.Assertions.assertThat(path).isEqualTo("상의>티셔츠");
    }
}
