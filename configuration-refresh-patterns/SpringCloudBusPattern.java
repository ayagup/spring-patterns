package com.example.demo.patterns.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.bus.event.RefreshRemoteApplicationEvent;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Spring Cloud Bus Pattern - Distributed Configuration Refresh
 * 
 * Purpose:
 * - Propagate configuration changes across all application instances
 * - Trigger refresh events via message broker (RabbitMQ/Kafka)
 * - Synchronize configuration in distributed systems
 * - Enable cluster-wide configuration updates
 * - Broadcast refresh to specific services or all instances
 * 
 * Use Cases:
 * - Microservices configuration synchronization
 * - Multi-instance application refresh
 * - Feature flag rollout across cluster
 * - Database connection pool updates for all instances
 * - API endpoint configuration changes
 * - Circuit breaker threshold updates cluster-wide
 * - Rate limiting configuration synchronization
 * - Security settings propagation
 * - Cache configuration updates
 * - Logging level changes for all instances
 * 
 * Key Concepts:
 * - Spring Cloud Bus: Message broker integration for event distribution
 * - RefreshRemoteApplicationEvent: Event broadcast to refresh configurations
 * - Message Broker: RabbitMQ or Kafka for reliable event delivery
 * - Destination Filtering: Target specific services or instances
 * - Event Propagation: Automatic broadcast to all subscribed instances
 * - ACK Mechanism: Confirmation of refresh completion
 * - /actuator/bus-refresh: Endpoint to trigger cluster-wide refresh
 * - Service ID: Identify and filter target services
 * 
 * Implementation Patterns:
 * 1. Basic Spring Cloud Bus setup with RabbitMQ
 * 2. Kafka-based Spring Cloud Bus
 * 3. Broadcast refresh to all instances
 * 4. Targeted refresh to specific service
 * 5. Refresh with destination patterns
 * 6. Custom bus events
 * 7. Bus refresh event listener
 * 8. Multi-tenant configuration refresh
 * 9. Refresh acknowledgment tracking
 * 10. Conditional refresh based on environment
 * 11. Refresh with rollback capability
 * 12. Bus refresh monitoring and metrics
 * 
 * Dependencies:
 * <!-- For RabbitMQ -->
 * <dependency>
 *     <groupId>org.springframework.cloud</groupId>
 *     <artifactId>spring-cloud-starter-bus-amqp</artifactId>
 * </dependency>
 * 
 * <!-- For Kafka -->
 * <dependency>
 *     <groupId>org.springframework.cloud</groupId>
 *     <artifactId>spring-cloud-starter-bus-kafka</artifactId>
 * </dependency>
 * 
 * <dependency>
 *     <groupId>org.springframework.cloud</groupId>
 *     <artifactId>spring-cloud-starter-config</artifactId>
 * </dependency>
 * 
 * Configuration (application.yml):
 * 
 * # RabbitMQ Configuration
 * spring:
 *   rabbitmq:
 *     host: localhost
 *     port: 5672
 *     username: guest
 *     password: guest
 *   cloud:
 *     bus:
 *       enabled: true
 *       refresh:
 *         enabled: true
 *       ack:
 *         enabled: true  # Enable acknowledgments
 *       trace:
 *         enabled: true  # Enable event tracing
 *       destination: springCloudBus  # Exchange name
 *     config:
 *       uri: http://config-server:8888
 * 
 * # Kafka Configuration (alternative)
 * spring:
 *   kafka:
 *     bootstrap-servers: localhost:9092
 *     consumer:
 *       group-id: ${spring.application.name}
 *   cloud:
 *     bus:
 *       enabled: true
 *       kafka:
 *         binder:
 *           brokers: localhost:9092
 * 
 * management:
 *   endpoints:
 *     web:
 *       exposure:
 *         include: bus-refresh,bus-env,health,info
 * 
 * Trigger Refresh:
 * # Refresh all instances of all services
 * POST http://localhost:8080/actuator/bus-refresh
 * 
 * # Refresh specific service (all instances)
 * POST http://localhost:8080/actuator/bus-refresh?destination=user-service:**
 * 
 * # Refresh specific instance
 * POST http://localhost:8080/actuator/bus-refresh?destination=user-service:8081
 * 
 * # Update environment property
 * POST http://localhost:8080/actuator/bus-env?name=app.feature.new-ui&value=true
 * 
 * Warnings:
 * - Requires message broker infrastructure (RabbitMQ/Kafka)
 * - Network partitions can cause inconsistent state
 * - Large clusters may experience refresh delays
 * - Message broker must be highly available
 * - Refresh is eventually consistent
 * - May cause temporary service degradation during refresh
 * - Monitor message broker health
 * - Implement retry logic for failed refreshes
 * - Consider refresh impact on stateful applications
 * - Test refresh behavior under load
 * 
 * Best Practices:
 * - Use message broker clustering for high availability
 * - Implement health checks for message broker
 * - Monitor refresh event delivery
 * - Log all refresh events for audit
 * - Test refresh in staging before production
 * - Use destination patterns for targeted updates
 * - Enable acknowledgments for tracking
 * - Implement circuit breaker for bus communication
 * - Document refresh procedures
 * - Monitor refresh latency across instances
 */
@SpringBootApplication
public class SpringCloudBusPattern {

    public static void main(String[] args) {
        SpringApplication.run(SpringCloudBusPattern.class, args);
    }

    // ============================================
    // Example 1: Basic Bus-Refreshable Service
    // ============================================
    
    /**
     * Service with @RefreshScope that responds to bus refresh events.
     */
    @Service
    @RefreshScope
    public static class BusRefreshableService {
        
        @Value("${app.message:Default Message}")
        private String message;
        
        @Value("${spring.application.name:unknown}")
        private String applicationName;
        
        private final String instanceId;
        private int refreshCount = 0;
        private LocalDateTime lastRefreshTime;
        
        public BusRefreshableService() {
            this.instanceId = UUID.randomUUID().toString().substring(0, 8);
        }
        
        @PostConstruct
        public void init() {
            refreshCount++;
            lastRefreshTime = LocalDateTime.now();
            System.out.println("[" + applicationName + ":" + instanceId + "] Initialized (refresh #" + refreshCount + ")");
            System.out.println("  Message: " + message);
        }
        
        public Map<String, Object> getInfo() {
            Map<String, Object> info = new LinkedHashMap<>();
            info.put("applicationName", applicationName);
            info.put("instanceId", instanceId);
            info.put("message", message);
            info.put("refreshCount", refreshCount);
            info.put("lastRefreshTime", lastRefreshTime);
            return info;
        }
    }

    // ============================================
    // Example 2: Bus Refresh Event Listener
    // ============================================
    
    /**
     * Listen to refresh events from Spring Cloud Bus.
     * Track refresh history and status.
     */
    @Component
    public static class BusRefreshEventListener {
        
        private final List<RefreshEvent> refreshHistory = new CopyOnWriteArrayList<>();
        
        @Value("${spring.application.name:unknown}")
        private String applicationName;
        
        @EventListener
        public void handleRefreshEvent(RefreshRemoteApplicationEvent event) {
            String originService = event.getOriginService();
            String destinationService = event.getDestinationService();
            String eventId = event.getId();
            
            RefreshEvent refreshEvent = new RefreshEvent(
                eventId,
                originService,
                destinationService,
                LocalDateTime.now()
            );
            
            refreshHistory.add(refreshEvent);
            
            System.out.println("=== Bus Refresh Event Received ===");
            System.out.println("  Event ID: " + eventId);
            System.out.println("  Origin: " + originService);
            System.out.println("  Destination: " + destinationService);
            System.out.println("  Local Service: " + applicationName);
            System.out.println("  Timestamp: " + refreshEvent.timestamp);
            
            // Keep only last 100 events
            if (refreshHistory.size() > 100) {
                refreshHistory.remove(0);
            }
        }
        
        public List<RefreshEvent> getRefreshHistory() {
            return new ArrayList<>(refreshHistory);
        }
        
        public static class RefreshEvent {
            public final String eventId;
            public final String originService;
            public final String destinationService;
            public final LocalDateTime timestamp;
            
            public RefreshEvent(String eventId, String originService, 
                              String destinationService, LocalDateTime timestamp) {
                this.eventId = eventId;
                this.originService = originService;
                this.destinationService = destinationService;
                this.timestamp = timestamp;
            }
        }
    }

    // ============================================
    // Example 3: Cluster-Wide Configuration Manager
    // ============================================
    
    /**
     * Manage configuration refresh across cluster.
     * Track which instances have been refreshed.
     */
    @Service
    public static class ClusterConfigManager {
        
        private final ApplicationEventPublisher eventPublisher;
        private final Map<String, InstanceRefreshStatus> instanceStatuses = new ConcurrentHashMap<>();
        private final AtomicInteger totalRefreshes = new AtomicInteger(0);
        
        @Value("${spring.application.name:app}")
        private String applicationName;
        
        public ClusterConfigManager(ApplicationEventPublisher eventPublisher) {
            this.eventPublisher = eventPublisher;
        }
        
        /**
         * Trigger refresh for all instances of current service.
         */
        public RefreshResult refreshAllInstances() {
            String destination = applicationName + ":**";
            return triggerRefresh(destination, "all-instances");
        }
        
        /**
         * Trigger refresh for specific instance.
         */
        public RefreshResult refreshSpecificInstance(String instanceId) {
            String destination = applicationName + ":" + instanceId;
            return triggerRefresh(destination, "specific-instance");
        }
        
        /**
         * Trigger refresh for all services.
         */
        public RefreshResult refreshAllServices() {
            return triggerRefresh("**", "all-services");
        }
        
        private RefreshResult triggerRefresh(String destination, String scope) {
            String eventId = UUID.randomUUID().toString();
            
            // In real implementation, would publish RefreshRemoteApplicationEvent
            // For demonstration, we'll track the request
            
            totalRefreshes.incrementAndGet();
            
            RefreshResult result = new RefreshResult();
            result.eventId = eventId;
            result.destination = destination;
            result.scope = scope;
            result.timestamp = LocalDateTime.now();
            result.status = "TRIGGERED";
            
            System.out.println("=== Refresh Triggered ===");
            System.out.println("  Event ID: " + eventId);
            System.out.println("  Destination: " + destination);
            System.out.println("  Scope: " + scope);
            System.out.println("  Total Refreshes: " + totalRefreshes.get());
            
            return result;
        }
        
        public void recordInstanceRefresh(String instanceId, boolean success) {
            InstanceRefreshStatus status = instanceStatuses.computeIfAbsent(
                instanceId, 
                k -> new InstanceRefreshStatus(instanceId)
            );
            
            status.recordRefresh(success);
        }
        
        public Map<String, Object> getClusterStatus() {
            Map<String, Object> status = new HashMap<>();
            status.put("totalRefreshes", totalRefreshes.get());
            status.put("trackedInstances", instanceStatuses.size());
            status.put("instances", new ArrayList<>(instanceStatuses.values()));
            return status;
        }
        
        public static class RefreshResult {
            public String eventId;
            public String destination;
            public String scope;
            public LocalDateTime timestamp;
            public String status;
        }
        
        public static class InstanceRefreshStatus {
            public final String instanceId;
            public int successCount = 0;
            public int failureCount = 0;
            public LocalDateTime lastRefreshTime;
            public LocalDateTime firstSeenTime = LocalDateTime.now();
            
            public InstanceRefreshStatus(String instanceId) {
                this.instanceId = instanceId;
            }
            
            public void recordRefresh(boolean success) {
                if (success) {
                    successCount++;
                } else {
                    failureCount++;
                }
                lastRefreshTime = LocalDateTime.now();
            }
        }
    }

    // ============================================
    // Example 4: Multi-Tenant Configuration Refresh
    // ============================================
    
    /**
     * Refresh configuration for specific tenants.
     * Each tenant can have different configuration.
     */
    @Service
    @RefreshScope
    public static class MultiTenantConfigService {
        
        @Value("${app.tenant.default.name:Default Tenant}")
        private String defaultTenantName;
        
        @Value("${app.tenant.default.max-users:100}")
        private int defaultMaxUsers;
        
        private final Map<String, TenantConfig> tenantConfigs = new ConcurrentHashMap<>();
        private int refreshCount = 0;
        
        @PostConstruct
        public void loadTenantConfigs() {
            refreshCount++;
            System.out.println("MultiTenantConfigService refresh #" + refreshCount);
            
            // In real implementation, would load from config server
            tenantConfigs.put("tenant1", new TenantConfig("tenant1", "Tenant One", 50));
            tenantConfigs.put("tenant2", new TenantConfig("tenant2", "Tenant Two", 100));
            tenantConfigs.put("tenant3", new TenantConfig("tenant3", "Tenant Three", 200));
            
            System.out.println("  Loaded configs for " + tenantConfigs.size() + " tenants");
        }
        
        public TenantConfig getTenantConfig(String tenantId) {
            return tenantConfigs.getOrDefault(
                tenantId, 
                new TenantConfig("default", defaultTenantName, defaultMaxUsers)
            );
        }
        
        public Map<String, TenantConfig> getAllTenantConfigs() {
            return new HashMap<>(tenantConfigs);
        }
        
        public static class TenantConfig {
            public final String tenantId;
            public final String name;
            public final int maxUsers;
            
            public TenantConfig(String tenantId, String name, int maxUsers) {
                this.tenantId = tenantId;
                this.name = name;
                this.maxUsers = maxUsers;
            }
        }
    }

    // ============================================
    // Example 5: Refresh Acknowledgment Tracker
    // ============================================
    
    /**
     * Track acknowledgments from instances that completed refresh.
     */
    @Component
    public static class RefreshAckTracker implements ApplicationListener<Object> {
        
        private final List<AckEvent> ackHistory = new CopyOnWriteArrayList<>();
        
        @Override
        public void onApplicationEvent(Object event) {
            // In real implementation, would listen to AckRemoteApplicationEvent
            // For demonstration, we track generic events
            
            if (event.getClass().getSimpleName().contains("Ack")) {
                AckEvent ackEvent = new AckEvent(
                    event.getClass().getSimpleName(),
                    LocalDateTime.now()
                );
                
                ackHistory.add(ackEvent);
                
                System.out.println("=== Refresh Acknowledgment ===");
                System.out.println("  Event Type: " + ackEvent.eventType);
                System.out.println("  Timestamp: " + ackEvent.timestamp);
                
                // Keep only last 100 acks
                if (ackHistory.size() > 100) {
                    ackHistory.remove(0);
                }
            }
        }
        
        public List<AckEvent> getAckHistory() {
            return new ArrayList<>(ackHistory);
        }
        
        public static class AckEvent {
            public final String eventType;
            public final LocalDateTime timestamp;
            
            public AckEvent(String eventType, LocalDateTime timestamp) {
                this.eventType = eventType;
                this.timestamp = timestamp;
            }
        }
    }

    // ============================================
    // Example 6: Environment-Specific Refresh
    // ============================================
    
    /**
     * Conditional refresh based on environment (dev, staging, prod).
     */
    @Service
    @RefreshScope
    public static class EnvironmentAwareConfig {
        
        @Value("${spring.profiles.active:default}")
        private String activeProfile;
        
        @Value("${app.env.refresh-enabled:true}")
        private boolean refreshEnabled;
        
        @Value("${app.env.feature-flags-enabled:false}")
        private boolean featureFlagsEnabled;
        
        private final String instanceId = UUID.randomUUID().toString().substring(0, 8);
        private int refreshCount = 0;
        
        @PostConstruct
        public void init() {
            refreshCount++;
            
            if (!refreshEnabled) {
                System.out.println("Refresh is DISABLED for environment: " + activeProfile);
                return;
            }
            
            System.out.println("EnvironmentAwareConfig initialized:");
            System.out.println("  Instance ID: " + instanceId);
            System.out.println("  Active Profile: " + activeProfile);
            System.out.println("  Refresh Count: " + refreshCount);
            System.out.println("  Refresh Enabled: " + refreshEnabled);
            System.out.println("  Feature Flags: " + featureFlagsEnabled);
        }
        
        public Map<String, Object> getConfig() {
            Map<String, Object> config = new HashMap<>();
            config.put("instanceId", instanceId);
            config.put("activeProfile", activeProfile);
            config.put("refreshCount", refreshCount);
            config.put("refreshEnabled", refreshEnabled);
            config.put("featureFlagsEnabled", featureFlagsEnabled);
            return config;
        }
    }

    // ============================================
    // Example 7: Bus Refresh Controller
    // ============================================
    
    /**
     * REST endpoints to trigger and monitor bus refresh.
     */
    @RestController
    @RequestMapping("/bus")
    public static class BusRefreshController {
        
        private final BusRefreshableService refreshableService;
        private final BusRefreshEventListener eventListener;
        private final ClusterConfigManager clusterManager;
        private final MultiTenantConfigService tenantService;
        private final RefreshAckTracker ackTracker;
        private final EnvironmentAwareConfig envConfig;
        
        public BusRefreshController(
                BusRefreshableService refreshableService,
                BusRefreshEventListener eventListener,
                ClusterConfigManager clusterManager,
                MultiTenantConfigService tenantService,
                RefreshAckTracker ackTracker,
                EnvironmentAwareConfig envConfig) {
            this.refreshableService = refreshableService;
            this.eventListener = eventListener;
            this.clusterManager = clusterManager;
            this.tenantService = tenantService;
            this.ackTracker = ackTracker;
            this.envConfig = envConfig;
        }
        
        @GetMapping("/info")
        public Map<String, Object> getInfo() {
            return refreshableService.getInfo();
        }
        
        @GetMapping("/refresh-history")
        public List<BusRefreshEventListener.RefreshEvent> getRefreshHistory() {
            return eventListener.getRefreshHistory();
        }
        
        @PostMapping("/refresh/all-instances")
        public ClusterConfigManager.RefreshResult refreshAllInstances() {
            return clusterManager.refreshAllInstances();
        }
        
        @PostMapping("/refresh/instance/{instanceId}")
        public ClusterConfigManager.RefreshResult refreshInstance(@PathVariable String instanceId) {
            return clusterManager.refreshSpecificInstance(instanceId);
        }
        
        @PostMapping("/refresh/all-services")
        public ClusterConfigManager.RefreshResult refreshAllServices() {
            return clusterManager.refreshAllServices();
        }
        
        @GetMapping("/cluster-status")
        public Map<String, Object> getClusterStatus() {
            return clusterManager.getClusterStatus();
        }
        
        @GetMapping("/tenants")
        public Map<String, MultiTenantConfigService.TenantConfig> getTenants() {
            return tenantService.getAllTenantConfigs();
        }
        
        @GetMapping("/tenants/{tenantId}")
        public MultiTenantConfigService.TenantConfig getTenant(@PathVariable String tenantId) {
            return tenantService.getTenantConfig(tenantId);
        }
        
        @GetMapping("/ack-history")
        public List<RefreshAckTracker.AckEvent> getAckHistory() {
            return ackTracker.getAckHistory();
        }
        
        @GetMapping("/environment")
        public Map<String, Object> getEnvironmentConfig() {
            return envConfig.getConfig();
        }
    }
}
