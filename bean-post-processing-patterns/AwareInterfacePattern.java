package com.example.beanpostprocessor;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.*;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Aware Interface Pattern
 * 
 * Demonstrates using Aware interfaces to inject Spring infrastructure
 * beans into application beans. Aware callbacks occur before initialization.
 * 
 * Key Concepts:
 * - ApplicationContextAware
 * - BeanFactoryAware
 * - BeanNameAware
 * - EnvironmentAware
 * - Infrastructure bean injection
 * 
 * Use Cases:
 * - Accessing ApplicationContext programmatically
 * - Dynamic bean lookup
 * - Accessing environment properties
 * - Bean introspection
 * - Framework integration
 */
@SpringBootApplication
public class AwareInterfacePattern {

    public static void main(String[] args) {
        SpringApplication.run(AwareInterfacePattern.class, args);
    }
}

/**
 * BeanPostProcessor to track Aware interface processing
 */
@Component
class AwareTrackingBeanPostProcessor implements BeanPostProcessor {

    private static final List<String> awareProcessing = new ArrayList<>();

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof ApplicationContextAware) {
            awareProcessing.add(beanName + " implements ApplicationContextAware");
        }
        if (bean instanceof BeanFactoryAware) {
            awareProcessing.add(beanName + " implements BeanFactoryAware");
        }
        if (bean instanceof BeanNameAware) {
            awareProcessing.add(beanName + " implements BeanNameAware");
        }
        if (bean instanceof EnvironmentAware) {
            awareProcessing.add(beanName + " implements EnvironmentAware");
        }
        return bean;
    }

    public static List<String> getAwareProcessing() {
        return new ArrayList<>(awareProcessing);
    }
}

/**
 * Bean demonstrating ApplicationContextAware
 */
@Component
class ApplicationContextAwareBean implements ApplicationContextAware {

    private ApplicationContext applicationContext;
    private String contextInfo;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        this.contextInfo = "Context ID: " + applicationContext.getId() + 
                          ", Display Name: " + applicationContext.getDisplayName();
        System.out.println("ApplicationContext injected into ApplicationContextAwareBean");
    }

    public String getContextInfo() {
        return contextInfo;
    }

    public int getBeanCount() {
        return applicationContext.getBeanDefinitionCount();
    }

    public <T> T getBean(Class<T> beanClass) {
        return applicationContext.getBean(beanClass);
    }

    public String[] getBeanNames() {
        return applicationContext.getBeanDefinitionNames();
    }
}

/**
 * Bean demonstrating BeanFactoryAware
 */
@Component
class BeanFactoryAwareBean implements BeanFactoryAware {

    private BeanFactory beanFactory;
    private String factoryInfo;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
        this.factoryInfo = "BeanFactory type: " + beanFactory.getClass().getSimpleName();
        System.out.println("BeanFactory injected into BeanFactoryAwareBean");
    }

    public String getFactoryInfo() {
        return factoryInfo;
    }

    public boolean containsBean(String name) {
        return beanFactory.containsBean(name);
    }

    public Object getBean(String name) {
        return beanFactory.getBean(name);
    }

    public boolean isSingleton(String name) {
        return beanFactory.isSingleton(name);
    }

    public boolean isPrototype(String name) {
        return beanFactory.isPrototype(name);
    }
}

/**
 * Bean demonstrating BeanNameAware
 */
@Component
class BeanNameAwareBean implements BeanNameAware {

    private String beanName;
    private long initTimestamp;

    @Override
    public void setBeanName(String name) {
        this.beanName = name;
        this.initTimestamp = System.currentTimeMillis();
        System.out.println("Bean name '" + name + "' injected into BeanNameAwareBean");
    }

    public String getBeanName() {
        return beanName;
    }

    public long getInitTimestamp() {
        return initTimestamp;
    }

    public Map<String, Object> getInfo() {
        return Map.of(
                "beanName", beanName != null ? beanName : "N/A",
                "initTimestamp", initTimestamp
        );
    }
}

/**
 * Bean demonstrating EnvironmentAware
 */
@Component
class EnvironmentAwareBean implements EnvironmentAware {

    private org.springframework.core.env.Environment environment;
    private String[] activeProfiles;
    private String[] defaultProfiles;

    @Override
    public void setEnvironment(org.springframework.core.env.Environment environment) {
        this.environment = environment;
        this.activeProfiles = environment.getActiveProfiles();
        this.defaultProfiles = environment.getDefaultProfiles();
        System.out.println("Environment injected into EnvironmentAwareBean");
    }

    public String[] getActiveProfiles() {
        return activeProfiles;
    }

    public String[] getDefaultProfiles() {
        return defaultProfiles;
    }

    public String getProperty(String key) {
        return environment.getProperty(key);
    }

    public Map<String, Object> getEnvironmentInfo() {
        return Map.of(
                "activeProfiles", activeProfiles,
                "defaultProfiles", defaultProfiles
        );
    }
}

/**
 * Bean demonstrating multiple Aware interfaces
 */
@Component
class MultiAwareBean implements ApplicationContextAware, BeanNameAware, EnvironmentAware {

    private ApplicationContext applicationContext;
    private String beanName;
    private org.springframework.core.env.Environment environment;
    private final List<String> awareCalls = new ArrayList<>();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        awareCalls.add("ApplicationContextAware.setApplicationContext called");
        System.out.println("MultiAwareBean: ApplicationContext set");
    }

    @Override
    public void setBeanName(String name) {
        this.beanName = name;
        awareCalls.add("BeanNameAware.setBeanName called with: " + name);
        System.out.println("MultiAwareBean: Bean name set to " + name);
    }

    @Override
    public void setEnvironment(org.springframework.core.env.Environment environment) {
        this.environment = environment;
        awareCalls.add("EnvironmentAware.setEnvironment called");
        System.out.println("MultiAwareBean: Environment set");
    }

    public Map<String, Object> getAllInfo() {
        return Map.of(
                "beanName", beanName != null ? beanName : "N/A",
                "contextId", applicationContext != null ? applicationContext.getId() : "N/A",
                "activeProfiles", environment != null ? environment.getActiveProfiles() : new String[0],
                "awareCalls", awareCalls
        );
    }

    public List<String> getAwareCalls() {
        return new ArrayList<>(awareCalls);
    }
}

/**
 * Controller to demonstrate Aware interfaces
 */
@RestController
class AwareInterfaceController {

    private final ApplicationContextAwareBean contextAwareBean;
    private final BeanFactoryAwareBean factoryAwareBean;
    private final BeanNameAwareBean nameAwareBean;
    private final EnvironmentAwareBean environmentAwareBean;
    private final MultiAwareBean multiAwareBean;

    public AwareInterfaceController(ApplicationContextAwareBean contextAwareBean,
                                   BeanFactoryAwareBean factoryAwareBean,
                                   BeanNameAwareBean nameAwareBean,
                                   EnvironmentAwareBean environmentAwareBean,
                                   MultiAwareBean multiAwareBean) {
        this.contextAwareBean = contextAwareBean;
        this.factoryAwareBean = factoryAwareBean;
        this.nameAwareBean = nameAwareBean;
        this.environmentAwareBean = environmentAwareBean;
        this.multiAwareBean = multiAwareBean;
    }

    @GetMapping("/aware/tracked")
    public List<String> getTrackedAware() {
        return AwareTrackingBeanPostProcessor.getAwareProcessing();
    }

    @GetMapping("/aware/context")
    public Map<String, Object> getContextInfo() {
        return Map.of(
                "contextInfo", contextAwareBean.getContextInfo(),
                "beanCount", contextAwareBean.getBeanCount()
        );
    }

    @GetMapping("/aware/factory")
    public Map<String, Object> getFactoryInfo() {
        return Map.of(
                "factoryInfo", factoryAwareBean.getFactoryInfo(),
                "containsUserService", factoryAwareBean.containsBean("userService")
        );
    }

    @GetMapping("/aware/name")
    public Map<String, Object> getNameInfo() {
        return nameAwareBean.getInfo();
    }

    @GetMapping("/aware/environment")
    public Map<String, Object> getEnvironmentInfo() {
        return environmentAwareBean.getEnvironmentInfo();
    }

    @GetMapping("/aware/multi")
    public Map<String, Object> getMultiAwareInfo() {
        return multiAwareBean.getAllInfo();
    }
}

/**
 * Documentation:
 * 
 * Common Aware Interfaces:
 * 
 * 1. ApplicationContextAware:
 *    - Provides ApplicationContext
 *    - Access to all beans
 *    - Publish events
 *    - Resource loading
 * 
 * 2. BeanFactoryAware:
 *    - Provides BeanFactory
 *    - Bean lookup
 *    - Bean metadata
 *    - Lower-level than ApplicationContext
 * 
 * 3. BeanNameAware:
 *    - Provides bean name
 *    - Useful for logging
 *    - Self-reference
 * 
 * 4. EnvironmentAware:
 *    - Provides Environment
 *    - Property access
 *    - Profile information
 * 
 * 5. ResourceLoaderAware:
 *    - Provides ResourceLoader
 *    - Load classpath resources
 *    - Load file system resources
 * 
 * 6. ApplicationEventPublisherAware:
 *    - Provides event publisher
 *    - Publish custom events
 * 
 * 7. MessageSourceAware:
 *    - Provides MessageSource
 *    - I18N message resolution
 * 
 * 8. ServletContextAware:
 *    - Provides ServletContext (web apps)
 *    - Servlet configuration
 * 
 * Execution Order:
 * 1. BeanNameAware.setBeanName()
 * 2. BeanClassLoaderAware.setBeanClassLoader()
 * 3. BeanFactoryAware.setBeanFactory()
 * 4. EnvironmentAware.setEnvironment()
 * 5. EmbeddedValueResolverAware.setEmbeddedValueResolver()
 * 6. ResourceLoaderAware.setResourceLoader()
 * 7. ApplicationEventPublisherAware.setApplicationEventPublisher()
 * 8. MessageSourceAware.setMessageSource()
 * 9. ApplicationContextAware.setApplicationContext()
 * 10. ServletContextAware.setServletContext()
 * 
 * Implementation:
 * 
 * @Component
 * class MyBean implements ApplicationContextAware {
 *     private ApplicationContext context;
 *     
 *     @Override
 *     public void setApplicationContext(ApplicationContext context) {
 *         this.context = context;
 *     }
 * }
 * 
 * When to Use:
 * - Need programmatic bean lookup
 * - Dynamic bean access
 * - Framework integration
 * - Publishing events
 * - Accessing environment
 * - Resource loading
 * 
 * When NOT to Use:
 * - Normal dependency injection (use @Autowired)
 * - Configuration properties (use @Value)
 * - Most application code
 * - Creates coupling to Spring
 * 
 * Best Practices:
 * - Use only when necessary
 * - Prefer dependency injection
 * - Document why Aware is needed
 * - Keep Aware usage minimal
 * - Consider alternatives
 * - Test with mock contexts
 * 
 * Alternatives:
 * 
 * 1. @Autowired ApplicationContext:
 *    @Component
 *    class MyBean {
 *        @Autowired
 *        private ApplicationContext context;
 *    }
 * 
 * 2. Constructor Injection:
 *    @Component
 *    class MyBean {
 *        private final ApplicationContext context;
 *        
 *        public MyBean(ApplicationContext context) {
 *            this.context = context;
 *        }
 *    }
 * 
 * Dynamic Bean Lookup:
 * 
 * // Bad - defeats purpose of DI
 * MyService service = context.getBean(MyService.class);
 * 
 * // Better - inject dependencies
 * @Autowired
 * private MyService service;
 * 
 * Use Cases for Aware:
 * 
 * 1. Plugin Systems:
 *    - Dynamically load beans
 *    - Bean discovery
 * 
 * 2. Factory Patterns:
 *    - Create beans dynamically
 *    - Prototype bean creation
 * 
 * 3. Framework Code:
 *    - Spring integration
 *    - Library development
 * 
 * 4. Event Publishing:
 *    - Custom event publishing
 *    - Event-driven architecture
 * 
 * Testing:
 * - Mock ApplicationContext
 * - Use @MockBean
 * - Test without full context when possible
 * 
 * Performance:
 * - Minimal overhead
 * - Callbacks executed once during initialization
 * - No runtime impact
 * 
 * Common Pitfalls:
 * - Overusing Aware interfaces
 * - Using for normal DI
 * - Creating tight coupling
 * - Not documenting usage
 * - Forgetting null checks
 * 
 * Advanced Patterns:
 * 
 * 1. Lazy Bean Lookup:
 *    private Supplier<MyBean> beanSupplier = 
 *        () -> context.getBean(MyBean.class);
 * 
 * 2. Optional Dependencies:
 *    try {
 *        return context.getBean(MyBean.class);
 *    } catch (NoSuchBeanDefinitionException e) {
 *        return null;
 *    }
 * 
 * 3. Bean Discovery:
 *    Map<String, MyInterface> beans = 
 *        context.getBeansOfType(MyInterface.class);
 * 
 * Security:
 * - Validate bean names before lookup
 * - Restrict bean access in multi-tenant apps
 * - Be careful with user input in bean lookup
 * 
 * Spring Boot:
 * - ApplicationContext often needed
 * - Use @Autowired for most cases
 * - Aware useful for auto-configuration
 */
