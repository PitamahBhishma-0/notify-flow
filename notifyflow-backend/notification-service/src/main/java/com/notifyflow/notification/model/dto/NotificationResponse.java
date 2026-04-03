package com.notifyflow.notification.model.dto;

import com.notiflyflow.notifycommon.dto.NotificationChannel;
import com.notiflyflow.notifycommon.dto.Priority;
import com.notifyflow.notification.model.enums.NotificationStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class NotificationResponse {
    private UUID id;
    private String recipientUserId;
    private NotificationChannel channel;
    private Priority priority;
    private String subject;
    private NotificationStatus status;
    private int retryCount;
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;
}
