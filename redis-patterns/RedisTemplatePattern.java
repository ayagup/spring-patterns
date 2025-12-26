package com.example.redis.template;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis Template Pattern
 * 
 * Demonstrates the use of RedisTemplate for generic Redis operations.
 * RedisTemplate provides a high-level abstraction for Redis interactions with:
 * - Generic type support for any object
 * - Serialization/deserialization handling
 * - Operations for all Redis data structures (String, Hash, List, Set, ZSet)
 * - Transaction support
 * - Pipeline operations
 * - Pub/Sub messaging
 * 
 * Use cases:
 * - Caching complex objects
 * - Session storage
 * - Distributed data structures
 * - Message queuing
 * - Rate limiting
 * - Leaderboards (sorted sets)
 */

@Configuration
class RedisTemplateConfig {
    
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // Use String serializer for keys
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        
        // Use JSON serializer for values
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        
        template.afterPropertiesSet();
        return template;
    }
}

record Product(String id, String name, String category, double price, int stock) {}

@Service
class RedisProductService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String KEY_PREFIX = "product:";
    
    public RedisProductService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    // String Operations
    public void saveProduct(Product product) {
        String key = KEY_PREFIX + product.id();
        redisTemplate.opsForValue().set(key, product);
    }
    
    public void saveProductWithExpiry(Product product, long timeout, TimeUnit unit) {
        String key = KEY_PREFIX + product.id();
        redisTemplate.opsForValue().set(key, product, timeout, unit);
    }
    
    public Product getProduct(String id) {
        String key = KEY_PREFIX + id;
        return (Product) redisTemplate.opsForValue().get(key);
    }
    
    public boolean deleteProduct(String id) {
        String key = KEY_PREFIX + id;
        return Boolean.TRUE.equals(redisTemplate.delete(key));
    }
    
    public boolean exists(String id) {
        String key = KEY_PREFIX + id;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
    
    public boolean setIfAbsent(String id, Product product) {
        String key = KEY_PREFIX + id;
        return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(key, product));
    }
    
    public long incrementStock(String id, int delta) {
        String key = KEY_PREFIX + id + ":stock";
        return redisTemplate.opsForValue().increment(key, delta);
    }
    
    // Hash Operations
    public void saveProductAsHash(Product product) {
        String key = KEY_PREFIX + "hash:" + product.id();
        redisTemplate.opsForHash().put(key, "name", product.name());
        redisTemplate.opsForHash().put(key, "category", product.category());
        redisTemplate.opsForHash().put(key, "price", product.price());
        redisTemplate.opsForHash().put(key, "stock", product.stock());
    }
    
    public Map<Object, Object> getProductHash(String id) {
        String key = KEY_PREFIX + "hash:" + id;
        return redisTemplate.opsForHash().entries(key);
    }
    
    public Object getHashField(String id, String field) {
        String key = KEY_PREFIX + "hash:" + id;
        return redisTemplate.opsForHash().get(key, field);
    }
    
    public void updateHashField(String id, String field, Object value) {
        String key = KEY_PREFIX + "hash:" + id;
        redisTemplate.opsForHash().put(key, field, value);
    }
    
    public boolean deleteHashField(String id, String field) {
        String key = KEY_PREFIX + "hash:" + id;
        return redisTemplate.opsForHash().delete(key, field) > 0;
    }
    
    // List Operations
    public void addToWishlist(String userId, Product product) {
        String key = "wishlist:" + userId;
        redisTemplate.opsForList().rightPush(key, product);
    }
    
    public List<Object> getWishlist(String userId) {
        String key = "wishlist:" + userId;
        return redisTemplate.opsForList().range(key, 0, -1);
    }
    
    public Product removeFromWishlist(String userId) {
        String key = "wishlist:" + userId;
        return (Product) redisTemplate.opsForList().leftPop(key);
    }
    
    public long getWishlistSize(String userId) {
        String key = "wishlist:" + userId;
        Long size = redisTemplate.opsForList().size(key);
        return size != null ? size : 0;
    }
    
    // Set Operations
    public void addToCategory(String category, String productId) {
        String key = "category:" + category;
        redisTemplate.opsForSet().add(key, productId);
    }
    
    public Set<Object> getCategoryProducts(String category) {
        String key = "category:" + category;
        return redisTemplate.opsForSet().members(key);
    }
    
    public boolean isInCategory(String category, String productId) {
        String key = "category:" + category;
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key, productId));
    }
    
    public boolean removeFromCategory(String category, String productId) {
        String key = "category:" + category;
        return redisTemplate.opsForSet().remove(key, productId) > 0;
    }
    
    // Sorted Set Operations (for leaderboards, rankings)
    public void addToLeaderboard(String productId, double score) {
        String key = "leaderboard:sales";
        redisTemplate.opsForZSet().add(key, productId, score);
    }
    
    public Set<Object> getTopProducts(int count) {
        String key = "leaderboard:sales";
        return redisTemplate.opsForZSet().reverseRange(key, 0, count - 1);
    }
    
    public Double getProductScore(String productId) {
        String key = "leaderboard:sales";
        return redisTemplate.opsForZSet().score(key, productId);
    }
    
    public Long getProductRank(String productId) {
        String key = "leaderboard:sales";
        return redisTemplate.opsForZSet().reverseRank(key, productId);
    }
    
    // Expiration Operations
    public boolean setExpire(String id, Duration duration) {
        String key = KEY_PREFIX + id;
        return Boolean.TRUE.equals(redisTemplate.expire(key, duration));
    }
    
    public long getTimeToLive(String id) {
        String key = KEY_PREFIX + id;
        Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        return ttl != null ? ttl : -1;
    }
    
    // Utility Operations
    public Set<String> getKeys(String pattern) {
        return redisTemplate.keys(pattern);
    }
    
    public long deleteKeys(String pattern) {
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            return redisTemplate.delete(keys);
        }
        return 0;
    }
}

@RestController
@RequestMapping("/api/redis/template")
class RedisTemplateController {
    
    private final RedisProductService productService;
    
    public RedisTemplateController(RedisProductService productService) {
        this.productService = productService;
    }
    
    @PostMapping("/products")
    public String saveProduct(@RequestBody Product product) {
        productService.saveProduct(product);
        return "Product saved: " + product.id();
    }
    
    @PostMapping("/products/expiry")
    public String saveProductWithExpiry(@RequestBody Product product, 
                                       @RequestParam long timeout,
                                       @RequestParam(defaultValue = "SECONDS") TimeUnit unit) {
        productService.saveProductWithExpiry(product, timeout, unit);
        return "Product saved with expiry: " + product.id();
    }
    
    @GetMapping("/products/{id}")
    public Product getProduct(@PathVariable String id) {
        return productService.getProduct(id);
    }
    
    @DeleteMapping("/products/{id}")
    public String deleteProduct(@PathVariable String id) {
        boolean deleted = productService.deleteProduct(id);
        return deleted ? "Product deleted" : "Product not found";
    }
    
    @GetMapping("/products/{id}/exists")
    public boolean exists(@PathVariable String id) {
        return productService.exists(id);
    }
    
    @PostMapping("/products/{id}/increment")
    public long incrementStock(@PathVariable String id, @RequestParam int delta) {
        return productService.incrementStock(id, delta);
    }
    
    @PostMapping("/products/hash")
    public String saveProductAsHash(@RequestBody Product product) {
        productService.saveProductAsHash(product);
        return "Product saved as hash: " + product.id();
    }
    
    @GetMapping("/products/hash/{id}")
    public Map<Object, Object> getProductHash(@PathVariable String id) {
        return productService.getProductHash(id);
    }
    
    @PostMapping("/wishlist/{userId}")
    public String addToWishlist(@PathVariable String userId, @RequestBody Product product) {
        productService.addToWishlist(userId, product);
        return "Added to wishlist";
    }
    
    @GetMapping("/wishlist/{userId}")
    public List<Object> getWishlist(@PathVariable String userId) {
        return productService.getWishlist(userId);
    }
    
    @PostMapping("/category/{category}/products/{productId}")
    public String addToCategory(@PathVariable String category, @PathVariable String productId) {
        productService.addToCategory(category, productId);
        return "Added to category";
    }
    
    @GetMapping("/category/{category}")
    public Set<Object> getCategoryProducts(@PathVariable String category) {
        return productService.getCategoryProducts(category);
    }
    
    @PostMapping("/leaderboard/{productId}")
    public String addToLeaderboard(@PathVariable String productId, @RequestParam double score) {
        productService.addToLeaderboard(productId, score);
        return "Added to leaderboard";
    }
    
    @GetMapping("/leaderboard/top")
    public Set<Object> getTopProducts(@RequestParam(defaultValue = "10") int count) {
        return productService.getTopProducts(count);
    }
    
    @GetMapping("/leaderboard/{productId}/rank")
    public Long getProductRank(@PathVariable String productId) {
        return productService.getProductRank(productId);
    }
    
    @GetMapping("/keys")
    public Set<String> getKeys(@RequestParam String pattern) {
        return productService.getKeys(pattern);
    }
    
    @GetMapping("/info")
    public String getInfo() {
        return """
                Redis Template Pattern
                =====================
                Supports:
                - String operations (get, set, increment)
                - Hash operations (hget, hset, hdel)
                - List operations (lpush, rpush, lpop, lrange)
                - Set operations (sadd, smembers, srem)
                - Sorted Set operations (zadd, zrange, zrank)
                - Expiration handling
                - Pattern-based key operations
                - Generic type support with JSON serialization
                """;
    }
}
