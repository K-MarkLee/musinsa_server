package com.mudosa.musinsa.settlement.batch.job;

import com.mudosa.musinsa.settlement.application.SettlementAggregationService;
import com.mudosa.musinsa.settlement.batch.common.BrandIdReader;
import com.mudosa.musinsa.settlement.batch.common.DateRangeCalculator;
import com.mudosa.musinsa.settlement.batch.config.BatchProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;

/**
 * 월간 정산 집계 배치
 * - 실행 주기: 매월 1일
 * - 집계 범위: SettlementDaily → SettlementMonthly
 * - 처리 방식: 브랜드별 청크 단위 처리
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class MonthlySettlementAggregationJob {

    private static final String JOB_NAME = "월간 정산 집계";

    private final SettlementAggregationService settlementService;
    private final BrandIdReader brandIdReader;
    private final BatchProperties batchProperties;

    @Bean
    public Job monthlySettlementJob(
        JobRepository jobRepository,
        PlatformTransactionManager transactionManager
    ) {
        return new JobBuilder("monthlySettlementJob", jobRepository)
            .start(buildStep(jobRepository, transactionManager))
            .build();
    }

    private Step buildStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("monthlySettlementStep", jobRepository)
            .<Long, Long>chunk(batchProperties.getChunkSize(), transactionManager)
            .reader(brandIdReader.createReader(JOB_NAME))
            .processor(buildProcessor())
            .writer(buildWriter())
            .faultTolerant()
            .skip(DataAccessException.class)
            .skipLimit(batchProperties.getMaxSkipCount())
            .build();
    }

    private ItemProcessor<Long, Long> buildProcessor() {
        return brandId -> {
            LocalDate startDate = DateRangeCalculator.getLastMonthStart();
            LocalDate endDate = DateRangeCalculator.getLastMonthEnd();

            settlementService.aggregateToMonthly(brandId, startDate, endDate);

            log.debug("브랜드 {} 월간 정산 집계 완료", brandId);
            return brandId;
        };
    }

    private ItemWriter<Long> buildWriter() {
        return chunk -> log.info("{} 청크 처리 완료: {} 개 브랜드", JOB_NAME, chunk.size());
    }
}