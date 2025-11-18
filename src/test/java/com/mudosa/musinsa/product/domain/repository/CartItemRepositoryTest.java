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

    @Test
    @DisplayName("deleteByUserIdAndProductOptionIdIn는 삭제 건수를 반환하고 실제로 삭제한다")
    @Transactional
    void deleteByUserIdAndProductOptionIdIn_deletesAndReturnsCount() {
        // given: user
        User user = User.create("u2","p","u2@example.com", UserRole.USER, null, null, null);
        em.persist(user);

    Brand brand = Brand.create("brandKo2", "brandEn2", BigDecimal.ZERO);
    em.persist(brand);
    Product product = Product.builder().brand(brand).productName("p").productInfo("i").productGenderType(ProductGenderType.ALL).brandName("b").categoryPath("c").isAvailable(true).build();
    em.persist(product);

    Inventory inv1 = Inventory.builder().stockQuantity(new StockQuantity(5)).build();
    em.persist(inv1);

    Inventory inv2 = Inventory.builder().stockQuantity(new StockQuantity(5)).build();
    em.persist(inv2);

    ProductOption po1 = ProductOption.create(product, new Money(5000), inv1);
    em.persist(po1);
    ProductOption po2 = ProductOption.create(product, new Money(6000), inv2);
    em.persist(po2);

        CartItem c1 = CartItem.builder().user(user).productOption(po1).quantity(1).build();
        CartItem c2 = CartItem.builder().user(user).productOption(po2).quantity(1).build();
        em.persist(c1);
        em.persist(c2);
        em.flush();

        // when
        int deleted = cartItemRepository.deleteByUserIdAndProductOptionIdIn(user.getId(), List.of(po1.getProductOptionId(), po2.getProductOptionId()));

        // then
        assertThat(deleted).isEqualTo(2);
        assertThat(cartItemRepository.findAllByUserId(user.getId())).isEmpty();
    }
}
