package com.notifyflow.notification.config;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;

@Configuration
public class ActiveMQConfig {

    public static final String QUEUE_HIGH     = "notify.queue.high";
    public static final String QUEUE_MEDIUM   = "notify.queue.medium";
    public static final String QUEUE_LOW      = "notify.queue.low";
    public static final String QUEUE_RETRY    = "notify.queue.retry";
    public static final String QUEUE_DLQ      = "notify.queue.dlq";

    @Value("${spring.activemq.broker-url}")
    private String brokerUrl;

    @Bean
    public ActiveMQConnectionFactory connectionFactory() {
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory();
        factory.setBrokerURL(brokerUrl);
        factory.setTrustAllPackages(true);
        return factory;
    }

    @Bean
    public JmsTemplate jmsTemplate() {
        JmsTemplate template = new JmsTemplate(connectionFactory());
        template.setDeliveryPersistent(true);
        template.setExplicitQosEnabled(true);
        return template;
    }

    @Bean
    public DefaultJmsListenerContainerFactory jmsListenerContainerFactory() {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory());
        factory.setConcurrency("3-10");
        factory.setSessionTransacted(true);
        return factory;
    }
}
