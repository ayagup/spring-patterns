package com.example.testing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;

/**
 * Test Rest Template Pattern
 * ===========================
 * 
 * Demonstrates the TestRestTemplate pattern for integration testing of
 * REST APIs in Spring Boot applications.
 * 
 * Use Cases:
 * ----------
 * 1. Integration testing REST endpoints
 * 2. Testing with real HTTP server
 * 3. End-to-end API testing
 * 4. Testing authentication/authorization
 * 5. Testing error responses
 * 6. Testing different content types
 * 7. Testing redirects and status codes
 * 8. Testing request/response headers
 * 
 * Key Features:
 * -------------
 * - Built for testing
 * - Auto-configured in @SpringBootTest
 * - Follows redirects by default
 * - Fault tolerant
 * - Built-in authentication support
 * - Error handling
 * - Type-safe responses
 * - Integration with assertions
 * 
 * TestRestTemplate vs RestTemplate:
 * ----------------------------------
 * TestRestTemplate:
 * - Designed for testing
 * - Fault tolerant (doesn't throw on 4xx/5xx)
 * - Follows redirects
 * - Auto-configured in tests
 * - Built-in test utilities
 * 
 * RestTemplate:
 * - Designed for production
 * - Throws on HTTP errors
 * - Doesn't follow redirects by default
 * - Manual configuration needed
 * - Production-ready
 * 
 * HTTP Methods Supported:
 * -----------------------
 * - GET: getForObject(), getForEntity()
 * - POST: postForObject(), postForEntity(), postForLocation()
 * - PUT: put()
 * - DELETE: delete()
 * - PATCH: patchForObject()
 * - HEAD: headForHeaders()
 * - OPTIONS: optionsForAllow()
 * - Exchange: exchange() for any method
 * 
 * Response Types:
 * ---------------
 * 1. ResponseEntity<T>:
 *    - Complete HTTP response
 *    - Status code, headers, body
 *    - Most flexible
 * 
 * 2. Direct Object (T):
 *    - Just the response body
 *    - Simpler but less info
 *    - Status assumed to be 2xx
 * 
 * 3. URI:
 *    - Location header from POST
 *    - For resource creation
 * 
 * Best Practices:
 * ---------------
 * 1. Use @SpringBootTest(webEnvironment = RANDOM_PORT)
 * 2. Inject TestRestTemplate
 * 3. Use ResponseEntity for full control
 * 4. Assert status codes explicitly
 * 5. Test all HTTP methods
 * 6. Test error scenarios
 * 7. Verify response headers
 * 8. Use proper object mapping
 * 
 * Common Patterns:
 * ----------------
 * 1. GET endpoint testing
 * 2. POST with JSON body
 * 3. PUT for updates
 * 4. DELETE operations
 * 5. Authentication testing
 * 6. Error response testing
 * 7. Pagination testing
 * 8. Content negotiation
 * 
 * @author Spring Patterns
 * @version 1.0
 */

// Mock REST Controller (would normally be @RestController)
class UserRestController {
    
    public ResponseEntity<User> getUser(Long id) {
        User user = new User(id, "John Doe", "john@example.com");
        return ResponseEntity.ok(user);
    }
    
    public ResponseEntity<User> createUser(User user) {
        user.setId(1L);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .header("Location", "/api/users/" + user.getId())
            .body(user);
    }
    
    public ResponseEntity<User> updateUser(Long id, User user) {
        user.setId(id);
        return ResponseEntity.ok(user);
    }
    
    public ResponseEntity<Void> deleteUser(Long id) {
        return ResponseEntity.noContent().build();
    }
}

// Domain model
class User {
    private Long id;
    private String name;
    private String email;
    
    public User() {}
    
    public User(Long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}

/**
 * Example 1: GET Request Testing
 */
// @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GetRequestIntegrationTest {
    
    // @LocalServerPort
    // private int port;
    
    // @Autowired
    // private TestRestTemplate restTemplate;
    
    private UserRestController controller = new UserRestController();
    
    @Test
    void testGetUser() {
        System.out.println("\n=== Test: GET Request ===");
        
        // Simulates: restTemplate.getForEntity("/api/users/1", User.class)
        ResponseEntity<User> response = controller.getUser(1L);
        
        System.out.println("  GET /api/users/1");
        System.out.println("  Status: " + response.getStatusCode());
        System.out.println("  User: " + response.getBody().getName());
        
        System.out.println("\n  With TestRestTemplate:");
        System.out.println("    ResponseEntity<User> response = restTemplate");
        System.out.println("        .getForEntity(\"/api/users/1\", User.class);");
        System.out.println("    assertEquals(HttpStatus.OK, response.getStatusCode());");
        System.out.println("    assertEquals(\"John Doe\", response.getBody().getName());");
        
        System.out.println("✓ GET request successful");
    }
    
    @Test
    void testGetUserForObject() {
        System.out.println("\n=== Test: GET For Object ===");
        
        // Simulates: restTemplate.getForObject("/api/users/1", User.class)
        User user = controller.getUser(1L).getBody();
        
        System.out.println("  GET /api/users/1");
        System.out.println("  User name: " + user.getName());
        System.out.println("  User email: " + user.getEmail());
        
        System.out.println("\n  With TestRestTemplate:");
        System.out.println("    User user = restTemplate");
        System.out.println("        .getForObject(\"/api/users/1\", User.class);");
        System.out.println("    assertNotNull(user);");
        
        System.out.println("✓ GET for object successful");
    }
}

/**
 * Example 2: POST Request Testing
 */
class PostRequestIntegrationTest {
    
    private UserRestController controller = new UserRestController();
    
    @Test
    void testCreateUser() {
        System.out.println("\n=== Test: POST Request ===");
        
        User newUser = new User(null, "Jane Smith", "jane@example.com");
        
        // Simulates: restTemplate.postForEntity("/api/users", newUser, User.class)
        ResponseEntity<User> response = controller.createUser(newUser);
        
        System.out.println("  POST /api/users");
        System.out.println("  Request body: " + newUser.getName());
        System.out.println("  Status: " + response.getStatusCode());
        System.out.println("  Location: " + response.getHeaders().get("Location"));
        System.out.println("  Created user ID: " + response.getBody().getId());
        
        System.out.println("\n  With TestRestTemplate:");
        System.out.println("    ResponseEntity<User> response = restTemplate");
        System.out.println("        .postForEntity(\"/api/users\", newUser, User.class);");
        System.out.println("    assertEquals(HttpStatus.CREATED, response.getStatusCode());");
        
        System.out.println("✓ POST request successful");
    }
    
    @Test
    void testPostForLocation() {
        System.out.println("\n=== Test: POST For Location ===");
        
        User newUser = new User(null, "Bob Wilson", "bob@example.com");
        ResponseEntity<User> response = controller.createUser(newUser);
        String location = response.getHeaders().get("Location").get(0);
        
        System.out.println("  POST /api/users");
        System.out.println("  Location header: " + location);
        
        System.out.println("\n  With TestRestTemplate:");
        System.out.println("    URI location = restTemplate");
        System.out.println("        .postForLocation(\"/api/users\", newUser);");
        System.out.println("    assertNotNull(location);");
        
        System.out.println("✓ POST for location successful");
    }
}

/**
 * Example 3: PUT Request Testing
 */
class PutRequestIntegrationTest {
    
    private UserRestController controller = new UserRestController();
    
    @Test
    void testUpdateUser() {
        System.out.println("\n=== Test: PUT Request ===");
        
        User updatedUser = new User(1L, "John Updated", "john.new@example.com");
        
        // Simulates: restTemplate.put("/api/users/1", updatedUser)
        ResponseEntity<User> response = controller.updateUser(1L, updatedUser);
        
        System.out.println("  PUT /api/users/1");
        System.out.println("  Updated name: " + response.getBody().getName());
        System.out.println("  Updated email: " + response.getBody().getEmail());
        
        System.out.println("\n  With TestRestTemplate:");
        System.out.println("    restTemplate.put(\"/api/users/1\", updatedUser);");
        System.out.println("    // Or with exchange for response:");
        System.out.println("    ResponseEntity<User> response = restTemplate.exchange(");
        System.out.println("        \"/api/users/1\", HttpMethod.PUT, ");
        System.out.println("        new HttpEntity<>(updatedUser), User.class);");
        
        System.out.println("✓ PUT request successful");
    }
}

/**
 * Example 4: DELETE Request Testing
 */
class DeleteRequestIntegrationTest {
    
    private UserRestController controller = new UserRestController();
    
    @Test
    void testDeleteUser() {
        System.out.println("\n=== Test: DELETE Request ===");
        
        // Simulates: restTemplate.delete("/api/users/1")
        ResponseEntity<Void> response = controller.deleteUser(1L);
        
        System.out.println("  DELETE /api/users/1");
        System.out.println("  Status: " + response.getStatusCode());
        
        System.out.println("\n  With TestRestTemplate:");
        System.out.println("    restTemplate.delete(\"/api/users/1\");");
        System.out.println("    // Or with exchange for response:");
        System.out.println("    ResponseEntity<Void> response = restTemplate.exchange(");
        System.out.println("        \"/api/users/1\", HttpMethod.DELETE, ");
        System.out.println("        null, Void.class);");
        System.out.println("    assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());");
        
        System.out.println("✓ DELETE request successful");
    }
}

/**
 * Example 5: Headers and Authentication
 */
class HeadersAndAuthTest {
    
    @Test
    void testWithHeaders() {
        System.out.println("\n=== Test: Headers and Authentication ===");
        
        System.out.println("  Setting custom headers:");
        System.out.println("    HttpHeaders headers = new HttpHeaders();");
        System.out.println("    headers.set(\"Authorization\", \"Bearer token\");");
        System.out.println("    headers.setContentType(MediaType.APPLICATION_JSON);");
        System.out.println();
        System.out.println("    HttpEntity<User> request = new HttpEntity<>(user, headers);");
        System.out.println("    ResponseEntity<User> response = restTemplate.exchange(");
        System.out.println("        \"/api/users\", HttpMethod.POST, request, User.class);");
        
        System.out.println("\n  Basic Authentication:");
        System.out.println("    TestRestTemplate authenticated = restTemplate");
        System.out.println("        .withBasicAuth(\"user\", \"password\");");
        System.out.println("    ResponseEntity<User> response = authenticated");
        System.out.println("        .getForEntity(\"/api/users/1\", User.class);");
        
        System.out.println("✓ Headers and auth demonstrated");
    }
}

/**
 * Example 6: Error Response Testing
 */
class ErrorResponseTest {
    
    @Test
    void testErrorResponse() {
        System.out.println("\n=== Test: Error Response ===");
        
        System.out.println("  Testing 404 Not Found:");
        System.out.println("    ResponseEntity<String> response = restTemplate");
        System.out.println("        .getForEntity(\"/api/users/999\", String.class);");
        System.out.println("    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());");
        
        System.out.println("\n  Testing 400 Bad Request:");
        System.out.println("    User invalidUser = new User();");
        System.out.println("    ResponseEntity<String> response = restTemplate");
        System.out.println("        .postForEntity(\"/api/users\", invalidUser, String.class);");
        System.out.println("    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());");
        
        System.out.println("\n  Note: TestRestTemplate doesn't throw on errors!");
        System.out.println("  Always check response.getStatusCode()");
        
        System.out.println("✓ Error response handling demonstrated");
    }
}

/**
 * Example 7: Exchange Method
 */
class ExchangeMethodTest {
    
    @Test
    void testExchangeMethod() {
        System.out.println("\n=== Test: Exchange Method ===");
        System.out.println("  Universal method for any HTTP request");
        
        System.out.println("\n  GET with exchange:");
        System.out.println("    ResponseEntity<User> response = restTemplate.exchange(");
        System.out.println("        \"/api/users/1\",");
        System.out.println("        HttpMethod.GET,");
        System.out.println("        null,");
        System.out.println("        User.class);");
        
        System.out.println("\n  POST with exchange:");
        System.out.println("    HttpEntity<User> request = new HttpEntity<>(user, headers);");
        System.out.println("    ResponseEntity<User> response = restTemplate.exchange(");
        System.out.println("        \"/api/users\",");
        System.out.println("        HttpMethod.POST,");
        System.out.println("        request,");
        System.out.println("        User.class);");
        
        System.out.println("\n  Benefits:");
        System.out.println("  - Works with any HTTP method");
        System.out.println("  - Full control over request");
        System.out.println("  - Type-safe response");
        
        System.out.println("✓ Exchange method demonstrated");
    }
}

/**
 * Example 8: URL Parameters and Path Variables
 */
class UrlParametersTest {
    
    @Test
    void testUrlParameters() {
        System.out.println("\n=== Test: URL Parameters ===");
        
        System.out.println("  Path variables:");
        System.out.println("    ResponseEntity<User> response = restTemplate");
        System.out.println("        .getForEntity(\"/api/users/{id}\", User.class, 1);");
        System.out.println("  Or with map:");
        System.out.println("    Map<String, Object> params = Map.of(\"id\", 1);");
        System.out.println("    restTemplate.getForEntity(\"/api/users/{id}\", User.class, params);");
        
        System.out.println("\n  Query parameters:");
        System.out.println("    String url = UriComponentsBuilder");
        System.out.println("        .fromPath(\"/api/users\")");
        System.out.println("        .queryParam(\"page\", 0)");
        System.out.println("        .queryParam(\"size\", 10)");
        System.out.println("        .toUriString();");
        System.out.println("    ResponseEntity<List> response = restTemplate");
        System.out.println("        .getForEntity(url, List.class);");
        
        System.out.println("✓ URL parameters demonstrated");
    }
}

/**
 * Main class for demonstration
 */
public class TestRestTemplatePattern {
    
    public static void main(String[] args) {
        System.out.println("=== Test Rest Template Pattern ===\n");
        System.out.println("This pattern demonstrates:");
        System.out.println("1. GET request testing");
        System.out.println("2. POST request testing");
        System.out.println("3. PUT request testing");
        System.out.println("4. DELETE request testing");
        System.out.println("5. Headers and authentication");
        System.out.println("6. Error response testing");
        System.out.println("7. Exchange method usage");
        System.out.println("8. URL parameters and path variables");
        System.out.println("\nRun the test classes to see the pattern in action.");
        System.out.println("==========================");
    }
}

/**
 * TestRestTemplate Pattern Summary:
 * 
 * Setup:
 * ------
 * @SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
 * class ApiIntegrationTest {
 *     @Autowired
 *     private TestRestTemplate restTemplate;
 * }
 * 
 * GET Requests:
 * -------------
 * // Get entity
 * ResponseEntity<User> response = restTemplate
 *     .getForEntity("/api/users/{id}", User.class, 1);
 * 
 * // Get object directly
 * User user = restTemplate
 *     .getForObject("/api/users/1", User.class);
 * 
 * POST Requests:
 * --------------
 * User newUser = new User("John", "john@example.com");
 * ResponseEntity<User> response = restTemplate
 *     .postForEntity("/api/users", newUser, User.class);
 * 
 * // Get location
 * URI location = restTemplate
 *     .postForLocation("/api/users", newUser);
 * 
 * PUT/DELETE:
 * -----------
 * restTemplate.put("/api/users/1", updatedUser);
 * restTemplate.delete("/api/users/1");
 * 
 * With Headers:
 * -------------
 * HttpHeaders headers = new HttpHeaders();
 * headers.set("Authorization", "Bearer token");
 * HttpEntity<User> request = new HttpEntity<>(user, headers);
 * ResponseEntity<User> response = restTemplate
 *     .exchange("/api/users", HttpMethod.POST, request, User.class);
 * 
 * Basic Auth:
 * -----------
 * TestRestTemplate authenticated = restTemplate
 *     .withBasicAuth("user", "password");
 */
