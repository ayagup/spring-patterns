package com.example.templateengine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

/**
 * JSP Integration Pattern
 * 
 * Demonstrates integration of JavaServer Pages (JSP) with Spring Boot.
 * JSP is a legacy technology but still used in many enterprise applications.
 * 
 * Features:
 * - Expression Language (EL)
 * - JSTL (JavaServer Pages Standard Tag Library)
 * - Custom tag libraries
 * - Spring tag library
 * - Form tag library
 * 
 * Note: Spring Boot doesn't recommend JSP for new applications.
 * Consider using Thymeleaf or other modern template engines.
 */
@SpringBootApplication
public class JSPIntegrationPattern {

    public static void main(String[] args) {
        SpringApplication.run(JSPIntegrationPattern.class, args);
    }

    /**
     * JSP View Resolver Configuration
     */
    @Configuration
    @EnableWebMvc
    public static class WebMvcConfig implements WebMvcConfigurer {

        @Override
        public void configureViewResolvers(ViewResolverRegistry registry) {
            InternalResourceViewResolver resolver = new InternalResourceViewResolver();
            resolver.setPrefix("/WEB-INF/jsp/");
            resolver.setSuffix(".jsp");
            resolver.setViewClass(JstlView.class);
            resolver.setExposeContextBeansAsAttributes(true);
            resolver.setExposePathVariables(true);
            registry.viewResolver(resolver);
        }

        /**
         * Alternative Bean-based configuration
         */
        @Bean
        public InternalResourceViewResolver jspViewResolver() {
            InternalResourceViewResolver resolver = new InternalResourceViewResolver();
            resolver.setPrefix("/WEB-INF/jsp/");
            resolver.setSuffix(".jsp");
            resolver.setViewClass(JstlView.class);
            resolver.setOrder(2); // Lower priority than other view resolvers
            return resolver;
        }
    }

    /**
     * Controller demonstrating JSP usage
     */
    @Controller
    public static class JSPController {

        @GetMapping("/jsp")
        public String index(Model model) {
            model.addAttribute("title", "JSP Example");
            model.addAttribute("message", "Welcome to JSP!");
            model.addAttribute("currentTime", System.currentTimeMillis());
            return "index";
        }

        @GetMapping("/jsp/users")
        public String users(Model model) {
            List<User> users = Arrays.asList(
                new User(1L, "John Doe", "john@example.com", "ADMIN"),
                new User(2L, "Jane Smith", "jane@example.com", "USER"),
                new User(3L, "Bob Johnson", "bob@example.com", "USER")
            );
            model.addAttribute("users", users);
            model.addAttribute("pageTitle", "User Management");
            return "users";
        }

        @GetMapping("/jsp/form")
        public String showForm(Model model) {
            model.addAttribute("user", new User());
            model.addAttribute("roles", Arrays.asList("ADMIN", "USER", "GUEST"));
            return "form";
        }

        @GetMapping("/jsp/el-demo")
        public String elDemo(Model model, HttpServletRequest request) {
            model.addAttribute("name", "Spring Framework");
            model.addAttribute("version", "6.0");
            model.addAttribute("items", Arrays.asList("Item1", "Item2", "Item3"));
            
            request.setAttribute("requestAttr", "Request Scope Value");
            request.getSession().setAttribute("sessionAttr", "Session Scope Value");
            
            return "el-demo";
        }
    }

    /**
     * User model
     */
    public static class User {
        private Long id;
        private String name;
        private String email;
        private String role;

        public User() {}

        public User(Long id, String name, String email, String role) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.role = role;
        }

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }
}

/*
 * Example JSP Template (WEB-INF/jsp/index.jsp):
 * 
 * <%@ page contentType="text/html;charset=UTF-8" language="java" %>
 * <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
 * <%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
 * <!DOCTYPE html>
 * <html>
 * <head>
 *     <title>${title}</title>
 * </head>
 * <body>
 *     <h1>${message}</h1>
 *     <p>Current time: <fmt:formatDate value="${currentTime}" pattern="yyyy-MM-dd HH:mm:ss"/></p>
 * </body>
 * </html>
 * 
 * 
 * Example with JSTL (WEB-INF/jsp/users.jsp):
 * 
 * <%@ page contentType="text/html;charset=UTF-8" language="java" %>
 * <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
 * <!DOCTYPE html>
 * <html>
 * <head>
 *     <title>${pageTitle}</title>
 * </head>
 * <body>
 *     <h1>Users</h1>
 *     <table border="1">
 *         <thead>
 *             <tr>
 *                 <th>ID</th>
 *                 <th>Name</th>
 *                 <th>Email</th>
 *                 <th>Role</th>
 *             </tr>
 *         </thead>
 *         <tbody>
 *             <c:forEach var="user" items="${users}">
 *                 <tr>
 *                     <td>${user.id}</td>
 *                     <td>${user.name}</td>
 *                     <td>${user.email}</td>
 *                     <td>
 *                         <c:choose>
 *                             <c:when test="${user.role == 'ADMIN'}">
 *                                 <span style="color: red;">${user.role}</span>
 *                             </c:when>
 *                             <c:otherwise>
 *                                 ${user.role}
 *                             </c:otherwise>
 *                         </c:choose>
 *                     </td>
 *                 </tr>
 *             </c:forEach>
 *         </tbody>
 *     </table>
 * </body>
 * </html>
 * 
 * 
 * Example with Spring Form Tags (WEB-INF/jsp/form.jsp):
 * 
 * <%@ page contentType="text/html;charset=UTF-8" language="java" %>
 * <%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
 * <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
 * <!DOCTYPE html>
 * <html>
 * <head>
 *     <title>User Form</title>
 * </head>
 * <body>
 *     <h1>User Form</h1>
 *     <form:form method="post" modelAttribute="user" action="/jsp/form/save">
 *         <div>
 *             <form:label path="name">Name:</form:label>
 *             <form:input path="name" />
 *             <form:errors path="name" cssClass="error"/>
 *         </div>
 *         <div>
 *             <form:label path="email">Email:</form:label>
 *             <form:input path="email" type="email"/>
 *             <form:errors path="email" cssClass="error"/>
 *         </div>
 *         <div>
 *             <form:label path="role">Role:</form:label>
 *             <form:select path="role">
 *                 <form:option value="">Select Role</form:option>
 *                 <form:options items="${roles}"/>
 *             </form:select>
 *         </div>
 *         <div>
 *             <button type="submit">Submit</button>
 *         </div>
 *     </form:form>
 * </body>
 * </html>
 * 
 * 
 * JSP Expression Language (EL):
 * 
 * ${expression}         - Evaluate expression
 * ${object.property}    - Access property
 * ${object['property']} - Alternative property access
 * ${array[0]}          - Array access
 * ${list[0]}           - List access
 * ${map.key}           - Map access
 * ${empty collection}  - Check if empty
 * ${condition ? a : b} - Ternary operator
 * ${a + b}             - Arithmetic
 * ${a == b}            - Comparison
 * ${a && b}            - Logical AND
 * ${a || b}            - Logical OR
 * 
 * 
 * JSTL Core Tags:
 * 
 * <c:out>              - Output with escaping
 * <c:set>              - Set variable
 * <c:remove>           - Remove variable
 * <c:if>               - Conditional
 * <c:choose>           - Switch statement
 * <c:when>             - Case in switch
 * <c:otherwise>        - Default case
 * <c:forEach>          - Iteration
 * <c:forTokens>        - Token iteration
 * <c:import>           - Import content
 * <c:url>              - Generate URL
 * <c:redirect>         - Redirect
 * <c:param>            - Set parameter
 * 
 * 
 * Spring Form Tags:
 * 
 * <form:form>          - Form wrapper
 * <form:input>         - Text input
 * <form:password>      - Password input
 * <form:hidden>        - Hidden input
 * <form:textarea>      - Textarea
 * <form:checkbox>      - Checkbox
 * <form:checkboxes>    - Multiple checkboxes
 * <form:radiobutton>   - Radio button
 * <form:radiobuttons>  - Multiple radio buttons
 * <form:select>        - Select dropdown
 * <form:option>        - Single option
 * <form:options>       - Multiple options
 * <form:errors>        - Validation errors
 * <form:label>         - Label
 * 
 * 
 * application.properties for JSP:
 * spring.mvc.view.prefix=/WEB-INF/jsp/
 * spring.mvc.view.suffix=.jsp
 */
