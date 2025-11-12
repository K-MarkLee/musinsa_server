package com.mudosa.musinsa.product.application.mapper;

import com.mudosa.musinsa.product.application.dto.ProductCreateRequest;
import com.mudosa.musinsa.product.application.dto.ProductDetailResponse;
import com.mudosa.musinsa.product.domain.model.OptionValue;
import com.mudosa.musinsa.product.domain.model.Product;
import com.mudosa.musinsa.product.domain.model.ProductOption;
import com.mudosa.musinsa.product.domain.model.ProductOptionValue;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 상품 생성/수정 커맨드 흐름에서 사용하는 매핑 유틸리티.
 */
public final class ProductCommandMapper {

    public static List<Product.ImageRegistration> toImageRegistrations(List<ProductCreateRequest.ImageCreateRequest> specs) {
        if (specs == null || specs.isEmpty()) {
            return Collections.emptyList();
        }
        return specs.stream()
            .map(spec -> new Product.ImageRegistration(spec.getImageUrl(), Boolean.TRUE.equals(spec.getIsThumbnail())))
            .collect(Collectors.toList());
    }

    public static ProductDetailResponse toProductDetail(Product product) {
        List<ProductDetailResponse.ImageResponse> imageResponses = product.getImages().stream()
            .map(image -> ProductDetailResponse.ImageResponse.builder()
                .imageId(image.getImageId())
                .imageUrl(image.getImageUrl())
                .isThumbnail(Boolean.TRUE.equals(image.getIsThumbnail()))
                .build())
            .collect(Collectors.toList());

        List<ProductDetailResponse.OptionDetail> optionDetails = product.getProductOptions().stream()
            .map(ProductCommandMapper::toOptionDetail)
            .collect(Collectors.toList());

        return ProductDetailResponse.builder()
            .productId(product.getProductId())
            .brandId(product.getBrand() != null ? product.getBrand().getBrandId() : null)
            .brandName(product.getBrandName())
            .productName(product.getProductName())
            .productInfo(product.getProductInfo())
            .productGenderType(product.getProductGenderType() != null
                ? product.getProductGenderType().name()
                : null)
            .isAvailable(product.getIsAvailable())
            .categoryPath(product.getCategoryPath())
            .images(imageResponses)
            .options(optionDetails)
            .build();
    }

    public static ProductDetailResponse.OptionDetail toOptionDetail(ProductOption option) {
        List<ProductDetailResponse.OptionDetail.OptionValueDetail> optionValueDetails = option.getProductOptionValues().stream()
            .map(ProductCommandMapper::toOptionValueDetail)
            .collect(Collectors.toList());

        Integer stockQuantity = null;
        Boolean hasStock = null;
        if (option.getInventory() != null && option.getInventory().getStockQuantity() != null) {
            stockQuantity = option.getInventory().getStockQuantity().getValue();
            hasStock = stockQuantity > 0;
        }

        return ProductDetailResponse.OptionDetail.builder()
            .optionId(option.getProductOptionId())
            .productPrice(option.getProductPrice() != null ? option.getProductPrice().getAmount() : null)
            .stockQuantity(stockQuantity)
            .hasStock(hasStock)
            .optionValues(optionValueDetails)
            .build();
    }

    private static ProductDetailResponse.OptionDetail.OptionValueDetail toOptionValueDetail(ProductOptionValue mapping) {
        OptionValue optionValue = mapping.getOptionValue();
        return ProductDetailResponse.OptionDetail.OptionValueDetail.builder()
            .optionValueId(optionValue != null ? optionValue.getOptionValueId() : null)
            .optionNameId(null)
            .optionName(optionValue != null ? optionValue.getOptionName() : null)
            .optionValue(optionValue != null ? optionValue.getOptionValue() : null)
            .build();
    }
}
