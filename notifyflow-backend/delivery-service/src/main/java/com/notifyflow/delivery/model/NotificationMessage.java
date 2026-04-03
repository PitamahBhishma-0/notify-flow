package com.notifyflow.delivery.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMessage implements Serializable {
    private UUID notificationId;
    private String recipientUserId;
    private String channel;
    private String priority;
    private String subject;
    private String body;
}
