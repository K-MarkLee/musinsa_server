package com.mudosa.musinsa.settlement.domain.repository;

import com.mudosa.musinsa.settlement.domain.dto.YearlyAggregationDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * SettlementMonthly MyBatis Mapper
 * 월간 정산 데이터를 연간으로 집계
 */
@Mapper
public interface SettlementMonthlyMapper {

    /**
     * 연간 집계 쿼리
     *
     * @param brandId 브랜드 ID
     * @param startDate 집계 시작일 (inclusive)
     * @param endDate 집계 종료일 (inclusive)
     * @return 연도별 집계 결과
     */
    List<YearlyAggregationDto> aggregateByYearly(
        @Param("brandId") Long brandId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
}
