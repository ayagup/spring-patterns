package com.example.beanpostprocessor;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Destruction Callback Pattern
 * 
 * Demonstrates bean destruction callbacks and cleanup operations
 * using @PreDestroy, DisposableBean, and DestructionAwareBeanPostProcessor.
 * 
 * Key Concepts:
 * - @PreDestroy annotation
 * - DisposableBean interface
 * - @Bean(destroyMethod)
 * - DestructionAwareBeanPostProcessor
 * - Resource cleanup
 * 
 * Use Cases:
 * - Closing connections
 * - Releasing resources
 * - Flushing caches
 * - Stopping background tasks
 * - Cleanup operations
 */
@SpringBootApplication
public class DestructionCallbackPattern {

    public static void main(String[] args) {
        SpringApplication.run(DestructionCallbackPattern.class, args);
    }
}

/**
 * DestructionAwareBeanPostProcessor for tracking cleanup
 */
@Component
class DestructionTrackingBeanPostProcessor implements DestructionAwareBeanPostProcessor {

    private static final List<String> destructionLog = new ArrayList<>();

    @Override
    public void postProcessBeforeDestruction(Object bean, String beanName) throws BeansException {
        if (bean instanceof CleanupDemo) {
            String log = "DestructionAwareBeanPostProcessor: Preparing destruction of " + beanName;
            destructionLog.add(log);
            System.out.println(log);
        }
    }

    @Override
    public boolean requiresDestruction(Object bean) {
        return bean instanceof CleanupDemo;
    }

    public static List<String> getDestructionLog() {
        return new ArrayList<>(destructionLog);
    }
}

/**
 * Marker interface for tracking
 */
interface CleanupDemo {
    String getStatus();
}

/**
 * Bean demonstrating all destruction callbacks
 */
@Component
class FullDestructionBean implements org.springframework.beans.factory.DisposableBean, CleanupDemo {

    private String status = "Active";
    private boolean preDestroyCalled = false;
    private boolean destroyCalled = false;
    private boolean customDestroyCalled = false;

    @PreDestroy
    public void preDestroy() {
        preDestroyCalled = true;
        status = "PreDestroy called";
        String log = "1. @PreDestroy method called";
        DestructionTrackingBeanPostProcessor.destructionLog.add(log);
        System.out.println(log);
    }

    @Override
    public void destroy() throws Exception {
        destroyCalled = true;
        status = "Destroy called";
        String log = "2. DisposableBean.destroy() called";
        DestructionTrackingBeanPostProcessor.destructionLog.add(log);
        System.out.println(log);
    }

    public void customDestroyMethod() {
        customDestroyCalled = true;
        status = "Custom destroy called";
        String log = "3. @Bean(destroyMethod) called";
        DestructionTrackingBeanPostProcessor.destructionLog.add(log);
        System.out.println(log);
    }

    @Override
    public String getStatus() {
        return status;
    }

    public Map<String, Boolean> getDestructionStatus() {
        return Map.of(
                "preDestroy", preDestroyCalled,
                "destroy", destroyCalled,
                "customDestroy", customDestroyCalled
        );
    }
}

/**
 * Bean with connection cleanup
 */
@Component
class ConnectionBean implements CleanupDemo {

    private boolean connectionOpen = true;
    private String status = "Connected";

    @PreDestroy
    public void closeConnection() {
        if (connectionOpen) {
            System.out.println("Closing connection...");
            connectionOpen = false;
            status = "Connection closed";
        }
    }

    @Override
    public String getStatus() {
        return status + " (open: " + connectionOpen + ")";
    }

    public boolean isConnectionOpen() {
        return connectionOpen;
    }
}

/**
 * Bean with resource cleanup
 */
@Component
class ResourceBean implements org.springframework.beans.factory.DisposableBean, CleanupDemo {

    private final List<String> resources = new ArrayList<>();
    private String status = "Resources allocated";

    public ResourceBean() {
        // Simulate resource allocation
        resources.add("resource1");
        resources.add("resource2");
        resources.add("resource3");
    }

    @Override
    public void destroy() {
        System.out.println("Releasing " + resources.size() + " resources");
        resources.clear();
        status = "Resources released";
    }

    @Override
    public String getStatus() {
        return status + " (count: " + resources.size() + ")";
    }
}

/**
 * Bean with cache cleanup
 */
@Component
class CacheBean implements CleanupDemo {

    private final List<String> cache = new ArrayList<>();
    private boolean flushed = false;
    private String status = "Cache active";

    public CacheBean() {
        // Simulate cache
        cache.add("cached-item-1");
        cache.add("cached-item-2");
    }

    @PreDestroy
    public void flushCache() {
        System.out.println("Flushing cache with " + cache.size() + " items");
        cache.clear();
        flushed = true;
        status = "Cache flushed";
    }

    @Override
    public String getStatus() {
        return status + " (items: " + cache.size() + ", flushed: " + flushed + ")";
    }
}

/**
 * Bean with background task cleanup
 */
@Component
class BackgroundTaskBean implements CleanupDemo {

    private boolean taskRunning = true;
    private String status = "Task running";

    @PreDestroy
    public void stopTask() {
        if (taskRunning) {
            System.out.println("Stopping background task...");
            taskRunning = false;
            status = "Task stopped";
        }
    }

    @Override
    public String getStatus() {
        return status + " (running: " + taskRunning + ")";
    }
}

/**
 * Bean with file handle cleanup
 */
@Component
class FileHandleBean implements org.springframework.beans.factory.DisposableBean, CleanupDemo {

    private boolean fileOpen = true;
    private String fileName = "data.txt";
    private String status = "File open";

    @PreDestroy
    public void closeFile() {
        if (fileOpen) {
            System.out.println("Closing file: " + fileName);
            fileOpen = false;
        }
    }

    @Override
    public void destroy() {
        System.out.println("Final cleanup for file: " + fileName);
        status = "File closed and cleanup complete";
    }

    @Override
    public String getStatus() {
        return status + " (file: " + fileName + ", open: " + fileOpen + ")";
    }
}

/**
 * Controller to check cleanup status
 */
@RestController
class DestructionController {

    private final FullDestructionBean fullBean;
    private final ConnectionBean connectionBean;
    private final ResourceBean resourceBean;
    private final CacheBean cacheBean;
    private final BackgroundTaskBean taskBean;
    private final FileHandleBean fileBean;

    public DestructionController(FullDestructionBean fullBean,
                                ConnectionBean connectionBean,
                                ResourceBean resourceBean,
                                CacheBean cacheBean,
                                BackgroundTaskBean taskBean,
                                FileHandleBean fileBean) {
        this.fullBean = fullBean;
        this.connectionBean = connectionBean;
        this.resourceBean = resourceBean;
        this.cacheBean = cacheBean;
        this.taskBean = taskBean;
        this.fileBean = fileBean;
    }

    @GetMapping("/destruction/log")
    public List<String> getDestructionLog() {
        return DestructionTrackingBeanPostProcessor.getDestructionLog();
    }

    @GetMapping("/destruction/full-bean")
    public Map<String, Object> getFullBean() {
        return Map.of(
                "status", fullBean.getStatus(),
                "destructionStatus", fullBean.getDestructionStatus()
        );
    }

    @GetMapping("/destruction/connection-bean")
    public Map<String, Object> getConnectionBean() {
        return Map.of(
                "status", connectionBean.getStatus(),
                "open", connectionBean.isConnectionOpen()
        );
    }

    @GetMapping("/destruction/resource-bean")
    public Map<String, String> getResourceBean() {
        return Map.of("status", resourceBean.getStatus());
    }

    @GetMapping("/destruction/cache-bean")
    public Map<String, String> getCacheBean() {
        return Map.of("status", cacheBean.getStatus());
    }

    @GetMapping("/destruction/task-bean")
    public Map<String, String> getTaskBean() {
        return Map.of("status", taskBean.getStatus());
    }

    @GetMapping("/destruction/file-bean")
    public Map<String, String> getFileBean() {
        return Map.of("status", fileBean.getStatus());
    }
}

/**
 * Documentation:
 * 
 * Bean Destruction Order (reverse of initialization):
 * 1. DestructionAwareBeanPostProcessor.postProcessBeforeDestruction
 * 2. @PreDestroy methods
 * 3. DisposableBean.destroy()
 * 4. @Bean(destroyMethod = "methodName")
 * 
 * Destruction Mechanisms:
 * 
 * 1. @PreDestroy:
 *    - JSR-250 annotation
 *    - Method-level
 *    - Runs first
 *    - Most common approach
 *    - Cannot have parameters
 * 
 * 2. DisposableBean interface:
 *    - Spring-specific
 *    - destroy() method
 *    - Couples code to Spring
 *    - Runs after @PreDestroy
 * 
 * 3. @Bean(destroyMethod):
 *    - Configuration-based
 *    - Decouples bean from Spring
 *    - Runs last
 *    - Default: "close" or "shutdown"
 * 
 * 4. DestructionAwareBeanPostProcessor:
 *    - Most powerful
 *    - Custom destruction logic
 *    - Runs before other callbacks
 * 
 * Examples:
 * 
 * 1. @PreDestroy:
 *    @Component
 *    class MyBean {
 *        @PreDestroy
 *        public void cleanup() {
 *            // Cleanup resources
 *        }
 *    }
 * 
 * 2. DisposableBean:
 *    @Component
 *    class MyBean implements DisposableBean {
 *        @Override
 *        public void destroy() {
 *            // Cleanup resources
 *        }
 *    }
 * 
 * 3. @Bean(destroyMethod):
 *    @Configuration
 *    class Config {
 *        @Bean(destroyMethod = "cleanup")
 *        public MyBean myBean() {
 *            return new MyBean();
 *        }
 *    }
 * 
 *    class MyBean {
 *        public void cleanup() {
 *            // Cleanup resources
 *        }
 *    }
 * 
 * Common Use Cases:
 * 
 * 1. Connection Cleanup:
 *    @PreDestroy
 *    public void closeConnection() {
 *        if (connection != null) {
 *            connection.close();
 *        }
 *    }
 * 
 * 2. Thread Pool Shutdown:
 *    @PreDestroy
 *    public void shutdownExecutor() {
 *        executor.shutdown();
 *        executor.awaitTermination(5, TimeUnit.SECONDS);
 *    }
 * 
 * 3. Cache Flush:
 *    @PreDestroy
 *    public void flushCache() {
 *        cache.flush();
 *        cache.clear();
 *    }
 * 
 * 4. File Closure:
 *    @PreDestroy
 *    public void closeFiles() {
 *        IOUtils.closeQuietly(fileHandle);
 *    }
 * 
 * 5. Metrics Export:
 *    @PreDestroy
 *    public void exportMetrics() {
 *        metricsCollector.flush();
 *    }
 * 
 * Best Practices:
 * - Always cleanup resources
 * - Handle exceptions gracefully
 * - Use try-catch in cleanup
 * - Log cleanup operations
 * - Set timeouts for shutdown
 * - Don't throw exceptions from @PreDestroy
 * - Make cleanup idempotent
 * - Test cleanup logic
 * 
 * Error Handling:
 * @PreDestroy
 * public void cleanup() {
 *     try {
 *         // Cleanup
 *     } catch (Exception e) {
 *         logger.error("Cleanup failed", e);
 *         // Don't rethrow
 *     }
 * }
 * 
 * Graceful Shutdown:
 * @PreDestroy
 * public void gracefulShutdown() {
 *     executor.shutdown();
 *     try {
 *         if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
 *             executor.shutdownNow();
 *         }
 *     } catch (InterruptedException e) {
 *         executor.shutdownNow();
 *     }
 * }
 * 
 * When Destruction Callbacks Run:
 * - Application shutdown
 * - Context close
 * - Bean scope destruction (prototype on explicit destroy)
 * - @RefreshScope refresh
 * 
 * Singleton vs Prototype:
 * - Singleton: Cleanup automatic
 * - Prototype: Must call destroy manually or use scope proxy
 * 
 * Disable Default Destroy Method:
 * @Bean(destroyMethod = "")
 * public DataSource dataSource() {
 *     // Spring won't call close() automatically
 * }
 * 
 * Testing:
 * - Test cleanup logic separately
 * - Verify resources released
 * - Test with context.close()
 * - Mock cleanup dependencies
 * 
 * Performance:
 * - Cleanup can slow shutdown
 * - Set reasonable timeouts
 * - Consider async cleanup
 * - Prioritize critical cleanup
 * 
 * Common Pitfalls:
 * - Throwing exceptions from @PreDestroy
 * - Not handling null references
 * - Long-running cleanup blocking shutdown
 * - Not testing cleanup code
 * - Circular dependencies in cleanup
 * - Forgetting to cleanup prototype beans
 */
