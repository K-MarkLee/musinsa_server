package com.mudosa.musinsa.domain.chat;

import com.mudosa.musinsa.ServerApplication;
import com.mudosa.musinsa.notification.domain.service.FcmService;
import com.mudosa.musinsa.security.JwtTokenProvider;
import com.mudosa.musinsa.settlement.batch.job.DailySettlementAggregationJob;
import com.mudosa.musinsa.settlement.batch.job.MonthlySettlementAggregationJob;
import com.mudosa.musinsa.settlement.batch.job.WeeklySettlementAggregationJob;
import com.mudosa.musinsa.settlement.batch.scheduler.SettlementBatchScheduler;
import com.mudosa.musinsa.settlement.domain.repository.SettlementDailyMapper;
import com.mudosa.musinsa.settlement.domain.repository.SettlementMonthlyMapper;
import com.mudosa.musinsa.settlement.domain.repository.SettlementPerTransactionMapper;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

// 공통 테스트 설정 분리
@Slf4j
@ActiveProfiles("test")
@SpringBootTest(
    classes = ServerApplication.class,
    properties = {
        "spring.main.allow-bean-definition-overriding=true",
    }
)
@ImportAutoConfiguration(exclude = {
    MybatisAutoConfiguration.class,
    org.springframework.boot.autoconfigure.batch.BatchAutoConfiguration.class
})
@Transactional
public abstract class ServiceConfig {

  @MockitoBean
  private JwtTokenProvider jwtTokenProvider;

  /**
   * 배치 문제 해결을 위한 임시 조치
   */
  @MockitoBean
  private SettlementDailyMapper settlementDailyMapper;
  @MockitoBean
  private SettlementMonthlyMapper settlementMonthlyMapper;
  @MockitoBean
  private SettlementPerTransactionMapper settlementPerTransactionMapper;

  @MockitoBean
  private SettlementBatchScheduler settlementBatchScheduler;
  @MockitoBean
  private DailySettlementAggregationJob dailySettlementAggregationJob;
  @MockitoBean
  private MonthlySettlementAggregationJob monthlySettlementAggregationJob;
  @MockitoBean
  private WeeklySettlementAggregationJob weeklySettlementAggregationJob;

  @MockitoBean
  private JobRepository jobRepository;
  @MockitoBean
  private FcmService fcmService;
}
