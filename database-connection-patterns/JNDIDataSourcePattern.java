package com.example.database.connection;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;
import org.springframework.jndi.JndiObjectFactoryBean;
import org.springframework.jndi.JndiTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * JNDI DataSource Pattern
 * 
 * Purpose:
 * - Lookup DataSource from JNDI (Java Naming and Directory Interface)
 * - Centralized DataSource management in application server
 * - Externalize database configuration from application
 * - Enable connection pooling at container level
 * 
 * JNDI Naming Conventions:
 * - Tomcat: java:comp/env/jdbc/myDataSource
 * - JBoss/WildFly: java:jboss/datasources/myDS
 * - WebLogic: jdbc/myDataSource
 * - WebSphere: jdbc/myDataSource
 * - GlassFish: jdbc/myDataSource
 * 
 * When to Use:
 * - Enterprise applications deployed in Java EE containers
 * - When database config is managed by ops team
 * - Multi-environment deployments (dev, staging, prod)
 * - When using container-managed connection pools
 * - Legacy applications migrating to Spring
 * 
 * Benefits:
 * - Separation of concerns (dev vs ops)
 * - Container-managed connection pooling
 * - No database credentials in application code
 * - Easy environment-specific configuration
 * - Centralized DataSource management
 * 
 * Limitations:
 * - Requires Java EE container
 * - More complex setup
 * - Not suitable for standalone applications
 * - Limited in cloud-native environments
 */
@SpringBootApplication
public class JNDIDataSourcePattern {

    public static void main(String[] args) {
        SpringApplication.run(JNDIDataSourcePattern.class, args);
        System.out.println("JNDI DataSource Pattern Application Started!");
        System.out.println("Visit: http://localhost:8080/api/jndi/info");
    }

    /**
     * Basic JNDI DataSource Configuration
     */
    @Configuration
    @Profile("jndi")
    public static class JndiDataSourceConfiguration {

        /**
         * Method 1: Using JndiDataSourceLookup (Simplest)
         */
        @Bean
        public DataSource dataSource() {
            JndiDataSourceLookup lookup = new JndiDataSourceLookup();
            return lookup.getDataSource("java:comp/env/jdbc/myDataSource");
        }

        /**
         * Method 2: Using JndiObjectFactoryBean (More control)
         */
        @Bean
        public JndiObjectFactoryBean jndiDataSource() {
            JndiObjectFactoryBean jndiObjectFactoryBean = new JndiObjectFactoryBean();
            jndiObjectFactoryBean.setJndiName("java:comp/env/jdbc/myDataSource");
            jndiObjectFactoryBean.setResourceRef(true);
            jndiObjectFactoryBean.setProxyInterface(DataSource.class);
            return jndiObjectFactoryBean;
        }

        /**
         * Method 3: Using JndiTemplate
         */
        public DataSource jndiTemplateDataSource() throws NamingException {
            JndiTemplate jndiTemplate = new JndiTemplate();
            return (DataSource) jndiTemplate.lookup("java:comp/env/jdbc/myDataSource");
        }

        /**
         * Method 4: Direct JNDI Lookup
         */
        public DataSource directJndiLookup() throws NamingException {
            Context context = new InitialContext();
            return (DataSource) context.lookup("java:comp/env/jdbc/myDataSource");
        }
    }

    /**
     * Multiple JNDI DataSources Configuration
     */
    @Configuration
    @Profile("jndi-multiple")
    public static class MultipleJndiDataSourceConfig {

        /**
         * Primary DataSource from JNDI
         */
        @Bean(name = "primaryDataSource")
        public DataSource primaryDataSource() {
            JndiDataSourceLookup lookup = new JndiDataSourceLookup();
            return lookup.getDataSource("java:comp/env/jdbc/primary");
        }

        /**
         * Secondary DataSource from JNDI
         */
        @Bean(name = "secondaryDataSource")
        public DataSource secondaryDataSource() {
            JndiDataSourceLookup lookup = new JndiDataSourceLookup();
            return lookup.getDataSource("java:comp/env/jdbc/secondary");
        }

        /**
         * Analytics DataSource from JNDI
         */
        @Bean(name = "analyticsDataSource")
        public DataSource analyticsDataSource() {
            JndiDataSourceLookup lookup = new JndiDataSourceLookup();
            return lookup.getDataSource("java:comp/env/jdbc/analytics");
        }

        /**
         * Reporting DataSource from JNDI
         */
        @Bean(name = "reportingDataSource")
        public DataSource reportingDataSource() {
            JndiDataSourceLookup lookup = new JndiDataSourceLookup();
            return lookup.getDataSource("java:comp/env/jdbc/reporting");
        }
    }

    /**
     * Container-Specific JNDI Configuration Examples
     */
    @Configuration
    public static class ContainerSpecificJndiConfig {

        /**
         * Tomcat JNDI Configuration
         * 
         * context.xml:
         * <Context>
         *   <Resource name="jdbc/myDataSource"
         *             auth="Container"
         *             type="javax.sql.DataSource"
         *             driverClassName="com.mysql.cj.jdbc.Driver"
         *             url="jdbc:mysql://localhost:3306/mydb"
         *             username="root"
         *             password="password"
         *             maxTotal="20"
         *             maxIdle="10"
         *             maxWaitMillis="10000"/>
         * </Context>
         * 
         * web.xml:
         * <resource-ref>
         *   <res-ref-name>jdbc/myDataSource</res-ref-name>
         *   <res-type>javax.sql.DataSource</res-type>
         *   <res-auth>Container</res-auth>
         * </resource-ref>
         */
        @Bean
        @Profile("tomcat")
        public DataSource tomcatDataSource() {
            JndiDataSourceLookup lookup = new JndiDataSourceLookup();
            return lookup.getDataSource("java:comp/env/jdbc/myDataSource");
        }

        /**
         * JBoss/WildFly JNDI Configuration
         * 
         * standalone.xml:
         * <datasource jndi-name="java:jboss/datasources/myDS" pool-name="myDS">
         *   <connection-url>jdbc:mysql://localhost:3306/mydb</connection-url>
         *   <driver>mysql</driver>
         *   <security>
         *     <user-name>root</user-name>
         *     <password>password</password>
         *   </security>
         * </datasource>
         */
        @Bean
        @Profile("jboss")
        public DataSource jbossDataSource() {
            JndiDataSourceLookup lookup = new JndiDataSourceLookup();
            return lookup.getDataSource("java:jboss/datasources/myDS");
        }

        /**
         * WebLogic JNDI Configuration
         */
        @Bean
        @Profile("weblogic")
        public DataSource weblogicDataSource() {
            JndiDataSourceLookup lookup = new JndiDataSourceLookup();
            return lookup.getDataSource("jdbc/myDataSource");
        }

        /**
         * WebSphere JNDI Configuration
         */
        @Bean
        @Profile("websphere")
        public DataSource websphereDataSource() {
            JndiDataSourceLookup lookup = new JndiDataSourceLookup();
            return lookup.getDataSource("jdbc/myDataSource");
        }
    }

    /**
     * JNDI Fallback Configuration
     * Falls back to local DataSource if JNDI lookup fails
     */
    @Configuration
    public static class JndiWithFallbackConfig {

        @Bean
        public DataSource dataSourceWithFallback() {
            try {
                JndiDataSourceLookup lookup = new JndiDataSourceLookup();
                return lookup.getDataSource("java:comp/env/jdbc/myDataSource");
            } catch (Exception e) {
                System.out.println("JNDI lookup failed, using fallback DataSource: " + e.getMessage());
                
                // Fallback to embedded H2 database
                org.springframework.jdbc.datasource.DriverManagerDataSource dataSource = 
                        new org.springframework.jdbc.datasource.DriverManagerDataSource();
                dataSource.setDriverClassName("org.h2.Driver");
                dataSource.setUrl("jdbc:h2:mem:testdb");
                dataSource.setUsername("sa");
                dataSource.setPassword("");
                
                return dataSource;
            }
        }
    }

    /**
     * JNDI Environment Configuration
     */
    @Configuration
    public static class JndiEnvironmentConfig {

        /**
         * Custom JNDI environment
         */
        public DataSource customJndiEnvironmentDataSource() throws NamingException {
            Hashtable<String, String> env = new Hashtable<>();
            env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.fscontext.RefFSContextFactory");
            env.put(Context.PROVIDER_URL, "file:///tmp");
            
            Context context = new InitialContext(env);
            return (DataSource) context.lookup("jdbc/myDataSource");
        }
    }

    /**
     * JNDI Information Service
     */
    @Service
    public static class JndiInfoService {

        private final DataSource dataSource;

        public JndiInfoService(DataSource dataSource) {
            this.dataSource = dataSource;
        }

        /**
         * Get JNDI DataSource information
         */
        public Map<String, Object> getJndiInfo() {
            Map<String, Object> info = new HashMap<>();
            
            try {
                info.put("dataSourceClass", dataSource.getClass().getName());
                info.put("dataSourceType", "JNDI DataSource");
                
                // Get connection info
                try (var connection = dataSource.getConnection()) {
                    var metaData = connection.getMetaData();
                    info.put("databaseProduct", metaData.getDatabaseProductName());
                    info.put("databaseVersion", metaData.getDatabaseProductVersion());
                    info.put("driverName", metaData.getDriverName());
                    info.put("url", metaData.getURL());
                }
            } catch (Exception e) {
                info.put("error", e.getMessage());
            }
            
            return info;
        }

        /**
         * List JNDI bindings (for debugging)
         */
        public Map<String, Object> listJndiBindings() {
            Map<String, Object> bindings = new HashMap<>();
            
            try {
                Context context = new InitialContext();
                
                // Common JNDI paths to check
                String[] paths = {
                    "java:comp/env/jdbc",
                    "java:jboss/datasources",
                    "jdbc"
                };
                
                for (String path : paths) {
                    try {
                        Object obj = context.lookup(path);
                        bindings.put(path, obj.getClass().getName());
                    } catch (NamingException e) {
                        bindings.put(path, "Not found: " + e.getMessage());
                    }
                }
            } catch (Exception e) {
                bindings.put("error", e.getMessage());
            }
            
            return bindings;
        }
    }

    /**
     * Database Service using JNDI DataSource
     */
    @Service
    public static class DatabaseService {

        private final JdbcTemplate jdbcTemplate;

        public DatabaseService(DataSource dataSource) {
            this.jdbcTemplate = new JdbcTemplate(dataSource);
            initializeDatabase();
        }

        private void initializeDatabase() {
            try {
                jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS orders (" +
                        "id INT PRIMARY KEY AUTO_INCREMENT, " +
                        "order_number VARCHAR(50), " +
                        "total DECIMAL(10, 2))");
                
                jdbcTemplate.update("INSERT INTO orders (order_number, total) VALUES (?, ?)", 
                        "ORD-001", 150.00);
                jdbcTemplate.update("INSERT INTO orders (order_number, total) VALUES (?, ?)", 
                        "ORD-002", 299.99);
            } catch (Exception e) {
                System.err.println("Failed to initialize database: " + e.getMessage());
            }
        }

        public List<Map<String, Object>> getAllOrders() {
            return jdbcTemplate.queryForList("SELECT * FROM orders");
        }
    }

    /**
     * REST Controller
     */
    @RestController
    @RequestMapping("/api/jndi")
    public static class JndiController {

        private final JndiInfoService jndiInfoService;
        private final DatabaseService databaseService;

        public JndiController(JndiInfoService jndiInfoService, 
                             DatabaseService databaseService) {
            this.jndiInfoService = jndiInfoService;
            this.databaseService = databaseService;
        }

        @GetMapping("/info")
        public Map<String, Object> getJndiInfo() {
            return jndiInfoService.getJndiInfo();
        }

        @GetMapping("/bindings")
        public Map<String, Object> listBindings() {
            return jndiInfoService.listJndiBindings();
        }

        @GetMapping("/test-query")
        public Map<String, Object> testQuery() {
            Map<String, Object> response = new HashMap<>();
            try {
                response.put("orders", databaseService.getAllOrders());
                response.put("status", "success");
            } catch (Exception e) {
                response.put("status", "error");
                response.put("message", e.getMessage());
            }
            return response;
        }
    }

    /**
     * Configuration in application.properties:
     * 
     * # JNDI DataSource
     * spring.datasource.jndi-name=java:comp/env/jdbc/myDataSource
     * 
     * # Or for specific profile
     * spring.profiles.active=jndi
     * 
     * 
     * Server Configuration Examples:
     * 
     * 1. Tomcat (context.xml):
     * <Context>
     *   <Resource name="jdbc/myDataSource"
     *             auth="Container"
     *             type="javax.sql.DataSource"
     *             factory="org.apache.tomcat.jdbc.pool.DataSourceFactory"
     *             driverClassName="com.mysql.cj.jdbc.Driver"
     *             url="jdbc:mysql://localhost:3306/mydb"
     *             username="root"
     *             password="password"
     *             initialSize="5"
     *             maxActive="20"
     *             maxIdle="10"
     *             minIdle="5"
     *             maxWait="10000"
     *             testOnBorrow="true"
     *             validationQuery="SELECT 1"/>
     * </Context>
     * 
     * 2. JBoss/WildFly (standalone.xml):
     * <datasources>
     *   <datasource jndi-name="java:jboss/datasources/myDS" pool-name="myDS" enabled="true">
     *     <connection-url>jdbc:mysql://localhost:3306/mydb</connection-url>
     *     <driver>mysql</driver>
     *     <pool>
     *       <min-pool-size>5</min-pool-size>
     *       <max-pool-size>20</max-pool-size>
     *     </pool>
     *     <security>
     *       <user-name>root</user-name>
     *       <password>password</password>
     *     </security>
     *     <validation>
     *       <validate-on-match>true</validate-on-match>
     *       <check-valid-connection-sql>SELECT 1</check-valid-connection-sql>
     *     </validation>
     *   </datasource>
     * </datasources>
     * 
     * 3. WebLogic (config.xml):
     * <JDBCDataSource>
     *   <Name>myDataSource</Name>
     *   <JNDIName>jdbc/myDataSource</JNDIName>
     *   <PoolName>myConnectionPool</PoolName>
     * </JDBCDataSource>
     */
}
