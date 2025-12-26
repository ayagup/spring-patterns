package com.example.neo4j;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.neo4j.core.ReactiveNeo4jTemplate;
import org.springframework.data.neo4j.core.schema.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Reactive Neo4j Template Pattern
 *
 * Demonstrates the use of ReactiveNeo4jTemplate for non-blocking, reactive graph operations.
 * This pattern is ideal for applications that require high concurrency and scalability,
 * leveraging Project Reactor for asynchronous data streams.
 *
 * Key Features:
 * - Non-blocking API for all graph operations
 * - Returns Mono and Flux for single and multiple results
 * - Integration with reactive web frameworks like Spring WebFlux
 * - Backpressure support
 * - Composable and declarative API
 *
 * Use Cases:
 * - High-performance, scalable APIs
 * - Real-time data streaming applications
 * - Microservices with high throughput requirements
 * - Applications built on a fully reactive stack
 *
 * @author Spring Patterns
 */
@SpringBootApplication
public class ReactiveNeo4jTemplatePattern {

    public static void main(String[] args) {
        SpringApplication.run(ReactiveNeo4jTemplatePattern.class, args);
    }
}

// Domain Model
@Node
class Product {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private String category;
    private double price;

    public Product() {}

    public Product(String name, String category, double price) {
        this.name = name;
        this.category = category;
        this.price = price;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
}

// Service Layer
@Service
class ReactiveProductGraphService {

    private final ReactiveNeo4jTemplate reactiveNeo4jTemplate;

    public ReactiveProductGraphService(ReactiveNeo4jTemplate reactiveNeo4jTemplate) {
        this.reactiveNeo4jTemplate = reactiveNeo4jTemplate;
    }

    public Mono<Product> saveProduct(Product product) {
        return reactiveNeo4jTemplate.save(product);
    }

    public Mono<Product> findById(Long id) {
        return reactiveNeo4jTemplate.findById(id, Product.class);
    }

    public Flux<Product> findAll() {
        return reactiveNeo4jTemplate.findAll(Product.class);
    }

    public Flux<Product> findByCategory(String category) {
        String cypher = "MATCH (p:Product) WHERE p.category = $category RETURN p";
        return reactiveNeo4jTemplate.findAll(cypher, Map.of("category", category), Product.class);
    }

    public Mono<Void> deleteById(Long id) {
        return reactiveNeo4jTemplate.deleteById(id, Product.class);
    }

    public Mono<Long> countProducts() {
        return reactiveNeo4jTemplate.count(Product.class);
    }

    public Mono<Product> updatePrice(Long id, double newPrice) {
        return reactiveNeo4jTemplate.findById(id, Product.class)
                .flatMap(product -> {
                    product.setPrice(newPrice);
                    return reactiveNeo4jTemplate.save(product);
                });
    }
    
    public Flux<Map<String, Object>> getCategoryPriceStats() {
        String cypher = """
            MATCH (p:Product)
            RETURN p.category as category, 
                   avg(p.price) as averagePrice, 
                   min(p.price) as minPrice, 
                   max(p.price) as maxPrice,
                   count(p) as productCount
            """;
        return reactiveNeo4jTemplate.findAll(cypher, Map.class);
    }
}

// REST Controller
@RestController
@RequestMapping("/api/reactive-neo4j-template")
class ReactiveProductGraphController {

    private final ReactiveProductGraphService service;

    public ReactiveProductGraphController(ReactiveProductGraphService service) {
        this.service = service;
    }

    @PostMapping("/products")
    public Mono<ResponseEntity<Product>> createProduct(@RequestBody Product product) {
        return service.saveProduct(product)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/products/{id}")
    public Mono<ResponseEntity<Product>> getProductById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/products")
    public Flux<Product> getAllProducts() {
        return service.findAll();
    }

    @GetMapping("/products/category/{category}")
    public Flux<Product> getProductsByCategory(@PathVariable String category) {
        return service.findByCategory(category);
    }

    @DeleteMapping("/products/{id}")
    public Mono<ResponseEntity<Void>> deleteProduct(@PathVariable Long id) {
        return service.deleteById(id)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }

    @GetMapping("/products/count")
    public Mono<Long> countProducts() {
        return service.countProducts();
    }

    @PutMapping("/products/{id}/price")
    public Mono<ResponseEntity<Product>> updatePrice(@PathVariable Long id, @RequestParam double newPrice) {
        return service.updatePrice(id, newPrice)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/products/stats")
    public Flux<Map<String, Object>> getStats() {
        return service.getCategoryPriceStats();
    }
    
    @GetMapping("/info")
    public Mono<Map<String, String>> getInfo() {
        return Mono.just(Map.of(
            "pattern", "Reactive Neo4j Template Pattern",
            "description", "Non-blocking graph operations using ReactiveNeo4jTemplate",
            "features", "Mono/Flux API, backpressure, integration with WebFlux",
            "endpoints", "8 reactive REST endpoints for product management"
        ));
    }
}
