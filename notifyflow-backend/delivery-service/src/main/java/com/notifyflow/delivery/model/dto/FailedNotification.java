package com.notifyflow.delivery.model.dto;

import com.notiflyflow.notifycommon.dto.NotificationChannel;
import com.notiflyflow.notifycommon.dto.Priority;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FailedNotification {
    private UUID notificationId;
    private String recipientUserId;
    private NotificationChannel channel;
    private Priority priority;
    private String subject;
    private String body;
    private String errorMessage;
    private int retryCount;
    private LocalDateTime failedAt;
    private boolean reprocessed;
}
