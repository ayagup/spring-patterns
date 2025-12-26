package com.example.springboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Configuration Properties Pattern
 * 
 * Demonstrates type-safe configuration properties binding with validation,
 * nested properties, and relaxed binding.
 * 
 * Key Concepts:
 * - @ConfigurationProperties
 * - Type-safe binding
 * - Property validation
 * - Nested properties
 * - Relaxed binding
 * 
 * Use Cases:
 * - Application configuration
 * - Type-safe properties
 * - Configuration validation
 * - Complex configuration
 * - Environment-specific config
 */
@SpringBootApplication
@EnableConfigurationProperties({AppProperties.class, DatabaseProperties.class})
public class ConfigurationPropertiesPattern {

    public static void main(String[] args) {
        SpringApplication.run(ConfigurationPropertiesPattern.class, args);
    }
}

/**
 * Application properties with validation
 */
@ConfigurationProperties(prefix = "app")
@Validated
class AppProperties {
    
    @NotBlank
    private String name;
    
    @Min(1)
    @Max(100)
    private int maxConnections = 10;
    
    private Duration timeout = Duration.ofSeconds(30);
    
    private List<String> allowedOrigins;
    
    private final Security security = new Security();
    
    private final Cache cache = new Cache();

    public static class Security {
        private boolean enabled = true;
        private String algorithm = "SHA-256";
        
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getAlgorithm() { return algorithm; }
        public void setAlgorithm(String algorithm) { this.algorithm = algorithm; }
    }

    public static class Cache {
        private int ttl = 3600;
        private int maxSize = 1000;
        
        public int getTtl() { return ttl; }
        public void setTtl(int ttl) { this.ttl = ttl; }
        public int getMaxSize() { return maxSize; }
        public void setMaxSize(int maxSize) { this.maxSize = maxSize; }
    }

    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getMaxConnections() { return maxConnections; }
    public void setMaxConnections(int maxConnections) { this.maxConnections = maxConnections; }
    public Duration getTimeout() { return timeout; }
    public void setTimeout(Duration timeout) { this.timeout = timeout; }
    public List<String> getAllowedOrigins() { return allowedOrigins; }
    public void setAllowedOrigins(List<String> allowedOrigins) { this.allowedOrigins = allowedOrigins; }
    public Security getSecurity() { return security; }
    public Cache getCache() { return cache; }
}

/**
 * Database properties
 */
@ConfigurationProperties(prefix = "database")
class DatabaseProperties {
    
    private String url;
    private String username;
    private String password;
    private final Pool pool = new Pool();

    public static class Pool {
        private int minSize = 5;
        private int maxSize = 20;
        private Duration maxWait = Duration.ofSeconds(30);
        
        public int getMinSize() { return minSize; }
        public void setMinSize(int minSize) { this.minSize = minSize; }
        public int getMaxSize() { return maxSize; }
        public void setMaxSize(int maxSize) { this.maxSize = maxSize; }
        public Duration getMaxWait() { return maxWait; }
        public void setMaxWait(Duration maxWait) { this.maxWait = maxWait; }
    }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Pool getPool() { return pool; }
}

/**
 * Service using configuration properties
 */
@Service
class ConfigPropertiesService {

    private final AppProperties appProperties;
    private final DatabaseProperties databaseProperties;

    public ConfigPropertiesService(AppProperties appProperties, 
                                  DatabaseProperties databaseProperties) {
        this.appProperties = appProperties;
        this.databaseProperties = databaseProperties;
    }

    public Map<String, Object> getAppConfig() {
        return Map.of(
                "name", appProperties.getName(),
                "maxConnections", appProperties.getMaxConnections(),
                "timeout", appProperties.getTimeout().getSeconds() + "s",
                "allowedOrigins", appProperties.getAllowedOrigins() != null ? 
                    appProperties.getAllowedOrigins() : List.of(),
                "security", Map.of(
                        "enabled", appProperties.getSecurity().isEnabled(),
                        "algorithm", appProperties.getSecurity().getAlgorithm()
                ),
                "cache", Map.of(
                        "ttl", appProperties.getCache().getTtl(),
                        "maxSize", appProperties.getCache().getMaxSize()
                )
        );
    }

    public Map<String, Object> getDatabaseConfig() {
        return Map.of(
                "url", databaseProperties.getUrl(),
                "username", databaseProperties.getUsername(),
                "pool", Map.of(
                        "minSize", databaseProperties.getPool().getMinSize(),
                        "maxSize", databaseProperties.getPool().getMaxSize(),
                        "maxWait", databaseProperties.getPool().getMaxWait().getSeconds() + "s"
                )
        );
    }
}

/**
 * Controller exposing configuration
 */
@RestController
class ConfigPropertiesController {

    private final ConfigPropertiesService service;

    public ConfigPropertiesController(ConfigPropertiesService service) {
        this.service = service;
    }

    @GetMapping("/config/app")
    public Map<String, Object> getAppConfig() {
        return service.getAppConfig();
    }

    @GetMapping("/config/database")
    public Map<String, Object> getDatabaseConfig() {
        return service.getDatabaseConfig();
    }
}

/**
 * Documentation:
 * 
 * application.properties:
 * app.name=MyApplication
 * app.max-connections=50
 * app.timeout=60s
 * app.allowed-origins=http://localhost,https://example.com
 * app.security.enabled=true
 * app.security.algorithm=SHA-512
 * app.cache.ttl=7200
 * app.cache.max-size=5000
 * 
 * database.url=jdbc:mysql://localhost:3306/mydb
 * database.username=admin
 * database.password=secret
 * database.pool.min-size=10
 * database.pool.max-size=50
 * database.pool.max-wait=45s
 * 
 * Relaxed Binding:
 * app.max-connections
 * app.maxConnections
 * app.MAX_CONNECTIONS
 * APP_MAX_CONNECTIONS (environment variable)
 * 
 * Data Types Supported:
 * - Primitives and wrappers
 * - String, Duration, Period
 * - Collections (List, Set, Map)
 * - Nested objects
 * - Enums
 * 
 * Validation:
 * @NotNull, @NotBlank, @NotEmpty
 * @Min, @Max, @Size
 * @Pattern, @Email
 * @Valid for nested objects
 * 
 * Best Practices:
 * - Use type-safe properties over @Value
 * - Validate properties
 * - Provide defaults
 * - Use Duration for timeouts
 * - Group related properties
 * - Document properties
 */
