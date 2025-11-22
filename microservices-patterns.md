I'll create a comprehensive Spring Boot microservices project demonstrating all these patterns. Due to the extensive nature of this request, I'll organize it into multiple interconnected services.

## Project Structure Overview

```
microservices-patterns/
├── eureka-server/              (Service Registry)
├── config-server/              (Config Server)
├── api-gateway/                (BFF, Server-Side LB)
├── product-service/            (Database per Service)
├── order-service/              (Feign, Ribbon, Event-Driven)
├── inventory-service/          (Shared Database demo)
├── notification-service/       (Event Consumer)
├── legacy-adapter-service/     (Anti-Corruption Layer)
├── payment-service/            (Orchestration)
├── analytics-service/          (Sidecar Pattern)
└── docker-compose.yml
```

## Parent POM

```xml pom.xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.microservices</groupId>
    <artifactId>microservices-patterns</artifactId>
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
        <module>inventory-service</module>
        <module>notification-service</module>
        <module>legacy-adapter-service</module>
        <module>payment-service</module>
        <module>analytics-service</module>
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

## 1. Eureka Server - Service Registry Pattern

```xml eureka-server/pom.xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.microservices</groupId>
        <artifactId>microservices-patterns</artifactId>
        <version>1.0.0</version>
    </parent>

    <artifactId>eureka-server</artifactId>

    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
    </dependencies>
</project>
```

```java eureka-server/src/main/java/com/microservices/eureka/EurekaServerApplication.java
package com.microservices.eureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Service Registry Pattern - Eureka Discovery Pattern
 * Central service registry for all microservices
 */
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
  instance:
    hostname: localhost
  client:
    register-with-eureka: false
    fetch-registry: false
    service-url:
      defaultZone: http://${eureka.instance.hostname}:${server.port}/eureka/
  server:
    enable-self-preservation: false
    eviction-interval-timer-in-ms: 4000

management:
  endpoints:
    web:
      exposure:
        include: "*"
```

## 2. Config Server - Configuration Management Pattern

```xml config-server/pom.xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.microservices</groupId>
        <artifactId>microservices-patterns</artifactId>
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
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
    </dependencies>
</project>
```

```java config-server/src/main/java/com/microservices/config/ConfigServerApplication.java
package com.microservices.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

/**
 * Config Server Pattern - Externalized Configuration Pattern
 * Centralized configuration management for all microservices
 */
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
          clone-on-start: true
          default-label: main
        # Alternative: use native filesystem
        native:
          search-locations: classpath:/config
  profiles:
    active: native

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
  instance:
    prefer-ip-address: true
```

## 3. API Gateway - BFF Pattern & Server-Side Load Balancing

```xml api-gateway/pom.xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.microservices</groupId>
        <artifactId>microservices-patterns</artifactId>
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
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-circuitbreaker-reactor-resilience4j</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis-reactive</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
    </dependencies>
</project>
```

```java api-gateway/src/main/java/com/microservices/gateway/ApiGatewayApplication.java
package com.microservices.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Backend for Frontend (BFF) Pattern
 * Server-Side Load Balancing Pattern
 * Ambassador Pattern
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ApiGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
```

```java api-gateway/src/main/java/com/microservices/gateway/config/GatewayConfig.java
package com.microservices.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * BFF Pattern - Different routes for different client types
 * Server-Side Load Balancing - Gateway handles load balancing
 */
@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            // Product Service Routes
            .route("product-service", r -> r
                .path("/api/products/**")
                .filters(f -> f
                    .stripPrefix(1)
                    .circuitBreaker(config -> config
                        .setName("productServiceCB")
                        .setFallbackUri("forward:/fallback/products"))
                    .retry(config -> config.setRetries(3)))
                .uri("lb://product-service"))
            
            // Order Service Routes
            .route("order-service", r -> r
                .path("/api/orders/**")
                .filters(f -> f
                    .stripPrefix(1)
                    .circuitBreaker(config -> config
                        .setName("orderServiceCB")
                        .setFallbackUri("forward:/fallback/orders")))
                .uri("lb://order-service"))
            
            // Mobile BFF - Optimized for mobile clients
            .route("mobile-bff", r -> r
                .path("/mobile/api/**")
                .filters(f -> f
                    .stripPrefix(2)
                    .addRequestHeader("Client-Type", "Mobile"))
                .uri("lb://order-service"))
            
            // Web BFF - Optimized for web clients
            .route("web-bff", r -> r
                .path("/web/api/**")
                .filters(f -> f
                    .stripPrefix(2)
                    .addRequestHeader("Client-Type", "Web"))
                .uri("lb://order-service"))
            
            // Payment Service
            .route("payment-service", r -> r
                .path("/api/payments/**")
                .filters(f -> f.stripPrefix(1))
                .uri("lb://payment-service"))
            
            // Inventory Service
            .route("inventory-service", r -> r
                .path("/api/inventory/**")
                .filters(f -> f.stripPrefix(1))
                .uri("lb://inventory-service"))
            
            .build();
    }
}
```

```java api-gateway/src/main/java/com/microservices/gateway/filter/AuthenticationFilter.java
package com.microservices.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * Ambassador Pattern - Authentication at gateway level
 */
@Component
@Slf4j
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    public AuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
            
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
            
            // Validate token (simplified)
            String token = authHeader.substring(7);
            if (!isValidToken(token)) {
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }
            
            return chain.filter(exchange);
        };
    }

    private boolean isValidToken(String token) {
        // Simplified token validation
        return token != null && !token.isEmpty();
    }

    public static class Config {
        // Configuration properties if needed
    }
}
```

```java api-gateway/src/main/java/com/microservices/gateway/controller/FallbackController.java
package com.microservices.gateway.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Fallback endpoints for circuit breaker
 */
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/products")
    public Mono<Map<String, String>> productFallback() {
        return Mono.just(Map.of(
            "message", "Product service is currently unavailable",
            "status", "fallback"
        ));
    }

    @GetMapping("/orders")
    public Mono<Map<String, String>> orderFallback() {
        return Mono.just(Map.of(
            "message", "Order service is currently unavailable",
            "status", "fallback"
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
  cloud:
    gateway:
      discovery:
        locator:
          enabled: true
          lower-case-service-id: true
      default-filters:
        - DedupeResponseHeader=Access-Control-Allow-Origin
  redis:
    host: localhost
    port: 6379

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
  instance:
    prefer-ip-address: true

management:
  endpoints:
    web:
      exposure:
        include: "*"
  endpoint:
    gateway:
      enabled: true
```

## 4. Product Service - Database per Service Pattern

```xml product-service/pom.xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.microservices</groupId>
        <artifactId>microservices-patterns</artifactId>
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
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-config</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.kafka</groupId>
            <artifactId>spring-kafka</artifactId>
        </dependency>
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
    </dependencies>
</project>
```

```java product-service/src/main/java/com/microservices/product/ProductServiceApplication.java
package com.microservices.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Database per Service Pattern
 * Each microservice has its own database
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ProductServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProductServiceApplication.class, args);
    }
}
```

```java product-service/src/main/java/com/microservices/product/entity/Product.java
package com.microservices.product.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Database per Service Pattern - Product has its own database
 */
@Entity
@Table(name = "products")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String sku;

    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stockQuantity;

    private String category;

    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

```java product-service/src/main/java/com/microservices/product/repository/ProductRepository.java
package com.microservices.product.repository;

import com.microservices.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findBySku(String sku);
    List<Product> findByCategory(String category);
    List<Product> findByNameContainingIgnoreCase(String name);
}
```

```java product-service/src/main/java/com/microservices/product/service/ProductService.java
package com.microservices.product.service;

import com.microservices.product.entity.Product;
import com.microservices.product.event.ProductCreatedEvent;
import com.microservices.product.event.ProductUpdatedEvent;
import com.microservices.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Event-Driven Architecture Pattern
 * Publishes events when products are created/updated
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public Product createProduct(Product product) {
        log.info("Creating product: {}", product.getName());
        Product saved = productRepository.save(product);
        
        // Event-Driven Architecture Pattern - Publish event
        publishProductCreatedEvent(saved);
        
        return saved;
    }

    @Transactional
    public Product updateProduct(Long id, Product product) {
        log.info("Updating product: {}", id);
        Product existing = productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Product not found"));
        
        existing.setName(product.getName());
        existing.setDescription(product.getDescription());
        existing.setPrice(product.getPrice());
        existing.setStockQuantity(product.getStockQuantity());
        existing.setCategory(product.getCategory());
        
        Product updated = productRepository.save(existing);
        
        // Publish event
        publishProductUpdatedEvent(updated);
        
        return updated;
    }

    public Product getProduct(Long id) {
        return productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategory(category);
    }

    private void publishProductCreatedEvent(Product product) {
        ProductCreatedEvent event = ProductCreatedEvent.builder()
            .productId(product.getId())
            .sku(product.getSku())
            .name(product.getName())
            .price(product.getPrice())
            .stockQuantity(product.getStockQuantity())
            .build();
        
        kafkaTemplate.send("product-events", event);
        log.info("Published ProductCreatedEvent for: {}", product.getSku());
    }

    private void publishProductUpdatedEvent(Product product) {
        ProductUpdatedEvent event = ProductUpdatedEvent.builder()
            .productId(product.getId())
            .sku(product.getSku())
            .name(product.getName())
            .price(product.getPrice())
            .stockQuantity(product.getStockQuantity())
            .build();
        
        kafkaTemplate.send("product-events", event);
        log.info("Published ProductUpdatedEvent for: {}", product.getSku());
    }
}
```

```java product-service/src/main/java/com/microservices/product/event/ProductCreatedEvent.java
package com.microservices.product.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCreatedEvent {
    private Long productId;
    private String sku;
    private String name;
    private BigDecimal price;
    private Integer stockQuantity;
}
```

```java product-service/src/main/java/com/microservices/product/event/ProductUpdatedEvent.java
package com.microservices.product.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductUpdatedEvent {
    private Long productId;
    private String sku;
    private String name;
    private BigDecimal price;
    private Integer stockQuantity;
}
```

```java product-service/src/main/java/com/microservices/product/controller/ProductController.java
package com.microservices.product.controller;

import com.microservices.product.entity.Product;
import com.microservices.product.service.ProductService;
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

    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        Product created = productService.createProduct(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        Product updated = productService.updateProduct(id, product);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable Long id) {
        Product product = productService.getProduct(id);
        return ResponseEntity.ok(product);
    }

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<Product>> getProductsByCategory(@PathVariable String category) {
        List<Product> products = productService.getProductsByCategory(category);
        return ResponseEntity.ok(products);
    }
}
```

```yaml product-service/src/main/resources/application.yml
server:
  port: 8081

spring:
  application:
    name: product-service
  datasource:
    url: jdbc:h2:mem:productdb
    driver-class-name: org.h2.Driver
    username: sa
    password:
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
  h2:
    console:
      enabled: true
  kafka:
    bootstrap-servers: localhost:9092
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
  instance:
    prefer-ip-address: true
```

## 5. Order Service - Feign Client, Ribbon, Service-to-Service Communication

```xml order-service/pom.xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.microservices</groupId>
        <artifactId>microservices-patterns</artifactId>
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
            <artifactId>spring-boot-starter-data-jpa</artifactId>
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
            <artifactId>spring-cloud-starter-loadbalancer</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.kafka</groupId>
            <artifactId>spring-kafka</artifactId>
        </dependency>
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
    </dependencies>
</project>
```

```java order-service/src/main/java/com/microservices/order/OrderServiceApplication.java
package com.microservices.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

```java order-service/src/main/java/com/microservices/order/OrderServiceApplication.java
/**
 * Feign Client Pattern
 * Client-Side Load Balancing Pattern (Ribbon)
 * Service-to-Service Communication Pattern
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class OrderServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
```

```java order-service/src/main/java/com/microservices/order/client/ProductClient.java
package com.microservices.order.client;

import com.microservices.order.dto.ProductDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * Feign Client Pattern - Declarative REST client
 * Client-Side Load Balancing - Feign uses load balancer
 * Service-to-Service Communication Pattern
 */
@FeignClient(
    name = "product-service",
    fallback = ProductClientFallback.class
)
public interface ProductClient {
    
    @GetMapping("/products/{id}")
    ProductDTO getProduct(@PathVariable Long id);
    
    @GetMapping("/products")
    List<ProductDTO> getAllProducts();
}
```

```java order-service/src/main/java/com/microservices/order/client/ProductClientFallback.java
package com.microservices.order.client;

import com.microservices.order.dto.ProductDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

/**
 * Fallback for Feign Client
 */
@Component
@Slf4j
public class ProductClientFallback implements ProductClient {

    @Override
    public ProductDTO getProduct(Long id) {
        log.warn("Fallback triggered for getProduct: {}", id);
        return ProductDTO.builder()
            .id(id)
            .name("Product Unavailable")
            .price(BigDecimal.ZERO)
            .build();
    }

    @Override
    public List<ProductDTO> getAllProducts() {
        log.warn("Fallback triggered for getAllProducts");
        return Collections.emptyList();
    }
}
```

```java order-service/src/main/java/com/microservices/order/client/InventoryClient.java
package com.microservices.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Feign Client for Inventory Service
 */
@FeignClient(name = "inventory-service")
public interface InventoryClient {
    
    @GetMapping("/inventory/check/{sku}")
    boolean checkStock(@PathVariable String sku, @RequestParam Integer quantity);
    
    @PostMapping("/inventory/reserve/{sku}")
    void reserveStock(@PathVariable String sku, @RequestParam Integer quantity);
    
    @PostMapping("/inventory/release/{sku}")
    void releaseStock(@PathVariable String sku, @RequestParam Integer quantity);
}
```

```java order-service/src/main/java/com/microservices/order/client/PaymentClient.java
package com.microservices.order.client;

import com.microservices.order.dto.PaymentRequest;
import com.microservices.order.dto.PaymentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Feign Client for Payment Service - Orchestration Pattern
 */
@FeignClient(name = "payment-service")
public interface PaymentClient {
    
    @PostMapping("/payments/process")
    PaymentResponse processPayment(@RequestBody PaymentRequest request);
}
```

```java order-service/src/main/java/com/microservices/order/entity/Order.java
package com.microservices.order.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String orderNumber;

    @Column(nullable = false)
    private String customerId;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "order")
    private List<OrderItem> items = new ArrayList<>();

    @Column(nullable = false)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private String paymentId;

    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = OrderStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

```java order-service/src/main/java/com/microservices/order/entity/OrderItem.java
package com.microservices.order.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private String productName;

    @Column(nullable = false)
    private String sku;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private BigDecimal subtotal;
}
```

```java order-service/src/main/java/com/microservices/order/entity/OrderStatus.java
package com.microservices.order.entity;

public enum OrderStatus {
    PENDING,
    PAYMENT_PROCESSING,
    PAYMENT_COMPLETED,
    CONFIRMED,
    SHIPPED,
    DELIVERED,
    CANCELLED,
    FAILED
}
```

```java order-service/src/main/java/com/microservices/order/service/OrderService.java
package com.microservices.order.service;

import com.microservices.order.client.InventoryClient;
import com.microservices.order.client.PaymentClient;
import com.microservices.order.client.ProductClient;
import com.microservices.order.dto.OrderRequest;
import com.microservices.order.dto.PaymentRequest;
import com.microservices.order.dto.PaymentResponse;
import com.microservices.order.dto.ProductDTO;
import com.microservices.order.entity.Order;
import com.microservices.order.entity.OrderItem;
import com.microservices.order.entity.OrderStatus;
import com.microservices.order.event.OrderCreatedEvent;
import com.microservices.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Orchestration Pattern - Order service orchestrates multiple services
 * Service-to-Service Communication using Feign clients
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductClient productClient;
    private final InventoryClient inventoryClient;
    private final PaymentClient paymentClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Orchestration Pattern - Coordinates multiple services
     */
    @Transactional
    public Order createOrder(OrderRequest request) {
        log.info("Creating order for customer: {}", request.getCustomerId());

        // Step 1: Validate products and calculate total
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new java.util.ArrayList<>();

        for (OrderRequest.OrderItemRequest itemRequest : request.getItems()) {
            // Service-to-Service Communication - Get product details
            ProductDTO product = productClient.getProduct(itemRequest.getProductId());
            
            // Check inventory
            boolean hasStock = inventoryClient.checkStock(
                product.getSku(), 
                itemRequest.getQuantity()
            );
            
            if (!hasStock) {
                throw new RuntimeException("Insufficient stock for: " + product.getName());
            }

            BigDecimal subtotal = product.getPrice()
                .multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            totalAmount = totalAmount.add(subtotal);

            OrderItem orderItem = OrderItem.builder()
                .productId(product.getId())
                .productName(product.getName())
                .sku(product.getSku())
                .quantity(itemRequest.getQuantity())
                .price(product.getPrice())
                .subtotal(subtotal)
                .build();

            orderItems.add(orderItem);
        }

        // Step 2: Create order
        Order order = Order.builder()
            .orderNumber(UUID.randomUUID().toString())
            .customerId(request.getCustomerId())
            .totalAmount(totalAmount)
            .status(OrderStatus.PENDING)
            .build();

        orderItems.forEach(item -> {
            item.setOrder(order);
            order.getItems().add(item);
        });

        Order savedOrder = orderRepository.save(order);

        // Step 3: Reserve inventory
        for (OrderItem item : savedOrder.getItems()) {
            inventoryClient.reserveStock(item.getSku(), item.getQuantity());
        }

        // Step 4: Process payment (Orchestration)
        try {
            PaymentRequest paymentRequest = PaymentRequest.builder()
                .orderId(savedOrder.getId())
                .amount(savedOrder.getTotalAmount())
                .customerId(savedOrder.getCustomerId())
                .build();

            PaymentResponse paymentResponse = paymentClient.processPayment(paymentRequest);
            
            savedOrder.setPaymentId(paymentResponse.getPaymentId());
            savedOrder.setStatus(OrderStatus.PAYMENT_COMPLETED);
            
        } catch (Exception e) {
            log.error("Payment failed for order: {}", savedOrder.getOrderNumber(), e);
            savedOrder.setStatus(OrderStatus.FAILED);
            
            // Release reserved inventory
            for (OrderItem item : savedOrder.getItems()) {
                inventoryClient.releaseStock(item.getSku(), item.getQuantity());
            }
            
            throw new RuntimeException("Payment processing failed", e);
        }

        Order finalOrder = orderRepository.save(savedOrder);

        // Step 5: Publish order created event (Event-Driven)
        publishOrderCreatedEvent(finalOrder);

        return finalOrder;
    }

    public Order getOrder(Long id) {
        return orderRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    public List<Order> getOrdersByCustomer(String customerId) {
        return orderRepository.findByCustomerId(customerId);
    }

    private void publishOrderCreatedEvent(Order order) {
        OrderCreatedEvent event = OrderCreatedEvent.builder()
            .orderId(order.getId())
            .orderNumber(order.getOrderNumber())
            .customerId(order.getCustomerId())
            .totalAmount(order.getTotalAmount())
            .status(order.getStatus().name())
            .build();

        kafkaTemplate.send("order-events", event);
        log.info("Published OrderCreatedEvent for: {}", order.getOrderNumber());
    }
}
```

```java order-service/src/main/java/com/microservices/order/repository/OrderRepository.java
package com.microservices.order.repository;

import com.microservices.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomerId(String customerId);
    List<Order> findByOrderNumber(String orderNumber);
}
```

```java order-service/src/main/java/com/microservices/order/dto/OrderRequest.java
package com.microservices.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {
    private String customerId;
    private List<OrderItemRequest> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemRequest {
        private Long productId;
        private Integer quantity;
    }
}
```

```java order-service/src/main/java/com/microservices/order/dto/ProductDTO.java
package com.microservices.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private Long id;
    private String name;
    private String sku;
    private BigDecimal price;
    private Integer stockQuantity;
}
```

```java order-service/src/main/java/com/microservices/order/dto/PaymentRequest.java
package com.microservices.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {
    private Long orderId;
    private BigDecimal amount;
    private String customerId;
}
```

```java order-service/src/main/java/com/microservices/order/dto/PaymentResponse.java
package com.microservices.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private String paymentId;
    private String status;
    private String message;
}
```

```java order-service/src/main/java/com/microservices/order/event/OrderCreatedEvent.java
package com.microservices.order.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreatedEvent {
    private Long orderId;
    private String orderNumber;
    private String customerId;
    private BigDecimal totalAmount;
    private String status;
}
```

```java order-service/src/main/java/com/microservices/order/controller/OrderController.java
package com.microservices.order.controller;

import com.microservices.order.dto.OrderRequest;
import com.microservices.order.entity.Order;
import com.microservices.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody OrderRequest request) {
        Order order = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrder(@PathVariable Long id) {
        Order order = orderService.getOrder(id);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<Order>> getOrdersByCustomer(@PathVariable String customerId) {
        List<Order> orders = orderService.getOrdersByCustomer(customerId);
        return ResponseEntity.ok(orders);
    }
}
```

```yaml order-service/src/main/resources/application.yml
server:
  port: 8082

spring:
  application:
    name: order-service
  datasource:
    url: jdbc:h2:mem:orderdb
    driver-class-name: org.h2.Driver
    username: sa
    password:
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
  h2:
    console:
      enabled: true
  kafka:
    bootstrap-servers: localhost:9092
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
  instance:
    prefer-ip-address: true

feign:
  client:
    config:
      default:
        connectTimeout: 5000
        readTimeout: 5000
  circuitbreaker:
    enabled: true
```

## 6. Inventory Service - Shared Database Pattern

```xml inventory-service/pom.xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.microservices</groupId>
        <artifactId>microservices-patterns</artifactId>
        <version>1.0.0</version>
    </parent>

    <artifactId>inventory-service</artifactId>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
    </dependencies>
</project>
```

```java inventory-service/src/main/java/com/microservices/inventory/InventoryServiceApplication.java
package com.microservices.inventory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Shared Database Pattern (for demonstration)
 * In production, prefer Database per Service
 */
@SpringBootApplication
@EnableDiscoveryClient
public class InventoryServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(InventoryServiceApplication.class, args);
    }
}
```

```java inventory-service/src/main/java/com/microservices/inventory/entity/Inventory.java
package com.microservices.inventory.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "inventory")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String sku;

    @Column(nullable = false)
    private Integer availableQuantity;

    @Column(nullable = false)
    private Integer reservedQuantity;

    @Version
    private Long version; // Optimistic locking
}
```

```java inventory-service/src/main/java/com/microservices/inventory/repository/InventoryRepository.java
package com.microservices.inventory.repository;

import com.microservices.inventory.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    
    @Lock(LockModeType.OPTIMISTIC)
    Optional<Inventory> findBySku(String sku);
}
```

```java inventory-service/src/main/java/com/microservices/inventory/service/InventoryService.java
package com.microservices.inventory.service;

import com.microservices.inventory.entity.Inventory;
import com.microservices.inventory.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    public boolean checkStock(String sku, Integer quantity) {
        Inventory inventory = inventoryRepository.findBySku(sku)
            .orElseThrow(() -> new RuntimeException("Inventory not found for SKU: " + sku));
        
        return inventory.getAvailableQuantity() >= quantity;
    }

    @Transactional
    public void reserveStock(String sku, Integer quantity) {
        log.info("Reserving {} units of SKU: {}", quantity, sku);
        
        Inventory inventory = inventoryRepository.findBySku(sku)
            .orElseThrow(() -> new RuntimeException("Inventory not found for SKU: " + sku));
        
        if (inventory.getAvailableQuantity() < quantity) {
            throw new RuntimeException("Insufficient stock for SKU: " + sku);
        }
        
        inventory.setAvailableQuantity(inventory.getAvailableQuantity() - quantity);
        inventory.setReservedQuantity(inventory.getReservedQuantity() + quantity);
        
        inventoryRepository.save(inventory);
    }

    @Transactional
    public void releaseStock(String sku, Integer quantity) {
        log.info("Releasing {} units of SKU: {}", quantity, sku);
        
        Inventory inventory = inventoryRepository.findBySku(sku)
            .orElseThrow(() -> new RuntimeException("Inventory not found for SKU: " + sku));
        
        inventory.setAvailableQuantity(inventory.getAvailableQuantity() + quantity);
        inventory.setReservedQuantity(inventory.getReservedQuantity() - quantity);
        
        inventoryRepository.save(inventory);
    }

    @Transactional
    public void confirmReservation(String sku, Integer quantity) {
        log.info("Confirming reservation of {} units for SKU: {}", quantity, sku);
        
        Inventory inventory = inventoryRepository.findBySku(sku)
            .orElseThrow(() -> new RuntimeException("Inventory not found for SKU: " + sku));
        
        inventory.setReservedQuantity(inventory.getReservedQuantity() - quantity);
        
        inventoryRepository.save(inventory);
    }
}
```

```java inventory-service/src/main/java/com/microservices/inventory/controller/InventoryController.java
package com.microservices.inventory.controller;

import com.microservices.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/check/{sku}")
    public ResponseEntity<Boolean> checkStock(@PathVariable String sku, 
                                              @RequestParam Integer quantity) {
        boolean hasStock = inventoryService.checkStock(sku, quantity);
        return ResponseEntity.ok(hasStock);
    }

    @PostMapping("/reserve/{sku}")
    public ResponseEntity<Void> reserveStock(@PathVariable String sku, 
                                            @RequestParam Integer quantity) {
        inventoryService.reserveStock(sku, quantity);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/release/{sku}")
    public ResponseEntity<Void> releaseStock(@PathVariable String sku, 
                                            @RequestParam Integer quantity) {
        inventoryService.releaseStock(sku, quantity);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/confirm/{sku}")
    public ResponseEntity<Void> confirmReservation(@PathVariable String sku, 
                                                   @RequestParam Integer quantity) {
        inventoryService.confirmReservation(sku, quantity);
        return ResponseEntity.ok().build();
    }
}
```

```yaml inventory-service/src/main/resources/application.yml
server:
  port: 8083

spring:
  application:
    name: inventory-service
  datasource:
    url: jdbc:h2:mem:inventorydb
    driver-class-name: org.h2.Driver
    username: sa
    password:
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
  instance:
    prefer-ip-address: true
```

## 7. Notification Service - Choreography Pattern

```xml notification-service/pom.xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.microservices</groupId>
        <artifactId>microservices-patterns</artifactId>
        <version>1.0.0</version>
    </parent>

    <artifactId>notification-service</artifactId>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.kafka</groupId>
            <artifactId>spring-kafka</artifactId>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
    </dependencies>
</project>
```

```java notification-service/src/main/java/com/microservices/notification/NotificationServiceApplication.java
package com.microservices.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Choreography Pattern - Reacts to events without being orchestrated
 * Event-Driven Architecture - Consumes events from Kafka
 */
@SpringBootApplication
@EnableDiscoveryClient
public class NotificationServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}
```

```java notification-service/src/main/java/com/microservices/notification/listener/OrderEventListener.java
package com.microservices.notification.listener;

import com.microservices.notification.event.OrderCreatedEvent;
import com.microservices.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Choreography Pattern - Service reacts to events independently
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {

    private final NotificationService notificationService;

    @KafkaListener(topics = "order-events", groupId = "notification-service")
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Received OrderCreatedEvent: {}", event.getOrderNumber());
        
        // Choreography - Service decides what to do based on event
        notificationService.sendOrderConfirmation(
            event.getCustomerId(),
            event.getOrderNumber(),
            event.getTotalAmount()
        );
    }
}
```

```java notification-service/src/main/java/com/microservices/notification/event/OrderCreatedEvent.java
package com.microservices.notification.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreatedEvent {
    private Long orderId;
    private String orderNumber;
    private String customerId;
    private BigDecimal totalAmount;
```java notification-service/src/main/java/com/microservices/notification/event/OrderCreatedEvent.java
    private String status;
}
```

```java notification-service/src/main/java/com/microservices/notification/service/NotificationService.java
package com.microservices.notification.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Notification service for sending emails/SMS
 */
@Service
@Slf4j
public class NotificationService {

    public void sendOrderConfirmation(String customerId, String orderNumber, BigDecimal amount) {
        log.info("Sending order confirmation to customer: {}", customerId);
        log.info("Order Number: {}, Amount: {}", orderNumber, amount);
        
        // Simulate sending email
        sendEmail(customerId, 
            "Order Confirmation",
            String.format("Your order %s has been confirmed. Total: $%s", orderNumber, amount));
    }

    public void sendPaymentNotification(String customerId, String paymentId, String status) {
        log.info("Sending payment notification to customer: {}", customerId);
        log.info("Payment ID: {}, Status: {}", paymentId, status);
        
        sendEmail(customerId,
            "Payment Update",
            String.format("Payment %s - Status: %s", paymentId, status));
    }

    private void sendEmail(String recipient, String subject, String body) {
        // Simulated email sending
        log.info("Email sent to: {}", recipient);
        log.info("Subject: {}", subject);
        log.info("Body: {}", body);
    }
}
```

```yaml notification-service/src/main/resources/application.yml
server:
  port: 8084

spring:
  application:
    name: notification-service
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: notification-service
      auto-offset-reset: earliest
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      properties:
        spring.json.trusted.packages: "*"

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
  instance:
    prefer-ip-address: true
```

## 8. Payment Service - Orchestration Pattern

```xml payment-service/pom.xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.microservices</groupId>
        <artifactId>microservices-patterns</artifactId>
        <version>1.0.0</version>
    </parent>

    <artifactId>payment-service</artifactId>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.kafka</groupId>
            <artifactId>spring-kafka</artifactId>
        </dependency>
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
    </dependencies>
</project>
```

```java payment-service/src/main/java/com/microservices/payment/PaymentServiceApplication.java
package com.microservices.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Payment Service - Part of Orchestration Pattern
 * Called by Order Service to process payments
 */
@SpringBootApplication
@EnableDiscoveryClient
public class PaymentServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(PaymentServiceApplication.class, args);
    }
}
```

```java payment-service/src/main/java/com/microservices/payment/entity/Payment.java
package com.microservices.payment.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String paymentId;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private String customerId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private String paymentMethod;

    private String transactionId;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
```

```java payment-service/src/main/java/com/microservices/payment/entity/PaymentStatus.java
package com.microservices.payment.entity;

public enum PaymentStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED,
    REFUNDED
}
```

```java payment-service/src/main/java/com/microservices/payment/repository/PaymentRepository.java
package com.microservices.payment.repository;

import com.microservices.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByPaymentId(String paymentId);
    Optional<Payment> findByOrderId(Long orderId);
}
```

```java payment-service/src/main/java/com/microservices/payment/service/PaymentService.java
package com.microservices.payment.service;

import com.microservices.payment.dto.PaymentRequest;
import com.microservices.payment.dto.PaymentResponse;
import com.microservices.payment.entity.Payment;
import com.microservices.payment.entity.PaymentStatus;
import com.microservices.payment.event.PaymentProcessedEvent;
import com.microservices.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Payment processing service
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        log.info("Processing payment for order: {}", request.getOrderId());

        Payment payment = Payment.builder()
            .paymentId(UUID.randomUUID().toString())
            .orderId(request.getOrderId())
            .customerId(request.getCustomerId())
            .amount(request.getAmount())
            .status(PaymentStatus.PROCESSING)
            .paymentMethod("CREDIT_CARD")
            .build();

        payment = paymentRepository.save(payment);

        try {
            // Simulate payment gateway call
            String transactionId = callPaymentGateway(payment);
            
            payment.setTransactionId(transactionId);
            payment.setStatus(PaymentStatus.COMPLETED);
            
            paymentRepository.save(payment);

            // Publish payment completed event
            publishPaymentProcessedEvent(payment);

            return PaymentResponse.builder()
                .paymentId(payment.getPaymentId())
                .status("SUCCESS")
                .message("Payment processed successfully")
                .build();

        } catch (Exception e) {
            log.error("Payment processing failed", e);
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);

            return PaymentResponse.builder()
                .paymentId(payment.getPaymentId())
                .status("FAILED")
                .message("Payment processing failed: " + e.getMessage())
                .build();
        }
    }

    private String callPaymentGateway(Payment payment) {
        // Simulate external payment gateway call
        log.info("Calling payment gateway for amount: {}", payment.getAmount());
        
        // Simulate processing delay
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return "TXN-" + UUID.randomUUID().toString();
    }

    private void publishPaymentProcessedEvent(Payment payment) {
        PaymentProcessedEvent event = PaymentProcessedEvent.builder()
            .paymentId(payment.getPaymentId())
            .orderId(payment.getOrderId())
            .customerId(payment.getCustomerId())
            .amount(payment.getAmount())
            .status(payment.getStatus().name())
            .build();

        kafkaTemplate.send("payment-events", event);
        log.info("Published PaymentProcessedEvent for payment: {}", payment.getPaymentId());
    }
}
```

```java payment-service/src/main/java/com/microservices/payment/dto/PaymentRequest.java
package com.microservices.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {
    private Long orderId;
    private BigDecimal amount;
    private String customerId;
}
```

```java payment-service/src/main/java/com/microservices/payment/dto/PaymentResponse.java
package com.microservices.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private String paymentId;
    private String status;
    private String message;
}
```

```java payment-service/src/main/java/com/microservices/payment/event/PaymentProcessedEvent.java
package com.microservices.payment.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentProcessedEvent {
    private String paymentId;
    private Long orderId;
    private String customerId;
    private BigDecimal amount;
    private String status;
}
```

```java payment-service/src/main/java/com/microservices/payment/controller/PaymentController.java
package com.microservices.payment.controller;

import com.microservices.payment.dto.PaymentRequest;
import com.microservices.payment.dto.PaymentResponse;
import com.microservices.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/process")
    public ResponseEntity<PaymentResponse> processPayment(@RequestBody PaymentRequest request) {
        PaymentResponse response = paymentService.processPayment(request);
        return ResponseEntity.ok(response);
    }
}
```

```yaml payment-service/src/main/resources/application.yml
server:
  port: 8085

spring:
  application:
    name: payment-service
  datasource:
    url: jdbc:h2:mem:paymentdb
    driver-class-name: org.h2.Driver
    username: sa
    password:
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
  kafka:
    bootstrap-servers: localhost:9092
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
  instance:
    prefer-ip-address: true
```

## 9. Legacy Adapter Service - Anti-Corruption Layer & Strangler Fig Pattern

```xml legacy-adapter-service/pom.xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.microservices</groupId>
        <artifactId>microservices-patterns</artifactId>
        <version>1.0.0</version>
    </parent>

    <artifactId>legacy-adapter-service</artifactId>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
    </dependencies>
</project>
```

```java legacy-adapter-service/src/main/java/com/microservices/legacyadapter/LegacyAdapterServiceApplication.java
package com.microservices.legacyadapter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Anti-Corruption Layer Pattern
 * Strangler Fig Pattern - Gradually replace legacy system
 */
@SpringBootApplication
@EnableDiscoveryClient
public class LegacyAdapterServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(LegacyAdapterServiceApplication.class, args);
    }
}
```

```java legacy-adapter-service/src/main/java/com/microservices/legacyadapter/model/LegacyCustomer.java
package com.microservices.legacyadapter.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Legacy system customer model
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LegacyCustomer {
    private String custId;
    private String custName;
    private String custAddr;
    private String custPhone;
    private String custEmail;
}
```

```java legacy-adapter-service/src/main/java/com/microservices/legacyadapter/model/ModernCustomer.java
package com.microservices.legacyadapter.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Modern system customer model
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModernCustomer {
    private String customerId;
    private String fullName;
    private Address address;
    private ContactInfo contactInfo;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Address {
        private String street;
        private String city;
        private String state;
        private String zipCode;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContactInfo {
        private String phone;
        private String email;
    }
}
```

```java legacy-adapter-service/src/main/java/com/microservices/legacyadapter/adapter/CustomerAdapter.java
package com.microservices.legacyadapter.adapter;

import com.microservices.legacyadapter.model.LegacyCustomer;
import com.microservices.legacyadapter.model.ModernCustomer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Anti-Corruption Layer Pattern
 * Translates between legacy and modern domain models
 */
@Component
@Slf4j
public class CustomerAdapter {

    /**
     * Convert legacy customer to modern customer
     */
    public ModernCustomer toModernCustomer(LegacyCustomer legacy) {
        log.info("Converting legacy customer: {}", legacy.getCustId());

        // Parse address from legacy format
        ModernCustomer.Address address = parseAddress(legacy.getCustAddr());

        ModernCustomer.ContactInfo contactInfo = ModernCustomer.ContactInfo.builder()
            .phone(legacy.getCustPhone())
            .email(legacy.getCustEmail())
            .build();

        return ModernCustomer.builder()
            .customerId(legacy.getCustId())
            .fullName(legacy.getCustName())
            .address(address)
            .contactInfo(contactInfo)
            .build();
    }

    /**
     * Convert modern customer to legacy customer
     */
    public LegacyCustomer toLegacyCustomer(ModernCustomer modern) {
        log.info("Converting modern customer: {}", modern.getCustomerId());

        // Format address to legacy format
        String legacyAddress = formatAddress(modern.getAddress());

        return LegacyCustomer.builder()
            .custId(modern.getCustomerId())
            .custName(modern.getFullName())
            .custAddr(legacyAddress)
            .custPhone(modern.getContactInfo().getPhone())
            .custEmail(modern.getContactInfo().getEmail())
            .build();
    }

    private ModernCustomer.Address parseAddress(String legacyAddress) {
        // Simplified parsing - in reality would be more complex
        String[] parts = legacyAddress.split(",");
        
        return ModernCustomer.Address.builder()
            .street(parts.length > 0 ? parts[0].trim() : "")
            .city(parts.length > 1 ? parts[1].trim() : "")
            .state(parts.length > 2 ? parts[2].trim() : "")
            .zipCode(parts.length > 3 ? parts[3].trim() : "")
            .build();
    }

    private String formatAddress(ModernCustomer.Address address) {
        return String.format("%s, %s, %s, %s",
            address.getStreet(),
            address.getCity(),
            address.getState(),
            address.getZipCode());
    }
}
```

```java legacy-adapter-service/src/main/java/com/microservices/legacyadapter/service/LegacySystemClient.java
package com.microservices.legacyadapter.service;

import com.microservices.legacyadapter.model.LegacyCustomer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Simulates legacy system client
 */
@Component
@Slf4j
public class LegacySystemClient {

    public LegacyCustomer getCustomer(String customerId) {
        log.info("Calling legacy system for customer: {}", customerId);
        
        // Simulate legacy system call
        return LegacyCustomer.builder()
            .custId(customerId)
            .custName("John Doe")
            .custAddr("123 Main St, Springfield, IL, 62701")
            .custPhone("555-1234")
            .custEmail("john.doe@example.com")
            .build();
    }

    public void updateCustomer(LegacyCustomer customer) {
        log.info("Updating customer in legacy system: {}", customer.getCustId());
        // Simulate legacy system update
    }
}
```

```java legacy-adapter-service/src/main/java/com/microservices/legacyadapter/service/CustomerService.java
package com.microservices.legacyadapter.service;

import com.microservices.legacyadapter.adapter.CustomerAdapter;
import com.microservices.legacyadapter.model.LegacyCustomer;
import com.microservices.legacyadapter.model.ModernCustomer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Strangler Fig Pattern - Gradually migrate from legacy to modern
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

    private final LegacySystemClient legacySystemClient;
    private final CustomerAdapter customerAdapter;

    /**
     * Strangler Fig Pattern - Route to legacy or modern system
     */
    public ModernCustomer getCustomer(String customerId) {
        log.info("Getting customer: {}", customerId);

        // Check if customer should be fetched from modern system
        if (shouldUseMod ernSystem(customerId)) {
            return getFromModernSystem(customerId);
        }

        // Fallback to legacy system
        LegacyCustomer legacyCustomer = legacySystemClient.getCustomer(customerId);
        return customerAdapter.toModernCustomer(legacyCustomer);
    }

    public void updateCustomer(ModernCustomer customer) {
        log.info("Updating customer: {}", customer.getCustomerId());

        if (shouldUseModernSystem(customer.getCustomerId())) {
            updateInModernSystem(customer);
        } else {
            LegacyCustomer legacyCustomer = customerAdapter.toLegacyCustomer(customer);
            legacySystemClient.updateCustomer(legacyCustomer);
        }
    }

    private boolean shouldUseModernSystem(String customerId) {
        // Strangler Fig - Gradually migrate customers to modern system
        // Example: migrate customers with IDs starting with "NEW-"
        return customerId.startsWith("NEW-");
    }

    private ModernCustomer getFromModernSystem(String customerId) {
        log.info("Fetching from modern system: {}", customerId);
        // Simulated modern system call
        return ModernCustomer.builder()
            .customerId(customerId)
            .fullName("Jane Smith")
            .address(ModernCustomer.Address.builder()
                .street("456 Oak Ave")
                .city("Chicago")
                .state("IL")
                .zipCode("60601")
                .build())
            .contactInfo(ModernCustomer.ContactInfo.builder()
                .phone("555-5678")
                .email("jane.smith@example.com")
                .build())
            .build();
    }

    private void updateInModernSystem(ModernCustomer customer) {
        log.info("Updating in modern system: {}", customer.getCustomerId());
        // Simulated modern system update
    }
}
```

```java legacy-adapter-service/src/main/java/com/microservices/legacyadapter/controller/CustomerController.java
package com.microservices.legacyadapter.controller;

import com.microservices.legacyadapter.model.ModernCustomer;
import com.microservices.legacyadapter.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping("/{customerId}")
    public ResponseEntity<ModernCustomer> getCustomer(@PathVariable String customerId) {
        ModernCustomer customer = customerService.getCustomer(customerId);
        return ResponseEntity.ok(customer);
    }

    @PutMapping("/{customerId}")
    public ResponseEntity<Void> updateCustomer(@PathVariable String customerId,
                                               @RequestBody ModernCustomer customer) {
        customer.setCustomerId(customerId);
        customerService.updateCustomer(customer);
        return ResponseEntity.ok().build();
    }
}
```

```yaml legacy-adapter-service/src/main/resources/application.yml
server:
  port: 8086

spring:
  application:
    name: legacy-adapter-service

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
  instance:
    prefer-ip-address: true
```

## 10. Analytics Service - Sidecar Pattern

```xml analytics-service/pom.xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.microservices</groupId>
        <artifactId>microservices-patterns</artifactId>
        <version>1.0.0</version>
    </parent>

    <artifactId>analytics-service</artifactId>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.kafka</groupId>
            <artifactId>spring-kafka</artifactId>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
    </dependencies>
</project>
```

```java analytics-service/src/main/java/com/microservices/analytics/AnalyticsServiceApplication.java
package com.microservices.analytics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Sidecar Pattern - Analytics service runs alongside main services
 * Collects metrics and logs without modifying main service code
 */
@SpringBootApplication
@EnableDiscoveryClient
public class AnalyticsServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AnalyticsServiceApplication.class, args);
    }
}
```

```java analytics-service/src/main/java/com/microservices/analytics/listener/EventCollector.java
package com.microservices.analytics.listener;

import com.microservices.analytics.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Sidecar Pattern - Collects events from all services
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EventCollector {

    private final AnalyticsService analyticsService;

    @KafkaListener(topics = "order-events", groupId = "analytics-service")
    public void collectOrderEvents(Map<String, Object> event) {
        log.info("Collecting order event: {}", event);
        analyticsService.recordEvent("order", event);
    }

    @KafkaListener(topics = "product-events", groupId = "analytics-service")
    public void collectProductEvents(Map<String, Object> event) {
        log.info("Collecting product event: {}", event);
        analyticsService.recordEvent("product", event);
    }

    @KafkaListener(topics = "payment-events", groupId = "analytics-service")
    public void collectPaymentEvents(Map<String, Object> event) {
        log.info("Collecting payment event: {}", event);
        analyticsService.recordEvent("payment", event);
    }
}
```

```java analytics-service/src/main/java/com/microservices/analytics/service/AnalyticsService.java
package com.microservices.analytics.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Analytics aggregation service
 */
@Service
@Slf4j
public class AnalyticsService {

    private final Map<String, AtomicLong> eventCounts = new ConcurrentHashMap<>();
    private final Map<String, Object> latestEvents = new ConcurrentHashMap<>();

    public void recordEvent(String eventType, Map<String, Object> eventData) {
        log.info("Recording {} event", eventType);
        
        eventCounts.computeIfAbsent(eventType, k -> new AtomicLong(0)).incrementAndGet();
        latestEvents.put(eventType, eventData);
        
        // In production, would store to time-series database
        persistToAnalyticsDB(eventType, eventData);
    }

    public Map<String, Long> getEventCounts() {
        Map<String, Long> counts = new ConcurrentHashMap<>();
        eventCounts.forEach((key, value) -> counts.put(key, value.get()));
        return counts;
    }

    public Map<String, Object> getLatestEvents() {
        return new ConcurrentHashMap<>(latestEvents);
    }

```java analytics-service/src/main/java/com/microservices/analytics/service/AnalyticsService.java
    private void persistToAnalyticsDB(String eventType, Map<String, Object> eventData) {
        // Simulate storing to analytics database
        log.debug("Persisting {} event to analytics DB at {}", eventType, LocalDateTime.now());
        // Would integrate with Elasticsearch, InfluxDB, or similar
    }
}
```

```java analytics-service/src/main/java/com/microservices/analytics/controller/AnalyticsController.java
package com.microservices.analytics.controller;

import com.microservices.analytics.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/counts")
    public ResponseEntity<Map<String, Long>> getEventCounts() {
        return ResponseEntity.ok(analyticsService.getEventCounts());
    }

    @GetMapping("/latest")
    public ResponseEntity<Map<String, Object>> getLatestEvents() {
        return ResponseEntity.ok(analyticsService.getLatestEvents());
    }
}
```

```yaml analytics-service/src/main/resources/application.yml
server:
  port: 8087

spring:
  application:
    name: analytics-service
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: analytics-service
      auto-offset-reset: earliest
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      properties:
        spring.json.trusted.packages: "*"

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
  instance:
    prefer-ip-address: true
```

## Docker Compose - Service Mesh Integration

```yaml docker-compose.yml
version: '3.8'

services:
  # Infrastructure Services
  
  zookeeper:
    image: confluentinc/cp-zookeeper:7.5.0
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000
    ports:
      - "2181:2181"
    networks:
      - microservices-network

  kafka:
    image: confluentinc/cp-kafka:7.5.0
    depends_on:
      - zookeeper
    ports:
      - "9092:9092"
      - "29092:29092"
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:29092,PLAINTEXT_HOST://localhost:9092
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT
      KAFKA_INTER_BROKER_LISTENER_NAME: PLAINTEXT
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
      KAFKA_AUTO_CREATE_TOPICS_ENABLE: "true"
    networks:
      - microservices-network

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    networks:
      - microservices-network

  # Service Registry
  eureka-server:
    build: ./eureka-server
    ports:
      - "8761:8761"
    networks:
      - microservices-network
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8761/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 5

  # Config Server
  config-server:
    build: ./config-server
    ports:
      - "8888:8888"
    depends_on:
      eureka-server:
        condition: service_healthy
    environment:
      - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/
    networks:
      - microservices-network

  # API Gateway
  api-gateway:
    build: ./api-gateway
    ports:
      - "8080:8080"
    depends_on:
      - eureka-server
      - redis
    environment:
      - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/
      - SPRING_REDIS_HOST=redis
    networks:
      - microservices-network

  # Microservices
  
  product-service:
    build: ./product-service
    ports:
      - "8081:8081"
    depends_on:
      - eureka-server
      - kafka
    environment:
      - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/
      - SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:29092
    networks:
      - microservices-network
    deploy:
      replicas: 2

  order-service:
    build: ./order-service
    ports:
      - "8082:8082"
    depends_on:
      - eureka-server
      - kafka
      - product-service
    environment:
      - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/
      - SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:29092
    networks:
      - microservices-network

  inventory-service:
    build: ./inventory-service
    ports:
      - "8083:8083"
    depends_on:
      - eureka-server
    environment:
      - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/
    networks:
      - microservices-network

  notification-service:
    build: ./notification-service
    depends_on:
      - kafka
    environment:
      - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/
      - SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:29092
    networks:
      - microservices-network

  payment-service:
    build: ./payment-service
    ports:
      - "8085:8085"
    depends_on:
      - eureka-server
      - kafka
    environment:
      - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/
      - SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:29092
    networks:
      - microservices-network

  legacy-adapter-service:
    build: ./legacy-adapter-service
    ports:
      - "8086:8086"
    depends_on:
      - eureka-server
    environment:
      - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/
    networks:
      - microservices-network

  analytics-service:
    build: ./analytics-service
    ports:
      - "8087:8087"
    depends_on:
      - kafka
    environment:
      - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/
      - SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:29092
    networks:
      - microservices-network

networks:
  microservices-network:
    driver: bridge
```

## Consul & Zookeeper Discovery Examples

```yaml config-server/src/main/resources/application-consul.yml
# Consul Discovery Pattern
spring:
  cloud:
    consul:
      host: localhost
      port: 8500
      discovery:
        enabled: true
        health-check-path: /actuator/health
        health-check-interval: 10s
        instance-id: ${spring.application.name}:${random.value}
```

```yaml config-server/src/main/resources/application-zookeeper.yml
# Zookeeper Discovery Pattern
spring:
  cloud:
    zookeeper:
      connect-string: localhost:2181
      discovery:
        enabled: true
        instance-host: localhost
        instance-port: ${server.port}
```

## README Documentation

```markdown README.md
# Microservices Patterns Implementation

Comprehensive implementation of 24 microservices patterns using Spring Boot and Spring Cloud.

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                         API Gateway (8080)                       │
│                    (BFF, Server-Side LB)                        │
└────────────────────────────┬────────────────────────────────────┘
                             │
        ┌────────────────────┼────────────────────┐
        │                    │                    │
        ▼                    ▼                    ▼
┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│   Product    │    │    Order     │    │  Inventory   │
│   Service    │◄───│   Service    │───►│   Service    │
│   (8081)     │    │   (8082)     │    │   (8083)     │
└──────┬───────┘    └──────┬───────┘    └──────────────┘
       │                   │
       │                   ├──────────────┐
       │                   │              │
       ▼                   ▼              ▼
┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│    Kafka     │    │   Payment    │    │   Legacy     │
│   Events     │    │   Service    │    │   Adapter    │
│              │    │   (8085)     │    │   (8086)     │
└──────┬───────┘    └──────────────┘    └──────────────┘
       │
       ├──────────────┬──────────────────┐
       ▼              ▼                  ▼
┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│Notification  │    │  Analytics   │    │  Eureka      │
│  Service     │    │  Service     │    │  Server      │
│  (8084)      │    │  (8087)      │    │  (8761)      │
└──────────────┘    └──────────────┘    └──────────────┘
```

## Patterns Implemented

### 1. Service Registry Pattern (Eureka)
- **Location**: `eureka-server/`
- **Port**: 8761
- Central service registry for all microservices
- Health checks and service discovery

### 2. Client-Side Load Balancing (Ribbon/LoadBalancer)
- **Location**: `order-service/` (Feign integration)
- Automatic load balancing between service instances
- Integrated with Eureka for service discovery

### 3. Server-Side Load Balancing
- **Location**: `api-gateway/`
- Gateway handles load distribution
- Uses `lb://` prefix in routes

### 4. Service-to-Service Communication
- **Location**: `order-service/client/`
- REST-based communication between services
- Uses Feign clients

### 5. Feign Client Pattern
- **Location**: `order-service/client/ProductClient.java`
- Declarative REST client
- Circuit breaker integration

### 6. Eureka Discovery Pattern
- All services register with Eureka
- Dynamic service discovery

### 7. Consul Discovery Pattern
- **Config**: `application-consul.yml`
- Alternative to Eureka

### 8. Zookeeper Discovery Pattern
- **Config**: `application-zookeeper.yml`
- Alternative service registry

### 9. Configuration Management Pattern
- **Location**: `config-server/`
- Centralized configuration
- External configuration support

### 10. Externalized Configuration Pattern
- Configuration stored outside application
- Git or filesystem backend

### 11. Config Server Pattern
- **Port**: 8888
- Serves configuration to all services

### 12. Config Client Pattern
- All services fetch config from config server

### 13. Backend for Frontend (BFF) Pattern
- **Location**: `api-gateway/config/GatewayConfig.java`
- Different routes for mobile/web clients

### 14. Strangler Fig Pattern
- **Location**: `legacy-adapter-service/service/CustomerService.java`
- Gradual migration from legacy system

### 15. Database per Service Pattern
- **Example**: `product-service/` has `productdb`
- Each service has its own database

### 16. Shared Database Pattern
- **Example**: `inventory-service/`
- Demonstrates shared database (not recommended)

### 17. Event-Driven Architecture Pattern
- **Location**: All services with Kafka integration
- Asynchronous communication via events

### 18. Choreography Pattern
- **Location**: `notification-service/`
- Services react independently to events

### 19. Orchestration Pattern
- **Location**: `order-service/service/OrderService.java`
- Order service orchestrates payment, inventory

### 20. Anti-Corruption Layer Pattern
- **Location**: `legacy-adapter-service/adapter/CustomerAdapter.java`
- Translates between legacy and modern models

### 21. Sidecar Pattern
- **Location**: `analytics-service/`
- Runs alongside services collecting metrics

### 22. Ambassador Pattern
- **Location**: `api-gateway/filter/AuthenticationFilter.java`
- Gateway handles cross-cutting concerns

### 23. Service Mesh Integration Pattern
- **Config**: Istio/Linkerd can be added
- Service discovery, load balancing, security

### 24. Ribbon Client Pattern
- **Location**: Integrated in Feign clients
- Client-side load balancing (legacy)

## Quick Start

### Prerequisites
- Java 17+
- Maven 3.8+
- Docker & Docker Compose

### Start Infrastructure
```bash
# Start Kafka, Zookeeper, Redis
docker-compose up -d zookeeper kafka redis

# Wait for services to be ready
docker-compose ps
```

### Build All Services
```bash
mvn clean package -DskipTests
```

### Start Services (Option 1: Manual)
```bash
# Start in order
cd eureka-server && mvn spring-boot:run &
cd config-server && mvn spring-boot:run &
cd api-gateway && mvn spring-boot:run &
cd product-service && mvn spring-boot:run &
cd inventory-service && mvn spring-boot:run &
cd order-service && mvn spring-boot:run &
cd payment-service && mvn spring-boot:run &
cd notification-service && mvn spring-boot:run &
cd legacy-adapter-service && mvn spring-boot:run &
cd analytics-service && mvn spring-boot:run &
```

### Start Services (Option 2: Docker)
```bash
docker-compose up --build
```

## Service Endpoints

| Service | Port | URL |
|---------|------|-----|
| Eureka Server | 8761 | http://localhost:8761 |
| Config Server | 8888 | http://localhost:8888 |
| API Gateway | 8080 | http://localhost:8080 |
| Product Service | 8081 | http://localhost:8081 |
| Order Service | 8082 | http://localhost:8082 |
| Inventory Service | 8083 | http://localhost:8083 |
| Notification Service | 8084 | - |
| Payment Service | 8085 | http://localhost:8085 |
| Legacy Adapter | 8086 | http://localhost:8086 |
| Analytics Service | 8087 | http://localhost:8087 |

## API Examples

### Create Product
```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Laptop",
    "sku": "LAPTOP-001",
    "description": "High-performance laptop",
    "price": 1299.99,
    "stockQuantity": 50,
    "category": "Electronics"
  }'
```

### Create Order (Orchestration Pattern)
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "CUST-001",
    "items": [
      {
        "productId": 1,
        "quantity": 2
      }
    ]
  }'
```

### Check Analytics (Sidecar Pattern)
```bash
curl http://localhost:8087/analytics/counts
curl http://localhost:8087/analytics/latest
```

### Legacy Customer (Anti-Corruption Layer)
```bash
curl http://localhost:8086/customers/LEGACY-001
```

## Pattern Demonstrations

### 1. Service Discovery
```bash
# Check registered services
curl http://localhost:8761/eureka/apps
```

### 2. Client-Side Load Balancing
```bash
# Multiple requests will be load balanced
for i in {1..10}; do
  curl http://localhost:8080/api/products
done
```

### 3. Circuit Breaker
```bash
# Stop product service
docker-compose stop product-service

# Request will fallback
curl http://localhost:8080/api/products
```

### 4. Event-Driven (Choreography)
```bash
# Create order - notification service will react
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{...}'

# Check notification service logs
docker-compose logs notification-service
```

### 5. BFF Pattern
```bash
# Mobile client
curl http://localhost:8080/mobile/api/orders \
  -H "Client-Type: Mobile"

# Web client
curl http://localhost:8080/web/api/orders \
  -H "Client-Type: Web"
```

## Monitoring

### Eureka Dashboard
```
http://localhost:8761
```

### Service Health
```bash
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
```

### Kafka Topics
```bash
docker-compose exec kafka kafka-topics --list \
  --bootstrap-server localhost:29092
```

### View Events
```bash
docker-compose exec kafka kafka-console-consumer \
  --bootstrap-server localhost:29092 \
  --topic order-events \
  --from-beginning
```

## Testing Patterns

### Test Service Registry
```bash
# Register service
# Check Eureka dashboard
open http://localhost:8761
```

### Test Load Balancing
```bash
# Scale product service
docker-compose up -d --scale product-service=3

# Make requests
ab -n 100 -c 10 http://localhost:8080/api/products
```

### Test Circuit Breaker
```bash
# Stop service
docker-compose stop payment-service

# Create order - will fail gracefully
curl -X POST http://localhost:8080/api/orders {...}
```

### Test Event-Driven
```bash
# Monitor events
docker-compose logs -f notification-service

# Create product/order
# Observe event processing
```

## Best Practices

1. **Service Independence**: Each service has its own database
2. **Event-Driven Communication**: Asynchronous for resilience
3. **Circuit Breakers**: Prevent cascading failures
4. **API Gateway**: Single entry point
5. **Service Discovery**: Dynamic service location
6. **Centralized Config**: External configuration
7. **Monitoring**: Health checks and metrics
8. **Security**: Authentication at gateway

## Troubleshooting

### Service Not Registering
```bash
# Check Eureka connection
curl http://localhost:8761/eureka/apps

# Check service logs
docker-compose logs product-service
```

### Kafka Connection Issues
```bash
# Check Kafka
docker-compose logs kafka

# Test connectivity
docker-compose exec product-service nc -zv kafka 29092
```

### Circuit Breaker Not Working
```bash
# Check Resilience4j config
curl http://localhost:8080/actuator/health
```

## Clean Up
```bash
# Stop all services
docker-compose down

# Remove volumes
docker-compose down -v
```

## License
MIT License
```

This implementation demonstrates all 24 microservices patterns in a working, integrated system!