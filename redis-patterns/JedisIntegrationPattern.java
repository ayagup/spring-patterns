package com.example.redis.jedis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Transaction;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Jedis Integration Pattern
 * 
 * Demonstrates Jedis (alternative Redis client for Spring Data Redis).
 * Jedis provides:
 * - Synchronous API
 * - Simple and straightforward
 * - Connection pooling
 * - Pipelining support
 * - Transaction support
 * - Pub/Sub support
 * 
 * Use cases:
 * - Simple Redis operations
 * - Synchronous applications
 * - Legacy Spring Data Redis applications
 * - Direct Redis client access
 * - Batch operations with pipelining
 */

@Configuration
class JedisIntegrationConfig {
    
    @Bean
    public JedisConnectionFactory jedisConnectionFactory() {
        RedisStandaloneConfiguration serverConfig = new RedisStandaloneConfiguration();
        serverConfig.setHostName("localhost");
        serverConfig.setPort(6379);
        
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(20);
        poolConfig.setMaxIdle(10);
        poolConfig.setMinIdle(5);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestWhileIdle(true);
        
        JedisClientConfiguration clientConfig = JedisClientConfiguration.builder()
            .usePooling()
            .poolConfig(poolConfig)
            .connectTimeout(Duration.ofSeconds(5))
            .readTimeout(Duration.ofSeconds(5))
            .build();
        
        return new JedisConnectionFactory(serverConfig, clientConfig);
    }
    
    @Bean
    public JedisPool jedisPool() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(20);
        poolConfig.setMaxIdle(10);
        poolConfig.setMinIdle(5);
        poolConfig.setMaxWait(Duration.ofMillis(2000));
        poolConfig.setTestOnBorrow(true);
        
        return new JedisPool(poolConfig, "localhost", 6379, 5000);
    }
}

@Service
class JedisService {
    
    private final JedisPool jedisPool;
    
    public JedisService(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }
    
    // String operations
    public String set(String key, String value) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.set(key, value);
        }
    }
    
    public String get(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.get(key);
        }
    }
    
    public String setex(String key, long seconds, String value) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.setex(key, seconds, value);
        }
    }
    
    public boolean exists(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.exists(key);
        }
    }
    
    public long del(String... keys) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.del(keys);
        }
    }
    
    public long incr(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.incr(key);
        }
    }
    
    public long incrBy(String key, long increment) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.incrBy(key, increment);
        }
    }
    
    // Hash operations
    public long hset(String key, String field, String value) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hset(key, field, value);
        }
    }
    
    public String hget(String key, String field) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hget(key, field);
        }
    }
    
    public Map<String, String> hgetAll(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hgetAll(key);
        }
    }
    
    // List operations
    public long lpush(String key, String... values) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.lpush(key, values);
        }
    }
    
    public String rpop(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.rpop(key);
        }
    }
    
    public List<String> lrange(String key, long start, long stop) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.lrange(key, start, stop);
        }
    }
    
    // Set operations
    public long sadd(String key, String... members) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.sadd(key, members);
        }
    }
    
    public Set<String> smembers(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.smembers(key);
        }
    }
    
    // Sorted Set operations
    public long zadd(String key, double score, String member) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zadd(key, score, member);
        }
    }
    
    public List<String> zrange(String key, long start, long stop) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zrange(key, start, stop);
        }
    }
    
    // Pipeline operations (batch)
    public List<Object> batchOperations(Map<String, String> data) {
        try (Jedis jedis = jedisPool.getResource()) {
            Pipeline pipeline = jedis.pipelined();
            
            for (Map.Entry<String, String> entry : data.entrySet()) {
                pipeline.set(entry.getKey(), entry.getValue());
            }
            
            return pipeline.syncAndReturnAll();
        }
    }
    
    // Transaction operations
    public List<Object> transactionalOperations(String key1, String val1, String key2, String val2) {
        try (Jedis jedis = jedisPool.getResource()) {
            Transaction transaction = jedis.multi();
            
            transaction.set(key1, val1);
            transaction.set(key2, val2);
            transaction.incr("counter");
            
            return transaction.exec();
        }
    }
    
    // Pub/Sub publish
    public long publish(String channel, String message) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.publish(channel, message);
        }
    }
    
    // Connection info
    public String ping() {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.ping();
        }
    }
    
    public Map<String, String> info() {
        try (Jedis jedis = jedisPool.getResource()) {
            String info = jedis.info("server");
            return Map.of("info", info);
        }
    }
}

@RestController
@RequestMapping("/api/redis/jedis")
class JedisIntegrationController {
    
    private final JedisService jedisService;
    
    public JedisIntegrationController(JedisService jedisService) {
        this.jedisService = jedisService;
    }
    
    @PostMapping("/set")
    public String set(@RequestParam String key, @RequestParam String value) {
        return jedisService.set(key, value);
    }
    
    @GetMapping("/get/{key}")
    public String get(@PathVariable String key) {
        return jedisService.get(key);
    }
    
    @PostMapping("/setex")
    public String setex(@RequestParam String key, 
                       @RequestParam long seconds, 
                       @RequestParam String value) {
        return jedisService.setex(key, seconds, value);
    }
    
    @GetMapping("/exists/{key}")
    public boolean exists(@PathVariable String key) {
        return jedisService.exists(key);
    }
    
    @DeleteMapping("/del/{key}")
    public long del(@PathVariable String key) {
        return jedisService.del(key);
    }
    
    @PostMapping("/incr/{key}")
    public long incr(@PathVariable String key) {
        return jedisService.incr(key);
    }
    
    @PostMapping("/hash/set")
    public long hset(@RequestParam String key, 
                    @RequestParam String field, 
                    @RequestParam String value) {
        return jedisService.hset(key, field, value);
    }
    
    @GetMapping("/hash/get")
    public String hget(@RequestParam String key, @RequestParam String field) {
        return jedisService.hget(key, field);
    }
    
    @GetMapping("/hash/getall/{key}")
    public Map<String, String> hgetAll(@PathVariable String key) {
        return jedisService.hgetAll(key);
    }
    
    @PostMapping("/list/lpush")
    public long lpush(@RequestParam String key, @RequestParam String... values) {
        return jedisService.lpush(key, values);
    }
    
    @GetMapping("/list/range/{key}")
    public List<String> lrange(@PathVariable String key,
                               @RequestParam(defaultValue = "0") long start,
                               @RequestParam(defaultValue = "-1") long stop) {
        return jedisService.lrange(key, start, stop);
    }
    
    @PostMapping("/set/add")
    public long sadd(@RequestParam String key, @RequestParam String... members) {
        return jedisService.sadd(key, members);
    }
    
    @GetMapping("/set/members/{key}")
    public Set<String> smembers(@PathVariable String key) {
        return jedisService.smembers(key);
    }
    
    @PostMapping("/zset/add")
    public long zadd(@RequestParam String key, 
                    @RequestParam double score, 
                    @RequestParam String member) {
        return jedisService.zadd(key, score, member);
    }
    
    @GetMapping("/zset/range/{key}")
    public List<String> zrange(@PathVariable String key,
                               @RequestParam(defaultValue = "0") long start,
                               @RequestParam(defaultValue = "-1") long stop) {
        return jedisService.zrange(key, start, stop);
    }
    
    @PostMapping("/batch")
    public List<Object> batchOperations(@RequestBody Map<String, String> data) {
        return jedisService.batchOperations(data);
    }
    
    @PostMapping("/transaction")
    public List<Object> transactionalOperations(@RequestParam String key1,
                                               @RequestParam String val1,
                                               @RequestParam String key2,
                                               @RequestParam String val2) {
        return jedisService.transactionalOperations(key1, val1, key2, val2);
    }
    
    @PostMapping("/publish")
    public long publish(@RequestParam String channel, @RequestParam String message) {
        return jedisService.publish(channel, message);
    }
    
    @GetMapping("/ping")
    public String ping() {
        return jedisService.ping();
    }
    
    @GetMapping("/info")
    public String getInfo() {
        return """
                Jedis Integration Pattern
                ========================
                Jedis is an alternative Redis client for Spring Data Redis.
                
                Features:
                - Synchronous API only
                - Simple and straightforward
                - Connection pooling with JedisPool
                - Pipelining for batch operations
                - Transaction support
                - Pub/Sub support
                
                Operations:
                - String: set, get, setex, incr
                - Hash: hset, hget, hgetAll
                - List: lpush, rpop, lrange
                - Set: sadd, smembers
                - Sorted Set: zadd, zrange
                - Pipeline: Batch operations
                - Transaction: Multi/exec
                
                Pool Configuration:
                - maxTotal: 20 connections
                - maxIdle: 10 connections
                - minIdle: 5 connections
                - maxWait: 2000ms
                - testOnBorrow: true
                
                Comparison with Lettuce:
                - Jedis: Simpler, synchronous only
                - Lettuce: More features, reactive support
                
                Use Cases:
                - Simple synchronous applications
                - Direct Redis operations
                - Batch processing with pipelines
                - Transactional operations
                
                Note: Lettuce is recommended for new projects
                      due to better performance and features.
                """;
    }
}
