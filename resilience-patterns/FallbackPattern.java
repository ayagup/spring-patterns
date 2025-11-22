package com.example.resilience.fallback;

import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Fallback Pattern Implementation
 * 
 * Purpose: Provides alternative responses when primary service fails,
 * ensuring graceful degradation and preventing complete service outage.
 * 
 * Key Components:
 * 1. FallbackHandler - Executes primary and fallback logic
 * 2. FallbackStrategy - Different fallback strategies (default, cached, alternative service)
 * 3. FallbackChain - Chain of fallbacks with priority
 * 4. FallbackMetrics - Track fallback usage
 * 
 * Features:
 * - Multiple fallback levels
 * - Cached fallback responses
 * - Alternative service routing
 * - Default value provision
 * - Fallback metrics tracking
 */

// Fallback Strategy Interface
interface FallbackStrategy<T> {
    T execute();
    String getStrategyName();
}

// Default Value Fallback
class DefaultValueFallback<T> implements FallbackStrategy<T> {
    private final T defaultValue;
    
    public DefaultValueFallback(T defaultValue) {
        this.defaultValue = defaultValue;
    }
    
    @Override
    public T execute() {
        return defaultValue;
    }
    
    @Override
    public String getStrategyName() {
        return "DefaultValue";
    }
}

// Cached Value Fallback
class CachedValueFallback<T> implements FallbackStrategy<T> {
    private final Map<String, T> cache;
    private final String cacheKey;
    
    public CachedValueFallback(Map<String, T> cache, String cacheKey) {
        this.cache = cache;
        this.cacheKey = cacheKey;
    }
    
    @Override
    public T execute() {
        return cache.get(cacheKey);
    }
    
    @Override
    public String getStrategyName() {
        return "CachedValue";
    }
}

// Alternative Service Fallback
class AlternativeServiceFallback<T> implements FallbackStrategy<T> {
    private final Supplier<T> alternativeService;
    
    public AlternativeServiceFallback(Supplier<T> alternativeService) {
        this.alternativeService = alternativeService;
    }
    
    @Override
    public T execute() {
        return alternativeService.get();
    }
    
    @Override
    public String getStrategyName() {
        return "AlternativeService";
    }
}

// Static Content Fallback
class StaticContentFallback<T> implements FallbackStrategy<T> {
    private final T staticContent;
    
    public StaticContentFallback(T staticContent) {
        this.staticContent = staticContent;
    }
    
    @Override
    public T execute() {
        return staticContent;
    }
    
    @Override
    public String getStrategyName() {
        return "StaticContent";
    }
}

// Fallback Metrics
class FallbackMetrics {
    private final Map<String, Integer> fallbackUsageCount = new ConcurrentHashMap<>();
    private final Map<String, Integer> primarySuccessCount = new ConcurrentHashMap<>();
    private final Map<String, Integer> primaryFailureCount = new ConcurrentHashMap<>();
    
    public void recordPrimarySuccess(String serviceName) {
        primarySuccessCount.merge(serviceName, 1, Integer::sum);
    }
    
    public void recordPrimaryFailure(String serviceName) {
        primaryFailureCount.merge(serviceName, 1, Integer::sum);
    }
    
    public void recordFallbackUsage(String serviceName, String strategyName) {
        String key = serviceName + ":" + strategyName;
        fallbackUsageCount.merge(key, 1, Integer::sum);
    }
    
    public Map<String, Integer> getFallbackUsageCount() {
        return new HashMap<>(fallbackUsageCount);
    }
    
    public int getPrimarySuccessCount(String serviceName) {
        return primarySuccessCount.getOrDefault(serviceName, 0);
    }
    
    public int getPrimaryFailureCount(String serviceName) {
        return primaryFailureCount.getOrDefault(serviceName, 0);
    }
    
    public double getFallbackRate(String serviceName) {
        int success = primarySuccessCount.getOrDefault(serviceName, 0);
        int failure = primaryFailureCount.getOrDefault(serviceName, 0);
        int total = success + failure;
        return total == 0 ? 0.0 : (double) failure / total * 100;
    }
}

// Fallback Chain
class FallbackChain<T> {
    private final List<FallbackStrategy<T>> strategies = new ArrayList<>();
    private final FallbackMetrics metrics;
    private final String serviceName;
    
    public FallbackChain(String serviceName, FallbackMetrics metrics) {
        this.serviceName = serviceName;
        this.metrics = metrics;
    }
    
    public FallbackChain<T> addStrategy(FallbackStrategy<T> strategy) {
        strategies.add(strategy);
        return this;
    }
    
    public T execute() {
        for (FallbackStrategy<T> strategy : strategies) {
            try {
                T result = strategy.execute();
                if (result != null) {
                    metrics.recordFallbackUsage(serviceName, strategy.getStrategyName());
                    System.out.println("[Fallback] Using strategy: " + strategy.getStrategyName() 
                                     + " for service: " + serviceName);
                    return result;
                }
            } catch (Exception e) {
                System.out.println("[Fallback] Strategy " + strategy.getStrategyName() 
                                 + " failed: " + e.getMessage());
            }
        }
        return null;
    }
}

// Main Fallback Handler
class FallbackHandler<T> {
    private final String serviceName;
    private final Supplier<T> primaryService;
    private final FallbackChain<T> fallbackChain;
    private final FallbackMetrics metrics;
    
    public FallbackHandler(String serviceName, Supplier<T> primaryService, 
                          FallbackChain<T> fallbackChain, FallbackMetrics metrics) {
        this.serviceName = serviceName;
        this.primaryService = primaryService;
        this.fallbackChain = fallbackChain;
        this.metrics = metrics;
    }
    
    public T execute() {
        try {
            T result = primaryService.get();
            metrics.recordPrimarySuccess(serviceName);
            System.out.println("[Primary] Success for service: " + serviceName);
            return result;
        } catch (Exception e) {
            metrics.recordPrimaryFailure(serviceName);
            System.out.println("[Primary] Failed for service: " + serviceName + " - " + e.getMessage());
            System.out.println("[Fallback] Activating fallback chain for: " + serviceName);
            return fallbackChain.execute();
        }
    }
}

// User Model
class User {
    private String id;
    private String name;
    private String email;
    private String status;
    
    public User(String id, String name, String email, String status) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.status = status;
    }
    
    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getStatus() { return status; }
    
    @Override
    public String toString() {
        return String.format("User{id='%s', name='%s', email='%s', status='%s'}", 
                           id, name, email, status);
    }
}

// User Service with Fallback
@Component
class UserService {
    private final Map<String, User> cache = new ConcurrentHashMap<>();
    private final FallbackMetrics metrics = new FallbackMetrics();
    private boolean primaryServiceAvailable = true;
    
    public UserService() {
        // Pre-populate cache
        cache.put("user1", new User("user1", "John Doe (Cached)", "john@cache.com", "cached"));
        cache.put("user2", new User("user2", "Jane Smith (Cached)", "jane@cache.com", "cached"));
    }
    
    public User getUserWithFallback(String userId) {
        // Create fallback chain
        FallbackChain<User> fallbackChain = new FallbackChain<>("UserService", metrics);
        
        // Add fallback strategies in priority order
        fallbackChain
            .addStrategy(new CachedValueFallback<>(cache, userId))
            .addStrategy(new AlternativeServiceFallback<>(() -> getFromBackupDatabase(userId)))
            .addStrategy(new DefaultValueFallback<>(new User(userId, "Guest User", "guest@example.com", "fallback")));
        
        // Create fallback handler
        FallbackHandler<User> handler = new FallbackHandler<>(
            "UserService",
            () -> getPrimaryUser(userId),
            fallbackChain,
            metrics
        );
        
        return handler.execute();
    }
    
    private User getPrimaryUser(String userId) {
        if (!primaryServiceAvailable) {
            throw new RuntimeException("Primary service unavailable");
        }
        
        // Simulate random failures
        if (Math.random() < 0.4) {
            throw new RuntimeException("Database connection timeout");
        }
        
        return new User(userId, "John Doe (Primary)", "john@primary.com", "active");
    }
    
    private User getFromBackupDatabase(String userId) {
        // Simulate backup database call
        if (Math.random() < 0.3) {
            throw new RuntimeException("Backup database also unavailable");
        }
        return new User(userId, "John Doe (Backup)", "john@backup.com", "active");
    }
    
    public void setPrimaryServiceAvailable(boolean available) {
        this.primaryServiceAvailable = available;
    }
    
    public FallbackMetrics getMetrics() {
        return metrics;
    }
}

// Product Service with Fallback
@Component
class ProductService {
    private final List<String> cachedProducts = Arrays.asList(
        "Laptop (Cached)", "Mouse (Cached)", "Keyboard (Cached)"
    );
    private final FallbackMetrics metrics = new FallbackMetrics();
    
    public List<String> getProductsWithFallback() {
        FallbackChain<List<String>> fallbackChain = new FallbackChain<>("ProductService", metrics);
        
        fallbackChain
            .addStrategy(new CachedValueFallback<>(Map.of("products", cachedProducts), "products"))
            .addStrategy(new StaticContentFallback<>(Arrays.asList("Default Product 1", "Default Product 2")));
        
        FallbackHandler<List<String>> handler = new FallbackHandler<>(
            "ProductService",
            this::getPrimaryProducts,
            fallbackChain,
            metrics
        );
        
        return handler.execute();
    }
    
    private List<String> getPrimaryProducts() {
        if (Math.random() < 0.5) {
            throw new RuntimeException("Product catalog service unavailable");
        }
        return Arrays.asList("Laptop (Primary)", "Mouse (Primary)", "Keyboard (Primary)", "Monitor (Primary)");
    }
    
    public FallbackMetrics getMetrics() {
        return metrics;
    }
}

// REST Controller
@RestController
@RequestMapping("/api/fallback")
class FallbackController {
    private final UserService userService;
    private final ProductService productService;
    
    public FallbackController(UserService userService, ProductService productService) {
        this.userService = userService;
        this.productService = productService;
    }
    
    @GetMapping("/user/{userId}")
    public String getUser(@PathVariable String userId) {
        User user = userService.getUserWithFallback(userId);
        return user != null ? user.toString() : "User not found";
    }
    
    @GetMapping("/products")
    public List<String> getProducts() {
        return productService.getProductsWithFallback();
    }
    
    @PostMapping("/simulate-outage/{available}")
    public String simulateOutage(@PathVariable boolean available) {
        userService.setPrimaryServiceAvailable(available);
        return "Primary service availability set to: " + available;
    }
    
    @GetMapping("/metrics")
    public String getMetrics() {
        StringBuilder sb = new StringBuilder("Fallback Metrics:\n\n");
        
        FallbackMetrics userMetrics = userService.getMetrics();
        sb.append("UserService:\n");
        sb.append("  Primary Success: ").append(userMetrics.getPrimarySuccessCount("UserService")).append("\n");
        sb.append("  Primary Failure: ").append(userMetrics.getPrimaryFailureCount("UserService")).append("\n");
        sb.append("  Fallback Rate: ").append(String.format("%.2f%%", userMetrics.getFallbackRate("UserService"))).append("\n");
        sb.append("  Fallback Usage:\n");
        userMetrics.getFallbackUsageCount().forEach((key, count) -> 
            sb.append("    ").append(key).append(": ").append(count).append("\n")
        );
        
        sb.append("\nProductService:\n");
        FallbackMetrics productMetrics = productService.getMetrics();
        sb.append("  Primary Success: ").append(productMetrics.getPrimarySuccessCount("ProductService")).append("\n");
        sb.append("  Primary Failure: ").append(productMetrics.getPrimaryFailureCount("ProductService")).append("\n");
        sb.append("  Fallback Rate: ").append(String.format("%.2f%%", productMetrics.getFallbackRate("ProductService"))).append("\n");
        
        return sb.toString();
    }
}

/**
 * Demonstration of Fallback Pattern
 */
public class FallbackPattern {
    
    public static void main(String[] args) {
        System.out.println("=== Fallback Pattern Demo ===\n");
        
        FallbackMetrics metrics = new FallbackMetrics();
        Map<String, User> cache = new ConcurrentHashMap<>();
        cache.put("user123", new User("user123", "Cached User", "cached@example.com", "cached"));
        
        // Scenario 1: Primary service succeeds
        System.out.println("1. Primary Service Success:");
        FallbackChain<User> chain1 = new FallbackChain<>("UserService", metrics);
        chain1.addStrategy(new CachedValueFallback<>(cache, "user123"))
              .addStrategy(new DefaultValueFallback<>(new User("default", "Default User", "default@example.com", "fallback")));
        
        FallbackHandler<User> handler1 = new FallbackHandler<>(
            "UserService",
            () -> new User("user123", "Primary User", "primary@example.com", "active"),
            chain1,
            metrics
        );
        
        User result1 = handler1.execute();
        System.out.println("Result: " + result1 + "\n");
        
        // Scenario 2: Primary service fails, use cached fallback
        System.out.println("2. Primary Fails, Cached Fallback:");
        FallbackHandler<User> handler2 = new FallbackHandler<>(
            "UserService",
            () -> { throw new RuntimeException("Primary service down"); },
            chain1,
            metrics
        );
        
        User result2 = handler2.execute();
        System.out.println("Result: " + result2 + "\n");
        
        // Scenario 3: Multiple fallback levels
        System.out.println("3. Multi-level Fallback Chain:");
        FallbackChain<String> chain3 = new FallbackChain<>("PaymentService", metrics);
        chain3.addStrategy(new AlternativeServiceFallback<>(() -> {
                throw new RuntimeException("Alternative service also down");
            }))
            .addStrategy(new CachedValueFallback<>(Map.of("payment", "Cached payment result"), "payment"))
            .addStrategy(new DefaultValueFallback<>("Payment queued for later processing"));
        
        FallbackHandler<String> handler3 = new FallbackHandler<>(
            "PaymentService",
            () -> { throw new RuntimeException("Primary payment gateway timeout"); },
            chain3,
            metrics
        );
        
        String result3 = handler3.execute();
        System.out.println("Result: " + result3 + "\n");
        
        // Display metrics
        System.out.println("=== Fallback Metrics ===");
        System.out.println("UserService - Primary Success: " + metrics.getPrimarySuccessCount("UserService"));
        System.out.println("UserService - Primary Failure: " + metrics.getPrimaryFailureCount("UserService"));
        System.out.println("UserService - Fallback Rate: " + String.format("%.2f%%", metrics.getFallbackRate("UserService")));
        System.out.println("\nFallback Usage:");
        metrics.getFallbackUsageCount().forEach((key, count) -> 
            System.out.println("  " + key + ": " + count + " times")
        );
    }
}
