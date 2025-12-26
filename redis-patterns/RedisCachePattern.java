package com.example.redis.cache;

import org.springframework.cache.annotation.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Redis Cache Pattern
 * 
 * Demonstrates Spring Cache abstraction with Redis as the cache provider.
 * Redis Cache provides:
 * - Declarative caching with annotations
 * - Automatic cache population
 * - Cache eviction strategies
 * - TTL (Time-To-Live) configuration
 * - Multiple cache configurations
 * - Conditional caching
 * 
 * Use cases:
 * - Database query result caching
 * - API response caching
 * - Expensive computation caching
 * - Reducing load on backend services
 * - Improving application performance
 */

@Configuration
@EnableCaching
@EnableAspectJAutoProxy
class RedisCacheConfig {
    
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // Default cache configuration
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(10))
            .serializeKeysWith(
                RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())
            )
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer())
            )
            .disableCachingNullValues();
        
        // Per-cache configurations
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // Short-lived cache (1 minute)
        cacheConfigurations.put("products", defaultConfig.entryTtl(Duration.ofMinutes(1)));
        
        // Medium-lived cache (5 minutes)
        cacheConfigurations.put("users", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        
        // Long-lived cache (30 minutes)
        cacheConfigurations.put("categories", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        
        // Very long-lived cache (1 hour)
        cacheConfigurations.put("config", defaultConfig.entryTtl(Duration.ofHours(1)));
        
        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(defaultConfig)
            .withInitialCacheConfigurations(cacheConfigurations)
            .transactionAware()
            .build();
    }
}

record Product(String id, String name, String category, double price, int stock) {}

record User(String id, String username, String email, String firstName, String lastName) {}

@Service
class ProductService {
    
    private int dbCallCount = 0;
    
    @Cacheable(value = "products", key = "#id")
    public Product getProduct(String id) {
        dbCallCount++;
        System.out.println("Fetching product from database: " + id + " (DB call #" + dbCallCount + ")");
        simulateSlowService();
        return new Product(id, "Product " + id, "Electronics", 99.99, 100);
    }
    
    @Cacheable(value = "products", key = "#category", condition = "#category != null")
    public List<Product> getProductsByCategory(String category) {
        dbCallCount++;
        System.out.println("Fetching products by category from database: " + category);
        simulateSlowService();
        return List.of(
            new Product("1", "Product 1", category, 99.99, 100),
            new Product("2", "Product 2", category, 149.99, 50)
        );
    }
    
    @CachePut(value = "products", key = "#product.id")
    public Product updateProduct(Product product) {
        System.out.println("Updating product in database: " + product.id());
        return product;
    }
    
    @CacheEvict(value = "products", key = "#id")
    public void deleteProduct(String id) {
        System.out.println("Deleting product from database: " + id);
    }
    
    @CacheEvict(value = "products", allEntries = true)
    public void clearAllProducts() {
        System.out.println("Clearing all products from cache");
    }
    
    @Caching(
        evict = {
            @CacheEvict(value = "products", key = "#id"),
            @CacheEvict(value = "products", key = "#product.category")
        }
    )
    public void deleteProductAndEvictCategory(String id, Product product) {
        System.out.println("Deleting product and evicting category cache");
    }
    
    // Conditional caching - only cache if price > 50
    @Cacheable(value = "products", key = "#id", condition = "#result != null && #result.price() > 50")
    public Product getExpensiveProduct(String id) {
        dbCallCount++;
        System.out.println("Fetching expensive product: " + id);
        return new Product(id, "Expensive Product", "Premium", 199.99, 10);
    }
    
    // Cache with unless - don't cache if stock is 0
    @Cacheable(value = "products", key = "#id", unless = "#result.stock() == 0")
    public Product getInStockProduct(String id) {
        dbCallCount++;
        System.out.println("Fetching in-stock product: " + id);
        return new Product(id, "Product", "General", 79.99, 5);
    }
    
    public int getDbCallCount() {
        return dbCallCount;
    }
    
    public void resetDbCallCount() {
        dbCallCount = 0;
    }
    
    private void simulateSlowService() {
        try {
            Thread.sleep(1000); // Simulate 1 second database call
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

@Service
class UserService {
    
    @Cacheable(value = "users", key = "#id")
    public User getUser(String id) {
        System.out.println("Fetching user from database: " + id);
        return new User(id, "user" + id, "user" + id + "@example.com", "First", "Last");
    }
    
    @CachePut(value = "users", key = "#user.id")
    public User updateUser(User user) {
        System.out.println("Updating user in database: " + user.id());
        return user;
    }
    
    @CacheEvict(value = "users", key = "#id")
    public void deleteUser(String id) {
        System.out.println("Deleting user from database: " + id);
    }
    
    @CacheEvict(value = "users", allEntries = true)
    public void clearAllUsers() {
        System.out.println("Clearing all users from cache");
    }
}

@RestController
@RequestMapping("/api/redis/cache")
class RedisCacheController {
    
    private final ProductService productService;
    private final UserService userService;
    private final CacheManager cacheManager;
    
    public RedisCacheController(ProductService productService, 
                               UserService userService,
                               CacheManager cacheManager) {
        this.productService = productService;
        this.userService = userService;
        this.cacheManager = cacheManager;
    }
    
    @GetMapping("/products/{id}")
    public Product getProduct(@PathVariable String id) {
        return productService.getProduct(id);
    }
    
    @GetMapping("/products/category/{category}")
    public List<Product> getProductsByCategory(@PathVariable String category) {
        return productService.getProductsByCategory(category);
    }
    
    @PutMapping("/products")
    public Product updateProduct(@RequestBody Product product) {
        return productService.updateProduct(product);
    }
    
    @DeleteMapping("/products/{id}")
    public String deleteProduct(@PathVariable String id) {
        productService.deleteProduct(id);
        return "Product deleted and cache evicted";
    }
    
    @DeleteMapping("/products")
    public String clearAllProducts() {
        productService.clearAllProducts();
        return "All products cleared from cache";
    }
    
    @GetMapping("/products/{id}/expensive")
    public Product getExpensiveProduct(@PathVariable String id) {
        return productService.getExpensiveProduct(id);
    }
    
    @GetMapping("/products/{id}/instock")
    public Product getInStockProduct(@PathVariable String id) {
        return productService.getInStockProduct(id);
    }
    
    @GetMapping("/products/stats")
    public Map<String, Object> getProductStats() {
        return Map.of(
            "dbCallCount", productService.getDbCallCount(),
            "cacheNames", cacheManager.getCacheNames()
        );
    }
    
    @DeleteMapping("/products/stats")
    public String resetStats() {
        productService.resetDbCallCount();
        return "Stats reset";
    }
    
    @GetMapping("/users/{id}")
    public User getUser(@PathVariable String id) {
        return userService.getUser(id);
    }
    
    @PutMapping("/users")
    public User updateUser(@RequestBody User user) {
        return userService.updateUser(user);
    }
    
    @DeleteMapping("/users/{id}")
    public String deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        return "User deleted and cache evicted";
    }
    
    @DeleteMapping("/users")
    public String clearAllUsers() {
        userService.clearAllUsers();
        return "All users cleared from cache";
    }
    
    @GetMapping("/info")
    public String getInfo() {
        return """
                Redis Cache Pattern
                ==================
                Features:
                - Declarative caching with @Cacheable
                - Cache update with @CachePut
                - Cache eviction with @CacheEvict
                - Multiple cache configurations
                - Per-cache TTL settings
                - Conditional caching (condition, unless)
                - Multiple cache eviction with @Caching
                
                Cache Configurations:
                - products: 1 minute TTL
                - users: 5 minutes TTL
                - categories: 30 minutes TTL
                - config: 1 hour TTL
                - default: 10 minutes TTL
                
                Annotations:
                - @Cacheable: Cache method results
                - @CachePut: Update cache
                - @CacheEvict: Remove from cache
                - @Caching: Multiple cache operations
                - condition: Cache conditionally
                - unless: Skip caching conditionally
                
                Benefits:
                - Reduces database load
                - Improves response time
                - Automatic cache management
                - Consistent API across cache providers
                """;
    }
}
