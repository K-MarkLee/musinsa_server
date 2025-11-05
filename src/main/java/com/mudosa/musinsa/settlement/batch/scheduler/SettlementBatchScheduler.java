package com.mudosa.musinsa.settlement.batch.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 정산 배치 스케줄러
 *
 * 정산 배치 Job 실행
 * 운영 환경에서만 활성화 (@Profile("!dev"))
 *
 * 스케줄:
 * - 일일 정산: 매일 오전 1시 (전날 데이터 집계)
 * - 주간 정산: 매주 월요일 오전 2시 (지난주 데이터 집계)
 * - 월간 정산: 매월 1일 오전 3시 (지난달 데이터 집계)
 * - 연간 정산: 매년 1월 1일 오전 4시 (작년 데이터 집계)
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Profile("!dev")  // 개발 환경에서는 비활성화
public class SettlementBatchScheduler {

    private final JobLauncher jobLauncher;
    private final Job dailySettlementJob;
    private final Job weeklySettlementJob;
    private final Job monthlySettlementJob;
    private final Job yearlySettlementJob;

    /**
     * 일일 정산 배치 스케줄
     * 매일 오전 1시에 실행 (전날 데이터 집계)
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void scheduleDailySettlement() {
        try {
            log.info("[스케줄러] 일일 정산 배치 시작 - 오전 1시");

            JobParameters params = createJobParameters();
            jobLauncher.run(dailySettlementJob, params);

            log.info("[스케줄러] 일일 정산 배치 완료");
        } catch (Exception e) {
            log.error("[스케줄러] 일일 정산 배치 실패", e);
            // TODO: 알림 전송 (Email -> slack 순으로 구현)
        }
    }

    /**
     * 주간 정산 배치 스케줄
     * 매주 월요일 오전 2시에 실행 (지난주 데이터 집계)
     */
    @Scheduled(cron = "0 0 2 * * MON")
    public void scheduleWeeklySettlement() {
        try {
            log.info("[스케줄러] 주간 정산 배치 시작 - 매주 월요일 오전 2시");

            JobParameters params = createJobParameters();
            jobLauncher.run(weeklySettlementJob, params);

            log.info("[스케줄러] 주간 정산 배치 완료");
        } catch (Exception e) {
            log.error("[스케줄러] 주간 정산 배치 실패", e);
            // TODO: 알림 전송
        }
    }

    /**
     * 월간 정산 배치 스케줄
     * 매월 1일 오전 3시에 실행 (지난달 데이터 집계)
     */
    @Scheduled(cron = "0 0 3 1 * ?")
    public void scheduleMonthlySettlement() {
        try {
            log.info("[스케줄러] 월간 정산 배치 시작 - 매월 1일 오전 3시");

            JobParameters params = createJobParameters();
            jobLauncher.run(monthlySettlementJob, params);

            log.info("[스케줄러] 월간 정산 배치 완료");
        } catch (Exception e) {
            log.error("[스케줄러] 월간 정산 배치 실패", e);
            // TODO: 알림 전송
        }
    }

    /**
     * 연간 정산 배치 스케줄
     * 매년 1월 1일 오전 4시에 실행 (작년 데이터 집계)
     */
    @Scheduled(cron = "0 0 4 1 1 ?")
    public void scheduleYearlySettlement() {
        try {
            log.info("[스케줄러] 연간 정산 배치 시작 - 매년 1월 1일 오전 4시");

            JobParameters params = createJobParameters();
            jobLauncher.run(yearlySettlementJob, params);

            log.info("[스케줄러] 연간 정산 배치 완료");
        } catch (Exception e) {
            log.error("[스케줄러] 연간 정산 배치 실패", e);
            // TODO: 알림 전송
        }
    }

    /**
     * JobParameters 생성
     * Spring Batch는 같은 Job을 여러 번 실행하려면 매번 다른 JobParameters가 필요
     * 시간을 파라미터로 넣어서 매번 다르게 만듦
     */
    private JobParameters createJobParameters() {
        return new JobParametersBuilder()
            .addLong("time", System.currentTimeMillis())
            .toJobParameters();
    }
}
