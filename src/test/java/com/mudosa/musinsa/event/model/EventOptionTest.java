package com.mudosa.musinsa.event.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("EventOption 엔티티 테스트")
class EventOptionTest {

    @Test
    @DisplayName("[해피케이스] EventOption 생성 - 정상적으로 EventOption을 생성한다")
    void createEventOption_Success() {
        // given
        LocalDateTime startedAt = LocalDateTime.of(2025, 11, 20, 0, 0);
        LocalDateTime endedAt = LocalDateTime.of(2025, 12, 20, 23, 59);
        Event event = Event.create(
                "테스트 이벤트",
                "설명",
                Event.EventType.DROP,
                Event.LimitScope.EVENT,
                1,
                true,
                startedAt,
                endedAt,
                null
        );
        BigDecimal eventPrice = new BigDecimal("50000");
        Integer eventStock = 100;

        // when
        EventOption eventOption = EventOption.create(event, null, eventPrice, eventStock);

        // then
        assertThat(eventOption).isNotNull();
        assertThat(eventOption.getEvent()).isEqualTo(event);
        assertThat(eventOption.getEventPrice()).isEqualTo(eventPrice);
        assertThat(eventOption.getEventStock()).isEqualTo(eventStock);
    }

    @Test
    @DisplayName("[해피케이스] EventOption 생성 - eventStock이 null일 경우 0으로 초기화된다")
    void createEventOption_WithNullStock_InitializedToZero() {
        // given
        LocalDateTime startedAt = LocalDateTime.of(2025, 11, 20, 0, 0);
        LocalDateTime endedAt = LocalDateTime.of(2025, 12, 20, 23, 59);
        Event event = Event.create(
                "테스트 이벤트",
                "설명",
                Event.EventType.DROP,
                Event.LimitScope.EVENT,
                1,
                true,
                startedAt,
                endedAt,
                null
        );

        // when
        EventOption eventOption = EventOption.create(event, null, null, null);

        // then
        assertThat(eventOption.getEventStock()).isEqualTo(0);
    }

    @Test
    @DisplayName("[해피케이스] 재고 증가 - 재고를 증가시킨다")
    void increaseStock_Success() {
        // given
        LocalDateTime startedAt = LocalDateTime.of(2025, 11, 20, 0, 0);
        LocalDateTime endedAt = LocalDateTime.of(2025, 12, 20, 23, 59);
        Event event = Event.create(
                "테스트 이벤트",
                "설명",
                Event.EventType.DROP,
                Event.LimitScope.EVENT,
                1,
                true,
                startedAt,
                endedAt,
                null
        );
        EventOption eventOption = EventOption.create(event, null, new BigDecimal("50000"), 100);

        // when
        eventOption.increaseStock(50);

        // then
        assertThat(eventOption.getEventStock()).isEqualTo(150);
    }

    @Test
    @DisplayName("[해피케이스] 재고 감소 - 재고를 감소시킨다")
    void decreaseStock_Success() {
        // given
        LocalDateTime startedAt = LocalDateTime.of(2025, 11, 20, 0, 0);
        LocalDateTime endedAt = LocalDateTime.of(2025, 12, 20, 23, 59);
        Event event = Event.create(
                "테스트 이벤트",
                "설명",
                Event.EventType.DROP,
                Event.LimitScope.EVENT,
                1,
                true,
                startedAt,
                endedAt,
                null
        );
        EventOption eventOption = EventOption.create(event, null, new BigDecimal("50000"), 100);

        // when
        eventOption.decreaseStock(30);

        // then
        assertThat(eventOption.getEventStock()).isEqualTo(70);
    }

    @Test
    @DisplayName("[예외케이스] 재고 감소 - 재고가 부족하면 예외가 발생한다")
    void decreaseStock_WhenInsufficientStock_ThrowsException() {
        // given
        LocalDateTime startedAt = LocalDateTime.of(2025, 11, 20, 0, 0);
        LocalDateTime endedAt = LocalDateTime.of(2025, 12, 20, 23, 59);
        Event event = Event.create(
                "테스트 이벤트",
                "설명",
                Event.EventType.DROP,
                Event.LimitScope.EVENT,
                1,
                true,
                startedAt,
                endedAt,
                null
        );
        EventOption eventOption = EventOption.create(event, null, new BigDecimal("50000"), 10);

        // when & then
        assertThatThrownBy(() -> eventOption.decreaseStock(20))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이벤트 재고가 부족합니다.");
    }

    @Test
    @DisplayName("[예외케이스] 재고 감소 - 재고를 0 미만으로 감소시키면 예외가 발생한다")
    void decreaseStock_WhenResultingNegativeStock_ThrowsException() {
        // given
        LocalDateTime startedAt = LocalDateTime.of(2025, 11, 20, 0, 0);
        LocalDateTime endedAt = LocalDateTime.of(2025, 12, 20, 23, 59);
        Event event = Event.create(
                "테스트 이벤트",
                "설명",
                Event.EventType.DROP,
                Event.LimitScope.EVENT,
                1,
                true,
                startedAt,
                endedAt,
                null
        );
        EventOption eventOption = EventOption.create(event, null, new BigDecimal("50000"), 5);

        // when & then
        assertThatThrownBy(() -> eventOption.decreaseStock(6))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이벤트 재고가 부족합니다.");
    }

    @Test
    @DisplayName("[해피케이스] Event 할당 - EventOption에 Event를 할당한다")
    void assignEvent_Success() {
        // given
        LocalDateTime startedAt = LocalDateTime.of(2025, 11, 20, 0, 0);
        LocalDateTime endedAt = LocalDateTime.of(2025, 12, 20, 23, 59);
        Event event1 = Event.create(
                "테스트 이벤트1",
                "설명1",
                Event.EventType.DROP,
                Event.LimitScope.EVENT,
                1,
                true,
                startedAt,
                endedAt,
                null
        );
        Event event2 = Event.create(
                "테스트 이벤트2",
                "설명2",
                Event.EventType.DROP,
                Event.LimitScope.EVENT,
                1,
                true,
                startedAt,
                endedAt,
                null
        );
        EventOption eventOption = EventOption.create(event1, null, new BigDecimal("50000"), 100);

        // when
        eventOption.assignEvent(event2);

        // then
        assertThat(eventOption.getEvent()).isEqualTo(event2);
    }
}
