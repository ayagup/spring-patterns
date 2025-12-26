package com.example.demo.patterns.health;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * RabbitMQ Health Indicator Pattern - Message Queue Health
 * 
 * Purpose:
 * - Monitor RabbitMQ connectivity
 * - Check queue status
 * - Track message metrics
 * - Detect messaging issues
 * 
 * Spring Boot provides built-in RabbitHealthIndicator
 * 
 * Configuration (application.yml):
 * spring:
 *   rabbitmq:
 *     host: localhost
 *     port: 5672
 * management:
 *   health:
 *     rabbit:
 *       enabled: true
 */
@SpringBootApplication
public class RabbitMQHealthIndicatorPattern {

    public static void main(String[] args) {
        SpringApplication.run(RabbitMQHealthIndicatorPattern.class, args);
    }

    @Component("customRabbitMQ")
    public static class CustomRabbitMQHealthIndicator implements HealthIndicator {
        
        @Override
        public Health health() {
            try {
                boolean connected = checkRabbitMQConnection();
                
                if (!connected) {
                    return Health.down()
                        .withDetail("rabbitmq", "disconnected")
                        .build();
                }
                
                Map<String, Object> details = new HashMap<>();
                details.put("version", "3.9.0");
                details.put("status", "running");
                details.put("queues", 5);
                details.put("totalMessages", 150);
                details.put("consumers", 3);
                
                return Health.up().withDetails(details).build();
            } catch (Exception e) {
                return Health.down().withException(e).build();
            }
        }
        
        private boolean checkRabbitMQConnection() {
            return Math.random() > 0.05;
        }
    }
}
