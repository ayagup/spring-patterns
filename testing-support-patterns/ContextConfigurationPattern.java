package com.example.testing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Context Configuration Pattern
 * ==============================
 * 
 * Demonstrates the @ContextConfiguration annotation pattern for loading
 * and configuring the Spring ApplicationContext for integration tests.
 * 
 * Use Cases:
 * ----------
 * 1. Load specific configuration classes for tests
 * 2. Configure test-specific application context
 * 3. Use custom context loaders
 * 4. Specify XML configuration files
 * 5. Combine multiple configuration sources
 * 6. Inherit parent context configurations
 * 7. Initialize context with specific profiles
 * 8. Test modular application contexts
 * 
 * Key Features:
 * -------------
 * - Loads Spring ApplicationContext for tests
 * - Supports Java @Configuration classes
 * - Supports XML configuration files
 * - Allows custom ContextLoader implementations
 * - Supports configuration inheritance
 * - Can specify initialization parameters
 * - Works with component scanning
 * - Caches contexts for performance
 * 
 * Annotation Attributes:
 * ----------------------
 * classes         - Java configuration classes to load
 * locations       - XML configuration file locations
 * loader          - Custom ContextLoader class
 * initializers    - ApplicationContextInitializer classes
 * inheritLocations - Inherit locations from superclass
 * inheritInitializers - Inherit initializers from superclass
 * name            - Name of the context hierarchy level
 * 
 * Configuration Methods:
 * ----------------------
 * 1. Java Configuration:
 *    @ContextConfiguration(classes = MyConfig.class)
 * 
 * 2. XML Configuration:
 *    @ContextConfiguration(locations = "classpath:test-context.xml")
 * 
 * 3. Custom Loader:
 *    @ContextConfiguration(loader = CustomContextLoader.class)
 * 
 * 4. Multiple Configurations:
 *    @ContextConfiguration(classes = {Config1.class, Config2.class})
 * 
 * Best Practices:
 * ---------------
 * 1. Use Java configuration over XML
 * 2. Create test-specific configurations
 * 3. Keep test contexts minimal
 * 4. Reuse configurations across tests
 * 5. Use @Import for modular configs
 * 6. Avoid loading full application context
 * 7. Use test slices when possible
 * 8. Document configuration choices
 * 
 * Context Hierarchy:
 * ------------------
 * Tests can have hierarchical contexts:
 * - Parent context with shared beans
 * - Child context with test-specific beans
 * - Child can override parent beans
 * 
 * Common Patterns:
 * ----------------
 * 1. Service layer testing with minimal context
 * 2. Repository testing with data configuration
 * 3. Web layer testing with MVC context
 * 4. Integration testing with full context
 * 5. Multi-module testing with separate contexts
 * 6. Testing with profiles and properties
 * 7. Custom initializer for test setup
 * 8. Inheritance-based configuration reuse
 * 
 * @author Spring Patterns
 * @version 1.0
 */

// Configuration class 1
@Configuration
class ServiceConfig {
    
    @Bean
    public UserService userService() {
        return new UserService();
    }
    
    @Bean
    public EmailService emailService() {
        return new EmailService();
    }
}

// Configuration class 2
@Configuration
class RepositoryConfig {
    
    @Bean
    public UserRepository userRepository() {
        return new UserRepository();
    }
    
    @Bean
    public OrderRepository orderRepository() {
        return new OrderRepository();
    }
}

// Configuration class 3
@Configuration
class CacheConfig {
    
    @Bean
    public CacheService cacheService() {
        return new CacheService();
    }
}

// Combined configuration
@Configuration
class AppConfig {
    
    @Bean
    public AppService appService() {
        return new AppService();
    }
    
    @Bean
    public DatabaseService databaseService() {
        return new DatabaseService();
    }
}

// Service classes
class UserService {
    public String getServiceName() {
        return "UserService";
    }
    
    public String processUser(String name) {
        return "Processed: " + name;
    }
}

class EmailService {
    public String getServiceName() {
        return "EmailService";
    }
    
    public void sendEmail(String to, String message) {
        System.out.println("  [EMAIL] Sending to " + to + ": " + message);
    }
}

class UserRepository {
    public String getType() {
        return "UserRepository";
    }
}

class OrderRepository {
    public String getType() {
        return "OrderRepository";
    }
}

class CacheService {
    private final java.util.Map<String, Object> cache = new java.util.HashMap<>();
    
    public void put(String key, Object value) {
        cache.put(key, value);
    }
    
    public Object get(String key) {
        return cache.get(key);
    }
    
    public int size() {
        return cache.size();
    }
}

class AppService {
    public String getVersion() {
        return "1.0.0";
    }
}

class DatabaseService {
    public String getConnectionInfo() {
        return "jdbc:h2:mem:testdb";
    }
}

/**
 * Example 1: Single Configuration Class
 * Demonstrates loading single @Configuration class
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ServiceConfig.class)
class SingleConfigTest {
    
    @Autowired
    private ApplicationContext context;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private EmailService emailService;
    
    @Test
    void testContextLoaded() {
        System.out.println("\n=== Test: Single Configuration Class ===");
        
        assertNotNull(context);
        System.out.println("✓ Application context loaded");
    }
    
    @Test
    void testServiceBeansAvailable() {
        System.out.println("\n=== Test: Service Beans Available ===");
        
        assertNotNull(userService);
        assertNotNull(emailService);
        
        assertEquals("UserService", userService.getServiceName());
        assertEquals("EmailService", emailService.getServiceName());
        
        System.out.println("✓ Service beans loaded from ServiceConfig");
    }
    
    @Test
    void testBeanCount() {
        System.out.println("\n=== Test: Bean Count ===");
        
        String[] beanNames = context.getBeanDefinitionNames();
        System.out.println("  Total beans: " + beanNames.length);
        
        assertTrue(context.containsBean("userService"));
        assertTrue(context.containsBean("emailService"));
        
        System.out.println("✓ Expected beans present in context");
    }
}

/**
 * Example 2: Multiple Configuration Classes
 * Demonstrates loading multiple @Configuration classes
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ServiceConfig.class, RepositoryConfig.class})
class MultipleConfigTest {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Test
    void testAllBeansLoaded() {
        System.out.println("\n=== Test: Multiple Configurations ===");
        
        assertNotNull(userService);
        assertNotNull(emailService);
        assertNotNull(userRepository);
        assertNotNull(orderRepository);
        
        System.out.println("✓ Beans from multiple configs loaded");
        System.out.println("  - ServiceConfig: UserService, EmailService");
        System.out.println("  - RepositoryConfig: UserRepository, OrderRepository");
    }
    
    @Test
    void testServiceAndRepositoryInteraction() {
        System.out.println("\n=== Test: Service-Repository Interaction ===");
        
        String result = userService.processUser("John Doe");
        assertEquals("Processed: John Doe", result);
        
        assertEquals("UserRepository", userRepository.getType());
        
        System.out.println("✓ Services and repositories work together");
    }
}

/**
 * Example 3: Configuration with Custom Loader
 * Demonstrates using custom ContextLoader
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(
    classes = AppConfig.class,
    loader = AnnotationConfigContextLoader.class
)
class CustomLoaderTest {
    
    @Autowired
    private AppService appService;
    
    @Autowired
    private DatabaseService databaseService;
    
    @Test
    void testContextWithCustomLoader() {
        System.out.println("\n=== Test: Custom Context Loader ===");
        
        assertNotNull(appService);
        assertNotNull(databaseService);
        
        assertEquals("1.0.0", appService.getVersion());
        assertEquals("jdbc:h2:mem:testdb", databaseService.getConnectionInfo());
        
        System.out.println("✓ Context loaded with AnnotationConfigContextLoader");
    }
}

/**
 * Example 4: Nested Test Configuration
 * Demonstrates nested @Configuration class
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration
class NestedConfigTest {
    
    @Configuration
    static class TestConfig {
        
        @Bean
        public TestService testService() {
            return new TestService();
        }
        
        @Bean
        public ValidationService validationService() {
            return new ValidationService();
        }
    }
    
    @Autowired
    private TestService testService;
    
    @Autowired
    private ValidationService validationService;
    
    @Test
    void testNestedConfiguration() {
        System.out.println("\n=== Test: Nested Configuration ===");
        
        assertNotNull(testService);
        assertNotNull(validationService);
        
        assertTrue(testService.isActive());
        assertTrue(validationService.validate("test"));
        
        System.out.println("✓ Nested @Configuration loaded automatically");
    }
}

class TestService {
    public boolean isActive() {
        return true;
    }
}

class ValidationService {
    public boolean validate(String input) {
        return input != null && !input.isEmpty();
    }
}

/**
 * Example 5: Context Configuration Inheritance
 * Demonstrates inheriting context configuration from parent test
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ServiceConfig.class)
abstract class BaseIntegrationTest {
    
    @Autowired
    protected UserService userService;
    
    @Autowired
    protected EmailService emailService;
}

class ChildIntegrationTest extends BaseIntegrationTest {
    
    @Test
    void testInheritedContext() {
        System.out.println("\n=== Test: Inherited Configuration ===");
        
        assertNotNull(userService);
        assertNotNull(emailService);
        
        System.out.println("✓ Context inherited from BaseIntegrationTest");
        System.out.println("  - UserService available");
        System.out.println("  - EmailService available");
    }
    
    @Test
    void testUseInheritedBeans() {
        System.out.println("\n=== Test: Use Inherited Beans ===");
        
        String result = userService.processUser("Inherited User");
        assertEquals("Processed: Inherited User", result);
        
        emailService.sendEmail("test@example.com", "Inheritance works!");
        
        System.out.println("✓ Can use beans from parent context");
    }
}

/**
 * Example 6: Additional Configuration
 * Demonstrates extending parent configuration
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ServiceConfig.class, CacheConfig.class})
class ExtendedConfigTest extends BaseIntegrationTest {
    
    @Autowired
    private CacheService cacheService;
    
    @Test
    void testExtendedConfiguration() {
        System.out.println("\n=== Test: Extended Configuration ===");
        
        // Has both parent and additional beans
        assertNotNull(userService);      // From ServiceConfig (parent)
        assertNotNull(emailService);     // From ServiceConfig (parent)
        assertNotNull(cacheService);     // From CacheConfig (additional)
        
        System.out.println("✓ Extended with additional CacheConfig");
    }
    
    @Test
    void testCacheServiceFunctionality() {
        System.out.println("\n=== Test: Cache Service ===");
        
        cacheService.put("key1", "value1");
        cacheService.put("key2", "value2");
        
        assertEquals("value1", cacheService.get("key1"));
        assertEquals(2, cacheService.size());
        
        System.out.println("✓ Additional cache service works");
    }
}

/**
 * Main class for demonstration
 */
public class ContextConfigurationPattern {
    
    public static void main(String[] args) {
        System.out.println("=== Context Configuration Pattern ===\n");
        System.out.println("This pattern demonstrates:");
        System.out.println("1. Loading single @Configuration class");
        System.out.println("2. Loading multiple configuration classes");
        System.out.println("3. Using custom ContextLoader");
        System.out.println("4. Nested test configuration");
        System.out.println("5. Configuration inheritance");
        System.out.println("6. Extending parent configurations");
        System.out.println("7. ApplicationContext features");
        System.out.println("8. Bean availability verification");
        System.out.println("\nRun the test classes to see the pattern in action.");
        System.out.println("==========================");
    }
}

/**
 * Context Configuration Summary:
 * 
 * Single Configuration:
 * ---------------------
 * @ContextConfiguration(classes = MyConfig.class)
 * class MyTest { }
 * 
 * Multiple Configurations:
 * ------------------------
 * @ContextConfiguration(classes = {Config1.class, Config2.class})
 * class MyTest { }
 * 
 * XML Configuration:
 * ------------------
 * @ContextConfiguration(locations = "classpath:applicationContext.xml")
 * class MyTest { }
 * 
 * Custom Loader:
 * --------------
 * @ContextConfiguration(
 *     classes = MyConfig.class,
 *     loader = CustomContextLoader.class
 * )
 * class MyTest { }
 * 
 * Nested Configuration:
 * ---------------------
 * @ContextConfiguration
 * class MyTest {
 *     @Configuration
 *     static class TestConfig {
 *         @Bean
 *         public MyBean myBean() { }
 *     }
 * }
 * 
 * Inheritance:
 * ------------
 * @ContextConfiguration(classes = BaseConfig.class)
 * abstract class BaseTest { }
 * 
 * class ChildTest extends BaseTest {
 *     // Inherits BaseConfig automatically
 * }
 * 
 * With Initializers:
 * ------------------
 * @ContextConfiguration(
 *     classes = MyConfig.class,
 *     initializers = MyContextInitializer.class
 * )
 * class MyTest { }
 */
