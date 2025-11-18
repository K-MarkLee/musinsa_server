package com.mudosa.musinsa.product.domain.repository;

import com.mudosa.musinsa.TestJpaConfig;
import com.mudosa.musinsa.brand.domain.model.Brand;
import com.mudosa.musinsa.product.domain.model.*;
import com.mudosa.musinsa.product.domain.vo.StockQuantity;
import com.mudosa.musinsa.common.vo.Money;
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
@DisplayName("ProductOptionRepository JPA 테스트")
class ProductOptionRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private ProductOptionRepository productOptionRepository;

    @Test
    @DisplayName("ProductOption 저장 시 Product와 Inventory가 매핑되어 조회된다")
    void save_and_fetch_relations() {
        Brand brand = Brand.create("b", "B", BigDecimal.ZERO);
        em.persist(brand);
        Product product = Product.builder().brand(brand).productName("p").productInfo("i").productGenderType(ProductGenderType.ALL).brandName("b").categoryPath("c").isAvailable(true).build();
        em.persist(product);

        Inventory inv = Inventory.builder().stockQuantity(new StockQuantity(7)).build();
        em.persist(inv);

        ProductOption po = ProductOption.create(product, new Money(1000), inv);
        em.persist(po);
        em.flush();
        em.clear();

        Optional<ProductOption> found = productOptionRepository.findById(po.getProductOptionId());
        assertThat(found).isPresent();
        ProductOption fetched = found.get();
        assertThat(fetched.getProduct()).isNotNull();
        assertThat(fetched.getInventory()).isNotNull();
        assertThat(fetched.getInventory().getStockQuantity().getValue()).isEqualTo(7);
    }
}
