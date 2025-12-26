package com.example.pagination.hypermedia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.hateoas.*;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.*;
import java.util.*;

/**
 * Hypermedia/HATEOAS Pagination Pattern
 * 
 * Pagination with hypermedia links (HAL format).
 * Provides navigational links (first, last, next, prev).
 * 
 * Dependencies:
 * - spring-boot-starter-hateoas
 */
@SpringBootApplication
public class HypermediaPaginationPattern {

    public static void main(String[] args) {
        SpringApplication.run(HypermediaPaginationPattern.class, args);
    }

    @Entity
    @Table(name = "articles")
    public static class Article {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        private String title;
        private String content;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }

    public interface ArticleRepository extends JpaRepository<Article, Long> {}

    @RestController
    @RequestMapping("/api/articles")
    public static class ArticleController {

        private final ArticleRepository repository;

        public ArticleController(ArticleRepository repository) {
            this.repository = repository;
        }

        /**
         * HATEOAS pagination with links
         */
        @GetMapping
        public PagedModel<EntityModel<Article>> getArticles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
        ) {
            Pageable pageable = PageRequest.of(page, size);
            Page<Article> articlePage = repository.findAll(pageable);
            
            // Convert entities to EntityModel
            List<EntityModel<Article>> articles = articlePage.getContent().stream()
                .map(article -> EntityModel.of(article,
                    WebMvcLinkBuilder.linkTo(
                        WebMvcLinkBuilder.methodOn(ArticleController.class)
                            .getArticle(article.getId())
                    ).withSelfRel()
                ))
                .toList();
            
            // Create PagedModel with metadata and links
            PagedModel.PageMetadata metadata = new PagedModel.PageMetadata(
                articlePage.getSize(),
                articlePage.getNumber(),
                articlePage.getTotalElements(),
                articlePage.getTotalPages()
            );
            
            PagedModel<EntityModel<Article>> pagedModel = 
                PagedModel.of(articles, metadata);
            
            // Add navigation links
            pagedModel.add(WebMvcLinkBuilder.linkTo(
                WebMvcLinkBuilder.methodOn(ArticleController.class)
                    .getArticles(page, size)
            ).withSelfRel());
            
            if (articlePage.hasNext()) {
                pagedModel.add(WebMvcLinkBuilder.linkTo(
                    WebMvcLinkBuilder.methodOn(ArticleController.class)
                        .getArticles(page + 1, size)
                ).withRel(IanaLinkRelations.NEXT));
            }
            
            if (articlePage.hasPrevious()) {
                pagedModel.add(WebMvcLinkBuilder.linkTo(
                    WebMvcLinkBuilder.methodOn(ArticleController.class)
                        .getArticles(page - 1, size)
                ).withRel(IanaLinkRelations.PREV));
            }
            
            pagedModel.add(WebMvcLinkBuilder.linkTo(
                WebMvcLinkBuilder.methodOn(ArticleController.class)
                    .getArticles(0, size)
            ).withRel(IanaLinkRelations.FIRST));
            
            int lastPage = articlePage.getTotalPages() - 1;
            pagedModel.add(WebMvcLinkBuilder.linkTo(
                WebMvcLinkBuilder.methodOn(ArticleController.class)
                    .getArticles(lastPage, size)
            ).withRel(IanaLinkRelations.LAST));
            
            return pagedModel;
        }

        @GetMapping("/{id}")
        public EntityModel<Article> getArticle(@PathVariable Long id) {
            Article article = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Not found"));
            return EntityModel.of(article);
        }
    }
}
