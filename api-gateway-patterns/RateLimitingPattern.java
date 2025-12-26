import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate Limiting Pattern - Spring Cloud Gateway
 * ============================================
 * 
 * Rate limiting controls the number of requests a client can make within a time window.
 * Prevents API abuse, protects backend services, and ensures fair resource allocation.
 * 
 * Rate Limiting Algorithms:
 * - Token Bucket: Allows burst traffic, refills at constant rate
 * - Leaky Bucket: Smooths traffic, fixed processing rate
 * - Fixed Window: Simple counter per time window
 * - Sliding Window: More accurate, considers request distribution
 * - Sliding Log: Most accurate, stores all request timestamps
 * 
 * Spring Cloud Gateway Rate Limiters:
 * - RedisRateLimiter: Distributed rate limiting using Redis
 * - Custom Rate Limiter: Implement RateLimiter interface
 * 
 * Key Components:
 * - KeyResolver: Determines rate limit key (IP, user, API key)
 * - RateLimiter: Implements rate limiting algorithm
 * - Configuration: Replenish rate, burst capacity
 * 
 * Use Cases:
 * - API throttling
 * - DDoS protection
 * - Fair usage enforcement
 * - Tiered pricing (different limits per tier)
 * - Abuse prevention
 * 
 * Dependencies:
 * <dependency>
 *     <groupId>org.springframework.cloud</groupId>
 *     <artifactId>spring-cloud-starter-gateway</artifactId>
 * </dependency>
 * <dependency>
 *     <groupId>org.springframework.boot</groupId>
 *     <artifactId>spring-boot-starter-data-redis-reactive</artifactId>
 * </dependency>
 * <dependency>
 *     <groupId>com.github.vladimir-bukhtoyarov</groupId>
 *     <artifactId>bucket4j-core</artifactId>
 * </dependency>
 */
@Configuration
public class RateLimitingPattern {

    /**
     * Example 1: Redis-Based Rate Limiting
     * Distributed rate limiting using Redis.
     * - replenishRate: Tokens added per second
     * - burstCapacity: Maximum tokens in bucket
     */
    @Bean
    public RouteLocator redisRateLimitRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
            // Rate limit by IP address
            .route("redis_rate_limit_ip", r -> r
                .path("/api/**")
                .filters(f -> f
                    .requestRateLimiter(c -> c
                        .setRateLimiter(redisRateLimiter())
                        .setKeyResolver(ipKeyResolver())
                    )
                )
                .uri("http://localhost:8081"))
            
            // Rate limit by user ID
            .route("redis_rate_limit_user", r -> r
                .path("/api/user/**")
                .filters(f -> f
                    .requestRateLimiter(c -> c
                        .setRateLimiter(redisRateLimiter())
                        .setKeyResolver(userKeyResolver())
                    )
                )
                .uri("http://localhost:8081"))
            
            // Rate limit by API key
            .route("redis_rate_limit_api_key", r -> r
                .path("/api/premium/**")
                .filters(f -> f
                    .requestRateLimiter(c -> c
                        .setRateLimiter(redisRateLimiter())
                        .setKeyResolver(apiKeyResolver())
                    )
                )
                .uri("http://localhost:8081"))
            
            .build();
    }

    /**
     * RedisRateLimiter Bean
     * ====================
     * 
     * replenishRate: How many requests per second user is allowed (steady state)
     * burstCapacity: Maximum number of requests allowed in a single second (burst)
     * requestedTokens: How many tokens a request costs (default 1)
     */
    @Bean
    public RedisRateLimiter redisRateLimiter() {
        return new RedisRateLimiter(
            10,   // replenishRate: 10 requests per second
            20    // burstCapacity: allows burst of 20 requests
        );
    }

    /**
     * Example 2: IP-Based KeyResolver
     * Rate limit by client IP address.
     */
    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            String ip = exchange.getRequest()
                .getRemoteAddress()
                .getAddress()
                .getHostAddress();
            return Mono.just(ip);
        };
    }

    /**
     * Example 3: User-Based KeyResolver
     * Rate limit by authenticated user ID.
     */
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> {
            // Extract user ID from JWT token or session
            String userId = exchange.getRequest()
                .getHeaders()
                .getFirst("X-User-ID");
            
            if (userId == null) {
                userId = "anonymous";
            }
            
            return Mono.just(userId);
        };
    }

    /**
     * Example 4: API Key KeyResolver
     * Rate limit by API key (different limits per tier).
     */
    @Bean
    public KeyResolver apiKeyResolver() {
        return exchange -> {
            String apiKey = exchange.getRequest()
                .getHeaders()
                .getFirst("X-API-Key");
            
            if (apiKey == null) {
                return Mono.just("no-api-key");
            }
            
            return Mono.just(apiKey);
        };
    }

    /**
     * Example 5: Principal-Based KeyResolver
     * Rate limit by authenticated principal.
     */
    @Bean
    public KeyResolver principalKeyResolver() {
        return exchange -> exchange.getPrincipal()
            .map(principal -> principal.getName())
            .defaultIfEmpty("anonymous");
    }

    /**
     * Example 6: Path-Based KeyResolver
     * Different rate limits for different API paths.
     */
    @Bean
    public KeyResolver pathKeyResolver() {
        return exchange -> {
            String path = exchange.getRequest().getPath().value();
            String ip = exchange.getRequest()
                .getRemoteAddress()
                .getAddress()
                .getHostAddress();
            
            // Combine IP and path for unique key
            return Mono.just(ip + ":" + path);
        };
    }

    /**
     * Example 7: Custom Token Bucket Rate Limiter (Bucket4j)
     * In-memory rate limiting using token bucket algorithm.
     */
    @Component
    public static class TokenBucketRateLimiter {
        
        private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
        
        public boolean allowRequest(String key) {
            Bucket bucket = buckets.computeIfAbsent(key, k -> createBucket());
            return bucket.tryConsume(1);
        }
        
        private Bucket createBucket() {
            // 10 requests per second, burst of 20
            Bandwidth limit = Bandwidth.classic(
                20,  // capacity
                Refill.intervally(10, Duration.ofSeconds(1))  // refill rate
            );
            return Bucket4j.builder()
                .addLimit(limit)
                .build();
        }
    }

    /**
     * Example 8: Tiered Rate Limiting
     * Different rate limits based on user tier (free, premium, enterprise).
     */
    @Component
    public static class TieredRateLimiter {
        
        private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
        
        public boolean allowRequest(String userId, UserTier tier) {
            Bucket bucket = buckets.computeIfAbsent(
                userId,
                k -> createBucket(tier)
            );
            return bucket.tryConsume(1);
        }
        
        private Bucket createBucket(UserTier tier) {
            Bandwidth limit;
            
            switch (tier) {
                case FREE:
                    // 10 requests per minute, burst of 15
                    limit = Bandwidth.classic(
                        15,
                        Refill.intervally(10, Duration.ofMinutes(1))
                    );
                    break;
                case PREMIUM:
                    // 100 requests per minute, burst of 150
                    limit = Bandwidth.classic(
                        150,
                        Refill.intervally(100, Duration.ofMinutes(1))
                    );
                    break;
                case ENTERPRISE:
                    // 1000 requests per minute, burst of 1500
                    limit = Bandwidth.classic(
                        1500,
                        Refill.intervally(1000, Duration.ofMinutes(1))
                    );
                    break;
                default:
                    throw new IllegalArgumentException("Unknown tier: " + tier);
            }
            
            return Bucket4j.builder()
                .addLimit(limit)
                .build();
        }
        
        public enum UserTier {
            FREE, PREMIUM, ENTERPRISE
        }
    }

    /**
     * Example 9: Composite KeyResolver
     * Combine multiple factors for rate limiting.
     */
    @Bean
    public KeyResolver compositeKeyResolver() {
        return exchange -> {
            String ip = exchange.getRequest()
                .getRemoteAddress()
                .getAddress()
                .getHostAddress();
            
            String userId = exchange.getRequest()
                .getHeaders()
                .getFirst("X-User-ID");
            
            String apiKey = exchange.getRequest()
                .getHeaders()
                .getFirst("X-API-Key");
            
            // Combine all factors
            String key = String.format("%s:%s:%s",
                ip,
                userId != null ? userId : "anonymous",
                apiKey != null ? apiKey : "no-key"
            );
            
            return Mono.just(key);
        };
    }

    /**
     * Example 10: Sliding Window Rate Limiter
     * More accurate than fixed window, considers request distribution.
     */
    @Component
    public static class SlidingWindowRateLimiter {
        
        private final Map<String, RequestLog> logs = new ConcurrentHashMap<>();
        private final int maxRequests = 100;
        private final Duration windowSize = Duration.ofMinutes(1);
        
        public boolean allowRequest(String key) {
            RequestLog log = logs.computeIfAbsent(key, k -> new RequestLog());
            return log.allowRequest(maxRequests, windowSize);
        }
        
        private static class RequestLog {
            private final List<Instant> requests = new ArrayList<>();
            
            public synchronized boolean allowRequest(int maxRequests, Duration windowSize) {
                Instant now = Instant.now();
                Instant windowStart = now.minus(windowSize);
                
                // Remove old requests outside window
                requests.removeIf(timestamp -> timestamp.isBefore(windowStart));
                
                // Check if within limit
                if (requests.size() < maxRequests) {
                    requests.add(now);
                    return true;
                }
                
                return false;
            }
        }
    }

    /**
     * YAML Configuration Example
     * ==========================
     * 
     * spring:
     *   cloud:
     *     gateway:
     *       routes:
     *         # Redis-based rate limiting
     *         - id: rate_limit_route
     *           uri: http://localhost:8081
     *           predicates:
     *             - Path=/api/**
     *           filters:
     *             - name: RequestRateLimiter
     *               args:
     *                 redis-rate-limiter.replenishRate: 10
     *                 redis-rate-limiter.burstCapacity: 20
     *                 redis-rate-limiter.requestedTokens: 1
     *                 key-resolver: "#{@ipKeyResolver}"
     * 
     *         # User-based rate limiting
     *         - id: user_rate_limit_route
     *           uri: http://localhost:8081
     *           predicates:
     *             - Path=/api/user/**
     *           filters:
     *             - name: RequestRateLimiter
     *               args:
     *                 redis-rate-limiter.replenishRate: 5
     *                 redis-rate-limiter.burstCapacity: 10
     *                 key-resolver: "#{@userKeyResolver}"
     * 
     *         # API key-based rate limiting
     *         - id: api_key_rate_limit_route
     *           uri: http://localhost:8081
     *           predicates:
     *             - Path=/api/premium/**
     *           filters:
     *             - name: RequestRateLimiter
     *               args:
     *                 redis-rate-limiter.replenishRate: 100
     *                 redis-rate-limiter.burstCapacity: 200
     *                 key-resolver: "#{@apiKeyResolver}"
     * 
     *   # Redis configuration
     *   redis:
     *     host: localhost
     *     port: 6379
     *     password: ${REDIS_PASSWORD:}
     *     timeout: 2000ms
     *     lettuce:
     *       pool:
     *         max-active: 8
     *         max-idle: 8
     *         min-idle: 0
     */

    /**
     * Response Headers:
     * =================
     * 
     * When rate limit is exceeded, Spring Cloud Gateway sets:
     * - X-RateLimit-Remaining: Number of remaining requests
     * - X-RateLimit-Requested-Tokens: Tokens requested
     * - X-RateLimit-Replenish-Rate: Tokens added per second
     * - X-RateLimit-Burst-Capacity: Maximum burst capacity
     * 
     * HTTP Status: 429 Too Many Requests
     */

    /**
     * Best Practices:
     * ===============
     * 
     * 1. Choose Right Algorithm: Token bucket for burst, leaky bucket for smooth traffic
     * 2. Set Appropriate Limits: Balance protection and user experience
     * 3. Use Redis for Distribution: Share rate limits across gateway instances
     * 4. Monitor Rate Limits: Track 429 responses, adjust limits
     * 5. Provide Feedback: Include rate limit headers in response
     * 6. Tiered Limits: Different limits for different user tiers
     * 7. Graceful Degradation: Inform users when limit exceeded
     * 8. Key Selection: Choose appropriate key (IP, user, API key)
     * 9. Burst Handling: Allow reasonable burst capacity
     * 10. Testing: Load test to validate rate limits
     * 
     * Common Pitfalls:
     * ================
     * 
     * 1. Too restrictive limits: Blocks legitimate users
     * 2. Too lenient limits: Doesn't prevent abuse
     * 3. Not using distributed cache: Different limits per instance
     * 4. IP-only limiting: Shared IPs (NAT, proxies) cause issues
     * 5. No burst capacity: Rejects valid burst traffic
     * 6. Not monitoring: Missing abuse patterns
     * 7. Fixed window issues: Allows 2x requests at window boundary
     * 8. Missing rate limit headers: Users don't know their limits
     * 9. Not handling 429: Users retry immediately
     * 10. Same limits for all: No differentiation by user tier
     * 
     * When to Use:
     * ============
     * 
     * - API throttling and abuse prevention
     * - Protecting backend services from overload
     * - Fair usage enforcement
     * - Tiered pricing models
     * - DDoS mitigation
     * - Cost control (prevent excessive cloud costs)
     * 
     * When NOT to Use:
     * ================
     * 
     * - Internal APIs (between trusted services)
     * - Real-time critical systems (may block important requests)
     * - Already have upstream rate limiting
     * - Low-traffic APIs (overhead not justified)
     */
}
