package com.example.demo.patterns.health;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

/**
 * Database Health Indicator Pattern - Database Connection Health
 * 
 * Purpose:
 * - Monitor database connectivity
 * - Check connection pool status
 * - Validate query performance
 * - Track database metrics
 * - Detect database issues
 * 
 * Spring Boot provides built-in database health indicators:
 * - DataSourceHealthIndicator (JDBC)
 * - MongoHealthIndicator
 * - RedisHealthIndicator
 * - CassandraHealthIndicator
 * - Neo4jHealthIndicator
 * 
 * Configuration (application.yml):
 * management:
 *   health:
 *     db:
 *       enabled: true
 * spring:
 *   datasource:
 *     url: jdbc:postgresql://localhost:5432/mydb
 *     username: user
 *     password: pass
 * 
 * Dependencies:
 * <dependency>
 *     <groupId>org.springframework.boot</groupId>
 *     <artifactId>spring-boot-starter-actuator</artifactId>
 * </dependency>
 * <dependency>
 *     <groupId>org.springframework.boot</groupId>
 *     <artifactId>spring-boot-starter-data-jpa</artifactId>
 * </dependency>
 */
@SpringBootApplication
public class DatabaseHealthIndicatorPattern {

    public static void main(String[] args) {
        SpringApplication.run(DatabaseHealthIndicatorPattern.class, args);
    }

    @Component("customDatabase")
    public static class CustomDatabaseHealthIndicator implements HealthIndicator {
        
        @Override
        public Health health() {
            try {
                // Simulate database check
                boolean connected = checkDatabaseConnection();
                int activeConnections = getActiveConnections();
                int maxConnections = 50;
                long queryTime = getAverageQueryTime();
                
                if (!connected) {
                    return Health.down()
                        .withDetail("database", "PostgreSQL")
                        .withDetail("error", "Connection failed")
                        .build();
                }
                
                double utilization = (double) activeConnections / maxConnections * 100;
                
                Map<String, Object> details = new HashMap<>();
                details.put("database", "PostgreSQL");
                details.put("status", "connected");
                details.put("activeConnections", activeConnections);
                details.put("maxConnections", maxConnections);
                details.put("utilization", String.format("%.1f%%", utilization));
                details.put("avgQueryTime", queryTime + "ms");
                
                if (utilization > 90 || queryTime > 1000) {
                    return Health.status("DEGRADED").withDetails(details).build();
                }
                
                return Health.up().withDetails(details).build();
            } catch (Exception e) {
                return Health.down().withException(e).build();
            }
        }
        
        private boolean checkDatabaseConnection() {
            return Math.random() > 0.05;
        }
        
        private int getActiveConnections() {
            return (int) (Math.random() * 30);
        }
        
        private long getAverageQueryTime() {
            return (long) (Math.random() * 500);
        }
    }
}
