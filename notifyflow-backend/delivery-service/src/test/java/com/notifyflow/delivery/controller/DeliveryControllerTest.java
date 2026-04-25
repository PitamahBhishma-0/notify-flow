package com.notifyflow.delivery.controller;

import com.notifyflow.delivery.model.dto.ChannelStats;
import com.notifyflow.delivery.model.dto.DeliveryMetricsResponse;
import com.notifyflow.delivery.model.dto.FailedNotification;
import com.notifyflow.delivery.model.dto.PriorityStats;
import com.notifyflow.delivery.model.dto.QueueMetricsResponse;
import com.notifyflow.delivery.service.FailedNotificationService;
import com.notifyflow.delivery.service.MetricsService;
import com.notiflyflow.notifycommon.dto.NotificationChannel;
import com.notiflyflow.notifycommon.dto.Priority;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DeliveryControllerTest {

    @Mock
    private MetricsService metricsService;

    @Mock
    private FailedNotificationService failedNotificationService;

    @InjectMocks
    private DeliveryController deliveryController;

    private DeliveryMetricsResponse mockMetrics;
    private List<ChannelStats> mockChannelStats;
    private List<PriorityStats> mockPriorityStats;
    private List<FailedNotification> mockFailedNotifications;
    private QueueMetricsResponse mockQueueMetrics;

    @BeforeEach
    void setUp() {
        mockMetrics = DeliveryMetricsResponse.builder()
                .totalSent(1000)
                .delivered(950)
                .failed(50)
                .inQueue(100)
                .avgDeliveryTime(1.5)
                .successRate(95.0)
                .build();

        mockChannelStats = List.of(
                ChannelStats.builder()
                        .channel(NotificationChannel.EMAIL)
                        .sent(500)
                        .delivered(480)
                        .failed(20)
                        .avgTime(1.2)
                        .build(),
                ChannelStats.builder()
                        .channel(NotificationChannel.SMS)
                        .sent(300)
                        .delivered(290)
                        .failed(10)
                        .avgTime(0.8)
                        .build(),
                ChannelStats.builder()
                        .channel(NotificationChannel.IN_APP)
                        .sent(200)
                        .delivered(180)
                        .failed(20)
                        .avgTime(0.3)
                        .build()
        );

        mockPriorityStats = List.of(
                PriorityStats.builder()
                        .priority(Priority.HIGH)
                        .sent(400)
                        .delivered(390)
                        .failed(10)
                        .build(),
                PriorityStats.builder()
                        .priority(Priority.MEDIUM)
                        .sent(400)
                        .delivered(380)
                        .failed(20)
                        .build(),
                PriorityStats.builder()
                        .priority(Priority.LOW)
                        .sent(200)
                        .delivered(180)
                        .failed(20)
                        .build()
        );

        mockFailedNotifications = List.of(
                FailedNotification.builder()
                        .notificationId(UUID.randomUUID())
                        .recipientUserId("user1")
                        .channel(NotificationChannel.EMAIL)
                        .priority(Priority.HIGH)
                        .subject("Test Subject 1")
                        .body("Test Body 1")
                        .errorMessage("SMTP error")
                        .retryCount(2)
                        .failedAt(LocalDateTime.now())
                        .reprocessed(false)
                        .build(),
                FailedNotification.builder()
                        .notificationId(UUID.randomUUID())
                        .recipientUserId("user2")
                        .channel(NotificationChannel.SMS)
                        .priority(Priority.MEDIUM)
                        .subject("Test Subject 2")
                        .body("Test Body 2")
                        .errorMessage("Invalid phone number")
                        .retryCount(1)
                        .failedAt(LocalDateTime.now())
                        .reprocessed(true)
                        .build()
        );

        mockQueueMetrics = QueueMetricsResponse.builder()
                .high(100)
                .medium(200)
                .low(50)
                .email(150)
                .sms(100)
                .inApp(100)
                .avgWaitTime(1.2)
                .activeWorkers(8)
                .build();
    }

    @Test
    void getMetrics_shouldReturnMetrics() {
        when(metricsService.getMetrics()).thenReturn(mockMetrics);

        ResponseEntity<DeliveryMetricsResponse> response = deliveryController.getMetrics();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockMetrics, response.getBody());
        assertEquals(1000, response.getBody().getTotalSent());
        assertEquals(950, response.getBody().getDelivered());
        assertEquals(50, response.getBody().getFailed());
        assertEquals(100, response.getBody().getInQueue());
        assertEquals(1.5, response.getBody().getAvgDeliveryTime(), 0.01);
        assertEquals(95.0, response.getBody().getSuccessRate(), 0.01);

        verify(metricsService, times(1)).getMetrics();
    }

    @Test
    void getChannelStats_shouldReturnChannelStats() {
        when(metricsService.getChannelStats()).thenReturn(mockChannelStats);

        ResponseEntity<List<ChannelStats>> response = deliveryController.getChannelStats();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(3, response.getBody().size());
        assertEquals(NotificationChannel.EMAIL, response.getBody().get(0).getChannel());
        assertEquals(500, response.getBody().get(0).getSent());
        assertEquals(480, response.getBody().get(0).getDelivered());
        assertEquals(20, response.getBody().get(0).getFailed());

        verify(metricsService, times(1)).getChannelStats();
    }

    @Test
    void getPriorityStats_shouldReturnPriorityStats() {
        when(metricsService.getPriorityStats()).thenReturn(mockPriorityStats);

        ResponseEntity<List<PriorityStats>> response = deliveryController.getPriorityStats();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(3, response.getBody().size());
        assertEquals(Priority.HIGH, response.getBody().get(0).getPriority());
        assertEquals(400, response.getBody().get(0).getSent());
        assertEquals(390, response.getBody().get(0).getDelivered());
        assertEquals(10, response.getBody().get(0).getFailed());

        verify(metricsService, times(1)).getPriorityStats();
    }

    @Test
    void getQueueMetrics_shouldReturnQueueMetrics() {
        when(metricsService.getQueueSizeByPriority(Priority.HIGH)).thenReturn(100L);
        when(metricsService.getQueueSizeByPriority(Priority.MEDIUM)).thenReturn(200L);
        when(metricsService.getQueueSizeByPriority(Priority.LOW)).thenReturn(50L);
        when(metricsService.getQueueSizeByChannel(NotificationChannel.EMAIL)).thenReturn(150L);
        when(metricsService.getQueueSizeByChannel(NotificationChannel.SMS)).thenReturn(100L);
        when(metricsService.getQueueSizeByChannel(NotificationChannel.IN_APP)).thenReturn(100L);

        ResponseEntity<QueueMetricsResponse> response = deliveryController.getQueueMetrics();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(100, response.getBody().getHigh());
        assertEquals(200, response.getBody().getMedium());
        assertEquals(50, response.getBody().getLow());
        assertEquals(150, response.getBody().getEmail());
        assertEquals(100, response.getBody().getSms());
        assertEquals(100, response.getBody().getInApp());
        assertEquals(0.0, response.getBody().getAvgWaitTime(), 0.01);
        assertEquals(8, response.getBody().getActiveWorkers());

        verify(metricsService, times(1)).getQueueSizeByPriority(Priority.HIGH);
        verify(metricsService, times(1)).getQueueSizeByPriority(Priority.MEDIUM);
        verify(metricsService, times(1)).getQueueSizeByPriority(Priority.LOW);
        verify(metricsService, times(1)).getQueueSizeByChannel(NotificationChannel.EMAIL);
        verify(metricsService, times(1)).getQueueSizeByChannel(NotificationChannel.SMS);
        verify(metricsService, times(1)).getQueueSizeByChannel(NotificationChannel.IN_APP);
    }

    @Test
    void getFailedNotifications_shouldReturnFailedNotifications() {
        when(failedNotificationService.getFailedNotifications(50)).thenReturn(mockFailedNotifications);

        ResponseEntity<List<FailedNotification>> response = deliveryController.getFailedNotifications(50);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        assertEquals("user1", response.getBody().get(0).getRecipientUserId());
        assertEquals("user2", response.getBody().get(1).getRecipientUserId());

        verify(failedNotificationService, times(1)).getFailedNotifications(50);
    }

    @Test
    void getFailedNotifications_withDefaultLimit_shouldUseDefaultLimit() {
        when(failedNotificationService.getFailedNotifications(50)).thenReturn(mockFailedNotifications);

        ResponseEntity<List<FailedNotification>> response = deliveryController.getFailedNotifications(100);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(failedNotificationService, times(1)).getFailedNotifications(50);
    }

    @Test
    void getFailedNotifications_withCustomLimit_shouldUseCustomLimit() {
        when(failedNotificationService.getFailedNotifications(10)).thenReturn(mockFailedNotifications.subList(0, 1));

        ResponseEntity<List<FailedNotification>> response = deliveryController.getFailedNotifications(10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(failedNotificationService, times(1)).getFailedNotifications(10);
    }

    @Test
    void clearFailedNotifications_shouldReturnNoContent() {
        doNothing().when(failedNotificationService).clearFailedNotifications();

        ResponseEntity<Void> response = deliveryController.clearFailedNotifications();

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());

        verify(failedNotificationService, times(1)).clearFailedNotifications();
    }

    @Test
    void getFailedNotifications_withEmptyList_shouldReturnEmptyList() {
        when(failedNotificationService.getFailedNotifications(anyInt())).thenReturn(List.of());

        ResponseEntity<List<FailedNotification>> response = deliveryController.getFailedNotifications(50);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());

        verify(failedNotificationService, times(1)).getFailedNotifications(50);
    }
}
