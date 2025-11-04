package com.mudosa.musinsa.notification.domain.service;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.mudosa.musinsa.brand.domain.model.BrandMember;
import com.mudosa.musinsa.brand.domain.repository.BrandMemberRepository;
import com.mudosa.musinsa.fbtoken.service.FirebaseTokenService;
import com.mudosa.musinsa.notification.domain.dto.NotificationDTO;
import com.mudosa.musinsa.notification.domain.model.Notification;
import com.mudosa.musinsa.notification.domain.model.NotificationMetadata;
import com.mudosa.musinsa.notification.domain.repository.NotificationMetadataRepository;
import com.mudosa.musinsa.notification.domain.repository.NotificationRepository;
import com.mudosa.musinsa.product.domain.model.Inventory;
import com.mudosa.musinsa.product.domain.model.ProductOption;
import com.mudosa.musinsa.product.domain.repository.ProductOptionRepository;
import com.mudosa.musinsa.user.domain.model.User;
import com.mudosa.musinsa.user.domain.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * 필요한 기능
 * 1. 어떤 사용자의 알림 목록 열람
 * 2. 어떤
 */

@Service
@Slf4j
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationMetadataRepository notificationMetadataRepository;
    private final FcmService fcmService;
    private final FirebaseTokenService firebaseTokenService;
    private final ProductOptionRepository productOptionRepository;
    private final BrandMemberRepository brandMemberRepository;

    public NotificationService(
            NotificationRepository notificationRepository,
            UserRepository userRepository,
            NotificationMetadataRepository notificationMetadataRepository,
            @Autowired(required = false) FcmService fcmService,
            FirebaseTokenService firebaseTokenService,
            ProductOptionRepository productOptionRepository,
            BrandMemberRepository brandMemberRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.notificationMetadataRepository = notificationMetadataRepository;
        this.fcmService = fcmService;
        this.firebaseTokenService = firebaseTokenService;
        this.productOptionRepository = productOptionRepository;
        this.brandMemberRepository = brandMemberRepository;
    }

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

    public void createNotification(Long userId,String notificationCategory) throws FirebaseMessagingException {

        User resultUser = userRepository.findById(userId).orElseThrow(
                ()->new NoSuchElementException("User not found")
        );

        NotificationMetadata resultNotificationMetadata = notificationMetadataRepository.findByNotificationCategory(notificationCategory).orElseThrow(
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
        //푸시 알림 보내기
        if (fcmService != null) {
            fcmService.sendMessageByToken(notification.getNotificationTitle(),notification.getNotificationMessage(),firebaseTokenService.readFirebaseTokens(userId));
        } else {
            log.info("FCM이 비활성화되어 있습니다. 푸시 알림을 전송하지 않습니다.");
        }
    }

    public void createChatNotification(Long userId, String title, String message, Long chatRoomId) throws FirebaseMessagingException {
        NotificationMetadata result = notificationMetadataRepository.findByNotificationCategory("CHAT").orElseThrow(
                ()->new NoSuchElementException("Notification Metadata not found")
        );
        User resultUser = userRepository.findById(userId).orElseThrow(
                ()->new NoSuchElementException("User not found")
        );

        Notification notification = Notification.builder()
                .user(resultUser)
                .notificationMetadata(result)
                .notificationTitle(title + "채팅방에서 메세지가 왔습니다.")
                .notificationMessage(Objects.isNull(message)?"첨부파일이 있습니다":message)
                .notificationUrl("/chat/"+chatRoomId.toString()+"/")
                .build();
        notificationRepository.save(notification);
        if (fcmService != null) {
            fcmService.sendMessageByToken(notification.getNotificationTitle(),notification.getNotificationMessage(),firebaseTokenService.readFirebaseTokens(userId));
        } else {
            log.info("FCM이 비활성화되어 있습니다. 푸시 알림을 전송하지 않습니다.");
        }
    }

    public int updateNotificationState(Long notificationId){
        return notificationRepository.updateNotificationStatus(notificationId);
    }

    public void createOutOfStockNote(Inventory inventory){
        ProductOption prodOption = productOptionRepository.findByInventory(inventory).orElseThrow(
                ()->new NoSuchElementException("Inventory not found"));
        BrandMember brandMem = brandMemberRepository.findByBrand(prodOption.getProduct().getBrand()).orElseThrow(
                ()->new NoSuchElementException("Product not found")
        );
        NotificationMetadata resultNotificationMetadata = notificationMetadataRepository.findByNotificationCategory("STOCKLACK").orElseThrow(()->new NoSuchElementException("Notification Metadata not found"));

        Notification notification = Notification.builder()
                .user(userRepository.findById(brandMem.getUserId()).orElseThrow(
                        ()->new NoSuchElementException("User not found")
                ))
                .notificationMetadata(resultNotificationMetadata)
                .notificationTitle(prodOption.getProduct().getProductName()+resultNotificationMetadata.getNotificationTitle())
                .notificationMessage(resultNotificationMetadata.getNotificationMessage())
                .notificationUrl(resultNotificationMetadata.getNotificationUrl())
                .build();
        notificationRepository.save(notification);

        if (fcmService != null) {
            try{
                fcmService.sendMessageByToken(notification.getNotificationTitle(),notification.getNotificationMessage(),firebaseTokenService.readFirebaseTokens(brandMem.getUserId()));
            }catch (FirebaseMessagingException e){
                log.error(e.getMessage());
            }
        } else {
            log.info("FCM이 비활성화되어 있습니다. 푸시 알림을 전송하지 않습니다.");
        }
    }

    public void createNotificationFromDTO (NotificationDTO dto){
        Notification note = Notification.builder()
                .user(userRepository.findById(dto.getUserId()).orElseThrow(()->new NoSuchElementException("User not found")))
                .notificationMetadata(notificationMetadataRepository.findByNotificationCategory("CHAT").orElseThrow(()->new NoSuchElementException("Notification Metadata not found")))
                .notificationTitle(dto.getNotificationTitle())
                .notificationMessage(dto.getNotificationMessage())
                .notificationUrl(dto.getNotificationUrl())
                .build();
        notificationRepository.save(note);
        if (fcmService != null) {
            try{
                fcmService.sendMessageByToken(note.getNotificationTitle(),note.getNotificationMessage(),firebaseTokenService.readFirebaseTokens(dto.getUserId()));
            } catch (FirebaseMessagingException e){
                log.error(e.getMessage());
            }
        } else {
            log.info("FCM이 비활성화되어 있습니다. 푸시 알림을 전송하지 않습니다.");
        }
    }
}
