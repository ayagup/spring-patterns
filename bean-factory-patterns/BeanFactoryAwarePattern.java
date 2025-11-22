package com.spring.patterns.factory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * BeanFactoryAware Pattern
 * 
 * BeanFactoryAware interface allows beans to be aware of the BeanFactory that created them.
 * Spring injects the BeanFactory reference via setBeanFactory() callback.
 * 
 * Characteristics:
 * - Implements BeanFactoryAware interface
 * - setBeanFactory() called during bean initialization
 * - Provides access to BeanFactory
 * - Can lookup other beans programmatically
 * - Useful for dynamic bean retrieval
 * - Creates coupling to Spring framework
 * 
 * Use Cases:
 * - Dynamic bean lookup
 * - Plugin architecture
 * - Strategy pattern implementation
 * - Bean introspection
 * - Custom bean lifecycle management
 * - Service locator pattern
 */
@SpringBootApplication
public class BeanFactoryAwarePattern {

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(BeanFactoryAwarePattern.class, args);
        
        System.out.println("\n=== BeanFactoryAware Pattern ===");
        
        // Example 1: Service Locator using BeanFactoryAware
        ServiceLocator locator = context.getBean(ServiceLocator.class);
        locator.getService("userService").execute();
        locator.getService("orderService").execute();
        
        // Example 2: Plugin Manager
        PluginManager pluginManager = context.getBean(PluginManager.class);
        pluginManager.loadPlugin("emailPlugin");
        pluginManager.loadPlugin("smsPlugin");
        
        // Example 3: Strategy Resolver
        StrategyResolver resolver = context.getBean(StrategyResolver.class);
        resolver.executeStrategy("aggressive");
        resolver.executeStrategy("conservative");
        
        // Example 4: Bean Inspector
        BeanInspector inspector = context.getBean(BeanInspector.class);
        inspector.inspect();
    }
}

/**
 * Example 1: Service Locator Pattern
 * Uses BeanFactory to dynamically lookup services
 */
@Component
class ServiceLocator implements BeanFactoryAware {
    
    private BeanFactory beanFactory;
    
    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        System.out.println("ServiceLocator.setBeanFactory() called");
        this.beanFactory = beanFactory;
    }
    
    /**
     * Dynamically retrieve service by name
     */
    public Service getService(String serviceName) {
        System.out.println("\n1. ServiceLocator looking up: " + serviceName);
        
        if (beanFactory.containsBean(serviceName)) {
            return beanFactory.getBean(serviceName, Service.class);
        }
        
        throw new IllegalArgumentException("Service not found: " + serviceName);
    }
    
    /**
     * Check if service exists
     */
    public boolean hasService(String serviceName) {
        return beanFactory.containsBean(serviceName);
    }
    
    /**
     * Get all services of type
     */
    public java.util.Map<String, Service> getAllServices() {
        return beanFactory.getBeansOfType(Service.class);
    }
}

interface Service {
    void execute();
    String getName();
}

@Component("userService")
class UserService implements Service {
    @Override
    public void execute() {
        System.out.println("   UserService executing...");
    }
    
    @Override
    public String getName() {
        return "UserService";
    }
}

@Component("orderService")
class OrderService implements Service {
    @Override
    public void execute() {
        System.out.println("   OrderService executing...");
    }
    
    @Override
    public String getName() {
        return "OrderService";
    }
}

@Component("paymentService")
class PaymentService implements Service {
    @Override
    public void execute() {
        System.out.println("   PaymentService executing...");
    }
    
    @Override
    public String getName() {
        return "PaymentService";
    }
}

/**
 * Example 2: Plugin Manager
 * Dynamically loads plugins using BeanFactory
 */
@Component
class PluginManager implements BeanFactoryAware {
    
    private BeanFactory beanFactory;
    
    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        System.out.println("PluginManager.setBeanFactory() called");
        this.beanFactory = beanFactory;
    }
    
    public void loadPlugin(String pluginName) {
        System.out.println("\n2. Loading plugin: " + pluginName);
        
        if (beanFactory.containsBean(pluginName)) {
            Plugin plugin = beanFactory.getBean(pluginName, Plugin.class);
            plugin.initialize();
            plugin.start();
        } else {
            System.out.println("   Plugin not found: " + pluginName);
        }
    }
    
    public void unloadPlugin(String pluginName) {
        if (beanFactory.containsBean(pluginName)) {
            Plugin plugin = beanFactory.getBean(pluginName, Plugin.class);
            plugin.stop();
        }
    }
}

interface Plugin {
    void initialize();
    void start();
    void stop();
}

@Component("emailPlugin")
class EmailPlugin implements Plugin {
    @Override
    public void initialize() {
        System.out.println("   EmailPlugin initialized");
    }
    
    @Override
    public void start() {
        System.out.println("   EmailPlugin started");
    }
    
    @Override
    public void stop() {
        System.out.println("   EmailPlugin stopped");
    }
}

@Component("smsPlugin")
class SmsPlugin implements Plugin {
    @Override
    public void initialize() {
        System.out.println("   SmsPlugin initialized");
    }
    
    @Override
    public void start() {
        System.out.println("   SmsPlugin started");
    }
    
    @Override
    public void stop() {
        System.out.println("   SmsPlugin stopped");
    }
}

/**
 * Example 3: Strategy Resolver
 * Resolves strategies dynamically using BeanFactory
 */
@Component
class StrategyResolver implements BeanFactoryAware {
    
    private BeanFactory beanFactory;
    
    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        System.out.println("StrategyResolver.setBeanFactory() called");
        this.beanFactory = beanFactory;
    }
    
    public void executeStrategy(String strategyType) {
        System.out.println("\n3. Resolving strategy: " + strategyType);
        
        String beanName = strategyType + "Strategy";
        
        if (beanFactory.containsBean(beanName)) {
            TradingStrategy strategy = beanFactory.getBean(beanName, TradingStrategy.class);
            strategy.trade(1000.0);
        } else {
            System.out.println("   Strategy not found: " + strategyType);
        }
    }
}

interface TradingStrategy {
    void trade(double amount);
}

@Component("aggressiveStrategy")
class AggressiveStrategy implements TradingStrategy {
    @Override
    public void trade(double amount) {
        System.out.println("   Aggressive trading with $" + amount);
        System.out.println("   Risk level: HIGH");
    }
}

@Component("conservativeStrategy")
class ConservativeStrategy implements TradingStrategy {
    @Override
    public void trade(double amount) {
        System.out.println("   Conservative trading with $" + amount);
        System.out.println("   Risk level: LOW");
    }
}

@Component("moderateStrategy")
class ModerateStrategy implements TradingStrategy {
    @Override
    public void trade(double amount) {
        System.out.println("   Moderate trading with $" + amount);
        System.out.println("   Risk level: MEDIUM");
    }
}

/**
 * Example 4: Bean Inspector
 * Inspects beans using BeanFactory
 */
@Component
class BeanInspector implements BeanFactoryAware {
    
    private BeanFactory beanFactory;
    
    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        System.out.println("BeanInspector.setBeanFactory() called");
        this.beanFactory = beanFactory;
    }
    
    public void inspect() {
        System.out.println("\n4. Bean Inspection:");
        
        // Check if beans exist
        System.out.println("   userService exists: " + beanFactory.containsBean("userService"));
        System.out.println("   orderService exists: " + beanFactory.containsBean("orderService"));
        
        // Check bean type
        if (beanFactory.containsBean("userService")) {
            Class<?> type = beanFactory.getType("userService");
            System.out.println("   userService type: " + type.getSimpleName());
        }
        
        // Check if singleton
        System.out.println("   userService is singleton: " + 
                         beanFactory.isSingleton("userService"));
        
        // Check if prototype
        System.out.println("   userService is prototype: " + 
                         beanFactory.isPrototype("userService"));
    }
    
    public boolean isBeanSingleton(String beanName) {
        return beanFactory.isSingleton(beanName);
    }
    
    public Class<?> getBeanType(String beanName) {
        return beanFactory.getType(beanName);
    }
}

/**
 * Example 5: Dynamic Bean Creator
 */
@Component
class DynamicBeanCreator implements BeanFactoryAware {
    
    private BeanFactory beanFactory;
    
    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }
    
    public Object createBean(String beanType) {
        switch (beanType.toLowerCase()) {
            case "user":
                return beanFactory.getBean(UserService.class);
            case "order":
                return beanFactory.getBean(OrderService.class);
            case "payment":
                return beanFactory.getBean(PaymentService.class);
            default:
                throw new IllegalArgumentException("Unknown bean type: " + beanType);
        }
    }
}

/**
 * REST Controller
 */
@RestController
@RequestMapping("/api/bean-factory-aware")
class BeanFactoryAwareController {
    
    private final ServiceLocator serviceLocator;
    private final StrategyResolver strategyResolver;
    
    public BeanFactoryAwareController(ServiceLocator serviceLocator,
                                     StrategyResolver strategyResolver) {
        this.serviceLocator = serviceLocator;
        this.strategyResolver = strategyResolver;
    }
    
    @GetMapping("/service/{name}")
    public String executeService(@PathVariable String name) {
        Service service = serviceLocator.getService(name);
        service.execute();
        return "Service executed: " + service.getName();
    }
    
    @GetMapping("/strategy/{type}")
    public String executeStrategy(@PathVariable String type) {
        strategyResolver.executeStrategy(type);
        return "Strategy executed: " + type;
    }
}

/**
 * Key Points:
 * 
 * 1. BeanFactoryAware Interface:
 *    public interface BeanFactoryAware extends Aware {
 *        void setBeanFactory(BeanFactory beanFactory) throws BeansException;
 *    }
 * 
 * 2. Usage:
 *    @Component
 *    class MyBean implements BeanFactoryAware {
 *        private BeanFactory beanFactory;
 *        
 *        @Override
 *        public void setBeanFactory(BeanFactory beanFactory) {
 *            this.beanFactory = beanFactory;
 *        }
 *    }
 * 
 * 3. Callback Lifecycle:
 *    1. Bean instantiated
 *    2. Dependencies injected
 *    3. setBeanFactory() called
 *    4. @PostConstruct methods called
 *    5. Bean ready for use
 * 
 * 4. BeanFactory Capabilities:
 *    ✓ getBean(name) - Retrieve bean by name
 *    ✓ getBean(Class) - Retrieve bean by type
 *    ✓ containsBean(name) - Check if bean exists
 *    ✓ isSingleton(name) - Check if singleton
 *    ✓ isPrototype(name) - Check if prototype
 *    ✓ getType(name) - Get bean type
 *    ✓ getAliases(name) - Get bean aliases
 * 
 * 5. Use Cases:
 *    ✓ Service Locator pattern
 *    ✓ Plugin architecture
 *    ✓ Strategy pattern
 *    ✓ Dynamic bean lookup
 *    ✓ Bean introspection
 *    ✓ Custom lifecycle management
 * 
 * 6. Advantages:
 *    ✓ Dynamic bean retrieval
 *    ✓ Runtime bean lookup
 *    ✓ Flexible architecture
 *    ✓ Plugin support
 *    ✓ Strategy selection
 * 
 * 7. Disadvantages:
 *    ✗ Creates Spring coupling
 *    ✗ Less testable
 *    ✗ Hides dependencies
 *    ✗ Can lead to service locator anti-pattern
 * 
 * 8. Best Practices:
 *    ✓ Prefer dependency injection when possible
 *    ✓ Use only when truly dynamic lookup needed
 *    ✓ Document why BeanFactory access is required
 *    ✓ Consider alternatives (ApplicationContext, Provider)
 *    ✓ Don't abuse for lazy initialization
 * 
 * 9. Alternatives:
 *    - @Autowired List<Service> (for all beans of type)
 *    - ApplicationContextAware (more features)
 *    - ObjectProvider<T> (lazy, optional lookup)
 *    - @Lookup method injection
 * 
 * 10. Testing:
 *     @Test
 *     void testBeanFactoryAware() {
 *         BeanFactory factory = mock(BeanFactory.class);
 *         ServiceLocator locator = new ServiceLocator();
 *         locator.setBeanFactory(factory);
 *         
 *         when(factory.getBean("userService", Service.class))
 *             .thenReturn(new UserService());
 *         
 *         Service service = locator.getService("userService");
 *         assertNotNull(service);
 *     }
 */
