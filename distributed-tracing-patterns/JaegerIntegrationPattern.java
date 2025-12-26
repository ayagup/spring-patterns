package com.example.tracing;

import io.jaegertracing.Configuration;
import io.jaegertracing.internal.JaegerTracer;
import io.jaegertracing.internal.samplers.ConstSampler;
import io.jaegertracing.internal.samplers.ProbabilisticSampler;
import io.jaegertracing.internal.samplers.RateLimitingSampler;
import io.opentracing.Tracer;
import org.springframework.context.annotation.Bean;

/**
 * Jaeger Integration Pattern
 * ==========================
 * 
 * Demonstrates integration with Jaeger distributed tracing platform.
 * 
 * Key Concepts:
 * ------------
 * 1. Jaeger - CNCF distributed tracing platform
 * 2. OpenTracing API - Vendor-neutral tracing API
 * 3. Agent/Collector - Trace collection architecture
 * 4. UDP/HTTP Transport - Span transmission
 * 5. Storage Backends - Cassandra, Elasticsearch, In-Memory
 * 
 * Jaeger Architecture:
 * -------------------
 * Application -> Jaeger Client -> Agent (UDP) -> Collector -> Storage -> UI
 * 
 * Dependencies:
 * ------------
 * <dependency>
 *     <groupId>io.opentracing.contrib</groupId>
 *     <artifactId>opentracing-spring-jaeger-cloud-starter</artifactId>
 * </dependency>
 * 
 * @author Spring Patterns
 * @version 1.0
 */

/**
 * Example 1: Basic Jaeger Configuration
 */
@org.springframework.context.annotation.Configuration
class BasicJaegerConfig {
    
    @Bean
    public Tracer jaegerTracer() {
        return Configuration.fromEnv("order-service").getTracer();
    }
}

/**
 * Environment Variables:
 * JAEGER_SERVICE_NAME=order-service
 * JAEGER_AGENT_HOST=localhost
 * JAEGER_AGENT_PORT=6831
 * JAEGER_SAMPLER_TYPE=const
 * JAEGER_SAMPLER_PARAM=1
 */

/**
 * Example 2: Programmatic Configuration
 */
@org.springframework.context.annotation.Configuration
class ProgrammaticJaegerConfig {
    
    @Bean
    public Tracer customJaegerTracer() {
        Configuration.SamplerConfiguration samplerConfig = 
            Configuration.SamplerConfiguration.fromEnv()
                .withType(ConstSampler.TYPE)
                .withParam(1);  // Sample all
        
        Configuration.ReporterConfiguration reporterConfig = 
            Configuration.ReporterConfiguration.fromEnv()
                .withLogSpans(true)
                .withFlushInterval(1000)
                .withMaxQueueSize(1000)
                .withSender(
                    Configuration.SenderConfiguration.fromEnv()
                        .withAgentHost("localhost")
                        .withAgentPort(6831)
                );
        
        return Configuration.fromEnv("order-service")
            .withSampler(samplerConfig)
            .withReporter(reporterConfig)
            .getTracer();
    }
}

/**
 * Example 3: Probability Sampling
 */
@org.springframework.context.annotation.Configuration
class ProbabilitySamplingConfig {
    
    @Bean
    public Tracer probabilisticTracer() {
        Configuration.SamplerConfiguration samplerConfig = 
            new Configuration.SamplerConfiguration()
                .withType(ProbabilisticSampler.TYPE)
                .withParam(0.1);  // Sample 10%
        
        return new Configuration("order-service")
            .withSampler(samplerConfig)
            .getTracer();
    }
}

/**
 * Example 4: Rate-Limiting Sampling
 */
@org.springframework.context.annotation.Configuration
class RateLimitingSamplingConfig {
    
    @Bean
    public Tracer rateLimitingTracer() {
        Configuration.SamplerConfiguration samplerConfig = 
            new Configuration.SamplerConfiguration()
                .withType(RateLimitingSampler.TYPE)
                .withParam(100);  // Max 100 traces/sec
        
        return new Configuration("order-service")
            .withSampler(samplerConfig)
            .getTracer();
    }
}

/**
 * Example 5: HTTP Collector Configuration
 */
@org.springframework.context.annotation.Configuration
class HttpCollectorConfig {
    
    @Bean
    public Tracer httpCollectorTracer() {
        Configuration.SenderConfiguration senderConfig = 
            new Configuration.SenderConfiguration()
                .withEndpoint("http://jaeger-collector:14268/api/traces");
        
        Configuration.ReporterConfiguration reporterConfig = 
            new Configuration.ReporterConfiguration()
                .withSender(senderConfig);
        
        return new Configuration("order-service")
            .withReporter(reporterConfig)
            .getTracer();
    }
}

/**
 * Example 6: Custom Tags Configuration
 */
@org.springframework.context.annotation.Configuration
class CustomTagsConfig {
    
    @Bean
    public Tracer tracerWithTags() {
        JaegerTracer tracer = Configuration.fromEnv("order-service")
            .getTracerBuilder()
            .withTag("environment", "production")
            .withTag("version", "v2.1.0")
            .withTag("region", "us-west")
            .withTag("datacenter", "dc1")
            .build();
        
        return tracer;
    }
}

/**
 * Example 7: Spring Cloud Sleuth with Jaeger
 */
class SleuthJaegerIntegration {
    
    /**
     * application.yml:
     * 
     * spring:
     *   application:
     *     name: order-service
     *   
     *   sleuth:
     *     enabled: true
     *     sampler:
     *       probability: 1.0
     *   
     * opentracing:
     *   jaeger:
     *     enabled: true
     *     service-name: ${spring.application.name}
     *     
     *     # UDP Agent
     *     udp-sender:
     *       host: localhost
     *       port: 6831
     *     
     *     # HTTP Collector (alternative)
     *     http-sender:
     *       url: http://jaeger-collector:14268/api/traces
     *     
     *     # Sampling
     *     probabilistic-sampler:
     *       sampling-rate: 1.0
     *     
     *     # Reporter
     *     log-spans: true
     *     flush-interval: 1000
     *     max-queue-size: 1000
     *     
     *     # Tags
     *     tags:
     *       environment: production
     *       version: v2.1.0
     */
}

/**
 * Example 8: Docker Compose Setup
 */
class DockerComposeSetup {
    
    /**
     * docker-compose.yml:
     * 
     * version: '3'
     * services:
     *   jaeger:
     *     image: jaegertracing/all-in-one:latest
     *     ports:
     *       - "5775:5775/udp"    # Zipkin compact thrift
     *       - "6831:6831/udp"    # Jaeger compact thrift (agent)
     *       - "6832:6832/udp"    # Jaeger binary thrift
     *       - "5778:5778"        # Config server
     *       - "16686:16686"      # UI
     *       - "14268:14268"      # Collector HTTP
     *       - "14250:14250"      # Collector gRPC
     *       - "9411:9411"        # Zipkin compatible
     *     environment:
     *       - COLLECTOR_ZIPKIN_HOST_PORT=:9411
     *       - SPAN_STORAGE_TYPE=elasticsearch
     *       - ES_SERVER_URLS=http://elasticsearch:9200
     *       
     *   # Optional: Elasticsearch backend
     *   elasticsearch:
     *     image: docker.elastic.co/elasticsearch/elasticsearch:7.15.0
     *     environment:
     *       - discovery.type=single-node
     *       - "ES_JAVA_OPTS=-Xms512m -Xmx512m"
     *     ports:
     *       - "9200:9200"
     *       
     *   # Optional: Cassandra backend
     *   cassandra:
     *     image: cassandra:3.11
     *     ports:
     *       - "9042:9042"
     *       
     *   jaeger-cassandra:
     *     image: jaegertracing/all-in-one:latest
     *     ports:
     *       - "16686:16686"
     *     environment:
     *       - SPAN_STORAGE_TYPE=cassandra
     *       - CASSANDRA_SERVERS=cassandra:9042
     *     depends_on:
     *       - cassandra
     */
}

/**
 * Example 9: Kubernetes Deployment
 */
class KubernetesDeployment {
    
    /**
     * jaeger-deployment.yaml:
     * 
     * apiVersion: apps/v1
     * kind: Deployment
     * metadata:
     *   name: jaeger
     * spec:
     *   replicas: 1
     *   selector:
     *     matchLabels:
     *       app: jaeger
     *   template:
     *     metadata:
     *       labels:
     *         app: jaeger
     *     spec:
     *       containers:
     *       - name: jaeger
     *         image: jaegertracing/all-in-one:latest
     *         ports:
     *         - containerPort: 16686  # UI
     *         - containerPort: 6831   # Agent
     *           protocol: UDP
     *         - containerPort: 14268  # Collector
     *         env:
     *         - name: SPAN_STORAGE_TYPE
     *           value: elasticsearch
     *         - name: ES_SERVER_URLS
     *           value: http://elasticsearch:9200
     * ---
     * apiVersion: v1
     * kind: Service
     * metadata:
     *   name: jaeger
     * spec:
     *   selector:
     *     app: jaeger
     *   ports:
     *   - name: ui
     *     port: 16686
     *   - name: agent
     *     port: 6831
     *     protocol: UDP
     *   - name: collector
     *     port: 14268
     */
}

/**
 * Example 10: Production Configuration
 */
class ProductionConfiguration {
    
    /**
     * application-prod.yml:
     * 
     * opentracing:
     *   jaeger:
     *     enabled: true
     *     service-name: order-service
     *     
     *     # Use HTTP collector in production
     *     http-sender:
     *       url: http://jaeger-collector.monitoring.svc:14268/api/traces
     *       max-payload: 1048576  # 1MB
     *     
     *     # Disable UDP in production
     *     udp-sender:
     *       host: ""
     *     
     *     # Sampling
     *     probabilistic-sampler:
     *       sampling-rate: 0.1  # 10% sampling
     *     
     *     # Reporter tuning
     *     flush-interval: 1000
     *     max-queue-size: 10000
     *     log-spans: false
     *     
     *     # Tags
     *     tags:
     *       environment: production
     *       version: ${app.version}
     *       region: ${cloud.region}
     *       pod: ${HOSTNAME}
     *       
     *     # Propagation
     *     propagation: jaeger,b3
     *     
     *     # Trace ID 128-bit
     *     trace-id-128bit: true
     */
}

/**
 * Jaeger UI Usage
 */
class JaegerUIUsage {
    
    /**
     * Jaeger UI Features (http://localhost:16686):
     * 
     * 1. Search Traces:
     *    - Service: order-service
     *    - Operation: GET /orders
     *    - Tags: http.status_code=500
     *    - Min/Max Duration
     *    - Lookback: 1h, 2d, custom
     *    
     * 2. Trace View:
     *    - Waterfall timeline
     *    - Span details
     *    - Tags and logs
     *    - Process info
     *    
     * 3. Compare Traces:
     *    - Compare two traces side-by-side
     *    - Identify differences
     *    
     * 4. System Architecture:
     *    - Service dependency graph
     *    - Call volume
     *    - Error rates
     *    - Latency percentiles
     *    
     * 5. Deep Dependency Graph:
     *    - Multi-level dependencies
     *    - Operation level
     */
}

/**
 * Main Pattern Class
 */
public class JaegerIntegrationPattern {
    
    public static void main(String[] args) {
        System.out.println("Jaeger Integration Pattern");
        System.out.println("==========================\n");
        
        System.out.println("Jaeger Components:");
        System.out.println("1. Client - Instruments application");
        System.out.println("2. Agent - Receives spans via UDP");
        System.out.println("3. Collector - Processes and stores spans");
        System.out.println("4. Query - Retrieves traces from storage");
        System.out.println("5. UI - Visualizes traces\n");
        
        System.out.println("Storage Backends:");
        System.out.println("- In-Memory (dev/testing)");
        System.out.println("- Cassandra (production)");
        System.out.println("- Elasticsearch (production)");
        System.out.println("- Kafka (stream processing)\n");
        
        System.out.println("Transport Options:");
        System.out.println("- UDP Agent: Low overhead, local agent");
        System.out.println("- HTTP Collector: Direct, no agent required\n");
        
        System.out.println("Quick Start:");
        System.out.println("1. docker run -d -p 16686:16686 -p 6831:6831/udp jaegertracing/all-in-one");
        System.out.println("2. Add: opentracing-spring-jaeger-cloud-starter");
        System.out.println("3. Set: JAEGER_AGENT_HOST=localhost");
        System.out.println("4. Visit: http://localhost:16686\n");
        
        System.out.println("Key Features:");
        System.out.println("✓ OpenTracing compatible");
        System.out.println("✓ CNCF graduated project");
        System.out.println("✓ High scalability");
        System.out.println("✓ Multiple storage backends");
        System.out.println("✓ Advanced UI with trace comparison");
        System.out.println("✓ Service dependency graph");
        System.out.println("✓ Adaptive sampling");
        System.out.println("✓ 128-bit trace IDs\n");
        
        System.out.println("Production Best Practices:");
        System.out.println("- Use HTTP collector or Agent daemonset");
        System.out.println("- Configure Elasticsearch/Cassandra backend");
        System.out.println("- Set appropriate sampling rate (5-10%)");
        System.out.println("- Enable adaptive sampling");
        System.out.println("- Monitor Jaeger components");
        System.out.println("- Configure retention policies");
    }
}
