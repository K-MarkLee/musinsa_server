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

import java.time.LocalDate;

/**
 * 주간 정산 집계 배치 Job
 *
 * 일일 정산 데이터를 주간 단위로 집계
 * 매주 월요일 자동 실행
 *
 * 처리 흐름:
 * 모든 브랜드 ID 조회 (BrandIdReader)
 * -> 브랜드별로 지난주 월요일~일요일의 일일 정산 데이터 집계
 * -> SettlementDaily → SettlementWeekly 변환 및 저장
 *
 * 집계 기간: 지난주 (월요일 ~ 일요일)
 */
@Slf4j
@Configuration
@Profile("!dev")  // 개발 환경에서는 배치 Job 로드 안 함
@RequiredArgsConstructor
public class WeeklySettlementAggregationJob {

    private static final String JOB_NAME = "주간 정산 집계";

    private final SettlementAggregationService settlementService;
    private final BrandIdReader brandIdReader;
    private final BatchProperties batchProperties;

    @Bean
    public Job weeklySettlementJob(
        JobRepository jobRepository,
        PlatformTransactionManager transactionManager
    ) {
        return new JobBuilder("weeklySettlementJob", jobRepository)
            .start(buildStep(jobRepository, transactionManager))
            .build();
    }

    private Step buildStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("weeklySettlementStep", jobRepository)
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
            LocalDate startDate = DateRangeCalculator.getLastWeekMonday();
            LocalDate endDate = DateRangeCalculator.getLastWeekSunday();

            settlementService.aggregateToWeekly(brandId, startDate, endDate);

            log.debug("브랜드 {} 주간 정산 집계 완료", brandId);
            return brandId;
        };
    }

    private ItemWriter<Long> buildWriter() {
        return chunk -> log.info("{} 청크 처리 완료: {} 개 브랜드", JOB_NAME, chunk.size());
    }
}