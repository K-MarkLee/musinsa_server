package com.mudosa.musinsa.settlement.domain.repository;

import com.mudosa.musinsa.settlement.domain.model.SettlementWeekly;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

/**
 * SettlementWeekly Repository
 */
@Repository
public interface SettlementWeeklyRepository extends JpaRepository<SettlementWeekly, Long> {

    /**
     * 브랜드별 주간 정산 조회 (페이징)
     */
    Page<SettlementWeekly> findByBrandId(Long brandId, Pageable pageable);

    /**
     * 브랜드 + 날짜 범위로 조회 (페이징)
     */
    Page<SettlementWeekly> findByBrandIdAndWeekStartDateBetween(
        Long brandId,
        LocalDate startDate,
        LocalDate endDate,
        Pageable pageable
    );

    /**
     * 브랜드 + 연도-월-주차로 조회
     */
    SettlementWeekly findByBrandIdAndSettlementYearAndSettlementMonthAndWeekOfMonth(
        Long brandId,
        Integer settlementYear,
        Integer settlementMonth,
        Integer weekOfMonth
    );
}
