package com.example.conditional;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Conditional On Missing Bean Pattern
 * ===================================
 * 
 * Demonstrates @ConditionalOnMissingBean annotation that creates beans only
 * when specific beans do NOT exist in the ApplicationContext. This is the most
 * common conditional in Spring Boot for providing default implementations.
 * 
 * Key Concepts:
 * ------------
 * 1. @ConditionalOnMissingBean - Bean registration when bean absent
 * 2. Default Implementations - Provide fallback beans
 * 3. User Overrides - Allow custom implementations
 * 4. Auto-Configuration - Enable smart defaults
 * 5. Non-Invasive Configuration - Don't override user beans
 * 
 * How It Works:
 * ------------
 * - Checks if specified beans are ABSENT from ApplicationContext
 * - If ALL beans missing → condition matches → bean created
 * - If ANY bean present → condition fails → bean skipped
 * - Opposite of @ConditionalOnBean
 * - Most common pattern in Spring Boot auto-configuration
 * 
 * Common Use Cases:
 * ----------------
 * - Provide default DataSource if user doesn't define one
 * - Create default RestTemplate if absent
 * - Configure default cache manager
 * - Supply default message converter
 * - Enable default security configuration
 * - Provide fallback implementations
 * 
 * Syntax:
 * ------
 * @ConditionalOnMissingBean
 * @ConditionalOnMissingBean(BeanClass.class)
 * @ConditionalOnMissingBean(name = "beanName")
 * @ConditionalOnMissingBean(type = "com.example.BeanClass")
 * @ConditionalOnMissingBean(annotation = CustomAnnotation.class)
 * 
 * Important Notes:
 * ---------------
 * - Use for providing defaults
 * - User-defined beans take precedence
 * - Most common conditional in Boot auto-config
 * - Enables "convention over configuration"
 * - Allows easy customization
 * 
 * @author Spring Patterns
 * @version 1.0
 */

/**
 * Example 1: Default DataSource Configuration
 */
@Configuration
class DataSourceAutoConfiguration {
    
    /**
     * Create default DataSource only if user hasn't defined one
     */
    @Bean
    @ConditionalOnMissingBean
    public String dataSource() {
        System.out.println("Creating DEFAULT DataSource (no user DataSource found)");
        System.out.println("  Using H2 in-memory database");
        System.out.println("  User can override by defining their own DataSource bean");
        return "Default H2 DataSource";
    }
    
    /**
     * Create JDBC Template if DataSource exists but JdbcTemplate doesn't
     */
    @Bean
    @ConditionalOnMissingBean
    public String jdbcTemplate() {
        System.out.println("Creating DEFAULT JDBC Template");
        return "Default JDBC Template";
    }
}

/**
 * Example 2: Default RestTemplate Configuration
 */
@Configuration
class RestTemplateAutoConfiguration {
    
    @Bean
    @ConditionalOnMissingBean
    public String restTemplate() {
        System.out.println("Creating DEFAULT RestTemplate (no user RestTemplate found)");
        System.out.println("  Using default configuration");
        System.out.println("  User can provide custom RestTemplate with specific settings");
        return "Default RestTemplate";
    }
    
    @Bean
    @ConditionalOnMissingBean
    public String restTemplateBuilder() {
        System.out.println("Creating DEFAULT RestTemplate Builder");
        return "Default RestTemplate Builder";
    }
}

/**
 * Example 3: Default Cache Manager
 */
@Configuration
class CacheAutoConfiguration {
    
    @Bean
    @ConditionalOnMissingBean
    public String cacheManager() {
        System.out.println("Creating DEFAULT Cache Manager (no user cache found)");
        System.out.println("  Using simple in-memory cache");
        System.out.println("  User can define Redis/Hazelcast cache instead");
        return "Default Simple Cache Manager";
    }
}

/**
 * Example 4: Default Object Mapper (JSON)
 */
@Configuration
class JacksonAutoConfiguration {
    
    @Bean
    @ConditionalOnMissingBean
    public String objectMapper() {
        System.out.println("Creating DEFAULT ObjectMapper (no user ObjectMapper found)");
        System.out.println("  Using default Jackson configuration");
        System.out.println("  User can customize by providing their own ObjectMapper");
        return "Default ObjectMapper";
    }
}

/**
 * Example 5: Default Message Converter
 */
@Configuration
class MessageConverterAutoConfiguration {
    
    @Bean
    @ConditionalOnMissingBean
    public String messageConverter() {
        System.out.println("Creating DEFAULT Message Converter");
        System.out.println("  Using String message converter");
        return "Default String Message Converter";
    }
}

/**
 * Example 6: Default Task Executor
 */
@Configuration
class TaskExecutionAutoConfiguration {
    
    @Bean
    @ConditionalOnMissingBean
    public String taskExecutor() {
        System.out.println("Creating DEFAULT Task Executor");
        System.out.println("  Using SimpleAsyncTaskExecutor");
        System.out.println("  User can define ThreadPoolTaskExecutor for production");
        return "Default Simple Task Executor";
    }
}

/**
 * Example 7: Default Transaction Manager
 */
@Configuration
class TransactionAutoConfiguration {
    
    @Bean
    @ConditionalOnMissingBean
    public String transactionManager() {
        System.out.println("Creating DEFAULT Transaction Manager");
        System.out.println("  Using DataSourceTransactionManager");
        return "Default Transaction Manager";
    }
}

/**
 * Example 8: Default Validator
 */
@Configuration
class ValidationAutoConfiguration {
    
    @Bean
    @ConditionalOnMissingBean
    public String validator() {
        System.out.println("Creating DEFAULT Validator");
        System.out.println("  Using LocalValidatorFactoryBean");
        return "Default Validator";
    }
}

/**
 * Example 9: Default Template Engine
 */
@Configuration
class TemplateEngineAutoConfiguration {
    
    @Bean
    @ConditionalOnMissingBean
    public String templateEngine() {
        System.out.println("Creating DEFAULT Template Engine");
        System.out.println("  Using simple template resolver");
        return "Default Template Engine";
    }
}

/**
 * Example 10: Default Error Handler
 */
@Configuration
class ErrorHandlerAutoConfiguration {
    
    @Bean
    @ConditionalOnMissingBean
    public String errorController() {
        System.out.println("Creating DEFAULT Error Controller");
        System.out.println("  Using BasicErrorController");
        return "Default Error Controller";
    }
}

/**
 * Example 11: Custom Service with Default Implementation
 */
@Configuration
class CustomServiceAutoConfiguration {
    
    @Bean
    @ConditionalOnMissingBean
    public String userService() {
        System.out.println("Creating DEFAULT User Service");
        System.out.println("  Using in-memory user service");
        System.out.println("  Replace with database-backed implementation in production");
        return "Default In-Memory User Service";
    }
}

/**
 * Example 12: Default Metrics Configuration
 */
@Configuration
class MetricsAutoConfiguration {
    
    @Bean
    @ConditionalOnMissingBean
    public String meterRegistry() {
        System.out.println("Creating DEFAULT Meter Registry");
        System.out.println("  Using SimpleMeterRegistry");
        return "Default Simple Meter Registry";
    }
}

/**
 * Main Pattern Class - Demonstrating User Override
 */
@Configuration
public class ConditionalOnMissingBeanPattern {
    
    /**
     * Default implementation - will be created if user doesn't define one
     */
    @Bean
    @ConditionalOnMissingBean(name = "emailService")
    public String defaultEmailService() {
        System.out.println("Creating DEFAULT Email Service");
        System.out.println("  Using console-based email service (for development)");
        System.out.println("  Override by defining 'emailService' bean for SMTP integration");
        return "Default Console Email Service";
    }
    
    /**
     * Default implementation with type checking
     */
    @Bean
    @ConditionalOnMissingBean
    public String paymentService() {
        System.out.println("Creating DEFAULT Payment Service");
        System.out.println("  Using mock payment service");
        return "Default Mock Payment Service";
    }
    
    /**
     * Example showing how user would override:
     * 
     * @Bean
     * public String emailService() {
     *     return "Custom SMTP Email Service"; // This would prevent defaultEmailService
     * }
     */
}

/**
 * User Configuration Example (demonstrates override)
 */
@Configuration
class UserCustomConfiguration {
    
    /**
     * User-defined bean - this will prevent the default from being created
     * Uncomment to see the override in action
     */
    // @Bean
    // public String dataSource() {
    //     System.out.println("Creating CUSTOM DataSource (user-defined)");
    //     System.out.println("  Using PostgreSQL database");
    //     return "Custom PostgreSQL DataSource";
    // }
}

/**
 * Usage Examples and Best Practices
 */
class ConditionalOnMissingBeanUsageExamples {
    
    public static void main(String[] args) {
        System.out.println("Conditional On Missing Bean Pattern");
        System.out.println("====================================\n");
        
        System.out.println("Purpose:");
        System.out.println("- Provide default beans when user hasn't defined them");
        System.out.println("- Enable 'convention over configuration'");
        System.out.println("- Allow easy customization by users\n");
        
        System.out.println("How It Works:");
        System.out.println("1. Spring checks if bean exists in context");
        System.out.println("2. If bean is MISSING → create default bean");
        System.out.println("3. If bean EXISTS (user-defined) → skip default");
        System.out.println("4. User beans always take precedence\n");
        
        System.out.println("Syntax Options:");
        System.out.println("@ConditionalOnMissingBean - match by return type");
        System.out.println("@ConditionalOnMissingBean(Type.class) - match by type");
        System.out.println("@ConditionalOnMissingBean(name = \"beanName\") - match by name");
        System.out.println("@ConditionalOnMissingBean(type = \"pkg.Class\") - match by string\n");
        
        System.out.println("Common Use Cases:");
        System.out.println("1. Default DataSource (H2 in-memory)");
        System.out.println("2. Default RestTemplate");
        System.out.println("3. Default Cache Manager (simple)");
        System.out.println("4. Default ObjectMapper (JSON)");
        System.out.println("5. Default Task Executor");
        System.out.println("6. Default Transaction Manager");
        System.out.println("7. Default Security Configuration");
        System.out.println("8. Default Error Handlers\n");
        
        System.out.println("Best Practices:");
        System.out.println("- Use for all default/auto-configuration beans");
        System.out.println("- Provide sensible defaults for development");
        System.out.println("- Document how users can override");
        System.out.println("- Use in auto-configuration classes");
        System.out.println("- Combine with @ConditionalOnClass for smart defaults");
        System.out.println("- Name beans clearly to avoid conflicts\n");
        
        System.out.println("Example Override:");
        System.out.println("// Auto-configuration provides default");
        System.out.println("@Bean");
        System.out.println("@ConditionalOnMissingBean");
        System.out.println("public DataSource dataSource() { ... }\n");
        
        System.out.println("// User overrides by defining their own");
        System.out.println("@Bean");
        System.out.println("public DataSource dataSource() {");
        System.out.println("  return new PostgreSQLDataSource();");
        System.out.println("}");
    }
}
