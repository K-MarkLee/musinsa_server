package com.mudosa.musinsa.event.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("EventImage 엔티티 테스트")
class EventImageTest {

    @Test
    @DisplayName("[해피케이스] EventImage 생성 - 썸네일 이미지로 생성한다")
    void createEventImage_WithThumbnailTrue_Success() {
        // given : 시나리오 진행에 필요한 모든 준비 과정
        String imageUrl = "https://example.com/thumbnail.jpg";
        Boolean isThumbnail = true;

        // when : 시나리오 행동 진행
        EventImage eventImage = EventImage.create(imageUrl, isThumbnail);

        // then: 시나리오 진행에 대한 결과 명시 , 검증
        assertThat(eventImage).isNotNull();
        assertThat(eventImage.getImageUrl()).isEqualTo(imageUrl);
        assertThat(eventImage.getIsThumbnail()).isTrue();
    }

    @Test
    @DisplayName("[해피케이스] EventImage 생성 - 일반 이미지로 생성한다")
    void createEventImage_WithThumbnailFalse_Success() {
        // given
        String imageUrl = "https://example.com/image.jpg";
        Boolean isThumbnail = false;

        // when
        EventImage eventImage = EventImage.create(imageUrl, isThumbnail);

        // then
        assertThat(eventImage).isNotNull();
        assertThat(eventImage.getImageUrl()).isEqualTo(imageUrl);
        assertThat(eventImage.getIsThumbnail()).isFalse();
    }

    @Test
    @DisplayName("[해피케이스] Event 할당 - EventImage에 Event를 할당한다")
    void assignEvent_Success() {
        // given
        LocalDateTime startedAt = LocalDateTime.of(2025, 11, 20, 0, 0);
        LocalDateTime endedAt = LocalDateTime.of(2025, 12, 20, 23, 59);
        Event event = Event.create(
                "테스트 이벤트",
                "설명",
                Event.EventType.DROP,
                1,
                true,
                startedAt,
                endedAt,
                null
        );
        EventImage eventImage = EventImage.create("https://example.com/image.jpg", true);

        // when
        eventImage.assignEvent(event);

        // then
        assertThat(eventImage.getEvent()).isEqualTo(event);
    }
}
