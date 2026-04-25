package com.notifyflow.delivery.service;

import com.notifyflow.delivery.model.dto.ChannelStats;
import com.notifyflow.delivery.model.dto.DeliveryMetricsResponse;
import com.notifyflow.delivery.model.dto.PriorityStats;
import com.notiflyflow.notifycommon.dto.NotificationChannel;
import com.notiflyflow.notifycommon.dto.Priority;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
@Slf4j
public class MetricsService {

    private static final String METRICS_PREFIX = "notify:metrics:";
    private static final String FAILED_PREFIX = "notify:failed:";
    private static final Duration METRICS_TTL = Duration.ofHours(24);

    private final StringRedisTemplate redisTemplate;

    public void recordDelivery(NotificationChannel channel, Priority priority, long deliveryTimeMs) {
        String channelKey = METRICS_PREFIX + "channel:" + channel + ":delivered";
        String priorityKey = METRICS_PREFIX + "priority:" + priority + ":delivered";
        String totalKey = METRICS_PREFIX + "total:delivered";
        String timeKey = METRICS_PREFIX + "total:time";

        redisTemplate.opsForValue().increment(channelKey);
        redisTemplate.opsForValue().increment(priorityKey);
        redisTemplate.opsForValue().increment(totalKey);

        redisTemplate.opsForValue().increment(timeKey, deliveryTimeMs);

        if (redisTemplate.getExpire(channelKey) == null || redisTemplate.getExpire(channelKey) <= 0) {
            redisTemplate.expire(channelKey, METRICS_TTL);
            redisTemplate.expire(priorityKey, METRICS_TTL);
            redisTemplate.expire(totalKey, METRICS_TTL);
            redisTemplate.expire(timeKey, METRICS_TTL);
        }
    }

    public void recordFailure(NotificationChannel channel, Priority priority, String errorMessage) {
        String channelKey = METRICS_PREFIX + "channel:" + channel + ":failed";
        String priorityKey = METRICS_PREFIX + "priority:" + priority + ":failed";
        String totalKey = METRICS_PREFIX + "total:failed";

        redisTemplate.opsForValue().increment(channelKey);
        redisTemplate.opsForValue().increment(priorityKey);
        redisTemplate.opsForValue().increment(totalKey);

        if (redisTemplate.getExpire(channelKey) == null || redisTemplate.getExpire(channelKey) <= 0) {
            redisTemplate.expire(channelKey, METRICS_TTL);
            redisTemplate.expire(priorityKey, METRICS_TTL);
            redisTemplate.expire(totalKey, METRICS_TTL);
        }
    }

    public void recordSent(NotificationChannel channel, Priority priority) {
        String channelKey = METRICS_PREFIX + "channel:" + channel + ":sent";
        String priorityKey = METRICS_PREFIX + "priority:" + priority + ":sent";
        String totalKey = METRICS_PREFIX + "total:sent";

        redisTemplate.opsForValue().increment(channelKey);
        redisTemplate.opsForValue().increment(priorityKey);
        redisTemplate.opsForValue().increment(totalKey);

        if (redisTemplate.getExpire(channelKey) == null || redisTemplate.getExpire(channelKey) <= 0) {
            redisTemplate.expire(channelKey, METRICS_TTL);
            redisTemplate.expire(priorityKey, METRICS_TTL);
            redisTemplate.expire(totalKey, METRICS_TTL);
        }
    }



    public DeliveryMetricsResponse getMetrics() {
        long totalSent = getLongValue(METRICS_PREFIX + "total:sent");
        long delivered = getLongValue(METRICS_PREFIX + "total:delivered");
        long failed = getLongValue(METRICS_PREFIX + "total:failed");
        long totalTimeMs = getLongValue(METRICS_PREFIX + "total:time");

        long inQueue = getQueueSize();

        double avgDeliveryTime = delivered > 0 ? (double) totalTimeMs / delivered / 1000.0 : 0.0;
        double successRate = totalSent > 0 ? (double) delivered / totalSent * 100.0 : 0.0;

        return DeliveryMetricsResponse.builder()
                .totalSent(totalSent)
                .delivered(delivered)
                .failed(failed)
                .inQueue(inQueue)
                .avgDeliveryTime(avgDeliveryTime)
                .successRate(successRate)
                .build();
    }

    public List<ChannelStats> getChannelStats() {
        List<ChannelStats> stats = new ArrayList<>();

        for (NotificationChannel channel : NotificationChannel.values()) {
            String prefix = METRICS_PREFIX + "channel:" + channel + ":";
            long sent = getLongValue(prefix + "sent");
            long delivered = getLongValue(prefix + "delivered");
            long failed = getLongValue(prefix + "failed");

            long totalTimeMs = getLongValue(prefix + "time");
            double avgTime = delivered > 0 ? (double) totalTimeMs / delivered / 1000.0 : 0.0;

            stats.add(ChannelStats.builder()
                    .channel(channel)
                    .sent(sent)
                    .delivered(delivered)
                    .failed(failed)
                    .avgTime(avgTime)
                    .build());
        }

        return stats;
    }

    public List<PriorityStats> getPriorityStats() {
        List<PriorityStats> stats = new ArrayList<>();

        for (Priority priority : Priority.values()) {
            String prefix = METRICS_PREFIX + "priority:" + priority + ":";
            long sent = getLongValue(prefix + "sent");
            long delivered = getLongValue(prefix + "delivered");
            long failed = getLongValue(prefix + "failed");

            stats.add(PriorityStats.builder()
                    .priority(priority)
                    .sent(sent)
                    .delivered(delivered)
                    .failed(failed)
                    .build());
        }

        return stats;
    }

    private long getLongValue(String key) {
        String value = redisTemplate.opsForValue().get(key);
        return value != null ? Long.parseLong(value) : 0L;
    }

    private long getQueueSize() {
        long high = getLongValue(METRICS_PREFIX + "queue:high");
        long medium = getLongValue(METRICS_PREFIX + "queue:medium");
        long low = getLongValue(METRICS_PREFIX + "queue:low");
        return high + medium + low;
    }

    public void updateQueueSize(Priority priority, int delta) {
        String key = METRICS_PREFIX + "queue:" + priority.toString().toLowerCase();
        redisTemplate.opsForValue().increment(key, delta);
        if (redisTemplate.getExpire(key) == null || redisTemplate.getExpire(key) <= 0) {
            redisTemplate.expire(key, METRICS_TTL);
        }
    }

    public long getQueueSizeByPriority(Priority priority) {
        return getLongValue(METRICS_PREFIX + "queue:" + priority.toString().toLowerCase());
    }

    public long getQueueSizeByChannel(NotificationChannel channel) {
        return getLongValue(METRICS_PREFIX + "queue:channel:" + channel);
    }

    public void updateQueueSizeByChannel(NotificationChannel channel, int delta) {
        String key = METRICS_PREFIX + "queue:channel:" + channel;
        redisTemplate.opsForValue().increment(key, delta);
        if (redisTemplate.getExpire(key) == null || redisTemplate.getExpire(key) <= 0) {
            redisTemplate.expire(key, METRICS_TTL);
        }
    }
}
