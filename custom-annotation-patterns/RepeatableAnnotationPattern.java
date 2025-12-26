package com.example.customannotation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.lang.annotation.*;

/**
 * Repeatable Annotation Pattern
 * 
 * Demonstrates creating repeatable annotations that can be applied multiple times
 * to the same element. Introduced in Java 8, repeatable annotations allow:
 * - Multiple instances on same target
 * - Container annotation pattern
 * - Cleaner syntax than array-based annotations
 * - Type-safe multiple values
 * 
 * Key Features:
 * - @Repeatable meta-annotation
 * - Container annotation
 * - Multiple annotation instances
 * - Simplified syntax
 * - Runtime reflection access
 * 
 * Use Cases:
 * - Multiple scheduled tasks
 * - Multiple validation rules
 * - Multiple security roles
 * - Multiple event listeners
 * - Multiple data sources
 * 
 * @author Spring Patterns
 */
@SpringBootApplication
public class RepeatableAnnotationPattern {

    public static void main(String[] args) {
        SpringApplication.run(RepeatableAnnotationPattern.class, args);
    }

    // =========================================================================
    // REPEATABLE ANNOTATION DEFINITIONS
    // =========================================================================

    /**
     * Repeatable schedule annotation
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Repeatable(Schedules.class)
    public @interface Schedule {
        
        /**
         * Cron expression
         */
        String cron() default "";
        
        /**
         * Fixed rate in milliseconds
         */
        long fixedRate() default -1;
        
        /**
         * Fixed delay in milliseconds
         */
        long fixedDelay() default -1;
        
        /**
         * Time zone
         */
        String zone() default "UTC";
    }

    /**
     * Container annotation for @Schedule
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    public @interface Schedules {
        Schedule[] value();
    }

    /**
     * Repeatable validation rule annotation
     */
    @Target({ElementType.FIELD, ElementType.PARAMETER})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Repeatable(ValidationRules.class)
    public @interface ValidationRule {
        
        /**
         * Rule type
         */
        RuleType type();
        
        /**
         * Rule parameter (e.g., length, pattern)
         */
        String param() default "";
        
        /**
         * Error message
         */
        String message() default "Validation failed";
    }

    public enum RuleType {
        NOT_NULL,
        MIN_LENGTH,
        MAX_LENGTH,
        PATTERN,
        RANGE,
        EMAIL,
        CUSTOM
    }

    /**
     * Container annotation for @ValidationRule
     */
    @Target({ElementType.FIELD, ElementType.PARAMETER})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    public @interface ValidationRules {
        ValidationRule[] value();
    }

    /**
     * Repeatable role annotation
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Repeatable(Roles.class)
    public @interface Role {
        
        /**
         * Role name
         */
        String value();
        
        /**
         * Permission level
         */
        int level() default 1;
    }

    /**
     * Container annotation for @Role
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    public @interface Roles {
        Role[] value();
    }

    /**
     * Repeatable event listener annotation
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Repeatable(EventListeners.class)
    public @interface EventListener {
        
        /**
         * Event type
         */
        String eventType();
        
        /**
         * Listener priority
         */
        int priority() default 0;
        
        /**
         * Whether listener is async
         */
        boolean async() default false;
    }

    /**
     * Container annotation for @EventListener
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    public @interface EventListeners {
        EventListener[] value();
    }

    /**
     * Repeatable cache configuration
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Repeatable(Caches.class)
    public @interface Cache {
        
        /**
         * Cache name
         */
        String name();
        
        /**
         * Cache key expression
         */
        String key() default "";
        
        /**
         * TTL in seconds
         */
        int ttl() default 300;
    }

    /**
     * Container annotation for @Cache
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    public @interface Caches {
        Cache[] value();
    }

    /**
     * Repeatable retry configuration
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Repeatable(Retries.class)
    public @interface Retry {
        
        /**
         * Exception type to retry on
         */
        Class<? extends Exception> exception();
        
        /**
         * Maximum retry attempts
         */
        int maxAttempts() default 3;
        
        /**
         * Backoff delay in milliseconds
         */
        long backoff() default 1000;
    }

    /**
     * Container annotation for @Retry
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    public @interface Retries {
        Retry[] value();
    }

    // =========================================================================
    // USAGE EXAMPLES
    // =========================================================================

    /**
     * Service with multiple schedules
     */
    @Service
    public static class ScheduledTaskService {
        
        /**
         * Multiple schedules on same method
         */
        @Schedule(cron = "0 0 0 * * *", zone = "UTC")  // Daily at midnight UTC
        @Schedule(cron = "0 0 12 * * *", zone = "America/New_York")  // Daily at noon EST
        @Schedule(fixedRate = 3600000)  // Every hour
        public void generateReport() {
            System.out.println("Generating report at: " + java.time.LocalDateTime.now());
        }
        
        @Schedule(cron = "0 */15 * * * *")  // Every 15 minutes
        @Schedule(fixedDelay = 60000)  // After 1 minute delay
        public void checkHealth() {
            System.out.println("Health check at: " + java.time.LocalDateTime.now());
        }
    }

    /**
     * Entity with validation rules
     */
    public static class User {
        
        @ValidationRule(type = RuleType.NOT_NULL, message = "Username is required")
        @ValidationRule(type = RuleType.MIN_LENGTH, param = "3", message = "Username must be at least 3 characters")
        @ValidationRule(type = RuleType.MAX_LENGTH, param = "20", message = "Username must not exceed 20 characters")
        @ValidationRule(type = RuleType.PATTERN, param = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscores")
        private String username;
        
        @ValidationRule(type = RuleType.NOT_NULL, message = "Email is required")
        @ValidationRule(type = RuleType.EMAIL, message = "Invalid email format")
        private String email;
        
        @ValidationRule(type = RuleType.NOT_NULL, message = "Age is required")
        @ValidationRule(type = RuleType.RANGE, param = "18-120", message = "Age must be between 18 and 120")
        private Integer age;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public Integer getAge() { return age; }
        public void setAge(Integer age) { this.age = age; }
    }

    /**
     * Service with multiple roles
     */
    @Service
    @Role(value = "USER", level = 1)
    @Role(value = "MANAGER", level = 2)
    @Role(value = "ADMIN", level = 3)
    public static class AdminService {
        
        @Role("SUPER_ADMIN")
        @Role("SYSTEM_ADMIN")
        public void deleteAllData() {
            System.out.println("Deleting all data (requires SUPER_ADMIN or SYSTEM_ADMIN)");
        }
        
        @Role("ADMIN")
        @Role("MANAGER")
        public void manageUsers() {
            System.out.println("Managing users (requires ADMIN or MANAGER)");
        }
    }

    /**
     * Event handler with multiple listeners
     */
    @Component
    public static class UserEventHandler {
        
        @EventListener(eventType = "UserCreated", priority = 1, async = true)
        @EventListener(eventType = "UserRegistered", priority = 1, async = true)
        @EventListener(eventType = "UserSignedUp", priority = 1, async = true)
        public void handleUserCreation(String userId) {
            System.out.println("Handling user creation events for: " + userId);
        }
        
        @EventListener(eventType = "UserUpdated", priority = 2)
        @EventListener(eventType = "UserModified", priority = 2)
        public void handleUserUpdate(String userId) {
            System.out.println("Handling user update events for: " + userId);
        }
    }

    /**
     * Service with multiple cache configurations
     */
    @Service
    @Cache(name = "users", key = "#userId", ttl = 600)
    @Cache(name = "userStats", key = "#userId + '_stats'", ttl = 300)
    public static class UserCacheService {
        
        @Cache(name = "userDetails", key = "#id", ttl = 1800)
        @Cache(name = "userProfile", key = "#id + '_profile'", ttl = 900)
        public User getUserDetails(Long id) {
            System.out.println("Fetching user details from database: " + id);
            return new User();
        }
    }

    /**
     * Service with multiple retry configurations
     */
    @Service
    public static class ExternalApiService {
        
        @Retry(exception = java.io.IOException.class, maxAttempts = 3, backoff = 1000)
        @Retry(exception = java.net.SocketTimeoutException.class, maxAttempts = 5, backoff = 2000)
        @Retry(exception = Exception.class, maxAttempts = 2, backoff = 500)
        public String callExternalApi(String endpoint) {
            System.out.println("Calling external API: " + endpoint);
            return "API Response";
        }
    }

    /**
     * Utility to inspect repeatable annotations
     */
    @Component
    public static class AnnotationInspector {
        
        public void inspectSchedules(Object bean) {
            Class<?> clazz = bean.getClass();
            
            for (java.lang.reflect.Method method : clazz.getDeclaredMethods()) {
                // Access repeatable annotations
                Schedule[] schedules = method.getAnnotationsByType(Schedule.class);
                
                if (schedules.length > 0) {
                    System.out.println("\nMethod: " + method.getName());
                    System.out.println("Schedules found: " + schedules.length);
                    
                    for (int i = 0; i < schedules.length; i++) {
                        Schedule schedule = schedules[i];
                        System.out.println("  Schedule " + (i + 1) + ":");
                        if (!schedule.cron().isEmpty()) {
                            System.out.println("    Cron: " + schedule.cron());
                            System.out.println("    Zone: " + schedule.zone());
                        }
                        if (schedule.fixedRate() > 0) {
                            System.out.println("    Fixed Rate: " + schedule.fixedRate() + "ms");
                        }
                        if (schedule.fixedDelay() > 0) {
                            System.out.println("    Fixed Delay: " + schedule.fixedDelay() + "ms");
                        }
                    }
                }
            }
        }
        
        public void inspectValidationRules(Class<?> clazz) {
            System.out.println("\nValidation rules for: " + clazz.getSimpleName());
            
            for (java.lang.reflect.Field field : clazz.getDeclaredFields()) {
                ValidationRule[] rules = field.getAnnotationsByType(ValidationRule.class);
                
                if (rules.length > 0) {
                    System.out.println("  Field: " + field.getName());
                    System.out.println("  Rules: " + rules.length);
                    
                    for (ValidationRule rule : rules) {
                        System.out.println("    - " + rule.type() + 
                                         (!rule.param().isEmpty() ? " (" + rule.param() + ")" : "") +
                                         ": " + rule.message());
                    }
                }
            }
        }
        
        public void inspectRoles(Class<?> clazz) {
            Role[] classRoles = clazz.getAnnotationsByType(Role.class);
            
            if (classRoles.length > 0) {
                System.out.println("\nClass roles for: " + clazz.getSimpleName());
                for (Role role : classRoles) {
                    System.out.println("  - " + role.value() + " (level " + role.level() + ")");
                }
            }
            
            for (java.lang.reflect.Method method : clazz.getDeclaredMethods()) {
                Role[] methodRoles = method.getAnnotationsByType(Role.class);
                
                if (methodRoles.length > 0) {
                    System.out.println("\nMethod roles for: " + method.getName());
                    for (Role role : methodRoles) {
                        System.out.println("  - " + role.value() + " (level " + role.level() + ")");
                    }
                }
            }
        }
    }
}

/**
 * DOCUMENTATION
 * 
 * Repeatable Annotation Pattern:
 * 
 * 1. What is @Repeatable?
 *    - Java 8 feature allowing multiple instances of same annotation
 *    - Requires container annotation
 *    - Simplifies syntax vs array-based annotations
 *    - Example: @Schedule @Schedule vs @Schedules({@Schedule, @Schedule})
 * 
 * 2. Creating Repeatable Annotations:
 *    Step 1: Create base annotation
 *    Step 2: Create container annotation with value() returning array
 *    Step 3: Annotate base with @Repeatable(Container.class)
 *    Step 4: Ensure retention and targets match
 * 
 * 3. Requirements:
 *    - Container annotation must have value() method
 *    - value() must return array of base annotation
 *    - @Retention must match (typically RUNTIME)
 *    - @Target must be compatible
 *    - Container name conventionally plural
 * 
 * 4. Accessing Repeatable Annotations:
 *    - Method.getAnnotationsByType(Schedule.class)
 *      Returns all instances including container
 *    - Method.getAnnotation(Schedules.class)
 *      Returns container annotation
 *    - Method.getAnnotation(Schedule.class)
 *      Returns single instance or null if multiple
 * 
 * 5. Common Use Cases:
 *    - Multiple schedules: Different cron expressions
 *    - Multiple validations: Combined validation rules
 *    - Multiple roles: Alternative permissions
 *    - Multiple event listeners: Same handler for multiple events
 *    - Multiple caches: Multi-level caching
 *    - Multiple retries: Different exception types
 * 
 * 6. Benefits:
 *    - Cleaner syntax than arrays
 *    - More readable code
 *    - Better IDE support
 *    - Natural grouping
 *    - Easier to add/remove instances
 * 
 * 7. Old Way (Pre-Java 8):
 *    @Schedules({
 *        @Schedule(cron = "0 0 0 * * *"),
 *        @Schedule(cron = "0 0 12 * * *")
 *    })
 *    
 *    New Way (Java 8+):
 *    @Schedule(cron = "0 0 0 * * *")
 *    @Schedule(cron = "0 0 12 * * *")
 * 
 * 8. Container Annotation Pattern:
 *    @interface Schedule { ... }
 *    
 *    @interface Schedules {
 *        Schedule[] value();  // REQUIRED
 *    }
 *    
 *    @Repeatable(Schedules.class)
 *    @interface Schedule { ... }
 * 
 * 9. Reflection Access:
 *    // Get all instances
 *    Schedule[] schedules = method.getAnnotationsByType(Schedule.class);
 *    
 *    // Check if present
 *    if (method.isAnnotationPresent(Schedules.class)) {
 *        Schedules container = method.getAnnotation(Schedules.class);
 *        for (Schedule s : container.value()) {
 *            // Process
 *        }
 *    }
 * 
 * 10. Best Practices:
 *     - Use plural name for container
 *     - Match retention policies
 *     - Match target types
 *     - Document both annotations
 *     - Provide examples in JavaDoc
 *     - Use getAnnotationsByType() for access
 * 
 * 11. Spring Integration:
 *     - Spring processes repeatable annotations
 *     - Works with component scanning
 *     - Supported in AOP pointcuts
 *     - Can be used in custom processors
 * 
 * 12. Testing:
 *     - Verify multiple instances are retained
 *     - Test reflection access methods
 *     - Check container annotation presence
 *     - Validate attribute values
 *     - Test order preservation (if relevant)
 */
