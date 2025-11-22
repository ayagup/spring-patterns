package com.spring.patterns.factory;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * ApplicationContextAware Pattern
 * 
 * ApplicationContextAware is a more powerful version of BeanFactoryAware.
 * It provides access to the entire ApplicationContext, not just BeanFactory.
 * 
 * Characteristics:
 * - Implements ApplicationContextAware interface
 * - setApplicationContext() called during initialization
 * - Access to ApplicationContext features
 * - Environment properties access
 * - Event publishing
 * - Resource loading
 * - More features than BeanFactory
 * 
 * Use Cases:
 * - Publishing application events
 * - Accessing environment properties
 * - Resource loading
 * - Advanced bean lookup
 * - Application-wide operations
 * - Framework integration
 */
@SpringBootApplication
public class ApplicationContextAwarePattern {

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(ApplicationContextAwarePattern.class, args);
        
        System.out.println("\n=== ApplicationContextAware Pattern ===");
        
        // Example 1: Event Publisher
        EventPublisher publisher = context.getBean(EventPublisher.class);
        publisher.publishUserCreated("john@example.com");
        publisher.publishOrderPlaced("ORDER-123", 299.99);
        
        // Example 2: Property Accessor
        PropertyAccessor accessor = context.getBean(PropertyAccessor.class);
        accessor.displayProperties();
        
        // Example 3: Resource Loader
        ResourceLoader loader = context.getBean(ResourceLoader.class);
        loader.loadResources();
        
        // Example 4: Bean Registry
        BeanRegistry registry = context.getBean(BeanRegistry.class);
        registry.listAllServices();
    }
}

/**
 * Example 1: Event Publisher using ApplicationContext
 */
@Component
class EventPublisher implements ApplicationContextAware {
    
    private ApplicationContext applicationContext;
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        System.out.println("EventPublisher.setApplicationContext() called");
        this.applicationContext = applicationContext;
    }
    
    public void publishUserCreated(String email) {
        System.out.println("\n1. Publishing UserCreatedEvent");
        UserCreatedEvent event = new UserCreatedEvent(this, email);
        applicationContext.publishEvent(event);
    }
    
    public void publishOrderPlaced(String orderId, double amount) {
        System.out.println("\n1. Publishing OrderPlacedEvent");
        OrderPlacedEvent event = new OrderPlacedEvent(this, orderId, amount);
        applicationContext.publishEvent(event);
    }
    
    public void publishCustomEvent(String message) {
        applicationContext.publishEvent(new CustomEvent(this, message));
    }
}

/**
 * Custom Application Events
 */
class UserCreatedEvent extends ApplicationEvent {
    private final String email;
    
    public UserCreatedEvent(Object source, String email) {
        super(source);
        this.email = email;
    }
    
    public String getEmail() {
        return email;
    }
}

class OrderPlacedEvent extends ApplicationEvent {
    private final String orderId;
    private final double amount;
    
    public OrderPlacedEvent(Object source, String orderId, double amount) {
        super(source);
        this.orderId = orderId;
        this.amount = amount;
    }
    
    public String getOrderId() {
        return orderId;
    }
    
    public double getAmount() {
        return amount;
    }
}

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
 * Event Listeners
 */
@Component
class UserEventListener {
    
    @EventListener
    public void onUserCreated(UserCreatedEvent event) {
        System.out.println("   UserEventListener received event");
        System.out.println("   User email: " + event.getEmail());
    }
}

@Component
class OrderEventListener {
    
    @EventListener
    public void onOrderPlaced(OrderPlacedEvent event) {
        System.out.println("   OrderEventListener received event");
        System.out.println("   Order ID: " + event.getOrderId());
        System.out.println("   Amount: $" + event.getAmount());
    }
}

/**
 * Example 2: Property Accessor using ApplicationContext
 */
@Component
class PropertyAccessor implements ApplicationContextAware {
    
    private ApplicationContext applicationContext;
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        System.out.println("PropertyAccessor.setApplicationContext() called");
        this.applicationContext = applicationContext;
    }
    
    public void displayProperties() {
        System.out.println("\n2. Accessing Environment Properties:");
        
        Environment env = applicationContext.getEnvironment();
        
        // Get active profiles
        String[] profiles = env.getActiveProfiles();
        System.out.println("   Active Profiles: " + String.join(", ", profiles));
        
        // Get properties
        String appName = env.getProperty("spring.application.name", "MyApp");
        System.out.println("   Application Name: " + appName);
        
        String port = env.getProperty("server.port", "8080");
        System.out.println("   Server Port: " + port);
        
        // Get application ID
        String appId = applicationContext.getId();
        System.out.println("   Application ID: " + appId);
        
        // Get display name
        String displayName = applicationContext.getDisplayName();
        System.out.println("   Display Name: " + displayName);
    }
    
    public String getProperty(String key, String defaultValue) {
        return applicationContext.getEnvironment().getProperty(key, defaultValue);
    }
    
    public boolean hasProfile(String profile) {
        return applicationContext.getEnvironment().acceptsProfiles(profile);
    }
}

/**
 * Example 3: Resource Loader using ApplicationContext
 */
@Component
class ResourceLoader implements ApplicationContextAware {
    
    private ApplicationContext applicationContext;
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        System.out.println("ResourceLoader.setApplicationContext() called");
        this.applicationContext = applicationContext;
    }
    
    public void loadResources() {
        System.out.println("\n3. Loading Resources:");
        
        try {
            // Load resource from classpath
            org.springframework.core.io.Resource resource = 
                applicationContext.getResource("classpath:application.properties");
            System.out.println("   Resource exists: " + resource.exists());
            System.out.println("   Resource file: " + resource.getFilename());
            
            // Load multiple resources
            org.springframework.core.io.Resource[] resources = 
                applicationContext.getResources("classpath*:*.xml");
            System.out.println("   Found XML resources: " + resources.length);
            
        } catch (Exception e) {
            System.out.println("   Resource loading: " + e.getMessage());
        }
    }
    
    public org.springframework.core.io.Resource loadResource(String location) {
        return applicationContext.getResource(location);
    }
}

/**
 * Example 4: Bean Registry using ApplicationContext
 */
@Component
class BeanRegistry implements ApplicationContextAware {
    
    private ApplicationContext applicationContext;
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        System.out.println("BeanRegistry.setApplicationContext() called");
        this.applicationContext = applicationContext;
    }
    
    public void listAllServices() {
        System.out.println("\n4. Listing All Service Beans:");
        
        Map<String, Object> services = applicationContext.getBeansWithAnnotation(Component.class);
        services.forEach((name, bean) -> {
            if (name.toLowerCase().contains("service")) {
                System.out.println("   " + name + " -> " + bean.getClass().getSimpleName());
            }
        });
    }
    
    public Map<String, Object> getBeansOfType(Class<?> type) {
        return applicationContext.getBeansOfType((Class<Object>) type);
    }
    
    public String[] getBeanNamesForType(Class<?> type) {
        return applicationContext.getBeanNamesForType(type);
    }
    
    public int getBeanDefinitionCount() {
        return applicationContext.getBeanDefinitionCount();
    }
}

/**
 * Example 5: Application Manager
 */
@Component
class ApplicationManager implements ApplicationContextAware {
    
    private ApplicationContext applicationContext;
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
    
    public void displayApplicationInfo() {
        System.out.println("Application Information:");
        System.out.println("  ID: " + applicationContext.getId());
        System.out.println("  Display Name: " + applicationContext.getDisplayName());
        System.out.println("  Startup Date: " + new java.util.Date(applicationContext.getStartupDate()));
        System.out.println("  Bean Count: " + applicationContext.getBeanDefinitionCount());
        
        // Parent context
        ApplicationContext parent = applicationContext.getParent();
        System.out.println("  Has Parent Context: " + (parent != null));
    }
    
    public boolean isActive() {
        return applicationContext.isActive();
    }
    
    public long getStartupDate() {
        return applicationContext.getStartupDate();
    }
}

/**
 * REST Controller
 */
@RestController
@RequestMapping("/api/application-context-aware")
class ApplicationContextAwareController {
    
    private final EventPublisher eventPublisher;
    private final PropertyAccessor propertyAccessor;
    private final BeanRegistry beanRegistry;
    
    public ApplicationContextAwareController(EventPublisher eventPublisher,
                                            PropertyAccessor propertyAccessor,
                                            BeanRegistry beanRegistry) {
        this.eventPublisher = eventPublisher;
        this.propertyAccessor = propertyAccessor;
        this.beanRegistry = beanRegistry;
    }
    
    @PostMapping("/events/user/{email}")
    public String publishUserEvent(@PathVariable String email) {
        eventPublisher.publishUserCreated(email);
        return "User event published: " + email;
    }
    
    @PostMapping("/events/order/{orderId}/{amount}")
    public String publishOrderEvent(@PathVariable String orderId, @PathVariable double amount) {
        eventPublisher.publishOrderPlaced(orderId, amount);
        return "Order event published: " + orderId;
    }
    
    @GetMapping("/properties/{key}")
    public String getProperty(@PathVariable String key) {
        return propertyAccessor.getProperty(key, "Not found");
    }
    
    @GetMapping("/beans")
    public Map<String, Object> getAllBeans() {
        return beanRegistry.getBeansOfType(Object.class);
    }
}

/**
 * Key Points:
 * 
 * 1. ApplicationContextAware Interface:
 *    public interface ApplicationContextAware extends Aware {
 *        void setApplicationContext(ApplicationContext context) 
 *            throws BeansException;
 *    }
 * 
 * 2. ApplicationContext vs BeanFactory:
 *    ApplicationContext extends BeanFactory with:
 *    ✓ Event publishing
 *    ✓ Environment access
 *    ✓ Resource loading
 *    ✓ Message source (i18n)
 *    ✓ Application lifecycle
 * 
 * 3. ApplicationContext Capabilities:
 *    ✓ publishEvent() - Publish events
 *    ✓ getEnvironment() - Access properties
 *    ✓ getResource() - Load resources
 *    ✓ getBeansOfType() - Find beans by type
 *    ✓ getBeanNamesForType() - Get bean names
 *    ✓ getParent() - Parent context
 *    ✓ getId() - Application ID
 *    ✓ getStartupDate() - Startup time
 * 
 * 4. Use Cases:
 *    ✓ Event publishing/listening
 *    ✓ Property access
 *    ✓ Resource loading
 *    ✓ Bean discovery
 *    ✓ Application management
 *    ✓ Framework integration
 * 
 * 5. Best Practices:
 *    ✓ Prefer specific interfaces (EnvironmentAware, etc.)
 *    ✓ Use @EventListener instead of ApplicationListener
 *    ✓ Prefer @Value for properties
 *    ✓ Use dependency injection when possible
 *    ✓ Document why context access needed
 * 
 * 6. Testing:
 *    @Test
 *    void testApplicationContextAware() {
 *        ApplicationContext context = mock(ApplicationContext.class);
 *        EventPublisher publisher = new EventPublisher();
 *        publisher.setApplicationContext(context);
 *        
 *        publisher.publishUserCreated("test@test.com");
 *        verify(context).publishEvent(any(UserCreatedEvent.class));
 *    }
 */
