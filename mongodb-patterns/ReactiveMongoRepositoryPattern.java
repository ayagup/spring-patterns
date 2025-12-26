package com.example.mongodb;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/**
 * Reactive Mongo Repository Pattern
 * 
 * Demonstrates reactive Spring Data MongoDB repository.
 * 
 * Reactive Repository Features:
 * - Returns Mono and Flux
 * - Non-blocking operations
 * - Backpressure support
 * - Reactive query methods
 * - Reactive custom queries
 * 
 * Use Cases:
 * - Reactive applications
 * - Non-blocking data access
 * - Streaming responses
 * - High-concurrency scenarios
 * 
 * @author Spring Patterns
 * @version 1.0
 */
@Configuration
public class ReactiveMongoRepositoryPattern {
}

@Document(collection = "books")
record Book(
    @Id String id,
    String title,
    String author,
    String isbn,
    String genre,
    int year,
    double price,
    int stock,
    LocalDateTime addedAt
) {}

interface ReactiveBookRepository extends ReactiveMongoRepository<Book, String> {
    
    Mono<Book> findByIsbn(String isbn);
    
    Flux<Book> findByAuthor(String author);
    
    Flux<Book> findByGenre(String genre);
    
    Flux<Book> findByYearGreaterThan(int year);
    
    Flux<Book> findByPriceLessThan(double price);
    
    Flux<Book> findByStockGreaterThan(int stock);
    
    @Query("{'title': {$regex: ?0, $options: 'i'}}")
    Flux<Book> searchByTitle(String titlePattern);
    
    @Query("{'year': {$gte: ?0, $lte: ?1}}")
    Flux<Book> findByYearRange(int startYear, int endYear);
    
    @Query("{'price': {$lte: ?0}, 'stock': {$gt: ?1}}")
    Flux<Book> findAffordableInStock(double maxPrice, int minStock);
    
    Mono<Long> countByAuthor(String author);
    
    Mono<Long> countByGenre(String genre);
    
    Mono<Boolean> existsByIsbn(String isbn);
    
    Mono<Void> deleteByIsbn(String isbn);
}

@RestController
@RequestMapping("/api/reactive/books")
class ReactiveBookController {

    private final ReactiveBookRepository bookRepository;

    public ReactiveBookController(ReactiveBookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @PostMapping
    public Mono<ResponseEntity<Book>> createBook(@RequestBody Book book) {
        return bookRepository.save(book)
            .map(ResponseEntity::ok);
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Book>> getBook(@PathVariable String id) {
        return bookRepository.findById(id)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping
    public Flux<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    @GetMapping("/isbn/{isbn}")
    public Mono<ResponseEntity<Book>> getByIsbn(@PathVariable String isbn) {
        return bookRepository.findByIsbn(isbn)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/author/{author}")
    public Flux<Book> getByAuthor(@PathVariable String author) {
        return bookRepository.findByAuthor(author);
    }

    @GetMapping("/genre/{genre}")
    public Flux<Book> getByGenre(@PathVariable String genre) {
        return bookRepository.findByGenre(genre);
    }

    @GetMapping("/year-after/{year}")
    public Flux<Book> getAfterYear(@PathVariable int year) {
        return bookRepository.findByYearGreaterThan(year);
    }

    @GetMapping("/price-under/{price}")
    public Flux<Book> getUnderPrice(@PathVariable double price) {
        return bookRepository.findByPriceLessThan(price);
    }

    @GetMapping("/in-stock/{minStock}")
    public Flux<Book> getInStock(@PathVariable int minStock) {
        return bookRepository.findByStockGreaterThan(minStock);
    }

    @GetMapping("/search")
    public Flux<Book> searchBooks(@RequestParam String title) {
        return bookRepository.searchByTitle(title);
    }

    @GetMapping("/year-range")
    public Flux<Book> getByYearRange(
            @RequestParam int start,
            @RequestParam int end) {
        return bookRepository.findByYearRange(start, end);
    }

    @GetMapping("/affordable")
    public Flux<Book> getAffordableInStock(
            @RequestParam double maxPrice,
            @RequestParam int minStock) {
        return bookRepository.findAffordableInStock(maxPrice, minStock);
    }

    @GetMapping("/count")
    public Mono<Long> countBooks() {
        return bookRepository.count();
    }

    @GetMapping("/count/author/{author}")
    public Mono<Long> countByAuthor(@PathVariable String author) {
        return bookRepository.countByAuthor(author);
    }

    @GetMapping("/count/genre/{genre}")
    public Mono<Long> countByGenre(@PathVariable String genre) {
        return bookRepository.countByGenre(genre);
    }

    @GetMapping("/exists/isbn/{isbn}")
    public Mono<Boolean> existsByIsbn(@PathVariable String isbn) {
        return bookRepository.existsByIsbn(isbn);
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteBook(@PathVariable String id) {
        return bookRepository.deleteById(id)
            .map(v -> ResponseEntity.noContent().<Void>build());
    }

    @DeleteMapping("/isbn/{isbn}")
    public Mono<ResponseEntity<Void>> deleteByIsbn(@PathVariable String isbn) {
        return bookRepository.deleteByIsbn(isbn)
            .map(v -> ResponseEntity.noContent().<Void>build());
    }

    @GetMapping("/info")
    public Mono<PatternInfo> getPatternInfo() {
        return Mono.just(new PatternInfo(
            "Reactive Mongo Repository Pattern",
            "Reactive data access using ReactiveMongoRepository",
            "1.0",
            List.of("Reactive streams", "Non-blocking", "Query methods", "Backpressure"),
            List.of("Reactive apps", "Streaming", "High concurrency")
        ));
    }

    record PatternInfo(String name, String description, String version, 
                      List<String> features, List<String> useCases) {}
}
