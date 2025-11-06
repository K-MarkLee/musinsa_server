package com.mudosa.musinsa.settlement.domain.vo;

import java.math.BigDecimal;

/**
 * 플랫폼 수수료율
 * 향후 브랜드별, 카테고리별 차등 수수료 확장 가능
 */
public class CommissionRate {

    private static final BigDecimal DEFAULT_RATE = new BigDecimal("0.10");

    public static BigDecimal getDefaultRate() {
        return DEFAULT_RATE;
    }
}