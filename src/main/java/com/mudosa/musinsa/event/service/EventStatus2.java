package com.mudosa.musinsa.event.service;

import com.mudosa.musinsa.event.model.Event;

import java.time.LocalDateTime;

public enum EventStatus2 {
    DRAFT, PLANNED, OPEN, PAUSED, ENDED, CANCELLED
;

    public EventStatus2 getSTatus(LocalDateTime currentTime,Event event){
        if (currentTime.isBefore(event.getStartedAt())) {
            return PLANNED;
        } else if (currentTime.isAfter(event.getEndedAt())) {
            return ENDED;
        } else {
            return OPEN;
        }
    }
}
