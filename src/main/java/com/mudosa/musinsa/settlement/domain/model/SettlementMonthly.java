package com.mudosa.musinsa.settlement.domain.model;

import com.mudosa.musinsa.common.domain.model.BaseEntity;
import com.mudosa.musinsa.common.vo.Money;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;

/**
 * 월간 정산 집계 애그리거트 루트
 */
@Entity
@Table(
    name = "settlements_monthly",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"settlement_number"}),
        @UniqueConstraint(columnNames = {"brand_id", "settlement_year_month"})
    },
    indexes = {
        @Index(name = "idx_settlement_year_month", columnList = "settlement_year_month"),
        @Index(name = "idx_brand_id", columnList = "brand_id"),
        @Index(name = "idx_settlement_status", columnList = "settlement_status")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j
public class SettlementMonthly extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "monthly_settlement_id")
    private Long id;

    @Column(name = "settlement_number", nullable = false, unique = true, length = 50)
    private String settlementNumber;

    @Column(name = "brand_id", nullable = false)
    private Long brandId;

    @Column(name = "settlement_year_month", nullable = false, length = 10)
    private String settlementYearMonth;

    @Column(name = "month_start_date", nullable = false)
    private LocalDate monthStartDate;

    @Column(name = "month_end_date", nullable = false)
    private LocalDate monthEndDate;

    @Column(name = "settlement_timezone", nullable = false, length = 50)
    private String settlementTimezone;

    @Column(name = "total_order_count", nullable = false)
    private Integer totalOrderCount = 0;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "total_sales_amount", nullable = false, precision = 15, scale = 2))
    private Money totalSalesAmount = Money.ZERO;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "total_commission_amount", nullable = false, precision = 15, scale = 2))
    private Money totalCommissionAmount = Money.ZERO;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "total_tax_amount", nullable = false, precision = 15, scale = 2))
    private Money totalTaxAmount = Money.ZERO;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "total_pg_fee_amount", nullable = false, precision = 15, scale = 2))
    private Money totalPgFeeAmount = Money.ZERO;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "final_settlement_amount", nullable = false, precision = 15, scale = 2))
    private Money finalSettlementAmount = Money.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "settlement_status", nullable = false, length = 20)
    private SettlementStatus settlementStatus = SettlementStatus.PENDING;

    @Column(name = "aggregated_at")
    private LocalDateTime aggregatedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    /**
     * 월간 정산 생성
     */
    public static SettlementMonthly create(
        Long brandId,
        int year,
        int month,
        String settlementNumber,
        String timezone
    ) {
        SettlementMonthly settlement = new SettlementMonthly();
        settlement.brandId = brandId;
        settlement.settlementYearMonth = String.format("%d-%02d", year, month);
        settlement.settlementNumber = settlementNumber;

        // Timezone 검증
        try {
            ZoneId.of(timezone);
            settlement.settlementTimezone = timezone;
        } catch (DateTimeException e) {
            log.warn("Invalid timezone: {}. Defaulting to UTC.", timezone, e);
            settlement.settlementTimezone = "UTC";
        }

        // 월의 시작일과 종료일 계산
        YearMonth yearMonth = YearMonth.of(year, month);
        settlement.monthStartDate = yearMonth.atDay(1);
        settlement.monthEndDate = yearMonth.atEndOfMonth();

        return settlement;
    }

    /**
     * 일간 정산 추가 (집계)
     */
    public void addDailySettlement(SettlementDaily daily) {
        this.totalOrderCount += daily.getTotalOrderCount();
        this.totalSalesAmount = this.totalSalesAmount.add(daily.getTotalSalesAmount());
        this.totalCommissionAmount = this.totalCommissionAmount.add(daily.getTotalCommissionAmount());
        this.totalTaxAmount = this.totalTaxAmount.add(daily.getTotalTaxAmount());
        this.totalPgFeeAmount = this.totalPgFeeAmount.add(daily.getTotalPgFeeAmount());
        this.finalSettlementAmount = calculateFinalAmount();
    }

    /**
     * 최종 정산 금액 계산
     */
    private Money calculateFinalAmount() {
        return totalSalesAmount
            .subtract(totalCommissionAmount)
            .subtract(totalTaxAmount)
            .subtract(totalPgFeeAmount);
    }

    /**
     * 집계 처리 시작
     */
    public void startProcessing() {
        this.settlementStatus = SettlementStatus.PROCESSING;
        this.aggregatedAt = LocalDateTime.now(ZoneId.of("UTC"));
    }

    /**
     * 정산 완료
     */
    public void complete() {
        this.settlementStatus = SettlementStatus.COMPLETED;
        this.completedAt = LocalDateTime.now(ZoneId.of("UTC"));
    }

    /**
     * 정산 실패
     */
    public void fail() {
        this.settlementStatus = SettlementStatus.FAILED;
    }
}
