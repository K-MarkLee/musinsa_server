package com.mudosa.musinsa.product.domain.model;

import com.mudosa.musinsa.product.domain.vo.StockQuantity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.mudosa.musinsa.exception.BusinessException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Inventory 도메인 모델의 테스트")
class InventoryTest {
    @Test
    @DisplayName("재고에 올바른 수량을 넣고 생성하면 정상적으로 생성된다.")
    void createInventory() {
        // given
        StockQuantity stockQuantity = new StockQuantity(100);

        // when
        Inventory inventory = Inventory.create(stockQuantity);

        // then
        assertThat(inventory).isNotNull();
        assertThat(inventory.getStockQuantity().getValue()).isEqualTo(100);
    }

    @ParameterizedTest
    @NullSource
    @DisplayName("재고 생성 시 재고 수량이 null 값이면 BusinessException이 발생한다.")
    void createInventoryWithNullValues(StockQuantity stockQuantity) {
        // when & then
        assertThatThrownBy(() -> Inventory.create(stockQuantity))
            .isInstanceOf(BusinessException.class)
            .extracting(e -> ((BusinessException) e).getErrorCode())
            .isEqualTo(com.mudosa.musinsa.exception.ErrorCode.INVENTORY_STOCK_QUANTITY_REQUIRED);
    }

    @Test
    @DisplayName("현재의 재고보다 낮은 수량의 재고를 감소시키면 정상적으로 재고가 감소된다.")
    void decreaseInventoryWithSufficientStock() {
        // given
        Inventory inventory = Inventory.create(new StockQuantity(50));
        int decreaseQuantity = 20;

        // when
        inventory.decrease(decreaseQuantity);

        // then
        assertThat(inventory.getStockQuantity().getValue()).isEqualTo(30);  
    }

    @Test
    @DisplayName("현재 재고보다 높은 수량의 재고를 감소시키면 BusinessException이 발생한다.")
    void decreaseInventoryWithInsufficientStock() {
        // given
        Inventory inventory = Inventory.create(new StockQuantity(30));
        int decreaseQuantity = 50;

        // when & then
        assertThatThrownBy(() -> inventory.decrease(decreaseQuantity))
            .isInstanceOf(BusinessException.class)
            .extracting(e -> ((BusinessException) e).getErrorCode())
            .isEqualTo(com.mudosa.musinsa.exception.ErrorCode.INVENTORY_INSUFFICIENT_STOCK);
    }

    @Test
    @DisplayName("요청한 수량이 올바르면 정상적으로 재고가 증가된다.")
    void increaseInventoryWithValidQuantity() {
        // given
        Inventory inventory = Inventory.create(new StockQuantity(40));
        int increaseQuantity = 30;

        // when
        inventory.increase(increaseQuantity);

        // then
        assertThat(inventory.getStockQuantity().getValue()).isEqualTo(70);
    }

    @Test
    @DisplayName("요청한 수량이 0 이하이면 BusinessException이 발생한다.")
    void increaseInventoryWithInvalidQuantity() {
        // given
        Inventory inventory = Inventory.create(new StockQuantity(40));
        int increaseQuantity = 0;

        // when & then
        assertThatThrownBy(() -> inventory.increase(increaseQuantity))
            .isInstanceOf(BusinessException.class)
            .extracting(e -> ((BusinessException) e).getErrorCode())
            .isEqualTo(com.mudosa.musinsa.exception.ErrorCode.INVALID_INVENTORY_UPDATE_VALUE);
    }

    @Test
    @DisplayName("요청한 수량이 현재 재고보다 적으면 충분한 재고가 있다고 반환한다.")
    void isSufficientStock() {
        // given
        Inventory inventory = Inventory.create(new StockQuantity(100));
        int requestedQuantity = 60;

        // when
        boolean result = inventory.isSufficientStock(requestedQuantity);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("요청한 수량이 현재 재고보다 많으면 충분한 재고가 없다고 반환한다.")
    void issufficientStockWithInsufficientQuantity() {
        // given
        Inventory inventory = Inventory.create(new StockQuantity(80));
        int requestedQuantity = 120;

        // when
        boolean result = inventory.isSufficientStock(requestedQuantity);

        // then
        assertThat(result).isFalse();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -10})
    @DisplayName("요청 수량이 0 이하이면 BusinessException이 발생한다.")
    void isSufficientStockWithInvalidRequestedQuantity(int requestedQuantity) {
        // given
        Inventory inventory = Inventory.create(new StockQuantity(50));

        // when & then
        assertThatThrownBy(() -> inventory.isSufficientStock(requestedQuantity))
            .isInstanceOf(BusinessException.class)
            .extracting(e -> ((BusinessException) e).getErrorCode())
            .isEqualTo(com.mudosa.musinsa.exception.ErrorCode.INVENTORY_STOCK_QUANTITY_REQUIRED);
    }

    /** 
     * 재고 수량이 null로 하려면 강제로 필드에 접근해야 해서 주석 처리함
    **/
    // @Test
    // @DisplayName("재고 수량이 null이면 BusinessException이 발생한다.")
    // void isSufficientStockWithNullInventory() {
    //     // given
    //     Inventory inventory = Inventory.create(new StockQuantity(50));
        
    //     // 강제로 stockQuantity를 null로 설정
    //     org.springframework.test.util.ReflectionTestUtils.setField(inventory, "stockQuantity", null);

    //     // when & then
    //     assertThatThrownBy(() -> inventory.isSufficientStock(10))
    //         .isInstanceOf(BusinessException.class)
    //         .extracting(e -> ((BusinessException) e).getErrorCode())
    //         .isEqualTo(com.mudosa.musinsa.exception.ErrorCode.INVENTORY_STOCK_QUANTITY_REQUIRED);
    // }

}
