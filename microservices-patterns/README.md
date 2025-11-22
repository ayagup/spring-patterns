# Microservices Patterns in Spring Framework

This directory contains comprehensive examples of Microservices Patterns used in Spring Cloud and modern distributed systems.

## Table of Contents

### Service Discovery & Registry
1. [Service Registry Pattern](#1-service-registry-pattern)
2. [Eureka Discovery Pattern](#7-eureka-discovery-pattern)
3. [Consul Discovery Pattern](#8-consul-discovery-pattern)
4. [Zookeeper Discovery Pattern](#9-zookeeper-discovery-pattern)

### Load Balancing
5. [Client-Side Load Balancing Pattern](#2-client-side-load-balancing-pattern)
6. [Server-Side Load Balancing Pattern](#3-server-side-load-balancing-pattern)
7. [Ribbon Client Pattern](#6-ribbon-client-pattern)

### Communication
8. [Service-to-Service Communication Pattern](#4-service-to-service-communication-pattern)
9. [Feign Client Pattern](#5-feign-client-pattern)

### Configuration
10. [Configuration Management Pattern](#10-configuration-management-pattern)
11. [Externalized Configuration Pattern](#11-externalized-configuration-pattern)
12. [Config Server Pattern](#12-config-server-pattern)
13. [Config Client Pattern](#13-config-client-pattern)

### Service Mesh & Proxies
14. [Service Mesh Integration Pattern](#14-service-mesh-integration-pattern)
15. [Sidecar Pattern](#15-sidecar-pattern)
16. [Ambassador Pattern](#16-ambassador-pattern)

### Integration & Migration
17. [Anti-Corruption Layer Pattern](#17-anti-corruption-layer-pattern)
18. [Backend for Frontend (BFF) Pattern](#18-backend-for-frontend-bff-pattern)
19. [Strangler Fig Pattern](#19-strangler-fig-pattern)

### Data Management
20. [Database per Service Pattern](#20-database-per-service-pattern)
21. [Shared Database Pattern](#21-shared-database-pattern)

### Orchestration & Choreography
22. [Event-Driven Architecture Pattern](#22-event-driven-architecture-pattern)
23. [Choreography Pattern](#23-choreography-pattern)
24. [Orchestration Pattern](#24-orchestration-pattern)

---

## Pattern Descriptions

### 1. Service Registry Pattern

**File**: `ServiceRegistryPattern.java`

**Purpose**: Central service discovery and registration for microservices.

**Key Components**:
- `ServiceRegistry`: Manages service registration and discovery
- `ServiceInstance`: Represents a running service instance
- `Health Check`: Monitors service availability
- `Heartbeat Mechanism`: Keeps services alive in registry

**Use Cases**:
- Dynamic service discovery
- Load balancing
- Fault tolerance
- Auto-scaling
- Service mesh

**Example**:
```java
// Register a service
ServiceInstance instance = new ServiceInstance(
    "user-service", "192.168.1.10", 8080, "v1.0");
registry.register(instance);

// Discover services
List<ServiceInstance> instances = registry.getInstances("user-service");

// Health check
boolean healthy = registry.isHealthy(instanceId);
```

**REST Endpoints**:
- `POST /registry/register` - Register service instance
- `DELETE /registry/deregister/{instanceId}` - Deregister instance
- `GET /registry/instances/{serviceName}` - Get service instances
- `GET /registry/services` - Get all services
- `POST /registry/heartbeat/{instanceId}` - Send heartbeat

---

### 2. Client-Side Load Balancing Pattern

**File**: `ClientSideLoadBalancingPattern.java`

**Purpose**: Distribute requests across service instances on the client side.

**Key Components**:
- `LoadBalancer`: Client-side load balancing logic
- `RoundRobinRule`: Distributes requests evenly
- `RandomRule`: Random server selection
- `WeightedResponseTimeRule`: Based on response times

**Load Balancing Strategies**:
1. **Round Robin**: Distributes requests sequentially
2. **Random**: Random selection
3. **Weighted Response Time**: Chooses fastest servers
4. **Least Connections**: Chooses least busy servers
5. **Zone Aware**: Prefers servers in same zone

**Example**:
```java
// Add servers
loadBalancer.addServer(new Server("service-1", "192.168.1.10", 8080));
loadBalancer.addServer(new Server("service-1", "192.168.1.11", 8080));

// Choose server
Server server = loadBalancer.chooseServer("service-1");

// Set custom rule
loadBalancer.setRule("service-1", new WeightedResponseTimeRule());
```

**Benefits**:
- No single point of failure
- Better performance (local decision)
- Reduced latency
- Dynamic server list updates

---

### 3. Server-Side Load Balancing Pattern

**Purpose**: Load balancing performed by a dedicated load balancer server.

**Components**:
- Reverse proxy (Nginx, HAProxy)
- Health checks
- SSL termination
- Request routing

**Advantages**:
- Centralized control
- SSL offloading
- Advanced routing rules
- Better security

**Example Configuration**:
```nginx
upstream backend {
    least_conn;
    server 192.168.1.10:8080 weight=3;
    server 192.168.1.11:8080;
    server 192.168.1.12:8080 backup;
}

server {
    listen 80;
    location / {
        proxy_pass http://backend;
    }
}
```

---

### 4. Service-to-Service Communication Pattern

**Communication Types**:
1. **Synchronous**: REST, gRPC
2. **Asynchronous**: Messaging (RabbitMQ, Kafka)
3. **Hybrid**: Combination of both

**Key Concerns**:
- Circuit breakers
- Timeouts
- Retries
- Fallbacks
- Service discovery
- Load balancing

**REST Communication Example**:
```java
@Service
class OrderService {
    private final RestTemplate restTemplate;
    
    public User getUser(Long userId) {
        return restTemplate.getForObject(
            "http://user-service/users/" + userId,
            User.class
        );
    }
}
```

**Messaging Example**:
```java
@Service
class OrderEventPublisher {
    private final RabbitTemplate rabbitTemplate;
    
    public void publishOrderCreated(Order order) {
        rabbitTemplate.convertAndSend(
            "orders.exchange", 
            "order.created", 
            order
        );
    }
}
```

---

### 5. Feign Client Pattern

**Purpose**: Declarative REST client for service-to-service communication.

**Features**:
- Declarative syntax
- Integration with Ribbon
- Circuit breaker support
- Custom encoders/decoders
- Request/response logging

**Example**:
```java
@FeignClient(name = "user-service", fallback = UserClientFallback.class)
public interface UserClient {
    
    @GetMapping("/users/{id}")
    User getUser(@PathVariable Long id);
    
    @PostMapping("/users")
    User createUser(@RequestBody User user);
}

@Component
class UserClientFallback implements UserClient {
    @Override
    public User getUser(Long id) {
        return new User(); // Default user
    }
}
```

**Configuration**:
```yaml
feign:
  client:
    config:
      default:
        connectTimeout: 5000
        readTimeout: 5000
  circuitbreaker:
    enabled: true
```

---

### 6. Ribbon Client Pattern

**Purpose**: Client-side load balancing library.

**Features**:
- Multiple load balancing rules
- Server list management
- Ping strategy
- Integration with Eureka
- Retry logic

**Example**:
```java
@Configuration
class RibbonConfiguration {
    
    @Bean
    public IRule ribbonRule() {
        return new WeightedResponseTimeRule();
    }
    
    @Bean
    public IPing ribbonPing() {
        return new PingUrl();
    }
}
```

---

### 7. Eureka Discovery Pattern

**Purpose**: Netflix Eureka-based service discovery.

**Components**:
- Eureka Server: Service registry
- Eureka Client: Service registration
- Self-preservation mode
- Zone awareness

**Server Configuration**:
```yaml
eureka:
  server:
    enable-self-preservation: true
    eviction-interval-timer-in-ms: 60000
```

**Client Configuration**:
```java
@EnableEurekaClient
@SpringBootApplication
public class UserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
```

---

### 8. Consul Discovery Pattern

**Purpose**: HashiCorp Consul-based service discovery.

**Features**:
- Service registration
- Health checks
- Key-Value store
- Multi-datacenter support
- DNS interface

**Example**:
```yaml
spring:
  cloud:
    consul:
      host: localhost
      port: 8500
      discovery:
        enabled: true
        health-check-path: /actuator/health
        health-check-interval: 10s
```

---

### 9. Zookeeper Discovery Pattern

**Purpose**: Apache Zookeeper-based service discovery.

**Features**:
- Distributed coordination
- Ephemeral nodes
- Watchers
- Leader election

**Example**:
```yaml
spring:
  cloud:
    zookeeper:
      connect-string: localhost:2181
      discovery:
        enabled: true
        root: /services
```

---

### 10. Configuration Management Pattern

**Purpose**: Centralized configuration management.

**Strategies**:
1. Environment variables
2. Configuration files
3. Configuration server
4. Distributed configuration

**Best Practices**:
- Externalize all configuration
- Use profiles for environments
- Encrypt sensitive data
- Version control configurations
- Support dynamic refresh

---

### 11. Externalized Configuration Pattern

**Purpose**: Separate configuration from code.

**Sources**:
- Application.properties/yml
- Environment variables
- Command-line arguments
- Config server
- Cloud configuration services

**Example**:
```java
@Configuration
@ConfigurationProperties(prefix = "app")
public class AppConfig {
    private String name;
    private String version;
    private Database database;
    
    // Getters and setters
}
```

```yaml
app:
  name: user-service
  version: 1.0.0
  database:
    url: jdbc:postgresql://localhost:5432/users
    username: ${DB_USER}
    password: ${DB_PASSWORD}
```

---

### 12. Config Server Pattern

**Purpose**: Centralized configuration server.

**Features**:
- Git backend
- Encryption/decryption
- Multiple environments
- Refresh capabilities
- REST API

**Server Setup**:
```java
@EnableConfigServer
@SpringBootApplication
public class ConfigServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
    }
}
```

**Configuration**:
```yaml
spring:
  cloud:
    config:
      server:
        git:
          uri: https://github.com/myorg/config-repo
          searchPaths: '{application}'
```

---

### 13. Config Client Pattern

**Purpose**: Client for fetching configuration from config server.

**Features**:
- Bootstrap configuration
- Auto-refresh
- Retry logic
- Failfast option

**Example**:
```yaml
spring:
  application:
    name: user-service
  cloud:
    config:
      uri: http://config-server:8888
      fail-fast: true
      retry:
        max-attempts: 6
```

---

### 14. Service Mesh Integration Pattern

**Purpose**: Infrastructure layer for service-to-service communication.

**Features** (Istio example):
- Traffic management
- Security (mTLS)
- Observability
- Policy enforcement

**Benefits**:
- No code changes required
- Consistent policies
- Advanced traffic control
- Better observability

---

### 15. Sidecar Pattern

**Purpose**: Auxiliary container alongside main service.

**Use Cases**:
- Logging
- Monitoring
- Proxy
- Service mesh agent

**Example** (Docker Compose):
```yaml
services:
  app:
    image: myapp:latest
    
  envoy-sidecar:
    image: envoyproxy/envoy:latest
    volumes:
      - ./envoy.yaml:/etc/envoy/envoy.yaml
```

---

### 16. Ambassador Pattern

**Purpose**: Client-side proxy for external services.

**Responsibilities**:
- Connection pooling
- Retry logic
- Circuit breaking
- Monitoring
- Logging

---

### 17. Anti-Corruption Layer Pattern

**Purpose**: Translate between different domain models.

**Use Cases**:
- Legacy system integration
- External API integration
- Domain isolation

**Example**:
```java
@Service
class LegacySystemAdapter {
    
    public ModernOrder translateOrder(LegacyOrder legacyOrder) {
        ModernOrder order = new ModernOrder();
        order.setOrderId(legacyOrder.getOrdNum());
        order.setCustomerId(legacyOrder.getCustId());
        // More translations...
        return order;
    }
}
```

---

### 18. Backend for Frontend (BFF) Pattern

**Purpose**: Separate backend for each frontend type.

**Benefits**:
- Optimized for specific clients
- Reduced chattiness
- Better performance
- Independent scaling

**Example**:
```java
// Mobile BFF
@RestController
@RequestMapping("/mobile-api")
class MobileBFF {
    public DashboardData getDashboard() {
        // Aggregated data optimized for mobile
    }
}

// Web BFF
@RestController
@RequestMapping("/web-api")
class WebBFF {
    public DashboardData getDashboard() {
        // Aggregated data optimized for web
    }
}
```

---

### 19. Strangler Fig Pattern

**Purpose**: Incrementally migrate from legacy to new system.

**Steps**:
1. Identify functionality to migrate
2. Create new service
3. Route traffic to new service
4. Deprecate legacy component
5. Remove legacy code

**Example**:
```java
@Component
class RoutingFilter implements Filter {
    public void doFilter(ServletRequest request, ...) {
        if (useNewService(request)) {
            forwardToNewService(request);
        } else {
            forwardToLegacyService(request);
        }
    }
}
```

---

### 20. Database per Service Pattern

**Purpose**: Each service has its own private database.

**Benefits**:
- Service independence
- Technology diversity
- Easier scaling
- Better isolation

**Challenges**:
- Distributed transactions
- Data consistency
- Query complexity

---

### 21. Shared Database Pattern

**Purpose**: Multiple services share the same database.

**Advantages**:
- ACID transactions
- Simpler queries
- Data consistency

**Disadvantages**:
- Tight coupling
- Scalability issues
- Schema changes affect multiple services

---

### 22. Event-Driven Architecture Pattern

**Purpose**: Services communicate through events.

**Components**:
- Event producers
- Event consumers
- Event bus/broker
- Event store

**Example**:
```java
// Publisher
@Service
class OrderService {
    @Autowired
    private ApplicationEventPublisher publisher;
    
    public void createOrder(Order order) {
        // Create order
        publisher.publishEvent(new OrderCreatedEvent(order));
    }
}

// Subscriber
@Component
class InventoryService {
    @EventListener
    public void handleOrderCreated(OrderCreatedEvent event) {
        // Update inventory
    }
}
```

---

### 23. Choreography Pattern

**Purpose**: Decentralized service coordination through events.

**Characteristics**:
- No central coordinator
- Services react to events
- Loose coupling
- Eventual consistency

**Example**:
```
Order Service -> OrderCreated Event
  -> Inventory Service (reserves items)
    -> ItemsReserved Event
  -> Payment Service (processes payment)
    -> PaymentProcessed Event
  -> Shipping Service (ships order)
```

---

### 24. Orchestration Pattern

**Purpose**: Centralized service coordination.

**Characteristics**:
- Central orchestrator
- Explicit workflow
- Easier to understand
- Single point of control

**Example**:
```java
@Service
class OrderOrchestrator {
    public void processOrder(Order order) {
        // Step 1: Reserve inventory
        inventoryService.reserve(order.getItems());
        
        // Step 2: Process payment
        paymentService.process(order.getPayment());
        
        // Step 3: Ship order
        shippingService.ship(order);
        
        // Step 4: Send notification
        notificationService.notify(order.getCustomerId());
    }
}
```

---

## Dependencies

```xml
<dependencies>
    <!-- Spring Boot -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <!-- Spring Cloud -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
    </dependency>
    
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-openfeign</artifactId>
    </dependency>
    
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-config</artifactId>
    </dependency>
    
    <!-- Load Balancing -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-loadbalancer</artifactId>
    </dependency>
    
    <!-- Circuit Breaker -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-circuitbreaker-resilience4j</artifactId>
    </dependency>
    
    <!-- Messaging -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-stream-rabbit</artifactId>
    </dependency>
</dependencies>
```

## Running Examples

```bash
# Start Eureka Server
java -jar eureka-server.jar

# Start Config Server
java -jar config-server.jar

# Start Microservices
java -jar user-service.jar
java -jar order-service.jar
java -jar inventory-service.jar
```

## Best Practices

### Service Discovery
- Use health checks
- Implement graceful shutdown
- Configure proper timeouts
- Use multiple registry instances

### Load Balancing
- Choose appropriate algorithm
- Monitor server health
- Configure retries
- Implement circuit breakers

### Configuration
- Externalize all config
- Use encryption for secrets
- Version control configurations
- Support dynamic refresh

### Communication
- Use timeouts
- Implement retries
- Add circuit breakers
- Log all requests
- Use correlation IDs

### Data Management
- Choose appropriate pattern
- Handle eventual consistency
- Use sagas for transactions
- Implement compensation logic

## Common Challenges & Solutions

### Challenge 1: Distributed Transactions
**Solution**: Use Saga pattern (choreography or orchestration)

### Challenge 2: Service Discovery
**Solution**: Use service registry (Eureka, Consul, Zookeeper)

### Challenge 3: Configuration Management
**Solution**: Use config server with version control

### Challenge 4: Fault Tolerance
**Solution**: Implement circuit breakers, retries, timeouts

### Challenge 5: Data Consistency
**Solution**: Event sourcing, eventual consistency, CQRS

## Testing Strategies

```java
// Integration Testing
@SpringBootTest
@AutoConfigureWireMock
class ServiceIntegrationTest {
    
    @Test
    void testServiceCommunication() {
        // Stub external service
        stubFor(get(urlEqualTo("/users/1"))
            .willReturn(aResponse()
                .withBody("{\"id\":1,\"name\":\"John\"}")));
        
        // Test
        User user = userClient.getUser(1L);
        assertThat(user.getName()).isEqualTo("John");
    }
}

// Contract Testing
@AutoConfigureStubRunner(
    ids = "com.example:user-service:+:stubs:8080",
    stubsMode = StubRunnerProperties.StubsMode.LOCAL
)
class ContractTest {
    // Contract verification tests
}
```

## Monitoring & Observability

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
  tracing:
    sampling:
      probability: 1.0
```

## Security Considerations

1. **Service-to-Service Authentication**
   - Use mTLS
   - JWT tokens
   - API keys

2. **API Gateway Security**
   - Rate limiting
   - Request validation
   - CORS configuration

3. **Secret Management**
   - Use Vault
   - Encrypt sensitive data
   - Rotate credentials

## Production Checklist

- [ ] Health checks configured
- [ ] Metrics exposed
- [ ] Distributed tracing enabled
- [ ] Circuit breakers configured
- [ ] Retries and timeouts set
- [ ] Load balancing configured
- [ ] Service discovery working
- [ ] Configuration externalized
- [ ] Logging centralized
- [ ] Security implemented
- [ ] Monitoring dashboards ready
- [ ] Alerts configured
- [ ] Documentation complete

## References

- [Spring Cloud Documentation](https://spring.io/projects/spring-cloud)
- [Microservices Patterns by Chris Richardson](https://microservices.io/patterns/)
- [Building Microservices by Sam Newman](https://samnewman.io/books/building_microservices/)
- [Spring Cloud Netflix](https://spring.io/projects/spring-cloud-netflix)
- [Service Mesh](https://istio.io/)

## License

Educational purposes.

## Author

Spring Patterns Team
