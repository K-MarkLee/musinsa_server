package com.mudosa.musinsa.event.service;

//유저 입장 및 트래픽 관리 , 동시성 게이트

import org.springframework.stereotype.Service;
import com.mudosa.musinsa.exception.BusinessException;
import com.mudosa.musinsa.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;


import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Service
public class EventEntryService {

    /*
    * [EventEntryService] : 동시성 제어만 담당
    * */

    private static final long HOLD_MILLIS = 5_000L; // 5초동안 hold
    //현재 점유 중인 슬롯들을 해시맵 형태로 저장
    private final ConcurrentMap<String, Instant> activeEntries = new ConcurrentHashMap<>();

    public EventEntryToken acquireSlot(Long eventId, Long userId) {
        Objects.requireNonNull(eventId, "eventId cannot be null");
        Objects.requireNonNull(userId, "userId cannot be null");

        purgeExpiredEntries(); //  맵에 남아 있는 만료된 엔트리들을 미리 청소

        String key = buildKey(eventId,userId);
        Instant now = Instant.now();
        Instant previous = activeEntries.putIfAbsent(key, now.plusMillis(HOLD_MILLIS));
        if(previous != null) {
            log.warn("이벤트 동시 요청 거부 - eventId: {}, userId: {}\", eventId, userId ");
            throw new BusinessException(ErrorCode.EVENT_ENTRY_CONFLICT);
        }

        log.debug("이벤트 슬롯 확보 - eventId :{}, userId: {}", eventId, userId );
        return new EventEntryToken(key);
    }
    private void purgeExpiredEntries() {
        Instant now = Instant.now();
        activeEntries.entrySet().removeIf(entry -> entry.getValue().isBefore(now));
    }

    private String buildKey(Long eventId, Long userId) {
        return eventId + "-" + userId;
    }

    // 토큰 객체

    public class EventEntryToken implements AutoCloseable {
        private final String key;
        private boolean released;

        private EventEntryToken(String key) {
            this.key = key;
        }

        public void release() {
            // 자원을 수동으로 해제
            if (!released) {
                activeEntries.remove(key);
                released = true;
            }
        }

        @Override
        // AutoCloseable.close()를 "재정의"
            public void close() {
        release();
        }
    }




}
