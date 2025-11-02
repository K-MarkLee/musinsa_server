package com.mudosa.musinsa.settlement.domain.dto;

import com.mudosa.musinsa.common.vo.Money;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * 연간 집계 DTO
 */
@Getter
public class YearlyAggregationDto {

    private Long brandId;
    private Integer year;
    private Long totalOrderCount;
    private Money totalSalesAmount;
    private Money totalCommissionAmount;
    private Money totalTaxAmount;
    private Money totalPgFeeAmount;

    public YearlyAggregationDto(
        Long brandId,
        Integer year,
        Long totalOrderCount,
        BigDecimal totalSalesAmount,
        BigDecimal totalCommissionAmount,
        BigDecimal totalTaxAmount,
        BigDecimal totalPgFeeAmount
    ) {
        this.brandId = brandId;
        this.year = year;
        this.totalOrderCount = totalOrderCount;
        this.totalSalesAmount = new Money(totalSalesAmount);
        this.totalCommissionAmount = new Money(totalCommissionAmount);
        this.totalTaxAmount = new Money(totalTaxAmount);
        this.totalPgFeeAmount = new Money(totalPgFeeAmount);
    }

    // MyBatis용 setter 메서드들
    public void setBrandId(Long brandId) {
        this.brandId = brandId;
    }

    public void setYear(Integer year) {
        this.year = year;
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
