package com.example.demo.patterns.metrics;

import io.micrometer.core.instrument.*;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.instrument.binder.cache.CaffeineCacheMetrics;
import io.micrometer.core.instrument.binder.jvm.*;
import io.micrometer.core.instrument.binder.logging.LogbackMetrics;
import io.micrometer.core.instrument.binder.system.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Meter Binder Pattern - Auto-Configure Common Metrics
 * 
 * Purpose:
 * - Bind common metrics automatically
 * - JVM metrics (memory, GC, threads, classes)
 * - System metrics (CPU, file descriptors, uptime)
 * - Cache metrics (hits, misses, evictions)
 * - DataSource metrics (active connections, idle)
 * - Thread pool metrics (active threads, queue size)
 * - Custom meter binders for app-specific metrics
 * 
 * Use Cases:
 * - JVM monitoring (heap, non-heap, GC)
 * - System resource monitoring
 * - Cache performance tracking
 * - Database connection pool monitoring
 * - Thread pool utilization
 * - Custom application metrics
 * - Infrastructure metrics
 * - Performance monitoring
 * 
 * Built-in Binders:
 * - ClassLoaderMetrics: Loaded/unloaded classes
 * - JvmMemoryMetrics: Heap, non-heap, buffer pools
 * - JvmGcMetrics: GC pauses, memory allocated
 * - JvmThreadMetrics: Live, daemon, peak threads
 * - ProcessorMetrics: CPU usage
 * - FileDescriptorMetrics: Open file descriptors
 * - UptimeMetrics: Application uptime
 * - LogbackMetrics: Log events by level
 * 
 * Configuration (application.yml):
 * management:
 *   metrics:
 *     enable:
 *       jvm: true
 *       process: true
 *       system: true
 *       logback: true
 *     binders:
 *       jvm.enabled: true
 * 
 * Dependencies:
 * <dependency>
 *     <groupId>org.springframework.boot</groupId>
 *     <artifactId>spring-boot-starter-actuator</artifactId>
 * </dependency>
 * <dependency>
 *     <groupId>io.micrometer</groupId>
 *     <artifactId>micrometer-core</artifactId>
 * </dependency>
 * 
 * Warnings:
 * - Binders add metrics automatically
 * - Too many binders impact performance
 * - Some binders require dependencies
 * - Test binder impact on memory
 * - Monitor metric cardinality
 * 
 * Best Practices:
 * - Enable only needed binders
 * - Use common tags for grouping
 * - Monitor JVM metrics in production
 * - Track GC pause times
 * - Monitor thread pool saturation
 * - Cache metrics for performance tuning
 * - Create custom binders for domain metrics
 * - Document custom binder metrics
 */
@SpringBootApplication
public class MeterBinderPattern {

    public static void main(String[] args) {
        SpringApplication.run(MeterBinderPattern.class, args);
    }

    // ============================================
    // Example 1: JVM Metrics Binders
    // ============================================
    
    @Configuration
    public static class JvmMetricsConfiguration {
        
        @Bean
        public MeterBinder jvmMemoryMetrics() {
            return new JvmMemoryMetrics();
        }
        
        @Bean
        public MeterBinder jvmGcMetrics() {
            return new JvmGcMetrics();
        }
        
        @Bean
        public MeterBinder jvmThreadMetrics() {
            return new JvmThreadMetrics();
        }
        
        @Bean
        public MeterBinder classLoaderMetrics() {
            return new ClassLoaderMetrics();
        }
        
        @Bean
        public MeterBinder jvmInfoMetrics() {
            return new JvmInfoMetrics();
        }
    }

    // ============================================
    // Example 2: System Metrics Binders
    // ============================================
    
    @Configuration
    public static class SystemMetricsConfiguration {
        
        @Bean
        public MeterBinder processorMetrics() {
            return new ProcessorMetrics();
        }
        
        @Bean
        public MeterBinder fileDescriptorMetrics() {
            return new FileDescriptorMetrics();
        }
        
        @Bean
        public MeterBinder uptimeMetrics() {
            return new UptimeMetrics();
        }
        
        @Bean
        public MeterBinder diskSpaceMetrics() {
            return new DiskSpaceMetrics(new java.io.File("/"));
        }
    }

    // ============================================
    // Example 3: Logging Metrics Binder
    // ============================================
    
    @Configuration
    public static class LoggingMetricsConfiguration {
        
        @Bean
        public MeterBinder logbackMetrics() {
            return new LogbackMetrics();
        }
    }

    // ============================================
    // Example 4: Custom Thread Pool Binder
    // ============================================
    
    @Service
    public static class CustomThreadPoolService {
        
        private final ExecutorService executorService;
        
        public CustomThreadPoolService(MeterRegistry registry) {
            ThreadPoolExecutor executor = new ThreadPoolExecutor(
                5,      // core pool size
                10,     // max pool size
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(100)
            );
            
            this.executorService = executor;
            
            // Bind thread pool metrics
            bindThreadPoolMetrics(registry, executor, "custom.threadpool");
        }
        
        private void bindThreadPoolMetrics(MeterRegistry registry, 
                                          ThreadPoolExecutor executor, 
                                          String name) {
            // Active thread count
            Gauge.builder(name + ".active", executor, ThreadPoolExecutor::getActiveCount)
                .description("Active threads in pool")
                .baseUnit("threads")
                .register(registry);
            
            // Pool size
            Gauge.builder(name + ".size", executor, ThreadPoolExecutor::getPoolSize)
                .description("Current pool size")
                .baseUnit("threads")
                .register(registry);
            
            // Core pool size
            Gauge.builder(name + ".core", executor, ThreadPoolExecutor::getCorePoolSize)
                .description("Core pool size")
                .baseUnit("threads")
                .register(registry);
            
            // Max pool size
            Gauge.builder(name + ".max", executor, ThreadPoolExecutor::getMaximumPoolSize)
                .description("Maximum pool size")
                .baseUnit("threads")
                .register(registry);
            
            // Queue size
            Gauge.builder(name + ".queue.size", executor, 
                    e -> e.getQueue().size())
                .description("Queue size")
                .baseUnit("tasks")
                .register(registry);
            
            // Queue remaining capacity
            Gauge.builder(name + ".queue.remaining", executor, 
                    e -> e.getQueue().remainingCapacity())
                .description("Queue remaining capacity")
                .baseUnit("tasks")
                .register(registry);
            
            // Completed tasks
            Gauge.builder(name + ".completed", executor, ThreadPoolExecutor::getCompletedTaskCount)
                .description("Completed tasks")
                .baseUnit("tasks")
                .register(registry);
        }
        
        public void submitTask(Runnable task) {
            executorService.submit(task);
        }
        
        public void shutdown() {
            executorService.shutdown();
        }
    }

    // ============================================
    // Example 5: Custom Cache Metrics Binder
    // ============================================
    
    @Service
    public static class CustomCacheService {
        
        private final Map<String, String> cache = new ConcurrentHashMap<>();
        private final AtomicInteger hits = new AtomicInteger(0);
        private final AtomicInteger misses = new AtomicInteger(0);
        private final AtomicInteger evictions = new AtomicInteger(0);
        
        public CustomCacheService(MeterRegistry registry) {
            bindCacheMetrics(registry, "custom.cache");
        }
        
        private void bindCacheMetrics(MeterRegistry registry, String name) {
            // Cache size
            Gauge.builder(name + ".size", cache, Map::size)
                .description("Cache size")
                .baseUnit("entries")
                .register(registry);
            
            // Cache hits
            Gauge.builder(name + ".hits", hits, AtomicInteger::get)
                .description("Cache hits")
                .baseUnit("hits")
                .register(registry);
            
            // Cache misses
            Gauge.builder(name + ".misses", misses, AtomicInteger::get)
                .description("Cache misses")
                .baseUnit("misses")
                .register(registry);
            
            // Cache evictions
            Gauge.builder(name + ".evictions", evictions, AtomicInteger::get)
                .description("Cache evictions")
                .baseUnit("evictions")
                .register(registry);
            
            // Hit ratio
            Gauge.builder(name + ".hit.ratio", this, 
                    service -> {
                        int h = service.hits.get();
                        int m = service.misses.get();
                        return (h + m) == 0 ? 0 : (double) h / (h + m);
                    })
                .description("Cache hit ratio")
                .baseUnit("ratio")
                .register(registry);
        }
        
        public String get(String key) {
            String value = cache.get(key);
            if (value != null) {
                hits.incrementAndGet();
            } else {
                misses.incrementAndGet();
            }
            return value;
        }
        
        public void put(String key, String value) {
            if (cache.size() >= 1000) {
                // Simple eviction
                cache.remove(cache.keySet().iterator().next());
                evictions.incrementAndGet();
            }
            cache.put(key, value);
        }
        
        public void clear() {
            cache.clear();
        }
    }

    // ============================================
    // Example 6: Custom Database Connection Pool Binder
    // ============================================
    
    @Service
    public static class CustomConnectionPoolService {
        
        private final AtomicInteger activeConnections = new AtomicInteger(0);
        private final AtomicInteger idleConnections = new AtomicInteger(10);
        private final int maxConnections = 20;
        private final AtomicInteger totalConnectionsCreated = new AtomicInteger(0);
        private final AtomicInteger totalConnectionsClosed = new AtomicInteger(0);
        
        public CustomConnectionPoolService(MeterRegistry registry) {
            bindConnectionPoolMetrics(registry, "custom.datasource");
        }
        
        private void bindConnectionPoolMetrics(MeterRegistry registry, String name) {
            // Active connections
            Gauge.builder(name + ".connections.active", activeConnections, AtomicInteger::get)
                .description("Active database connections")
                .baseUnit("connections")
                .register(registry);
            
            // Idle connections
            Gauge.builder(name + ".connections.idle", idleConnections, AtomicInteger::get)
                .description("Idle database connections")
                .baseUnit("connections")
                .register(registry);
            
            // Max connections
            Gauge.builder(name + ".connections.max", maxConnections)
                .description("Maximum database connections")
                .baseUnit("connections")
                .register(registry);
            
            // Total connections
            Gauge.builder(name + ".connections.total", this, 
                    service -> service.activeConnections.get() + service.idleConnections.get())
                .description("Total database connections")
                .baseUnit("connections")
                .register(registry);
            
            // Utilization percentage
            Gauge.builder(name + ".connections.utilization", this,
                    service -> (double) service.activeConnections.get() / service.maxConnections * 100)
                .description("Connection pool utilization percentage")
                .baseUnit("percent")
                .register(registry);
            
            // Total created
            Gauge.builder(name + ".connections.created", totalConnectionsCreated, AtomicInteger::get)
                .description("Total connections created")
                .baseUnit("connections")
                .register(registry);
            
            // Total closed
            Gauge.builder(name + ".connections.closed", totalConnectionsClosed, AtomicInteger::get)
                .description("Total connections closed")
                .baseUnit("connections")
                .register(registry);
        }
        
        public void acquireConnection() {
            if (idleConnections.get() > 0) {
                idleConnections.decrementAndGet();
                activeConnections.incrementAndGet();
            } else if (activeConnections.get() + idleConnections.get() < maxConnections) {
                activeConnections.incrementAndGet();
                totalConnectionsCreated.incrementAndGet();
            }
        }
        
        public void releaseConnection() {
            if (activeConnections.get() > 0) {
                activeConnections.decrementAndGet();
                idleConnections.incrementAndGet();
            }
        }
    }

    // ============================================
    // Example 7: Custom Application Metrics Binder
    // ============================================
    
    public static class ApplicationMetricsBinder implements MeterBinder {
        
        private final AtomicInteger activeUsers = new AtomicInteger(0);
        private final AtomicInteger totalRequests = new AtomicInteger(0);
        private final AtomicInteger activeRequests = new AtomicInteger(0);
        
        @Override
        public void bindTo(MeterRegistry registry) {
            // Active users
            Gauge.builder("app.users.active", activeUsers, AtomicInteger::get)
                .description("Number of active users")
                .baseUnit("users")
                .tag("type", "active")
                .register(registry);
            
            // Total requests
            Gauge.builder("app.requests.total", totalRequests, AtomicInteger::get)
                .description("Total requests processed")
                .baseUnit("requests")
                .register(registry);
            
            // Active requests
            Gauge.builder("app.requests.active", activeRequests, AtomicInteger::get)
                .description("Currently active requests")
                .baseUnit("requests")
                .register(registry);
            
            System.out.println("Application metrics binder registered");
        }
        
        public void userLogin() {
            activeUsers.incrementAndGet();
        }
        
        public void userLogout() {
            if (activeUsers.get() > 0) {
                activeUsers.decrementAndGet();
            }
        }
        
        public void requestStart() {
            totalRequests.incrementAndGet();
            activeRequests.incrementAndGet();
        }
        
        public void requestEnd() {
            if (activeRequests.get() > 0) {
                activeRequests.decrementAndGet();
            }
        }
    }

    // ============================================
    // Example 8: Business Metrics Binder
    // ============================================
    
    public static class BusinessMetricsBinder implements MeterBinder {
        
        private final AtomicInteger ordersToday = new AtomicInteger(0);
        private final AtomicInteger revenueToday = new AtomicInteger(0);
        private final AtomicInteger activeSubscriptions = new AtomicInteger(0);
        
        @Override
        public void bindTo(MeterRegistry registry) {
            // Orders today
            Gauge.builder("business.orders.today", ordersToday, AtomicInteger::get)
                .description("Number of orders today")
                .baseUnit("orders")
                .tag("period", "daily")
                .register(registry);
            
            // Revenue today
            Gauge.builder("business.revenue.today", revenueToday, AtomicInteger::get)
                .description("Revenue today in cents")
                .baseUnit("cents")
                .tag("period", "daily")
                .register(registry);
            
            // Active subscriptions
            Gauge.builder("business.subscriptions.active", activeSubscriptions, AtomicInteger::get)
                .description("Number of active subscriptions")
                .baseUnit("subscriptions")
                .register(registry);
            
            System.out.println("Business metrics binder registered");
        }
        
        public void recordOrder(int amountCents) {
            ordersToday.incrementAndGet();
            revenueToday.addAndGet(amountCents);
        }
        
        public void addSubscription() {
            activeSubscriptions.incrementAndGet();
        }
        
        public void cancelSubscription() {
            if (activeSubscriptions.get() > 0) {
                activeSubscriptions.decrementAndGet();
            }
        }
        
        public void resetDaily() {
            ordersToday.set(0);
            revenueToday.set(0);
        }
    }

    // ============================================
    // Example 9: Meter Binder Configuration
    // ============================================
    
    @Configuration
    public static class CustomBindersConfiguration {
        
        @Bean
        public ApplicationMetricsBinder applicationMetricsBinder() {
            return new ApplicationMetricsBinder();
        }
        
        @Bean
        public BusinessMetricsBinder businessMetricsBinder() {
            return new BusinessMetricsBinder();
        }
    }

    // ============================================
    // Example 10: Meter Binder REST Controller
    // ============================================
    
    @RestController
    @RequestMapping("/api/binders")
    public static class MeterBinderController {
        
        private final CustomThreadPoolService threadPoolService;
        private final CustomCacheService cacheService;
        private final CustomConnectionPoolService connectionPoolService;
        private final ApplicationMetricsBinder appBinder;
        private final BusinessMetricsBinder businessBinder;
        
        public MeterBinderController(
                CustomThreadPoolService threadPoolService,
                CustomCacheService cacheService,
                CustomConnectionPoolService connectionPoolService,
                ApplicationMetricsBinder appBinder,
                BusinessMetricsBinder businessBinder) {
            this.threadPoolService = threadPoolService;
            this.cacheService = cacheService;
            this.connectionPoolService = connectionPoolService;
            this.appBinder = appBinder;
            this.businessBinder = businessBinder;
        }
        
        @PostMapping("/threadpool/task")
        public Map<String, String> submitTask() {
            threadPoolService.submitTask(() -> {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            return Collections.singletonMap("status", "task submitted");
        }
        
        @GetMapping("/cache/{key}")
        public Map<String, String> getCached(@PathVariable String key) {
            String value = cacheService.get(key);
            if (value == null) {
                return Collections.singletonMap("result", "cache miss");
            }
            return Collections.singletonMap("value", value);
        }
        
        @PostMapping("/cache")
        public Map<String, String> putCache(
                @RequestParam String key,
                @RequestParam String value) {
            cacheService.put(key, value);
            return Collections.singletonMap("status", "cached");
        }
        
        @DeleteMapping("/cache")
        public Map<String, String> clearCache() {
            cacheService.clear();
            return Collections.singletonMap("status", "cache cleared");
        }
        
        @PostMapping("/connections/acquire")
        public Map<String, String> acquireConnection() {
            connectionPoolService.acquireConnection();
            return Collections.singletonMap("status", "connection acquired");
        }
        
        @PostMapping("/connections/release")
        public Map<String, String> releaseConnection() {
            connectionPoolService.releaseConnection();
            return Collections.singletonMap("status", "connection released");
        }
        
        @PostMapping("/app/login")
        public Map<String, String> userLogin() {
            appBinder.userLogin();
            return Collections.singletonMap("status", "user logged in");
        }
        
        @PostMapping("/app/logout")
        public Map<String, String> userLogout() {
            appBinder.userLogout();
            return Collections.singletonMap("status", "user logged out");
        }
        
        @PostMapping("/app/request/start")
        public Map<String, String> requestStart() {
            appBinder.requestStart();
            return Collections.singletonMap("status", "request started");
        }
        
        @PostMapping("/app/request/end")
        public Map<String, String> requestEnd() {
            appBinder.requestEnd();
            return Collections.singletonMap("status", "request ended");
        }
        
        @PostMapping("/business/order")
        public Map<String, String> recordOrder(@RequestParam int amountCents) {
            businessBinder.recordOrder(amountCents);
            return Collections.singletonMap("status", "order recorded");
        }
        
        @PostMapping("/business/subscription/add")
        public Map<String, String> addSubscription() {
            businessBinder.addSubscription();
            return Collections.singletonMap("status", "subscription added");
        }
        
        @PostMapping("/business/subscription/cancel")
        public Map<String, String> cancelSubscription() {
            businessBinder.cancelSubscription();
            return Collections.singletonMap("status", "subscription cancelled");
        }
        
        @PostMapping("/business/reset")
        public Map<String, String> resetDailyMetrics() {
            businessBinder.resetDaily();
            return Collections.singletonMap("status", "daily metrics reset");
        }
    }
}
