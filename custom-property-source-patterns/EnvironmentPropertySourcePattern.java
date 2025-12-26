package com.example.propertysource;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.StreamSupport;

/**
 * Environment Property Source Pattern
 * 
 * Demonstrates the Environment abstraction for accessing application properties
 * from multiple sources in a unified way. The Environment provides a sophisticated
 * API for property resolution with type conversion and profile support.
 * 
 * Key Concepts:
 * - Environment interface
 * - ConfigurableEnvironment
 * - Property resolution hierarchy
 * - Type conversion
 * - Profile management
 * - PropertyResolver interface
 * 
 * Use Cases:
 * - Unified property access
 * - Environment-specific configuration
 * - Type-safe property retrieval
 * - Profile-based configuration
 * - Property placeholder resolution
 */
@SpringBootApplication
public class EnvironmentPropertySourcePattern {

    public static void main(String[] args) {
        SpringApplication.run(EnvironmentPropertySourcePattern.class, args);
    }
}

/**
 * Service demonstrating Environment usage
 */
@org.springframework.stereotype.Service
class EnvironmentService {

    @Autowired
    private Environment environment;

    @Autowired
    private ConfigurableEnvironment configurableEnvironment;

    /**
     * Get property as String
     */
    public String getStringProperty(String key) {
        return environment.getProperty(key);
    }

    /**
     * Get property with default value
     */
    public String getStringProperty(String key, String defaultValue) {
        return environment.getProperty(key, defaultValue);
    }

    /**
     * Get property with type conversion
     */
    public <T> T getTypedProperty(String key, Class<T> targetType) {
        return environment.getProperty(key, targetType);
    }

    /**
     * Get property with type conversion and default
     */
    public <T> T getTypedProperty(String key, Class<T> targetType, T defaultValue) {
        return environment.getProperty(key, targetType, defaultValue);
    }

    /**
     * Get required property (throws if missing)
     */
    public String getRequiredProperty(String key) throws IllegalStateException {
        return environment.getRequiredProperty(key);
    }

    /**
     * Check if property exists
     */
    public boolean containsProperty(String key) {
        return environment.containsProperty(key);
    }

    /**
     * Resolve placeholders in text
     */
    public String resolvePlaceholders(String text) {
        return environment.resolvePlaceholders(text);
    }

    /**
     * Resolve placeholders (throws if unresolvable)
     */
    public String resolveRequiredPlaceholders(String text) {
        return environment.resolveRequiredPlaceholders(text);
    }

    /**
     * Get active profiles
     */
    public String[] getActiveProfiles() {
        return environment.getActiveProfiles();
    }

    /**
     * Get default profiles
     */
    public String[] getDefaultProfiles() {
        return environment.getDefaultProfiles();
    }

    /**
     * Check if profile is active
     */
    public boolean acceptsProfiles(String... profiles) {
        return environment.acceptsProfiles(profiles);
    }

    /**
     * Get system environment variables
     */
    public Map<String, String> getSystemEnvironment() {
        return environment.getSystemEnvironment();
    }

    /**
     * Get system properties
     */
    public Map<String, Object> getSystemProperties() {
        return environment.getSystemProperties();
    }

    /**
     * Get all property sources
     */
    public Map<String, String> getAllPropertySources() {
        Map<String, String> sources = new HashMap<>();
        configurableEnvironment.getPropertySources().forEach(source -> {
            sources.put(source.getName(), source.getClass().getSimpleName());
        });
        return sources;
    }
}

/**
 * Configuration class demonstrating property injection
 */
@Configuration
class EnvironmentConfig {

    @Autowired
    private Environment environment;

    /**
     * Bean using environment for property access
     */
    @Bean
    public AppConfiguration appConfiguration() {
        AppConfiguration config = new AppConfiguration();
        config.setAppName(environment.getProperty("app.name", "Default App"));
        config.setAppVersion(environment.getProperty("app.version", "1.0"));
        config.setDebugEnabled(environment.getProperty("app.debug", Boolean.class, false));
        config.setMaxConnections(environment.getProperty("app.max.connections", Integer.class, 10));
        config.setTimeout(environment.getProperty("app.timeout", Long.class, 5000L));
        return config;
    }
}

/**
 * Configuration POJO
 */
class AppConfiguration {
    private String appName;
    private String appVersion;
    private Boolean debugEnabled;
    private Integer maxConnections;
    private Long timeout;

    // Getters and setters
    public String getAppName() { return appName; }
    public void setAppName(String appName) { this.appName = appName; }
    public String getAppVersion() { return appVersion; }
    public void setAppVersion(String appVersion) { this.appVersion = appVersion; }
    public Boolean getDebugEnabled() { return debugEnabled; }
    public void setDebugEnabled(Boolean debugEnabled) { this.debugEnabled = debugEnabled; }
    public Integer getMaxConnections() { return maxConnections; }
    public void setMaxConnections(Integer maxConnections) { this.maxConnections = maxConnections; }
    public Long getTimeout() { return timeout; }
    public void setTimeout(Long timeout) { this.timeout = timeout; }
}

/**
 * Component demonstrating @Value with Environment
 */
@org.springframework.stereotype.Component
class PropertyInjectionDemo {

    @Value("${server.port:8080}")
    private Integer serverPort;

    @Value("${spring.application.name:My App}")
    private String applicationName;

    @Value("${feature.enabled:false}")
    private Boolean featureEnabled;

    @Value("${custom.list:item1,item2,item3}")
    private String[] customList;

    @Value("#{systemProperties['user.name']}")
    private String systemUser;

    @Value("#{environment['PATH']}")
    private String pathVariable;

    // Getters
    public Integer getServerPort() { return serverPort; }
    public String getApplicationName() { return applicationName; }
    public Boolean getFeatureEnabled() { return featureEnabled; }
    public String[] getCustomList() { return customList; }
    public String getSystemUser() { return systemUser; }
    public String getPathVariable() { return pathVariable; }
}

/**
 * Controller demonstrating Environment access
 */
@RestController
@RequestMapping("/api/environment")
class EnvironmentController {

    private final EnvironmentService environmentService;
    private final AppConfiguration appConfiguration;
    private final PropertyInjectionDemo propertyDemo;

    public EnvironmentController(EnvironmentService environmentService,
                                AppConfiguration appConfiguration,
                                PropertyInjectionDemo propertyDemo) {
        this.environmentService = environmentService;
        this.appConfiguration = appConfiguration;
        this.propertyDemo = propertyDemo;
    }

    /**
     * Get property by key
     */
    @GetMapping("/property")
    public Map<String, Object> getProperty(String key, String defaultValue) {
        return Map.of(
                "key", key,
                "value", environmentService.getStringProperty(key, defaultValue),
                "exists", environmentService.containsProperty(key)
        );
    }

    /**
     * Get typed property
     */
    @GetMapping("/property/typed")
    public Map<String, Object> getTypedProperty(String key, String type) {
        Object value = switch (type.toLowerCase()) {
            case "int", "integer" -> environmentService.getTypedProperty(key, Integer.class);
            case "long" -> environmentService.getTypedProperty(key, Long.class);
            case "boolean", "bool" -> environmentService.getTypedProperty(key, Boolean.class);
            case "double" -> environmentService.getTypedProperty(key, Double.class);
            default -> environmentService.getStringProperty(key);
        };
        
        return Map.of(
                "key", key,
                "type", type,
                "value", value != null ? value : "null"
        );
    }

    /**
     * Get active profiles
     */
    @GetMapping("/profiles")
    public Map<String, Object> getProfiles() {
        return Map.of(
                "active", environmentService.getActiveProfiles(),
                "default", environmentService.getDefaultProfiles()
        );
    }

    /**
     * Get all property sources
     */
    @GetMapping("/sources")
    public Map<String, String> getPropertySources() {
        return environmentService.getAllPropertySources();
    }

    /**
     * Get application configuration
     */
    @GetMapping("/config")
    public AppConfiguration getAppConfiguration() {
        return appConfiguration;
    }

    /**
     * Get injected properties
     */
    @GetMapping("/injected")
    public Map<String, Object> getInjectedProperties() {
        return Map.of(
                "serverPort", propertyDemo.getServerPort(),
                "applicationName", propertyDemo.getApplicationName(),
                "featureEnabled", propertyDemo.getFeatureEnabled(),
                "customList", propertyDemo.getCustomList(),
                "systemUser", propertyDemo.getSystemUser()
        );
    }

    /**
     * Resolve placeholders
     */
    @GetMapping("/resolve")
    public Map<String, String> resolvePlaceholders(String text) {
        return Map.of(
                "original", text,
                "resolved", environmentService.resolvePlaceholders(text)
        );
    }

    /**
     * Get system environment
     */
    @GetMapping("/system/env")
    public Map<String, String> getSystemEnvironment() {
        return environmentService.getSystemEnvironment();
    }

    /**
     * Get system properties
     */
    @GetMapping("/system/properties")
    public Map<String, Object> getSystemProperties() {
        return environmentService.getSystemProperties();
    }
}

/**
 * Documentation:
 * 
 * Environment Interface:
 * - Central interface for property resolution
 * - Part of org.springframework.core.env package
 * - Extends PropertyResolver
 * - Provides profile support
 * 
 * Key Methods:
 * 
 * Property Access:
 * - getProperty(String key): Returns String or null
 * - getProperty(String key, String defaultValue): With default
 * - getProperty(String key, Class<T> targetType): With type conversion
 * - getRequiredProperty(String key): Throws if missing
 * - containsProperty(String key): Check existence
 * 
 * Placeholder Resolution:
 * - resolvePlaceholders(String text): Resolve ${...} placeholders
 * - resolveRequiredPlaceholders(String text): Throws if unresolvable
 * 
 * Profile Management:
 * - getActiveProfiles(): Get active profiles
 * - getDefaultProfiles(): Get default profiles
 * - acceptsProfiles(String... profiles): Check profile
 * 
 * System Access:
 * - getSystemEnvironment(): OS environment variables
 * - getSystemProperties(): Java system properties
 * 
 * Type Conversion:
 * - Automatic conversion for common types
 * - Boolean: true, false, on, off, yes, no, 1, 0
 * - Numbers: Integer, Long, Double, Float
 * - Collections: Comma-separated to arrays/lists
 * - Custom converters can be registered
 * 
 * Property Source Priority (highest to lowest):
 * 1. ServletConfig init parameters
 * 2. ServletContext init parameters
 * 3. JNDI (java:comp/env/)
 * 4. JVM system properties (-D)
 * 5. JVM system environment
 * 6. Random values (random.*)
 * 7. application-{profile}.properties outside jar
 * 8. application-{profile}.properties inside jar
 * 9. application.properties outside jar
 * 10. application.properties inside jar
 * 11. @PropertySource on @Configuration
 * 12. Default properties (SpringApplication.setDefaultProperties)
 * 
 * Best Practices:
 * - Inject Environment rather than accessing statically
 * - Use type-safe property access with getProperty(key, Class<T>)
 * - Provide sensible defaults
 * - Use @Value for simple property injection
 * - Use @ConfigurationProperties for complex configuration
 * - Use profiles for environment-specific config
 * - Document all custom properties
 * - Validate property values
 * - Use placeholders for composable properties
 * 
 * Placeholder Syntax:
 * - ${property.name}: Simple placeholder
 * - ${property.name:defaultValue}: With default
 * - ${property.one.${property.two}}: Nested placeholders
 * - #{expression}: SpEL expression
 * 
 * Profile Expressions:
 * - @Profile("dev"): Single profile
 * - @Profile("dev | test"): OR condition
 * - @Profile("!prod"): NOT condition
 * - @Profile("dev & cloud"): AND condition
 * 
 * Common Use Cases:
 * - Database connection configuration
 * - Feature toggles
 * - Service endpoints
 * - Timeouts and thresholds
 * - Environment-specific behavior
 * - External service integration
 * - Security configuration
 */
