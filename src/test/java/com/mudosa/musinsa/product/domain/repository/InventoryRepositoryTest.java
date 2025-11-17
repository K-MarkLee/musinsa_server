package com.mudosa.musinsa.product.domain.repository;

import com.mudosa.musinsa.TestJpaConfig;
import com.mudosa.musinsa.product.domain.model.Inventory;
import com.mudosa.musinsa.product.domain.vo.StockQuantity;
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
@DisplayName("InventoryRepository JPA 테스트")
class InventoryRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Test
    @DisplayName("Inventory 저장 후 조회가 가능하다")
    void save_and_find() {
        // given
        Inventory inv = Inventory.builder().stockQuantity(new StockQuantity(10)).build();
        em.persist(inv);
        em.flush();
        em.clear();

        // when
        Optional<Inventory> found = inventoryRepository.findById(inv.getInventoryId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getStockQuantity().getValue()).isEqualTo(10);
    }
}
