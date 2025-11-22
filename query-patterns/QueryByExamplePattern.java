package com.example.querypatterns;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Query by Example (QBE) Pattern Implementation
 * 
 * Demonstrates dynamic queries using Example API.
 * 
 * Key Components:
 * - Example for probe-based queries
 * - ExampleMatcher for customization
 * - No need for @Query annotations
 * - Dynamic filtering based on non-null fields
 * 
 * Benefits:
 * - Simple dynamic queries
 * - No boilerplate code
 * - Type-safe
 * - No string-based queries
 * - Easy to use
 * 
 * Use Cases:
 * - Dynamic search forms
 * - Simple filtering
 * - Prototype-based searches
 * - User-driven queries
 * 
 * @author Spring Patterns
 */
@SpringBootApplication
public class QueryByExamplePattern {

    public static void main(String[] args) {
        SpringApplication.run(QueryByExamplePattern.class, args);
    }

    @Entity
    @Table(name = "books")
    public static class Book {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        
        private String title;
        private String author;
        private String isbn;
        private String publisher;
        private String genre;
        private Integer publicationYear;
        private BigDecimal price;
        private Integer pages;
        private String language;
        private Boolean available;
        
        public Book() {
            this.available = true;
        }
        
        public Book(String title, String author, String genre, BigDecimal price) {
            this();
            this.title = title;
            this.author = author;
            this.genre = genre;
            this.price = price;
        }
        
        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getAuthor() { return author; }
        public void setAuthor(String author) { this.author = author; }
        
        public String getIsbn() { return isbn; }
        public void setIsbn(String isbn) { this.isbn = isbn; }
        
        public String getPublisher() { return publisher; }
        public void setPublisher(String publisher) { this.publisher = publisher; }
        
        public String getGenre() { return genre; }
        public void setGenre(String genre) { this.genre = genre; }
        
        public Integer getPublicationYear() { return publicationYear; }
        public void setPublicationYear(Integer publicationYear) { this.publicationYear = publicationYear; }
        
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        
        public Integer getPages() { return pages; }
        public void setPages(Integer pages) { this.pages = pages; }
        
        public String getLanguage() { return language; }
        public void setLanguage(String language) { this.language = language; }
        
        public Boolean getAvailable() { return available; }
        public void setAvailable(Boolean available) { this.available = available; }
    }
    
    @Repository
    public interface BookRepository extends JpaRepository<Book, Long> {
        // No @Query needed - using Example API
    }
    
    @Service
    @Transactional
    public static class BookQueryByExampleService {
        
        private final BookRepository bookRepository;
        
        public BookQueryByExampleService(BookRepository bookRepository) {
            this.bookRepository = bookRepository;
        }
        
        /**
         * Simple example - exact match on non-null fields
         */
        public List<Book> findBooksByExample(Book probe) {
            Example<Book> example = Example.of(probe);
            return bookRepository.findAll(example);
        }
        
        /**
         * Example with case-insensitive matching
         */
        public List<Book> findBooksIgnoreCase(Book probe) {
            ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnoreCase()
                .withStringMatcher(ExampleMatcher.StringMatcher.EXACT);
            
            Example<Book> example = Example.of(probe, matcher);
            return bookRepository.findAll(example);
        }
        
        /**
         * Example with "contains" matching for strings
         */
        public List<Book> searchBooks(Book probe) {
            ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnoreCase()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);
            
            Example<Book> example = Example.of(probe, matcher);
            return bookRepository.findAll(example);
        }
        
        /**
         * Example with specific fields ignored
         */
        public List<Book> findBooksIgnoringPrice(Book probe) {
            ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnorePaths("price", "pages");
            
            Example<Book> example = Example.of(probe, matcher);
            return bookRepository.findAll(example);
        }
        
        /**
         * Example with custom matchers for specific fields
         */
        public List<Book> findWithCustomMatching(Book probe) {
            ExampleMatcher matcher = ExampleMatcher.matching()
                .withMatcher("title", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase())
                .withMatcher("author", ExampleMatcher.GenericPropertyMatchers.startsWith().ignoreCase())
                .withMatcher("genre", ExampleMatcher.GenericPropertyMatchers.exact());
            
            Example<Book> example = Example.of(probe, matcher);
            return bookRepository.findAll(example);
        }
        
        /**
         * Example with null handling
         */
        public List<Book> findBooksIncludingNulls(Book probe) {
            ExampleMatcher matcher = ExampleMatcher.matching()
                .withIncludeNullValues()
                .withIgnoreCase();
            
            Example<Book> example = Example.of(probe, matcher);
            return bookRepository.findAll(example);
        }
        
        /**
         * Example with "starts with" matching
         */
        public List<Book> findBooksStartingWith(String titlePrefix, String authorPrefix) {
            Book probe = new Book();
            probe.setTitle(titlePrefix);
            probe.setAuthor(authorPrefix);
            
            ExampleMatcher matcher = ExampleMatcher.matching()
                .withStringMatcher(ExampleMatcher.StringMatcher.STARTING)
                .withIgnoreCase();
            
            Example<Book> example = Example.of(probe, matcher);
            return bookRepository.findAll(example);
        }
        
        /**
         * Example with "ends with" matching
         */
        public List<Book> findBooksEndingWith(String titleSuffix) {
            Book probe = new Book();
            probe.setTitle(titleSuffix);
            
            ExampleMatcher matcher = ExampleMatcher.matching()
                .withMatcher("title", ExampleMatcher.GenericPropertyMatchers.endsWith().ignoreCase());
            
            Example<Book> example = Example.of(probe, matcher);
            return bookRepository.findAll(example);
        }
        
        /**
         * Count using Example
         */
        public long countBooksByExample(Book probe) {
            Example<Book> example = Example.of(probe);
            return bookRepository.count(example);
        }
        
        /**
         * Check existence using Example
         */
        public boolean existsByExample(Book probe) {
            Example<Book> example = Example.of(probe);
            return bookRepository.exists(example);
        }
        
        /**
         * Complex search with multiple criteria
         */
        public List<Book> complexSearch(String title, String author, String genre, 
                                        Boolean available) {
            Book probe = new Book();
            probe.setTitle(title);
            probe.setAuthor(author);
            probe.setGenre(genre);
            probe.setAvailable(available);
            
            ExampleMatcher matcher = ExampleMatcher.matchingAll()
                .withIgnoreCase()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
                .withIgnoreNullValues();
            
            Example<Book> example = Example.of(probe, matcher);
            return bookRepository.findAll(example);
        }
    }
    
    @RestController
    @RequestMapping("/api/qbe/books")
    public static class BookQBEController {
        
        private final BookQueryByExampleService qbeService;
        private final BookRepository bookRepository;
        
        public BookQBEController(BookQueryByExampleService qbeService,
                                BookRepository bookRepository) {
            this.qbeService = qbeService;
            this.bookRepository = bookRepository;
        }
        
        @PostMapping
        public Book create(@RequestBody Book book) {
            return bookRepository.save(book);
        }
        
        @PostMapping("/search")
        public List<Book> search(@RequestBody Book probe) {
            return qbeService.searchBooks(probe);
        }
        
        @GetMapping("/search")
        public List<Book> searchByParams(
                @RequestParam(required = false) String title,
                @RequestParam(required = false) String author,
                @RequestParam(required = false) String genre,
                @RequestParam(required = false) Boolean available) {
            return qbeService.complexSearch(title, author, genre, available);
        }
        
        @PostMapping("/count")
        public long count(@RequestBody Book probe) {
            return qbeService.countBooksByExample(probe);
        }
        
        @PostMapping("/exists")
        public boolean exists(@RequestBody Book probe) {
            return qbeService.existsByExample(probe);
        }
    }
}

/**
 * Best Practices:
 * 1. Use for simple dynamic queries
 * 2. Customize ExampleMatcher for specific needs
 * 3. Consider performance for large datasets
 * 4. Not suitable for complex queries (joins, aggregations)
 * 5. Works best with string matching
 * 6. Cannot handle OR conditions easily
 * 7. Limited support for numeric ranges
 * 8. Use Specification or Querydsl for complex scenarios
 */
