package com.mudosa.musinsa.notification.domain.service;

import com.mudosa.musinsa.notification.domain.dto.NotificationDTO;
import com.mudosa.musinsa.notification.domain.model.Notification;
import com.mudosa.musinsa.notification.domain.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 필요한 기능
 * 1. 어떤 사용자의 알림 목록 열람
 * 2. 어떤
 */

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;

    public List<NotificationDTO> get(Long userId){
        List<Notification> listResult = notificationRepository.findByUserId(userId);
        List<NotificationDTO> result = new ArrayList<>();
        for(Notification notification : listResult){
            NotificationDTO dto = NotificationDTO.builder()
                    .notificationId(notification.getNotificationId())
                    .userId(notification.getUser().getId())
                    .nMetadataId(notification.getNotificationMetadata().getNMetadataId())
                    .notificationTitle(notification.getNotificationTitle())
                    .notificationMessage(notification.getNotificationMessage())
                    .notificationUrl(notification.getNotificationUrl())
                    .notificationStatus(notification.getNotificationStatus())
                    .readAt(notification.getReadAt())
                    .build();
            result.add(dto);
        }
        return result;
    }
}
