package com.example.api.composition;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * API Composition & API Gateway Aggregation Pattern
 * 
 * Purpose: Combine data from multiple services into unified responses
 * 
 * API Composition Pattern:
 * - Aggregates data from multiple microservices
 * - Executes requests in parallel for performance
 * - Combines results into single response
 * - Handles partial failures gracefully
 * 
 * API Gateway Aggregation Pattern:
 * - Central entry point for all client requests
 * - Routes requests to appropriate services
 * - Aggregates multiple service calls
 * - Provides cross-cutting concerns (auth, logging, rate limiting)
 * 
 * Features:
 * - Parallel service calls
 * - Response merging
 * - Fallback handling
 * - Request routing
 * - Load balancing
 */

// Service Response Models
class UserData {
    private Long id;
    private String name;
    private String email;
    
    public UserData(Long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }
    
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
}

class OrderData {
    private Long id;
    private Long userId;
    private String status;
    private Double total;
    
    public OrderData(Long id, Long userId, String status, Double total) {
        this.id = id;
        this.userId = userId;
        this.status = status;
        this.total = total;
    }
    
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getStatus() { return status; }
    public Double getTotal() { return total; }
}

class PaymentData {
    private Long orderId;
    private String method;
    private String status;
    
    public PaymentData(Long orderId, String method, String status) {
        this.orderId = orderId;
        this.method = method;
        this.status = status;
    }
    
    public Long getOrderId() { return orderId; }
    public String getMethod() { return method; }
    public String getStatus() { return status; }
}

// Composed Response
class UserProfile {
    private UserData user;
    private List<OrderData> orders;
    private Map<String, Object> preferences;
    private Map<String, Object> metadata;
    
    public UserData getUser() { return user; }
    public void setUser(UserData user) { this.user = user; }
    
    public List<OrderData> getOrders() { return orders; }
    public void setOrders(List<OrderData> orders) { this.orders = orders; }
    
    public Map<String, Object> getPreferences() { return preferences; }
    public void setPreferences(Map<String, Object> preferences) { this.preferences = preferences; }
    
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
}

class OrderSummary {
    private OrderData order;
    private UserData user;
    private PaymentData payment;
    private List<String> items;
    
    public OrderData getOrder() { return order; }
    public void setOrder(OrderData order) { this.order = order; }
    
    public UserData getUser() { return user; }
    public void setUser(UserData user) { this.user = user; }
    
    public PaymentData getPayment() { return payment; }
    public void setPayment(PaymentData payment) { this.payment = payment; }
    
    public List<String> getItems() { return items; }
    public void setItems(List<String> items) { this.items = items; }
}

// Simulated Microservices
class UserService {
    private Map<Long, UserData> users = new ConcurrentHashMap<>();
    
    public UserService() {
        users.put(1L, new UserData(1L, "Alice", "alice@example.com"));
        users.put(2L, new UserData(2L, "Bob", "bob@example.com"));
    }
    
    public CompletableFuture<UserData> getUserAsync(Long userId) {
        return CompletableFuture.supplyAsync(() -> {
            simulateDelay(100); // Simulate network latency
            return users.get(userId);
        });
    }
    
    public UserData getUser(Long userId) {
        simulateDelay(100);
        return users.get(userId);
    }
    
    private void simulateDelay(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) {}
    }
}

class OrderService {
    private Map<Long, List<OrderData>> userOrders = new ConcurrentHashMap<>();
    
    public OrderService() {
        userOrders.put(1L, Arrays.asList(
            new OrderData(101L, 1L, "COMPLETED", 299.99),
            new OrderData(102L, 1L, "PENDING", 149.99)
        ));
        userOrders.put(2L, Arrays.asList(
            new OrderData(201L, 2L, "SHIPPED", 599.99)
        ));
    }
    
    public CompletableFuture<List<OrderData>> getUserOrdersAsync(Long userId) {
        return CompletableFuture.supplyAsync(() -> {
            simulateDelay(150);
            return userOrders.getOrDefault(userId, new ArrayList<>());
        });
    }
    
    public OrderData getOrder(Long orderId) {
        simulateDelay(150);
        return userOrders.values().stream()
            .flatMap(List::stream)
            .filter(o -> o.getId().equals(orderId))
            .findFirst()
            .orElse(null);
    }
    
    private void simulateDelay(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) {}
    }
}

class PaymentService {
    private Map<Long, PaymentData> payments = new ConcurrentHashMap<>();
    
    public PaymentService() {
        payments.put(101L, new PaymentData(101L, "CREDIT_CARD", "PAID"));
        payments.put(102L, new PaymentData(102L, "PAYPAL", "PENDING"));
        payments.put(201L, new PaymentData(201L, "CREDIT_CARD", "PAID"));
    }
    
    public CompletableFuture<PaymentData> getPaymentAsync(Long orderId) {
        return CompletableFuture.supplyAsync(() -> {
            simulateDelay(80);
            return payments.get(orderId);
        });
    }
    
    private void simulateDelay(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) {}
    }
}

class PreferencesService {
    public CompletableFuture<Map<String, Object>> getUserPreferencesAsync(Long userId) {
        return CompletableFuture.supplyAsync(() -> {
            simulateDelay(50);
            Map<String, Object> prefs = new HashMap<>();
            prefs.put("theme", "dark");
            prefs.put("language", "en");
            prefs.put("notifications", true);
            return prefs;
        });
    }
    
    private void simulateDelay(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) {}
    }
}

/**
 * API Composition Service
 * Aggregates data from multiple services
 */
class CompositionService {
    private final UserService userService = new UserService();
    private final OrderService orderService = new OrderService();
    private final PreferencesService preferencesService = new PreferencesService();
    private final ExecutorService executor = Executors.newFixedThreadPool(10);
    
    /**
     * Compose user profile from multiple services
     * Parallel execution for performance
     */
    public CompletableFuture<UserProfile> getUserProfile(Long userId) {
        CompletableFuture<UserData> userFuture = userService.getUserAsync(userId);
        CompletableFuture<List<OrderData>> ordersFuture = orderService.getUserOrdersAsync(userId);
        CompletableFuture<Map<String, Object>> prefsFuture = preferencesService.getUserPreferencesAsync(userId);
        
        // Combine all futures
        return CompletableFuture.allOf(userFuture, ordersFuture, prefsFuture)
            .thenApply(v -> {
                UserProfile profile = new UserProfile();
                profile.setUser(userFuture.join());
                profile.setOrders(ordersFuture.join());
                profile.setPreferences(prefsFuture.join());
                
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("totalOrders", profile.getOrders().size());
                metadata.put("composedAt", new Date());
                profile.setMetadata(metadata);
                
                return profile;
            });
    }
    
    /**
     * Compose user profile with fallback handling
     */
    public UserProfile getUserProfileWithFallback(Long userId) {
        UserProfile profile = new UserProfile();
        
        // User data (required)
        try {
            profile.setUser(userService.getUser(userId));
        } catch (Exception e) {
            throw new RuntimeException("User service failed", e);
        }
        
        // Orders (optional, use fallback on failure)
        try {
            profile.setOrders(orderService.getUserOrdersAsync(userId).get(2, TimeUnit.SECONDS));
        } catch (Exception e) {
            profile.setOrders(new ArrayList<>()); // Fallback to empty list
        }
        
        // Preferences (optional)
        try {
            profile.setPreferences(preferencesService.getUserPreferencesAsync(userId).get(2, TimeUnit.SECONDS));
        } catch (Exception e) {
            Map<String, Object> defaultPrefs = new HashMap<>();
            defaultPrefs.put("theme", "light");
            defaultPrefs.put("language", "en");
            profile.setPreferences(defaultPrefs);
        }
        
        return profile;
    }
    
    public void shutdown() {
        executor.shutdown();
    }
}

/**
 * API Gateway Service
 * Central entry point with routing and aggregation
 */
class ApiGateway {
    private final UserService userService = new UserService();
    private final OrderService orderService = new OrderService();
    private final PaymentService paymentService = new PaymentService();
    private final CompositionService compositionService = new CompositionService();
    
    /**
     * Aggregate order details from multiple services
     */
    public CompletableFuture<OrderSummary> getOrderSummary(Long orderId) {
        // Get order first
        CompletableFuture<OrderData> orderFuture = CompletableFuture.supplyAsync(
            () -> orderService.getOrder(orderId)
        );
        
        return orderFuture.thenCompose(order -> {
            if (order == null) {
                return CompletableFuture.completedFuture(null);
            }
            
            // Parallel calls for user and payment
            CompletableFuture<UserData> userFuture = userService.getUserAsync(order.getUserId());
            CompletableFuture<PaymentData> paymentFuture = paymentService.getPaymentAsync(order.getId());
            
            return CompletableFuture.allOf(userFuture, paymentFuture)
                .thenApply(v -> {
                    OrderSummary summary = new OrderSummary();
                    summary.setOrder(order);
                    summary.setUser(userFuture.join());
                    summary.setPayment(paymentFuture.join());
                    summary.setItems(Arrays.asList("Item 1", "Item 2")); // Mock items
                    return summary;
                });
        });
    }
    
    /**
     * Route request to appropriate service
     */
    public Object routeRequest(String path, Map<String, String> params) {
        if (path.startsWith("/users/")) {
            Long userId = Long.parseLong(path.substring("/users/".length()));
            if (params.containsKey("include") && params.get("include").equals("full")) {
                try {
                    return compositionService.getUserProfile(userId).get();
                } catch (Exception e) {
                    return null;
                }
            }
            return userService.getUser(userId);
        } else if (path.startsWith("/orders/")) {
            Long orderId = Long.parseLong(path.substring("/orders/".length()));
            if (params.containsKey("summary") && params.get("summary").equals("true")) {
                try {
                    return getOrderSummary(orderId).get();
                } catch (Exception e) {
                    return null;
                }
            }
            return orderService.getOrder(orderId);
        }
        return null;
    }
}

/**
 * API Composition Controller
 */
@RestController
@RequestMapping("/api/composite")
class CompositionController {
    private final CompositionService compositionService = new CompositionService();
    
    @GetMapping("/users/{userId}/profile")
    public CompletableFuture<ResponseEntity<UserProfile>> getUserProfile(@PathVariable Long userId) {
        return compositionService.getUserProfile(userId)
            .thenApply(ResponseEntity::ok)
            .exceptionally(e -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }
}

/**
 * API Gateway Controller
 */
@RestController
@RequestMapping("/gateway")
class GatewayController {
    private final ApiGateway gateway = new ApiGateway();
    
    @GetMapping("/orders/{orderId}/summary")
    public CompletableFuture<ResponseEntity<OrderSummary>> getOrderSummary(@PathVariable Long orderId) {
        return gateway.getOrderSummary(orderId)
            .thenApply(summary -> summary != null ? 
                ResponseEntity.ok(summary) : 
                ResponseEntity.notFound().build());
    }
}

/**
 * Demonstration of API Composition and Gateway Aggregation Patterns
 */
public class APICompositionAndGatewayPattern {
    
    public static void main(String[] args) throws Exception {
        System.out.println("=== API Composition Pattern Demo ===\n");
        
        CompositionService compositionService = new CompositionService();
        
        System.out.println("1. Sequential Service Calls (Slow):");
        long start = System.currentTimeMillis();
        UserService userService = new UserService();
        OrderService orderService = new OrderService();
        UserData user = userService.getUser(1L);
        List<OrderData> orders = orderService.getUserOrdersAsync(1L).get();
        long sequential = System.currentTimeMillis() - start;
        System.out.println("   User: " + user.getName());
        System.out.println("   Orders: " + orders.size());
        System.out.println("   Time: " + sequential + "ms");
        
        System.out.println("\n2. Parallel Composition (Fast):");
        start = System.currentTimeMillis();
        UserProfile profile = compositionService.getUserProfile(1L).get();
        long parallel = System.currentTimeMillis() - start;
        System.out.println("   User: " + profile.getUser().getName());
        System.out.println("   Orders: " + profile.getOrders().size());
        System.out.println("   Preferences: " + profile.getPreferences());
        System.out.println("   Time: " + parallel + "ms");
        System.out.println("   Performance Gain: " + (sequential - parallel) + "ms faster");
        
        System.out.println("\n3. Composition with Fallback:");
        UserProfile profileWithFallback = compositionService.getUserProfileWithFallback(1L);
        System.out.println("   User: " + profileWithFallback.getUser().getName());
        System.out.println("   Orders: " + profileWithFallback.getOrders().size());
        System.out.println("   Preferences: " + profileWithFallback.getPreferences());
        System.out.println("   Note: Uses fallback values on service failure");
        
        System.out.println("\n=== API Gateway Aggregation Pattern Demo ===\n");
        
        ApiGateway gateway = new ApiGateway();
        
        System.out.println("1. Aggregate Order Summary:");
        OrderSummary summary = gateway.getOrderSummary(101L).get();
        System.out.println("   Order ID: " + summary.getOrder().getId());
        System.out.println("   Customer: " + summary.getUser().getName());
        System.out.println("   Total: $" + summary.getOrder().getTotal());
        System.out.println("   Payment: " + summary.getPayment().getMethod() + " - " + summary.getPayment().getStatus());
        System.out.println("   Items: " + summary.getItems());
        
        System.out.println("\n2. Gateway Routing:");
        System.out.println("   GET /gateway/users/1 → UserService");
        System.out.println("   GET /gateway/users/1?include=full → CompositionService");
        System.out.println("   GET /gateway/orders/101 → OrderService");
        System.out.println("   GET /gateway/orders/101/summary → Aggregated response");
        
        System.out.println("\n=== Composition Patterns ===");
        System.out.println("1. Parallel Composition:");
        System.out.println("   - Execute multiple service calls in parallel");
        System.out.println("   - Use CompletableFuture.allOf()");
        System.out.println("   - Reduces total latency");
        
        System.out.println("\n2. Sequential Composition:");
        System.out.println("   - When services depend on each other");
        System.out.println("   - Use thenCompose() or thenApply()");
        System.out.println("   - Example: Get user → Get user's orders");
        
        System.out.println("\n3. Fallback Composition:");
        System.out.println("   - Handle partial failures gracefully");
        System.out.println("   - Provide default values");
        System.out.println("   - Mark optional vs required data");
        
        System.out.println("\n=== API Gateway Responsibilities ===");
        System.out.println("✓ Request Routing - Route to appropriate microservice");
        System.out.println("✓ Request Aggregation - Combine multiple service calls");
        System.out.println("✓ Protocol Translation - REST → gRPC, HTTP → WebSocket");
        System.out.println("✓ Authentication & Authorization - Centralized security");
        System.out.println("✓ Rate Limiting - Per-client request limits");
        System.out.println("✓ Load Balancing - Distribute load across instances");
        System.out.println("✓ Caching - Cache frequent responses");
        System.out.println("✓ Logging & Monitoring - Centralized observability");
        System.out.println("✓ Circuit Breaking - Prevent cascading failures");
        System.out.println("✓ Request/Response Transformation - Adapt formats");
        
        System.out.println("\n=== Best Practices ===");
        System.out.println("✓ Use parallel calls when services are independent");
        System.out.println("✓ Set timeouts for all service calls");
        System.out.println("✓ Implement circuit breakers for downstream services");
        System.out.println("✓ Provide fallback values for non-critical data");
        System.out.println("✓ Cache aggregated responses when possible");
        System.out.println("✓ Monitor and log all gateway requests");
        System.out.println("✓ Use bulkhead pattern to isolate service failures");
        System.out.println("✓ Implement retry logic with exponential backoff");
        System.out.println("✓ Consider using a gateway framework (Spring Cloud Gateway, Kong, etc.)");
        
        compositionService.shutdown();
    }
}
