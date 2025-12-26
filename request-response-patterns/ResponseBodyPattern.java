package com.example.requestresponse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response Body Pattern
 * 
 * Demonstrates how to return HTTP response bodies in Spring MVC.
 * @ResponseBody annotation (implicit in @RestController) converts
 * return value to HTTP response body using HttpMessageConverters.
 */
@SpringBootApplication
public class ResponseBodyPattern {

    public static void main(String[] args) {
        SpringApplication.run(ResponseBodyPattern.class, args);
    }

    @RestController
    @RequestMapping("/api/products")
    static class ProductController {

        /**
         * Simple response body - automatically converted to JSON
         */
        @GetMapping("/{id}")
        public Product getProduct(@PathVariable Long id) {
            return new Product(id, "Laptop", 999.99, "Electronics");
        }

        /**
         * Response body with list
         */
        @GetMapping
        public List<Product> getAllProducts() {
            return List.of(
                new Product(1L, "Laptop", 999.99, "Electronics"),
                new Product(2L, "Mouse", 29.99, "Electronics"),
                new Product(3L, "Keyboard", 79.99, "Electronics")
            );
        }

        /**
         * Response body with custom status
         */
        @PostMapping
        @ResponseStatus(HttpStatus.CREATED)
        public Product createProduct(@RequestBody Product product) {
            product.setCreatedAt(LocalDateTime.now());
            return product;
        }

        /**
         * Response body with specific media type
         */
        @GetMapping(value = "/{id}/xml", produces = MediaType.APPLICATION_XML_VALUE)
        public Product getProductAsXml(@PathVariable Long id) {
            return new Product(id, "Laptop", 999.99, "Electronics");
        }

        /**
         * Response body with wrapper
         */
        @GetMapping("/wrapped")
        public ApiResponse<List<Product>> getWrappedProducts() {
            List<Product> products = List.of(
                new Product(1L, "Laptop", 999.99, "Electronics"),
                new Product(2L, "Mouse", 29.99, "Electronics")
            );
            return new ApiResponse<>(true, "Products retrieved successfully", products);
        }

        /**
         * Paginated response body
         */
        @GetMapping("/paginated")
        public PagedResponse<Product> getPagedProducts(
                @RequestParam(defaultValue = "0") int page,
                @RequestParam(defaultValue = "10") int size) {
            List<Product> products = List.of(
                new Product(1L, "Laptop", 999.99, "Electronics"),
                new Product(2L, "Mouse", 29.99, "Electronics")
            );
            return new PagedResponse<>(products, page, size, 100L);
        }
    }

    /**
     * Product entity
     */
    static class Product {
        private Long id;
        private String name;
        private Double price;
        private String category;
        
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private LocalDateTime createdAt;

        public Product() {}

        public Product(Long id, String name, Double price, String category) {
            this.id = id;
            this.name = name;
            this.price = price;
            this.category = category;
        }

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Double getPrice() { return price; }
        public void setPrice(Double price) { this.price = price; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }

    /**
     * Generic API response wrapper
     */
    static class ApiResponse<T> {
        private boolean success;
        private String message;
        private T data;

        public ApiResponse(boolean success, String message, T data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public T getData() { return data; }
        public void setData(T data) { this.data = data; }
    }

    /**
     * Paginated response wrapper
     */
    static class PagedResponse<T> {
        private List<T> content;
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;

        public PagedResponse(List<T> content, int page, int size, long totalElements) {
            this.content = content;
            this.page = page;
            this.size = size;
            this.totalElements = totalElements;
            this.totalPages = (int) Math.ceil((double) totalElements / size);
        }

        public List<T> getContent() { return content; }
        public void setContent(List<T> content) { this.content = content; }
        public int getPage() { return page; }
        public void setPage(int page) { this.page = page; }
        public int getSize() { return size; }
        public void setSize(int size) { this.size = size; }
        public long getTotalElements() { return totalElements; }
        public void setTotalElements(long totalElements) { this.totalElements = totalElements; }
        public int getTotalPages() { return totalPages; }
        public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
    }
}
