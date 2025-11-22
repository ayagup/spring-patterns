# Spring Monitoring and Management Patterns

This directory contains comprehensive demonstrations of Spring Boot Monitoring and Management patterns using Spring Boot 3.x, Micrometer, and Spring Boot Actuator.

## 📋 Overview

Monitoring and management are critical aspects of production applications. Spring Boot provides powerful tools through Actuator, JMX, and Micrometer to monitor, manage, and observe your applications in real-time.

This collection demonstrates 9 essential monitoring patterns with complete, runnable implementations.

## 🎯 Patterns Included

| Pattern | File | Description | Key Features |
|---------|------|-------------|--------------|
| **JMX Pattern** | `JMXPattern.java` | JMX MBean exposure and remote management | MBean registration, JConsole integration, remote monitoring |
| **MBean Pattern** | `MBeanPattern.java` | Standard, Dynamic, and Model MBeans | Three MBean types, operations, attributes, metadata |
| **Actuator Pattern** | `ActuatorPattern.java` | Spring Boot Actuator integration | Health, info, metrics endpoints, custom indicators |
| **Health Indicator** | `HealthIndicatorPattern.java` | Custom health checks | Database, API, disk, memory, reactive health checks |
| **Metrics Pattern** | `MetricsPattern.java` | Micrometer metrics collection | Counter, Timer, Gauge, Distribution Summary |
| **Endpoint Pattern** | `EndpointPattern.java` | Custom actuator endpoints | @ReadOperation, @WriteOperation, @DeleteOperation |
| **Info Contributor** | `InfoContributorPattern.java` | Application metadata | Build info, Git info, team info, dependencies |
| **Auditing Pattern** | `AuditingPattern.java` | Audit events and entity tracking | JPA auditing, security events, change tracking |
| **Tracing Pattern** | `TracingPattern.java` | Distributed tracing | Spans, trace context, Zipkin/Jaeger export |

## 🚀 Quick Start

### Prerequisites

```xml
<!-- Core Dependencies -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>

<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-core</artifactId>
</dependency>

<!-- For Tracing -->
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-tracing-bridge-brave</artifactId>
</dependency>

<dependency>
    <groupId>io.zipkin.reporter2</groupId>
    <artifactId>zipkin-reporter-brave</artifactId>
</dependency>

<!-- For JMX -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-jmx</artifactId>
</dependency>
```

### Basic Configuration

```properties
# application.properties

# Actuator Endpoints
management.endpoints.web.exposure.include=*
management.endpoint.health.show-details=always
management.endpoint.health.show-components=always

# Metrics
management.metrics.enable.jvm=true
management.metrics.enable.process=true
management.metrics.enable.system=true

# Tracing
management.tracing.sampling.probability=1.0
management.zipkin.tracing.endpoint=http://localhost:9411/api/v2/spans

# JMX
spring.jmx.enabled=true
management.endpoints.jmx.exposure.include=*

# Info Endpoint
management.info.env.enabled=true
management.info.build.enabled=true
management.info.git.enabled=true
management.info.git.mode=full

# Auditing
spring.jpa.properties.hibernate.enable_lazy_load_no_trans=true
```

## 📚 Pattern Details

### 1. JMX Pattern (`JMXPattern.java`)

**Purpose**: Expose application metrics and management operations via JMX for monitoring with JConsole or VisualVM.

**Key Components**:
- `@EnableMBeanExport` - Enable Spring JMX support
- `@ManagedResource` - Mark class as MBean
- `@ManagedOperation` - Expose management operations
- `@ManagedAttribute` - Expose attributes

**Use Cases**:
- Remote monitoring and management
- Integration with enterprise monitoring tools
- Runtime configuration changes
- Performance metric exposure

---

### 2. MBean Pattern (`MBeanPattern.java`)

**Purpose**: Demonstrate three types of MBeans with different use cases.

**MBean Types**:
1. **Standard MBean**: Static interface-based
2. **Dynamic MBean**: Runtime attribute registration
3. **Model MBean**: Advanced metadata and descriptors

**When to Use**:
- Standard: Simple, compile-time known operations
- Dynamic: Runtime-defined attributes and operations
- Model: Complex metadata and advanced scenarios

---

### 3. Actuator Pattern (`ActuatorPattern.java`)

**Purpose**: Leverage Spring Boot Actuator for comprehensive application monitoring.

**Built-in Endpoints**:
- `/actuator/health` - Application health status
- `/actuator/metrics` - Application metrics
- `/actuator/info` - Application information
- `/actuator/env` - Environment properties
- `/actuator/loggers` - Logger configuration
- `/actuator/threaddump` - Thread dump
- `/actuator/heapdump` - Heap dump

---

### 4. Health Indicator Pattern (`HealthIndicatorPattern.java`)

**Purpose**: Implement custom health checks for various application components.

**Health Indicators Included**:
1. **Database Health**: Connection pool checks
2. **API Health**: External API availability
3. **Disk Space Health**: Storage monitoring
4. **Memory Health**: JVM memory usage
5. **Custom Service Health**: Business service checks
6. **Reactive Health**: Async health checks

**Health Status**: UP, DOWN, OUT_OF_SERVICE, UNKNOWN

---

### 5. Metrics Pattern (`MetricsPattern.java`)

**Purpose**: Collect application metrics using Micrometer for monitoring and alerting.

**Metric Types**:
1. **Counter** - Monotonically increasing value
2. **Timer** - Measure short-duration latencies
3. **Gauge** - Current value observation
4. **Distribution Summary** - Distribution of events

**Access Metrics**:
- `/actuator/metrics` - List all metrics
- `/actuator/metrics/{name}` - Specific metric details
- `/actuator/prometheus` - Prometheus format

---

### 6. Endpoint Pattern (`EndpointPattern.java`)

**Purpose**: Create custom actuator endpoints for application-specific management operations.

**Endpoint Types**:
- `@Endpoint` - Technology-agnostic (Web + JMX)
- `@WebEndpoint` - Web-only
- `@JmxEndpoint` - JMX-only

**Operations**:
- `@ReadOperation` - GET / JMX read
- `@WriteOperation` - POST / JMX write
- `@DeleteOperation` - DELETE / JMX delete
- `@Selector` - Path variable

---

### 7. Info Contributor Pattern (`InfoContributorPattern.java`)

**Purpose**: Contribute custom information to the `/actuator/info` endpoint.

**Info Contributors**:
1. **Build Info**: Version, name, build time
2. **Git Info**: Branch, commit, author
3. **Environment Info**: Profiles, Java version
4. **Team Info**: Team members, contacts
5. **Features Info**: API versions, capabilities
6. **Dependencies Info**: External services
7. **Runtime Info**: Uptime, memory, threads
8. **Business Info**: Domain metrics

---

### 8. Auditing Pattern (`AuditingPattern.java`)

**Purpose**: Track and audit entity changes, security events, and business operations.

**Features**:
- JPA entity auditing with @CreatedBy, @CreatedDate, @LastModifiedBy, @LastModifiedDate
- Security event auditing (authentication, authorization)
- Business event auditing (orders, payments)
- Custom audit event repository
- Audit event listeners

**Audit Event Types**:
- AUTHENTICATION_SUCCESS / AUTHENTICATION_FAILURE
- AUTHORIZATION_FAILURE
- ORDER_CREATED / ORDER_STATUS_CHANGED
- PAYMENT_PROCESSED
- DATA_ACCESSED / DATA_MODIFIED

---

### 9. Tracing Pattern (`TracingPattern.java`)

**Purpose**: Implement distributed tracing to track requests across microservices.

**Features**:
- Automatic span creation with @NewSpan
- Manual span creation with Tracer
- Span tags and events
- Trace context propagation
- Observation API (metrics + tracing)
- Zipkin/Jaeger export

**Trace Components**:
- **Trace ID**: Unique identifier for entire request flow
- **Span ID**: Identifier for current operation
- **Parent Span ID**: Calling span
- **Tags**: Metadata (user.id, http.method)

**Zipkin Setup**:
```bash
docker run -d -p 9411:9411 openzipkin/zipkin
# Access UI: http://localhost:9411/zipkin
```

---

## 🛠️ Running the Examples

### With Spring Boot

```bash
# Maven
mvn spring-boot:run

# Gradle
./gradlew bootRun
```

### Docker Setup for Monitoring

```bash
# Zipkin for tracing
docker run -d -p 9411:9411 openzipkin/zipkin

# Prometheus for metrics
docker run -d -p 9090:9090 -v prometheus.yml:/etc/prometheus/prometheus.yml prom/prometheus

# Grafana for visualization
docker run -d -p 3000:3000 grafana/grafana
```

## 📊 Monitoring Stack Integration

### Prometheus Configuration

```yaml
# prometheus.yml
scrape_configs:
  - job_name: 'spring-boot-app'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['localhost:8080']
```

### Grafana Dashboard

Import dashboard ID `4701` for Spring Boot 2.x/3.x metrics.

## 🔒 Security Considerations

```java
@Configuration
public class ActuatorSecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
            .requestMatchers("/actuator/health").permitAll()
            .requestMatchers("/actuator/info").permitAll()
            .requestMatchers("/actuator/**").hasRole("ADMIN")
        );
        return http.build();
    }
}
```

## 📈 Production Best Practices

1. **Health Checks**: Implement liveness and readiness probes
2. **Metrics**: Avoid high-cardinality tags, use percentiles for latency
3. **Tracing**: Use sampling in production (0.1 = 10%)
4. **Auditing**: Log all security events, implement retention policies
5. **Endpoints**: Expose only necessary endpoints, use HTTPS

## 📖 Additional Resources

- [Spring Boot Actuator Reference](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [Micrometer Documentation](https://micrometer.io/docs)
- [Distributed Tracing with Spring](https://spring.io/projects/spring-cloud-sleuth)
- [Zipkin Documentation](https://zipkin.io/)
- [Prometheus Documentation](https://prometheus.io/docs/)

---

**Author**: Spring Patterns  
**Version**: 1.0  
**Last Updated**: 2024
