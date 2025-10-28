package com.mudosa.musinsa.notification.repository;

import com.mudosa.musinsa.notification.domain.model.NotificationMetadata;
import com.mudosa.musinsa.notification.domain.repository.NotificationMetadataRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class NoteMetaRepoTest {
    @Autowired
    NotificationMetadataRepository notificationMetadataRepository;

    @Test
    void testJpa(){
        NotificationMetadata notificationMetadata = NotificationMetadata.builder()
                .notificationTitle("좋아요한 상품이 재입고되었습니다.")
                .notificationMessage("지금 바로 확인하실 수 있습니다.")
                .notificationType("admin")
                .notificationCategory("RESTOCK")
                .notificationUrl("ghfhgfhgf").build();
        notificationMetadataRepository.save(notificationMetadata);
    }
}
