package com.mudosa.musinsa.product.domain.model;

import com.mudosa.musinsa.common.vo.Money;
import com.mudosa.musinsa.exception.BusinessException;
import com.mudosa.musinsa.exception.ErrorCode;
import com.mudosa.musinsa.product.domain.vo.StockQuantity;
import com.mudosa.musinsa.user.domain.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("CartItem 도메인 모델의 테스트")
class CartItemTest {

    @Test
    @DisplayName("필수 값을 넣으면 장바구니 아이템이 생성된다.")
    void createCartItem() {
        // given
        User user = User.builder().build();
        ProductOption productOption = createProductOption();

        // when
        CartItem cartItem = CartItem.create(user, productOption, 2);

        // then
        assertThat(cartItem.getUser()).isEqualTo(user);
        assertThat(cartItem.getProductOption()).isEqualTo(productOption);
        assertThat(cartItem.getQuantity()).isEqualTo(2);
    }

    @Test
    @DisplayName("상품 옵션이 null이면 BusinessException이 발생한다.")
    void createCartItemWithNullProductOption() {
        // given
        User user = User.builder().build();

        // when // then
        assertThatThrownBy(() -> CartItem.create(user, null, 1))
            .isInstanceOf(BusinessException.class)
            .extracting(e -> ((BusinessException) e).getErrorCode())
            .isEqualTo(ErrorCode.CART_ITEM_PRODUCT_OPTION_REQUIRED);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    @DisplayName("수량이 0 이하이면 BusinessException이 발생한다.")
    void createCartItemWithInvalidQuantity(int invalidQuantity) {
        // given
        User user = User.builder().build();
        ProductOption productOption = createProductOption();

        // when // then
        assertThatThrownBy(() -> CartItem.create(user, productOption, invalidQuantity))
            .isInstanceOf(BusinessException.class)
            .extracting(e -> ((BusinessException) e).getErrorCode())
            .isEqualTo(ErrorCode.CART_ITEM_QUANTITY_INVALID);
    }

    @Test
    @DisplayName("수량이 null이면 BusinessException이 발생한다.")
    void createCartItemWithNullQuantity() {
        // given
        User user = User.builder().build();
        ProductOption productOption = createProductOption();

        // when // then
        assertThatThrownBy(() -> CartItem.create(user, productOption, null))
            .isInstanceOf(BusinessException.class)
            .extracting(e -> ((BusinessException) e).getErrorCode())
            .isEqualTo(ErrorCode.CART_ITEM_QUANTITY_REQUIRED);
    }

    @Test
    @DisplayName("수량을 1 이상으로 변경하면 값이 변경된다.")
    void changeQuantity() {
        // given
        User user = User.builder().build();
        ProductOption productOption = createProductOption();
        CartItem cartItem = CartItem.create(user, productOption, 1);

        // when
        cartItem.changeQuantity(3);

        // then
        assertThat(cartItem.getQuantity()).isEqualTo(3);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -5})
    @DisplayName("수량을 0 이하로 변경하면 BusinessException이 발생한다.")
    void changeQuantityWithInvalidValue(int invalidQuantity) {
        // given
        User user = User.builder().build();
        ProductOption productOption = createProductOption();
        CartItem cartItem = CartItem.create(user, productOption, 1);

        // when // then
        assertThatThrownBy(() -> cartItem.changeQuantity(invalidQuantity))
            .isInstanceOf(BusinessException.class)
            .extracting(e -> ((BusinessException) e).getErrorCode())
            .isEqualTo(ErrorCode.CART_ITEM_QUANTITY_INVALID);
    }

        private ProductOption createProductOption() {
        return ProductOption.builder()
            .productPrice(new Money(1000L))
            .inventory(Inventory.create(new StockQuantity(10)))
            .build();
    }
}
