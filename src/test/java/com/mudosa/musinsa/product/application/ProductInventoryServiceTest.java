package com.mudosa.musinsa.product.application;

import com.mudosa.musinsa.brand.domain.model.Brand;
import com.mudosa.musinsa.brand.domain.repository.BrandMemberRepository;
import com.mudosa.musinsa.exception.BusinessException;
import com.mudosa.musinsa.product.application.dto.ProductAvailabilityRequest;
import com.mudosa.musinsa.product.application.dto.ProductOptionStockResponse;
import com.mudosa.musinsa.product.application.dto.StockAdjustmentRequest;
import com.mudosa.musinsa.product.domain.model.*;
import com.mudosa.musinsa.product.domain.repository.InventoryRepository;
import com.mudosa.musinsa.product.domain.repository.ProductOptionRepository;
import com.mudosa.musinsa.product.domain.repository.ProductRepository;
import com.mudosa.musinsa.product.domain.vo.StockQuantity;
import com.mudosa.musinsa.common.vo.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class ProductInventoryServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductOptionRepository productOptionRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private BrandMemberRepository brandMemberRepository;

    @InjectMocks
    private ProductInventoryService service;

    private void setId(Object target, String fieldName, Long id) {
        try {
            Field f = target.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(target, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Nested
    @DisplayName("adjustStock 검증")
    class AdjustStockTests {

        @Test
        @DisplayName("수량이 null이면 예외가 발생한다")
        void adjustStock_nullQuantity_shouldThrow() {
            // Given: null 값 전달
            // When / Then
            assertThatThrownBy(() -> service.adjustStock(1L, null, true))
                .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("재고 엔티티가 존재하지 않으면 예외가 발생한다")
        void adjustStock_inventoryNotFound_shouldThrow() {
            // Given: repository가 빈 Optional 반환
            given(inventoryRepository.findByProductOptionIdWithLock(anyLong())).willReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> service.adjustStock(1L, 5, true))
                .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("차감 시 재고 부족이면 BusinessException으로 래핑된다")
        void adjustStock_decrease_insufficient() {
            // Given: 재고 수량이 적은 Inventory 엔티티가 존재한다
            Inventory inv = Inventory.builder().stockQuantity(new StockQuantity(1)).build();
            setId(inv, "inventoryId", 99L);
            given(inventoryRepository.findByProductOptionIdWithLock(eq(10L))).willReturn(Optional.of(inv));

            // When / Then
            assertThatThrownBy(() -> service.adjustStock(10L, 5, false))
                .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("증가 시 재고가 늘어나고 저장된다")
        void adjustStock_increase_success() {
            // Given: 초기 재고가 2인 Inventory 엔티티가 존재한다
            Inventory inv = Inventory.builder().stockQuantity(new StockQuantity(2)).build();
            setId(inv, "inventoryId", 100L);
            given(inventoryRepository.findByProductOptionIdWithLock(eq(20L))).willReturn(Optional.of(inv));

            // When
            Inventory updated = service.adjustStock(20L, 3, true);

            // Then
            assertThat(updated.getStockQuantity().getValue()).isEqualTo(5);
            then(inventoryRepository).should().save(inv);
        }
    }

    @Nested
    @DisplayName("addStock / subtractStock 동작")
    class ModifyStockTests {

        @Test
        @DisplayName("addStock 호출 시 재고가 추가되고 DTO가 반환된다")
        void addStock_success() {
            // Given: 브랜드, 상품, 옵션, 재고를 구성한다
            Brand brand = Brand.create("b", "b", BigDecimal.ONE);
            setId(brand, "brandId", 1L);

            Product product = Product.builder().brand(brand).productName("p").productInfo("i").productGenderType(ProductGenderType.ALL).brandName("b").categoryPath("c").isAvailable(true).build();
            setId(product, "productId", 2L);

            Inventory inv = Inventory.builder().stockQuantity(new StockQuantity(5)).build();
            setId(inv, "inventoryId", 11L);

            ProductOption po = ProductOption.create(product, new Money(1000), inv);
            setId(po, "productOptionId", 33L);

            given(brandMemberRepository.existsByBrand_BrandIdAndUserId(eq(1L), anyLong())).willReturn(true);
            given(productOptionRepository.findByIdWithProductAndInventory(eq(33L))).willReturn(Optional.of(po));
            given(inventoryRepository.findByProductOptionIdWithLock(eq(33L))).willReturn(Optional.of(inv));

            StockAdjustmentRequest req = StockAdjustmentRequest.builder().productOptionId(33L).quantity(4).build();

            // When: addStock을 호출하면
            ProductOptionStockResponse resp = service.addStock(1L, 2L, req, 999L);

            // Then: DTO에 증가된 재고가 반영된다
            assertThat(resp).isNotNull();
            assertThat(resp.getStockQuantity()).isEqualTo(9);
        }

        @Test
        @DisplayName("subtractStock 호출 시 정상 차감되고 DTO가 반환된다")
        void subtractStock_success() {
            // Given: 브랜드, 상품, 옵션, 재고를 구성한다
            Brand brand = Brand.create("b", "b", BigDecimal.ONE);
            setId(brand, "brandId", 1L);

            Product product = Product.builder().brand(brand).productName("p").productInfo("i").productGenderType(ProductGenderType.ALL).brandName("b").categoryPath("c").isAvailable(true).build();
            setId(product, "productId", 2L);

            Inventory inv = Inventory.builder().stockQuantity(new StockQuantity(10)).build();
            setId(inv, "inventoryId", 12L);

            ProductOption po = ProductOption.create(product, new Money(2000), inv);
            setId(po, "productOptionId", 44L);

            given(brandMemberRepository.existsByBrand_BrandIdAndUserId(eq(1L), anyLong())).willReturn(true);
            given(productOptionRepository.findByIdWithProductAndInventory(eq(44L))).willReturn(Optional.of(po));
            given(inventoryRepository.findByProductOptionIdWithLock(eq(44L))).willReturn(Optional.of(inv));

            StockAdjustmentRequest req = StockAdjustmentRequest.builder().productOptionId(44L).quantity(3).build();

            // When: subtractStock을 호출하면
            ProductOptionStockResponse resp = service.subtractStock(1L, 2L, req, 999L);

            // Then: DTO에 차감된 재고가 반영된다
            assertThat(resp.getStockQuantity()).isEqualTo(7);
        }

        @Test
        @DisplayName("subtractStock에서 재고 부족 시 BusinessException을 던진다")
        void subtractStock_insufficient_shouldThrow() {
            // Given: 브랜드, 상품, 옵션, 재고를 구성한다 (재고 부족)
            Brand brand = Brand.create("b", "b", BigDecimal.ONE);
            setId(brand, "brandId", 1L);

            Product product = Product.builder().brand(brand).productName("p").productInfo("i").productGenderType(ProductGenderType.ALL).brandName("b").categoryPath("c").isAvailable(true).build();
            setId(product, "productId", 2L);

            Inventory inv = Inventory.builder().stockQuantity(new StockQuantity(1)).build();
            setId(inv, "inventoryId", 13L);

            ProductOption po = ProductOption.create(product, new Money(2000), inv);
            setId(po, "productOptionId", 55L);

            given(brandMemberRepository.existsByBrand_BrandIdAndUserId(eq(1L), anyLong())).willReturn(true);
            given(productOptionRepository.findByIdWithProductAndInventory(eq(55L))).willReturn(Optional.of(po));
            given(inventoryRepository.findByProductOptionIdWithLock(eq(55L))).willReturn(Optional.of(inv));

            StockAdjustmentRequest req = StockAdjustmentRequest.builder().productOptionId(55L).quantity(5).build();

            // When / Then: 차감 시 재고 부족으로 BusinessException 발생
            assertThatThrownBy(() -> service.subtractStock(1L, 2L, req, 999L))
                .isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    @DisplayName("상품 옵션 재고 조회 및 판매 상태 변경")
    class QueryAndAvailabilityTests {

        @Test
        @DisplayName("getProductOptionStocks는 매핑된 재고 목록을 반환한다")
        void getProductOptionStocks_success() {
            // Given: 브랜드, 상품과 옵션·재고를 구성한다
            Brand brand = Brand.create("b", "b", BigDecimal.ONE);
            setId(brand, "brandId", 5L);

            Product product = Product.builder().brand(brand).productName("p").productInfo("i").productGenderType(ProductGenderType.ALL).brandName("b").categoryPath("c").isAvailable(true).build();
            setId(product, "productId", 6L);

            Inventory inv = Inventory.builder().stockQuantity(new StockQuantity(2)).build();
            ProductOption po = ProductOption.create(product, new Money(1000), inv);
            setId(po, "productOptionId", 77L);
            product.addProductOption(po);

            given(brandMemberRepository.existsByBrand_BrandIdAndUserId(eq(5L), anyLong())).willReturn(true);
            given(productRepository.findDetailByIdForManager(eq(6L), eq(5L))).willReturn(Optional.of(product));

            // When: getProductOptionStocks 호출
            var list = service.getProductOptionStocks(5L, 6L, 999L);

            // Then: 매핑된 재고 목록이 반환된다
            assertThat(list).hasSize(1);
            assertThat(list.get(0).getProductOptionId()).isEqualTo(77L);
        }

        @Test
        @DisplayName("updateProductAvailability: null 요청이면 예외가 발생한다")
        void updateAvailability_nullRequest_shouldThrow() {
            // Given: 브랜드와 상품을 구성하고 null 요청을 준비한다
            Brand brand = Brand.create("b", "b", BigDecimal.ONE);
            setId(brand, "brandId", 7L);

            Product product = Product.builder().brand(brand).productName("p").productInfo("i").productGenderType(ProductGenderType.ALL).brandName("b").categoryPath("c").isAvailable(true).build();
            setId(product, "productId", 8L);

            given(brandMemberRepository.existsByBrand_BrandIdAndUserId(eq(7L), anyLong())).willReturn(true);
            given(productRepository.findDetailByIdForManager(eq(8L), eq(7L))).willReturn(Optional.of(product));

            ProductAvailabilityRequest req = ProductAvailabilityRequest.builder().isAvailable(null).build();

            // When / Then: null 상태로 요청 시 BusinessException 발생
            assertThatThrownBy(() -> service.updateProductAvailability(7L, 8L, req, 999L))
                .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("updateProductAvailability: 동일 상태로 요청하면 예외가 발생한다")
        void updateAvailability_sameValue_shouldThrow() {
            // Given: 브랜드와 상품을 구성하고 동일한 상태로 요청을 준비한다
            Brand brand = Brand.create("b", "b", BigDecimal.ONE);
            setId(brand, "brandId", 7L);

            Product product = Product.builder().brand(brand).productName("p").productInfo("i").productGenderType(ProductGenderType.ALL).brandName("b").categoryPath("c").isAvailable(true).build();
            setId(product, "productId", 8L);

            given(brandMemberRepository.existsByBrand_BrandIdAndUserId(eq(7L), anyLong())).willReturn(true);
            given(productRepository.findDetailByIdForManager(eq(8L), eq(7L))).willReturn(Optional.of(product));

            ProductAvailabilityRequest req = ProductAvailabilityRequest.builder().isAvailable(true).build();

            // When / Then: 동일 상태로 요청 시 BusinessException 발생
            assertThatThrownBy(() -> service.updateProductAvailability(7L, 8L, req, 999L))
                .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("updateProductAvailability 성공 케이스")
        void updateAvailability_success() {
            // Given: 브랜드와 상품을 구성하고 다른 상태로 변경 요청을 준비한다
            Brand brand = Brand.create("b", "b", BigDecimal.ONE);
            setId(brand, "brandId", 7L);

            Product product = Product.builder().brand(brand).productName("p").productInfo("i").productGenderType(ProductGenderType.ALL).brandName("b").categoryPath("c").isAvailable(true).build();
            setId(product, "productId", 8L);

            given(brandMemberRepository.existsByBrand_BrandIdAndUserId(eq(7L), anyLong())).willReturn(true);
            given(productRepository.findDetailByIdForManager(eq(8L), eq(7L))).willReturn(Optional.of(product));

            ProductAvailabilityRequest req = ProductAvailabilityRequest.builder().isAvailable(false).build();

            // When: updateProductAvailability 호출
            var resp = service.updateProductAvailability(7L, 8L, req, 999L);

            // Then: 변경된 상태가 응답에 반영된다
            assertThat(resp.getProductId()).isEqualTo(8L);
            assertThat(resp.getIsAvailable()).isFalse();
        }
    }

}
