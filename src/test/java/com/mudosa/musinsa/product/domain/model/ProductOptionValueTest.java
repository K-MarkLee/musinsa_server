package com.mudosa.musinsa.product.domain.model;

import com.mudosa.musinsa.exception.BusinessException;
import com.mudosa.musinsa.exception.ErrorCode;

import com.mudosa.musinsa.common.vo.Money;
import com.mudosa.musinsa.product.domain.vo.StockQuantity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ProductOptionValue 도메인 모델의 테스트")
class ProductOptionValueTest {

    @Test
    @DisplayName("상품 옵션 값에 올바른 정보를 넣으면 상품 옵션 값이 정상적으로 생성된다.")
    void createProductOptionValue() {
        // given
        ProductOption productOption = ProductOption.builder()
            .productPrice(new Money(10000L))
            .inventory(
                Inventory.builder()
                    .stockQuantity(new StockQuantity(10))
                    .build()
            )
            .build();

        OptionValue optionValue = OptionValue.create("색상", "레드");

        // when
        ProductOptionValue productOptionValue = ProductOptionValue.create(productOption, optionValue);

        // then
        assertThat(productOptionValue).isNotNull();
        assertThat(productOptionValue.getProductOption()).isEqualTo(productOption);
        assertThat(productOptionValue.getOptionValue()).isEqualTo(optionValue);
    }

    @ParameterizedTest
    @NullSource
    @DisplayName("상품 옵션에 null 값을 넣으면 BusinessException이 발생한다.")
    void createProductOptionValueWithNullProductOption(ProductOption invalidProductOption) {
        // given
        OptionValue optionValue = OptionValue.create("색상", "레드");

        // when // then
        assertThatThrownBy(() -> ProductOptionValue.create(invalidProductOption, optionValue))
            .isInstanceOf(BusinessException.class)
            .extracting(e -> ((BusinessException) e).getErrorCode())
            .isEqualTo(ErrorCode.PRODUCT_OPTION_REQUIRED);
    }

    @ParameterizedTest
    @NullSource
    @DisplayName("옵션 값에 null을 넣으면 BusinessException이 발생한다.")
    void createProductOptionValueWithNullOptionValue(OptionValue invalidOptionValue) {
        // given
        ProductOption productOption = ProductOption.builder()
            .productPrice(new Money(10000L))
            .inventory(Inventory.create(new StockQuantity(10)))
            .build();

        // when // then
        assertThatThrownBy(() -> ProductOptionValue.create(productOption, invalidOptionValue))
            .isInstanceOf(BusinessException.class)
            .extracting(e -> ((BusinessException) e).getErrorCode())
            .isEqualTo(ErrorCode.OPTION_VALUE_REQUIRED);
    }

    @Test
    @DisplayName("상품 옵션이 나중에 연결되면 연관관계가 설정된다.")
    void attachToProductOption() {
        // given
        ProductOption productOption = ProductOption.builder()
            .productPrice(new Money(10000L))
            .inventory(Inventory.create(new StockQuantity(10)))
            .build();

        OptionValue optionValue = OptionValue.create("색상", "레드");
        ProductOptionValue productOptionValue = ProductOptionValue.builder()
            .optionValue(optionValue)
            .build();

        // when
        productOptionValue.attachTo(productOption);

        // then
        assertThat(productOptionValue.getProductOption()).isEqualTo(productOption);
    }

    @Test
    @DisplayName("복합 키가 올바르게 갱신된다.")
    void refreshIdentifiers() {
        // given
        ProductOption productOption = ProductOption.builder()
            .productPrice(new Money(10000L))
            .inventory(Inventory.create(new StockQuantity(10)))
            .build();

        OptionValue optionValue = OptionValue.create("색상", "레드");
        
        ProductOptionValue productOptionValue = ProductOptionValue.builder()
            .optionValue(optionValue)
            .build();

        // when
        productOptionValue.attachTo(productOption);

        // then
        assertThat(productOptionValue.getId().getProductOptionId()).isEqualTo(productOption.getProductOptionId());
        assertThat(productOptionValue.getId().getOptionValueId()).isEqualTo(optionValue.getOptionValueId());
    }

}
