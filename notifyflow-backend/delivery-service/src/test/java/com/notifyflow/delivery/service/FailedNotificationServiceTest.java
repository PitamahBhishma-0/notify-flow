package com.notifyflow.delivery.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notifyflow.delivery.model.dto.FailedNotification;
import com.notiflyflow.notifycommon.dto.NotificationChannel;
import com.notiflyflow.notifycommon.dto.NotificationMessage;
import com.notiflyflow.notifycommon.dto.Priority;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FailedNotificationServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ListOperations<String, String> listOperations;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private FailedNotificationService failedNotificationService;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForList()).thenReturn(listOperations);
    }

    @Test
    void recordFailed_shouldSerializeAndStoreNotification() throws Exception {
        UUID notificationId = UUID.randomUUID();
        NotificationMessage message = NotificationMessage.builder()
                .notificationId(notificationId)
                .recipientUserId("user123")
                .channel(NotificationChannel.EMAIL)
                .priority(Priority.HIGH)
                .subject("Test Subject")
                .body("Test Body")
                .retryCount(2)
                .reprocessed(false)
                .build();

        FailedNotification expectedFailed = FailedNotification.builder()
                .notificationId(notificationId)
                .recipientUserId("user123")
                .channel(NotificationChannel.EMAIL)
                .priority(Priority.HIGH)
                .subject("Test Subject")
                .body("Test Body")
                .errorMessage("Test error")
                .retryCount(2)
                .failedAt(LocalDateTime.now())
                .reprocessed(false)
                .build();

        when(objectMapper.writeValueAsString(any(FailedNotification.class))).thenReturn("json-string");

        failedNotificationService.recordFailed(message, "Test error");

        verify(objectMapper, times(1)).writeValueAsString(argThat((FailedNotification failed) ->
                failed.getNotificationId().equals(notificationId) &&
                failed.getRecipientUserId().equals("user123") &&
                failed.getErrorMessage().equals("Test error")
        ));
        verify(listOperations, times(1)).leftPush(eq("notify:failed:list"), eq("json-string"));
        verify(redisTemplate, times(1)).expire(eq("notify:failed:list"), any());
    }

    @Test
    void getFailedNotifications_shouldReturnListOfFailedNotifications() throws Exception {
        List<String> jsonList = List.of(
                "{\"notificationId\":\"id1\",\"recipientUserId\":\"user1\",\"channel\":\"EMAIL\",\"priority\":\"HIGH\",\"subject\":\"Subject1\",\"body\":\"Body1\",\"errorMessage\":\"Error1\",\"retryCount\":1,\"failedAt\":\"2024-01-01T10:00:00\",\"reprocessed\":false}",
                "{\"notificationId\":\"id2\",\"recipientUserId\":\"user2\",\"channel\":\"SMS\",\"priority\":\"MEDIUM\",\"subject\":\"Subject2\",\"body\":\"Body2\",\"errorMessage\":\"Error2\",\"retryCount\":2,\"failedAt\":\"2024-01-01T11:00:00\",\"reprocessed\":true}"
        );

        when(listOperations.range("notify:failed:list", 0, 49)).thenReturn(jsonList);

        FailedNotification failed1 = FailedNotification.builder()
                .notificationId(UUID.fromString("00000000-0000-0000-0000-000000000001"))
                .recipientUserId("user1")
                .channel(NotificationChannel.EMAIL)
                .priority(Priority.HIGH)
                .subject("Subject1")
                .body("Body1")
                .errorMessage("Error1")
                .retryCount(1)
                .failedAt(LocalDateTime.of(2024, 1, 1, 10, 0))
                .reprocessed(false)
                .build();

        FailedNotification failed2 = FailedNotification.builder()
                .notificationId(UUID.fromString("00000000-0000-0000-0000-000000000002"))
                .recipientUserId("user2")
                .channel(NotificationChannel.SMS)
                .priority(Priority.MEDIUM)
                .subject("Subject2")
                .body("Body2")
                .errorMessage("Error2")
                .retryCount(2)
                .failedAt(LocalDateTime.of(2024, 1, 1, 11, 0))
                .reprocessed(true)
                .build();

        when(objectMapper.readValue(jsonList.get(0), FailedNotification.class)).thenReturn(failed1);
        when(objectMapper.readValue(jsonList.get(1), FailedNotification.class)).thenReturn(failed2);

        List<FailedNotification> result = failedNotificationService.getFailedNotifications(50);

        assertEquals(2, result.size());
        assertEquals("user1", result.get(0).getRecipientUserId());
        assertEquals("user2", result.get(1).getRecipientUserId());
    }

    @Test
    void getFailedNotifications_withLimit_shouldRespectLimit() throws Exception {
        List<String> jsonList = List.of("json1", "json2", "json3");

        when(listOperations.range("notify:failed:list", 0, 1)).thenReturn(jsonList.subList(0, 2));

        FailedNotification failed1 = FailedNotification.builder().build();
        FailedNotification failed2 = FailedNotification.builder().build();

        when(objectMapper.readValue("json1", FailedNotification.class)).thenReturn(failed1);
        when(objectMapper.readValue("json2", FailedNotification.class)).thenReturn(failed2);

        List<FailedNotification> result = failedNotificationService.getFailedNotifications(2);

        assertEquals(2, result.size());
        verify(listOperations, times(1)).range("notify:failed:list", 0, 1);
    }

    @Test
    void getFailedNotifications_withEmptyList_shouldReturnEmptyList() {
        when(listOperations.range("notify:failed:list", 0, 49)).thenReturn(null);

        List<FailedNotification> result = failedNotificationService.getFailedNotifications(50);

        assertTrue(result.isEmpty());
    }

    @Test
    void clearFailedNotifications_shouldDeleteKey() {
        failedNotificationService.clearFailedNotifications();

        verify(redisTemplate, times(1)).delete("notify:failed:list");
    }

    @Test
    void recordFailed_withException_shouldLogError() throws Exception {
        NotificationMessage message = NotificationMessage.builder()
                .notificationId(UUID.randomUUID())
                .recipientUserId("user123")
                .channel(NotificationChannel.EMAIL)
                .priority(Priority.HIGH)
                .subject("Test Subject")
                .body("Test Body")
                .retryCount(0)
                .reprocessed(false)
                .build();

        when(objectMapper.writeValueAsString(any(FailedNotification.class)))
                .thenThrow(new RuntimeException("Serialization error"));

        assertDoesNotThrow(() -> failedNotificationService.recordFailed(message, "Test error"));

        verify(listOperations, never()).leftPush(anyString(), anyString());
    }

    @Test
    void getFailedNotifications_withException_shouldLogErrorAndReturnEmpty() throws Exception {
        when(listOperations.range("notify:failed:list", 0, 49))
                .thenThrow(new RuntimeException("Redis error"));

        List<FailedNotification> result = failedNotificationService.getFailedNotifications(50);

        assertTrue(result.isEmpty());
    }
}
