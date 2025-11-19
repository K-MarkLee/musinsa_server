package com.mudosa.musinsa.product.application;

import com.mudosa.musinsa.ServiceConfig;
import com.mudosa.musinsa.brand.domain.model.Brand;
import com.mudosa.musinsa.brand.domain.model.BrandMember;
import com.mudosa.musinsa.brand.domain.repository.BrandMemberRepository;
import com.mudosa.musinsa.brand.domain.repository.BrandRepository;
import com.mudosa.musinsa.common.vo.Money;
import com.mudosa.musinsa.exception.BusinessException;
import com.mudosa.musinsa.product.application.dto.ProductOptionStockResponse;
import com.mudosa.musinsa.product.application.dto.StockAdjustmentRequest;
import com.mudosa.musinsa.product.domain.model.Inventory;
import com.mudosa.musinsa.product.domain.model.Product;
import com.mudosa.musinsa.product.domain.model.ProductGenderType;
import com.mudosa.musinsa.product.domain.model.ProductOption;
import com.mudosa.musinsa.product.domain.repository.InventoryRepository;
import com.mudosa.musinsa.product.domain.repository.ProductRepository;
import com.mudosa.musinsa.product.domain.vo.StockQuantity;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
@DisplayName("ProductInventoryService 통합 테스트")
class ProductInventoryServiceIntegrationTest extends ServiceConfig {

    private static final long BRAND_MANAGER_ID = 910L;

    @Autowired
    private ProductInventoryService inventoryService;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private InventoryRepository inventoryRepository;
    @Autowired
    private BrandRepository brandRepository;
    @Autowired
    private BrandMemberRepository brandMemberRepository;

    private Product initProductWithOption(int stock) {
        Brand brand = brandRepository.save(Brand.create("브랜드", "brand", BigDecimal.ONE));
        brandMemberRepository.save(BrandMember.create(BRAND_MANAGER_ID, brand));

        Product product = productRepository.save(Product.builder()
            .brand(brand)
            .productName("통합테스트 상품")
            .productInfo("재고 조정 검증용 상품")
            .productGenderType(ProductGenderType.ALL)
            .brandName(brand.getNameKo())
            .categoryPath("상의>티셔츠")
            .isAvailable(true)
            .build());

        Inventory inventory = inventoryRepository.save(Inventory.builder()
            .stockQuantity(new StockQuantity(stock))
            .build());
        ProductOption option = ProductOption.create(product, new Money(1000L), inventory);
        product.addProductOption(option);
        return productRepository.save(product);
    }

    @Test
    @DisplayName("재고 증가 호출 시 실제 재고가 늘어나고 DTO에 반영된다")
    void addStock_successfullyIncreasesQuantity() {
        Product product = initProductWithOption(3);
        ProductOption option = product.getProductOptions().get(0);

        StockAdjustmentRequest request = StockAdjustmentRequest.builder()
            .productOptionId(option.getProductOptionId())
            .quantity(4)
            .build();

        ProductOptionStockResponse response = inventoryService.addStock(
            product.getBrand().getBrandId(),
            product.getProductId(),
            request,
            BRAND_MANAGER_ID);

        assertThat(response.getStockQuantity()).isEqualTo(7);
        assertThat(inventoryRepository.findById(option.getInventory().getInventoryId())
            .orElseThrow()
            .getStockQuantity().getValue()).isEqualTo(7);
    }

    @Test
    @DisplayName("재고를 초과해 차감하면 BusinessException 을 던진다")
    void subtractStock_insufficientInventory() {
        Product product = initProductWithOption(2);
        ProductOption option = product.getProductOptions().get(0);

        StockAdjustmentRequest request = StockAdjustmentRequest.builder()
            .productOptionId(option.getProductOptionId())
            .quantity(5)
            .build();

        assertThatThrownBy(() -> inventoryService.subtractStock(
            product.getBrand().getBrandId(),
            product.getProductId(),
            request,
            BRAND_MANAGER_ID)).isInstanceOf(BusinessException.class);
    }

}
