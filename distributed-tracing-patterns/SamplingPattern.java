package com.example.tracing;

import brave.sampler.Sampler;
import brave.sampler.RateLimitingSampler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Sampling Pattern
 * ================
 * 
 * Demonstrates sampling strategies for controlling trace collection volume.
 * 
 * Key Concepts:
 * ------------
 * 1. Sampling - Select subset of traces to record
 * 2. Sampling Rate - Percentage of traces to keep
 * 3. Rate Limiting - Maximum traces per second
 * 4. Deterministic - Same decision for same trace
 * 5. Adaptive - Adjust based on conditions
 * 
 * Sampling Strategies:
 * -------------------
 * - Always Sample (100%)
 * - Never Sample (0%)
 * - Probability (e.g., 10%)
 * - Rate Limiting (e.g., 100 traces/sec)
 * - Conditional (based on criteria)
 * 
 * Configuration:
 * -------------
 * spring.sleuth.sampler.probability=0.1  # 10%
 * spring.sleuth.sampler.rate=100  # 100 traces/sec
 * 
 * @author Spring Patterns
 * @version 1.0
 */

/**
 * Example 1: Always Sampler
 */
@Configuration
class AlwaysSamplerConfig {
    
    /**
     * Sample all traces (100%)
     * Use for: Development, debugging, critical services
     */
    @Bean
    public Sampler alwaysSampler() {
        return Sampler.ALWAYS_SAMPLE;
    }
}

/**
 * Example 2: Never Sampler
 */
@Configuration
class NeverSamplerConfig {
    
    /**
     * Sample no traces (0%)
     * Use for: Turning off tracing, maintenance
     */
    @Bean
    public Sampler neverSampler() {
        return Sampler.NEVER_SAMPLE;
    }
}

/**
 * Example 3: Probability Sampler
 */
@Configuration
class ProbabilitySamplerConfig {
    
    /**
     * Sample 10% of traces
     * Use for: High-volume services, cost control
     */
    @Bean
    public Sampler probabilitySampler() {
        return Sampler.create(0.1f); // 10%
    }
    
    /**
     * Different probabilities for different scenarios
     */
    public void demonstrateProbabilities() {
        Sampler low = Sampler.create(0.01f);    // 1%
        Sampler medium = Sampler.create(0.1f);   // 10%
        Sampler high = Sampler.create(0.5f);     // 50%
        
        System.out.println("Low traffic: 1%");
        System.out.println("Medium traffic: 10%");
        System.out.println("High priority: 50%");
    }
}

/**
 * Example 4: Rate Limiting Sampler
 */
@Configuration
class RateLimitingSamplerConfig {
    
    /**
     * Sample max 100 traces per second
     * Use for: Predictable backend load
     */
    @Bean
    public Sampler rateLimitingSampler() {
        return RateLimitingSampler.create(100); // 100 traces/sec
    }
    
    /**
     * Different rates for different services
     */
    public void demonstrateRateLimits() {
        Sampler lowRate = RateLimitingSampler.create(10);    // 10/sec
        Sampler mediumRate = RateLimitingSampler.create(100);  // 100/sec
        Sampler highRate = RateLimitingSampler.create(1000);   // 1000/sec
        
        System.out.println("Low volume service: 10 traces/sec");
        System.out.println("Medium volume: 100 traces/sec");
        System.out.println("High volume: 1000 traces/sec");
    }
}

/**
 * Example 5: Conditional Sampler
 */
class ConditionalSampler extends Sampler {
    
    @Override
    public boolean isSampled(long traceId) {
        // Always sample if trace ID ends in 0 (10% sampling)
        return traceId % 10 == 0;
    }
}

/**
 * Example 6: Error-Based Sampler
 */
class ErrorBasedSampler extends Sampler {
    
    private final Sampler normalSampler = Sampler.create(0.1f); // 10%
    
    @Override
    public boolean isSampled(long traceId) {
        // In real scenario, check if request resulted in error
        // Always sample errors, use probability for normal
        return normalSampler.isSampled(traceId);
    }
    
    /**
     * Force sampling on error
     */
    public void onError() {
        // Mark current trace as sampled
        System.out.println("Error detected - force sampling");
    }
}

/**
 * Example 7: Path-Based Sampler
 */
class PathBasedSampler extends Sampler {
    
    private String currentPath = "";
    
    @Override
    public boolean isSampled(long traceId) {
        // Sample based on request path
        if (currentPath.startsWith("/api/critical")) {
            return true; // 100% for critical paths
        } else if (currentPath.startsWith("/api/admin")) {
            return traceId % 2 == 0; // 50% for admin
        } else {
            return traceId % 10 == 0; // 10% for others
        }
    }
    
    public void setPath(String path) {
        this.currentPath = path;
    }
}

/**
 * Example 8: User-Based Sampler
 */
class UserBasedSampler extends Sampler {
    
    private String currentUserId = "";
    
    @Override
    public boolean isSampled(long traceId) {
        // Always sample for specific test users
        if (currentUserId.startsWith("test_")) {
            return true;
        }
        // Always sample for admin users
        if (currentUserId.startsWith("admin_")) {
            return true;
        }
        // 10% for regular users
        return traceId % 10 == 0;
    }
    
    public void setUserId(String userId) {
        this.currentUserId = userId;
    }
}

/**
 * Example 9: Time-Based Sampler
 */
class TimeBasedSampler extends Sampler {
    
    @Override
    public boolean isSampled(long traceId) {
        int hour = java.time.LocalTime.now().getHour();
        
        // Higher sampling during business hours
        if (hour >= 9 && hour <= 17) {
            return traceId % 5 == 0; // 20% during business hours
        } else {
            return traceId % 20 == 0; // 5% off-hours
        }
    }
}

/**
 * Example 10: Composite Sampler
 */
@Configuration
class CompositeSamplerConfig {
    
    /**
     * Combine multiple sampling strategies
     */
    @Bean
    public Sampler compositeSampler() {
        return new CompositeSampler();
    }
    
    static class CompositeSampler extends Sampler {
        
        private final Sampler rateLimiter = RateLimitingSampler.create(100);
        private final Sampler probabilitySampler = Sampler.create(0.1f);
        
        @Override
        public boolean isSampled(long traceId) {
            // Must pass both rate limit AND probability
            return rateLimiter.isSampled(traceId) &&
                   probabilitySampler.isSampled(traceId);
        }
    }
}

/**
 * Sampling Configuration Examples
 */
class SamplingConfiguration {
    
    /**
     * Development environment - sample everything
     */
    public Sampler developmentSampler() {
        return Sampler.ALWAYS_SAMPLE;
    }
    
    /**
     * Staging environment - sample 50%
     */
    public Sampler stagingSampler() {
        return Sampler.create(0.5f);
    }
    
    /**
     * Production environment - rate limited
     */
    public Sampler productionSampler() {
        return RateLimitingSampler.create(100);
    }
    
    /**
     * High-volume service - low sampling
     */
    public Sampler highVolumeSampler() {
        return Sampler.create(0.01f); // 1%
    }
    
    /**
     * Critical service - high sampling
     */
    public Sampler criticalServiceSampler() {
        return Sampler.create(0.5f); // 50%
    }
}

/**
 * Adaptive Sampling Strategy
 */
class AdaptiveSampler extends Sampler {
    
    private float currentRate = 0.1f;
    private int requestCount = 0;
    private int errorCount = 0;
    
    @Override
    public boolean isSampled(long traceId) {
        return traceId % (int)(1 / currentRate) == 0;
    }
    
    /**
     * Adjust sampling based on error rate
     */
    public void adjustSampling() {
        requestCount++;
        
        // Every 1000 requests, check error rate
        if (requestCount % 1000 == 0) {
            float errorRate = (float) errorCount / requestCount;
            
            if (errorRate > 0.05) {
                // High error rate - increase sampling
                currentRate = Math.min(1.0f, currentRate * 2);
                System.out.println("Increasing sampling to: " + (currentRate * 100) + "%");
            } else if (errorRate < 0.01) {
                // Low error rate - decrease sampling
                currentRate = Math.max(0.01f, currentRate / 2);
                System.out.println("Decreasing sampling to: " + (currentRate * 100) + "%");
            }
            
            // Reset counters
            requestCount = 0;
            errorCount = 0;
        }
    }
    
    public void recordError() {
        errorCount++;
    }
}

/**
 * Sampling Decision Logger
 */
class SamplingDecisionLogger {
    
    public void logDecision(boolean sampled, String reason, long traceId) {
        String decision = sampled ? "SAMPLED" : "NOT SAMPLED";
        System.out.println(String.format(
            "Trace %s %s - Reason: %s",
            Long.toHexString(traceId),
            decision,
            reason
        ));
    }
    
    public void demonstrateLogging() {
        logDecision(true, "Critical path", 123456L);
        logDecision(true, "Error occurred", 234567L);
        logDecision(false, "Probability 10%", 345678L);
        logDecision(false, "Rate limit exceeded", 456789L);
    }
}

/**
 * Main Pattern Class
 */
public class SamplingPattern {
    
    public void demonstrateSamplingPattern() {
        System.out.println("\n=== Sampling Pattern ===");
        System.out.println("Control trace collection volume");
        System.out.println("\nSampling Strategies:");
        System.out.println("  - Always (100%)");
        System.out.println("  - Never (0%)");
        System.out.println("  - Probability (e.g., 10%)");
        System.out.println("  - Rate Limiting (traces/sec)");
        System.out.println("  - Conditional (based on criteria)");
        System.out.println("\nEnvironment Recommendations:");
        System.out.println("  - Development: 100% (Always)");
        System.out.println("  - Staging: 50% (Probability)");
        System.out.println("  - Production: Rate limited or low %");
        System.out.println("\nBenefits:");
        System.out.println("  - Reduce storage costs");
        System.out.println("  - Lower backend load");
        System.out.println("  - Maintain visibility");
        System.out.println("  - Control trace volume");
        System.out.println("\nBest Practices:");
        System.out.println("  - Always sample errors");
        System.out.println("  - Higher rate for critical paths");
        System.out.println("  - Use rate limiting for predictability");
        System.out.println("  - Adjust based on traffic");
    }
    
    public void demonstrateSamplingRates() {
        System.out.println("\n=== Sampling Rate Examples ===");
        
        Sampler always = Sampler.ALWAYS_SAMPLE;
        Sampler never = Sampler.NEVER_SAMPLE;
        Sampler low = Sampler.create(0.01f);
        Sampler medium = Sampler.create(0.1f);
        Sampler high = Sampler.create(0.5f);
        
        System.out.println("Always: " + (always.isSampled(123) ? "SAMPLED" : "NOT SAMPLED"));
        System.out.println("Never: " + (never.isSampled(123) ? "SAMPLED" : "NOT SAMPLED"));
        System.out.println("Low (1%): Approximately 1 in 100 traces");
        System.out.println("Medium (10%): Approximately 1 in 10 traces");
        System.out.println("High (50%): Approximately 1 in 2 traces");
    }
}
