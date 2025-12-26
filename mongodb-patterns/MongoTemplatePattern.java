package com.example.mongodb;

import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

/**
 * Mongo Template Pattern
 * 
 * Demonstrates Spring Data MongoDB template for flexible document operations.
 * 
 * MongoTemplate Features:
 * - CRUD operations on documents
 * - Query operations with criteria
 * - Update operations (single and bulk)
 * - Aggregation pipeline support
 * - Index management
 * - Collection operations
 * - Geospatial queries
 * 
 * Query Operations:
 * - Find by criteria
 * - Find one document
 * - Find all documents
 * - Count documents
 * - Exists check
 * - Distinct values
 * 
 * Update Operations:
 * - Update single document
 * - Update multiple documents
 * - Upsert operation
 * - FindAndModify
 * - Replace document
 * 
 * Use Cases:
 * - Complex queries beyond repository methods
 * - Dynamic query construction
 * - Bulk operations
 * - Aggregation pipelines
 * - Collection management
 * - Index creation and management
 * 
 * @author Spring Patterns
 * @version 1.0
 */
@Configuration
public class MongoTemplatePattern {

    @Bean
    public MongoProductService mongoProductService(MongoTemplate mongoTemplate) {
        return new MongoProductService(mongoTemplate);
    }
}

record Product(
    String id,
    String name,
    String category,
    double price,
    int stock,
    String description,
    List<String> tags,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    boolean active
) {}

@RestController
@RequestMapping("/api/mongo/products")
class MongoProductService {

    private final MongoTemplate mongoTemplate;
    private static final String COLLECTION_NAME = "products";

    public MongoProductService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public Product save(Product product) {
        return mongoTemplate.save(product, COLLECTION_NAME);
    }

    public Product findById(String id) {
        return mongoTemplate.findById(id, Product.class, COLLECTION_NAME);
    }

    public List<Product> findAll() {
        return mongoTemplate.findAll(Product.class, COLLECTION_NAME);
    }

    public List<Product> findByCategory(String category) {
        Query query = new Query(Criteria.where("category").is(category));
        return mongoTemplate.find(query, Product.class, COLLECTION_NAME);
    }

    public List<Product> findByPriceRange(double minPrice, double maxPrice) {
        Query query = new Query(
            Criteria.where("price").gte(minPrice).lte(maxPrice)
        );
        return mongoTemplate.find(query, Product.class, COLLECTION_NAME);
    }

    public List<Product> findByTag(String tag) {
        Query query = new Query(Criteria.where("tags").in(tag));
        return mongoTemplate.find(query, Product.class, COLLECTION_NAME);
    }

    public List<Product> searchByName(String namePattern) {
        Query query = new Query(
            Criteria.where("name").regex(namePattern, "i")
        );
        return mongoTemplate.find(query, Product.class, COLLECTION_NAME);
    }

    public Product updatePrice(String id, double newPrice) {
        Query query = new Query(Criteria.where("_id").is(id));
        Update update = new Update()
            .set("price", newPrice)
            .set("updatedAt", LocalDateTime.now());
        
        return mongoTemplate.findAndModify(
            query, update, Product.class, COLLECTION_NAME
        );
    }

    public UpdateResult updateStock(String id, int quantity) {
        Query query = new Query(Criteria.where("_id").is(id));
        Update update = new Update()
            .inc("stock", quantity)
            .set("updatedAt", LocalDateTime.now());
        
        return mongoTemplate.updateFirst(query, update, Product.class, COLLECTION_NAME);
    }

    public UpdateResult updateMultiplePrices(String category, double percentage) {
        Query query = new Query(Criteria.where("category").is(category));
        Update update = new Update()
            .mul("price", 1 + (percentage / 100))
            .set("updatedAt", LocalDateTime.now());
        
        return mongoTemplate.updateMulti(query, update, Product.class, COLLECTION_NAME);
    }

    public Product upsert(Product product) {
        Query query = new Query(Criteria.where("name").is(product.name()));
        Update update = new Update()
            .set("category", product.category())
            .set("price", product.price())
            .set("stock", product.stock())
            .set("description", product.description())
            .set("tags", product.tags())
            .set("active", product.active())
            .set("updatedAt", LocalDateTime.now())
            .setOnInsert("createdAt", LocalDateTime.now());
        
        mongoTemplate.upsert(query, update, Product.class, COLLECTION_NAME);
        return mongoTemplate.findOne(query, Product.class, COLLECTION_NAME);
    }

    public DeleteResult delete(String id) {
        Query query = new Query(Criteria.where("_id").is(id));
        return mongoTemplate.remove(query, Product.class, COLLECTION_NAME);
    }

    public DeleteResult deleteByCategory(String category) {
        Query query = new Query(Criteria.where("category").is(category));
        return mongoTemplate.remove(query, Product.class, COLLECTION_NAME);
    }

    public long count() {
        return mongoTemplate.count(new Query(), Product.class, COLLECTION_NAME);
    }

    public long countByCategory(String category) {
        Query query = new Query(Criteria.where("category").is(category));
        return mongoTemplate.count(query, Product.class, COLLECTION_NAME);
    }

    public boolean exists(String id) {
        Query query = new Query(Criteria.where("_id").is(id));
        return mongoTemplate.exists(query, Product.class, COLLECTION_NAME);
    }

    public List<String> distinctCategories() {
        return mongoTemplate.findDistinct(
            new Query(), "category", Product.class, String.class
        );
    }

    public List<CategoryStats> getCategoryStats() {
        Aggregation aggregation = newAggregation(
            match(Criteria.where("active").is(true)),
            group("category")
                .count().as("count")
                .sum("stock").as("totalStock")
                .avg("price").as("averagePrice")
                .min("price").as("minPrice")
                .max("price").as("maxPrice"),
            project("count", "totalStock", "averagePrice", "minPrice", "maxPrice")
                .and("_id").as("category"),
            sort(org.springframework.data.domain.Sort.Direction.DESC, "count")
        );
        
        AggregationResults<CategoryStats> results = 
            mongoTemplate.aggregate(aggregation, COLLECTION_NAME, CategoryStats.class);
        
        return results.getMappedResults();
    }

    public List<Product> findLowStock(int threshold) {
        Query query = new Query(
            Criteria.where("stock").lt(threshold).and("active").is(true)
        );
        return mongoTemplate.find(query, Product.class, COLLECTION_NAME);
    }

    record CategoryStats(
        String category,
        long count,
        long totalStock,
        double averagePrice,
        double minPrice,
        double maxPrice
    ) {}
}

@RestController
@RequestMapping("/api/mongo/products")
class MongoProductController {

    private final MongoProductService productService;

    public MongoProductController(MongoProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        Product saved = productService.save(product);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable String id) {
        Product product = productService.findById(id);
        return product != null ? ResponseEntity.ok(product) : ResponseEntity.notFound().build();
    }

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.findAll());
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<Product>> getByCategory(@PathVariable String category) {
        return ResponseEntity.ok(productService.findByCategory(category));
    }

    @GetMapping("/price-range")
    public ResponseEntity<List<Product>> getByPriceRange(
            @RequestParam double min,
            @RequestParam double max) {
        return ResponseEntity.ok(productService.findByPriceRange(min, max));
    }

    @GetMapping("/tag/{tag}")
    public ResponseEntity<List<Product>> getByTag(@PathVariable String tag) {
        return ResponseEntity.ok(productService.findByTag(tag));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchByName(@RequestParam String name) {
        return ResponseEntity.ok(productService.searchByName(name));
    }

    @PutMapping("/{id}/price")
    public ResponseEntity<Product> updatePrice(
            @PathVariable String id,
            @RequestParam double price) {
        Product updated = productService.updatePrice(id, price);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}/stock")
    public ResponseEntity<UpdateResult> updateStock(
            @PathVariable String id,
            @RequestParam int quantity) {
        UpdateResult result = productService.updateStock(id, quantity);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/category/{category}/bulk-price-update")
    public ResponseEntity<UpdateResult> bulkPriceUpdate(
            @PathVariable String category,
            @RequestParam double percentage) {
        UpdateResult result = productService.updateMultiplePrices(category, percentage);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/upsert")
    public ResponseEntity<Product> upsertProduct(@RequestBody Product product) {
        Product result = productService.upsert(product);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<DeleteResult> deleteProduct(@PathVariable String id) {
        DeleteResult result = productService.delete(id);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/category/{category}")
    public ResponseEntity<DeleteResult> deleteByCategory(@PathVariable String category) {
        DeleteResult result = productService.deleteByCategory(category);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/count")
    public ResponseEntity<Long> countProducts() {
        return ResponseEntity.ok(productService.count());
    }

    @GetMapping("/count/category/{category}")
    public ResponseEntity<Long> countByCategory(@PathVariable String category) {
        return ResponseEntity.ok(productService.countByCategory(category));
    }

    @GetMapping("/{id}/exists")
    public ResponseEntity<Boolean> productExists(@PathVariable String id) {
        return ResponseEntity.ok(productService.exists(id));
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategories() {
        return ResponseEntity.ok(productService.distinctCategories());
    }

    @GetMapping("/stats/categories")
    public ResponseEntity<List<MongoProductService.CategoryStats>> getCategoryStats() {
        return ResponseEntity.ok(productService.getCategoryStats());
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<Product>> getLowStockProducts(@RequestParam int threshold) {
        return ResponseEntity.ok(productService.findLowStock(threshold));
    }

    @GetMapping("/info")
    public ResponseEntity<PatternInfo> getPatternInfo() {
        return ResponseEntity.ok(new PatternInfo(
            "Mongo Template Pattern",
            "Flexible document operations using MongoTemplate",
            "1.0",
            List.of("CRUD operations", "Query criteria", "Bulk updates", "Aggregation pipelines", "Index management"),
            List.of("Complex queries", "Dynamic query building", "Bulk operations", "Aggregation", "Collection management")
        ));
    }

    record PatternInfo(String name, String description, String version, 
                      List<String> features, List<String> useCases) {}
}
