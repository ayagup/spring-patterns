package com.example.demo.patterns.health;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Cassandra Health Indicator Pattern - Cassandra Database Health
 * 
 * Purpose:
 * - Monitor Cassandra connectivity
 * - Check cluster status
 * - Track Cassandra metrics
 * - Detect database issues
 * 
 * Spring Boot provides built-in CassandraHealthIndicator
 * 
 * Configuration (application.yml):
 * spring:
 *   data:
 *     cassandra:
 *       contact-points: localhost
 *       port: 9042
 * management:
 *   health:
 *     cassandra:
 *       enabled: true
 */
@SpringBootApplication
public class CassandraHealthIndicatorPattern {

    public static void main(String[] args) {
        SpringApplication.run(CassandraHealthIndicatorPattern.class, args);
    }

    @Component("customCassandra")
    public static class CustomCassandraHealthIndicator implements HealthIndicator {
        
        @Override
        public Health health() {
            try {
                boolean connected = checkCassandraConnection();
                
                if (!connected) {
                    return Health.down()
                        .withDetail("cassandra", "disconnected")
                        .build();
                }
                
                Map<String, Object> details = new HashMap<>();
                details.put("version", "4.0.0");
                details.put("cluster", "TestCluster");
                details.put("nodes", 3);
                details.put("replicationFactor", 3);
                details.put("keyspaces", 5);
                
                return Health.up().withDetails(details).build();
            } catch (Exception e) {
                return Health.down().withException(e).build();
            }
        }
        
        private boolean checkCassandraConnection() {
            return Math.random() > 0.05;
        }
    }
}
