package com.mudosa.musinsa.settlement.domain.dto;

import com.mudosa.musinsa.common.vo.Money;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * 월간 집계 DTO
 */
@Getter
public class MonthlyAggregationDto {

    private Long brandId;
    private Integer year;
    private Integer month;
    private Integer totalOrderCount;
    private Money totalSalesAmount;
    private Money totalCommissionAmount;
    private Money totalTaxAmount;
    private Money totalPgFeeAmount;

    public MonthlyAggregationDto(
        Long brandId,
        Integer year,
        Integer month,
        Long totalOrderCount,
        BigDecimal totalSalesAmount,
        BigDecimal totalCommissionAmount,
        BigDecimal totalTaxAmount,
        BigDecimal totalPgFeeAmount
    ) {
        this.brandId = brandId;
        this.year = year;
        this.month = month;
        this.totalOrderCount = totalOrderCount.intValue();
        this.totalSalesAmount = new Money(totalSalesAmount);
        this.totalCommissionAmount = new Money(totalCommissionAmount);
        this.totalTaxAmount = new Money(totalTaxAmount);
        this.totalPgFeeAmount = new Money(totalPgFeeAmount);
    }
}
