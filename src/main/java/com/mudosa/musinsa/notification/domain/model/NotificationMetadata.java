package com.mudosa.musinsa.notification.domain.model;

import com.mudosa.musinsa.common.domain.model.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 알림 메타데이터 애그리거트 루트
 * - Notification과 별도 애그리거트 (알림 템플릿 정보)
 */
@Entity
@Table(name = "notification_metadata")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationMetadata extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long nMetadataId;

    private String notificationTitle;
    private String notificationMessage;
    private String notificationType;
    private String notificationCategory;
    private String notificationUrl;

}
