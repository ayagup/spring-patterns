package com.example.redis.lettuce;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.api.reactive.RedisReactiveCommands;
import io.lettuce.core.codec.StringCodec;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.RedisPubSubListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Lettuce Integration Pattern
 * 
 * Demonstrates Lettuce (the default Redis client for Spring Data Redis).
 * Lettuce provides:
 * - Synchronous, asynchronous, and reactive APIs
 * - Connection pooling
 * - Cluster support
 * - Pub/Sub support
 * - Pipelining and transactions
 * - Thread-safe connections
 * 
 * Use cases:
 * - High-performance Redis operations
 * - Non-blocking Redis access
 * - Reactive applications
 * - Clustered Redis deployments
 * - Real-time messaging
 */

@Configuration
class LettuceIntegrationConfig {
    
    @Bean
    public RedisClient redisClient() {
        RedisURI redisUri = RedisURI.builder()
            .withHost("localhost")
            .withPort(6379)
            .withTimeout(Duration.ofSeconds(5))
            .build();
        
        return RedisClient.create(redisUri);
    }
    
    @Bean
    public StatefulRedisConnection<String, String> redisConnection(RedisClient redisClient) {
        return redisClient.connect();
    }
    
    @Bean
    public RedisConnectionFactory lettuceConnectionFactory() {
        return new LettuceConnectionFactory("localhost", 6379);
    }
}

@Service
class LettuceSyncService {
    
    private final StatefulRedisConnection<String, String> connection;
    private final RedisCommands<String, String> syncCommands;
    
    public LettuceSyncService(StatefulRedisConnection<String, String> connection) {
        this.connection = connection;
        this.syncCommands = connection.sync();
    }
    
    // Synchronous operations
    public String set(String key, String value) {
        return syncCommands.set(key, value);
    }
    
    public String get(String key) {
        return syncCommands.get(key);
    }
    
    public String setex(String key, long seconds, String value) {
        return syncCommands.setex(key, seconds, value);
    }
    
    public boolean exists(String key) {
        return syncCommands.exists(key) > 0;
    }
    
    public long del(String... keys) {
        return syncCommands.del(keys);
    }
    
    public long incr(String key) {
        return syncCommands.incr(key);
    }
    
    public long incrBy(String key, long amount) {
        return syncCommands.incrby(key, amount);
    }
    
    public long lpush(String key, String... values) {
        return syncCommands.lpush(key, values);
    }
    
    public String rpop(String key) {
        return syncCommands.rpop(key);
    }
    
    public List<String> lrange(String key, long start, long stop) {
        return syncCommands.lrange(key, start, stop);
    }
    
    public long sadd(String key, String... members) {
        return syncCommands.sadd(key, members);
    }
    
    public java.util.Set<String> smembers(String key) {
        return syncCommands.smembers(key);
    }
    
    public boolean zadd(String key, double score, String member) {
        return syncCommands.zadd(key, score, member) > 0;
    }
    
    public List<String> zrange(String key, long start, long stop) {
        return syncCommands.zrange(key, start, stop);
    }
}

@Service
class LettuceAsyncService {
    
    private final StatefulRedisConnection<String, String> connection;
    private final RedisAsyncCommands<String, String> asyncCommands;
    
    public LettuceAsyncService(StatefulRedisConnection<String, String> connection) {
        this.connection = connection;
        this.asyncCommands = connection.async();
    }
    
    // Asynchronous operations return RedisFuture (CompletableFuture)
    public java.util.concurrent.CompletableFuture<String> setAsync(String key, String value) {
        return asyncCommands.set(key, value).toCompletableFuture();
    }
    
    public java.util.concurrent.CompletableFuture<String> getAsync(String key) {
        return asyncCommands.get(key).toCompletableFuture();
    }
    
    public java.util.concurrent.CompletableFuture<Long> incrAsync(String key) {
        return asyncCommands.incr(key).toCompletableFuture();
    }
    
    public java.util.concurrent.CompletableFuture<Long> delAsync(String... keys) {
        return asyncCommands.del(keys).toCompletableFuture();
    }
}

@Service
class LettuceReactiveService {
    
    private final StatefulRedisConnection<String, String> connection;
    private final RedisReactiveCommands<String, String> reactiveCommands;
    
    public LettuceReactiveService(StatefulRedisConnection<String, String> connection) {
        this.connection = connection;
        this.reactiveCommands = connection.reactive();
    }
    
    // Reactive operations return Mono/Flux
    public Mono<String> setReactive(String key, String value) {
        return reactiveCommands.set(key, value);
    }
    
    public Mono<String> getReactive(String key) {
        return reactiveCommands.get(key);
    }
    
    public Mono<Long> incrReactive(String key) {
        return reactiveCommands.incr(key);
    }
    
    public Mono<Long> delReactive(String... keys) {
        return reactiveCommands.del(keys);
    }
}

@Service
class LettucePubSubService {
    
    private final RedisClient redisClient;
    private final List<String> receivedMessages = new CopyOnWriteArrayList<>();
    
    public LettucePubSubService(RedisClient redisClient) {
        this.redisClient = redisClient;
    }
    
    public void subscribe(String channel) {
        StatefulRedisPubSubConnection<String, String> connection = 
            redisClient.connectPubSub();
        
        connection.addListener(new RedisPubSubListener<String, String>() {
            @Override
            public void message(String channel, String message) {
                System.out.println("Received: " + message + " on channel: " + channel);
                receivedMessages.add(message);
            }
            
            @Override
            public void message(String pattern, String channel, String message) {
                System.out.println("Pattern: " + pattern + ", Channel: " + channel + ", Message: " + message);
            }
            
            @Override
            public void subscribed(String channel, long count) {
                System.out.println("Subscribed to " + channel + " (count: " + count + ")");
            }
            
            @Override
            public void psubscribed(String pattern, long count) {
                System.out.println("Pattern subscribed to " + pattern);
            }
            
            @Override
            public void unsubscribed(String channel, long count) {
                System.out.println("Unsubscribed from " + channel);
            }
            
            @Override
            public void punsubscribed(String pattern, long count) {
                System.out.println("Pattern unsubscribed from " + pattern);
            }
        });
        
        connection.sync().subscribe(channel);
    }
    
    public void publish(String channel, String message) {
        StatefulRedisConnection<String, String> connection = redisClient.connect();
        connection.sync().publish(channel, message);
        connection.close();
    }
    
    public List<String> getReceivedMessages() {
        return List.copyOf(receivedMessages);
    }
}

@RestController
@RequestMapping("/api/redis/lettuce")
class LettuceIntegrationController {
    
    private final LettuceSyncService syncService;
    private final LettuceAsyncService asyncService;
    private final LettuceReactiveService reactiveService;
    private final LettucePubSubService pubSubService;
    
    public LettuceIntegrationController(LettuceSyncService syncService,
                                       LettuceAsyncService asyncService,
                                       LettuceReactiveService reactiveService,
                                       LettucePubSubService pubSubService) {
        this.syncService = syncService;
        this.asyncService = asyncService;
        this.reactiveService = reactiveService;
        this.pubSubService = pubSubService;
    }
    
    // Sync endpoints
    @PostMapping("/sync/set")
    public String setSync(@RequestParam String key, @RequestParam String value) {
        return syncService.set(key, value);
    }
    
    @GetMapping("/sync/get/{key}")
    public String getSync(@PathVariable String key) {
        return syncService.get(key);
    }
    
    @PostMapping("/sync/incr/{key}")
    public long incrSync(@PathVariable String key) {
        return syncService.incr(key);
    }
    
    // Async endpoints
    @PostMapping("/async/set")
    public java.util.concurrent.CompletableFuture<String> setAsync(
            @RequestParam String key, @RequestParam String value) {
        return asyncService.setAsync(key, value);
    }
    
    @GetMapping("/async/get/{key}")
    public java.util.concurrent.CompletableFuture<String> getAsync(@PathVariable String key) {
        return asyncService.getAsync(key);
    }
    
    // Reactive endpoints
    @PostMapping("/reactive/set")
    public Mono<String> setReactive(@RequestParam String key, @RequestParam String value) {
        return reactiveService.setReactive(key, value);
    }
    
    @GetMapping("/reactive/get/{key}")
    public Mono<String> getReactive(@PathVariable String key) {
        return reactiveService.getReactive(key);
    }
    
    // Pub/Sub endpoints
    @PostMapping("/pubsub/subscribe/{channel}")
    public String subscribe(@PathVariable String channel) {
        pubSubService.subscribe(channel);
        return "Subscribed to " + channel;
    }
    
    @PostMapping("/pubsub/publish")
    public String publish(@RequestParam String channel, @RequestParam String message) {
        pubSubService.publish(channel, message);
        return "Message published";
    }
    
    @GetMapping("/pubsub/messages")
    public List<String> getReceivedMessages() {
        return pubSubService.getReceivedMessages();
    }
    
    @GetMapping("/info")
    public String getInfo() {
        return """
                Lettuce Integration Pattern
                ==========================
                Lettuce is the default Redis client for Spring Data Redis.
                
                Features:
                - Synchronous API (RedisCommands)
                - Asynchronous API (RedisAsyncCommands with CompletableFuture)
                - Reactive API (RedisReactiveCommands with Mono/Flux)
                - Pub/Sub support
                - Thread-safe connections
                - Connection pooling
                - Cluster support
                - Sentinel support
                - Pipelining and transactions
                
                APIs Available:
                1. Sync: Blocking operations
                2. Async: Non-blocking with CompletableFuture
                3. Reactive: Reactive Streams with Mono/Flux
                4. Pub/Sub: Real-time messaging
                
                Advantages:
                - High performance
                - Low resource usage
                - Netty-based (non-blocking I/O)
                - Thread-safe
                - Active development
                - Production-ready
                
                Use Cases:
                - High-throughput applications
                - Reactive/WebFlux applications
                - Microservices
                - Real-time systems
                """;
    }
}
