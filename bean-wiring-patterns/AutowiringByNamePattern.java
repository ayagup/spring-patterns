package com.spring.patterns.wiring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

/**
 * Autowiring by Name Pattern
 * 
 * Spring autowires beans by matching the name of the dependency field/parameter
 * with the bean name in the application context.
 * 
 * Characteristics:
 * - Matches bean by name (field/parameter name = bean name)
 * - Uses @Resource annotation (JSR-250)
 * - Fallback to type matching if name doesn't match
 * - Useful when multiple beans of same type exist
 * - Bean names can be customized via @Component("name") or @Bean(name="name")
 * 
 * Bean Naming Convention:
 * - Default: Class name with first letter lowercase
 * - Custom: Specified in @Component, @Service, @Bean annotations
 * - Can specify multiple names (aliases)
 * 
 * Use Cases:
 * - Multiple implementations of same interface
 * - Selecting specific bean by name
 * - Legacy code compatibility
 * - Explicit dependency selection
 */
@SpringBootApplication
public class AutowiringByNamePattern {

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(AutowiringByNamePattern.class, args);
        
        System.out.println("\n=== Autowiring by Name Pattern ===");
        
        // List all bean names
        System.out.println("\nAvailable beans:");
        for (String name : context.getBeanDefinitionNames()) {
            if (name.contains("Payment") || name.contains("Notification")) {
                System.out.println("  - " + name);
            }
        }
        
        // Demonstrate name-based wiring
        PaymentProcessor processor = context.getBean(PaymentProcessor.class);
        System.out.println("\nPayment processor uses:");
        System.out.println("  - " + processor.getPaymentService().getClass().getSimpleName());
        System.out.println("  - " + processor.getNotificationService().getClass().getSimpleName());
        
        processor.processPayment("ORDER-123", 100.00);
    }
}

/**
 * Configuration defining multiple beans of same type with different names
 */
@Configuration
class NameWiringConfig {
    
    // Payment service beans with different names
    @Bean(name = "creditCardPayment")
    public PaymentService creditCardPaymentService() {
        return new CreditCardPaymentService();
    }
    
    @Bean(name = "paypalPayment")
    public PaymentService paypalPaymentService() {
        return new PayPalPaymentService();
    }
    
    @Bean(name = "stripePayment")
    public PaymentService stripePaymentService() {
        return new StripePaymentService();
    }
    
    // Notification service beans with different names
    @Bean(name = "emailNotification")
    public NotificationService emailNotificationService() {
        return new EmailNotificationService();
    }
    
    @Bean(name = "smsNotification")
    public NotificationService smsNotificationService() {
        return new SmsNotificationService();
    }
}

/**
 * Payment service interface
 */
interface PaymentService {
    String processPayment(String orderId, double amount);
    String getPaymentMethod();
}

/**
 * Credit card payment implementation
 */
class CreditCardPaymentService implements PaymentService {
    
    public CreditCardPaymentService() {
        System.out.println("CreditCardPaymentService created");
    }
    
    @Override
    public String processPayment(String orderId, double amount) {
        return "Credit Card payment of $" + amount + " for " + orderId;
    }
    
    @Override
    public String getPaymentMethod() {
        return "Credit Card";
    }
}

/**
 * PayPal payment implementation
 */
class PayPalPaymentService implements PaymentService {
    
    public PayPalPaymentService() {
        System.out.println("PayPalPaymentService created");
    }
    
    @Override
    public String processPayment(String orderId, double amount) {
        return "PayPal payment of $" + amount + " for " + orderId;
    }
    
    @Override
    public String getPaymentMethod() {
        return "PayPal";
    }
}

/**
 * Stripe payment implementation
 */
class StripePaymentService implements PaymentService {
    
    public StripePaymentService() {
        System.out.println("StripePaymentService created");
    }
    
    @Override
    public String processPayment(String orderId, double amount) {
        return "Stripe payment of $" + amount + " for " + orderId;
    }
    
    @Override
    public String getPaymentMethod() {
        return "Stripe";
    }
}

/**
 * Notification service interface
 */
interface NotificationService {
    void sendNotification(String message);
    String getNotificationType();
}

/**
 * Email notification implementation
 */
class EmailNotificationService implements NotificationService {
    
    public EmailNotificationService() {
        System.out.println("EmailNotificationService created");
    }
    
    @Override
    public void sendNotification(String message) {
        System.out.println("Email notification: " + message);
    }
    
    @Override
    public String getNotificationType() {
        return "Email";
    }
}

/**
 * SMS notification implementation
 */
class SmsNotificationService implements NotificationService {
    
    public SmsNotificationService() {
        System.out.println("SmsNotificationService created");
    }
    
    @Override
    public void sendNotification(String message) {
        System.out.println("SMS notification: " + message);
    }
    
    @Override
    public String getNotificationType() {
        return "SMS";
    }
}

/**
 * Payment processor using @Resource for name-based autowiring
 */
@Service
class PaymentProcessor {
    
    // Autowire by name using @Resource
    // Field name "creditCardPayment" matches bean name
    @Resource(name = "creditCardPayment")
    private PaymentService paymentService;
    
    // Field name matches bean name "emailNotification"
    @Resource(name = "emailNotification")
    private NotificationService notificationService;
    
    public PaymentProcessor() {
        System.out.println("PaymentProcessor created");
    }
    
    public String processPayment(String orderId, double amount) {
        String result = paymentService.processPayment(orderId, amount);
        notificationService.sendNotification("Payment processed: " + orderId);
        return result;
    }
    
    public PaymentService getPaymentService() {
        return paymentService;
    }
    
    public NotificationService getNotificationService() {
        return notificationService;
    }
}

/**
 * Order service using different payment method
 */
@Service
class OrderService {
    
    // Uses PayPal payment service
    @Resource(name = "paypalPayment")
    private PaymentService paymentService;
    
    // Uses SMS notification
    @Resource(name = "smsNotification")
    private NotificationService notificationService;
    
    public String placeOrder(String orderId, double amount) {
        String result = paymentService.processPayment(orderId, amount);
        notificationService.sendNotification("Order placed: " + orderId);
        return result;
    }
}

/**
 * Subscription service using Stripe
 */
@Service
class SubscriptionService {
    
    // Uses Stripe payment service
    @Resource(name = "stripePayment")
    private PaymentService paymentService;
    
    // Uses Email notification
    @Resource(name = "emailNotification")
    private NotificationService notificationService;
    
    public String createSubscription(String userId, double amount) {
        String result = paymentService.processPayment("SUB-" + userId, amount);
        notificationService.sendNotification("Subscription created for user: " + userId);
        return result;
    }
}

/**
 * Service using field name matching (without explicit name)
 */
@Component("customLogger")
class CustomLogger {
    
    public void log(String message) {
        System.out.println("[CustomLogger] " + message);
    }
}

@Service
class LoggingService {
    
    // Field name "customLogger" matches bean name
    @Resource
    private CustomLogger customLogger;
    
    public void logEvent(String event) {
        customLogger.log("Event: " + event);
    }
}

/**
 * Demonstrating multiple names (aliases)
 */
@Component(value = {"mainCache", "primaryCache", "appCache"})
class CacheManager {
    
    public void put(String key, Object value) {
        System.out.println("Cache put: " + key);
    }
    
    public Object get(String key) {
        return "Cached value for: " + key;
    }
}

@Service
class CachingService {
    
    // Can use any of the bean names
    @Resource(name = "primaryCache")
    private CacheManager cacheManager;
    
    public void cacheData(String key, Object value) {
        cacheManager.put(key, value);
    }
}

/**
 * REST Controller
 */
@RestController
@RequestMapping("/api/name-wiring")
class NameWiringController {
    
    private final PaymentProcessor paymentProcessor;
    private final OrderService orderService;
    private final SubscriptionService subscriptionService;
    
    public NameWiringController(PaymentProcessor paymentProcessor,
                               OrderService orderService,
                               SubscriptionService subscriptionService) {
        this.paymentProcessor = paymentProcessor;
        this.orderService = orderService;
        this.subscriptionService = subscriptionService;
    }
    
    @GetMapping("/payment")
    public String processPayment() {
        return paymentProcessor.processPayment("ORDER-001", 99.99);
    }
    
    @GetMapping("/order")
    public String placeOrder() {
        return orderService.placeOrder("ORDER-002", 149.99);
    }
    
    @GetMapping("/subscription")
    public String createSubscription() {
        return subscriptionService.createSubscription("USER-123", 29.99);
    }
    
    @GetMapping("/info")
    public String getInfo() {
        return "Payment methods available:\n" +
               "  - Credit Card (/payment)\n" +
               "  - PayPal (/order)\n" +
               "  - Stripe (/subscription)";
    }
}

/**
 * Key Points:
 * 
 * 1. @Resource Annotation:
 *    - JSR-250 standard
 *    - By name first, then by type
 *    - @Resource(name = "beanName")
 *    - Field name used if name not specified
 * 
 * 2. Bean Naming:
 *    - Default: camelCase of class name
 *    - Custom: @Component("name"), @Bean(name="name")
 *    - Multiple names: @Component({"name1", "name2"})
 * 
 * 3. Naming Strategies:
 *    @Component
 *    class UserService { } → bean name: "userService"
 *    
 *    @Component("customName")
 *    class UserService { } → bean name: "customName"
 *    
 *    @Bean(name = {"primary", "main"})
 *    public Service service() { } → names: "primary", "main"
 * 
 * 4. @Resource vs @Autowired:
 *    @Resource:
 *    - By name first, then type
 *    - JSR-250 standard
 *    - name attribute
 *    
 *    @Autowired:
 *    - By type only
 *    - Spring-specific
 *    - Needs @Qualifier for name
 * 
 * 5. Use Cases:
 *    ✓ Multiple implementations of interface
 *    ✓ Selecting specific implementation
 *    ✓ Avoiding @Qualifier verbosity
 *    ✓ JSR-250 compliance
 * 
 * 6. Best Practices:
 *    ✓ Use meaningful bean names
 *    ✓ Be consistent with naming
 *    ✓ Document custom names
 *    ✓ Prefer type-based with @Primary for clarity
 * 
 * 7. When to Use:
 *    - Multiple beans of same type
 *    - Explicit bean selection needed
 *    - Legacy code compatibility
 *    - JSR-250 requirement
 * 
 * 8. Fallback Behavior:
 *    @Resource without name:
 *    1. Match by field name
 *    2. Match by type
 *    3. Fail with NoSuchBeanDefinitionException
 */
