package com.example.requestresponse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

/**
 * Request Parameter Pattern
 * 
 * Demonstrates how to handle query parameters and form data in Spring MVC.
 * @RequestParam annotation binds request parameters to method arguments.
 */
@SpringBootApplication
public class RequestParameterPattern {

    public static void main(String[] args) {
        SpringApplication.run(RequestParameterPattern.class, args);
    }

    @RestController
    @RequestMapping("/api/search")
    static class SearchController {

        /**
         * Simple request parameter
         * URL: /api/search?query=laptop
         */
        @GetMapping
        public String search(@RequestParam String query) {
            return "Searching for: " + query;
        }

        /**
         * Request parameter with default value
         * URL: /api/search/products?query=laptop&page=0
         */
        @GetMapping("/products")
        public String searchProducts(
                @RequestParam String query,
                @RequestParam(defaultValue = "0") int page,
                @RequestParam(defaultValue = "10") int size) {
            return String.format("Searching '%s' - Page: %d, Size: %d", query, page, size);
        }

        /**
         * Optional request parameter
         * URL: /api/search/items?q=laptop&category=electronics
         */
        @GetMapping("/items")
        public String searchItems(
                @RequestParam(name = "q") String query,
                @RequestParam(required = false) String category,
                @RequestParam(required = false) Double minPrice,
                @RequestParam(required = false) Double maxPrice) {
            StringBuilder result = new StringBuilder("Searching: " + query);
            if (category != null) result.append(", Category: ").append(category);
            if (minPrice != null) result.append(", Min Price: ").append(minPrice);
            if (maxPrice != null) result.append(", Max Price: ").append(maxPrice);
            return result.toString();
        }

        /**
         * Multiple values for same parameter
         * URL: /api/search/multi?tags=spring&tags=java&tags=boot
         */
        @GetMapping("/multi")
        public String searchWithMultipleValues(@RequestParam("tags") String[] tags) {
            return "Searching tags: " + String.join(", ", tags);
        }

        /**
         * All parameters as Map
         * URL: /api/search/flexible?name=John&age=30&city=NYC
         */
        @GetMapping("/flexible")
        public Map<String, String> searchFlexible(@RequestParam Map<String, String> allParams) {
            return allParams;
        }

        /**
         * Request parameter with custom name
         * URL: /api/search/custom?search_term=laptop
         */
        @GetMapping("/custom")
        public String customParamName(
                @RequestParam(name = "search_term", required = true) String searchTerm) {
            return "Searching: " + searchTerm;
        }

        /**
         * Form data submission
         * POST /api/search/form with form data
         */
        @PostMapping("/form")
        public String submitForm(
                @RequestParam String username,
                @RequestParam String email,
                @RequestParam(required = false) String phone) {
            return String.format("Form submitted - Username: %s, Email: %s, Phone: %s",
                    username, email, phone != null ? phone : "N/A");
        }

        /**
         * Boolean request parameter
         * URL: /api/search/filter?includeInactive=true
         */
        @GetMapping("/filter")
        public String filterSearch(
                @RequestParam String query,
                @RequestParam(defaultValue = "false") boolean includeInactive,
                @RequestParam(defaultValue = "true") boolean includeActive) {
            return String.format("Query: %s, Active: %b, Inactive: %b",
                    query, includeActive, includeInactive);
        }
    }

    /**
     * Controller with complex filtering
     */
    @RestController
    @RequestMapping("/api/products")
    static class ProductFilterController {

        @GetMapping("/filter")
        public String filterProducts(
                @RequestParam(required = false) String category,
                @RequestParam(required = false) String brand,
                @RequestParam(required = false) Double minPrice,
                @RequestParam(required = false) Double maxPrice,
                @RequestParam(required = false) String sortBy,
                @RequestParam(defaultValue = "asc") String sortOrder,
                @RequestParam(defaultValue = "0") int page,
                @RequestParam(defaultValue = "20") int size) {

            StringBuilder filter = new StringBuilder("Filtering products: ");
            if (category != null) filter.append("Category=").append(category).append(" ");
            if (brand != null) filter.append("Brand=").append(brand).append(" ");
            if (minPrice != null) filter.append("MinPrice=").append(minPrice).append(" ");
            if (maxPrice != null) filter.append("MaxPrice=").append(maxPrice).append(" ");
            filter.append(String.format("| Sort: %s %s | Page: %d, Size: %d",
                    sortBy != null ? sortBy : "default", sortOrder, page, size));

            return filter.toString();
        }
    }
}
