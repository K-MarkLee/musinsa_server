package com.mudosa.musinsa.settlement.domain.model;

import com.mudosa.musinsa.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 연간 정산 집계 애그리거트 루트
 */
@Entity
@Table(name = "settlements_yearly")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SettlementYearly extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "settlement_yearly_id")
    private Long id;
    
    @Column(name = "brand_id", nullable = false)
    private Long brandId;
    
    @Column(name = "settlement_year", nullable = false)
    private Integer settlementYear;
    
    @Column(name = "total_order_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalOrderAmount = BigDecimal.ZERO;
    
    @Column(name = "total_commission_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalCommissionAmount = BigDecimal.ZERO;
    
    @Column(name = "total_settlement_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalSettlementAmount = BigDecimal.ZERO;
    
    @Column(name = "transaction_count", nullable = false)
    private Integer transactionCount = 0;
    
    /**
     * 연간 정산 생성
     */
    public static SettlementYearly create(Long brandId, int year) {
        SettlementYearly settlement = new SettlementYearly();
        settlement.brandId = brandId;
        settlement.settlementYear = year;
        settlement.totalOrderAmount = BigDecimal.ZERO;
        settlement.totalCommissionAmount = BigDecimal.ZERO;
        settlement.totalSettlementAmount = BigDecimal.ZERO;
        settlement.transactionCount = 0;
        return settlement;
    }
}
