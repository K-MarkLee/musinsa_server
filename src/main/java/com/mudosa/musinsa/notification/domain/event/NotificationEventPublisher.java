package com.mudosa.musinsa.notification.domain.event;

import com.mudosa.musinsa.domain.chat.dto.MessageResponse;

public interface NotificationEventPublisher {
    void publishChatNotificationCreatedEvent(MessageResponse dto);
}
