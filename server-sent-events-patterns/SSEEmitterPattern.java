package com.example.sse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;

/**
 * SSE Emitter Pattern
 * 
 * Purpose: Server-Sent Events (SSE) implementation for one-way server-to-client real-time updates.
 * SseEmitter allows the server to push updates to clients over HTTP connection.
 * 
 * Key Features:
 * - Unidirectional server-to-client communication
 * - Automatic reconnection from client side
 * - Event ID for last-event tracking
 * - Multiple named event types
 * - Long-lived HTTP connections
 * - Built on standard HTTP (no special protocols)
 * 
 * Use Cases:
 * - Live notifications
 * - Real-time dashboards
 * - Live feeds (news, sports, stock prices)
 * - Progress updates
 * - System monitoring
 * 
 * @author Spring Patterns
 */
@SpringBootApplication
@EnableScheduling
@EnableAsync
public class SSEEmitterPattern {

    public static void main(String[] args) {
        SpringApplication.run(SSEEmitterPattern.class, args);
    }

    /**
     * SSE Configuration
     */
    @Configuration
    public static class SSEConfig {
        
        @Bean
        public ExecutorService executorService() {
            return Executors.newCachedThreadPool();
        }
    }

    /**
     * SSE Controller - Endpoints for SSE connections
     */
    @RestController
    @RequestMapping("/api/sse")
    public static class SSEController {

        private final SSEService sseService;

        public SSEController(SSEService sseService) {
            this.sseService = sseService;
        }

        /**
         * Basic SSE endpoint - simple text updates
         */
        @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
        public SseEmitter streamEvents() {
            SseEmitter emitter = new SseEmitter(Long.MAX_VALUE); // No timeout
            
            sseService.addEmitter("general", emitter);
            
            emitter.onCompletion(() -> sseService.removeEmitter("general", emitter));
            emitter.onTimeout(() -> sseService.removeEmitter("general", emitter));
            emitter.onError((ex) -> sseService.removeEmitter("general", emitter));
            
            return emitter;
        }

        /**
         * Named event stream - for specific event channels
         */
        @GetMapping(path = "/stream/{channel}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
        public SseEmitter streamChannelEvents(@PathVariable String channel) {
            SseEmitter emitter = new SseEmitter(300000L); // 5 minutes timeout
            
            sseService.addEmitter(channel, emitter);
            
            emitter.onCompletion(() -> {
                System.out.println("Client disconnected from channel: " + channel);
                sseService.removeEmitter(channel, emitter);
            });
            
            emitter.onTimeout(() -> {
                System.out.println("SSE connection timeout for channel: " + channel);
                sseService.removeEmitter(channel, emitter);
            });
            
            emitter.onError((ex) -> {
                System.err.println("SSE error for channel: " + channel + " - " + ex.getMessage());
                sseService.removeEmitter(channel, emitter);
            });
            
            // Send initial connection confirmation
            try {
                SseEmitter.SseEventBuilder event = SseEmitter.event()
                    .id(UUID.randomUUID().toString())
                    .name("connection")
                    .data("Connected to channel: " + channel);
                emitter.send(event);
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
            
            return emitter;
        }

        /**
         * Notification stream with specific event types
         */
        @GetMapping(path = "/notifications", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
        public SseEmitter streamNotifications(@RequestParam(required = false) String userId) {
            SseEmitter emitter = new SseEmitter(0L); // No timeout
            
            String channel = userId != null ? "notifications-" + userId : "notifications-all";
            sseService.addEmitter(channel, emitter);
            
            emitter.onCompletion(() -> sseService.removeEmitter(channel, emitter));
            emitter.onTimeout(() -> sseService.removeEmitter(channel, emitter));
            emitter.onError((ex) -> sseService.removeEmitter(channel, emitter));
            
            // Send welcome notification
            try {
                Notification welcomeNotification = new Notification(
                    "system",
                    "Welcome",
                    "Connected to notification stream",
                    NotificationType.INFO,
                    LocalDateTime.now()
                );
                
                SseEmitter.SseEventBuilder event = SseEmitter.event()
                    .id(UUID.randomUUID().toString())
                    .name("notification")
                    .data(welcomeNotification);
                    
                emitter.send(event);
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
            
            return emitter;
        }

        /**
         * Stock price updates stream
         */
        @GetMapping(path = "/stocks/{symbol}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
        public SseEmitter streamStockPrice(@PathVariable String symbol) {
            SseEmitter emitter = new SseEmitter(600000L); // 10 minutes
            
            String channel = "stock-" + symbol.toUpperCase();
            sseService.addEmitter(channel, emitter);
            
            emitter.onCompletion(() -> sseService.removeEmitter(channel, emitter));
            emitter.onTimeout(() -> sseService.removeEmitter(channel, emitter));
            emitter.onError((ex) -> sseService.removeEmitter(channel, emitter));
            
            return emitter;
        }

        /**
         * Send message to specific channel
         */
        @PostMapping("/send/{channel}")
        public Map<String, Object> sendToChannel(
                @PathVariable String channel,
                @RequestBody Map<String, String> message) {
            
            int count = sseService.sendToChannel(channel, message.get("message"));
            
            Map<String, Object> response = new HashMap<>();
            response.put("channel", channel);
            response.put("recipientCount", count);
            response.put("timestamp", LocalDateTime.now());
            
            return response;
        }

        /**
         * Send notification to user
         */
        @PostMapping("/notify/{userId}")
        public Map<String, Object> notifyUser(
                @PathVariable String userId,
                @RequestBody Notification notification) {
            
            notification.setTimestamp(LocalDateTime.now());
            int count = sseService.sendNotification(userId, notification);
            
            Map<String, Object> response = new HashMap<>();
            response.put("userId", userId);
            response.put("recipientCount", count);
            response.put("notification", notification);
            
            return response;
        }

        /**
         * Broadcast to all connections
         */
        @PostMapping("/broadcast")
        public Map<String, Object> broadcast(@RequestBody Map<String, String> message) {
            int count = sseService.broadcastToAll(message.get("message"));
            
            Map<String, Object> response = new HashMap<>();
            response.put("totalRecipients", count);
            response.put("timestamp", LocalDateTime.now());
            
            return response;
        }

        /**
         * Get active connections info
         */
        @GetMapping("/connections")
        public Map<String, Object> getConnectionsInfo() {
            return sseService.getConnectionsInfo();
        }
    }

    /**
     * SSE Service - Manages emitters and event distribution
     */
    @Service
    public static class SSEService {

        private final Map<String, List<SseEmitter>> emitters = new ConcurrentHashMap<>();
        private final ExecutorService executorService;

        public SSEService(ExecutorService executorService) {
            this.executorService = executorService;
        }

        /**
         * Add emitter to channel
         */
        public void addEmitter(String channel, SseEmitter emitter) {
            emitters.computeIfAbsent(channel, k -> new CopyOnWriteArrayList<>()).add(emitter);
            System.out.println("Added emitter to channel: " + channel + 
                             " (Total: " + emitters.get(channel).size() + ")");
        }

        /**
         * Remove emitter from channel
         */
        public void removeEmitter(String channel, SseEmitter emitter) {
            List<SseEmitter> channelEmitters = emitters.get(channel);
            if (channelEmitters != null) {
                channelEmitters.remove(emitter);
                System.out.println("Removed emitter from channel: " + channel + 
                                 " (Remaining: " + channelEmitters.size() + ")");
                
                if (channelEmitters.isEmpty()) {
                    emitters.remove(channel);
                }
            }
        }

        /**
         * Send message to specific channel
         */
        public int sendToChannel(String channel, String message) {
            List<SseEmitter> channelEmitters = emitters.get(channel);
            if (channelEmitters == null || channelEmitters.isEmpty()) {
                return 0;
            }

            int successCount = 0;
            List<SseEmitter> deadEmitters = new ArrayList<>();

            for (SseEmitter emitter : channelEmitters) {
                try {
                    SseEmitter.SseEventBuilder event = SseEmitter.event()
                        .id(UUID.randomUUID().toString())
                        .name("message")
                        .data(new MessageEvent(channel, message, LocalDateTime.now()));
                    
                    emitter.send(event);
                    successCount++;
                } catch (IOException e) {
                    deadEmitters.add(emitter);
                }
            }

            // Clean up dead emitters
            deadEmitters.forEach(emitter -> removeEmitter(channel, emitter));

            return successCount;
        }

        /**
         * Send notification to user
         */
        public int sendNotification(String userId, Notification notification) {
            String channel = "notifications-" + userId;
            List<SseEmitter> channelEmitters = emitters.get(channel);
            
            if (channelEmitters == null || channelEmitters.isEmpty()) {
                // Try sending to all notifications channel
                channel = "notifications-all";
                channelEmitters = emitters.get(channel);
                if (channelEmitters == null || channelEmitters.isEmpty()) {
                    return 0;
                }
            }

            int successCount = 0;
            List<SseEmitter> deadEmitters = new ArrayList<>();

            for (SseEmitter emitter : channelEmitters) {
                try {
                    SseEmitter.SseEventBuilder event = SseEmitter.event()
                        .id(UUID.randomUUID().toString())
                        .name("notification")
                        .data(notification);
                    
                    emitter.send(event);
                    successCount++;
                } catch (IOException e) {
                    deadEmitters.add(emitter);
                }
            }

            deadEmitters.forEach(emitter -> removeEmitter(channel, emitter));

            return successCount;
        }

        /**
         * Broadcast to all emitters in all channels
         */
        public int broadcastToAll(String message) {
            int totalSent = 0;
            
            for (String channel : emitters.keySet()) {
                totalSent += sendToChannel(channel, message);
            }
            
            return totalSent;
        }

        /**
         * Send stock price update
         */
        public void sendStockUpdate(String symbol, StockPrice stockPrice) {
            String channel = "stock-" + symbol.toUpperCase();
            List<SseEmitter> channelEmitters = emitters.get(channel);
            
            if (channelEmitters == null || channelEmitters.isEmpty()) {
                return;
            }

            List<SseEmitter> deadEmitters = new ArrayList<>();

            for (SseEmitter emitter : channelEmitters) {
                try {
                    SseEmitter.SseEventBuilder event = SseEmitter.event()
                        .id(UUID.randomUUID().toString())
                        .name("stock-update")
                        .data(stockPrice);
                    
                    emitter.send(event);
                } catch (IOException e) {
                    deadEmitters.add(emitter);
                }
            }

            deadEmitters.forEach(emitter -> removeEmitter(channel, emitter));
        }

        /**
         * Get connections info
         */
        public Map<String, Object> getConnectionsInfo() {
            Map<String, Object> info = new HashMap<>();
            Map<String, Integer> channelCounts = new HashMap<>();
            
            int totalConnections = 0;
            for (Map.Entry<String, List<SseEmitter>> entry : emitters.entrySet()) {
                int count = entry.getValue().size();
                channelCounts.put(entry.getKey(), count);
                totalConnections += count;
            }
            
            info.put("totalConnections", totalConnections);
            info.put("totalChannels", emitters.size());
            info.put("channelCounts", channelCounts);
            info.put("timestamp", LocalDateTime.now());
            
            return info;
        }

        /**
         * Scheduled task - Send periodic updates
         */
        @Scheduled(fixedRate = 5000)
        public void sendPeriodicUpdates() {
            if (emitters.isEmpty()) {
                return;
            }

            // Send heartbeat to general channel
            sendToChannel("general", "Heartbeat at " + 
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME));

            // Simulate stock price updates
            simulateStockUpdates();
        }

        /**
         * Simulate stock price updates
         */
        private void simulateStockUpdates() {
            List<String> symbols = Arrays.asList("AAPL", "GOOGL", "MSFT", "AMZN", "TSLA");
            Random random = new Random();
            
            for (String symbol : symbols) {
                String channel = "stock-" + symbol;
                if (emitters.containsKey(channel)) {
                    double basePrice = 100 + random.nextInt(400);
                    double change = (random.nextDouble() - 0.5) * 10;
                    double changePercent = (change / basePrice) * 100;
                    
                    StockPrice stockPrice = new StockPrice(
                        symbol,
                        basePrice + change,
                        change,
                        changePercent,
                        (long)(random.nextInt(10000) + 1000),
                        LocalDateTime.now()
                    );
                    
                    sendStockUpdate(symbol, stockPrice);
                }
            }
        }
    }

    /**
     * Message Event Model
     */
    public static class MessageEvent {
        private String channel;
        private String message;
        private LocalDateTime timestamp;

        public MessageEvent() {}

        public MessageEvent(String channel, String message, LocalDateTime timestamp) {
            this.channel = channel;
            this.message = message;
            this.timestamp = timestamp;
        }

        // Getters and Setters
        public String getChannel() { return channel; }
        public void setChannel(String channel) { this.channel = channel; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }

    /**
     * Notification Model
     */
    public static class Notification {
        private String id;
        private String title;
        private String message;
        private NotificationType type;
        private LocalDateTime timestamp;

        public Notification() {
            this.id = UUID.randomUUID().toString();
        }

        public Notification(String id, String title, String message, 
                          NotificationType type, LocalDateTime timestamp) {
            this.id = id;
            this.title = title;
            this.message = message;
            this.type = type;
            this.timestamp = timestamp;
        }

        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public NotificationType getType() { return type; }
        public void setType(NotificationType type) { this.type = type; }

        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }

    /**
     * Notification Type Enum
     */
    public enum NotificationType {
        INFO, WARNING, ERROR, SUCCESS
    }

    /**
     * Stock Price Model
     */
    public static class StockPrice {
        private String symbol;
        private double price;
        private double change;
        private double changePercent;
        private long volume;
        private LocalDateTime timestamp;

        public StockPrice() {}

        public StockPrice(String symbol, double price, double change, 
                         double changePercent, long volume, LocalDateTime timestamp) {
            this.symbol = symbol;
            this.price = price;
            this.change = change;
            this.changePercent = changePercent;
            this.volume = volume;
            this.timestamp = timestamp;
        }

        // Getters and Setters
        public String getSymbol() { return symbol; }
        public void setSymbol(String symbol) { this.symbol = symbol; }

        public double getPrice() { return price; }
        public void setPrice(double price) { this.price = price; }

        public double getChange() { return change; }
        public void setChange(double change) { this.change = change; }

        public double getChangePercent() { return changePercent; }
        public void setChangePercent(double changePercent) { this.changePercent = changePercent; }

        public long getVolume() { return volume; }
        public void setVolume(long volume) { this.volume = volume; }

        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }

    /**
     * HTML Client Example
     */
    @Controller
    public static class WebController {
        
        @GetMapping("/")
        public String index() {
            return "sse-demo";
        }
    }
}

/*
 * Client-Side JavaScript Example:
 * 
 * // Basic SSE connection
 * const eventSource = new EventSource('/api/sse/stream');
 * 
 * eventSource.onmessage = function(event) {
 *     console.log('Message:', event.data);
 * };
 * 
 * eventSource.onerror = function(error) {
 *     console.error('SSE Error:', error);
 * };
 * 
 * // Named event listener
 * eventSource.addEventListener('notification', function(event) {
 *     const notification = JSON.parse(event.data);
 *     console.log('Notification:', notification);
 * });
 * 
 * // Channel-specific connection
 * const newsSource = new EventSource('/api/sse/stream/news');
 * newsSource.onmessage = function(event) {
 *     console.log('News update:', event.data);
 * };
 * 
 * // Stock price updates
 * const stockSource = new EventSource('/api/sse/stocks/AAPL');
 * stockSource.addEventListener('stock-update', function(event) {
 *     const stockData = JSON.parse(event.data);
 *     console.log('Stock update:', stockData);
 * });
 * 
 * // Close connection
 * eventSource.close();
 * 
 * HTML Example:
 * <!DOCTYPE html>
 * <html>
 * <head>
 *     <title>SSE Demo</title>
 * </head>
 * <body>
 *     <h1>Server-Sent Events Demo</h1>
 *     <div id="events"></div>
 *     
 *     <script>
 *         const eventSource = new EventSource('/api/sse/stream');
 *         const eventsDiv = document.getElementById('events');
 *         
 *         eventSource.onmessage = function(event) {
 *             const eventData = JSON.parse(event.data);
 *             const eventElement = document.createElement('div');
 *             eventElement.textContent = JSON.stringify(eventData, null, 2);
 *             eventsDiv.appendChild(eventElement);
 *         };
 *     </script>
 * </body>
 * </html>
 */

/*
 * Application Properties:
 * 
 * # Server configuration
 * server.port=8080
 * 
 * # SSE configuration
 * spring.mvc.async.request-timeout=600000
 * 
 * # Thread pool for async
 * spring.task.execution.pool.core-size=10
 * spring.task.execution.pool.max-size=50
 * spring.task.execution.pool.queue-capacity=100
 * 
 * # Scheduling
 * spring.task.scheduling.pool.size=5
 */
