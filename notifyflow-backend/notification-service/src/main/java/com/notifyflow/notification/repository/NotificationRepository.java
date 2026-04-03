package com.notifyflow.notification.repository;

import com.notifyflow.notification.model.entity.Notification;
import com.notifyflow.notification.model.enums.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    Page<Notification> findByRecipientUserId(String userId, Pageable pageable);
    Page<Notification> findByStatus(NotificationStatus status, Pageable pageable);
    List<Notification> findByStatusAndRetryCountLessThan(NotificationStatus status, int maxRetries);
}
