package com.example.templateengine;

import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Freemarker Integration Pattern
 * 
 * Demonstrates integration of Apache FreeMarker template engine with Spring Boot.
 * FreeMarker is a Java-based template engine focusing on generating text output.
 * 
 * Features:
 * - Powerful template language
 * - Built-in directives and functions
 * - Macro support
 * - Custom directives
 * - Good performance
 * - Template inheritance
 */
@SpringBootApplication
public class FreemarkerIntegrationPattern {

    public static void main(String[] args) {
        SpringApplication.run(FreemarkerIntegrationPattern.class, args);
    }

    /**
     * FreeMarker Configuration
     */
    @Bean
    public FreeMarkerConfigurer freeMarkerConfigurer() {
        FreeMarkerConfigurer configurer = new FreeMarkerConfigurer();
        configurer.setTemplateLoaderPath("classpath:/templates/");
        configurer.setDefaultEncoding("UTF-8");
        
        // Additional configuration
        Map<String, Object> variables = new HashMap<>();
        variables.put("xml_escape", "xml");
        configurer.setFreemarkerVariables(variables);
        
        return configurer;
    }

    /**
     * FreeMarker View Resolver
     */
    @Bean
    public FreeMarkerViewResolver freeMarkerViewResolver() {
        FreeMarkerViewResolver resolver = new FreeMarkerViewResolver();
        resolver.setCache(true);
        resolver.setPrefix("");
        resolver.setSuffix(".ftl");
        resolver.setContentType("text/html; charset=UTF-8");
        resolver.setExposeSpringMacroHelpers(true);
        resolver.setExposeRequestAttributes(true);
        resolver.setExposeSessionAttributes(true);
        return resolver;
    }

    /**
     * Advanced FreeMarker Configuration
     */
    @Bean
    public Configuration freemarkerConfiguration() {
        Configuration config = new Configuration(Configuration.VERSION_2_3_31);
        config.setDefaultEncoding("UTF-8");
        config.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        config.setLogTemplateExceptions(false);
        config.setWrapUncheckedExceptions(true);
        config.setFallbackOnNullLoopVariable(false);
        return config;
    }

    /**
     * Controller demonstrating FreeMarker usage
     */
    @Controller
    public static class FreemarkerController {

        @GetMapping("/freemarker")
        public String index(Model model) {
            model.addAttribute("title", "FreeMarker Example");
            model.addAttribute("message", "Welcome to FreeMarker!");
            return "freemarker/index";
        }

        @GetMapping("/freemarker/data")
        public String dataExample(Model model) {
            List<Person> people = Arrays.asList(
                new Person("John", "Doe", 30, "john@example.com"),
                new Person("Jane", "Smith", 25, "jane@example.com"),
                new Person("Bob", "Johnson", 35, "bob@example.com")
            );
            model.addAttribute("people", people);
            model.addAttribute("company", "Acme Corp");
            return "freemarker/data";
        }

        @GetMapping("/freemarker/directives")
        public String directivesExample(Model model) {
            model.addAttribute("items", Arrays.asList("Item 1", "Item 2", "Item 3"));
            model.addAttribute("showDetails", true);
            model.addAttribute("count", 5);
            return "freemarker/directives";
        }

        @GetMapping("/freemarker/macros")
        public String macrosExample(Model model) {
            model.addAttribute("users", Arrays.asList(
                new User("Alice", "ADMIN"),
                new User("Bob", "USER"),
                new User("Charlie", "GUEST")
            ));
            return "freemarker/macros";
        }
    }

    /**
     * Person model
     */
    public static class Person {
        private String firstName;
        private String lastName;
        private int age;
        private String email;

        public Person(String firstName, String lastName, int age, String email) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.age = age;
            this.email = email;
        }

        // Getters
        public String getFirstName() { return firstName; }
        public String getLastName() { return lastName; }
        public int getAge() { return age; }
        public String getEmail() { return email; }
        public String getFullName() { return firstName + " " + lastName; }
    }

    /**
     * User model
     */
    public static class User {
        private String name;
        private String role;

        public User(String name, String role) {
            this.name = name;
            this.role = role;
        }

        public String getName() { return name; }
        public String getRole() { return role; }
    }
}

/*
 * Example FreeMarker Template (templates/freemarker/index.ftl):
 * 
 * <!DOCTYPE html>
 * <html>
 * <head>
 *     <title>${title}</title>
 * </head>
 * <body>
 *     <h1>${message}</h1>
 *     <p>This is a FreeMarker template example.</p>
 * </body>
 * </html>
 * 
 * 
 * Example with Directives (templates/freemarker/directives.ftl):
 * 
 * <!DOCTYPE html>
 * <html>
 * <head>
 *     <title>FreeMarker Directives</title>
 * </head>
 * <body>
 *     <#-- List directive -->
 *     <h2>Items:</h2>
 *     <ul>
 *         <#list items as item>
 *             <li>${item}</li>
 *         </#list>
 *     </ul>
 * 
 *     <#-- If directive -->
 *     <#if showDetails>
 *         <p>Details are shown</p>
 *     <#else>
 *         <p>Details are hidden</p>
 *     </#if>
 * 
 *     <#-- Switch directive -->
 *     <#switch count>
 *         <#case 1>
 *             <p>One item</p>
 *             <#break>
 *         <#case 5>
 *             <p>Five items</p>
 *             <#break>
 *         <#default>
 *             <p>Other count</p>
 *     </#switch>
 * </body>
 * </html>
 * 
 * 
 * Example with Macros (templates/freemarker/macros.ftl):
 * 
 * <#-- Define macro -->
 * <#macro userBadge user>
 *     <div class="badge badge-${user.role?lower_case}">
 *         ${user.name} - ${user.role}
 *     </div>
 * </#macro>
 * 
 * <!DOCTYPE html>
 * <html>
 * <body>
 *     <h1>User Badges</h1>
 *     <#list users as user>
 *         <@userBadge user=user />
 *     </#list>
 * </body>
 * </html>
 * 
 * 
 * Common FreeMarker Directives:
 * 
 * ${...}            - Expression interpolation
 * <#list>           - Iteration
 * <#if>             - Conditional
 * <#switch>         - Switch statement
 * <#macro>          - Define macro
 * <@macro>          - Call macro
 * <#assign>         - Variable assignment
 * <#include>        - Include template
 * <#import>         - Import template library
 * ?default          - Default value
 * ?upper_case       - Convert to uppercase
 * ?lower_case       - Convert to lowercase
 * ?cap_first        - Capitalize first letter
 * ?size             - Get size
 * ?date             - Format as date
 * ?number           - Format as number
 */
