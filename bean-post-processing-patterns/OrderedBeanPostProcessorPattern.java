package com.example.beanpostprocessor;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Ordered Bean Post Processor Pattern
 * 
 * Demonstrates controlling the execution order of multiple
 * BeanPostProcessors using @Order, Ordered, and PriorityOrdered.
 * 
 * Key Concepts:
 * - @Order annotation
 * - Ordered interface
 * - PriorityOrdered interface
 * - Execution order control
 * - Priority-based processing
 * 
 * Use Cases:
 * - Sequential processing steps
 * - Dependency between processors
 * - Priority-based bean modification
 * - Layered initialization logic
 */
@SpringBootApplication
public class OrderedBeanPostProcessorPattern {

    public static void main(String[] args) {
        SpringApplication.run(OrderedBeanPostProcessorPattern.class, args);
    }
}

/**
 * Highest priority processor using PriorityOrdered
 * PriorityOrdered processors run before Ordered processors
 */
@Component
class HighestPriorityProcessor implements BeanPostProcessor, PriorityOrdered {

    private static final List<String> processedBeans = new ArrayList<>();

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof TestBean) {
            String log = "HighestPriorityProcessor (PriorityOrdered) - BEFORE: " + beanName;
            processedBeans.add(log);
            System.out.println(log);
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof TestBean) {
            String log = "HighestPriorityProcessor (PriorityOrdered) - AFTER: " + beanName;
            processedBeans.add(log);
            System.out.println(log);
        }
        return bean;
    }

    @Override
    public int getOrder() {
        return 1; // Lowest value = highest priority
    }

    public static List<String> getProcessedBeans() {
        return new ArrayList<>(processedBeans);
    }
}

/**
 * First processor using @Order annotation
 */
@Component
@Order(1)
class FirstProcessor implements BeanPostProcessor {

    private static final List<String> processedBeans = new ArrayList<>();

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof TestBean) {
            String log = "FirstProcessor (@Order(1)) - BEFORE: " + beanName;
            processedBeans.add(log);
            System.out.println(log);
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof TestBean) {
            String log = "FirstProcessor (@Order(1)) - AFTER: " + beanName;
            processedBeans.add(log);
            System.out.println(log);
        }
        return bean;
    }

    public static List<String> getProcessedBeans() {
        return new ArrayList<>(processedBeans);
    }
}

/**
 * Second processor using @Order annotation
 */
@Component
@Order(2)
class SecondProcessor implements BeanPostProcessor {

    private static final List<String> processedBeans = new ArrayList<>();

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof TestBean) {
            String log = "SecondProcessor (@Order(2)) - BEFORE: " + beanName;
            processedBeans.add(log);
            System.out.println(log);
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof TestBean) {
            String log = "SecondProcessor (@Order(2)) - AFTER: " + beanName;
            processedBeans.add(log);
            System.out.println(log);
        }
        return bean;
    }

    public static List<String> getProcessedBeans() {
        return new ArrayList<>(processedBeans);
    }
}

/**
 * Third processor using Ordered interface
 */
@Component
class ThirdProcessor implements BeanPostProcessor, Ordered {

    private static final List<String> processedBeans = new ArrayList<>();

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof TestBean) {
            String log = "ThirdProcessor (Ordered.getOrder()=3) - BEFORE: " + beanName;
            processedBeans.add(log);
            System.out.println(log);
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof TestBean) {
            String log = "ThirdProcessor (Ordered.getOrder()=3) - AFTER: " + beanName;
            processedBeans.add(log);
            System.out.println(log);
        }
        return bean;
    }

    @Override
    public int getOrder() {
        return 3;
    }

    public static List<String> getProcessedBeans() {
        return new ArrayList<>(processedBeans);
    }
}

/**
 * Unordered processor (runs last)
 */
@Component
class UnorderedProcessor implements BeanPostProcessor {

    private static final List<String> processedBeans = new ArrayList<>();

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof TestBean) {
            String log = "UnorderedProcessor (no ordering) - BEFORE: " + beanName;
            processedBeans.add(log);
            System.out.println(log);
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof TestBean) {
            String log = "UnorderedProcessor (no ordering) - AFTER: " + beanName;
            processedBeans.add(log);
            System.out.println(log);
        }
        return bean;
    }

    public static List<String> getProcessedBeans() {
        return new ArrayList<>(processedBeans);
    }
}

/**
 * Marker interface for filtering
 */
interface TestBean {
    String getName();
}

/**
 * Test bean to demonstrate ordered processing
 */
@Component
class SampleTestBean implements TestBean {
    
    private final String name = "SampleTestBean";
    
    @Override
    public String getName() {
        return name;
    }
}

/**
 * Controller to show processing order
 */
@RestController
class OrderedProcessorController {

    private final SampleTestBean testBean;

    public OrderedProcessorController(SampleTestBean testBean) {
        this.testBean = testBean;
    }

    @GetMapping("/ordered/all-logs")
    public Map<String, List<String>> getAllProcessingLogs() {
        return Map.of(
                "highestPriority", HighestPriorityProcessor.getProcessedBeans(),
                "first", FirstProcessor.getProcessedBeans(),
                "second", SecondProcessor.getProcessedBeans(),
                "third", ThirdProcessor.getProcessedBeans(),
                "unordered", UnorderedProcessor.getProcessedBeans()
        );
    }

    @GetMapping("/ordered/test-bean")
    public Map<String, String> getTestBean() {
        return Map.of("name", testBean.getName());
    }

    @GetMapping("/ordered/execution-order")
    public Map<String, String> getExecutionOrder() {
        return Map.of(
                "description", "Execution Order of BeanPostProcessors",
                "1", "HighestPriorityProcessor (PriorityOrdered, order=1)",
                "2", "FirstProcessor (@Order(1))",
                "3", "SecondProcessor (@Order(2))",
                "4", "ThirdProcessor (Ordered, order=3)",
                "5", "UnorderedProcessor (no ordering - runs last)"
        );
    }
}

/**
 * Documentation:
 * 
 * Ordering Mechanisms:
 * 
 * 1. PriorityOrdered Interface:
 *    - Highest priority
 *    - Always runs before Ordered
 *    - Implements getOrder() method
 *    - Used by Spring infrastructure beans
 * 
 * 2. Ordered Interface:
 *    - Medium priority
 *    - Runs after PriorityOrdered
 *    - Implements getOrder() method
 *    - Common for application processors
 * 
 * 3. @Order Annotation:
 *    - Convenient alternative to Ordered
 *    - Same priority as Ordered
 *    - Cleaner code
 * 
 * 4. No Ordering:
 *    - Lowest priority
 *    - Undefined order among unordered
 *    - Runs after all ordered processors
 * 
 * Execution Order:
 * 1. PriorityOrdered (sorted by getOrder())
 * 2. Ordered (sorted by getOrder())
 * 3. @Order (sorted by value)
 * 4. Unordered (undefined order)
 * 
 * Order Values:
 * - Lower values = Higher priority
 * - Ordered.HIGHEST_PRECEDENCE = Integer.MIN_VALUE
 * - Ordered.LOWEST_PRECEDENCE = Integer.MAX_VALUE
 * - Default (no ordering) = Ordered.LOWEST_PRECEDENCE + 1
 * 
 * Implementation Examples:
 * 
 * 1. Using PriorityOrdered:
 *    @Component
 *    class MyProcessor implements BeanPostProcessor, PriorityOrdered {
 *        @Override
 *        public int getOrder() {
 *            return 1;
 *        }
 *    }
 * 
 * 2. Using Ordered:
 *    @Component
 *    class MyProcessor implements BeanPostProcessor, Ordered {
 *        @Override
 *        public int getOrder() {
 *            return Ordered.HIGHEST_PRECEDENCE + 10;
 *        }
 *    }
 * 
 * 3. Using @Order:
 *    @Component
 *    @Order(100)
 *    class MyProcessor implements BeanPostProcessor { }
 * 
 * Common Order Values:
 * - Security: Ordered.HIGHEST_PRECEDENCE
 * - Validation: Ordered.HIGHEST_PRECEDENCE + 10
 * - Logging: Ordered.LOWEST_PRECEDENCE - 10
 * - Custom: Application-specific
 * 
 * Use Cases:
 * 
 * 1. Dependency Between Processors:
 *    - Processor A must run before B
 *    - Use lower order for A
 * 
 * 2. Layered Processing:
 *    - Security checks first
 *    - Validation next
 *    - Logging last
 * 
 * 3. Infrastructure vs Application:
 *    - Infrastructure: PriorityOrdered
 *    - Application: Ordered or @Order
 * 
 * 4. Performance Optimization:
 *    - Fast processors first
 *    - Expensive processors last
 * 
 * Best Practices:
 * - Use PriorityOrdered for critical infrastructure
 * - Use @Order for clarity
 * - Document why ordering matters
 * - Leave gaps between values (10, 20, 30)
 * - Use constants for order values
 * - Test different orderings
 * - Be explicit about dependencies
 * 
 * Debugging Order Issues:
 * - Enable debug logging
 * - Add logs in each processor
 * - Print order values on startup
 * - Check for conflicts
 * - Verify interface implementations
 * 
 * Common Pitfalls:
 * - Assuming unordered processors run in creation order
 * - Not documenting order dependencies
 * - Using same order values
 * - Forgetting PriorityOrdered runs first
 * - Circular dependencies between processors
 * 
 * Advanced Patterns:
 * 
 * 1. Conditional Ordering:
 *    @Override
 *    public int getOrder() {
 *        return environment.getProperty("processor.order", Integer.class, 100);
 *    }
 * 
 * 2. Order Constants:
 *    public class ProcessorOrder {
 *        public static final int SECURITY = 10;
 *        public static final int VALIDATION = 20;
 *        public static final int LOGGING = 30;
 *    }
 * 
 * 3. Dynamic Ordering:
 *    Adjust order based on runtime conditions
 * 
 * Testing:
 * - Verify execution order
 * - Test with different configurations
 * - Mock dependencies
 * - Check edge cases
 * 
 * Performance:
 * - Ordering has minimal overhead
 * - Sorting happens once at startup
 * - No runtime impact
 * 
 * Spring Boot Auto-Configuration:
 * - Uses PriorityOrdered extensively
 * - Application processors use Ordered
 * - Check auto-configuration order values
 * 
 * Combining with Other Features:
 * - @Conditional + @Order: Conditional ordering
 * - @Profile + @Order: Profile-specific ordering
 * - @DependsOn: Bean creation order
 */
