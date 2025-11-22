package com.example.monitoring.jmx;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableMBeanExport;
import org.springframework.jmx.export.MBeanExporter;
import org.springframework.jmx.export.annotation.*;
import org.springframework.jmx.support.ConnectorServerFactoryBean;
import org.springframework.jmx.support.MBeanServerFactoryBean;
import org.springframework.stereotype.Component;

import javax.management.*;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * JMX Pattern - Java Management Extensions Integration
 * 
 * Demonstrates:
 * 1. Spring JMX integration with @ManagedResource
 * 2. MBean attributes and operations exposure
 * 3. JMX notifications
 * 4. MBean server configuration
 * 5. Remote JMX connectivity
 * 6. Dynamic MBean management
 * 7. Application monitoring via JMX
 * 8. Performance metrics exposure
 */
public class JMXPattern {

    public static void main(String[] args) throws Exception {
        System.out.println("=== Spring JMX Pattern Demo ===\n");

        // Create Spring application context
        AnnotationConfigApplicationContext context = 
            new AnnotationConfigApplicationContext(JMXConfig.class);

        // Get managed beans
        ApplicationMetricsMBean metricsBean = context.getBean(ApplicationMetricsMBean.class);
        CacheManagementMBean cacheBean = context.getBean(CacheManagementMBean.class);
        ThreadPoolMBean threadPoolBean = context.getBean(ThreadPoolMBean.class);

        System.out.println("JMX MBeans registered and available for monitoring");
        System.out.println("Connect using JConsole or VisualVM to: service:jmx:rmi:///jndi/rmi://localhost:9999/jmxrmi\n");

        // Simulate application activity
        simulateApplicationActivity(metricsBean, cacheBean, threadPoolBean);

        // Display current metrics
        displayMetrics(metricsBean, cacheBean, threadPoolBean);

        // Demonstrate JMX operations
        demonstrateJMXOperations(metricsBean, cacheBean, threadPoolBean);

        System.out.println("\nJMX Pattern demo completed successfully!");
        System.out.println("MBeans are still accessible via JMX for 30 seconds...");
        
        // Keep application running to allow JMX connections
        Thread.sleep(30000);
        
        context.close();
    }

    private static void simulateApplicationActivity(
            ApplicationMetricsMBean metrics, 
            CacheManagementMBean cache,
            ThreadPoolMBean threadPool) throws InterruptedException {
        
        System.out.println("Simulating application activity...\n");

        // Simulate requests
        for (int i = 0; i < 100; i++) {
            metrics.recordRequest(i % 10 == 0 ? 500 : 50);
            if (i % 20 == 0) {
                metrics.recordError();
            }
            
            // Simulate cache operations
            if (i % 3 == 0) {
                cache.recordCacheHit();
            } else {
                cache.recordCacheMiss();
            }
            
            // Simulate thread pool usage
            threadPool.submitTask();
            if (i % 5 == 0) {
                threadPool.completeTask();
            }
            
            Thread.sleep(10);
        }
    }

    private static void displayMetrics(
            ApplicationMetricsMBean metrics,
            CacheManagementMBean cache,
            ThreadPoolMBean threadPool) {
        
        System.out.println("=== Current Metrics ===");
        System.out.println("\nApplication Metrics:");
        System.out.println("  Total Requests: " + metrics.getTotalRequests());
        System.out.println("  Error Count: " + metrics.getErrorCount());
        System.out.println("  Average Response Time: " + metrics.getAverageResponseTime() + "ms");
        System.out.println("  Error Rate: " + String.format("%.2f%%", metrics.getErrorRate()));
        System.out.println("  Uptime: " + metrics.getUptime() + "s");

        System.out.println("\nCache Metrics:");
        System.out.println("  Cache Size: " + cache.getCacheSize());
        System.out.println("  Hit Count: " + cache.getHitCount());
        System.out.println("  Miss Count: " + cache.getMissCount());
        System.out.println("  Hit Ratio: " + String.format("%.2f%%", cache.getHitRatio()));

        System.out.println("\nThread Pool Metrics:");
        System.out.println("  Active Threads: " + threadPool.getActiveThreads());
        System.out.println("  Pool Size: " + threadPool.getPoolSize());
        System.out.println("  Completed Tasks: " + threadPool.getCompletedTasks());
        System.out.println("  Queue Size: " + threadPool.getQueueSize());
    }

    private static void demonstrateJMXOperations(
            ApplicationMetricsMBean metrics,
            CacheManagementMBean cache,
            ThreadPoolMBean threadPool) {
        
        System.out.println("\n=== Demonstrating JMX Operations ===");

        // Reset metrics
        System.out.println("\n1. Resetting application metrics...");
        metrics.resetMetrics();
        System.out.println("   Metrics reset. Total requests: " + metrics.getTotalRequests());

        // Clear cache
        System.out.println("\n2. Clearing cache...");
        cache.clearCache();
        System.out.println("   Cache cleared. Size: " + cache.getCacheSize());

        // Resize thread pool
        System.out.println("\n3. Resizing thread pool...");
        int oldSize = threadPool.getPoolSize();
        threadPool.setPoolSize(20);
        System.out.println("   Pool resized from " + oldSize + " to " + threadPool.getPoolSize());

        // Trigger health check
        System.out.println("\n4. Performing health check...");
        String health = metrics.performHealthCheck();
        System.out.println("   Health Status: " + health);
    }

    /**
     * Spring JMX Configuration
     */
    @Configuration
    @EnableMBeanExport
    public static class JMXConfig {

        @Bean
        public MBeanServerFactoryBean mbeanServer() {
            MBeanServerFactoryBean factory = new MBeanServerFactoryBean();
            factory.setLocateExistingServerIfPossible(true);
            return factory;
        }

        @Bean
        public ConnectorServerFactoryBean connectorServer() throws MalformedObjectNameException {
            ConnectorServerFactoryBean connectorServer = new ConnectorServerFactoryBean();
            connectorServer.setObjectName("connector:name=rmi");
            connectorServer.setServiceUrl("service:jmx:rmi:///jndi/rmi://localhost:9999/jmxrmi");
            return connectorServer;
        }

        @Bean
        public ApplicationMetricsMBean applicationMetrics() {
            return new ApplicationMetricsMBean();
        }

        @Bean
        public CacheManagementMBean cacheManagement() {
            return new CacheManagementMBean();
        }

        @Bean
        public ThreadPoolMBean threadPoolManagement() {
            return new ThreadPoolMBean();
        }
    }

    /**
     * Application Metrics MBean
     * Exposes application performance metrics via JMX
     */
    @ManagedResource(
        objectName = "com.example:type=Monitoring,name=ApplicationMetrics",
        description = "Application Performance Metrics"
    )
    @Component
    public static class ApplicationMetricsMBean {

        private final AtomicLong totalRequests = new AtomicLong(0);
        private final AtomicLong errorCount = new AtomicLong(0);
        private final AtomicLong totalResponseTime = new AtomicLong(0);
        private final long startTime = System.currentTimeMillis();

        @ManagedAttribute(description = "Total number of requests processed")
        public long getTotalRequests() {
            return totalRequests.get();
        }

        @ManagedAttribute(description = "Total number of errors")
        public long getErrorCount() {
            return errorCount.get();
        }

        @ManagedAttribute(description = "Average response time in milliseconds")
        public double getAverageResponseTime() {
            long total = totalRequests.get();
            return total == 0 ? 0 : (double) totalResponseTime.get() / total;
        }

        @ManagedAttribute(description = "Error rate as percentage")
        public double getErrorRate() {
            long total = totalRequests.get();
            return total == 0 ? 0 : (errorCount.get() * 100.0) / total;
        }

        @ManagedAttribute(description = "Application uptime in seconds")
        public long getUptime() {
            return (System.currentTimeMillis() - startTime) / 1000;
        }

        @ManagedOperation(description = "Reset all metrics")
        @ManagedOperationParameters({})
        public void resetMetrics() {
            totalRequests.set(0);
            errorCount.set(0);
            totalResponseTime.set(0);
        }

        @ManagedOperation(description = "Perform application health check")
        public String performHealthCheck() {
            double errorRate = getErrorRate();
            if (errorRate > 10) {
                return "UNHEALTHY - Error rate: " + String.format("%.2f%%", errorRate);
            } else if (errorRate > 5) {
                return "DEGRADED - Error rate: " + String.format("%.2f%%", errorRate);
            } else {
                return "HEALTHY - Error rate: " + String.format("%.2f%%", errorRate);
            }
        }

        // Internal methods (not exposed via JMX)
        public void recordRequest(long responseTime) {
            totalRequests.incrementAndGet();
            totalResponseTime.addAndGet(responseTime);
        }

        public void recordError() {
            errorCount.incrementAndGet();
        }
    }

    /**
     * Cache Management MBean
     * Exposes cache statistics and management operations
     */
    @ManagedResource(
        objectName = "com.example:type=Monitoring,name=CacheManagement",
        description = "Cache Management and Statistics"
    )
    @Component
    public static class CacheManagementMBean {

        private final Map<String, Object> cache = new HashMap<>();
        private final AtomicLong hitCount = new AtomicLong(0);
        private final AtomicLong missCount = new AtomicLong(0);

        @ManagedAttribute(description = "Current cache size")
        public int getCacheSize() {
            return cache.size();
        }

        @ManagedAttribute(description = "Cache hit count")
        public long getHitCount() {
            return hitCount.get();
        }

        @ManagedAttribute(description = "Cache miss count")
        public long getMissCount() {
            return missCount.get();
        }

        @ManagedAttribute(description = "Cache hit ratio as percentage")
        public double getHitRatio() {
            long total = hitCount.get() + missCount.get();
            return total == 0 ? 0 : (hitCount.get() * 100.0) / total;
        }

        @ManagedOperation(description = "Clear all cache entries")
        public void clearCache() {
            cache.clear();
        }

        @ManagedOperation(description = "Remove specific cache entry")
        @ManagedOperationParameters({
            @ManagedOperationParameter(name = "key", description = "Cache key to remove")
        })
        public boolean removeEntry(String key) {
            return cache.remove(key) != null;
        }

        @ManagedOperation(description = "Get cache statistics summary")
        public String getStatistics() {
            return String.format(
                "Size: %d, Hits: %d, Misses: %d, Ratio: %.2f%%",
                getCacheSize(), getHitCount(), getMissCount(), getHitRatio()
            );
        }

        // Internal methods
        public void recordCacheHit() {
            hitCount.incrementAndGet();
            cache.put("entry-" + System.nanoTime(), "data");
        }

        public void recordCacheMiss() {
            missCount.incrementAndGet();
        }
    }

    /**
     * Thread Pool Management MBean
     * Exposes thread pool metrics and configuration
     */
    @ManagedResource(
        objectName = "com.example:type=Monitoring,name=ThreadPool",
        description = "Thread Pool Monitoring and Management"
    )
    @Component
    public static class ThreadPoolMBean {

        private final AtomicInteger activeThreads = new AtomicInteger(0);
        private AtomicInteger poolSize = new AtomicInteger(10);
        private final AtomicLong completedTasks = new AtomicLong(0);
        private final AtomicInteger queueSize = new AtomicInteger(0);

        @ManagedAttribute(description = "Number of active threads")
        public int getActiveThreads() {
            return activeThreads.get();
        }

        @ManagedAttribute(description = "Current pool size")
        public int getPoolSize() {
            return poolSize.get();
        }

        @ManagedAttribute(description = "Set pool size", currencyTimeLimit = 20)
        public void setPoolSize(int size) {
            if (size < 1 || size > 100) {
                throw new IllegalArgumentException("Pool size must be between 1 and 100");
            }
            poolSize.set(size);
        }

        @ManagedAttribute(description = "Total completed tasks")
        public long getCompletedTasks() {
            return completedTasks.get();
        }

        @ManagedAttribute(description = "Current queue size")
        public int getQueueSize() {
            return queueSize.get();
        }

        @ManagedMetric(
            description = "Thread pool utilization percentage",
            category = "utilization",
            metricType = MetricType.GAUGE
        )
        public double getUtilization() {
            int pool = poolSize.get();
            return pool == 0 ? 0 : (activeThreads.get() * 100.0) / pool;
        }

        @ManagedOperation(description = "Shutdown thread pool gracefully")
        public String shutdown() {
            int active = activeThreads.get();
            activeThreads.set(0);
            return "Thread pool shutdown. " + active + " threads were active.";
        }

        // Internal methods
        public void submitTask() {
            if (activeThreads.get() < poolSize.get()) {
                activeThreads.incrementAndGet();
            } else {
                queueSize.incrementAndGet();
            }
        }

        public void completeTask() {
            if (activeThreads.get() > 0) {
                activeThreads.decrementAndGet();
                completedTasks.incrementAndGet();
                
                // Process from queue
                if (queueSize.get() > 0) {
                    queueSize.decrementAndGet();
                    activeThreads.incrementAndGet();
                }
            }
        }
    }

    /**
     * Custom MBean using standard JMX (not Spring annotations)
     * Demonstrates programmatic MBean registration
     */
    public static class CustomMBean implements DynamicMBean {

        private final Map<String, Object> attributes = new HashMap<>();

        public CustomMBean() {
            attributes.put("Status", "Running");
            attributes.put("Version", "1.0.0");
        }

        @Override
        public Object getAttribute(String attribute) throws AttributeNotFoundException {
            if (!attributes.containsKey(attribute)) {
                throw new AttributeNotFoundException("Attribute " + attribute + " not found");
            }
            return attributes.get(attribute);
        }

        @Override
        public void setAttribute(Attribute attribute) throws AttributeNotFoundException {
            String name = attribute.getName();
            if (!attributes.containsKey(name)) {
                throw new AttributeNotFoundException("Attribute " + name + " not found");
            }
            attributes.put(name, attribute.getValue());
        }

        @Override
        public AttributeList getAttributes(String[] attributes) {
            AttributeList list = new AttributeList();
            for (String attr : attributes) {
                try {
                    list.add(new Attribute(attr, getAttribute(attr)));
                } catch (AttributeNotFoundException e) {
                    // Skip invalid attributes
                }
            }
            return list;
        }

        @Override
        public AttributeList setAttributes(AttributeList attributes) {
            AttributeList list = new AttributeList();
            for (Object obj : attributes) {
                Attribute attr = (Attribute) obj;
                try {
                    setAttribute(attr);
                    list.add(attr);
                } catch (AttributeNotFoundException e) {
                    // Skip invalid attributes
                }
            }
            return list;
        }

        @Override
        public Object invoke(String actionName, Object[] params, String[] signature) 
                throws MBeanException, ReflectionException {
            if ("restart".equals(actionName)) {
                attributes.put("Status", "Restarting");
                return "Application restarted";
            }
            throw new ReflectionException(new NoSuchMethodException(actionName));
        }

        @Override
        public MBeanInfo getMBeanInfo() {
            MBeanAttributeInfo[] attrs = new MBeanAttributeInfo[] {
                new MBeanAttributeInfo("Status", "java.lang.String", "Application status", true, true, false),
                new MBeanAttributeInfo("Version", "java.lang.String", "Application version", true, false, false)
            };

            MBeanOperationInfo[] ops = new MBeanOperationInfo[] {
                new MBeanOperationInfo("restart", "Restart application", null, "java.lang.String", MBeanOperationInfo.ACTION)
            };

            return new MBeanInfo(
                this.getClass().getName(),
                "Custom Application MBean",
                attrs,
                null,
                ops,
                null
            );
        }
    }
}
