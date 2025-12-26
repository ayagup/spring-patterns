package com.example.springboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

/**
 * Bootstrap Context Pattern
 * 
 * Demonstrates using bootstrap context for early initialization,
 * configuration loading, and application context preparation.
 * 
 * Key Concepts:
 * - Bootstrap ApplicationContext
 * - ApplicationContextInitializer
 * - Early configuration loading
 * - Parent context setup
 * - Cloud Config bootstrap
 * 
 * Use Cases:
 * - Cloud Config integration
 * - Vault secrets loading
 * - Early bean registration
 * - Custom property sources
 * - Multi-context setup
 */
@SpringBootApplication
public class BootstrapContextPattern {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(BootstrapContextPattern.class);
        
        // Add initializer
        app.addInitializers(new CustomApplicationContextInitializer());
        
        app.run(args);
    }
}

/**
 * Custom application context initializer
 */
class CustomApplicationContextInitializer 
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        
        System.out.println("=== Bootstrap Context Initialization ===");
        System.out.println("Active profiles: " + 
            String.join(", ", environment.getActiveProfiles()));
        System.out.println("Application name: " + 
            environment.getProperty("spring.application.name", "default"));
        
        // Register custom property source
        // PropertySource customSource = new CustomPropertySource();
        // environment.getPropertySources().addFirst(customSource);
        
        System.out.println("=== Bootstrap initialization complete ===");
    }
}

/**
 * Bootstrap configuration
 */
@Configuration
class BootstrapConfig {
    
    public BootstrapConfig() {
        System.out.println("BootstrapConfig initialized");
    }
}

/**
 * Controller providing bootstrap information
 */
@Controller
class BootstrapController {

    private final ConfigurableApplicationContext applicationContext;

    public BootstrapController(ConfigurableApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @GetMapping("/bootstrap/info")
    @ResponseBody
    public Map<String, Object> getBootstrapInfo() {
        return Map.of(
                "contextId", applicationContext.getId(),
                "displayName", applicationContext.getDisplayName(),
                "active", applicationContext.isActive(),
                "activeProfiles", applicationContext.getEnvironment().getActiveProfiles(),
                "bootstrapEnabled", true
        );
    }

    @GetMapping("/bootstrap/properties")
    @ResponseBody
    public Map<String, String> getBootstrapProperties() {
        ConfigurableEnvironment env = applicationContext.getEnvironment();
        return Map.of(
                "applicationName", env.getProperty("spring.application.name", "unknown"),
                "configName", env.getProperty("spring.config.name", "application"),
                "configLocation", env.getProperty("spring.config.location", "default")
        );
    }
}

/**
 * Documentation:
 * 
 * Bootstrap Context (Legacy Spring Cloud):
 * bootstrap.yml or bootstrap.properties loaded before application.yml
 * 
 * bootstrap.yml:
 * spring:
 *   application:
 *     name: my-application
 *   cloud:
 *     config:
 *       uri: http://config-server:8888
 *       fail-fast: true
 * 
 * Modern Approach (Spring Boot 2.4+):
 * Use spring.config.import instead
 * 
 * application.properties:
 * spring.config.import=optional:configserver:http://config-server:8888
 * spring.config.import=vault://secret/myapp
 * 
 * ApplicationContextInitializer:
 * public class MyInitializer 
 *         implements ApplicationContextInitializer<ConfigurableApplicationContext> {
 *     
 *     @Override
 *     public void initialize(ConfigurableApplicationContext ctx) {
 *         // Early initialization
 *         ConfigurableEnvironment env = ctx.getEnvironment();
 *         // Add property sources
 *         // Register beans
 *     }
 * }
 * 
 * Registration Methods:
 * 
 * 1. SpringApplication:
 *    SpringApplication app = new SpringApplication(MyApp.class);
 *    app.addInitializers(new MyInitializer());
 * 
 * 2. spring.factories:
 *    org.springframework.context.ApplicationContextInitializer=\
 *    com.example.MyInitializer
 * 
 * 3. Application properties:
 *    context.initializer.classes=com.example.MyInitializer
 * 
 * Bootstrap vs Application Context:
 * - Bootstrap: Parent context, loads first
 * - Application: Child context, main application
 * 
 * Use Cases:
 * 
 * 1. Cloud Config:
 *    spring.cloud.config.uri=http://config-server
 *    spring.cloud.config.name=myapp
 *    spring.cloud.config.profile=production
 * 
 * 2. Vault Integration:
 *    spring.cloud.vault.uri=https://vault:8200
 *    spring.cloud.vault.token=${VAULT_TOKEN}
 *    spring.cloud.vault.generic.backend=secret
 * 
 * 3. Consul Config:
 *    spring.cloud.consul.config.enabled=true
 *    spring.cloud.consul.config.prefix=config
 * 
 * Migration to spring.config.import:
 * 
 * Old (bootstrap.yml):
 * spring:
 *   cloud:
 *     config:
 *       uri: http://config-server
 * 
 * New (application.properties):
 * spring.config.import=configserver:http://config-server
 * 
 * Benefits:
 * - Simpler configuration
 * - Better error messages
 * - Consistent with Spring Boot
 * - No extra bootstrap context
 * 
 * Custom Property Source:
 * public class MyInitializer 
 *         implements ApplicationContextInitializer {
 *     @Override
 *     public void initialize(ConfigurableApplicationContext ctx) {
 *         ConfigurableEnvironment env = ctx.getEnvironment();
 *         
 *         Map<String, Object> props = new HashMap<>();
 *         props.put("custom.property", "value");
 *         
 *         PropertySource ps = new MapPropertySource("custom", props);
 *         env.getPropertySources().addFirst(ps);
 *     }
 * }
 * 
 * Best Practices:
 * - Use spring.config.import for new apps
 * - Keep bootstrap minimal
 * - Document initialization order
 * - Handle failures gracefully
 * - Test bootstrap scenarios
 * - Use appropriate property sources
 */
