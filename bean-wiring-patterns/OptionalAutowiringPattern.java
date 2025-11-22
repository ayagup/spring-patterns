package com.spring.patterns.wiring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/**
 * Optional Autowiring Pattern
 * 
 * Spring supports optional dependencies that may or may not be present:
 * 1. Optional<T> - Java 8 Optional wrapper
 * 2. @Autowired(required = false) - Bean can be null
 * 3. @Nullable - Parameter can be null
 * 
 * Characteristics:
 * - Dependency is NOT required
 * - Application starts even if bean missing
 * - Prevents NoSuchBeanDefinitionException
 * - Graceful degradation
 * - Feature toggles
 * 
 * Approaches:
 * 1. Optional<T> - Best for Java 8+
 * 2. @Autowired(required = false) - Field/setter can be null
 * 3. @Nullable - Parameter can be null
 * 4. ObjectProvider<T> - Lazy optional resolution
 * 
 * Use Cases:
 * - Feature flags/toggles
 * - Optional integrations
 * - Backward compatibility
 * - Conditional features
 * - Graceful degradation
 */
@SpringBootApplication
public class OptionalAutowiringPattern {

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(OptionalAutowiringPattern.class, args);
        
        System.out.println("\n=== Optional Autowiring Pattern ===");
        
        // Demonstrate optional dependencies
        UserService userService = context.getBean(UserService.class);
        userService.createUser("john@example.com", "John Doe");
        
        // Demonstrate feature service
        FeatureService featureService = context.getBean(FeatureService.class);
        featureService.executeFeature();
        
        // Demonstrate notification service
        NotificationManager manager = context.getBean(NotificationManager.class);
        manager.sendNotification("Test notification");
    }
}

/**
 * Configuration
 */
@Configuration
class OptionalWiringConfig {
    
    // Email service is available
    @Bean
    public EmailService emailService() {
        return new EmailService();
    }
    
    // Analytics service is available
    @Bean
    public AnalyticsService analyticsService() {
        return new AnalyticsService();
    }
    
    // Note: SmsService is NOT defined - demonstrates optional dependency
    // Note: PushService is NOT defined - demonstrates optional dependency
}

/**
 * Email Service (Available)
 */
class EmailService {
    
    public EmailService() {
        System.out.println("EmailService created");
    }
    
    public void sendEmail(String to, String message) {
        System.out.println("[Email] Sent to " + to + ": " + message);
    }
}

/**
 * SMS Service interface (Implementation NOT available)
 */
interface SmsService {
    void sendSms(String to, String message);
}

/**
 * Push Service interface (Implementation NOT available)
 */
interface PushService {
    void sendPush(String userId, String message);
}

/**
 * Analytics Service (Available)
 */
class AnalyticsService {
    
    public AnalyticsService() {
        System.out.println("AnalyticsService created");
    }
    
    public void trackEvent(String event) {
        System.out.println("[Analytics] Event tracked: " + event);
    }
}

/**
 * Cache Service interface (Implementation NOT available)
 */
interface CacheService {
    void put(String key, Object value);
    Object get(String key);
}

/**
 * Example 1: Optional<T> - Recommended Approach
 */
@Service
class UserService {
    
    private final EmailService emailService; // Required (always present)
    private final Optional<SmsService> smsService; // Optional (may not be present)
    private final Optional<PushService> pushService; // Optional
    private final Optional<AnalyticsService> analyticsService; // Optional
    
    @Autowired
    public UserService(EmailService emailService,
                      Optional<SmsService> smsService,
                      Optional<PushService> pushService,
                      Optional<AnalyticsService> analyticsService) {
        this.emailService = emailService;
        this.smsService = smsService;
        this.pushService = pushService;
        this.analyticsService = analyticsService;
        
        System.out.println("\nUserService created:");
        System.out.println("  - EmailService: available");
        System.out.println("  - SmsService: " + (smsService.isPresent() ? "available" : "NOT available"));
        System.out.println("  - PushService: " + (pushService.isPresent() ? "available" : "NOT available"));
        System.out.println("  - AnalyticsService: " + (analyticsService.isPresent() ? "available" : "NOT available"));
    }
    
    public String createUser(String email, String name) {
        String userId = "USER-" + System.currentTimeMillis();
        
        // Required service - always called
        emailService.sendEmail(email, "Welcome " + name);
        
        // Optional services - called only if present
        smsService.ifPresent(service -> 
            service.sendSms(email, "Welcome via SMS")
        );
        
        pushService.ifPresent(service -> 
            service.sendPush(userId, "Welcome push notification")
        );
        
        analyticsService.ifPresent(service -> 
            service.trackEvent("USER_CREATED:" + userId)
        );
        
        return userId;
    }
}

/**
 * Example 2: @Autowired(required = false)
 */
@Service
class OrderService {
    
    private final EmailService emailService;
    
    // Optional dependency - can be null
    @Autowired(required = false)
    private CacheService cacheService;
    
    @Autowired(required = false)
    private SmsService smsService;
    
    @Autowired
    public OrderService(EmailService emailService) {
        this.emailService = emailService;
        System.out.println("\nOrderService created:");
        System.out.println("  - CacheService: " + (cacheService != null ? "available" : "NOT available"));
        System.out.println("  - SmsService: " + (smsService != null ? "available" : "NOT available"));
    }
    
    public String createOrder(String userId, String product) {
        String orderId = "ORDER-" + System.currentTimeMillis();
        
        emailService.sendEmail(userId, "Order " + orderId + " created");
        
        // Use cache if available
        if (cacheService != null) {
            cacheService.put("order:" + orderId, product);
            System.out.println("Order cached");
        } else {
            System.out.println("Cache not available - skipping");
        }
        
        // Send SMS if available
        if (smsService != null) {
            smsService.sendSms(userId, "Order " + orderId + " confirmed");
        } else {
            System.out.println("SMS not available - skipping");
        }
        
        return orderId;
    }
}

/**
 * Example 3: @Nullable Parameter
 */
@Service
class PaymentService {
    
    private final EmailService emailService;
    private final AnalyticsService analyticsService;
    private final SmsService smsService; // Can be null
    
    @Autowired
    public PaymentService(EmailService emailService,
                         AnalyticsService analyticsService,
                         @Nullable SmsService smsService) {
        this.emailService = emailService;
        this.analyticsService = analyticsService;
        this.smsService = smsService;
        
        System.out.println("\nPaymentService created:");
        System.out.println("  - SmsService: " + (smsService != null ? "available" : "NULL"));
    }
    
    public String processPayment(String userId, double amount) {
        String paymentId = "PAY-" + System.currentTimeMillis();
        
        emailService.sendEmail(userId, "Payment processed: $" + amount);
        analyticsService.trackEvent("PAYMENT_PROCESSED:" + paymentId);
        
        if (smsService != null) {
            smsService.sendSms(userId, "Payment confirmed: $" + amount);
        }
        
        return paymentId;
    }
}

/**
 * Example 4: Optional with Fallback Behavior
 */
@Service
class NotificationManager {
    
    private final Optional<SmsService> smsService;
    private final Optional<PushService> pushService;
    private final EmailService fallbackService; // Always available
    
    @Autowired
    public NotificationManager(Optional<SmsService> smsService,
                              Optional<PushService> pushService,
                              EmailService fallbackService) {
        this.smsService = smsService;
        this.pushService = pushService;
        this.fallbackService = fallbackService;
        System.out.println("\nNotificationManager created with fallback");
    }
    
    public void sendNotification(String message) {
        System.out.println("\nSending notification:");
        
        boolean sent = false;
        
        // Try SMS first
        if (smsService.isPresent()) {
            smsService.get().sendSms("user", message);
            sent = true;
        }
        
        // Try Push
        if (pushService.isPresent()) {
            pushService.get().sendPush("user", message);
            sent = true;
        }
        
        // Fallback to email if no other channel available
        if (!sent) {
            System.out.println("Using fallback channel (Email)");
            fallbackService.sendEmail("user@example.com", message);
        }
    }
}

/**
 * Example 5: Feature Toggles with Optional Dependencies
 */
@Component
class FeatureFlag {
    
    public boolean isEnabled(String feature) {
        // Simulate feature flag check
        return false; // Features disabled
    }
}

@Service
class FeatureService {
    
    private final FeatureFlag featureFlag;
    private final Optional<CacheService> cacheService;
    private final Optional<AnalyticsService> analyticsService;
    
    @Autowired
    public FeatureService(FeatureFlag featureFlag,
                         Optional<CacheService> cacheService,
                         Optional<AnalyticsService> analyticsService) {
        this.featureFlag = featureFlag;
        this.cacheService = cacheService;
        this.analyticsService = analyticsService;
        System.out.println("\nFeatureService created");
    }
    
    public void executeFeature() {
        System.out.println("\nExecuting feature:");
        
        // Caching feature
        if (featureFlag.isEnabled("caching") && cacheService.isPresent()) {
            System.out.println("Caching enabled and available");
            cacheService.get().put("key", "value");
        } else {
            System.out.println("Caching disabled or unavailable");
        }
        
        // Analytics feature
        if (featureFlag.isEnabled("analytics")) {
            analyticsService.ifPresent(service -> {
                System.out.println("Analytics enabled");
                service.trackEvent("FEATURE_EXECUTED");
            });
        } else {
            System.out.println("Analytics disabled");
        }
    }
}

/**
 * Example 6: Optional with Default Values
 */
@Service
class ConfigurationService {
    
    private final String defaultValue = "default-config";
    private final Optional<CacheService> cacheService;
    
    @Autowired
    public ConfigurationService(Optional<CacheService> cacheService) {
        this.cacheService = cacheService;
    }
    
    public String getConfig(String key) {
        // Try cache first
        String cached = cacheService
            .map(cache -> (String) cache.get(key))
            .orElse(null);
        
        if (cached != null) {
            return cached;
        }
        
        // Return default
        return defaultValue;
    }
    
    public void setConfig(String key, String value) {
        // Save to cache if available
        cacheService.ifPresentOrElse(
            cache -> {
                cache.put(key, value);
                System.out.println("Config cached: " + key);
            },
            () -> System.out.println("Cache unavailable - config not persisted")
        );
    }
}

/**
 * REST Controller
 */
@RestController
@RequestMapping("/api/optional-wiring")
class OptionalWiringController {
    
    private final UserService userService;
    private final OrderService orderService;
    private final PaymentService paymentService;
    private final NotificationManager notificationManager;
    
    public OptionalWiringController(UserService userService,
                                   OrderService orderService,
                                   PaymentService paymentService,
                                   NotificationManager notificationManager) {
        this.userService = userService;
        this.orderService = orderService;
        this.paymentService = paymentService;
        this.notificationManager = notificationManager;
    }
    
    @GetMapping("/user")
    public String createUser() {
        return userService.createUser("test@example.com", "Test User");
    }
    
    @GetMapping("/order")
    public String createOrder() {
        return orderService.createOrder("USER-123", "Laptop");
    }
    
    @GetMapping("/payment")
    public String processPayment() {
        return paymentService.processPayment("USER-123", 99.99);
    }
    
    @GetMapping("/notify")
    public String sendNotification() {
        notificationManager.sendNotification("Test notification");
        return "Notification sent";
    }
}

/**
 * Key Points:
 * 
 * 1. Optional<T> Approach (Recommended):
 *    @Autowired
 *    public Service(Optional<Dependency> dep) {
 *        this.dep = dep;
 *        dep.ifPresent(d -> d.doSomething());
 *    }
 * 
 * 2. required = false Approach:
 *    @Autowired(required = false)
 *    private Dependency dep; // Can be null
 *    
 *    if (dep != null) {
 *        dep.doSomething();
 *    }
 * 
 * 3. @Nullable Approach:
 *    @Autowired
 *    public Service(@Nullable Dependency dep) {
 *        this.dep = dep; // Can be null
 *    }
 * 
 * 4. Comparison:
 *    
 *    Optional<T>:
 *    ✓ Null-safe
 *    ✓ Functional style
 *    ✓ Clear intent
 *    ✓ Best for Java 8+
 *    
 *    @Autowired(required=false):
 *    ✓ Simple
 *    ✓ Explicit null checks
 *    ✓ Works with older Java
 *    
 *    @Nullable:
 *    ✓ Parameter-level
 *    ✓ IDE support
 *    ✓ Clear documentation
 * 
 * 5. Use Cases:
 *    ✓ Feature flags
 *    ✓ Optional integrations
 *    ✓ Backward compatibility
 *    ✓ Conditional features
 *    ✓ Graceful degradation
 *    ✓ Development vs production
 * 
 * 6. Optional Methods:
 *    dep.ifPresent(d -> ...);
 *    dep.isPresent();
 *    dep.orElse(default);
 *    dep.orElseGet(() -> ...);
 *    dep.orElseThrow();
 *    dep.map(d -> ...);
 *    dep.filter(d -> ...);
 * 
 * 7. Best Practices:
 *    ✓ Use Optional<T> for new code
 *    ✓ Document why dependency is optional
 *    ✓ Provide fallback behavior
 *    ✓ Don't overuse (most deps should be required)
 *    ✓ Test both present and absent cases
 * 
 * 8. Anti-patterns:
 *    ✗ Optional.get() without isPresent() check
 *    ✗ Too many optional dependencies
 *    ✗ Using Optional for performance (lazy init)
 *    ✗ Optional in fields (use for params/returns)
 * 
 * 9. Testing:
 *    @Test
 *    void testWithOptionalPresent() {
 *        Service service = new Service(Optional.of(mock));
 *        // Test...
 *    }
 *    
 *    @Test
 *    void testWithOptionalAbsent() {
 *        Service service = new Service(Optional.empty());
 *        // Test fallback behavior...
 *    }
 * 
 * 10. When to Use Optional Dependencies:
 *     ✓ Feature flags/toggles
 *     ✓ Environment-specific features
 *     ✓ Optional integrations (monitoring, caching)
 *     ✓ Backward compatibility
 *     ✓ Graceful degradation
 *     
 *     When NOT to use:
 *     ✗ Core business logic
 *     ✗ Required functionality
 *     ✗ Lazy initialization (use @Lazy instead)
 */
