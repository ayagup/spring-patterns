package com.spring.patterns.scope;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.NamedThreadLocal;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread Scope Pattern
 * 
 * Thread scope creates bean instances bound to specific threads.
 * Each thread gets its own instance via ThreadLocal storage.
 * 
 * Characteristics:
 * - One instance per thread
 * - Thread-local storage
 * - No cross-thread visibility
 * - Destroyed when thread completes
 * 
 * Use Cases:
 * - Thread-specific configuration
 * - Thread-local caching
 * - Async task context
 * - Thread-bound transactions
 * - Per-thread metrics
 */
@SpringBootApplication
public class ThreadScopePattern {

    public static void main(String[] args) {
        SpringApplication.run(ThreadScopePattern.class, args);
        System.out.println("\n=== Thread Scope Pattern Started ===");
    }
}

/**
 * Thread Scope implementation
 */
@Component("threadScope")
class SimpleThreadScope implements Scope {
    
    private final ThreadLocal<Map<String, Object>> threadScope = 
        new NamedThreadLocal<Map<String, Object>>("SimpleThreadScope") {
            @Override
            protected Map<String, Object> initialValue() {
                return new HashMap<>();
            }
        };
    
    private final ThreadLocal<Map<String, Runnable>> destructionCallbacks = 
        new NamedThreadLocal<Map<String, Runnable>>("SimpleThreadScope-destructionCallbacks") {
            @Override
            protected Map<String, Runnable> initialValue() {
                return new HashMap<>();
            }
        };
    
    @Override
    public Object get(String name, ObjectFactory<?> objectFactory) {
        Map<String, Object> scope = threadScope.get();
        return scope.computeIfAbsent(name, k -> {
            System.out.println("Creating thread-scoped bean '" + name + 
                             "' for thread: " + Thread.currentThread().getName());
            return objectFactory.getObject();
        });
    }
    
    @Override
    public Object remove(String name) {
        Map<String, Object> scope = threadScope.get();
        destructionCallbacks.get().remove(name);
        return scope.remove(name);
    }
    
    @Override
    public void registerDestructionCallback(String name, Runnable callback) {
        destructionCallbacks.get().put(name, callback);
    }
    
    @Override
    public Object resolveContextualObject(String key) {
        return null;
    }
    
    @Override
    public String getConversationId() {
        return Thread.currentThread().getName();
    }
    
    public void clearThread() {
        Map<String, Runnable> callbacks = destructionCallbacks.get();
        callbacks.values().forEach(Runnable::run);
        callbacks.clear();
        threadScope.get().clear();
    }
}

/**
 * Thread scope configuration
 */
@Configuration
class ThreadScopeConfig {
    
    @Bean
    public static org.springframework.beans.factory.config.CustomScopeConfigurer threadScopeConfigurer(
            SimpleThreadScope threadScope) {
        org.springframework.beans.factory.config.CustomScopeConfigurer configurer = 
            new org.springframework.beans.factory.config.CustomScopeConfigurer();
        
        Map<String, Object> scopes = new HashMap<>();
        scopes.put("thread", threadScope);
        configurer.setScopes(scopes);
        
        return configurer;
    }
    
    @Bean
    @org.springframework.context.annotation.Scope(value = "thread", proxyMode = ScopedProxyMode.TARGET_CLASS)
    public ThreadLocalContext threadLocalContext() {
        return new ThreadLocalContext();
    }
    
    @Bean
    @org.springframework.context.annotation.Scope("thread")
    public ThreadMetrics threadMetrics() {
        return new ThreadMetrics();
    }
}

/**
 * Thread-scoped context bean
 */
class ThreadLocalContext {
    private final String contextId;
    private final String threadName;
    private final LocalDateTime createdAt;
    private final Map<String, Object> attributes = new HashMap<>();
    
    public ThreadLocalContext() {
        this.contextId = "THREAD-" + UUID.randomUUID().toString().substring(0, 8);
        this.threadName = Thread.currentThread().getName();
        this.createdAt = LocalDateTime.now();
        System.out.println("ThreadLocalContext created: " + contextId + 
                         " on thread: " + threadName);
    }
    
    public String getContextId() { return contextId; }
    public String getThreadName() { return threadName; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    
    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }
    
    public Object getAttribute(String key) {
        return attributes.get(key);
    }
}

/**
 * Thread-scoped metrics bean
 */
class ThreadMetrics {
    private final String metricsId;
    private final LocalDateTime startTime;
    private int operationCount = 0;
    
    public ThreadMetrics() {
        this.metricsId = "METRICS-" + System.currentTimeMillis();
        this.startTime = LocalDateTime.now();
        System.out.println("ThreadMetrics created: " + metricsId);
    }
    
    public void incrementOperations() {
        operationCount++;
    }
    
    public String getMetricsId() { return metricsId; }
    public LocalDateTime getStartTime() { return startTime; }
    public int getOperationCount() { return operationCount; }
}

/**
 * Refresh Scope Pattern (Spring Cloud)
 * 
 * Refresh scope allows beans to be recreated when configuration changes.
 * Used primarily in Spring Cloud Config for dynamic configuration updates.
 * 
 * Note: This is a simplified example. Full implementation requires Spring Cloud dependencies.
 */
@Component("refreshScope")
class SimpleRefreshScope implements Scope {
    
    private final Map<String, Object> scopedObjects = new ConcurrentHashMap<>();
    private final Map<String, Runnable> destructionCallbacks = new ConcurrentHashMap<>();
    
    @Override
    public Object get(String name, ObjectFactory<?> objectFactory) {
        return scopedObjects.computeIfAbsent(name, k -> {
            System.out.println("Creating refresh-scoped bean: " + name);
            return objectFactory.getObject();
        });
    }
    
    @Override
    public Object remove(String name) {
        Runnable callback = destructionCallbacks.remove(name);
        if (callback != null) {
            callback.run();
        }
        return scopedObjects.remove(name);
    }
    
    @Override
    public void registerDestructionCallback(String name, Runnable callback) {
        destructionCallbacks.put(name, callback);
    }
    
    @Override
    public Object resolveContextualObject(String key) {
        return null;
    }
    
    @Override
    public String getConversationId() {
        return "refresh";
    }
    
    /**
     * Refresh all beans in this scope
     */
    public void refreshAll() {
        System.out.println("Refreshing all refresh-scoped beans...");
        destructionCallbacks.values().forEach(Runnable::run);
        destructionCallbacks.clear();
        scopedObjects.clear();
    }
    
    /**
     * Refresh specific bean
     */
    public void refresh(String name) {
        System.out.println("Refreshing bean: " + name);
        Runnable callback = destructionCallbacks.remove(name);
        if (callback != null) {
            callback.run();
        }
        scopedObjects.remove(name);
    }
}

/**
 * Refresh scope configuration
 */
@Configuration
class RefreshScopeConfig {
    
    @Bean
    public static org.springframework.beans.factory.config.CustomScopeConfigurer refreshScopeConfigurer(
            SimpleRefreshScope refreshScope) {
        org.springframework.beans.factory.config.CustomScopeConfigurer configurer = 
            new org.springframework.beans.factory.config.CustomScopeConfigurer();
        
        Map<String, Object> scopes = new HashMap<>();
        scopes.put("refresh", refreshScope);
        configurer.setScopes(scopes);
        
        return configurer;
    }
    
    @Bean
    @org.springframework.context.annotation.Scope(value = "refresh", proxyMode = ScopedProxyMode.TARGET_CLASS)
    public DynamicConfiguration dynamicConfiguration() {
        return new DynamicConfiguration();
    }
}

/**
 * Refresh-scoped configuration bean
 */
class DynamicConfiguration {
    private final String configId;
    private final LocalDateTime loadedAt;
    private String apiUrl = "http://localhost:8080/api";
    private int timeout = 30;
    
    public DynamicConfiguration() {
        this.configId = "CONFIG-" + System.currentTimeMillis();
        this.loadedAt = LocalDateTime.now();
        System.out.println("DynamicConfiguration loaded: " + configId + " at " + loadedAt);
    }
    
    public String getConfigId() { return configId; }
    public LocalDateTime getLoadedAt() { return loadedAt; }
    public String getApiUrl() { return apiUrl; }
    public int getTimeout() { return timeout; }
}

/**
 * REST Controller
 */
@RestController
@RequestMapping("/api/thread-refresh")
class ThreadRefreshController {
    
    private final ThreadLocalContext threadContext;
    private final ThreadMetrics threadMetrics;
    private final DynamicConfiguration config;
    private final SimpleRefreshScope refreshScope;
    
    public ThreadRefreshController(ThreadLocalContext threadContext,
                                  ThreadMetrics threadMetrics,
                                  DynamicConfiguration config,
                                  SimpleRefreshScope refreshScope) {
        this.threadContext = threadContext;
        this.threadMetrics = threadMetrics;
        this.config = config;
        this.refreshScope = refreshScope;
    }
    
    @GetMapping("/thread-info")
    public String getThreadInfo() {
        threadMetrics.incrementOperations();
        threadContext.setAttribute("lastAccess", LocalDateTime.now());
        
        return "Thread Context:\n" +
               "  Context ID: " + threadContext.getContextId() + "\n" +
               "  Thread: " + threadContext.getThreadName() + "\n" +
               "  Created: " + threadContext.getCreatedAt() + "\n" +
               "  Operations: " + threadMetrics.getOperationCount();
    }
    
    @GetMapping("/config")
    public String getConfig() {
        return "Dynamic Configuration:\n" +
               "  Config ID: " + config.getConfigId() + "\n" +
               "  Loaded at: " + config.getLoadedAt() + "\n" +
               "  API URL: " + config.getApiUrl() + "\n" +
               "  Timeout: " + config.getTimeout();
    }
    
    @PostMapping("/refresh")
    public String refresh() {
        refreshScope.refreshAll();
        return "Configuration refreshed";
    }
}

/**
 * Key Points:
 * 
 * 1. Thread Scope:
 *    - One bean instance per thread
 *    - Uses ThreadLocal storage
 *    - Isolated across threads
 *    - Manual cleanup required
 * 
 * 2. Refresh Scope:
 *    - Beans recreated on demand
 *    - Used for dynamic configuration
 *    - Spring Cloud feature
 *    - Trigger via /actuator/refresh
 * 
 * 3. Thread Scope Use Cases:
 *    ✓ Thread-specific context
 *    ✓ Async task processing
 *    ✓ Thread-local caching
 *    ✓ Per-thread metrics
 * 
 * 4. Refresh Scope Use Cases:
 *    ✓ Dynamic configuration
 *    ✓ Feature toggles
 *    ✓ A/B testing
 *    ✓ Runtime config changes
 */
