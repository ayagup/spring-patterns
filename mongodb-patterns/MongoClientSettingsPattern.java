package com.example.mongodb;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Mongo Client Settings Pattern
 * 
 * Demonstrates MongoDB client configuration.
 * 
 * Configuration Options:
 * - Connection string
 * - Connection pool settings
 * - Socket timeouts
 * - Server selection timeout
 * - SSL/TLS configuration
 * - Read preferences
 * - Write concerns
 * - Compression
 * 
 * Use Cases:
 * - Production MongoDB configuration
 * - Performance tuning
 * - Connection management
 * - Security settings
 * 
 * @author Spring Patterns
 * @version 1.0
 */
@Configuration
public class MongoClientSettingsPattern {

    @Bean
    public MongoClient mongoClient() {
        ConnectionString connectionString = new ConnectionString(
            "mongodb://localhost:27017/mydb"
        );
        
        MongoClientSettings settings = MongoClientSettings.builder()
            .applyConnectionString(connectionString)
            .applyToConnectionPoolSettings(builder -> builder
                .maxSize(100)
                .minSize(10)
                .maxWaitTime(2, TimeUnit.SECONDS)
                .maxConnectionLifeTime(30, TimeUnit.MINUTES)
                .maxConnectionIdleTime(10, TimeUnit.MINUTES))
            .applyToSocketSettings(builder -> builder
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS))
            .applyToServerSettings(builder -> builder
                .heartbeatFrequency(10, TimeUnit.SECONDS)
                .minHeartbeatFrequency(500, TimeUnit.MILLISECONDS))
            .applyToClusterSettings(builder -> builder
                .serverSelectionTimeout(5, TimeUnit.SECONDS)
                .localThreshold(15, TimeUnit.MILLISECONDS))
            .build();
        
        return MongoClients.create(settings);
    }
}

@RestController
@RequestMapping("/api/mongo/settings")
class MongoSettingsController {

    @GetMapping("/info")
    public ResponseEntity<SettingsInfo> getInfo() {
        return ResponseEntity.ok(new SettingsInfo(
            "Mongo Client Settings Pattern",
            "MongoDB client configuration",
            "1.0",
            List.of(
                "Connection pool: max=100, min=10",
                "Socket timeout: connect=5s, read=10s",
                "Server selection timeout: 5s",
                "Heartbeat frequency: 10s"
            ),
            List.of("Production config", "Performance tuning", "Connection management", "Security")
        ));
    }

    record SettingsInfo(String name, String description, String version,
                       List<String> settings, List<String> useCases) {}
}
