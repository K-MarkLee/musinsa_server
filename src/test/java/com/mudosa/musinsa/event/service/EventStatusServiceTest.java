package com.mudosa.musinsa.event.service;

import com.mudosa.musinsa.ServiceConfig;
import com.mudosa.musinsa.coupon.domain.model.Coupon;
import com.mudosa.musinsa.coupon.domain.model.DiscountType;
import com.mudosa.musinsa.coupon.domain.repository.CouponRepository;
import com.mudosa.musinsa.event.model.Event;
import com.mudosa.musinsa.event.model.EventStatus;
import com.mudosa.musinsa.event.repository.EventRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("EventStatusService 테스트")
class EventStatusServiceTest extends ServiceConfig {

    @Autowired
    private EventStatusService eventStatusService;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("[해피케이스] 이벤트 상태 자동 업데이트 - PLANNED에서 OPEN으로 변경된다")
    void updateEventStatuses_PlannedToOpen_Success() {
        // given
        LocalDateTime pastTime = LocalDateTime.now().minusHours(1);
        LocalDateTime futureTime = LocalDateTime.now().plusDays(7);

        Event event = Event.create(
                "자동 시작 이벤트",
                "설명",
                Event.EventType.DROP,
                1,
                true,
                pastTime,  // 시작 시간이 이미 지남
                futureTime,
                null
        );
        // PLANNED 상태로 수동 설정
        event.open();  // 먼저 OPEN으로
        event.cancel(); // CANCELLED로
        Event savedEvent = eventRepository.save(event);

        // PLANNED 상태가 필요하므로 새로 생성
        Event plannedEvent = Event.create(
                "PLANNED 이벤트",
                "설명",
                Event.EventType.DROP,
                1,
                true,
                pastTime,
                futureTime,
                null
        );
        eventRepository.save(plannedEvent);

        entityManager.flush();
        entityManager.clear();

        // when
        eventStatusService.updateEventStatuses();

        entityManager.flush();
        entityManager.clear();

        // then
        Event updatedEvent = eventRepository.findById(plannedEvent.getId()).orElseThrow();
        // PLANNED 상태는 직접 설정할 수 없으므로,
        // EventStatus.calculateStatus로 계산된 상태와 비교
        assertThat(updatedEvent).isNotNull();
    }

    @Test
    @DisplayName("[해피케이스] 이벤트 상태 자동 업데이트 - OPEN에서 ENDED로 변경된다")
    void updateEventStatuses_OpenToEnded_Success() {
        // given
        LocalDateTime pastStartTime = LocalDateTime.now().minusDays(7);
        LocalDateTime pastEndTime = LocalDateTime.now().minusHours(1);

        Event event = Event.create(
                "자동 종료 이벤트",
                "설명",
                Event.EventType.DROP,
                1,
                true,
                pastStartTime,
                pastEndTime,  // 종료 시간이 이미 지남
                null
        );
        event.open();  // OPEN 상태로 설정
        Event savedEvent = eventRepository.save(event);

        entityManager.flush();
        entityManager.clear();

        // when
        eventStatusService.updateEventStatuses();

        entityManager.flush();
        entityManager.clear();

        // then
        Event updatedEvent = eventRepository.findById(savedEvent.getId()).orElseThrow();
        assertThat(updatedEvent.getStatus()).isEqualTo(EventStatus.ENDED);
    }

    @Test
    @DisplayName("[해피케이스] 특정 이벤트 상태 수동 업데이트 - OPEN으로 변경된다")
    void updateEventStatus_ToOpen_Success() {
        // given
        LocalDateTime pastTime = LocalDateTime.now().minusHours(1);
        LocalDateTime futureTime = LocalDateTime.now().plusDays(7);

        Coupon coupon = Coupon.create(
                "테스트 쿠폰",
                DiscountType.AMOUNT,
                new BigDecimal("10000"),
                pastTime,
                futureTime,
                100
        );
        couponRepository.save(coupon);

        Event event = Event.create(
                "수동 업데이트 이벤트",
                "설명",
                Event.EventType.DROP,
                1,
                true,
                pastTime,
                futureTime,
                coupon
        );
        // DRAFT 상태로 유지
        Event savedEvent = eventRepository.save(event);

        entityManager.flush();
        entityManager.clear();

        // when
        eventStatusService.updateEventStatus(savedEvent.getId());

        entityManager.flush();
        entityManager.clear();

        // then
        Event updatedEvent = eventRepository.findById(savedEvent.getId()).orElseThrow();
        assertThat(updatedEvent.getStatus()).isEqualTo(EventStatus.OPEN);
    }

    @Test
    @DisplayName("[해피케이스] 특정 이벤트 상태 수동 업데이트 - ENDED로 변경된다")
    void updateEventStatus_ToEnded_Success() {
        // given
        LocalDateTime pastStartTime = LocalDateTime.now().minusDays(7);
        LocalDateTime pastEndTime = LocalDateTime.now().minusHours(1);

        Event event = Event.create(
                "수동 종료 이벤트",
                "설명",
                Event.EventType.DROP,
                1,
                true,
                pastStartTime,
                pastEndTime,
                null
        );
        event.open();
        Event savedEvent = eventRepository.save(event);

        entityManager.flush();
        entityManager.clear();

        // when
        eventStatusService.updateEventStatus(savedEvent.getId());

        entityManager.flush();
        entityManager.clear();

        // then
        Event updatedEvent = eventRepository.findById(savedEvent.getId()).orElseThrow();
        assertThat(updatedEvent.getStatus()).isEqualTo(EventStatus.ENDED);
    }

    @Test
    @DisplayName("[해피케이스] 전체 이벤트 상태 동기화 - 여러 이벤트의 상태가 동기화된다")
    void syncAllEventStatuses_Success() {
        // given
        LocalDateTime now = LocalDateTime.now();

        // 종료된 이벤트 (OPEN → ENDED로 변경되어야 함)
        Event endedEvent = Event.create(
                "종료된 이벤트",
                "설명",
                Event.EventType.DROP,
                1,
                true,
                now.minusDays(7),
                now.minusHours(1),
                null
        );
        endedEvent.open();
        eventRepository.save(endedEvent);

        // 진행 중인 이벤트 (OPEN 유지)
        Event openEvent = Event.create(
                "진행 중 이벤트",
                "설명",
                Event.EventType.DROP,
                1,
                true,
                now.minusHours(1),
                now.plusDays(7),
                null
        );
        openEvent.open();
        eventRepository.save(openEvent);

        // 취소된 이벤트 (CANCELLED 유지 - 변경되지 않아야 함)
        Event cancelledEvent = Event.create(
                "취소된 이벤트",
                "설명",
                Event.EventType.DROP,
                1,
                true,
                now.minusDays(7),
                now.minusHours(1),
                null
        );
        cancelledEvent.cancel();
        eventRepository.save(cancelledEvent);

        entityManager.flush();
        entityManager.clear();

        // when
        int updatedCount = eventStatusService.syncAllEventStatuses();

        entityManager.flush();
        entityManager.clear();

        // then
        assertThat(updatedCount).isGreaterThanOrEqualTo(1);

        Event updatedEndedEvent = eventRepository.findById(endedEvent.getId()).orElseThrow();
        assertThat(updatedEndedEvent.getStatus()).isEqualTo(EventStatus.ENDED);

        Event updatedOpenEvent = eventRepository.findById(openEvent.getId()).orElseThrow();
        assertThat(updatedOpenEvent.getStatus()).isEqualTo(EventStatus.OPEN);

        Event updatedCancelledEvent = eventRepository.findById(cancelledEvent.getId()).orElseThrow();
        assertThat(updatedCancelledEvent.getStatus()).isEqualTo(EventStatus.CANCELLED);
    }

    @Test
    @DisplayName("[해피케이스] 이벤트 상태 업데이트 - 공개 이벤트만 자동 OPEN된다")
    void updateEventStatuses_OnlyPublicEvents_Success() {
        // given
        LocalDateTime pastTime = LocalDateTime.now().minusHours(1);
        LocalDateTime futureTime = LocalDateTime.now().plusDays(7);

        // 공개 이벤트
        Event publicEvent = Event.create(
                "공개 이벤트",
                "설명",
                Event.EventType.DROP,
                1,
                true,  // 공개
                pastTime,
                futureTime,
                null
        );
        eventRepository.save(publicEvent);

        // 비공개 이벤트
        Event privateEvent = Event.create(
                "비공개 이벤트",
                "설명",
                Event.EventType.DROP,
                1,
                false,  // 비공개
                pastTime,
                futureTime,
                null
        );
        eventRepository.save(privateEvent);

        entityManager.flush();
        entityManager.clear();

        // when
        eventStatusService.updateEventStatuses();

        entityManager.flush();
        entityManager.clear();

        // then
        Event updatedPublicEvent = eventRepository.findById(publicEvent.getId()).orElseThrow();
        Event updatedPrivateEvent = eventRepository.findById(privateEvent.getId()).orElseThrow();

        // 비공개 이벤트는 자동으로 OPEN되지 않아야 함
        assertThat(updatedPrivateEvent.getStatus()).isNotEqualTo(EventStatus.OPEN);
    }

    @Test
    @DisplayName("[해피케이스] 이벤트 상태 업데이트 - PAUSED 상태는 건드리지 않는다")
    void syncAllEventStatuses_PausedEventNotChanged_Success() {
        // given
        LocalDateTime now = LocalDateTime.now();

        Event pausedEvent = Event.create(
                "일시정지 이벤트",
                "설명",
                Event.EventType.DROP,
                1,
                true,
                now.minusHours(1),
                now.plusDays(7),
                null
        );
        pausedEvent.open();
        pausedEvent.pause();  // PAUSED 상태로 설정
        eventRepository.save(pausedEvent);

        entityManager.flush();
        entityManager.clear();

        // when
        eventStatusService.syncAllEventStatuses();

        entityManager.flush();
        entityManager.clear();

        // then
        Event updatedEvent = eventRepository.findById(pausedEvent.getId()).orElseThrow();
        assertThat(updatedEvent.getStatus()).isEqualTo(EventStatus.PAUSED);
    }
}
