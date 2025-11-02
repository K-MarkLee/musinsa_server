package com.mudosa.musinsa.product.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 재고 조정을 위한 관리자 요청 DTO이다.
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class StockAdjustmentRequest {

    @NotNull(message = "상품 옵션 ID는 필수입니다.")
    @Positive(message = "상품 옵션 ID는 0보다 커야 합니다.")
    private Long productOptionId;

    @NotNull(message = "재고 수량은 필수입니다.")
    @Positive(message = "재고 조정 수량은 0보다 커야 합니다.")
    private Integer quantity;
}
