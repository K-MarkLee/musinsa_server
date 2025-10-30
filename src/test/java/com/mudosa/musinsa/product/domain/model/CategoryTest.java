package com.mudosa.musinsa.product.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CategoryTest {

    @Test
    @DisplayName("카테고리 경로 생성 시 순환 참조를 감지한다")
    void buildPath_detectsCycle() throws Exception {
        Category root = Category.builder()
            .categoryName("상의")
            .parent(null)
            .imageUrl(null)
            .build();

        Category child = Category.builder()
            .categoryName("티셔츠")
            .parent(root)
            .imageUrl(null)
            .build();

        setParent(root, child);

        assertThatThrownBy(root::buildPath)
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("순환 참조");
    }

    private void setParent(Category target, Category parent) throws Exception {
        Field parentField = Category.class.getDeclaredField("parent");
        parentField.setAccessible(true);
        parentField.set(target, parent);
    }
}
