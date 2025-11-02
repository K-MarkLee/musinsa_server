package com.mudosa.musinsa.settlement.domain.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 정산 번호 생성
 */
@Component
@RequiredArgsConstructor
public class SettlementNumberGenerator {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * 일일 정산 번호 생성
     * 형식: DAILY-YYYYMMDD-XXXXX
     * 예시: DAILY-20250127-00001
     */
    public String generateDailyNumber(LocalDate date) {
        Long sequence = getNextSequence("daily_settlement_seq");
        String dateStr = date.toString().replace("-", "");
        return String.format("DAILY-%s-%05d", dateStr, sequence);
    }

    /**
     * 주간 정산 번호 생성
     * 형식: WEEKLY-YYYYWXX-XXXXX
     * 예시: WEEKLY-2025W04-00001
     */
    public String generateWeeklyNumber(int year, int week) {
        Long sequence = getNextSequence("weekly_settlement_seq");
        return String.format("WEEKLY-%dW%02d-%05d", year, week, sequence);
    }

    /**
     * 월간 정산 번호 생성
     * 형식: MONTHLY-YYYYMM-XXXXX
     * 예시: MONTHLY-202501-00001
     */
    public String generateMonthlyNumber(int year, int month) {
        Long sequence = getNextSequence("monthly_settlement_seq");
        return String.format("MONTHLY-%d%02d-%05d", year, month, sequence);
    }

    /**
     * 연간 정산 번호 생성
     * 형식: YEARLY-YYYY-XXXXX
     * 예시: YEARLY-2025-00001
     */
    public String generateYearlyNumber(int year) {
        Long sequence = getNextSequence("yearly_settlement_seq");
        return String.format("YEARLY-%d-%05d", year, sequence);
    }

    /**
     * DB 시퀀스에서 다음 값 조회 (MySQL용 - AUTO_INCREMENT 시뮬레이션)
     * 간단한 타임스탬프 기반 번호 생성으로 대체
     */
    private Long getNextSequence(String sequenceName) {
        // MySQL에는 시퀀스가 없으므로 간단히 현재 시간 밀리초 사용
        // 실제 운영에서는 별도 시퀀스 테이블 또는 Redis 등 사용 권장
        return System.currentTimeMillis() % 100000; // 5자리 숫자
    }
}
