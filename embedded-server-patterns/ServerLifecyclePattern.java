package com.example.embeddedserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Server Lifecycle Pattern
 * 
 * Demonstrates handling embedded server lifecycle events,
 * including startup, initialization, and shutdown hooks.
 * 
 * Key Concepts:
 * - WebServerInitializedEvent
 * - ApplicationReadyEvent
 * - Server lifecycle
 * - Startup hooks
 * - Shutdown hooks
 * 
 * Use Cases:
 * - Post-startup tasks
 * - Health checks
 * - Resource initialization
 * - Graceful shutdown
 * - Monitoring setup
 */
@SpringBootApplication
public class ServerLifecyclePattern {

    public static void main(String[] args) {
        SpringApplication.run(ServerLifecyclePattern.class, args);
    }
}

/**
 * Server lifecycle event listener
 */
@Component
class ServerLifecycleListener {

    private static final List<String> events = new ArrayList<>();
    private int serverPort;
    private String serverType;

    /**
     * Listen to web server initialized event
     */
    @EventListener
    public void onWebServerInitialized(WebServerInitializedEvent event) {
        WebServer webServer = event.getWebServer();
        serverPort = webServer.getPort();
        serverType = event.getApplicationContext().getServerNamespace();
        
        String eventMsg = "Web server initialized on port: " + serverPort;
        events.add(eventMsg);
        System.out.println(eventMsg);
    }

    /**
     * Listen to application ready event
     */
    @EventListener
    public void onApplicationReady(ApplicationReadyEvent event) {
        String eventMsg = "Application ready - all components initialized";
        events.add(eventMsg);
        System.out.println(eventMsg);
        
        // Perform post-startup tasks
        performPostStartupTasks();
    }

    private void performPostStartupTasks() {
        System.out.println("Performing post-startup tasks...");
        // Initialize caches, warm up connections, etc.
    }

    public static List<String> getEvents() {
        return new ArrayList<>(events);
    }

    public int getServerPort() {
        return serverPort;
    }

    public String getServerType() {
        return serverType;
    }
}

/**
 * Shutdown hook component
 */
@Component
class ShutdownHook implements ApplicationListener<org.springframework.context.event.ContextClosedEvent> {

    private static final List<String> shutdownEvents = new ArrayList<>();

    @Override
    public void onApplicationEvent(org.springframework.context.event.ContextClosedEvent event) {
        String eventMsg = "Application shutting down - cleaning up resources";
        shutdownEvents.add(eventMsg);
        System.out.println(eventMsg);
        
        performCleanup();
    }

    private void performCleanup() {
        System.out.println("Closing connections...");
        System.out.println("Flushing caches...");
        System.out.println("Saving state...");
    }

    public static List<String> getShutdownEvents() {
        return new ArrayList<>(shutdownEvents);
    }
}

/**
 * Service providing lifecycle information
 */
@Service
class ServerLifecycleService {

    private final ServletWebServerApplicationContext applicationContext;
    private final ServerLifecycleListener lifecycleListener;

    public ServerLifecycleService(ServletWebServerApplicationContext applicationContext,
                                 ServerLifecycleListener lifecycleListener) {
        this.applicationContext = applicationContext;
        this.lifecycleListener = lifecycleListener;
    }

    public Map<String, Object> getServerInfo() {
        WebServer webServer = applicationContext.getWebServer();
        
        return Map.of(
                "port", webServer.getPort(),
                "running", applicationContext.isRunning(),
                "active", applicationContext.isActive(),
                "serverType", lifecycleListener.getServerType() != null ? 
                    lifecycleListener.getServerType() : "unknown"
        );
    }

    public Map<String, Object> getLifecycleEvents() {
        return Map.of(
                "startupEvents", ServerLifecycleListener.getEvents(),
                "shutdownEvents", ShutdownHook.getShutdownEvents(),
                "totalEvents", ServerLifecycleListener.getEvents().size() + 
                              ShutdownHook.getShutdownEvents().size()
        );
    }
}

/**
 * Controller exposing lifecycle information
 */
@RestController
class ServerLifecycleController {

    private final ServerLifecycleService lifecycleService;

    public ServerLifecycleController(ServerLifecycleService lifecycleService) {
        this.lifecycleService = lifecycleService;
    }

    @GetMapping("/server/info")
    public Map<String, Object> getServerInfo() {
        return lifecycleService.getServerInfo();
    }

    @GetMapping("/server/lifecycle")
    public Map<String, Object> getLifecycleEvents() {
        return lifecycleService.getLifecycleEvents();
    }
}

/**
 * Documentation:
 * 
 * Server Lifecycle Events:
 * 
 * 1. WebServerInitializedEvent:
 *    @EventListener
 *    public void onServerInit(WebServerInitializedEvent event) {
 *        int port = event.getWebServer().getPort();
 *        System.out.println("Server started on port: " + port);
 *    }
 * 
 * 2. ApplicationReadyEvent:
 *    @EventListener
 *    public void onReady(ApplicationReadyEvent event) {
 *        System.out.println("Application is ready");
 *    }
 * 
 * 3. ContextClosedEvent:
 *    @EventListener
 *    public void onShutdown(ContextClosedEvent event) {
 *        System.out.println("Application shutting down");
 *    }
 * 
 * Graceful Shutdown:
 * 
 * application.properties:
 * server.shutdown=graceful
 * spring.lifecycle.timeout-per-shutdown-phase=20s
 * 
 * Custom Shutdown Hook:
 * @Bean
 * public ApplicationListener<ContextClosedEvent> shutdownListener() {
 *     return event -> {
 *         System.out.println("Custom shutdown logic");
 *     };
 * }
 * 
 * CommandLineRunner (runs after startup):
 * @Bean
 * public CommandLineRunner runner() {
 *     return args -> {
 *         System.out.println("Application started");
 *     };
 * }
 * 
 * ApplicationRunner (similar to CommandLineRunner):
 * @Bean
 * public ApplicationRunner appRunner() {
 *     return args -> {
 *         System.out.println("Application started");
 *     };
 * }
 * 
 * SmartLifecycle (fine-grained control):
 * @Component
 * class MyLifecycle implements SmartLifecycle {
 *     
 *     @Override
 *     public void start() {
 *         System.out.println("Starting...");
 *     }
 *     
 *     @Override
 *     public void stop() {
 *         System.out.println("Stopping...");
 *     }
 *     
 *     @Override
 *     public boolean isRunning() {
 *         return running;
 *     }
 *     
 *     @Override
 *     public int getPhase() {
 *         return 0; // Order of execution
 *     }
 * }
 * 
 * Lifecycle Phases:
 * 1. Context initialized
 * 2. Beans created
 * 3. BeanPostProcessors run
 * 4. @PostConstruct methods called
 * 5. InitializingBean.afterPropertiesSet()
 * 6. Custom init methods
 * 7. SmartLifecycle.start()
 * 8. WebServer started
 * 9. ApplicationReadyEvent fired
 * 10. CommandLineRunner/ApplicationRunner executed
 * 
 * Shutdown Phases:
 * 1. ContextClosedEvent fired
 * 2. SmartLifecycle.stop()
 * 3. @PreDestroy methods called
 * 4. DisposableBean.destroy()
 * 5. Custom destroy methods
 * 6. WebServer stopped
 * 7. Context closed
 * 
 * Best Practices:
 * - Use ApplicationReadyEvent for post-startup tasks
 * - Implement graceful shutdown
 * - Set appropriate shutdown timeout
 * - Clean up resources in shutdown hooks
 * - Use SmartLifecycle for ordered startup/shutdown
 * - Monitor lifecycle events for debugging
 */
