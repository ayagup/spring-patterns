package com.example.propertysource;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.client.ConfigClientProperties;
import org.springframework.cloud.config.client.ConfigServicePropertySourceLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Cloud Config Property Source Pattern
 * 
 * Demonstrates integrating with Spring Cloud Config Server for
 * centralized configuration management across distributed systems.
 * 
 * Key Concepts:
 * - Spring Cloud Config Client
 * - ConfigServicePropertySourceLocator
 * - Centralized configuration
 * - Dynamic refresh
 * - Environment-specific configuration
 * - Git-backed configuration
 * 
 * Use Cases:
 * - Microservices architecture
 * - Multi-environment deployments
 * - Centralized secret management
 * - Configuration versioning
 * - Dynamic property updates
 */
@SpringBootApplication
public class CloudConfigPropertySourcePattern {

    public static void main(String[] args) {
        SpringApplication.run(CloudConfigPropertySourcePattern.class, args);
    }
}

/**
 * Cloud Config configuration
 * 
 * bootstrap.properties/bootstrap.yml typically contains:
 * spring.application.name=myapp
 * spring.cloud.config.uri=http://config-server:8888
 * spring.cloud.config.profile=dev
 * spring.cloud.config.label=main
 */
@Configuration
class CloudConfigConfiguration {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /**
     * Simulated Cloud Config client configuration
     */
    @Bean
    public ConfigClientProperties configClientProperties() {
        ConfigClientProperties props = new ConfigClientProperties(null);
        props.setUri(new String[]{"http://localhost:8888"});
        props.setName("myapp");
        props.setProfile("dev");
        props.setLabel("main");
        return props;
    }
}

/**
 * Service to interact with Cloud Config
 */
@org.springframework.stereotype.Service
class CloudConfigService {

    private final ConfigurableEnvironment environment;
    private final RestTemplate restTemplate;

    public CloudConfigService(ConfigurableEnvironment environment, RestTemplate restTemplate) {
        this.environment = environment;
        this.restTemplate = restTemplate;
    }

    /**
     * Get property from Cloud Config
     */
    public String getCloudProperty(String key) {
        return environment.getProperty(key);
    }

    /**
     * Get all properties from Cloud Config source
     */
    public Map<String, Object> getCloudConfigProperties() {
        Map<String, Object> properties = new HashMap<>();
        
        org.springframework.core.env.PropertySource<?> cloudSource = 
                environment.getPropertySources().stream()
                        .filter(ps -> ps.getName().contains("configService") || 
                                     ps.getName().contains("cloudConfig"))
                        .findFirst()
                        .orElse(null);
        
        if (cloudSource instanceof org.springframework.core.env.EnumerablePropertySource) {
            org.springframework.core.env.EnumerablePropertySource<?> eps = 
                    (org.springframework.core.env.EnumerablePropertySource<?>) cloudSource;
            
            for (String propertyName : eps.getPropertyNames()) {
                properties.put(propertyName, eps.getProperty(propertyName));
            }
        }
        
        return properties;
    }

    /**
     * Refresh configuration from Cloud Config Server
     * In real application, this would trigger @RefreshScope beans to reload
     */
    public void refreshConfiguration() {
        // Simulated refresh - in real app would call /actuator/refresh endpoint
        System.out.println("Configuration refresh requested");
    }

    /**
     * Get configuration from specific profile
     */
    public Map<String, Object> getProfileConfiguration(String profile) {
        Map<String, Object> config = new HashMap<>();
        
        // Simulated profile-specific config fetch
        config.put("profile", profile);
        config.put("source", "cloud-config");
        config.put("timestamp", System.currentTimeMillis());
        
        return config;
    }
}

/**
 * Controller to expose Cloud Config properties
 */
@RestController
@RequestMapping("/api/cloudconfig")
class CloudConfigController {

    private final CloudConfigService cloudConfigService;

    public CloudConfigController(CloudConfigService cloudConfigService) {
        this.cloudConfigService = cloudConfigService;
    }

    @GetMapping("/property")
    public Map<String, String> getProperty(String key) {
        return Map.of(
                "key", key,
                "value", cloudConfigService.getCloudProperty(key) != null ? 
                        cloudConfigService.getCloudProperty(key) : "Not found",
                "source", "cloud-config"
        );
    }

    @GetMapping("/properties")
    public Map<String, Object> getAllCloudProperties() {
        return cloudConfigService.getCloudConfigProperties();
    }

    @GetMapping("/refresh")
    public Map<String, String> refreshConfig() {
        cloudConfigService.refreshConfiguration();
        return Map.of("status", "Configuration refresh triggered");
    }

    @GetMapping("/profile")
    public Map<String, Object> getProfileConfig(String profile) {
        return cloudConfigService.getProfileConfiguration(profile);
    }
}

/**
 * Documentation:
 * 
 * Spring Cloud Config Architecture:
 * - Config Server: Centralized configuration server
 * - Config Client: Application that fetches configuration
 * - Backend: Git, SVN, File System, Vault
 * - Environment: dev, test, staging, prod
 * 
 * Dependencies:
 * <dependency>
 *     <groupId>org.springframework.cloud</groupId>
 *     <artifactId>spring-cloud-starter-config</artifactId>
 * </dependency>
 * 
 * Bootstrap Configuration (bootstrap.yml):
 * spring:
 *   application:
 *     name: myapp
 *   cloud:
 *     config:
 *       uri: http://config-server:8888
 *       profile: dev
 *       label: main
 *       fail-fast: true
 *       retry:
 *         max-attempts: 6
 *         initial-interval: 1000
 * 
 * Config Server Setup:
 * @EnableConfigServer on main class
 * spring.cloud.config.server.git.uri=https://github.com/myorg/config-repo
 * 
 * Git Repository Structure:
 * config-repo/
 *   application.yml           # Default for all apps
 *   application-dev.yml       # Dev profile for all apps
 *   myapp.yml                 # Specific to myapp
 *   myapp-dev.yml             # myapp dev profile
 *   myapp-prod.yml            # myapp prod profile
 * 
 * Property Resolution Order:
 * 1. /{application}-{profile}.yml in Git
 * 2. /{application}.yml in Git
 * 3. /application-{profile}.yml in Git
 * 4. /application.yml in Git
 * 5. Local application.yml
 * 6. Local application-{profile}.yml
 * 
 * Dynamic Refresh with @RefreshScope:
 * @RefreshScope
 * @Component
 * class MyConfig {
 *     @Value("${dynamic.property}")
 *     private String dynamicProperty;
 * }
 * 
 * Trigger refresh: POST /actuator/refresh
 * 
 * Spring Cloud Bus for Broadcast Refresh:
 * - POST /actuator/bus-refresh triggers refresh on all instances
 * - Requires RabbitMQ or Kafka
 * - Dependency: spring-cloud-starter-bus-amqp or spring-cloud-starter-bus-kafka
 * 
 * Encryption and Decryption:
 * - Config Server can decrypt {cipher} prefixed values
 * - Uses encrypt/decrypt endpoints
 * - Requires JCE unlimited strength
 * 
 * Example encrypted property:
 * db.password: {cipher}AQAEncryptedValueHere
 * 
 * Health Check:
 * - Actuator exposes /actuator/env to view all property sources
 * - /actuator/configprops shows @ConfigurationProperties
 * 
 * Fail-Fast:
 * spring.cloud.config.fail-fast=true
 * - Application fails to start if can't connect to Config Server
 * - Recommended for production
 * 
 * Retry Configuration:
 * spring.cloud.config.retry.max-attempts=6
 * spring.cloud.config.retry.initial-interval=1000
 * spring.cloud.config.retry.multiplier=1.1
 * spring.cloud.config.retry.max-interval=2000
 * 
 * Multiple Config Server URLs:
 * spring.cloud.config.uri=http://server1:8888,http://server2:8888
 * - Failover support
 * - Client tries each URL in order
 * 
 * Discovery Integration (Eureka):
 * spring.cloud.config.discovery.enabled=true
 * spring.cloud.config.discovery.service-id=config-server
 * - Config Server registered in Eureka
 * - Client discovers Config Server dynamically
 * 
 * Authentication:
 * spring.cloud.config.username=user
 * spring.cloud.config.password=password
 * - Basic authentication to Config Server
 * 
 * Profiles:
 * spring.cloud.config.profile=dev,mysql
 * - Multiple profiles supported
 * - Comma-separated list
 * 
 * Label (Git Branch/Tag):
 * spring.cloud.config.label=feature-branch
 * - Defaults to "main" or "master"
 * - Can point to specific branch or tag
 * 
 * Best Practices:
 * - Use Git for version control
 * - Encrypt sensitive properties
 * - Use fail-fast in production
 * - Configure retry with backoff
 * - Use @RefreshScope for dynamic properties
 * - Monitor Config Server health
 * - Use Spring Cloud Bus for distributed refresh
 * - Separate configs by environment
 * - Use profiles for environment-specific config
 * - Keep local fallback configuration
 * 
 * Common Issues:
 * - Config Server unreachable: Check URI and network
 * - Properties not refreshing: Ensure @RefreshScope and /actuator/refresh
 * - Wrong profile: Verify spring.cloud.config.profile
 * - Git authentication: Configure credentials properly
 * - Bootstrap not loading: Ensure bootstrap.yml in classpath
 * 
 * Testing:
 * @SpringBootTest
 * @TestPropertySource(properties = {
 *     "spring.cloud.config.enabled=false"
 * })
 * - Disable Cloud Config for unit tests
 * 
 * Alternatives:
 * - Consul Config
 * - Vault Config
 * - Kubernetes ConfigMaps
 * - AWS Parameter Store
 */
