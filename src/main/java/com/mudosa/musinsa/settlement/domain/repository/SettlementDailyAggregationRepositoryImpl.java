package com.mudosa.musinsa.settlement.domain.repository;

import com.mudosa.musinsa.settlement.domain.dto.MonthlyAggregationDto;
import com.mudosa.musinsa.settlement.domain.dto.WeeklyAggregationDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * 집계 Repository 인터페이스 구현체
 * 일 -> 주/월
 */
@Repository
public class SettlementDailyAggregationRepositoryImpl
    implements SettlementDailyAggregationRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<WeeklyAggregationDto> aggregateByWeekly(
        Long brandId,
        LocalDate startDate,
        LocalDate endDate
    ) {
        String jpql = """
            SELECT new com.mudosa.musinsa.settlement.domain.dto.WeeklyAggregationDto(
                d.brandId,
                FUNCTION('YEAR', d.settlementDate),
                FUNCTION('WEEK', d.settlementDate),
                MIN(d.settlementDate),
                MAX(d.settlementDate),
                SUM(d.totalOrderCount),
                SUM(d.totalSalesAmount.amount),
                SUM(d.totalCommissionAmount.amount),
                SUM(d.totalTaxAmount.amount),
                SUM(d.totalPgFeeAmount.amount)
            )
            FROM SettlementDaily d
            WHERE d.brandId = :brandId
              AND d.settlementDate BETWEEN :startDate AND :endDate
            GROUP BY d.brandId, FUNCTION('YEAR', d.settlementDate), FUNCTION('WEEK', d.settlementDate)
            ORDER BY FUNCTION('YEAR', d.settlementDate), FUNCTION('WEEK', d.settlementDate)
            """;

        return entityManager.createQuery(jpql, WeeklyAggregationDto.class)
            .setParameter("brandId", brandId)
            .setParameter("startDate", startDate)
            .setParameter("endDate", endDate)
            .getResultList();
    }

    @Override
    public List<MonthlyAggregationDto> aggregateByMonthly(
        Long brandId,
        LocalDate startDate,
        LocalDate endDate
    ) {
        String jpql = """
            SELECT new com.mudosa.musinsa.settlement.domain.dto.MonthlyAggregationDto(
                d.brandId,
                FUNCTION('YEAR', d.settlementDate),
                FUNCTION('MONTH', d.settlementDate),
                SUM(d.totalOrderCount),
                SUM(d.totalSalesAmount.amount),
                SUM(d.totalCommissionAmount.amount),
                SUM(d.totalTaxAmount.amount),
                SUM(d.totalPgFeeAmount.amount)
            )
            FROM SettlementDaily d
            WHERE d.brandId = :brandId
              AND d.settlementDate BETWEEN :startDate AND :endDate
            GROUP BY d.brandId, FUNCTION('YEAR', d.settlementDate), FUNCTION('MONTH', d.settlementDate)
            ORDER BY FUNCTION('YEAR', d.settlementDate), FUNCTION('MONTH', d.settlementDate)
            """;

        return entityManager.createQuery(jpql, MonthlyAggregationDto.class)
            .setParameter("brandId", brandId)
            .setParameter("startDate", startDate)
            .setParameter("endDate", endDate)
            .getResultList();
    }
}
