package com.mudosa.musinsa.product.application;

import com.mudosa.musinsa.ServiceConfig;
import com.mudosa.musinsa.brand.domain.model.Brand;
import com.mudosa.musinsa.brand.domain.model.BrandMember;
import com.mudosa.musinsa.brand.domain.repository.BrandMemberRepository;
import com.mudosa.musinsa.brand.domain.repository.BrandRepository;
import com.mudosa.musinsa.exception.BusinessException;
import com.mudosa.musinsa.product.application.dto.ProductCreateRequest;
import com.mudosa.musinsa.product.domain.model.Category;
import com.mudosa.musinsa.product.domain.model.OptionValue;
import com.mudosa.musinsa.product.domain.model.Product;
import com.mudosa.musinsa.product.domain.model.ProductOption;
import com.mudosa.musinsa.product.domain.model.ProductOptionValue;
import com.mudosa.musinsa.product.domain.model.ProductGenderType;
import com.mudosa.musinsa.product.domain.repository.CategoryRepository;
import com.mudosa.musinsa.product.domain.repository.OptionValueRepository;
import com.mudosa.musinsa.product.domain.repository.ProductRepository;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * ProductCommandService 핵심 흐름을 실제 DB/트랜잭션 위에서 검증하는 통합 테스트.
 * Chat 모듈과 동일하게 ServiceConfig를 상속받아 공통 설정과 MockitoBean들을 재사용한다.
 */
@Transactional
@DisplayName("ProductCommandService 통합 테스트")
class ProductCommandServiceIntegrationTest extends ServiceConfig {

    private static final Long BRAND_MANAGER_ID = 900L;

    @Autowired
    private ProductCommandService productCommandService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private OptionValueRepository optionValueRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private BrandMemberRepository brandMemberRepository;

    @Test
    @DisplayName("상품 생성 요청이 들어오면 이미지·옵션까지 모두 영속화된다")
    void createProduct_persistsAggregateGraph() {
        // --- 준비: 브랜드, 브랜드 멤버, 카테고리, 옵션 값을 실제 DB에 적재한다.
        Brand brand = brandRepository.save(Brand.create("테스트브랜드", "test-brand", BigDecimal.valueOf(10.0)));
        brandMemberRepository.save(BrandMember.create(BRAND_MANAGER_ID, brand));

        Category parent = categoryRepository.save(Category.builder()
            .categoryName("상의")
            .imageUrl(null)
            .parent(null)
            .build());
        Category category = categoryRepository.save(Category.builder()
            .categoryName("티셔츠")
            .parent(parent)
            .imageUrl(null)
            .build());

        OptionValue color = optionValueRepository.save(
            OptionValue.builder().optionName("색상").optionValue("Red").build());
        OptionValue size = optionValueRepository.save(
            OptionValue.builder().optionName("사이즈").optionValue("M").build());

        ProductCreateRequest request = ProductCreateRequest.builder()
            .productName("베이직 티셔츠")
            .productInfo("유저가 업로드한 기본 티셔츠")
            .productGenderType(ProductGenderType.ALL)
            .categoryPath(category.buildPath())
            .isAvailable(true)
            .images(List.of(ProductCreateRequest.ImageCreateRequest.builder()
                .imageUrl("https://cdn.musinsa.test/product/awesome-tee.jpg")
                .isThumbnail(true)
                .build()))
            .options(List.of(ProductCreateRequest.OptionCreateRequest.builder()
                .productPrice(BigDecimal.valueOf(19900))
                .stockQuantity(7)
                .optionValueIds(List.of(color.getOptionValueId(), size.getOptionValueId()))
                .build()))
            .build();

        // --- 실행: 실제 서비스 메서드를 호출한다.
        Long productId = productCommandService.createProduct(request, brand, category, BRAND_MANAGER_ID);

        // --- 검증: 저장된 상품 그래프를 직접 조회해 본다.
        Product saved = productRepository.findById(productId).orElseThrow();
        assertThat(saved.getProductName()).isEqualTo("베이직 티셔츠");
        assertThat(saved.getImages()).hasSize(1);

        // 옵션 · 재고가 cascade 로 저장되었는지 검증
        assertThat(saved.getProductOptions()).hasSize(1);
        ProductOption option = saved.getProductOptions().get(0);
        assertThat(option.getInventory().getStockQuantity().getValue()).isEqualTo(7);

        List<ProductOptionValue> optionValues = option.getProductOptionValues();
        assertThat(optionValues).hasSize(2);
        assertThat(optionValues)
            .extracting(pov -> pov.getOptionValue().getOptionValue())
            .containsExactlyInAnyOrder("Red", "M");
    }

    @Test
    @DisplayName("브랜드 멤버가 아닌 사용자가 생성 요청을 보내면 예외가 발생한다")
    void createProduct_withoutBrandMembership_shouldFail() {
        // --- 준비: 브랜드/카테고리는 존재하지만 BrandMember 레코드는 만들지 않는다.
        Brand brand = brandRepository.save(Brand.create("다른브랜드", "another", BigDecimal.valueOf(15.0)));
        Category category = categoryRepository.save(Category.builder()
            .categoryName("아우터")
            .imageUrl(null)
            .parent(null)
            .build());

        OptionValue color = optionValueRepository.save(
            OptionValue.builder().optionName("색상").optionValue("Black").build());
        OptionValue size = optionValueRepository.save(
            OptionValue.builder().optionName("사이즈").optionValue("M").build());

        ProductCreateRequest request = ProductCreateRequest.builder()
            .productName("블랙 자켓")
            .productInfo("브랜드 권한 없이 생성 시도")
            .productGenderType(ProductGenderType.MEN)
            .categoryPath(category.buildPath())
            .images(List.of(ProductCreateRequest.ImageCreateRequest.builder()
                .imageUrl("https://cdn.musinsa.test/product/jacket.jpg")
                .isThumbnail(true)
                .build()))
            .options(List.of(ProductCreateRequest.OptionCreateRequest.builder()
                .productPrice(BigDecimal.valueOf(99000))
                .stockQuantity(3)
                .optionValueIds(List.of(color.getOptionValueId(), size.getOptionValueId()))
                .build()))
            .build();

        // --- 검증: 브랜드 멤버가 아니므로 BusinessException 이 발생해야 한다.
        assertThatThrownBy(() -> productCommandService.createProduct(request, brand, category, BRAND_MANAGER_ID))
            .isInstanceOf(BusinessException.class);
    }
}
