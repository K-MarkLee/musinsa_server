package com.mudosa.musinsa.settlement.application;

import com.mudosa.musinsa.brand.domain.model.Brand;
import com.mudosa.musinsa.brand.domain.repository.BrandRepository;
import com.mudosa.musinsa.common.vo.Money;
import com.mudosa.musinsa.exception.BusinessException;
import com.mudosa.musinsa.exception.ErrorCode;
import com.mudosa.musinsa.settlement.domain.model.SettlementDaily;
import com.mudosa.musinsa.settlement.domain.model.SettlementMonthly;
import com.mudosa.musinsa.settlement.domain.model.SettlementWeekly;
import com.mudosa.musinsa.settlement.domain.model.SettlementYearly;
import com.mudosa.musinsa.settlement.domain.model.SettlementPerTransaction;
import com.mudosa.musinsa.settlement.domain.repository.SettlementDailyRepository;
import com.mudosa.musinsa.settlement.domain.repository.SettlementMonthlyRepository;
import com.mudosa.musinsa.settlement.domain.repository.SettlementPerTransactionRepository;
import com.mudosa.musinsa.settlement.domain.repository.SettlementWeeklyRepository;
import com.mudosa.musinsa.settlement.domain.repository.SettlementYearlyRepository;
import com.mudosa.musinsa.settlement.presentation.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * 정산 조회 온니
 *
 * 프론트엔드에 정산 데이터를 제공
 * SettlementController API 호출 시 실행
 *
 * - @Transactional(readOnly = true)
 * - 페이징
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SettlementQueryService {

    private static final int WEEK_LOOKUP_DAYS = 7;
    private static final int STATISTICS_PAGE_SIZE = 1;

    private final SettlementDailyRepository dailyRepository;
    private final SettlementWeeklyRepository weeklyRepository;
    private final SettlementMonthlyRepository monthlyRepository;
    private final SettlementYearlyRepository yearlyRepository;
    private final SettlementPerTransactionRepository perTransactionRepository;
    private final BrandRepository brandRepository;

    /* 일일 정산 목록 조회 (페이징) */
    public Page<SettlementDailyResponse> getDailySettlements(Long brandId, Pageable pageable) {
        log.info("일일 정산 목록 조회 - brandId: {}", brandId);

        Page<SettlementDaily> settlements;

        if (brandId == null) {
            // brandId가 없으면 모든 브랜드 조회
            settlements = dailyRepository.findAll(pageable);
        } else {
            settlements = dailyRepository.findByBrandIdOrderBySettlementDateDesc(brandId, pageable);
        }

        return settlements.map(settlement -> {
            String brandName = getBrandName(settlement.getBrandId());
            return SettlementDailyResponse.from(settlement, brandName);
        });
    }

    /* 일일 정산 상세 조회 */
    public SettlementDailyResponse getDailySettlement(Long settlementDailyId) {
        log.info("일일 정산 상세 조회 - id: {}", settlementDailyId);

        SettlementDaily settlement = dailyRepository.findById(settlementDailyId)
            .orElseThrow(() -> new BusinessException(ErrorCode.SETTLEMENT_NOT_FOUND));

        String brandName = getBrandName(settlement.getBrandId());

        return SettlementDailyResponse.from(settlement, brandName);
    }

    /* 주간 정산 목록 조회 (페이징) */
    public Page<SettlementWeeklyResponse> getWeeklySettlements(Long brandId, Pageable pageable) {
        log.info("주간 정산 목록 조회 - brandId: {}", brandId);

        Page<SettlementWeekly> settlements = weeklyRepository.findByBrandIdOrderByWeekStartDateDesc(brandId, pageable);

        String brandName = getBrandName(brandId);

        return settlements.map(settlement -> SettlementWeeklyResponse.from(settlement, brandName));
    }

    /* 주간 정산 상세 조회 */
    public SettlementWeeklyResponse getWeeklySettlement(Long settlementWeeklyId) {
        log.info("주간 정산 상세 조회 - id: {}", settlementWeeklyId);

        SettlementWeekly settlement = weeklyRepository.findById(settlementWeeklyId)
            .orElseThrow(() -> new BusinessException(ErrorCode.SETTLEMENT_NOT_FOUND));

        String brandName = getBrandName(settlement.getBrandId());

        return SettlementWeeklyResponse.from(settlement, brandName);
    }

    /* 월간 정산 목록 조회 (페이징) */
    public Page<SettlementMonthlyResponse> getMonthlySettlements(Long brandId, Pageable pageable) {
        log.info("월간 정산 목록 조회 - brandId: {}", brandId);

        Page<SettlementMonthly> settlements;

        if (brandId == null) {
            // brandId가 없으면 모든 브랜드 조회
            settlements = monthlyRepository.findAll(pageable);
        } else {
            settlements = monthlyRepository.findByBrandIdOrderBySettlementYearDescSettlementMonthDesc(brandId, pageable);
        }

        return settlements.map(settlement -> {
            String brandName = getBrandName(settlement.getBrandId());
            return SettlementMonthlyResponse.from(settlement, brandName);
        });
    }

    /* 월간 정산 상세 조회 */
    public SettlementMonthlyResponse getMonthlySettlement(Long settlementMonthlyId) {
        log.info("월간 정산 상세 조회 - id: {}", settlementMonthlyId);

        SettlementMonthly settlement = monthlyRepository.findById(settlementMonthlyId)
            .orElseThrow(() -> new BusinessException(ErrorCode.SETTLEMENT_NOT_FOUND));

        String brandName = getBrandName(settlement.getBrandId());

        return SettlementMonthlyResponse.from(settlement, brandName);
    }

    /* 연간 정산 목록 조회 (페이징) */
    public Page<SettlementYearlyResponse> getYearlySettlements(Long brandId, Pageable pageable) {
        log.info("연간 정산 목록 조회 - brandId: {}", brandId);

        Page<SettlementYearly> settlements = yearlyRepository.findByBrandIdOrderBySettlementYearDesc(brandId, pageable);

        String brandName = getBrandName(brandId);

        return settlements.map(settlement -> SettlementYearlyResponse.from(settlement, brandName));
    }

    /* 연간 정산 상세 조회 */
    public SettlementYearlyResponse getYearlySettlement(Long settlementYearlyId) {
        log.info("연간 정산 상세 조회 - id: {}", settlementYearlyId);

        SettlementYearly settlement = yearlyRepository.findById(settlementYearlyId)
            .orElseThrow(() -> new BusinessException(ErrorCode.SETTLEMENT_NOT_FOUND));

        String brandName = getBrandName(settlement.getBrandId());

        return SettlementYearlyResponse.from(settlement, brandName);
    }

    /* 거래별 정산 목록 조회 (페이징) */
    public Page<SettlementPerTransactionResponse> getPerTransactionSettlements(
        Long brandId,
        LocalDate startDate,
        LocalDate endDate,
        Pageable pageable
    ) {
        log.info("거래별 정산 목록 조회 - brandId: {}, startDate: {}, endDate: {}",
            brandId, startDate, endDate);

        List<SettlementPerTransaction> transactions = perTransactionRepository
            .findByBrandIdAndTransactionDateLocalBetween(brandId, startDate, endDate);

        String brandName = getBrandName(brandId);

        // 리스트를 Page로 변환 (페이징 적용)
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), transactions.size());

        List<SettlementPerTransactionResponse> responseList = transactions.subList(start, end)
            .stream()
            .map(tx -> SettlementPerTransactionResponse.from(tx, brandName))
            .collect(Collectors.toList());

        return new PageImpl<>(responseList, pageable, transactions.size());
    }

    /* 거래별 정산 단건 조회 */
    public SettlementPerTransactionResponse getPerTransactionSettlement(Long transactionId) {
        log.info("거래별 정산 단건 조회 - id: {}", transactionId);

        SettlementPerTransaction transaction = perTransactionRepository.findById(transactionId)
            .orElseThrow(() -> new BusinessException(ErrorCode.SETTLEMENT_NOT_FOUND));

        String brandName = getBrandName(transaction.getBrandId());

        return SettlementPerTransactionResponse.from(transaction, brandName);
    }

    /* 정산 통계 조회 */
    public SettlementStatisticsResponse getStatistics(Long brandId) {
        log.info("정산 통계 조회 - brandId: {}", brandId);

        String brandName = (brandId == null) ? "전체 브랜드" : getBrandName(brandId);
        LocalDate today = LocalDate.now();
        int currentYear = today.getYear();
        int currentMonth = today.getMonthValue();

        SettlementStatistics todayStats = getTodayStatistics(brandId, today);
        SettlementStatistics weekStats = getWeekStatistics(brandId, today);
        SettlementStatistics monthStats = getMonthStatistics(brandId, currentYear, currentMonth);
        SettlementStatistics yearStats = getYearStatistics(brandId, currentYear);
        SettlementStatistics totalStats = getTotalStatistics(brandId);

        return SettlementStatisticsResponse.of(
            brandId != null ? brandId : 0L,
            brandName,
            todayStats.salesAmount,
            todayStats.orderCount,
            weekStats.salesAmount,
            weekStats.orderCount,
            monthStats.salesAmount,
            monthStats.orderCount,
            yearStats.salesAmount,
            yearStats.orderCount,
            totalStats.salesAmount,
            totalStats.orderCount
        );
    }

    /* 오늘 통계 조회 */
    private SettlementStatistics getTodayStatistics(Long brandId, LocalDate today) {
        if (brandId == null) {
            // 전체 브랜드의 오늘 통계 합산
            var allDailySettlements = dailyRepository.findAll().stream()
                .filter(s -> s.getSettlementDate().equals(today))
                .toList();

            Money totalSales = allDailySettlements.stream()
                .map(SettlementDaily::getTotalSalesAmount)
                .reduce(Money.ZERO, Money::add);

            Integer totalOrderCount = allDailySettlements.stream()
                .map(SettlementDaily::getTotalOrderCount)
                .reduce(0, Integer::sum);

            return new SettlementStatistics(totalSales, totalOrderCount);
        }

        return dailyRepository
            .findByBrandIdAndSettlementDate(brandId, today)
            .map(SettlementStatistics::from)
            .orElseGet(SettlementStatistics::empty);
    }

    /* 이번 주 통계 조회 */
    private SettlementStatistics getWeekStatistics(Long brandId, LocalDate today) {
        if (brandId == null) {
            // 전체 브랜드의 이번 주 통계 합산
            var allWeeklySettlements = weeklyRepository.findAll().stream()
                .filter(s -> !s.getWeekStartDate().isBefore(today.minusDays(WEEK_LOOKUP_DAYS))
                    && !s.getWeekStartDate().isAfter(today))
                .toList();

            Money totalSales = allWeeklySettlements.stream()
                .map(SettlementWeekly::getTotalSalesAmount)
                .reduce(Money.ZERO, Money::add);

            Integer totalOrderCount = allWeeklySettlements.stream()
                .map(SettlementWeekly::getTotalOrderCount)
                .reduce(0, Integer::sum);

            return new SettlementStatistics(totalSales, totalOrderCount);
        }

        return weeklyRepository
            .findByBrandIdAndWeekStartDateBetween(
                brandId,
                today.minusDays(WEEK_LOOKUP_DAYS),
                today,
                Pageable.ofSize(STATISTICS_PAGE_SIZE)
            )
            .getContent()
            .stream()
            .findFirst()
            .map(SettlementStatistics::from)
            .orElseGet(SettlementStatistics::empty);
    }

    /* 이번 달 통계 조회 */
    private SettlementStatistics getMonthStatistics(Long brandId, int year, int month) {
        if (brandId == null) {
            // 전체 브랜드의 이번 달 통계 합산
            var allMonthlySettlements = monthlyRepository.findAll().stream()
                .filter(s -> s.getSettlementYear() == year && s.getSettlementMonth() == month)
                .toList();

            Money totalSales = allMonthlySettlements.stream()
                .map(SettlementMonthly::getTotalSalesAmount)
                .reduce(Money.ZERO, Money::add);

            Integer totalOrderCount = allMonthlySettlements.stream()
                .map(SettlementMonthly::getTotalOrderCount)
                .reduce(0, Integer::sum);

            return new SettlementStatistics(totalSales, totalOrderCount);
        }

        return monthlyRepository
            .findByBrandIdAndSettlementYearAndSettlementMonth(brandId, year, month)
            .map(SettlementStatistics::from)
            .orElseGet(SettlementStatistics::empty);
    }

    /* 올해 통계 조회 */
    private SettlementStatistics getYearStatistics(Long brandId, int year) {
        if (brandId == null) {
            // 전체 브랜드의 올해 통계 합산
            var allYearlySettlements = yearlyRepository.findAll().stream()
                .filter(s -> s.getSettlementYear() == year)
                .toList();

            Money totalSales = allYearlySettlements.stream()
                .map(SettlementYearly::getTotalSalesAmount)
                .reduce(Money.ZERO, Money::add);

            Integer totalOrderCount = allYearlySettlements.stream()
                .map(SettlementYearly::getTotalOrderCount)
                .reduce(0, Integer::sum);

            return new SettlementStatistics(totalSales, totalOrderCount);
        }

        return yearlyRepository
            .findByBrandIdAndSettlementYear(brandId, year)
            .map(SettlementStatistics::from)
            .orElseGet(SettlementStatistics::empty);
    }

    /* 전체 통계 조회 (모든 연도 합산) */
    private SettlementStatistics getTotalStatistics(Long brandId) {
        var allYearlySettlements = (brandId == null)
            ? yearlyRepository.findAll()
            : yearlyRepository.findByBrandId(brandId);

        Money totalSales = allYearlySettlements.stream()
            .map(SettlementYearly::getTotalSalesAmount)
            .reduce(Money.ZERO, Money::add);

        Integer totalOrderCount = allYearlySettlements.stream()
            .map(SettlementYearly::getTotalOrderCount)
            .reduce(0, Integer::sum);

        return new SettlementStatistics(totalSales, totalOrderCount);
    }

    /* 정산 통계 데이터 홀더 */
    private static class SettlementStatistics {
        private final Money salesAmount;
        private final Integer orderCount;

        private SettlementStatistics(Money salesAmount, Integer orderCount) {
            this.salesAmount = salesAmount;
            this.orderCount = orderCount;
        }

        static SettlementStatistics from(SettlementDaily daily) {
            return new SettlementStatistics(
                daily.getTotalSalesAmount(),
                daily.getTotalOrderCount()
            );
        }

        static SettlementStatistics from(SettlementWeekly weekly) {
            return new SettlementStatistics(
                weekly.getTotalSalesAmount(),
                weekly.getTotalOrderCount()
            );
        }

        static SettlementStatistics from(SettlementMonthly monthly) {
            return new SettlementStatistics(
                monthly.getTotalSalesAmount(),
                monthly.getTotalOrderCount()
            );
        }

        static SettlementStatistics from(SettlementYearly yearly) {
            return new SettlementStatistics(
                yearly.getTotalSalesAmount(),
                yearly.getTotalOrderCount()
            );
        }

        static SettlementStatistics empty() {
            return new SettlementStatistics(Money.ZERO, 0);
        }
    }

    /* 브랜드명 조회 */
    private String getBrandName(Long brandId) {
        Brand brand = brandRepository.findById(brandId)
            .orElseThrow(() -> new BusinessException(ErrorCode.BRAND_NOT_FOUND));

        return brand.getNameKo();
    }
}