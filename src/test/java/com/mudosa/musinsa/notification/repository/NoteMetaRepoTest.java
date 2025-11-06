package com.mudosa.musinsa.notification.repository;

import com.mudosa.musinsa.notification.domain.model.NotificationMetadata;
import com.mudosa.musinsa.notification.domain.repository.NotificationMetadataRepository;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.NoSuchElementException;
import java.util.Optional;

@SpringBootTest
@Log4j2
public class NoteMetaRepoTest {
    @Autowired
    NotificationMetadataRepository notificationMetadataRepository;

    @Test
    void noteMetaRepoCreateTest(){
        NotificationMetadata notificationMetadata = NotificationMetadata.builder()
                .notificationTitle("좋아요한 상품이 재입고되었습니다.")
                .notificationMessage("지금 바로 확인하실 수 있습니다.")
                .notificationType("admin")
                .notificationCategory("RESTOCK")
                .notificationUrl("ghfhgfhgf")
                .build();
        notificationMetadataRepository.save(notificationMetadata);
    }

    @Test
    void NoteMetaRepoDeleteTest(){
        notificationMetadataRepository.deleteById(1L);
    }

    @Test
    void NoteMetaRepoReadTest(){
        Optional<NotificationMetadata> optionalResult = notificationMetadataRepository.findByNotificationCategory("RESTOCK");
        NotificationMetadata result = optionalResult.orElseThrow(
                ()->new NoSuchElementException("Category not found: RESTOCK")
        );
        log.info(result.getNotificationTitle());
    }
}
