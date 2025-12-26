package com.example.springboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

/**
 * Enable/Exclude Auto-Configuration Pattern
 * 
 * Demonstrates controlling which auto-configurations are enabled or excluded
 * in Spring Boot applications.
 * 
 * Key Concepts:
 * - @EnableAutoConfiguration
 * - exclude attribute
 * - excludeName attribute
 * - spring.autoconfigure.exclude property
 * - Auto-configuration control
 * 
 * Use Cases:
 * - Disable unwanted features
 * - Custom implementations
 * - Testing scenarios
 * - Performance optimization
 * - Conflict resolution
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class EnableExcludeAutoConfigPattern {

    public static void main(String[] args) {
        SpringApplication.run(EnableExcludeAutoConfigPattern.class, args);
    }
}

/**
 * Alternative: Using @EnableAutoConfiguration
 */
@Configuration
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
class AlternativeConfig {
    // Configuration
}

/**
 * Controller demonstrating auto-configuration exclusion
 */
@Controller
class AutoConfigController {

    @GetMapping("/autoconfig/excluded")
    @ResponseBody
    public Map<String, Object> getExcludedConfigs() {
        return Map.of(
                "excluded", new String[]{
                        "DataSourceAutoConfiguration",
                        "HibernateJpaAutoConfiguration"
                },
                "reason", "Custom database configuration",
                "method", "@SpringBootApplication(exclude = {...})"
        );
    }

    @GetMapping("/autoconfig/info")
    @ResponseBody
    public Map<String, String> getInfo() {
        return Map.of(
                "autoConfigEnabled", "true",
                "exclusionsApplied", "true",
                "customConfig", "active"
        );
    }
}

/**
 * Documentation:
 * 
 * Exclude via @SpringBootApplication:
 * @SpringBootApplication(exclude = {
 *     DataSourceAutoConfiguration.class,
 *     HibernateJpaAutoConfiguration.class
 * })
 * 
 * Exclude by name:
 * @SpringBootApplication(excludeName = {
 *     "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration"
 * })
 * 
 * Exclude via @EnableAutoConfiguration:
 * @Configuration
 * @EnableAutoConfiguration(exclude = {
 *     DataSourceAutoConfiguration.class
 * })
 * 
 * Exclude via application.properties:
 * spring.autoconfigure.exclude=\
 *   org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,\
 *   org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
 * 
 * View Auto-Configuration Report:
 * --debug flag or debug=true in properties
 * 
 * Actuator Endpoint:
 * /actuator/conditions
 * 
 * Common Exclusions:
 * 
 * 1. Database:
 *    - DataSourceAutoConfiguration
 *    - HibernateJpaAutoConfiguration
 *    - JdbcTemplateAutoConfiguration
 * 
 * 2. Security:
 *    - SecurityAutoConfiguration
 *    - SecurityFilterAutoConfiguration
 * 
 * 3. Web:
 *    - WebMvcAutoConfiguration
 *    - ErrorMvcAutoConfiguration
 * 
 * 4. Messaging:
 *    - JmsAutoConfiguration
 *    - KafkaAutoConfiguration
 *    - RabbitAutoConfiguration
 * 
 * 5. Cache:
 *    - CacheAutoConfiguration
 *    - RedisCacheConfiguration
 * 
 * Testing Exclusions:
 * @SpringBootTest(properties = {
 *     "spring.autoconfigure.exclude=" +
 *     "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration"
 * })
 * 
 * Best Practices:
 * - Document why exclusions are needed
 * - Provide custom implementations
 * - Test with exclusions
 * - Review periodically
 * - Use specific exclusions
 * - Consider alternatives first
 */
