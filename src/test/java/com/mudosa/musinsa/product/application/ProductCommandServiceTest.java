package com.mudosa.musinsa.product.application;

import com.mudosa.musinsa.brand.domain.model.Brand;
import com.mudosa.musinsa.brand.domain.repository.BrandMemberRepository;
import com.mudosa.musinsa.exception.BusinessException;
import com.mudosa.musinsa.product.application.dto.ProductCreateRequest;
import com.mudosa.musinsa.product.application.dto.ProductDetailResponse;
import com.mudosa.musinsa.product.application.dto.ProductOptionCreateRequest;
import com.mudosa.musinsa.product.domain.model.*;
import com.mudosa.musinsa.product.domain.repository.OptionValueRepository;
import com.mudosa.musinsa.product.domain.repository.ProductOptionRepository;
import com.mudosa.musinsa.product.domain.repository.ProductRepository;
import com.mudosa.musinsa.product.domain.repository.ImageRepository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class ProductCommandServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OptionValueRepository optionValueRepository;

    @Mock
    private BrandMemberRepository brandMemberRepository;

    @Mock
    private ProductOptionRepository productOptionRepository;

    @Mock
    private ImageRepository imageRepository;

    @InjectMocks
    private ProductCommandService service;

    // 
    private void setId(Object target, String fieldName, Long id) {
        try {
            Field f = target.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(target, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private OptionValue buildOptionValue(Long id, String name, String value) {
        OptionValue ov = OptionValue.builder().optionName(name).optionValue(value).build();
        setId(ov, "optionValueId", id);
        return ov;
    }

    @Nested
    @DisplayName("상품 생성 테스트")
    class CreateProduct {

        @Test
        @DisplayName("유효한 요청이면 상품과 옵션이 생성되어 productId를 반환한다")
        void createProduct_success() {
            // given
            Brand brand = Brand.create("브랜드KO", "brandEN", BigDecimal.valueOf(1.5));
            setId(brand, "brandId", 10L);

            Category category = Category.builder().categoryName("상의").parent(null).imageUrl(null).build();

            ProductCreateRequest.OptionCreateRequest optReq = ProductCreateRequest.OptionCreateRequest.builder()
                    .productPrice(BigDecimal.valueOf(10000))
                    .stockQuantity(5)
                    .optionValueIds(List.of(101L, 102L))
                    .build();

            ProductCreateRequest.ImageCreateRequest imgReq = ProductCreateRequest.ImageCreateRequest.builder()
                    .imageUrl("http://img")
                    .isThumbnail(true)
                    .build();

            ProductCreateRequest req = ProductCreateRequest.builder()
                    .productName("티셔츠")
                    .productInfo("설명")
                    .productGenderType(ProductGenderType.ALL)
                    .categoryPath(category.buildPath())
                    .isAvailable(true)
                    .images(List.of(imgReq))
                    .options(List.of(optReq))
                    .build();

            given(brandMemberRepository.existsByBrand_BrandIdAndUserId(eq(10L), anyLong())).willReturn(true);

            OptionValue ov1 = buildOptionValue(101L, "색상", "red");
            OptionValue ov2 = buildOptionValue(102L, "사이즈", "M");
            given(optionValueRepository.findAllByOptionValueIdIn(anyList())).willReturn(List.of(ov1, ov2));

            // simulate save returning entity with id
            willAnswer(invocation -> {
                Product p = invocation.getArgument(0);
                setId(p, "productId", 555L);
                return p;
            }).given(productRepository).save(any(Product.class));

            // when
            Long resultId = service.createProduct(req, brand, category, 999L);

            // then
            assertThat(resultId).isEqualTo(555L);
            then(productRepository).should().save(any(Product.class));
        }

        @Test
        @DisplayName("옵션 값 ID에 존재하지 않는 값이 포함되면 예외가 발생한다")
        void createProduct_missingOptionValue_shouldThrow() {
            Brand brand = Brand.create("브랜드KO", "brandEN", BigDecimal.valueOf(1.5));
            setId(brand, "brandId", 10L);
            Category category = Category.builder().categoryName("상의").parent(null).imageUrl(null).build();

            ProductCreateRequest.OptionCreateRequest optReq = ProductCreateRequest.OptionCreateRequest.builder()
                    .productPrice(BigDecimal.valueOf(10000))
                    .stockQuantity(5)
                    .optionValueIds(List.of(101L, 102L))
                    .build();

            ProductCreateRequest.ImageCreateRequest imgReq = ProductCreateRequest.ImageCreateRequest.builder()
                    .imageUrl("http://img")
                    .isThumbnail(true)
                    .build();

            ProductCreateRequest req = ProductCreateRequest.builder()
                    .productName("티셔츠")
                    .productInfo("설명")
                    .productGenderType(ProductGenderType.ALL)
                    .categoryPath(category.buildPath())
                    .isAvailable(true)
                    .images(List.of(imgReq))
                    .options(List.of(optReq))
                    .build();

            given(brandMemberRepository.existsByBrand_BrandIdAndUserId(eq(10L), anyLong())).willReturn(true);
            // repository returns only one OptionValue, so missing one id
            OptionValue ov1 = buildOptionValue(101L, "색상", "red");
            given(optionValueRepository.findAllByOptionValueIdIn(anyList())).willReturn(List.of(ov1));

            // when / then
            assertThatThrownBy(() -> service.createProduct(req, brand, category, 999L))
                .isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    @DisplayName("addProductOption 메서드")
    class AddProductOptionTests {

        @Test
        @DisplayName("유효한 요청이면 옵션이 추가되고 상세 dto를 반환한다")
        void addOption_success() {
            // given
            Brand brand = Brand.create("브랜드KO", "brandEN", BigDecimal.valueOf(1.5));
            setId(brand, "brandId", 10L);

            Product product = Product.builder().brand(brand).productName("p").productInfo("i").brandName("브랜드").categoryPath("c").isAvailable(true).build();
            setId(product, "productId", 200L);

            ProductOptionCreateRequest req = ProductOptionCreateRequest.builder()
                    .productPrice(BigDecimal.valueOf(5000))
                    .stockQuantity(3)
                    .optionValueIds(List.of(201L, 202L))
                    .build();

            given(brandMemberRepository.existsByBrand_BrandIdAndUserId(eq(10L), anyLong())).willReturn(true);
            given(productRepository.findDetailByIdForManagerWithLock(eq(200L), eq(10L))).willReturn(Optional.of(product));

            OptionValue color = buildOptionValue(201L, "색상", "blue");
            OptionValue size = buildOptionValue(202L, "사이즈", "M");
            given(optionValueRepository.findAllByOptionValueIdIn(anyList())).willReturn(List.of(color, size));
            given(productOptionRepository.existsByProductIdAndOptionValueIds(anyLong(), anyLong(), anyLong()))
                .willReturn(false);

            // when
            ProductDetailResponse.OptionDetail detail = service.addProductOption(10L, 200L, req, 999L);

            // then
            assertThat(detail).isNotNull();
            assertThat(detail.getOptionValues()).hasSize(2);
            then(productRepository).should().findDetailByIdForManagerWithLock(eq(200L), eq(10L));
            then(productRepository).should(never()).findDetailByIdForManager(anyLong(), anyLong());
            then(productOptionRepository).should()
                .existsByProductIdAndOptionValueIds(eq(200L), eq(size.getOptionValueId()), eq(color.getOptionValueId()));
        }

        @Test
        @DisplayName("요청이 null이면 예외가 발생한다")
        void addOption_nullRequest_shouldThrow() {
            Brand brand = Brand.create("브랜드KO", "brandEN", BigDecimal.valueOf(1.5));
            setId(brand, "brandId", 10L);
            Product product = Product.builder().brand(brand).productName("p").productInfo("i").brandName("브랜드").categoryPath("c").isAvailable(true).build();
            setId(product, "productId", 200L);

            given(brandMemberRepository.existsByBrand_BrandIdAndUserId(eq(10L), anyLong())).willReturn(true);
            given(productRepository.findDetailByIdForManagerWithLock(eq(200L), eq(10L))).willReturn(Optional.of(product));

            assertThatThrownBy(() -> service.addProductOption(10L, 200L, null, 999L))
                .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("락 조회 시 상품을 찾지 못하면 예외를 던진다")
        void addOption_whenProductMissing_shouldThrow() {
            given(brandMemberRepository.existsByBrand_BrandIdAndUserId(eq(10L), anyLong())).willReturn(true);
            given(productRepository.findDetailByIdForManagerWithLock(eq(200L), eq(10L))).willReturn(Optional.empty());

            ProductOptionCreateRequest req = ProductOptionCreateRequest.builder()
                    .productPrice(BigDecimal.valueOf(5000))
                    .stockQuantity(3)
                    .optionValueIds(List.of(201L, 202L))
                    .build();

            assertThatThrownBy(() -> service.addProductOption(10L, 200L, req, 999L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("상품을 찾을 수 없습니다");
        }
    }

}
