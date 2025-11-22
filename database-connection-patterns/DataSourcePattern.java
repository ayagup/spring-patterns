package com.example.database.connection;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * DataSource Pattern
 * 
 * Purpose:
 * - Provides abstraction for database connection management
 * - Centralizes database configuration and connection logic
 * - Supports different DataSource implementations
 * - Enables connection pooling and resource management
 * 
 * DataSource Types in Spring:
 * 1. DriverManagerDataSource - Simple, no pooling (development only)
 * 2. SimpleDriverDataSource - JDBC 4.0 compliant, no pooling
 * 3. SingleConnectionDataSource - Single shared connection
 * 4. HikariDataSource - Production-grade connection pool
 * 5. Tomcat JDBC DataSource - Alternative pooling solution
 * 6. DBCP2 DataSource - Apache Commons DBCP2 pool
 * 7. C3P0 DataSource - Legacy pooling solution
 * 
 * When to Use:
 * - All database-driven applications
 * - When abstracting database connection details
 * - For testing with different database backends
 * - Managing multiple database connections
 * 
 * Benefits:
 * - Centralized database configuration
 * - Easy switching between databases
 * - Simplified testing with embedded databases
 * - Connection management abstraction
 */
@SpringBootApplication
public class DataSourcePattern {

    public static void main(String[] args) {
        SpringApplication.run(DataSourcePattern.class, args);
        System.out.println("DataSource Pattern Application Started!");
        System.out.println("Visit: http://localhost:8080/api/datasource/info");
        System.out.println("Visit: http://localhost:8080/api/datasource/test-query");
    }

    /**
     * Basic DataSource Configuration
     */
    @Configuration
    public static class BasicDataSourceConfiguration {

        /**
         * 1. DriverManagerDataSource
         * - Simplest implementation
         * - No connection pooling
         * - Creates new connection for each request
         * - Suitable only for development/testing
         */
        @Bean
        public DataSource driverManagerDataSource() {
            DriverManagerDataSource dataSource = new DriverManagerDataSource();
            dataSource.setDriverClassName("org.h2.Driver");
            dataSource.setUrl("jdbc:h2:mem:testdb");
            dataSource.setUsername("sa");
            dataSource.setPassword("");
            
            // Optional: Set connection properties
            Properties connectionProperties = new Properties();
            connectionProperties.setProperty("autoCommit", "true");
            connectionProperties.setProperty("characterEncoding", "UTF-8");
            dataSource.setConnectionProperties(connectionProperties);
            
            return dataSource;
        }

        /**
         * 2. SimpleDriverDataSource
         * - Similar to DriverManagerDataSource
         * - Uses JDBC 4.0 Driver instance directly
         * - No connection pooling
         */
        public DataSource simpleDriverDataSource() throws Exception {
            SimpleDriverDataSource dataSource = new SimpleDriverDataSource();
            
            // Load driver
            Driver driver = (Driver) Class.forName("org.h2.Driver").getDeclaredConstructor().newInstance();
            dataSource.setDriver(driver);
            
            dataSource.setUrl("jdbc:h2:mem:testdb");
            dataSource.setUsername("sa");
            dataSource.setPassword("");
            
            return dataSource;
        }

        /**
         * 3. DataSourceBuilder (Spring Boot way)
         * - Type-safe configuration
         * - Automatically detects connection pool on classpath
         * - Supports HikariCP, Tomcat, DBCP2, etc.
         */
        @Bean
        @Primary
        @ConfigurationProperties(prefix = "spring.datasource")
        public DataSource primaryDataSource() {
            return DataSourceBuilder.create().build();
        }
    }

    /**
     * Multiple DataSource Types Configuration
     */
    @Configuration
    public static class MultipleDataSourceTypesConfig {

        /**
         * H2 In-Memory DataSource
         */
        @Bean
        public DataSource h2DataSource() {
            DriverManagerDataSource dataSource = new DriverManagerDataSource();
            dataSource.setDriverClassName("org.h2.Driver");
            dataSource.setUrl("jdbc:h2:mem:h2db;DB_CLOSE_DELAY=-1");
            dataSource.setUsername("sa");
            dataSource.setPassword("");
            return dataSource;
        }

        /**
         * MySQL DataSource
         */
        public DataSource mysqlDataSource() {
            DriverManagerDataSource dataSource = new DriverManagerDataSource();
            dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
            dataSource.setUrl("jdbc:mysql://localhost:3306/mydb?useSSL=false&serverTimezone=UTC");
            dataSource.setUsername("root");
            dataSource.setPassword("password");
            return dataSource;
        }

        /**
         * PostgreSQL DataSource
         */
        public DataSource postgresDataSource() {
            DriverManagerDataSource dataSource = new DriverManagerDataSource();
            dataSource.setDriverClassName("org.postgresql.Driver");
            dataSource.setUrl("jdbc:postgresql://localhost:5432/mydb");
            dataSource.setUsername("postgres");
            dataSource.setPassword("password");
            return dataSource;
        }

        /**
         * Oracle DataSource
         */
        public DataSource oracleDataSource() {
            DriverManagerDataSource dataSource = new DriverManagerDataSource();
            dataSource.setDriverClassName("oracle.jdbc.OracleDriver");
            dataSource.setUrl("jdbc:oracle:thin:@localhost:1521:orcl");
            dataSource.setUsername("system");
            dataSource.setPassword("password");
            return dataSource;
        }

        /**
         * SQL Server DataSource
         */
        public DataSource sqlServerDataSource() {
            DriverManagerDataSource dataSource = new DriverManagerDataSource();
            dataSource.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            dataSource.setUrl("jdbc:sqlserver://localhost:1433;databaseName=mydb");
            dataSource.setUsername("sa");
            dataSource.setPassword("password");
            return dataSource;
        }
    }

    /**
     * Advanced DataSource Configuration
     */
    @Configuration
    public static class AdvancedDataSourceConfig {

        /**
         * DataSource with custom properties
         */
        public DataSource customPropertiesDataSource() {
            DriverManagerDataSource dataSource = new DriverManagerDataSource();
            dataSource.setDriverClassName("org.h2.Driver");
            dataSource.setUrl("jdbc:h2:mem:customdb");
            dataSource.setUsername("sa");
            dataSource.setPassword("");
            
            // Set custom connection properties
            Properties props = new Properties();
            props.setProperty("MODE", "MySQL");                    // H2 MySQL compatibility mode
            props.setProperty("DATABASE_TO_UPPER", "false");       // Case-sensitive identifiers
            props.setProperty("DB_CLOSE_DELAY", "-1");            // Keep database in memory
            props.setProperty("INIT", "CREATE SCHEMA IF NOT EXISTS myschema"); // Initialization
            
            dataSource.setConnectionProperties(props);
            
            return dataSource;
        }

        /**
         * DataSource with schema initialization
         */
        public DataSource schemaInitializedDataSource() {
            DriverManagerDataSource dataSource = new DriverManagerDataSource();
            dataSource.setDriverClassName("org.h2.Driver");
            dataSource.setUrl("jdbc:h2:mem:schemadb;INIT=RUNSCRIPT FROM 'classpath:schema.sql'");
            dataSource.setUsername("sa");
            dataSource.setPassword("");
            
            return dataSource;
        }

        /**
         * DataSource from properties file
         */
        @Bean
        @ConfigurationProperties(prefix = "custom.datasource")
        public DataSource propertiesBasedDataSource() {
            return DataSourceBuilder.create()
                    .type(DriverManagerDataSource.class)
                    .build();
        }
    }

    /**
     * DataSource Information Service
     */
    @Service
    public static class DataSourceInfoService {

        private final DataSource dataSource;

        public DataSourceInfoService(DataSource dataSource) {
            this.dataSource = dataSource;
        }

        /**
         * Get DataSource metadata
         */
        public Map<String, Object> getDataSourceInfo() {
            Map<String, Object> info = new HashMap<>();
            
            try {
                info.put("dataSourceClass", dataSource.getClass().getSimpleName());
                
                // Get connection metadata
                try (var connection = dataSource.getConnection()) {
                    var metaData = connection.getMetaData();
                    
                    info.put("databaseProductName", metaData.getDatabaseProductName());
                    info.put("databaseProductVersion", metaData.getDatabaseProductVersion());
                    info.put("driverName", metaData.getDriverName());
                    info.put("driverVersion", metaData.getDriverVersion());
                    info.put("url", metaData.getURL());
                    info.put("username", metaData.getUserName());
                    info.put("catalogTerm", metaData.getCatalogTerm());
                    info.put("schemaTerm", metaData.getSchemaTerm());
                    info.put("supportsTransactions", metaData.supportsTransactions());
                    info.put("supportsBatchUpdates", metaData.supportsBatchUpdates());
                    info.put("supportsNamedParameters", metaData.supportsNamedParameters());
                    info.put("defaultTransactionIsolation", 
                            getTransactionIsolationName(metaData.getDefaultTransactionIsolation()));
                }
            } catch (Exception e) {
                info.put("error", e.getMessage());
            }
            
            return info;
        }

        private String getTransactionIsolationName(int level) {
            return switch (level) {
                case java.sql.Connection.TRANSACTION_NONE -> "NONE";
                case java.sql.Connection.TRANSACTION_READ_UNCOMMITTED -> "READ_UNCOMMITTED";
                case java.sql.Connection.TRANSACTION_READ_COMMITTED -> "READ_COMMITTED";
                case java.sql.Connection.TRANSACTION_REPEATABLE_READ -> "REPEATABLE_READ";
                case java.sql.Connection.TRANSACTION_SERIALIZABLE -> "SERIALIZABLE";
                default -> "UNKNOWN";
            };
        }

        /**
         * Test connection
         */
        public boolean testConnection() {
            try (var connection = dataSource.getConnection()) {
                return connection.isValid(5);
            } catch (Exception e) {
                return false;
            }
        }
    }

    /**
     * Database Service
     */
    @Service
    public static class DatabaseService {

        private final JdbcTemplate jdbcTemplate;

        public DatabaseService(DataSource dataSource) {
            this.jdbcTemplate = new JdbcTemplate(dataSource);
            initializeDatabase();
        }

        private void initializeDatabase() {
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS products (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT, " +
                    "name VARCHAR(100), " +
                    "price DECIMAL(10, 2))");
            
            // Insert sample data
            jdbcTemplate.update("INSERT INTO products (name, price) VALUES (?, ?)", 
                    "Laptop", 999.99);
            jdbcTemplate.update("INSERT INTO products (name, price) VALUES (?, ?)", 
                    "Mouse", 29.99);
            jdbcTemplate.update("INSERT INTO products (name, price) VALUES (?, ?)", 
                    "Keyboard", 79.99);
        }

        public List<Map<String, Object>> getAllProducts() {
            return jdbcTemplate.queryForList("SELECT * FROM products");
        }

        public int getProductCount() {
            return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM products", Integer.class);
        }
    }

    /**
     * REST Controller
     */
    @RestController
    @RequestMapping("/api/datasource")
    public static class DataSourceController {

        private final DataSourceInfoService infoService;
        private final DatabaseService databaseService;

        public DataSourceController(DataSourceInfoService infoService, 
                                   DatabaseService databaseService) {
            this.infoService = infoService;
            this.databaseService = databaseService;
        }

        @GetMapping("/info")
        public Map<String, Object> getDataSourceInfo() {
            return infoService.getDataSourceInfo();
        }

        @GetMapping("/test-connection")
        public Map<String, Object> testConnection() {
            Map<String, Object> response = new HashMap<>();
            response.put("connected", infoService.testConnection());
            response.put("timestamp", System.currentTimeMillis());
            return response;
        }

        @GetMapping("/test-query")
        public Map<String, Object> testQuery() {
            Map<String, Object> response = new HashMap<>();
            response.put("products", databaseService.getAllProducts());
            response.put("count", databaseService.getProductCount());
            return response;
        }
    }

    /**
     * Configuration Examples
     * 
     * application.properties:
     * 
     * # Primary DataSource (auto-configured)
     * spring.datasource.url=jdbc:h2:mem:testdb
     * spring.datasource.username=sa
     * spring.datasource.password=
     * spring.datasource.driver-class-name=org.h2.Driver
     * 
     * # Custom DataSource
     * custom.datasource.url=jdbc:mysql://localhost:3306/customdb
     * custom.datasource.username=root
     * custom.datasource.password=password
     * custom.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
     * 
     * # Connection properties
     * spring.datasource.hikari.connection-timeout=20000
     * spring.datasource.hikari.maximum-pool-size=10
     * 
     * application.yml:
     * 
     * spring:
     *   datasource:
     *     url: jdbc:postgresql://localhost:5432/mydb
     *     username: postgres
     *     password: password
     *     driver-class-name: org.postgresql.Driver
     *     hikari:
     *       maximum-pool-size: 10
     *       minimum-idle: 5
     *       connection-timeout: 20000
     * 
     * custom:
     *   datasource:
     *     url: jdbc:h2:mem:customdb
     *     username: sa
     *     password:
     *     driver-class-name: org.h2.Driver
     */
}
