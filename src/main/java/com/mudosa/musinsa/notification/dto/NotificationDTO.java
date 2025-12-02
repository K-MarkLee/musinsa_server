package com.mudosa.musinsa.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationDTO {
    private Long notificationId;
    private Long userId;
    private Integer nMetadataId;
    private String notificationTitle;
    private String notificationMessage;
    private String notificationUrl;
    private Boolean notificationStatus;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
