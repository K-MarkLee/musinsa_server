package com.mudosa.musinsa.product.domain.model;

import com.mudosa.musinsa.exception.BusinessException;
import com.mudosa.musinsa.exception.ErrorCode;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;

import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
// import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Category 도메인 모델의 테스트")
public class CategoryTest {
    
    @Test
    @DisplayName("카테고리에 올바른 이름을 넣으면 부모 카테고리가 정상적으로 생성된다.")
    void createCategory() {
        // given
        String categoryName = "상의";
        String imageUrl = null;

        // when
        Category category = Category.create(categoryName, null, imageUrl);

        // then
        assertThat(category.getCategoryName()).isEqualTo("상의");
    }

    @Test
    @DisplayName("카테고리에 올바른 이름과 부모 카테고리를 넣으면 자식 카테고리가 정상적으로 생성된다.")
    void createChildCategory() {
        // given
        Category parentCategory = Category.create("상의", null, null);
        String categoryName = "티셔츠";
        String imageUrl = null;

        // when
        Category category = Category.create(categoryName, parentCategory, imageUrl);

        // then
        assertThat(category.getCategoryName()).isEqualTo("티셔츠");
        assertThat(category.getParent().getCategoryName()).isEqualTo("상의");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    @DisplayName("카테고리에 올바르지 못한(null, 빈값) 이름을 넣으면 BusinessException이 발생한다.")
    void createCategoryWithInvalidName(String invalidName) {
        // when // then
        assertThatThrownBy(() -> Category.create(invalidName, null, null))
            .isInstanceOf(BusinessException.class)
            .extracting(e -> ((BusinessException) e).getErrorCode())
            .isEqualTo(ErrorCode.CATEGORY_NAME_REQUIRED);
    }

    @Test
    @DisplayName("부모가 존재하는 카테고리의 이름을 사용하여 카테고리 경로를 생성한다.")
    void buildPath() {
        // given
        Category parentCategory = Category.create("상의", null, null);
        Category childCategory = Category.create("티셔츠", parentCategory, null);

        // when
        String path = childCategory.buildPath();

        // then
        assertThat(path).isEqualTo("상의>티셔츠");
    }

    @Test
    @DisplayName("부모 카테고리의 이름을 사용하여 카테고리 경로를 생성한다.")
    void buildParentPath() {
        // given
        Category category = Category.create("상의", null, null);

        // when
        String path = category.buildPath();

        // then
        assertThat(path).isEqualTo("상의");
    }

    /**
     * 순환 참조 테스트는 ReflectionTestUtils를 사용하여 강제로 순환 참조를 하지 않는 이상 테스트가 불가합니다.
    **/

    // @Test
    // @DisplayName("카테고리 계층에 순환 참조가 있을 경우 예외가 발생한다.")
    // void buildPathWithCircularCategory() {
    //     // given
    //     Category categoryA = Category.create("A", null, null);
    //     Category categoryB = Category.create("B", categoryA, null);
        
    //     // 강제로 순환 참조 설정
    //     ReflectionTestUtils.setField(categoryA, "parent", categoryB);

    //     // when // then
    //     assertThatThrownBy(() -> categoryA.buildPath())
    //         .isInstanceOf(IllegalStateException.class)
    //         .hasMessageContaining("카테고리 계층에 순환 참조가 감지되었습니다.");
        
    // }

}
