package com.mudosa.musinsa.settlement.batch.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/* Spring Batch 설정 */
@Configuration
@EnableConfigurationProperties(BatchProperties.class)
public class BatchConfig {
}