package com.example.templateengine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Arrays;
import java.util.List;

/**
 * Layout Pattern
 * 
 * Demonstrates template layout patterns for creating reusable page structures.
 * Layouts help maintain consistent page structure across an application.
 * 
 * Features:
 * - Master page/layout templates
 * - Content sections/blocks
 * - Template inheritance
 * - Nested layouts
 * - Conditional layouts
 * 
 * Common Approaches:
 * 1. Thymeleaf Layout Dialect
 * 2. SiteMesh
 * 3. Tiles
 * 4. Custom fragments
 */
@SpringBootApplication
public class LayoutPattern {

    public static void main(String[] args) {
        SpringApplication.run(LayoutPattern.class, args);
    }

    @Controller
    public static class LayoutController {

        @GetMapping("/layout/home")
        public String home(Model model) {
            model.addAttribute("pageTitle", "Home Page");
            model.addAttribute("message", "Welcome to the home page!");
            return "layouts/home";
        }

        @GetMapping("/layout/about")
        public String about(Model model) {
            model.addAttribute("pageTitle", "About Us");
            model.addAttribute("company", "Acme Corp");
            return "layouts/about";
        }

        @GetMapping("/layout/dashboard")
        public String dashboard(Model model) {
            model.addAttribute("pageTitle", "Dashboard");
            model.addAttribute("stats", Arrays.asList(
                new Stat("Users", "1,234"),
                new Stat("Orders", "567"),
                new Stat("Revenue", "$12,345")
            ));
            return "layouts/dashboard";
        }
    }

    public static class Stat {
        private String name;
        private String value;

        public Stat(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public String getName() { return name; }
        public String getValue() { return value; }
    }
}

/*
 * Thymeleaf Layout Dialect Example:
 * 
 * ===== Main Layout Template (templates/layouts/main.html) =====
 * 
 * <!DOCTYPE html>
 * <html xmlns:th="http://www.thymeleaf.org"
 *       xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout">
 * <head>
 *     <meta charset="UTF-8"/>
 *     <title layout:title-pattern="$CONTENT_TITLE - $LAYOUT_TITLE">My App</title>
 *     
 *     <!-- Common CSS -->
 *     <link rel="stylesheet" th:href="@{/css/bootstrap.min.css}"/>
 *     <link rel="stylesheet" th:href="@{/css/main.css}"/>
 *     
 *     <!-- Page-specific CSS -->
 *     <th:block layout:fragment="css"></th:block>
 * </head>
 * <body>
 *     <!-- Header -->
 *     <header>
 *         <nav class="navbar">
 *             <a th:href="@{/}">Home</a>
 *             <a th:href="@{/about}">About</a>
 *             <a th:href="@{/contact}">Contact</a>
 *         </nav>
 *     </header>
 *     
 *     <!-- Main Content -->
 *     <main>
 *         <div layout:fragment="content">
 *             <!-- Content goes here -->
 *         </div>
 *     </main>
 *     
 *     <!-- Footer -->
 *     <footer>
 *         <p>&copy; 2024 My App. All rights reserved.</p>
 *     </footer>
 *     
 *     <!-- Common JavaScript -->
 *     <script th:src="@{/js/jquery.min.js}"></script>
 *     <script th:src="@{/js/bootstrap.min.js}"></script>
 *     
 *     <!-- Page-specific JavaScript -->
 *     <th:block layout:fragment="scripts"></th:block>
 * </body>
 * </html>
 * 
 * 
 * ===== Content Page (templates/layouts/home.html) =====
 * 
 * <!DOCTYPE html>
 * <html xmlns:th="http://www.thymeleaf.org"
 *       xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout"
 *       layout:decorate="~{layouts/main}">
 * <head>
 *     <title th:text="${pageTitle}">Home</title>
 *     
 *     <!-- Page-specific CSS -->
 *     <th:block layout:fragment="css">
 *         <link rel="stylesheet" th:href="@{/css/home.css}"/>
 *     </th:block>
 * </head>
 * <body>
 *     <!-- Page content -->
 *     <div layout:fragment="content">
 *         <h1 th:text="${pageTitle}">Home Page</h1>
 *         <p th:text="${message}">Welcome message</p>
 *     </div>
 *     
 *     <!-- Page-specific JavaScript -->
 *     <th:block layout:fragment="scripts">
 *         <script th:src="@{/js/home.js}"></script>
 *     </th:block>
 * </body>
 * </html>
 * 
 * 
 * ===== Dashboard Layout (templates/layouts/dashboard-layout.html) =====
 * 
 * <!DOCTYPE html>
 * <html xmlns:th="http://www.thymeleaf.org"
 *       xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout"
 *       layout:decorate="~{layouts/main}">
 * <body>
 *     <div layout:fragment="content">
 *         <div class="dashboard">
 *             <!-- Sidebar -->
 *             <aside class="sidebar">
 *                 <ul>
 *                     <li><a href="/dashboard">Overview</a></li>
 *                     <li><a href="/dashboard/users">Users</a></li>
 *                     <li><a href="/dashboard/orders">Orders</a></li>
 *                     <li><a href="/dashboard/settings">Settings</a></li>
 *                 </ul>
 *             </aside>
 *             
 *             <!-- Main dashboard content -->
 *             <div class="dashboard-content">
 *                 <div layout:fragment="dashboard-content">
 *                     <!-- Dashboard pages fill this -->
 *                 </div>
 *             </div>
 *         </div>
 *     </div>
 * </body>
 * </html>
 * 
 * 
 * ===== Dashboard Page (templates/layouts/dashboard.html) =====
 * 
 * <!DOCTYPE html>
 * <html xmlns:th="http://www.thymeleaf.org"
 *       xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout"
 *       layout:decorate="~{layouts/dashboard-layout}">
 * <head>
 *     <title>Dashboard</title>
 * </head>
 * <body>
 *     <div layout:fragment="dashboard-content">
 *         <h1>Dashboard Overview</h1>
 *         <div class="stats">
 *             <div th:each="stat : ${stats}" class="stat-card">
 *                 <h3 th:text="${stat.name}">Stat Name</h3>
 *                 <p th:text="${stat.value}">Value</p>
 *             </div>
 *         </div>
 *     </div>
 * </body>
 * </html>
 * 
 * 
 * Thymeleaf Layout Dialect Maven Dependency:
 * 
 * <dependency>
 *     <groupId>nz.net.ultraq.thymeleaf</groupId>
 *     <artifactId>thymeleaf-layout-dialect</artifactId>
 *     <version>3.1.0</version>
 * </dependency>
 * 
 * 
 * Layout Attributes:
 * 
 * layout:decorate          - Specify parent layout
 * layout:fragment          - Define replaceable section
 * layout:insert            - Insert fragment
 * layout:replace           - Replace fragment
 * layout:title-pattern     - Title formatting pattern
 * layout:decorator         - Legacy decorate attribute
 * 
 * 
 * FreeMarker Layout Example:
 * 
 * ===== Main Layout (layouts/main.ftl) =====
 * 
 * <!DOCTYPE html>
 * <html>
 * <head>
 *     <title>${title!'My App'}</title>
 *     <#include "common/head.ftl">
 *     <#if additionalCss??>
 *         ${additionalCss}
 *     </#if>
 * </head>
 * <body>
 *     <#include "common/header.ftl">
 *     
 *     <main>
 *         ${content}
 *     </main>
 *     
 *     <#include "common/footer.ftl">
 *     
 *     <#if additionalScripts??>
 *         ${additionalScripts}
 *     </#if>
 * </body>
 * </html>
 * 
 * 
 * Layout Best Practices:
 * 
 * 1. Use consistent naming conventions for fragments
 * 2. Define clear content areas (content, sidebar, scripts, css)
 * 3. Keep layouts DRY (Don't Repeat Yourself)
 * 4. Support multiple layout levels (main -> dashboard -> specific page)
 * 5. Make layouts flexible with optional fragments
 * 6. Use fragments for reusable components
 * 7. Optimize fragment caching
 * 8. Document fragment contracts
 */
