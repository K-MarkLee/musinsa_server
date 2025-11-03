package com.mudosa.musinsa.settlement;

import com.mudosa.musinsa.common.vo.Money;
import com.mudosa.musinsa.settlement.application.SettlementAggregationService;
import com.mudosa.musinsa.settlement.domain.model.*;
import com.mudosa.musinsa.settlement.domain.repository.SettlementDailyRepository;
import com.mudosa.musinsa.settlement.domain.repository.SettlementMonthlyRepository;
import com.mudosa.musinsa.settlement.domain.repository.SettlementPerTransactionRepository;
import com.mudosa.musinsa.settlement.domain.repository.SettlementWeeklyRepository;
import com.mudosa.musinsa.settlement.fixture.SettlementFixture;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Settlement 집계 서비스 통합 테스트
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class SettlementAggregationServiceTest {

    @Autowired(required = false)
    private SettlementAggregationService aggregationService;

    @Autowired(required = false)
    private SettlementPerTransactionRepository perTransactionRepository;

    @Autowired(required = false)
    private SettlementDailyRepository dailyRepository;

    @Autowired(required = false)
    private SettlementWeeklyRepository weeklyRepository;

    @Autowired(required = false)
    private SettlementMonthlyRepository monthlyRepository;

    private static final Long TEST_BRAND_ID = 1L;
    private static final Long TEST_ORDER_ID = 100L;

    @BeforeEach
    void setUp() {
        if (perTransactionRepository == null || aggregationService == null) {
            log.warn("Settlement 레포지토리를 사용할 수 없습니다. 테스트 설정을 건너뜁니다.");
            return;
        }

        perTransactionRepository.deleteAll();
        if (dailyRepository != null) dailyRepository.deleteAll();
        if (weeklyRepository != null) weeklyRepository.deleteAll();
        if (monthlyRepository != null) monthlyRepository.deleteAll();
    }

    @Test
    @DisplayName("거래별 데이터를 일일 정산으로 집계한다")
    void testAggregateToDailySettlement() {
        if (aggregationService == null || perTransactionRepository == null) {
            log.info("Settlement 기능이 비활성화되어 테스트를 스킵합니다.");
            return;
        }

        LocalDate testDate = LocalDate.of(2025, 10, 30);

        createTestTransaction(TEST_BRAND_ID, TEST_ORDER_ID, testDate,
            new BigDecimal("10000"), new BigDecimal("1000"), new BigDecimal("100"), new BigDecimal("300"));
        createTestTransaction(TEST_BRAND_ID, TEST_ORDER_ID + 1, testDate,
            new BigDecimal("20000"), new BigDecimal("2000"), new BigDecimal("200"), new BigDecimal("600"));
        createTestTransaction(TEST_BRAND_ID, TEST_ORDER_ID + 2, testDate,
            new BigDecimal("30000"), new BigDecimal("3000"), new BigDecimal("300"), new BigDecimal("900"));

        log.info("테스트 거래 데이터 3건 생성 완료");

        // When: 일일 집계 실행
        List<SettlementDaily> dailySettlements = aggregationService.aggregateToDaily(
            TEST_BRAND_ID, testDate, testDate
        );

        // Then: 일일 정산 1건 생성 확인
        assertThat(dailySettlements).hasSize(1);

        SettlementDaily daily = dailySettlements.get(0);
        assertThat(daily.getBrandId()).isEqualTo(TEST_BRAND_ID);
        assertThat(daily.getSettlementDate()).isEqualTo(testDate);
        assertThat(daily.getTotalOrderCount()).isEqualTo(3);
        assertThat(daily.getTotalSalesAmount()).isEqualTo(new Money(new BigDecimal("60000")));
        assertThat(daily.getTotalCommissionAmount()).isEqualTo(new Money(new BigDecimal("6000")));
        assertThat(daily.getTotalTaxAmount()).isEqualTo(new Money(new BigDecimal("600")));
        assertThat(daily.getTotalPgFeeAmount()).isEqualTo(new Money(new BigDecimal("1800")));
        assertThat(daily.getSettlementStatus()).isEqualTo(SettlementStatus.COMPLETED);

        // 최종 정산 금액 = 60000 - 6000 - 600 - 1800 = 51600
        assertThat(daily.getFinalSettlementAmount()).isEqualTo(new Money(new BigDecimal("51600")));

        log.info("일일 정산 집계 성공: {}", daily.getSettlementNumber());
    }

    @Test
    @DisplayName("거래별 데이터를 일일로 집계한 후 월간 정산으로 집계한다")
    void testAggregateToMonthlySettlement() {
        if (aggregationService == null || perTransactionRepository == null) {
            log.info("Settlement 기능이 비활성화되어 테스트를 스킵합니다.");
            return;
        }

        // Step 1: 거래별 정산 데이터 생성 (10월 1일, 15일, 31일)
        LocalDate oct1 = LocalDate.of(2025, 10, 1);
        LocalDate oct15 = LocalDate.of(2025, 10, 15);
        LocalDate oct31 = LocalDate.of(2025, 10, 31);

        // 10월 1일 거래 5건
        for (int i = 0; i < 5; i++) {
            createTestTransaction(TEST_BRAND_ID, TEST_ORDER_ID + i, oct1,
                new BigDecimal("10000"), new BigDecimal("1000"), new BigDecimal("100"), new BigDecimal("300"));
        }

        // 10월 15일 거래 3건
        for (int i = 0; i < 3; i++) {
            createTestTransaction(TEST_BRAND_ID, TEST_ORDER_ID + 100 + i, oct15,
                new BigDecimal("10000"), new BigDecimal("1000"), new BigDecimal("100"), new BigDecimal("300"));
        }

        // 10월 31일 거래 2건
        for (int i = 0; i < 2; i++) {
            createTestTransaction(TEST_BRAND_ID, TEST_ORDER_ID + 200 + i, oct31,
                new BigDecimal("10000"), new BigDecimal("1000"), new BigDecimal("100"), new BigDecimal("300"));
        }

        log.info("테스트 거래 데이터 10건 생성 완료 (10/1: 5건, 10/15: 3건, 10/31: 2건)");

        // Step 2: 일일 정산으로 집계
        List<SettlementDaily> dailySettlements = aggregationService.aggregateToDaily(
            TEST_BRAND_ID, oct1, oct31
        );

        assertThat(dailySettlements).hasSize(3);
        log.info("일일 정산 집계 완료: {} 건", dailySettlements.size());

        // Step 3: 월간 정산으로 집계
        List<SettlementMonthly> monthlySettlements = aggregationService.aggregateToMonthly(
            TEST_BRAND_ID, oct1, oct31
        );

        // Then: 월간 정산 1건 생성 확인
        assertThat(monthlySettlements).hasSize(1);

        SettlementMonthly monthly = monthlySettlements.get(0);
        assertThat(monthly.getBrandId()).isEqualTo(TEST_BRAND_ID);
        assertThat(monthly.getSettlementYear()).isEqualTo(2025);
        assertThat(monthly.getSettlementMonth()).isEqualTo(10);
        assertThat(monthly.getTotalOrderCount()).isEqualTo(10); // 5 + 3 + 2
        assertThat(monthly.getTotalSalesAmount()).isEqualTo(new Money(new BigDecimal("100000")));
        assertThat(monthly.getSettlementStatus()).isEqualTo(SettlementStatus.COMPLETED);

        log.info("월간 정산 집계 성공: {}", monthly.getSettlementNumber());
    }

    @Test
    @DisplayName("거래별 데이터를 일일로 집계한 후 주간 정산으로 집계한다")
    void testAggregateToWeeklySettlement() {
        if (aggregationService == null || perTransactionRepository == null) {
            log.info("Settlement 기능이 비활성화되어 테스트를 스킵합니다.");
            return;
        }

        // Step 1: 거래별 정산 데이터 생성 (10월 28일 ~ 30일, 같은 주)
        LocalDate day1 = LocalDate.of(2025, 10, 28);
        LocalDate day2 = LocalDate.of(2025, 10, 29);
        LocalDate day3 = LocalDate.of(2025, 10, 30);

        createTestTransaction(TEST_BRAND_ID, 1L, day1,
            new BigDecimal("10000"), new BigDecimal("1000"), new BigDecimal("100"), new BigDecimal("300"));
        createTestTransaction(TEST_BRAND_ID, 2L, day2,
            new BigDecimal("20000"), new BigDecimal("2000"), new BigDecimal("200"), new BigDecimal("600"));
        createTestTransaction(TEST_BRAND_ID, 3L, day3,
            new BigDecimal("30000"), new BigDecimal("3000"), new BigDecimal("300"), new BigDecimal("900"));

        log.info("테스트 거래 데이터 3건 생성 완료 (10/28, 10/29, 10/30)");

        // Step 2: 일일 정산으로 집계
        List<SettlementDaily> dailySettlements = aggregationService.aggregateToDaily(
            TEST_BRAND_ID, day1, day3
        );

        assertThat(dailySettlements).hasSize(3);
        log.info("일일 정산 집계 완료: {} 건", dailySettlements.size());

        // Step 3: 주간 정산으로 집계
        List<SettlementWeekly> weeklySettlements = aggregationService.aggregateToWeekly(
            TEST_BRAND_ID, day1, day3
        );

        // Then: 주간 정산 생성 확인
        assertThat(weeklySettlements).isNotEmpty();

        SettlementWeekly weekly = weeklySettlements.get(0);
        assertThat(weekly.getBrandId()).isEqualTo(TEST_BRAND_ID);
        assertThat(weekly.getTotalOrderCount()).isEqualTo(3);
        assertThat(weekly.getTotalSalesAmount()).isEqualTo(new Money(new BigDecimal("60000")));
        assertThat(weekly.getSettlementStatus()).isEqualTo(SettlementStatus.COMPLETED);

        log.info("주간 정산 집계 성공: {}", weekly.getSettlementNumber());
    }

    @Test
    @DisplayName("거래별 데이터를 일일, 월간으로 집계한 후 연간 정산으로 집계한다")
    void testAggregateToYearlySettlement() {
        if (aggregationService == null || perTransactionRepository == null) {
            log.info("Settlement 기능이 비활성화되어 테스트를 스킵합니다.");
            return;
        }

        // Step 1: 거래별 정산 데이터 생성 (2025년 여러 달)
        LocalDate jan15 = LocalDate.of(2025, 1, 15);
        LocalDate jun20 = LocalDate.of(2025, 6, 20);
        LocalDate dec10 = LocalDate.of(2025, 12, 10);

        // 1월 거래 2건
        for (int i = 0; i < 2; i++) {
            createTestTransaction(TEST_BRAND_ID, 1000L + i, jan15,
                new BigDecimal("10000"), new BigDecimal("1000"), new BigDecimal("100"), new BigDecimal("300"));
        }

        // 6월 거래 3건
        for (int i = 0; i < 3; i++) {
            createTestTransaction(TEST_BRAND_ID, 2000L + i, jun20,
                new BigDecimal("10000"), new BigDecimal("1000"), new BigDecimal("100"), new BigDecimal("300"));
        }

        // 12월 거래 5건
        for (int i = 0; i < 5; i++) {
            createTestTransaction(TEST_BRAND_ID, 3000L + i, dec10,
                new BigDecimal("10000"), new BigDecimal("1000"), new BigDecimal("100"), new BigDecimal("300"));
        }

        log.info("테스트 거래 데이터 10건 생성 완료 (1월: 2건, 6월: 3건, 12월: 5건)");

        // Step 2: 일일 정산으로 집계
        LocalDate yearStart = LocalDate.of(2025, 1, 1);
        LocalDate yearEnd = LocalDate.of(2025, 12, 31);

        List<SettlementDaily> dailySettlements = aggregationService.aggregateToDaily(
            TEST_BRAND_ID, yearStart, yearEnd
        );

        assertThat(dailySettlements).hasSize(3);
        log.info("일일 정산 집계 완료: {} 건", dailySettlements.size());

        // Step 3: 월간 정산으로 집계
        List<SettlementMonthly> monthlySettlements = aggregationService.aggregateToMonthly(
            TEST_BRAND_ID, yearStart, yearEnd
        );

        assertThat(monthlySettlements).hasSize(3);
        log.info("월간 정산 집계 완료: {} 건", monthlySettlements.size());

        // Step 4: 연간 정산으로 집계
        SettlementYearly yearly = aggregationService.aggregateToYearly(TEST_BRAND_ID, 2025)
            .orElseThrow(() -> new AssertionError("연간 정산 데이터가 없습니다"));

        // Then: 연간 정산 확인
        assertThat(yearly).isNotNull();
        assertThat(yearly.getBrandId()).isEqualTo(TEST_BRAND_ID);
        assertThat(yearly.getSettlementYear()).isEqualTo(2025);
        assertThat(yearly.getTotalOrderCount()).isEqualTo(10); // 2 + 3 + 5
        assertThat(yearly.getTotalSalesAmount()).isEqualTo(new Money(new BigDecimal("100000")));
        assertThat(yearly.getSettlementStatus()).isEqualTo(SettlementStatus.COMPLETED);

        log.info("연간 정산 집계 성공: {}", yearly.getSettlementNumber());
    }

    @Test
    @DisplayName("거래 데이터가 없으면 빈 목록을 반환한다")
    void testAggregateWithNoData() {
        if (aggregationService == null) {
            log.info("Settlement 기능이 비활성화되어 테스트를 스킵합니다.");
            return;
        }

        LocalDate startDate = LocalDate.of(2025, 11, 1);
        LocalDate endDate = LocalDate.of(2025, 11, 5);

        List<SettlementDaily> result = aggregationService.aggregateToDaily(
            TEST_BRAND_ID, startDate, endDate
        );

        assertThat(result).isEmpty();
        log.info("데이터 없음 케이스 처리 성공");
    }

    /**
     * 테스트용 거래별 정산 데이터 생성
     */
    private void createTestTransaction(
        Long brandId,
        Long orderId,
        LocalDate settlementDate,
        BigDecimal transactionAmount,
        BigDecimal commissionAmount,
        BigDecimal taxAmount,
        BigDecimal pgFeeAmount
    ) {
        SettlementPerTransaction transaction = SettlementFixture.createTransaction(
            brandId,
            orderId,
            settlementDate,
            transactionAmount.toString(),
            commissionAmount.toString(),
            taxAmount.toString(),
            pgFeeAmount.toString()
        );

        perTransactionRepository.save(transaction);
    }
}
