package com.example.miscellaneous.callback;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * Callback Pattern - Demonstrates Spring's Callback Pattern
 * 
 * This pattern shows how to:
 * 1. Implement callback interfaces
 * 2. Register callbacks for events
 * 3. Execute callbacks asynchronously
 * 4. Handle callback results
 * 5. Implement callback chains
 * 6. Use functional callbacks
 * 7. Handle callback errors
 * 8. Implement timeout callbacks
 * 9. Create callback registries
 * 10. Use callback templates
 * 
 * Key Concepts:
 * - Callback: Code executed at a later time or on event
 * - Asynchronous Execution: Non-blocking callback execution
 * - Event-Driven: Callbacks triggered by events
 * - Template-Callback: Template method with callback hook
 * - Functional Interface: Java 8+ callback interfaces
 * 
 * Callback Types:
 * 1. Success Callback: Executed on successful operation
 * 2. Error Callback: Executed on error
 * 3. Completion Callback: Executed when operation completes
 * 4. Progress Callback: Executed during operation progress
 * 5. Timeout Callback: Executed on timeout
 * 
 * Use Cases:
 * - Asynchronous operations
 * - Event handling
 * - Resource management
 * - Template methods
 * - Plugin architectures
 * 
 * Dependencies:
 * - spring-context
 * - spring-boot-starter-web
 * 
 * @author Spring Patterns
 * @version 1.0
 */
@SpringBootApplication
public class CallbackPattern {
    
    public static void main(String[] args) {
        var context = SpringApplication.run(CallbackPattern.class, args);
        demonstrateCallbacks(context);
    }
    
    /**
     * Demonstrates various callback patterns
     */
    private static void demonstrateCallbacks(org.springframework.context.ApplicationContext context) {
        System.out.println("=== Callback Pattern Demonstrations ===\n");
        
        CallbackService service = context.getBean(CallbackService.class);
        
        // Demo 1: Simple callback
        demonstrateSimpleCallback(service);
        
        // Demo 2: Async callback
        demonstrateAsyncCallback(service);
        
        // Demo 3: Callback with result
        demonstrateCallbackWithResult(service);
        
        // Demo 4: Callback chain
        demonstrateCallbackChain(service);
        
        // Demo 5: Error callback
        demonstrateErrorCallback(service);
    }
    
    /**
     * Demonstrates simple callback execution
     */
    private static void demonstrateSimpleCallback(CallbackService service) {
        System.out.println("1. Simple Callback:");
        
        service.executeWithCallback("Task1", () -> {
            System.out.println("   Callback executed for Task1");
        });
        
        System.out.println();
    }
    
    /**
     * Demonstrates async callback
     */
    private static void demonstrateAsyncCallback(CallbackService service) {
        System.out.println("2. Async Callback:");
        
        service.executeAsync("AsyncTask", new AsyncCallback<String>() {
            @Override
            public void onSuccess(String result) {
                System.out.println("   Async success: " + result);
            }
            
            @Override
            public void onError(Exception e) {
                System.out.println("   Async error: " + e.getMessage());
            }
        });
        
        System.out.println("   Async operation started...");
        System.out.println();
    }
    
    /**
     * Demonstrates callback with result
     */
    private static void demonstrateCallbackWithResult(CallbackService service) {
        System.out.println("3. Callback with Result:");
        
        String result = service.executeWithResult("ResultTask", data -> {
            return "Processed: " + data;
        });
        
        System.out.println("   Result: " + result);
        System.out.println();
    }
    
    /**
     * Demonstrates callback chain
     */
    private static void demonstrateCallbackChain(CallbackService service) {
        System.out.println("4. Callback Chain:");
        
        service.executeChain("ChainTask")
            .then(result -> {
                System.out.println("   First callback: " + result);
                return result.toUpperCase();
            })
            .then(result -> {
                System.out.println("   Second callback: " + result);
                return result + "!";
            })
            .execute();
        
        System.out.println();
    }
    
    /**
     * Demonstrates error callback
     */
    private static void demonstrateErrorCallback(CallbackService service) {
        System.out.println("5. Error Callback:");
        
        service.executeWithErrorHandling("ErrorTask", 
            () -> {
                throw new RuntimeException("Simulated error");
            },
            e -> {
                System.out.println("   Error handled: " + e.getMessage());
            }
        );
        
        System.out.println();
    }
}

// ============================================================================
// Callback Interfaces
// ============================================================================

/**
 * Simple callback interface
 */
@FunctionalInterface
interface Callback {
    void execute();
}

/**
 * Callback with parameter
 */
@FunctionalInterface
interface ParameterizedCallback<T> {
    void execute(T parameter);
}

/**
 * Callback with result
 */
@FunctionalInterface
interface ResultCallback<T, R> {
    R execute(T parameter);
}

/**
 * Async callback with success and error handling
 */
interface AsyncCallback<T> {
    void onSuccess(T result);
    void onError(Exception exception);
}

/**
 * Progress callback
 */
@FunctionalInterface
interface ProgressCallback {
    void onProgress(int current, int total);
}

/**
 * Completion callback
 */
@FunctionalInterface
interface CompletionCallback<T> {
    void onComplete(T result);
}

// ============================================================================
// Callback Service
// ============================================================================

/**
 * Service demonstrating various callback patterns
 */
@Service
class CallbackService {
    
    private final ExecutorService executor = Executors.newFixedThreadPool(5);
    
    /**
     * Execute operation with simple callback
     */
    public void executeWithCallback(String taskName, Callback callback) {
        System.out.println("   Executing: " + taskName);
        // Perform operation
        callback.execute();
    }
    
    /**
     * Execute operation asynchronously with callback
     */
    public void executeAsync(String taskName, AsyncCallback<String> callback) {
        executor.submit(() -> {
            try {
                Thread.sleep(100); // Simulate work
                String result = "Completed: " + taskName;
                callback.onSuccess(result);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
    
    /**
     * Execute operation with result callback
     */
    public <R> R executeWithResult(String taskName, ResultCallback<String, R> callback) {
        System.out.println("   Executing: " + taskName);
        return callback.execute(taskName);
    }
    
    /**
     * Execute operation with error handling
     */
    public void executeWithErrorHandling(String taskName, 
                                        Callback operation,
                                        Consumer<Exception> errorCallback) {
        try {
            System.out.println("   Attempting: " + taskName);
            operation.execute();
        } catch (Exception e) {
            errorCallback.accept(e);
        }
    }
    
    /**
     * Execute operation with progress callback
     */
    public void executeWithProgress(String taskName, 
                                   int totalSteps, 
                                   ProgressCallback callback) {
        for (int i = 1; i <= totalSteps; i++) {
            callback.onProgress(i, totalSteps);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    /**
     * Create callback chain
     */
    public CallbackChain executeChain(String taskName) {
        return new CallbackChain(taskName);
    }
}

// ============================================================================
// Callback Chain
// ============================================================================

/**
 * Chainable callback implementation
 */
class CallbackChain {
    private final String taskName;
    private final List<ResultCallback<String, String>> callbacks = new ArrayList<>();
    
    public CallbackChain(String taskName) {
        this.taskName = taskName;
    }
    
    public CallbackChain then(ResultCallback<String, String> callback) {
        callbacks.add(callback);
        return this;
    }
    
    public String execute() {
        String result = taskName;
        for (ResultCallback<String, String> callback : callbacks) {
            result = callback.execute(result);
        }
        return result;
    }
}

// ============================================================================
// Callback Registry
// ============================================================================

/**
 * Registry for managing callbacks
 */
@Component
class CallbackRegistry {
    
    private final Map<String, List<Callback>> callbacks = new ConcurrentHashMap<>();
    
    /**
     * Register a callback for an event
     */
    public void register(String eventName, Callback callback) {
        callbacks.computeIfAbsent(eventName, k -> new CopyOnWriteArrayList<>())
                 .add(callback);
    }
    
    /**
     * Unregister a callback
     */
    public void unregister(String eventName, Callback callback) {
        List<Callback> eventCallbacks = callbacks.get(eventName);
        if (eventCallbacks != null) {
            eventCallbacks.remove(callback);
        }
    }
    
    /**
     * Trigger all callbacks for an event
     */
    public void trigger(String eventName) {
        List<Callback> eventCallbacks = callbacks.get(eventName);
        if (eventCallbacks != null) {
            eventCallbacks.forEach(Callback::execute);
        }
    }
    
    /**
     * Clear all callbacks for an event
     */
    public void clear(String eventName) {
        callbacks.remove(eventName);
    }
    
    /**
     * Clear all callbacks
     */
    public void clearAll() {
        callbacks.clear();
    }
}

// ============================================================================
// Callback Template
// ============================================================================

/**
 * Template for callback-based operations
 */
abstract class CallbackTemplate<T> {
    
    /**
     * Execute operation with callbacks
     */
    public final T execute() {
        beforeExecution();
        
        T result = null;
        try {
            result = doExecute();
            onSuccess(result);
        } catch (Exception e) {
            onError(e);
            throw e;
        } finally {
            afterExecution();
        }
        
        return result;
    }
    
    /**
     * Template method - to be implemented by subclasses
     */
    protected abstract T doExecute();
    
    /**
     * Hook: called before execution
     */
    protected void beforeExecution() {
        // Default: do nothing
    }
    
    /**
     * Hook: called after execution
     */
    protected void afterExecution() {
        // Default: do nothing
    }
    
    /**
     * Hook: called on success
     */
    protected void onSuccess(T result) {
        // Default: do nothing
    }
    
    /**
     * Hook: called on error
     */
    protected void onError(Exception e) {
        // Default: do nothing
    }
}

// ============================================================================
// Domain Models
// ============================================================================

/**
 * Operation result
 */
class OperationResult<T> {
    private final T data;
    private final boolean success;
    private final String message;
    private final LocalDateTime timestamp;
    
    public OperationResult(T data, boolean success, String message) {
        this.data = data;
        this.success = success;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }
    
    // Getters
    public T getData() { return data; }
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public LocalDateTime getTimestamp() { return timestamp; }
    
    @Override
    public String toString() {
        return "OperationResult{success=" + success + ", message='" + message + "', timestamp=" + timestamp + '}';
    }
}

/**
 * Callback task
 */
class CallbackTask<T> {
    private final String id;
    private final String name;
    private final Callable<T> operation;
    private final List<Consumer<T>> successCallbacks;
    private final List<Consumer<Exception>> errorCallbacks;
    
    public CallbackTask(String id, String name, Callable<T> operation) {
        this.id = id;
        this.name = name;
        this.operation = operation;
        this.successCallbacks = new ArrayList<>();
        this.errorCallbacks = new ArrayList<>();
    }
    
    public CallbackTask<T> onSuccess(Consumer<T> callback) {
        successCallbacks.add(callback);
        return this;
    }
    
    public CallbackTask<T> onError(Consumer<Exception> callback) {
        errorCallbacks.add(callback);
        return this;
    }
    
    public void execute() {
        try {
            T result = operation.call();
            successCallbacks.forEach(cb -> cb.accept(result));
        } catch (Exception e) {
            errorCallbacks.forEach(cb -> cb.accept(e));
        }
    }
    
    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
}

// ============================================================================
// REST Controller
// ============================================================================

/**
 * Controller demonstrating callback patterns
 */
@RestController
@RequestMapping("/api/callbacks")
class CallbackController {
    
    private final CallbackService callbackService;
    private final CallbackRegistry callbackRegistry;
    
    public CallbackController(CallbackService callbackService, 
                            CallbackRegistry callbackRegistry) {
        this.callbackService = callbackService;
        this.callbackRegistry = callbackRegistry;
    }
    
    /**
     * Execute task with callback
     */
    @PostMapping("/execute")
    public ResponseEntity<Map<String, String>> executeWithCallback(
            @RequestBody Map<String, String> request) {
        
        String taskName = request.get("taskName");
        
        callbackService.executeWithCallback(taskName, () -> {
            System.out.println("Callback executed for: " + taskName);
        });
        
        Map<String, String> response = new HashMap<>();
        response.put("status", "executed");
        response.put("task", taskName);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Execute async task with callback
     */
    @PostMapping("/execute-async")
    public ResponseEntity<Map<String, String>> executeAsync(
            @RequestBody Map<String, String> request) {
        
        String taskName = request.get("taskName");
        
        callbackService.executeAsync(taskName, new AsyncCallback<String>() {
            @Override
            public void onSuccess(String result) {
                System.out.println("Async success: " + result);
            }
            
            @Override
            public void onError(Exception e) {
                System.out.println("Async error: " + e.getMessage());
            }
        });
        
        Map<String, String> response = new HashMap<>();
        response.put("status", "started");
        response.put("task", taskName);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Register callback for event
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> registerCallback(
            @RequestParam String eventName) {
        
        callbackRegistry.register(eventName, () -> {
            System.out.println("Event triggered: " + eventName);
        });
        
        Map<String, String> response = new HashMap<>();
        response.put("status", "registered");
        response.put("event", eventName);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Trigger event callbacks
     */
    @PostMapping("/trigger")
    public ResponseEntity<Map<String, String>> triggerEvent(
            @RequestParam String eventName) {
        
        callbackRegistry.trigger(eventName);
        
        Map<String, String> response = new HashMap<>();
        response.put("status", "triggered");
        response.put("event", eventName);
        
        return ResponseEntity.ok(response);
    }
}
