package com.mudosa.musinsa.event.model;

import java.time.LocalDateTime;

public enum EventStatus {
    DRAFT, PLANNED, OPEN, PAUSED, ENDED, CANCELLED;

    // 기존 service의 calculateEventStatus 로직 그대로
    public static EventStatus calculateStatus(Event event, LocalDateTime currentTime) {
        if (currentTime.isBefore(event.getStartedAt())) {
            return PLANNED;
        } else if (currentTime.isAfter(event.getEndedAt())) {
            return ENDED;
        } else {
            return OPEN;
        }
    }
}
