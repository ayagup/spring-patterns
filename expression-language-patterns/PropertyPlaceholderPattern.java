package com.spring.patterns.expressionlanguage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Property Placeholder Pattern
 * 
 * Demonstrates comprehensive usage of property placeholders in Spring:
 * - ${} syntax for property resolution
 * - Default values
 * - System properties
 * - Environment variables
 * - application.properties/yml
 * - @ConfigurationProperties
 * - PropertySourcesPlaceholderConfigurer
 * - Type conversion
 * - Nested properties
 */

// ===================== Configuration Properties =====================

@ConfigurationProperties(prefix = "app")
record AppProperties(
    String name,
    String version,
    String description,
    ServerConfig server,
    DatabaseConfig database,
    SecurityConfig security,
    FeatureFlags features
) {
    record ServerConfig(
        int port,
        String contextPath,
        String host,
        int connectionTimeout
    ) {}
    
    record DatabaseConfig(
        String url,
        String username,
        String password,
        String driverClassName,
        PoolConfig pool
    ) {
        record PoolConfig(
            int minSize,
            int maxSize,
            int timeout
        ) {}
    }
    
    record SecurityConfig(
        boolean enabled,
        String secretKey,
        int tokenExpiration,
        List<String> allowedOrigins
    ) {}
    
    record FeatureFlags(
        boolean newUiEnabled,
        boolean betaFeaturesEnabled,
        boolean debugMode
    ) {}
}

// ===================== Property Source Configuration =====================

@Configuration
@PropertySource("classpath:application.properties")
@EnableConfigurationProperties(AppProperties.class)
class PropertyPlaceholderConfiguration {
    
    /**
     * PropertySourcesPlaceholderConfigurer bean
     * Required for ${} placeholder resolution in @Value
     */
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyConfigurer() {
        PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
        // Set to not fail if property file is missing
        configurer.setIgnoreResourceNotFound(true);
        // Set to not fail if placeholder cannot be resolved
        configurer.setIgnoreUnresolvablePlaceholders(false);
        return configurer;
    }
}

// ===================== Basic Property Placeholder Usage =====================

@Component
class BasicPropertyComponent {
    
    // Simple property placeholder
    @Value("${app.name:My Application}")
    private String appName;
    
    @Value("${app.version:1.0.0}")
    private String appVersion;
    
    @Value("${app.description:Default Description}")
    private String appDescription;
    
    // Numeric properties with defaults
    @Value("${server.port:8080}")
    private int serverPort;
    
    @Value("${server.connection-timeout:30000}")
    private int connectionTimeout;
    
    // Boolean properties
    @Value("${security.enabled:true}")
    private boolean securityEnabled;
    
    @Value("${features.debug-mode:false}")
    private boolean debugMode;
    
    public Map<String, Object> getProperties() {
        return Map.of(
            "appName", appName,
            "appVersion", appVersion,
            "appDescription", appDescription,
            "serverPort", serverPort,
            "connectionTimeout", connectionTimeout,
            "securityEnabled", securityEnabled,
            "debugMode", debugMode
        );
    }
}

// ===================== Advanced Property Placeholder Usage =====================

@Component
class AdvancedPropertyComponent {
    
    // List/Array properties
    @Value("${security.allowed-origins:http://localhost:3000,http://localhost:4200}")
    private List<String> allowedOrigins;
    
    @Value("${app.supported-languages:en,es,fr,de}")
    private String[] supportedLanguages;
    
    // Map properties (using SpEL)
    @Value("#{${app.custom-config:{}}}")
    private Map<String, String> customConfig;
    
    // System properties
    @Value("${user.name:unknown}")
    private String systemUser;
    
    @Value("${java.version:unknown}")
    private String javaVersion;
    
    @Value("${os.name:unknown}")
    private String osName;
    
    // Environment variables
    @Value("${PATH:}")
    private String pathEnv;
    
    @Value("${HOME:}")
    private String homeEnv;
    
    // Nested property with Elvis operator
    @Value("${database.url:jdbc:h2:mem:testdb}")
    private String databaseUrl;
    
    @Value("${database.pool.max-size:10}")
    private int maxPoolSize;
    
    public Map<String, Object> getAdvancedProperties() {
        return Map.of(
            "allowedOrigins", allowedOrigins,
            "supportedLanguages", supportedLanguages,
            "customConfig", customConfig,
            "systemUser", systemUser,
            "javaVersion", javaVersion,
            "osName", osName,
            "databaseUrl", databaseUrl,
            "maxPoolSize", maxPoolSize
        );
    }
}

// ===================== Property Placeholder with SpEL =====================

@Component
class SpelPropertyComponent {
    
    // Combining placeholder and SpEL
    @Value("#{${app.server.port:8080} + 1000}")
    private int managementPort;
    
    // Conditional property based on another property
    @Value("#{${security.enabled:true} ? 'SECURE' : 'INSECURE'}")
    private String securityMode;
    
    // Mathematical expression with properties
    @Value("#{${database.pool.min-size:5} * 2}")
    private int calculatedPoolSize;
    
    // String concatenation
    @Value("${app.name:MyApp} v${app.version:1.0}")
    private String appFullName;
    
    // Complex SpEL expression with properties
    @Value("#{T(java.lang.Math).max(${server.port:8080}, 8000)}")
    private int maxPort;
    
    // List size check
    @Value("#{${security.allowed-origins:http://localhost:3000}.split(',').length}")
    private int originsCount;
    
    public Map<String, Object> getSpelProperties() {
        return Map.of(
            "managementPort", managementPort,
            "securityMode", securityMode,
            "calculatedPoolSize", calculatedPoolSize,
            "appFullName", appFullName,
            "maxPort", maxPort,
            "originsCount", originsCount
        );
    }
}

// ===================== Environment-based Property Access =====================

@Service
class PropertyService {
    
    private final Environment environment;
    private final AppProperties appProperties;
    
    public PropertyService(Environment environment, AppProperties appProperties) {
        this.environment = environment;
        this.appProperties = appProperties;
    }
    
    /**
     * Programmatic property access using Environment
     */
    public Map<String, Object> getEnvironmentProperties() {
        return Map.of(
            "appName", environment.getProperty("app.name", "Default App"),
            "serverPort", environment.getProperty("server.port", Integer.class, 8080),
            "securityEnabled", environment.getProperty("security.enabled", Boolean.class, true),
            "activeProfiles", environment.getActiveProfiles(),
            "defaultProfiles", environment.getDefaultProfiles()
        );
    }
    
    /**
     * Check if property exists
     */
    public Map<String, Boolean> checkProperties() {
        return Map.of(
            "hasAppName", environment.containsProperty("app.name"),
            "hasDatabaseUrl", environment.containsProperty("database.url"),
            "hasCustomProperty", environment.containsProperty("custom.property")
        );
    }
    
    /**
     * Get required property (throws exception if missing)
     */
    public String getRequiredProperty(String key) {
        try {
            return environment.getRequiredProperty(key);
        } catch (IllegalStateException e) {
            return "Property not found: " + key;
        }
    }
    
    /**
     * Access Configuration Properties
     */
    public Map<String, Object> getConfigurationProperties() {
        Map<String, Object> props = new HashMap<>();
        
        props.put("appName", appProperties.name());
        props.put("appVersion", appProperties.version());
        props.put("serverPort", appProperties.server().port());
        props.put("serverHost", appProperties.server().host());
        props.put("databaseUrl", appProperties.database().url());
        props.put("poolMaxSize", appProperties.database().pool().maxSize());
        props.put("securityEnabled", appProperties.security().enabled());
        props.put("allowedOrigins", appProperties.security().allowedOrigins());
        props.put("newUiEnabled", appProperties.features().newUiEnabled());
        
        return props;
    }
}

// ===================== Constructor Injection with Placeholders =====================

@Component
class ConstructorPropertyComponent {
    
    private final String serviceName;
    private final int retryAttempts;
    private final long timeout;
    
    public ConstructorPropertyComponent(
        @Value("${service.name:DefaultService}") String serviceName,
        @Value("${service.retry-attempts:3}") int retryAttempts,
        @Value("${service.timeout:5000}") long timeout
    ) {
        this.serviceName = serviceName;
        this.retryAttempts = retryAttempts;
        this.timeout = timeout;
    }
    
    public Map<String, Object> getServiceConfig() {
        return Map.of(
            "serviceName", serviceName,
            "retryAttempts", retryAttempts,
            "timeout", timeout
        );
    }
}

// ===================== Method Parameter Injection =====================

@Configuration
class MethodPropertyConfiguration {
    
    @Bean
    public Map<String, Object> databaseConfig(
        @Value("${database.url:jdbc:h2:mem:testdb}") String url,
        @Value("${database.username:sa}") String username,
        @Value("${database.driver:org.h2.Driver}") String driver,
        @Value("${database.pool.max-size:10}") int maxPoolSize
    ) {
        return Map.of(
            "url", url,
            "username", username,
            "driver", driver,
            "maxPoolSize", maxPoolSize
        );
    }
    
    @Bean
    public Map<String, Object> cacheConfig(
        @Value("${cache.type:caffeine}") String cacheType,
        @Value("${cache.ttl:3600}") long ttl,
        @Value("${cache.max-size:1000}") int maxSize
    ) {
        return Map.of(
            "cacheType", cacheType,
            "ttl", ttl,
            "maxSize", maxSize
        );
    }
}

// ===================== Profile-specific Properties =====================

@Component
class ProfilePropertyComponent {
    
    // Will differ based on active profile
    @Value("${app.environment:development}")
    private String environment;
    
    @Value("${logging.level:INFO}")
    private String loggingLevel;
    
    @Value("${database.show-sql:false}")
    private boolean showSql;
    
    @Value("${cache.enabled:true}")
    private boolean cacheEnabled;
    
    public Map<String, Object> getProfileProperties() {
        return Map.of(
            "environment", environment,
            "loggingLevel", loggingLevel,
            "showSql", showSql,
            "cacheEnabled", cacheEnabled
        );
    }
}

// ===================== REST Controller =====================

@RestController
@RequestMapping("/api/properties")
class PropertyController {
    
    private final BasicPropertyComponent basicProps;
    private final AdvancedPropertyComponent advancedProps;
    private final SpelPropertyComponent spelProps;
    private final PropertyService propertyService;
    private final ConstructorPropertyComponent constructorProps;
    private final ProfilePropertyComponent profileProps;
    
    public PropertyController(
        BasicPropertyComponent basicProps,
        AdvancedPropertyComponent advancedProps,
        SpelPropertyComponent spelProps,
        PropertyService propertyService,
        ConstructorPropertyComponent constructorProps,
        ProfilePropertyComponent profileProps
    ) {
        this.basicProps = basicProps;
        this.advancedProps = advancedProps;
        this.spelProps = spelProps;
        this.propertyService = propertyService;
        this.constructorProps = constructorProps;
        this.profileProps = profileProps;
    }
    
    @GetMapping("/basic")
    public Map<String, Object> getBasicProperties() {
        return basicProps.getProperties();
    }
    
    @GetMapping("/advanced")
    public Map<String, Object> getAdvancedProperties() {
        return advancedProps.getAdvancedProperties();
    }
    
    @GetMapping("/spel")
    public Map<String, Object> getSpelProperties() {
        return spelProps.getSpelProperties();
    }
    
    @GetMapping("/environment")
    public Map<String, Object> getEnvironmentProperties() {
        return propertyService.getEnvironmentProperties();
    }
    
    @GetMapping("/check")
    public Map<String, Boolean> checkProperties() {
        return propertyService.checkProperties();
    }
    
    @GetMapping("/config")
    public Map<String, Object> getConfigurationProperties() {
        return propertyService.getConfigurationProperties();
    }
    
    @GetMapping("/constructor")
    public Map<String, Object> getConstructorProperties() {
        return constructorProps.getServiceConfig();
    }
    
    @GetMapping("/profile")
    public Map<String, Object> getProfileProperties() {
        return profileProps.getProfileProperties();
    }
    
    @GetMapping("/all")
    public Map<String, Object> getAllProperties() {
        Map<String, Object> all = new HashMap<>();
        all.put("basic", basicProps.getProperties());
        all.put("advanced", advancedProps.getAdvancedProperties());
        all.put("spel", spelProps.getSpelProperties());
        all.put("environment", propertyService.getEnvironmentProperties());
        all.put("config", propertyService.getConfigurationProperties());
        all.put("constructor", constructorProps.getServiceConfig());
        all.put("profile", profileProps.getProfileProperties());
        return all;
    }
}

/**
 * Key Concepts Demonstrated:
 * 
 * 1. Property Placeholder Syntax:
 *    - ${property.name} - Basic placeholder
 *    - ${property.name:defaultValue} - With default value
 *    - ${property.nested.value} - Nested properties
 * 
 * 2. Property Sources:
 *    - application.properties
 *    - application.yml
 *    - System properties
 *    - Environment variables
 *    - Command line arguments
 *    - @PropertySource files
 * 
 * 3. @Value Annotation:
 *    - Field injection
 *    - Constructor injection
 *    - Method parameter injection
 *    - Type conversion (String, int, boolean, List, etc.)
 * 
 * 4. Default Values:
 *    - Colon syntax: ${prop:default}
 *    - Empty string default: ${prop:}
 *    - Numeric defaults: ${port:8080}
 *    - Boolean defaults: ${enabled:true}
 * 
 * 5. Type Conversion:
 *    - String to int/Integer
 *    - String to boolean/Boolean
 *    - String to long/Long
 *    - String to List/Array
 *    - String to Map (with SpEL)
 * 
 * 6. @ConfigurationProperties:
 *    - Type-safe configuration
 *    - Nested properties
 *    - Validation support
 *    - Relaxed binding
 *    - Metadata support
 * 
 * 7. Environment API:
 *    - getProperty() with defaults
 *    - getRequiredProperty()
 *    - containsProperty()
 *    - Active profiles
 *    - Property resolution
 * 
 * 8. PropertySourcesPlaceholderConfigurer:
 *    - Enable ${} resolution
 *    - Custom property sources
 *    - Placeholder prefix/suffix
 *    - Ignore missing resources
 * 
 * 9. Combining with SpEL:
 *    - #{${property} + value}
 *    - Conditional: #{${prop} ? 'A' : 'B'}
 *    - Type operations: T(Class).method()
 * 
 * 10. Profile-specific Properties:
 *     - application-{profile}.properties
 *     - @Profile annotation
 *     - Active profile detection
 * 
 * application.properties Example:
 * 
 * # Application
 * app.name=My Spring Application
 * app.version=2.0.0
 * app.description=A sample application
 * 
 * # Server
 * app.server.port=8080
 * app.server.context-path=/api
 * app.server.host=localhost
 * app.server.connection-timeout=30000
 * 
 * # Database
 * app.database.url=jdbc:postgresql://localhost:5432/mydb
 * app.database.username=user
 * app.database.password=pass
 * app.database.driver-class-name=org.postgresql.Driver
 * app.database.pool.min-size=5
 * app.database.pool.max-size=20
 * app.database.pool.timeout=30000
 * 
 * # Security
 * app.security.enabled=true
 * app.security.secret-key=mySecretKey123
 * app.security.token-expiration=3600
 * app.security.allowed-origins=http://localhost:3000,http://localhost:4200
 * 
 * # Features
 * app.features.new-ui-enabled=true
 * app.features.beta-features-enabled=false
 * app.features.debug-mode=false
 * 
 * Testing:
 * 
 * curl http://localhost:8080/api/properties/basic
 * curl http://localhost:8080/api/properties/advanced
 * curl http://localhost:8080/api/properties/spel
 * curl http://localhost:8080/api/properties/config
 * curl http://localhost:8080/api/properties/all
 * 
 * Best Practices:
 * 
 * 1. Always provide default values
 * 2. Use @ConfigurationProperties for complex configs
 * 3. Externalize all environment-specific values
 * 4. Use profiles for environment separation
 * 5. Validate configuration properties
 * 6. Use meaningful property names
 * 7. Document all properties
 * 8. Use relaxed binding (kebab-case, camelCase)
 */
