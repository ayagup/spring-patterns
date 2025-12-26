package com.example.tracing;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import zipkin2.reporter.AsyncReporter;
import zipkin2.reporter.Reporter;
import zipkin2.reporter.Sender;
import zipkin2.reporter.brave.ZipkinSpanHandler;
import zipkin2.reporter.urlconnection.URLConnectionSender;

/**
 * Zipkin Integration Pattern
 * ==========================
 * 
 * Demonstrates integration with Zipkin distributed tracing backend.
 * 
 * Key Concepts:
 * ------------
 * 1. Zipkin Server - Central trace collection and UI
 * 2. Span Reporter - Send spans to Zipkin
 * 3. HTTP/Kafka Transport - How spans are sent
 * 4. Sampling - Control what gets reported
 * 5. UI - Visualize traces and dependencies
 * 
 * Zipkin Architecture:
 * -------------------
 * Application -> Brave/Sleuth -> Reporter -> Transport -> Zipkin Server -> UI
 * 
 * Dependencies:
 * ------------
 * <dependency>
 *     <groupId>org.springframework.cloud</groupId>
 *     <artifactId>spring-cloud-sleuth-zipkin</artifactId>
 * </dependency>
 * 
 * @author Spring Patterns
 * @version 1.0
 */

/**
 * Example 1: Basic Zipkin Configuration
 */
@Configuration
class BasicZipkinConfig {
    
    /**
     * application.yml:
     * spring:
     *   zipkin:
     *     base-url: http://localhost:9411
     *     enabled: true
     */
    
    @Bean
    public Sender zipkinSender() {
        return URLConnectionSender.create("http://localhost:9411/api/v2/spans");
    }
}

/**
 * Example 2: HTTP Transport Configuration
 */
@Configuration
class HttpTransportConfig {
    
    @Bean
    public Sender httpSender() {
        return URLConnectionSender.newBuilder()
            .endpoint("http://zipkin-server:9411/api/v2/spans")
            .connectTimeout(1000)
            .readTimeout(5000)
            .compressionEnabled(true)
            .build();
    }
    
    @Bean
    public ZipkinSpanHandler zipkinSpanHandler(Sender sender) {
        return ZipkinSpanHandler.create(
            AsyncReporter.builder(sender)
                .build()
        );
    }
}

/**
 * Example 3: Kafka Transport Configuration
 */
class KafkaTransportConfig {
    
    /**
     * application.yml:
     * spring:
     *   zipkin:
     *     sender:
     *       type: kafka
     *     kafka:
     *       bootstrap-servers: localhost:9092
     *       topic: zipkin
     */
    
    /*
    <dependency>
        <groupId>org.springframework.kafka</groupId>
        <artifactId>spring-kafka</artifactId>
    </dependency>
    <dependency>
        <groupId>io.zipkin.reporter2</groupId>
        <artifactId>zipkin-sender-kafka</artifactId>
    </dependency>
    */
}

/**
 * Example 4: RabbitMQ Transport Configuration
 */
class RabbitMQTransportConfig {
    
    /**
     * application.yml:
     * spring:
     *   zipkin:
     *     sender:
     *       type: rabbit
     *     rabbitmq:
     *       addresses: localhost:5672
     *       queue: zipkin
     */
    
    /*
    <dependency>
        <groupId>org.springframework.amqp</groupId>
        <artifactId>spring-rabbit</artifactId>
    </dependency>
    <dependency>
        <groupId>io.zipkin.reporter2</groupId>
        <artifactId>zipkin-sender-amqp-client</artifactId>
    </dependency>
    */
}

/**
 * Example 5: Custom Reporter Configuration
 */
@Configuration
class CustomReporterConfig {
    
    @Bean
    public ZipkinSpanHandler customZipkinSpanHandler(Sender sender) {
        return ZipkinSpanHandler.newBuilder(
            AsyncReporter.builder(sender)
                .queuedMaxSpans(1000)        // Buffer size
                .messageMaxBytes(500000)      // Max message size
                .closeTimeout(1000)           // Close timeout ms
                .messageTimeout(1000)         // Send timeout ms
                .build()
        ).build();
    }
}

/**
 * Example 6: Multiple Zipkin Servers
 */
@Configuration
class MultipleZipkinServersConfig {
    
    @Bean
    public ZipkinSpanHandler primaryZipkinHandler() {
        Sender sender = URLConnectionSender.create(
            "http://zipkin-primary:9411/api/v2/spans"
        );
        return ZipkinSpanHandler.create(AsyncReporter.create(sender));
    }
    
    @Bean
    public ZipkinSpanHandler secondaryZipkinHandler() {
        Sender sender = URLConnectionSender.create(
            "http://zipkin-secondary:9411/api/v2/spans"
        );
        return ZipkinSpanHandler.create(AsyncReporter.create(sender));
    }
}

/**
 * Example 7: Service Name Configuration
 */
class ServiceNameConfig {
    
    /**
     * application.yml:
     * spring:
     *   application:
     *     name: order-service  # Used as service name in Zipkin
     *   
     *   zipkin:
     *     base-url: http://localhost:9411
     *     service:
     *       name: ${spring.application.name}
     */
}

/**
 * Example 8: Span Tags Configuration
 */
class SpanTagsConfig {
    
    /**
     * application.yml:
     * spring:
     *   sleuth:
     *     baggage:
     *       remote-fields: userId,sessionId,tenantId
     *       correlation-fields: userId,sessionId,tenantId
     *       
     *     propagation:
     *       tag:
     *         enabled: true
     *         whitelisted-keys: userId,sessionId
     */
}

/**
 * Example 9: Discovery Service Integration
 */
class DiscoveryIntegrationConfig {
    
    /**
     * application.yml:
     * spring:
     *   zipkin:
     *     base-url: http://zipkin-server:9411
     *     discovery-client-enabled: true  # Use service discovery
     *     
     * eureka:
     *   client:
     *     service-url:
     *       defaultZone: http://localhost:8761/eureka/
     */
}

/**
 * Example 10: Docker Compose Setup
 */
class DockerComposeSetup {
    
    /**
     * docker-compose.yml:
     * 
     * version: '3'
     * services:
     *   zipkin:
     *     image: openzipkin/zipkin:latest
     *     ports:
     *       - "9411:9411"
     *     environment:
     *       - STORAGE_TYPE=mem  # or elasticsearch, cassandra, mysql
     *       
     *   # Optional: Elasticsearch storage
     *   elasticsearch:
     *     image: docker.elastic.co/elasticsearch/elasticsearch:7.15.0
     *     environment:
     *       - discovery.type=single-node
     *     ports:
     *       - "9200:9200"
     *       
     *   zipkin-elasticsearch:
     *     image: openzipkin/zipkin:latest
     *     ports:
     *       - "9411:9411"
     *     environment:
     *       - STORAGE_TYPE=elasticsearch
     *       - ES_HOSTS=elasticsearch:9200
     *     depends_on:
     *       - elasticsearch
     */
}

/**
 * Complete Configuration Example
 */
class CompleteZipkinConfiguration {
    
    /**
     * application.yml:
     * 
     * spring:
     *   application:
     *     name: order-service
     *   
     *   zipkin:
     *     enabled: true
     *     base-url: http://localhost:9411
     *     
     *     # Sender configuration
     *     sender:
     *       type: web  # or kafka, rabbit
     *       
     *     # Service discovery
     *     discovery-client-enabled: false
     *     
     *     # Locator
     *     locator:
     *       discovery:
     *         enabled: false
     *         
     *   sleuth:
     *     enabled: true
     *     
     *     # Sampling
     *     sampler:
     *       probability: 1.0  # 100% for development
     *       
     *     # Baggage
     *     baggage:
     *       remote-fields: userId,sessionId,tenantId
     *       correlation-fields: userId,sessionId
     *       
     *     # Web
     *     web:
     *       skip-pattern: /actuator.*|/health|/metrics
     *       
     *     # Integration
     *     integration:
     *       enabled: true
     *       
     *     # Async
     *     async:
     *       enabled: true
     *       
     *     # Messaging
     *     messaging:
     *       enabled: true
     *       kafka:
     *         enabled: true
     */
}

/**
 * Zipkin UI Usage Examples
 */
class ZipkinUIUsage {
    
    /**
     * Zipkin UI Features:
     * 
     * 1. Find Traces:
     *    - http://localhost:9411/zipkin/
     *    - Search by: service, span, tags, duration
     *    - Time range: last hour, day, custom
     *    
     * 2. Trace Details:
     *    - View complete request flow
     *    - See timing breakdown
     *    - Inspect tags and annotations
     *    - Identify slow operations
     *    
     * 3. Dependencies:
     *    - http://localhost:9411/zipkin/dependency
     *    - Visualize service dependencies
     *    - Call volume and error rates
     *    
     * 4. Search Examples:
     *    - serviceName=order-service
     *    - http.method=GET
     *    - http.status_code=500
     *    - minDuration=100ms
     *    - tags=userId:12345
     */
}

/**
 * Performance Tuning
 */
class PerformanceTuning {
    
    /**
     * Optimize Zipkin Reporting:
     * 
     * 1. Sampling:
     *    spring.sleuth.sampler.probability: 0.1  # 10% sampling
     *    
     * 2. Buffer Size:
     *    reporter.queuedMaxSpans: 1000
     *    
     * 3. Batch Size:
     *    reporter.messageMaxBytes: 500000
     *    
     * 4. Timeouts:
     *    reporter.messageTimeout: 1000
     *    reporter.closeTimeout: 1000
     *    
     * 5. Transport:
     *    - HTTP: Simple, direct
     *    - Kafka: High throughput, async
     *    - RabbitMQ: Reliable, async
     *    
     * 6. Compression:
     *    sender.compressionEnabled: true
     */
}

/**
 * Main Pattern Class
 */
public class ZipkinIntegrationPattern {
    
    public static void main(String[] args) {
        System.out.println("Zipkin Integration Pattern");
        System.out.println("==========================\n");
        
        System.out.println("Zipkin Components:");
        System.out.println("1. Collector - Receives spans");
        System.out.println("2. Storage - Stores traces (memory, ES, Cassandra, MySQL)");
        System.out.println("3. API - Query traces");
        System.out.println("4. UI - Visualize traces\n");
        
        System.out.println("Transport Options:");
        System.out.println("- HTTP (default): Simple, synchronous");
        System.out.println("- Kafka: High throughput, asynchronous");
        System.out.println("- RabbitMQ: Reliable, asynchronous\n");
        
        System.out.println("Quick Start:");
        System.out.println("1. docker run -d -p 9411:9411 openzipkin/zipkin");
        System.out.println("2. Add dependency: spring-cloud-sleuth-zipkin");
        System.out.println("3. Configure: spring.zipkin.base-url=http://localhost:9411");
        System.out.println("4. Visit: http://localhost:9411/zipkin/\n");
        
        System.out.println("UI Features:");
        System.out.println("✓ Search traces by service, tags, duration");
        System.out.println("✓ View trace timeline");
        System.out.println("✓ Inspect span details");
        System.out.println("✓ Service dependency graph");
        System.out.println("✓ Error tracking");
        System.out.println("✓ Performance analysis\n");
        
        System.out.println("Production Setup:");
        System.out.println("- Use Kafka/RabbitMQ transport");
        System.out.println("- Enable Elasticsearch storage");
        System.out.println("- Configure retention policies");
        System.out.println("- Set appropriate sampling rate");
        System.out.println("- Monitor Zipkin server metrics");
    }
}
