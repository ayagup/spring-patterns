# Database Connection Patterns in Spring

This directory contains comprehensive examples of various Database Connection Patterns in Spring Boot applications. Each pattern demonstrates best practices for managing database connections in different scenarios.

## 📋 Table of Contents

1. [Connection Pooling Pattern](#1-connection-pooling-pattern)
2. [DataSource Pattern](#2-datasource-pattern)
3. [JNDI DataSource Pattern](#3-jndi-datasource-pattern)
4. [Embedded Database Pattern](#4-embedded-database-pattern)
5. [Multiple DataSource Pattern](#5-multiple-datasource-pattern)
6. [DataSource Routing Pattern](#6-datasource-routing-pattern)
7. [Read/Write Splitting Pattern](#7-readwrite-splitting-pattern)
8. [Master-Slave Pattern](#8-master-slave-pattern)
9. [Sharding Pattern](#9-sharding-pattern)

---

## 1. Connection Pooling Pattern

**File:** `ConnectionPoolingPattern.java`

### Purpose
Manage database connections efficiently using connection pools to improve performance and resource utilization.

### Key Features
- HikariCP integration (fastest connection pool)
- Connection pool monitoring and statistics
- Configurable pool size and timeout settings
- Connection leak detection
- Health check queries

### When to Use
- Production applications
- High-concurrency scenarios
- When connection creation is expensive
- Need connection reuse

### Configuration Example
```properties
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=20000
spring.datasource.hikari.idle-timeout=300000
spring.datasource.hikari.max-lifetime=1200000
```

### Benefits
- ⚡ Faster database access (connection reuse)
- 📊 Resource optimization
- 🔍 Built-in monitoring
- 🛡️ Connection leak prevention
- 📈 Better scalability

---

## 2. DataSource Pattern

**File:** `DataSourcePattern.java`

### Purpose
Configure and manage various types of DataSource implementations for different database requirements.

### Key Features
- DriverManagerDataSource (simple, non-pooled)
- SimpleDriverDataSource
- DataSourceBuilder with @ConfigurationProperties
- Support for MySQL, PostgreSQL, Oracle, SQL Server
- Custom DataSource creation

### When to Use
- Simple applications without pooling needs
- Testing and development
- Custom DataSource requirements
- Multiple database vendor support

### Configuration Example
```properties
app.datasource.mysql.url=jdbc:mysql://localhost:3306/mydb
app.datasource.mysql.username=root
app.datasource.mysql.password=password
```

### Benefits
- 🔧 Flexible configuration
- 🎯 Vendor-agnostic
- 📝 Simple setup
- 🔄 Easy migration

---

## 3. JNDI DataSource Pattern

**File:** `JNDIDataSourcePattern.java`

### Purpose
Lookup DataSource from JNDI in Java EE containers for container-managed connections.

### Key Features
- JNDI lookup integration
- Support for Tomcat, JBoss, WebLogic, WebSphere
- Container-managed connection pooling
- Resource reference configuration
- Fallback DataSource handling

### When to Use
- Java EE application servers
- Container-managed resources
- Enterprise deployments
- Shared connection pools

### Configuration Example
```xml
<!-- Tomcat context.xml -->
<Resource name="jdbc/MyDB" 
          auth="Container"
          type="javax.sql.DataSource"
          maxTotal="100" 
          maxIdle="30"
          maxWaitMillis="10000"
          username="user" 
          password="password"
          driverClassName="com.mysql.cj.jdbc.Driver"
          url="jdbc:mysql://localhost:3306/mydb"/>
```

### Benefits
- 🏢 Enterprise integration
- 🔐 Centralized security
- 📦 Container management
- ♻️ Resource sharing

---

## 4. Embedded Database Pattern

**File:** `EmbeddedDatabasePattern.java`

### Purpose
Use in-memory databases for testing, prototyping, and development without external database dependencies.

### Key Features
- H2, HSQLDB, Derby support
- EmbeddedDatabaseBuilder
- In-memory and file-based modes
- Schema and data initialization
- H2 console integration

### When to Use
- Unit and integration testing
- Prototyping and demos
- Development environments
- CI/CD pipelines

### Configuration Example
```properties
# H2 in-memory
spring.datasource.url=jdbc:h2:mem:testdb
spring.h2.console.enabled=true

# H2 file-based
spring.datasource.url=jdbc:h2:file:./data/mydb

# HSQLDB
spring.datasource.url=jdbc:hsqldb:mem:testdb
```

### Benefits
- 🚀 Fast startup
- 🧪 Perfect for testing
- 📦 No external dependencies
- 🔄 Easy reset
- 💻 Development convenience

---

## 5. Multiple DataSource Pattern

**File:** `MultipleDataSourcePattern.java`

### Purpose
Configure and manage multiple DataSources for different purposes (operational, analytics, reporting, multi-tenant).

### Key Features
- Multiple DataSource beans with @Qualifier
- Separate JdbcTemplates per DataSource
- Multi-tenant support
- Cross-database operations
- Independent transaction managers

### When to Use
- Multi-tenant applications
- Separate operational and analytics databases
- Data integration scenarios
- Microservices with multiple DBs

### Configuration Example
```properties
# Operational Database
operational.datasource.url=jdbc:mysql://localhost:3306/operational
operational.datasource.username=op_user

# Analytics Database
analytics.datasource.url=jdbc:postgresql://localhost:5432/analytics
analytics.datasource.username=analytics_user

# Reporting Database
reporting.datasource.url=jdbc:oracle:thin:@localhost:1521:reporting
reporting.datasource.username=report_user
```

### Benefits
- 🎯 Purpose-specific databases
- 🔀 Data isolation
- 📊 Separate scaling
- 🏗️ Better architecture
- 🔐 Security separation

---

## 6. DataSource Routing Pattern

**File:** `DataSourceRoutingPattern.java`

### Purpose
Dynamically route database requests to different DataSources based on runtime context (tenant, operation type, user, region).

### Key Features
- AbstractRoutingDataSource implementation
- ThreadLocal context management
- Tenant-based routing
- Operation-based routing
- User/Region-based routing

### When to Use
- Multi-tenant SaaS applications
- Geographic data distribution
- User-based data segregation
- Context-aware routing

### Configuration Example
```java
@Bean
public DataSource routingDataSource() {
    DynamicRoutingDataSource routing = new DynamicRoutingDataSource();
    
    Map<Object, Object> dataSources = new HashMap<>();
    dataSources.put("TENANT_A", tenantADataSource());
    dataSources.put("TENANT_B", tenantBDataSource());
    
    routing.setTargetDataSources(dataSources);
    routing.setDefaultTargetDataSource(defaultDataSource());
    
    return routing;
}
```

### Benefits
- 🔀 Dynamic routing
- 🏢 Multi-tenancy support
- 🌍 Geographic distribution
- 🎯 Context-aware
- 🔧 Flexible configuration

---

## 7. Read/Write Splitting Pattern

**File:** `ReadWriteSplittingPattern.java`

### Purpose
Route write operations to master database and read operations to slave replicas for better performance and scalability.

### Key Features
- Master/Slave routing
- @ReadOperation / @WriteOperation annotations
- Transaction-based routing (@Transactional readOnly)
- Automatic replication simulation
- Load distribution

### When to Use
- Read-heavy applications (90%+ reads)
- Database replication setup
- Need to scale read capacity
- Reduce master database load

### Configuration Example
```properties
# Master (Write)
master.datasource.url=jdbc:mysql://master-db:3306/mydb
master.datasource.username=write_user

# Slave (Read)
slave.datasource.url=jdbc:mysql://slave-db:3306/mydb
slave.datasource.username=read_user
```

### Benefits
- 📈 Read scalability
- ⚡ Better performance
- 💪 Reduced master load
- 🔄 Replication support
- 🎯 Optimized queries

---

## 8. Master-Slave Pattern

**File:** `MasterSlavePattern.java`

### Purpose
One master handles writes, multiple slaves handle reads with load balancing and automatic failover.

### Key Features
- Master for writes only
- Multiple slaves for reads
- Round-robin load balancing
- Weighted load balancing
- Slave health monitoring
- Automatic failover

### When to Use
- High read-to-write ratio (90%+)
- Need horizontal read scaling
- Geographic distribution
- High availability requirements

### Load Balancing Strategies
```java
// Round-robin
LoadBalancer lb = new RoundRobinLoadBalancer();

// Weighted (50%, 30%, 20%)
LoadBalancer lb = new WeightedLoadBalancer();
```

### Benefits
- 📊 Horizontal read scaling
- ⚖️ Load balancing
- 🛡️ High availability
- 🌍 Geographic distribution
- 💪 Fault tolerance

---

## 9. Sharding Pattern

**File:** `ShardingPattern.java`

### Purpose
Horizontal partitioning of data across multiple databases based on sharding key for massive scale.

### Key Features
- Hash-based sharding
- Range-based sharding
- Consistent hashing
- Geographic sharding
- Multi-shard query aggregation
- Shard distribution statistics

### When to Use
- Very large datasets (TB+)
- High write throughput
- Database size limits reached
- Need horizontal scaling
- Multi-tenant at scale

### Sharding Strategies

#### 1. Hash-based Sharding
```java
ShardingStrategy strategy = new HashBasedSharding();
// shard = hash(customerId) % shard_count
```

#### 2. Range-based Sharding
```java
ShardingStrategy strategy = new RangeBasedSharding();
// Shard 0: IDs 0-1000
// Shard 1: IDs 1001-2000
// Shard 2: IDs 2001-3000
```

#### 3. Consistent Hashing
```java
ShardingStrategy strategy = new ConsistentHashingSharding(4, 100);
// Minimal data redistribution when adding shards
```

#### 4. Geographic Sharding
```java
ShardingStrategy strategy = new GeographicSharding();
// US → Shard 0, EU → Shard 1, ASIA → Shard 2
```

### Configuration Example
```properties
# Shard 0 (US Region)
shard0.datasource.url=jdbc:mysql://us-db-1:3306/shard0
shard0.datasource.username=user

# Shard 1 (EU Region)
shard1.datasource.url=jdbc:mysql://eu-db-1:3306/shard1
shard1.datasource.username=user

# Shard 2 (ASIA Region)
shard2.datasource.url=jdbc:mysql://asia-db-1:3306/shard2
shard2.datasource.username=user
```

### Benefits
- 📈 Massive horizontal scaling
- ⚡ Improved performance per shard
- 🌍 Geographic data locality
- 💾 Overcome size limits
- 🎯 Better resource utilization

### Considerations
- ⚠️ Complex query routing
- 🔀 Cross-shard joins difficult
- 🔄 Rebalancing overhead
- 🎓 Higher operational complexity

---

## 🎯 Pattern Comparison Matrix

| Pattern | Use Case | Complexity | Scalability | Availability | Performance |
|---------|----------|------------|-------------|--------------|-------------|
| Connection Pooling | Production apps | Low | Medium | Medium | High |
| DataSource | Simple apps | Low | Low | Medium | Medium |
| JNDI DataSource | Enterprise apps | Medium | Medium | High | Medium |
| Embedded Database | Testing/Dev | Low | Low | Low | High |
| Multiple DataSource | Multi-purpose | Medium | Medium | Medium | Medium |
| DataSource Routing | Multi-tenant | High | High | Medium | High |
| Read/Write Splitting | Read-heavy | Medium | High | Medium | High |
| Master-Slave | High availability | High | High | High | High |
| Sharding | Massive scale | Very High | Very High | High | Very High |

---

## 🚀 Getting Started

### Prerequisites
- Java 11 or higher
- Maven or Gradle
- Spring Boot 2.5+

### Dependencies
```xml
<dependencies>
    <!-- Spring Boot Starter JDBC -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-jdbc</artifactId>
    </dependency>
    
    <!-- HikariCP (included in spring-boot-starter-jdbc) -->
    
    <!-- H2 Database -->
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>runtime</scope>
    </dependency>
    
    <!-- MySQL (for production) -->
    <dependency>
        <groupId>mysql</groupId>
        <artifactId>mysql-connector-java</artifactId>
        <scope>runtime</scope>
    </dependency>
    
    <!-- PostgreSQL (optional) -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>
</dependencies>
```

### Running Examples

Each pattern is a standalone Spring Boot application. To run:

```bash
# Compile
javac -cp "path/to/spring-boot-libs/*" ConnectionPoolingPattern.java

# Or using Spring Boot CLI
spring run ConnectionPoolingPattern.java

# Or as part of Spring Boot project
mvn spring-boot:run
```

---

## 📊 Performance Characteristics

### Connection Pooling
- **Connection Acquisition:** 1-5ms (vs 50-100ms without pooling)
- **Throughput:** 10,000+ req/sec with proper pool size
- **Memory:** ~1MB per 100 connections

### Read/Write Splitting
- **Read Performance:** 2-3x improvement with 2 slaves
- **Write Performance:** Same as single master
- **Throughput:** Scales linearly with slave count

### Sharding
- **Write Performance:** Near-linear scaling with shard count
- **Read Performance:** Near-linear scaling with shard count
- **Storage:** Distributed across shards
- **Query Latency:** Single-shard: <10ms, Multi-shard: 50-200ms

---

## 🎓 Best Practices

### 1. Connection Pool Sizing
```
connections = ((core_count × 2) + effective_spindle_count)
```
- Start with small pool (10-20)
- Monitor active connections
- Adjust based on metrics

### 2. Choosing Sharding Key
- ✅ High cardinality
- ✅ Even distribution
- ✅ Query pattern aligned
- ✅ Stable over time
- ❌ Avoid hot spots
- ❌ Avoid frequently changing keys

### 3. Multi-tenant Isolation
- Separate schemas per tenant
- Tenant context in ThreadLocal
- Clear context after request
- Validate tenant access

### 4. Monitoring
- Connection pool statistics
- Query performance
- Shard distribution
- Replication lag
- Error rates

---

## 🔧 Configuration Tips

### Production Settings
```properties
# Connection Pool
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=10
spring.datasource.hikari.connection-timeout=20000
spring.datasource.hikari.idle-timeout=600000
spring.datasource.hikari.max-lifetime=1800000
spring.datasource.hikari.leak-detection-threshold=60000

# Statement Cache
spring.datasource.hikari.data-source-properties.cachePrepStmts=true
spring.datasource.hikari.data-source-properties.prepStmtCacheSize=250
spring.datasource.hikari.data-source-properties.prepStmtCacheSqlLimit=2048
```

### Testing Settings
```properties
# H2 Console
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# Show SQL
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

---

## 🧪 Testing Strategies

### Unit Testing
```java
@DataJdbcTest
class DataSourceTest {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Test
    void testConnection() {
        Integer result = jdbcTemplate.queryForObject(
            "SELECT 1", Integer.class);
        assertEquals(1, result);
    }
}
```

### Integration Testing
```java
@SpringBootTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class ShardingIntegrationTest {
    @Autowired
    private ShardingManager shardingManager;
    
    @Test
    void testShardDistribution() {
        // Test logic
    }
}
```

---

## 🐛 Common Pitfalls

### 1. Connection Leaks
❌ **Problem:** Not closing connections
```java
Connection conn = dataSource.getConnection();
// ... operations without try-with-resources
```

✅ **Solution:** Use try-with-resources
```java
try (Connection conn = dataSource.getConnection()) {
    // operations
}
```

### 2. Pool Exhaustion
❌ **Problem:** Too small pool size
✅ **Solution:** Monitor and adjust pool size

### 3. Cross-Shard Joins
❌ **Problem:** Joining data across shards
✅ **Solution:** Denormalize or use application-level joins

### 4. Context Leaks
❌ **Problem:** Not clearing ThreadLocal context
✅ **Solution:** Use try-finally
```java
try {
    TenantContext.setTenant("tenant1");
    // operations
} finally {
    TenantContext.clear();
}
```

---

## 📚 Additional Resources

- [Spring Boot DataSource Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/data.html#data.sql.datasource)
- [HikariCP GitHub](https://github.com/brettwooldridge/HikariCP)
- [Database Sharding Patterns](https://learn.microsoft.com/en-us/azure/architecture/patterns/sharding)
- [Master-Slave Replication](https://dev.mysql.com/doc/refman/8.0/en/replication.html)

---

## 📝 License

These examples are provided for educational purposes.

---

## 🤝 Contributing

Feel free to submit issues and enhancement requests!

---

**Happy Coding! 🚀**
