package com.example.springboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Conditional Bean Pattern
 * 
 * Demonstrates various @Conditional annotations for conditional bean registration
 * based on properties, classes, beans, and custom conditions.
 * 
 * Key Concepts:
 * - @ConditionalOnProperty
 * - @ConditionalOnClass/OnMissingClass
 * - @ConditionalOnBean/OnMissingBean
 * - @ConditionalOnExpression
 * - Custom conditions
 * 
 * Use Cases:
 * - Feature flags
 * - Environment-specific beans
 * - Optional dependencies
 * - Default bean provision
 * - Profile-based configuration
 */
@SpringBootApplication
public class ConditionalBeanPattern {

    public static void main(String[] args) {
        SpringApplication.run(ConditionalBeanPattern.class, args);
    }
}

/**
 * Conditional beans configuration
 */
@Configuration
class ConditionalBeansConfig {

    /**
     * Bean created only if property is true
     */
    @Bean
    @ConditionalOnProperty(name = "feature.email.enabled", havingValue = "true")
    public EmailService emailService() {
        return new EmailService("SMTP Email Service");
    }

    /**
     * Bean created only if email service is missing
     */
    @Bean
    @ConditionalOnMissingBean(EmailService.class)
    public EmailService defaultEmailService() {
        return new EmailService("Default Mock Email Service");
    }

    /**
     * Bean created only if specific class is on classpath
     */
    @Bean
    @ConditionalOnClass(name = "com.example.CustomLibrary")
    public AdvancedFeature advancedFeature() {
        return new AdvancedFeature();
    }

    /**
     * Bean created based on SpEL expression
     */
    @Bean
    @ConditionalOnExpression("${feature.cache.enabled:false} and ${feature.cache.type} == 'redis'")
    public CacheService redisCache() {
        return new CacheService("Redis");
    }

    /**
     * Bean created only if another bean exists
     */
    @Bean
    @ConditionalOnBean(CacheService.class)
    public CacheMonitor cacheMonitor(CacheService cacheService) {
        return new CacheMonitor(cacheService);
    }
}

/**
 * Email service
 */
class EmailService {
    private final String type;

    public EmailService(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void sendEmail(String to, String message) {
        System.out.println(type + ": Sending email to " + to + ": " + message);
    }
}

/**
 * Advanced feature (requires specific class)
 */
class AdvancedFeature {
    public Map<String, String> getInfo() {
        return Map.of(
                "name", "Advanced Feature",
                "status", "enabled"
        );
    }
}

/**
 * Cache service
 */
class CacheService {
    private final String cacheType;

    public CacheService(String cacheType) {
        this.cacheType = cacheType;
    }

    public String getCacheType() {
        return cacheType;
    }

    public void put(String key, Object value) {
        System.out.println(cacheType + " cache: Putting " + key);
    }
}

/**
 * Cache monitor (depends on cache service)
 */
class CacheMonitor {
    private final CacheService cacheService;

    public CacheMonitor(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    public Map<String, String> getStatus() {
        return Map.of(
                "monitoring", cacheService.getCacheType() + " cache",
                "status", "active"
        );
    }
}

/**
 * Service providing conditional bean information
 */
@Service
class ConditionalBeanService {

    private final List<Object> conditionalBeans = new ArrayList<>();

    public ConditionalBeanService(org.springframework.context.ApplicationContext context) {
        // Check which conditional beans are present
        if (context.containsBean("emailService")) {
            conditionalBeans.add(context.getBean("emailService"));
        }
        if (context.containsBean("defaultEmailService")) {
            conditionalBeans.add(context.getBean("defaultEmailService"));
        }
        if (context.containsBean("advancedFeature")) {
            conditionalBeans.add(context.getBean("advancedFeature"));
        }
        if (context.containsBean("redisCache")) {
            conditionalBeans.add(context.getBean("redisCache"));
        }
        if (context.containsBean("cacheMonitor")) {
            conditionalBeans.add(context.getBean("cacheMonitor"));
        }
    }

    public Map<String, Object> getConditionalBeansInfo() {
        return Map.of(
                "totalConditionalBeans", conditionalBeans.size(),
                "beanCount", conditionalBeans.size()
        );
    }
}

/**
 * Controller exposing conditional bean information
 */
@RestController
class ConditionalBeanController {

    private final ConditionalBeanService service;

    public ConditionalBeanController(ConditionalBeanService service) {
        this.service = service;
    }

    @GetMapping("/conditional/info")
    public Map<String, Object> getInfo() {
        return service.getConditionalBeansInfo();
    }
}

/**
 * Documentation:
 * 
 * Conditional Annotations:
 * 
 * @ConditionalOnProperty:
 * @Bean
 * @ConditionalOnProperty(
 *     name = "feature.enabled",
 *     havingValue = "true",
 *     matchIfMissing = false
 * )
 * public FeatureService feature() { }
 * 
 * @ConditionalOnClass:
 * @Bean
 * @ConditionalOnClass(DataSource.class)
 * public DataSourceMonitor monitor() { }
 * 
 * @ConditionalOnMissingClass:
 * @Bean
 * @ConditionalOnMissingClass("com.example.CustomImpl")
 * public DefaultImpl defaultImpl() { }
 * 
 * @ConditionalOnBean:
 * @Bean
 * @ConditionalOnBean(DataSource.class)
 * public Repository repository(DataSource ds) { }
 * 
 * @ConditionalOnMissingBean:
 * @Bean
 * @ConditionalOnMissingBean
 * public Service defaultService() { }
 * 
 * @ConditionalOnExpression:
 * @Bean
 * @ConditionalOnExpression("'${spring.profiles.active}' == 'prod'")
 * public ProdService prodService() { }
 * 
 * @ConditionalOnWebApplication:
 * @Bean
 * @ConditionalOnWebApplication
 * public WebController controller() { }
 * 
 * @ConditionalOnNotWebApplication:
 * @Bean
 * @ConditionalOnNotWebApplication
 * public BatchService batch() { }
 * 
 * Custom Condition:
 * public class CustomCondition implements Condition {
 *     @Override
 *     public boolean matches(ConditionContext context,
 *                           AnnotatedTypeMetadata metadata) {
 *         return context.getEnvironment()
 *             .getProperty("custom.enabled", Boolean.class, false);
 *     }
 * }
 * 
 * @Bean
 * @Conditional(CustomCondition.class)
 * public CustomService custom() { }
 * 
 * application.properties:
 * feature.email.enabled=true
 * feature.cache.enabled=true
 * feature.cache.type=redis
 * 
 * Best Practices:
 * - Use specific conditionals over generic
 * - Provide defaults with @ConditionalOnMissingBean
 * - Document conditional behavior
 * - Test different configurations
 * - Use properties for feature flags
 * - Keep conditions simple
 */
