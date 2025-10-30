package com.mudosa.musinsa.settlement.domain.repository;

import com.mudosa.musinsa.settlement.domain.model.SettlementDaily;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

/**
 * 일별 정산 데이터 조회
 */
@Repository
public interface SettlementDailyRepository
    extends JpaRepository<SettlementDaily, Long>,
            SettlementDailyAggregationRepository {

    /**
     * 브랜드별 일일 정산 조회 (페이징)
     */
    Page<SettlementDaily> findByBrandId(Long brandId, Pageable pageable);

    /**
     * 브랜드 + 날짜 범위로 조회 (페이징)
     */
    Page<SettlementDaily> findByBrandIdAndSettlementDateBetween(
        Long brandId,
        LocalDate startDate,
        LocalDate endDate,
        Pageable pageable
    );

    /**
     * 브랜드 + 특정 날짜 조회
     */
    SettlementDaily findByBrandIdAndSettlementDate(Long brandId, LocalDate settlementDate);
}
