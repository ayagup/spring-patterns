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
 * Elasticsearch Template Pattern
 * 
 * Demonstrates the use of ElasticsearchOperations (ElasticsearchTemplate)
 * for low-level document operations with Elasticsearch.
 * 
 * Key concepts:
 * - ElasticsearchOperations for CRUD operations
 * - Index operations (create, delete, exists)
 * - Document operations (index, get, update, delete)
 * - Query operations with Query DSL
 * - Bulk operations
 * - Refresh and flush operations
 * 
 * Use cases:
 * - Full-text search
 * - Document indexing
 * - Custom queries
 * - Bulk data operations
 * - Index management
 */
@SpringBootApplication
public class ElasticsearchTemplatePattern {

    public static void main(String[] args) {
        SpringApplication.run(ElasticsearchTemplatePattern.class, args);
    }
}

/**
 * Article document for Elasticsearch
 */
record Article(
    String id,
    String title,
    String content,
    String author,
    List<String> tags,
    String category,
    Integer views,
    LocalDateTime publishedDate,
    LocalDateTime updatedDate
) {
    public Article {
        if (updatedDate == null) {
            updatedDate = LocalDateTime.now();
        }
    }
}

/**
 * Service demonstrating ElasticsearchOperations
 */
@Service
class ArticleSearchService {
    
    private final ElasticsearchOperations elasticsearchOperations;
    private static final String INDEX_NAME = "articles";
    
    public ArticleSearchService(ElasticsearchOperations elasticsearchOperations) {
        this.elasticsearchOperations = elasticsearchOperations;
    }
    
    /**
     * Index a single article
     */
    public Article indexArticle(Article article) {
        IndexQuery indexQuery = new IndexQueryBuilder()
            .withId(article.id())
            .withObject(article)
            .build();
        
        elasticsearchOperations.index(indexQuery, IndexCoordinates.of(INDEX_NAME));
        return article;
    }
    
    /**
     * Index multiple articles in bulk
     */
    public List<Article> indexArticlesBulk(List<Article> articles) {
        List<IndexQuery> queries = articles.stream()
            .map(article -> new IndexQueryBuilder()
                .withId(article.id())
                .withObject(article)
                .build())
            .collect(Collectors.toList());
        
        elasticsearchOperations.bulkIndex(queries, IndexCoordinates.of(INDEX_NAME));
        return articles;
    }
    
    /**
     * Get article by ID
     */
    public Article getArticle(String id) {
        return elasticsearchOperations.get(id, Article.class, IndexCoordinates.of(INDEX_NAME));
    }
    
    /**
     * Update article
     */
    public Article updateArticle(Article article) {
        UpdateQuery updateQuery = UpdateQuery.builder(article.id())
            .withDocument(
                org.springframework.data.elasticsearch.core.document.Document.create()
            )
            .build();
        
        elasticsearchOperations.update(updateQuery, IndexCoordinates.of(INDEX_NAME));
        return article;
    }
    
    /**
     * Delete article by ID
     */
    public String deleteArticle(String id) {
        return elasticsearchOperations.delete(id, IndexCoordinates.of(INDEX_NAME));
    }
    
    /**
     * Search articles by title (match query)
     */
    public List<Article> searchByTitle(String title) {
        Criteria criteria = new Criteria("title").matches(title);
        Query query = new CriteriaQuery(criteria);
        
        SearchHits<Article> searchHits = elasticsearchOperations.search(query, Article.class, IndexCoordinates.of(INDEX_NAME));
        return searchHits.stream()
            .map(SearchHit::getContent)
            .collect(Collectors.toList());
    }
    
    /**
     * Search articles by content (full-text search)
     */
    public List<Article> searchByContent(String content) {
        Criteria criteria = new Criteria("content").contains(content);
        Query query = new CriteriaQuery(criteria);
        
        SearchHits<Article> searchHits = elasticsearchOperations.search(query, Article.class, IndexCoordinates.of(INDEX_NAME));
        return searchHits.stream()
            .map(SearchHit::getContent)
            .collect(Collectors.toList());
    }
    
    /**
     * Search articles by author
     */
    public List<Article> searchByAuthor(String author) {
        Criteria criteria = new Criteria("author").is(author);
        Query query = new CriteriaQuery(criteria);
        
        SearchHits<Article> searchHits = elasticsearchOperations.search(query, Article.class, IndexCoordinates.of(INDEX_NAME));
        return searchHits.stream()
            .map(SearchHit::getContent)
            .collect(Collectors.toList());
    }
    
    /**
     * Search articles by tag
     */
    public List<Article> searchByTag(String tag) {
        Criteria criteria = new Criteria("tags").contains(tag);
        Query query = new CriteriaQuery(criteria);
        
        SearchHits<Article> searchHits = elasticsearchOperations.search(query, Article.class, IndexCoordinates.of(INDEX_NAME));
        return searchHits.stream()
            .map(SearchHit::getContent)
            .collect(Collectors.toList());
    }
    
    /**
     * Search articles by category
     */
    public List<Article> searchByCategory(String category) {
        Criteria criteria = new Criteria("category").is(category);
        Query query = new CriteriaQuery(criteria);
        
        SearchHits<Article> searchHits = elasticsearchOperations.search(query, Article.class, IndexCoordinates.of(INDEX_NAME));
        return searchHits.stream()
            .map(SearchHit::getContent)
            .collect(Collectors.toList());
    }
    
    /**
     * Search articles with views greater than threshold
     */
    public List<Article> searchByViewsGreaterThan(Integer views) {
        Criteria criteria = new Criteria("views").greaterThan(views);
        Query query = new CriteriaQuery(criteria);
        
        SearchHits<Article> searchHits = elasticsearchOperations.search(query, Article.class, IndexCoordinates.of(INDEX_NAME));
        return searchHits.stream()
            .map(SearchHit::getContent)
            .collect(Collectors.toList());
    }
    
    /**
     * Search all articles
     */
    public List<Article> searchAll() {
        Query query = Query.findAll();
        
        SearchHits<Article> searchHits = elasticsearchOperations.search(query, Article.class, IndexCoordinates.of(INDEX_NAME));
        return searchHits.stream()
            .map(SearchHit::getContent)
            .collect(Collectors.toList());
    }
    
    /**
     * Count total articles
     */
    public long count() {
        Query query = Query.findAll();
        return elasticsearchOperations.count(query, IndexCoordinates.of(INDEX_NAME));
    }
    
    /**
     * Check if article exists
     */
    public boolean exists(String id) {
        return elasticsearchOperations.exists(id, IndexCoordinates.of(INDEX_NAME));
    }
    
    /**
     * Delete all articles
     */
    public void deleteAll() {
        Query query = Query.findAll();
        elasticsearchOperations.delete(query, Article.class, IndexCoordinates.of(INDEX_NAME));
    }
}

/**
 * REST controller for article search operations
 */
@RestController
@RequestMapping("/api/articles")
class ArticleSearchController {
    
    private final ArticleSearchService articleSearchService;
    
    public ArticleSearchController(ArticleSearchService articleSearchService) {
        this.articleSearchService = articleSearchService;
    }
    
    @PostMapping
    public ResponseEntity<Article> indexArticle(@RequestBody Article article) {
        return ResponseEntity.ok(articleSearchService.indexArticle(article));
    }
    
    @PostMapping("/bulk")
    public ResponseEntity<List<Article>> indexArticlesBulk(@RequestBody List<Article> articles) {
        return ResponseEntity.ok(articleSearchService.indexArticlesBulk(articles));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Article> getArticle(@PathVariable String id) {
        Article article = articleSearchService.getArticle(id);
        return article != null ? ResponseEntity.ok(article) : ResponseEntity.notFound().build();
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Article> updateArticle(@PathVariable String id, @RequestBody Article article) {
        Article updated = new Article(id, article.title(), article.content(), article.author(),
                                     article.tags(), article.category(), article.views(),
                                     article.publishedDate(), LocalDateTime.now());
        return ResponseEntity.ok(articleSearchService.updateArticle(updated));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteArticle(@PathVariable String id) {
        return ResponseEntity.ok(articleSearchService.deleteArticle(id));
    }
    
    @GetMapping("/search/title")
    public ResponseEntity<List<Article>> searchByTitle(@RequestParam String title) {
        return ResponseEntity.ok(articleSearchService.searchByTitle(title));
    }
    
    @GetMapping("/search/content")
    public ResponseEntity<List<Article>> searchByContent(@RequestParam String content) {
        return ResponseEntity.ok(articleSearchService.searchByContent(content));
    }
    
    @GetMapping("/search/author")
    public ResponseEntity<List<Article>> searchByAuthor(@RequestParam String author) {
        return ResponseEntity.ok(articleSearchService.searchByAuthor(author));
    }
    
    @GetMapping("/search/tag")
    public ResponseEntity<List<Article>> searchByTag(@RequestParam String tag) {
        return ResponseEntity.ok(articleSearchService.searchByTag(tag));
    }
    
    @GetMapping("/search/category")
    public ResponseEntity<List<Article>> searchByCategory(@RequestParam String category) {
        return ResponseEntity.ok(articleSearchService.searchByCategory(category));
    }
    
    @GetMapping("/search/views")
    public ResponseEntity<List<Article>> searchByViewsGreaterThan(@RequestParam Integer views) {
        return ResponseEntity.ok(articleSearchService.searchByViewsGreaterThan(views));
    }
    
    @GetMapping("/search/all")
    public ResponseEntity<List<Article>> searchAll() {
        return ResponseEntity.ok(articleSearchService.searchAll());
    }
    
    @GetMapping("/count")
    public ResponseEntity<Long> count() {
        return ResponseEntity.ok(articleSearchService.count());
    }
    
    @GetMapping("/{id}/exists")
    public ResponseEntity<Boolean> exists(@PathVariable String id) {
        return ResponseEntity.ok(articleSearchService.exists(id));
    }
    
    @DeleteMapping("/all")
    public ResponseEntity<Void> deleteAll() {
        articleSearchService.deleteAll();
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/info")
    public ResponseEntity<String> getInfo() {
        return ResponseEntity.ok("""
            Elasticsearch Template Pattern
            
            This pattern demonstrates the use of ElasticsearchOperations for low-level
            document operations with Elasticsearch.
            
            Features:
            - Document indexing (single and bulk)
            - Full-text search with Criteria queries
            - Match queries for title search
            - Contains queries for content search
            - Exact match for author and category
            - Range queries for views
            - Count and exists operations
            - Update and delete operations
            
            Endpoints:
            - POST /api/articles - Index single article
            - POST /api/articles/bulk - Index multiple articles
            - GET /api/articles/{id} - Get article by ID
            - PUT /api/articles/{id} - Update article
            - DELETE /api/articles/{id} - Delete article
            - GET /api/articles/search/title?title= - Search by title
            - GET /api/articles/search/content?content= - Search by content
            - GET /api/articles/search/author?author= - Search by author
            - GET /api/articles/search/tag?tag= - Search by tag
            - GET /api/articles/search/category?category= - Search by category
            - GET /api/articles/search/views?views= - Search by views
            - GET /api/articles/search/all - Get all articles
            - GET /api/articles/count - Count articles
            - GET /api/articles/{id}/exists - Check exists
            - DELETE /api/articles/all - Delete all articles
            """);
    }
}
