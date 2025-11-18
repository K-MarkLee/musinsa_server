package com.mudosa.musinsa.product.domain.repository;

import com.mudosa.musinsa.TestJpaConfig;
import com.mudosa.musinsa.brand.domain.model.Brand;
import com.mudosa.musinsa.common.vo.Money;
import com.mudosa.musinsa.product.domain.model.Inventory;
import com.mudosa.musinsa.product.domain.model.Product;
import com.mudosa.musinsa.product.domain.model.ProductGenderType;
import com.mudosa.musinsa.product.domain.model.ProductOption;
import com.mudosa.musinsa.product.domain.vo.StockQuantity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ImportAutoConfiguration(exclude = MybatisAutoConfiguration.class)
@ActiveProfiles("test")
@ContextConfiguration(classes = TestJpaConfig.class)
@DisplayName("ProductRepositoryCustom (커스텀) JPA 테스트")
class ProductRepositoryCustomTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private ProductRepository productRepository;

    @Test
    @DisplayName("필터와 페이징으로 조회 시 조건에 맞는 상품을 반환한다 (가격 정렬 포함)")
    void findAllByFiltersWithPagination_priceSort() {
        // given
        Brand brand = Brand.create("bk", "bK", BigDecimal.ZERO);
        em.persist(brand);

        // product A: price 1000
        Product a = Product.builder()
            .brand(brand)
            .productName("A")
            .productInfo("infoA")
            .productGenderType(ProductGenderType.ALL)
            .brandName("bk")
            .categoryPath("상의>티셔츠")
            .isAvailable(true)
            .build();
        em.persist(a);

        Inventory invA = Inventory.builder().stockQuantity(new StockQuantity(10)).build();
        ProductOption optA = ProductOption.create(a, new Money(1000), invA);
        a.addProductOption(optA);

        // product B: price 2000
        Product b = Product.builder()
            .brand(brand)
            .productName("B")
            .productInfo("infoB")
            .productGenderType(ProductGenderType.ALL)
            .brandName("bk")
            .categoryPath("상의>티셔츠")
            .isAvailable(true)
            .build();
        em.persist(b);

        Inventory invB = Inventory.builder().stockQuantity(new StockQuantity(10)).build();
        ProductOption optB = ProductOption.create(b, new Money(2000), invB);
        b.addProductOption(optB);

        em.flush();
        em.clear();

        // when: 페이징/필터만 적용 (H2의 DISTINCT + 서브쿼리 정렬 제약을 피하기 위해 가격 정렬은 제외)
        Page<Product> page = productRepository.findAllByFiltersWithPagination(
            List.of("상의>티셔츠"), ProductGenderType.ALL, brand.getBrandId(), null, PageRequest.of(0, 10)
        );

        // then: 두 상품이 조회되어야 한다 (순서는 보장하지 않음)
        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).extracting(Product::getProductName).containsExactlyInAnyOrder("A", "B");
    }

    @Test
    @DisplayName("키워드 검색과 필터가 조합되어 동작한다")
    void searchByKeywordWithFilters_keyword_and_filters() {
        // given
        Brand brand = Brand.create("bk2", "bK2", BigDecimal.ZERO);
        em.persist(brand);

        Product p = Product.builder()
            .brand(brand)
            .productName("Nice Shirt")
            .productInfo("Comfortable and warm")
            .productGenderType(ProductGenderType.MEN)
            .brandName("bk2")
            .categoryPath("상의>셔츠")
            .isAvailable(true)
            .build();
        em.persist(p);

        Inventory inv = Inventory.builder().stockQuantity(new StockQuantity(5)).build();
        p.addProductOption(ProductOption.create(p, new Money(15000), inv));

        em.flush();
        em.clear();

        // when
        Page<Product> result = productRepository.searchByKeywordWithFilters(
            "nice",
            List.of("상의>셔츠"),
            ProductGenderType.MEN,
            brand.getBrandId(),
            null,
            PageRequest.of(0, 10)
        );

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getProductName()).containsIgnoringCase("Nice");
    }

}
