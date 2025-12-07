package com.mudosa.musinsa.product.infrastructure.search.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

/**
 * ES 색인을 위한 옵션 단위 조회 DTO.
 * DB에서 projection으로 필요한 필드만 끌어오고, 매퍼에서 ProductDocument로 변환한다.
 */
@Getter
@Builder
public class ProductIndexDto {
    private final Long productOptionId;
    private final Long productId;
    private final Long brandId;
    private final String productName;
    private final String krBrandName;
    private final String enBrandName;
    private final String categoryPath;
    private final String gender;
    private final Boolean isAvailable;
    private final BigDecimal defaultPrice;
    private final String thumbnailUrl;
    private final Boolean hasStock;
    private final List<String> colorOptions;
    private final List<String> sizeOptions;

    public List<String> getColorOptions() {
        return colorOptions != null ? colorOptions : Collections.emptyList();
    }

    public List<String> getSizeOptions() {
        return sizeOptions != null ? sizeOptions : Collections.emptyList();
    }
}
