package com.example.templateengine;

import com.samskivert.mustache.Mustache;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.view.MustacheViewResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mustache Integration Pattern
 * 
 * Demonstrates integration of Mustache template engine with Spring Boot.
 * Mustache is a logic-less template syntax that works in many languages.
 * 
 * Features:
 * - Logic-less templates (no if/else in templates)
 * - Simple syntax
 * - Language-agnostic
 * - Fast rendering
 * - Pre-compilation support
 * - Lambda expressions
 * 
 * Spring Boot provides auto-configuration for Mustache.
 */
@SpringBootApplication
public class MustacheIntegrationPattern {

    public static void main(String[] args) {
        SpringApplication.run(MustacheIntegrationPattern.class, args);
    }

    /**
     * Mustache Compiler Configuration (optional)
     * Spring Boot auto-configures this by default
     */
    @Bean
    public Mustache.Compiler mustacheCompiler() {
        return Mustache.compiler()
            .defaultValue("N/A")
            .escapeHTML(true)
            .standardsMode(false)
            .withFormatter(new Mustache.Formatter() {
                @Override
                public String format(Object value) {
                    if (value instanceof Double) {
                        return String.format("%.2f", value);
                    }
                    return String.valueOf(value);
                }
            });
    }

    /**
     * Mustache View Resolver Configuration (optional)
     * Spring Boot auto-configures this by default
     */
    @Bean
    public MustacheViewResolver mustacheViewResolver() {
        MustacheViewResolver resolver = new MustacheViewResolver();
        resolver.setPrefix("classpath:/templates/");
        resolver.setSuffix(".mustache");
        resolver.setCache(true);
        resolver.setContentType("text/html;charset=UTF-8");
        return resolver;
    }

    /**
     * Controller demonstrating Mustache usage
     */
    @Controller
    public static class MustacheController {

        @GetMapping("/mustache")
        public String index(Model model) {
            model.addAttribute("title", "Mustache Example");
            model.addAttribute("message", "Welcome to Mustache!");
            model.addAttribute("showMessage", true);
            return "mustache/index";
        }

        @GetMapping("/mustache/list")
        public String listExample(Model model) {
            List<Product> products = Arrays.asList(
                new Product("Laptop", 999.99, true),
                new Product("Mouse", 29.99, true),
                new Product("Keyboard", 79.99, false),
                new Product("Monitor", 299.99, true)
            );
            model.addAttribute("products", products);
            model.addAttribute("hasProducts", !products.isEmpty());
            return "mustache/list";
        }

        @GetMapping("/mustache/sections")
        public String sectionsExample(Model model) {
            Map<String, Object> data = new HashMap<>();
            data.put("person", new Person("John Doe", "john@example.com"));
            data.put("showPerson", true);
            data.put("items", Arrays.asList("Apple", "Banana", "Orange"));
            data.put("emptyList", Arrays.asList());
            
            model.addAllAttributes(data);
            return "mustache/sections";
        }

        @GetMapping("/mustache/partials")
        public String partialsExample(Model model) {
            List<User> users = Arrays.asList(
                new User("Alice", "alice@example.com", "Admin"),
                new User("Bob", "bob@example.com", "User"),
                new User("Charlie", "charlie@example.com", "Guest")
            );
            model.addAttribute("users", users);
            return "mustache/partials";
        }

        @GetMapping("/mustache/lambda")
        public String lambdaExample(Model model) {
            model.addAttribute("name", "world");
            model.addAttribute("bold", new Mustache.Lambda() {
                @Override
                public void execute(Template.Fragment frag, Writer out) 
                    throws java.io.IOException {
                    out.write("<b>" + frag.execute() + "</b>");
                }
            });
            return "mustache/lambda";
        }
    }

    /**
     * Product model
     */
    public static class Product {
        private String name;
        private double price;
        private boolean available;

        public Product(String name, double price, boolean available) {
            this.name = name;
            this.price = price;
            this.available = available;
        }

        public String getName() { return name; }
        public double getPrice() { return price; }
        public boolean isAvailable() { return available; }
        public boolean isNotAvailable() { return !available; }
    }

    /**
     * Person model
     */
    public static class Person {
        private String name;
        private String email;

        public Person(String name, String email) {
            this.name = name;
            this.email = email;
        }

        public String getName() { return name; }
        public String getEmail() { return email; }
    }

    /**
     * User model
     */
    public static class User {
        private String name;
        private String email;
        private String role;

        public User(String name, String email, String role) {
            this.name = name;
            this.email = email;
            this.role = role;
        }

        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getRole() { return role; }
    }
}

/*
 * Example Mustache Template (templates/mustache/index.mustache):
 * 
 * <!DOCTYPE html>
 * <html>
 * <head>
 *     <title>{{title}}</title>
 * </head>
 * <body>
 *     {{#showMessage}}
 *         <h1>{{message}}</h1>
 *     {{/showMessage}}
 *     <p>This is a Mustache template example.</p>
 * </body>
 * </html>
 * 
 * 
 * Example with Lists (templates/mustache/list.mustache):
 * 
 * <!DOCTYPE html>
 * <html>
 * <head>
 *     <title>Product List</title>
 * </head>
 * <body>
 *     <h1>Products</h1>
 *     {{#hasProducts}}
 *         <table>
 *             <thead>
 *                 <tr>
 *                     <th>Name</th>
 *                     <th>Price</th>
 *                     <th>Status</th>
 *                 </tr>
 *             </thead>
 *             <tbody>
 *                 {{#products}}
 *                 <tr>
 *                     <td>{{name}}</td>
 *                     <td>${{price}}</td>
 *                     <td>
 *                         {{#available}}
 *                             <span class="badge-success">Available</span>
 *                         {{/available}}
 *                         {{#notAvailable}}
 *                             <span class="badge-danger">Out of Stock</span>
 *                         {{/notAvailable}}
 *                     </td>
 *                 </tr>
 *                 {{/products}}
 *             </tbody>
 *         </table>
 *     {{/hasProducts}}
 *     {{^hasProducts}}
 *         <p>No products available.</p>
 *     {{/hasProducts}}
 * </body>
 * </html>
 * 
 * 
 * Example with Sections (templates/mustache/sections.mustache):
 * 
 * <!DOCTYPE html>
 * <html>
 * <body>
 *     {{! This is a comment }}
 *     
 *     {{! Section - renders if value is truthy }}
 *     {{#showPerson}}
 *         <div class="person">
 *             <p>Name: {{person.name}}</p>
 *             <p>Email: {{person.email}}</p>
 *         </div>
 *     {{/showPerson}}
 *     
 *     {{! Inverted Section - renders if value is falsy }}
 *     {{^showPerson}}
 *         <p>No person data available.</p>
 *     {{/showPerson}}
 *     
 *     {{! List iteration }}
 *     <h2>Items:</h2>
 *     <ul>
 *         {{#items}}
 *             <li>{{.}}</li>
 *         {{/items}}
 *     </ul>
 *     
 *     {{! Empty list handling }}
 *     {{^emptyList}}
 *         <p>Empty list is indeed empty!</p>
 *     {{/emptyList}}
 * </body>
 * </html>
 * 
 * 
 * Mustache Syntax:
 * 
 * {{variable}}        - Variable interpolation (HTML escaped)
 * {{{variable}}}      - Unescaped variable
 * {{&variable}}       - Alternative unescaped syntax
 * {{#section}}...{{/section}}  - Section (truthy check/loop)
 * {{^section}}...{{/section}}  - Inverted section (falsy check)
 * {{! comment}}       - Comment
 * {{>partial}}        - Partial (include)
 * {{.}}               - Current item in iteration
 * {{=<% %>=}}         - Change delimiters
 * 
 * Sections behavior:
 * - False/null: Don't render
 * - Non-empty list: Iterate
 * - Lambda: Execute function
 * - Other truthy: Render once
 * 
 * Spring Boot application.properties:
 * spring.mustache.prefix=classpath:/templates/
 * spring.mustache.suffix=.mustache
 * spring.mustache.cache=true
 * spring.mustache.charset=UTF-8
 * spring.mustache.check-template-location=true
 */
