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
    private Integer month;
    private Integer weekOfMonth;
    private LocalDate weekStartDate;
    private LocalDate weekEndDate;
    private String settlementTimezone;
    private Long totalOrderCount;
    private Money totalSalesAmount;
    private Money totalCommissionAmount;
    private Money totalTaxAmount;
    private Money totalPgFeeAmount;

    public WeeklyAggregationDto(
        Long brandId,
        Integer year,
        Integer month,
        Integer weekOfMonth,
        LocalDate weekStartDate,
        LocalDate weekEndDate,
        String settlementTimezone,
        Long totalOrderCount,
        BigDecimal totalSalesAmount,
        BigDecimal totalCommissionAmount,
        BigDecimal totalTaxAmount,
        BigDecimal totalPgFeeAmount
    ) {
        this.brandId = brandId;
        this.year = year;
        this.month = month;
        this.weekOfMonth = weekOfMonth;
        this.weekStartDate = weekStartDate;
        this.weekEndDate = weekEndDate;
        this.settlementTimezone = settlementTimezone;
        this.totalOrderCount = totalOrderCount;
        this.totalSalesAmount = new Money(totalSalesAmount);
        this.totalCommissionAmount = new Money(totalCommissionAmount);
        this.totalTaxAmount = new Money(totalTaxAmount);
        this.totalPgFeeAmount = new Money(totalPgFeeAmount);
    }
}
