package com.example.demo.patterns.metrics;

import io.micrometer.core.instrument.*;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.config.MeterFilterReply;
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.*;
import java.util.function.Predicate;

/**
 * Meter Filter Pattern - Filter and Transform Metrics
 * 
 * Purpose:
 * - Accept or deny meters based on criteria
 * - Transform meter names and tags
 * - Configure distribution statistics
 * - Apply rate limits to metrics
 * - Add common tags conditionally
 * - Rename metrics for consistency
 * - Control which metrics get exported
 * 
 * Use Cases:
 * - Block noisy metrics from export
 * - Rename metrics for consistency
 * - Add environment-specific tags
 * - Configure histograms/percentiles
 * - Limit high-cardinality metrics
 * - Filter by metric name prefix
 * - Transform tag values
 * - Apply backend-specific filters
 * 
 * Filter Types:
 * - Accept/Deny: Control which meters are registered
 * - Transform: Modify meter IDs (name, tags)
 * - Configure: Set distribution config
 * - Rate Limit: Limit updates per second
 * - Common Tags: Add tags to specific meters
 * 
 * Configuration (application.yml):
 * management:
 *   metrics:
 *     enable:
 *       jvm: true
 *       process: false  # Filter out process metrics
 *     tags:
 *       application: ${spring.application.name}
 * 
 * Filter Order:
 * 1. Deny/Accept filters (short-circuit)
 * 2. Transform filters (modify ID)
 * 3. Configure filters (distribution config)
 * 4. Common tags (add tags)
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
 * 
 * Warnings:
 * - Filters are applied in registration order
 * - Deny filters short-circuit (no further processing)
 * - Too many filters impact performance
 * - Transform filters cannot change meter type
 * - Be careful with wildcard patterns
 * - Test filters thoroughly
 * 
 * Best Practices:
 * - Use deny filters for noisy metrics
 * - Apply common tags early
 * - Use transform for name normalization
 * - Configure distributions for timers
 * - Document filter rules
 * - Monitor filter impact
 * - Use specific patterns (avoid ".*")
 * - Test filter behavior
 */
@SpringBootApplication
public class MeterFilterPattern {

    public static void main(String[] args) {
        SpringApplication.run(MeterFilterPattern.class, args);
    }

    // ============================================
    // Example 1: Accept/Deny Filters
    // ============================================
    
    @Configuration
    public static class AcceptDenyFilterConfiguration {
        
        @Bean
        public MeterRegistryCustomizer<MeterRegistry> denyFilters() {
            return registry -> {
                // Deny specific metrics by name
                registry.config().meterFilter(
                    MeterFilter.deny(id -> 
                        id.getName().startsWith("jvm.buffer"))
                );
                
                // Deny metrics with specific tags
                registry.config().meterFilter(
                    MeterFilter.deny(id -> 
                        id.getTags().stream()
                            .anyMatch(tag -> tag.getKey().equals("internal") && 
                                           tag.getValue().equals("true")))
                );
                
                // Only accept specific metric prefixes
                registry.config().meterFilter(
                    MeterFilter.accept(id -> 
                        id.getName().startsWith("http.") ||
                        id.getName().startsWith("db.") ||
                        id.getName().startsWith("api."))
                );
                
                System.out.println("Accept/Deny filters configured");
            };
        }
    }

    // ============================================
    // Example 2: Name and Tag Transformation
    // ============================================
    
    @Configuration
    public static class TransformFilterConfiguration {
        
        @Bean
        public MeterRegistryCustomizer<MeterRegistry> transformFilters() {
            return registry -> {
                // Rename metric (add prefix)
                registry.config().meterFilter(
                    MeterFilter.renameTag("http.server.requests", "uri", "endpoint")
                );
                
                // Transform tag values (lowercase)
                registry.config().meterFilter(new MeterFilter() {
                    @Override
                    public Meter.Id map(Meter.Id id) {
                        if (id.getName().startsWith("http.")) {
                            List<Tag> transformedTags = new ArrayList<>();
                            for (Tag tag : id.getTags()) {
                                if (tag.getKey().equals("method")) {
                                    transformedTags.add(Tag.of(tag.getKey(), 
                                        tag.getValue().toUpperCase()));
                                } else {
                                    transformedTags.add(tag);
                                }
                            }
                            return id.replaceTags(transformedTags);
                        }
                        return id;
                    }
                });
                
                // Add prefix to metric names
                registry.config().meterFilter(new MeterFilter() {
                    @Override
                    public Meter.Id map(Meter.Id id) {
                        if (id.getName().startsWith("custom.")) {
                            return id.withName("app." + id.getName());
                        }
                        return id;
                    }
                });
                
                System.out.println("Transform filters configured");
            };
        }
    }

    // ============================================
    // Example 3: Distribution Configuration
    // ============================================
    
    @Configuration
    public static class DistributionFilterConfiguration {
        
        @Bean
        public MeterRegistryCustomizer<MeterRegistry> distributionFilters() {
            return registry -> {
                // Configure histogram for HTTP requests
                registry.config().meterFilter(new MeterFilter() {
                    @Override
                    public DistributionStatisticConfig configure(Meter.Id id, 
                                                                 DistributionStatisticConfig config) {
                        if (id.getName().startsWith("http.server.requests")) {
                            return DistributionStatisticConfig.builder()
                                .percentilesHistogram(true)
                                .percentiles(0.5, 0.95, 0.99)
                                .serviceLevelObjectives(
                                    Duration.ofMillis(50).toNanos(),
                                    Duration.ofMillis(100).toNanos(),
                                    Duration.ofMillis(200).toNanos(),
                                    Duration.ofMillis(500).toNanos()
                                )
                                .minimumExpectedValue(Duration.ofMillis(1).toNanos())
                                .maximumExpectedValue(Duration.ofSeconds(10).toNanos())
                                .build()
                                .merge(config);
                        }
                        return config;
                    }
                });
                
                // Configure histogram for database queries
                registry.config().meterFilter(new MeterFilter() {
                    @Override
                    public DistributionStatisticConfig configure(Meter.Id id, 
                                                                 DistributionStatisticConfig config) {
                        if (id.getName().equals("db.query.duration")) {
                            return DistributionStatisticConfig.builder()
                                .percentiles(0.5, 0.95, 0.99, 0.999)
                                .serviceLevelObjectives(
                                    Duration.ofMillis(10).toNanos(),
                                    Duration.ofMillis(50).toNanos(),
                                    Duration.ofMillis(100).toNanos()
                                )
                                .build()
                                .merge(config);
                        }
                        return config;
                    }
                });
                
                System.out.println("Distribution filters configured");
            };
        }
    }

    // ============================================
    // Example 4: Common Tag Filters
    // ============================================
    
    @Configuration
    public static class CommonTagFilterConfiguration {
        
        @Bean
        public MeterRegistryCustomizer<MeterRegistry> commonTagFilters() {
            return registry -> {
                // Add tags to specific metrics
                registry.config().meterFilter(
                    MeterFilter.commonTags("application", "demo-app")
                );
                
                // Add environment tag
                String environment = System.getProperty("spring.profiles.active", "dev");
                registry.config().meterFilter(
                    MeterFilter.commonTags("environment", environment)
                );
                
                // Add region tag
                registry.config().meterFilter(
                    MeterFilter.commonTags("region", "us-east-1")
                );
                
                // Add custom tags conditionally
                registry.config().meterFilter(new MeterFilter() {
                    @Override
                    public Meter.Id map(Meter.Id id) {
                        if (id.getName().startsWith("api.")) {
                            return id.withTag(Tag.of("layer", "api"));
                        } else if (id.getName().startsWith("db.")) {
                            return id.withTag(Tag.of("layer", "data"));
                        }
                        return id;
                    }
                });
                
                System.out.println("Common tag filters configured");
            };
        }
    }

    // ============================================
    // Example 5: Custom Filter Service
    // ============================================
    
    @Service
    public static class CustomFilterService {
        
        // Filter to limit high-cardinality metrics
        public static class CardinalityLimitFilter implements MeterFilter {
            
            private final int maxTags;
            
            public CardinalityLimitFilter(int maxTags) {
                this.maxTags = maxTags;
            }
            
            @Override
            public MeterFilterReply accept(Meter.Id id) {
                if (id.getTags().size() > maxTags) {
                    System.out.println("DENIED (too many tags): " + id.getName() + 
                        " (" + id.getTags().size() + " tags)");
                    return MeterFilterReply.DENY;
                }
                return MeterFilterReply.NEUTRAL;
            }
        }
        
        // Filter to add timestamp tag
        public static class TimestampTagFilter implements MeterFilter {
            
            @Override
            public Meter.Id map(Meter.Id id) {
                if (id.getName().startsWith("custom.")) {
                    return id.withTag(Tag.of("created_at", 
                        String.valueOf(System.currentTimeMillis())));
                }
                return id;
            }
        }
        
        // Filter to normalize metric names
        public static class NormalizationFilter implements MeterFilter {
            
            @Override
            public Meter.Id map(Meter.Id id) {
                String normalized = id.getName()
                    .toLowerCase()
                    .replace('-', '.')
                    .replace('_', '.');
                return id.withName(normalized);
            }
        }
        
        // Filter based on environment
        public static class EnvironmentFilter implements MeterFilter {
            
            private final String environment;
            
            public EnvironmentFilter(String environment) {
                this.environment = environment;
            }
            
            @Override
            public MeterFilterReply accept(Meter.Id id) {
                // In production, deny debug metrics
                if ("production".equals(environment) && 
                    id.getName().contains("debug")) {
                    return MeterFilterReply.DENY;
                }
                
                // In development, allow all metrics
                if ("development".equals(environment)) {
                    return MeterFilterReply.ACCEPT;
                }
                
                return MeterFilterReply.NEUTRAL;
            }
        }
    }

    // ============================================
    // Example 6: Dynamic Filter Management
    // ============================================
    
    @Service
    public static class DynamicFilterService {
        
        private final MeterRegistry registry;
        private final List<MeterFilter> activeFilters = new ArrayList<>();
        
        public DynamicFilterService(MeterRegistry registry) {
            this.registry = registry;
        }
        
        public void addDenyFilter(String namePrefix) {
            MeterFilter filter = MeterFilter.denyNameStartsWith(namePrefix);
            registry.config().meterFilter(filter);
            activeFilters.add(filter);
            System.out.println("Deny filter added for: " + namePrefix);
        }
        
        public void addAcceptFilter(String namePrefix) {
            MeterFilter filter = MeterFilter.acceptNameStartsWith(namePrefix);
            registry.config().meterFilter(filter);
            activeFilters.add(filter);
            System.out.println("Accept filter added for: " + namePrefix);
        }
        
        public void addMaximumAllowableMetrics(int count) {
            MeterFilter filter = MeterFilter.maximumAllowableMetrics(count);
            registry.config().meterFilter(filter);
            activeFilters.add(filter);
            System.out.println("Maximum allowable metrics set to: " + count);
        }
        
        public void addCommonTag(String key, String value) {
            MeterFilter filter = MeterFilter.commonTags(Arrays.asList(Tag.of(key, value)));
            registry.config().meterFilter(filter);
            activeFilters.add(filter);
            System.out.println("Common tag added: " + key + "=" + value);
        }
        
        public void addIgnoreTagFilter(String... tagKeys) {
            MeterFilter filter = MeterFilter.ignoreTags(tagKeys);
            registry.config().meterFilter(filter);
            activeFilters.add(filter);
            System.out.println("Ignoring tags: " + String.join(", ", tagKeys));
        }
        
        public void replaceTagValues(String tagKey, String oldValue, String newValue) {
            MeterFilter filter = MeterFilter.replaceTagValues(tagKey, 
                s -> s.equals(oldValue) ? newValue : s);
            registry.config().meterFilter(filter);
            activeFilters.add(filter);
            System.out.println("Tag replacement added: " + tagKey + 
                " (" + oldValue + " -> " + newValue + ")");
        }
        
        public int getActiveFilterCount() {
            return activeFilters.size();
        }
    }

    // ============================================
    // Example 7: Filter Statistics Service
    // ============================================
    
    @Service
    public static class FilterStatisticsService {
        
        private final MeterRegistry registry;
        private int deniedCount = 0;
        private int acceptedCount = 0;
        private final List<String> deniedMetrics = new ArrayList<>();
        
        public FilterStatisticsService(MeterRegistry registry) {
            this.registry = registry;
            
            // Add monitoring filter
            registry.config().meterFilter(new MeterFilter() {
                @Override
                public MeterFilterReply accept(Meter.Id id) {
                    acceptedCount++;
                    return MeterFilterReply.NEUTRAL;
                }
            });
        }
        
        public void recordDenial(String metricName) {
            deniedCount++;
            deniedMetrics.add(metricName);
            if (deniedMetrics.size() > 100) {
                deniedMetrics.remove(0);
            }
        }
        
        public Map<String, Object> getFilterStats() {
            Map<String, Object> stats = new HashMap<>();
            stats.put("accepted_count", acceptedCount);
            stats.put("denied_count", deniedCount);
            stats.put("total_registered_metrics", registry.getMeters().size());
            stats.put("recent_denied_metrics", new ArrayList<>(deniedMetrics));
            stats.put("acceptance_rate", 
                acceptedCount > 0 ? (double) acceptedCount / (acceptedCount + deniedCount) * 100 : 100.0);
            return stats;
        }
    }

    // ============================================
    // Example 8: Meter Filter REST Controller
    // ============================================
    
    @RestController
    @RequestMapping("/api/filters")
    public static class MeterFilterController {
        
        private final DynamicFilterService dynamicFilterService;
        private final FilterStatisticsService filterStatsService;
        
        public MeterFilterController(
                DynamicFilterService dynamicFilterService,
                FilterStatisticsService filterStatsService) {
            this.dynamicFilterService = dynamicFilterService;
            this.filterStatsService = filterStatsService;
        }
        
        @PostMapping("/deny")
        public Map<String, String> addDenyFilter(@RequestParam String namePrefix) {
            dynamicFilterService.addDenyFilter(namePrefix);
            return Collections.singletonMap("status", "deny filter added");
        }
        
        @PostMapping("/accept")
        public Map<String, String> addAcceptFilter(@RequestParam String namePrefix) {
            dynamicFilterService.addAcceptFilter(namePrefix);
            return Collections.singletonMap("status", "accept filter added");
        }
        
        @PostMapping("/max-metrics")
        public Map<String, String> setMaxMetrics(@RequestParam int count) {
            dynamicFilterService.addMaximumAllowableMetrics(count);
            return Collections.singletonMap("status", "max metrics set to " + count);
        }
        
        @PostMapping("/common-tag")
        public Map<String, String> addCommonTag(
                @RequestParam String key,
                @RequestParam String value) {
            dynamicFilterService.addCommonTag(key, value);
            return Collections.singletonMap("status", "common tag added");
        }
        
        @PostMapping("/ignore-tags")
        public Map<String, String> ignoreTags(@RequestParam String[] tagKeys) {
            dynamicFilterService.addIgnoreTagFilter(tagKeys);
            return Collections.singletonMap("status", 
                "ignoring tags: " + String.join(", ", tagKeys));
        }
        
        @PostMapping("/replace-tag")
        public Map<String, String> replaceTagValue(
                @RequestParam String tagKey,
                @RequestParam String oldValue,
                @RequestParam String newValue) {
            dynamicFilterService.replaceTagValues(tagKey, oldValue, newValue);
            return Collections.singletonMap("status", "tag replacement added");
        }
        
        @GetMapping("/count")
        public Map<String, Integer> getFilterCount() {
            return Collections.singletonMap("active_filters", 
                dynamicFilterService.getActiveFilterCount());
        }
        
        @GetMapping("/stats")
        public Map<String, Object> getFilterStats() {
            return filterStatsService.getFilterStats();
        }
    }
}
