package com.example.demo.patterns.health;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * MongoDB Health Indicator Pattern - MongoDB Database Health
 * 
 * Purpose:
 * - Monitor MongoDB connectivity
 * - Check database status
 * - Track MongoDB metrics
 * - Detect database issues
 * 
 * Spring Boot provides built-in MongoHealthIndicator
 * 
 * Configuration (application.yml):
 * spring:
 *   data:
 *     mongodb:
 *       uri: mongodb://localhost:27017/mydb
 * management:
 *   health:
 *     mongo:
 *       enabled: true
 */
@SpringBootApplication
public class MongoDBHealthIndicatorPattern {

    public static void main(String[] args) {
        SpringApplication.run(MongoDBHealthIndicatorPattern.class, args);
    }

    @Component("customMongoDB")
    public static class CustomMongoDBHealthIndicator implements HealthIndicator {
        
        @Override
        public Health health() {
            try {
                boolean connected = checkMongoDBConnection();
                
                if (!connected) {
                    return Health.down()
                        .withDetail("mongodb", "disconnected")
                        .build();
                }
                
                Map<String, Object> details = new HashMap<>();
                details.put("version", "5.0.0");
                details.put("database", "mydb");
                details.put("collections", 10);
                details.put("documents", 50000);
                details.put("replicaSet", "rs0");
                
                return Health.up().withDetails(details).build();
            } catch (Exception e) {
                return Health.down().withException(e).build();
            }
        }
        
        private boolean checkMongoDBConnection() {
            return Math.random() > 0.05;
        }
    }
}
