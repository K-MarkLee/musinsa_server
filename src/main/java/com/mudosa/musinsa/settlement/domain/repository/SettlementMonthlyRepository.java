package com.mudosa.musinsa.settlement.domain.repository;

import com.mudosa.musinsa.settlement.domain.model.SettlementMonthly;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

/**
 * 월별 정산 데이터 조회
 */
@Repository
public interface SettlementMonthlyRepository
    extends JpaRepository<SettlementMonthly, Long> {

    /**
     * 브랜드별 월간 정산 조회 (페이징)
     */
    Page<SettlementMonthly> findByBrandId(Long brandId, Pageable pageable);

    /**
     * 브랜드 + 날짜 범위로 조회 (페이징)
     */
    Page<SettlementMonthly> findByBrandIdAndMonthStartDateBetween(
        Long brandId,
        LocalDate startDate,
        LocalDate endDate,
        Pageable pageable
    );

    /**
     * 브랜드 + 연도-월로 조회
     */
    SettlementMonthly findByBrandIdAndSettlementYearAndSettlementMonth(
        Long brandId,
        Integer settlementYear,
        Integer settlementMonth
    );
}
