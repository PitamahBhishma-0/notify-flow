package com.notifyflow.delivery.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.SimpleType;
import com.notiflyflow.notifycommon.dto.NotificationMessage;
import jakarta.jms.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.RedeliveryPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@Slf4j
public class ActiveMqConfig {
    @Bean
    public ConnectionFactory connectionFactory() {
        ActiveMQConnectionFactory connectionFactory =
                new ActiveMQConnectionFactory("tcp://localhost:61616");

        // Trust only your DTO package
        List<String> trustedPackages = Arrays.asList(
                "com.notiflyflow.notifycommon.dto",
                "java.lang",  // Already trusted by default, but explicit is good
                "java.util"    // Already trusted by default
        );
        connectionFactory.setTrustedPackages(trustedPackages);

        return connectionFactory;
    }

    @Bean
    public RedeliveryPolicy redeliveryPolicy() {
        RedeliveryPolicy policy = new RedeliveryPolicy();
        policy.setInitialRedeliveryDelay(5000); // Wait 5 seconds first
        policy.setBackOffMultiplier(2);         // Double the wait time each failure
        policy.setUseExponentialBackOff(true);
        policy.setMaximumRedeliveries(5);       // Total retries before DLQ
        return policy;
    }

}
