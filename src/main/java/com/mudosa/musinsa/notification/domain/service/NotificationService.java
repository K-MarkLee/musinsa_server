package com.mudosa.musinsa.notification.domain.service;

import com.mudosa.musinsa.notification.domain.dto.NotificationDTO;
import com.mudosa.musinsa.notification.domain.model.Notification;
import com.mudosa.musinsa.notification.domain.repository.NotificationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.modelmapper.ModelMapper;

import java.util.ArrayList;
import java.util.List;

/**
 * 필요한 기능
 * 1. 어떤 사용자의 알림 목록 열람
 * 2. 어떤
 */

@Service
@Transactional
@RequiredArgsConstructor
public class NotificationService {
//    private final ModelMapper modelMapper;
    private final NotificationRepository notificationRepository;

    public List<NotificationDTO> get(Long userId){
        List<Notification> result = notificationRepository.findByUserId(userId);
        List<NotificationDTO> resultDTO = new ArrayList<>();
//        for (Notification notification : result) {
////            resultDTO.add(modelMapper.map(notification, NotificationDTO.class));
//        }
        return resultDTO;
    }
}
