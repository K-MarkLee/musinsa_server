package com.mudosa.musinsa.product.domain.model;

import com.mudosa.musinsa.exception.BusinessException;
import com.mudosa.musinsa.exception.ErrorCode;
import com.mudosa.musinsa.product.domain.vo.StockQuantity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("StockQuantity 도메인 vo 모델 테스트")
class StockQuantityTest {

    @Test
    @DisplayName("유효한 재고 수량으로 생성하면 값이 세팅된다.")
    void createStockQuantity() {
        // when
        StockQuantity stockQuantity = new StockQuantity(10);

        // then
        assertThat(stockQuantity.getValue()).isEqualTo(10);
    }

    @ParameterizedTest
    @ValueSource(ints = { -1, -10})
    @DisplayName("재고 수량이 음수이면 BusinessException이 발생한다.")
    void createStockQuantityWithNegativeValue(int invalid) {
        // when // then
        assertThatThrownBy(() -> new StockQuantity(invalid))
            .isInstanceOf(BusinessException.class)
            .extracting(e -> ((BusinessException) e).getErrorCode())
            .isEqualTo(ErrorCode.STOCK_QUANTITY_CANNOT_BE_NEGATIVE);
    }

    @Test
    @DisplayName("재고 수량이 null이면 BusinessException이 발생한다.")
    void createStockQuantityWithNull() {
        // when // then
        assertThatThrownBy(() -> new StockQuantity(null))
            .isInstanceOf(BusinessException.class)
            .extracting(e -> ((BusinessException) e).getErrorCode())
            .isEqualTo(ErrorCode.STOCK_QUANTITY_CANNOT_BE_NULL);
    }

    @Test
    @DisplayName("재고를 감소시키면 값이 줄어든다.")
    void decrease() {
        // given
        StockQuantity stockQuantity = new StockQuantity(10);

        // when
        stockQuantity.decrease(3);

        // then
        assertThat(stockQuantity.getValue()).isEqualTo(7);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    @DisplayName("재고 감소 수량이 0 이하이면 BusinessException이 발생한다.")
    void decreaseWithInvalidValue(int invalid) {
        // given
        StockQuantity stockQuantity = new StockQuantity(5);

        // when // then
        assertThatThrownBy(() -> stockQuantity.decrease(invalid))
            .isInstanceOf(BusinessException.class)
            .extracting(e -> ((BusinessException) e).getErrorCode())
            .isEqualTo(ErrorCode.STOCK_QUANTITY_CANNOT_BE_LESS_THAN_ONE);
    }

    @Test
    @DisplayName("재고보다 많은 수량을 감소시키면 BusinessException이 발생한다.")
    void decreaseMoreThanAvailable() {
        // given
        StockQuantity stockQuantity = new StockQuantity(5);

        // when // then
        assertThatThrownBy(() -> stockQuantity.decrease(10))
            .isInstanceOf(BusinessException.class)
            .extracting(e -> ((BusinessException) e).getErrorCode())
            .isEqualTo(ErrorCode.STOCK_QUANTITY_OUT_OF_STOCK);
    }

    @Test
    @DisplayName("재고를 증가시키면 값이 늘어난다.")
    void increase() {
        // given
        StockQuantity stockQuantity = new StockQuantity(5);

        // when
        stockQuantity.increase(3);

        // then
        assertThat(stockQuantity.getValue()).isEqualTo(8);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -2})
    @DisplayName("재고 증가 수량이 0 이하이면 BusinessException이 발생한다.")
    void increaseWithInvalidValue(int invalid) {
        // given
        StockQuantity stockQuantity = new StockQuantity(5);

        // when // then
        assertThatThrownBy(() -> stockQuantity.increase(invalid))
            .isInstanceOf(BusinessException.class)
            .extracting(e -> ((BusinessException) e).getErrorCode())
            .isEqualTo(ErrorCode.STOCK_QUANTITY_INVALID);
    }
}
