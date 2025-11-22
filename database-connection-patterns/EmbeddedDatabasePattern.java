package com.example.database.connection;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.DatabasePopulator;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Embedded Database Pattern
 * 
 * Purpose:
 * - Use in-memory database for development, testing, and demos
 * - Quick setup without external database dependencies
 * - Self-contained database lifecycle
 * - Automatic cleanup when application stops
 * 
 * Supported Embedded Databases:
 * 1. H2 - Most popular, MySQL/PostgreSQL compatible modes
 * 2. HSQLDB - HyperSQL Database, lightweight and fast
 * 3. Derby - Apache Derby, robust and standards-compliant
 * 
 * When to Use:
 * - Unit and integration testing
 * - Development environment
 * - Proof of concept / demos
 * - Temporary data storage
 * - CI/CD pipeline testing
 * 
 * When NOT to Use:
 * - Production environments
 * - Data persistence requirements
 * - High-volume applications
 * - Multi-instance deployments
 * 
 * Benefits:
 * - Zero configuration
 * - Fast startup and execution
 * - No external dependencies
 * - Automatic schema creation
 * - Perfect for testing
 */
@SpringBootApplication
public class EmbeddedDatabasePattern {

    public static void main(String[] args) {
        SpringApplication.run(EmbeddedDatabasePattern.class, args);
        System.out.println("Embedded Database Pattern Application Started!");
        System.out.println("Visit: http://localhost:8080/api/embedded/customers");
        System.out.println("Visit: http://localhost:8080/api/embedded/db-info");
    }

    /**
     * H2 Embedded Database Configuration
     */
    @Configuration
    @Profile("h2")
    public static class H2EmbeddedDatabaseConfig {

        /**
         * Method 1: Using EmbeddedDatabaseBuilder (Recommended)
         */
        @Bean
        public DataSource h2DataSource() {
            return new EmbeddedDatabaseBuilder()
                    .setType(EmbeddedDatabaseType.H2)
                    .setName("h2db")
                    .addScript("classpath:schema.sql")
                    .addScript("classpath:data.sql")
                    .build();
        }

        /**
         * Method 2: H2 with console enabled
         */
        public DataSource h2WithConsole() {
            EmbeddedDatabaseBuilder builder = new EmbeddedDatabaseBuilder();
            EmbeddedDatabase db = builder
                    .setType(EmbeddedDatabaseType.H2)
                    .setName("h2db;MODE=MySQL;DB_CLOSE_DELAY=-1")
                    .addScript("schema.sql")
                    .build();
            
            return db;
        }

        /**
         * Method 3: H2 with custom properties
         */
        public DataSource h2CustomProperties() {
            return new EmbeddedDatabaseBuilder()
                    .setType(EmbeddedDatabaseType.H2)
                    .setName("customh2;MODE=PostgreSQL;DATABASE_TO_UPPER=false")
                    .generateUniqueName(false)
                    .build();
        }
    }

    /**
     * HSQLDB Embedded Database Configuration
     */
    @Configuration
    @Profile("hsqldb")
    public static class HsqldbEmbeddedDatabaseConfig {

        /**
         * HSQLDB in-memory database
         */
        @Bean
        public DataSource hsqldbDataSource() {
            return new EmbeddedDatabaseBuilder()
                    .setType(EmbeddedDatabaseType.HSQL)
                    .setName("hsqldb")
                    .addScript("classpath:schema.sql")
                    .addScript("classpath:data.sql")
                    .build();
        }

        /**
         * HSQLDB with custom configuration
         */
        public DataSource hsqldbCustom() {
            return new EmbeddedDatabaseBuilder()
                    .setType(EmbeddedDatabaseType.HSQL)
                    .setName("customhsqldb")
                    .generateUniqueName(true)
                    .addDefaultScripts()
                    .build();
        }
    }

    /**
     * Derby Embedded Database Configuration
     */
    @Configuration
    @Profile("derby")
    public static class DerbyEmbeddedDatabaseConfig {

        /**
         * Apache Derby in-memory database
         */
        @Bean
        public DataSource derbyDataSource() {
            return new EmbeddedDatabaseBuilder()
                    .setType(EmbeddedDatabaseType.DERBY)
                    .setName("derbydb")
                    .addScript("classpath:schema.sql")
                    .addScript("classpath:data.sql")
                    .build();
        }
    }

    /**
     * Default Embedded Database Configuration (H2)
     */
    @Configuration
    public static class DefaultEmbeddedDatabaseConfig {

        /**
         * Simple embedded database with inline schema
         */
        @Bean
        public DataSource dataSource() {
            return new EmbeddedDatabaseBuilder()
                    .setType(EmbeddedDatabaseType.H2)
                    .setName("embeddeddb")
                    .ignoreFailedDrops(true)
                    .build();
        }

        /**
         * Database initializer for custom schema
         */
        @Bean
        public DataSourceInitializer dataSourceInitializer(DataSource dataSource) {
            DataSourceInitializer initializer = new DataSourceInitializer();
            initializer.setDataSource(dataSource);
            initializer.setDatabasePopulator(databasePopulator());
            return initializer;
        }

        private DatabasePopulator databasePopulator() {
            ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            populator.setContinueOnError(true);
            populator.setIgnoreFailedDrops(true);
            // Add scripts if available
            // populator.addScript(new ClassPathResource("schema.sql"));
            // populator.addScript(new ClassPathResource("data.sql"));
            return populator;
        }
    }

    /**
     * Multiple Embedded Databases Configuration
     */
    @Configuration
    public static class MultipleEmbeddedDatabasesConfig {

        /**
         * Primary embedded database
         */
        @Bean(name = "primaryEmbeddedDb")
        public EmbeddedDatabase primaryDatabase() {
            return new EmbeddedDatabaseBuilder()
                    .setName("primarydb")
                    .setType(EmbeddedDatabaseType.H2)
                    .build();
        }

        /**
         * Test embedded database
         */
        @Bean(name = "testEmbeddedDb")
        public EmbeddedDatabase testDatabase() {
            return new EmbeddedDatabaseBuilder()
                    .setName("testdb")
                    .setType(EmbeddedDatabaseType.H2)
                    .build();
        }

        /**
         * Cache embedded database
         */
        @Bean(name = "cacheEmbeddedDb")
        public EmbeddedDatabase cacheDatabase() {
            return new EmbeddedDatabaseBuilder()
                    .setName("cachedb")
                    .setType(EmbeddedDatabaseType.HSQL)
                    .build();
        }
    }

    /**
     * Advanced Embedded Database Configuration
     */
    @Configuration
    public static class AdvancedEmbeddedConfig {

        /**
         * Embedded database with unique name generation
         */
        public DataSource uniqueNameDatabase() {
            return new EmbeddedDatabaseBuilder()
                    .setType(EmbeddedDatabaseType.H2)
                    .generateUniqueName(true)
                    .build();
        }

        /**
         * Embedded database with script-based initialization
         */
        public DataSource scriptBasedDatabase() {
            return new EmbeddedDatabaseBuilder()
                    .setType(EmbeddedDatabaseType.H2)
                    .setName("scriptdb")
                    .addScript("create-schema.sql")
                    .addScript("insert-data.sql")
                    .addScript("create-indexes.sql")
                    .ignoreFailedDrops(true)
                    .continueOnError(false)
                    .build();
        }

        /**
         * H2 with MySQL compatibility mode
         */
        public DataSource h2MySQLMode() {
            return new EmbeddedDatabaseBuilder()
                    .setType(EmbeddedDatabaseType.H2)
                    .setName("h2mysql;MODE=MySQL;DATABASE_TO_LOWER=TRUE")
                    .build();
        }

        /**
         * H2 with PostgreSQL compatibility mode
         */
        public DataSource h2PostgreSQLMode() {
            return new EmbeddedDatabaseBuilder()
                    .setType(EmbeddedDatabaseType.H2)
                    .setName("h2postgres;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE")
                    .build();
        }
    }

    /**
     * Embedded Database Information Service
     */
    @Service
    public static class EmbeddedDatabaseInfoService {

        private final DataSource dataSource;

        public EmbeddedDatabaseInfoService(DataSource dataSource) {
            this.dataSource = dataSource;
        }

        /**
         * Get database metadata
         */
        public Map<String, Object> getDatabaseInfo() {
            Map<String, Object> info = new HashMap<>();
            
            try (var connection = dataSource.getConnection()) {
                var metaData = connection.getMetaData();
                
                info.put("databaseProductName", metaData.getDatabaseProductName());
                info.put("databaseProductVersion", metaData.getDatabaseProductVersion());
                info.put("databaseMajorVersion", metaData.getDatabaseMajorVersion());
                info.put("databaseMinorVersion", metaData.getDatabaseMinorVersion());
                info.put("driverName", metaData.getDriverName());
                info.put("driverVersion", metaData.getDriverVersion());
                info.put("url", metaData.getURL());
                info.put("username", metaData.getUserName());
                info.put("isEmbedded", true);
                info.put("isInMemory", metaData.getURL().contains(":mem:"));
                info.put("supportsTransactions", metaData.supportsTransactions());
                info.put("supportsBatchUpdates", metaData.supportsBatchUpdates());
                
                // Get schema info
                var catalogs = metaData.getCatalogs();
                java.util.List<String> catalogList = new java.util.ArrayList<>();
                while (catalogs.next()) {
                    catalogList.add(catalogs.getString(1));
                }
                info.put("catalogs", catalogList);
                
            } catch (Exception e) {
                info.put("error", e.getMessage());
            }
            
            return info;
        }

        /**
         * List all tables in database
         */
        public List<String> listTables() {
            java.util.List<String> tables = new java.util.ArrayList<>();
            
            try (var connection = dataSource.getConnection()) {
                var metaData = connection.getMetaData();
                var rs = metaData.getTables(null, null, "%", new String[]{"TABLE"});
                
                while (rs.next()) {
                    tables.add(rs.getString("TABLE_NAME"));
                }
            } catch (Exception e) {
                System.err.println("Error listing tables: " + e.getMessage());
            }
            
            return tables;
        }
    }

    /**
     * Customer Service (Example business logic)
     */
    @Service
    public static class CustomerService {

        private final JdbcTemplate jdbcTemplate;

        public CustomerService(DataSource dataSource) {
            this.jdbcTemplate = new JdbcTemplate(dataSource);
            initializeDatabase();
        }

        private void initializeDatabase() {
            // Create schema
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS customers (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT, " +
                    "name VARCHAR(100) NOT NULL, " +
                    "email VARCHAR(100) NOT NULL UNIQUE, " +
                    "phone VARCHAR(20), " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            
            // Insert sample data
            jdbcTemplate.update(
                    "INSERT INTO customers (name, email, phone) VALUES (?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE name = name",
                    "Alice Johnson", "alice@example.com", "555-0101");
            
            jdbcTemplate.update(
                    "INSERT INTO customers (name, email, phone) VALUES (?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE name = name",
                    "Bob Smith", "bob@example.com", "555-0102");
            
            jdbcTemplate.update(
                    "INSERT INTO customers (name, email, phone) VALUES (?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE name = name",
                    "Carol White", "carol@example.com", "555-0103");
        }

        public List<Map<String, Object>> getAllCustomers() {
            return jdbcTemplate.queryForList("SELECT * FROM customers ORDER BY id");
        }

        public Map<String, Object> getCustomer(int id) {
            return jdbcTemplate.queryForMap("SELECT * FROM customers WHERE id = ?", id);
        }

        public void addCustomer(String name, String email, String phone) {
            jdbcTemplate.update("INSERT INTO customers (name, email, phone) VALUES (?, ?, ?)",
                    name, email, phone);
        }

        public void updateCustomer(int id, String name, String email, String phone) {
            jdbcTemplate.update("UPDATE customers SET name = ?, email = ?, phone = ? WHERE id = ?",
                    name, email, phone, id);
        }

        public void deleteCustomer(int id) {
            jdbcTemplate.update("DELETE FROM customers WHERE id = ?", id);
        }

        public int getCustomerCount() {
            return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM customers", Integer.class);
        }
    }

    /**
     * REST Controller
     */
    @RestController
    @RequestMapping("/api/embedded")
    public static class EmbeddedDatabaseController {

        private final EmbeddedDatabaseInfoService infoService;
        private final CustomerService customerService;

        public EmbeddedDatabaseController(EmbeddedDatabaseInfoService infoService,
                                         CustomerService customerService) {
            this.infoService = infoService;
            this.customerService = customerService;
        }

        @GetMapping("/db-info")
        public Map<String, Object> getDatabaseInfo() {
            Map<String, Object> response = infoService.getDatabaseInfo();
            response.put("tables", infoService.listTables());
            return response;
        }

        @GetMapping("/customers")
        public List<Map<String, Object>> getAllCustomers() {
            return customerService.getAllCustomers();
        }

        @GetMapping("/customers/{id}")
        public Map<String, Object> getCustomer(@PathVariable int id) {
            return customerService.getCustomer(id);
        }

        @PostMapping("/customers")
        public Map<String, Object> addCustomer(@RequestBody Map<String, String> customer) {
            customerService.addCustomer(
                    customer.get("name"),
                    customer.get("email"),
                    customer.get("phone")
            );
            return Map.of("status", "created", "count", customerService.getCustomerCount());
        }

        @PutMapping("/customers/{id}")
        public Map<String, Object> updateCustomer(@PathVariable int id,
                                                  @RequestBody Map<String, String> customer) {
            customerService.updateCustomer(
                    id,
                    customer.get("name"),
                    customer.get("email"),
                    customer.get("phone")
            );
            return Map.of("status", "updated");
        }

        @DeleteMapping("/customers/{id}")
        public Map<String, Object> deleteCustomer(@PathVariable int id) {
            customerService.deleteCustomer(id);
            return Map.of("status", "deleted", "count", customerService.getCustomerCount());
        }

        @GetMapping("/stats")
        public Map<String, Object> getStats() {
            Map<String, Object> stats = new HashMap<>();
            stats.put("customerCount", customerService.getCustomerCount());
            stats.put("tables", infoService.listTables());
            return stats;
        }
    }

    /**
     * Configuration in application.properties:
     * 
     * # H2 Embedded Database (Default)
     * spring.datasource.url=jdbc:h2:mem:testdb
     * spring.datasource.driverClassName=org.h2.Driver
     * spring.datasource.username=sa
     * spring.datasource.password=
     * 
     * # H2 Console (for debugging)
     * spring.h2.console.enabled=true
     * spring.h2.console.path=/h2-console
     * spring.h2.console.settings.web-allow-others=false
     * 
     * # HSQLDB
     * spring.datasource.url=jdbc:hsqldb:mem:testdb
     * spring.datasource.driverClassName=org.hsqldb.jdbc.JDBCDriver
     * 
     * # Derby
     * spring.datasource.url=jdbc:derby:memory:testdb;create=true
     * spring.datasource.driverClassName=org.apache.derby.jdbc.EmbeddedDriver
     * 
     * # Schema initialization
     * spring.sql.init.mode=always
     * spring.sql.init.schema-locations=classpath:schema.sql
     * spring.sql.init.data-locations=classpath:data.sql
     * 
     * # JPA with embedded database
     * spring.jpa.hibernate.ddl-auto=create-drop
     * spring.jpa.show-sql=true
     * spring.jpa.properties.hibernate.format_sql=true
     */
}
