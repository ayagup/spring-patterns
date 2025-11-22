package com.example.api.hateoas;

import org.springframework.hateoas.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * HATEOAS Pattern Implementation
 * Hypermedia as the Engine of Application State
 * 
 * Purpose: Clients interact with application entirely through hypermedia
 * provided dynamically by application servers. No hardcoded URIs.
 * 
 * Key Components:
 * 1. Resource representations with embedded links
 * 2. Link relations (self, next, prev, collection)
 * 3. HAL (Hypertext Application Language) format
 * 4. Affordances (available actions)
 * 5. Resource assemblers
 * 
 * Features:
 * - Self-describing APIs
 * - Dynamic navigation through links
 * - Discoverable actions based on state
 * - Decoupling clients from server URI structure
 */

// Order Entity
class Order {
    private Long id;
    private String customerName;
    private String status; // PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED
    private Double totalAmount;
    private List<OrderItem> items;
    
    public Order() {
        this.items = new ArrayList<>();
    }
    
    public Order(Long id, String customerName, String status, Double totalAmount) {
        this.id = id;
        this.customerName = customerName;
        this.status = status;
        this.totalAmount = totalAmount;
        this.items = new ArrayList<>();
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }
    
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
}

class OrderItem {
    private String productName;
    private Integer quantity;
    private Double price;
    
    public OrderItem(String productName, Integer quantity, Double price) {
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
    }
    
    public String getProductName() { return productName; }
    public Integer getQuantity() { return quantity; }
    public Double getPrice() { return price; }
}

// HATEOAS Resource Model
class OrderResource extends RepresentationModel<OrderResource> {
    private Long orderId;
    private String customerName;
    private String status;
    private Double totalAmount;
    private List<OrderItem> items;
    
    public OrderResource(Order order) {
        this.orderId = order.getId();
        this.customerName = order.getCustomerName();
        this.status = order.getStatus();
        this.totalAmount = order.getTotalAmount();
        this.items = order.getItems();
    }
    
    public Long getOrderId() { return orderId; }
    public String getCustomerName() { return customerName; }
    public String getStatus() { return status; }
    public Double getTotalAmount() { return totalAmount; }
    public List<OrderItem> getItems() { return items; }
}

// Order Service
class OrderService {
    private final Map<Long, Order> orders = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    
    public OrderService() {
        // Initialize with sample data
        Order order1 = new Order(idGenerator.getAndIncrement(), "John Doe", "PENDING", 299.99);
        order1.getItems().add(new OrderItem("Laptop", 1, 999.99));
        orders.put(order1.getId(), order1);
        
        Order order2 = new Order(idGenerator.getAndIncrement(), "Jane Smith", "SHIPPED", 59.99);
        order2.getItems().add(new OrderItem("Mouse", 2, 29.99));
        orders.put(order2.getId(), order2);
    }
    
    public List<Order> getAllOrders() {
        return new ArrayList<>(orders.values());
    }
    
    public Optional<Order> getOrderById(Long id) {
        return Optional.ofNullable(orders.get(id));
    }
    
    public Order createOrder(Order order) {
        Long id = idGenerator.getAndIncrement();
        order.setId(id);
        order.setStatus("PENDING");
        orders.put(id, order);
        return order;
    }
    
    public boolean updateOrderStatus(Long id, String newStatus) {
        Order order = orders.get(id);
        if (order != null) {
            order.setStatus(newStatus);
            return true;
        }
        return false;
    }
    
    public boolean cancelOrder(Long id) {
        Order order = orders.get(id);
        if (order != null && order.getStatus().equals("PENDING")) {
            order.setStatus("CANCELLED");
            return true;
        }
        return false;
    }
}

// Resource Assembler
class OrderResourceAssembler {
    
    public OrderResource toResource(Order order) {
        OrderResource resource = new OrderResource(order);
        
        // Self link - link to this resource
        resource.add(Link.of("/api/orders/" + order.getId()).withSelfRel());
        
        // Collection link - link to all orders
        resource.add(Link.of("/api/orders").withRel("collection"));
        
        // State-based links (affordances)
        String status = order.getStatus();
        
        if ("PENDING".equals(status)) {
            // Can process or cancel pending orders
            resource.add(Link.of("/api/orders/" + order.getId() + "/process").withRel("process"));
            resource.add(Link.of("/api/orders/" + order.getId() + "/cancel").withRel("cancel"));
        } else if ("PROCESSING".equals(status)) {
            // Can ship processing orders
            resource.add(Link.of("/api/orders/" + order.getId() + "/ship").withRel("ship"));
        } else if ("SHIPPED".equals(status)) {
            // Can mark shipped orders as delivered
            resource.add(Link.of("/api/orders/" + order.getId() + "/deliver").withRel("deliver"));
        } else if ("DELIVERED".equals(status)) {
            // Can return delivered orders
            resource.add(Link.of("/api/orders/" + order.getId() + "/return").withRel("return"));
        }
        
        // Customer link
        resource.add(Link.of("/api/customers/" + order.getCustomerName()).withRel("customer"));
        
        // Payment link
        resource.add(Link.of("/api/orders/" + order.getId() + "/payment").withRel("payment"));
        
        return resource;
    }
    
    public CollectionModel<OrderResource> toCollectionResource(List<Order> orders) {
        List<OrderResource> orderResources = new ArrayList<>();
        for (Order order : orders) {
            orderResources.add(toResource(order));
        }
        
        CollectionModel<OrderResource> collection = CollectionModel.of(orderResources);
        collection.add(Link.of("/api/orders").withSelfRel());
        collection.add(Link.of("/api/orders/search").withRel("search"));
        
        return collection;
    }
}

// HATEOAS Controller
@RestController
@RequestMapping("/api/orders")
class OrderController {
    private final OrderService orderService = new OrderService();
    private final OrderResourceAssembler assembler = new OrderResourceAssembler();
    
    @GetMapping
    public ResponseEntity<CollectionModel<OrderResource>> getAllOrders() {
        List<Order> orders = orderService.getAllOrders();
        return ResponseEntity.ok(assembler.toCollectionResource(orders));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<OrderResource> getOrderById(@PathVariable Long id) {
        return orderService.getOrderById(id)
            .map(assembler::toResource)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<OrderResource> createOrder(@RequestBody Order order) {
        Order created = orderService.createOrder(order);
        OrderResource resource = assembler.toResource(created);
        return ResponseEntity.created(Link.of("/api/orders/" + created.getId()).toUri())
            .body(resource);
    }
    
    @PostMapping("/{id}/process")
    public ResponseEntity<OrderResource> processOrder(@PathVariable Long id) {
        boolean updated = orderService.updateOrderStatus(id, "PROCESSING");
        if (updated) {
            return orderService.getOrderById(id)
                .map(assembler::toResource)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
        }
        return ResponseEntity.badRequest().build();
    }
    
    @PostMapping("/{id}/cancel")
    public ResponseEntity<OrderResource> cancelOrder(@PathVariable Long id) {
        boolean cancelled = orderService.cancelOrder(id);
        if (cancelled) {
            return orderService.getOrderById(id)
                .map(assembler::toResource)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
        }
        return ResponseEntity.badRequest().build();
    }
}

/**
 * Demonstration of HATEOAS Pattern
 */
public class HATEOASPattern {
    
    public static void main(String[] args) {
        System.out.println("=== HATEOAS Pattern Demo ===\n");
        
        OrderService service = new OrderService();
        OrderResourceAssembler assembler = new OrderResourceAssembler();
        
        System.out.println("1. GET /api/orders/1 - Retrieve order with HATEOAS links");
        Optional<Order> order = service.getOrderById(1L);
        if (order.isPresent()) {
            OrderResource resource = assembler.toResource(order.get());
            System.out.println("   Order ID: " + resource.getOrderId());
            System.out.println("   Status: " + resource.getStatus());
            System.out.println("   Links:");
            resource.getLinks().forEach(link -> 
                System.out.println("     - " + link.getRel() + ": " + link.getHref())
            );
        }
        
        System.out.println("\n2. Understanding Link Relations:");
        System.out.println("   'self': Link to this resource");
        System.out.println("   'collection': Link to all orders");
        System.out.println("   'process': Action to process the order");
        System.out.println("   'cancel': Action to cancel the order");
        System.out.println("   'customer': Link to customer resource");
        System.out.println("   'payment': Link to payment information");
        
        System.out.println("\n3. State-based Affordances:");
        System.out.println("   PENDING order links: process, cancel");
        Order pending = new Order(10L, "Test", "PENDING", 100.0);
        OrderResource pendingResource = assembler.toResource(pending);
        System.out.println("   Available actions:");
        pendingResource.getLinks().forEach(link -> {
            if (!"self".equals(link.getRel().value()) && !"collection".equals(link.getRel().value())) {
                System.out.println("     - " + link.getRel());
            }
        });
        
        System.out.println("\n   SHIPPED order links: deliver");
        Order shipped = new Order(11L, "Test", "SHIPPED", 100.0);
        OrderResource shippedResource = assembler.toResource(shipped);
        System.out.println("   Available actions:");
        shippedResource.getLinks().forEach(link -> {
            if (!"self".equals(link.getRel().value()) && !"collection".equals(link.getRel().value()) 
                && !"customer".equals(link.getRel().value()) && !"payment".equals(link.getRel().value())) {
                System.out.println("     - " + link.getRel());
            }
        });
        
        System.out.println("\n4. Collection Resource with Links:");
        List<Order> allOrders = service.getAllOrders();
        CollectionModel<OrderResource> collection = assembler.toCollectionResource(allOrders);
        System.out.println("   Total orders: " + collection.getContent().size());
        System.out.println("   Collection links:");
        collection.getLinks().forEach(link ->
            System.out.println("     - " + link.getRel() + ": " + link.getHref())
        );
        
        System.out.println("\n=== HATEOAS Benefits ===");
        System.out.println("1. Discoverability: Clients discover available actions through links");
        System.out.println("2. Loose Coupling: Clients don't hardcode URIs");
        System.out.println("3. Evolvability: Server can change URIs without breaking clients");
        System.out.println("4. State Transitions: Links represent valid state transitions");
        System.out.println("5. Self-Documenting: Available actions are clear from the response");
        
        System.out.println("\n=== HAL Format Example ===");
        System.out.println("{");
        System.out.println("  \"orderId\": 1,");
        System.out.println("  \"customerName\": \"John Doe\",");
        System.out.println("  \"status\": \"PENDING\",");
        System.out.println("  \"totalAmount\": 299.99,");
        System.out.println("  \"_links\": {");
        System.out.println("    \"self\": { \"href\": \"/api/orders/1\" },");
        System.out.println("    \"collection\": { \"href\": \"/api/orders\" },");
        System.out.println("    \"process\": { \"href\": \"/api/orders/1/process\" },");
        System.out.println("    \"cancel\": { \"href\": \"/api/orders/1/cancel\" }");
        System.out.println("  }");
        System.out.println("}");
    }
}
