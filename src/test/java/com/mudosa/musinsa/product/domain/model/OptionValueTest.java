package com.mudosa.musinsa.product.domain.model;

import com.mudosa.musinsa.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import com.mudosa.musinsa.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("OptionValue 도메인 모델의 테스트")
class OptionValueTest {

    @Test
    @DisplayName("옵션 값에 올바른 정보를 넣으면 옵션 값이 정상적으로 생성된다.")
    void createOptionValue() {
        // given
        String optionName = "색상";
        String optionValue = "레드";

        // when
        OptionValue optionValueEntity = OptionValue.create(optionName, optionValue);

        // then
        assertThat(optionValueEntity.getOptionName()).isEqualTo("색상");
        assertThat(optionValueEntity.getOptionValue()).isEqualTo("레드");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    @DisplayName("옵션 값에 올바르지 못한(null, 빈값) 옵션명을 넣으면 BusinessException이 발생한다.")
    void createOptionValueWithInvalidOptionName(String invalidOptionName) {
        // given
        String optionValue = "레드";
        
        // when // then
        assertThatThrownBy(() -> OptionValue.create(invalidOptionName, optionValue))
            .isInstanceOf(BusinessException.class)
            .extracting(e -> ((BusinessException) e).getErrorCode())
            .isEqualTo(ErrorCode.OPTION_NAME_REQUIRED);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    @DisplayName("옵션 값에 올바르지 못한(null, 빈값) 옵션명을 넣으면 BusinessException이 발생한다.")
    void createOptionValueWithInvalidOptionValue(String invalidOptionValue) {
        // given
        String optionName = "색상";
        
        // when // then
        assertThatThrownBy(() -> OptionValue.create(optionName, invalidOptionValue))
            .isInstanceOf(BusinessException.class)
            .extracting(e -> ((BusinessException) e).getErrorCode())
            .isEqualTo(ErrorCode.OPTION_VALUE_REQUIRED);
    }

}
