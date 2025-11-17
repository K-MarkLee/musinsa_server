package com.mudosa.musinsa.product.application.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

import java.math.BigDecimal;

// 상품 옵션 단건 등록 요청을 담는 DTO이다.
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductOptionCreateRequest {

    @NotNull(message = "옵션 가격은 필수입니다.")
    private BigDecimal productPrice;

    @NotNull(message = "재고 수량은 필수입니다.")
    private Integer stockQuantity;

    @NotEmpty(message = "옵션 값 ID는 최소 1개 이상이어야 합니다.")
    private List<Long> optionValueIds;
}
