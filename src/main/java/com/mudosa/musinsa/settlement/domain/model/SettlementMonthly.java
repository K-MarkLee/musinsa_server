package com.mudosa.musinsa.settlement.domain.model;

import com.mudosa.musinsa.common.domain.model.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 월간 정산 집계 애그리거트 루트
 */
@Entity
@Table(name = "settlements_monthly")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SettlementMonthly extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "settlement_monthly_id")
    private Long id;
    
    @Column(name = "brand_id", nullable = false)
    private Long brandId;
    
    @Column(name = "settlement_year", nullable = false)
    private Integer settlementYear;
    
    @Column(name = "settlement_month", nullable = false)
    private Integer settlementMonth;
    
    @Column(name = "total_order_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalOrderAmount = BigDecimal.ZERO;
    
    @Column(name = "total_commission_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalCommissionAmount = BigDecimal.ZERO;
    
    @Column(name = "total_settlement_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalSettlementAmount = BigDecimal.ZERO;
    
    @Column(name = "transaction_count", nullable = false)
    private Integer transactionCount = 0;
    
    /**
     * 월간 정산 생성
     */
    public static SettlementMonthly create(Long brandId, int year, int month) {
        SettlementMonthly settlement = new SettlementMonthly();
        settlement.brandId = brandId;
        settlement.settlementYear = year;
        settlement.settlementMonth = month;
        settlement.totalOrderAmount = BigDecimal.ZERO;
        settlement.totalCommissionAmount = BigDecimal.ZERO;
        settlement.totalSettlementAmount = BigDecimal.ZERO;
        settlement.transactionCount = 0;
        return settlement;
    }
}
