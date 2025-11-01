package com.mudosa.musinsa.product.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 상품 정보를 수정하기 위한 요청 DTO.
 * 추후 정책 확정 시 필드 제약을 조정할 수 있도록 기본 구조만 정의한다.
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductUpdateRequest {

    @NotBlank(message = "상품명은 필수입니다.")
    private String productName;

    @NotBlank(message = "상품 정보는 필수입니다.")
    private String productInfo;

    @NotBlank(message = "상품 성별 타입은 필수입니다.")
    private String productGenderType;

    private Boolean isAvailable;

    private String brandName;

    @Size(min = 1, message = "상품 이미지는 최소 1장 이상이어야 합니다.")
    @Valid
    private List<ImageUpdateRequest> images;

    // 옵션 수정 정책은 추후 확정. 구조만 마련해둔다.
    @Valid
    private List<OptionUpdateRequest> options;

    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    public static class ImageUpdateRequest {
        @NotBlank(message = "이미지 URL은 필수입니다.")
        private String imageUrl;
        private Boolean isThumbnail;
    }

    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    public static class OptionUpdateRequest {
        private Long productOptionId;
        private BigDecimal productPrice;
        private Integer stockQuantity;
        private List<Long> optionValueIds;
    }

    @AssertTrue(message = "상품 이미지는 썸네일 1개를 포함해야 합니다.")
    public boolean isValidThumbnailConfiguration() {
        if (images == null || images.isEmpty()) {
            return false;
        }
        long thumbnailCount = images.stream()
            .filter(image -> Boolean.TRUE.equals(image.getIsThumbnail()))
            .count();
        return thumbnailCount == 1;
    }
}
