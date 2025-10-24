package com.mudosa.musinsa.notification.domain.model;

import com.mudosa.musinsa.common.domain.model.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 알림 메타데이터 애그리거트 루트
 * - Notification과 별도 애그리거트 (알림 템플릿 정보)
 */
@Entity
@Table(name = "notification_metadata")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationMetadata extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "n_metadata_id")
    private Long id;
    
    @Column(name = "notification_type", nullable = false, length = 50)
    private String notificationType;
    
    @Column(name = "title", nullable = false, length = 200)
    private String title;
    
    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;
    
    @Column(name = "link_url", length = 2048)
    private String linkUrl;
    
    /**
     * 알림 메타데이터 생성
     */
    public static NotificationMetadata create(
        String notificationType,
        String title,
        String message,
        String linkUrl
    ) {
        NotificationMetadata metadata = new NotificationMetadata();
        metadata.notificationType = notificationType;
        metadata.title = title;
        metadata.message = message;
        metadata.linkUrl = linkUrl;
        return metadata;
    }
}
