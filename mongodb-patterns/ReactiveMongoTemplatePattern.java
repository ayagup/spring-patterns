package com.example.mongodb;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/**
 * Reactive Mongo Template Pattern
 * 
 * Demonstrates reactive MongoDB operations using ReactiveMongoTemplate.
 * 
 * Reactive Features:
 * - Non-blocking database operations
 * - Reactive streams (Mono and Flux)
 * - Backpressure support
 * - Reactive queries
 * - Reactive updates
 * - Reactive aggregations
 * 
 * Publishers:
 * - Mono<T>: 0 or 1 element
 * - Flux<T>: 0 to N elements
 * 
 * Use Cases:
 * - High-concurrency applications
 * - Non-blocking I/O operations
 * - Streaming data
 * - Reactive microservices
 * - Event-driven architectures
 * 
 * @author Spring Patterns
 * @version 1.0
 */
@Configuration
public class ReactiveMongoTemplatePattern {

    @Bean
    public ReactiveOrderService reactiveOrderService(ReactiveMongoTemplate reactiveMongoTemplate) {
        return new ReactiveOrderService(reactiveMongoTemplate);
    }
}

record Order(
    String id,
    String customerId,
    String customerName,
    double totalAmount,
    String status,
    LocalDateTime orderDate,
    LocalDateTime deliveryDate
) {}

@RestController
@RequestMapping("/api/reactive/orders")
class ReactiveOrderService {

    private final ReactiveMongoTemplate mongoTemplate;
    private static final String COLLECTION = "orders";

    public ReactiveOrderService(ReactiveMongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public Mono<Order> save(Order order) {
        return mongoTemplate.save(order, COLLECTION);
    }

    public Mono<Order> findById(String id) {
        return mongoTemplate.findById(id, Order.class, COLLECTION);
    }

    public Flux<Order> findAll() {
        return mongoTemplate.findAll(Order.class, COLLECTION);
    }

    public Flux<Order> findByCustomerId(String customerId) {
        Query query = new Query(Criteria.where("customerId").is(customerId));
        return mongoTemplate.find(query, Order.class, COLLECTION);
    }

    public Flux<Order> findByStatus(String status) {
        Query query = new Query(Criteria.where("status").is(status));
        return mongoTemplate.find(query, Order.class, COLLECTION);
    }

    public Flux<Order> findByAmountRange(double min, double max) {
        Query query = new Query(
            Criteria.where("totalAmount").gte(min).lte(max)
        );
        return mongoTemplate.find(query, Order.class, COLLECTION);
    }

    public Mono<Order> updateStatus(String id, String newStatus) {
        Query query = new Query(Criteria.where("_id").is(id));
        Update update = new Update().set("status", newStatus);
        return mongoTemplate.findAndModify(query, update, Order.class, COLLECTION);
    }

    public Mono<Long> count() {
        return mongoTemplate.count(new Query(), Order.class, COLLECTION);
    }

    public Mono<Long> countByStatus(String status) {
        Query query = new Query(Criteria.where("status").is(status));
        return mongoTemplate.count(query, Order.class, COLLECTION);
    }

    public Mono<Boolean> exists(String id) {
        Query query = new Query(Criteria.where("_id").is(id));
        return mongoTemplate.exists(query, Order.class, COLLECTION);
    }

    public Mono<Void> delete(String id) {
        Query query = new Query(Criteria.where("_id").is(id));
        return mongoTemplate.remove(query, Order.class, COLLECTION).then();
    }
}

@RestController
@RequestMapping("/api/reactive/orders")
class ReactiveOrderController {

    private final ReactiveOrderService orderService;

    public ReactiveOrderController(ReactiveOrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public Mono<ResponseEntity<Order>> createOrder(@RequestBody Order order) {
        return orderService.save(order)
            .map(ResponseEntity::ok);
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Order>> getOrder(@PathVariable String id) {
        return orderService.findById(id)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping
    public Flux<Order> getAllOrders() {
        return orderService.findAll();
    }

    @GetMapping("/customer/{customerId}")
    public Flux<Order> getByCustomer(@PathVariable String customerId) {
        return orderService.findByCustomerId(customerId);
    }

    @GetMapping("/status/{status}")
    public Flux<Order> getByStatus(@PathVariable String status) {
        return orderService.findByStatus(status);
    }

    @GetMapping("/amount-range")
    public Flux<Order> getByAmountRange(
            @RequestParam double min,
            @RequestParam double max) {
        return orderService.findByAmountRange(min, max);
    }

    @PutMapping("/{id}/status")
    public Mono<ResponseEntity<Order>> updateStatus(
            @PathVariable String id,
            @RequestParam String status) {
        return orderService.updateStatus(id, status)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/count")
    public Mono<Long> countOrders() {
        return orderService.count();
    }

    @GetMapping("/count/status/{status}")
    public Mono<Long> countByStatus(@PathVariable String status) {
        return orderService.countByStatus(status);
    }

    @GetMapping("/{id}/exists")
    public Mono<Boolean> orderExists(@PathVariable String id) {
        return orderService.exists(id);
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteOrder(@PathVariable String id) {
        return orderService.delete(id)
            .map(v -> ResponseEntity.noContent().<Void>build());
    }

    @GetMapping("/info")
    public Mono<PatternInfo> getPatternInfo() {
        return Mono.just(new PatternInfo(
            "Reactive Mongo Template Pattern",
            "Non-blocking MongoDB operations using ReactiveMongoTemplate",
            "1.0",
            List.of("Non-blocking", "Reactive streams", "Backpressure", "High concurrency"),
            List.of("Reactive microservices", "Streaming data", "Event-driven", "High-throughput")
        ));
    }

    record PatternInfo(String name, String description, String version, 
                      List<String> features, List<String> useCases) {}
}
