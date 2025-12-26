package com.example.testing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test Configuration Pattern
 * ===========================
 * 
 * Demonstrates the @TestConfiguration annotation pattern for defining
 * test-specific Spring configurations that complement or override main
 * application configurations during testing.
 * 
 * Use Cases:
 * ----------
 * 1. Override beans with test doubles/mocks
 * 2. Add test-specific bean configurations
 * 3. Replace production dependencies with test versions
 * 4. Configure embedded databases for testing
 * 5. Set up test-specific services
 * 6. Mock external integrations
 * 7. Configure test data sources
 * 8. Setup test security configurations
 * 
 * Key Features:
 * -------------
 * - @TestConfiguration is not picked up by component scanning
 * - Must be explicitly imported or used as nested class
 * - Can override existing beans with @Primary
 * - Supports all standard Spring configuration features
 * - Isolated from production configurations
 * - Can be reused across multiple test classes
 * - Supports @Bean, @Import, @PropertySource
 * - Works with Spring Boot test slices
 * 
 * @TestConfiguration vs @Configuration:
 * -------------------------------------
 * @TestConfiguration:
 *   - Not auto-detected by component scanning
 *   - Specifically for test scenarios
 *   - Must be explicitly imported or nested
 *   - Clearly marks test-only configurations
 *   - Preferred for test beans
 * 
 * @Configuration:
 *   - Auto-detected by component scanning
 *   - Used in production code
 *   - Available to all contexts
 *   - Generic purpose configuration
 * 
 * Usage Patterns:
 * ---------------
 * 1. Nested inner class:
 *    @TestConfiguration
 *    static class TestConfig { ... }
 * 
 * 2. Explicit import:
 *    @Import(MyTestConfiguration.class)
 * 
 * 3. Separate file:
 *    @ContextConfiguration(classes = {AppConfig.class, TestConfig.class})
 * 
 * 4. With Spring Boot Test:
 *    @SpringBootTest
 *    @Import(TestConfig.class)
 * 
 * Best Practices:
 * ---------------
 * 1. Use for test-specific beans only
 * 2. Keep test configurations simple
 * 3. Name clearly (e.g., *TestConfig)
 * 4. Document overrides and reasons
 * 5. Avoid complex logic in test configs
 * 6. Use @Primary to override beans
 * 7. Group related test beans together
 * 8. Consider reusability across tests
 * 
 * Common Patterns:
 * ----------------
 * 1. Mock external services
 * 2. Embedded database setup
 * 3. Test security configuration
 * 4. Custom test beans
 * 5. Property overrides
 * 6. Test event listeners
 * 7. Test AOP aspects
 * 8. Custom validators for tests
 * 
 * @author Spring Patterns
 * @version 1.0
 */

// Main configuration class
@org.springframework.context.annotation.Configuration
class MainConfiguration {
    
    @Bean
    public UserService userService() {
        return new ProductionUserService();
    }
    
    @Bean
    public EmailService emailService() {
        return new ProductionEmailService();
    }
    
    @Bean
    public PaymentGateway paymentGateway() {
        return new ProductionPaymentGateway();
    }
}

// Domain and service classes
interface UserService {
    String createUser(String name);
    String getUserType();
}

interface EmailService {
    void sendEmail(String to, String message);
    boolean isTestMode();
}

interface PaymentGateway {
    boolean processPayment(double amount);
    String getGatewayType();
}

// Production implementations
class ProductionUserService implements UserService {
    @Override
    public String createUser(String name) {
        return "Production: Created user " + name;
    }
    
    @Override
    public String getUserType() {
        return "PRODUCTION";
    }
}

class ProductionEmailService implements EmailService {
    @Override
    public void sendEmail(String to, String message) {
        System.out.println("Production: Sending email to " + to);
    }
    
    @Override
    public boolean isTestMode() {
        return false;
    }
}

class ProductionPaymentGateway implements PaymentGateway {
    @Override
    public boolean processPayment(double amount) {
        System.out.println("Production: Processing payment of $" + amount);
        return true;
    }
    
    @Override
    public String getGatewayType() {
        return "PRODUCTION";
    }
}

// Test implementations
class TestUserService implements UserService {
    @Override
    public String createUser(String name) {
        return "Test: Created user " + name;
    }
    
    @Override
    public String getUserType() {
        return "TEST";
    }
}

class TestEmailService implements EmailService {
    private java.util.List<String> sentEmails = new java.util.ArrayList<>();
    
    @Override
    public void sendEmail(String to, String message) {
        sentEmails.add(to + ": " + message);
        System.out.println("Test: Email captured (not actually sent)");
    }
    
    @Override
    public boolean isTestMode() {
        return true;
    }
    
    public java.util.List<String> getSentEmails() {
        return sentEmails;
    }
}

class MockPaymentGateway implements PaymentGateway {
    private double totalProcessed = 0;
    
    @Override
    public boolean processPayment(double amount) {
        totalProcessed += amount;
        System.out.println("Mock: Payment of $" + amount + " processed");
        return true;
    }
    
    @Override
    public String getGatewayType() {
        return "MOCK";
    }
    
    public double getTotalProcessed() {
        return totalProcessed;
    }
}

/**
 * Example 1: Nested Test Configuration
 * Demonstrates using @TestConfiguration as nested inner class
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = MainConfiguration.class)
class NestedTestConfigurationTest {
    
    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public UserService userService() {
            return new TestUserService();
        }
    }
    
    @Autowired
    private UserService userService;
    
    @Test
    void testNestedConfiguration() {
        String result = userService.createUser("John");
        assertTrue(result.startsWith("Test:"), "Should use test implementation");
        assertEquals("TEST", userService.getUserType());
        
        System.out.println("✓ Nested test configuration working");
        System.out.println("  User Type: " + userService.getUserType());
    }
}

/**
 * Example 2: Separate Test Configuration File
 * Demonstrates external test configuration class
 */
@TestConfiguration
class ExternalTestConfig {
    
    @Bean
    @Primary
    public EmailService emailService() {
        return new TestEmailService();
    }
    
    @Bean
    @Primary
    public PaymentGateway paymentGateway() {
        return new MockPaymentGateway();
    }
}

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {MainConfiguration.class, ExternalTestConfig.class})
class ExternalTestConfigurationTest {
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private PaymentGateway paymentGateway;
    
    @Test
    void testExternalConfiguration() {
        assertTrue(emailService.isTestMode(), "Should use test email service");
        assertEquals("MOCK", paymentGateway.getGatewayType());
        
        System.out.println("✓ External test configuration working");
        System.out.println("  Email Test Mode: " + emailService.isTestMode());
        System.out.println("  Gateway Type: " + paymentGateway.getGatewayType());
    }
}

/**
 * Example 3: Multiple Test Configurations
 * Demonstrates combining multiple test configurations
 */
@TestConfiguration
class DatabaseTestConfig {
    
    @Bean
    public TestDatabaseService testDatabaseService() {
        return new TestDatabaseService();
    }
    
    static class TestDatabaseService {
        public String getConnectionString() {
            return "jdbc:h2:mem:testdb";
        }
    }
}

@TestConfiguration
class SecurityTestConfig {
    
    @Bean
    public TestSecurityService testSecurityService() {
        return new TestSecurityService();
    }
    
    static class TestSecurityService {
        public boolean isSecurityEnabled() {
            return false; // Disable security for tests
        }
    }
}

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    MainConfiguration.class,
    DatabaseTestConfig.class,
    SecurityTestConfig.class
})
class MultipleTestConfigurationsTest {
    
    @Autowired
    private DatabaseTestConfig.TestDatabaseService databaseService;
    
    @Autowired
    private SecurityTestConfig.TestSecurityService securityService;
    
    @Test
    void testMultipleConfigurations() {
        assertNotNull(databaseService);
        assertNotNull(securityService);
        
        assertEquals("jdbc:h2:mem:testdb", databaseService.getConnectionString());
        assertFalse(securityService.isSecurityEnabled());
        
        System.out.println("✓ Multiple test configurations loaded");
        System.out.println("  Database: " + databaseService.getConnectionString());
        System.out.println("  Security Enabled: " + securityService.isSecurityEnabled());
    }
}

/**
 * Example 4: Test Configuration with Properties
 * Demonstrates combining test configuration with property sources
 */
@TestConfiguration
@org.springframework.context.annotation.PropertySource("classpath:test.properties")
class PropertyTestConfig {
    
    @Bean
    public TestPropertyBean testPropertyBean(
            @org.springframework.beans.factory.annotation.Value("${test.property:default}") String property) {
        return new TestPropertyBean(property);
    }
    
    static class TestPropertyBean {
        private final String value;
        
        public TestPropertyBean(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
    }
}

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {MainConfiguration.class, PropertyTestConfig.class})
@org.springframework.test.context.TestPropertySource(properties = "test.property=test-value")
class PropertyTestConfigurationTest {
    
    @Autowired
    private PropertyTestConfig.TestPropertyBean propertyBean;
    
    @Test
    void testPropertyConfiguration() {
        assertNotNull(propertyBean);
        assertEquals("test-value", propertyBean.getValue());
        
        System.out.println("✓ Test configuration with properties working");
        System.out.println("  Property Value: " + propertyBean.getValue());
    }
}

/**
 * Example 5: Test Configuration with Bean Overrides
 * Demonstrates overriding multiple beans for testing
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = MainConfiguration.class)
class BeanOverrideTest {
    
    @TestConfiguration
    static class OverrideConfig {
        
        @Bean
        @Primary
        public UserService userService() {
            return new TestUserService();
        }
        
        @Bean
        @Primary
        public EmailService emailService() {
            return new TestEmailService();
        }
        
        @Bean
        @Primary
        public PaymentGateway paymentGateway() {
            return new MockPaymentGateway();
        }
    }
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private PaymentGateway paymentGateway;
    
    @Test
    void testAllBeansOverridden() {
        assertEquals("TEST", userService.getUserType());
        assertTrue(emailService.isTestMode());
        assertEquals("MOCK", paymentGateway.getGatewayType());
        
        System.out.println("✓ All beans successfully overridden");
        System.out.println("  UserService: " + userService.getUserType());
        System.out.println("  EmailService Test Mode: " + emailService.isTestMode());
        System.out.println("  PaymentGateway: " + paymentGateway.getGatewayType());
    }
}

/**
 * Example 6: Test Configuration with Custom Services
 * Demonstrates adding test-only services
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = MainConfiguration.class)
class CustomTestServicesTest {
    
    @TestConfiguration
    static class CustomServicesConfig {
        
        @Bean
        public TestDataGenerator testDataGenerator() {
            return new TestDataGenerator();
        }
        
        @Bean
        public TestValidator testValidator() {
            return new TestValidator();
        }
    }
    
    static class TestDataGenerator {
        public String generateTestData() {
            return "test-data-" + System.currentTimeMillis();
        }
    }
    
    static class TestValidator {
        public boolean validate(String data) {
            return data != null && data.startsWith("test-");
        }
    }
    
    @Autowired
    private TestDataGenerator dataGenerator;
    
    @Autowired
    private TestValidator validator;
    
    @Test
    void testCustomServices() {
        String testData = dataGenerator.generateTestData();
        assertTrue(validator.validate(testData));
        
        System.out.println("✓ Custom test services working");
        System.out.println("  Generated Data: " + testData);
        System.out.println("  Validation: " + validator.validate(testData));
    }
}

/**
 * Example 7: Conditional Test Configuration
 * Demonstrates conditional bean creation in tests
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = MainConfiguration.class)
@org.springframework.test.context.ActiveProfiles("test")
class ConditionalTestConfigurationTest {
    
    @TestConfiguration
    static class ConditionalConfig {
        
        @Bean
        @org.springframework.context.annotation.Profile("test")
        public TestModeService testModeService() {
            return new TestModeService();
        }
    }
    
    static class TestModeService {
        public String getMode() {
            return "TEST_MODE";
        }
    }
    
    @Autowired
    private TestModeService testModeService;
    
    @Test
    void testConditionalConfiguration() {
        assertNotNull(testModeService);
        assertEquals("TEST_MODE", testModeService.getMode());
        
        System.out.println("✓ Conditional test configuration working");
        System.out.println("  Mode: " + testModeService.getMode());
    }
}

/**
 * Main class for demonstration
 */
public class TestConfigurationPattern {
    
    public static void main(String[] args) {
        System.out.println("=== Test Configuration Pattern ===\n");
        System.out.println("This pattern demonstrates:");
        System.out.println("1. Nested test configuration classes");
        System.out.println("2. External test configuration files");
        System.out.println("3. Multiple test configurations");
        System.out.println("4. Test configuration with properties");
        System.out.println("5. Bean overrides with @Primary");
        System.out.println("6. Custom test-only services");
        System.out.println("7. Conditional test configuration");
        System.out.println("8. Test doubles and mocks setup");
        System.out.println("\nRun the test classes to see the pattern in action.");
        System.out.println("====================================");
    }
}

/**
 * Best Practices Summary:
 * 
 * 1. Organization:
 *    - Keep test configs in test packages
 *    - Name with *TestConfig suffix
 *    - Group related test beans
 * 
 * 2. Bean Overriding:
 *    - Use @Primary for overrides
 *    - Document why beans are replaced
 *    - Keep test beans simple
 * 
 * 3. Reusability:
 *    - Create shareable test configs
 *    - Use base test classes
 *    - Extract common test beans
 * 
 * 4. Isolation:
 *    - Don't mix test and production configs
 *    - Use @TestConfiguration explicitly
 *    - Avoid side effects
 * 
 * 5. Clarity:
 *    - Document test configuration purpose
 *    - Use descriptive bean names
 *    - Keep configurations focused
 */
