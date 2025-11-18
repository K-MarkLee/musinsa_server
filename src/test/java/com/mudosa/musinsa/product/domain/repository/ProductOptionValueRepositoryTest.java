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

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ImportAutoConfiguration(exclude = MybatisAutoConfiguration.class)
@ActiveProfiles("test")
@ContextConfiguration(classes = TestJpaConfig.class)
@DisplayName("ProductOptionValueRepository JPA 테스트")
class ProductOptionValueRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private ProductOptionValueRepository repository;

    @Test
    @DisplayName("ProductOptionValue가 ProductOption에 연결되어 저장된다")
    void save_and_belongsTo_productOption() {
        Brand brand = Brand.create("b", "B", BigDecimal.ZERO);
        em.persist(brand);
        Product product = Product.builder().brand(brand).productName("p").productInfo("i").productGenderType(ProductGenderType.ALL).brandName("b").categoryPath("c").isAvailable(true).build();
        em.persist(product);

        Inventory inv = Inventory.builder().stockQuantity(new StockQuantity(3)).build();
        em.persist(inv);

        ProductOption po = ProductOption.create(product, new Money(1000), inv);
        em.persist(po);

        OptionValue ov = OptionValue.builder().optionName("size").optionValue("M").build();
        em.persist(ov);

    ProductOptionValue pov = ProductOptionValue.create(po, ov);
    em.persist(pov);
    em.flush();
    em.clear();

    var list = repository.findAllByProductOptionIdsWithOptionValue(java.util.List.of(po.getProductOptionId()));
    assertThat(list).hasSize(1);
    assertThat(list.get(0).getProductOption()).isNotNull();
    assertThat(list.get(0).getOptionValue()).isNotNull();
    }
}
