package com.mudosa.musinsa.notification.domain.model;

import com.mudosa.musinsa.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 알림 애그리거트 루트
 */
@Entity
@Table(name = "notification")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId; // User 애그리거트 참조 (ID만)
    
    @Column(name = "n_metadata_id", nullable = false)
    private Long nMetadataId; // NotificationMetadata 참조 (ID만)
    
    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;
    
    /**
     * 알림 생성
     */
    public static Notification create(Long userId, Long metadataId) {
        Notification notification = new Notification();
        notification.userId = userId;
        notification.nMetadataId = metadataId;
        notification.isRead = false;
        return notification;
    }
    
    /**
     * 읽음 처리
     */
    public void markAsRead() {
        this.isRead = true;
    }
}
