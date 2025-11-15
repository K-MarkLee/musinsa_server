package com.mudosa.musinsa.product.application.dto;

import com.mudosa.musinsa.product.domain.model.ProductGenderType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 브랜드 매니저용 상품 응답 DTO
 * isAvailable = false인 상품도 포함하여 조회
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductManagerResponse {

    private Long productId;
    private String productName;
    private String productInfo;
    private Boolean isAvailable;
    private String brandName;
    private String categoryPath;
    private ProductGenderType productGenderType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ImageInfo> images;
    private List<OptionInfo> options;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImageInfo {
        private Long imageId;
        private String imageUrl;
        private Boolean isThumbnail;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionInfo {
        private Long optionId;
    private java.math.BigDecimal price;
        private Integer stockQuantity;
        private List<String> optionValues;
    }

}