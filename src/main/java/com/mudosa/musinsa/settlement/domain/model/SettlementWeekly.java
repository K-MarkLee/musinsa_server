package com.mudosa.musinsa.settlement.domain.model;

import com.mudosa.musinsa.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 주간 정산 집계 애그리거트 루트
 */
@Entity
@Table(name = "settlements_weekly")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SettlementWeekly extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "settlement_weekly_id")
    private Long id;
    
    @Column(name = "brand_id", nullable = false)
    private Long brandId;
    
    @Column(name = "settlement_year", nullable = false)
    private Integer settlementYear;
    
    @Column(name = "settlement_week", nullable = false)
    private Integer settlementWeek;
    
    @Column(name = "total_order_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalOrderAmount = BigDecimal.ZERO;
    
    @Column(name = "total_commission_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalCommissionAmount = BigDecimal.ZERO;
    
    @Column(name = "total_settlement_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalSettlementAmount = BigDecimal.ZERO;
    
    @Column(name = "transaction_count", nullable = false)
    private Integer transactionCount = 0;
    
    /**
     * 주간 정산 생성
     */
    public static SettlementWeekly create(Long brandId, int year, int week) {
        SettlementWeekly settlement = new SettlementWeekly();
        settlement.brandId = brandId;
        settlement.settlementYear = year;
        settlement.settlementWeek = week;
        settlement.totalOrderAmount = BigDecimal.ZERO;
        settlement.totalCommissionAmount = BigDecimal.ZERO;
        settlement.totalSettlementAmount = BigDecimal.ZERO;
        settlement.transactionCount = 0;
        return settlement;
    }
}
