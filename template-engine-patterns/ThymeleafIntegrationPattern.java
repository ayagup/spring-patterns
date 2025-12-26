package com.example.templateengine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import org.thymeleaf.templatemode.TemplateMode;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Thymeleaf Integration Pattern
 * 
 * Demonstrates integration of Thymeleaf template engine with Spring Boot.
 * Thymeleaf is a modern server-side Java template engine for web and standalone environments.
 * 
 * Features:
 * - Natural templates (can be viewed in browser without server)
 * - Strong integration with Spring MVC
 * - Expression language (OGNL-based)
 * - Fragments and layouts
 * - Internationalization support
 * - Security integration
 */
@SpringBootApplication
public class ThymeleafIntegrationPattern {

    public static void main(String[] args) {
        SpringApplication.run(ThymeleafIntegrationPattern.class, args);
    }

    /**
     * Thymeleaf Template Resolver Configuration
     */
    @Bean
    public SpringResourceTemplateResolver templateResolver() {
        SpringResourceTemplateResolver templateResolver = new SpringResourceTemplateResolver();
        templateResolver.setPrefix("classpath:/templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setCacheable(false); // Set to true in production
        templateResolver.setCharacterEncoding("UTF-8");
        return templateResolver;
    }

    /**
     * Thymeleaf Template Engine Configuration
     */
    @Bean
    public SpringTemplateEngine templateEngine() {
        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(templateResolver());
        templateEngine.setEnableSpringELCompiler(true);
        return templateEngine;
    }

    /**
     * Thymeleaf View Resolver Configuration
     */
    @Bean
    public ThymeleafViewResolver viewResolver() {
        ThymeleafViewResolver viewResolver = new ThymeleafViewResolver();
        viewResolver.setTemplateEngine(templateEngine());
        viewResolver.setCharacterEncoding("UTF-8");
        viewResolver.setOrder(1);
        return viewResolver;
    }

    /**
     * Controller demonstrating Thymeleaf usage
     */
    @Controller
    public static class ThymeleafController {

        @GetMapping("/")
        public String index(Model model) {
            model.addAttribute("message", "Welcome to Thymeleaf!");
            model.addAttribute("currentTime", LocalDateTime.now());
            return "index";
        }

        @GetMapping("/users")
        public String users(Model model) {
            List<User> users = Arrays.asList(
                new User(1L, "John Doe", "john@example.com", true),
                new User(2L, "Jane Smith", "jane@example.com", false),
                new User(3L, "Bob Johnson", "bob@example.com", true)
            );
            model.addAttribute("users", users);
            model.addAttribute("pageTitle", "User List");
            return "users";
        }

        @GetMapping("/form")
        public String showForm(Model model) {
            model.addAttribute("user", new User());
            return "form";
        }

        @PostMapping("/form")
        public String submitForm(@RequestParam String name, 
                                @RequestParam String email,
                                Model model) {
            model.addAttribute("success", true);
            model.addAttribute("userName", name);
            return "form";
        }

        @GetMapping("/fragments")
        public String fragments(Model model) {
            model.addAttribute("title", "Fragment Example");
            return "fragments";
        }

        @GetMapping("/conditional")
        public String conditional(Model model) {
            model.addAttribute("loggedIn", true);
            model.addAttribute("role", "ADMIN");
            return "conditional";
        }

        @GetMapping("/iterations")
        public String iterations(Model model) {
            List<Product> products = Arrays.asList(
                new Product("P001", "Laptop", 999.99, true),
                new Product("P002", "Mouse", 29.99, true),
                new Product("P003", "Keyboard", 79.99, false),
                new Product("P004", "Monitor", 299.99, true)
            );
            model.addAttribute("products", products);
            return "iterations";
        }
    }

    /**
     * User model for demonstration
     */
    public static class User {
        private Long id;
        private String name;
        private String email;
        private boolean active;

        public User() {}

        public User(Long id, String name, String email, boolean active) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.active = active;
        }

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
    }

    /**
     * Product model for demonstration
     */
    public static class Product {
        private String code;
        private String name;
        private double price;
        private boolean inStock;

        public Product(String code, String name, double price, boolean inStock) {
            this.code = code;
            this.name = name;
            this.price = price;
            this.inStock = inStock;
        }

        // Getters
        public String getCode() { return code; }
        public String getName() { return name; }
        public double getPrice() { return price; }
        public boolean isInStock() { return inStock; }
    }
}

/*
 * Example Thymeleaf Template (templates/index.html):
 * 
 * <!DOCTYPE html>
 * <html xmlns:th="http://www.thymeleaf.org">
 * <head>
 *     <title>Thymeleaf Example</title>
 * </head>
 * <body>
 *     <h1 th:text="${message}">Default Message</h1>
 *     <p>Current time: <span th:text="${currentTime}"></span></p>
 * </body>
 * </html>
 * 
 * 
 * Example with iteration (templates/users.html):
 * 
 * <!DOCTYPE html>
 * <html xmlns:th="http://www.thymeleaf.org">
 * <head>
 *     <title th:text="${pageTitle}">User List</title>
 * </head>
 * <body>
 *     <h1>Users</h1>
 *     <table>
 *         <thead>
 *             <tr>
 *                 <th>ID</th>
 *                 <th>Name</th>
 *                 <th>Email</th>
 *                 <th>Status</th>
 *             </tr>
 *         </thead>
 *         <tbody>
 *             <tr th:each="user : ${users}">
 *                 <td th:text="${user.id}">1</td>
 *                 <td th:text="${user.name}">John</td>
 *                 <td th:text="${user.email}">john@example.com</td>
 *                 <td th:text="${user.active} ? 'Active' : 'Inactive'">Active</td>
 *             </tr>
 *         </tbody>
 *     </table>
 * </body>
 * </html>
 * 
 * 
 * Common Thymeleaf Expressions:
 * 
 * ${...}     - Variable expressions (model attributes)
 * *{...}     - Selection expressions (selected object)
 * #{...}     - Message expressions (i18n)
 * @{...}     - Link URL expressions
 * ~{...}     - Fragment expressions
 * 
 * th:text    - Set text content
 * th:utext   - Set unescaped text content
 * th:if      - Conditional rendering
 * th:unless  - Negated conditional
 * th:each    - Iteration
 * th:object  - Select object for *{} expressions
 * th:field   - Form field binding
 * th:href    - Set href attribute
 * th:src     - Set src attribute
 * th:attr    - Set any attribute
 */
