package com.example.springmodulithpatterns;

import org.springframework.modulith.ApplicationModule;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Application Module Pattern
 * 
 * Demonstrates Spring Modulith's @ApplicationModule for organizing code into
 * logical, self-contained modules with clear boundaries and dependencies.
 * 
 * Key Concepts:
 * - Package-by-feature organization
 * - Module boundaries and encapsulation
 * - API vs Internal packages
 * - Module metadata and documentation
 * - Inter-module communication
 */
@SpringBootApplication
public class ApplicationModulePattern {

    public static void main(String[] args) {
        SpringApplication.run(ApplicationModulePattern.class, args);
    }

    /**
     * Application Module configuration
     * Defines module boundaries and allowed dependencies
     */
    @ApplicationModule(
        displayName = "Order Management",
        allowedDependencies = {"inventory", "payment"}
    )
    static class OrderModule {
        // This annotation marks the package as a Spring Modulith module
        // All subpackages are considered internal unless explicitly exposed
    }

    /**
     * Service layer - Internal to the module
     */
    @Service
    static class OrderService {
        
        public Order createOrder(CreateOrderRequest request) {
            // Validate order
            if (request.items().isEmpty()) {
                throw new IllegalArgumentException("Order must have at least one item");
            }
            
            // Create order
            Order order = new Order(
                generateOrderId(),
                request.customerId(),
                request.items(),
                calculateTotal(request.items()),
                OrderStatus.PENDING
            );
            
            return order;
        }
        
        public Order getOrder(String orderId) {
            // Simulate order retrieval
            return new Order(
                orderId,
                "CUST-001",
                List.of(new OrderItem("ITEM-001", 2, 29.99)),
                59.98,
                OrderStatus.CONFIRMED
            );
        }
        
        public List<Order> getOrdersByCustomer(String customerId) {
            // Simulate customer orders retrieval
            return List.of(
                new Order(
                    "ORD-001",
                    customerId,
                    List.of(new OrderItem("ITEM-001", 1, 29.99)),
                    29.99,
                    OrderStatus.SHIPPED
                )
            );
        }
        
        public Order updateOrderStatus(String orderId, OrderStatus status) {
            Order order = getOrder(orderId);
            return new Order(
                order.orderId(),
                order.customerId(),
                order.items(),
                order.total(),
                status
            );
        }
        
        private String generateOrderId() {
            return "ORD-" + System.currentTimeMillis();
        }
        
        private double calculateTotal(List<OrderItem> items) {
            return items.stream()
                .mapToDouble(item -> item.quantity() * item.price())
                .sum();
        }
    }

    /**
     * REST Controller - Public API of the module
     * This represents the module's interface exposed to other modules
     */
    @RestController
    @RequestMapping("/api/orders")
    static class OrderController {
        
        private final OrderService orderService;
        
        public OrderController(OrderService orderService) {
            this.orderService = orderService;
        }
        
        @PostMapping
        public OrderResponse createOrder(@RequestBody CreateOrderRequest request) {
            Order order = orderService.createOrder(request);
            return new OrderResponse(
                order.orderId(),
                order.customerId(),
                order.items(),
                order.total(),
                order.status().name(),
                "Order created successfully"
            );
        }
        
        @GetMapping("/{orderId}")
        public OrderResponse getOrder(@PathVariable String orderId) {
            Order order = orderService.getOrder(orderId);
            return new OrderResponse(
                order.orderId(),
                order.customerId(),
                order.items(),
                order.total(),
                order.status().name(),
                "success"
            );
        }
        
        @GetMapping("/customer/{customerId}")
        public CustomerOrdersResponse getCustomerOrders(@PathVariable String customerId) {
            List<Order> orders = orderService.getOrdersByCustomer(customerId);
            List<OrderInfo> orderInfos = orders.stream()
                .map(o -> new OrderInfo(o.orderId(), o.total(), o.status().name()))
                .toList();
            return new CustomerOrdersResponse(customerId, orderInfos, orderInfos.size());
        }
        
        @PatchMapping("/{orderId}/status")
        public OrderResponse updateStatus(
                @PathVariable String orderId,
                @RequestBody UpdateStatusRequest request) {
            Order order = orderService.updateOrderStatus(
                orderId,
                OrderStatus.valueOf(request.status())
            );
            return new OrderResponse(
                order.orderId(),
                order.customerId(),
                order.items(),
                order.total(),
                order.status().name(),
                "Status updated successfully"
            );
        }
        
        @GetMapping("/info")
        public Map<String, Object> getInfo() {
            return Map.of(
                "pattern", "Application Module Pattern",
                "description", "Organize code into logical, self-contained modules with clear boundaries",
                "module", "Order Management",
                "allowedDependencies", List.of("inventory", "payment"),
                "features", List.of(
                    "Package-by-feature organization",
                    "Clear module boundaries",
                    "API vs Internal separation",
                    "Inter-module communication",
                    "Dependency management"
                ),
                "endpoints", List.of(
                    "POST /api/orders",
                    "GET /api/orders/{orderId}",
                    "GET /api/orders/customer/{customerId}",
                    "PATCH /api/orders/{orderId}/status",
                    "GET /api/orders/info"
                )
            );
        }
    }

    // Domain Models
    record Order(
        String orderId,
        String customerId,
        List<OrderItem> items,
        double total,
        OrderStatus status
    ) {}

    record OrderItem(String productId, int quantity, double price) {}

    enum OrderStatus {
        PENDING, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED
    }

    // DTOs
    record CreateOrderRequest(String customerId, List<OrderItem> items) {}
    record UpdateStatusRequest(String status) {}
    record OrderResponse(
        String orderId,
        String customerId,
        List<OrderItem> items,
        double total,
        String status,
        String message
    ) {}
    record OrderInfo(String orderId, double total, String status) {}
    record CustomerOrdersResponse(String customerId, List<OrderInfo> orders, int count) {}
}
