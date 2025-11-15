package com.mudosa.musinsa.product.application.dto;

import java.math.BigDecimal;
import com.mudosa.musinsa.product.domain.model.ProductGenderType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

// 상품 등록 요청 정보를 담아 서비스 계층으로 전달하는 DTO이다.
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductCreateRequest {

    @NotBlank(message = "상품명은 필수입니다.")
    private String productName;

    @NotBlank(message = "상품 정보는 필수입니다.")
    private String productInfo;

    @NotNull(message = "상품 성별 타입은 필수입니다.")
    private ProductGenderType productGenderType;

    @NotBlank(message = "카테고리 경로는 필수입니다.")
    private String categoryPath;

    private Boolean isAvailable;

    @Size(min = 1, message = "상품 이미지는 최소 1장 이상 등록해야 합니다.")
    @Valid
    private List<ImageCreateRequest> images;

    @Size(min = 1, message = "상품 옵션은 최소 1개 이상 등록해야 합니다.")
    @Valid
    private List<OptionCreateRequest> options;

    // 상품 이미지 등록에 필요한 데이터를 담는 내부 DTO이다.
    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    public static class ImageCreateRequest {

        @NotBlank(message = "이미지 URL은 필수입니다.")
        private String imageUrl;

        private Boolean isThumbnail;
    }

    // 상품 옵션 가격과 재고, 옵션 값을 전달하는 내부 DTO이다.
    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    public static class OptionCreateRequest {

        @NotNull(message = "옵션 가격은 필수입니다.")
    private BigDecimal productPrice;

        @NotNull(message = "재고 수량은 필수입니다.")
        private Integer stockQuantity;

        @NotEmpty(message = "옵션 값 ID는 최소 1개 이상이어야 합니다.")
        private List<Long> optionValueIds;
    }
}
