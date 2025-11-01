package com.mudosa.musinsa.product.application;

import com.mudosa.musinsa.brand.domain.model.Brand;
import com.mudosa.musinsa.brand.domain.model.BrandStatus;
import com.mudosa.musinsa.product.application.dto.ProductDetailResponse;
import com.mudosa.musinsa.product.application.dto.ProductUpdateRequest;
import com.mudosa.musinsa.product.domain.model.Product;
import com.mudosa.musinsa.product.domain.model.ProductGenderType;
import com.mudosa.musinsa.product.domain.repository.CategoryRepository;
import com.mudosa.musinsa.product.domain.repository.OptionValueRepository;
import com.mudosa.musinsa.product.domain.repository.ProductLikeRepository;
import com.mudosa.musinsa.product.domain.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OptionValueRepository optionValueRepository;

    @Mock
    private ProductLikeRepository productLikeRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    @DisplayName("상품 기본 정보를 수정하면 해당 값이 갱신된다")
    void updateProduct_updatesFields() {
        Brand brand = Brand.builder()
            .brandId(1L)
            .nameKo("브랜드")
            .nameEn("BRAND")
            .status(BrandStatus.ACTIVE)
            .commissionRate(BigDecimal.TEN)
            .build();
        Product product = Product.builder()
            .brand(brand)
            .productName("기존 상품")
            .productInfo("기존 설명")
            .productGenderType(ProductGenderType.MEN)
            .brandName("브랜드")
            .categoryPath("상의/티셔츠")
            .isAvailable(true)
            .build();

        when(productRepository.findDetailById(1L)).thenReturn(Optional.of(product));
        when(productLikeRepository.countByProduct(product)).thenReturn(0L);

        ProductUpdateRequest request = ProductUpdateRequest.builder()
            .productName("수정된 상품")
            .productInfo("수정 설명")
            .productGenderType("women")
            .categoryPath("상의/셔츠")
            .isAvailable(true)
            .images(Collections.singletonList(ProductUpdateRequest.ImageUpdateRequest.builder()
                .imageUrl("http://example.com/thumb.jpg")
                .isThumbnail(true)
                .build()))
            .build();

        ProductDetailResponse response = productService.updateProduct(1L, request);

        assertThat(response.getProductName()).isEqualTo("수정된 상품");
        assertThat(product.getProductGenderType()).isEqualTo(ProductGenderType.WOMEN);
        assertThat(product.getImages()).hasSize(1);
    }

    @Test
    @DisplayName("상품 비활성화 요청 시 isAvailable 이 false 로 바뀐다")
    void disableProduct_changesAvailability() {
        Brand brand = Brand.builder()
            .brandId(1L)
            .nameKo("브랜드")
            .nameEn("BRAND")
            .status(BrandStatus.ACTIVE)
            .commissionRate(BigDecimal.TEN)
            .build();
        Product product = Product.builder()
            .brand(brand)
            .productName("상품")
            .productInfo("설명")
            .productGenderType(ProductGenderType.MEN)
            .brandName("브랜드")
            .categoryPath("상의/티셔츠")
            .isAvailable(true)
            .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        productService.disableProduct(1L);

        assertThat(product.getIsAvailable()).isFalse();
    }
}
