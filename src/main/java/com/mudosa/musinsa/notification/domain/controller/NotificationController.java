package com.mudosa.musinsa.notification.domain.controller;

import com.mudosa.musinsa.notification.domain.dto.NotificationDTO;
import com.mudosa.musinsa.notification.domain.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notification")
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping("/{userId}")
    public List<NotificationDTO> readNotification(@PathVariable Long userId) {
        return notificationService.readNotification(userId);
    }

    @PatchMapping("/read")
    public int updateNotification(@RequestBody NotificationDTO notificationDTO) {
        return notificationService.updateNotificationState(notificationDTO.getNotificationId());
    }

    @PostMapping("/create/test")
    public NotificationDTO createNotification(@RequestBody NotificationDTO notificationDTO) {
        notificationService.createNotificationFromDTO(notificationDTO);
        return notificationDTO;
    }

}