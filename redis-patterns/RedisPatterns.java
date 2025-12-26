package com.example.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.*;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Redis Patterns - Complete Collection
 * 
 * All 12 Redis integration patterns:
 * 1. Redis Template Pattern
 * 2. String Redis Template Pattern
 * 3. Redis Repository Pattern
 * 4. Reactive Redis Template Pattern
 * 5. Pub/Sub Pattern
 * 6. Redis Cache Pattern
 * 7. Redis Session Pattern
 * 8. Redis Messaging Pattern
 * 9. Redis Serializer Pattern
 * 10. Redis Connection Factory Pattern
 * 11. Lettuce Integration Pattern
 * 12. Jedis Integration Pattern
 * 
 * @author Spring Patterns
 */

@Data
class User {
    private String id;
    private String username;
    private String email;
    private Integer age;
}

@Data
class Product {
    private String id;
    private String name;
    private Double price;
}

/**
 * 1. Redis Template Pattern
 * Core Redis operations template
 */
@Service
@Slf4j
class RedisTemplateService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    public RedisTemplateService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    public void setValue(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }
    
    public void setValueWithExpiry(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }
    
    public Object getValue(String key) {
        return redisTemplate.opsForValue().get(key);
    }
    
    public void delete(String key) {
        redisTemplate.delete(key);
    }
    
    public void addToList(String key, Object value) {
        redisTemplate.opsForList().rightPush(key, value);
    }
    
    public void addToSet(String key, Object value) {
        redisTemplate.opsForSet().add(key, value);
    }
    
    public void addToHash(String key, String hashKey, Object value) {
        redisTemplate.opsForHash().put(key, hashKey, value);
    }
    
    public String getInfo() {
        return """
                Redis Template Pattern
                =====================
                
                Data Structures:
                
                1. String Operations (opsForValue):
                   set(key, value)
                   get(key)
                   increment(key)
                   append(key, value)
                   
                2. List Operations (opsForList):
                   leftPush(key, value)
                   rightPush(key, value)
                   leftPop(key)
                   range(key, start, end)
                   
                3. Set Operations (opsForSet):
                   add(key, values...)
                   members(key)
                   isMember(key, value)
                   remove(key, values...)
                   
                4. Hash Operations (opsForHash):
                   put(key, hashKey, value)
                   get(key, hashKey)
                   entries(key)
                   delete(key, hashKeys...)
                   
                5. Sorted Set (opsForZSet):
                   add(key, value, score)
                   range(key, start, end)
                   rangeByScore(key, min, max)
                   
                Expiration:
                expire(key, timeout, TimeUnit.SECONDS)
                expireAt(key, date)
                getExpire(key)
                persist(key)
                """;
    }
}

/**
 * 2. String Redis Template Pattern
 * Specialized for String operations
 */
@Service
@Slf4j
class StringRedisTemplateService {
    
    private final StringRedisTemplate stringRedisTemplate;
    
    public StringRedisTemplateService(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }
    
    public void setString(String key, String value) {
        stringRedisTemplate.opsForValue().set(key, value);
    }
    
    public String getString(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }
    
    public void incrementCounter(String key) {
        stringRedisTemplate.opsForValue().increment(key);
    }
    
    public String getInfo() {
        return """
                String Redis Template Pattern
                ============================
                
                Purpose:
                - Optimized for String operations
                - No serialization overhead
                - Direct String storage
                
                Usage:
                StringRedisTemplate template = new StringRedisTemplate(factory);
                template.opsForValue().set("key", "value");
                String value = template.opsForValue().get("key");
                
                Common Operations:
                - Counters: increment(), decrement()
                - Caching: set with TTL
                - Session data: String-based tokens
                - Rate limiting: String counters
                
                vs RedisTemplate:
                - StringRedisTemplate: String keys/values
                - RedisTemplate: Any Java objects
                """;
    }
}

/**
 * 3. Redis Repository Pattern
 * Spring Data repository abstraction
 */
@Service
@Slf4j
class RedisRepositoryService {
    
    public String getInfo() {
        return """
                Redis Repository Pattern
                =======================
                
                Entity Definition:
                @RedisHash("users")
                public class User {
                    @Id
                    private String id;
                    private String username;
                    private String email;
                    
                    @TimeToLive
                    private Long expiration;
                    
                    @Indexed
                    private String department;
                }
                
                Repository:
                public interface UserRepository extends CrudRepository<User, String> {
                    List<User> findByDepartment(String department);
                    List<User> findByUsernameContaining(String pattern);
                }
                
                Configuration:
                @Configuration
                @EnableRedisRepositories
                public class RedisConfig {
                    @Bean
                    public RedisConnectionFactory connectionFactory() {
                        return new LettuceConnectionFactory();
                    }
                }
                
                Usage:
                User user = new User();
                user.setUsername("john");
                userRepository.save(user);
                
                Optional<User> found = userRepository.findById(id);
                List<User> users = userRepository.findByDepartment("IT");
                """;
    }
}

/**
 * 4. Reactive Redis Template Pattern
 * Reactive Redis operations
 */
@Service
@Slf4j
class ReactiveRedisTemplateService {
    
    private final ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;
    
    public ReactiveRedisTemplateService(ReactiveRedisTemplate<String, Object> reactiveRedisTemplate) {
        this.reactiveRedisTemplate = reactiveRedisTemplate;
    }
    
    public Mono<Boolean> setValue(String key, Object value) {
        return reactiveRedisTemplate.opsForValue().set(key, value);
    }
    
    public Mono<Object> getValue(String key) {
        return reactiveRedisTemplate.opsForValue().get(key);
    }
    
    public Flux<Object> getListRange(String key, long start, long end) {
        return reactiveRedisTemplate.opsForList().range(key, start, end);
    }
    
    public String getInfo() {
        return """
                Reactive Redis Template Pattern
                ===============================
                
                Purpose:
                - Non-blocking Redis operations
                - Reactive Streams API
                - Backpressure support
                
                Configuration:
                @Bean
                public ReactiveRedisTemplate<String, Object> reactiveRedisTemplate(
                    ReactiveRedisConnectionFactory factory) {
                    
                    RedisSerializationContext<String, Object> context = 
                        RedisSerializationContext
                            .<String, Object>newSerializationContext(
                                new StringRedisSerializer())
                            .value(new GenericJackson2JsonRedisSerializer())
                            .build();
                    
                    return new ReactiveRedisTemplate<>(factory, context);
                }
                
                Usage:
                reactiveRedisTemplate.opsForValue()
                    .set("key", value)
                    .subscribe();
                
                Mono<String> value = reactiveRedisTemplate.opsForValue()
                    .get("key")
                    .map(v -> (String) v);
                
                Flux<Object> list = reactiveRedisTemplate.opsForList()
                    .range("mylist", 0, -1);
                """;
    }
}

/**
 * 5. Pub/Sub Pattern
 * Redis publish/subscribe messaging
 */
@Service
@Slf4j
class RedisPubSubService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    public RedisPubSubService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    public void publish(String channel, Object message) {
        redisTemplate.convertAndSend(channel, message);
        log.info("Published message to channel: {}", channel);
    }
    
    public String getInfo() {
        return """
                Redis Pub/Sub Pattern
                ====================
                
                Publisher:
                redisTemplate.convertAndSend("channel", message);
                
                Subscriber Configuration:
                @Bean
                RedisMessageListenerContainer container(
                    RedisConnectionFactory factory) {
                    
                    RedisMessageListenerContainer container = 
                        new RedisMessageListenerContainer();
                    container.setConnectionFactory(factory);
                    container.addMessageListener(
                        messageListener(),
                        new ChannelTopic("my-channel")
                    );
                    return container;
                }
                
                @Bean
                MessageListener messageListener() {
                    return (message, pattern) -> {
                        String channel = new String(message.getChannel());
                        String body = new String(message.getBody());
                        log.info("Received: {} from {}", body, channel);
                    };
                }
                
                Patterns:
                - ChannelTopic: Exact channel match
                - PatternTopic: Wildcard patterns
                  * PatternTopic("events.*")
                  * PatternTopic("user.*.created")
                
                Use Cases:
                - Real-time notifications
                - Event broadcasting
                - Chat applications
                - Live updates
                """;
    }
}

/**
 * 6. Redis Cache Pattern
 * Caching with Redis
 */
@Service
@Slf4j
@EnableCaching
class RedisCacheService {
    
    @Cacheable(value = "users", key = "#id")
    public User getUserById(String id) {
        log.info("Fetching user from database: {}", id);
        // Simulate database call
        User user = new User();
        user.setId(id);
        user.setUsername("user" + id);
        return user;
    }
    
    public String getInfo() {
        return """
                Redis Cache Pattern
                ==================
                
                Configuration:
                @Configuration
                @EnableCaching
                public class CacheConfig {
                    
                    @Bean
                    public RedisCacheManager cacheManager(
                        RedisConnectionFactory factory) {
                        
                        RedisCacheConfiguration config = 
                            RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(Duration.ofMinutes(10))
                                .disableCachingNullValues()
                                .serializeKeysWith(
                                    RedisSerializationContext.SerializationPair
                                        .fromSerializer(new StringRedisSerializer())
                                )
                                .serializeValuesWith(
                                    RedisSerializationContext.SerializationPair
                                        .fromSerializer(new GenericJackson2JsonRedisSerializer())
                                );
                        
                        return RedisCacheManager.builder(factory)
                            .cacheDefaults(config)
                            .build();
                    }
                }
                
                Annotations:
                @Cacheable(value = "users", key = "#id")
                public User getUser(String id) { ... }
                
                @CachePut(value = "users", key = "#user.id")
                public User updateUser(User user) { ... }
                
                @CacheEvict(value = "users", key = "#id")
                public void deleteUser(String id) { ... }
                
                @CacheEvict(value = "users", allEntries = true)
                public void clearCache() { ... }
                
                Custom Key:
                @Cacheable(value = "users", 
                           key = "#dept + ':' + #role")
                public List<User> findUsers(String dept, String role) { ... }
                """;
    }
}

/**
 * 7. Redis Session Pattern
 * HTTP session management with Redis
 */
@Service
@Slf4j
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 3600)
class RedisSessionService {
    
    public String getInfo() {
        return """
                Redis Session Pattern
                ====================
                
                Configuration:
                @Configuration
                @EnableRedisHttpSession(maxInactiveIntervalInSeconds = 3600)
                public class SessionConfig {
                    // Automatic session management
                }
                
                application.properties:
                spring.session.store-type=redis
                spring.session.redis.namespace=spring:session
                spring.session.timeout=30m
                
                Usage in Controller:
                @PostMapping("/login")
                public String login(HttpSession session, @RequestBody LoginRequest req) {
                    session.setAttribute("userId", req.getUserId());
                    session.setAttribute("roles", req.getRoles());
                    return "OK";
                }
                
                @GetMapping("/profile")
                public User getProfile(HttpSession session) {
                    String userId = (String) session.getAttribute("userId");
                    return userService.findById(userId);
                }
                
                Benefits:
                - Distributed sessions across instances
                - Session persistence across restarts
                - Auto-expiration
                - No sticky sessions required
                
                Redis Storage:
                spring:session:sessions:{sessionId}
                spring:session:sessions:expires:{sessionId}
                spring:session:expirations:{timestamp}
                """;
    }
}

/**
 * 8-12. Advanced Patterns
 */
@Service
@Slf4j
class RedisAdvancedPatternsService {
    
    /**
     * 8. Redis Messaging Pattern
     */
    public String getMessagingInfo() {
        return """
                Redis Messaging Pattern
                ======================
                
                Configuration:
                @Bean
                public RedisMessageListenerContainer container(
                    RedisConnectionFactory factory,
                    MessageListenerAdapter adapter) {
                    
                    RedisMessageListenerContainer container = 
                        new RedisMessageListenerContainer();
                    container.setConnectionFactory(factory);
                    container.addMessageListener(adapter, 
                        new PatternTopic("queue:*"));
                    return container;
                }
                
                @Bean
                public MessageListenerAdapter adapter(MessageReceiver receiver) {
                    return new MessageListenerAdapter(receiver, "handleMessage");
                }
                
                Receiver:
                @Component
                public class MessageReceiver {
                    public void handleMessage(String message) {
                        log.info("Received: {}", message);
                    }
                }
                
                Sender:
                redisTemplate.convertAndSend("queue:orders", order);
                
                Use Cases:
                - Task queues
                - Job scheduling
                - Event streaming
                - Message routing
                """;
    }
    
    /**
     * 9. Redis Serializer Pattern
     */
    public String getSerializerInfo() {
        return """
                Redis Serializer Pattern
                =======================
                
                Built-in Serializers:
                
                1. StringRedisSerializer
                   - UTF-8 String encoding
                   - Keys and simple values
                   
                2. JdkSerializationRedisSerializer
                   - Java native serialization
                   - Requires Serializable
                   - Binary format
                   
                3. GenericJackson2JsonRedisSerializer
                   - JSON format
                   - Type information included
                   - Cross-language compatible
                   
                4. Jackson2JsonRedisSerializer<T>
                   - JSON for specific type
                   - No type information
                   - Better performance
                   
                5. GenericToStringSerializer<T>
                   - toString()/parse
                   - Simple types
                
                Configuration:
                @Bean
                public RedisTemplate<String, Object> redisTemplate(
                    RedisConnectionFactory factory) {
                    
                    RedisTemplate<String, Object> template = new RedisTemplate<>();
                    template.setConnectionFactory(factory);
                    
                    // Key serializer
                    template.setKeySerializer(new StringRedisSerializer());
                    template.setHashKeySerializer(new StringRedisSerializer());
                    
                    // Value serializer
                    GenericJackson2JsonRedisSerializer serializer = 
                        new GenericJackson2JsonRedisSerializer();
                    template.setValueSerializer(serializer);
                    template.setHashValueSerializer(serializer);
                    
                    return template;
                }
                
                Custom Serializer:
                public class CustomSerializer implements RedisSerializer<MyType> {
                    @Override
                    public byte[] serialize(MyType value) {
                        // Custom serialization
                    }
                    
                    @Override
                    public MyType deserialize(byte[] bytes) {
                        // Custom deserialization
                    }
                }
                """;
    }
    
    /**
     * 10. Redis Connection Factory Pattern
     */
    public String getConnectionFactoryInfo() {
        return """
                Redis Connection Factory Pattern
                ===============================
                
                Configuration:
                @Bean
                public RedisConnectionFactory redisConnectionFactory() {
                    RedisStandaloneConfiguration config = 
                        new RedisStandaloneConfiguration();
                    config.setHostName("localhost");
                    config.setPort(6379);
                    config.setPassword(RedisPassword.of("secret"));
                    config.setDatabase(0);
                    
                    LettuceConnectionFactory factory = 
                        new LettuceConnectionFactory(config);
                    return factory;
                }
                
                Sentinel Configuration:
                @Bean
                public RedisConnectionFactory sentinelFactory() {
                    RedisSentinelConfiguration config = 
                        new RedisSentinelConfiguration()
                            .master("mymaster")
                            .sentinel("host1", 26379)
                            .sentinel("host2", 26379);
                    
                    return new LettuceConnectionFactory(config);
                }
                
                Cluster Configuration:
                @Bean
                public RedisConnectionFactory clusterFactory() {
                    RedisClusterConfiguration config = 
                        new RedisClusterConfiguration(
                            Arrays.asList(
                                "host1:6379",
                                "host2:6379",
                                "host3:6379"
                            )
                        );
                    
                    return new LettuceConnectionFactory(config);
                }
                
                Connection Pooling:
                GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
                poolConfig.setMaxTotal(100);
                poolConfig.setMaxIdle(50);
                poolConfig.setMinIdle(10);
                poolConfig.setMaxWaitMillis(2000);
                
                LettuceClientConfiguration clientConfig = 
                    LettucePoolingClientConfiguration.builder()
                        .poolConfig(poolConfig)
                        .commandTimeout(Duration.ofSeconds(5))
                        .build();
                """;
    }
    
    /**
     * 11. Lettuce Integration Pattern
     */
    public String getLettuceInfo() {
        return """
                Lettuce Integration Pattern
                ==========================
                
                Features:
                - Asynchronous API
                - Reactive API (Project Reactor)
                - Thread-safe
                - Connection pooling
                - Cluster support
                - Sentinel support
                
                Configuration:
                <dependency>
                    <groupId>io.lettuce</groupId>
                    <artifactId>lettuce-core</artifactId>
                </dependency>
                
                @Bean
                public LettuceConnectionFactory lettuceFactory() {
                    LettuceClientConfiguration clientConfig = 
                        LettuceClientConfiguration.builder()
                            .commandTimeout(Duration.ofSeconds(5))
                            .shutdownTimeout(Duration.ofMillis(100))
                            .useSsl()
                            .build();
                    
                    return new LettuceConnectionFactory(
                        standaloneConfig, clientConfig);
                }
                
                Advanced Features:
                - Pipelining
                - Transactions
                - Pub/Sub
                - Lua scripting
                - Stream support
                - Master/Replica
                
                application.properties:
                spring.redis.lettuce.pool.enabled=true
                spring.redis.lettuce.pool.max-active=100
                spring.redis.lettuce.pool.max-idle=50
                spring.redis.lettuce.pool.min-idle=10
                spring.redis.lettuce.pool.max-wait=2000ms
                spring.redis.lettuce.shutdown-timeout=100ms
                """;
    }
    
    /**
     * 12. Jedis Integration Pattern
     */
    public String getJedisInfo() {
        return """
                Jedis Integration Pattern
                ========================
                
                Features:
                - Synchronous API
                - Simple interface
                - Lightweight
                - Widely used
                
                Configuration:
                <dependency>
                    <groupId>redis.clients</groupId>
                    <artifactId>jedis</artifactId>
                </dependency>
                
                @Bean
                public JedisConnectionFactory jedisFactory() {
                    RedisStandaloneConfiguration config = 
                        new RedisStandaloneConfiguration();
                    config.setHostName("localhost");
                    config.setPort(6379);
                    
                    JedisClientConfiguration clientConfig = 
                        JedisClientConfiguration.builder()
                            .connectTimeout(Duration.ofSeconds(5))
                            .readTimeout(Duration.ofSeconds(5))
                            .usePooling()
                            .poolConfig(poolConfig())
                            .build();
                    
                    return new JedisConnectionFactory(config, clientConfig);
                }
                
                Connection Pooling:
                JedisPoolConfig poolConfig = new JedisPoolConfig();
                poolConfig.setMaxTotal(100);
                poolConfig.setMaxIdle(50);
                poolConfig.setMinIdle(10);
                poolConfig.setMaxWaitMillis(2000);
                poolConfig.setTestOnBorrow(true);
                poolConfig.setTestOnReturn(true);
                
                application.properties:
                spring.redis.jedis.pool.enabled=true
                spring.redis.jedis.pool.max-active=100
                spring.redis.jedis.pool.max-idle=50
                spring.redis.jedis.pool.min-idle=10
                spring.redis.jedis.pool.max-wait=2000ms
                
                Lettuce vs Jedis:
                - Lettuce: Async, reactive, thread-safe
                - Jedis: Sync, simple, thread-per-connection
                - Lettuce: Better for high concurrency
                - Jedis: Better for simple use cases
                """;
    }
}

/**
 * REST Controller
 */
@RestController
@RequestMapping("/redis/patterns")
@Slf4j
class RedisPatternsController {
    
    private final RedisTemplateService redisTemplateService;
    private final StringRedisTemplateService stringTemplateService;
    private final RedisRepositoryService repositoryService;
    private final ReactiveRedisTemplateService reactiveService;
    private final RedisPubSubService pubSubService;
    private final RedisCacheService cacheService;
    private final RedisSessionService sessionService;
    private final RedisAdvancedPatternsService advancedService;
    
    public RedisPatternsController(
            RedisTemplateService redisTemplateService,
            StringRedisTemplateService stringTemplateService,
            RedisRepositoryService repositoryService,
            ReactiveRedisTemplateService reactiveService,
            RedisPubSubService pubSubService,
            RedisCacheService cacheService,
            RedisSessionService sessionService,
            RedisAdvancedPatternsService advancedService) {
        this.redisTemplateService = redisTemplateService;
        this.stringTemplateService = stringTemplateService;
        this.repositoryService = repositoryService;
        this.reactiveService = reactiveService;
        this.pubSubService = pubSubService;
        this.cacheService = cacheService;
        this.sessionService = sessionService;
        this.advancedService = advancedService;
    }
    
    @GetMapping("/template")
    public String getTemplateInfo() {
        return redisTemplateService.getInfo();
    }
    
    @GetMapping("/string-template")
    public String getStringTemplateInfo() {
        return stringTemplateService.getInfo();
    }
    
    @GetMapping("/repository")
    public String getRepositoryInfo() {
        return repositoryService.getInfo();
    }
    
    @GetMapping("/reactive-template")
    public String getReactiveTemplateInfo() {
        return reactiveService.getInfo();
    }
    
    @GetMapping("/pubsub")
    public String getPubSubInfo() {
        return pubSubService.getInfo();
    }
    
    @GetMapping("/cache")
    public String getCacheInfo() {
        return cacheService.getInfo();
    }
    
    @GetMapping("/session")
    public String getSessionInfo() {
        return sessionService.getInfo();
    }
    
    @GetMapping("/messaging")
    public String getMessagingInfo() {
        return advancedService.getMessagingInfo();
    }
    
    @GetMapping("/serializer")
    public String getSerializerInfo() {
        return advancedService.getSerializerInfo();
    }
    
    @GetMapping("/connection-factory")
    public String getConnectionFactoryInfo() {
        return advancedService.getConnectionFactoryInfo();
    }
    
    @GetMapping("/lettuce")
    public String getLettuceInfo() {
        return advancedService.getLettuceInfo();
    }
    
    @GetMapping("/jedis")
    public String getJedisInfo() {
        return advancedService.getJedisInfo();
    }
}

@SpringBootApplication
public class RedisPatterns {
    public static void main(String[] args) {
        SpringApplication.run(RedisPatterns.class, args);
    }
}
