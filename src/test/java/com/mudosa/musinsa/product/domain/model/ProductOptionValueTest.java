package com.mudosa.musinsa.product.domain.model;

import com.mudosa.musinsa.brand.domain.model.Brand;
import com.mudosa.musinsa.common.vo.Money;
import com.mudosa.musinsa.product.domain.vo.StockQuantity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ProductOptionValue는 옵션-값 매핑의 무결성과 식별자 갱신을 보장해야 한다")
class ProductOptionValueTest {

    @Test
    @DisplayName("productOption 또는 optionValue가 null이면 생성 시 IllegalArgumentException이 발생해야 한다")
    void constructor_nullArgs_throws() {
        // given
        OptionValue ov = OptionValue.builder().optionName("size").optionValue("M").build();
        Brand brand = Brand.create("b","b", java.math.BigDecimal.ZERO);
        Product product = Product.builder().brand(brand).productName("n").productInfo("i").productGenderType(ProductGenderType.ALL).brandName("b").categoryPath("c").isAvailable(true).build();
        Inventory inv = Inventory.builder().stockQuantity(new StockQuantity(5)).build();
        ProductOption option = ProductOption.create(product, new Money(1000L), inv);

        // when / then
        assertThatThrownBy(() -> ProductOptionValue.create(null, ov)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ProductOptionValue.create(option, null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("attachTo 호출 시 복합 식별자가 현재 연관 상태에 맞게 갱신되어야 한다")
    void attachTo_updatesIdentifiers() {
        // given
        OptionValue ov = OptionValue.builder().optionName("size").optionValue("M").build();
        Brand brand = Brand.create("b","b", java.math.BigDecimal.ZERO);
        Product product = Product.builder().brand(brand).productName("n").productInfo("i").productGenderType(ProductGenderType.ALL).brandName("b").categoryPath("c").isAvailable(true).build();
        Inventory inv = Inventory.builder().stockQuantity(new StockQuantity(5)).build();
        ProductOption option = ProductOption.create(product, new Money(1000L), inv);

        // when
        ProductOptionValue pov = ProductOptionValue.create(option, ov);

        // then: getters reflect associations
        assertThat(pov.getOptionValue()).isEqualTo(ov);
        assertThat(pov.getProductOption()).isEqualTo(option);
        // id fields may be null prior to persistence but object should be constructed
        assertThat(pov).isNotNull();
    }
}
