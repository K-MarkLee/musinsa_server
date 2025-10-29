package com.mudosa.musinsa.event.presentation.dto.res;


import com.mudosa.musinsa.event.model.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter

public class EventListResDto {
    /*
    - 실제 API 응답에 사용할 DTO 클래스
    - controller => client로 데이터를 전달할 때 이 구조를 사용해서 응답
    */
    private Long eventId;
    private String title; //이벤트 이름
    private String description;

    // 이벤트 타입
    private Event.EventType eventType;
    private Event.EventStatus eventStatus;

    private Boolean isPublic;
    private Integer limitPerUser;
    private Event.LimitScope limitScope;

    private LocalDateTime startedAt;  //이벤트 시작 시간
    private LocalDateTime endedAt;  //이벤트 종료 시간

    // 정적 팩토리 메소드, 클래스 내부에 있어야 한다 !!
    // 추후에 service,controller 안에서 (EventListResDto::from)의 형태로 사용가능하다.
    public static EventListResDto from(Event event) {
        return new EventListResDto(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getEventType(),
                event.getStatus(),
                event.getIsPublic(),
                event.getLimitPerUser(),
                event.getLimitScope(),
                event.getStartedAt(),
                event.getEndedAt()
        );
    }
}
