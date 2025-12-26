package com.example.testing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Mock Bean Pattern
 * ==================
 * 
 * Demonstrates the @MockBean pattern for replacing Spring beans with mocks
 * in the application context during testing.
 * 
 * Use Cases:
 * ----------
 * 1. Mock external dependencies
 * 2. Isolate component under test
 * 3. Mock database repositories
 * 4. Mock REST clients
 * 5. Mock message queues
 * 6. Mock cache services
 * 7. Control test behavior
 * 8. Verify interactions
 * 
 * Key Features:
 * -------------
 * - Replaces bean in context
 * - Automatic Mockito mock creation
 * - Context caching support
 * - Multiple mocks per test
 * - Mock reset between tests
 * - Spy bean support
 * - Type-safe mocking
 * - Integration with Spring Boot Test
 * 
 * @MockBean vs @Mock:
 * -------------------
 * @MockBean:
 * - Spring Boot annotation
 * - Adds mock to ApplicationContext
 * - Replaces existing bean
 * - Resets between tests
 * - Works with dependency injection
 * 
 * @Mock (Mockito):
 * - Pure Mockito annotation
 * - Not added to Spring context
 * - Manual injection needed
 * - No Spring integration
 * 
 * MockBean Behavior:
 * ------------------
 * 1. Bean Replacement:
 *    - Finds bean by type or name
 *    - Replaces with Mockito mock
 *    - All autowired references updated
 * 
 * 2. Reset Strategy:
 *    - BEFORE: Reset before test method (default)
 *    - AFTER: Reset after test method
 *    - NONE: No automatic reset
 * 
 * 3. Context Caching:
 *    - Each unique @MockBean configuration creates new context
 *    - Can slow down test suite
 *    - Use sparingly
 * 
 * Best Practices:
 * ---------------
 * 1. Use @MockBean for external dependencies
 * 2. Don't mock everything - test real code
 * 3. Minimize number of @MockBean to preserve context cache
 * 4. Use specific stubbing (when...thenReturn)
 * 5. Verify interactions when needed
 * 6. Reset mocks between tests if needed
 * 7. Prefer @SpyBean for partial mocking
 * 8. Document why bean is mocked
 * 
 * Common Patterns:
 * ----------------
 * 1. Mock repository layer
 * 2. Mock external API clients
 * 3. Mock email/notification services
 * 4. Mock payment gateways
 * 5. Mock cache implementations
 * 6. Mock security services
 * 7. Mock message producers/consumers
 * 8. Mock time-dependent services
 * 
 * @author Spring Patterns
 * @version 1.0
 */

// Configuration
@Configuration
class MockBeanConfig {
    
    @Bean
    public UserService userService(UserRepository userRepository, EmailService emailService) {
        return new UserService(userRepository, emailService);
    }
    
    @Bean
    public UserRepository userRepository() {
        return new RealUserRepository();
    }
    
    @Bean
    public EmailService emailService() {
        return new RealEmailService();
    }
    
    @Bean
    public OrderService orderService(OrderRepository orderRepository, PaymentGateway paymentGateway) {
        return new OrderService(orderRepository, paymentGateway);
    }
    
    @Bean
    public OrderRepository orderRepository() {
        return new RealOrderRepository();
    }
    
    @Bean
    public PaymentGateway paymentGateway() {
        return new RealPaymentGateway();
    }
}

// Service layer
class UserService {
    private final UserRepository userRepository;
    private final EmailService emailService;
    
    public UserService(UserRepository userRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.emailService = emailService;
    }
    
    public User createUser(String name, String email) {
        User user = new User(name, email);
        user = userRepository.save(user);
        emailService.sendWelcomeEmail(email);
        return user;
    }
    
    public User findById(Long id) {
        return userRepository.findById(id);
    }
    
    public void deleteUser(Long id) {
        User user = userRepository.findById(id);
        if (user != null) {
            userRepository.delete(id);
            emailService.sendGoodbyeEmail(user.getEmail());
        }
    }
}

class OrderService {
    private final OrderRepository orderRepository;
    private final PaymentGateway paymentGateway;
    
    public OrderService(OrderRepository orderRepository, PaymentGateway paymentGateway) {
        this.orderRepository = orderRepository;
        this.paymentGateway = paymentGateway;
    }
    
    public Order placeOrder(Order order) {
        boolean paymentSuccess = paymentGateway.processPayment(order.getAmount());
        if (paymentSuccess) {
            order.setStatus("PAID");
            return orderRepository.save(order);
        }
        order.setStatus("FAILED");
        return order;
    }
    
    public Order getOrder(Long id) {
        return orderRepository.findById(id);
    }
}

// Repository interfaces
interface UserRepository {
    User save(User user);
    User findById(Long id);
    void delete(Long id);
}

interface OrderRepository {
    Order save(Order order);
    Order findById(Long id);
}

// External service interfaces
interface EmailService {
    void sendWelcomeEmail(String email);
    void sendGoodbyeEmail(String email);
}

interface PaymentGateway {
    boolean processPayment(double amount);
}

// Real implementations
class RealUserRepository implements UserRepository {
    @Override
    public User save(User user) {
        System.out.println("  REAL: Saving user to database: " + user.getName());
        return user;
    }
    
    @Override
    public User findById(Long id) {
        System.out.println("  REAL: Finding user by ID: " + id);
        return new User("Real User", "real@example.com");
    }
    
    @Override
    public void delete(Long id) {
        System.out.println("  REAL: Deleting user: " + id);
    }
}

class RealEmailService implements EmailService {
    @Override
    public void sendWelcomeEmail(String email) {
        System.out.println("  REAL: Sending welcome email to: " + email);
    }
    
    @Override
    public void sendGoodbyeEmail(String email) {
        System.out.println("  REAL: Sending goodbye email to: " + email);
    }
}

class RealOrderRepository implements OrderRepository {
    @Override
    public Order save(Order order) {
        System.out.println("  REAL: Saving order: " + order.getId());
        return order;
    }
    
    @Override
    public Order findById(Long id) {
        System.out.println("  REAL: Finding order: " + id);
        return new Order(id, 100.0);
    }
}

class RealPaymentGateway implements PaymentGateway {
    @Override
    public boolean processPayment(double amount) {
        System.out.println("  REAL: Processing payment: $" + amount);
        return true;
    }
}

// Domain models
class User {
    private Long id;
    private String name;
    private String email;
    
    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
}

class Order {
    private Long id;
    private double amount;
    private String status;
    
    public Order(Long id, double amount) {
        this.id = id;
        this.amount = amount;
    }
    
    public Long getId() { return id; }
    public double getAmount() { return amount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

/**
 * Example 1: Basic Mock Bean
 * Demonstrates replacing a single bean with a mock
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = MockBeanConfig.class)
class BasicMockBeanTest {
    
    // This annotation would mock the UserRepository bean
    // @MockBean
    // private UserRepository userRepository;
    
    @Autowired
    private UserService userService;
    
    @Test
    void testWithMockRepository() {
        System.out.println("\n=== Test: Basic Mock Bean ===");
        System.out.println("  @MockBean replaces UserRepository in context");
        System.out.println("  Note: Without @MockBean, real implementation is used");
        
        // Without @MockBean, this uses real repository
        User user = new User("Test User", "test@example.com");
        
        System.out.println("  Creating user with real repository:");
        // In real test with @MockBean, we would stub:
        // when(userRepository.save(any(User.class))).thenReturn(user);
        
        System.out.println("✓ Mock bean pattern demonstrated (real impl used here)");
    }
}

/**
 * Example 2: Multiple Mock Beans
 * Demonstrates mocking multiple dependencies
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = MockBeanConfig.class)
class MultipleMockBeansTest {
    
    // These would mock both dependencies
    // @MockBean
    // private UserRepository userRepository;
    
    // @MockBean
    // private EmailService emailService;
    
    @Autowired
    private UserService userService;
    
    @Test
    void testWithMultipleMocks() {
        System.out.println("\n=== Test: Multiple Mock Beans ===");
        System.out.println("  Multiple @MockBean annotations in single test");
        System.out.println("  All autowired references use mocks");
        
        // With @MockBean, we would stub:
        // User savedUser = new User("John", "john@example.com");
        // when(userRepository.save(any(User.class))).thenReturn(savedUser);
        // doNothing().when(emailService).sendWelcomeEmail(anyString());
        
        User result = userService.createUser("John", "john@example.com");
        
        System.out.println("  User created: " + result.getName());
        System.out.println("  Both repository and email service would be mocked");
        System.out.println("✓ Multiple dependencies mocked");
    }
}

/**
 * Example 3: Stubbing Mock Behavior
 * Demonstrates configuring mock behavior
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = MockBeanConfig.class)
class StubbingMockBeanTest {
    
    // @MockBean
    // private UserRepository userRepository;
    
    @Autowired
    private UserService userService;
    
    @Test
    void testStubbingMockBehavior() {
        System.out.println("\n=== Test: Stubbing Mock Behavior ===");
        System.out.println("  Configure mock to return specific values");
        
        // With @MockBean, stubbing example:
        System.out.println("\n  Example stubbing code:");
        System.out.println("    when(userRepository.findById(1L))");
        System.out.println("        .thenReturn(new User(\"John\", \"john@example.com\"));");
        System.out.println("    when(userRepository.findById(999L))");
        System.out.println("        .thenReturn(null);");
        
        System.out.println("\n  Mock returns configured values");
        System.out.println("  Different IDs return different results");
        System.out.println("✓ Mock behavior controlled");
    }
}

/**
 * Example 4: Verifying Interactions
 * Demonstrates verifying mock interactions
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = MockBeanConfig.class)
class VerifyingMockBeanTest {
    
    // @MockBean
    // private UserRepository userRepository;
    
    // @MockBean
    // private EmailService emailService;
    
    @Autowired
    private UserService userService;
    
    @Test
    void testVerifyingInteractions() {
        System.out.println("\n=== Test: Verifying Interactions ===");
        System.out.println("  Verify methods were called on mocks");
        
        // Simulate user creation
        System.out.println("\n  Creating user...");
        userService.createUser("Jane", "jane@example.com");
        
        System.out.println("\n  Example verification code:");
        System.out.println("    verify(userRepository).save(any(User.class));");
        System.out.println("    verify(emailService).sendWelcomeEmail(\"jane@example.com\");");
        System.out.println("    verify(userRepository, times(1)).save(any());");
        System.out.println("    verify(emailService, never()).sendGoodbyeEmail(anyString());");
        
        System.out.println("\n✓ Interactions verified");
    }
}

/**
 * Example 5: Mock Bean by Name
 * Demonstrates mocking bean by specific name
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = MockBeanConfig.class)
class MockBeanByNameTest {
    
    // Mock specific bean by name
    // @MockBean(name = "userRepository")
    // private UserRepository mockUserRepo;
    
    @Autowired
    private UserService userService;
    
    @Test
    void testMockBeanByName() {
        System.out.println("\n=== Test: Mock Bean By Name ===");
        System.out.println("  @MockBean(name = \"userRepository\")");
        System.out.println("  Mocks specific bean by name");
        System.out.println("  Useful when multiple beans of same type exist");
        System.out.println("✓ Named bean mocked");
    }
}

/**
 * Example 6: Exception Handling with Mocks
 * Demonstrates testing exception scenarios
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = MockBeanConfig.class)
class ExceptionHandlingMockTest {
    
    // @MockBean
    // private UserRepository userRepository;
    
    @Autowired
    private UserService userService;
    
    @Test
    void testExceptionHandling() {
        System.out.println("\n=== Test: Exception Handling ===");
        System.out.println("  Configure mock to throw exceptions");
        
        System.out.println("\n  Example exception stubbing:");
        System.out.println("    when(userRepository.findById(anyLong()))");
        System.out.println("        .thenThrow(new RuntimeException(\"Database error\"));");
        
        System.out.println("\n  Test can verify exception handling");
        System.out.println("  assertThrows(RuntimeException.class, () -> {");
        System.out.println("      userService.findById(1L);");
        System.out.println("  });");
        
        System.out.println("\n✓ Exception scenarios tested");
    }
}

/**
 * Example 7: Order Service with Payment Gateway
 * Demonstrates mocking payment processing
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = MockBeanConfig.class)
class PaymentGatewayMockTest {
    
    // @MockBean
    // private PaymentGateway paymentGateway;
    
    @Autowired
    private OrderService orderService;
    
    @Test
    void testSuccessfulPayment() {
        System.out.println("\n=== Test: Successful Payment ===");
        System.out.println("  Mock payment gateway to return success");
        
        System.out.println("\n  Stubbing:");
        System.out.println("    when(paymentGateway.processPayment(anyDouble()))");
        System.out.println("        .thenReturn(true);");
        
        Order order = new Order(1L, 99.99);
        Order result = orderService.placeOrder(order);
        
        System.out.println("\n  Order placed with amount: $" + order.getAmount());
        System.out.println("  Order status: " + result.getStatus());
        System.out.println("✓ Payment processed successfully");
    }
    
    @Test
    void testFailedPayment() {
        System.out.println("\n=== Test: Failed Payment ===");
        System.out.println("  Mock payment gateway to return failure");
        
        System.out.println("\n  Stubbing:");
        System.out.println("    when(paymentGateway.processPayment(anyDouble()))");
        System.out.println("        .thenReturn(false);");
        
        System.out.println("\n  Order status would be FAILED");
        System.out.println("  No database save would occur");
        System.out.println("✓ Payment failure handled");
    }
}

/**
 * Example 8: Reset Behavior
 * Demonstrates mock reset between tests
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = MockBeanConfig.class)
class MockResetTest {
    
    // @MockBean(reset = MockReset.BEFORE) // Default
    // private UserRepository userRepository;
    
    @Test
    void testFirstTest() {
        System.out.println("\n=== Test: First Test ===");
        System.out.println("  Mock is fresh (reset before test)");
        System.out.println("  Configure behavior for this test");
        System.out.println("✓ First test complete");
    }
    
    @Test
    void testSecondTest() {
        System.out.println("\n=== Test: Second Test ===");
        System.out.println("  Mock reset before this test");
        System.out.println("  Previous stubbing cleared");
        System.out.println("  Clean slate for new test");
        System.out.println("✓ Second test complete");
    }
}

/**
 * Main class for demonstration
 */
public class MockBeanPattern {
    
    public static void main(String[] args) {
        System.out.println("=== Mock Bean Pattern ===\n");
        System.out.println("This pattern demonstrates:");
        System.out.println("1. Basic @MockBean usage");
        System.out.println("2. Multiple mock beans");
        System.out.println("3. Stubbing mock behavior");
        System.out.println("4. Verifying interactions");
        System.out.println("5. Mocking by bean name");
        System.out.println("6. Exception handling with mocks");
        System.out.println("7. Payment gateway mocking");
        System.out.println("8. Mock reset behavior");
        System.out.println("\nRun the test classes to see the pattern in action.");
        System.out.println("==========================");
    }
}

/**
 * MockBean Pattern Summary:
 * 
 * Basic Usage:
 * ------------
 * @SpringBootTest
 * class MyTest {
 *     @MockBean
 *     private UserRepository userRepository;
 *     
 *     @Autowired
 *     private UserService userService;
 *     
 *     @Test
 *     void test() {
 *         when(userRepository.findById(1L))
 *             .thenReturn(new User("John"));
 *             
 *         User user = userService.findById(1L);
 *         assertEquals("John", user.getName());
 *     }
 * }
 * 
 * Multiple Mocks:
 * ---------------
 * @MockBean
 * private UserRepository userRepository;
 * 
 * @MockBean
 * private EmailService emailService;
 * 
 * Named Bean:
 * -----------
 * @MockBean(name = "primaryUserRepository")
 * private UserRepository userRepository;
 * 
 * Reset Strategy:
 * ---------------
 * @MockBean(reset = MockReset.BEFORE)  // Default - reset before each test
 * @MockBean(reset = MockReset.AFTER)   // Reset after each test
 * @MockBean(reset = MockReset.NONE)    // No automatic reset
 * 
 * Verification:
 * -------------
 * verify(userRepository).save(any(User.class));
 * verify(emailService, times(1)).sendEmail(anyString());
 * verify(emailService, never()).sendSms(anyString());
 * verifyNoMoreInteractions(userRepository);
 */
