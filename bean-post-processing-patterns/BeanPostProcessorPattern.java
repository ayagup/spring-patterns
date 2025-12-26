package com.example.beanpostprocessor;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Bean Post Processor Pattern
 * 
 * Demonstrates using BeanPostProcessor to customize bean initialization.
 * This is a fundamental Spring pattern for cross-cutting concerns and
 * bean modification before/after initialization.
 * 
 * Key Concepts:
 * - BeanPostProcessor interface
 * - postProcessBeforeInitialization
 * - postProcessAfterInitialization
 * - Bean lifecycle customization
 * - Cross-cutting concerns
 * 
 * Use Cases:
 * - Logging bean initialization
 * - Validation of beans
 * - Wrapping beans with proxies
 * - Custom annotation processing
 * - Performance monitoring
 */
@SpringBootApplication
public class BeanPostProcessorPattern {

    public static void main(String[] args) {
        SpringApplication.run(BeanPostProcessorPattern.class, args);
    }
}

/**
 * Custom BeanPostProcessor for logging
 */
@Component
class LoggingBeanPostProcessor implements BeanPostProcessor {

    private static final List<String> initializationLog = new ArrayList<>();

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        String log = "BEFORE initialization of bean: " + beanName;
        initializationLog.add(log);
        System.out.println(log);
        return bean; // Return the bean unchanged
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        String log = "AFTER initialization of bean: " + beanName;
        initializationLog.add(log);
        System.out.println(log);
        return bean; // Return the bean unchanged
    }

    public static List<String> getInitializationLog() {
        return new ArrayList<>(initializationLog);
    }
}

/**
 * Validation BeanPostProcessor
 * Validates beans that implement Validatable interface
 */
@Component
class ValidationBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof Validatable) {
            Validatable validatable = (Validatable) bean;
            if (!validatable.isValid()) {
                throw new IllegalStateException("Bean " + beanName + " failed validation");
            }
            System.out.println("Bean " + beanName + " validated successfully");
        }
        return bean;
    }
}

/**
 * Interface for validatable beans
 */
interface Validatable {
    boolean isValid();
}

/**
 * Auditing BeanPostProcessor
 * Wraps beans that implement Auditable with audit logging
 */
@Component
class AuditingBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof Auditable) {
            System.out.println("Registering auditing for bean: " + beanName);
            // In real scenario, might wrap with proxy for method-level auditing
            ((Auditable) bean).enableAuditing();
        }
        return bean;
    }
}

/**
 * Interface for auditable beans
 */
interface Auditable {
    void enableAuditing();
    boolean isAuditingEnabled();
}

/**
 * Performance monitoring BeanPostProcessor
 */
@Component
class PerformanceBeanPostProcessor implements BeanPostProcessor {

    private final List<String> monitoredBeans = new ArrayList<>();

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        // Monitor service beans
        if (beanName.endsWith("Service")) {
            monitoredBeans.add(beanName);
            System.out.println("Performance monitoring enabled for: " + beanName);
            // In real scenario, would create proxy for method timing
        }
        return bean;
    }

    public List<String> getMonitoredBeans() {
        return new ArrayList<>(monitoredBeans);
    }
}

/**
 * Sample service bean
 */
@Component
class UserService implements Validatable, Auditable {

    private String serviceName = "UserService";
    private boolean auditingEnabled = false;

    @PostConstruct
    public void init() {
        System.out.println(serviceName + " initialized");
    }

    public String getServiceName() {
        return serviceName;
    }

    @Override
    public boolean isValid() {
        return serviceName != null && !serviceName.isEmpty();
    }

    @Override
    public void enableAuditing() {
        this.auditingEnabled = true;
    }

    @Override
    public boolean isAuditingEnabled() {
        return auditingEnabled;
    }
}

/**
 * Sample service bean
 */
@Component
class ProductService implements Validatable {

    private String serviceName = "ProductService";

    @PostConstruct
    public void init() {
        System.out.println(serviceName + " initialized");
    }

    public String getServiceName() {
        return serviceName;
    }

    @Override
    public boolean isValid() {
        return serviceName != null;
    }
}

/**
 * Controller to demonstrate BeanPostProcessor effects
 */
@RestController
class BeanPostProcessorController {

    private final UserService userService;
    private final ProductService productService;
    private final PerformanceBeanPostProcessor performanceProcessor;

    public BeanPostProcessorController(UserService userService,
                                      ProductService productService,
                                      PerformanceBeanPostProcessor performanceProcessor) {
        this.userService = userService;
        this.productService = productService;
        this.performanceProcessor = performanceProcessor;
    }

    @GetMapping("/bpp/init-log")
    public List<String> getInitializationLog() {
        return LoggingBeanPostProcessor.getInitializationLog();
    }

    @GetMapping("/bpp/monitored-beans")
    public List<String> getMonitoredBeans() {
        return performanceProcessor.getMonitoredBeans();
    }

    @GetMapping("/bpp/user-service")
    public Map<String, Object> getUserServiceInfo() {
        return Map.of(
                "serviceName", userService.getServiceName(),
                "valid", userService.isValid(),
                "auditingEnabled", userService.isAuditingEnabled()
        );
    }

    @GetMapping("/bpp/product-service")
    public Map<String, Object> getProductServiceInfo() {
        return Map.of(
                "serviceName", productService.getServiceName(),
                "valid", productService.isValid()
        );
    }
}

/**
 * Documentation:
 * 
 * BeanPostProcessor Interface:
 * - Allows custom modification of new bean instances
 * - Called for EVERY bean in the ApplicationContext
 * - Two callback methods:
 *   1. postProcessBeforeInitialization: Before @PostConstruct, InitializingBean.afterPropertiesSet()
 *   2. postProcessAfterInitialization: After initialization callbacks
 * 
 * Bean Lifecycle Order:
 * 1. Bean instantiation
 * 2. Dependency injection
 * 3. postProcessBeforeInitialization (all BeanPostProcessors)
 * 4. @PostConstruct methods
 * 5. InitializingBean.afterPropertiesSet()
 * 6. @Bean(initMethod = "init")
 * 7. postProcessAfterInitialization (all BeanPostProcessors)
 * 8. Bean ready to use
 * 
 * Registration:
 * 
 * 1. Via @Component:
 *    @Component
 *    class MyBeanPostProcessor implements BeanPostProcessor { }
 * 
 * 2. Via @Bean:
 *    @Configuration
 *    class Config {
 *        @Bean
 *        public BeanPostProcessor myProcessor() {
 *            return new MyBeanPostProcessor();
 *        }
 *    }
 * 
 * 3. Programmatically:
 *    context.getBeanFactory().addBeanPostProcessor(new MyBeanPostProcessor());
 * 
 * Method Signatures:
 * 
 * Object postProcessBeforeInitialization(Object bean, String beanName)
 * - Called before initialization callbacks
 * - Return: modified bean instance or original
 * - Can return different object (proxy)
 * 
 * Object postProcessAfterInitialization(Object bean, String beanName)
 * - Called after initialization callbacks
 * - Perfect place for wrapping with proxies
 * - Most AOP proxies created here
 * 
 * Common Use Cases:
 * 
 * 1. Logging:
 *    Log all bean initialization for debugging
 * 
 * 2. Validation:
 *    Validate bean state after initialization
 * 
 * 3. Proxy Creation:
 *    Wrap beans with proxies (AOP, transactions, security)
 * 
 * 4. Custom Annotation Processing:
 *    Process custom annotations on beans
 * 
 * 5. Performance Monitoring:
 *    Add timing/metrics collection
 * 
 * 6. Dependency Validation:
 *    Ensure required dependencies are set
 * 
 * Built-in BeanPostProcessors:
 * - AutowiredAnnotationBeanPostProcessor: Processes @Autowired, @Value, @Inject
 * - CommonAnnotationBeanPostProcessor: Processes @PostConstruct, @PreDestroy, @Resource
 * - PersistenceAnnotationBeanPostProcessor: Processes @PersistenceContext, @PersistenceUnit
 * - ScheduledAnnotationBeanPostProcessor: Processes @Scheduled
 * - AsyncAnnotationBeanPostProcessor: Processes @Async
 * 
 * Best Practices:
 * - Keep processing lightweight
 * - Avoid complex logic in BeanPostProcessor
 * - Be careful with bean ordering (use @Order if needed)
 * - Return original bean if no changes needed
 * - Handle exceptions gracefully
 * - Use specific interfaces/markers to target specific beans
 * - Document what your BeanPostProcessor does
 * - Be aware it processes ALL beans
 * 
 * Performance Considerations:
 * - BeanPostProcessor called for EVERY bean
 * - Can slow down application startup
 * - Use filtering (check bean type/name) to minimize overhead
 * - Avoid expensive operations
 * 
 * Advanced Patterns:
 * 
 * 1. Conditional Processing:
 *    if (bean.getClass().isAnnotationPresent(MyAnnotation.class)) {
 *        // Process
 *    }
 * 
 * 2. Proxy Creation:
 *    return Proxy.newProxyInstance(
 *        bean.getClass().getClassLoader(),
 *        bean.getClass().getInterfaces(),
 *        new MyInvocationHandler(bean)
 *    );
 * 
 * 3. Ordered Processing:
 *    @Component
 *    @Order(1)
 *    class FirstProcessor implements BeanPostProcessor { }
 * 
 *    @Component
 *    @Order(2)
 *    class SecondProcessor implements BeanPostProcessor { }
 * 
 * Filtering Strategies:
 * - By bean name: if (beanName.endsWith("Service"))
 * - By type: if (bean instanceof MyInterface)
 * - By annotation: if (bean.getClass().isAnnotationPresent(MyAnnotation.class))
 * - By package: if (bean.getClass().getPackage().getName().startsWith("com.example"))
 * 
 * Error Handling:
 * - Throwing exception prevents bean creation
 * - Use for validation that must pass
 * - Log warnings for non-critical issues
 * - Return bean to allow creation to continue
 * 
 * Testing:
 * - BeanPostProcessors are automatically registered
 * - Can be excluded with @ComponentScan excludeFilters
 * - Can mock for unit testing
 * 
 * Common Pitfalls:
 * - Forgetting to return bean instance
 * - Processing beans too early (before dependencies injected)
 * - Creating circular dependencies
 * - Not handling null bean names
 * - Expensive operations slowing startup
 * 
 * Debugging:
 * - Enable debug logging: logging.level.org.springframework.beans=DEBUG
 * - Add logging in both methods
 * - Check bean lifecycle events
 * - Use breakpoints in processor methods
 * 
 * Alternatives:
 * - @PostConstruct for single bean initialization
 * - ApplicationListener for context events
 * - BeanFactoryPostProcessor for bean definition modification
 * - @DependsOn for ordering
 */
