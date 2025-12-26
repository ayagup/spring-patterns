package com.example.demo.patterns.configuration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Property Source Pattern - Custom Property Sources
 * 
 * Purpose:
 * - Load properties from custom sources (database, API, vault)
 * - Extend Spring's property resolution mechanism
 * - Support dynamic property loading
 * - Enable multi-tenant configuration
 * - Integrate with external configuration systems
 * 
 * Use Cases:
 * - Database-driven configuration (per-tenant settings)
 * - REST API property source (external config service)
 * - HashiCorp Vault integration (secrets management)
 * - AWS Parameter Store/Secrets Manager
 * - Azure Key Vault configuration
 * - Consul/Etcd key-value store
 * - Redis property cache
 * - Encrypted property sources
 * - Environment-specific overrides
 * - Multi-tenant configuration per customer
 * 
 * Key Concepts:
 * - PropertySource: Abstraction for property origin
 * - PropertySourcesPlaceholderConfigurer: Resolves ${} placeholders
 * - @PropertySource: Annotation to declare property sources
 * - Environment: Container for all PropertySources
 * - PropertySourceLocator: Spring Cloud Config interface
 * - Order/Priority: Higher priority sources override lower
 * - Composite Property Source: Combine multiple sources
 * 
 * Implementation Patterns:
 * 1. Database property source (JDBC)
 * 2. REST API property source
 * 3. Redis cache property source
 * 4. Vault secrets property source
 * 5. Encrypted property source
 * 6. Multi-tenant property source
 * 7. Composite property source (fallback chain)
 * 8. Lazy-loading property source
 * 9. Cached property source with TTL
 * 10. Environment-aware property source
 * 11. Property source with refresh capability
 * 12. Custom @PropertySource implementation
 * 
 * Dependencies:
 * <dependency>
 *     <groupId>org.springframework.boot</groupId>
 *     <artifactId>spring-boot-starter</artifactId>
 * </dependency>
 * 
 * <!-- For database property source -->
 * <dependency>
 *     <groupId>org.springframework.boot</groupId>
 *     <artifactId>spring-boot-starter-jdbc</artifactId>
 * </dependency>
 * 
 * <!-- For Vault -->
 * <dependency>
 *     <groupId>org.springframework.cloud</groupId>
 *     <artifactId>spring-cloud-starter-vault-config</artifactId>
 * </dependency>
 * 
 * Database Schema (for database property source):
 * CREATE TABLE application_properties (
 *     property_key VARCHAR(255) PRIMARY KEY,
 *     property_value VARCHAR(1000),
 *     environment VARCHAR(50),
 *     tenant_id VARCHAR(50),
 *     created_at TIMESTAMP,
 *     updated_at TIMESTAMP
 * );
 * 
 * Warnings:
 * - Property source initialization happens early in Spring lifecycle
 * - Don't rely on other beans in property source creation
 * - Handle failures gracefully (network issues, DB down)
 * - Cache properties to avoid excessive external calls
 * - Consider performance impact of external property sources
 * - Secure sensitive property sources (encryption, auth)
 * - Monitor property source health
 * - Test fallback scenarios when source unavailable
 * - Be cautious with refresh behavior
 * - Document property precedence/priority
 * 
 * Best Practices:
 * - Use higher priority for environment-specific sources
 * - Implement caching for expensive property sources
 * - Provide sensible defaults
 * - Log property source registration
 * - Validate property values
 * - Implement circuit breakers for external sources
 * - Use connection pooling for database sources
 * - Encrypt sensitive properties
 * - Document custom property sources
 * - Monitor property access patterns
 */
@SpringBootApplication
public class PropertySourcePattern {

    public static void main(String[] args) {
        SpringApplication.run(PropertySourcePattern.class, args);
    }

    // ============================================
    // Example 1: Database Property Source
    // ============================================
    
    /**
     * Load properties from database table.
     * Useful for multi-tenant configuration.
     */
    public static class DatabasePropertySource extends PropertySource<Map<String, Object>> {
        
        private final Map<String, Object> properties;
        
        public DatabasePropertySource(String name, DataSource dataSource) {
            super(name);
            this.properties = loadPropertiesFromDatabase(dataSource);
        }
        
        public DatabasePropertySource(String name, Map<String, Object> properties) {
            super(name);
            this.properties = properties;
        }
        
        @Override
        public Object getProperty(String name) {
            return properties.get(name);
        }
        
        private Map<String, Object> loadPropertiesFromDatabase(DataSource dataSource) {
            Map<String, Object> props = new HashMap<>();
            
            // Simulate database query
            // In real implementation:
            // try (Connection conn = dataSource.getConnection()) {
            //     String sql = "SELECT property_key, property_value FROM application_properties";
            //     try (Statement stmt = conn.createStatement()) {
            //         ResultSet rs = stmt.executeQuery(sql);
            //         while (rs.next()) {
            //             props.put(rs.getString("property_key"), rs.getString("property_value"));
            //         }
            //     }
            // }
            
            // Simulated data
            props.put("app.database.url", "jdbc:mysql://localhost:3306/mydb");
            props.put("app.database.username", "dbuser");
            props.put("app.database.max-connections", "20");
            
            System.out.println("DatabasePropertySource loaded " + props.size() + " properties");
            
            return props;
        }
        
        public Map<String, Object> getAllProperties() {
            return new HashMap<>(properties);
        }
    }

    // ============================================
    // Example 2: REST API Property Source
    // ============================================
    
    /**
     * Fetch properties from external REST API.
     */
    public static class RestApiPropertySource extends PropertySource<Map<String, Object>> {
        
        private final Map<String, Object> properties;
        private final String apiUrl;
        
        public RestApiPropertySource(String name, String apiUrl) {
            super(name);
            this.apiUrl = apiUrl;
            this.properties = fetchPropertiesFromApi();
        }
        
        @Override
        public Object getProperty(String name) {
            return properties.get(name);
        }
        
        private Map<String, Object> fetchPropertiesFromApi() {
            Map<String, Object> props = new HashMap<>();
            
            // In real implementation, would use RestTemplate or WebClient:
            // RestTemplate restTemplate = new RestTemplate();
            // ResponseEntity<Map> response = restTemplate.getForEntity(apiUrl, Map.class);
            // return response.getBody();
            
            // Simulated API response
            props.put("app.api.endpoint", "https://api.example.com");
            props.put("app.api.timeout", "5000");
            props.put("app.api.retry-count", "3");
            
            System.out.println("RestApiPropertySource loaded " + props.size() + " properties from " + apiUrl);
            
            return props;
        }
        
        public void refresh() {
            properties.clear();
            properties.putAll(fetchPropertiesFromApi());
            System.out.println("RestApiPropertySource refreshed");
        }
    }

    // ============================================
    // Example 3: Cached Property Source
    // ============================================
    
    /**
     * Cache properties with TTL to reduce external calls.
     */
    public static class CachedPropertySource extends PropertySource<Map<String, Object>> {
        
        private final PropertySource<?> delegate;
        private final Map<String, CachedValue> cache = new ConcurrentHashMap<>();
        private final long ttlMillis;
        
        public CachedPropertySource(String name, PropertySource<?> delegate, long ttlMillis) {
            super(name);
            this.delegate = delegate;
            this.ttlMillis = ttlMillis;
        }
        
        @Override
        public Object getProperty(String name) {
            CachedValue cached = cache.get(name);
            
            if (cached != null && !cached.isExpired()) {
                return cached.value;
            }
            
            Object value = delegate.getProperty(name);
            if (value != null) {
                cache.put(name, new CachedValue(value, System.currentTimeMillis() + ttlMillis));
            }
            
            return value;
        }
        
        public void invalidateCache() {
            cache.clear();
            System.out.println("CachedPropertySource cache invalidated");
        }
        
        public void invalidate(String key) {
            cache.remove(key);
        }
        
        private static class CachedValue {
            final Object value;
            final long expiryTime;
            
            CachedValue(Object value, long expiryTime) {
                this.value = value;
                this.expiryTime = expiryTime;
            }
            
            boolean isExpired() {
                return System.currentTimeMillis() > expiryTime;
            }
        }
    }

    // ============================================
    // Example 4: Multi-Tenant Property Source
    // ============================================
    
    /**
     * Load tenant-specific configuration.
     */
    public static class MultiTenantPropertySource extends PropertySource<Map<String, Object>> {
        
        private final Map<String, Map<String, Object>> tenantProperties = new ConcurrentHashMap<>();
        private final ThreadLocal<String> currentTenant = new ThreadLocal<>();
        
        public MultiTenantPropertySource(String name) {
            super(name);
            initializeTenantProperties();
        }
        
        private void initializeTenantProperties() {
            // Tenant 1 properties
            Map<String, Object> tenant1Props = new HashMap<>();
            tenant1Props.put("app.tenant.name", "Tenant One");
            tenant1Props.put("app.tenant.max-users", "100");
            tenant1Props.put("app.tenant.features.premium", "false");
            tenantProperties.put("tenant1", tenant1Props);
            
            // Tenant 2 properties
            Map<String, Object> tenant2Props = new HashMap<>();
            tenant2Props.put("app.tenant.name", "Tenant Two");
            tenant2Props.put("app.tenant.max-users", "500");
            tenant2Props.put("app.tenant.features.premium", "true");
            tenantProperties.put("tenant2", tenant2Props);
            
            System.out.println("MultiTenantPropertySource initialized with " + 
                tenantProperties.size() + " tenants");
        }
        
        public void setCurrentTenant(String tenantId) {
            currentTenant.set(tenantId);
        }
        
        public String getCurrentTenant() {
            return currentTenant.get();
        }
        
        public void clearCurrentTenant() {
            currentTenant.remove();
        }
        
        @Override
        public Object getProperty(String name) {
            String tenantId = currentTenant.get();
            if (tenantId == null) {
                return null;
            }
            
            Map<String, Object> props = tenantProperties.get(tenantId);
            return props != null ? props.get(name) : null;
        }
        
        public void setTenantProperty(String tenantId, String key, Object value) {
            tenantProperties.computeIfAbsent(tenantId, k -> new HashMap<>())
                .put(key, value);
        }
        
        public Map<String, Object> getTenantProperties(String tenantId) {
            return new HashMap<>(tenantProperties.getOrDefault(tenantId, Collections.emptyMap()));
        }
    }

    // ============================================
    // Example 5: Encrypted Property Source
    // ============================================
    
    /**
     * Decrypt properties before returning.
     */
    public static class EncryptedPropertySource extends PropertySource<Map<String, Object>> {
        
        private final Map<String, Object> encryptedProperties;
        private final String encryptionKey;
        
        public EncryptedPropertySource(String name, Map<String, Object> properties, String encryptionKey) {
            super(name);
            this.encryptedProperties = properties;
            this.encryptionKey = encryptionKey;
        }
        
        @Override
        public Object getProperty(String name) {
            Object value = encryptedProperties.get(name);
            
            if (value instanceof String) {
                String strValue = (String) value;
                if (strValue.startsWith("{encrypted}")) {
                    return decrypt(strValue.substring(11));
                }
            }
            
            return value;
        }
        
        private String decrypt(String encrypted) {
            // Simple demonstration - in real implementation use proper encryption:
            // - AES encryption
            // - Key management service
            // - Proper IV and salt
            
            // Simulated decryption
            return "decrypted_" + encrypted;
        }
        
        public void setEncryptedProperty(String key, String encryptedValue) {
            encryptedProperties.put(key, "{encrypted}" + encryptedValue);
        }
    }

    // ============================================
    // Example 6: Composite Property Source
    // ============================================
    
    /**
     * Combine multiple property sources with fallback.
     */
    public static class CompositePropertySource extends PropertySource<List<PropertySource<?>>> {
        
        private final List<PropertySource<?>> propertySources;
        
        public CompositePropertySource(String name, List<PropertySource<?>> propertySources) {
            super(name);
            this.propertySources = new ArrayList<>(propertySources);
        }
        
        @Override
        public Object getProperty(String name) {
            for (PropertySource<?> source : propertySources) {
                Object value = source.getProperty(name);
                if (value != null) {
                    return value;
                }
            }
            return null;
        }
        
        public void addPropertySource(PropertySource<?> propertySource) {
            propertySources.add(0, propertySource); // Add at beginning (highest priority)
        }
        
        public List<PropertySource<?>> getPropertySources() {
            return new ArrayList<>(propertySources);
        }
    }

    // ============================================
    // Example 7: Property Source Configuration
    // ============================================
    
    /**
     * Register custom property sources with Spring.
     */
    @Configuration
    public static class PropertySourceConfiguration {
        
        private final ConfigurableEnvironment environment;
        
        public PropertySourceConfiguration(ConfigurableEnvironment environment) {
            this.environment = environment;
        }
        
        @PostConstruct
        public void registerCustomPropertySources() {
            System.out.println("=== Registering Custom Property Sources ===");
            
            // Database property source (simulated)
            Map<String, Object> dbProps = new HashMap<>();
            dbProps.put("app.db.message", "From Database");
            DatabasePropertySource dbSource = new DatabasePropertySource("databasePropertySource", dbProps);
            environment.getPropertySources().addFirst(dbSource);
            System.out.println("  Registered: DatabasePropertySource");
            
            // REST API property source
            RestApiPropertySource apiSource = new RestApiPropertySource(
                "restApiPropertySource", 
                "http://config-api.example.com/properties"
            );
            environment.getPropertySources().addLast(apiSource);
            System.out.println("  Registered: RestApiPropertySource");
            
            // Multi-tenant property source
            MultiTenantPropertySource tenantSource = new MultiTenantPropertySource("multiTenantPropertySource");
            environment.getPropertySources().addFirst(tenantSource);
            System.out.println("  Registered: MultiTenantPropertySource");
            
            // Encrypted property source
            Map<String, Object> encProps = new HashMap<>();
            encProps.put("app.secret.api-key", "{encrypted}abc123xyz");
            EncryptedPropertySource encSource = new EncryptedPropertySource(
                "encryptedPropertySource", 
                encProps, 
                "encryption-key"
            );
            environment.getPropertySources().addFirst(encSource);
            System.out.println("  Registered: EncryptedPropertySource");
        }
    }

    // ============================================
    // Example 8: Property Source Manager
    // ============================================
    
    /**
     * Manage and inspect property sources.
     */
    @Component
    public static class PropertySourceManager {
        
        private final ConfigurableEnvironment environment;
        
        public PropertySourceManager(ConfigurableEnvironment environment) {
            this.environment = environment;
        }
        
        public List<String> listPropertySources() {
            List<String> names = new ArrayList<>();
            environment.getPropertySources().forEach(ps -> names.add(ps.getName()));
            return names;
        }
        
        public Map<String, Object> getPropertySourceDetails(String name) {
            PropertySource<?> ps = environment.getPropertySources().get(name);
            if (ps == null) {
                return Collections.emptyMap();
            }
            
            Map<String, Object> details = new HashMap<>();
            details.put("name", ps.getName());
            details.put("class", ps.getClass().getSimpleName());
            
            if (ps instanceof DatabasePropertySource) {
                DatabasePropertySource dbPs = (DatabasePropertySource) ps;
                details.put("properties", dbPs.getAllProperties());
            } else if (ps instanceof MapPropertySource) {
                MapPropertySource mapPs = (MapPropertySource) ps;
                details.put("properties", mapPs.getSource());
            }
            
            return details;
        }
        
        public Map<String, Object> getAllPropertySources() {
            Map<String, Object> all = new LinkedHashMap<>();
            
            environment.getPropertySources().forEach(ps -> {
                all.put(ps.getName(), getPropertySourceDetails(ps.getName()));
            });
            
            return all;
        }
    }

    // ============================================
    // Example 9: Property Source Controller
    // ============================================
    
    /**
     * REST endpoints to inspect property sources.
     */
    @RestController
    @RequestMapping("/property-sources")
    public static class PropertySourceController {
        
        private final PropertySourceManager manager;
        private final ConfigurableEnvironment environment;
        
        public PropertySourceController(PropertySourceManager manager,
                                       ConfigurableEnvironment environment) {
            this.manager = manager;
            this.environment = environment;
        }
        
        @GetMapping
        public List<String> listPropertySources() {
            return manager.listPropertySources();
        }
        
        @GetMapping("/{name}")
        public Map<String, Object> getPropertySource(@org.springframework.web.bind.annotation.PathVariable String name) {
            return manager.getPropertySourceDetails(name);
        }
        
        @GetMapping("/all")
        public Map<String, Object> getAllPropertySources() {
            return manager.getAllPropertySources();
        }
        
        @GetMapping("/property/{key}")
        public Map<String, Object> resolveProperty(@org.springframework.web.bind.annotation.PathVariable String key) {
            Map<String, Object> result = new HashMap<>();
            result.put("key", key);
            result.put("value", environment.getProperty(key));
            result.put("resolvedFrom", findPropertySource(key));
            return result;
        }
        
        private String findPropertySource(String key) {
            for (PropertySource<?> ps : environment.getPropertySources()) {
                if (ps.getProperty(key) != null) {
                    return ps.getName();
                }
            }
            return "Not found";
        }
    }
}
