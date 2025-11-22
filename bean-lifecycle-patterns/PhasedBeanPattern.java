package com.spring.patterns.lifecycle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * Phased Bean Pattern
 * ====================
 * 
 * Demonstrates ordered lifecycle management using phase values.
 * Beans with lower phase values start first and stop last.
 * 
 * PHASE ORDERING:
 * - Phase 0: Critical infrastructure (DB, messaging)
 * - Phase 100: Business services
 * - Phase 200: Application services
 * - Phase 300: Web/API layer
 * - Phase Integer.MAX_VALUE: Default phase
 * 
 * STARTUP ORDER: 0 → 100 → 200 → 300
 * SHUTDOWN ORDER: 300 → 200 → 100 → 0
 */

@SpringBootApplication
public class PhasedBeanPattern {
    public static void main(String[] args) {
        SpringApplication.run(PhasedBeanPattern.class, args);
    }
}

@Component
class CriticalInfrastructure implements SmartLifecycle {
    private boolean running;
    
    @Override
    public void start() {
        System.out.println("[Phase 0] CriticalInfrastructure STARTED");
        running = true;
    }
    
    @Override
    public void stop() {
        System.out.println("[Phase 0] CriticalInfrastructure STOPPED");
        running = false;
    }
    
    @Override
    public boolean isRunning() { return running; }
    
    @Override
    public int getPhase() { return 0; }
    
    public String getInfo() { return "Phase 0: Critical Infrastructure"; }
}

@Component
class BusinessLayer implements SmartLifecycle {
    private boolean running;
    
    @Override
    public void start() {
        System.out.println("[Phase 100] BusinessLayer STARTED");
        running = true;
    }
    
    @Override
    public void stop() {
        System.out.println("[Phase 100] BusinessLayer STOPPED");
        running = false;
    }
    
    @Override
    public boolean isRunning() { return running; }
    
    @Override
    public int getPhase() { return 100; }
    
    public String getInfo() { return "Phase 100: Business Layer"; }
}

@Component
class ApplicationLayer implements SmartLifecycle {
    private boolean running;
    
    @Override
    public void start() {
        System.out.println("[Phase 200] ApplicationLayer STARTED");
        running = true;
    }
    
    @Override
    public void stop() {
        System.out.println("[Phase 200] ApplicationLayer STOPPED");
        running = false;
    }
    
    @Override
    public boolean isRunning() { return running; }
    
    @Override
    public int getPhase() { return 200; }
    
    public String getInfo() { return "Phase 200: Application Layer"; }
}

@Component
class WebLayer implements SmartLifecycle {
    private boolean running;
    
    @Override
    public void start() {
        System.out.println("[Phase 300] WebLayer STARTED");
        running = true;
    }
    
    @Override
    public void stop() {
        System.out.println("[Phase 300] WebLayer STOPPED");
        running = false;
    }
    
    @Override
    public boolean isRunning() { return running; }
    
    @Override
    public int getPhase() { return 300; }
    
    public String getInfo() { return "Phase 300: Web Layer"; }
}

@RestController
@RequestMapping("/api/phased")
class PhasedBeanController {
    private final CriticalInfrastructure infra;
    private final BusinessLayer business;
    private final ApplicationLayer app;
    private final WebLayer web;
    
    public PhasedBeanController(
            CriticalInfrastructure infra,
            BusinessLayer business,
            ApplicationLayer app,
            WebLayer web) {
        this.infra = infra;
        this.business = business;
        this.app = app;
        this.web = web;
        System.out.println("\nAll phased beans started in order\n");
    }
    
    @GetMapping("/status")
    public String getStatus() {
        return String.format("""
            Phased Bean Pattern Status:
            
            %s - Running: %s
            %s - Running: %s
            %s - Running: %s
            %s - Running: %s
            
            Startup Order: Phase 0 → 100 → 200 → 300
            Shutdown Order: Phase 300 → 200 → 100 → 0
            """,
            infra.getInfo(), infra.isRunning(),
            business.getInfo(), business.isRunning(),
            app.getInfo(), app.isRunning(),
            web.getInfo(), web.isRunning()
        );
    }
}

/**
 * TESTING:
 * curl http://localhost:8080/api/phased/status
 * 
 * Watch console for phase-ordered startup
 */
