package com.mudosa.musinsa.event.domain.model;

import com.mudosa.musinsa.common.domain.model.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 이벤트 이미지 엔티티
 * Event 애그리거트 내부
 */
@Entity
@Table(
        name = "event_image",
        indexes = {
                @Index(name = "idx_evtimg_event", columnList = "event_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EventImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_image_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "image_url", nullable = false, length = 2048)
    private String imageUrl;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    public static EventImage create(String imageUrl, int displayOrder) {
        EventImage image = new EventImage();
        image.imageUrl = imageUrl;
        image.displayOrder = displayOrder;
        return image;
    }

    /** Event 할당 (Event 애그리거트에서만 호출) */
    void assignEvent(Event event) {
        this.event = event;
    }
}
