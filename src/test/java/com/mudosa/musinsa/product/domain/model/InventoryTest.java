package com.mudosa.musinsa.product.domain.model;

import com.mudosa.musinsa.product.domain.vo.StockQuantity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Inventory 엔티티는 재고 무결성 검증과 증감 동작을 제공해야 한다")
class InventoryTest {

    @Test
    @DisplayName("null StockQuantity로 생성하면 IllegalArgumentException이 발생해야 한다")
    void constructor_nullStock_throws() {
        // given
        StockQuantity given = null;

        // when / then
        assertThatThrownBy(() -> Inventory.builder().stockQuantity(given).build()).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("요청 수량이 재고보다 크면 decrease 호출 시 IllegalStateException이 발생해야 한다")
    void decrease_insufficient_throws() {
        // given
        Inventory inv = Inventory.builder().stockQuantity(new StockQuantity(2)).build();

        // when / then
        assertThatThrownBy(() -> inv.decrease(3)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("decrease 호출 시 재고가 감소하고 increase 호출 시 재고가 증가해야 한다")
    void decrease_increase_happyPath() {
        // given
        Inventory inv = Inventory.builder().stockQuantity(new StockQuantity(5)).build();

        // when
        inv.decrease(2);

        // then
        assertThat(inv.getStockQuantity().getValue()).isEqualTo(3);

        // when
        inv.increase(4);

        // then
        assertThat(inv.getStockQuantity().getValue()).isEqualTo(7);
    }

    @Test
    @DisplayName("요청 수량이 0 이하이면 isSufficientStock 호출 시 IllegalArgumentException이 발생해야 한다")
    void isSufficientStock_invalidRequest_throws() {
        // given
        Inventory inv = Inventory.builder().stockQuantity(new StockQuantity(5)).build();

        // when / then
        assertThatThrownBy(() -> inv.isSufficientStock(0)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("요청 수량에 대해 재고가 충분하면 true, 부족하면 false를 반환해야 한다")
    void isSufficientStock_happyPath() {
        // given
        Inventory inv = Inventory.builder().stockQuantity(new StockQuantity(5)).build();

        // when / then
        assertThat(inv.isSufficientStock(3)).isTrue();
        assertThat(inv.isSufficientStock(5)).isTrue();
        assertThat(inv.isSufficientStock(6)).isFalse();
    }
}
