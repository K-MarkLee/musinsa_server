package com.mudosa.musinsa.settlement.domain.repository;

import com.mudosa.musinsa.settlement.domain.model.SettlementYearly;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 연간 정산 Repository
 */
@Repository
public interface SettlementYearlyRepository extends JpaRepository<SettlementYearly, Long> {

    /* 브랜드별 연간 정산 조회 (최신순, 페이징) */
    Page<SettlementYearly> findByBrandIdOrderBySettlementYearDesc(
        Long brandId,
        Pageable pageable
    );

    /* 브랜드별 + 정산 연도 범위로 조회 (페이징) */
    Page<SettlementYearly> findByBrandIdAndSettlementYearBetween(
        Long brandId,
        Integer startYear,
        Integer endYear,
        Pageable pageable
    );

    /* 브랜드별 + 특정 연도 조회 (통계용) */
    java.util.Optional<SettlementYearly> findByBrandIdAndSettlementYear(
        Long brandId,
        Integer settlementYear
    );

    /* 브랜드별 모든 연간 정산 조회 (전체 통계용) */
    java.util.List<SettlementYearly> findByBrandId(Long brandId);

    /* 전체 브랜드 연간 정산 조회 (페이징) */
    Page<SettlementYearly> findAllByOrderBySettlementYearDesc(Pageable pageable);

    /* 전체 브랜드 최근 N년 연간 정산 조회 */
    Page<SettlementYearly> findAllBySettlementYearGreaterThanEqualOrderBySettlementYearDesc(
        Integer startYear,
        Pageable pageable
    );

    /* 브랜드별 최근 N년 연간 정산 조회 */
    Page<SettlementYearly> findByBrandIdAndSettlementYearGreaterThanEqualOrderBySettlementYearDesc(
        Long brandId,
        Integer startYear,
        Pageable pageable
    );
}
