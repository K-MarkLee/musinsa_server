package com.mudosa.musinsa.settlement.domain.model;

import com.mudosa.musinsa.common.domain.model.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 거래별 정산 애그리거트 루트
 */
@Entity
@Table(name = "settlements_per_transaction")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SettlementPerTransaction extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "settlement_id")
    private Long id;
    
    @Column(name = "brand_id", nullable = false)
    private Long brandId; // Brand 애그리거트 참조 (ID만)
    
    @Column(name = "payment_id", nullable = false)
    private Long paymentId; // Payment 애그리거트 참조 (ID만)
    
    @Column(name = "order_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal orderAmount;
    
    @Column(name = "commission_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal commissionRate;
    
    @Column(name = "commission_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal commissionAmount;
    
    @Column(name = "settlement_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal settlementAmount;
    
    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "settlement_status", nullable = false)
    private SettlementStatus settlementStatus;
    
    /**
     * 정산 생성
     */
    public static SettlementPerTransaction create(
        Long brandId,
        Long paymentId,
        BigDecimal orderAmount,
        BigDecimal commissionRate
    ) {
        SettlementPerTransaction settlement = new SettlementPerTransaction();
        settlement.brandId = brandId;
        settlement.paymentId = paymentId;
        settlement.orderAmount = orderAmount;
        settlement.commissionRate = commissionRate;
        settlement.transactionDate = LocalDateTime.now();
        settlement.settlementStatus = SettlementStatus.PENDING;
        
        // 수수료 및 정산금액 계산
        settlement.commissionAmount = orderAmount
            .multiply(commissionRate)
            .divide(BigDecimal.valueOf(100));
        settlement.settlementAmount = orderAmount.subtract(settlement.commissionAmount);
        
        return settlement;
    }
    
    /**
     * 정산 완료
     */
    public void complete() {
        this.settlementStatus = SettlementStatus.COMPLETED;
    }
}
