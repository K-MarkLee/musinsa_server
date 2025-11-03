package com.mudosa.musinsa.product.application.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

// 상품 옵션 재고 현황을 반환하는 응답 DTO이다.
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductOptionStockResponse {

    private Long productOptionId;
    private String productName;
    private BigDecimal productPrice;
    private Integer stockQuantity;
    private Boolean hasStock;
    private List<OptionValueSummary> optionValues;

    public List<OptionValueSummary> getOptionValues() {
        return optionValues != null ? optionValues : Collections.emptyList();
    }

    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    public static class OptionValueSummary {
        private Long optionValueId;
        private String optionName;
        private String optionValue;
    }
}
