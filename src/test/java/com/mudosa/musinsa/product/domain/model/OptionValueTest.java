package com.mudosa.musinsa.product.domain.model;

import com.mudosa.musinsa.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("OptionValue는 필수 필드 검증과 유효성/연관 검사 메서드를 제공해야 한다")
class OptionValueTest {

    @Test
    @DisplayName("optionName이 null이면 빌더 호출 시 BusinessException이 발생해야 한다")
    void constructor_nullName_throws() {
        // given
        String name = null;

        // when / then
        assertThatThrownBy(() -> OptionValue.create(name, name))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("optionValue가 null 또는 공백이면 빌더 호출 시 BusinessException이 발생해야 한다")
    void constructor_invalidValue_throws() {
        // given
        String name = "size";

        // when / then
        assertThatThrownBy(() -> OptionValue.create(name, null))
            .isInstanceOf(BusinessException.class);

        assertThatThrownBy(() -> OptionValue.create(name, " "))
            .isInstanceOf(BusinessException.class);
    }
}
