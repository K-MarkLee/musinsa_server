package com.mudosa.musinsa.product.application;

import com.mudosa.musinsa.ServiceConfig;
import com.mudosa.musinsa.brand.domain.model.Brand;
import com.mudosa.musinsa.brand.domain.repository.BrandRepository;
import com.mudosa.musinsa.common.vo.Money;
import com.mudosa.musinsa.product.application.dto.ProductDetailResponse;
import com.mudosa.musinsa.product.application.dto.ProductSearchCondition;
import com.mudosa.musinsa.product.application.dto.ProductSearchResponse;
import com.mudosa.musinsa.product.domain.model.Image;
import com.mudosa.musinsa.product.domain.model.Inventory;
import com.mudosa.musinsa.product.domain.model.Product;
import com.mudosa.musinsa.product.domain.model.ProductGenderType;
import com.mudosa.musinsa.product.domain.model.ProductOption;
import com.mudosa.musinsa.product.domain.repository.ProductRepository;
import com.mudosa.musinsa.product.domain.vo.StockQuantity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ProductQueryService 통합 테스트")
class ProductQueryServiceIntegrationTest extends ServiceConfig {

    @Autowired
    private ProductQueryService productQueryService;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private BrandRepository brandRepository;

    private Product cheapProduct;
    private Product midProduct;
    private Product expensiveProduct;
    private Product womenProduct;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        brandRepository.deleteAll();

        Brand brand = brandRepository.save(Brand.create("조회브랜드", "query-brand", BigDecimal.ONE));

        cheapProduct = productRepository.save(buildProduct(brand, "베이직 티셔츠", "상의>티셔츠", 20000, 5, ProductGenderType.ALL));
        midProduct = productRepository.save(buildProduct(brand, "스탠다드 티셔츠", "상의>티셔츠", 35000, 4, ProductGenderType.ALL));
        expensiveProduct = productRepository.save(buildProduct(brand, "프리미엄 티셔츠", "상의>티셔츠", 50000, 3, ProductGenderType.ALL));
        womenProduct = productRepository.save(buildProduct(brand, "여성 블라우스", "여성>블라우스", 40000, 6, ProductGenderType.WOMEN));
    }

    private Product buildProduct(Brand brand,
                                 String name,
                                 String categoryPath,
                                 long price,
                                 int stock,
                                 ProductGenderType gender) {
        Product product = Product.builder()
            .brand(brand)
            .productName(name)
            .productInfo("설명 " + name)
            .productGenderType(gender)
            .brandName(brand.getNameKo())
            .categoryPath(categoryPath)
            .isAvailable(true)
            .build();

        product.addImage(Image.create("https://cdn.musinsa.test/" + name + ".jpg", true));

        Inventory inventory = Inventory.builder()
            .stockQuantity(new StockQuantity(stock))
            .build();
        ProductOption option = ProductOption.create(product, new Money(price), inventory);
        product.addProductOption(option);

        return product;
    }

    @Test
    @DisplayName("LOWEST 정렬은 가격이 낮은 순서(저가→중간→고가)로 결과를 반환한다")
    void searchProducts_sortedByLowestPrice() {
        ProductSearchCondition condition = ProductSearchCondition.builder()
            .keyword("티셔츠")
            .categoryPaths(List.of("상의>티셔츠"))
            .gender(ProductGenderType.ALL)
            .priceSort(ProductSearchCondition.PriceSort.LOWEST)
            .pageable(PageRequest.of(0, 10))
            .build();

        ProductSearchResponse response = productQueryService.searchProducts(condition);

        assertThat(response.getProducts()).hasSize(3);
        assertThat(response.getProducts().get(0).getProductName()).isEqualTo(cheapProduct.getProductName());
        assertThat(response.getProducts().get(1).getProductName()).isEqualTo(midProduct.getProductName());
        assertThat(response.getProducts().get(2).getProductName()).isEqualTo(expensiveProduct.getProductName());
    }

    @Test
    @DisplayName("HIGHEST 정렬은 최고가 상품이 맨 앞에 온다")
    void searchProducts_sortedByHighestPrice() {
        ProductSearchCondition condition = ProductSearchCondition.builder()
            .keyword("티셔츠")
            .categoryPaths(List.of("상의>티셔츠"))
            .gender(ProductGenderType.ALL)
            .priceSort(ProductSearchCondition.PriceSort.HIGHEST)
            .pageable(PageRequest.of(0, 10))
            .build();

        ProductSearchResponse response = productQueryService.searchProducts(condition);

        assertThat(response.getProducts()).hasSize(3);
        assertThat(response.getProducts().get(0).getProductName()).isEqualTo(expensiveProduct.getProductName());
    }

    @Test
    @DisplayName("카테고리 필터를 적용하면 다른 카테고리 상품은 제외된다")
    void searchProducts_filtersByCategory() {
        ProductSearchCondition condition = ProductSearchCondition.builder()
            .categoryPaths(List.of("상의>티셔츠"))
            .pageable(PageRequest.of(0, 10))
            .build();

        ProductSearchResponse response = productQueryService.searchProducts(condition);

        assertThat(response.getProducts()).extracting(ProductSearchResponse.ProductSummary::getProductName)
            .contains(cheapProduct.getProductName(), midProduct.getProductName(), expensiveProduct.getProductName())
            .doesNotContain(womenProduct.getProductName());
    }

    @Test
    @DisplayName("성별 필터를 WOMEN으로 주면 여성 카테고리 상품만 조회된다")
    void searchProducts_filtersByGender() {
        ProductSearchCondition condition = ProductSearchCondition.builder()
            .gender(ProductGenderType.WOMEN)
            .pageable(PageRequest.of(0, 10))
            .build();

        ProductSearchResponse response = productQueryService.searchProducts(condition);

        assertThat(response.getProducts()).hasSize(1);
        assertThat(response.getProducts().get(0).getProductName()).isEqualTo(womenProduct.getProductName());
    }

    @Test
    @DisplayName("상품 ID로 상세 조회 시 이미지와 옵션 정보가 함께 반환된다")
    void getProductDetail_returnsAggregate() {
        ProductDetailResponse detail = productQueryService.getProductDetail(midProduct.getProductId());

        assertThat(detail.getProductId()).isEqualTo(midProduct.getProductId());
        assertThat(detail.getImages()).hasSize(1);
        assertThat(detail.getOptions()).hasSize(1);
        assertThat(detail.getOptions().get(0).getHasStock()).isTrue();
    }
}
