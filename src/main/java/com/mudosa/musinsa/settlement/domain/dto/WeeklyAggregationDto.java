package com.mudosa.musinsa.settlement.domain.dto;

import com.mudosa.musinsa.common.vo.Money;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 주간 집계 DTO
 */
@Getter
public class WeeklyAggregationDto {

    private Long brandId;
    private Integer year;
    private Integer week;
    private LocalDate weekStartDate;
    private LocalDate weekEndDate;
    private Long totalOrderCount;
    private Money totalSalesAmount;
    private Money totalCommissionAmount;
    private Money totalTaxAmount;
    private Money totalPgFeeAmount;

    public WeeklyAggregationDto(
        Long brandId,
        Integer year,
        Integer week,
        LocalDate weekStartDate,
        LocalDate weekEndDate,
        Long totalOrderCount,
        BigDecimal totalSalesAmount,
        BigDecimal totalCommissionAmount,
        BigDecimal totalTaxAmount,
        BigDecimal totalPgFeeAmount
    ) {
        this.brandId = brandId;
        this.year = year;
        this.week = week;
        this.weekStartDate = weekStartDate;
        this.weekEndDate = weekEndDate;
        this.totalOrderCount = totalOrderCount;
        this.totalSalesAmount = new Money(totalSalesAmount);
        this.totalCommissionAmount = new Money(totalCommissionAmount);
        this.totalTaxAmount = new Money(totalTaxAmount);
        this.totalPgFeeAmount = new Money(totalPgFeeAmount);
    }
}
