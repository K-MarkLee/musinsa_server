package com.mudosa.musinsa.settlement.batch.dto;

import com.mudosa.musinsa.common.vo.Money;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 일일 집계 DTO
 */
@Getter
public class DailyAggregationDto {

    private Long brandId;
    private LocalDate settlementDate;
    private Long totalOrderCount;
    private Money totalSalesAmount;
    private Money totalCommissionAmount;
    private Money totalTaxAmount;
    private Money totalPgFeeAmount;

    public DailyAggregationDto(
        Long brandId,
        LocalDate settlementDate,
        Long totalOrderCount,
        BigDecimal totalSalesAmount,
        BigDecimal totalCommissionAmount,
        BigDecimal totalTaxAmount,
        BigDecimal totalPgFeeAmount
    ) {
        this.brandId = brandId;
        this.settlementDate = settlementDate;
        this.totalOrderCount = totalOrderCount;
        this.totalSalesAmount = new Money(totalSalesAmount);
        this.totalCommissionAmount = new Money(totalCommissionAmount);
        this.totalTaxAmount = new Money(totalTaxAmount);
        this.totalPgFeeAmount = new Money(totalPgFeeAmount);
    }
}
