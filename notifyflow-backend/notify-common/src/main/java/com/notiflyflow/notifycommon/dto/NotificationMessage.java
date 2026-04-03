package com.notiflyflow.notifycommon.dto;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.UUID;

@Data
@Builder
public class NotificationMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private UUID notificationId;
    private String recipientUserId;
    private NotificationChannel channel;
    private Priority priority;
    private String subject;
    private String body;
    private boolean reprocessed;
    private int retryCount = 0;
}


