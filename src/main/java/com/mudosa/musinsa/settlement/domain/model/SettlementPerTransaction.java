package com.mudosa.musinsa.settlement.domain.model;

import com.mudosa.musinsa.common.domain.model.CreatedOnlyEntity;
import com.mudosa.musinsa.common.vo.Money;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * 거래별 정산 애그리거트 루트
 */
@Entity
@Table(name = "settlements_per_transaction")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j
public class SettlementPerTransaction extends CreatedOnlyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Long id;

    @Column(name = "brand_id", nullable = false)
    private Long brandId;

    @Column(name = "payment_id", nullable = false)
    private Long paymentId;

    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;

    @Column(name = "transaction_date_local", nullable = false)
    private LocalDate transactionDateLocal;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 20)
    private TransactionType transactionType = TransactionType.ORDER;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "transaction_amount", nullable = false, precision = 15, scale = 2))
    private Money transactionAmount;

    @Column(name = "commission_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal commissionRate;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "commission_amount", nullable = false, precision = 15, scale = 2))
    private Money commissionAmount;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "tax_amount", nullable = false, precision = 15, scale = 2))
    private Money taxAmount;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "pg_fee_amount", nullable = false, precision = 15, scale = 2))
    private Money pgFeeAmount;

    @Column(name = "timezone_offset", nullable = false, length = 10)
    private String timezoneOffset = "+09:00";

    /**
     * 정산 거래 생성
     *
     * @param pgFeeAmount PG사 수수료 (Payment 도메인에서 계산된 값)
     */
    public static SettlementPerTransaction create(
        Long brandId,
        Long paymentId,
        Money transactionAmount,
        BigDecimal commissionRate,
        Money pgFeeAmount,
        TransactionType transactionType,
        String timezoneOffset
    ) {
        SettlementPerTransaction settlement = new SettlementPerTransaction();
        settlement.brandId = brandId;
        settlement.paymentId = paymentId;
        settlement.transactionAmount = transactionAmount;
        settlement.commissionRate = commissionRate;
        settlement.pgFeeAmount = pgFeeAmount;
        settlement.transactionType = transactionType;

        // UTC 현재 시각
        ZonedDateTime utcNow = ZonedDateTime.now(ZoneId.of("UTC"));
        settlement.transactionDate = utcNow.toLocalDateTime();

        // 사용자 타임존 기준 날짜
        ZoneId userZoneId;
        try {
            userZoneId = ZoneId.of(timezoneOffset);
            settlement.timezoneOffset = timezoneOffset;
        } catch (DateTimeException e) {
            log.warn("Invalid timezoneOffset: {}. Defaulting to UTC.", timezoneOffset, e);
            settlement.timezoneOffset = "UTC";
            userZoneId = ZoneId.of("UTC");
        }

        settlement.transactionDateLocal = utcNow
            .withZoneSameInstant(userZoneId)
            .toLocalDate();

        // 플랫폼 수수료 계산
        settlement.commissionAmount = transactionAmount
            .multiply(commissionRate)
            .divide(100);

        // 부가세 계산
        settlement.taxAmount = settlement.commissionAmount
            .multiply(BigDecimal.valueOf(10))
            .divide(100);

        return settlement;
    }

    /**
     * 최종 정산 금액 계산
     */
    public Money calculateFinalSettlementAmount() {
        return transactionAmount
            .subtract(commissionAmount)
            .subtract(taxAmount)
            .subtract(pgFeeAmount);
    }
}
