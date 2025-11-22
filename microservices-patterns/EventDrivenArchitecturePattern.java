package com.example.microservices.eventdriven;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * Event-Driven Architecture Pattern
 * 
 * This pattern demonstrates event-driven microservices architecture where services
 * communicate through asynchronous event publishing and consumption.
 * 
 * Key Components:
 * 1. EventBus - Central message broker for event distribution
 * 2. EventPublisher - Publishes domain events
 * 3. EventListener - Subscribes to and handles events
 * 4. EventStore - Persists events for event sourcing
 * 5. DomainEvent - Represents a state change in the system
 * 
 * Patterns Demonstrated:
 * - Event Sourcing - Store state as sequence of events
 * - CQRS - Command Query Responsibility Segregation
 * - Saga Pattern - Distributed transactions across services
 * - Event Streaming - Real-time event processing
 * 
 * Use Cases:
 * - Decoupled microservices communication
 * - Audit trails and event history
 * - Real-time data synchronization
 * - Complex business workflows
 * - Eventual consistency
 */

@SpringBootApplication
public class EventDrivenArchitecturePattern {

    public static void main(String[] args) {
        SpringApplication.run(EventDrivenArchitecturePattern.class, args);
        
        // Demonstration
        System.out.println("=== Event-Driven Architecture Pattern Demo ===\n");
        
        // Create Event Bus and Event Store
        EventBus eventBus = new EventBus();
        EventStore eventStore = new EventStore();
        
        // Create services
        OrderService orderService = new OrderService(eventBus, eventStore);
        InventoryService inventoryService = new InventoryService(eventBus);
        PaymentService paymentService = new PaymentService();
        NotificationService notificationService = new NotificationService();
        
        // Register event listeners
        System.out.println("1. Registering Event Listeners:");
        registerEventListeners(eventBus, inventoryService, paymentService, notificationService);
        
        // Create an order (triggers event chain)
        System.out.println("\n2. Creating Order (Event Chain):");
        String orderId = orderService.createOrder("user-123", Arrays.asList(
            new OrderItem("product-1", 2, 29.99),
            new OrderItem("product-2", 1, 49.99)
        ));
        
        // Wait for async processing
        try { Thread.sleep(500); } catch (InterruptedException e) {}
        
        // Event Sourcing - Replay events
        System.out.println("\n3. Event Sourcing - Replaying Events:");
        List<DomainEvent> orderEvents = eventStore.getEvents(orderId);
        System.out.println("Total events for order " + orderId + ": " + orderEvents.size());
        orderEvents.forEach(event -> 
            System.out.println("  - " + event.getEventType() + " at " + event.getTimestamp()));
        
        // Reconstruct order state from events
        Order reconstructedOrder = orderService.reconstructOrder(orderId);
        System.out.println("Reconstructed order status: " + reconstructedOrder.getStatus());
        
        // CQRS - Command and Query separation
        System.out.println("\n4. CQRS Pattern:");
        orderService.updateOrderStatus(orderId, OrderStatus.SHIPPED);
        
        OrderQueryModel queryModel = orderService.getOrderQuery(orderId);
        System.out.println("Order query: " + queryModel.getOrderId() + 
                         " - Status: " + queryModel.getStatus() + 
                         " - Total: $" + queryModel.getTotalAmount());
        
        // Saga Pattern - Distributed transaction
        System.out.println("\n5. Saga Pattern - Distributed Transaction:");
        String sagaOrderId = orderService.createOrderWithSaga("user-456", Arrays.asList(
            new OrderItem("product-3", 5, 99.99)
        ));
        
        try { Thread.sleep(500); } catch (InterruptedException e) {}
        
        // Event Statistics
        System.out.println("\n6. Event Bus Statistics:");
        Map<String, Object> stats = eventBus.getStatistics();
        stats.forEach((key, value) -> System.out.println("  " + key + ": " + value));
        
        // Event Store Statistics
        System.out.println("\n7. Event Store Statistics:");
        System.out.println("  Total events: " + eventStore.getTotalEvents());
        System.out.println("  Aggregates: " + eventStore.getAggregateCount());
    }
    
    private static void registerEventListeners(EventBus eventBus, 
                                              InventoryService inventoryService,
                                              PaymentService paymentService,
                                              NotificationService notificationService) {
        
        eventBus.subscribe("OrderCreated", inventoryService::onOrderCreated);
        eventBus.subscribe("OrderCreated", paymentService::onOrderCreated);
        eventBus.subscribe("OrderCreated", notificationService::onOrderCreated);
        
        eventBus.subscribe("InventoryReserved", paymentService::onInventoryReserved);
        eventBus.subscribe("PaymentProcessed", notificationService::onPaymentProcessed);
        
        eventBus.subscribe("OrderShipped", notificationService::onOrderShipped);
        
        System.out.println("Event listeners registered for OrderCreated, InventoryReserved, PaymentProcessed, OrderShipped");
    }
}

/**
 * Event Bus - Message broker for event distribution
 */
class EventBus {
    private final Map<String, List<Consumer<DomainEvent>>> subscribers = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newFixedThreadPool(10);
    private long totalEventsPublished = 0;
    
    public void subscribe(String eventType, Consumer<DomainEvent> handler) {
        subscribers.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>()).add(handler);
    }
    
    public void publish(DomainEvent event) {
        totalEventsPublished++;
        String eventType = event.getEventType();
        
        List<Consumer<DomainEvent>> handlers = subscribers.get(eventType);
        if (handlers != null) {
            handlers.forEach(handler -> 
                executor.submit(() -> {
                    try {
                        handler.accept(event);
                    } catch (Exception e) {
                        System.err.println("Error handling event " + eventType + ": " + e.getMessage());
                    }
                })
            );
        }
    }
    
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalEventsPublished", totalEventsPublished);
        stats.put("eventTypes", subscribers.keySet());
        stats.put("totalSubscribers", subscribers.values().stream().mapToInt(List::size).sum());
        return stats;
    }
    
    public void shutdown() {
        executor.shutdown();
    }
}

/**
 * Domain Event - Base class for all events
 */
abstract class DomainEvent {
    private final String eventId;
    private final String eventType;
    private final String aggregateId;
    private final LocalDateTime timestamp;
    private final Map<String, Object> metadata;
    
    protected DomainEvent(String eventType, String aggregateId) {
        this.eventId = UUID.randomUUID().toString();
        this.eventType = eventType;
        this.aggregateId = aggregateId;
        this.timestamp = LocalDateTime.now();
        this.metadata = new HashMap<>();
    }
    
    public String getEventId() { return eventId; }
    public String getEventType() { return eventType; }
    public String getAggregateId() { return aggregateId; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public Map<String, Object> getMetadata() { return metadata; }
    
    public void addMetadata(String key, Object value) {
        metadata.put(key, value);
    }
}

/**
 * Order Events
 */
class OrderCreatedEvent extends DomainEvent {
    private final String userId;
    private final List<OrderItem> items;
    private final double totalAmount;
    
    public OrderCreatedEvent(String orderId, String userId, List<OrderItem> items, double totalAmount) {
        super("OrderCreated", orderId);
        this.userId = userId;
        this.items = items;
        this.totalAmount = totalAmount;
    }
    
    public String getUserId() { return userId; }
    public List<OrderItem> getItems() { return items; }
    public double getTotalAmount() { return totalAmount; }
}

class InventoryReservedEvent extends DomainEvent {
    private final String orderId;
    
    public InventoryReservedEvent(String orderId) {
        super("InventoryReserved", orderId);
        this.orderId = orderId;
    }
    
    public String getOrderId() { return orderId; }
}

class PaymentProcessedEvent extends DomainEvent {
    private final String orderId;
    private final double amount;
    
    public PaymentProcessedEvent(String orderId, double amount) {
        super("PaymentProcessed", orderId);
        this.orderId = orderId;
        this.amount = amount;
    }
    
    public String getOrderId() { return orderId; }
    public double getAmount() { return amount; }
}

class OrderShippedEvent extends DomainEvent {
    private final String orderId;
    
    public OrderShippedEvent(String orderId) {
        super("OrderShipped", orderId);
        this.orderId = orderId;
    }
    
    public String getOrderId() { return orderId; }
}

class OrderStatusChangedEvent extends DomainEvent {
    private final OrderStatus oldStatus;
    private final OrderStatus newStatus;
    
    public OrderStatusChangedEvent(String orderId, OrderStatus oldStatus, OrderStatus newStatus) {
        super("OrderStatusChanged", orderId);
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
    }
    
    public OrderStatus getOldStatus() { return oldStatus; }
    public OrderStatus getNewStatus() { return newStatus; }
}

/**
 * Event Store - Persists events for event sourcing
 */
class EventStore {
    private final Map<String, List<DomainEvent>> eventStreams = new ConcurrentHashMap<>();
    
    public void append(DomainEvent event) {
        String aggregateId = event.getAggregateId();
        eventStreams.computeIfAbsent(aggregateId, k -> new CopyOnWriteArrayList<>()).add(event);
    }
    
    public List<DomainEvent> getEvents(String aggregateId) {
        return new ArrayList<>(eventStreams.getOrDefault(aggregateId, Collections.emptyList()));
    }
    
    public List<DomainEvent> getEvents(String aggregateId, long fromVersion) {
        List<DomainEvent> allEvents = getEvents(aggregateId);
        return allEvents.subList((int) fromVersion, allEvents.size());
    }
    
    public long getTotalEvents() {
        return eventStreams.values().stream().mapToLong(List::size).sum();
    }
    
    public int getAggregateCount() {
        return eventStreams.size();
    }
}

/**
 * Order Service - Publishes domain events
 */
@Service
class OrderService {
    private final EventBus eventBus;
    private final EventStore eventStore;
    private final Map<String, Order> orders = new ConcurrentHashMap<>();
    
    public OrderService(EventBus eventBus, EventStore eventStore) {
        this.eventBus = eventBus;
        this.eventStore = eventStore;
    }
    
    public String createOrder(String userId, List<OrderItem> items) {
        String orderId = "ORD-" + UUID.randomUUID().toString().substring(0, 8);
        double totalAmount = items.stream().mapToDouble(i -> i.getPrice() * i.getQuantity()).sum();
        
        Order order = new Order(orderId, userId, items, totalAmount);
        orders.put(orderId, order);
        
        // Publish event
        OrderCreatedEvent event = new OrderCreatedEvent(orderId, userId, items, totalAmount);
        eventStore.append(event);
        eventBus.publish(event);
        
        System.out.println("Order created: " + orderId + " - Total: $" + totalAmount);
        return orderId;
    }
    
    public String createOrderWithSaga(String userId, List<OrderItem> items) {
        String orderId = createOrder(userId, items);
        System.out.println("Saga started for order: " + orderId);
        return orderId;
    }
    
    public void updateOrderStatus(String orderId, OrderStatus newStatus) {
        Order order = orders.get(orderId);
        if (order != null) {
            OrderStatus oldStatus = order.getStatus();
            order.setStatus(newStatus);
            
            OrderStatusChangedEvent event = new OrderStatusChangedEvent(orderId, oldStatus, newStatus);
            eventStore.append(event);
            eventBus.publish(event);
            
            if (newStatus == OrderStatus.SHIPPED) {
                OrderShippedEvent shippedEvent = new OrderShippedEvent(orderId);
                eventStore.append(shippedEvent);
                eventBus.publish(shippedEvent);
            }
            
            System.out.println("Order status updated: " + orderId + " - " + newStatus);
        }
    }
    
    public Order reconstructOrder(String orderId) {
        List<DomainEvent> events = eventStore.getEvents(orderId);
        Order order = null;
        
        for (DomainEvent event : events) {
            if (event instanceof OrderCreatedEvent) {
                OrderCreatedEvent e = (OrderCreatedEvent) event;
                order = new Order(orderId, e.getUserId(), e.getItems(), e.getTotalAmount());
            } else if (event instanceof OrderStatusChangedEvent) {
                OrderStatusChangedEvent e = (OrderStatusChangedEvent) event;
                if (order != null) {
                    order.setStatus(e.getNewStatus());
                }
            }
        }
        
        return order;
    }
    
    public OrderQueryModel getOrderQuery(String orderId) {
        Order order = orders.get(orderId);
        if (order != null) {
            return new OrderQueryModel(order);
        }
        return null;
    }
}

/**
 * Inventory Service - Reacts to order events
 */
@Service
class InventoryService {
    private final EventBus eventBus;
    private final Map<String, Integer> inventory = new ConcurrentHashMap<>();
    
    public InventoryService(EventBus eventBus) {
        this.eventBus = eventBus;
        
        // Initialize inventory
        inventory.put("product-1", 100);
        inventory.put("product-2", 50);
        inventory.put("product-3", 25);
    }
    
    public void onOrderCreated(DomainEvent event) {
        if (event instanceof OrderCreatedEvent) {
            OrderCreatedEvent orderEvent = (OrderCreatedEvent) event;
            String orderId = orderEvent.getAggregateId();
            
            System.out.println("Inventory: Reserving items for order " + orderId);
            
            // Reserve inventory
            boolean reserved = reserveInventory(orderEvent.getItems());
            
            if (reserved) {
                InventoryReservedEvent reservedEvent = new InventoryReservedEvent(orderId);
                eventBus.publish(reservedEvent);
                System.out.println("Inventory: Items reserved for order " + orderId);
            } else {
                System.out.println("Inventory: Insufficient stock for order " + orderId);
            }
        }
    }
    
    private boolean reserveInventory(List<OrderItem> items) {
        // Check and reserve inventory
        for (OrderItem item : items) {
            Integer stock = inventory.get(item.getProductId());
            if (stock == null || stock < item.getQuantity()) {
                return false;
            }
        }
        
        // Reserve
        items.forEach(item -> 
            inventory.computeIfPresent(item.getProductId(), (k, v) -> v - item.getQuantity()));
        
        return true;
    }
}

/**
 * Payment Service - Processes payments
 */
@Service
class PaymentService {
    
    public void onOrderCreated(DomainEvent event) {
        if (event instanceof OrderCreatedEvent) {
            OrderCreatedEvent orderEvent = (OrderCreatedEvent) event;
            System.out.println("Payment: Initiated for order " + orderEvent.getAggregateId());
        }
    }
    
    public void onInventoryReserved(DomainEvent event) {
        if (event instanceof InventoryReservedEvent) {
            InventoryReservedEvent invEvent = (InventoryReservedEvent) event;
            String orderId = invEvent.getOrderId();
            
            System.out.println("Payment: Processing payment for order " + orderId);
            
            // Simulate payment processing
            boolean success = processPayment(orderId);
            
            if (success) {
                System.out.println("Payment: Completed for order " + orderId);
            }
        }
    }
    
    private boolean processPayment(String orderId) {
        // Simulate payment processing
        return true;
    }
}

/**
 * Notification Service - Sends notifications
 */
@Service
class NotificationService {
    
    public void onOrderCreated(DomainEvent event) {
        if (event instanceof OrderCreatedEvent) {
            OrderCreatedEvent orderEvent = (OrderCreatedEvent) event;
            System.out.println("Notification: Order confirmation sent for " + orderEvent.getAggregateId());
        }
    }
    
    public void onPaymentProcessed(DomainEvent event) {
        if (event instanceof PaymentProcessedEvent) {
            PaymentProcessedEvent paymentEvent = (PaymentProcessedEvent) event;
            System.out.println("Notification: Payment receipt sent for order " + paymentEvent.getOrderId());
        }
    }
    
    public void onOrderShipped(DomainEvent event) {
        if (event instanceof OrderShippedEvent) {
            OrderShippedEvent shippedEvent = (OrderShippedEvent) event;
            System.out.println("Notification: Shipping notification sent for order " + shippedEvent.getOrderId());
        }
    }
}

/**
 * Domain Models
 */
class Order {
    private final String orderId;
    private final String userId;
    private final List<OrderItem> items;
    private final double totalAmount;
    private OrderStatus status;
    private final LocalDateTime createdAt;
    
    public Order(String orderId, String userId, List<OrderItem> items, double totalAmount) {
        this.orderId = orderId;
        this.userId = userId;
        this.items = new ArrayList<>(items);
        this.totalAmount = totalAmount;
        this.status = OrderStatus.CREATED;
        this.createdAt = LocalDateTime.now();
    }
    
    public String getOrderId() { return orderId; }
    public String getUserId() { return userId; }
    public List<OrderItem> getItems() { return items; }
    public double getTotalAmount() { return totalAmount; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}

class OrderItem {
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
}

enum OrderStatus {
    CREATED, PAYMENT_PENDING, PAYMENT_COMPLETED, SHIPPED, DELIVERED, CANCELLED
}

/**
 * CQRS Query Model
 */
class OrderQueryModel {
    private final String orderId;
    private final String userId;
    private final double totalAmount;
    private final OrderStatus status;
    private final int itemCount;
    
    public OrderQueryModel(Order order) {
        this.orderId = order.getOrderId();
        this.userId = order.getUserId();
        this.totalAmount = order.getTotalAmount();
        this.status = order.getStatus();
        this.itemCount = order.getItems().size();
    }
    
    public String getOrderId() { return orderId; }
    public String getUserId() { return userId; }
    public double getTotalAmount() { return totalAmount; }
    public OrderStatus getStatus() { return status; }
    public int getItemCount() { return itemCount; }
}

/**
 * REST Controller for Event-Driven APIs
 */
@RestController
@RequestMapping("/api")
class EventDrivenController {
    private final OrderService orderService;
    private final EventBus eventBus;
    private final EventStore eventStore;
    
    public EventDrivenController(OrderService orderService, EventBus eventBus, EventStore eventStore) {
        this.orderService = orderService;
        this.eventBus = eventBus;
        this.eventStore = eventStore;
    }
    
    @PostMapping("/orders")
    public Map<String, Object> createOrder(@RequestBody Map<String, Object> request) {
        String userId = (String) request.get("userId");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> itemsData = (List<Map<String, Object>>) request.get("items");
        
        List<OrderItem> items = new ArrayList<>();
        for (Map<String, Object> itemData : itemsData) {
            items.add(new OrderItem(
                (String) itemData.get("productId"),
                (Integer) itemData.get("quantity"),
                (Double) itemData.get("price")
            ));
        }
        
        String orderId = orderService.createOrder(userId, items);
        return Map.of("orderId", orderId, "status", "created");
    }
    
    @GetMapping("/orders/{orderId}/events")
    public List<DomainEvent> getOrderEvents(@PathVariable String orderId) {
        return eventStore.getEvents(orderId);
    }
    
    @GetMapping("/events/stats")
    public Map<String, Object> getEventStats() {
        Map<String, Object> stats = eventBus.getStatistics();
        stats.put("eventStoreTotal", eventStore.getTotalEvents());
        return stats;
    }
}
