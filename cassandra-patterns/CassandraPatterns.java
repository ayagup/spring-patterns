package com.example.cassandra;

import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.cassandra.core.ReactiveCassandraTemplate;
import org.springframework.data.cassandra.core.cql.CqlTemplate;
import org.springframework.data.cassandra.core.query.Query;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Cassandra Patterns - Complete Collection
 * 
 * All 8 Cassandra integration patterns:
 * 1. Cassandra Template Pattern
 * 2. Cassandra Repository Pattern
 * 3. Reactive Cassandra Template Pattern
 * 4. CQL Template Pattern
 * 5. Cassandra Converter Pattern
 * 6. Cassandra Batch Pattern
 * 7. Lightweight Transaction Pattern
 * 8. Time Series Pattern
 * 
 * @author Spring Patterns
 */

@Data
class User {
    private UUID id;
    private String username;
    private String email;
    private Integer age;
    private LocalDateTime createdAt;
}

@Data
class TimeSeriesData {
    private UUID sensorId;
    private LocalDateTime timestamp;
    private Double value;
    private String unit;
}

/**
 * 1. Cassandra Template Pattern
 */
@Service
@Slf4j
class CassandraTemplateService {
    
    private final CassandraTemplate cassandraTemplate;
    
    public CassandraTemplateService(CassandraTemplate cassandraTemplate) {
        this.cassandraTemplate = cassandraTemplate;
    }
    
    public User createUser(User user) {
        return cassandraTemplate.insert(user);
    }
    
    public User updateUser(User user) {
        return cassandraTemplate.update(user);
    }
    
    public void deleteUser(UUID id) {
        cassandraTemplate.deleteById(id, User.class);
    }
    
    public User findUserById(UUID id) {
        return cassandraTemplate.selectOneById(id, User.class);
    }
    
    public List<User> findAllUsers() {
        return cassandraTemplate.select(Query.empty(), User.class);
    }
    
    public String getInfo() {
        return """
                Cassandra Template Pattern
                =========================
                
                Operations:
                - insert() - Insert row
                - update() - Update row
                - delete() - Delete row
                - select() - Query rows
                - selectOne() - Single row
                - selectOneById() - By primary key
                - count() - Count rows
                - exists() - Check existence
                
                Query Building:
                Query query = Query.query(
                    Criteria.where("username").is("john")
                ).limit(10);
                
                List<User> users = cassandraTemplate.select(query, User.class);
                
                Insert Options:
                InsertOptions options = InsertOptions.builder()
                    .ttl(Duration.ofHours(24))
                    .ifNotExists()
                    .build();
                
                cassandraTemplate.insert(user, options);
                
                Update Options:
                UpdateOptions options = UpdateOptions.builder()
                    .ifExists()
                    .build();
                """;
    }
}

/**
 * 2. Cassandra Repository Pattern
 */
interface UserRepository extends CassandraRepository<User, UUID> {
    List<User> findByUsername(String username);
    List<User> findByAgeGreaterThan(int age);
}

@Service
@Slf4j
class CassandraRepositoryService {
    
    private final UserRepository userRepository;
    
    public CassandraRepositoryService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    public String getInfo() {
        return """
                Cassandra Repository Pattern
                ===========================
                
                Entity:
                @Table("users")
                public class User {
                    @PrimaryKey
                    private UUID id;
                    
                    @Column("username")
                    private String username;
                    
                    @Column("email")
                    private String email;
                    
                    @Column("created_at")
                    private LocalDateTime createdAt;
                }
                
                Composite Key:
                @PrimaryKeyClass
                public class UserKey {
                    @PrimaryKeyColumn(
                        name = "department", 
                        ordinal = 0, 
                        type = PrimaryKeyType.PARTITIONED
                    )
                    private String department;
                    
                    @PrimaryKeyColumn(
                        name = "user_id", 
                        ordinal = 1, 
                        type = PrimaryKeyType.CLUSTERED
                    )
                    private UUID userId;
                }
                
                Repository:
                public interface UserRepository 
                    extends CassandraRepository<User, UUID> {
                    
                    List<User> findByDepartment(String department);
                    
                    @Query("SELECT * FROM users WHERE age > ?0")
                    List<User> findUsersOlderThan(int age);
                }
                
                Usage:
                User user = new User();
                userRepository.save(user);
                
                Optional<User> found = userRepository.findById(id);
                List<User> all = userRepository.findAll();
                """;
    }
}

/**
 * 3. Reactive Cassandra Template Pattern
 */
@Service
@Slf4j
class ReactiveCassandraTemplateService {
    
    private final ReactiveCassandraTemplate reactiveCassandraTemplate;
    
    public ReactiveCassandraTemplateService(ReactiveCassandraTemplate reactiveCassandraTemplate) {
        this.reactiveCassandraTemplate = reactiveCassandraTemplate;
    }
    
    public Mono<User> createUser(User user) {
        return reactiveCassandraTemplate.insert(user);
    }
    
    public Flux<User> findAllUsers() {
        return reactiveCassandraTemplate.selectAll(User.class);
    }
    
    public String getInfo() {
        return """
                Reactive Cassandra Template Pattern
                ==================================
                
                Operations:
                Mono<User> user = template.insert(user);
                Flux<User> users = template.selectAll(User.class);
                Mono<Boolean> deleted = template.delete(user);
                
                Reactive Queries:
                Query query = Query.query(
                    Criteria.where("age").gte(18)
                );
                
                Flux<User> adults = template.select(query, User.class)
                    .limitRate(100)
                    .buffer(50);
                
                Backpressure:
                template.selectAll(User.class)
                    .take(1000)
                    .delayElements(Duration.ofMillis(10))
                    .subscribe();
                """;
    }
}

/**
 * 4-8. Advanced Patterns
 */
@Service
@Slf4j
class CassandraAdvancedPatternsService {
    
    private final CqlTemplate cqlTemplate;
    private final CassandraTemplate cassandraTemplate;
    
    public CassandraAdvancedPatternsService(
            CqlTemplate cqlTemplate,
            CassandraTemplate cassandraTemplate) {
        this.cqlTemplate = cqlTemplate;
        this.cassandraTemplate = cassandraTemplate;
    }
    
    /**
     * 4. CQL Template Pattern
     */
    public void executeCql(String cql) {
        cqlTemplate.execute(cql);
    }
    
    public String getCqlTemplateInfo() {
        return """
                CQL Template Pattern
                ===================
                
                Purpose:
                - Execute raw CQL statements
                - Low-level Cassandra access
                - Custom queries
                
                Usage:
                cqlTemplate.execute(
                    "CREATE TABLE IF NOT EXISTS users (id UUID PRIMARY KEY, username TEXT)"
                );
                
                Query with Parameters:
                List<User> users = cqlTemplate.query(
                    "SELECT * FROM users WHERE age > ?",
                    new Object[]{18},
                    (row, rowNum) -> {
                        User user = new User();
                        user.setId(row.getUUID("id"));
                        user.setUsername(row.getString("username"));
                        return user;
                    }
                );
                
                Prepared Statements:
                PreparedStatement ps = session.prepare(
                    "INSERT INTO users (id, username) VALUES (?, ?)"
                );
                
                BoundStatement bound = ps.bind(UUID.randomUUID(), "john");
                cqlTemplate.execute(bound);
                
                Batch Execution:
                cqlTemplate.execute(
                    "BEGIN BATCH " +
                    "INSERT INTO users (id, username) VALUES (?, ?); " +
                    "INSERT INTO audit (id, action) VALUES (?, ?); " +
                    "APPLY BATCH"
                );
                """;
    }
    
    /**
     * 5. Cassandra Converter Pattern
     */
    public String getConverterInfo() {
        return """
                Cassandra Converter Pattern
                ==========================
                
                Purpose:
                - Custom type conversion
                - Complex object mapping
                - Date/time handling
                
                Custom Converter:
                @WritingConverter
                public class PersonToRowConverter 
                    implements Converter<Person, Row> {
                    
                    @Override
                    public Row convert(Person person) {
                        Row row = new Row();
                        row.set("id", person.getId(), UUID.class);
                        row.set("name", person.getName(), String.class);
                        return row;
                    }
                }
                
                @ReadingConverter
                public class RowToPersonConverter 
                    implements Converter<Row, Person> {
                    
                    @Override
                    public Person convert(Row row) {
                        Person person = new Person();
                        person.setId(row.getUUID("id"));
                        person.setName(row.getString("name"));
                        return person;
                    }
                }
                
                Registration:
                @Configuration
                public class CassandraConfig {
                    @Bean
                    public CassandraCustomConversions customConversions() {
                        return new CassandraCustomConversions(
                            Arrays.asList(
                                new PersonToRowConverter(),
                                new RowToPersonConverter()
                            )
                        );
                    }
                }
                """;
    }
    
    /**
     * 6. Cassandra Batch Pattern
     */
    public String getBatchInfo() {
        return """
                Cassandra Batch Pattern
                ======================
                
                Purpose:
                - Atomic operations
                - Multiple inserts/updates
                - Cross-table consistency
                
                Logged Batch:
                cqlTemplate.execute(
                    "BEGIN BATCH " +
                    "INSERT INTO users (id, username) VALUES (uuid(), 'john'); " +
                    "INSERT INTO users (id, username) VALUES (uuid(), 'jane'); " +
                    "APPLY BATCH"
                );
                
                Unlogged Batch:
                cqlTemplate.execute(
                    "BEGIN UNLOGGED BATCH " +
                    "INSERT INTO users (id, username) VALUES (uuid(), 'john'); " +
                    "INSERT INTO users (id, username) VALUES (uuid(), 'jane'); " +
                    "APPLY BATCH"
                );
                
                Counter Batch:
                cqlTemplate.execute(
                    "BEGIN COUNTER BATCH " +
                    "UPDATE page_views SET views = views + 1 WHERE page = 'home'; " +
                    "UPDATE page_views SET views = views + 1 WHERE page = 'about'; " +
                    "APPLY BATCH"
                );
                
                Best Practices:
                - Keep batches small (<100 rows)
                - Batch same partition key
                - Use unlogged for performance
                - Logged for cross-partition atomicity
                """;
    }
    
    /**
     * 7. Lightweight Transaction Pattern
     */
    public String getLwtInfo() {
        return """
                Lightweight Transaction Pattern
                ==============================
                
                Purpose:
                - Compare-and-set operations
                - Conditional updates
                - Linearizable consistency
                
                IF NOT EXISTS:
                cqlTemplate.execute(
                    "INSERT INTO users (id, username, email) " +
                    "VALUES (uuid(), 'john', 'john@example.com') " +
                    "IF NOT EXISTS"
                );
                
                IF EXISTS:
                cqlTemplate.execute(
                    "UPDATE users SET email = 'new@example.com' " +
                    "WHERE id = ? " +
                    "IF EXISTS",
                    userId
                );
                
                IF Condition:
                cqlTemplate.execute(
                    "UPDATE accounts SET balance = balance - 100 " +
                    "WHERE id = ? " +
                    "IF balance >= 100",
                    accountId
                );
                
                Check Result:
                ResultSet rs = cqlTemplate.queryForResultSet(
                    "INSERT INTO users (id, username) VALUES (?, ?) IF NOT EXISTS",
                    userId, username
                );
                
                boolean applied = rs.one().getBoolean("[applied]");
                if (!applied) {
                    // Insert failed, handle conflict
                }
                
                Performance:
                - Slower than normal writes
                - Uses Paxos consensus
                - Use sparingly
                - Best for critical operations
                """;
    }
    
    /**
     * 8. Time Series Pattern
     */
    public String getTimeSeriesInfo() {
        return """
                Time Series Pattern
                ==================
                
                Schema Design:
                CREATE TABLE sensor_data (
                    sensor_id UUID,
                    bucket_date DATE,
                    timestamp TIMESTAMP,
                    value DOUBLE,
                    unit TEXT,
                    PRIMARY KEY ((sensor_id, bucket_date), timestamp)
                ) WITH CLUSTERING ORDER BY (timestamp DESC);
                
                Entity:
                @Table("sensor_data")
                public class SensorData {
                    @PrimaryKeyColumn(
                        name = "sensor_id",
                        ordinal = 0,
                        type = PrimaryKeyType.PARTITIONED
                    )
                    private UUID sensorId;
                    
                    @PrimaryKeyColumn(
                        name = "bucket_date",
                        ordinal = 1,
                        type = PrimaryKeyType.PARTITIONED
                    )
                    private LocalDate bucketDate;
                    
                    @PrimaryKeyColumn(
                        name = "timestamp",
                        ordinal = 2,
                        type = PrimaryKeyType.CLUSTERED,
                        ordering = Ordering.DESCENDING
                    )
                    private LocalDateTime timestamp;
                    
                    private Double value;
                    private String unit;
                }
                
                Query Patterns:
                // Recent data for sensor
                SELECT * FROM sensor_data 
                WHERE sensor_id = ? 
                  AND bucket_date = ? 
                  AND timestamp >= ?
                LIMIT 1000;
                
                // Range query
                SELECT * FROM sensor_data 
                WHERE sensor_id = ? 
                  AND bucket_date IN (?, ?, ?)
                  AND timestamp >= ? 
                  AND timestamp <= ?;
                
                TTL for Auto-expiry:
                INSERT INTO sensor_data 
                    (sensor_id, bucket_date, timestamp, value) 
                VALUES (?, ?, ?, ?) 
                USING TTL 86400;  -- 24 hours
                
                Best Practices:
                - Partition by time bucket (day/hour)
                - Limit partition size (<100MB)
                - Use TTL for automatic cleanup
                - Clustering order DESC for recent data
                - Consider bucketing strategy
                """;
    }
}

/**
 * REST Controller
 */
@RestController
@RequestMapping("/cassandra/patterns")
@Slf4j
class CassandraPatternsController {
    
    private final CassandraTemplateService templateService;
    private final CassandraRepositoryService repositoryService;
    private final ReactiveCassandraTemplateService reactiveService;
    private final CassandraAdvancedPatternsService advancedService;
    
    public CassandraPatternsController(
            CassandraTemplateService templateService,
            CassandraRepositoryService repositoryService,
            ReactiveCassandraTemplateService reactiveService,
            CassandraAdvancedPatternsService advancedService) {
        this.templateService = templateService;
        this.repositoryService = repositoryService;
        this.reactiveService = reactiveService;
        this.advancedService = advancedService;
    }
    
    @GetMapping("/template")
    public String getTemplateInfo() {
        return templateService.getInfo();
    }
    
    @GetMapping("/repository")
    public String getRepositoryInfo() {
        return repositoryService.getInfo();
    }
    
    @GetMapping("/reactive-template")
    public String getReactiveTemplateInfo() {
        return reactiveService.getInfo();
    }
    
    @GetMapping("/cql-template")
    public String getCqlTemplateInfo() {
        return advancedService.getCqlTemplateInfo();
    }
    
    @GetMapping("/converter")
    public String getConverterInfo() {
        return advancedService.getConverterInfo();
    }
    
    @GetMapping("/batch")
    public String getBatchInfo() {
        return advancedService.getBatchInfo();
    }
    
    @GetMapping("/lwt")
    public String getLwtInfo() {
        return advancedService.getLwtInfo();
    }
    
    @GetMapping("/time-series")
    public String getTimeSeriesInfo() {
        return advancedService.getTimeSeriesInfo();
    }
}

@SpringBootApplication
public class CassandraPatterns {
    public static void main(String[] args) {
        SpringApplication.run(CassandraPatterns.class, args);
    }
}
