package com.mudosa.musinsa.settlement.domain.repository;

import com.mudosa.musinsa.settlement.domain.model.SettlementYearly;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 연간 정산 데이터 조회
 */
@Repository
public interface SettlementYearlyRepository extends JpaRepository<SettlementYearly, Long> {

    /**
     * 브랜드별 연간 정산 조회 (페이징)
     */
    Page<SettlementYearly> findByBrandId(Long brandId, Pageable pageable);

    /**
     * 브랜드 + 연도 범위로 조회 (페이징)
     */
    Page<SettlementYearly> findByBrandIdAndSettlementYearBetween(
        Long brandId,
        Integer startYear,
        Integer endYear,
        Pageable pageable
    );

    /**
     * 브랜드 + 특정 연도로 조회
     */
    SettlementYearly findByBrandIdAndSettlementYear(Long brandId, Integer settlementYear);
}
