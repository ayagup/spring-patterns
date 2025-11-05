# Spring Reactive Patterns

I'll create a comprehensive Spring Boot application demonstrating all 11 reactive patterns using Spring WebFlux.

## Project Structure

```
spring-reactive-patterns/
├── src/main/java/org/example/
│   ├── ReactivePatternsApplication.java
│   ├── config/
│   ├── model/
│   ├── repository/
│   └── patterns/reactive/
│       ├── streams/
│       ├── backpressure/
│       ├── pubsub/
│       ├── mono/
│       ├── flux/
│       ├── repository/
│       ├── handler/
│       ├── functional/
│       ├── router/
│       ├── sse/
│       └── websocket/
├── pom.xml
└── application.properties
```

## 1. Main Application & Configuration

```java
// src/main/java/org/example/ReactivePatternsApplication.java
package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

@SpringBootApplication
@EnableReactiveMongoRepositories
public class ReactivePatternsApplication {
    public static void main(String[] args) {
        SpringApplication.run(ReactivePatternsApplication.class, args);
    }
}
```

## 2. Domain Models

```java
// src/main/java/org/example/model/Product.java
package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "products")
public class Product {
    @Id
    private String id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private String category;
    private LocalDateTime createdAt;
    
    public Product(String name, BigDecimal price, String category) {
        this.name = name;
        this.price = price;
        this.category = category;
        this.createdAt = LocalDateTime.now();
    }
}
```

```java
// src/main/java/org/example/model/User.java
package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {
    @Id
    private String id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private LocalDateTime createdAt;
    
    public User(String username, String email) {
        this.username = username;
        this.email = email;
        this.createdAt = LocalDateTime.now();
    }
}
```

```java
// src/main/java/org/example/model/Message.java
package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    private String id;
    private String sender;
    private String content;
    private LocalDateTime timestamp;
    
    public Message(String sender, String content) {
        this.id = java.util.UUID.randomUUID().toString();
        this.sender = sender;
        this.content = content;
        this.timestamp = LocalDateTime.now();
    }
}
```

## 3. Pattern 1: Reactive Streams Pattern

```java
// src/main/java/org/example/patterns/reactive/streams/ReactiveStreamsService.java
package org.example.patterns.reactive.streams;

import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.Duration;

/**
 * Reactive Streams Pattern.
 * Demonstrates Publisher-Subscriber with Reactive Streams API.
 */
@Slf4j
@Service
public class ReactiveStreamsService {
    
    /**
     * Create a reactive stream publisher.
     */
    public Publisher<Integer> createPublisher() {
        return Flux.range(1, 10)
                .delayElements(Duration.ofMillis(100))
                .doOnNext(i -> log.info("Publisher emitting: {}", i));
    }
    
    /**
     * Custom subscriber implementation.
     */
    public void subscribeWithCustomSubscriber(Publisher<Integer> publisher) {
        publisher.subscribe(new Subscriber<Integer>() {
            private Subscription subscription;
            
            @Override
            public void onSubscribe(Subscription s) {
                log.info("Reactive Streams: Subscribed");
                this.subscription = s;
                s.request(1); // Request one item at a time
            }
            
            @Override
            public void onNext(Integer integer) {
                log.info("Reactive Streams: Received: {}", integer);
                subscription.request(1); // Request next item
            }
            
            @Override
            public void onError(Throwable t) {
                log.error("Reactive Streams: Error", t);
            }
            
            @Override
            public void onComplete() {
                log.info("Reactive Streams: Completed");
            }
        });
    }
    
    /**
     * Demonstrate stream processing.
     */
    public Flux<String> processStream() {
        return Flux.range(1, 20)
                .map(i -> "Item-" + i)
                .filter(s -> Integer.parseInt(s.split("-")[1]) % 2 == 0)
                .doOnNext(s -> log.info("Processing: {}", s));
    }
}
```

```java
// src/main/java/org/example/patterns/reactive/streams/ReactiveStreamsController.java
package org.example.patterns.reactive.streams;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/reactive-streams")
@RequiredArgsConstructor
public class ReactiveStreamsController {
    
    private final ReactiveStreamsService reactiveStreamsService;
    
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamData() {
        return reactiveStreamsService.processStream();
    }
    
    @GetMapping("/demo")
    public Flux<Integer> demoReactiveStreams() {
        return Flux.from(reactiveStreamsService.createPublisher());
    }
}
```

## 4. Pattern 2: Backpressure Pattern

```java
// src/main/java/org/example/patterns/reactive/backpressure/BackpressureService.java
package org.example.patterns.reactive.backpressure;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;

/**
 * Backpressure Pattern.
 * Handles flow control when producer is faster than consumer.
 */
@Slf4j
@Service
public class BackpressureService {
    
    /**
     * Fast producer - demonstrates backpressure.
     */
    public Flux<Integer> createFastProducer() {
        return Flux.range(1, 100)
                .delayElements(Duration.ofMillis(10))
                .doOnNext(i -> log.info("Producing: {}", i));
    }
    
    /**
     * Slow consumer with backpressure handling.
     */
    public Flux<Integer> slowConsumer(Flux<Integer> source) {
        return source
                .onBackpressureBuffer(10) // Buffer up to 10 items
                .publishOn(Schedulers.boundedElastic())
                .doOnNext(i -> {
                    try {
                        Thread.sleep(100); // Slow processing
                        log.info("Consuming: {}", i);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
    }
    
    /**
     * Backpressure with drop strategy.
     */
    public Flux<Integer> backpressureDrop() {
        return Flux.range(1, 1000)
                .delayElements(Duration.ofMillis(1))
                .onBackpressureDrop(i -> log.warn("Dropped: {}", i))
                .publishOn(Schedulers.parallel())
                .doOnNext(i -> {
                    try {
                        Thread.sleep(10);
                        log.info("Processing: {}", i);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
    }
    
    /**
     * Backpressure with latest strategy.
     */
    public Flux<Integer> backpressureLatest() {
        return Flux.range(1, 100)
                .delayElements(Duration.ofMillis(10))
                .onBackpressureLatest()
                .publishOn(Schedulers.parallel())
                .doOnNext(i -> {
                    try {
                        Thread.sleep(50);
                        log.info("Latest: {}", i);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
    }
    
    /**
     * Backpressure with error strategy.
     */
    public Flux<Integer> backpressureError() {
        return Flux.range(1, 100)
                .delayElements(Duration.ofMillis(5))
                .onBackpressureError()
                .doOnNext(i -> log.info("Item: {}", i))
                .doOnError(e -> log.error("Backpressure error: {}", e.getMessage()));
    }
}
```

```java
// src/main/java/org/example/patterns/reactive/backpressure/BackpressureController.java
package org.example.patterns.reactive.backpressure;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/backpressure")
@RequiredArgsConstructor
public class BackpressureController {
    
    private final BackpressureService backpressureService;
    
    @GetMapping(value = "/buffer", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Integer> backpressureBuffer() {
        Flux<Integer> producer = backpressureService.createFastProducer();
        return backpressureService.slowConsumer(producer);
    }
    
    @GetMapping(value = "/drop", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Integer> backpressureDrop() {
        return backpressureService.backpressureDrop();
    }
    
    @GetMapping(value = "/latest", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Integer> backpressureLatest() {
        return backpressureService.backpressureLatest();
    }
}
```

## 5. Pattern 3: Publisher-Subscriber Pattern

```java
// src/main/java/org/example/patterns/reactive/pubsub/EventPublisher.java
package org.example.patterns.reactive.pubsub;

import lombok.extern.slf4j.Slf4j;
import org.example.model.Message;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

/**
 * Publisher-Subscriber Pattern.
 * Implements pub-sub with multiple subscribers.
 */
@Slf4j
@Component
public class EventPublisher {
    
    private final Sinks.Many<Message> sink = Sinks.many().multicast().onBackpressureBuffer();
    
    /**
     * Publish event to all subscribers.
     */
    public void publish(Message message) {
        log.info("Publishing message: {} from {}", message.getContent(), message.getSender());
        sink.tryEmitNext(message);
    }
    
    /**
     * Subscribe to events.
     */
    public Flux<Message> subscribe() {
        return sink.asFlux()
                .doOnSubscribe(s -> log.info("New subscriber"))
                .doOnNext(msg -> log.info("Subscriber received: {}", msg.getContent()));
    }
    
    /**
     * Subscribe with filter.
     */
    public Flux<Message> subscribeFiltered(String senderFilter) {
        return sink.asFlux()
                .filter(msg -> msg.getSender().equals(senderFilter))
                .doOnNext(msg -> log.info("Filtered subscriber received: {}", msg.getContent()));
    }
}
```

```java
// src/main/java/org/example/patterns/reactive/pubsub/PubSubService.java
package org.example.patterns.reactive.pubsub;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.model.Message;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class PubSubService {
    
    private final EventPublisher eventPublisher;
    
    /**
     * Publish messages periodically.
     */
    public void startPublishing() {
        Flux.interval(Duration.ofSeconds(2))
                .map(i -> new Message("System", "Event #" + i))
                .subscribe(eventPublisher::publish);
    }
    
    /**
     * Get subscriber stream.
     */
    public Flux<Message> getMessageStream() {
        return eventPublisher.subscribe();
    }
    
    /**
     * Publish single message.
     */
    public void publishMessage(String sender, String content) {
        eventPublisher.publish(new Message(sender, content));
    }
}
```

```java
// src/main/java/org/example/patterns/reactive/pubsub/PubSubController.java
package org.example.patterns.reactive.pubsub;

import lombok.RequiredArgsConstructor;
import org.example.model.Message;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/pubsub")
@RequiredArgsConstructor
public class PubSubController {
    
    private final PubSubService pubSubService;
    
    @PostMapping("/publish")
    public Mono<Map<String, String>> publish(@RequestBody Message message) {
        pubSubService.publishMessage(message.getSender(), message.getContent());
        return Mono.just(Map.of("status", "published"));
    }
    
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Message> subscribe() {
        return pubSubService.getMessageStream();
    }
}
```

## 6. Pattern 4: Mono Pattern

```java
// src/main/java/org/example/patterns/reactive/mono/MonoService.java
package org.example.patterns.reactive.mono;

import lombok.extern.slf4j.Slf4j;
import org.example.model.User;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Optional;

/**
 * Mono Pattern.
 * Represents 0 or 1 element in reactive stream.
 */
@Slf4j
@Service
public class MonoService {
    
    /**
     * Simple Mono creation.
     */
    public Mono<String> createSimpleMono() {
        return Mono.just("Hello Reactive World")
                .doOnNext(s -> log.info("Mono value: {}", s));
    }
    
    /**
     * Empty Mono.
     */
    public Mono<String> emptyMono() {
        return Mono.empty()
                .doOnSuccess(s -> log.info("Empty Mono completed"));
    }
    
    /**
     * Mono with error.
     */
    public Mono<String> errorMono() {
        return Mono.error(new RuntimeException("Mono error"))
                .doOnError(e -> log.error("Error in Mono: {}", e.getMessage()))
                .cast(String.class);
    }
    
    /**
     * Delayed Mono.
     */
    public Mono<String> delayedMono() {
        return Mono.just("Delayed value")
                .delayElement(Duration.ofSeconds(2))
                .doOnNext(s -> log.info("Delayed Mono: {}", s));
    }
    
    /**
     * Mono from Callable.
     */
    public Mono<User> monoFromCallable() {
        return Mono.fromCallable(() -> {
            log.info("Executing callable");
            Thread.sleep(1000);
            return new User("john.doe", "john@example.com");
        }).doOnNext(user -> log.info("User created: {}", user.getUsername()));
    }
    
    /**
     * Mono transformation.
     */
    public Mono<String> transformMono(String input) {
        return Mono.just(input)
                .map(String::toUpperCase)
                .filter(s -> s.length() > 5)
                .defaultIfEmpty("DEFAULT")
                .doOnNext(s -> log.info("Transformed: {}", s));
    }
    
    /**
     * Mono with flatMap.
     */
    public Mono<User> flatMapExample(String username) {
        return Mono.just(username)
                .flatMap(this::fetchUser)
                .doOnNext(user -> log.info("Fetched user: {}", user.getUsername()));
    }
    
    private Mono<User> fetchUser(String username) {
        return Mono.just(new User(username, username + "@example.com"))
                .delayElement(Duration.ofMillis(500));
    }
    
    /**
     * Mono error handling.
     */
    public Mono<String> errorHandling(boolean shouldError) {
        return Mono.defer(() -> {
            if (shouldError) {
                return Mono.error(new RuntimeException("Simulated error"));
            }
            return Mono.just("Success");
        })
        .onErrorReturn("Error handled")
        .doOnNext(s -> log.info("Result: {}", s));
    }
    
    /**
     * Mono with retry.
     */
    public Mono<String> monoWithRetry() {
        return Mono.fromCallable(() -> {
            if (Math.random() > 0.5) {
                throw new RuntimeException("Random failure");
            }
            return "Success after retry";
        })
        .retry(3)
        .doOnNext(s -> log.info("Retry result: {}", s))
        .onErrorReturn("Failed after retries");
    }
}
```

```java
// src/main/java/org/example/patterns/reactive/mono/MonoController.java
package org.example.patterns.reactive.mono;

import lombok.RequiredArgsConstructor;
import org.example.model.User;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/mono")
@RequiredArgsConstructor
public class MonoController {
    
    private final MonoService monoService;
    
    @GetMapping("/simple")
    public Mono<String> simple() {
        return monoService.createSimpleMono();
    }
    
    @GetMapping("/empty")
    public Mono<String> empty() {
        return monoService.emptyMono();
    }
    
    @GetMapping("/delayed")
    public Mono<String> delayed() {
        return monoService.delayedMono();
    }
    
    @GetMapping("/user")
    public Mono<User> user() {
        return monoService.monoFromCallable();
    }
    
    @GetMapping("/transform/{input}")
    public Mono<String> transform(@PathVariable String input) {
        return monoService.transformMono(input);
    }
    
    @GetMapping("/flatmap/{username}")
    public Mono<User> flatMap(@PathVariable String username) {
        return monoService.flatMapExample(username);
    }
    
    @GetMapping("/error-handling")
    public Mono<String> errorHandling(@RequestParam(defaultValue = "false") boolean error) {
        return monoService.errorHandling(error);
    }
    
    @GetMapping("/retry")
    public Mono<String> retry() {
        return monoService.monoWithRetry();
    }
}
```

## 7. Pattern 5: Flux Pattern

```java
// src/main/java/org/example/patterns/reactive/flux/FluxService.java
package org.example.patterns.reactive.flux;

import lombok.extern.slf4j.Slf4j;
import org.example.model.Product;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

/**
 * Flux Pattern.
 * Represents 0 to N elements in reactive stream.
 */
@Slf4j
@Service
public class FluxService {
    
    /**
     * Simple Flux creation.
     */
    public Flux<Integer> createSimpleFlux() {
        return Flux.just(1, 2, 3, 4, 5)
                .doOnNext(i -> log.info("Flux value: {}", i));
    }
    
```java
    /**
     * Flux from collection.
     */
    public Flux<String> fluxFromCollection() {
        List<String> items = Arrays.asList("Apple", "Banana", "Cherry", "Date");
        return Flux.fromIterable(items)
                .doOnNext(item -> log.info("Item: {}", item));
    }
    
    /**
     * Flux range.
     */
    public Flux<Integer> fluxRange() {
        return Flux.range(1, 10)
                .doOnNext(i -> log.info("Range value: {}", i));
    }
    
    /**
     * Infinite Flux with interval.
     */
    public Flux<Long> infiniteFlux() {
        return Flux.interval(Duration.ofSeconds(1))
                .doOnNext(i -> log.info("Tick: {}", i));
    }
    
    /**
     * Flux transformation with map.
     */
    public Flux<String> transformFlux() {
        return Flux.range(1, 5)
                .map(i -> "Item-" + i)
                .doOnNext(s -> log.info("Transformed: {}", s));
    }
    
    /**
     * Flux filtering.
     */
    public Flux<Integer> filterFlux() {
        return Flux.range(1, 20)
                .filter(i -> i % 2 == 0)
                .doOnNext(i -> log.info("Even number: {}", i));
    }
    
    /**
     * Flux with flatMap.
     */
    public Flux<Product> flatMapExample() {
        return Flux.just("Electronics", "Books", "Clothing")
                .flatMap(this::getProductsByCategory)
                .doOnNext(p -> log.info("Product: {}", p.getName()));
    }
    
    private Flux<Product> getProductsByCategory(String category) {
        return Flux.just(
                new Product("Product-" + category + "-1", new BigDecimal("99.99"), category),
                new Product("Product-" + category + "-2", new BigDecimal("149.99"), category)
        ).delayElements(Duration.ofMillis(100));
    }
    
    /**
     * Flux concatenation.
     */
    public Flux<Integer> concatFlux() {
        Flux<Integer> flux1 = Flux.range(1, 5);
        Flux<Integer> flux2 = Flux.range(6, 5);
        
        return Flux.concat(flux1, flux2)
                .doOnNext(i -> log.info("Concat value: {}", i));
    }
    
    /**
     * Flux merge.
     */
    public Flux<String> mergeFlux() {
        Flux<String> flux1 = Flux.just("A", "B", "C")
                .delayElements(Duration.ofMillis(100));
        Flux<String> flux2 = Flux.just("1", "2", "3")
                .delayElements(Duration.ofMillis(150));
        
        return Flux.merge(flux1, flux2)
                .doOnNext(s -> log.info("Merged: {}", s));
    }
    
    /**
     * Flux zip.
     */
    public Flux<String> zipFlux() {
        Flux<String> names = Flux.just("John", "Jane", "Bob");
        Flux<Integer> ages = Flux.just(30, 25, 35);
        
        return Flux.zip(names, ages, (name, age) -> name + " is " + age + " years old")
                .doOnNext(s -> log.info("Zipped: {}", s));
    }
    
    /**
     * Flux error handling.
     */
    public Flux<Integer> errorHandling() {
        return Flux.range(1, 10)
                .map(i -> {
                    if (i == 5) {
                        throw new RuntimeException("Error at 5");
                    }
                    return i;
                })
                .onErrorResume(e -> {
                    log.error("Error occurred: {}", e.getMessage());
                    return Flux.just(100, 200);
                })
                .doOnNext(i -> log.info("Result: {}", i));
    }
    
    /**
     * Flux buffer.
     */
    public Flux<List<Integer>> bufferFlux() {
        return Flux.range(1, 10)
                .buffer(3)
                .doOnNext(list -> log.info("Buffer: {}", list));
    }
    
    /**
     * Flux window.
     */
    public Flux<Flux<Integer>> windowFlux() {
        return Flux.range(1, 10)
                .window(3)
                .doOnNext(window -> 
                    window.collectList().subscribe(list -> log.info("Window: {}", list))
                );
    }
}
```

```java
// src/main/java/org/example/patterns/reactive/flux/FluxController.java
package org.example.patterns.reactive.flux;

import lombok.RequiredArgsConstructor;
import org.example.model.Product;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequestMapping("/api/flux")
@RequiredArgsConstructor
public class FluxController {
    
    private final FluxService fluxService;
    
    @GetMapping("/simple")
    public Flux<Integer> simple() {
        return fluxService.createSimpleFlux();
    }
    
    @GetMapping("/collection")
    public Flux<String> collection() {
        return fluxService.fluxFromCollection();
    }
    
    @GetMapping("/range")
    public Flux<Integer> range() {
        return fluxService.fluxRange();
    }
    
    @GetMapping(value = "/infinite", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Long> infinite() {
        return fluxService.infiniteFlux().take(10);
    }
    
    @GetMapping("/transform")
    public Flux<String> transform() {
        return fluxService.transformFlux();
    }
    
    @GetMapping("/filter")
    public Flux<Integer> filter() {
        return fluxService.filterFlux();
    }
    
    @GetMapping(value = "/flatmap", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<Product> flatMap() {
        return fluxService.flatMapExample();
    }
    
    @GetMapping("/concat")
    public Flux<Integer> concat() {
        return fluxService.concatFlux();
    }
    
    @GetMapping(value = "/merge", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> merge() {
        return fluxService.mergeFlux();
    }
    
    @GetMapping("/zip")
    public Flux<String> zip() {
        return fluxService.zipFlux();
    }
    
    @GetMapping("/error-handling")
    public Flux<Integer> errorHandling() {
        return fluxService.errorHandling();
    }
    
    @GetMapping("/buffer")
    public Flux<List<Integer>> buffer() {
        return fluxService.bufferFlux();
    }
}
```

## 8. Pattern 6: Reactive Repository Pattern

```java
// src/main/java/org/example/patterns/reactive/repository/ReactiveProductRepository.java
package org.example.patterns.reactive.repository;

import org.example.model.Product;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

/**
 * Reactive Repository Pattern.
 * Non-blocking database operations.
 */
@Repository
public interface ReactiveProductRepository extends ReactiveMongoRepository<Product, String> {
    
    Flux<Product> findByCategory(String category);
    
    Flux<Product> findByPriceLessThan(BigDecimal price);
    
    Flux<Product> findByNameContaining(String name);
    
    @Query("{ 'price': { $gte: ?0, $lte: ?1 } }")
    Flux<Product> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice);
    
    Mono<Long> countByCategory(String category);
    
    Mono<Boolean> existsByName(String name);
}
```

```java
// src/main/java/org/example/patterns/reactive/repository/ReactiveUserRepository.java
package org.example.patterns.reactive.repository;

import org.example.model.User;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface ReactiveUserRepository extends ReactiveMongoRepository<User, String> {
    
    Mono<User> findByUsername(String username);
    
    Mono<User> findByEmail(String email);
    
    Mono<Boolean> existsByUsername(String username);
}
```

```java
// src/main/java/org/example/patterns/reactive/repository/ReactiveRepositoryService.java
package org.example.patterns.reactive.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.model.Product;
import org.example.model.User;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReactiveRepositoryService {
    
    private final ReactiveProductRepository productRepository;
    private final ReactiveUserRepository userRepository;
    
    /**
     * Save product reactively.
     */
    public Mono<Product> saveProduct(Product product) {
        log.info("Saving product: {}", product.getName());
        return productRepository.save(product)
                .doOnSuccess(p -> log.info("Product saved with ID: {}", p.getId()));
    }
    
    /**
     * Find product by ID.
     */
    public Mono<Product> findProductById(String id) {
        return productRepository.findById(id)
                .doOnNext(p -> log.info("Found product: {}", p.getName()))
                .switchIfEmpty(Mono.error(new RuntimeException("Product not found")));
    }
    
    /**
     * Find all products.
     */
    public Flux<Product> findAllProducts() {
        return productRepository.findAll()
                .doOnNext(p -> log.info("Product: {}", p.getName()));
    }
    
    /**
     * Find by category.
     */
    public Flux<Product> findByCategory(String category) {
        return productRepository.findByCategory(category)
                .doOnNext(p -> log.info("Category product: {}", p.getName()));
    }
    
    /**
     * Find by price range.
     */
    public Flux<Product> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return productRepository.findByPriceRange(minPrice, maxPrice)
                .doOnNext(p -> log.info("Product in range: {} - ${}", p.getName(), p.getPrice()));
    }
    
    /**
     * Update product.
     */
    public Mono<Product> updateProduct(String id, Product updates) {
        return productRepository.findById(id)
                .flatMap(existing -> {
                    existing.setName(updates.getName());
                    existing.setPrice(updates.getPrice());
                    existing.setStock(updates.getStock());
                    return productRepository.save(existing);
                })
                .doOnSuccess(p -> log.info("Product updated: {}", p.getId()));
    }
    
    /**
     * Delete product.
     */
    public Mono<Void> deleteProduct(String id) {
        return productRepository.deleteById(id)
                .doOnSuccess(v -> log.info("Product deleted: {}", id));
    }
    
    /**
     * Count by category.
     */
    public Mono<Long> countByCategory(String category) {
        return productRepository.countByCategory(category)
                .doOnNext(count -> log.info("Products in category {}: {}", category, count));
    }
    
    /**
     * User operations.
     */
    public Mono<User> saveUser(User user) {
        return userRepository.save(user)
                .doOnSuccess(u -> log.info("User saved: {}", u.getUsername()));
    }
    
    public Mono<User> findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .doOnNext(u -> log.info("Found user: {}", u.getUsername()));
    }
}
```

```java
// src/main/java/org/example/patterns/reactive/repository/ReactiveRepositoryController.java
package org.example.patterns.reactive.repository;

import lombok.RequiredArgsConstructor;
import org.example.model.Product;
import org.example.model.User;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/reactive-repository")
@RequiredArgsConstructor
public class ReactiveRepositoryController {
    
    private final ReactiveRepositoryService repositoryService;
    
    @PostMapping("/products")
    public Mono<Product> createProduct(@RequestBody Product product) {
        product.setCreatedAt(LocalDateTime.now());
        return repositoryService.saveProduct(product);
    }
    
    @GetMapping("/products/{id}")
    public Mono<Product> getProduct(@PathVariable String id) {
        return repositoryService.findProductById(id);
    }
    
    @GetMapping("/products")
    public Flux<Product> getAllProducts() {
        return repositoryService.findAllProducts();
    }
    
    @GetMapping("/products/category/{category}")
    public Flux<Product> getProductsByCategory(@PathVariable String category) {
        return repositoryService.findByCategory(category);
    }
    
    @GetMapping("/products/price-range")
    public Flux<Product> getProductsByPriceRange(@RequestParam BigDecimal min,
                                                 @RequestParam BigDecimal max) {
        return repositoryService.findByPriceRange(min, max);
    }
    
    @PutMapping("/products/{id}")
    public Mono<Product> updateProduct(@PathVariable String id, @RequestBody Product product) {
        return repositoryService.updateProduct(id, product);
    }
    
    @DeleteMapping("/products/{id}")
    public Mono<Void> deleteProduct(@PathVariable String id) {
        return repositoryService.deleteProduct(id);
    }
    
    @PostMapping("/users")
    public Mono<User> createUser(@RequestBody User user) {
        user.setCreatedAt(LocalDateTime.now());
        return repositoryService.saveUser(user);
    }
    
    @GetMapping("/users/{username}")
    public Mono<User> getUser(@PathVariable String username) {
        return repositoryService.findUserByUsername(username);
    }
}
```

## 9. Pattern 7: WebFlux Handler Pattern

```java
// src/main/java/org/example/patterns/reactive/handler/ProductHandler.java
package org.example.patterns.reactive.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.model.Product;
import org.example.patterns.reactive.repository.ReactiveRepositoryService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * WebFlux Handler Pattern.
 * Functional handler for reactive endpoints.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductHandler {
    
    private final ReactiveRepositoryService repositoryService;
    
    /**
     * Get all products handler.
     */
    public Mono<ServerResponse> getAllProducts(ServerRequest request) {
        log.info("Handler: Get all products");
        
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(repositoryService.findAllProducts(), Product.class);
    }
    
    /**
     * Get product by ID handler.
     */
    public Mono<ServerResponse> getProductById(ServerRequest request) {
        String id = request.pathVariable("id");
        log.info("Handler: Get product by ID: {}", id);
        
        return repositoryService.findProductById(id)
                .flatMap(product -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(product))
                .switchIfEmpty(ServerResponse.notFound().build());
    }
    
    /**
     * Create product handler.
     */
    public Mono<ServerResponse> createProduct(ServerRequest request) {
        log.info("Handler: Create product");
        
        return request.bodyToMono(Product.class)
                .doOnNext(p -> p.setCreatedAt(LocalDateTime.now()))
                .flatMap(repositoryService::saveProduct)
                .flatMap(product -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(product));
    }
    
    /**
     * Update product handler.
     */
    public Mono<ServerResponse> updateProduct(ServerRequest request) {
        String id = request.pathVariable("id");
        log.info("Handler: Update product: {}", id);
        
        return request.bodyToMono(Product.class)
                .flatMap(product -> repositoryService.updateProduct(id, product))
                .flatMap(product -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(product))
                .switchIfEmpty(ServerResponse.notFound().build());
    }
    
    /**
     * Delete product handler.
     */
    public Mono<ServerResponse> deleteProduct(ServerRequest request) {
        String id = request.pathVariable("id");
        log.info("Handler: Delete product: {}", id);
        
        return repositoryService.deleteProduct(id)
                .then(ServerResponse.noContent().build());
    }
    
    /**
     * Get products by category handler.
     */
    public Mono<ServerResponse> getProductsByCategory(ServerRequest request) {
        String category = request.pathVariable("category");
        log.info("Handler: Get products by category: {}", category);
        
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(repositoryService.findByCategory(category), Product.class);
    }
    
    /**
     * Stream products handler.
     */
    public Mono<ServerResponse> streamProducts(ServerRequest request) {
        log.info("Handler: Stream products");
        
        return ServerResponse.ok()
                .contentType(MediaType.TEXT_EVENT_STREAM)
                .body(repositoryService.findAllProducts()
                        .delayElements(java.time.Duration.ofSeconds(1)), Product.class);
    }
}
```

## 10. Pattern 8: Functional Endpoint Pattern

```java
// src/main/java/org/example/patterns/reactive/functional/FunctionalEndpointConfig.java
package org.example.patterns.reactive.functional;

import lombok.RequiredArgsConstructor;
import org.example.patterns.reactive.handler.ProductHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.*;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;

/**
 * Functional Endpoint Pattern.
 * Define routes functionally without @Controller annotations.
 */
@Configuration
@RequiredArgsConstructor
public class FunctionalEndpointConfig {
    
    private final ProductHandler productHandler;
    
    @Bean
    public RouterFunction<ServerResponse> productRoutes() {
        return RouterFunctions
                .route(GET("/functional/products").and(accept(MediaType.APPLICATION_JSON)), 
                       productHandler::getAllProducts)
                .andRoute(GET("/functional/products/{id}").and(accept(MediaType.APPLICATION_JSON)), 
                       productHandler::getProductById)
                .andRoute(POST("/functional/products").and(accept(MediaType.APPLICATION_JSON)), 
                       productHandler::createProduct)
                .andRoute(PUT("/functional/products/{id}").and(accept(MediaType.APPLICATION_JSON)), 
                       productHandler::updateProduct)
                .andRoute(DELETE("/functional/products/{id}"), 
                       productHandler::deleteProduct)
                .andRoute(GET("/functional/products/category/{category}").and(accept(MediaType.APPLICATION_JSON)), 
                       productHandler::getProductsByCategory)
                .andRoute(GET("/functional/products/stream").and(accept(MediaType.TEXT_EVENT_STREAM)), 
                       productHandler::streamProducts);
    }
    
    /**
     * Nested routes.
     */
    @Bean
    public RouterFunction<ServerResponse> nestedRoutes() {
        return RouterFunctions.nest(
                path("/functional/api"),
                RouterFunctions
                        .route(GET("/hello"), request -> 
                                ServerResponse.ok().bodyValue("Hello Functional"))
                        .andRoute(GET("/info"), request -> 
                                ServerResponse.ok().bodyValue("Functional Endpoint Info"))
        );
    }
    
    /**
     * Routes with filters.
     */
    @Bean
    public RouterFunction<ServerResponse> filteredRoutes() {
        return RouterFunctions
                .route(GET("/functional/protected"), 
                       request -> ServerResponse.ok().bodyValue("Protected resource"))
                .filter((request, next) -> {
                    // Add custom filter logic
                    return next.handle(request);
                });
    }
}
```

## 11. Pattern 9: Router Function Pattern

```java
// src/main/java/org/example/patterns/reactive/router/CustomRouterConfig.java
package org.example.patterns/reactive.router;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

/**
 * Router Function Pattern.
 * Advanced routing with conditions and transformations.
 */
@Configuration
public class CustomRouterConfig {
    
    @Bean
    public RouterFunction<ServerResponse> advancedRoutes() {
        return RouterFunctions
                // Simple route
                .route(RequestPredicates.GET("/router/hello"),
                       this::helloHandler)
                
                // Route with path variable
                .andRoute(RequestPredicates.GET("/router/greet/{name}"),
                       this::greetHandler)
                
                // Route with query parameter
                .andRoute(RequestPredicates.GET("/router/search")
                       .and(RequestPredicates.queryParam("q", q -> !q.isEmpty())),
                       this::searchHandler)
                
                // Route with header check
                .andRoute(RequestPredicates.GET("/router/secure")
                       .and(RequestPredicates.headers(headers -> 
                               headers.firstHeader("Authorization") != null)),
                       this::secureHandler)
                
                // Stream route
                .andRoute(RequestPredicates.GET("/router/stream")
                       .and(RequestPredicates.accept(MediaType.TEXT_EVENT_STREAM)),
                       this::streamHandler)
                
                // Composed routes
                .andNest(RequestPredicates.path("/router/api"),
                       this::apiRoutes);
    }
    
    private Mono<ServerResponse> helloHandler(ServerRequest request) {
        return ServerResponse.ok().bodyValue(Map.of("message", "Hello from Router"));
    }
    
    private Mono<ServerResponse> greetHandler(ServerRequest request) {
        String name = request.pathVariable("name");
        return ServerResponse.ok().bodyValue(Map.of("greeting", "Hello, " + name));
    }
    
    private Mono<ServerResponse> searchHandler(ServerRequest request) {
        String query = request.queryParam("q").orElse("");
        return ServerResponse.ok().bodyValue(Map.of("query", query, "results", 10));
    }
    
    private Mono<ServerResponse> secureHandler(ServerRequest request) {
        return ServerResponse.ok().bodyValue(Map.of("message", "Secured resource accessed"));
    }
    
    private Mono<ServerResponse> streamHandler(ServerRequest request) {
        Flux<Long> stream = Flux.interval(Duration.ofSeconds(1)).take(10);
        return ServerResponse.ok()
                .contentType(MediaType.TEXT_EVENT_STREAM)
                .body(stream, Long.class);
    }
    
    private RouterFunction<ServerResponse> apiRoutes() {
        return RouterFunctions
                .route(RequestPredicates.GET("/users"), 
                       request -> ServerResponse.ok().bodyValue("Users list"))
                .andRoute(RequestPredicates.GET("/products"), 
                       request -> ServerResponse.ok().bodyValue("Products list"));
    }
}
```

## 12. Pattern 10: Server-Sent Events Pattern

```java
// src/main/java/org/example/patterns/reactive/sse/SseService.java
package org.example.patterns.reactive.sse;

import lombok.extern.slf4j.Slf4j;
import org.example.model.Message;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Server-Sent Events Pattern.
 * One-way communication from server to client.
 */
@Slf4j
@Service
public class SseService {
    
    /**
     * Simple SSE stream.
     */
    public Flux<String> simpleStream() {
        return Flux.interval(Duration.ofSeconds(1))
                .map(i -> "Event #" + i + " at " + LocalDateTime.now())
                .doOnNext(event -> log.info("SSE: {}", event));
    }
    
    /**
     * SSE with heartbeat.
     */
    public Flux<String> streamWithHeartbeat() {
        return Flux.merge(
                Flux.interval(Duration.ofSeconds(2))
                        .map(i -> "Data event #" + i),
                Flux.interval(Duration.ofSeconds(5))
                        .map(i -> "heartbeat")
        ).doOnNext(event -> log.info("SSE with heartbeat: {}", event));
    }
    
    /**
     * Message stream.
     */
    public Flux<Message> messageStream() {
        return Flux.interval(Duration.ofSeconds(1))
                .map(i -> new Message(
                        UUID.randomUUID().toString(),
                        "System",
                        "Message #" + i,
                        LocalDateTime.now()
                ))
                .doOnNext(msg -> log.info("SSE Message: {}", msg.getContent()));
    }
    
    /**
     * Finite SSE stream.
     */
    public Flux<String> finiteStream() {
        return Flux.range(1, 10)
                .delayElements(Duration.ofMillis(500))
                .map(i -> "Item " + i)
                .doOnNext(item -> log.info("Finite SSE: {}", item))
                .doOnComplete(() -> log.info("Finite stream completed"));
    }
}
```

```java
// src/main/java/org/example/patterns/reactive/sse/SseController.java
package org.example.patterns.reactive.sse;

import lombok.RequiredArgsConstructor;
import org.example.model.Message;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;

@RestController
@RequestMapping("/api/sse")
@RequiredArgsConstructor
public class SseController {
    
    private final SseService sseService;
    
    /**
     * Simple SSE endpoint.
     */
    @GetMapping(value = "/simple", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> simpleStream() {
        return sseService.simpleStream();
    }
    
    /**
     * SSE with ServerSentEvent wrapper.
     */
    @GetMapping(value = "/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> eventStream() {
        return sseService.simpleStream()
                .map(data -> ServerSentEvent.<String>builder()
                        .id(String.valueOf(System.currentTimeMillis()))
                        .event("message")
                        .data(data)
                        .retry(Duration.ofSeconds(5))
                        .build());
    }
    
    /**
     * Message stream.
     */
    @GetMapping(value = "/messages", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Message> messageStream() {
        return sseService.messageStream();
    }
    
    /**
     * Finite stream.
     */
    @GetMapping(value = "/finite", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> finiteStream() {
        return sseService.finiteStream();
    }
    
    /**
     * Heartbeat stream.
     */
    @GetMapping(value = "/heartbeat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> heartbeatStream() {
        return sseService.streamWithHeartbeat();
    }
}
```

## 13. Pattern 11: WebSocket Pattern

```java
// src/main/java/org/example/patterns/reactive/websocket/ReactiveWebSocketHandler.java
package org.example.patterns.reactive.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.model.Message;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.Duration;

/**
 * WebSocket Pattern.
 * Bi-directional communication over WebSocket.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReactiveWebSocketHandler implements WebSocketHandler {
    
    private final ObjectMapper objectMapper;
    private final Sinks.Many<Message> messageSink = Sinks.many().multicast().onBackpressureBuffer();
    
    @Override
    public Mono<Void> handle(WebSocketSession session) {
        log.info("WebSocket connection established: {}", session.getId());
        
        // Receive messages from client
        Flux<Message> receiveFlux = session.receive()
                .map(WebSocketMessage::getPayloadAsText)
                .map(this::parseMessage)
                .doOnNext(msg -> {
                    log.info("Received message: {} from {}", msg.getContent(), msg.getSender());
                    messageSink.tryEmitNext(msg);
                });
        
        // Send messages to client
        Flux<WebSocketMessage> sendFlux = messageSink.asFlux()
                .map(this::messageToJson)
                .map(session::textMessage);
        
        // Combine receive and send
        return session.send(sendFlux)
                .and(receiveFlux);
    }
    
    private Message parseMessage(String json) {
        try {
            return objectMapper.readValue(json, Message.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse message", e);
            return new Message("system", "Error parsing message");
        }
    }
    
    private String messageToJson(Message message) {
        try {
            return objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize message", e);
            return "{}";
        }
    }
}
```

```java
// src/main/java/org/example/patterns/reactive/websocket/EchoWebSocketHandler.java
package org.example.patterns.reactive.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

/**
 * Simple echo WebSocket handler.
 */
@Slf4j
@Component
public class EchoWebSocketHandler implements WebSocketHandler {
    
    @Override
    public Mono<Void> handle(WebSocketSession session) {
        log.info("Echo WebSocket connected: {}", session.getId());
        
        return session.send(
                session.receive()
                        .map(WebSocketMessage::getPayloadAsText)
                        .doOnNext(msg -> log.info("Echo received: {}", msg))
                        .map(msg -> "Echo: " + msg)
                        .map(session::textMessage)
        );
    }
}
```

```java
// src/main/java/org/example/patterns/reactive/websocket/StreamWebSocketHandler.java
package org.example.patterns.reactive.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Streaming WebSocket handler.
 */
@Slf4j
@Component
public class StreamWebSocketHandler implements WebSocketHandler {
    
    @Override
    public Mono<Void> handle(WebSocketSession session) {
        log.info("Stream WebSocket connected: {}", session.getId());
        
        Flux<String> stream = Flux.interval(Duration.ofSeconds(1))
                .map(i -> "Stream event #" + i + " at " + LocalDateTime.now())
                .doOnNext(msg -> log.info("Streaming: {}", msg));
        
        return session.send(
                stream.map(session::textMessage)
        );
    }
}
```

```java
// src/main/java/org/example/patterns/reactive/websocket/WebSocketConfig.java
package org.example.patterns.reactive.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;

import java.util.HashMap;
import java.util.Map;

/**
 * WebSocket configuration.
 */
@Configuration
@RequiredArgsConstructor
public class WebSocketConfig {
    
    private final ReactiveWebSocketHandler reactiveWebSocketHandler;
    private final EchoWebSocketHandler echoWebSocketHandler;
    private final StreamWebSocketHandler streamWebSocketHandler;
    
    @Bean
    public HandlerMapping webSocketHandlerMapping() {
        Map<String, WebSocketHandler> map = new HashMap<>();
        map.put("/ws/chat", reactiveWebSocketHandler);
        map.put("/ws/echo", echoWebSocketHandler);
        map.put("/ws/stream", streamWebSocketHandler);
        
        SimpleUrlHandlerMapping handlerMapping = new SimpleUrlHandlerMapping();
        handlerMapping.setOrder(1);
        handlerMapping.setUrlMap(map);
        
        return handlerMapping;
    }
    
    @Bean
    public WebSocketHandlerAdapter handlerAdapter() {
        return new WebSocketHandlerAdapter();
    }
}
```

## 14. Maven Configuration (pom.xml)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
        <relativePath/>
    </parent>
    
    <groupId>org.example</groupId>
    <artifactId>spring-reactive-patterns</artifactId>
    <version>1.0-SNAPSHOT</version>
    <name>Spring Reactive Patterns</name>
    <description>Demonstration of reactive patterns in Spring</description>
    
    <properties>
        <java.version>17</java.version>
    </properties>
    
    <dependencies>
        <!-- Spring WebFlux -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-webflux</artifactId>
        </dependency>
        
        <!-- Spring Data Reactive MongoDB -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-mongodb-reactive</artifactId>
        </dependency>
        
        <!-- Embedded MongoDB for testing -->
        <dependency>
            <groupId>de.flapdoodle.embed</groupId>
            <artifactId>de.flapdoodle.embed.mongo</artifactId>
            <scope>test</scope>
        </dependency>
        
        <!-- Reactor Core -->
        <dependency>
            <groupId>io.projectreactor</groupId>
            <artifactId>reactor-core</artifactId>
        </dependency>
        
        <!-- Jackson -->
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
        </dependency>
        
        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        
        <!-- Spring Boot Test -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        
        <!-- Reactor Test -->
        <dependency>
            <groupId>io.projectreactor</groupId>
            <artifactId>reactor-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

## 15. Application Configuration

```properties
# src/main/resources/application.properties
spring.application.name=spring-reactive-patterns

# Server Configuration
server.port=8080

# MongoDB Configuration
spring.data.mongodb.host=localhost
spring.data.mongodb.port=27017
spring.data.mongodb.database=reactive_patterns
spring.data.mongodb.auto-index-creation=true

# Logging
logging.level.root=INFO
logging.level.org.example=DEBUG
logging.level.org.springframework.data.mongodb=DEBUG
logging.level.reactor.netty=INFO

# WebFlux Configuration
spring.webflux.base-path=/
```

## 16. Test Classes

```java
// src/test/java/org/example/patterns/reactive/mono/MonoTest.java
package org.example.patterns.reactive.mono;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.test.StepVerifier;

@SpringBootTest
class MonoTest {
    
    @Autowired
    private MonoService monoService;
    
    @Test
    void testSimpleMono() {
        StepVerifier.create(monoService.createSimpleMono())
                .expectNext("Hello Reactive World")
                .verifyComplete();
    }
    
    @Test
    void testEmptyMono() {
        StepVerifier.create(monoService.emptyMono())
                .verifyComplete();
    }
    
    @Test
    void testMonoTransform() {
        StepVerifier.create(monoService.transformMono("hello"))
                .expectNext("HELLO")
                .verifyComplete();
    }
    
    @Test
    void testMonoErrorHandling() {
        StepVerifier.create(monoService.errorHandling(true))
                .expectNext("Error handled")
                .verifyComplete();
    }
}
```

```java
// src/test/java/org/example/patterns/reactive/flux/FluxTest.java
package org.example.patterns.reactive.flux;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.test.StepVerifier;

@SpringBootTest
class FluxTest {
    
    @Autowired
    private FluxService fluxService;
    
    @Test
    void testSimpleFlux() {
        StepVerifier.create(fluxService.createSimpleFlux())
                .expectNext(1, 2, 3, 4, 5)
                .verifyComplete();
    }
    
    @Test
    void testFluxFilter() {
        StepVerifier.create(fluxService.filterFlux())
                .expectNext(2, 4, 6, 8, 10, 12, 14, 16, 18, 20)
                .verifyComplete();
    }
    
    @Test
    void testFluxErrorHandling() {
        StepVerifier.create(fluxService.errorHandling())
                .expectNext(1, 2, 3, 4)
                .expectNext(100, 200)
                .expectNext(6, 7, 8, 9, 10)
                .verifyComplete();
    }
}
```

```java
// src/test/java/org/example/patterns/reactive/backpressure/BackpressureTest.java
package org.example.patterns.reactive.backpressure;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;

@SpringBootTest
class BackpressureTest {
    
    @Autowired
    private BackpressureService backpressureService;
    
    @Test
    void testBackpressureBuffer() {
        Flux<Integer> producer = backpressureService.createFastProducer();
        Flux<Integer> result = backpressureService.slowConsumer(producer);
        
        StepVerifier.create(result.take(10))
                .expectNextCount(10)
                .verifyComplete();
    }
    
    @Test
    void testBackpressureDrop() {
        StepVerifier.create(backpressureService.backpressureDrop().take(20))
                .expectNextCount(20)
                .thenCancel()
                .verify(Duration.ofSeconds(5));
    }
}
```

## 17. README.md

```markdown
# Spring Reactive Patterns

Comprehensive demonstration of 11 reactive programming patterns using Spring WebFlux and Project Reactor.

## Patterns Implemented

### 1. Reactive Streams Pattern
**Endpoint:** `/api/reactive-streams/stream`

Implementation of Reactive Streams specification with Publisher-Subscriber.

**Key Concepts:**
- Publisher: Produces data
- Subscriber: Consumes data
- Subscription: Controls flow
- Processor: Transforms data

**Example:**
```java
Publisher<Integer> publisher = Flux.range(1, 10);
publisher.subscribe(new Subscriber<Integer>() {
    public void onSubscribe(Subscription s) {
        s.request(Long.MAX_VALUE);
    }
    public void onNext(Integer i) { }
    public void onError(Throwable t) { }
    public void onComplete() { }
});
```

### 2. Backpressure Pattern
**Endpoint:** `/api/backpressure/buffer`

Handles flow control when producer is faster than consumer.

**Strategies:**
- **Buffer:** `onBackpressureBuffer()` - Buffer overflow items
- **Drop:** `onBackpressureDrop()` - Drop overflow items
- **Latest:** `onBackpressureLatest()` - Keep only latest
- **Error:** `onBackpressureError()` - Signal error

**Example:**
```java
Flux.range(1, 1000)
    .onBackpressureBuffer(10)  // Buffer 10 items
    .publishOn(Schedulers.parallel())
    .subscribe();
```

### 3. Publisher-Subscriber Pattern
**Endpoint:** `/api/pubsub/subscribe`

Multiple subscribers receive events from single publisher.

**Features:**
- Multicast to multiple subscribers
- Backpressure support
- Hot vs Cold publishers

**Example:**
```java
Sinks.Many<Message> sink = Sinks.many().multicast().onBackpressureBuffer();
sink.tryEmitNext(message);
Flux<Message> flux = sink.asFlux();
```

### 4. Mono Pattern
**Endpoint:** `/api/mono/simple`

Represents 0 or 1 element in reactive stream.

**Operations:**
- `just()` - Create from value
- `empty()` - Empty Mono
- `error()` - Error Mono
- `map()` - Transform
- `flatMap()` - Async transform
- `filter()` - Conditional
- `defaultIfEmpty()` - Fallback

**Example:**
```java
Mono<User> user = Mono.just(new User("john"))
    .map(u -> u.toUpperCase())
    .filter(u -> u.length() > 5)
    .defaultIfEmpty(new User("default"));
```

### 5. Flux Pattern
**Endpoint:** `/api/flux/simple`

Represents 0 to N elements in reactive stream.

**Operations:**
- `just()`, `fromIterable()`, `range()` - Creation
- `map()`, `flatMap()` - Transformation
- `filter()` - Filtering
- `concat()`, `merge()` - Combination
- `zip()` - Pairing
- `buffer()`, `window()` - Grouping

**Example:**
```java
Flux<Integer> flux = Flux.range(1, 10)
    .filter(i -> i % 2 == 0)
    .map(i -> i * 2)
    .buffer(3);
```

### 6. Reactive Repository Pattern
**Endpoint:** `/api/reactive-repository/products`

Non-blocking database operations.

**Features:**
- Extends `ReactiveMongoRepository`
- Returns `Mono<T>` or `Flux<T>`
- Custom queries with `@Query`
- Reactive CRUD operations

**Example:**
```java
public interface ProductRepository extends ReactiveMongoRepository<Product, String> {
    Flux<Product> findByCategory(String category);
    Mono<Long> countByCategory(String category);
}
```

### 7. WebFlux Handler Pattern
**Endpoint:** `/functional/products`

Functional handlers for reactive endpoints.

**Handler Methods:**
```java
@Component
public class ProductHandler {
    public Mono<ServerResponse> getAllProducts(ServerRequest request) {
        return ServerResponse.ok()
            .body(productService.findAll(), Product.class);
    }
}
```

**Advantages:**
- Functional programming style
- No annotations
- Composable
- Testable

### 8. Functional Endpoint Pattern
**Endpoint:** `/functional/api/*`

Define routes functionally without controllers.

**Example:**
```java
@Bean
public RouterFunction<ServerResponse> routes() {
    return RouterFunctions
        .route(GET("/products"), handler::getAllProducts)
        .andRoute(POST("/products"), handler::createProduct);
}
```

### 9. Router Function Pattern
**Endpoint:** `/router/*`

Advanced routing with conditions.

**Features:**
- Path matching
- Query parameter matching
- Header matching
- Nested routes
- Filters

**Example:**
```java
RouterFunctions
    .route(GET("/api/{id}")
        .and(queryParam("type", t -> !t.isEmpty())),
        handler::getById)
    .filter((request, next) -> {
        // Add filter logic
        return next.handle(request);
    });
```

### 10. Server-Sent Events Pattern
**Endpoint:** `/api/sse/simple`

One-way server-to-client streaming.

**Use Cases:**
- Real-time notifications
- Live updates
- Progress tracking
- Stock tickers

**Example:**
```java
@GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public Flux<ServerSentEvent<String>> stream() {
    return Flux.interval(Duration.ofSeconds(1))
        .map(i -> ServerSentEvent.<String>builder()
            .id(String.valueOf(i))
            .event("message")
            .data("Event " + i)
            .build());
}
```

### 11. WebSocket Pattern
**Endpoint:** `ws://localhost:8080/ws/chat`

Bi-directional real-time communication.

**Handlers:**
- `/ws/chat` - Chat messages
- `/ws/echo` - Echo server
- `/ws/stream` - Server streaming

**Example:**
```java
public class ChatHandler implements WebSocketHandler {
    public Mono<Void> handle(WebSocketSession session) {
        return session.send(
            session.receive()
                .map(WebSocketMessage::getPayloadAsText)
                .map(session::textMessage)
        );
    }
}
```

## Running the Application

### Prerequisites
- Java 17+
- Maven 3.6+
- MongoDB (or use embedded for testing)

### Build and Run
```bash
mvn clean install
mvn spring-boot:run
```

### Access Application
Base URL: http://localhost:8080

## Testing Patterns

### Mono Pattern
```bash
# Simple Mono
curl http://localhost:8080/api/mono/simple

# Transform
curl http://localhost:8080/api/mono/transform/hello

# Error handling
curl http://localhost:8080/api/mono/error-handling?error=true
```

### Flux Pattern
```bash
# Simple Flux
curl http://localhost:8080/api/flux/simple

# Filter
curl http://localhost:8080/api/flux/filter

# Stream (SSE)
curl http://localhost:8080/api/flux/infinite
```

### Server-Sent Events
```bash
# Simple stream
curl http://localhost:8080/api/sse/simple

# With events
curl http://localhost:8080/api/sse/events

# Messages
curl http://localhost:8080/api/sse/messages
```

### WebSocket (using wscat)
```bash
# Install wscat
npm install -g wscat

# Connect to chat
wscat -c ws://localhost:8080/ws/chat

# Send message
> {"sender":"user1","content":"Hello"}

# Connect to echo
wscat -c ws://localhost:8080/ws/echo

# Connect to stream
wscat -c ws://localhost:8080/ws/stream
```

### Reactive Repository
```bash
# Create product
curl -X POST http://localhost:8080/api/reactive-repository/products \
  -H "Content-Type: application/json" \
  -d '{"name":"Laptop","price":999.99,"category":"Electronics"}'

# Get all products
curl http://localhost:8080/api/reactive-repository/products

# Get by category
curl http://localhost:8080/api/reactive-repository/products/category/Electronics
```

## Key Concepts

### Reactive Programming Principles
1. **Asynchronous:** Non-blocking operations
2. **Event-driven:** React to events
3. **Backpressure:** Flow control
4. **Resilient:** Error handling

### Mono vs Flux
| Mono | Flux |
|------|------|
| 0 or 1 element | 0 to N elements |
| Like Optional | Like Stream |
| Single async result | Multiple async results |

### Hot vs Cold Publishers
**Cold Publisher:**
- Starts emitting when subscribed
- Each subscriber gets all data
- Example: `Flux.just()`, `Flux.range()`

**Hot Publisher:**
- Emits regardless of subscribers
- Subscribers get data from subscription point
- Example: `Sinks.many().multicast()`

## Best Practices

### 1. Choose Right Type
- Use `Mono<T>` for 0-1 results
- Use `Flux<T>` for 0-N results
- Use `Mono<Void>` for completion signal

### 2. Error Handling
```java
flux.onErrorResume(e -> Flux.empty())
    .onErrorReturn(defaultValue)
    .retry(3)
    .timeout(Duration.ofSeconds(5));
```

### 3. Backpressure
Always consider backpressure:
```java
flux.onBackpressureBuffer(100)
    .publishOn(Schedulers.parallel());
```

### 4. Resource Management
```java
flux.using(
    () -> createResource(),
    resource -> processResource(resource),
    resource -> resource.close()
);
```

### 5. Testing
Use StepVerifier:
```java
StepVerifier.create(mono)
    .expectNext("value")
    .verifyComplete();
```

## Performance Tips

### 1. Threading
```java
flux.publishOn(Schedulers.parallel())
    .subscribeOn(Schedulers.boundedElastic());
```

### 2. Batching
```java
flux.buffer(100)
    .flatMap(batch -> processBatch(batch));
```

### 3. Caching
```java
Mono<Data> cachedMono = expensiveOperation()
    .cache(Duration.ofMinutes(5));
```

## Common Pitfalls

### 1. Blocking Calls
❌ **Wrong:**
```java
Mono<String> result = mono.block(); // Blocks!
```

✅ **Right:**
```java
mono.subscribe(result -> process(result));
```

### 2. Subscribing Multiple Times
Each subscribe() creates new execution.

### 3. Not Handling Errors
Always provide error handling.

### 4. Ignoring Backpressure
Can lead to OutOfMemoryError.

## License

MIT License - free to use for learning and projects.
```

This completes the comprehensive implementation of all 11 Reactive Patterns with working code, tests, and thorough documentation!