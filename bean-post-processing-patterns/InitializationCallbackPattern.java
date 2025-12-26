package com.example.beanpostprocessor;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Initialization Callback Pattern
 * 
 * Demonstrates various bean initialization callbacks and their
 * execution order in relation to BeanPostProcessor.
 * 
 * Key Concepts:
 * - @PostConstruct
 * - InitializingBean.afterPropertiesSet()
 * - @Bean(initMethod)
 * - BeanPostProcessor callbacks
 * - Initialization order
 * 
 * Use Cases:
 * - Resource initialization
 * - Connection pool setup
 * - Cache warmup
 * - Data validation
 * - Configuration verification
 */
@SpringBootApplication
public class InitializationCallbackPattern {

    public static void main(String[] args) {
        SpringApplication.run(InitializationCallbackPattern.class, args);
    }
}

/**
 * BeanPostProcessor to track initialization order
 */
@Component
class InitializationTrackingBeanPostProcessor implements BeanPostProcessor {

    private static final List<String> initializationSteps = new ArrayList<>();

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof InitializationDemo) {
            String step = "1. BeanPostProcessor.postProcessBeforeInitialization - " + beanName;
            initializationSteps.add(step);
            System.out.println(step);
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof InitializationDemo) {
            String step = "5. BeanPostProcessor.postProcessAfterInitialization - " + beanName;
            initializationSteps.add(step);
            System.out.println(step);
        }
        return bean;
    }

    public static List<String> getInitializationSteps() {
        return new ArrayList<>(initializationSteps);
    }
}

/**
 * Interface marker for tracking
 */
interface InitializationDemo {
    String getStatus();
}

/**
 * Bean demonstrating all initialization callbacks
 */
@Component
class FullInitializationBean implements org.springframework.beans.factory.InitializingBean, InitializationDemo {

    private String status = "Created";
    private boolean postConstructCalled = false;
    private boolean afterPropertiesSetCalled = false;
    private boolean initMethodCalled = false;

    public FullInitializationBean() {
        status = "Constructor called";
        System.out.println("0. Constructor: FullInitializationBean");
    }

    @PostConstruct
    public void postConstruct() {
        postConstructCalled = true;
        status = "PostConstruct called";
        String step = "2. @PostConstruct method called";
        InitializationTrackingBeanPostProcessor.initializationSteps.add(step);
        System.out.println(step);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        afterPropertiesSetCalled = true;
        status = "AfterPropertiesSet called";
        String step = "3. InitializingBean.afterPropertiesSet() called";
        InitializationTrackingBeanPostProcessor.initializationSteps.add(step);
        System.out.println(step);
    }

    public void customInitMethod() {
        initMethodCalled = true;
        status = "Init method called";
        String step = "4. @Bean(initMethod) called";
        InitializationTrackingBeanPostProcessor.initializationSteps.add(step);
        System.out.println(step);
    }

    @Override
    public String getStatus() {
        return status;
    }

    public Map<String, Boolean> getCallbackStatus() {
        return Map.of(
                "postConstruct", postConstructCalled,
                "afterPropertiesSet", afterPropertiesSetCalled,
                "initMethod", initMethodCalled
        );
    }
}

/**
 * Bean with @PostConstruct only
 */
@Component
class PostConstructBean implements InitializationDemo {

    private String status = "Created";
    private int initializationCount = 0;

    @PostConstruct
    public void initialize() {
        initializationCount++;
        status = "Initialized via @PostConstruct";
        System.out.println("PostConstructBean initialized");
    }

    @Override
    public String getStatus() {
        return status + " (count: " + initializationCount + ")";
    }
}

/**
 * Bean with InitializingBean only
 */
@Component
class InitializingBeanExample implements org.springframework.beans.factory.InitializingBean, InitializationDemo {

    private String status = "Created";
    private List<String> initData = new ArrayList<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        status = "Initialized via afterPropertiesSet";
        initData.add("data1");
        initData.add("data2");
        System.out.println("InitializingBeanExample initialized");
    }

    @Override
    public String getStatus() {
        return status + " (data count: " + initData.size() + ")";
    }

    public List<String> getInitData() {
        return new ArrayList<>(initData);
    }
}

/**
 * Bean with validation in initialization
 */
@Component
class ValidatedBean implements InitializationDemo {

    private String requiredProperty = "default";
    private int requiredNumber = 0;

    @PostConstruct
    public void validate() {
        if (requiredProperty == null || requiredProperty.isEmpty()) {
            throw new BeanInitializationException("requiredProperty must not be null or empty");
        }
        if (requiredNumber < 0) {
            throw new BeanInitializationException("requiredNumber must not be negative");
        }
        System.out.println("ValidatedBean validation passed");
    }

    @Override
    public String getStatus() {
        return "Validated (property: " + requiredProperty + ", number: " + requiredNumber + ")";
    }

    public void setRequiredProperty(String requiredProperty) {
        this.requiredProperty = requiredProperty;
    }

    public void setRequiredNumber(int requiredNumber) {
        this.requiredNumber = requiredNumber;
    }
}

/**
 * Bean with resource initialization
 */
@Component
class ResourceInitializationBean implements org.springframework.beans.factory.InitializingBean, InitializationDemo {

    private boolean resourcesLoaded = false;
    private long initTimestamp = 0;

    @PostConstruct
    public void loadResources() {
        System.out.println("Loading resources...");
        // Simulate resource loading
        initTimestamp = System.currentTimeMillis();
    }

    @Override
    public void afterPropertiesSet() {
        System.out.println("Verifying resources...");
        resourcesLoaded = true;
    }

    @Override
    public String getStatus() {
        return "Resources loaded: " + resourcesLoaded + " at " + initTimestamp;
    }
}

/**
 * Controller to demonstrate initialization
 */
@RestController
class InitializationController {

    private final FullInitializationBean fullBean;
    private final PostConstructBean postConstructBean;
    private final InitializingBeanExample initializingBean;
    private final ValidatedBean validatedBean;
    private final ResourceInitializationBean resourceBean;

    public InitializationController(FullInitializationBean fullBean,
                                   PostConstructBean postConstructBean,
                                   InitializingBeanExample initializingBean,
                                   ValidatedBean validatedBean,
                                   ResourceInitializationBean resourceBean) {
        this.fullBean = fullBean;
        this.postConstructBean = postConstructBean;
        this.initializingBean = initializingBean;
        this.validatedBean = validatedBean;
        this.resourceBean = resourceBean;
    }

    @GetMapping("/init/steps")
    public List<String> getInitializationSteps() {
        return InitializationTrackingBeanPostProcessor.getInitializationSteps();
    }

    @GetMapping("/init/full-bean")
    public Map<String, Object> getFullBean() {
        return Map.of(
                "status", fullBean.getStatus(),
                "callbacks", fullBean.getCallbackStatus()
        );
    }

    @GetMapping("/init/post-construct-bean")
    public Map<String, String> getPostConstructBean() {
        return Map.of("status", postConstructBean.getStatus());
    }

    @GetMapping("/init/initializing-bean")
    public Map<String, Object> getInitializingBean() {
        return Map.of(
                "status", initializingBean.getStatus(),
                "data", initializingBean.getInitData()
        );
    }

    @GetMapping("/init/validated-bean")
    public Map<String, String> getValidatedBean() {
        return Map.of("status", validatedBean.getStatus());
    }

    @GetMapping("/init/resource-bean")
    public Map<String, String> getResourceBean() {
        return Map.of("status", resourceBean.getStatus());
    }
}

/**
 * Documentation:
 * 
 * Bean Initialization Order:
 * 1. Constructor
 * 2. Dependency Injection
 * 3. BeanPostProcessor.postProcessBeforeInitialization
 * 4. @PostConstruct methods
 * 5. InitializingBean.afterPropertiesSet()
 * 6. @Bean(initMethod = "methodName")
 * 7. BeanPostProcessor.postProcessAfterInitialization
 * 
 * Initialization Mechanisms:
 * 
 * 1. @PostConstruct:
 *    - JSR-250 annotation
 *    - Method-level
 *    - Can have any access modifier
 *    - Cannot have parameters
 *    - Runs after dependency injection
 *    - Most common approach
 * 
 * 2. InitializingBean interface:
 *    - Spring-specific
 *    - afterPropertiesSet() method
 *    - Couples code to Spring
 *    - Runs after @PostConstruct
 *    - Less preferred than @PostConstruct
 * 
 * 3. @Bean(initMethod):
 *    - Configuration-based
 *    - Specified in @Bean annotation
 *    - Decouples bean from Spring
 *    - Runs after InitializingBean
 *    - Good for third-party beans
 * 
 * 4. Custom BeanPostProcessor:
 *    - Most powerful
 *    - Can modify beans
 *    - Cross-cutting concerns
 *    - Runs before/after other callbacks
 * 
 * Examples:
 * 
 * 1. @PostConstruct:
 *    @Component
 *    class MyBean {
 *        @PostConstruct
 *        public void init() {
 *            // Initialize
 *        }
 *    }
 * 
 * 2. InitializingBean:
 *    @Component
 *    class MyBean implements InitializingBean {
 *        @Override
 *        public void afterPropertiesSet() {
 *            // Initialize
 *        }
 *    }
 * 
 * 3. @Bean(initMethod):
 *    @Configuration
 *    class Config {
 *        @Bean(initMethod = "init")
 *        public MyBean myBean() {
 *            return new MyBean();
 *        }
 *    }
 * 
 *    class MyBean {
 *        public void init() {
 *            // Initialize
 *        }
 *    }
 * 
 * Best Practices:
 * - Prefer @PostConstruct for simplicity
 * - Use InitializingBean for Spring-aware beans
 * - Use initMethod for third-party beans
 * - Keep initialization logic lightweight
 * - Validate configuration in initialization
 * - Handle exceptions appropriately
 * - Document initialization requirements
 * - Use lazy-init sparingly
 * 
 * Common Use Cases:
 * 
 * 1. Resource Loading:
 *    @PostConstruct
 *    public void loadResources() {
 *        // Load files, templates, etc.
 *    }
 * 
 * 2. Connection Pool Setup:
 *    @PostConstruct
 *    public void initializePool() {
 *        // Create connection pool
 *    }
 * 
 * 3. Cache Warmup:
 *    @PostConstruct
 *    public void warmupCache() {
 *        // Pre-load cache
 *    }
 * 
 * 4. Validation:
 *    @PostConstruct
 *    public void validate() {
 *        if (required == null) {
 *            throw new IllegalStateException();
 *        }
 *    }
 * 
 * 5. Scheduled Tasks:
 *    @PostConstruct
 *    public void startScheduler() {
 *        // Start background tasks
 *    }
 * 
 * Error Handling:
 * - Exceptions prevent bean creation
 * - Application fails to start
 * - Use for critical validation
 * - Log warnings for non-critical issues
 * 
 * Async Initialization:
 * @PostConstruct
 * public void init() {
 *     CompletableFuture.runAsync(() -> {
 *         // Long-running initialization
 *     });
 * }
 * 
 * Conditional Initialization:
 * @PostConstruct
 * public void init() {
 *     if (environment.getProperty("feature.enabled", Boolean.class, false)) {
 *         // Initialize feature
 *     }
 * }
 * 
 * Testing:
 * - @PostConstruct runs in tests
 * - Can disable with @TestPropertySource
 * - Mock dependencies before initialization
 * - Test initialization failures
 * 
 * Performance:
 * - Initialization adds to startup time
 * - Use lazy-init for non-critical beans
 * - Consider async for long operations
 * - Profile startup time
 * 
 * Destruction Callbacks:
 * - @PreDestroy (counterpart to @PostConstruct)
 * - DisposableBean.destroy()
 * - @Bean(destroyMethod)
 * - Same order in reverse
 * 
 * Common Pitfalls:
 * - Using @PostConstruct in abstract classes
 * - Forgetting exception handling
 * - Long-running initialization blocking startup
 * - Circular dependencies in initialization
 * - Not testing initialization logic
 */
