package com.mudosa.musinsa.product.domain.repository;

import com.mudosa.musinsa.common.vo.Money;
import com.mudosa.musinsa.product.domain.model.*;
import com.mudosa.musinsa.brand.domain.model.Brand;
import java.math.BigDecimal;
import com.mudosa.musinsa.product.domain.vo.StockQuantity;
import com.mudosa.musinsa.user.domain.model.User;
import com.mudosa.musinsa.user.domain.model.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import com.mudosa.musinsa.TestJpaConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ImportAutoConfiguration(exclude = MybatisAutoConfiguration.class)
@ActiveProfiles("test")
@ContextConfiguration(classes = TestJpaConfig.class)
@DisplayName("CartItemRepository JPA 테스트")
class CartItemRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Test
    @DisplayName("findAllWithDetailsByUserId는 연관된 옵션/상품/재고를 함께 조회해야 한다")
    void findAllWithDetailsByUserId_fetchesAssociations() {
        // given: 사용자
        User user = User.create("u","p","u@example.com", UserRole.USER, null, null, null);
        em.persist(user);

    // given: product + inventory + option
    Brand brand = Brand.create("brandKo", "brandEn", BigDecimal.ZERO);
    em.persist(brand);
    Product product = Product.builder().brand(brand).productName("p").productInfo("i").productGenderType(ProductGenderType.ALL).brandName("b").categoryPath("c").isAvailable(true).build();
    em.persist(product);

        Inventory inv = Inventory.builder().stockQuantity(new StockQuantity(5)).build();
        em.persist(inv);

        ProductOption po = ProductOption.create(product, new Money(10000), inv);
        em.persist(po);

        // given: cart item
        CartItem ci = CartItem.builder().user(user).productOption(po).quantity(2).build();
        em.persist(ci);
        em.flush();
        em.clear();

        // when
        List<CartItem> list = cartItemRepository.findAllWithDetailsByUserId(user.getId());

        // then
        assertThat(list).hasSize(1);
        CartItem fetched = list.get(0);
        assertThat(fetched.getProductOption()).isNotNull();
        assertThat(fetched.getProductOption().getProduct()).isNotNull();
        assertThat(fetched.getProductOption().getInventory()).isNotNull();
    }

}
