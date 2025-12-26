package com.example.propertysource;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.ResourcePropertySource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Map;

/**
 * YAML Property Source Pattern
 * 
 * Demonstrates loading and using YAML files as property sources in Spring applications.
 * YAML provides a more readable format for hierarchical configuration compared to properties files.
 * 
 * Key Concepts:
 * - YAML format support
 * - YamlPropertySourceLoader
 * - Hierarchical property structure
 * - Multi-document YAML files
 * - Profile-specific YAML
 * 
 * Use Cases:
 * - Complex configuration structures
 * - Multi-environment configuration
 * - Hierarchical property organization
 * - Readable configuration files
 * - List and map properties
 */
@SpringBootApplication
@PropertySource(value = "classpath:application.yml", factory = YamlPropertySourceFactory.class)
public class YAMLPropertySourcePattern {

    public static void main(String[] args) {
        SpringApplication.run(YAMLPropertySourcePattern.class, args);
    }
}

/**
 * Custom PropertySourceFactory for YAML files
 */
class YamlPropertySourceFactory implements org.springframework.core.io.support.PropertySourceFactory {

    @Override
    public org.springframework.core.env.PropertySource<?> createPropertySource(
            String name, 
            org.springframework.core.io.support.EncodedResource resource) throws IOException {
        
        org.springframework.beans.factory.config.YamlPropertiesFactoryBean factory = 
                new org.springframework.beans.factory.config.YamlPropertiesFactoryBean();
        factory.setResources(resource.getResource());

        java.util.Properties properties = factory.getObject();
        String sourceName = name != null ? name : resource.getResource().getFilename();
        
        return new org.springframework.core.env.PropertiesPropertySource(sourceName, properties);
    }
}

/**
 * Configuration class that loads YAML properties
 */
@Configuration
class YamlConfig {

    private final ConfigurableEnvironment environment;

    public YamlConfig(ConfigurableEnvironment environment) {
        this.environment = environment;
    }

    @PostConstruct
    public void loadYamlProperties() {
        try {
            // Load custom YAML file
            ResourcePropertySource yamlSource = new ResourcePropertySource(
                    "customYamlSource",
                    new ClassPathResource("custom-config.yml")
            );
            environment.getPropertySources().addLast(yamlSource);
        } catch (IOException e) {
            // Handle exception - file might not exist
            System.err.println("Could not load custom-config.yml: " + e.getMessage());
        }
    }
}

/**
 * Configuration properties from YAML
 */
@Configuration
@ConfigurationProperties(prefix = "app")
class AppYamlProperties {
    
    private String name;
    private String version;
    private ServerConfig server;
    private DatabaseConfig database;
    private FeatureFlags features;
    
    // Nested configuration
    public static class ServerConfig {
        private int port;
        private String host;
        private int timeout;
        
        public int getPort() { return port; }
        public void setPort(int port) { this.port = port; }
        public String getHost() { return host; }
        public void setHost(String host) { this.host = host; }
        public int getTimeout() { return timeout; }
        public void setTimeout(int timeout) { this.timeout = timeout; }
    }
    
    public static class DatabaseConfig {
        private String url;
        private String username;
        private String password;
        private PoolConfig pool;
        
        public static class PoolConfig {
            private int minSize;
            private int maxSize;
            
            public int getMinSize() { return minSize; }
            public void setMinSize(int minSize) { this.minSize = minSize; }
            public int getMaxSize() { return maxSize; }
            public void setMaxSize(int maxSize) { this.maxSize = maxSize; }
        }
        
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public PoolConfig getPool() { return pool; }
        public void setPool(PoolConfig pool) { this.pool = pool; }
    }
    
    public static class FeatureFlags {
        private boolean newUI;
        private boolean analytics;
        private boolean betaFeatures;
        
        public boolean isNewUI() { return newUI; }
        public void setNewUI(boolean newUI) { this.newUI = newUI; }
        public boolean isAnalytics() { return analytics; }
        public void setAnalytics(boolean analytics) { this.analytics = analytics; }
        public boolean isBetaFeatures() { return betaFeatures; }
        public void setBetaFeatures(boolean betaFeatures) { this.betaFeatures = betaFeatures; }
    }
    
    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public ServerConfig getServer() { return server; }
    public void setServer(ServerConfig server) { this.server = server; }
    public DatabaseConfig getDatabase() { return database; }
    public void setDatabase(DatabaseConfig database) { this.database = database; }
    public FeatureFlags getFeatures() { return features; }
    public void setFeatures(FeatureFlags features) { this.features = features; }
}

/**
 * Controller to expose YAML properties
 */
@RestController
@RequestMapping("/api/yaml")
class YamlPropertiesController {

    private final AppYamlProperties appProperties;

    public YamlPropertiesController(AppYamlProperties appProperties) {
        this.appProperties = appProperties;
    }

    @GetMapping("/config")
    public AppYamlProperties getConfiguration() {
        return appProperties;
    }

    @GetMapping("/server")
    public AppYamlProperties.ServerConfig getServerConfig() {
        return appProperties.getServer();
    }

    @GetMapping("/database")
    public AppYamlProperties.DatabaseConfig getDatabaseConfig() {
        return appProperties.getDatabase();
    }

    @GetMapping("/features")
    public AppYamlProperties.FeatureFlags getFeatures() {
        return appProperties.getFeatures();
    }
}

/**
 * Documentation:
 * 
 * YAML Format Benefits:
 * - More readable than properties files
 * - Supports hierarchical data
 * - Natural representation of lists and maps
 * - Less repetitive for nested properties
 * - Supports multi-document files
 * 
 * Example YAML Structure:
 * 
 * app:
 *   name: My Application
 *   version: 1.0.0
 *   server:
 *     port: 8080
 *     host: localhost
 *     timeout: 5000
 *   database:
 *     url: jdbc:mysql://localhost:3306/mydb
 *     username: user
 *     password: pass
 *     pool:
 *       min-size: 5
 *       max-size: 20
 *   features:
 *     new-ui: true
 *     analytics: false
 *     beta-features: false
 * 
 * Lists in YAML:
 * 
 * app:
 *   allowed-origins:
 *     - http://localhost:3000
 *     - http://localhost:4200
 *     - https://example.com
 * 
 * Maps in YAML:
 * 
 * app:
 *   environment-variables:
 *     NODE_ENV: production
 *     API_KEY: secret-key
 *     DEBUG: false
 * 
 * Multi-document YAML (with profiles):
 * 
 * app:
 *   name: My App
 * ---
 * spring:
 *   profiles: dev
 * app:
 *   name: My App (Dev)
 * ---
 * spring:
 *   profiles: prod
 * app:
 *   name: My App (Production)
 * 
 * Spring Boot YAML Support:
 * - application.yml automatically loaded
 * - application-{profile}.yml for profiles
 * - YamlPropertiesFactoryBean for loading
 * - YamlPropertySourceLoader for custom loading
 * 
 * Loading YAML Files:
 * 
 * 1. Using @PropertySource with factory:
 *    @PropertySource(value = "classpath:config.yml", factory = YamlPropertySourceFactory.class)
 * 
 * 2. Programmatically:
 *    YamlPropertySourceLoader loader = new YamlPropertySourceLoader();
 *    List<PropertySource<?>> sources = loader.load("config", resource);
 * 
 * 3. Using YamlPropertiesFactoryBean:
 *    YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
 *    factory.setResources(new ClassPathResource("config.yml"));
 *    Properties props = factory.getObject();
 * 
 * Property Naming Conventions:
 * - Use kebab-case in YAML: my-property-name
 * - Maps to camelCase in Java: myPropertyName
 * - Also supports dot notation: my.property.name
 * 
 * Best Practices:
 * - Use YAML for complex hierarchical config
 * - Use properties files for simple config
 * - Organize related properties under common prefixes
 * - Use profiles for environment-specific config
 * - Document structure with comments
 * - Validate YAML syntax
 * - Use @ConfigurationProperties for type-safe binding
 * - Provide sensible defaults
 * 
 * Common Pitfalls:
 * - Indentation must be spaces, not tabs
 * - Inconsistent indentation causes parsing errors
 * - Special characters may need quoting
 * - Boolean values: true/false (lowercase)
 * - Null values: ~ or null
 * 
 * YAML vs Properties:
 * 
 * YAML Advantages:
 * - More readable
 * - Less repetitive
 * - Native support for lists/maps
 * - Better for complex structures
 * 
 * Properties Advantages:
 * - Simpler format
 * - Better IDE support historically
 * - More familiar to many developers
 * - Easier to override single values
 * 
 * Integration with @ConfigurationProperties:
 * - Binds YAML to Java objects
 * - Type-safe configuration
 * - Validation support
 * - Nested properties
 * - Lists and maps support
 * - Relaxed binding rules
 */
