package com.example.templateengine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Arrays;
import java.util.List;

/**
 * Fragment Pattern
 * 
 * Demonstrates using template fragments for reusable UI components.
 * Fragments are reusable template pieces that can be included across pages.
 * 
 * Features:
 * - Reusable UI components
 * - Parameterized fragments
 * - Fragment expressions
 * - Conditional fragments
 * - Fragment composition
 * 
 * Benefits:
 * - DRY (Don't Repeat Yourself)
 * - Maintainability
 * - Consistency
 * - Testability
 */
@SpringBootApplication
public class FragmentPattern {

    public static void main(String[] args) {
        SpringApplication.run(FragmentPattern.class, args);
    }

    @Controller
    public static class FragmentController {

        @GetMapping("/fragments/demo")
        public String fragmentDemo(Model model) {
            model.addAttribute("user", new User("John Doe", "john@example.com", "ADMIN"));
            model.addAttribute("notifications", Arrays.asList(
                new Notification("New message", "info"),
                new Notification("Warning!", "warning"),
                new Notification("Error occurred", "error")
            ));
            model.addAttribute("products", Arrays.asList(
                new Product("Laptop", 999.99, true),
                new Product("Mouse", 29.99, false)
            ));
            return "fragments/demo";
        }
    }

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

    public static class Notification {
        private String message;
        private String type;

        public Notification(String message, String type) {
            this.message = message;
            this.type = type;
        }

        public String getMessage() { return message; }
        public String getType() { return type; }
    }

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
    }
}

/*
 * Thymeleaf Fragment Examples:
 * 
 * ===== Fragment Definition (templates/fragments/common.html) =====
 * 
 * <!DOCTYPE html>
 * <html xmlns:th="http://www.thymeleaf.org">
 * <body>
 * 
 * <!-- Simple Fragment -->
 * <div th:fragment="header">
 *     <header>
 *         <h1>My Application</h1>
 *         <nav>
 *             <a th:href="@{/}">Home</a>
 *             <a th:href="@{/about}">About</a>
 *         </nav>
 *     </header>
 * </div>
 * 
 * <!-- Parameterized Fragment -->
 * <div th:fragment="alert(type, message)">
 *     <div th:class="'alert alert-' + ${type}">
 *         <span th:text="${message}">Alert message</span>
 *     </div>
 * </div>
 * 
 * <!-- Fragment with th:block (no wrapper element) -->
 * <th:block th:fragment="userInfo(user)">
 *     <span th:text="${user.name}">Name</span>
 *     <span th:text="${user.email}">Email</span>
 *     <span th:text="${user.role}">Role</span>
 * </th:block>
 * 
 * <!-- Fragment with default values -->
 * <div th:fragment="card(title, content)">
 *     <div class="card">
 *         <div class="card-header" th:text="${title ?: 'Default Title'}">Title</div>
 *         <div class="card-body" th:text="${content ?: 'No content'}">Content</div>
 *     </div>
 * </div>
 * 
 * <!-- Fragment Selection by ID -->
 * <footer id="footer-fragment">
 *     <p>&copy; 2024 My App</p>
 * </footer>
 * 
 * <!-- Fragment with iteration -->
 * <ul th:fragment="list(items)">
 *     <li th:each="item : ${items}" th:text="${item}">Item</li>
 * </ul>
 * 
 * </body>
 * </html>
 * 
 * 
 * ===== Using Fragments (templates/fragments/demo.html) =====
 * 
 * <!DOCTYPE html>
 * <html xmlns:th="http://www.thymeleaf.org">
 * <head>
 *     <title>Fragment Demo</title>
 * </head>
 * <body>
 * 
 * <!-- Insert fragment (keeps wrapper) -->
 * <div th:insert="~{fragments/common :: header}">Header placeholder</div>
 * 
 * <!-- Replace with fragment (replaces wrapper) -->
 * <div th:replace="~{fragments/common :: header}">Header placeholder</div>
 * 
 * <!-- Include fragment content (no wrapper) -->
 * <div th:include="~{fragments/common :: header}">Header placeholder</div>
 * 
 * <!-- Parameterized fragment -->
 * <div th:replace="~{fragments/common :: alert('success', 'Operation completed!')}"></div>
 * <div th:replace="~{fragments/common :: alert('error', 'Something went wrong!')}"></div>
 * 
 * <!-- User info fragment -->
 * <div th:replace="~{fragments/common :: userInfo(${user})}"></div>
 * 
 * <!-- Fragment by ID -->
 * <div th:replace="~{fragments/common :: #footer-fragment}"></div>
 * 
 * <!-- Conditional fragments -->
 * <div th:if="${user.role == 'ADMIN'}" 
 *      th:insert="~{fragments/admin :: adminPanel}"></div>
 * 
 * <!-- Loop with fragments -->
 * <div th:each="notification : ${notifications}">
 *     <div th:replace="~{fragments/common :: alert(${notification.type}, ${notification.message})}"></div>
 * </div>
 * 
 * <!-- Fragment expression in variable -->
 * <div th:with="frag = ~{fragments/common :: header}">
 *     <div th:replace="${frag}"></div>
 * </div>
 * 
 * </body>
 * </html>
 * 
 * 
 * ===== Advanced Fragment (templates/fragments/product.html) =====
 * 
 * <div th:fragment="productCard(product)" class="product-card">
 *     <h3 th:text="${product.name}">Product Name</h3>
 *     <p class="price">$<span th:text="${#numbers.formatDecimal(product.price, 1, 2)}">0.00</span></p>
 *     <p th:if="${product.available}" class="status available">In Stock</p>
 *     <p th:unless="${product.available}" class="status out-of-stock">Out of Stock</p>
 *     <button th:if="${product.available}" 
 *             th:onclick="'addToCart(' + ${product.id} + ')'">Add to Cart</button>
 * </div>
 * 
 * 
 * FreeMarker Fragment Examples:
 * 
 * ===== Fragment Definition (fragments/common.ftl) =====
 * 
 * <#-- Simple Macro -->
 * <#macro header>
 *     <header>
 *         <h1>My Application</h1>
 *         <nav>
 *             <a href="/">Home</a>
 *             <a href="/about">About</a>
 *         </nav>
 *     </header>
 * </#macro>
 * 
 * <#-- Parameterized Macro -->
 * <#macro alert type message>
 *     <div class="alert alert-${type}">
 *         ${message}
 *     </div>
 * </#macro>
 * 
 * <#-- Macro with nested content -->
 * <#macro card title>
 *     <div class="card">
 *         <div class="card-header">${title}</div>
 *         <div class="card-body">
 *             <#nested>
 *         </div>
 *     </div>
 * </#macro>
 * 
 * 
 * ===== Using Fragments (demo.ftl) =====
 * 
 * <#import "fragments/common.ftl" as common>
 * 
 * <!DOCTYPE html>
 * <html>
 * <body>
 * 
 * <!-- Use macro -->
 * <@common.header />
 * 
 * <!-- Parameterized macro -->
 * <@common.alert type="success" message="Operation completed!" />
 * 
 * <!-- Macro with nested content -->
 * <@common.card title="User Profile">
 *     <p>Name: ${user.name}</p>
 *     <p>Email: ${user.email}</p>
 * </@common.card>
 * 
 * </body>
 * </html>
 * 
 * 
 * Fragment Syntax Comparison:
 * 
 * Thymeleaf:
 * - th:fragment="name"             - Define fragment
 * - th:fragment="name(param1, ...)" - Parameterized fragment
 * - th:insert="~{file :: fragment}" - Insert fragment
 * - th:replace="~{file :: fragment}" - Replace with fragment
 * - ~{file :: #id}                 - Fragment by ID
 * 
 * FreeMarker:
 * - <#macro name>...</#macro>      - Define macro
 * - <#macro name param1...>        - Parameterized macro
 * - <@name />                      - Use macro
 * - <#import "file" as alias>      - Import macros
 * - <#include "file">              - Include template
 * - <#nested>                      - Nested content
 * 
 * JSP:
 * - <jsp:include page="file.jsp"/> - Include JSP
 * - <%@ include file="file.jsp" %> - Static include
 * - <jsp:param name="x" value="y"/> - Pass parameters
 * - <c:import url="file.jsp"/>     - JSTL import
 * 
 * 
 * Best Practices:
 * 
 * 1. Keep fragments small and focused
 * 2. Use meaningful names for fragments
 * 3. Document fragment parameters
 * 4. Make fragments reusable and generic
 * 5. Avoid tight coupling between fragments
 * 6. Use fragment composition for complex components
 * 7. Cache frequently used fragments
 * 8. Test fragments independently
 * 9. Version fragment interfaces
 * 10. Consider performance implications
 */
