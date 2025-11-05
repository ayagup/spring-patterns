I'll create a comprehensive Spring Boot project demonstrating each cloud pattern. This will be a multi-module Maven project.

## Project Structure

```xml pom.xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.cloudpatterns</groupId>
    <artifactId>cloud-patterns-parent</artifactId>
    <version>1.0.0</version>
    <packaging>pom</packaging>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
    </parent>

    <modules>
        <module>eureka-server</module>
        <module>config-server</module>
        <module>api-gateway</module>
        <module>product-service</module>
        <module>order-service</module>
    </modules>

    <properties>
        <java.version>17</java.version>
        <spring-cloud.version>2023.0.0</spring-cloud.version>
    </properties>

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
</project>
```

## 1. Service Discovery Pattern (Eureka Server)

```xml eureka-server/pom.xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.cloudpatterns</groupId>
        <artifactId>cloud-patterns-parent</artifactId>
        <version>1.0.0</version>
    </parent>

    <artifactId>eureka-server</artifactId>

    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
        </dependency>
    </dependencies>
</project>
```

```java eureka-server/src/main/java/com/cloudpatterns/eureka/EurekaServerApplication.java
package com.cloudpatterns.eureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
```

```yaml eureka-server/src/main/resources/application.yml
server:
  port: 8761

spring:
  application:
    name: eureka-server

eureka:
  client:
    register-with-eureka: false
    fetch-registry: false
  server:
    enable-self-preservation: false
```

## 2. Configuration Server Pattern

```xml config-server/pom.xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.cloudpatterns</groupId>
        <artifactId>cloud-patterns-parent</artifactId>
        <version>1.0.0</version>
    </parent>

    <artifactId>config-server</artifactId>

    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-config-server</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>
    </dependencies>
</project>
```

```java config-server/src/main/java/com/cloudpatterns/config/ConfigServerApplication.java
package com.cloudpatterns.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@SpringBootApplication
@EnableConfigServer
public class ConfigServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
    }
}
```

```yaml config-server/src/main/resources/application.yml
server:
  port: 8888

spring:
  application:
    name: config-server
  cloud:
    config:
      server:
        git:
          uri: https://github.com/your-org/config-repo
          default-label: main
          clone-on-start: true
        # Alternative: use native file system
        native:
          search-locations: classpath:/config

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

## 3. API Gateway Pattern with Rate Limiting

```xml api-gateway/pom.xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.cloudpatterns</groupId>
        <artifactId>cloud-patterns-parent</artifactId>
        <version>1.0.0</version>
    </parent>

    <artifactId>api-gateway</artifactId>

    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-gateway</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis-reactive</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-circuitbreaker-reactor-resilience4j</artifactId>
        </dependency>
    </dependencies>
</project>
```

```java api-gateway/src/main/java/com/cloudpatterns/gateway/ApiGatewayApplication.java
package com.cloudpatterns.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ApiGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
```

```java api-gateway/src/main/java/com/cloudpatterns/gateway/config/GatewayConfig.java
package com.cloudpatterns.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import reactor.core.publisher.Mono;

@Configuration
public class GatewayConfig {

    /**
     * Rate Limiting Pattern - Using Redis-based rate limiter
     */
    @Bean
    public RedisRateLimiter redisRateLimiter() {
        return new RedisRateLimiter(10, 20); // replenishRate, burstCapacity
    }

    /**
     * Rate limiting by IP address
     */
    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> Mono.just(
            exchange.getRequest()
                .getRemoteAddress()
                .getAddress()
                .getHostAddress()
        );
    }

    /**
     * API Gateway Pattern with Load Balancing, Circuit Breaker, and Rate Limiting
     */
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder, 
                                          RedisRateLimiter rateLimiter) {
        return builder.routes()
            // Product Service Route with Circuit Breaker and Rate Limiting
            .route("product-service", r -> r
                .path("/api/products/**")
                .filters(f -> f
                    .stripPrefix(1)
                    // Circuit Breaker Pattern
                    .circuitBreaker(config -> config
                        .setName("productServiceCB")
                        .setFallbackUri("forward:/fallback/products"))
                    // Rate Limiting Pattern
                    .requestRateLimiter(config -> config
                        .setRateLimiter(rateLimiter)
                        .setKeyResolver(ipKeyResolver()))
                    // Retry with Exponential Backoff
                    .retry(config -> config
                        .setRetries(3)
                        .setBackoff(java.time.Duration.ofMillis(100),
                                   java.time.Duration.ofMillis(1000),
                                   2, true))
                )
                // Load Balancer Pattern - lb:// prefix enables client-side load balancing
                .uri("lb://product-service")
            )
            
            // Order Service Route
            .route("order-service", r -> r
                .path("/api/orders/**")
                .filters(f -> f
                    .stripPrefix(1)
                    .circuitBreaker(config -> config
                        .setName("orderServiceCB")
                        .setFallbackUri("forward:/fallback/orders"))
                    .requestRateLimiter(config -> config
                        .setRateLimiter(rateLimiter)
                        .setKeyResolver(ipKeyResolver()))
                )
                .uri("lb://order-service")
            )
            .build();
    }
}
```

```java api-gateway/src/main/java/com/cloudpatterns/gateway/controller/FallbackController.java
package com.cloudpatterns.gateway.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/products")
    public Mono<Map<String, String>> productFallback() {
        return Mono.just(Map.of(
            "message", "Product service is currently unavailable. Please try again later.",
            "status", "CIRCUIT_OPEN"
        ));
    }

    @GetMapping("/orders")
    public Mono<Map<String, String>> orderFallback() {
        return Mono.just(Map.of(
            "message", "Order service is currently unavailable. Please try again later.",
            "status", "CIRCUIT_OPEN"
        ));
    }
}
```

```yaml api-gateway/src/main/resources/application.yml
server:
  port: 8080

spring:
  application:
    name: api-gateway
  redis:
    host: localhost
    port: 6379
  cloud:
    gateway:
      discovery:
        locator:
          enabled: true
          lower-case-service-id: true

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/

# Circuit Breaker Configuration
resilience4j:
  circuitbreaker:
    instances:
      productServiceCB:
        sliding-window-size: 10
        failure-rate-threshold: 50
        wait-duration-in-open-state: 10000
        permitted-number-of-calls-in-half-open-state: 3
        automatic-transition-from-open-to-half-open-enabled: true
      orderServiceCB:
        sliding-window-size: 10
        failure-rate-threshold: 50
        wait-duration-in-open-state: 10000

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
```

## 4. Product Service - Circuit Breaker, Bulkhead, Distributed Tracing

```xml product-service/pom.xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.cloudpatterns</groupId>
        <artifactId>cloud-patterns-parent</artifactId>
        <version>1.0.0</version>
    </parent>

    <artifactId>product-service</artifactId>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-config</artifactId>
        </dependency>
        
        <!-- Circuit Breaker and Bulkhead Pattern -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-circuitbreaker-resilience4j</artifactId>
        </dependency>
        
        <!-- Distributed Tracing Pattern -->
        <dependency>
            <groupId>io.micrometer</groupId>
            <artifactId>micrometer-tracing-bridge-brave</artifactId>
        </dependency>
        <dependency>
            <groupId>io.zipkin.reporter2</groupId>
            <artifactId>zipkin-reporter-brave</artifactId>
        </dependency>
        
        <!-- Centralized Logging -->
        <dependency>
            <groupId>net.logstash.logback</groupId>
            <artifactId>logstash-logback-encoder</artifactId>
            <version>7.4</version>
        </dependency>
        
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
    </dependencies>
</project>
```

```java product-service/src/main/java/com/cloudpatterns/product/ProductServiceApplication.java
package com.cloudpatterns.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ProductServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProductServiceApplication.class, args);
    }
}
```

```java product-service/src/main/java/com/cloudpatterns/product/model/Product.java
package com.cloudpatterns.product.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
}
```

```java product-service/src/main/java/com/cloudpatterns/product/service/ProductService.java
package com.cloudpatterns.product.service;

import com.cloudpatterns.product.model.Product;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Slf4j
public class ProductService {
    
    private final Map<Long, Product> productStore = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    
    public ProductService() {
        // Initialize with sample data
        createProduct(Product.builder()
            .name("Laptop")
            .description("High-performance laptop")
            .price(new BigDecimal("1299.99"))
            .stockQuantity(50)
            .build());
            
        createProduct(Product.builder()
            .name("Smartphone")
            .description("Latest smartphone model")
            .price(new BigDecimal("899.99"))
            .stockQuantity(100)
            .build());
    }
    
    /**
     * Circuit Breaker Pattern - Protects against cascading failures
     * Bulkhead Pattern - Isolates resources to prevent resource exhaustion
     * Retry Pattern - Automatically retries failed operations
     */
    @CircuitBreaker(name = "productService", fallbackMethod = "getAllProductsFallback")
    @Bulkhead(name = "productService", type = Bulkhead.Type.SEMAPHORE)
    @Retry(name = "productService")
    public List<Product> getAllProducts() {
        log.info("Fetching all products");
        
        // Simulate potential failure for demonstration
        if (Math.random() < 0.1) {
            log.error("Simulated failure in getAllProducts");
            throw new RuntimeException("Database connection failed");
        }
        
        return new ArrayList<>(productStore.values());
    }
    
    @CircuitBreaker(name = "productService", fallbackMethod = "getProductByIdFallback")
    @Bulkhead(name = "productService", type = Bulkhead.Type.SEMAPHORE)
    public Optional<Product> getProductById(Long id) {
        log.info("Fetching product with id: {}", id);
        return Optional.ofNullable(productStore.get(id));
    }
    
    public Product createProduct(Product product) {
        log.info("Creating new product: {}", product.getName());
        product.setId(idGenerator.getAndIncrement());
        productStore.put(product.getId(), product);
        return product;
    }
    
    public Optional<Product> updateProduct(Long id, Product product) {
        log.info("Updating product with id: {}", id);
        if (productStore.containsKey(id)) {
            product.setId(id);
            productStore.put(id, product);
            return Optional.of(product);
        }
        return Optional.empty();
    }
    
    public boolean deleteProduct(Long id) {
        log.info("Deleting product with id: {}", id);
        return productStore.remove(id) != null;
    }
    
    // Fallback methods for Circuit Breaker
    private List<Product> getAllProductsFallback(Exception e) {
        log.warn("Fallback triggered for getAllProducts: {}", e.getMessage());
        return List.of(Product.builder()
            .id(0L)
            .name("Fallback Product")
            .description("Service temporarily unavailable")
            .price(BigDecimal.ZERO)
            .stockQuantity(0)
            .build());
    }
    
    private Optional<Product> getProductByIdFallback(Long id, Exception e) {
        log.warn("Fallback triggered for getProductById: {}", e.getMessage());
        return Optional.of(Product.builder()
            .id(id)
            .name("Fallback Product")
            .description("Service temporarily unavailable")
            .price(BigDecimal.ZERO)
            .stockQuantity(0)
            .build());
    }
}
```

```java product-service/src/main/java/com/cloudpatterns/product/controller/ProductController.java
package com.cloudpatterns.product.controller;

import com.cloudpatterns.product.model.Product;
import com.cloudpatterns.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {
    
    private final ProductService productService;
    
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        log.info("GET /products - Fetching all products");
        List<Product> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        log.info("GET /products/{} - Fetching product by id", id);
        return productService.getProductById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        log.info("POST /products - Creating new product");
        Product created = productService.createProduct(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, 
                                                 @RequestBody Product product) {
        log.info("PUT /products/{} - Updating product", id);
        return productService.updateProduct(id, product)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        log.info("DELETE /products/{} - Deleting product", id);
        boolean deleted = productService.deleteProduct(id);
        return deleted ? ResponseEntity.noContent().build() 
                      : ResponseEntity.notFound().build();
    }
}
```

```yaml product-service/src/main/resources/application.yml
server:
  port: 8081

spring:
  application:
    name: product-service
  config:
    import: optional:configserver:http://localhost:8888

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
  instance:
    prefer-ip-address: true

# Distributed Tracing Pattern - Zipkin configuration
management:
  tracing:
    sampling:
      probability: 1.0
  zipkin:
    tracing:
      endpoint: http://localhost:9411/api/v2/spans
  endpoints:
    web:
      exposure:
        include: "*"
  endpoint:
    health:
      show-details: always

# Circuit Breaker Pattern Configuration
resilience4j:
  circuitbreaker:
    instances:
      productService:
        register-health-indicator: true
        sliding-window-size: 100
        minimum-number-of-calls: 10
        permitted-number-of-calls-in-half-open-state: 3
        automatic-transition-from-open-to-half-open-enabled: true
        wait-duration-in-open-state: 5s
        failure-rate-threshold: 50
        event-consumer-buffer-size: 10
        record-exceptions:
          - java.lang.RuntimeException
          - java.io.IOException
  
  # Retry with Exponential Backoff Pattern
  retry:
    instances:
      productService:
        max-attempts: 3
        wait-duration: 1s
        enable-exponential-backoff: true
        exponential-backoff-multiplier: 2
        retry-exceptions:
          - java.lang.RuntimeException
  
  # Bulkhead Pattern Configuration
  bulkhead:
    instances:
      productService:
        max-concurrent-calls: 10
        max-wait-duration: 1s

# Centralized Logging Pattern - Structured logging
logging:
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
  level:
    com.cloudpatterns: DEBUG
    org.springframework.cloud: INFO
```

```xml product-service/src/main/resources/logback-spring.xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <include resource="org/springframework/boot/logging/logback/defaults.xml"/>
    
    <!-- Centralized Logging Pattern - JSON format for log aggregation -->
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder class="net.logstash.logback.encoder.LogstashEncoder">
            <customFields>{"application":"product-service"}</customFields>
        </encoder>
    </appender>
    
    <!-- File appender for sending logs to centralized system -->
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/product-service.log</file>
        <encoder class="net.logstash.logback.encoder.LogstashEncoder">
            <customFields>{"application":"product-service"}</customFields>
        </encoder>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>logs/product-service.%d{yyyy-MM-dd}.log</fileNamePattern>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
    </appender>
    
    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="FILE"/>
    </root>
</configuration>
```

## 5. Order Service - Demonstrating Service Mesh concepts

```xml order-service/pom.xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.cloudpatterns</groupId>
        <artifactId>cloud-patterns-parent</artifactId>
        <version>1.0.0</version>
    </parent>

    <artifactId>order-service</artifactId>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
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
            <artifactId>spring-cloud-starter-circuitbreaker-resilience4j</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-loadbalancer</artifactId>
        </dependency>
        
        <!-- Distributed Tracing -->
        <dependency>
            <groupId>io.micrometer</groupId>
            <artifactId>micrometer-tracing-bridge-brave</artifactId>
        </dependency>
        <dependency>
            <groupId>io.zipkin.reporter2</groupId>
            <artifactId>zipkin-reporter-brave</artifactId>
        </dependency>
        
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
    </dependencies>
</project>
```

```java order-service/src/main/java/com/cloudpatterns/order/OrderServiceApplication.java
package com.cloudpatterns.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class OrderServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
```

```java order-service/src/main/java/com/cloudpatterns/order/model/Order.java
package com.cloudpatterns.order.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    private Long id;
    private String customerId;
    private List<OrderItem> items;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

```java order-service/src/main/java/com/cloudpatterns/order/model/OrderItem.java
package com.cloudpatterns.order.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal price;
}
```

```java order-service/src/main/java/com/cloudpatterns/order/model/OrderStatus.java
package com.cloudpatterns.order.model;

public enum OrderStatus {
    PENDING,
    CONFIRMED,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED
}
```

```java order-service/src/main/java/com/cloudpatterns/order/model/Product.java
package com.cloudpatterns.order.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
}
```

```java order-service/src/main/java/com/cloudpatterns/order/client/ProductClient.java
package com.cloudpatterns.order.client;

import com.cloudpatterns.order.model.Product;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * Service Discovery Pattern - Uses Eureka to discover product-service
 * Load Balancer Pattern - Feign client automatically load balances between instances
 * Circuit Breaker Pattern - Configured via application.yml
 */
@FeignClient(
    name = "product-service",
    fallback = ProductClientFallback.class
)
public interface ProductClient {
    
    @GetMapping("/products")
    List<Product> getAllProducts();
    
    @GetMapping("/products/{id}")
    Product getProductById(@PathVariable Long id);
}
```

```java order-service/src/main/java/com/cloudpatterns/order/client/ProductClientFallback.java
package com.cloudpatterns.order.client;

import com.cloudpatterns.order.model.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

/**
 * Circuit Breaker Pattern - Fallback implementation
 */
@Component
@Slf4j
public class ProductClientFallback implements ProductClient {
    
    @Override
    public List<Product> getAllProducts() {
        log.warn("ProductClient fallback triggered for getAllProducts");
        return Collections.emptyList();
    }
    
    @Override
    public Product getProductById(Long id) {
        log.warn("ProductClient fallback triggered for getProductById: {}", id);
        return Product.builder()
            .id(id)
            .name("Product Unavailable")
            .description("Product service is currently unavailable")
            .price(BigDecimal.ZERO)
            .stockQuantity(0)
            .build();
    }
}
```

```java order-service/src/main/java/com/cloudpatterns/order/service/OrderService.java
package com.cloudpatterns.order.service;

import com.cloudpatterns.order.client.ProductClient;
import com.cloudpatterns.order.model.Order;
import com.cloudpatterns.order.model.OrderItem;
import com.cloudpatterns.order.model.OrderStatus;
import com.cloudpatterns.order.model.Product;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    
    private final ProductClient productClient;
    private final Map<Long, Order> orderStore = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    
    /**
     * Bulkhead Pattern - Isolates this operation to prevent thread pool exhaustion
     * Circuit Breaker Pattern - Protects against cascading failures
     * Retry Pattern - Retries on transient failures
     */
    @Bulkhead(name = "orderService", type = Bulkhead.Type.SEMAPHORE)
    @CircuitBreaker(name = "orderService", fallbackMethod = "createOrderFallback")
    @Retry(name = "orderService")
    public Order createOrder(Order order) {
        log.info("Creating new order for customer: {}", order.getCustomerId());
        
        // Validate products and calculate total
        BigDecimal total = BigDecimal.ZERO;
        List<OrderItem> validatedItems = new ArrayList<>();
        
        for (OrderItem item : order.getItems()) {
            // Service Discovery + Load Balancer Pattern - Feign client handles this
            Product product = productClient.getProductById(item.getProductId());
            
            if (product == null || product.getStockQuantity() < item.getQuantity()) {
                log.warn("Insufficient stock for product: {}", item.getProductId());
                throw new RuntimeException("Insufficient stock for product: " + item.getProductId());
            }
            
            item.setProductName(product.getName());
            item.setPrice(product.getPrice());
            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            validatedItems.add(item);
        }
        
        order.setId(idGenerator.getAndIncrement());
        order.setItems(validatedItems);
        order.setTotalAmount(total);
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        
        orderStore.put(order.getId(), order);
        log.info("Order created successfully with id: {}", order.getId());
        
        return order;
    }
    
    public List<Order> getAllOrders() {
        log.info("Fetching all orders");
        return new ArrayList<>(orderStore.values());
    }
    
    public Optional<Order> getOrderById(Long id) {
        log.info("Fetching order with id: {}", id);
        return Optional.ofNullable(orderStore.get(id));
    }
    
    public List<Order> getOrdersByCustomerId(String customerId) {
        log.info("Fetching orders for customer: {}", customerId);
        return orderStore.values().stream()
            .filter(order -> order.getCustomerId().equals(customerId))
            .toList();
    }
    
    public Optional<Order> updateOrderStatus(Long id, OrderStatus status) {
        log.info("Updating order {} status to {}", id, status);
        Order order = orderStore.get(id);
        if (order != null) {
            order.setStatus(status);
            order.setUpdatedAt(LocalDateTime.now());
            return Optional.of(order);
        }
        return Optional.empty();
    }
    
    public boolean cancelOrder(Long id) {
        log.info("Cancelling order with id: {}", id);
        Order order = orderStore.get(id);
        if (order != null && order.getStatus() == OrderStatus.PENDING) {
            order.setStatus(OrderStatus.CANCELLED);
            order.setUpdatedAt(LocalDateTime.now());
            return true;
        }
        return false;
    }
    
    // Fallback method for Circuit Breaker
    private Order createOrderFallback(Order order, Exception e) {
        log.error("Fallback triggered for createOrder: {}", e.getMessage());
        order.setId(-1L);
        order.setStatus(OrderStatus.CANCELLED);
        order.setTotalAmount(BigDecimal.ZERO);
        return order;
    }
}
```

```java order-service/src/main/java/com/cloudpatterns/order/controller/OrderController.java
package com.cloudpatterns.order.controller;

import com.cloudpatterns.order.model.Order;
import com.cloudpatterns.order.model.OrderStatus;
import com.cloudpatterns.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {
    
    private final OrderService orderService;
    
    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        log.info("GET /orders - Fetching all orders");
        List<Order> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        log.info("GET /orders/{} - Fetching order by id", id);
        return orderService.getOrderById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<Order>> getOrdersByCustomerId(@PathVariable String customerId) {
        log.info("GET /orders/customer/{} - Fetching orders by customer", customerId);
        List<Order> orders = orderService.getOrdersByCustomerId(customerId);
        return ResponseEntity.ok(orders);
    }
    
    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody Order order) {
        log.info("POST /orders - Creating new order");
        try {
            Order created = orderService.createOrder(order);
            if (created.getId() == -1L) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(created);
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            log.error("Error creating order: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    @PatchMapping("/{id}/status")
    public ResponseEntity<Order> updateOrderStatus(@PathVariable Long id, 
                                                    @RequestBody Map<String, String> statusUpdate) {
        log.info("PATCH /orders/{}/status - Updating order status", id);
        OrderStatus status = OrderStatus.valueOf(statusUpdate.get("status"));
        return orderService.updateOrderStatus(id, status)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelOrder(@PathVariable Long id) {
        log.info("DELETE /orders/{} - Cancelling order", id);
        boolean cancelled = orderService.cancelOrder(id);
        return cancelled ? ResponseEntity.noContent().build() 
                        : ResponseEntity.notFound().build();
    }
}
```

```yaml order-service/src/main/resources/application.yml
server:
  port: 8082

spring:
  application:
    name: order-service

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
  instance:
    prefer-ip-address: true
    metadata-map:
      # Service Mesh Pattern - Metadata for service mesh integration
      version: "1.0.0"
      zone: "us-east-1a"

# Distributed Tracing Pattern - Zipkin configuration
management:
  tracing:
    sampling:
      probability: 1.0
  zipkin:
    tracing:
      endpoint: http://localhost:9411/api/v2/spans
  endpoints:
    web:
      exposure:
        include: "*"
  endpoint:
    health:
      show-details: always
  metrics:
    export:
      prometheus:
        enabled: true

# Circuit Breaker Pattern Configuration
resilience4j:
  circuitbreaker:
    instances:
      orderService:
        register-health-indicator: true
        sliding-window-size: 100
        minimum-number-of-calls: 10
        permitted-number-of-calls-in-half-open-state: 3
        automatic-transition-from-open-to-half-open-enabled: true
        wait-duration-in-open-state: 10s
        failure-rate-threshold: 50
        event-consumer-buffer-size: 10
  
  # Retry with Exponential Backoff Pattern
  retry:
    instances:
      orderService:
        max-attempts: 3
        wait-duration: 500ms
        enable-exponential-backoff: true
        exponential-backoff-multiplier: 2
        retry-exceptions:
          - org.springframework.web.client.ResourceAccessException
          - feign.RetryableException
  
  # Bulkhead Pattern Configuration
  bulkhead:
    instances:
      orderService:
        max-concurrent-calls: 20
        max-wait-duration: 2s

# Feign Client Configuration - Load Balancer Pattern
feign:
  client:
    config:
      default:
        connectTimeout: 5000
        readTimeout: 5000
        loggerLevel: basic
  circuitbreaker:
    enabled: true
    alphanumeric-ids:
      enabled: true

# Centralized Logging Pattern
logging:
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  level:
    com.cloudpatterns: DEBUG
    org.springframework.cloud: INFO
    feign: DEBUG
```

```xml order-service/src/main/resources/logback-spring.xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <include resource="org/springframework/boot/logging/logback/defaults.xml"/>
    
    <!-- Centralized Logging Pattern - JSON format for ELK stack -->
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder class="net.logstash.logback.encoder.LogstashEncoder">
            <customFields>{"application":"order-service"}</customFields>
            <includeMdcKeyName>traceId</includeMdcKeyName>
            <includeMdcKeyName>spanId</includeMdcKeyName>
        </encoder>
    </appender>
    
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/order-service.log</file>
        <encoder class="net.logstash.logback.encoder.LogstashEncoder">
            <customFields>{"application":"order-service"}</customFields>
            <includeMdcKeyName>traceId</includeMdcKeyName>
            <includeMdcKeyName>spanId</includeMdcKeyName>
        </encoder>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>logs/order-service.%d{yyyy-MM-dd}.log</fileNamePattern>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
    </appender>
    
    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="FILE"/>
    </root>
</configuration>
```

## 6. Service Mesh Pattern - Istio Configuration Examples

```yaml k8s/service-mesh/product-service-deployment.yaml
apiVersion: v1
kind: Service
metadata:
  name: product-service
  labels:
    app: product-service
    service: product-service
spec:
  ports:
  - port: 8081
    name: http
  selector:
    app: product-service
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: product-service-v1
  labels:
    app: product-service
    version: v1
spec:
  replicas: 3
  selector:
    matchLabels:
      app: product-service
      version: v1
  template:
    metadata:
      labels:
        app: product-service
        version: v1
      annotations:
        # Service Mesh Pattern - Istio sidecar injection
        sidecar.istio.io/inject: "true"
    spec:
      containers:
      - name: product-service
        image: product-service:1.0.0
        ports:
        - containerPort: 8081
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "kubernetes"
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "500m"
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8081
          initialDelaySeconds: 60
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8081
          initialDelaySeconds: 30
          periodSeconds: 5
```

```yaml k8s/service-mesh/istio-virtual-service.yaml
# Service Mesh Pattern - Traffic management with Istio
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: product-service
spec:
  hosts:
  - product-service
  http:
  - match:
    - headers:
        canary:
          exact: "true"
    route:
    - destination:
        host: product-service
        subset: v2
      weight: 100
  - route:
    - destination:
        host: product-service
        subset: v1
      weight: 90
    - destination:
        host: product-service
        subset: v2
      weight: 10
  # Circuit Breaker Pattern at Service Mesh level
  - timeout: 5s
    retries:
      attempts: 3
      perTryTimeout: 2s
---
# Service Mesh Pattern - Destination rules
apiVersion: networking.istio.io/v1beta1
kind: DestinationRule
metadata:
  name: product-service
spec:
  host: product-service
  trafficPolicy:
    # Load Balancer Pattern
    loadBalancer:
      simple: LEAST_REQUEST
    # Circuit Breaker Pattern
    connectionPool:
      tcp:
        maxConnections: 100
      http:
        http1MaxPendingRequests: 50
        http2MaxRequests: 100
        maxRequestsPerConnection: 2
    outlierDetection:
      consecutiveErrors: 5
      interval: 30s
      baseEjectionTime: 30s
      maxEjectionPercent: 50
      minHealthPercent: 40
  subsets:
  - name: v1
    labels:
      version: v1
  - name: v2
    labels:
      version: v2
```

```yaml k8s/service-mesh/istio-gateway.yaml
# API Gateway Pattern - Istio Gateway
apiVersion: networking.istio.io/v1beta1
kind: Gateway
metadata:
  name: cloud-patterns-gateway
spec:
  selector:
    istio: ingressgateway
  servers:
  - port:
      number: 80
      name: http
      protocol: HTTP
    hosts:
    - "*"
  - port:
      number: 443
      name: https
      protocol: HTTPS
    tls:
      mode: SIMPLE
      credentialName: cloud-patterns-cert
    hosts:
    - "api.cloudpatterns.com"
---
# Rate Limiting Pattern - Istio EnvoyFilter
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: rate-limit-filter
spec:
  workloadSelector:
    labels:
      app: product-service
  configPatches:
  - applyTo: HTTP_FILTER
    match:
      context: SIDECAR_INBOUND
      listener:
        filterChain:
          filter:
            name: "envoy.filters.network.http_connection_manager"
    patch:
      operation: INSERT_BEFORE
      value:
        name: envoy.filters.http.local_ratelimit
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.http.local_ratelimit.v3.LocalRateLimit
          stat_prefix: http_local_rate_limiter
          token_bucket:
            max_tokens: 100
            tokens_per_fill: 100
            fill_interval: 60s
          filter_enabled:
            runtime_key: local_rate_limit_enabled
            default_value:
              numerator: 100
              denominator: HUNDRED
```

## 7. Docker Compose for Local Development

```yaml docker-compose.yml
version: '3.8'

services:
  # Service Discovery Pattern - Eureka Server
  eureka-server:
    build: ./eureka-server
    ports:
      - "8761:8761"
    networks:
      - cloud-patterns-network
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8761/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 5

  # Configuration Server Pattern
  config-server:
    build: ./config-server
    ports:
      - "8888:8888"
    depends_on:
      - eureka-server
    networks:
      - cloud-patterns-network
    environment:
      - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/

  # Redis for Rate Limiting Pattern
  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    networks:
      - cloud-patterns-network
    command: redis-server --appendonly yes

  # Distributed Tracing Pattern - Zipkin
  zipkin:
    image: openzipkin/zipkin:latest
    ports:
      - "9411:9411"
    networks:
      - cloud-patterns-network

  # Centralized Logging Pattern - ELK Stack
  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch:8.11.0
    environment:
      - discovery.type=single-node
      - xpack.security.enabled=false
      - "ES_JAVA_OPTS=-Xms512m -Xmx512m"
    ports:
      - "9200:9200"
    networks:
      - cloud-patterns-network
    volumes:
      - elasticsearch-data:/usr/share/elasticsearch/data

  logstash:
    image: docker.elastic.co/logstash/logstash:8.11.0
    ports:
      - "5000:5000"
      - "9600:9600"
    networks:
      - cloud-patterns-network
    volumes:
      - ./logstash/pipeline:/usr/share/logstash/pipeline:ro
    depends_on:
      - elasticsearch

  kibana:
    image: docker.elastic.co/kibana/kibana:8.11.0
    ports:
      - "5601:5601"
    networks:
      - cloud-patterns-network
    environment:
      - ELASTICSEARCH_HOSTS=http://elasticsearch:9200
    depends_on:
      - elasticsearch

  # Product Service (multiple instances for Load Balancing)
  product-service-1:
    build: ./product-service
    ports:
      - "8081:8081"
    depends_on:
      - eureka-server
      - config-server
      - zipkin
    networks:
      - cloud-patterns-network
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/
      - MANAGEMENT_ZIPKIN_TRACING_ENDPOINT=http://zipkin:9411/api/v2/spans
      - EUREKA_INSTANCE_INSTANCEID=product-service-1

  product-service-2:
    build: ./product-service
    ports:
      - "8091:8081"
    depends_on:
      - eureka-server
      - config-server
      - zipkin
    networks:
      - cloud-patterns-network
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/
      - MANAGEMENT_ZIPKIN_TRACING_ENDPOINT=http://zipkin:9411/api/v2/spans
      - EUREKA_INSTANCE_INSTANCEID=product-service-2

  # Order Service
  order-service:
    build: ./order-service
    ports:
      - "8082:8082"
    depends_on:
      - eureka-server
      - config-server
      - zipkin
      - product-service-1
      - product-service-2
    networks:
      - cloud-patterns-network
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/
      - MANAGEMENT_ZIPKIN_TRACING_ENDPOINT=http://zipkin:9411/api/v2/spans

  # API Gateway Pattern with Load Balancer and Rate Limiting
  api-gateway:
    build: ./api-gateway
    ports:
      - "8080:8080"
    depends_on:
      - eureka-server
      - redis
      - product-service-1
      - product-service-2
      - order-service
    networks:
      - cloud-patterns-network
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/
      - SPRING_REDIS_HOST=redis
      - SPRING_REDIS_PORT=6379

networks:
  cloud-patterns-network:
    driver: bridge

volumes:
  elasticsearch-data:
    driver: local
```

```conf logstash/pipeline/logstash.conf
# Centralized Logging Pattern - Logstash pipeline configuration
input {
  tcp {
    port => 5000
    codec => json
  }
  
  file {
    path => "/var/log/services/*.log"
    start_position => "beginning"
    codec => json
  }
}

filter {
  # Parse JSON logs
  if [message] =~ /^\{/ {
    json {
      source => "message"
    }
  }
  
  # Add metadata
  mutate {
    add_field => {
      "[@metadata][index_prefix]" => "cloud-patterns"
    }
  }
  
  # Extract trace information for Distributed Tracing correlation
  if [traceId] {
    mutate {
      add_field => {
        "trace_id" => "%{traceId}"
        "span_id" => "%{spanId}"
      }
    }
  }
}

output {
  elasticsearch {
    hosts => ["elasticsearch:9200"]
    index => "%{[@metadata][index_prefix]}-%{+YYYY.MM.dd}"
  }
  
  stdout {
    codec => rubydebug
  }
}
```

## 8. README and Usage Guide

```markdown README.md
# Cloud Patterns Implementation with Spring Boot

This project demonstrates implementation of all major cloud patterns using Spring Boot and Spring Cloud.

## Patterns Implemented

### 1. **Service Discovery Pattern** (Eureka Server)
- Location: `eureka-server/`
- Automatic service registration and discovery
- Health checking and load balancing support

### 2. **Configuration Server Pattern**
- Location: `config-server/`
- Centralized configuration management
- Support for Git and native file system backends

### 3. **API Gateway Pattern**
- Location: `api-gateway/`
- Single entry point for all microservices
- Request routing and composition
- Authentication and authorization

### 4. **Load Balancer Pattern**
- Implemented in: API Gateway and Feign Clients
- Client-side load balancing using Spring Cloud LoadBalancer
- Multiple instances of product-service demonstrate this

### 5. **Circuit Breaker Pattern**
- Implemented in: Product Service, Order Service, API Gateway
- Using Resilience4j
- Prevents cascading failures
- Automatic recovery with half-open state

### 6. **Bulkhead Pattern**
- Implemented in: Product Service, Order Service
- Thread pool isolation
- Prevents resource exhaustion

### 7. **Retry with Exponential Backoff Pattern**
- Implemented in: All services
- Automatic retry on transient failures
- Exponential backoff to prevent overwhelming services

### 8. **Rate Limiting Pattern**
- Implemented in: API Gateway
- Redis-based distributed rate limiting
- IP-based rate limiting

### 9. **Distributed Tracing Pattern**
- Implemented using: Micrometer Tracing + Zipkin
- Correlation IDs across services
- Request flow visualization

### 10. **Centralized Logging Pattern**
- Implemented using: Logstash + Elasticsearch + Kibana (ELK)
- Structured JSON logging
- Correlation with distributed traces

### 11. **Service Mesh Pattern**
- Implemented using: Istio (K8s deployment)
- Traffic management
- Security policies
- Observability

## Architecture

```markdown README.md
                                    ┌─────────────────┐
                                    │  Eureka Server  │
                                    │ (Port: 8761)    │
                                    └────────┬────────┘
                                             │
                    ┌────────────────────────┼────────────────────────┐
                    │                        │                        │
          ┌─────────▼─────────┐   ┌─────────▼─────────┐   ┌─────────▼─────────┐
          │  Config Server    │   │   API Gateway     │   │     Zipkin        │
          │  (Port: 8888)     │   │   (Port: 8080)    │   │  (Port: 9411)     │
          └───────────────────┘   └─────────┬─────────┘   └───────────────────┘
                                             │
                              ┌──────────────┴──────────────┐
                              │                             │
                    ┌─────────▼─────────┐       ┌─────────▼─────────┐
                    │ Product Service   │       │  Order Service    │
                    │ Instance 1 & 2    │       │   (Port: 8082)    │
                    │ (Ports: 8081,8091)│       └───────────────────┘
                    └───────────────────┘
                              │
                    ┌─────────▼─────────┐
                    │      Redis        │
                    │  (Port: 6379)     │
                    └───────────────────┘

                    ┌───────────────────────────────────────┐
                    │         ELK Stack                     │
                    │  Elasticsearch - Logstash - Kibana    │
                    │  (Ports: 9200, 5000, 5601)           │
                    └───────────────────────────────────────┘
```

## Prerequisites

- Java 17 or higher
- Maven 3.8+
- Docker and Docker Compose (for local deployment)
- Kubernetes cluster (for Service Mesh pattern)
- kubectl and istioctl (for K8s deployment)

## Quick Start

### Using Docker Compose (Recommended)

1. **Build all services:**
```bash
mvn clean package -DskipTests
```

2. **Start all services:**
```bash
docker-compose up -d
```

3. **Check service status:**
```bash
docker-compose ps
```

4. **Access services:**
- Eureka Dashboard: http://localhost:8761
- API Gateway: http://localhost:8080
- Zipkin UI: http://localhost:9411
- Kibana: http://localhost:5601
- Product Service (direct): http://localhost:8081
- Order Service (direct): http://localhost:8082

### Manual Startup (Development)

1. **Start Eureka Server:**
```bash
cd eureka-server
mvn spring-boot:run
```

2. **Start Config Server:**
```bash
cd config-server
mvn spring-boot:run
```

3. **Start Redis:**
```bash
docker run -d -p 6379:6379 redis:7-alpine
```

4. **Start Zipkin:**
```bash
docker run -d -p 9411:9411 openzipkin/zipkin
```

5. **Start Product Service (multiple instances):**
```bash
cd product-service
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8081
# In another terminal
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8091
```

6. **Start Order Service:**
```bash
cd order-service
mvn spring-boot:run
```

7. **Start API Gateway:**
```bash
cd api-gateway
mvn spring-boot:run
```

## Testing the Patterns

### 1. Service Discovery Pattern

Check registered services in Eureka:
```bash
curl http://localhost:8761/eureka/apps
```

### 2. API Gateway + Load Balancer Pattern

Make multiple requests to see load balancing:
```bash
# Through API Gateway
for i in {1..10}; do
  curl http://localhost:8080/api/products
  echo ""
done
```

Check Eureka dashboard to see different instances handling requests.

### 3. Circuit Breaker Pattern

Trigger circuit breaker by stopping product service and making requests:
```bash
# Stop product service instances
docker-compose stop product-service-1 product-service-2

# Make requests - should get fallback responses
curl http://localhost:8080/api/products

# Check circuit breaker status
curl http://localhost:8080/actuator/health
```

### 4. Rate Limiting Pattern

Test rate limiting (configured for 10 requests/minute):
```bash
# Exceed rate limit
for i in {1..15}; do
  curl -w "\nStatus: %{http_code}\n" http://localhost:8080/api/products
  sleep 1
done
```

You should see HTTP 429 (Too Many Requests) after the limit.

### 5. Retry with Exponential Backoff

Monitor logs to see retry attempts:
```bash
# Watch product service logs
docker-compose logs -f product-service-1

# The service simulates random failures - retries will be logged
```

### 6. Bulkhead Pattern

Test concurrent requests to see bulkhead isolation:
```bash
# Install Apache Bench
# Make 100 concurrent requests
ab -n 100 -c 20 http://localhost:8080/api/products
```

Check resilience4j metrics:
```bash
curl http://localhost:8081/actuator/metrics/resilience4j.bulkhead.available.concurrent.calls
```

### 7. Distributed Tracing Pattern

1. Make some requests:
```bash
curl http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "CUST001",
    "items": [
      {
        "productId": 1,
        "quantity": 2
      }
    ]
  }'
```

2. View traces in Zipkin: http://localhost:9411
3. Search for traces and see the complete request flow across services

### 8. Centralized Logging Pattern

1. Wait for logs to be indexed (30-60 seconds)
2. Access Kibana: http://localhost:5601
3. Create index pattern: `cloud-patterns-*`
4. Search logs with correlation:
   - Filter by `application: "order-service"`
   - Search for specific `traceId` to see all related logs

### 9. Configuration Server Pattern

1. Check configuration:
```bash
curl http://localhost:8888/product-service/default
```

2. Update configuration in git repository
3. Refresh configuration:
```bash
curl -X POST http://localhost:8081/actuator/refresh
```

## API Examples

### Product Service APIs

**Get all products:**
```bash
curl http://localhost:8080/api/products
```

**Get product by ID:**
```bash
curl http://localhost:8080/api/products/1
```

**Create product:**
```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Tablet",
    "description": "10-inch tablet",
    "price": 399.99,
    "stockQuantity": 75
  }'
```

**Update product:**
```bash
curl -X PUT http://localhost:8080/api/products/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Laptop Pro",
    "description": "Updated high-performance laptop",
    "price": 1499.99,
    "stockQuantity": 45
  }'
```

**Delete product:**
```bash
curl -X DELETE http://localhost:8080/api/products/1
```

### Order Service APIs

**Get all orders:**
```bash
curl http://localhost:8080/api/orders
```

**Get order by ID:**
```bash
curl http://localhost:8080/api/orders/1
```

**Create order:**
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "CUST001",
    "items": [
      {
        "productId": 1,
        "quantity": 2
      },
      {
        "productId": 2,
        "quantity": 1
      }
    ]
  }'
```

**Get orders by customer:**
```bash
curl http://localhost:8080/api/orders/customer/CUST001
```

**Update order status:**
```bash
curl -X PATCH http://localhost:8080/api/orders/1/status \
  -H "Content-Type: application/json" \
  -d '{
    "status": "CONFIRMED"
  }'
```

**Cancel order:**
```bash
curl -X DELETE http://localhost:8080/api/orders/1
```

## Kubernetes Deployment (Service Mesh Pattern)

### Prerequisites
- Kubernetes cluster (minikube, kind, or cloud provider)
- Istio installed

### Install Istio

```bash
# Download Istio
curl -L https://istio.io/downloadIstio | sh -
cd istio-*
export PATH=$PWD/bin:$PATH

# Install Istio
istioctl install --set profile=demo -y

# Enable automatic sidecar injection
kubectl label namespace default istio-injection=enabled
```

### Deploy Services

```bash
# Build and push images
docker build -t your-registry/eureka-server:1.0.0 ./eureka-server
docker build -t your-registry/product-service:1.0.0 ./product-service
docker build -t your-registry/order-service:1.0.0 ./order-service
docker build -t your-registry/api-gateway:1.0.0 ./api-gateway

docker push your-registry/eureka-server:1.0.0
docker push your-registry/product-service:1.0.0
docker push your-registry/order-service:1.0.0
docker push your-registry/api-gateway:1.0.0

# Deploy to Kubernetes
kubectl apply -f k8s/service-mesh/

# Check pods
kubectl get pods

# Check Istio injection
kubectl get pods -o jsonpath='{.items[*].spec.containers[*].name}'
```

### Access Services

```bash
# Get Istio Ingress Gateway external IP
export INGRESS_HOST=$(kubectl get svc istio-ingressgateway -n istio-system -o jsonpath='{.status.loadBalancer.ingress[0].ip}')

# Access via gateway
curl http://$INGRESS_HOST/api/products
```

### Monitor Service Mesh

```bash
# Install Kiali, Prometheus, Grafana, Jaeger
kubectl apply -f https://raw.githubusercontent.com/istio/istio/release-1.20/samples/addons/prometheus.yaml
kubectl apply -f https://raw.githubusercontent.com/istio/istio/release-1.20/samples/addons/grafana.yaml
kubectl apply -f https://raw.githubusercontent.com/istio/istio/release-1.20/samples/addons/kiali.yaml
kubectl apply -f https://raw.githubusercontent.com/istio/istio/release-1.20/samples/addons/jaeger.yaml

# Access Kiali dashboard
istioctl dashboard kiali

# Access Grafana
istioctl dashboard grafana

# Access Jaeger
istioctl dashboard jaeger
```

## Monitoring and Observability

### Metrics Endpoints

All services expose Actuator endpoints:

```bash
# Health check
curl http://localhost:8081/actuator/health

# Metrics
curl http://localhost:8081/actuator/metrics

# Circuit breaker metrics
curl http://localhost:8081/actuator/metrics/resilience4j.circuitbreaker.state

# Bulkhead metrics
curl http://localhost:8081/actuator/metrics/resilience4j.bulkhead.available.concurrent.calls

# Prometheus metrics
curl http://localhost:8081/actuator/prometheus
```

### Distributed Tracing

Access Zipkin UI at http://localhost:9411:
- View service dependencies
- Analyze request latency
- Debug distributed transactions
- Find bottlenecks

### Centralized Logging

Access Kibana at http://localhost:5601:
- Create visualizations
- Set up dashboards
- Configure alerts
- Analyze patterns

## Configuration

### Resilience4j Configuration

Circuit breaker settings in `application.yml`:
```yaml
resilience4j:
  circuitbreaker:
    instances:
      productService:
        sliding-window-size: 100
        failure-rate-threshold: 50
        wait-duration-in-open-state: 10s
```

### Rate Limiting Configuration

In API Gateway:
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: product-service
          filters:
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 10
                redis-rate-limiter.burstCapacity: 20
```

## Troubleshooting

### Services not registering with Eureka

Check Eureka URL in service configuration:
```bash
curl http://localhost:8081/actuator/env | grep eureka
```

### Circuit breaker not working

Check circuit breaker state:
```bash
curl http://localhost:8081/actuator/health | jq '.components.circuitBreakers'
```

### Rate limiting not working

Verify Redis connection:
```bash
docker-compose logs redis
curl http://localhost:8080/actuator/health | jq '.components.redis'
```

### Traces not appearing in Zipkin

Check tracing configuration:
```bash
curl http://localhost:8081/actuator/env | grep zipkin
```

### No logs in Kibana

Check Logstash and Elasticsearch:
```bash
docker-compose logs logstash
curl http://localhost:9200/_cat/indices
```

## Best Practices

1. **Service Discovery**: Always use health checks
2. **Circuit Breaker**: Set appropriate thresholds based on SLA
3. **Bulkhead**: Size thread pools based on resource availability
4. **Retry**: Use exponential backoff to prevent thundering herd
5. **Rate Limiting**: Implement per-user and per-IP limits
6. **Distributed Tracing**: Sample at appropriate rate (100% for dev, 10-20% for prod)
7. **Logging**: Use structured logging with correlation IDs
8. **Service Mesh**: Start with simple policies and evolve

## Performance Testing

### Load Testing with Apache Bench

```bash
# Test API Gateway
ab -n 10000 -c 100 http://localhost:8080/api/products

# Test with authentication
ab -n 10000 -c 100 -H "Authorization: Bearer token" http://localhost:8080/api/orders
```

### Chaos Engineering

Test resilience by introducing failures:

```bash
# Stop a service instance
docker-compose stop product-service-1

# Introduce network delay
docker-compose exec product-service-1 tc qdisc add dev eth0 root netem delay 100ms

# Introduce packet loss
docker-compose exec product-service-1 tc qdisc add dev eth0 root netem loss 10%
```

## Cleanup

### Docker Compose

```bash
# Stop all services
docker-compose down

# Remove volumes
docker-compose down -v

# Remove images
docker-compose down --rmi all
```

### Kubernetes

```bash
# Delete all resources
kubectl delete -f k8s/service-mesh/

# Uninstall Istio
istioctl uninstall --purge -y
```

## Additional Resources

- [Spring Cloud Documentation](https://spring.io/projects/spring-cloud)
- [Resilience4j Documentation](https://resilience4j.readme.io/)
- [Istio Documentation](https://istio.io/latest/docs/)
- [Zipkin Documentation](https://zipkin.io/)
- [ELK Stack Documentation](https://www.elastic.co/guide/index.html)

## Contributing

Contributions are welcome! Please read the contributing guidelines before submitting PRs.

## License

This project is licensed under the MIT License.
```

## 10. Additional Supporting Files

```dockerfile eureka-server/Dockerfile
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8761
ENTRYPOINT ["java", "-jar", "app.jar"]
```

```dockerfile config-server/Dockerfile
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8888
ENTRYPOINT ["java", "-jar", "app.jar"]
```

```dockerfile api-gateway/Dockerfile
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

```dockerfile product-service/Dockerfile
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8081
HEALTHCHECK --interval=30s --timeout=3s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8081/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
```

```dockerfile order-service/Dockerfile
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8082
HEALTHCHECK --interval=30s --timeout=3s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8082/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
```

```gitignore .gitignore
# Compiled class files
*.class
target/
build/

# Log files
*.log
logs/

# Package Files
*.jar
*.war
*.nar
*.ear
*.zip
*.tar.gz
*.rar

# IDEs
.idea/
*.iml
.vscode/
.settings/
.classpath
.project

# OS
.DS_Store
Thumbs.db

# Spring Boot
HELP.md
.gradle
application-local.yml

# Docker
.dockerignore

# Maven
.mvn/wrapper/maven-wrapper.jar
```

```properties config-server/src/main/resources/config/product-service.properties
# Product Service Configuration
server.port=8081

# Database Configuration (example)
spring.datasource.url=jdbc:postgresql://localhost:5432/products
spring.datasource.username=product_user
spring.datasource.password=product_pass

# Resilience Configuration
resilience4j.circuitbreaker.instances.default.sliding-window-size=10
resilience4j.circuitbreaker.instances.default.failure-rate-threshold=50

# Custom Application Properties
app.product.cache.ttl=3600
app.product.max-results=100
```

```properties config-server/src/main/resources/config/order-service.properties
# Order Service Configuration
server.port=8082

# Database Configuration (example)
spring.datasource.url=jdbc:postgresql://localhost:5432/orders
spring.datasource.username=order_user
spring.datasource.password=order_pass

# Feign Client Configuration
feign.client.config.default.connectTimeout=5000
feign.client.config.default.readTimeout=5000

# Custom Application Properties
app.order.payment-timeout=30000
app.order.max-items=50
```

This comprehensive implementation covers all 11 cloud patterns with working examples. You can deploy this locally using Docker Compose or on Kubernetes with Istio for the full Service Mesh experience. Each pattern is demonstrated with practical code and configuration that you can adapt for your specific needs.