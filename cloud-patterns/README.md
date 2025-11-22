# Spring Cloud Patterns

This directory contains comprehensive implementations of essential Spring Cloud patterns for building resilient, scalable microservices architectures.

## Table of Contents

1. [Service Discovery Pattern](#service-discovery-pattern)
2. [Circuit Breaker Pattern](#circuit-breaker-pattern)
3. [Load Balancer Pattern](#load-balancer-pattern)
4. [API Gateway Pattern](#api-gateway-pattern)
5. [Configuration Server Pattern](#configuration-server-pattern)
6. [Distributed Tracing Pattern](#distributed-tracing-pattern)
7. [Centralized Logging Pattern](#centralized-logging-pattern)
8. [Bulkhead Pattern](#bulkhead-pattern)
9. [Retry with Exponential Backoff Pattern](#retry-with-exponential-backoff-pattern)
10. [Rate Limiting Pattern](#rate-limiting-pattern)
11. [Service Mesh Pattern](#service-mesh-pattern)

---

## Service Discovery Pattern

**File:** `ServiceDiscoveryPattern.java`

### Purpose
Enable services to discover and communicate with each other without hard-coded endpoints.

### Key Features
- Service registration with Eureka/Consul
- Instance discovery and lookup
- Health checking and heartbeats
- Load balancing strategy selection
- Manual registry implementation

### Discovery Servers
- **Eureka** (port 8761) - Netflix OSS
- **Consul** (port 8500) - HashiCorp
- **Zookeeper** (port 2181) - Apache
- **Kubernetes DNS** - Cloud-native

### Dependencies
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-consul-discovery</artifactId>
</dependency>
```

### Configuration
```yaml
# Eureka Server
spring:
  application:
    name: eureka-server
server:
  port: 8761
eureka:
  client:
    register-with-eureka: false
    fetch-registry: false

# Eureka Client
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
  instance:
    prefer-ip-address: true
```

### REST Endpoints
- `GET /api/discovery/services` - List all services
- `GET /api/discovery/services/{id}/instances` - Get service instances
- `POST /api/discovery/services/{id}/register` - Register instance
- `DELETE /api/discovery/services/{id}/instances/{instanceId}` - Deregister

---

## Circuit Breaker Pattern

**File:** `CircuitBreakerPattern.java`

### Purpose
Prevent cascading failures by failing fast when a service is down.

### Key Features
- Three-state machine (CLOSED, OPEN, HALF_OPEN)
- Automatic state transitions
- Failure tracking with sliding window
- Slow call detection
- Fallback methods
- Resilience4j integration

### States
1. **CLOSED** - Normal operation, requests pass through
2. **OPEN** - Too many failures, requests fail immediately
3. **HALF_OPEN** - Testing recovery, limited requests allowed

### Dependencies
```xml
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-spring-boot3</artifactId>
</dependency>
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-circuitbreaker</artifactId>
</dependency>
```

### Configuration
```java
@CircuitBreaker(
    name = "paymentService",
    fallbackMethod = "fallbackPayment"
)

CircuitBreakerConfig.custom()
    .failureRateThreshold(50)              // 50% failure rate
    .waitDurationInOpenState(Duration.ofSeconds(10))
    .slidingWindowSize(10)                 // Last 10 calls
    .permittedNumberOfCallsInHalfOpenState(5)
    .slowCallDurationThreshold(Duration.ofSeconds(2))
    .build();
```

### REST Endpoints
- `GET /api/circuit/call-service` - Test circuit breaker
- `GET /api/circuit/state` - Get current state
- `POST /api/circuit/state/open` - Manually open circuit
- `POST /api/circuit/reset` - Reset circuit breaker

---

## Load Balancer Pattern

**File:** `LoadBalancerPattern.java`

### Purpose
Distribute client requests across multiple service instances.

### Key Features
- Multiple load balancing strategies
- Client-side load balancing
- Health-aware routing
- Server statistics tracking

### Load Balancing Strategies
1. **Round Robin** - Sequential distribution
2. **Weighted Round Robin** - Weight-based distribution
3. **Random** - Random selection
4. **Least Connections** - Route to least busy instance
5. **IP Hash** - Sticky sessions by client IP
6. **Least Response Time** - Fastest responding server

### Dependencies
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-loadbalancer</artifactId>
</dependency>
```

### Configuration
```java
@LoadBalanced
@Bean
public RestTemplate restTemplate() {
    return new RestTemplate();
}
```

### REST Endpoints
- `GET /api/lb/call-service?strategy=roundRobin` - Test load balancing
- `GET /api/lb/servers` - List all servers
- `GET /api/lb/stats` - View statistics
- `POST /api/lb/servers/{id}/health` - Update server health

---

## API Gateway Pattern

**File:** `APIGatewayPattern.java`

### Purpose
Provide a single entry point for all client requests with routing, filtering, and security.

### Key Features
- Request routing to microservices
- Authentication and authorization
- Rate limiting
- Request/response transformation
- CORS handling
- Circuit breaker integration

### Dependencies
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-gateway</artifactId>
</dependency>
```

### Configuration
```java
@Bean
public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
    return builder.routes()
        .route("user_service", r -> r
            .path("/api/users/**")
            .filters(f -> f
                .stripPrefix(1)
                .addRequestHeader("X-Gateway", "API-Gateway"))
            .uri("lb://user-service"))
        .build();
}
```

### Global Filters
- `LoggingGlobalFilter` - Request/response logging
- `AuthenticationGlobalFilter` - JWT validation
- `RateLimitingGlobalFilter` - Rate limiting
- `CORSGlobalFilter` - CORS headers

---

## Configuration Server Pattern

**File:** `ConfigurationServerPattern.java`

### Purpose
Centralize configuration management for all microservices.

### Key Features
- Git-backed configuration storage
- Environment-specific configurations
- Dynamic refresh without restart
- Configuration encryption
- Profile-based configs (dev, test, prod)

### Dependencies
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-config-server</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-config-client</artifactId>
</dependency>
```

### Server Configuration
```yaml
server:
  port: 8888
spring:
  cloud:
    config:
      server:
        git:
          uri: https://github.com/yourorg/config-repo
          default-label: main
          search-paths: config
```

### Client Configuration
```yaml
spring:
  config:
    import: optional:configserver:http://localhost:8888
  application:
    name: my-service
  profiles:
    active: dev
```

### REST Endpoints
- `GET /api/config/{application}/{profile}` - Get configuration
- `POST /api/config/{application}/{profile}` - Update configuration
- `POST /api/config/encrypt` - Encrypt value
- `POST /api/config/decrypt` - Decrypt value
- `GET /api/config/changes` - View change history

---

## Distributed Tracing Pattern

**File:** `DistributedTracingPattern.java`

### Purpose
Track requests as they flow through multiple microservices.

### Key Features
- Correlation ID propagation
- Trace and span creation
- Trace context propagation
- Integration with Zipkin
- Custom tags and logs

### Dependencies
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-sleuth</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-sleuth-zipkin</artifactId>
</dependency>
```

### Configuration
```yaml
spring:
  sleuth:
    sampler:
      probability: 0.1  # Sample 10% of requests
  zipkin:
    base-url: http://localhost:9411
    enabled: true
```

### Trace Components
- **Trace ID** - Unique across entire request
- **Span ID** - Unique per service call
- **Parent Span ID** - Link to calling service
- **Tags** - Metadata (user.id, order.id)
- **Logs** - Events during span execution

### REST Endpoints
- `POST /api/tracing/orders/{orderId}` - Create traced order
- `GET /api/tracing/traces/{traceId}` - View trace details
- `GET /api/tracing/traces` - List all traces

---

## Centralized Logging Pattern

**File:** `CentralizedLoggingPattern.java`

### Purpose
Aggregate logs from all services into a centralized location for analysis.

### Key Features
- Structured logging (JSON format)
- MDC (Mapped Diagnostic Context)
- Correlation ID tracking
- ELK stack integration
- Log aggregation and searching

### Dependencies
```xml
<dependency>
    <groupId>net.logstash.logback</groupId>
    <artifactId>logstash-logback-encoder</artifactId>
</dependency>
```

### Logback Configuration
```xml
<encoder class="net.logstash.logback.encoder.LogstashEncoder">
    <includeMdcKeyName>traceId</includeMdcKeyName>
    <includeMdcKeyName>spanId</includeMdcKeyName>
    <includeMdcKeyName>userId</includeMdcKeyName>
</encoder>
```

### MDC Usage
```java
MDC.put("userId", "12345");
MDC.put("requestId", UUID.randomUUID().toString());
logger.info("Processing request"); // Includes MDC context
MDC.clear();
```

### REST Endpoints
- `POST /api/logging/users` - Create user with logging
- `GET /api/logging/logs/service/{serviceName}` - Logs by service
- `GET /api/logging/logs/trace/{traceId}` - Logs by trace
- `GET /api/logging/logs/stats` - Log statistics

---

## Bulkhead Pattern

**File:** `BulkheadPattern.java`

### Purpose
Isolate resources to prevent resource exhaustion and cascading failures.

### Key Features
- Semaphore-based bulkhead (concurrent call limits)
- Thread pool bulkhead (separate thread pools)
- Resource isolation per service
- Queue management
- Metrics tracking

### Dependencies
```xml
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-bulkhead</artifactId>
</dependency>
```

### Configuration
```java
// Semaphore Bulkhead
BulkheadConfig.custom()
    .maxConcurrentCalls(10)
    .maxWaitDuration(Duration.ofMillis(500))
    .build();

// Thread Pool Bulkhead
ThreadPoolBulkheadConfig.custom()
    .maxThreadPoolSize(10)
    .coreThreadPoolSize(5)
    .queueCapacity(20)
    .keepAliveDuration(Duration.ofMillis(20))
    .build();
```

### Bulkhead Types
1. **Semaphore** - Limits concurrent calls, lightweight
2. **Thread Pool** - Separate thread pool with queue

### REST Endpoints
- `POST /api/bulkhead/user/{userId}` - Test semaphore bulkhead
- `POST /api/bulkhead/payment/{paymentId}` - Test payment bulkhead
- `POST /api/bulkhead/async/{requestId}` - Test thread pool bulkhead
- `GET /api/bulkhead/metrics` - View bulkhead metrics

---

## Retry with Exponential Backoff Pattern

**File:** `RetryExponentialBackoffPattern.java`

### Purpose
Automatically retry failed operations with increasing delays between attempts.

### Key Features
- Exponential backoff algorithm
- Jitter to prevent thundering herd
- Configurable max attempts
- Different retry policies
- Metrics tracking

### Dependencies
```xml
<dependency>
    <groupId>org.springframework.retry</groupId>
    <artifactId>spring-retry</artifactId>
</dependency>
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-retry</artifactId>
</dependency>
```

### Configuration
```java
@Retryable(
    value = {TransientException.class},
    maxAttempts = 5,
    backoff = @Backoff(
        delay = 1000,      // Initial delay: 1 second
        multiplier = 2,    // Double each time
        maxDelay = 10000   // Max delay: 10 seconds
    )
)
```

### Backoff Strategies
1. **Fixed Delay** - 1s, 1s, 1s, 1s
2. **Exponential** - 1s, 2s, 4s, 8s, 16s
3. **Exponential with Jitter** - 1s±0.5s, 2s±1s, 4s±2s

### REST Endpoints
- `POST /api/retry/exponential/{operation}` - Test exponential backoff
- `POST /api/retry/fixed/{operation}` - Test fixed delay
- `GET /api/retry/metrics` - View retry metrics
- `GET /api/retry/metrics/{operation}` - Metrics for specific operation

---

## Rate Limiting Pattern

**File:** `RateLimitingPattern.java`

### Purpose
Control the rate of requests to prevent abuse and ensure fair resource allocation.

### Key Features
- Token bucket algorithm
- Sliding window algorithm
- Fixed window algorithm
- Per-client rate limiting
- Quota management

### Dependencies
```xml
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-ratelimiter</artifactId>
</dependency>
```

### Configuration
```java
RateLimiterConfig.custom()
    .limitForPeriod(50)                     // 50 requests
    .limitRefreshPeriod(Duration.ofSeconds(1))  // per second
    .timeoutDuration(Duration.ofMillis(500))
    .build();
```

### Algorithms
1. **Token Bucket** - Bucket capacity + refill rate
2. **Sliding Window** - Accurate, memory-intensive
3. **Fixed Window** - Simple, allows boundary bursts

### REST Endpoints
- `GET /api/ratelimit/token-bucket?clientId=user1` - Test token bucket
- `GET /api/ratelimit/sliding-window?clientId=user1` - Test sliding window
- `GET /api/ratelimit/fixed-window?clientId=user1` - Test fixed window
- `GET /api/ratelimit/metrics` - View metrics

---

## Service Mesh Pattern

**File:** `ServiceMeshPattern.java`

### Purpose
Provide infrastructure layer for service-to-service communication with observability and security.

### Key Features
- Sidecar proxy pattern
- Traffic routing and splitting
- Circuit breaking at proxy level
- Mutual TLS (mTLS)
- A/B testing support
- Observability (metrics, traces, logs)

### Architecture
```
Control Plane (Istio/Linkerd)
    |
    |--- Configure proxies
    |--- Distribute policies
    |--- Aggregate telemetry
    |
Data Plane (Sidecar Proxies)
    |
    [Service A] <--> [Envoy Proxy A]
                          |
                     Service Mesh
                          |
    [Service B] <--> [Envoy Proxy B]
```

### Popular Service Meshes
- **Istio** - Google/IBM, feature-rich
- **Linkerd** - CNCF, lightweight
- **Consul Connect** - HashiCorp
- **AWS App Mesh** - AWS-native

### Traffic Management
```yaml
# A/B Testing: 90% v1, 10% v2
apiVersion: networking.istio.io/v1alpha3
kind: VirtualService
metadata:
  name: user-service
spec:
  hosts:
  - user-service
  http:
  - match:
    - headers:
        user-group:
          exact: beta
    route:
    - destination:
        host: user-service
        subset: v2
  - route:
    - destination:
        host: user-service
        subset: v1
      weight: 90
    - destination:
        host: user-service
        subset: v2
      weight: 10
```

### REST Endpoints
- `GET /api/mesh/users/{userId}` - Call user service through mesh
- `GET /api/mesh/orders/{orderId}` - Call order service
- `GET /api/mesh/metrics` - View proxy metrics
- `POST /api/mesh/routing/{serviceName}` - Update traffic routing

---

## Production Best Practices

### Service Discovery
- Use health checks to ensure service availability
- Implement graceful shutdown for proper deregistration
- Set appropriate heartbeat intervals
- Use zone-aware routing for multi-region deployments

### Circuit Breaker
- Set appropriate failure thresholds (50-80%)
- Use slow call detection for latency issues
- Implement meaningful fallback methods
- Monitor circuit breaker metrics

### Load Balancer
- Choose strategy based on use case (round-robin for stateless, IP hash for sticky sessions)
- Monitor server health and remove unhealthy instances
- Implement connection pooling
- Use weighted balancing for gradual rollouts

### API Gateway
- Implement proper authentication and authorization
- Use rate limiting to prevent abuse
- Add request/response logging for debugging
- Implement circuit breakers for backend services
- Enable CORS only for trusted origins

### Configuration Server
- Use Git for version control and audit trail
- Encrypt sensitive properties
- Implement proper access control
- Use profiles for environment separation
- Enable refresh scope for dynamic updates

### Distributed Tracing
- Sample traces (10% in production)
- Add meaningful tags and logs
- Use trace IDs in logs for correlation
- Monitor trace collection overhead
- Set appropriate retention policies

### Centralized Logging
- Use structured logging (JSON)
- Include correlation IDs
- Set appropriate log levels
- Implement log rotation and retention
- Use MDC for contextual information

### Bulkhead
- Isolate critical services
- Size thread pools based on capacity planning
- Monitor queue sizes
- Implement fallbacks for bulkhead full scenarios
- Use semaphore bulkhead for fast operations

### Retry
- Only retry transient errors
- Use exponential backoff with jitter
- Set reasonable max attempts (3-5)
- Implement idempotency for retried operations
- Monitor retry success rates

### Rate Limiting
- Set limits based on capacity
- Return appropriate HTTP status (429)
- Include Retry-After header
- Implement different tiers for different clients
- Monitor rejection rates

### Service Mesh
- Start with observability features
- Gradually enable mTLS
- Use canary deployments for testing
- Monitor sidecar resource usage
- Implement proper retry and timeout policies

---

## Running the Examples

### Prerequisites
```bash
# Java 17+
java -version

# Maven 3.8+
mvn -version
```

### Build
```bash
mvn clean install
```

### Run Individual Patterns
```bash
# Service Discovery
java ServiceDiscoveryPattern

# Circuit Breaker
java CircuitBreakerPattern

# Load Balancer
java LoadBalancerPattern

# API Gateway
java APIGatewayPattern

# Configuration Server
java ConfigurationServerPattern

# Distributed Tracing
java DistributedTracingPattern

# Centralized Logging
java CentralizedLoggingPattern

# Bulkhead
java BulkheadPattern

# Retry
java RetryExponentialBackoffPattern

# Rate Limiting
java RateLimitingPattern

# Service Mesh
java ServiceMeshPattern
```

---

## Testing Patterns

### Service Discovery
```bash
# Register service
curl -X POST http://localhost:8080/api/discovery/services/user-service/register

# Discover services
curl http://localhost:8080/api/discovery/services/user-service/instances
```

### Circuit Breaker
```bash
# Test circuit breaker
curl http://localhost:8080/api/circuit/call-service

# Check state
curl http://localhost:8080/api/circuit/state

# Manually open circuit
curl -X POST http://localhost:8080/api/circuit/state/open
```

### Load Balancer
```bash
# Round robin
curl "http://localhost:8080/api/lb/call-service?strategy=roundRobin"

# Weighted
curl "http://localhost:8080/api/lb/call-service?strategy=weighted"

# View stats
curl http://localhost:8080/api/lb/stats
```

### Rate Limiting
```bash
# Test token bucket (run multiple times to trigger limit)
for i in {1..150}; do
  curl http://localhost:8080/api/ratelimit/token-bucket
done

# View metrics
curl http://localhost:8080/api/ratelimit/metrics
```

---

## Monitoring and Observability

### Metrics to Monitor
- **Service Discovery**: Service count, instance count, health check failures
- **Circuit Breaker**: State, failure rate, slow call rate
- **Load Balancer**: Request distribution, server health, response times
- **API Gateway**: Request count, latency, error rate
- **Configuration**: Refresh count, fetch errors
- **Tracing**: Trace count, span count, sampling rate
- **Logging**: Log rate, error count
- **Bulkhead**: Queue size, rejection rate, thread pool usage
- **Retry**: Retry count, success rate, backoff duration
- **Rate Limiting**: Rejection rate, quota usage
- **Service Mesh**: Proxy CPU/memory, request latency, error rate

### Tools
- **Prometheus** - Metrics collection
- **Grafana** - Visualization
- **Zipkin/Jaeger** - Distributed tracing
- **ELK Stack** - Centralized logging
- **Kiali** - Service mesh observability (Istio)

---

## Additional Resources

### Documentation
- [Spring Cloud Documentation](https://spring.io/projects/spring-cloud)
- [Resilience4j Documentation](https://resilience4j.readme.io/)
- [Istio Documentation](https://istio.io/latest/docs/)
- [Netflix OSS](https://netflix.github.io/)

### Books
- "Spring Microservices in Action" by John Carnell
- "Building Microservices" by Sam Newman
- "Release It!" by Michael Nygard

### Articles
- [The Twelve-Factor App](https://12factor.net/)
- [Microservices Patterns](https://microservices.io/patterns/)

---

## License

These patterns are provided as educational examples. Feel free to use and modify them for your projects.

## Author

Spring Patterns - Comprehensive Cloud Pattern Implementations

---

**Note:** These are demonstration files showing pattern implementations. In production, ensure proper dependency management, security configurations, and testing before deployment.
