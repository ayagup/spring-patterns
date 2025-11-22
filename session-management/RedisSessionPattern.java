package com.example.session;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * REDIS SESSION PATTERN
 * ======================
 * 
 * Purpose:
 * - Store sessions in Redis
 * - Enable distributed session management
 * - High-performance session access
 * - Automatic session expiration
 * - Session persistence
 * 
 * Key Components:
 * 1. Spring Session + Redis integration
 * 2. RedisTemplate for operations
 * 3. Session serialization
 * 4. TTL management
 * 5. Connection pooling
 * 
 * Benefits:
 * - Shared sessions across instances
 * - Fast in-memory storage
 * - Built-in expiration
 * - Pub/sub for session events
 * - High availability with Redis Cluster
 * 
 * Use Cases:
 * - Clustered applications
 * - Microservices
 * - Cloud deployments
 * - High-traffic websites
 * - Real-time applications
 */

// 1. REDIS SESSION CONFIGURATION
@Configuration
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 1800) // 30 minutes
class RedisSessionConfiguration {
    
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        // Lettuce is the recommended Redis client
        LettuceConnectionFactory factory = new LettuceConnectionFactory();
        factory.setHostName("localhost");
        factory.setPort(6379);
        factory.setDatabase(0);
        // factory.setPassword("your-password");
        return factory;
    }
    
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
        
        return template;
    }
}

// 2. REDIS SESSION (simulated for demonstration)
class RedisSession implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final String id;
    private final Instant creationTime;
    private Instant lastAccessedTime;
    private Duration maxInactiveInterval;
    private final Map<String, Object> attributes;
    
    public RedisSession(String id) {
        this.id = id;
        this.creationTime = Instant.now();
        this.lastAccessedTime = Instant.now();
        this.maxInactiveInterval = Duration.ofMinutes(30);
        this.attributes = new ConcurrentHashMap<>();
    }
    
    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
        touch();
    }
    
    public Object getAttribute(String name) {
        return attributes.get(name);
    }
    
    public void removeAttribute(String name) {
        attributes.remove(name);
    }
    
    public Set<String> getAttributeNames() {
        return new HashSet<>(attributes.keySet());
    }
    
    public void touch() {
        this.lastAccessedTime = Instant.now();
    }
    
    // Getters and setters
    public String getId() { return id; }
    public Instant getCreationTime() { return creationTime; }
    public Instant getLastAccessedTime() { return lastAccessedTime; }
    public void setMaxInactiveInterval(Duration interval) { this.maxInactiveInterval = interval; }
    public Duration getMaxInactiveInterval() { return maxInactiveInterval; }
    public Map<String, Object> getAttributes() { return new HashMap<>(attributes); }
}

// 3. REDIS SESSION REPOSITORY (simulated)
class RedisSessionRepository {
    
    // Simulates RedisTemplate operations
    private final Map<String, RedisSession> redisStore = new ConcurrentHashMap<>();
    private final Map<String, Long> expirationTimes = new ConcurrentHashMap<>();
    
    private static final String SESSION_PREFIX = "spring:session:sessions:";
    private static final String INDEX_PREFIX = "spring:session:index:";
    
    public RedisSession createSession() {
        String id = UUID.randomUUID().toString();
        RedisSession session = new RedisSession(id);
        save(session);
        return session;
    }
    
    public void save(RedisSession session) {
        String key = SESSION_PREFIX + session.getId();
        redisStore.put(key, session);
        
        // Set expiration (TTL)
        long ttlSeconds = session.getMaxInactiveInterval().getSeconds();
        long expirationTime = System.currentTimeMillis() + (ttlSeconds * 1000);
        expirationTimes.put(key, expirationTime);
        
        System.out.println("Session saved to Redis: " + session.getId() + 
                         " (TTL: " + ttlSeconds + "s)");
    }
    
    public RedisSession findById(String id) {
        String key = SESSION_PREFIX + id;
        
        // Check expiration
        Long expirationTime = expirationTimes.get(key);
        if (expirationTime != null && System.currentTimeMillis() > expirationTime) {
            deleteById(id);
            return null;
        }
        
        RedisSession session = redisStore.get(key);
        if (session != null) {
            session.touch();
            save(session); // Update TTL
        }
        return session;
    }
    
    public void deleteById(String id) {
        String key = SESSION_PREFIX + id;
        redisStore.remove(key);
        expirationTimes.remove(key);
        System.out.println("Session deleted from Redis: " + id);
    }
    
    public Collection<RedisSession> findAll() {
        return redisStore.values().stream()
            .filter(session -> {
                String key = SESSION_PREFIX + session.getId();
                Long exp = expirationTimes.get(key);
                return exp == null || System.currentTimeMillis() <= exp;
            })
            .toList();
    }
    
    // Index sessions by user (for finding all user sessions)
    public void indexByUser(String sessionId, String userId) {
        String indexKey = INDEX_PREFIX + "user:" + userId;
        // In real Redis, use SADD to add to a set
        System.out.println("Indexed session " + sessionId + " for user " + userId);
    }
    
    public Collection<String> findSessionIdsByUser(String userId) {
        // In real Redis, use SMEMBERS to get set members
        return Collections.emptyList();
    }
}

// 4. REDIS OPERATIONS WRAPPER
class RedisSessionOperations {
    
    private final RedisSessionRepository repository;
    
    public RedisSessionOperations(RedisSessionRepository repository) {
        this.repository = repository;
    }
    
    // Hash operations (session attributes stored as Redis hash)
    public void setHashValue(String sessionId, String field, Object value) {
        RedisSession session = repository.findById(sessionId);
        if (session != null) {
            session.setAttribute(field, value);
            repository.save(session);
        }
    }
    
    public Object getHashValue(String sessionId, String field) {
        RedisSession session = repository.findById(sessionId);
        return session != null ? session.getAttribute(field) : null;
    }
    
    public Map<String, Object> getAllHashValues(String sessionId) {
        RedisSession session = repository.findById(sessionId);
        return session != null ? session.getAttributes() : Collections.emptyMap();
    }
    
    // TTL operations
    public void expire(String sessionId, long seconds) {
        RedisSession session = repository.findById(sessionId);
        if (session != null) {
            session.setMaxInactiveInterval(Duration.ofSeconds(seconds));
            repository.save(session);
        }
    }
    
    public long getTTL(String sessionId) {
        RedisSession session = repository.findById(sessionId);
        if (session == null) {
            return -2; // Key doesn't exist
        }
        
        long ttlSeconds = session.getMaxInactiveInterval().getSeconds();
        long elapsedSeconds = Duration.between(session.getLastAccessedTime(), Instant.now()).getSeconds();
        long remainingSeconds = ttlSeconds - elapsedSeconds;
        
        return Math.max(remainingSeconds, 0);
    }
    
    // Pub/Sub for session events (simulated)
    public void publishSessionCreated(String sessionId) {
        System.out.println("Published: session.created - " + sessionId);
    }
    
    public void publishSessionDeleted(String sessionId) {
        System.out.println("Published: session.deleted - " + sessionId);
    }
    
    public void publishSessionExpired(String sessionId) {
        System.out.println("Published: session.expired - " + sessionId);
    }
}

// 5. REDIS SESSION STATISTICS
class RedisSessionStatistics {
    
    private final RedisSessionRepository repository;
    
    public RedisSessionStatistics(RedisSessionRepository repository) {
        this.repository = repository;
    }
    
    public int getTotalSessions() {
        return repository.findAll().size();
    }
    
    public Map<String, Object> getStatistics() {
        Collection<RedisSession> sessions = repository.findAll();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalSessions", sessions.size());
        stats.put("avgAttributeCount", sessions.stream()
            .mapToInt(s -> s.getAttributes().size())
            .average()
            .orElse(0.0));
        
        return stats;
    }
    
    public void printStatistics() {
        Map<String, Object> stats = getStatistics();
        System.out.println("Redis Session Statistics:");
        stats.forEach((key, value) -> 
            System.out.println("   " + key + ": " + value)
        );
    }
}

// 6. REDIS CONNECTION POOL CONFIGURATION
class RedisConnectionPoolConfig {
    
    private int maxTotal = 8;
    private int maxIdle = 8;
    private int minIdle = 0;
    private long maxWaitMillis = -1;
    
    public void configure() {
        System.out.println("Redis Connection Pool Configuration:");
        System.out.println("   Max Total: " + maxTotal);
        System.out.println("   Max Idle: " + maxIdle);
        System.out.println("   Min Idle: " + minIdle);
        System.out.println("   Max Wait: " + maxWaitMillis + "ms");
    }
    
    // Getters and setters
    public int getMaxTotal() { return maxTotal; }
    public void setMaxTotal(int maxTotal) { this.maxTotal = maxTotal; }
    public int getMaxIdle() { return maxIdle; }
    public void setMaxIdle(int maxIdle) { this.maxIdle = maxIdle; }
    public int getMinIdle() { return minIdle; }
    public void setMinIdle(int minIdle) { this.minIdle = minIdle; }
    public long getMaxWaitMillis() { return maxWaitMillis; }
    public void setMaxWaitMillis(long maxWaitMillis) { this.maxWaitMillis = maxWaitMillis; }
}

/**
 * DEMONSTRATION
 */
public class RedisSessionPattern {
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== REDIS SESSION PATTERN ===\n");
        
        RedisSessionRepository repository = new RedisSessionRepository();
        RedisSessionOperations operations = new RedisSessionOperations(repository);
        RedisSessionStatistics statistics = new RedisSessionStatistics(repository);
        
        // 1. Create sessions
        System.out.println("1. CREATING SESSIONS:");
        RedisSession session1 = repository.createSession();
        session1.setAttribute("userId", 100L);
        session1.setAttribute("username", "alice");
        session1.setAttribute("role", "ADMIN");
        repository.save(session1);
        operations.publishSessionCreated(session1.getId());
        
        RedisSession session2 = repository.createSession();
        session2.setAttribute("userId", 200L);
        session2.setAttribute("username", "bob");
        repository.save(session2);
        operations.publishSessionCreated(session2.getId());
        System.out.println();
        
        // 2. Retrieve session
        System.out.println("2. RETRIEVING SESSION:");
        RedisSession retrieved = repository.findById(session1.getId());
        System.out.println("   Session ID: " + retrieved.getId());
        System.out.println("   Attributes: " + retrieved.getAttributes());
        System.out.println("   TTL: " + operations.getTTL(retrieved.getId()) + "s");
        System.out.println();
        
        // 3. Update session attributes
        System.out.println("3. UPDATING ATTRIBUTES:");
        operations.setHashValue(session1.getId(), "lastLogin", Instant.now().toString());
        operations.setHashValue(session1.getId(), "loginCount", 5);
        
        Map<String, Object> allAttrs = operations.getAllHashValues(session1.getId());
        System.out.println("   All attributes: " + allAttrs);
        System.out.println();
        
        // 4. Session expiration
        System.out.println("4. SESSION EXPIRATION:");
        RedisSession shortLivedSession = repository.createSession();
        shortLivedSession.setMaxInactiveInterval(Duration.ofSeconds(2));
        repository.save(shortLivedSession);
        
        System.out.println("   Created session with 2s TTL");
        System.out.println("   Waiting 3 seconds...");
        Thread.sleep(3000);
        
        RedisSession expired = repository.findById(shortLivedSession.getId());
        System.out.println("   Session after expiration: " + (expired == null ? "null (expired)" : "exists"));
        if (expired == null) {
            operations.publishSessionExpired(shortLivedSession.getId());
        }
        System.out.println();
        
        // 5. User session indexing
        System.out.println("5. USER SESSION INDEXING:");
        repository.indexByUser(session1.getId(), "user-100");
        repository.indexByUser(session2.getId(), "user-200");
        System.out.println();
        
        // 6. Statistics
        System.out.println("6. SESSION STATISTICS:");
        statistics.printStatistics();
        System.out.println();
        
        // 7. Connection pool
        System.out.println("7. CONNECTION POOL:");
        RedisConnectionPoolConfig poolConfig = new RedisConnectionPoolConfig();
        poolConfig.setMaxTotal(50);
        poolConfig.setMaxIdle(20);
        poolConfig.setMinIdle(5);
        poolConfig.configure();
        System.out.println();
        
        System.out.println("Redis Commands (actual usage):");
        System.out.println("   SET spring:session:sessions:<id> <session-data>");
        System.out.println("   EXPIRE spring:session:sessions:<id> 1800");
        System.out.println("   GET spring:session:sessions:<id>");
        System.out.println("   HSET spring:session:sessions:<id> attr <value>");
        System.out.println("   HGETALL spring:session:sessions:<id>");
        System.out.println("   SADD spring:session:index:user:<userId> <sessionId>");
        System.out.println("   SMEMBERS spring:session:index:user:<userId>");
        System.out.println();
        
        System.out.println("Configuration (application.properties):");
        System.out.println("   spring.session.store-type=redis");
        System.out.println("   spring.redis.host=localhost");
        System.out.println("   spring.redis.port=6379");
        System.out.println("   spring.redis.password=your-password");
        System.out.println("   spring.session.redis.namespace=spring:session");
        System.out.println("   spring.session.timeout=30m");
        System.out.println();
        
        System.out.println("Benefits:");
        System.out.println("   ✓ Fast in-memory storage");
        System.out.println("   ✓ Automatic expiration");
        System.out.println("   ✓ Distributed sessions");
        System.out.println("   ✓ High availability");
        System.out.println("   ✓ Pub/sub for events");
        System.out.println("   ✓ Session indexing");
        System.out.println();
        
        System.out.println("Production Considerations:");
        System.out.println("   • Use Redis Sentinel for HA");
        System.out.println("   • Configure Redis Cluster for scale");
        System.out.println("   • Monitor Redis memory usage");
        System.out.println("   • Set appropriate maxmemory-policy");
        System.out.println("   • Use connection pooling");
        System.out.println("   • Enable persistence (AOF/RDB)");
    }
}
