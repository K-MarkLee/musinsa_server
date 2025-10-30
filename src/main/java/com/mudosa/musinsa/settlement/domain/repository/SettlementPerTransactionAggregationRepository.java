package com.mudosa.musinsa.settlement.domain.repository;

import com.mudosa.musinsa.settlement.domain.dto.DailyAggregationDto;

import java.time.LocalDate;
import java.util.List;

/**
 * 집계 Repository 인터페이스
 * 건 -> 일
 */
public interface SettlementPerTransactionAggregationRepository {

    /**
     * 일일 집계 쿼리
     *
     * @param brandId 브랜드 ID
     * @param startDate 집계 시작일 (inclusive)
     * @param endDate 집계 종료일 (inclusive)
     * @return 일별 집계 결과
     */
    List<DailyAggregationDto> aggregateByDaily(
        Long brandId,
        LocalDate startDate,
        LocalDate endDate
    );
}
