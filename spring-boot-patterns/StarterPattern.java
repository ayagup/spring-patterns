package com.example.springboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Starter Pattern
 * 
 * Demonstrates creating custom Spring Boot starters with auto-configuration,
 * dependencies, and default configuration.
 * 
 * Key Concepts:
 * - Starter dependencies
 * - Auto-configuration
 * - Default properties
 * - Starter naming conventions
 * - META-INF/spring.factories
 * 
 * Use Cases:
 * - Library integration
 * - Common functionality
 * - Team standards
 * - Cross-project features
 * - Framework extensions
 */
@SpringBootApplication
public class StarterPattern {

    public static void main(String[] args) {
        SpringApplication.run(StarterPattern.class, args);
    }
}

/**
 * Starter auto-configuration
 */
@Configuration
class CustomStarterAutoConfiguration {

    @Bean
    public StarterService starterService(StarterProperties properties) {
        return new StarterService(properties);
    }
}

/**
 * Starter configuration properties
 */
@org.springframework.boot.context.properties.ConfigurationProperties(prefix = "custom.starter")
class StarterProperties {
    
    private boolean enabled = true;
    private String prefix = "STARTER";
    private int timeout = 5000;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getPrefix() { return prefix; }
    public void setPrefix(String prefix) { this.prefix = prefix; }
    public int getTimeout() { return timeout; }
    public void setTimeout(int timeout) { this.timeout = timeout; }
}

/**
 * Service provided by starter
 */
class StarterService {
    
    private final StarterProperties properties;

    public StarterService(StarterProperties properties) {
        this.properties = properties;
    }

    public Map<String, Object> getInfo() {
        return Map.of(
                "enabled", properties.isEnabled(),
                "prefix", properties.getPrefix(),
                "timeout", properties.getTimeout()
        );
    }

    public String processMessage(String message) {
        return properties.getPrefix() + ": " + message;
    }
}

/**
 * Service using starter
 */
@Service
class StarterDemoService {

    private final StarterService starterService;

    public StarterDemoService(StarterService starterService) {
        this.starterService = starterService;
    }

    public Map<String, Object> getStarterInfo() {
        return starterService.getInfo();
    }

    public String processWithStarter(String message) {
        return starterService.processMessage(message);
    }
}

/**
 * Controller demonstrating starter usage
 */
@RestController
class StarterController {

    private final StarterDemoService service;

    public StarterController(StarterDemoService service) {
        this.service = service;
    }

    @GetMapping("/starter/info")
    public Map<String, Object> getInfo() {
        return service.getStarterInfo();
    }

    @GetMapping("/starter/process")
    public Map<String, String> process() {
        return Map.of(
                "input", "Hello World",
                "output", service.processWithStarter("Hello World")
        );
    }
}

/**
 * Documentation:
 * 
 * Starter Structure:
 * 
 * my-spring-boot-starter/
 * ├── pom.xml
 * └── src/main/
 *     ├── java/
 *     │   └── com/example/starter/
 *     │       ├── MyAutoConfiguration.java
 *     │       ├── MyProperties.java
 *     │       └── MyService.java
 *     └── resources/
 *         └── META-INF/
 *             └── spring.factories
 * 
 * pom.xml (Starter):
 * <dependencies>
 *     <dependency>
 *         <groupId>org.springframework.boot</groupId>
 *         <artifactId>spring-boot-autoconfigure</artifactId>
 *     </dependency>
 *     <dependency>
 *         <groupId>org.springframework.boot</groupId>
 *         <artifactId>spring-boot-configuration-processor</artifactId>
 *         <optional>true</optional>
 *     </dependency>
 *     <!-- Add other dependencies -->
 * </dependencies>
 * 
 * spring.factories:
 * org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
 * com.example.starter.MyAutoConfiguration
 * 
 * Naming Conventions:
 * - Official: spring-boot-starter-{name}
 * - Third-party: {name}-spring-boot-starter
 * 
 * Auto-Configuration:
 * @Configuration
 * @ConditionalOnClass(MyService.class)
 * @EnableConfigurationProperties(MyProperties.class)
 * public class MyAutoConfiguration {
 *     
 *     @Bean
 *     @ConditionalOnMissingBean
 *     public MyService myService(MyProperties properties) {
 *         return new MyService(properties);
 *     }
 * }
 * 
 * Configuration Properties:
 * @ConfigurationProperties(prefix = "my.starter")
 * public class MyProperties {
 *     private boolean enabled = true;
 *     private String apiKey;
 *     // getters/setters
 * }
 * 
 * Usage (Consumer):
 * <dependency>
 *     <groupId>com.example</groupId>
 *     <artifactId>my-spring-boot-starter</artifactId>
 *     <version>1.0.0</version>
 * </dependency>
 * 
 * application.properties:
 * my.starter.enabled=true
 * my.starter.api-key=secret
 * 
 * Best Practices:
 * - Separate autoconfigure module
 * - Use conditional annotations
 * - Provide sensible defaults
 * - Document all properties
 * - Include configuration metadata
 * - Follow naming conventions
 * - Make configuration overridable
 * - Test with multiple scenarios
 * 
 * Configuration Metadata:
 * spring-configuration-metadata.json
 * {
 *   "properties": [
 *     {
 *       "name": "my.starter.enabled",
 *       "type": "java.lang.Boolean",
 *       "description": "Enable the starter",
 *       "defaultValue": true
 *     }
 *   ]
 * }
 */
