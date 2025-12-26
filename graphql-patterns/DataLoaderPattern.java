package com.example.graphql.dataloader;

import org.dataloader.DataLoader;
import org.dataloader.DataLoaderRegistry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.graphql.execution.BatchLoaderRegistry;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Data Loader Pattern (GraphQL)
 * 
 * Demonstrates the Data Loader pattern to solve the N+1 query problem in GraphQL.
 * Data Loader batches and caches requests to avoid redundant database calls.
 * 
 * Key Concepts:
 * - Batch Loading: Collects multiple requests and fetches them in a single batch
 * - Caching: Prevents duplicate requests within a single GraphQL query
 * - N+1 Problem Solution: Reduces database queries from N+1 to 2
 * - Async Processing: Uses CompletableFuture for non-blocking operations
 * 
 * Dependencies:
 * - spring-boot-starter-graphql
 * - java-dataloader
 * - spring-boot-starter-webflux (for reactive support)
 */
@SpringBootApplication
public class DataLoaderPattern {

    public static void main(String[] args) {
        SpringApplication.run(DataLoaderPattern.class, args);
    }

    /**
     * GraphQL Data Loader Configuration
     * Registers batch loaders for efficient data fetching
     */
    @Bean
    public DataLoaderRegistry dataLoaderRegistry(
            AuthorDataLoaderService authorService,
            ReviewDataLoaderService reviewService,
            PublisherDataLoaderService publisherService) {
        
        DataLoaderRegistry registry = new DataLoaderRegistry();
        
        // Author Data Loader - batches author requests
        DataLoader<String, Author> authorLoader = DataLoader.newDataLoader(
            authorIds -> {
                System.out.println("Batch loading authors: " + authorIds);
                return CompletableFuture.supplyAsync(() -> 
                    authorService.findByIds(authorIds)
                );
            }
        );
        registry.register("authorLoader", authorLoader);
        
        // Review Data Loader - batches review requests
        DataLoader<String, List<Review>> reviewLoader = DataLoader.newMappedDataLoader(
            bookIds -> {
                System.out.println("Batch loading reviews for books: " + bookIds);
                return CompletableFuture.supplyAsync(() -> 
                    reviewService.findByBookIds(bookIds)
                );
            }
        );
        registry.register("reviewLoader", reviewLoader);
        
        // Publisher Data Loader with caching
        DataLoader<String, Publisher> publisherLoader = DataLoader.newDataLoader(
            publisherIds -> {
                System.out.println("Batch loading publishers: " + publisherIds);
                return CompletableFuture.supplyAsync(() -> 
                    publisherService.findByIds(publisherIds)
                );
            },
            org.dataloader.DataLoaderOptions.newOptions()
                .setCachingEnabled(true)
                .setMaxBatchSize(100)
        );
        registry.register("publisherLoader", publisherLoader);
        
        return registry;
    }

    /**
     * Spring for GraphQL Batch Loader Registration
     * Alternative approach using BatchLoaderRegistry
     */
    @Bean
    public BatchLoaderRegistry batchLoaderRegistry(
            AuthorDataLoaderService authorService,
            CategoryDataLoaderService categoryService) {
        
        BatchLoaderRegistry registry = new BatchLoaderRegistry();
        
        // Batch loader for authors
        registry.forTypePair(String.class, Author.class)
            .registerBatchLoader((authorIds, environment) -> {
                System.out.println("Spring Batch loading authors: " + authorIds);
                return Flux.fromIterable(authorService.findByIds(authorIds));
            });
        
        // Batch loader for categories
        registry.forName("categoryLoader")
            .registerBatchLoader((categoryIds, environment) -> {
                System.out.println("Batch loading categories: " + categoryIds);
                return Flux.fromIterable(
                    categoryService.findByIds((List<String>) categoryIds)
                );
            });
        
        return registry;
    }

    /**
     * GraphQL Controller using Data Loaders
     */
    @Controller
    public static class BookDataLoaderController {
        
        private final BookDataLoaderService bookService;
        
        public BookDataLoaderController(BookDataLoaderService bookService) {
            this.bookService = bookService;
        }

        /**
         * Query that will benefit from data loader batching
         * When fetching multiple books with their authors, data loader will:
         * 1. Collect all author IDs
         * 2. Fetch all authors in a single batch
         * 3. Return cached results for duplicate requests
         */
        @QueryMapping
        public List<Book> books() {
            return bookService.findAll();
        }

        @QueryMapping
        public Book book(String id) {
            return bookService.findById(id);
        }

        /**
         * Field resolver using Data Loader
         * Instead of fetching author for each book individually,
         * data loader will batch these requests
         */
        @SchemaMapping(typeName = "Book", field = "author")
        public CompletableFuture<Author> author(Book book, DataLoader<String, Author> authorLoader) {
            // This will be batched with other author requests
            return authorLoader.load(book.getAuthorId());
        }

        /**
         * Field resolver for reviews using mapped data loader
         */
        @SchemaMapping(typeName = "Book", field = "reviews")
        public CompletableFuture<List<Review>> reviews(
                Book book, 
                DataLoader<String, List<Review>> reviewLoader) {
            return reviewLoader.load(book.getId());
        }

        /**
         * Field resolver for publisher
         */
        @SchemaMapping(typeName = "Book", field = "publisher")
        public CompletableFuture<Publisher> publisher(
                Book book, 
                DataLoader<String, Publisher> publisherLoader) {
            return publisherLoader.load(book.getPublisherId());
        }
    }

    /**
     * Domain Models
     */
    public static class Book {
        private String id;
        private String title;
        private String authorId;
        private String publisherId;
        private List<String> categoryIds;

        public Book(String id, String title, String authorId, String publisherId) {
            this.id = id;
            this.title = title;
            this.authorId = authorId;
            this.publisherId = publisherId;
            this.categoryIds = new ArrayList<>();
        }

        public String getId() { return id; }
        public String getTitle() { return title; }
        public String getAuthorId() { return authorId; }
        public String getPublisherId() { return publisherId; }
        public List<String> getCategoryIds() { return categoryIds; }
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

    public static class Publisher {
        private String id;
        private String name;

        public Publisher(String id, String name) {
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

        public Review(String id, String bookId, Integer rating) {
            this.id = id;
            this.bookId = bookId;
            this.rating = rating;
        }

        public String getId() { return id; }
        public String getBookId() { return bookId; }
        public Integer getRating() { return rating; }
    }

    public static class Category {
        private String id;
        private String name;

        public Category(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getId() { return id; }
        public String getName() { return name; }
    }

    /**
     * Data Loader Services
     * These services implement batch loading logic
     */
    @Service
    public static class BookDataLoaderService {
        private final Map<String, Book> books = new HashMap<>();

        public BookDataLoaderService() {
            books.put("1", new Book("1", "Spring Boot in Action", "1", "1"));
            books.put("2", new Book("2", "GraphQL Essentials", "2", "1"));
            books.put("3", new Book("3", "Reactive Programming", "1", "2"));
        }

        public Book findById(String id) {
            // Simulating database delay
            simulateDelay(50);
            return books.get(id);
        }

        public List<Book> findAll() {
            simulateDelay(100);
            return new ArrayList<>(books.values());
        }

        private void simulateDelay(long ms) {
            try {
                Thread.sleep(ms);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Service
    public static class AuthorDataLoaderService {
        private final Map<String, Author> authors = new HashMap<>();

        public AuthorDataLoaderService() {
            authors.put("1", new Author("1", "Craig Walls"));
            authors.put("2", new Author("2", "Alex Banks"));
        }

        /**
         * Batch loading method - fetches multiple authors in one call
         * This is the key to solving the N+1 problem
         */
        public List<Author> findByIds(List<String> ids) {
            System.out.println("Database call: Loading " + ids.size() + " authors");
            // Simulating database batch query
            simulateDelay(100);
            return ids.stream()
                .map(authors::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        }

        private void simulateDelay(long ms) {
            try {
                Thread.sleep(ms);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Service
    public static class ReviewDataLoaderService {
        private final List<Review> reviews = new ArrayList<>();

        public ReviewDataLoaderService() {
            reviews.add(new Review("1", "1", 5));
            reviews.add(new Review("2", "1", 4));
            reviews.add(new Review("3", "2", 5));
        }

        /**
         * Batch loading method for reviews by book IDs
         * Returns a map of bookId -> List<Review>
         */
        public Map<String, List<Review>> findByBookIds(Set<String> bookIds) {
            System.out.println("Database call: Loading reviews for " + bookIds.size() + " books");
            simulateDelay(100);
            
            return reviews.stream()
                .filter(r -> bookIds.contains(r.getBookId()))
                .collect(Collectors.groupingBy(Review::getBookId));
        }

        private void simulateDelay(long ms) {
            try {
                Thread.sleep(ms);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Service
    public static class PublisherDataLoaderService {
        private final Map<String, Publisher> publishers = new HashMap<>();

        public PublisherDataLoaderService() {
            publishers.put("1", new Publisher("1", "Manning Publications"));
            publishers.put("2", new Publisher("2", "O'Reilly Media"));
        }

        public List<Publisher> findByIds(List<String> ids) {
            System.out.println("Database call: Loading " + ids.size() + " publishers");
            simulateDelay(100);
            return ids.stream()
                .map(publishers::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        }

        private void simulateDelay(long ms) {
            try {
                Thread.sleep(ms);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Service
    public static class CategoryDataLoaderService {
        private final Map<String, Category> categories = new HashMap<>();

        public CategoryDataLoaderService() {
            categories.put("1", new Category("1", "Technology"));
            categories.put("2", new Category("2", "Programming"));
        }

        public List<Category> findByIds(List<String> ids) {
            System.out.println("Database call: Loading " + ids.size() + " categories");
            return ids.stream()
                .map(categories::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        }
    }
}
