package com.example.springmodulithpatterns;

import org.springframework.modulith.events.EventPublicationRegistry;
import org.springframework.modulith.events.CompletedEventPublications;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Event Publication Registry Pattern
 * 
 * Demonstrates Spring Modulith's Event Publication Registry for tracking
 * and ensuring reliable delivery of application events.
 * 
 * Key Concepts:
 * - Event publication tracking
 * - Failed event retry
 * - Event completion management
 * - Event publication history
 * - Guaranteed event delivery
 */
@SpringBootApplication
public class EventPublicationRegistryPattern {

    public static void main(String[] args) {
        SpringApplication.run(EventPublicationRegistryPattern.class, args);
    }

    @Service
    static class OrderEventService {
        
        private final ApplicationEventPublisher eventPublisher;
        private final EventPublicationRegistry eventRegistry;
        
        public OrderEventService(
                ApplicationEventPublisher eventPublisher,
                EventPublicationRegistry eventRegistry) {
            this.eventPublisher = eventPublisher;
            this.eventRegistry = eventRegistry;
        }
        
        /**
         * Publish order created event
         * The event is automatically tracked by EventPublicationRegistry
         */
        @Transactional
        public void publishOrderCreated(String orderId, String customerId, double amount) {
            OrderCreatedEvent event = new OrderCreatedEvent(
                orderId,
                customerId,
                amount,
                Instant.now()
            );
            
            // Publish event - automatically tracked by EventPublicationRegistry
            eventPublisher.publishEvent(event);
        }
        
        /**
         * Publish order shipped event
         */
        @Transactional
        public void publishOrderShipped(String orderId, String trackingNumber) {
            OrderShippedEvent event = new OrderShippedEvent(
                orderId,
                trackingNumber,
                Instant.now()
            );
            
            eventPublisher.publishEvent(event);
        }
        
        /**
         * Get incomplete event publications
         * These are events that were published but not yet processed
         */
        public List<EventPublicationInfo> getIncompletePublications() {
            var incompleteEvents = eventRegistry.findIncompletePublications();
            
            return incompleteEvents.stream()
                .map(pub -> new EventPublicationInfo(
                    pub.getEvent().getClass().getSimpleName(),
                    pub.getPublicationDate().toString(),
                    "incomplete"
                ))
                .toList();
        }
        
        /**
         * Resubmit failed event publications
         */
        @Transactional
        public int resubmitFailedPublications() {
            var incompleteEvents = eventRegistry.findIncompletePublications();
            int count = 0;
            
            for (var publication : incompleteEvents) {
                try {
                    // Resubmit the event
                    eventPublisher.publishEvent(publication.getEvent());
                    count++;
                } catch (Exception e) {
                    // Log error and continue
                    System.err.println("Failed to resubmit event: " + e.getMessage());
                }
            }
            
            return count;
        }
        
        /**
         * Get completed event publications
         */
        public List<EventPublicationInfo> getCompletedPublications() {
            CompletedEventPublications completedEvents = 
                eventRegistry.findCompletedPublications();
            
            return completedEvents.stream()
                .map(pub -> new EventPublicationInfo(
                    pub.getEvent().getClass().getSimpleName(),
                    pub.getPublicationDate().toString(),
                    "completed"
                ))
                .toList();
        }
        
        /**
         * Clear completed event publications older than given duration
         */
        @Transactional
        public int clearOldCompletedPublications() {
            CompletedEventPublications completedEvents = 
                eventRegistry.findCompletedPublications();
            
            // Delete completed publications
            eventRegistry.deleteCompletedPublications();
            
            return (int) completedEvents.stream().count();
        }
    }

    /**
     * Event listener that processes OrderCreatedEvent
     */
    @Service
    static class OrderEventListener {
        
        @EventListener
        @Transactional
        public void handleOrderCreated(OrderCreatedEvent event) {
            // Process order created event
            System.out.println("Processing OrderCreatedEvent: " + event.orderId());
            
            // Simulate processing
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            // Event completion is automatically tracked by EventPublicationRegistry
        }
        
        @EventListener
        @Transactional
        public void handleOrderShipped(OrderShippedEvent event) {
            // Process order shipped event
            System.out.println("Processing OrderShippedEvent: " + event.orderId());
            
            // Simulate processing
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @RestController
    @RequestMapping("/api/event-registry")
    static class EventRegistryController {
        
        private final OrderEventService orderEventService;
        
        public EventRegistryController(OrderEventService orderEventService) {
            this.orderEventService = orderEventService;
        }
        
        @PostMapping("/publish/order-created")
        public PublishResponse publishOrderCreated(@RequestBody OrderCreatedRequest request) {
            orderEventService.publishOrderCreated(
                request.orderId(),
                request.customerId(),
                request.amount()
            );
            return new PublishResponse("OrderCreatedEvent published successfully");
        }
        
        @PostMapping("/publish/order-shipped")
        public PublishResponse publishOrderShipped(@RequestBody OrderShippedRequest request) {
            orderEventService.publishOrderShipped(
                request.orderId(),
                request.trackingNumber()
            );
            return new PublishResponse("OrderShippedEvent published successfully");
        }
        
        @GetMapping("/incomplete")
        public EventPublicationsResponse getIncompletePublications() {
            List<EventPublicationInfo> events = orderEventService.getIncompletePublications();
            return new EventPublicationsResponse(events, events.size());
        }
        
        @GetMapping("/completed")
        public EventPublicationsResponse getCompletedPublications() {
            List<EventPublicationInfo> events = orderEventService.getCompletedPublications();
            return new EventPublicationsResponse(events, events.size());
        }
        
        @PostMapping("/resubmit")
        public ResubmitResponse resubmitFailed() {
            int count = orderEventService.resubmitFailedPublications();
            return new ResubmitResponse(count, "Events resubmitted successfully");
        }
        
        @DeleteMapping("/completed")
        public ClearResponse clearCompleted() {
            int count = orderEventService.clearOldCompletedPublications();
            return new ClearResponse(count, "Completed publications cleared");
        }
        
        @GetMapping("/info")
        public Map<String, Object> getInfo() {
            return Map.of(
                "pattern", "Event Publication Registry Pattern",
                "description", "Track and ensure reliable delivery of application events",
                "features", List.of(
                    "Event publication tracking",
                    "Failed event retry",
                    "Event completion management",
                    "Publication history",
                    "Guaranteed delivery"
                ),
                "endpoints", List.of(
                    "POST /api/event-registry/publish/order-created",
                    "POST /api/event-registry/publish/order-shipped",
                    "GET /api/event-registry/incomplete",
                    "GET /api/event-registry/completed",
                    "POST /api/event-registry/resubmit",
                    "DELETE /api/event-registry/completed",
                    "GET /api/event-registry/info"
                )
            );
        }
    }

    // Domain Events
    record OrderCreatedEvent(
        String orderId,
        String customerId,
        double amount,
        Instant timestamp
    ) {}

    record OrderShippedEvent(
        String orderId,
        String trackingNumber,
        Instant timestamp
    ) {}

    // DTOs
    record OrderCreatedRequest(String orderId, String customerId, double amount) {}
    record OrderShippedRequest(String orderId, String trackingNumber) {}
    record PublishResponse(String message) {}
    record EventPublicationInfo(String eventType, String publicationDate, String status) {}
    record EventPublicationsResponse(List<EventPublicationInfo> publications, int count) {}
    record ResubmitResponse(int count, String message) {}
    record ClearResponse(int count, String message) {}
}
