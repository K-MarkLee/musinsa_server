package com.mudosa.musinsa.product.domain.repository;

import com.mudosa.musinsa.TestJpaConfig;
import com.mudosa.musinsa.product.domain.model.Category;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ImportAutoConfiguration(exclude = MybatisAutoConfiguration.class)
@ActiveProfiles("test")
@ContextConfiguration(classes = TestJpaConfig.class)
@DisplayName("CategoryRepository JPA 테스트")
class CategoryRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    @DisplayName("카테고리를 이름으로 조회할 수 있다")
    void findByCategoryName() {
        // given
        Category root = Category.builder().categoryName("상의").imageUrl(null).parent(null).build();
        em.persist(root);

        Category child = Category.builder().categoryName("티셔츠").parent(root).imageUrl(null).build();
        em.persist(child);

        em.flush();
        em.clear();

        // when
        Optional<Category> found = categoryRepository.findByCategoryName("티셔츠");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getCategoryName()).isEqualTo("티셔츠");
    }

    @Test
    @DisplayName("경로로 카테고리를 찾을 수 있다 and isValidPath 동작 확인")
    void findByPath_and_isValidPath() {
        // given: 상의 > 카디건
        Category root = Category.builder().categoryName("상의").imageUrl(null).parent(null).build();
        em.persist(root);

        Category child = Category.builder().categoryName("카디건").parent(root).imageUrl(null).build();
        em.persist(child);

        em.flush();
        em.clear();

        // when
        String path = "상의>카디건";
        Category found = categoryRepository.findByPath(path);
        boolean valid = categoryRepository.isValidPath(path);

        // then
        assertThat(found).isNotNull();
        assertThat(found.getCategoryName()).isEqualTo("카디건");
        assertThat(valid).isTrue();
    }

}
