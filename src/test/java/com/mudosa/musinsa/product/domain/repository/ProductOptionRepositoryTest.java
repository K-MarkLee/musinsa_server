package com.mudosa.musinsa.product.domain.repository;

import com.mudosa.musinsa.ServiceConfig;
import com.mudosa.musinsa.brand.domain.model.Brand;
import com.mudosa.musinsa.brand.domain.model.BrandStatus;
import com.mudosa.musinsa.common.vo.Money;
import com.mudosa.musinsa.product.domain.model.*;
import com.mudosa.musinsa.product.domain.vo.StockQuantity;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ProductOptionRepositoryTest extends ServiceConfig {
    @Autowired
    private EntityManager em;

    @Autowired
    private ProductOptionRepository productOptionRepository;

    @DisplayName("ProductOptionId 리스트로 재고를 조회한다.")
    @Test
    void findByProductOptionIdIn(){
        //given
        Brand brand = createBrand();
        em.persist(brand);

        Product product = createProduct(brand, true);
        em.persist(product);

        Inventory inventory1 = createInventory(10);
        Inventory inventory2 = createInventory(20);
        em.persist(inventory1);
        em.persist(inventory2);

        ProductOption productOption1 = createProductOption(product, inventory1, 10000L);
        ProductOption productOption2 = createProductOption(product, inventory2, 10000L);

        em.persist(productOption1);
        em.persist(productOption2);

        List<Long> optionIds = List.of(productOption1.getProductOptionId(), productOption2.getProductOptionId());

        //when
        List<ProductOption> inventories = productOptionRepository.findByProductOptionIdIn(optionIds);

        //then
        assertThat(inventories)
                .extracting(productOption -> productOption.getStockQuantity())
                .contains(10, 20);
    }

    @DisplayName("비관적 락을 건 채로 ProductOptionId로 재고를 조회")
    @Test
    void findByProductOptionIdInWithPessimisticLock(){
        //given
        Brand brand = createBrand();
        em.persist(brand);

        Product product = createProduct(brand, true);
        em.persist(product);

        Inventory inventory1 = createInventory(10);
        Inventory inventory2 = createInventory(20);
        em.persist(inventory1);
        em.persist(inventory2);

        ProductOption productOption1 = createProductOption(product, inventory1, 10000L);
        ProductOption productOption2 = createProductOption(product, inventory2, 10000L);

        em.persist(productOption1);
        em.persist(productOption2);

        List<Long> optionIds = List.of(productOption1.getProductOptionId(), productOption2.getProductOptionId());

        //when
        List<ProductOption> inventories = productOptionRepository.findByProductOptionIdInWithPessimisticLock(optionIds);

        //then
        assertThat(inventories)
                .extracting(p->p.getStockQuantity())
                .contains(10, 20);
    }

    private Inventory createInventory(int stockQuantity) {
        return Inventory.builder()
                .stockQuantity(new StockQuantity(stockQuantity))
                .build();
    }

    private ProductOption createProductOption(Product product, Inventory inventory, Long price) {
        return ProductOption.builder()
                .product(product)
                .productPrice(new Money(price))
                .inventory(inventory)
                .build();
    }

    private Brand createBrand() {
        return Brand.builder()
                .nameKo("테스트 브랜드")
                .nameEn("Test Brand")
                .status(BrandStatus.ACTIVE)
                .commissionRate(new java.math.BigDecimal("10.00"))
                .logoUrl("https://example.com/logo.jpg")
                .build();
    }

    private Product createProduct(Brand brand, boolean isValid) {
        return Product.builder()
                .brand(brand)
                .productName("테스트 상품")
                .productInfo("테스트 상품 설명")
                .productGenderType(ProductGenderType.ALL)
                .brandName(brand.getNameKo())
                .categoryPath("상의/티셔츠")
                .isAvailable(isValid)
                .build();
    }

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