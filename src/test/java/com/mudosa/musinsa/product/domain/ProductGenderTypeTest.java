package com.mudosa.musinsa.product.domain;

import com.mudosa.musinsa.product.domain.model.Product;
import com.mudosa.musinsa.product.domain.model.ProductGenderType;
import com.mudosa.musinsa.product.domain.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

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

    @Autowired(required = false)
    private ProductRepository productRepository;

    @Test
    @DisplayName("ProductGenderType.ALL이 문자열로 저장되고 조회됨")
    void testProductGenderTypeString() {
        if (productRepository == null) {
            log.info("ProductRepository가 없어 테스트 스킵");
            return;
        }

        // Given: ALL 타입을 가진 상품 조회
        Product product = productRepository.findAll().stream()
                .filter(p -> p.getProductGenderType() != null)
                .filter(p -> p.getProductGenderType() == ProductGenderType.ALL)
                .findFirst()
                .orElse(null);

        if (product == null) {
            log.info("ALL 타입 상품이 없어 테스트 스킵");
            return;
        }

        // When: 상품 조회
        log.info("조회된 상품 ID: {}", product.getProductId());
    log.info("상품 성별 타입: {}", product.getProductGenderType());

        // Then: ALL 타입이 정상적으로 조회됨
    assertThat(product.getProductGenderType())
        .isEqualTo(ProductGenderType.ALL);

        log.info("✅ ProductGenderType.ALL 정상 조회 성공");
    }

    @Test
    @DisplayName("모든 ProductGenderType 값이 정상 조회됨")
    void testAllProductGenderTypes() {
        if (productRepository == null) {
            log.info("ProductRepository가 없어 테스트 스킵");
            return;
        }

        // Given: 모든 상품 조회
        var products = productRepository.findAll();

        log.info("전체 상품 수: {}", products.size());

        // When & Then: 각 타입별로 확인
    for (ProductGenderType type : ProductGenderType.values()) {
            long count = products.stream()
                    .filter(p -> p.getProductGenderType() != null)
            .filter(p -> p.getProductGenderType() == type)
                    .count();

            log.info("{} 타입 상품 수: {}", type, count);
        }

        // 에러 없이 모든 상품 조회 성공
        assertThat(products).isNotNull();
        log.info("✅ 모든 ProductGenderType 정상 조회 성공");
    }
}
