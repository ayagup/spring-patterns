package com.example.cassandra;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.cassandra.core.query.Criteria;
import org.springframework.data.cassandra.core.query.Query;
import org.springframework.data.cassandra.core.query.Update;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Cassandra Template Pattern
 * 
 * Demonstrates the use of CassandraTemplate for low-level CQL operations
 * in Apache Cassandra NoSQL database.
 * 
 * Key concepts:
 * - CassandraTemplate for CQL operations
 * - Insert, update, delete operations
 * - Query with criteria
 * - Select one and select many
 * - Count and exists operations
 * - Truncate operations
 * 
 * Use cases:
 * - When you need fine-grained control over CQL queries
 * - Dynamic query construction
 * - Bulk operations
 * - Custom data access patterns
 * - Complex queries not supported by repositories
 */
@SpringBootApplication
public class CassandraTemplatePattern {

    public static void main(String[] args) {
        SpringApplication.run(CassandraTemplatePattern.class, args);
    }
}

/**
 * Product entity for Cassandra
 */
record Product(
    UUID id,
    String name,
    String category,
    Double price,
    Integer stock,
    String description,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public Product {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }
}

/**
 * Service demonstrating CassandraTemplate operations
 */
@Service
class ProductService {
    
    private final CassandraTemplate cassandraTemplate;
    
    public ProductService(CassandraTemplate cassandraTemplate) {
        this.cassandraTemplate = cassandraTemplate;
    }
    
    /**
     * Insert a new product
     */
    public Product createProduct(Product product) {
        return cassandraTemplate.insert(product);
    }
    
    /**
     * Insert multiple products
     */
    public List<Product> createProducts(List<Product> products) {
        return cassandraTemplate.insert(products);
    }
    
    /**
     * Update an existing product
     */
    public Product updateProduct(Product product) {
        Product updated = new Product(
            product.id(),
            product.name(),
            product.category(),
            product.price(),
            product.stock(),
            product.description(),
            product.createdAt(),
            LocalDateTime.now()
        );
        return cassandraTemplate.update(updated);
    }
    
    /**
     * Update product using Query and Update objects
     */
    public boolean updateProductByQuery(UUID id, Integer newStock) {
        Query query = Query.query(Criteria.where("id").is(id));
        Update update = Update.update("stock", newStock)
                             .set("updatedAt", LocalDateTime.now());
        return cassandraTemplate.update(query, update, Product.class);
    }
    
    /**
     * Find product by ID
     */
    public Product findById(UUID id) {
        return cassandraTemplate.selectOneById(id, Product.class);
    }
    
    /**
     * Find all products
     */
    public List<Product> findAll() {
        return cassandraTemplate.select(Query.empty(), Product.class);
    }
    
    /**
     * Find products by category
     */
    public List<Product> findByCategory(String category) {
        Query query = Query.query(Criteria.where("category").is(category));
        return cassandraTemplate.select(query, Product.class);
    }
    
    /**
     * Find products with price greater than
     */
    public List<Product> findByPriceGreaterThan(Double price) {
        Query query = Query.query(Criteria.where("price").gt(price));
        return cassandraTemplate.select(query, Product.class);
    }
    
    /**
     * Find products with pagination
     */
    public List<Product> findWithLimit(int limit) {
        Query query = Query.empty().limit(limit);
        return cassandraTemplate.select(query, Product.class);
    }
    
    /**
     * Check if product exists
     */
    public boolean exists(UUID id) {
        return cassandraTemplate.exists(id, Product.class);
    }
    
    /**
     * Count all products
     */
    public long count() {
        return cassandraTemplate.count(Product.class);
    }
    
    /**
     * Count products by category
     */
    public long countByCategory(String category) {
        Query query = Query.query(Criteria.where("category").is(category));
        return cassandraTemplate.count(query, Product.class);
    }
    
    /**
     * Delete product by ID
     */
    public boolean deleteById(UUID id) {
        return cassandraTemplate.deleteById(id, Product.class);
    }
    
    /**
     * Delete product entity
     */
    public boolean deleteProduct(Product product) {
        return cassandraTemplate.delete(product);
    }
    
    /**
     * Delete products by query
     */
    public boolean deleteByCategory(String category) {
        Query query = Query.query(Criteria.where("category").is(category));
        return cassandraTemplate.delete(query, Product.class);
    }
    
    /**
     * Truncate all products (use with caution)
     */
    public void truncate() {
        cassandraTemplate.truncate(Product.class);
    }
    
    /**
     * Execute custom CQL statement
     */
    public List<Product> executeCql(String cql) {
        return cassandraTemplate.getCqlOperations()
                                .query(cql, (row, rowNum) -> 
                                    new Product(
                                        row.getUuid("id"),
                                        row.getString("name"),
                                        row.getString("category"),
                                        row.getDouble("price"),
                                        row.getInt("stock"),
                                        row.getString("description"),
                                        row.get("createdAt", LocalDateTime.class),
                                        row.get("updatedAt", LocalDateTime.class)
                                    )
                                );
    }
}

/**
 * REST controller for product operations
 */
@RestController
@RequestMapping("/api/products")
class ProductController {
    
    private final ProductService productService;
    
    public ProductController(ProductService productService) {
        this.productService = productService;
    }
    
    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        return ResponseEntity.ok(productService.createProduct(product));
    }
    
    @PostMapping("/batch")
    public ResponseEntity<List<Product>> createProducts(@RequestBody List<Product> products) {
        return ResponseEntity.ok(productService.createProducts(products));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable UUID id, @RequestBody Product product) {
        Product updated = new Product(id, product.name(), product.category(), 
                                     product.price(), product.stock(), product.description(),
                                     product.createdAt(), product.updatedAt());
        return ResponseEntity.ok(productService.updateProduct(updated));
    }
    
    @PatchMapping("/{id}/stock")
    public ResponseEntity<Void> updateStock(@PathVariable UUID id, @RequestParam Integer stock) {
        productService.updateProductByQuery(id, stock);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable UUID id) {
        Product product = productService.findById(id);
        return product != null ? ResponseEntity.ok(product) : ResponseEntity.notFound().build();
    }
    
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts(@RequestParam(required = false) Integer limit) {
        List<Product> products = limit != null ? 
            productService.findWithLimit(limit) : 
            productService.findAll();
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/category/{category}")
    public ResponseEntity<List<Product>> getProductsByCategory(@PathVariable String category) {
        return ResponseEntity.ok(productService.findByCategory(category));
    }
    
    @GetMapping("/price/greater-than/{price}")
    public ResponseEntity<List<Product>> getProductsByPriceGreaterThan(@PathVariable Double price) {
        return ResponseEntity.ok(productService.findByPriceGreaterThan(price));
    }
    
    @GetMapping("/{id}/exists")
    public ResponseEntity<Boolean> productExists(@PathVariable UUID id) {
        return ResponseEntity.ok(productService.exists(id));
    }
    
    @GetMapping("/count")
    public ResponseEntity<Long> countProducts() {
        return ResponseEntity.ok(productService.count());
    }
    
    @GetMapping("/count/category/{category}")
    public ResponseEntity<Long> countByCategory(@PathVariable String category) {
        return ResponseEntity.ok(productService.countByCategory(category));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable UUID id) {
        productService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
    
    @DeleteMapping("/category/{category}")
    public ResponseEntity<Void> deleteByCategory(@PathVariable String category) {
        productService.deleteByCategory(category);
        return ResponseEntity.noContent().build();
    }
    
    @DeleteMapping("/truncate")
    public ResponseEntity<Void> truncate() {
        productService.truncate();
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/cql")
    public ResponseEntity<List<Product>> executeCql(@RequestBody String cql) {
        return ResponseEntity.ok(productService.executeCql(cql));
    }
    
    @GetMapping("/info")
    public ResponseEntity<String> getInfo() {
        return ResponseEntity.ok("""
            Cassandra Template Pattern
            
            This pattern demonstrates the use of CassandraTemplate for low-level CQL operations
            in Apache Cassandra NoSQL database.
            
            Features:
            - Insert single and multiple products
            - Update operations with Query and Update objects
            - Select by ID, category, price range
            - Pagination with limit
            - Count operations
            - Delete by ID, entity, or query
            - Truncate table
            - Execute custom CQL statements
            
            Endpoints:
            - POST /api/products - Create product
            - POST /api/products/batch - Create multiple products
            - PUT /api/products/{id} - Update product
            - PATCH /api/products/{id}/stock - Update stock
            - GET /api/products/{id} - Get product
            - GET /api/products - Get all products (optional limit)
            - GET /api/products/category/{category} - Get by category
            - GET /api/products/price/greater-than/{price} - Filter by price
            - GET /api/products/{id}/exists - Check exists
            - GET /api/products/count - Count all
            - GET /api/products/count/category/{category} - Count by category
            - DELETE /api/products/{id} - Delete product
            - DELETE /api/products/category/{category} - Delete by category
            - DELETE /api/products/truncate - Truncate table
            - POST /api/products/cql - Execute custom CQL
            """);
    }
}
