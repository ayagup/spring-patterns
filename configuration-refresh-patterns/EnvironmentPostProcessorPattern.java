package com.example.demo.patterns.configuration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Environment Post Processor Pattern - Modify Environment Before Application Starts
 * 
 * Purpose:
 * - Customize Spring Environment before ApplicationContext is created
 * - Add property sources early in bootstrap phase
 * - Decrypt encrypted properties
 * - Fetch remote configuration
 * - Set up environment-specific properties
 * - Validate required properties exist
 * 
 * Use Cases:
 * - Fetch encryption keys from AWS KMS/Azure Key Vault before startup
 * - Decrypt encrypted application properties
 * - Load secrets from HashiCorp Vault
 * - Add cloud provider metadata as properties
 * - Set up multi-tenant configuration
 * - Validate required environment variables present
 * - Add computed properties based on environment
 * - Integrate with service discovery for config locations
 * - Load properties from external HTTP endpoints
 * - Transform property values before application starts
 * 
 * Key Concepts:
 * - EnvironmentPostProcessor: Runs before ApplicationContext refresh
 * - Execution Order: Controlled via @Order or Ordered interface
 * - Spring Factories: Register via META-INF/spring.factories
 * - Early Bootstrap: Runs before @Configuration classes
 * - Environment Mutation: Add/modify property sources
 * - No Bean Context: Cannot inject beans (too early)
 * - Multiple Processors: Chain multiple post processors
 * 
 * Implementation Patterns:
 * 1. Basic EnvironmentPostProcessor implementation
 * 2. Decrypt encrypted properties
 * 3. Fetch secrets from external vault
 * 4. Add cloud metadata properties
 * 5. Environment-specific property setup
 * 6. Required property validation
 * 7. Computed property addition
 * 8. Multiple post processor chaining
 * 9. Conditional property source loading
 * 10. Remote configuration fetching
 * 11. Property value transformation
 * 12. Logging and debugging post processors
 * 
 * Dependencies:
 * <dependency>
 *     <groupId>org.springframework.boot</groupId>
 *     <artifactId>spring-boot-starter</artifactId>
 * </dependency>
 * 
 * Registration (META-INF/spring.factories):
 * org.springframework.boot.env.EnvironmentPostProcessor=\
 *   com.example.demo.patterns.configuration.EnvironmentPostProcessorPattern$CustomEnvironmentPostProcessor,\
 *   com.example.demo.patterns.configuration.EnvironmentPostProcessorPattern$DecryptionPostProcessor
 * 
 * Warnings:
 * - Runs before Spring context initialized (no bean injection)
 * - Exceptions here prevent application startup
 * - Keep processing lightweight (affects startup time)
 * - Cannot use @Autowired or other bean features
 * - Use System.out for logging (logger beans not available)
 * - Handle failures gracefully
 * - Be cautious with external service calls (may fail)
 * - Test thoroughly as errors are hard to debug
 * - Document processor execution order
 * - Consider impact on startup performance
 * 
 * Best Practices:
 * - Use @Order to control execution sequence
 * - Implement fail-fast validation for critical properties
 * - Provide fallback for optional properties
 * - Log all modifications for debugging
 * - Keep external calls minimal
 * - Use timeouts for remote fetches
 * - Cache fetched values when possible
 * - Document all added properties
 * - Test with various environment configurations
 * - Use System properties for debugging flags
 */
@SpringBootApplication
public class EnvironmentPostProcessorPattern {

    public static void main(String[] args) {
        SpringApplication.run(EnvironmentPostProcessorPattern.class, args);
    }

    // ============================================
    // Example 1: Basic Environment Post Processor
    // ============================================
    
    /**
     * Add custom properties before application starts.
     * Registered via META-INF/spring.factories
     */
    public static class CustomEnvironmentPostProcessor implements EnvironmentPostProcessor {
        
        @Override
        public void postProcessEnvironment(ConfigurableEnvironment environment, 
                                          SpringApplication application) {
            System.out.println("=== CustomEnvironmentPostProcessor Executing ===");
            
            Map<String, Object> customProperties = new HashMap<>();
            
            // Add timestamp when processor ran
            customProperties.put("app.processor.execution-time", LocalDateTime.now().toString());
            
            // Add application metadata
            customProperties.put("app.processor.name", "CustomEnvironmentPostProcessor");
            customProperties.put("app.processor.version", "1.0");
            
            // Add computed properties
            String environment_name = String.join(",", environment.getActiveProfiles());
            if (environment_name.isEmpty()) {
                environment_name = "default";
            }
            customProperties.put("app.active-profiles", environment_name);
            
            // Create and add property source
            MapPropertySource propertySource = new MapPropertySource(
                "customPostProcessorProperties",
                customProperties
            );
            
            environment.getPropertySources().addLast(propertySource);
            
            System.out.println("  Added " + customProperties.size() + " custom properties");
        }
    }

    // ============================================
    // Example 2: Decryption Post Processor
    // ============================================
    
    /**
     * Decrypt encrypted properties before application uses them.
     */
    public static class DecryptionPostProcessor implements EnvironmentPostProcessor {
        
        private static final String ENCRYPTED_PREFIX = "{encrypted}";
        
        @Override
        public void postProcessEnvironment(ConfigurableEnvironment environment, 
                                          SpringApplication application) {
            System.out.println("=== DecryptionPostProcessor Executing ===");
            
            Map<String, Object> decryptedProperties = new HashMap<>();
            
            // Find encrypted properties
            MutablePropertySources sources = environment.getPropertySources();
            sources.forEach(source -> {
                if (source instanceof MapPropertySource) {
                    MapPropertySource mapSource = (MapPropertySource) source;
                    Map<String, Object> sourceMap = mapSource.getSource();
                    
                    sourceMap.forEach((key, value) -> {
                        if (value instanceof String) {
                            String strValue = (String) value;
                            if (strValue.startsWith(ENCRYPTED_PREFIX)) {
                                String encrypted = strValue.substring(ENCRYPTED_PREFIX.length());
                                String decrypted = decrypt(encrypted);
                                decryptedProperties.put(key, decrypted);
                                
                                System.out.println("  Decrypted property: " + key);
                            }
                        }
                    });
                }
            });
            
            // Add decrypted properties with higher priority
            if (!decryptedProperties.isEmpty()) {
                MapPropertySource decryptedSource = new MapPropertySource(
                    "decryptedProperties",
                    decryptedProperties
                );
                environment.getPropertySources().addFirst(decryptedSource);
                
                System.out.println("  Decrypted " + decryptedProperties.size() + " properties");
            }
        }
        
        private String decrypt(String encrypted) {
            // Simple demonstration - in real implementation:
            // - Use AWS KMS, Azure Key Vault, or HashiCorp Vault
            // - Proper AES/RSA encryption
            // - Key management
            
            // Simulated decryption
            return "DECRYPTED[" + encrypted + "]";
        }
    }

    // ============================================
    // Example 3: Secrets Vault Post Processor
    // ============================================
    
    /**
     * Fetch secrets from external vault before startup.
     */
    public static class SecretsVaultPostProcessor implements EnvironmentPostProcessor {
        
        @Override
        public void postProcessEnvironment(ConfigurableEnvironment environment, 
                                          SpringApplication application) {
            System.out.println("=== SecretsVaultPostProcessor Executing ===");
            
            // Check if vault integration enabled
            String vaultEnabled = environment.getProperty("app.vault.enabled", "false");
            if (!"true".equals(vaultEnabled)) {
                System.out.println("  Vault integration disabled, skipping");
                return;
            }
            
            // Fetch secrets from vault
            Map<String, Object> secrets = fetchSecretsFromVault(environment);
            
            if (!secrets.isEmpty()) {
                MapPropertySource secretsSource = new MapPropertySource(
                    "vaultSecrets",
                    secrets
                );
                environment.getPropertySources().addFirst(secretsSource);
                
                System.out.println("  Loaded " + secrets.size() + " secrets from vault");
            }
        }
        
        private Map<String, Object> fetchSecretsFromVault(ConfigurableEnvironment environment) {
            Map<String, Object> secrets = new HashMap<>();
            
            // In real implementation:
            // - Connect to HashiCorp Vault
            // - Use AWS Secrets Manager
            // - Use Azure Key Vault
            // - Authenticate with proper credentials
            // - Fetch secrets by path
            
            // Simulated vault response
            secrets.put("app.database.password", "vault_db_password_123");
            secrets.put("app.api.secret-key", "vault_api_key_xyz");
            secrets.put("app.encryption.key", "vault_encryption_key_abc");
            
            return secrets;
        }
    }

    // ============================================
    // Example 4: Cloud Metadata Post Processor
    // ============================================
    
    /**
     * Add cloud provider metadata as properties.
     */
    public static class CloudMetadataPostProcessor implements EnvironmentPostProcessor {
        
        @Override
        public void postProcessEnvironment(ConfigurableEnvironment environment, 
                                          SpringApplication application) {
            System.out.println("=== CloudMetadataPostProcessor Executing ===");
            
            Map<String, Object> metadata = fetchCloudMetadata();
            
            if (!metadata.isEmpty()) {
                MapPropertySource metadataSource = new MapPropertySource(
                    "cloudMetadata",
                    metadata
                );
                environment.getPropertySources().addLast(metadataSource);
                
                System.out.println("  Added " + metadata.size() + " cloud metadata properties");
            }
        }
        
        private Map<String, Object> fetchCloudMetadata() {
            Map<String, Object> metadata = new HashMap<>();
            
            // In real implementation:
            // - Call AWS EC2 metadata endpoint: http://169.254.169.254/latest/meta-data/
            // - Call Azure metadata service: http://169.254.169.254/metadata/instance
            // - Call GCP metadata server: http://metadata.google.internal/computeMetadata/v1/
            
            // Simulated metadata
            metadata.put("cloud.provider", "aws");
            metadata.put("cloud.region", "us-east-1");
            metadata.put("cloud.availability-zone", "us-east-1a");
            metadata.put("cloud.instance-id", "i-1234567890abcdef0");
            metadata.put("cloud.instance-type", "t3.medium");
            
            return metadata;
        }
    }

    // ============================================
    // Example 5: Required Properties Validator
    // ============================================
    
    /**
     * Validate required properties exist before startup.
     * Fail fast if critical properties missing.
     */
    public static class RequiredPropertiesValidator implements EnvironmentPostProcessor {
        
        private static final String[] REQUIRED_PROPERTIES = {
            "spring.application.name",
            "app.database.url"
        };
        
        @Override
        public void postProcessEnvironment(ConfigurableEnvironment environment, 
                                          SpringApplication application) {
            System.out.println("=== RequiredPropertiesValidator Executing ===");
            
            List<String> missingProperties = new ArrayList<>();
            
            for (String property : REQUIRED_PROPERTIES) {
                if (environment.getProperty(property) == null) {
                    missingProperties.add(property);
                }
            }
            
            if (!missingProperties.isEmpty()) {
                String message = "Missing required properties: " + String.join(", ", missingProperties);
                System.err.println("  ERROR: " + message);
                throw new IllegalStateException(message);
            }
            
            System.out.println("  All required properties present");
        }
    }

    // ============================================
    // Example 6: Environment-Specific Setup
    // ============================================
    
    /**
     * Add properties based on active profiles.
     */
    public static class EnvironmentSpecificPostProcessor implements EnvironmentPostProcessor {
        
        @Override
        public void postProcessEnvironment(ConfigurableEnvironment environment, 
                                          SpringApplication application) {
            System.out.println("=== EnvironmentSpecificPostProcessor Executing ===");
            
            String[] activeProfiles = environment.getActiveProfiles();
            
            Map<String, Object> envProperties = new HashMap<>();
            
            // Development environment
            if (Arrays.asList(activeProfiles).contains("dev")) {
                envProperties.put("app.debug.enabled", "true");
                envProperties.put("app.cache.enabled", "false");
                envProperties.put("logging.level.root", "DEBUG");
                System.out.println("  Applied development environment properties");
            }
            
            // Production environment
            if (Arrays.asList(activeProfiles).contains("prod")) {
                envProperties.put("app.debug.enabled", "false");
                envProperties.put("app.cache.enabled", "true");
                envProperties.put("logging.level.root", "INFO");
                envProperties.put("app.security.strict", "true");
                System.out.println("  Applied production environment properties");
            }
            
            // Staging environment
            if (Arrays.asList(activeProfiles).contains("staging")) {
                envProperties.put("app.debug.enabled", "true");
                envProperties.put("app.cache.enabled", "true");
                envProperties.put("logging.level.root", "INFO");
                System.out.println("  Applied staging environment properties");
            }
            
            if (!envProperties.isEmpty()) {
                MapPropertySource propertySource = new MapPropertySource(
                    "environmentSpecificProperties",
                    envProperties
                );
                environment.getPropertySources().addLast(propertySource);
            }
        }
    }

    // ============================================
    // Example 7: Computed Properties Post Processor
    // ============================================
    
    /**
     * Add computed properties based on existing values.
     */
    public static class ComputedPropertiesPostProcessor implements EnvironmentPostProcessor {
        
        @Override
        public void postProcessEnvironment(ConfigurableEnvironment environment, 
                                          SpringApplication application) {
            System.out.println("=== ComputedPropertiesPostProcessor Executing ===");
            
            Map<String, Object> computedProperties = new HashMap<>();
            
            // Compute application name with environment
            String appName = environment.getProperty("spring.application.name", "app");
            String[] profiles = environment.getActiveProfiles();
            String profileSuffix = profiles.length > 0 ? "-" + profiles[0] : "";
            computedProperties.put("app.full-name", appName + profileSuffix);
            
            // Compute database pool name
            computedProperties.put("app.database.pool-name", 
                appName + "-pool-" + UUID.randomUUID().toString().substring(0, 8));
            
            // Compute service URLs based on environment
            String baseUrl = environment.getProperty("app.base-url", "http://localhost:8080");
            computedProperties.put("app.api.url", baseUrl + "/api");
            computedProperties.put("app.health.url", baseUrl + "/actuator/health");
            
            // Add hostname and timestamp
            computedProperties.put("app.hostname", getHostname());
            computedProperties.put("app.startup-time", LocalDateTime.now().toString());
            
            MapPropertySource propertySource = new MapPropertySource(
                "computedProperties",
                computedProperties
            );
            environment.getPropertySources().addLast(propertySource);
            
            System.out.println("  Added " + computedProperties.size() + " computed properties");
        }
        
        private String getHostname() {
            try {
                return java.net.InetAddress.getLocalHost().getHostName();
            } catch (Exception e) {
                return "unknown";
            }
        }
    }

    // ============================================
    // Example 8: Post Processor Info Component
    // ============================================
    
    /**
     * Display information about executed post processors.
     */
    @Component
    public static class PostProcessorInfo {
        
        private final ConfigurableEnvironment environment;
        
        public PostProcessorInfo(ConfigurableEnvironment environment) {
            this.environment = environment;
        }
        
        @PostConstruct
        public void displayInfo() {
            System.out.println("\n=== Environment Post Processor Results ===");
            
            // Display properties added by post processors
            displayPropertySource("customPostProcessorProperties");
            displayPropertySource("decryptedProperties");
            displayPropertySource("vaultSecrets");
            displayPropertySource("cloudMetadata");
            displayPropertySource("environmentSpecificProperties");
            displayPropertySource("computedProperties");
        }
        
        private void displayPropertySource(String name) {
            org.springframework.core.env.PropertySource<?> ps = environment.getPropertySources().get(name);
            if (ps instanceof MapPropertySource) {
                MapPropertySource mapSource = (MapPropertySource) ps;
                Map<String, Object> source = mapSource.getSource();
                
                if (!source.isEmpty()) {
                    System.out.println("\n" + name + ":");
                    source.forEach((key, value) -> {
                        String displayValue = key.contains("password") || key.contains("secret") || key.contains("key")
                            ? "***" : String.valueOf(value);
                        System.out.println("  " + key + " = " + displayValue);
                    });
                }
            }
        }
    }

    // ============================================
    // Example 9: Environment Post Processor Controller
    // ============================================
    
    /**
     * REST endpoints to view post processor results.
     */
    @RestController
    @RequestMapping("/env-post-processor")
    public static class EnvironmentPostProcessorController {
        
        private final ConfigurableEnvironment environment;
        
        public EnvironmentPostProcessorController(ConfigurableEnvironment environment) {
            this.environment = environment;
        }
        
        @GetMapping("/property-sources")
        public List<String> getPropertySources() {
            List<String> sources = new ArrayList<>();
            environment.getPropertySources().forEach(ps -> sources.add(ps.getName()));
            return sources;
        }
        
        @GetMapping("/custom-properties")
        public Map<String, Object> getCustomProperties() {
            Map<String, Object> properties = new LinkedHashMap<>();
            
            addPropertiesFromSource("customPostProcessorProperties", properties);
            addPropertiesFromSource("computedProperties", properties);
            addPropertiesFromSource("environmentSpecificProperties", properties);
            
            return properties;
        }
        
        @GetMapping("/cloud-metadata")
        public Map<String, Object> getCloudMetadata() {
            Map<String, Object> metadata = new HashMap<>();
            addPropertiesFromSource("cloudMetadata", metadata);
            return metadata;
        }
        
        @GetMapping("/all-properties")
        public Map<String, Map<String, Object>> getAllPostProcessorProperties() {
            Map<String, Map<String, Object>> all = new LinkedHashMap<>();
            
            List<String> postProcessorSources = Arrays.asList(
                "customPostProcessorProperties",
                "decryptedProperties",
                "vaultSecrets",
                "cloudMetadata",
                "environmentSpecificProperties",
                "computedProperties"
            );
            
            for (String sourceName : postProcessorSources) {
                Map<String, Object> sourceProps = new HashMap<>();
                addPropertiesFromSource(sourceName, sourceProps);
                if (!sourceProps.isEmpty()) {
                    all.put(sourceName, sourceProps);
                }
            }
            
            return all;
        }
        
        private void addPropertiesFromSource(String sourceName, Map<String, Object> target) {
            org.springframework.core.env.PropertySource<?> ps = environment.getPropertySources().get(sourceName);
            if (ps instanceof MapPropertySource) {
                MapPropertySource mapSource = (MapPropertySource) ps;
                Map<String, Object> source = mapSource.getSource();
                
                source.forEach((key, value) -> {
                    // Mask sensitive values
                    if (key.contains("password") || key.contains("secret") || key.contains("key")) {
                        target.put(key, "***");
                    } else {
                        target.put(key, value);
                    }
                });
            }
        }
    }
}
