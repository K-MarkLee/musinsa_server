package com.mudosa.musinsa.product.application;

import com.mudosa.musinsa.brand.domain.repository.BrandMemberRepository;
import com.mudosa.musinsa.common.vo.Money;
import com.mudosa.musinsa.exception.BusinessException;
import com.mudosa.musinsa.exception.ErrorCode;
import com.mudosa.musinsa.product.application.dto.ProductAvailabilityRequest;
import com.mudosa.musinsa.product.application.dto.ProductAvailabilityResponse;
import com.mudosa.musinsa.product.application.dto.ProductOptionStockResponse;
import com.mudosa.musinsa.product.application.dto.StockAdjustmentRequest;
import com.mudosa.musinsa.product.domain.model.Inventory;

import com.mudosa.musinsa.product.domain.model.OptionValue;
import com.mudosa.musinsa.product.domain.model.Product;
import com.mudosa.musinsa.product.domain.model.ProductOption;
import com.mudosa.musinsa.product.domain.model.ProductOptionValue;
import com.mudosa.musinsa.product.domain.repository.ProductOptionRepository;
import com.mudosa.musinsa.product.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        validateBrandMember(brandId, userId);
        
        Product product = productRepository.findDetailById(productId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "상품을 찾을 수 없습니다. productId=" + productId));

        validateBrandOwnership(product, brandId);

        return product.getProductOptions().stream()
            .map(this::mapToStockResponse)
            .collect(Collectors.toList());
    }

    // 옵션 재고를 증가시킨다.
    @Transactional
    public ProductOptionStockResponse addStock(Long brandId,
                                               Long productId,
                                               StockAdjustmentRequest request,
                                               Long userId) {
        return adjustStock(brandId, productId, request, userId, true);
    }

    // 옵션 재고를 감소시킨다.
    @Transactional
    public ProductOptionStockResponse subtractStock(Long brandId,
                                                    Long productId,
                                                    StockAdjustmentRequest request,
                                                    Long userId) {
        return adjustStock(brandId, productId, request, userId, false);
    }

    // 재고 조정 공통 메서드
    @Transactional
    private ProductOptionStockResponse adjustStock(Long brandId,
                                                    Long productId,
                                                    StockAdjustmentRequest request,
                                                    Long userId,
                                                    boolean isIncrease) {
        validateBrandMember(brandId, userId);
        ProductOption productOption = loadProductOptionForBrand(brandId, userId, productId, request.getProductOptionId());

        Inventory updatedInventory = isIncrease 
            ? inventoryService.addStock(productOption.getProductOptionId(), request.getQuantity())
            : inventoryService.subtractStock(productOption.getProductOptionId(), request.getQuantity());

        return mapToStockResponse(productOption, updatedInventory);
    }

    // 상품 판매 가능 상태를 변경한다.
    @Transactional
    public ProductAvailabilityResponse updateProductAvailability(Long brandId,
                                                                 Long productId,
                                                                 ProductAvailabilityRequest request,
                                                                 Long userId) {
        validateBrandMember(brandId, userId);
        
        Product product = productRepository.findDetailById(productId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "상품을 찾을 수 없습니다. productId=" + productId));

        validateBrandOwnership(product, brandId);

        Boolean requestedAvailability = request.getIsAvailable();
        if (requestedAvailability == null) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "판매 가능 여부는 필수입니다.");
        }

        if (Objects.equals(product.getIsAvailable(), requestedAvailability)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "이미 요청한 판매 상태와 동일합니다.");
        }

        product.changeAvailability(requestedAvailability);

        return ProductAvailabilityResponse.builder()
            .productId(product.getProductId())
            .isAvailable(product.getIsAvailable())
            .build();
    }

    // 브랜드의 상품의 옵션을 로드하고 유효성을 검증한다.
    private ProductOption loadProductOptionForBrand(Long brandId,
                                                   Long userId,
                                                   Long productId,
                                                   Long productOptionId) {
        ProductOption productOption = productOptionRepository.findByIdWithProductAndInventory(productOptionId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_OPTION_NOT_AVAILABLE, "상품 옵션을 찾을 수 없습니다. productOptionId=" + productOptionId));

        Product product = productOption.getProduct();
        if (product == null) {
            throw new BusinessException(ErrorCode.PRODUCT_OPTION_NOT_AVAILABLE, "상품 정보를 찾을 수 없습니다. productOptionId=" + productOptionId);
        }

        if (!Objects.equals(product.getProductId(), productId)) {
            throw new BusinessException(ErrorCode.PRODUCT_OPTION_NOT_AVAILABLE, "요청한 상품과 옵션이 일치하지 않습니다. productId=" + productId);
        }

        validateBrandOwnership(product, brandId);
        return productOption;
    }

    private void validateBrandMember(Long brandId,
                                    Long userId) {
        if (!brandMemberRepository.existsByBrand_BrandIdAndUserId(brandId, userId)) {
            throw new BusinessException(ErrorCode.BRAND_NOT_MATCHED, "브랜드 멤버 권한이 없습니다. brandId=" + brandId + ", userId=" + userId);
        }
    }

    private void validateBrandOwnership(Product product,
                                        Long brandId) {
        if (product.getBrand() == null || !Objects.equals(product.getBrand().getBrandId(), brandId)) {
            throw new BusinessException(ErrorCode.NOT_BRAND_PRODUCT);
        }
    }

    private ProductOptionStockResponse mapToStockResponse(ProductOption productOption) {
        return mapToStockResponse(productOption, productOption.getInventory());
    }

    private ProductOptionStockResponse mapToStockResponse(ProductOption productOption,
                                                          Inventory inventory) {
        Inventory effectiveInventory = inventory != null ? inventory : productOption.getInventory();
        Integer stockQuantity = null;
        boolean hasStock = false;
        
        if (effectiveInventory != null && effectiveInventory.getStockQuantity() != null) {
            stockQuantity = effectiveInventory.getStockQuantity().getValue();
            hasStock = stockQuantity > 0;
        }

        Money productPrice = productOption.getProductPrice();

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
            .hasStock(hasStock)
            .optionValues(optionValueSummaries)
            .build();
    }

    private ProductOptionStockResponse.OptionValueSummary mapToOptionValueSummary(ProductOptionValue productOptionValue) {
        OptionValue optionValue = productOptionValue.getOptionValue();
        return ProductOptionStockResponse.OptionValueSummary.builder()
            .optionValueId(optionValue != null ? optionValue.getOptionValueId() : null)
            .optionName(optionValue != null ? optionValue.getOptionName() : null)
            .optionValue(optionValue != null ? optionValue.getOptionValue() : null)
            .build();
    }
}
