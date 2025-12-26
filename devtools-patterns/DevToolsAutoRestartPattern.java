package com.example.devtools.patterns;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.devtools.restart.RestartScope;
import org.springframework.boot.devtools.restart.Restarter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 🔄 SPRING BOOT DEVTOOLS - AUTO-RESTART PATTERN 🔄
 * ==================================================
 * 
 * Demonstrates Spring Boot DevTools automatic application restart on classpath
 * changes. DevTools uses a dual classloader strategy to achieve fast restarts
 * during development, significantly improving developer productivity.
 * 
 * 🎯 KEY CONCEPTS:
 * ===============
 * 
 * 1️⃣ DUAL CLASSLOADER STRATEGY:
 *    - Base Classloader: Loads third-party JARs (rarely change)
 *    - Restart Classloader: Loads application classes (frequently change)
 *    - On change: Only restart classloader is recreated
 *    - Result: Restart in 1-2 seconds vs 30+ seconds
 * 
 * 2️⃣ AUTOMATIC RESTART TRIGGERS:
 *    - File changes in classpath
 *    - IDE auto-compile
 *    - Maven/Gradle build
 *    - Configurable triggers
 * 
 * 3️⃣ RESTART SCOPE:
 *    - @RestartScope annotation for beans
 *    - Beans survive restart
 *    - Maintain state across restarts
 *    - Useful for caches, connections
 * 
 * 4️⃣ EXCLUDE PATTERNS:
 *    - Exclude static resources
 *    - Exclude templates
 *    - Custom exclusions
 *    - Prevent unnecessary restarts
 * 
 * 📦 DEPENDENCIES:
 * ===============
 * <dependency>
 *     <groupId>org.springframework.boot</groupId>
 *     <artifactId>spring-boot-devtools</artifactId>
 *     <optional>true</optional>
 *     <scope>runtime</scope>
 * </dependency>
 * 
 * ⚙️ CONFIGURATION (application.yml):
 * ===================================
 * spring:
 *   devtools:
 *     restart:
 *       enabled: true                    # Enable auto-restart (default: true)
 *       poll-interval: 1s                # Check for changes every 1 second
 *       quiet-period: 400ms              # Wait 400ms after last change before restart
 *       additional-paths:                # Additional directories to watch
 *         - src/main/resources
 *         - src/main/webapp
 *       additional-exclude: custom/**    # Additional exclusions
 *       exclude:                         # Default exclusions
 *         - META-INF/maven/**
 *         - META-INF/resources/**
 *         - resources/**
 *         - static/**
 *         - public/**
 *         - templates/**
 *         - **/*Test.class
 *         - **/*Tests.class
 *       log-condition-evaluation-delta: true  # Log auto-config changes
 *       trigger-file: .trigger           # Manual restart trigger file
 * 
 * 🚀 HOW IT WORKS:
 * ===============
 * 1. DevTools monitors classpath for changes
 * 2. On change detection, waits for quiet period (400ms default)
 * 3. Stops current ApplicationContext
 * 4. Recreates restart classloader with new classes
 * 5. Starts new ApplicationContext
 * 6. Total time: 1-2 seconds (vs 30+ seconds cold start)
 * 
 * 🔄 RESTART LIFECYCLE:
 * ====================
 * 1. File Change Detected
 * 2. Quiet Period Wait (aggregates multiple changes)
 * 3. Pre-Shutdown (@PreDestroy methods)
 * 4. ApplicationContext Close
 * 5. Restart Classloader Recreate
 * 6. ApplicationContext Restart
 * 7. Post-Startup (@PostConstruct methods)
 * 8. Ready for Requests
 * 
 * ⚠️ IMPORTANT NOTES:
 * ==================
 * - Only works in development (automatic disable in production JAR)
 * - Requires IDE auto-compile or build tool watch mode
 * - Not suitable for production (performance overhead)
 * - State loss unless using @RestartScope
 * - May cause issues with JPA entity caching
 * - Memory leaks if references held to old classloader
 * 
 * 💡 WHEN TO USE:
 * ==============
 * ✅ Local development
 * ✅ Rapid prototyping
 * ✅ Backend API development
 * ✅ Microservices development
 * ✅ Spring Boot applications
 * 
 * ❌ WHEN NOT TO USE:
 * ==================
 * ❌ Production environments
 * ❌ CI/CD pipelines
 * ❌ Performance testing
 * ❌ Load testing
 * ❌ Security testing (may leak sensitive data)
 * ❌ Docker containers (use volume mounts carefully)
 * 
 * @author Spring Patterns
 * @version 1.0
 * @since 2024-01-20
 */
@SpringBootApplication
public class DevToolsAutoRestartPattern {

    public static void main(String[] args) {
        SpringApplication.run(DevToolsAutoRestartPattern.class, args);
    }
}

/**
 * DevTools Auto-Restart Configuration
 */
@Configuration
@Profile("dev")
class DevToolsRestartConfiguration {

    /**
     * Restart Statistics Bean
     * Survives restarts to track restart count
     */
    @Bean
    @RestartScope  // Bean survives application restarts
    public RestartStatistics restartStatistics() {
        return new RestartStatistics();
    }

    /**
     * Restart Monitor Bean
     * Monitors restart events
     */
    @Bean
    public RestartMonitor restartMonitor(RestartStatistics statistics) {
        return new RestartMonitor(statistics);
    }

    /**
     * Classloader Info Service
     * Provides information about classloaders
     */
    @Bean
    public ClassloaderInfoService classloaderInfoService() {
        return new ClassloaderInfoService();
    }
}

/**
 * Restart Statistics
 * Tracks restart count and timing (survives restarts with @RestartScope)
 */
@RestartScope
class RestartStatistics {
    private final AtomicInteger restartCount = new AtomicInteger(0);
    private LocalDateTime firstStartTime = LocalDateTime.now();
    private LocalDateTime lastRestartTime = LocalDateTime.now();
    private final List<RestartEvent> restartHistory = new ArrayList<>();

    public void recordRestart() {
        int count = restartCount.incrementAndGet();
        lastRestartTime = LocalDateTime.now();
        
        RestartEvent event = new RestartEvent(count, lastRestartTime);
        restartHistory.add(event);
        
        System.out.println("🔄 Application Restart #" + count + " at " + lastRestartTime);
    }

    public int getRestartCount() {
        return restartCount.get();
    }

    public LocalDateTime getFirstStartTime() {
        return firstStartTime;
    }

    public LocalDateTime getLastRestartTime() {
        return lastRestartTime;
    }

    public Duration getTotalUptime() {
        return Duration.between(firstStartTime, LocalDateTime.now());
    }

    public List<RestartEvent> getRestartHistory() {
        return new ArrayList<>(restartHistory);
    }

    public void reset() {
        restartCount.set(0);
        firstStartTime = LocalDateTime.now();
        lastRestartTime = LocalDateTime.now();
        restartHistory.clear();
    }
}

/**
 * Restart Event
 */
class RestartEvent {
    private final int restartNumber;
    private final LocalDateTime timestamp;

    public RestartEvent(int restartNumber, LocalDateTime timestamp) {
        this.restartNumber = restartNumber;
        this.timestamp = timestamp;
    }

    public int getRestartNumber() { return restartNumber; }
    public LocalDateTime getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return String.format("Restart #%d at %s", restartNumber, timestamp);
    }
}

/**
 * Restart Monitor
 * Monitors application restart lifecycle
 */
@Service
class RestartMonitor {

    private final RestartStatistics statistics;
    private LocalDateTime startupTime;

    public RestartMonitor(RestartStatistics statistics) {
        this.statistics = statistics;
    }

    @PostConstruct
    public void onStartup() {
        startupTime = LocalDateTime.now();
        statistics.recordRestart();
        
        System.out.println("✅ Application Started");
        System.out.println("   - Restart Count: " + statistics.getRestartCount());
        System.out.println("   - Startup Time: " + startupTime);
        System.out.println("   - Total Uptime: " + statistics.getTotalUptime());
    }

    @PreDestroy
    public void onShutdown() {
        Duration sessionDuration = Duration.between(startupTime, LocalDateTime.now());
        
        System.out.println("🛑 Application Shutting Down for Restart");
        System.out.println("   - Session Duration: " + sessionDuration.getSeconds() + "s");
        System.out.println("   - Total Restarts: " + statistics.getRestartCount());
    }

    public Map<String, Object> getStatus() {
        Map<String, Object> status = new ConcurrentHashMap<>();
        status.put("restartCount", statistics.getRestartCount());
        status.put("firstStartTime", statistics.getFirstStartTime());
        status.put("lastRestartTime", statistics.getLastRestartTime());
        status.put("totalUptime", statistics.getTotalUptime().getSeconds() + "s");
        status.put("currentSessionStartTime", startupTime);
        status.put("currentSessionDuration", 
            Duration.between(startupTime, LocalDateTime.now()).getSeconds() + "s");
        return status;
    }
}

/**
 * Classloader Information Service
 * Provides details about base and restart classloaders
 */
@Service
class ClassloaderInfoService {

    /**
     * Get information about current classloader hierarchy
     */
    public Map<String, Object> getClassloaderInfo() {
        Map<String, Object> info = new ConcurrentHashMap<>();
        
        ClassLoader currentClassLoader = getClass().getClassLoader();
        
        List<Map<String, String>> hierarchy = new ArrayList<>();
        ClassLoader cl = currentClassLoader;
        int level = 0;
        
        while (cl != null) {
            Map<String, String> clInfo = new ConcurrentHashMap<>();
            clInfo.put("level", String.valueOf(level));
            clInfo.put("type", cl.getClass().getSimpleName());
            clInfo.put("name", cl.getName());
            clInfo.put("toString", cl.toString());
            hierarchy.add(clInfo);
            
            cl = cl.getParent();
            level++;
        }
        
        info.put("currentClassLoader", currentClassLoader.getClass().getName());
        info.put("hierarchy", hierarchy);
        info.put("isRestartClassLoader", 
            currentClassLoader.getClass().getName().contains("RestartClassLoader"));
        
        return info;
    }

    /**
     * Get loaded classes count (approximate)
     */
    public Map<String, Object> getLoadedClassesInfo() {
        Map<String, Object> info = new ConcurrentHashMap<>();
        
        ClassLoader classLoader = getClass().getClassLoader();
        info.put("classLoader", classLoader.getClass().getSimpleName());
        info.put("timestamp", LocalDateTime.now());
        
        return info;
    }

    /**
     * Check if running with DevTools restart enabled
     */
    public boolean isRestartEnabled() {
        try {
            Class.forName("org.springframework.boot.devtools.restart.Restarter");
            Restarter restarter = Restarter.getInstance();
            return restarter != null && restarter.getInitialUrls() != null;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Get restart URLs (classpath URLs being monitored)
     */
    public List<String> getRestartUrls() {
        try {
            Restarter restarter = Restarter.getInstance();
            if (restarter != null && restarter.getInitialUrls() != null) {
                return Arrays.stream(restarter.getInitialUrls())
                    .map(Object::toString)
                    .collect(Collectors.toList());
            }
        } catch (Exception e) {
            System.err.println("Error getting restart URLs: " + e.getMessage());
        }
        return Collections.emptyList();
    }
}

/**
 * Manual Restart Service
 * Programmatically trigger application restart
 */
@Service
class ManualRestartService {

    /**
     * Programmatically restart the application
     * 
     * WARNING: This will stop and restart the entire Spring context
     */
    public void restartApplication() {
        try {
            Restarter restarter = Restarter.getInstance();
            if (restarter != null) {
                System.out.println("🔄 Manual restart triggered");
                restarter.restart();
            } else {
                System.err.println("❌ Restart not available (DevTools not enabled)");
            }
        } catch (Exception e) {
            System.err.println("❌ Failed to restart: " + e.getMessage());
        }
    }

    /**
     * Create trigger file to restart application
     * 
     * DevTools can be configured to watch a trigger file:
     * spring.devtools.restart.trigger-file=.trigger
     */
    public void createTriggerFile() {
        try {
            Path triggerFile = Paths.get(".trigger");
            File file = triggerFile.toFile();
            
            if (file.exists()) {
                // Update timestamp
                file.setLastModified(System.currentTimeMillis());
            } else {
                // Create new file
                file.createNewFile();
            }
            
            System.out.println("✅ Trigger file created/updated: " + triggerFile.toAbsolutePath());
        } catch (Exception e) {
            System.err.println("❌ Failed to create trigger file: " + e.getMessage());
        }
    }

    /**
     * Check if restart is available
     */
    public boolean isRestartAvailable() {
        try {
            Restarter restarter = Restarter.getInstance();
            return restarter != null;
        } catch (Exception e) {
            return false;
        }
    }
}

/**
 * Restart Scope Bean Example
 * Demonstrates bean that survives restarts
 */
@Service
@RestartScope
class CachedDataService {

    private final Map<String, Object> cache = new ConcurrentHashMap<>();
    private final LocalDateTime cacheCreationTime = LocalDateTime.now();
    private final AtomicInteger accessCount = new AtomicInteger(0);

    @PostConstruct
    public void init() {
        System.out.println("📦 CachedDataService initialized (survives restarts)");
        System.out.println("   - Cache creation time: " + cacheCreationTime);
        System.out.println("   - Current cache size: " + cache.size());
        System.out.println("   - Total access count: " + accessCount.get());
    }

    public void put(String key, Object value) {
        cache.put(key, value);
        accessCount.incrementAndGet();
    }

    public Object get(String key) {
        accessCount.incrementAndGet();
        return cache.get(key);
    }

    public int getCacheSize() {
        return cache.size();
    }

    public int getAccessCount() {
        return accessCount.get();
    }

    public LocalDateTime getCacheCreationTime() {
        return cacheCreationTime;
    }

    public Duration getCacheAge() {
        return Duration.between(cacheCreationTime, LocalDateTime.now());
    }

    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new ConcurrentHashMap<>();
        stats.put("cacheSize", cache.size());
        stats.put("accessCount", accessCount.get());
        stats.put("creationTime", cacheCreationTime);
        stats.put("age", getCacheAge().getSeconds() + "s");
        return stats;
    }

    public void clear() {
        cache.clear();
    }
}

/**
 * Restart Performance Service
 * Measures restart performance
 */
@Service
class RestartPerformanceService {

    private final List<RestartPerformanceMetric> metrics = new ArrayList<>();

    public void recordRestartMetric(Duration restartDuration, int classesReloaded) {
        RestartPerformanceMetric metric = new RestartPerformanceMetric(
            LocalDateTime.now(),
            restartDuration,
            classesReloaded
        );
        metrics.add(metric);
    }

    public List<RestartPerformanceMetric> getMetrics() {
        return new ArrayList<>(metrics);
    }

    public Duration getAverageRestartTime() {
        if (metrics.isEmpty()) {
            return Duration.ZERO;
        }
        
        long totalMs = metrics.stream()
            .mapToLong(m -> m.getRestartDuration().toMillis())
            .sum();
        
        return Duration.ofMillis(totalMs / metrics.size());
    }

    public RestartPerformanceMetric getFastestRestart() {
        return metrics.stream()
            .min(Comparator.comparing(RestartPerformanceMetric::getRestartDuration))
            .orElse(null);
    }

    public RestartPerformanceMetric getSlowestRestart() {
        return metrics.stream()
            .max(Comparator.comparing(RestartPerformanceMetric::getRestartDuration))
            .orElse(null);
    }
}

/**
 * Restart Performance Metric
 */
class RestartPerformanceMetric {
    private final LocalDateTime timestamp;
    private final Duration restartDuration;
    private final int classesReloaded;

    public RestartPerformanceMetric(LocalDateTime timestamp, Duration restartDuration, 
                                    int classesReloaded) {
        this.timestamp = timestamp;
        this.restartDuration = restartDuration;
        this.classesReloaded = classesReloaded;
    }

    public LocalDateTime getTimestamp() { return timestamp; }
    public Duration getRestartDuration() { return restartDuration; }
    public int getClassesReloaded() { return classesReloaded; }

    @Override
    public String toString() {
        return String.format("RestartMetric{time=%s, duration=%dms, classes=%d}",
            timestamp, restartDuration.toMillis(), classesReloaded);
    }
}

/**
 * DevTools Auto-Restart REST Controller
 */
@RestController
@RequestMapping("/api/devtools/restart")
class DevToolsRestartController {

    private final RestartMonitor restartMonitor;
    private final RestartStatistics restartStatistics;
    private final ClassloaderInfoService classloaderInfoService;
    private final ManualRestartService manualRestartService;
    private final CachedDataService cachedDataService;
    private final RestartPerformanceService performanceService;

    public DevToolsRestartController(RestartMonitor restartMonitor,
                                     RestartStatistics restartStatistics,
                                     ClassloaderInfoService classloaderInfoService,
                                     ManualRestartService manualRestartService,
                                     CachedDataService cachedDataService,
                                     RestartPerformanceService performanceService) {
        this.restartMonitor = restartMonitor;
        this.restartStatistics = restartStatistics;
        this.classloaderInfoService = classloaderInfoService;
        this.manualRestartService = manualRestartService;
        this.cachedDataService = cachedDataService;
        this.performanceService = performanceService;
    }

    /**
     * GET /api/devtools/restart/status
     * Get restart status and statistics
     */
    @GetMapping("/status")
    public Map<String, Object> getStatus() {
        return restartMonitor.getStatus();
    }

    /**
     * GET /api/devtools/restart/statistics
     * Get restart statistics
     */
    @GetMapping("/statistics")
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new ConcurrentHashMap<>();
        stats.put("restartCount", restartStatistics.getRestartCount());
        stats.put("firstStartTime", restartStatistics.getFirstStartTime());
        stats.put("lastRestartTime", restartStatistics.getLastRestartTime());
        stats.put("totalUptime", restartStatistics.getTotalUptime().getSeconds() + "s");
        stats.put("restartHistory", restartStatistics.getRestartHistory());
        return stats;
    }

    /**
     * GET /api/devtools/restart/classloader
     * Get classloader information
     */
    @GetMapping("/classloader")
    public Map<String, Object> getClassloaderInfo() {
        return classloaderInfoService.getClassloaderInfo();
    }

    /**
     * GET /api/devtools/restart/urls
     * Get monitored classpath URLs
     */
    @GetMapping("/urls")
    public List<String> getRestartUrls() {
        return classloaderInfoService.getRestartUrls();
    }

    /**
     * GET /api/devtools/restart/enabled
     * Check if restart is enabled
     */
    @GetMapping("/enabled")
    public Map<String, Boolean> isRestartEnabled() {
        Map<String, Boolean> response = new ConcurrentHashMap<>();
        response.put("enabled", classloaderInfoService.isRestartEnabled());
        response.put("available", manualRestartService.isRestartAvailable());
        return response;
    }

    /**
     * POST /api/devtools/restart/trigger
     * Manually trigger application restart
     */
    @PostMapping("/trigger")
    public String triggerRestart() {
        if (!manualRestartService.isRestartAvailable()) {
            return "❌ Restart not available (DevTools not enabled)";
        }
        
        manualRestartService.restartApplication();
        return "🔄 Restart triggered (response may not be received)";
    }

    /**
     * POST /api/devtools/restart/trigger-file
     * Create trigger file for restart
     */
    @PostMapping("/trigger-file")
    public String createTriggerFile() {
        manualRestartService.createTriggerFile();
        return "✅ Trigger file created/updated";
    }

    /**
     * GET /api/devtools/restart/cache
     * Get cached data statistics (survives restarts)
     */
    @GetMapping("/cache")
    public Map<String, Object> getCacheStatistics() {
        return cachedDataService.getStatistics();
    }

    /**
     * PUT /api/devtools/restart/cache
     * Add data to restart-scoped cache
     */
    @PutMapping("/cache")
    public String addToCache(@RequestParam String key, @RequestParam String value) {
        cachedDataService.put(key, value);
        return "✅ Added to cache: " + key + " = " + value;
    }

    /**
     * GET /api/devtools/restart/cache/{key}
     * Get value from restart-scoped cache
     */
    @GetMapping("/cache/{key}")
    public Object getCacheValue(@PathVariable String key) {
        return cachedDataService.get(key);
    }

    /**
     * DELETE /api/devtools/restart/cache
     * Clear restart-scoped cache
     */
    @DeleteMapping("/cache")
    public String clearCache() {
        cachedDataService.clear();
        return "✅ Cache cleared";
    }

    /**
     * GET /api/devtools/restart/performance
     * Get restart performance metrics
     */
    @GetMapping("/performance")
    public Map<String, Object> getPerformanceMetrics() {
        Map<String, Object> metrics = new ConcurrentHashMap<>();
        metrics.put("averageRestartTime", performanceService.getAverageRestartTime().toMillis() + "ms");
        metrics.put("fastestRestart", performanceService.getFastestRestart());
        metrics.put("slowestRestart", performanceService.getSlowestRestart());
        metrics.put("allMetrics", performanceService.getMetrics());
        return metrics;
    }

    /**
     * DELETE /api/devtools/restart/statistics
     * Reset restart statistics
     */
    @DeleteMapping("/statistics")
    public String resetStatistics() {
        restartStatistics.reset();
        return "✅ Statistics reset";
    }
}

/**
 * 📚 USAGE EXAMPLES:
 * =================
 * 
 * 1️⃣ BASIC AUTO-RESTART SETUP:
 * -----------------------------
 * # pom.xml
 * <dependency>
 *     <groupId>org.springframework.boot</groupId>
 *     <artifactId>spring-boot-devtools</artifactId>
 *     <optional>true</optional>
 *     <scope>runtime</scope>
 * </dependency>
 * 
 * # application-dev.yml
 * spring:
 *   devtools:
 *     restart:
 *       enabled: true
 * 
 * 2️⃣ CUSTOM RESTART CONFIGURATION:
 * ---------------------------------
 * spring:
 *   devtools:
 *     restart:
 *       enabled: true
 *       poll-interval: 1s
 *       quiet-period: 400ms
 *       additional-paths:
 *         - src/main/resources
 *       exclude:
 *         - static/**
 *         - public/**
 *       log-condition-evaluation-delta: true
 * 
 * 3️⃣ RESTART-SCOPED BEAN:
 * ------------------------
 * @Service
 * @RestartScope  // Bean survives restarts
 * public class MyCache {
 *     private Map<String, Object> data = new ConcurrentHashMap<>();
 *     
 *     // Cache persists across restarts
 * }
 * 
 * 4️⃣ TRIGGER FILE RESTART:
 * -------------------------
 * # application.yml
 * spring:
 *   devtools:
 *     restart:
 *       trigger-file: .trigger
 * 
 * # To restart, touch the trigger file:
 * touch .trigger
 * 
 * 5️⃣ PROGRAMMATIC RESTART:
 * -------------------------
 * @Autowired
 * private ManualRestartService restartService;
 * 
 * public void restartApp() {
 *     restartService.restartApplication();
 * }
 * 
 * 6️⃣ MONITORING RESTARTS:
 * ------------------------
 * curl http://localhost:8080/api/devtools/restart/status
 * curl http://localhost:8080/api/devtools/restart/statistics
 * curl http://localhost:8080/api/devtools/restart/classloader
 * curl http://localhost:8080/api/devtools/restart/urls
 * 
 * 7️⃣ TRIGGER MANUAL RESTART:
 * ---------------------------
 * curl -X POST http://localhost:8080/api/devtools/restart/trigger
 * curl -X POST http://localhost:8080/api/devtools/restart/trigger-file
 * 
 * 8️⃣ RESTART-SCOPED CACHE:
 * -------------------------
 * # Add to cache (survives restarts)
 * curl -X PUT "http://localhost:8080/api/devtools/restart/cache?key=user&value=john"
 * 
 * # Get from cache
 * curl http://localhost:8080/api/devtools/restart/cache/user
 * 
 * # Get cache statistics
 * curl http://localhost:8080/api/devtools/restart/cache
 * 
 * 9️⃣ IDE AUTO-COMPILE SETUP:
 * ---------------------------
 * IntelliJ IDEA:
 * 1. File → Settings → Build, Execution, Deployment → Compiler
 * 2. Check "Build project automatically"
 * 3. Help → Find Action → Registry
 * 4. Enable "compiler.automake.allow.when.app.running"
 * 
 * Eclipse/STS:
 * - Project → Build Automatically (enabled by default)
 * 
 * 🔟 DISABLE IN PRODUCTION:
 * --------------------------
 * # application-prod.yml
 * spring:
 *   devtools:
 *     restart:
 *       enabled: false
 * 
 * # Or via JVM argument:
 * java -Dspring.devtools.restart.enabled=false -jar app.jar
 * 
 * # DevTools is automatically disabled in packaged JARs
 */
