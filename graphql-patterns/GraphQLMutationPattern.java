package com.example.graphql.mutation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * GraphQL Mutation Pattern
 * 
 * Demonstrates how to implement GraphQL mutations for Create, Update, and Delete operations.
 * Shows input validation, transactional mutations, optimistic locking, and batch mutations.
 * 
 * Key Concepts:
 * - @MutationMapping - Maps GraphQL mutations to methods
 * - Input Types - Structured input objects for mutations
 * - Validation - JSR-303 validation on inputs
 * - Transactions - Transactional mutation operations
 * - Return Types - Mutation results and payloads
 * - Error Handling - Graceful error responses
 * 
 * Dependencies:
 * - spring-boot-starter-graphql
 * - spring-boot-starter-validation
 * - spring-boot-starter-data-jpa (for transactions)
 */
@SpringBootApplication
public class GraphQLMutationPattern {

    public static void main(String[] args) {
        SpringApplication.run(GraphQLMutationPattern.class, args);
    }

    /**
     * GraphQL Mutation Controller
     * Handles all mutation operations
     */
    @Controller
    public static class BookMutationController {
        
        private final BookMutationService bookService;
        
        public BookMutationController(BookMutationService bookService) {
            this.bookService = bookService;
        }

        /**
         * Create Book Mutation
         * GraphQL: mutation { createBook(input: { title: "New Book", authorId: "1" }) { id title } }
         */
        @MutationMapping
        @Transactional
        public Book createBook(@Argument @Valid CreateBookInput input) {
            return bookService.createBook(input);
        }

        /**
         * Update Book Mutation with Optimistic Locking
         * GraphQL: mutation { updateBook(id: "1", input: { title: "Updated", version: 1 }) { id title version } }
         */
        @MutationMapping
        @Transactional
        public Book updateBook(@Argument String id, @Argument @Valid UpdateBookInput input) {
            return bookService.updateBook(id, input);
        }

        /**
         * Delete Book Mutation
         * GraphQL: mutation { deleteBook(id: "1") { success message } }
         */
        @MutationMapping
        @Transactional
        public DeleteBookPayload deleteBook(@Argument String id) {
            boolean success = bookService.deleteBook(id);
            return new DeleteBookPayload(success, success ? "Book deleted successfully" : "Book not found");
        }

        /**
         * Soft Delete Mutation
         */
        @MutationMapping
        @Transactional
        public Book softDeleteBook(@Argument String id) {
            return bookService.softDeleteBook(id);
        }

        /**
         * Batch Create Mutation
         * GraphQL: mutation { createBooks(inputs: [{...}, {...}]) { id title } }
         */
        @MutationMapping
        @Transactional
        public List<Book> createBooks(@Argument List<@Valid CreateBookInput> inputs) {
            return bookService.createBooks(inputs);
        }

        /**
         * Batch Update Mutation
         */
        @MutationMapping
        @Transactional
        public List<Book> updateBooks(@Argument List<@Valid BatchUpdateInput> inputs) {
            return bookService.updateBooks(inputs);
        }

        /**
         * Batch Delete Mutation
         */
        @MutationMapping
        @Transactional
        public BatchDeletePayload deleteBooks(@Argument List<String> ids) {
            int deletedCount = bookService.deleteBooks(ids);
            return new BatchDeletePayload(deletedCount, ids.size(), "Deleted " + deletedCount + " books");
        }

        /**
         * Partial Update Mutation (PATCH-style)
         */
        @MutationMapping
        @Transactional
        public Book patchBook(@Argument String id, @Argument PatchBookInput input) {
            return bookService.patchBook(id, input);
        }

        /**
         * Add Review Mutation (Nested Mutation)
         */
        @MutationMapping
        @Transactional
        public Book addReview(@Argument String bookId, @Argument @Valid AddReviewInput input) {
            return bookService.addReview(bookId, input);
        }

        /**
         * Reactive Mutation
         */
        @MutationMapping
        public Mono<Book> createBookAsync(@Argument @Valid CreateBookInput input) {
            return bookService.createBookAsync(input);
        }

        /**
         * Mutation with Custom Payload
         */
        @MutationMapping
        @Transactional
        public PublishBookPayload publishBook(@Argument String id) {
            Book book = bookService.publishBook(id);
            return new PublishBookPayload(book, "Book published successfully", LocalDateTime.now());
        }

        /**
         * Mutation with Side Effects
         */
        @MutationMapping
        @Transactional
        public Book updateBookAndNotify(@Argument String id, @Argument @Valid UpdateBookInput input) {
            Book book = bookService.updateBook(id, input);
            bookService.notifySubscribers(book);
            return book;
        }
    }

    /**
     * Input Types (DTOs)
     */
    public record CreateBookInput(
        @NotBlank(message = "Title is required")
        @Size(min = 1, max = 200, message = "Title must be between 1 and 200 characters")
        String title,
        
        @NotBlank(message = "Author ID is required")
        String authorId,
        
        String isbn,
        
        Double price,
        
        List<String> tags
    ) {}

    public record UpdateBookInput(
        @NotBlank(message = "Title is required")
        String title,
        
        String isbn,
        
        Double price,
        
        @NotNull(message = "Version is required for optimistic locking")
        Integer version,
        
        List<String> tags
    ) {}

    public record BatchUpdateInput(
        @NotBlank String id,
        @NotBlank String title,
        Integer version
    ) {}

    public record PatchBookInput(
        String title,
        String isbn,
        Double price,
        Boolean available
    ) {}

    public record AddReviewInput(
        @NotBlank String userId,
        @NotNull Integer rating,
        String comment
    ) {}

    /**
     * Payload Types (Response DTOs)
     */
    public record DeleteBookPayload(boolean success, String message) {}
    
    public record BatchDeletePayload(int deletedCount, int totalRequested, String message) {}
    
    public record PublishBookPayload(Book book, String message, LocalDateTime publishedAt) {}

    /**
     * Domain Models
     */
    public static class Book {
        private String id;
        private String title;
        private String authorId;
        private String isbn;
        private Double price;
        private Integer version;
        private List<String> tags;
        private List<Review> reviews;
        private boolean deleted;
        private boolean published;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Book(String id, String title, String authorId) {
            this.id = id;
            this.title = title;
            this.authorId = authorId;
            this.version = 0;
            this.tags = new ArrayList<>();
            this.reviews = new ArrayList<>();
            this.deleted = false;
            this.published = false;
            this.createdAt = LocalDateTime.now();
            this.updatedAt = LocalDateTime.now();
        }

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getTitle() { return title; }
        public void setTitle(String title) { 
            this.title = title;
            this.updatedAt = LocalDateTime.now();
        }
        public String getAuthorId() { return authorId; }
        public void setAuthorId(String authorId) { this.authorId = authorId; }
        public String getIsbn() { return isbn; }
        public void setIsbn(String isbn) { this.isbn = isbn; }
        public Double getPrice() { return price; }
        public void setPrice(Double price) { this.price = price; }
        public Integer getVersion() { return version; }
        public void setVersion(Integer version) { this.version = version; }
        public void incrementVersion() { this.version++; }
        public List<String> getTags() { return tags; }
        public void setTags(List<String> tags) { this.tags = tags; }
        public List<Review> getReviews() { return reviews; }
        public void setReviews(List<Review> reviews) { this.reviews = reviews; }
        public boolean isDeleted() { return deleted; }
        public void setDeleted(boolean deleted) { this.deleted = deleted; }
        public boolean isPublished() { return published; }
        public void setPublished(boolean published) { this.published = published; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public LocalDateTime getUpdatedAt() { return updatedAt; }
    }

    public static class Review {
        private String id;
        private String userId;
        private Integer rating;
        private String comment;
        private LocalDateTime createdAt;

        public Review(String id, String userId, Integer rating, String comment) {
            this.id = id;
            this.userId = userId;
            this.rating = rating;
            this.comment = comment;
            this.createdAt = LocalDateTime.now();
        }

        public String getId() { return id; }
        public String getUserId() { return userId; }
        public Integer getRating() { return rating; }
        public String getComment() { return comment; }
        public LocalDateTime getCreatedAt() { return createdAt; }
    }

    /**
     * Mutation Service
     */
    @Service
    public static class BookMutationService {
        private final Map<String, Book> books = new ConcurrentHashMap<>();

        public Book createBook(CreateBookInput input) {
            String id = UUID.randomUUID().toString();
            Book book = new Book(id, input.title(), input.authorId());
            book.setIsbn(input.isbn());
            book.setPrice(input.price());
            if (input.tags() != null) {
                book.setTags(new ArrayList<>(input.tags()));
            }
            books.put(id, book);
            return book;
        }

        public Book updateBook(String id, UpdateBookInput input) {
            Book book = books.get(id);
            if (book == null) {
                throw new RuntimeException("Book not found: " + id);
            }
            
            // Optimistic locking check
            if (!book.getVersion().equals(input.version())) {
                throw new RuntimeException("Version mismatch. Book was modified by another user.");
            }
            
            book.setTitle(input.title());
            book.setIsbn(input.isbn());
            book.setPrice(input.price());
            if (input.tags() != null) {
                book.setTags(new ArrayList<>(input.tags()));
            }
            book.incrementVersion();
            
            return book;
        }

        public boolean deleteBook(String id) {
            return books.remove(id) != null;
        }

        public Book softDeleteBook(String id) {
            Book book = books.get(id);
            if (book != null) {
                book.setDeleted(true);
            }
            return book;
        }

        public List<Book> createBooks(List<CreateBookInput> inputs) {
            return inputs.stream()
                .map(this::createBook)
                .toList();
        }

        public List<Book> updateBooks(List<BatchUpdateInput> inputs) {
            return inputs.stream()
                .map(input -> {
                    UpdateBookInput updateInput = new UpdateBookInput(
                        input.title(), null, null, input.version(), null
                    );
                    return updateBook(input.id(), updateInput);
                })
                .toList();
        }

        public int deleteBooks(List<String> ids) {
            return (int) ids.stream()
                .filter(this::deleteBook)
                .count();
        }

        public Book patchBook(String id, PatchBookInput input) {
            Book book = books.get(id);
            if (book == null) {
                throw new RuntimeException("Book not found: " + id);
            }
            
            // Only update provided fields
            if (input.title() != null) book.setTitle(input.title());
            if (input.isbn() != null) book.setIsbn(input.isbn());
            if (input.price() != null) book.setPrice(input.price());
            if (input.available() != null) book.setDeleted(!input.available());
            
            book.incrementVersion();
            return book;
        }

        public Book addReview(String bookId, AddReviewInput input) {
            Book book = books.get(bookId);
            if (book == null) {
                throw new RuntimeException("Book not found: " + bookId);
            }
            
            String reviewId = UUID.randomUUID().toString();
            Review review = new Review(reviewId, input.userId(), input.rating(), input.comment());
            book.getReviews().add(review);
            
            return book;
        }

        public Mono<Book> createBookAsync(CreateBookInput input) {
            return Mono.fromCallable(() -> createBook(input));
        }

        public Book publishBook(String id) {
            Book book = books.get(id);
            if (book != null) {
                book.setPublished(true);
            }
            return book;
        }

        public void notifySubscribers(Book book) {
            // Simulated notification logic
            System.out.println("Notifying subscribers about update to: " + book.getTitle());
        }
    }
}
