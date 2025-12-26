package com.example.conditional;

import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Conditional On Resource Pattern
 * ===============================
 * 
 * Demonstrates @ConditionalOnResource annotation that creates beans only when
 * specific resources exist in the classpath or file system. This enables
 * configuration based on file presence.
 * 
 * Key Concepts:
 * ------------
 * 1. @ConditionalOnResource - Bean registration based on resource presence
 * 2. Resource Detection - Check for files/resources
 * 3. Configuration Files - Enable features when config files present
 * 4. Classpath Resources - Check resources in JAR/classpath
 * 5. File System Resources - Check files on disk
 * 
 * How It Works:
 * ------------
 * - Checks for resource existence using Spring's resource loading
 * - Supports classpath:, file:, http:, and other prefixes
 * - Multiple resources can be specified (ALL must exist)
 * - Resource location uses Spring's ResourceLoader
 * - Evaluated at configuration processing time
 * 
 * Resource Patterns:
 * -----------------
 * - classpath:config/app.properties
 * - file:/etc/myapp/config.xml
 * - classpath*:META-INF/services/*
 * - ${user.home}/.myapp/settings.properties
 * 
 * Common Use Cases:
 * ----------------
 * - Enable features when license file present
 * - Load configuration when config file exists
 * - Activate modules based on descriptor files
 * - Check for SSL certificates
 * - Verify database scripts existence
 * - Conditional on external configuration
 * 
 * Syntax:
 * ------
 * @ConditionalOnResource(resources = "classpath:config.properties")
 * @ConditionalOnResource(resources = {"file1.xml", "file2.xml"})
 * @ConditionalOnResource(resources = "file:/etc/app/license.key")
 * 
 * @author Spring Patterns
 * @version 1.0
 */

/**
 * Example 1: License-Based Feature Enablement
 */
@Configuration
class LicenseConfiguration {
    
    /**
     * Enable premium features only if license file exists
     */
    @Bean
    @ConditionalOnResource(resources = "classpath:license/premium.license")
    public String premiumFeatures() {
        System.out.println("Creating Premium Features (premium.license found)");
        System.out.println("  License file: classpath:license/premium.license");
        return "Premium Features Module";
    }
    
    /**
     * Enable enterprise features if enterprise license exists
     */
    @Bean
    @ConditionalOnResource(resources = "classpath:license/enterprise.license")
    public String enterpriseFeatures() {
        System.out.println("Creating Enterprise Features (enterprise.license found)");
        return "Enterprise Features Module";
    }
}

/**
 * Example 2: Custom Configuration Loading
 */
@Configuration
class CustomConfigurationLoader {
    
    /**
     * Load custom database config if file exists
     */
    @Bean
    @ConditionalOnResource(resources = "classpath:custom/database.properties")
    public String customDatabaseConfig() {
        System.out.println("Loading Custom Database Config");
        System.out.println("  From: classpath:custom/database.properties");
        return "Custom Database Configuration";
    }
    
    /**
     * Load API keys from external file
     */
    @Bean
    @ConditionalOnResource(resources = "file:${user.home}/.myapp/api-keys.properties")
    public String apiKeyConfiguration() {
        System.out.println("Loading API Keys from user home directory");
        return "API Key Configuration";
    }
}

/**
 * Example 3: SSL Certificate Detection
 */
@Configuration
class SSLConfiguration {
    
    /**
     * Enable SSL if certificate files exist
     */
    @Bean
    @ConditionalOnResource(resources = {
        "classpath:ssl/keystore.jks",
        "classpath:ssl/truststore.jks"
    })
    public String sslConfiguration() {
        System.out.println("Creating SSL Configuration");
        System.out.println("  Keystore and Truststore found");
        return "SSL Configuration";
    }
}

/**
 * Example 4: Database Migration Scripts
 */
@Configuration
class DatabaseMigrationConfiguration {
    
    /**
     * Enable Flyway if migration scripts exist
     */
    @Bean
    @ConditionalOnResource(resources = "classpath:db/migration/V1__init.sql")
    public String flywayMigration() {
        System.out.println("Creating Flyway Migration (migration scripts found)");
        System.out.println("  Migration directory: classpath:db/migration/");
        return "Flyway Migration";
    }
    
    /**
     * Enable Liquibase if changelog exists
     */
    @Bean
    @ConditionalOnResource(resources = "classpath:db/changelog/db.changelog-master.xml")
    public String liquibaseMigration() {
        System.out.println("Creating Liquibase Migration (changelog found)");
        return "Liquibase Migration";
    }
}

/**
 * Example 5: Template Detection
 */
@Configuration
class TemplateConfiguration {
    
    /**
     * Enable email templates if they exist
     */
    @Bean
    @ConditionalOnResource(resources = "classpath:templates/email/welcome.html")
    public String emailTemplateEngine() {
        System.out.println("Creating Email Template Engine (templates found)");
        return "Email Template Engine";
    }
    
    /**
     * Enable report templates
     */
    @Bean
    @ConditionalOnResource(resources = "classpath:templates/reports/invoice.pdf")
    public String reportGenerator() {
        System.out.println("Creating Report Generator (PDF template found)");
        return "Report Generator";
    }
}

/**
 * Example 6: Plugin System
 */
@Configuration
class PluginConfiguration {
    
    /**
     * Load plugin if descriptor file exists
     */
    @Bean
    @ConditionalOnResource(resources = "classpath:plugins/payment-plugin.xml")
    public String paymentPlugin() {
        System.out.println("Loading Payment Plugin (descriptor found)");
        System.out.println("  Descriptor: classpath:plugins/payment-plugin.xml");
        return "Payment Plugin";
    }
    
    /**
     * Load notification plugin
     */
    @Bean
    @ConditionalOnResource(resources = "classpath:plugins/notification-plugin.xml")
    public String notificationPlugin() {
        System.out.println("Loading Notification Plugin");
        return "Notification Plugin";
    }
}

/**
 * Example 7: Static Resource Configuration
 */
@Configuration
class StaticResourceConfiguration {
    
    /**
     * Serve static resources if they exist
     */
    @Bean
    @ConditionalOnResource(resources = "classpath:static/index.html")
    public String staticResourceHandler() {
        System.out.println("Creating Static Resource Handler (static files found)");
        System.out.println("  Serving from: classpath:static/");
        return "Static Resource Handler";
    }
}

/**
 * Example 8: Feature Module Detection
 */
@Configuration
class FeatureModuleConfiguration {
    
    /**
     * Enable analytics module if descriptor exists
     */
    @Bean
    @ConditionalOnResource(resources = "classpath:META-INF/modules/analytics.module")
    public String analyticsModule() {
        System.out.println("Loading Analytics Module");
        System.out.println("  Module descriptor: META-INF/modules/analytics.module");
        return "Analytics Module";
    }
}

/**
 * Example 9: I18n Resource Loading
 */
@Configuration
class InternationalizationConfiguration {
    
    /**
     * Enable i18n if message bundles exist
     */
    @Bean
    @ConditionalOnResource(resources = "classpath:i18n/messages.properties")
    public String messageSource() {
        System.out.println("Creating Message Source (i18n resources found)");
        System.out.println("  Message bundle: classpath:i18n/messages.properties");
        return "Message Source";
    }
}

/**
 * Example 10: External Configuration Detection
 */
@Configuration
class ExternalConfiguration {
    
    /**
     * Load external config if file exists in /etc
     */
    @Bean
    @ConditionalOnResource(resources = "file:/etc/myapp/application.yml")
    public String externalConfigLoader() {
        System.out.println("Loading External Configuration");
        System.out.println("  From: /etc/myapp/application.yml");
        return "External Configuration Loader";
    }
}

/**
 * Main Pattern Class
 */
@Configuration
public class ConditionalOnResourcePattern {
    
    /**
     * Example: Enable feature if configuration file exists
     */
    @Bean
    @ConditionalOnResource(resources = "classpath:config/features.yml")
    public String featureConfiguration() {
        System.out.println("Loading Feature Configuration");
        System.out.println("  Configuration file: classpath:config/features.yml");
        System.out.println("  Features enabled based on config file");
        return "Feature Configuration";
    }
    
    /**
     * Example: Multiple resources (ALL must exist)
     */
    @Bean
    @ConditionalOnResource(resources = {
        "classpath:config/app.properties",
        "classpath:config/database.properties"
    })
    public String multiResourceConfiguration() {
        System.out.println("Loading Multi-Resource Configuration");
        System.out.println("  Both app.properties and database.properties found");
        return "Multi-Resource Configuration";
    }
}

/**
 * Usage Examples and Best Practices
 */
class ConditionalOnResourceUsageExamples {
    
    public static void main(String[] args) {
        System.out.println("Conditional On Resource Pattern");
        System.out.println("================================\n");
        
        System.out.println("Purpose:");
        System.out.println("- Create beans only when specific resources exist");
        System.out.println("- Enable features based on file presence");
        System.out.println("- Load configurations conditionally\n");
        
        System.out.println("Resource Location Syntax:");
        System.out.println("1. Classpath: classpath:config/app.properties");
        System.out.println("2. File system: file:/etc/myapp/config.xml");
        System.out.println("3. User home: file:${user.home}/.myapp/settings.ini");
        System.out.println("4. HTTP: http://config-server/app.properties");
        System.out.println("5. Pattern: classpath*:META-INF/*.xml\n");
        
        System.out.println("Common Use Cases:");
        System.out.println("1. License file detection (premium features)");
        System.out.println("2. Custom configuration loading");
        System.out.println("3. SSL certificate verification");
        System.out.println("4. Database migration scripts");
        System.out.println("5. Template file detection");
        System.out.println("6. Plugin system (descriptor files)");
        System.out.println("7. Static resource serving");
        System.out.println("8. Feature module detection");
        System.out.println("9. I18n message bundles");
        System.out.println("10. External configuration files\n");
        
        System.out.println("Examples:");
        System.out.println("// License-based features");
        System.out.println("@ConditionalOnResource(");
        System.out.println("  resources = \"classpath:license/premium.license\"");
        System.out.println(")");
        System.out.println("public PremiumFeatures premiumFeatures() { ... }\n");
        
        System.out.println("// SSL configuration");
        System.out.println("@ConditionalOnResource(resources = {");
        System.out.println("  \"classpath:ssl/keystore.jks\",");
        System.out.println("  \"classpath:ssl/truststore.jks\"");
        System.out.println("})");
        System.out.println("public SSLConfig sslConfig() { ... }\n");
        
        System.out.println("Best Practices:");
        System.out.println("- Use descriptive resource paths");
        System.out.println("- Document required resources");
        System.out.println("- Provide clear error messages");
        System.out.println("- Consider fallback configurations");
        System.out.println("- Use classpath: for packaged resources");
        System.out.println("- Use file: for external resources");
        System.out.println("- Test with resources present and absent");
    }
}
