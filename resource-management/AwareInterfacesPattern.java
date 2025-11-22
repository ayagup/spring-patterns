package com.spring.patterns.resourcemanagement;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.*;
import org.springframework.context.*;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.web.context.ServletContextAware;

import javax.servlet.ServletContext;

/**
 * Aware Interfaces Pattern
 * 
 * Demonstrates Spring's Aware interfaces that allow beans to be aware of
 * and interact with the Spring container infrastructure.
 * 
 * Key Concepts:
 * - Aware interfaces provide callback methods for container infrastructure
 * - Beans can access ApplicationContext, BeanFactory, and other Spring components
 * - Callbacks execute during bean initialization
 * - Useful for accessing Spring infrastructure programmatically
 */

@Configuration
class AwareInterfacesConfig {
    
    @Bean
    public ApplicationContextAwareBean applicationContextAwareBean() {
        return new ApplicationContextAwareBean();
    }
    
    @Bean
    public BeanFactoryAwareBean beanFactoryAwareBean() {
        return new BeanFactoryAwareBean();
    }
    
    @Bean
    public EnvironmentAwareBean environmentAwareBean() {
        return new EnvironmentAwareBean();
    }
    
    @Bean
    public ResourceLoaderAwareBean resourceLoaderAwareBean() {
        return new ResourceLoaderAwareBean();
    }
    
    @Bean
    public BeanNameAwareBean beanNameAwareBean() {
        return new BeanNameAwareBean();
    }
    
    @Bean
    public MultipleAwareBean multipleAwareBean() {
        return new MultipleAwareBean();
    }
}

/**
 * Bean implementing ApplicationContextAware
 * Provides access to the ApplicationContext
 */
@Component
class ApplicationContextAwareBean implements ApplicationContextAware {
    
    private ApplicationContext applicationContext;
    
    public ApplicationContextAwareBean() {
        System.out.println("ApplicationContextAwareBean: Constructor called");
    }
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        System.out.println("ApplicationContextAwareBean: setApplicationContext() called");
        this.applicationContext = applicationContext;
        System.out.println("  ApplicationContext injected: " + applicationContext.getDisplayName());
    }
    
    public void demonstrateContextAccess() {
        System.out.println("\n=== ApplicationContext Access ===");
        System.out.println("Context ID: " + applicationContext.getId());
        System.out.println("Bean Definition Count: " + applicationContext.getBeanDefinitionCount());
        System.out.println("Startup Date: " + applicationContext.getStartupDate());
        
        // Can access any bean from context
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        System.out.println("Sample beans: ");
        for (int i = 0; i < Math.min(3, beanNames.length); i++) {
            System.out.println("  - " + beanNames[i]);
        }
    }
    
    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }
}

/**
 * Bean implementing BeanFactoryAware
 * Provides access to the BeanFactory
 */
class BeanFactoryAwareBean implements BeanFactoryAware {
    
    private BeanFactory beanFactory;
    
    public BeanFactoryAwareBean() {
        System.out.println("BeanFactoryAwareBean: Constructor called");
    }
    
    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        System.out.println("BeanFactoryAwareBean: setBeanFactory() called");
        this.beanFactory = beanFactory;
        System.out.println("  BeanFactory injected");
    }
    
    public void demonstrateBeanFactoryAccess() {
        System.out.println("\n=== BeanFactory Access ===");
        
        // Check if beans exist
        boolean hasAppContextBean = beanFactory.containsBean("applicationContextAwareBean");
        System.out.println("Contains applicationContextAwareBean: " + hasAppContextBean);
        
        // Check bean type
        boolean isSingleton = beanFactory.isSingleton("applicationContextAwareBean");
        System.out.println("applicationContextAwareBean is singleton: " + isSingleton);
        
        // Get bean by type
        try {
            ApplicationContextAwareBean bean = beanFactory.getBean(ApplicationContextAwareBean.class);
            System.out.println("Retrieved bean: " + bean.getClass().getSimpleName());
        } catch (Exception e) {
            System.out.println("Error retrieving bean: " + e.getMessage());
        }
    }
    
    public BeanFactory getBeanFactory() {
        return beanFactory;
    }
}

/**
 * Bean implementing BeanNameAware
 * Provides the bean's name as configured in the container
 */
class BeanNameAwareBean implements BeanNameAware {
    
    private String beanName;
    
    public BeanNameAwareBean() {
        System.out.println("BeanNameAwareBean: Constructor called");
    }
    
    @Override
    public void setBeanName(String name) {
        System.out.println("BeanNameAwareBean: setBeanName() called");
        this.beanName = name;
        System.out.println("  Bean name set to: " + name);
    }
    
    public void displayBeanName() {
        System.out.println("\n=== Bean Name ===");
        System.out.println("This bean is registered as: " + beanName);
    }
    
    public String getBeanName() {
        return beanName;
    }
}

/**
 * Bean implementing EnvironmentAware
 * Provides access to the Environment
 */
class EnvironmentAwareBean implements EnvironmentAware {
    
    private Environment environment;
    
    public EnvironmentAwareBean() {
        System.out.println("EnvironmentAwareBean: Constructor called");
    }
    
    @Override
    public void setEnvironment(Environment environment) {
        System.out.println("EnvironmentAwareBean: setEnvironment() called");
        this.environment = environment;
        System.out.println("  Environment injected");
    }
    
    public void demonstrateEnvironmentAccess() {
        System.out.println("\n=== Environment Access ===");
        
        // Get active profiles
        String[] activeProfiles = environment.getActiveProfiles();
        System.out.println("Active Profiles: " + 
                (activeProfiles.length > 0 ? String.join(", ", activeProfiles) : "None"));
        
        // Get default profiles
        String[] defaultProfiles = environment.getDefaultProfiles();
        System.out.println("Default Profiles: " + String.join(", ", defaultProfiles));
        
        // Get properties
        String javaVersion = environment.getProperty("java.version");
        System.out.println("Java Version: " + javaVersion);
        
        String osName = environment.getProperty("os.name");
        System.out.println("OS Name: " + osName);
        
        // Check property
        boolean hasProperty = environment.containsProperty("user.home");
        System.out.println("Has 'user.home' property: " + hasProperty);
    }
    
    public Environment getEnvironment() {
        return environment;
    }
}

/**
 * Bean implementing ResourceLoaderAware
 * Provides access to the ResourceLoader
 */
class ResourceLoaderAwareBean implements ResourceLoaderAware {
    
    private ResourceLoader resourceLoader;
    
    public ResourceLoaderAwareBean() {
        System.out.println("ResourceLoaderAwareBean: Constructor called");
    }
    
    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        System.out.println("ResourceLoaderAwareBean: setResourceLoader() called");
        this.resourceLoader = resourceLoader;
        System.out.println("  ResourceLoader injected");
    }
    
    public void demonstrateResourceLoading() {
        System.out.println("\n=== Resource Loading ===");
        
        try {
            // Load a classpath resource
            var resource = resourceLoader.getResource("classpath:application.properties");
            System.out.println("Resource: " + resource.getDescription());
            System.out.println("Exists: " + resource.exists());
            
            // Load from different locations
            String[] locations = {
                "classpath:config.xml",
                "file:C:/temp/data.txt",
                "https://www.example.com"
            };
            
            System.out.println("\nTrying different resource locations:");
            for (String location : locations) {
                var res = resourceLoader.getResource(location);
                System.out.println("  " + location + " -> exists: " + res.exists());
            }
            
        } catch (Exception e) {
            System.out.println("Error loading resource: " + e.getMessage());
        }
    }
    
    public ResourceLoader getResourceLoader() {
        return resourceLoader;
    }
}

/**
 * Bean implementing BeanClassLoaderAware
 * Provides access to the ClassLoader used to load the bean
 */
class BeanClassLoaderAwareBean implements BeanClassLoaderAware {
    
    private ClassLoader classLoader;
    
    public BeanClassLoaderAwareBean() {
        System.out.println("BeanClassLoaderAwareBean: Constructor called");
    }
    
    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        System.out.println("BeanClassLoaderAwareBean: setBeanClassLoader() called");
        this.classLoader = classLoader;
        System.out.println("  ClassLoader: " + classLoader.getClass().getSimpleName());
    }
    
    public void demonstrateClassLoaderAccess() {
        System.out.println("\n=== ClassLoader Access ===");
        System.out.println("ClassLoader: " + classLoader);
        System.out.println("Parent ClassLoader: " + classLoader.getParent());
    }
    
    public ClassLoader getClassLoader() {
        return classLoader;
    }
}

/**
 * Bean implementing ApplicationEventPublisherAware
 * Provides access to the ApplicationEventPublisher
 */
class EventPublisherAwareBean implements ApplicationEventPublisherAware {
    
    private ApplicationEventPublisher eventPublisher;
    
    public EventPublisherAwareBean() {
        System.out.println("EventPublisherAwareBean: Constructor called");
    }
    
    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        System.out.println("EventPublisherAwareBean: setApplicationEventPublisher() called");
        this.eventPublisher = applicationEventPublisher;
        System.out.println("  ApplicationEventPublisher injected");
    }
    
    public void publishCustomEvent(String message) {
        System.out.println("\n=== Publishing Event ===");
        System.out.println("Publishing event with message: " + message);
        
        CustomEvent event = new CustomEvent(this, message);
        eventPublisher.publishEvent(event);
        
        System.out.println("Event published successfully");
    }
}

/**
 * Custom event class
 */
class CustomEvent extends ApplicationEvent {
    
    private final String message;
    
    public CustomEvent(Object source, String message) {
        super(source);
        this.message = message;
    }
    
    public String getMessage() {
        return message;
    }
}

/**
 * Bean implementing multiple Aware interfaces
 * Demonstrates the callback order
 */
class MultipleAwareBean implements 
        BeanNameAware, 
        BeanClassLoaderAware,
        BeanFactoryAware, 
        EnvironmentAware,
        ResourceLoaderAware,
        ApplicationEventPublisherAware,
        ApplicationContextAware {
    
    private String beanName;
    private ClassLoader classLoader;
    private BeanFactory beanFactory;
    private Environment environment;
    private ResourceLoader resourceLoader;
    private ApplicationEventPublisher eventPublisher;
    private ApplicationContext applicationContext;
    
    public MultipleAwareBean() {
        System.out.println("\n=== MultipleAwareBean Initialization ===");
        System.out.println("1. Constructor called");
    }
    
    @Override
    public void setBeanName(String name) {
        System.out.println("2. BeanNameAware: setBeanName() - " + name);
        this.beanName = name;
    }
    
    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        System.out.println("3. BeanClassLoaderAware: setBeanClassLoader()");
        this.classLoader = classLoader;
    }
    
    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        System.out.println("4. BeanFactoryAware: setBeanFactory()");
        this.beanFactory = beanFactory;
    }
    
    @Override
    public void setEnvironment(Environment environment) {
        System.out.println("5. EnvironmentAware: setEnvironment()");
        this.environment = environment;
    }
    
    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        System.out.println("6. ResourceLoaderAware: setResourceLoader()");
        this.resourceLoader = resourceLoader;
    }
    
    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        System.out.println("7. ApplicationEventPublisherAware: setApplicationEventPublisher()");
        this.eventPublisher = applicationEventPublisher;
    }
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        System.out.println("8. ApplicationContextAware: setApplicationContext()");
        this.applicationContext = applicationContext;
        System.out.println("=== All Aware callbacks completed ===\n");
    }
    
    public void displayAllInjectedComponents() {
        System.out.println("\n=== Injected Components Summary ===");
        System.out.println("Bean Name: " + beanName);
        System.out.println("ClassLoader: " + (classLoader != null ? "Injected" : "Not injected"));
        System.out.println("BeanFactory: " + (beanFactory != null ? "Injected" : "Not injected"));
        System.out.println("Environment: " + (environment != null ? "Injected" : "Not injected"));
        System.out.println("ResourceLoader: " + (resourceLoader != null ? "Injected" : "Not injected"));
        System.out.println("EventPublisher: " + (eventPublisher != null ? "Injected" : "Not injected"));
        System.out.println("ApplicationContext: " + (applicationContext != null ? "Injected" : "Not injected"));
    }
}

/**
 * Mock ServletContextAware for demonstration
 * (Would be used in web applications)
 */
class WebApplicationBean implements ServletContextAware {
    
    private ServletContext servletContext;
    
    @Override
    public void setServletContext(ServletContext servletContext) {
        System.out.println("ServletContextAware: setServletContext() called");
        this.servletContext = servletContext;
        System.out.println("  ServletContext injected (web environment only)");
    }
    
    public void demonstrateServletContext() {
        if (servletContext != null) {
            System.out.println("\n=== ServletContext Access ===");
            System.out.println("Context Path: " + servletContext.getContextPath());
            System.out.println("Server Info: " + servletContext.getServerInfo());
        } else {
            System.out.println("ServletContext not available (not a web environment)");
        }
    }
}

public class AwareInterfacesPattern {
    
    public static void main(String[] args) {
        System.out.println("=== Spring Aware Interfaces Pattern Demo ===\n");
        
        // Create ApplicationContext
        AnnotationConfigApplicationContext context = 
                new AnnotationConfigApplicationContext(AwareInterfacesConfig.class);
        
        System.out.println("\n" + "=".repeat(50));
        
        // Demo 1: ApplicationContextAware
        System.out.println("\n=== Demo 1: ApplicationContextAware ===");
        ApplicationContextAwareBean contextAware = context.getBean(ApplicationContextAwareBean.class);
        contextAware.demonstrateContextAccess();
        
        // Demo 2: BeanFactoryAware
        System.out.println("\n=== Demo 2: BeanFactoryAware ===");
        BeanFactoryAwareBean factoryAware = context.getBean(BeanFactoryAwareBean.class);
        factoryAware.demonstrateBeanFactoryAccess();
        
        // Demo 3: BeanNameAware
        System.out.println("\n=== Demo 3: BeanNameAware ===");
        BeanNameAwareBean nameAware = context.getBean(BeanNameAwareBean.class);
        nameAware.displayBeanName();
        
        // Demo 4: EnvironmentAware
        System.out.println("\n=== Demo 4: EnvironmentAware ===");
        EnvironmentAwareBean envAware = context.getBean(EnvironmentAwareBean.class);
        envAware.demonstrateEnvironmentAccess();
        
        // Demo 5: ResourceLoaderAware
        System.out.println("\n=== Demo 5: ResourceLoaderAware ===");
        ResourceLoaderAwareBean resourceAware = context.getBean(ResourceLoaderAwareBean.class);
        resourceAware.demonstrateResourceLoading();
        
        // Demo 6: Multiple Aware interfaces
        System.out.println("\n=== Demo 6: Multiple Aware Interfaces ===");
        MultipleAwareBean multipleAware = context.getBean(MultipleAwareBean.class);
        multipleAware.displayAllInjectedComponents();
        
        // Demo 7: Aware callback order
        System.out.println("\n=== Demo 7: Aware Callback Order ===");
        displayAwareCallbackOrder();
        
        // Close context
        context.close();
        
        System.out.println("\n=== Demo Completed ===");
    }
    
    private static void displayAwareCallbackOrder() {
        System.out.println("\nAware Interface Callback Order:");
        System.out.println("1. BeanNameAware.setBeanName()");
        System.out.println("2. BeanClassLoaderAware.setBeanClassLoader()");
        System.out.println("3. BeanFactoryAware.setBeanFactory()");
        System.out.println("4. EnvironmentAware.setEnvironment()");
        System.out.println("5. EmbeddedValueResolverAware.setEmbeddedValueResolver()");
        System.out.println("6. ResourceLoaderAware.setResourceLoader()");
        System.out.println("7. ApplicationEventPublisherAware.setApplicationEventPublisher()");
        System.out.println("8. MessageSourceAware.setMessageSource()");
        System.out.println("9. ApplicationContextAware.setApplicationContext()");
        System.out.println("10. ServletContextAware.setServletContext() [web only]");
    }
}

/*
 * Key Takeaways:
 * 
 * 1. Aware interfaces allow beans to access Spring infrastructure
 * 2. Multiple Aware interfaces can be implemented by a single bean
 * 3. Callbacks execute in specific order during initialization
 * 4. Provides programmatic access to container resources
 * 5. Alternative to dependency injection for framework components
 * 
 * Common Aware Interfaces:
 * - ApplicationContextAware: Access to ApplicationContext
 * - BeanFactoryAware: Access to BeanFactory
 * - BeanNameAware: Bean's name in the container
 * - EnvironmentAware: Access to Environment
 * - ResourceLoaderAware: Access to ResourceLoader
 * - ApplicationEventPublisherAware: Publish events
 * - MessageSourceAware: Access to MessageSource (i18n)
 * - ServletContextAware: Access to ServletContext (web)
 * - BeanClassLoaderAware: Access to ClassLoader
 * 
 * Callback Execution Order:
 * 1. BeanNameAware
 * 2. BeanClassLoaderAware
 * 3. BeanFactoryAware
 * 4. EnvironmentAware
 * 5. ResourceLoaderAware
 * 6. ApplicationEventPublisherAware
 * 7. MessageSourceAware
 * 8. ApplicationContextAware
 * 9. ServletContextAware (web only)
 * 
 * When to Use:
 * - Need programmatic access to Spring infrastructure
 * - Framework component integration
 * - Dynamic bean lookup
 * - Event publishing
 * - Resource loading
 * - Environment access
 * 
 * Best Practices:
 * - Prefer dependency injection over Aware interfaces when possible
 * - Use Aware interfaces for framework integration
 * - Don't overuse - makes code Spring-specific
 * - Document why Aware interface is necessary
 * - Consider alternatives (e.g., @Autowired ApplicationContext)
 * 
 * Benefits:
 * - Direct access to Spring infrastructure
 * - Programmatic container interaction
 * - Framework extension points
 * - Dynamic resource access
 * 
 * Use Cases:
 * - Custom bean post processors
 * - Framework integrations
 * - Dynamic bean lookup
 * - Event-driven architectures
 * - Resource management
 * - Environment-specific logic
 * - Testing utilities
 */
