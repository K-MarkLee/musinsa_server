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
import java.time.YearMonth;
import java.util.List;

/**
 * 월간 정산 집계 배치 Job
 * - 매월 1일 실행
 * - SettlementDaily → SettlementMonthly 집계
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class MonthlySettlementAggregationJob {

    private final SettlementAggregationService settlementAggregationService;
    private final BrandRepository brandRepository;

    @Bean
    public Job monthlySettlementJob(JobRepository jobRepository, Step monthlySettlementStep) {
        return new JobBuilder("monthlySettlementJob", jobRepository)
            .start(monthlySettlementStep)
            .build();
    }

    @Bean
    public Step monthlySettlementStep(
        JobRepository jobRepository,
        PlatformTransactionManager transactionManager
    ) {
        return new StepBuilder("monthlySettlementStep", jobRepository)
            .tasklet(monthlySettlementTasklet(), transactionManager)
            .build();
    }

    @Bean
    public Tasklet monthlySettlementTasklet() {
        return (contribution, chunkContext) -> {
            log.info("=== 월간 정산 집계 배치 시작 ===");

            // 지난 달 기준으로 집계
            YearMonth lastMonth = YearMonth.now().minusMonths(1);
            LocalDate startDate = lastMonth.atDay(1);
            LocalDate endDate = lastMonth.atEndOfMonth();

            log.info("집계 대상 기간: {} ~ {}", startDate, endDate);

            // 모든 브랜드의 월간 정산 집계
            List<Long> brandIds = brandRepository.findAll()
                .stream()
                .map(brand -> brand.getBrandId())
                .toList();

            log.info("집계 대상 브랜드 수: {}", brandIds.size());

            int successCount = 0;
            int failCount = 0;

            for (Long brandId : brandIds) {
                try {
                    settlementAggregationService.aggregateToMonthly(
                        brandId,
                        startDate,
                        endDate
                    );
                    successCount++;
                    log.info("브랜드 {} 월간 정산 집계 완료", brandId);
                } catch (Exception e) {
                    failCount++;
                    log.error("브랜드 {} 월간 정산 집계 실패", brandId, e);
                }
            }

            log.info("=== 월간 정산 집계 배치 완료 === 성공: {}, 실패: {}", successCount, failCount);

            return RepeatStatus.FINISHED;
        };
    }
}