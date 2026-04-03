package com.notifyflow.delivery.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeduplicationService {

    private static final String PREFIX = "notify:dedup:";
    private static final Duration TTL   = Duration.ofHours(24);

    private final StringRedisTemplate redisTemplate;

    public boolean isDuplicate(UUID notificationId) {
        String key = PREFIX + notificationId;
        Boolean isNew = redisTemplate.opsForValue().setIfAbsent(key, "1", TTL);
        if (Boolean.FALSE.equals(isNew)) {
            log.warn("Duplicate notification detected [{}] — skipping", notificationId);
            return true;
        }
        return false;
    }

    public boolean isRateLimited(String userId, String channel) {
        String key = "notify:rate:" + userId + ":" + channel;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count == 1) {
            redisTemplate.expire(key, Duration.ofMinutes(1));
        }
        // Allow max 10 notifications per user per channel per minute
        if (count != null && count > 10) {
            log.warn("Rate limit hit for user [{}] on channel [{}]", userId, channel);
            return true;
        }
        return false;
    }
}
