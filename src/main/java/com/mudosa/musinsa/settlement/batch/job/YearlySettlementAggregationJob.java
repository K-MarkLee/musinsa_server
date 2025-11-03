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
 * 연간 정산 집계 배치 Job
 * - 매년 1월 1일 실행
 * - SettlementMonthly → SettlementYearly 집계
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class YearlySettlementAggregationJob {

    private final SettlementAggregationService settlementAggregationService;
    private final BrandRepository brandRepository;

    @Bean
    public Job yearlySettlementJob(JobRepository jobRepository, Step yearlySettlementStep) {
        return new JobBuilder("yearlySettlementJob", jobRepository)
            .start(yearlySettlementStep)
            .build();
    }

    @Bean
    public Step yearlySettlementStep(
        JobRepository jobRepository,
        PlatformTransactionManager transactionManager
    ) {
        return new StepBuilder("yearlySettlementStep", jobRepository)
            .tasklet(yearlySettlementTasklet(), transactionManager)
            .build();
    }

    @Bean
    public Tasklet yearlySettlementTasklet() {
        return (contribution, chunkContext) -> {
            log.info("=== 연간 정산 집계 배치 시작 ===");

            // 작년 기준으로 집계
            int lastYear = LocalDate.now().getYear() - 1;

            log.info("집계 대상 연도: {}", lastYear);

            // 모든 브랜드의 연간 정산 집계
            List<Long> brandIds = brandRepository.findAll()
                .stream()
                .map(brand -> brand.getBrandId())
                .toList();

            log.info("집계 대상 브랜드 수: {}", brandIds.size());

            int successCount = 0;
            int failCount = 0;
            int noDataCount = 0;

            for (Long brandId : brandIds) {
                try {
                    var result = settlementAggregationService.aggregateToYearly(brandId, lastYear);
                    if (result.isPresent()) {
                        successCount++;
                        log.info("브랜드 {} 연간 정산 집계 완료", brandId);
                    } else {
                        noDataCount++;
                        log.warn("브랜드 {} 연간 정산 데이터 없음", brandId);
                    }
                } catch (Exception e) {
                    failCount++;
                    log.error("브랜드 {} 연간 정산 집계 실패", brandId, e);
                }
            }

            log.info("=== 연간 정산 집계 배치 완료 === 성공: {}, 데이터 없음: {}, 실패: {}",
                successCount, noDataCount, failCount);

            return RepeatStatus.FINISHED;
        };
    }
}