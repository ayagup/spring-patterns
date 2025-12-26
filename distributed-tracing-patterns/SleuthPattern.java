package com.example.tracing;

import brave.Tracer;
import brave.sampler.Sampler;
import io.micrometer.tracing.brave.bridge.BraveTracer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.sleuth.autoconfig.instrument.web.SkipPatternProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

/**
 * Sleuth Pattern
 * ==============
 * 
 * Demonstrates Spring Cloud Sleuth for automatic distributed tracing.
 * 
 * Key Concepts:
 * ------------
 * 1. Auto-instrumentation - No code changes required
 * 2. Trace/Span IDs - Automatic generation
 * 3. Log Correlation - TraceId in logs
 * 4. Integration - RestTemplate, WebClient, Messaging
 * 5. Sampling - Control trace collection
 * 
 * Dependencies:
 * ------------
 * <dependency>
 *     <groupId>org.springframework.cloud</groupId>
 *     <artifactId>spring-cloud-starter-sleuth</artifactId>
 * </dependency>
 * 
 * @author Spring Patterns
 * @version 1.0
 */

/**
 * Example 1: Basic Sleuth Configuration
 */
@Configuration
class BasicSleuthConfig {
    
    /**
     * Enable Sleuth with default settings
     * application.yml:
     * spring:
     *   sleuth:
     *     enabled: true
     */
    
    @Bean
    public Sampler defaultSampler() {
        // Sample all requests (100%)
        return Sampler.ALWAYS_SAMPLE;
    }
}

/**
 * Example 2: Probability-Based Sampling
 */
@Configuration
class ProbabilitySamplingConfig {
    
    @Bean
    public Sampler probabilitySampler() {
        // Sample 10% of requests
        return Sampler.create(0.1f);
    }
}

/**
 * Configuration in application.yml:
 * spring:
 *   sleuth:
 *     sampler:
 *       probability: 0.1  # 10% sampling
 */

/**
 * Example 3: Rate-Limiting Sampler
 */
@Configuration
class RateLimitingSamplingConfig {
    
    @Bean
    public Sampler rateLimitingSampler() {
        // Sample max 1000 traces per second
        return brave.sampler.RateLimitingSampler.create(1000);
    }
}

/**
 * Example 4: Custom Skip Patterns
 */
@Configuration
class SkipPatternConfig {
    
    @Bean
    SkipPatternProvider customSkipPatternProvider() {
        return () -> Pattern.compile(
            "/actuator.*|/health|/metrics|/static/.*|/favicon.ico"
        );
    }
}

/**
 * Configuration in application.yml:
 * spring:
 *   sleuth:
 *     web:
 *       skip-pattern: /actuator.*|/health|/metrics
 */

/**
 * Example 5: Log Correlation
 */
@Service
class LogCorrelationExample {
    
    private final Tracer tracer;
    
    public LogCorrelationExample(Tracer tracer) {
        this.tracer = tracer;
    }
    
    public void processWithLogs(String data) {
        // Sleuth automatically adds TraceId and SpanId to logs
        System.out.println("Processing data: " + data);
        
        var span = tracer.currentSpan();
        if (span != null) {
            System.out.println("[TraceId: " + span.context().traceIdString() + 
                             ", SpanId: " + span.context().spanIdString() + "]");
        }
    }
}

/**
 * Logback configuration (logback-spring.xml):
 * <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
 *     <encoder>
 *         <pattern>
 *             %d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - 
 *             [TraceId: %X{traceId}] [SpanId: %X{spanId}] - %msg%n
 *         </pattern>
 *     </encoder>
 * </appender>
 */

/**
 * Example 6: RestTemplate Integration
 */
@Service
class RestTemplateIntegrationExample {
    
    // RestTemplate is automatically instrumented by Sleuth
    // No code changes needed!
    
    public void callExternalService() {
        // Sleuth automatically:
        // 1. Creates a new span
        // 2. Injects trace context into headers
        // 3. Tags HTTP method, URL, status code
        // 4. Handles errors
        
        System.out.println("RestTemplate call automatically traced");
    }
}

/**
 * Example 7: WebClient Integration
 */
@Service
class WebClientIntegrationExample {
    
    // WebClient is automatically instrumented by Sleuth
    
    public void reactiveCall() {
        // Sleuth automatically propagates context in reactive chains
        System.out.println("WebClient call automatically traced");
    }
}

/**
 * Example 8: Messaging Integration
 */
@Service
class MessagingIntegrationExample {
    
    // Spring Cloud Stream/Spring Kafka automatically instrumented
    
    public void sendMessage(String message) {
        // Sleuth automatically:
        // 1. Creates PRODUCER span
        // 2. Injects trace context into message headers
        System.out.println("Message send automatically traced");
    }
    
    public void receiveMessage(String message) {
        // Sleuth automatically:
        // 1. Extracts trace context from headers
        // 2. Creates CONSUMER span
        // 3. Links to producer span
        System.out.println("Message receive automatically traced");
    }
}

/**
 * Example 9: Async Processing
 */
@Service
class AsyncProcessingExample {
    
    private final Tracer tracer;
    
    public AsyncProcessingExample(Tracer tracer) {
        this.tracer = tracer;
    }
    
    public void asyncOperation() {
        // Sleuth propagates context to @Async methods
        var span = tracer.currentSpan();
        System.out.println("Main thread TraceId: " + 
            (span != null ? span.context().traceIdString() : "none"));
        
        // Call @Async method - context is propagated
        processAsync();
    }
    
    // @Async - Sleuth automatically propagates trace context
    private void processAsync() {
        var span = tracer.currentSpan();
        System.out.println("Async thread TraceId: " + 
            (span != null ? span.context().traceIdString() : "none"));
    }
}

/**
 * Example 10: Custom Tags
 */
@Service
class CustomTagsExample {
    
    private final Tracer tracer;
    
    public CustomTagsExample(Tracer tracer) {
        this.tracer = tracer;
    }
    
    public void processOrder(String orderId, String customerId) {
        var span = tracer.currentSpan();
        
        if (span != null) {
            // Add custom tags
            span.tag("order.id", orderId);
            span.tag("customer.id", customerId);
            span.tag("business.operation", "order-processing");
            span.tag("service.version", "v2.1");
            
            System.out.println("Added custom tags to span");
        }
    }
}

/**
 * Complete application.yml Configuration
 */
class SleuthConfiguration {
    /*
    spring:
      application:
        name: order-service
      
      sleuth:
        enabled: true
        
        # Sampling
        sampler:
          probability: 0.1  # 10% of requests
          rate: 1000        # Max 1000 traces/sec
        
        # Log correlation
        log:
          slf4j:
            enabled: true
            whitelisted-mdc-keys: userId,sessionId
        
        # Web
        web:
          enabled: true
          skip-pattern: /actuator.*|/health|/metrics
          
        # Async
        async:
          enabled: true
          
        # Messaging
        messaging:
          enabled: true
          kafka:
            enabled: true
          rabbit:
            enabled: true
            
        # HTTP
        http:
          legacy:
            enabled: false
          
        # Baggage (context propagation)
        baggage:
          remote-fields: userId,sessionId
          correlation-fields: userId,sessionId
          
        # Integration
        integration:
          enabled: true
          
        # RxJava
        rxjava:
          schedulers:
            hook:
              enabled: true
              
        # Supports
        supports:
          jdbc: true
    */
}

/**
 * Complete pom.xml Dependencies
 */
class SleuthDependencies {
    /*
    <dependencies>
        <!-- Core Sleuth -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-sleuth</artifactId>
        </dependency>
        
        <!-- Zipkin Reporter -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-sleuth-zipkin</artifactId>
        </dependency>
        
        <!-- Brave (underlying implementation) -->
        <dependency>
            <groupId>io.zipkin.brave</groupId>
            <artifactId>brave</artifactId>
        </dependency>
        
        <!-- Optional: Kafka integration -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-sleuth-kafka-streams</artifactId>
        </dependency>
        
        <!-- Optional: RabbitMQ integration -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-sleuth-stream</artifactId>
        </dependency>
    </dependencies>
    
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>${spring-cloud.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>
    */
}

/**
 * Main Pattern Class
 */
public class SleuthPattern {
    
    public static void main(String[] args) {
        System.out.println("Sleuth Pattern - Auto-Instrumentation");
        System.out.println("=====================================\n");
        
        System.out.println("Features:");
        System.out.println("✓ Zero code changes required");
        System.out.println("✓ Automatic trace/span generation");
        System.out.println("✓ Log correlation (TraceId in logs)");
        System.out.println("✓ RestTemplate/WebClient integration");
        System.out.println("✓ Messaging integration (Kafka, RabbitMQ)");
        System.out.println("✓ Async processing support");
        System.out.println("✓ Database query tracing\n");
        
        System.out.println("Auto-Instrumented Components:");
        System.out.println("- RestTemplate");
        System.out.println("- WebClient (reactive)");
        System.out.println("- Feign clients");
        System.out.println("- Spring Cloud Stream");
        System.out.println("- Kafka");
        System.out.println("- RabbitMQ");
        System.out.println("- Async methods (@Async)");
        System.out.println("- Scheduled tasks (@Scheduled)");
        System.out.println("- Hystrix commands\n");
        
        System.out.println("Configuration:");
        System.out.println("spring.sleuth.sampler.probability: 0.1  # 10% sampling");
        System.out.println("spring.sleuth.web.skip-pattern: /actuator.*");
        System.out.println("spring.sleuth.baggage.remote-fields: userId\n");
        
        System.out.println("Benefits:");
        System.out.println("- No manual span creation");
        System.out.println("- Automatic context propagation");
        System.out.println("- Consistent trace correlation");
        System.out.println("- Easy debugging with TraceId in logs");
    }
}
