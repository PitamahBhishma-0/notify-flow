package com.notifyflow.delivery.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueueMetricsResponse {
    private long high;
    private long medium;
    private long low;
    private long email;
    private long sms;
    private long inApp;
    private double avgWaitTime;
    private int activeWorkers;
}
