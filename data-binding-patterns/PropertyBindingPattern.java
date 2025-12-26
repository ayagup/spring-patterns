package com.example.databinding;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Email;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;

/**
 * Property Binding Pattern
 * 
 * Demonstrates binding external configuration properties to Java objects.
 * Spring Boot's property binding makes configuration management easy and type-safe.
 * 
 * Features:
 * - Type-safe configuration
 * - Nested properties
 * - Collections and maps
 * - Validation
 * - Relaxed binding
 * - Property conversion
 * 
 * Binding Sources:
 * - application.properties
 * - application.yml
 * - Environment variables
 * - Command-line arguments
 * - System properties
 */
@SpringBootApplication
@EnableConfigurationProperties
public class PropertyBindingPattern {

    public static void main(String[] args) {
        SpringApplication.run(PropertyBindingPattern.class, args);
    }

    /**
     * Simple Property Binding
     */
    @Component
    @ConfigurationProperties(prefix = "app")
    public static class AppProperties {
        private String name;
        private String version;
        private boolean enabled;
        private int maxConnections;

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getVersion() { return version; }
        public void setVersion(String version) { this.version = version; }
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public int getMaxConnections() { return maxConnections; }
        public void setMaxConnections(int maxConnections) { this.maxConnections = maxConnections; }
    }

    /**
     * Nested Property Binding
     */
    @Component
    @ConfigurationProperties(prefix = "database")
    public static class DatabaseProperties {
        private String url;
        private String username;
        private String password;
        private Pool pool;
        private Connection connection;

        public static class Pool {
            private int minSize;
            private int maxSize;
            private long timeout;

            public int getMinSize() { return minSize; }
            public void setMinSize(int minSize) { this.minSize = minSize; }
            public int getMaxSize() { return maxSize; }
            public void setMaxSize(int maxSize) { this.maxSize = maxSize; }
            public long getTimeout() { return timeout; }
            public void setTimeout(long timeout) { this.timeout = timeout; }
        }

        public static class Connection {
            private int timeout;
            private boolean autoCommit;

            public int getTimeout() { return timeout; }
            public void setTimeout(int timeout) { this.timeout = timeout; }
            public boolean isAutoCommit() { return autoCommit; }
            public void setAutoCommit(boolean autoCommit) { this.autoCommit = autoCommit; }
        }

        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public Pool getPool() { return pool; }
        public void setPool(Pool pool) { this.pool = pool; }
        public Connection getConnection() { return connection; }
        public void setConnection(Connection connection) { this.connection = connection; }
    }

    /**
     * Collection Property Binding
     */
    @Component
    @ConfigurationProperties(prefix = "server")
    public static class ServerProperties {
        private List<String> allowedOrigins;
        private Map<String, String> headers;
        private List<Endpoint> endpoints;

        public static class Endpoint {
            private String path;
            private String method;
            private boolean secured;

            public String getPath() { return path; }
            public void setPath(String path) { this.path = path; }
            public String getMethod() { return method; }
            public void setMethod(String method) { this.method = method; }
            public boolean isSecured() { return secured; }
            public void setSecured(boolean secured) { this.secured = secured; }
        }

        public List<String> getAllowedOrigins() { return allowedOrigins; }
        public void setAllowedOrigins(List<String> allowedOrigins) { this.allowedOrigins = allowedOrigins; }
        public Map<String, String> getHeaders() { return headers; }
        public void setHeaders(Map<String, String> headers) { this.headers = headers; }
        public List<Endpoint> getEndpoints() { return endpoints; }
        public void setEndpoints(List<Endpoint> endpoints) { this.endpoints = endpoints; }
    }

    /**
     * Validated Property Binding
     */
    @Component
    @ConfigurationProperties(prefix = "mail")
    @Validated
    public static class MailProperties {
        @NotBlank
        private String host;

        @Min(1)
        @Max(65535)
        private int port;

        @Email
        private String from;

        private boolean ssl;

        public String getHost() { return host; }
        public void setHost(String host) { this.host = host; }
        public int getPort() { return port; }
        public void setPort(int port) { this.port = port; }
        public String getFrom() { return from; }
        public void setFrom(String from) { this.from = from; }
        public boolean isSsl() { return ssl; }
        public void setSsl(boolean ssl) { this.ssl = ssl; }
    }

    /**
     * Constructor Binding (Immutable Properties)
     */
    @ConfigurationProperties(prefix = "security")
    public static class SecurityProperties {
        private final String secret;
        private final long tokenExpiration;
        private final boolean enabled;

        public SecurityProperties(String secret, long tokenExpiration, boolean enabled) {
            this.secret = secret;
            this.tokenExpiration = tokenExpiration;
            this.enabled = enabled;
        }

        public String getSecret() { return secret; }
        public long getTokenExpiration() { return tokenExpiration; }
        public boolean isEnabled() { return enabled; }
    }
}

/*
 * application.properties examples:
 * 
 * # Simple Properties
 * app.name=My Application
 * app.version=1.0.0
 * app.enabled=true
 * app.max-connections=100
 * 
 * # Nested Properties
 * database.url=jdbc:mysql://localhost:3306/mydb
 * database.username=root
 * database.password=secret
 * database.pool.min-size=5
 * database.pool.max-size=20
 * database.pool.timeout=30000
 * database.connection.timeout=5000
 * database.connection.auto-commit=true
 * 
 * # Collection Properties
 * server.allowed-origins[0]=http://localhost:3000
 * server.allowed-origins[1]=http://localhost:4200
 * 
 * server.headers.X-Frame-Options=DENY
 * server.headers.X-Content-Type-Options=nosniff
 * 
 * server.endpoints[0].path=/api/users
 * server.endpoints[0].method=GET
 * server.endpoints[0].secured=true
 * server.endpoints[1].path=/api/public
 * server.endpoints[1].method=GET
 * server.endpoints[1].secured=false
 * 
 * # Validated Properties
 * mail.host=smtp.gmail.com
 * mail.port=587
 * mail.from=noreply@example.com
 * mail.ssl=true
 * 
 * # Constructor Binding
 * security.secret=mySecretKey123
 * security.token-expiration=3600000
 * security.enabled=true
 * 
 * 
 * application.yml examples:
 * 
 * app:
 *   name: My Application
 *   version: 1.0.0
 *   enabled: true
 *   max-connections: 100
 * 
 * database:
 *   url: jdbc:mysql://localhost:3306/mydb
 *   username: root
 *   password: secret
 *   pool:
 *     min-size: 5
 *     max-size: 20
 *     timeout: 30000
 *   connection:
 *     timeout: 5000
 *     auto-commit: true
 * 
 * server:
 *   allowed-origins:
 *     - http://localhost:3000
 *     - http://localhost:4200
 *   headers:
 *     X-Frame-Options: DENY
 *     X-Content-Type-Options: nosniff
 *   endpoints:
 *     - path: /api/users
 *       method: GET
 *       secured: true
 *     - path: /api/public
 *       method: GET
 *       secured: false
 * 
 * mail:
 *   host: smtp.gmail.com
 *   port: 587
 *   from: noreply@example.com
 *   ssl: true
 * 
 * 
 * Relaxed Binding:
 * 
 * The following are all equivalent:
 * - app.maxConnections
 * - app.max-connections
 * - app.max_connections
 * - APP_MAX_CONNECTIONS (environment variable)
 * 
 * 
 * Type Conversion:
 * 
 * Spring Boot automatically converts:
 * - String to primitives (int, long, boolean, etc.)
 * - String to Duration (e.g., "10s", "5m", "1h")
 * - String to DataSize (e.g., "10MB", "1GB")
 * - String to Enum
 * - Comma-separated String to List
 * - String to URL, URI, File
 * 
 * 
 * Using ConfigurationProperties in Code:
 * 
 * @RestController
 * public class MyController {
 *     
 *     private final AppProperties appProperties;
 *     
 *     public MyController(AppProperties appProperties) {
 *         this.appProperties = appProperties;
 *     }
 *     
 *     @GetMapping("/info")
 *     public String getInfo() {
 *         return "App: " + appProperties.getName() + 
 *                " v" + appProperties.getVersion();
 *     }
 * }
 * 
 * 
 * Metadata Generation:
 * 
 * Add dependency for IDE autocomplete:
 * <dependency>
 *     <groupId>org.springframework.boot</groupId>
 *     <artifactId>spring-boot-configuration-processor</artifactId>
 *     <optional>true</optional>
 * </dependency>
 * 
 * This generates META-INF/spring-configuration-metadata.json
 */
