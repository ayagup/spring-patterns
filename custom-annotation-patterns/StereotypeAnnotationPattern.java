package com.example.customannotation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.lang.annotation.*;

/**
 * Stereotype Annotation Pattern
 * 
 * Demonstrates creating custom stereotype annotations that mark classes as Spring components.
 * Stereotype annotations are specializations of @Component that indicate specific roles in
 * the application architecture:
 * - Component scanning and auto-detection
 * - Layer identification (presentation, service, data)
 * - Architectural documentation
 * - AOP pointcut targets
 * - Custom bean behavior
 * 
 * Key Features:
 * - Component scanning
 * - Layer semantics
 * - Custom stereotypes
 * - Bean registration
 * - AOP integration
 * 
 * Use Cases:
 * - Define architectural layers
 * - Create domain-specific components
 * - Mark specialized beans
 * - Support component scanning
 * - Enable aspect targeting
 * 
 * @author Spring Patterns
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.example.customannotation")
public class StereotypeAnnotationPattern {

    public static void main(String[] args) {
        SpringApplication.run(StereotypeAnnotationPattern.class, args);
    }

    // =========================================================================
    // BASIC STEREOTYPE ANNOTATIONS
    // =========================================================================

    /**
     * Stereotype for business logic components
     * Meta-annotated with @Component
     */
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Component
    public @interface BusinessLogic {
        
        /**
         * Bean name
         */
        @AliasFor(annotation = Component.class)
        String value() default "";
        
        /**
         * Business domain
         */
        String domain() default "";
    }

    /**
     * Stereotype for integration components
     */
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Component
    public @interface Integration {
        
        @AliasFor(annotation = Component.class)
        String value() default "";
        
        /**
         * External system type
         */
        String system() default "";
        
        /**
         * Integration protocol
         */
        String protocol() default "REST";
    }

    /**
     * Stereotype for utility components
     */
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Component
    public @interface Utility {
        
        @AliasFor(annotation = Component.class)
        String value() default "";
        
        /**
         * Utility category
         */
        String category() default "general";
    }

    // =========================================================================
    // LAYER-SPECIFIC STEREOTYPES
    // =========================================================================

    /**
     * Stereotype for data access objects
     * More specific than @Repository
     */
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Repository
    public @interface DataAccessObject {
        
        @AliasFor(annotation = Repository.class)
        String value() default "";
        
        /**
         * Entity type managed
         */
        String entityType() default "";
        
        /**
         * Data source name
         */
        String dataSource() default "primary";
    }

    /**
     * Stereotype for cache repositories
     */
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Component
    public @interface CacheRepository {
        
        @AliasFor(annotation = Component.class)
        String value() default "";
        
        /**
         * Cache provider
         */
        String provider() default "Redis";
        
        /**
         * Default TTL in seconds
         */
        int ttl() default 300;
    }

    /**
     * Stereotype for facade services
     */
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Service
    public @interface Facade {
        
        @AliasFor(annotation = Service.class)
        String value() default "";
        
        /**
         * Subsystem being facade
         */
        String subsystem() default "";
    }

    // =========================================================================
    // DOMAIN-SPECIFIC STEREOTYPES
    // =========================================================================

    /**
     * Stereotype for event handlers
     */
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Component
    public @interface EventHandler {
        
        @AliasFor(annotation = Component.class)
        String value() default "";
        
        /**
         * Event types handled
         */
        String[] eventTypes() default {};
        
        /**
         * Handler priority
         */
        int priority() default 0;
    }

    /**
     * Stereotype for validators
     */
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Component
    public @interface Validator {
        
        @AliasFor(annotation = Component.class)
        String value() default "";
        
        /**
         * Entity type validated
         */
        String entityType();
        
        /**
         * Validation phase
         */
        String phase() default "CREATE";
    }

    /**
     * Stereotype for transformers/converters
     */
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Component
    public @interface Transformer {
        
        @AliasFor(annotation = Component.class)
        String value() default "";
        
        /**
         * Source type
         */
        String from();
        
        /**
         * Target type
         */
        String to();
    }

    /**
     * Stereotype for schedulers
     */
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Component
    public @interface Scheduler {
        
        @AliasFor(annotation = Component.class)
        String value() default "";
        
        /**
         * Schedule type
         */
        String scheduleType() default "CRON";
    }

    // =========================================================================
    // SPECIALIZED STEREOTYPES
    // =========================================================================

    /**
     * Stereotype for workflow components
     */
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Component
    public @interface Workflow {
        
        @AliasFor(annotation = Component.class)
        String value() default "";
        
        /**
         * Workflow name
         */
        String workflowName();
        
        /**
         * Workflow version
         */
        String version() default "1.0";
    }

    /**
     * Stereotype for strategy pattern implementations
     */
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Component
    public @interface Strategy {
        
        @AliasFor(annotation = Component.class)
        String value() default "";
        
        /**
         * Strategy type
         */
        String type();
        
        /**
         * Strategy priority
         */
        int priority() default 0;
    }

    /**
     * Stereotype for plugin components
     */
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Component
    public @interface Plugin {
        
        @AliasFor(annotation = Component.class)
        String value() default "";
        
        /**
         * Plugin name
         */
        String name();
        
        /**
         * Plugin version
         */
        String version();
        
        /**
         * Whether plugin is enabled by default
         */
        boolean enabledByDefault() default true;
    }

    // =========================================================================
    // USAGE EXAMPLES
    // =========================================================================

    /**
     * Business logic component
     */
    @BusinessLogic(domain = "User Management")
    public static class UserBusinessLogic {
        
        public void validateUserCreation(String username, String email) {
            System.out.println("Validating user creation: " + username);
        }
        
        public void processUserRegistration(String username) {
            System.out.println("Processing user registration: " + username);
        }
    }

    /**
     * Integration component
     */
    @Integration(system = "Payment Gateway", protocol = "REST")
    public static class PaymentGatewayIntegration {
        
        public String processPayment(String orderId, double amount) {
            System.out.println("Processing payment for order: " + orderId + ", amount: " + amount);
            return "PAYMENT_" + orderId;
        }
        
        public String refundPayment(String paymentId) {
            System.out.println("Refunding payment: " + paymentId);
            return "REFUND_" + paymentId;
        }
    }

    /**
     * Utility component
     */
    @Utility(category = "String Operations")
    public static class StringUtility {
        
        public String sanitize(String input) {
            System.out.println("Sanitizing string: " + input);
            return input != null ? input.trim() : "";
        }
        
        public String hash(String input) {
            System.out.println("Hashing string");
            return Integer.toHexString(input.hashCode());
        }
    }

    /**
     * Data access object
     */
    @DataAccessObject(entityType = "User", dataSource = "primary")
    public static class UserDAO {
        
        public User findById(Long id) {
            System.out.println("Finding user by ID: " + id);
            return new User(id, "User " + id);
        }
        
        public void save(User user) {
            System.out.println("Saving user: " + user.getUsername());
        }
        
        public void delete(Long id) {
            System.out.println("Deleting user: " + id);
        }
    }

    /**
     * Cache repository
     */
    @CacheRepository(provider = "Redis", ttl = 600)
    public static class UserCacheRepository {
        
        public User get(String key) {
            System.out.println("Getting from cache: " + key);
            return null; // Would fetch from Redis
        }
        
        public void put(String key, User user) {
            System.out.println("Putting to cache: " + key);
            // Would store in Redis
        }
    }

    /**
     * Facade service
     */
    @Facade(subsystem = "Order Processing")
    public static class OrderFacade {
        
        public String placeOrder(String customerId, String productId) {
            System.out.println("Placing order for customer: " + customerId);
            return "ORDER_123";
        }
        
        public void cancelOrder(String orderId) {
            System.out.println("Canceling order: " + orderId);
        }
    }

    /**
     * Event handler
     */
    @EventHandler(eventTypes = {"UserCreated", "UserUpdated"}, priority = 1)
    public static class UserEventHandler {
        
        public void handleUserCreated(String userId) {
            System.out.println("Handling UserCreated event for: " + userId);
        }
        
        public void handleUserUpdated(String userId) {
            System.out.println("Handling UserUpdated event for: " + userId);
        }
    }

    /**
     * Validator component
     */
    @Validator(entityType = "Order", phase = "CREATE")
    public static class OrderValidator {
        
        public boolean validate(Order order) {
            System.out.println("Validating order: " + order.getId());
            return order.getAmount() > 0;
        }
    }

    /**
     * Transformer component
     */
    @Transformer(from = "UserDTO", to = "User")
    public static class UserTransformer {
        
        public User transform(UserDTO dto) {
            System.out.println("Transforming UserDTO to User");
            return new User(dto.getId(), dto.getUsername());
        }
    }

    /**
     * Scheduler component
     */
    @Scheduler(scheduleType = "CRON")
    public static class ReportScheduler {
        
        public void generateDailyReport() {
            System.out.println("Generating daily report");
        }
    }

    /**
     * Workflow component
     */
    @Workflow(workflowName = "OrderApproval", version = "2.0")
    public static class OrderApprovalWorkflow {
        
        public void startWorkflow(String orderId) {
            System.out.println("Starting approval workflow for order: " + orderId);
        }
    }

    /**
     * Strategy implementation
     */
    @Strategy(type = "PaymentStrategy", priority = 1)
    public static class CreditCardPaymentStrategy {
        
        public void process(double amount) {
            System.out.println("Processing credit card payment: " + amount);
        }
    }

    /**
     * Plugin component
     */
    @Plugin(name = "Email Notifier", version = "1.5.0", enabledByDefault = true)
    public static class EmailNotifierPlugin {
        
        public void sendEmail(String to, String subject, String body) {
            System.out.println("Sending email to: " + to);
        }
    }

    // Domain Classes

    public static class User {
        private Long id;
        private String username;

        public User(Long id, String username) {
            this.id = id;
            this.username = username;
        }

        public Long getId() { return id; }
        public String getUsername() { return username; }
    }

    public static class UserDTO {
        private Long id;
        private String username;

        public Long getId() { return id; }
        public String getUsername() { return username; }
    }

    public static class Order {
        private Long id;
        private double amount;

        public Long getId() { return id; }
        public double getAmount() { return amount; }
    }
}

/**
 * DOCUMENTATION
 * 
 * Stereotype Annotation Pattern:
 * 
 * 1. What is a Stereotype?
 *    - Specialized form of @Component
 *    - Indicates specific role in architecture
 *    - Enables component scanning
 *    - Provides semantic meaning
 *    - Supports AOP pointcuts
 * 
 * 2. Built-in Spring Stereotypes:
 *    - @Component: Generic component
 *    - @Service: Business logic layer
 *    - @Repository: Data access layer (with exception translation)
 *    - @Controller: Presentation layer
 *    - @RestController: REST API layer
 *    - @Configuration: Configuration class
 * 
 * 3. Creating Custom Stereotypes:
 *    - Annotate with @Component (or existing stereotype)
 *    - Add @Target(ElementType.TYPE)
 *    - Add @Retention(RetentionPolicy.RUNTIME)
 *    - Optionally add @Documented
 *    - Define custom attributes
 * 
 * 4. Component Scanning:
 *    - Spring scans for @Component and stereotypes
 *    - Registers beans automatically
 *    - Respects @ComponentScan configuration
 *    - Processes meta-annotations transitively
 * 
 * 5. Benefits:
 *    - Clear architectural layering
 *    - Self-documenting code
 *    - Consistent bean naming
 *    - AOP pointcut targets
 *    - Custom bean behavior
 * 
 * 6. Common Custom Stereotypes:
 *    - @BusinessLogic: Business rules
 *    - @Integration: External system integration
 *    - @Facade: Subsystem facade
 *    - @EventHandler: Event processing
 *    - @Validator: Validation logic
 *    - @Transformer: Data transformation
 *    - @Workflow: Workflow orchestration
 * 
 * 7. Attribute Design:
 *    - Use @AliasFor for bean name
 *    - Add domain-specific metadata
 *    - Provide sensible defaults
 *    - Document attribute purpose
 * 
 * 8. AOP Integration:
 *    - Target specific stereotypes in pointcuts
 *    - Example: @Around("@within(Integration)")
 *    - Apply cross-cutting concerns by layer
 *    - Consistent logging/monitoring
 * 
 * 9. Best Practices:
 *    - Create meaningful layer names
 *    - One stereotype per architectural concern
 *    - Document stereotype purpose
 *    - Use consistent naming conventions
 *    - Align with team architecture
 * 
 * 10. Difference from @Component:
 *     - @Component: Generic bean
 *     - Stereotype: Specific role/responsibility
 *     - Stereotypes provide semantic clarity
 *     - Better architectural documentation
 *     - Enables layer-specific behavior
 * 
 * 11. Testing:
 *     - Verify component scanning detects stereotypes
 *     - Check bean registration
 *     - Test AOP pointcuts
 *     - Validate custom attributes
 *     - Ensure proper bean naming
 */
