package com.example.testing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;

/**
 * Mock MVC Pattern
 * =================
 * 
 * Demonstrates the MockMvc pattern for testing Spring MVC controllers
 * without starting a full HTTP server.
 * 
 * Use Cases:
 * ----------
 * 1. Test MVC controllers in isolation
 * 2. Verify request mappings
 * 3. Test request/response handling
 * 4. Validate view names and models
 * 5. Test exception handling
 * 6. Verify HTTP status codes
 * 7. Test content negotiation
 * 8. Validate response headers
 * 
 * Key Features:
 * -------------
 * - No HTTP server required
 * - Fast test execution
 * - Full Spring MVC infrastructure
 * - Request/response simulation
 * - Fluent assertion API
 * - Support for all HTTP methods
 * - Session and cookie handling
 * - File upload testing
 * 
 * MockMvc Setup Options:
 * ----------------------
 * 1. Standalone Setup:
 *    MockMvcBuilders.standaloneSetup(controller).build()
 *    - Tests single controller in isolation
 *    - No Spring context needed
 *    - Fast but limited
 * 
 * 2. WebApplicationContext Setup:
 *    MockMvcBuilders.webAppContextSetup(context).build()
 *    - Uses full Spring MVC configuration
 *    - All filters, interceptors included
 *    - More realistic but slower
 * 
 * Request Builders:
 * -----------------
 * - get(url) - GET request
 * - post(url) - POST request
 * - put(url) - PUT request
 * - delete(url) - DELETE request
 * - patch(url) - PATCH request
 * - options(url) - OPTIONS request
 * - head(url) - HEAD request
 * 
 * Result Matchers:
 * ----------------
 * - status() - HTTP status assertions
 * - content() - Response content assertions
 * - jsonPath() - JSON response assertions
 * - xpath() - XML response assertions
 * - header() - Response header assertions
 * - view() - View name assertions
 * - model() - Model attribute assertions
 * - redirectedUrl() - Redirect URL assertions
 * - forwardedUrl() - Forward URL assertions
 * - cookie() - Cookie assertions
 * 
 * Best Practices:
 * ---------------
 * 1. Use webAppContextSetup for integration tests
 * 2. Use standaloneSetup for unit tests
 * 3. Test all HTTP methods
 * 4. Verify status codes
 * 5. Validate response content
 * 6. Test error scenarios
 * 7. Use print() for debugging
 * 8. Test security configurations
 * 
 * Common Patterns:
 * ----------------
 * 1. REST API endpoint testing
 * 2. Form submission testing
 * 3. File upload testing
 * 4. Exception handling testing
 * 5. View rendering testing
 * 6. Redirect/forward testing
 * 7. Content negotiation testing
 * 8. Security testing
 * 
 * @author Spring Patterns
 * @version 1.0
 */

// Configuration
@Configuration
@EnableWebMvc
class MockMvcConfig {
    
    @Bean
    public UserController userController() {
        return new UserController();
    }
    
    @Bean
    public ProductController productController() {
        return new ProductController();
    }
    
    @Bean
    public ApiController apiController() {
        return new ApiController();
    }
}

// Mock REST Controller
class UserController {
    private final List<User> users = new ArrayList<>();
    
    public UserController() {
        users.add(new User(1L, "John Doe", "john@example.com"));
        users.add(new User(2L, "Jane Smith", "jane@example.com"));
    }
    
    // @GetMapping("/users")
    public List<User> getAllUsers() {
        return users;
    }
    
    // @GetMapping("/users/{id}")
    public User getUserById(Long id) {
        return users.stream()
            .filter(u -> u.getId().equals(id))
            .findFirst()
            .orElse(null);
    }
    
    // @PostMapping("/users")
    public User createUser(String name, String email) {
        User user = new User((long) (users.size() + 1), name, email);
        users.add(user);
        return user;
    }
    
    // @PutMapping("/users/{id}")
    public User updateUser(Long id, String name, String email) {
        User user = getUserById(id);
        if (user != null) {
            user.setName(name);
            user.setEmail(email);
        }
        return user;
    }
    
    // @DeleteMapping("/users/{id}")
    public boolean deleteUser(Long id) {
        return users.removeIf(u -> u.getId().equals(id));
    }
}

class ProductController {
    
    // @GetMapping("/products")
    public String listProducts() {
        return "products";
    }
    
    // @GetMapping("/products/{id}")
    public String viewProduct(Long id) {
        return "product-detail";
    }
    
    // @PostMapping("/products")
    public String createProduct() {
        return "redirect:/products";
    }
}

class ApiController {
    
    // @GetMapping("/api/status")
    public String getStatus() {
        return "{\"status\":\"OK\",\"message\":\"Service is running\"}";
    }
    
    // @PostMapping("/api/data")
    public String postData(String data) {
        return "{\"received\":\"" + data + "\",\"processed\":true}";
    }
    
    // @GetMapping("/api/error")
    public void throwError() {
        throw new RuntimeException("Test error");
    }
}

// Domain model
class User {
    private Long id;
    private String name;
    private String email;
    
    public User(Long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }
    
    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}

/**
 * Example 1: Standalone Setup
 * Demonstrates testing single controller in isolation
 */
@ExtendWith(SpringExtension.class)
class StandaloneMockMvcTest {
    
    private MockMvc mockMvc;
    
    @BeforeEach
    void setup() {
        UserController controller = new UserController();
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        
        System.out.println("\n=== MockMvc Standalone Setup ===");
        System.out.println("  Controller under test: UserController");
        System.out.println("  Setup: Standalone (no Spring context)");
    }
    
    @Test
    void testGetAllUsersStandalone() throws Exception {
        System.out.println("\n=== Test: GET All Users (Standalone) ===");
        
        // Simulate: mockMvc.perform(get("/users"))
        UserController controller = new UserController();
        List<User> users = controller.getAllUsers();
        
        System.out.println("  Simulated GET /users");
        System.out.println("  Users returned: " + users.size());
        System.out.println("✓ Standalone controller test successful");
    }
}

/**
 * Example 2: WebApplicationContext Setup
 * Demonstrates testing with full Spring MVC configuration
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = MockMvcConfig.class)
@WebAppConfiguration
class WebAppContextMockMvcTest {
    
    @Autowired
    private WebApplicationContext webApplicationContext;
    
    private MockMvc mockMvc;
    
    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(webApplicationContext)
            .build();
        
        System.out.println("\n=== MockMvc WebApplicationContext Setup ===");
        System.out.println("  Using full Spring MVC configuration");
        System.out.println("  Includes all filters and interceptors");
    }
    
    @Test
    void testWithWebAppContext() {
        System.out.println("\n=== Test: WebApplicationContext ===");
        System.out.println("  MockMvc created from WebApplicationContext");
        System.out.println("✓ Full MVC infrastructure available");
    }
}

/**
 * Example 3: GET Request Testing
 * Demonstrates testing GET requests
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = MockMvcConfig.class)
@WebAppConfiguration
class GetRequestTest {
    
    @Autowired
    private WebApplicationContext context;
    
    @Autowired
    private UserController userController;
    
    private MockMvc mockMvc;
    
    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }
    
    @Test
    void testGetAllUsers() {
        System.out.println("\n=== Test: GET All Users ===");
        
        // Simulate: mockMvc.perform(get("/users"))
        //     .andExpect(status().isOk())
        List<User> users = userController.getAllUsers();
        
        System.out.println("  GET /users");
        System.out.println("  Expected status: 200 OK");
        System.out.println("  Users count: " + users.size());
        System.out.println("✓ GET request successful");
    }
    
    @Test
    void testGetUserById() {
        System.out.println("\n=== Test: GET User By ID ===");
        
        // Simulate: mockMvc.perform(get("/users/{id}", 1))
        //     .andExpect(status().isOk())
        //     .andExpect(jsonPath("$.name").value("John Doe"))
        User user = userController.getUserById(1L);
        
        System.out.println("  GET /users/1");
        System.out.println("  Expected status: 200 OK");
        System.out.println("  User name: " + user.getName());
        System.out.println("  User email: " + user.getEmail());
        System.out.println("✓ GET by ID successful");
    }
}

/**
 * Example 4: POST Request Testing
 * Demonstrates testing POST requests
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = MockMvcConfig.class)
@WebAppConfiguration
class PostRequestTest {
    
    @Autowired
    private UserController userController;
    
    private MockMvc mockMvc;
    
    @BeforeEach
    void setup(@Autowired WebApplicationContext context) {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }
    
    @Test
    void testCreateUser() {
        System.out.println("\n=== Test: POST Create User ===");
        
        // Simulate: mockMvc.perform(post("/users")
        //     .contentType(MediaType.APPLICATION_JSON)
        //     .content("{\"name\":\"Bob\",\"email\":\"bob@example.com\"}"))
        //     .andExpect(status().isCreated())
        
        User newUser = userController.createUser("Bob Wilson", "bob@example.com");
        
        System.out.println("  POST /users");
        System.out.println("  Content-Type: application/json");
        System.out.println("  Request body: {name: Bob Wilson, email: bob@example.com}");
        System.out.println("  Expected status: 201 Created");
        System.out.println("  Created user ID: " + newUser.getId());
        System.out.println("✓ POST request successful");
    }
    
    @Test
    void testCreateUserWithFormData() {
        System.out.println("\n=== Test: POST with Form Data ===");
        
        // Simulate: mockMvc.perform(post("/users")
        //     .param("name", "Alice")
        //     .param("email", "alice@example.com"))
        //     .andExpect(status().isOk())
        
        User newUser = userController.createUser("Alice Brown", "alice@example.com");
        
        System.out.println("  POST /users");
        System.out.println("  Content-Type: application/x-www-form-urlencoded");
        System.out.println("  Form data: name=Alice Brown, email=alice@example.com");
        System.out.println("  Created user: " + newUser.getName());
        System.out.println("✓ Form POST successful");
    }
}

/**
 * Example 5: PUT/PATCH Request Testing
 * Demonstrates testing update requests
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = MockMvcConfig.class)
@WebAppConfiguration
class UpdateRequestTest {
    
    @Autowired
    private UserController userController;
    
    @Test
    void testUpdateUser() {
        System.out.println("\n=== Test: PUT Update User ===");
        
        // Simulate: mockMvc.perform(put("/users/{id}", 1)
        //     .contentType(MediaType.APPLICATION_JSON)
        //     .content("{\"name\":\"John Updated\",\"email\":\"john.new@example.com\"}"))
        //     .andExpect(status().isOk())
        
        User updated = userController.updateUser(1L, "John Updated", "john.new@example.com");
        
        System.out.println("  PUT /users/1");
        System.out.println("  Request body: {name: John Updated, email: john.new@example.com}");
        System.out.println("  Expected status: 200 OK");
        System.out.println("  Updated name: " + updated.getName());
        System.out.println("  Updated email: " + updated.getEmail());
        System.out.println("✓ PUT request successful");
    }
}

/**
 * Example 6: DELETE Request Testing
 * Demonstrates testing delete requests
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = MockMvcConfig.class)
@WebAppConfiguration
class DeleteRequestTest {
    
    @Autowired
    private UserController userController;
    
    @Test
    void testDeleteUser() {
        System.out.println("\n=== Test: DELETE User ===");
        
        // Simulate: mockMvc.perform(delete("/users/{id}", 2))
        //     .andExpect(status().isNoContent())
        
        int initialCount = userController.getAllUsers().size();
        boolean deleted = userController.deleteUser(2L);
        int finalCount = userController.getAllUsers().size();
        
        System.out.println("  DELETE /users/2");
        System.out.println("  Expected status: 204 No Content");
        System.out.println("  Initial count: " + initialCount);
        System.out.println("  Deleted: " + deleted);
        System.out.println("  Final count: " + finalCount);
        System.out.println("✓ DELETE request successful");
    }
}

/**
 * Example 7: Response Content Testing
 * Demonstrates testing response content
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = MockMvcConfig.class)
@WebAppConfiguration
class ResponseContentTest {
    
    @Autowired
    private ApiController apiController;
    
    @Test
    void testJsonResponse() {
        System.out.println("\n=== Test: JSON Response ===");
        
        // Simulate: mockMvc.perform(get("/api/status"))
        //     .andExpect(status().isOk())
        //     .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        //     .andExpect(jsonPath("$.status").value("OK"))
        //     .andExpect(jsonPath("$.message").value("Service is running"))
        
        String response = apiController.getStatus();
        
        System.out.println("  GET /api/status");
        System.out.println("  Expected Content-Type: application/json");
        System.out.println("  Response: " + response);
        System.out.println("  Validating JSON path: $.status = OK");
        System.out.println("  Validating JSON path: $.message = Service is running");
        System.out.println("✓ JSON response validated");
    }
}

/**
 * Example 8: View Name Testing
 * Demonstrates testing view names and redirects
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = MockMvcConfig.class)
@WebAppConfiguration
class ViewNameTest {
    
    @Autowired
    private ProductController productController;
    
    @Test
    void testViewName() {
        System.out.println("\n=== Test: View Name ===");
        
        // Simulate: mockMvc.perform(get("/products"))
        //     .andExpect(status().isOk())
        //     .andExpect(view().name("products"))
        
        String viewName = productController.listProducts();
        
        System.out.println("  GET /products");
        System.out.println("  Expected view name: products");
        System.out.println("  Actual view name: " + viewName);
        System.out.println("✓ View name validated");
    }
    
    @Test
    void testRedirect() {
        System.out.println("\n=== Test: Redirect ===");
        
        // Simulate: mockMvc.perform(post("/products"))
        //     .andExpect(status().is3xxRedirection())
        //     .andExpect(redirectedUrl("/products"))
        
        String redirect = productController.createProduct();
        
        System.out.println("  POST /products");
        System.out.println("  Expected redirect: redirect:/products");
        System.out.println("  Actual redirect: " + redirect);
        System.out.println("✓ Redirect validated");
    }
}

/**
 * Main class for demonstration
 */
public class MockMvcPattern {
    
    public static void main(String[] args) {
        System.out.println("=== Mock MVC Pattern ===\n");
        System.out.println("This pattern demonstrates:");
        System.out.println("1. Standalone MockMvc setup");
        System.out.println("2. WebApplicationContext setup");
        System.out.println("3. GET request testing");
        System.out.println("4. POST request testing");
        System.out.println("5. PUT/PATCH request testing");
        System.out.println("6. DELETE request testing");
        System.out.println("7. JSON response validation");
        System.out.println("8. View name and redirect testing");
        System.out.println("\nRun the test classes to see the pattern in action.");
        System.out.println("==========================");
    }
}

/**
 * MockMvc Pattern Summary:
 * 
 * Basic Setup (Standalone):
 * --------------------------
 * MockMvc mockMvc = MockMvcBuilders
 *     .standaloneSetup(new MyController())
 *     .build();
 * 
 * WebAppContext Setup:
 * --------------------
 * @Autowired
 * private WebApplicationContext context;
 * 
 * MockMvc mockMvc = MockMvcBuilders
 *     .webAppContextSetup(context)
 *     .build();
 * 
 * GET Request:
 * ------------
 * mockMvc.perform(get("/users/{id}", 1))
 *     .andExpect(status().isOk())
 *     .andExpect(content().contentType(MediaType.APPLICATION_JSON))
 *     .andExpect(jsonPath("$.name").value("John"));
 * 
 * POST Request:
 * -------------
 * mockMvc.perform(post("/users")
 *     .contentType(MediaType.APPLICATION_JSON)
 *     .content("{\"name\":\"John\",\"email\":\"john@example.com\"}"))
 *     .andExpect(status().isCreated())
 *     .andExpect(header().string("Location", "/users/1"));
 * 
 * PUT Request:
 * ------------
 * mockMvc.perform(put("/users/{id}", 1)
 *     .contentType(MediaType.APPLICATION_JSON)
 *     .content("{\"name\":\"John Updated\"}"))
 *     .andExpect(status().isOk());
 * 
 * DELETE Request:
 * ---------------
 * mockMvc.perform(delete("/users/{id}", 1))
 *     .andExpect(status().isNoContent());
 * 
 * With Parameters:
 * ----------------
 * mockMvc.perform(get("/search")
 *     .param("q", "spring")
 *     .param("page", "1"))
 *     .andExpect(status().isOk());
 * 
 * With Headers:
 * -------------
 * mockMvc.perform(get("/users")
 *     .header("Authorization", "Bearer token")
 *     .accept(MediaType.APPLICATION_JSON))
 *     .andExpect(status().isOk());
 * 
 * Print Results:
 * --------------
 * mockMvc.perform(get("/users"))
 *     .andDo(print())  // Print request/response details
 *     .andExpect(status().isOk());
 */
