package com.example.demo.patterns.health;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Redis Health Indicator Pattern - Redis Cache Health
 * 
 * Purpose:
 * - Monitor Redis connectivity
 * - Check Redis performance
 * - Track cache metrics
 * - Detect Redis issues
 * 
 * Spring Boot provides built-in RedisHealthIndicator
 * 
 * Configuration (application.yml):
 * spring:
 *   redis:
 *     host: localhost
 *     port: 6379
 * management:
 *   health:
 *     redis:
 *       enabled: true
 */
@SpringBootApplication
public class RedisHealthIndicatorPattern {

    public static void main(String[] args) {
        SpringApplication.run(RedisHealthIndicatorPattern.class, args);
    }

    @Component("customRedis")
    public static class CustomRedisHealthIndicator implements HealthIndicator {
        
        @Override
        public Health health() {
            try {
                boolean connected = checkRedisConnection();
                
                if (!connected) {
                    return Health.down()
                        .withDetail("redis", "disconnected")
                        .build();
                }
                
                Map<String, Object> details = new HashMap<>();
                details.put("version", "6.2.0");
                details.put("mode", "standalone");
                details.put("connectedClients", 10);
                details.put("usedMemory", "512MB");
                details.put("hitRate", "85%");
                
                return Health.up().withDetails(details).build();
            } catch (Exception e) {
                return Health.down().withException(e).build();
            }
        }
        
        private boolean checkRedisConnection() {
            return Math.random() > 0.05;
        }
    }
}
