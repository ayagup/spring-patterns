package com.example.customannotation;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.lang.annotation.*;
import java.lang.reflect.Method;

/**
 * Annotation Processor Pattern
 * 
 * Demonstrates creating custom annotation processors that execute logic when
 * annotations are detected. Processors can:
 * - Process annotations at runtime
 * - Modify bean behavior
 * - Validate configuration
 * - Initialize components
 * - Register metadata
 * 
 * Key Features:
 * - BeanPostProcessor implementation
 * - Reflection-based processing
 * - Bean lifecycle hooks
 * - Custom initialization logic
 * - Metadata registration
 * 
 * Use Cases:
 * - Custom initialization
 * - Validation enforcement
 * - Metadata collection
 * - Aspect-like behavior
 * - Configuration validation
 * 
 * @author Spring Patterns
 */
@SpringBootApplication
public class AnnotationProcessorPattern {

    public static void main(String[] args) {
        SpringApplication.run(AnnotationProcessorPattern.class, args);
    }

    // =========================================================================
    // CUSTOM ANNOTATIONS TO PROCESS
    // =========================================================================

    /**
     * Annotation for methods that should be logged
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    public @interface Logged {
        
        /**
         * Log level
         */
        LogLevel level() default LogLevel.INFO;
        
        /**
         * Whether to log method parameters
         */
        boolean logParameters() default true;
        
        /**
         * Whether to log return value
         */
        boolean logReturnValue() default true;
    }

    public enum LogLevel {
        DEBUG, INFO, WARN, ERROR
    }

    /**
     * Annotation for initialization methods
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    public @interface Initialize {
        
        /**
         * Initialization priority (lower = earlier)
         */
        int priority() default 0;
        
        /**
         * Whether initialization is required
         */
        boolean required() default true;
    }

    /**
     * Annotation for methods that should be monitored
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    public @interface Monitored {
        
        /**
         * Metric name
         */
        String metric() default "";
        
        /**
         * Whether to track execution time
         */
        boolean trackTime() default true;
        
        /**
         * Whether to track invocation count
         */
        boolean trackCount() default true;
    }

    /**
     * Annotation for methods that should be validated
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    public @interface Validated {
        
        /**
         * Validation groups
         */
        String[] groups() default {};
    }

    /**
     * Annotation for classes that need registration
     */
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    public @interface Registered {
        
        /**
         * Registry name
         */
        String value() default "";
        
        /**
         * Registration priority
         */
        int priority() default 0;
    }

    /**
     * Annotation for cacheable components
     */
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    public @interface Cacheable {
        
        /**
         * Cache name
         */
        String value();
        
        /**
         * Cache TTL in seconds
         */
        int ttl() default 300;
    }

    // =========================================================================
    // ANNOTATION PROCESSORS
    // =========================================================================

    /**
     * Processor for @Initialize annotations
     */
    @Component
    public static class InitializeAnnotationProcessor implements BeanPostProcessor {
        
        @Override
        public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
            Class<?> clazz = bean.getClass();
            
            // Find all methods with @Initialize
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.isAnnotationPresent(Initialize.class)) {
                    Initialize init = method.getAnnotation(Initialize.class);
                    
                    System.out.println("\n[InitializeProcessor] Processing @Initialize on " + 
                                     beanName + "." + method.getName());
                    System.out.println("  Priority: " + init.priority());
                    System.out.println("  Required: " + init.required());
                    
                    try {
                        method.setAccessible(true);
                        method.invoke(bean);
                        System.out.println("  ✓ Initialization completed");
                    } catch (Exception e) {
                        if (init.required()) {
                            throw new RuntimeException("Required initialization failed: " + 
                                                     method.getName(), e);
                        } else {
                            System.out.println("  ⚠ Optional initialization failed: " + e.getMessage());
                        }
                    }
                }
            }
            
            return bean;
        }
    }

    /**
     * Processor for @Logged annotations
     */
    @Component
    public static class LoggedAnnotationProcessor implements BeanPostProcessor {
        
        @Override
        public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
            Class<?> clazz = bean.getClass();
            
            // Scan for @Logged methods
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.isAnnotationPresent(Logged.class)) {
                    Logged logged = method.getAnnotation(Logged.class);
                    
                    System.out.println("\n[LoggedProcessor] Detected @Logged on " + 
                                     beanName + "." + method.getName());
                    System.out.println("  Log Level: " + logged.level());
                    System.out.println("  Log Parameters: " + logged.logParameters());
                    System.out.println("  Log Return Value: " + logged.logReturnValue());
                    
                    // In real implementation: wrap with logging proxy
                }
            }
            
            return bean;
        }
    }

    /**
     * Processor for @Monitored annotations
     */
    @Component
    public static class MonitoredAnnotationProcessor implements BeanPostProcessor {
        
        @Override
        public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
            Class<?> clazz = bean.getClass();
            
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.isAnnotationPresent(Monitored.class)) {
                    Monitored monitored = method.getAnnotation(Monitored.class);
                    
                    String metricName = monitored.metric().isEmpty() ? 
                            method.getName() : monitored.metric();
                    
                    System.out.println("\n[MonitoredProcessor] Registering monitoring for " + 
                                     beanName + "." + method.getName());
                    System.out.println("  Metric Name: " + metricName);
                    System.out.println("  Track Time: " + monitored.trackTime());
                    System.out.println("  Track Count: " + monitored.trackCount());
                    
                    // In real implementation: register with metrics system
                }
            }
            
            return bean;
        }
    }

    /**
     * Processor for @Registered annotations
     */
    @Component
    public static class RegisteredAnnotationProcessor implements BeanPostProcessor {
        
        private final ComponentRegistry registry = new ComponentRegistry();
        
        @Override
        public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
            Class<?> clazz = bean.getClass();
            
            if (clazz.isAnnotationPresent(Registered.class)) {
                Registered registered = clazz.getAnnotation(Registered.class);
                
                String registryName = registered.value().isEmpty() ? 
                        clazz.getSimpleName() : registered.value();
                
                System.out.println("\n[RegisteredProcessor] Registering component: " + registryName);
                System.out.println("  Bean Name: " + beanName);
                System.out.println("  Priority: " + registered.priority());
                
                registry.register(registryName, bean, registered.priority());
            }
            
            return bean;
        }
        
        public ComponentRegistry getRegistry() {
            return registry;
        }
    }

    /**
     * Processor for @Cacheable annotations
     */
    @Component
    public static class CacheableAnnotationProcessor implements BeanPostProcessor {
        
        @Override
        public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
            Class<?> clazz = bean.getClass();
            
            if (clazz.isAnnotationPresent(Cacheable.class)) {
                Cacheable cacheable = clazz.getAnnotation(Cacheable.class);
                
                System.out.println("\n[CacheableProcessor] Setting up cache for: " + beanName);
                System.out.println("  Cache Name: " + cacheable.value());
                System.out.println("  TTL: " + cacheable.ttl() + " seconds");
                
                // In real implementation: configure cache manager
            }
            
            return bean;
        }
    }

    /**
     * Processor for @Validated annotations
     */
    @Component
    public static class ValidatedAnnotationProcessor implements BeanPostProcessor {
        
        @Override
        public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
            Class<?> clazz = bean.getClass();
            
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.isAnnotationPresent(Validated.class)) {
                    Validated validated = method.getAnnotation(Validated.class);
                    
                    System.out.println("\n[ValidatedProcessor] Setting up validation for " + 
                                     beanName + "." + method.getName());
                    System.out.println("  Validation Groups: " + 
                                     String.join(", ", validated.groups()));
                    
                    // In real implementation: set up method interceptor
                }
            }
            
            return bean;
        }
    }

    // =========================================================================
    // USAGE EXAMPLES
    // =========================================================================

    /**
     * Service with initialization
     */
    @Service
    @Registered(value = "DataService", priority = 1)
    public static class DataService {
        
        private boolean initialized = false;
        
        @Initialize(priority = 1, required = true)
        public void initializeDatabase() {
            System.out.println("  → Initializing database connection");
            initialized = true;
        }
        
        @Initialize(priority = 2, required = false)
        public void loadCachedData() {
            System.out.println("  → Loading cached data");
        }
        
        @Logged(level = LogLevel.INFO, logParameters = true)
        @Monitored(metric = "data.fetch", trackTime = true)
        public String fetchData(String id) {
            System.out.println("Fetching data for ID: " + id);
            return "Data for " + id;
        }
    }

    /**
     * Service with monitoring
     */
    @Service
    @Cacheable(value = "userCache", ttl = 600)
    public static class UserService {
        
        @Initialize(priority = 1)
        public void setup() {
            System.out.println("  → Setting up UserService");
        }
        
        @Logged(level = LogLevel.DEBUG)
        @Monitored(trackTime = true, trackCount = true)
        public User getUser(Long id) {
            System.out.println("Getting user: " + id);
            return new User(id, "User" + id);
        }
        
        @Validated(groups = {"Create", "Update"})
        @Monitored(metric = "user.save")
        public void saveUser(User user) {
            System.out.println("Saving user: " + user.getUsername());
        }
    }

    /**
     * Service with registration
     */
    @Service
    @Registered(value = "NotificationService", priority = 2)
    public static class NotificationService {
        
        @Initialize(priority = 1)
        public void initializeChannels() {
            System.out.println("  → Initializing notification channels");
        }
        
        @Logged(level = LogLevel.INFO, logReturnValue = false)
        @Monitored(metric = "notification.send", trackCount = true)
        public void sendNotification(String userId, String message) {
            System.out.println("Sending notification to " + userId + ": " + message);
        }
    }

    /**
     * Component registry
     */
    public static class ComponentRegistry {
        
        private final java.util.Map<String, ComponentInfo> components = 
                new java.util.LinkedHashMap<>();
        
        public void register(String name, Object component, int priority) {
            components.put(name, new ComponentInfo(component, priority));
            System.out.println("  ✓ Registered in component registry");
        }
        
        public Object get(String name) {
            ComponentInfo info = components.get(name);
            return info != null ? info.component : null;
        }
        
        public void printRegistry() {
            System.out.println("\n=== Component Registry ===");
            components.forEach((name, info) -> {
                System.out.println("  " + name + " (priority: " + info.priority + ")");
            });
        }
        
        private static class ComponentInfo {
            final Object component;
            final int priority;
            
            ComponentInfo(Object component, int priority) {
                this.component = component;
                this.priority = priority;
            }
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
}

/**
 * DOCUMENTATION
 * 
 * Annotation Processor Pattern:
 * 
 * 1. What is an Annotation Processor?
 *    - Component that processes custom annotations at runtime
 *    - Implements BeanPostProcessor for Spring beans
 *    - Executes logic when annotations are detected
 *    - Can modify, validate, or enhance beans
 * 
 * 2. BeanPostProcessor Interface:
 *    - postProcessBeforeInitialization(bean, beanName)
 *      Called before bean initialization methods
 *    - postProcessAfterInitialization(bean, beanName)
 *      Called after bean initialization
 *    - Return bean (possibly wrapped/modified)
 * 
 * 3. Processing Steps:
 *    1. Get bean class: bean.getClass()
 *    2. Scan for annotations: class.getDeclaredMethods()
 *    3. Check annotation presence: method.isAnnotationPresent()
 *    4. Get annotation: method.getAnnotation()
 *    5. Read attributes: annotation.attribute()
 *    6. Execute logic based on attributes
 *    7. Return bean (or proxy)
 * 
 * 4. Common Use Cases:
 *    - Custom initialization: @Initialize methods
 *    - Logging setup: @Logged methods
 *    - Monitoring registration: @Monitored methods
 *    - Validation setup: @Validated methods
 *    - Component registration: @Registered classes
 *    - Cache configuration: @Cacheable classes
 * 
 * 5. Reflection API:
 *    - Class.getDeclaredMethods(): Get all methods
 *    - Method.isAnnotationPresent(X.class): Check if annotated
 *    - Method.getAnnotation(X.class): Get annotation instance
 *    - Method.setAccessible(true): Access private methods
 *    - Method.invoke(object, args): Invoke method
 * 
 * 6. Processing Patterns:
 *    
 *    Method-level Processing:
 *    for (Method method : clazz.getDeclaredMethods()) {
 *        if (method.isAnnotationPresent(MyAnnotation.class)) {
 *            MyAnnotation ann = method.getAnnotation(MyAnnotation.class);
 *            // Process annotation
 *        }
 *    }
 *    
 *    Class-level Processing:
 *    if (clazz.isAnnotationPresent(MyAnnotation.class)) {
 *        MyAnnotation ann = clazz.getAnnotation(MyAnnotation.class);
 *        // Process annotation
 *    }
 * 
 * 7. Advanced Techniques:
 *    - Create proxies for method interception
 *    - Register with external systems
 *    - Build metadata indexes
 *    - Validate configuration
 *    - Initialize components
 * 
 * 8. Best Practices:
 *    - Process in postProcessAfterInitialization
 *    - Handle exceptions gracefully
 *    - Log processing actions
 *    - Don't modify bean structure
 *    - Return original bean or proxy
 *    - Document processor behavior
 * 
 * 9. Performance Considerations:
 *    - Processors run for every bean
 *    - Keep processing logic fast
 *    - Cache reflection results
 *    - Filter beans early
 *    - Avoid heavy computation
 * 
 * 10. Spring Integration:
 *     - Automatically detected if @Component
 *     - Runs during application startup
 *     - Processes all beans in context
 *     - Respects bean dependencies
 *     - Can be ordered with @Order
 * 
 * 11. Testing:
 *     - Unit test processor logic
 *     - Mock beans for testing
 *     - Verify annotation detection
 *     - Test attribute reading
 *     - Check side effects
 * 
 * 12. Real-World Applications:
 *     - Spring's @Scheduled processor
 *     - Spring's @Cacheable processor
 *     - Spring's @Async processor
 *     - Custom initialization
 *     - Metrics registration
 *     - Audit logging setup
 */
