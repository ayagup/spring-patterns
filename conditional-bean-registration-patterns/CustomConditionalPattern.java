package com.example.conditional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;

/**
 * Custom Conditional Pattern
 * ==========================
 * 
 * Demonstrates creating custom @Conditional annotations by implementing
 * the Condition interface. This allows you to create your own conditional
 * logic beyond the built-in Spring Boot conditionals.
 * 
 * Key Concepts:
 * ------------
 * 1. @Conditional - Base annotation for custom conditions
 * 2. Condition interface - Custom condition implementation
 * 3. ConditionContext - Access to environment, registry, resources
 * 4. AnnotatedTypeMetadata - Access to annotation attributes
 * 5. Custom Conditional Annotations - Reusable conditions
 * 
 * How It Works:
 * ------------
 * - Implement Condition interface
 * - Override matches() method
 * - Return true to create bean, false to skip
 * - Access environment, registry, classloader
 * - Create custom annotation with @Conditional
 * 
 * When to Use:
 * -----------
 * - Complex business logic conditions
 * - Multiple criteria evaluation
 * - Custom platform detection
 * - License validation
 * - Feature flag systems
 * - Time-based conditions
 * - External service checks
 * - Custom configuration logic
 * 
 * Common Patterns:
 * ---------------
 * - Environment-based conditions
 * - Time window conditions
 * - External service availability
 * - License key validation
 * - Custom platform detection
 * - Database schema checks
 * - Feature flag evaluation
 * - Multi-factor conditions
 * 
 * @author Spring Patterns
 * @version 1.0
 */

/**
 * Example 1: Time-Based Condition
 */
class BusinessHoursCondition implements Condition {
    
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        // Check if current time is within business hours
        int hour = java.time.LocalTime.now().getHour();
        boolean isBusinessHours = hour >= 9 && hour < 17;
        
        System.out.println("Business Hours Condition: " + (isBusinessHours ? "MATCHED" : "NOT MATCHED"));
        System.out.println("  Current hour: " + hour);
        System.out.println("  Business hours: 9:00 - 17:00");
        
        return isBusinessHours;
    }
}

@Configuration
class BusinessHoursConfiguration {
    
    @Bean
    @Conditional(BusinessHoursCondition.class)
    public String businessHoursService() {
        System.out.println("Creating Business Hours Service");
        System.out.println("  Active only during 9 AM - 5 PM");
        return "Business Hours Service";
    }
}

/**
 * Example 2: License-Based Condition
 */
class LicenseValidCondition implements Condition {
    
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        // Check if valid license exists
        String licenseKey = context.getEnvironment().getProperty("app.license.key");
        boolean hasLicense = licenseKey != null && !licenseKey.isEmpty();
        
        // Simulate license validation
        boolean isValid = hasLicense && licenseKey.startsWith("PREMIUM-");
        
        System.out.println("License Validation Condition: " + (isValid ? "MATCHED" : "NOT MATCHED"));
        System.out.println("  License key present: " + hasLicense);
        System.out.println("  License valid: " + isValid);
        
        return isValid;
    }
}

@Configuration
class PremiumFeaturesConfiguration {
    
    @Bean
    @Conditional(LicenseValidCondition.class)
    public String premiumFeatures() {
        System.out.println("Creating Premium Features");
        System.out.println("  Requires valid license: app.license.key=PREMIUM-XXX");
        return "Premium Features";
    }
}

/**
 * Example 3: External Service Availability Condition
 */
class ExternalServiceAvailableCondition implements Condition {
    
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String serviceUrl = context.getEnvironment().getProperty("external.service.url");
        
        if (serviceUrl == null || serviceUrl.isEmpty()) {
            System.out.println("External Service Condition: NOT MATCHED (no URL configured)");
            return false;
        }
        
        // Simulate availability check (in real app, would do HTTP call)
        boolean isAvailable = simulateServiceCheck(serviceUrl);
        
        System.out.println("External Service Condition: " + (isAvailable ? "MATCHED" : "NOT MATCHED"));
        System.out.println("  Service URL: " + serviceUrl);
        System.out.println("  Service available: " + isAvailable);
        
        return isAvailable;
    }
    
    private boolean simulateServiceCheck(String url) {
        // In real implementation, would check HTTP connectivity
        return url.contains("api");
    }
}

@Configuration
class ExternalIntegrationConfiguration {
    
    @Bean
    @Conditional(ExternalServiceAvailableCondition.class)
    public String externalServiceClient() {
        System.out.println("Creating External Service Client");
        System.out.println("  Connected to external API");
        return "External Service Client";
    }
}

/**
 * Example 4: Database Schema Condition
 */
class DatabaseSchemaExistsCondition implements Condition {
    
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        // Check if specific database schema exists
        String schemaName = context.getEnvironment().getProperty("app.database.schema", "public");
        
        // Simulate schema check (in real app, would query database)
        boolean schemaExists = simulateSchemaCheck(schemaName);
        
        System.out.println("Database Schema Condition: " + (schemaExists ? "MATCHED" : "NOT MATCHED"));
        System.out.println("  Schema name: " + schemaName);
        System.out.println("  Schema exists: " + schemaExists);
        
        return schemaExists;
    }
    
    private boolean simulateSchemaCheck(String schemaName) {
        // In real implementation, would execute SQL query
        return "public".equals(schemaName) || "app".equals(schemaName);
    }
}

@Configuration
class DatabaseConfiguration {
    
    @Bean
    @Conditional(DatabaseSchemaExistsCondition.class)
    public String databaseMigration() {
        System.out.println("Creating Database Migration Service");
        System.out.println("  Schema validated");
        return "Database Migration";
    }
}

/**
 * Example 5: Feature Flag Condition
 */
class FeatureFlagCondition implements Condition {
    
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        // Get feature flag from annotation attribute
        String featureName = (String) metadata.getAnnotationAttributes(
            ConditionalOnFeatureFlag.class.getName()).get("value");
        
        // Check if feature is enabled
        String flagKey = "features." + featureName + ".enabled";
        boolean isEnabled = context.getEnvironment().getProperty(flagKey, Boolean.class, false);
        
        System.out.println("Feature Flag Condition: " + (isEnabled ? "MATCHED" : "NOT MATCHED"));
        System.out.println("  Feature: " + featureName);
        System.out.println("  Property: " + flagKey + " = " + isEnabled);
        
        return isEnabled;
    }
}

/**
 * Custom annotation for feature flags
 */
@Conditional(FeatureFlagCondition.class)
@interface ConditionalOnFeatureFlag {
    String value();
}

@Configuration
class FeatureFlagConfiguration {
    
    @Bean
    @ConditionalOnFeatureFlag("payment")
    public String paymentFeature() {
        System.out.println("Creating Payment Feature");
        System.out.println("  Controlled by: features.payment.enabled");
        return "Payment Feature";
    }
    
    @Bean
    @ConditionalOnFeatureFlag("analytics")
    public String analyticsFeature() {
        System.out.println("Creating Analytics Feature");
        System.out.println("  Controlled by: features.analytics.enabled");
        return "Analytics Feature";
    }
}

/**
 * Example 6: Operating System Condition
 */
class WindowsCondition implements Condition {
    
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String osName = System.getProperty("os.name").toLowerCase();
        boolean isWindows = osName.contains("windows");
        
        System.out.println("Windows Condition: " + (isWindows ? "MATCHED" : "NOT MATCHED"));
        System.out.println("  OS Name: " + osName);
        
        return isWindows;
    }
}

class LinuxCondition implements Condition {
    
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String osName = System.getProperty("os.name").toLowerCase();
        boolean isLinux = osName.contains("linux");
        
        System.out.println("Linux Condition: " + (isLinux ? "MATCHED" : "NOT MATCHED"));
        System.out.println("  OS Name: " + osName);
        
        return isLinux;
    }
}

@Configuration
class OperatingSystemConfiguration {
    
    @Bean
    @Conditional(WindowsCondition.class)
    public String windowsFileSystem() {
        System.out.println("Creating Windows File System Service");
        System.out.println("  Using backslash path separator");
        return "Windows File System";
    }
    
    @Bean
    @Conditional(LinuxCondition.class)
    public String linuxFileSystem() {
        System.out.println("Creating Linux File System Service");
        System.out.println("  Using forward slash path separator");
        return "Linux File System";
    }
}

/**
 * Example 7: Environment Variable Condition
 */
class EnvironmentVariableCondition implements Condition {
    
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String varName = (String) metadata.getAnnotationAttributes(
            ConditionalOnEnvVariable.class.getName()).get("name");
        String expectedValue = (String) metadata.getAnnotationAttributes(
            ConditionalOnEnvVariable.class.getName()).get("havingValue");
        
        String actualValue = System.getenv(varName);
        boolean matches = expectedValue.equals(actualValue);
        
        System.out.println("Environment Variable Condition: " + (matches ? "MATCHED" : "NOT MATCHED"));
        System.out.println("  Variable: " + varName);
        System.out.println("  Expected: " + expectedValue);
        System.out.println("  Actual: " + actualValue);
        
        return matches;
    }
}

@Conditional(EnvironmentVariableCondition.class)
@interface ConditionalOnEnvVariable {
    String name();
    String havingValue();
}

@Configuration
class EnvironmentConfiguration {
    
    @Bean
    @ConditionalOnEnvVariable(name = "DEPLOYMENT_ENV", havingValue = "production")
    public String productionMonitoring() {
        System.out.println("Creating Production Monitoring");
        System.out.println("  Enabled in production environment");
        return "Production Monitoring";
    }
}

/**
 * Example 8: Class Present Condition (Custom Implementation)
 */
class CustomClassPresentCondition implements Condition {
    
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String className = (String) metadata.getAnnotationAttributes(
            ConditionalOnCustomClass.class.getName()).get("value");
        
        ClassLoader classLoader = context.getClassLoader();
        boolean isPresent = false;
        
        try {
            if (classLoader != null) {
                Class.forName(className, false, classLoader);
                isPresent = true;
            }
        } catch (ClassNotFoundException e) {
            isPresent = false;
        }
        
        System.out.println("Custom Class Present Condition: " + (isPresent ? "MATCHED" : "NOT MATCHED"));
        System.out.println("  Class: " + className);
        
        return isPresent;
    }
}

@Conditional(CustomClassPresentCondition.class)
@interface ConditionalOnCustomClass {
    String value();
}

@Configuration
class CustomClassConfiguration {
    
    @Bean
    @ConditionalOnCustomClass("com.example.CustomLibrary")
    public String customLibraryIntegration() {
        System.out.println("Creating Custom Library Integration");
        return "Custom Library Integration";
    }
}

/**
 * Example 9: Multiple Conditions (AND Logic)
 */
class ProductionAndSecureCondition implements Condition {
    
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        boolean isProduction = "production".equals(
            context.getEnvironment().getProperty("spring.profiles.active"));
        boolean isSslEnabled = context.getEnvironment().getProperty(
            "server.ssl.enabled", Boolean.class, false);
        
        boolean matches = isProduction && isSslEnabled;
        
        System.out.println("Production AND Secure Condition: " + (matches ? "MATCHED" : "NOT MATCHED"));
        System.out.println("  Is Production: " + isProduction);
        System.out.println("  SSL Enabled: " + isSslEnabled);
        
        return matches;
    }
}

@Configuration
class SecureProductionConfiguration {
    
    @Bean
    @Conditional(ProductionAndSecureCondition.class)
    public String secureProductionService() {
        System.out.println("Creating Secure Production Service");
        System.out.println("  Requires: production profile + SSL enabled");
        return "Secure Production Service";
    }
}

/**
 * Example 10: Resource Exists Condition (Custom)
 */
class CustomResourceCondition implements Condition {
    
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String resourcePath = (String) metadata.getAnnotationAttributes(
            ConditionalOnCustomResource.class.getName()).get("value");
        
        boolean exists = context.getResourceLoader().getResource(resourcePath).exists();
        
        System.out.println("Custom Resource Condition: " + (exists ? "MATCHED" : "NOT MATCHED"));
        System.out.println("  Resource: " + resourcePath);
        System.out.println("  Exists: " + exists);
        
        return exists;
    }
}

@Conditional(CustomResourceCondition.class)
@interface ConditionalOnCustomResource {
    String value();
}

@Configuration
class CustomResourceConfiguration {
    
    @Bean
    @ConditionalOnCustomResource("classpath:custom-config.xml")
    public String customConfigLoader() {
        System.out.println("Creating Custom Config Loader");
        return "Custom Config Loader";
    }
}

/**
 * Main Pattern Class
 */
@Configuration
public class CustomConditionalPattern {
    
    /**
     * Example: Simple custom condition
     */
    @Bean
    public String customConditionInfo() {
        System.out.println("Custom Conditional Pattern Examples");
        System.out.println("  Create your own @Conditional by implementing Condition interface");
        return "Custom Condition Info";
    }
}

/**
 * Usage Examples and Best Practices
 */
class CustomConditionalUsageExamples {
    
    public static void main(String[] args) {
        System.out.println("Custom Conditional Pattern");
        System.out.println("==========================\n");
        
        System.out.println("Purpose:");
        System.out.println("- Create custom conditional logic");
        System.out.println("- Implement complex business rules");
        System.out.println("- Extend Spring Boot's conditional framework\n");
        
        System.out.println("Implementation Steps:");
        System.out.println("1. Implement Condition interface");
        System.out.println("2. Override matches() method");
        System.out.println("3. Return true/false based on logic");
        System.out.println("4. Create custom annotation (optional)");
        System.out.println("5. Use @Conditional(YourCondition.class)\n");
        
        System.out.println("Common Custom Conditions:");
        System.out.println("1. Time-Based - Business hours, maintenance windows");
        System.out.println("2. License Validation - Premium features");
        System.out.println("3. External Service - API availability");
        System.out.println("4. Database Schema - Schema existence");
        System.out.println("5. Feature Flags - Dynamic feature control");
        System.out.println("6. Operating System - OS-specific beans");
        System.out.println("7. Environment Variables - Deployment context");
        System.out.println("8. Class Presence - Custom library detection");
        System.out.println("9. Multiple Criteria - Complex AND/OR logic");
        System.out.println("10. Resource Existence - File/config checks\n");
        
        System.out.println("ConditionContext Provides:");
        System.out.println("- getEnvironment() - Access properties");
        System.out.println("- getBeanFactory() - Access bean registry");
        System.out.println("- getClassLoader() - Load classes");
        System.out.println("- getResourceLoader() - Load resources");
        System.out.println("- getRegistry() - Bean definition registry\n");
        
        System.out.println("Best Practices:");
        System.out.println("- Keep conditions simple and focused");
        System.out.println("- Log condition evaluation results");
        System.out.println("- Handle exceptions gracefully");
        System.out.println("- Document condition requirements");
        System.out.println("- Test conditions thoroughly");
        System.out.println("- Avoid expensive operations in matches()");
        System.out.println("- Cache results when appropriate");
        System.out.println("- Create reusable annotation wrappers\n");
        
        System.out.println("Example Implementation:");
        System.out.println("public class MyCondition implements Condition {");
        System.out.println("  ");
        System.out.println("  @Override");
        System.out.println("  public boolean matches(ConditionContext context,");
        System.out.println("                         AnnotatedTypeMetadata metadata) {");
        System.out.println("    String value = context.getEnvironment()");
        System.out.println("        .getProperty(\"my.property\");");
        System.out.println("    return \"expected\".equals(value);");
        System.out.println("  }");
        System.out.println("}");
        System.out.println("");
        System.out.println("@Configuration");
        System.out.println("public class MyConfig {");
        System.out.println("  ");
        System.out.println("  @Bean");
        System.out.println("  @Conditional(MyCondition.class)");
        System.out.println("  public MyService myService() {");
        System.out.println("    return new MyService();");
        System.out.println("  }");
        System.out.println("}");
    }
}
