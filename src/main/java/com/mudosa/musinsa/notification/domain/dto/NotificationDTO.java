package com.mudosa.musinsa.notification.domain.dto;

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
    private Long nMetadataId;
    private String notificationTitle;
    private String notificationMessage;
    private String notificationUrl;
    private Boolean notificationStatus;
    private LocalDateTime readAt;
}
