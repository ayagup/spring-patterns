package com.example.microservices.feignclient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.lang.annotation.*;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.time.LocalDateTime;

/**
 * Feign Client Pattern
 * 
 * This pattern demonstrates a declarative REST client approach similar to Spring Cloud OpenFeign.
 * Feign eliminates boilerplate code by allowing developers to define HTTP APIs as Java interfaces.
 * 
 * Key Components:
 * 1. FeignClient - Annotation for declaring REST clients
 * 2. FeignClientFactory - Creates proxy instances of Feign clients
 * 3. RequestInterceptor - Intercepts and modifies requests
 * 4. ErrorDecoder - Custom error handling
 * 5. Fallback - Circuit breaker fallback implementation
 * 
 * Features:
 * - Declarative HTTP client
 * - Automatic request/response serialization
 * - Load balancing integration
 * - Circuit breaker support
 * - Request/Response interceptors
 * - Custom error handling
 * 
 * Use Cases:
 * - Microservice inter-communication
 * - REST API consumption
 * - Service mesh integration
 * - API gateway patterns
 */

@SpringBootApplication
public class FeignClientPattern {

    public static void main(String[] args) {
        SpringApplication.run(FeignClientPattern.class, args);
        
        // Demonstration
        System.out.println("=== Feign Client Pattern Demo ===\n");
        
        FeignClientFactory factory = new FeignClientFactory();
        
        // Register service endpoints
        factory.registerServiceUrl("user-service", "http://localhost:8081");
        factory.registerServiceUrl("order-service", "http://localhost:8082");
        factory.registerServiceUrl("product-service", "http://localhost:8083");
        
        // Create Feign client proxies
        UserServiceClient userClient = factory.create(UserServiceClient.class);
        OrderServiceClient orderClient = factory.create(OrderServiceClient.class);
        ProductServiceClient productClient = factory.create(ProductServiceClient.class);
        
        // Use the clients
        System.out.println("1. Fetching User:");
        try {
            User user = userClient.getUserById("user123");
            System.out.println("User: " + user.getName() + " (" + user.getEmail() + ")");
        } catch (Exception e) {
            System.err.println("Error fetching user: " + e.getMessage());
        }
        
        System.out.println("\n2. Creating Order:");
        try {
            OrderRequest request = new OrderRequest("user123", "prod456", 2);
            Order order = orderClient.createOrder(request);
            System.out.println("Order created: " + order.getOrderId() + ", Status: " + order.getStatus());
        } catch (Exception e) {
            System.err.println("Error creating order: " + e.getMessage());
        }
        
        System.out.println("\n3. Fetching Products:");
        try {
            List<Product> products = productClient.getProducts();
            System.out.println("Found " + products.size() + " products:");
            products.forEach(p -> System.out.println("  - " + p.getName() + ": $" + p.getPrice()));
        } catch (Exception e) {
            System.err.println("Error fetching products: " + e.getMessage());
        }
        
        System.out.println("\n4. Testing Fallback:");
        factory.enableCircuitBreaker(true);
        try {
            // This will trigger fallback
            User user = userClient.getUserById("invalid-user");
            System.out.println("User (from fallback): " + user.getName());
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
        
        System.out.println("\n5. Using Request Interceptor:");
        factory.addInterceptor(new AuthenticationInterceptor("Bearer token-12345"));
        User authenticatedUser = userClient.getUserById("user789");
        System.out.println("Authenticated request completed for: " + authenticatedUser.getName());
    }
}

/**
 * FeignClient annotation - marks an interface as a Feign client
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@interface FeignClient {
    String name();
    String url() default "";
    Class<?> fallback() default void.class;
}

/**
 * HTTP Method annotations
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface GetMapping {
    String value();
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface PostMapping {
    String value();
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface PutMapping {
    String value();
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface DeleteMapping {
    String value();
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@interface PathVariable {
    String value() default "";
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@interface RequestBody {
}

/**
 * User Service Feign Client
 */
@FeignClient(name = "user-service", fallback = UserServiceFallback.class)
interface UserServiceClient {
    
    @GetMapping("/api/users/{id}")
    User getUserById(@PathVariable("id") String id);
    
    @GetMapping("/api/users")
    List<User> getAllUsers();
    
    @PostMapping("/api/users")
    User createUser(@RequestBody User user);
    
    @PutMapping("/api/users/{id}")
    User updateUser(@PathVariable("id") String id, @RequestBody User user);
    
    @DeleteMapping("/api/users/{id}")
    void deleteUser(@PathVariable("id") String id);
}

/**
 * Fallback implementation for User Service
 */
@Component
class UserServiceFallback implements UserServiceClient {
    
    @Override
    public User getUserById(String id) {
        return new User(id, "Fallback User", "fallback@example.com");
    }
    
    @Override
    public List<User> getAllUsers() {
        return Collections.emptyList();
    }
    
    @Override
    public User createUser(User user) {
        return user;
    }
    
    @Override
    public User updateUser(String id, User user) {
        return user;
    }
    
    @Override
    public void deleteUser(String id) {
        // No-op in fallback
    }
}

/**
 * Order Service Feign Client
 */
@FeignClient(name = "order-service")
interface OrderServiceClient {
    
    @GetMapping("/api/orders/{id}")
    Order getOrderById(@PathVariable("id") String id);
    
    @PostMapping("/api/orders")
    Order createOrder(@RequestBody OrderRequest request);
    
    @GetMapping("/api/orders/user/{userId}")
    List<Order> getOrdersByUserId(@PathVariable("userId") String userId);
}

/**
 * Product Service Feign Client
 */
@FeignClient(name = "product-service")
interface ProductServiceClient {
    
    @GetMapping("/api/products")
    List<Product> getProducts();
    
    @GetMapping("/api/products/{id}")
    Product getProductById(@PathVariable("id") String id);
    
    @PostMapping("/api/products")
    Product createProduct(@RequestBody Product product);
}

/**
 * Feign Client Factory - Creates proxy instances
 */
class FeignClientFactory {
    private final Map<String, String> serviceUrls = new ConcurrentHashMap<>();
    private final List<RequestInterceptor> interceptors = new ArrayList<>();
    private ErrorDecoder errorDecoder = new DefaultErrorDecoder();
    private boolean circuitBreakerEnabled = false;
    
    public void registerServiceUrl(String serviceName, String url) {
        serviceUrls.put(serviceName, url);
        System.out.println("Registered Feign client: " + serviceName + " -> " + url);
    }
    
    public void addInterceptor(RequestInterceptor interceptor) {
        interceptors.add(interceptor);
    }
    
    public void setErrorDecoder(ErrorDecoder errorDecoder) {
        this.errorDecoder = errorDecoder;
    }
    
    public void enableCircuitBreaker(boolean enabled) {
        this.circuitBreakerEnabled = enabled;
    }
    
    @SuppressWarnings("unchecked")
    public <T> T create(Class<T> clientInterface) {
        FeignClient annotation = clientInterface.getAnnotation(FeignClient.class);
        if (annotation == null) {
            throw new IllegalArgumentException("Interface must be annotated with @FeignClient");
        }
        
        String serviceName = annotation.name();
        String baseUrl = serviceUrls.get(serviceName);
        
        if (baseUrl == null) {
            baseUrl = annotation.url().isEmpty() ? "http://localhost:8080" : annotation.url();
        }
        
        FeignInvocationHandler handler = new FeignInvocationHandler(
            baseUrl, serviceName, interceptors, errorDecoder, circuitBreakerEnabled, annotation.fallback()
        );
        
        return (T) Proxy.newProxyInstance(
            clientInterface.getClassLoader(),
            new Class<?>[]{clientInterface},
            handler
        );
    }
}

/**
 * Invocation Handler for Feign proxy
 */
class FeignInvocationHandler implements InvocationHandler {
    private final String baseUrl;
    private final String serviceName;
    private final List<RequestInterceptor> interceptors;
    private final ErrorDecoder errorDecoder;
    private final boolean circuitBreakerEnabled;
    private final Class<?> fallbackClass;
    private Object fallbackInstance;
    
    public FeignInvocationHandler(String baseUrl, String serviceName, 
                                  List<RequestInterceptor> interceptors,
                                  ErrorDecoder errorDecoder,
                                  boolean circuitBreakerEnabled,
                                  Class<?> fallbackClass) {
        this.baseUrl = baseUrl;
        this.serviceName = serviceName;
        this.interceptors = interceptors;
        this.errorDecoder = errorDecoder;
        this.circuitBreakerEnabled = circuitBreakerEnabled;
        this.fallbackClass = fallbackClass;
        
        if (fallbackClass != void.class) {
            try {
                this.fallbackInstance = fallbackClass.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                System.err.println("Failed to create fallback instance: " + e.getMessage());
            }
        }
    }
    
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            // Build request
            FeignRequest request = buildRequest(method, args);
            
            // Apply interceptors
            for (RequestInterceptor interceptor : interceptors) {
                interceptor.apply(request);
            }
            
            // Execute request
            System.out.println("Feign Request: " + request.getMethod() + " " + request.getUrl());
            Object response = executeRequest(request, method.getReturnType());
            
            return response;
            
        } catch (Exception e) {
            // Handle error with decoder
            Exception decoded = errorDecoder.decode(serviceName, e);
            
            // Try fallback if circuit breaker is enabled
            if (circuitBreakerEnabled && fallbackInstance != null) {
                System.out.println("Using fallback for " + method.getName());
                return method.invoke(fallbackInstance, args);
            }
            
            throw decoded;
        }
    }
    
    private FeignRequest buildRequest(Method method, Object[] args) {
        String path = extractPath(method);
        String httpMethod = extractHttpMethod(method);
        
        // Replace path variables
        if (args != null) {
            Annotation[][] paramAnnotations = method.getParameterAnnotations();
            for (int i = 0; i < args.length; i++) {
                for (Annotation annotation : paramAnnotations[i]) {
                    if (annotation instanceof PathVariable) {
                        PathVariable pv = (PathVariable) annotation;
                        String varName = pv.value().isEmpty() ? "arg" + i : pv.value();
                        path = path.replace("{" + varName + "}", String.valueOf(args[i]));
                    }
                }
            }
        }
        
        String url = baseUrl + path;
        FeignRequest request = new FeignRequest(httpMethod, url);
        
        // Add request body
        if (args != null) {
            Annotation[][] paramAnnotations = method.getParameterAnnotations();
            for (int i = 0; i < args.length; i++) {
                for (Annotation annotation : paramAnnotations[i]) {
                    if (annotation instanceof RequestBody) {
                        request.setBody(args[i]);
                    }
                }
            }
        }
        
        return request;
    }
    
    private String extractPath(Method method) {
        if (method.isAnnotationPresent(GetMapping.class)) {
            return method.getAnnotation(GetMapping.class).value();
        } else if (method.isAnnotationPresent(PostMapping.class)) {
            return method.getAnnotation(PostMapping.class).value();
        } else if (method.isAnnotationPresent(PutMapping.class)) {
            return method.getAnnotation(PutMapping.class).value();
        } else if (method.isAnnotationPresent(DeleteMapping.class)) {
            return method.getAnnotation(DeleteMapping.class).value();
        }
        return "";
    }
    
    private String extractHttpMethod(Method method) {
        if (method.isAnnotationPresent(GetMapping.class)) return "GET";
        if (method.isAnnotationPresent(PostMapping.class)) return "POST";
        if (method.isAnnotationPresent(PutMapping.class)) return "PUT";
        if (method.isAnnotationPresent(DeleteMapping.class)) return "DELETE";
        return "GET";
    }
    
    @SuppressWarnings("unchecked")
    private Object executeRequest(FeignRequest request, Class<?> returnType) {
        // Simulate HTTP request execution
        // In real implementation, would use HttpClient or RestTemplate
        
        if (returnType == User.class) {
            return new User("user123", "John Doe", "john@example.com");
        } else if (returnType == Order.class) {
            return new Order("ORD-123", "user123", "CREATED", LocalDateTime.now());
        } else if (returnType == List.class) {
            // Return mock list based on request
            if (request.getUrl().contains("products")) {
                return Arrays.asList(
                    new Product("prod1", "Laptop", 999.99),
                    new Product("prod2", "Mouse", 29.99)
                );
            }
            return Collections.emptyList();
        } else if (returnType == void.class || returnType == Void.class) {
            return null;
        }
        
        return null;
    }
}

/**
 * Feign Request representation
 */
class FeignRequest {
    private final String method;
    private final String url;
    private final Map<String, String> headers = new HashMap<>();
    private Object body;
    
    public FeignRequest(String method, String url) {
        this.method = method;
        this.url = url;
    }
    
    public void addHeader(String name, String value) {
        headers.put(name, value);
    }
    
    public String getMethod() { return method; }
    public String getUrl() { return url; }
    public Map<String, String> getHeaders() { return headers; }
    public Object getBody() { return body; }
    public void setBody(Object body) { this.body = body; }
}

/**
 * Request Interceptor interface
 */
interface RequestInterceptor {
    void apply(FeignRequest request);
}

/**
 * Authentication Interceptor
 */
class AuthenticationInterceptor implements RequestInterceptor {
    private final String token;
    
    public AuthenticationInterceptor(String token) {
        this.token = token;
    }
    
    @Override
    public void apply(FeignRequest request) {
        request.addHeader("Authorization", token);
        System.out.println("Added Authorization header: " + token);
    }
}

/**
 * Error Decoder interface
 */
interface ErrorDecoder {
    Exception decode(String serviceName, Exception exception);
}

/**
 * Default Error Decoder
 */
class DefaultErrorDecoder implements ErrorDecoder {
    @Override
    public Exception decode(String serviceName, Exception exception) {
        return new RuntimeException("Service call failed to " + serviceName + ": " + exception.getMessage());
    }
}

/**
 * Domain Models
 */
class User {
    private String id;
    private String name;
    private String email;
    
    public User() {}
    
    public User(String id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }
    
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}

class Order {
    private String orderId;
    private String userId;
    private String status;
    private LocalDateTime createdAt;
    
    public Order() {}
    
    public Order(String orderId, String userId, String status, LocalDateTime createdAt) {
        this.orderId = orderId;
        this.userId = userId;
        this.status = status;
        this.createdAt = createdAt;
    }
    
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

class OrderRequest {
    private String userId;
    private String productId;
    private int quantity;
    
    public OrderRequest() {}
    
    public OrderRequest(String userId, String productId, int quantity) {
        this.userId = userId;
        this.productId = productId;
        this.quantity = quantity;
    }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}

class Product {
    private String id;
    private String name;
    private double price;
    
    public Product() {}
    
    public Product(String id, String name, double price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }
    
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
}
