package com.notifyflow.delivery.handler;

import com.notiflyflow.notifycommon.dto.NotificationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailHandler {

    private final JavaMailSender mailSender;

    public void send(NotificationMessage message, String recipientEmail) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(recipientEmail);
        mail.setSubject(message.getSubject());
        mail.setText(message.getBody());
        mailSender.send(mail);
        log.info("Email sent for notification [{}]", message.getNotificationId());
    }
}
