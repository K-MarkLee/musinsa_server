package com.mudosa.musinsa.event.model;

import com.mudosa.musinsa.common.domain.model.BaseEntity;
import com.mudosa.musinsa.coupon.domain.model.Coupon;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Check;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "event",
        indexes = {
                @Index(name = "idx_event_status", columnList = "status"),
                @Index(name = "idx_event_period", columnList = "started_at, ended_at")
        }
)
@Check(constraints = "ended_at > started_at")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Event extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    private Long id;  

    // DDL: title VARCHAR(255) NOT NULL
    @Column(name = "title", nullable = false, length = 255)
    private String title;

    // DDL: description TEXT NULL
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // DDL: event_type ENUM('DROP','COMMENT','DISCOUNT') NOT NULL
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 20)
    private EventType eventType;

    // DDL: status ENUM('DRAFT','PLANNED','OPEN','PAUSED','ENDED','CANCELLED') NOT NULL DEFAULT 'DRAFT'
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private EventStatus status = EventStatus.DRAFT;

    // DDL: is_public BOOLEAN NOT NULL DEFAULT TRUE
    @Column(name = "is_public", nullable = false)
    private Boolean isPublic = true;

    // DDL: limit_per_user INT NOT NULL DEFAULT 1
    @Column(name = "limit_per_user", nullable = false)
    private Integer limitPerUser = 1;

    // DDL: limit_scope ENUM('EVENT','OPTION') NOT NULL DEFAULT 'EVENT'
    @Enumerated(EnumType.STRING)
    @Column(name = "limit_scope", nullable = false, length = 20)
    private LimitScope limitScope = LimitScope.EVENT;

    // DDL: started_at / ended_at NOT NULL
    // 이벤트 시작시간 , 종료 시간
    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt; // 로컬 데이트타임 내장 메서드 이용가능

    @Column(name = "ended_at", nullable = false)
    private LocalDateTime endedAt;

    // 같은 애그리거트 내부 연관(DDL 외부에 별도 테이블 필요)
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EventOption> eventOptions = new ArrayList<>();

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EventImage> eventImages = new ArrayList<>();

    /** 팩토리 메서드 */
    public static Event create(
            String title,
            String description,
            EventType eventType,
            LimitScope limitScope,
            int limitPerUser,
            boolean isPublic,
            LocalDateTime startedAt,
            LocalDateTime endedAt
    ) {
        Event e = new Event();
        e.title = title;
        e.description = description;
        e.eventType = eventType;
        e.status = EventStatus.DRAFT; // DDL default와 일치
        e.isPublic = isPublic;
        e.limitScope = limitScope;
        e.limitPerUser = limitPerUser;
        e.startedAt = startedAt;
        e.endedAt = endedAt;
        return e;
    }

    public void addEventOption(EventOption option) {
        this.eventOptions.add(option);
        option.assignEvent(this);
    }

    public void addEventImage(EventImage image) {
        this.eventImages.add(image);
        image.assignEvent(this);
    }

    /** 진행중 여부(비즈니스 로직) */
    public boolean isOngoing(LocalDateTime now) {
        return !now.isBefore(startedAt) && !now.isAfter(endedAt) && status == EventStatus.OPEN;
    }

    /** 상태 전이 예시 */
    public void open() { this.status = EventStatus.OPEN; }
    public void pause() { this.status = EventStatus.PAUSED; }
    public void end() { this.status = EventStatus.ENDED; }
    public void cancel() { this.status = EventStatus.CANCELLED; }

    // ===== Enums =====
    public enum EventType { DROP, COMMENT, DISCOUNT }
    public enum EventStatus { DRAFT, PLANNED, OPEN, PAUSED, ENDED, CANCELLED }
    public enum LimitScope { EVENT, OPTION }
}
