package com.mudosa.musinsa.notification.api.service;

import com.mudosa.musinsa.ServiceConfig;
import com.mudosa.musinsa.brand.domain.model.Brand;
import com.mudosa.musinsa.brand.domain.model.BrandStatus;
import com.mudosa.musinsa.domain.chat.dto.MessageResponse;
import com.mudosa.musinsa.domain.chat.entity.ChatPart;
import com.mudosa.musinsa.domain.chat.entity.ChatRoom;
import com.mudosa.musinsa.domain.chat.enums.ChatPartRole;
import com.mudosa.musinsa.domain.chat.enums.ChatRoomType;
import com.mudosa.musinsa.notification.domain.dto.NotificationDTO;
import com.mudosa.musinsa.notification.domain.event.ChatNotificationCreatedEvent;
import com.mudosa.musinsa.notification.domain.model.Notification;
import com.mudosa.musinsa.notification.domain.model.NotificationMetadata;
import com.mudosa.musinsa.notification.domain.service.NotificationService;
import com.mudosa.musinsa.user.domain.model.User;
import com.mudosa.musinsa.user.domain.model.UserRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@DisplayName("알림 서비스 테스트")
class NotificationServiceTest extends ServiceConfig {

    @Autowired
    private NotificationService notificationService;

    @AfterEach
    void tearDown() {
        notificationRepository.deleteAllInBatch();
        chatPartRepository.deleteAllInBatch();
        chatRoomRepository.deleteAllInBatch();
        brandRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
        notificationMetadataRepository.deleteAllInBatch();
    }

    @Nested
    @DisplayName("어떤 사용자의 알림 내역을 조회한다.")
    class ReadNotification {
        @DisplayName("사용자1의 알림 내역을 조회한다.")
        @Test
        void ReadNotificationTest(){
        // given
            User user1 = saveUser("user1");
            User user2 = saveUser("user2");

            NotificationMetadata notificationMetadata = saveNotificationMetadata("CHAT");

            saveNotification(user1,notificationMetadata);
            saveNotification(user1,notificationMetadata);
            saveNotification(user1,notificationMetadata);
            saveNotification(user2,notificationMetadata);
            saveNotification(user2,notificationMetadata);

        // when
            List<NotificationDTO> notificationDTOS = notificationService.readNotification(user1.getId());
        // then
            assertThat(notificationDTOS).hasSize(3);

        }
    }

    @Nested
    @DisplayName("어떤 사용자의 알림 내역 중 1개를 읽음 처리한다.")
    class UpdateNotificationState{

        @DisplayName("사용자1의 알림 5 개 중 선택된 1개를 읽음 처리한다.")
        @Test
        void Test(){
        // given
            User user1 = saveUser("user1");

            NotificationMetadata notificationMetadata = saveNotificationMetadata("CHAT");

            Notification notification = saveNotification(user1,notificationMetadata);
            saveNotification(user1,notificationMetadata);
            saveNotification(user1,notificationMetadata);
            saveNotification(user1,notificationMetadata);
            saveNotification(user1,notificationMetadata);

        // when

            int updateCounter = notificationService.updateNotificationState(notification.getNotificationId());

        // then
            assertThat(updateCounter).isEqualTo(1);

        }
    }

    @Nested
    @DisplayName("발신자를 제외한 채팅방의 모든 사용자에게 알림을 보낸다.")
    class CreateChatNotification{

        @DisplayName("발신자1을 제외한 참여자 2,3 모두에게 알림을 보낸다.")
        @Test
        void CreateChatNotificationTest(){
        // given
            User user1 = saveUser("user1");
            User user2 = saveUser("user2");
            User user3 = saveUser("user3");

            saveNotificationMetadata("CHAT");

            Brand brand = saveBrand("브랜드", "brand");

            ChatRoom chatRoom = saveChatRoom(brand);

            saveChatPart(chatRoom, user1);
            saveChatPart(chatRoom, user2);
            saveChatPart(chatRoom, user3);

            ChatNotificationCreatedEvent chatNotificationCreatedEvent = createChatNotificationCreatedEvent(user1,chatRoom);
        // when
            List<Notification> notifications = notificationService.createChatNotification(chatNotificationCreatedEvent);
        // then
            assertThat(notifications).hasSize(2);

        }
    }

    private User saveUser(String userName){
        User user = User.create(userName, "yong1234!", "test@test.com", UserRole.USER, "http://mudosa/uploads/avatar/avatar1.png", "010-0000-0000", "서울 강남구");
        userRepository.save(user);
        return user;
    }

    private Brand saveBrand(String nameKo, String nameEn) {
        Brand brand = Brand.builder()
                .nameKo(nameKo)
                .nameEn(nameEn)
                .commissionRate(BigDecimal.valueOf(10.00))
                .status(BrandStatus.ACTIVE)
                .build();
        brandRepository.save(brand);
        return brand;
    }

    private ChatRoom saveChatRoom(Brand brand) {
        ChatRoom chatRoom = ChatRoom.builder()
                .brand(brand)
                .type(ChatRoomType.GROUP)
                .build();
        chatRoomRepository.save(chatRoom);
        return chatRoom;
    }

    private ChatPart saveChatPart(ChatRoom chatRoom, User user) {
        return chatPartRepository.save(
                ChatPart.builder()
                        .chatRoom(chatRoom)
                        .user(user)
                        .role(ChatPartRole.USER)
                        .build()
        );
    }

    private ChatNotificationCreatedEvent createChatNotificationCreatedEvent(User user, ChatRoom chatRoom) {
        return new ChatNotificationCreatedEvent(MessageResponse.builder()
                .userId(user.getId())
                .chatId(chatRoom.getChatId())
                .content("")
                .build()
        );
    }
}
