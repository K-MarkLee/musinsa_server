package com.mudosa.musinsa.product.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 재고 판매 가능 상태를 변경하기 위한 요청 DTO이다.
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class StockAvailabilityRequest {

    @NotNull(message = "상품 옵션 ID는 필수입니다.")
    private Long productOptionId;

    @NotNull(message = "판매 가능 여부는 필수입니다.")
    private Boolean isAvailable;
}
