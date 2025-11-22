package com.spring.patterns.lifecycle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * SmartLifecycle Pattern
 * =======================
 * 
 * SmartLifecycle extends Lifecycle with additional features:
 * - Auto-start capability (isAutoStartup)
 * - Phased startup/shutdown (getPhase)
 * - Graceful shutdown callback (stop(Runnable))
 * 
 * INTERFACE:
 * public interface SmartLifecycle extends Lifecycle, Phased {
 *     default boolean isAutoStartup() { return true; }
 *     default void stop(Runnable callback) { stop(); callback.run(); }
 *     default int getPhase() { return DEFAULT_PHASE; }
 * }
 * 
 * PHASES:
 * - Lower phase values start first
 * - Higher phase values stop first
 * - DEFAULT_PHASE = Integer.MAX_VALUE
 * 
 * USE CASES:
 * - Auto-starting services
 * - Ordered startup/shutdown
 * - Graceful shutdown
 * - Dependency ordering
 */

@SpringBootApplication
public class SmartLifecyclePattern {
    public static void main(String[] args) {
        SpringApplication.run(SmartLifecyclePattern.class, args);
    }
}

/**
 * Phase 1: Infrastructure (starts first, stops last)
 */
@Component
class DatabaseConnectionManager implements SmartLifecycle {
    private boolean running;
    private LocalDateTime startedAt;
    
    @Override
    public void start() {
        System.out.println("Phase 1 - DatabaseConnectionManager started");
        running = true;
        startedAt = LocalDateTime.now();
    }
    
    @Override
    public void stop() {
        System.out.println("Phase 1 - DatabaseConnectionManager stopped");
        running = false;
    }
    
    @Override
    public boolean isRunning() {
        return running;
    }
    
    @Override
    public boolean isAutoStartup() {
        return true; // Auto-start on application startup
    }
    
    @Override
    public void stop(Runnable callback) {
        System.out.println("Phase 1 - DatabaseConnectionManager graceful shutdown");
        stop();
        callback.run();
    }
    
    @Override
    public int getPhase() {
        return 1; // Start first, stop last
    }
    
    public String getStatus() {
        return String.format("Running: %s, Started: %s, Phase: %d",
            running, startedAt, getPhase());
    }
}

/**
 * Phase 2: Business Services (starts after infrastructure)
 */
@Component
class BusinessService implements SmartLifecycle {
    private boolean running;
    private LocalDateTime startedAt;
    
    @Override
    public void start() {
        System.out.println("Phase 2 - BusinessService started");
        running = true;
        startedAt = LocalDateTime.now();
    }
    
    @Override
    public void stop() {
        System.out.println("Phase 2 - BusinessService stopped");
        running = false;
    }
    
    @Override
    public boolean isRunning() {
        return running;
    }
    
    @Override
    public boolean isAutoStartup() {
        return true;
    }
    
    @Override
    public void stop(Runnable callback) {
        System.out.println("Phase 2 - BusinessService graceful shutdown");
        stop();
        callback.run();
    }
    
    @Override
    public int getPhase() {
        return 2; // Start after phase 1, stop before phase 1
    }
    
    public String getStatus() {
        return String.format("Running: %s, Started: %s, Phase: %d",
            running, startedAt, getPhase());
    }
}

/**
 * Phase 3: API/Web Layer (starts last, stops first)
 */
@Component
class WebServiceManager implements SmartLifecycle {
    private boolean running;
    private LocalDateTime startedAt;
    
    @Override
    public void start() {
        System.out.println("Phase 3 - WebServiceManager started");
        running = true;
        startedAt = LocalDateTime.now();
    }
    
    @Override
    public void stop() {
        System.out.println("Phase 3 - WebServiceManager stopped");
        running = false;
    }
    
    @Override
    public boolean isRunning() {
        return running;
    }
    
    @Override
    public boolean isAutoStartup() {
        return true;
    }
    
    @Override
    public void stop(Runnable callback) {
        System.out.println("Phase 3 - WebServiceManager graceful shutdown");
        stop();
        callback.run();
    }
    
    @Override
    public int getPhase() {
        return 3; // Start last, stop first
    }
    
    public String getStatus() {
        return String.format("Running: %s, Started: %s, Phase: %d",
            running, startedAt, getPhase());
    }
}

@RestController
@RequestMapping("/api/smart-lifecycle")
class SmartLifecycleController {
    private final DatabaseConnectionManager dbManager;
    private final BusinessService businessService;
    private final WebServiceManager webManager;
    
    public SmartLifecycleController(
            DatabaseConnectionManager dbManager,
            BusinessService businessService,
            WebServiceManager webManager) {
        this.dbManager = dbManager;
        this.businessService = businessService;
        this.webManager = webManager;
        System.out.println("\nAll SmartLifecycle beans auto-started in phase order\n");
    }
    
    @GetMapping("/status")
    public String getStatus() {
        return String.format("""
            SmartLifecycle Pattern Status:
            
            Database Manager (Phase 1): %s
            Business Service (Phase 2): %s
            Web Manager (Phase 3): %s
            
            Startup Order: Phase 1 → Phase 2 → Phase 3
            Shutdown Order: Phase 3 → Phase 2 → Phase 1
            """,
            dbManager.getStatus(),
            businessService.getStatus(),
            webManager.getStatus()
        );
    }
}

/**
 * TESTING:
 * curl http://localhost:8080/api/smart-lifecycle/status
 * 
 * Check console output for startup/shutdown order
 */
