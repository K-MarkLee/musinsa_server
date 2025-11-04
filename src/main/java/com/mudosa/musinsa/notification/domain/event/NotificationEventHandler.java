package com.mudosa.musinsa.notification.domain.event;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.mudosa.musinsa.domain.chat.entity.ChatPart;
import com.mudosa.musinsa.domain.chat.repository.ChatPartRepository;
import com.mudosa.musinsa.notification.domain.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventHandler {

    private final NotificationService notificationService;
    private final ChatPartRepository chatPartRepository;

    @TransactionalEventListener(phase= TransactionPhase.AFTER_COMMIT)
    public void handleNotificationRequired(NotificationRequiredEvent event){
        List<ChatPart> chatPartList = chatPartRepository.findChatPartsExcludingUser(event.getUserId(), event.getChatId());
        try {
            for (ChatPart cp : chatPartList) {
                notificationService.createChatNotification(cp.getUser().getId(), cp.getChatRoom().getBrand().getNameKo(), event.message(), cp.getChatRoom().getChatId());
            }
        } catch (FirebaseMessagingException e){
            log.error(e.getMessage());
        }
    }
}
