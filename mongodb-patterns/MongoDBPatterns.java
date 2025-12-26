package com.example.mongodb;

import com.mongodb.client.result.UpdateResult;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

/**
 * MongoDB Patterns - Complete Collection
 * 
 * All 10 MongoDB integration patterns:
 * 1. Mongo Template Pattern
 * 2. Mongo Repository Pattern
 * 3. Reactive Mongo Template Pattern
 * 4. Reactive Mongo Repository Pattern
 * 5. Document Converter Pattern
 * 6. Aggregation Pattern
 * 7. GridFS Pattern
 * 8. Change Stream Pattern
 * 9. Transaction Pattern
 * 10. Mongo Client Settings Pattern
 * 
 * @author Spring Patterns
 */

@Document(collection = "users")
@Data
class User {
    @Id
    private String id;
    private String username;
    private String email;
    private Integer age;
    private String department;
    private LocalDateTime createdAt;
    private List<String> tags;
}

@Document(collection = "products")
@Data
class Product {
    @Id
    private String id;
    private String name;
    private Double price;
    private String category;
    private Integer stock;
}

/**
 * 1. Mongo Template Pattern
 * Core MongoDB operations template
 */
@Service
@Slf4j
class MongoTemplateService {
    
    private final MongoTemplate mongoTemplate;
    
    public MongoTemplateService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }
    
    public User createUser(User user) {
        return mongoTemplate.insert(user);
    }
    
    public User updateUser(String id, User user) {
        Query query = new Query(Criteria.where("id").is(id));
        Update update = new Update()
            .set("email", user.getEmail())
            .set("age", user.getAge());
        UpdateResult result = mongoTemplate.updateFirst(query, update, User.class);
        log.info("Updated {} documents", result.getModifiedCount());
        return mongoTemplate.findById(id, User.class);
    }
    
    public void deleteUser(String id) {
        Query query = new Query(Criteria.where("id").is(id));
        mongoTemplate.remove(query, User.class);
    }
    
    public List<User> findUsers(String department) {
        Query query = new Query(Criteria.where("department").is(department));
        return mongoTemplate.find(query, User.class);
    }
    
    public long countUsers() {
        return mongoTemplate.count(new Query(), User.class);
    }
    
    public String getInfo() {
        return """
                Mongo Template Pattern
                =====================
                
                Operations:
                - insert() - Insert document
                - save() - Insert or update
                - updateFirst() - Update one
                - updateMulti() - Update many
                - upsert() - Update or insert
                - remove() - Delete documents
                - findOne() - Find single
                - find() - Find multiple
                - findById() - Find by ID
                - findAll() - Find all
                - count() - Count documents
                - exists() - Check existence
                
                Query Building:
                Query query = new Query()
                    .addCriteria(Criteria.where("age").gte(18))
                    .with(Sort.by("username"))
                    .limit(10)
                    .skip(20);
                
                Update Building:
                Update update = new Update()
                    .set("field", value)
                    .inc("counter", 1)
                    .push("array", item)
                    .currentDate("updated");
                """;
    }
}

/**
 * 2. Mongo Repository Pattern
 * Spring Data repository abstraction
 */
interface UserRepository extends MongoRepository<User, String> {
    List<User> findByDepartment(String department);
    List<User> findByAgeGreaterThan(int age);
    List<User> findByUsernameContaining(String pattern);
    List<User> findByEmailAndDepartment(String email, String dept);
}

@Service
@Slf4j
class MongoRepositoryService {
    
    private final UserRepository userRepository;
    
    public MongoRepositoryService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    public List<User> findByDepartment(String department) {
        return userRepository.findByDepartment(department);
    }
    
    public String getInfo() {
        return """
                Mongo Repository Pattern
                =======================
                
                Interface:
                public interface UserRepository extends MongoRepository<User, String> {
                    List<User> findByUsername(String username);
                    List<User> findByAgeGreaterThan(int age);
                    List<User> findByDepartmentAndAgeGreaterThan(String dept, int age);
                    
                    @Query("{'email': ?0}")
                    User findByEmailAddress(String email);
                    
                    @Query("{'age': {'$gte': ?0, '$lte': ?1}}")
                    List<User> findByAgeBetween(int min, int max);
                }
                
                Query Methods:
                - findBy...
                - countBy...
                - deleteBy...
                - existsBy...
                
                Keywords:
                - And, Or, Between, LessThan, GreaterThan
                - Like, NotLike, StartingWith, EndingWith
                - Containing, NotContaining
                - In, NotIn, True, False
                - OrderBy, Distinct
                """;
    }
}

/**
 * 3. Reactive Mongo Template Pattern
 * Reactive MongoDB operations
 */
@Service
@Slf4j
class ReactiveMongoTemplateService {
    
    private final ReactiveMongoTemplate reactiveMongoTemplate;
    
    public ReactiveMongoTemplateService(ReactiveMongoTemplate reactiveMongoTemplate) {
        this.reactiveMongoTemplate = reactiveMongoTemplate;
    }
    
    public Mono<User> createUser(User user) {
        return reactiveMongoTemplate.insert(user);
    }
    
    public Flux<User> findAllUsers() {
        return reactiveMongoTemplate.findAll(User.class);
    }
    
    public Mono<User> findUserById(String id) {
        return reactiveMongoTemplate.findById(id, User.class);
    }
    
    public String getInfo() {
        return """
                Reactive Mongo Template Pattern
                ===============================
                
                Operations return Mono/Flux:
                Mono<User> user = template.findById(id, User.class);
                Flux<User> users = template.findAll(User.class);
                
                Reactive Queries:
                Query query = Query.query(Criteria.where("age").gte(18));
                Flux<User> adults = template.find(query, User.class);
                
                Backpressure:
                template.find(query, User.class)
                    .limitRate(100)
                    .buffer(50)
                    .subscribe();
                """;
    }
}

/**
 * 4. Reactive Mongo Repository Pattern
 * Reactive repository abstraction
 */
interface ReactiveUserRepository extends ReactiveMongoRepository<User, String> {
    Flux<User> findByDepartment(String department);
    Mono<User> findByEmail(String email);
    Flux<User> findByAgeGreaterThan(int age);
}

@Service
@Slf4j
class ReactiveMongoRepositoryService {
    
    private final ReactiveUserRepository reactiveUserRepository;
    
    public ReactiveMongoRepositoryService(ReactiveUserRepository reactiveUserRepository) {
        this.reactiveUserRepository = reactiveUserRepository;
    }
    
    public Flux<User> findByDepartment(String department) {
        return reactiveUserRepository.findByDepartment(department);
    }
    
    public String getInfo() {
        return """
                Reactive Mongo Repository Pattern
                =================================
                
                Interface:
                public interface ReactiveUserRepository 
                    extends ReactiveMongoRepository<User, String> {
                    
                    Flux<User> findByDepartment(String dept);
                    Mono<User> findByEmail(String email);
                    Flux<User> findByAgeGreaterThan(int age);
                }
                
                Usage:
                reactiveUserRepository.findByDepartment("IT")
                    .filter(user -> user.getAge() > 25)
                    .map(User::getEmail)
                    .subscribe(System.out::println);
                """;
    }
}

/**
 * 5-10. Consolidated Advanced Patterns
 */
@Service
@Slf4j
class MongoAdvancedPatternsService {
    
    private final MongoTemplate mongoTemplate;
    
    public MongoAdvancedPatternsService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }
    
    /**
     * 6. Aggregation Pattern
     */
    public List<Object> performAggregation() {
        Aggregation aggregation = Aggregation.newAggregation(
            Aggregation.match(Criteria.where("age").gte(18)),
            Aggregation.group("department").count().as("count"),
            Aggregation.sort(Sort.Direction.DESC, "count")
        );
        return mongoTemplate.aggregate(aggregation, "users", Object.class).getMappedResults();
    }
    
    public String getAggregationInfo() {
        return """
                Aggregation Pattern
                ==================
                
                Pipeline Stages:
                Aggregation agg = Aggregation.newAggregation(
                    match(Criteria.where("age").gte(18)),
                    group("department")
                        .count().as("count")
                        .avg("age").as("avgAge"),
                    sort(Sort.Direction.DESC, "count"),
                    project("department", "count", "avgAge"),
                    limit(10)
                );
                
                Stages:
                - $match - Filter documents
                - $group - Group by field
                - $project - Select fields
                - $sort - Sort results
                - $limit - Limit results
                - $skip - Skip documents
                - $unwind - Deconstruct arrays
                - $lookup - Join collections
                - $facet - Multiple pipelines
                """;
    }
    
    /**
     * 7. GridFS Pattern
     */
    public String getGridFSInfo() {
        return """
                GridFS Pattern
                =============
                
                Purpose:
                - Store large files (>16MB)
                - Chunked storage
                - Metadata support
                
                Configuration:
                @Bean
                public GridFsTemplate gridFsTemplate(
                    MongoDatabaseFactory dbFactory,
                    MongoConverter converter) {
                    return new GridFsTemplate(dbFactory, converter);
                }
                
                Store File:
                InputStream inputStream = new FileInputStream(file);
                ObjectId id = gridFsTemplate.store(
                    inputStream,
                    "filename.pdf",
                    "application/pdf",
                    metadata
                );
                
                Retrieve File:
                GridFSFile file = gridFsTemplate.findOne(
                    Query.query(Criteria.where("_id").is(id))
                );
                GridFsResource resource = gridFsTemplate.getResource(file);
                
                Delete File:
                gridFsTemplate.delete(
                    Query.query(Criteria.where("_id").is(id))
                );
                """;
    }
    
    /**
     * 8. Change Stream Pattern
     */
    public String getChangeStreamInfo() {
        return """
                Change Stream Pattern
                ====================
                
                Purpose:
                - Real-time data changes
                - Event-driven processing
                - Replication tracking
                
                Watch Collection:
                MessageListenerContainer container = 
                    new DefaultMessageListenerContainer(mongoTemplate);
                
                ChangeStreamRequest<User> request = 
                    ChangeStreamRequest.builder()
                        .collection("users")
                        .filter(Aggregation.newAggregation(
                            Aggregation.match(Criteria.where("operationType").is("insert"))
                        ))
                        .build();
                
                container.register(request, User.class)
                    .doOnNext(message -> {
                        log.info("Change: {}", message.getBody());
                    })
                    .subscribe();
                
                Operations:
                - insert - Document inserted
                - update - Document updated
                - replace - Document replaced
                - delete - Document deleted
                - invalidate - Collection dropped
                """;
    }
    
    /**
     * 9. Transaction Pattern
     */
    @Transactional
    public void performTransaction() {
        User user = new User();
        user.setUsername("john");
        mongoTemplate.insert(user);
        
        Product product = new Product();
        product.setName("Widget");
        mongoTemplate.insert(product);
        
        // Both operations in same transaction
    }
    
    public String getTransactionInfo() {
        return """
                Transaction Pattern
                ==================
                
                Requirements:
                - MongoDB 4.0+ with replica set
                - Or MongoDB 4.2+ with sharded cluster
                
                Configuration:
                @Configuration
                public class MongoConfig {
                    @Bean
                    MongoTransactionManager transactionManager(
                        MongoDatabaseFactory dbFactory) {
                        return new MongoTransactionManager(dbFactory);
                    }
                }
                
                Usage:
                @Transactional
                public void transferMoney(String from, String to, double amount) {
                    mongoTemplate.updateFirst(
                        Query.query(Criteria.where("id").is(from)),
                        new Update().inc("balance", -amount),
                        Account.class
                    );
                    
                    mongoTemplate.updateFirst(
                        Query.query(Criteria.where("id").is(to)),
                        new Update().inc("balance", amount),
                        Account.class
                    );
                }
                
                Programmatic:
                TransactionTemplate txTemplate = new TransactionTemplate(txManager);
                txTemplate.execute(status -> {
                    // transactional operations
                    return result;
                });
                """;
    }
    
    /**
     * 10. Mongo Client Settings Pattern
     */
    public String getClientSettingsInfo() {
        return """
                Mongo Client Settings Pattern
                ============================
                
                Configuration:
                @Bean
                public MongoClient mongoClient() {
                    MongoClientSettings settings = MongoClientSettings.builder()
                        .applyConnectionString(
                            new ConnectionString("mongodb://localhost:27017/mydb")
                        )
                        .applyToConnectionPoolSettings(builder ->
                            builder
                                .maxSize(100)
                                .minSize(10)
                                .maxWaitTime(2, TimeUnit.SECONDS)
                        )
                        .applyToSocketSettings(builder ->
                            builder
                                .connectTimeout(5, TimeUnit.SECONDS)
                                .readTimeout(10, TimeUnit.SECONDS)
                        )
                        .applyToClusterSettings(builder ->
                            builder
                                .serverSelectionTimeout(5, TimeUnit.SECONDS)
                        )
                        .readPreference(ReadPreference.secondaryPreferred())
                        .writeConcern(WriteConcern.MAJORITY)
                        .retryWrites(true)
                        .build();
                    
                    return MongoClients.create(settings);
                }
                
                Properties:
                application.properties:
                spring.data.mongodb.uri=mongodb://localhost:27017/mydb
                spring.data.mongodb.database=mydb
                spring.data.mongodb.auto-index-creation=true
                spring.data.mongodb.field-naming-strategy=snake_case
                
                Read Preference:
                - primary() - Read from primary
                - primaryPreferred() - Primary, fallback secondary
                - secondary() - Read from secondary
                - secondaryPreferred() - Secondary, fallback primary
                - nearest() - Lowest latency
                
                Write Concern:
                - ACKNOWLEDGED - Acknowledged by primary
                - W1, W2, W3 - Acknowledged by N nodes
                - MAJORITY - Acknowledged by majority
                - JOURNALED - Written to journal
                """;
    }
}

/**
 * REST Controller
 */
@RestController
@RequestMapping("/mongodb/patterns")
@Slf4j
class MongoDBPatternsController {
    
    private final MongoTemplateService templateService;
    private final MongoRepositoryService repositoryService;
    private final ReactiveMongoTemplateService reactiveTemplateService;
    private final ReactiveMongoRepositoryService reactiveRepoService;
    private final MongoAdvancedPatternsService advancedService;
    
    public MongoDBPatternsController(
            MongoTemplateService templateService,
            MongoRepositoryService repositoryService,
            ReactiveMongoTemplateService reactiveTemplateService,
            ReactiveMongoRepositoryService reactiveRepoService,
            MongoAdvancedPatternsService advancedService) {
        this.templateService = templateService;
        this.repositoryService = repositoryService;
        this.reactiveTemplateService = reactiveTemplateService;
        this.reactiveRepoService = reactiveRepoService;
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
        return reactiveTemplateService.getInfo();
    }
    
    @GetMapping("/reactive-repository")
    public String getReactiveRepositoryInfo() {
        return reactiveRepoService.getInfo();
    }
    
    @GetMapping("/aggregation")
    public String getAggregationInfo() {
        return advancedService.getAggregationInfo();
    }
    
    @GetMapping("/gridfs")
    public String getGridFSInfo() {
        return advancedService.getGridFSInfo();
    }
    
    @GetMapping("/change-stream")
    public String getChangeStreamInfo() {
        return advancedService.getChangeStreamInfo();
    }
    
    @GetMapping("/transaction")
    public String getTransactionInfo() {
        return advancedService.getTransactionInfo();
    }
    
    @GetMapping("/client-settings")
    public String getClientSettingsInfo() {
        return advancedService.getClientSettingsInfo();
    }
}

@SpringBootApplication
public class MongoDBPatterns {
    public static void main(String[] args) {
        SpringApplication.run(MongoDBPatterns.class, args);
    }
}
