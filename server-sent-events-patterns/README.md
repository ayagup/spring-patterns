# Server-Sent Events (SSE) Patterns

This directory contains comprehensive implementations of Server-Sent Events (SSE) patterns in Spring Boot. These patterns enable real-time, server-to-client data streaming for various use cases.

## Overview

Server-Sent Events (SSE) is a standard that enables servers to push real-time updates to web clients over HTTP. These patterns demonstrate different approaches to implementing SSE in Spring Boot applications.

## Patterns

### 1. SSE Emitter Pattern
**File**: `SSEEmitterPattern.java`

**Purpose**: Standard SSE implementation for one-way server-to-client real-time updates.

**Key Features**:
- Unidirectional server-to-client communication
- Automatic reconnection from client side
- Event ID for last-event tracking
- Multiple named event types
- Long-lived HTTP connections
- Channel-based messaging

**Use Cases**:
- Live notifications
- Real-time dashboards
- Live feeds (news, sports, stock prices)
- Progress updates
- System monitoring

**Example**:
```java
@GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public SseEmitter streamEvents() {
    SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
    sseService.addEmitter("general", emitter);
    
    emitter.onCompletion(() -> sseService.removeEmitter("general", emitter));
    emitter.onTimeout(() -> sseService.removeEmitter("general", emitter));
    emitter.onError((ex) -> sseService.removeEmitter("general", emitter));
    
    return emitter;
}
```

---

### 2. Streaming Response Body Pattern
**File**: `StreamingResponseBodyPattern.java`

**Purpose**: Stream large amounts of data or continuous updates using StreamingResponseBody for direct output stream control.

**Key Features**:
- Direct access to output stream
- Efficient for large file transfers
- Asynchronous streaming
- Memory-efficient chunk processing
- Custom content type support
- Progress tracking capability

**Use Cases**:
- File downloads (large files)
- Video/audio streaming
- CSV/Excel export
- Log file streaming
- Database export
- Real-time data export

**Example**:
```java
@GetMapping("/csv")
public ResponseEntity<StreamingResponseBody> streamCSV(
        @RequestParam(defaultValue = "1000") int recordCount) {
    
    StreamingResponseBody stream = outputStream -> {
        String header = "ID,Name,Email,CreatedAt\n";
        outputStream.write(header.getBytes(StandardCharsets.UTF_8));
        
        for (int i = 0; i < recordCount; i++) {
            String record = generateCSVRecord(i + 1);
            outputStream.write(record.getBytes(StandardCharsets.UTF_8));
            
            if ((i + 1) % 100 == 0) {
                outputStream.flush();
            }
        }
    };

    return ResponseEntity.ok()
        .header("Content-Disposition", "attachment; filename=export.csv")
        .contentType(MediaType.parseMediaType("text/csv"))
        .body(stream);
}
```

---

### 3. Long Polling Pattern
**File**: `LongPollingPattern.java`

**Purpose**: Implement long polling for near real-time updates with reduced server load compared to short polling.

**Key Features**:
- Reduced network overhead vs short polling
- Near real-time updates
- DeferredResult for async response handling
- Timeout management
- Automatic retry mechanism
- Message queue integration

**Use Cases**:
- Chat applications
- Notification systems
- Real-time updates with legacy browser support
- Systems behind restrictive proxies
- Mobile applications

**Example**:
```java
@GetMapping("/messages")
public DeferredResult<ResponseEntity<Message>> pollMessages(
        @RequestParam(required = false) String lastMessageId,
        @RequestParam(defaultValue = "30000") long timeout) {
    
    DeferredResult<ResponseEntity<Message>> deferredResult = 
        new DeferredResult<>(timeout);

    deferredResult.onTimeout(() -> {
        deferredResult.setResult(ResponseEntity.status(HttpStatus.NO_CONTENT).build());
    });

    pollingService.registerMessagePoll(lastMessageId, deferredResult);

    return deferredResult;
}
```

---

### 4. Event Stream Pattern
**File**: `EventStreamPattern.java`

**Purpose**: Structured event streaming with multiple event types, event metadata, and event categorization.

**Key Features**:
- Multiple event types in single stream
- Event categorization and filtering
- Event metadata (ID, timestamp, retry)
- Event subscription management
- Event history and replay
- Event transformation

**Use Cases**:
- Multi-source event aggregation
- Complex event processing
- Event-driven architectures
- Real-time analytics dashboards
- Activity feeds
- Audit trails

**Example**:
```java
@GetMapping(path = "/stream/types", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public SseEmitter streamEventTypes(
        @RequestParam List<String> types,
        @RequestParam(required = false) String lastEventId) {
    
    SseEmitter emitter = new SseEmitter(300000L);
    
    eventStreamService.subscribe("filtered", types, emitter);
    
    emitter.onCompletion(() -> eventStreamService.unsubscribe(emitter));
    emitter.onTimeout(() -> eventStreamService.unsubscribe(emitter));
    emitter.onError((ex) -> eventStreamService.unsubscribe(emitter));
    
    return emitter;
}
```

---

### 5. Continuous Update Pattern
**File**: `ContinuousUpdatePattern.java`

**Purpose**: Provide continuous real-time updates from multiple data sources with subscription management, data aggregation, and update scheduling.

**Key Features**:
- Multiple update sources
- Configurable update frequency
- Data source subscription management
- Update buffering and batching
- Update priority handling
- Source health monitoring
- Rate limiting

**Use Cases**:
- Real-time dashboards
- Live monitoring systems
- IoT data streaming
- Financial data feeds
- Social media feeds
- News aggregation

**Example**:
```java
@GetMapping(path = "/dashboard", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public SseEmitter subscribeToDashboard(
        @RequestParam(defaultValue = "5000") long updateInterval) {
    
    SseEmitter emitter = new SseEmitter(0L);
    
    UpdateSubscription subscription = continuousUpdateService.subscribeToDashboard(
        updateInterval,
        emitter
    );
    
    emitter.onCompletion(() -> continuousUpdateService.unsubscribe(subscription.getId()));
    
    return emitter;
}
```

---

## Pattern Comparison Matrix

| Pattern | Protocol | Complexity | Bidirectional | Browser Support | Best For |
|---------|----------|------------|---------------|-----------------|----------|
| SSE Emitter | SSE/HTTP | Low | No | Modern browsers | Simple real-time updates |
| Streaming Response Body | HTTP | Medium | No | All browsers | Large file/data streaming |
| Long Polling | HTTP | Medium | Yes | All browsers | Legacy browser support |
| Event Stream | SSE/HTTP | High | No | Modern browsers | Complex event systems |
| Continuous Update | SSE/HTTP | High | No | Modern browsers | Multi-source dashboards |

## When to Use Each Pattern

### ✅ Use SSE Emitter Pattern When:
- Need simple one-way server-to-client updates
- Working with modern browsers
- Want automatic reconnection
- Need named event types
- Building notification systems

### ❌ Don't Use SSE Emitter Pattern When:
- Need bidirectional communication (use WebSocket)
- Supporting IE/Edge Legacy
- Behind proxies that don't support SSE
- Need guaranteed message delivery

---

### ✅ Use Streaming Response Body Pattern When:
- Streaming large files
- Exporting large datasets
- Need direct stream control
- Memory efficiency is critical
- Generating dynamic content (CSV, logs)

### ❌ Don't Use Streaming Response Body Pattern When:
- Need real-time bidirectional updates
- Small, frequent updates
- Need automatic reconnection
- Building notification systems

---

### ✅ Use Long Polling Pattern When:
- Supporting older browsers
- Behind restrictive firewalls/proxies
- Need request-response model
- Building chat applications
- Near real-time is sufficient

### ❌ Don't Use Long Polling Pattern When:
- Need very high-frequency updates
- Server resources are limited
- Modern browsers only
- Can use SSE or WebSocket

---

### ✅ Use Event Stream Pattern When:
- Multiple event types needed
- Event categorization required
- Need event history/replay
- Building event-driven architecture
- Complex event filtering needed

### ❌ Don't Use Event Stream Pattern When:
- Simple single-type updates
- No need for event metadata
- Minimal event filtering
- Simple notification system

---

### ✅ Use Continuous Update Pattern When:
- Aggregating multiple data sources
- Building real-time dashboards
- Configurable update intervals needed
- Source health monitoring required
- IoT/sensor data streaming

### ❌ Don't Use Continuous Update Pattern When:
- Single data source
- Event-driven updates (not time-based)
- Simple notification needs
- No need for data aggregation

---

## Configuration

### Maven Dependencies

```xml
<dependencies>
    <!-- Spring Boot Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <!-- Jackson for JSON -->
    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
    </dependency>
    
    <!-- Optional: For reactive support -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-webflux</artifactId>
    </dependency>
</dependencies>
```

### Application Properties

```properties
# Server configuration
server.port=8080

# Async request timeout (milliseconds)
spring.mvc.async.request-timeout=600000

# Thread pool for async operations
spring.task.execution.pool.core-size=10
spring.task.execution.pool.max-size=50
spring.task.execution.pool.queue-capacity=100

# Scheduling configuration
spring.task.scheduling.pool.size=5

# Max file size (for streaming)
spring.servlet.multipart.max-file-size=100MB
spring.servlet.multipart.max-request-size=100MB

# Connection timeout
server.connection-timeout=600000
```

### Basic SSE Configuration

```java
@Configuration
public class SSEConfig {
    
    @Bean
    public ExecutorService executorService() {
        return Executors.newCachedThreadPool();
    }
    
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                    .allowedOrigins("*")
                    .allowedMethods("GET", "POST", "PUT", "DELETE")
                    .allowedHeaders("*")
                    .allowCredentials(false);
            }
        };
    }
}
```

### Advanced Async Configuration

```java
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {
    
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("async-");
        executor.initialize();
        return executor;
    }
    
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new SimpleAsyncUncaughtExceptionHandler();
    }
}
```

---

## Client-Side Implementation

### JavaScript - EventSource (SSE)

```javascript
// Basic SSE connection
const eventSource = new EventSource('/api/sse/stream');

eventSource.onmessage = function(event) {
    const data = JSON.parse(event.data);
    console.log('Message:', data);
    updateUI(data);
};

eventSource.onerror = function(error) {
    console.error('SSE Error:', error);
    eventSource.close();
    
    // Reconnect after delay
    setTimeout(() => {
        connectSSE();
    }, 5000);
};

// Named event listener
eventSource.addEventListener('notification', function(event) {
    const notification = JSON.parse(event.data);
    showNotification(notification);
});

// Close connection
window.addEventListener('beforeunload', function() {
    eventSource.close();
});
```

### JavaScript - Fetch API (Streaming)

```javascript
// Stream text data
async function streamData() {
    const response = await fetch('/api/stream/text');
    const reader = response.body.getReader();
    const decoder = new TextDecoder();
    
    while (true) {
        const {done, value} = await reader.read();
        if (done) break;
        
        const chunk = decoder.decode(value, {stream: true});
        console.log('Received chunk:', chunk);
        processChunk(chunk);
    }
}

streamData();
```

### JavaScript - Long Polling

```javascript
function longPoll(url, lastMessageId) {
    const params = lastMessageId ? `?lastMessageId=${lastMessageId}` : '';
    
    fetch(url + params)
        .then(response => {
            if (response.status === 200) {
                return response.json();
            } else if (response.status === 204) {
                return null; // Timeout
            }
            throw new Error('Polling failed');
        })
        .then(data => {
            if (data) {
                handleMessage(data);
                longPoll(url, data.id); // Continue polling
            } else {
                longPoll(url, lastMessageId); // Retry
            }
        })
        .catch(error => {
            console.error('Error:', error);
            setTimeout(() => longPoll(url, lastMessageId), 5000);
        });
}

longPoll('/api/poll/messages');
```

---

## Best Practices

### 1. Connection Management

```java
// Always implement lifecycle handlers
emitter.onCompletion(() -> {
    cleanupResources(emitter);
    logger.info("Connection completed");
});

emitter.onTimeout(() -> {
    cleanupResources(emitter);
    logger.warn("Connection timeout");
});

emitter.onError((Throwable t) -> {
    cleanupResources(emitter);
    logger.error("Connection error: " + t.getMessage());
});
```

### 2. Error Handling

```java
@Service
public class SSEService {
    
    public void sendEvent(SseEmitter emitter, Object data) {
        try {
            SseEmitter.SseEventBuilder event = SseEmitter.event()
                .id(UUID.randomUUID().toString())
                .name("update")
                .data(data)
                .reconnectTime(3000L); // Reconnect after 3 seconds
            
            emitter.send(event);
        } catch (IOException e) {
            logger.error("Error sending event", e);
            emitter.completeWithError(e);
            removeEmitter(emitter);
        }
    }
}
```

### 3. Memory Management

```java
// Limit number of active connections
private static final int MAX_CONNECTIONS = 1000;
private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

public void addEmitter(String id, SseEmitter emitter) {
    if (emitters.size() >= MAX_CONNECTIONS) {
        emitter.completeWithError(new RuntimeException("Max connections reached"));
        return;
    }
    emitters.put(id, emitter);
}

// Periodic cleanup
@Scheduled(fixedRate = 60000)
public void cleanupDeadConnections() {
    emitters.entrySet().removeIf(entry -> {
        // Check if connection is still alive
        // Remove dead connections
        return isConnectionDead(entry.getValue());
    });
}
```

### 4. Event ID and Retry

```java
// Always include event IDs for client-side reconnection
SseEmitter.SseEventBuilder event = SseEmitter.event()
    .id(generateEventId())
    .name("update")
    .data(data)
    .reconnectTime(5000L); // Client will retry after 5 seconds

// Generate sequential IDs
private AtomicLong eventCounter = new AtomicLong(0);

private String generateEventId() {
    return String.format("evt-%d-%d", 
        System.currentTimeMillis(), 
        eventCounter.incrementAndGet());
}
```

### 5. Timeout Configuration

```java
// Set appropriate timeouts based on use case
SseEmitter emitter = new SseEmitter(Long.MAX_VALUE); // No timeout
SseEmitter emitter = new SseEmitter(300000L);        // 5 minutes
SseEmitter emitter = new SseEmitter(0L);             // Use default

// Send periodic heartbeats
@Scheduled(fixedRate = 15000)
public void sendHeartbeat() {
    for (SseEmitter emitter : emitters.values()) {
        try {
            emitter.send(SseEmitter.event()
                .name("heartbeat")
                .data("ping"));
        } catch (IOException e) {
            removeEmitter(emitter);
        }
    }
}
```

### 6. Threading and Async

```java
// Use async for non-blocking operations
@Async
public void processAndBroadcast(Data data) {
    Data processed = processData(data);
    broadcastToAllClients(processed);
}

// Use scheduled executor for periodic tasks
@Bean
public ScheduledExecutorService scheduledExecutorService() {
    return Executors.newScheduledThreadPool(5);
}

public void schedulePeriodicUpdate(SseEmitter emitter, long interval) {
    scheduledExecutor.scheduleAtFixedRate(
        () -> sendUpdate(emitter),
        0,
        interval,
        TimeUnit.MILLISECONDS
    );
}
```

### 7. CORS Configuration

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins("http://localhost:3000", "https://example.com")
            .allowedMethods("GET", "POST", "PUT", "DELETE")
            .allowedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600);
    }
}
```

---

## Common Use Cases

### 1. Real-Time Notification System

**Patterns**: SSE Emitter + Event Stream

```java
// Server
@GetMapping(path = "/notifications", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public SseEmitter notifications(@RequestParam String userId) {
    SseEmitter emitter = new SseEmitter(0L);
    notificationService.subscribe(userId, emitter);
    return emitter;
}

// Client
const notificationSource = new EventSource('/api/notifications?userId=123');
notificationSource.addEventListener('notification', (event) => {
    const notification = JSON.parse(event.data);
    showToast(notification.title, notification.message);
});
```

### 2. Live Dashboard

**Patterns**: Continuous Update + SSE Emitter

```java
// Server
@GetMapping(path = "/dashboard", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public SseEmitter dashboard(@RequestParam long updateInterval) {
    SseEmitter emitter = new SseEmitter(0L);
    dashboardService.subscribe(updateInterval, emitter);
    return emitter;
}

// Client
const dashboard = new EventSource('/api/dashboard?updateInterval=5000');
dashboard.addEventListener('update', (event) => {
    const data = JSON.parse(event.data);
    updateCharts(data);
    updateMetrics(data);
});
```

### 3. File Export/Download

**Patterns**: Streaming Response Body

```java
// Server
@GetMapping("/export/csv")
public ResponseEntity<StreamingResponseBody> exportCSV() {
    StreamingResponseBody stream = outputStream -> {
        exportService.exportToCSV(outputStream);
    };
    
    return ResponseEntity.ok()
        .header("Content-Disposition", "attachment; filename=export.csv")
        .contentType(MediaType.parseMediaType("text/csv"))
        .body(stream);
}

// Client
function downloadExport() {
    fetch('/api/export/csv')
        .then(response => response.blob())
        .then(blob => {
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = 'export.csv';
            a.click();
        });
}
```

### 4. Chat Application

**Patterns**: Long Polling + SSE Emitter

```java
// Server - Long Polling for sending
@PostMapping("/chat/send")
public ResponseEntity<Message> sendMessage(@RequestBody MessageRequest request) {
    Message message = chatService.sendMessage(request);
    return ResponseEntity.ok(message);
}

// Server - SSE for receiving
@GetMapping(path = "/chat/messages", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public SseEmitter receiveMessages(@RequestParam String roomId) {
    SseEmitter emitter = new SseEmitter(0L);
    chatService.subscribe(roomId, emitter);
    return emitter;
}

// Client
const chatSource = new EventSource('/api/chat/messages?roomId=room1');
chatSource.addEventListener('message', (event) => {
    const message = JSON.parse(event.data);
    displayMessage(message);
});

function sendMessage(text) {
    fetch('/api/chat/send', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({content: text, roomId: 'room1'})
    });
}
```

### 5. Stock Price Monitoring

**Patterns**: Event Stream + Continuous Update

```java
// Server
@GetMapping(path = "/stocks/{symbol}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public SseEmitter monitorStock(@PathVariable String symbol) {
    SseEmitter emitter = new SseEmitter(600000L);
    stockService.subscribe(symbol, emitter);
    return emitter;
}

// Client
const stockSource = new EventSource('/api/stocks/AAPL');
stockSource.addEventListener('stock-update', (event) => {
    const stock = JSON.parse(event.data);
    updateStockPrice(stock.symbol, stock.price, stock.change);
});
```

---

## Performance Optimization

### 1. Connection Pooling

```java
@Configuration
public class ConnectionPoolConfig {
    
    @Bean
    public ExecutorService connectionExecutor() {
        return new ThreadPoolExecutor(
            10,  // core pool size
            100, // max pool size
            60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(1000),
            new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }
}
```

### 2. Message Batching

```java
@Service
public class BatchingService {
    
    private final BlockingQueue<Message> messageQueue = new LinkedBlockingQueue<>();
    
    @Scheduled(fixedRate = 1000)
    public void sendBatch() {
        List<Message> batch = new ArrayList<>();
        messageQueue.drainTo(batch, 100); // Max 100 messages per batch
        
        if (!batch.isEmpty()) {
            broadcastBatch(batch);
        }
    }
    
    public void queueMessage(Message message) {
        messageQueue.offer(message);
    }
}
```

### 3. Compression

```java
@Configuration
public class CompressionConfig {
    
    @Bean
    public FilterRegistrationBean<GzipCompressingFilter> gzipFilter() {
        FilterRegistrationBean<GzipCompressingFilter> registration = 
            new FilterRegistrationBean<>();
        registration.setFilter(new GzipCompressingFilter());
        registration.addUrlPatterns("/api/*");
        return registration;
    }
}
```

### 4. Caching

```java
@Service
public class CachingSSEService {
    
    @Cacheable(value = "events", key = "#eventId")
    public Event getEvent(String eventId) {
        return eventRepository.findById(eventId);
    }
    
    @CacheEvict(value = "events", allEntries = true)
    @Scheduled(fixedRate = 300000) // 5 minutes
    public void clearCache() {
        logger.info("Event cache cleared");
    }
}
```

---

## Security Considerations

### 1. Authentication

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
                .antMatchers("/api/public/**").permitAll()
                .antMatchers("/api/sse/**").authenticated()
                .and()
            .httpBasic();
    }
}

// Controller with authentication
@GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public SseEmitter stream(Authentication authentication) {
    String username = authentication.getName();
    SseEmitter emitter = new SseEmitter(0L);
    sseService.subscribe(username, emitter);
    return emitter;
}
```

### 2. Rate Limiting

```java
@Service
public class RateLimitService {
    
    private final Map<String, RateLimiter> limiters = new ConcurrentHashMap<>();
    
    public boolean allowRequest(String userId) {
        RateLimiter limiter = limiters.computeIfAbsent(userId, 
            k -> RateLimiter.create(10.0)); // 10 requests per second
        
        return limiter.tryAcquire();
    }
}

@GetMapping("/stream")
public SseEmitter stream(@RequestParam String userId) {
    if (!rateLimitService.allowRequest(userId)) {
        throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS);
    }
    // ... create emitter
}
```

### 3. Input Validation

```java
@PostMapping("/events/publish")
public ResponseEntity<Event> publishEvent(@Valid @RequestBody EventData eventData) {
    Event event = eventService.publishEvent(eventData);
    return ResponseEntity.ok(event);
}

public class EventData {
    @NotBlank
    @Size(max = 100)
    private String type;
    
    @NotBlank
    @Size(max = 50)
    private String category;
    
    @Valid
    private Map<String, Object> data;
}
```

---

## Troubleshooting

### Problem 1: Connections Timing Out

**Symptoms**: SSE connections close after a few minutes

**Solutions**:
```java
// 1. Set appropriate timeout
SseEmitter emitter = new SseEmitter(0L); // No timeout

// 2. Send periodic heartbeats
@Scheduled(fixedRate = 15000)
public void sendHeartbeat() {
    broadcastToAll(SseEmitter.event().name("heartbeat").data("ping"));
}

// 3. Configure server timeout
spring.mvc.async.request-timeout=0
```

### Problem 2: Memory Leaks

**Symptoms**: Memory usage increases over time

**Solutions**:
```java
// 1. Implement proper cleanup
emitter.onCompletion(() -> removeEmitter(emitter));
emitter.onTimeout(() -> removeEmitter(emitter));
emitter.onError((ex) -> removeEmitter(emitter));

// 2. Periodic cleanup
@Scheduled(fixedRate = 60000)
public void cleanupDeadConnections() {
    emitters.values().removeIf(this::isConnectionDead);
}

// 3. Limit max connections
private static final int MAX_CONNECTIONS = 1000;
```

### Problem 3: Events Not Received

**Symptoms**: Client doesn't receive events

**Solutions**:
```javascript
// 1. Check event listeners
eventSource.addEventListener('your-event-name', handler);

// 2. Check CORS configuration
registry.addMapping("/api/**")
    .allowedOrigins("http://localhost:3000")
    .allowedHeaders("*");

// 3. Verify event structure
SseEmitter.SseEventBuilder event = SseEmitter.event()
    .id("123")
    .name("update")  // Must match client listener
    .data(data);
```

### Problem 4: High Server Load

**Symptoms**: Server CPU/memory high with many connections

**Solutions**:
```java
// 1. Implement batching
@Scheduled(fixedRate = 1000)
public void sendBatchUpdates() {
    List<Update> batch = collectUpdates();
    broadcastBatch(batch);
}

// 2. Use connection pooling
@Bean
public ExecutorService executorService() {
    return Executors.newFixedThreadPool(20);
}

// 3. Implement rate limiting
private final RateLimiter rateLimiter = RateLimiter.create(100.0);
```

### Problem 5: Proxy/Firewall Issues

**Symptoms**: SSE doesn't work behind proxy

**Solutions**:
```java
// 1. Use Long Polling as fallback
if (!isSSESupported()) {
    return useLongPolling();
}

// 2. Configure proxy timeout
# Nginx configuration
proxy_read_timeout 600s;
proxy_buffering off;

// 3. Send periodic data
@Scheduled(fixedRate = 30000)
public void sendKeepAlive() {
    emitter.send(SseEmitter.event().comment("keep-alive"));
}
```

---

## Testing

### Unit Testing

```java
@Test
public void testSSEEmitter() throws Exception {
    SseEmitter emitter = new SseEmitter();
    
    CompletableFuture<Object> future = new CompletableFuture<>();
    
    emitter.onCompletion(() -> future.complete(null));
    
    SseEmitter.SseEventBuilder event = SseEmitter.event()
        .name("test")
        .data("test-data");
    
    emitter.send(event);
    emitter.complete();
    
    future.get(5, TimeUnit.SECONDS);
}
```

### Integration Testing

```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class SSEIntegrationTest {
    
    @LocalServerPort
    private int port;
    
    @Test
    public void testSSEStream() {
        WebClient client = WebClient.create("http://localhost:" + port);
        
        Flux<ServerSentEvent<String>> eventStream = client.get()
            .uri("/api/sse/stream")
            .retrieve()
            .bodyToFlux(new ParameterizedTypeReference<ServerSentEvent<String>>() {});
        
        StepVerifier.create(eventStream.take(5))
            .expectNextCount(5)
            .verifyComplete();
    }
}
```

---

## Summary

These Server-Sent Events patterns provide comprehensive solutions for real-time server-to-client communication in Spring Boot applications. Choose the appropriate pattern based on your specific requirements:

- **SSE Emitter**: Simple, modern browser real-time updates
- **Streaming Response Body**: Large file/data streaming with memory efficiency
- **Long Polling**: Legacy browser support, near real-time updates
- **Event Stream**: Complex event systems with filtering and history
- **Continuous Update**: Multi-source dashboards with scheduled updates

All patterns are production-ready and include proper error handling, lifecycle management, and best practices.

---

## Additional Resources

- [Spring MVC Async Documentation](https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#mvc-ann-async)
- [Server-Sent Events Specification](https://html.spec.whatwg.org/multipage/server-sent-events.html)
- [MDN Web Docs - EventSource](https://developer.mozilla.org/en-US/docs/Web/API/EventSource)
- [Spring Boot Reference Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Reactive Streams](https://www.reactive-streams.org/)
