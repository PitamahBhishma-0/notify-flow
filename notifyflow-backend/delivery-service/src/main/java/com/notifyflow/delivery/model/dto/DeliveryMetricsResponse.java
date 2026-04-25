package com.notifyflow.delivery.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryMetricsResponse {
    private long totalSent;
    private long delivered;
    private long failed;
    private long inQueue;
    private double avgDeliveryTime;
    private double successRate;
}
