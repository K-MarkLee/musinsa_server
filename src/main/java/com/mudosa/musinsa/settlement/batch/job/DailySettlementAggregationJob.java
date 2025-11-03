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

import java.time.LocalDate;
import java.util.List;

/**
 * 일일 정산 집계 배치 Job
 * - 매일 자정 실행
 * - SettlementPerTransaction → SettlementDaily 집계
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class DailySettlementAggregationJob {

    private final SettlementAggregationService settlementAggregationService;
    private final BrandRepository brandRepository;

    @Bean
    public Job dailySettlementJob(JobRepository jobRepository, Step dailySettlementStep) {
        return new JobBuilder("dailySettlementJob", jobRepository)
            .start(dailySettlementStep)
            .build();
    }

    @Bean
    public Step dailySettlementStep(
        JobRepository jobRepository,
        PlatformTransactionManager transactionManager
    ) {
        return new StepBuilder("dailySettlementStep", jobRepository)
            .tasklet(dailySettlementTasklet(), transactionManager)
            .build();
    }

    @Bean
    public Tasklet dailySettlementTasklet() {
        return (contribution, chunkContext) -> {
            log.info("=== 일일 정산 집계 배치 시작 ===");

            // 어제 날짜 기준으로 집계
            LocalDate targetDate = LocalDate.now().minusDays(1);

            log.info("집계 대상 날짜: {}", targetDate);

            // 모든 브랜드의 일일 정산 집계
            List<Long> brandIds = brandRepository.findAll()
                .stream()
                .map(brand -> brand.getBrandId())
                .toList();

            log.info("집계 대상 브랜드 수: {}", brandIds.size());

            int successCount = 0;
            int failCount = 0;

            for (Long brandId : brandIds) {
                try {
                    settlementAggregationService.aggregateToDaily(
                        brandId,
                        targetDate,
                        targetDate
                    );
                    successCount++;
                    log.info("브랜드 {} 일일 정산 집계 완료", brandId);
                } catch (Exception e) {
                    failCount++;
                    log.error("브랜드 {} 일일 정산 집계 실패", brandId, e);
                }
            }

            log.info("=== 일일 정산 집계 배치 완료 === 성공: {}, 실패: {}", successCount, failCount);

            return RepeatStatus.FINISHED;
        };
    }
}
