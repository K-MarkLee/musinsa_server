package com.mudosa.musinsa.product.domain.model;

import com.mudosa.musinsa.product.domain.vo.StockQuantity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("StockQuantity 값 객체는 null/음수 생성 방지와 증감 로직을 제공해야 한다")
class StockQuantityTest {

    @Test
    @DisplayName("null을 전달하면 생성 시 IllegalArgumentException이 발생해야 한다")
    void constructor_null_throws() {
        // given
        Integer given = null;

        // when / then
        assertThatThrownBy(() -> new StockQuantity(given)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("음수 값을 전달하면 생성 시 IllegalArgumentException이 발생해야 한다")
    void constructor_negative_throws() {
        // given
        int given = -1;

        // when / then
        assertThatThrownBy(() -> new StockQuantity(given)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("재고를 감소하면 값이 줄고, 증가하면 값이 늘어나야 한다")
    void decrease_increase_happyPath() {
        // given
        StockQuantity qty = new StockQuantity(5);

        // when
        qty.decrease(2);

        // then
        assertThat(qty.getValue()).isEqualTo(3);

        // when
        qty.increase(4);

        // then
        assertThat(qty.getValue()).isEqualTo(7);
    }

    @Test
    @DisplayName("0 또는 음수로 감소 요청하면 IllegalArgumentException이 발생해야 한다")
    void decrease_invalidAmount_throws() {
        // given
        StockQuantity qty = new StockQuantity(5);

        // when / then
        assertThatThrownBy(() -> qty.decrease(0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> qty.decrease(-1)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("재고보다 큰 수량으로 감소 요청하면 IllegalArgumentException이 발생해야 한다")
    void decrease_moreThanAvailable_throws() {
        // given
        StockQuantity qty = new StockQuantity(2);

        // when / then
        assertThatThrownBy(() -> qty.decrease(3)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("0 또는 음수로 증가 요청하면 IllegalArgumentException이 발생해야 한다")
    void increase_invalidAmount_throws() {
        // given
        StockQuantity qty = new StockQuantity(2);

        // when / then
        assertThatThrownBy(() -> qty.increase(0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> qty.increase(-5)).isInstanceOf(IllegalArgumentException.class);
    }
}
