package com.example.redis.reactive;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Reactive Redis Template Pattern
 * 
 * Demonstrates the use of ReactiveRedisTemplate for non-blocking Redis operations.
 * ReactiveRedisTemplate provides:
 * - Fully reactive, non-blocking operations
 * - Backpressure support
 * - Reactive Streams integration
 * - All Redis data structures (String, Hash, List, Set, ZSet)
 * - Pipeline operations
 * - Transaction support
 * 
 * Use cases:
 * - High-throughput reactive applications
 * - WebFlux integration
 * - Real-time streaming data
 * - Non-blocking caching
 * - Reactive microservices
 */

@Configuration
class ReactiveRedisTemplateConfig {
    
    @Bean
    public ReactiveRedisTemplate<String, Object> reactiveRedisTemplate(
            ReactiveRedisConnectionFactory connectionFactory) {
        
        StringRedisSerializer keySerializer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer valueSerializer = new GenericJackson2JsonRedisSerializer();
        
        RedisSerializationContext<String, Object> serializationContext = 
            RedisSerializationContext.<String, Object>newSerializationContext()
                .key(keySerializer)
                .value(valueSerializer)
                .hashKey(keySerializer)
                .hashValue(valueSerializer)
                .build();
        
        return new ReactiveRedisTemplate<>(connectionFactory, serializationContext);
    }
}

record Message(String id, String content, String sender, long timestamp) {}

@Service
class ReactiveRedisService {
    
    private final ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;
    
    public ReactiveRedisService(ReactiveRedisTemplate<String, Object> reactiveRedisTemplate) {
        this.reactiveRedisTemplate = reactiveRedisTemplate;
    }
    
    // String Operations
    public Mono<Boolean> setValue(String key, Object value) {
        return reactiveRedisTemplate.opsForValue().set(key, value);
    }
    
    public Mono<Boolean> setValueWithExpiry(String key, Object value, Duration duration) {
        return reactiveRedisTemplate.opsForValue().set(key, value, duration);
    }
    
    public Mono<Object> getValue(String key) {
        return reactiveRedisTemplate.opsForValue().get(key);
    }
    
    public Mono<Boolean> delete(String key) {
        return reactiveRedisTemplate.delete(key).map(count -> count > 0);
    }
    
    public Mono<Boolean> exists(String key) {
        return reactiveRedisTemplate.hasKey(key);
    }
    
    public Mono<Boolean> expire(String key, Duration duration) {
        return reactiveRedisTemplate.expire(key, duration);
    }
    
    public Mono<Boolean> setIfAbsent(String key, Object value, Duration duration) {
        return reactiveRedisTemplate.opsForValue().setIfAbsent(key, value, duration);
    }
    
    public Mono<Long> increment(String key) {
        return reactiveRedisTemplate.opsForValue().increment(key);
    }
    
    public Mono<Long> incrementBy(String key, long delta) {
        return reactiveRedisTemplate.opsForValue().increment(key, delta);
    }
    
    public Mono<Double> incrementByFloat(String key, double delta) {
        return reactiveRedisTemplate.opsForValue().increment(key, delta);
    }
    
    // Hash Operations
    public Mono<Boolean> setHashValue(String key, String field, Object value) {
        return reactiveRedisTemplate.opsForHash().put(key, field, value);
    }
    
    public Mono<Object> getHashValue(String key, String field) {
        return reactiveRedisTemplate.opsForHash().get(key, field);
    }
    
    public Flux<Object> getHashValues(String key) {
        return reactiveRedisTemplate.opsForHash().values(key);
    }
    
    public Mono<Long> deleteHashField(String key, String field) {
        return reactiveRedisTemplate.opsForHash().remove(key, field);
    }
    
    public Mono<Boolean> hashFieldExists(String key, String field) {
        return reactiveRedisTemplate.opsForHash().hasKey(key, field);
    }
    
    // List Operations
    public Mono<Long> leftPush(String key, Object value) {
        return reactiveRedisTemplate.opsForList().leftPush(key, value);
    }
    
    public Mono<Long> rightPush(String key, Object value) {
        return reactiveRedisTemplate.opsForList().rightPush(key, value);
    }
    
    public Mono<Object> leftPop(String key) {
        return reactiveRedisTemplate.opsForList().leftPop(key);
    }
    
    public Mono<Object> rightPop(String key) {
        return reactiveRedisTemplate.opsForList().rightPop(key);
    }
    
    public Flux<Object> getListRange(String key, long start, long end) {
        return reactiveRedisTemplate.opsForList().range(key, start, end);
    }
    
    public Mono<Long> getListSize(String key) {
        return reactiveRedisTemplate.opsForList().size(key);
    }
    
    // Set Operations
    public Mono<Long> addToSet(String key, Object... values) {
        return reactiveRedisTemplate.opsForSet().add(key, values);
    }
    
    public Flux<Object> getSetMembers(String key) {
        return reactiveRedisTemplate.opsForSet().members(key);
    }
    
    public Mono<Boolean> isMemberOfSet(String key, Object value) {
        return reactiveRedisTemplate.opsForSet().isMember(key, value);
    }
    
    public Mono<Long> removeFromSet(String key, Object... values) {
        return reactiveRedisTemplate.opsForSet().remove(key, values);
    }
    
    public Mono<Long> getSetSize(String key) {
        return reactiveRedisTemplate.opsForSet().size(key);
    }
    
    public Mono<Object> popFromSet(String key) {
        return reactiveRedisTemplate.opsForSet().pop(key);
    }
    
    // Sorted Set Operations
    public Mono<Boolean> addToSortedSet(String key, Object value, double score) {
        return reactiveRedisTemplate.opsForZSet().add(key, value, score);
    }
    
    public Flux<Object> getSortedSetRange(String key, long start, long end) {
        return reactiveRedisTemplate.opsForZSet().range(key, org.springframework.data.domain.Range.closed(start, end));
    }
    
    public Flux<Object> getSortedSetReverseRange(String key, long start, long end) {
        return reactiveRedisTemplate.opsForZSet().reverseRange(key, org.springframework.data.domain.Range.closed(start, end));
    }
    
    public Mono<Double> getSortedSetScore(String key, Object value) {
        return reactiveRedisTemplate.opsForZSet().score(key, value);
    }
    
    public Mono<Long> getSortedSetRank(String key, Object value) {
        return reactiveRedisTemplate.opsForZSet().rank(key, value);
    }
    
    public Mono<Double> incrementSortedSetScore(String key, Object value, double delta) {
        return reactiveRedisTemplate.opsForZSet().incrementScore(key, value, delta);
    }
    
    // Key Operations
    public Flux<String> keys(String pattern) {
        return reactiveRedisTemplate.keys(pattern);
    }
    
    public Mono<Long> deleteKeys(String pattern) {
        return reactiveRedisTemplate.keys(pattern)
            .collectList()
            .flatMap(keys -> reactiveRedisTemplate.delete(keys.toArray(new String[0])));
    }
}

@RestController
@RequestMapping("/api/redis/reactive")
class ReactiveRedisController {
    
    private final ReactiveRedisService reactiveRedisService;
    
    public ReactiveRedisController(ReactiveRedisService reactiveRedisService) {
        this.reactiveRedisService = reactiveRedisService;
    }
    
    @PostMapping("/set")
    public Mono<String> setValue(@RequestParam String key, @RequestBody Message value) {
        return reactiveRedisService.setValue(key, value)
            .map(success -> success ? "Value set successfully" : "Failed to set value");
    }
    
    @PostMapping("/set/expiry")
    public Mono<String> setValueWithExpiry(@RequestParam String key, 
                                          @RequestBody Message value,
                                          @RequestParam long seconds) {
        return reactiveRedisService.setValueWithExpiry(key, value, Duration.ofSeconds(seconds))
            .map(success -> success ? "Value set with expiry" : "Failed to set value");
    }
    
    @GetMapping("/get/{key}")
    public Mono<Object> getValue(@PathVariable String key) {
        return reactiveRedisService.getValue(key);
    }
    
    @DeleteMapping("/delete/{key}")
    public Mono<String> delete(@PathVariable String key) {
        return reactiveRedisService.delete(key)
            .map(deleted -> deleted ? "Key deleted" : "Key not found");
    }
    
    @GetMapping("/exists/{key}")
    public Mono<Boolean> exists(@PathVariable String key) {
        return reactiveRedisService.exists(key);
    }
    
    @PostMapping("/increment/{key}")
    public Mono<Long> increment(@PathVariable String key, 
                               @RequestParam(defaultValue = "1") long delta) {
        return delta == 1 ? 
            reactiveRedisService.increment(key) : 
            reactiveRedisService.incrementBy(key, delta);
    }
    
    @PostMapping("/hash/set")
    public Mono<String> setHashValue(@RequestParam String key,
                                     @RequestParam String field,
                                     @RequestBody Message value) {
        return reactiveRedisService.setHashValue(key, field, value)
            .map(success -> success ? "Hash field set" : "Failed to set hash field");
    }
    
    @GetMapping("/hash/get")
    public Mono<Object> getHashValue(@RequestParam String key, @RequestParam String field) {
        return reactiveRedisService.getHashValue(key, field);
    }
    
    @GetMapping("/hash/values/{key}")
    public Flux<Object> getHashValues(@PathVariable String key) {
        return reactiveRedisService.getHashValues(key);
    }
    
    @PostMapping("/list/lpush")
    public Mono<Long> leftPush(@RequestParam String key, @RequestBody Message value) {
        return reactiveRedisService.leftPush(key, value);
    }
    
    @PostMapping("/list/rpush")
    public Mono<Long> rightPush(@RequestParam String key, @RequestBody Message value) {
        return reactiveRedisService.rightPush(key, value);
    }
    
    @GetMapping("/list/range/{key}")
    public Flux<Object> getListRange(@PathVariable String key,
                                     @RequestParam(defaultValue = "0") long start,
                                     @RequestParam(defaultValue = "-1") long end) {
        return reactiveRedisService.getListRange(key, start, end);
    }
    
    @PostMapping("/set/add")
    public Mono<Long> addToSet(@RequestParam String key, @RequestBody Message... values) {
        return reactiveRedisService.addToSet(key, (Object[]) values);
    }
    
    @GetMapping("/set/members/{key}")
    public Flux<Object> getSetMembers(@PathVariable String key) {
        return reactiveRedisService.getSetMembers(key);
    }
    
    @PostMapping("/zset/add")
    public Mono<String> addToSortedSet(@RequestParam String key,
                                       @RequestBody Message value,
                                       @RequestParam double score) {
        return reactiveRedisService.addToSortedSet(key, value, score)
            .map(success -> success ? "Added to sorted set" : "Failed to add");
    }
    
    @GetMapping("/zset/range/{key}")
    public Flux<Object> getSortedSetRange(@PathVariable String key,
                                         @RequestParam(defaultValue = "0") long start,
                                         @RequestParam(defaultValue = "-1") long end) {
        return reactiveRedisService.getSortedSetRange(key, start, end);
    }
    
    @GetMapping("/zset/top/{key}")
    public Flux<Object> getTopScores(@PathVariable String key, @RequestParam int count) {
        return reactiveRedisService.getSortedSetReverseRange(key, 0, count - 1);
    }
    
    @GetMapping("/keys")
    public Flux<String> keys(@RequestParam String pattern) {
        return reactiveRedisService.keys(pattern);
    }
    
    @GetMapping("/info")
    public Mono<String> getInfo() {
        return Mono.just("""
                Reactive Redis Template Pattern
                ===============================
                Features:
                - Fully reactive, non-blocking operations
                - Backpressure support
                - Mono<T> for single results
                - Flux<T> for multiple results
                - All Redis data structures
                - Pipeline operations support
                
                Operations:
                - String: set, get, increment, delete
                - Hash: put, get, delete, values
                - List: push, pop, range, size
                - Set: add, members, remove, size
                - Sorted Set: add, range, rank, score
                - Keys: pattern search, bulk delete
                """);
    }
}
