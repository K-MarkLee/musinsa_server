package com.mudosa.musinsa.settlement.application;

import com.mudosa.musinsa.settlement.domain.dto.DailyAggregationDto;
import com.mudosa.musinsa.settlement.domain.dto.MonthlyAggregationDto;
import com.mudosa.musinsa.settlement.domain.dto.WeeklyAggregationDto;
import com.mudosa.musinsa.settlement.domain.dto.YearlyAggregationDto;
import com.mudosa.musinsa.settlement.domain.model.SettlementDaily;
import com.mudosa.musinsa.settlement.domain.model.SettlementMonthly;
import com.mudosa.musinsa.settlement.domain.model.SettlementWeekly;
import com.mudosa.musinsa.settlement.domain.model.SettlementYearly;
import com.mudosa.musinsa.settlement.domain.repository.SettlementDailyMapper;
import com.mudosa.musinsa.settlement.domain.repository.SettlementDailyRepository;
import com.mudosa.musinsa.settlement.domain.repository.SettlementMonthlyMapper;
import com.mudosa.musinsa.settlement.domain.repository.SettlementMonthlyRepository;
import com.mudosa.musinsa.settlement.domain.repository.SettlementPerTransactionRepository;
import com.mudosa.musinsa.settlement.domain.repository.SettlementWeeklyRepository;
import com.mudosa.musinsa.settlement.domain.repository.SettlementYearlyRepository;
import com.mudosa.musinsa.settlement.domain.service.SettlementNumberGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 정산 집계 서비스
 * - 거래별 → 일일 집계
 * - 일일 → 주간 집계
 * - 일일 → 월간 집계
 * - 월간 → 연간 집계
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SettlementAggregationService {

    private final SettlementPerTransactionRepository perTransactionRepository;
    private final SettlementDailyRepository dailyRepository;
    private final SettlementDailyMapper dailyMapper;
    private final SettlementWeeklyRepository weeklyRepository;
    private final SettlementMonthlyRepository monthlyRepository;
    private final SettlementMonthlyMapper monthlyMapper;
    private final SettlementYearlyRepository yearlyRepository;
    private final SettlementNumberGenerator settlementNumberGenerator;

    /**
     * 거래별 정산 데이터를 일일 정산으로 집계
     *
     * @param brandId 브랜드 ID
     * @param startDate 시작일
     * @param endDate 종료일
     * @return 생성된 일일 정산 목록
     */
    @Transactional
    public List<SettlementDaily> aggregateToDaily(Long brandId, LocalDate startDate, LocalDate endDate) {
        log.info("Starting daily aggregation for brandId={}, period={} to {}", brandId, startDate, endDate);

        List<DailyAggregationDto> aggregations = perTransactionRepository.aggregateByDaily(
            brandId, startDate, endDate
        );

        if (aggregations.isEmpty()) {
            log.warn("No transactions found for aggregation");
            return List.of();
        }

        log.info("Found {} daily aggregations", aggregations.size());

        List<SettlementDaily> dailySettlements = new ArrayList<>();

        for (DailyAggregationDto dto : aggregations) {
            String settlementNumber = settlementNumberGenerator.generateDailyNumber(
                dto.getSettlementDate()
            );

            SettlementDaily daily = SettlementDaily.create(
                dto.getBrandId(),
                dto.getSettlementDate(),
                settlementNumber,
                "Asia/Seoul"
            );

            setDailyAggregationData(daily, dto);

            daily.startProcessing();
            daily.complete();

            SettlementDaily saved = dailyRepository.save(daily);
            dailySettlements.add(saved);

            log.info("Created daily settlement: {}, amount={}",
                saved.getSettlementNumber(), saved.getFinalSettlementAmount());
        }

        log.info("Successfully created {} daily settlements", dailySettlements.size());
        return dailySettlements;
    }

    /**
     * 일일 정산 데이터를 주간 정산으로 집계 (MyBatis)
     *
     * @param brandId 브랜드 ID
     * @param startDate 시작일
     * @param endDate 종료일
     * @return 생성된 주간 정산 목록
     */
    @Transactional
    public List<SettlementWeekly> aggregateToWeekly(Long brandId, LocalDate startDate, LocalDate endDate) {
        log.info("Starting weekly aggregation for brandId={}, period={} to {}", brandId, startDate, endDate);

        List<WeeklyAggregationDto> aggregations = dailyMapper.aggregateByWeekly(brandId, startDate, endDate);

        if (aggregations.isEmpty()) {
            log.warn("No daily settlements found for weekly aggregation");
            return List.of();
        }

        log.info("Found {} weekly aggregations", aggregations.size());

        List<SettlementWeekly> weeklySettlements = new ArrayList<>();

        for (WeeklyAggregationDto dto : aggregations) {
            String settlementNumber = settlementNumberGenerator.generateWeeklyNumber(
                dto.getYear(), dto.getWeekOfMonth()
            );

            SettlementWeekly weekly = SettlementWeekly.create(
                dto.getBrandId(),
                dto.getYear(),
                dto.getWeekOfMonth(),
                dto.getWeekStartDate(),
                dto.getWeekEndDate(),
                settlementNumber,
                "Asia/Seoul"
            );

            setWeeklyAggregationData(weekly, dto);

            weekly.startProcessing();
            weekly.complete();

            SettlementWeekly saved = weeklyRepository.save(weekly);
            weeklySettlements.add(saved);

            log.info("Created weekly settlement: {}, amount={}",
                saved.getSettlementNumber(), saved.getFinalSettlementAmount());
        }

        log.info("Successfully created {} weekly settlements", weeklySettlements.size());
        return weeklySettlements;
    }

    /**
     * 일일 정산 데이터를 월간 정산으로 집계 (MyBatis)
     *
     * @param brandId 브랜드 ID
     * @param startDate 시작일
     * @param endDate 종료일
     * @return 생성된 월간 정산 목록
     */
    @Transactional
    public List<SettlementMonthly> aggregateToMonthly(Long brandId, LocalDate startDate, LocalDate endDate) {
        log.info("Starting monthly aggregation for brandId={}, period={} to {}", brandId, startDate, endDate);

        List<MonthlyAggregationDto> aggregations = dailyMapper.aggregateByMonthly(brandId, startDate, endDate);

        if (aggregations.isEmpty()) {
            log.warn("No daily settlements found for monthly aggregation");
            return List.of();
        }

        log.info("Found {} monthly aggregations", aggregations.size());

        List<SettlementMonthly> monthlySettlements = new ArrayList<>();

        for (MonthlyAggregationDto dto : aggregations) {
            String settlementNumber = settlementNumberGenerator.generateMonthlyNumber(
                dto.getYear(), dto.getMonth()
            );

            SettlementMonthly monthly = SettlementMonthly.create(
                dto.getBrandId(),
                dto.getYear(),
                dto.getMonth(),
                settlementNumber,
                "Asia/Seoul"
            );

            setMonthlyAggregationData(monthly, dto);

            monthly.startProcessing();
            monthly.complete();

            SettlementMonthly saved = monthlyRepository.save(monthly);
            monthlySettlements.add(saved);

            log.info("Created monthly settlement: {}, amount={}",
                saved.getSettlementNumber(), saved.getFinalSettlementAmount());
        }

        log.info("Successfully created {} monthly settlements", monthlySettlements.size());
        return monthlySettlements;
    }

    /**
     * 월간 정산 데이터를 연간 정산으로 집계 (MyBatis)
     *
     * @param brandId 브랜드 ID
     * @param year 연도
     * @return 생성된 연간 정산 (데이터가 없으면 Optional.empty())
     */
    @Transactional
    public Optional<SettlementYearly> aggregateToYearly(Long brandId, int year) {
        log.info("Starting yearly aggregation for brandId={}, year={}", brandId, year);

        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);

        List<YearlyAggregationDto> aggregations = monthlyMapper.aggregateByYearly(brandId, startDate, endDate);

        if (aggregations.isEmpty()) {
            log.warn("No monthly settlements found for yearly aggregation");
            return Optional.empty();
        }

        YearlyAggregationDto dto = aggregations.get(0);

        String settlementNumber = settlementNumberGenerator.generateYearlyNumber(dto.getYear());

        SettlementYearly yearly = SettlementYearly.create(
            dto.getBrandId(),
            dto.getYear(),
            settlementNumber,
            "Asia/Seoul"
        );

        setYearlyAggregationData(yearly, dto);

        yearly.startProcessing();
        yearly.complete();

        SettlementYearly saved = yearlyRepository.save(yearly);

        log.info("Created yearly settlement: {}, amount={}",
            saved.getSettlementNumber(), saved.getFinalSettlementAmount());

        return Optional.of(saved);
    }

    /**
     * DailyAggregationDto 데이터를 SettlementDaily에 설정
     */
    private void setDailyAggregationData(SettlementDaily daily, DailyAggregationDto dto) {
        daily.setAggregatedData(
            dto.getTotalOrderCount().intValue(),
            dto.getTotalSalesAmount(),
            dto.getTotalCommissionAmount(),
            dto.getTotalTaxAmount(),
            dto.getTotalPgFeeAmount()
        );
    }

    /**
     * WeeklyAggregationDto 데이터를 SettlementWeekly에 설정
     */
    private void setWeeklyAggregationData(SettlementWeekly weekly, WeeklyAggregationDto dto) {
        weekly.setAggregatedData(
            dto.getTotalOrderCount().intValue(),
            dto.getTotalSalesAmount(),
            dto.getTotalCommissionAmount(),
            dto.getTotalTaxAmount(),
            dto.getTotalPgFeeAmount()
        );
    }

    /**
     * MonthlyAggregationDto 데이터를 SettlementMonthly에 설정
     */
    private void setMonthlyAggregationData(SettlementMonthly monthly, MonthlyAggregationDto dto) {
        monthly.setAggregatedData(
            dto.getTotalOrderCount().intValue(),
            dto.getTotalSalesAmount(),
            dto.getTotalCommissionAmount(),
            dto.getTotalTaxAmount(),
            dto.getTotalPgFeeAmount()
        );
    }

    /**
     * YearlyAggregationDto 데이터를 SettlementYearly에 설정
     */
    private void setYearlyAggregationData(SettlementYearly yearly, YearlyAggregationDto dto) {
        yearly.setAggregatedData(
            dto.getTotalOrderCount().intValue(),
            dto.getTotalSalesAmount(),
            dto.getTotalCommissionAmount(),
            dto.getTotalTaxAmount(),
            dto.getTotalPgFeeAmount()
        );
    }
}