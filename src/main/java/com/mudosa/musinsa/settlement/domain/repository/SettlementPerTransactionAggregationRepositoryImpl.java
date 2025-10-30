package com.mudosa.musinsa.settlement.domain.repository;

import com.mudosa.musinsa.settlement.domain.dto.DailyAggregationDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * 집계 Repository 인터페이스 구현체
 * 건 -> 일
 */
@Repository
public class SettlementPerTransactionAggregationRepositoryImpl
    implements SettlementPerTransactionAggregationRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<DailyAggregationDto> aggregateByDaily(
        Long brandId,
        LocalDate startDate,
        LocalDate endDate
    ) {
        String jpql = """
            SELECT new com.mudosa.musinsa.settlement.domain.dto.DailyAggregationDto(
                t.brandId,
                t.transactionDateLocal,
                COUNT(t.id),
                SUM(t.transactionAmount.amount),
                SUM(t.commissionAmount.amount),
                SUM(t.taxAmount.amount),
                SUM(t.pgFeeAmount.amount)
            )
            FROM SettlementPerTransaction t
            WHERE t.brandId = :brandId
              AND t.transactionDateLocal BETWEEN :startDate AND :endDate
            GROUP BY t.brandId, t.transactionDateLocal
            ORDER BY t.transactionDateLocal
            """;

        return entityManager.createQuery(jpql, DailyAggregationDto.class)
            .setParameter("brandId", brandId)
            .setParameter("startDate", startDate)
            .setParameter("endDate", endDate)
            .getResultList();
    }
}
