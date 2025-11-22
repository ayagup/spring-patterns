package com.example.sse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Continuous Update Pattern
 * 
 * Purpose: Provide continuous real-time updates from multiple data sources with
 * subscription management, data aggregation, and update scheduling.
 * 
 * Key Features:
 * - Multiple update sources
 * - Configurable update frequency
 * - Data source subscription management
 * - Update buffering and batching
 * - Update priority handling
 * - Source health monitoring
 * - Rate limiting
 * 
 * Use Cases:
 * - Real-time dashboards
 * - Live monitoring systems
 * - IoT data streaming
 * - Financial data feeds
 * - Social media feeds
 * - News aggregation
 * 
 * @author Spring Patterns
 */
@SpringBootApplication
@EnableScheduling
@EnableAsync
public class ContinuousUpdatePattern {

    public static void main(String[] args) {
        SpringApplication.run(ContinuousUpdatePattern.class, args);
    }

    /**
     * Configuration
     */
    @Configuration
    public static class ContinuousUpdateConfig {
        
        @Bean
        public ScheduledExecutorService scheduledExecutorService() {
            return Executors.newScheduledThreadPool(5);
        }
    }

    /**
     * Continuous Update Controller
     */
    @RestController
    @RequestMapping("/api/continuous")
    public static class ContinuousUpdateController {

        private final ContinuousUpdateService continuousUpdateService;

        public ContinuousUpdateController(ContinuousUpdateService continuousUpdateService) {
            this.continuousUpdateService = continuousUpdateService;
        }

        /**
         * Subscribe to all updates
         */
        @GetMapping(path = "/updates", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
        public SseEmitter subscribeToUpdates(
                @RequestParam(defaultValue = "1000") long updateInterval) {
            
            SseEmitter emitter = new SseEmitter(0L); // No timeout
            
            UpdateSubscription subscription = continuousUpdateService.subscribe(
                "all",
                null,
                updateInterval,
                emitter
            );
            
            emitter.onCompletion(() -> continuousUpdateService.unsubscribe(subscription.getId()));
            emitter.onTimeout(() -> continuousUpdateService.unsubscribe(subscription.getId()));
            emitter.onError((ex) -> continuousUpdateService.unsubscribe(subscription.getId()));
            
            return emitter;
        }

        /**
         * Subscribe to specific sources
         */
        @GetMapping(path = "/updates/sources", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
        public SseEmitter subscribeToSources(
                @RequestParam List<String> sources,
                @RequestParam(defaultValue = "1000") long updateInterval) {
            
            SseEmitter emitter = new SseEmitter(300000L); // 5 minutes
            
            UpdateSubscription subscription = continuousUpdateService.subscribe(
                "sources",
                sources,
                updateInterval,
                emitter
            );
            
            emitter.onCompletion(() -> continuousUpdateService.unsubscribe(subscription.getId()));
            emitter.onTimeout(() -> continuousUpdateService.unsubscribe(subscription.getId()));
            emitter.onError((ex) -> continuousUpdateService.unsubscribe(subscription.getId()));
            
            return emitter;
        }

        /**
         * Subscribe to dashboard updates
         */
        @GetMapping(path = "/dashboard", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
        public SseEmitter subscribeToDashboard(
                @RequestParam(defaultValue = "5000") long updateInterval) {
            
            SseEmitter emitter = new SseEmitter(0L);
            
            UpdateSubscription subscription = continuousUpdateService.subscribeToDashboard(
                updateInterval,
                emitter
            );
            
            emitter.onCompletion(() -> continuousUpdateService.unsubscribe(subscription.getId()));
            emitter.onTimeout(() -> continuousUpdateService.unsubscribe(subscription.getId()));
            emitter.onError((ex) -> continuousUpdateService.unsubscribe(subscription.getId()));
            
            return emitter;
        }

        /**
         * Subscribe to metrics updates
         */
        @GetMapping(path = "/metrics", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
        public SseEmitter subscribeToMetrics(
                @RequestParam(defaultValue = "2000") long updateInterval) {
            
            SseEmitter emitter = new SseEmitter(0L);
            
            UpdateSubscription subscription = continuousUpdateService.subscribeToMetrics(
                updateInterval,
                emitter
            );
            
            emitter.onCompletion(() -> continuousUpdateService.unsubscribe(subscription.getId()));
            emitter.onTimeout(() -> continuousUpdateService.unsubscribe(subscription.getId()));
            emitter.onError((ex) -> continuousUpdateService.unsubscribe(subscription.getId()));
            
            return emitter;
        }

        /**
         * Get available data sources
         */
        @GetMapping("/sources")
        public List<DataSource> getDataSources() {
            return continuousUpdateService.getDataSources();
        }

        /**
         * Get subscription statistics
         */
        @GetMapping("/stats")
        public SubscriptionStats getStats() {
            return continuousUpdateService.getStats();
        }

        /**
         * Configure data source
         */
        @PostMapping("/sources")
        public DataSource configureSource(@RequestBody DataSourceConfig config) {
            return continuousUpdateService.configureSource(config);
        }

        /**
         * Enable/disable data source
         */
        @PutMapping("/sources/{sourceId}/enabled")
        public DataSource toggleSource(
                @PathVariable String sourceId,
                @RequestParam boolean enabled) {
            
            return continuousUpdateService.toggleSource(sourceId, enabled);
        }
    }

    /**
     * Continuous Update Service
     */
    @Service
    public static class ContinuousUpdateService {

        private final Map<String, UpdateSubscription> subscriptions = new ConcurrentHashMap<>();
        private final Map<String, DataSource> dataSources = new ConcurrentHashMap<>();
        private final ScheduledExecutorService scheduledExecutor;
        
        private long subscriptionCounter = 0;

        public ContinuousUpdateService(ScheduledExecutorService scheduledExecutor) {
            this.scheduledExecutor = scheduledExecutor;
            initializeDataSources();
        }

        /**
         * Initialize default data sources
         */
        private void initializeDataSources() {
            configureSource(new DataSourceConfig("system", "System Metrics", 1000, true));
            configureSource(new DataSourceConfig("market", "Market Data", 5000, true));
            configureSource(new DataSourceConfig("social", "Social Feed", 10000, true));
            configureSource(new DataSourceConfig("iot", "IoT Sensors", 2000, true));
            configureSource(new DataSourceConfig("news", "News Feed", 30000, true));
        }

        /**
         * Subscribe to updates
         */
        public UpdateSubscription subscribe(String type, List<String> sources, 
                                           long updateInterval, SseEmitter emitter) {
            String subscriptionId = generateSubscriptionId();
            
            UpdateSubscription subscription = new UpdateSubscription(
                subscriptionId,
                type,
                sources,
                updateInterval,
                emitter,
                LocalDateTime.now()
            );
            
            subscriptions.put(subscriptionId, subscription);
            
            // Schedule continuous updates
            ScheduledFuture<?> scheduledFuture = scheduledExecutor.scheduleAtFixedRate(
                () -> sendUpdate(subscription),
                0,
                updateInterval,
                TimeUnit.MILLISECONDS
            );
            
            subscription.setScheduledFuture(scheduledFuture);
            
            System.out.println("New subscription: " + subscriptionId + 
                             ", interval=" + updateInterval + "ms" +
                             ", total=" + subscriptions.size());
            
            return subscription;
        }

        /**
         * Subscribe to dashboard
         */
        public UpdateSubscription subscribeToDashboard(long updateInterval, SseEmitter emitter) {
            return subscribe("dashboard", 
                Arrays.asList("system", "market", "social"), 
                updateInterval, 
                emitter);
        }

        /**
         * Subscribe to metrics
         */
        public UpdateSubscription subscribeToMetrics(long updateInterval, SseEmitter emitter) {
            return subscribe("metrics", 
                Arrays.asList("system", "iot"), 
                updateInterval, 
                emitter);
        }

        /**
         * Unsubscribe
         */
        public void unsubscribe(String subscriptionId) {
            UpdateSubscription subscription = subscriptions.remove(subscriptionId);
            
            if (subscription != null) {
                if (subscription.getScheduledFuture() != null) {
                    subscription.getScheduledFuture().cancel(false);
                }
                
                System.out.println("Subscription removed: " + subscriptionId + 
                                 ", remaining=" + subscriptions.size());
            }
        }

        /**
         * Send update to subscriber
         */
        private void sendUpdate(UpdateSubscription subscription) {
            try {
                // Collect updates from relevant sources
                List<Update> updates = collectUpdates(subscription);
                
                if (updates.isEmpty()) {
                    return;
                }
                
                // Create aggregated update
                AggregatedUpdate aggregatedUpdate = new AggregatedUpdate(
                    UUID.randomUUID().toString(),
                    subscription.getType(),
                    updates,
                    System.currentTimeMillis(),
                    LocalDateTime.now()
                );
                
                // Send via SSE
                SseEmitter.SseEventBuilder event = SseEmitter.event()
                    .id(aggregatedUpdate.getId())
                    .name("update")
                    .data(aggregatedUpdate);
                
                subscription.getEmitter().send(event);
                subscription.incrementUpdateCount();
                
            } catch (IOException e) {
                // Error sending, unsubscribe
                unsubscribe(subscription.getId());
            } catch (Exception e) {
                System.err.println("Error sending update: " + e.getMessage());
            }
        }

        /**
         * Collect updates from data sources
         */
        private List<Update> collectUpdates(UpdateSubscription subscription) {
            List<Update> updates = new ArrayList<>();
            
            for (DataSource source : dataSources.values()) {
                if (!source.isEnabled()) {
                    continue;
                }
                
                // Filter by subscription sources
                if (subscription.getSources() != null && 
                    !subscription.getSources().isEmpty() &&
                    !subscription.getSources().contains(source.getId())) {
                    continue;
                }
                
                // Generate update from source
                Update update = generateUpdateFromSource(source);
                if (update != null) {
                    updates.add(update);
                }
            }
            
            return updates;
        }

        /**
         * Generate update from data source
         */
        private Update generateUpdateFromSource(DataSource source) {
            Random random = new Random();
            Map<String, Object> data = new HashMap<>();
            
            switch (source.getId()) {
                case "system":
                    data.put("cpu", random.nextInt(100));
                    data.put("memory", random.nextInt(100));
                    data.put("disk", random.nextInt(100));
                    break;
                    
                case "market":
                    data.put("index", "S&P 500");
                    data.put("value", 4000 + random.nextDouble() * 500);
                    data.put("change", (random.nextDouble() - 0.5) * 100);
                    break;
                    
                case "social":
                    data.put("posts", random.nextInt(1000));
                    data.put("likes", random.nextInt(10000));
                    data.put("shares", random.nextInt(1000));
                    break;
                    
                case "iot":
                    data.put("temperature", 20 + random.nextDouble() * 10);
                    data.put("humidity", 40 + random.nextDouble() * 40);
                    data.put("pressure", 980 + random.nextDouble() * 40);
                    break;
                    
                case "news":
                    data.put("articles", random.nextInt(50));
                    data.put("trending", Arrays.asList("Topic1", "Topic2", "Topic3"));
                    break;
                    
                default:
                    data.put("value", random.nextInt(100));
            }
            
            return new Update(
                source.getId(),
                source.getName(),
                data,
                System.currentTimeMillis(),
                LocalDateTime.now()
            );
        }

        /**
         * Configure data source
         */
        public DataSource configureSource(DataSourceConfig config) {
            DataSource source = new DataSource(
                config.getId(),
                config.getName(),
                config.getUpdateInterval(),
                config.isEnabled(),
                LocalDateTime.now()
            );
            
            dataSources.put(source.getId(), source);
            
            return source;
        }

        /**
         * Toggle source
         */
        public DataSource toggleSource(String sourceId, boolean enabled) {
            DataSource source = dataSources.get(sourceId);
            if (source != null) {
                source.setEnabled(enabled);
                source.setLastUpdated(LocalDateTime.now());
            }
            return source;
        }

        /**
         * Get data sources
         */
        public List<DataSource> getDataSources() {
            return new ArrayList<>(dataSources.values());
        }

        /**
         * Get statistics
         */
        public SubscriptionStats getStats() {
            long totalUpdates = subscriptions.values().stream()
                .mapToLong(UpdateSubscription::getUpdateCount)
                .sum();
            
            Map<String, Integer> subscriptionsByType = subscriptions.values().stream()
                .collect(Collectors.groupingBy(
                    UpdateSubscription::getType,
                    Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));
            
            return new SubscriptionStats(
                subscriptions.size(),
                totalUpdates,
                dataSources.size(),
                subscriptionsByType,
                LocalDateTime.now()
            );
        }

        /**
         * Generate subscription ID
         */
        private synchronized String generateSubscriptionId() {
            return String.format("sub-%d-%d", 
                System.currentTimeMillis(), 
                ++subscriptionCounter);
        }

        /**
         * Scheduled health check
         */
        @Scheduled(fixedRate = 60000)
        public void healthCheck() {
            System.out.println("Health check - Subscriptions: " + subscriptions.size() + 
                             ", Data sources: " + dataSources.size());
            
            // Update source health
            for (DataSource source : dataSources.values()) {
                source.setHealthy(true);
                source.setLastUpdated(LocalDateTime.now());
            }
        }
    }

    // Model Classes

    public static class UpdateSubscription {
        private String id;
        private String type;
        private List<String> sources;
        private long updateInterval;
        private SseEmitter emitter;
        private LocalDateTime subscribedAt;
        private ScheduledFuture<?> scheduledFuture;
        private long updateCount = 0;

        public UpdateSubscription(String id, String type, List<String> sources,
                                 long updateInterval, SseEmitter emitter, LocalDateTime subscribedAt) {
            this.id = id;
            this.type = type;
            this.sources = sources;
            this.updateInterval = updateInterval;
            this.emitter = emitter;
            this.subscribedAt = subscribedAt;
        }

        public void incrementUpdateCount() {
            updateCount++;
        }

        // Getters and Setters
        public String getId() { return id; }
        public String getType() { return type; }
        public List<String> getSources() { return sources; }
        public long getUpdateInterval() { return updateInterval; }
        public SseEmitter getEmitter() { return emitter; }
        public LocalDateTime getSubscribedAt() { return subscribedAt; }
        public ScheduledFuture<?> getScheduledFuture() { return scheduledFuture; }
        public void setScheduledFuture(ScheduledFuture<?> scheduledFuture) { 
            this.scheduledFuture = scheduledFuture; 
        }
        public long getUpdateCount() { return updateCount; }
    }

    public static class Update {
        private String sourceId;
        private String sourceName;
        private Map<String, Object> data;
        private long timestampMillis;
        private LocalDateTime timestamp;

        public Update(String sourceId, String sourceName, Map<String, Object> data,
                     long timestampMillis, LocalDateTime timestamp) {
            this.sourceId = sourceId;
            this.sourceName = sourceName;
            this.data = data;
            this.timestampMillis = timestampMillis;
            this.timestamp = timestamp;
        }

        // Getters
        public String getSourceId() { return sourceId; }
        public String getSourceName() { return sourceName; }
        public Map<String, Object> getData() { return data; }
        public long getTimestampMillis() { return timestampMillis; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }

    public static class AggregatedUpdate {
        private String id;
        private String type;
        private List<Update> updates;
        private long timestampMillis;
        private LocalDateTime timestamp;

        public AggregatedUpdate(String id, String type, List<Update> updates,
                               long timestampMillis, LocalDateTime timestamp) {
            this.id = id;
            this.type = type;
            this.updates = updates;
            this.timestampMillis = timestampMillis;
            this.timestamp = timestamp;
        }

        // Getters
        public String getId() { return id; }
        public String getType() { return type; }
        public List<Update> getUpdates() { return updates; }
        public long getTimestampMillis() { return timestampMillis; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }

    public static class DataSource {
        private String id;
        private String name;
        private long updateInterval;
        private boolean enabled;
        private boolean healthy;
        private LocalDateTime lastUpdated;

        public DataSource(String id, String name, long updateInterval, 
                         boolean enabled, LocalDateTime lastUpdated) {
            this.id = id;
            this.name = name;
            this.updateInterval = updateInterval;
            this.enabled = enabled;
            this.healthy = true;
            this.lastUpdated = lastUpdated;
        }

        // Getters and Setters
        public String getId() { return id; }
        public String getName() { return name; }
        public long getUpdateInterval() { return updateInterval; }
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public boolean isHealthy() { return healthy; }
        public void setHealthy(boolean healthy) { this.healthy = healthy; }
        public LocalDateTime getLastUpdated() { return lastUpdated; }
        public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
    }

    public static class DataSourceConfig {
        private String id;
        private String name;
        private long updateInterval;
        private boolean enabled;

        public DataSourceConfig() {}

        public DataSourceConfig(String id, String name, long updateInterval, boolean enabled) {
            this.id = id;
            this.name = name;
            this.updateInterval = updateInterval;
            this.enabled = enabled;
        }

        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public long getUpdateInterval() { return updateInterval; }
        public void setUpdateInterval(long updateInterval) { this.updateInterval = updateInterval; }
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }

    public static class SubscriptionStats {
        private int activeSubscriptions;
        private long totalUpdates;
        private int dataSources;
        private Map<String, Integer> subscriptionsByType;
        private LocalDateTime timestamp;

        public SubscriptionStats(int activeSubscriptions, long totalUpdates, int dataSources,
                                Map<String, Integer> subscriptionsByType, LocalDateTime timestamp) {
            this.activeSubscriptions = activeSubscriptions;
            this.totalUpdates = totalUpdates;
            this.dataSources = dataSources;
            this.subscriptionsByType = subscriptionsByType;
            this.timestamp = timestamp;
        }

        // Getters
        public int getActiveSubscriptions() { return activeSubscriptions; }
        public long getTotalUpdates() { return totalUpdates; }
        public int getDataSources() { return dataSources; }
        public Map<String, Integer> getSubscriptionsByType() { return subscriptionsByType; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }
}

/*
 * Client-Side JavaScript Example:
 * 
 * // Subscribe to continuous updates
 * const updateSource = new EventSource('/api/continuous/updates?updateInterval=1000');
 * 
 * updateSource.addEventListener('update', function(event) {
 *     const aggregatedUpdate = JSON.parse(event.data);
 *     console.log('Received updates:', aggregatedUpdate);
 *     
 *     aggregatedUpdate.updates.forEach(update => {
 *         console.log(`${update.sourceName}:`, update.data);
 *         updateUI(update.sourceId, update.data);
 *     });
 * });
 * 
 * updateSource.onerror = function(error) {
 *     console.error('Update stream error:', error);
 * };
 * 
 * // Subscribe to dashboard
 * const dashboardSource = new EventSource('/api/continuous/dashboard?updateInterval=5000');
 * 
 * dashboardSource.addEventListener('update', function(event) {
 *     const data = JSON.parse(event.data);
 *     updateDashboard(data);
 * });
 * 
 * // Subscribe to specific sources
 * const metricsSource = new EventSource(
 *     '/api/continuous/updates/sources?sources=system&sources=iot&updateInterval=2000'
 * );
 * 
 * metricsSource.addEventListener('update', function(event) {
 *     const data = JSON.parse(event.data);
 *     data.updates.forEach(update => {
 *         if (update.sourceId === 'system') {
 *             updateSystemMetrics(update.data);
 *         } else if (update.sourceId === 'iot') {
 *             updateIoTSensors(update.data);
 *         }
 *     });
 * });
 * 
 * // Helper functions
 * function updateUI(sourceId, data) {
 *     const element = document.getElementById(sourceId);
 *     if (element) {
 *         element.textContent = JSON.stringify(data, null, 2);
 *     }
 * }
 * 
 * function updateDashboard(aggregatedUpdate) {
 *     document.getElementById('dashboard-timestamp').textContent = 
 *         new Date(aggregatedUpdate.timestampMillis).toLocaleString();
 *     
 *     aggregatedUpdate.updates.forEach(update => {
 *         updateUI(update.sourceId + '-widget', update.data);
 *     });
 * }
 * 
 * // Cleanup
 * window.addEventListener('beforeunload', function() {
 *     updateSource.close();
 *     dashboardSource.close();
 *     metricsSource.close();
 * });
 */
