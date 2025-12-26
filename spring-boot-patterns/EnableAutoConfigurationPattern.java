package com.example.springboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Enable Auto-Configuration Pattern
 * ==================================
 * 
 * Demonstrates the @EnableAutoConfiguration annotation pattern in Spring Boot
 * for enabling automatic configuration of Spring application context based on
 * classpath dependencies and defined beans.
 * 
 * Use Cases:
 * ----------
 * 1. Automatic configuration of Spring applications
 * 2. Convention-over-configuration approach
 * 3. Minimal manual configuration requirements
 * 4. Conditional bean creation based on classpath
 * 5. Auto-configuration of third-party libraries
 * 6. Development-time productivity enhancement
 * 7. Standardized application setup
 * 8. Starter dependencies integration
 * 
 * Key Features:
 * -------------
 * - Automatically configures beans based on classpath
 * - Works with spring.factories mechanism
 * - Conditional configuration using @Conditional
 * - Can be customized and excluded
 * - Part of @SpringBootApplication meta-annotation
 * - Scans META-INF/spring.factories files
 * - Creates beans when dependencies are present
 * - Respects user-defined configurations
 * 
 * @EnableAutoConfiguration vs @SpringBootApplication:
 * ----------------------------------------------------
 * @EnableAutoConfiguration:
 *   - Only enables auto-configuration
 *   - Doesn't enable component scanning
 *   - Doesn't mark class as @Configuration
 *   - More granular control
 *   - Used when customizing setup
 * 
 * @SpringBootApplication:
 *   - Meta-annotation including @EnableAutoConfiguration
 *   - Also includes @ComponentScan
 *   - Also includes @Configuration
 *   - Convenience annotation
 *   - Standard for most applications
 * 
 * How Auto-Configuration Works:
 * ------------------------------
 * 1. Spring Boot scans META-INF/spring.factories
 * 2. Loads classes listed under EnableAutoConfiguration key
 * 3. Evaluates @Conditional annotations on each class
 * 4. Creates beans if conditions are met
 * 5. User beans take precedence over auto-configured beans
 * 6. Auto-configuration happens after user configuration
 * 
 * Exclusion Patterns:
 * -------------------
 * Method 1: Annotation-based
 *   @EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
 * 
 * Method 2: Application properties
 *   spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
 * 
 * Method 3: excludeName attribute
 *   @EnableAutoConfiguration(excludeName = {"org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration"})
 * 
 * Common Auto-Configurations:
 * ----------------------------
 * - DataSourceAutoConfiguration (database)
 * - HibernateJpaAutoConfiguration (JPA)
 * - JacksonAutoConfiguration (JSON)
 * - ThymeleafAutoConfiguration (templates)
 * - SecurityAutoConfiguration (security)
 * - WebMvcAutoConfiguration (web MVC)
 * - ReactiveWebServerFactoryAutoConfiguration (WebFlux)
 * - RabbitAutoConfiguration (messaging)
 * - RedisAutoConfiguration (cache)
 * - MongoAutoConfiguration (NoSQL)
 * 
 * Best Practices:
 * ---------------
 * 1. Use @SpringBootApplication for standard apps
 * 2. Use @EnableAutoConfiguration for custom setups
 * 3. Exclude unnecessary auto-configurations
 * 4. Review auto-configuration report
 * 5. Override with user-defined beans when needed
 * 6. Use @ConditionalOnMissingBean appropriately
 * 7. Document excluded configurations
 * 8. Test with different profiles
 * 
 * Debug Auto-Configuration:
 * --------------------------
 * # Enable debug mode
 * debug=true
 * 
 * # Or run with flag
 * java -jar app.jar --debug
 * 
 * # Actuator conditions endpoint
 * management.endpoints.web.exposure.include=conditions
 * http://localhost:8080/actuator/conditions
 * 
 * @author Spring Patterns
 * @version 1.0
 */

/**
 * Example 1: Using @EnableAutoConfiguration directly
 * This demonstrates manual configuration without @SpringBootApplication
 */
@Configuration
@EnableAutoConfiguration(exclude = {
    DataSourceAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class
})
class ManualAutoConfigurationExample {
    // Manual configuration with selective auto-configuration
}

/**
 * Example 2: Standard @SpringBootApplication
 * This is the recommended approach for most applications
 */
@SpringBootApplication
public class EnableAutoConfigurationPattern {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = 
            SpringApplication.run(EnableAutoConfigurationPattern.class, args);
        
        // Display auto-configuration information
        displayAutoConfigurationInfo(context);
    }

    private static void displayAutoConfigurationInfo(ConfigurableApplicationContext context) {
        System.out.println("\n=== Auto-Configuration Information ===\n");
        
        // Display loaded bean count
        String[] beanNames = context.getBeanDefinitionNames();
        System.out.println("Total Beans Loaded: " + beanNames.length);
        
        // Display auto-configured beans (simplified detection)
        long autoConfiguredCount = Arrays.stream(beanNames)
            .filter(name -> name.contains("AutoConfiguration"))
            .count();
        System.out.println("Auto-Configuration Beans: " + autoConfiguredCount);
        
        // Display environment info
        System.out.println("\nActive Profiles: " + 
            Arrays.toString(context.getEnvironment().getActiveProfiles()));
        
        System.out.println("\n======================================\n");
    }
}

/**
 * Example 3: Excluding multiple auto-configurations
 */
@Configuration
@EnableAutoConfiguration(exclude = {
    DataSourceAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class
})
class ExclusionExample {
    
    @Bean
    public String excludedConfigInfo() {
        return "DataSource and JPA auto-configurations are excluded";
    }
}

/**
 * Example 4: Using excludeName for string-based exclusion
 * Useful when the class is not on the classpath
 */
@Configuration
@EnableAutoConfiguration(excludeName = {
    "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration",
    "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration"
})
class ExcludeByNameExample {
    // Excludes by fully qualified class name
}

/**
 * Controller to expose auto-configuration information
 */
@RestController
class AutoConfigurationController {
    
    private final ConfigurableApplicationContext context;
    
    public AutoConfigurationController(ConfigurableApplicationContext context) {
        this.context = context;
    }
    
    /**
     * GET /auto-config/beans
     * Returns information about all loaded beans
     */
    @GetMapping("/auto-config/beans")
    public Map<String, Object> getBeans() {
        String[] beanNames = context.getBeanDefinitionNames();
        
        return Map.of(
            "totalBeans", beanNames.length,
            "autoConfigBeans", Arrays.stream(beanNames)
                .filter(name -> name.contains("AutoConfiguration"))
                .collect(Collectors.toList()),
            "userBeans", Arrays.stream(beanNames)
                .filter(name -> !name.contains("AutoConfiguration") && 
                               !name.startsWith("org.springframework"))
                .limit(20) // Limit for readability
                .collect(Collectors.toList())
        );
    }
    
    /**
     * GET /auto-config/profiles
     * Returns active and default profiles
     */
    @GetMapping("/auto-config/profiles")
    public Map<String, Object> getProfiles() {
        return Map.of(
            "activeProfiles", Arrays.asList(context.getEnvironment().getActiveProfiles()),
            "defaultProfiles", Arrays.asList(context.getEnvironment().getDefaultProfiles())
        );
    }
    
    /**
     * GET /auto-config/properties
     * Returns key application properties
     */
    @GetMapping("/auto-config/properties")
    public Map<String, String> getProperties() {
        return Map.of(
            "serverPort", context.getEnvironment().getProperty("server.port", "8080"),
            "applicationName", context.getEnvironment().getProperty("spring.application.name", "N/A"),
            "debugMode", context.getEnvironment().getProperty("debug", "false"),
            "autoConfigExclude", context.getEnvironment().getProperty(
                "spring.autoconfigure.exclude", "none")
        );
    }
    
    /**
     * GET /auto-config/info
     * Returns comprehensive auto-configuration information
     */
    @GetMapping("/auto-config/info")
    public Map<String, Object> getInfo() {
        String[] beanNames = context.getBeanDefinitionNames();
        
        Map<String, Long> beanCategories = Arrays.stream(beanNames)
            .collect(Collectors.groupingBy(
                name -> {
                    if (name.contains("AutoConfiguration")) return "Auto-Configuration";
                    if (name.startsWith("org.springframework")) return "Spring Framework";
                    return "User Defined";
                },
                Collectors.counting()
            ));
        
        return Map.of(
            "totalBeans", beanNames.length,
            "categories", beanCategories,
            "applicationContext", context.getClass().getSimpleName(),
            "startupDate", context.getStartupDate(),
            "running", context.isRunning(),
            "active", context.isActive()
        );
    }
}

/**
 * Example configuration demonstrating conditional beans
 * These work with auto-configuration
 */
@Configuration
class ConditionalBeansExample {
    
    /**
     * This bean will be created automatically if conditions are met
     * Auto-configuration respects user-defined beans
     */
    @Bean
    public CustomService customService() {
        return new CustomService("User-defined service - takes precedence");
    }
    
    static class CustomService {
        private final String description;
        
        public CustomService(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}

/**
 * Documentation for application.properties configuration
 * 
 * # Exclude auto-configurations
 * spring.autoconfigure.exclude=\
 *   org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,\
 *   org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
 * 
 * # Enable debug mode to see auto-configuration report
 * debug=true
 * 
 * # Logging level for auto-configuration
 * logging.level.org.springframework.boot.autoconfigure=DEBUG
 * 
 * # Actuator endpoints for auto-configuration insights
 * management.endpoints.web.exposure.include=conditions,beans,configprops
 * management.endpoint.conditions.enabled=true
 * management.endpoint.beans.enabled=true
 * 
 * # Common property to disable specific auto-configurations
 * spring.autoconfigure.exclude[0]=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
 * spring.autoconfigure.exclude[1]=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
 */

/**
 * Common Auto-Configuration Classes Reference:
 * 
 * Web:
 * - org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration
 * - org.springframework.boot.autoconfigure.web.reactive.WebFluxAutoConfiguration
 * - org.springframework.boot.autoconfigure.web.embedded.EmbeddedWebServerFactoryCustomizerAutoConfiguration
 * 
 * Data:
 * - org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
 * - org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
 * - org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration
 * - org.springframework.boot.autoconfigure.data.mongodb.MongoDataAutoConfiguration
 * 
 * Security:
 * - org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
 * - org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration
 * 
 * Messaging:
 * - org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration
 * - org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration
 * - org.springframework.boot.autoconfigure.jms.JmsAutoConfiguration
 * 
 * Templates:
 * - org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration
 * - org.springframework.boot.autoconfigure.freemarker.FreeMarkerAutoConfiguration
 * 
 * JSON:
 * - org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration
 * - org.springframework.boot.autoconfigure.gson.GsonAutoConfiguration
 * 
 * Caching:
 * - org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration
 * 
 * Actuator:
 * - org.springframework.boot.actuate.autoconfigure.web.server.ManagementContextAutoConfiguration
 * - org.springframework.boot.actuate.autoconfigure.endpoint.EndpointAutoConfiguration
 */
