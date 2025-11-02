package com.mudosa.musinsa.settlement;

import com.mudosa.musinsa.common.vo.Money;
import com.mudosa.musinsa.settlement.domain.model.*;
import com.mudosa.musinsa.settlement.domain.repository.SettlementDailyRepository;
import com.mudosa.musinsa.settlement.domain.repository.SettlementPerTransactionRepository;
import com.mudosa.musinsa.settlement.fixture.SettlementFixture;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Settlement 기본 기능 테스트 (MyBatis 제외)
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class SettlementBasicTest {

    @Autowired(required = false)
    private SettlementPerTransactionRepository perTransactionRepository;

    @Autowired(required = false)
    private SettlementDailyRepository dailyRepository;

    @Test
    @DisplayName("거래별 정산 엔티티를 생성하고 저장할 수 있다")
    void testCreateSettlementPerTransaction() {
        if (perTransactionRepository == null) {
            log.info("Settlement 기능이 비활성화되어 테스트를 스킵합니다.");
            return;
        }

        LocalDate testDate = LocalDate.of(2025, 10, 30);

        SettlementPerTransaction transaction = SettlementFixture.createTransaction(
            1L,
            100L,
            testDate,
            "10000",
            "1000",
            "100",
            "300"
        );

        SettlementPerTransaction saved = perTransactionRepository.save(transaction);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getBrandId()).isEqualTo(1L);
        assertThat(saved.getTransactionAmount()).isEqualTo(new Money(new BigDecimal("10000")));
        assertThat(saved.getCommissionAmount()).isEqualTo(new Money(new BigDecimal("1000")));
        assertThat(saved.calculateFinalSettlementAmount())
            .isEqualTo(new Money(new BigDecimal("8600")));

        log.info("거래별 정산 생성 성공: ID={}", saved.getId());
    }

    @Test
    @DisplayName("일일 정산 엔티티를 생성하고 저장할 수 있다")
    void testCreateSettlementDaily() {
        if (dailyRepository == null) {
            log.info("Settlement 기능이 비활성화되어 테스트를 스킵합니다.");
            return;
        }

        LocalDate testDate = LocalDate.of(2025, 10, 30);

        SettlementDaily daily = SettlementDaily.create(
            1L,
            testDate,
            "DAILY-2025-10-30-001",
            "Asia/Seoul"
        );

        daily.setAggregatedData(
            5,
            new Money(new BigDecimal("50000")),
            new Money(new BigDecimal("5000")),
            new Money(new BigDecimal("500")),
            new Money(new BigDecimal("1500"))
        );

        daily.startProcessing();
        daily.complete();

        SettlementDaily saved = dailyRepository.save(daily);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getBrandId()).isEqualTo(1L);
        assertThat(saved.getSettlementDate()).isEqualTo(testDate);
        assertThat(saved.getTotalOrderCount()).isEqualTo(5);
        assertThat(saved.getTotalSalesAmount()).isEqualTo(new Money(new BigDecimal("50000")));
        assertThat(saved.getSettlementStatus()).isEqualTo(SettlementStatus.COMPLETED);
        assertThat(saved.getFinalSettlementAmount())
            .isEqualTo(new Money(new BigDecimal("43000")));

        log.info("일일 정산 생성 성공: {}", saved.getSettlementNumber());
    }

    @Test
    @DisplayName("일일 정산에 거래를 추가할 수 있다")
    void testAddTransactionToDaily() {
        if (dailyRepository == null || perTransactionRepository == null) {
            log.info("Settlement 기능이 비활성화되어 테스트를 스킵합니다.");
            return;
        }

        LocalDate testDate = LocalDate.of(2025, 10, 30);

        SettlementPerTransaction transaction1 = SettlementFixture.createTransaction(1L, 101L, testDate, "10000", "1000", "100", "300");
        SettlementPerTransaction transaction2 = SettlementFixture.createTransaction(1L, 102L, testDate, "20000", "2000", "200", "600");

        perTransactionRepository.save(transaction1);
        perTransactionRepository.save(transaction2);

        SettlementDaily daily = SettlementDaily.create(1L, testDate, "DAILY-TEST", "Asia/Seoul");

        daily.addTransaction(transaction1);
        daily.addTransaction(transaction2);

        SettlementDaily saved = dailyRepository.save(daily);

        assertThat(saved.getTotalOrderCount()).isEqualTo(2);
        assertThat(saved.getTotalSalesAmount()).isEqualTo(new Money(new BigDecimal("30000")));
        assertThat(saved.getTotalCommissionAmount()).isEqualTo(new Money(new BigDecimal("3000")));

        log.info("거래 추가 성공: 주문 {}건, 총액 {}",
            saved.getTotalOrderCount(), saved.getTotalSalesAmount());
    }

}
