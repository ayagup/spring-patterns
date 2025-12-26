package com.example.cloudfunction;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.function.Function;

/**
 * Reactive Function Pattern
 * ==========================
 * 
 * Demonstrates reactive functions using Project Reactor (Mono/Flux)
 * in Spring Cloud Function for non-blocking, asynchronous processing.
 * 
 * Key Concepts:
 * ------------
 * 1. Mono<T> - 0 or 1 element reactive stream
 * 2. Flux<T> - 0 to N elements reactive stream
 * 3. Non-blocking - Asynchronous processing
 * 4. Backpressure - Flow control
 * 5. Stream Processing - Continuous data flow
 * 
 * How It Works:
 * ------------
 * - Define Function<Mono<T>, Mono<R>> or Function<Flux<T>, Flux<R>>
 * - Spring Cloud Function handles reactive subscriptions
 * - Non-blocking I/O for high throughput
 * - Automatic backpressure management
 * - Perfect for streaming data
 * 
 * Benefits:
 * --------
 * - High scalability
 * - Resource efficiency
 * - Backpressure handling
 * - Composable streams
 * - Event-driven processing
 * 
 * @author Spring Patterns
 * @version 1.0
 */

/**
 * Example 1: Simple Reactive Function (Mono)
 */
@Configuration
class SimpleReactiveFunctionExample {
    
    /**
     * Transform single item reactively
     * 
     * Input: Mono<String>
     * Output: Mono<String>
     */
    @Bean
    public Function<Mono<String>, Mono<String>> reactiveUppercase() {
        return mono -> mono
            .doOnNext(value -> System.out.println("Processing: " + value))
            .map(String::toUpperCase)
            .doOnNext(value -> System.out.println("Result: " + value));
    }
}

/**
 * Example 2: Flux Processing
 */
@Configuration
class FluxProcessingExample {
    
    /**
     * Process stream of items
     * 
     * Input: Flux<String>
     * Output: Flux<String>
     */
    @Bean
    public Function<Flux<String>, Flux<String>> processStream() {
        return flux -> flux
            .doOnNext(item -> System.out.println("Received: " + item))
            .map(String::toUpperCase)
            .filter(item -> item.length() > 3)
            .doOnNext(item -> System.out.println("Filtered: " + item));
    }
}

/**
 * Example 3: Async Transformation
 */
@Configuration
class AsyncTransformationExample {
    
    static class Order {
        private String id;
        private double amount;
        
        public Order(String id, double amount) {
            this.id = id;
            this.amount = amount;
        }
        
        public String getId() { return id; }
        public double getAmount() { return amount; }
    }
    
    static class OrderResult {
        private String orderId;
        private String status;
        private double total;
        
        public OrderResult(String orderId, String status, double total) {
            this.orderId = orderId;
            this.status = status;
            this.total = total;
        }
        
        public String getOrderId() { return orderId; }
        public String getStatus() { return status; }
        public double getTotal() { return total; }
    }
    
    /**
     * Async order processing with enrichment
     */
    @Bean
    public Function<Mono<Order>, Mono<OrderResult>> processOrderAsync() {
        return orderMono -> orderMono
            .doOnNext(order -> System.out.println("Processing order: " + order.getId()))
            .flatMap(order -> enrichOrder(order))
            .map(order -> new OrderResult(
                order.getId(),
                order.getAmount() > 1000 ? "HIGH_VALUE" : "STANDARD",
                order.getAmount() * 1.1
            ));
    }
    
    private Mono<Order> enrichOrder(Order order) {
        // Simulate async enrichment (e.g., database lookup, API call)
        return Mono.just(order)
            .delayElement(java.time.Duration.ofMillis(100))
            .doOnNext(o -> System.out.println("Order enriched: " + o.getId()));
    }
}

/**
 * Example 4: Batching and Windowing
 */
@Configuration
class BatchingWindowingExample {
    
    /**
     * Process items in batches
     */
    @Bean
    public Function<Flux<Integer>, Flux<java.util.List<Integer>>> batchProcessor() {
        return flux -> flux
            .doOnNext(item -> System.out.println("Item: " + item))
            .buffer(5) // Batch size of 5
            .doOnNext(batch -> System.out.println("Processing batch of " + batch.size()));
    }
    
    /**
     * Time-based windowing
     */
    @Bean
    public Function<Flux<String>, Flux<java.util.List<String>>> timeWindowProcessor() {
        return flux -> flux
            .doOnNext(item -> System.out.println("Item: " + item))
            .buffer(java.time.Duration.ofSeconds(1)) // 1-second windows
            .doOnNext(window -> System.out.println("Window size: " + window.size()));
    }
}

/**
 * Example 5: Error Handling
 */
@Configuration
class ReactiveErrorHandlingExample {
    
    /**
     * Handle errors in reactive stream
     */
    @Bean
    public Function<Flux<String>, Flux<String>> processWithErrorHandling() {
        return flux -> flux
            .doOnNext(item -> System.out.println("Processing: " + item))
            .map(item -> {
                if (item.contains("error")) {
                    throw new RuntimeException("Simulated error");
                }
                return item.toUpperCase();
            })
            .onErrorResume(error -> {
                System.err.println("Error occurred: " + error.getMessage());
                return Flux.just("ERROR_HANDLED");
            })
            .doOnNext(item -> System.out.println("Result: " + item));
    }
    
    /**
     * Retry on error
     */
    @Bean
    public Function<Mono<String>, Mono<String>> processWithRetry() {
        return mono -> mono
            .map(String::toUpperCase)
            .retry(3) // Retry up to 3 times
            .doOnError(error -> System.err.println("Failed after retries: " + error));
    }
}

/**
 * Example 6: Filtering and Aggregation
 */
@Configuration
class FilteringAggregationExample {
    
    static class SensorReading {
        private double temperature;
        private long timestamp;
        
        public SensorReading(double temperature, long timestamp) {
            this.temperature = temperature;
            this.timestamp = timestamp;
        }
        
        public double getTemperature() { return temperature; }
        public long getTimestamp() { return timestamp; }
    }
    
    /**
     * Filter and aggregate sensor readings
     */
    @Bean
    public Function<Flux<SensorReading>, Mono<Double>> calculateAverageTemp() {
        return flux -> flux
            .filter(reading -> reading.getTemperature() > 0)
            .map(SensorReading::getTemperature)
            .reduce(0.0, Double::sum)
            .map(sum -> sum / 10); // Assuming 10 readings
    }
    
    /**
     * Alert on high temperature
     */
    @Bean
    public Function<Flux<SensorReading>, Flux<SensorReading>> alertOnHighTemp() {
        return flux -> flux
            .filter(reading -> reading.getTemperature() > 30)
            .doOnNext(reading -> 
                System.out.println("ALERT: High temperature detected: " + 
                    reading.getTemperature() + "°C")
            );
    }
}

/**
 * Example 7: Parallel Processing
 */
@Configuration
class ParallelProcessingExample {
    
    /**
     * Process items in parallel
     */
    @Bean
    public Function<Flux<String>, Flux<String>> parallelProcessor() {
        return flux -> flux
            .parallel(4) // 4 parallel threads
            .runOn(reactor.core.scheduler.Schedulers.parallel())
            .map(item -> {
                System.out.println("Processing " + item + " on " + 
                    Thread.currentThread().getName());
                return item.toUpperCase();
            })
            .sequential();
    }
}

/**
 * Example 8: Debouncing and Throttling
 */
@Configuration
class DebouncingThrottlingExample {
    
    /**
     * Debounce rapid events
     */
    @Bean
    public Function<Flux<String>, Flux<String>> debounceEvents() {
        return flux -> flux
            .debounce(java.time.Duration.ofMillis(300))
            .doOnNext(item -> System.out.println("Debounced: " + item));
    }
    
    /**
     * Throttle event rate
     */
    @Bean
    public Function<Flux<String>, Flux<String>> throttleEvents() {
        return flux -> flux
            .sample(java.time.Duration.ofSeconds(1))
            .doOnNext(item -> System.out.println("Sampled: " + item));
    }
}

/**
 * Example 9: Merge and Combine Streams
 */
@Configuration
class MergeCombineExample {
    
    /**
     * Merge multiple streams
     */
    public Flux<String> mergeStreams(Flux<String> stream1, Flux<String> stream2) {
        return Flux.merge(stream1, stream2)
            .doOnNext(item -> System.out.println("Merged item: " + item));
    }
    
    /**
     * Combine latest values
     */
    public Flux<String> combineStreams(Flux<String> stream1, Flux<Integer> stream2) {
        return Flux.combineLatest(
            stream1,
            stream2,
            (str, num) -> str + ": " + num
        ).doOnNext(combined -> System.out.println("Combined: " + combined));
    }
    
    /**
     * Zip streams together
     */
    public Flux<String> zipStreams(Flux<String> stream1, Flux<String> stream2) {
        return Flux.zip(
            stream1,
            stream2,
            (a, b) -> a + " + " + b
        ).doOnNext(zipped -> System.out.println("Zipped: " + zipped));
    }
}

/**
 * Example 10: Stateful Processing
 */
@Configuration
class StatefulProcessingExample {
    
    /**
     * Running count
     */
    @Bean
    public Function<Flux<String>, Flux<String>> runningCount() {
        return flux -> flux
            .index() // Add index to each element
            .map(tuple -> {
                long index = tuple.getT1();
                String value = tuple.getT2();
                return "[" + index + "] " + value;
            })
            .doOnNext(item -> System.out.println("Indexed: " + item));
    }
    
    /**
     * Running total
     */
    @Bean
    public Function<Flux<Integer>, Flux<Integer>> runningTotal() {
        return flux -> flux
            .scan(0, Integer::sum) // Accumulate running sum
            .doOnNext(sum -> System.out.println("Running total: " + sum));
    }
    
    /**
     * Distinct elements
     */
    @Bean
    public Function<Flux<String>, Flux<String>> distinctElements() {
        return flux -> flux
            .distinct()
            .doOnNext(item -> System.out.println("Distinct: " + item));
    }
}

/**
 * Main Pattern Class
 */
@Configuration
public class ReactiveFunctionPattern {
    
    /**
     * Core Reactive Function demonstration
     */
    public void demonstrateReactiveFunction() {
        System.out.println("\n=== Reactive Function Pattern ===");
        System.out.println("Non-blocking asynchronous processing");
        System.out.println("\nKey Features:");
        System.out.println("  - Mono<T> for 0-1 elements");
        System.out.println("  - Flux<T> for 0-N elements");
        System.out.println("  - Backpressure support");
        System.out.println("  - Non-blocking I/O");
        System.out.println("  - Stream composition");
        System.out.println("\nOperators:");
        System.out.println("  - map, filter, flatMap");
        System.out.println("  - buffer, window");
        System.out.println("  - merge, zip, combine");
        System.out.println("  - retry, timeout");
        System.out.println("  - debounce, sample");
    }
}

/**
 * Usage Examples
 */
class ReactiveFunctionUsageExamples {
    
    public static void main(String[] args) {
        System.out.println("Reactive Function Pattern Usage");
        System.out.println("===============================\n");
        
        System.out.println("1. Mono Function:");
        System.out.println("@Bean");
        System.out.println("public Function<Mono<String>, Mono<String>> process() {");
        System.out.println("    return mono -> mono.map(String::toUpperCase);");
        System.out.println("}\n");
        
        System.out.println("2. Flux Function:");
        System.out.println("@Bean");
        System.out.println("public Function<Flux<String>, Flux<String>> processStream() {");
        System.out.println("    return flux -> flux");
        System.out.println("        .map(String::toUpperCase)");
        System.out.println("        .filter(s -> s.length() > 3);");
        System.out.println("}\n");
        
        System.out.println("3. Error Handling:");
        System.out.println("flux");
        System.out.println("    .map(this::process)");
        System.out.println("    .onErrorResume(e -> Flux.empty())");
        System.out.println("    .retry(3);\n");
        
        System.out.println("Benefits:");
        System.out.println("- High throughput");
        System.out.println("- Resource efficient");
        System.out.println("- Backpressure handling");
        System.out.println("- Non-blocking I/O");
        System.out.println("- Stream composition");
    }
}
