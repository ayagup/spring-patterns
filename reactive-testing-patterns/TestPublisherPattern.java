package com.example.reactive.testing;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;
import reactor.test.StepVerifier;

/**
 * Test Publisher Pattern - Creating Test Publishers for Reactive Testing
 * 
 * TestPublisher is a utility from Project Reactor for creating Publishers that
 * can be manually controlled during testing. It allows you to programmatically
 * emit signals (onNext, onError, onComplete) to test how subscribers react.
 * 
 * Key Concepts:
 * - Manual Control: Emit signals on demand
 * - Behavior Control: Configure how TestPublisher should behave
 * - Signal Emission: Control when and what signals are emitted
 * - Error Injection: Inject errors at specific points
 * - Backpressure Testing: Test backpressure handling
 * 
 * TestPublisher Types:
 * - Normal: Follows reactive streams specification strictly
 * - Non-compliant: Can violate specification for error testing
 * 
 * Common Methods:
 * - next(T): Emit onNext signal
 * - error(Throwable): Emit onError signal
 * - complete(): Emit onComplete signal
 * - emit(T...): Emit multiple values then complete
 * - flux(): Convert to Flux
 * - mono(): Convert to Mono
 * - assertSubscribers(int): Assert subscriber count
 * - assertMaxRequested(long): Assert max requested
 * - assertMinRequested(long): Assert min requested
 * - assertCancelled(): Assert cancellation
 * - assertNotCancelled(): Assert not cancelled
 * 
 * Violation Options:
 * - REQUEST_OVERFLOW: Allow more elements than requested
 * - ALLOW_NULL: Allow null values
 * - CLEANUP_ON_TERMINATE: Clean up on termination
 * - DEFER_CANCELLATION: Defer cancellation
 * 
 * Use Cases:
 * - Testing subscriber behavior
 * - Testing backpressure handling
 * - Testing error scenarios
 * - Simulating edge cases
 * - Testing reactive operators
 * 
 * Best Practices:
 * - Use createNoncompliant for violation testing
 * - Assert subscriber state after operations
 * - Test both normal and edge cases
 * - Verify proper cleanup
 * - Test cancellation scenarios
 */
public class TestPublisherPattern {

    // Service that processes reactive streams
    static class DataProcessor {
        
        public Mono<String> processData(Flux<String> dataStream) {
            return dataStream
                    .filter(s -> s != null && !s.isEmpty())
                    .map(String::toUpperCase)
                    .collect(StringBuilder::new, StringBuilder::append)
                    .map(StringBuilder::toString);
        }
        
        public Flux<Integer> doubleValues(Flux<Integer> numbers) {
            return numbers.map(n -> n * 2);
        }
        
        public Mono<Long> countElements(Flux<?> stream) {
            return stream.count();
        }
        
        public Flux<String> transformWithErrorHandling(Flux<String> input) {
            return input
                    .map(String::toUpperCase)
                    .onErrorResume(e -> Flux.just("ERROR"));
        }
    }

    // Test examples
    static class TestPublisherExamples {
        
        private final DataProcessor processor = new DataProcessor();
        
        // Basic TestPublisher usage
        public void testBasicEmission() {
            TestPublisher<String> publisher = TestPublisher.create();
            
            StepVerifier.create(publisher.flux())
                    .then(() -> publisher.next("Hello"))
                    .expectNext("Hello")
                    .then(() -> publisher.next("World"))
                    .expectNext("World")
                    .then(() -> publisher.complete())
                    .verifyComplete();
        }
        
        // Using emit for multiple values
        public void testEmitMultipleValues() {
            TestPublisher<Integer> publisher = TestPublisher.create();
            
            StepVerifier.create(publisher.flux())
                    .then(() -> publisher.emit(1, 2, 3, 4, 5))
                    .expectNext(1, 2, 3, 4, 5)
                    .verifyComplete();
        }
        
        // Testing error emission
        public void testErrorEmission() {
            TestPublisher<String> publisher = TestPublisher.create();
            
            StepVerifier.create(publisher.flux())
                    .then(() -> publisher.next("Data"))
                    .expectNext("Data")
                    .then(() -> publisher.error(new RuntimeException("Test error")))
                    .expectError(RuntimeException.class)
                    .verify();
        }
        
        // Testing with processor
        public void testWithProcessor() {
            TestPublisher<String> publisher = TestPublisher.create();
            Flux<String> processed = processor.transformWithErrorHandling(publisher.flux());
            
            StepVerifier.create(processed)
                    .then(() -> publisher.next("hello"))
                    .expectNext("HELLO")
                    .then(() -> publisher.next("world"))
                    .expectNext("WORLD")
                    .then(() -> publisher.complete())
                    .verifyComplete();
        }
        
        // Testing backpressure
        public void testBackpressure() {
            TestPublisher<Integer> publisher = TestPublisher.create();
            
            StepVerifier.create(publisher.flux(), 2)  // Request only 2
                    .then(() -> {
                        publisher.next(1);
                        publisher.next(2);
                        publisher.assertMinRequested(0);
                    })
                    .expectNext(1, 2)
                    .thenRequest(1)
                    .then(() -> publisher.next(3))
                    .expectNext(3)
                    .then(() -> publisher.complete())
                    .verifyComplete();
        }
        
        // Testing cancellation
        public void testCancellation() {
            TestPublisher<String> publisher = TestPublisher.create();
            
            StepVerifier.create(publisher.flux())
                    .then(() -> publisher.next("First"))
                    .expectNext("First")
                    .thenCancel()
                    .verify();
            
            publisher.assertCancelled();
        }
        
        // Testing subscriber assertions
        public void testSubscriberAssertions() {
            TestPublisher<Integer> publisher = TestPublisher.create();
            
            // Subscribe
            publisher.flux().subscribe();
            
            // Assert one subscriber
            publisher.assertSubscribers(1);
            
            // Emit values
            publisher.next(1);
            publisher.next(2);
            publisher.complete();
            
            // Assert not cancelled
            publisher.assertNotCancelled();
        }
        
        // Testing non-compliant publisher (allows null)
        public void testNonCompliantPublisher() {
            TestPublisher<String> publisher = TestPublisher.createNoncompliant(
                    TestPublisher.Violation.ALLOW_NULL);
            
            StepVerifier.create(publisher.flux())
                    .then(() -> publisher.next("Valid"))
                    .expectNext("Valid")
                    .then(() -> publisher.next(null))  // Normally not allowed
                    .expectNext((String) null)
                    .then(() -> publisher.complete())
                    .verifyComplete();
        }
        
        // Testing request overflow
        public void testRequestOverflow() {
            TestPublisher<Integer> publisher = TestPublisher.createNoncompliant(
                    TestPublisher.Violation.REQUEST_OVERFLOW);
            
            StepVerifier.create(publisher.flux(), 2)  // Request only 2
                    .then(() -> {
                        // Emit more than requested
                        publisher.next(1);
                        publisher.next(2);
                        publisher.next(3);  // Overflow
                    })
                    .expectNext(1, 2, 3)
                    .then(() -> publisher.complete())
                    .verifyComplete();
        }
        
        // Testing as Mono
        public void testAsMono() {
            TestPublisher<String> publisher = TestPublisher.create();
            Mono<String> mono = publisher.mono();
            
            StepVerifier.create(mono)
                    .then(() -> publisher.emit("Single Value"))
                    .expectNext("Single Value")
                    .verifyComplete();
        }
        
        // Testing multiple subscribers
        public void testMultipleSubscribers() {
            TestPublisher<Integer> publisher = TestPublisher.create();
            Flux<Integer> flux = publisher.flux();
            
            // Subscribe multiple times
            flux.subscribe();
            flux.subscribe();
            
            // Assert subscriber count
            publisher.assertSubscribers(2);
            
            // Emit to all subscribers
            publisher.next(42);
            publisher.complete();
        }
        
        // Testing with operators
        public void testWithOperators() {
            TestPublisher<Integer> publisher = TestPublisher.create();
            Flux<Integer> doubled = processor.doubleValues(publisher.flux());
            
            StepVerifier.create(doubled)
                    .then(() -> publisher.next(1))
                    .expectNext(2)
                    .then(() -> publisher.next(5))
                    .expectNext(10)
                    .then(() -> publisher.next(10))
                    .expectNext(20)
                    .then(() -> publisher.complete())
                    .verifyComplete();
        }
        
        // Testing error recovery
        public void testErrorRecovery() {
            TestPublisher<String> publisher = TestPublisher.create();
            Flux<String> transformed = processor.transformWithErrorHandling(publisher.flux());
            
            StepVerifier.create(transformed)
                    .then(() -> publisher.next("data"))
                    .expectNext("DATA")
                    .then(() -> publisher.error(new RuntimeException("Error")))
                    .expectNext("ERROR")
                    .verifyComplete();
        }
        
        // Testing cleanup
        public void testCleanup() {
            TestPublisher<String> publisher = TestPublisher.createNoncompliant(
                    TestPublisher.Violation.CLEANUP_ON_TERMINATE);
            
            StepVerifier.create(publisher.flux())
                    .then(() -> publisher.emit("Data"))
                    .expectNext("Data")
                    .verifyComplete();
            
            // Publisher should be cleaned up after termination
        }
        
        // Testing deferred cancellation
        public void testDeferredCancellation() {
            TestPublisher<Integer> publisher = TestPublisher.createNoncompliant(
                    TestPublisher.Violation.DEFER_CANCELLATION);
            
            StepVerifier.create(publisher.flux())
                    .then(() -> publisher.next(1))
                    .expectNext(1)
                    .thenCancel()
                    .verify();
            
            // With DEFER_CANCELLATION, publisher might not be immediately cancelled
        }
        
        // Testing next with assertions
        public void testNextWithAssertions() {
            TestPublisher<String> publisher = TestPublisher.create();
            
            publisher.flux().subscribe();
            
            publisher.assertSubscribers(1);
            publisher.next("First");
            publisher.assertMinRequested(0);
            publisher.next("Second");
            publisher.complete();
            publisher.assertNotCancelled();
        }
    }

    public static void main(String[] args) {
        System.out.println("Test Publisher Pattern - Creating Test Publishers");
        System.out.println("=================================================");
        
        TestPublisherExamples examples = new TestPublisherExamples();
        
        System.out.println("\nRunning TestPublisher examples...");
        
        try {
            examples.testBasicEmission();
            System.out.println("✓ Basic emission test passed");
        } catch (AssertionError e) {
            System.out.println("✗ Basic emission test failed: " + e.getMessage());
        }
        
        try {
            examples.testEmitMultipleValues();
            System.out.println("✓ Emit multiple values test passed");
        } catch (AssertionError e) {
            System.out.println("✗ Emit multiple values test failed: " + e.getMessage());
        }
        
        try {
            examples.testErrorEmission();
            System.out.println("✓ Error emission test passed");
        } catch (AssertionError e) {
            System.out.println("✗ Error emission test failed: " + e.getMessage());
        }
        
        try {
            examples.testBackpressure();
            System.out.println("✓ Backpressure test passed");
        } catch (AssertionError e) {
            System.out.println("✗ Backpressure test failed: " + e.getMessage());
        }
        
        try {
            examples.testCancellation();
            System.out.println("✓ Cancellation test passed");
        } catch (AssertionError e) {
            System.out.println("✗ Cancellation test failed: " + e.getMessage());
        }
        
        System.out.println("\nTestPublisher examples completed!");
        System.out.println("\nKey Methods:");
        System.out.println("- next(T): Emit onNext signal");
        System.out.println("- error(Throwable): Emit onError signal");
        System.out.println("- complete(): Emit onComplete signal");
        System.out.println("- emit(T...): Emit values then complete");
        System.out.println("- assertSubscribers(int): Assert subscriber count");
        System.out.println("- assertCancelled(): Assert cancellation");
        System.out.println("- createNoncompliant(): Create violating publisher");
    }
}
