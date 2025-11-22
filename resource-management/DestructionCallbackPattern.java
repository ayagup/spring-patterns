package com.spring.patterns.resourcemanagement;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.Closeable;
import java.io.IOException;

/**
 * Destruction Callback Pattern
 * 
 * Demonstrates Spring's destruction callbacks for cleaning up resources
 * when beans are destroyed. Multiple mechanisms are available:
 * - DisposableBean interface
 * - @PreDestroy annotation (JSR-250)
 * - Custom destroy method via @Bean
 * - AutoCloseable/Closeable interface
 * 
 * Key Concepts:
 * - Destruction callbacks ensure proper resource cleanup
 * - Callbacks execute when ApplicationContext is closed
 * - Multiple callback mechanisms with execution order
 * - Critical for preventing resource leaks
 */

@Configuration
class DestructionConfig {
    
    @Bean(destroyMethod = "customCleanup")
    public FileManager fileManager() {
        return new FileManager();
    }
    
    @Bean
    public ConnectionPool connectionPool() {
        return new ConnectionPool();
    }
    
    @Bean
    public ResourceHandler resourceHandler() {
        return new ResourceHandler();
    }
    
    @Bean(destroyMethod = "close")
    public NetworkConnection networkConnection() {
        return new NetworkConnection();
    }
    
    // Spring automatically calls close() on Closeable beans
    @Bean(destroyMethod = "")  // Empty string disables auto-detection
    public ManualCloseable manualCloseable() {
        return new ManualCloseable();
    }
}

/**
 * Bean implementing DisposableBean interface
 */
@Component
class ConnectionPool implements DisposableBean {
    
    private boolean active = false;
    private int activeConnections = 0;
    
    public ConnectionPool() {
        System.out.println("ConnectionPool: Constructor - Creating pool");
        this.active = true;
        this.activeConnections = 10;
        System.out.println("ConnectionPool: Pool created with " + 
                activeConnections + " connections");
    }
    
    public void borrowConnection() {
        if (active && activeConnections > 0) {
            activeConnections--;
            System.out.println("  Borrowed connection. Remaining: " + activeConnections);
        } else {
            System.out.println("  No connections available or pool inactive");
        }
    }
    
    public void returnConnection() {
        if (active) {
            activeConnections++;
            System.out.println("  Returned connection. Total: " + activeConnections);
        }
    }
    
    @Override
    public void destroy() throws Exception {
        System.out.println("ConnectionPool: DisposableBean.destroy() - Cleaning up");
        
        if (active) {
            System.out.println("  Closing " + activeConnections + " active connections");
            activeConnections = 0;
            active = false;
            System.out.println("  Connection pool destroyed");
        } else {
            System.out.println("  Pool already destroyed");
        }
    }
    
    public boolean isActive() {
        return active;
    }
}

/**
 * Bean with @PreDestroy annotation
 */
class ResourceHandler {
    
    private boolean initialized = false;
    private String resourcePath = "/tmp/resources";
    
    public ResourceHandler() {
        System.out.println("ResourceHandler: Constructor - Initializing");
        this.initialized = true;
        System.out.println("ResourceHandler: Allocated resources at " + resourcePath);
    }
    
    public void processResource(String resourceName) {
        if (initialized) {
            System.out.println("  Processing resource: " + resourceName);
        } else {
            System.out.println("  ResourceHandler not initialized");
        }
    }
    
    @PreDestroy
    public void cleanup() {
        System.out.println("ResourceHandler: @PreDestroy - Cleanup starting");
        
        if (initialized) {
            System.out.println("  Releasing resources at " + resourcePath);
            System.out.println("  Closing file handles");
            System.out.println("  Clearing caches");
            initialized = false;
            System.out.println("  Cleanup completed");
        }
    }
}

/**
 * Bean with custom destroy method
 */
class FileManager {
    
    private boolean operational = false;
    private int openFiles = 0;
    
    public FileManager() {
        System.out.println("FileManager: Constructor - Starting up");
        this.operational = true;
        this.openFiles = 5;
        System.out.println("FileManager: Started with " + openFiles + " open files");
    }
    
    public void openFile(String filename) {
        if (operational) {
            openFiles++;
            System.out.println("  Opened file: " + filename + 
                    " (Total open: " + openFiles + ")");
        }
    }
    
    public void closeFile(String filename) {
        if (operational && openFiles > 0) {
            openFiles--;
            System.out.println("  Closed file: " + filename + 
                    " (Remaining: " + openFiles + ")");
        }
    }
    
    // Custom destroy method (specified in @Bean)
    public void customCleanup() {
        System.out.println("FileManager: customCleanup() - Custom destroy method");
        
        if (operational) {
            System.out.println("  Closing " + openFiles + " open files");
            
            while (openFiles > 0) {
                System.out.println("    Closing file #" + openFiles);
                openFiles--;
            }
            
            System.out.println("  Flushing buffers");
            System.out.println("  Releasing file system locks");
            operational = false;
            System.out.println("  FileManager shutdown complete");
        }
    }
}

/**
 * Bean implementing Closeable interface
 */
class NetworkConnection implements Closeable {
    
    private boolean connected = false;
    private String serverAddress = "192.168.1.100:8080";
    
    public NetworkConnection() {
        System.out.println("NetworkConnection: Constructor - Establishing connection");
        this.connected = true;
        System.out.println("NetworkConnection: Connected to " + serverAddress);
    }
    
    public void sendData(String data) {
        if (connected) {
            System.out.println("  Sending data: " + data);
        } else {
            System.out.println("  Not connected - cannot send data");
        }
    }
    
    @Override
    public void close() throws IOException {
        System.out.println("NetworkConnection: close() - Closing connection");
        
        if (connected) {
            System.out.println("  Flushing output buffers");
            System.out.println("  Closing socket to " + serverAddress);
            connected = false;
            System.out.println("  Connection closed");
        } else {
            System.out.println("  Already disconnected");
        }
    }
    
    public boolean isConnected() {
        return connected;
    }
}

/**
 * Closeable bean with manual close (destroyMethod="")
 */
class ManualCloseable implements Closeable {
    
    private boolean open = true;
    
    public ManualCloseable() {
        System.out.println("ManualCloseable: Constructor - Resource opened");
    }
    
    public void doWork() {
        if (open) {
            System.out.println("  ManualCloseable: Doing work");
        }
    }
    
    @Override
    public void close() throws IOException {
        System.out.println("ManualCloseable: close() - This should NOT be called automatically");
        open = false;
    }
    
    public void manualShutdown() {
        System.out.println("ManualCloseable: manualShutdown() - Manual cleanup");
        open = false;
    }
}

/**
 * Bean demonstrating all destruction callbacks
 */
class MultiDestructionBean implements DisposableBean {
    
    private boolean active = true;
    
    public MultiDestructionBean() {
        System.out.println("\nMultiDestructionBean: Constructor");
    }
    
    public void doWork() {
        System.out.println("MultiDestructionBean: Performing work");
    }
    
    @PreDestroy
    public void preDestroyMethod() {
        System.out.println("1. MultiDestructionBean: @PreDestroy called");
    }
    
    @Override
    public void destroy() throws Exception {
        System.out.println("2. MultiDestructionBean: DisposableBean.destroy() called");
    }
    
    public void customDestroy() {
        System.out.println("3. MultiDestructionBean: Custom destroy method called");
    }
}

/**
 * Demonstration of destruction callback order
 */
class DestructionOrderDemo {
    
    public static void demonstrateOrder() {
        System.out.println("\n=== Destruction Callback Order ===");
        
        AnnotationConfigApplicationContext context = 
                new AnnotationConfigApplicationContext();
        
        // Register bean with all destruction mechanisms
        context.registerBean("multiBean", MultiDestructionBean.class,
                bd -> bd.setDestroyMethodName("customDestroy"));
        
        context.refresh();
        
        MultiDestructionBean bean = context.getBean(MultiDestructionBean.class);
        bean.doWork();
        
        System.out.println("\nClosing context - destruction callbacks will execute:");
        context.close();
        
        System.out.println("\nDestruction Order:");
        System.out.println("  1. @PreDestroy methods");
        System.out.println("  2. DisposableBean.destroy()");
        System.out.println("  3. Custom destroy method");
    }
}

public class DestructionCallbackPattern {
    
    public static void main(String[] args) {
        System.out.println("=== Spring Destruction Callback Pattern Demo ===\n");
        
        // Demo 1: DisposableBean interface
        System.out.println("=== Demo 1: DisposableBean Interface ===");
        demonstrateDisposableBean();
        
        // Demo 2: @PreDestroy annotation
        System.out.println("\n=== Demo 2: @PreDestroy Annotation ===");
        demonstratePreDestroy();
        
        // Demo 3: Custom destroy method
        System.out.println("\n=== Demo 3: Custom Destroy Method ===");
        demonstrateCustomDestroy();
        
        // Demo 4: Closeable interface
        System.out.println("\n=== Demo 4: Closeable Interface ===");
        demonstrateCloseable();
        
        // Demo 5: Destruction callback order
        DestructionOrderDemo.demonstrateOrder();
        
        // Demo 6: Disabling auto-close
        System.out.println("\n=== Demo 6: Disabling Auto-Close ===");
        demonstrateManualClose();
        
        System.out.println("\n=== Demo Completed ===");
    }
    
    private static void demonstrateDisposableBean() {
        System.out.println("Creating context...\n");
        
        AnnotationConfigApplicationContext context = 
                new AnnotationConfigApplicationContext(DestructionConfig.class);
        
        ConnectionPool pool = context.getBean(ConnectionPool.class);
        
        System.out.println("\nUsing ConnectionPool:");
        pool.borrowConnection();
        pool.borrowConnection();
        pool.returnConnection();
        
        System.out.println("\nClosing context (triggers DisposableBean.destroy())...\n");
        context.close();
    }
    
    private static void demonstratePreDestroy() {
        System.out.println("Creating context...\n");
        
        AnnotationConfigApplicationContext context = 
                new AnnotationConfigApplicationContext(DestructionConfig.class);
        
        ResourceHandler handler = context.getBean(ResourceHandler.class);
        
        System.out.println("\nUsing ResourceHandler:");
        handler.processResource("data.txt");
        handler.processResource("config.xml");
        
        System.out.println("\nClosing context (triggers @PreDestroy)...\n");
        context.close();
    }
    
    private static void demonstrateCustomDestroy() {
        System.out.println("Creating context...\n");
        
        AnnotationConfigApplicationContext context = 
                new AnnotationConfigApplicationContext(DestructionConfig.class);
        
        FileManager fileManager = context.getBean(FileManager.class);
        
        System.out.println("\nUsing FileManager:");
        fileManager.openFile("document.pdf");
        fileManager.openFile("image.jpg");
        fileManager.closeFile("document.pdf");
        
        System.out.println("\nClosing context (triggers custom destroy method)...\n");
        context.close();
    }
    
    private static void demonstrateCloseable() {
        System.out.println("Creating context...\n");
        
        AnnotationConfigApplicationContext context = 
                new AnnotationConfigApplicationContext(DestructionConfig.class);
        
        NetworkConnection connection = context.getBean(NetworkConnection.class);
        
        System.out.println("\nUsing NetworkConnection:");
        connection.sendData("Hello, Server!");
        connection.sendData("GET /api/data");
        
        System.out.println("\nClosing context (triggers Closeable.close())...\n");
        context.close();
    }
    
    private static void demonstrateManualClose() {
        System.out.println("Creating context...\n");
        
        AnnotationConfigApplicationContext context = 
                new AnnotationConfigApplicationContext(DestructionConfig.class);
        
        ManualCloseable manual = context.getBean(ManualCloseable.class);
        
        System.out.println("\nUsing ManualCloseable:");
        manual.doWork();
        
        System.out.println("\nManually calling shutdown:");
        manual.manualShutdown();
        
        System.out.println("\nClosing context (should NOT call close() automatically)...\n");
        context.close();
        
        System.out.println("\nNote: destroyMethod=\"\" disables automatic close() invocation");
    }
}

/*
 * Key Takeaways:
 * 
 * 1. Multiple destruction callback mechanisms available
 * 2. Callbacks execute when ApplicationContext is closed
 * 3. Execution order: @PreDestroy -> DisposableBean -> custom destroy
 * 4. Critical for preventing resource leaks
 * 5. Spring auto-detects close() and shutdown() methods
 * 
 * Destruction Mechanisms:
 * - @PreDestroy: JSR-250 annotation, executes first
 * - DisposableBean: Spring interface, executes second
 * - Custom destroy method: Via @Bean(destroyMethod="..."), executes third
 * - Closeable/AutoCloseable: Auto-detected by Spring
 * 
 * Destruction Order:
 * 1. @PreDestroy annotated methods
 * 2. DisposableBean.destroy()
 * 3. Custom destroy method (specified in @Bean)
 * 
 * Auto-Detection:
 * - Spring automatically calls close() on Closeable beans
 * - Spring automatically calls shutdown() on beans with that method
 * - Use destroyMethod="" to disable auto-detection
 * - Use destroyMethod="(inferred)" for auto-detection (default)
 * 
 * Benefits:
 * - Prevents resource leaks
 * - Ensures graceful shutdown
 * - Multiple options for different needs
 * - Standard annotations (JSR-250)
 * - Automatic cleanup support
 * 
 * Use Cases:
 * - Database connection cleanup
 * - File handle closing
 * - Network connection termination
 * - Cache flushing
 * - Thread pool shutdown
 * - Temporary file deletion
 * - Lock release
 * - Session cleanup
 * 
 * Best Practices:
 * - Always implement proper cleanup
 * - Choose appropriate callback mechanism
 * - Handle exceptions in destroy methods
 * - Make destroy methods idempotent
 * - Close resources in reverse order of acquisition
 * - Use try-with-resources when possible
 */
