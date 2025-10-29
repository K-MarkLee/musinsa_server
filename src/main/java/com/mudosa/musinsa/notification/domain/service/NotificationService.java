package com.mudosa.musinsa.notification.domain.service;

import com.mudosa.musinsa.notification.domain.dto.NotificationDTO;
import com.mudosa.musinsa.notification.domain.model.Notification;
import com.mudosa.musinsa.notification.domain.model.NotificationMetadata;
import com.mudosa.musinsa.notification.domain.repository.NotificationMetadataRepository;
import com.mudosa.musinsa.notification.domain.repository.NotificationRepository;
import com.mudosa.musinsa.user.domain.model.User;
import com.mudosa.musinsa.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * 필요한 기능
 * 1. 어떤 사용자의 알림 목록 열람
 * 2. 어떤
 */

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationMetadataRepository notificationMetadataRepository;

    public List<NotificationDTO> readNotification(Long userId){
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

    public void createNotification(Long userId,String notificationCategory){

        Optional<User> optionalUser = userRepository.findById(userId);
        User resultUser = optionalUser.orElseThrow(
                ()->new NoSuchElementException("User not found")
        );

        Optional<NotificationMetadata> optionalNotificationMetadata = notificationMetadataRepository.findByNotificationCategory(notificationCategory);
        NotificationMetadata resultNotificationMetadata = optionalNotificationMetadata.orElseThrow(
                ()->new NoSuchElementException("Notification Metadata not found")
        );

        Notification notification = Notification.builder()
                .user(resultUser)
                .notificationMetadata(resultNotificationMetadata)
                .notificationTitle(resultNotificationMetadata.getNotificationTitle())
                .notificationMessage(resultNotificationMetadata.getNotificationMessage())
                .notificationUrl(resultNotificationMetadata.getNotificationUrl())
                .build();
        notificationRepository.save(notification);
    }

    public int updateNotificationState(Long notificationId){
        return notificationRepository.updateNotificationStatus(notificationId);
    }
}
