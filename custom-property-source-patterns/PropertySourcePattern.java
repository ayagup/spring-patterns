package com.example.propertysource;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * Property Source Pattern
 * 
 * Demonstrates the Property Source pattern in Spring for managing application configuration.
 * This pattern allows externalizing configuration from code and provides a flexible way
 * to manage properties from various sources.
 * 
 * Key Concepts:
 * - PropertySource abstraction
 * - Custom property sources
 * - Property source ordering
 * - Environment abstraction
 * - Property resolution
 * 
 * Use Cases:
 * - External configuration management
 * - Multiple configuration sources
 * - Environment-specific configuration
 * - Dynamic property loading
 * - Configuration override mechanisms
 */
@SpringBootApplication
public class PropertySourcePattern {

    public static void main(String[] args) {
        SpringApplication.run(PropertySourcePattern.class, args);
    }
}

/**
 * Custom MapPropertySource implementation
 */
class CustomMapPropertySource extends MapPropertySource {

    public CustomMapPropertySource(String name, Map<String, Object> source) {
        super(name, source);
    }

    @Override
    public Object getProperty(String name) {
        Object value = super.getProperty(name);
        // Can add custom logic here (e.g., decryption, transformation)
        return value;
    }
}

/**
 * Custom PropertySource from database simulation
 */
class DatabasePropertySource extends PropertySource<Map<String, String>> {

    public DatabasePropertySource(String name) {
        super(name, loadPropertiesFromDatabase());
    }

    @Override
    public Object getProperty(String name) {
        return this.source.get(name);
    }

    @Override
    public boolean containsProperty(String name) {
        return this.source.containsKey(name);
    }

    /**
     * Simulate loading properties from database
     */
    private static Map<String, String> loadPropertiesFromDatabase() {
        Map<String, String> properties = new HashMap<>();
        properties.put("db.app.name", "My Application");
        properties.put("db.app.version", "1.0.0");
        properties.put("db.feature.enabled", "true");
        properties.put("db.max.connections", "100");
        return properties;
    }
}

/**
 * Custom PropertySource from external API simulation
 */
class ApiPropertySource extends PropertySource<Map<String, Object>> {

    public ApiPropertySource(String name) {
        super(name, fetchPropertiesFromApi());
    }

    @Override
    public Object getProperty(String name) {
        return this.source.get(name);
    }

    /**
     * Simulate fetching properties from external API
     */
    private static Map<String, Object> fetchPropertiesFromApi() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("api.service.url", "https://api.example.com");
        properties.put("api.timeout", 5000);
        properties.put("api.retry.count", 3);
        return properties;
    }
}

/**
 * Configuration to register custom property sources
 */
@Configuration
class PropertySourceConfig {

    private final ConfigurableEnvironment environment;

    public PropertySourceConfig(ConfigurableEnvironment environment) {
        this.environment = environment;
    }

    @PostConstruct
    public void registerCustomPropertySources() {
        // Create custom map property source
        Map<String, Object> customProperties = new HashMap<>();
        customProperties.put("custom.property.one", "value1");
        customProperties.put("custom.property.two", "value2");
        customProperties.put("custom.property.three", "value3");
        
        CustomMapPropertySource customSource = new CustomMapPropertySource(
                "customPropertySource", customProperties);

        // Add database property source
        DatabasePropertySource databaseSource = new DatabasePropertySource("databasePropertySource");

        // Add API property source
        ApiPropertySource apiSource = new ApiPropertySource("apiPropertySource");

        // Register property sources with environment
        // Sources are checked in order - first registered has highest priority
        environment.getPropertySources().addFirst(customSource);
        environment.getPropertySources().addLast(databaseSource);
        environment.getPropertySources().addLast(apiSource);
    }
}

/**
 * Service demonstrating property source usage
 */
@org.springframework.stereotype.Service
class PropertySourceService {

    private final ConfigurableEnvironment environment;

    public PropertySourceService(ConfigurableEnvironment environment) {
        this.environment = environment;
    }

    /**
     * Get property from any source
     */
    public String getProperty(String key) {
        return environment.getProperty(key);
    }

    /**
     * Get property with default value
     */
    public String getProperty(String key, String defaultValue) {
        return environment.getProperty(key, defaultValue);
    }

    /**
     * Get property with type conversion
     */
    public <T> T getProperty(String key, Class<T> targetType) {
        return environment.getProperty(key, targetType);
    }

    /**
     * Check if property exists
     */
    public boolean containsProperty(String key) {
        return environment.containsProperty(key);
    }

    /**
     * Get all property sources
     */
    public Map<String, String> getAllPropertySources() {
        Map<String, String> sources = new HashMap<>();
        environment.getPropertySources().forEach(propertySource -> {
            sources.put(propertySource.getName(), propertySource.getClass().getSimpleName());
        });
        return sources;
    }

    /**
     * Get properties from specific source
     */
    public Map<String, Object> getPropertiesFromSource(String sourceName) {
        Map<String, Object> properties = new HashMap<>();
        PropertySource<?> source = environment.getPropertySources().get(sourceName);
        
        if (source instanceof MapPropertySource) {
            MapPropertySource mapSource = (MapPropertySource) source;
            for (String propertyName : mapSource.getPropertyNames()) {
                properties.put(propertyName, mapSource.getProperty(propertyName));
            }
        }
        
        return properties;
    }
}

/**
 * Controller to demonstrate property source access
 */
@RestController
@RequestMapping("/api/properties")
class PropertySourceController {

    private final PropertySourceService propertySourceService;
    private final ConfigurableEnvironment environment;

    public PropertySourceController(PropertySourceService propertySourceService,
                                   ConfigurableEnvironment environment) {
        this.propertySourceService = propertySourceService;
        this.environment = environment;
    }

    /**
     * Get single property
     */
    @GetMapping("/get")
    public Map<String, String> getProperty(String key) {
        return Map.of(
                "key", key,
                "value", propertySourceService.getProperty(key, "Not found")
        );
    }

    /**
     * Get all property sources
     */
    @GetMapping("/sources")
    public Map<String, String> getPropertySources() {
        return propertySourceService.getAllPropertySources();
    }

    /**
     * Get custom properties
     */
    @GetMapping("/custom")
    public Map<String, Object> getCustomProperties() {
        return Map.of(
                "property.one", environment.getProperty("custom.property.one", ""),
                "property.two", environment.getProperty("custom.property.two", ""),
                "property.three", environment.getProperty("custom.property.three", "")
        );
    }

    /**
     * Get database properties
     */
    @GetMapping("/database")
    public Map<String, Object> getDatabaseProperties() {
        return Map.of(
                "app.name", environment.getProperty("db.app.name", ""),
                "app.version", environment.getProperty("db.app.version", ""),
                "feature.enabled", environment.getProperty("db.feature.enabled", Boolean.class, false),
                "max.connections", environment.getProperty("db.max.connections", Integer.class, 0)
        );
    }

    /**
     * Get API properties
     */
    @GetMapping("/api")
    public Map<String, Object> getApiProperties() {
        return Map.of(
                "service.url", environment.getProperty("api.service.url", ""),
                "timeout", environment.getProperty("api.timeout", Integer.class, 0),
                "retry.count", environment.getProperty("api.retry.count", Integer.class, 0)
        );
    }

    /**
     * Get properties from specific source
     */
    @GetMapping("/source")
    public Map<String, Object> getPropertiesFromSource(String sourceName) {
        return propertySourceService.getPropertiesFromSource(sourceName);
    }
}

/**
 * Documentation:
 * 
 * PropertySource Abstraction:
 * - Base class for all property sources
 * - Provides name and source object
 * - Abstract getProperty(String name) method
 * - Can be extended for custom sources
 * 
 * Common PropertySource Implementations:
 * - MapPropertySource: Properties from Map
 * - PropertiesPropertySource: Properties from Properties object
 * - ResourcePropertySource: Properties from Resource
 * - SystemEnvironmentPropertySource: System environment variables
 * - CommandLinePropertySource: Command line arguments
 * 
 * Property Source Ordering:
 * - addFirst(): Highest priority
 * - addLast(): Lowest priority
 * - addBefore(String, PropertySource): Add before named source
 * - addAfter(String, PropertySource): Add after named source
 * - remove(String): Remove named source
 * - replace(String, PropertySource): Replace named source
 * 
 * Default Property Source Order (highest to lowest):
 * 1. Command line arguments
 * 2. JNDI attributes
 * 3. Java System properties
 * 4. OS environment variables
 * 5. application.properties/yml
 * 6. @PropertySource annotations
 * 
 * Environment Interface:
 * - getProperty(String key): Get property value
 * - getProperty(String key, Class<T>): Get with type conversion
 * - getProperty(String key, String defaultValue): Get with default
 * - containsProperty(String key): Check existence
 * - getRequiredProperty(String key): Throws if missing
 * - getPropertySources(): Access PropertySources object
 * 
 * Best Practices:
 * - Use meaningful names for custom sources
 * - Document property keys and expected values
 * - Provide default values where appropriate
 * - Use type-safe property access
 * - Handle missing properties gracefully
 * - Consider property source ordering carefully
 * - Cache frequently accessed properties
 * - Validate property values
 * 
 * Custom PropertySource Guidelines:
 * - Extend PropertySource<T>
 * - Implement getProperty(String name)
 * - Optionally override containsProperty(String name)
 * - Thread-safe if accessed concurrently
 * - Consider lazy loading for expensive operations
 * - Handle errors gracefully
 * 
 * Use Cases:
 * - Database-backed configuration
 * - Configuration from external services
 * - Encrypted properties
 * - Multi-tenant configuration
 * - Feature flags
 * - A/B testing configuration
 * - Dynamic configuration updates
 * 
 * Performance Considerations:
 * - Property lookup is sequential through sources
 * - Cache frequently accessed properties
 * - Avoid expensive operations in getProperty()
 * - Use lazy loading for external sources
 * - Consider async loading for slow sources
 */
