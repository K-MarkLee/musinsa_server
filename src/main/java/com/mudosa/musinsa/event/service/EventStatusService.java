package com.mudosa.musinsa.event.service;

import com.mudosa.musinsa.event.model.Event;
import com.mudosa.musinsa.event.model.EventStatus;
import com.mudosa.musinsa.event.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled; //스케줄러 사용
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 이벤트 상태 변경 로직 (시스템 단) 분리
 *
 * 목적:
 * - 주기적으로 이벤트의 상태를 자동으로 업데이트
 * - PLANNED → OPEN: 시작 시간이 도래한 이벤트
 * - OPEN → ENDED: 종료 시간이 지난 이벤트
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EventStatusService {

    private final EventRepository eventRepository;

    /**
     * 이벤트 상태 자동 업데이트 (1분마다 실행)
     *
     * cron 표현식: "초 분 시 일 월 요일"
     * "0 * * * * *" = 매분 0초에 실행
     */
    @Scheduled(cron = "0 * * * * *")  // 매분 실행
    @Transactional
    public void updateEventStatuses() {
        LocalDateTime now = LocalDateTime.now();

        log.debug("이벤트 상태 자동 업데이트 시작 - {}", now);

        int openedCount = updatePlannedToOpen(now);
        int endedCount = updateOpenToEnded(now);

        if (openedCount > 0 || endedCount > 0) {
            log.info("이벤트 상태 업데이트 완료 - OPEN: {}건, ENDED: {}건", openedCount, endedCount);
        }
    }

    /**
     * PLANNED/DRAFT 상태의 이벤트 중 시작 시간이 도래한 이벤트를 OPEN으로 변경
     */
    private int updatePlannedToOpen(LocalDateTime now) {
        // PLANNED 상태이면서 시작 시간이 지난 이벤트 조회
        List<Event> plannedEvents = eventRepository.findAllByStatusAndStartedAtBefore(
                EventStatus.PLANNED, now
        );

        // DRAFT 상태이면서 시작 시간이 지난 이벤트도 조회
        List<Event> draftEvents = eventRepository.findAllByStatusAndStartedAtBefore(
                EventStatus.DRAFT, now
        );

        int count = 0;

        // PLANNED → OPEN
        for (Event event : plannedEvents) {
            if (event.getIsPublic()) {  // 공개 이벤트만 자동 OPEN
                event.open();
                count++;
                log.info("이벤트 자동 시작 - eventId: {}, title: {}", event.getId(), event.getTitle());
            }
        }

        // DRAFT → OPEN (선택적, 필요한 경우만)
        for (Event event : draftEvents) {
            // DRAFT 상태는 수동으로만 열도록 하려면 이 부분 제거
            // 자동으로 열려면 아래 주석 해제
            /*
            if (event.getIsPublic() && shouldAutoOpen(event)) {
                event.open();
                count++;
                log.info("이벤트 자동 시작 (DRAFT→OPEN) - eventId: {}, title: {}", event.getId(), event.getTitle());
            }
            */
        }

        return count;
    }

    /**
     * OPEN 상태의 이벤트 중 종료 시간이 지난 이벤트를 ENDED로 변경
     */
    private int updateOpenToEnded(LocalDateTime now) {
        // OPEN 상태이면서 종료 시간이 지난 이벤트 조회
        List<Event> openEvents = eventRepository.findAllByStatusAndEndedAtBefore(
                EventStatus.OPEN, now
        );

        int count = 0;

        for (Event event : openEvents) {
            event.end();
            count++;
            log.info("이벤트 자동 종료 - eventId: {}, title: {}", event.getId(), event.getTitle());
        }

        return count;
    }

    /**
     * 수동으로 특정 이벤트의 상태를 업데이트 (즉시 실행)
     *
     * @param eventId 업데이트할 이벤트 ID
     */
    @Transactional
    public void updateEventStatus(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("이벤트를 찾을 수 없습니다: " + eventId));

        LocalDateTime now = LocalDateTime.now();
        EventStatus calculatedStatus = EventStatus.calculateStatus(event, now);

        // 계산된 상태와 현재 DB 상태가 다르면 업데이트
        if (event.getStatus() != calculatedStatus) {
            switch (calculatedStatus) {
                case OPEN -> event.open();
                case ENDED -> event.end();
                // PLANNED는 자동으로 변경하지 않음 (시작 전 상태는 수동 관리)
            }
            log.info("이벤트 상태 수동 업데이트 - eventId: {}, {} → {}",
                    eventId, event.getStatus(), calculatedStatus);
        }
    }

    /**
     * 모든 이벤트의 상태를 강제로 동기화 (관리자용, 데이터 정합성 복구 시 사용)
     */
    @Transactional
    public int syncAllEventStatuses() {
        log.info("전체 이벤트 상태 동기화 시작");

        List<Event> allEvents = eventRepository.findAll();
        LocalDateTime now = LocalDateTime.now();
        int updatedCount = 0;

        for (Event event : allEvents) {
            EventStatus currentStatus = event.getStatus();
            EventStatus calculatedStatus = EventStatus.calculateStatus(event, now);

            // CANCELLED는 건드리지 않음
            if (currentStatus == EventStatus.CANCELLED) {
                continue;
            }

            // 계산된 상태와 다르면 업데이트
            if (currentStatus != calculatedStatus) {
                switch (calculatedStatus) {
                    case PLANNED -> {
                        // PLANNED로 되돌리지 않음 (이미 OPEN이면 유지)
                    }
                    case OPEN -> {
                        if (currentStatus != EventStatus.PAUSED) {  // PAUSED는 수동 관리
                            event.open();
                            updatedCount++;
                        }
                    }
                    case ENDED -> {
                        event.end();
                        updatedCount++;
                    }
                }
            }
        }

        log.info("전체 이벤트 상태 동기화 완료 - 업데이트: {}건 / 전체: {}건", updatedCount, allEvents.size());
        return updatedCount;
    }
}
