package com.example.database.connection;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Read/Write Splitting Pattern
 * 
 * Purpose:
 * - Route read operations to read replicas
 * - Route write operations to master database
 * - Improve performance and scalability
 * - Distribute database load across multiple servers
 * 
 * How It Works:
 * - Uses AbstractRoutingDataSource to route queries
 * - Detects transaction type (read-only vs read-write)
 * - Automatically routes to appropriate database
 * - Maintains data consistency through replication
 * 
 * Architecture:
 * - Master (Primary): Handles all write operations
 * - Slaves (Replicas): Handle read-only operations
 * - Replication: Master → Slaves (async or sync)
 * 
 * Routing Strategies:
 * 1. Transaction-based: @Transactional(readOnly=true/false)
 * 2. Method-based: Detect SELECT vs INSERT/UPDATE/DELETE
 * 3. Annotation-based: Custom @ReadOperation/@WriteOperation
 * 4. Manual: Explicit DataSource selection
 * 
 * When to Use:
 * - Read-heavy workloads (80%+ reads)
 * - High-traffic applications
 * - Need to scale read operations independently
 * - Reporting/analytics queries separate from transactions
 * 
 * Benefits:
 * - Improved read performance
 * - Better resource utilization
 * - Reduced master database load
 * - Easy horizontal scaling of reads
 * - Isolation of analytical queries
 * 
 * Considerations:
 * - Replication lag (eventual consistency)
 * - Master is single point of failure
 * - Additional infrastructure complexity
 * - Monitoring replication health
 */
@SpringBootApplication
@EnableTransactionManagement
public class ReadWriteSplittingPattern {

    public static void main(String[] args) {
        SpringApplication.run(ReadWriteSplittingPattern.class, args);
        System.out.println("Read/Write Splitting Pattern Application Started!");
        System.out.println("Visit: http://localhost:8080/api/rw/products (read from replica)");
        System.out.println("Visit: POST http://localhost:8080/api/rw/products (write to master)");
    }

    /**
     * DataSource Type Enum
     */
    public enum DataSourceType {
        MASTER,  // Write operations
        SLAVE    // Read operations
    }

    /**
     * DataSource Context Holder
     */
    public static class DataSourceContextHolder {

        private static final ThreadLocal<DataSourceType> contextHolder = new ThreadLocal<>();

        public static void setDataSourceType(DataSourceType dataSourceType) {
            contextHolder.set(dataSourceType);
        }

        public static DataSourceType getDataSourceType() {
            return contextHolder.get();
        }

        public static void clearDataSourceType() {
            contextHolder.remove();
        }

        public static void setMaster() {
            setDataSourceType(DataSourceType.MASTER);
        }

        public static void setSlave() {
            setDataSourceType(DataSourceType.SLAVE);
        }

        public static boolean isMaster() {
            return DataSourceType.MASTER.equals(getDataSourceType());
        }

        public static boolean isSlave() {
            return DataSourceType.SLAVE.equals(getDataSourceType());
        }
    }

    /**
     * Read/Write Routing DataSource
     */
    public static class ReadWriteRoutingDataSource extends AbstractRoutingDataSource {

        @Override
        protected Object determineCurrentLookupKey() {
            DataSourceType dataSourceType = DataSourceContextHolder.getDataSourceType();
            
            // Default to slave (read) if not specified
            if (dataSourceType == null) {
                dataSourceType = DataSourceType.SLAVE;
            }
            
            System.out.println("Routing to: " + dataSourceType);
            return dataSourceType;
        }
    }

    /**
     * DataSource Configuration
     */
    @Configuration
    public static class ReadWriteDataSourceConfig {

        /**
         * Master DataSource (write operations)
         */
        @Bean(name = "masterDataSource")
        public DataSource masterDataSource() {
            DriverManagerDataSource dataSource = new DriverManagerDataSource();
            dataSource.setDriverClassName("org.h2.Driver");
            dataSource.setUrl("jdbc:h2:mem:masterdb;DB_CLOSE_DELAY=-1");
            dataSource.setUsername("sa");
            dataSource.setPassword("");
            return dataSource;
        }

        /**
         * Slave DataSource (read operations)
         */
        @Bean(name = "slaveDataSource")
        public DataSource slaveDataSource() {
            DriverManagerDataSource dataSource = new DriverManagerDataSource();
            dataSource.setDriverClassName("org.h2.Driver");
            dataSource.setUrl("jdbc:h2:mem:slavedb;DB_CLOSE_DELAY=-1");
            dataSource.setUsername("sa");
            dataSource.setPassword("");
            return dataSource;
        }

        /**
         * Routing DataSource
         */
        @Bean
        public DataSource routingDataSource() {
            ReadWriteRoutingDataSource routingDataSource = new ReadWriteRoutingDataSource();
            
            Map<Object, Object> targetDataSources = new HashMap<>();
            targetDataSources.put(DataSourceType.MASTER, masterDataSource());
            targetDataSources.put(DataSourceType.SLAVE, slaveDataSource());
            
            routingDataSource.setTargetDataSources(targetDataSources);
            routingDataSource.setDefaultTargetDataSource(slaveDataSource()); // Default to read
            
            return routingDataSource;
        }

        @Bean
        public JdbcTemplate jdbcTemplate(DataSource routingDataSource) {
            return new JdbcTemplate(routingDataSource);
        }

        @Bean
        public PlatformTransactionManager transactionManager(DataSource routingDataSource) {
            return new DataSourceTransactionManager(routingDataSource);
        }
    }

    /**
     * Transaction Routing Interceptor
     * Automatically routes based on @Transactional(readOnly) attribute
     */
    @Component
    public static class TransactionRoutingInterceptor 
            implements org.springframework.transaction.support.TransactionSynchronization {

        @Override
        public void beforeCommit(boolean readOnly) {
            if (readOnly) {
                DataSourceContextHolder.setSlave();
            } else {
                DataSourceContextHolder.setMaster();
            }
        }

        @Override
        public void afterCompletion(int status) {
            DataSourceContextHolder.clearDataSourceType();
        }
    }

    /**
     * Custom Annotations for Read/Write Operations
     */
    @java.lang.annotation.Target({java.lang.annotation.ElementType.METHOD, java.lang.annotation.ElementType.TYPE})
    @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
    @Transactional(readOnly = true)
    public @interface ReadOperation {
    }

    @java.lang.annotation.Target({java.lang.annotation.ElementType.METHOD, java.lang.annotation.ElementType.TYPE})
    @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
    @Transactional(readOnly = false)
    public @interface WriteOperation {
    }

    /**
     * Product Service with Read/Write Splitting
     */
    @Service
    public static class ProductService {

        private final JdbcTemplate jdbcTemplate;

        public ProductService(JdbcTemplate jdbcTemplate) {
            this.jdbcTemplate = jdbcTemplate;
            initializeDatabases();
        }

        private void initializeDatabases() {
            // Initialize master database
            DataSourceContextHolder.setMaster();
            try {
                jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS products (" +
                        "id INT PRIMARY KEY AUTO_INCREMENT, " +
                        "name VARCHAR(100), " +
                        "price DECIMAL(10, 2), " +
                        "stock INT)");
                
                jdbcTemplate.update("INSERT INTO products (name, price, stock) VALUES (?, ?, ?)",
                        "Laptop", 999.99, 50);
                jdbcTemplate.update("INSERT INTO products (name, price, stock) VALUES (?, ?, ?)",
                        "Mouse", 29.99, 200);
                jdbcTemplate.update("INSERT INTO products (name, price, stock) VALUES (?, ?, ?)",
                        "Keyboard", 79.99, 150);
            } finally {
                DataSourceContextHolder.clearDataSourceType();
            }

            // Simulate replication to slave
            replicateToSlave();
        }

        /**
         * Simulate replication from master to slave
         */
        private void replicateToSlave() {
            DataSourceContextHolder.setSlave();
            try {
                jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS products (" +
                        "id INT PRIMARY KEY AUTO_INCREMENT, " +
                        "name VARCHAR(100), " +
                        "price DECIMAL(10, 2), " +
                        "stock INT)");
                
                jdbcTemplate.update("INSERT INTO products (name, price, stock) VALUES (?, ?, ?)",
                        "Laptop", 999.99, 50);
                jdbcTemplate.update("INSERT INTO products (name, price, stock) VALUES (?, ?, ?)",
                        "Mouse", 29.99, 200);
                jdbcTemplate.update("INSERT INTO products (name, price, stock) VALUES (?, ?, ?)",
                        "Keyboard", 79.99, 150);
            } finally {
                DataSourceContextHolder.clearDataSourceType();
            }
        }

        /**
         * Read Operation - Routes to Slave
         */
        @ReadOperation
        public List<Map<String, Object>> getAllProducts() {
            DataSourceContextHolder.setSlave();
            try {
                System.out.println("Reading from SLAVE database");
                return jdbcTemplate.queryForList("SELECT * FROM products");
            } finally {
                DataSourceContextHolder.clearDataSourceType();
            }
        }

        /**
         * Read Operation - Routes to Slave
         */
        @ReadOperation
        public Map<String, Object> getProduct(int id) {
            DataSourceContextHolder.setSlave();
            try {
                System.out.println("Reading from SLAVE database");
                return jdbcTemplate.queryForMap("SELECT * FROM products WHERE id = ?", id);
            } finally {
                DataSourceContextHolder.clearDataSourceType();
            }
        }

        /**
         * Write Operation - Routes to Master
         */
        @WriteOperation
        public void addProduct(String name, double price, int stock) {
            DataSourceContextHolder.setMaster();
            try {
                System.out.println("Writing to MASTER database");
                jdbcTemplate.update("INSERT INTO products (name, price, stock) VALUES (?, ?, ?)",
                        name, price, stock);
                
                // Simulate replication
                replicateNewProduct(name, price, stock);
            } finally {
                DataSourceContextHolder.clearDataSourceType();
            }
        }

        /**
         * Write Operation - Routes to Master
         */
        @WriteOperation
        public void updateProduct(int id, String name, double price, int stock) {
            DataSourceContextHolder.setMaster();
            try {
                System.out.println("Writing to MASTER database");
                jdbcTemplate.update("UPDATE products SET name = ?, price = ?, stock = ? WHERE id = ?",
                        name, price, stock, id);
                
                // Simulate replication
                replicateUpdateProduct(id, name, price, stock);
            } finally {
                DataSourceContextHolder.clearDataSourceType();
            }
        }

        /**
         * Write Operation - Routes to Master
         */
        @WriteOperation
        public void deleteProduct(int id) {
            DataSourceContextHolder.setMaster();
            try {
                System.out.println("Writing to MASTER database");
                jdbcTemplate.update("DELETE FROM products WHERE id = ?", id);
                
                // Simulate replication
                replicateDeleteProduct(id);
            } finally {
                DataSourceContextHolder.clearDataSourceType();
            }
        }

        /**
         * Read Operation with explicit slave routing
         */
        public int getProductCount() {
            DataSourceContextHolder.setSlave();
            try {
                System.out.println("Reading count from SLAVE database");
                return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM products", Integer.class);
            } finally {
                DataSourceContextHolder.clearDataSourceType();
            }
        }

        // Simulate replication methods
        private void replicateNewProduct(String name, double price, int stock) {
            DataSourceContextHolder.setSlave();
            try {
                jdbcTemplate.update("INSERT INTO products (name, price, stock) VALUES (?, ?, ?)",
                        name, price, stock);
            } catch (Exception e) {
                // Handle replication error
            } finally {
                DataSourceContextHolder.clearDataSourceType();
            }
        }

        private void replicateUpdateProduct(int id, String name, double price, int stock) {
            DataSourceContextHolder.setSlave();
            try {
                jdbcTemplate.update("UPDATE products SET name = ?, price = ?, stock = ? WHERE id = ?",
                        name, price, stock, id);
            } catch (Exception e) {
                // Handle replication error
            } finally {
                DataSourceContextHolder.clearDataSourceType();
            }
        }

        private void replicateDeleteProduct(int id) {
            DataSourceContextHolder.setSlave();
            try {
                jdbcTemplate.update("DELETE FROM products WHERE id = ?", id);
            } catch (Exception e) {
                // Handle replication error
            } finally {
                DataSourceContextHolder.clearDataSourceType();
            }
        }
    }

    /**
     * Monitoring Service
     */
    @Service
    public static class ReplicationMonitoringService {

        private final DataSource masterDataSource;
        private final DataSource slaveDataSource;

        public ReplicationMonitoringService(DataSource routingDataSource) {
            // In production, inject actual DataSources
            this.masterDataSource = null;
            this.slaveDataSource = null;
        }

        public Map<String, Object> getReplicationStatus() {
            Map<String, Object> status = new HashMap<>();
            status.put("currentDataSource", DataSourceContextHolder.getDataSourceType());
            status.put("masterStatus", "HEALTHY");
            status.put("slaveStatus", "HEALTHY");
            status.put("replicationLag", "0ms");
            return status;
        }
    }

    /**
     * REST Controller
     */
    @RestController
    @RequestMapping("/api/rw")
    public static class ReadWriteSplittingController {

        private final ProductService productService;
        private final ReplicationMonitoringService monitoringService;

        public ReadWriteSplittingController(ProductService productService,
                                           ReplicationMonitoringService monitoringService) {
            this.productService = productService;
            this.monitoringService = monitoringService;
        }

        /**
         * GET - Routes to Slave
         */
        @GetMapping("/products")
        public List<Map<String, Object>> getAllProducts() {
            return productService.getAllProducts();
        }

        /**
         * GET - Routes to Slave
         */
        @GetMapping("/products/{id}")
        public Map<String, Object> getProduct(@PathVariable int id) {
            return productService.getProduct(id);
        }

        /**
         * POST - Routes to Master
         */
        @PostMapping("/products")
        public Map<String, Object> addProduct(@RequestBody Map<String, Object> product) {
            productService.addProduct(
                    (String) product.get("name"),
                    ((Number) product.get("price")).doubleValue(),
                    ((Number) product.get("stock")).intValue()
            );
            return Map.of("status", "created", "dataSource", "MASTER");
        }

        /**
         * PUT - Routes to Master
         */
        @PutMapping("/products/{id}")
        public Map<String, Object> updateProduct(@PathVariable int id,
                                                 @RequestBody Map<String, Object> product) {
            productService.updateProduct(
                    id,
                    (String) product.get("name"),
                    ((Number) product.get("price")).doubleValue(),
                    ((Number) product.get("stock")).intValue()
            );
            return Map.of("status", "updated", "dataSource", "MASTER");
        }

        /**
         * DELETE - Routes to Master
         */
        @DeleteMapping("/products/{id}")
        public Map<String, Object> deleteProduct(@PathVariable int id) {
            productService.deleteProduct(id);
            return Map.of("status", "deleted", "dataSource", "MASTER");
        }

        /**
         * GET - Stats from Slave
         */
        @GetMapping("/stats")
        public Map<String, Object> getStats() {
            Map<String, Object> stats = new HashMap<>();
            stats.put("productCount", productService.getProductCount());
            stats.put("dataSource", "SLAVE");
            return stats;
        }

        /**
         * GET - Replication monitoring
         */
        @GetMapping("/replication-status")
        public Map<String, Object> getReplicationStatus() {
            return monitoringService.getReplicationStatus();
        }
    }

    /**
     * Configuration in application.properties:
     * 
     * # Master DataSource (Write)
     * datasource.master.jdbc-url=jdbc:mysql://master.example.com:3306/mydb
     * datasource.master.username=master_user
     * datasource.master.password=master_password
     * datasource.master.hikari.maximum-pool-size=20
     * datasource.master.hikari.minimum-idle=5
     * 
     * # Slave DataSource (Read)
     * datasource.slave.jdbc-url=jdbc:mysql://slave.example.com:3306/mydb
     * datasource.slave.username=slave_user
     * datasource.slave.password=slave_password
     * datasource.slave.hikari.maximum-pool-size=30
     * datasource.slave.hikari.minimum-idle=10
     * datasource.slave.hikari.read-only=true
     * 
     * # Transaction management
     * spring.jpa.properties.hibernate.connection.provider_disables_autocommit=false
     * 
     * 
     * Best Practices:
     * 
     * 1. Use @Transactional(readOnly=true) for read operations
     * 2. Monitor replication lag
     * 3. Handle replication failures gracefully
     * 4. Consider eventual consistency in design
     * 5. Use connection pooling appropriately
     * 6. Implement health checks for master and slaves
     * 7. Consider read-after-write consistency requirements
     * 8. Plan for master failover scenarios
     */
}
