package com.example.requestresponse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

/**
 * Path Variable Pattern
 * 
 * Demonstrates how to extract values from URI path in Spring MVC.
 * @PathVariable annotation binds URI template variables to method parameters.
 */
@SpringBootApplication
public class PathVariablePattern {

    public static void main(String[] args) {
        SpringApplication.run(PathVariablePattern.class, args);
    }

    @RestController
    @RequestMapping("/api")
    static class ResourceController {

        /**
         * Simple path variable
         * GET /api/users/123
         */
        @GetMapping("/users/{id}")
        public String getUser(@PathVariable Long id) {
            return "User ID: " + id;
        }

        /**
         * Multiple path variables
         * GET /api/users/123/orders/456
         */
        @GetMapping("/users/{userId}/orders/{orderId}")
        public String getOrder(@PathVariable Long userId, @PathVariable Long orderId) {
            return String.format("User: %d, Order: %d", userId, orderId);
        }

        /**
         * Path variable with custom name
         * GET /api/products/LAPTOP-001
         */
        @GetMapping("/products/{productCode}")
        public String getProduct(@PathVariable(name = "productCode") String code) {
            return "Product Code: " + code;
        }

        /**
         * Optional path variable with regex
         * GET /api/items/12345 or /api/items/ABC123
         */
        @GetMapping("/items/{itemId:[a-zA-Z0-9]+}")
        public String getItem(@PathVariable String itemId) {
            return "Item ID: " + itemId;
        }

        /**
         * Path variable with specific pattern
         * GET /api/files/2024/11/29/document.pdf
         */
        @GetMapping("/files/{year}/{month}/{day}/{filename}")
        public String getFile(
                @PathVariable int year,
                @PathVariable int month,
                @PathVariable int day,
                @PathVariable String filename) {
            return String.format("File: %s, Date: %d-%02d-%02d", filename, year, month, day);
        }

        /**
         * Path variable with version
         * GET /api/v1/customers/789
         * GET /api/v2/customers/789
         */
        @GetMapping("/{version}/customers/{id}")
        public String getCustomer(
                @PathVariable String version,
                @PathVariable Long id) {
            return String.format("API Version: %s, Customer ID: %d", version, id);
        }

        /**
         * RESTful CRUD with path variables
         */
        @GetMapping("/posts/{id}")
        public String getPost(@PathVariable Long id) {
            return "Fetching post: " + id;
        }

        @PutMapping("/posts/{id}")
        public String updatePost(@PathVariable Long id, @RequestBody String content) {
            return "Updating post " + id + " with: " + content;
        }

        @DeleteMapping("/posts/{id}")
        public String deletePost(@PathVariable Long id) {
            return "Deleting post: " + id;
        }

        /**
         * Nested resource path variables
         * GET /api/organizations/10/departments/5/employees/100
         */
        @GetMapping("/organizations/{orgId}/departments/{deptId}/employees/{empId}")
        public String getEmployee(
                @PathVariable Long orgId,
                @PathVariable Long deptId,
                @PathVariable Long empId) {
            return String.format("Org: %d, Dept: %d, Employee: %d", orgId, deptId, empId);
        }

        /**
         * Path variable with enum
         * GET /api/status/ACTIVE
         */
        @GetMapping("/status/{status}")
        public String getByStatus(@PathVariable Status status) {
            return "Status: " + status.name();
        }

        /**
         * Path variable with required=false (Spring 4.3.3+)
         * GET /api/documents/123 or /api/documents/
         */
        @GetMapping({"/documents/{id}", "/documents"})
        public String getDocument(@PathVariable(required = false) Long id) {
            return id != null ? "Document ID: " + id : "All documents";
        }
    }

    /**
     * Status enum for demonstration
     */
    enum Status {
        ACTIVE, INACTIVE, PENDING, DELETED
    }

    /**
     * Controller demonstrating path variables with matrix variables
     */
    @RestController
    @RequestMapping("/api/search")
    static class SearchController {

        /**
         * Path variable with search context
         * GET /api/search/products/electronics
         */
        @GetMapping("/{category}")
        public String searchByCategory(@PathVariable String category) {
            return "Searching in category: " + category;
        }

        /**
         * Combined path variable and request parameters
         * GET /api/search/category/electronics?minPrice=100&maxPrice=500
         */
        @GetMapping("/category/{category}")
        public String searchInCategory(
                @PathVariable String category,
                @RequestParam(required = false) Double minPrice,
                @RequestParam(required = false) Double maxPrice) {
            StringBuilder result = new StringBuilder("Category: " + category);
            if (minPrice != null) result.append(", Min: ").append(minPrice);
            if (maxPrice != null) result.append(", Max: ").append(maxPrice);
            return result.toString();
        }
    }
}
