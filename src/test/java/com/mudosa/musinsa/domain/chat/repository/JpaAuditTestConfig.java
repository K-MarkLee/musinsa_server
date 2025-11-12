package com.mudosa.musinsa.domain.chat.repository; // 테스트 패키지에 맞게

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

// updatedAt, createdId 자동 생성
@Configuration
@EnableJpaAuditing
public class JpaAuditTestConfig {
}
