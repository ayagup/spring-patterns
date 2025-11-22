package com.example.cloud.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Configuration Server Pattern - Demonstrates Centralized Configuration Management
 * 
 * This pattern shows how to:
 * 1. Set up Spring Cloud Config Server
 * 2. Store configurations in Git repository
 * 3. Manage environment-specific configurations
 * 4. Implement dynamic configuration refresh
 * 5. Encrypt sensitive properties
 * 6. Use profiles for different environments
 * 7. Implement configuration versioning
 * 8. Handle configuration precedence
 * 9. Monitor configuration changes
 * 10. Implement fallback configurations
 * 
 * Key Concepts:
 * - Centralized Config: Single source of truth
 * - Environment-Specific: Different configs per environment
 * - Dynamic Refresh: Update without restart
 * - Encryption: Secure sensitive data
 * - Profiles: dev, test, prod configurations
 * 
 * Configuration Hierarchy:
 * 1. application.yml (default)
 * 2. application-{profile}.yml (profile-specific)
 * 3. {application-name}.yml (app-specific)
 * 4. {application-name}-{profile}.yml (app + profile specific)
 * 
 * Dependencies:
 * - spring-cloud-config-server
 * - spring-cloud-config-client
 * - spring-cloud-starter-bootstrap
 * 
 * @author Spring Patterns
 * @version 1.0
 */

@SpringBootApplication
@EnableConfigServer
public class ConfigurationServerPattern {

    public static void main(String[] args) {
        SpringApplication.run(ConfigurationServerPattern.class, args);
        
        System.out.println("=".repeat(80));
        System.out.println("CONFIGURATION SERVER PATTERN DEMONSTRATION");
        System.out.println("=".repeat(80));
        
        demonstrateConfigServer();
        demonstrateConfigClient();
        demonstrateRefreshScope();
        
        System.out.println("\nConfig Server running on port 8888");
        System.out.println("Access configs at: http://localhost:8888/{application}/{profile}");
        System.out.println("\nPress Ctrl+C to stop.\n");
    }
    
    private static void demonstrateConfigServer() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("CONFIG SERVER SETUP");
        System.out.println("=".repeat(80));
        
        System.out.println("\nserver.port=8888");
        System.out.println("spring.cloud.config.server.git.uri=https://github.com/config-repo");
        System.out.println("spring.cloud.config.server.git.default-label=main");
        System.out.println("spring.cloud.config.server.git.search-paths=config");
    }
    
    private static void demonstrateConfigClient() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("CONFIG CLIENT SETUP");
        System.out.println("=".repeat(80));
        
        System.out.println("\nspring.config.import=optional:configserver:http://localhost:8888");
        System.out.println("spring.application.name=my-service");
        System.out.println("spring.profiles.active=dev");
    }
    
    private static void demonstrateRefreshScope() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("DYNAMIC REFRESH");
        System.out.println("=".repeat(80));
        
        System.out.println("\n@RefreshScope annotation enables dynamic refresh");
        System.out.println("POST /actuator/refresh to reload configuration");
        System.out.println("Spring Cloud Bus for broadcasting refresh events");
    }
}

/**
 * Configuration Properties Model
 */
class ConfigProperty {
    private String key;
    private String value;
    private String profile;
    private String label;
    
    public ConfigProperty(String key, String value, String profile, String label) {
        this.key = key;
        this.value = value;
        this.profile = profile;
        this.label = label;
    }
    
    // Getters and setters
    public String getKey() { return key; }
    public String getValue() { return value; }
    public String getProfile() { return profile; }
    public String getLabel() { return label; }
    public void setValue(String value) { this.value = value; }
}

/**
 * In-Memory Configuration Repository
 */
@Service
class ConfigRepository {
    
    private final Map<String, Map<String, ConfigProperty>> configurations = new ConcurrentHashMap<>();
    
    public ConfigRepository() {
        initializeDefaultConfigs();
    }
    
    private void initializeDefaultConfigs() {
        // Default profile
        addConfig("application", "default", "server.port", "8080", "main");
        addConfig("application", "default", "logging.level.root", "INFO", "main");
        
        // Development profile
        addConfig("application", "dev", "server.port", "8081", "main");
        addConfig("application", "dev", "logging.level.root", "DEBUG", "main");
        addConfig("application", "dev", "spring.datasource.url", 
            "jdbc:h2:mem:devdb", "main");
        
        // Production profile
        addConfig("application", "prod", "server.port", "80", "main");
        addConfig("application", "prod", "logging.level.root", "WARN", "main");
        addConfig("application", "prod", "spring.datasource.url", 
            "jdbc:postgresql://prod-db:5432/appdb", "main");
        
        // Service-specific configs
        addConfig("user-service", "default", "service.name", "User Service", "main");
        addConfig("user-service", "default", "service.version", "1.0.0", "main");
        
        addConfig("order-service", "default", "service.name", "Order Service", "main");
        addConfig("order-service", "default", "service.version", "2.0.0", "main");
    }
    
    private void addConfig(String application, String profile, String key, 
                          String value, String label) {
        String configKey = application + ":" + profile;
        configurations.computeIfAbsent(configKey, k -> new HashMap<>())
            .put(key, new ConfigProperty(key, value, profile, label));
    }
    
    public Map<String, ConfigProperty> getConfig(String application, String profile) {
        String configKey = application + ":" + profile;
        Map<String, ConfigProperty> config = new HashMap<>();
        
        // Start with default application config
        Map<String, ConfigProperty> defaultConfig = 
            configurations.get("application:default");
        if (defaultConfig != null) {
            config.putAll(defaultConfig);
        }
        
        // Override with profile-specific application config
        Map<String, ConfigProperty> profileConfig = 
            configurations.get("application:" + profile);
        if (profileConfig != null) {
            config.putAll(profileConfig);
        }
        
        // Override with application-specific default config
        Map<String, ConfigProperty> appConfig = 
            configurations.get(application + ":default");
        if (appConfig != null) {
            config.putAll(appConfig);
        }
        
        // Override with application + profile specific config
        Map<String, ConfigProperty> appProfileConfig = 
            configurations.get(configKey);
        if (appProfileConfig != null) {
            config.putAll(appProfileConfig);
        }
        
        return config;
    }
    
    public void updateConfig(String application, String profile, String key, String value) {
        String configKey = application + ":" + profile;
        configurations.computeIfAbsent(configKey, k -> new HashMap<>())
            .put(key, new ConfigProperty(key, value, profile, "main"));
    }
}

/**
 * Configuration Service with Refresh Scope
 */
@Service
@RefreshScope
class ConfigurationService {
    
    @Value("${app.feature.enabled:false}")
    private boolean featureEnabled;
    
    @Value("${app.max.connections:100}")
    private int maxConnections;
    
    @Value("${app.timeout:5000}")
    private int timeout;
    
    private final ConfigRepository configRepository;
    
    public ConfigurationService(ConfigRepository configRepository) {
        this.configRepository = configRepository;
    }
    
    public Map<String, Object> getCurrentConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("featureEnabled", featureEnabled);
        config.put("maxConnections", maxConnections);
        config.put("timeout", timeout);
        return config;
    }
    
    public Map<String, ConfigProperty> getConfigForService(String application, String profile) {
        return configRepository.getConfig(application, profile);
    }
}

/**
 * Configuration Encryption Service
 */
@Service
class ConfigEncryptionService {
    
    private final String encryptionKey = "my-secret-key";
    
    public String encrypt(String plainText) {
        // Simplified encryption (use proper encryption in production)
        return "{cipher}" + Base64.getEncoder()
            .encodeToString(plainText.getBytes());
    }
    
    public String decrypt(String encrypted) {
        if (encrypted.startsWith("{cipher}")) {
            String cipherText = encrypted.substring(8);
            return new String(Base64.getDecoder().decode(cipherText));
        }
        return encrypted;
    }
    
    public boolean isEncrypted(String value) {
        return value != null && value.startsWith("{cipher}");
    }
}

/**
 * Configuration Change Monitor
 */
@Service
class ConfigChangeMonitor {
    
    private final Map<String, String> configVersions = new ConcurrentHashMap<>();
    private final List<ConfigChangeEvent> changeHistory = new ArrayList<>();
    
    public void recordChange(String application, String profile, String key, 
                            String oldValue, String newValue) {
        ConfigChangeEvent event = new ConfigChangeEvent(
            application, profile, key, oldValue, newValue, new Date()
        );
        changeHistory.add(event);
        
        String versionKey = application + ":" + profile;
        configVersions.merge(versionKey, "1", (old, v) -> 
            String.valueOf(Integer.parseInt(old) + 1));
    }
    
    public List<ConfigChangeEvent> getChangeHistory() {
        return new ArrayList<>(changeHistory);
    }
    
    public String getVersion(String application, String profile) {
        return configVersions.getOrDefault(application + ":" + profile, "0");
    }
}

/**
 * Configuration Change Event
 */
class ConfigChangeEvent {
    private final String application;
    private final String profile;
    private final String key;
    private final String oldValue;
    private final String newValue;
    private final Date timestamp;
    
    public ConfigChangeEvent(String application, String profile, String key,
                            String oldValue, String newValue, Date timestamp) {
        this.application = application;
        this.profile = profile;
        this.key = key;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.timestamp = timestamp;
    }
    
    // Getters
    public String getApplication() { return application; }
    public String getProfile() { return profile; }
    public String getKey() { return key; }
    public String getOldValue() { return oldValue; }
    public String getNewValue() { return newValue; }
    public Date getTimestamp() { return timestamp; }
}

/**
 * REST Controller for Configuration Management
 */
@RestController
@RequestMapping("/api/config")
class ConfigController {
    
    private final ConfigurationService configurationService;
    private final ConfigRepository configRepository;
    private final ConfigEncryptionService encryptionService;
    private final ConfigChangeMonitor changeMonitor;
    
    public ConfigController(ConfigurationService configurationService,
                          ConfigRepository configRepository,
                          ConfigEncryptionService encryptionService,
                          ConfigChangeMonitor changeMonitor) {
        this.configurationService = configurationService;
        this.configRepository = configRepository;
        this.encryptionService = encryptionService;
        this.changeMonitor = changeMonitor;
    }
    
    @GetMapping("/{application}/{profile}")
    public Map<String, ConfigProperty> getConfig(
            @PathVariable String application,
            @PathVariable String profile) {
        return configurationService.getConfigForService(application, profile);
    }
    
    @GetMapping("/current")
    public Map<String, Object> getCurrentConfig() {
        return configurationService.getCurrentConfig();
    }
    
    @PostMapping("/{application}/{profile}")
    public String updateConfig(
            @PathVariable String application,
            @PathVariable String profile,
            @RequestParam String key,
            @RequestParam String value) {
        
        Map<String, ConfigProperty> currentConfig = 
            configRepository.getConfig(application, profile);
        String oldValue = currentConfig.containsKey(key) ? 
            currentConfig.get(key).getValue() : null;
        
        configRepository.updateConfig(application, profile, key, value);
        changeMonitor.recordChange(application, profile, key, oldValue, value);
        
        return "Configuration updated successfully";
    }
    
    @PostMapping("/encrypt")
    public String encryptValue(@RequestParam String value) {
        return encryptionService.encrypt(value);
    }
    
    @PostMapping("/decrypt")
    public String decryptValue(@RequestParam String value) {
        return encryptionService.decrypt(value);
    }
    
    @GetMapping("/changes")
    public List<ConfigChangeEvent> getChangeHistory() {
        return changeMonitor.getChangeHistory();
    }
    
    @GetMapping("/version/{application}/{profile}")
    public Map<String, String> getVersion(
            @PathVariable String application,
            @PathVariable String profile) {
        String version = changeMonitor.getVersion(application, profile);
        return Map.of("version", version);
    }
}
