package com.mudosa.musinsa.notification.domain;

import com.mudosa.musinsa.ServiceConfig;
import com.mudosa.musinsa.notification.domain.model.NotificationMetadata;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

class NotificationMetadataRepositoryTest extends ServiceConfig {

    private NotificationMetadata saveNotificationMetadata(String notificationCategory) {
        return notificationMetadataRepository.save(
                NotificationMetadata.builder()
                        .notificationCategory(notificationCategory)
                        .build()
        );
    }

    @AfterEach
    void tearDown() {
        notificationMetadataRepository.deleteAllInBatch();
    }

    @Nested
    @DisplayName("`CHAT` 카테고리의 알림 메타데이터를 조회한다.")
    class findByNotificationCategory {

        @DisplayName("조회된 값이 있을 때")
        @Test
        void findByNotificationCategoryTest(){
        // given
            saveNotificationMetadata("CHAT");
            saveNotificationMetadata("RESTOCK");
            saveNotificationMetadata("STOCKLACK");
        // when
            Optional<NotificationMetadata> notificationMetadata = notificationMetadataRepository.findByNotificationCategory("CHAT");
        // then
            assertSame("CHAT", notificationMetadata.orElseThrow().getNotificationCategory());
        }

        @DisplayName("조회된 값이 없을 때")
        @Test
        void canTFindByNotificationCategoryTest(){
        // given

        // when
        Optional<NotificationMetadata> notificationMetadata = notificationMetadataRepository.findByNotificationCategory("CHAT");
        // then
        assertTrue(notificationMetadata.isEmpty());
        }
    }

}
