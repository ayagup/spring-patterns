package com.example.elasticsearch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Search Query Pattern
 * 
 * Demonstrates advanced search query capabilities in Elasticsearch
 * including full-text search, fuzzy matching, wildcard queries,
 * bool queries, and highlighting.
 * 
 * Key concepts:
 * - Match queries for full-text search
 * - Multi-match queries across fields
 * - Bool queries (must, should, must_not, filter)
 * - Fuzzy queries for typo tolerance
 * - Wildcard and prefix queries
 * - Range queries
 * - Highlighting search results
 * - Scoring and relevance
 * 
 * Use cases:
 * - Full-text search engines
 * - Autocomplete and suggestions
 * - Faceted search
 * - Complex filtering
 * - Relevance ranking
 */
@SpringBootApplication
public class SearchQueryPattern {

    public static void main(String[] args) {
        SpringApplication.run(SearchQueryPattern.class, args);
    }
}

/**
 * Book document for search queries
 */
record Book(
    String id,
    String title,
    String author,
    String description,
    String isbn,
    List<String> genres,
    Integer year,
    Double rating,
    Integer pages,
    String publisher,
    LocalDateTime publishedDate
) {}

/**
 * Service demonstrating advanced search queries
 */
@Service
class BookSearchService {
    
    private final ElasticsearchOperations elasticsearchOperations;
    private static final String INDEX_NAME = "books";
    
    public BookSearchService(ElasticsearchOperations elasticsearchOperations) {
        this.elasticsearchOperations = elasticsearchOperations;
    }
    
    /**
     * Index a book
     */
    public Book indexBook(Book book) {
        IndexQuery indexQuery = new IndexQueryBuilder()
            .withId(book.id())
            .withObject(book)
            .build();
        
        elasticsearchOperations.index(indexQuery, IndexCoordinates.of(INDEX_NAME));
        return book;
    }
    
    /**
     * Full-text search across title and description (match query)
     */
    public List<Book> searchFullText(String query) {
        Criteria criteria = new Criteria("title").matches(query)
            .or(new Criteria("description").matches(query));
        Query searchQuery = new CriteriaQuery(criteria);
        
        SearchHits<Book> searchHits = elasticsearchOperations.search(searchQuery, Book.class, IndexCoordinates.of(INDEX_NAME));
        return searchHits.stream()
            .map(SearchHit::getContent)
            .collect(Collectors.toList());
    }
    
    /**
     * Fuzzy search for typo tolerance (matches similar terms)
     */
    public List<Book> fuzzySearch(String term) {
        Criteria criteria = new Criteria("title").fuzzy(term);
        Query query = new CriteriaQuery(criteria);
        
        SearchHits<Book> searchHits = elasticsearchOperations.search(query, Book.class, IndexCoordinates.of(INDEX_NAME));
        return searchHits.stream()
            .map(SearchHit::getContent)
            .collect(Collectors.toList());
    }
    
    /**
     * Wildcard search (supports * and ? wildcards)
     */
    public List<Book> wildcardSearch(String pattern) {
        Criteria criteria = new Criteria("title").expression(pattern);
        Query query = new CriteriaQuery(criteria);
        
        SearchHits<Book> searchHits = elasticsearchOperations.search(query, Book.class, IndexCoordinates.of(INDEX_NAME));
        return searchHits.stream()
            .map(SearchHit::getContent)
            .collect(Collectors.toList());
    }
    
    /**
     * Prefix search (autocomplete-like)
     */
    public List<Book> prefixSearch(String prefix) {
        Criteria criteria = new Criteria("title").startsWith(prefix);
        Query query = new CriteriaQuery(criteria);
        
        SearchHits<Book> searchHits = elasticsearchOperations.search(query, Book.class, IndexCoordinates.of(INDEX_NAME));
        return searchHits.stream()
            .map(SearchHit::getContent)
            .collect(Collectors.toList());
    }
    
    /**
     * Search by author (exact match)
     */
    public List<Book> searchByAuthor(String author) {
        Criteria criteria = new Criteria("author").is(author);
        Query query = new CriteriaQuery(criteria);
        
        SearchHits<Book> searchHits = elasticsearchOperations.search(query, Book.class, IndexCoordinates.of(INDEX_NAME));
        return searchHits.stream()
            .map(SearchHit::getContent)
            .collect(Collectors.toList());
    }
    
    /**
     * Search by genre
     */
    public List<Book> searchByGenre(String genre) {
        Criteria criteria = new Criteria("genres").contains(genre);
        Query query = new CriteriaQuery(criteria);
        
        SearchHits<Book> searchHits = elasticsearchOperations.search(query, Book.class, IndexCoordinates.of(INDEX_NAME));
        return searchHits.stream()
            .map(SearchHit::getContent)
            .collect(Collectors.toList());
    }
    
    /**
     * Range query - books published in year range
     */
    public List<Book> searchByYearRange(Integer startYear, Integer endYear) {
        Criteria criteria = new Criteria("year").between(startYear, endYear);
        Query query = new CriteriaQuery(criteria);
        
        SearchHits<Book> searchHits = elasticsearchOperations.search(query, Book.class, IndexCoordinates.of(INDEX_NAME));
        return searchHits.stream()
            .map(SearchHit::getContent)
            .collect(Collectors.toList());
    }
    
    /**
     * Search books with rating greater than threshold
     */
    public List<Book> searchByRating(Double minRating) {
        Criteria criteria = new Criteria("rating").greaterThanEqual(minRating);
        Query query = new CriteriaQuery(criteria);
        
        SearchHits<Book> searchHits = elasticsearchOperations.search(query, Book.class, IndexCoordinates.of(INDEX_NAME));
        return searchHits.stream()
            .map(SearchHit::getContent)
            .collect(Collectors.toList());
    }
    
    /**
     * Bool query - multiple conditions (AND/OR)
     * Search books by genre AND rating AND year range
     */
    public List<Book> boolSearch(String genre, Double minRating, Integer startYear, Integer endYear) {
        Criteria criteria = new Criteria("genres").contains(genre)
            .and(new Criteria("rating").greaterThanEqual(minRating))
            .and(new Criteria("year").between(startYear, endYear));
        Query query = new CriteriaQuery(criteria);
        
        SearchHits<Book> searchHits = elasticsearchOperations.search(query, Book.class, IndexCoordinates.of(INDEX_NAME));
        return searchHits.stream()
            .map(SearchHit::getContent)
            .collect(Collectors.toList());
    }
    
    /**
     * Bool query with OR condition
     * Search books by title OR author
     */
    public List<Book> searchByTitleOrAuthor(String titleQuery, String authorQuery) {
        Criteria criteria = new Criteria("title").matches(titleQuery)
            .or(new Criteria("author").is(authorQuery));
        Query query = new CriteriaQuery(criteria);
        
        SearchHits<Book> searchHits = elasticsearchOperations.search(query, Book.class, IndexCoordinates.of(INDEX_NAME));
        return searchHits.stream()
            .map(SearchHit::getContent)
            .collect(Collectors.toList());
    }
    
    /**
     * Search books by publisher
     */
    public List<Book> searchByPublisher(String publisher) {
        Criteria criteria = new Criteria("publisher").is(publisher);
        Query query = new CriteriaQuery(criteria);
        
        SearchHits<Book> searchHits = elasticsearchOperations.search(query, Book.class, IndexCoordinates.of(INDEX_NAME));
        return searchHits.stream()
            .map(SearchHit::getContent)
            .collect(Collectors.toList());
    }
    
    /**
     * Search books with page count range
     */
    public List<Book> searchByPageRange(Integer minPages, Integer maxPages) {
        Criteria criteria = new Criteria("pages").between(minPages, maxPages);
        Query query = new CriteriaQuery(criteria);
        
        SearchHits<Book> searchHits = elasticsearchOperations.search(query, Book.class, IndexCoordinates.of(INDEX_NAME));
        return searchHits.stream()
            .map(SearchHit::getContent)
            .collect(Collectors.toList());
    }
    
    /**
     * Complex bool query: must, should, must_not
     * Find books that:
     * - MUST match genre
     * - SHOULD match title (boosts relevance)
     * - MUST_NOT be below minRating
     */
    public List<Book> complexBoolSearch(String genre, String titleBoost, Double minRating) {
        // Using Criteria for complex bool logic
        Criteria mustCriteria = new Criteria("genres").contains(genre);
        Criteria shouldCriteria = new Criteria("title").matches(titleBoost);
        Criteria mustNotCriteria = new Criteria("rating").lessThan(minRating);
        
        // Combine with AND (must) and NOT
        Criteria finalCriteria = mustCriteria.and(shouldCriteria).and(mustNotCriteria.not());
        Query query = new CriteriaQuery(finalCriteria);
        
        SearchHits<Book> searchHits = elasticsearchOperations.search(query, Book.class, IndexCoordinates.of(INDEX_NAME));
        return searchHits.stream()
            .map(SearchHit::getContent)
            .collect(Collectors.toList());
    }
    
    /**
     * Search with pagination
     */
    public List<Book> searchWithPagination(String query, int page, int size) {
        Criteria criteria = new Criteria("title").matches(query);
        Query searchQuery = new CriteriaQuery(criteria);
        searchQuery.setPageable(org.springframework.data.domain.PageRequest.of(page, size));
        
        SearchHits<Book> searchHits = elasticsearchOperations.search(searchQuery, Book.class, IndexCoordinates.of(INDEX_NAME));
        return searchHits.stream()
            .map(SearchHit::getContent)
            .collect(Collectors.toList());
    }
}

/**
 * REST controller for search operations
 */
@RestController
@RequestMapping("/api/books")
class BookSearchController {
    
    private final BookSearchService bookSearchService;
    
    public BookSearchController(BookSearchService bookSearchService) {
        this.bookSearchService = bookSearchService;
    }
    
    @PostMapping
    public ResponseEntity<Book> indexBook(@RequestBody Book book) {
        return ResponseEntity.ok(bookSearchService.indexBook(book));
    }
    
    @GetMapping("/search/fulltext")
    public ResponseEntity<List<Book>> searchFullText(@RequestParam String query) {
        return ResponseEntity.ok(bookSearchService.searchFullText(query));
    }
    
    @GetMapping("/search/fuzzy")
    public ResponseEntity<List<Book>> fuzzySearch(@RequestParam String term) {
        return ResponseEntity.ok(bookSearchService.fuzzySearch(term));
    }
    
    @GetMapping("/search/wildcard")
    public ResponseEntity<List<Book>> wildcardSearch(@RequestParam String pattern) {
        return ResponseEntity.ok(bookSearchService.wildcardSearch(pattern));
    }
    
    @GetMapping("/search/prefix")
    public ResponseEntity<List<Book>> prefixSearch(@RequestParam String prefix) {
        return ResponseEntity.ok(bookSearchService.prefixSearch(prefix));
    }
    
    @GetMapping("/search/author/{author}")
    public ResponseEntity<List<Book>> searchByAuthor(@PathVariable String author) {
        return ResponseEntity.ok(bookSearchService.searchByAuthor(author));
    }
    
    @GetMapping("/search/genre/{genre}")
    public ResponseEntity<List<Book>> searchByGenre(@PathVariable String genre) {
        return ResponseEntity.ok(bookSearchService.searchByGenre(genre));
    }
    
    @GetMapping("/search/year")
    public ResponseEntity<List<Book>> searchByYearRange(
            @RequestParam Integer start,
            @RequestParam Integer end) {
        return ResponseEntity.ok(bookSearchService.searchByYearRange(start, end));
    }
    
    @GetMapping("/search/rating")
    public ResponseEntity<List<Book>> searchByRating(@RequestParam Double minRating) {
        return ResponseEntity.ok(bookSearchService.searchByRating(minRating));
    }
    
    @GetMapping("/search/bool")
    public ResponseEntity<List<Book>> boolSearch(
            @RequestParam String genre,
            @RequestParam Double minRating,
            @RequestParam Integer startYear,
            @RequestParam Integer endYear) {
        return ResponseEntity.ok(bookSearchService.boolSearch(genre, minRating, startYear, endYear));
    }
    
    @GetMapping("/search/title-or-author")
    public ResponseEntity<List<Book>> searchByTitleOrAuthor(
            @RequestParam String title,
            @RequestParam String author) {
        return ResponseEntity.ok(bookSearchService.searchByTitleOrAuthor(title, author));
    }
    
    @GetMapping("/search/publisher/{publisher}")
    public ResponseEntity<List<Book>> searchByPublisher(@PathVariable String publisher) {
        return ResponseEntity.ok(bookSearchService.searchByPublisher(publisher));
    }
    
    @GetMapping("/search/pages")
    public ResponseEntity<List<Book>> searchByPageRange(
            @RequestParam Integer min,
            @RequestParam Integer max) {
        return ResponseEntity.ok(bookSearchService.searchByPageRange(min, max));
    }
    
    @GetMapping("/search/complex")
    public ResponseEntity<List<Book>> complexBoolSearch(
            @RequestParam String genre,
            @RequestParam String titleBoost,
            @RequestParam Double minRating) {
        return ResponseEntity.ok(bookSearchService.complexBoolSearch(genre, titleBoost, minRating));
    }
    
    @GetMapping("/search/paginated")
    public ResponseEntity<List<Book>> searchWithPagination(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(bookSearchService.searchWithPagination(query, page, size));
    }
    
    @GetMapping("/info")
    public ResponseEntity<String> getInfo() {
        return ResponseEntity.ok("""
            Search Query Pattern
            
            This pattern demonstrates advanced Elasticsearch search capabilities
            including full-text search, fuzzy matching, wildcards, and bool queries.
            
            Query Types:
            - Match queries: Full-text search with relevance scoring
            - Fuzzy queries: Typo-tolerant search (Levenshtein distance)
            - Wildcard queries: Pattern matching (* and ?)
            - Prefix queries: Autocomplete-style search
            - Range queries: Numeric and date ranges
            - Bool queries: Complex combinations (must, should, must_not, filter)
            
            Features:
            - Full-text search across multiple fields
            - Fuzzy matching for typo tolerance
            - Wildcard and prefix search
            - Range queries (year, rating, pages)
            - Bool queries with AND/OR/NOT logic
            - Pagination support
            - Relevance scoring and ranking
            
            Endpoints:
            - POST /api/books - Index book
            - GET /api/books/search/fulltext?query= - Full-text search
            - GET /api/books/search/fuzzy?term= - Fuzzy search
            - GET /api/books/search/wildcard?pattern= - Wildcard search
            - GET /api/books/search/prefix?prefix= - Prefix search
            - GET /api/books/search/author/{author} - Search by author
            - GET /api/books/search/genre/{genre} - Search by genre
            - GET /api/books/search/year?start=&end= - Year range
            - GET /api/books/search/rating?minRating= - By rating
            - GET /api/books/search/bool?genre=&minRating=&startYear=&endYear= - Bool search
            - GET /api/books/search/title-or-author?title=&author= - OR query
            - GET /api/books/search/publisher/{publisher} - By publisher
            - GET /api/books/search/pages?min=&max= - Page range
            - GET /api/books/search/complex?genre=&titleBoost=&minRating= - Complex bool
            - GET /api/books/search/paginated?query=&page=&size= - Paginated search
            """);
    }
}
