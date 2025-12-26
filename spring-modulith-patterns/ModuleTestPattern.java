package com.example.springmodulithpatterns;

import org.springframework.modulith.test.ModuleTest;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Module Test Pattern
 * 
 * Demonstrates Spring Modulith's @ModuleTest for testing individual modules
 * in isolation with proper module boundaries.
 * 
 * Key Concepts:
 * - Module-level integration testing
 * - Test slicing for modules
 * - Module boundary validation in tests
 * - Mock external module dependencies
 * - Bootstrap only relevant module components
 * 
 * Usage:
 * @ModuleTest
 * class OrderModuleTests {
 *     // Only Order module beans are loaded
 *     // External dependencies can be mocked
 * }
 */
@SpringBootApplication
public class ModuleTestPattern {

    public static void main(String[] args) {
        SpringApplication.run(ModuleTestPattern.class, args);
    }

    /**
     * Service to be tested within its module context
     */
    @Service
    static class ProductService {
        
        public Product createProduct(String name, double price, String category) {
            validateProduct(name, price, category);
            
            return new Product(
                generateProductId(),
                name,
                price,
                category,
                "ACTIVE"
            );
        }
        
        public Product getProduct(String productId) {
            // Simulate product retrieval
            return new Product(
                productId,
                "Sample Product",
                99.99,
                "Electronics",
                "ACTIVE"
            );
        }
        
        public List<Product> getProductsByCategory(String category) {
            // Simulate category search
            return List.of(
                new Product("PROD-001", "Product 1", 49.99, category, "ACTIVE"),
                new Product("PROD-002", "Product 2", 79.99, category, "ACTIVE")
            );
        }
        
        public Product updateProductPrice(String productId, double newPrice) {
            if (newPrice <= 0) {
                throw new IllegalArgumentException("Price must be positive");
            }
            
            Product product = getProduct(productId);
            return new Product(
                product.productId(),
                product.name(),
                newPrice,
                product.category(),
                product.status()
            );
        }
        
        public void deleteProduct(String productId) {
            // Simulate product deletion
            System.out.println("Deleting product: " + productId);
        }
        
        private void validateProduct(String name, double price, String category) {
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("Product name is required");
            }
            
            if (price <= 0) {
                throw new IllegalArgumentException("Price must be positive");
            }
            
            if (category == null || category.trim().isEmpty()) {
                throw new IllegalArgumentException("Category is required");
            }
        }
        
        private String generateProductId() {
            return "PROD-" + System.currentTimeMillis();
        }
    }

    /**
     * Test information service
     */
    @Service
    static class ModuleTestInfoService {
        
        public TestConfiguration getTestConfiguration() {
            return new TestConfiguration(
                "@ModuleTest",
                "Module-level integration testing",
                List.of(
                    "Bootstrap only module components",
                    "Test module in isolation",
                    "Validate module boundaries",
                    "Mock external dependencies",
                    "Faster test execution"
                )
            );
        }
        
        public List<TestScenario> getTestScenarios() {
            return List.of(
                new TestScenario(
                    "Create Product Test",
                    "Test product creation within module",
                    "Validates product creation logic in isolation"
                ),
                new TestScenario(
                    "Get Product Test",
                    "Test product retrieval",
                    "Validates product lookup functionality"
                ),
                new TestScenario(
                    "Update Price Test",
                    "Test price update logic",
                    "Validates business rules for price updates"
                ),
                new TestScenario(
                    "Module Boundary Test",
                    "Test module boundaries are respected",
                    "Ensures no unauthorized cross-module access"
                )
            );
        }
        
        public TestExample getTestExample() {
            String exampleCode = """
                @ModuleTest
                class ProductModuleTests {
                    
                    @Autowired
                    ProductService productService;
                    
                    @Test
                    void shouldCreateProduct() {
                        Product product = productService.createProduct(
                            "Test Product",
                            99.99,
                            "Electronics"
                        );
                        
                        assertThat(product.name()).isEqualTo("Test Product");
                        assertThat(product.price()).isEqualTo(99.99);
                    }
                    
                    @Test
                    void shouldValidatePrice() {
                        assertThatThrownBy(() -> 
                            productService.createProduct("Test", -10.0, "Category")
                        ).isInstanceOf(IllegalArgumentException.class)
                         .hasMessageContaining("Price must be positive");
                    }
                }
                """;
            
            return new TestExample(
                "Product Module Test Example",
                exampleCode,
                "JUnit 5 + AssertJ"
            );
        }
    }

    @RestController
    @RequestMapping("/api/module-test")
    static class ModuleTestController {
        
        private final ProductService productService;
        private final ModuleTestInfoService testInfoService;
        
        public ModuleTestController(
                ProductService productService,
                ModuleTestInfoService testInfoService) {
            this.productService = productService;
            this.testInfoService = testInfoService;
        }
        
        @PostMapping("/products")
        public ProductResponse createProduct(@RequestBody CreateProductRequest request) {
            try {
                Product product = productService.createProduct(
                    request.name(),
                    request.price(),
                    request.category()
                );
                return new ProductResponse(product, "Product created successfully");
            } catch (IllegalArgumentException e) {
                return new ProductResponse(null, "Error: " + e.getMessage());
            }
        }
        
        @GetMapping("/products/{productId}")
        public ProductResponse getProduct(@PathVariable String productId) {
            Product product = productService.getProduct(productId);
            return new ProductResponse(product, "success");
        }
        
        @GetMapping("/products/category/{category}")
        public ProductsResponse getProductsByCategory(@PathVariable String category) {
            List<Product> products = productService.getProductsByCategory(category);
            return new ProductsResponse(products, products.size());
        }
        
        @PatchMapping("/products/{productId}/price")
        public ProductResponse updatePrice(
                @PathVariable String productId,
                @RequestBody UpdatePriceRequest request) {
            try {
                Product product = productService.updateProductPrice(productId, request.price());
                return new ProductResponse(product, "Price updated successfully");
            } catch (IllegalArgumentException e) {
                return new ProductResponse(null, "Error: " + e.getMessage());
            }
        }
        
        @GetMapping("/test-config")
        public TestConfiguration getTestConfiguration() {
            return testInfoService.getTestConfiguration();
        }
        
        @GetMapping("/test-scenarios")
        public TestScenariosResponse getTestScenarios() {
            List<TestScenario> scenarios = testInfoService.getTestScenarios();
            return new TestScenariosResponse(scenarios, scenarios.size());
        }
        
        @GetMapping("/test-example")
        public TestExample getTestExample() {
            return testInfoService.getTestExample();
        }
        
        @GetMapping("/info")
        public Map<String, Object> getInfo() {
            return Map.of(
                "pattern", "Module Test Pattern",
                "description", "Test modules in isolation with @ModuleTest",
                "annotation", "@ModuleTest",
                "benefits", List.of(
                    "Faster test execution",
                    "Module boundary validation",
                    "Isolated testing",
                    "Bootstrap only relevant components",
                    "Mock external dependencies"
                ),
                "features", List.of(
                    "Module-level integration testing",
                    "Test slicing",
                    "Boundary validation",
                    "Component isolation",
                    "Dependency mocking"
                ),
                "endpoints", List.of(
                    "POST /api/module-test/products",
                    "GET /api/module-test/products/{productId}",
                    "GET /api/module-test/products/category/{category}",
                    "PATCH /api/module-test/products/{productId}/price",
                    "GET /api/module-test/test-config",
                    "GET /api/module-test/test-scenarios",
                    "GET /api/module-test/test-example",
                    "GET /api/module-test/info"
                )
            );
        }
    }

    // Domain Models
    record Product(
        String productId,
        String name,
        double price,
        String category,
        String status
    ) {}

    // DTOs
    record CreateProductRequest(String name, double price, String category) {}
    record UpdatePriceRequest(double price) {}
    record ProductResponse(Product product, String message) {}
    record ProductsResponse(List<Product> products, int count) {}
    record TestConfiguration(String annotation, String description, List<String> features) {}
    record TestScenario(String name, String description, String purpose) {}
    record TestExample(String title, String code, String framework) {}
    record TestScenariosResponse(List<TestScenario> scenarios, int count) {}
}
