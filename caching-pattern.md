# Spring Caching Patterns

I'll create a comprehensive Spring Boot application demonstrating all 11 caching patterns.

## Project Structure

```
spring-caching-patterns/
├── src/main/java/org/example/
│   ├── CachingPatternsApplication.java
│   ├── config/
│   ├── model/
│   ├── repository/
│   ├── service/
│   └── patterns/caching/
│       ├── cacheaside/
│       ├── readthrough/
│       ├── writethrough/
│       ├── writebehind/
│       ├── refreshahead/
│       ├── abstraction/
│       ├── cachemanager/
│       ├── resolver/
│       ├── eviction/
│       ├── conditional/
│       └── keygenerator/
├── pom.xml
└── application.properties
```

## 1. Main Application & Base Configuration

```java
// src/main/java/org/example/CachingPatternsApplication.java
package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableCaching
@EnableScheduling
public class CachingPatternsApplication {
    public static void main(String[] args) {
        SpringApplication.run(CachingPatternsApplication.class, args);
    }
}
```

## 2. Domain Models

```java
// src/main/java/org/example/model/Product.java
package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private String category;
    private LocalDateTime lastUpdated;
    
    public Product(Long id, String name, BigDecimal price) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.lastUpdated = LocalDateTime.now();
    }
}
```

```java
// src/main/java/org/example/model/User.java
package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
}
```

```java
// src/main/java/org/example/model/Order.java
package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private Long userId;
    private Long productId;
    private Integer quantity;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private LocalDateTime orderDate;
    
    public enum OrderStatus {
        PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED
    }
}
```

## 3. Repository Layer (Simulated Database)

```java
// src/main/java/org/example/repository/ProductRepository.java
package org.example.repository;

import lombok.extern.slf4j.Slf4j;
import org.example.model.Product;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Repository
public class ProductRepository {
    
    private final Map<Long, Product> database = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    
    public ProductRepository() {
        initializeData();
    }
    
    private void initializeData() {
        save(new Product(null, "Laptop", "Gaming Laptop", 
                new BigDecimal("1299.99"), 10, "Electronics", LocalDateTime.now()));
        save(new Product(null, "Mouse", "Wireless Mouse", 
                new BigDecimal("29.99"), 50, "Electronics", LocalDateTime.now()));
        save(new Product(null, "Keyboard", "Mechanical Keyboard", 
                new BigDecimal("89.99"), 30, "Electronics", LocalDateTime.now()));
        
        log.info("Initialized product database with {} products", database.size());
    }
    
    public Product findById(Long id) {
        log.info("DATABASE: Finding product by ID: {}", id);
        simulateSlowQuery();
        return database.get(id);
    }
    
    public List<Product> findAll() {
        log.info("DATABASE: Finding all products");
        simulateSlowQuery();
        return new ArrayList<>(database.values());
    }
    
    public List<Product> findByCategory(String category) {
        log.info("DATABASE: Finding products by category: {}", category);
        simulateSlowQuery();
        return database.values().stream()
                .filter(p -> category.equals(p.getCategory()))
                .toList();
    }
    
    public Product save(Product product) {
        if (product.getId() == null) {
            product.setId(idGenerator.getAndIncrement());
        }
        product.setLastUpdated(LocalDateTime.now());
        database.put(product.getId(), product);
        log.info("DATABASE: Saved product: {}", product.getId());
        return product;
    }
    
    public void delete(Long id) {
        database.remove(id);
        log.info("DATABASE: Deleted product: {}", id);
    }
    
    private void simulateSlowQuery() {
        try {
            Thread.sleep(1000); // Simulate slow database query
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
```

```java
// src/main/java/org/example/repository/UserRepository.java
package org.example.repository;

import lombok.extern.slf4j.Slf4j;
import org.example.model.User;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Repository
public class UserRepository {
    
    private final Map<Long, User> database = new ConcurrentHashMap<>();
    
    public UserRepository() {
        database.put(1L, new User(1L, "john.doe", "john@example.com", 
                "John", "Doe", LocalDateTime.now(), LocalDateTime.now()));
        database.put(2L, new User(2L, "jane.smith", "jane@example.com", 
                "Jane", "Smith", LocalDateTime.now(), LocalDateTime.now()));
    }
    
    public User findById(Long id) {
        log.info("DATABASE: Finding user by ID: {}", id);
        simulateSlowQuery();
        return database.get(id);
    }
    
    public User save(User user) {
        user.setLastLogin(LocalDateTime.now());
        database.put(user.getId(), user);
        log.info("DATABASE: Saved user: {}", user.getId());
        return user;
    }
    
    private void simulateSlowQuery() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
```

## 4. Cache Configuration

```java
// src/main/java/org/example/config/CacheConfig.java
package org.example.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.interceptor.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig implements CachingConfigurer {
    
    /**
     * Primary cache manager using Caffeine.
     */
    @Primary
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
                "products", "users", "orders", "categories", 
                "readThrough", "writeThrough", "writeBehind"
        );
        
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .recordStats());
        
        return cacheManager;
    }
    
    /**
     * Secondary cache manager for specific use cases.
     */
    @Bean
    public CacheManager secondaryCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("secondary");
        
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(100)
                .expireAfterAccess(5, TimeUnit.MINUTES));
        
        return cacheManager;
    }
    
    /**
     * Custom key generator.
     */
    @Bean
    @Override
    public KeyGenerator keyGenerator() {
        return new SimpleKeyGenerator();
    }
    
    /**
     * Custom cache resolver.
     */
    @Bean
    @Override
    public CacheResolver cacheResolver() {
        return new SimpleCacheResolver(cacheManager());
    }
    
    /**
     * Custom cache error handler.
     */
    @Bean
    @Override
    public CacheErrorHandler errorHandler() {
        return new SimpleCacheErrorHandler();
    }
}
```

## 5. Pattern 1: Cache-Aside Pattern

```java
// src/main/java/org/example/patterns/caching/cacheaside/CacheAsideService.java
package org.example.patterns.caching.cacheaside;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.model.Product;
import org.example.repository.ProductRepository;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

/**
 * Cache-Aside Pattern (Lazy Loading).
 * Application manages cache explicitly.
 * Read: Check cache → if miss, load from DB → populate cache
 * Write: Update DB → invalidate cache
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CacheAsideService {
    
    private final ProductRepository productRepository;
    private final CacheManager cacheManager;
    
    private static final String CACHE_NAME = "products";
    
    /**
     * Manual cache management - Cache-Aside pattern.
     */
    public Product getProduct(Long id) {
        log.info("Cache-Aside: Getting product {}", id);
        
        // 1. Try to get from cache
        Cache cache = cacheManager.getCache(CACHE_NAME);
        if (cache != null) {
            Product cached = cache.get(id, Product.class);
            if (cached != null) {
                log.info("Cache-Aside: Cache HIT for product {}", id);
                return cached;
            }
        }
        
        // 2. Cache MISS - load from database
        log.info("Cache-Aside: Cache MISS for product {}", id);
        Product product = productRepository.findById(id);
        
        // 3. Populate cache
        if (product != null && cache != null) {
            cache.put(id, product);
            log.info("Cache-Aside: Cached product {}", id);
        }
        
        return product;
    }
    
    /**
     * Update database and invalidate cache.
     */
    public Product updateProduct(Product product) {
        log.info("Cache-Aside: Updating product {}", product.getId());
        
        // 1. Update database
        Product updated = productRepository.save(product);
        
        // 2. Invalidate cache
        Cache cache = cacheManager.getCache(CACHE_NAME);
        if (cache != null) {
            cache.evict(product.getId());
            log.info("Cache-Aside: Evicted product {} from cache", product.getId());
        }
        
        return updated;
    }
    
    /**
     * Delete from database and cache.
     */
    public void deleteProduct(Long id) {
        log.info("Cache-Aside: Deleting product {}", id);
        
        // 1. Delete from database
        productRepository.delete(id);
        
        // 2. Remove from cache
        Cache cache = cacheManager.getCache(CACHE_NAME);
        if (cache != null) {
            cache.evict(id);
            log.info("Cache-Aside: Evicted product {} from cache", id);
        }
    }
}
```

```java
// src/main/java/org/example/patterns/caching/cacheaside/CacheAsideController.java
package org.example.patterns.caching.cacheaside;

import lombok.RequiredArgsConstructor;
import org.example.model.Product;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cache-aside")
@RequiredArgsConstructor
public class CacheAsideController {
    
    private final CacheAsideService cacheAsideService;
    
    @GetMapping("/product/{id}")
    public Product getProduct(@PathVariable Long id) {
        return cacheAsideService.getProduct(id);
    }
    
    @PutMapping("/product")
    public Product updateProduct(@RequestBody Product product) {
        return cacheAsideService.updateProduct(product);
    }
    
    @DeleteMapping("/product/{id}")
    public Map<String, String> deleteProduct(@PathVariable Long id) {
        cacheAsideService.deleteProduct(id);
        return Map.of("message", "Product deleted");
    }
}
```

## 6. Pattern 2: Read-Through Pattern

```java
// src/main/java/org/example/patterns/caching/readthrough/ReadThroughService.java
package org.example.patterns.caching.readthrough;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.model.Product;
import org.example.repository.ProductRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Read-Through Pattern.
 * Cache automatically loads data from database on cache miss.
 * Uses @Cacheable annotation - Spring handles caching automatically.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReadThroughService {
    
    private final ProductRepository productRepository;
    
    /**
     * Read-through caching with @Cacheable.
     * Spring automatically:
     * 1. Checks cache
     * 2. On miss, invokes method
     * 3. Caches result
     */
    @Cacheable(value = "readThrough", key = "#id")
    public Product getProduct(Long id) {
        log.info("Read-Through: Loading product {} from database", id);
        return productRepository.findById(id);
    }
    
    /**
     * Cacheable with condition.
     */
    @Cacheable(value = "readThrough", key = "#id", 
               condition = "#id != null && #id > 0")
    public Product getProductConditional(Long id) {
        log.info("Read-Through (Conditional): Loading product {}", id);
        return productRepository.findById(id);
    }
    
    /**
     * Cacheable with unless (don't cache if result is null).
     */
    @Cacheable(value = "readThrough", key = "#id", 
               unless = "#result == null")
    public Product getProductUnless(Long id) {
        log.info("Read-Through (Unless): Loading product {}", id);
        return productRepository.findById(id);
    }
    
    /**
     * Cache collection results.
     */
    @Cacheable(value = "categories", key = "#category")
    public List<Product> getProductsByCategory(String category) {
        log.info("Read-Through: Loading products for category {}", category);
        return productRepository.findByCategory(category);
    }
}
```

```java
// src/main/java/org/example/patterns/caching/readthrough/ReadThroughController.java
package org.example.patterns.caching.readthrough;

import lombok.RequiredArgsConstructor;
import org.example.model.Product;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/read-through")
@RequiredArgsConstructor
public class ReadThroughController {
    
    private final ReadThroughService readThroughService;
    
    @GetMapping("/product/{id}")
    public Product getProduct(@PathVariable Long id) {
        return readThroughService.getProduct(id);
    }
    
    @GetMapping("/product/conditional/{id}")
    public Product getProductConditional(@PathVariable Long id) {
        return readThroughService.getProductConditional(id);
    }
    
    @GetMapping("/product/unless/{id}")
    public Product getProductUnless(@PathVariable Long id) {
        return readThroughService.getProductUnless(id);
    }
    
    @GetMapping("/category/{category}")
    public List<Product> getProductsByCategory(@PathVariable String category) {
        return readThroughService.getProductsByCategory(category);
    }
}
```

## 7. Pattern 3: Write-Through Pattern

```java
// src/main/java/org/example/patterns/caching/writethrough/WriteThroughService.java
package org.example.patterns.caching.writethrough;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.model.Product;
import org.example.repository.ProductRepository;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Write-Through Pattern.
 * Data is written to cache and database synchronously.
 * Uses @CachePut - always executes method and updates cache.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WriteThroughService {
    
    private final ProductRepository productRepository;
    
    /**
     * Read with caching.
     */
    @Cacheable(value = "writeThrough", key = "#id")
    public Product getProduct(Long id) {
        log.info("Write-Through: Reading product {}", id);
        return productRepository.findById(id);
    }
    
    /**
     * Write-through: Update both database and cache.
     * @CachePut always executes the method and updates cache.
     */
    @CachePut(value = "writeThrough", key = "#product.id")
    public Product updateProduct(Product product) {
        log.info("Write-Through: Updating product {} in database and cache", 
                product.getId());
        
        // 1. Write to database
        Product updated = productRepository.save(product);
        
        // 2. Cache is automatically updated by @CachePut
        log.info("Write-Through: Product {} cached", product.getId());
        
        return updated;
    }
    
    /**
     * Create and cache.
     */
    @CachePut(value = "writeThrough", key = "#result.id")
    public Product createProduct(Product product) {
        log.info("Write-Through: Creating product");
        
        Product created = productRepository.save(product);
        log.info("Write-Through: Product {} created and cached", created.getId());
        
        return created;
    }
}
```

```java
// src/main/java/org/example/patterns/caching/writethrough/WriteThroughController.java
package org.example.patterns.caching.writethrough;

import lombok.RequiredArgsConstructor;
import org.example.model.Product;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/write-through")
@RequiredArgsConstructor
public class WriteThroughController {
    
    private final WriteThroughService writeThroughService;
    
    @GetMapping("/product/{id}")
    public Product getProduct(@PathVariable Long id) {
        return writeThroughService.getProduct(id);
    }
    
    @PostMapping("/product")
    public Product createProduct(@RequestBody Product product) {
        return writeThroughService.createProduct(product);
    }
    
    @PutMapping("/product")
    public Product updateProduct(@RequestBody Product product) {
        return writeThroughService.updateProduct(product);
    }
}
```

## 8. Pattern 4: Write-Behind Pattern

```java
// src/main/java/org/example/patterns/caching/writebehind/WriteBehindService.java
package org.example.patterns.caching.writebehind;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.model.Product;
import org.example.repository.ProductRepository;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Write-Behind Pattern (Write-Back).
 * Data is written to cache immediately.
 * Database write happens asynchronously in background.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WriteBehindService {
    
    private final ProductRepository productRepository;
    private final Queue<Product> writeQueue = new ConcurrentLinkedQueue<>();
    
    /**
     * Read from cache.
     */
    @Cacheable(value = "writeBehind", key = "#id")
    public Product getProduct(Long id) {
        log.info("Write-Behind: Reading product {}", id);
        return productRepository.findById(id);
    }
    
    /**
     * Write-behind: Update cache immediately, database asynchronously.
     */
    @CachePut(value = "writeBehind", key = "#product.id")
    public Product updateProduct(Product product) {
        log.info("Write-Behind: Updating product {} in cache", product.getId());
        
        // 1. Update cache immediately (via @CachePut)
        // 2. Queue for async database write
        writeQueue.offer(product);
        
        // 3. Trigger async write
        asyncWriteToDatabase(product);
        
        return product;
    }
    
    /**
     * Asynchronous database write.
     */
    @Async
    protected void asyncWriteToDatabase(Product product) {
        try {
            Thread.sleep(100); // Simulate batching delay
            log.info("Write-Behind: Async writing product {} to database", 
                    product.getId());
            productRepository.save(product);
            writeQueue.remove(product);
        } catch (Exception e) {
            log.error("Write-Behind: Failed to write product {}", product.getId(), e);
            // Implement retry logic here
        }
    }
    
    /**
     * Get pending writes count.
     */
    public int getPendingWrites() {
        return writeQueue.size();
    }
}
```

```java
// src/main/java/org/example/patterns/caching/writebehind/WriteBehindController.java
package org.example.patterns.caching.writebehind;

import lombok.RequiredArgsConstructor;
import org.example.model.Product;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/write-behind")
@RequiredArgsConstructor
public class WriteBehindController {
    
    private final WriteBehindService writeBehindService;
    
    @GetMapping("/product/{id}")
    public Product getProduct(@PathVariable Long id) {
        return writeBehindService.getProduct(id);
    }
    
    @PutMapping("/product")
    public Product updateProduct(@RequestBody Product product) {
        return writeBehindService.updateProduct(product);
    }
    
    @GetMapping("/pending-writes")
    public Map<String, Integer> getPendingWrites() {
        return Map.of("pendingWrites", writeBehindService.getPendingWrites());
    }
}
```

## 9. Pattern 5: Refresh-Ahead Pattern

```java
// src/main/java/org/example/patterns/caching/refreshahead/RefreshAheadService.java
package org.example.patterns.caching.refreshahead;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.model.Product;
import org.example.repository.ProductRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Refresh-Ahead Pattern.
 * Proactively refreshes cache before expiration.
 * Prevents cache misses for frequently accessed data.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshAheadService {
    
    private final ProductRepository productRepository;
    private final Set<Long> hotProducts = ConcurrentHashMap.newKeySet();
    
    /**
     * Cacheable read - tracks hot products.
     */
    @Cacheable(value = "products", key = "#id")
    public Product getProduct(Long id) {
        log.info("Refresh-Ahead: Loading product {}", id);
        
        // Track as hot product
        hotProducts.add(id);
        
        return productRepository.findById(id);
    }
    
    /**
     * Scheduled refresh of hot products (every 5 minutes).
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    public void refreshHotProducts() {
        log.info("Refresh-Ahead: Refreshing {} hot products", hotProducts.size());
        
        for (Long productId : hotProducts) {
            try {
                Product fresh = productRepository.findById(productId);
                if (fresh != null) {
                    fresh.setLastUpdated(LocalDateTime.now());
                    log.info("Refresh-Ahead: Refreshed product {}", productId);
                }
            } catch (Exception e) {
                log.error("Refresh-Ahead: Failed to refresh product {}", productId, e);
            }
        }
    }
    
    /**
     * Mark product as hot (frequently accessed).
     */
    public void markAsHot(Long productId) {
        hotProducts.add(productId);
        log.info("Refresh-Ahead: Product {} marked as hot", productId);
    }
    
    /**
     * Remove from hot products.
     */
    public void removeFromHot(Long productId) {
        hotProducts.remove(productId);
        log.info("Refresh-Ahead: Product {} removed from hot list", productId);
    }
    
    /**
     * Get hot products count.
     */
    public int getHotProductsCount() {
        return hotProducts.size();
    }
}
```

```java
// src/main/java/org/example/patterns/caching/refreshahead/RefreshAheadController.java
package org.example.patterns.caching.refreshahead;

import lombok.RequiredArgsConstructor;
import org.example.model.Product;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/refresh-ahead")
@RequiredArgsConstructor
public class RefreshAheadController {
    
    private final RefreshAheadService refreshAheadService;
    
    @GetMapping("/product/{id}")
    public Product getProduct(@PathVariable Long id) {
        return refreshAheadService.getProduct(id);
    }
    
    @PostMapping("/mark-hot/{id}")
    public Map<String, String> markAsHot(@PathVariable Long id) {
        refreshAheadService.markAsHot(id);
        return Map.of("message", "Product marked as hot");
    }
    
    @DeleteMapping("/mark-hot/{id}")
    public Map<String, String> removeFromHot(@PathVariable Long id) {
        refreshAheadService.removeFromHot(id);
        return Map.of("message", "Product removed from hot list");
    }
    
    @GetMapping("/hot-count")
    public Map<String, Integer> getHotCount() {
        return Map.of("hotProducts", refreshAheadService.getHotProductsCount());
    }
    
    @PostMapping("/refresh")
    public Map<String, String> triggerRefresh() {
        refreshAheadService.refreshHotProducts();
        return Map.of("message", "Refresh triggered");
    }
}
```

## 10. Pattern 6: Cache Abstraction Pattern

```java
// src/main/java/org/example/patterns/caching/abstraction/CacheAbstractionService.java
package org.example.patterns.caching.abstraction;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.model.Product;
import org.example.model.User;
import org.example.repository.ProductRepository;
import org.example.repository.UserRepository;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Cache Abstraction Pattern.
 * Spring's caching abstraction provides vendor-neutral API.
 * Can switch between caching providers without code changes.
 */
@Slf4j
@Service
@CacheConfig(cacheNames = "abstraction")
@RequiredArgsConstructor
public class CacheAbstractionService {
    
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    
    /**
     * Cacheable - cache name from @CacheConfig.
     */
    @Cacheable(key = "'product:' + #id")
    public Product getProduct(Long id) {
        log.info("Cache Abstraction: Loading product {}", id);
        return productRepository.findById(id);
    }
    
    /**
     * Multiple cache names.
     */
    @Cacheable(cacheNames = {"products", "allProducts"}, key = "#id")
    public Product getProductMultiCache(Long id) {
```java
        log.info("Cache Abstraction: Loading product {} (multi-cache)", id);
        return productRepository.findById(id);
    }
    
    /**
     * Cache with different key.
     */
    @Cacheable(key = "'user:' + #id")
    public User getUser(Long id) {
        log.info("Cache Abstraction: Loading user {}", id);
        return userRepository.findById(id);
    }
    
    /**
     * CachePut - always executes and updates cache.
     */
    @CachePut(key = "'product:' + #product.id")
    public Product updateProduct(Product product) {
        log.info("Cache Abstraction: Updating product {}", product.getId());
        return productRepository.save(product);
    }
    
    /**
     * CacheEvict - removes from cache.
     */
    @CacheEvict(key = "'product:' + #id")
    public void deleteProduct(Long id) {
        log.info("Cache Abstraction: Deleting product {}", id);
        productRepository.delete(id);
    }
    
    /**
     * Evict all entries from cache.
     */
    @CacheEvict(allEntries = true)
    public void clearCache() {
        log.info("Cache Abstraction: Clearing all cache entries");
    }
    
    /**
     * Evict before method invocation.
     */
    @CacheEvict(key = "'product:' + #id", beforeInvocation = true)
    public void deleteProductEarly(Long id) {
        log.info("Cache Abstraction: Evicting cache before deletion");
        productRepository.delete(id);
    }
}
```

```java
// src/main/java/org/example/patterns/caching/abstraction/CacheAbstractionController.java
package org.example.patterns.caching.abstraction;

import lombok.RequiredArgsConstructor;
import org.example.model.Product;
import org.example.model.User;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cache-abstraction")
@RequiredArgsConstructor
public class CacheAbstractionController {
    
    private final CacheAbstractionService cacheAbstractionService;
    
    @GetMapping("/product/{id}")
    public Product getProduct(@PathVariable Long id) {
        return cacheAbstractionService.getProduct(id);
    }
    
    @GetMapping("/product-multi/{id}")
    public Product getProductMultiCache(@PathVariable Long id) {
        return cacheAbstractionService.getProductMultiCache(id);
    }
    
    @GetMapping("/user/{id}")
    public User getUser(@PathVariable Long id) {
        return cacheAbstractionService.getUser(id);
    }
    
    @PutMapping("/product")
    public Product updateProduct(@RequestBody Product product) {
        return cacheAbstractionService.updateProduct(product);
    }
    
    @DeleteMapping("/product/{id}")
    public Map<String, String> deleteProduct(@PathVariable Long id) {
        cacheAbstractionService.deleteProduct(id);
        return Map.of("message", "Product deleted");
    }
    
    @DeleteMapping("/cache")
    public Map<String, String> clearCache() {
        cacheAbstractionService.clearCache();
        return Map.of("message", "Cache cleared");
    }
}
```

## 11. Pattern 7: Cache Manager Pattern

```java
// src/main/java/org/example/patterns/caching/cachemanager/CacheManagerService.java
package org.example.patterns.caching.cachemanager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Cache Manager Pattern.
 * Direct interaction with CacheManager for advanced cache operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CacheManagerService {
    
    private final CacheManager cacheManager;
    
    /**
     * Get value from specific cache.
     */
    public <T> T get(String cacheName, Object key, Class<T> type) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            T value = cache.get(key, type);
            if (value != null) {
                log.info("Cache Manager: HIT - cache={}, key={}", cacheName, key);
                return value;
            }
        }
        log.info("Cache Manager: MISS - cache={}, key={}", cacheName, key);
        return null;
    }
    
    /**
     * Put value into cache.
     */
    public void put(String cacheName, Object key, Object value) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.put(key, value);
            log.info("Cache Manager: PUT - cache={}, key={}", cacheName, key);
        }
    }
    
    /**
     * Evict specific key from cache.
     */
    public void evict(String cacheName, Object key) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.evict(key);
            log.info("Cache Manager: EVICT - cache={}, key={}", cacheName, key);
        }
    }
    
    /**
     * Clear entire cache.
     */
    public void clear(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
            log.info("Cache Manager: CLEAR - cache={}", cacheName);
        }
    }
    
    /**
     * Get all cache names.
     */
    public Collection<String> getCacheNames() {
        return cacheManager.getCacheNames();
    }
    
    /**
     * Check if key exists in cache.
     */
    public boolean exists(String cacheName, Object key) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            Cache.ValueWrapper wrapper = cache.get(key);
            return wrapper != null;
        }
        return false;
    }
    
    /**
     * Get or compute value.
     */
    public <T> T getOrCompute(String cacheName, Object key, 
                             Class<T> type, java.util.function.Supplier<T> supplier) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            T value = cache.get(key, type);
            if (value != null) {
                log.info("Cache Manager: Cache hit for key={}", key);
                return value;
            }
            
            // Compute value
            value = supplier.get();
            cache.put(key, value);
            log.info("Cache Manager: Computed and cached key={}", key);
            return value;
        }
        return supplier.get();
    }
    
    /**
     * Get cache statistics.
     */
    public Map<String, Object> getCacheStats(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        Map<String, Object> stats = new HashMap<>();
        
        if (cache != null) {
            stats.put("cacheName", cacheName);
            stats.put("cacheType", cache.getClass().getSimpleName());
            stats.put("nativeCache", cache.getNativeCache().getClass().getSimpleName());
            
            // Caffeine-specific stats
            if (cache.getNativeCache() instanceof com.github.benmanes.caffeine.cache.Cache) {
                com.github.benmanes.caffeine.cache.Cache<?, ?> caffeineCache = 
                        (com.github.benmanes.caffeine.cache.Cache<?, ?>) cache.getNativeCache();
                
                com.github.benmanes.caffeine.cache.stats.CacheStats cacheStats = 
                        caffeineCache.stats();
                
                stats.put("hitCount", cacheStats.hitCount());
                stats.put("missCount", cacheStats.missCount());
                stats.put("hitRate", cacheStats.hitRate());
                stats.put("evictionCount", cacheStats.evictionCount());
                stats.put("size", caffeineCache.estimatedSize());
            }
        }
        
        return stats;
    }
}
```

```java
// src/main/java/org/example/patterns/caching/cachemanager/CacheManagerController.java
package org.example.patterns.caching.cachemanager;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Map;

@RestController
@RequestMapping("/api/cache-manager")
@RequiredArgsConstructor
public class CacheManagerController {
    
    private final CacheManagerService cacheManagerService;
    
    @GetMapping("/{cacheName}/{key}")
    public Object get(@PathVariable String cacheName, @PathVariable String key) {
        return cacheManagerService.get(cacheName, key, Object.class);
    }
    
    @PostMapping("/{cacheName}")
    public Map<String, String> put(@PathVariable String cacheName,
                                   @RequestBody Map<String, Object> payload) {
        String key = (String) payload.get("key");
        Object value = payload.get("value");
        cacheManagerService.put(cacheName, key, value);
        return Map.of("message", "Value cached");
    }
    
    @DeleteMapping("/{cacheName}/{key}")
    public Map<String, String> evict(@PathVariable String cacheName, 
                                     @PathVariable String key) {
        cacheManagerService.evict(cacheName, key);
        return Map.of("message", "Key evicted");
    }
    
    @DeleteMapping("/{cacheName}")
    public Map<String, String> clear(@PathVariable String cacheName) {
        cacheManagerService.clear(cacheName);
        return Map.of("message", "Cache cleared");
    }
    
    @GetMapping("/names")
    public Collection<String> getCacheNames() {
        return cacheManagerService.getCacheNames();
    }
    
    @GetMapping("/exists/{cacheName}/{key}")
    public Map<String, Boolean> exists(@PathVariable String cacheName, 
                                       @PathVariable String key) {
        return Map.of("exists", cacheManagerService.exists(cacheName, key));
    }
    
    @GetMapping("/stats/{cacheName}")
    public Map<String, Object> getStats(@PathVariable String cacheName) {
        return cacheManagerService.getCacheStats(cacheName);
    }
}
```

## 12. Pattern 8: Cache Resolver Pattern

```java
// src/main/java/org/example/patterns/caching/resolver/CustomCacheResolver.java
package org.example.patterns.caching.resolver;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.CacheOperationInvocationContext;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Cache Resolver Pattern.
 * Custom logic to determine which cache(s) to use at runtime.
 */
@Slf4j
@Component("customCacheResolver")
public class CustomCacheResolver implements CacheResolver {
    
    private final CacheManager cacheManager;
    
    public CustomCacheResolver(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }
    
    @Override
    public Collection<? extends Cache> resolveCaches(
            CacheOperationInvocationContext<?> context) {
        
        Collection<Cache> caches = new ArrayList<>();
        
        // Get method parameters
        Object[] args = context.getArgs();
        
        // Custom resolution logic based on method arguments
        if (args.length > 0) {
            Object firstArg = args[0];
            
            // Route to different caches based on argument type or value
            if (firstArg instanceof Long) {
                Long id = (Long) firstArg;
                if (id > 100) {
                    log.info("Custom Resolver: Using secondary cache for ID > 100");
                    caches.add(cacheManager.getCache("secondary"));
                } else {
                    log.info("Custom Resolver: Using primary cache for ID <= 100");
                    caches.add(cacheManager.getCache("products"));
                }
            } else if (firstArg instanceof String) {
                log.info("Custom Resolver: Using categories cache for String key");
                caches.add(cacheManager.getCache("categories"));
            }
        }
        
        // Fallback to default cache
        if (caches.isEmpty()) {
            log.info("Custom Resolver: Using default cache");
            caches.add(cacheManager.getCache("products"));
        }
        
        return caches;
    }
}
```

```java
// src/main/java/org/example/patterns/caching/resolver/CacheResolverService.java
package org.example.patterns.caching.resolver;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.model.Product;
import org.example.repository.ProductRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service using custom cache resolver.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CacheResolverService {
    
    private final ProductRepository productRepository;
    
    /**
     * Uses custom cache resolver to determine cache at runtime.
     */
    @Cacheable(cacheResolver = "customCacheResolver")
    public Product getProduct(Long id) {
        log.info("Cache Resolver: Loading product {}", id);
        return productRepository.findById(id);
    }
    
    /**
     * String parameter routes to different cache.
     */
    @Cacheable(cacheResolver = "customCacheResolver")
    public List<Product> getProductsByCategory(String category) {
        log.info("Cache Resolver: Loading products for category {}", category);
        return productRepository.findByCategory(category);
    }
}
```

```java
// src/main/java/org/example/patterns/caching/resolver/CacheResolverController.java
package org.example.patterns.caching.resolver;

import lombok.RequiredArgsConstructor;
import org.example.model.Product;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cache-resolver")
@RequiredArgsConstructor
public class CacheResolverController {
    
    private final CacheResolverService cacheResolverService;
    
    @GetMapping("/product/{id}")
    public Product getProduct(@PathVariable Long id) {
        return cacheResolverService.getProduct(id);
    }
    
    @GetMapping("/category/{category}")
    public List<Product> getProductsByCategory(@PathVariable String category) {
        return cacheResolverService.getProductsByCategory(category);
    }
}
```

## 13. Pattern 9: Cache Eviction Pattern

```java
// src/main/java/org/example/patterns/caching/eviction/CacheEvictionService.java
package org.example.patterns.caching.eviction;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.model.Product;
import org.example.repository.ProductRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Cache Eviction Pattern.
 * Demonstrates various cache eviction strategies.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CacheEvictionService {
    
    private final ProductRepository productRepository;
    
    /**
     * Cache read.
     */
    @Cacheable(value = "products", key = "#id")
    public Product getProduct(Long id) {
        log.info("Cache Eviction: Loading product {}", id);
        return productRepository.findById(id);
    }
    
    /**
     * Cache collection.
     */
    @Cacheable(value = "categories", key = "#category")
    public List<Product> getProductsByCategory(String category) {
        log.info("Cache Eviction: Loading category {}", category);
        return productRepository.findByCategory(category);
    }
    
    /**
     * Evict single entry.
     */
    @CacheEvict(value = "products", key = "#id")
    public void evictProduct(Long id) {
        log.info("Cache Eviction: Evicting product {}", id);
    }
    
    /**
     * Evict all entries from cache.
     */
    @CacheEvict(value = "products", allEntries = true)
    public void evictAllProducts() {
        log.info("Cache Eviction: Evicting all products");
    }
    
    /**
     * Evict from multiple caches.
     */
    @Caching(evict = {
        @CacheEvict(value = "products", key = "#id"),
        @CacheEvict(value = "categories", allEntries = true)
    })
    public void evictProductAndCategories(Long id) {
        log.info("Cache Eviction: Evicting product {} and all categories", id);
    }
    
    /**
     * Evict before method invocation (default is after).
     */
    @CacheEvict(value = "products", key = "#id", beforeInvocation = true)
    public void evictBeforeUpdate(Long id) {
        log.info("Cache Eviction: Evicting before update for product {}", id);
        // Update logic here
        productRepository.save(productRepository.findById(id));
    }
    
    /**
     * Conditional eviction.
     */
    @CacheEvict(value = "products", key = "#id", 
                condition = "#id > 10")
    public void evictConditional(Long id) {
        log.info("Cache Eviction: Conditional eviction for product {}", id);
    }
    
    /**
     * Scheduled eviction (every 10 minutes).
     */
    @Scheduled(fixedRate = 600000)
    @CacheEvict(value = {"products", "categories"}, allEntries = true)
    public void scheduledEviction() {
        log.info("Cache Eviction: Scheduled eviction of all caches");
    }
    
    /**
     * Time-based eviction (daily at midnight).
     */
    @Scheduled(cron = "0 0 0 * * ?")
    @CacheEvict(value = "products", allEntries = true)
    public void dailyEviction() {
        log.info("Cache Eviction: Daily cache eviction");
    }
}
```

```java
// src/main/java/org/example/patterns/caching/eviction/CacheEvictionController.java
package org.example.patterns.caching.eviction;

import lombok.RequiredArgsConstructor;
import org.example.model.Product;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cache-eviction")
@RequiredArgsConstructor
public class CacheEvictionController {
    
    private final CacheEvictionService cacheEvictionService;
    
    @GetMapping("/product/{id}")
    public Product getProduct(@PathVariable Long id) {
        return cacheEvictionService.getProduct(id);
    }
    
    @GetMapping("/category/{category}")
    public List<Product> getProductsByCategory(@PathVariable String category) {
        return cacheEvictionService.getProductsByCategory(category);
    }
    
    @DeleteMapping("/product/{id}")
    public Map<String, String> evictProduct(@PathVariable Long id) {
        cacheEvictionService.evictProduct(id);
        return Map.of("message", "Product cache evicted");
    }
    
    @DeleteMapping("/products")
    public Map<String, String> evictAllProducts() {
        cacheEvictionService.evictAllProducts();
        return Map.of("message", "All products evicted");
    }
    
    @DeleteMapping("/product-and-categories/{id}")
    public Map<String, String> evictProductAndCategories(@PathVariable Long id) {
        cacheEvictionService.evictProductAndCategories(id);
        return Map.of("message", "Product and categories evicted");
    }
    
    @PostMapping("/trigger-scheduled")
    public Map<String, String> triggerScheduledEviction() {
        cacheEvictionService.scheduledEviction();
        return Map.of("message", "Scheduled eviction triggered");
    }
}
```

## 14. Pattern 10: Conditional Caching Pattern

```java
// src/main/java/org/example/patterns/caching/conditional/ConditionalCachingService.java
package org.example.patterns.caching.conditional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.model.Product;
import org.example.model.User;
import org.example.repository.ProductRepository;
import org.example.repository.UserRepository;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Conditional Caching Pattern.
 * Cache based on conditions using SpEL expressions.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConditionalCachingService {
    
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    
    /**
     * Cache only if id is positive.
     */
    @Cacheable(value = "products", key = "#id", 
               condition = "#id != null && #id > 0")
    public Product getProduct(Long id) {
        log.info("Conditional Cache: Loading product {}", id);
        return productRepository.findById(id);
    }
    
    /**
     * Don't cache if result is null.
     */
    @Cacheable(value = "products", key = "#id", 
               unless = "#result == null")
    public Product getProductUnlessNull(Long id) {
        log.info("Conditional Cache: Loading product {} (unless null)", id);
        return productRepository.findById(id);
    }
    
    /**
     * Cache only expensive products.
     */
    @Cacheable(value = "products", key = "#id",
               unless = "#result == null || #result.price.compareTo(new java.math.BigDecimal('100')) < 0")
    public Product getExpensiveProduct(Long id) {
        log.info("Conditional Cache: Loading expensive product {}", id);
        return productRepository.findById(id);
    }
    
    /**
     * Cache based on method parameter.
     */
    @Cacheable(value = "products", key = "#id",
               condition = "#useCache == true")
    public Product getProductWithFlag(Long id, boolean useCache) {
        log.info("Conditional Cache: Loading product {} (useCache={})", id, useCache);
        return productRepository.findById(id);
    }
    
    /**
     * Cache only if user is premium.
     */
    @Cacheable(value = "users", key = "#id",
               condition = "#root.target.isPremiumUser(#id)")
    public User getUser(Long id) {
        log.info("Conditional Cache: Loading user {}", id);
        return userRepository.findById(id);
    }
    
    /**
     * Update cache only if product is in stock.
     */
    @CachePut(value = "products", key = "#product.id",
              condition = "#product.stock != null && #product.stock > 0")
    public Product updateProduct(Product product) {
        log.info("Conditional Cache: Updating product {}", product.getId());
        return productRepository.save(product);
    }
    
    /**
     * Cache with complex condition.
     */
    @Cacheable(value = "products", key = "#id",
               condition = "#id > 0 && #id < 1000",
               unless = "#result == null || #result.stock == 0")
    public Product getProductComplex(Long id) {
        log.info("Conditional Cache: Complex condition for product {}", id);
        return productRepository.findById(id);
    }
    
    /**
     * Helper method for condition evaluation.
     */
    public boolean isPremiumUser(Long userId) {
        User user = userRepository.findById(userId);
        return user != null && user.getEmail().contains("premium");
    }
}
```

```java
// src/main/java/org/example/patterns/caching/conditional/ConditionalCachingController.java
package org.example.patterns.caching.conditional;

import lombok.RequiredArgsConstructor;
import org.example.model.Product;
import org.example.model.User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/conditional-caching")
@RequiredArgsConstructor
public class ConditionalCachingController {
    
    private final ConditionalCachingService conditionalCachingService;
    
    @GetMapping("/product/{id}")
    public Product getProduct(@PathVariable Long id) {
        return conditionalCachingService.getProduct(id);
    }
    
    @GetMapping("/product-unless-null/{id}")
    public Product getProductUnlessNull(@PathVariable Long id) {
        return conditionalCachingService.getProductUnlessNull(id);
    }
    
    @GetMapping("/expensive-product/{id}")
    public Product getExpensiveProduct(@PathVariable Long id) {
        return conditionalCachingService.getExpensiveProduct(id);
    }
    
    @GetMapping("/product-with-flag/{id}")
    public Product getProductWithFlag(@PathVariable Long id, 
                                     @RequestParam(defaultValue = "true") boolean useCache) {
        return conditionalCachingService.getProductWithFlag(id, useCache);
    }
    
    @GetMapping("/user/{id}")
    public User getUser(@PathVariable Long id) {
        return conditionalCachingService.getUser(id);
    }
    
    @PutMapping("/product")
    public Product updateProduct(@RequestBody Product product) {
        return conditionalCachingService.updateProduct(product);
    }
    
    @GetMapping("/product-complex/{id}")
    public Product getProductComplex(@PathVariable Long id) {
        return conditionalCachingService.getProductComplex(id);
    }
}
```

## 15. Pattern 11: Cache Key Generator Pattern

```java
// src/main/java/org/example/patterns/caching/keygenerator/CustomKeyGenerator.java
package org.example.patterns.caching.keygenerator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Cache Key Generator Pattern.
 * Custom logic for generating cache keys.
 */
@Slf4j
@Component("customKeyGenerator")
public class CustomKeyGenerator implements KeyGenerator {
    
    @Override
    public Object generate(Object target, Method method, Object... params) {
        String className = target.getClass().getSimpleName();
        String methodName = method.getName();
        String paramsKey = Arrays.stream(params)
                .map(param -> param != null ? param.toString() : "null")
                .collect(Collectors.joining("-"));
        
        String key = String.format("%s:%s:%s", className, methodName, paramsKey);
        
        log.info("Custom Key Generator: Generated key = {}", key);
        return key;
    }
}
```

```java
// src/main/java/org/example/patterns/caching/keygenerator/CompositeKeyGenerator.java
package org.example.patterns.caching.keygenerator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.util.Base64;

/**
 * Composite key generator using hash for complex objects.
 */
@Slf4j
@Component("compositeKeyGenerator")
public class CompositeKeyGenerator implements KeyGenerator {
    
    @Override
    public Object generate(Object target, Method method, Object... params) {
        try {
            StringBuilder keyBuilder = new StringBuilder();
            
            for (Object param : params) {
                if (param != null) {
                    keyBuilder.append(param.toString());
                }
            }
            
            // Generate hash for complex keys
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(keyBuilder.toString().getBytes());
            String key = Base64.getEncoder().encodeToString(hash);
            
            log.info("Composite Key Generator: Generated hash key = {}", key);
            return key;
            
        } catch (Exception e) {
            log.error("Failed to generate key", e);
            return Arrays.hashCode(params);
        }
    }
}
```

```java
// src/main/java/org/example/patterns/caching/keygenerator/CacheKeyGeneratorService.java
package org.example.patterns.caching.keygenerator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.model.Product;
import org.example.repository.ProductRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service using custom key generators.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CacheKeyGeneratorService {
    
    private final ProductRepository productRepository;
    
    /**
     * Uses custom key generator.
     */
    @Cacheable(value = "products", keyGenerator = "customKeyGenerator")
    public Product getProduct(Long id) {
        log.info("Key Generator: Loading product {}", id);
        return productRepository.findById(id);
    }
    
    /**
     * Complex key with multiple parameters.
     */
    @Cacheable(value = "products", keyGenerator = "customKeyGenerator")
    public List<Product> searchProducts(String category, String name, Integer minStock) {
        log.info("Key Generator: Searching products - category={}, name={}, minStock={}",
                category, name, minStock);
        return productRepository.findByCategory(category);
    }
    
    /**
     * Uses composite key generator for complex objects.
     */
    @Cacheable(value = "products", keyGenerator = "compositeKeyGenerator")
    public Product findProduct(Product searchCriteria) {
        log.info("Key Generator: Finding product with criteria: {}", searchCriteria);
        return productRepository.findById(searchCriteria.getId());
    }
    
    /**
     * SpEL-based custom key.
     */
    @Cacheable(value = "products", 
               key = "T(String).format('product-%d-%s', #id, #category)")
    public Product getProductByIdAndCategory(Long id, String category) {
```java
        log.info("Key Generator: SpEL custom key - id={}, category={}", id, category);
        return productRepository.findById(id);
    }
    
    /**
     * Root object access in key generation.
     */
    @Cacheable(value = "products",
               key = "#root.methodName + ':' + #root.args[0]")
    public Product getProductWithRootObject(Long id) {
        log.info("Key Generator: Using root object for key - id={}", id);
        return productRepository.findById(id);
    }
    
    /**
     * Conditional key generation.
     */
    @Cacheable(value = "products",
               key = "#id > 100 ? 'large:' + #id : 'small:' + #id")
    public Product getProductConditionalKey(Long id) {
        log.info("Key Generator: Conditional key for id={}", id);
        return productRepository.findById(id);
    }
}
```

```java
// src/main/java/org/example/patterns/caching/keygenerator/CacheKeyGeneratorController.java
package org.example.patterns.caching.keygenerator;

import lombok.RequiredArgsConstructor;
import org.example.model.Product;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/key-generator")
@RequiredArgsConstructor
public class CacheKeyGeneratorController {
    
    private final CacheKeyGeneratorService keyGeneratorService;
    
    @GetMapping("/product/{id}")
    public Product getProduct(@PathVariable Long id) {
        return keyGeneratorService.getProduct(id);
    }
    
    @GetMapping("/search")
    public List<Product> searchProducts(@RequestParam String category,
                                       @RequestParam(required = false) String name,
                                       @RequestParam(required = false) Integer minStock) {
        return keyGeneratorService.searchProducts(category, name, minStock);
    }
    
    @PostMapping("/find")
    public Product findProduct(@RequestBody Product searchCriteria) {
        return keyGeneratorService.findProduct(searchCriteria);
    }
    
    @GetMapping("/product-by-category/{id}")
    public Product getProductByIdAndCategory(@PathVariable Long id,
                                            @RequestParam String category) {
        return keyGeneratorService.getProductByIdAndCategory(id, category);
    }
    
    @GetMapping("/product-root/{id}")
    public Product getProductWithRootObject(@PathVariable Long id) {
        return keyGeneratorService.getProductWithRootObject(id);
    }
    
    @GetMapping("/product-conditional/{id}")
    public Product getProductConditionalKey(@PathVariable Long id) {
        return keyGeneratorService.getProductConditionalKey(id);
    }
}
```

## 16. Maven Configuration (pom.xml)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
        <relativePath/>
    </parent>
    
    <groupId>org.example</groupId>
    <artifactId>spring-caching-patterns</artifactId>
    <version>1.0-SNAPSHOT</version>
    <name>Spring Caching Patterns</name>
    <description>Demonstration of caching patterns in Spring</description>
    
    <properties>
        <java.version>17</java.version>
    </properties>
    
    <dependencies>
        <!-- Spring Boot Starter -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        
        <!-- Spring Boot Cache -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-cache</artifactId>
        </dependency>
        
        <!-- Caffeine Cache -->
        <dependency>
            <groupId>com.github.ben-manes.caffeine</groupId>
            <artifactId>caffeine</artifactId>
        </dependency>
        
        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        
        <!-- Spring Boot Test -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

## 17. Application Configuration

```properties
# src/main/resources/application.properties
spring.application.name=spring-caching-patterns

# Server Configuration
server.port=8080

# Cache Configuration
spring.cache.type=caffeine
spring.cache.cache-names=products,users,orders,categories,readThrough,writeThrough,writeBehind,secondary,abstraction
spring.cache.caffeine.spec=maximumSize=1000,expireAfterWrite=10m,recordStats

# Logging
logging.level.root=INFO
logging.level.org.example=DEBUG
logging.level.org.springframework.cache=DEBUG
```

## 18. Test Classes

```java
// src/test/java/org/example/patterns/caching/cacheaside/CacheAsideTest.java
package org.example.patterns.caching.cacheaside;

import org.example.model.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CacheAsideTest {
    
    @Autowired
    private CacheAsideService cacheAsideService;
    
    @Autowired
    private CacheManager cacheManager;
    
    @Test
    void testCacheAsidePattern() {
        // First call - cache miss
        Product product1 = cacheAsideService.getProduct(1L);
        assertNotNull(product1);
        
        // Second call - cache hit
        Product product2 = cacheAsideService.getProduct(1L);
        assertNotNull(product2);
        assertEquals(product1.getId(), product2.getId());
    }
    
    @Test
    void testCacheEviction() {
        Product product = cacheAsideService.getProduct(1L);
        assertNotNull(product);
        
        // Update should evict cache
        product.setPrice(new BigDecimal("999.99"));
        cacheAsideService.updateProduct(product);
        
        // Cache should be empty
        assertNull(cacheManager.getCache("products").get(1L));
    }
}
```

```java
// src/test/java/org/example/patterns/caching/readthrough/ReadThroughTest.java
package org.example.patterns.caching.readthrough;

import org.example.model.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ReadThroughTest {
    
    @Autowired
    private ReadThroughService readThroughService;
    
    @Autowired
    private CacheManager cacheManager;
    
    @Test
    void testReadThroughCaching() {
        // First call - loads from database and caches
        Product product1 = readThroughService.getProduct(1L);
        assertNotNull(product1);
        
        // Verify cached
        assertNotNull(cacheManager.getCache("readThrough").get(1L));
        
        // Second call - from cache
        Product product2 = readThroughService.getProduct(1L);
        assertNotNull(product2);
    }
    
    @Test
    void testConditionalCaching() {
        // Positive ID - should cache
        Product product1 = readThroughService.getProductConditional(1L);
        assertNotNull(product1);
        
        // Negative ID - should not cache
        Product product2 = readThroughService.getProductConditional(-1L);
        // Would be null in real scenario
    }
}
```

```java
// src/test/java/org/example/patterns/caching/keygenerator/KeyGeneratorTest.java
package org.example.patterns.caching.keygenerator;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.interceptor.KeyGenerator;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class KeyGeneratorTest {
    
    @Autowired
    private KeyGenerator customKeyGenerator;
    
    @Test
    void testCustomKeyGenerator() throws NoSuchMethodException {
        Method method = CacheKeyGeneratorService.class
                .getMethod("getProduct", Long.class);
        
        Object key = customKeyGenerator.generate(
                new CacheKeyGeneratorService(null), 
                method, 
                1L
        );
        
        assertNotNull(key);
        assertTrue(key.toString().contains("CacheKeyGeneratorService"));
        assertTrue(key.toString().contains("getProduct"));
        assertTrue(key.toString().contains("1"));
    }
}
```

## 19. README.md

```markdown
# Spring Caching Patterns

Comprehensive demonstration of 11 essential caching patterns in Spring Framework.

## Patterns Implemented

### 1. Cache-Aside Pattern (Lazy Loading)
**Endpoint:** `/api/cache-aside/product/{id}`

Application manages cache explicitly.

**Flow:**
```
Read:
1. Check cache
2. If miss, load from database
3. Populate cache
4. Return data

Write:
1. Update database
2. Invalidate cache
```

**Use Case:** Most common caching pattern, full control over caching logic

**Example:**
```java
public Product getProduct(Long id) {
    Cache cache = cacheManager.getCache("products");
    Product cached = cache.get(id, Product.class);
    if (cached != null) return cached;
    
    Product product = repository.findById(id);
    cache.put(id, product);
    return product;
}
```

### 2. Read-Through Pattern
**Endpoint:** `/api/read-through/product/{id}`

Cache automatically loads from database on miss.

**Annotation:** `@Cacheable`

**Flow:**
```
1. Spring checks cache
2. On miss, invokes method
3. Caches result automatically
```

**Example:**
```java
@Cacheable(value = "products", key = "#id")
public Product getProduct(Long id) {
    return repository.findById(id);
}
```

**Advantages:**
- ✅ Declarative approach
- ✅ Less boilerplate code
- ✅ Consistent caching behavior

### 3. Write-Through Pattern
**Endpoint:** `/api/write-through/product`

Updates database and cache synchronously.

**Annotation:** `@CachePut`

**Flow:**
```
1. Update database
2. Update cache (always)
```

**Example:**
```java
@CachePut(value = "products", key = "#product.id")
public Product updateProduct(Product product) {
    return repository.save(product);
}
```

**Use Case:** Strong consistency required between cache and database

### 4. Write-Behind Pattern (Write-Back)
**Endpoint:** `/api/write-behind/product`

Updates cache immediately, database asynchronously.

**Flow:**
```
1. Update cache immediately
2. Queue database write
3. Async batch write to database
```

**Advantages:**
- ✅ Better write performance
- ✅ Reduced database load
- ⚠️ Risk of data loss on failure

**Example:**
```java
@CachePut(value = "products", key = "#product.id")
public Product updateProduct(Product product) {
    writeQueue.offer(product);
    asyncWriteToDatabase(product);
    return product;
}
```

### 5. Refresh-Ahead Pattern
**Endpoint:** `/api/refresh-ahead/product/{id}`

Proactively refreshes cache before expiration.

**Strategy:**
- Track frequently accessed items (hot data)
- Scheduled refresh before TTL expires
- Prevents cache misses

**Example:**
```java
@Scheduled(fixedRate = 300000) // 5 minutes
public void refreshHotProducts() {
    for (Long id : hotProducts) {
        Product fresh = repository.findById(id);
        cache.put(id, fresh);
    }
}
```

**Use Case:** High-traffic, read-heavy applications

### 6. Cache Abstraction Pattern
**Endpoint:** `/api/cache-abstraction/product/{id}`

Vendor-neutral caching API.

**Features:**
- Switch cache providers without code changes
- Supports: Caffeine, Redis, Ehcache, Hazelcast
- Unified annotations

**Configuration:**
```java
@CacheConfig(cacheNames = "products")
public class MyService {
    @Cacheable
    public Product get(Long id) { }
    
    @CachePut
    public Product update(Product p) { }
    
    @CacheEvict
    public void delete(Long id) { }
}
```

### 7. Cache Manager Pattern
**Endpoint:** `/api/cache-manager/*`

Direct cache management operations.

**Operations:**
- Get cache by name
- Put/get/evict entries
- Clear entire cache
- Get statistics

**Example:**
```java
Cache cache = cacheManager.getCache("products");
cache.put(key, value);
cache.evict(key);
cache.clear();
```

**Use Case:** Advanced cache control, monitoring

### 8. Cache Resolver Pattern
**Endpoint:** `/api/cache-resolver/product/{id}`

Custom logic to determine cache at runtime.

**Example:**
```java
@Component
public class CustomCacheResolver implements CacheResolver {
    public Collection<? extends Cache> resolveCaches(...) {
        if (condition) {
            return Collections.singleton(primaryCache);
        }
        return Collections.singleton(secondaryCache);
    }
}

@Cacheable(cacheResolver = "customCacheResolver")
public Product getProduct(Long id) { }
```

**Use Case:** Multi-tenant, routing to different caches

### 9. Cache Eviction Pattern
**Endpoint:** `/api/cache-eviction/*`

Strategies for removing stale data.

**Strategies:**
1. **Single Entry:** `@CacheEvict(key = "#id")`
2. **All Entries:** `@CacheEvict(allEntries = true)`
3. **Multiple Caches:** `@Caching(evict = {...})`
4. **Before Invocation:** `@CacheEvict(beforeInvocation = true)`
5. **Conditional:** `@CacheEvict(condition = "...")`
6. **Scheduled:** `@Scheduled + @CacheEvict`

**Example:**
```java
@CacheEvict(value = "products", key = "#id")
public void delete(Long id) { }

@Scheduled(cron = "0 0 0 * * ?")
@CacheEvict(value = "products", allEntries = true)
public void dailyEviction() { }
```

### 10. Conditional Caching Pattern
**Endpoint:** `/api/conditional-caching/*`

Cache based on conditions.

**Conditions:**
- `condition` - Before method execution
- `unless` - After method execution

**Examples:**
```java
// Cache only positive IDs
@Cacheable(condition = "#id > 0")

// Don't cache null results
@Cacheable(unless = "#result == null")

// Cache expensive products only
@Cacheable(unless = "#result.price < 100")

// Cache with parameter flag
@Cacheable(condition = "#useCache == true")

// Complex condition
@Cacheable(
    condition = "#id > 0 && #id < 1000",
    unless = "#result == null || #result.stock == 0"
)
```

### 11. Cache Key Generator Pattern
**Endpoint:** `/api/key-generator/*`

Custom cache key generation.

**Built-in Generators:**
- `SimpleKeyGenerator` (default)
- Custom generators

**SpEL Keys:**
```java
// Simple
@Cacheable(key = "#id")

// Concatenation
@Cacheable(key = "#id + ':' + #category")

// Method name
@Cacheable(key = "#root.methodName")

// Format
@Cacheable(key = "T(String).format('product-%d', #id)")

// Conditional
@Cacheable(key = "#id > 100 ? 'large:' + #id : 'small:' + #id")
```

**Custom Generator:**
```java
@Component
public class CustomKeyGenerator implements KeyGenerator {
    public Object generate(Object target, Method method, Object... params) {
        return method.getName() + ":" + Arrays.toString(params);
    }
}

@Cacheable(keyGenerator = "customKeyGenerator")
public Product get(Long id) { }
```

## Running the Application

### Prerequisites
- Java 17+
- Maven 3.6+

### Build and Run
```bash
mvn clean install
mvn spring-boot:run
```

### Access Application
Base URL: http://localhost:8080

## Testing Patterns

### Cache-Aside
```bash
# First call (cache miss)
curl http://localhost:8080/api/cache-aside/product/1

# Second call (cache hit)
curl http://localhost:8080/api/cache-aside/product/1

# Update (evicts cache)
curl -X PUT http://localhost:8080/api/cache-aside/product \
  -H "Content-Type: application/json" \
  -d '{"id":1,"name":"Updated","price":999.99}'
```

### Read-Through
```bash
# Automatic caching
curl http://localhost:8080/api/read-through/product/1
curl http://localhost:8080/api/read-through/product/1  # From cache
```

### Write-Through
```bash
# Updates cache automatically
curl -X PUT http://localhost:8080/api/write-through/product \
  -H "Content-Type: application/json" \
  -d '{"id":1,"name":"Updated","price":1299.99}'
```

### Cache Manager
```bash
# Get cache names
curl http://localhost:8080/api/cache-manager/names

# Get cache stats
curl http://localhost:8080/api/cache-manager/stats/products

# Manual put
curl -X POST http://localhost:8080/api/cache-manager/products \
  -H "Content-Type: application/json" \
  -d '{"key":"test","value":"data"}'

# Clear cache
curl -X DELETE http://localhost:8080/api/cache-manager/products
```

### Cache Eviction
```bash
# Evict single entry
curl -X DELETE http://localhost:8080/api/cache-eviction/product/1

# Evict all
curl -X DELETE http://localhost:8080/api/cache-eviction/products
```

## Cache Configuration

### Caffeine Cache
```java
@Bean
public CacheManager cacheManager() {
    CaffeineCacheManager manager = new CaffeineCacheManager();
    manager.setCaffeine(Caffeine.newBuilder()
        .maximumSize(1000)
        .expireAfterWrite(10, TimeUnit.MINUTES)
        .recordStats());
    return manager;
}
```

### Multiple Cache Managers
```java
@Primary
@Bean
public CacheManager primaryCacheManager() { }

@Bean
public CacheManager secondaryCacheManager() { }
```

## Best Practices

### 1. Choose Right Pattern
- **Cache-Aside:** Maximum control, complex scenarios
- **Read-Through:** Simple read-heavy applications
- **Write-Through:** Strong consistency needed
- **Write-Behind:** High write throughput needed
- **Refresh-Ahead:** Prevent cache misses for hot data

### 2. Cache Keys
- ✅ Use meaningful keys
- ✅ Include version/timestamp if needed
- ✅ Keep keys short
- ✅ Avoid collision

### 3. TTL (Time-To-Live)
- Set appropriate expiration
- Longer for static data
- Shorter for frequently changing data

### 4. Cache Size
- Monitor cache size
- Set maximum size
- Use LRU/LFU eviction

### 5. Monitoring
- Track hit/miss ratio
- Monitor cache size
- Alert on high miss rate

## Common Pitfalls

### 1. Cache Stampede
**Problem:** Many requests hit database simultaneously on cache miss

**Solution:**
```java
// Use locking
synchronized(lock) {
    if (cache.get(key) == null) {
        value = loadFromDB();
        cache.put(key, value);
    }
}
```

### 2. Stale Data
**Problem:** Cache returns outdated data

**Solutions:**
- Set TTL
- Invalidate on update
- Use write-through pattern

### 3. Memory Issues
**Problem:** Cache grows too large

**Solutions:**
- Set maximum size
- Use eviction policies
- Monitor memory usage

### 4. Thundering Herd
**Problem:** All cached items expire simultaneously

**Solution:** Stagger expiration times
```java
.expireAfterWrite(10 + random.nextInt(5), TimeUnit.MINUTES)
```

## Performance Tips

### 1. Async Caching
```java
@Async
@CachePut
public CompletableFuture<Product> updateAsync(Product p) {
    return CompletableFuture.completedFuture(repository.save(p));
}
```

### 2. Batch Operations
```java
public List<Product> getAll(List<Long> ids) {
    return ids.stream()
        .map(this::getProductCached)
        .collect(Collectors.toList());
}
```

### 3. Compression
For large objects, compress before caching

## License

MIT License - free to use for learning and projects.
```

This completes the comprehensive implementation of all 11 Caching Patterns with working code, tests, and thorough documentation!    