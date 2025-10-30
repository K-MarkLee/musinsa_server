package com.mudosa.musinsa.product.application;

import com.mudosa.musinsa.exception.BusinessException;
import com.mudosa.musinsa.exception.ErrorCode;
import com.mudosa.musinsa.product.application.dto.ProductOptionStockResponse;
import com.mudosa.musinsa.product.application.dto.ProductAvailabilityRequest;
import com.mudosa.musinsa.product.application.dto.StockAdjustmentRequest;
import com.mudosa.musinsa.product.application.dto.StockAvailabilityRequest;
import com.mudosa.musinsa.product.application.dto.StockOverrideRequest;
import com.mudosa.musinsa.product.domain.model.Inventory;
import com.mudosa.musinsa.product.domain.model.OptionName;
import com.mudosa.musinsa.product.domain.model.OptionValue;
import com.mudosa.musinsa.product.domain.model.Product;
import com.mudosa.musinsa.product.domain.model.ProductOption;
import com.mudosa.musinsa.product.domain.model.ProductOptionValue;
import com.mudosa.musinsa.product.domain.repository.ProductOptionRepository;
import com.mudosa.musinsa.product.domain.repository.ProductRepository;
import com.mudosa.musinsa.brand.domain.repository.BrandMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

// 브랜드 관리자가 상품 옵션 재고를 조회하고 조정하는 애플리케이션 서비스이다.
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductInventoryService {

    private final ProductRepository productRepository;
    private final ProductOptionRepository productOptionRepository;
    private final InventoryService inventoryService;
    private final BrandMemberRepository brandMemberRepository;

    // 브랜드와 상품을 기준으로 옵션 재고 목록을 조회한다.
    public List<ProductOptionStockResponse> getProductOptionStocks(Long brandId,
                                                                  Long productId,
                                                                  Long userId) {
        Product product = productRepository.findDetailById(productId)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND,
                "상품을 찾을 수 없습니다. productId=" + productId));

        validateBrandPrivileges(product, brandId, userId);

        return product.getProductOptions().stream()
            .map(this::mapToStockResponse)
            .collect(Collectors.toList());
    }

    // 옵션 재고를 증가시킨다.
    @Transactional
    public void addStock(Long brandId,
                         Long productId,
                         Long userId,
                         StockAdjustmentRequest request) {
        ProductOption productOption = loadProductOptionForBrand(brandId, productId, userId, request.getProductOptionId());

        inventoryService.addStock(productOption.getProductOptionId(), request.getQuantity());
    }

    // 옵션 재고 수량을 직접 덮어쓴다.
    @Transactional
    public void overrideStock(Long brandId,
                              Long productId,
                              Long userId,
                              StockOverrideRequest request) {
        ProductOption productOption = loadProductOptionForBrand(brandId, productId, userId, request.getProductOptionId());
        inventoryService.overrideStock(productOption.getProductOptionId(), request.getQuantity());
    }

    // 옵션 판매 가능 상태를 변경한다.
    @Transactional
    public void updateInventoryAvailability(Long brandId,
                                            Long productId,
                                            Long userId,
                                            StockAvailabilityRequest request) {
        ProductOption productOption = loadProductOptionForBrand(brandId, productId, userId, request.getProductOptionId());
        inventoryService.changeAvailability(productOption.getProductOptionId(), request.getIsAvailable());
    }

    // 상품 전체 판매 가능 상태를 변경한다.
    @Transactional
    public void updateProductAvailability(Long brandId,
                                          Long productId,
                                          Long userId,
                                          ProductAvailabilityRequest request) {
        Product product = productRepository.findDetailById(productId)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND,
                "상품을 찾을 수 없습니다. productId=" + productId));

        validateBrandPrivileges(product, brandId, userId);

        product.changeAvailability(request.getIsAvailable());
    }

    private ProductOption loadProductOptionForBrand(Long brandId,
                                                   Long productId,
                                                   Long userId,
                                                   Long productOptionId) {
        ProductOption productOption = productOptionRepository.findById(productOptionId)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND,
                "상품 옵션을 찾을 수 없습니다. productOptionId=" + productOptionId));

        Product product = productOption.getProduct();
        if (product == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND,
                "상품 정보를 찾을 수 없습니다. productOptionId=" + productOptionId);
        }

        if (!Objects.equals(product.getProductId(), productId)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR,
                "요청한 상품과 옵션이 일치하지 않습니다. productId=" + productId);
        }

        validateBrandPrivileges(product, brandId, userId);
        return productOption;
    }

    private void validateBrandPrivileges(Product product,
                                         Long brandId,
                                         Long userId) {
        if (product.getBrand() == null || !Objects.equals(product.getBrand().getBrandId(), brandId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN,
                "브랜드 권한이 없습니다. brandId=" + brandId);
        }

        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_USER);
        }

        boolean member = brandMemberRepository.existsByBrand_BrandIdAndUserId(brandId, userId);
        if (!member) {
            throw new BusinessException(ErrorCode.FORBIDDEN,
                "브랜드 멤버가 아닙니다. brandId=" + brandId);
        }

        // TODO 추후 인증 체계 도입 시 사용자 역할(OWNER, ADMIN 등)까지 확인해 세부 권한을 제어한다.
    }

    private ProductOptionStockResponse mapToStockResponse(ProductOption productOption) {
        Inventory inventory = productOption.getInventory();
        Integer stockQuantity = null;
        Boolean inventoryAvailable = null;
        if (inventory != null) {
            if (inventory.getStockQuantity() != null) {
                stockQuantity = inventory.getStockQuantity().getValue();
            }
            inventoryAvailable = inventory.getIsAvailable();
        }

        BigDecimal productPrice = productOption.getProductPrice() != null
            ? productOption.getProductPrice().getAmount()
            : null;

        List<ProductOptionStockResponse.OptionValueSummary> optionValueSummaries = productOption.getProductOptionValues().stream()
            .map(this::mapToOptionValueSummary)
            .collect(Collectors.toList());

        Product product = productOption.getProduct();
        String productName = product != null ? product.getProductName() : null;

        return ProductOptionStockResponse.builder()
            .productOptionId(productOption.getProductOptionId())
            .productName(productName)
            .productPrice(productPrice)
            .stockQuantity(stockQuantity)
            .inventoryAvailable(inventoryAvailable)
            .optionValues(optionValueSummaries)
            .build();
    }

    private ProductOptionStockResponse.OptionValueSummary mapToOptionValueSummary(ProductOptionValue mapping) {
        OptionValue optionValue = mapping.getOptionValue();
        OptionName optionName = optionValue != null ? optionValue.getOptionName() : null;
        return ProductOptionStockResponse.OptionValueSummary.builder()
            .optionValueId(optionValue != null ? optionValue.getOptionValueId() : null)
            .optionName(optionName != null ? optionName.getOptionName() : null)
            .optionValue(optionValue != null ? optionValue.getOptionValue() : null)
            .build();
    }
}
