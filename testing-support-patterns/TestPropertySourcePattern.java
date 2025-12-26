package com.example.testing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test Property Source Pattern
 * =============================
 * 
 * Demonstrates the @TestPropertySource annotation pattern for overriding and
 * defining properties specifically for test execution in Spring Test Context.
 * 
 * Use Cases:
 * ----------
 * 1. Override production properties for testing
 * 2. Define test-specific configuration values
 * 3. Test different property configurations
 * 4. Mock external service URLs for testing
 * 5. Override database connection properties
 * 6. Test property validation logic
 * 7. Configure test data sources
 * 8. Set feature flags for testing
 * 
 * Key Features:
 * -------------
 * - Overrides default property sources
 * - Supports both .properties and inline properties
 * - Higher precedence than @PropertySource
 * - Can specify multiple property files
 * - Supports property placeholder resolution
 * - Inheritable from test class hierarchy
 * - Can be combined with other test annotations
 * - Supports Spring Expression Language (SpEL)
 * 
 * Syntax Options:
 * ---------------
 * 1. Properties file:
 *    @TestPropertySource(locations = "classpath:test.properties")
 * 
 * 2. Inline properties:
 *    @TestPropertySource(properties = {"key=value", "another.key=value"})
 * 
 * 3. Multiple files:
 *    @TestPropertySource(locations = {"classpath:a.properties", "classpath:b.properties"})
 * 
 * 4. Combined:
 *    @TestPropertySource(
 *        locations = "classpath:test.properties",
 *        properties = "override.key=value"
 *    )
 * 
 * 5. Inheritance mode:
 *    @TestPropertySource(inheritLocations = false)
 * 
 * Property Precedence (highest to lowest):
 * ----------------------------------------
 * 1. @TestPropertySource inline properties
 * 2. @TestPropertySource file properties
 * 3. System properties
 * 4. @PropertySource properties
 * 5. application.properties/yml
 * 6. Default properties
 * 
 * Best Practices:
 * ---------------
 * 1. Use inline properties for simple overrides
 * 2. Use files for complex test configurations
 * 3. Keep test properties separate from production
 * 4. Use consistent naming conventions
 * 5. Document why properties are overridden
 * 6. Minimize test-specific property changes
 * 7. Use profiles for environment-specific tests
 * 8. Avoid hardcoding sensitive data
 * 
 * Common Patterns:
 * ----------------
 * 1. Database URL override
 * 2. External API endpoint mocking
 * 3. Feature flag testing
 * 4. Timeout configuration for tests
 * 5. Security disabled for tests
 * 6. Logging level adjustments
 * 7. Cache configuration override
 * 8. Batch size adjustments
 * 
 * @author Spring Patterns
 * @version 1.0
 */

// Configuration class for tests
@org.springframework.context.annotation.Configuration
class TestPropertyConfig {
    
    @Value("${app.name}")
    private String appName;
    
    @Value("${app.version}")
    private String appVersion;
    
    @Value("${db.url}")
    private String dbUrl;
    
    @Value("${feature.enabled:false}")
    private boolean featureEnabled;
    
    @Value("${api.timeout:5000}")
    private int apiTimeout;
    
    @org.springframework.context.annotation.Bean
    public AppConfig appConfig() {
        return new AppConfig(appName, appVersion, dbUrl, featureEnabled, apiTimeout);
    }
    
    @org.springframework.context.annotation.Bean
    public DatabaseService databaseService() {
        return new DatabaseService(dbUrl);
    }
    
    @org.springframework.context.annotation.Bean
    public ApiService apiService() {
        return new ApiService(apiTimeout);
    }
}

// Application configuration bean
class AppConfig {
    private final String name;
    private final String version;
    private final String dbUrl;
    private final boolean featureEnabled;
    private final int apiTimeout;
    
    public AppConfig(String name, String version, String dbUrl, 
                    boolean featureEnabled, int apiTimeout) {
        this.name = name;
        this.version = version;
        this.dbUrl = dbUrl;
        this.featureEnabled = featureEnabled;
        this.apiTimeout = apiTimeout;
    }
    
    public String getName() { return name; }
    public String getVersion() { return version; }
    public String getDbUrl() { return dbUrl; }
    public boolean isFeatureEnabled() { return featureEnabled; }
    public int getApiTimeout() { return apiTimeout; }
}

// Database service
class DatabaseService {
    private final String connectionUrl;
    
    public DatabaseService(String connectionUrl) {
        this.connectionUrl = connectionUrl;
    }
    
    public String getConnectionUrl() {
        return connectionUrl;
    }
    
    public boolean isTestDatabase() {
        return connectionUrl.contains("test") || connectionUrl.contains("h2:mem");
    }
}

// API service
class ApiService {
    private final int timeout;
    
    public ApiService(int timeout) {
        this.timeout = timeout;
    }
    
    public int getTimeout() {
        return timeout;
    }
}

/**
 * Example 1: Using Inline Properties
 * Demonstrates simple property override with inline values
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestPropertyConfig.class)
@TestPropertySource(properties = {
    "app.name=Test Application",
    "app.version=1.0.0-TEST",
    "db.url=jdbc:h2:mem:testdb",
    "feature.enabled=true",
    "api.timeout=1000"
})
class InlinePropertiesTest {
    
    @Autowired
    private AppConfig appConfig;
    
    @Value("${app.name}")
    private String appName;
    
    @Test
    void testInlineProperties() {
        assertEquals("Test Application", appConfig.getName());
        assertEquals("1.0.0-TEST", appConfig.getVersion());
        assertEquals("jdbc:h2:mem:testdb", appConfig.getDbUrl());
        assertTrue(appConfig.isFeatureEnabled());
        assertEquals(1000, appConfig.getApiTimeout());
        
        System.out.println("✓ Inline properties applied successfully");
        System.out.println("  App Name: " + appConfig.getName());
        System.out.println("  Version: " + appConfig.getVersion());
        System.out.println("  DB URL: " + appConfig.getDbUrl());
    }
    
    @Test
    void testPropertyInjection() {
        assertEquals("Test Application", appName);
        System.out.println("✓ @Value injection working with test properties");
    }
}

/**
 * Example 2: Database Configuration Override
 * Demonstrates overriding database properties for testing
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestPropertyConfig.class)
@TestPropertySource(properties = {
    "app.name=Database Test App",
    "app.version=1.0.0",
    "db.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
    "feature.enabled=false",
    "api.timeout=5000"
})
class DatabaseConfigurationTest {
    
    @Autowired
    private DatabaseService databaseService;
    
    @Test
    void testDatabaseUrlOverride() {
        String dbUrl = databaseService.getConnectionUrl();
        
        assertTrue(dbUrl.startsWith("jdbc:h2:mem"), "Should use H2 in-memory database");
        assertTrue(databaseService.isTestDatabase(), "Should be detected as test database");
        
        System.out.println("✓ Database URL overridden for testing");
        System.out.println("  Connection URL: " + dbUrl);
    }
}

/**
 * Example 3: API Configuration for Testing
 * Demonstrates configuring external API properties for tests
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestPropertyConfig.class)
@TestPropertySource(properties = {
    "app.name=API Test",
    "app.version=1.0.0",
    "db.url=jdbc:h2:mem:testdb",
    "api.timeout=500"  // Shorter timeout for tests
})
class ApiConfigurationTest {
    
    @Autowired
    private ApiService apiService;
    
    @Test
    void testApiTimeout() {
        assertEquals(500, apiService.getTimeout(), "Should use test timeout value");
        System.out.println("✓ API timeout configured for testing: " + apiService.getTimeout() + "ms");
    }
}

/**
 * Example 4: Feature Flag Testing
 * Demonstrates testing with different feature flag configurations
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestPropertyConfig.class)
@TestPropertySource(properties = {
    "app.name=Feature Test",
    "app.version=1.0.0",
    "db.url=jdbc:h2:mem:testdb",
    "feature.enabled=true"
})
class FeatureEnabledTest {
    
    @Autowired
    private AppConfig appConfig;
    
    @Test
    void testFeatureEnabled() {
        assertTrue(appConfig.isFeatureEnabled(), "Feature should be enabled");
        System.out.println("✓ Testing with feature enabled");
    }
}

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestPropertyConfig.class)
@TestPropertySource(properties = {
    "app.name=Feature Test",
    "app.version=1.0.0",
    "db.url=jdbc:h2:mem:testdb",
    "feature.enabled=false"
})
class FeatureDisabledTest {
    
    @Autowired
    private AppConfig appConfig;
    
    @Test
    void testFeatureDisabled() {
        assertFalse(appConfig.isFeatureEnabled(), "Feature should be disabled");
        System.out.println("✓ Testing with feature disabled");
    }
}

/**
 * Example 5: Environment Property Access
 * Demonstrates accessing test properties through Environment
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestPropertyConfig.class)
@TestPropertySource(properties = {
    "app.name=Environment Test",
    "app.version=2.0.0",
    "db.url=jdbc:h2:mem:envtest",
    "custom.property=custom-value",
    "numeric.property=12345"
})
class EnvironmentPropertiesTest {
    
    @Autowired
    private Environment environment;
    
    @Test
    void testEnvironmentAccess() {
        assertEquals("Environment Test", environment.getProperty("app.name"));
        assertEquals("2.0.0", environment.getProperty("app.version"));
        assertEquals("custom-value", environment.getProperty("custom.property"));
        
        System.out.println("✓ Properties accessible through Environment");
    }
    
    @Test
    void testPropertyWithDefault() {
        String value = environment.getProperty("non.existent", "default-value");
        assertEquals("default-value", value);
        System.out.println("✓ Default value returned for missing property");
    }
    
    @Test
    void testPropertyTypeConversion() {
        Integer numericValue = environment.getProperty("numeric.property", Integer.class);
        assertEquals(12345, numericValue);
        System.out.println("✓ Property type conversion working");
    }
    
    @Test
    void testRequiredProperty() {
        String requiredValue = environment.getRequiredProperty("app.name");
        assertNotNull(requiredValue);
        System.out.println("✓ Required property retrieved: " + requiredValue);
    }
}

/**
 * Example 6: Multiple Property Values
 * Demonstrates complex property configuration scenarios
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestPropertyConfig.class)
@TestPropertySource(properties = {
    "app.name=Multi-Value Test",
    "app.version=1.0.0",
    "db.url=jdbc:postgresql://localhost:5432/testdb",
    "db.username=test_user",
    "db.password=test_password",
    "cache.enabled=true",
    "cache.ttl=3600",
    "security.enabled=false"
})
class MultiplePropertiesTest {
    
    @Value("${db.username}")
    private String dbUsername;
    
    @Value("${db.password}")
    private String dbPassword;
    
    @Value("${cache.enabled}")
    private boolean cacheEnabled;
    
    @Value("${cache.ttl}")
    private int cacheTtl;
    
    @Value("${security.enabled}")
    private boolean securityEnabled;
    
    @Test
    void testAllProperties() {
        assertEquals("test_user", dbUsername);
        assertEquals("test_password", dbPassword);
        assertTrue(cacheEnabled);
        assertEquals(3600, cacheTtl);
        assertFalse(securityEnabled);
        
        System.out.println("✓ All test properties configured correctly");
        System.out.println("  DB User: " + dbUsername);
        System.out.println("  Cache Enabled: " + cacheEnabled);
        System.out.println("  Cache TTL: " + cacheTtl + "s");
        System.out.println("  Security Enabled: " + securityEnabled);
    }
}

/**
 * Example 7: Property Placeholder Resolution
 * Demonstrates property placeholders and expressions
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestPropertyConfig.class)
@TestPropertySource(properties = {
    "app.name=Placeholder Test",
    "app.version=1.0.0",
    "db.url=jdbc:h2:mem:testdb",
    "base.url=http://localhost",
    "server.port=8080",
    "api.endpoint=${base.url}:${server.port}/api"
})
class PropertyPlaceholderTest {
    
    @Value("${api.endpoint}")
    private String apiEndpoint;
    
    @Value("${base.url}")
    private String baseUrl;
    
    @Value("${server.port}")
    private String serverPort;
    
    @Test
    void testPropertyPlaceholders() {
        assertEquals("http://localhost:8080/api", apiEndpoint);
        System.out.println("✓ Property placeholder resolution working");
        System.out.println("  Base URL: " + baseUrl);
        System.out.println("  Server Port: " + serverPort);
        System.out.println("  API Endpoint: " + apiEndpoint);
    }
}

/**
 * Example 8: Testing Property-Driven Behavior
 * Demonstrates testing application behavior based on properties
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestPropertyConfig.class)
@TestPropertySource(properties = {
    "app.name=Behavior Test",
    "app.version=1.0.0",
    "db.url=jdbc:h2:mem:testdb",
    "logging.level=DEBUG",
    "batch.size=100",
    "retry.attempts=3"
})
class PropertyDrivenBehaviorTest {
    
    @Value("${logging.level}")
    private String loggingLevel;
    
    @Value("${batch.size}")
    private int batchSize;
    
    @Value("${retry.attempts}")
    private int retryAttempts;
    
    @Test
    void testLoggingConfiguration() {
        assertEquals("DEBUG", loggingLevel);
        System.out.println("✓ Logging level configured for testing: " + loggingLevel);
    }
    
    @Test
    void testBatchConfiguration() {
        assertEquals(100, batchSize);
        assertTrue(batchSize > 0);
        System.out.println("✓ Batch size configured: " + batchSize);
    }
    
    @Test
    void testRetryConfiguration() {
        assertEquals(3, retryAttempts);
        System.out.println("✓ Retry attempts configured: " + retryAttempts);
    }
}

/**
 * Main class for demonstration
 */
public class TestPropertySourcePattern {
    
    public static void main(String[] args) {
        System.out.println("=== Test Property Source Pattern ===\n");
        System.out.println("This pattern demonstrates:");
        System.out.println("1. Inline property configuration for tests");
        System.out.println("2. Database property override");
        System.out.println("3. API configuration for testing");
        System.out.println("4. Feature flag testing");
        System.out.println("5. Environment property access");
        System.out.println("6. Multiple property configuration");
        System.out.println("7. Property placeholder resolution");
        System.out.println("8. Property-driven behavior testing");
        System.out.println("\nRun the test classes to see the pattern in action.");
        System.out.println("====================================");
    }
}

/**
 * Example test.properties file:
 * 
 * # Application properties
 * app.name=Test Application
 * app.version=1.0.0-TEST
 * 
 * # Database properties
 * db.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
 * db.username=sa
 * db.password=
 * db.driver=org.h2.Driver
 * 
 * # Feature flags
 * feature.enabled=true
 * feature.beta=false
 * 
 * # API configuration
 * api.timeout=1000
 * api.retries=3
 * api.base.url=http://localhost:9999
 * 
 * # Cache configuration
 * cache.enabled=false
 * cache.ttl=300
 * 
 * # Security (disabled for tests)
 * security.enabled=false
 * 
 * # Logging
 * logging.level.root=INFO
 * logging.level.com.example=DEBUG
 */
