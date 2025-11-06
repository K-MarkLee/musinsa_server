package com.mudosa.musinsa.settlement.batch.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/* Spring Batch 설정 */
@Configuration
@Profile("!dev")  // 개발 환경에서는 배치 설정 로드 안 함
@EnableConfigurationProperties(BatchProperties.class)
public class BatchConfig {
}