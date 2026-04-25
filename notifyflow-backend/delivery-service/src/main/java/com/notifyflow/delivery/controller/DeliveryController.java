package com.notifyflow.delivery.controller;

import com.notiflyflow.notifycommon.dto.NotificationChannel;
import com.notiflyflow.notifycommon.dto.Priority;
import com.notifyflow.delivery.model.dto.ChannelStats;
import com.notifyflow.delivery.model.dto.DeliveryMetricsResponse;
import com.notifyflow.delivery.model.dto.FailedNotification;
import com.notifyflow.delivery.model.dto.PriorityStats;
import com.notifyflow.delivery.model.dto.QueueMetricsResponse;
import com.notifyflow.delivery.service.FailedNotificationService;
import com.notifyflow.delivery.service.MetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/delivery")
@RequiredArgsConstructor
@Slf4j
public class DeliveryController {

    private final MetricsService metricsService;
    private final FailedNotificationService failedNotificationService;

    @GetMapping("/metrics")
    public ResponseEntity<DeliveryMetricsResponse> getMetrics() {
        log.info("Fetching delivery metrics");
        DeliveryMetricsResponse metrics = metricsService.getMetrics();
        return ResponseEntity.ok(metrics);
    }

    @GetMapping("/metrics/channel")
    public ResponseEntity<List<ChannelStats>> getChannelStats() {
        log.info("Fetching channel stats");
        List<ChannelStats> stats = metricsService.getChannelStats();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/metrics/priority")
    public ResponseEntity<List<PriorityStats>> getPriorityStats() {
        log.info("Fetching priority stats");
        List<PriorityStats> stats = metricsService.getPriorityStats();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/queue")
    public ResponseEntity<QueueMetricsResponse> getQueueMetrics() {
        log.info("Fetching queue metrics");
        QueueMetricsResponse metrics = QueueMetricsResponse.builder()
                .high(metricsService.getQueueSizeByPriority(Priority.HIGH))
                .medium(metricsService.getQueueSizeByPriority(Priority.MEDIUM))
                .low(metricsService.getQueueSizeByPriority(Priority.LOW))
                .email(metricsService.getQueueSizeByChannel(NotificationChannel.EMAIL))
                .sms(metricsService.getQueueSizeByChannel(NotificationChannel.SMS))
                .inApp(metricsService.getQueueSizeByChannel(NotificationChannel.IN_APP))
                .avgWaitTime(0.0)
                .activeWorkers(8)
                .build();
        return ResponseEntity.ok(metrics);
    }

    @GetMapping("/failed")
    public ResponseEntity<List<FailedNotification>> getFailedNotifications(
            @RequestParam(defaultValue = "50") int limit) {
        log.info("Fetching failed notifications with limit: {}", limit);
        List<FailedNotification> failed = failedNotificationService.getFailedNotifications(limit);
        return ResponseEntity.ok(failed);
    }

    @DeleteMapping("/failed")
    public ResponseEntity<Void> clearFailedNotifications() {
        log.info("Clearing all failed notifications");
        failedNotificationService.clearFailedNotifications();
        return ResponseEntity.noContent().build();
    }
}
