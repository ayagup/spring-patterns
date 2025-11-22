# Event-Driven Patterns in Spring

This directory contains comprehensive implementations of **8 Event-Driven Patterns** in Spring Framework. These patterns demonstrate how to build event-driven architectures, implement reactive systems, and design loosely coupled applications using Spring's event infrastructure and Project Reactor.

## 📋 Table of Contents

- [Overview](#overview)
- [Patterns Implemented](#patterns-implemented)
- [Prerequisites](#prerequisites)
- [Dependencies](#dependencies)
- [Pattern Details](#pattern-details)
- [Usage Examples](#usage-examples)
- [Best Practices](#best-practices)
- [Testing](#testing)
- [Production Considerations](#production-considerations)
- [References](#references)

## 🎯 Overview

Event-driven architecture is a software design pattern promoting the production, detection, consumption, and reaction to events. These patterns demonstrate:

- **Loose Coupling**: Components communicate through events without direct dependencies
- **Scalability**: Asynchronous processing enables better resource utilization
- **Flexibility**: Easy to add new event handlers without modifying publishers
- **Auditability**: Complete history of state changes through events
- **Temporal Decoupling**: Publishers and consumers don't need to be active simultaneously

## 📚 Patterns Implemented

| Pattern | File | Description | Lines |
|---------|------|-------------|-------|
| **Event Publisher** | `EventPublisherPattern.java` | ApplicationEventPublisher, custom events, publishing mechanisms | 500 |
| **Event Listener** | `EventListenerPattern.java` | @EventListener, conditional listening, async listeners | 550 |
| **Application Event** | `ApplicationEventPattern.java` | ApplicationEvent hierarchy, context lifecycle events | 500 |
| **Domain Event** | `DomainEventPattern.java` | DDD domain events, @DomainEvents, aggregate roots | 550 |
| **Event Bus** | `EventBusPattern.java` | Custom event bus, topic routing, subscribers | 500 |
| **Event Sourcing** | `EventSourcingPattern.java` | Event store, aggregate reconstruction, snapshots | 600 |
| **Event Stream** | `EventStreamPattern.java` | Reactive streams, Flux/Mono, backpressure | 550 |
| **Event Store** | `EventStorePattern.java` | Append-only storage, versioning, event queries | 600 |

**Total**: 8 patterns, **4,350 lines** of production-quality code

## 🔧 Prerequisites

- **Java**: 17 or higher
- **Spring Boot**: 3.0+
- **Spring Framework**: 6.0+
- **Project Reactor**: 3.5+ (for reactive patterns)
- **Maven/Gradle**: For dependency management

## 📦 Dependencies

### Core Dependencies

```xml
<!-- Spring Boot Starter -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter</artifactId>
</dependency>

<!-- Spring Context (Events) -->
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-context</artifactId>
</dependency>

<!-- Spring Data Commons (Domain Events) -->
<dependency>
    <groupId>org.springframework.data</groupId>
    <artifactId>spring-data-commons</artifactId>
</dependency>
```

### Reactive Dependencies

```xml
<!-- Spring WebFlux (for reactive streams) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>

<!-- Reactor Core -->
<dependency>
    <groupId>io.projectreactor</groupId>
    <artifactId>reactor-core</artifactId>
</dependency>
```

### Optional Dependencies

```xml
<!-- For REST endpoints -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<!-- For async processing -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
</dependency>
```

## 📖 Pattern Details

### 1. Event Publisher Pattern

**Purpose**: Publish events using Spring's ApplicationEventPublisher

**Key Features**:
- Direct event publishing with `ApplicationEventPublisher`
- Custom event publishers implementing `ApplicationEventPublisherAware`
- Synchronous and asynchronous event publishing
- Event metadata and tracking
- Conditional event publishing

**Usage**:
```java
@Service
public class OrderService {
    private final ApplicationEventPublisher eventPublisher;
    
    public void createOrder(Order order) {
        // Business logic
        eventPublisher.publishEvent(new OrderCreatedEvent(order));
    }
}
```

**REST Endpoints**:
- `POST /api/events/user` - Publish user created event
- `POST /api/events/order` - Publish order placed event
- `GET /api/events/history` - View event publishing history

**When to Use**:
- Need to notify other components of state changes
- Implementing cross-cutting concerns (logging, auditing)
- Building loosely coupled systems
- Publishing business domain events

---

### 2. Event Listener Pattern

**Purpose**: Handle events using @EventListener annotation

**Key Features**:
- Method-level event listeners with `@EventListener`
- Conditional listening using SpEL expressions
- Asynchronous listeners with `@Async`
- Ordered listener execution with `@Order`
- Generic event handling for multiple event types
- Exception handling in listeners

**Usage**:
```java
@Component
public class OrderEventListener {
    
    @EventListener
    @Order(1)
    public void handleOrderCreated(OrderCreatedEvent event) {
        // Handle event synchronously
    }
    
    @Async
    @EventListener
    public void sendNotification(OrderCreatedEvent event) {
        // Handle event asynchronously
    }
    
    @EventListener(condition = "#event.amount > 1000")
    public void handleLargeOrder(OrderEvent event) {
        // Only process large orders
    }
}
```

**REST Endpoints**:
- `POST /api/listener/trigger/user` - Trigger user event
- `POST /api/listener/trigger/order` - Trigger order event
- `GET /api/listener/stats` - View listener statistics

**When to Use**:
- Processing events from multiple sources
- Implementing event-driven workflows
- Decoupling event producers from consumers
- Parallel processing of events

---

### 3. Application Event Pattern

**Purpose**: Use Spring's ApplicationEvent hierarchy for lifecycle events

**Key Features**:
- Extending `ApplicationEvent` for custom events
- Listening to context lifecycle events (ContextRefreshedEvent, etc.)
- Spring Boot application events (ApplicationReadyEvent, etc.)
- Implementing `ApplicationListener` interface
- Event source tracking
- Context-aware event handling

**Built-in Events**:
- `ContextRefreshedEvent` - Context initialized/refreshed
- `ContextStartedEvent` - Context started
- `ContextStoppedEvent` - Context stopped
- `ContextClosedEvent` - Context closing
- `ApplicationReadyEvent` - Application ready to serve traffic

**Usage**:
```java
public class UserRegistrationEvent extends ApplicationEvent {
    private final String userId;
    private final String email;
    
    public UserRegistrationEvent(Object source, String userId, String email) {
        super(source);
        this.userId = userId;
        this.email = email;
    }
}

@Component
public class ContextEventListener {
    @EventListener
    public void onContextRefresh(ContextRefreshedEvent event) {
        // Initialization logic
    }
}
```

**REST Endpoints**:
- `POST /api/app-events/custom` - Publish custom application event
- `POST /api/app-events/user-registration` - Publish user registration
- `GET /api/app-events/history` - View event history
- `GET /api/app-events/context-info` - Get context information

**When to Use**:
- Hooking into application lifecycle
- Initialization and cleanup tasks
- Context-aware event processing
- Legacy code requiring ApplicationEvent

---

### 4. Domain Event Pattern

**Purpose**: Implement Domain-Driven Design (DDD) domain events

**Key Features**:
- Domain events in DDD context
- Aggregate roots publishing events with `AbstractAggregateRoot`
- `@DomainEvents` annotation for event collection
- `@AfterDomainEventPublication` for cleanup
- Event-driven aggregates
- Domain event listeners
- Automatic event publishing on save

**Principles**:
- Events named in **past tense** (OrderPlaced, UserRegistered)
- Events are **immutable** after creation
- Events contain all **necessary information**
- Published by **aggregate roots**
- Express **domain concepts**

**Usage**:
```java
public class Order extends AbstractAggregateRoot<Order> {
    
    public Order create(String customerId) {
        // Business logic
        registerEvent(new OrderCreatedEvent(this.id, customerId));
        return this;
    }
    
    public void placeOrder() {
        // Business logic
        registerEvent(new OrderPlacedEvent(this.id, this.totalAmount));
    }
}

@Component
public class OrderDomainEventListener {
    @EventListener
    public void handleOrderPlaced(OrderPlacedEvent event) {
        // Process domain event
    }
}
```

**REST Endpoints**:
- `POST /api/domain/orders` - Create order (publishes domain events)
- `POST /api/domain/orders/{id}/items` - Add order item
- `POST /api/domain/orders/{id}/place` - Place order
- `GET /api/domain/orders/{id}` - Get order state

**When to Use**:
- Implementing Domain-Driven Design
- Modeling business domain events
- Event-sourced aggregates
- Capturing business state changes

---

### 5. Event Bus Pattern

**Purpose**: Custom event bus for topic-based event routing

**Key Features**:
- Topic-based event routing
- Multiple subscribers per topic
- Synchronous and asynchronous event buses
- Subscriber registration and management
- Event filtering by topic
- Dead letter queue support
- Event bus metrics and monitoring

**Components**:
- **Event Bus**: Central distribution mechanism
- **Topics**: Named channels for routing
- **Subscribers**: Event consumers
- **Publishers**: Event producers

**Usage**:
```java
@Component
public class EventBusService {
    private final SynchronousEventBus syncBus;
    private final AsynchronousEventBus asyncBus;
    
    public void publishToTopic(String topic, Object payload) {
        BusEvent<Object> event = new BusEvent<>(topic, payload);
        asyncBus.publish(event);
    }
}

// Subscribe to topics
public class OrderSubscriber implements EventBusSubscriber<OrderData> {
    @Override
    public void onEvent(BusEvent<OrderData> event) {
        // Process event
    }
}

eventBus.subscribe("order.created", orderSubscriber);
```

**REST Endpoints**:
- `POST /api/eventbus/publish/sync` - Publish to topic (synchronous)
- `POST /api/eventbus/publish/async` - Publish to topic (asynchronous)
- `GET /api/eventbus/metrics` - View event bus metrics
- `GET /api/eventbus/subscribers` - View subscriber counts

**When to Use**:
- Need custom routing logic
- Topic-based pub/sub messaging
- Multiple subscriber types per event
- Decoupled microservices communication

---

### 6. Event Sourcing Pattern

**Purpose**: Store state as sequence of events instead of current state

**Key Features**:
- Event-sourced aggregates
- Event store integration
- Aggregate reconstruction from events
- Snapshots for performance optimization
- Event replay capability
- Command handling
- Optimistic concurrency control
- Event versioning

**Principles**:
- **Events as Source of Truth**: State derived from events
- **Append-Only**: Events never deleted or modified
- **Immutable**: Events cannot change after storage
- **Complete Audit Trail**: All changes recorded
- **Temporal Queries**: Query state at any point in time

**Usage**:
```java
public class Account {
    private String accountId;
    private double balance;
    private long version;
    private final List<DomainEvent> uncommittedEvents = new ArrayList<>();
    
    // Command
    public void deposit(double amount) {
        applyEvent(new MoneyDepositedEvent(accountId, version + 1, amount));
    }
    
    // Apply event
    private void applyEvent(DomainEvent event) {
        applyChange(event);  // Update state
        uncommittedEvents.add(event);  // Track for saving
    }
    
    // Reconstruct from history
    public void loadFromHistory(List<DomainEvent> history) {
        for (DomainEvent event : history) {
            applyChange(event);
        }
    }
}

// Repository
@Component
public class AccountRepository {
    public void save(Account account) {
        List<DomainEvent> events = account.getUncommittedEvents();
        eventStore.saveEvents(account.getId(), events, account.getVersion());
        account.markEventsAsCommitted();
    }
    
    public Account findById(String id) {
        Account account = new Account();
        List<DomainEvent> events = eventStore.getEvents(id);
        account.loadFromHistory(events);
        return account;
    }
}
```

**REST Endpoints**:
- `POST /api/sourcing/accounts` - Create account
- `POST /api/sourcing/accounts/{id}/deposit` - Deposit money
- `POST /api/sourcing/accounts/{id}/withdraw` - Withdraw money
- `GET /api/sourcing/accounts/{id}` - Get account state
- `GET /api/sourcing/accounts/{id}/events` - Get account events

**When to Use**:
- Need complete audit trail
- Temporal queries required
- Event replay capability needed
- Complex business domains
- Regulatory compliance requirements

---

### 7. Event Stream Pattern

**Purpose**: Reactive event streams using Project Reactor

**Key Features**:
- `Flux` for multiple events (0 to N)
- `Mono` for single events (0 or 1)
- Backpressure handling strategies
- Stream transformations (map, filter, flatMap)
- Stream combinations (merge, zip, concat)
- Buffering and windowing
- Error handling and retry
- Hot vs cold streams

**Backpressure Strategies**:
- **Buffer**: Store events temporarily
- **Drop**: Discard events when overwhelmed
- **Latest**: Keep only latest event
- **Error**: Signal error when overwhelmed

**Usage**:
```java
@Component
public class EventStreamProcessor {
    
    // Hot stream (multicast)
    private final Sinks.Many<Event> sink = Sinks.many().multicast().onBackpressureBuffer();
    
    public void publishEvent(Event event) {
        sink.tryEmitNext(event);
    }
    
    public Flux<Event> subscribe() {
        return sink.asFlux();
    }
    
    // Transform stream
    public Flux<ProcessedEvent> processEvents() {
        return sink.asFlux()
            .map(this::transform)
            .filter(event -> event.isValid())
            .buffer(10)  // Batch 10 events
            .flatMap(this::processBatch);
    }
    
    // Combine streams
    public Flux<Event> mergeStreams(Flux<Event> stream1, Flux<Event> stream2) {
        return Flux.merge(stream1, stream2);
    }
}
```

**REST Endpoints**:
- `POST /api/stream/publish` - Publish event to stream
- `GET /api/stream/events` - Subscribe to event stream (SSE)
- `GET /api/stream/events/cold` - Get cold stream
- `GET /api/stream/events/buffered` - Get buffered events
- `GET /api/stream/statistics` - View stream statistics

**When to Use**:
- Reactive applications
- Real-time event processing
- Handling high-volume events
- Backpressure management needed
- Stream composition and transformation

---

### 8. Event Store Pattern

**Purpose**: Persistent append-only storage for events

**Key Features**:
- Append-only event storage
- Event versioning per aggregate
- Global event sequence
- Aggregate stream queries
- Event type queries
- Time-range queries
- Optimistic concurrency control
- Event projections
- Store metrics and statistics

**Storage Concepts**:
- **Aggregate Stream**: All events for one aggregate
- **Global Stream**: All events in chronological order
- **Event Version**: Monotonically increasing per aggregate
- **Global Sequence**: Unique sequence number for all events

**Usage**:
```java
@Component
public class EventStore {
    
    // Append event
    public StoredEvent appendEvent(AppendEvent event) {
        long newVersion = getCurrentVersion(event.getAggregateId()) + 1;
        long globalSeq = globalSequence.incrementAndGet();
        
        StoredEvent stored = new StoredEvent(
            globalSeq, event.getAggregateId(), event.getEventType(),
            newVersion, event.getData(), event.getMetadata()
        );
        
        aggregateStreams.get(event.getAggregateId()).add(stored);
        globalStream.add(stored);
        
        return stored;
    }
    
    // Query aggregate stream
    public List<StoredEvent> getAggregateStream(String aggregateId) {
        return aggregateStreams.get(aggregateId);
    }
    
    // Query by time range
    public List<StoredEvent> getEventsByTimeRange(LocalDateTime from, LocalDateTime to) {
        return globalStream.stream()
            .filter(e -> e.getTimestamp().isAfter(from) && e.getTimestamp().isBefore(to))
            .collect(Collectors.toList());
    }
}
```

**REST Endpoints**:
- `POST /api/eventstore/append` - Append event to store
- `GET /api/eventstore/stream/{aggregateId}` - Get aggregate stream
- `GET /api/eventstore/stream/{aggregateId}/from/{version}` - Get from version
- `GET /api/eventstore/type/{eventType}` - Get events by type
- `GET /api/eventstore/all` - Get all events
- `GET /api/eventstore/metrics` - View store metrics
- `GET /api/eventstore/projection/{aggregateId}` - Get aggregate projection

**When to Use**:
- Event sourcing implementation
- Complete audit trail required
- Event replay capability
- Temporal queries needed
- Event-driven architecture

---

## 💡 Usage Examples

### Example 1: Simple Event Publishing and Listening

```java
// Define custom event
public class UserRegisteredEvent {
    private final String userId;
    private final String email;
    
    public UserRegisteredEvent(String userId, String email) {
        this.userId = userId;
        this.email = email;
    }
    // getters...
}

// Publisher
@Service
public class UserService {
    private final ApplicationEventPublisher eventPublisher;
    
    public void registerUser(String userId, String email) {
        // Business logic
        User user = createUser(userId, email);
        
        // Publish event
        eventPublisher.publishEvent(new UserRegisteredEvent(userId, email));
    }
}

// Listener
@Component
public class EmailNotificationListener {
    
    @EventListener
    public void sendWelcomeEmail(UserRegisteredEvent event) {
        emailService.sendWelcomeEmail(event.getEmail());
    }
}

@Component
public class AuditLogger {
    
    @EventListener
    public void logUserRegistration(UserRegisteredEvent event) {
        auditLog.log("User registered: " + event.getUserId());
    }
}
```

### Example 2: Conditional and Async Listeners

```java
@Component
public class OrderEventHandlers {
    
    // Only handle large orders
    @EventListener(condition = "#event.amount > 1000")
    public void handleLargeOrder(OrderPlacedEvent event) {
        notifyManagement(event);
    }
    
    // Async processing
    @Async
    @EventListener
    public void processOrderAsync(OrderPlacedEvent event) {
        // Long-running task
        inventoryService.reserve(event.getItems());
    }
    
    // Ordered execution
    @EventListener
    @Order(1)
    public void validateOrder(OrderPlacedEvent event) {
        // First handler
    }
    
    @EventListener
    @Order(2)
    public void processPayment(OrderPlacedEvent event) {
        // Second handler
    }
}
```

### Example 3: Domain Events with Aggregates

```java
// Aggregate Root
public class Order extends AbstractAggregateRoot<Order> {
    private String orderId;
    private List<OrderItem> items;
    private OrderStatus status;
    
    public static Order create(String orderId, String customerId) {
        Order order = new Order();
        order.orderId = orderId;
        order.status = OrderStatus.CREATED;
        
        // Register domain event
        order.registerEvent(new OrderCreatedEvent(orderId, customerId));
        return order;
    }
    
    public void addItem(OrderItem item) {
        this.items.add(item);
        registerEvent(new OrderItemAddedEvent(orderId, item));
    }
    
    public void place() {
        if (items.isEmpty()) {
            throw new IllegalStateException("Cannot place empty order");
        }
        this.status = OrderStatus.PLACED;
        registerEvent(new OrderPlacedEvent(orderId, calculateTotal()));
    }
}

// Service
@Service
public class OrderService {
    private final OrderRepository repository;
    private final ApplicationEventPublisher eventPublisher;
    
    public void placeOrder(Order order) {
        order.place();
        
        // Save and publish domain events
        repository.save(order);
        
        // Domain events automatically published by Spring Data
        order.domainEvents().forEach(eventPublisher::publishEvent);
        order.clearDomainEvents();
    }
}
```

### Example 4: Event Sourcing

```java
// Event-Sourced Aggregate
public class BankAccount {
    private String accountId;
    private double balance;
    private long version;
    private List<DomainEvent> uncommittedEvents = new ArrayList<>();
    
    // Commands
    public void deposit(double amount) {
        if (amount <= 0) throw new IllegalArgumentException();
        applyEvent(new MoneyDepositedEvent(accountId, version + 1, amount));
    }
    
    public void withdraw(double amount) {
        if (amount > balance) throw new InsufficientFundsException();
        applyEvent(new MoneyWithdrawnEvent(accountId, version + 1, amount));
    }
    
    // Apply event (for new events)
    private void applyEvent(DomainEvent event) {
        applyChange(event);
        uncommittedEvents.add(event);
    }
    
    // Apply change to state
    private void applyChange(DomainEvent event) {
        if (event instanceof MoneyDepositedEvent) {
            this.balance += ((MoneyDepositedEvent) event).getAmount();
        } else if (event instanceof MoneyWithdrawnEvent) {
            this.balance -= ((MoneyWithdrawnEvent) event).getAmount();
        }
        this.version = event.getVersion();
    }
    
    // Load from history
    public void loadFromHistory(List<DomainEvent> history) {
        for (DomainEvent event : history) {
            applyChange(event);
        }
    }
}

// Repository
@Component
public class BankAccountRepository {
    private final EventStore eventStore;
    
    public void save(BankAccount account) {
        eventStore.saveEvents(
            account.getAccountId(),
            account.getUncommittedEvents(),
            account.getVersion()
        );
        account.markEventsAsCommitted();
    }
    
    public BankAccount findById(String accountId) {
        BankAccount account = new BankAccount();
        List<DomainEvent> events = eventStore.getEvents(accountId);
        account.loadFromHistory(events);
        return account;
    }
}
```

### Example 5: Reactive Event Streams

```java
@Component
public class RealtimeEventProcessor {
    
    // Hot stream
    private final Sinks.Many<SensorReading> sink = 
        Sinks.many().multicast().onBackpressureBuffer();
    
    public void publishReading(SensorReading reading) {
        sink.tryEmitNext(reading);
    }
    
    // Process stream with transformations
    public Flux<Alert> processReadings() {
        return sink.asFlux()
            // Filter abnormal readings
            .filter(reading -> reading.getValue() > threshold)
            // Transform to alerts
            .map(reading -> new Alert(reading))
            // Buffer for batch processing
            .buffer(Duration.ofSeconds(5))
            // Process batches
            .flatMap(this::processBatch)
            // Handle errors
            .onErrorResume(error -> {
                logger.error("Processing error", error);
                return Flux.empty();
            })
            // Retry on failure
            .retry(3);
    }
    
    // Combine multiple streams
    public Flux<CombinedData> mergeMultipleSensors(
            Flux<SensorReading> sensor1,
            Flux<SensorReading> sensor2) {
        
        return Flux.zip(sensor1, sensor2)
            .map(tuple -> new CombinedData(tuple.getT1(), tuple.getT2()));
    }
}

// SSE endpoint
@RestController
public class StreamController {
    
    @GetMapping(value = "/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<SensorReading> streamReadings() {
        return processor.processReadings()
            .doOnNext(reading -> logger.info("Streaming: " + reading));
    }
}
```

## ✅ Best Practices

### Event Design

1. **Name Events in Past Tense**
   ```java
   // Good
   UserRegisteredEvent
   OrderPlacedEvent
   PaymentProcessedEvent
   
   // Bad
   RegisterUserEvent
   PlaceOrderEvent
   ProcessPaymentEvent
   ```

2. **Make Events Immutable**
   ```java
   public class OrderPlacedEvent {
       private final String orderId;
       private final double amount;
       private final LocalDateTime timestamp;
       
       // Constructor, getters only - no setters
   }
   ```

3. **Include All Necessary Information**
   ```java
   // Good - self-contained
   public class OrderPlacedEvent {
       private final String orderId;
       private final String customerId;
       private final double amount;
       private final List<OrderItem> items;
       private final LocalDateTime placedAt;
   }
   
   // Bad - missing context
   public class OrderPlacedEvent {
       private final String orderId;  // Listener needs to query for details
   }
   ```

4. **Add Event Metadata**
   ```java
   public class BaseEvent {
       private final String eventId = UUID.randomUUID().toString();
       private final LocalDateTime timestamp = LocalDateTime.now();
       private final String correlationId;
       private final String userId;
   }
   ```

### Listener Design

1. **Keep Listeners Focused**
   ```java
   // Good - single responsibility
   @EventListener
   public void sendWelcomeEmail(UserRegisteredEvent event) {
       emailService.sendWelcome(event.getEmail());
   }
   
   // Bad - multiple responsibilities
   @EventListener
   public void handleUserRegistered(UserRegisteredEvent event) {
       emailService.sendWelcome(event.getEmail());
       analyticsService.track(event);
       auditService.log(event);
       // Too many concerns
   }
   ```

2. **Use Async for Long-Running Tasks**
   ```java
   @Async
   @EventListener
   public void generateReport(OrderPlacedEvent event) {
       // Long-running report generation
       reportService.generateOrderReport(event.getOrderId());
   }
   ```

3. **Handle Exceptions Gracefully**
   ```java
   @EventListener
   public void handleOrder(OrderPlacedEvent event) {
       try {
           orderProcessor.process(event);
       } catch (Exception e) {
           logger.error("Failed to process order: " + event.getOrderId(), e);
           // Send to dead letter queue or retry
       }
   }
   ```

### Event Sourcing

1. **Use Snapshots for Performance**
   ```java
   // Take snapshot every 100 events
   if (aggregate.getVersion() % 100 == 0) {
       snapshotStore.save(aggregate);
   }
   
   // Load from snapshot + subsequent events
   Aggregate aggregate = snapshotStore.load(aggregateId);
   List<Event> events = eventStore.getEventsAfterVersion(
       aggregateId, aggregate.getVersion()
   );
   aggregate.applyEvents(events);
   ```

2. **Implement Optimistic Locking**
   ```java
   public void save(Aggregate aggregate) {
       long expectedVersion = aggregate.getVersion() - uncommittedEvents.size();
       
       if (currentVersion != expectedVersion) {
           throw new ConcurrencyException();
       }
       
       eventStore.saveEvents(aggregate.getId(), uncommittedEvents, expectedVersion);
   }
   ```

3. **Version Your Events**
   ```java
   public class OrderPlacedEvent_V2 extends OrderPlacedEvent {
       private final String currency;  // New field
       
       // Include upgrade method from V1
       public static OrderPlacedEvent_V2 from(OrderPlacedEvent_V1 v1) {
           return new OrderPlacedEvent_V2(
               v1.getOrderId(),
               v1.getAmount(),
               "USD"  // Default for old events
           );
       }
   }
   ```

### Reactive Streams

1. **Choose Appropriate Backpressure Strategy**
   ```java
   // Buffer for temporary bursts
   flux.onBackpressureBuffer(1000);
   
   // Drop for real-time data
   flux.onBackpressureDrop();
   
   // Latest for state updates
   flux.onBackpressureLatest();
   ```

2. **Handle Errors Properly**
   ```java
   flux
       .onErrorResume(error -> {
           logger.error("Stream error", error);
           return Flux.empty();  // Continue with empty stream
       })
       .retry(3)  // Retry on failure
       .timeout(Duration.ofSeconds(30));  // Timeout protection
   ```

3. **Use Appropriate Schedulers**
   ```java
   flux
       .publishOn(Schedulers.parallel())  // CPU-intensive work
       .subscribeOn(Schedulers.boundedElastic());  // I/O operations
   ```

## 🧪 Testing

### Unit Testing Events

```java
@SpringBootTest
class EventPublisherTest {
    
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    
    @MockBean
    private OrderEventListener listener;
    
    @Test
    void shouldPublishOrderEvent() {
        // Given
        OrderPlacedEvent event = new OrderPlacedEvent("order-1", 100.0);
        
        // When
        eventPublisher.publishEvent(event);
        
        // Then
        verify(listener, times(1)).handleOrderPlaced(event);
    }
}
```

### Testing Event Listeners

```java
@SpringBootTest
class EventListenerTest {
    
    @Autowired
    private OrderEventListener listener;
    
    @Test
    void shouldHandleOrderEvent() {
        // Given
        OrderPlacedEvent event = new OrderPlacedEvent("order-1", 100.0);
        
        // When
        listener.handleOrderPlaced(event);
        
        // Then
        // Verify behavior
        assertThat(result).isNotNull();
    }
}
```

### Testing Event Sourcing

```java
class BankAccountTest {
    
    @Test
    void shouldReconstructFromEvents() {
        // Given
        List<DomainEvent> events = List.of(
            new AccountCreatedEvent("acc-1", 1, "John", 0),
            new MoneyDepositedEvent("acc-1", 2, 100.0),
            new MoneyWithdrawnEvent("acc-1", 3, 30.0)
        );
        
        // When
        BankAccount account = new BankAccount();
        account.loadFromHistory(events);
        
        // Then
        assertThat(account.getBalance()).isEqualTo(70.0);
        assertThat(account.getVersion()).isEqualTo(3);
    }
}
```

### Testing Reactive Streams

```java
@Test
void shouldProcessEventStream() {
    // Given
    Flux<Event> eventStream = Flux.just(
        new Event("event-1"),
        new Event("event-2"),
        new Event("event-3")
    );
    
    // When
    Flux<ProcessedEvent> processed = processor.process(eventStream);
    
    // Then
    StepVerifier.create(processed)
        .expectNextCount(3)
        .verifyComplete();
}
```

## 🚀 Production Considerations

### Performance

1. **Use Async Processing for Non-Critical Events**
   - Enable `@EnableAsync`
   - Configure thread pool appropriately
   - Monitor thread pool metrics

2. **Implement Caching for Event Replay**
   - Cache frequently accessed event streams
   - Use snapshots for long event histories
   - Implement TTL for cache entries

3. **Batch Event Processing**
   ```java
   flux.buffer(100, Duration.ofSeconds(5))
       .flatMap(this::processBatch);
   ```

### Scalability

1. **Partition Event Streams**
   - Partition by aggregate ID
   - Use consistent hashing
   - Implement partition rebalancing

2. **Use Persistent Event Store**
   - PostgreSQL with JSONB for events
   - Apache Kafka for event streaming
   - EventStore DB for event sourcing

3. **Implement Circuit Breakers**
   ```java
   @CircuitBreaker(name = "eventProcessor")
   @EventListener
   public void processEvent(OrderEvent event) {
       // Processing logic
   }
   ```

### Reliability

1. **Implement Retry Mechanisms**
   ```java
   @Retryable(
       value = {TransientException.class},
       maxAttempts = 3,
       backoff = @Backoff(delay = 1000)
   )
   @EventListener
   public void handleEvent(Event event) {
       // Processing logic
   }
   ```

2. **Use Dead Letter Queue**
   ```java
   @EventListener
   public void handleEvent(Event event) {
       try {
           processEvent(event);
       } catch (Exception e) {
           deadLetterQueue.send(event, e);
       }
   }
   ```

3. **Implement Idempotency**
   ```java
   @EventListener
   public void handleEvent(Event event) {
       if (processedEvents.contains(event.getEventId())) {
           logger.info("Event already processed: " + event.getEventId());
           return;
       }
       
       processEvent(event);
       processedEvents.add(event.getEventId());
   }
   ```

### Monitoring

1. **Add Metrics**
   ```java
   @EventListener
   @Timed(value = "event.processing.time")
   @Counted(value = "event.processed")
   public void handleEvent(Event event) {
       // Processing logic
   }
   ```

2. **Implement Distributed Tracing**
   - Add correlation IDs to events
   - Use Spring Cloud Sleuth
   - Integrate with Zipkin/Jaeger

3. **Monitor Event Lag**
   - Track time between event creation and processing
   - Alert on excessive lag
   - Monitor backpressure signals

## 📚 References

### Spring Documentation
- [Spring Events Documentation](https://docs.spring.io/spring-framework/reference/core/beans/context-introduction.html#context-functionality-events)
- [Spring Data Domain Events](https://docs.spring.io/spring-data/commons/docs/current/reference/html/#core.domain-events)
- [Project Reactor Reference](https://projectreactor.io/docs/core/release/reference/)

### Books
- "Domain-Driven Design" by Eric Evans
- "Implementing Domain-Driven Design" by Vaughn Vernon
- "Event Sourcing and CQRS" by Greg Young

### Articles
- [Event Sourcing Pattern - Microsoft](https://docs.microsoft.com/en-us/azure/architecture/patterns/event-sourcing)
- [CQRS Pattern - Martin Fowler](https://martinfowler.com/bliki/CQRS.html)
- [Reactive Manifesto](https://www.reactivemanifesto.org/)

---

## 📝 Summary

This collection demonstrates **8 comprehensive event-driven patterns** in Spring:

1. ✅ **Event Publisher** - Publishing events with ApplicationEventPublisher
2. ✅ **Event Listener** - Handling events with @EventListener
3. ✅ **Application Event** - Spring's ApplicationEvent hierarchy
4. ✅ **Domain Event** - DDD domain events with aggregates
5. ✅ **Event Bus** - Custom topic-based event routing
6. ✅ **Event Sourcing** - State as sequence of events
7. ✅ **Event Stream** - Reactive streams with Project Reactor
8. ✅ **Event Store** - Append-only event persistence

**Total Implementation**: 4,350+ lines of production-quality code with comprehensive examples, REST endpoints, and best practices.

All patterns include working demonstrations, REST APIs for testing, and detailed documentation for production use.
