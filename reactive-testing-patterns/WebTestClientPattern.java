package com.example.reactive.testing;

import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.http.MediaType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.Duration;

/**
 * Web Test Client Pattern - Comprehensive WebFlux Testing
 * 
 * WebTestClient is a non-blocking, reactive client designed for testing WebFlux
 * applications. It provides a fluent API for making HTTP requests and asserting
 * responses, with full support for reactive streams and backpressure.
 * 
 * Key Concepts:
 * - Non-blocking HTTP Client: Tests reactive endpoints without blocking
 * - Fluent API: Chain request setup and response assertions
 * - Response Assertions: Assert status, headers, body content
 * - JSON/XML Support: Deserialize and verify response bodies
 * - Streaming Support: Test Server-Sent Events and streaming endpoints
 * - Cookie/Session Support: Test cookie and session handling
 * - Timeout Configuration: Configure timeouts for long operations
 * 
 * Setup Methods:
 * - bindToServer(String): Test running server
 * - bindToController(Object...): Test specific controllers
 * - bindToRouterFunction(RouterFunction): Test router functions
 * - bindToApplicationContext(ApplicationContext): Test with full context
 * - bindToWebHandler(WebHandler): Test low-level WebHandler
 * 
 * Request Methods:
 * - get(), post(), put(), delete(), patch(), options(), head()
 * - uri(String): Set request URI
 * - accept(MediaType): Set Accept header
 * - contentType(MediaType): Set Content-Type header
 * - header(String, String): Set custom header
 * - cookie(String, String): Set cookie
 * - bodyValue(Object): Set request body
 * - body(Publisher, Class): Set reactive body
 * 
 * Response Assertions:
 * - expectStatus(): Assert HTTP status
 * - expectHeader(): Assert response headers
 * - expectBody(): Assert response body
 * - expectBodyList(): Assert list response
 * - expectCookie(): Assert cookies
 * - returnResult(): Get full response
 * 
 * Use Cases:
 * - Testing WebFlux REST APIs
 * - Testing Server-Sent Events
 * - Testing streaming endpoints
 * - Integration testing
 * - API contract testing
 * 
 * Best Practices:
 * - Use bindToController for unit tests
 * - Use bindToServer for integration tests
 * - Assert both status and body
 * - Test error scenarios
 * - Verify headers and cookies
 * - Use timeout configuration for long operations
 */
public class WebTestClientPattern {

    // Domain model
    static class Product {
        private Long id;
        private String name;
        private Double price;
        
        public Product() {}
        
        public Product(Long id, String name, Double price) {
            this.id = id;
            this.name = name;
            this.price = price;
        }
        
        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Double getPrice() { return price; }
        public void setPrice(Double price) { this.price = price; }
    }

    // Reactive controller
    static class ProductController {
        
        public Mono<Product> getProduct(Long id) {
            return Mono.just(new Product(id, "Product-" + id, 99.99));
        }
        
        public Flux<Product> getAllProducts() {
            return Flux.just(
                new Product(1L, "Laptop", 999.99),
                new Product(2L, "Mouse", 29.99),
                new Product(3L, "Keyboard", 79.99)
            );
        }
        
        public Mono<Product> createProduct(Product product) {
            product.setId(System.currentTimeMillis());
            return Mono.just(product);
        }
        
        public Mono<Product> updateProduct(Long id, Product product) {
            product.setId(id);
            return Mono.just(product);
        }
        
        public Mono<Void> deleteProduct(Long id) {
            return Mono.empty();
        }
        
        public Flux<String> streamEvents() {
            return Flux.interval(Duration.ofSeconds(1))
                    .map(i -> "Event-" + i)
                    .take(5);
        }
        
        public Mono<Product> notFound() {
            return Mono.error(new RuntimeException("Product not found"));
        }
    }

    /**
     * WebTestClient Test Examples
     * 
     * In a real Spring Boot application, you would:
     * 1. Use @WebFluxTest to test controllers
     * 2. Inject WebTestClient automatically
     * 3. Use @MockBean for service dependencies
     * 
     * Example:
     * @WebFluxTest(ProductController.class)
     * class ProductControllerTest {
     *     @Autowired
     *     private WebTestClient webTestClient;
     * 
     *     @Test
     *     void testGetProduct() {
     *         webTestClient.get()
     *             .uri("/products/1")
     *             .exchange()
     *             .expectStatus().isOk()
     *             .expectBody(Product.class)
     *             .value(product -> assertEquals(1L, product.getId()));
     *     }
     * }
     */
    static class WebTestClientExamples {
        
        // Simulated WebTestClient setup
        // In real application, use @Autowired WebTestClient
        
        // Test GET request
        public void testGetProduct() {
            System.out.println("\nTest: GET /products/1");
            System.out.println("Expected: 200 OK with product data");
            System.out.println("WebTestClient usage:");
            System.out.println("  webTestClient.get()");
            System.out.println("    .uri(\"/products/1\")");
            System.out.println("    .accept(MediaType.APPLICATION_JSON)");
            System.out.println("    .exchange()");
            System.out.println("    .expectStatus().isOk()");
            System.out.println("    .expectHeader().contentType(MediaType.APPLICATION_JSON)");
            System.out.println("    .expectBody(Product.class)");
            System.out.println("    .value(product -> {");
            System.out.println("        assertEquals(1L, product.getId());");
            System.out.println("        assertEquals(\"Product-1\", product.getName());");
            System.out.println("    });");
        }
        
        // Test GET all
        public void testGetAllProducts() {
            System.out.println("\nTest: GET /products");
            System.out.println("Expected: 200 OK with list of products");
            System.out.println("WebTestClient usage:");
            System.out.println("  webTestClient.get()");
            System.out.println("    .uri(\"/products\")");
            System.out.println("    .exchange()");
            System.out.println("    .expectStatus().isOk()");
            System.out.println("    .expectBodyList(Product.class)");
            System.out.println("    .hasSize(3)");
            System.out.println("    .contains(");
            System.out.println("        new Product(1L, \"Laptop\", 999.99),");
            System.out.println("        new Product(2L, \"Mouse\", 29.99)");
            System.out.println("    );");
        }
        
        // Test POST request
        public void testCreateProduct() {
            System.out.println("\nTest: POST /products");
            System.out.println("Expected: 201 CREATED with created product");
            System.out.println("WebTestClient usage:");
            System.out.println("  Product newProduct = new Product(null, \"Monitor\", 299.99);");
            System.out.println("  webTestClient.post()");
            System.out.println("    .uri(\"/products\")");
            System.out.println("    .contentType(MediaType.APPLICATION_JSON)");
            System.out.println("    .bodyValue(newProduct)");
            System.out.println("    .exchange()");
            System.out.println("    .expectStatus().isCreated()");
            System.out.println("    .expectBody(Product.class)");
            System.out.println("    .value(product -> {");
            System.out.println("        assertNotNull(product.getId());");
            System.out.println("        assertEquals(\"Monitor\", product.getName());");
            System.out.println("    });");
        }
        
        // Test PUT request
        public void testUpdateProduct() {
            System.out.println("\nTest: PUT /products/1");
            System.out.println("Expected: 200 OK with updated product");
            System.out.println("WebTestClient usage:");
            System.out.println("  Product updated = new Product(1L, \"Updated\", 149.99);");
            System.out.println("  webTestClient.put()");
            System.out.println("    .uri(\"/products/1\")");
            System.out.println("    .bodyValue(updated)");
            System.out.println("    .exchange()");
            System.out.println("    .expectStatus().isOk()");
            System.out.println("    .expectBody(Product.class)");
            System.out.println("    .value(product -> assertEquals(\"Updated\", product.getName()));");
        }
        
        // Test DELETE request
        public void testDeleteProduct() {
            System.out.println("\nTest: DELETE /products/1");
            System.out.println("Expected: 204 NO CONTENT");
            System.out.println("WebTestClient usage:");
            System.out.println("  webTestClient.delete()");
            System.out.println("    .uri(\"/products/1\")");
            System.out.println("    .exchange()");
            System.out.println("    .expectStatus().isNoContent()");
            System.out.println("    .expectBody().isEmpty();");
        }
        
        // Test streaming endpoint
        public void testStreamEvents() {
            System.out.println("\nTest: GET /events (Server-Sent Events)");
            System.out.println("Expected: Stream of events");
            System.out.println("WebTestClient usage:");
            System.out.println("  webTestClient.get()");
            System.out.println("    .uri(\"/events\")");
            System.out.println("    .accept(MediaType.TEXT_EVENT_STREAM)");
            System.out.println("    .exchange()");
            System.out.println("    .expectStatus().isOk()");
            System.out.println("    .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM)");
            System.out.println("    .returnResult(String.class)");
            System.out.println("    .getResponseBody()");
            System.out.println("    .take(5)");
            System.out.println("    .collectList()");
            System.out.println("    .block()");
            System.out.println("    .forEach(event -> System.out.println(\"Received: \" + event));");
        }
        
        // Test error response
        public void testNotFound() {
            System.out.println("\nTest: GET /products/999 (not found)");
            System.out.println("Expected: 404 NOT FOUND");
            System.out.println("WebTestClient usage:");
            System.out.println("  webTestClient.get()");
            System.out.println("    .uri(\"/products/999\")");
            System.out.println("    .exchange()");
            System.out.println("    .expectStatus().isNotFound();");
        }
        
        // Test with headers
        public void testWithHeaders() {
            System.out.println("\nTest: GET with custom headers");
            System.out.println("WebTestClient usage:");
            System.out.println("  webTestClient.get()");
            System.out.println("    .uri(\"/products/1\")");
            System.out.println("    .header(\"X-Custom-Header\", \"CustomValue\")");
            System.out.println("    .header(\"Authorization\", \"Bearer token123\")");
            System.out.println("    .exchange()");
            System.out.println("    .expectStatus().isOk()");
            System.out.println("    .expectHeader().exists(\"X-Response-Header\");");
        }
        
        // Test with cookies
        public void testWithCookies() {
            System.out.println("\nTest: GET with cookies");
            System.out.println("WebTestClient usage:");
            System.out.println("  webTestClient.get()");
            System.out.println("    .uri(\"/products/1\")");
            System.out.println("    .cookie(\"session\", \"abc123\")");
            System.out.println("    .exchange()");
            System.out.println("    .expectStatus().isOk()");
            System.out.println("    .expectCookie().exists(\"session\");");
        }
        
        // Test JSON path assertions
        public void testJsonPath() {
            System.out.println("\nTest: JSON Path assertions");
            System.out.println("WebTestClient usage:");
            System.out.println("  webTestClient.get()");
            System.out.println("    .uri(\"/products/1\")");
            System.out.println("    .exchange()");
            System.out.println("    .expectStatus().isOk()");
            System.out.println("    .expectBody()");
            System.out.println("    .jsonPath(\"$.id\").isEqualTo(1)");
            System.out.println("    .jsonPath(\"$.name\").isEqualTo(\"Product-1\")");
            System.out.println("    .jsonPath(\"$.price\").isNumber();");
        }
        
        // Test timeout configuration
        public void testTimeout() {
            System.out.println("\nTest: Timeout configuration");
            System.out.println("WebTestClient configuration:");
            System.out.println("  WebTestClient client = WebTestClient");
            System.out.println("    .bindToServer()");
            System.out.println("    .baseUrl(\"http://localhost:8080\")");
            System.out.println("    .responseTimeout(Duration.ofSeconds(30))");
            System.out.println("    .build();");
        }
    }

    public static void main(String[] args) {
        System.out.println("Web Test Client Pattern - Comprehensive WebFlux Testing");
        System.out.println("=======================================================");
        
        WebTestClientExamples examples = new WebTestClientExamples();
        
        examples.testGetProduct();
        examples.testGetAllProducts();
        examples.testCreateProduct();
        examples.testUpdateProduct();
        examples.testDeleteProduct();
        examples.testStreamEvents();
        examples.testNotFound();
        examples.testWithHeaders();
        examples.testWithCookies();
        examples.testJsonPath();
        examples.testTimeout();
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println("Key Setup Methods:");
        System.out.println("- bindToServer(url): Test running server");
        System.out.println("- bindToController(...): Test controllers");
        System.out.println("- bindToRouterFunction(...): Test router functions");
        System.out.println("- bindToApplicationContext(...): Test with context");
        
        System.out.println("\nKey Request Methods:");
        System.out.println("- get/post/put/delete/patch()");
        System.out.println("- uri(String): Set request URI");
        System.out.println("- accept/contentType(MediaType)");
        System.out.println("- header/cookie(key, value)");
        System.out.println("- bodyValue(Object): Set body");
        
        System.out.println("\nKey Assertion Methods:");
        System.out.println("- expectStatus(): Assert HTTP status");
        System.out.println("- expectHeader(): Assert headers");
        System.out.println("- expectBody(): Assert response body");
        System.out.println("- expectBodyList(): Assert list");
        System.out.println("- expectCookie(): Assert cookies");
        System.out.println("- jsonPath(String): JSON path assertions");
    }
}
