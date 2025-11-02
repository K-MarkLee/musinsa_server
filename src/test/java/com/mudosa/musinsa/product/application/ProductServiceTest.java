package com.mudosa.musinsa.product.application;

import com.mudosa.musinsa.brand.domain.model.Brand;
import com.mudosa.musinsa.brand.domain.model.BrandStatus;
import com.mudosa.musinsa.exception.BusinessException;
import com.mudosa.musinsa.exception.ErrorCode;
import com.mudosa.musinsa.common.vo.Money;
import com.mudosa.musinsa.product.application.dto.ProductDetailResponse;
import com.mudosa.musinsa.product.application.dto.ProductOptionCreateRequest;
import com.mudosa.musinsa.product.application.dto.ProductUpdateRequest;
import com.mudosa.musinsa.product.domain.model.Product;
import com.mudosa.musinsa.product.domain.model.ProductGenderType;
import com.mudosa.musinsa.product.domain.model.ProductOption;
import com.mudosa.musinsa.product.domain.model.OptionName;
import com.mudosa.musinsa.product.domain.model.OptionValue;
import com.mudosa.musinsa.product.domain.model.Inventory;
import com.mudosa.musinsa.product.domain.vo.StockQuantity;
import com.mudosa.musinsa.product.domain.repository.OptionValueRepository;
import com.mudosa.musinsa.product.domain.repository.ProductLikeRepository;
import com.mudosa.musinsa.product.domain.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OptionValueRepository optionValueRepository;

    @Mock
    private ProductLikeRepository productLikeRepository;

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
        String originalCategoryPath = product.getCategoryPath();

        when(productRepository.findDetailById(1L)).thenReturn(Optional.of(product));
        when(productLikeRepository.countByProduct(product)).thenReturn(0L);

        ProductUpdateRequest request = ProductUpdateRequest.builder()
            .productName("수정된 상품")
            .productInfo("수정 설명")
            .productGenderType("women")
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
    assertThat(product.getCategoryPath()).isEqualTo(originalCategoryPath);
    }

    @Test
    @DisplayName("변경할 값이 없으면 상품 수정이 거부된다")
    void updateProduct_noChanges_throws() {
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

        ProductUpdateRequest request = ProductUpdateRequest.builder().build();

        assertThatThrownBy(() -> productService.updateProduct(1L, request))
            .isInstanceOf(BusinessException.class)
            .extracting(ex -> ((BusinessException) ex).getErrorCode())
            .isEqualTo(ErrorCode.VALIDATION_ERROR);
    }

    @Test
    @DisplayName("브랜드 관리자가 옵션을 추가하면 새로운 옵션이 등록된다")
    void addProductOption_addsNewOption() {
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
        ReflectionTestUtils.setField(product, "productId", 1L);

        OptionName optionName = OptionName.builder()
            .optionName("사이즈")
            .build();
        ReflectionTestUtils.setField(optionName, "optionNameId", 5L);

        OptionValue optionValue = OptionValue.builder()
            .optionName(optionName)
            .optionValue("M")
            .build();
        ReflectionTestUtils.setField(optionValue, "optionValueId", 11L);

        ProductOptionCreateRequest request = ProductOptionCreateRequest.builder()
            .productPrice(BigDecimal.valueOf(15000))
            .stockQuantity(3)
            .optionValueIds(List.of(11L))
            .build();

        when(productRepository.findDetailById(1L)).thenReturn(Optional.of(product));
        when(optionValueRepository.findAllByOptionValueIdIn(anyList())).thenReturn(List.of(optionValue));

        doAnswer(invocation -> {
            product.getProductOptions().forEach(option ->
                ReflectionTestUtils.setField(option, "productOptionId", 99L));
            return null;
        }).when(productRepository).flush();

        ProductDetailResponse.OptionDetail response = productService.addProductOption(1L, 1L, request);

        assertThat(product.getProductOptions()).hasSize(1);
        assertThat(response.getOptionId()).isEqualTo(99L);
        assertThat(response.getProductPrice()).isEqualTo(BigDecimal.valueOf(15000));
        assertThat(response.getOptionValues()).hasSize(1);
    }

    @Test
    @DisplayName("브랜드 관리자가 옵션을 삭제하면 옵션 컬렉션에서 제거된다")
    void removeProductOption_removesOption() {
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
        ReflectionTestUtils.setField(product, "productId", 1L);

        Inventory inventory = Inventory.builder()
            .stockQuantity(new StockQuantity(5))
            .build();

        ProductOption productOption = ProductOption.builder()
            .product(product)
            .productPrice(new Money(BigDecimal.valueOf(12000)))
            .inventory(inventory)
            .productOptionValues(Collections.emptyList())
            .build();

        ReflectionTestUtils.setField(productOption, "productOptionId", 200L);
        product.addProductOption(productOption);

        when(productRepository.findDetailById(1L)).thenReturn(Optional.of(product));

        productService.removeProductOption(1L, 1L, 200L);

        assertThat(product.getProductOptions()).isEmpty();
        verify(productRepository).flush();
    }
}
