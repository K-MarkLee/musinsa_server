package com.mudosa.musinsa.product.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("OptionValue는 필수 필드 검증과 유효성/연관 검사 메서드를 제공해야 한다")
class OptionValueTest {

    @Test
    @DisplayName("optionName이 null이면 빌더 호출 시 IllegalArgumentException이 발생해야 한다")
    void constructor_nullName_throws() {
        // given
        String name = null;

        // when / then
        assertThatThrownBy(() -> OptionValue.builder().optionName(name).optionValue("M").build())
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("optionValue가 null 또는 공백이면 빌더 호출 시 IllegalArgumentException이 발생해야 한다")
    void constructor_invalidValue_throws() {
        // given
        String name = "size";

        // when / then
        assertThatThrownBy(() -> OptionValue.builder().optionName(name).optionValue(null).build())
            .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> OptionValue.builder().optionName(name).optionValue(" ").build())
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("isValid는 값이 비어있지 않음을 확인하고 belongsTo는 옵션명 일치를 판단해야 한다")
    void isValid_belongsTo_happyPath() {
        // given
        OptionValue ov = OptionValue.builder().optionName("size").optionValue("M").build();

        // when / then
        assertThat(ov.isValid()).isTrue();
        assertThat(ov.belongsTo("size")).isTrue();
        assertThat(ov.belongsTo("color")).isFalse();
    }
}
