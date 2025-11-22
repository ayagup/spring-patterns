package com.example.database.connection;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Connection Pooling Pattern
 * 
 * Purpose:
 * - Reuses database connections instead of creating new ones for each request
 * - Improves application performance by reducing connection overhead
 * - Manages connection lifecycle, validation, and resource cleanup
 * - Prevents connection leaks and database resource exhaustion
 * 
 * Key Components:
 * 1. HikariCP - High-performance JDBC connection pool (default in Spring Boot)
 * 2. Pool configuration - Size, timeout, validation settings
 * 3. Connection management - Acquire, release, validation
 * 4. Monitoring - Pool metrics and health checks
 * 
 * When to Use:
 * - All production database applications
 * - High-concurrency scenarios
 * - Applications with frequent database operations
 * - Microservices requiring efficient resource usage
 * 
 * Benefits:
 * - Reduced connection creation overhead
 * - Better resource utilization
 * - Improved application throughput
 * - Connection validation and leak detection
 * - Configurable pool size and behavior
 */
@SpringBootApplication
public class ConnectionPoolingPattern {

    public static void main(String[] args) {
        SpringApplication.run(ConnectionPoolingPattern.class, args);
        System.out.println("Connection Pooling Pattern Application Started!");
        System.out.println("Visit: http://localhost:8080/api/pool/stats");
        System.out.println("Visit: http://localhost:8080/api/pool/test-query");
    }

    /**
     * HikariCP Configuration
     * HikariCP is the default connection pool in Spring Boot 2.x+
     */
    @Configuration
    public static class HikariPoolConfiguration {

        /**
         * Basic HikariCP DataSource with common pool settings
         */
        @Bean
        public DataSource dataSource() {
            HikariConfig config = new HikariConfig();
            
            // Database connection details
            config.setJdbcUrl("jdbc:h2:mem:testdb");
            config.setUsername("sa");
            config.setPassword("");
            config.setDriverClassName("org.h2.Driver");
            
            // Pool sizing
            config.setMinimumIdle(5);                    // Minimum idle connections
            config.setMaximumPoolSize(20);               // Maximum pool size
            config.setIdleTimeout(600000);               // 10 minutes
            config.setMaxLifetime(1800000);              // 30 minutes
            
            // Connection behavior
            config.setConnectionTimeout(30000);          // 30 seconds
            config.setValidationTimeout(5000);           // 5 seconds
            config.setLeakDetectionThreshold(60000);     // 60 seconds
            
            // Connection testing
            config.setConnectionTestQuery("SELECT 1");
            
            // Pool name for monitoring
            config.setPoolName("HikariCP-Primary-Pool");
            
            // Performance optimizations
            config.setAutoCommit(true);
            config.setConnectionInitSql("SELECT 1");
            
            // Additional properties
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("useServerPrepStmts", "true");
            
            return new HikariDataSource(config);
        }

        /**
         * Alternative: Configuration from application.properties
         */
        @Bean
        @ConfigurationProperties(prefix = "spring.datasource.hikari")
        public HikariConfig hikariConfigFromProperties() {
            return new HikariConfig();
        }
    }

    /**
     * Advanced Pool Configuration Examples
     */
    @Configuration
    public static class AdvancedPoolConfiguration {

        /**
         * High-throughput configuration for busy applications
         */
        public DataSource highThroughputDataSource() {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:postgresql://localhost:5432/mydb");
            config.setUsername("user");
            config.setPassword("password");
            
            // Larger pool for high concurrency
            config.setMinimumIdle(10);
            config.setMaximumPoolSize(50);
            
            // Aggressive connection recycling
            config.setMaxLifetime(900000);              // 15 minutes
            config.setIdleTimeout(300000);              // 5 minutes
            
            // Fast connection acquisition
            config.setConnectionTimeout(10000);          // 10 seconds
            
            config.setPoolName("HikariCP-HighThroughput");
            
            return new HikariDataSource(config);
        }

        /**
         * Low-latency configuration for real-time applications
         */
        public DataSource lowLatencyDataSource() {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:mysql://localhost:3306/mydb");
            config.setUsername("user");
            config.setPassword("password");
            
            // Maintain warm connections
            config.setMinimumIdle(20);
            config.setMaximumPoolSize(30);
            
            // Keep connections fresh
            config.setMaxLifetime(1200000);             // 20 minutes
            config.setIdleTimeout(600000);              // 10 minutes
            
            // Fail fast
            config.setConnectionTimeout(5000);           // 5 seconds
            config.setValidationTimeout(3000);           // 3 seconds
            
            config.setPoolName("HikariCP-LowLatency");
            
            return new HikariDataSource(config);
        }

        /**
         * Resource-constrained configuration for limited environments
         */
        public DataSource resourceConstrainedDataSource() {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:h2:mem:smalldb");
            config.setUsername("sa");
            config.setPassword("");
            
            // Smaller pool
            config.setMinimumIdle(2);
            config.setMaximumPoolSize(5);
            
            // Longer connection reuse
            config.setMaxLifetime(3600000);             // 1 hour
            config.setIdleTimeout(1800000);             // 30 minutes
            
            config.setPoolName("HikariCP-ResourceConstrained");
            
            return new HikariDataSource(config);
        }
    }

    /**
     * Connection Pool Monitoring Service
     */
    @Service
    public static class PoolMonitoringService {

        private final HikariDataSource dataSource;

        public PoolMonitoringService(DataSource dataSource) {
            this.dataSource = (HikariDataSource) dataSource;
        }

        /**
         * Get current pool statistics
         */
        public Map<String, Object> getPoolStats() {
            Map<String, Object> stats = new HashMap<>();
            
            if (dataSource.getHikariPoolMXBean() != null) {
                stats.put("poolName", dataSource.getPoolName());
                stats.put("activeConnections", dataSource.getHikariPoolMXBean().getActiveConnections());
                stats.put("idleConnections", dataSource.getHikariPoolMXBean().getIdleConnections());
                stats.put("totalConnections", dataSource.getHikariPoolMXBean().getTotalConnections());
                stats.put("threadsAwaitingConnection", dataSource.getHikariPoolMXBean().getThreadsAwaitingConnection());
                stats.put("maximumPoolSize", dataSource.getMaximumPoolSize());
                stats.put("minimumIdle", dataSource.getMinimumIdle());
            }
            
            return stats;
        }

        /**
         * Check pool health
         */
        public boolean isPoolHealthy() {
            try {
                if (dataSource.getHikariPoolMXBean() != null) {
                    int active = dataSource.getHikariPoolMXBean().getActiveConnections();
                    int total = dataSource.getHikariPoolMXBean().getTotalConnections();
                    int waiting = dataSource.getHikariPoolMXBean().getThreadsAwaitingConnection();
                    
                    // Pool is unhealthy if too many connections are active or threads are waiting
                    return waiting == 0 && active < total * 0.9;
                }
            } catch (Exception e) {
                return false;
            }
            return true;
        }

        /**
         * Soft evict idle connections
         */
        public void evictIdleConnections() {
            if (dataSource.getHikariPoolMXBean() != null) {
                dataSource.getHikariPoolMXBean().softEvictConnections();
            }
        }

        /**
         * Suspend pool (for maintenance)
         */
        public void suspendPool() {
            if (dataSource.getHikariPoolMXBean() != null) {
                dataSource.getHikariPoolMXBean().suspendPool();
            }
        }

        /**
         * Resume pool (after maintenance)
         */
        public void resumePool() {
            if (dataSource.getHikariPoolMXBean() != null) {
                dataSource.getHikariPoolMXBean().resumePool();
            }
        }
    }

    /**
     * Database Service using connection pool
     */
    @Service
    public static class DatabaseService {

        private final JdbcTemplate jdbcTemplate;
        private final DataSource dataSource;

        public DatabaseService(JdbcTemplate jdbcTemplate, DataSource dataSource) {
            this.jdbcTemplate = jdbcTemplate;
            this.dataSource = dataSource;
            initializeDatabase();
        }

        private void initializeDatabase() {
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT, " +
                    "username VARCHAR(50), " +
                    "email VARCHAR(100))");
            
            // Insert sample data
            jdbcTemplate.update("INSERT INTO users (username, email) VALUES (?, ?)", 
                    "john_doe", "john@example.com");
            jdbcTemplate.update("INSERT INTO users (username, email) VALUES (?, ?)", 
                    "jane_smith", "jane@example.com");
        }

        /**
         * Example: Get all users (uses connection from pool)
         */
        public List<Map<String, Object>> getAllUsers() {
            return jdbcTemplate.queryForList("SELECT * FROM users");
        }

        /**
         * Example: Manual connection management
         */
        public void demonstrateManualConnection() throws SQLException {
            // Get connection from pool
            try (Connection connection = dataSource.getConnection()) {
                System.out.println("Got connection from pool: " + connection);
                
                // Use connection
                connection.setAutoCommit(false);
                
                try {
                    // Execute queries
                    connection.prepareStatement("SELECT COUNT(*) FROM users").executeQuery();
                    
                    connection.commit();
                } catch (Exception e) {
                    connection.rollback();
                    throw e;
                }
                
                // Connection automatically returned to pool when try-with-resources closes
            }
        }

        /**
         * Simulate high load scenario
         */
        public void simulateHighLoad(int requests) {
            for (int i = 0; i < requests; i++) {
                jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Integer.class);
            }
        }
    }

    /**
     * REST Controller for pool management
     */
    @RestController
    @RequestMapping("/api/pool")
    public static class PoolController {

        private final PoolMonitoringService monitoringService;
        private final DatabaseService databaseService;

        public PoolController(PoolMonitoringService monitoringService, 
                            DatabaseService databaseService) {
            this.monitoringService = monitoringService;
            this.databaseService = databaseService;
        }

        @GetMapping("/stats")
        public Map<String, Object> getPoolStats() {
            return monitoringService.getPoolStats();
        }

        @GetMapping("/health")
        public Map<String, Object> checkHealth() {
            Map<String, Object> response = new HashMap<>();
            response.put("healthy", monitoringService.isPoolHealthy());
            response.put("stats", monitoringService.getPoolStats());
            return response;
        }

        @GetMapping("/test-query")
        public Map<String, Object> testQuery() {
            Map<String, Object> response = new HashMap<>();
            response.put("users", databaseService.getAllUsers());
            response.put("poolStats", monitoringService.getPoolStats());
            return response;
        }

        @GetMapping("/evict-idle")
        public String evictIdleConnections() {
            monitoringService.evictIdleConnections();
            return "Idle connections evicted";
        }

        @GetMapping("/load-test")
        public Map<String, Object> loadTest() {
            Map<String, Object> response = new HashMap<>();
            
            long startTime = System.currentTimeMillis();
            databaseService.simulateHighLoad(100);
            long endTime = System.currentTimeMillis();
            
            response.put("requests", 100);
            response.put("duration", (endTime - startTime) + "ms");
            response.put("poolStats", monitoringService.getPoolStats());
            
            return response;
        }
    }

    /**
     * Configuration Properties Example
     * 
     * application.properties:
     * 
     * spring.datasource.hikari.pool-name=MyHikariPool
     * spring.datasource.hikari.minimum-idle=5
     * spring.datasource.hikari.maximum-pool-size=20
     * spring.datasource.hikari.idle-timeout=600000
     * spring.datasource.hikari.max-lifetime=1800000
     * spring.datasource.hikari.connection-timeout=30000
     * spring.datasource.hikari.validation-timeout=5000
     * spring.datasource.hikari.leak-detection-threshold=60000
     * spring.datasource.hikari.connection-test-query=SELECT 1
     * spring.datasource.hikari.auto-commit=true
     * 
     * # MySQL specific optimizations
     * spring.datasource.hikari.data-source-properties.cachePrepStmts=true
     * spring.datasource.hikari.data-source-properties.prepStmtCacheSize=250
     * spring.datasource.hikari.data-source-properties.prepStmtCacheSqlLimit=2048
     * spring.datasource.hikari.data-source-properties.useServerPrepStmts=true
     */
}
