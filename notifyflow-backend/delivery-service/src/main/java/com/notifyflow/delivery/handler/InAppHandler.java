package com.notifyflow.delivery.handler;

import com.notiflyflow.notifycommon.dto.NotificationMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
//import com.notifyflow.notification.model.entity;
@Component
@Slf4j
public class InAppHandler {

    public void send(NotificationMessage message) {
        
        //push to websocket for in app notification
        //save to notification history
//        this.saveToNotification(message);
        log.info("In-app notification dispatched for user [{}], notification [{}]",
                message.getRecipientUserId(), message.getNotificationId());
    }


}
