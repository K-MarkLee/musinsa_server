package com.mudosa.musinsa.notification.domain.model;

import com.mudosa.musinsa.common.domain.BaseEntity;
import com.mudosa.musinsa.user.domain.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 알림 애그리거트 루트
 */
@Entity
@Table(name = "notification")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notificationId;

    @ManyToOne
    @JoinColumn(name="user")
    private User user;

    @ManyToOne
    @JoinColumn(name="notification_metadata")
    private NotificationMetadata notificationMetadata;

    private String notificationTitle;
    private String notificationMessage;
    private String notificationUrl;
    private Boolean notificationStatus;
    private LocalDateTime readAt;

}
