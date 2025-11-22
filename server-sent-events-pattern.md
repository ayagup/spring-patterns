### [Continue](https://continue.dev) session transcript
 Exported: 11/5/2025, 2:02:41 PM

#### _User_

> generate a java program in spring for each of the below patterns:
> ```
> **Server-Sent Events (SSE) Patterns**
> - SSE Emitter Pattern
> - Streaming Response Body Pattern
> - Long Polling Pattern
> - Event Stream Pattern
> - Continuous Update Pattern
> ```

#### _Assistant_

> I'll create a comprehensive Spring Boot application demonstrating all five SSE patterns.
> 
> ## 1. Project Setup
> 
> ```xml pom.xml
> <?xml version="1.0" encoding="UTF-8"?>
> <project xmlns="http://maven.apache.org/POM/4.0.0"
>          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
>          xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
>          http://www.apache.org/maven/4.0.0/xsd/maven-4.0.0.xsd">
>     <modelVersion>4.0.0</modelVersion>
>     
>     <parent>
>         <groupId>org.springframework.boot</groupId>
>         <artifactId>spring-boot-starter-parent</artifactId>
>         <version>3.2.0</version>
>         <relativePath/>
>     </parent>
>     
>     <groupId>com.example</groupId>
>     <artifactId>sse-patterns</artifactId>
>     <version>1.0.0</version>
>     
>     <properties>
>         <java.version>17</java.version>
>     </properties>
>     
>     <dependencies>
>         <dependency>
>             <groupId>org.springframework.boot</groupId>
>             <artifactId>spring-boot-starter-web</artifactId>
>         </dependency>
>         <dependency>
>             <groupId>org.projectlombok</groupId>
>             <artifactId>lombok</artifactId>
>             <optional>true</optional>
>         </dependency>
>     </dependencies>
>     
>     <build>
>         <plugins>
>             <plugin>
>                 <groupId>org.springframework.boot</groupId>
>                 <artifactId>spring-boot-maven-plugin</artifactId>
>             </plugin>
>         </plugins>
>     </build>
> </project>
> ```
> 
> ## 2. Main Application
> 
> ```java src/main/java/com/example/sse/SseApplication.java
> package com.example.sse;
> 
> import org.springframework.boot.SpringApplication;
> import org.springframework.boot.autoconfigure.SpringBootApplication;
> import org.springframework.scheduling.annotation.EnableScheduling;
> 
> @SpringBootApplication
> @EnableScheduling
> public class SseApplication {
>     public static void main(String[] args) {
>         SpringApplication.run(SseApplication.class, args);
>     }
> }
> ```
> 
> ## 3. Data Models
> 
> ```java src/main/java/com/example/sse/model/StockPrice.java
> package com.example.sse.model;
> 
> import lombok.AllArgsConstructor;
> import lombok.Data;
> import lombok.NoArgsConstructor;
> 
> import java.time.LocalDateTime;
> 
> @Data
> @AllArgsConstructor
> @NoArgsConstructor
> public class StockPrice {
>     private String symbol;
>     private double price;
>     private LocalDateTime timestamp;
>     private double changePercent;
> }
> ```
> 
> ```java src/main/java/com/example/sse/model/ServerEvent.java
> package com.example.sse.model;
> 
> import lombok.AllArgsConstructor;
> import lombok.Data;
> import lombok.NoArgsConstructor;
> 
> @Data
> @AllArgsConstructor
> @NoArgsConstructor
> public class ServerEvent {
>     private String eventType;
>     private String message;
>     private Object data;
>     private long timestamp;
> }
> ```
> 
> ## 4. Pattern 1: SSE Emitter Pattern
> 
> ```java src/main/java/com/example/sse/controller/SseEmitterController.java
> package com.example.sse.controller;
> 
> import com.example.sse.model.StockPrice;
> import lombok.extern.slf4j.Slf4j;
> import org.springframework.http.MediaType;
> import org.springframework.web.bind.annotation.GetMapping;
> import org.springframework.web.bind.annotation.PathVariable;
> import org.springframework.web.bind.annotation.RequestMapping;
> import org.springframework.web.bind.annotation.RestController;
> import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
> 
> import java.io.IOException;
> import java.time.LocalDateTime;
> import java.util.Map;
> import java.util.concurrent.ConcurrentHashMap;
> import java.util.concurrent.ExecutorService;
> import java.util.concurrent.Executors;
> 
> /**
>  * SSE Emitter Pattern
>  * Uses SseEmitter to send server events to connected clients.
>  * Ideal for real-time updates like stock prices, notifications, etc.
>  */
> @Slf4j
> @RestController
> @RequestMapping("/api/sse-emitter")
> public class SseEmitterController {
>     
>     private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
>     private final ExecutorService executor = Executors.newCachedThreadPool();
>     
>     @GetMapping(value = "/stock/{symbol}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
>     public SseEmitter streamStockPrice(@PathVariable String symbol) {
>         SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
>         String clientId = symbol + "-" + System.currentTimeMillis();
>         
>         emitters.put(clientId, emitter);
>         
>         emitter.onCompletion(() -> {
>             log.info("SSE completed for client: {}", clientId);
>             emitters.remove(clientId);
>         });
>         
>         emitter.onTimeout(() -> {
>             log.warn("SSE timeout for client: {}", clientId);
>             emitters.remove(clientId);
>         });
>         
>         emitter.onError(e -> {
>             log.error("SSE error for client: {}", clientId, e);
>             emitters.remove(clientId);
>         });
>         
>         // Start sending events
>         executor.execute(() -> sendStockUpdates(emitter, symbol, clientId));
>         
>         return emitter;
>     }
>     
>     private void sendStockUpdates(SseEmitter emitter, String symbol, String clientId) {
>         try {
>             double basePrice = 100.0;
>             for (int i = 0; i < 50; i++) {
>                 if (!emitters.containsKey(clientId)) {
>                     break;
>                 }
>                 
>                 double price = basePrice + (Math.random() * 10 - 5);
>                 double change = ((price - basePrice) / basePrice) * 100;
>                 
>                 StockPrice stockPrice = new StockPrice(
>                     symbol,
>                     price,
>                     LocalDateTime.now(),
>                     change
>                 );
>                 
>                 emitter.send(SseEmitter.event()
>                     .name("stock-update")
>                     .data(stockPrice)
>                     .id(String.valueOf(i))
>                 );
>                 
>                 Thread.sleep(1000);
>             }
>             
>             emitter.complete();
>         } catch (IOException | InterruptedException e) {
>             log.error("Error sending stock updates", e);
>             emitter.completeWithError(e);
>         }
>     }
> }
> ```
> 
> ## 5. Pattern 2: Streaming Response Body Pattern
> 
> ```java src/main/java/com/example/sse/controller/StreamingResponseController.java
> package com.example.sse.controller;
> 
> import com.example.sse.model.ServerEvent;
> import com.fasterxml.jackson.databind.ObjectMapper;
> import lombok.RequiredArgsConstructor;
> import lombok.extern.slf4j.Slf4j;
> import org.springframework.http.MediaType;
> import org.springframework.web.bind.annotation.GetMapping;
> import org.springframework.web.bind.annotation.RequestMapping;
> import org.springframework.web.bind.annotation.RestController;
> import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
> 
> import java.io.IOException;
> import java.nio.charset.StandardCharsets;
> 
> /**
>  * Streaming Response Body Pattern
>  * Uses StreamingResponseBody for fine-grained control over the response stream.
>  * Useful for large data transfers, file streaming, or custom streaming protocols.
>  */
> @Slf4j
> @RestController
> @RequestMapping("/api/streaming")
> @RequiredArgsConstructor
> public class StreamingResponseController {
>     
>     private final ObjectMapper objectMapper;
>     
>     @GetMapping(value = "/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
>     public StreamingResponseBody streamEvents() {
>         return outputStream -> {
>             try {
>                 for (int i = 0; i < 30; i++) {
>                     ServerEvent event = new ServerEvent(
>                         "info",
>                         "Event number " + i,
>                         Map.of("index", i, "timestamp", System.currentTimeMillis()),
>                         System.currentTimeMillis()
>                     );
>                     
>                     // Format as SSE
>                     String sseData = formatSseEvent("server-event", event, String.valueOf(i));
>                     outputStream.write(sseData.getBytes(StandardCharsets.UTF_8));
>                     outputStream.flush();
>                     
>                     Thread.sleep(500);
>                 }
>                 
>                 log.info("Streaming completed");
>             } catch (InterruptedException e) {
>                 Thread.currentThread().interrupt();
>                 log.error("Streaming interrupted", e);
>             } catch (IOException e) {
>                 log.error("Streaming error", e);
>             }
>         };
>     }
>     
>     @GetMapping(value = "/large-data", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
>     public StreamingResponseBody streamLargeData() {
>         return outputStream -> {
>             try {
>                 // Simulate streaming large dataset in chunks
>                 int totalChunks = 100;
>                 int chunkSize = 1024; // 1KB chunks
>                 
>                 for (int i = 0; i < totalChunks; i++) {
>                     byte[] chunk = generateDataChunk(i, chunkSize);
>                     outputStream.write(chunk);
>                     outputStream.flush();
>                     
>                     Thread.sleep(50);
>                 }
>                 
>                 log.info("Large data streaming completed");
>             } catch (InterruptedException e) {
>                 Thread.currentThread().interrupt();
>                 log.error("Large data streaming interrupted", e);
>             } catch (IOException e) {
>                 log.error("Large data streaming error", e);
>             }
>         };
>     }
>     
>     private String formatSseEvent(String eventName, Object data, String id) throws IOException {
>         String jsonData = objectMapper.writeValueAsString(data);
>         return String.format("event: %s\nid: %s\ndata: %s\n\n", eventName, id, jsonData);
>     }
>     
>     private byte[] generateDataChunk(int index, int size) {
>         String data = String.format("Chunk %d: ", index) + "x".repeat(size - 20);
>         return data.getBytes(StandardCharsets.UTF_8);
>     }
> }
> ```
> 
> ## 6. Pattern 3: Long Polling Pattern
> 
> ```java src/main/java/com/example/sse/controller/LongPollingController.java
> package com.example.sse.controller;
> 
> import com.example.sse.model.ServerEvent;
> import lombok.extern.slf4j.Slf4j;
> import org.springframework.http.ResponseEntity;
> import org.springframework.web.bind.annotation.*;
> import org.springframework.web.context.request.async.DeferredResult;
> 
> import java.util.Map;
> import java.util.Queue;
> import java.util.concurrent.ConcurrentHashMap;
> import java.util.concurrent.ConcurrentLinkedQueue;
> import java.util.concurrent.Executors;
> import java.util.concurrent.ScheduledExecutorService;
> import java.util.concurrent.TimeUnit;
> 
> /**
>  * Long Polling Pattern
>  * Client requests are held open until data is available or timeout occurs.
>  * Ideal for scenarios where SSE is not supported or bidirectional communication is needed.
>  */
> @Slf4j
> @RestController
> @RequestMapping("/api/long-polling")
> public class LongPollingController {
>     
>     private final Map<String, Queue<ServerEvent>> eventQueues = new ConcurrentHashMap<>();
>     private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
>     
>     public LongPollingController() {
>         // Simulate events being generated
>         scheduler.scheduleAtFixedRate(this::generateEvents, 0, 2, TimeUnit.SECONDS);
>     }
>     
>     @GetMapping("/events/{clientId}")
>     public DeferredResult<ResponseEntity<ServerEvent>> pollEvents(@PathVariable String clientId) {
>         DeferredResult<ResponseEntity<ServerEvent>> deferredResult = 
>             new DeferredResult<>(30000L); // 30 second timeout
>         
>         deferredResult.onTimeout(() -> {
>             log.info("Long polling timeout for client: {}", clientId);
>             deferredResult.setResult(ResponseEntity.noContent().build());
>         });
>         
>         // Check if events are available
>         Queue<ServerEvent> queue = eventQueues.computeIfAbsent(clientId, 
>             k -> new ConcurrentLinkedQueue<>());
>         
>         ServerEvent event = queue.poll();
>         if (event != null) {
>             deferredResult.setResult(ResponseEntity.ok(event));
>         } else {
>             // Wait for events
>             scheduler.schedule(() -> {
>                 ServerEvent nextEvent = queue.poll();
>                 if (nextEvent != null) {
>                     deferredResult.setResult(ResponseEntity.ok(nextEvent));
>                 } else {
>                     deferredResult.setResult(ResponseEntity.noContent().build());
>                 }
>             }, 100, TimeUnit.MILLISECONDS);
>         }
>         
>         return deferredResult;
>     }
>     
>     @PostMapping("/register/{clientId}")
>     public ResponseEntity<String> registerClient(@PathVariable String clientId) {
>         eventQueues.putIfAbsent(clientId, new ConcurrentLinkedQueue<>());
>         log.info("Client registered: {}", clientId);
>         return ResponseEntity.ok("Client registered: " + clientId);
>     }
>     
>     @DeleteMapping("/unregister/{clientId}")
>     public ResponseEntity<String> unregisterClient(@PathVariable String clientId) {
>         eventQueues.remove(clientId);
>         log.info("Client unregistered: {}", clientId);
>         return ResponseEntity.ok("Client unregistered: " + clientId);
>     }
>     
>     private void generateEvents() {
>         ServerEvent event = new ServerEvent(
>             "notification",
>             "Scheduled event at " + System.currentTimeMillis(),
>             Map.of("random", Math.random()),
>             System.currentTimeMillis()
>         );
>         
>         eventQueues.values().forEach(queue -> {
>             if (queue.size() < 10) { // Limit queue size
>                 queue.offer(event);
>             }
>         });
>     }
> }
> ```
> 
> ## 7. Pattern 4: Event Stream Pattern
> 
> ```java src/main/java/com/example/sse/controller/EventStreamController.java
> package com.example.sse.controller;
> 
> import com.example.sse.model.ServerEvent;
> import com.example.sse.service.EventStreamService;
> import lombok.RequiredArgsConstructor;
> import lombok.extern.slf4j.Slf4j;
> import org.springframework.http.MediaType;
> import org.springframework.http.codec.ServerSentEvent;
> import org.springframework.web.bind.annotation.GetMapping;
> import org.springframework.web.bind.annotation.PathVariable;
> import org.springframework.web.bind.annotation.RequestMapping;
> import org.springframework.web.bind.annotation.RestController;
> import reactor.core.publisher.Flux;
> 
> import java.time.Duration;
> 
> /**
>  * Event Stream Pattern
>  * Uses reactive streams (Flux) for event streaming.
>  * Ideal for reactive applications and handling backpressure.
>  */
> @Slf4j
> @RestController
> @RequestMapping("/api/event-stream")
> @RequiredArgsConstructor
> public class EventStreamController {
>     
>     private final EventStreamService eventStreamService;
>     
>     @GetMapping(value = "/notifications", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
>     public Flux<ServerSentEvent<ServerEvent>> streamNotifications() {
>         return eventStreamService.getNotificationStream()
>             .map(event -> ServerSentEvent.<ServerEvent>builder()
>                 .id(String.valueOf(event.getTimestamp()))
>                 .event("notification")
>                 .data(event)
>                 .build());
>     }
>     
>     @GetMapping(value = "/metrics", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
>     public Flux<ServerSentEvent<Map<String, Object>>> streamMetrics() {
>         return Flux.interval(Duration.ofSeconds(1))
>             .map(i -> {
>                 Map<String, Object> metrics = Map.of(
>                     "cpu", Math.random() * 100,
>                     "memory", Math.random() * 100,
>                     "disk", Math.random() * 100,
>                     "timestamp", System.currentTimeMillis()
>                 );
>                 return ServerSentEvent.<Map<String, Object>>builder()
>                     .id(String.valueOf(i))
>                     .event("metrics")
>                     .data(metrics)
>                     .build();
>             });
>     }
>     
>     @GetMapping(value = "/channel/{channelName}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
>     public Flux<ServerSentEvent<ServerEvent>> streamChannel(@PathVariable String channelName) {
>         return eventStreamService.getChannelStream(channelName)
>             .map(event -> ServerSentEvent.<ServerEvent>builder()
>                 .id(String.valueOf(event.getTimestamp()))
>                 .event("channel-event")
>                 .data(event)
>                 .comment("Channel: " + channelName)
>                 .build());
>     }
> }
> ```
> 
> ```java src/main/java/com/example/sse/service/EventStreamService.java
> package com.example.sse.service;
> 
> import com.example.sse.model.ServerEvent;
> import lombok.extern.slf4j.Slf4j;
> import org.springframework.stereotype.Service;
> import reactor.core.publisher.Flux;
> import reactor.core.publisher.Sinks;
> 
> import java.time.Duration;
> import java.util.Map;
> import java.util.concurrent.ConcurrentHashMap;
> 
> @Slf4j
> @Service
> public class EventStreamService {
>     
>     private final Sinks.Many<ServerEvent> notificationSink;
>     private final Map<String, Sinks.Many<ServerEvent>> channelSinks;
>     
>     public EventStreamService() {
>         this.notificationSink = Sinks.many().multicast().onBackpressureBuffer();
>         this.channelSinks = new ConcurrentHashMap<>();
>         
>         // Start generating sample events
>         startEventGeneration();
>     }
>     
>     public Flux<ServerEvent> getNotificationStream() {
>         return notificationSink.asFlux();
>     }
>     
>     public Flux<ServerEvent> getChannelStream(String channelName) {
>         Sinks.Many<ServerEvent> sink = channelSinks.computeIfAbsent(
>             channelName,
>             k -> Sinks.many().multicast().onBackpressureBuffer()
>         );
>         return sink.asFlux();
>     }
>     
>     public void publishNotification(ServerEvent event) {
>         notificationSink.tryEmitNext(event);
>     }
>     
>     public void publishToChannel(String channelName, ServerEvent event) {
>         Sinks.Many<ServerEvent> sink = channelSinks.get(channelName);
>         if (sink != null) {
>             sink.tryEmitNext(event);
>         }
>     }
>     
>     private void startEventGeneration() {
>         // Generate notifications every 3 seconds
>         Flux.interval(Duration.ofSeconds(3))
>             .subscribe(i -> {
>                 ServerEvent event = new ServerEvent(
>                     "info",
>                     "Notification #" + i,
>                     Map.of("index", i),
>                     System.currentTimeMillis()
>                 );
>                 publishNotification(event);
>             });
>         
>         // Generate channel events
>         Flux.interval(Duration.ofSeconds(2))
>             .subscribe(i -> {
>                 String channel = "channel-" + (i % 3);
>                 ServerEvent event = new ServerEvent(
>                     "channel-message",
>                     "Message for " + channel,
>                     Map.of("index", i, "channel", channel),
>                     System.currentTimeMillis()
>                 );
>                 publishToChannel(channel, event);
>             });
>     }
> }
> ```
> 
> ## 8. Pattern 5: Continuous Update Pattern
> 
> ```java src/main/java/com/example/sse/controller/ContinuousUpdateController.java
> package com.example.sse.controller;
> 
> import com.example.sse.service.ContinuousUpdateService;
> import lombok.RequiredArgsConstructor;
> import lombok.extern.slf4j.Slf4j;
> import org.springframework.http.MediaType;
> import org.springframework.web.bind.annotation.*;
> import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
> 
> /**
>  * Continuous Update Pattern
>  * Maintains long-lived connections for continuous data updates.
>  * Ideal for dashboards, monitoring systems, and live feeds.
>  */
> @Slf4j
> @RestController
> @RequestMapping("/api/continuous")
> @RequiredArgsConstructor
> public class ContinuousUpdateController {
>     
>     private final ContinuousUpdateService updateService;
>     
>     @GetMapping(value = "/dashboard/{dashboardId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
>     public SseEmitter subscribeToDashboard(@PathVariable String dashboardId) {
>         log.info("New dashboard subscription: {}", dashboardId);
>         return updateService.subscribe(dashboardId);
>     }
>     
>     @PostMapping("/dashboard/{dashboardId}/update")
>     public void updateDashboard(
>             @PathVariable String dashboardId,
>             @RequestBody Map<String, Object> data) {
>         log.info("Dashboard update received for: {}", dashboardId);
>         updateService.broadcastUpdate(dashboardId, data);
>     }
>     
>     @GetMapping(value = "/live-feed/{feedType}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
>     public SseEmitter subscribeToLiveFeed(@PathVariable String feedType) {
>         log.info("New live feed subscription: {}", feedType);
>         return updateService.subscribeToFeed(feedType);
>     }
> }
> ```
> 
> ```java src/main/java/com/example/sse/service/ContinuousUpdateService.java
> package com.example.sse.service;
> 
> import com.example.sse.model.ServerEvent;
> import lombok.extern.slf4j.Slf4j;
> import org.springframework.scheduling.annotation.Scheduled;
> import org.springframework.stereotype.Service;
> import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
> 
> import java.io.IOException;
> import java.util.Map;
> import java.util.Set;
> import java.util.concurrent.ConcurrentHashMap;
> import java.util.concurrent.CopyOnWriteArraySet;
> 
> @Slf4j
> @Service
> public class ContinuousUpdateService {
>     
>     private final Map<String, Set<SseEmitter>> dashboardSubscribers = new ConcurrentHashMap<>();
>     private final Map<String, Set<SseEmitter>> feedSubscribers = new ConcurrentHashMap<>();
>     
>     public SseEmitter subscribe(String dashboardId) {
>         SseEmitter emitter = new SseEmitter(0L); // No timeout
>         
>         Set<SseEmitter> subscribers = dashboardSubscribers.computeIfAbsent(
>             dashboardId,
>             k -> new CopyOnWriteArraySet<>()
>         );
>         
>         subscribers.add(emitter);
>         
>         emitter.onCompletion(() -> {
>             log.info("Dashboard subscription completed: {}", dashboardId);
>             subscribers.remove(emitter);
>         });
>         
>         emitter.onTimeout(() -> {
>             log.warn("Dashboard subscription timeout: {}", dashboardId);
>             subscribers.remove(emitter);
>         });
>         
>         emitter.onError(e -> {
>             log.error("Dashboard subscription error: {}", dashboardId, e);
>             subscribers.remove(emitter);
>         });
>         
>         // Send initial connection event
>         try {
>             emitter.send(SseEmitter.event()
>                 .name("connected")
>                 .data(Map.of("dashboardId", dashboardId, "timestamp", System.currentTimeMillis()))
>             );
>         } catch (IOException e) {
>             log.error("Error sending initial event", e);
>             subscribers.remove(emitter);
>         }
>         
>         return emitter;
>     }
>     
>     public SseEmitter subscribeToFeed(String feedType) {
>         SseEmitter emitter = new SseEmitter(0L);
>         
>         Set<SseEmitter> subscribers = feedSubscribers.computeIfAbsent(
>             feedType,
>             k -> new CopyOnWriteArraySet<>()
>         );
>         
>         subscribers.add(emitter);
>         
>         emitter.onCompletion(() -> subscribers.remove(emitter));
>         emitter.onTimeout(() -> subscribers.remove(emitter));
>         emitter.onError(e -> subscribers.remove(emitter));
>         
>         return emitter;
>     }
>     
>     public void broadcastUpdate(String dashboardId, Map<String, Object> data) {
>         Set<SseEmitter> subscribers = dashboardSubscribers.get(dashboardId);
>         if (subscribers != null) {
>             ServerEvent event = new ServerEvent(
>                 "dashboard-update",
>                 "Dashboard data updated",
>                 data,
>                 System.currentTimeMillis()
>             );
>             
>             sendToSubscribers(subscribers, event, "update");
>         }
>     }
>     
>     @Scheduled(fixedRate = 5000) // Every 5 seconds
>     public void sendHeartbeat() {
>         ServerEvent heartbeat = new ServerEvent(
>             "heartbeat",
>             "Keep-alive",
>             Map.of("timestamp", System.currentTimeMillis()),
>             System.currentTimeMillis()
>         );
>         
>         dashboardSubscribers.values().forEach(subscribers -> 
>             sendToSubscribers(subscribers, heartbeat, "heartbeat")
>         );
>     }
>     
>     @Scheduled(fixedRate = 2000) // Every 2 seconds
>     public void generateFeedUpdates() {
>         feedSubscribers.forEach((feedType, subscribers) -> {
>             ServerEvent event = new ServerEvent(
>                 "feed-update",
>                 "New " + feedType + " data",
>                 Map.of(
>                     "feedType", feedType,
>                     "value", Math.random() * 100,
>                     "timestamp", System.currentTimeMillis()
>                 ),
>                 System.currentTimeMillis()
>             );
>             
>             sendToSubscribers(subscribers, event, "feed-data");
>         });
>     }
>     
>     private void sendToSubscribers(Set<SseEmitter> subscribers, ServerEvent event, String eventName) {
>         subscribers.removeIf(emitter -> {
>             try {
>                 emitter.send(SseEmitter.event()
>                     .name(eventName)
>                     .data(event)
>                     .id(String.valueOf(event.getTimestamp()))
>                 );
>                 return false;
>             } catch (IOException e) {
>                 log.error("Error sending event to subscriber", e);
>                 return true;
>             }
>         });
>     }
> }
> ```
> 
> ## 9. Configuration
> 
> ```java src/main/java/com/example/sse/config/WebConfig.java
> package com.example.sse.config;
> 
> import com.fasterxml.jackson.databind.ObjectMapper;
> import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
> import org.springframework.context.annotation.Bean;
> import org.springframework.context.annotation.Configuration;
> import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
> import org.springframework.web.servlet.config.annotation.CorsRegistry;
> import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
> 
> @Configuration
> public class WebConfig implements WebMvcConfigurer {
>     
>     @Override
>     public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
>         configurer.setDefaultTimeout(60000); // 60 seconds default timeout
>     }
>     
>     @Override
>     public void addCorsMappings(CorsRegistry registry) {
>         registry.addMapping("/api/**")
>             .allowedOrigins("*")
>             .allowedMethods("GET", "POST", "DELETE")
>             .allowedHeaders("*");
>     }
>     
>     @Bean
>     public ObjectMapper objectMapper() {
>         ObjectMapper mapper = new ObjectMapper();
>         mapper.registerModule(new JavaTimeModule());
>         return mapper;
>     }
> }
> ```
> 
> ## 10. Application Properties
> 
> ```properties src/main/resources/application.properties
> server.port=8080
> spring.application.name=sse-patterns
> 
> # Async configuration
> spring.mvc.async.request-timeout=60000
> 
> # Logging
> logging.level.com.example.sse=DEBUG
> logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} - %msg%n
> 
> # Jackson configuration
> spring.jackson.serialization.write-dates-as-timestamps=false
> ```
> 
> ## 11. Test HTML Client
> 
> ```html src/main/resources/static/index.html
> <!DOCTYPE html>
> <html>
> <head>
>     <title>SSE Patterns Demo</title>
>     <style>
>         body { font-family: Arial, sans-serif; margin: 20px; }
>         .pattern-section { 
>             border: 1px solid #ccc; 
>             padding: 15px; 
>             margin: 10px 0; 
>             border-radius: 5px;
>         }
>         .output { 
>             background: #f5f5f5; 
>             padding: 10px; 
>             height: 200px; 
>             overflow-y: scroll; 
>             font-family: monospace;
>             font-size: 12px;
>         }
>         button { 
>             margin: 5px; 
>             padding: 8px 15px; 
>             cursor: pointer;
>         }
>         h2 { color: #333; }
>         .event { 
>             margin: 5px 0; 
>             padding: 5px; 
>             border-left: 3px solid #4CAF50;
>         }
>     </style>
> </head>
> <body>
>     <h1>Server-Sent Events (SSE) Patterns Demo</h1>
>     
>     <!-- Pattern 1: SSE Emitter -->
>     <div class="pattern-section">
>         <h2>1. SSE Emitter Pattern (Stock Prices)</h2>
>         <button onclick="startSseEmitter()">Start</button>
>         <button onclick="stopSseEmitter()">Stop</button>
>         <div id="sse-emitter-output" class="output"></div>
>     </div>
>     
>     <!-- Pattern 2: Streaming Response -->
>     <div class="pattern-section">
>         <h2>2. Streaming Response Body Pattern</h2>
>         <button onclick="startStreaming()">Start Events Stream</button>
>         <button onclick="stopStreaming()">Stop</button>
>         <div id="streaming-output" class="output"></div>
>     </div>
>     
>     <!-- Pattern 3: Long Polling -->
>     <div class="pattern-section">
>         <h2>3. Long Polling Pattern</h2>
>         <button onclick="startLongPolling()">Start</button>
>         <button onclick="stopLongPolling()">Stop</button>
>         <div id="long-polling-output" class="output"></div>
>     </div>
>     
>     <!-- Pattern 4: Event Stream -->
>     <div class="pattern-section">
>         <h2>4. Event Stream Pattern (Reactive)</h2>
>         <button onclick="startEventStream()">Start Notifications</button>
>         <button onclick="stopEventStream()">Stop</button>
>         <div id="event-stream-output" class="output"></div>
>     </div>
>     
>     <!-- Pattern 5: Continuous Update -->
>     <div class="pattern-section">
>         <h2>5. Continuous Update Pattern (Dashboard)</h2>
>         <button onclick="startContinu

#### _User_

> Continue your response exactly where you left off:

#### _Assistant_

> ```html src/main/resources/static/index.html
>         <button onclick="startContinuousUpdate()">Start Dashboard</button>
>         <button onclick="stopContinuousUpdate()">Stop</button>
>         <div id="continuous-output" class="output"></div>
>     </div>
> 
>     <script>
>         let sseEmitterSource = null;
>         let streamingSource = null;
>         let longPollingInterval = null;
>         let longPollingActive = false;
>         let eventStreamSource = null;
>         let continuousSource = null;
>         const clientId = 'client-' + Date.now();
> 
>         // Pattern 1: SSE Emitter
>         function startSseEmitter() {
>             const output = document.getElementById('sse-emitter-output');
>             output.innerHTML = '<div class="event">Connecting to SSE Emitter...</div>';
>             
>             sseEmitterSource = new EventSource('/api/sse-emitter/stock/AAPL');
>             
>             sseEmitterSource.addEventListener('stock-update', (event) => {
>                 const data = JSON.parse(event.data);
>                 const msg = `Stock: ${data.symbol}, Price: $${data.price.toFixed(2)}, ` +
>                            `Change: ${data.changePercent.toFixed(2)}%, Time: ${data.timestamp}`;
>                 addMessage(output, msg);
>             });
>             
>             sseEmitterSource.onerror = (error) => {
>                 addMessage(output, 'Error or connection closed');
>                 console.error('SSE Emitter error:', error);
>             };
>         }
> 
>         function stopSseEmitter() {
>             if (sseEmitterSource) {
>                 sseEmitterSource.close();
>                 sseEmitterSource = null;
>                 addMessage(document.getElementById('sse-emitter-output'), 'Connection closed');
>             }
>         }
> 
>         // Pattern 2: Streaming Response
>         function startStreaming() {
>             const output = document.getElementById('streaming-output');
>             output.innerHTML = '<div class="event">Connecting to Streaming Response...</div>';
>             
>             streamingSource = new EventSource('/api/streaming/events');
>             
>             streamingSource.addEventListener('server-event', (event) => {
>                 const data = JSON.parse(event.data);
>                 const msg = `Event: ${data.eventType}, Message: ${data.message}, ` +
>                            `Data: ${JSON.stringify(data.data)}`;
>                 addMessage(output, msg);
>             });
>             
>             streamingSource.onerror = (error) => {
>                 addMessage(output, 'Stream ended or error occurred');
>                 console.error('Streaming error:', error);
>             };
>         }
> 
>         function stopStreaming() {
>             if (streamingSource) {
>                 streamingSource.close();
>                 streamingSource = null;
>                 addMessage(document.getElementById('streaming-output'), 'Stream closed');
>             }
>         }
> 
>         // Pattern 3: Long Polling
>         function startLongPolling() {
>             const output = document.getElementById('long-polling-output');
>             output.innerHTML = '<div class="event">Starting Long Polling...</div>';
>             
>             // Register client first
>             fetch(`/api/long-polling/register/${clientId}`, { method: 'POST' })
>                 .then(() => {
>                     longPollingActive = true;
>                     pollForEvents();
>                 });
>         }
> 
>         function pollForEvents() {
>             if (!longPollingActive) return;
>             
>             const output = document.getElementById('long-polling-output');
>             
>             fetch(`/api/long-polling/events/${clientId}`)
>                 .then(response => {
>                     if (response.status === 200) {
>                         return response.json();
>                     }
>                     return null;
>                 })
>                 .then(data => {
>                     if (data) {
>                         const msg = `Event: ${data.eventType}, Message: ${data.message}, ` +
>                                    `Data: ${JSON.stringify(data.data)}`;
>                         addMessage(output, msg);
>                     }
>                     // Immediately poll again
>                     if (longPollingActive) {
>                         setTimeout(pollForEvents, 100);
>                     }
>                 })
>                 .catch(error => {
>                     console.error('Long polling error:', error);
>                     if (longPollingActive) {
>                         setTimeout(pollForEvents, 1000);
>                     }
>                 });
>         }
> 
>         function stopLongPolling() {
>             longPollingActive = false;
>             fetch(`/api/long-polling/unregister/${clientId}`, { method: 'DELETE' })
>                 .then(() => {
>                     addMessage(document.getElementById('long-polling-output'), 'Long polling stopped');
>                 });
>         }
> 
>         // Pattern 4: Event Stream (Reactive)
>         function startEventStream() {
>             const output = document.getElementById('event-stream-output');
>             output.innerHTML = '<div class="event">Connecting to Event Stream...</div>';
>             
>             eventStreamSource = new EventSource('/api/event-stream/notifications');
>             
>             eventStreamSource.addEventListener('notification', (event) => {
>                 const data = JSON.parse(event.data);
>                 const msg = `Type: ${data.eventType}, Message: ${data.message}, ` +
>                            `Data: ${JSON.stringify(data.data)}, ID: ${event.lastEventId}`;
>                 addMessage(output, msg);
>             });
>             
>             eventStreamSource.onerror = (error) => {
>                 addMessage(output, 'Event stream error or closed');
>                 console.error('Event stream error:', error);
>             };
>         }
> 
>         function stopEventStream() {
>             if (eventStreamSource) {
>                 eventStreamSource.close();
>                 eventStreamSource = null;
>                 addMessage(document.getElementById('event-stream-output'), 'Event stream closed');
>             }
>         }
> 
>         // Pattern 5: Continuous Update
>         function startContinuousUpdate() {
>             const output = document.getElementById('continuous-output');
>             output.innerHTML = '<div class="event">Connecting to Dashboard...</div>';
>             
>             const dashboardId = 'dashboard-1';
>             continuousSource = new EventSource(`/api/continuous/dashboard/${dashboardId}`);
>             
>             continuousSource.addEventListener('connected', (event) => {
>                 const data = JSON.parse(event.data);
>                 addMessage(output, `Connected to dashboard: ${data.dashboardId}`);
>             });
>             
>             continuousSource.addEventListener('heartbeat', (event) => {
>                 const data = JSON.parse(event.data);
>                 addMessage(output, `Heartbeat received at ${new Date(data.data.timestamp).toLocaleTimeString()}`);
>             });
>             
>             continuousSource.addEventListener('update', (event) => {
>                 const data = JSON.parse(event.data);
>                 const msg = `Dashboard Update: ${data.message}, Data: ${JSON.stringify(data.data)}`;
>                 addMessage(output, msg);
>             });
>             
>             continuousSource.onerror = (error) => {
>                 addMessage(output, 'Dashboard connection error');
>                 console.error('Continuous update error:', error);
>             };
>         }
> 
>         function stopContinuousUpdate() {
>             if (continuousSource) {
>                 continuousSource.close();
>                 continuousSource = null;
>                 addMessage(document.getElementById('continuous-output'), 'Dashboard connection closed');
>             }
>         }
> 
>         // Helper function to add messages
>         function addMessage(outputElement, message) {
>             const time = new Date().toLocaleTimeString();
>             const eventDiv = document.createElement('div');
>             eventDiv.className = 'event';
>             eventDiv.textContent = `[${time}] ${message}`;
>             outputElement.appendChild(eventDiv);
>             outputElement.scrollTop = outputElement.scrollHeight;
>             
>             // Keep only last 50 messages
>             while (outputElement.children.length > 50) {
>                 outputElement.removeChild(outputElement.firstChild);
>             }
>         }
> 
>         // Cleanup on page unload
>         window.addEventListener('beforeunload', () => {
>             stopSseEmitter();
>             stopStreaming();
>             stopLongPolling();
>             stopEventStream();
>             stopContinuousUpdate();
>         });
>     </script>
> </body>
> </html>
> ```
> 
> ## 12. Additional Dependencies for Reactive Support
> 
> ```xml pom.xml
> <?xml version="1.0" encoding="UTF-8"?>
> <project xmlns="http://maven.apache.org/POM/4.0.0"
>          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
>          xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
>          http://www.apache.org/maven/4.0.0/xsd/maven-4.0.0.xsd">
>     <modelVersion>4.0.0</modelVersion>
>     
>     <parent>
>         <groupId>org.springframework.boot</groupId>
>         <artifactId>spring-boot-starter-parent</artifactId>
>         <version>3.2.0</version>
>         <relativePath/>
>     </parent>
>     
>     <groupId>com.example</groupId>
>     <artifactId>sse-patterns</artifactId>
>     <version>1.0.0</version>
>     
>     <properties>
>         <java.version>17</java.version>
>     </properties>
>     
>     <dependencies>
>         <dependency>
>             <groupId>org.springframework.boot</groupId>
>             <artifactId>spring-boot-starter-web</artifactId>
>         </dependency>
>         <dependency>
>             <groupId>org.springframework.boot</groupId>
>             <artifactId>spring-boot-starter-webflux</artifactId>
>         </dependency>
>         <dependency>
>             <groupId>org.projectlombok</groupId>
>             <artifactId>lombok</artifactId>
>             <optional>true</optional>
>         </dependency>
>         <dependency>
>             <groupId>org.springframework.boot</groupId>
>             <artifactId>spring-boot-starter-test</artifactId>
>             <scope>test</scope>
>         </dependency>
>     </dependencies>
>     
>     <build>
>         <plugins>
>             <plugin>
>                 <groupId>org.springframework.boot</groupId>
>                 <artifactId>spring-boot-maven-plugin</artifactId>
>             </plugin>
>         </plugins>
>     </build>
> </project>
> ```
> 
> ## 13. README with Usage Instructions
> 
> ```markdown README.md
> # Server-Sent Events (SSE) Patterns in Spring Boot
> 
> This project demonstrates five different SSE patterns using Spring Boot.
> 
> ## Patterns Implemented
> 
> ### 1. SSE Emitter Pattern
> - **Endpoint**: `GET /api/sse-emitter/stock/{symbol}`
> - **Use Case**: Real-time stock price updates
> - **Features**: Server-initiated events, automatic connection management
> 
> ### 2. Streaming Response Body Pattern
> - **Endpoint**: `GET /api/streaming/events`
> - **Use Case**: Large data transfers, custom streaming protocols
> - **Features**: Fine-grained control over response stream, custom formatting
> 
> ### 3. Long Polling Pattern
> - **Endpoints**: 
>   - `POST /api/long-polling/register/{clientId}`
>   - `GET /api/long-polling/events/{clientId}`
>   - `DELETE /api/long-polling/unregister/{clientId}`
> - **Use Case**: When SSE is not supported, bidirectional communication
> - **Features**: Deferred results, timeout handling, event queuing
> 
> ### 4. Event Stream Pattern (Reactive)
> - **Endpoints**:
>   - `GET /api/event-stream/notifications`
>   - `GET /api/event-stream/metrics`
>   - `GET /api/event-stream/channel/{channelName}`
> - **Use Case**: Reactive applications, backpressure handling
> - **Features**: Flux-based streaming, multiple channels, reactive operators
> 
> ### 5. Continuous Update Pattern
> - **Endpoints**:
>   - `GET /api/continuous/dashboard/{dashboardId}`
>   - `POST /api/continuous/dashboard/{dashboardId}/update`
>   - `GET /api/continuous/live-feed/{feedType}`
> - **Use Case**: Dashboards, monitoring systems, live feeds
> - **Features**: Long-lived connections, heartbeat mechanism, broadcast updates
> 
> ## Running the Application
> 
> ```bash
> mvn spring-boot:run
> ```
> 
> ## Testing the Patterns
> 
> 1. Open browser: `http://localhost:8080`
> 2. Click buttons to test each pattern
> 3. Watch real-time events in the output sections
> 
> ## API Testing with cURL
> 
> ### SSE Emitter
> ```bash
> curl -N http://localhost:8080/api/sse-emitter/stock/AAPL
> ```
> 
> ### Streaming Response
> ```bash
> curl -N http://localhost:8080/api/streaming/events
> ```
> 
> ### Long Polling
> ```bash
> # Register
> curl -X POST http://localhost:8080/api/long-polling/register/test-client
> 
> # Poll for events
> curl http://localhost:8080/api/long-polling/events/test-client
> 
> # Unregister
> curl -X DELETE http://localhost:8080/api/long-polling/unregister/test-client
> ```
> 
> ### Event Stream
> ```bash
> curl -N http://localhost:8080/api/event-stream/notifications
> curl -N http://localhost:8080/api/event-stream/metrics
> curl -N http://localhost:8080/api/event-stream/channel/channel-1
> ```
> 
> ### Continuous Update
> ```bash
> curl -N http://localhost:8080/api/continuous/dashboard/dash-1
> curl -N http://localhost:8080/api/continuous/live-feed/news
> ```
> 
> ## Key Features
> 
> - ✅ Multiple SSE implementation patterns
> - ✅ Reactive and non-reactive approaches
> - ✅ Connection lifecycle management
> - ✅ Error handling and timeout management
> - ✅ Heartbeat mechanism
> - ✅ Backpressure handling
> - ✅ Multi-client support
> - ✅ Interactive HTML test client
> 
> ## Technologies Used
> 
> - Spring Boot 3.2.0
> - Spring WebFlux (Reactive Streams)
> - Project Reactor
> - Lombok
> - Jackson
> 
> ## Pattern Comparison
> 
> | Pattern | Complexity | Use Case | Browser Support |
> |---------|-----------|----------|-----------------|
> | SSE Emitter | Low | Simple real-time updates | All modern browsers |
> | Streaming Response | Medium | Large data, custom protocols | All browsers |
> | Long Polling | Medium | Legacy browser support | All browsers including IE |
> | Event Stream | High | Reactive apps, backpressure | Modern browsers |
> | Continuous Update | Medium | Dashboards, monitoring | All modern browsers |
> ```
> 
> This complete implementation provides working examples of all five SSE patterns with proper error handling, connection management, and a functional test interface!