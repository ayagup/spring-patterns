package com.example.testing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.mockito.Mockito.*;

/**
 * Spy Bean Pattern
 * =================
 * 
 * Demonstrates the @SpyBean pattern for partial mocking - using a real
 * bean instance but with the ability to stub specific methods.
 * 
 * Use Cases:
 * ----------
 * 1. Partial mocking of beans
 * 2. Test real implementation with selective overrides
 * 3. Verify method calls on real objects
 * 4. Mock only certain methods
 * 5. Test integration with controlled dependencies
 * 6. Debug complex interactions
 * 7. Gradual test migration
 * 8. Performance-critical testing
 * 
 * Key Features:
 * -------------
 * - Real object with mock capabilities
 * - Selective method stubbing
 * - Call real methods by default
 * - Verify interactions
 * - Combine real and mocked behavior
 * - Type-safe spying
 * - Spring context integration
 * - Reset between tests
 * 
 * @SpyBean vs @MockBean:
 * ----------------------
 * @SpyBean:
 * - Wraps real bean instance
 * - Calls real methods unless stubbed
 * - Partial mocking
 * - Real object behavior preserved
 * - Use when most behavior is real
 * 
 * @MockBean:
 * - Creates pure mock
 * - No real methods called
 * - All methods must be stubbed
 * - Complete isolation
 * - Use when full control needed
 * 
 * @SpyBean vs Mockito @Spy:
 * -------------------------
 * @SpyBean:
 * - Spring Boot annotation
 * - Wraps bean in ApplicationContext
 * - All autowired references updated
 * - Reset between tests
 * 
 * @Spy (Mockito):
 * - Pure Mockito annotation
 * - Not added to Spring context
 * - Manual injection needed
 * - No Spring integration
 * 
 * When to Use Spy:
 * ----------------
 * 1. Most methods work correctly
 * 2. Need to stub one or two methods
 * 3. Want to verify calls on real object
 * 4. Testing integration scenarios
 * 5. Debugging complex interactions
 * 6. Can't easily mock all dependencies
 * 7. Want realistic behavior with control
 * 8. Gradual test migration
 * 
 * Best Practices:
 * ---------------
 * 1. Prefer real objects over spies
 * 2. Use spy only when necessary
 * 3. Don't spy on too many beans
 * 4. Document why spy is needed
 * 5. Verify real method calls
 * 6. Be careful with void methods
 * 7. Reset spy state between tests
 * 8. Consider splitting complex beans
 * 
 * Common Patterns:
 * ----------------
 * 1. Spy on service layer
 * 2. Spy on cache implementations
 * 3. Verify method call counts
 * 4. Override expensive operations
 * 5. Test retry logic
 * 6. Mock external API calls only
 * 7. Verify internal method calls
 * 8. Test decorator patterns
 * 
 * @author Spring Patterns
 * @version 1.0
 */

// Configuration
@Configuration
class SpyBeanConfig {
    
    @Bean
    public NotificationService notificationService() {
        return new NotificationService();
    }
    
    @Bean
    public UserProfileService userProfileService() {
        return new UserProfileService();
    }
    
    @Bean
    public CacheService cacheService() {
        return new CacheService();
    }
    
    @Bean
    public AuditService auditService() {
        return new AuditService();
    }
}

// Services with real implementations
class NotificationService {
    
    public void sendEmail(String to, String message) {
        System.out.println("  REAL: Sending email to " + to + ": " + message);
        // In production, would actually send email
    }
    
    public void sendSms(String phone, String message) {
        System.out.println("  REAL: Sending SMS to " + phone + ": " + message);
        // In production, would actually send SMS
    }
    
    public void sendPushNotification(String userId, String message) {
        System.out.println("  REAL: Sending push notification to " + userId + ": " + message);
        // In production, would actually send push notification
    }
    
    public int getNotificationCount() {
        System.out.println("  REAL: Getting notification count from database");
        return 42; // In production, would query database
    }
}

class UserProfileService {
    
    public UserProfile loadProfile(String userId) {
        System.out.println("  REAL: Loading profile from database for user: " + userId);
        return new UserProfile(userId, "Real User", "real@example.com");
    }
    
    public void saveProfile(UserProfile profile) {
        System.out.println("  REAL: Saving profile to database: " + profile.getName());
        // In production, would save to database
    }
    
    public boolean validateProfile(UserProfile profile) {
        System.out.println("  REAL: Validating profile: " + profile.getName());
        return profile.getName() != null && profile.getEmail() != null;
    }
    
    public String generateProfileUrl(String userId) {
        System.out.println("  REAL: Generating profile URL for: " + userId);
        return "https://example.com/users/" + userId;
    }
}

class CacheService {
    
    public Object get(String key) {
        System.out.println("  REAL: Getting from cache: " + key);
        return null; // In production, would check cache
    }
    
    public void put(String key, Object value) {
        System.out.println("  REAL: Putting in cache: " + key);
        // In production, would store in cache
    }
    
    public void evict(String key) {
        System.out.println("  REAL: Evicting from cache: " + key);
        // In production, would remove from cache
    }
    
    public void clear() {
        System.out.println("  REAL: Clearing entire cache");
        // In production, would clear cache
    }
}

class AuditService {
    private int auditCount = 0;
    
    public void logAction(String action, String userId) {
        auditCount++;
        System.out.println("  REAL: Audit log [" + auditCount + "]: " + userId + " - " + action);
        // In production, would log to audit system
    }
    
    public void logError(String error) {
        auditCount++;
        System.out.println("  REAL: Error log [" + auditCount + "]: " + error);
        // In production, would log error
    }
    
    public int getAuditCount() {
        return auditCount;
    }
}

// Domain model
class UserProfile {
    private String userId;
    private String name;
    private String email;
    
    public UserProfile(String userId, String name, String email) {
        this.userId = userId;
        this.name = name;
        this.email = email;
    }
    
    public String getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
}

/**
 * Example 1: Basic Spy Bean
 * Demonstrates creating a spy on a real bean
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = SpyBeanConfig.class)
class BasicSpyBeanTest {
    
    // @SpyBean
    // private NotificationService notificationService;
    
    @Autowired
    private NotificationService notificationService;
    
    @Test
    void testBasicSpy() {
        System.out.println("\n=== Test: Basic Spy Bean ===");
        System.out.println("  @SpyBean wraps real bean instance");
        System.out.println("  Real methods are called unless stubbed");
        
        // Without @SpyBean, this calls real method
        notificationService.sendEmail("test@example.com", "Hello");
        
        System.out.println("\n  With @SpyBean, could verify:");
        System.out.println("    verify(notificationService).sendEmail(anyString(), anyString());");
        
        System.out.println("✓ Real method called and verifiable");
    }
}

/**
 * Example 2: Partial Stubbing
 * Demonstrates stubbing only specific methods
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = SpyBeanConfig.class)
class PartialStubbingTest {
    
    // @SpyBean
    // private NotificationService notificationService;
    
    @Autowired
    private NotificationService notificationService;
    
    @Test
    void testPartialStubbing() {
        System.out.println("\n=== Test: Partial Stubbing ===");
        System.out.println("  Stub expensive methods, keep others real");
        
        System.out.println("\n  Stubbing example:");
        System.out.println("    doNothing().when(notificationService).sendEmail(anyString(), anyString());");
        System.out.println("  This prevents actual email sending");
        
        System.out.println("\n  Real method calls:");
        notificationService.sendSms("123-456-7890", "Test SMS");
        notificationService.sendPushNotification("user123", "Test Push");
        
        System.out.println("\n  These use real implementation");
        System.out.println("  Only sendEmail() would be stubbed");
        System.out.println("✓ Partial stubbing demonstrated");
    }
}

/**
 * Example 3: Verify Real Method Calls
 * Demonstrates verifying calls on spied bean
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = SpyBeanConfig.class)
class VerifyRealMethodCallsTest {
    
    // @SpyBean
    // private UserProfileService userProfileService;
    
    @Autowired
    private UserProfileService userProfileService;
    
    @Test
    void testVerifyRealCalls() {
        System.out.println("\n=== Test: Verify Real Method Calls ===");
        System.out.println("  Verify that real methods were called");
        
        UserProfile profile = userProfileService.loadProfile("user123");
        boolean isValid = userProfileService.validateProfile(profile);
        
        System.out.println("\n  Profile loaded: " + profile.getName());
        System.out.println("  Validation result: " + isValid);
        
        System.out.println("\n  Verification example:");
        System.out.println("    verify(userProfileService).loadProfile(\"user123\");");
        System.out.println("    verify(userProfileService).validateProfile(any());");
        System.out.println("    verify(userProfileService, times(1)).loadProfile(anyString());");
        
        System.out.println("✓ Real method calls verified");
    }
}

/**
 * Example 4: Stubbing Return Values
 * Demonstrates overriding return values while keeping logic
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = SpyBeanConfig.class)
class StubbingReturnValuesTest {
    
    // @SpyBean
    // private CacheService cacheService;
    
    @Autowired
    private CacheService cacheService;
    
    @Test
    void testStubbingReturnValues() {
        System.out.println("\n=== Test: Stubbing Return Values ===");
        System.out.println("  Override specific method return values");
        
        System.out.println("\n  Stubbing example:");
        System.out.println("    when(cacheService.get(\"user:123\"))");
        System.out.println("        .thenReturn(new User(\"Cached User\"));");
        
        // Simulate cache lookup (real method)
        Object result = cacheService.get("user:123");
        System.out.println("\n  Cache result: " + result);
        
        System.out.println("\n  Other methods still work normally:");
        cacheService.put("key", "value");
        cacheService.evict("key");
        
        System.out.println("✓ Return value stubbed, other methods real");
    }
}

/**
 * Example 5: Void Method Stubbing
 * Demonstrates stubbing void methods on spy
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = SpyBeanConfig.class)
class VoidMethodStubbingTest {
    
    // @SpyBean
    // private NotificationService notificationService;
    
    @Autowired
    private NotificationService notificationService;
    
    @Test
    void testVoidMethodStubbing() {
        System.out.println("\n=== Test: Void Method Stubbing ===");
        System.out.println("  Stub void methods to prevent execution");
        
        System.out.println("\n  Stubbing void methods:");
        System.out.println("    doNothing().when(notificationService)");
        System.out.println("        .sendEmail(anyString(), anyString());");
        System.out.println("    doThrow(new RuntimeException(\"SMS failed\"))");
        System.out.println("        .when(notificationService)");
        System.out.println("        .sendSms(anyString(), anyString());");
        
        System.out.println("\n  sendEmail() would do nothing");
        System.out.println("  sendSms() would throw exception");
        System.out.println("  Other methods execute normally");
        
        notificationService.sendPushNotification("user123", "Test");
        
        System.out.println("✓ Void method stubbing demonstrated");
    }
}

/**
 * Example 6: Multiple Spies
 * Demonstrates using multiple spy beans
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = SpyBeanConfig.class)
class MultipleSpiesTest {
    
    // @SpyBean
    // private NotificationService notificationService;
    
    // @SpyBean
    // private AuditService auditService;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private AuditService auditService;
    
    @Test
    void testMultipleSpies() {
        System.out.println("\n=== Test: Multiple Spies ===");
        System.out.println("  Multiple @SpyBean annotations in one test");
        
        notificationService.sendEmail("test@example.com", "Message");
        auditService.logAction("SEND_EMAIL", "user123");
        
        System.out.println("\n  Both services use real implementations");
        System.out.println("  Both can be verified:");
        System.out.println("    verify(notificationService).sendEmail(...);");
        System.out.println("    verify(auditService).logAction(...);");
        
        System.out.println("✓ Multiple spies working together");
    }
}

/**
 * Example 7: Spy with Call Count Verification
 * Demonstrates verifying how many times methods were called
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = SpyBeanConfig.class)
class CallCountVerificationTest {
    
    // @SpyBean
    // private AuditService auditService;
    
    @Autowired
    private AuditService auditService;
    
    @Test
    void testCallCountVerification() {
        System.out.println("\n=== Test: Call Count Verification ===");
        System.out.println("  Verify exact number of method calls");
        
        auditService.logAction("ACTION_1", "user123");
        auditService.logAction("ACTION_2", "user123");
        auditService.logAction("ACTION_3", "user123");
        
        int count = auditService.getAuditCount();
        System.out.println("\n  Total audit logs: " + count);
        
        System.out.println("\n  Verification examples:");
        System.out.println("    verify(auditService, times(3)).logAction(anyString(), eq(\"user123\"));");
        System.out.println("    verify(auditService, atLeast(2)).logAction(anyString(), anyString());");
        System.out.println("    verify(auditService, atMost(5)).logAction(anyString(), anyString());");
        System.out.println("    verify(auditService, never()).logError(anyString());");
        
        System.out.println("✓ Call counts verified");
    }
}

/**
 * Example 8: Spy vs Mock Comparison
 * Demonstrates differences between spy and mock
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = SpyBeanConfig.class)
class SpyVsMockComparisonTest {
    
    @Autowired
    private NotificationService notificationService;
    
    @Test
    void testSpyVsMockComparison() {
        System.out.println("\n=== Test: Spy vs Mock Comparison ===");
        
        System.out.println("\n@SpyBean behavior:");
        System.out.println("  - Wraps real NotificationService instance");
        System.out.println("  - Methods execute real code unless stubbed");
        System.out.println("  - Can verify real method calls");
        System.out.println("  - Useful for partial mocking");
        
        System.out.println("\n@MockBean behavior:");
        System.out.println("  - Creates pure mock of NotificationService");
        System.out.println("  - No real code executes");
        System.out.println("  - All methods return null unless stubbed");
        System.out.println("  - Complete isolation");
        
        System.out.println("\nWhen to use @SpyBean:");
        System.out.println("  ✓ Most methods work correctly");
        System.out.println("  ✓ Need to stub one or two expensive methods");
        System.out.println("  ✓ Want realistic behavior with control");
        
        System.out.println("\nWhen to use @MockBean:");
        System.out.println("  ✓ Need complete control over bean");
        System.out.println("  ✓ Want total isolation");
        System.out.println("  ✓ Testing error conditions");
        
        System.out.println("✓ Comparison complete");
    }
}

/**
 * Main class for demonstration
 */
public class SpyBeanPattern {
    
    public static void main(String[] args) {
        System.out.println("=== Spy Bean Pattern ===\n");
        System.out.println("This pattern demonstrates:");
        System.out.println("1. Basic spy bean usage");
        System.out.println("2. Partial method stubbing");
        System.out.println("3. Verifying real method calls");
        System.out.println("4. Stubbing return values");
        System.out.println("5. Void method stubbing");
        System.out.println("6. Multiple spy beans");
        System.out.println("7. Call count verification");
        System.out.println("8. Spy vs mock comparison");
        System.out.println("\nRun the test classes to see the pattern in action.");
        System.out.println("==========================");
    }
}

/**
 * SpyBean Pattern Summary:
 * 
 * Basic Usage:
 * ------------
 * @SpringBootTest
 * class MyTest {
 *     @SpyBean
 *     private NotificationService notificationService;
 *     
 *     @Test
 *     void test() {
 *         // Stub expensive method
 *         doNothing().when(notificationService).sendEmail(anyString(), anyString());
 *         
 *         // Call real method
 *         notificationService.sendSms("123", "Test");
 *         
 *         // Verify calls
 *         verify(notificationService).sendSms("123", "Test");
 *     }
 * }
 * 
 * Stubbing Void Methods:
 * ----------------------
 * doNothing().when(service).voidMethod();
 * doThrow(new RuntimeException()).when(service).voidMethod();
 * doAnswer(invocation -> {
 *     // Custom logic
 *     return null;
 * }).when(service).voidMethod();
 * 
 * Stubbing Return Values:
 * -----------------------
 * when(service.method()).thenReturn(value);
 * when(service.method()).thenThrow(new RuntimeException());
 * 
 * Verification:
 * -------------
 * verify(service).method();
 * verify(service, times(3)).method();
 * verify(service, atLeast(1)).method();
 * verify(service, atMost(5)).method();
 * verify(service, never()).method();
 * verifyNoMoreInteractions(service);
 * 
 * Reset Spy:
 * ----------
 * reset(service);  // Clear all stubbing and invocations
 */
