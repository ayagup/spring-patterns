package com.example.elasticsearch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Elasticsearch Repository Pattern
 * 
 * Demonstrates the use of Spring Data Elasticsearch Repository
 * for automatic query generation and CRUD operations.
 * 
 * Key concepts:
 * - @Document annotation for Elasticsearch entities
 * - ElasticsearchRepository interface
 * - Derived query methods
 * - Custom query methods with @Query
 * - Paging and sorting
 * - Aggregations
 * 
 * Use cases:
 * - Type-safe repository operations
 * - Automatic query generation
 * - Complex search queries
 * - Document management
 */
@SpringBootApplication
public class ElasticsearchRepositoryPattern {

    public static void main(String[] args) {
        SpringApplication.run(ElasticsearchRepositoryPattern.class, args);
    }
}

/**
 * Product document for Elasticsearch
 */
@Document(indexName = "products")
record Product(
    @Id String id,
    @Field(type = FieldType.Text) String name,
    @Field(type = FieldType.Keyword) String category,
    @Field(type = FieldType.Text) String description,
    @Field(type = FieldType.Double) Double price,
    @Field(type = FieldType.Integer) Integer stock,
    @Field(type = FieldType.Keyword) String brand,
    @Field(type = FieldType.Keyword) List<String> tags,
    @Field(type = FieldType.Boolean) Boolean available,
    @Field(type = FieldType.Date) LocalDateTime createdDate
) {}

/**
 * Repository interface for Product
 */
interface ProductRepository extends ElasticsearchRepository<Product, String> {
    
    // Derived query methods - Spring Data generates implementation
    List<Product> findByName(String name);
    
    List<Product> findByCategory(String category);
    
    List<Product> findByBrand(String brand);
    
    List<Product> findByPriceGreaterThan(Double price);
    
    List<Product> findByPriceLessThan(Double price);
    
    List<Product> findByPriceBetween(Double minPrice, Double maxPrice);
    
    List<Product> findByStockGreaterThan(Integer stock);
    
    List<Product> findByAvailable(Boolean available);
    
    List<Product> findByTagsContaining(String tag);
    
    List<Product> findByCategoryAndAvailable(String category, Boolean available);
    
    List<Product> findByNameContaining(String name);
    
    List<Product> findByDescriptionContaining(String description);
    
    // Count methods
    long countByCategory(String category);
    
    long countByAvailable(Boolean available);
    
    // Exists methods
    boolean existsByName(String name);
    
    // Delete methods
    long deleteByCategory(String category);
}

/**
 * Review document for Elasticsearch
 */
@Document(indexName = "reviews")
record Review(
    @Id String id,
    @Field(type = FieldType.Keyword) String productId,
    @Field(type = FieldType.Keyword) String userId,
    @Field(type = FieldType.Text) String title,
    @Field(type = FieldType.Text) String content,
    @Field(type = FieldType.Integer) Integer rating,
    @Field(type = FieldType.Boolean) Boolean verified,
    @Field(type = FieldType.Date) LocalDateTime reviewDate
) {}

/**
 * Repository interface for Review
 */
interface ReviewRepository extends ElasticsearchRepository<Review, String> {
    
    List<Review> findByProductId(String productId);
    
    List<Review> findByUserId(String userId);
    
    List<Review> findByRating(Integer rating);
    
    List<Review> findByRatingGreaterThanEqual(Integer rating);
    
    List<Review> findByVerified(Boolean verified);
    
    List<Review> findByProductIdAndVerified(String productId, Boolean verified);
    
    List<Review> findByTitleContaining(String title);
    
    List<Review> findByContentContaining(String content);
    
    long countByProductId(String productId);
    
    long countByRating(Integer rating);
}

/**
 * Service for product operations
 */
@Service
class ProductService {
    
    private final ProductRepository productRepository;
    
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }
    
    public Product createProduct(Product product) {
        return productRepository.save(product);
    }
    
    public List<Product> createProducts(List<Product> products) {
        return (List<Product>) productRepository.saveAll(products);
    }
    
    public Optional<Product> getProduct(String id) {
        return productRepository.findById(id);
    }
    
    public List<Product> getAllProducts() {
        return (List<Product>) productRepository.findAll();
    }
    
    public List<Product> findByName(String name) {
        return productRepository.findByName(name);
    }
    
    public List<Product> findByCategory(String category) {
        return productRepository.findByCategory(category);
    }
    
    public List<Product> findByBrand(String brand) {
        return productRepository.findByBrand(brand);
    }
    
    public List<Product> findByPriceRange(Double minPrice, Double maxPrice) {
        return productRepository.findByPriceBetween(minPrice, maxPrice);
    }
    
    public List<Product> findByTag(String tag) {
        return productRepository.findByTagsContaining(tag);
    }
    
    public List<Product> findAvailableByCategory(String category) {
        return productRepository.findByCategoryAndAvailable(category, true);
    }
    
    public long countByCategory(String category) {
        return productRepository.countByCategory(category);
    }
    
    public boolean existsByName(String name) {
        return productRepository.existsByName(name);
    }
    
    public void deleteProduct(String id) {
        productRepository.deleteById(id);
    }
    
    public long deleteByCategory(String category) {
        return productRepository.deleteByCategory(category);
    }
}

/**
 * Service for review operations
 */
@Service
class ReviewService {
    
    private final ReviewRepository reviewRepository;
    
    public ReviewService(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }
    
    public Review createReview(Review review) {
        return reviewRepository.save(review);
    }
    
    public List<Review> getProductReviews(String productId) {
        return reviewRepository.findByProductId(productId);
    }
    
    public List<Review> getVerifiedReviews(String productId) {
        return reviewRepository.findByProductIdAndVerified(productId, true);
    }
    
    public List<Review> getHighRatedReviews(Integer minRating) {
        return reviewRepository.findByRatingGreaterThanEqual(minRating);
    }
    
    public long countProductReviews(String productId) {
        return reviewRepository.countByProductId(productId);
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
    
    @PostMapping("/bulk")
    public ResponseEntity<List<Product>> createProducts(@RequestBody List<Product> products) {
        return ResponseEntity.ok(productService.createProducts(products));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable String id) {
        return productService.getProduct(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }
    
    @GetMapping("/search/name/{name}")
    public ResponseEntity<List<Product>> findByName(@PathVariable String name) {
        return ResponseEntity.ok(productService.findByName(name));
    }
    
    @GetMapping("/search/category/{category}")
    public ResponseEntity<List<Product>> findByCategory(@PathVariable String category) {
        return ResponseEntity.ok(productService.findByCategory(category));
    }
    
    @GetMapping("/search/brand/{brand}")
    public ResponseEntity<List<Product>> findByBrand(@PathVariable String brand) {
        return ResponseEntity.ok(productService.findByBrand(brand));
    }
    
    @GetMapping("/search/price")
    public ResponseEntity<List<Product>> findByPriceRange(
            @RequestParam Double min,
            @RequestParam Double max) {
        return ResponseEntity.ok(productService.findByPriceRange(min, max));
    }
    
    @GetMapping("/search/tag/{tag}")
    public ResponseEntity<List<Product>> findByTag(@PathVariable String tag) {
        return ResponseEntity.ok(productService.findByTag(tag));
    }
    
    @GetMapping("/search/available/{category}")
    public ResponseEntity<List<Product>> findAvailableByCategory(@PathVariable String category) {
        return ResponseEntity.ok(productService.findAvailableByCategory(category));
    }
    
    @GetMapping("/count/{category}")
    public ResponseEntity<Long> countByCategory(@PathVariable String category) {
        return ResponseEntity.ok(productService.countByCategory(category));
    }
    
    @GetMapping("/exists/{name}")
    public ResponseEntity<Boolean> existsByName(@PathVariable String name) {
        return ResponseEntity.ok(productService.existsByName(name));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable String id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
    
    @DeleteMapping("/category/{category}")
    public ResponseEntity<Long> deleteByCategory(@PathVariable String category) {
        return ResponseEntity.ok(productService.deleteByCategory(category));
    }
}

/**
 * REST controller for review operations
 */
@RestController
@RequestMapping("/api/reviews")
class ReviewController {
    
    private final ReviewService reviewService;
    
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }
    
    @PostMapping
    public ResponseEntity<Review> createReview(@RequestBody Review review) {
        return ResponseEntity.ok(reviewService.createReview(review));
    }
    
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<Review>> getProductReviews(@PathVariable String productId) {
        return ResponseEntity.ok(reviewService.getProductReviews(productId));
    }
    
    @GetMapping("/product/{productId}/verified")
    public ResponseEntity<List<Review>> getVerifiedReviews(@PathVariable String productId) {
        return ResponseEntity.ok(reviewService.getVerifiedReviews(productId));
    }
    
    @GetMapping("/rating/{minRating}")
    public ResponseEntity<List<Review>> getHighRatedReviews(@PathVariable Integer minRating) {
        return ResponseEntity.ok(reviewService.getHighRatedReviews(minRating));
    }
    
    @GetMapping("/product/{productId}/count")
    public ResponseEntity<Long> countProductReviews(@PathVariable String productId) {
        return ResponseEntity.ok(reviewService.countProductReviews(productId));
    }
    
    @GetMapping("/info")
    public ResponseEntity<String> getInfo() {
        return ResponseEntity.ok("""
            Elasticsearch Repository Pattern
            
            This pattern demonstrates Spring Data Elasticsearch Repository
            for automatic query generation and type-safe operations.
            
            Features:
            - @Document annotation for index mapping
            - Derived query methods (findBy, countBy, existsBy, deleteBy)
            - Field type annotations (@Field with FieldType)
            - Automatic query generation
            - Type-safe repository interface
            - Multi-field queries (And, Or conditions)
            - Range queries (GreaterThan, LessThan, Between)
            - Text search (Containing queries)
            
            Endpoints:
            Products:
            - POST /api/products - Create product
            - POST /api/products/bulk - Create multiple products
            - GET /api/products/{id} - Get product
            - GET /api/products - Get all products
            - GET /api/products/search/name/{name} - Find by name
            - GET /api/products/search/category/{category} - Find by category
            - GET /api/products/search/brand/{brand} - Find by brand
            - GET /api/products/search/price?min=&max= - Find by price range
            - GET /api/products/search/tag/{tag} - Find by tag
            - GET /api/products/search/available/{category} - Find available by category
            - GET /api/products/count/{category} - Count by category
            - GET /api/products/exists/{name} - Check if exists
            - DELETE /api/products/{id} - Delete product
            - DELETE /api/products/category/{category} - Delete by category
            
            Reviews:
            - POST /api/reviews - Create review
            - GET /api/reviews/product/{productId} - Get product reviews
            - GET /api/reviews/product/{productId}/verified - Get verified reviews
            - GET /api/reviews/rating/{minRating} - Get high-rated reviews
            - GET /api/reviews/product/{productId}/count - Count reviews
            """);
    }
}
