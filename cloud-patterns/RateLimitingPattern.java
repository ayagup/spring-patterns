package com.example.cloud.ratelimit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Rate Limiting Pattern - Demonstrates Request Throttling and Quota Management
 * 
 * This pattern shows how to:
 * 1. Implement token bucket algorithm
 * 2. Implement sliding window algorithm
 * 3. Implement fixed window algorithm
 * 4. Configure rate limits per user/IP
 * 5. Handle rate limit exceeded scenarios
 * 6. Implement distributed rate limiting
 * 7. Monitor rate limit metrics
 * 8. Implement quota management
 * 9. Add rate limit headers to responses
 * 10. Test rate limiting behavior
 * 
 * Key Concepts:
 * - Rate Limiting: Control request frequency
 * - Token Bucket: Tokens refilled at fixed rate
 * - Sliding Window: Track requests in time window
 * - Fixed Window: Reset counter at intervals
 * - Quota: Total allowed requests per period
 * 
 * Rate Limiting Algorithms:
 * 1. Token Bucket - Most flexible
 * 2. Leaky Bucket - Smooth output rate
 * 3. Fixed Window Counter - Simple but imprecise
 * 4. Sliding Window Log - Accurate but memory-intensive
 * 5. Sliding Window Counter - Balance of accuracy and efficiency
 * 
 * Dependencies:
 * - resilience4j-ratelimiter
 * - spring-cloud-gateway-ratelimiter
 * 
 * @author Spring Patterns
 * @version 1.0
 */

@SpringBootApplication
public class RateLimitingPattern {

    public static void main(String[] args) {
        SpringApplication.run(RateLimitingPattern.class, args);
        
        System.out.println("=".repeat(80));
        System.out.println("RATE LIMITING PATTERN DEMONSTRATION");
        System.out.println("=".repeat(80));
        
        demonstrateAlgorithms();
        demonstrateConfiguration();
        
        System.out.println("\nApplication running with rate limiting");
        System.out.println("Test endpoints:");
        System.out.println("GET /api/ratelimit/resource - Test rate limiting");
        System.out.println("GET /api/ratelimit/metrics - View metrics");
        System.out.println("\nPress Ctrl+C to stop.\n");
    }
    
    private static void demonstrateAlgorithms() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("RATE LIMITING ALGORITHMS");
        System.out.println("=".repeat(80));
        
        System.out.println("\n1. Token Bucket:");
        System.out.println("   - Bucket capacity: 100 tokens");
        System.out.println("   - Refill rate: 10 tokens/second");
        System.out.println("   - Allows bursts up to bucket size");
        
        System.out.println("\n2. Sliding Window:");
        System.out.println("   - Window size: 60 seconds");
        System.out.println("   - Max requests: 100");
        System.out.println("   - Accurate rate limiting");
        
        System.out.println("\n3. Fixed Window:");
        System.out.println("   - Window: 1 minute intervals");
        System.out.println("   - Limit: 100 requests");
        System.out.println("   - Simple but allows 2x burst at boundary");
    }
    
    private static void demonstrateConfiguration() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("RATE LIMIT CONFIGURATION");
        System.out.println("=".repeat(80));
        
        System.out.println("\nResilient4j RateLimiter:");
        System.out.println("limitForPeriod=50");
        System.out.println("limitRefreshPeriod=1s");
        System.out.println("timeoutDuration=500ms");
    }
}

/**
 * Token Bucket Rate Limiter
 */
class TokenBucketRateLimiter {
    private final long capacity;
    private final long refillRate;  // tokens per second
    private long tokens;
    private Instant lastRefillTime;
    
    public TokenBucketRateLimiter(long capacity, long refillRate) {
        this.capacity = capacity;
        this.refillRate = refillRate;
        this.tokens = capacity;
        this.lastRefillTime = Instant.now();
    }
    
    public synchronized boolean tryAcquire() {
        refill();
        
        if (tokens > 0) {
            tokens--;
            return true;
        }
        
        return false;
    }
    
    public synchronized boolean tryAcquire(long permits) {
        refill();
        
        if (tokens >= permits) {
            tokens -= permits;
            return true;
        }
        
        return false;
    }
    
    private void refill() {
        Instant now = Instant.now();
        long elapsedSeconds = Duration.between(lastRefillTime, now).getSeconds();
        
        if (elapsedSeconds > 0) {
            long tokensToAdd = elapsedSeconds * refillRate;
            tokens = Math.min(capacity, tokens + tokensToAdd);
            lastRefillTime = now;
        }
    }
    
    public synchronized long getAvailableTokens() {
        refill();
        return tokens;
    }
}

/**
 * Sliding Window Rate Limiter
 */
class SlidingWindowRateLimiter {
    private final long windowSizeMs;
    private final int maxRequests;
    private final Queue<Long> requestTimestamps;
    
    public SlidingWindowRateLimiter(long windowSizeMs, int maxRequests) {
        this.windowSizeMs = windowSizeMs;
        this.maxRequests = maxRequests;
        this.requestTimestamps = new ConcurrentLinkedQueue<>();
    }
    
    public synchronized boolean tryAcquire() {
        long now = System.currentTimeMillis();
        long windowStart = now - windowSizeMs;
        
        // Remove old timestamps
        while (!requestTimestamps.isEmpty() && 
               requestTimestamps.peek() < windowStart) {
            requestTimestamps.poll();
        }
        
        if (requestTimestamps.size() < maxRequests) {
            requestTimestamps.offer(now);
            return true;
        }
        
        return false;
    }
    
    public synchronized int getCurrentRequests() {
        long now = System.currentTimeMillis();
        long windowStart = now - windowSizeMs;
        
        // Clean old timestamps
        while (!requestTimestamps.isEmpty() && 
               requestTimestamps.peek() < windowStart) {
            requestTimestamps.poll();
        }
        
        return requestTimestamps.size();
    }
    
    public int getRemainingRequests() {
        return Math.max(0, maxRequests - getCurrentRequests());
    }
}

/**
 * Fixed Window Rate Limiter
 */
class FixedWindowRateLimiter {
    private final long windowSizeMs;
    private final int maxRequests;
    private final AtomicInteger counter;
    private volatile long windowStartTime;
    
    public FixedWindowRateLimiter(long windowSizeMs, int maxRequests) {
        this.windowSizeMs = windowSizeMs;
        this.maxRequests = maxRequests;
        this.counter = new AtomicInteger(0);
        this.windowStartTime = System.currentTimeMillis();
    }
    
    public synchronized boolean tryAcquire() {
        long now = System.currentTimeMillis();
        
        // Check if window has expired
        if (now - windowStartTime >= windowSizeMs) {
            counter.set(0);
            windowStartTime = now;
        }
        
        if (counter.get() < maxRequests) {
            counter.incrementAndGet();
            return true;
        }
        
        return false;
    }
    
    public int getCurrentCount() {
        return counter.get();
    }
    
    public int getRemainingRequests() {
        return Math.max(0, maxRequests - counter.get());
    }
}

/**
 * Rate Limiter Metrics
 */
class RateLimiterMetrics {
    private final String name;
    private long totalRequests;
    private long allowedRequests;
    private long rejectedRequests;
    
    public RateLimiterMetrics(String name) {
        this.name = name;
        this.totalRequests = 0;
        this.allowedRequests = 0;
        this.rejectedRequests = 0;
    }
    
    public void recordRequest(boolean allowed) {
        totalRequests++;
        if (allowed) {
            allowedRequests++;
        } else {
            rejectedRequests++;
        }
    }
    
    // Getters
    public String getName() { return name; }
    public long getTotalRequests() { return totalRequests; }
    public long getAllowedRequests() { return allowedRequests; }
    public long getRejectedRequests() { return rejectedRequests; }
    public double getRejectionRate() {
        return totalRequests > 0 ? 
            (double) rejectedRequests / totalRequests * 100 : 0.0;
    }
}

/**
 * Rate Limit Exception
 */
class RateLimitExceededException extends RuntimeException {
    private final long retryAfterSeconds;
    
    public RateLimitExceededException(String message, long retryAfterSeconds) {
        super(message);
        this.retryAfterSeconds = retryAfterSeconds;
    }
    
    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}

/**
 * Multi-tier Rate Limiter Service
 */
@Service
class RateLimitService {
    
    private final Map<String, TokenBucketRateLimiter> tokenBucketLimiters;
    private final Map<String, SlidingWindowRateLimiter> slidingWindowLimiters;
    private final Map<String, FixedWindowRateLimiter> fixedWindowLimiters;
    private final Map<String, RateLimiterMetrics> metricsMap;
    
    public RateLimitService() {
        tokenBucketLimiters = new ConcurrentHashMap<>();
        slidingWindowLimiters = new ConcurrentHashMap<>();
        fixedWindowLimiters = new ConcurrentHashMap<>();
        metricsMap = new ConcurrentHashMap<>();
        
        // Initialize default limiters
        initializeDefaultLimiters();
    }
    
    private void initializeDefaultLimiters() {
        // Token bucket: 100 capacity, 10 tokens/second
        tokenBucketLimiters.put("api", new TokenBucketRateLimiter(100, 10));
        
        // Sliding window: 100 requests per minute
        slidingWindowLimiters.put("api", new SlidingWindowRateLimiter(60000, 100));
        
        // Fixed window: 100 requests per minute
        fixedWindowLimiters.put("api", new FixedWindowRateLimiter(60000, 100));
        
        // Initialize metrics
        metricsMap.put("token-bucket", new RateLimiterMetrics("token-bucket"));
        metricsMap.put("sliding-window", new RateLimiterMetrics("sliding-window"));
        metricsMap.put("fixed-window", new RateLimiterMetrics("fixed-window"));
    }
    
    public boolean checkTokenBucket(String clientId) {
        TokenBucketRateLimiter limiter = tokenBucketLimiters
            .computeIfAbsent(clientId, k -> new TokenBucketRateLimiter(100, 10));
        
        boolean allowed = limiter.tryAcquire();
        metricsMap.get("token-bucket").recordRequest(allowed);
        
        if (!allowed) {
            throw new RateLimitExceededException(
                "Rate limit exceeded (Token Bucket)", 1);
        }
        
        return allowed;
    }
    
    public boolean checkSlidingWindow(String clientId) {
        SlidingWindowRateLimiter limiter = slidingWindowLimiters
            .computeIfAbsent(clientId, k -> new SlidingWindowRateLimiter(60000, 100));
        
        boolean allowed = limiter.tryAcquire();
        metricsMap.get("sliding-window").recordRequest(allowed);
        
        if (!allowed) {
            throw new RateLimitExceededException(
                "Rate limit exceeded (Sliding Window)", 60);
        }
        
        return allowed;
    }
    
    public boolean checkFixedWindow(String clientId) {
        FixedWindowRateLimiter limiter = fixedWindowLimiters
            .computeIfAbsent(clientId, k -> new FixedWindowRateLimiter(60000, 100));
        
        boolean allowed = limiter.tryAcquire();
        metricsMap.get("fixed-window").recordRequest(allowed);
        
        if (!allowed) {
            throw new RateLimitExceededException(
                "Rate limit exceeded (Fixed Window)", 60);
        }
        
        return allowed;
    }
    
    public Map<String, Object> getRateLimitInfo(String clientId, String algorithm) {
        Map<String, Object> info = new HashMap<>();
        
        switch (algorithm.toLowerCase()) {
            case "token-bucket":
                TokenBucketRateLimiter tokenLimiter = tokenBucketLimiters.get(clientId);
                if (tokenLimiter != null) {
                    info.put("availableTokens", tokenLimiter.getAvailableTokens());
                }
                break;
                
            case "sliding-window":
                SlidingWindowRateLimiter slidingLimiter = slidingWindowLimiters.get(clientId);
                if (slidingLimiter != null) {
                    info.put("remainingRequests", slidingLimiter.getRemainingRequests());
                    info.put("currentRequests", slidingLimiter.getCurrentRequests());
                }
                break;
                
            case "fixed-window":
                FixedWindowRateLimiter fixedLimiter = fixedWindowLimiters.get(clientId);
                if (fixedLimiter != null) {
                    info.put("remainingRequests", fixedLimiter.getRemainingRequests());
                    info.put("currentCount", fixedLimiter.getCurrentCount());
                }
                break;
        }
        
        return info;
    }
    
    public Map<String, RateLimiterMetrics> getAllMetrics() {
        return new HashMap<>(metricsMap);
    }
}

/**
 * REST Controller demonstrating rate limiting
 */
@RestController
@RequestMapping("/api/ratelimit")
class RateLimitController {
    
    private final RateLimitService rateLimitService;
    
    public RateLimitController(RateLimitService rateLimitService) {
        this.rateLimitService = rateLimitService;
    }
    
    @GetMapping("/token-bucket")
    public Map<String, Object> testTokenBucket(
            @RequestParam(defaultValue = "default") String clientId) {
        try {
            rateLimitService.checkTokenBucket(clientId);
            return Map.of(
                "status", "allowed",
                "algorithm", "token-bucket",
                "info", rateLimitService.getRateLimitInfo(clientId, "token-bucket")
            );
        } catch (RateLimitExceededException e) {
            return Map.of(
                "status", "rate_limited",
                "message", e.getMessage(),
                "retryAfter", e.getRetryAfterSeconds()
            );
        }
    }
    
    @GetMapping("/sliding-window")
    public Map<String, Object> testSlidingWindow(
            @RequestParam(defaultValue = "default") String clientId) {
        try {
            rateLimitService.checkSlidingWindow(clientId);
            return Map.of(
                "status", "allowed",
                "algorithm", "sliding-window",
                "info", rateLimitService.getRateLimitInfo(clientId, "sliding-window")
            );
        } catch (RateLimitExceededException e) {
            return Map.of(
                "status", "rate_limited",
                "message", e.getMessage(),
                "retryAfter", e.getRetryAfterSeconds()
            );
        }
    }
    
    @GetMapping("/fixed-window")
    public Map<String, Object> testFixedWindow(
            @RequestParam(defaultValue = "default") String clientId) {
        try {
            rateLimitService.checkFixedWindow(clientId);
            return Map.of(
                "status", "allowed",
                "algorithm", "fixed-window",
                "info", rateLimitService.getRateLimitInfo(clientId, "fixed-window")
            );
        } catch (RateLimitExceededException e) {
            return Map.of(
                "status", "rate_limited",
                "message", e.getMessage(),
                "retryAfter", e.getRetryAfterSeconds()
            );
        }
    }
    
    @GetMapping("/metrics")
    public Map<String, RateLimiterMetrics> getMetrics() {
        return rateLimitService.getAllMetrics();
    }
}
