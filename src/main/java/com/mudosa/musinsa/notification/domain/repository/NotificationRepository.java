package com.mudosa.musinsa.notification.domain.repository;

import com.mudosa.musinsa.notification.domain.model.Notification;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Notification Repository
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    List<Notification> findByUserId(Long userId);

    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.notificationStatus = true, n.readAt = CURRENT_TIMESTAMP WHERE n.notificationId = :notificationId")
    int updateNotificationStatus(@Param("notificationId")Long notificationId);
}
