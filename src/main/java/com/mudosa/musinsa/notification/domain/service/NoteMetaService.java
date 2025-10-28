package com.mudosa.musinsa.notification.domain.service;

import com.mudosa.musinsa.notification.domain.dto.NoteMetaDTO;
import com.mudosa.musinsa.notification.domain.model.NotificationMetadata;
import com.mudosa.musinsa.notification.domain.repository.NotificationMetadataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NoteMetaService {
    private final NotificationMetadataRepository notificationMetadataRepository;

    public NoteMetaDTO get (String category){
        Optional<NotificationMetadata> optionalResult = notificationMetadataRepository.findByNotificationCategory(category);
        NotificationMetadata result = optionalResult.orElseThrow(
                ()->new NoSuchElementException("Category not found" + category)
        );
        return NoteMetaDTO.builder()
                .nMetadataId(result.getNMetadataId())
                .notificationTitle(result.getNotificationTitle())
                .notificationMessage(result.getNotificationMessage())
                .notificationType(result.getNotificationType())
                .notificationCategory(result.getNotificationCategory())
                .notificationUrl(result.getNotificationUrl())
                .build();
    }
}
