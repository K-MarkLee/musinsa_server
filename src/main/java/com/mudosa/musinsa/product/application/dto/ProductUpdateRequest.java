package com.mudosa.musinsa.product.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    private String productName;

    private String productInfo;

    private Boolean isAvailable;

    @Valid
    private List<ImageUpdateRequest> images;

    // 옵션 수정 정책은 추후 확정. 구조만 마련해둔다.
    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    public static class ImageUpdateRequest {
        @NotBlank(message = "이미지 URL은 필수입니다.")
        private String imageUrl;
        private Boolean isThumbnail;
    }

    @AssertTrue(message = "상품 이미지를 수정할 경우 썸네일 1개를 포함해야 합니다.")
    public boolean isValidThumbnailConfiguration() {
        if (images == null || images.isEmpty()) {
            return true;
        }
        long thumbnailCount = images.stream()
            .filter(image -> Boolean.TRUE.equals(image.getIsThumbnail()))
            .count();
        return thumbnailCount == 1;
    }

    @AssertTrue(message = "상품명은 비어 있을 수 없습니다.")
    public boolean isProductNameValid() {
        return productName == null || !productName.trim().isEmpty();
    }

    @AssertTrue(message = "상품 정보는 비어 있을 수 없습니다.")
    public boolean isProductInfoValid() {
        return productInfo == null || !productInfo.trim().isEmpty();
    }

    public boolean hasUpdatableField() {
        return productName != null
            || productInfo != null
            || isAvailable != null
            || (images != null);
    }
}
