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
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * 연간 정산 집계 배치 Job
 *
 * 월간 정산 데이터를 연간 단위로 집계
 * 매년 1월 1일 자동 실행
 *
 * 처리 흐름:
 * -> 모든 브랜드 ID 조회 (BrandIdReader)
 * -> 브랜드별로 작년 1월~12월의 월간 정산 데이터 집계
 * -> SettlementMonthly → SettlementYearly 변환 및 저장
 *
 * 집계 기간: 작년 (1월 ~ 12월)
 */
@Slf4j
@Configuration
@Profile("!dev")  // 개발 환경에서는 배치 Job 로드 안 함
@RequiredArgsConstructor
public class YearlySettlementAggregationJob {

    private static final String JOB_NAME = "연간 정산 집계";

    private final SettlementAggregationService settlementService;
    private final BrandIdReader brandIdReader;
    private final BatchProperties batchProperties;

    @Bean
    public Job yearlySettlementJob(
        JobRepository jobRepository,
        PlatformTransactionManager transactionManager
    ) {
        return new JobBuilder("yearlySettlementJob", jobRepository)
            .start(buildStep(jobRepository, transactionManager))
            .build();
    }

    private Step buildStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("yearlySettlementStep", jobRepository)
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
            int targetYear = DateRangeCalculator.getLastYear();

            var result = settlementService.aggregateToYearly(brandId, targetYear);

            if (result.isPresent()) {
                log.debug("브랜드 {} 연간 정산 집계 완료", brandId);
                return brandId;
            } else {
                log.warn("브랜드 {} 연간 정산 데이터 없음", brandId);
                return null;
            }
        };
    }

    private ItemWriter<Long> buildWriter() {
        return chunk -> {
            long processedCount = chunk.getItems().stream()
                .filter(brandId -> brandId != null)
                .count();

            log.info("{} 청크 처리 완료: {} 개 브랜드 (데이터 없음: {})",
                JOB_NAME,
                processedCount,
                chunk.size() - processedCount);
        };
    }
}