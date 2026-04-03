package com.notifyflow.notification.service;

import com.notiflyflow.notifycommon.dto.NotificationMessage;
import com.notifyflow.notification.messaging.NotificationProducer;
import com.notifyflow.notification.model.dto.NotificationRequest;
import com.notifyflow.notification.model.dto.NotificationResponse;
import com.notifyflow.notification.model.entity.Notification;
import com.notifyflow.notification.model.enums.NotificationStatus;
import com.notifyflow.notification.repository.NotificationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository repository;
    private final NotificationProducer producer;

    @Transactional
    public NotificationResponse dispatch(NotificationRequest request) {
        Notification notification = Notification.builder()
                .recipientUserId(request.getRecipientUserId())
                .channel(request.getChannel())
                .priority(request.getPriority())
                .subject(request.getSubject())
                .body(request.getBody())
                .status(NotificationStatus.QUEUED)
                .build();

        notification = repository.save(notification);

        NotificationMessage message = NotificationMessage.builder()
                .notificationId(notification.getId())
                .recipientUserId(notification.getRecipientUserId())
                .channel(notification.getChannel())
                .priority(notification.getPriority())
                .subject(notification.getSubject())
                .body(notification.getBody())
                .build();

        producer.send(message);
        log.info("Notification [{}] queued successfully", notification.getId());
        return toResponse(notification);
    }

    public NotificationResponse getById(UUID id) {
        return repository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Notification not found: " + id));
    }

    public Page<NotificationResponse> getByUser(String userId, Pageable pageable) {
        return repository.findByRecipientUserId(userId, pageable).map(this::toResponse);
    }

    public Page<NotificationResponse> getByStatus(NotificationStatus status, Pageable pageable) {
        return repository.findByStatus(status, pageable).map(this::toResponse);
    }

    private NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .recipientUserId(n.getRecipientUserId())
                .channel(n.getChannel())
                .priority(n.getPriority())
                .subject(n.getSubject())
                .status(n.getStatus())
                .retryCount(n.getRetryCount())
                .createdAt(n.getCreatedAt())
                .sentAt(n.getSentAt())
                .build();
    }
}
