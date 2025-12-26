package com.example.hateoas.patterns;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.IanaLinkRelations;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

/**
 * Link Pattern
 * 
 * Demonstrates Link creation and management in Spring HATEOAS.
 * Links are the fundamental building blocks of hypermedia APIs.
 * 
 * Key Features:
 * - Link creation with href and rel
 * - Standard link relations (IANA)
 * - Custom link relations
 * - Templated links
 * - Link builders
 * - Link utilities
 */
@SpringBootApplication
public class LinkPattern implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(LinkPattern.class, args);
    }

    @Override
    public void run(String... args) {
        demonstrateLinkCreation();
        demonstrateStandardRelations();
        demonstrateTemplatedLinks();
        demonstrateLinkBuilders();
    }

    private void demonstrateLinkCreation() {
        System.out.println("=== Link Pattern ===\n");
        System.out.println("1. Basic Link Creation:");

        // Simple link
        Link selfLink = Link.of("/api/users/1", "self");
        System.out.println("   Self Link: " + selfLink.getHref());
        System.out.println("   Relation: " + selfLink.getRel());

        // Link with full URL
        Link fullLink = Link.of("https://api.example.com/users/1", "self");
        System.out.println("   Full URL: " + fullLink.getHref());

        System.out.println();
    }

    private void demonstrateStandardRelations() {
        System.out.println("2. Standard Link Relations (IANA):");

        // IANA standard relations
        Link selfRel = Link.of("/api/resource", IanaLinkRelations.SELF);
        Link nextRel = Link.of("/api/resource?page=2", IanaLinkRelations.NEXT);
        Link prevRel = Link.of("/api/resource?page=1", IanaLinkRelations.PREV);
        Link firstRel = Link.of("/api/resource?page=1", IanaLinkRelations.FIRST);
        Link lastRel = Link.of("/api/resource?page=10", IanaLinkRelations.LAST);

        System.out.println("   self: " + selfRel.getHref());
        System.out.println("   next: " + nextRel.getHref());
        System.out.println("   prev: " + prevRel.getHref());
        System.out.println("   first: " + firstRel.getHref());
        System.out.println("   last: " + lastRel.getHref());

        System.out.println("\n   Other IANA Relations:");
        System.out.println("   - about: Reference information");
        System.out.println("   - alternate: Alternate representation");
        System.out.println("   - collection: Collection resource");
        System.out.println("   - edit: Editable resource");
        System.out.println("   - item: Item in collection");

        System.out.println();
    }

    private void demonstrateTemplatedLinks() {
        System.out.println("3. Templated Links:");

        // URI template
        Link templatedLink = Link.of("/api/users{?name,age}", "search");
        System.out.println("   Template: " + templatedLink.getHref());
        System.out.println("   Is Templated: " + templatedLink.isTemplated());

        // Expand template
        Link expandedLink = Link.of("/api/users?name=John&age=30", "search");
        System.out.println("   Expanded: " + expandedLink.getHref());

        System.out.println("\n   Template Variables:");
        System.out.println("   {id} - Path variable");
        System.out.println("   {?param} - Query parameter");
        System.out.println("   {?param1,param2} - Multiple params");

        System.out.println();
    }

    private void demonstrateLinkBuilders() {
        System.out.println("4. Link Builders:");

        // Using WebMvcLinkBuilder
        System.out.println("   WebMvcLinkBuilder:");
        System.out.println("   linkTo(UserController.class).withRel(\"users\")");
        System.out.println("   linkTo(methodOn(...).getUser(1)).withSelfRel()");

        System.out.println("\n5. Link Composition:");
        Link baseLink = Link.of("/api");
        Link usersLink = Link.of(baseLink.getHref() + "/users", "users");
        Link userLink = Link.of(usersLink.getHref() + "/1", "user");
        
        System.out.println("   Base: " + baseLink.getHref());
        System.out.println("   Users: " + usersLink.getHref());
        System.out.println("   User: " + userLink.getHref());

        System.out.println("\n6. Link Affordances:");
        System.out.println("   Links can specify:");
        System.out.println("   - HTTP methods allowed");
        System.out.println("   - Media types supported");
        System.out.println("   - Request/response formats");

        System.out.println("\n7. Custom Relations:");
        Link customLink1 = Link.of("/api/orders/1/invoice", "invoice");
        Link customLink2 = Link.of("/api/orders/1/tracking", "tracking");
        Link customLink3 = Link.of("/api/orders/1/cancel", "cancel");

        System.out.println("   " + customLink1.getRel() + ": " + customLink1.getHref());
        System.out.println("   " + customLink2.getRel() + ": " + customLink2.getHref());
        System.out.println("   " + customLink3.getRel() + ": " + customLink3.getHref());

        System.out.println("\n8. Best Practices:");
        System.out.println("   - Use standard IANA relations when possible");
        System.out.println("   - Make link relations discoverable");
        System.out.println("   - Provide templates for queries");
        System.out.println("   - Include all relevant links");
        System.out.println("   - Document custom relations");
    }
}
