package com.notifyflow.delivery.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notifyflow.delivery.model.dto.FailedNotification;
import com.notiflyflow.notifycommon.dto.NotificationChannel;
import com.notiflyflow.notifycommon.dto.NotificationMessage;
import com.notiflyflow.notifycommon.dto.Priority;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class FailedNotificationService {

    private static final String FAILED_LIST_KEY = "notify:failed:list";
    private static final Duration FAILED_TTL = Duration.ofDays(7);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public void recordFailed(NotificationMessage message, String errorMessage) {
        try {
            FailedNotification failed = FailedNotification.builder()
                    .notificationId(message.getNotificationId())
                    .recipientUserId(message.getRecipientUserId())
                    .channel(message.getChannel())
                    .priority(message.getPriority())
                    .subject(message.getSubject())
                    .body(message.getBody())
                    .errorMessage(errorMessage)
                    .retryCount(message.getRetryCount())
                    .failedAt(LocalDateTime.now())
                    .reprocessed(message.isReprocessed())
                    .build();

            String json = objectMapper.writeValueAsString(failed);
            redisTemplate.opsForList().leftPush(FAILED_LIST_KEY, json);

            redisTemplate.expire(FAILED_LIST_KEY, FAILED_TTL);

            log.info("Recorded failed notification [{}]: {}", message.getNotificationId(), errorMessage);
        } catch (Exception e) {
            log.error("Failed to record failed notification", e);
        }
    }

    public List<FailedNotification> getFailedNotifications(int limit) {
        List<FailedNotification> failedList = new ArrayList<>();

        try {
            List<String> jsonList = redisTemplate.opsForList().range(FAILED_LIST_KEY, 0, limit - 1);

            if (jsonList != null) {
                for (String json : jsonList) {
                    FailedNotification failed = objectMapper.readValue(json, FailedNotification.class);
                    failedList.add(failed);
                }
            }
        } catch (Exception e) {
            log.error("Failed to retrieve failed notifications", e);
        }

        return failedList;
    }

    public void clearFailedNotifications() {
        try {
            redisTemplate.delete(FAILED_LIST_KEY);
            log.info("Cleared all failed notifications");
        } catch (Exception e) {
            log.error("Failed to clear failed notifications", e);
        }
    }
}
