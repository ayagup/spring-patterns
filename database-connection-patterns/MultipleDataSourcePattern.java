package com.example.database.connection;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Multiple DataSource Pattern
 * 
 * Purpose:
 * - Connect to multiple databases from single application
 * - Separate databases for different purposes (operational, analytics, reporting)
 * - Multi-tenant architectures with database per tenant
 * - Microservices data integration
 * 
 * Use Cases:
 * 1. Legacy system integration - accessing old and new databases
 * 2. CQRS - separate read and write databases
 * 3. Multi-tenancy - different database per tenant
 * 4. Data warehouse - operational DB + analytics DB
 * 5. Microservices - service owns multiple bounded contexts
 * 
 * When to Use:
 * - Application needs data from multiple databases
 * - Separating operational and analytical workloads
 * - Multi-tenant SaaS applications
 * - Migrating between database systems
 * - Integrating with external systems
 * 
 * Best Practices:
 * - Use @Primary for most common DataSource
 * - Name beans clearly (customerDb, orderDb, etc.)
 * - Configure separate transaction managers
 * - Use @Qualifier to inject specific DataSource
 * - Consider connection pool sizing carefully
 * 
 * Challenges:
 * - Distributed transactions (consider Saga pattern instead)
 * - Complex transaction management
 * - Connection pool resource management
 * - Configuration complexity
 */
@SpringBootApplication
@EnableTransactionManagement
public class MultipleDataSourcePattern {

    public static void main(String[] args) {
        SpringApplication.run(MultipleDataSourcePattern.class, args);
        System.out.println("Multiple DataSource Pattern Application Started!");
        System.out.println("Visit: http://localhost:8080/api/multi-ds/summary");
    }

    /**
     * Basic Multiple DataSource Configuration
     */
    @Configuration
    public static class MultipleDataSourceConfig {

        /**
         * Primary DataSource (default for most operations)
         */
        @Bean(name = "primaryDataSource")
        @Primary
        @ConfigurationProperties(prefix = "spring.datasource.primary")
        public DataSource primaryDataSource() {
            return DataSourceBuilder.create()
                    .type(HikariDataSource.class)
                    .build();
        }

        /**
         * Secondary DataSource (for specific operations)
         */
        @Bean(name = "secondaryDataSource")
        @ConfigurationProperties(prefix = "spring.datasource.secondary")
        public DataSource secondaryDataSource() {
            return DataSourceBuilder.create()
                    .type(HikariDataSource.class)
                    .build();
        }

        /**
         * Primary JdbcTemplate
         */
        @Bean(name = "primaryJdbcTemplate")
        @Primary
        public JdbcTemplate primaryJdbcTemplate(@Qualifier("primaryDataSource") DataSource dataSource) {
            return new JdbcTemplate(dataSource);
        }

        /**
         * Secondary JdbcTemplate
         */
        @Bean(name = "secondaryJdbcTemplate")
        public JdbcTemplate secondaryJdbcTemplate(@Qualifier("secondaryDataSource") DataSource dataSource) {
            return new JdbcTemplate(dataSource);
        }

        /**
         * Primary Transaction Manager
         */
        @Bean(name = "primaryTransactionManager")
        @Primary
        public PlatformTransactionManager primaryTransactionManager(
                @Qualifier("primaryDataSource") DataSource dataSource) {
            return new DataSourceTransactionManager(dataSource);
        }

        /**
         * Secondary Transaction Manager
         */
        @Bean(name = "secondaryTransactionManager")
        public PlatformTransactionManager secondaryTransactionManager(
                @Qualifier("secondaryDataSource") DataSource dataSource) {
            return new DataSourceTransactionManager(dataSource);
        }
    }

    /**
     * Multi-Purpose DataSource Configuration
     * Separate databases for different purposes
     */
    @Configuration
    public static class MultiPurposeDataSourceConfig {

        /**
         * Operational Database (OLTP - transactional)
         */
        @Bean(name = "operationalDataSource")
        @ConfigurationProperties(prefix = "datasource.operational")
        public DataSource operationalDataSource() {
            HikariDataSource dataSource = new HikariDataSource();
            dataSource.setPoolName("Operational-Pool");
            // Optimized for writes and transactions
            dataSource.setMaximumPoolSize(20);
            dataSource.setMinimumIdle(5);
            dataSource.setConnectionTimeout(30000);
            return dataSource;
        }

        /**
         * Analytics Database (OLAP - analytical)
         */
        @Bean(name = "analyticsDataSource")
        @ConfigurationProperties(prefix = "datasource.analytics")
        public DataSource analyticsDataSource() {
            HikariDataSource dataSource = new HikariDataSource();
            dataSource.setPoolName("Analytics-Pool");
            // Optimized for complex queries
            dataSource.setMaximumPoolSize(10);
            dataSource.setMinimumIdle(2);
            dataSource.setConnectionTimeout(60000);
            dataSource.setIdleTimeout(300000);
            return dataSource;
        }

        /**
         * Reporting Database (read-only replicas)
         */
        @Bean(name = "reportingDataSource")
        @ConfigurationProperties(prefix = "datasource.reporting")
        public DataSource reportingDataSource() {
            HikariDataSource dataSource = new HikariDataSource();
            dataSource.setPoolName("Reporting-Pool");
            // Optimized for read-heavy workload
            dataSource.setMaximumPoolSize(15);
            dataSource.setReadOnly(true);
            return dataSource;
        }

        /**
         * Archive Database (cold storage)
         */
        @Bean(name = "archiveDataSource")
        @ConfigurationProperties(prefix = "datasource.archive")
        public DataSource archiveDataSource() {
            HikariDataSource dataSource = new HikariDataSource();
            dataSource.setPoolName("Archive-Pool");
            // Smaller pool for infrequent access
            dataSource.setMaximumPoolSize(5);
            dataSource.setMinimumIdle(1);
            return dataSource;
        }

        // JdbcTemplates for each database
        @Bean(name = "operationalJdbcTemplate")
        public JdbcTemplate operationalJdbcTemplate(@Qualifier("operationalDataSource") DataSource ds) {
            return new JdbcTemplate(ds);
        }

        @Bean(name = "analyticsJdbcTemplate")
        public JdbcTemplate analyticsJdbcTemplate(@Qualifier("analyticsDataSource") DataSource ds) {
            return new JdbcTemplate(ds);
        }

        @Bean(name = "reportingJdbcTemplate")
        public JdbcTemplate reportingJdbcTemplate(@Qualifier("reportingDataSource") DataSource ds) {
            return new JdbcTemplate(ds);
        }

        @Bean(name = "archiveJdbcTemplate")
        public JdbcTemplate archiveJdbcTemplate(@Qualifier("archiveDataSource") DataSource ds) {
            return new JdbcTemplate(ds);
        }
    }

    /**
     * Multi-Tenant DataSource Configuration
     * Separate database per tenant
     */
    @Configuration
    public static class MultiTenantDataSourceConfig {

        @Bean(name = "tenant1DataSource")
        @ConfigurationProperties(prefix = "datasource.tenant1")
        public DataSource tenant1DataSource() {
            return DataSourceBuilder.create().build();
        }

        @Bean(name = "tenant2DataSource")
        @ConfigurationProperties(prefix = "datasource.tenant2")
        public DataSource tenant2DataSource() {
            return DataSourceBuilder.create().build();
        }

        @Bean(name = "tenant3DataSource")
        @ConfigurationProperties(prefix = "datasource.tenant3")
        public DataSource tenant3DataSource() {
            return DataSourceBuilder.create().build();
        }

        /**
         * Tenant DataSource Registry
         */
        @Bean
        public Map<String, DataSource> tenantDataSources(
                @Qualifier("tenant1DataSource") DataSource tenant1,
                @Qualifier("tenant2DataSource") DataSource tenant2,
                @Qualifier("tenant3DataSource") DataSource tenant3) {
            
            Map<String, DataSource> dataSources = new HashMap<>();
            dataSources.put("tenant1", tenant1);
            dataSources.put("tenant2", tenant2);
            dataSources.put("tenant3", tenant3);
            return dataSources;
        }
    }

    /**
     * Operational Database Service
     */
    @Service
    public static class OperationalService {

        private final JdbcTemplate jdbcTemplate;

        public OperationalService(@Qualifier("primaryJdbcTemplate") JdbcTemplate jdbcTemplate) {
            this.jdbcTemplate = jdbcTemplate;
            initializeDatabase();
        }

        private void initializeDatabase() {
            try {
                jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS transactions (" +
                        "id INT PRIMARY KEY AUTO_INCREMENT, " +
                        "transaction_id VARCHAR(50), " +
                        "amount DECIMAL(10, 2), " +
                        "status VARCHAR(20), " +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
                
                // Insert sample data
                jdbcTemplate.update("INSERT INTO transactions (transaction_id, amount, status) VALUES (?, ?, ?)",
                        "TXN-001", 100.00, "COMPLETED");
                jdbcTemplate.update("INSERT INTO transactions (transaction_id, amount, status) VALUES (?, ?, ?)",
                        "TXN-002", 250.50, "PENDING");
            } catch (Exception e) {
                System.err.println("Operational DB initialization: " + e.getMessage());
            }
        }

        public List<Map<String, Object>> getAllTransactions() {
            return jdbcTemplate.queryForList("SELECT * FROM transactions ORDER BY id DESC");
        }

        public int getTransactionCount() {
            return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM transactions", Integer.class);
        }

        public void addTransaction(String transactionId, double amount, String status) {
            jdbcTemplate.update("INSERT INTO transactions (transaction_id, amount, status) VALUES (?, ?, ?)",
                    transactionId, amount, status);
        }
    }

    /**
     * Analytics Database Service
     */
    @Service
    public static class AnalyticsService {

        private final JdbcTemplate jdbcTemplate;

        public AnalyticsService(@Qualifier("secondaryJdbcTemplate") JdbcTemplate jdbcTemplate) {
            this.jdbcTemplate = jdbcTemplate;
            initializeDatabase();
        }

        private void initializeDatabase() {
            try {
                jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS metrics (" +
                        "id INT PRIMARY KEY AUTO_INCREMENT, " +
                        "metric_name VARCHAR(100), " +
                        "metric_value DECIMAL(15, 2), " +
                        "period VARCHAR(20), " +
                        "recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
                
                // Insert sample data
                jdbcTemplate.update("INSERT INTO metrics (metric_name, metric_value, period) VALUES (?, ?, ?)",
                        "daily_revenue", 5000.00, "2024-01");
                jdbcTemplate.update("INSERT INTO metrics (metric_name, metric_value, period) VALUES (?, ?, ?)",
                        "active_users", 1250.00, "2024-01");
            } catch (Exception e) {
                System.err.println("Analytics DB initialization: " + e.getMessage());
            }
        }

        public List<Map<String, Object>> getAllMetrics() {
            return jdbcTemplate.queryForList("SELECT * FROM metrics ORDER BY recorded_at DESC");
        }

        public Map<String, Object> getMetricsSummary() {
            Map<String, Object> summary = new HashMap<>();
            summary.put("totalMetrics", jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM metrics", Integer.class));
            summary.put("metrics", getAllMetrics());
            return summary;
        }
    }

    /**
     * Data Integration Service
     * Coordinates operations across multiple databases
     */
    @Service
    public static class DataIntegrationService {

        private final OperationalService operationalService;
        private final AnalyticsService analyticsService;

        public DataIntegrationService(OperationalService operationalService,
                                     AnalyticsService analyticsService) {
            this.operationalService = operationalService;
            this.analyticsService = analyticsService;
        }

        /**
         * Get consolidated data from all databases
         */
        public Map<String, Object> getConsolidatedData() {
            Map<String, Object> data = new HashMap<>();
            
            // Data from operational database
            data.put("operational", Map.of(
                "transactions", operationalService.getAllTransactions(),
                "count", operationalService.getTransactionCount()
            ));
            
            // Data from analytics database
            data.put("analytics", analyticsService.getMetricsSummary());
            
            return data;
        }

        /**
         * Synchronize data between databases
         * Note: This is simplified - production would use CDC or messaging
         */
        public void synchronizeData() {
            // Example: Copy transaction summaries to analytics
            // In production, use Change Data Capture (CDC) or message queues
            System.out.println("Synchronizing data across databases...");
        }
    }

    /**
     * DataSource Monitoring Service
     */
    @Service
    public static class DataSourceMonitoringService {

        private final DataSource primaryDataSource;
        private final DataSource secondaryDataSource;

        public DataSourceMonitoringService(
                @Qualifier("primaryDataSource") DataSource primaryDataSource,
                @Qualifier("secondaryDataSource") DataSource secondaryDataSource) {
            this.primaryDataSource = primaryDataSource;
            this.secondaryDataSource = secondaryDataSource;
        }

        public Map<String, Object> getDataSourcesInfo() {
            Map<String, Object> info = new HashMap<>();
            info.put("primary", getDataSourceInfo(primaryDataSource, "Primary"));
            info.put("secondary", getDataSourceInfo(secondaryDataSource, "Secondary"));
            return info;
        }

        private Map<String, Object> getDataSourceInfo(DataSource dataSource, String name) {
            Map<String, Object> info = new HashMap<>();
            info.put("name", name);
            info.put("class", dataSource.getClass().getSimpleName());
            
            try (var connection = dataSource.getConnection()) {
                var metaData = connection.getMetaData();
                info.put("database", metaData.getDatabaseProductName());
                info.put("version", metaData.getDatabaseProductVersion());
                info.put("url", metaData.getURL());
                info.put("connected", true);
            } catch (Exception e) {
                info.put("connected", false);
                info.put("error", e.getMessage());
            }
            
            return info;
        }
    }

    /**
     * REST Controller
     */
    @RestController
    @RequestMapping("/api/multi-ds")
    public static class MultipleDataSourceController {

        private final DataIntegrationService integrationService;
        private final OperationalService operationalService;
        private final AnalyticsService analyticsService;
        private final DataSourceMonitoringService monitoringService;

        public MultipleDataSourceController(DataIntegrationService integrationService,
                                           OperationalService operationalService,
                                           AnalyticsService analyticsService,
                                           DataSourceMonitoringService monitoringService) {
            this.integrationService = integrationService;
            this.operationalService = operationalService;
            this.analyticsService = analyticsService;
            this.monitoringService = monitoringService;
        }

        @GetMapping("/summary")
        public Map<String, Object> getSummary() {
            return integrationService.getConsolidatedData();
        }

        @GetMapping("/operational/transactions")
        public List<Map<String, Object>> getTransactions() {
            return operationalService.getAllTransactions();
        }

        @GetMapping("/analytics/metrics")
        public List<Map<String, Object>> getMetrics() {
            return analyticsService.getAllMetrics();
        }

        @GetMapping("/datasources/info")
        public Map<String, Object> getDataSourcesInfo() {
            return monitoringService.getDataSourcesInfo();
        }

        @GetMapping("/sync")
        public Map<String, Object> syncData() {
            integrationService.synchronizeData();
            return Map.of("status", "synchronized", "timestamp", System.currentTimeMillis());
        }
    }

    /**
     * Configuration in application.properties:
     * 
     * # Primary DataSource
     * spring.datasource.primary.jdbc-url=jdbc:h2:mem:primarydb
     * spring.datasource.primary.username=sa
     * spring.datasource.primary.password=
     * spring.datasource.primary.driver-class-name=org.h2.Driver
     * spring.datasource.primary.hikari.maximum-pool-size=20
     * spring.datasource.primary.hikari.minimum-idle=5
     * 
     * # Secondary DataSource
     * spring.datasource.secondary.jdbc-url=jdbc:h2:mem:secondarydb
     * spring.datasource.secondary.username=sa
     * spring.datasource.secondary.password=
     * spring.datasource.secondary.driver-class-name=org.h2.Driver
     * spring.datasource.secondary.hikari.maximum-pool-size=10
     * spring.datasource.secondary.hikari.minimum-idle=2
     * 
     * # Operational DataSource
     * datasource.operational.jdbc-url=jdbc:mysql://localhost:3306/operational
     * datasource.operational.username=root
     * datasource.operational.password=password
     * 
     * # Analytics DataSource
     * datasource.analytics.jdbc-url=jdbc:postgresql://localhost:5432/analytics
     * datasource.analytics.username=postgres
     * datasource.analytics.password=password
     * 
     * # Reporting DataSource
     * datasource.reporting.jdbc-url=jdbc:mysql://replica.example.com:3306/reporting
     * datasource.reporting.username=readonly
     * datasource.reporting.password=password
     * 
     * # Archive DataSource
     * datasource.archive.jdbc-url=jdbc:postgresql://archive.example.com:5432/archive
     * datasource.archive.username=archiver
     * datasource.archive.password=password
     * 
     * # Tenant DataSources
     * datasource.tenant1.jdbc-url=jdbc:mysql://localhost:3306/tenant1_db
     * datasource.tenant1.username=tenant1
     * datasource.tenant1.password=password1
     * 
     * datasource.tenant2.jdbc-url=jdbc:mysql://localhost:3306/tenant2_db
     * datasource.tenant2.username=tenant2
     * datasource.tenant2.password=password2
     * 
     * datasource.tenant3.jdbc-url=jdbc:mysql://localhost:3306/tenant3_db
     * datasource.tenant3.username=tenant3
     * datasource.tenant3.password=password3
     */
}
