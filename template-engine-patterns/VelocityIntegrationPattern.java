package com.example.templateengine;

import org.apache.velocity.app.VelocityEngine;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.view.velocity.VelocityConfigurer;
import org.springframework.web.servlet.view.velocity.VelocityViewResolver;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Velocity Integration Pattern
 * 
 * Demonstrates integration of Apache Velocity template engine with Spring.
 * Note: Velocity is deprecated and no longer actively maintained.
 * Consider using Thymeleaf or FreeMarker for new projects.
 * 
 * Features:
 * - Simple template language (VTL - Velocity Template Language)
 * - Reference variables ($variable)
 * - Directives (#if, #foreach, #set)
 * - Macro support
 * - Template inheritance
 * 
 * Note: Spring Framework removed built-in Velocity support in Spring 5.0+
 * This example shows the legacy pattern for reference.
 */
@SpringBootApplication
public class VelocityIntegrationPattern {

    public static void main(String[] args) {
        SpringApplication.run(VelocityIntegrationPattern.class, args);
    }

    /**
     * Velocity Engine Configuration
     * Note: This is a legacy pattern
     */
    @Bean
    public VelocityEngine velocityEngine() {
        Properties props = new Properties();
        props.setProperty("resource.loader", "class");
        props.setProperty("class.resource.loader.class", 
            "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        props.setProperty("input.encoding", "UTF-8");
        props.setProperty("output.encoding", "UTF-8");
        
        VelocityEngine engine = new VelocityEngine();
        engine.init(props);
        return engine;
    }

    /**
     * Alternative: Manual Velocity Configuration for legacy Spring versions
     */
    public static class LegacyVelocityConfig {
        
        // @Bean
        // public VelocityConfigurer velocityConfigurer() {
        //     VelocityConfigurer configurer = new VelocityConfigurer();
        //     configurer.setResourceLoaderPath("classpath:/templates/");
        //     
        //     Properties props = new Properties();
        //     props.setProperty("input.encoding", "UTF-8");
        //     props.setProperty("output.encoding", "UTF-8");
        //     configurer.setVelocityProperties(props);
        //     
        //     return configurer;
        // }
        
        // @Bean
        // public VelocityViewResolver velocityViewResolver() {
        //     VelocityViewResolver resolver = new VelocityViewResolver();
        //     resolver.setCache(true);
        //     resolver.setPrefix("");
        //     resolver.setSuffix(".vm");
        //     resolver.setContentType("text/html; charset=UTF-8");
        //     resolver.setExposeSpringMacroHelpers(true);
        //     resolver.setExposeRequestAttributes(true);
        //     resolver.setExposeSessionAttributes(true);
        //     return resolver;
        // }
    }

    /**
     * Controller demonstrating Velocity usage (legacy)
     */
    @Controller
    public static class VelocityController {

        @GetMapping("/velocity")
        public String index(Model model) {
            model.addAttribute("title", "Velocity Example");
            model.addAttribute("message", "Welcome to Velocity!");
            return "velocity/index";
        }

        @GetMapping("/velocity/list")
        public String listExample(Model model) {
            List<Item> items = Arrays.asList(
                new Item(1, "Item One", 10.99),
                new Item(2, "Item Two", 20.99),
                new Item(3, "Item Three", 30.99)
            );
            model.addAttribute("items", items);
            model.addAttribute("total", items.stream()
                .mapToDouble(Item::getPrice).sum());
            return "velocity/list";
        }

        @GetMapping("/velocity/conditions")
        public String conditionsExample(Model model) {
            model.addAttribute("loggedIn", true);
            model.addAttribute("username", "john_doe");
            model.addAttribute("role", "ADMIN");
            model.addAttribute("itemCount", 5);
            return "velocity/conditions";
        }
    }

    /**
     * Item model
     */
    public static class Item {
        private int id;
        private String name;
        private double price;

        public Item(int id, String name, double price) {
            this.id = id;
            this.name = name;
            this.price = price;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public double getPrice() { return price; }
    }
}

/*
 * Example Velocity Template (templates/velocity/index.vm):
 * 
 * <!DOCTYPE html>
 * <html>
 * <head>
 *     <title>$title</title>
 * </head>
 * <body>
 *     <h1>$message</h1>
 *     <p>This is a Velocity template example.</p>
 * </body>
 * </html>
 * 
 * 
 * Example with Directives (templates/velocity/list.vm):
 * 
 * <!DOCTYPE html>
 * <html>
 * <head>
 *     <title>Item List</title>
 * </head>
 * <body>
 *     <h1>Items</h1>
 *     <table>
 *         <thead>
 *             <tr>
 *                 <th>ID</th>
 *                 <th>Name</th>
 *                 <th>Price</th>
 *             </tr>
 *         </thead>
 *         <tbody>
 *             #foreach($item in $items)
 *             <tr>
 *                 <td>$item.id</td>
 *                 <td>$item.name</td>
 *                 <td>$$item.price</td>
 *             </tr>
 *             #end
 *         </tbody>
 *     </table>
 *     <p>Total: $$total</p>
 * </body>
 * </html>
 * 
 * 
 * Example with Conditions (templates/velocity/conditions.vm):
 * 
 * <!DOCTYPE html>
 * <html>
 * <body>
 *     #if($loggedIn)
 *         <p>Welcome, $username!</p>
 *         
 *         #if($role == "ADMIN")
 *             <p>You have admin privileges</p>
 *         #elseif($role == "USER")
 *             <p>You are a regular user</p>
 *         #else
 *             <p>You are a guest</p>
 *         #end
 *     #else
 *         <p>Please log in</p>
 *     #end
 * 
 *     #set($double = $itemCount * 2)
 *     <p>Item count: $itemCount, Double: $double</p>
 * </body>
 * </html>
 * 
 * 
 * Common Velocity Directives:
 * 
 * $variable          - Reference variable
 * ${variable}        - Formal reference notation
 * $!variable         - Quiet reference (no error if null)
 * #set($var = value) - Set variable
 * #if($condition)    - Conditional
 * #elseif            - Else if
 * #else              - Else
 * #end               - End block
 * #foreach($item in $list) - Loop
 * #break             - Break loop
 * #include           - Include template
 * #parse             - Parse template
 * #macro(name)       - Define macro
 * #name()            - Call macro
 * ##                 - Comment
 * #*...*#            - Multi-line comment
 * 
 * Built-in Tools:
 * $date              - Date formatting
 * $number            - Number formatting
 * $math              - Math operations
 * $esc               - Escaping
 * 
 * Migration Note:
 * Since Velocity is deprecated, consider migrating to:
 * - Thymeleaf: Modern, natural templates, strong Spring integration
 * - FreeMarker: Powerful, flexible, good performance
 */
