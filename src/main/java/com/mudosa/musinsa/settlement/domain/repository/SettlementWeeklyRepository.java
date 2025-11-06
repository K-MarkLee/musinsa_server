package com.mudosa.musinsa.settlement.domain.repository;

import com.mudosa.musinsa.settlement.domain.model.SettlementWeekly;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

/**
 * 주간 정산 Repository
 */
@Repository
public interface SettlementWeeklyRepository extends JpaRepository<SettlementWeekly, Long> {

    /* 브랜드별 주간 정산 조회 (최신순, 페이징) */
    Page<SettlementWeekly> findByBrandIdOrderByWeekStartDateDesc(Long brandId, Pageable pageable);

    /* 브랜드별 + 주간 시작일 범위로 조회 (통계용) */
    Page<SettlementWeekly> findByBrandIdAndWeekStartDateBetween(
        Long brandId,
        LocalDate startDate,
        LocalDate endDate,
        Pageable pageable
    );

    /* 브랜드별 + 연도/월/주차로 특정 주간 정산 조회 */
    SettlementWeekly findByBrandIdAndSettlementYearAndSettlementMonthAndWeekOfMonth(
        Long brandId,
        Integer settlementYear,
        Integer settlementMonth,
        Integer weekOfMonth
    );

    /* 전체 브랜드 주간 정산 조회 (페이징) */
    Page<SettlementWeekly> findAllByOrderByWeekStartDateDesc(Pageable pageable);
}
