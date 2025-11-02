package com.mudosa.musinsa.config;

import com.mudosa.musinsa.notification.domain.service.FcmService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import static org.mockito.Mockito.mock;

/**
 * 테스트 전용 설정
 * FCM 등 외부 서비스를 Mock으로 대체
 */
@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public FcmService fcmService() {
        return mock(FcmService.class);
    }
}
