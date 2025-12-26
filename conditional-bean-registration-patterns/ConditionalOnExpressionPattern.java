package com.example.conditional;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Conditional On Expression Pattern
 * =================================
 * 
 * Demonstrates @ConditionalOnExpression annotation that creates beans based on
 * Spring Expression Language (SpEL) expressions. This provides the most flexible
 * conditional bean registration mechanism.
 * 
 * Key Concepts:
 * ------------
 * 1. @ConditionalOnExpression - Bean registration based on SpEL expression
 * 2. SpEL Evaluation - Use Spring Expression Language
 * 3. Complex Conditions - Combine multiple criteria
 * 4. Property Access - Access configuration properties in expressions
 * 5. Environment Variables - Use environment in conditions
 * 
 * How It Works:
 * ------------
 * - Evaluates SpEL expression at configuration time
 * - If expression evaluates to true → bean created
 * - If expression evaluates to false → bean skipped
 * - Can access properties, environment, system properties
 * - Supports complex logic with && || ! operators
 * 
 * SpEL Expression Features:
 * ------------------------
 * - Property access: ${property.name}
 * - Environment: environment['JAVA_HOME']
 * - System properties: systemProperties['os.name']
 * - Logical operators: && (and), || (or), ! (not)
 * - Comparison: ==, !=, <, >, <=, >=
 * - String operations: contains, startsWith, endsWith
 * - Null-safe navigation: ?.
 * 
 * Common Use Cases:
 * ----------------
 * - Complex feature toggles
 * - Multi-condition bean creation
 * - Environment-specific logic
 * - Version-based configuration
 * - Combined property checks
 * - Operating system specific beans
 * 
 * Syntax:
 * ------
 * @ConditionalOnExpression("${feature.enabled:false}")
 * @ConditionalOnExpression("'${env}' == 'production'")
 * @ConditionalOnExpression("${prop1} && ${prop2}")
 * @ConditionalOnExpression("T(java.lang.System).getProperty('os.name') matches '.*Windows.*'")
 * 
 * @author Spring Patterns
 * @version 1.0
 */

/**
 * Example 1: Simple Property Expression
 */
@Configuration
class SimpleExpressionConfiguration {
    
    /**
     * Create bean if cache.enabled is true
     */
    @Bean
    @ConditionalOnExpression("${cache.enabled:false}")
    public String cacheService() {
        System.out.println("Creating Cache Service (cache.enabled=true)");
        return "Cache Service";
    }
}

/**
 * Example 2: Multiple Property Conditions
 */
@Configuration
class MultiplePropertyConfiguration {
    
    /**
     * Create bean only if both security AND ssl are enabled
     */
    @Bean
    @ConditionalOnExpression("${security.enabled:false} && ${security.ssl.enabled:false}")
    public String sslSecurityFilter() {
        System.out.println("Creating SSL Security Filter");
        System.out.println("  (security.enabled=true AND security.ssl.enabled=true)");
        return "SSL Security Filter";
    }
    
    /**
     * Create bean if either kafka OR rabbitmq is enabled
     */
    @Bean
    @ConditionalOnExpression("${messaging.kafka.enabled:false} || ${messaging.rabbitmq.enabled:false}")
    public String messagingService() {
        System.out.println("Creating Messaging Service");
        System.out.println("  (Kafka OR RabbitMQ enabled)");
        return "Messaging Service";
    }
}

/**
 * Example 3: Environment-Based Conditions
 */
@Configuration
class EnvironmentBasedConfiguration {
    
    /**
     * Create bean only in production environment
     */
    @Bean
    @ConditionalOnExpression("'${spring.profiles.active:default}' == 'production'")
    public String productionMonitoring() {
        System.out.println("Creating Production Monitoring");
        System.out.println("  (profile=production)");
        return "Production Monitoring";
    }
    
    /**
     * Create bean in dev or test environments
     */
    @Bean
    @ConditionalOnExpression(
        "'${spring.profiles.active:dev}'.equals('dev') || " +
        "'${spring.profiles.active:dev}'.equals('test')"
    )
    public String debugTooling() {
        System.out.println("Creating Debug Tooling");
        System.out.println("  (profile=dev OR test)");
        return "Debug Tooling";
    }
}

/**
 * Example 4: Numeric Comparisons
 */
@Configuration
class NumericComparisonConfiguration {
    
    /**
     * Create bean only if pool size is greater than 10
     */
    @Bean
    @ConditionalOnExpression("${thread.pool.size:5} > 10")
    public String largePoolMonitor() {
        System.out.println("Creating Large Pool Monitor");
        System.out.println("  (thread.pool.size > 10)");
        return "Large Pool Monitor";
    }
    
    /**
     * Create bean if max connections within range
     */
    @Bean
    @ConditionalOnExpression(
        "${db.max.connections:10} >= 50 && ${db.max.connections:10} <= 200"
    )
    public String connectionPoolOptimizer() {
        System.out.println("Creating Connection Pool Optimizer");
        System.out.println("  (50 <= max.connections <= 200)");
        return "Connection Pool Optimizer";
    }
}

/**
 * Example 5: String Operations
 */
@Configuration
class StringOperationConfiguration {
    
    /**
     * Create bean if database URL contains 'postgresql'
     */
    @Bean
    @ConditionalOnExpression(
        "'${spring.datasource.url:}'.contains('postgresql')"
    )
    public String postgresqlOptimizations() {
        System.out.println("Creating PostgreSQL Optimizations");
        return "PostgreSQL Optimizations";
    }
    
    /**
     * Create bean if app name starts with 'microservice-'
     */
    @Bean
    @ConditionalOnExpression(
        "'${spring.application.name:}'.startsWith('microservice-')"
    )
    public String microserviceConfiguration() {
        System.out.println("Creating Microservice Configuration");
        return "Microservice Configuration";
    }
}

/**
 * Example 6: System Property Access
 */
@Configuration
class SystemPropertyConfiguration {
    
    /**
     * Create bean only on Windows OS
     */
    @Bean
    @ConditionalOnExpression(
        "T(java.lang.System).getProperty('os.name').toLowerCase().contains('windows')"
    )
    public String windowsSpecificService() {
        System.out.println("Creating Windows-Specific Service");
        return "Windows-Specific Service";
    }
    
    /**
     * Create bean if running on Java 11+
     */
    @Bean
    @ConditionalOnExpression(
        "T(java.lang.System).getProperty('java.version').startsWith('11') || " +
        "T(java.lang.System).getProperty('java.version').startsWith('17')"
    )
    public String modernJavaFeatures() {
        System.out.println("Creating Modern Java Features");
        System.out.println("  (Java 11 or 17 detected)");
        return "Modern Java Features";
    }
}

/**
 * Example 7: Negation Conditions
 */
@Configuration
class NegationConfiguration {
    
    /**
     * Create bean only if cache is NOT enabled
     */
    @Bean
    @ConditionalOnExpression("!${cache.enabled:true}")
    public String noCacheStrategy() {
        System.out.println("Creating No-Cache Strategy");
        System.out.println("  (cache.enabled=false)");
        return "No-Cache Strategy";
    }
}

/**
 * Example 8: Null-Safe Expressions
 */
@Configuration
class NullSafeConfiguration {
    
    /**
     * Create bean with null-safe property access
     */
    @Bean
    @ConditionalOnExpression(
        "'${optional.property:}'.length() > 0"
    )
    public String optionalFeature() {
        System.out.println("Creating Optional Feature");
        System.out.println("  (optional.property is set)");
        return "Optional Feature";
    }
}

/**
 * Example 9: Complex Business Logic
 */
@Configuration
class ComplexLogicConfiguration {
    
    /**
     * Create bean with complex multi-condition logic
     */
    @Bean
    @ConditionalOnExpression(
        "(${features.premium:false} && ${user.subscription.active:false}) || " +
        "${features.trial.enabled:true}"
    )
    public String premiumOrTrialFeatures() {
        System.out.println("Creating Premium/Trial Features");
        System.out.println("  (premium subscription OR trial enabled)");
        return "Premium/Trial Features";
    }
    
    /**
     * Create bean based on deployment environment and debug mode
     */
    @Bean
    @ConditionalOnExpression(
        "('${env}' == 'production' && ${monitoring.enabled:true}) || " +
        "('${env}' == 'staging' && ${debug.mode:false})"
    )
    public String environmentMonitoring() {
        System.out.println("Creating Environment Monitoring");
        return "Environment Monitoring";
    }
}

/**
 * Example 10: Feature Flag Combinations
 */
@Configuration
class FeatureFlagConfiguration {
    
    /**
     * Enable A/B test variant based on multiple flags
     */
    @Bean
    @ConditionalOnExpression(
        "${ab.test.enabled:false} && " +
        "'${ab.test.variant:}'.equals('B') && " +
        "${ab.test.percentage:0} >= 50"
    )
    public String abTestVariantB() {
        System.out.println("Creating A/B Test Variant B");
        System.out.println("  (test enabled, variant=B, percentage>=50)");
        return "A/B Test Variant B";
    }
}

/**
 * Main Pattern Class
 */
@Configuration
public class ConditionalOnExpressionPattern {
    
    /**
     * Example: Simple boolean expression
     */
    @Bean
    @ConditionalOnExpression("${features.advanced.search:false}")
    public String advancedSearch() {
        System.out.println("Creating Advanced Search");
        return "Advanced Search Service";
    }
    
    /**
     * Example: Complex multi-criteria expression
     */
    @Bean
    @ConditionalOnExpression(
        "${cloud.enabled:false} && " +
        "('${cloud.provider:}'.equals('aws') || '${cloud.provider:}'.equals('azure')) && " +
        "${cloud.region:''}.length() > 0"
    )
    public String cloudIntegration() {
        System.out.println("Creating Cloud Integration");
        System.out.println("  (cloud enabled, provider=AWS/Azure, region specified)");
        return "Cloud Integration Service";
    }
}

/**
 * Usage Examples and Best Practices
 */
class ConditionalOnExpressionUsageExamples {
    
    public static void main(String[] args) {
        System.out.println("Conditional On Expression Pattern");
        System.out.println("==================================\n");
        
        System.out.println("Purpose:");
        System.out.println("- Create beans based on complex SpEL expressions");
        System.out.println("- Combine multiple conditions with logic operators");
        System.out.println("- Access properties, environment, system properties\n");
        
        System.out.println("SpEL Expression Features:");
        System.out.println("1. Property access: ${property.name:default}");
        System.out.println("2. Logical AND: ${prop1} && ${prop2}");
        System.out.println("3. Logical OR: ${prop1} || ${prop2}");
        System.out.println("4. Negation: !${property}");
        System.out.println("5. Comparison: ==, !=, <, >, <=, >=");
        System.out.println("6. String methods: .contains(), .startsWith(), .equals()");
        System.out.println("7. System props: T(System).getProperty('os.name')");
        System.out.println("8. Length: '${prop}'.length() > 0\n");
        
        System.out.println("Common Examples:");
        System.out.println("// Simple boolean");
        System.out.println("@ConditionalOnExpression(\"${feature.enabled:false}\")\n");
        
        System.out.println("// Multiple conditions (AND)");
        System.out.println("@ConditionalOnExpression(");
        System.out.println("  \"${security.enabled} && ${ssl.enabled}\"");
        System.out.println(")\n");
        
        System.out.println("// Multiple conditions (OR)");
        System.out.println("@ConditionalOnExpression(");
        System.out.println("  \"${kafka.enabled} || ${rabbitmq.enabled}\"");
        System.out.println(")\n");
        
        System.out.println("// Environment check");
        System.out.println("@ConditionalOnExpression(");
        System.out.println("  \"'${spring.profiles.active}' == 'production'\"");
        System.out.println(")\n");
        
        System.out.println("// Numeric comparison");
        System.out.println("@ConditionalOnExpression(");
        System.out.println("  \"${thread.pool.size:10} > 50\"");
        System.out.println(")\n");
        
        System.out.println("// String operations");
        System.out.println("@ConditionalOnExpression(");
        System.out.println("  \"'${db.url}'.contains('postgresql')\"");
        System.out.println(")\n");
        
        System.out.println("Best Practices:");
        System.out.println("- Provide default values: ${prop:default}");
        System.out.println("- Use quotes for string literals: '${prop}'");
        System.out.println("- Keep expressions readable");
        System.out.println("- Document complex conditions");
        System.out.println("- Test with various property values");
        System.out.println("- Prefer @ConditionalOnProperty for simple cases");
        System.out.println("- Use for complex multi-criteria conditions\n");
        
        System.out.println("When to Use:");
        System.out.println("- Complex boolean logic required");
        System.out.println("- Multiple properties need to be checked");
        System.out.println("- Numeric comparisons needed");
        System.out.println("- String operations required");
        System.out.println("- System property access needed");
        System.out.println("- Simple annotations (@ConditionalOnProperty) insufficient");
    }
}
