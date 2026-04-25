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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeduplicationServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private DeduplicationService deduplicationService;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void isDuplicate_withNewNotification_shouldReturnFalse() {
        UUID notificationId = UUID.randomUUID();
        when(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class))).thenReturn(true);

        boolean result = deduplicationService.isDuplicate(notificationId);

        assertFalse(result);
        verify(valueOperations, times(1)).setIfAbsent(
                eq("notify:dedup:" + notificationId),
                eq("1"),
                any(Duration.class)
        );
    }

    @Test
    void isDuplicate_withExistingNotification_shouldReturnTrue() {
        UUID notificationId = UUID.randomUUID();
        when(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class))).thenReturn(false);

        boolean result = deduplicationService.isDuplicate(notificationId);

        assertTrue(result);
        verify(valueOperations, times(1)).setIfAbsent(
                eq("notify:dedup:" + notificationId),
                eq("1"),
                any(Duration.class)
        );
    }

    @Test
    void isRateLimited_withFirstRequest_shouldReturnFalse() {
        String userId = "user123";
        String channel = "EMAIL";
        when(valueOperations.increment(anyString())).thenReturn(1L);

        boolean result = deduplicationService.isRateLimited(userId, channel);

        assertFalse(result);
        verify(valueOperations, times(1)).increment(eq("notify:rate:user123:EMAIL"));
        verify(redisTemplate, times(1)).expire(eq("notify:rate:user123:EMAIL"), any(Duration.class));
    }

    @Test
    void isRateLimited_withRequestUnderLimit_shouldReturnFalse() {
        String userId = "user123";
        String channel = "EMAIL";
        when(valueOperations.increment(anyString())).thenReturn(5L);

        boolean result = deduplicationService.isRateLimited(userId, channel);

        assertFalse(result);
        verify(valueOperations, times(1)).increment(eq("notify:rate:user123:EMAIL"));
        verify(redisTemplate, never()).expire(anyString(), any(Duration.class));
    }

    @Test
    void isRateLimited_withRequestAtLimit_shouldReturnTrue() {
        String userId = "user123";
        String channel = "EMAIL";
        when(valueOperations.increment(anyString())).thenReturn(11L);

        boolean result = deduplicationService.isRateLimited(userId, channel);

        assertTrue(result);
        verify(valueOperations, times(1)).increment(eq("notify:rate:user123:EMAIL"));
        verify(redisTemplate, never()).expire(anyString(), any(Duration.class));
    }

    @Test
    void isRateLimited_withRequestOverLimit_shouldReturnTrue() {
        String userId = "user123";
        String channel = "EMAIL";
        when(valueOperations.increment(anyString())).thenReturn(15L);

        boolean result = deduplicationService.isRateLimited(userId, channel);

        assertTrue(result);
        verify(valueOperations, times(1)).increment(eq("notify:rate:user123:EMAIL"));
        verify(redisTemplate, never()).expire(anyString(), any(Duration.class));
    }
}
