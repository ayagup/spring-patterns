package com.example.hateoas.patterns;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.hateoas.Affordance;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

/**
 * Affordance Pattern
 * 
 * Demonstrates Affordances in Spring HATEOAS.
 * Affordances describe what actions are available on a resource,
 * including supported HTTP methods, media types, and parameters.
 * 
 * Key Features:
 * - Describe available actions
 * - HTTP method support
 * - Media type negotiation
 * - Request/response metadata
 * - Dynamic action discovery
 * - Self-describing APIs
 */
@SpringBootApplication
public class AffordancePattern implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(AffordancePattern.class, args);
    }

    @Override
    public void run(String... args) {
        System.out.println("=== Affordance Pattern ===\n");
        System.out.println("1. What are Affordances?");
        System.out.println("   - Actions available on a resource");
        System.out.println("   - HTTP methods supported");
        System.out.println("   - Media types accepted");
        System.out.println("   - Request/response formats");
        System.out.println("   - Operation metadata");

        System.out.println("\n2. Affordance Information:");
        System.out.println("   - HTTP Method (GET, POST, PUT, DELETE, etc.)");
        System.out.println("   - URI template");
        System.out.println("   - Input media types");
        System.out.println("   - Output media types");
        System.out.println("   - Required/optional parameters");

        System.out.println("\n3. Example Resource with Affordances:");
        System.out.println("   GET /api/orders/1");
        System.out.println("   Affordances:");
        System.out.println("   - GET: Retrieve order");
        System.out.println("   - PUT: Update order");
        System.out.println("   - DELETE: Cancel order");
        System.out.println("   - POST /api/orders/1/pay: Pay for order");

        System.out.println("\n4. Media Type Formats:");
        System.out.println("   - HAL: Shows affordances in _links");
        System.out.println("   - HAL-FORMS: Detailed forms with affordances");
        System.out.println("   - Collection+JSON: Template-based");
        System.out.println("   - Siren: Actions with fields");

        System.out.println("\n5. Benefits:");
        System.out.println("   - Client discoverability");
        System.out.println("   - Dynamic action binding");
        System.out.println("   - Self-describing APIs");
        System.out.println("   - Reduced coupling");
        System.out.println("   - Automatic UI generation possible");

        System.out.println("\n6. Creating Affordances:");
        System.out.println("   Link with affordance:");
        System.out.println("   linkTo(...).withSelfRel()");
        System.out.println("     .andAffordance(afford(methodOn(...).update(...)))");
        System.out.println("     .andAffordance(afford(methodOn(...).delete(...)))");

        System.out.println("\n7. Use Cases:");
        System.out.println("   - Dynamic form generation");
        System.out.println("   - Action menu creation");
        System.out.println("   - Permission-based actions");
        System.out.println("   - Workflow state transitions");
        System.out.println("   - API documentation");
    }

    /**
     * Order Entity
     */
    static class Order {
        private Long id;
        private String status;
        private double total;

        public Order(Long id, String status, double total) {
            this.id = id;
            this.status = status;
            this.total = total;
        }

        public Long getId() { return id; }
        public String getStatus() { return status; }
        public double getTotal() { return total; }
    }

    /**
     * Order Controller with Affordances
     */
    @RestController
    @RequestMapping("/api/orders")
    static class OrderController {

        @GetMapping("/{id}")
        public EntityModel<Order> getOrder(@PathVariable Long id) {
            Order order = new Order(id, "PENDING", 99.99);
            
            // Create link with affordances
            Link selfLink = linkTo(methodOn(OrderController.class).getOrder(id))
                .withSelfRel()
                .andAffordance(afford(methodOn(OrderController.class).updateOrder(id, null)))
                .andAffordance(afford(methodOn(OrderController.class).deleteOrder(id)));
            
            return EntityModel.of(order, selfLink);
        }

        @PutMapping("/{id}")
        public Order updateOrder(@PathVariable Long id, @RequestBody Order order) {
            return order;
        }

        @DeleteMapping("/{id}")
        public void deleteOrder(@PathVariable Long id) {
            // Delete logic
        }

        @PostMapping("/{id}/pay")
        public Order payOrder(@PathVariable Long id) {
            return new Order(id, "PAID", 99.99);
        }
    }
}
