package com.example.testing;

import org.junit.jupiter.api.Test;

/**
 * Web MVC Test Pattern
 * =====================
 * 
 * Demonstrates the @WebMvcTest annotation for testing Spring MVC
 * controllers in isolation with minimal context.
 * 
 * Use Cases:
 * ----------
 * 1. Test MVC controllers only
 * 2. Test request mappings
 * 3. Test input validation
 * 4. Test exception handling
 * 5. Test view resolution
 * 6. Test content negotiation
 * 7. Fast controller tests
 * 8. Isolated layer testing
 * 
 * Key Features:
 * -------------
 * - Auto-configures MockMvc
 * - Loads only MVC components
 * - Disables full auto-configuration
 * - Supports controller slicing
 * - Mock service dependencies
 * - Fast test execution
 * - No database/JPA configuration
 * - Security auto-configuration included
 * 
 * What Gets Loaded:
 * -----------------
 * Loaded:
 * - @Controller, @RestController
 * - @ControllerAdvice
 * - @JsonComponent
 * - Converter, Filter
 * - WebMvcConfigurer
 * - HandlerMethodArgumentResolver
 * 
 * Not Loaded:
 * - @Service, @Repository, @Component
 * - Database configurations
 * - JPA entities
 * - Business logic beans
 * 
 * @author Spring Patterns
 * @version 1.0
 */
public class WebMvcTestPattern {
    
    @Test
    void demonstrateWebMvcTest() {
        System.out.println("\n=== Web MVC Test Pattern ===");
        
        System.out.println("\nBasic Usage:");
        System.out.println("  @WebMvcTest(UserController.class)");
        System.out.println("  class UserControllerTest {");
        System.out.println("      @Autowired");
        System.out.println("      private MockMvc mockMvc;");
        System.out.println("      ");
        System.out.println("      @MockBean");
        System.out.println("      private UserService userService;");
        System.out.println("      ");
        System.out.println("      @Test");
        System.out.println("      void testGetUser() throws Exception {");
        System.out.println("          mockMvc.perform(get(\"/users/1\"))");
        System.out.println("              .andExpect(status().isOk());");
        System.out.println("      }");
        System.out.println("  }");
        
        System.out.println("\nTest Multiple Controllers:");
        System.out.println("  @WebMvcTest({UserController.class, OrderController.class})");
        
        System.out.println("\nTest All Controllers:");
        System.out.println("  @WebMvcTest");
        System.out.println("  // Tests all controllers in application");
        
        System.out.println("\nWith MockBean:");
        System.out.println("  @WebMvcTest(ProductController.class)");
        System.out.println("  class ProductControllerTest {");
        System.out.println("      @Autowired");
        System.out.println("      private MockMvc mockMvc;");
        System.out.println("      ");
        System.out.println("      @MockBean");
        System.out.println("      private ProductService productService;");
        System.out.println("      ");
        System.out.println("      @Test");
        System.out.println("      void testCreateProduct() throws Exception {");
        System.out.println("          Product product = new Product(\"Laptop\", 999.99);");
        System.out.println("          when(productService.save(any()))");
        System.out.println("              .thenReturn(product);");
        System.out.println("          ");
        System.out.println("          mockMvc.perform(post(\"/products\")");
        System.out.println("              .contentType(MediaType.APPLICATION_JSON)");
        System.out.println("              .content(\"{\\\"name\\\":\\\"Laptop\\\"}\"))");
        System.out.println("              .andExpect(status().isCreated());");
        System.out.println("      }");
        System.out.println("  }");
        
        System.out.println("\nWithout Security:");
        System.out.println("  @WebMvcTest(controllers = UserController.class,");
        System.out.println("      excludeAutoConfiguration = SecurityAutoConfiguration.class)");
        
        System.out.println("\nWith Custom Configuration:");
        System.out.println("  @WebMvcTest(UserController.class)");
        System.out.println("  @Import(CustomWebConfig.class)");
        
        System.out.println("\n✓ Web MVC Test pattern demonstrated");
    }
    
    public static void main(String[] args) {
        System.out.println("=== Web MVC Test Pattern ===");
        System.out.println("Test Spring MVC controllers in isolation");
        System.out.println("Run tests to see pattern in action");
    }
}

/**
 * Web MVC Test Summary:
 * 
 * Single Controller:
 * ------------------
 * @WebMvcTest(UserController.class)
 * class UserControllerTest {
 *     @Autowired
 *     private MockMvc mockMvc;
 *     
 *     @MockBean
 *     private UserService userService;
 * }
 * 
 * Multiple Controllers:
 * ---------------------
 * @WebMvcTest({UserController.class, OrderController.class})
 * 
 * All Controllers:
 * ----------------
 * @WebMvcTest
 * 
 * Exclude Security:
 * -----------------
 * @WebMvcTest(
 *     controllers = UserController.class,
 *     excludeAutoConfiguration = SecurityAutoConfiguration.class
 * )
 * 
 * With Properties:
 * ----------------
 * @WebMvcTest
 * @TestPropertySource(properties = {
 *     "spring.mvc.throw-exception-if-no-handler-found=true"
 * })
 */
