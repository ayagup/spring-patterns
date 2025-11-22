package com.spring.patterns.lifecycle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.Lifecycle;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * Lifecycle Interface Pattern
 * ============================
 * 
 * The Lifecycle interface provides start() and stop() methods for beans that
 * require explicit start/stop semantics beyond initialization/destruction.
 * 
 * INTERFACE:
 * public interface Lifecycle {
 *     void start();
 *     void stop();
 *     boolean isRunning();
 * }
 * 
 * USE CASES:
 * - Start/stop services
 * - Control message listeners
 * - Manage background threads
 * - Control resource consumption
 */

@SpringBootApplication
public class LifecycleInterfacePattern {
    public static void main(String[] args) {
        SpringApplication.run(LifecycleInterfacePattern.class, args);
    }
}

@Component
class BackgroundService implements Lifecycle {
    private boolean running;
    private LocalDateTime startedAt;
    private LocalDateTime stoppedAt;
    
    @Override
    public void start() {
        System.out.println("BackgroundService - start() called");
        running = true;
        startedAt = LocalDateTime.now();
    }
    
    @Override
    public void stop() {
        System.out.println("BackgroundService - stop() called");
        running = false;
        stoppedAt = LocalDateTime.now();
    }
    
    @Override
    public boolean isRunning() {
        return running;
    }
    
    public String getStatus() {
        return String.format("Running: %s, Started: %s, Stopped: %s",
            running, startedAt, stoppedAt);
    }
}

@Component
class MessageListener implements Lifecycle {
    private boolean running;
    private int messagesReceived = 0;
    
    @Override
    public void start() {
        System.out.println("MessageListener - start() called");
        running = true;
    }
    
    @Override
    public void stop() {
        System.out.println("MessageListener - stop() called");
        running = false;
    }
    
    @Override
    public boolean isRunning() {
        return running;
    }
    
    public void receiveMessage() {
        if (running) messagesReceived++;
    }
    
    public String getStatus() {
        return String.format("Running: %s, Messages: %d", running, messagesReceived);
    }
}

@RestController
@RequestMapping("/api/lifecycle")
class LifecycleController {
    private final BackgroundService backgroundService;
    private final MessageListener messageListener;
    
    public LifecycleController(BackgroundService backgroundService, MessageListener messageListener) {
        this.backgroundService = backgroundService;
        this.messageListener = messageListener;
    }
    
    @GetMapping("/status")
    public String getStatus() {
        return String.format("""
            Lifecycle Pattern Status:
            
            Background Service: %s
            Message Listener: %s
            """,
            backgroundService.getStatus(),
            messageListener.getStatus()
        );
    }
    
    @PostMapping("/background/start")
    public String startBackground() {
        backgroundService.start();
        return "Background service started";
    }
    
    @PostMapping("/background/stop")
    public String stopBackground() {
        backgroundService.stop();
        return "Background service stopped";
    }
    
    @PostMapping("/listener/start")
    public String startListener() {
        messageListener.start();
        return "Message listener started";
    }
    
    @PostMapping("/listener/stop")
    public String stopListener() {
        messageListener.stop();
        return "Message listener stopped";
    }
}

/**
 * TESTING:
 * curl http://localhost:8080/api/lifecycle/status
 * curl -X POST http://localhost:8080/api/lifecycle/background/start
 * curl -X POST http://localhost:8080/api/lifecycle/background/stop
 */
