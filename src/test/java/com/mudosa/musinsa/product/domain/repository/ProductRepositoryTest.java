package com.mudosa.musinsa.product.domain.repository;

import com.mudosa.musinsa.TestJpaConfig;
import com.mudosa.musinsa.brand.domain.model.Brand;
import com.mudosa.musinsa.product.domain.model.Product;
import com.mudosa.musinsa.product.domain.model.ProductGenderType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ImportAutoConfiguration(exclude = MybatisAutoConfiguration.class)
@ActiveProfiles("test")
@ContextConfiguration(classes = TestJpaConfig.class)
@DisplayName("ProductRepository JPA 테스트")
class ProductRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private ProductRepository productRepository;

    @Test
    @DisplayName("Product 저장 후 조회된다")
    void save_and_find() {
        Brand brand = Brand.create("bk", "bK", BigDecimal.ZERO);
        em.persist(brand);

        Product p = Product.builder().brand(brand).productName("name").productInfo("info").productGenderType(ProductGenderType.ALL).brandName("bn").categoryPath("cat").isAvailable(true).build();
        em.persist(p);
        em.flush();
        em.clear();

        Optional<Product> found = productRepository.findById(p.getProductId());
        assertThat(found).isPresent();
        assertThat(found.get().getProductName()).isEqualTo("name");
    }
}
