package com.example.demo.patterns.health;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Mail Health Indicator Pattern - Email Service Health
 * 
 * Purpose:
 * - Monitor email server connectivity
 * - Validate SMTP configuration
 * - Check mail queue status
 * - Detect email sending issues
 * 
 * Spring Boot provides built-in MailHealthIndicator
 * 
 * Configuration (application.yml):
 * spring:
 *   mail:
 *     host: smtp.gmail.com
 *     port: 587
 *     username: user@gmail.com
 *     password: password
 * management:
 *   health:
 *     mail:
 *       enabled: true
 */
@SpringBootApplication
public class MailHealthIndicatorPattern {

    public static void main(String[] args) {
        SpringApplication.run(MailHealthIndicatorPattern.class, args);
    }

    @Component("customMail")
    public static class CustomMailHealthIndicator implements HealthIndicator {
        
        @Override
        public Health health() {
            try {
                boolean connected = checkMailServer();
                
                Map<String, Object> details = new HashMap<>();
                details.put("location", "smtp.gmail.com:587");
                details.put("protocol", "SMTP");
                
                if (!connected) {
                    return Health.down()
                        .withDetails(details)
                        .withDetail("error", "Cannot connect to mail server")
                        .build();
                }
                
                details.put("status", "connected");
                return Health.up().withDetails(details).build();
            } catch (Exception e) {
                return Health.down().withException(e).build();
            }
        }
        
        private boolean checkMailServer() {
            return Math.random() > 0.1;
        }
    }
}
