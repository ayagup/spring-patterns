package com.example.miscellaneous.beanfactorypostprocessor;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Bean Factory Post Processor Pattern - Demonstrates Spring's BeanFactoryPostProcessor
 * 
 * This pattern shows how to:
 * 1. Implement BeanFactoryPostProcessor interface
 * 2. Modify bean definitions before instantiation
 * 3. Register new bean definitions
 * 4. Modify bean properties
 * 5. Change bean scopes
 * 6. Add bean dependencies
 * 7. Process configuration metadata
 * 8. Implement custom placeholders
 * 9. Validate bean configuration
 * 10. Apply conditional bean creation
 * 
 * Key Concepts:
 * - BeanFactoryPostProcessor: Modifies bean definitions before beans are created
 * - BeanDefinitionRegistryPostProcessor: Can add new bean definitions
 * - ConfigurableListableBeanFactory: Access to bean factory
 * - BeanDefinition: Metadata about a bean
 * - Property Placeholder: Resolves ${...} expressions
 * 
 * Execution Order:
 * 1. BeanDefinitionRegistryPostProcessor.postProcessBeanDefinitionRegistry
 * 2. BeanDefinitionRegistryPostProcessor.postProcessBeanFactory
 * 3. BeanFactoryPostProcessor.postProcessBeanFactory
 * 4. Bean instantiation begins
 * 5. BeanPostProcessor methods execute
 * 
 * Common Use Cases:
 * - Property placeholder resolution
 * - Custom bean definition modification
 * - Conditional bean registration
 * - Bean definition validation
 * - Dynamic bean creation
 * 
 * Dependencies:
 * - spring-context
 * - spring-boot-starter-web
 * 
 * @author Spring Patterns
 * @version 1.0
 */
@SpringBootApplication
public class BeanFactoryPostProcessorPattern {
    
    public static void main(String[] args) {
        var context = SpringApplication.run(BeanFactoryPostProcessorPattern.class, args);
        demonstrateBeanFactoryPostProcessors(context);
    }
    
    /**
     * Demonstrates bean factory post processor functionality
     */
    private static void demonstrateBeanFactoryPostProcessors(
            org.springframework.context.ApplicationContext context) {
        System.out.println("=== Bean Factory Post Processor Pattern Demonstrations ===\n");
        
        // Demo 1: Check bean definition modifications
        System.out.println("1. Bean Definition Modifications:");
        BeanDefinitionTracker tracker = context.getBean(BeanDefinitionTracker.class);
        System.out.println("   Total bean definitions: " + tracker.getTotalBeanDefinitions());
        System.out.println("   Modified beans: " + tracker.getModifiedBeans().size());
        System.out.println();
        
        // Demo 2: Check dynamically registered beans
        System.out.println("2. Dynamically Registered Beans:");
        System.out.println("   Dynamic beans: " + tracker.getDynamicBeans());
        System.out.println();
    }
}

// ============================================================================
// Bean Factory Post Processors
// ============================================================================

/**
 * Bean Definition Modifier - modifies existing bean definitions
 */
@Component
class BeanDefinitionModifierPostProcessor implements BeanFactoryPostProcessor {
    
    private final BeanDefinitionTracker tracker;
    
    public BeanDefinitionModifierPostProcessor(BeanDefinitionTracker tracker) {
        this.tracker = tracker;
    }
    
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) 
            throws BeansException {
        System.out.println("   [BeanDefModifier] Processing bean factory");
        
        String[] beanNames = beanFactory.getBeanDefinitionNames();
        tracker.setTotalBeanDefinitions(beanNames.length);
        
        for (String beanName : beanNames) {
            BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
            
            // Modify specific beans
            if (beanName.contains("Service")) {
                System.out.println("   [BeanDefModifier] Modifying service bean: " + beanName);
                tracker.recordModifiedBean(beanName);
                
                // Example modifications
                beanDefinition.setLazyInit(true);
            }
        }
    }
}

/**
 * Dynamic Bean Registrar - registers new bean definitions
 */
@Component
class DynamicBeanRegistrarPostProcessor implements BeanDefinitionRegistryPostProcessor {
    
    private final BeanDefinitionTracker tracker;
    
    public DynamicBeanRegistrarPostProcessor(BeanDefinitionTracker tracker) {
        this.tracker = tracker;
    }
    
    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) 
            throws BeansException {
        System.out.println("   [DynamicRegistrar] Registering dynamic beans");
        
        // Register dynamic bean
        GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
        beanDefinition.setBeanClass(DynamicService.class);
        beanDefinition.setScope(BeanDefinition.SCOPE_SINGLETON);
        
        registry.registerBeanDefinition("dynamicService", beanDefinition);
        tracker.recordDynamicBean("dynamicService");
        
        System.out.println("   [DynamicRegistrar] Registered dynamic service bean");
    }
    
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) 
            throws BeansException {
        System.out.println("   [DynamicRegistrar] Post-processing bean factory");
    }
}

/**
 * Property Placeholder Processor - custom placeholder resolution
 */
@Component
class CustomPropertyPlaceholderPostProcessor implements BeanFactoryPostProcessor {
    
    private final Map<String, String> properties = new HashMap<>();
    
    public CustomPropertyPlaceholderPostProcessor() {
        // Initialize custom properties
        properties.put("app.name", "Spring Patterns App");
        properties.put("app.version", "1.0.0");
        properties.put("app.environment", "development");
    }
    
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) 
            throws BeansException {
        System.out.println("   [PropertyPlaceholder] Processing custom properties");
        
        // Process property placeholders
        String[] beanNames = beanFactory.getBeanDefinitionNames();
        for (String beanName : beanNames) {
            BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
            processPropertyValues(beanDefinition);
        }
    }
    
    private void processPropertyValues(BeanDefinition beanDefinition) {
        // Process property values with placeholders
        // Implementation would resolve ${...} expressions
    }
    
    public String getProperty(String key) {
        return properties.get(key);
    }
}

/**
 * Bean Scope Modifier - changes bean scopes based on conditions
 */
@Component
class BeanScopeModifierPostProcessor implements BeanFactoryPostProcessor {
    
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) 
            throws BeansException {
        System.out.println("   [ScopeModifier] Modifying bean scopes");
        
        String[] beanNames = beanFactory.getBeanDefinitionNames();
        for (String beanName : beanNames) {
            BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
            
            // Change scope for specific beans
            if (beanName.contains("Repository")) {
                beanDefinition.setScope(BeanDefinition.SCOPE_SINGLETON);
            }
        }
    }
}

// ============================================================================
// Bean Definition Tracker
// ============================================================================

/**
 * Tracks bean definition modifications
 */
@Component
class BeanDefinitionTracker {
    
    private int totalBeanDefinitions;
    private final Set<String> modifiedBeans = ConcurrentHashMap.newKeySet();
    private final Set<String> dynamicBeans = ConcurrentHashMap.newKeySet();
    private final Map<String, BeanDefinitionInfo> beanDefinitions = new ConcurrentHashMap<>();
    
    public void setTotalBeanDefinitions(int total) {
        this.totalBeanDefinitions = total;
    }
    
    public int getTotalBeanDefinitions() {
        return totalBeanDefinitions;
    }
    
    public void recordModifiedBean(String beanName) {
        modifiedBeans.add(beanName);
    }
    
    public Set<String> getModifiedBeans() {
        return new HashSet<>(modifiedBeans);
    }
    
    public void recordDynamicBean(String beanName) {
        dynamicBeans.add(beanName);
    }
    
    public Set<String> getDynamicBeans() {
        return new HashSet<>(dynamicBeans);
    }
    
    public void recordBeanDefinition(String beanName, BeanDefinitionInfo info) {
        beanDefinitions.put(beanName, info);
    }
    
    public Map<String, BeanDefinitionInfo> getBeanDefinitions() {
        return new HashMap<>(beanDefinitions);
    }
}

/**
 * Bean definition information
 */
class BeanDefinitionInfo {
    private String beanName;
    private String className;
    private String scope;
    private boolean lazyInit;
    private boolean singleton;
    
    public BeanDefinitionInfo(String beanName, String className, String scope, 
                            boolean lazyInit, boolean singleton) {
        this.beanName = beanName;
        this.className = className;
        this.scope = scope;
        this.lazyInit = lazyInit;
        this.singleton = singleton;
    }
    
    // Getters
    public String getBeanName() { return beanName; }
    public String getClassName() { return className; }
    public String getScope() { return scope; }
    public boolean isLazyInit() { return lazyInit; }
    public boolean isSingleton() { return singleton; }
}

// ============================================================================
// Services
// ============================================================================

/**
 * Example service that will be modified
 */
@Component
class ConfigurableService {
    
    public void performOperation() {
        System.out.println("   ConfigurableService: Performing operation");
    }
}

/**
 * Dynamically registered service
 */
class DynamicService {
    
    public void execute() {
        System.out.println("   DynamicService: Executing dynamic operation");
    }
}

// ============================================================================
// REST Controller
// ============================================================================

/**
 * Controller demonstrating bean factory post processor effects
 */
@RestController
@RequestMapping("/api/bean-factory-post-processor")
class BeanFactoryPostProcessorController {
    
    private final BeanDefinitionTracker tracker;
    private final CustomPropertyPlaceholderPostProcessor propertyProcessor;
    
    public BeanFactoryPostProcessorController(
            BeanDefinitionTracker tracker,
            CustomPropertyPlaceholderPostProcessor propertyProcessor) {
        this.tracker = tracker;
        this.propertyProcessor = propertyProcessor;
    }
    
    /**
     * Get bean definition statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalBeanDefinitions", tracker.getTotalBeanDefinitions());
        stats.put("modifiedBeans", tracker.getModifiedBeans());
        stats.put("dynamicBeans", tracker.getDynamicBeans());
        
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Get custom properties
     */
    @GetMapping("/properties")
    public ResponseEntity<Map<String, String>> getProperties() {
        Map<String, String> properties = new HashMap<>();
        properties.put("app.name", propertyProcessor.getProperty("app.name"));
        properties.put("app.version", propertyProcessor.getProperty("app.version"));
        properties.put("app.environment", propertyProcessor.getProperty("app.environment"));
        
        return ResponseEntity.ok(properties);
    }
    
    /**
     * Get modified beans
     */
    @GetMapping("/modified-beans")
    public ResponseEntity<Set<String>> getModifiedBeans() {
        return ResponseEntity.ok(tracker.getModifiedBeans());
    }
}

// ============================================================================
// Configuration
// ============================================================================

/**
 * Configuration for bean factory post processors
 */
@Configuration
class BeanFactoryPostProcessorConfiguration {
    
    /**
     * Custom bean factory post processor
     */
    @Bean
    public static BeanFactoryPostProcessor customBeanFactoryPostProcessor() {
        return beanFactory -> {
            System.out.println("   [CustomBFPP] Custom bean factory post processor");
        };
    }
}
