package com.notifyflow.delivery.handler;

import com.notiflyflow.notifycommon.dto.NotificationMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SmsHandler {

    public void send(NotificationMessage message, String phoneNumber) {
        // Pluggable: integrate Twilio, AWS SNS, or any provider here
        log.info("SMS dispatched to [{}] for notification [{}]: {}",
                phoneNumber, message.getNotificationId(), message.getSubject());
    }
}
