package com.example.beanpostprocessor;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.annotation.*;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Custom Annotation Processor Pattern
 * 
 * Demonstrates using BeanPostProcessor to process custom annotations
 * and apply cross-cutting concerns based on annotation presence.
 * 
 * Key Concepts:
 * - Custom annotations
 * - Annotation-driven configuration
 * - Runtime annotation processing
 * - Proxy-based implementation
 * - Cross-cutting concerns
 * 
 * Use Cases:
 * - Custom logging annotation
 * - Performance monitoring annotation
 * - Caching annotation
 * - Retry annotation
 * - Validation annotation
 */
@SpringBootApplication
public class CustomAnnotationProcessorPattern {

    public static void main(String[] args) {
        SpringApplication.run(CustomAnnotationProcessorPattern.class, args);
    }
}

/**
 * Custom annotation for logging
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@interface LogExecutionTime {
    String value() default "";
    boolean includeArgs() default false;
}

/**
 * Custom annotation for retry logic
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@interface Retry {
    int maxAttempts() default 3;
    long delay() default 1000;
}

/**
 * Custom annotation for caching
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@interface Cacheable {
    String value() default "";
    int ttl() default 300; // seconds
}

/**
 * BeanPostProcessor for @LogExecutionTime annotation
 */
@Component
class LogExecutionTimeBeanPostProcessor implements BeanPostProcessor {

    private static final List<String> loggedBeans = new ArrayList<>();

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = bean.getClass();
        
        // Check if class has @LogExecutionTime annotation
        if (beanClass.isAnnotationPresent(LogExecutionTime.class)) {
            loggedBeans.add(beanName);
            System.out.println("Enabling execution time logging for: " + beanName);
            
            // In production, would create proxy here
            // For demonstration, just log
        }
        
        return bean;
    }

    public static List<String> getLoggedBeans() {
        return new ArrayList<>(loggedBeans);
    }
}

/**
 * BeanPostProcessor for @Retry annotation
 */
@Component
class RetryBeanPostProcessor implements BeanPostProcessor {

    private static final List<String> retryEnabledBeans = new ArrayList<>();

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = bean.getClass();
        
        if (beanClass.isAnnotationPresent(Retry.class)) {
            Retry retry = beanClass.getAnnotation(Retry.class);
            retryEnabledBeans.add(beanName + " (max=" + retry.maxAttempts() + ", delay=" + retry.delay() + "ms)");
            System.out.println("Enabling retry for: " + beanName + 
                             " with maxAttempts=" + retry.maxAttempts() + 
                             ", delay=" + retry.delay() + "ms");
        }
        
        return bean;
    }

    public static List<String> getRetryEnabledBeans() {
        return new ArrayList<>(retryEnabledBeans);
    }
}

/**
 * BeanPostProcessor for @Cacheable annotation
 */
@Component
class CacheableBeanPostProcessor implements BeanPostProcessor {

    private static final List<String> cacheableBeans = new ArrayList<>();

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = bean.getClass();
        
        if (beanClass.isAnnotationPresent(Cacheable.class)) {
            Cacheable cacheable = beanClass.getAnnotation(Cacheable.class);
            cacheableBeans.add(beanName + " (ttl=" + cacheable.ttl() + "s)");
            System.out.println("Enabling caching for: " + beanName + 
                             " with TTL=" + cacheable.ttl() + "s");
        }
        
        return bean;
    }

    public static List<String> getCacheableBeans() {
        return new ArrayList<>(cacheableBeans);
    }
}

/**
 * Service with @LogExecutionTime annotation
 */
@Component
@LogExecutionTime("UserService operations")
class AnnotatedUserService {
    
    public String getUser(String id) {
        return "User: " + id;
    }
    
    public List<String> getAllUsers() {
        return List.of("user1", "user2", "user3");
    }
}

/**
 * Service with @Retry annotation
 */
@Component
@Retry(maxAttempts = 5, delay = 2000)
class RetryablePaymentService {
    
    public String processPayment(String paymentId) {
        return "Payment processed: " + paymentId;
    }
    
    public boolean validateCard(String cardNumber) {
        return cardNumber != null && cardNumber.length() == 16;
    }
}

/**
 * Service with @Cacheable annotation
 */
@Component
@Cacheable(value = "products", ttl = 600)
class CachedProductService {
    
    public Map<String, String> getProduct(String id) {
        return Map.of(
                "id", id,
                "name", "Product " + id,
                "price", "99.99"
        );
    }
    
    public List<String> getPopularProducts() {
        return List.of("product1", "product2", "product3");
    }
}

/**
 * Service with multiple annotations
 */
@Component
@LogExecutionTime
@Retry(maxAttempts = 3)
@Cacheable(ttl = 300)
class MultiAnnotatedService {
    
    public String complexOperation(String input) {
        return "Processed: " + input;
    }
}

/**
 * Controller to demonstrate annotation processing
 */
@RestController
class CustomAnnotationController {

    private final AnnotatedUserService userService;
    private final RetryablePaymentService paymentService;
    private final CachedProductService productService;
    private final MultiAnnotatedService multiService;

    public CustomAnnotationController(AnnotatedUserService userService,
                                     RetryablePaymentService paymentService,
                                     CachedProductService productService,
                                     MultiAnnotatedService multiService) {
        this.userService = userService;
        this.paymentService = paymentService;
        this.productService = productService;
        this.multiService = multiService;
    }

    @GetMapping("/annotations/logged-beans")
    public List<String> getLoggedBeans() {
        return LogExecutionTimeBeanPostProcessor.getLoggedBeans();
    }

    @GetMapping("/annotations/retry-enabled")
    public List<String> getRetryEnabledBeans() {
        return RetryBeanPostProcessor.getRetryEnabledBeans();
    }

    @GetMapping("/annotations/cacheable-beans")
    public List<String> getCacheableBeans() {
        return CacheableBeanPostProcessor.getCacheableBeans();
    }

    @GetMapping("/annotations/user-service")
    public Map<String, Object> testUserService() {
        return Map.of(
                "user", userService.getUser("123"),
                "allUsers", userService.getAllUsers()
        );
    }

    @GetMapping("/annotations/payment-service")
    public Map<String, Object> testPaymentService() {
        return Map.of(
                "payment", paymentService.processPayment("PAY123"),
                "cardValid", paymentService.validateCard("1234567890123456")
        );
    }

    @GetMapping("/annotations/product-service")
    public Map<String, Object> testProductService() {
        return Map.of(
                "product", productService.getProduct("P123"),
                "popularProducts", productService.getPopularProducts()
        );
    }

    @GetMapping("/annotations/multi-service")
    public Map<String, String> testMultiService() {
        return Map.of(
                "result", multiService.complexOperation("test data")
        );
    }
}

/**
 * Documentation:
 * 
 * Custom Annotation Processing Flow:
 * 1. Define custom annotation with @interface
 * 2. Create BeanPostProcessor to detect annotation
 * 3. Apply behavior based on annotation presence
 * 4. Optionally create proxy for method interception
 * 
 * Creating Custom Annotations:
 * 
 * @Target: Where annotation can be applied
 * - ElementType.TYPE: Class, interface, enum
 * - ElementType.METHOD: Method
 * - ElementType.FIELD: Field
 * - ElementType.PARAMETER: Method parameter
 * - ElementType.CONSTRUCTOR: Constructor
 * - ElementType.PACKAGE: Package
 * 
 * @Retention: When annotation is available
 * - RetentionPolicy.SOURCE: Compile time only
 * - RetentionPolicy.CLASS: Class file, not runtime
 * - RetentionPolicy.RUNTIME: Available at runtime (use this)
 * 
 * @Documented: Include in JavaDoc
 * 
 * @Inherited: Subclasses inherit annotation
 * 
 * Annotation Elements:
 * @interface MyAnnotation {
 *     String value() default "";
 *     int priority() default 0;
 *     String[] tags() default {};
 * }
 * 
 * Detecting Annotations in BeanPostProcessor:
 * 
 * // Class-level
 * if (bean.getClass().isAnnotationPresent(MyAnnotation.class)) {
 *     MyAnnotation ann = bean.getClass().getAnnotation(MyAnnotation.class);
 *     String value = ann.value();
 * }
 * 
 * // Method-level
 * for (Method method : bean.getClass().getDeclaredMethods()) {
 *     if (method.isAnnotationPresent(MyAnnotation.class)) {
 *         MyAnnotation ann = method.getAnnotation(MyAnnotation.class);
 *         // Process method
 *     }
 * }
 * 
 * Creating Proxies for Method Interception:
 * 
 * return Proxy.newProxyInstance(
 *     bean.getClass().getClassLoader(),
 *     bean.getClass().getInterfaces(),
 *     (proxy, method, args) -> {
 *         // Before method execution
 *         Object result = method.invoke(bean, args);
 *         // After method execution
 *         return result;
 *     }
 * );
 * 
 * Using CGLIB for Class Proxying:
 * 
 * Enhancer enhancer = new Enhancer();
 * enhancer.setSuperclass(bean.getClass());
 * enhancer.setCallback((MethodInterceptor) (obj, method, args, proxy) -> {
 *     // Before
 *     Object result = proxy.invokeSuper(obj, args);
 *     // After
 *     return result;
 * });
 * return enhancer.create();
 * 
 * Common Patterns:
 * 
 * 1. Logging:
 *    @LogExecutionTime
 *    - Log method execution time
 *    - Log arguments and return values
 * 
 * 2. Retry:
 *    @Retry(maxAttempts = 3)
 *    - Retry failed operations
 *    - Exponential backoff
 * 
 * 3. Caching:
 *    @Cacheable(ttl = 300)
 *    - Cache method results
 *    - TTL-based expiration
 * 
 * 4. Validation:
 *    @Validate
 *    - Validate method parameters
 *    - Validate return values
 * 
 * 5. Security:
 *    @RequiresRole("ADMIN")
 *    - Check user permissions
 *    - Enforce access control
 * 
 * 6. Async:
 *    @AsyncExecute
 *    - Run method asynchronously
 *    - Return CompletableFuture
 * 
 * 7. Metrics:
 *    @Monitored
 *    - Collect method metrics
 *    - Send to monitoring system
 * 
 * Best Practices:
 * - Keep annotations focused
 * - Use meaningful names
 * - Document annotation purpose
 * - Provide sensible defaults
 * - Use type-safe elements
 * - Consider inheritance
 * - Handle null safely
 * - Validate annotation values
 * 
 * Advanced Techniques:
 * 
 * 1. Meta-Annotations:
 *    @Target(ElementType.TYPE)
 *    @Retention(RetentionPolicy.RUNTIME)
 *    @LogExecutionTime
 *    @Cacheable
 *    @interface Service { }
 * 
 * 2. Annotation Inheritance:
 *    @Inherited
 *    @interface MyAnnotation { }
 * 
 * 3. Annotation Arrays:
 *    @Repeatable(MyAnnotations.class)
 *    @interface MyAnnotation { }
 * 
 *    @interface MyAnnotations {
 *        MyAnnotation[] value();
 *    }
 * 
 * Testing:
 * - Test annotation detection
 * - Verify behavior is applied
 * - Mock annotation processing
 * - Test with/without annotations
 * 
 * Performance:
 * - Reflection can be expensive
 * - Cache annotation lookups
 * - Minimize proxy overhead
 * - Consider AspectJ for compile-time weaving
 * 
 * Limitations:
 * - Cannot modify final methods
 * - Reflection overhead
 * - Proxy limitations (interface vs class)
 * - Class loading complexity
 */
