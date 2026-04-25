package com.notifyflow.delivery.model.dto;

import com.notiflyflow.notifycommon.dto.NotificationChannel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChannelStats {
    private NotificationChannel channel;
    private long sent;
    private long delivered;
    private long failed;
    private double avgTime;
}
