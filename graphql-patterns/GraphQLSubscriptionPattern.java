package com.example.graphql.subscription;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.SubscriptionMapping;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * GraphQL Subscription Pattern
 * 
 * Demonstrates real-time GraphQL subscriptions for push-based data updates.
 * Shows event publishing, filtering, and reactive streaming.
 * 
 * Key Concepts:
 * - @SubscriptionMapping - Maps GraphQL subscriptions to reactive streams
 * - Flux Publisher - Reactive stream for continuous data
 * - Sinks - Multicasting events to multiple subscribers
 * - Event Filtering - Subscription with arguments
 * - Backpressure - Handling slow consumers
 * 
 * Dependencies:
 * - spring-boot-starter-graphql
 * - spring-boot-starter-webflux
 * - reactor-core
 */
@SpringBootApplication
public class GraphQLSubscriptionPattern {

    public static void main(String[] args) {
        SpringApplication.run(GraphQLSubscriptionPattern.class, args);
    }

    /**
     * GraphQL Subscription Controller
     */
    @Controller
    public static class BookSubscriptionController {
        
        private final BookSubscriptionService subscriptionService;
        
        public BookSubscriptionController(BookSubscriptionService subscriptionService) {
            this.subscriptionService = subscriptionService;
        }

        /**
         * Subscribe to newly created books
         * GraphQL: subscription { bookCreated { id title author } }
         */
        @SubscriptionMapping
        public Flux<Book> bookCreated() {
            return subscriptionService.subscribeToBookCreated();
        }

        /**
         * Subscribe to book updates for specific book
         * GraphQL: subscription { bookUpdated(id: "1") { id title version } }
         */
        @SubscriptionMapping
        public Flux<Book> bookUpdated(@Argument String id) {
            return subscriptionService.subscribeToBookUpdated(id);
        }

        /**
         * Subscribe to book deletions
         * GraphQL: subscription { bookDeleted { id title deletedAt } }
         */
        @SubscriptionMapping
        public Flux<BookDeletedEvent> bookDeleted() {
            return subscriptionService.subscribeToBookDeleted();
        }

        /**
         * Subscribe to reviews for a specific book
         * GraphQL: subscription { reviewAdded(bookId: "1") { id rating comment } }
         */
        @SubscriptionMapping
        public Flux<Review> reviewAdded(@Argument String bookId) {
            return subscriptionService.subscribeToReviewsForBook(bookId);
        }

        /**
         * Subscribe to all book events (create, update, delete)
         * GraphQL: subscription { bookEvent { type book } }
         */
        @SubscriptionMapping
        public Flux<BookEvent> bookEvent() {
            return subscriptionService.subscribeToAllBookEvents();
        }

        /**
         * Subscribe with filter criteria
         * GraphQL: subscription { booksInCategory(category: "Tech") { id title } }
         */
        @SubscriptionMapping
        public Flux<Book> booksInCategory(@Argument String category) {
            return subscriptionService.subscribeToBooksInCategory(category);
        }

        /**
         * Periodic subscription (interval-based)
         * GraphQL: subscription { bookStats { totalBooks averageRating } }
         */
        @SubscriptionMapping
        public Flux<BookStats> bookStats() {
            return subscriptionService.subscribeToBookStats();
        }

        /**
         * Subscription with backpressure handling
         */
        @SubscriptionMapping
        public Flux<PriceUpdate> priceUpdates() {
            return subscriptionService.subscribeToPriceUpdates()
                .onBackpressureBuffer(100)
                .onBackpressureDrop(update -> 
                    System.out.println("Dropped price update: " + update)
                );
        }
    }

    /**
     * Domain Models
     */
    public static class Book {
        private String id;
        private String title;
        private String author;
        private String category;
        private Double price;
        private Integer version;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Book(String id, String title, String author) {
            this.id = id;
            this.title = title;
            this.author = author;
            this.version = 0;
            this.createdAt = LocalDateTime.now();
            this.updatedAt = LocalDateTime.now();
        }

        public String getId() { return id; }
        public String getTitle() { return title; }
        public void setTitle(String title) { 
            this.title = title;
            this.updatedAt = LocalDateTime.now();
            this.version++;
        }
        public String getAuthor() { return author; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public Double getPrice() { return price; }
        public void setPrice(Double price) { this.price = price; }
        public Integer getVersion() { return version; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public LocalDateTime getUpdatedAt() { return updatedAt; }
    }

    public record Review(String id, String bookId, String userId, Integer rating, String comment, LocalDateTime createdAt) {}
    
    public record BookDeletedEvent(String id, String title, LocalDateTime deletedAt) {}
    
    public record BookEvent(String type, Book book, LocalDateTime timestamp) {}
    
    public record BookStats(int totalBooks, double averageRating, int totalReviews) {}
    
    public record PriceUpdate(String bookId, String title, Double oldPrice, Double newPrice, LocalDateTime timestamp) {}

    /**
     * Subscription Service
     * Manages event publishers and subscribers
     */
    @Service
    public static class BookSubscriptionService {
        
        // Sinks for multicasting events to multiple subscribers
        private final Sinks.Many<Book> bookCreatedSink;
        private final Sinks.Many<Book> bookUpdatedSink;
        private final Sinks.Many<BookDeletedEvent> bookDeletedSink;
        private final Sinks.Many<Review> reviewAddedSink;
        private final Sinks.Many<BookEvent> bookEventSink;
        private final Sinks.Many<PriceUpdate> priceUpdateSink;
        
        private final Map<String, Book> books = new ConcurrentHashMap<>();
        private final List<Review> reviews = new ArrayList<>();

        public BookSubscriptionService() {
            // Initialize sinks for event publishing
            this.bookCreatedSink = Sinks.many().multicast().onBackpressureBuffer();
            this.bookUpdatedSink = Sinks.many().multicast().onBackpressureBuffer();
            this.bookDeletedSink = Sinks.many().multicast().onBackpressureBuffer();
            this.reviewAddedSink = Sinks.many().multicast().onBackpressureBuffer();
            this.bookEventSink = Sinks.many().multicast().onBackpressureBuffer();
            this.priceUpdateSink = Sinks.many().multicast().onBackpressureBuffer();
            
            // Simulate some events for testing
            startEventSimulation();
        }

        /**
         * Subscribe to book creation events
         */
        public Flux<Book> subscribeToBookCreated() {
            return bookCreatedSink.asFlux();
        }

        /**
         * Subscribe to updates for a specific book
         */
        public Flux<Book> subscribeToBookUpdated(String id) {
            return bookUpdatedSink.asFlux()
                .filter(book -> book.getId().equals(id));
        }

        /**
         * Subscribe to book deletion events
         */
        public Flux<BookDeletedEvent> subscribeToBookDeleted() {
            return bookDeletedSink.asFlux();
        }

        /**
         * Subscribe to reviews for a specific book
         */
        public Flux<Review> subscribeToReviewsForBook(String bookId) {
            return reviewAddedSink.asFlux()
                .filter(review -> review.bookId().equals(bookId));
        }

        /**
         * Subscribe to all book events
         */
        public Flux<BookEvent> subscribeToAllBookEvents() {
            return bookEventSink.asFlux();
        }

        /**
         * Subscribe to books in specific category
         */
        public Flux<Book> subscribeToBooksInCategory(String category) {
            return bookCreatedSink.asFlux()
                .filter(book -> category.equals(book.getCategory()));
        }

        /**
         * Subscribe to periodic stats updates
         */
        public Flux<BookStats> subscribeToBookStats() {
            return Flux.interval(Duration.ofSeconds(5))
                .map(tick -> calculateStats());
        }

        /**
         * Subscribe to price updates
         */
        public Flux<PriceUpdate> subscribeToPriceUpdates() {
            return priceUpdateSink.asFlux();
        }

        /**
         * Publish a book creation event
         */
        public void publishBookCreated(Book book) {
            books.put(book.getId(), book);
            bookCreatedSink.tryEmitNext(book);
            bookEventSink.tryEmitNext(new BookEvent("CREATED", book, LocalDateTime.now()));
        }

        /**
         * Publish a book update event
         */
        public void publishBookUpdated(Book book) {
            books.put(book.getId(), book);
            bookUpdatedSink.tryEmitNext(book);
            bookEventSink.tryEmitNext(new BookEvent("UPDATED", book, LocalDateTime.now()));
        }

        /**
         * Publish a book deletion event
         */
        public void publishBookDeleted(String id, String title) {
            books.remove(id);
            BookDeletedEvent event = new BookDeletedEvent(id, title, LocalDateTime.now());
            bookDeletedSink.tryEmitNext(event);
            
            // Can't include book in event since it's deleted, so create a placeholder
            Book deletedBook = new Book(id, title, "Unknown");
            bookEventSink.tryEmitNext(new BookEvent("DELETED", deletedBook, LocalDateTime.now()));
        }

        /**
         * Publish a review added event
         */
        public void publishReviewAdded(Review review) {
            reviews.add(review);
            reviewAddedSink.tryEmitNext(review);
        }

        /**
         * Publish a price update event
         */
        public void publishPriceUpdate(String bookId, Double oldPrice, Double newPrice) {
            Book book = books.get(bookId);
            if (book != null) {
                PriceUpdate update = new PriceUpdate(
                    bookId, 
                    book.getTitle(), 
                    oldPrice, 
                    newPrice, 
                    LocalDateTime.now()
                );
                priceUpdateSink.tryEmitNext(update);
                book.setPrice(newPrice);
            }
        }

        /**
         * Calculate current statistics
         */
        private BookStats calculateStats() {
            int totalBooks = books.size();
            double avgRating = reviews.stream()
                .mapToInt(Review::rating)
                .average()
                .orElse(0.0);
            int totalReviews = reviews.size();
            
            return new BookStats(totalBooks, avgRating, totalReviews);
        }

        /**
         * Simulate events for testing subscriptions
         */
        private void startEventSimulation() {
            // Simulate book creation every 10 seconds
            Flux.interval(Duration.ofSeconds(10))
                .subscribe(tick -> {
                    String id = UUID.randomUUID().toString();
                    Book book = new Book(id, "Book " + tick, "Author " + tick);
                    book.setCategory(tick % 2 == 0 ? "Tech" : "Fiction");
                    book.setPrice(29.99 + tick);
                    publishBookCreated(book);
                });

            // Simulate book updates every 15 seconds
            Flux.interval(Duration.ofSeconds(15))
                .subscribe(tick -> {
                    if (!books.isEmpty()) {
                        Book book = books.values().iterator().next();
                        book.setTitle("Updated: " + book.getTitle());
                        publishBookUpdated(book);
                    }
                });

            // Simulate price updates every 7 seconds
            Flux.interval(Duration.ofSeconds(7))
                .subscribe(tick -> {
                    if (!books.isEmpty()) {
                        Book book = books.values().iterator().next();
                        Double oldPrice = book.getPrice();
                        Double newPrice = oldPrice * 1.1;
                        publishPriceUpdate(book.getId(), oldPrice, newPrice);
                    }
                });
        }
    }
}
