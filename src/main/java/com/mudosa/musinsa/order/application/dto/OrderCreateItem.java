package com.mudosa.musinsa.order.application.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class OrderCreateItem {
    @NotNull(message = "상품 옵션 ID는 필수입니다")
    private Long productOptionId;

    @NotNull(message = "수량은 필수입니다")
    @Min(value = 1, message = "수량은 최소 1개 이상이어야 합니다")
    private Integer quantity;
    //가격은 위험하다고 판단해서 안받음
}
