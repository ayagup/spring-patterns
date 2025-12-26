package com.example.cors;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Controller-level CORS Pattern
 * 
 * Demonstrates CORS configuration at the controller class level,
 * applying to all methods within the controller.
 * 
 * Features:
 * - Controller-wide CORS settings
 * - Inherited by all methods
 * - Method-level override capability
 * - Cleaner than per-method configuration
 */
@SpringBootApplication
public class ControllerLevelCORSPattern {
    
    public static void main(String[] args) {
        SpringApplication.run(ControllerLevelCORSPattern.class, args);
    }
    
    /**
     * Controller with class-level @CrossOrigin
     * All methods inherit these CORS settings
     */
    @RestController
    @RequestMapping("/api/products")
    @CrossOrigin(origins = {"http://localhost:3000", "https://shop.example.com"},
                methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE},
                allowedHeaders = {"Content-Type", "Authorization"},
                exposedHeaders = {"X-Total-Count"},
                allowCredentials = "true",
                maxAge = 3600)
    public static class ProductController {
        
        @GetMapping
        public List<Map<String, Object>> getAllProducts() {
            return Arrays.asList(
                Map.of("id", 1, "name", "Product 1", "price", 29.99),
                Map.of("id", 2, "name", "Product 2", "price", 49.99),
                Map.of("id", 3, "name", "Product 3", "price", 19.99)
            );
        }
        
        @GetMapping("/{id}")
        public Map<String, Object> getProduct(@PathVariable int id) {
            return Map.of(
                "id", id,
                "name", "Product " + id,
                "price", 29.99,
                "description", "Product description",
                "timestamp", LocalDateTime.now()
            );
        }
        
        @PostMapping
        public Map<String, Object> createProduct(@RequestBody Map<String, Object> product) {
            return Map.of(
                "message", "Product created",
                "product", product,
                "timestamp", LocalDateTime.now()
            );
        }
        
        @PutMapping("/{id}")
        public Map<String, Object> updateProduct(
                @PathVariable int id,
                @RequestBody Map<String, Object> product) {
            return Map.of(
                "message", "Product updated",
                "id", id,
                "product", product,
                "timestamp", LocalDateTime.now()
            );
        }
        
        @DeleteMapping("/{id}")
        public Map<String, String> deleteProduct(@PathVariable int id) {
            return Map.of(
                "message", "Product deleted",
                "id", String.valueOf(id)
            );
        }
    }
    
    /**
     * Another controller with different CORS settings
     */
    @RestController
    @RequestMapping("/api/orders")
    @CrossOrigin(origins = "http://localhost:3000",
                methods = {RequestMethod.GET, RequestMethod.POST},
                maxAge = 1800)
    public static class OrderController {
        
        @GetMapping
        public List<Map<String, Object>> getOrders() {
            return Arrays.asList(
                Map.of("id", 1, "total", 99.99, "status", "pending"),
                Map.of("id", 2, "total", 149.99, "status", "shipped")
            );
        }
        
        @PostMapping
        public Map<String, Object> createOrder(@RequestBody Map<String, Object> order) {
            return Map.of(
                "message", "Order created",
                "order", order,
                "timestamp", LocalDateTime.now()
            );
        }
    }
    
    @RestController
    @RequestMapping("/info")
    public static class InfoController {
        
        @GetMapping("/cors-demo")
        public Map<String, Object> getCorsDemo() {
            return Map.of(
                "pattern", "Controller-level CORS",
                "description", "CORS configured at controller class level",
                "benefit", "All methods inherit CORS settings automatically",
                "controllers", Map.of(
                    "/api/products", "localhost:3000 + shop.example.com",
                    "/api/orders", "localhost:3000 only"
                ),
                "timestamp", LocalDateTime.now()
            );
        }
    }
}
