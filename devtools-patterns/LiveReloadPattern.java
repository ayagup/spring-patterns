package com.example.devtools.patterns;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.devtools.livereload.LiveReloadServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.time.LocalDateTime;

/**
 * 🔄 SPRING BOOT DEVTOOLS - LIVERELOAD PATTERN 🔄
 * ================================================
 * 
 * Demonstrates Spring Boot DevTools LiveReload functionality for automatic
 * browser refresh during development. LiveReload eliminates manual browser
 * refresh after code changes, improving developer productivity.
 * 
 * 🎯 KEY CONCEPTS:
 * ===============
 * 
 * 1️⃣ LIVERELOAD SERVER:
 *    - Embedded WebSocket server on port 35729
 *    - Monitors classpath changes
 *    - Sends reload signals to browser
 *    - Browser extension required
 * 
 * 2️⃣ FILE WATCHING:
 *    - WatchService API for file system monitoring
 *    - Detect CREATE, MODIFY, DELETE events
 *    - Trigger reload on changes
 *    - Configurable watch paths
 * 
 * 3️⃣ BROWSER INTEGRATION:
 *    - LiveReload browser extension (Chrome/Firefox/Safari)
 *    - WebSocket connection to port 35729
 *    - Automatic page refresh
 *    - CSS/JS hot reload without full refresh
 * 
 * 4️⃣ DEVTOOLS AUTO-RESTART:
 *    - Works with DevTools restart mechanism
 *    - Restart + LiveReload = seamless development
 *    - Fast restart with base/restart classloaders
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
 *     livereload:
 *       enabled: true          # Enable LiveReload server (default: true)
 *       port: 35729            # LiveReload server port (default: 35729)
 *     restart:
 *       enabled: true          # Enable auto-restart (works with LiveReload)
 *       additional-paths:
 *         - src/main/resources
 *       exclude:
 *         - static/**
 *         - public/**
 * 
 * 🌐 BROWSER EXTENSION SETUP:
 * ===========================
 * Chrome: http://livereload.com/extensions/
 * Firefox: https://addons.mozilla.org/en-US/firefox/addon/livereload-web-extension/
 * Safari: http://livereload.com/extensions/
 * 
 * After installing:
 * 1. Start Spring Boot application with DevTools
 * 2. Open browser to http://localhost:8080
 * 3. Click LiveReload extension icon (should show "LiveReload is connected")
 * 4. Make code changes
 * 5. Browser auto-refreshes
 * 
 * 🎨 STATIC RESOURCE LIVERELOAD:
 * ==============================
 * DevTools monitors these directories by default:
 * - /META-INF/maven
 * - /META-INF/resources
 * - /resources
 * - /static
 * - /public
 * - /templates
 * 
 * CSS/JS changes trigger browser refresh WITHOUT full page reload.
 * HTML/Thymeleaf changes trigger full page reload.
 * 
 * ⚠️ IMPORTANT NOTES:
 * ==================
 * - DevTools must be in runtime/optional scope (not packaged in production)
 * - LiveReload only works in development environment
 * - Disable in production (spring.devtools.restart.enabled=false)
 * - Browser extension required for LiveReload functionality
 * - Port 35729 must be available
 * - Does not work with WAR deployment
 * - Works best with IDE auto-compile (IntelliJ IDEA, Eclipse, STS)
 * 
 * 💡 WHEN TO USE:
 * ==============
 * ✅ Local development with frequent UI changes
 * ✅ Frontend development (HTML/CSS/JS)
 * ✅ Thymeleaf/JSP template development
 * ✅ REST API development with Swagger UI
 * ✅ Rapid prototyping
 * 
 * ❌ WHEN NOT TO USE:
 * ==================
 * ❌ Production environments (always disabled)
 * ❌ CI/CD pipelines
 * ❌ Docker containers (complex networking)
 * ❌ Remote development (LiveReload is local only)
 * ❌ Performance testing (DevTools adds overhead)
 * 
 * @author Spring Patterns
 * @version 1.0
 * @since 2024-01-20
 */
@SpringBootApplication
public class LiveReloadPattern {

    public static void main(String[] args) {
        SpringApplication.run(LiveReloadPattern.class, args);
    }
}

/**
 * LiveReload Configuration
 * Configures LiveReload server and file watching
 */
@Configuration
@Profile("dev")
class LiveReloadConfiguration {

    /**
     * Configure custom LiveReload settings
     * 
     * Note: Spring Boot DevTools automatically starts LiveReload server.
     * This bean demonstrates custom configuration if needed.
     */
    @Bean
    public LiveReloadServerInfo liveReloadServerInfo() {
        return new LiveReloadServerInfo(35729, true);
    }

    /**
     * File watcher for custom directories
     * Monitors additional directories beyond default classpath
     */
    @Bean
    public CustomFileWatcher customFileWatcher(LiveReloadNotifier notifier) {
        return new CustomFileWatcher(notifier);
    }
}

/**
 * LiveReload Server Information
 */
class LiveReloadServerInfo {
    private final int port;
    private final boolean enabled;

    public LiveReloadServerInfo(int port, boolean enabled) {
        this.port = port;
        this.enabled = enabled;
    }

    public int getPort() { return port; }
    public boolean isEnabled() { return enabled; }

    @Override
    public String toString() {
        return String.format("LiveReload{port=%d, enabled=%s}", port, enabled);
    }
}

/**
 * Custom File Watcher Service
 * Monitors file system changes and triggers LiveReload
 */
@Service
class CustomFileWatcher {

    private final LiveReloadNotifier notifier;
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private WatchService watchService;
    private final Map<WatchKey, Path> watchKeys = new ConcurrentHashMap<>();

    public CustomFileWatcher(LiveReloadNotifier notifier) {
        this.notifier = notifier;
        initializeWatchService();
    }

    private void initializeWatchService() {
        try {
            watchService = FileSystems.getDefault().newWatchService();
            
            // Watch src/main/resources (if exists)
            Path resourcesPath = Paths.get("src/main/resources");
            if (Files.exists(resourcesPath)) {
                registerDirectory(resourcesPath);
            }

            // Watch src/main/webapp (if exists)
            Path webappPath = Paths.get("src/main/webapp");
            if (Files.exists(webappPath)) {
                registerDirectory(webappPath);
            }

            // Start watching
            executor.scheduleWithFixedDelay(this::processEvents, 0, 500, TimeUnit.MILLISECONDS);
            
        } catch (IOException e) {
            System.err.println("Failed to initialize file watcher: " + e.getMessage());
        }
    }

    private void registerDirectory(Path directory) throws IOException {
        WatchKey key = directory.register(
            watchService,
            StandardWatchEventKinds.ENTRY_CREATE,
            StandardWatchEventKinds.ENTRY_MODIFY,
            StandardWatchEventKinds.ENTRY_DELETE
        );
        watchKeys.put(key, directory);
        System.out.println("Watching directory: " + directory);
    }

    private void processEvents() {
        WatchKey key = watchService.poll();
        if (key == null) {
            return;
        }

        Path directory = watchKeys.get(key);
        if (directory == null) {
            key.reset();
            return;
        }

        for (WatchEvent<?> event : key.pollEvents()) {
            WatchEvent.Kind<?> kind = event.kind();
            
            if (kind == StandardWatchEventKinds.OVERFLOW) {
                continue;
            }

            @SuppressWarnings("unchecked")
            WatchEvent<Path> pathEvent = (WatchEvent<Path>) event;
            Path filename = pathEvent.context();
            Path fullPath = directory.resolve(filename);

            System.out.println("File " + kind.name() + ": " + fullPath);

            // Trigger LiveReload notification
            notifier.notifyChange(fullPath, kind.name());
        }

        key.reset();
    }

    @PreDestroy
    public void destroy() {
        try {
            if (watchService != null) {
                watchService.close();
            }
            executor.shutdown();
        } catch (IOException e) {
            System.err.println("Error closing watch service: " + e.getMessage());
        }
    }

    public Map<Path, String> getWatchedDirectories() {
        Map<Path, String> directories = new ConcurrentHashMap<>();
        watchKeys.values().forEach(path -> directories.put(path, "WATCHING"));
        return directories;
    }
}

/**
 * LiveReload Notifier Service
 * Sends reload signals (simulated - actual LiveReload is handled by DevTools)
 */
@Service
class LiveReloadNotifier {

    private final List<LiveReloadEvent> recentEvents = new ArrayList<>();
    private static final int MAX_EVENTS = 100;

    public void notifyChange(Path path, String eventType) {
        LiveReloadEvent event = new LiveReloadEvent(
            path.toString(),
            eventType,
            LocalDateTime.now()
        );

        synchronized (recentEvents) {
            recentEvents.add(0, event);
            if (recentEvents.size() > MAX_EVENTS) {
                recentEvents.remove(recentEvents.size() - 1);
            }
        }

        System.out.println("LiveReload notification: " + event);
    }

    public List<LiveReloadEvent> getRecentEvents() {
        synchronized (recentEvents) {
            return new ArrayList<>(recentEvents);
        }
    }
}

/**
 * LiveReload Event
 */
class LiveReloadEvent {
    private final String path;
    private final String eventType;
    private final LocalDateTime timestamp;

    public LiveReloadEvent(String path, String eventType, LocalDateTime timestamp) {
        this.path = path;
        this.eventType = eventType;
        this.timestamp = timestamp;
    }

    public String getPath() { return path; }
    public String getEventType() { return eventType; }
    public LocalDateTime getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return String.format("LiveReloadEvent{path='%s', type=%s, time=%s}", 
            path, eventType, timestamp);
    }
}

/**
 * LiveReload Monitoring Service
 * Provides statistics and monitoring for LiveReload activity
 */
@Service
class LiveReloadMonitoringService {

    private long totalReloads = 0;
    private LocalDateTime lastReloadTime;
    private final Map<String, Integer> reloadsByFileType = new ConcurrentHashMap<>();

    public void recordReload(String filePath) {
        totalReloads++;
        lastReloadTime = LocalDateTime.now();

        String fileExtension = getFileExtension(filePath);
        reloadsByFileType.merge(fileExtension, 1, Integer::sum);
    }

    private String getFileExtension(String filePath) {
        int lastDot = filePath.lastIndexOf('.');
        return lastDot > 0 ? filePath.substring(lastDot + 1) : "unknown";
    }

    public LiveReloadStatistics getStatistics() {
        return new LiveReloadStatistics(
            totalReloads,
            lastReloadTime,
            new ConcurrentHashMap<>(reloadsByFileType)
        );
    }

    public void reset() {
        totalReloads = 0;
        lastReloadTime = null;
        reloadsByFileType.clear();
    }
}

/**
 * LiveReload Statistics
 */
class LiveReloadStatistics {
    private final long totalReloads;
    private final LocalDateTime lastReloadTime;
    private final Map<String, Integer> reloadsByFileType;

    public LiveReloadStatistics(long totalReloads, LocalDateTime lastReloadTime,
                                Map<String, Integer> reloadsByFileType) {
        this.totalReloads = totalReloads;
        this.lastReloadTime = lastReloadTime;
        this.reloadsByFileType = reloadsByFileType;
    }

    public long getTotalReloads() { return totalReloads; }
    public LocalDateTime getLastReloadTime() { return lastReloadTime; }
    public Map<String, Integer> getReloadsByFileType() { return reloadsByFileType; }

    @Override
    public String toString() {
        return String.format("LiveReloadStatistics{total=%d, last=%s, byType=%s}",
            totalReloads, lastReloadTime, reloadsByFileType);
    }
}

/**
 * Static Resource LiveReload Service
 * Demonstrates handling of static resource changes
 */
@Service
class StaticResourceLiveReloadService {

    private final LiveReloadMonitoringService monitoringService;

    public StaticResourceLiveReloadService(LiveReloadMonitoringService monitoringService) {
        this.monitoringService = monitoringService;
    }

    /**
     * Handle CSS file changes
     * CSS changes can be hot-reloaded without full page refresh
     */
    public void handleCssChange(String cssFilePath) {
        System.out.println("CSS file changed: " + cssFilePath);
        monitoringService.recordReload(cssFilePath);
        // DevTools LiveReload will inject new CSS without page reload
    }

    /**
     * Handle JavaScript file changes
     * JavaScript changes trigger full page reload
     */
    public void handleJavaScriptChange(String jsFilePath) {
        System.out.println("JavaScript file changed: " + jsFilePath);
        monitoringService.recordReload(jsFilePath);
        // DevTools LiveReload will trigger full page reload
    }

    /**
     * Handle HTML/Template file changes
     * Template changes trigger full page reload
     */
    public void handleTemplateChange(String templateFilePath) {
        System.out.println("Template file changed: " + templateFilePath);
        monitoringService.recordReload(templateFilePath);
        // DevTools LiveReload will trigger full page reload
    }

    /**
     * Handle image file changes
     * Image changes trigger resource reload
     */
    public void handleImageChange(String imageFilePath) {
        System.out.println("Image file changed: " + imageFilePath);
        monitoringService.recordReload(imageFilePath);
        // DevTools LiveReload will reload image resources
    }
}

/**
 * LiveReload REST Controller
 * Provides endpoints for LiveReload monitoring and testing
 */
@RestController
@RequestMapping("/api/livereload")
class LiveReloadController {

    private final LiveReloadNotifier notifier;
    private final LiveReloadMonitoringService monitoringService;
    private final CustomFileWatcher fileWatcher;
    private final StaticResourceLiveReloadService staticResourceService;

    public LiveReloadController(LiveReloadNotifier notifier,
                                LiveReloadMonitoringService monitoringService,
                                CustomFileWatcher fileWatcher,
                                StaticResourceLiveReloadService staticResourceService) {
        this.notifier = notifier;
        this.monitoringService = monitoringService;
        this.fileWatcher = fileWatcher;
        this.staticResourceService = staticResourceService;
    }

    /**
     * GET /api/livereload/events
     * Get recent LiveReload events
     */
    @GetMapping("/events")
    public List<LiveReloadEvent> getRecentEvents() {
        return notifier.getRecentEvents();
    }

    /**
     * GET /api/livereload/statistics
     * Get LiveReload statistics
     */
    @GetMapping("/statistics")
    public LiveReloadStatistics getStatistics() {
        return monitoringService.getStatistics();
    }

    /**
     * GET /api/livereload/watched-directories
     * Get list of watched directories
     */
    @GetMapping("/watched-directories")
    public Map<Path, String> getWatchedDirectories() {
        return fileWatcher.getWatchedDirectories();
    }

    /**
     * POST /api/livereload/trigger
     * Manually trigger LiveReload notification (for testing)
     */
    @PostMapping("/trigger")
    public String triggerReload(@RequestParam String filePath) {
        notifier.notifyChange(Paths.get(filePath), "MANUAL");
        monitoringService.recordReload(filePath);
        return "LiveReload triggered for: " + filePath;
    }

    /**
     * POST /api/livereload/css
     * Simulate CSS file change
     */
    @PostMapping("/css")
    public String triggerCssReload(@RequestParam String cssFile) {
        staticResourceService.handleCssChange(cssFile);
        return "CSS reload triggered: " + cssFile;
    }

    /**
     * POST /api/livereload/js
     * Simulate JavaScript file change
     */
    @PostMapping("/js")
    public String triggerJsReload(@RequestParam String jsFile) {
        staticResourceService.handleJavaScriptChange(jsFile);
        return "JavaScript reload triggered: " + jsFile;
    }

    /**
     * POST /api/livereload/template
     * Simulate template file change
     */
    @PostMapping("/template")
    public String triggerTemplateReload(@RequestParam String templateFile) {
        staticResourceService.handleTemplateChange(templateFile);
        return "Template reload triggered: " + templateFile;
    }

    /**
     * POST /api/livereload/image
     * Simulate image file change
     */
    @PostMapping("/image")
    public String triggerImageReload(@RequestParam String imageFile) {
        staticResourceService.handleImageChange(imageFile);
        return "Image reload triggered: " + imageFile;
    }

    /**
     * DELETE /api/livereload/statistics
     * Reset LiveReload statistics
     */
    @DeleteMapping("/statistics")
    public String resetStatistics() {
        monitoringService.reset();
        return "LiveReload statistics reset";
    }

    /**
     * GET /api/livereload/status
     * Get LiveReload server status
     */
    @GetMapping("/status")
    public Map<String, Object> getStatus() {
        Map<String, Object> status = new ConcurrentHashMap<>();
        status.put("enabled", true);
        status.put("port", 35729);
        status.put("connected", "Check browser extension");
        status.put("statistics", monitoringService.getStatistics());
        status.put("watchedDirectories", fileWatcher.getWatchedDirectories().size());
        return status;
    }
}

/**
 * 📚 USAGE EXAMPLES:
 * =================
 * 
 * 1️⃣ BASIC LIVERELOAD SETUP:
 * ---------------------------
 * # application-dev.yml
 * spring:
 *   devtools:
 *     livereload:
 *       enabled: true
 *       port: 35729
 * 
 * # pom.xml
 * <dependency>
 *     <groupId>org.springframework.boot</groupId>
 *     <artifactId>spring-boot-devtools</artifactId>
 *     <optional>true</optional>
 *     <scope>runtime</scope>
 * </dependency>
 * 
 * 2️⃣ BROWSER EXTENSION SETUP:
 * ----------------------------
 * 1. Install LiveReload extension for your browser
 * 2. Start Spring Boot application: mvn spring-boot:run
 * 3. Open http://localhost:8080 in browser
 * 4. Click LiveReload extension icon (should connect to port 35729)
 * 5. Make changes to CSS/HTML/Java files
 * 6. Browser automatically refreshes
 * 
 * 3️⃣ IDE AUTO-COMPILE:
 * ---------------------
 * IntelliJ IDEA:
 * - File → Settings → Build, Execution, Deployment → Compiler
 * - Check "Build project automatically"
 * - Help → Find Action → Registry → Enable "compiler.automake.allow.when.app.running"
 * 
 * Eclipse/STS:
 * - Project → Build Automatically (enabled by default)
 * 
 * 4️⃣ CUSTOM WATCH DIRECTORIES:
 * -----------------------------
 * spring:
 *   devtools:
 *     restart:
 *       additional-paths:
 *         - src/main/custom-resources
 *         - src/main/webapp
 * 
 * 5️⃣ EXCLUDE DIRECTORIES:
 * ------------------------
 * spring:
 *   devtools:
 *     restart:
 *       exclude:
 *         - static/**          # Don't restart on static file changes
 *         - public/**          # Don't restart on public file changes
 *         - templates/**       # Don't restart on template changes
 * 
 * 6️⃣ MONITORING LIVERELOAD:
 * --------------------------
 * curl http://localhost:8080/api/livereload/status
 * curl http://localhost:8080/api/livereload/statistics
 * curl http://localhost:8080/api/livereload/events
 * curl http://localhost:8080/api/livereload/watched-directories
 * 
 * 7️⃣ MANUAL TRIGGER (TESTING):
 * -----------------------------
 * curl -X POST "http://localhost:8080/api/livereload/trigger?filePath=test.css"
 * curl -X POST "http://localhost:8080/api/livereload/css?cssFile=styles.css"
 * curl -X POST "http://localhost:8080/api/livereload/js?jsFile=app.js"
 * 
 * 8️⃣ DISABLE IN PRODUCTION:
 * --------------------------
 * # application-prod.yml
 * spring:
 *   devtools:
 *     restart:
 *       enabled: false
 *     livereload:
 *       enabled: false
 * 
 * # Or via JVM argument:
 * java -Dspring.devtools.restart.enabled=false -jar app.jar
 * 
 * 9️⃣ DOCKER LIVERELOAD (ADVANCED):
 * ---------------------------------
 * # Requires port mapping and volume mounting
 * docker run -p 8080:8080 -p 35729:35729 \
 *   -v $(pwd)/src:/app/src \
 *   myapp:latest
 * 
 * # Note: LiveReload in Docker is complex, prefer local development
 * 
 * 🔟 TROUBLESHOOTING:
 * -------------------
 * ❌ Browser not refreshing:
 *    - Check browser extension is installed and enabled
 *    - Verify port 35729 is not blocked by firewall
 *    - Check DevTools is in runtime scope
 * 
 * ❌ Too many refreshes:
 *    - Exclude static directories from restart
 *    - Use spring.devtools.restart.exclude
 * 
 * ❌ Not detecting changes:
 *    - Enable IDE auto-compile
 *    - Check additional-paths configuration
 * 
 * ❌ Port 35729 already in use:
 *    - Change port: spring.devtools.livereload.port=35730
 *    - Find and kill process using port 35729
 */
