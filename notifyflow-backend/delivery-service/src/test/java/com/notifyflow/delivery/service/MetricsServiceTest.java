package com.notifyflow.delivery.service;

import com.notiflyflow.notifycommon.dto.NotificationChannel;
import com.notiflyflow.notifycommon.dto.Priority;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MetricsServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private MetricsService metricsService;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void recordDelivery_shouldIncrementMetrics() {
        metricsService.recordDelivery(NotificationChannel.EMAIL, Priority.HIGH, 1500L);

        verify(valueOperations, times(1)).increment(eq("notify:metrics:channel:EMAIL:delivered"));
        verify(valueOperations, times(1)).increment(eq("notify:metrics:priority:HIGH:delivered"));
        verify(valueOperations, times(1)).increment(eq("notify:metrics:total:delivered"));
        verify(valueOperations, times(1)).increment(eq("notify:metrics:total:time"), eq(1500L));
    }

    @Test
    void recordFailure_shouldIncrementFailureMetrics() {
        metricsService.recordFailure(NotificationChannel.SMS, Priority.MEDIUM, "Connection timeout");

        verify(valueOperations, times(1)).increment(eq("notify:metrics:channel:SMS:failed"));
        verify(valueOperations, times(1)).increment(eq("notify:metrics:priority:MEDIUM:failed"));
        verify(valueOperations, times(1)).increment(eq("notify:metrics:total:failed"));
    }

    @Test
    void recordSent_shouldIncrementSentMetrics() {
        metricsService.recordSent(NotificationChannel.IN_APP, Priority.LOW);

        verify(valueOperations, times(1)).increment(eq("notify:metrics:channel:IN_APP:sent"));
        verify(valueOperations, times(1)).increment(eq("notify:metrics:priority:LOW:sent"));
        verify(valueOperations, times(1)).increment(eq("notify:metrics:total:sent"));
    }

    @Test
    void getMetrics_shouldReturnCorrectMetrics() {
        when(valueOperations.get("notify:metrics:total:sent")).thenReturn("100");
        when(valueOperations.get("notify:metrics:total:delivered")).thenReturn("95");
        when(valueOperations.get("notify:metrics:total:failed")).thenReturn("5");
        when(valueOperations.get("notify:metrics:total:time")).thenReturn("95000");
        when(valueOperations.get("notify:metrics:queue:high")).thenReturn("10");
        when(valueOperations.get("notify:metrics:queue:medium")).thenReturn("20");
        when(valueOperations.get("notify:metrics:queue:low")).thenReturn("5");
        when(redisTemplate.getExpire(anyString())).thenReturn(-1L);

        var metrics = metricsService.getMetrics();

        assertEquals(100, metrics.getTotalSent());
        assertEquals(95, metrics.getDelivered());
        assertEquals(5, metrics.getFailed());
        assertEquals(35, metrics.getInQueue());
        assertEquals(1.0, metrics.getAvgDeliveryTime(), 0.01);
        assertEquals(95.0, metrics.getSuccessRate(), 0.01);
    }

    @Test
    void getMetrics_withZeroDeliveries_shouldReturnZeroAvgTime() {
        when(valueOperations.get("notify:metrics:total:sent")).thenReturn("100");
        when(valueOperations.get("notify:metrics:total:delivered")).thenReturn("0");
        when(valueOperations.get("notify:metrics:total:failed")).thenReturn("5");
        when(valueOperations.get("notify:metrics:total:time")).thenReturn("0");
        when(valueOperations.get("notify:metrics:queue:high")).thenReturn("0");
        when(valueOperations.get("notify:metrics:queue:medium")).thenReturn("0");
        when(valueOperations.get("notify:metrics:queue:low")).thenReturn("0");
        when(redisTemplate.getExpire(anyString())).thenReturn(-1L);

        var metrics = metricsService.getMetrics();

        assertEquals(0.0, metrics.getAvgDeliveryTime(), 0.01);
    }

    @Test
    void getChannelStats_shouldReturnStatsForAllChannels() {
        when(valueOperations.get("notify:metrics:channel:EMAIL:sent")).thenReturn("50");
        when(valueOperations.get("notify:metrics:channel:EMAIL:delivered")).thenReturn("45");
        when(valueOperations.get("notify:metrics:channel:EMAIL:failed")).thenReturn("5");
        when(valueOperations.get("notify:metrics:channel:EMAIL:time")).thenReturn("45000");

        when(valueOperations.get("notify:metrics:channel:SMS:sent")).thenReturn("30");
        when(valueOperations.get("notify:metrics:channel:SMS:delivered")).thenReturn("28");
        when(valueOperations.get("notify:metrics:channel:SMS:failed")).thenReturn("2");
        when(valueOperations.get("notify:metrics:channel:SMS:time")).thenReturn("28000");

        when(valueOperations.get("notify:metrics:channel:IN_APP:sent")).thenReturn("20");
        when(valueOperations.get("notify:metrics:channel:IN_APP:delivered")).thenReturn("20");
        when(valueOperations.get("notify:metrics:channel:IN_APP:failed")).thenReturn("0");
        when(valueOperations.get("notify:metrics:channel:IN_APP:time")).thenReturn("20000");

        var stats = metricsService.getChannelStats();

        assertEquals(3, stats.size());
        assertEquals(NotificationChannel.EMAIL, stats.getFirst().getChannel());
        assertEquals(50, stats.getFirst().getSent());
        assertEquals(45, stats.getFirst().getDelivered());
        assertEquals(5, stats.getFirst().getFailed());
        assertEquals(1.0, stats.getFirst().getAvgTime(), 0.01);
    }

    @Test
    void getPriorityStats_shouldReturnStatsForAllPriorities() {
        when(valueOperations.get("notify:metrics:priority:HIGH:sent")).thenReturn("40");
        when(valueOperations.get("notify:metrics:priority:HIGH:delivered")).thenReturn("38");
        when(valueOperations.get("notify:metrics:priority:HIGH:failed")).thenReturn("2");

        when(valueOperations.get("notify:metrics:priority:MEDIUM:sent")).thenReturn("40");
        when(valueOperations.get("notify:metrics:priority:MEDIUM:delivered")).thenReturn("37");
        when(valueOperations.get("notify:metrics:priority:MEDIUM:failed")).thenReturn("3");

        when(valueOperations.get("notify:metrics:priority:LOW:sent")).thenReturn("20");
        when(valueOperations.get("notify:metrics:priority:LOW:delivered")).thenReturn("18");
        when(valueOperations.get("notify:metrics:priority:LOW:failed")).thenReturn("2");

        var stats = metricsService.getPriorityStats();

        assertEquals(3, stats.size());
        assertEquals(Priority.HIGH, stats.getFirst().getPriority());
        assertEquals(40, stats.getFirst().getSent());
        assertEquals(38, stats.getFirst().getDelivered());
        assertEquals(2, stats.getFirst().getFailed());
    }

    @Test
    void updateQueueSize_shouldIncrementQueueSize() {
        metricsService.updateQueueSize(Priority.HIGH, 5);

        verify(valueOperations, times(1)).increment(eq("notify:metrics:queue:high"), eq(5L));
    }

    @Test
    void getQueueSizeByPriority_shouldReturnCorrectSize() {
        when(valueOperations.get("notify:metrics:queue:high")).thenReturn("100");

        long size = metricsService.getQueueSizeByPriority(Priority.HIGH);

        assertEquals(100, size);
    }

    @Test
    void getQueueSizeByChannel_shouldReturnCorrectSize() {
        when(valueOperations.get("notify:metrics:queue:channel:EMAIL")).thenReturn("50");

        long size = metricsService.getQueueSizeByChannel(NotificationChannel.EMAIL);

        assertEquals(50, size);
    }

    @Test
    void updateQueueSizeByChannel_shouldIncrementQueueSize() {
        metricsService.updateQueueSizeByChannel(NotificationChannel.SMS, 10);

        verify(valueOperations, times(1)).increment(eq("notify:metrics:queue:channel:SMS"), eq(10L));
    }
}
