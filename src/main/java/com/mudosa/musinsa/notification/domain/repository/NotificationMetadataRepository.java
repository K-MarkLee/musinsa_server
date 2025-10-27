package com.mudosa.musinsa.notification.domain.repository;

import com.mudosa.musinsa.notification.domain.model.NotificationMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;

/**
 * NotificationMetadata Repository
 */
@Repository
public interface NotificationMetadataRepository extends JpaRepository<NotificationMetadata, BigInteger> {
    
    List<NotificationMetadata> findByNotificationType(String notificationType);
}
