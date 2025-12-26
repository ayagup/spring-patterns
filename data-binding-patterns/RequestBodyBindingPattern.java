package com.example.databinding;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Request Body Binding Pattern
 * 
 * Demonstrates Spring's @RequestBody annotation for:
 * - Binding HTTP request body to Java objects
 * - JSON to object conversion
 * - XML to object conversion
 * - Validation of request body
 * - Custom message converters
 * 
 * Key Features:
 * - Automatic JSON/XML deserialization
 * - Integration with Jackson/JAXB
 * - Validation with @Valid
 * - Content type negotiation
 * - Custom converters
 * 
 * Use Cases:
 * - REST API endpoints
 * - JSON payload processing
 * - Complex object graphs
 * - Batch operations
 * - File upload metadata
 * 
 * @author Spring Patterns
 */
@SpringBootApplication
public class RequestBodyBindingPattern {

    public static void main(String[] args) {
        SpringApplication.run(RequestBodyBindingPattern.class, args);
    }

    /**
     * Basic @RequestBody usage
     */
    @RestController
    @RequestMapping("/api/users")
    public static class UserApiController {

        /**
         * Simple request body binding
         * POST /api/users
         * Content-Type: application/json
         * Body: {"username":"john","email":"john@example.com","password":"secret"}
         */
        @PostMapping
        public ResponseEntity<User> createUser(@RequestBody User user) {
            // Process user
            System.out.println("Creating user: " + user.getUsername());
            user.setId(1L);
            return ResponseEntity.status(HttpStatus.CREATED).body(user);
        }

        /**
         * Request body with validation
         * Validates using JSR-303 annotations on User class
         */
        @PostMapping("/validated")
        public ResponseEntity<?> createValidatedUser(@Valid @RequestBody User user, 
                                                      BindingResult bindingResult) {
            if (bindingResult.hasErrors()) {
                List<String> errors = new ArrayList<>();
                bindingResult.getAllErrors().forEach(error -> 
                    errors.add(error.getDefaultMessage())
                );
                return ResponseEntity.badRequest().body(Map.of("errors", errors));
            }
            
            user.setId(2L);
            return ResponseEntity.status(HttpStatus.CREATED).body(user);
        }

        /**
         * Update user - PUT request
         */
        @PutMapping("/{id}")
        public ResponseEntity<User> updateUser(@PathVariable Long id, 
                                               @RequestBody User user) {
            user.setId(id);
            System.out.println("Updating user: " + id);
            return ResponseEntity.ok(user);
        }

        /**
         * Partial update - PATCH request
         */
        @PatchMapping("/{id}")
        public ResponseEntity<User> partialUpdate(@PathVariable Long id,
                                                  @RequestBody Map<String, Object> updates) {
            System.out.println("Partial update for user: " + id);
            System.out.println("Updates: " + updates);
            
            // Apply partial updates
            User user = new User();
            user.setId(id);
            if (updates.containsKey("email")) {
                user.setEmail((String) updates.get("email"));
            }
            
            return ResponseEntity.ok(user);
        }

        /**
         * Batch create users
         */
        @PostMapping("/batch")
        public ResponseEntity<List<User>> createUsers(@RequestBody List<User> users) {
            System.out.println("Creating " + users.size() + " users");
            
            // Assign IDs
            long id = 1L;
            for (User user : users) {
                user.setId(id++);
            }
            
            return ResponseEntity.status(HttpStatus.CREATED).body(users);
        }
    }

    /**
     * Complex nested object binding
     */
    @RestController
    @RequestMapping("/api/orders")
    public static class OrderApiController {

        /**
         * Bind complex nested objects
         * POST /api/orders
         * Body: {
         *   "customer": {"name":"John","email":"john@example.com"},
         *   "items": [
         *     {"productId":1,"quantity":2,"price":10.99},
         *     {"productId":2,"quantity":1,"price":25.50}
         *   ],
         *   "shippingAddress": {"street":"123 Main St","city":"Boston","zip":"02101"}
         * }
         */
        @PostMapping
        public ResponseEntity<Order> createOrder(@Valid @RequestBody Order order) {
            order.setId(1L);
            order.setOrderDate(LocalDateTime.now());
            order.setStatus("PENDING");
            
            // Calculate total
            double total = order.getItems().stream()
                    .mapToDouble(item -> item.getPrice() * item.getQuantity())
                    .sum();
            order.setTotal(total);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(order);
        }

        /**
         * Update order status
         */
        @PatchMapping("/{id}/status")
        public ResponseEntity<Order> updateStatus(@PathVariable Long id,
                                                  @RequestBody StatusUpdate statusUpdate) {
            Order order = new Order();
            order.setId(id);
            order.setStatus(statusUpdate.getStatus());
            
            System.out.println("Updated order " + id + " status to: " + statusUpdate.getStatus());
            
            return ResponseEntity.ok(order);
        }
    }

    /**
     * Content type negotiation
     */
    @RestController
    @RequestMapping("/api/products")
    public static class ProductApiController {

        /**
         * Consumes JSON
         * Content-Type: application/json
         */
        @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
        public ResponseEntity<Product> createFromJson(@RequestBody Product product) {
            System.out.println("Creating product from JSON");
            product.setId(1L);
            return ResponseEntity.status(HttpStatus.CREATED).body(product);
        }

        /**
         * Consumes XML
         * Content-Type: application/xml
         */
        @PostMapping(consumes = MediaType.APPLICATION_XML_VALUE)
        public ResponseEntity<Product> createFromXml(@RequestBody Product product) {
            System.out.println("Creating product from XML");
            product.setId(2L);
            return ResponseEntity.status(HttpStatus.CREATED).body(product);
        }

        /**
         * Accepts both JSON and XML
         */
        @PostMapping(path = "/flexible", 
                    consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
        public ResponseEntity<Product> createFlexible(@RequestBody Product product) {
            System.out.println("Creating product (flexible content type)");
            product.setId(3L);
            return ResponseEntity.status(HttpStatus.CREATED).body(product);
        }

        /**
         * Custom content type
         */
        @PostMapping(consumes = "application/vnd.company.product.v1+json")
        public ResponseEntity<Product> createV1(@RequestBody Product product) {
            System.out.println("Creating product using v1 API");
            product.setId(4L);
            return ResponseEntity.status(HttpStatus.CREATED).body(product);
        }
    }

    /**
     * Generic type binding with wrapper classes
     */
    @RestController
    @RequestMapping("/api/data")
    public static class DataApiController {

        /**
         * Generic wrapper for paginated responses
         */
        @PostMapping("/search")
        public ResponseEntity<PagedResponse<User>> search(@RequestBody SearchRequest searchRequest) {
            System.out.println("Searching with criteria: " + searchRequest.getQuery());
            
            PagedResponse<User> response = new PagedResponse<>();
            response.setPage(searchRequest.getPage());
            response.setSize(searchRequest.getSize());
            response.setTotalElements(100L);
            response.setTotalPages(10);
            
            List<User> users = new ArrayList<>();
            // Add sample data
            User user = new User();
            user.setId(1L);
            user.setUsername("john_doe");
            users.add(user);
            
            response.setContent(users);
            
            return ResponseEntity.ok(response);
        }

        /**
         * API response wrapper
         */
        @PostMapping("/process")
        public ResponseEntity<ApiResponse<String>> process(@RequestBody Map<String, Object> data) {
            ApiResponse<String> response = new ApiResponse<>();
            response.setSuccess(true);
            response.setMessage("Data processed successfully");
            response.setData("Result: " + data.size() + " fields processed");
            
            return ResponseEntity.ok(response);
        }
    }

    /**
     * Advanced binding with custom deserializers
     */
    @RestController
    @RequestMapping("/api/advanced")
    public static class AdvancedApiController {

        /**
         * Polymorphic binding using @JsonTypeInfo
         */
        @PostMapping("/notifications")
        public ResponseEntity<Notification> sendNotification(@RequestBody Notification notification) {
            System.out.println("Sending notification of type: " + notification.getType());
            return ResponseEntity.ok(notification);
        }

        /**
         * Custom date format handling
         */
        @PostMapping("/events")
        public ResponseEntity<Event> createEvent(@RequestBody Event event) {
            System.out.println("Creating event: " + event.getTitle() + " on " + event.getEventDate());
            return ResponseEntity.status(HttpStatus.CREATED).body(event);
        }

        /**
         * Optional request body
         */
        @PostMapping("/optional")
        public ResponseEntity<String> withOptionalBody(@RequestBody(required = false) Map<String, Object> data) {
            if (data == null || data.isEmpty()) {
                return ResponseEntity.ok("No data provided");
            }
            return ResponseEntity.ok("Received: " + data.size() + " fields");
        }
    }

    // Domain Classes

    public static class User {
        private Long id;
        
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        private String username;
        
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        private String email;
        
        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        private String password;
        
        @Past(message = "Birth date must be in the past")
        private LocalDate birthDate;

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        
        public LocalDate getBirthDate() { return birthDate; }
        public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }
    }

    public static class Order {
        private Long id;
        
        @NotNull(message = "Customer is required")
        @Valid
        private Customer customer;
        
        @NotEmpty(message = "Order must have at least one item")
        @Valid
        private List<OrderItem> items;
        
        @Valid
        private Address shippingAddress;
        
        private LocalDateTime orderDate;
        private String status;
        private Double total;

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public Customer getCustomer() { return customer; }
        public void setCustomer(Customer customer) { this.customer = customer; }
        
        public List<OrderItem> getItems() { return items; }
        public void setItems(List<OrderItem> items) { this.items = items; }
        
        public Address getShippingAddress() { return shippingAddress; }
        public void setShippingAddress(Address shippingAddress) { this.shippingAddress = shippingAddress; }
        
        public LocalDateTime getOrderDate() { return orderDate; }
        public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public Double getTotal() { return total; }
        public void setTotal(Double total) { this.total = total; }
    }

    public static class Customer {
        @NotBlank(message = "Customer name is required")
        private String name;
        
        @Email(message = "Customer email must be valid")
        private String email;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    public static class OrderItem {
        @NotNull(message = "Product ID is required")
        private Long productId;
        
        @Min(value = 1, message = "Quantity must be at least 1")
        private Integer quantity;
        
        @DecimalMin(value = "0.01", message = "Price must be positive")
        private Double price;

        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        
        public Double getPrice() { return price; }
        public void setPrice(Double price) { this.price = price; }
    }

    public static class Address {
        private String street;
        private String city;
        private String state;
        private String zip;
        private String country;

        public String getStreet() { return street; }
        public void setStreet(String street) { this.street = street; }
        
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        
        public String getState() { return state; }
        public void setState(String state) { this.state = state; }
        
        public String getZip() { return zip; }
        public void setZip(String zip) { this.zip = zip; }
        
        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }
    }

    public static class Product {
        private Long id;
        private String name;
        private String description;
        private Double price;
        private Integer stock;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public Double getPrice() { return price; }
        public void setPrice(Double price) { this.price = price; }
        
        public Integer getStock() { return stock; }
        public void setStock(Integer stock) { this.stock = stock; }
    }

    public static class StatusUpdate {
        @NotBlank(message = "Status is required")
        private String status;

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    public static class SearchRequest {
        private String query;
        private Integer page = 0;
        private Integer size = 10;
        private String sortBy;
        private String sortDirection = "ASC";

        public String getQuery() { return query; }
        public void setQuery(String query) { this.query = query; }
        
        public Integer getPage() { return page; }
        public void setPage(Integer page) { this.page = page; }
        
        public Integer getSize() { return size; }
        public void setSize(Integer size) { this.size = size; }
        
        public String getSortBy() { return sortBy; }
        public void setSortBy(String sortBy) { this.sortBy = sortBy; }
        
        public String getSortDirection() { return sortDirection; }
        public void setSortDirection(String sortDirection) { this.sortDirection = sortDirection; }
    }

    public static class PagedResponse<T> {
        private List<T> content;
        private Integer page;
        private Integer size;
        private Long totalElements;
        private Integer totalPages;

        public List<T> getContent() { return content; }
        public void setContent(List<T> content) { this.content = content; }
        
        public Integer getPage() { return page; }
        public void setPage(Integer page) { this.page = page; }
        
        public Integer getSize() { return size; }
        public void setSize(Integer size) { this.size = size; }
        
        public Long getTotalElements() { return totalElements; }
        public void setTotalElements(Long totalElements) { this.totalElements = totalElements; }
        
        public Integer getTotalPages() { return totalPages; }
        public void setTotalPages(Integer totalPages) { this.totalPages = totalPages; }
    }

    public static class ApiResponse<T> {
        private Boolean success;
        private String message;
        private T data;
        private LocalDateTime timestamp = LocalDateTime.now();

        public Boolean getSuccess() { return success; }
        public void setSuccess(Boolean success) { this.success = success; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public T getData() { return data; }
        public void setData(T data) { this.data = data; }
        
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }

    public static class Notification {
        private String type;
        private String recipient;
        private String subject;
        private String message;

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public String getRecipient() { return recipient; }
        public void setRecipient(String recipient) { this.recipient = recipient; }
        
        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    public static class Event {
        private String title;
        private LocalDate eventDate;
        private String location;
        private String description;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public LocalDate getEventDate() { return eventDate; }
        public void setEventDate(LocalDate eventDate) { this.eventDate = eventDate; }
        
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}

/**
 * DOCUMENTATION
 * 
 * @RequestBody Usage:
 * 
 * 1. Basic Binding:
 *    - Automatically deserializes request body to Java object
 *    - Uses HttpMessageConverter implementations
 *    - Default: Jackson for JSON, JAXB for XML
 * 
 * 2. Validation:
 *    - Use @Valid to trigger JSR-303 validation
 *    - BindingResult captures validation errors
 *    - Custom validators supported
 * 
 * 3. Content Type Negotiation:
 *    - consumes attribute specifies accepted content types
 *    - Multiple content types supported
 *    - Custom media types allowed
 * 
 * 4. Advanced Features:
 *    - Nested object binding
 *    - Collection binding
 *    - Generic type support
 *    - Optional request bodies (required=false)
 *    - Polymorphic deserialization
 * 
 * Best Practices:
 * - Always validate user input with @Valid
 * - Use DTO classes instead of entities
 * - Handle BindingResult for validation errors
 * - Specify consumes for API versioning
 * - Use meaningful validation messages
 * - Consider partial updates with PATCH
 * 
 * Message Converters:
 * - MappingJackson2HttpMessageConverter: JSON
 * - Jaxb2RootElementHttpMessageConverter: XML
 * - StringHttpMessageConverter: Text
 * - Custom converters can be registered
 * 
 * Integration:
 * - Works with all HTTP methods (POST, PUT, PATCH, DELETE)
 * - Combines with @PathVariable, @RequestParam
 * - Compatible with ResponseEntity
 * - Supports content negotiation with produces
 */
