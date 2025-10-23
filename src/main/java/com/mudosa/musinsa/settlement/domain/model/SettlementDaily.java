package com.mudosa.musinsa.settlement.domain.model;

import com.mudosa.musinsa.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 일일 정산 집계 애그리거트 루트
 */
@Entity
@Table(name = "settlements_daily")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SettlementDaily extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "settlement_daily_id")
    private Long id;
    
    @Column(name = "brand_id", nullable = false)
    private Long brandId;
    
    @Column(name = "settlement_date", nullable = false)
    private LocalDate settlementDate;
    
    @Column(name = "total_order_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalOrderAmount = BigDecimal.ZERO;
    
    @Column(name = "total_commission_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalCommissionAmount = BigDecimal.ZERO;
    
    @Column(name = "total_settlement_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalSettlementAmount = BigDecimal.ZERO;
    
    @Column(name = "transaction_count", nullable = false)
    private Integer transactionCount = 0;
    
    /**
     * 일일 정산 생성
     */
    public static SettlementDaily create(Long brandId, LocalDate settlementDate) {
        SettlementDaily settlement = new SettlementDaily();
        settlement.brandId = brandId;
        settlement.settlementDate = settlementDate;
        settlement.totalOrderAmount = BigDecimal.ZERO;
        settlement.totalCommissionAmount = BigDecimal.ZERO;
        settlement.totalSettlementAmount = BigDecimal.ZERO;
        settlement.transactionCount = 0;
        return settlement;
    }
}
