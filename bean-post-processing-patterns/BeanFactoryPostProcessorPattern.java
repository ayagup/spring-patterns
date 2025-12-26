package com.example.beanpostprocessor;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Bean Factory Post Processor Pattern
 * 
 * Demonstrates using BeanFactoryPostProcessor to modify bean definitions
 * before beans are instantiated. This is a more fundamental customization
 * point than BeanPostProcessor.
 * 
 * Key Concepts:
 * - BeanFactoryPostProcessor interface
 * - BeanDefinitionRegistryPostProcessor
 * - Bean definition modification
 * - Dynamic bean registration
 * - Property placeholder resolution
 * 
 * Use Cases:
 * - Modify bean definitions
 * - Register beans programmatically
 * - Override bean properties
 * - Dynamic configuration
 * - Custom property resolution
 */
@SpringBootApplication
public class BeanFactoryPostProcessorPattern {

    public static void main(String[] args) {
        SpringApplication.run(BeanFactoryPostProcessorPattern.class, args);
    }
}

/**
 * Custom BeanFactoryPostProcessor for logging bean definitions
 */
@Component
class LoggingBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

    private static final List<String> beanDefinitions = new ArrayList<>();

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        System.out.println("=== BeanFactoryPostProcessor: Logging Bean Definitions ===");
        
        String[] beanNames = beanFactory.getBeanDefinitionNames();
        for (String beanName : beanNames) {
            String definition = beanName + " - " + beanFactory.getBeanDefinition(beanName).getBeanClassName();
            beanDefinitions.add(definition);
            System.out.println(definition);
        }
        
        System.out.println("Total beans: " + beanNames.length);
    }

    public static List<String> getBeanDefinitions() {
        return new ArrayList<>(beanDefinitions);
    }
}

/**
 * Custom BeanDefinitionRegistryPostProcessor for dynamic bean registration
 */
@Component
class DynamicBeanRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor {

    private static final List<String> registeredBeans = new ArrayList<>();

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        System.out.println("=== Registering dynamic beans ===");
        
        // Register dynamic service bean
        GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
        beanDefinition.setBeanClass(DynamicService.class);
        beanDefinition.setLazyInit(false);
        beanDefinition.setScope("singleton");
        
        registry.registerBeanDefinition("dynamicService", beanDefinition);
        registeredBeans.add("dynamicService");
        System.out.println("Registered dynamic bean: dynamicService");
        
        // Register another dynamic bean
        GenericBeanDefinition helperDefinition = new GenericBeanDefinition();
        helperDefinition.setBeanClass(DynamicHelper.class);
        
        registry.registerBeanDefinition("dynamicHelper", helperDefinition);
        registeredBeans.add("dynamicHelper");
        System.out.println("Registered dynamic bean: dynamicHelper");
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        System.out.println("=== Modifying bean definitions ===");
        
        // Can modify existing bean definitions here
        // Example: Change scope, add property values, etc.
    }

    public static List<String> getRegisteredBeans() {
        return new ArrayList<>(registeredBeans);
    }
}

/**
 * Dynamically registered service
 */
class DynamicService {
    
    private String serviceName = "DynamicService";
    
    public String getServiceName() {
        return serviceName;
    }
    
    public String performAction() {
        return "Action performed by " + serviceName;
    }
}

/**
 * Dynamically registered helper
 */
class DynamicHelper {
    
    private String helperName = "DynamicHelper";
    
    public String getHelperName() {
        return helperName;
    }
    
    public String help() {
        return "Help provided by " + helperName;
    }
}

/**
 * Controller to demonstrate BeanFactoryPostProcessor effects
 */
@RestController
class BeanFactoryPostProcessorController {

    private final DynamicService dynamicService;
    private final DynamicHelper dynamicHelper;

    public BeanFactoryPostProcessorController(DynamicService dynamicService,
                                             DynamicHelper dynamicHelper) {
        this.dynamicService = dynamicService;
        this.dynamicHelper = dynamicHelper;
    }

    @GetMapping("/bfpp/bean-definitions")
    public List<String> getBeanDefinitions() {
        return LoggingBeanFactoryPostProcessor.getBeanDefinitions();
    }

    @GetMapping("/bfpp/registered-beans")
    public List<String> getRegisteredBeans() {
        return DynamicBeanRegistryPostProcessor.getRegisteredBeans();
    }

    @GetMapping("/bfpp/dynamic-service")
    public Map<String, String> getDynamicService() {
        return Map.of(
                "serviceName", dynamicService.getServiceName(),
                "action", dynamicService.performAction()
        );
    }

    @GetMapping("/bfpp/dynamic-helper")
    public Map<String, String> getDynamicHelper() {
        return Map.of(
                "helperName", dynamicHelper.getHelperName(),
                "help", dynamicHelper.help()
        );
    }
}

/**
 * Documentation:
 * 
 * BeanFactoryPostProcessor vs BeanPostProcessor:
 * - BeanFactoryPostProcessor: Modifies bean DEFINITIONS before instantiation
 * - BeanPostProcessor: Modifies bean INSTANCES after instantiation
 * 
 * Execution Order:
 * 1. BeanFactoryPostProcessor (modify definitions)
 * 2. Bean instantiation
 * 3. BeanPostProcessor (modify instances)
 * 
 * BeanFactoryPostProcessor Interface:
 * void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory)
 * - Called after bean definitions loaded
 * - Before bean instantiation
 * - Can modify bean definitions
 * - Can register new beans
 * 
 * BeanDefinitionRegistryPostProcessor:
 * - Extends BeanFactoryPostProcessor
 * - Additional method: postProcessBeanDefinitionRegistry
 * - Called BEFORE regular BeanFactoryPostProcessors
 * - Can register new bean definitions
 * 
 * Methods:
 * 1. postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry)
 *    - Register new bean definitions
 *    - Remove bean definitions
 *    - First to execute
 * 
 * 2. postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory)
 *    - Modify bean definitions
 *    - Access all bean definitions
 *    - Second to execute
 * 
 * Registration:
 * 
 * @Component
 * class MyBeanFactoryPostProcessor implements BeanFactoryPostProcessor { }
 * 
 * @Configuration
 * class Config {
 *     @Bean
 *     public static BeanFactoryPostProcessor myProcessor() {
 *         return new MyBeanFactoryPostProcessor();
 *     }
 * }
 * 
 * Note: Must be static @Bean method
 * 
 * Common Use Cases:
 * 
 * 1. Property Placeholder Resolution:
 *    PropertySourcesPlaceholderConfigurer
 * 
 * 2. Dynamic Bean Registration:
 *    Register beans based on configuration
 * 
 * 3. Bean Definition Modification:
 *    Change scope, lazy-init, dependencies
 * 
 * 4. Custom Configuration:
 *    Process custom annotations on configuration
 * 
 * Bean Definition Modification Examples:
 * 
 * BeanDefinition bd = beanFactory.getBeanDefinition("myBean");
 * 
 * // Change scope
 * bd.setScope("prototype");
 * 
 * // Make lazy
 * bd.setLazyInit(true);
 * 
 * // Add dependency
 * bd.setDependsOn("otherBean");
 * 
 * // Set property value
 * bd.getPropertyValues().add("propertyName", "value");
 * 
 * Dynamic Bean Registration:
 * 
 * GenericBeanDefinition beanDef = new GenericBeanDefinition();
 * beanDef.setBeanClass(MyClass.class);
 * beanDef.setScope("singleton");
 * beanDef.setLazyInit(false);
 * beanDef.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
 * 
 * // Add constructor arguments
 * beanDef.getConstructorArgumentValues().addGenericArgumentValue("value");
 * 
 * // Add property values
 * beanDef.getPropertyValues().add("prop", "value");
 * 
 * registry.registerBeanDefinition("myBean", beanDef);
 * 
 * Built-in BeanFactoryPostProcessors:
 * - PropertySourcesPlaceholderConfigurer: Resolves ${...} placeholders
 * - PropertyOverrideConfigurer: Overrides bean properties
 * - CustomScopeConfigurer: Registers custom scopes
 * - ConfigurationClassPostProcessor: Processes @Configuration classes
 * 
 * Best Practices:
 * - Keep processing lightweight
 * - Don't instantiate beans in BeanFactoryPostProcessor
 * - Use BeanDefinitionRegistryPostProcessor for bean registration
 * - Be careful with ordering (use @Order or PriorityOrdered)
 * - Document modifications clearly
 * - Avoid circular dependencies
 * 
 * Ordering:
 * 
 * @Component
 * @Order(1)
 * class FirstProcessor implements BeanFactoryPostProcessor { }
 * 
 * Or implement PriorityOrdered or Ordered
 * 
 * Advanced Patterns:
 * 
 * 1. Conditional Bean Registration:
 *    if (someCondition) {
 *        registry.registerBeanDefinition("bean", definition);
 *    }
 * 
 * 2. Bean Definition Iteration:
 *    for (String name : beanFactory.getBeanDefinitionNames()) {
 *        BeanDefinition bd = beanFactory.getBeanDefinition(name);
 *        // Modify based on criteria
 *    }
 * 
 * 3. Custom Annotation Processing:
 *    if (beanClass.isAnnotationPresent(MyAnnotation.class)) {
 *        // Modify bean definition
 *    }
 * 
 * Limitations:
 * - Cannot instantiate beans
 * - Cannot use @Autowired in BeanFactoryPostProcessor
 * - Must not trigger bean instantiation
 * - Limited to definition-level modifications
 * 
 * Testing:
 * - Automatically invoked in Spring context
 * - Can be excluded in tests
 * - Can verify by checking bean definitions
 * 
 * Common Pitfalls:
 * - Instantiating beans too early
 * - Creating circular dependencies
 * - Not making @Bean method static
 * - Forgetting to handle null bean definitions
 * - Expensive operations slowing startup
 * 
 * Debugging:
 * - Enable debug: logging.level.org.springframework=DEBUG
 * - Add logging in postProcess methods
 * - Check order of execution
 * - Verify bean definitions after processing
 * 
 * Security Considerations:
 * - Validate bean class names before registration
 * - Sanitize property values
 * - Restrict to trusted configuration
 * - Audit bean definition changes
 */
