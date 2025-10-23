package com.mudosa.musinsa.event.domain.model;

import com.mudosa.musinsa.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 이벤트 애그리거트 루트
 */
@Entity
@Table(name = "event")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Event extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    private Long id;
    
    @Column(name = "event_name", nullable = false, length = 100)
    private String eventName;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "event_start_time", nullable = false)
    private LocalDateTime eventStartTime;
    
    @Column(name = "event_end_time", nullable = false)
    private LocalDateTime eventEndTime;
    
    @Column(name = "stock_quantity")
    private Integer stockQuantity;
    
    @Column(name = "purchase_limit_per_user")
    private Integer purchaseLimitPerUser;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    // 이벤트 옵션 (같은 애그리거트)
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EventOption> eventOptions = new ArrayList<>();
    
    // 이벤트 이미지 (같은 애그리거트)
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EventImage> eventImages = new ArrayList<>();
    
    /**
     * 이벤트 생성
     */
    public static Event create(
        String eventName,
        String description,
        LocalDateTime startTime,
        LocalDateTime endTime,
        Integer stockQuantity,
        Integer purchaseLimitPerUser
    ) {
        Event event = new Event();
        event.eventName = eventName;
        event.description = description;
        event.eventStartTime = startTime;
        event.eventEndTime = endTime;
        event.stockQuantity = stockQuantity;
        event.purchaseLimitPerUser = purchaseLimitPerUser;
        event.isActive = true;
        return event;
    }
    
    /**
     * 이벤트 옵션 추가
     */
    public void addEventOption(EventOption option) {
        this.eventOptions.add(option);
        option.assignEvent(this);
    }
    
    /**
     * 이벤트 이미지 추가
     */
    public void addEventImage(EventImage image) {
        this.eventImages.add(image);
        image.assignEvent(this);
    }
    
    /**
     * 이벤트 진행 중 여부
     */
    public boolean isOngoing() {
        LocalDateTime now = LocalDateTime.now();
        return isActive 
            && !now.isBefore(eventStartTime) 
            && !now.isAfter(eventEndTime);
    }
}
