package com.mudosa.musinsa.notification.repository;


import com.mudosa.musinsa.notification.domain.model.Notification;
import com.mudosa.musinsa.notification.domain.model.NotificationMetadata;
import com.mudosa.musinsa.notification.domain.repository.NotificationMetadataRepository;
import com.mudosa.musinsa.notification.domain.repository.NotificationRepository;
import com.mudosa.musinsa.user.domain.model.User;
import com.mudosa.musinsa.user.domain.repository.UserRepository;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@SpringBootTest
@Log4j2
public class NotificationRepoTest {
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private NotificationMetadataRepository notificationMetadataRepository;

    @Test
    void notificationRepoCreateTest(){
        Optional<User> optionalUser = userRepository.findById(1L);
        Optional<NotificationMetadata> optionalNotificationMetadata = notificationMetadataRepository.findByNotificationCategory("RESTOCK");
        User resultUser = optionalUser.orElseThrow(
                ()->new NoSuchElementException("User not fount: 1")
        );
        NotificationMetadata resultNotificationMetadata = optionalNotificationMetadata.orElseThrow(
                ()->new NoSuchElementException("Notification Metadata not found: RESTOCK")
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
    @Test
    void notificationRepoReadTest(){
        List<Notification> listResult = notificationRepository.findByUserId(1L);
        for(Notification notification: listResult){
            log.info(notification.getNotificationTitle());
        }
    }

    @Test
    void notificationUpdateTest(){
        log.info(notificationRepository.updateNotificationStatus(1L));
    }

}
