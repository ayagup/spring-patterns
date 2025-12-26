package com.example.demo.patterns.health;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

/**
 * Reactive Health Indicator Pattern - Non-blocking Health Checks
 * 
 * Purpose:
 * - Non-blocking health checks
 * - Reactive health monitoring
 * - Async health verification
 * - WebFlux integration
 * - High-performance health endpoints
 * 
 * Use Cases:
 * - Reactive applications
 * - Non-blocking database checks
 * - Async external service verification
 * - WebFlux health endpoints
 * - High-throughput systems
 * - Cloud-native reactive apps
 * 
 * ReactiveHealthIndicator Interface:
 * public interface ReactiveHealthIndicator extends HealthContributor {
 *     Mono<Health> health();
 * }
 * 
 * Configuration (application.yml):
 * management:
 *   endpoints:
 *     web:
 *       exposure:
 *         include: health
 *   endpoint:
 *     health:
 *       show-details: always
 * spring:
 *   webflux:
 *     base-path: /api
 * 
 * Reactive Health Benefits:
 * - Non-blocking I/O
 * - Better resource utilization
 * - Parallel health checks
 * - Backpressure support
 * - Timeout handling
 * - Error recovery
 * 
 * Dependencies:
 * <dependency>
 *     <groupId>org.springframework.boot</groupId>
 *     <artifactId>spring-boot-starter-actuator</artifactId>
 * </dependency>
 * <dependency>
 *     <groupId>org.springframework.boot</groupId>
 *     <artifactId>spring-boot-starter-webflux</artifactId>
 * </dependency>
 * 
 * Reactive Operators:
 * - timeout(): Set maximum wait time
 * - retry(): Retry on failure
 * - onErrorResume(): Fallback on error
 * - delayElement(): Simulate latency
 * - subscribeOn(): Specify scheduler
 * 
 * Warnings:
 * - Set appropriate timeouts
 * - Handle backpressure
 * - Avoid blocking operations
 * - Test timeout scenarios
 * - Monitor reactive streams
 * - Handle subscription errors
 * 
 * Best Practices:
 * - Use appropriate timeouts (1-3 seconds)
 * - Implement retry logic
 * - Provide fallback values
 * - Use proper error handling
 * - Test with realistic delays
 * - Monitor subscription performance
 * - Use circuit breakers
 * - Log health check errors
 * - Cache when appropriate
 * - Use parallel execution for independent checks
 */
@SpringBootApplication
public class ReactiveHealthIndicatorPattern {

    public static void main(String[] args) {
        SpringApplication.run(ReactiveHealthIndicatorPattern.class, args);
    }

    // ============================================
    // Example 1: Basic Reactive Health Indicator
    // ============================================
    
    @Component("reactiveBasic")
    public static class BasicReactiveHealthIndicator implements ReactiveHealthIndicator {
        
        @Override
        public Mono<Health> health() {
            return Mono.fromSupplier(() -> {
                // Simulate health check
                boolean healthy = Math.random() > 0.05; // 95% success
                
                if (healthy) {
                    return Health.up()
                        .withDetail("type", "reactive")
                        .withDetail("timestamp", Instant.now())
                        .build();
                } else {
                    return Health.down()
                        .withDetail("type", "reactive")
                        .withDetail("error", "Service unavailable")
                        .build();
                }
            });
        }
    }

    // ============================================
    // Example 2: Reactive Database Health
    // ============================================
    
    @Component("reactiveDatabase")
    public static class ReactiveDatabaseHealthIndicator implements ReactiveHealthIndicator {
        
        @Override
        public Mono<Health> health() {
            return checkDatabaseConnection()
                .timeout(Duration.ofSeconds(3))
                .map(connected -> {
                    if (connected) {
                        return Health.up()
                            .withDetail("database", "PostgreSQL")
                            .withDetail("connectionPool", "active")
                            .withDetail("connections", 10)
                            .withDetail("responseTime", "15ms")
                            .build();
                    } else {
                        return Health.down()
                            .withDetail("database", "PostgreSQL")
                            .withDetail("error", "Connection failed")
                            .build();
                    }
                })
                .onErrorResume(error -> {
                    return Mono.just(Health.down()
                        .withDetail("database", "PostgreSQL")
                        .withDetail("error", error.getMessage())
                        .build());
                });
        }
        
        private Mono<Boolean> checkDatabaseConnection() {
            return Mono.delay(Duration.ofMillis(50))
                .map(delay -> Math.random() > 0.05); // 95% success
        }
    }

    // ============================================
    // Example 3: Reactive External API Health
    // ============================================
    
    @Component("reactiveExternalApi")
    public static class ReactiveExternalApiHealthIndicator implements ReactiveHealthIndicator {
        
        private static final String API_URL = "https://api.example.com/health";
        
        @Override
        public Mono<Health> health() {
            return callExternalApi()
                .timeout(Duration.ofSeconds(5))
                .retry(2) // Retry twice on failure
                .map(response -> Health.up()
                    .withDetail("api", "External Service")
                    .withDetail("url", API_URL)
                    .withDetail("status", response.status)
                    .withDetail("responseTime", response.responseTime)
                    .build())
                .onErrorResume(error -> {
                    return Mono.just(Health.down()
                        .withDetail("api", "External Service")
                        .withDetail("url", API_URL)
                        .withDetail("error", error.getMessage())
                        .withDetail("retries", "3 attempts failed")
                        .build());
                });
        }
        
        private Mono<ApiResponse> callExternalApi() {
            return Mono.delay(Duration.ofMillis(100))
                .map(delay -> {
                    boolean success = Math.random() > 0.15; // 85% success
                    if (success) {
                        return new ApiResponse("UP", "100ms");
                    } else {
                        throw new RuntimeException("API unavailable");
                    }
                });
        }
        
        private static class ApiResponse {
            String status;
            String responseTime;
            
            ApiResponse(String status, String responseTime) {
                this.status = status;
                this.responseTime = responseTime;
            }
        }
    }

    // ============================================
    // Example 4: Reactive Redis Health
    // ============================================
    
    @Component("reactiveRedis")
    public static class ReactiveRedisHealthIndicator implements ReactiveHealthIndicator {
        
        @Override
        public Mono<Health> health() {
            return Mono.zip(
                checkRedisConnection(),
                getRedisInfo(),
                checkRedisLatency()
            )
            .timeout(Duration.ofSeconds(2))
            .map(tuple -> {
                boolean connected = tuple.getT1();
                RedisInfo info = tuple.getT2();
                long latency = tuple.getT3();
                
                if (!connected) {
                    return Health.down()
                        .withDetail("redis", "connection failed")
                        .build();
                }
                
                Map<String, Object> details = new HashMap<>();
                details.put("redis", "connected");
                details.put("version", info.version);
                details.put("uptime", info.uptime);
                details.put("connectedClients", info.connectedClients);
                details.put("usedMemory", info.usedMemory);
                details.put("latency", latency + "ms");
                
                if (latency > 100) {
                    return Health.status("DEGRADED")
                        .withDetails(details)
                        .withDetail("warning", "High latency detected")
                        .build();
                }
                
                return Health.up()
                    .withDetails(details)
                    .build();
            })
            .onErrorResume(error -> {
                return Mono.just(Health.down()
                    .withDetail("redis", "error")
                    .withDetail("error", error.getMessage())
                    .build());
            });
        }
        
        private Mono<Boolean> checkRedisConnection() {
            return Mono.delay(Duration.ofMillis(20))
                .map(delay -> Math.random() > 0.05); // 95% success
        }
        
        private Mono<RedisInfo> getRedisInfo() {
            return Mono.delay(Duration.ofMillis(30))
                .map(delay -> new RedisInfo("6.2.0", "30d", 100, "512MB"));
        }
        
        private Mono<Long> checkRedisLatency() {
            return Mono.delay(Duration.ofMillis(10))
                .map(delay -> (long) (Math.random() * 150)); // 0-150ms
        }
        
        private static class RedisInfo {
            String version;
            String uptime;
            int connectedClients;
            String usedMemory;
            
            RedisInfo(String version, String uptime, int connectedClients, String usedMemory) {
                this.version = version;
                this.uptime = uptime;
                this.connectedClients = connectedClients;
                this.usedMemory = usedMemory;
            }
        }
    }

    // ============================================
    // Example 5: Reactive MongoDB Health
    // ============================================
    
    @Component("reactiveMongoDB")
    public static class ReactiveMongoDBHealthIndicator implements ReactiveHealthIndicator {
        
        @Override
        public Mono<Health> health() {
            return checkMongoConnection()
                .timeout(Duration.ofSeconds(3))
                .flatMap(connected -> {
                    if (!connected) {
                        return Mono.just(Health.down()
                            .withDetail("mongodb", "connection failed")
                            .build());
                    }
                    
                    return getMongoStats()
                        .map(stats -> {
                            Map<String, Object> details = new HashMap<>();
                            details.put("mongodb", "connected");
                            details.put("version", stats.version);
                            details.put("collections", stats.collections);
                            details.put("documents", stats.documents);
                            details.put("dataSize", stats.dataSize);
                            details.put("replicaSet", stats.replicaSet);
                            
                            return Health.up()
                                .withDetails(details)
                                .build();
                        });
                })
                .onErrorResume(error -> {
                    return Mono.just(Health.down()
                        .withDetail("mongodb", "error")
                        .withDetail("error", error.getMessage())
                        .build());
                });
        }
        
        private Mono<Boolean> checkMongoConnection() {
            return Mono.delay(Duration.ofMillis(40))
                .map(delay -> Math.random() > 0.05); // 95% success
        }
        
        private Mono<MongoStats> getMongoStats() {
            return Mono.delay(Duration.ofMillis(60))
                .map(delay -> new MongoStats("5.0.0", 25, 150000, "1.2GB", "rs0"));
        }
        
        private static class MongoStats {
            String version;
            int collections;
            int documents;
            String dataSize;
            String replicaSet;
            
            MongoStats(String version, int collections, int documents, String dataSize, String replicaSet) {
                this.version = version;
                this.collections = collections;
                this.documents = documents;
                this.dataSize = dataSize;
                this.replicaSet = replicaSet;
            }
        }
    }

    // ============================================
    // Example 6: Parallel Reactive Health Checks
    // ============================================
    
    @Component("reactiveParallel")
    public static class ParallelReactiveHealthIndicator implements ReactiveHealthIndicator {
        
        @Override
        public Mono<Health> health() {
            // Execute multiple checks in parallel
            Mono<Health> databaseCheck = checkDatabase();
            Mono<Health> cacheCheck = checkCache();
            Mono<Health> messageQueueCheck = checkMessageQueue();
            
            return Mono.zip(databaseCheck, cacheCheck, messageQueueCheck)
                .map(tuple -> {
                    Health db = tuple.getT1();
                    Health cache = tuple.getT2();
                    Health mq = tuple.getT3();
                    
                    Map<String, Object> details = new HashMap<>();
                    details.put("database", db.getStatus().getCode());
                    details.put("cache", cache.getStatus().getCode());
                    details.put("messageQueue", mq.getStatus().getCode());
                    
                    // Aggregate status
                    boolean allUp = "UP".equals(db.getStatus().getCode()) &&
                                   "UP".equals(cache.getStatus().getCode()) &&
                                   "UP".equals(mq.getStatus().getCode());
                    
                    if (allUp) {
                        return Health.up()
                            .withDetails(details)
                            .withDetail("parallelChecks", 3)
                            .build();
                    } else {
                        return Health.down()
                            .withDetails(details)
                            .withDetail("parallelChecks", 3)
                            .build();
                    }
                });
        }
        
        private Mono<Health> checkDatabase() {
            return Mono.delay(Duration.ofMillis(100))
                .map(delay -> Health.up().build());
        }
        
        private Mono<Health> checkCache() {
            return Mono.delay(Duration.ofMillis(50))
                .map(delay -> Health.up().build());
        }
        
        private Mono<Health> checkMessageQueue() {
            return Mono.delay(Duration.ofMillis(75))
                .map(delay -> Health.up().build());
        }
    }

    // ============================================
    // Example 7: Reactive Health with Circuit Breaker
    // ============================================
    
    @Component("reactiveCircuitBreaker")
    public static class CircuitBreakerReactiveHealthIndicator implements ReactiveHealthIndicator {
        
        private int failureCount = 0;
        private static final int FAILURE_THRESHOLD = 3;
        private Instant circuitOpenTime = null;
        private static final Duration CIRCUIT_TIMEOUT = Duration.ofSeconds(30);
        
        @Override
        public Mono<Health> health() {
            // Check if circuit is open
            if (isCircuitOpen()) {
                long remainingSeconds = Duration.between(Instant.now(), 
                    circuitOpenTime.plus(CIRCUIT_TIMEOUT)).getSeconds();
                
                return Mono.just(Health.outOfService()
                    .withDetail("circuitBreaker", "OPEN")
                    .withDetail("reason", "Too many failures")
                    .withDetail("failureCount", failureCount)
                    .withDetail("reopenIn", remainingSeconds + " seconds")
                    .build());
            }
            
            return performHealthCheck()
                .timeout(Duration.ofSeconds(2))
                .map(success -> {
                    if (success) {
                        failureCount = 0; // Reset on success
                        return Health.up()
                            .withDetail("circuitBreaker", "CLOSED")
                            .withDetail("failureCount", 0)
                            .build();
                    } else {
                        failureCount++;
                        if (failureCount >= FAILURE_THRESHOLD) {
                            circuitOpenTime = Instant.now();
                            return Health.outOfService()
                                .withDetail("circuitBreaker", "OPEN")
                                .withDetail("failureCount", failureCount)
                                .build();
                        }
                        return Health.down()
                            .withDetail("circuitBreaker", "CLOSED")
                            .withDetail("failureCount", failureCount)
                            .build();
                    }
                })
                .onErrorResume(error -> {
                    failureCount++;
                    if (failureCount >= FAILURE_THRESHOLD) {
                        circuitOpenTime = Instant.now();
                    }
                    return Mono.just(Health.down()
                        .withDetail("circuitBreaker", failureCount >= FAILURE_THRESHOLD ? "OPEN" : "CLOSED")
                        .withDetail("error", error.getMessage())
                        .withDetail("failureCount", failureCount)
                        .build());
                });
        }
        
        private boolean isCircuitOpen() {
            if (circuitOpenTime == null) {
                return false;
            }
            
            // Check if timeout has elapsed
            if (Instant.now().isAfter(circuitOpenTime.plus(CIRCUIT_TIMEOUT))) {
                circuitOpenTime = null; // Close circuit
                failureCount = 0; // Reset counter
                return false;
            }
            
            return true;
        }
        
        private Mono<Boolean> performHealthCheck() {
            return Mono.delay(Duration.ofMillis(100))
                .map(delay -> Math.random() > 0.3); // 70% success
        }
    }

    // ============================================
    // Example 8: Reactive Health with Caching
    // ============================================
    
    @Component("reactiveCached")
    public static class CachedReactiveHealthIndicator implements ReactiveHealthIndicator {
        
        private Mono<Health> cachedHealth = null;
        private Instant lastCheckTime = null;
        private static final Duration CACHE_DURATION = Duration.ofSeconds(10);
        
        @Override
        public Mono<Health> health() {
            Instant now = Instant.now();
            
            // Return cached result if still valid
            if (cachedHealth != null && lastCheckTime != null &&
                now.isBefore(lastCheckTime.plus(CACHE_DURATION))) {
                
                return cachedHealth.map(health -> 
                    Health.status(health.getStatus())
                        .withDetails(health.getDetails())
                        .withDetail("cached", true)
                        .withDetail("cacheAge", 
                            Duration.between(lastCheckTime, now).getSeconds() + "s")
                        .build()
                );
            }
            
            // Perform fresh health check
            cachedHealth = performExpensiveHealthCheck()
                .cache() // Cache the Mono
                .map(result -> {
                    lastCheckTime = Instant.now();
                    return result;
                });
            
            return cachedHealth;
        }
        
        private Mono<Health> performExpensiveHealthCheck() {
            return Mono.delay(Duration.ofMillis(500)) // Simulate expensive operation
                .map(delay -> {
                    boolean healthy = Math.random() > 0.1; // 90% success
                    return healthy ? 
                        Health.up()
                            .withDetail("type", "expensive check")
                            .withDetail("cached", false)
                            .withDetail("duration", "500ms")
                            .build() :
                        Health.down()
                            .withDetail("type", "expensive check")
                            .withDetail("error", "Check failed")
                            .build();
                });
        }
    }

    // ============================================
    // Example 9: Reactive Health REST Controller
    // ============================================
    
    @RestController
    @RequestMapping("/api/reactive-health")
    public static class ReactiveHealthController {
        
        private final CircuitBreakerReactiveHealthIndicator circuitBreakerHealth;
        
        public ReactiveHealthController(CircuitBreakerReactiveHealthIndicator circuitBreakerHealth) {
            this.circuitBreakerHealth = circuitBreakerHealth;
        }
        
        @GetMapping("/info")
        public Mono<Map<String, Object>> getHealthInfo() {
            Map<String, Object> info = new HashMap<>();
            info.put("reactiveHealthSupported", true);
            info.put("nonBlockingChecks", true);
            info.put("parallelExecution", true);
            info.put("circuitBreakerEnabled", true);
            info.put("cachingSupported", true);
            return Mono.just(info);
        }
        
        @GetMapping("/circuit-breaker/status")
        public Mono<Health> getCircuitBreakerStatus() {
            return circuitBreakerHealth.health();
        }
    }
}
