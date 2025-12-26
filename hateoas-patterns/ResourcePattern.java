package com.example.hateoas.patterns;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.Link;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

/**
 * Resource Pattern (Deprecated - use RepresentationModel)
 * 
 * Demonstrates the legacy Resource pattern in Spring HATEOAS.
 * In modern HATEOAS, this is replaced by RepresentationModel.
 * Resources add hypermedia links to domain objects.
 * 
 * Key Features:
 * - Wrap domain objects with links
 * - Self-referential links
 * - Related resource links
 * - Navigation links
 * - HATEOAS compliance
 * - RESTful resource representation
 */
@SpringBootApplication
public class ResourcePattern implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(ResourcePattern.class, args);
    }

    @Override
    public void run(String... args) {
        System.out.println("=== Resource Pattern (RepresentationModel) ===\n");
        System.out.println("1. Resource Pattern Purpose:");
        System.out.println("   - Add hypermedia links to resources");
        System.out.println("   - Enable client navigation");
        System.out.println("   - Follow HATEOAS principles");
        System.out.println("   - Decouple client from server URLs");

        System.out.println("\n2. Evolution:");
        System.out.println("   Old: Resource<T> (deprecated)");
        System.out.println("   New: EntityModel<T>");
        System.out.println("   Base: RepresentationModel<T>");

        System.out.println("\n3. Link Types:");
        System.out.println("   - self: Link to current resource");
        System.out.println("   - collection: Link to collection");
        System.out.println("   - related: Links to related resources");
        System.out.println("   - custom: Application-specific links");

        System.out.println("\n4. Example Resource:");
        UserResource userResource = createUserResource();
        System.out.println("   User: " + userResource.getUsername());
        System.out.println("   Email: " + userResource.getEmail());
        System.out.println("   Links: " + userResource.getLinks().size());

        System.out.println("\n5. Benefits:");
        System.out.println("   - Discoverability");
        System.out.println("   - Loose coupling");
        System.out.println("   - Evolvability");
        System.out.println("   - Self-documentation");

        System.out.println("\n6. HATEOAS Level:");
        System.out.println("   Richardson Maturity Model - Level 3");
        System.out.println("   Hypermedia as the Engine of Application State");
    }

    private UserResource createUserResource() {
        UserResource resource = new UserResource(1L, "john_doe", "john@example.com");
        
        // Add self link
        resource.add(Link.of("/api/users/1", "self"));
        
        // Add related links
        resource.add(Link.of("/api/users/1/orders", "orders"));
        resource.add(Link.of("/api/users/1/profile", "profile"));
        
        return resource;
    }

    /**
     * User Resource (extends RepresentationModel)
     */
    static class UserResource extends RepresentationModel<UserResource> {
        private Long id;
        private String username;
        private String email;

        public UserResource(Long id, String username, String email) {
            this.id = id;
            this.username = username;
            this.email = email;
        }

        public Long getId() { return id; }
        public String getUsername() { return username; }
        public String getEmail() { return email; }
    }

    /**
     * REST Controller demonstrating Resource Pattern
     */
    @RestController
    @RequestMapping("/api/users")
    static class UserController {

        @GetMapping("/{id}")
        public UserResource getUser(@PathVariable Long id) {
            UserResource resource = new UserResource(id, "user_" + id, "user" + id + "@example.com");
            
            // Add self link
            resource.add(linkTo(methodOn(UserController.class).getUser(id)).withSelfRel());
            
            // Add collection link
            resource.add(linkTo(UserController.class).withRel("users"));
            
            return resource;
        }
    }
}
