package com.mudosa.musinsa.settlement.batch.dto;

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

    public WeeklyAggregationDto() {
    }

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

    public void setBrandId(Long brandId) {
        this.brandId = brandId;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public void setWeekOfMonth(Integer weekOfMonth) {
        this.weekOfMonth = weekOfMonth;
    }

    public void setWeekStartDate(LocalDate weekStartDate) {
        this.weekStartDate = weekStartDate;
    }

    public void setWeekEndDate(LocalDate weekEndDate) {
        this.weekEndDate = weekEndDate;
    }

    public void setSettlementTimezone(String settlementTimezone) {
        this.settlementTimezone = settlementTimezone;
    }

    public void setTotalOrderCount(Long totalOrderCount) {
        this.totalOrderCount = totalOrderCount;
    }

    public void setTotalSalesAmount(BigDecimal totalSalesAmount) {
        this.totalSalesAmount = new Money(totalSalesAmount);
    }

    public void setTotalCommissionAmount(BigDecimal totalCommissionAmount) {
        this.totalCommissionAmount = new Money(totalCommissionAmount);
    }

    public void setTotalTaxAmount(BigDecimal totalTaxAmount) {
        this.totalTaxAmount = new Money(totalTaxAmount);
    }

    public void setTotalPgFeeAmount(BigDecimal totalPgFeeAmount) {
        this.totalPgFeeAmount = new Money(totalPgFeeAmount);
    }
}
