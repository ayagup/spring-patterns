### [Continue](https://continue.dev) session transcript
 Exported: 11/5/2025, 1:48:36 PM

#### _User_

> generate a java program in spring for each of the below patterns:
> ```
> **Database Connection Patterns**
> - Connection Pooling Pattern
> - DataSource Pattern
> - JNDI DataSource Pattern
> - Embedded Database Pattern
> - Multiple DataSource Pattern
> - DataSource Routing Pattern
> - Read/Write Splitting Pattern
> - Master-Slave Pattern
> - Sharding Pattern
> ```

#### _Assistant_

> I'll create a comprehensive Spring Boot application demonstrating each database connection pattern.
> 
> ## Project Structure
> 
> ```xml pom.xml
> <?xml version="1.0" encoding="UTF-8"?>
> <project xmlns="http://maven.apache.org/POM/4.0.0"
>          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
>          xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
>          http://maven.apache.org/xsd/maven-4.0.0.xsd">
>     <modelVersion>4.0.0</modelVersion>
>     
>     <parent>
>         <groupId>org.springframework.boot</groupId>
>         <artifactId>spring-boot-starter-parent</artifactId>
>         <version>3.2.0</version>
>     </parent>
>     
>     <groupId>com.example</groupId>
>     <artifactId>database-patterns</artifactId>
>     <version>1.0.0</version>
>     
>     <properties>
>         <java.version>17</java.version>
>     </properties>
>     
>     <dependencies>
>         <dependency>
>             <groupId>org.springframework.boot</groupId>
>             <artifactId>spring-boot-starter-data-jpa</artifactId>
>         </dependency>
>         <dependency>
>             <groupId>org.springframework.boot</groupId>
>             <artifactId>spring-boot-starter-web</artifactId>
>         </dependency>
>         <dependency>
>             <groupId>com.zaxxer</groupId>
>             <artifactId>HikariCP</artifactId>
>         </dependency>
>         <dependency>
>             <groupId>org.apache.commons</groupId>
>             <artifactId>commons-dbcp2</artifactId>
>         </dependency>
>         <dependency>
>             <groupId>com.h2database</groupId>
>             <artifactId>h2</artifactId>
>         </dependency>
>         <dependency>
>             <groupId>org.postgresql</groupId>
>             <artifactId>postgresql</artifactId>
>         </dependency>
>         <dependency>
>             <groupId>org.projectlombok</groupId>
>             <artifactId>lombok</artifactId>
>             <optional>true</optional>
>         </dependency>
>     </dependencies>
> </project>
> ```
> 
> ## 1. Connection Pooling Pattern
> 
> ```java src/main/java/com/example/patterns/connectionpool/ConnectionPoolConfig.java
> package com.example.patterns.connectionpool;
> 
> import com.zaxxer.hikari.HikariConfig;
> import com.zaxxer.hikari.HikariDataSource;
> import org.springframework.context.annotation.Bean;
> import org.springframework.context.annotation.Configuration;
> 
> import javax.sql.DataSource;
> 
> /**
>  * Connection Pooling Pattern
>  * Reuses database connections to improve performance and resource utilization
>  */
> @Configuration
> public class ConnectionPoolConfig {
>     
>     @Bean(name = "hikariDataSource")
>     public DataSource hikariDataSource() {
>         HikariConfig config = new HikariConfig();
>         config.setJdbcUrl("jdbc:h2:mem:pooldb");
>         config.setUsername("sa");
>         config.setPassword("");
>         config.setDriverClassName("org.h2.Driver");
>         
>         // Connection pool settings
>         config.setMaximumPoolSize(10);
>         config.setMinimumIdle(2);
>         config.setConnectionTimeout(30000);
>         config.setIdleTimeout(600000);
>         config.setMaxLifetime(1800000);
>         config.setPoolName("HikariCP-Pool");
>         
>         return new HikariDataSource(config);
>     }
> }
> ```
> 
> ```java src/main/java/com/example/patterns/connectionpool/ConnectionPoolService.java
> package com.example.patterns.connectionpool;
> 
> import lombok.RequiredArgsConstructor;
> import org.springframework.beans.factory.annotation.Qualifier;
> import org.springframework.stereotype.Service;
> 
> import javax.sql.DataSource;
> import java.sql.Connection;
> import java.sql.ResultSet;
> import java.sql.SQLException;
> import java.sql.Statement;
> 
> @Service
> @RequiredArgsConstructor
> public class ConnectionPoolService {
>     
>     @Qualifier("hikariDataSource")
>     private final DataSource dataSource;
>     
>     public String executeQuery() throws SQLException {
>         try (Connection conn = dataSource.getConnection();
>              Statement stmt = conn.createStatement();
>              ResultSet rs = stmt.executeQuery("SELECT 'Connection Pool Pattern' as result")) {
>             
>             if (rs.next()) {
>                 return rs.getString("result");
>             }
>         }
>         return "No result";
>     }
> }
> ```
> 
> ## 2. DataSource Pattern
> 
> ```java src/main/java/com/example/patterns/datasource/DataSourceConfig.java
> package com.example.patterns.datasource;
> 
> import org.springframework.boot.jdbc.DataSourceBuilder;
> import org.springframework.context.annotation.Bean;
> import org.springframework.context.annotation.Configuration;
> 
> import javax.sql.DataSource;
> 
> /**
>  * DataSource Pattern
>  * Standard abstraction for database connections in Java
>  */
> @Configuration
> public class DataSourceConfig {
>     
>     @Bean(name = "standardDataSource")
>     public DataSource standardDataSource() {
>         return DataSourceBuilder.create()
>                 .driverClassName("org.h2.Driver")
>                 .url("jdbc:h2:mem:standarddb")
>                 .username("sa")
>                 .password("")
>                 .build();
>     }
> }
> ```
> 
> ## 3. JNDI DataSource Pattern
> 
> ```java src/main/java/com/example/patterns/jndi/JndiDataSourceConfig.java
> package com.example.patterns.jndi;
> 
> import org.springframework.context.annotation.Bean;
> import org.springframework.context.annotation.Configuration;
> import org.springframework.jndi.JndiObjectFactoryBean;
> 
> import javax.naming.NamingException;
> import javax.sql.DataSource;
> 
> /**
>  * JNDI DataSource Pattern
>  * Retrieves DataSource from application server's JNDI context
>  */
> @Configuration
> public class JndiDataSourceConfig {
>     
>     @Bean(name = "jndiDataSource")
>     public JndiObjectFactoryBean jndiDataSource() {
>         JndiObjectFactoryBean bean = new JndiObjectFactoryBean();
>         bean.setJndiName("java:comp/env/jdbc/MyDataSource");
>         bean.setProxyInterface(DataSource.class);
>         bean.setLookupOnStartup(false);
>         return bean;
>     }
>     
>     // Fallback for non-JNDI environments
>     @Bean(name = "fallbackDataSource")
>     public DataSource fallbackDataSource() {
>         return org.springframework.boot.jdbc.DataSourceBuilder.create()
>                 .driverClassName("org.h2.Driver")
>                 .url("jdbc:h2:mem:jndidb")
>                 .username("sa")
>                 .password("")
>                 .build();
>     }
> }
> ```
> 
> ## 4. Embedded Database Pattern
> 
> ```java src/main/java/com/example/patterns/embedded/EmbeddedDatabaseConfig.java
> package com.example.patterns.embedded;
> 
> import org.springframework.context.annotation.Bean;
> import org.springframework.context.annotation.Configuration;
> import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
> import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
> 
> import javax.sql.DataSource;
> 
> /**
>  * Embedded Database Pattern
>  * In-memory database for testing and development
>  */
> @Configuration
> public class EmbeddedDatabaseConfig {
>     
>     @Bean(name = "embeddedDataSource")
>     public DataSource embeddedDataSource() {
>         return new EmbeddedDatabaseBuilder()
>                 .setType(EmbeddedDatabaseType.H2)
>                 .setName("embeddeddb")
>                 .addScript("classpath:schema.sql")
>                 .addScript("classpath:data.sql")
>                 .build();
>     }
> }
> ```
> 
> ```sql src/main/resources/schema.sql
> CREATE TABLE IF NOT EXISTS users (
>     id BIGINT AUTO_INCREMENT PRIMARY KEY,
>     username VARCHAR(100) NOT NULL,
>     email VARCHAR(100) NOT NULL
> );
> ```
> 
> ```sql src/main/resources/data.sql
> INSERT INTO users (username, email) VALUES ('john_doe', 'john@example.com');
> INSERT INTO users (username, email) VALUES ('jane_smith', 'jane@example.com');
> ```
> 
> ## 5. Multiple DataSource Pattern
> 
> ```java src/main/java/com/example/patterns/multiple/MultipleDataSourceConfig.java
> package com.example.patterns.multiple;
> 
> import org.springframework.boot.context.properties.ConfigurationProperties;
> import org.springframework.boot.jdbc.DataSourceBuilder;
> import org.springframework.context.annotation.Bean;
> import org.springframework.context.annotation.Configuration;
> import org.springframework.context.annotation.Primary;
> 
> import javax.sql.DataSource;
> 
> /**
>  * Multiple DataSource Pattern
>  * Connects to multiple independent databases
>  */
> @Configuration
> public class MultipleDataSourceConfig {
>     
>     @Primary
>     @Bean(name = "primaryDataSource")
>     @ConfigurationProperties(prefix = "spring.datasource.primary")
>     public DataSource primaryDataSource() {
>         return DataSourceBuilder.create()
>                 .driverClassName("org.h2.Driver")
>                 .url("jdbc:h2:mem:primarydb")
>                 .username("sa")
>                 .password("")
>                 .build();
>     }
>     
>     @Bean(name = "secondaryDataSource")
>     @ConfigurationProperties(prefix = "spring.datasource.secondary")
>     public DataSource secondaryDataSource() {
>         return DataSourceBuilder.create()
>                 .driverClassName("org.h2.Driver")
>                 .url("jdbc:h2:mem:secondarydb")
>                 .username("sa")
>                 .password("")
>                 .build();
>     }
> }
> ```
> 
> ```java src/main/java/com/example/patterns/multiple/MultipleDataSourceService.java
> package com.example.patterns.multiple;
> 
> import lombok.RequiredArgsConstructor;
> import org.springframework.beans.factory.annotation.Qualifier;
> import org.springframework.stereotype.Service;
> 
> import javax.sql.DataSource;
> import java.sql.Connection;
> import java.sql.SQLException;
> 
> @Service
> @RequiredArgsConstructor
> public class MultipleDataSourceService {
>     
>     @Qualifier("primaryDataSource")
>     private final DataSource primaryDataSource;
>     
>     @Qualifier("secondaryDataSource")
>     private final DataSource secondaryDataSource;
>     
>     public void usePrimaryDatabase() throws SQLException {
>         try (Connection conn = primaryDataSource.getConnection()) {
>             System.out.println("Connected to primary database");
>         }
>     }
>     
>     public void useSecondaryDatabase() throws SQLException {
>         try (Connection conn = secondaryDataSource.getConnection()) {
>             System.out.println("Connected to secondary database");
>         }
>     }
> }
> ```
> 
> ## 6. DataSource Routing Pattern
> 
> ```java src/main/java/com/example/patterns/routing/RoutingDataSourceConfig.java
> package com.example.patterns.routing;
> 
> import org.springframework.context.annotation.Bean;
> import org.springframework.context.annotation.Configuration;
> import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
> 
> import javax.sql.DataSource;
> import java.util.HashMap;
> import java.util.Map;
> 
> /**
>  * DataSource Routing Pattern
>  * Dynamically routes to different DataSources based on context
>  */
> @Configuration
> public class RoutingDataSourceConfig {
>     
>     @Bean(name = "routingDataSource")
>     public DataSource routingDataSource() {
>         Map<Object, Object> targetDataSources = new HashMap<>();
>         
>         targetDataSources.put("DB1", createDataSource("jdbc:h2:mem:db1"));
>         targetDataSources.put("DB2", createDataSource("jdbc:h2:mem:db2"));
>         targetDataSources.put("DB3", createDataSource("jdbc:h2:mem:db3"));
>         
>         RoutingDataSource routingDataSource = new RoutingDataSource();
>         routingDataSource.setTargetDataSources(targetDataSources);
>         routingDataSource.setDefaultTargetDataSource(targetDataSources.get("DB1"));
>         
>         return routingDataSource;
>     }
>     
>     private DataSource createDataSource(String url) {
>         return org.springframework.boot.jdbc.DataSourceBuilder.create()
>                 .driverClassName("org.h2.Driver")
>                 .url(url)
>                 .username("sa")
>                 .password("")
>                 .build();
>     }
> }
> ```
> 
> ```java src/main/java/com/example/patterns/routing/RoutingDataSource.java
> package com.example.patterns.routing;
> 
> import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
> 
> public class RoutingDataSource extends AbstractRoutingDataSource {
>     
>     @Override
>     protected Object determineCurrentLookupKey() {
>         return DataSourceContextHolder.getDataSourceType();
>     }
> }
> ```
> 
> ```java src/main/java/com/example/patterns/routing/DataSourceContextHolder.java
> package com.example.patterns.routing;
> 
> public class DataSourceContextHolder {
>     
>     private static final ThreadLocal<String> contextHolder = new ThreadLocal<>();
>     
>     public static void setDataSourceType(String dataSourceType) {
>         contextHolder.set(dataSourceType);
>     }
>     
>     public static String getDataSourceType() {
>         return contextHolder.get();
>     }
>     
>     public static void clearDataSourceType() {
>         contextHolder.remove();
>     }
> }
> ```
> 
> ## 7. Read/Write Splitting Pattern
> 
> ```java src/main/java/com/example/patterns/readwrite/ReadWriteDataSourceConfig.java
> package com.example.patterns.readwrite;
> 
> import org.springframework.context.annotation.Bean;
> import org.springframework.context.annotation.Configuration;
> import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
> 
> import javax.sql.DataSource;
> import java.util.HashMap;
> import java.util.Map;
> 
> /**
>  * Read/Write Splitting Pattern
>  * Routes write operations to master and read operations to replicas
>  */
> @Configuration
> public class ReadWriteDataSourceConfig {
>     
>     @Bean(name = "readWriteDataSource")
>     public DataSource readWriteDataSource() {
>         Map<Object, Object> targetDataSources = new HashMap<>();
>         
>         // Master for writes
>         targetDataSources.put(DataSourceType.WRITE, createDataSource("jdbc:h2:mem:master"));
>         
>         // Replicas for reads
>         targetDataSources.put(DataSourceType.READ, createDataSource("jdbc:h2:mem:replica1"));
>         
>         ReadWriteRoutingDataSource routingDataSource = new ReadWriteRoutingDataSource();
>         routingDataSource.setTargetDataSources(targetDataSources);
>         routingDataSource.setDefaultTargetDataSource(targetDataSources.get(DataSourceType.WRITE));
>         
>         return routingDataSource;
>     }
>     
>     private DataSource createDataSource(String url) {
>         return org.springframework.boot.jdbc.DataSourceBuilder.create()
>                 .driverClassName("org.h2.Driver")
>                 .url(url)
>                 .username("sa")
>                 .password("")
>                 .build();
>     }
> }
> ```
> 
> ```java src/main/java/com/example/patterns/readwrite/DataSourceType.java
> package com.example.patterns.readwrite;
> 
> public enum DataSourceType {
>     READ, WRITE
> }
> ```
> 
> ```java src/main/java/com/example/patterns/readwrite/ReadWriteRoutingDataSource.java
> package com.example.patterns.readwrite;
> 
> import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
> 
> public class ReadWriteRoutingDataSource extends AbstractRoutingDataSource {
>     
>     @Override
>     protected Object determineCurrentLookupKey() {
>         return DataSourceContextHolder.getDataSourceType();
>     }
>     
>     static class DataSourceContextHolder {
>         private static final ThreadLocal<DataSourceType> contextHolder = new ThreadLocal<>();
>         
>         public static void setDataSourceType(DataSourceType type) {
>             contextHolder.set(type);
>         }
>         
>         public static DataSourceType getDataSourceType() {
>             return contextHolder.get() != null ? contextHolder.get() : DataSourceType.WRITE;
>         }
>         
>         public static void clearDataSourceType() {
>             contextHolder.remove();
>         }
>     }
> }
> ```
> 
> ```java src/main/java/com/example/patterns/readwrite/ReadOnly.java
> package com.example.patterns.readwrite;
> 
> import java.lang.annotation.*;
> 
> @Target({ElementType.METHOD, ElementType.TYPE})
> @Retention(RetentionPolicy.RUNTIME)
> @Documented
> public @interface ReadOnly {
> }
> ```
> 
> ```java src/main/java/com/example/patterns/readwrite/ReadWriteAspect.java
> package com.example.patterns.readwrite;
> 
> import org.aspectj.lang.ProceedingJoinPoint;
> import org.aspectj.lang.annotation.Around;
> import org.aspectj.lang.annotation.Aspect;
> import org.springframework.stereotype.Component;
> 
> @Aspect
> @Component
> public class ReadWriteAspect {
>     
>     @Around("@annotation(readOnly)")
>     public Object setReadDataSource(ProceedingJoinPoint joinPoint, ReadOnly readOnly) throws Throwable {
>         try {
>             ReadWriteRoutingDataSource.DataSourceContextHolder.setDataSourceType(DataSourceType.READ);
>             return joinPoint.proceed();
>         } finally {
>             ReadWriteRoutingDataSource.DataSourceContextHolder.clearDataSourceType();
>         }
>     }
> }
> ```
> 
> ## 8. Master-Slave Pattern
> 
> ```java src/main/java/com/example/patterns/masterslave/MasterSlaveDataSourceConfig.java
> package com.example.patterns.masterslave;
> 
> import com.zaxxer.hikari.HikariDataSource;
> import org.springframework.context.annotation.Bean;
> import org.springframework.context.annotation.Configuration;
> 
> import javax.sql.DataSource;
> import java.util.HashMap;
> import java.util.Map;
> 
> /**
>  * Master-Slave Pattern
>  * Master handles all writes, slaves handle reads with load balancing
>  */
> @Configuration
> public class MasterSlaveDataSourceConfig {
>     
>     @Bean(name = "masterSlaveDataSource")
>     public DataSource masterSlaveDataSource() {
>         Map<Object, Object> targetDataSources = new HashMap<>();
>         
>         // Master database
>         targetDataSources.put("master", createDataSource("jdbc:h2:mem:master", 5));
>         
>         // Slave databases
>         targetDataSources.put("slave1", createDataSource("jdbc:h2:mem:slave1", 10));
>         targetDataSources.put("slave2", createDataSource("jdbc:h2:mem:slave2", 10));
>         targetDataSources.put("slave3", createDataSource("jdbc:h2:mem:slave3", 10));
>         
>         MasterSlaveRoutingDataSource routingDataSource = new MasterSlaveRoutingDataSource();
>         routingDataSource.setTargetDataSources(targetDataSources);
>         routingDataSource.setDefaultTargetDataSource(targetDataSources.get("master"));
>         
>         return routingDataSource;
>     }
>     
>     private DataSource createDataSource(String url, int poolSize) {
>         HikariDataSource dataSource = new HikariDataSource();
>         dataSource.setJdbcUrl(url);
>         dataSource.setUsername("sa");
>         dataSource.setPassword("");
>         dataSource.setDriverClassName("org.h2.Driver");
>         dataSource.setMaximumPoolSize(poolSize);
>         return dataSource;
>     }
> }
> ```
> 
> ```java src/main/java/com/example/patterns/masterslave/MasterSlaveRoutingDataSource.java
> package com.example.patterns.masterslave;
> 
> import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
> 
> import java.util.concurrent.atomic.AtomicInteger;
> 
> public class MasterSlaveRoutingDataSource extends AbstractRoutingDataSource {
>     
>     private final AtomicInteger counter = new AtomicInteger(0);
>     
>     @Override
>     protected Object determineCurrentLookupKey() {
>         boolean isReadOnly = DataSourceContextHolder.isReadOnly();
>         
>         if (isReadOnly) {
>             // Round-robin load balancing across slaves
>             int slaveIndex = counter.getAndIncrement() % 3;
>             return "slave" + (slaveIndex + 1);
>         } else {
>             return "master";
>         }
>     }
>     
>     static class DataSourceContextHolder {
>         private static final ThreadLocal<Boolean> readOnlyContext = new ThreadLocal<>();
>         
>         public static void setReadOnly(boolean readOnly) {
>             readOnlyContext.set(readOnly);
>         }
>         
>         public static boolean isReadOnly() {
>             Boolean readOnly = readOnlyContext.get();
>             return readOnly != null && readOnly;
>         }
>         
>         public static void clear() {
>             readOnlyContext.remove();
>         }
>     }
> }
> ```
> 
> ## 9. Sharding Pattern
> 
> ```java src/main/java/com/example/patterns/sharding/ShardingDataSourceConfig.java
> package com.example.patterns.sharding;
> 
> import org.springframework.context.annotation.Bean;
> import org.springframework.context.annotation.Configuration;
> 
> import javax.sql.DataSource;
> import java.util.HashMap;
> import java.util.Map;
> 
> /**
>  * Sharding Pattern
>  * Distributes data across multiple databases based on shard key
>  */
> @Configuration
> public class ShardingDataSourceConfig {
>     
>     private static final int SHARD_COUNT = 4;
>     
>     @Bean(name = "shardingDataSource")
>     public DataSource shardingDataSource() {
>         Map<Object, Object> targetDataSources = new HashMap<>();
>         
>         // Create shards
>         for (int i = 0; i < SHARD_COUNT; i++) {
>             targetDataSources.put(
>                 "shard" + i,
>                 createDataSource("jdbc:h2:mem:shard" + i)
>             );
>         }
>         
>         ShardingRoutingDataSource routingDataSource = new ShardingRoutingDataSource();
>         routingDataSource.setTargetDataSources(targetDataSources);
>         routingDataSource.setDefaultTargetDataSource(targetDataSources.get("shard0"));
>         
>         return routingDataSource;
>     }
>     
>     private DataSource createDataSource(String url) {
>         return org.springframework.boot.jdbc.DataSourceBuilder.create()
>                 .driverClassName("org.h2.Driver")
>                 .url(url)
>                 .username("sa")
>                 .password("")
>                 .build();
>     }
> }
> ```
> 
> ```java src/main/java/com/example/patterns/sharding/ShardingRoutingDataSource.java
> package com.example.patterns.sharding;
> 
> import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
> 
> public class ShardingRoutingDataSource extends AbstractRoutingDataSource {
>     
>     private static final int SHARD_COUNT = 4;
>     
>     @Override
>     protected Object determineCurrentLookupKey() {
>         Long shardKey = ShardContextHolder.getShardKey();
>         if (shardKey == null) {
>             return "shard0";
>         }
>         
>         // Hash-based sharding
>         int shardIndex = (int) (shardKey % SHARD_COUNT);
>         return "shard" + shardIndex;
>     }
>     
>     static class ShardContextHolder {
>         private static final ThreadLocal<Long> shardKeyContext = new ThreadLocal<>();
>         
>         public static void setShardKey(Long shardKey) {
>             shardKeyContext.set(shardKey);
>         }
>         
>         public static Long getShardKey() {
>             return shardKeyContext.get();
>         }
>         
>         public static void clear() {
>             shardKeyContext.remove();
>         }
>     }
> }
> ```
> 
> ```java src/main/java/com/example/patterns/sharding/ShardingService.java
> package com.example.patterns.sharding;
> 
> import lombok.RequiredArgsConstructor;
> import org.springframework.beans.factory.annotation.Qualifier;
> import org.springframework.stereotype.Service;
> 
> import javax.sql.DataSource;
> import java.sql.Connection;
> import java.sql.SQLException;
> 
> @Service
> @RequiredArgsConstructor
> public class ShardingService {
>     
>     @Qualifier("shardingDataSource")
>     private final DataSource shardingDataSource;
>     
>     public void saveUser(Long userId, String userData) throws SQLException {
>         try {
>             // Set shard key based on user ID
>             ShardingRoutingDataSource.ShardContextHolder.setShardKey(userId);
>             
>             try (Connection conn = shardingDataSource.getConnection()) {
>                 // Execute insert into the correct shard
>                 System.out.println("Saving user " + userId + " to shard");
>             }
>         } finally {
>             ShardingRoutingDataSource.ShardContextHolder.clear();
>         }
>     }
>     
>     public String getUser(Long userId) throws SQLException {
>         try {
>             ShardingRoutingDataSource.ShardContextHolder.setShardKey(userId);
>             
>             try (Connection conn = shardingDataSource.getConnection()) {
>                 // Execute query from the correct shard
>                 return "User data for " + userId;
>             }
>         } finally {
>             ShardingRoutingDataSource.ShardContextHolder.clear();
>         }
>     }
> }
> ```
> 
> ## Main Application
> 
> ```java src/main/java/com/example/DatabasePatternsApplication.java
> package com.example;
> 
> import org.springframework.boot.SpringApplication;
> import org.springframework.boot.autoconfigure.SpringBootApplication;
> import org.springframework.context.annotation.EnableAspectJAutoProxy;
> 
> @SpringBootApplication
> @EnableAspectJAutoProxy
> public class DatabasePatternsApplication {
>     
>     public static void main(String[] args) {
>         SpringApplication.run(DatabasePatternsApplication.class, args);
>     }
> }
> ```
> 
> ## Application Properties
> 
> ```properties src/main/resources/application.properties
> # Primary DataSource
> spring.datasource.primary.driver-class-name=org.h2.Driver
> spring.datasource.primary.url=jdbc:h2:mem:primarydb
> spring.datasource.primary.username=sa
> spring.datasource.primary.password=
> 
> # Secondary DataSource
> spring.datasource.secondary.driver-class-name=org.h2.Driver
> spring.datasource.secondary.url=jdbc:h2:mem:secondarydb
> spring.datasource.secondary.username=sa
> spring.datasource.secondary.password=
> 
> # JPA Settings
> spring.jpa.hibernate.ddl-auto=update
> spring.jpa.show-sql=true
> 
> # H2 Console
> spring.h2.console.enabled=true
> ```
> 
> ## Summary
> 
> Each pattern demonstrates:
> 
> 1. **Connection Pooling** - HikariCP for efficient connection reuse
> 2. **DataSource** - Standard JDBC DataSource abstraction
> 3. **JNDI DataSource** - Application server managed connections
> 4. **Embedded Database** - In-memory H2 for testing
> 5. **Multiple DataSource** - Independent databases for different modules
> 6. **DataSource Routing** - Dynamic routing based on context
> 7. **Read/Write Splitting** - Separate read and write databases
> 8. **Master-Slave** - Load-balanced reads across replicas
> 9. **Sharding** - Horizontal partitioning across databases
> 
> All patterns use Spring's `AbstractRoutingDataSource` for dynamic routing and ThreadLocal for context management.