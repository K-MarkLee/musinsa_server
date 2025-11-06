package com.mudosa.musinsa.settlement.domain.repository;

import com.mudosa.musinsa.settlement.batch.dto.DailyAggregationDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * SettlementPerTransaction MyBatis Mapper
 * 거래별 정산 데이터를 일일로 집계
 */
@Mapper
public interface SettlementPerTransactionMapper {

    /**
     * 일일 집계 쿼리
     *
     * @param brandId 브랜드 ID
     * @param startDate 집계 시작일 (inclusive)
     * @param endDate 집계 종료일 (inclusive)
     * @return 일별 집계 결과
     */
    List<DailyAggregationDto> aggregateByDaily(
        @Param("brandId") Long brandId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
}
