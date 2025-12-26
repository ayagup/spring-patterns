package com.example.demo.patterns.health;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Disk Space Health Indicator Pattern - Disk Space Monitoring
 * 
 * Purpose:
 * - Monitor disk space availability
 * - Prevent disk full scenarios
 * - Alert on low disk space
 * - Track storage usage
 * 
 * Spring Boot provides built-in DiskSpaceHealthIndicator
 * 
 * Configuration (application.yml):
 * management:
 *   health:
 *     diskspace:
 *       enabled: true
 *       threshold: 10MB
 *       path: C:/
 */
@SpringBootApplication
public class DiskSpaceHealthIndicatorPattern {

    public static void main(String[] args) {
        SpringApplication.run(DiskSpaceHealthIndicatorPattern.class, args);
    }

    @Component("customDiskSpace")
    public static class CustomDiskSpaceHealthIndicator implements HealthIndicator {
        
        private final long threshold = 10_000_000_000L; // 10GB
        
        @Override
        public Health health() {
            try {
                File path = new File(".");
                long totalSpace = path.getTotalSpace();
                long freeSpace = path.getFreeSpace();
                long usableSpace = path.getUsableSpace();
                
                Map<String, Object> details = new HashMap<>();
                details.put("total", formatBytes(totalSpace));
                details.put("free", formatBytes(freeSpace));
                details.put("usable", formatBytes(usableSpace));
                details.put("threshold", formatBytes(threshold));
                
                if (freeSpace < threshold) {
                    return Health.down()
                        .withDetails(details)
                        .withDetail("error", "Disk space below threshold")
                        .build();
                }
                
                return Health.up().withDetails(details).build();
            } catch (Exception e) {
                return Health.down().withException(e).build();
            }
        }
        
        private String formatBytes(long bytes) {
            if (bytes < 1024) return bytes + " B";
            int exp = (int) (Math.log(bytes) / Math.log(1024));
            char pre = "KMGTPE".charAt(exp - 1);
            return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
        }
    }
}
