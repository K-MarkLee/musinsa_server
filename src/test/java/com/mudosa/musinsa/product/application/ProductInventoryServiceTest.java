package com.mudosa.musinsa.product.application;

import com.mudosa.musinsa.brand.domain.model.Brand;
import com.mudosa.musinsa.brand.domain.model.BrandStatus;
import com.mudosa.musinsa.common.vo.Money;
import com.mudosa.musinsa.exception.BusinessException;
import com.mudosa.musinsa.exception.ErrorCode;
import com.mudosa.musinsa.product.application.dto.ProductAvailabilityRequest;
import com.mudosa.musinsa.product.application.dto.ProductOptionStockResponse;
import com.mudosa.musinsa.product.application.dto.StockAdjustmentRequest;
import com.mudosa.musinsa.product.domain.model.Inventory;
import com.mudosa.musinsa.product.domain.model.OptionName;
import com.mudosa.musinsa.product.domain.model.OptionValue;
import com.mudosa.musinsa.product.domain.model.Product;
import com.mudosa.musinsa.product.domain.model.ProductGenderType;
import com.mudosa.musinsa.product.domain.model.ProductOption;
import com.mudosa.musinsa.product.domain.model.ProductOptionValue;
import com.mudosa.musinsa.product.domain.repository.ProductOptionRepository;
import com.mudosa.musinsa.product.domain.repository.ProductRepository;
import com.mudosa.musinsa.product.domain.vo.StockQuantity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductInventoryServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductOptionRepository productOptionRepository;

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private ProductInventoryService productInventoryService;

    @Test
    @DisplayName("브랜드가 소유한 상품의 옵션 재고가 요약 정보로 변환된다")
    void getProductOptionStocks_returnsSummary() {
        Brand brand = Brand.builder()
            .brandId(1L)
            .nameKo("브랜드")
            .nameEn("BRAND")
            .status(BrandStatus.ACTIVE)
            .commissionRate(BigDecimal.TEN)
            .build();

        Product product = Product.builder()
            .brand(brand)
            .productName("티셔츠")
            .productInfo("상세 설명")
            .productGenderType(ProductGenderType.ALL)
            .brandName("브랜드")
            .categoryPath("상의/티셔츠")
            .isAvailable(true)
            .build();
        ReflectionTestUtils.setField(product, "productId", 100L);

        Inventory inventory = Inventory.builder()
            .stockQuantity(new StockQuantity(5))
            .build();

        ProductOption productOption = ProductOption.builder()
            .product(product)
            .productPrice(new Money(new BigDecimal("19900")))
            .inventory(inventory)
            .build();
        ReflectionTestUtils.setField(productOption, "productOptionId", 200L);

        OptionName optionName = OptionName.builder()
            .optionName("사이즈")
            .build();
        ReflectionTestUtils.setField(optionName, "optionNameId", 300L);

        OptionValue optionValue = OptionValue.builder()
            .optionName(optionName)
            .optionValue("M")
            .build();
        ReflectionTestUtils.setField(optionValue, "optionValueId", 400L);

        ProductOptionValue mapping = ProductOptionValue.builder()
            .productOption(productOption)
            .optionValue(optionValue)
            .build();
        productOption.addOptionValue(mapping);
        product.addProductOption(productOption);

        when(productRepository.findDetailById(100L)).thenReturn(Optional.of(product));

        List<ProductOptionStockResponse> responses = productInventoryService.getProductOptionStocks(1L, 100L);

        assertThat(responses).hasSize(1);
        ProductOptionStockResponse summary = responses.get(0);
        assertThat(summary.getProductOptionId()).isEqualTo(200L);
        assertThat(summary.getProductName()).isEqualTo("티셔츠");
    assertThat(summary.getProductPrice()).isEqualByComparingTo("19900");
        assertThat(summary.getStockQuantity()).isEqualTo(5);
        assertThat(summary.getHasStock()).isTrue();
        assertThat(summary.getOptionValues())
            .hasSize(1)
            .extracting(ProductOptionStockResponse.OptionValueSummary::getOptionValue)
            .containsExactly("M");
    }

    @Test
    @DisplayName("다른 브랜드가 접근하면 재고 조회가 거부된다")
    void getProductOptionStocks_brandMismatch_throwsForbidden() {
        Brand brand = Brand.builder()
            .brandId(1L)
            .nameKo("브랜드")
            .nameEn("BRAND")
            .status(BrandStatus.ACTIVE)
            .commissionRate(BigDecimal.TEN)
            .build();

        Product product = Product.builder()
            .brand(brand)
            .productName("티셔츠")
            .productInfo("상세 설명")
            .productGenderType(ProductGenderType.ALL)
            .brandName("브랜드")
            .categoryPath("상의/티셔츠")
            .isAvailable(true)
            .build();
        ReflectionTestUtils.setField(product, "productId", 100L);

        when(productRepository.findDetailById(100L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> productInventoryService.getProductOptionStocks(2L, 100L))
            .isInstanceOf(BusinessException.class)
            .extracting(ex -> ((BusinessException) ex).getErrorCode())
            .isEqualTo(ErrorCode.FORBIDDEN);
    }

    @Test
    @DisplayName("재고 추가 요청은 InventoryService에 위임된다")
    void addStock_delegatesToInventoryService() {
        Product product = prepareProductWithOption();
        ProductOption productOption = product.getProductOptions().get(0);

        when(productOptionRepository.findById(200L)).thenReturn(Optional.of(productOption));

        StockAdjustmentRequest request = StockAdjustmentRequest.builder()
            .productOptionId(200L)
            .quantity(3)
            .build();

        productInventoryService.addStock(1L, 100L, request);

        verify(inventoryService, times(1)).addStock(200L, 3);
    }

    @Test
    @DisplayName("재고 차감 요청은 InventoryService에 위임된다")
    void subtractStock_delegatesToInventoryService() {
        Product product = prepareProductWithOption();
        ProductOption productOption = product.getProductOptions().get(0);

        when(productOptionRepository.findById(200L)).thenReturn(Optional.of(productOption));

        StockAdjustmentRequest request = StockAdjustmentRequest.builder()
            .productOptionId(200L)
            .quantity(2)
            .build();

        productInventoryService.subtractStock(1L, 100L, request);

        verify(inventoryService, times(1)).subtractStock(200L, 2);
    }

    @Test
    @DisplayName("재고 조정 시 다른 브랜드가 접근하면 예외가 발생한다")
    void adjustStock_brandMismatch_throwsForbidden() {
        Product product = prepareProductWithOption();
        ProductOption productOption = product.getProductOptions().get(0);

        when(productOptionRepository.findById(200L)).thenReturn(Optional.of(productOption));

        StockAdjustmentRequest request = StockAdjustmentRequest.builder()
            .productOptionId(200L)
            .quantity(1)
            .build();

        assertThatThrownBy(() -> productInventoryService.addStock(999L, 100L, request))
            .isInstanceOf(BusinessException.class)
            .extracting(ex -> ((BusinessException) ex).getErrorCode())
            .isEqualTo(ErrorCode.FORBIDDEN);

        assertThatThrownBy(() -> productInventoryService.subtractStock(999L, 100L, request))
            .isInstanceOf(BusinessException.class)
            .extracting(ex -> ((BusinessException) ex).getErrorCode())
            .isEqualTo(ErrorCode.FORBIDDEN);
    }

    @Test
    @DisplayName("상품 판매 가능 상태가 브랜드 권한 검증 후 변경된다")
    void updateProductAvailability_updatesFlag() {
        Brand brand = Brand.builder()
            .brandId(1L)
            .nameKo("브랜드")
            .nameEn("BRAND")
            .status(BrandStatus.ACTIVE)
            .commissionRate(BigDecimal.TEN)
            .build();

        Product product = Product.builder()
            .brand(brand)
            .productName("니트")
            .productInfo("상세")
            .productGenderType(ProductGenderType.ALL)
            .brandName("브랜드")
            .categoryPath("상의/니트")
            .isAvailable(true)
            .build();
        ReflectionTestUtils.setField(product, "productId", 100L);

        when(productRepository.findDetailById(100L)).thenReturn(Optional.of(product));

        ProductAvailabilityRequest request = ProductAvailabilityRequest.builder()
            .isAvailable(false)
            .build();

        productInventoryService.updateProductAvailability(1L, 100L, request);

        assertThat(product.getIsAvailable()).isFalse();
    }

    @Test
    @DisplayName("판매 가능 상태 변경 시 브랜드가 다르면 예외가 발생한다")
    void updateProductAvailability_brandMismatch_throwsForbidden() {
        Product product = prepareProductWithOption();
        ReflectionTestUtils.setField(product.getBrand(), "brandId", 1L);

        when(productRepository.findDetailById(100L)).thenReturn(Optional.of(product));

        ProductAvailabilityRequest request = ProductAvailabilityRequest.builder()
            .isAvailable(false)
            .build();

        assertThatThrownBy(() -> productInventoryService.updateProductAvailability(2L, 100L, request))
            .isInstanceOf(BusinessException.class)
            .extracting(ex -> ((BusinessException) ex).getErrorCode())
            .isEqualTo(ErrorCode.FORBIDDEN);
    }

    private Product prepareProductWithOption() {
        Brand brand = Brand.builder()
            .brandId(1L)
            .nameKo("브랜드")
            .nameEn("BRAND")
            .status(BrandStatus.ACTIVE)
            .commissionRate(BigDecimal.TEN)
            .build();

        Product product = Product.builder()
            .brand(brand)
            .productName("패딩")
            .productInfo("설명")
            .productGenderType(ProductGenderType.ALL)
            .brandName("브랜드")
            .categoryPath("아우터/패딩")
            .isAvailable(true)
            .build();
        ReflectionTestUtils.setField(product, "productId", 100L);

        Inventory inventory = Inventory.builder()
            .stockQuantity(new StockQuantity(10))
            .build();

        ProductOption option = ProductOption.builder()
            .product(product)
            .productPrice(new Money(new BigDecimal("29900")))
            .inventory(inventory)
            .build();
        ReflectionTestUtils.setField(option, "productOptionId", 200L);

        product.addProductOption(option);
        return product;
    }
}
