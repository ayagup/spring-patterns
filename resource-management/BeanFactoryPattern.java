package com.spring.patterns.resourcemanagement;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.ClassPathResource;

/**
 * Bean Factory Pattern
 * 
 * Demonstrates Spring's BeanFactory as the fundamental container for
 * bean management. Shows the difference between BeanFactory and ApplicationContext,
 * and demonstrates programmatic bean registration.
 * 
 * Key Concepts:
 * - BeanFactory is the root interface for Spring IoC container
 * - Lazy initialization by default (beans created on demand)
 * - ApplicationContext extends BeanFactory with more features
 * - Supports programmatic bean registration and retrieval
 */

@Configuration
class BeanFactoryConfig {
    
    @Bean
    public ProductService productService() {
        return new ProductService("Product Service Bean");
    }
    
    @Bean
    @Scope("prototype")
    public PrototypeBean prototypeBean() {
        return new PrototypeBean();
    }
    
    @Bean
    public OrderService orderService(ProductService productService) {
        return new OrderService(productService);
    }
}

/**
 * Simple service bean
 */
class ProductService {
    
    private final String name;
    private static int instanceCount = 0;
    
    public ProductService(String name) {
        this.name = name;
        instanceCount++;
        System.out.println("ProductService created: " + name + " (Instance #" + instanceCount + ")");
    }
    
    public String getName() {
        return name;
    }
    
    public void processProduct(String product) {
        System.out.println("Processing product: " + product);
    }
    
    public static int getInstanceCount() {
        return instanceCount;
    }
}

/**
 * Prototype scoped bean
 */
class PrototypeBean {
    
    private static int instanceCount = 0;
    private final int id;
    
    public PrototypeBean() {
        instanceCount++;
        this.id = instanceCount;
        System.out.println("PrototypeBean created: Instance #" + id);
    }
    
    public int getId() {
        return id;
    }
    
    public static int getInstanceCount() {
        return instanceCount;
    }
}

/**
 * Service with dependency
 */
class OrderService {
    
    private final ProductService productService;
    
    public OrderService(ProductService productService) {
        this.productService = productService;
        System.out.println("OrderService created with dependency: " + 
                productService.getName());
    }
    
    public void processOrder(String orderId) {
        System.out.println("Processing order: " + orderId);
        productService.processProduct("Product for order " + orderId);
    }
}

/**
 * Demonstrating programmatic bean registration
 */
class BeanFactoryManager {
    
    private final DefaultListableBeanFactory beanFactory;
    
    public BeanFactoryManager() {
        this.beanFactory = new DefaultListableBeanFactory();
    }
    
    /**
     * Register bean programmatically
     */
    public void registerBean(String beanName, Class<?> beanClass) {
        BeanDefinition beanDefinition = BeanDefinitionBuilder
                .rootBeanDefinition(beanClass)
                .getBeanDefinition();
        
        beanFactory.registerBeanDefinition(beanName, beanDefinition);
        System.out.println("Registered bean: " + beanName + " (" + 
                beanClass.getSimpleName() + ")");
    }
    
    /**
     * Register singleton bean with instance
     */
    public void registerSingleton(String beanName, Object instance) {
        beanFactory.registerSingleton(beanName, instance);
        System.out.println("Registered singleton: " + beanName);
    }
    
    /**
     * Get bean from factory
     */
    public <T> T getBean(String beanName, Class<T> requiredType) {
        return beanFactory.getBean(beanName, requiredType);
    }
    
    /**
     * Get bean by type
     */
    public <T> T getBean(Class<T> requiredType) {
        return beanFactory.getBean(requiredType);
    }
    
    /**
     * Check if bean exists
     */
    public boolean containsBean(String beanName) {
        return beanFactory.containsBean(beanName);
    }
    
    /**
     * Get bean definition count
     */
    public int getBeanDefinitionCount() {
        return beanFactory.getBeanDefinitionCount();
    }
    
    /**
     * List all bean names
     */
    public String[] getBeanDefinitionNames() {
        return beanFactory.getBeanDefinitionNames();
    }
    
    public BeanFactory getBeanFactory() {
        return beanFactory;
    }
}

/**
 * Custom bean for demonstration
 */
class CustomerService {
    
    private String name;
    private int customerId;
    
    public CustomerService() {
        System.out.println("CustomerService created (no-arg constructor)");
    }
    
    public CustomerService(String name, int customerId) {
        this.name = name;
        this.customerId = customerId;
        System.out.println("CustomerService created: " + name + " (ID: " + customerId + ")");
    }
    
    public void serve() {
        System.out.println("Serving customer: " + name);
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public int getCustomerId() {
        return customerId;
    }
    
    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }
}

public class BeanFactoryPattern {
    
    public static void main(String[] args) {
        System.out.println("=== Spring Bean Factory Pattern Demo ===\n");
        
        // Demo 1: Basic BeanFactory usage
        System.out.println("1. Basic BeanFactory Usage:");
        demonstrateBasicBeanFactory();
        
        // Demo 2: Programmatic bean registration
        System.out.println("\n2. Programmatic Bean Registration:");
        demonstrateProgrammaticRegistration();
        
        // Demo 3: Singleton vs Prototype scope
        System.out.println("\n3. Singleton vs Prototype Scope:");
        demonstrateBeanScopes();
        
        // Demo 4: Lazy initialization
        System.out.println("\n4. Lazy Initialization:");
        demonstrateLazyInitialization();
        
        // Demo 5: Bean lifecycle in BeanFactory
        System.out.println("\n5. Bean Lifecycle:");
        demonstrateBeanLifecycle();
        
        // Demo 6: Dependency injection
        System.out.println("\n6. Dependency Injection:");
        demonstrateDependencyInjection();
        
        System.out.println("\n=== Demo Completed ===");
    }
    
    private static void demonstrateBasicBeanFactory() {
        // Create BeanFactory through ApplicationContext
        AnnotationConfigApplicationContext context = 
                new AnnotationConfigApplicationContext(BeanFactoryConfig.class);
        
        BeanFactory beanFactory = context.getBeanFactory();
        
        System.out.println("BeanFactory created");
        
        // Get bean from factory
        ProductService service = beanFactory.getBean("productService", ProductService.class);
        service.processProduct("Laptop");
        
        // Check if bean exists
        boolean exists = beanFactory.containsBean("productService");
        System.out.println("Bean 'productService' exists: " + exists);
        
        // Check singleton status
        boolean isSingleton = beanFactory.isSingleton("productService");
        System.out.println("Bean 'productService' is singleton: " + isSingleton);
        
        // Get same bean again (should be same instance for singleton)
        ProductService service2 = beanFactory.getBean("productService", ProductService.class);
        System.out.println("Same instance: " + (service == service2));
        
        context.close();
    }
    
    private static void demonstrateProgrammaticRegistration() {
        BeanFactoryManager manager = new BeanFactoryManager();
        
        System.out.println("Initial bean count: " + manager.getBeanDefinitionCount());
        
        // Register bean definition
        manager.registerBean("productService", ProductService.class);
        manager.registerBean("customerService", CustomerService.class);
        
        System.out.println("Bean count after registration: " + 
                manager.getBeanDefinitionCount());
        
        // Register singleton instance
        ProductService existingInstance = new ProductService("Existing Product Service");
        manager.registerSingleton("existingProduct", existingInstance);
        
        // Get registered beans
        System.out.println("\nRetrieving beans:");
        ProductService product = manager.getBean("productService", ProductService.class);
        product.processProduct("Desktop");
        
        CustomerService customer = manager.getBean("customerService", CustomerService.class);
        customer.setName("John Doe");
        customer.setCustomerId(1001);
        customer.serve();
        
        // List all beans
        System.out.println("\nAll registered beans:");
        String[] beanNames = manager.getBeanDefinitionNames();
        for (String name : beanNames) {
            System.out.println("  - " + name);
        }
    }
    
    private static void demonstrateBeanScopes() {
        AnnotationConfigApplicationContext context = 
                new AnnotationConfigApplicationContext(BeanFactoryConfig.class);
        
        BeanFactory beanFactory = context.getBeanFactory();
        
        // Singleton scope (default)
        System.out.println("\nSingleton Scope:");
        ProductService singleton1 = beanFactory.getBean("productService", ProductService.class);
        ProductService singleton2 = beanFactory.getBean("productService", ProductService.class);
        System.out.println("Same instance: " + (singleton1 == singleton2));
        System.out.println("Instance count: " + ProductService.getInstanceCount());
        
        // Prototype scope
        System.out.println("\nPrototype Scope:");
        PrototypeBean prototype1 = beanFactory.getBean("prototypeBean", PrototypeBean.class);
        PrototypeBean prototype2 = beanFactory.getBean("prototypeBean", PrototypeBean.class);
        System.out.println("Same instance: " + (prototype1 == prototype2));
        System.out.println("Prototype 1 ID: " + prototype1.getId());
        System.out.println("Prototype 2 ID: " + prototype2.getId());
        System.out.println("Total instances created: " + PrototypeBean.getInstanceCount());
        
        context.close();
    }
    
    private static void demonstrateLazyInitialization() {
        System.out.println("Creating BeanFactory...");
        
        // BeanFactory uses lazy initialization by default
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        
        // Register bean definition
        BeanDefinition beanDefinition = BeanDefinitionBuilder
                .rootBeanDefinition(ProductService.class)
                .addConstructorArgValue("Lazy Product Service")
                .setLazyInit(true)
                .getBeanDefinition();
        
        beanFactory.registerBeanDefinition("lazyProduct", beanDefinition);
        
        System.out.println("Bean definition registered (not yet created)");
        System.out.println("Bean exists in factory: " + beanFactory.containsBean("lazyProduct"));
        
        // Bean is created only when requested
        System.out.println("\nRequesting bean for the first time...");
        ProductService service = beanFactory.getBean("lazyProduct", ProductService.class);
        
        System.out.println("\nRequesting bean again...");
        ProductService service2 = beanFactory.getBean("lazyProduct", ProductService.class);
        System.out.println("Same instance: " + (service == service2));
    }
    
    private static void demonstrateBeanLifecycle() {
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        
        System.out.println("Phase 1: Bean Definition Registration");
        
        // Register bean definition with constructor args
        BeanDefinition beanDef = BeanDefinitionBuilder
                .rootBeanDefinition(CustomerService.class)
                .addConstructorArgValue("Alice")
                .addConstructorArgValue(2001)
                .getBeanDefinition();
        
        beanFactory.registerBeanDefinition("customer", beanDef);
        System.out.println("Bean definition registered");
        
        System.out.println("\nPhase 2: Bean Instantiation (on first request)");
        CustomerService customer = beanFactory.getBean("customer", CustomerService.class);
        
        System.out.println("\nPhase 3: Bean Usage");
        customer.serve();
        
        System.out.println("\nPhase 4: Bean Retrieval (existing instance)");
        CustomerService sameCustomer = beanFactory.getBean("customer", CustomerService.class);
        System.out.println("Same instance: " + (customer == sameCustomer));
        
        System.out.println("\nPhase 5: Factory Shutdown");
        beanFactory.destroySingletons();
        System.out.println("Singletons destroyed");
    }
    
    private static void demonstrateDependencyInjection() {
        AnnotationConfigApplicationContext context = 
                new AnnotationConfigApplicationContext(BeanFactoryConfig.class);
        
        BeanFactory beanFactory = context.getBeanFactory();
        
        System.out.println("Getting bean with dependencies:");
        
        // OrderService has dependency on ProductService
        OrderService orderService = beanFactory.getBean("orderService", OrderService.class);
        
        System.out.println("\nUsing service with injected dependency:");
        orderService.processOrder("ORD-12345");
        
        // Verify dependency injection
        ProductService productService = beanFactory.getBean("productService", ProductService.class);
        System.out.println("\nProductService instance count: " + 
                ProductService.getInstanceCount());
        
        context.close();
    }
}

/*
 * Key Takeaways:
 * 
 * 1. BeanFactory is the root interface for Spring IoC container
 * 2. Provides basic dependency injection and bean lifecycle management
 * 3. Lazy initialization by default (beans created on demand)
 * 4. ApplicationContext extends BeanFactory with additional features
 * 5. Supports programmatic bean registration
 * 
 * BeanFactory vs ApplicationContext:
 * - BeanFactory: Lazy initialization, minimal features
 * - ApplicationContext: Eager initialization, more features (events, i18n, etc.)
 * - ApplicationContext is preferred for most applications
 * - BeanFactory useful for resource-constrained environments
 * 
 * BeanFactory Features:
 * - Bean instantiation and lifecycle management
 * - Dependency injection (constructor, setter)
 * - Bean scope management (singleton, prototype)
 * - Bean retrieval by name or type
 * - Bean existence checking
 * - Programmatic bean registration
 * 
 * Bean Scopes:
 * - singleton: One instance per container (default)
 * - prototype: New instance for each request
 * - request: One instance per HTTP request (web)
 * - session: One instance per HTTP session (web)
 * - application: One instance per ServletContext (web)
 * 
 * Benefits:
 * - Lightweight container for basic IoC needs
 * - Programmatic bean management
 * - Lazy initialization for resource efficiency
 * - Foundation for ApplicationContext
 * 
 * Use Cases:
 * - Resource-constrained environments
 * - Programmatic bean registration
 * - Testing and mocking
 * - Legacy application integration
 * - Custom container implementations
 */
