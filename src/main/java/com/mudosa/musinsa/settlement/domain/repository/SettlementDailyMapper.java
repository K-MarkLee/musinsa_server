package com.mudosa.musinsa.settlement.domain.repository;

import com.mudosa.musinsa.settlement.batch.dto.MonthlyAggregationDto;
import com.mudosa.musinsa.settlement.batch.dto.WeeklyAggregationDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * SettlementDaily MyBatis Mapper
 * 일일 정산 데이터를 주간/월간으로 집계
 */
@Mapper
public interface SettlementDailyMapper {

    /**
     * 주간 집계 쿼리
     *
     * @param brandId 브랜드 ID
     * @param startDate 집계 시작일 (inclusive)
     * @param endDate 집계 종료일 (inclusive)
     * @return 주별 집계 결과
     */
    List<WeeklyAggregationDto> aggregateByWeekly(
        @Param("brandId") Long brandId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
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
        @Param("brandId") Long brandId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
}
