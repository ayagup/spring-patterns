package com.example.database.connection;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DataSource Routing Pattern
 * 
 * Purpose:
 * - Dynamically route database operations to different DataSources
 * - Implement runtime DataSource selection based on context
 * - Support multi-tenancy, read/write splitting, sharding
 * - Enable flexible database routing strategies
 * 
 * How It Works:
 * - Extends AbstractRoutingDataSource
 * - Implements determineCurrentLookupKey() to select DataSource
 * - Uses ThreadLocal to store routing context
 * - Resolves DataSource at runtime based on criteria
 * 
 * Routing Strategies:
 * 1. Tenant-based - Route by tenant ID
 * 2. Operation-based - Route by operation type (read/write)
 * 3. User-based - Route by user/customer
 * 4. Time-based - Route by time of day
 * 5. Load-based - Route based on load balancing
 * 6. Region-based - Route by geographical region
 * 
 * When to Use:
 * - Multi-tenant applications
 * - Read/write splitting scenarios
 * - Database sharding implementations
 * - A/B testing with different databases
 * - Gradual migration between databases
 * 
 * Benefits:
 * - Dynamic DataSource selection
 * - No code changes for routing logic
 * - Transparent to business logic
 * - Flexible routing strategies
 */
@SpringBootApplication
public class DataSourceRoutingPattern {

    public static void main(String[] args) {
        SpringApplication.run(DataSourceRoutingPattern.class, args);
        System.out.println("DataSource Routing Pattern Application Started!");
        System.out.println("Visit: http://localhost:8080/api/routing/tenant1/data");
        System.out.println("Visit: http://localhost:8080/api/routing/tenant2/data");
    }

    /**
     * DataSource Routing Context
     * ThreadLocal-based context holder for routing decisions
     */
    public static class DataSourceContextHolder {

        private static final ThreadLocal<String> contextHolder = new ThreadLocal<>();

        public static void setDataSourceType(String dataSourceType) {
            contextHolder.set(dataSourceType);
        }

        public static String getDataSourceType() {
            return contextHolder.get();
        }

        public static void clearDataSourceType() {
            contextHolder.remove();
        }
    }

    /**
     * Dynamic Routing DataSource
     * Routes to different DataSources based on context
     */
    public static class DynamicRoutingDataSource extends AbstractRoutingDataSource {

        @Override
        protected Object determineCurrentLookupKey() {
            String dataSourceType = DataSourceContextHolder.getDataSourceType();
            System.out.println("Routing to DataSource: " + dataSourceType);
            return dataSourceType;
        }

        @Override
        protected DataSource determineTargetDataSource() {
            Object lookupKey = determineCurrentLookupKey();
            if (lookupKey == null) {
                // Default to first available DataSource
                return super.determineTargetDataSource();
            }
            return super.determineTargetDataSource();
        }
    }

    /**
     * Basic Routing Configuration
     */
    @Configuration
    public static class RoutingDataSourceConfiguration {

        /**
         * Tenant 1 DataSource
         */
        @Bean(name = "tenant1DataSource")
        public DataSource tenant1DataSource() {
            DriverManagerDataSource dataSource = new DriverManagerDataSource();
            dataSource.setDriverClassName("org.h2.Driver");
            dataSource.setUrl("jdbc:h2:mem:tenant1db");
            dataSource.setUsername("sa");
            dataSource.setPassword("");
            return dataSource;
        }

        /**
         * Tenant 2 DataSource
         */
        @Bean(name = "tenant2DataSource")
        public DataSource tenant2DataSource() {
            DriverManagerDataSource dataSource = new DriverManagerDataSource();
            dataSource.setDriverClassName("org.h2.Driver");
            dataSource.setUrl("jdbc:h2:mem:tenant2db");
            dataSource.setUsername("sa");
            dataSource.setPassword("");
            return dataSource;
        }

        /**
         * Tenant 3 DataSource
         */
        @Bean(name = "tenant3DataSource")
        public DataSource tenant3DataSource() {
            DriverManagerDataSource dataSource = new DriverManagerDataSource();
            dataSource.setDriverClassName("org.h2.Driver");
            dataSource.setUrl("jdbc:h2:mem:tenant3db");
            dataSource.setUsername("sa");
            dataSource.setPassword("");
            return dataSource;
        }

        /**
         * Default DataSource (fallback)
         */
        @Bean(name = "defaultDataSource")
        public DataSource defaultDataSource() {
            DriverManagerDataSource dataSource = new DriverManagerDataSource();
            dataSource.setDriverClassName("org.h2.Driver");
            dataSource.setUrl("jdbc:h2:mem:defaultdb");
            dataSource.setUsername("sa");
            dataSource.setPassword("");
            return dataSource;
        }

        /**
         * Routing DataSource
         */
        @Bean
        public DataSource routingDataSource() {
            DynamicRoutingDataSource routingDataSource = new DynamicRoutingDataSource();
            
            // Map of DataSource keys to actual DataSources
            Map<Object, Object> targetDataSources = new HashMap<>();
            targetDataSources.put("tenant1", tenant1DataSource());
            targetDataSources.put("tenant2", tenant2DataSource());
            targetDataSources.put("tenant3", tenant3DataSource());
            targetDataSources.put("default", defaultDataSource());
            
            routingDataSource.setTargetDataSources(targetDataSources);
            routingDataSource.setDefaultTargetDataSource(defaultDataSource());
            
            return routingDataSource;
        }

        @Bean
        public JdbcTemplate jdbcTemplate(DataSource routingDataSource) {
            return new JdbcTemplate(routingDataSource);
        }
    }

    /**
     * Advanced Routing Strategies
     */
    @Configuration
    public static class AdvancedRoutingStrategies {

        /**
         * Tenant-based Routing DataSource
         */
        public static class TenantRoutingDataSource extends AbstractRoutingDataSource {
            @Override
            protected Object determineCurrentLookupKey() {
                // Get tenant ID from context (could be from JWT, header, etc.)
                String tenantId = TenantContext.getCurrentTenant();
                return tenantId != null ? "tenant_" + tenantId : "default";
            }
        }

        /**
         * Operation-based Routing DataSource (Read/Write Split)
         */
        public static class OperationRoutingDataSource extends AbstractRoutingDataSource {
            @Override
            protected Object determineCurrentLookupKey() {
                String operation = OperationContext.getCurrentOperation();
                return operation != null ? operation : "read";
            }
        }

        /**
         * User-based Routing DataSource
         */
        public static class UserRoutingDataSource extends AbstractRoutingDataSource {
            @Override
            protected Object determineCurrentLookupKey() {
                Long userId = UserContext.getCurrentUserId();
                if (userId == null) return "default";
                
                // Shard by user ID (e.g., even/odd)
                return userId % 2 == 0 ? "even_shard" : "odd_shard";
            }
        }

        /**
         * Region-based Routing DataSource
         */
        public static class RegionRoutingDataSource extends AbstractRoutingDataSource {
            @Override
            protected Object determineCurrentLookupKey() {
                String region = RegionContext.getCurrentRegion();
                return region != null ? region : "us-east";
            }
        }

        /**
         * Time-based Routing DataSource
         */
        public static class TimeBasedRoutingDataSource extends AbstractRoutingDataSource {
            @Override
            protected Object determineCurrentLookupKey() {
                int hour = java.time.LocalTime.now().getHour();
                // Route to different DB during business hours vs off-hours
                return (hour >= 9 && hour < 17) ? "business_hours" : "off_hours";
            }
        }

        /**
         * Custom Rule-based Routing DataSource
         */
        public static class RuleBasedRoutingDataSource extends AbstractRoutingDataSource {
            
            private final Map<String, RoutingRule> rules = new HashMap<>();
            
            public void addRule(String name, RoutingRule rule) {
                rules.put(name, rule);
            }
            
            @Override
            protected Object determineCurrentLookupKey() {
                for (RoutingRule rule : rules.values()) {
                    if (rule.matches()) {
                        return rule.getTargetDataSource();
                    }
                }
                return "default";
            }
        }
    }

    /**
     * Context Holders for Different Routing Strategies
     */
    public static class TenantContext {
        private static final ThreadLocal<String> currentTenant = new ThreadLocal<>();
        
        public static void setCurrentTenant(String tenant) {
            currentTenant.set(tenant);
        }
        
        public static String getCurrentTenant() {
            return currentTenant.get();
        }
        
        public static void clear() {
            currentTenant.remove();
        }
    }

    public static class OperationContext {
        private static final ThreadLocal<String> currentOperation = new ThreadLocal<>();
        
        public static void setCurrentOperation(String operation) {
            currentOperation.set(operation);
        }
        
        public static String getCurrentOperation() {
            return currentOperation.get();
        }
        
        public static void clear() {
            currentOperation.remove();
        }
    }

    public static class UserContext {
        private static final ThreadLocal<Long> currentUserId = new ThreadLocal<>();
        
        public static void setCurrentUserId(Long userId) {
            currentUserId.set(userId);
        }
        
        public static Long getCurrentUserId() {
            return currentUserId.get();
        }
        
        public static void clear() {
            currentUserId.remove();
        }
    }

    public static class RegionContext {
        private static final ThreadLocal<String> currentRegion = new ThreadLocal<>();
        
        public static void setCurrentRegion(String region) {
            currentRegion.set(region);
        }
        
        public static String getCurrentRegion() {
            return currentRegion.get();
        }
        
        public static void clear() {
            currentRegion.remove();
        }
    }

    /**
     * Routing Rule Interface
     */
    public interface RoutingRule {
        boolean matches();
        String getTargetDataSource();
    }

    /**
     * DataSource Routing Service
     */
    @Service
    public static class RoutingService {

        private final JdbcTemplate jdbcTemplate;

        public RoutingService(JdbcTemplate jdbcTemplate) {
            this.jdbcTemplate = jdbcTemplate;
        }

        /**
         * Initialize tenant databases
         */
        public void initializeTenantData(String tenant) {
            DataSourceContextHolder.setDataSourceType(tenant);
            try {
                jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS data (" +
                        "id INT PRIMARY KEY AUTO_INCREMENT, " +
                        "tenant VARCHAR(50), " +
                        "value VARCHAR(200))");
                
                jdbcTemplate.update("INSERT INTO data (tenant, value) VALUES (?, ?)",
                        tenant, "Data for " + tenant);
            } finally {
                DataSourceContextHolder.clearDataSourceType();
            }
        }

        /**
         * Get data for specific tenant
         */
        public List<Map<String, Object>> getDataForTenant(String tenant) {
            DataSourceContextHolder.setDataSourceType(tenant);
            try {
                return jdbcTemplate.queryForList("SELECT * FROM data");
            } finally {
                DataSourceContextHolder.clearDataSourceType();
            }
        }

        /**
         * Add data to tenant database
         */
        public void addDataToTenant(String tenant, String value) {
            DataSourceContextHolder.setDataSourceType(tenant);
            try {
                jdbcTemplate.update("INSERT INTO data (tenant, value) VALUES (?, ?)",
                        tenant, value);
            } finally {
                DataSourceContextHolder.clearDataSourceType();
            }
        }
    }

    /**
     * Routing Information Service
     */
    @Service
    public static class RoutingInfoService {

        public Map<String, Object> getRoutingInfo() {
            Map<String, Object> info = new HashMap<>();
            info.put("currentDataSource", DataSourceContextHolder.getDataSourceType());
            info.put("availableDataSources", List.of("tenant1", "tenant2", "tenant3", "default"));
            info.put("routingStrategy", "Tenant-based");
            return info;
        }
    }

    /**
     * Database Initialization Component
     */
    @Component
    public static class DatabaseInitializer {

        private final RoutingService routingService;

        public DatabaseInitializer(RoutingService routingService) {
            this.routingService = routingService;
            initialize();
        }

        private void initialize() {
            // Initialize all tenant databases
            routingService.initializeTenantData("tenant1");
            routingService.initializeTenantData("tenant2");
            routingService.initializeTenantData("tenant3");
            routingService.initializeTenantData("default");
            
            System.out.println("Tenant databases initialized");
        }
    }

    /**
     * REST Controller
     */
    @RestController
    @RequestMapping("/api/routing")
    public static class RoutingController {

        private final RoutingService routingService;
        private final RoutingInfoService routingInfoService;

        public RoutingController(RoutingService routingService, 
                                RoutingInfoService routingInfoService) {
            this.routingService = routingService;
            this.routingInfoService = routingInfoService;
        }

        @GetMapping("/{tenant}/data")
        public List<Map<String, Object>> getTenantData(@PathVariable String tenant) {
            return routingService.getDataForTenant(tenant);
        }

        @PostMapping("/{tenant}/data")
        public Map<String, Object> addTenantData(@PathVariable String tenant,
                                                @RequestBody Map<String, String> payload) {
            routingService.addDataToTenant(tenant, payload.get("value"));
            return Map.of("status", "success", "tenant", tenant);
        }

        @GetMapping("/info")
        public Map<String, Object> getRoutingInfo() {
            return routingInfoService.getRoutingInfo();
        }

        @GetMapping("/test-routing")
        public Map<String, Object> testRouting() {
            Map<String, Object> results = new HashMap<>();
            
            // Test each tenant
            for (String tenant : List.of("tenant1", "tenant2", "tenant3")) {
                results.put(tenant, routingService.getDataForTenant(tenant));
            }
            
            return results;
        }
    }

    /**
     * Configuration in application.properties:
     * 
     * # Routing DataSource Configuration
     * # Individual tenant databases would be configured separately
     * 
     * # Tenant 1
     * datasource.tenant1.url=jdbc:mysql://localhost:3306/tenant1_db
     * datasource.tenant1.username=tenant1
     * datasource.tenant1.password=password1
     * 
     * # Tenant 2
     * datasource.tenant2.url=jdbc:mysql://localhost:3306/tenant2_db
     * datasource.tenant2.username=tenant2
     * datasource.tenant2.password=password2
     * 
     * # Tenant 3
     * datasource.tenant3.url=jdbc:postgresql://localhost:5432/tenant3_db
     * datasource.tenant3.username=tenant3
     * datasource.tenant3.password=password3
     * 
     * # Default
     * datasource.default.url=jdbc:h2:mem:defaultdb
     * datasource.default.username=sa
     * datasource.default.password=
     * 
     * # Routing configuration
     * app.routing.strategy=tenant-based
     * app.routing.default-datasource=default
     * 
     * 
     * Usage Examples:
     * 
     * 1. Manual routing:
     *    DataSourceContextHolder.setDataSourceType("tenant1");
     *    // execute queries
     *    DataSourceContextHolder.clearDataSourceType();
     * 
     * 2. Using interceptor/filter:
     *    Extract tenant from JWT/header
     *    Set context before controller execution
     *    Clear context after execution
     * 
     * 3. Using AOP:
     *    @Around annotation on methods
     *    Determine routing based on method parameters
     *    Clean up in finally block
     */
}
