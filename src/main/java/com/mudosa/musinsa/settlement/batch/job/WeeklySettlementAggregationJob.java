package com.mudosa.musinsa.settlement.batch.job;

import com.mudosa.musinsa.brand.domain.repository.BrandRepository;
import com.mudosa.musinsa.settlement.application.SettlementAggregationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

/**
 * 주간 정산 집계 배치 Job
 * - 매주 월요일 실행
 * - SettlementDaily → SettlementWeekly 집계
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class WeeklySettlementAggregationJob {

    private final SettlementAggregationService settlementAggregationService;
    private final BrandRepository brandRepository;

    @Bean
    public Job weeklySettlementJob(JobRepository jobRepository, Step weeklySettlementStep) {
        return new JobBuilder("weeklySettlementJob", jobRepository)
            .start(weeklySettlementStep)
            .build();
    }

    @Bean
    public Step weeklySettlementStep(
        JobRepository jobRepository,
        PlatformTransactionManager transactionManager
    ) {
        return new StepBuilder("weeklySettlementStep", jobRepository)
            .tasklet(weeklySettlementTasklet(), transactionManager)
            .build();
    }

    @Bean
    public Tasklet weeklySettlementTasklet() {
        return (contribution, chunkContext) -> {
            log.info("=== 주간 정산 집계 배치 시작 ===");

            // 지난 주 기준으로 집계 (월요일 ~ 일요일)
            LocalDate today = LocalDate.now();
            LocalDate lastMonday = today.minusWeeks(1).with(DayOfWeek.MONDAY);
            LocalDate lastSunday = lastMonday.plusDays(6);

            log.info("집계 대상 기간: {} ~ {}", lastMonday, lastSunday);

            // 모든 브랜드의 주간 정산 집계
            List<Long> brandIds = brandRepository.findAll()
                .stream()
                .map(brand -> brand.getBrandId())
                .toList();

            log.info("집계 대상 브랜드 수: {}", brandIds.size());

            int successCount = 0;
            int failCount = 0;

            for (Long brandId : brandIds) {
                try {
                    settlementAggregationService.aggregateToWeekly(
                        brandId,
                        lastMonday,
                        lastSunday
                    );
                    successCount++;
                    log.info("브랜드 {} 주간 정산 집계 완료", brandId);
                } catch (Exception e) {
                    failCount++;
                    log.error("브랜드 {} 주간 정산 집계 실패", brandId, e);
                }
            }

            log.info("=== 주간 정산 집계 배치 완료 === 성공: {}, 실패: {}", successCount, failCount);

            return RepeatStatus.FINISHED;
        };
    }
}