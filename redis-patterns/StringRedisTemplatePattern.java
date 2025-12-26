package com.example.redis.stringtemplate;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * String Redis Template Pattern
 * 
 * Demonstrates the use of StringRedisTemplate for String-based Redis operations.
 * StringRedisTemplate is a specialized version of RedisTemplate that uses StringRedisSerializer
 * for both keys and values, making it ideal for:
 * - Simple string storage and retrieval
 * - Counter operations
 * - Bit operations
 * - JSON string storage
 * - Interoperability with other Redis clients
 * 
 * Use cases:
 * - Configuration management
 * - Feature flags
 * - Rate limiting counters
 * - Simple caching
 * - Session tokens
 * - API keys storage
 */

@Configuration
class StringRedisTemplateConfig {
    
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }
}

@Service
class StringRedisService {
    
    private final StringRedisTemplate stringRedisTemplate;
    
    public StringRedisService(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }
    
    // String Operations
    public void setValue(String key, String value) {
        stringRedisTemplate.opsForValue().set(key, value);
    }
    
    public void setValueWithExpiry(String key, String value, long timeout, TimeUnit unit) {
        stringRedisTemplate.opsForValue().set(key, value, timeout, unit);
    }
    
    public String getValue(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }
    
    public boolean setIfAbsent(String key, String value, Duration duration) {
        return Boolean.TRUE.equals(
            stringRedisTemplate.opsForValue().setIfAbsent(key, value, duration)
        );
    }
    
    public boolean setIfPresent(String key, String value) {
        return Boolean.TRUE.equals(
            stringRedisTemplate.opsForValue().setIfPresent(key, value)
        );
    }
    
    public String getAndSet(String key, String value) {
        return stringRedisTemplate.opsForValue().getAndSet(key, value);
    }
    
    public String getAndDelete(String key) {
        return stringRedisTemplate.opsForValue().getAndDelete(key);
    }
    
    // Counter Operations
    public long increment(String key) {
        return stringRedisTemplate.opsForValue().increment(key);
    }
    
    public long incrementBy(String key, long delta) {
        return stringRedisTemplate.opsForValue().increment(key, delta);
    }
    
    public double incrementByFloat(String key, double delta) {
        return stringRedisTemplate.opsForValue().increment(key, delta);
    }
    
    public long decrement(String key) {
        return stringRedisTemplate.opsForValue().decrement(key);
    }
    
    public long decrementBy(String key, long delta) {
        return stringRedisTemplate.opsForValue().decrement(key, delta);
    }
    
    // Bit Operations
    public boolean setBit(String key, long offset, boolean value) {
        return Boolean.TRUE.equals(stringRedisTemplate.opsForValue().setBit(key, offset, value));
    }
    
    public boolean getBit(String key, long offset) {
        return Boolean.TRUE.equals(stringRedisTemplate.opsForValue().getBit(key, offset));
    }
    
    public long bitCount(String key) {
        return stringRedisTemplate.execute(connection -> 
            connection.bitCount(key.getBytes())
        );
    }
    
    // Hash Operations
    public void setHashValue(String key, String field, String value) {
        stringRedisTemplate.opsForHash().put(key, field, value);
    }
    
    public String getHashValue(String key, String field) {
        return (String) stringRedisTemplate.opsForHash().get(key, field);
    }
    
    public void setMultipleHashValues(String key, Map<String, String> values) {
        stringRedisTemplate.opsForHash().putAll(key, values);
    }
    
    public Map<Object, Object> getAllHashValues(String key) {
        return stringRedisTemplate.opsForHash().entries(key);
    }
    
    public boolean hashFieldExists(String key, String field) {
        return stringRedisTemplate.opsForHash().hasKey(key, field);
    }
    
    public long deleteHashFields(String key, String... fields) {
        return stringRedisTemplate.opsForHash().delete(key, (Object[]) fields);
    }
    
    public long incrementHashField(String key, String field, long delta) {
        return stringRedisTemplate.opsForHash().increment(key, field, delta);
    }
    
    // List Operations
    public long leftPush(String key, String value) {
        return stringRedisTemplate.opsForList().leftPush(key, value);
    }
    
    public long rightPush(String key, String value) {
        return stringRedisTemplate.opsForList().rightPush(key, value);
    }
    
    public String leftPop(String key) {
        return stringRedisTemplate.opsForList().leftPop(key);
    }
    
    public String rightPop(String key) {
        return stringRedisTemplate.opsForList().rightPop(key);
    }
    
    public List<String> getListRange(String key, long start, long end) {
        return stringRedisTemplate.opsForList().range(key, start, end);
    }
    
    public long getListSize(String key) {
        Long size = stringRedisTemplate.opsForList().size(key);
        return size != null ? size : 0;
    }
    
    public void trimList(String key, long start, long end) {
        stringRedisTemplate.opsForList().trim(key, start, end);
    }
    
    // Set Operations
    public long addToSet(String key, String... values) {
        return stringRedisTemplate.opsForSet().add(key, values);
    }
    
    public Set<String> getSetMembers(String key) {
        return stringRedisTemplate.opsForSet().members(key);
    }
    
    public boolean isMemberOfSet(String key, String value) {
        return Boolean.TRUE.equals(stringRedisTemplate.opsForSet().isMember(key, value));
    }
    
    public long removeFromSet(String key, String... values) {
        return stringRedisTemplate.opsForSet().remove(key, (Object[]) values);
    }
    
    public long getSetSize(String key) {
        Long size = stringRedisTemplate.opsForSet().size(key);
        return size != null ? size : 0;
    }
    
    public String popFromSet(String key) {
        return stringRedisTemplate.opsForSet().pop(key);
    }
    
    // Sorted Set Operations
    public boolean addToSortedSet(String key, String value, double score) {
        return Boolean.TRUE.equals(stringRedisTemplate.opsForZSet().add(key, value, score));
    }
    
    public Set<String> getSortedSetRange(String key, long start, long end) {
        return stringRedisTemplate.opsForZSet().range(key, start, end);
    }
    
    public Set<String> getSortedSetReverseRange(String key, long start, long end) {
        return stringRedisTemplate.opsForZSet().reverseRange(key, start, end);
    }
    
    public Set<String> getSortedSetByScore(String key, double min, double max) {
        return stringRedisTemplate.opsForZSet().rangeByScore(key, min, max);
    }
    
    public Double getSortedSetScore(String key, String value) {
        return stringRedisTemplate.opsForZSet().score(key, value);
    }
    
    public Long getSortedSetRank(String key, String value) {
        return stringRedisTemplate.opsForZSet().rank(key, value);
    }
    
    public double incrementSortedSetScore(String key, String value, double delta) {
        return stringRedisTemplate.opsForZSet().incrementScore(key, value, delta);
    }
    
    // Key Operations
    public boolean delete(String key) {
        return Boolean.TRUE.equals(stringRedisTemplate.delete(key));
    }
    
    public long delete(Set<String> keys) {
        return stringRedisTemplate.delete(keys);
    }
    
    public boolean exists(String key) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
    }
    
    public boolean expire(String key, Duration duration) {
        return Boolean.TRUE.equals(stringRedisTemplate.expire(key, duration));
    }
    
    public long getTimeToLive(String key) {
        Long ttl = stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);
        return ttl != null ? ttl : -1;
    }
    
    public Set<String> keys(String pattern) {
        return stringRedisTemplate.keys(pattern);
    }
}

@RestController
@RequestMapping("/api/redis/string")
class StringRedisController {
    
    private final StringRedisService redisService;
    
    public StringRedisController(StringRedisService redisService) {
        this.redisService = redisService;
    }
    
    @PostMapping("/set")
    public String setValue(@RequestParam String key, @RequestParam String value) {
        redisService.setValue(key, value);
        return "Value set successfully";
    }
    
    @PostMapping("/set/expiry")
    public String setValueWithExpiry(@RequestParam String key, 
                                     @RequestParam String value,
                                     @RequestParam long timeout,
                                     @RequestParam(defaultValue = "SECONDS") TimeUnit unit) {
        redisService.setValueWithExpiry(key, value, timeout, unit);
        return "Value set with expiry";
    }
    
    @GetMapping("/get")
    public String getValue(@RequestParam String key) {
        return redisService.getValue(key);
    }
    
    @PostMapping("/increment")
    public long increment(@RequestParam String key, @RequestParam(defaultValue = "1") long delta) {
        return delta == 1 ? redisService.increment(key) : redisService.incrementBy(key, delta);
    }
    
    @PostMapping("/decrement")
    public long decrement(@RequestParam String key, @RequestParam(defaultValue = "1") long delta) {
        return delta == 1 ? redisService.decrement(key) : redisService.decrementBy(key, delta);
    }
    
    @PostMapping("/hash/set")
    public String setHashValue(@RequestParam String key, 
                              @RequestParam String field, 
                              @RequestParam String value) {
        redisService.setHashValue(key, field, value);
        return "Hash field set successfully";
    }
    
    @GetMapping("/hash/get")
    public String getHashValue(@RequestParam String key, @RequestParam String field) {
        return redisService.getHashValue(key, field);
    }
    
    @GetMapping("/hash/all")
    public Map<Object, Object> getAllHashValues(@RequestParam String key) {
        return redisService.getAllHashValues(key);
    }
    
    @PostMapping("/list/lpush")
    public long leftPush(@RequestParam String key, @RequestParam String value) {
        return redisService.leftPush(key, value);
    }
    
    @PostMapping("/list/rpush")
    public long rightPush(@RequestParam String key, @RequestParam String value) {
        return redisService.rightPush(key, value);
    }
    
    @GetMapping("/list/range")
    public List<String> getListRange(@RequestParam String key, 
                                     @RequestParam(defaultValue = "0") long start,
                                     @RequestParam(defaultValue = "-1") long end) {
        return redisService.getListRange(key, start, end);
    }
    
    @PostMapping("/set/add")
    public long addToSet(@RequestParam String key, @RequestParam String... values) {
        return redisService.addToSet(key, values);
    }
    
    @GetMapping("/set/members")
    public Set<String> getSetMembers(@RequestParam String key) {
        return redisService.getSetMembers(key);
    }
    
    @PostMapping("/zset/add")
    public boolean addToSortedSet(@RequestParam String key, 
                                 @RequestParam String value,
                                 @RequestParam double score) {
        return redisService.addToSortedSet(key, value, score);
    }
    
    @GetMapping("/zset/range")
    public Set<String> getSortedSetRange(@RequestParam String key,
                                        @RequestParam(defaultValue = "0") long start,
                                        @RequestParam(defaultValue = "-1") long end) {
        return redisService.getSortedSetRange(key, start, end);
    }
    
    @GetMapping("/zset/top")
    public Set<String> getTopScores(@RequestParam String key, @RequestParam int count) {
        return redisService.getSortedSetReverseRange(key, 0, count - 1);
    }
    
    @DeleteMapping("/delete")
    public boolean delete(@RequestParam String key) {
        return redisService.delete(key);
    }
    
    @GetMapping("/exists")
    public boolean exists(@RequestParam String key) {
        return redisService.exists(key);
    }
    
    @GetMapping("/ttl")
    public long getTimeToLive(@RequestParam String key) {
        return redisService.getTimeToLive(key);
    }
    
    @GetMapping("/keys")
    public Set<String> keys(@RequestParam String pattern) {
        return redisService.keys(pattern);
    }
    
    @GetMapping("/info")
    public String getInfo() {
        return """
                String Redis Template Pattern
                ============================
                Specialized template for String operations:
                - String get/set with expiry
                - Counter operations (increment/decrement)
                - Bit operations
                - Hash operations (field-value pairs)
                - List operations (push/pop/range)
                - Set operations (add/remove/members)
                - Sorted Set operations (scoring/ranking)
                - Key management (delete/expire/ttl)
                - Pattern-based key queries
                """;
    }
}
