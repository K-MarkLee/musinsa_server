package com.mudosa.musinsa.notification.domain.event;

import com.mudosa.musinsa.domain.chat.repository.ChatPartRepository;
import com.mudosa.musinsa.notification.domain.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {

    private final NotificationService notificationService;
    private final ChatPartRepository chatPartRepository;

    @TransactionalEventListener(phase= TransactionPhase.AFTER_COMMIT)
    public void handleNotificationRequired(ChatNotificationCreatedEvent event){
//        List<ChatPart> chatPartList = chatPartRepository.findChatPartsExcludingUser(event.getUserId(), event.getChatId());
        notificationService.createChatNotification(event);
            //TODO: DB 저장시 여러번 하는게 맞을까?
//            for (ChatPart cp : chatPartList) {
//                notificationService.createChatNotification(cp.getUser().getId(), cp.getChatRoom().getBrand().getNameKo(), event.message(), cp.getChatRoom().getChatId());
//            }
    }
}
