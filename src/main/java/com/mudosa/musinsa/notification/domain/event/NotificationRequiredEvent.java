package com.mudosa.musinsa.notification.domain.event;

/**
 * 알림 생성 이벤트
 * @param userId 알림을 받는 userId
 * @param chatId
 */

public record NotificationRequiredEvent(Long userId, Long chatId, String message) {
    public Long getUserId() {
        return userId;
    }
    public Long getChatId() {
        return chatId;
    }

}
