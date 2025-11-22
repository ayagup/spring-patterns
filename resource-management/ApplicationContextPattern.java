package com.spring.patterns.resourcemanagement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Arrays;

/**
 * Application Context Pattern
 * 
 * Demonstrates Spring's ApplicationContext as the central interface for
 * accessing Spring container. Shows different ApplicationContext implementations
 * and their features.
 * 
 * Key Concepts:
 * - ApplicationContext is the central interface to the Spring IoC container
 * - Provides bean factory methods, resource loading, event publication
 * - Different implementations for various use cases
 * - Supports environment abstraction and profiles
 */

@Configuration
@ComponentScan(basePackages = "com.spring.patterns.resourcemanagement")
@PropertySource("classpath:application.properties")
class ApplicationContextConfig {
    
    @Bean
    public MessageService messageService() {
        return new MessageService("Hello from Application Context!");
    }
    
    @Bean
    public UserService userService() {
        return new UserService();
    }
    
    @Bean
    @Profile("dev")
    public DataSourceConfig devDataSource() {
        return new DataSourceConfig("dev-database");
    }
    
    @Bean
    @Profile("prod")
    public DataSourceConfig prodDataSource() {
        return new DataSourceConfig("prod-database");
    }
}

/**
 * Simple service bean
 */
@Service
class MessageService {
    
    private final String message;
    
    public MessageService(String message) {
        this.message = message;
    }
    
    public MessageService() {
        this.message = "Default message";
    }
    
    public String getMessage() {
        return message;
    }
    
    public void printMessage() {
        System.out.println("Message: " + message);
    }
}

/**
 * Service demonstrating ApplicationContext usage
 */
@Service
class UserService {
    
    @Autowired
    private ApplicationContext applicationContext;
    
    @Autowired
    private Environment environment;
    
    public void demonstrateContextFeatures() {
        System.out.println("\n=== ApplicationContext Features ===");
        
        // Feature 1: Bean access
        System.out.println("\n1. Bean Access:");
        MessageService messageService = applicationContext.getBean(MessageService.class);
        System.out.println("Retrieved bean: " + messageService.getMessage());
        
        // Feature 2: Bean information
        System.out.println("\n2. Bean Information:");
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        System.out.println("Total beans: " + beanNames.length);
        System.out.println("Sample beans: " + Arrays.toString(
                Arrays.copyOf(beanNames, Math.min(5, beanNames.length))));
        
        // Feature 3: Environment access
        System.out.println("\n3. Environment Access:");
        System.out.println("Active profiles: " + Arrays.toString(environment.getActiveProfiles()));
        System.out.println("Default profiles: " + Arrays.toString(environment.getDefaultProfiles()));
        
        // Feature 4: Resource loading
        System.out.println("\n4. Resource Loading:");
        try {
            var resource = applicationContext.getResource("classpath:application.properties");
            System.out.println("Resource exists: " + resource.exists());
            System.out.println("Resource description: " + resource.getDescription());
        } catch (Exception e) {
            System.out.println("Resource loading error: " + e.getMessage());
        }
        
        // Feature 5: Application context hierarchy
        System.out.println("\n5. Context Information:");
        System.out.println("Context ID: " + applicationContext.getId());
        System.out.println("Context display name: " + applicationContext.getDisplayName());
        System.out.println("Context start time: " + applicationContext.getStartupDate());
    }
    
    public void listBeansByType() {
        System.out.println("\n=== Beans by Type ===");
        
        // Get all Service beans
        String[] serviceBeans = applicationContext.getBeanNamesForType(Object.class);
        System.out.println("Total beans in context: " + serviceBeans.length);
        
        // Check if bean exists
        boolean hasMessageService = applicationContext.containsBean("messageService");
        System.out.println("Contains messageService: " + hasMessageService);
        
        // Check bean singleton status
        boolean isSingleton = applicationContext.isSingleton("messageService");
        System.out.println("messageService is singleton: " + isSingleton);
    }
}

/**
 * Configuration class for demonstrating profiles
 */
class DataSourceConfig {
    private final String databaseName;
    
    public DataSourceConfig(String databaseName) {
        this.databaseName = databaseName;
    }
    
    public String getDatabaseName() {
        return databaseName;
    }
    
    @Override
    public String toString() {
        return "DataSourceConfig{database='" + databaseName + "'}";
    }
}

/**
 * Service to demonstrate context lifecycle
 */
@Component
class ContextLifecycleDemo {
    
    @Autowired
    private ApplicationContext context;
    
    public void demonstrateLifecycle() {
        System.out.println("\n=== Context Lifecycle Demo ===");
        
        // Check if context is active
        if (context instanceof ConfigurableApplicationContext) {
            ConfigurableApplicationContext configurableContext = 
                    (ConfigurableApplicationContext) context;
            System.out.println("Context is active: " + configurableContext.isActive());
        }
        
        // Get context startup information
        System.out.println("Context startup date: " + context.getStartupDate());
        System.out.println("Application name: " + context.getApplicationName());
    }
}

/**
 * Interface for configurable application context
 */
interface ConfigurableApplicationContext extends ApplicationContext {
    boolean isActive();
}

public class ApplicationContextPattern {
    
    public static void main(String[] args) {
        System.out.println("=== Spring Application Context Pattern Demo ===\n");
        
        // Demo 1: Annotation-based ApplicationContext
        System.out.println("1. Annotation-based ApplicationContext:");
        demonstrateAnnotationContext();
        
        // Demo 2: Different ApplicationContext types
        System.out.println("\n2. Different ApplicationContext Types:");
        demonstrateContextTypes();
        
        // Demo 3: Programmatic context creation
        System.out.println("\n3. Programmatic Context Creation:");
        demonstrateProgrammaticContext();
        
        // Demo 4: Context hierarchy
        System.out.println("\n4. Context Hierarchy:");
        demonstrateContextHierarchy();
        
        // Demo 5: Environment and profiles
        System.out.println("\n5. Environment and Profiles:");
        demonstrateProfiles();
        
        System.out.println("\n=== Demo Completed ===");
    }
    
    private static void demonstrateAnnotationContext() {
        // Create annotation-based context
        AnnotationConfigApplicationContext context = 
                new AnnotationConfigApplicationContext(ApplicationContextConfig.class);
        
        System.out.println("Context created: " + context.getDisplayName());
        System.out.println("Bean definition count: " + context.getBeanDefinitionCount());
        
        // Get and use a bean
        MessageService messageService = context.getBean(MessageService.class);
        messageService.printMessage();
        
        // Get UserService and demonstrate features
        UserService userService = context.getBean(UserService.class);
        userService.demonstrateContextFeatures();
        userService.listBeansByType();
        
        // Close context
        context.close();
        System.out.println("Context closed successfully");
    }
    
    private static void demonstrateContextTypes() {
        System.out.println("\nAvailable ApplicationContext Implementations:");
        
        // 1. AnnotationConfigApplicationContext
        System.out.println("\n1. AnnotationConfigApplicationContext:");
        System.out.println("   - For Java-based configuration");
        System.out.println("   - Supports @Configuration classes");
        System.out.println("   - Component scanning support");
        
        // 2. ClassPathXmlApplicationContext
        System.out.println("\n2. ClassPathXmlApplicationContext:");
        System.out.println("   - Loads context from classpath XML");
        System.out.println("   - Traditional XML configuration");
        System.out.println("   - Example: new ClassPathXmlApplicationContext(\"applicationContext.xml\")");
        
        // 3. FileSystemXmlApplicationContext
        System.out.println("\n3. FileSystemXmlApplicationContext:");
        System.out.println("   - Loads context from file system XML");
        System.out.println("   - Absolute or relative paths");
        System.out.println("   - Example: new FileSystemXmlApplicationContext(\"C:/config/context.xml\")");
        
        // 4. GenericApplicationContext
        System.out.println("\n4. GenericApplicationContext:");
        System.out.println("   - Generic, flexible context");
        System.out.println("   - Programmatic bean registration");
        System.out.println("   - No specific configuration format");
        
        // 5. Web ApplicationContexts
        System.out.println("\n5. Web ApplicationContexts:");
        System.out.println("   - XmlWebApplicationContext");
        System.out.println("   - AnnotationConfigWebApplicationContext");
        System.out.println("   - For web applications");
    }
    
    private static void demonstrateProgrammaticContext() {
        // Create a generic application context programmatically
        GenericApplicationContext context = new GenericApplicationContext();
        
        // Register beans programmatically
        context.registerBean("messageService", MessageService.class, 
                () -> new MessageService("Programmatically created bean"));
        
        context.registerBean("userService", UserService.class);
        
        // Refresh context to initialize beans
        context.refresh();
        
        System.out.println("Programmatic context created");
        System.out.println("Bean count: " + context.getBeanDefinitionCount());
        
        // Get and use bean
        MessageService service = context.getBean(MessageService.class);
        service.printMessage();
        
        // Close context
        context.close();
    }
    
    private static void demonstrateContextHierarchy() {
        // Create parent context
        AnnotationConfigApplicationContext parentContext = 
                new AnnotationConfigApplicationContext();
        parentContext.register(ApplicationContextConfig.class);
        parentContext.refresh();
        
        System.out.println("Parent context created");
        
        // Create child context with parent
        AnnotationConfigApplicationContext childContext = 
                new AnnotationConfigApplicationContext();
        childContext.setParent(parentContext);
        childContext.refresh();
        
        System.out.println("Child context created with parent");
        System.out.println("Child can access parent beans: " + 
                (childContext.getParent() != null));
        
        // Child context can access beans from parent
        MessageService service = childContext.getBean(MessageService.class);
        System.out.println("Accessed parent bean from child: " + service.getMessage());
        
        // Close contexts
        childContext.close();
        parentContext.close();
    }
    
    private static void demonstrateProfiles() {
        // Create context with 'dev' profile
        AnnotationConfigApplicationContext devContext = 
                new AnnotationConfigApplicationContext();
        devContext.getEnvironment().setActiveProfiles("dev");
        devContext.register(ApplicationContextConfig.class);
        devContext.refresh();
        
        System.out.println("\nDev Profile Context:");
        System.out.println("Active profiles: " + 
                Arrays.toString(devContext.getEnvironment().getActiveProfiles()));
        
        // Check for profile-specific beans
        boolean hasDevDataSource = devContext.containsBean("devDataSource");
        boolean hasProdDataSource = devContext.containsBean("prodDataSource");
        System.out.println("Has dev DataSource: " + hasDevDataSource);
        System.out.println("Has prod DataSource: " + hasProdDataSource);
        
        devContext.close();
        
        // Create context with 'prod' profile
        AnnotationConfigApplicationContext prodContext = 
                new AnnotationConfigApplicationContext();
        prodContext.getEnvironment().setActiveProfiles("prod");
        prodContext.register(ApplicationContextConfig.class);
        prodContext.refresh();
        
        System.out.println("\nProd Profile Context:");
        System.out.println("Active profiles: " + 
                Arrays.toString(prodContext.getEnvironment().getActiveProfiles()));
        
        hasDevDataSource = prodContext.containsBean("devDataSource");
        hasProdDataSource = prodContext.containsBean("prodDataSource");
        System.out.println("Has dev DataSource: " + hasDevDataSource);
        System.out.println("Has prod DataSource: " + hasProdDataSource);
        
        prodContext.close();
    }
}

/*
 * Key Takeaways:
 * 
 * 1. ApplicationContext is the central interface for Spring IoC container
 * 2. Provides bean management, resource loading, event publication
 * 3. Multiple implementations for different configuration styles
 * 4. Supports environment abstraction and profiles
 * 5. Can create context hierarchies (parent-child relationships)
 * 
 * ApplicationContext Features:
 * - Bean factory methods (getBean, containsBean, etc.)
 * - Resource loading (getResource, getResources)
 * - Event publication (publishEvent)
 * - Environment access (getEnvironment)
 * - Message source (getMessage for i18n)
 * - Application lifecycle management
 * 
 * Common Implementations:
 * - AnnotationConfigApplicationContext: Java config
 * - ClassPathXmlApplicationContext: XML from classpath
 * - FileSystemXmlApplicationContext: XML from file system
 * - GenericApplicationContext: Programmatic registration
 * - Web contexts: For web applications
 * 
 * Benefits:
 * - Central access point to Spring container
 * - Consistent API across different config styles
 * - Support for profiles and environments
 * - Hierarchical context support
 * - Resource and message management
 * 
 * Use Cases:
 * - Application bootstrapping
 * - Bean retrieval and management
 * - Profile-based configuration
 * - Resource loading
 * - Environment property access
 * - Event-driven architecture
 */
