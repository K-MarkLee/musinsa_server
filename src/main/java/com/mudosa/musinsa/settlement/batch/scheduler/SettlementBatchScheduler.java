package com.mudosa.musinsa.settlement.batch.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * 정산 배치 스케줄러
 * - 일일 정산: 매일 자정 (00:00)
 * - 주간 정산: 매주 월요일 01:00
 * - 월간 정산: 매월 1일 02:00
 * - 연간 정산: 매년 1월 1일 03:00
 */
@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class SettlementBatchScheduler {

    private final JobLauncher jobLauncher;
    private final Job dailySettlementJob;
    private final Job weeklySettlementJob;
    private final Job monthlySettlementJob;
    private final Job yearlySettlementJob;

    /**
     * 일일 정산 배치 - 매일 자정 실행
     */
    @Scheduled(cron = "0 0 0 * * ?", zone = "Asia/Seoul")
    public void runDailySettlement() {
        try {
            log.info("=== 일일 정산 배치 스케줄 시작 ===");
            JobParameters jobParameters = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

            jobLauncher.run(dailySettlementJob, jobParameters);
            log.info("=== 일일 정산 배치 스케줄 완료 ===");
        } catch (Exception e) {
            log.error("일일 정산 배치 실행 실패", e);
        }
    }

    /**
     * 주간 정산 배치 - 매주 월요일 01:00 실행
     */
    @Scheduled(cron = "0 0 1 ? * MON", zone = "Asia/Seoul")
    public void runWeeklySettlement() {
        try {
            log.info("=== 주간 정산 배치 스케줄 시작 ===");
            JobParameters jobParameters = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

            jobLauncher.run(weeklySettlementJob, jobParameters);
            log.info("=== 주간 정산 배치 스케줄 완료 ===");
        } catch (Exception e) {
            log.error("주간 정산 배치 실행 실패", e);
        }
    }

    /**
     * 월간 정산 배치 - 매월 1일 02:00 실행
     */
    @Scheduled(cron = "0 0 2 1 * ?", zone = "Asia/Seoul")
    public void runMonthlySettlement() {
        try {
            log.info("=== 월간 정산 배치 스케줄 시작 ===");
            JobParameters jobParameters = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

            jobLauncher.run(monthlySettlementJob, jobParameters);
            log.info("=== 월간 정산 배치 스케줄 완료 ===");
        } catch (Exception e) {
            log.error("월간 정산 배치 실행 실패", e);
        }
    }

    /**
     * 연간 정산 배치 - 매년 1월 1일 03:00 실행
     */
    @Scheduled(cron = "0 0 3 1 1 ?", zone = "Asia/Seoul")
    public void runYearlySettlement() {
        try {
            log.info("=== 연간 정산 배치 스케줄 시작 ===");
            JobParameters jobParameters = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

            jobLauncher.run(yearlySettlementJob, jobParameters);
            log.info("=== 연간 정산 배치 스케줄 완료 ===");
        } catch (Exception e) {
            log.error("연간 정산 배치 실행 실패", e);
        }
    }
}