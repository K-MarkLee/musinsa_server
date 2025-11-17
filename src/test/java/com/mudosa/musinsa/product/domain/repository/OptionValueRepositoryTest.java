package com.mudosa.musinsa.product.domain.repository;

import com.mudosa.musinsa.TestJpaConfig;
import com.mudosa.musinsa.product.domain.model.OptionValue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ImportAutoConfiguration(exclude = MybatisAutoConfiguration.class)
@ActiveProfiles("test")
@ContextConfiguration(classes = TestJpaConfig.class)
@DisplayName("OptionValueRepository JPA 테스트")
class OptionValueRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private OptionValueRepository optionValueRepository;

    @Test
    @DisplayName("OptionValue 저장 후 조회된다")
    void save_and_find() {
    OptionValue v = OptionValue.builder().optionName("color").optionValue("red").build();
        em.persist(v);
        em.flush();
        em.clear();

        var found = optionValueRepository.findById(v.getOptionValueId());
        assertThat(found).isPresent();
    assertThat(found.get().getOptionValue()).isEqualTo("red");
    }
}
