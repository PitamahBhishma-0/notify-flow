package com.notifyflow.gateway.config;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class ServiceHealthIndicators {

    @Bean("notification-service")
    public HealthIndicator notificationServiceHealthIndicator(WebClient.Builder webClientBuilder) {
        return new RemoteServiceHealthIndicator(webClientBuilder, "http://localhost:8081/actuator/health");
    }

    @Bean("user-service")
    public HealthIndicator userServiceHealthIndicator(WebClient.Builder webClientBuilder) {
        return new RemoteServiceHealthIndicator(webClientBuilder, "http://localhost:8082/actuator/health");
    }
    @Bean("delivery-service")
    public HealthIndicator deliveryService(WebClient.Builder webClientBuilder) {
        return new RemoteServiceHealthIndicator(webClientBuilder, "http://localhost:8083/actuator/health");
    }
}

// Helper class to ping the other services
class RemoteServiceHealthIndicator implements HealthIndicator {
    private final WebClient webClient;
    private final String url;

    public RemoteServiceHealthIndicator(WebClient.Builder builder, String url) {
        this.webClient = builder.build();
        this.url = url;
    }

    @Override
    public Health health() {
        try {
            String status = webClient.get().uri(url).retrieve()
                    .bodyToMono(JsonNode.class)
                    .map(node -> node.get("status").asText())
                    .block();
            return "UP".equals(status) ? Health.up().build() : Health.down().build();
        } catch (Exception e) {
            return Health.down().withDetail("error", "Service unreachable").build();
        }
    }
}