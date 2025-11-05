package com.mudosa.musinsa.settlement.application;

import com.mudosa.musinsa.settlement.batch.dto.DailyAggregationDto;
import com.mudosa.musinsa.settlement.batch.dto.MonthlyAggregationDto;
import com.mudosa.musinsa.settlement.batch.dto.WeeklyAggregationDto;
import com.mudosa.musinsa.settlement.batch.dto.YearlyAggregationDto;
import com.mudosa.musinsa.settlement.domain.model.SettlementDaily;
import com.mudosa.musinsa.settlement.domain.model.SettlementMonthly;
import com.mudosa.musinsa.settlement.domain.model.SettlementWeekly;
import com.mudosa.musinsa.settlement.domain.model.SettlementYearly;
import com.mudosa.musinsa.settlement.domain.repository.SettlementDailyMapper;
import com.mudosa.musinsa.settlement.domain.repository.SettlementDailyRepository;
import com.mudosa.musinsa.settlement.domain.repository.SettlementMonthlyMapper;
import com.mudosa.musinsa.settlement.domain.repository.SettlementMonthlyRepository;
import com.mudosa.musinsa.settlement.domain.repository.SettlementPerTransactionMapper;
import com.mudosa.musinsa.settlement.domain.repository.SettlementPerTransactionRepository;
import com.mudosa.musinsa.settlement.domain.repository.SettlementWeeklyRepository;
import com.mudosa.musinsa.settlement.domain.repository.SettlementYearlyRepository;
import com.mudosa.musinsa.settlement.domain.service.SettlementNumberGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 정산 집계 서비스
 *
 * 정산 데이터를 단계별로 집계하여 통계 테이블에 저장
 * Spring Batch Job에서 자동 실행 or 수동 API 호출로 실행됨당
 *
 * 계층적 집계:
 * 1. 거래별 → 일일 집계 (SettlementPerTransaction → SettlementDaily)
 * 2. 일일 → 주간 집계 (SettlementDaily → SettlementWeekly)
 * 3. 일일 → 월간 집계 (SettlementDaily → SettlementMonthly)
 * 4. 월간 → 연간 집계 (SettlementMonthly → SettlementYearly)
 *
 * 호출 위치:
 * - DailySettlementAggregationJob (일일 배치)
 * - WeeklySettlementAggregationJob (주간 배치)
 * - MonthlySettlementAggregationJob (월간 배치)
 * - YearlySettlementAggregationJob (연간 배치)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SettlementAggregationService {

    private final SettlementPerTransactionRepository perTransactionRepository;
    private final SettlementPerTransactionMapper perTransactionMapper;
    private final SettlementDailyRepository dailyRepository;
    private final SettlementDailyMapper dailyMapper;
    private final SettlementWeeklyRepository weeklyRepository;
    private final SettlementMonthlyRepository monthlyRepository;
    private final SettlementMonthlyMapper monthlyMapper;
    private final SettlementYearlyRepository yearlyRepository;
    private final SettlementNumberGenerator settlementNumberGenerator;

    /**
     * 거래별 정산 데이터 -> 일일 정산으로 집계
     *
     * @param brandId 브랜드 ID
     * @param startDate 시작일
     * @param endDate 종료일
     * @return 생성된 일일 정산 목록
     */
    @Transactional
    public List<SettlementDaily> aggregateToDaily(Long brandId, LocalDate startDate, LocalDate endDate) {
        log.info("일일 정산 집계 시작: brandId={}, 기간={} ~ {}", brandId, startDate, endDate);

        // settlements_per_transaction 집계
        List<DailyAggregationDto> aggregations = perTransactionMapper.aggregateByDaily(
            brandId, startDate, endDate
        );

        if (aggregations.isEmpty()) {
            log.warn("집계할 거래 데이터가 없습니다");
            return List.of();
        }

        log.info("일일 집계 데이터 {}건 발견", aggregations.size());

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

            log.info("일일 정산 생성 완료: {}, 금액={}",
                saved.getSettlementNumber(), saved.getFinalSettlementAmount());
        }

        log.info("일일 정산 {}건 생성 완료", dailySettlements.size());
        return dailySettlements;
    }

    /**
     * 일일 정산 데이터 -> 주간 정산으로 집계
     *
     * @param brandId 브랜드 ID
     * @param startDate 시작일
     * @param endDate 종료일
     * @return 생성된 주간 정산 목록
     */
    @Transactional
    public List<SettlementWeekly> aggregateToWeekly(Long brandId, LocalDate startDate, LocalDate endDate) {
        log.info("주간 정산 집계 시작: brandId={}, 기간={} ~ {}", brandId, startDate, endDate);

        // settlements_daily 데이터 집계
        List<WeeklyAggregationDto> aggregations = dailyMapper.aggregateByWeekly(brandId, startDate, endDate);

        if (aggregations.isEmpty()) {
            log.warn("주간 집계할 일일 정산 데이터가 없습니다");
            return List.of();
        }

        log.info("주간 집계 데이터 {}건 발견", aggregations.size());

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

            log.info("주간 정산 생성 완료: {}, 금액={}",
                saved.getSettlementNumber(), saved.getFinalSettlementAmount());
        }

        log.info("주간 정산 {}건 생성 완료", weeklySettlements.size());
        return weeklySettlements;
    }

    /**
     * 일일 정산 데이터 -> 월간 정산으로 집계
     *
     * @param brandId 브랜드 ID
     * @param startDate 시작일
     * @param endDate 종료일
     * @return 생성된 월간 정산 목록
     */
    @Transactional
    public List<SettlementMonthly> aggregateToMonthly(Long brandId, LocalDate startDate, LocalDate endDate) {
        log.info("월간 정산 집계 시작: brandId={}, 기간={} ~ {}", brandId, startDate, endDate);

        // settlements_daily 데이터 집계
        List<MonthlyAggregationDto> aggregations = dailyMapper.aggregateByMonthly(brandId, startDate, endDate);

        if (aggregations.isEmpty()) {
            log.warn("월간 집계할 일일 정산 데이터가 없습니다");
            return List.of();
        }

        log.info("월간 집계 데이터 {}건 발견", aggregations.size());

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

            log.info("월간 정산 생성 완료: {}, 금액={}",
                saved.getSettlementNumber(), saved.getFinalSettlementAmount());
        }

        log.info("월간 정산 {}건 생성 완료", monthlySettlements.size());
        return monthlySettlements;
    }

    /**
     * 월간 정산 데이터 -> 연간 정산으로 집계
     *
     * @param brandId 브랜드 ID
     * @param year 연도
     * @return 생성된 연간 정산 (데이터가 없으면 Optional.empty())
     */
    @Transactional
    public Optional<SettlementYearly> aggregateToYearly(Long brandId, int year) {
        log.info("연간 정산 집계 시작: brandId={}, 연도={}", brandId, year);

        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);

        // settlements_monthly 데이터 집계
        List<YearlyAggregationDto> aggregations = monthlyMapper.aggregateByYearly(brandId, startDate, endDate);

        if (aggregations.isEmpty()) {
            log.warn("연간 집계할 월간 정산 데이터가 없습니다");
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

        log.info("연간 정산 생성 완료: {}, 금액={}",
            saved.getSettlementNumber(), saved.getFinalSettlementAmount());

        return Optional.of(saved);
    }

    /* DailyAggregationDto 데이터를 SettlementDaily에 설정 */
    private void setDailyAggregationData(SettlementDaily daily, DailyAggregationDto dto) {
        daily.setAggregatedData(
            dto.getTotalOrderCount().intValue(),
            dto.getTotalSalesAmount(),
            dto.getTotalCommissionAmount(),
            dto.getTotalTaxAmount(),
            dto.getTotalPgFeeAmount()
        );
    }

    /* WeeklyAggregationDto 데이터를 SettlementWeekly에 설정 */
    private void setWeeklyAggregationData(SettlementWeekly weekly, WeeklyAggregationDto dto) {
        weekly.setAggregatedData(
            dto.getTotalOrderCount().intValue(),
            dto.getTotalSalesAmount(),
            dto.getTotalCommissionAmount(),
            dto.getTotalTaxAmount(),
            dto.getTotalPgFeeAmount()
        );
    }

    /* MonthlyAggregationDto 데이터를 SettlementMonthly에 설정 */
    private void setMonthlyAggregationData(SettlementMonthly monthly, MonthlyAggregationDto dto) {
        monthly.setAggregatedData(
            dto.getTotalOrderCount().intValue(),
            dto.getTotalSalesAmount(),
            dto.getTotalCommissionAmount(),
            dto.getTotalTaxAmount(),
            dto.getTotalPgFeeAmount()
        );
    }

    /* YearlyAggregationDto 데이터를 SettlementYearly에 설정 */
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