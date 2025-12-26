package com.example.springboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Auto-Configuration Pattern
 * 
 * Demonstrates creating custom auto-configuration classes that conditionally
 * configure beans based on classpath, properties, and existing beans.
 * 
 * Key Concepts:
 * - @Configuration with conditions
 * - @ConditionalOnClass
 * - @ConditionalOnProperty
 * - @ConditionalOnMissingBean
 * - Auto-configuration ordering
 * 
 * Use Cases:
 * - Custom starters
 * - Library auto-configuration
 * - Conditional bean creation
 * - Framework integration
 * - Default configurations
 */
@SpringBootApplication
public class AutoConfigurationPattern {

    public static void main(String[] args) {
        SpringApplication.run(AutoConfigurationPattern.class, args);
    }
}

/**
 * Custom auto-configuration class
 */
@Configuration
@ConditionalOnClass(name = "com.example.CustomService")
@EnableConfigurationProperties(CustomProperties.class)
class CustomAutoConfiguration {

    /**
     * Configure bean only if not already defined
     */
    @Bean
    @ConditionalOnMissingBean
    public CustomService customService(CustomProperties properties) {
        CustomService service = new CustomService();
        service.setEnabled(properties.isEnabled());
        service.setMessage(properties.getMessage());
        return service;
    }

    /**
     * Configure bean based on property
     */
    @Bean
    @ConditionalOnProperty(name = "custom.feature.enabled", havingValue = "true")
    public FeatureService featureService() {
        return new FeatureService();
    }
}

/**
 * Configuration properties
 */
@org.springframework.boot.context.properties.ConfigurationProperties(prefix = "custom")
class CustomProperties {
    
    private boolean enabled = true;
    private String message = "Default message";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

/**
 * Custom service configured by auto-configuration
 */
class CustomService {
    
    private boolean enabled;
    private String message;

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, Object> getInfo() {
        return Map.of(
                "enabled", enabled,
                "message", message,
                "autoConfigured", true
        );
    }
}

/**
 * Feature service conditionally configured
 */
class FeatureService {
    
    public Map<String, String> getFeatureInfo() {
        return Map.of(
                "feature", "Custom Feature",
                "status", "enabled"
        );
    }
}

/**
 * Service exposing auto-configuration info
 */
@Service
class AutoConfigService {

    private final CustomService customService;
    private final CustomProperties properties;

    public AutoConfigService(CustomService customService, 
                            CustomProperties properties) {
        this.customService = customService;
        this.properties = properties;
    }

    public Map<String, Object> getAutoConfigInfo() {
        return Map.of(
                "customService", customService.getInfo(),
                "properties", Map.of(
                        "enabled", properties.isEnabled(),
                        "message", properties.getMessage()
                )
        );
    }
}

/**
 * Controller exposing auto-configuration
 */
@RestController
class AutoConfigController {

    private final AutoConfigService autoConfigService;

    public AutoConfigController(AutoConfigService autoConfigService) {
        this.autoConfigService = autoConfigService;
    }

    @GetMapping("/autoconfig/info")
    public Map<String, Object> getInfo() {
        return autoConfigService.getAutoConfigInfo();
    }
}

/**
 * Documentation:
 * 
 * Auto-Configuration Structure:
 * 
 * @Configuration
 * @ConditionalOnClass(DataSource.class)
 * @EnableConfigurationProperties(DataSourceProperties.class)
 * public class DataSourceAutoConfiguration {
 *     
 *     @Bean
 *     @ConditionalOnMissingBean
 *     public DataSource dataSource(DataSourceProperties props) {
 *         return new HikariDataSource(...);
 *     }
 * }
 * 
 * Conditional Annotations:
 * 
 * @ConditionalOnClass - Class exists on classpath
 * @ConditionalOnMissingClass - Class not on classpath
 * @ConditionalOnBean - Bean exists in context
 * @ConditionalOnMissingBean - Bean not in context
 * @ConditionalOnProperty - Property has specific value
 * @ConditionalOnResource - Resource exists
 * @ConditionalOnWebApplication - Web application
 * @ConditionalOnNotWebApplication - Not web application
 * @ConditionalOnExpression - SpEL expression true
 * 
 * spring.factories (META-INF):
 * org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
 * com.example.CustomAutoConfiguration
 * 
 * Auto-Configuration Ordering:
 * @AutoConfigureAfter(DataSourceAutoConfiguration.class)
 * @AutoConfigureBefore(WebMvcAutoConfiguration.class)
 * @AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
 * 
 * Exclude Auto-Configuration:
 * @SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
 * 
 * Or in application.properties:
 * spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
 * 
 * Best Practices:
 * - Use @ConditionalOnMissingBean for defaults
 * - Provide configuration properties
 * - Document auto-configuration behavior
 * - Use appropriate ordering
 * - Make configuration overridable
 * - Test with different conditions
 * 
 * Debug Auto-Configuration:
 * --debug or spring.devtools.restart.enabled=true
 * 
 * View auto-configuration report:
 * /actuator/conditions
 */
