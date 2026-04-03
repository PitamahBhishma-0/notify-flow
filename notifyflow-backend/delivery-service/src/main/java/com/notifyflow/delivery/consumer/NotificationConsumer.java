package com.notifyflow.delivery.consumer;

import com.notiflyflow.notifycommon.dto.NotificationMessage;
import com.notifyflow.delivery.handler.EmailHandler;
import com.notifyflow.delivery.handler.InAppHandler;
import com.notifyflow.delivery.handler.SmsHandler;
import com.notifyflow.delivery.service.DeduplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final DeduplicationService deduplicationService;
    private final EmailHandler emailHandler;
    private final SmsHandler smsHandler;
    private final InAppHandler inAppHandler;
    private final JmsTemplate jmsTemplate;

    @JmsListener(destination = "notify.queue.high", concurrency = "5-10")
    public void consumeHigh(NotificationMessage message) {
        process(message);
    }

    @JmsListener(destination = "notify.queue.medium", concurrency = "3-5")
    public void consumeMedium(NotificationMessage message) {
        process(message);
    }

    @JmsListener(destination = "notify.queue.low", concurrency = "1-3")
    public void consumeLow(NotificationMessage message) {
        process(message);
    }

    private void process(NotificationMessage message) {
        if (message.getRetryCount() == 0) {
            if (deduplicationService.isDuplicate(message.getNotificationId())) return;
            if (deduplicationService.isRateLimited(
                    message.getRecipientUserId(),
                    String.valueOf(message.getChannel()))) return;
        }
        try {
            switch (message.getChannel().name()) {
                case "EMAIL"  -> emailHandler.send(message, message.getRecipientUserId());
                case "SMS"    -> smsHandler.send(message, message.getRecipientUserId());
                case "IN_APP" -> inAppHandler.send(message);
                default       -> log.warn("Unknown channel: {}", message.getChannel());
            }
            log.info("Notification [{}] delivered via [{}]", message.getNotificationId(), message.getChannel());
        } catch (Exception e) {
            log.error("Delivery failed for [{}] (attempt {}): {}",
                    message.getNotificationId(), message.getRetryCount() + 1, e.getMessage());
            jmsTemplate.convertAndSend("notify.queue.retry", message);
        }
    }


    @JmsListener(destination = "notify.queue.dlq")
    public void reprocess(NotificationMessage message) {
        if (message.isReprocessed()) {
            log.warn("Message [{}] is already marked reprocessed. Skipping DLQ re-attempt.",
                    message.getNotificationId());
            return;
        }
        process(message);
    }
}
