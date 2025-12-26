package com.example.demo.patterns.metrics;

import io.micrometer.core.instrument.*;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Meter Registry Pattern - Centralized Metrics Registry
 * 
 * Purpose:
 * - Central registry for all application meters
 * - Configure common tags across all metrics
 * - Support multiple backend registries (Prometheus, Graphite, InfluxDB)
 * - Apply global meter filters
 * - Enable/disable specific metrics
 * - Export metrics to multiple monitoring systems
 * 
 * Use Cases:
 * - Multi-backend metric export
 * - Common tagging strategy (application, environment, region)
 * - Meter discovery and inspection
 * - Dynamic meter configuration
 * - Metric filtering and transformation
 * - Test metrics with SimpleMeterRegistry
 * - Composite registries for multi-backend
 * 
 * Registry Types:
 * - SimpleMeterRegistry: In-memory for testing
 * - PrometheusMeterRegistry: Prometheus pull model
 * - GraphiteMeterRegistry: Graphite push model
 * - InfluxMeterRegistry: InfluxDB time-series
 * - JmxMeterRegistry: JMX export
 * - CompositeMeterRegistry: Multiple backends
 * 
 * Configuration (application.yml):
 * management:
 *   metrics:
 *     tags:
 *       application: ${spring.application.name}
 *       environment: ${spring.profiles.active}
 *       region: us-east-1
 *       instance: ${HOSTNAME:localhost}
 *     enable:
 *       jvm: true
 *       process: true
 *       system: true
 *     export:
 *       prometheus:
 *         enabled: true
 *       graphite:
 *         enabled: false
 * 
 * Dependencies:
 * <dependency>
 *     <groupId>org.springframework.boot</groupId>
 *     <artifactId>spring-boot-starter-actuator</artifactId>
 * </dependency>
 * <dependency>
 *     <groupId>io.micrometer</groupId>
 *     <artifactId>micrometer-core</artifactId>
 * </dependency>
 * <!-- For specific registries -->
 * <dependency>
 *     <groupId>io.micrometer</groupId>
 *     <artifactId>micrometer-registry-prometheus</artifactId>
 * </dependency>
 * 
 * Warnings:
 * - Too many common tags impact performance
 * - Registry customizers apply globally
 * - Be careful with meter filters (can hide metrics)
 * - Composite registries duplicate storage
 * - High cardinality tags cause memory issues
 * 
 * Best Practices:
 * - Use common tags for filtering/grouping
 * - Keep common tags low cardinality
 * - Apply consistent naming conventions
 * - Use meter filters to control what's exported
 * - Test with SimpleMeterRegistry
 * - Document tagging strategy
 * - Monitor registry memory usage
 * - Use composite registries for multi-backend
 */
@SpringBootApplication
public class MeterRegistryPattern {

    public static void main(String[] args) {
        SpringApplication.run(MeterRegistryPattern.class, args);
    }

    // ============================================
    // Example 1: Basic Registry Customization
    // ============================================
    
    @Configuration
    public static class BasicRegistryConfiguration {
        
        @Bean
        public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
            return registry -> {
                // Add common tags to all metrics
                registry.config()
                    .commonTags("application", "demo-app")
                    .commonTags("environment", "production")
                    .commonTags("region", "us-east-1")
                    .commonTags("datacenter", "dc1")
                    .commonTags("version", "1.0.0");
                
                System.out.println("Common tags configured");
            };
        }
    }

    // ============================================
    // Example 2: Registry Inspection Service
    // ============================================
    
    @Service
    public static class RegistryInspectionService {
        
        private final MeterRegistry registry;
        
        public RegistryInspectionService(MeterRegistry registry) {
            this.registry = registry;
        }
        
        public List<String> getAllMeterNames() {
            return registry.getMeters().stream()
                .map(meter -> meter.getId().getName())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        }
        
        public List<Map<String, Object>> findMeters(String namePrefix) {
            return registry.getMeters().stream()
                .filter(meter -> meter.getId().getName().startsWith(namePrefix))
                .map(this::meterToMap)
                .collect(Collectors.toList());
        }
        
        public Map<String, Object> getMeterDetails(String name) {
            Meter meter = registry.find(name).meter();
            if (meter == null) {
                return Collections.singletonMap("error", "Meter not found");
            }
            return meterToMap(meter);
        }
        
        public List<Map<String, Object>> getMetersByTag(String tagKey, String tagValue) {
            return registry.getMeters().stream()
                .filter(meter -> meter.getId().getTags().stream()
                    .anyMatch(tag -> tag.getKey().equals(tagKey) && tag.getValue().equals(tagValue)))
                .map(this::meterToMap)
                .collect(Collectors.toList());
        }
        
        public Map<String, Long> getMeterCounts() {
            Map<String, Long> counts = new HashMap<>();
            
            counts.put("counter", registry.getMeters().stream()
                .filter(m -> m instanceof Counter)
                .count());
            
            counts.put("gauge", registry.getMeters().stream()
                .filter(m -> m instanceof Gauge)
                .count());
            
            counts.put("timer", registry.getMeters().stream()
                .filter(m -> m instanceof Timer)
                .count());
            
            counts.put("distribution_summary", registry.getMeters().stream()
                .filter(m -> m instanceof DistributionSummary)
                .count());
            
            counts.put("long_task_timer", registry.getMeters().stream()
                .filter(m -> m instanceof LongTaskTimer)
                .count());
            
            counts.put("total", (long) registry.getMeters().size());
            
            return counts;
        }
        
        private Map<String, Object> meterToMap(Meter meter) {
            Map<String, Object> map = new HashMap<>();
            map.put("name", meter.getId().getName());
            map.put("type", meter.getId().getType().toString());
            map.put("description", meter.getId().getDescription());
            map.put("baseUnit", meter.getId().getBaseUnit());
            
            Map<String, String> tags = new HashMap<>();
            for (Tag tag : meter.getId().getTags()) {
                tags.put(tag.getKey(), tag.getValue());
            }
            map.put("tags", tags);
            
            // Add measurements
            List<Map<String, Object>> measurements = new ArrayList<>();
            for (Measurement measurement : meter.measure()) {
                Map<String, Object> m = new HashMap<>();
                m.put("statistic", measurement.getStatistic().toString());
                m.put("value", measurement.getValue());
                measurements.add(m);
            }
            map.put("measurements", measurements);
            
            return map;
        }
    }

    // ============================================
    // Example 3: Dynamic Meter Registration
    // ============================================
    
    @Service
    public static class DynamicMeterService {
        
        private final MeterRegistry registry;
        private final Map<String, Counter> dynamicCounters = new HashMap<>();
        private final Map<String, Timer> dynamicTimers = new HashMap<>();
        
        public DynamicMeterService(MeterRegistry registry) {
            this.registry = registry;
        }
        
        public void registerCounter(String name, String description, Map<String, String> tags) {
            if (dynamicCounters.containsKey(name)) {
                System.out.println("Counter already registered: " + name);
                return;
            }
            
            Counter.Builder builder = Counter.builder(name)
                .description(description);
            
            for (Map.Entry<String, String> entry : tags.entrySet()) {
                builder.tag(entry.getKey(), entry.getValue());
            }
            
            Counter counter = builder.register(registry);
            dynamicCounters.put(name, counter);
            
            System.out.println("Counter registered: " + name);
        }
        
        public void incrementCounter(String name, double amount) {
            Counter counter = dynamicCounters.get(name);
            if (counter != null) {
                counter.increment(amount);
            } else {
                System.out.println("Counter not found: " + name);
            }
        }
        
        public void registerTimer(String name, String description, Map<String, String> tags) {
            if (dynamicTimers.containsKey(name)) {
                System.out.println("Timer already registered: " + name);
                return;
            }
            
            Timer.Builder builder = Timer.builder(name)
                .description(description);
            
            for (Map.Entry<String, String> entry : tags.entrySet()) {
                builder.tag(entry.getKey(), entry.getValue());
            }
            
            Timer timer = builder.register(registry);
            dynamicTimers.put(name, timer);
            
            System.out.println("Timer registered: " + name);
        }
        
        public void removeMeter(String name) {
            Meter meter = registry.find(name).meter();
            if (meter != null) {
                registry.remove(meter);
                dynamicCounters.remove(name);
                dynamicTimers.remove(name);
                System.out.println("Meter removed: " + name);
            }
        }
        
        public List<String> getDynamicMeterNames() {
            List<String> names = new ArrayList<>();
            names.addAll(dynamicCounters.keySet());
            names.addAll(dynamicTimers.keySet());
            return names;
        }
    }

    // ============================================
    // Example 4: Composite Registry (Multi-Backend)
    // ============================================
    
    @Service
    public static class CompositeRegistryService {
        
        private final CompositeMeterRegistry compositeMeterRegistry;
        private final SimpleMeterRegistry simpleRegistry;
        
        public CompositeRegistryService() {
            this.compositeMeterRegistry = new CompositeMeterRegistry();
            this.simpleRegistry = new SimpleMeterRegistry();
            
            // Add simple registry to composite
            compositeMeterRegistry.add(simpleRegistry);
            
            // Add common tags
            compositeMeterRegistry.config()
                .commonTags("application", "multi-backend-app")
                .commonTags("environment", "test");
            
            System.out.println("Composite registry configured");
        }
        
        public void addRegistry(MeterRegistry registry) {
            compositeMeterRegistry.add(registry);
            System.out.println("Registry added to composite");
        }
        
        public void removeRegistry(MeterRegistry registry) {
            compositeMeterRegistry.remove(registry);
            System.out.println("Registry removed from composite");
        }
        
        public Set<MeterRegistry> getRegistries() {
            return compositeMeterRegistry.getRegistries();
        }
        
        public void recordMetric(String name, double value) {
            // Metric will be recorded in all registered backends
            Counter.builder(name)
                .description("Composite counter")
                .register(compositeMeterRegistry)
                .increment(value);
        }
        
        public Map<String, Object> getCompositeStats() {
            Map<String, Object> stats = new HashMap<>();
            stats.put("registry_count", compositeMeterRegistry.getRegistries().size());
            stats.put("meter_count", compositeMeterRegistry.getMeters().size());
            stats.put("registry_types", compositeMeterRegistry.getRegistries().stream()
                .map(r -> r.getClass().getSimpleName())
                .collect(Collectors.toList()));
            return stats;
        }
    }

    // ============================================
    // Example 5: Registry Configuration Service
    // ============================================
    
    @Service
    public static class RegistryConfigurationService {
        
        private final MeterRegistry registry;
        
        public RegistryConfigurationService(MeterRegistry registry) {
            this.registry = registry;
        }
        
        public void applyCommonTags(Map<String, String> tags) {
            for (Map.Entry<String, String> entry : tags.entrySet()) {
                registry.config().commonTags(entry.getKey(), entry.getValue());
            }
            System.out.println("Common tags applied: " + tags);
        }
        
        public List<Map<String, String>> getCommonTags() {
            List<Map<String, String>> tags = new ArrayList<>();
            
            // Common tags are applied to all meters
            // Sample from first meter if available
            if (!registry.getMeters().isEmpty()) {
                Meter meter = registry.getMeters().iterator().next();
                for (Tag tag : meter.getId().getTags()) {
                    Map<String, String> tagMap = new HashMap<>();
                    tagMap.put("key", tag.getKey());
                    tagMap.put("value", tag.getValue());
                    tags.add(tagMap);
                }
            }
            
            return tags;
        }
        
        public void enableMeterIdempotency(boolean enable) {
            // Meter ID prefix can help with idempotency
            if (enable) {
                registry.config().commonTags("idempotency", "enabled");
            }
        }
        
        public Map<String, Object> getRegistryConfig() {
            Map<String, Object> config = new HashMap<>();
            config.put("registry_class", registry.getClass().getSimpleName());
            config.put("total_meters", registry.getMeters().size());
            config.put("clock", registry.config().clock().getClass().getSimpleName());
            return config;
        }
    }

    // ============================================
    // Example 6: Meter Search Service
    // ============================================
    
    @Service
    public static class MeterSearchService {
        
        private final MeterRegistry registry;
        
        public MeterSearchService(MeterRegistry registry) {
            this.registry = registry;
        }
        
        public Optional<Counter> findCounter(String name) {
            return Optional.ofNullable(registry.find(name).counter());
        }
        
        public Optional<Gauge> findGauge(String name) {
            return Optional.ofNullable(registry.find(name).gauge());
        }
        
        public Optional<Timer> findTimer(String name) {
            return Optional.ofNullable(registry.find(name).timer());
        }
        
        public List<Counter> findCounters(String namePrefix) {
            return registry.getMeters().stream()
                .filter(m -> m instanceof Counter)
                .filter(m -> m.getId().getName().startsWith(namePrefix))
                .map(m -> (Counter) m)
                .collect(Collectors.toList());
        }
        
        public List<Timer> findTimersWithTag(String tagKey, String tagValue) {
            return registry.getMeters().stream()
                .filter(m -> m instanceof Timer)
                .filter(m -> m.getId().getTags().stream()
                    .anyMatch(tag -> tag.getKey().equals(tagKey) && tag.getValue().equals(tagValue)))
                .map(m -> (Timer) m)
                .collect(Collectors.toList());
        }
        
        public Map<String, Object> searchMeters(String keyword) {
            Map<String, Object> results = new HashMap<>();
            
            List<String> matchingNames = registry.getMeters().stream()
                .map(m -> m.getId().getName())
                .filter(name -> name.toLowerCase().contains(keyword.toLowerCase()))
                .distinct()
                .sorted()
                .collect(Collectors.toList());
            
            results.put("keyword", keyword);
            results.put("match_count", matchingNames.size());
            results.put("matching_meters", matchingNames);
            
            return results;
        }
    }

    // ============================================
    // Example 7: Meter Lifecycle Service
    // ============================================
    
    @Service
    public static class MeterLifecycleService {
        
        private final MeterRegistry registry;
        private final Map<String, Meter> managedMeters = new HashMap<>();
        
        public MeterLifecycleService(MeterRegistry registry) {
            this.registry = registry;
        }
        
        public void createMeter(String name, String type, Map<String, String> tags) {
            Meter meter;
            
            switch (type.toLowerCase()) {
                case "counter":
                    Counter.Builder counterBuilder = Counter.builder(name)
                        .description("Managed counter: " + name);
                    for (Map.Entry<String, String> entry : tags.entrySet()) {
                        counterBuilder.tag(entry.getKey(), entry.getValue());
                    }
                    meter = counterBuilder.register(registry);
                    break;
                    
                case "timer":
                    Timer.Builder timerBuilder = Timer.builder(name)
                        .description("Managed timer: " + name);
                    for (Map.Entry<String, String> entry : tags.entrySet()) {
                        timerBuilder.tag(entry.getKey(), entry.getValue());
                    }
                    meter = timerBuilder.register(registry);
                    break;
                    
                default:
                    throw new IllegalArgumentException("Unsupported meter type: " + type);
            }
            
            managedMeters.put(name, meter);
            System.out.println("Meter created: " + name + " (type: " + type + ")");
        }
        
        public void removeMeter(String name) {
            Meter meter = managedMeters.remove(name);
            if (meter != null) {
                registry.remove(meter);
                System.out.println("Meter removed: " + name);
            }
        }
        
        public void removeAllManagedMeters() {
            for (Meter meter : managedMeters.values()) {
                registry.remove(meter);
            }
            managedMeters.clear();
            System.out.println("All managed meters removed");
        }
        
        public List<String> getManagedMeterNames() {
            return new ArrayList<>(managedMeters.keySet());
        }
        
        public boolean isMeterManaged(String name) {
            return managedMeters.containsKey(name);
        }
    }

    // ============================================
    // Example 8: Meter Registry REST Controller
    // ============================================
    
    @RestController
    @RequestMapping("/api/registry")
    public static class MeterRegistryController {
        
        private final RegistryInspectionService inspectionService;
        private final DynamicMeterService dynamicService;
        private final CompositeRegistryService compositeService;
        private final RegistryConfigurationService configService;
        private final MeterSearchService searchService;
        private final MeterLifecycleService lifecycleService;
        
        public MeterRegistryController(
                RegistryInspectionService inspectionService,
                DynamicMeterService dynamicService,
                CompositeRegistryService compositeService,
                RegistryConfigurationService configService,
                MeterSearchService searchService,
                MeterLifecycleService lifecycleService) {
            this.inspectionService = inspectionService;
            this.dynamicService = dynamicService;
            this.compositeService = compositeService;
            this.configService = configService;
            this.searchService = searchService;
            this.lifecycleService = lifecycleService;
        }
        
        @GetMapping("/meters")
        public List<String> getAllMeters() {
            return inspectionService.getAllMeterNames();
        }
        
        @GetMapping("/meters/prefix/{prefix}")
        public List<Map<String, Object>> findByPrefix(@PathVariable String prefix) {
            return inspectionService.findMeters(prefix);
        }
        
        @GetMapping("/meters/{name}")
        public Map<String, Object> getMeterDetails(@PathVariable String name) {
            return inspectionService.getMeterDetails(name);
        }
        
        @GetMapping("/meters/tag/{key}/{value}")
        public List<Map<String, Object>> findByTag(
                @PathVariable String key,
                @PathVariable String value) {
            return inspectionService.getMetersByTag(key, value);
        }
        
        @GetMapping("/meters/counts")
        public Map<String, Long> getMeterCounts() {
            return inspectionService.getMeterCounts();
        }
        
        @PostMapping("/meters/counter")
        public Map<String, String> registerCounter(
                @RequestParam String name,
                @RequestParam String description,
                @RequestParam Map<String, String> tags) {
            dynamicService.registerCounter(name, description, tags);
            return Collections.singletonMap("status", "registered");
        }
        
        @PostMapping("/meters/timer")
        public Map<String, String> registerTimer(
                @RequestParam String name,
                @RequestParam String description,
                @RequestParam Map<String, String> tags) {
            dynamicService.registerTimer(name, description, tags);
            return Collections.singletonMap("status", "registered");
        }
        
        @PostMapping("/meters/counter/{name}/increment")
        public Map<String, String> incrementCounter(
                @PathVariable String name,
                @RequestParam(defaultValue = "1.0") double amount) {
            dynamicService.incrementCounter(name, amount);
            return Collections.singletonMap("status", "incremented");
        }
        
        @DeleteMapping("/meters/{name}")
        public Map<String, String> removeMeter(@PathVariable String name) {
            dynamicService.removeMeter(name);
            return Collections.singletonMap("status", "removed");
        }
        
        @GetMapping("/composite/stats")
        public Map<String, Object> getCompositeStats() {
            return compositeService.getCompositeStats();
        }
        
        @PostMapping("/config/tags")
        public Map<String, String> applyCommonTags(@RequestBody Map<String, String> tags) {
            configService.applyCommonTags(tags);
            return Collections.singletonMap("status", "applied");
        }
        
        @GetMapping("/config/tags")
        public List<Map<String, String>> getCommonTags() {
            return configService.getCommonTags();
        }
        
        @GetMapping("/config")
        public Map<String, Object> getRegistryConfig() {
            return configService.getRegistryConfig();
        }
        
        @GetMapping("/search")
        public Map<String, Object> searchMeters(@RequestParam String keyword) {
            return searchService.searchMeters(keyword);
        }
        
        @PostMapping("/lifecycle/create")
        public Map<String, String> createMeter(
                @RequestParam String name,
                @RequestParam String type,
                @RequestParam Map<String, String> tags) {
            lifecycleService.createMeter(name, type, tags);
            return Collections.singletonMap("status", "created");
        }
        
        @DeleteMapping("/lifecycle/{name}")
        public Map<String, String> removeManagedMeter(@PathVariable String name) {
            lifecycleService.removeMeter(name);
            return Collections.singletonMap("status", "removed");
        }
        
        @GetMapping("/lifecycle/managed")
        public List<String> getManagedMeters() {
            return lifecycleService.getManagedMeterNames();
        }
    }
}
