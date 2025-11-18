package com.mudosa.musinsa.product.domain.repository;

import com.mudosa.musinsa.TestJpaConfig;
import com.mudosa.musinsa.brand.domain.model.Brand;
import com.mudosa.musinsa.product.domain.model.Product;
import com.mudosa.musinsa.product.domain.model.ProductGenderType;
import com.mudosa.musinsa.product.domain.model.ProductLike;
import com.mudosa.musinsa.user.domain.model.User;
import com.mudosa.musinsa.user.domain.model.UserRole;
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
@DisplayName("ProductLikeRepository JPA 테스트")
class ProductLikeRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private ProductLikeRepository productLikeRepository;

    @Test
    @DisplayName("ProductLike 저장 후 조회가 가능하다")
    void save_and_find() {
        Brand brand = Brand.create("b", "B", BigDecimal.ZERO);
        em.persist(brand);
        Product p = Product.builder().brand(brand).productName("p").productInfo("i").productGenderType(ProductGenderType.ALL).brandName("b").categoryPath("c").isAvailable(true).build();
        em.persist(p);

        User user = User.create("u", "pw", "u@example.com", UserRole.USER, null, null, null);
        em.persist(user);

    ProductLike like = ProductLike.builder().product(p).userId(user.getId()).build();
        em.persist(like);
        em.flush();
        em.clear();

        var found = productLikeRepository.findById(like.getProductLikeId());
        assertThat(found).isPresent();
    }
}
