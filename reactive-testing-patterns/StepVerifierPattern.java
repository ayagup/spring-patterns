package com.example.reactive.testing;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.time.Duration;

/**
 * Step Verifier Pattern - Testing Reactive Publishers
 * 
 * StepVerifier is the primary tool for testing reactive streams in Project Reactor.
 * It provides a fluent API for declaring expectations about the signals emitted by
 * a Publisher (Flux or Mono) and verifying them step by step.
 * 
 * Key Concepts:
 * - Subscription: StepVerifier subscribes to the Publisher under test
 * - Expectations: Define expected signals (onNext, onError, onComplete)
 * - Verification: Verify that the Publisher emits signals as expected
 * - Time manipulation: Control virtual time for time-based operators
 * 
 * Common Methods:
 * - expectNext(T): Expect the next element
 * - expectNextCount(long): Expect a specific number of elements
 * - expectNextMatches(Predicate): Expect element matching predicate
 * - expectError(Class): Expect an error of specific type
 * - expectComplete(): Expect completion signal
 * - expectTimeout(Duration): Expect timeout
 * - verifyComplete(): Verify and expect completion
 * - verifyError(): Verify and expect error
 * - verify(): Verify all expectations
 * - thenCancel(): Cancel subscription
 * - thenAwait(Duration): Wait for specified duration
 * 
 * Use Cases:
 * - Unit testing reactive streams
 * - Testing Mono and Flux operators
 * - Verifying error handling
 * - Testing backpressure behavior
 * - Time-based operator testing
 * 
 * Best Practices:
 * - Use expectNextCount for large sequences
 * - Test both happy path and error scenarios
 * - Use expectNextMatches for complex assertions
 * - Verify completion or error at the end
 * - Use StepVerifier.withVirtualTime for time-based tests
 */
public class StepVerifierPattern {

    // Example service with reactive methods
    static class ReactiveService {
        
        public Mono<String> getUser(Long id) {
            if (id == null || id <= 0) {
                return Mono.error(new IllegalArgumentException("Invalid user ID"));
            }
            return Mono.just("User-" + id)
                    .delayElement(Duration.ofMillis(100));
        }
        
        public Flux<Integer> getNumbers(int count) {
            return Flux.range(1, count);
        }
        
        public Flux<String> getMessages() {
            return Flux.just("Hello", "World", "Reactive", "Streams");
        }
        
        public Mono<String> processWithDelay() {
            return Mono.just("Processed")
                    .delayElement(Duration.ofSeconds(5));
        }
        
        public Flux<Long> generateTicks(int count) {
            return Flux.interval(Duration.ofSeconds(1))
                    .take(count);
        }
        
        public Mono<String> failAfterDelay() {
            return Mono.delay(Duration.ofSeconds(2))
                    .then(Mono.error(new RuntimeException("Failed after delay")));
        }
        
        public Flux<Integer> generateWithError() {
            return Flux.range(1, 5)
                    .concatWith(Flux.error(new RuntimeException("Generation error")));
        }
    }

    // Test examples (would normally be in test class with @Test annotations)
    static class StepVerifierTests {
        
        private final ReactiveService service = new ReactiveService();
        
        // Basic expectation test
        public void testBasicExpectNext() {
            Mono<String> mono = service.getUser(1L);
            
            StepVerifier.create(mono)
                    .expectNext("User-1")
                    .verifyComplete();
        }
        
        // Testing multiple elements
        public void testMultipleElements() {
            Flux<String> flux = service.getMessages();
            
            StepVerifier.create(flux)
                    .expectNext("Hello")
                    .expectNext("World")
                    .expectNext("Reactive")
                    .expectNext("Streams")
                    .verifyComplete();
        }
        
        // Testing with expectNextCount
        public void testExpectNextCount() {
            Flux<Integer> flux = service.getNumbers(10);
            
            StepVerifier.create(flux)
                    .expectNextCount(10)
                    .verifyComplete();
        }
        
        // Testing with expectNextMatches
        public void testExpectNextMatches() {
            Flux<Integer> flux = service.getNumbers(5);
            
            StepVerifier.create(flux)
                    .expectNextMatches(n -> n == 1)
                    .expectNextMatches(n -> n > 1 && n <= 5)
                    .expectNextMatches(n -> n > 2 && n <= 5)
                    .expectNextMatches(n -> n > 3 && n <= 5)
                    .expectNextMatches(n -> n == 5)
                    .verifyComplete();
        }
        
        // Testing error scenarios
        public void testErrorHandling() {
            Mono<String> mono = service.getUser(-1L);
            
            StepVerifier.create(mono)
                    .expectError(IllegalArgumentException.class)
                    .verify();
        }
        
        // Testing error with message
        public void testErrorMessage() {
            Mono<String> mono = service.getUser(null);
            
            StepVerifier.create(mono)
                    .expectErrorMatches(throwable -> 
                            throwable instanceof IllegalArgumentException &&
                            throwable.getMessage().equals("Invalid user ID"))
                    .verify();
        }
        
        // Testing flux with error
        public void testFluxWithError() {
            Flux<Integer> flux = service.generateWithError();
            
            StepVerifier.create(flux)
                    .expectNext(1, 2, 3, 4, 5)
                    .expectError(RuntimeException.class)
                    .verify();
        }
        
        // Testing with virtual time
        public void testWithVirtualTime() {
            StepVerifier.withVirtualTime(() -> service.processWithDelay())
                    .expectSubscription()
                    .expectNoEvent(Duration.ofSeconds(4))
                    .thenAwait(Duration.ofSeconds(1))
                    .expectNext("Processed")
                    .verifyComplete();
        }
        
        // Testing interval with virtual time
        public void testIntervalWithVirtualTime() {
            StepVerifier.withVirtualTime(() -> service.generateTicks(3))
                    .expectSubscription()
                    .expectNoEvent(Duration.ofMillis(999))
                    .thenAwait(Duration.ofSeconds(1))
                    .expectNext(0L)
                    .thenAwait(Duration.ofSeconds(1))
                    .expectNext(1L)
                    .thenAwait(Duration.ofSeconds(1))
                    .expectNext(2L)
                    .verifyComplete();
        }
        
        // Testing cancellation
        public void testCancellation() {
            Flux<Long> flux = Flux.interval(Duration.ofMillis(100))
                    .take(10);
            
            StepVerifier.create(flux)
                    .expectNext(0L, 1L, 2L)
                    .thenCancel()
                    .verify();
        }
        
        // Testing backpressure
        public void testBackpressure() {
            Flux<Integer> flux = service.getNumbers(100);
            
            StepVerifier.create(flux, 10)  // Request only 10 initially
                    .expectNextCount(10)
                    .thenRequest(10)  // Request 10 more
                    .expectNextCount(10)
                    .thenRequest(80)  // Request remaining
                    .expectNextCount(80)
                    .verifyComplete();
        }
        
        // Testing timeout
        public void testTimeout() {
            Mono<String> mono = Mono.delay(Duration.ofSeconds(10))
                    .thenReturn("Late");
            
            StepVerifier.create(mono)
                    .expectTimeout(Duration.ofSeconds(5))
                    .verify();
        }
        
        // Testing assertion hook
        public void testAssertionHook() {
            Flux<Integer> flux = service.getNumbers(5);
            
            StepVerifier.create(flux)
                    .expectNextCount(5)
                    .expectComplete()
                    .verify();
        }
        
        // Testing recordWith for collecting elements
        public void testRecordWith() {
            Flux<String> flux = service.getMessages();
            
            StepVerifier.create(flux)
                    .recordWith(java.util.ArrayList::new)
                    .expectNextCount(4)
                    .consumeRecordedWith(messages -> {
                        assert messages.size() == 4;
                        assert messages.contains("Hello");
                        assert messages.contains("World");
                    })
                    .verifyComplete();
        }
        
        // Testing consumeNextWith
        public void testConsumeNextWith() {
            Flux<Integer> flux = service.getNumbers(3);
            
            StepVerifier.create(flux)
                    .consumeNextWith(n -> assert n == 1)
                    .consumeNextWith(n -> assert n == 2)
                    .consumeNextWith(n -> assert n == 3)
                    .verifyComplete();
        }
        
        // Testing thenAwait
        public void testThenAwait() {
            StepVerifier.withVirtualTime(() -> service.failAfterDelay())
                    .expectSubscription()
                    .thenAwait(Duration.ofSeconds(2))
                    .expectError(RuntimeException.class)
                    .verify();
        }
        
        // Testing verify with duration
        public void testVerifyWithDuration() {
            Flux<Long> flux = Flux.interval(Duration.ofMillis(100))
                    .take(5);
            
            StepVerifier.create(flux)
                    .expectNextCount(5)
                    .verifyComplete();
        }
    }

    public static void main(String[] args) {
        System.out.println("StepVerifier Pattern - Testing Reactive Publishers");
        System.out.println("==================================================");
        
        StepVerifierTests tests = new StepVerifierTests();
        
        System.out.println("\nRunning StepVerifier tests...");
        
        try {
            tests.testBasicExpectNext();
            System.out.println("✓ Basic expectNext test passed");
        } catch (AssertionError e) {
            System.out.println("✗ Basic expectNext test failed: " + e.getMessage());
        }
        
        try {
            tests.testMultipleElements();
            System.out.println("✓ Multiple elements test passed");
        } catch (AssertionError e) {
            System.out.println("✗ Multiple elements test failed: " + e.getMessage());
        }
        
        try {
            tests.testExpectNextCount();
            System.out.println("✓ ExpectNextCount test passed");
        } catch (AssertionError e) {
            System.out.println("✗ ExpectNextCount test failed: " + e.getMessage());
        }
        
        try {
            tests.testErrorHandling();
            System.out.println("✓ Error handling test passed");
        } catch (AssertionError e) {
            System.out.println("✗ Error handling test failed: " + e.getMessage());
        }
        
        try {
            tests.testBackpressure();
            System.out.println("✓ Backpressure test passed");
        } catch (AssertionError e) {
            System.out.println("✗ Backpressure test failed: " + e.getMessage());
        }
        
        System.out.println("\nStepVerifier tests completed!");
        System.out.println("\nKey Methods:");
        System.out.println("- expectNext(T): Expect specific next element");
        System.out.println("- expectNextCount(long): Expect number of elements");
        System.out.println("- expectNextMatches(Predicate): Expect matching element");
        System.out.println("- expectError(Class): Expect error of type");
        System.out.println("- expectComplete(): Expect completion signal");
        System.out.println("- verifyComplete(): Verify and expect completion");
        System.out.println("- verifyError(): Verify and expect error");
        System.out.println("- thenCancel(): Cancel subscription");
        System.out.println("- withVirtualTime(): Test time-based operators");
    }
}
