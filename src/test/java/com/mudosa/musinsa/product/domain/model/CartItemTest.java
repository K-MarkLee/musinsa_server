package com.mudosa.musinsa.product.domain.model;

import com.mudosa.musinsa.exception.BusinessException;
import com.mudosa.musinsa.user.domain.model.User;
import com.mudosa.musinsa.user.domain.model.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("CartItem 도메인 테스트")
class CartItemTest {

    private void setId(Object target, String fieldName, Long id) {
        try {
            Field f = target.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(target, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Nested
    @DisplayName("생성 검증")
    class Creation {

        @Test
        @DisplayName("유효한 사용자와 옵션, 수량이 주어지면 CartItem이 생성된다.")
        void create_success() {
            // Given: 유효한 사용자, 상품 옵션과 수량
            User user = User.create("test","pw","test@example.com", UserRole.USER, null, null, null);
            setId(user, "id", 1L);

            Product product = Product.builder()
                    .brand(null)
                    .productName("p")
                    .productInfo("i")
                    .productGenderType(ProductGenderType.ALL)
                    .brandName(null)
                    .categoryPath(null)
                    .isAvailable(true)
                    .build();
            ProductOption option = ProductOption.builder().product(product).productPrice(new com.mudosa.musinsa.common.vo.Money(1000)).inventory(Inventory.builder().stockQuantity(new com.mudosa.musinsa.product.domain.vo.StockQuantity(10)).build()).build();

            // When: CartItem 생성
            CartItem item = CartItem.builder().user(user).productOption(option).quantity(2).build();

            // Then: 생성되고 요청한 수량이 반영된다
            assertThat(item).isNotNull();
            assertThat(item.getQuantity()).isEqualTo(2);
        }

        @Test
        @DisplayName("사용자가 null이면 생성 시 BusinessException이 발생한다.")
        void create_nullUser_shouldThrow() {
            // Given: 사용자 값이 null이고 유효한 상품 옵션이 주어진다
            Product product = Product.builder().brand(null).productName("p").productInfo("i").productGenderType(ProductGenderType.ALL).brandName(null).categoryPath(null).isAvailable(true).build();
            ProductOption option = ProductOption.builder().product(product).productPrice(new com.mudosa.musinsa.common.vo.Money(1000)).inventory(Inventory.builder().stockQuantity(new com.mudosa.musinsa.product.domain.vo.StockQuantity(10)).build()).build();

            // When / Then: 사용자 null로 빌드하면 예외가 발생한다
            assertThatThrownBy(() -> CartItem.create(null, option, 2))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("수량이 0 이하이면 생성 시 BusinessException이 발생한다.")
        void create_invalidQuantity_shouldThrow() {
            // Given: 유효한 사용자와 상품 옵션이지만 수량이 유효하지 않다
            User user = User.create("test","pw","test@example.com", UserRole.USER, null, null, null);
            setId(user, "id", 1L);
            Product product = Product.builder().brand(null).productName("p").productInfo("i").productGenderType(ProductGenderType.ALL).brandName(null).categoryPath(null).isAvailable(true).build();
            ProductOption option = ProductOption.builder().product(product).productPrice(new com.mudosa.musinsa.common.vo.Money(1000)).inventory(Inventory.builder().stockQuantity(new com.mudosa.musinsa.product.domain.vo.StockQuantity(10)).build()).build();

            // When / Then: 0 이하 수량으로 빌드하면 예외가 발생한다
            assertThatThrownBy(() -> CartItem.create(user, option, 0))
                    .isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    @DisplayName("수량 변경")
    class ChangeQuantity {

        @Test
        @DisplayName("유효한 수량으로 변경하면 수량이 반영된다.")
        void changeQuantity_success() {
            // Given: 기존 CartItem이 수량 2로 존재한다
            User user = User.create("test","pw","test@example.com", UserRole.USER, null, null, null);
            setId(user, "id", 1L);
            Product product = Product.builder().brand(null).productName("p").productInfo("i").productGenderType(ProductGenderType.ALL).brandName(null).categoryPath(null).isAvailable(true).build();
            ProductOption option = ProductOption.builder().product(product).productPrice(new com.mudosa.musinsa.common.vo.Money(1000)).inventory(Inventory.builder().stockQuantity(new com.mudosa.musinsa.product.domain.vo.StockQuantity(10)).build()).build();
            CartItem item = CartItem.builder().user(user).productOption(option).quantity(2).build();

            // When: 수량을 5로 변경한다
            item.changeQuantity(5);

            // Then: 수량이 5로 업데이트된다
            assertThat(item.getQuantity()).isEqualTo(5);
        }

        @Test
        @DisplayName("0 이하로 변경하면 BusinessException이 발생한다.")
        void changeQuantity_invalid_shouldThrow() {
            // Given: 수량 2인 CartItem이 존재한다
            User user = User.create("test","pw","test@example.com", UserRole.USER, null, null, null);
            setId(user, "id", 1L);
            Product product = Product.builder().brand(null).productName("p").productInfo("i").productGenderType(ProductGenderType.ALL).brandName(null).categoryPath(null).isAvailable(true).build();
            ProductOption option = ProductOption.builder().product(product).productPrice(new com.mudosa.musinsa.common.vo.Money(1000)).inventory(Inventory.builder().stockQuantity(new com.mudosa.musinsa.product.domain.vo.StockQuantity(10)).build()).build();
            CartItem item = CartItem.builder().user(user).productOption(option).quantity(2).build();

            // When / Then: 0 이하로 변경하면 예외가 발생한다
            assertThatThrownBy(() -> item.changeQuantity(0)).isInstanceOf(BusinessException.class);
        }
    }
}
