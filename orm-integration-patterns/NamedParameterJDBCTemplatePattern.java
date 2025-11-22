package com.example.orm.integration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Named Parameter JDBC Template Pattern
 * 
 * Purpose:
 * - Named parameters instead of positional (?)
 * - More readable SQL queries
 * - Better parameter management
 * - SqlParameterSource support
 * 
 * Features:
 * 1. Named parameters (:paramName)
 * 2. MapSqlParameterSource
 * 3. BeanPropertySqlParameterSource
 * 4. SqlParameterSource
 * 5. Batch operations with named params
 * 6. Better code readability
 * 7. Type-safe parameter binding
 * 
 * When to Use:
 * - Complex queries with many parameters
 * - IN clauses with variable lists
 * - Better code maintainability needed
 * - Working with DTOs/beans
 * - Dynamic parameter binding
 * 
 * Benefits:
 * - Self-documenting SQL
 * - Easier to maintain
 * - No parameter order issues
 * - Flexible parameter binding
 * - Bean property mapping
 */
@SpringBootApplication
public class NamedParameterJDBCTemplatePattern {

    public static void main(String[] args) {
        SpringApplication.run(NamedParameterJDBCTemplatePattern.class, args);
        System.out.println("Named Parameter JDBC Template Pattern Application Started!");
        System.out.println("Visit: http://localhost:8080/api/named-jdbc/books");
    }

    /**
     * Book Entity
     */
    public static class Book {
        private Long id;
        private String title;
        private String author;
        private String isbn;
        private String genre;
        private Double price;
        private Integer publishedYear;
        private Boolean available;

        // Constructors
        public Book() {}

        public Book(String title, String author, String isbn, String genre, 
                   Double price, Integer publishedYear) {
            this.title = title;
            this.author = author;
            this.isbn = isbn;
            this.genre = genre;
            this.price = price;
            this.publishedYear = publishedYear;
            this.available = true;
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
        
        public String getGenre() { return genre; }
        public void setGenre(String genre) { this.genre = genre; }
        
        public Double getPrice() { return price; }
        public void setPrice(Double price) { this.price = price; }
        
        public Integer getPublishedYear() { return publishedYear; }
        public void setPublishedYear(Integer publishedYear) { this.publishedYear = publishedYear; }
        
        public Boolean getAvailable() { return available; }
        public void setAvailable(Boolean available) { this.available = available; }
    }

    /**
     * Named Parameter JDBC Configuration
     */
    @Configuration
    public static class NamedJdbcConfig {

        @Bean
        public NamedParameterJdbcTemplate namedParameterJdbcTemplate(DataSource dataSource) {
            return new NamedParameterJdbcTemplate(dataSource);
        }

        @Bean
        public DataSourceTransactionManager transactionManager(DataSource dataSource) {
            return new DataSourceTransactionManager(dataSource);
        }
    }

    /**
     * Book Repository using NamedParameterJdbcTemplate
     */
    @Repository
    public static class BookRepository {

        private final NamedParameterJdbcTemplate namedJdbcTemplate;

        public BookRepository(NamedParameterJdbcTemplate namedJdbcTemplate) {
            this.namedJdbcTemplate = namedJdbcTemplate;
            initializeTable();
        }

        private void initializeTable() {
            String sql = "CREATE TABLE IF NOT EXISTS books (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "title VARCHAR(200) NOT NULL, " +
                    "author VARCHAR(100) NOT NULL, " +
                    "isbn VARCHAR(20) UNIQUE, " +
                    "genre VARCHAR(50), " +
                    "price DECIMAL(10,2), " +
                    "published_year INT, " +
                    "available BOOLEAN DEFAULT TRUE)";
            namedJdbcTemplate.getJdbcTemplate().execute(sql);
        }

        /**
         * Save using BeanPropertySqlParameterSource
         */
        @Transactional
        public Book save(Book book) {
            String sql = "INSERT INTO books (title, author, isbn, genre, price, published_year, available) " +
                    "VALUES (:title, :author, :isbn, :genre, :price, :publishedYear, :available)";
            
            SqlParameterSource params = new BeanPropertySqlParameterSource(book);
            KeyHolder keyHolder = new GeneratedKeyHolder();
            
            namedJdbcTemplate.update(sql, params, keyHolder);
            book.setId(keyHolder.getKey().longValue());
            
            return book;
        }

        /**
         * Find by ID using MapSqlParameterSource
         */
        public Optional<Book> findById(Long id) {
            String sql = "SELECT * FROM books WHERE id = :id";
            
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("id", id);
            
            try {
                Book book = namedJdbcTemplate.queryForObject(sql, params, this::mapRow);
                return Optional.ofNullable(book);
            } catch (Exception e) {
                return Optional.empty();
            }
        }

        /**
         * Find all
         */
        public List<Book> findAll() {
            String sql = "SELECT * FROM books";
            return namedJdbcTemplate.query(sql, this::mapRow);
        }

        /**
         * Find by author using named parameter
         */
        public List<Book> findByAuthor(String author) {
            String sql = "SELECT * FROM books WHERE author LIKE :author";
            
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("author", "%" + author + "%");
            
            return namedJdbcTemplate.query(sql, params, this::mapRow);
        }

        /**
         * Find by multiple genres using IN clause
         */
        public List<Book> findByGenres(List<String> genres) {
            String sql = "SELECT * FROM books WHERE genre IN (:genres)";
            
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("genres", genres);
            
            return namedJdbcTemplate.query(sql, params, this::mapRow);
        }

        /**
         * Find by price range using Map
         */
        public List<Book> findByPriceRange(Double minPrice, Double maxPrice) {
            String sql = "SELECT * FROM books WHERE price BETWEEN :minPrice AND :maxPrice";
            
            Map<String, Object> params = new HashMap<>();
            params.put("minPrice", minPrice);
            params.put("maxPrice", maxPrice);
            
            return namedJdbcTemplate.query(sql, params, this::mapRow);
        }

        /**
         * Complex search with multiple optional parameters
         */
        public List<Book> search(String title, String author, String genre, 
                                Integer yearFrom, Integer yearTo, Boolean available) {
            StringBuilder sql = new StringBuilder("SELECT * FROM books WHERE 1=1");
            MapSqlParameterSource params = new MapSqlParameterSource();
            
            if (title != null && !title.isEmpty()) {
                sql.append(" AND title LIKE :title");
                params.addValue("title", "%" + title + "%");
            }
            if (author != null && !author.isEmpty()) {
                sql.append(" AND author LIKE :author");
                params.addValue("author", "%" + author + "%");
            }
            if (genre != null && !genre.isEmpty()) {
                sql.append(" AND genre = :genre");
                params.addValue("genre", genre);
            }
            if (yearFrom != null) {
                sql.append(" AND published_year >= :yearFrom");
                params.addValue("yearFrom", yearFrom);
            }
            if (yearTo != null) {
                sql.append(" AND published_year <= :yearTo");
                params.addValue("yearTo", yearTo);
            }
            if (available != null) {
                sql.append(" AND available = :available");
                params.addValue("available", available);
            }
            
            return namedJdbcTemplate.query(sql.toString(), params, this::mapRow);
        }

        /**
         * Update using BeanPropertySqlParameterSource
         */
        @Transactional
        public int update(Book book) {
            String sql = "UPDATE books SET title = :title, author = :author, " +
                    "isbn = :isbn, genre = :genre, price = :price, " +
                    "published_year = :publishedYear, available = :available " +
                    "WHERE id = :id";
            
            SqlParameterSource params = new BeanPropertySqlParameterSource(book);
            return namedJdbcTemplate.update(sql, params);
        }

        /**
         * Update specific fields
         */
        @Transactional
        public int updatePrice(Long id, Double newPrice) {
            String sql = "UPDATE books SET price = :price WHERE id = :id";
            
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("id", id);
            params.addValue("price", newPrice);
            
            return namedJdbcTemplate.update(sql, params);
        }

        /**
         * Batch insert using SqlParameterSourceUtils
         */
        @Transactional
        public int[] batchInsert(List<Book> books) {
            String sql = "INSERT INTO books (title, author, isbn, genre, price, published_year, available) " +
                    "VALUES (:title, :author, :isbn, :genre, :price, :publishedYear, :available)";
            
            SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(books.toArray());
            return namedJdbcTemplate.batchUpdate(sql, batch);
        }

        /**
         * Batch update using MapSqlParameterSource array
         */
        @Transactional
        public int[] batchUpdatePrices(Map<Long, Double> idPriceMap) {
            String sql = "UPDATE books SET price = :price WHERE id = :id";
            
            List<MapSqlParameterSource> batchParams = new ArrayList<>();
            for (Map.Entry<Long, Double> entry : idPriceMap.entrySet()) {
                MapSqlParameterSource params = new MapSqlParameterSource();
                params.addValue("id", entry.getKey());
                params.addValue("price", entry.getValue());
                batchParams.add(params);
            }
            
            return namedJdbcTemplate.batchUpdate(sql, 
                    batchParams.toArray(new MapSqlParameterSource[0]));
        }

        /**
         * Delete by ID
         */
        @Transactional
        public int delete(Long id) {
            String sql = "DELETE FROM books WHERE id = :id";
            
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("id", id);
            
            return namedJdbcTemplate.update(sql, params);
        }

        /**
         * Delete by IDs (IN clause)
         */
        @Transactional
        public int deleteByIds(List<Long> ids) {
            String sql = "DELETE FROM books WHERE id IN (:ids)";
            
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("ids", ids);
            
            return namedJdbcTemplate.update(sql, params);
        }

        /**
         * Count with parameters
         */
        public Long countByGenre(String genre) {
            String sql = "SELECT COUNT(*) FROM books WHERE genre = :genre";
            
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("genre", genre);
            
            return namedJdbcTemplate.queryForObject(sql, params, Long.class);
        }

        /**
         * Get genres with book count
         */
        public Map<String, Integer> getGenreCount() {
            String sql = "SELECT genre, COUNT(*) as count FROM books GROUP BY genre";
            
            return namedJdbcTemplate.query(sql, rs -> {
                Map<String, Integer> result = new HashMap<>();
                while (rs.next()) {
                    result.put(rs.getString("genre"), rs.getInt("count"));
                }
                return result;
            });
        }

        /**
         * Row Mapper
         */
        private Book mapRow(ResultSet rs, int rowNum) throws SQLException {
            Book book = new Book();
            book.setId(rs.getLong("id"));
            book.setTitle(rs.getString("title"));
            book.setAuthor(rs.getString("author"));
            book.setIsbn(rs.getString("isbn"));
            book.setGenre(rs.getString("genre"));
            book.setPrice(rs.getDouble("price"));
            book.setPublishedYear(rs.getInt("published_year"));
            book.setAvailable(rs.getBoolean("available"));
            return book;
        }
    }

    /**
     * Book Service
     */
    @Service
    public static class BookService {

        private final BookRepository repository;

        public BookService(BookRepository repository) {
            this.repository = repository;
        }

        public Book createBook(Book book) {
            return repository.save(book);
        }

        public void batchCreateBooks(List<Book> books) {
            repository.batchInsert(books);
        }

        public Optional<Book> getBook(Long id) {
            return repository.findById(id);
        }

        public List<Book> getAllBooks() {
            return repository.findAll();
        }

        public List<Book> searchByAuthor(String author) {
            return repository.findByAuthor(author);
        }

        public List<Book> searchByGenres(List<String> genres) {
            return repository.findByGenres(genres);
        }

        public List<Book> searchByPriceRange(Double min, Double max) {
            return repository.findByPriceRange(min, max);
        }

        public List<Book> advancedSearch(String title, String author, String genre,
                                        Integer yearFrom, Integer yearTo, Boolean available) {
            return repository.search(title, author, genre, yearFrom, yearTo, available);
        }

        public void updateBook(Book book) {
            repository.update(book);
        }

        public void updatePrice(Long id, Double price) {
            repository.updatePrice(id, price);
        }

        public void deleteBook(Long id) {
            repository.delete(id);
        }

        public void deleteBooks(List<Long> ids) {
            repository.deleteByIds(ids);
        }

        public Long countByGenre(String genre) {
            return repository.countByGenre(genre);
        }

        public Map<String, Integer> getGenreStatistics() {
            return repository.getGenreCount();
        }
    }

    /**
     * REST Controller
     */
    @RestController
    @RequestMapping("/api/named-jdbc")
    public static class BookController {

        private final BookService service;

        public BookController(BookService service) {
            this.service = service;
        }

        @PostMapping("/books")
        public Book createBook(@RequestBody Book book) {
            return service.createBook(book);
        }

        @PostMapping("/books/batch")
        public void batchCreate(@RequestBody List<Book> books) {
            service.batchCreateBooks(books);
        }

        @GetMapping("/books/{id}")
        public Optional<Book> getBook(@PathVariable Long id) {
            return service.getBook(id);
        }

        @GetMapping("/books")
        public List<Book> getAllBooks() {
            return service.getAllBooks();
        }

        @GetMapping("/books/author/{author}")
        public List<Book> searchByAuthor(@PathVariable String author) {
            return service.searchByAuthor(author);
        }

        @PostMapping("/books/genres")
        public List<Book> searchByGenres(@RequestBody List<String> genres) {
            return service.searchByGenres(genres);
        }

        @GetMapping("/books/price-range")
        public List<Book> searchByPrice(@RequestParam Double min, @RequestParam Double max) {
            return service.searchByPriceRange(min, max);
        }

        @GetMapping("/books/search")
        public List<Book> advancedSearch(
                @RequestParam(required = false) String title,
                @RequestParam(required = false) String author,
                @RequestParam(required = false) String genre,
                @RequestParam(required = false) Integer yearFrom,
                @RequestParam(required = false) Integer yearTo,
                @RequestParam(required = false) Boolean available) {
            return service.advancedSearch(title, author, genre, yearFrom, yearTo, available);
        }

        @PutMapping("/books/{id}")
        public void updateBook(@PathVariable Long id, @RequestBody Book book) {
            book.setId(id);
            service.updateBook(book);
        }

        @PutMapping("/books/{id}/price")
        public void updatePrice(@PathVariable Long id, @RequestParam Double price) {
            service.updatePrice(id, price);
        }

        @DeleteMapping("/books/{id}")
        public void deleteBook(@PathVariable Long id) {
            service.deleteBook(id);
        }

        @DeleteMapping("/books/batch")
        public void deleteBooks(@RequestBody List<Long> ids) {
            service.deleteBooks(ids);
        }

        @GetMapping("/books/genre/{genre}/count")
        public Long countByGenre(@PathVariable String genre) {
            return service.countByGenre(genre);
        }

        @GetMapping("/books/genre-statistics")
        public Map<String, Integer> getGenreStats() {
            return service.getGenreStatistics();
        }
    }
}

/**
 * Best Practices:
 * 
 * 1. Use BeanPropertySqlParameterSource for entity objects
 * 2. Use MapSqlParameterSource for dynamic parameters
 * 3. Use IN clause with lists for multiple values
 * 4. Build dynamic SQL with MapSqlParameterSource
 * 5. Use SqlParameterSourceUtils for batch operations
 * 6. Name parameters descriptively
 * 7. Handle null values in parameter sources
 * 8. Use EmptySqlParameterSource when no parameters needed
 */
