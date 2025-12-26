package com.example.pagination.slice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.*;

/**
 * Slice-based Pagination Pattern
 * 
 * Uses Slice instead of Page for better performance.
 * Slice doesn't count total elements, only checks if next page exists.
 * Ideal for infinite scrolling.
 */
@SpringBootApplication
public class SlicePaginationPattern {

    public static void main(String[] args) {
        SpringApplication.run(SlicePaginationPattern.class, args);
    }

    @Entity
    @Table(name = "posts")
    public static class Post {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        private String title;
        private String content;
        private Long userId;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
    }

    public interface PostRepository extends JpaRepository<Post, Long> {
        Slice<Post> findByUserId(Long userId, Pageable pageable);
    }

    @RestController
    @RequestMapping("/api/posts")
    public static class PostController {

        private final PostRepository repository;

        public PostController(PostRepository repository) {
            this.repository = repository;
        }

        /**
         * Slice-based pagination for infinite scroll
         * Returns hasNext but not total count
         */
        @GetMapping
        public Slice<Post> getPosts(Pageable pageable) {
            Slice<Post> slice = repository.findAll(pageable);
            
            // Slice methods
            System.out.println("Has next: " + slice.hasNext());
            System.out.println("Has previous: " + slice.hasPrevious());
            System.out.println("Number of elements: " + slice.getNumberOfElements());
            // Note: getTotalElements() not available in Slice
            
            return slice;
        }

        @GetMapping("/user/{userId}")
        public SliceResponse<Post> getUserPosts(
            @PathVariable Long userId,
            Pageable pageable
        ) {
            Slice<Post> slice = repository.findByUserId(userId, pageable);
            return new SliceResponse<>(
                slice.getContent(),
                slice.hasNext(),
                slice.getNumber(),
                slice.getSize()
            );
        }
    }

    /**
     * Custom response wrapper for Slice
     */
    public record SliceResponse<T>(
        java.util.List<T> content,
        boolean hasNext,
        int pageNumber,
        int pageSize
    ) {}
}
