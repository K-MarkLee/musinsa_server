package com.mudosa.musinsa.product.domain.model;

import com.mudosa.musinsa.brand.domain.model.Brand;
import com.mudosa.musinsa.common.vo.Money;
import com.mudosa.musinsa.exception.BusinessException;
import com.mudosa.musinsa.exception.ErrorCode;
import com.mudosa.musinsa.product.domain.vo.StockQuantity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ProductOption 도메인은 생성시 유효성 검증과 재고/옵션값 로직을 보장해야 한다")
class ProductOptionDomainTest {

    @Nested
    @DisplayName("생성 시 유효성 검증: null 상품, 비정상 가격, null 재고는 예외를 발생시켜야 한다")
    class Creation {
        @Test
        @DisplayName("product가 null이면 create 호출 시 IllegalArgumentException이 발생해야 한다")
        void nullProduct_throws() {
            // given
            Inventory inv = Inventory.builder().stockQuantity(new StockQuantity(5)).build();

            // when / then
            assertThatThrownBy(() -> ProductOption.create(null, new Money(1000L), inv)).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("productPrice가 0 이하이면 create 호출 시 IllegalArgumentException이 발생해야 한다")
        void nonPositivePrice_throws() {
            // given
            Brand brand = Brand.create("b","b", java.math.BigDecimal.ZERO);
            Product product = Product.builder().brand(brand).productName("n").productInfo("i").productGenderType(ProductGenderType.ALL).brandName("b").categoryPath("c").isAvailable(true).build();
            Inventory inv = Inventory.builder().stockQuantity(new StockQuantity(5)).build();

            // when / then
            assertThatThrownBy(() -> ProductOption.create(product, Money.ZERO, inv)).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("inventory가 null이면 create 호출 시 IllegalArgumentException이 발생해야 한다")
        void nullInventory_throws() {
            // given
            Brand brand = Brand.create("b","b", java.math.BigDecimal.ZERO);
            Product product = Product.builder().brand(brand).productName("n").productInfo("i").productGenderType(ProductGenderType.ALL).brandName("b").categoryPath("c").isAvailable(true).build();

            // when / then
            assertThatThrownBy(() -> ProductOption.create(product, new Money(1000L), null)).isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("재고 차감/복구 및 가용성 검증")
    class StockBehavior {

        @Test
        @DisplayName("차감 수량이 0 이하이면 decreaseStock 호출 시 BusinessException(ErrorCode.VALIDATION_ERROR)이 발생해야 한다")
        void decrease_zeroOrNegative_throwsBusiness() {
            // given
            Brand brand = Brand.create("b","b", java.math.BigDecimal.ZERO);
            Product product = Product.builder().brand(brand).productName("n").productInfo("i").productGenderType(ProductGenderType.ALL).brandName("b").categoryPath("c").isAvailable(true).build();
            Inventory inv = Inventory.builder().stockQuantity(new StockQuantity(5)).build();
            ProductOption option = ProductOption.create(product, new Money(1000L), inv);

            // when / then
            assertThatThrownBy(() -> option.decreaseStock(0))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode()).isEqualTo(ErrorCode.VALIDATION_ERROR));
        }

        @Test
        @DisplayName("재고보다 큰 차감 요청 시 decreaseStock 호출 시 BusinessException(ErrorCode.INSUFFICIENT_STOCK)이 발생해야 한다")
        void decrease_insufficientStock_wrapsBusiness() {
            // given
            Brand brand = Brand.create("b","b", java.math.BigDecimal.ZERO);
            Product product = Product.builder().brand(brand).productName("n").productInfo("i").productGenderType(ProductGenderType.ALL).brandName("b").categoryPath("c").isAvailable(true).build();
            Inventory inv = Inventory.builder().stockQuantity(new StockQuantity(2)).build();
            ProductOption option = ProductOption.create(product, new Money(1000L), inv);

            // when / then
            assertThatThrownBy(() -> option.decreaseStock(3))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode()).isEqualTo(ErrorCode.INSUFFICIENT_STOCK));
        }

        @Test
        @DisplayName("복구 수량이 0 이하이면 restoreStock 호출 시 BusinessException(ErrorCode.VALIDATION_ERROR)이 발생해야 한다")
        void restore_invalidAmount_throwsBusiness() {
            // given
            Brand brand = Brand.create("b","b", java.math.BigDecimal.ZERO);
            Product product = Product.builder().brand(brand).productName("n").productInfo("i").productGenderType(ProductGenderType.ALL).brandName("b").categoryPath("c").isAvailable(true).build();
            Inventory inv = Inventory.builder().stockQuantity(new StockQuantity(2)).build();
            ProductOption option = ProductOption.create(product, new Money(1000L), inv);

            // when / then
            assertThatThrownBy(() -> option.restoreStock(0))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode()).isEqualTo(ErrorCode.VALIDATION_ERROR));
        }

        @Test
        @DisplayName("재고가 0이면 validateAvailable 호출 시 BusinessException(ErrorCode.PRODUCT_OPTION_NOT_AVAILABLE)이 발생해야 한다")
        void validateAvailable_soldOut_throws() {
            // given
            Brand brand = Brand.create("b","b", java.math.BigDecimal.ZERO);
            Product product = Product.builder().brand(brand).productName("n").productInfo("i").productGenderType(ProductGenderType.ALL).brandName("b").categoryPath("c").isAvailable(true).build();
            Inventory inv = Inventory.builder().stockQuantity(new StockQuantity(0)).build();
            ProductOption option = ProductOption.create(product, new Money(1000L), inv);

            // when / then
            assertThatThrownBy(option::validateAvailable)
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode()).isEqualTo(ErrorCode.PRODUCT_OPTION_NOT_AVAILABLE));
        }

        @Test
        @DisplayName("null을 전달하면 addOptionValue는 아무것도 하지 않아야 하며, 정상 ProductOptionValue 추가 시 예외가 없어야 한다")
        void addOptionValue_behaviour() {
            // given
            Brand brand = Brand.create("b","b", java.math.BigDecimal.ZERO);
            Product product = Product.builder().brand(brand).productName("n").productInfo("i").productGenderType(ProductGenderType.ALL).brandName("b").categoryPath("c").isAvailable(true).build();
            Inventory inv = Inventory.builder().stockQuantity(new StockQuantity(5)).build();
            ProductOption option = ProductOption.create(product, new Money(1000L), inv);

            // when: null 입력은 무시
            option.addOptionValue(null);

            // when: 정상 케이스 추가
            OptionValue ov = OptionValue.builder().optionName("size").optionValue("M").build();
            ProductOptionValue pov = ProductOptionValue.create(option, ov);
            option.addOptionValue(pov);

            // then: 예외 없이 normalized ids 호출 가능
            assertThat(option.normalizedOptionValueIds()).isNotNull();
        }
    }
}
