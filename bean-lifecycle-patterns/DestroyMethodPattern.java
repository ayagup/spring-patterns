package com.spring.patterns.lifecycle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Destroy Method Pattern
 * =======================
 * 
 * Demonstrates using the @Bean(destroyMethod) attribute to specify custom cleanup methods.
 * Similar to initMethod, this is useful for third-party classes.
 * 
 * KEY FEATURES:
 * =============
 * - Specify destroy method name in @Bean annotation
 * - Works with third-party classes
 * - XML equivalent: <bean destroy-method="methodName"/>
 * - Method must be public/protected and no-arg
 * - Called before bean destruction
 * - Spring auto-detects "close" and "shutdown" methods
 * - Use destroyMethod="" to disable auto-detection
 * 
 * AUTO-DETECTION:
 * ===============
 * Spring automatically calls these methods if they exist:
 * - close()
 * - shutdown()
 * 
 * To disable: @Bean(destroyMethod="")
 * 
 * WHEN TO USE:
 * ============
 * - Third-party library classes
 * - Can't modify source code
 * - Need specific cleanup method name
 * - Override auto-detected methods
 */

@SpringBootApplication
public class DestroyMethodPattern {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(DestroyMethodPattern.class, args);
        System.out.println("\n=== Destroy Method Pattern Demo ===\n");
        System.out.println("Use /api/destroy-method/shutdown to trigger cleanup.\n");
    }
}

@Configuration
class DestroyMethodConfig {
    
    @Bean(destroyMethod = "cleanup")
    public ExternalResourceManager resourceManager() {
        return new ExternalResourceManager();
    }
    
    @Bean(destroyMethod = "disconnect")
    public RemoteServiceClient remoteClient() {
        return new RemoteServiceClient("api.example.com");
    }
    
    @Bean(destroyMethod = "flush")
    public BufferedLogger bufferedLogger() {
        return new BufferedLogger();
    }
    
    // Auto-detects "close()" method
    @Bean
    public AutoCloseableResource autoCloseResource() {
        return new AutoCloseableResource();
    }
    
    // Disable auto-detection
    @Bean(destroyMethod = "")
    public NoAutoCloseResource noAutoClose() {
        return new NoAutoCloseResource();
    }
}

/**
 * Example 1: Resource Manager with cleanup
 */
class ExternalResourceManager {
    private final List<Resource> resources = new ArrayList<>();
    private boolean active = true;
    
    public ExternalResourceManager() {
        System.out.println("ExternalResourceManager - Allocating resources");
        resources.add(new Resource("File Handle"));
        resources.add(new Resource("Network Socket"));
        resources.add(new Resource("Database Connection"));
        System.out.println("  Allocated " + resources.size() + " resources");
    }
    
    // Destroy method specified in @Bean(destroyMethod = "cleanup")
    public void cleanup() {
        System.out.println("\nExternalResourceManager - cleanup() method called");
        for (Resource resource : resources) {
            resource.release();
        }
        resources.clear();
        active = false;
        System.out.println("  All resources released");
    }
    
    public String getStatus() {
        return String.format("Active: %s, Resources: %d", active, resources.size());
    }
    
    static class Resource {
        private final String name;
        
        public Resource(String name) {
            this.name = name;
        }
        
        public void release() {
            System.out.println("  Released: " + name);
        }
    }
}

/**
 * Example 2: Remote Service Client
 */
class RemoteServiceClient {
    private final String serverUrl;
    private boolean connected = true;
    private int requestCount = 0;
    
    public RemoteServiceClient(String serverUrl) {
        System.out.println("\nRemoteServiceClient - Connecting to " + serverUrl);
        this.serverUrl = serverUrl;
        System.out.println("  Connected");
    }
    
    public void makeRequest(String endpoint) {
        if (connected) {
            requestCount++;
        }
    }
    
    // Destroy method specified in @Bean(destroyMethod = "disconnect")
    public void disconnect() {
        System.out.println("\nRemoteServiceClient - disconnect() method called");
        System.out.println("  Total requests made: " + requestCount);
        connected = false;
        System.out.println("  Disconnected from " + serverUrl);
    }
    
    public String getStatus() {
        return String.format("URL: %s, Connected: %s, Requests: %d",
            serverUrl, connected, requestCount);
    }
}

/**
 * Example 3: Buffered Logger
 */
class BufferedLogger {
    private final List<String> buffer = new ArrayList<>();
    private int totalLogs = 0;
    
    public BufferedLogger() {
        System.out.println("\nBufferedLogger - Initializing");
    }
    
    public void log(String message) {
        buffer.add(LocalDateTime.now() + " - " + message);
        totalLogs++;
        
        if (buffer.size() >= 10) {
            flushBuffer();
        }
    }
    
    private void flushBuffer() {
        System.out.println("  Flushing " + buffer.size() + " log entries");
        buffer.clear();
    }
    
    // Destroy method specified in @Bean(destroyMethod = "flush")
    public void flush() {
        System.out.println("\nBufferedLogger - flush() method called");
        if (!buffer.isEmpty()) {
            flushBuffer();
        }
        System.out.println("  Total logs written: " + totalLogs);
    }
    
    public String getStatus() {
        return String.format("Buffered: %d, Total: %d", buffer.size(), totalLogs);
    }
}

/**
 * Example 4: Auto-closeable resource
 * (Spring auto-detects close() method)
 */
class AutoCloseableResource {
    private boolean open = true;
    
    public AutoCloseableResource() {
        System.out.println("\nAutoCloseableResource - Opening resource");
    }
    
    // Spring auto-detects and calls this method
    public void close() {
        System.out.println("\nAutoCloseableResource - close() method called (auto-detected)");
        open = false;
        System.out.println("  Resource closed");
    }
    
    public boolean isOpen() {
        return open;
    }
}

/**
 * Example 5: No auto-close resource
 * (Auto-detection disabled with destroyMethod="")
 */
class NoAutoCloseResource {
    private boolean open = true;
    
    public NoAutoCloseResource() {
        System.out.println("\nNoAutoCloseResource - Opening resource");
        System.out.println("  (Auto-detection disabled)");
    }
    
    // This method will NOT be called because destroyMethod=""
    public void close() {
        System.out.println("NoAutoCloseResource - close() method (NOT CALLED)");
        open = false;
    }
    
    public boolean isOpen() {
        return open;
    }
}

/**
 * REST Controller
 */
@RestController
@RequestMapping("/api/destroy-method")
class DestroyMethodController {
    
    private final ExternalResourceManager resourceManager;
    private final RemoteServiceClient remoteClient;
    private final BufferedLogger bufferedLogger;
    private final AutoCloseableResource autoCloseResource;
    private final NoAutoCloseResource noAutoClose;
    private final ConfigurableApplicationContext context;
    
    public DestroyMethodController(
            ExternalResourceManager resourceManager,
            RemoteServiceClient remoteClient,
            BufferedLogger bufferedLogger,
            AutoCloseableResource autoCloseResource,
            NoAutoCloseResource noAutoClose,
            ConfigurableApplicationContext context) {
        this.resourceManager = resourceManager;
        this.remoteClient = remoteClient;
        this.bufferedLogger = bufferedLogger;
        this.autoCloseResource = autoCloseResource;
        this.noAutoClose = noAutoClose;
        this.context = context;
        System.out.println("\nDestroyMethodController - All beans initialized\n");
    }
    
    @GetMapping("/status")
    public String getStatus() {
        return String.format("""
            Destroy Method Pattern Status:
            
            Resource Manager: %s
            Remote Client: %s
            Buffered Logger: %s
            Auto-Close Resource: Open=%s
            No Auto-Close Resource: Open=%s
            """,
            resourceManager.getStatus(),
            remoteClient.getStatus(),
            bufferedLogger.getStatus(),
            autoCloseResource.isOpen(),
            noAutoClose.isOpen()
        );
    }
    
    @PostMapping("/request")
    public String makeRequest(@RequestParam String endpoint) {
        remoteClient.makeRequest(endpoint);
        return "Request made to: " + endpoint;
    }
    
    @PostMapping("/log")
    public String log(@RequestParam String message) {
        bufferedLogger.log(message);
        return "Log added: " + message;
    }
    
    @PostMapping("/shutdown")
    public String shutdown() {
        new Thread(() -> {
            try {
                Thread.sleep(1000);
                System.out.println("\n=== Triggering Destroy Methods ===\n");
                context.close();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
        return "Shutdown initiated. Watch console for destroy method execution.";
    }
}

/**
 * TESTING:
 * ========
 * 
 * curl http://localhost:8080/api/destroy-method/status
 * curl -X POST "http://localhost:8080/api/destroy-method/request?endpoint=/api/users"
 * curl -X POST "http://localhost:8080/api/destroy-method/log?message=Test"
 * curl -X POST http://localhost:8080/api/destroy-method/shutdown
 * 
 * BEST PRACTICES:
 * ===============
 * 
 * 1. Use for third-party classes
 * 2. Document destroy method in bean configuration
 * 3. Handle cleanup errors gracefully
 * 4. Be aware of auto-detection (close, shutdown)
 * 5. Use destroyMethod="" to disable auto-detection
 * 6. Keep cleanup fast
 * 7. Don't throw exceptions from destroy methods
 * 
 * AUTO-DETECTION BEHAVIOR:
 * ========================
 * Spring automatically calls:
 * - close() if it exists
 * - shutdown() if it exists
 * 
 * To disable: @Bean(destroyMethod = "")
 * To specify custom: @Bean(destroyMethod = "customMethod")
 */
