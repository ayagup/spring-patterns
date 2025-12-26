package com.example.pagination.custom;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Custom Pagination Response Pattern
 * 
 * Provides custom pagination response format
 * with flexible metadata structure.
 */
@SpringBootApplication
public class CustomPaginationResponsePattern {

    public static void main(String[] args) {
        SpringApplication.run(CustomPaginationResponsePattern.class, args);
    }

    /**
     * Generic pagination response wrapper
     */
    public static class PaginatedResponse<T> {
        private List<T> data;
        private PaginationMetadata pagination;

        public PaginatedResponse(List<T> data, PaginationMetadata pagination) {
            this.data = data;
            this.pagination = pagination;
        }

        public List<T> getData() { return data; }
        public void setData(List<T> data) { this.data = data; }
        public PaginationMetadata getPagination() { return pagination; }
        public void setPagination(PaginationMetadata pagination) { this.pagination = pagination; }
    }

    /**
     * Pagination metadata
     */
    public static class PaginationMetadata {
        private int currentPage;
        private int pageSize;
        private long totalItems;
        private int totalPages;
        private boolean hasNext;
        private boolean hasPrevious;
        private Integer nextPage;
        private Integer previousPage;

        public static class Builder {
            private int currentPage;
            private int pageSize;
            private long totalItems;

            public Builder currentPage(int currentPage) {
                this.currentPage = currentPage;
                return this;
            }

            public Builder pageSize(int pageSize) {
                this.pageSize = pageSize;
                return this;
            }

            public Builder totalItems(long totalItems) {
                this.totalItems = totalItems;
                return this;
            }

            public PaginationMetadata build() {
                PaginationMetadata metadata = new PaginationMetadata();
                metadata.currentPage = this.currentPage;
                metadata.pageSize = this.pageSize;
                metadata.totalItems = this.totalItems;
                metadata.totalPages = (int) Math.ceil((double) totalItems / pageSize);
                metadata.hasNext = currentPage < metadata.totalPages - 1;
                metadata.hasPrevious = currentPage > 0;
                metadata.nextPage = metadata.hasNext ? currentPage + 1 : null;
                metadata.previousPage = metadata.hasPrevious ? currentPage - 1 : null;
                return metadata;
            }
        }

        // Getters
        public int getCurrentPage() { return currentPage; }
        public int getPageSize() { return pageSize; }
        public long getTotalItems() { return totalItems; }
        public int getTotalPages() { return totalPages; }
        public boolean isHasNext() { return hasNext; }
        public boolean isHasPrevious() { return hasPrevious; }
        public Integer getNextPage() { return nextPage; }
        public Integer getPreviousPage() { return previousPage; }
    }

    public static class Book {
        private Long id;
        private String title;
        private String author;

        public Book(Long id, String title, String author) {
            this.id = id;
            this.title = title;
            this.author = author;
        }

        public Long getId() { return id; }
        public String getTitle() { return title; }
        public String getAuthor() { return author; }
    }

    @RestController
    @RequestMapping("/api/books")
    public static class BookController {

        private final List<Book> allBooks = createSampleBooks();

        private List<Book> createSampleBooks() {
            List<Book> books = new ArrayList<>();
            for (int i = 1; i <= 100; i++) {
                books.add(new Book((long) i, "Book " + i, "Author " + (i % 10)));
            }
            return books;
        }

        /**
         * Custom paginated response
         */
        @GetMapping
        public PaginatedResponse<Book> getBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
        ) {
            int start = page * size;
            int end = Math.min(start + size, allBooks.size());
            
            List<Book> pageData = allBooks.subList(start, end);
            
            PaginationMetadata metadata = new PaginationMetadata.Builder()
                .currentPage(page)
                .pageSize(size)
                .totalItems(allBooks.size())
                .build();
            
            return new PaginatedResponse<>(pageData, metadata);
        }
    }
}
