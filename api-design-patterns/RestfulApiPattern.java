package com.example.api.restful;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * RESTful API Pattern Implementation
 * 
 * Purpose: Implements REST architectural constraints for building scalable,
 * stateless web services using standard HTTP methods and status codes.
 * 
 * Key Components:
 * 1. Resource representations (JSON/XML)
 * 2. HTTP methods (GET, POST, PUT, PATCH, DELETE)
 * 3. Proper HTTP status codes
 * 4. Stateless communication
 * 5. URI-based resource identification
 * 
 * Features:
 * - CRUD operations via HTTP methods
 * - Proper status codes (200, 201, 204, 400, 404, etc.)
 * - Content negotiation
 * - Error handling with standard formats
 * - Resource validation
 */

// Product Resource Model
class Product {
    private Long id;
    private String name;
    private String description;
    private Double price;
    private String category;
    private Integer stock;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public Product() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public Product(Long id, String name, String description, Double price, String category, Integer stock) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.stock = stock;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { 
        this.name = name;
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { 
        this.description = description;
        this.updatedAt = LocalDateTime.now();
    }
    
    public Double getPrice() { return price; }
    public void setPrice(Double price) { 
        this.price = price;
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { 
        this.category = category;
        this.updatedAt = LocalDateTime.now();
    }
    
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { 
        this.stock = stock;
        this.updatedAt = LocalDateTime.now();
    }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}

// API Error Response
class ApiError {
    private int status;
    private String error;
    private String message;
    private String path;
    private LocalDateTime timestamp;
    
    public ApiError(int status, String error, String message, String path) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.timestamp = LocalDateTime.now();
    }
    
    // Getters
    public int getStatus() { return status; }
    public String getError() { return error; }
    public String getMessage() { return message; }
    public String getPath() { return path; }
    public LocalDateTime getTimestamp() { return timestamp; }
}

// Product Service
class ProductService {
    private final Map<Long, Product> products = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    
    public ProductService() {
        // Initialize with sample data
        createProduct(new Product(null, "Laptop", "High-performance laptop", 999.99, "Electronics", 50));
        createProduct(new Product(null, "Mouse", "Wireless mouse", 29.99, "Electronics", 100));
        createProduct(new Product(null, "Desk", "Ergonomic desk", 299.99, "Furniture", 20));
    }
    
    public List<Product> getAllProducts() {
        return new ArrayList<>(products.values());
    }
    
    public Optional<Product> getProductById(Long id) {
        return Optional.ofNullable(products.get(id));
    }
    
    public Product createProduct(Product product) {
        Long id = idGenerator.getAndIncrement();
        product.setId(id);
        products.put(id, product);
        return product;
    }
    
    public Optional<Product> updateProduct(Long id, Product updatedProduct) {
        Product existing = products.get(id);
        if (existing == null) {
            return Optional.empty();
        }
        
        updatedProduct.setId(id);
        products.put(id, updatedProduct);
        return Optional.of(updatedProduct);
    }
    
    public Optional<Product> partialUpdateProduct(Long id, Map<String, Object> updates) {
        Product product = products.get(id);
        if (product == null) {
            return Optional.empty();
        }
        
        updates.forEach((key, value) -> {
            switch (key) {
                case "name":
                    product.setName((String) value);
                    break;
                case "description":
                    product.setDescription((String) value);
                    break;
                case "price":
                    product.setPrice(((Number) value).doubleValue());
                    break;
                case "category":
                    product.setCategory((String) value);
                    break;
                case "stock":
                    product.setStock(((Number) value).intValue());
                    break;
            }
        });
        
        return Optional.of(product);
    }
    
    public boolean deleteProduct(Long id) {
        return products.remove(id) != null;
    }
    
    public List<Product> getProductsByCategory(String category) {
        return products.values().stream()
            .filter(p -> p.getCategory().equalsIgnoreCase(category))
            .collect(Collectors.toList());
    }
}

// RESTful Product API Controller
@RestController
@RequestMapping("/api/products")
class ProductController {
    private final ProductService productService = new ProductService();
    
    /**
     * GET /api/products - Retrieve all products
     * HTTP 200 OK
     */
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }
    
    /**
     * GET /api/products/{id} - Retrieve a specific product
     * HTTP 200 OK if found
     * HTTP 404 Not Found if not exists
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable Long id) {
        return productService.getProductById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiError(404, "Not Found", "Product not found with id: " + id, "/api/products/" + id)));
    }
    
    /**
     * POST /api/products - Create a new product
     * HTTP 201 Created with Location header
     * HTTP 400 Bad Request if validation fails
     */
    @PostMapping
    public ResponseEntity<?> createProduct(@RequestBody Product product) {
        // Validation
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(new ApiError(400, "Bad Request", "Product name is required", "/api/products"));
        }
        
        if (product.getPrice() == null || product.getPrice() <= 0) {
            return ResponseEntity.badRequest()
                .body(new ApiError(400, "Bad Request", "Product price must be greater than 0", "/api/products"));
        }
        
        Product created = productService.createProduct(product);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .header("Location", "/api/products/" + created.getId())
            .body(created);
    }
    
    /**
     * PUT /api/products/{id} - Full update of a product
     * HTTP 200 OK if updated
     * HTTP 404 Not Found if not exists
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        return productService.updateProduct(id, product)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiError(404, "Not Found", "Product not found with id: " + id, "/api/products/" + id)));
    }
    
    /**
     * PATCH /api/products/{id} - Partial update of a product
     * HTTP 200 OK if updated
     * HTTP 404 Not Found if not exists
     */
    @PatchMapping("/{id}")
    public ResponseEntity<?> partialUpdateProduct(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        return productService.partialUpdateProduct(id, updates)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiError(404, "Not Found", "Product not found with id: " + id, "/api/products/" + id)));
    }
    
    /**
     * DELETE /api/products/{id} - Delete a product
     * HTTP 204 No Content if deleted
     * HTTP 404 Not Found if not exists
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        boolean deleted = productService.deleteProduct(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiError(404, "Not Found", "Product not found with id: " + id, "/api/products/" + id));
        }
    }
    
    /**
     * GET /api/products/category/{category} - Get products by category
     * HTTP 200 OK
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<Product>> getProductsByCategory(@PathVariable String category) {
        List<Product> products = productService.getProductsByCategory(category);
        return ResponseEntity.ok(products);
    }
    
    /**
     * HEAD /api/products/{id} - Check if product exists
     * HTTP 200 OK if exists
     * HTTP 404 Not Found if not exists
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.HEAD)
    public ResponseEntity<Void> checkProductExists(@PathVariable Long id) {
        return productService.getProductById(id)
            .map(p -> ResponseEntity.ok().<Void>build())
            .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * OPTIONS /api/products - Return allowed methods
     */
    @RequestMapping(method = RequestMethod.OPTIONS)
    public ResponseEntity<Void> options() {
        return ResponseEntity
            .ok()
            .header("Allow", "GET, POST, PUT, PATCH, DELETE, HEAD, OPTIONS")
            .build();
    }
}

/**
 * Demonstration of RESTful API Pattern
 */
public class RestfulApiPattern {
    
    public static void main(String[] args) {
        System.out.println("=== RESTful API Pattern Demo ===\n");
        
        ProductService service = new ProductService();
        
        // Simulate REST operations
        System.out.println("1. GET /api/products - Retrieve all products");
        List<Product> allProducts = service.getAllProducts();
        System.out.println("   Found " + allProducts.size() + " products");
        allProducts.forEach(p -> System.out.println("   - " + p.getName() + " ($" + p.getPrice() + ")"));
        
        System.out.println("\n2. POST /api/products - Create new product");
        Product newProduct = new Product(null, "Keyboard", "Mechanical keyboard", 89.99, "Electronics", 75);
        Product created = service.createProduct(newProduct);
        System.out.println("   Created: " + created.getName() + " with ID: " + created.getId());
        System.out.println("   HTTP 201 Created");
        System.out.println("   Location: /api/products/" + created.getId());
        
        System.out.println("\n3. GET /api/products/" + created.getId() + " - Retrieve specific product");
        Optional<Product> retrieved = service.getProductById(created.getId());
        if (retrieved.isPresent()) {
            System.out.println("   Found: " + retrieved.get().getName());
            System.out.println("   HTTP 200 OK");
        }
        
        System.out.println("\n4. PUT /api/products/" + created.getId() + " - Full update");
        Product updated = new Product(created.getId(), "Mechanical Keyboard", 
                                     "RGB Mechanical keyboard", 119.99, "Electronics", 60);
        service.updateProduct(created.getId(), updated);
        System.out.println("   Updated product name and price");
        System.out.println("   HTTP 200 OK");
        
        System.out.println("\n5. PATCH /api/products/" + created.getId() + " - Partial update");
        Map<String, Object> updates = new HashMap<>();
        updates.put("stock", 50);
        updates.put("price", 99.99);
        service.partialUpdateProduct(created.getId(), updates);
        System.out.println("   Updated stock and price only");
        System.out.println("   HTTP 200 OK");
        
        System.out.println("\n6. GET /api/products/category/Electronics - Filter by category");
        List<Product> electronics = service.getProductsByCategory("Electronics");
        System.out.println("   Found " + electronics.size() + " electronics products");
        System.out.println("   HTTP 200 OK");
        
        System.out.println("\n7. DELETE /api/products/" + created.getId() + " - Delete product");
        boolean deleted = service.deleteProduct(created.getId());
        System.out.println("   Deleted: " + deleted);
        System.out.println("   HTTP 204 No Content");
        
        System.out.println("\n8. GET /api/products/999 - Try to get non-existent product");
        Optional<Product> notFound = service.getProductById(999L);
        if (!notFound.isPresent()) {
            System.out.println("   HTTP 404 Not Found");
            System.out.println("   Error: Product not found with id: 999");
        }
        
        // REST Principles Summary
        System.out.println("\n=== RESTful API Principles ===");
        System.out.println("1. Resource-based: URIs identify resources (/api/products/{id})");
        System.out.println("2. HTTP Methods:");
        System.out.println("   - GET: Retrieve resources (safe, idempotent)");
        System.out.println("   - POST: Create new resources");
        System.out.println("   - PUT: Full update (idempotent)");
        System.out.println("   - PATCH: Partial update");
        System.out.println("   - DELETE: Remove resources (idempotent)");
        System.out.println("   - HEAD: Check existence without body");
        System.out.println("   - OPTIONS: Get allowed methods");
        System.out.println("3. HTTP Status Codes:");
        System.out.println("   - 200 OK: Successful GET, PUT, PATCH");
        System.out.println("   - 201 Created: Successful POST");
        System.out.println("   - 204 No Content: Successful DELETE");
        System.out.println("   - 400 Bad Request: Validation error");
        System.out.println("   - 404 Not Found: Resource doesn't exist");
        System.out.println("   - 500 Internal Server Error: Server error");
        System.out.println("4. Stateless: Each request contains all necessary information");
        System.out.println("5. Cacheable: Responses can be cached (GET, HEAD)");
        System.out.println("6. Uniform Interface: Standard HTTP methods and status codes");
    }
}
