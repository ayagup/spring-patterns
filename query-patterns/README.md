# Spring Query Patterns

This directory contains comprehensive implementations of all major query patterns used in Spring Data JPA applications.

## 📋 Table of Contents

1. [Overview](#overview)
2. [Pattern Descriptions](#pattern-descriptions)
3. [Comparison Matrix](#comparison-matrix)
4. [When to Use Each Pattern](#when-to-use-each-pattern)
5. [Configuration](#configuration)
6. [Best Practices](#best-practices)
7. [Performance Considerations](#performance-considerations)
8. [Common Pitfalls](#common-pitfalls)

## Overview

Query patterns in Spring Data JPA provide different approaches to retrieving data from databases. Each pattern has its strengths, use cases, and trade-offs. This collection demonstrates all 10 major query patterns with complete, runnable examples.

### Patterns Included

1. **Query DSL Pattern** - Type-safe query construction
2. **Criteria API Pattern** - JPA standard dynamic queries
3. **Named Query Pattern** - Pre-defined static queries
4. **Native Query Pattern** - Database-specific SQL queries
5. **JPQL Pattern** - Object-oriented query language
6. **Query by Example Pattern** - Probe-based dynamic queries
7. **Specification Pattern** - Composable query specifications
8. **Querydsl Predicate Pattern** - Predicate-based repository queries
9. **Dynamic Query Pattern** - Runtime query construction
10. **Stored Procedure Pattern** - Database stored procedure calls

## Pattern Descriptions

### 1. Query DSL Pattern
**File:** `QueryDSLPattern.java`

Uses Querydsl library for type-safe, fluent query construction.

**Key Features:**
- Compile-time type checking
- IDE autocomplete support
- Fluent API
- Q-classes generation
- Complex query support

**Example:**
```java
QProduct product = QProduct.product;
BooleanExpression predicate = product.category.eq("Electronics")
                                             .and(product.price.lt(1000));
List<Product> results = productRepository.findAll(predicate);
```

### 2. Criteria API Pattern
**File:** `CriteriaAPIPattern.java`

JPA standard for building dynamic, type-safe queries programmatically.

**Key Features:**
- JPA standard (vendor-neutral)
- Type-safe at runtime
- Dynamic query construction
- Complex join support
- Metamodel integration

**Example:**
```java
CriteriaBuilder cb = entityManager.getCriteriaBuilder();
CriteriaQuery<Order> query = cb.createQuery(Order.class);
Root<Order> root = query.from(Order.class);
query.where(cb.equal(root.get("status"), OrderStatus.PENDING));
```

### 3. Named Query Pattern
**File:** `NamedQueryPattern.java`

Static queries defined using @NamedQuery annotation.

**Key Features:**
- Validated at startup
- Centralized definition
- Pre-compiled for performance
- Reusable across repositories
- Support for native queries

**Example:**
```java
@NamedQuery(
    name = "Employee.findByDepartment",
    query = "SELECT e FROM Employee e WHERE e.department = :department"
)
```

### 4. Native Query Pattern
**File:** `NativeQueryPattern.java`

Direct SQL queries using database-specific syntax.

**Key Features:**
- Full database feature access
- Database-specific optimizations
- Complex queries support
- Window functions, CTEs
- Bulk operations

**Example:**
```java
@Query(value = "SELECT * FROM accounts WHERE balance >= :minBalance", 
       nativeQuery = true)
List<Account> findByMinimumBalanceNative(@Param("minBalance") BigDecimal minBalance);
```

### 5. JPQL Pattern
**File:** `JPQLPattern.java`

Java Persistence Query Language - object-oriented query language.

**Key Features:**
- Database independent
- Entity-based (not table-based)
- Polymorphism support
- JOIN FETCH for eager loading
- Aggregate functions

**Example:**
```java
@Query("SELECT c FROM Customer c WHERE c.status = :status ORDER BY c.totalPurchases DESC")
List<Customer> findByStatus(@Param("status") CustomerStatus status);
```

### 6. Query by Example Pattern
**File:** `QueryByExamplePattern.java`

Probe-based dynamic queries using Example API.

**Key Features:**
- No annotations needed
- Simple dynamic queries
- ExampleMatcher customization
- Type-safe
- Easy to use

**Example:**
```java
Book probe = new Book();
probe.setAuthor("Martin");
probe.setGenre("Programming");

ExampleMatcher matcher = ExampleMatcher.matching()
    .withIgnoreCase()
    .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);

Example<Book> example = Example.of(probe, matcher);
List<Book> results = bookRepository.findAll(example);
```

### 7. Specification Pattern
**File:** `SpecificationPattern.java`

Reusable, composable query specifications using JPA Criteria API.

**Key Features:**
- Reusable query components
- Composable with and/or/not
- Type-safe
- Testable in isolation
- Clean separation of concerns

**Example:**
```java
Specification<Invoice> spec = Specification
    .where(InvoiceSpecs.hasStatus(status))
    .and(InvoiceSpecs.amountBetween(minAmount, maxAmount))
    .and(InvoiceSpecs.issuedBetween(startDate, endDate));

List<Invoice> invoices = invoiceRepository.findAll(spec);
```

### 8. Querydsl Predicate Pattern
**File:** `QuerydslPredicatePattern.java`

Predicate-based repository queries using Querydsl.

**Key Features:**
- Similar to Query DSL
- Repository integration
- Predicate composition
- Type-safe
- Web support via QuerydslBinderCustomizer

### 9. Dynamic Query Pattern
**File:** `DynamicQueryPattern.java`

Building queries dynamically at runtime using Criteria API.

**Key Features:**
- Runtime query construction
- Conditional predicate building
- Flexible filtering
- Dynamic sorting
- Parameter-based queries

**Example:**
```java
List<Predicate> predicates = new ArrayList<>();
if (name != null) {
    predicates.add(cb.like(root.get("name"), "%" + name + "%"));
}
if (status != null) {
    predicates.add(cb.equal(root.get("status"), status));
}
query.where(cb.and(predicates.toArray(new Predicate[0])));
```

### 10. Stored Procedure Pattern
**File:** `StoredProcedurePattern.java`

Calling database stored procedures.

**Key Features:**
- Complex database logic
- IN/OUT/INOUT parameters
- Result set mapping
- @Procedure annotation support
- Named procedure queries

**Example:**
```java
@NamedStoredProcedureQuery(
    name = "Transaction.getTotalByType",
    procedureName = "get_total_by_type",
    parameters = {
        @StoredProcedureParameter(mode = ParameterMode.IN, name = "txn_type", type = String.class),
        @StoredProcedureParameter(mode = ParameterMode.OUT, name = "total", type = BigDecimal.class)
    }
)
```

## Comparison Matrix

| Pattern | Type Safety | Complexity | Performance | Database Portability | Dynamic | Learning Curve |
|---------|-------------|------------|-------------|---------------------|---------|----------------|
| Query DSL | ✅ Compile-time | Medium | High | ✅ Excellent | ✅ Yes | Medium |
| Criteria API | ⚠️ Runtime | High | High | ✅ Excellent | ✅ Yes | High |
| Named Query | ❌ Runtime | Low | Very High* | ✅ Excellent | ❌ No | Low |
| Native Query | ❌ None | Low-High | Very High | ❌ Poor | ⚠️ Limited | Low |
| JPQL | ⚠️ Runtime | Low-Medium | High | ✅ Excellent | ⚠️ Limited | Low |
| Query by Example | ✅ Compile-time | Very Low | Medium | ✅ Excellent | ✅ Yes | Very Low |
| Specification | ⚠️ Runtime | Medium | High | ✅ Excellent | ✅ Yes | Medium |
| Querydsl Predicate | ✅ Compile-time | Low-Medium | High | ✅ Excellent | ✅ Yes | Medium |
| Dynamic Query | ⚠️ Runtime | Medium-High | High | ✅ Excellent | ✅ Yes | Medium-High |
| Stored Procedure | ❌ None | Low-High | Very High | ❌ Poor | ❌ No | Medium |

*Pre-compiled and cached

## When to Use Each Pattern

### Use Query DSL When:
- ✅ Need compile-time type safety
- ✅ Complex dynamic queries required
- ✅ Want excellent IDE support
- ✅ Building domain-specific query language
- ❌ Simple CRUD operations only
- ❌ Cannot add build dependencies

### Use Criteria API When:
- ✅ Need JPA standard solution
- ✅ Database portability is critical
- ✅ Cannot use third-party libraries
- ✅ Complex dynamic queries
- ❌ Simple queries
- ❌ Want easier syntax

### Use Named Query When:
- ✅ Queries are static and reused
- ✅ Want startup validation
- ✅ Performance is critical
- ✅ Centralized query management
- ❌ Dynamic filtering needed
- ❌ Queries change frequently

### Use Native Query When:
- ✅ Database-specific features needed (CTEs, window functions)
- ✅ Complex reporting queries
- ✅ Performance optimization required
- ✅ Legacy database integration
- ❌ Database portability needed
- ❌ Simple entity operations

### Use JPQL When:
- ✅ Need database independence
- ✅ Entity relationship navigation
- ✅ Moderate complexity queries
- ✅ Standard JPA compliance
- ❌ Database-specific features needed
- ❌ Very complex queries

### Use Query by Example When:
- ✅ Simple dynamic search forms
- ✅ Prototype-based searches
- ✅ Quick and dirty filtering
- ✅ Minimal code desired
- ❌ Complex queries with joins
- ❌ OR conditions needed
- ❌ Numeric ranges required

### Use Specification When:
- ✅ Reusable query components needed
- ✅ Complex business rules
- ✅ Composable query logic
- ✅ Testable query components
- ❌ Very simple queries
- ❌ One-off searches

### Use Querydsl Predicate When:
- ✅ Using Query DSL already
- ✅ Want repository integration
- ✅ Web query parameter binding
- ✅ Type-safe predicates
- ❌ Cannot use Querydsl library

### Use Dynamic Query When:
- ✅ Search filters are optional
- ✅ User-driven dynamic queries
- ✅ Conditional query building
- ✅ Cannot use Specification pattern
- ❌ Query logic is reusable
- ❌ Simple static queries

### Use Stored Procedure When:
- ✅ Complex database logic
- ✅ Existing procedures
- ✅ Performance-critical operations
- ✅ Batch processing
- ❌ Need database portability
- ❌ Simple CRUD operations

## Configuration

### Maven Dependencies

```xml
<dependencies>
    <!-- Spring Data JPA -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    
    <!-- Database (H2 for examples) -->
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>runtime</scope>
    </dependency>
    
    <!-- Querydsl (for Query DSL patterns) -->
    <dependency>
        <groupId>com.querydsl</groupId>
        <artifactId>querydsl-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>com.querydsl</groupId>
        <artifactId>querydsl-apt</artifactId>
        <scope>provided</scope>
    </dependency>
    
    <!-- Web (for REST controllers) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
</dependencies>
```

### Application Properties

```properties
# Database Configuration
spring.datasource.url=jdbc:h2:mem:querydb
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# JPA Configuration
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect

# Query Caching (for Named Queries)
spring.jpa.properties.hibernate.cache.use_query_cache=true
spring.jpa.properties.hibernate.cache.use_second_level_cache=true

# Logging
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
```

### Querydsl Q-Class Generation

Add to `pom.xml`:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>com.mysema.maven</groupId>
            <artifactId>apt-maven-plugin</artifactId>
            <version>1.1.3</version>
            <executions>
                <execution>
                    <goals>
                        <goal>process</goal>
                    </goals>
                    <configuration>
                        <outputDirectory>target/generated-sources/java</outputDirectory>
                        <processor>com.querydsl.apt.jpa.JPAAnnotationProcessor</processor>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

## Best Practices

### General Best Practices

1. **Choose the Right Pattern**
   - Use simplest pattern that meets requirements
   - Consider maintainability over cleverness
   - Think about team expertise

2. **Performance Optimization**
   - Use fetch joins to avoid N+1 queries
   - Implement pagination for large result sets
   - Add appropriate database indexes
   - Use projections for read-only queries
   - Monitor query performance in production

3. **Type Safety**
   - Prefer compile-time type safety when possible
   - Use Querydsl or Specifications for complex queries
   - Validate dynamic queries

4. **Code Organization**
   - Keep query logic close to entities
   - Use separate repository classes for complex queries
   - Create reusable Specifications
   - Document complex query logic

5. **Testing**
   - Test queries with various data scenarios
   - Use @DataJpaTest for repository testing
   - Test edge cases (null values, empty results)
   - Verify query performance

### Pattern-Specific Best Practices

#### Query DSL
- Generate Q-classes during build
- Use static imports for cleaner code
- Create reusable predicate factories
- Leverage JPAQueryFactory for complex queries

#### Criteria API
- Use Metamodel for type-safe attribute access
- Build predicates list for dynamic queries
- Combine with Specification pattern
- Cache CriteriaBuilder instances

#### Named Queries
- Name queries consistently (Entity.operation)
- Document query purpose and parameters
- Use query caching when appropriate
- Externalize to orm.xml for very large queries

#### Native Queries
- Document database-specific features used
- Use only when necessary
- Handle database portability explicitly
- Test against actual database

#### JPQL
- Use named parameters, not positional
- Use JOIN FETCH for eager loading
- Constructor expressions for DTOs
- Avoid Cartesian products

#### Query by Example
- Use for simple search forms only
- Customize ExampleMatcher appropriately
- Understand limitations (no OR, limited ranges)
- Consider Specification for complex cases

#### Specification
- Create atomic, reusable specifications
- Handle null values gracefully
- Use descriptive method names
- Test specifications in isolation

#### Dynamic Queries
- Validate input parameters
- Build predicates only for non-null values
- Use consistent sorting/paging
- Consider using Specification pattern instead

#### Stored Procedures
- Document procedure contract clearly
- Handle NULL values appropriately
- Use transactions correctly
- Test procedures independently

## Performance Considerations

### Query Optimization

1. **N+1 Query Problem**
   ```java
   // BAD - Causes N+1 queries
   @Query("SELECT c FROM Customer c")
   List<Customer> findAll();
   
   // GOOD - Single query with JOIN FETCH
   @Query("SELECT DISTINCT c FROM Customer c LEFT JOIN FETCH c.purchases")
   List<Customer> findAllWithPurchases();
   ```

2. **Pagination**
   ```java
   // Use Pageable for large result sets
   Page<Product> findByCategory(String category, Pageable pageable);
   ```

3. **Projections**
   ```java
   // Fetch only required fields
   @Query("SELECT NEW com.example.dto.ProductSummary(p.id, p.name, p.price) " +
          "FROM Product p")
   List<ProductSummary> findAllSummaries();
   ```

4. **Indexes**
   ```java
   @Entity
   @Table(name = "products", indexes = {
       @Index(name = "idx_category", columnList = "category"),
       @Index(name = "idx_price", columnList = "price")
   })
   public class Product { }
   ```

### Caching

1. **Second-Level Cache**
   ```properties
   spring.jpa.properties.hibernate.cache.use_second_level_cache=true
   spring.jpa.properties.hibernate.cache.region.factory_class=org.hibernate.cache.jcache.JCacheRegionFactory
   ```

2. **Query Cache**
   ```java
   TypedQuery<Employee> query = entityManager.createNamedQuery("Employee.findByDepartment", Employee.class);
   query.setHint("org.hibernate.cacheable", true);
   ```

## Common Pitfalls

### 1. N+1 Queries
**Problem:** Lazy loading triggers additional queries for each entity.

**Solution:** Use JOIN FETCH or EntityGraph.

### 2. Cartesian Products
**Problem:** Multiple JOIN FETCH on collections.

**Solution:** Use multiple queries or @EntityGraph.

### 3. LazyInitializationException
**Problem:** Accessing lazy collections outside transaction.

**Solution:** Use JOIN FETCH, @Transactional, or DTOs.

### 4. Unindexed Queries
**Problem:** Slow queries on large tables.

**Solution:** Add database indexes on queried columns.

### 5. Over-fetching
**Problem:** Selecting entire entities when only few fields needed.

**Solution:** Use DTOs or projections.

### 6. String-based Queries
**Problem:** Typos and refactoring issues.

**Solution:** Use type-safe alternatives (Querydsl, Criteria API).

### 7. Missing Pagination
**Problem:** Loading entire tables into memory.

**Solution:** Always use Pageable for potentially large results.

### 8. Ignoring Database Portability
**Problem:** Using database-specific features in portable applications.

**Solution:** Stick to JPQL/Criteria API or abstract database differences.

### 9. Complex Specifications
**Problem:** Unreadable and hard to maintain specifications.

**Solution:** Break into smaller, well-named methods.

### 10. Premature Optimization
**Problem:** Complex queries for small datasets.

**Solution:** Start simple, optimize when necessary.

## Testing

### Repository Testing

```java
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProductRepositoryTest {
    
    @Autowired
    private ProductRepository productRepository;
    
    @Test
    void testFindByCategory() {
        Product product = new Product("Laptop", "Electronics", new BigDecimal("999"));
        productRepository.save(product);
        
        List<Product> results = productRepository.findByCategory("Electronics");
        
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Laptop");
    }
}
```

### Specification Testing

```java
class InvoiceSpecsTest {
    
    @Test
    void testHasStatusSpecification() {
        Invoice invoice = new Invoice();
        invoice.setStatus(InvoiceStatus.PAID);
        
        Specification<Invoice> spec = InvoiceSpecs.hasStatus(InvoiceStatus.PAID);
        
        // Test specification logic
        assertThat(spec).isNotNull();
    }
}
```

## Summary

This collection provides comprehensive examples of all major Spring Data JPA query patterns. Choose the appropriate pattern based on:

- **Complexity** - Simple vs. complex queries
- **Type Safety** - Compile-time vs. runtime checking
- **Performance** - Query optimization needs
- **Portability** - Database independence requirements
- **Maintainability** - Team expertise and long-term maintenance
- **Dynamism** - Static vs. dynamic query requirements

Each pattern has its place in a well-designed application. Understanding when and how to use each pattern will help you build efficient, maintainable data access layers.

## Additional Resources

- [Spring Data JPA Documentation](https://spring.io/projects/spring-data-jpa)
- [Querydsl Documentation](http://www.querydsl.com/)
- [JPA Specification](https://jakarta.ee/specifications/persistence/)
- [Hibernate Documentation](https://hibernate.org/orm/documentation/)

---

**Note:** All patterns are demonstrated with complete, runnable Spring Boot applications. Each file contains detailed JavaDoc comments, examples, and best practices.
