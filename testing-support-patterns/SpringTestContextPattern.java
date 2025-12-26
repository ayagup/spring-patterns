package com.example.testing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Spring Test Context Pattern
 * ============================
 * 
 * Demonstrates the Spring Test Context framework pattern for integration testing
 * with Spring ApplicationContext management, dependency injection in tests, and
 * test execution lifecycle management.
 * 
 * Use Cases:
 * ----------
 * 1. Integration testing with Spring context
 * 2. Testing Spring beans and their interactions
 * 3. Testing dependency injection configurations
 * 4. Testing Spring AOP and transactions
 * 5. Testing Spring MVC controllers
 * 6. Testing Spring Data repositories
 * 7. Testing Spring Security configurations
 * 8. End-to-end application testing
 * 
 * Key Features:
 * -------------
 * - Manages Spring ApplicationContext lifecycle
 * - Caches context between tests for performance
 * - Supports dependency injection in test classes
 * - Provides transaction management for tests
 * - Supports multiple context configurations
 * - Integrates with JUnit 4, JUnit 5, and TestNG
 * - Provides test execution listeners
 * - Supports context hierarchies
 * 
 * Core Annotations:
 * -----------------
 * @ExtendWith(SpringExtension.class) - JUnit 5 extension
 * @RunWith(SpringRunner.class) - JUnit 4 runner
 * @ContextConfiguration - Specifies how to load context
 * @SpringBootTest - Boot-specific test annotation
 * @WebAppConfiguration - Web application context
 * @ActiveProfiles - Activate specific profiles
 * @TestPropertySource - Override properties
 * @DirtiesContext - Marks context for reload
 * 
 * Context Configuration Options:
 * ------------------------------
 * 1. Java-based configuration:
 *    @ContextConfiguration(classes = AppConfig.class)
 * 
 * 2. XML-based configuration:
 *    @ContextConfiguration(locations = "classpath:app-config.xml")
 * 
 * 3. Mixed configuration:
 *    @ContextConfiguration(classes = AppConfig.class, 
 *                         locations = "classpath:datasource.xml")
 * 
 * 4. Context initializers:
 *    @ContextConfiguration(initializers = CustomInitializer.class)
 * 
 * 5. Custom loader:
 *    @ContextConfiguration(loader = CustomContextLoader.class)
 * 
 * Context Caching:
 * ----------------
 * - Contexts are cached by default
 * - Cache key: configuration locations, classes, profiles, etc.
 * - Improves test performance significantly
 * - Same configuration = same cached context
 * - Use @DirtiesContext to force reload
 * 
 * Test Execution Lifecycle:
 * -------------------------
 * 1. TestContext framework initializes
 * 2. ApplicationContext created (or retrieved from cache)
 * 3. Test instance created
 * 4. Dependency injection performed
 * 5. @Before/@BeforeEach methods executed
 * 6. Test method executed
 * 7. @After/@AfterEach methods executed
 * 8. Context optionally cleaned (if dirty)
 * 
 * Best Practices:
 * ---------------
 * 1. Minimize context reloads (@DirtiesContext)
 * 2. Use profiles for test-specific configurations
 * 3. Override properties with @TestPropertySource
 * 4. Keep test configurations lightweight
 * 5. Use test slices for focused testing
 * 6. Clean up resources in @AfterEach
 * 7. Use constructor injection in tests
 * 8. Avoid static state in tests
 * 
 * Common Patterns:
 * ----------------
 * 1. Service layer testing
 * 2. Repository testing
 * 3. Controller testing
 * 4. Configuration testing
 * 5. Security testing
 * 6. Transaction testing
 * 7. Async testing
 * 8. Event testing
 * 
 * @author Spring Patterns
 * @version 1.0
 */

// Test configuration class
@org.springframework.context.annotation.Configuration
class TestConfig {
    
    @org.springframework.context.annotation.Bean
    public UserService userService() {
        return new UserService(userRepository());
    }
    
    @org.springframework.context.annotation.Bean
    public UserRepository userRepository() {
        return new UserRepository();
    }
    
    @org.springframework.context.annotation.Bean
    public EmailService emailService() {
        return new EmailService();
    }
    
    @org.springframework.context.annotation.Bean
    public NotificationService notificationService() {
        return new NotificationService(emailService());
    }
}

// Domain classes
class User {
    private Long id;
    private String name;
    private String email;
    
    public User(Long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }
    
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
}

// Service layer
class UserService {
    private final UserRepository userRepository;
    
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    public User createUser(String name, String email) {
        User user = new User(System.currentTimeMillis(), name, email);
        userRepository.save(user);
        return user;
    }
    
    public User findById(Long id) {
        return userRepository.findById(id);
    }
    
    public void deleteUser(Long id) {
        userRepository.delete(id);
    }
}

// Repository layer
class UserRepository {
    private java.util.Map<Long, User> storage = new java.util.HashMap<>();
    
    public void save(User user) {
        storage.put(user.getId(), user);
    }
    
    public User findById(Long id) {
        return storage.get(id);
    }
    
    public void delete(Long id) {
        storage.remove(id);
    }
    
    public long count() {
        return storage.size();
    }
    
    public void clear() {
        storage.clear();
    }
}

// Email service
class EmailService {
    public void sendEmail(String to, String subject, String body) {
        System.out.println("Sending email to: " + to);
        System.out.println("Subject: " + subject);
        System.out.println("Body: " + body);
    }
}

// Notification service
class NotificationService {
    private final EmailService emailService;
    
    public NotificationService(EmailService emailService) {
        this.emailService = emailService;
    }
    
    public void notifyUser(String email, String message) {
        emailService.sendEmail(email, "Notification", message);
    }
}

/**
 * Example 1: Basic Spring Test Context
 * Demonstrates fundamental test context setup
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
class BasicSpringTestContextTest {
    
    @Autowired
    private ApplicationContext applicationContext;
    
    @Autowired
    private UserService userService;
    
    @Test
    void contextLoads() {
        assertNotNull(applicationContext, "Application context should not be null");
        System.out.println("✓ Application context loaded successfully");
    }
    
    @Test
    void testDependencyInjection() {
        assertNotNull(userService, "UserService should be autowired");
        System.out.println("✓ Dependency injection working correctly");
    }
    
    @Test
    void testBeanCount() {
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        assertTrue(beanNames.length > 0, "Context should contain beans");
        System.out.println("✓ Total beans in context: " + beanNames.length);
    }
}

/**
 * Example 2: Service Layer Testing
 * Demonstrates testing business logic with Spring context
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
class ServiceLayerTest {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private UserRepository userRepository;
    
    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        userRepository.clear();
        System.out.println("Setup: Repository cleared");
    }
    
    @Test
    void testCreateUser() {
        User user = userService.createUser("John Doe", "john@example.com");
        
        assertNotNull(user, "Created user should not be null");
        assertEquals("John Doe", user.getName());
        assertEquals("john@example.com", user.getEmail());
        
        System.out.println("✓ User created successfully: " + user.getName());
    }
    
    @Test
    void testFindUserById() {
        User createdUser = userService.createUser("Jane Doe", "jane@example.com");
        User foundUser = userService.findById(createdUser.getId());
        
        assertNotNull(foundUser, "User should be found");
        assertEquals(createdUser.getId(), foundUser.getId());
        assertEquals(createdUser.getName(), foundUser.getName());
        
        System.out.println("✓ User found by ID: " + foundUser.getName());
    }
    
    @Test
    void testDeleteUser() {
        User user = userService.createUser("Bob Smith", "bob@example.com");
        Long userId = user.getId();
        
        userService.deleteUser(userId);
        User deletedUser = userService.findById(userId);
        
        assertNull(deletedUser, "Deleted user should not be found");
        System.out.println("✓ User deleted successfully");
    }
}

/**
 * Example 3: Testing Bean Interactions
 * Demonstrates testing multiple beans working together
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
class BeanInteractionTest {
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private EmailService emailService;
    
    @Test
    void testServiceDependencies() {
        assertNotNull(notificationService, "NotificationService should be autowired");
        assertNotNull(emailService, "EmailService should be autowired");
        System.out.println("✓ All service dependencies resolved");
    }
    
    @Test
    void testNotificationSending() {
        notificationService.notifyUser("user@example.com", "Test notification");
        System.out.println("✓ Notification sent successfully");
    }
}

/**
 * Example 4: Testing with Constructor Injection
 * Demonstrates preferred constructor-based dependency injection in tests
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
class ConstructorInjectionTest {
    
    private final UserService userService;
    private final UserRepository userRepository;
    
    @Autowired
    public ConstructorInjectionTest(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }
    
    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        userRepository.clear();
    }
    
    @Test
    void testWithConstructorInjection() {
        assertNotNull(userService, "UserService should be injected");
        assertNotNull(userRepository, "UserRepository should be injected");
        
        User user = userService.createUser("Constructor Test", "test@example.com");
        assertEquals(1, userRepository.count());
        
        System.out.println("✓ Constructor injection working correctly");
    }
}

/**
 * Example 5: Context Configuration with Multiple Classes
 */
@org.springframework.context.annotation.Configuration
class AdditionalTestConfig {
    
    @org.springframework.context.annotation.Bean
    public AuditService auditService() {
        return new AuditService();
    }
    
    static class AuditService {
        public void audit(String action) {
            System.out.println("Audit: " + action);
        }
    }
}

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TestConfig.class, AdditionalTestConfig.class})
class MultipleConfigurationTest {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private AdditionalTestConfig.AuditService auditService;
    
    @Test
    void testMultipleConfigurations() {
        assertNotNull(userService, "UserService from TestConfig should be available");
        assertNotNull(auditService, "AuditService from AdditionalTestConfig should be available");
        
        auditService.audit("User service accessed");
        System.out.println("✓ Multiple configurations loaded successfully");
    }
}

/**
 * Example 6: Testing ApplicationContext Features
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
class ApplicationContextFeaturesTest {
    
    @Autowired
    private ApplicationContext context;
    
    @Test
    void testBeanRetrieval() {
        UserService userService = context.getBean(UserService.class);
        assertNotNull(userService, "Should retrieve bean by type");
        
        UserService userServiceByName = context.getBean("userService", UserService.class);
        assertNotNull(userServiceByName, "Should retrieve bean by name");
        
        assertSame(userService, userServiceByName, "Should be same instance (singleton)");
        
        System.out.println("✓ Bean retrieval methods working correctly");
    }
    
    @Test
    void testEnvironment() {
        String[] activeProfiles = context.getEnvironment().getActiveProfiles();
        System.out.println("Active profiles: " + java.util.Arrays.toString(activeProfiles));
        System.out.println("✓ Environment accessible from context");
    }
    
    @Test
    void testBeanFactory() {
        assertTrue(context.containsBean("userService"), "Should contain userService bean");
        assertTrue(context.containsBean("userRepository"), "Should contain userRepository bean");
        
        String[] beanNames = context.getBeanNamesForType(UserService.class);
        assertEquals(1, beanNames.length, "Should have exactly one UserService bean");
        
        System.out.println("✓ Bean factory operations working correctly");
    }
}

/**
 * Main class for demonstration
 */
public class SpringTestContextPattern {
    
    public static void main(String[] args) {
        System.out.println("=== Spring Test Context Pattern ===\n");
        System.out.println("This pattern demonstrates:");
        System.out.println("1. Spring Test Context framework setup");
        System.out.println("2. ApplicationContext management in tests");
        System.out.println("3. Dependency injection in test classes");
        System.out.println("4. Service and repository testing");
        System.out.println("5. Bean interaction testing");
        System.out.println("6. Constructor injection in tests");
        System.out.println("7. Multiple configuration sources");
        System.out.println("8. ApplicationContext features in tests");
        System.out.println("\nRun the test classes to see the pattern in action.");
        System.out.println("====================================");
    }
}

/**
 * Configuration in application-test.properties:
 * 
 * # Test-specific properties
 * spring.test.context.cache.maxSize=32
 * logging.level.org.springframework.test=DEBUG
 * 
 * # Database configuration for tests
 * spring.datasource.url=jdbc:h2:mem:testdb
 * spring.datasource.driver-class-name=org.h2.Driver
 * spring.jpa.hibernate.ddl-auto=create-drop
 * 
 * # Disable specific features in tests
 * spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
 */
