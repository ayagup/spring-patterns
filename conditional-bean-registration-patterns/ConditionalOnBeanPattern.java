package com.example.conditional;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Conditional On Bean Pattern
 * ===========================
 * 
 * Demonstrates @ConditionalOnBean annotation that creates beans only when
 * specific beans exist in the ApplicationContext. This enables beans to
 * depend on the presence of other beans.
 * 
 * Key Concepts:
 * ------------
 * 1. @ConditionalOnBean - Bean registration based on bean presence
 * 2. Bean Dependency - Create bean only if dependency exists
 * 3. Configuration Ordering - Bean creation sequence matters
 * 4. Context Inspection - Check ApplicationContext for beans
 * 5. Type or Name Matching - Match by type, name, or annotation
 * 
 * How It Works:
 * ------------
 * - Checks if specified beans exist in ApplicationContext
 * - If ALL specified beans present → condition matches → bean created
 * - If ANY bean missing → condition fails → bean skipped
 * - Can match by type, name, or annotation
 * - Searches entire context (including parent contexts)
 * 
 * Common Use Cases:
 * ----------------
 * - Create dependent beans
 * - Configure integrations when component available
 * - Build decorator/wrapper beans
 * - Add features when base infrastructure present
 * - Create beans conditionally based on configuration
 * 
 * Syntax:
 * ------
 * @ConditionalOnBean(BeanClass.class)
 * @ConditionalOnBean(name = "beanName")
 * @ConditionalOnBean(type = "com.example.BeanClass")
 * @ConditionalOnBean(annotation = CustomAnnotation.class)
 * @ConditionalOnBean(value = {Bean1.class, Bean2.class})
 * 
 * Important Notes:
 * ---------------
 * - Bean order matters (use @DependsOn if needed)
 * - Cannot detect beans in same @Configuration class
 * - Checks at configuration processing time
 * - Use @ConditionalOnMissingBean for defaults
 * - Can search by type, name, or annotation
 * 
 * @author Spring Patterns
 * @version 1.0
 */

/**
 * Example 1: Cache Manager Decorator (when CacheManager exists)
 */
@Configuration
class CacheConfiguration {
    
    @Bean
    public String cacheManager() {
        System.out.println("Creating primary Cache Manager");
        return "Primary Cache Manager";
    }
    
    /**
     * Only create if cacheManager bean exists
     */
    @Bean
    @ConditionalOnBean(name = "cacheManager")
    public String cacheStatistics() {
        System.out.println("Creating Cache Statistics (cacheManager found)");
        System.out.println("  Wrapping cacheManager with statistics");
        return "Cache Statistics";
    }
    
    @Bean
    @ConditionalOnBean(name = "cacheManager")
    public String cacheMonitor() {
        System.out.println("Creating Cache Monitor");
        return "Cache Monitor";
    }
}

/**
 * Example 2: Transaction Manager Configurators
 */
@Configuration
class TransactionConfiguration {
    
    @Bean
    public String transactionManager() {
        System.out.println("Creating Transaction Manager");
        return "Transaction Manager";
    }
    
    @Bean
    @ConditionalOnBean(name = "transactionManager")
    public String transactionInterceptor() {
        System.out.println("Creating Transaction Interceptor (transactionManager found)");
        return "Transaction Interceptor";
    }
    
    @Bean
    @ConditionalOnBean(name = "transactionManager")
    public String transactionTemplate() {
        System.out.println("Creating Transaction Template");
        return "Transaction Template";
    }
}

/**
 * Example 3: DataSource Dependent Beans
 */
@Configuration
class DataSourceDependentConfiguration {
    
    // Simulating DataSource bean (in real app, this would be auto-configured)
    @Bean
    public String dataSource() {
        System.out.println("Creating DataSource");
        return "DataSource";
    }
    
    @Bean
    @ConditionalOnBean(name = "dataSource")
    public String jdbcTemplate() {
        System.out.println("Creating JDBC Template (dataSource found)");
        return "JDBC Template";
    }
    
    @Bean
    @ConditionalOnBean(name = "dataSource")
    public String dataSourceHealthIndicator() {
        System.out.println("Creating DataSource Health Indicator");
        return "DataSource Health Indicator";
    }
    
    @Bean
    @ConditionalOnBean(name = "dataSource")
    public String flyway() {
        System.out.println("Creating Flyway Migration (dataSource found)");
        return "Flyway Migration";
    }
}

/**
 * Example 4: Security Configurators
 */
@Configuration
class SecurityDependentConfiguration {
    
    @Bean
    public String securityFilterChain() {
        System.out.println("Creating Security Filter Chain");
        return "Security Filter Chain";
    }
    
    @Bean
    @ConditionalOnBean(name = "securityFilterChain")
    public String authenticationAuditor() {
        System.out.println("Creating Authentication Auditor (security found)");
        return "Authentication Auditor";
    }
    
    @Bean
    @ConditionalOnBean(name = "securityFilterChain")
    public String securityEventListener() {
        System.out.println("Creating Security Event Listener");
        return "Security Event Listener";
    }
}

/**
 * Example 5: Messaging Infrastructure
 */
@Configuration
class MessagingConfiguration {
    
    @Bean
    public String messageTemplate() {
        System.out.println("Creating Message Template");
        return "Message Template";
    }
    
    @Bean
    @ConditionalOnBean(name = "messageTemplate")
    public String messageConverter() {
        System.out.println("Creating Message Converter (messageTemplate found)");
        return "Message Converter";
    }
    
    @Bean
    @ConditionalOnBean(name = "messageTemplate")
    public String messageListenerContainer() {
        System.out.println("Creating Message Listener Container");
        return "Message Listener Container";
    }
}

/**
 * Example 6: REST Client Configuration
 */
@Configuration
class RestClientConfiguration {
    
    @Bean
    public String restTemplate() {
        System.out.println("Creating Rest Template");
        return "Rest Template";
    }
    
    @Bean
    @ConditionalOnBean(name = "restTemplate")
    public String restTemplateInterceptor() {
        System.out.println("Creating Rest Template Interceptor (restTemplate found)");
        System.out.println("  Adding logging and authentication interceptors");
        return "Rest Template Interceptor";
    }
    
    @Bean
    @ConditionalOnBean(name = "restTemplate")
    public String restTemplateCustomizer() {
        System.out.println("Creating Rest Template Customizer");
        return "Rest Template Customizer";
    }
}

/**
 * Example 7: Metrics Integration
 */
@Configuration
class MetricsConfiguration {
    
    @Bean
    public String meterRegistry() {
        System.out.println("Creating Meter Registry");
        return "Meter Registry";
    }
    
    @Bean
    @ConditionalOnBean(name = "meterRegistry")
    public String metricsExporter() {
        System.out.println("Creating Metrics Exporter (meterRegistry found)");
        return "Metrics Exporter";
    }
    
    @Bean
    @ConditionalOnBean(name = "meterRegistry")
    public String customMetrics() {
        System.out.println("Creating Custom Metrics");
        return "Custom Metrics";
    }
}

/**
 * Example 8: Task Execution Infrastructure
 */
@Configuration
class TaskExecutionConfiguration {
    
    @Bean
    public String taskExecutor() {
        System.out.println("Creating Task Executor");
        return "Task Executor";
    }
    
    @Bean
    @ConditionalOnBean(name = "taskExecutor")
    public String asyncConfigurer() {
        System.out.println("Creating Async Configurer (taskExecutor found)");
        return "Async Configurer";
    }
    
    @Bean
    @ConditionalOnBean(name = "taskExecutor")
    public String taskExecutorMonitor() {
        System.out.println("Creating Task Executor Monitor");
        return "Task Executor Monitor";
    }
}

/**
 * Example 9: Entity Manager Integration
 */
@Configuration
class EntityManagerConfiguration {
    
    @Bean
    public String entityManagerFactory() {
        System.out.println("Creating Entity Manager Factory");
        return "Entity Manager Factory";
    }
    
    @Bean
    @ConditionalOnBean(name = "entityManagerFactory")
    public String jpaTransactionManager() {
        System.out.println("Creating JPA Transaction Manager (EMF found)");
        return "JPA Transaction Manager";
    }
    
    @Bean
    @ConditionalOnBean(name = "entityManagerFactory")
    public String jpaVendorAdapter() {
        System.out.println("Creating JPA Vendor Adapter");
        return "JPA Vendor Adapter";
    }
}

/**
 * Example 10: Web Server Customization
 */
@Configuration
class WebServerConfiguration {
    
    @Bean
    public String servletWebServerFactory() {
        System.out.println("Creating Servlet Web Server Factory");
        return "Servlet Web Server Factory";
    }
    
    @Bean
    @ConditionalOnBean(name = "servletWebServerFactory")
    public String webServerCustomizer() {
        System.out.println("Creating Web Server Customizer (factory found)");
        return "Web Server Customizer";
    }
    
    @Bean
    @ConditionalOnBean(name = "servletWebServerFactory")
    public String errorPageCustomizer() {
        System.out.println("Creating Error Page Customizer");
        return "Error Page Customizer";
    }
}

/**
 * Main Pattern Class
 */
@Configuration
public class ConditionalOnBeanPattern {
    
    @Bean
    public String primaryService() {
        System.out.println("Creating Primary Service");
        return "Primary Service";
    }
    
    /**
     * Example: Create decorator only if primary service exists
     */
    @Bean
    @ConditionalOnBean(name = "primaryService")
    public String serviceDecorator() {
        System.out.println("Creating Service Decorator (primaryService found)");
        System.out.println("  Adding logging and caching to primaryService");
        return "Service Decorator";
    }
    
    /**
     * Example: Create monitoring only if service exists
     */
    @Bean
    @ConditionalOnBean(name = "primaryService")
    public String serviceMonitor() {
        System.out.println("Creating Service Monitor (primaryService found)");
        return "Service Monitor";
    }
}

/**
 * Usage Examples and Best Practices
 */
class ConditionalOnBeanUsageExamples {
    
    public static void main(String[] args) {
        System.out.println("Conditional On Bean Pattern");
        System.out.println("============================\n");
        
        System.out.println("Purpose:");
        System.out.println("- Create beans only when specific beans exist");
        System.out.println("- Build dependent infrastructure");
        System.out.println("- Add decorators/wrappers conditionally\n");
        
        System.out.println("Syntax Options:");
        System.out.println("1. By type: @ConditionalOnBean(DataSource.class)");
        System.out.println("2. By name: @ConditionalOnBean(name = \"dataSource\")");
        System.out.println("3. By string type: @ConditionalOnBean(type = \"javax.sql.DataSource\")");
        System.out.println("4. By annotation: @ConditionalOnBean(annotation = Service.class)");
        System.out.println("5. Multiple: @ConditionalOnBean({Bean1.class, Bean2.class})\n");
        
        System.out.println("Common Use Cases:");
        System.out.println("1. Create JDBC Template when DataSource exists");
        System.out.println("2. Add cache statistics when CacheManager present");
        System.out.println("3. Configure transaction when TransactionManager exists");
        System.out.println("4. Add metrics when MeterRegistry available");
        System.out.println("5. Create health indicators when components present");
        System.out.println("6. Add interceptors when base infrastructure ready");
        System.out.println("7. Build decorators around existing beans\n");
        
        System.out.println("Important Considerations:");
        System.out.println("- Bean creation order matters");
        System.out.println("- Cannot detect beans in same @Configuration");
        System.out.println("- Use @DependsOn to control order if needed");
        System.out.println("- Evaluated during configuration processing");
        System.out.println("- Searches entire context (including parents)\n");
        
        System.out.println("Best Practices:");
        System.out.println("- Use for dependent infrastructure");
        System.out.println("- Combine with @Primary for defaults");
        System.out.println("- Document bean dependencies");
        System.out.println("- Avoid circular dependencies");
        System.out.println("- Use specific bean names for clarity");
        System.out.println("- Consider using @ConditionalOnMissingBean for fallbacks");
    }
}
