package com.example.elasticsearch;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.GeoPointField;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.*;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.elasticsearch.repository.ReactiveElasticsearchRepository;
import org.springframework.data.geo.Point;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Elasticsearch Patterns - Complete Collection
 * 
 * All 8 Elasticsearch integration patterns:
 * 1. Elasticsearch Template Pattern
 * 2. Elasticsearch Repository Pattern
 * 3. Reactive Elasticsearch Template Pattern
 * 4. Search Query Pattern
 * 5. Aggregation Query Pattern
 * 6. Bulk Operation Pattern
 * 7. Index Template Pattern
 * 8. Geo Query Pattern
 * 
 * @author Spring Patterns
 */

@Document(indexName = "users")
@Data
class User {
    @Id
    private String id;
    
    @Field(type = FieldType.Text)
    private String username;
    
    @Field(type = FieldType.Keyword)
    private String email;
    
    @Field(type = FieldType.Integer)
    private Integer age;
    
    @Field(type = FieldType.Text)
    private String department;
    
    @Field(type = FieldType.Date)
    private LocalDateTime createdAt;
    
    @GeoPointField
    private Point location;
}

@Document(indexName = "products")
@Data
class Product {
    @Id
    private String id;
    
    @Field(type = FieldType.Text)
    private String name;
    
    @Field(type = FieldType.Double)
    private Double price;
    
    @Field(type = FieldType.Keyword)
    private String category;
}

/**
 * 1. Elasticsearch Template Pattern
 */
@Service
@Slf4j
class ElasticsearchTemplateService {
    
    private final ElasticsearchOperations elasticsearchOperations;
    
    public ElasticsearchTemplateService(ElasticsearchOperations elasticsearchOperations) {
        this.elasticsearchOperations = elasticsearchOperations;
    }
    
    public User save(User user) {
        return elasticsearchOperations.save(user);
    }
    
    public User findById(String id) {
        return elasticsearchOperations.get(id, User.class);
    }
    
    public void delete(String id) {
        elasticsearchOperations.delete(id, User.class);
    }
    
    public SearchHits<User> search(String searchTerm) {
        Criteria criteria = new Criteria("username").contains(searchTerm);
        Query query = new CriteriaQuery(criteria);
        return elasticsearchOperations.search(query, User.class);
    }
    
    public String getInfo() {
        return """
                Elasticsearch Template Pattern
                ==============================
                
                Operations:
                - save() - Index document
                - get() - Retrieve by ID
                - delete() - Delete document
                - search() - Search documents
                - count() - Count matches
                - exists() - Check existence
                
                Index Operations:
                IndexOperations indexOps = 
                    elasticsearchOperations.indexOps(User.class);
                
                // Create index
                indexOps.create();
                
                // Create mapping
                indexOps.putMapping(User.class);
                
                // Delete index
                indexOps.delete();
                
                // Exists check
                boolean exists = indexOps.exists();
                
                Search with Query:
                Criteria criteria = new Criteria("field")
                    .is("value")
                    .and("age").greaterThanEqual(18);
                
                Query query = new CriteriaQuery(criteria);
                SearchHits<User> hits = elasticsearchOperations.search(
                    query, User.class
                );
                
                Pagination:
                PageRequest pageRequest = PageRequest.of(0, 10);
                query.setPageable(pageRequest);
                """;
    }
}

/**
 * 2. Elasticsearch Repository Pattern
 */
interface UserRepository extends ElasticsearchRepository<User, String> {
    List<User> findByUsername(String username);
    List<User> findByAgeGreaterThan(int age);
    List<User> findByDepartmentAndAgeGreaterThan(String dept, int age);
}

@Service
@Slf4j
class ElasticsearchRepositoryService {
    
    private final UserRepository userRepository;
    
    public ElasticsearchRepositoryService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    public String getInfo() {
        return """
                Elasticsearch Repository Pattern
                ===============================
                
                Interface:
                public interface UserRepository 
                    extends ElasticsearchRepository<User, String> {
                    
                    List<User> findByUsername(String username);
                    List<User> findByAgeGreaterThan(int age);
                    List<User> findByUsernameContaining(String pattern);
                    
                    @Query("{\"match\": {\"username\": \"?0\"}}")
                    List<User> findByUsernameQuery(String username);
                }
                
                Query Methods:
                - findBy...
                - findAll...
                - count...
                - delete...
                - exists...
                
                Keywords:
                - And, Or
                - Is, Equals
                - Between, LessThan, GreaterThan
                - Like, NotLike, StartingWith, EndingWith
                - Containing, NotContaining
                - In, NotIn
                - True, False
                - OrderBy
                
                Usage:
                User user = new User();
                userRepository.save(user);
                
                Optional<User> found = userRepository.findById(id);
                List<User> users = userRepository.findByDepartment("IT");
                long count = userRepository.count();
                """;
    }
}

/**
 * 3. Reactive Elasticsearch Template Pattern
 */
@Service
@Slf4j
class ReactiveElasticsearchTemplateService {
    
    private final ReactiveElasticsearchOperations reactiveElasticsearchOperations;
    
    public ReactiveElasticsearchTemplateService(
            ReactiveElasticsearchOperations reactiveElasticsearchOperations) {
        this.reactiveElasticsearchOperations = reactiveElasticsearchOperations;
    }
    
    public Mono<User> save(User user) {
        return reactiveElasticsearchOperations.save(user);
    }
    
    public Flux<User> findAll() {
        Criteria criteria = new Criteria();
        Query query = new CriteriaQuery(criteria);
        return reactiveElasticsearchOperations.search(query, User.class)
            .map(searchHit -> searchHit.getContent());
    }
    
    public String getInfo() {
        return """
                Reactive Elasticsearch Template Pattern
                ======================================
                
                Operations:
                Mono<User> user = operations.save(user);
                Mono<User> found = operations.get(id, User.class);
                Mono<Boolean> deleted = operations.delete(id, User.class);
                
                Search:
                Query query = new CriteriaQuery(
                    new Criteria("field").is("value")
                );
                
                Flux<SearchHit<User>> hits = operations.search(
                    query, User.class
                );
                
                Flux<User> users = hits.map(SearchHit::getContent);
                
                Backpressure:
                operations.search(query, User.class)
                    .map(SearchHit::getContent)
                    .limitRate(100)
                    .buffer(50)
                    .subscribe();
                """;
    }
}

/**
 * 4-8. Advanced Patterns
 */
@Service
@Slf4j
class ElasticsearchAdvancedPatternsService {
    
    private final ElasticsearchOperations elasticsearchOperations;
    
    public ElasticsearchAdvancedPatternsService(
            ElasticsearchOperations elasticsearchOperations) {
        this.elasticsearchOperations = elasticsearchOperations;
    }
    
    /**
     * 4. Search Query Pattern
     */
    public String getSearchQueryInfo() {
        return """
                Search Query Pattern
                ===================
                
                Match Query:
                Criteria criteria = new Criteria("username")
                    .matches("john");
                Query query = new CriteriaQuery(criteria);
                
                Multi-Match Query:
                Criteria criteria = new Criteria()
                    .or("username").contains("john")
                    .or("email").contains("john");
                
                Boolean Query:
                Criteria criteria = new Criteria("department").is("IT")
                    .and("age").greaterThanEqual(18)
                    .and("active").is(true);
                
                Range Query:
                Criteria criteria = new Criteria("age")
                    .between(18, 65);
                
                Wildcard Query:
                Criteria criteria = new Criteria("username")
                    .expression("jo*n");
                
                Native Query:
                Query query = NativeQuery.builder()
                    .withQuery(Query.of(q -> q
                        .match(m -> m
                            .field("username")
                            .query("john")
                        )
                    ))
                    .withPageable(PageRequest.of(0, 10))
                    .build();
                
                SearchHits<User> hits = operations.search(query, User.class);
                
                Fuzzy Query:
                Query query = NativeQuery.builder()
                    .withQuery(Query.of(q -> q
                        .fuzzy(f -> f
                            .field("username")
                            .value("jhn")
                            .fuzziness("AUTO")
                        )
                    ))
                    .build();
                
                Phrase Query:
                Query query = NativeQuery.builder()
                    .withQuery(Query.of(q -> q
                        .matchPhrase(mp -> mp
                            .field("description")
                            .query("quick brown fox")
                        )
                    ))
                    .build();
                """;
    }
    
    /**
     * 5. Aggregation Query Pattern
     */
    public String getAggregationInfo() {
        return """
                Aggregation Query Pattern
                ========================
                
                Terms Aggregation:
                Query query = NativeQuery.builder()
                    .withAggregation("departments", Aggregation.of(a -> a
                        .terms(t -> t
                            .field("department.keyword")
                            .size(10)
                        )
                    ))
                    .build();
                
                SearchHits<User> hits = operations.search(query, User.class);
                Aggregations aggregations = hits.getAggregations();
                
                Stats Aggregation:
                Query query = NativeQuery.builder()
                    .withAggregation("age_stats", Aggregation.of(a -> a
                        .stats(s -> s.field("age"))
                    ))
                    .build();
                
                Avg Aggregation:
                Query query = NativeQuery.builder()
                    .withAggregation("avg_age", Aggregation.of(a -> a
                        .avg(avg -> avg.field("age"))
                    ))
                    .build();
                
                Date Histogram:
                Query query = NativeQuery.builder()
                    .withAggregation("by_month", Aggregation.of(a -> a
                        .dateHistogram(dh -> dh
                            .field("createdAt")
                            .calendarInterval(CalendarInterval.Month)
                        )
                    ))
                    .build();
                
                Nested Aggregations:
                Query query = NativeQuery.builder()
                    .withAggregation("departments", Aggregation.of(a -> a
                        .terms(t -> t.field("department.keyword"))
                        .aggregations("avg_age", Aggregation.of(sub -> sub
                            .avg(avg -> avg.field("age"))
                        ))
                    ))
                    .build();
                """;
    }
    
    /**
     * 6. Bulk Operation Pattern
     */
    public String getBulkOperationInfo() {
        return """
                Bulk Operation Pattern
                =====================
                
                Bulk Index:
                List<IndexQuery> queries = users.stream()
                    .map(user -> new IndexQueryBuilder()
                        .withId(user.getId())
                        .withObject(user)
                        .build())
                    .collect(Collectors.toList());
                
                List<IndexedObjectInformation> result = 
                    operations.bulkIndex(queries, IndexCoordinates.of("users"));
                
                Bulk Update:
                List<UpdateQuery> queries = updates.stream()
                    .map(update -> UpdateQuery.builder(update.getId())
                        .withDocument(Document.from(update.getFields()))
                        .build())
                    .collect(Collectors.toList());
                
                operations.bulkUpdate(queries, IndexCoordinates.of("users"));
                
                Bulk Delete:
                List<String> ids = Arrays.asList("1", "2", "3");
                Query query = IdsQueryBuilder.builder()
                    .withIds(ids)
                    .build();
                
                operations.delete(query, User.class);
                
                Update By Query:
                UpdateQuery updateQuery = UpdateQuery.builder(query)
                    .withScriptType(ScriptType.INLINE)
                    .withScript("ctx._source.age += 1")
                    .build();
                
                operations.updateByQuery(updateQuery, IndexCoordinates.of("users"));
                
                Performance:
                - Batch size: 1000-5000 documents
                - Use bulk API for large operations
                - Monitor rejected operations
                - Handle partial failures
                """;
    }
    
    /**
     * 7. Index Template Pattern
     */
    public String getIndexTemplateInfo() {
        return """
                Index Template Pattern
                =====================
                
                Purpose:
                - Define index settings
                - Configure mappings
                - Set aliases
                - Apply to multiple indices
                
                Create Index:
                IndexOperations indexOps = operations.indexOps(User.class);
                
                Settings settings = Settings.builder()
                    .put("index.number_of_shards", 3)
                    .put("index.number_of_replicas", 2)
                    .put("index.refresh_interval", "5s")
                    .build();
                
                indexOps.create(settings);
                
                Mapping:
                @Document(indexName = "users")
                public class User {
                    @Id
                    private String id;
                    
                    @Field(type = FieldType.Text, analyzer = "standard")
                    private String username;
                    
                    @Field(type = FieldType.Keyword)
                    private String email;
                    
                    @Field(type = FieldType.Integer)
                    private Integer age;
                    
                    @Field(type = FieldType.Date, 
                           format = DateFormat.date_time)
                    private LocalDateTime createdAt;
                    
                    @MultiField(
                        mainField = @Field(type = FieldType.Text),
                        otherFields = {
                            @InnerField(suffix = "keyword", 
                                        type = FieldType.Keyword)
                        }
                    )
                    private String description;
                }
                
                Alias:
                AliasActions actions = new AliasActions(
                    new AliasAction.Add(
                        AliasActionParameters.builder()
                            .withIndices("users-2024")
                            .withAliases("users-current")
                            .build()
                    )
                );
                
                indexOps.alias(actions);
                
                Reindex:
                ReindexRequest request = new ReindexRequest()
                    .setSourceIndices("old-index")
                    .setDestIndex("new-index");
                
                client.reindex(request);
                """;
    }
    
    /**
     * 8. Geo Query Pattern
     */
    public String getGeoQueryInfo() {
        return """
                Geo Query Pattern
                ================
                
                Entity with Geo Point:
                @Document(indexName = "locations")
                public class Location {
                    @Id
                    private String id;
                    
                    @Field(type = FieldType.Text)
                    private String name;
                    
                    @GeoPointField
                    private GeoPoint location;
                }
                
                Geo Distance Query:
                Query query = NativeQuery.builder()
                    .withQuery(Query.of(q -> q
                        .geoDistance(gd -> gd
                            .field("location")
                            .distance("10km")
                            .location(loc -> loc
                                .latlon(latlon -> latlon
                                    .lat(40.7128)
                                    .lon(-74.0060)
                                )
                            )
                        )
                    ))
                    .build();
                
                SearchHits<Location> hits = operations.search(
                    query, Location.class
                );
                
                Geo Bounding Box:
                Query query = NativeQuery.builder()
                    .withQuery(Query.of(q -> q
                        .geoBoundingBox(gbb -> gbb
                            .field("location")
                            .boundingBox(bb -> bb
                                .tlbr(tlbr -> tlbr
                                    .topLeft(tl -> tl
                                        .latlon(ll -> ll.lat(41.0).lon(-75.0))
                                    )
                                    .bottomRight(br -> br
                                        .latlon(ll -> ll.lat(40.0).lon(-74.0))
                                    )
                                )
                            )
                        )
                    ))
                    .build();
                
                Geo Polygon:
                Query query = NativeQuery.builder()
                    .withQuery(Query.of(q -> q
                        .geoPolygon(gp -> gp
                            .field("location")
                            .polygon(p -> p
                                .points(Arrays.asList(
                                    GeoLocation.of(40.0, -74.0),
                                    GeoLocation.of(41.0, -74.0),
                                    GeoLocation.of(41.0, -75.0),
                                    GeoLocation.of(40.0, -75.0),
                                    GeoLocation.of(40.0, -74.0)
                                ))
                            )
                        )
                    ))
                    .build();
                
                Sort by Distance:
                Query query = NativeQuery.builder()
                    .withQuery(matchAllQuery())
                    .withSort(Sort.by(
                        new GeoDistanceOrder("location", 
                            new GeoPoint(40.7128, -74.0060))
                            .withUnit("km")
                    ))
                    .build();
                """;
    }
}

/**
 * REST Controller
 */
@RestController
@RequestMapping("/elasticsearch/patterns")
@Slf4j
class ElasticsearchPatternsController {
    
    private final ElasticsearchTemplateService templateService;
    private final ElasticsearchRepositoryService repositoryService;
    private final ReactiveElasticsearchTemplateService reactiveService;
    private final ElasticsearchAdvancedPatternsService advancedService;
    
    public ElasticsearchPatternsController(
            ElasticsearchTemplateService templateService,
            ElasticsearchRepositoryService repositoryService,
            ReactiveElasticsearchTemplateService reactiveService,
            ElasticsearchAdvancedPatternsService advancedService) {
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
    
    @GetMapping("/search-query")
    public String getSearchQueryInfo() {
        return advancedService.getSearchQueryInfo();
    }
    
    @GetMapping("/aggregation")
    public String getAggregationInfo() {
        return advancedService.getAggregationInfo();
    }
    
    @GetMapping("/bulk-operation")
    public String getBulkOperationInfo() {
        return advancedService.getBulkOperationInfo();
    }
    
    @GetMapping("/index-template")
    public String getIndexTemplateInfo() {
        return advancedService.getIndexTemplateInfo();
    }
    
    @GetMapping("/geo-query")
    public String getGeoQueryInfo() {
        return advancedService.getGeoQueryInfo();
    }
}

@SpringBootApplication
public class ElasticsearchPatterns {
    public static void main(String[] args) {
        SpringApplication.run(ElasticsearchPatterns.class, args);
    }
}
