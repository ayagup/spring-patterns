package com.example.customannotation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.lang.annotation.*;

/**
 * Conditional Annotation Pattern
 * 
 * Demonstrates conditional bean registration using @Conditional and custom condition annotations.
 * Beans are registered only when specific conditions are met:
 * - Property-based conditions
 * - Class presence conditions
 * - Profile-based conditions
 * - Custom logic conditions
 * - OS/environment conditions
 * 
 * Key Features:
 * - Conditional bean creation
 * - Custom @Conditional annotations
 * - Environment-based configuration
 * - Feature toggles
 * - Platform-specific beans
 * 
 * Use Cases:
 * - Feature flags
 * - Environment-specific beans
 * - Optional dependencies
 * - Multi-platform applications
 * - A/B testing
 * 
 * @author Spring Patterns
 */
@SpringBootApplication
public class ConditionalAnnotationPattern {

    public static void main(String[] args) {
        SpringApplication.run(ConditionalAnnotationPattern.class, args);
    }

    // =========================================================================
    // CUSTOM CONDITIONAL ANNOTATIONS
    // =========================================================================

    /**
     * Conditional on feature flag
     */
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Conditional(OnFeatureFlagCondition.class)
    public @interface ConditionalOnFeatureFlag {
        
        /**
         * Feature flag name
         */
        String value();
        
        /**
         * Whether feature should be enabled or disabled
         */
        boolean enabled() default true;
    }

    /**
     * Conditional on specific OS
     */
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Conditional(OnOSCondition.class)
    public @interface ConditionalOnOS {
        
        /**
         * Operating system names
         */
        String[] value();
    }

    /**
     * Conditional on development environment
     */
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Conditional(OnDevelopmentCondition.class)
    public @interface ConditionalOnDevelopment {
    }

    /**
     * Conditional on production environment
     */
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Conditional(OnProductionCondition.class)
    public @interface ConditionalOnProduction {
    }

    /**
     * Conditional on cloud environment
     */
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Conditional(OnCloudCondition.class)
    public @interface ConditionalOnCloud {
        
        /**
         * Cloud provider
         */
        String provider() default "";
    }

    /**
     * Conditional on user role
     */
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Conditional(OnUserRoleCondition.class)
    public @interface ConditionalOnUserRole {
        
        /**
         * Required roles
         */
        String[] roles();
    }

    // =========================================================================
    // CONDITION IMPLEMENTATIONS
    // =========================================================================

    /**
     * Condition for feature flags
     */
    public static class OnFeatureFlagCondition implements org.springframework.context.annotation.Condition {
        
        @Override
        public boolean matches(org.springframework.context.annotation.ConditionContext context,
                             AnnotatedTypeMetadata metadata) {
            
            java.util.Map<String, Object> attributes = metadata.getAnnotationAttributes(
                    ConditionalOnFeatureFlag.class.getName());
            
            if (attributes == null) {
                return false;
            }
            
            String flagName = (String) attributes.get("value");
            boolean expectedEnabled = (boolean) attributes.get("enabled");
            
            // Check property: feature.{flagName}.enabled
            String propertyName = "feature." + flagName + ".enabled";
            String flagValue = context.getEnvironment().getProperty(propertyName, "false");
            boolean actualEnabled = Boolean.parseBoolean(flagValue);
            
            boolean matches = actualEnabled == expectedEnabled;
            System.out.println("Feature flag " + flagName + ": expected=" + expectedEnabled + 
                             ", actual=" + actualEnabled + ", matches=" + matches);
            
            return matches;
        }
    }

    /**
     * Condition for operating system
     */
    public static class OnOSCondition implements org.springframework.context.annotation.Condition {
        
        @Override
        public boolean matches(org.springframework.context.annotation.ConditionContext context,
                             AnnotatedTypeMetadata metadata) {
            
            java.util.Map<String, Object> attributes = metadata.getAnnotationAttributes(
                    ConditionalOnOS.class.getName());
            
            if (attributes == null) {
                return false;
            }
            
            String[] osNames = (String[]) attributes.get("value");
            String currentOS = System.getProperty("os.name").toLowerCase();
            
            for (String os : osNames) {
                if (currentOS.contains(os.toLowerCase())) {
                    System.out.println("OS condition matched: " + os);
                    return true;
                }
            }
            
            System.out.println("OS condition not matched. Current: " + currentOS);
            return false;
        }
    }

    /**
     * Condition for development environment
     */
    public static class OnDevelopmentCondition implements org.springframework.context.annotation.Condition {
        
        @Override
        public boolean matches(org.springframework.context.annotation.ConditionContext context,
                             AnnotatedTypeMetadata metadata) {
            
            String[] profiles = context.getEnvironment().getActiveProfiles();
            boolean isDev = java.util.Arrays.asList(profiles).contains("dev") ||
                           java.util.Arrays.asList(profiles).contains("development");
            
            System.out.println("Development condition: " + isDev);
            return isDev;
        }
    }

    /**
     * Condition for production environment
     */
    public static class OnProductionCondition implements org.springframework.context.annotation.Condition {
        
        @Override
        public boolean matches(org.springframework.context.annotation.ConditionContext context,
                             AnnotatedTypeMetadata metadata) {
            
            String[] profiles = context.getEnvironment().getActiveProfiles();
            boolean isProd = java.util.Arrays.asList(profiles).contains("prod") ||
                            java.util.Arrays.asList(profiles).contains("production");
            
            System.out.println("Production condition: " + isProd);
            return isProd;
        }
    }

    /**
     * Condition for cloud environment
     */
    public static class OnCloudCondition implements org.springframework.context.annotation.Condition {
        
        @Override
        public boolean matches(org.springframework.context.annotation.ConditionContext context,
                             AnnotatedTypeMetadata metadata) {
            
            java.util.Map<String, Object> attributes = metadata.getAnnotationAttributes(
                    ConditionalOnCloud.class.getName());
            
            String cloudProvider = context.getEnvironment().getProperty("cloud.provider", "");
            
            if (attributes != null && !((String) attributes.get("provider")).isEmpty()) {
                String requiredProvider = (String) attributes.get("provider");
                boolean matches = cloudProvider.equalsIgnoreCase(requiredProvider);
                System.out.println("Cloud provider condition: required=" + requiredProvider + 
                                 ", actual=" + cloudProvider + ", matches=" + matches);
                return matches;
            }
            
            // Just check if any cloud provider is configured
            boolean isCloud = !cloudProvider.isEmpty();
            System.out.println("Cloud condition: " + isCloud);
            return isCloud;
        }
    }

    /**
     * Condition for user role
     */
    public static class OnUserRoleCondition implements org.springframework.context.annotation.Condition {
        
        @Override
        public boolean matches(org.springframework.context.annotation.ConditionContext context,
                             AnnotatedTypeMetadata metadata) {
            
            java.util.Map<String, Object> attributes = metadata.getAnnotationAttributes(
                    ConditionalOnUserRole.class.getName());
            
            if (attributes == null) {
                return false;
            }
            
            String[] requiredRoles = (String[]) attributes.get("roles");
            String userRoles = context.getEnvironment().getProperty("user.roles", "");
            
            for (String role : requiredRoles) {
                if (userRoles.contains(role)) {
                    System.out.println("User role condition matched: " + role);
                    return true;
                }
            }
            
            return false;
        }
    }

    // =========================================================================
    // CONDITIONAL BEAN CONFIGURATIONS
    // =========================================================================

    @Configuration
    public static class FeatureFlagConfig {
        
        @Bean
        @ConditionalOnFeatureFlag("new-ui")
        public UIService newUIService() {
            System.out.println("Creating NEW UI Service");
            return new UIService("New UI - Feature Enabled");
        }
        
        @Bean
        @ConditionalOnFeatureFlag(value = "new-ui", enabled = false)
        public UIService legacyUIService() {
            System.out.println("Creating LEGACY UI Service");
            return new UIService("Legacy UI - Feature Disabled");
        }
        
        @Bean
        @ConditionalOnFeatureFlag("analytics")
        public AnalyticsService analyticsService() {
            System.out.println("Creating Analytics Service");
            return new AnalyticsService();
        }
    }

    @Configuration
    public static class PlatformSpecificConfig {
        
        @Bean
        @ConditionalOnOS({"windows"})
        public FileSystemService windowsFileSystem() {
            System.out.println("Creating Windows FileSystem");
            return new FileSystemService("Windows", "C:\\");
        }
        
        @Bean
        @ConditionalOnOS({"linux", "unix"})
        public FileSystemService linuxFileSystem() {
            System.out.println("Creating Linux FileSystem");
            return new FileSystemService("Linux", "/");
        }
        
        @Bean
        @ConditionalOnOS({"mac"})
        public FileSystemService macFileSystem() {
            System.out.println("Creating Mac FileSystem");
            return new FileSystemService("Mac", "/Users");
        }
    }

    @Configuration
    public static class EnvironmentConfig {
        
        @Bean
        @ConditionalOnDevelopment
        public DatabaseService devDatabase() {
            System.out.println("Creating DEV Database");
            return new DatabaseService("localhost", "dev_db");
        }
        
        @Bean
        @ConditionalOnProduction
        public DatabaseService prodDatabase() {
            System.out.println("Creating PROD Database");
            return new DatabaseService("prod-server.com", "prod_db");
        }
        
        @Bean
        @ConditionalOnDevelopment
        public LoggingService debugLogging() {
            System.out.println("Creating DEBUG Logging");
            return new LoggingService("DEBUG");
        }
        
        @Bean
        @ConditionalOnProduction
        public LoggingService infoLogging() {
            System.out.println("Creating INFO Logging");
            return new LoggingService("INFO");
        }
    }

    @Configuration
    public static class CloudConfig {
        
        @Bean
        @ConditionalOnCloud(provider = "AWS")
        public StorageService s3Storage() {
            System.out.println("Creating AWS S3 Storage");
            return new StorageService("S3");
        }
        
        @Bean
        @ConditionalOnCloud(provider = "Azure")
        public StorageService azureBlobStorage() {
            System.out.println("Creating Azure Blob Storage");
            return new StorageService("Azure Blob");
        }
        
        @Bean
        @ConditionalOnCloud(provider = "GCP")
        public StorageService googleCloudStorage() {
            System.out.println("Creating Google Cloud Storage");
            return new StorageService("GCS");
        }
    }

    // Using Spring's built-in conditionals

    @Configuration
    public static class BuiltInConditionalsConfig {
        
        /**
         * Bean created only if property exists
         */
        @Bean
        @ConditionalOnProperty(name = "email.enabled", havingValue = "true")
        public EmailService emailService() {
            System.out.println("Creating Email Service");
            return new EmailService();
        }
        
        /**
         * Bean created only if class is on classpath
         */
        @Bean
        @ConditionalOnClass(name = "com.mysql.cj.jdbc.Driver")
        public MySQLService mySQLService() {
            System.out.println("Creating MySQL Service");
            return new MySQLService();
        }
        
        /**
         * Bean created only if another bean doesn't exist
         */
        @Bean
        @ConditionalOnMissingBean(CacheService.class)
        public CacheService defaultCache() {
            System.out.println("Creating Default Cache (no cache bean found)");
            return new CacheService("Default");
        }
        
        /**
         * Bean created only if resource exists
         */
        @Bean
        @ConditionalOnResource(resources = "classpath:application.properties")
        public ConfigService configService() {
            System.out.println("Creating Config Service");
            return new ConfigService();
        }
    }

    // Service Classes

    public static class UIService {
        private final String version;

        public UIService(String version) {
            this.version = version;
            System.out.println("UI Service initialized: " + version);
        }

        public String getVersion() { return version; }
    }

    public static class AnalyticsService {
        public void trackEvent(String event) {
            System.out.println("Tracking event: " + event);
        }
    }

    public static class FileSystemService {
        private final String os;
        private final String rootPath;

        public FileSystemService(String os, String rootPath) {
            this.os = os;
            this.rootPath = rootPath;
        }

        public String getRootPath() { return rootPath; }
    }

    public static class DatabaseService {
        private final String host;
        private final String database;

        public DatabaseService(String host, String database) {
            this.host = host;
            this.database = database;
        }

        public String getConnectionString() {
            return "jdbc:mysql://" + host + "/" + database;
        }
    }

    public static class LoggingService {
        private final String level;

        public LoggingService(String level) {
            this.level = level;
        }

        public void log(String message) {
            System.out.println("[" + level + "] " + message);
        }
    }

    public static class StorageService {
        private final String provider;

        public StorageService(String provider) {
            this.provider = provider;
        }

        public void store(String key, String value) {
            System.out.println("[" + provider + "] Storing: " + key);
        }
    }

    public static class EmailService {
        public void send(String to, String subject) {
            System.out.println("Sending email to: " + to);
        }
    }

    public static class MySQLService {
        public void connect() {
            System.out.println("Connecting to MySQL");
        }
    }

    public static class CacheService {
        private final String type;

        public CacheService(String type) {
            this.type = type;
        }

        public void put(String key, Object value) {
            System.out.println("[" + type + "] Caching: " + key);
        }
    }

    public static class ConfigService {
        public String get(String key) {
            System.out.println("Getting config: " + key);
            return "";
        }
    }
}

/**
 * DOCUMENTATION
 * 
 * Conditional Annotation Pattern:
 * 
 * 1. @Conditional Basics:
 *    - Controls bean registration
 *    - Evaluated at application startup
 *    - Implement org.springframework.context.annotation.Condition
 *    - Return true to register bean
 * 
 * 2. Built-in Spring Boot Conditionals:
 *    - @ConditionalOnProperty: Property value check
 *    - @ConditionalOnClass: Class presence check
 *    - @ConditionalOnMissingClass: Class absence check
 *    - @ConditionalOnBean: Bean presence check
 *    - @ConditionalOnMissingBean: Bean absence check
 *    - @ConditionalOnResource: Resource existence check
 *    - @ConditionalOnWebApplication: Web application check
 *    - @ConditionalOnExpression: SpEL expression
 * 
 * 3. Custom Conditionals:
 *    - Create annotation with @Conditional
 *    - Implement Condition interface
 *    - Access ConditionContext for environment info
 *    - Read annotation attributes via metadata
 * 
 * 4. Use Cases:
 *    - Feature flags/toggles
 *    - Environment-specific configuration
 *    - Optional dependency handling
 *    - Platform-specific implementations
 *    - A/B testing
 *    - Multi-tenant configurations
 * 
 * 5. ConditionContext API:
 *    - getEnvironment(): Access properties, profiles
 *    - getBeanFactory(): Check bean definitions
 *    - getResourceLoader(): Load resources
 *    - getClassLoader(): Check class availability
 *    - getRegistry(): Access bean definition registry
 * 
 * 6. Best Practices:
 *    - Keep conditions simple and fast
 *    - Log condition evaluation results
 *    - Use built-in conditionals when possible
 *    - Document condition requirements
 *    - Test with different configurations
 * 
 * 7. Common Patterns:
 *    - Feature toggles via properties
 *    - Environment-based beans (dev/prod)
 *    - Cloud provider selection
 *    - OS-specific implementations
 *    - Graceful degradation
 * 
 * 8. Testing Conditionals:
 *    - Set properties in test configuration
 *    - Use @ActiveProfiles for profile testing
 *    - Mock ConditionContext
 *    - Test both true and false paths
 *    - Verify bean presence/absence
 */
