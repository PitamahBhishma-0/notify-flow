package com.notifyflow.delivery.retry;

import com.notiflyflow.notifycommon.dto.NotificationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RetryConsumer {

    private static final int MAX_RETRIES = 0;
    private static final String RETRY_QUEUE = "notify.queue.retry";
    private static final String DLQ         = "notify.queue.dlq";

    private final JmsTemplate jmsTemplate;

    @JmsListener(destination = RETRY_QUEUE)
    public void handleRetry(NotificationMessage message) {
        int attempt = message.getRetryCount() + 1;
        message.setRetryCount(attempt);

        if (attempt > MAX_RETRIES) {
            log.warn("Max retries ({}) exceeded for [{}]. Sending to DLQ.",
                    MAX_RETRIES, message.getNotificationId());
            sendToDLQ(message);
            return;
        }
        try {
            // Log exactly where we are going
            String targetQueue = "notify.queue." + message.getPriority().toString().toLowerCase();
            log.info("Moving [{}] from RETRY to [{}]", message.getNotificationId(), targetQueue);

            jmsTemplate.convertAndSend(targetQueue, message);
            // Explicitly logging completion helps verify the ACK happens
            log.info("Successfully re-routed [{}] to {}", message.getNotificationId(), targetQueue);
        } catch (Exception e) {
            log.error("Critical failure in retry logic", e);
            sendToDLQ(message);
        }
    }

    public void sendToDLQ(NotificationMessage message) {
        if (message.isReprocessed()) {
            log.warn("Message [{}] already sent to DLQ once. Dropping to prevent DLQ loop.",
                    message.getNotificationId());
            return;  // hard stop — do not send again
        }
        message.setReprocessed(true);
        jmsTemplate.convertAndSend(DLQ, message, postProcessor -> {
            postProcessor.setStringProperty("X-REPROCESSED-FROM-DLQ", "true");
            postProcessor.setIntProperty("X-FINAL-RETRY-COUNT", message.getRetryCount());
            return postProcessor;
        });
        log.info("Message [{}] sent to DLQ after {} attempt(s).",
                message.getNotificationId(), message.getRetryCount());
    }
}
