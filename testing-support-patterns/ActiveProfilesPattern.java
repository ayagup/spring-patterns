package com.example.testing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Active Profiles Pattern
 * ========================
 * 
 * Demonstrates the @ActiveProfiles annotation pattern for activating specific
 * Spring profiles during test execution to load profile-specific configurations.
 * 
 * Use Cases:
 * ----------
 * 1. Test with different environment configurations
 * 2. Activate test-specific bean profiles
 * 3. Test profile-conditional beans
 * 4. Integration testing with profiles
 * 5. Database profile testing (H2, PostgreSQL, MySQL)
 * 6. Feature flag testing via profiles
 * 7. Environment-specific property testing
 * 8. Multi-profile combination testing
 * 
 * Key Features:
 * -------------
 * - Activates one or more profiles for tests
 * - Overrides default profile configuration
 * - Supports multiple profile activation
 * - Works with @Profile annotated beans
 * - Supports profile inheritance
 * - Can be combined with other test annotations
 * - Supports profile expressions
 * - Environment-specific bean loading
 * 
 * Syntax:
 * -------
 * Single profile:
 *   @ActiveProfiles("test")
 * 
 * Multiple profiles:
 *   @ActiveProfiles({"test", "h2"})
 * 
 * With inheritance:
 *   @ActiveProfiles(value = "test", inheritProfiles = false)
 * 
 * With resolver:
 *   @ActiveProfiles(resolver = CustomProfileResolver.class)
 * 
 * Profile Resolution Order:
 * -------------------------
 * 1. @ActiveProfiles annotation
 * 2. spring.profiles.active property
 * 3. Default profiles
 * 4. Profile resolver (if specified)
 * 
 * Common Profiles:
 * ----------------
 * - test: Test environment
 * - dev: Development environment
 * - prod: Production environment
 * - h2: H2 database
 * - postgres: PostgreSQL database
 * - mock: Mock services
 * - integration: Integration tests
 * - unit: Unit tests
 * 
 * Best Practices:
 * ---------------
 * 1. Use consistent profile naming
 * 2. Document profile purposes
 * 3. Avoid excessive profile combinations
 * 4. Use test-specific profiles
 * 5. Combine with @TestPropertySource
 * 6. Keep profile logic simple
 * 7. Test profile-specific beans
 * 8. Use profile groups for related configs
 * 
 * @author Spring Patterns
 * @version 1.0
 */

// Configuration with profile-specific beans
@Configuration
class ProfileConfiguration {
    
    @Bean
    @org.springframework.context.annotation.Profile("test")
    public DatabaseService testDatabaseService() {
        return new H2DatabaseService();
    }
    
    @Bean
    @org.springframework.context.annotation.Profile("prod")
    public DatabaseService prodDatabaseService() {
        return new PostgreSQLDatabaseService();
    }
    
    @Bean
    @org.springframework.context.annotation.Profile("dev")
    public DatabaseService devDatabaseService() {
        return new MySQLDatabaseService();
    }
    
    @Bean
    @org.springframework.context.annotation.Profile("test")
    public EmailService testEmailService() {
        return new MockEmailService();
    }
    
    @Bean
    @org.springframework.context.annotation.Profile("prod")
    public EmailService prodEmailService() {
        return new SmtpEmailService();
    }
    
    @Bean
    @org.springframework.context.annotation.Profile({"test", "dev"})
    public CacheService debugCacheService() {
        return new InMemoryCacheService();
    }
    
    @Bean
    @org.springframework.context.annotation.Profile("prod")
    public CacheService prodCacheService() {
        return new RedisCacheService();
    }
    
    @Bean
    @org.springframework.context.annotation.Profile("!prod")
    public DebugService debugService() {
        return new DebugService();
    }
}

// Service interfaces and implementations
interface DatabaseService {
    String getConnectionInfo();
    String getType();
}

class H2DatabaseService implements DatabaseService {
    @Override
    public String getConnectionInfo() {
        return "jdbc:h2:mem:testdb";
    }
    
    @Override
    public String getType() {
        return "H2";
    }
}

class PostgreSQLDatabaseService implements DatabaseService {
    @Override
    public String getConnectionInfo() {
        return "jdbc:postgresql://localhost:5432/proddb";
    }
    
    @Override
    public String getType() {
        return "PostgreSQL";
    }
}

class MySQLDatabaseService implements DatabaseService {
    @Override
    public String getConnectionInfo() {
        return "jdbc:mysql://localhost:3306/devdb";
    }
    
    @Override
    public String getType() {
        return "MySQL";
    }
}

interface EmailService {
    void sendEmail(String to, String message);
    String getServiceType();
}

class MockEmailService implements EmailService {
    private java.util.List<String> sentEmails = new java.util.ArrayList<>();
    
    @Override
    public void sendEmail(String to, String message) {
        sentEmails.add(to + ": " + message);
    }
    
    @Override
    public String getServiceType() {
        return "MOCK";
    }
    
    public int getSentCount() {
        return sentEmails.size();
    }
}

class SmtpEmailService implements EmailService {
    @Override
    public void sendEmail(String to, String message) {
        System.out.println("SMTP: Sending to " + to);
    }
    
    @Override
    public String getServiceType() {
        return "SMTP";
    }
}

interface CacheService {
    void put(String key, Object value);
    Object get(String key);
    String getCacheType();
}

class InMemoryCacheService implements CacheService {
    private java.util.Map<String, Object> cache = new java.util.HashMap<>();
    
    @Override
    public void put(String key, Object value) {
        cache.put(key, value);
    }
    
    @Override
    public Object get(String key) {
        return cache.get(key);
    }
    
    @Override
    public String getCacheType() {
        return "IN_MEMORY";
    }
}

class RedisCacheService implements CacheService {
    @Override
    public void put(String key, Object value) {
        System.out.println("Redis: Setting " + key);
    }
    
    @Override
    public Object get(String key) {
        return null;
    }
    
    @Override
    public String getCacheType() {
        return "REDIS";
    }
}

class DebugService {
    public String getDebugInfo() {
        return "Debug mode enabled";
    }
}

/**
 * Example 1: Single Profile Activation
 * Demonstrates activating a single test profile
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ProfileConfiguration.class)
@ActiveProfiles("test")
class SingleProfileTest {
    
    @Autowired
    private DatabaseService databaseService;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private CacheService cacheService;
    
    @Autowired
    private DebugService debugService;
    
    @Test
    void testProfileBeans() {
        assertEquals("H2", databaseService.getType());
        assertEquals("MOCK", emailService.getServiceType());
        assertEquals("IN_MEMORY", cacheService.getCacheType());
        assertNotNull(debugService);
        
        System.out.println("✓ Test profile activated");
        System.out.println("  Database: " + databaseService.getType());
        System.out.println("  Email: " + emailService.getServiceType());
        System.out.println("  Cache: " + cacheService.getCacheType());
        System.out.println("  Debug: " + debugService.getDebugInfo());
    }
}

/**
 * Example 2: Multiple Profiles Activation
 * Demonstrates activating multiple profiles simultaneously
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ProfileConfiguration.class)
@ActiveProfiles({"test", "dev"})
class MultipleProfilesTest {
    
    @Autowired
    private DatabaseService databaseService;
    
    @Autowired
    private CacheService cacheService;
    
    @Test
    void testMultipleProfiles() {
        // When multiple profiles match, last one wins or specific precedence applies
        assertNotNull(databaseService);
        assertEquals("IN_MEMORY", cacheService.getCacheType());
        
        System.out.println("✓ Multiple profiles activated: test, dev");
        System.out.println("  Database: " + databaseService.getType());
        System.out.println("  Cache: " + cacheService.getCacheType());
    }
}

/**
 * Example 3: Production Profile Testing
 * Demonstrates testing with production profile
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ProfileConfiguration.class)
@ActiveProfiles("prod")
class ProductionProfileTest {
    
    @Autowired
    private DatabaseService databaseService;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private CacheService cacheService;
    
    @Autowired(required = false)
    private DebugService debugService;
    
    @Test
    void testProductionProfile() {
        assertEquals("PostgreSQL", databaseService.getType());
        assertEquals("SMTP", emailService.getServiceType());
        assertEquals("REDIS", cacheService.getCacheType());
        assertNull(debugService, "Debug service should not be available in prod");
        
        System.out.println("✓ Production profile activated");
        System.out.println("  Database: " + databaseService.getType());
        System.out.println("  Email: " + emailService.getServiceType());
        System.out.println("  Cache: " + cacheService.getCacheType());
        System.out.println("  Debug Service: " + (debugService == null ? "Not Available" : "Available"));
    }
}

/**
 * Example 4: Development Profile Testing
 * Demonstrates testing with development profile
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ProfileConfiguration.class)
@ActiveProfiles("dev")
class DevelopmentProfileTest {
    
    @Autowired
    private DatabaseService databaseService;
    
    @Autowired
    private CacheService cacheService;
    
    @Autowired
    private DebugService debugService;
    
    @Test
    void testDevelopmentProfile() {
        assertEquals("MySQL", databaseService.getType());
        assertEquals("IN_MEMORY", cacheService.getCacheType());
        assertNotNull(debugService);
        
        System.out.println("✓ Development profile activated");
        System.out.println("  Database: " + databaseService.getType());
        System.out.println("  Cache: " + cacheService.getCacheType());
        System.out.println("  Debug: " + debugService.getDebugInfo());
    }
}

/**
 * Example 5: Profile-Specific Configuration
 * Demonstrates custom profile configurations
 */
@Configuration
class CustomProfileConfig {
    
    @Bean
    @org.springframework.context.annotation.Profile("integration")
    public IntegrationTestService integrationTestService() {
        return new IntegrationTestService();
    }
    
    @Bean
    @org.springframework.context.annotation.Profile("unit")
    public UnitTestService unitTestService() {
        return new UnitTestService();
    }
    
    static class IntegrationTestService {
        public String getType() {
            return "INTEGRATION";
        }
    }
    
    static class UnitTestService {
        public String getType() {
            return "UNIT";
        }
    }
}

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = CustomProfileConfig.class)
@ActiveProfiles("integration")
class IntegrationProfileTest {
    
    @Autowired
    private CustomProfileConfig.IntegrationTestService integrationService;
    
    @Test
    void testIntegrationProfile() {
        assertNotNull(integrationService);
        assertEquals("INTEGRATION", integrationService.getType());
        
        System.out.println("✓ Integration profile activated");
        System.out.println("  Service Type: " + integrationService.getType());
    }
}

/**
 * Example 6: Negated Profile Expression
 * Demonstrates using negated profiles (!profile)
 */
@Configuration
class NegatedProfileConfig {
    
    @Bean
    @org.springframework.context.annotation.Profile("!prod")
    public TestHelperService testHelperService() {
        return new TestHelperService();
    }
    
    static class TestHelperService {
        public boolean isAvailable() {
            return true;
        }
    }
}

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = NegatedProfileConfig.class)
@ActiveProfiles("test")
class NegatedProfileTest {
    
    @Autowired
    private NegatedProfileConfig.TestHelperService testHelper;
    
    @Test
    void testNegatedProfile() {
        assertNotNull(testHelper, "TestHelper should be available when profile is not 'prod'");
        assertTrue(testHelper.isAvailable());
        
        System.out.println("✓ Negated profile expression working");
        System.out.println("  TestHelper available: " + testHelper.isAvailable());
    }
}

/**
 * Example 7: Environment-Specific Properties with Profiles
 * Demonstrates combining profiles with property sources
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ProfileConfiguration.class)
@ActiveProfiles("test")
@org.springframework.test.context.TestPropertySource(properties = {
    "app.name=Test Application",
    "app.environment=test"
})
class ProfileWithPropertiesTest {
    
    @Autowired
    private DatabaseService databaseService;
    
    @org.springframework.beans.factory.annotation.Value("${app.environment}")
    private String environment;
    
    @Test
    void testProfileWithProperties() {
        assertEquals("test", environment);
        assertEquals("H2", databaseService.getType());
        
        System.out.println("✓ Profile with properties");
        System.out.println("  Environment: " + environment);
        System.out.println("  Database: " + databaseService.getType());
    }
}

/**
 * Main class for demonstration
 */
public class ActiveProfilesPattern {
    
    public static void main(String[] args) {
        System.out.println("=== Active Profiles Pattern ===\n");
        System.out.println("This pattern demonstrates:");
        System.out.println("1. Single profile activation");
        System.out.println("2. Multiple profiles activation");
        System.out.println("3. Production profile testing");
        System.out.println("4. Development profile testing");
        System.out.println("5. Custom profile configurations");
        System.out.println("6. Negated profile expressions");
        System.out.println("7. Profiles with property sources");
        System.out.println("8. Profile-conditional bean loading");
        System.out.println("\nRun the test classes to see the pattern in action.");
        System.out.println("=================================");
    }
}

/**
 * Common Profile Patterns:
 * 
 * Database Profiles:
 * ------------------
 * @ActiveProfiles("h2")       - H2 in-memory database
 * @ActiveProfiles("postgres") - PostgreSQL database
 * @ActiveProfiles("mysql")    - MySQL database
 * 
 * Environment Profiles:
 * ---------------------
 * @ActiveProfiles("test")     - Test environment
 * @ActiveProfiles("dev")      - Development environment
 * @ActiveProfiles("prod")     - Production environment
 * 
 * Feature Profiles:
 * -----------------
 * @ActiveProfiles("mock")     - Mock external services
 * @ActiveProfiles("cloud")    - Cloud-specific features
 * @ActiveProfiles("secure")   - Security enabled
 * 
 * Test Type Profiles:
 * -------------------
 * @ActiveProfiles("integration") - Integration tests
 * @ActiveProfiles("unit")        - Unit tests
 * @ActiveProfiles("e2e")          - End-to-end tests
 * 
 * Combined Profiles:
 * ------------------
 * @ActiveProfiles({"test", "h2", "mock"})
 * @ActiveProfiles({"dev", "mysql", "debug"})
 * @ActiveProfiles({"prod", "postgres", "secure"})
 */
