package com.example.templateengine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.view.MustacheViewResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.BeanNameViewResolver;
import org.springframework.web.servlet.view.ContentNegotiatingViewResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * View Resolver Chain Pattern
 * 
 * Demonstrates configuring multiple view resolvers in a chain.
 * Spring tries each ViewResolver in order until one returns a view.
 * 
 * Features:
 * - Multiple template engines
 * - Content negotiation
 * - Order-based resolution
 * - Fallback mechanisms
 * - Custom view resolvers
 * 
 * Common View Resolver Chain Order:
 * 1. BeanNameViewResolver (highest priority)
 * 2. ContentNegotiatingViewResolver
 * 3. ThymeleafViewResolver
 * 4. FreeMarkerViewResolver
 * 5. InternalResourceViewResolver (lowest priority, JSP)
 */
@SpringBootApplication
public class ViewResolverChainPattern {

    public static void main(String[] args) {
        SpringApplication.run(ViewResolverChainPattern.class, args);
    }

    /**
     * Configuration using ViewResolverRegistry
     */
    @Configuration
    @EnableWebMvc
    public static class ViewResolverChainConfig implements WebMvcConfigurer {

        @Override
        public void configureViewResolvers(ViewResolverRegistry registry) {
            // Order 1: Thymeleaf for HTML templates
            ThymeleafViewResolver thymeleafResolver = new ThymeleafViewResolver();
            thymeleafResolver.setTemplateEngine(templateEngine());
            thymeleafResolver.setOrder(1);
            thymeleafResolver.setViewNames(new String[] {"thymeleaf/*"});
            registry.viewResolver(thymeleafResolver);

            // Order 2: Mustache for simple templates
            MustacheViewResolver mustacheResolver = new MustacheViewResolver();
            mustacheResolver.setPrefix("classpath:/templates/");
            mustacheResolver.setSuffix(".mustache");
            mustacheResolver.setOrder(2);
            mustacheResolver.setViewNames(new String[] {"mustache/*"});
            registry.viewResolver(mustacheResolver);

            // Order 3: JSP resolver (fallback)
            InternalResourceViewResolver jspResolver = new InternalResourceViewResolver();
            jspResolver.setPrefix("/WEB-INF/jsp/");
            jspResolver.setSuffix(".jsp");
            jspResolver.setOrder(3);
            registry.viewResolver(jspResolver);
        }

        @Bean
        public SpringTemplateEngine templateEngine() {
            return new SpringTemplateEngine();
        }
    }

    /**
     * Bean-based ViewResolver Configuration
     */
    @Configuration
    public static class BeanBasedViewResolverConfig {

        /**
         * BeanNameViewResolver - resolves view names as Spring beans
         * Highest priority (Order 0)
         */
        @Bean
        public BeanNameViewResolver beanNameViewResolver() {
            BeanNameViewResolver resolver = new BeanNameViewResolver();
            resolver.setOrder(Ordered.HIGHEST_PRECEDENCE);
            return resolver;
        }

        /**
         * ContentNegotiatingViewResolver - negotiates based on content type
         * Uses Accept header or file extension
         */
        @Bean
        public ContentNegotiatingViewResolver contentNegotiatingViewResolver() {
            ContentNegotiatingViewResolver resolver = new ContentNegotiatingViewResolver();
            resolver.setOrder(1);
            
            List<ViewResolver> viewResolvers = new ArrayList<>();
            viewResolvers.add(thymeleafViewResolver());
            viewResolvers.add(jspViewResolver());
            resolver.setViewResolvers(viewResolvers);
            
            // Default views for content negotiation
            List<org.springframework.web.servlet.View> defaultViews = new ArrayList<>();
            defaultViews.add(jsonView());
            resolver.setDefaultViews(defaultViews);
            
            return resolver;
        }

        /**
         * Thymeleaf View Resolver
         */
        @Bean
        public ThymeleafViewResolver thymeleafViewResolver() {
            ThymeleafViewResolver resolver = new ThymeleafViewResolver();
            resolver.setTemplateEngine(thymeleafTemplateEngine());
            resolver.setCharacterEncoding("UTF-8");
            resolver.setOrder(2);
            resolver.setViewNames(new String[] {"*.html", "thymeleaf/*"});
            resolver.setExcludedViewNames(new String[] {"jsp/*"});
            return resolver;
        }

        @Bean
        public SpringTemplateEngine thymeleafTemplateEngine() {
            return new SpringTemplateEngine();
        }

        /**
         * JSP View Resolver (fallback)
         */
        @Bean
        public InternalResourceViewResolver jspViewResolver() {
            InternalResourceViewResolver resolver = new InternalResourceViewResolver();
            resolver.setPrefix("/WEB-INF/jsp/");
            resolver.setSuffix(".jsp");
            resolver.setOrder(Ordered.LOWEST_PRECEDENCE);
            return resolver;
        }

        /**
         * JSON View for content negotiation
         */
        @Bean(name = "jsonView")
        public MappingJackson2JsonView jsonView() {
            MappingJackson2JsonView view = new MappingJackson2JsonView();
            view.setPrettyPrint(true);
            return view;
        }
    }

    /**
     * Custom View Resolver
     */
    public static class CustomViewResolver implements ViewResolver {
        
        private int order = Ordered.LOWEST_PRECEDENCE;

        @Override
        public org.springframework.web.servlet.View resolveViewName(
                String viewName, java.util.Locale locale) throws Exception {
            
            if (viewName.startsWith("custom:")) {
                // Return custom view
                return new CustomView();
            }
            return null; // Let other resolvers handle it
        }

        public void setOrder(int order) {
            this.order = order;
        }
    }

    /**
     * Custom View Implementation
     */
    public static class CustomView implements org.springframework.web.servlet.View {

        @Override
        public String getContentType() {
            return "text/html;charset=UTF-8";
        }

        @Override
        public void render(java.util.Map<String, ?> model,
                          javax.servlet.http.HttpServletRequest request,
                          javax.servlet.http.HttpServletResponse response) 
                throws Exception {
            response.getWriter().write("<html><body><h1>Custom View</h1></body></html>");
        }
    }

    /**
     * Controller demonstrating view resolver chain
     */
    @Controller
    public static class ViewResolverController {

        @GetMapping("/thymeleaf/demo")
        public String thymeleafView(Model model) {
            model.addAttribute("message", "Resolved by Thymeleaf");
            return "thymeleaf/demo"; // Resolved by ThymeleafViewResolver
        }

        @GetMapping("/mustache/demo")
        public String mustacheView(Model model) {
            model.addAttribute("message", "Resolved by Mustache");
            return "mustache/demo"; // Resolved by MustacheViewResolver
        }

        @GetMapping("/jsp/demo")
        public String jspView(Model model) {
            model.addAttribute("message", "Resolved by JSP");
            return "jsp/demo"; // Resolved by InternalResourceViewResolver
        }

        @GetMapping("/jsonView")
        public String jsonView(Model model) {
            model.addAttribute("name", "John Doe");
            model.addAttribute("email", "john@example.com");
            return "jsonView"; // Resolved by BeanNameViewResolver
        }

        @GetMapping("/content-negotiation")
        public String contentNegotiation(Model model) {
            model.addAttribute("data", Arrays.asList("Item1", "Item2", "Item3"));
            // Returns JSON if Accept: application/json
            // Returns HTML if Accept: text/html
            return "data";
        }
    }
}

/*
 * View Resolver Chain Flow:
 * 
 * 1. Request comes in with view name "thymeleaf/demo"
 * 2. DispatcherServlet asks each ViewResolver in order
 * 3. BeanNameViewResolver (Order 0) - checks if bean exists, returns null
 * 4. ContentNegotiatingViewResolver (Order 1) - negotiates based on Accept header
 * 5. ThymeleafViewResolver (Order 2) - matches "thymeleaf/*", returns view
 * 6. If no match, continues to next resolver
 * 7. InternalResourceViewResolver (lowest order) - fallback, always returns view
 * 
 * 
 * View Resolver Types:
 * 
 * BeanNameViewResolver
 * - Resolves view names as Spring bean names
 * - Useful for programmatic views (JSON, PDF, Excel)
 * - Example: return "jsonView" -> looks for @Bean(name="jsonView")
 * 
 * ContentNegotiatingViewResolver
 * - Negotiates based on Accept header or file extension
 * - Delegates to other resolvers
 * - Returns appropriate view based on requested media type
 * 
 * ThymeleafViewResolver
 * - Resolves Thymeleaf templates
 * - Prefix + view name + suffix = template path
 * - Supports view name patterns
 * 
 * FreeMarkerViewResolver
 * - Resolves FreeMarker templates
 * - Similar to ThymeleafViewResolver
 * 
 * InternalResourceViewResolver
 * - Resolves JSP and servlet resources
 * - Should be lowest priority
 * - Always returns a view (no null check)
 * 
 * UrlBasedViewResolver
 * - Base class for URL-based resolvers
 * - Maps view names to URLs
 * 
 * 
 * View Resolver Properties:
 * 
 * order                  - Priority (lower = higher priority)
 * viewNames              - Patterns to match (wildcards allowed)
 * excludedViewNames      - Patterns to exclude
 * prefix                 - Path prefix for templates
 * suffix                 - File extension for templates
 * viewClass              - View class to instantiate
 * cache                  - Whether to cache resolved views
 * 
 * 
 * Content Negotiation Strategies:
 * 
 * 1. Accept Header (default)
 *    - Request: Accept: application/json
 *    - Returns: JSON view
 * 
 * 2. File Extension
 *    - Request: /data.json
 *    - Returns: JSON view
 * 
 * 3. Query Parameter
 *    - Request: /data?format=json
 *    - Returns: JSON view
 * 
 * 
 * application.properties:
 * 
 * # Content negotiation
 * spring.mvc.contentnegotiation.favor-parameter=true
 * spring.mvc.contentnegotiation.parameter-name=format
 * spring.mvc.contentnegotiation.favor-path-extension=false
 * spring.mvc.contentnegotiation.media-types.json=application/json
 * spring.mvc.contentnegotiation.media-types.xml=application/xml
 * 
 * # View resolver order
 * spring.thymeleaf.view-names=thymeleaf/*
 * spring.thymeleaf.order=1
 * 
 * # JSP
 * spring.mvc.view.prefix=/WEB-INF/jsp/
 * spring.mvc.view.suffix=.jsp
 */
