package com.mudosa.musinsa.product.application.mapper;

import com.mudosa.musinsa.common.vo.Money;
import com.mudosa.musinsa.product.application.dto.ProductDetailResponse;
import com.mudosa.musinsa.product.domain.model.*;
import com.mudosa.musinsa.product.domain.vo.StockQuantity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ProductCommandMapper 단위 테스트")
class ProductCommandMapperTest {

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
    @DisplayName("toProductDetail 매핑")
    class ToDetail {
        @Test
        @DisplayName("Given: 이미지와 옵션값이 있으면 상세 DTO로 매핑된다")
        void mapsImagesAndOptions() {
            Product p = Product.builder()
                    .brand(null)
                    .productName("셔츠")
                    .productInfo("깔끔")
                    .productGenderType(ProductGenderType.ALL)
                    .brandName("브")
                    .categoryPath("상의/셔츠")
                    .isAvailable(true)
                    .build();

            Image img = Image.create("http://img/3.jpg", true);
            p.addImage(img);
            setId(p, "productId", 60L);
            setId(img, "imageId", 61L);

            Inventory inv = Inventory.builder().stockQuantity(new StockQuantity(7)).build();
            ProductOption opt = ProductOption.create(p, new Money(45000), inv);
            setId(opt, "productOptionId", 70L);

            OptionValue ov = OptionValue.builder().optionName("사이즈").optionValue("M").build();
            setId(ov, "optionValueId", 80L);
            ProductOptionValue pov = ProductOptionValue.create(opt, ov);
            opt.addOptionValue(pov);
            p.addProductOption(opt);

            ProductDetailResponse resp = ProductCommandMapper.toProductDetail(p);

            assertThat(resp.getProductId()).isEqualTo(60L);
            assertThat(resp.getImages()).hasSize(1);
            assertThat(resp.getOptions()).hasSize(1);
            ProductDetailResponse.OptionDetail od = resp.getOptions().get(0);
            assertThat(od.getOptionId()).isEqualTo(70L);
            assertThat(od.getStockQuantity()).isEqualTo(7);
            assertThat(od.getHasStock()).isTrue();
            assertThat(od.getOptionValues()).hasSize(1);
            assertThat(od.getOptionValues().get(0).getOptionValueId()).isEqualTo(80L);
        }
    }
}
