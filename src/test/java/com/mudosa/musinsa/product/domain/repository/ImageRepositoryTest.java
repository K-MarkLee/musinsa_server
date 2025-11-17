package com.mudosa.musinsa.product.domain.repository;

import com.mudosa.musinsa.TestJpaConfig;
import com.mudosa.musinsa.brand.domain.model.Brand;
import com.mudosa.musinsa.product.domain.model.Image;
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

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ImportAutoConfiguration(exclude = MybatisAutoConfiguration.class)
@ActiveProfiles("test")
@ContextConfiguration(classes = TestJpaConfig.class)
@DisplayName("ImageRepository JPA 테스트")
class ImageRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private ImageRepository imageRepository;

    @Test
    @DisplayName("Image를 Product와 함께 저장하고 조회할 수 있다")
    void save_and_find_by_product() {
        Brand brand = Brand.create("b", "B", BigDecimal.ZERO);
        em.persist(brand);
        Product p = Product.builder().brand(brand).productName("p").productInfo("i").productGenderType(ProductGenderType.ALL).brandName("b").categoryPath("c").isAvailable(true).build();
        em.persist(p);

    Image img = Image.create("/img/1.png", true);
    p.addImage(img);
    em.persist(p);
        em.flush();
        em.clear();

    var list = imageRepository.findThumbnailsByProductIds(java.util.List.of(p.getProductId()));
    assertThat(list).isNotEmpty();
    assertThat(list.get(0).getImageUrl()).isEqualTo("/img/1.png");
    }
}
