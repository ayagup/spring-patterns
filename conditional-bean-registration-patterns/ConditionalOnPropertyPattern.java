package com.example.conditional;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Conditional On Property Pattern
 * ===============================
 * 
 * Demonstrates @ConditionalOnProperty annotation that creates beans based on
 * the presence and value of configuration properties. This enables feature
 * toggles and environment-specific configuration.
 * 
 * Key Concepts:
 * ------------
 * 1. @ConditionalOnProperty - Bean registration based on properties
 * 2. Feature Toggles - Enable/disable features via configuration
 * 3. Environment Configuration - Different beans per environment
 * 4. Property Matching - Match by value, presence, or absence
 * 5. Flexible Configuration - Runtime behavior control
 * 
 * How It Works:
 * ------------
 * - Checks application properties (application.yml/properties)
 * - Can check for property presence, absence, or specific values
 * - Supports prefix for grouped properties
 * - Can match single or multiple properties
 * - Defaults can be specified with havingValue
 * 
 * Common Use Cases:
 * ----------------
 * - Feature flags/toggles
 * - Environment-specific beans
 * - Enable/disable modules
 * - Conditional integrations
 * - A/B testing configurations
 * - Debug mode enablement
 * 
 * Syntax:
 * ------
 * @ConditionalOnProperty(name = "feature.enabled")
 * @ConditionalOnProperty(name = "feature.enabled", havingValue = "true")
 * @ConditionalOnProperty(prefix = "app", name = "feature")
 * @ConditionalOnProperty(name = "feature", matchIfMissing = true)
 * @ConditionalOnProperty(name = {"prop1", "prop2"})
 * 
 * Parameters:
 * ----------
 * - name: Property name(s) to check
 * - prefix: Common prefix for properties
 * - havingValue: Expected value (default: not "false")
 * - matchIfMissing: Match if property absent (default: false)
 * - relaxedNames: Use relaxed binding (default: true)
 * 
 * @author Spring Patterns
 * @version 1.0
 */

/**
 * Example 1: Feature Toggle
 */
@Configuration
class FeatureToggleConfiguration {
    
    /**
     * Create bean only if cache.enabled=true in properties
     * 
     * application.properties:
     * cache.enabled=true
     */
    @Bean
    @ConditionalOnProperty(name = "cache.enabled", havingValue = "true")
    public String cacheManager() {
        System.out.println("Creating Cache Manager (cache.enabled=true)");
        return "Cache Manager";
    }
    
    /**
     * Create bean only if email.notifications.enabled=true
     * 
     * application.yml:
     * email:
     *   notifications:
     *     enabled: true
     */
    @Bean
    @ConditionalOnProperty(name = "email.notifications.enabled", havingValue = "true")
    public String emailNotificationService() {
        System.out.println("Creating Email Notification Service (enabled=true)");
        return "Email Notification Service";
    }
}

/**
 * Example 2: Debug Mode Configuration
 */
@Configuration
class DebugModeConfiguration {
    
    /**
     * Create debug beans only when debug.mode=true
     */
    @Bean
    @ConditionalOnProperty(name = "debug.mode", havingValue = "true")
    public String debugLogger() {
        System.out.println("Creating Debug Logger (debug.mode=true)");
        System.out.println("  Verbose logging enabled");
        return "Debug Logger";
    }
    
    @Bean
    @ConditionalOnProperty(name = "debug.mode", havingValue = "true")
    public String performanceMonitor() {
        System.out.println("Creating Performance Monitor (debug mode)");
        return "Performance Monitor";
    }
}

/**
 * Example 3: Environment-Specific Configuration
 */
@Configuration
class EnvironmentConfiguration {
    
    /**
     * Production database when env=production
     */
    @Bean
    @ConditionalOnProperty(name = "app.env", havingValue = "production")
    public String productionDataSource() {
        System.out.println("Creating Production DataSource (env=production)");
        return "PostgreSQL DataSource";
    }
    
    /**
     * Development database when env=development
     */
    @Bean
    @ConditionalOnProperty(name = "app.env", havingValue = "development")
    public String developmentDataSource() {
        System.out.println("Creating Development DataSource (env=development)");
        return "H2 DataSource";
    }
}

/**
 * Example 4: Integration Toggles
 */
@Configuration
class IntegrationConfiguration {
    
    /**
     * Enable Kafka integration when enabled
     */
    @Bean
    @ConditionalOnProperty(prefix = "integrations.kafka", name = "enabled", havingValue = "true")
    public String kafkaIntegration() {
        System.out.println("Creating Kafka Integration (integrations.kafka.enabled=true)");
        return "Kafka Integration";
    }
    
    /**
     * Enable RabbitMQ integration when enabled
     */
    @Bean
    @ConditionalOnProperty(prefix = "integrations.rabbitmq", name = "enabled", havingValue = "true")
    public String rabbitmqIntegration() {
        System.out.println("Creating RabbitMQ Integration (enabled=true)");
        return "RabbitMQ Integration";
    }
}

/**
 * Example 5: Property Presence Check
 */
@Configuration
class PropertyPresenceConfiguration {
    
    /**
     * Create bean if property exists (any value)
     */
    @Bean
    @ConditionalOnProperty(name = "custom.api.key")
    public String apiClient() {
        System.out.println("Creating API Client (custom.api.key is set)");
        return "API Client";
    }
}

/**
 * Example 6: Match If Missing (Default Behavior)
 */
@Configuration
class DefaultBehaviorConfiguration {
    
    /**
     * Create bean if property missing or equals "true"
     * matchIfMissing=true means create if property not defined
     */
    @Bean
    @ConditionalOnProperty(
        name = "features.auto-backup",
        havingValue = "true",
        matchIfMissing = true
    )
    public String autoBackupService() {
        System.out.println("Creating Auto Backup Service");
        System.out.println("  (enabled by default if not configured)");
        return "Auto Backup Service";
    }
}

/**
 * Example 7: Multiple Properties
 */
@Configuration
class MultiPropertyConfiguration {
    
    /**
     * Create bean only if ALL properties match
     */
    @Bean
    @ConditionalOnProperty(
        name = {"security.enabled", "security.ssl.enabled"},
        havingValue = "true"
    )
    public String sslSecurityFilter() {
        System.out.println("Creating SSL Security Filter");
        System.out.println("  (both security.enabled and security.ssl.enabled = true)");
        return "SSL Security Filter";
    }
}

/**
 * Example 8: Prefix-Based Configuration
 */
@Configuration
class PrefixConfiguration {
    
    /**
     * Using prefix for cleaner property names
     * Checks: app.features.payment = "stripe"
     */
    @Bean
    @ConditionalOnProperty(
        prefix = "app.features",
        name = "payment",
        havingValue = "stripe"
    )
    public String stripePaymentProvider() {
        System.out.println("Creating Stripe Payment Provider");
        System.out.println("  (app.features.payment=stripe)");
        return "Stripe Payment Provider";
    }
    
    @Bean
    @ConditionalOnProperty(
        prefix = "app.features",
        name = "payment",
        havingValue = "paypal"
    )
    public String paypalPaymentProvider() {
        System.out.println("Creating PayPal Payment Provider");
        System.out.println("  (app.features.payment=paypal)");
        return "PayPal Payment Provider";
    }
}

/**
 * Example 9: Monitoring Configuration
 */
@Configuration
class MonitoringConfiguration {
    
    @Bean
    @ConditionalOnProperty(name = "monitoring.metrics.enabled", havingValue = "true")
    public String metricsCollector() {
        System.out.println("Creating Metrics Collector (metrics enabled)");
        return "Metrics Collector";
    }
    
    @Bean
    @ConditionalOnProperty(name = "monitoring.tracing.enabled", havingValue = "true")
    public String distributedTracing() {
        System.out.println("Creating Distributed Tracing (tracing enabled)");
        return "Distributed Tracing";
    }
}

/**
 * Example 10: Scheduling Configuration
 */
@Configuration
class SchedulingConfiguration {
    
    @Bean
    @ConditionalOnProperty(
        name = "scheduling.enabled",
        havingValue = "true",
        matchIfMissing = false
    )
    public String scheduledTasks() {
        System.out.println("Creating Scheduled Tasks (scheduling.enabled=true)");
        return "Scheduled Tasks";
    }
}

/**
 * Main Pattern Class
 */
@Configuration
public class ConditionalOnPropertyPattern {
    
    /**
     * Example: Simple feature toggle
     * 
     * application.properties:
     * features.advanced-search=true
     */
    @Bean
    @ConditionalOnProperty(
        name = "features.advanced-search",
        havingValue = "true"
    )
    public String advancedSearchService() {
        System.out.println("Creating Advanced Search Service");
        System.out.println("  (features.advanced-search=true)");
        return "Advanced Search Service";
    }
    
    /**
     * Example: Enabled by default
     */
    @Bean
    @ConditionalOnProperty(
        name = "features.basic-auth",
        havingValue = "true",
        matchIfMissing = true  // Enabled if property not specified
    )
    public String basicAuthFilter() {
        System.out.println("Creating Basic Auth Filter");
        System.out.println("  (enabled by default)");
        return "Basic Auth Filter";
    }
}

/**
 * Usage Examples and Configuration
 */
class ConditionalOnPropertyUsageExamples {
    
    public static void main(String[] args) {
        System.out.println("Conditional On Property Pattern");
        System.out.println("================================\n");
        
        System.out.println("Purpose:");
        System.out.println("- Create beans based on configuration properties");
        System.out.println("- Implement feature toggles");
        System.out.println("- Enable environment-specific configuration\n");
        
        System.out.println("Syntax:");
        System.out.println("@ConditionalOnProperty(name = \"prop.name\")");
        System.out.println("@ConditionalOnProperty(name = \"prop\", havingValue = \"value\")");
        System.out.println("@ConditionalOnProperty(prefix = \"app\", name = \"feature\")");
        System.out.println("@ConditionalOnProperty(name = \"prop\", matchIfMissing = true)\n");
        
        System.out.println("Configuration Examples:");
        System.out.println("# application.properties");
        System.out.println("cache.enabled=true");
        System.out.println("debug.mode=false");
        System.out.println("app.env=production");
        System.out.println("integrations.kafka.enabled=true");
        System.out.println("features.advanced-search=true\n");
        
        System.out.println("# application.yml");
        System.out.println("app:");
        System.out.println("  features:");
        System.out.println("    payment: stripe");
        System.out.println("monitoring:");
        System.out.println("  metrics:");
        System.out.println("    enabled: true\n");
        
        System.out.println("Common Use Cases:");
        System.out.println("1. Feature toggles (enable/disable features)");
        System.out.println("2. Environment-specific beans (dev/staging/prod)");
        System.out.println("3. Integration switches (Kafka, RabbitMQ, etc.)");
        System.out.println("4. Debug/monitoring configuration");
        System.out.println("5. Payment provider selection");
        System.out.println("6. Security level configuration");
        System.out.println("7. A/B testing variants\n");
        
        System.out.println("Best Practices:");
        System.out.println("- Use descriptive property names");
        System.out.println("- Group related properties with prefixes");
        System.out.println("- Document default behavior (matchIfMissing)");
        System.out.println("- Provide sensible defaults");
        System.out.println("- Use 'true'/'false' for boolean features");
        System.out.println("- Consider using profiles for major variants\n");
        
        System.out.println("Advanced Features:");
        System.out.println("- matchIfMissing: Default when property absent");
        System.out.println("- relaxedNames: spring.my-prop = spring.myProp");
        System.out.println("- Multiple names: All must match");
        System.out.println("- Prefix: Cleaner grouped properties");
    }
}
