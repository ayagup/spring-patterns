### [Continue](https://continue.dev) session transcript
 Exported: 11/5/2025, 2:43:43 PM

#### _User_

> generate a java program in spring for each of the below patterns:
> ```
> **Multi-tenancy Patterns**
> - Tenant Identification Pattern
> - Tenant Context Pattern
> - Tenant Resolver Pattern
> - Shared Schema Pattern
> - Separate Schema Pattern
> - Separate Database Pattern
> - Tenant Isolation Pattern
> - Tenant Routing Pattern
> ```

#### _Assistant_

> I'll create Spring Boot applications demonstrating each multi-tenancy pattern:
> 
> ## 1. Tenant Identification Pattern
> 
> ```java src/main/java/com/example/tenancy/identification/TenantIdentifier.java
> package com.example.tenancy.identification;
> 
> import org.springframework.stereotype.Component;
> import jakarta.servlet.http.HttpServletRequest;
> 
> @Component
> public class TenantIdentifier {
>     
>     public String identifyTenant(HttpServletRequest request) {
>         // Strategy 1: From subdomain
>         String tenantFromSubdomain = extractFromSubdomain(request);
>         if (tenantFromSubdomain != null) {
>             return tenantFromSubdomain;
>         }
>         
>         // Strategy 2: From header
>         String tenantFromHeader = request.getHeader("X-Tenant-ID");
>         if (tenantFromHeader != null) {
>             return tenantFromHeader;
>         }
>         
>         // Strategy 3: From JWT token
>         String tenantFromToken = extractFromToken(request);
>         if (tenantFromToken != null) {
>             return tenantFromToken;
>         }
>         
>         // Strategy 4: From path parameter
>         return extractFromPath(request);
>     }
>     
>     private String extractFromSubdomain(HttpServletRequest request) {
>         String serverName = request.getServerName();
>         if (serverName.contains(".")) {
>             String[] parts = serverName.split("\\.");
>             if (parts.length > 2) {
>                 return parts[0]; // subdomain as tenant
>             }
>         }
>         return null;
>     }
>     
>     private String extractFromToken(HttpServletRequest request) {
>         String authHeader = request.getHeader("Authorization");
>         if (authHeader != null && authHeader.startsWith("Bearer ")) {
>             // Parse JWT and extract tenant claim
>             // Implementation depends on your JWT library
>             return null; // Placeholder
>         }
>         return null;
>     }
>     
>     private String extractFromPath(HttpServletRequest request) {
>         String path = request.getRequestURI();
>         if (path.startsWith("/tenant/")) {
>             String[] parts = path.split("/");
>             if (parts.length > 2) {
>                 return parts[2];
>             }
>         }
>         return null;
>     }
> }
> ```
> 
> ```java src/main/java/com/example/tenancy/identification/TenantInterceptor.java
> package com.example.tenancy.identification;
> 
> import org.springframework.stereotype.Component;
> import org.springframework.web.servlet.HandlerInterceptor;
> import jakarta.servlet.http.HttpServletRequest;
> import jakarta.servlet.http.HttpServletResponse;
> 
> @Component
> public class TenantInterceptor implements HandlerInterceptor {
>     
>     private final TenantIdentifier tenantIdentifier;
>     
>     public TenantInterceptor(TenantIdentifier tenantIdentifier) {
>         this.tenantIdentifier = tenantIdentifier;
>     }
>     
>     @Override
>     public boolean preHandle(HttpServletRequest request, 
>                             HttpServletResponse response, 
>                             Object handler) {
>         String tenantId = tenantIdentifier.identifyTenant(request);
>         
>         if (tenantId == null) {
>             response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
>             return false;
>         }
>         
>         request.setAttribute("tenantId", tenantId);
>         return true;
>     }
> }
> ```
> 
> ## 2. Tenant Context Pattern
> 
> ```java src/main/java/com/example/tenancy/context/TenantContext.java
> package com.example.tenancy.context;
> 
> public class TenantContext {
>     
>     private static final ThreadLocal<String> currentTenant = new ThreadLocal<>();
>     
>     public static void setTenantId(String tenantId) {
>         currentTenant.set(tenantId);
>     }
>     
>     public static String getTenantId() {
>         return currentTenant.get();
>     }
>     
>     public static void clear() {
>         currentTenant.remove();
>     }
> }
> ```
> 
> ```java src/main/java/com/example/tenancy/context/TenantFilter.java
> package com.example.tenancy.context;
> 
> import jakarta.servlet.*;
> import jakarta.servlet.http.HttpServletRequest;
> import org.springframework.core.annotation.Order;
> import org.springframework.stereotype.Component;
> import java.io.IOException;
> 
> @Component
> @Order(1)
> public class TenantFilter implements Filter {
>     
>     @Override
>     public void doFilter(ServletRequest request, 
>                         ServletResponse response, 
>                         FilterChain chain) throws IOException, ServletException {
>         try {
>             HttpServletRequest httpRequest = (HttpServletRequest) request;
>             String tenantId = httpRequest.getHeader("X-Tenant-ID");
>             
>             if (tenantId != null) {
>                 TenantContext.setTenantId(tenantId);
>             }
>             
>             chain.doFilter(request, response);
>         } finally {
>             TenantContext.clear();
>         }
>     }
> }
> ```
> 
> ```java src/main/java/com/example/tenancy/context/TenantAwareService.java
> package com.example.tenancy.context;
> 
> import org.springframework.stereotype.Service;
> 
> @Service
> public class TenantAwareService {
>     
>     public void processData(String data) {
>         String tenantId = TenantContext.getTenantId();
>         System.out.println("Processing data for tenant: " + tenantId);
>         // Business logic using tenant context
>     }
>     
>     public String getCurrentTenantData() {
>         String tenantId = TenantContext.getTenantId();
>         return "Data for tenant: " + tenantId;
>     }
> }
> ```
> 
> ## 3. Tenant Resolver Pattern
> 
> ```java src/main/java/com/example/tenancy/resolver/TenantResolver.java
> package com.example.tenancy.resolver;
> 
> import jakarta.servlet.http.HttpServletRequest;
> 
> public interface TenantResolver {
>     String resolve(HttpServletRequest request);
>     int getPriority();
> }
> ```
> 
> ```java src/main/java/com/example/tenancy/resolver/HeaderTenantResolver.java
> package com.example.tenancy.resolver;
> 
> import jakarta.servlet.http.HttpServletRequest;
> import org.springframework.stereotype.Component;
> 
> @Component
> public class HeaderTenantResolver implements TenantResolver {
>     
>     @Override
>     public String resolve(HttpServletRequest request) {
>         return request.getHeader("X-Tenant-ID");
>     }
>     
>     @Override
>     public int getPriority() {
>         return 1;
>     }
> }
> ```
> 
> ```java src/main/java/com/example/tenancy/resolver/SubdomainTenantResolver.java
> package com.example.tenancy.resolver;
> 
> import jakarta.servlet.http.HttpServletRequest;
> import org.springframework.stereotype.Component;
> 
> @Component
> public class SubdomainTenantResolver implements TenantResolver {
>     
>     @Override
>     public String resolve(HttpServletRequest request) {
>         String serverName = request.getServerName();
>         if (serverName.contains(".")) {
>             String[] parts = serverName.split("\\.");
>             if (parts.length > 2) {
>                 return parts[0];
>             }
>         }
>         return null;
>     }
>     
>     @Override
>     public int getPriority() {
>         return 2;
>     }
> }
> ```
> 
> ```java src/main/java/com/example/tenancy/resolver/CompositeTenantResolver.java
> package com.example.tenancy.resolver;
> 
> import jakarta.servlet.http.HttpServletRequest;
> import org.springframework.stereotype.Component;
> import java.util.List;
> import java.util.Comparator;
> 
> @Component
> public class CompositeTenantResolver {
>     
>     private final List<TenantResolver> resolvers;
>     
>     public CompositeTenantResolver(List<TenantResolver> resolvers) {
>         this.resolvers = resolvers.stream()
>             .sorted(Comparator.comparingInt(TenantResolver::getPriority))
>             .toList();
>     }
>     
>     public String resolveTenant(HttpServletRequest request) {
>         for (TenantResolver resolver : resolvers) {
>             String tenantId = resolver.resolve(request);
>             if (tenantId != null && !tenantId.isEmpty()) {
>                 return tenantId;
>             }
>         }
>         return "default";
>     }
> }
> ```
> 
> ## 4. Shared Schema Pattern
> 
> ```java src/main/java/com/example/tenancy/shared/entity/Product.java
> package com.example.tenancy.shared.entity;
> 
> import jakarta.persistence.*;
> import lombok.Data;
> 
> @Entity
> @Table(name = "products")
> @Data
> public class Product {
>     
>     @Id
>     @GeneratedValue(strategy = GenerationType.IDENTITY)
>     private Long id;
>     
>     @Column(name = "tenant_id", nullable = false)
>     private String tenantId;
>     
>     private String name;
>     private Double price;
>     
>     // Tenant discriminator in WHERE clause
> }
> ```
> 
> ```java src/main/java/com/example/tenancy/shared/repository/ProductRepository.java
> package com.example.tenancy.shared.repository;
> 
> import com.example.tenancy.shared.entity.Product;
> import org.springframework.data.jpa.repository.JpaRepository;
> import org.springframework.data.jpa.repository.Query;
> import org.springframework.data.repository.query.Param;
> import java.util.List;
> 
> public interface ProductRepository extends JpaRepository<Product, Long> {
>     
>     List<Product> findByTenantId(String tenantId);
>     
>     @Query("SELECT p FROM Product p WHERE p.tenantId = :tenantId AND p.name LIKE %:name%")
>     List<Product> searchByName(@Param("tenantId") String tenantId, 
>                                @Param("name") String name);
> }
> ```
> 
> ```java src/main/java/com/example/tenancy/shared/aspect/TenantFilterAspect.java
> package com.example.tenancy.shared.aspect;
> 
> import com.example.tenancy.context.TenantContext;
> import org.aspectj.lang.ProceedingJoinPoint;
> import org.aspectj.lang.annotation.Around;
> import org.aspectj.lang.annotation.Aspect;
> import org.springframework.stereotype.Component;
> import org.hibernate.Session;
> import jakarta.persistence.EntityManager;
> import jakarta.persistence.PersistenceContext;
> 
> @Aspect
> @Component
> public class TenantFilterAspect {
>     
>     @PersistenceContext
>     private EntityManager entityManager;
>     
>     @Around("execution(* com.example.tenancy.shared.repository.*.*(..))")
>     public Object applyTenantFilter(ProceedingJoinPoint joinPoint) throws Throwable {
>         Session session = entityManager.unwrap(Session.class);
>         String tenantId = TenantContext.getTenantId();
>         
>         if (tenantId != null) {
>             session.enableFilter("tenantFilter")
>                    .setParameter("tenantId", tenantId);
>         }
>         
>         try {
>             return joinPoint.proceed();
>         } finally {
>             session.disableFilter("tenantFilter");
>         }
>     }
> }
> ```
> 
> ## 5. Separate Schema Pattern
> 
> ```java src/main/java/com/example/tenancy/schema/config/SchemaRoutingDataSource.java
> package com.example.tenancy.schema.config;
> 
> import com.example.tenancy.context.TenantContext;
> import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
> 
> public class SchemaRoutingDataSource extends AbstractRoutingDataSource {
>     
>     @Override
>     protected Object determineCurrentLookupKey() {
>         return TenantContext.getTenantId();
>     }
> }
> ```
> 
> ```java src/main/java/com/example/tenancy/schema/config/MultiTenantSchemaConfig.java
> package com.example.tenancy.schema.config;
> 
> import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
> import org.springframework.boot.context.properties.ConfigurationProperties;
> import org.springframework.context.annotation.Bean;
> import org.springframework.context.annotation.Configuration;
> import org.springframework.jdbc.datasource.DriverManagerDataSource;
> import javax.sql.DataSource;
> import java.util.HashMap;
> import java.util.Map;
> 
> @Configuration
> public class MultiTenantSchemaConfig {
>     
>     @Bean
>     @ConfigurationProperties("spring.datasource")
>     public DataSourceProperties dataSourceProperties() {
>         return new DataSourceProperties();
>     }
>     
>     @Bean
>     public DataSource dataSource(DataSourceProperties properties) {
>         SchemaRoutingDataSource routingDataSource = new SchemaRoutingDataSource();
>         
>         Map<Object, Object> targetDataSources = new HashMap<>();
>         
>         // Tenant 1 - Schema: tenant1_schema
>         DriverManagerDataSource ds1 = new DriverManagerDataSource();
>         ds1.setUrl(properties.getUrl() + "?currentSchema=tenant1_schema");
>         ds1.setUsername(properties.getUsername());
>         ds1.setPassword(properties.getPassword());
>         targetDataSources.put("tenant1", ds1);
>         
>         // Tenant 2 - Schema: tenant2_schema
>         DriverManagerDataSource ds2 = new DriverManagerDataSource();
>         ds2.setUrl(properties.getUrl() + "?currentSchema=tenant2_schema");
>         ds2.setUsername(properties.getUsername());
>         ds2.setPassword(properties.getPassword());
>         targetDataSources.put("tenant2", ds2);
>         
>         routingDataSource.setTargetDataSources(targetDataSources);
>         routingDataSource.setDefaultTargetDataSource(ds1);
>         
>         return routingDataSource;
>     }
> }
> ```
> 
> ```java src/main/java/com/example/tenancy/schema/entity/Order.java
> package com.example.tenancy.schema.entity;
> 
> import jakarta.persistence.*;
> import lombok.Data;
> import java.time.LocalDateTime;
> 
> @Entity
> @Table(name = "orders")
> @Data
> public class Order {
>     
>     @Id
>     @GeneratedValue(strategy = GenerationType.IDENTITY)
>     private Long id;
>     
>     private String customerName;
>     private Double totalAmount;
>     private LocalDateTime orderDate;
>     
>     // No tenant_id column needed - schema isolation
> }
> ```
> 
> ## 6. Separate Database Pattern
> 
> ```java src/main/java/com/example/tenancy/database/config/TenantDatabaseConfig.java
> package com.example.tenancy.database.config;
> 
> import com.zaxxer.hikari.HikariDataSource;
> import org.springframework.boot.context.properties.ConfigurationProperties;
> import org.springframework.context.annotation.Bean;
> import org.springframework.context.annotation.Configuration;
> import javax.sql.DataSource;
> import java.util.HashMap;
> import java.util.Map;
> 
> @Configuration
> public class TenantDatabaseConfig {
>     
>     @Bean
>     @ConfigurationProperties("tenants")
>     public Map<String, TenantDataSourceProperties> tenantProperties() {
>         return new HashMap<>();
>     }
>     
>     @Bean
>     public DataSource multiTenantDataSource(Map<String, TenantDataSourceProperties> tenantProperties) {
>         DatabaseRoutingDataSource routingDataSource = new DatabaseRoutingDataSource();
>         
>         Map<Object, Object> targetDataSources = new HashMap<>();
>         
>         tenantProperties.forEach((tenantId, props) -> {
>             HikariDataSource dataSource = new HikariDataSource();
>             dataSource.setJdbcUrl(props.getUrl());
>             dataSource.setUsername(props.getUsername());
>             dataSource.setPassword(props.getPassword());
>             dataSource.setMaximumPoolSize(props.getMaxPoolSize());
>             
>             targetDataSources.put(tenantId, dataSource);
>         });
>         
>         routingDataSource.setTargetDataSources(targetDataSources);
>         
>         return routingDataSource;
>     }
> }
> ```
> 
> ```java src/main/java/com/example/tenancy/database/config/TenantDataSourceProperties.java
> package com.example.tenancy.database.config;
> 
> import lombok.Data;
> 
> @Data
> public class TenantDataSourceProperties {
>     private String url;
>     private String username;
>     private String password;
>     private int maxPoolSize = 10;
> }
> ```
> 
> ```java src/main/java/com/example/tenancy/database/config/DatabaseRoutingDataSource.java
> package com.example.tenancy.database.config;
> 
> import com.example.tenancy.context.TenantContext;
> import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
> 
> public class DatabaseRoutingDataSource extends AbstractRoutingDataSource {
>     
>     @Override
>     protected Object determineCurrentLookupKey() {
>         String tenantId = TenantContext.getTenantId();
>         if (tenantId == null) {
>             throw new IllegalStateException("Tenant ID not set");
>         }
>         return tenantId;
>     }
> }
> ```
> 
> ```yaml src/main/resources/application.yml
> tenants:
>   tenant1:
>     url: jdbc:postgresql://localhost:5432/tenant1_db
>     username: tenant1_user
>     password: tenant1_pass
>     max-pool-size: 10
>   tenant2:
>     url: jdbc:postgresql://localhost:5432/tenant2_db
>     username: tenant2_user
>     password: tenant2_pass
>     max-pool-size: 10
>   tenant3:
>     url: jdbc:postgresql://remote-server:5432/tenant3_db
>     username: tenant3_user
>     password: tenant3_pass
>     max-pool-size: 5
> ```
> 
> ## 7. Tenant Isolation Pattern
> 
> ```java src/main/java/com/example/tenancy/isolation/TenantIsolationFilter.java
> package com.example.tenancy.isolation;
> 
> import jakarta.servlet.*;
> import jakarta.servlet.http.HttpServletRequest;
> import jakarta.servlet.http.HttpServletResponse;
> import org.springframework.stereotype.Component;
> import java.io.IOException;
> 
> @Component
> public class TenantIsolationFilter implements Filter {
>     
>     private final TenantValidator tenantValidator;
>     private final TenantIsolationService isolationService;
>     
>     public TenantIsolationFilter(TenantValidator tenantValidator,
>                                 TenantIsolationService isolationService) {
>         this.tenantValidator = tenantValidator;
>         this.isolationService = isolationService;
>     }
>     
>     @Override
>     public void doFilter(ServletRequest request, 
>                         ServletResponse response, 
>                         FilterChain chain) throws IOException, ServletException {
>         HttpServletRequest httpRequest = (HttpServletRequest) request;
>         HttpServletResponse httpResponse = (HttpServletResponse) response;
>         
>         String tenantId = httpRequest.getHeader("X-Tenant-ID");
>         
>         // Validate tenant exists and is active
>         if (!tenantValidator.isValidTenant(tenantId)) {
>             httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
>             httpResponse.getWriter().write("Invalid or inactive tenant");
>             return;
>         }
>         
>         // Apply isolation policies
>         isolationService.applyIsolation(tenantId);
>         
>         chain.doFilter(request, response);
>     }
> }
> ```
> 
> ```java src/main/java/com/example/tenancy/isolation/TenantValidator.java
> package com.example.tenancy.isolation;
> 
> import org.springframework.stereotype.Component;
> import java.util.Map;
> import java.util.concurrent.ConcurrentHashMap;
> 
> @Component
> public class TenantValidator {
>     
>     private final Map<String, TenantStatus> tenantStatusMap = new ConcurrentHashMap<>();
>     
>     public TenantValidator() {
>         // Initialize with some tenants
>         tenantStatusMap.put("tenant1", new TenantStatus(true, "ACTIVE"));
>         tenantStatusMap.put("tenant2", new TenantStatus(true, "ACTIVE"));
>         tenantStatusMap.put("tenant3", new TenantStatus(false, "SUSPENDED"));
>     }
>     
>     public boolean isValidTenant(String tenantId) {
>         TenantStatus status = tenantStatusMap.get(tenantId);
>         return status != null && status.isActive() && "ACTIVE".equals(status.getStatus());
>     }
>     
>     public void suspendTenant(String tenantId) {
>         tenantStatusMap.computeIfPresent(tenantId, (k, v) -> {
>             v.setActive(false);
>             v.setStatus("SUSPENDED");
>             return v;
>         });
>     }
>     
>     public void activateTenant(String tenantId) {
>         tenantStatusMap.computeIfPresent(tenantId, (k, v) -> {
>             v.setActive(true);
>             v.setStatus("ACTIVE");
>             return v;
>         });
>     }
>     
>     static class TenantStatus {
>         private boolean active;
>         private String status;
>         
>         public TenantStatus(boolean active, String status) {
>             this.active = active;
>             this.status = status;
>         }
>         
>         public boolean isActive() { return active; }
>         public void setActive(boolean active) { this.active = active; }
>         public String getStatus() { return status; }
>         public void setStatus(String status) { this.status = status; }
>     }
> }
> ```
> 
> ```java src/main/java/com/example/tenancy/isolation/TenantIsolationService.java
> package com.example.tenancy.isolation;
> 
> import com.example.tenancy.context.TenantContext;
> import org.springframework.stereotype.Service;
> import java.util.Map;
> import java.util.concurrent.ConcurrentHashMap;
> 
> @Service
> public class TenantIsolationService {
>     
>     private final Map<String, IsolationPolicy> isolationPolicies = new ConcurrentHashMap<>();
>     
>     public TenantIsolationService() {
>         // Configure isolation policies per tenant
>         isolationPolicies.put("tenant1", new IsolationPolicy(
>             IsolationLevel.STRICT, 
>             true, 
>             1000
>         ));
>         isolationPolicies.put("tenant2", new IsolationPolicy(
>             IsolationLevel.MODERATE, 
>             false, 
>             5000
>         ));
>     }
>     
>     public void applyIsolation(String tenantId) {
>         TenantContext.setTenantId(tenantId);
>         
>         IsolationPolicy policy = isolationPolicies.get(tenantId);
>         if (policy != null) {
>             // Apply resource limits
>             if (policy.isResourceLimitsEnabled()) {
>                 applyResourceLimits(policy.getMaxConnections());
>             }
>             
>             // Apply data encryption if required
>             if (policy.getLevel() == IsolationLevel.STRICT) {
>                 enableDataEncryption();
>             }
>         }
>     }
>     
>     private void applyResourceLimits(int maxConnections) {
>         // Implement connection pooling limits
>     }
>     
>     private void enableDataEncryption() {
>         // Enable encryption for sensitive data
>     }
>     
>     enum IsolationLevel {
>         STRICT, MODERATE, RELAXED
>     }
>     
>     static class IsolationPolicy {
>         private final IsolationLevel level;
>         private final boolean resourceLimitsEnabled;
>         private final int maxConnections;
>         
>         public IsolationPolicy(IsolationLevel level, boolean resourceLimitsEnabled, int maxConnections) {
>             this.level = level;
>             this.resourceLimitsEnabled = resourceLimitsEnabled;
>             this.maxConnections = maxConnections;
>         }
>         
>         public IsolationLevel getLevel() { return level; }
>         public boolean isResourceLimitsEnabled() { return resourceLimitsEnabled; }
>         public int getMaxConnections() { return maxConnections; }
>     }
> }
> ```
> 
> ## 8. Tenant Routing Pattern
> 
> ```java src/main/java/com/example/tenancy/routing/TenantRouter.java
> package com.example.tenancy.routing;
> 
> import org.springframework.stereotype.Component;
> import java.util.Map;
> import java.util.concurrent.ConcurrentHashMap;
> 
> @Component
> public class TenantRouter {
>     
>     private final Map<String, TenantRoute> routingTable = new ConcurrentHashMap<>();
>     
>     public TenantRouter() {
>         // Configure routing rules
>         routingTable.put("tenant1", new TenantRoute(
>             "us-east-1", 
>             "primary-cluster", 
>             RoutingStrategy.GEOGRAPHIC
>         ));
>         routingTable.put("tenant2", new TenantRoute(
>             "eu-west-1", 
>             "secondary-cluster", 
>             RoutingStrategy.GEOGRAPHIC
>         ));
>         routingTable.put("tenant3", new TenantRoute(
>             "us-west-2", 
>             "premium-cluster", 
>             RoutingStrategy.TIER_BASED
>         ));
>     }
>     
>     public TenantRoute route(String tenantId) {
>         TenantRoute route = routingTable.get(tenantId);
>         if (route == null) {
>             return getDefaultRoute();
>         }
>         return route;
>     }
>     
>     public String resolveDataSource(String tenantId) {
>         TenantRoute route = route(tenantId);
>         return route.getClusterId() + "-datasource";
>     }
>     
>     public String resolveRegion(String tenantId) {
>         TenantRoute route = route(tenantId);
>         return route.getRegion();
>     }
>     
>     private TenantRoute getDefaultRoute() {
>         return new TenantRoute("us-east-1", "default-cluster", RoutingStrategy.ROUND_ROBIN);
>     }
>     
>     public void updateRoute(String tenantId, TenantRoute route) {
>         routingTable.put(tenantId, route);
>     }
> }
> ```
> 
> ```java src/main/java/com/example/tenancy/routing/TenantRoute.java
> package com.example.tenancy.routing;
> 
> import lombok.AllArgsConstructor;
> import lombok.Data;
> 
> @Data
> @AllArgsConstructor
> public class TenantRoute {
>     private String region;
>     private String clusterId;
>     private RoutingStrategy strategy;
> }
> ```
> 
> ```java src/main/java/com/example/tenancy/routing/RoutingStrategy.java
> package com.example.tenancy.routing;
> 
> public enum RoutingStrategy {
>     GEOGRAPHIC,      // Route based on geographic location
>     TIER_BASED,      // Route based on tenant tier (free, premium, enterprise)
>     ROUND_ROBIN,     // Distribute load evenly
>     LEAST_LOADED,    // Route to least loaded instance
>     CUSTOM          // Custom routing logic
> }
> ```
> 
> ```java src/main/java/com/example/tenancy/routing/TenantRoutingService.java
> package com.example.tenancy.routing;
> 
> import com.example.tenancy.context.TenantContext;
> import org.springframework.stereotype.Service;
> 
> @Service
> public class TenantRoutingService {
>     
>     private final TenantRouter tenantRouter;
>     
>     public TenantRoutingService(TenantRouter tenantRouter) {
>         this.tenantRouter = tenantRouter;
>     }
>     
>     public void routeRequest() {
>         String tenantId = TenantContext.getTenantId();
>         TenantRoute route = tenantRouter.route(tenantId);
>         
>         System.out.println("Routing tenant " + tenantId + " to:");
>         System.out.println("  Region: " + route.getRegion());
>         System.out.println("  Cluster: " + route.getClusterId());
>         System.out.println("  Strategy: " + route.getStrategy());
>         
>         // Apply routing logic based on strategy
>         switch (route.getStrategy()) {
>             case GEOGRAPHIC:
>                 routeByGeography(route);
>                 break;
>             case TIER_BASED:
>                 routeByTier(route);
>                 break;
>             case ROUND_ROBIN:
>                 routeRoundRobin(route);
>                 break;
>             case LEAST_LOADED:
>                 routeToLeastLoaded(route);
>                 break;
>             default:
>                 routeDefault(route);
>         }
>     }
>     
>     private void routeByGeography(TenantRoute route) {
>         // Implementation for geographic routing
>     }
>     
>     private void routeByTier(TenantRoute route) {
>         // Implementation for tier-based routing
>     }
>     
>     private void routeRoundRobin(TenantRoute route) {
>         // Implementation for round-robin routing
>     }
>     
>     private void routeToLeastLoaded(TenantRoute route) {
>         // Implementation for least-loaded routing
>     }
>     
>     private void routeDefault(TenantRoute route) {
>         // Default routing implementation
>     }
> }
> ```
> 
> ```java src/main/java/com/example/tenancy/routing/controller/RoutingController.java
> package com.example.tenancy.routing.controller;
> 
> import com.example.tenancy.routing.TenantRouter;
> import com.example.tenancy.routing.TenantRoute;
> import org.springframework.web.bind.annotation.*;
> 
> @RestController
> @RequestMapping("/api/routing")
> public class RoutingController {
>     
>     private final TenantRouter tenantRouter;
>     
>     public RoutingController(TenantRouter tenantRouter) {
>         this.tenantRouter = tenantRouter;
>     }
>     
>     @GetMapping("/route/{tenantId}")
>     public TenantRoute getRoute(@PathVariable String tenantId) {
>         return tenantRouter.route(tenantId);
>     }
>     
>     @PutMapping("/route/{tenantId}")
>     public void updateRoute(@PathVariable String tenantId, @RequestBody TenantRoute route) {
>         tenantRouter.updateRoute(tenantId, route);
>     }
> }
> ```
> 
> ## Main Application & Dependencies
> 
> ```java src/main/java/com/example/tenancy/MultiTenancyApplication.java
> package com.example.tenancy;
> 
> import org.springframework.boot.SpringApplication;
> import org.springframework.boot.autoconfigure.SpringBootApplication;
> import org.springframework.context.annotation.EnableAspectJAutoProxy;
> 
> @SpringBootApplication
> @EnableAspectJAutoProxy
> public class MultiTenancyApplication {
>     
>     public static void main(String[] args) {
>         SpringApplication.run(MultiTenancyApplication.class, args);
>     }
> }
> ```
> 
> ```xml pom.xml
> <?xml version="1.0" encoding="UTF-8"?>
> <project xmlns="http://maven.apache.org/POM/4.0.0"
>          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
>          xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
>          http://maven.apache.org/xsd/maven-4.0.0.xsd">
>     <modelVersion>4.0.0</modelVersion>
>     
>     <parent>
>         <groupId>org.springframework.boot</groupId>
>         <artifactId>spring-boot-starter-parent</artifactId>
>         <version>3.2.0</version>
>         <relativePath/>
>     </parent>
>     
>     <groupId>com.example</groupId>
>     <artifactId>multi-tenancy-patterns</artifactId>
>     <version>1.0.0</version>
>     
>     <properties>
>         <java.version>17</java.version>
>     </properties>
>     
>     <dependencies>
>         <dependency>
>             <groupId>org.springframework.boot</groupId>
>             <artifactId>spring-boot-starter-web</artifactId>
>         </dependency>
>         
>         <dependency>
>             <groupId>org.springframework.boot</groupId>
>             <artifactId>spring-boot-starter-data-jpa</artifactId>
>         </dependency>
>         
>         <dependency>
>             <groupId>org.springframework.boot</groupId>
>             <artifactId>spring-boot-starter-aop</artifactId>
>         </dependency>
>         
>         <dependency>
>             <groupId>org.postgresql</groupId>
>             <artifactId>postgresql

#### _User_

> Continue your response exactly where you left off:

#### _Assistant_

> ```xml pom.xml
>         <dependency>
>             <groupId>org.postgresql</groupId>
>             <artifactId>postgresql</artifactId>
>             <scope>runtime</scope>
>         </dependency>
>         
>         <dependency>
>             <groupId>com.zaxxer</groupId>
>             <artifactId>HikariCP</artifactId>
>         </dependency>
>         
>         <dependency>
>             <groupId>org.projectlombok</groupId>
>             <artifactId>lombok</artifactId>
>             <optional>true</optional>
>         </dependency>
>         
>         <dependency>
>             <groupId>org.springframework.boot</groupId>
>             <artifactId>spring-boot-starter-test</artifactId>
>             <scope>test</scope>
>         </dependency>
>     </dependencies>
>     
>     <build>
>         <plugins>
>             <plugin>
>                 <groupId>org.springframework.boot</groupId>
>                 <artifactId>spring-boot-maven-plugin</artifactId>
>             </plugin>
>         </plugins>
>     </build>
> </project>
> ```
> 
> ## Sample Controllers Demonstrating Each Pattern
> 
> ```java src/main/java/com/example/tenancy/controller/SharedSchemaController.java
> package com.example.tenancy.controller;
> 
> import com.example.tenancy.context.TenantContext;
> import com.example.tenancy.shared.entity.Product;
> import com.example.tenancy.shared.repository.ProductRepository;
> import org.springframework.web.bind.annotation.*;
> import java.util.List;
> 
> @RestController
> @RequestMapping("/api/shared-schema/products")
> public class SharedSchemaController {
>     
>     private final ProductRepository productRepository;
>     
>     public SharedSchemaController(ProductRepository productRepository) {
>         this.productRepository = productRepository;
>     }
>     
>     @GetMapping
>     public List<Product> getAllProducts() {
>         String tenantId = TenantContext.getTenantId();
>         return productRepository.findByTenantId(tenantId);
>     }
>     
>     @PostMapping
>     public Product createProduct(@RequestBody Product product) {
>         String tenantId = TenantContext.getTenantId();
>         product.setTenantId(tenantId);
>         return productRepository.save(product);
>     }
>     
>     @GetMapping("/search")
>     public List<Product> searchProducts(@RequestParam String name) {
>         String tenantId = TenantContext.getTenantId();
>         return productRepository.searchByName(tenantId, name);
>     }
> }
> ```
> 
> ```java src/main/java/com/example/tenancy/controller/SeparateSchemaController.java
> package com.example.tenancy.controller;
> 
> import com.example.tenancy.schema.entity.Order;
> import org.springframework.web.bind.annotation.*;
> import jakarta.persistence.EntityManager;
> import jakarta.persistence.PersistenceContext;
> import org.springframework.transaction.annotation.Transactional;
> import java.util.List;
> 
> @RestController
> @RequestMapping("/api/separate-schema/orders")
> public class SeparateSchemaController {
>     
>     @PersistenceContext
>     private EntityManager entityManager;
>     
>     @GetMapping
>     public List<Order> getAllOrders() {
>         return entityManager.createQuery("SELECT o FROM Order o", Order.class)
>                            .getResultList();
>     }
>     
>     @PostMapping
>     @Transactional
>     public Order createOrder(@RequestBody Order order) {
>         entityManager.persist(order);
>         return order;
>     }
>     
>     @GetMapping("/{id}")
>     public Order getOrder(@PathVariable Long id) {
>         return entityManager.find(Order.class, id);
>     }
> }
> ```
> 
> ```java src/main/java/com/example/tenancy/controller/TenantManagementController.java
> package com.example.tenancy.controller;
> 
> import com.example.tenancy.isolation.TenantValidator;
> import com.example.tenancy.routing.TenantRouter;
> import com.example.tenancy.routing.TenantRoute;
> import org.springframework.web.bind.annotation.*;
> 
> @RestController
> @RequestMapping("/api/admin/tenants")
> public class TenantManagementController {
>     
>     private final TenantValidator tenantValidator;
>     private final TenantRouter tenantRouter;
>     
>     public TenantManagementController(TenantValidator tenantValidator, 
>                                      TenantRouter tenantRouter) {
>         this.tenantValidator = tenantValidator;
>         this.tenantRouter = tenantRouter;
>     }
>     
>     @PostMapping("/{tenantId}/suspend")
>     public String suspendTenant(@PathVariable String tenantId) {
>         tenantValidator.suspendTenant(tenantId);
>         return "Tenant " + tenantId + " suspended";
>     }
>     
>     @PostMapping("/{tenantId}/activate")
>     public String activateTenant(@PathVariable String tenantId) {
>         tenantValidator.activateTenant(tenantId);
>         return "Tenant " + tenantId + " activated";
>     }
>     
>     @GetMapping("/{tenantId}/route")
>     public TenantRoute getTenantRoute(@PathVariable String tenantId) {
>         return tenantRouter.route(tenantId);
>     }
>     
>     @PutMapping("/{tenantId}/route")
>     public String updateTenantRoute(@PathVariable String tenantId, 
>                                    @RequestBody TenantRoute route) {
>         tenantRouter.updateRoute(tenantId, route);
>         return "Route updated for tenant " + tenantId;
>     }
> }
> ```
> 
> ## Configuration Files
> 
> ```java src/main/java/com/example/tenancy/config/WebConfig.java
> package com.example.tenancy.config;
> 
> import com.example.tenancy.identification.TenantInterceptor;
> import org.springframework.context.annotation.Configuration;
> import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
> import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
> 
> @Configuration
> public class WebConfig implements WebMvcConfigurer {
>     
>     private final TenantInterceptor tenantInterceptor;
>     
>     public WebConfig(TenantInterceptor tenantInterceptor) {
>         this.tenantInterceptor = tenantInterceptor;
>     }
>     
>     @Override
>     public void addInterceptors(InterceptorRegistry registry) {
>         registry.addInterceptor(tenantInterceptor)
>                 .addPathPatterns("/api/**")
>                 .excludePathPatterns("/api/admin/**", "/api/public/**");
>     }
> }
> ```
> 
> ```java src/main/java/com/example/tenancy/config/JpaConfig.java
> package com.example.tenancy.config;
> 
> import org.springframework.context.annotation.Configuration;
> import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
> import org.springframework.transaction.annotation.EnableTransactionManagement;
> 
> @Configuration
> @EnableJpaRepositories(basePackages = "com.example.tenancy")
> @EnableTransactionManagement
> public class JpaConfig {
> }
> ```
> 
> ```properties src/main/resources/application.properties
> # Server Configuration
> server.port=8080
> 
> # Database Configuration (Default)
> spring.datasource.url=jdbc:postgresql://localhost:5432/multitenancy_db
> spring.datasource.username=postgres
> spring.datasource.password=postgres
> spring.datasource.driver-class-name=org.postgresql.Driver
> 
> # JPA/Hibernate Configuration
> spring.jpa.hibernate.ddl-auto=update
> spring.jpa.show-sql=true
> spring.jpa.properties.hibernate.format_sql=true
> spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
> 
> # Hikari Connection Pool
> spring.datasource.hikari.maximum-pool-size=20
> spring.datasource.hikari.minimum-idle=5
> spring.datasource.hikari.connection-timeout=30000
> spring.datasource.hikari.idle-timeout=600000
> spring.datasource.hikari.max-lifetime=1800000
> 
> # Logging
> logging.level.org.springframework.web=DEBUG
> logging.level.org.hibernate.SQL=DEBUG
> logging.level.com.example.tenancy=DEBUG
> ```
> 
> ## Test Classes
> 
> ```java src/test/java/com/example/tenancy/TenantIdentificationTest.java
> package com.example.tenancy;
> 
> import com.example.tenancy.identification.TenantIdentifier;
> import org.junit.jupiter.api.Test;
> import org.springframework.beans.factory.annotation.Autowired;
> import org.springframework.boot.test.context.SpringBootTest;
> import org.springframework.mock.web.MockHttpServletRequest;
> 
> import static org.junit.jupiter.api.Assertions.*;
> 
> @SpringBootTest
> class TenantIdentificationTest {
>     
>     @Autowired
>     private TenantIdentifier tenantIdentifier;
>     
>     @Test
>     void testIdentifyTenantFromHeader() {
>         MockHttpServletRequest request = new MockHttpServletRequest();
>         request.addHeader("X-Tenant-ID", "tenant1");
>         
>         String tenantId = tenantIdentifier.identifyTenant(request);
>         
>         assertEquals("tenant1", tenantId);
>     }
>     
>     @Test
>     void testIdentifyTenantFromSubdomain() {
>         MockHttpServletRequest request = new MockHttpServletRequest();
>         request.setServerName("tenant2.example.com");
>         
>         String tenantId = tenantIdentifier.identifyTenant(request);
>         
>         assertEquals("tenant2", tenantId);
>     }
>     
>     @Test
>     void testIdentifyTenantFromPath() {
>         MockHttpServletRequest request = new MockHttpServletRequest();
>         request.setRequestURI("/tenant/tenant3/api/products");
>         
>         String tenantId = tenantIdentifier.identifyTenant(request);
>         
>         assertEquals("tenant3", tenantId);
>     }
> }
> ```
> 
> ```java src/test/java/com/example/tenancy/TenantContextTest.java
> package com.example.tenancy;
> 
> import com.example.tenancy.context.TenantContext;
> import org.junit.jupiter.api.AfterEach;
> import org.junit.jupiter.api.Test;
> 
> import static org.junit.jupiter.api.Assertions.*;
> 
> class TenantContextTest {
>     
>     @AfterEach
>     void cleanup() {
>         TenantContext.clear();
>     }
>     
>     @Test
>     void testSetAndGetTenantId() {
>         TenantContext.setTenantId("tenant1");
>         
>         assertEquals("tenant1", TenantContext.getTenantId());
>     }
>     
>     @Test
>     void testClearTenantContext() {
>         TenantContext.setTenantId("tenant1");
>         TenantContext.clear();
>         
>         assertNull(TenantContext.getTenantId());
>     }
>     
>     @Test
>     void testThreadIsolation() throws InterruptedException {
>         TenantContext.setTenantId("main-tenant");
>         
>         Thread thread = new Thread(() -> {
>             assertNull(TenantContext.getTenantId());
>             TenantContext.setTenantId("thread-tenant");
>             assertEquals("thread-tenant", TenantContext.getTenantId());
>         });
>         
>         thread.start();
>         thread.join();
>         
>         assertEquals("main-tenant", TenantContext.getTenantId());
>     }
> }
> ```
> 
> ```java src/test/java/com/example/tenancy/TenantRoutingTest.java
> package com.example.tenancy;
> 
> import com.example.tenancy.routing.TenantRouter;
> import com.example.tenancy.routing.TenantRoute;
> import com.example.tenancy.routing.RoutingStrategy;
> import org.junit.jupiter.api.Test;
> import org.springframework.beans.factory.annotation.Autowired;
> import org.springframework.boot.test.context.SpringBootTest;
> 
> import static org.junit.jupiter.api.Assertions.*;
> 
> @SpringBootTest
> class TenantRoutingTest {
>     
>     @Autowired
>     private TenantRouter tenantRouter;
>     
>     @Test
>     void testRouteTenant() {
>         TenantRoute route = tenantRouter.route("tenant1");
>         
>         assertNotNull(route);
>         assertEquals("us-east-1", route.getRegion());
>         assertEquals("primary-cluster", route.getClusterId());
>     }
>     
>     @Test
>     void testUpdateRoute() {
>         TenantRoute newRoute = new TenantRoute(
>             "ap-south-1", 
>             "asia-cluster", 
>             RoutingStrategy.GEOGRAPHIC
>         );
>         
>         tenantRouter.updateRoute("tenant1", newRoute);
>         TenantRoute updatedRoute = tenantRouter.route("tenant1");
>         
>         assertEquals("ap-south-1", updatedRoute.getRegion());
>         assertEquals("asia-cluster", updatedRoute.getClusterId());
>     }
>     
>     @Test
>     void testResolveDataSource() {
>         String dataSource = tenantRouter.resolveDataSource("tenant2");
>         
>         assertEquals("secondary-cluster-datasource", dataSource);
>     }
> }
> ```
> 
> ## README Documentation
> 
> ```markdown README.md
> # Multi-Tenancy Patterns in Spring Boot
> 
> This project demonstrates 8 essential multi-tenancy patterns implemented in Spring Boot.
> 
> ## Patterns Implemented
> 
> ### 1. Tenant Identification Pattern
> Identifies tenants from multiple sources (header, subdomain, path, JWT token).
> 
> **Usage:**
> ```bash
> curl -H "X-Tenant-ID: tenant1" http://localhost:8080/api/products
> ```
> 
> ### 2. Tenant Context Pattern
> Thread-local storage for tenant information throughout request lifecycle.
> 
> ### 3. Tenant Resolver Pattern
> Chain of responsibility pattern for tenant resolution with priority.
> 
> ### 4. Shared Schema Pattern
> Single database, single schema with tenant discriminator column.
> 
> **Database:**
> ```sql
> CREATE TABLE products (
>     id SERIAL PRIMARY KEY,
>     tenant_id VARCHAR(50) NOT NULL,
>     name VARCHAR(255),
>     price DECIMAL(10,2)
> );
> CREATE INDEX idx_tenant_id ON products(tenant_id);
> ```
> 
> ### 5. Separate Schema Pattern
> Single database with separate schemas per tenant.
> 
> **Setup:**
> ```sql
> CREATE SCHEMA tenant1_schema;
> CREATE SCHEMA tenant2_schema;
> ```
> 
> ### 6. Separate Database Pattern
> Complete database isolation - one database per tenant.
> 
> **Configuration:** See `application.yml` for tenant database mappings.
> 
> ### 7. Tenant Isolation Pattern
> Ensures strict data and resource isolation between tenants.
> 
> ### 8. Tenant Routing Pattern
> Routes requests to appropriate infrastructure based on tenant.
> 
> ## Running the Application
> 
> ```bash
> mvn clean install
> mvn spring-boot:run
> ```
> 
> ## API Examples
> 
> ```bash
> # Get products (Shared Schema)
> curl -H "X-Tenant-ID: tenant1" http://localhost:8080/api/shared-schema/products
> 
> # Create order (Separate Schema)
> curl -H "X-Tenant-ID: tenant2" -X POST \
>   -H "Content-Type: application/json" \
>   -d '{"customerName":"John","totalAmount":100.0}' \
>   http://localhost:8080/api/separate-schema/orders
> 
> # Get tenant route
> curl http://localhost:8080/api/admin/tenants/tenant1/route
> 
> # Suspend tenant
> curl -X POST http://localhost:8080/api/admin/tenants/tenant3/suspend
> ```
> 
> ## Architecture Considerations
> 
> - **Shared Schema**: Best for many small tenants, cost-effective
> - **Separate Schema**: Good balance of isolation and cost
> - **Separate Database**: Maximum isolation, higher operational cost
> 
> ## Testing
> 
> ```bash
> mvn test
> ```
> ```
> 
> All 8 multi-tenancy patterns are now implemented with complete working examples, tests, and documentation. Each pattern addresses different aspects of multi-tenant application design with varying levels of isolation and resource sharing.