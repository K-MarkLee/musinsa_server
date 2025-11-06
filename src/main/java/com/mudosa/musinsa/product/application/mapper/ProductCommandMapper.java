package com.mudosa.musinsa.product.application.mapper;

import com.mudosa.musinsa.brand.domain.model.Brand;
import com.mudosa.musinsa.product.application.ProductCommandService.ProductCreateCommand;
import com.mudosa.musinsa.product.application.dto.ProductCreateRequest;
import com.mudosa.musinsa.product.application.dto.ProductDetailResponse;
import com.mudosa.musinsa.product.domain.model.Category;
import com.mudosa.musinsa.product.domain.model.OptionValue;
import com.mudosa.musinsa.product.domain.model.Product;
import com.mudosa.musinsa.product.domain.model.ProductGenderType;
import com.mudosa.musinsa.product.domain.model.ProductOption;
import com.mudosa.musinsa.product.domain.model.ProductOptionValue;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 상품 생성/수정 커맨드 흐름에서 사용하는 매핑 유틸리티.
 */
public final class ProductCommandMapper {

    private ProductCommandMapper() {
    }

    public static ProductCreateCommand toCreateCommand(ProductCreateRequest request,
                                                       Brand brand,
                                                       Category category,
                                                       ProductGenderType genderType) {
        List<ProductCreateCommand.ImageSpec> imageSpecs = request.getImages() == null
            ? Collections.emptyList()
            : request.getImages().stream()
                .map(image -> new ProductCreateCommand.ImageSpec(
                    image.getImageUrl(),
                    Boolean.TRUE.equals(image.getIsThumbnail())))
                .collect(Collectors.toList());

        List<ProductCreateCommand.OptionSpec> optionSpecs = request.getOptions() == null
            ? Collections.emptyList()
            : request.getOptions().stream()
                .map(option -> new ProductCreateCommand.OptionSpec(
                    option.getProductPrice(),
                    option.getStockQuantity(),
                    option.getOptionValueIds()))
                .collect(Collectors.toList());

        return ProductCreateCommand.builder()
            .brand(brand)
            .productName(request.getProductName())
            .productInfo(request.getProductInfo())
            .productGenderType(genderType)
            .brandName(brand != null ? brand.getNameKo() : request.getBrandName())
            .categoryPath(category != null ? category.buildPath() : request.getCategoryPath())
            .isAvailable(request.getIsAvailable())
            .images(imageSpecs)
            .options(optionSpecs)
            .build();
    }

    public static List<Product.ImageRegistration> toImageRegistrations(List<ProductCreateCommand.ImageSpec> specs) {
        if (specs == null || specs.isEmpty()) {
            return Collections.emptyList();
        }
        return specs.stream()
            .map(spec -> new Product.ImageRegistration(spec.imageUrl(), spec.isThumbnail()))
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
