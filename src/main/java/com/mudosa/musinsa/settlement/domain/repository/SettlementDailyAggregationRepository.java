package com.mudosa.musinsa.settlement.domain.repository;

import com.mudosa.musinsa.settlement.domain.dto.MonthlyAggregationDto;
import com.mudosa.musinsa.settlement.domain.dto.WeeklyAggregationDto;

import java.time.LocalDate;
import java.util.List;

/**
 * 집계 Repository 인터페이스
 * 일 -> 주/월
 */
public interface SettlementDailyAggregationRepository {

    /**
     * 주간 집계 쿼리
     *
     * @param brandId 브랜드 ID
     * @param startDate 집계 시작일 (inclusive)
     * @param endDate 집계 종료일 (inclusive)
     * @return 주별 집계 결과
     */
    List<WeeklyAggregationDto> aggregateByWeekly(
        Long brandId,
        LocalDate startDate,
        LocalDate endDate
    );

    /**
     * 월간 집계 쿼리
     *
     * @param brandId 브랜드 ID
     * @param startDate 집계 시작일 (inclusive)
     * @param endDate 집계 종료일 (inclusive)
     * @return 월별 집계 결과
     */
    List<MonthlyAggregationDto> aggregateByMonthly(
        Long brandId,
        LocalDate startDate,
        LocalDate endDate
    );
}
