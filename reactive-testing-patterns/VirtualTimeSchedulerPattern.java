package com.example.reactive.testing;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.scheduler.VirtualTimeScheduler;
import java.time.Duration;

/**
 * Virtual Time Scheduler Pattern - Testing Time-based Reactive Operators
 * 
 * VirtualTimeScheduler allows testing of time-based reactive operators without
 * actually waiting for real time to pass. It provides a way to manipulate time
 * during tests, making time-dependent tests fast and deterministic.
 * 
 * Key Concepts:
 * - Virtual Time: Simulated time that can be advanced programmatically
 * - Time Manipulation: Control time progression in tests
 * - Deterministic: Same test always takes same virtual time
 * - Fast Execution: Tests complete instantly regardless of delays
 * - Time-based Operators: delay, timeout, interval, window, buffer
 * 
 * How It Works:
 * 1. Enable virtual time with VirtualTimeScheduler
 * 2. Create reactive stream with time-based operators
 * 3. Advance virtual time as needed
 * 4. Verify expected behavior
 * 
 * StepVerifier Integration:
 * - StepVerifier.withVirtualTime(): Create with virtual time
 * - thenAwait(Duration): Advance virtual time
 * - expectNoEvent(Duration): Assert no events for duration
 * - expectSubscription(): Expect initial subscription
 * 
 * Time Advancement Methods:
 * - advanceTimeBy(Duration): Advance by specific duration
 * - advanceTimeTo(Instant): Advance to specific instant
 * - advanceTime(): Advance to next scheduled task
 * 
 * Use Cases:
 * - Testing delay operators
 * - Testing timeout scenarios
 * - Testing interval/ticker operators
 * - Testing window operations
 * - Testing buffer with time
 * - Testing debounce/throttle
 * - Testing retry with backoff
 * 
 * Best Practices:
 * - Use withVirtualTime for time-based tests
 * - Always call expectSubscription() first
 * - Use expectNoEvent to verify silence
 * - Advance time progressively
 * - Test edge cases around time boundaries
 * - Verify cleanup after time advancement
 */
public class VirtualTimeSchedulerPattern {

    // Service with time-based operations
    static class TimeBasedService {
        
        public Mono<String> delayedResponse() {
            return Mono.just("Response")
                    .delayElement(Duration.ofHours(1));
        }
        
        public Flux<Long> ticker(int count) {
            return Flux.interval(Duration.ofMinutes(10))
                    .take(count);
        }
        
        public Mono<String> withTimeout() {
            return Mono.delay(Duration.ofSeconds(30))
                    .thenReturn("Completed")
                    .timeout(Duration.ofSeconds(20));
        }
        
        public Flux<String> bufferedMessages() {
            return Flux.interval(Duration.ofSeconds(1))
                    .map(i -> "Message-" + i)
                    .buffer(Duration.ofSeconds(5))
                    .flatMap(Flux::fromIterable)
                    .take(10);
        }
        
        public Flux<Long> windowedData() {
            return Flux.interval(Duration.ofSeconds(2))
                    .window(Duration.ofSeconds(10))
                    .flatMap(window -> window.count())
                    .take(3);
        }
        
        public Mono<String> retryWithBackoff() {
            return Mono.<String>error(new RuntimeException("Error"))
                    .retryWhen(reactor.util.retry.Retry.fixedDelay(3, Duration.ofSeconds(10)))
                    .timeout(Duration.ofMinutes(1));
        }
        
        public Flux<String> debounced() {
            return Flux.just("A", "B", "C", "D", "E")
                    .delayElements(Duration.ofMillis(100))
                    .debounce(Duration.ofMillis(50));
        }
        
        public Flux<Long> sample() {
            return Flux.interval(Duration.ofMillis(10))
                    .sample(Duration.ofMillis(100))
                    .take(5);
        }
    }

    // Test examples
    static class VirtualTimeTests {
        
        private final TimeBasedService service = new TimeBasedService();
        
        // Basic virtual time test
        public void testDelayedResponse() {
            StepVerifier.withVirtualTime(() -> service.delayedResponse())
                    .expectSubscription()
                    .expectNoEvent(Duration.ofMinutes(59))
                    .thenAwait(Duration.ofMinutes(1))
                    .expectNext("Response")
                    .verifyComplete();
        }
        
        // Testing interval
        public void testTicker() {
            StepVerifier.withVirtualTime(() -> service.ticker(3))
                    .expectSubscription()
                    .expectNoEvent(Duration.ofMinutes(9))
                    .thenAwait(Duration.ofMinutes(1))
                    .expectNext(0L)
                    .thenAwait(Duration.ofMinutes(10))
                    .expectNext(1L)
                    .thenAwait(Duration.ofMinutes(10))
                    .expectNext(2L)
                    .verifyComplete();
        }
        
        // Testing timeout
        public void testTimeout() {
            StepVerifier.withVirtualTime(() -> service.withTimeout())
                    .expectSubscription()
                    .thenAwait(Duration.ofSeconds(20))
                    .expectError(java.util.concurrent.TimeoutException.class)
                    .verify();
        }
        
        // Testing buffer with time
        public void testBufferedMessages() {
            StepVerifier.withVirtualTime(() -> service.bufferedMessages())
                    .expectSubscription()
                    .thenAwait(Duration.ofSeconds(10))
                    .expectNextCount(10)
                    .verifyComplete();
        }
        
        // Testing window operations
        public void testWindowedData() {
            StepVerifier.withVirtualTime(() -> service.windowedData())
                    .expectSubscription()
                    .thenAwait(Duration.ofSeconds(10))
                    .expectNext(5L)  // 5 elements in first 10-second window
                    .thenAwait(Duration.ofSeconds(10))
                    .expectNext(5L)  // 5 elements in second window
                    .thenAwait(Duration.ofSeconds(10))
                    .expectNext(5L)  // 5 elements in third window
                    .verifyComplete();
        }
        
        // Testing progressive time advancement
        public void testProgressiveAdvancement() {
            Flux<Long> flux = Flux.interval(Duration.ofSeconds(1))
                    .take(5);
            
            StepVerifier.withVirtualTime(() -> flux)
                    .expectSubscription()
                    .expectNoEvent(Duration.ofMillis(999))
                    .thenAwait(Duration.ofMillis(1))
                    .expectNext(0L)
                    .thenAwait(Duration.ofSeconds(1))
                    .expectNext(1L)
                    .thenAwait(Duration.ofSeconds(1))
                    .expectNext(2L)
                    .thenAwait(Duration.ofSeconds(1))
                    .expectNext(3L)
                    .thenAwait(Duration.ofSeconds(1))
                    .expectNext(4L)
                    .verifyComplete();
        }
        
        // Testing debounce
        public void testDebounced() {
            StepVerifier.withVirtualTime(() -> service.debounced())
                    .expectSubscription()
                    .thenAwait(Duration.ofMillis(500))
                    .expectNext("E")  // Only last element within debounce window
                    .verifyComplete();
        }
        
        // Testing sample
        public void testSample() {
            StepVerifier.withVirtualTime(() -> service.sample())
                    .expectSubscription()
                    .thenAwait(Duration.ofSeconds(1))
                    .expectNextCount(5)
                    .verifyComplete();
        }
        
        // Testing multiple delays
        public void testMultipleDelays() {
            Flux<String> flux = Flux.just("A", "B", "C")
                    .delayElements(Duration.ofHours(1));
            
            StepVerifier.withVirtualTime(() -> flux)
                    .expectSubscription()
                    .thenAwait(Duration.ofHours(1))
                    .expectNext("A")
                    .thenAwait(Duration.ofHours(1))
                    .expectNext("B")
                    .thenAwait(Duration.ofHours(1))
                    .expectNext("C")
                    .verifyComplete();
        }
        
        // Testing expectNoEvent
        public void testExpectNoEvent() {
            Mono<String> mono = Mono.delay(Duration.ofDays(1))
                    .thenReturn("After a day");
            
            StepVerifier.withVirtualTime(() -> mono)
                    .expectSubscription()
                    .expectNoEvent(Duration.ofHours(23))
                    .thenAwait(Duration.ofHours(1))
                    .expectNext("After a day")
                    .verifyComplete();
        }
        
        // Testing combined time operations
        public void testCombinedTimeOperations() {
            Flux<String> flux = Flux.interval(Duration.ofSeconds(5))
                    .map(i -> "Item-" + i)
                    .timeout(Duration.ofSeconds(30))
                    .take(5);
            
            StepVerifier.withVirtualTime(() -> flux)
                    .expectSubscription()
                    .thenAwait(Duration.ofSeconds(5))
                    .expectNext("Item-0")
                    .thenAwait(Duration.ofSeconds(5))
                    .expectNext("Item-1")
                    .thenAwait(Duration.ofSeconds(5))
                    .expectNext("Item-2")
                    .thenAwait(Duration.ofSeconds(5))
                    .expectNext("Item-3")
                    .thenAwait(Duration.ofSeconds(5))
                    .expectNext("Item-4")
                    .verifyComplete();
        }
        
        // Testing delay subscription
        public void testDelaySubscription() {
            Flux<String> flux = Flux.just("Immediate")
                    .delaySubscription(Duration.ofMinutes(30));
            
            StepVerifier.withVirtualTime(() -> flux)
                    .expectSubscription()
                    .expectNoEvent(Duration.ofMinutes(29))
                    .thenAwait(Duration.ofMinutes(1))
                    .expectNext("Immediate")
                    .verifyComplete();
        }
        
        // Testing elapsed time
        public void testElapsed() {
            Flux<String> flux = Flux.just("A", "B", "C")
                    .delayElements(Duration.ofSeconds(10))
                    .elapsed()
                    .map(tuple -> tuple.getT2());  // Get value, ignore time
            
            StepVerifier.withVirtualTime(() -> flux)
                    .expectSubscription()
                    .thenAwait(Duration.ofSeconds(10))
                    .expectNext("A")
                    .thenAwait(Duration.ofSeconds(10))
                    .expectNext("B")
                    .thenAwait(Duration.ofSeconds(10))
                    .expectNext("C")
                    .verifyComplete();
        }
        
        // Testing take with time
        public void testTakeWithTime() {
            Flux<Long> flux = Flux.interval(Duration.ofSeconds(1))
                    .take(Duration.ofSeconds(5));
            
            StepVerifier.withVirtualTime(() -> flux)
                    .expectSubscription()
                    .thenAwait(Duration.ofSeconds(5))
                    .expectNextCount(5)
                    .verifyComplete();
        }
        
        // Testing skip with time
        public void testSkipWithTime() {
            Flux<Long> flux = Flux.interval(Duration.ofSeconds(1))
                    .skip(Duration.ofSeconds(3))
                    .take(3);
            
            StepVerifier.withVirtualTime(() -> flux)
                    .expectSubscription()
                    .thenAwait(Duration.ofSeconds(6))
                    .expectNext(3L, 4L, 5L)
                    .verifyComplete();
        }
    }

    public static void main(String[] args) {
        System.out.println("Virtual Time Scheduler Pattern - Testing Time-based Operators");
        System.out.println("==============================================================");
        
        VirtualTimeTests tests = new VirtualTimeTests();
        
        System.out.println("\nRunning virtual time tests...");
        
        try {
            tests.testDelayedResponse();
            System.out.println("✓ Delayed response test passed");
        } catch (AssertionError e) {
            System.out.println("✗ Delayed response test failed: " + e.getMessage());
        }
        
        try {
            tests.testTicker();
            System.out.println("✓ Ticker test passed");
        } catch (AssertionError e) {
            System.out.println("✗ Ticker test failed: " + e.getMessage());
        }
        
        try {
            tests.testTimeout();
            System.out.println("✓ Timeout test passed");
        } catch (AssertionError e) {
            System.out.println("✗ Timeout test failed: " + e.getMessage());
        }
        
        try {
            tests.testProgressiveAdvancement();
            System.out.println("✓ Progressive advancement test passed");
        } catch (AssertionError e) {
            System.out.println("✗ Progressive advancement test failed: " + e.getMessage());
        }
        
        try {
            tests.testExpectNoEvent();
            System.out.println("✓ ExpectNoEvent test passed");
        } catch (AssertionError e) {
            System.out.println("✗ ExpectNoEvent test failed: " + e.getMessage());
        }
        
        System.out.println("\nVirtual time tests completed!");
        System.out.println("\nKey Features:");
        System.out.println("- withVirtualTime(): Enable virtual time");
        System.out.println("- thenAwait(Duration): Advance virtual time");
        System.out.println("- expectNoEvent(Duration): Assert silence");
        System.out.println("- expectSubscription(): Expect subscription");
        System.out.println("\nTime-based Operators Tested:");
        System.out.println("- delay/delayElements");
        System.out.println("- interval");
        System.out.println("- timeout");
        System.out.println("- buffer/window with time");
        System.out.println("- debounce/sample");
        System.out.println("- take/skip with duration");
    }
}
