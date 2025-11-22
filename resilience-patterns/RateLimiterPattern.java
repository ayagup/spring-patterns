package com.example.resilience.ratelimiter;

import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Rate Limiter Pattern Implementation
 * 
 * Purpose: Controls the rate of requests to prevent system overload,
 * ensuring fair resource allocation and protecting against abuse.
 * 
 * Key Components:
 * 1. RateLimiter - Main rate limiting interface
 * 2. TokenBucketRateLimiter - Token bucket algorithm
 * 3. SlidingWindowRateLimiter - Sliding window counter
 * 4. FixedWindowRateLimiter - Fixed window counter
 * 5. LeakyBucketRateLimiter - Leaky bucket algorithm
 * 
 * Features:
 * - Token bucket algorithm
 * - Sliding window rate limiting
 * - Fixed window rate limiting
 * - Per-user rate limiting
 * - Rate limit metrics
 */

// Rate Limiter Interface
interface RateLimiter {
    boolean tryAcquire();
    boolean tryAcquire(int permits);
    String getRateLimiterType();
}

// Token Bucket Rate Limiter
class TokenBucketRateLimiter implements RateLimiter {
    private final long capacity;
    private final long refillRate; // tokens per second
    private final AtomicLong tokens;
    private volatile Instant lastRefillTime;
    
    public TokenBucketRateLimiter(long capacity, long refillRate) {
        this.capacity = capacity;
        this.refillRate = refillRate;
        this.tokens = new AtomicLong(capacity);
        this.lastRefillTime = Instant.now();
    }
    
    @Override
    public boolean tryAcquire() {
        return tryAcquire(1);
    }
    
    @Override
    public synchronized boolean tryAcquire(int permits) {
        refill();
        
        long currentTokens = tokens.get();
        if (currentTokens >= permits) {
            tokens.addAndGet(-permits);
            return true;
        }
        return false;
    }
    
    private void refill() {
        Instant now = Instant.now();
        long elapsedSeconds = Duration.between(lastRefillTime, now).getSeconds();
        
        if (elapsedSeconds > 0) {
            long tokensToAdd = elapsedSeconds * refillRate;
            long currentTokens = tokens.get();
            long newTokens = Math.min(currentTokens + tokensToAdd, capacity);
            tokens.set(newTokens);
            lastRefillTime = now;
        }
    }
    
    @Override
    public String getRateLimiterType() {
        return "TokenBucket(capacity=" + capacity + ", refillRate=" + refillRate + "/s)";
    }
    
    public long getAvailableTokens() {
        refill();
        return tokens.get();
    }
}

// Sliding Window Rate Limiter
class SlidingWindowRateLimiter implements RateLimiter {
    private final int maxRequests;
    private final Duration windowDuration;
    private final Queue<Instant> requestTimestamps;
    
    public SlidingWindowRateLimiter(int maxRequests, Duration windowDuration) {
        this.maxRequests = maxRequests;
        this.windowDuration = windowDuration;
        this.requestTimestamps = new LinkedList<>();
    }
    
    @Override
    public synchronized boolean tryAcquire() {
        return tryAcquire(1);
    }
    
    @Override
    public synchronized boolean tryAcquire(int permits) {
        Instant now = Instant.now();
        Instant windowStart = now.minus(windowDuration);
        
        // Remove old timestamps
        while (!requestTimestamps.isEmpty() && requestTimestamps.peek().isBefore(windowStart)) {
            requestTimestamps.poll();
        }
        
        // Check if we can accept the request
        if (requestTimestamps.size() + permits <= maxRequests) {
            for (int i = 0; i < permits; i++) {
                requestTimestamps.offer(now);
            }
            return true;
        }
        
        return false;
    }
    
    @Override
    public String getRateLimiterType() {
        return "SlidingWindow(max=" + maxRequests + ", window=" + windowDuration.getSeconds() + "s)";
    }
    
    public int getCurrentRequests() {
        Instant now = Instant.now();
        Instant windowStart = now.minus(windowDuration);
        
        synchronized (this) {
            while (!requestTimestamps.isEmpty() && requestTimestamps.peek().isBefore(windowStart)) {
                requestTimestamps.poll();
            }
            return requestTimestamps.size();
        }
    }
}

// Fixed Window Rate Limiter
class FixedWindowRateLimiter implements RateLimiter {
    private final int maxRequests;
    private final Duration windowDuration;
    private final AtomicLong requestCount;
    private volatile Instant windowStart;
    
    public FixedWindowRateLimiter(int maxRequests, Duration windowDuration) {
        this.maxRequests = maxRequests;
        this.windowDuration = windowDuration;
        this.requestCount = new AtomicLong(0);
        this.windowStart = Instant.now();
    }
    
    @Override
    public synchronized boolean tryAcquire() {
        return tryAcquire(1);
    }
    
    @Override
    public synchronized boolean tryAcquire(int permits) {
        Instant now = Instant.now();
        
        // Check if we need to reset the window
        if (Duration.between(windowStart, now).compareTo(windowDuration) >= 0) {
            windowStart = now;
            requestCount.set(0);
        }
        
        long current = requestCount.get();
        if (current + permits <= maxRequests) {
            requestCount.addAndGet(permits);
            return true;
        }
        
        return false;
    }
    
    @Override
    public String getRateLimiterType() {
        return "FixedWindow(max=" + maxRequests + ", window=" + windowDuration.getSeconds() + "s)";
    }
    
    public long getCurrentRequests() {
        return requestCount.get();
    }
}

// Leaky Bucket Rate Limiter
class LeakyBucketRateLimiter implements RateLimiter {
    private final Semaphore semaphore;
    private final long leakRate; // requests per second
    private final Timer timer;
    
    public LeakyBucketRateLimiter(int capacity, long leakRate) {
        this.semaphore = new Semaphore(capacity);
        this.leakRate = leakRate;
        this.timer = new Timer(true);
        
        // Schedule leak task
        long leakIntervalMs = 1000 / leakRate;
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                semaphore.release();
            }
        }, leakIntervalMs, leakIntervalMs);
    }
    
    @Override
    public boolean tryAcquire() {
        return semaphore.tryAcquire();
    }
    
    @Override
    public boolean tryAcquire(int permits) {
        return semaphore.tryAcquire(permits);
    }
    
    @Override
    public String getRateLimiterType() {
        return "LeakyBucket(leakRate=" + leakRate + "/s)";
    }
    
    public int getAvailablePermits() {
        return semaphore.availablePermits();
    }
    
    public void shutdown() {
        timer.cancel();
    }
}

// Rate Limit Metrics
class RateLimitMetrics {
    private final ConcurrentHashMap<String, AtomicLong> allowed = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> rejected = new ConcurrentHashMap<>();
    
    public void recordAllowed(String key) {
        allowed.computeIfAbsent(key, k -> new AtomicLong(0)).incrementAndGet();
    }
    
    public void recordRejected(String key) {
        rejected.computeIfAbsent(key, k -> new AtomicLong(0)).incrementAndGet();
    }
    
    public long getAllowed(String key) {
        return allowed.getOrDefault(key, new AtomicLong(0)).get();
    }
    
    public long getRejected(String key) {
        return rejected.getOrDefault(key, new AtomicLong(0)).get();
    }
    
    public double getRejectionRate(String key) {
        long allowedCount = getAllowed(key);
        long rejectedCount = getRejected(key);
        long total = allowedCount + rejectedCount;
        return total == 0 ? 0.0 : (double) rejectedCount / total * 100;
    }
}

// Rate Limiter Registry
@Component
class RateLimiterRegistry {
    private final ConcurrentHashMap<String, RateLimiter> limiters = new ConcurrentHashMap<>();
    private final RateLimitMetrics metrics = new RateLimitMetrics();
    
    public RateLimiter getOrCreate(String key, String type, Object... params) {
        return limiters.computeIfAbsent(key, k -> createRateLimiter(type, params));
    }
    
    private RateLimiter createRateLimiter(String type, Object[] params) {
        switch (type.toLowerCase()) {
            case "token-bucket":
                return new TokenBucketRateLimiter((Long) params[0], (Long) params[1]);
            case "sliding-window":
                return new SlidingWindowRateLimiter((Integer) params[0], (Duration) params[1]);
            case "fixed-window":
                return new FixedWindowRateLimiter((Integer) params[0], (Duration) params[1]);
            case "leaky-bucket":
                return new LeakyBucketRateLimiter((Integer) params[0], (Long) params[1]);
            default:
                throw new IllegalArgumentException("Unknown rate limiter type: " + type);
        }
    }
    
    public boolean tryAcquire(String key) {
        RateLimiter limiter = limiters.get(key);
        if (limiter == null) {
            return true; // No limit configured
        }
        
        boolean allowed = limiter.tryAcquire();
        if (allowed) {
            metrics.recordAllowed(key);
        } else {
            metrics.recordRejected(key);
        }
        return allowed;
    }
    
    public RateLimitMetrics getMetrics() {
        return metrics;
    }
    
    public Map<String, RateLimiter> getAllLimiters() {
        return new HashMap<>(limiters);
    }
}

// API Service with Rate Limiting
@Component
class ApiService {
    private final RateLimiterRegistry registry;
    
    public ApiService(RateLimiterRegistry registry) {
        this.registry = registry;
        
        // Initialize rate limiters
        registry.getOrCreate("global", "token-bucket", 100L, 10L); // 100 capacity, 10/s refill
        registry.getOrCreate("user-api", "sliding-window", 50, Duration.ofMinutes(1)); // 50 req/min
        registry.getOrCreate("admin-api", "fixed-window", 1000, Duration.ofHours(1)); // 1000 req/hour
    }
    
    public String callApi(String apiType, String userId) {
        String rateLimitKey = apiType + ":" + userId;
        
        // Check per-user rate limit
        if (!registry.tryAcquire(rateLimitKey)) {
            return "Rate limit exceeded for " + apiType + " - user: " + userId;
        }
        
        // Check global rate limit
        if (!registry.tryAcquire("global")) {
            return "Global rate limit exceeded";
        }
        
        return "API call successful for " + apiType + " - user: " + userId;
    }
}

// REST Controller
@RestController
@RequestMapping("/api/rate-limit")
class RateLimiterController {
    private final ApiService apiService;
    private final RateLimiterRegistry registry;
    
    public RateLimiterController(ApiService apiService, RateLimiterRegistry registry) {
        this.apiService = apiService;
        this.registry = registry;
    }
    
    @GetMapping("/call")
    public String callApi(@RequestParam(defaultValue = "user-api") String apiType,
                         @RequestParam(defaultValue = "user1") String userId) {
        return apiService.callApi(apiType, userId);
    }
    
    @GetMapping("/metrics")
    public String getMetrics() {
        StringBuilder sb = new StringBuilder("Rate Limit Metrics:\n\n");
        
        RateLimitMetrics metrics = registry.getMetrics();
        sb.append("Global:\n");
        sb.append("  Allowed: ").append(metrics.getAllowed("global")).append("\n");
        sb.append("  Rejected: ").append(metrics.getRejected("global")).append("\n");
        sb.append("  Rejection Rate: ").append(String.format("%.2f%%", metrics.getRejectionRate("global"))).append("\n");
        
        return sb.toString();
    }
    
    @GetMapping("/status")
    public String getStatus() {
        StringBuilder sb = new StringBuilder("Rate Limiter Status:\n\n");
        
        registry.getAllLimiters().forEach((key, limiter) -> {
            sb.append(key).append(": ").append(limiter.getRateLimiterType()).append("\n");
            
            if (limiter instanceof TokenBucketRateLimiter) {
                sb.append("  Available Tokens: ").append(((TokenBucketRateLimiter) limiter).getAvailableTokens()).append("\n");
            } else if (limiter instanceof SlidingWindowRateLimiter) {
                sb.append("  Current Requests: ").append(((SlidingWindowRateLimiter) limiter).getCurrentRequests()).append("\n");
            }
        });
        
        return sb.toString();
    }
}

/**
 * Demonstration of Rate Limiter Pattern
 */
public class RateLimiterPattern {
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Rate Limiter Pattern Demo ===\n");
        
        // Scenario 1: Token Bucket Rate Limiter
        System.out.println("1. Token Bucket Rate Limiter (capacity=5, refill=2/s):");
        TokenBucketRateLimiter tokenBucket = new TokenBucketRateLimiter(5, 2);
        
        for (int i = 1; i <= 7; i++) {
            boolean allowed = tokenBucket.tryAcquire();
            System.out.println("   Request " + i + ": " + (allowed ? "ALLOWED" : "REJECTED") + 
                             " (tokens: " + tokenBucket.getAvailableTokens() + ")");
        }
        
        System.out.println("   Waiting 2 seconds for token refill...");
        Thread.sleep(2000);
        System.out.println("   Tokens after refill: " + tokenBucket.getAvailableTokens());
        
        // Scenario 2: Sliding Window Rate Limiter
        System.out.println("\n2. Sliding Window Rate Limiter (max=3 requests per 2 seconds):");
        SlidingWindowRateLimiter slidingWindow = new SlidingWindowRateLimiter(3, Duration.ofSeconds(2));
        
        for (int i = 1; i <= 5; i++) {
            boolean allowed = slidingWindow.tryAcquire();
            System.out.println("   Request " + i + ": " + (allowed ? "ALLOWED" : "REJECTED") +
                             " (current: " + slidingWindow.getCurrentRequests() + "/3)");
            Thread.sleep(500);
        }
        
        // Scenario 3: Fixed Window Rate Limiter
        System.out.println("\n3. Fixed Window Rate Limiter (max=4 requests per 3 seconds):");
        FixedWindowRateLimiter fixedWindow = new FixedWindowRateLimiter(4, Duration.ofSeconds(3));
        
        for (int i = 1; i <= 6; i++) {
            boolean allowed = fixedWindow.tryAcquire();
            System.out.println("   Request " + i + ": " + (allowed ? "ALLOWED" : "REJECTED") +
                             " (current: " + fixedWindow.getCurrentRequests() + "/4)");
        }
        
        System.out.println("   Waiting 3 seconds for window reset...");
        Thread.sleep(3000);
        
        boolean allowed = fixedWindow.tryAcquire();
        System.out.println("   Request after reset: " + (allowed ? "ALLOWED" : "REJECTED") +
                         " (current: " + fixedWindow.getCurrentRequests() + "/4)");
        
        // Scenario 4: Per-User Rate Limiting
        System.out.println("\n4. Per-User Rate Limiting:");
        RateLimiterRegistry registry = new RateLimiterRegistry();
        registry.getOrCreate("user1", "token-bucket", 3L, 1L);
        registry.getOrCreate("user2", "token-bucket", 3L, 1L);
        
        for (int i = 1; i <= 4; i++) {
            boolean user1Allowed = registry.tryAcquire("user1");
            boolean user2Allowed = registry.tryAcquire("user2");
            System.out.println("   Request " + i + " - User1: " + (user1Allowed ? "ALLOWED" : "REJECTED") +
                             ", User2: " + (user2Allowed ? "ALLOWED" : "REJECTED"));
        }
        
        // Display metrics
        System.out.println("\n=== Rate Limit Metrics ===");
        RateLimitMetrics metrics = registry.getMetrics();
        System.out.println("User1 - Allowed: " + metrics.getAllowed("user1"));
        System.out.println("User1 - Rejected: " + metrics.getRejected("user1"));
        System.out.println("User1 - Rejection Rate: " + String.format("%.2f%%", metrics.getRejectionRate("user1")));
        System.out.println("User2 - Allowed: " + metrics.getAllowed("user2"));
        System.out.println("User2 - Rejected: " + metrics.getRejected("user2"));
    }
}
