package com.mudosa.musinsa.notification.api.service;

import com.mudosa.musinsa.ServiceConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;

@DisplayName("알림 메타데이터 서비스 테스트")
public class NotificationMetadataServiceTest extends ServiceConfig {

    @AfterEach
    void tearDown() {
        notificationMetadataRepository.deleteAllInBatch();
    }
}
