package com.example.microservices.configserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Config Server Pattern
 * 
 * This pattern demonstrates Spring Cloud Config Server, which provides centralized
 * external configuration for distributed systems. Configuration is stored in a
 * version-controlled repository (Git) and served to client applications.
 * 
 * Key Components:
 * 1. ConfigServer - Central configuration server
 * 2. EnvironmentRepository - Stores configuration in different backends (Git, File, etc.)
 * 3. ConfigClient - Client application that fetches configuration
 * 4. PropertySource - Configuration properties for an application/profile
 * 5. EncryptionService - Encrypts/decrypts sensitive configuration
 * 
 * Features:
 * - Centralized configuration management
 * - Environment-specific configuration (dev, test, prod)
 * - Version control of configuration
 * - Dynamic configuration refresh
 * - Encryption/decryption of sensitive properties
 * - Multiple backend support (Git, File, Vault)
 * 
 * Use Cases:
 * - Managing configuration across multiple environments
 * - Storing sensitive configuration securely
 * - Dynamic configuration updates without restart
 * - Configuration versioning and rollback
 * - Multi-application configuration management
 */

@SpringBootApplication
public class ConfigServerPattern {

    public static void main(String[] args) {
        SpringApplication.run(ConfigServerPattern.class, args);
        
        // Demonstration
        System.out.println("=== Config Server Pattern Demo ===\n");
        
        // Create Config Server with Git backend
        GitEnvironmentRepository gitRepo = new GitEnvironmentRepository("config-repo");
        ConfigServer configServer = new ConfigServer(gitRepo);
        EncryptionService encryptionService = new EncryptionService("my-secret-key");
        
        // Setup sample configuration
        System.out.println("1. Setting up Configuration:");
        setupSampleConfiguration(gitRepo);
        
        // Fetch configuration for different environments
        System.out.println("\n2. Fetching Configuration:");
        
        Environment devEnv = configServer.getEnvironment("user-service", "dev", null);
        System.out.println("user-service (dev) configuration:");
        devEnv.getPropertySources().forEach(ps -> {
            System.out.println("  Source: " + ps.getName());
            ps.getSource().forEach((key, value) -> 
                System.out.println("    " + key + " = " + value));
        });
        
        Environment prodEnv = configServer.getEnvironment("user-service", "prod", null);
        System.out.println("\nuser-service (prod) configuration:");
        prodEnv.getPropertySources().forEach(ps -> {
            System.out.println("  Source: " + ps.getName());
            ps.getSource().forEach((key, value) -> 
                System.out.println("    " + key + " = " + value));
        });
        
        // Encrypt sensitive data
        System.out.println("\n3. Encrypting Sensitive Configuration:");
        String plainPassword = "db-password-123";
        String encrypted = encryptionService.encrypt(plainPassword);
        String decrypted = encryptionService.decrypt(encrypted);
        
        System.out.println("Plain: " + plainPassword);
        System.out.println("Encrypted: " + encrypted);
        System.out.println("Decrypted: " + decrypted);
        
        // Config Client usage
        System.out.println("\n4. Config Client Fetching Configuration:");
        ConfigClient configClient = new ConfigClient("user-service", "dev", configServer);
        configClient.fetchConfiguration();
        
        String dbUrl = configClient.getProperty("spring.datasource.url");
        String serverPort = configClient.getProperty("server.port");
        System.out.println("Database URL: " + dbUrl);
        System.out.println("Server Port: " + serverPort);
        
        // Refresh configuration
        System.out.println("\n5. Dynamic Configuration Refresh:");
        gitRepo.updateProperty("user-service", "dev", "feature.new-ui", "true");
        configClient.refresh();
        System.out.println("Feature flag 'new-ui': " + configClient.getProperty("feature.new-ui"));
        
        // Multiple profiles
        System.out.println("\n6. Multiple Profiles:");
        Environment multiProfile = configServer.getEnvironment("order-service", "dev,mysql", null);
        System.out.println("order-service with profiles [dev, mysql]:");
        multiProfile.getPropertySources().forEach(ps -> 
            System.out.println("  Source: " + ps.getName()));
    }
    
    private static void setupSampleConfiguration(GitEnvironmentRepository repo) {
        // user-service dev configuration
        Map<String, String> userServiceDev = new HashMap<>();
        userServiceDev.put("server.port", "8081");
        userServiceDev.put("spring.datasource.url", "jdbc:h2:mem:devdb");
        userServiceDev.put("spring.datasource.username", "dev");
        userServiceDev.put("logging.level.root", "DEBUG");
        userServiceDev.put("feature.new-ui", "false");
        repo.saveConfiguration("user-service", "dev", userServiceDev);
        
        // user-service prod configuration
        Map<String, String> userServiceProd = new HashMap<>();
        userServiceProd.put("server.port", "8080");
        userServiceProd.put("spring.datasource.url", "jdbc:postgresql://prod-db:5432/users");
        userServiceProd.put("spring.datasource.username", "prod_user");
        userServiceProd.put("logging.level.root", "INFO");
        userServiceProd.put("feature.new-ui", "true");
        repo.saveConfiguration("user-service", "prod", userServiceProd);
        
        // order-service dev configuration
        Map<String, String> orderServiceDev = new HashMap<>();
        orderServiceDev.put("server.port", "8082");
        orderServiceDev.put("spring.datasource.url", "jdbc:h2:mem:orderdb");
        repo.saveConfiguration("order-service", "dev", orderServiceDev);
        
        // order-service mysql profile
        Map<String, String> orderServiceMysql = new HashMap<>();
        orderServiceMysql.put("spring.datasource.url", "jdbc:mysql://localhost:3306/orders");
        orderServiceMysql.put("spring.datasource.driver-class-name", "com.mysql.cj.jdbc.Driver");
        repo.saveConfiguration("order-service", "mysql", orderServiceMysql);
        
        System.out.println("Sample configuration created for user-service and order-service");
    }
}

/**
 * Config Server - Central configuration server
 */
@Service
class ConfigServer {
    private final EnvironmentRepository environmentRepository;
    private final Map<String, LocalDateTime> lastAccessTime = new ConcurrentHashMap<>();
    
    public ConfigServer(EnvironmentRepository environmentRepository) {
        this.environmentRepository = environmentRepository;
        System.out.println("Config Server started on port 8888");
    }
    
    public Environment getEnvironment(String application, String profile, String label) {
        lastAccessTime.put(application + ":" + profile, LocalDateTime.now());
        return environmentRepository.findOne(application, profile, label);
    }
    
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalApplications", lastAccessTime.size());
        stats.put("repositoryType", environmentRepository.getClass().getSimpleName());
        stats.put("lastAccessTimes", lastAccessTime);
        return stats;
    }
}

/**
 * Environment Repository Interface
 */
interface EnvironmentRepository {
    Environment findOne(String application, String profile, String label);
}

/**
 * Git-based Environment Repository
 */
class GitEnvironmentRepository implements EnvironmentRepository {
    private final String basePath;
    private final Map<String, Map<String, Map<String, String>>> configStore = new ConcurrentHashMap<>();
    
    public GitEnvironmentRepository(String basePath) {
        this.basePath = basePath;
        System.out.println("Git repository initialized at: " + basePath);
    }
    
    @Override
    public Environment findOne(String application, String profile, String label) {
        Environment environment = new Environment(application, profile, label);
        
        // Handle multiple profiles
        String[] profiles = profile != null ? profile.split(",") : new String[]{"default"};
        
        // Add property sources in order of precedence (most specific first)
        for (int i = profiles.length - 1; i >= 0; i--) {
            String prof = profiles[i].trim();
            
            // Application-specific profile configuration
            Map<String, String> appProfileConfig = getConfiguration(application, prof);
            if (appProfileConfig != null) {
                PropertySource ps = new PropertySource(application + "-" + prof, appProfileConfig);
                environment.addPropertySource(ps);
            }
            
            // Application default configuration
            if (i == profiles.length - 1) {
                Map<String, String> appConfig = getConfiguration(application, "default");
                if (appConfig != null) {
                    PropertySource ps = new PropertySource(application, appConfig);
                    environment.addPropertySource(ps);
                }
            }
        }
        
        return environment;
    }
    
    public void saveConfiguration(String application, String profile, Map<String, String> properties) {
        configStore.computeIfAbsent(application, k -> new ConcurrentHashMap<>())
                  .put(profile, new HashMap<>(properties));
    }
    
    public Map<String, String> getConfiguration(String application, String profile) {
        return configStore.getOrDefault(application, Collections.emptyMap())
                         .get(profile);
    }
    
    public void updateProperty(String application, String profile, String key, String value) {
        Map<String, Map<String, String>> appConfigs = configStore.get(application);
        if (appConfigs != null) {
            Map<String, String> profileConfig = appConfigs.get(profile);
            if (profileConfig != null) {
                profileConfig.put(key, value);
                System.out.println("Updated property: " + key + " = " + value + 
                                 " for " + application + "-" + profile);
            }
        }
    }
}

/**
 * File-based Environment Repository
 */
class FileEnvironmentRepository implements EnvironmentRepository {
    private final String searchPath;
    
    public FileEnvironmentRepository(String searchPath) {
        this.searchPath = searchPath;
    }
    
    @Override
    public Environment findOne(String application, String profile, String label) {
        Environment environment = new Environment(application, profile, label);
        
        // Load from file system
        String filename = application + "-" + profile + ".properties";
        Path filePath = Paths.get(searchPath, filename);
        
        if (Files.exists(filePath)) {
            try {
                Properties props = new Properties();
                props.load(Files.newInputStream(filePath));
                
                Map<String, String> source = new HashMap<>();
                props.forEach((key, value) -> source.put(key.toString(), value.toString()));
                
                PropertySource ps = new PropertySource(filename, source);
                environment.addPropertySource(ps);
            } catch (IOException e) {
                System.err.println("Error loading configuration from file: " + e.getMessage());
            }
        }
        
        return environment;
    }
}

/**
 * Environment - Contains configuration for an application/profile
 */
class Environment {
    private final String name;
    private final String[] profiles;
    private final String label;
    private final List<PropertySource> propertySources;
    private final String version;
    
    public Environment(String name, String profile, String label) {
        this.name = name;
        this.profiles = profile != null ? profile.split(",") : new String[]{"default"};
        this.label = label;
        this.propertySources = new ArrayList<>();
        this.version = "1.0.0";
    }
    
    public void addPropertySource(PropertySource propertySource) {
        propertySources.add(propertySource);
    }
    
    public String getName() { return name; }
    public String[] getProfiles() { return profiles; }
    public String getLabel() { return label; }
    public List<PropertySource> getPropertySources() { return propertySources; }
    public String getVersion() { return version; }
}

/**
 * Property Source - A named source of properties
 */
class PropertySource {
    private final String name;
    private final Map<String, String> source;
    
    public PropertySource(String name, Map<String, String> source) {
        this.name = name;
        this.source = source;
    }
    
    public String getName() { return name; }
    public Map<String, String> getSource() { return source; }
}

/**
 * Config Client - Client that fetches configuration from server
 */
class ConfigClient {
    private final String applicationName;
    private final String profile;
    private final ConfigServer configServer;
    private final Map<String, String> properties = new ConcurrentHashMap<>();
    
    public ConfigClient(String applicationName, String profile, ConfigServer configServer) {
        this.applicationName = applicationName;
        this.profile = profile;
        this.configServer = configServer;
    }
    
    public void fetchConfiguration() {
        Environment environment = configServer.getEnvironment(applicationName, profile, null);
        
        properties.clear();
        
        // Merge all property sources (earlier sources override later ones)
        for (int i = environment.getPropertySources().size() - 1; i >= 0; i--) {
            PropertySource ps = environment.getPropertySources().get(i);
            properties.putAll(ps.getSource());
        }
        
        System.out.println("Fetched " + properties.size() + " properties for " + 
                         applicationName + " (" + profile + ")");
    }
    
    public void refresh() {
        System.out.println("Refreshing configuration for " + applicationName);
        fetchConfiguration();
    }
    
    public String getProperty(String key) {
        return properties.get(key);
    }
    
    public String getProperty(String key, String defaultValue) {
        return properties.getOrDefault(key, defaultValue);
    }
    
    public Map<String, String> getAllProperties() {
        return new HashMap<>(properties);
    }
}

/**
 * Encryption Service - Encrypts/decrypts sensitive configuration
 */
class EncryptionService {
    private final String key;
    
    public EncryptionService(String key) {
        this.key = key;
    }
    
    public String encrypt(String plainText) {
        // Simple XOR encryption for demonstration
        // In production, use proper encryption (AES, RSA, etc.)
        StringBuilder encrypted = new StringBuilder();
        for (int i = 0; i < plainText.length(); i++) {
            char c = plainText.charAt(i);
            char k = key.charAt(i % key.length());
            encrypted.append((char) (c ^ k));
        }
        return Base64.getEncoder().encodeToString(encrypted.toString().getBytes());
    }
    
    public String decrypt(String encryptedText) {
        // Simple XOR decryption for demonstration
        byte[] decoded = Base64.getDecoder().decode(encryptedText);
        String encrypted = new String(decoded);
        
        StringBuilder decrypted = new StringBuilder();
        for (int i = 0; i < encrypted.length(); i++) {
            char c = encrypted.charAt(i);
            char k = key.charAt(i % key.length());
            decrypted.append((char) (c ^ k));
        }
        return decrypted.toString();
    }
}

/**
 * REST Controller for Config Server
 */
@RestController
@RequestMapping("/config")
class ConfigServerController {
    private final ConfigServer configServer;
    private final EncryptionService encryptionService;
    
    public ConfigServerController(ConfigServer configServer) {
        this.configServer = configServer;
        this.encryptionService = new EncryptionService("default-key");
    }
    
    @GetMapping("/{application}/{profile}")
    public Environment getConfiguration(@PathVariable String application,
                                       @PathVariable String profile) {
        return configServer.getEnvironment(application, profile, null);
    }
    
    @GetMapping("/{application}/{profile}/{label}")
    public Environment getConfigurationWithLabel(@PathVariable String application,
                                                 @PathVariable String profile,
                                                 @PathVariable String label) {
        return configServer.getEnvironment(application, profile, label);
    }
    
    @PostMapping("/encrypt")
    public Map<String, String> encrypt(@RequestBody Map<String, String> request) {
        String plainText = request.get("plainText");
        String encrypted = encryptionService.encrypt(plainText);
        return Map.of("encrypted", encrypted);
    }
    
    @PostMapping("/decrypt")
    public Map<String, String> decrypt(@RequestBody Map<String, String> request) {
        String encryptedText = request.get("encryptedText");
        String decrypted = encryptionService.decrypt(encryptedText);
        return Map.of("decrypted", decrypted);
    }
    
    @GetMapping("/stats")
    public Map<String, Object> getStatistics() {
        return configServer.getStatistics();
    }
}

/**
 * Config properties with auto-refresh support
 */
class RefreshableConfig {
    private final ConfigClient configClient;
    private final Map<String, List<PropertyChangeListener>> listeners = new ConcurrentHashMap<>();
    
    public RefreshableConfig(ConfigClient configClient) {
        this.configClient = configClient;
    }
    
    public void refresh() {
        Map<String, String> oldProperties = configClient.getAllProperties();
        configClient.refresh();
        Map<String, String> newProperties = configClient.getAllProperties();
        
        // Notify listeners of changes
        newProperties.forEach((key, newValue) -> {
            String oldValue = oldProperties.get(key);
            if (!Objects.equals(oldValue, newValue)) {
                notifyPropertyChanged(key, oldValue, newValue);
            }
        });
    }
    
    public void addPropertyChangeListener(String propertyKey, PropertyChangeListener listener) {
        listeners.computeIfAbsent(propertyKey, k -> new ArrayList<>()).add(listener);
    }
    
    private void notifyPropertyChanged(String key, String oldValue, String newValue) {
        List<PropertyChangeListener> keyListeners = listeners.get(key);
        if (keyListeners != null) {
            keyListeners.forEach(listener -> 
                listener.onPropertyChanged(key, oldValue, newValue));
        }
    }
}

/**
 * Property Change Listener interface
 */
@FunctionalInterface
interface PropertyChangeListener {
    void onPropertyChanged(String key, String oldValue, String newValue);
}
