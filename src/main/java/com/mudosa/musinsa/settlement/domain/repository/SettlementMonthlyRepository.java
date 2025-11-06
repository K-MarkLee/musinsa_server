package com.mudosa.musinsa.settlement.domain.repository;

import com.mudosa.musinsa.settlement.domain.model.SettlementMonthly;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

/**
 * 월별 정산 Repository
 */
@Repository
public interface SettlementMonthlyRepository extends JpaRepository<SettlementMonthly, Long> {

    /* 브랜드별 월간 정산 조회 (최신순, 페이징) */
    Page<SettlementMonthly> findByBrandIdOrderBySettlementYearDescSettlementMonthDesc(
        Long brandId,
        Pageable pageable
    );

    /* 브랜드별 + 월 시작일 범위로 조회 (페이징) */
    Page<SettlementMonthly> findByBrandIdAndMonthStartDateBetween(
        Long brandId,
        LocalDate startDate,
        LocalDate endDate,
        Pageable pageable
    );

    /* 브랜드별 + 특정 연도/월 조회 (통계용) */
    java.util.Optional<SettlementMonthly> findByBrandIdAndSettlementYearAndSettlementMonth(
        Long brandId,
        Integer settlementYear,
        Integer settlementMonth
    );

    /* 전체 브랜드 월간 정산 조회 (페이징) */
    Page<SettlementMonthly> findAllByOrderBySettlementYearDescSettlementMonthDesc(Pageable pageable);

    /* 전체 브랜드 최근 N년 월간 정산 조회 */
    Page<SettlementMonthly> findAllBySettlementYearGreaterThanEqualOrderBySettlementYearDescSettlementMonthDesc(
        Integer startYear,
        Pageable pageable
    );

    /* 브랜드별 최근 N년 월간 정산 조회 */
    Page<SettlementMonthly> findByBrandIdAndSettlementYearGreaterThanEqualOrderBySettlementYearDescSettlementMonthDesc(
        Long brandId,
        Integer startYear,
        Pageable pageable
    );
}
