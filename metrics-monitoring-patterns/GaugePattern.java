package com.example.demo.patterns.metrics;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.MultiGauge;
import io.micrometer.core.instrument.Tags;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Gauge Pattern - Current Value Metrics
 * 
 * Purpose:
 * - Track values that can increase or decrease
 * - Monitor current state
 * - Measure instantaneous values
 * - Report latest value on each scrape
 * 
 * Use Cases:
 * - Memory usage (heap, non-heap)
 * - Active connections
 * - Thread counts
 * - Queue size/depth
 * - Cache size
 * - Current temperature
 * - Active user sessions
 * - Pool sizes (connection, thread)
 * - JVM metrics
 * - System resources (CPU, disk)
 * 
 * Gauge Characteristics:
 * - Value can increase or decrease
 * - Reports current value (not cumulative)
 * - Sample on each scrape
 * - Thread-safe atomic references
 * - Low overhead
 * - No time dimension
 * 
 * Gauge vs Counter:
 * - Gauge: Can go up or down (memory, connections)
 * - Counter: Only increases (requests, errors)
 * 
 * Gauge Types:
 * - Number Gauge: Track numeric values
 * - Object Gauge: Track object state
 * - Time Gauge: Track time-based values
 * - Multi Gauge: Track multiple related values
 * 
 * Dependencies:
 * <dependency>
 *     <groupId>io.micrometer</groupId>
 *     <artifactId>micrometer-core</artifactId>
 * </dependency>
 * 
 * Configuration (application.yml):
 * management:
 *   metrics:
 *     tags:
 *       application: demo-app
 * 
 * Query Examples (Prometheus):
 * # Current memory usage
 * jvm_memory_used_bytes
 * 
 * # Active connections
 * connections_active
 * 
 * # Memory usage percentage
 * (jvm_memory_used_bytes / jvm_memory_max_bytes) * 100
 * 
 * Best Practices:
 * - Use for current state (not totals)
 * - Track object references (not primitives)
 * - Use AtomicInteger/AtomicLong for thread safety
 * - Don't create gauges in loops
 * - Register gauge once, update value continuously
 * - Use meaningful names
 * - Add units in description
 * - Use tags for dimensionality
 * - Monitor both used and available resources
 * - Set up alerts for thresholds
 * 
 * Common Anti-Patterns:
 * - Using gauge for counters (use Counter instead)
 * - Creating new gauge on each update
 * - Gauging non-atomic primitives
 * - High-cardinality tags
 */
@SpringBootApplication
@EnableScheduling
public class GaugePattern {

    public static void main(String[] args) {
        SpringApplication.run(GaugePattern.class, args);
    }

    // ============================================
    // Example 1: Basic Gauge with AtomicInteger
    // ============================================
    
    @Service
    public static class ConnectionPoolService {
        
        private final AtomicInteger activeConnections = new AtomicInteger(0);
        private final AtomicInteger idleConnections = new AtomicInteger(20);
        private final int maxConnections = 20;
        
        public ConnectionPoolService(MeterRegistry registry) {
            // Register gauges
            Gauge.builder("connections.active", activeConnections, AtomicInteger::get)
                .description("Number of active connections")
                .tag("pool", "main")
                .register(registry);
            
            Gauge.builder("connections.idle", idleConnections, AtomicInteger::get)
                .description("Number of idle connections")
                .tag("pool", "main")
                .register(registry);
            
            Gauge.builder("connections.max", () -> maxConnections)
                .description("Maximum connections")
                .tag("pool", "main")
                .register(registry);
            
            // Usage percentage
            Gauge.builder("connections.usage_percent", this, 
                    ConnectionPoolService::getUsagePercent)
                .description("Connection pool usage percentage")
                .tag("pool", "main")
                .baseUnit("percent")
                .register(registry);
        }
        
        public void acquireConnection() {
            if (idleConnections.get() > 0) {
                activeConnections.incrementAndGet();
                idleConnections.decrementAndGet();
                System.out.println("Connection acquired. Active: " + activeConnections.get() + 
                    ", Idle: " + idleConnections.get());
            } else {
                System.out.println("No idle connections available!");
            }
        }
        
        public void releaseConnection() {
            if (activeConnections.get() > 0) {
                activeConnections.decrementAndGet();
                idleConnections.incrementAndGet();
                System.out.println("Connection released. Active: " + activeConnections.get() + 
                    ", Idle: " + idleConnections.get());
            }
        }
        
        private double getUsagePercent() {
            return (activeConnections.get() * 100.0) / maxConnections;
        }
    }

    // ============================================
    // Example 2: JVM Memory Gauge
    // ============================================
    
    @Service
    public static class JvmMemoryMetricsService {
        
        private final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        
        public JvmMemoryMetricsService(MeterRegistry registry) {
            // Heap memory
            Gauge.builder("jvm.memory.heap.used", memoryBean,
                    bean -> bean.getHeapMemoryUsage().getUsed())
                .description("Used heap memory")
                .baseUnit("bytes")
                .register(registry);
            
            Gauge.builder("jvm.memory.heap.max", memoryBean,
                    bean -> bean.getHeapMemoryUsage().getMax())
                .description("Maximum heap memory")
                .baseUnit("bytes")
                .register(registry);
            
            Gauge.builder("jvm.memory.heap.committed", memoryBean,
                    bean -> bean.getHeapMemoryUsage().getCommitted())
                .description("Committed heap memory")
                .baseUnit("bytes")
                .register(registry);
            
            // Non-heap memory
            Gauge.builder("jvm.memory.nonheap.used", memoryBean,
                    bean -> bean.getNonHeapMemoryUsage().getUsed())
                .description("Used non-heap memory")
                .baseUnit("bytes")
                .register(registry);
            
            // Memory usage percentage
            Gauge.builder("jvm.memory.heap.usage_percent", this,
                    service -> service.getHeapUsagePercent())
                .description("Heap memory usage percentage")
                .baseUnit("percent")
                .register(registry);
        }
        
        private double getHeapUsagePercent() {
            long used = memoryBean.getHeapMemoryUsage().getUsed();
            long max = memoryBean.getHeapMemoryUsage().getMax();
            return max > 0 ? (used * 100.0) / max : 0;
        }
        
        public void printMemoryStats() {
            System.out.println("=== JVM Memory Stats ===");
            System.out.println("Heap Used: " + memoryBean.getHeapMemoryUsage().getUsed() / (1024 * 1024) + " MB");
            System.out.println("Heap Max: " + memoryBean.getHeapMemoryUsage().getMax() / (1024 * 1024) + " MB");
            System.out.println("Heap Usage: " + String.format("%.2f%%", getHeapUsagePercent()));
        }
    }

    // ============================================
    // Example 3: Thread Metrics Gauge
    // ============================================
    
    @Service
    public static class ThreadMetricsService {
        
        private final ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        
        public ThreadMetricsService(MeterRegistry registry) {
            Gauge.builder("jvm.threads.live", threadBean, ThreadMXBean::getThreadCount)
                .description("Current thread count")
                .register(registry);
            
            Gauge.builder("jvm.threads.daemon", threadBean, ThreadMXBean::getDaemonThreadCount)
                .description("Current daemon thread count")
                .register(registry);
            
            Gauge.builder("jvm.threads.peak", threadBean, ThreadMXBean::getPeakThreadCount)
                .description("Peak thread count")
                .register(registry);
            
            Gauge.builder("jvm.threads.started", threadBean, ThreadMXBean::getTotalStartedThreadCount)
                .description("Total started thread count")
                .register(registry);
        }
        
        public void printThreadStats() {
            System.out.println("=== Thread Stats ===");
            System.out.println("Live Threads: " + threadBean.getThreadCount());
            System.out.println("Daemon Threads: " + threadBean.getDaemonThreadCount());
            System.out.println("Peak Threads: " + threadBean.getPeakThreadCount());
        }
    }

    // ============================================
    // Example 4: Queue Size Gauge
    // ============================================
    
    @Service
    public static class QueueMetricsService {
        
        private final Queue<String> messageQueue = new LinkedList<>();
        private final int maxQueueSize = 1000;
        
        public QueueMetricsService(MeterRegistry registry) {
            Gauge.builder("queue.size", messageQueue, Queue::size)
                .description("Current queue size")
                .tag("queue", "messages")
                .register(registry);
            
            Gauge.builder("queue.max_size", () -> maxQueueSize)
                .description("Maximum queue size")
                .tag("queue", "messages")
                .register(registry);
            
            Gauge.builder("queue.usage_percent", this, QueueMetricsService::getQueueUsagePercent)
                .description("Queue usage percentage")
                .tag("queue", "messages")
                .baseUnit("percent")
                .register(registry);
        }
        
        public synchronized void enqueue(String message) {
            if (messageQueue.size() < maxQueueSize) {
                messageQueue.offer(message);
                System.out.println("Message enqueued. Queue size: " + messageQueue.size());
            } else {
                System.out.println("Queue full! Cannot enqueue message.");
            }
        }
        
        public synchronized String dequeue() {
            String message = messageQueue.poll();
            if (message != null) {
                System.out.println("Message dequeued. Queue size: " + messageQueue.size());
            }
            return message;
        }
        
        private double getQueueUsagePercent() {
            return (messageQueue.size() * 100.0) / maxQueueSize;
        }
    }

    // ============================================
    // Example 5: Cache Size Gauge
    // ============================================
    
    @Service
    public static class CacheMetricsService {
        
        private final Map<String, Object> cache = new ConcurrentHashMap<>();
        private final int maxCacheSize = 500;
        private final AtomicLong totalEvictions = new AtomicLong(0);
        
        public CacheMetricsService(MeterRegistry registry) {
            Gauge.builder("cache.size", cache, Map::size)
                .description("Current cache size")
                .tag("cache", "main")
                .register(registry);
            
            Gauge.builder("cache.max_size", () -> maxCacheSize)
                .description("Maximum cache size")
                .tag("cache", "main")
                .register(registry);
            
            Gauge.builder("cache.usage_percent", this, CacheMetricsService::getCacheUsagePercent)
                .description("Cache usage percentage")
                .tag("cache", "main")
                .baseUnit("percent")
                .register(registry);
            
            Gauge.builder("cache.evictions.total", totalEvictions, AtomicLong::get)
                .description("Total cache evictions")
                .tag("cache", "main")
                .register(registry);
        }
        
        public void put(String key, Object value) {
            if (cache.size() >= maxCacheSize) {
                // Simple eviction: remove first entry
                String firstKey = cache.keySet().iterator().next();
                cache.remove(firstKey);
                totalEvictions.incrementAndGet();
                System.out.println("Cache full. Evicted: " + firstKey);
            }
            cache.put(key, value);
            System.out.println("Cache put: " + key + ". Size: " + cache.size());
        }
        
        public Object get(String key) {
            return cache.get(key);
        }
        
        public void remove(String key) {
            cache.remove(key);
            System.out.println("Cache remove: " + key + ". Size: " + cache.size());
        }
        
        private double getCacheUsagePercent() {
            return (cache.size() * 100.0) / maxCacheSize;
        }
    }

    // ============================================
    // Example 6: Session Metrics Gauge
    // ============================================
    
    @Service
    public static class SessionMetricsService {
        
        private final AtomicInteger activeSessions = new AtomicInteger(0);
        private final AtomicInteger totalSessions = new AtomicInteger(0);
        private final Map<String, Long> sessionMap = new ConcurrentHashMap<>();
        
        public SessionMetricsService(MeterRegistry registry) {
            Gauge.builder("sessions.active", activeSessions, AtomicInteger::get)
                .description("Number of active sessions")
                .register(registry);
            
            Gauge.builder("sessions.total", totalSessions, AtomicInteger::get)
                .description("Total sessions created")
                .register(registry);
            
            Gauge.builder("sessions.map_size", sessionMap, Map::size)
                .description("Session map size")
                .register(registry);
        }
        
        public String createSession(String userId) {
            String sessionId = "SESSION-" + System.currentTimeMillis();
            sessionMap.put(sessionId, System.currentTimeMillis());
            activeSessions.incrementAndGet();
            totalSessions.incrementAndGet();
            System.out.println("Session created: " + sessionId + " for user: " + userId);
            return sessionId;
        }
        
        public void closeSession(String sessionId) {
            if (sessionMap.remove(sessionId) != null) {
                activeSessions.decrementAndGet();
                System.out.println("Session closed: " + sessionId);
            }
        }
        
        @Scheduled(fixedRate = 60000) // Clean up expired sessions every minute
        public void cleanupExpiredSessions() {
            long now = System.currentTimeMillis();
            long timeout = 30 * 60 * 1000; // 30 minutes
            
            sessionMap.entrySet().removeIf(entry -> {
                if (now - entry.getValue() > timeout) {
                    activeSessions.decrementAndGet();
                    System.out.println("Session expired: " + entry.getKey());
                    return true;
                }
                return false;
            });
        }
    }

    // ============================================
    // Example 7: Resource Pool Gauge
    // ============================================
    
    @Service
    public static class ResourcePoolMetricsService {
        
        private final AtomicInteger available = new AtomicInteger(100);
        private final AtomicInteger inUse = new AtomicInteger(0);
        private final AtomicInteger pending = new AtomicInteger(0);
        private final int total = 100;
        
        public ResourcePoolMetricsService(MeterRegistry registry) {
            Gauge.builder("pool.resources.available", available, AtomicInteger::get)
                .description("Available resources in pool")
                .tag("pool", "worker")
                .register(registry);
            
            Gauge.builder("pool.resources.in_use", inUse, AtomicInteger::get)
                .description("Resources in use")
                .tag("pool", "worker")
                .register(registry);
            
            Gauge.builder("pool.resources.pending", pending, AtomicInteger::get)
                .description("Pending resource requests")
                .tag("pool", "worker")
                .register(registry);
            
            Gauge.builder("pool.resources.total", () -> total)
                .description("Total resources in pool")
                .tag("pool", "worker")
                .register(registry);
        }
        
        public boolean acquire() {
            if (available.get() > 0) {
                available.decrementAndGet();
                inUse.incrementAndGet();
                System.out.println("Resource acquired. Available: " + available.get());
                return true;
            } else {
                pending.incrementAndGet();
                System.out.println("No resources available. Pending: " + pending.get());
                return false;
            }
        }
        
        public void release() {
            if (inUse.get() > 0) {
                inUse.decrementAndGet();
                if (pending.get() > 0) {
                    pending.decrementAndGet();
                } else {
                    available.incrementAndGet();
                }
                System.out.println("Resource released. Available: " + available.get());
            }
        }
    }

    // ============================================
    // Example 8: Multi Gauge - Multiple Related Metrics
    // ============================================
    
    @Service
    public static class ServerMetricsService {
        
        private final Map<String, ServerStats> serverStats = new ConcurrentHashMap<>();
        private final MultiGauge serverLoadGauge;
        
        public ServerMetricsService(MeterRegistry registry) {
            this.serverLoadGauge = MultiGauge.builder("server.load")
                .description("Load per server")
                .tag("cluster", "main")
                .register(registry);
            
            // Initialize some servers
            serverStats.put("server-1", new ServerStats("server-1"));
            serverStats.put("server-2", new ServerStats("server-2"));
            serverStats.put("server-3", new ServerStats("server-3"));
        }
        
        @Scheduled(fixedRate = 5000)
        public void updateServerMetrics() {
            serverLoadGauge.register(
                serverStats.values().stream()
                    .map(stats -> MultiGauge.Row.of(
                        Tags.of("server", stats.serverId),
                        stats.getCurrentLoad()
                    ))
                    .collect(java.util.stream.Collectors.toList()),
                true
            );
        }
        
        public void updateServerLoad(String serverId, double load) {
            ServerStats stats = serverStats.get(serverId);
            if (stats != null) {
                stats.setLoad(load);
                System.out.println("Server " + serverId + " load updated to: " + load);
            }
        }
        
        private static class ServerStats {
            private final String serverId;
            private double currentLoad = 0.0;
            
            public ServerStats(String serverId) {
                this.serverId = serverId;
            }
            
            public double getCurrentLoad() {
                return currentLoad;
            }
            
            public void setLoad(double load) {
                this.currentLoad = load;
            }
        }
    }

    // ============================================
    // Example 9: Business Metrics Gauge
    // ============================================
    
    @Service
    public static class BusinessMetricsService {
        
        private final AtomicInteger activeOrders = new AtomicInteger(0);
        private final AtomicInteger pendingPayments = new AtomicInteger(0);
        private final AtomicLong currentRevenue = new AtomicLong(0);
        
        public BusinessMetricsService(MeterRegistry registry) {
            Gauge.builder("business.orders.active", activeOrders, AtomicInteger::get)
                .description("Number of active orders")
                .register(registry);
            
            Gauge.builder("business.payments.pending", pendingPayments, AtomicInteger::get)
                .description("Number of pending payments")
                .register(registry);
            
            Gauge.builder("business.revenue.current", currentRevenue, AtomicLong::get)
                .description("Current revenue today")
                .baseUnit("USD")
                .register(registry);
        }
        
        public void createOrder(double amount) {
            activeOrders.incrementAndGet();
            pendingPayments.incrementAndGet();
            System.out.println("Order created. Active orders: " + activeOrders.get());
        }
        
        public void completePayment(double amount) {
            if (pendingPayments.get() > 0) {
                pendingPayments.decrementAndGet();
                currentRevenue.addAndGet((long) amount);
                System.out.println("Payment completed. Revenue: $" + currentRevenue.get());
            }
        }
        
        public void completeOrder() {
            if (activeOrders.get() > 0) {
                activeOrders.decrementAndGet();
                System.out.println("Order completed. Active orders: " + activeOrders.get());
            }
        }
        
        @Scheduled(cron = "0 0 0 * * *") // Reset daily at midnight
        public void resetDailyRevenue() {
            currentRevenue.set(0);
            System.out.println("Daily revenue reset");
        }
    }

    // ============================================
    // Example 10: Gauge Controller
    // ============================================
    
    @RestController
    @RequestMapping("/gauge-demo")
    public static class GaugeController {
        
        private final ConnectionPoolService poolService;
        private final JvmMemoryMetricsService memoryMetrics;
        private final ThreadMetricsService threadMetrics;
        private final QueueMetricsService queueMetrics;
        private final CacheMetricsService cacheMetrics;
        private final SessionMetricsService sessionMetrics;
        private final ResourcePoolMetricsService resourcePool;
        private final ServerMetricsService serverMetrics;
        private final BusinessMetricsService businessMetrics;
        
        public GaugeController(
                ConnectionPoolService poolService,
                JvmMemoryMetricsService memoryMetrics,
                ThreadMetricsService threadMetrics,
                QueueMetricsService queueMetrics,
                CacheMetricsService cacheMetrics,
                SessionMetricsService sessionMetrics,
                ResourcePoolMetricsService resourcePool,
                ServerMetricsService serverMetrics,
                BusinessMetricsService businessMetrics) {
            this.poolService = poolService;
            this.memoryMetrics = memoryMetrics;
            this.threadMetrics = threadMetrics;
            this.queueMetrics = queueMetrics;
            this.cacheMetrics = cacheMetrics;
            this.sessionMetrics = sessionMetrics;
            this.resourcePool = resourcePool;
            this.serverMetrics = serverMetrics;
            this.businessMetrics = businessMetrics;
        }
        
        @PostMapping("/pool/acquire")
        public String acquireConnection() {
            poolService.acquireConnection();
            return "Connection acquired";
        }
        
        @PostMapping("/pool/release")
        public String releaseConnection() {
            poolService.releaseConnection();
            return "Connection released";
        }
        
        @GetMapping("/jvm/memory")
        public String getMemoryStats() {
            memoryMetrics.printMemoryStats();
            return "Check console";
        }
        
        @GetMapping("/jvm/threads")
        public String getThreadStats() {
            threadMetrics.printThreadStats();
            return "Check console";
        }
        
        @PostMapping("/queue/enqueue")
        public String enqueueMessage(@RequestParam String message) {
            queueMetrics.enqueue(message);
            return "Message enqueued";
        }
        
        @PostMapping("/queue/dequeue")
        public String dequeueMessage() {
            String message = queueMetrics.dequeue();
            return message != null ? message : "Queue empty";
        }
        
        @PostMapping("/cache/put")
        public String cachePut(@RequestParam String key, @RequestParam String value) {
            cacheMetrics.put(key, value);
            return "Cached";
        }
        
        @PostMapping("/session/create")
        public String createSession(@RequestParam String userId) {
            return sessionMetrics.createSession(userId);
        }
        
        @PostMapping("/session/close")
        public String closeSession(@RequestParam String sessionId) {
            sessionMetrics.closeSession(sessionId);
            return "Session closed";
        }
        
        @PostMapping("/resource/acquire")
        public String acquireResource() {
            boolean acquired = resourcePool.acquire();
            return acquired ? "Resource acquired" : "No resources available";
        }
        
        @PostMapping("/resource/release")
        public String releaseResource() {
            resourcePool.release();
            return "Resource released";
        }
        
        @PostMapping("/server/load")
        public String updateServerLoad(@RequestParam String serverId, @RequestParam double load) {
            serverMetrics.updateServerLoad(serverId, load);
            return "Load updated";
        }
        
        @PostMapping("/business/order")
        public String createOrder(@RequestParam double amount) {
            businessMetrics.createOrder(amount);
            return "Order created";
        }
        
        @PostMapping("/business/payment")
        public String completePayment(@RequestParam double amount) {
            businessMetrics.completePayment(amount);
            return "Payment completed";
        }
    }
}
