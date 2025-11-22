package com.example.api.richardson;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Richardson Maturity Model Pattern
 * 
 * Purpose: Defines 4 levels of REST maturity, from basic HTTP to full HATEOAS.
 * 
 * Levels:
 * Level 0 - The Swamp of POX (Plain Old XML)
 *   - Single URI, single HTTP method (usually POST)
 *   - RPC-style, tunneling everything through HTTP
 * 
 * Level 1 - Resources
 *   - Multiple URIs, but single HTTP method
 *   - Each resource has its own endpoint
 * 
 * Level 2 - HTTP Verbs
 *   - Multiple URIs, multiple HTTP methods
 *   - Proper use of GET, POST, PUT, DELETE
 *   - HTTP status codes used correctly
 * 
 * Level 3 - Hypermedia Controls (HATEOAS)
 *   - Responses include links to related resources
 *   - Self-describing, discoverable API
 * 
 * Features:
 * - Progressive enhancement from Level 0 to Level 3
 * - Each level builds on previous level
 * - Level 2 is minimum for RESTful APIs
 */

// Book Entity
class Book {
    private Long id;
    private String title;
    private String author;
    private String isbn;
    private Double price;
    
    public Book() {}
    
    public Book(Long id, String title, String author, String isbn, Double price) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.price = price;
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
}

// Book Service
class BookService {
    private final Map<Long, Book> books = new ConcurrentHashMap<>();
    private Long nextId = 1L;
    
    public BookService() {
        books.put(nextId, new Book(nextId++, "Clean Code", "Robert Martin", "978-0132350884", 44.99));
        books.put(nextId, new Book(nextId++, "Design Patterns", "Gang of Four", "978-0201633610", 54.99));
    }
    
    public List<Book> findAll() {
        return new ArrayList<>(books.values());
    }
    
    public Book findById(Long id) {
        return books.get(id);
    }
    
    public Book create(Book book) {
        book.setId(nextId++);
        books.put(book.getId(), book);
        return book;
    }
    
    public Book update(Book book) {
        books.put(book.getId(), book);
        return book;
    }
    
    public void delete(Long id) {
        books.remove(id);
    }
}

/**
 * Level 0: The Swamp of POX
 * Single endpoint /api, all operations via POST
 */
@RestController
@RequestMapping("/level0/api")
class Level0Controller {
    private final BookService service = new BookService();
    
    @PostMapping
    public ResponseEntity<Map<String, Object>> handleRequest(@RequestBody Map<String, Object> request) {
        String action = (String) request.get("action");
        Map<String, Object> response = new HashMap<>();
        
        switch (action) {
            case "getBooks":
                response.put("books", service.findAll());
                break;
            case "getBook":
                Long id = Long.valueOf(request.get("id").toString());
                response.put("book", service.findById(id));
                break;
            case "createBook":
                Map<String, Object> bookData = (Map<String, Object>) request.get("book");
                Book newBook = mapToBook(bookData);
                response.put("book", service.create(newBook));
                break;
            case "updateBook":
                Map<String, Object> updateData = (Map<String, Object>) request.get("book");
                Book updateBook = mapToBook(updateData);
                response.put("book", service.update(updateBook));
                break;
            case "deleteBook":
                Long deleteId = Long.valueOf(request.get("id").toString());
                service.delete(deleteId);
                response.put("success", true);
                break;
            default:
                response.put("error", "Unknown action");
        }
        
        return ResponseEntity.ok(response);
    }
    
    private Book mapToBook(Map<String, Object> data) {
        Book book = new Book();
        if (data.containsKey("id")) book.setId(Long.valueOf(data.get("id").toString()));
        if (data.containsKey("title")) book.setTitle((String) data.get("title"));
        if (data.containsKey("author")) book.setAuthor((String) data.get("author"));
        if (data.containsKey("isbn")) book.setIsbn((String) data.get("isbn"));
        if (data.containsKey("price")) book.setPrice(Double.valueOf(data.get("price").toString()));
        return book;
    }
}

/**
 * Level 1: Resources
 * Multiple resources, but still using POST for everything
 */
@RestController
@RequestMapping("/level1")
class Level1Controller {
    private final BookService service = new BookService();
    
    @PostMapping("/books/list")
    public ResponseEntity<List<Book>> getAllBooks() {
        return ResponseEntity.ok(service.findAll());
    }
    
    @PostMapping("/books/get")
    public ResponseEntity<Book> getBook(@RequestBody Map<String, Long> request) {
        Book book = service.findById(request.get("id"));
        return book != null ? ResponseEntity.ok(book) : ResponseEntity.notFound().build();
    }
    
    @PostMapping("/books/create")
    public ResponseEntity<Book> createBook(@RequestBody Book book) {
        Book created = service.create(book);
        return ResponseEntity.ok(created);
    }
    
    @PostMapping("/books/update")
    public ResponseEntity<Book> updateBook(@RequestBody Book book) {
        Book updated = service.update(book);
        return ResponseEntity.ok(updated);
    }
    
    @PostMapping("/books/delete")
    public ResponseEntity<Void> deleteBook(@RequestBody Map<String, Long> request) {
        service.delete(request.get("id"));
        return ResponseEntity.ok().build();
    }
}

/**
 * Level 2: HTTP Verbs
 * Proper use of HTTP methods and status codes
 */
@RestController
@RequestMapping("/level2/books")
class Level2Controller {
    private final BookService service = new BookService();
    
    @GetMapping
    public ResponseEntity<List<Book>> getAllBooks() {
        return ResponseEntity.ok(service.findAll());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Book> getBook(@PathVariable Long id) {
        Book book = service.findById(id);
        return book != null ? ResponseEntity.ok(book) : ResponseEntity.notFound().build();
    }
    
    @PostMapping
    public ResponseEntity<Book> createBook(@RequestBody Book book) {
        Book created = service.create(book);
        return ResponseEntity.status(HttpStatus.CREATED)
            .header("Location", "/level2/books/" + created.getId())
            .body(created);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Book> updateBook(@PathVariable Long id, @RequestBody Book book) {
        book.setId(id);
        Book updated = service.update(book);
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

/**
 * Level 3: Hypermedia Controls (HATEOAS)
 * Includes links to related resources
 */
class BookResource {
    private Book book;
    private Map<String, String> links;
    
    public BookResource(Book book) {
        this.book = book;
        this.links = new HashMap<>();
        
        // Self link
        links.put("self", "/level3/books/" + book.getId());
        // Collection link
        links.put("collection", "/level3/books");
        // Update link
        links.put("update", "/level3/books/" + book.getId());
        // Delete link
        links.put("delete", "/level3/books/" + book.getId());
        // Author books link
        links.put("authorBooks", "/level3/books?author=" + book.getAuthor());
    }
    
    public Book getBook() { return book; }
    public Map<String, String> getLinks() { return links; }
}

@RestController
@RequestMapping("/level3/books")
class Level3Controller {
    private final BookService service = new BookService();
    
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllBooks() {
        List<BookResource> resources = new ArrayList<>();
        for (Book book : service.findAll()) {
            resources.add(new BookResource(book));
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("books", resources);
        
        Map<String, String> links = new HashMap<>();
        links.put("self", "/level3/books");
        links.put("create", "/level3/books");
        response.put("_links", links);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<BookResource> getBook(@PathVariable Long id) {
        Book book = service.findById(id);
        return book != null ? 
            ResponseEntity.ok(new BookResource(book)) : 
            ResponseEntity.notFound().build();
    }
    
    @PostMapping
    public ResponseEntity<BookResource> createBook(@RequestBody Book book) {
        Book created = service.create(book);
        return ResponseEntity.status(HttpStatus.CREATED)
            .header("Location", "/level3/books/" + created.getId())
            .body(new BookResource(created));
    }
}

/**
 * Demonstration of Richardson Maturity Model
 */
public class RichardsonMaturityModelPattern {
    
    public static void main(String[] args) {
        System.out.println("=== Richardson Maturity Model Demo ===\n");
        
        System.out.println("Level 0: The Swamp of POX");
        System.out.println("  Endpoint: POST /level0/api");
        System.out.println("  Request: { \"action\": \"getBooks\" }");
        System.out.println("  Request: { \"action\": \"createBook\", \"book\": {...} }");
        System.out.println("  Characteristics:");
        System.out.println("    - Single endpoint");
        System.out.println("    - All operations via POST");
        System.out.println("    - Action specified in request body");
        System.out.println("    - RPC-style, not RESTful");
        System.out.println("  Drawbacks:");
        System.out.println("    - No caching (everything is POST)");
        System.out.println("    - Not idempotent");
        System.out.println("    - Difficult to understand API");
        
        System.out.println("\nLevel 1: Resources");
        System.out.println("  Endpoints:");
        System.out.println("    POST /level1/books/list");
        System.out.println("    POST /level1/books/get");
        System.out.println("    POST /level1/books/create");
        System.out.println("  Characteristics:");
        System.out.println("    - Multiple URIs for different resources");
        System.out.println("    - Still using POST for everything");
        System.out.println("    - Resource-oriented structure");
        System.out.println("  Improvements over Level 0:");
        System.out.println("    - Better organization");
        System.out.println("    - Clearer API structure");
        System.out.println("  Drawbacks:");
        System.out.println("    - Still no caching");
        System.out.println("    - Not using HTTP verbs correctly");
        
        System.out.println("\nLevel 2: HTTP Verbs");
        System.out.println("  Endpoints:");
        System.out.println("    GET    /level2/books        - List all books");
        System.out.println("    GET    /level2/books/{id}   - Get specific book");
        System.out.println("    POST   /level2/books        - Create book");
        System.out.println("    PUT    /level2/books/{id}   - Update book");
        System.out.println("    DELETE /level2/books/{id}   - Delete book");
        System.out.println("  Characteristics:");
        System.out.println("    - Proper HTTP verbs (GET, POST, PUT, DELETE)");
        System.out.println("    - Correct status codes (200, 201, 204, 404)");
        System.out.println("    - Idempotent operations (GET, PUT, DELETE)");
        System.out.println("  Improvements over Level 1:");
        System.out.println("    - GET requests can be cached");
        System.out.println("    - Idempotency for safe operations");
        System.out.println("    - Better HTTP semantics");
        System.out.println("  Note: This is the minimum for RESTful APIs");
        
        System.out.println("\nLevel 3: Hypermedia Controls (HATEOAS)");
        System.out.println("  Same endpoints as Level 2, but responses include links");
        System.out.println("  Example response:");
        System.out.println("  {");
        System.out.println("    \"book\": {");
        System.out.println("      \"id\": 1,");
        System.out.println("      \"title\": \"Clean Code\"");
        System.out.println("    },");
        System.out.println("    \"_links\": {");
        System.out.println("      \"self\": \"/level3/books/1\",");
        System.out.println("      \"collection\": \"/level3/books\",");
        System.out.println("      \"update\": \"/level3/books/1\",");
        System.out.println("      \"delete\": \"/level3/books/1\",");
        System.out.println("      \"authorBooks\": \"/level3/books?author=Robert+Martin\"");
        System.out.println("    }");
        System.out.println("  }");
        System.out.println("  Characteristics:");
        System.out.println("    - Hypermedia links in responses");
        System.out.println("    - Self-describing API");
        System.out.println("    - Discoverable resources");
        System.out.println("  Improvements over Level 2:");
        System.out.println("    - Clients don't hardcode URIs");
        System.out.println("    - API is self-documenting");
        System.out.println("    - Easier to evolve API");
        
        System.out.println("\n=== Maturity Level Comparison ===");
        System.out.println("┌────────┬──────────────┬─────────────┬──────────────┬──────────┐");
        System.out.println("│ Level  │ URIs         │ HTTP Verbs  │ Status Codes │ HATEOAS  │");
        System.out.println("├────────┼──────────────┼─────────────┼──────────────┼──────────┤");
        System.out.println("│ 0      │ Single       │ POST only   │ Usually 200  │ No       │");
        System.out.println("│ 1      │ Multiple     │ POST only   │ Usually 200  │ No       │");
        System.out.println("│ 2      │ Multiple     │ GET/POST/   │ Proper codes │ No       │");
        System.out.println("│        │              │ PUT/DELETE  │              │          │");
        System.out.println("│ 3      │ Multiple     │ GET/POST/   │ Proper codes │ Yes      │");
        System.out.println("│        │              │ PUT/DELETE  │              │          │");
        System.out.println("└────────┴──────────────┴─────────────┴──────────────┴──────────┘");
        
        System.out.println("\n=== Recommendations ===");
        System.out.println("✓ Minimum: Implement Level 2 for any RESTful API");
        System.out.println("✓ Ideal: Implement Level 3 for public APIs");
        System.out.println("✓ Avoid: Levels 0 and 1 for new APIs");
        System.out.println("✓ Migration: Gradually move from lower to higher levels");
    }
}
