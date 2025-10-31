package com.mudosa.musinsa.settlement.domain.repository;

import com.mudosa.musinsa.settlement.domain.dto.YearlyAggregationDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * 집계 Repository 인터페이스 구현체
 * 월 -> 년
 */
@Repository
public class SettlementMonthlyAggregationRepositoryImpl
    implements SettlementMonthlyAggregationRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<YearlyAggregationDto> aggregateByYearly(
        Long brandId,
        LocalDate startDate,
        LocalDate endDate
    ) {
        String jpql = """
            SELECT new com.mudosa.musinsa.settlement.domain.dto.YearlyAggregationDto(
                m.brandId,
                FUNCTION('YEAR', m.monthStartDate),
                SUM(m.totalOrderCount),
                SUM(m.totalSalesAmount.amount),
                SUM(m.totalCommissionAmount.amount),
                SUM(m.totalTaxAmount.amount),
                SUM(m.totalPgFeeAmount.amount)
            )
            FROM SettlementMonthly m
            WHERE m.brandId = :brandId
              AND m.monthStartDate BETWEEN :startDate AND :endDate
            GROUP BY m.brandId, FUNCTION('YEAR', m.monthStartDate)
            ORDER BY FUNCTION('YEAR', m.monthStartDate)
            """;

        return entityManager.createQuery(jpql, YearlyAggregationDto.class)
            .setParameter("brandId", brandId)
            .setParameter("startDate", startDate)
            .setParameter("endDate", endDate)
            .getResultList();
    }
}
