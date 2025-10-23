package com.mudosa.musinsa.notification.domain.repository;

import com.mudosa.musinsa.notification.domain.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Notification Repository
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    List<Notification> findByUserId(Long userId);
    
    List<Notification> findByUserIdAndIsReadFalse(Long userId);
}
