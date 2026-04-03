package com.notifyflow.notification.messaging;

import com.notiflyflow.notifycommon.dto.NotificationMessage;
import com.notiflyflow.notifycommon.dto.Priority;
import com.notifyflow.notification.config.ActiveMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationProducer {

    private final JmsTemplate jmsTemplate;

    public void send(NotificationMessage message) {
        String queue = resolveQueue(message.getPriority());
        jmsTemplate.convertAndSend(queue, message);
        log.info("Dispatched notification [{}] to queue [{}]", message.getNotificationId(), queue);
    }

    private String resolveQueue(Priority priority) {
        return switch (priority) {
            case HIGH   -> ActiveMQConfig.QUEUE_HIGH;
            case MEDIUM -> ActiveMQConfig.QUEUE_MEDIUM;
            case LOW    -> ActiveMQConfig.QUEUE_LOW;
        };
    }
}
