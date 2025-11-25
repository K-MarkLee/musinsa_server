package com.mudosa.musinsa.product.application.mapper;

import com.mudosa.musinsa.common.vo.Money;
import com.mudosa.musinsa.product.application.dto.ProductSearchResponse;
import com.mudosa.musinsa.product.application.dto.ProductDetailResponse;
import com.mudosa.musinsa.product.domain.model.*;
import com.mudosa.musinsa.product.domain.vo.StockQuantity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ProductQueryMapper 단위 테스트")
class ProductQueryMapperTest {

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
    @DisplayName("toProductSummary 매핑")
    class SummaryMapping {
        @Test
        @DisplayName("Given: thumbnail 이미지와 재고가 있는 옵션들이 있으면 요약에 thumbnail, hasStock, lowestPrice가 반영된다")
        void toProductSummary_mapsFields() {
            // Given: product with two options, one with stock=0 and one with stock=5
            Product p = Product.builder()
                    .brand(null)
                    .productName("티")
                    .productInfo("설명")
                    .productGenderType(ProductGenderType.ALL)
                    .brandName("브")
                    .categoryPath("상의/티")
                    .isAvailable(true)
                    .build();

            Image thumb = Image.builder()
                    .imageUrl("http://img/1.jpg")
                    .isThumbnail(true)
                    .build();
            p.addImage(thumb);
            setId(p, "productId", 10L);
            setId(thumb, "imageId", 11L);

            Inventory inv1 = Inventory.builder().stockQuantity(new StockQuantity(0)).build();
            ProductOption o1 = ProductOption.create(p, new Money(15000), inv1);
            setId(o1, "productOptionId", 21L);

            Inventory inv2 = Inventory.builder().stockQuantity(new StockQuantity(5)).build();
            ProductOption o2 = ProductOption.create(p, new Money(12000), inv2);
            setId(o2, "productOptionId", 22L);

            p.addProductOption(o1);
            p.addProductOption(o2);

            // When
            ProductSearchResponse.ProductSummary s = ProductQueryMapper.toProductSummary(p);

            // Then
            assertThat(s.getProductId()).isEqualTo(10L);
            assertThat(s.getThumbnailUrl()).isEqualTo("http://img/1.jpg");
            assertThat(s.getHasStock()).isTrue();
            assertThat(s.getLowestPrice()).isEqualByComparingTo(new BigDecimal("12000.00"));
        }
    }

    @Nested
    @DisplayName("toProductDetail 매핑")
    class DetailMapping {
        @Test
        @DisplayName("Given: 옵션에 option values와 재고 정보가 있으면 상세 응답에 매핑된다")
        void toProductDetail_mapsOptionsAndImages() {
            // Given
            Product p = Product.builder()
                    .brand(null)
                    .productName("바지")
                    .productInfo("편함")
                    .productGenderType(ProductGenderType.ALL)
                    .brandName("브")
                    .categoryPath("하의/바지")
                    .isAvailable(true)
                    .build();

            Image img = Image.builder()
                    .imageUrl("http://example.com/image2.jpg")
                    .isThumbnail(false)
                    .build();
            p.addImage(img);
            setId(p, "productId", 30L);
            setId(img, "imageId", 31L);

            Inventory inv = Inventory.builder().stockQuantity(new StockQuantity(2)).build();
            ProductOption opt = ProductOption.create(p, new Money(30000), inv);
            setId(opt, "productOptionId", 40L);

        OptionValue ov = OptionValue.builder()
                .optionName("컬러")
                .optionValue("레드")
                .build();
        setId(ov, "optionValueId", 50L);
        ProductOptionValue mapping = ProductOptionValue.create(opt, ov);
            opt.addOptionValue(mapping);
            p.addProductOption(opt);

            // When
            ProductDetailResponse resp = ProductQueryMapper.toProductDetail(p);

            // Then
            assertThat(resp.getProductId()).isEqualTo(30L);
            assertThat(resp.getImages()).hasSize(1);
            assertThat(resp.getOptions()).hasSize(1);
            ProductDetailResponse.OptionDetail od = resp.getOptions().get(0);
            assertThat(od.getStockQuantity()).isEqualTo(2);
            assertThat(od.getOptionValues()).hasSize(1);
            assertThat(od.getOptionValues().get(0).getOptionValueId()).isEqualTo(50L);
        }
    }
}
