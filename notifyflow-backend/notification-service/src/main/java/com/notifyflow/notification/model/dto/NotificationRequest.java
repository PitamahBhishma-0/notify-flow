package com.notifyflow.notification.model.dto;

import com.notiflyflow.notifycommon.dto.NotificationChannel;
import com.notiflyflow.notifycommon.dto.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NotificationRequest {

    @NotBlank(message = "Recipient user ID is required")
    private String recipientUserId;

    @NotNull(message = "Channel is required")
    private NotificationChannel channel;

    @NotNull(message = "Priority is required")
    private Priority priority;

    @NotBlank(message = "Subject is required")
    private String subject;

    @NotBlank(message = "Body is required")
    private String body;
}
