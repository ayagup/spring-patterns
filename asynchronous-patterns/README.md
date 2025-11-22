# Asynchronous Processing Patterns

Comprehensive guide to asynchronous processing patterns in Spring and Java applications.

## Table of Contents

1. [Overview](#overview)
2. [Future and Callable Pattern](#future-and-callable-pattern)
3. [CompletableFuture Pattern](#completablefuture-pattern)
4. [ListenableFuture Pattern](#listenablefuture-pattern)
5. [DeferredResult Pattern](#deferredresult-pattern)
6. [Async Method and @Async Annotation Pattern](#async-method-and-async-annotation-pattern)
7. [Event Loop and Non-blocking I/O Pattern](#event-loop-and-non-blocking-io-pattern)
8. [Callback and Promise Pattern](#callback-and-promise-pattern)
9. [Comparison Matrix](#comparison-matrix)
10. [Best Practices](#best-practices)
11. [Production Checklist](#production-checklist)

---

## Overview

Asynchronous processing is crucial for building scalable, responsive applications. This guide covers 11 essential patterns for handling async operations in Spring and Java.

### Why Asynchronous Processing?

✓ **Improved Responsiveness** - UI/API remains responsive during long operations  
✓ **Better Resource Utilization** - Threads aren't blocked waiting for I/O  
✓ **Scalability** - Handle more concurrent requests with fewer threads  
✓ **Parallelism** - Execute multiple operations simultaneously  
✓ **Non-blocking I/O** - Process thousands of connections efficiently

### When to Use Async

**Use When:**
- I/O-bound operations (database, network, file)
- Long-running computations
- External API calls
- Batch processing
- Real-time notifications
- High concurrency requirements

**Avoid When:**
- Simple, fast operations
- Operations requiring immediate results
- Sequential dependencies
- Debugging complexity is unacceptable

---

## Future and Callable Pattern

### Purpose

Execute tasks asynchronously and retrieve results later using Java's core concurrency utilities.

### Key Components

```java
// Callable - Task that returns a result
Callable<Integer> task = () -> {
    Thread.sleep(1000);
    return 42;
};

// ExecutorService - Manages thread pool
ExecutorService executor = Executors.newFixedThreadPool(5);

// Future - Handle to retrieve result
Future<Integer> future = executor.submit(task);

// Get result (blocks until ready)
Integer result = future.get();

// With timeout
Integer result = future.get(5, TimeUnit.SECONDS);

// Cancel task
boolean cancelled = future.cancel(true);
```

### Executor Types

| Executor | Description | Use Case |
|----------|-------------|----------|
| `newFixedThreadPool(n)` | Fixed number of threads | Controlled concurrency |
| `newCachedThreadPool()` | Grows as needed | Unpredictable workload |
| `newSingleThreadExecutor()` | Single worker thread | Sequential processing |
| `newScheduledThreadPool(n)` | Scheduled execution | Periodic tasks |

### Features

**Advantages:**
- ✓ Simple, straightforward API
- ✓ Built into Java standard library
- ✓ Thread pool management
- ✓ Task cancellation support
- ✓ Timeout handling

**Limitations:**
- ✗ Blocking `get()` method
- ✗ No callback support
- ✗ Limited error handling
- ✗ Cannot chain operations
- ✗ No composition support

### Use Cases

```java
// 1. Parallel data processing
List<Future<Result>> futures = new ArrayList<>();
for (Data data : dataList) {
    Future<Result> future = executor.submit(() -> process(data));
    futures.add(future);
}

// Wait for all results
List<Result> results = new ArrayList<>();
for (Future<Result> future : futures) {
    results.add(future.get());
}

// 2. Timeout handling
try {
    Result result = future.get(5, TimeUnit.SECONDS);
} catch (TimeoutException e) {
    future.cancel(true);
    // Handle timeout
}

// 3. Check status without blocking
if (future.isDone()) {
    Result result = future.get(); // Won't block
}
```

### Best Practices

✓ Always handle `ExecutionException`  
✓ Set timeouts to prevent indefinite blocking  
✓ Cancel tasks when no longer needed  
✓ Shutdown executor service properly  
✓ Use appropriate thread pool size  
✓ Monitor thread pool metrics

### Thread Pool Sizing

```java
// CPU-intensive tasks
int cpuCount = Runtime.getRuntime().availableProcessors();
int poolSize = cpuCount + 1;

// I/O-intensive tasks
int poolSize = cpuCount * 2;

// Mixed workload
int poolSize = (int) (cpuCount / (1 - blockingCoefficient));
```

---

## CompletableFuture Pattern

### Purpose

Build non-blocking asynchronous pipelines with composition, chaining, and enhanced error handling.

### Key Methods

**Creation:**
```java
// Async with result
CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
    return fetchData();
});

// Async without result
CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
    processData();
});

// Already completed
CompletableFuture<String> future = CompletableFuture.completedFuture("value");
```

**Transformation:**
```java
// Transform result
CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> "123")
    .thenApply(Integer::parseInt);

// Consume result
future.thenAccept(result -> System.out.println(result));

// Run after completion
future.thenRun(() -> System.out.println("Done"));
```

**Composition:**
```java
// Chain dependent async operations
CompletableFuture<User> userFuture = getUserAsync(userId);
CompletableFuture<List<Order>> ordersFuture = userFuture
    .thenCompose(user -> getOrdersAsync(user.getId()));

// Combine independent futures
CompletableFuture<String> combined = userFuture.thenCombine(
    ordersFuture,
    (user, orders) -> user.getName() + " has " + orders.size() + " orders"
);
```

**Combining Multiple Futures:**
```java
// Wait for all
CompletableFuture<Void> all = CompletableFuture.allOf(
    future1, future2, future3
);

all.thenRun(() -> {
    // All completed
    Result1 r1 = future1.join();
    Result2 r2 = future2.join();
    Result3 r3 = future3.join();
});

// First to complete
CompletableFuture<Object> any = CompletableFuture.anyOf(
    future1, future2, future3
);
```

**Error Handling:**
```java
// Handle exception
CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
    if (condition) throw new RuntimeException("Error");
    return "Success";
}).exceptionally(ex -> {
    return "Fallback value";
});

// Handle both result and exception
future.handle((result, ex) -> {
    if (ex != null) {
        return "Error: " + ex.getMessage();
    }
    return result;
});

// Cleanup (always executed)
future.whenComplete((result, ex) -> {
    // Cleanup resources
});
```

### Async Variants

All transformation methods have async variants that execute on a different thread:

```java
// Execute on common ForkJoinPool
future.thenApplyAsync(transformation);

// Execute on custom executor
ExecutorService executor = Executors.newFixedThreadPool(10);
future.thenApplyAsync(transformation, executor);
```

### Advanced Patterns

**1. Timeout Handling (Java 9+):**
```java
CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
    // Long operation
})
.orTimeout(5, TimeUnit.SECONDS)
.exceptionally(ex -> "Timeout");
```

**2. Fallback on Timeout:**
```java
future.completeOnTimeout("Default value", 3, TimeUnit.SECONDS);
```

**3. Retry Pattern:**
```java
public <T> CompletableFuture<T> retryAsync(Supplier<T> operation, int maxRetries) {
    CompletableFuture<T> future = CompletableFuture.supplyAsync(operation);
    
    for (int i = 0; i < maxRetries; i++) {
        future = future.exceptionally(ex -> null)
            .thenCompose(result -> {
                if (result == null) {
                    return CompletableFuture.supplyAsync(operation);
                }
                return CompletableFuture.completedFuture(result);
            });
    }
    
    return future;
}
```

### Use Cases

✓ Microservices orchestration  
✓ Parallel API calls  
✓ Database + cache queries  
✓ Event-driven workflows  
✓ Async notification pipelines  
✓ Complex data transformation pipelines

### Best Practices

✓ Use `thenApply` for transformations  
✓ Use `thenCompose` for chaining async operations  
✓ Use `thenCombine` for independent parallel operations  
✓ Always handle exceptions with `exceptionally`/`handle`  
✓ Use custom executors for CPU-intensive tasks  
✓ Avoid blocking with `get()` - use `thenAccept`  
✓ Set timeouts for external operations  
✓ Use `join()` instead of `get()` to avoid checked exceptions

---

## ListenableFuture Pattern

### Purpose

Spring's enhanced Future with callback support for non-blocking result handling.

### Key Components

```java
// Spring's ListenableFuture interface
public interface ListenableFuture<T> extends Future<T> {
    void addCallback(ListenableFutureCallback<? super T> callback);
    void addCallback(SuccessCallback<? super T> successCallback, 
                    FailureCallback failureCallback);
}

// Callback interface
public interface ListenableFutureCallback<T> {
    void onSuccess(T result);
    void onFailure(Throwable ex);
}
```

### Usage

**Basic Callback:**
```java
ListenableFuture<String> future = asyncTaskExecutor.submitListenable(task);

future.addCallback(
    result -> System.out.println("Success: " + result),
    error -> System.err.println("Error: " + error.getMessage())
);
```

**Full Callback:**
```java
future.addCallback(new ListenableFutureCallback<String>() {
    @Override
    public void onSuccess(String result) {
        // Handle success
    }
    
    @Override
    public void onFailure(Throwable ex) {
        // Handle error
    }
});
```

**With @Async:**
```java
@Service
public class EmailService {
    
    @Async
    public ListenableFuture<String> sendEmailAsync(String to, String subject) {
        // Send email
        return new AsyncResult<>("Email sent to " + to);
    }
}

// Usage
ListenableFuture<String> future = emailService.sendEmailAsync("user@example.com", "Hello");
future.addCallback(
    result -> log.info("Email sent successfully"),
    error -> log.error("Email failed", error)
);
```

### Features

**Advantages:**
- ✓ Non-blocking callbacks
- ✓ Success/failure handlers
- ✓ Better error handling than Future
- ✓ Multiple callbacks per future
- ✓ Spring integration
- ✓ @Async method support

**Limitations:**
- ✗ Less powerful than CompletableFuture
- ✗ Limited composition support
- ✗ Spring-specific
- ✗ Chaining is cumbersome

### Use Cases

✓ Async email sending  
✓ Payment processing  
✓ External API calls  
✓ Background job processing  
✓ Notification systems  
✓ Spring async methods

### Best Practices

✓ Always add both success and failure callbacks  
✓ Handle errors gracefully in callbacks  
✓ Don't block in callback methods  
✓ Use appropriate thread pool size  
✓ Set timeouts for long operations  
✓ Log callback execution for debugging  
✓ Consider CompletableFuture for complex chains

---

## DeferredResult Pattern

### Purpose

Handle asynchronous request processing in Spring MVC for long-polling, real-time updates, and background jobs.

### Key Concepts

```java
@RestController
public class AsyncController {
    
    @GetMapping("/long-poll")
    public DeferredResult<String> longPolling() {
        DeferredResult<String> deferredResult = new DeferredResult<>(30000L);
        
        // Set result later from another thread
        executorService.submit(() -> {
            Thread.sleep(5000);
            deferredResult.setResult("Data available");
        });
        
        return deferredResult;
    }
}
```

### Configuration

```java
// Constructor options
new DeferredResult<>();                          // No timeout
new DeferredResult<>(5000L);                     // 5 second timeout
new DeferredResult<>(5000L, "Timeout response"); // With timeout value
```

### Callbacks

```java
DeferredResult<String> result = new DeferredResult<>(30000L);

// Timeout callback
result.onTimeout(() -> {
    log.warn("Request timed out");
    result.setResult("No updates available");
});

// Completion callback
result.onCompletion(() -> {
    log.info("Request completed");
    // Cleanup resources
});

// Error callback
result.onError(throwable -> {
    log.error("Request error", throwable);
});
```

### Patterns

**1. Long Polling:**
```java
@RestController
public class NotificationController {
    
    private final Map<String, DeferredResult<String>> pendingRequests = new ConcurrentHashMap<>();
    
    @GetMapping("/notifications")
    public DeferredResult<String> getNotifications(@RequestParam String userId) {
        DeferredResult<String> result = new DeferredResult<>(60000L, "No notifications");
        
        pendingRequests.put(userId, result);
        
        result.onCompletion(() -> pendingRequests.remove(userId));
        
        return result;
    }
    
    // Called when notification is ready
    public void sendNotification(String userId, String message) {
        DeferredResult<String> result = pendingRequests.remove(userId);
        if (result != null) {
            result.setResult(message);
        }
    }
}
```

**2. Async External API Call:**
```java
@GetMapping("/weather/{city}")
public DeferredResult<WeatherResponse> getWeather(@PathVariable String city) {
    DeferredResult<WeatherResponse> result = new DeferredResult<>(5000L);
    
    result.onTimeout(() -> {
        result.setResult(new WeatherResponse("Timeout"));
    });
    
    weatherService.fetchWeatherAsync(city, new AsyncCallback<WeatherResponse>() {
        @Override
        public void onSuccess(WeatherResponse response) {
            result.setResult(response);
        }
        
        @Override
        public void onError(Throwable error) {
            result.setErrorResult(error);
        }
    });
    
    return result;
}
```

**3. Background Job Status:**
```java
@GetMapping("/jobs/{jobId}/status")
public DeferredResult<JobStatus> getJobStatus(@PathVariable String jobId) {
    DeferredResult<JobStatus> result = new DeferredResult<>(10000L);
    
    jobService.monitorJob(jobId, status -> {
        if (status.isComplete()) {
            result.setResult(status);
        }
    });
    
    return result;
}
```

### Use Cases

✓ Long polling for real-time updates  
✓ Server-Sent Events (SSE)  
✓ Async external API calls  
✓ Background job status polling  
✓ Chat applications  
✓ Live dashboards  
✓ Notification systems

### Best Practices

✓ Always set a timeout  
✓ Provide meaningful timeout results  
✓ Handle errors properly  
✓ Use `onCompletion` for cleanup  
✓ Monitor active deferred results  
✓ Don't hold too many pending requests  
✓ Consider WebSocket for bidirectional communication

### DeferredResult vs Callable

| Feature | DeferredResult | Callable |
|---------|---------------|----------|
| Result Setting | From any thread | Must return from call() |
| Thread Management | External control | Spring managed |
| Use Case | Event-driven, long polling | Simple async |
| Flexibility | High | Low |
| Complexity | Medium | Low |

---

## Async Method and @Async Annotation Pattern

### Purpose

Enable declarative asynchronous method execution with Spring's @Async annotation.

### Configuration

**1. Enable Async Processing:**
```java
@Configuration
@EnableAsync
public class AsyncConfiguration {
    
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(25);
        executor.setThreadNamePrefix("Async-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }
}
```

**2. Implementing AsyncConfigurer:**
```java
@Configuration
@EnableAsync
public class AsyncConfiguration implements AsyncConfigurer {
    
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("MyApp-");
        executor.initialize();
        return executor;
    }
    
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new CustomAsyncExceptionHandler();
    }
}

public class CustomAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {
    
    @Override
    public void handleUncaughtException(Throwable ex, Method method, Object... params) {
        log.error("Async method '{}' threw exception", method.getName(), ex);
        log.error("Method parameters: {}", Arrays.toString(params));
    }
}
```

### @Async Return Types

**1. Void (Fire and Forget):**
```java
@Service
public class NotificationService {
    
    @Async
    public void sendNotification(String userId, String message) {
        // Async execution - no return value
        emailService.send(userId, message);
    }
}
```

**2. Future:**
```java
@Service
public class DataService {
    
    @Async
    public Future<Data> fetchDataAsync(String id) {
        Data data = repository.findById(id);
        return new AsyncResult<>(data);
    }
}

// Usage
Future<Data> future = dataService.fetchDataAsync("123");
Data data = future.get(); // Blocking
```

**3. CompletableFuture:**
```java
@Service
public class UserService {
    
    @Async
    public CompletableFuture<User> getUserAsync(String userId) {
        User user = userRepository.findById(userId);
        return CompletableFuture.completedFuture(user);
    }
}

// Usage
CompletableFuture<User> future = userService.getUserAsync("user-123");
future.thenAccept(user -> {
    // Non-blocking callback
    processUser(user);
});
```

**4. ListenableFuture:**
```java
@Service
public class OrderService {
    
    @Async
    public ListenableFuture<Order> getOrderAsync(String orderId) {
        Order order = orderRepository.findById(orderId);
        return new AsyncResult<>(order);
    }
}

// Usage
ListenableFuture<Order> future = orderService.getOrderAsync("order-456");
future.addCallback(
    order -> log.info("Order: {}", order),
    error -> log.error("Error", error)
);
```

### Thread Pool Configuration

**Thread Pool Parameters:**
```java
executor.setCorePoolSize(10);           // Minimum threads
executor.setMaxPoolSize(50);            // Maximum threads
executor.setQueueCapacity(100);         // Queue size
executor.setKeepAliveSeconds(60);       // Idle thread timeout
executor.setThreadNamePrefix("Async-"); // Thread naming
executor.setRejectedExecutionHandler(  // Rejection policy
    new ThreadPoolExecutor.CallerRunsPolicy()
);
```

**Sizing Guidelines:**

| Workload | Formula | Example (8 cores) |
|----------|---------|-------------------|
| CPU-intensive | cores + 1 | 9 |
| I/O-intensive | cores * 2 | 16 |
| Mixed | cores / (1 - blocking coefficient) | 20 (if 60% I/O) |

**Multiple Executors:**
```java
@Configuration
@EnableAsync
public class AsyncConfiguration {
    
    @Bean(name = "ioExecutor")
    public Executor ioExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(20);
        executor.setMaxPoolSize(50);
        executor.setThreadNamePrefix("IO-");
        executor.initialize();
        return executor;
    }
    
    @Bean(name = "cpuExecutor")
    public Executor cpuExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(9);
        executor.setMaxPoolSize(9);
        executor.setThreadNamePrefix("CPU-");
        executor.initialize();
        return executor;
    }
}

// Usage
@Service
public class ProcessingService {
    
    @Async("ioExecutor")
    public void processIO() {
        // I/O-intensive work
    }
    
    @Async("cpuExecutor")
    public void processCPU() {
        // CPU-intensive work
    }
}
```

### Exception Handling

**For void methods:**
```java
@Configuration
@EnableAsync
public class AsyncConfiguration implements AsyncConfigurer {
    
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) -> {
            log.error("Async exception in {}", method.getName(), ex);
            // Send alert, log to monitoring system, etc.
        };
    }
}
```

**For Future/CompletableFuture:**
```java
@Async
public CompletableFuture<String> processAsync() {
    return CompletableFuture.supplyAsync(() -> {
        // Process
        if (error) throw new RuntimeException("Error");
        return "Success";
    }).exceptionally(ex -> {
        log.error("Error in async processing", ex);
        return "Fallback";
    });
}
```

### Use Cases

✓ Email/SMS notifications  
✓ Report generation  
✓ Batch processing  
✓ File upload/download  
✓ External API calls  
✓ Background data synchronization  
✓ Audit logging  
✓ Cache warming  
✓ Analytics processing

### Common Pitfalls

✗ **Self-invocation doesn't work** - @Async uses proxies
```java
// Won't work - calling @Async method from same class
@Service
public class MyService {
    public void doSomething() {
        this.asyncMethod(); // NOT ASYNC!
    }
    
    @Async
    public void asyncMethod() {
        // ...
    }
}

// Solution: Inject the service
@Service
public class MyService {
    @Autowired
    private MyService self;
    
    public void doSomething() {
        self.asyncMethod(); // Now async
    }
    
    @Async
    public void asyncMethod() {
        // ...
    }
}
```

✗ **Forgetting @EnableAsync**  
✗ **Not handling exceptions in void methods**  
✗ **Using default thread pool for all tasks**  
✗ **Blocking operations in async methods**  
✗ **Not setting proper thread pool limits**

### Best Practices

✓ Configure appropriate thread pool size  
✓ Use meaningful thread name prefixes  
✓ Set queue capacity to prevent memory issues  
✓ Implement `AsyncUncaughtExceptionHandler`  
✓ Use CompletableFuture for complex chains  
✓ Don't call @Async methods from same class  
✓ Monitor thread pool metrics  
✓ Set graceful shutdown with `awaitTermination`  
✓ Use separate executors for different workloads  
✓ Log async method execution for debugging

---

## Event Loop and Non-blocking I/O Pattern

### Purpose

Handle many connections efficiently with single-threaded event processing and non-blocking I/O operations.

### Event Loop Concepts

**Components:**
- **Event Queue** - Stores pending events
- **Event Loop** - Processes events sequentially
- **Event Handlers** - React to specific events
- **Single Thread** - All processing in one thread

**Flow:**
```
1. Events added to queue
2. Loop picks next event
3. Dispatches to handler
4. Handler processes event
5. Repeat
```

### Simple Event Loop

```java
public class SimpleEventLoop {
    private final BlockingQueue<Event> eventQueue = new LinkedBlockingQueue<>();
    private final Map<String, List<EventHandler>> handlers = new ConcurrentHashMap<>();
    private volatile boolean running = false;
    
    public void on(String eventType, EventHandler handler) {
        handlers.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>())
                .add(handler);
    }
    
    public void emit(Event event) {
        eventQueue.offer(event);
    }
    
    public void start() {
        running = true;
        new Thread(() -> {
            while (running) {
                try {
                    Event event = eventQueue.poll(100, TimeUnit.MILLISECONDS);
                    if (event != null) {
                        processEvent(event);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "EventLoop").start();
    }
    
    private void processEvent(Event event) {
        List<EventHandler> eventHandlers = handlers.get(event.getType());
        if (eventHandlers != null) {
            for (EventHandler handler : eventHandlers) {
                handler.handle(event);
            }
        }
    }
}

// Usage
EventLoop eventLoop = new EventLoop();

eventLoop.on("user.join", event -> {
    String username = (String) event.getData();
    System.out.println(username + " joined");
});

eventLoop.start();
eventLoop.emit(new Event("user.join", "Alice"));
```

### Non-blocking I/O (NIO)

**Key Components:**

| Component | Description |
|-----------|-------------|
| Selector | Multiplexes multiple channels |
| Channel | Connection to I/O device |
| Buffer | Container for data |
| SelectionKey | Channel registration with selector |

**Operations:**
- `OP_ACCEPT` - Accept new connections
- `OP_CONNECT` - Connection established
- `OP_READ` - Data ready to read
- `OP_WRITE` - Ready to write data

**NIO Server Example:**
```java
public class NonBlockingServer {
    
    private Selector selector;
    private ServerSocketChannel serverChannel;
    
    public void start(int port) throws IOException {
        selector = Selector.open();
        serverChannel = ServerSocketChannel.open();
        serverChannel.bind(new InetSocketAddress(port));
        serverChannel.configureBlocking(false);
        
        // Register for ACCEPT events
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        
        eventLoop();
    }
    
    private void eventLoop() throws IOException {
        while (true) {
            // Select ready channels
            selector.select();
            
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectedKeys.iterator();
            
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();
                
                if (key.isAcceptable()) {
                    handleAccept(key);
                } else if (key.isReadable()) {
                    handleRead(key);
                } else if (key.isWritable()) {
                    handleWrite(key);
                }
            }
        }
    }
    
    private void handleAccept(SelectionKey key) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = serverChannel.accept();
        clientChannel.configureBlocking(false);
        clientChannel.register(selector, SelectionKey.OP_READ);
    }
    
    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        
        int bytesRead = channel.read(buffer);
        if (bytesRead == -1) {
            channel.close();
            return;
        }
        
        // Process data
        buffer.flip();
        // ...
    }
    
    private void handleWrite(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = (ByteBuffer) key.attachment();
        channel.write(buffer);
    }
}
```

### Reactor Pattern

**Purpose:** Demultiplex and dispatch events

**Components:**
- Resources (I/O sources)
- Synchronous Event Demultiplexer (Selector)
- Dispatcher (Event loop)
- Request Handlers

```java
public class Reactor {
    private final EventLoop eventLoop;
    private final Map<String, Consumer<Object>> reactors = new ConcurrentHashMap<>();
    
    public void register(String eventType, Consumer<Object> reactor) {
        reactors.put(eventType, reactor);
        eventLoop.on(eventType, event -> {
            reactors.get(event.getType()).accept(event.getData());
        });
    }
    
    public void dispatch(String eventType, Object data) {
        eventLoop.emit(new Event(eventType, data));
    }
}

// Usage
Reactor reactor = new Reactor();

// Data pipeline
reactor.register("data.received", data -> {
    System.out.println("Processing: " + data);
    reactor.dispatch("data.validated", data);
});

reactor.register("data.validated", data -> {
    String processed = ((String) data).toUpperCase();
    reactor.dispatch("data.transformed", processed);
});

reactor.register("data.transformed", data -> {
    System.out.println("Storing: " + data);
});

reactor.dispatch("data.received", "sample data");
```

### Benefits

✓ High scalability (1000s of connections)  
✓ Low resource usage  
✓ No thread-per-request overhead  
✓ Predictable latency  
✓ Simple concurrency model  
✓ Efficient for I/O-bound workloads

### Limitations

✗ Single-threaded (one CPU core)  
✗ Not ideal for CPU-intensive tasks  
✗ Callback complexity  
✗ Debugging can be harder  
✗ Error in handler blocks loop

### Use Cases

✓ Web servers (Node.js, Netty)  
✓ Chat servers  
✓ Real-time applications  
✓ Proxy servers  
✓ Message brokers  
✓ IoT gateways

### Best Practices

✓ Don't block the event loop  
✓ Keep handlers lightweight  
✓ Use worker threads for CPU-intensive tasks  
✓ Handle errors in handlers  
✓ Monitor queue size  
✓ Set appropriate buffer sizes  
✓ Use timeouts for operations

### Blocking vs Non-blocking

| Aspect | Blocking I/O | Non-blocking I/O |
|--------|--------------|------------------|
| Threading | Thread per connection | Single thread, many connections |
| Scalability | Limited (threads expensive) | High (thousands of connections) |
| Resource Usage | High | Low |
| Programming Model | Simple, synchronous | Complex, event-driven |
| Latency | Variable (context switching) | Predictable |
| CPU Utilization | Low (blocked threads) | High (no blocking) |

### Real-world Frameworks

- **Node.js** - JavaScript event loop
- **Netty** - Java NIO framework
- **Vert.x** - Reactive applications
- **Reactor** - Spring reactive framework
- **Project Loom** - Virtual threads (Java 19+)

---

## Callback and Promise Pattern

### Purpose

Handle asynchronous operations with callbacks (functions passed to async operations) and promises (containers for future values).

### Callback Pattern

**Types of Callbacks:**

```java
// 1. Success callback
interface SuccessCallback<T> {
    void onSuccess(T result);
}

// 2. Error callback
interface ErrorCallback {
    void onError(Throwable error);
}

// 3. Combined callback
interface AsyncCallback<T> {
    void onSuccess(T result);
    void onError(Throwable error);
}

// 4. Progress callback
interface ProgressCallback {
    void onProgress(int progress);
}
```

**Basic Usage:**
```java
// Simple callback
public void fetchData(String url, AsyncCallback<String> callback) {
    executor.submit(() -> {
        try {
            String data = httpClient.get(url);
            callback.onSuccess(data);
        } catch (Exception e) {
            callback.onError(e);
        }
    });
}

// Usage
fetchData("https://api.example.com", new AsyncCallback<String>() {
    @Override
    public void onSuccess(String data) {
        System.out.println("Data: " + data);
    }
    
    @Override
    public void onError(Throwable error) {
        System.err.println("Error: " + error.getMessage());
    }
});

// With lambdas
fetchData("https://api.example.com",
    data -> System.out.println("Data: " + data),
    error -> System.err.println("Error: " + error.getMessage())
);
```

**Callback Hell:**
```java
// Nested callbacks (avoid this!)
fetchUser(userId, new Callback<User>() {
    @Override
    public void onSuccess(User user) {
        fetchOrders(user.getId(), new Callback<List<Order>>() {
            @Override
            public void onSuccess(List<Order> orders) {
                fetchPayments(orders.get(0).getId(), new Callback<Payment>() {
                    @Override
                    public void onSuccess(Payment payment) {
                        // Finally got the payment...
                    }
                });
            }
        });
    }
});
```

### Promise Pattern

**Promise States:**
- **Pending** - Initial state
- **Fulfilled** - Operation completed successfully
- **Rejected** - Operation failed

**Basic Promise:**
```java
public class Promise<T> {
    private enum State { PENDING, FULFILLED, REJECTED }
    
    private State state = State.PENDING;
    private T value;
    private Throwable error;
    
    public Promise<T> then(SuccessCallback<T> callback) {
        if (state == State.FULFILLED) {
            callback.onSuccess(value);
        }
        return this;
    }
    
    public Promise<T> catchError(ErrorCallback callback) {
        if (state == State.REJECTED) {
            callback.onError(error);
        }
        return this;
    }
    
    public void fulfill(T value) {
        this.value = value;
        this.state = State.FULFILLED;
        // Notify callbacks
    }
    
    public void reject(Throwable error) {
        this.error = error;
        this.state = State.REJECTED;
        // Notify error callbacks
    }
}
```

**Promise Usage:**
```java
// Create promise
Promise<User> userPromise = new Promise<>();

// Add handlers
userPromise
    .then(user -> System.out.println("User: " + user))
    .catchError(error -> System.err.println("Error: " + error.getMessage()))
    .finally_(() -> System.out.println("Cleanup"));

// Fulfill later
executor.submit(() -> {
    try {
        User user = fetchUser();
        userPromise.fulfill(user);
    } catch (Exception e) {
        userPromise.reject(e);
    }
});
```

**Promise Chaining:**
```java
// Chain promises to avoid callback hell
userService.findUserById("user-123")
    .then(user -> {
        System.out.println("User: " + user);
    })
    .thenCompose(user -> {
        // Chain another promise
        return orderService.findOrdersByUserId(user.getId());
    })
    .then(orders -> {
        System.out.println("Orders: " + orders);
    })
    .catchError(error -> {
        System.err.println("Error in chain: " + error.getMessage());
    });
```

**Promise Transformation:**
```java
// Transform promise result
Promise<User> userPromise = userService.findUser("123");

Promise<String> namePromise = userPromise.thenMap(user -> {
    return user.getName().toUpperCase();
});

namePromise.then(name -> {
    System.out.println("Uppercase name: " + name);
});
```

**Promise Utilities:**
```java
// Wait for all promises
public static <T> Promise<List<T>> all(List<Promise<T>> promises) {
    Promise<List<T>> resultPromise = new Promise<>();
    List<T> results = new ArrayList<>();
    AtomicInteger counter = new AtomicInteger(0);
    
    for (Promise<T> promise : promises) {
        promise.then(value -> {
            results.add(value);
            if (counter.incrementAndGet() == promises.size()) {
                resultPromise.fulfill(results);
            }
        }).catchError(resultPromise::reject);
    }
    
    return resultPromise;
}

// Race - first to complete
public static <T> Promise<T> race(List<Promise<T>> promises) {
    Promise<T> resultPromise = new Promise<>();
    
    for (Promise<T> promise : promises) {
        promise.then(resultPromise::fulfill);
        promise.catchError(resultPromise::reject);
    }
    
    return resultPromise;
}

// Usage
List<Promise<String>> promises = Arrays.asList(
    fetchData("url1"),
    fetchData("url2"),
    fetchData("url3")
);

Promise.all(promises).then(results -> {
    System.out.println("All results: " + results);
});

Promise.race(promises).then(firstResult -> {
    System.out.println("First result: " + firstResult);
});
```

### Callback vs Promise

| Feature | Callback | Promise |
|---------|----------|---------|
| Readability | Poor (nested) | Good (chained) |
| Error Handling | Each callback | Centralized catch |
| Composition | Difficult | Easy |
| Control Flow | Inverted | Normal |
| Debugging | Hard | Easier |

### Use Cases

✓ Async I/O operations  
✓ HTTP requests  
✓ Database queries  
✓ File operations  
✓ Event handling  
✓ Animation/UI updates

### Best Practices

✓ Always handle errors  
✓ Use promises over nested callbacks  
✓ Return promises for chaining  
✓ Use async/await for cleaner code (JavaScript/C#)  
✓ Avoid creating unnecessary promises  
✓ Use `Promise.all` for parallel operations  
✓ Set timeouts for long operations

---

## Comparison Matrix

### Feature Comparison

| Pattern | Blocking | Callbacks | Chaining | Composition | Error Handling | Spring Support |
|---------|----------|-----------|----------|-------------|----------------|----------------|
| Future | ✓ | ✗ | ✗ | ✗ | Basic | ✓ |
| Callable | ✓ | ✗ | ✗ | ✗ | Basic | ✓ |
| CompletableFuture | ✗ | ✓ | ✓ | ✓ | Advanced | ✓ |
| ListenableFuture | ✗ | ✓ | ✗ | Limited | Good | ✓ |
| DeferredResult | ✗ | ✓ | ✗ | ✗ | Good | ✓ |
| @Async | ✗ | Depends | Depends | Depends | Good | ✓ |
| Event Loop | ✗ | ✓ | ✓ | ✓ | Custom | ✗ |
| Callback | ✗ | ✓ | Limited | Limited | Manual | ✗ |
| Promise | ✗ | ✓ | ✓ | ✓ | Good | ✗ |

### Use Case Recommendation

| Scenario | Recommended Pattern | Alternative |
|----------|---------------------|-------------|
| Simple async task | @Async (void) | Future |
| Need result later | CompletableFuture | ListenableFuture |
| Spring MVC async | DeferredResult | Callable |
| Complex orchestration | CompletableFuture | Promise chain |
| Long polling | DeferredResult | Server-Sent Events |
| High concurrency | Event Loop + NIO | Virtual threads (Loom) |
| Real-time notifications | DeferredResult | WebSocket |
| Batch processing | @Async + CompletableFuture | Parallel streams |
| External API calls | CompletableFuture | @Async + ListenableFuture |
| File I/O | CompletableFuture | @Async |

### Performance Characteristics

| Pattern | Throughput | Latency | Resource Usage | Scalability |
|---------|------------|---------|----------------|-------------|
| Future | Medium | High (blocking) | High (threads) | Medium |
| CompletableFuture | High | Low | Medium | High |
| ListenableFuture | High | Low | Medium | High |
| DeferredResult | High | Low | Low | Very High |
| @Async | High | Low | Medium | High |
| Event Loop | Very High | Very Low | Very Low | Very High |

---

## Best Practices

### General Guidelines

#### 1. Choose the Right Pattern

```java
// ❌ Wrong - Using Future with blocking get()
Future<String> future = executor.submit(task);
String result = future.get(); // Blocks thread

// ✅ Right - Using CompletableFuture with callback
CompletableFuture.supplyAsync(task)
    .thenAccept(result -> process(result));
```

#### 2. Always Handle Errors

```java
// ❌ Wrong - Ignoring errors
CompletableFuture.supplyAsync(() -> fetchData())
    .thenAccept(data -> process(data));

// ✅ Right - Handling errors
CompletableFuture.supplyAsync(() -> fetchData())
    .thenAccept(data -> process(data))
    .exceptionally(ex -> {
        log.error("Error fetching data", ex);
        return null;
    });
```

#### 3. Set Timeouts

```java
// ❌ Wrong - No timeout
future.get();

// ✅ Right - With timeout
try {
    future.get(5, TimeUnit.SECONDS);
} catch (TimeoutException e) {
    future.cancel(true);
    // Handle timeout
}

// ✅ Best - CompletableFuture with timeout (Java 9+)
future.orTimeout(5, TimeUnit.SECONDS)
    .exceptionally(ex -> handleTimeout());
```

#### 4. Use Appropriate Thread Pools

```java
// ❌ Wrong - Using common pool for blocking I/O
CompletableFuture.supplyAsync(() -> {
    // Blocking I/O - bad for common pool!
    return blockingDatabaseCall();
});

// ✅ Right - Custom executor for blocking operations
ExecutorService ioExecutor = Executors.newFixedThreadPool(20);
CompletableFuture.supplyAsync(() -> {
    return blockingDatabaseCall();
}, ioExecutor);
```

#### 5. Avoid Callback Hell

```java
// ❌ Wrong - Nested callbacks
fetchUser(userId, user -> {
    fetchOrders(user.getId(), orders -> {
        fetchPayments(orders.get(0).getId(), payment -> {
            // Deep nesting...
        });
    });
});

// ✅ Right - Chained promises/futures
fetchUser(userId)
    .thenCompose(user -> fetchOrders(user.getId()))
    .thenCompose(orders -> fetchPayments(orders.get(0).getId()))
    .thenAccept(payment -> processPayment(payment));
```

### Thread Pool Configuration

#### Sizing Formulas

```java
// CPU-bound tasks
int cpuPoolSize = Runtime.getRuntime().availableProcessors() + 1;

// I/O-bound tasks
int ioPoolSize = Runtime.getRuntime().availableProcessors() * 2;

// Mixed workload (Blocking Coefficient = time spent blocking)
// BC = 0.5 means 50% of time is spent waiting/blocking
int mixedPoolSize = (int) (cpuCount / (1 - blockingCoefficient));
```

#### Queue Configuration

```java
// ❌ Wrong - Unbounded queue (can cause OOM)
new ThreadPoolExecutor(5, 10, 60, TimeUnit.SECONDS,
    new LinkedBlockingQueue<>()); // No limit!

// ✅ Right - Bounded queue with rejection policy
new ThreadPoolExecutor(5, 10, 60, TimeUnit.SECONDS,
    new LinkedBlockingQueue<>(100),
    new ThreadPoolExecutor.CallerRunsPolicy());
```

#### Rejection Policies

| Policy | Behavior |
|--------|----------|
| AbortPolicy | Throws RejectedExecutionException (default) |
| CallerRunsPolicy | Runs task in calling thread |
| DiscardPolicy | Silently discards task |
| DiscardOldestPolicy | Discards oldest unhandled task |

### Async Method Best Practices

```java
// ❌ Wrong - Self-invocation
@Service
public class MyService {
    @Async
    public void asyncMethod() { }
    
    public void caller() {
        this.asyncMethod(); // NOT ASYNC! (self-invocation)
    }
}

// ✅ Right - Inject service
@Service
public class MyService {
    @Autowired
    private MyService self;
    
    @Async
    public void asyncMethod() { }
    
    public void caller() {
        self.asyncMethod(); // Async through proxy
    }
}
```

### Exception Handling

```java
// For void @Async methods
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) -> {
            log.error("Async method '{}' threw exception", method.getName(), ex);
            // Send alerts, metrics, etc.
        };
    }
}

// For CompletableFuture
CompletableFuture.supplyAsync(() -> riskyOperation())
    .exceptionally(ex -> {
        log.error("Operation failed", ex);
        return fallbackValue;
    })
    .thenAccept(result -> process(result));
```

### Monitoring and Debugging

```java
// Log async operations
@Async
public CompletableFuture<Result> processAsync(String id) {
    log.info("Starting async processing for: {}", id);
    long startTime = System.currentTimeMillis();
    
    return CompletableFuture.supplyAsync(() -> {
        try {
            Result result = process(id);
            log.info("Completed async processing for {} in {}ms", 
                id, System.currentTimeMillis() - startTime);
            return result;
        } catch (Exception e) {
            log.error("Failed async processing for: {}", id, e);
            throw e;
        }
    });
}

// Monitor thread pool
ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) asyncExecutor;
log.info("Active threads: {}", executor.getActiveCount());
log.info("Pool size: {}", executor.getPoolSize());
log.info("Queue size: {}", executor.getThreadPoolExecutor().getQueue().size());
```

---

## Production Checklist

### Configuration

- [ ] Thread pool sizes appropriate for workload
- [ ] Queue capacity set (not unbounded)
- [ ] Thread naming configured for debugging
- [ ] Rejection policy defined
- [ ] Graceful shutdown configured
- [ ] Multiple executors for different workloads
- [ ] Timeouts set for all async operations
- [ ] AsyncUncaughtExceptionHandler implemented

### Error Handling

- [ ] All async operations have error handlers
- [ ] Exceptions logged with context
- [ ] Fallback strategies defined
- [ ] Alerts configured for critical failures
- [ ] Dead letter queue for failed tasks
- [ ] Retry logic with exponential backoff
- [ ] Circuit breaker for external dependencies

### Monitoring

- [ ] Thread pool metrics exposed (active, queue size, rejected)
- [ ] Async operation duration tracked
- [ ] Success/failure rates monitored
- [ ] Timeout frequency tracked
- [ ] Resource utilization monitored (CPU, memory)
- [ ] Alerting configured for anomalies
- [ ] Distributed tracing for async flows

### Testing

- [ ] Unit tests with mocked async behavior
- [ ] Integration tests with real async execution
- [ ] Timeout scenarios tested
- [ ] Error handling tested
- [ ] Concurrent execution tested
- [ ] Thread pool exhaustion tested
- [ ] Performance/load tests conducted

### Documentation

- [ ] Async patterns documented
- [ ] Thread pool configurations documented
- [ ] Error handling strategy documented
- [ ] Timeout values documented
- [ ] Monitoring dashboards created
- [ ] Runbooks for common issues

### Code Quality

- [ ] No self-invocation of @Async methods
- [ ] No blocking operations in event loops
- [ ] Appropriate return types used
- [ ] Callbacks are non-blocking
- [ ] Resource cleanup in finally blocks
- [ ] Thread-safe data structures used
- [ ] No shared mutable state

### Performance

- [ ] Appropriate pattern chosen for use case
- [ ] Thread pool sized correctly
- [ ] Connection pools configured
- [ ] Database query optimization
- [ ] Caching strategy defined
- [ ] Batch processing where applicable
- [ ] Parallel streams for CPU-bound tasks

---

## Summary

### Key Takeaways

1. **Choose Wisely** - Select the right pattern for your use case
   - Simple tasks → @Async
   - Complex orchestration → CompletableFuture
   - Web async → DeferredResult
   - High concurrency → Event Loop + NIO

2. **Always Handle Errors** - Async operations can fail silently
   - Use exceptionally/handle/catchError
   - Implement AsyncUncaughtExceptionHandler
   - Log with context

3. **Configure Thread Pools** - One size doesn't fit all
   - Size based on workload (CPU vs I/O)
   - Set bounded queues
   - Use multiple pools for different tasks
   - Monitor and adjust

4. **Set Timeouts** - Prevent indefinite waiting
   - All external calls
   - Long-running operations
   - User-facing requests

5. **Monitor Everything** - You can't improve what you don't measure
   - Thread pool metrics
   - Operation duration
   - Success/failure rates
   - Resource utilization

### Evolution Path

```
Future → CompletableFuture → Reactive (Project Reactor)
                ↓
         Virtual Threads (Project Loom)
```

### Further Reading

- Java Concurrency in Practice - Brian Goetz
- Reactive Programming with RxJava - Tomasz Nurkiewicz
- Spring Framework Documentation - Async Support
- Project Reactor Documentation
- Netty in Action - Norman Maurer

---

## Appendix: Code Examples Summary

All patterns demonstrated with complete working examples:

1. **FutureAndCallablePattern.java** - Basic async with Future/Callable
2. **CompletableFuturePattern.java** - Advanced composition and chaining
3. **ListenableFuturePattern.java** - Spring async with callbacks
4. **DeferredResultPattern.java** - Spring MVC async requests
5. **AsyncMethodAndAnnotationPattern.java** - @Async configuration
6. **EventLoopAndNonBlockingIOPattern.java** - Event-driven architecture
7. **CallbackAndPromisePattern.java** - Callback and promise patterns

Each file contains:
- Pattern implementation
- Working examples
- Use case demonstrations
- Best practices
- Common pitfalls
- Performance considerations

---

**Version:** 1.0  
**Last Updated:** 2024  
**Author:** Spring Patterns Collection
