package com.mudosa.musinsa.settlement.domain.repository;

import com.mudosa.musinsa.settlement.domain.dto.YearlyAggregationDto;

import java.time.LocalDate;
import java.util.List;

/**
 * 집계 Repository 인터페이스
 * 월 -> 년
 */
public interface SettlementMonthlyAggregationRepository {

    /**
     * 연간 집계 쿼리
     *
     * @param brandId 브랜드 ID
     * @param startDate 집계 시작일 (inclusive)
     * @param endDate 집계 종료일 (inclusive)
     * @return 연도별 집계 결과
     */
    List<YearlyAggregationDto> aggregateByYearly(
        Long brandId,
        LocalDate startDate,
        LocalDate endDate
    );
}
