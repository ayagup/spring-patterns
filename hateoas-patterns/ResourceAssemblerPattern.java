package com.example.hateoas.patterns;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

/**
 * Resource Assembler Pattern
 * 
 * Demonstrates the RepresentationModelAssembler pattern for converting
 * domain objects into HATEOAS resources with links.
 * 
 * Key Features:
 * - Centralized resource assembly
 * - Consistent link generation
 * - Reusable conversion logic
 * - DRY principle
 * - Type-safe resource creation
 * - Automatic link building
 */
@SpringBootApplication
public class ResourceAssemblerPattern implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(ResourceAssemblerPattern.class, args);
    }

    @Override
    public void run(String... args) {
        System.out.println("=== Resource Assembler Pattern ===\n");
        System.out.println("1. Assembler Purpose:");
        System.out.println("   - Convert entities to resources");
        System.out.println("   - Add links consistently");
        System.out.println("   - Centralize resource creation");
        System.out.println("   - Promote reusability");

        System.out.println("\n2. Benefits:");
        System.out.println("   - DRY principle");
        System.out.println("   - Consistent link generation");
        System.out.println("   - Easy to test");
        System.out.println("   - Separation of concerns");

        System.out.println("\n3. Usage:");
        System.out.println("   Entity → Assembler → EntityModel<Entity>");
        System.out.println("   Assembler adds all necessary links");

        System.out.println("\n4. Example:");
        ProductAssembler assembler = new ProductAssembler();
        Product product = new Product(1L, "Laptop", 999.99);
        EntityModel<Product> resource = assembler.toModel(product);
        System.out.println("   Product: " + resource.getContent().getName());
        System.out.println("   Links: " + resource.getLinks().size());

        System.out.println("\n5. Key Methods:");
        System.out.println("   - toModel(): Entity → EntityModel");
        System.out.println("   - toCollectionModel(): List → CollectionModel");
    }

    /**
     * Product Entity
     */
    static class Product {
        private Long id;
        private String name;
        private double price;

        public Product(Long id, String name, double price) {
            this.id = id;
            this.name = name;
            this.price = price;
        }

        public Long getId() { return id; }
        public String getName() { return name; }
        public double getPrice() { return price; }
    }

    /**
     * Product Resource Assembler
     */
    @Component
    static class ProductAssembler implements RepresentationModelAssembler<Product, EntityModel<Product>> {

        @Override
        public EntityModel<Product> toModel(Product product) {
            return EntityModel.of(product,
                linkTo(methodOn(ProductController.class).getProduct(product.getId())).withSelfRel(),
                linkTo(methodOn(ProductController.class).getAllProducts()).withRel("products"),
                linkTo(methodOn(ProductController.class).getProductReviews(product.getId())).withRel("reviews")
            );
        }
    }

    /**
     * Product Controller
     */
    @RestController
    @RequestMapping("/api/products")
    static class ProductController {

        private final ProductAssembler assembler = new ProductAssembler();

        @GetMapping("/{id}")
        public EntityModel<Product> getProduct(@PathVariable Long id) {
            Product product = new Product(id, "Product " + id, 99.99);
            return assembler.toModel(product);
        }

        @GetMapping
        public String getAllProducts() {
            return "All products";
        }

        @GetMapping("/{id}/reviews")
        public String getProductReviews(@PathVariable Long id) {
            return "Reviews for product " + id;
        }
    }
}
