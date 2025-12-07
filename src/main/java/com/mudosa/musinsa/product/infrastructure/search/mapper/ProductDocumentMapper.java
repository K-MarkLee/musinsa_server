package com.mudosa.musinsa.product.infrastructure.search.mapper;

import com.mudosa.musinsa.product.domain.model.Image;
import com.mudosa.musinsa.product.domain.model.OptionValue;
import com.mudosa.musinsa.product.domain.model.Product;
import com.mudosa.musinsa.product.domain.model.ProductOption;
import com.mudosa.musinsa.product.domain.model.ProductOptionValue;
import com.mudosa.musinsa.product.infrastructure.search.document.ProductDocument;
import com.mudosa.musinsa.product.infrastructure.search.dto.ProductIndexDto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 도메인 엔티티를 검색 도큐먼트로 변환하는 매퍼.
 * DB 조회 결과(상품 + 옵션 + 옵션값 + 재고)를 ES 색인 모델로 풀어준다.
 */
public final class ProductDocumentMapper {

    private ProductDocumentMapper() {
    }

    /**
     * 단일 상품을 옵션 단위 검색 도큐먼트 목록으로 변환한다.
     */
    public static List<ProductDocument> toDocuments(Product product) {
        if (product == null || product.getProductOptions() == null) {
            return List.of();
        }

        String productName = product.getProductName();
        String krBrandName = product.getBrandName();
        Long brandId = product.getBrand() != null ? product.getBrand().getBrandId() : null;
        String enBrandName = product.getBrand() != null ? product.getBrand().getNameEn() : null;
        String categoryPath = product.getCategoryPath();
        String gender = product.getProductGenderType() != null ? product.getProductGenderType().name() : null;
        Boolean isAvailable = product.getIsAvailable();
        String thumbnailUrl = resolveThumbnail(product);

        return product.getProductOptions().stream()
            .filter(Objects::nonNull)
            .map(option -> toDocument(option, brandId, productName, krBrandName, enBrandName, categoryPath, gender, isAvailable, thumbnailUrl))
            .collect(Collectors.toList());
    }

    private static ProductDocument toDocument(ProductOption option,
                                              Long brandId,
                                              String productName,
                                              String krBrandName,
                                              String enBrandName,
                                              String categoryPath,
                                              String gender,
                                              Boolean isAvailable,
                                              String thumbnailUrl) {
        Long optionId = option.getProductOptionId();
        Long productId = option.getProduct() != null ? option.getProduct().getProductId() : null;

        Long price = toLongPrice(option.getProductPrice() != null ? option.getProductPrice().getAmount() : null);
        if (price == null && option.getProduct() != null) {
            price = toLongPrice(option.getProduct().getDefaultPrice());
        }

        Boolean hasStock = option.getInventory() != null && option.getInventory().getStockQuantity() != null
            ? option.getInventory().getStockQuantity().getValue() != 0
            : null;

        Set<String> colorOptions = new LinkedHashSet<>();
        Set<String> sizeOptions = new LinkedHashSet<>();
        if (option.getProductOptionValues() != null) {
            option.getProductOptionValues().forEach(mapping -> classifyOptionValue(mapping, colorOptions, sizeOptions));
        }

        return ProductDocument.builder()
            .productOptionId(optionId)
            .productId(productId)
            .brandId(brandId)
            .productName(productName)
            .krBrandName(krBrandName)
            .enBrandName(enBrandName)
            .categoryPath(categoryPath)
            .colorOptions(new ArrayList<>(colorOptions))
            .sizeOptions(new ArrayList<>(sizeOptions))
            .defaultPrice(price)
            .thumbnailUrl(thumbnailUrl)
            .isAvailable(isAvailable)
            .hasStock(hasStock)
            .gender(gender)
            .build();
    }

    /**
     * 조회 DTO를 검색 도큐먼트로 변환한다.
     */
    public static ProductDocument toDocument(ProductIndexDto dto) {
        if (dto == null) {
            return null;
        }
        Long price = toLongPrice(dto.getDefaultPrice());

        return ProductDocument.builder()
            .productOptionId(dto.getProductOptionId())
            .productId(dto.getProductId())
            .brandId(dto.getBrandId())
            .productName(dto.getProductName())
            .krBrandName(dto.getKrBrandName())
            .enBrandName(dto.getEnBrandName())
            .categoryPath(dto.getCategoryPath())
            .colorOptions(new ArrayList<>(dto.getColorOptions()))
            .sizeOptions(new ArrayList<>(dto.getSizeOptions()))
            .defaultPrice(price)
            .thumbnailUrl(dto.getThumbnailUrl())
            .isAvailable(dto.getIsAvailable())
            .hasStock(dto.getHasStock())
            .gender(dto.getGender())
            .build();
    }

    private static void classifyOptionValue(ProductOptionValue mapping,
                                            Set<String> colorOptions,
                                            Set<String> sizeOptions) {
        if (mapping == null || mapping.getOptionValue() == null) {
            return;
        }
        OptionValue optionValue = mapping.getOptionValue();
        Long optionValueId = optionValue.getOptionValueId();
        String value = optionValue.getOptionValue();
        if (value == null) {
            return;
        }
        // 도메인 규칙: option_value_id 1~8 → 사이즈, 9~60 → 색상
        if (optionValueId != null) {
            if (optionValueId >= 1 && optionValueId <= 8) {
                sizeOptions.add(value);
                return;
            }
            if (optionValueId >= 9 && optionValueId <= 60) {
                colorOptions.add(value);
                return;
            }
        }
        // 범위 밖이거나 ID 없으면 옵션명 힌트로 분류
        String optionName = optionValue.getOptionName();
        String normalizedName = optionName != null ? optionName.trim().toLowerCase() : "";
        if (normalizedName.contains("색상") || normalizedName.contains("컬러") || normalizedName.contains("color")) {
            colorOptions.add(value);
        } else if (normalizedName.contains("사이즈") || normalizedName.contains("size")) {
            sizeOptions.add(value);
        }
    }

    private static String resolveThumbnail(Product product) {
        if (product == null) {
            return null;
        }
        if (product.getThumbnailImage() != null) {
            return product.getThumbnailImage();
        }
        if (product.getImages() != null) {
            return product.getImages().stream()
                .filter(Objects::nonNull)
                .filter(image -> Boolean.TRUE.equals(image.getIsThumbnail()))
                .map(Image::getImageUrl)
                .findFirst()
                .orElse(null);
        }
        return null;
    }

    private static Long toLongPrice(BigDecimal price) {
        if (price == null) {
            return null;
        }
        return price.longValue();
    }
}
