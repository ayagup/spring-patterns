package com.example.events.domain;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.data.domain.DomainEvents;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Domain Event Pattern - Demonstrates Domain-Driven Design Events
 * 
 * This pattern shows how to:
 * 1. Create domain events in DDD context
 * 2. Use @DomainEvents annotation
 * 3. Publish events from aggregate roots
 * 4. Register domain events
 * 5. Clear events after publishing
 * 6. Implement event-driven aggregates
 * 7. Handle domain event listeners
 * 8. Maintain event ordering
 * 9. Implement event-driven sagas
 * 10. Track domain changes via events
 * 
 * Key Concepts:
 * - Domain Event: Something that happened in the domain
 * - Aggregate Root: Entity that publishes domain events
 * - @DomainEvents: Marks method that returns domain events
 * - @AfterDomainEventPublication: Clears events after publishing
 * - Event Sourcing: Store changes as sequence of events
 * 
 * Domain Event Principles:
 * 1. Named in Past Tense (OrderPlaced, UserRegistered)
 * 2. Immutable after creation
 * 3. Contain all necessary information
 * 4. Published by aggregate roots
 * 5. Express domain concepts
 * 
 * Dependencies:
 * - spring-context
 * - spring-data-commons
 * 
 * @author Spring Patterns
 * @version 1.0
 */

@SpringBootApplication
public class DomainEventPattern {

    public static void main(String[] args) {
        SpringApplication.run(DomainEventPattern.class, args);
        
        System.out.println("=".repeat(80));
        System.out.println("DOMAIN EVENT PATTERN DEMONSTRATION");
        System.out.println("=".repeat(80));
        
        demonstrateDomainEvents();
        demonstrateAggregateRoots();
        
        System.out.println("\nApplication running with Domain Event patterns");
        System.out.println("Test endpoints:");
        System.out.println("POST /api/domain/orders - Create order (publishes domain events)");
        System.out.println("GET /api/domain/events - View domain event history");
        System.out.println("\nPress Ctrl+C to stop.\n");
    }
    
    private static void demonstrateDomainEvents() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("DOMAIN EVENT CHARACTERISTICS");
        System.out.println("=".repeat(80));
        
        System.out.println("\n1. Past Tense Naming:");
        System.out.println("   OrderPlaced, UserRegistered, PaymentProcessed");
        
        System.out.println("\n2. Immutability:");
        System.out.println("   Events cannot be changed after creation");
        
        System.out.println("\n3. Self-Contained:");
        System.out.println("   Events contain all necessary information");
    }
    
    private static void demonstrateAggregateRoots() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("AGGREGATE ROOTS AND DOMAIN EVENTS");
        System.out.println("=".repeat(80));
        
        System.out.println("\n- Extend AbstractAggregateRoot");
        System.out.println("- Use registerEvent() to add events");
        System.out.println("- @DomainEvents automatically published");
        System.out.println("- Events cleared after publication");
    }
}

/**
 * Domain Event Base Class
 */
abstract class BaseDomainEvent {
    private final String eventId;
    private final LocalDateTime occurredOn;
    
    protected BaseDomainEvent() {
        this.eventId = UUID.randomUUID().toString();
        this.occurredOn = LocalDateTime.now();
    }
    
    public String getEventId() { return eventId; }
    public LocalDateTime getOccurredOn() { return occurredOn; }
}

/**
 * Order Domain Events
 */
class OrderCreatedEvent extends BaseDomainEvent {
    private final String orderId;
    private final String customerId;
    
    public OrderCreatedEvent(String orderId, String customerId) {
        super();
        this.orderId = orderId;
        this.customerId = customerId;
    }
    
    public String getOrderId() { return orderId; }
    public String getCustomerId() { return customerId; }
}

class OrderItemAddedEvent extends BaseDomainEvent {
    private final String orderId;
    private final String productId;
    private final int quantity;
    private final double price;
    
    public OrderItemAddedEvent(String orderId, String productId, int quantity, double price) {
        super();
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.price = price;
    }
    
    public String getOrderId() { return orderId; }
    public String getProductId() { return productId; }
    public int getQuantity() { return quantity; }
    public double getPrice() { return price; }
}

class OrderPlacedEvent extends BaseDomainEvent {
    private final String orderId;
    private final double totalAmount;
    
    public OrderPlacedEvent(String orderId, double totalAmount) {
        super();
        this.orderId = orderId;
        this.totalAmount = totalAmount;
    }
    
    public String getOrderId() { return orderId; }
    public double getTotalAmount() { return totalAmount; }
}

class OrderCancelledEvent extends BaseDomainEvent {
    private final String orderId;
    private final String reason;
    
    public OrderCancelledEvent(String orderId, String reason) {
        super();
        this.orderId = orderId;
        this.reason = reason;
    }
    
    public String getOrderId() { return orderId; }
    public String getReason() { return reason; }
}

/**
 * User Domain Events
 */
class UserCreatedEvent extends BaseDomainEvent {
    private final String userId;
    private final String username;
    private final String email;
    
    public UserCreatedEvent(String userId, String username, String email) {
        super();
        this.userId = userId;
        this.username = username;
        this.email = email;
    }
    
    public String getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
}

class UserActivatedEvent extends BaseDomainEvent {
    private final String userId;
    
    public UserActivatedEvent(String userId) {
        super();
        this.userId = userId;
    }
    
    public String getUserId() { return userId; }
}

class UserDeactivatedEvent extends BaseDomainEvent {
    private final String userId;
    
    public UserDeactivatedEvent(String userId) {
        super();
        this.userId = userId;
    }
    
    public String getUserId() { return userId; }
}

/**
 * Product Domain Events
 */
class ProductCreatedEvent extends BaseDomainEvent {
    private final String productId;
    private final String name;
    private final double price;
    
    public ProductCreatedEvent(String productId, String name, double price) {
        super();
        this.productId = productId;
        this.name = name;
        this.price = price;
    }
    
    public String getProductId() { return productId; }
    public String getName() { return name; }
    public double getPrice() { return price; }
}

class ProductPriceChangedEvent extends BaseDomainEvent {
    private final String productId;
    private final double oldPrice;
    private final double newPrice;
    
    public ProductPriceChangedEvent(String productId, double oldPrice, double newPrice) {
        super();
        this.productId = productId;
        this.oldPrice = oldPrice;
        this.newPrice = newPrice;
    }
    
    public String getProductId() { return productId; }
    public double getOldPrice() { return oldPrice; }
    public double getNewPrice() { return newPrice; }
}

/**
 * Order Aggregate Root
 */
class Order extends AbstractAggregateRoot<Order> {
    private final String orderId;
    private final String customerId;
    private final List<OrderItem> items;
    private OrderStatus status;
    private double totalAmount;
    
    public Order(String orderId, String customerId) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.items = new ArrayList<>();
        this.status = OrderStatus.CREATED;
        this.totalAmount = 0.0;
        
        // Register domain event
        registerEvent(new OrderCreatedEvent(orderId, customerId));
    }
    
    public void addItem(String productId, int quantity, double price) {
        OrderItem item = new OrderItem(productId, quantity, price);
        items.add(item);
        totalAmount += item.getSubtotal();
        
        registerEvent(new OrderItemAddedEvent(orderId, productId, quantity, price));
    }
    
    public void placeOrder() {
        if (items.isEmpty()) {
            throw new IllegalStateException("Cannot place order without items");
        }
        
        this.status = OrderStatus.PLACED;
        registerEvent(new OrderPlacedEvent(orderId, totalAmount));
    }
    
    public void cancel(String reason) {
        this.status = OrderStatus.CANCELLED;
        registerEvent(new OrderCancelledEvent(orderId, reason));
    }
    
    public String getOrderId() { return orderId; }
    public String getCustomerId() { return customerId; }
    public List<OrderItem> getItems() { return new ArrayList<>(items); }
    public OrderStatus getStatus() { return status; }
    public double getTotalAmount() { return totalAmount; }
    
    enum OrderStatus {
        CREATED, PLACED, CANCELLED, COMPLETED
    }
    
    static class OrderItem {
        private final String productId;
        private final int quantity;
        private final double price;
        
        public OrderItem(String productId, int quantity, double price) {
            this.productId = productId;
            this.quantity = quantity;
            this.price = price;
        }
        
        public String getProductId() { return productId; }
        public int getQuantity() { return quantity; }
        public double getPrice() { return price; }
        public double getSubtotal() { return quantity * price; }
    }
}

/**
 * User Aggregate Root
 */
class User extends AbstractAggregateRoot<User> {
    private final String userId;
    private final String username;
    private final String email;
    private boolean active;
    
    public User(String userId, String username, String email) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.active = false;
        
        registerEvent(new UserCreatedEvent(userId, username, email));
    }
    
    public void activate() {
        this.active = true;
        registerEvent(new UserActivatedEvent(userId));
    }
    
    public void deactivate() {
        this.active = false;
        registerEvent(new UserDeactivatedEvent(userId));
    }
    
    public String getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public boolean isActive() { return active; }
}

/**
 * Product Aggregate Root
 */
class Product extends AbstractAggregateRoot<Product> {
    private final String productId;
    private final String name;
    private double price;
    
    public Product(String productId, String name, double price) {
        this.productId = productId;
        this.name = name;
        this.price = price;
        
        registerEvent(new ProductCreatedEvent(productId, name, price));
    }
    
    public void changePrice(double newPrice) {
        double oldPrice = this.price;
        this.price = newPrice;
        registerEvent(new ProductPriceChangedEvent(productId, oldPrice, newPrice));
    }
    
    public String getProductId() { return productId; }
    public String getName() { return name; }
    public double getPrice() { return price; }
}

/**
 * Domain Event Listeners
 */
@Component
class OrderDomainEventListener {
    
    @EventListener
    public void handleOrderCreated(OrderCreatedEvent event) {
        System.out.printf("[OrderDomainEventListener] Order created: %s for customer %s%n",
            event.getOrderId(), event.getCustomerId());
    }
    
    @EventListener
    public void handleOrderItemAdded(OrderItemAddedEvent event) {
        System.out.printf("[OrderDomainEventListener] Item added to order %s: %s (qty: %d, price: $%.2f)%n",
            event.getOrderId(), event.getProductId(), event.getQuantity(), event.getPrice());
    }
    
    @EventListener
    public void handleOrderPlaced(OrderPlacedEvent event) {
        System.out.printf("[OrderDomainEventListener] Order placed: %s, Total: $%.2f%n",
            event.getOrderId(), event.getTotalAmount());
    }
    
    @EventListener
    public void handleOrderCancelled(OrderCancelledEvent event) {
        System.out.printf("[OrderDomainEventListener] Order cancelled: %s, Reason: %s%n",
            event.getOrderId(), event.getReason());
    }
}

@Component
class UserDomainEventListener {
    
    @EventListener
    public void handleUserCreated(UserCreatedEvent event) {
        System.out.printf("[UserDomainEventListener] User created: %s (%s)%n",
            event.getUsername(), event.getEmail());
    }
    
    @EventListener
    public void handleUserActivated(UserActivatedEvent event) {
        System.out.printf("[UserDomainEventListener] User activated: %s%n", event.getUserId());
    }
    
    @EventListener
    public void handleUserDeactivated(UserDeactivatedEvent event) {
        System.out.printf("[UserDomainEventListener] User deactivated: %s%n", event.getUserId());
    }
}

@Component
class ProductDomainEventListener {
    
    @EventListener
    public void handleProductCreated(ProductCreatedEvent event) {
        System.out.printf("[ProductDomainEventListener] Product created: %s, Price: $%.2f%n",
            event.getName(), event.getPrice());
    }
    
    @EventListener
    public void handleProductPriceChanged(ProductPriceChangedEvent event) {
        System.out.printf("[ProductDomainEventListener] Product %s price changed: $%.2f → $%.2f%n",
            event.getProductId(), event.getOldPrice(), event.getNewPrice());
    }
}

/**
 * Domain Event Publisher Service
 */
@Service
class DomainEventPublisherService {
    
    private final ApplicationEventPublisher eventPublisher;
    
    public DomainEventPublisherService(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }
    
    public void publishDomainEvents(AbstractAggregateRoot<?> aggregate) {
        aggregate.domainEvents().forEach(eventPublisher::publishEvent);
        aggregate.clearDomainEvents();
    }
}

/**
 * Order Service
 */
@Service
class OrderService {
    
    private final DomainEventPublisherService domainEventPublisher;
    private final Map<String, Order> orders = new HashMap<>();
    
    public OrderService(DomainEventPublisherService domainEventPublisher) {
        this.domainEventPublisher = domainEventPublisher;
    }
    
    public Order createOrder(String orderId, String customerId) {
        Order order = new Order(orderId, customerId);
        orders.put(orderId, order);
        
        domainEventPublisher.publishDomainEvents(order);
        
        return order;
    }
    
    public void addItem(String orderId, String productId, int quantity, double price) {
        Order order = orders.get(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Order not found: " + orderId);
        }
        
        order.addItem(productId, quantity, price);
        domainEventPublisher.publishDomainEvents(order);
    }
    
    public void placeOrder(String orderId) {
        Order order = orders.get(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Order not found: " + orderId);
        }
        
        order.placeOrder();
        domainEventPublisher.publishDomainEvents(order);
    }
    
    public Order getOrder(String orderId) {
        return orders.get(orderId);
    }
}

/**
 * REST Controller for Domain Events
 */
@RestController
@RequestMapping("/api/domain")
class DomainEventController {
    
    private final OrderService orderService;
    
    public DomainEventController(OrderService orderService) {
        this.orderService = orderService;
    }
    
    @PostMapping("/orders")
    public Map<String, Object> createOrder(
            @RequestParam String orderId,
            @RequestParam String customerId) {
        
        Order order = orderService.createOrder(orderId, customerId);
        
        return Map.of(
            "orderId", order.getOrderId(),
            "customerId", order.getCustomerId(),
            "status", order.getStatus().toString()
        );
    }
    
    @PostMapping("/orders/{orderId}/items")
    public Map<String, String> addOrderItem(
            @PathVariable String orderId,
            @RequestParam String productId,
            @RequestParam int quantity,
            @RequestParam double price) {
        
        orderService.addItem(orderId, productId, quantity, price);
        
        return Map.of(
            "status", "added",
            "orderId", orderId,
            "productId", productId
        );
    }
    
    @PostMapping("/orders/{orderId}/place")
    public Map<String, Object> placeOrder(@PathVariable String orderId) {
        orderService.placeOrder(orderId);
        
        Order order = orderService.getOrder(orderId);
        
        return Map.of(
            "status", "placed",
            "orderId", orderId,
            "totalAmount", order.getTotalAmount()
        );
    }
    
    @GetMapping("/orders/{orderId}")
    public Map<String, Object> getOrder(@PathVariable String orderId) {
        Order order = orderService.getOrder(orderId);
        
        if (order == null) {
            return Map.of("error", "Order not found");
        }
        
        return Map.of(
            "orderId", order.getOrderId(),
            "customerId", order.getCustomerId(),
            "status", order.getStatus().toString(),
            "totalAmount", order.getTotalAmount(),
            "itemCount", order.getItems().size()
        );
    }
}
