package com.example.graphql.resolver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.graphql.data.method.annotation.SubscriptionMapping;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * GraphQL Resolver Pattern
 * 
 * Demonstrates how to implement GraphQL resolvers using Spring for GraphQL.
 * Shows query resolvers, mutation resolvers, field resolvers, and subscription resolvers.
 * 
 * Key Components:
 * - @QueryMapping - Maps GraphQL queries to methods
 * - @MutationMapping - Maps GraphQL mutations to methods
 * - @SchemaMapping - Maps nested field resolvers
 * - @SubscriptionMapping - Maps GraphQL subscriptions
 * - @Argument - Binds GraphQL arguments to method parameters
 * 
 * Dependencies:
 * - spring-boot-starter-graphql
 * - spring-boot-starter-webflux (for reactive support)
 */
@SpringBootApplication
public class GraphQLResolverPattern {

    public static void main(String[] args) {
        SpringApplication.run(GraphQLResolverPattern.class, args);
    }

    /**
     * GraphQL Query Resolver
     * Handles all GraphQL query operations
     */
    @Controller
    public static class BookQueryResolver {
        
        private final BookResolverService bookService;
        
        public BookQueryResolver(BookResolverService bookService) {
            this.bookService = bookService;
        }

        /**
         * Query to get a book by ID
         * GraphQL: query { book(id: "1") { id title } }
         */
        @QueryMapping
        public Book book(@Argument String id) {
            return bookService.findById(id);
        }

        /**
         * Query to get all books
         * GraphQL: query { books { id title author { name } } }
         */
        @QueryMapping
        public List<Book> books() {
            return bookService.findAll();
        }

        /**
         * Query with multiple arguments
         * GraphQL: query { searchBooks(title: "Spring", minPrice: 10.0) { id title } }
         */
        @QueryMapping
        public List<Book> searchBooks(
            @Argument String title,
            @Argument Double minPrice,
            @Argument Integer limit
        ) {
            return bookService.search(title, minPrice, limit);
        }

        /**
         * Query with input object
         * GraphQL: query { filterBooks(filter: { category: "Tech", available: true }) { id title } }
         */
        @QueryMapping
        public List<Book> filterBooks(@Argument BookFilter filter) {
            return bookService.filterBooks(filter);
        }

        /**
         * Reactive query returning Mono
         */
        @QueryMapping
        public Mono<Book> bookAsync(@Argument String id) {
            return bookService.findByIdAsync(id);
        }

        /**
         * Reactive query returning Flux
         */
        @QueryMapping
        public Flux<Book> booksAsync() {
            return bookService.findAllAsync();
        }
    }

    /**
     * GraphQL Mutation Resolver
     * Handles all GraphQL mutation operations
     */
    @Controller
    public static class BookMutationResolver {
        
        private final BookResolverService bookService;
        
        public BookMutationResolver(BookResolverService bookService) {
            this.bookService = bookService;
        }

        /**
         * Mutation to create a new book
         * GraphQL: mutation { createBook(input: { title: "New Book", authorId: "1" }) { id title } }
         */
        @MutationMapping
        public Book createBook(@Argument BookInput input) {
            return bookService.createBook(input);
        }

        /**
         * Mutation to update an existing book
         * GraphQL: mutation { updateBook(id: "1", input: { title: "Updated Title" }) { id title } }
         */
        @MutationMapping
        public Book updateBook(@Argument String id, @Argument BookInput input) {
            return bookService.updateBook(id, input);
        }

        /**
         * Mutation to delete a book
         * GraphQL: mutation { deleteBook(id: "1") }
         */
        @MutationMapping
        public Boolean deleteBook(@Argument String id) {
            return bookService.deleteBook(id);
        }

        /**
         * Reactive mutation
         */
        @MutationMapping
        public Mono<Book> createBookAsync(@Argument BookInput input) {
            return bookService.createBookAsync(input);
        }
    }

    /**
     * GraphQL Field Resolver
     * Resolves nested fields for Book type
     */
    @Controller
    public static class BookFieldResolver {
        
        private final AuthorResolverService authorService;
        private final ReviewResolverService reviewService;
        
        public BookFieldResolver(AuthorResolverService authorService, ReviewResolverService reviewService) {
            this.authorService = authorService;
            this.reviewService = reviewService;
        }

        /**
         * Field resolver for Book.author
         * Resolves the author field when requested in a Book query
         */
        @SchemaMapping(typeName = "Book", field = "author")
        public Author author(Book book) {
            return authorService.findById(book.getAuthorId());
        }

        /**
         * Field resolver for Book.reviews
         * Resolves the reviews field for a book
         */
        @SchemaMapping(typeName = "Book", field = "reviews")
        public List<Review> reviews(Book book) {
            return reviewService.findByBookId(book.getId());
        }

        /**
         * Field resolver with additional logic
         * Calculates average rating for a book
         */
        @SchemaMapping(typeName = "Book", field = "averageRating")
        public Double averageRating(Book book) {
            List<Review> reviews = reviewService.findByBookId(book.getId());
            return reviews.stream()
                .mapToDouble(Review::getRating)
                .average()
                .orElse(0.0);
        }

        /**
         * Reactive field resolver
         */
        @SchemaMapping(typeName = "Book", field = "authorAsync")
        public Mono<Author> authorAsync(Book book) {
            return authorService.findByIdAsync(book.getAuthorId());
        }
    }

    /**
     * GraphQL Subscription Resolver
     * Handles real-time subscriptions
     */
    @Controller
    public static class BookSubscriptionResolver {
        
        private final BookResolverService bookService;
        
        public BookSubscriptionResolver(BookResolverService bookService) {
            this.bookService = bookService;
        }

        /**
         * Subscription for new books
         * GraphQL: subscription { bookCreated { id title } }
         */
        @SubscriptionMapping
        public Flux<Book> bookCreated() {
            return bookService.subscribeToNewBooks();
        }

        /**
         * Subscription with filter
         * GraphQL: subscription { bookUpdated(id: "1") { id title } }
         */
        @SubscriptionMapping
        public Flux<Book> bookUpdated(@Argument String id) {
            return bookService.subscribeToBookUpdates(id);
        }
    }

    /**
     * Domain Models
     */
    public static class Book {
        private String id;
        private String title;
        private String authorId;
        private Double price;
        private String category;
        private Boolean available;

        public Book(String id, String title, String authorId, Double price) {
            this.id = id;
            this.title = title;
            this.authorId = authorId;
            this.price = price;
            this.category = "General";
            this.available = true;
        }

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getAuthorId() { return authorId; }
        public void setAuthorId(String authorId) { this.authorId = authorId; }
        public Double getPrice() { return price; }
        public void setPrice(Double price) { this.price = price; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public Boolean getAvailable() { return available; }
        public void setAvailable(Boolean available) { this.available = available; }
    }

    public static class Author {
        private String id;
        private String name;

        public Author(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getId() { return id; }
        public String getName() { return name; }
    }

    public static class Review {
        private String id;
        private String bookId;
        private Integer rating;
        private String comment;

        public Review(String id, String bookId, Integer rating, String comment) {
            this.id = id;
            this.bookId = bookId;
            this.rating = rating;
            this.comment = comment;
        }

        public String getId() { return id; }
        public String getBookId() { return bookId; }
        public Integer getRating() { return rating; }
        public String getComment() { return comment; }
    }

    public record BookInput(String title, String authorId, Double price, String category) {}
    public record BookFilter(String category, Boolean available, Double minPrice, Double maxPrice) {}

    /**
     * Services
     */
    @Service
    public static class BookResolverService {
        private final Map<String, Book> books = new ConcurrentHashMap<>();
        private final Flux<Book> bookCreatedPublisher;

        public BookResolverService() {
            books.put("1", new Book("1", "Spring in Action", "1", 39.99));
            books.put("2", new Book("2", "GraphQL Basics", "2", 29.99));
            this.bookCreatedPublisher = Flux.create(sink -> {
                // Publisher logic for real-time updates
            });
        }

        public Book findById(String id) {
            return books.get(id);
        }

        public List<Book> findAll() {
            return new ArrayList<>(books.values());
        }

        public List<Book> search(String title, Double minPrice, Integer limit) {
            return books.values().stream()
                .filter(b -> title == null || b.getTitle().contains(title))
                .filter(b -> minPrice == null || b.getPrice() >= minPrice)
                .limit(limit != null ? limit : Long.MAX_VALUE)
                .toList();
        }

        public List<Book> filterBooks(BookFilter filter) {
            return books.values().stream()
                .filter(b -> filter.category() == null || b.getCategory().equals(filter.category()))
                .filter(b -> filter.available() == null || b.getAvailable().equals(filter.available()))
                .filter(b -> filter.minPrice() == null || b.getPrice() >= filter.minPrice())
                .filter(b -> filter.maxPrice() == null || b.getPrice() <= filter.maxPrice())
                .toList();
        }

        public Book createBook(BookInput input) {
            String id = UUID.randomUUID().toString();
            Book book = new Book(id, input.title(), input.authorId(), input.price());
            books.put(id, book);
            return book;
        }

        public Book updateBook(String id, BookInput input) {
            Book book = books.get(id);
            if (book != null) {
                book.setTitle(input.title());
                book.setPrice(input.price());
            }
            return book;
        }

        public Boolean deleteBook(String id) {
            return books.remove(id) != null;
        }

        public Mono<Book> findByIdAsync(String id) {
            return Mono.justOrEmpty(books.get(id)).delayElement(Duration.ofMillis(100));
        }

        public Flux<Book> findAllAsync() {
            return Flux.fromIterable(books.values()).delayElements(Duration.ofMillis(50));
        }

        public Mono<Book> createBookAsync(BookInput input) {
            return Mono.fromCallable(() -> createBook(input)).delayElement(Duration.ofMillis(100));
        }

        public Flux<Book> subscribeToNewBooks() {
            return bookCreatedPublisher;
        }

        public Flux<Book> subscribeToBookUpdates(String id) {
            return Flux.interval(Duration.ofSeconds(1))
                .map(i -> books.get(id))
                .filter(Objects::nonNull);
        }
    }

    @Service
    public static class AuthorResolverService {
        private final Map<String, Author> authors = new HashMap<>();

        public AuthorResolverService() {
            authors.put("1", new Author("1", "Craig Walls"));
            authors.put("2", new Author("2", "Eve Porcello"));
        }

        public Author findById(String id) {
            return authors.get(id);
        }

        public Mono<Author> findByIdAsync(String id) {
            return Mono.justOrEmpty(authors.get(id));
        }
    }

    @Service
    public static class ReviewResolverService {
        private final List<Review> reviews = new ArrayList<>();

        public ReviewResolverService() {
            reviews.add(new Review("1", "1", 5, "Excellent book!"));
            reviews.add(new Review("2", "1", 4, "Very informative"));
        }

        public List<Review> findByBookId(String bookId) {
            return reviews.stream()
                .filter(r -> r.getBookId().equals(bookId))
                .toList();
        }
    }
}
