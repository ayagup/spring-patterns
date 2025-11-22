# ORM Integration Patterns in Spring

This directory contains comprehensive examples of various ORM (Object-Relational Mapping) Integration Patterns in Spring Boot applications. Each pattern demonstrates different approaches to database access and persistence.

## 📋 Table of Contents

1. [JPA Integration Pattern](#1-jpa-integration-pattern)
2. [Hibernate Integration Pattern](#2-hibernate-integration-pattern)
3. [MyBatis Integration Pattern](#3-mybatis-integration-pattern)
4. [JDBC Template Pattern](#4-jdbc-template-pattern)
5. [Named Parameter JDBC Template Pattern](#5-named-parameter-jdbc-template-pattern)
6. [Simple JDBC Insert Pattern](#6-simple-jdbc-insert-pattern)
7. [Simple JDBC Call Pattern](#7-simple-jdbc-call-pattern)
8. [Entity Manager Patterns](#8-entity-manager-patterns)

---

## 1. JPA Integration Pattern

**File:** `JPAIntegrationPattern.java`

### Purpose
Java Persistence API (JPA) integration providing a standard ORM interface for entity management and database operations.

### Key Features
- EntityManager for persistence operations
- JPQL (Java Persistence Query Language)
- Entity lifecycle management
- First and second-level caching
- Lazy loading and eager fetching
- Transaction management
- Optimistic locking with @Version

### When to Use
- Need vendor-neutral ORM
- Complex object relationships
- Advanced query capabilities
- Standard JPA compliance required
- Enterprise applications

### Configuration Example
```properties
# JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect

# Second-level cache
spring.jpa.properties.hibernate.cache.use_second_level_cache=true
spring.jpa.properties.hibernate.cache.region.factory_class=org.hibernate.cache.jcache.JCacheRegionFactory
```

### Benefits
- ✅ Vendor independence
- ✅ Rich ORM features
- ✅ Type-safe queries
- ✅ Relationship management
- ✅ Caching support

---

## 2. Hibernate Integration Pattern

**File:** `HibernateIntegrationPattern.java`

### Purpose
Native Hibernate ORM integration with advanced features and performance optimizations.

### Key Features
- SessionFactory management
- HQL (Hibernate Query Language)
- Criteria API
- Advanced caching strategies
- Batch processing
- Native SQL support
- Lazy loading optimizations

### When to Use
- Need Hibernate-specific features
- Advanced caching requirements
- Batch operations
- Performance-critical applications
- Complex mappings

### Configuration Example
```properties
# Hibernate specific
spring.jpa.properties.hibernate.jdbc.batch_size=20
spring.jpa.properties.hibernate.order_inserts=true
spring.jpa.properties.hibernate.order_updates=true
spring.jpa.properties.hibernate.jdbc.fetch_size=50
spring.jpa.properties.hibernate.default_batch_fetch_size=16

# Cache configuration
spring.jpa.properties.hibernate.cache.use_query_cache=true
spring.jpa.properties.hibernate.generate_statistics=true
```

### Benefits
- ✅ Powerful caching
- ✅ Rich query options
- ✅ Batch processing support
- ✅ Performance tuning
- ✅ Flexible mappings

---

## 3. MyBatis Integration Pattern

**File:** `MyBatisIntegrationPattern.java`

### Purpose
SQL-centric persistence framework providing fine-grained SQL control with object mapping.

### Key Features
- Annotation-based SQL mapping
- XML-based SQL mapping
- Dynamic SQL generation
- Result mapping
- Type handlers
- Batch operations
- Stored procedure support

### When to Use
- Need fine-grained SQL control
- Complex SQL queries
- Legacy database schemas
- Stored procedures
- Mix of ORM and SQL

### Configuration Example
```properties
# MyBatis Configuration
mybatis.configuration.map-underscore-to-camel-case=true
mybatis.configuration.cache-enabled=true
mybatis.configuration.lazy-loading-enabled=true
mybatis.configuration.default-executor-type=reuse
mybatis.mapper-locations=classpath:mapper/*.xml
mybatis.type-aliases-package=com.example.domain
```

### Benefits
- ✅ Full SQL control
- ✅ Easy to optimize
- ✅ Simple learning curve
- ✅ Flexible mapping
- ✅ Great for complex queries

---

## 4. JDBC Template Pattern

**File:** `JDBCTemplatePattern.java`

### Purpose
Simplified JDBC operations with automatic resource management and exception translation.

### Key Features
- Automatic resource cleanup
- DataAccessException translation
- PreparedStatement support
- RowMapper for result mapping
- Batch operations
- Transaction integration
- CallableStatement support

### When to Use
- Simple database operations
- No ORM overhead needed
- Direct SQL control
- Legacy JDBC migration
- Batch processing

### Configuration Example
```java
@Bean
public JdbcTemplate jdbcTemplate(DataSource dataSource) {
    JdbcTemplate template = new JdbcTemplate(dataSource);
    template.setFetchSize(100);
    template.setMaxRows(1000);
    template.setQueryTimeout(30);
    return template;
}
```

### Benefits
- ✅ Simplified JDBC code
- ✅ Automatic resource management
- ✅ Better exception handling
- ✅ Transaction support
- ✅ Lightweight

---

## 5. Named Parameter JDBC Template Pattern

**File:** `NamedParameterJDBCTemplatePattern.java`

### Purpose
JDBC operations with named parameters for better readability and maintainability.

### Key Features
- Named parameters (:paramName)
- MapSqlParameterSource
- BeanPropertySqlParameterSource
- IN clause support
- Dynamic SQL building
- Batch operations

### When to Use
- Complex queries with many parameters
- IN clauses with variable lists
- Better code maintainability
- Working with DTOs/beans
- Dynamic parameter binding

### Configuration Example
```java
@Bean
public NamedParameterJdbcTemplate namedParameterJdbcTemplate(DataSource dataSource) {
    return new NamedParameterJdbcTemplate(dataSource);
}
```

### Benefits
- ✅ Self-documenting SQL
- ✅ Easier to maintain
- ✅ No parameter order issues
- ✅ Flexible parameter binding
- ✅ Bean property mapping

---

## 6. Simple JDBC Insert Pattern

**File:** `SimpleJDBCInsertPattern.java`

### Purpose
Simplified insert operations with automatic key generation and metadata-driven approach.

### Key Features
- Automatic column detection
- Generated key retrieval
- No SQL for simple inserts
- Batch insert support
- Table metadata caching
- Type conversion

### When to Use
- Simple insert operations
- Need auto-generated keys
- Minimize boilerplate code
- Batch insertions
- Metadata-driven approach

### Configuration Example
```java
@Bean
public SimpleJdbcInsert simpleJdbcInsert(DataSource dataSource) {
    return new SimpleJdbcInsert(dataSource)
            .withTableName("employees")
            .usingGeneratedKeyColumns("id");
}
```

### Benefits
- ✅ No SQL for basic inserts
- ✅ Automatic key handling
- ✅ Less code to maintain
- ✅ Metadata caching
- ✅ Batch support

---

## 7. Simple JDBC Call Pattern

**File:** `SimpleJDBCCallPattern.java`

### Purpose
Simplified stored procedure and function calls with automatic metadata detection.

### Key Features
- Stored procedure calls
- Function calls
- IN/OUT/INOUT parameters
- Result set handling
- Return value mapping
- Metadata caching

### When to Use
- Stored procedures in database
- Database functions
- Complex database logic
- Legacy database procedures
- Database-side processing

### Configuration Example
```java
SimpleJdbcCall jdbcCall = new SimpleJdbcCall(dataSource)
    .withProcedureName("transfer_funds")
    .declareParameters(
        new SqlParameter("from_account", Types.VARCHAR),
        new SqlParameter("to_account", Types.VARCHAR),
        new SqlParameter("amount", Types.DECIMAL),
        new SqlOutParameter("status", Types.VARCHAR)
    );
```

### Benefits
- ✅ Simplified procedure calls
- ✅ Automatic parameter detection
- ✅ Type-safe handling
- ✅ Metadata caching
- ✅ Clean API

---

## 8. Entity Manager Patterns

### Entity Manager Pattern
Manages the lifecycle of entities and provides persistence operations.

### Entity Manager Factory Pattern
Creates and configures EntityManager instances.

### Persistence Unit Pattern
Defines a set of entity classes managed together.

### Persistence Context Pattern
Manages a set of entity instances in a transaction.

---

## 🎯 Pattern Comparison Matrix

| Pattern | Complexity | Performance | SQL Control | Learning Curve | Use Case |
|---------|-----------|-------------|-------------|----------------|----------|
| JPA | Medium | Good | Low | Medium | Standard ORM |
| Hibernate | High | Excellent | Medium | High | Advanced ORM |
| MyBatis | Medium | Excellent | High | Low | SQL-centric |
| JDBC Template | Low | Good | High | Low | Simple queries |
| Named JDBC Template | Low | Good | High | Low | Readable queries |
| Simple JDBC Insert | Low | Good | None | Very Low | Simple inserts |
| Simple JDBC Call | Medium | Good | None | Medium | Stored procedures |

---

## 🚀 Getting Started

### Prerequisites
- Java 11 or higher
- Maven or Gradle
- Spring Boot 2.5+
- Database (H2, MySQL, PostgreSQL, etc.)

### Dependencies

```xml
<dependencies>
    <!-- Spring Boot Starter Data JPA -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    
    <!-- Spring Boot Starter JDBC -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-jdbc</artifactId>
    </dependency>
    
    <!-- Hibernate -->
    <dependency>
        <groupId>org.hibernate</groupId>
        <artifactId>hibernate-core</artifactId>
    </dependency>
    
    <!-- MyBatis Spring Boot Starter -->
    <dependency>
        <groupId>org.mybatis.spring.boot</groupId>
        <artifactId>mybatis-spring-boot-starter</artifactId>
        <version>2.3.0</version>
    </dependency>
    
    <!-- H2 Database -->
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>runtime</scope>
    </dependency>
    
    <!-- MySQL Driver (for production) -->
    <dependency>
        <groupId>mysql</groupId>
        <artifactId>mysql-connector-java</artifactId>
        <scope>runtime</scope>
    </dependency>
</dependencies>
```

---

## 📊 Performance Characteristics

### JPA/Hibernate
- **Query Performance:** Good with proper fetch strategies
- **Insert Performance:** Good, excellent with batch
- **Update Performance:** Good with dirty checking
- **Memory:** Higher due to caching
- **Best For:** Complex object graphs

### MyBatis
- **Query Performance:** Excellent (optimized SQL)
- **Insert Performance:** Excellent
- **Update Performance:** Excellent
- **Memory:** Low overhead
- **Best For:** SQL-centric applications

### JDBC Template
- **Query Performance:** Excellent (direct JDBC)
- **Insert Performance:** Very Good
- **Update Performance:** Very Good
- **Memory:** Minimal overhead
- **Best For:** Simple CRUD operations

---

## 🎓 Best Practices

### 1. Choosing the Right Pattern

**Use JPA/Hibernate when:**
- Need object-oriented domain model
- Complex relationships
- Vendor independence required
- Standard JPA compliance needed

**Use MyBatis when:**
- Need full SQL control
- Complex queries
- Legacy database schemas
- Performance-critical SQL

**Use JDBC Template when:**
- Simple CRUD operations
- No ORM overhead needed
- Direct SQL control
- Lightweight solution

### 2. Performance Optimization

```properties
# Connection Pool (HikariCP)
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5

# JPA/Hibernate Batch Size
spring.jpa.properties.hibernate.jdbc.batch_size=20

# Enable Second-Level Cache
spring.jpa.properties.hibernate.cache.use_second_level_cache=true

# Query Cache
spring.jpa.properties.hibernate.cache.use_query_cache=true
```

### 3. Transaction Management

```java
@Transactional(readOnly = true) // For read operations
public List<Entity> findAll() {
    return repository.findAll();
}

@Transactional // For write operations
public void save(Entity entity) {
    repository.save(entity);
}

@Transactional(propagation = Propagation.REQUIRES_NEW)
public void independentTransaction() {
    // New transaction
}
```

### 4. Error Handling

```java
try {
    jdbcTemplate.update(sql, params);
} catch (DataAccessException e) {
    // Handle specific exceptions
    if (e instanceof DuplicateKeyException) {
        // Handle duplicate key
    } else if (e instanceof DataIntegrityViolationException) {
        // Handle constraint violation
    }
}
```

---

## 🐛 Common Pitfalls

### 1. N+1 Query Problem

❌ **Problem:**
```java
List<Order> orders = orderRepository.findAll();
for (Order order : orders) {
    order.getCustomer().getName(); // N+1 queries!
}
```

✅ **Solution:**
```java
@Query("SELECT o FROM Order o JOIN FETCH o.customer")
List<Order> findAllWithCustomer();
```

### 2. Connection Leaks

❌ **Problem:**
```java
Connection conn = dataSource.getConnection();
// ... operations without closing
```

✅ **Solution:**
```java
try (Connection conn = dataSource.getConnection()) {
    // operations
} // Auto-closed
```

### 3. LazyInitializationException

❌ **Problem:**
```java
@Transactional
public Order getOrder(Long id) {
    return repository.findById(id);
}
// Later outside transaction
order.getItems().size(); // LazyInitializationException!
```

✅ **Solution:**
```java
@Transactional(readOnly = true)
public Order getOrderWithItems(Long id) {
    Order order = repository.findById(id);
    order.getItems().size(); // Initialize within transaction
    return order;
}
```

### 4. Inefficient Batch Operations

❌ **Problem:**
```java
for (Entity entity : entities) {
    repository.save(entity); // Multiple DB calls
}
```

✅ **Solution:**
```java
repository.saveAll(entities); // Batch operation
```

---

## 🧪 Testing Strategies

### Unit Testing with H2

```java
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class RepositoryTest {
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private UserRepository repository;
    
    @Test
    void testFindByEmail() {
        User user = new User("test@example.com");
        entityManager.persist(user);
        
        Optional<User> found = repository.findByEmail("test@example.com");
        assertTrue(found.isPresent());
    }
}
```

### Integration Testing

```java
@SpringBootTest
@Transactional
class ServiceIntegrationTest {
    @Autowired
    private UserService service;
    
    @Test
    void testCreateUser() {
        User user = service.createUser(new User("John", "Doe"));
        assertNotNull(user.getId());
    }
}
```

---

## 📚 Additional Resources

- [Spring Data JPA Documentation](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)
- [Hibernate Documentation](https://hibernate.org/orm/documentation/)
- [MyBatis Documentation](https://mybatis.org/mybatis-3/)
- [Spring JDBC Documentation](https://docs.spring.io/spring-framework/docs/current/reference/html/data-access.html#jdbc)

---

## 📝 License

These examples are provided for educational purposes.

---

**Happy Coding! 🚀**
