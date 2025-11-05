package com.mudosa.musinsa.settlement.domain.repository;

import com.mudosa.musinsa.settlement.domain.model.SettlementDaily;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

/**
 * 일별 정산 Repository
 */
@Repository
public interface SettlementDailyRepository extends JpaRepository<SettlementDaily, Long> {

    /* 브랜드별 일일 정산 조회 (최신순, 페이징) */
    Page<SettlementDaily> findByBrandIdOrderBySettlementDateDesc(Long brandId, Pageable pageable);

    /* 브랜드별 + 정산일 범위로 조회 (페이징) */
    Page<SettlementDaily> findByBrandIdAndSettlementDateBetween(
        Long brandId,
        LocalDate startDate,
        LocalDate endDate,
        Pageable pageable
    );

    /* 브랜드별 + 특정 정산일 조회 (통계) */
    java.util.Optional<SettlementDaily> findByBrandIdAndSettlementDate(
        Long brandId,
        LocalDate settlementDate
    );

    /* 전체 브랜드 일별 정산 조회 (페이징) */
    Page<SettlementDaily> findAllByOrderBySettlementDateDesc(Pageable pageable);
}
