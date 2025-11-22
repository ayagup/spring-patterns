package com.spring.patterns.wiring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

/**
 * Collection Autowiring Pattern
 * 
 * Spring can automatically inject collections of beans (List, Set, Array)
 * when multiple beans of the same type exist.
 * 
 * Characteristics:
 * - Injects ALL beans of specified type
 * - Supports List<T>, Set<T>, T[]
 * - Maintains insertion order for List
 * - Removes duplicates for Set
 * - Can use @Order to control ordering
 * - Empty collection if no beans found
 * 
 * Collection Types:
 * 1. List<T> - Ordered collection with duplicates
 * 2. Set<T> - Unordered unique collection
 * 3. T[] - Array of beans
 * 4. Collection<T> - Generic collection
 * 
 * Ordering:
 * - @Order annotation controls order
 * - Lower values have higher priority
 * - Default order: Integer.MAX_VALUE
 * 
 * Use Cases:
 * - Plugin architecture
 * - Event handlers/listeners
 * - Notification channels
 * - Validation rules
 * - Filter chains
 * - Strategy pattern implementations
 */
@SpringBootApplication
public class CollectionAutowiringPattern {

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(CollectionAutowiringPattern.class, args);
        
        System.out.println("\n=== Collection Autowiring Pattern ===");
        
        // Demonstrate collection injection
        NotificationManager manager = context.getBean(NotificationManager.class);
        manager.sendNotification("System update available");
        
        // Demonstrate validation
        ValidationService validationService = context.getBean(ValidationService.class);
        validationService.validateUser("john@example.com", "John Doe", 25);
        
        // Demonstrate event handling
        EventDispatcher dispatcher = context.getBean(EventDispatcher.class);
        dispatcher.dispatchEvent("USER_REGISTERED", "user123");
        
        // Demonstrate filtering
        FilterChain filterChain = context.getBean(FilterChain.class);
        filterChain.processRequest("Sample request data");
    }
}

/**
 * Configuration
 */
@Configuration
class CollectionWiringConfig {
    
    @Bean
    @Order(1)
    public PaymentGateway creditCardGateway() {
        return new CreditCardGateway();
    }
    
    @Bean
    @Order(2)
    public PaymentGateway paypalGateway() {
        return new PayPalGateway();
    }
    
    @Bean
    @Order(3)
    public PaymentGateway cryptoGateway() {
        return new CryptoGateway();
    }
}

/**
 * Notification Channel interface
 */
interface NotificationChannel {
    void send(String message);
    String getChannelName();
}

/**
 * Email notification
 */
@Component
@Order(1) // Highest priority
class EmailNotification implements NotificationChannel {
    
    @Override
    public void send(String message) {
        System.out.println("[Email] " + message);
    }
    
    @Override
    public String getChannelName() {
        return "Email";
    }
}

/**
 * SMS notification
 */
@Component
@Order(2)
class SmsNotification implements NotificationChannel {
    
    @Override
    public void send(String message) {
        System.out.println("[SMS] " + message);
    }
    
    @Override
    public String getChannelName() {
        return "SMS";
    }
}

/**
 * Push notification
 */
@Component
@Order(3)
class PushNotification implements NotificationChannel {
    
    @Override
    public void send(String message) {
        System.out.println("[Push] " + message);
    }
    
    @Override
    public String getChannelName() {
        return "Push";
    }
}

/**
 * Slack notification
 */
@Component
@Order(4)
class SlackNotification implements NotificationChannel {
    
    @Override
    public void send(String message) {
        System.out.println("[Slack] " + message);
    }
    
    @Override
    public String getChannelName() {
        return "Slack";
    }
}

/**
 * Example 1: List Injection (Ordered)
 */
@Service
class NotificationManager {
    
    // Injects ALL NotificationChannel beans as a List
    // Ordered by @Order annotation
    private final List<NotificationChannel> channels;
    
    @Autowired
    public NotificationManager(List<NotificationChannel> channels) {
        this.channels = channels;
        System.out.println("\nNotificationManager created with " + channels.size() + " channels:");
        channels.forEach(channel -> System.out.println("  - " + channel.getChannelName()));
    }
    
    public void sendNotification(String message) {
        System.out.println("\nSending via all channels:");
        channels.forEach(channel -> channel.send(message));
    }
    
    public void sendViaSpecificChannels(String message, int maxChannels) {
        System.out.println("\nSending via first " + maxChannels + " channels:");
        channels.stream()
                .limit(maxChannels)
                .forEach(channel -> channel.send(message));
    }
}

/**
 * Validator interface
 */
interface Validator {
    boolean validate(Object data);
    String getValidatorName();
}

/**
 * Email validator
 */
@Component
class EmailValidator implements Validator {
    
    @Override
    public boolean validate(Object data) {
        String email = data.toString();
        boolean valid = email != null && email.contains("@");
        System.out.println("  Email validation: " + (valid ? "✓" : "✗"));
        return valid;
    }
    
    @Override
    public String getValidatorName() {
        return "Email";
    }
}

/**
 * Age validator
 */
@Component
class AgeValidator implements Validator {
    
    @Override
    public boolean validate(Object data) {
        if (data instanceof Integer) {
            int age = (Integer) data;
            boolean valid = age >= 18 && age <= 100;
            System.out.println("  Age validation: " + (valid ? "✓" : "✗"));
            return valid;
        }
        return false;
    }
    
    @Override
    public String getValidatorName() {
        return "Age";
    }
}

/**
 * Name validator
 */
@Component
class NameValidator implements Validator {
    
    @Override
    public boolean validate(Object data) {
        String name = data.toString();
        boolean valid = name != null && !name.trim().isEmpty() && name.length() >= 2;
        System.out.println("  Name validation: " + (valid ? "✓" : "✗"));
        return valid;
    }
    
    @Override
    public String getValidatorName() {
        return "Name";
    }
}

/**
 * Example 2: Validation using Collection
 */
@Service
class ValidationService {
    
    // Injects all validators
    private final List<Validator> validators;
    
    @Autowired
    public ValidationService(List<Validator> validators) {
        this.validators = validators;
        System.out.println("\nValidationService created with " + validators.size() + " validators");
    }
    
    public boolean validateUser(String email, String name, int age) {
        System.out.println("\nValidating user data:");
        
        // Run all validators
        boolean emailValid = validators.stream()
                .filter(v -> v.getValidatorName().equals("Email"))
                .findFirst()
                .map(v -> v.validate(email))
                .orElse(false);
        
        boolean nameValid = validators.stream()
                .filter(v -> v.getValidatorName().equals("Name"))
                .findFirst()
                .map(v -> v.validate(name))
                .orElse(false);
        
        boolean ageValid = validators.stream()
                .filter(v -> v.getValidatorName().equals("Age"))
                .findFirst()
                .map(v -> v.validate(age))
                .orElse(false);
        
        return emailValid && nameValid && ageValid;
    }
}

/**
 * Event Handler interface
 */
interface EventHandler {
    void handle(String eventType, String data);
    boolean canHandle(String eventType);
}

/**
 * User event handler
 */
@Component
class UserEventHandler implements EventHandler {
    
    @Override
    public void handle(String eventType, String data) {
        System.out.println("  [UserEventHandler] Processing: " + eventType + " - " + data);
    }
    
    @Override
    public boolean canHandle(String eventType) {
        return eventType.startsWith("USER_");
    }
}

/**
 * Order event handler
 */
@Component
class OrderEventHandler implements EventHandler {
    
    @Override
    public void handle(String eventType, String data) {
        System.out.println("  [OrderEventHandler] Processing: " + eventType + " - " + data);
    }
    
    @Override
    public boolean canHandle(String eventType) {
        return eventType.startsWith("ORDER_");
    }
}

/**
 * Payment event handler
 */
@Component
class PaymentEventHandler implements EventHandler {
    
    @Override
    public void handle(String eventType, String data) {
        System.out.println("  [PaymentEventHandler] Processing: " + eventType + " - " + data);
    }
    
    @Override
    public boolean canHandle(String eventType) {
        return eventType.startsWith("PAYMENT_");
    }
}

/**
 * Example 3: Event Dispatching with Collection
 */
@Service
class EventDispatcher {
    
    private final List<EventHandler> handlers;
    
    @Autowired
    public EventDispatcher(List<EventHandler> handlers) {
        this.handlers = handlers;
        System.out.println("\nEventDispatcher created with " + handlers.size() + " handlers");
    }
    
    public void dispatchEvent(String eventType, String data) {
        System.out.println("\nDispatching event: " + eventType);
        
        handlers.stream()
                .filter(handler -> handler.canHandle(eventType))
                .forEach(handler -> handler.handle(eventType, data));
    }
}

/**
 * Request Filter interface
 */
interface RequestFilter {
    String filter(String request);
    int getOrder();
}

/**
 * Authentication filter
 */
@Component
@Order(1)
class AuthenticationFilter implements RequestFilter {
    
    @Override
    public String filter(String request) {
        System.out.println("  [1] Authentication filter applied");
        return request + " [AUTHENTICATED]";
    }
    
    @Override
    public int getOrder() {
        return 1;
    }
}

/**
 * Validation filter
 */
@Component
@Order(2)
class RequestValidationFilter implements RequestFilter {
    
    @Override
    public String filter(String request) {
        System.out.println("  [2] Validation filter applied");
        return request + " [VALIDATED]";
    }
    
    @Override
    public int getOrder() {
        return 2;
    }
}

/**
 * Logging filter
 */
@Component
@Order(3)
class LoggingFilter implements RequestFilter {
    
    @Override
    public String filter(String request) {
        System.out.println("  [3] Logging filter applied");
        return request + " [LOGGED]";
    }
    
    @Override
    public int getOrder() {
        return 3;
    }
}

/**
 * Example 4: Filter Chain using Collection
 */
@Service
class FilterChain {
    
    // Ordered list of filters
    private final List<RequestFilter> filters;
    
    @Autowired
    public FilterChain(List<RequestFilter> filters) {
        this.filters = filters;
        System.out.println("\nFilterChain created with " + filters.size() + " filters");
    }
    
    public String processRequest(String request) {
        System.out.println("\nProcessing request through filter chain:");
        
        String result = request;
        for (RequestFilter filter : filters) {
            result = filter.filter(result);
        }
        
        System.out.println("\nFinal result: " + result);
        return result;
    }
}

/**
 * Payment Gateway interface
 */
interface PaymentGateway {
    String process(double amount);
    String getName();
}

/**
 * Credit Card Gateway
 */
class CreditCardGateway implements PaymentGateway {
    
    @Override
    public String process(double amount) {
        return "Credit Card: $" + amount;
    }
    
    @Override
    public String getName() {
        return "Credit Card";
    }
}

/**
 * PayPal Gateway
 */
class PayPalGateway implements PaymentGateway {
    
    @Override
    public String process(double amount) {
        return "PayPal: $" + amount;
    }
    
    @Override
    public String getName() {
        return "PayPal";
    }
}

/**
 * Crypto Gateway
 */
class CryptoGateway implements PaymentGateway {
    
    @Override
    public String process(double amount) {
        return "Cryptocurrency: $" + amount;
    }
    
    @Override
    public String getName() {
        return "Crypto";
    }
}

/**
 * Example 5: Set Injection (No duplicates)
 */
@Service
class PaymentService {
    
    // Injects as Set (unique beans only)
    private final Set<PaymentGateway> gateways;
    
    @Autowired
    public PaymentService(Set<PaymentGateway> gateways) {
        this.gateways = gateways;
        System.out.println("\nPaymentService created with " + gateways.size() + " gateways");
    }
    
    public void showAvailableGateways() {
        System.out.println("\nAvailable payment gateways:");
        gateways.forEach(gateway -> System.out.println("  - " + gateway.getName()));
    }
}

/**
 * Example 6: Array Injection
 */
@Service
class GatewayManager {
    
    // Injects as array
    private final PaymentGateway[] gateways;
    
    @Autowired
    public GatewayManager(PaymentGateway[] gateways) {
        this.gateways = gateways;
        System.out.println("\nGatewayManager created with " + gateways.length + " gateways");
    }
    
    public String processWithFallback(double amount) {
        for (PaymentGateway gateway : gateways) {
            try {
                return gateway.process(amount);
            } catch (Exception e) {
                System.out.println(gateway.getName() + " failed, trying next...");
            }
        }
        return "All gateways failed";
    }
}

/**
 * REST Controller
 */
@RestController
@RequestMapping("/api/collection-wiring")
class CollectionWiringController {
    
    private final NotificationManager notificationManager;
    private final ValidationService validationService;
    private final EventDispatcher eventDispatcher;
    private final FilterChain filterChain;
    
    public CollectionWiringController(NotificationManager notificationManager,
                                     ValidationService validationService,
                                     EventDispatcher eventDispatcher,
                                     FilterChain filterChain) {
        this.notificationManager = notificationManager;
        this.validationService = validationService;
        this.eventDispatcher = eventDispatcher;
        this.filterChain = filterChain;
    }
    
    @GetMapping("/notify")
    public String sendNotification() {
        notificationManager.sendNotification("Test notification");
        return "Notification sent via all channels";
    }
    
    @GetMapping("/validate")
    public String validateUser() {
        boolean valid = validationService.validateUser("test@example.com", "Test User", 25);
        return "Validation result: " + (valid ? "PASS" : "FAIL");
    }
    
    @GetMapping("/event")
    public String dispatchEvent() {
        eventDispatcher.dispatchEvent("USER_CREATED", "user123");
        return "Event dispatched";
    }
    
    @GetMapping("/filter")
    public String filterRequest() {
        return filterChain.processRequest("Test request");
    }
}

/**
 * Key Points:
 * 
 * 1. Collection Injection:
 *    @Autowired
 *    private List<Service> services;  // All Service beans
 *    
 *    @Autowired
 *    private Set<Service> services;   // Unique Service beans
 *    
 *    @Autowired
 *    private Service[] services;      // Array of Service beans
 * 
 * 2. Ordering with @Order:
 *    @Component
 *    @Order(1)  // Lower value = higher priority
 *    class HighPriorityService implements Service { }
 *    
 *    @Component
 *    @Order(10)
 *    class LowPriorityService implements Service { }
 * 
 * 3. Empty Collections:
 *    - No beans found → Empty collection (not null)
 *    - List<T> → []
 *    - Set<T> → []
 *    - T[] → new T[0]
 * 
 * 4. Use Cases:
 *    ✓ Plugin architecture (load all plugins)
 *    ✓ Event handlers (notify all listeners)
 *    ✓ Validation rules (run all validators)
 *    ✓ Filter chains (apply all filters)
 *    ✓ Notification channels (send to all)
 *    ✓ Strategy pattern (try multiple strategies)
 * 
 * 5. Advantages:
 *    ✓ Automatic discovery of implementations
 *    ✓ No manual registration needed
 *    ✓ Extensible (add new beans → auto-injected)
 *    ✓ Type-safe
 *    ✓ Supports ordering
 * 
 * 6. Best Practices:
 *    ✓ Use List<T> for ordered processing
 *    ✓ Use Set<T> to avoid duplicates
 *    ✓ Use @Order for explicit ordering
 *    ✓ Document expected bean types
 *    ✓ Handle empty collections gracefully
 * 
 * 7. Comparison:
 *    List<T>:
 *    ✓ Maintains order
 *    ✓ Allows duplicates
 *    ✓ Index access
 *    
 *    Set<T>:
 *    ✓ No duplicates
 *    ✓ Unordered
 *    ✓ Membership testing
 *    
 *    T[]:
 *    ✓ Array operations
 *    ✓ Fixed size
 *    ✓ Primitive compatible
 * 
 * 8. Generic Collections:
 *    @Autowired
 *    private Collection<Service> services;
 *    
 *    @Autowired
 *    private Iterable<Service> services;
 * 
 * 9. Conditional Beans:
 *    @Component
 *    @ConditionalOnProperty("feature.enabled")
 *    class FeatureService implements Service { }
 *    
 *    // Only injected if condition met
 * 
 * 10. Testing:
 *     @TestConfiguration
 *     static class TestConfig {
 *         @Bean
 *         public Service mockService1() { ... }
 *         
 *         @Bean
 *         public Service mockService2() { ... }
 *     }
 *     // Both beans injected into List<Service>
 */
