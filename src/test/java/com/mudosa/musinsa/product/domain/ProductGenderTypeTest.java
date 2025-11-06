package com.mudosa.musinsa.product.domain;

import com.mudosa.musinsa.brand.domain.model.Brand;
import com.mudosa.musinsa.brand.domain.repository.BrandRepository;
import com.mudosa.musinsa.product.domain.model.Product;
import com.mudosa.musinsa.product.domain.model.ProductGenderType;
import com.mudosa.musinsa.product.domain.repository.ProductRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ProductGenderType Enum 저장/조회 테스트
 *
 * 목적: @Enumerated(EnumType.STRING) 적용 후 정상 동작 확인
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ProductGenderTypeTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    @DisplayName("ProductGenderType는 문자열로 저장되고 조회된다")
    void persistsEnumAsString() {
        Brand brand = createBrand();
        Product saved = productRepository.save(createProduct(brand, ProductGenderType.ALL, "ALL"));

        entityManager.flush();
        entityManager.clear();

        Product reloaded = productRepository.findById(saved.getProductId())
            .orElseThrow();

        assertThat(reloaded.getProductGenderType()).isEqualTo(ProductGenderType.ALL);

        String rawValue = jdbcTemplate.queryForObject(
            "select product_gender_type from product where product_id = ?",
            String.class,
            saved.getProductId()
        );
        assertThat(rawValue).isEqualTo("ALL");
    }

    @Test
    @DisplayName("모든 ProductGenderType 값을 영속화해도 정상 조회된다")
    void persistsAllEnumValues() {
        Brand brand = createBrand();

        EnumMap<ProductGenderType, Long> productIds = new EnumMap<>(ProductGenderType.class);
        for (ProductGenderType genderType : ProductGenderType.values()) {
            Product product = productRepository.save(createProduct(brand, genderType, genderType.name()));
            productIds.put(genderType, product.getProductId());
        }

        entityManager.flush();
        entityManager.clear();

        List<Product> found = productRepository.findAllById(productIds.values());

        assertThat(found)
            .extracting(Product::getProductGenderType)
            .containsExactlyInAnyOrder(ProductGenderType.values());
    }

    private Brand createBrand() {
        return brandRepository.save(Brand.create("테스트 브랜드", "Test Brand", new BigDecimal("10.00")));
    }

    private Product createProduct(Brand brand, ProductGenderType genderType, String suffix) {
        return Product.builder()
            .brand(brand)
            .productName("테스트 상품-" + suffix)
            .productInfo("상세 설명")
            .productGenderType(genderType)
            .brandName(brand.getNameKo())
            .categoryPath("상의/티셔츠")
            .build();
    }
}
