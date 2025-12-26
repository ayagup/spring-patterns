package com.example.workflow.statemachine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * State Machine Pattern
 * 
 * Demonstrates:
 * - State machine implementation
 * - State transitions
 * - Transition guards/conditions
 * - State actions/handlers
 * - State history tracking
 * - Complex workflow states
 * 
 * Dependencies:
 * - spring-statemachine-core (optional for production)
 */

@SpringBootApplication
public class StateMachinePattern {
    public static void main(String[] args) {
        SpringApplication.run(StateMachinePattern.class, args);
    }
}

@Configuration
class StateMachineConfig {
    @Bean
    public OrderStateMachine orderStateMachine() {
        return new OrderStateMachine();
    }
}

@RestController
@RequestMapping("/api/statemachine")
class StateMachineController {
    private final StateMachineService service;
    
    public StateMachineController(StateMachineService service) {
        this.service = service;
    }
    
    @PostMapping("/orders")
    public ResponseEntity<OrderEntity> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        return ResponseEntity.ok(service.createOrder(request));
    }
    
    @PostMapping("/orders/{orderId}/transition")
    public ResponseEntity<OrderEntity> transitionOrder(
            @PathVariable String orderId,
            @Valid @RequestBody TransitionRequest request) {
        return service.transitionOrder(orderId, request.getEvent())
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/orders/{orderId}")
    public ResponseEntity<OrderEntity> getOrder(@PathVariable String orderId) {
        return service.getOrder(orderId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/orders/{orderId}/history")
    public ResponseEntity<List<StateTransition>> getHistory(@PathVariable String orderId) {
        return ResponseEntity.ok(service.getHistory(orderId));
    }
}

@Service
class StateMachineService {
    private final OrderStateMachine stateMachine;
    private final Map<String, OrderEntity> orders = new ConcurrentHashMap<>();
    private final Map<String, List<StateTransition>> history = new ConcurrentHashMap<>();
    
    public StateMachineService(OrderStateMachine stateMachine) {
        this.stateMachine = stateMachine;
    }
    
    public OrderEntity createOrder(CreateOrderRequest request) {
        String orderId = UUID.randomUUID().toString();
        OrderEntity order = new OrderEntity(orderId, request.getCustomerName(), 
            request.getAmount(), OrderState.CREATED, LocalDateTime.now());
        
        orders.put(orderId, order);
        recordTransition(orderId, null, OrderState.CREATED, OrderEvent.CREATE, true);
        
        return order;
    }
    
    public Optional<OrderEntity> transitionOrder(String orderId, OrderEvent event) {
        OrderEntity order = orders.get(orderId);
        if (order == null) {
            return Optional.empty();
        }
        
        OrderState currentState = order.getState();
        OrderState newState = stateMachine.transition(currentState, event);
        
        if (newState != currentState) {
            order.setState(newState);
            order.setLastUpdated(LocalDateTime.now());
            recordTransition(orderId, currentState, newState, event, true);
        } else {
            recordTransition(orderId, currentState, newState, event, false);
        }
        
        return Optional.of(order);
    }
    
    public Optional<OrderEntity> getOrder(String orderId) {
        return Optional.ofNullable(orders.get(orderId));
    }
    
    public List<StateTransition> getHistory(String orderId) {
        return history.getOrDefault(orderId, Collections.emptyList());
    }
    
    private void recordTransition(String orderId, OrderState from, OrderState to, 
                                  OrderEvent event, boolean success) {
        StateTransition transition = new StateTransition(from, to, event, success, LocalDateTime.now());
        history.computeIfAbsent(orderId, k -> new ArrayList<>()).add(transition);
    }
}

class OrderStateMachine {
    private final Map<StateEventPair, OrderState> transitions = new HashMap<>();
    
    public OrderStateMachine() {
        // Define valid transitions
        addTransition(OrderState.CREATED, OrderEvent.SUBMIT, OrderState.PENDING);
        addTransition(OrderState.PENDING, OrderEvent.APPROVE, OrderState.APPROVED);
        addTransition(OrderState.PENDING, OrderEvent.REJECT, OrderState.REJECTED);
        addTransition(OrderState.APPROVED, OrderEvent.SHIP, OrderState.SHIPPED);
        addTransition(OrderState.SHIPPED, OrderEvent.DELIVER, OrderState.DELIVERED);
        addTransition(OrderState.DELIVERED, OrderEvent.COMPLETE, OrderState.COMPLETED);
        
        // Cancel transitions from multiple states
        addTransition(OrderState.CREATED, OrderEvent.CANCEL, OrderState.CANCELLED);
        addTransition(OrderState.PENDING, OrderEvent.CANCEL, OrderState.CANCELLED);
        addTransition(OrderState.APPROVED, OrderEvent.CANCEL, OrderState.CANCELLED);
    }
    
    private void addTransition(OrderState from, OrderEvent event, OrderState to) {
        transitions.put(new StateEventPair(from, event), to);
    }
    
    public OrderState transition(OrderState currentState, OrderEvent event) {
        OrderState newState = transitions.get(new StateEventPair(currentState, event));
        return newState != null ? newState : currentState;
    }
    
    public boolean canTransition(OrderState currentState, OrderEvent event) {
        return transitions.containsKey(new StateEventPair(currentState, event));
    }
}

class CreateOrderRequest {
    @NotBlank
    private String customerName;
    private double amount;
    
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
}

class TransitionRequest {
    private OrderEvent event;
    
    public OrderEvent getEvent() { return event; }
    public void setEvent(OrderEvent event) { this.event = event; }
}

class OrderEntity {
    private String id;
    private String customerName;
    private double amount;
    private OrderState state;
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdated;
    
    public OrderEntity(String id, String customerName, double amount, OrderState state, LocalDateTime createdAt) {
        this.id = id;
        this.customerName = customerName;
        this.amount = amount;
        this.state = state;
        this.createdAt = createdAt;
        this.lastUpdated = createdAt;
    }
    
    public String getId() { return id; }
    public String getCustomerName() { return customerName; }
    public double getAmount() { return amount; }
    public OrderState getState() { return state; }
    public void setState(OrderState state) { this.state = state; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
}

class StateTransition {
    private OrderState fromState;
    private OrderState toState;
    private OrderEvent event;
    private boolean success;
    private LocalDateTime timestamp;
    
    public StateTransition(OrderState fromState, OrderState toState, OrderEvent event, 
                          boolean success, LocalDateTime timestamp) {
        this.fromState = fromState;
        this.toState = toState;
        this.event = event;
        this.success = success;
        this.timestamp = timestamp;
    }
    
    public OrderState getFromState() { return fromState; }
    public OrderState getToState() { return toState; }
    public OrderEvent getEvent() { return event; }
    public boolean isSuccess() { return success; }
    public LocalDateTime getTimestamp() { return timestamp; }
}

class StateEventPair {
    private final OrderState state;
    private final OrderEvent event;
    
    public StateEventPair(OrderState state, OrderEvent event) {
        this.state = state;
        this.event = event;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StateEventPair that = (StateEventPair) o;
        return state == that.state && event == that.event;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(state, event);
    }
}

enum OrderState {
    CREATED, PENDING, APPROVED, REJECTED, SHIPPED, DELIVERED, COMPLETED, CANCELLED
}

enum OrderEvent {
    CREATE, SUBMIT, APPROVE, REJECT, SHIP, DELIVER, COMPLETE, CANCEL
}
