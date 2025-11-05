# Spring Web MVC Patterns

I'll create a comprehensive Spring Boot application demonstrating all 15 Web MVC patterns.

## Project Structure

```
spring-web-mvc-patterns/
├── src/main/java/org/example/
│   ├── WebMvcPatternsApplication.java
│   ├── config/
│   ├── controller/
│   ├── model/
│   ├── service/
│   └── patterns/webmvc/
│       ├── frontcontroller/
│       ├── dispatcher/
│       ├── handlermapping/
│       ├── handleradapter/
│       ├── viewresolver/
│       ├── interceptor/
│       ├── exceptionresolver/
│       ├── modelattribute/
│       ├── sessionattributes/
│       ├── flashattributes/
│       ├── redirect/
│       ├── forward/
│       ├── resttemplate/
│       ├── contentnegotiation/
│       └── multipartresolver/
├── src/main/resources/
│   ├── templates/
│   └── static/
├── pom.xml
└── application.properties
```

## 1. Main Application & Configuration

```java
// src/main/java/org/example/WebMvcPatternsApplication.java
package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WebMvcPatternsApplication {
    public static void main(String[] args) {
        SpringApplication.run(WebMvcPatternsApplication.class, args);
    }
}
```

```java
// src/main/java/org/example/config/WebMvcConfig.java
package org.example.config;

import org.example.patterns.webmvc.interceptor.LoggingInterceptor;
import org.example.patterns.webmvc.interceptor.PerformanceInterceptor;
import org.example.patterns.webmvc.interceptor.SecurityInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import java.util.Locale;

@Configuration
@EnableWebMvc
public class WebMvcConfig implements WebMvcConfigurer {
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Register custom interceptors
        registry.addInterceptor(new LoggingInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/static/**");
        
        registry.addInterceptor(new PerformanceInterceptor())
                .addPathPatterns("/api/**");
        
        registry.addInterceptor(new SecurityInterceptor())
                .addPathPatterns("/secure/**");
        
        registry.addInterceptor(localeChangeInterceptor());
    }
    
    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer
                .favorParameter(true)
                .parameterName("format")
                .ignoreAcceptHeader(false)
                .defaultContentType(MediaType.APPLICATION_JSON)
                .mediaType("json", MediaType.APPLICATION_JSON)
                .mediaType("xml", MediaType.APPLICATION_XML)
                .mediaType("html", MediaType.TEXT_HTML);
    }
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
    }
    
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("index");
        registry.addViewController("/login").setViewName("login");
    }
    
    @Bean
    public InternalResourceViewResolver jspViewResolver() {
        InternalResourceViewResolver resolver = new InternalResourceViewResolver();
        resolver.setPrefix("/WEB-INF/views/");
        resolver.setSuffix(".jsp");
        resolver.setOrder(2);
        return resolver;
    }
    
    @Bean
    public MappingJackson2JsonView jsonView() {
        return new MappingJackson2JsonView();
    }
    
    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver resolver = new SessionLocaleResolver();
        resolver.setDefaultLocale(Locale.US);
        return resolver;
    }
    
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang");
        return interceptor;
    }
    
    @Bean
    public StandardServletMultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }
    
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
```

## 2. Domain Models

```java
// src/main/java/org/example/model/User.java
package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private LocalDateTime createdAt;
    
    public User(String username, String email) {
        this.username = username;
        this.email = email;
        this.createdAt = LocalDateTime.now();
    }
    
    public String getFullName() {
        return firstName + " " + lastName;
    }
}
```

```java
// src/main/java/org/example/model/Product.java
package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private String category;
}
```

```java
// src/main/java/org/example/model/ApiResponse.java
package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "Success", data);
    }
    
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null);
    }
}
```

```java
// src/main/java/org/example/model/FileUploadForm.java
package org.example.model;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class FileUploadForm {
    private String description;
    private MultipartFile file;
}
```

## 3. Pattern 1: Front Controller Pattern

```java
// src/main/java/org/example/patterns/webmvc/frontcontroller/FrontControllerDemo.java
package org.example.patterns.webmvc.frontcontroller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Front Controller Pattern.
 * DispatcherServlet acts as the front controller in Spring MVC.
 * It receives all requests and delegates to appropriate handlers.
 */
@Slf4j
@Controller
@RequestMapping("/front-controller")
public class FrontControllerDemo {
    
    @GetMapping("/demo")
    public String demo(Model model) {
        log.info("DispatcherServlet routed request to FrontControllerDemo");
        
        model.addAttribute("pattern", "Front Controller");
        model.addAttribute("description", 
            "DispatcherServlet acts as a centralized entry point for all web requests");
        model.addAttribute("flow", new String[]{
            "1. Client sends request to DispatcherServlet",
            "2. DispatcherServlet consults HandlerMapping",
            "3. HandlerMapping returns handler",
            "4. DispatcherServlet invokes handler",
            "5. Handler returns ModelAndView",
            "6. DispatcherServlet resolves view",
            "7. View renders response"
        });
        
        return "patterns/front-controller";
    }
    
    @GetMapping("/example1")
    public String example1() {
        log.info("Example 1 handled by Front Controller");
        return "patterns/example";
    }
    
    @GetMapping("/example2")
    public String example2() {
        log.info("Example 2 handled by Front Controller");
        return "patterns/example";
    }
}
```

## 4. Pattern 2: Dispatcher Pattern

```java
// src/main/java/org/example/patterns/webmvc/dispatcher/DispatcherPatternController.java
package org.example.patterns.webmvc.dispatcher;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Dispatcher Pattern.
 * Demonstrates how DispatcherServlet dispatches requests to controllers.
 */
@Slf4j
@Controller
@RequestMapping("/dispatcher")
public class DispatcherPatternController {
    
    @GetMapping("/demo")
    public String demo(Model model) {
        log.info("DispatcherServlet dispatched to demo handler");
        
        model.addAttribute("pattern", "Dispatcher Pattern");
        model.addAttribute("dispatcher", "DispatcherServlet");
        model.addAttribute("info", 
            "Central dispatcher that delegates request processing to various components");
        
        return "patterns/dispatcher";
    }
    
    @GetMapping("/dispatch/{destination}")
    public String dispatchToDestination(@PathVariable String destination, Model model) {
        log.info("Dispatching to destination: {}", destination);
        
        model.addAttribute("destination", destination);
        model.addAttribute("timestamp", System.currentTimeMillis());
        
        return "patterns/dispatched";
    }
}
```

```java
// src/main/java/org/example/patterns/webmvc/dispatcher/CustomDispatcherServlet.java
package org.example.patterns.webmvc.dispatcher;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.DispatcherServlet;

/**
 * Custom DispatcherServlet to demonstrate dispatcher pattern.
 */
@Slf4j
@Component
public class CustomDispatcherServlet extends DispatcherServlet {
    
    @Override
    protected void doDispatch(HttpServletRequest request, 
                             jakarta.servlet.http.HttpServletResponse response) 
            throws Exception {
        
        log.info("=== Custom Dispatcher Processing ===");
        log.info("Request URI: {}", request.getRequestURI());
        log.info("Method: {}", request.getMethod());
        log.info("Content Type: {}", request.getContentType());
        
        // Delegate to parent DispatcherServlet
        super.doDispatch(request, response);
        
        log.info("=== Dispatch Complete ===");
    }
}
```

## 5. Pattern 3: Handler Mapping Pattern

```java
// src/main/java/org/example/patterns/webmvc/handlermapping/HandlerMappingConfig.java
package org.example.patterns.webmvc.handlermapping;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.springframework.web.servlet.mvc.Controller;

import java.util.HashMap;
import java.util.Map;

/**
 * Handler Mapping Pattern.
 * Maps URLs to handler objects (controllers).
 */
@Configuration
public class HandlerMappingConfig {
    
    @Bean
    public SimpleUrlHandlerMapping customHandlerMapping() {
        SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
        
        Map<String, Controller> urlMap = new HashMap<>();
        urlMap.put("/handler-mapping/legacy", legacyController());
        urlMap.put("/handler-mapping/simple", simpleController());
        
        mapping.setUrlMap(urlMap);
        mapping.setOrder(0);
        
        return mapping;
    }
    
    @Bean
    public Controller legacyController() {
        return new LegacyController();
    }
    
    @Bean
    public Controller simpleController() {
        return new SimpleControllerImpl();
    }
}
```

```java
// src/main/java/org/example/patterns/webmvc/handlermapping/LegacyController.java
package org.example.patterns.webmvc.handlermapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

/**
 * Legacy Spring MVC Controller (pre-annotation style).
 */
@Slf4j
public class LegacyController implements Controller {
    
    @Override
    public ModelAndView handleRequest(HttpServletRequest request, 
                                     HttpServletResponse response) {
        log.info("Legacy Controller handling request via HandlerMapping");
        
        ModelAndView mav = new ModelAndView("patterns/handler-mapping");
        mav.addObject("pattern", "Handler Mapping");
        mav.addObject("controller", "LegacyController");
        mav.addObject("mapping", "SimpleUrlHandlerMapping");
        
        return mav;
    }
}
```

```java
// src/main/java/org/example/patterns/webmvc/handlermapping/SimpleControllerImpl.java
package org.example.patterns.webmvc.handlermapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

@Slf4j
public class SimpleControllerImpl implements Controller {
    
    @Override
    public ModelAndView handleRequest(HttpServletRequest request, 
                                     HttpServletResponse response) {
        log.info("Simple Controller via URL mapping");
        
        ModelAndView mav = new ModelAndView("patterns/handler-mapping");
        mav.addObject("pattern", "Handler Mapping");
        mav.addObject("controller", "SimpleController");
        
        return mav;
    }
}
```

```java
// src/main/java/org/example/patterns/webmvc/handlermapping/AnnotationHandlerMappingDemo.java
package org.example.patterns.webmvc.handlermapping;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Modern annotation-based handler mapping.
 */
@Slf4j
@Controller
@RequestMapping("/handler-mapping")
public class AnnotationHandlerMappingDemo {
    
    @GetMapping("/annotation")
    public String annotationBased(Model model) {
        log.info("Annotation-based handler mapping via @RequestMapping");
        
        model.addAttribute("pattern", "Handler Mapping");
        model.addAttribute("type", "Annotation-based");
        model.addAttribute("mapping", "RequestMappingHandlerMapping");
        model.addAttribute("info", 
            "@RequestMapping annotations are scanned and registered automatically");
        
        return "patterns/handler-mapping";
    }
}
```

## 6. Pattern 4: Handler Adapter Pattern

```java
// src/main/java/org/example/patterns/webmvc/handleradapter/CustomHandlerAdapter.java
package org.example.patterns.webmvc.handleradapter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.ModelAndView;

/**
 * Handler Adapter Pattern.
 * Adapts different types of handlers to a common interface.
 */
@Slf4j
@Component
public class CustomHandlerAdapter implements HandlerAdapter {
    
    @Override
    public boolean supports(Object handler) {
        return handler instanceof CustomHandler;
    }
    
    @Override
    public ModelAndView handle(HttpServletRequest request, 
                              HttpServletResponse response, 
                              Object handler) throws Exception {
        
        log.info("CustomHandlerAdapter adapting CustomHandler");
        
        CustomHandler customHandler = (CustomHandler) handler;
        String result = customHandler.execute(request, response);
        
        ModelAndView mav = new ModelAndView("patterns/handler-adapter");
        mav.addObject("pattern", "Handler Adapter");
        mav.addObject("adapter", "CustomHandlerAdapter");
        mav.addObject("result", result);
        
        return mav;
    }
    
    @Override
    public long getLastModified(HttpServletRequest request, Object handler) {
        return -1;
    }
}
```

```java
// src/main/java/org/example/patterns/webmvc/handleradapter/CustomHandler.java
package org.example.patterns.webmvc.handleradapter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Custom handler interface.
 */
public interface CustomHandler {
    String execute(HttpServletRequest request, HttpServletResponse response);
}
```

```java
// src/main/java/org/example/patterns/webmvc/handleradapter/CustomHandlerImpl.java
package org.example.patterns.webmvc.handleradapter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CustomHandlerImpl implements CustomHandler {
    
    @Override
    public String execute(HttpServletRequest request, HttpServletResponse response) {
        log.info("CustomHandler executing custom logic");
        return "Custom handler executed successfully via adapter pattern";
    }
}
```

```java
// src/main/java/org/example/patterns/webmvc/handleradapter/HandlerAdapterDemo.java
package org.example.patterns.webmvc.handleradapter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Demonstrates different handler adapters in Spring MVC.
 */
@Slf4j
@Controller
@RequestMapping("/handler-adapter")
public class HandlerAdapterDemo {
    
    @GetMapping("/demo")
    public String demo(Model model) {
        log.info("RequestMappingHandlerAdapter handling @RequestMapping");
        
        model.addAttribute("pattern", "Handler Adapter");
        model.addAttribute("adapters", new String[]{
            "RequestMappingHandlerAdapter - @RequestMapping methods",
            "HttpRequestHandlerAdapter - HttpRequestHandler interface",
            "SimpleControllerHandlerAdapter - Controller interface",
            "CustomHandlerAdapter - Custom handler types"
        });
        
        return "patterns/handler-adapter";
    }
}
```

## 7. Pattern 5: View Resolver Pattern

```java
// src/main/java/org/example/patterns/webmvc/viewresolver/ViewResolverConfig.java
package org.example.patterns.webmvc.viewresolver;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.view.BeanNameViewResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;
import org.springframework.web.servlet.view.xml.MappingJackson2XmlView;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

/**
 * View Resolver Pattern.
 * Resolves logical view names to actual views.
 */
@Configuration
public class ViewResolverConfig {
    
    @Bean
    public BeanNameViewResolver beanNameViewResolver() {
        BeanNameViewResolver resolver = new BeanNameViewResolver();
        resolver.setOrder(0);
        return resolver;
    }
    
    @Bean
    public ThymeleafViewResolver thymeleafViewResolver() {
        ThymeleafViewResolver resolver = new ThymeleafViewResolver();
        resolver.setTemplateEngine(templateEngine());
        resolver.setOrder(1);
        resolver.setViewNames(new String[]{"patterns/*", "*.html"});
        return resolver;
    }
    
    @Bean
    public SpringTemplateEngine templateEngine() {
        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(templateResolver());
        return engine;
    }
    
    @Bean
    public ClassLoaderTemplateResolver templateResolver() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML");
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(false);
        return resolver;
    }
    
    @Bean(name = "jsonView")
    public MappingJackson2JsonView jsonView() {
        return new MappingJackson2JsonView();
    }
    
    @Bean(name = "xmlView")
    public MappingJackson2XmlView xmlView() {
        return new MappingJackson2XmlView();
    }
}
```

```java
// src/main/java/org/example/patterns/webmvc/viewresolver/ViewResolverController.java
package org.example.patterns.webmvc.viewresolver;

import lombok.extern.slf4j.Slf4j;
import org.example.model.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;

@Slf4j
@Controller
@RequestMapping("/view-resolver")
public class ViewResolverController {
    
    @GetMapping("/demo")
    public String demo(Model model) {
        log.info("View Resolver Pattern demonstration");
        
        model.addAttribute("pattern", "View Resolver");
        model.addAttribute("resolvers", new String[]{
            "BeanNameViewResolver - Resolves view names to bean names",
            "ThymeleafViewResolver - Resolves Thymeleaf templates",
            "InternalResourceViewResolver - Resolves JSP views",
            "ContentNegotiatingViewResolver - Resolves based on content type"
        });
        
        return "patterns/view-resolver";
    }
    
    @GetMapping("/thymeleaf")
    public String thymeleafView(Model model) {
        log.info("Resolving Thymeleaf view");
        
        User user = new User(1L, "john.doe", "john@example.com", 
                "John", "Doe", LocalDateTime.now());
        model.addAttribute("user", user);
        
        return "patterns/thymeleaf-view";
    }
    
    @GetMapping("/json")
    public String jsonView(Model model) {
        log.info("Resolving JSON view via BeanNameViewResolver");
        
        User user = new User(2L, "jane.doe", "jane@example.com", 
                "Jane", "Doe", LocalDateTime.now());
        model.addAttribute("user", user);
        
        return "jsonView"; // Resolves to bean named "jsonView"
    }
    
    @GetMapping("/xml")
    public String xmlView(Model model) {
        log.info("Resolving XML view via BeanNameViewResolver");
        
        User user = new User(3L, "bob.smith", "bob@example.com", 
                "Bob", "Smith", LocalDateTime.now());
        model.addAttribute("user", user);
        
        return "xmlView";
    }
}
```

## 8. Pattern 6: Interceptor Pattern

```java
// src/main/java/org/example/patterns/webmvc/interceptor/LoggingInterceptor.java
package org.example.patterns.webmvc.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * Interceptor Pattern - Logging Interceptor.
 * Intercepts requests for logging purposes.
 */
@Slf4j
public class LoggingInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request, 
                            HttpServletResponse response, 
                            Object handler) {
        log.info("=== LoggingInterceptor: PRE-HANDLE ===");
        log.info("Request URI: {}", request.getRequestURI());
        log.info("Method: {}", request.getMethod());
        log.info("Handler: {}", handler.getClass().getSimpleName());
        
        request.setAttribute("startTime", System.currentTimeMillis());
        return true; // Continue with request processing
    }
    
    @Override
    public void postHandle(HttpServletRequest request, 
                          HttpServletResponse response, 
                          Object handler, 
                          ModelAndView modelAndView) {
        log.info("=== LoggingInterceptor: POST-HANDLE ===");
        if (modelAndView != null) {
            log.info("View Name: {}", modelAndView.getViewName());
            log.info("Model: {}", modelAndView.getModel());
        }
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, 
                               HttpServletResponse response, 
                               Object handler, 
                               Exception ex) {
        log.info("=== LoggingInterceptor: AFTER-COMPLETION ===");
        
        Long startTime = (Long) request.getAttribute("startTime");
        if (startTime != null) {
            long duration = System.currentTimeMillis() - startTime;
            log.info("Request processing time: {} ms", duration);
        }
        
        log.info("Response Status: {}", response.getStatus());
        
        if (ex != null) {
            log.error("Exception occurred: {}", ex.getMessage());
        }
    }
}
```

```java
// src/main/java/org/example/patterns/webmvc/interceptor/PerformanceInterceptor.java
package org.example.patterns.webmvc.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
public class PerformanceInterceptor implements HandlerInterceptor {
    
    private static final long SLOW_REQUEST_THRESHOLD = 1000; // 1 second
    
    @Override
    public boolean preHandle(HttpServletRequest request, 
                            HttpServletResponse response, 
                            Object handler) {
        request.setAttribute("perfStartTime", System.currentTimeMillis());
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, 
                               HttpServletResponse response, 
                               Object handler, 
                               Exception ex) {
        Long startTime = (Long) request.getAttribute("perfStartTime");
        if (startTime != null) {
            long duration = System.currentTimeMillis() - startTime;
            
            if (duration > SLOW_REQUEST_THRESHOLD) {
                log.warn("SLOW REQUEST DETECTED: {} - {} ms", 
                        request.getRequestURI(), duration);
            } else {
                log.info("Performance: {} - {} ms", 
                        request.getRequestURI(), duration);
            }
        }
    }
}
```

```java
// src/main/java/org/example/patterns/webmvc/interceptor/SecurityInterceptor.java
package org.example.patterns.webmvc/interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
public class SecurityInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request, 
                            HttpServletResponse response, 
                            Object handler) throws Exception {
        log.info("SecurityInterceptor: Checking authentication");
        
        HttpSession session = request.getSession(false);
        
        if (session == null || session.getAttribute("user") == null) {
            log.warn("Unauthorized access attempt to: {}", request.getRequestURI());
            response.sendRedirect("/login");
            return false; // Stop request processing
        }
        
        log.info("User authenticated: {}", session.getAttribute("user"));
        return true;
    }
}
```

```java
// src/main/java/org/example/patterns/webmvc/interceptor/InterceptorDemoController.java
package org.example.patterns.webmvc.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping("/interceptor")
public class InterceptorDemoController {
    
    @GetMapping("/demo")
    public String demo(Model model) {
        log.info("InterceptorDemo controller method");
        
```java
        model.addAttribute("pattern", "Interceptor Pattern");
        model.addAttribute("interceptors", new String[]{
            "LoggingInterceptor - Logs all request details",
            "PerformanceInterceptor - Monitors request duration",
            "SecurityInterceptor - Checks authentication",
            "LocaleChangeInterceptor - Handles locale changes"
        });
        model.addAttribute("phases", new String[]{
            "preHandle - Before handler execution",
            "postHandle - After handler, before view rendering",
            "afterCompletion - After complete request processing"
        });
        
        return "patterns/interceptor";
    }
    
    @GetMapping("/test-slow")
    public String testSlowRequest() throws InterruptedException {
        log.info("Simulating slow request");
        Thread.sleep(1500); // Trigger performance warning
        return "patterns/interceptor";
    }
}
```

## 9. Pattern 7: Exception Resolver Pattern

```java
// src/main/java/org/example/patterns/webmvc/exceptionresolver/GlobalExceptionHandler.java
package org.example.patterns.webmvc.exceptionresolver;

import lombok.extern.slf4j.Slf4j;
import org.example.model.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Exception Resolver Pattern.
 * Centralized exception handling using @ControllerAdvice.
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ModelAndView handleResourceNotFound(ResourceNotFoundException ex, 
                                              HttpServletRequest request) {
        log.error("Resource not found: {}", ex.getMessage());
        
        ModelAndView mav = new ModelAndView("error/404");
        mav.addObject("error", "Resource Not Found");
        mav.addObject("message", ex.getMessage());
        mav.addObject("path", request.getRequestURI());
        mav.addObject("status", 404);
        
        return mav;
    }
    
    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiResponse<String>> handleValidationException(
            ValidationException ex) {
        log.error("Validation error: {}", ex.getMessage());
        return ResponseEntity
                .badRequest()
                .body(ApiResponse.error(ex.getMessage()));
    }
    
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<String>> handleBusinessException(
            BusinessException ex) {
        log.error("Business error: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ApiResponse.error(ex.getMessage()));
    }
    
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ModelAndView handleNoHandlerFound(NoHandlerFoundException ex) {
        log.error("No handler found: {}", ex.getRequestURL());
        
        ModelAndView mav = new ModelAndView("error/404");
        mav.addObject("error", "Page Not Found");
        mav.addObject("message", "The requested page does not exist");
        
        return mav;
    }
    
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView handleGenericException(Exception ex, 
                                              HttpServletRequest request) {
        log.error("Unexpected error: ", ex);
        
        ModelAndView mav = new ModelAndView("error/500");
        mav.addObject("error", "Internal Server Error");
        mav.addObject("message", ex.getMessage());
        mav.addObject("path", request.getRequestURI());
        
        return mav;
    }
}
```

```java
// src/main/java/org/example/patterns/webmvc/exceptionresolver/ResourceNotFoundException.java
package org.example.patterns.webmvc.exceptionresolver;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
```

```java
// src/main/java/org/example/patterns/webmvc/exceptionresolver/ValidationException.java
package org.example.patterns.webmvc.exceptionresolver;

public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}
```

```java
// src/main/java/org/example/patterns/webmvc/exceptionresolver/BusinessException.java
package org.example.patterns.webmvc.exceptionresolver;

public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
```

```java
// src/main/java/org/example/patterns/webmvc/exceptionresolver/ExceptionResolverController.java
package org.example.patterns.webmvc.exceptionresolver;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping("/exception-resolver")
public class ExceptionResolverController {
    
    @GetMapping("/demo")
    public String demo(Model model) {
        model.addAttribute("pattern", "Exception Resolver");
        model.addAttribute("info", "Centralized exception handling with @ControllerAdvice");
        model.addAttribute("handlers", new String[]{
            "@ExceptionHandler - Handle specific exceptions",
            "@ResponseStatus - Set HTTP status code",
            "ModelAndView - Return error views",
            "ResponseEntity - Return JSON error responses"
        });
        
        return "patterns/exception-resolver";
    }
    
    @GetMapping("/not-found/{id}")
    public String triggerNotFound(@PathVariable Long id) {
        throw new ResourceNotFoundException("Resource with ID " + id + " not found");
    }
    
    @GetMapping("/validation-error")
    public String triggerValidationError() {
        throw new ValidationException("Invalid input parameters");
    }
    
    @GetMapping("/business-error")
    public String triggerBusinessError() {
        throw new BusinessException("Business rule violation");
    }
    
    @GetMapping("/generic-error")
    public String triggerGenericError() {
        throw new RuntimeException("Unexpected error occurred");
    }
}
```

## 10. Pattern 8: Model Attribute Pattern

```java
// src/main/java/org/example/patterns/webmvc/modelattribute/ModelAttributeController.java
package org.example.patterns.webmvc.modelattribute;

import lombok.extern.slf4j.Slf4j;
import org.example.model.Product;
import org.example.model.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Model Attribute Pattern.
 * Demonstrates @ModelAttribute for binding and sharing data.
 */
@Slf4j
@Controller
@RequestMapping("/model-attribute")
public class ModelAttributeController {
    
    /**
     * @ModelAttribute method - Executed before every handler method.
     * Populates common model attributes.
     */
    @ModelAttribute("categories")
    public List<String> populateCategories() {
        log.info("@ModelAttribute: Populating categories");
        return Arrays.asList("Electronics", "Books", "Clothing", "Food");
    }
    
    @ModelAttribute("timestamp")
    public String populateTimestamp() {
        return LocalDateTime.now().toString();
    }
    
    @GetMapping("/demo")
    public String demo(Model model) {
        model.addAttribute("pattern", "Model Attribute Pattern");
        model.addAttribute("info", 
            "@ModelAttribute populates model data before handler execution");
        
        return "patterns/model-attribute";
    }
    
    /**
     * @ModelAttribute on method parameter - Binds form data to object.
     */
    @GetMapping("/create-user")
    public String showUserForm(Model model) {
        model.addAttribute("user", new User());
        return "patterns/user-form";
    }
    
    @PostMapping("/create-user")
    public String createUser(@ModelAttribute("user") User user, Model model) {
        log.info("@ModelAttribute binding: {}", user);
        
        user.setCreatedAt(LocalDateTime.now());
        model.addAttribute("message", "User created successfully");
        model.addAttribute("user", user);
        
        return "patterns/user-success";
    }
    
    /**
     * @ModelAttribute with custom name.
     */
    @GetMapping("/create-product")
    public String showProductForm(Model model) {
        model.addAttribute("product", new Product());
        return "patterns/product-form";
    }
    
    @PostMapping("/create-product")
    public String createProduct(@ModelAttribute("product") Product product, 
                               Model model) {
        log.info("Product created: {}", product);
        
        model.addAttribute("message", "Product created successfully");
        model.addAttribute("product", product);
        
        return "patterns/product-success";
    }
    
    /**
     * @ModelAttribute method returning model attribute.
     */
    @ModelAttribute("appInfo")
    public AppInfo getAppInfo() {
        return new AppInfo("Spring MVC Patterns", "1.0.0", "Demo Application");
    }
    
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class AppInfo {
        private String name;
        private String version;
        private String description;
    }
}
```

## 11. Pattern 9: Session Attributes Pattern

```java
// src/main/java/org/example/patterns/webmvc/sessionattributes/SessionAttributesController.java
package org.example.patterns.webmvc.sessionattributes;

import lombok.extern.slf4j.Slf4j;
import org.example.model.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Session Attributes Pattern.
 * Stores model attributes in HTTP session across multiple requests.
 */
@Slf4j
@Controller
@RequestMapping("/session-attributes")
@SessionAttributes({"cart", "currentUser"})
public class SessionAttributesController {
    
    @ModelAttribute("cart")
    public ShoppingCart createCart() {
        log.info("Creating new shopping cart in session");
        return new ShoppingCart();
    }
    
    @GetMapping("/demo")
    public String demo(Model model) {
        model.addAttribute("pattern", "Session Attributes Pattern");
        model.addAttribute("info", 
            "@SessionAttributes stores model data in HTTP session");
        
        return "patterns/session-attributes";
    }
    
    @GetMapping("/login")
    public String showLogin() {
        return "patterns/login-form";
    }
    
    @PostMapping("/login")
    public String login(@RequestParam String username, Model model) {
        log.info("User logging in: {}", username);
        
        User user = new User(1L, username, username + "@example.com", 
                username, "User", LocalDateTime.now());
        
        model.addAttribute("currentUser", user);
        model.addAttribute("message", "Login successful");
        
        return "redirect:/session-attributes/dashboard";
    }
    
    @GetMapping("/dashboard")
    public String dashboard(@ModelAttribute("currentUser") User user, Model model) {
        log.info("Dashboard accessed by: {}", user.getUsername());
        
        model.addAttribute("message", "Welcome, " + user.getFullName());
        
        return "patterns/dashboard";
    }
    
    @GetMapping("/add-to-cart")
    public String showAddToCart() {
        return "patterns/add-to-cart";
    }
    
    @PostMapping("/add-to-cart")
    public String addToCart(@ModelAttribute("cart") ShoppingCart cart,
                           @RequestParam String productName,
                           @RequestParam Integer quantity,
                           Model model) {
        log.info("Adding to cart: {} x {}", productName, quantity);
        
        cart.addItem(productName, quantity);
        model.addAttribute("message", "Item added to cart");
        
        return "redirect:/session-attributes/view-cart";
    }
    
    @GetMapping("/view-cart")
    public String viewCart(@ModelAttribute("cart") ShoppingCart cart, Model model) {
        log.info("Viewing cart: {} items", cart.getItemCount());
        
        model.addAttribute("cart", cart);
        
        return "patterns/view-cart";
    }
    
    @GetMapping("/clear-cart")
    public String clearCart(@ModelAttribute("cart") ShoppingCart cart, Model model) {
        log.info("Clearing cart");
        
        cart.clear();
        model.addAttribute("message", "Cart cleared");
        
        return "redirect:/session-attributes/view-cart";
    }
    
    @GetMapping("/logout")
    public String logout(SessionStatus sessionStatus, Model model) {
        log.info("User logging out - clearing session");
        
        sessionStatus.setComplete(); // Clear all session attributes
        model.addAttribute("message", "Logged out successfully");
        
        return "redirect:/session-attributes/demo";
    }
    
    @lombok.Data
    public static class ShoppingCart {
        private final List<CartItem> items = new ArrayList<>();
        
        public void addItem(String productName, Integer quantity) {
            items.add(new CartItem(productName, quantity));
        }
        
        public int getItemCount() {
            return items.size();
        }
        
        public void clear() {
            items.clear();
        }
    }
    
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class CartItem {
        private String productName;
        private Integer quantity;
    }
}
```

## 12. Pattern 10: Flash Attributes Pattern

```java
// src/main/java/org/example/patterns/webmvc/flashattributes/FlashAttributesController.java
package org.example.patterns.webmvc.flashattributes;

import lombok.extern.slf4j.Slf4j;
import org.example.model.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Flash Attributes Pattern.
 * Stores attributes temporarily for use after redirect (PRG pattern).
 */
@Slf4j
@Controller
@RequestMapping("/flash-attributes")
public class FlashAttributesController {
    
    private final Map<Long, User> userDatabase = new HashMap<>();
    private Long userIdCounter = 1L;
    
    @GetMapping("/demo")
    public String demo(Model model) {
        model.addAttribute("pattern", "Flash Attributes Pattern");
        model.addAttribute("info", 
            "RedirectAttributes stores data temporarily for the next request");
        model.addAttribute("use-case", "Post-Redirect-Get (PRG) pattern");
        
        return "patterns/flash-attributes";
    }
    
    @GetMapping("/register")
    public String showRegistrationForm() {
        return "patterns/register-form";
    }
    
    /**
     * Flash attributes survive redirect.
     */
    @PostMapping("/register")
    public String register(@RequestParam String username,
                          @RequestParam String email,
                          RedirectAttributes redirectAttributes) {
        log.info("Registering user: {}", username);
        
        User user = new User(userIdCounter++, username, email, 
                username, "User", LocalDateTime.now());
        userDatabase.put(user.getId(), user);
        
        // Flash attributes - available only for next request
        redirectAttributes.addFlashAttribute("message", 
                "Registration successful!");
        redirectAttributes.addFlashAttribute("user", user);
        redirectAttributes.addFlashAttribute("messageType", "success");
        
        // Regular redirect attributes - added to URL
        redirectAttributes.addAttribute("userId", user.getId());
        
        return "redirect:/flash-attributes/success";
    }
    
    @GetMapping("/success")
    public String registrationSuccess(@RequestParam(required = false) Long userId,
                                     Model model) {
        log.info("Registration success page - userId: {}", userId);
        
        // Flash attributes automatically added to model
        // They are available here but won't be available on page refresh
        
        return "patterns/registration-success";
    }
    
    @PostMapping("/update-profile")
    public String updateProfile(@RequestParam Long userId,
                               @RequestParam String firstName,
                               @RequestParam String lastName,
                               RedirectAttributes redirectAttributes) {
        log.info("Updating profile for user: {}", userId);
        
        User user = userDatabase.get(userId);
        if (user != null) {
            user.setFirstName(firstName);
            user.setLastName(lastName);
            
            redirectAttributes.addFlashAttribute("message", 
                    "Profile updated successfully!");
            redirectAttributes.addFlashAttribute("messageType", "success");
        } else {
            redirectAttributes.addFlashAttribute("message", 
                    "User not found!");
            redirectAttributes.addFlashAttribute("messageType", "error");
        }
        
        return "redirect:/flash-attributes/profile/" + userId;
    }
    
    @GetMapping("/profile/{userId}")
    public String viewProfile(@PathVariable Long userId, Model model) {
        User user = userDatabase.get(userId);
        
        if (user != null) {
            model.addAttribute("user", user);
        }
        
        return "patterns/profile";
    }
    
    @PostMapping("/delete")
    public String deleteUser(@RequestParam Long userId,
                            RedirectAttributes redirectAttributes) {
        log.info("Deleting user: {}", userId);
        
        User removed = userDatabase.remove(userId);
        
        if (removed != null) {
            redirectAttributes.addFlashAttribute("message", 
                    "User deleted: " + removed.getUsername());
            redirectAttributes.addFlashAttribute("messageType", "warning");
        }
        
        return "redirect:/flash-attributes/demo";
    }
}
```

## 13. Pattern 11: Redirect Pattern

```java
// src/main/java/org/example/patterns/webmvc/redirect/RedirectPatternController.java
package org.example.patterns.webmvc.redirect;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

/**
 * Redirect Pattern.
 * Demonstrates different redirect techniques in Spring MVC.
 */
@Slf4j
@Controller
@RequestMapping("/redirect")
public class RedirectPatternController {
    
    @GetMapping("/demo")
    public String demo(Model model) {
        model.addAttribute("pattern", "Redirect Pattern");
        model.addAttribute("types", new String[]{
            "String with 'redirect:' prefix",
            "RedirectView object",
            "Redirect with attributes",
            "Redirect to external URL"
        });
        
        return "patterns/redirect";
    }
    
    /**
     * Simple redirect using "redirect:" prefix.
     */
    @GetMapping("/simple")
    public String simpleRedirect() {
        log.info("Simple redirect");
        return "redirect:/redirect/target";
    }
    
    /**
     * Redirect with path variables.
     */
    @GetMapping("/with-path/{id}")
    public String redirectWithPath(@PathVariable Long id) {
        log.info("Redirect with path variable: {}", id);
        return "redirect:/redirect/target/" + id;
    }
    
    /**
     * Redirect with query parameters.
     */
    @GetMapping("/with-params")
    public String redirectWithParams(RedirectAttributes redirectAttributes) {
        log.info("Redirect with query parameters");
        
        redirectAttributes.addAttribute("param1", "value1");
        redirectAttributes.addAttribute("param2", "value2");
        
        return "redirect:/redirect/target";
    }
    
    /**
     * Redirect using RedirectView.
     */
    @GetMapping("/view-object")
    public RedirectView redirectUsingView(RedirectAttributes redirectAttributes) {
        log.info("Redirect using RedirectView object");
        
        redirectAttributes.addFlashAttribute("message", "Redirected via RedirectView");
        
        RedirectView redirectView = new RedirectView();
        redirectView.setUrl("/redirect/target");
        redirectView.setContextRelative(true);
        
        return redirectView;
    }
    
    /**
     * Redirect to external URL.
     */
    @GetMapping("/external")
    public RedirectView redirectToExternal() {
        log.info("Redirect to external URL");
        
        RedirectView redirectView = new RedirectView();
        redirectView.setUrl("https://spring.io");
        redirectView.setContextRelative(false);
        
        return redirectView;
    }
    
    /**
     * POST-Redirect-GET pattern.
     */
    @PostMapping("/submit-form")
    public String submitForm(@RequestParam String data,
                            RedirectAttributes redirectAttributes) {
        log.info("Form submitted: {}", data);
        
        // Process data
        String processedData = data.toUpperCase();
        
        // Add flash attributes for next request
        redirectAttributes.addFlashAttribute("result", processedData);
        redirectAttributes.addFlashAttribute("message", "Form processed successfully");
        
        // Redirect to prevent duplicate submission
        return "redirect:/redirect/form-result";
    }
    
    @GetMapping("/form-result")
    public String formResult() {
        // Flash attributes automatically available in model
        return "patterns/form-result";
    }
    
    /**
     * Target endpoints for redirects.
     */
    @GetMapping("/target")
    public String redirectTarget(@RequestParam(required = false) String param1,
                                 @RequestParam(required = false) String param2,
                                 Model model) {
        log.info("Redirect target reached - param1: {}, param2: {}", param1, param2);
        
        model.addAttribute("message", "Redirected successfully");
        model.addAttribute("param1", param1);
        model.addAttribute("param2", param2);
        
        return "patterns/redirect-target";
    }
    
    @GetMapping("/target/{id}")
    public String redirectTargetWithPath(@PathVariable Long id, Model model) {
        log.info("Redirect target with path: {}", id);
        
        model.addAttribute("message", "Redirected with ID: " + id);
        model.addAttribute("id", id);
        
        return "patterns/redirect-target";
    }
}
```

## 14. Pattern 12: Forward Pattern

```java
// src/main/java/org/example/patterns/webmvc/forward/ForwardPatternController.java
package org.example.patterns.webmvc.forward;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Forward Pattern.
 * Server-side forwarding (unlike redirect which is client-side).
 */
@Slf4j
@Controller
@RequestMapping("/forward")
public class ForwardPatternController {
    
    @GetMapping("/demo")
    public String demo(Model model) {
        model.addAttribute("pattern", "Forward Pattern");
        model.addAttribute("info", 
            "Server-side forwarding using 'forward:' prefix");
        model.addAttribute("differences", new String[]{
            "Forward: Server-side, same request, URL unchanged",
            "Redirect: Client-side, new request, URL changed"
        });
        
        return "patterns/forward";
    }
    
    /**
     * Simple forward using "forward:" prefix.
     */
    @GetMapping("/simple")
    public String simpleForward(Model model, HttpServletRequest request) {
        log.info("Simple forward from: {}", request.getRequestURI());
        
        model.addAttribute("forwardedFrom", "/forward/simple");
        model.addAttribute("message", "This request was forwarded");
        
        return "forward:/forward/target";
    }
    
    /**
     * Forward with data in model.
     */
    @GetMapping("/with-data")
    public String forwardWithData(Model model, HttpServletRequest request) {
        log.info("Forward with data");
        
        model.addAttribute("data", "Forwarded data");
        model.addAttribute("timestamp", System.currentTimeMillis());
        model.addAttribute("originalUrl", request.getRequestURI());
        
        return "forward:/forward/target";
    }
    
    /**
     * Forward with request attributes.
     */
    @GetMapping("/with-attributes")
    public String forwardWithAttributes(HttpServletRequest request) {
        log.info("Forward with request attributes");
        
        request.setAttribute("customAttribute", "Custom Value");
        request.setAttribute("forwardCount", 1);
        
        return "forward:/forward/target";
    }
    
    /**
     * Conditional forward.
     */
    @GetMapping("/conditional")
    public String conditionalForward(@RequestParam(defaultValue = "false") boolean condition,
                                    Model model) {
        log.info("Conditional forward - condition: {}", condition);
        
        if (condition) {
            model.addAttribute("message", "Condition was true - forwarding");
            return "forward:/forward/target";
        } else {
            model.addAttribute("message", "Condition was false - no forward");
            return "patterns/forward";
        }
    }
    
    /**
     * Chain of forwards.
     */
    @GetMapping("/chain-start")
    public String chainStart(HttpServletRequest request) {
        log.info("Forward chain started");
        
        request.setAttribute("chainStep", "Step 1");
        
        return "forward:/forward/chain-middle";
    }
    
    @GetMapping("/chain-middle")
    public String chainMiddle(HttpServletRequest request) {
        log.info("Forward chain middle");
        
        String previousStep = (String) request.getAttribute("chainStep");
        request.setAttribute("chainStep", previousStep + " -> Step 2");
        
        return "forward:/forward/chain-end";
    }
    
    @GetMapping("/chain-end")
    public String chainEnd(HttpServletRequest request, Model model) {
        log.info("Forward chain ended");
        
        String chainPath = (String) request.getAttribute("chainStep");
        model.addAttribute("chainPath", chainPath + " -> Step 3 (End)");
        
        return "patterns/forward-chain";
    }
    
    /**
     * Target endpoint for forwards.
     */
    @GetMapping("/target")
    public String forwardTarget(HttpServletRequest request, Model model) {
        log.info("Forward target reached");
        
        model.addAttribute("targetReached", true);
        model.addAttribute("requestUri", request.getRequestURI());
        model.addAttribute("forwardUri", request.getAttribute(
                "jakarta.servlet.forward.request_uri"));
        
        // Check for custom attributes
        Object customAttr = request.getAttribute("customAttribute");
        if (customAttr != null) {
            model.addAttribute("customAttribute", customAttr);
        }
        
        return "patterns/forward-target";
    }
}
```

## 15. Pattern 13: REST Template Pattern

```java
// src/main/java/org/example/patterns/webmvc/resttemplate/RestTemplateService.java
package org.example.patterns.webmvc.resttemplate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.model.User;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Template Pattern.
 * Synchronous client for making HTTP requests.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RestTemplateService {
    
    private final RestTemplate restTemplate;
    private static final String BASE_URL = "https://jsonplaceholder.typicode.com";
    
    /**
     * GET request using getForObject.
     */
    public User getUserById(Long id) {
        log.info("GET request for user: {}", id);
        
        String url = BASE_URL + "/users/{id}";
        User user = restTemplate.getForObject(url, User.class, id);
        
        log.info("Retrieved user: {}", user);
        return user;
    }
    
    /**
     * GET request using getForEntity (includes response details).
     */
    public ResponseEntity<User> getUserWithDetails(Long id) {
        log.info("GET request with ResponseEntity for user: {}", id);
        
        String url = BASE_URL + "/users/{id}";
        ResponseEntity<User> response = restTemplate.getForEntity(url, User.class, id);
        
        log.info("Status: {}, Headers: {}", response.getStatusCode(), 
                response.getHeaders());
        
        return response;
    }
    
    /**
     * GET request with query parameters.
     */
    public List<User> getUsersWithParams(Integer limit) {
        log.info("GET request with query params - limit: {}", limit);
        
        String url = UriComponentsBuilder.fromHttpUrl(BASE_URL + "/users")
                .queryParam("_limit", limit)
                .toUriString();
        
        ResponseEntity<List<User>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<User>>() {}
        );
        
        return response.getBody();
    }
    
    /**
     * POST request using postForObject.
     */
    public User createUser(User user) {
        log.info("POST request to create user: {}", user);
        
        String url = BASE_URL + "/users";
        User createdUser = restTemplate.postForObject(url, user, User.class);
        
        log.info("Created user: {}", createdUser);
        return createdUser;
    }
    
    /**
     * POST request using postForEntity.
     */
    public ResponseEntity<User> createUserWithResponse(User user) {
        log.info("POST request with ResponseEntity");
        
```java
        String url = BASE_URL + "/users";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<User> request = new HttpEntity<>(user, headers);
        
        ResponseEntity<User> response = restTemplate.postForEntity(url, request, User.class);
        
        log.info("Status: {}, Location: {}", response.getStatusCode(), 
                response.getHeaders().getLocation());
        
        return response;
    }
    
    /**
     * PUT request using put method.
     */
    public void updateUser(Long id, User user) {
        log.info("PUT request to update user: {}", id);
        
        String url = BASE_URL + "/users/{id}";
        restTemplate.put(url, user, id);
        
        log.info("User updated: {}", id);
    }
    
    /**
     * PUT request using exchange.
     */
    public ResponseEntity<User> updateUserWithResponse(Long id, User user) {
        log.info("PUT request with ResponseEntity");
        
        String url = BASE_URL + "/users/{id}";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<User> request = new HttpEntity<>(user, headers);
        
        return restTemplate.exchange(url, HttpMethod.PUT, request, User.class, id);
    }
    
    /**
     * DELETE request.
     */
    public void deleteUser(Long id) {
        log.info("DELETE request for user: {}", id);
        
        String url = BASE_URL + "/users/{id}";
        restTemplate.delete(url, id);
        
        log.info("User deleted: {}", id);
    }
    
    /**
     * Custom headers in request.
     */
    public ResponseEntity<User> getUserWithCustomHeaders(Long id) {
        log.info("GET request with custom headers");
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Custom-Header", "CustomValue");
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        
        String url = BASE_URL + "/users/{id}";
        return restTemplate.exchange(url, HttpMethod.GET, entity, User.class, id);
    }
    
    /**
     * Exchange method - full control over request/response.
     */
    public ResponseEntity<String> makeCustomRequest() {
        log.info("Custom request using exchange");
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer token123");
        
        Map<String, String> body = new HashMap<>();
        body.put("key", "value");
        
        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
        
        return restTemplate.exchange(
                BASE_URL + "/posts",
                HttpMethod.POST,
                request,
                String.class
        );
    }
}
```

```java
// src/main/java/org/example/patterns/webmvc/resttemplate/RestTemplateController.java
package org.example.patterns.webmvc.resttemplate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.model.ApiResponse;
import org.example.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/rest-template")
@RequiredArgsConstructor
public class RestTemplateController {
    
    private final RestTemplateService restTemplateService;
    
    @GetMapping("/demo")
    public ApiResponse<String> demo() {
        return ApiResponse.success("REST Template Pattern - Synchronous HTTP client");
    }
    
    @GetMapping("/get-user/{id}")
    public ApiResponse<User> getUser(@PathVariable Long id) {
        log.info("Fetching user via RestTemplate: {}", id);
        User user = restTemplateService.getUserById(id);
        return ApiResponse.success(user);
    }
    
    @GetMapping("/get-user-details/{id}")
    public ResponseEntity<User> getUserDetails(@PathVariable Long id) {
        log.info("Fetching user details via RestTemplate: {}", id);
        return restTemplateService.getUserWithDetails(id);
    }
    
    @GetMapping("/get-users")
    public ApiResponse<List<User>> getUsers(@RequestParam(defaultValue = "5") Integer limit) {
        log.info("Fetching users with limit: {}", limit);
        List<User> users = restTemplateService.getUsersWithParams(limit);
        return ApiResponse.success(users);
    }
    
    @PostMapping("/create-user")
    public ApiResponse<User> createUser(@RequestBody User user) {
        log.info("Creating user via RestTemplate");
        User created = restTemplateService.createUser(user);
        return ApiResponse.success(created);
    }
    
    @PutMapping("/update-user/{id}")
    public ApiResponse<String> updateUser(@PathVariable Long id, @RequestBody User user) {
        log.info("Updating user via RestTemplate: {}", id);
        restTemplateService.updateUser(id, user);
        return ApiResponse.success("User updated");
    }
    
    @DeleteMapping("/delete-user/{id}")
    public ApiResponse<String> deleteUser(@PathVariable Long id) {
        log.info("Deleting user via RestTemplate: {}", id);
        restTemplateService.deleteUser(id);
        return ApiResponse.success("User deleted");
    }
}
```

## 16. Pattern 14: Content Negotiation Pattern

```java
// src/main/java/org/example/patterns/webmvc/contentnegotiation/ContentNegotiationController.java
package org.example.patterns.webmvc.contentnegotiation;

import lombok.extern.slf4j.Slf4j;
import org.example.model.Product;
import org.example.model.User;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Content Negotiation Pattern.
 * Returns different content types based on client preferences.
 */
@Slf4j
@Controller
@RequestMapping("/content-negotiation")
public class ContentNegotiationController {
    
    @GetMapping("/demo")
    public String demo(org.springframework.ui.Model model) {
        model.addAttribute("pattern", "Content Negotiation Pattern");
        model.addAttribute("info", 
            "Server responds with different formats based on Accept header or extension");
        model.addAttribute("methods", new String[]{
            "Accept header: application/json, application/xml, text/html",
            "URL extension: .json, .xml, .html",
            "Query parameter: ?format=json"
        });
        
        return "patterns/content-negotiation";
    }
    
    /**
     * Produces JSON by default.
     */
    @GetMapping(value = "/user", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public User getUserAsJson() {
        log.info("Returning user as JSON");
        return createSampleUser();
    }
    
    /**
     * Produces XML when requested.
     */
    @GetMapping(value = "/user", produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    public User getUserAsXml() {
        log.info("Returning user as XML");
        return createSampleUser();
    }
    
    /**
     * Produces HTML view.
     */
    @GetMapping(value = "/user", produces = MediaType.TEXT_HTML_VALUE)
    public String getUserAsHtml(org.springframework.ui.Model model) {
        log.info("Returning user as HTML");
        model.addAttribute("user", createSampleUser());
        return "patterns/user-view";
    }
    
    /**
     * Multiple produces - content negotiation based on Accept header.
     */
    @GetMapping(value = "/product", 
                produces = {MediaType.APPLICATION_JSON_VALUE, 
                           MediaType.APPLICATION_XML_VALUE})
    @ResponseBody
    public Product getProduct() {
        log.info("Returning product with content negotiation");
        return new Product(1L, "Laptop", "Gaming Laptop", 
                new BigDecimal("1299.99"), 10, "Electronics");
    }
    
    /**
     * Content negotiation with @ResponseBody.
     */
    @GetMapping("/users")
    @ResponseBody
    public List<User> getUsers() {
        log.info("Returning users list");
        return Arrays.asList(
            createSampleUser(),
            new User(2L, "jane.doe", "jane@example.com", 
                    "Jane", "Doe", LocalDateTime.now())
        );
    }
    
    /**
     * Using path extension for content type (.json, .xml).
     */
    @GetMapping("/data/{id}")
    @ResponseBody
    public DataResponse getData(@PathVariable Long id) {
        log.info("Returning data for id: {}", id);
        return new DataResponse(id, "Sample Data", LocalDateTime.now());
    }
    
    /**
     * Consumes specific content type.
     */
    @PostMapping(value = "/create", 
                consumes = MediaType.APPLICATION_JSON_VALUE,
                produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public User createUser(@RequestBody User user) {
        log.info("Creating user from JSON: {}", user);
        user.setId(100L);
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }
    
    /**
     * Different methods for different content types.
     */
    @PostMapping(value = "/submit", 
                consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseBody
    public String submitForm(@RequestParam String name, @RequestParam String value) {
        log.info("Form submitted: {}={}", name, value);
        return "Form processed: " + name + " = " + value;
    }
    
    @PostMapping(value = "/submit", 
                consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String submitJson(@RequestBody FormData data) {
        log.info("JSON submitted: {}", data);
        return "JSON processed: " + data;
    }
    
    private User createSampleUser() {
        return new User(1L, "john.doe", "john@example.com", 
                "John", "Doe", LocalDateTime.now());
    }
    
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class DataResponse {
        private Long id;
        private String data;
        private LocalDateTime timestamp;
    }
    
    @lombok.Data
    public static class FormData {
        private String name;
        private String value;
    }
}
```

```java
// src/main/java/org/example/patterns/webmvc/contentnegotiation/CustomContentNegotiationStrategy.java
package org.example.patterns.webmvc.contentnegotiation;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.accept.ContentNegotiationStrategy;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.Collections;
import java.util.List;

/**
 * Custom content negotiation strategy.
 */
@Slf4j
public class CustomContentNegotiationStrategy implements ContentNegotiationStrategy {
    
    @Override
    public List<MediaType> resolveMediaTypes(NativeWebRequest request) 
            throws HttpMediaTypeNotAcceptableException {
        
        HttpServletRequest servletRequest = request.getNativeRequest(HttpServletRequest.class);
        
        if (servletRequest != null) {
            String customHeader = servletRequest.getHeader("X-Response-Format");
            
            if ("json".equalsIgnoreCase(customHeader)) {
                log.info("Custom negotiation: JSON requested");
                return Collections.singletonList(MediaType.APPLICATION_JSON);
            } else if ("xml".equalsIgnoreCase(customHeader)) {
                log.info("Custom negotiation: XML requested");
                return Collections.singletonList(MediaType.APPLICATION_XML);
            }
        }
        
        return Collections.singletonList(MediaType.ALL);
    }
}
```

## 17. Pattern 15: Multipart Resolver Pattern

```java
// src/main/java/org/example/patterns/webmvc/multipartresolver/MultipartResolverController.java
package org.example.patterns.webmvc.multipartresolver;

import lombok.extern.slf4j.Slf4j;
import org.example.model.FileUploadForm;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Multipart Resolver Pattern.
 * Handles file uploads and multipart form data.
 */
@Slf4j
@Controller
@RequestMapping("/multipart-resolver")
public class MultipartResolverController {
    
    private static final String UPLOAD_DIR = "uploads/";
    private final List<UploadedFileInfo> uploadedFiles = new ArrayList<>();
    
    @GetMapping("/demo")
    public String demo(Model model) {
        model.addAttribute("pattern", "Multipart Resolver Pattern");
        model.addAttribute("info", 
            "StandardServletMultipartResolver handles file uploads");
        model.addAttribute("features", new String[]{
            "Single file upload",
            "Multiple file upload",
            "File with form data",
            "Size limits and validation"
        });
        
        return "patterns/multipart-resolver";
    }
    
    @GetMapping("/upload-form")
    public String showUploadForm(Model model) {
        model.addAttribute("uploadForm", new FileUploadForm());
        return "patterns/upload-form";
    }
    
    /**
     * Single file upload.
     */
    @PostMapping("/upload-single")
    public String uploadSingleFile(@RequestParam("file") MultipartFile file, Model model) {
        log.info("Single file upload: {}", file.getOriginalFilename());
        
        if (file.isEmpty()) {
            model.addAttribute("message", "Please select a file to upload");
            return "patterns/upload-result";
        }
        
        try {
            UploadedFileInfo fileInfo = saveFile(file);
            uploadedFiles.add(fileInfo);
            
            model.addAttribute("message", "File uploaded successfully");
            model.addAttribute("fileInfo", fileInfo);
            
            log.info("File saved: {}", fileInfo);
            
        } catch (IOException e) {
            log.error("Failed to upload file", e);
            model.addAttribute("message", "Failed to upload file: " + e.getMessage());
        }
        
        return "patterns/upload-result";
    }
    
    /**
     * Multiple files upload.
     */
    @PostMapping("/upload-multiple")
    public String uploadMultipleFiles(@RequestParam("files") MultipartFile[] files, 
                                     Model model) {
        log.info("Multiple files upload: {} files", files.length);
        
        List<UploadedFileInfo> uploaded = new ArrayList<>();
        
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                try {
                    UploadedFileInfo fileInfo = saveFile(file);
                    uploaded.add(fileInfo);
                    uploadedFiles.add(fileInfo);
                } catch (IOException e) {
                    log.error("Failed to upload file: {}", file.getOriginalFilename(), e);
                }
            }
        }
        
        model.addAttribute("message", uploaded.size() + " files uploaded successfully");
        model.addAttribute("uploadedFiles", uploaded);
        
        return "patterns/upload-result";
    }
    
    /**
     * File upload with form data.
     */
    @PostMapping("/upload-with-data")
    public String uploadWithFormData(@ModelAttribute FileUploadForm form, Model model) {
        log.info("File upload with form data - Description: {}", form.getDescription());
        
        MultipartFile file = form.getFile();
        
        if (file.isEmpty()) {
            model.addAttribute("message", "Please select a file");
            return "patterns/upload-result";
        }
        
        try {
            UploadedFileInfo fileInfo = saveFile(file);
            fileInfo.setDescription(form.getDescription());
            uploadedFiles.add(fileInfo);
            
            model.addAttribute("message", "File and data uploaded successfully");
            model.addAttribute("fileInfo", fileInfo);
            
        } catch (IOException e) {
            log.error("Failed to upload file", e);
            model.addAttribute("message", "Upload failed: " + e.getMessage());
        }
        
        return "patterns/upload-result";
    }
    
    /**
     * File upload with validation.
     */
    @PostMapping("/upload-validated")
    public String uploadWithValidation(@RequestParam("file") MultipartFile file, 
                                      Model model) {
        log.info("Validated file upload: {}", file.getOriginalFilename());
        
        // Validate file
        String validationError = validateFile(file);
        if (validationError != null) {
            model.addAttribute("message", validationError);
            return "patterns/upload-result";
        }
        
        try {
            UploadedFileInfo fileInfo = saveFile(file);
            uploadedFiles.add(fileInfo);
            
            model.addAttribute("message", "File validated and uploaded successfully");
            model.addAttribute("fileInfo", fileInfo);
            
        } catch (IOException e) {
            log.error("Failed to upload file", e);
            model.addAttribute("message", "Upload failed: " + e.getMessage());
        }
        
        return "patterns/upload-result";
    }
    
    /**
     * List all uploaded files.
     */
    @GetMapping("/list")
    public String listUploadedFiles(Model model) {
        model.addAttribute("uploadedFiles", uploadedFiles);
        model.addAttribute("totalFiles", uploadedFiles.size());
        return "patterns/file-list";
    }
    
    private UploadedFileInfo saveFile(MultipartFile file) throws IOException {
        // Create upload directory if not exists
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";
        String savedFilename = UUID.randomUUID().toString() + extension;
        
        // Save file
        Path filePath = uploadPath.resolve(savedFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        return new UploadedFileInfo(
                originalFilename,
                savedFilename,
                file.getContentType(),
                file.getSize(),
                filePath.toString()
        );
    }
    
    private String validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            return "File is empty";
        }
        
        // Check file size (max 5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            return "File size exceeds maximum limit of 5MB";
        }
        
        // Check file type
        String contentType = file.getContentType();
        if (contentType == null || 
            (!contentType.startsWith("image/") && 
             !contentType.equals("application/pdf"))) {
            return "Only images and PDF files are allowed";
        }
        
        return null; // Valid
    }
    
    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class UploadedFileInfo {
        private String originalFilename;
        private String savedFilename;
        private String contentType;
        private Long size;
        private String path;
        private String description;
        
        public UploadedFileInfo(String originalFilename, String savedFilename, 
                               String contentType, Long size, String path) {
            this.originalFilename = originalFilename;
            this.savedFilename = savedFilename;
            this.contentType = contentType;
            this.size = size;
            this.path = path;
        }
        
        public String getFormattedSize() {
            if (size < 1024) return size + " B";
            if (size < 1024 * 1024) return String.format("%.2f KB", size / 1024.0);
            return String.format("%.2f MB", size / (1024.0 * 1024.0));
        }
    }
}
```

```java
// src/main/java/org/example/patterns/webmvc/multipartresolver/MultipartConfig.java
package org.example.patterns.webmvc.multipartresolver;

import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;

import jakarta.servlet.MultipartConfigElement;

/**
 * Multipart configuration for file uploads.
 */
@Configuration
public class MultipartConfig {
    
    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        
        // Maximum file size (10MB)
        factory.setMaxFileSize(DataSize.ofMegabytes(10));
        
        // Maximum request size (50MB)
        factory.setMaxRequestSize(DataSize.ofMegabytes(50));
        
        // File size threshold after which files will be written to disk
        factory.setFileSizeThreshold(DataSize.ofKilobytes(512));
        
        return factory.createMultipartConfig();
    }
}
```

## 18. Maven Configuration (pom.xml)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
        <relativePath/>
    </parent>
    
    <groupId>org.example</groupId>
    <artifactId>spring-web-mvc-patterns</artifactId>
    <version>1.0-SNAPSHOT</version>
    <name>Spring Web MVC Patterns</name>
    <description>Demonstration of Web MVC patterns in Spring</description>
    
    <properties>
        <java.version>17</java.version>
    </properties>
    
    <dependencies>
        <!-- Spring Boot Web Starter -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        
        <!-- Thymeleaf Template Engine -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-thymeleaf</artifactId>
        </dependency>
        
        <!-- Validation -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        
        <!-- Jackson XML Support -->
        <dependency>
            <groupId>com.fasterxml.jackson.dataformat</groupId>
            <artifactId>jackson-dataformat-xml</artifactId>
        </dependency>
        
        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        
        <!-- Spring Boot DevTools -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-devtools</artifactId>
            <scope>runtime</scope>
            <optional>true</optional>
        </dependency>
        
        <!-- Spring Boot Test -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

## 19. Application Configuration

```properties
# src/main/resources/application.properties
spring.application.name=spring-web-mvc-patterns

# Server Configuration
server.port=8080
server.servlet.context-path=/

# Thymeleaf Configuration
spring.thymeleaf.cache=false
spring.thymeleaf.enabled=true
spring.thymeleaf.prefix=classpath:/templates/
spring.thymeleaf.suffix=.html
spring.thymeleaf.mode=HTML

# Multipart Configuration
spring.servlet.multipart.enabled=true
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=50MB
spring.servlet.multipart.file-size-threshold=512KB

# Logging
logging.level.root=INFO
logging.level.org.example=DEBUG
logging.level.org.springframework.web=DEBUG

# Error Handling
server.error.include-message=always
server.error.include-binding-errors=always
server.error.include-stacktrace=on_param
server.error.include-exception=false

# Content Negotiation
spring.mvc.contentnegotiation.favor-parameter=true
spring.mvc.contentnegotiation.parameter-name=format

# Static Resources
spring.web.resources.static-locations=classpath:/static/
spring.web.resources.cache.period=3600
```

## 20. Sample Thymeleaf Templates

```html
<!-- src/main/resources/templates/patterns/front-controller.html -->
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <title>Front Controller Pattern</title>
    <link rel="stylesheet" th:href="@{/css/styles.css}">
</head>
<body>
    <h1 th:text="${pattern}">Pattern Name</h1>
    <p th:text="${description}">Description</p>
    
    <h2>Request Flow:</h2>
    <ol>
        <li th:each="step : ${flow}" th:text="${step}">Flow Step</li>
    </ol>
    
    <a th:href="@{/}">Back to Home</a>
</body>
</html>
```

```html
<!-- src/main/resources/templates/patterns/user-form.html -->
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <title>User Form - Model Attribute Pattern</title>
</head>
<body>
    <h1>Create User</h1>
    
    <form th:action="@{/model-attribute/create-user}" 
          th:object="${user}" 
          method="post">
        
        <div>
            <label>Username:</label>
            <input type="text" th:field="*{username}" required/>
        </div>
        
        <div>
            <label>Email:</label>
            <input type="email" th:field="*{email}" required/>
        </div>
        
        <div>
            <label>First Name:</label>
            <input type="text" th:field="*{firstName}"/>
        </div>
        
        <div>
            <label>Last Name:</label>
            <input type="text" th:field="*{lastName}"/>
        </div>
        
        <button type="submit">Create User</button>
    </form>
</body>
</html>
```

```html
<!-- src/main/resources/templates/patterns/upload-form.html -->
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <title>File Upload - Multipart Resolver Pattern</title>
</head>
<body>
    <h1>File Upload</h1>
    
    <h2>Single File Upload</h2>
    <form th:action="@{/multipart-resolver/upload-single}" 
          method="post" 
          enctype="multipart/form-data">
        <input type="file" name="file" required/>
        <button type="submit">Upload</button>
    </form>
    
    <h2>Multiple Files Upload</h2>
    <form th:action="@{/multipart-resolver/upload-multiple}" 
          method="post" 
          enctype="multipart/form-data">
        <input type="file" name="files" multiple required/>
        <button type="submit">Upload Files</button>
    </form>
    
    <h2>File with Form Data</h2>
    <form th:action="@{/multipart-resolver/upload-with-data}" 
          th:object="${uploadForm}"
          method="post" 
          enctype="multipart/form-data">
        
        <div>
            <label>Description:</label>
            <input type="text" th:field="*{description}"/>
        </div>
        
        <div>
            <label>File:</label>
            <input type="file" th:field="*{file}" required/>
        </div>
        
        <button type="submit">Upload</button>
    </form>
</body>
</html>
```

## 21. README.md

```markdown
# Spring Web MVC Patterns

Comprehensive demonstration of 15 essential Web MVC patterns in Spring Framework.

## Patterns Implemented

### 1. Front Controller Pattern
**Endpoint:** `/front-controller/demo`

DispatcherServlet acts as the centralized entry point for all web requests.

**Key Points:**
- Single entry point for all requests
- Delegates to appropriate handlers
- Centralizes request processing logic

### 2. Dispatcher Pattern
**Endpoint:** `/dispatcher/demo`

DispatcherServlet dispatches requests to various components (handlers, interceptors, view resolvers).

### 3. Handler Mapping Pattern
**Endpoints:**
- `/handler-mapping/legacy` - Legacy Controller interface
- `/handler-mapping/simple` - SimpleUrlHandlerMapping
- `/handler-mapping/annotation` - Annotation-based mapping

Maps URLs to handler objects.

**Mapping Types:**
- `@RequestMapping` annotations (modern)
- `SimpleUrlHandlerMapping` (legacy)
- `BeanNameUrlHandlerMapping`

### 4. Handler Adapter Pattern
**Endpoint:** `/handler-adapter/demo`

Adapts different handler types to a common interface.

**Adapters:**
- `RequestMappingHandlerAdapter` - @RequestMapping methods
- `SimpleControllerHandlerAdapter` - Controller interface
- `HttpRequestHandlerAdapter` - HttpRequestHandler
- Custom adapters for custom handlers

### 5. View Resolver Pattern
**Endpoints:**
- `/view-resolver/demo` - Overview
- `/view-resolver/thymeleaf` - Thymeleaf template
- `/view-resolver/json` - JSON view
- `/view-resolver/xml` - XML view

Resolves logical view names to actual view implementations.

**Resolvers:**
- `ThymeleafViewResolver` - Thymeleaf templates
- `InternalResourceViewResolver` - JSP views
- `BeanNameViewResolver` - Bean-based views
- `ContentNegotiatingViewResolver` - Content negotiation

### 6. Interceptor Pattern
**Endpoint:** `/interceptor/demo`

Intercepts requests at different phases.

**Interceptors:**
- `LoggingInterceptor` - Request logging
- `PerformanceInterceptor` - Performance monitoring
- `SecurityInterceptor` - Authentication check

**Phases:**
1. `preHandle()` - Before handler execution
2. `postHandle()` - After handler, before view
3. `afterCompletion()` - After complete processing

**Usage:**
```java
@Override
public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(new LoggingInterceptor())
            .addPathPatterns("/**")
            .excludePathPatterns("/static/**");
}
```

### 7. Exception Resolver Pattern
**Endpoints:**
- `/exception-resolver/demo` - Overview
- `/exception-resolver/not-found/{id}` - 404 error
- `/exception-resolver/validation-error` - Validation error
- `/exception-resolver/business-error` - Business exception

Centralized exception handling with `@ControllerAdvice`.

**Exception Types:**
- `ResourceNotFoundException` → 404
- `ValidationException` → 400
- `BusinessException` → 422
- `Exception` → 500

**Example:**
```java
@ExceptionHandler(ResourceNotFoundException.class)
@ResponseStatus(HttpStatus.NOT_FOUND)
public ModelAndView handleResourceNotFound(ResourceNotFoundException ex) {
    // Return error view
}
```

### 8. Model Attribute Pattern
**Endpoints:**
- `/model-attribute/demo` - Overview
- `/model-attribute/create-user` - User form with @ModelAttribute

Binds form data to objects and populates common model attributes.

**Uses:**
1. **Method-level** - Populate common data before every handler
2. **Parameter-level** - Bind form data to objects

**Example:**
```java
@ModelAttribute("categories")
public List<String> populateCategories() {
    return Arrays.asList("Category1", "Category2");
}

@PostMapping("/create")
public String create(@ModelAttribute("user") User user) {
    // user is automatically populated from form data
}
```

### 9. Session Attributes Pattern
**Endpoints:**
- `/session-attributes/login` - Login (stores user in session)
- `/session-attributes/dashboard` - Requires session user
- `/session-attributes/add-to-cart` - Shopping cart in session
- `/session-attributes/view-cart` - View cart
- `/session-attributes/logout` - Clear session

Stores model attributes in HTTP session across multiple requests.

**Example:**
```java
@Controller
@SessionAttributes({"cart", "currentUser"})
public class SessionController {
    
    @ModelAttribute("cart")
    public ShoppingCart createCart() {
        return new ShoppingCart();
    }
    
    // cart persists across requests
}
```

**Clear Session:**
```java
public String logout(SessionStatus sessionStatus) {
    sessionStatus.setComplete(); // Clear all session attributes
}
```

### 10. Flash Attributes Pattern
**Endpoints:**
- `/flash-attributes/register` - Registration form
- `/flash-attributes/success` - Success page (receives flash attributes)

Temporary attributes that survive redirect (Post-Redirect-Get pattern).

**Example:**
```java
@PostMapping("/register")
public String register(RedirectAttributes redirectAttributes) {
    redirectAttributes.addFlashAttribute("message", "Success!");
    redirectAttributes.addFlashAttribute("user", user);
    return "redirect:/success";
}
```

**Key Points:**
- Flash attributes survive redirect
- Automatically removed after next request
- Perfect for PRG (Post-Redirect-Get) pattern

### 11. Redirect Pattern
**Endpoints:**
- `/redirect/simple` - Simple redirect
- `/redirect/with-path/{id}` - Redirect with path variable
- `/redirect/with-params` - Redirect with query params
- `/redirect/view-object` - RedirectView object
- `/redirect/external` - External redirect

Server instructs client to make new request.

**Methods:**
1. **String with prefix:**
```java
return "redirect:/target";
```

2. **RedirectView object:**
```java
RedirectView view = new RedirectView();
view.setUrl("/target");
return view;
```

3. **With attributes:**
```java
redirectAttributes.addAttribute("param", "value"); // URL param
redirectAttributes.addFlashAttribute("msg", "data"); // Flash
```

### 12. Forward Pattern
**Endpoints:**
- `/forward/simple` - Simple forward
- `/forward/with-data` - Forward with model data
- `/forward/conditional` - Conditional forward
- `/forward/chain-start` - Forward chain

Server-side forwarding (URL unchanged in browser).

**Example:**
```java
return "forward:/target"; // Server-side forward
```

**vs Redirect:**
- **Forward:** Same request, server-side, URL unchanged
- **Redirect:** New request, client-side, URL changed

**Forward Chain:**
```
/chain-start → /chain-middle → /chain-end
```

### 13. REST Template Pattern
**Endpoints:**
- `/rest-template/get-user/{id}` - GET request
- `/rest-template/create-user` - POST request
- `/rest-template/update-user/{id}` - PUT request
- `/rest-template/delete-user/{id}` - DELETE request

Synchronous HTTP client for REST API calls.

**Operations:**
```java
// GET
User user = restTemplate.getForObject(url, User.class, id);
ResponseEntity<User> response = restTemplate.getForEntity(url, User.class, id);

// POST
User created = restTemplate.postForObject(url, user, User.class);

// PUT
restTemplate.put(url, user, id);

// DELETE
restTemplate.delete(url, id);

// EXCHANGE (full control)
ResponseEntity<User> response = restTemplate.exchange(
    url, HttpMethod.GET, entity, User.class
);
```

### 14. Content Negotiation Pattern
**Endpoints:**
- `/content-negotiation/user` - Returns JSON/XML/HTML based on Accept header
- `/content-negotiation/product` - Multiple formats
- `/content-negotiation/users` - List response

Returns different content types based on client preferences.

**Methods:**
1. **Accept Header:**
```
Accept: application/json → JSON
Accept: application/xml → XML
Accept: text/html → HTML
```

2. **URL Extension:**
```
/user.json → JSON
/user.xml → XML
```

3. **Query Parameter:**
```
/user?format=json → JSON
```

**Example:**
```java
@GetMapping(value = "/user", 
            produces = {MediaType.APPLICATION_JSON_VALUE,
                       MediaType.APPLICATION_XML_VALUE})
public User getUser() {
    return user;
}
```

### 15. Multipart Resolver Pattern
**Endpoints:**
- `/multipart-resolver/upload-form` - File upload form
- `/multipart-resolver/upload-single` - Single file
- `/multipart-resolver/upload-multiple` - Multiple files
- `/multipart-resolver/upload-with-data` - File + form data
- `/multipart-resolver/list` - List uploaded files

Handles file uploads and multipart form data.

**Configuration:**
```properties
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=50MB
spring.servlet.multipart.file-size-threshold=512KB
```

**Single File:**
```java
@PostMapping("/upload")
public String upload(@RequestParam("file") MultipartFile file) {
    file.transferTo(new File(path));
}
```

**Multiple Files:**
```java
@PostMapping("/upload-multiple")
public String upload(@RequestParam("files") MultipartFile[] files) {
    for (MultipartFile file : files) {
        // process each file
    }
}
```

**With Form Data:**
```java
@PostMapping("/upload-with-data")
public String upload(@ModelAttribute FileUploadForm form) {
    MultipartFile file = form.getFile();
    String description = form.getDescription();
}
```

## Running the Application

### Prerequisites
- Java 17+
- Maven 3.6+

### Build and Run
```bash
mvn clean install
mvn spring-boot:run
```

### Access Application
- Base URL: http://localhost:8080
- Pattern Demos: http://localhost:8080/{pattern-name}/demo

## Testing Patterns

### Front Controller
```bash
curl http://localhost:8080/front-controller/demo
```

### Interceptor
```bash
# Triggers all interceptors
curl http://localhost:8080/interceptor/demo

# Triggers slow request warning
curl http://localhost:8080/interceptor/test-slow
```

### Exception Resolver
```bash
# 404 error
curl http://localhost:8080/exception-resolver/not-found/123

# Validation error
curl http://localhost:8080/exception-resolver/validation-error
```

### REST Template
```bash
# GET user
curl http://localhost:8080/rest-template/get-user/1

# Create user
curl -X POST http://localhost:8080/rest-template/create-user \
  -H "Content-Type: application/json" \
  -d '{"username":"john","email":"john@example.com"}'
```

### Content Negotiation
```bash
# JSON response
curl -H "Accept: application/json" \
  http://localhost:8080/content-negotiation/user

# XML response
curl -H "Accept: application/xml" \
  http://localhost:8080/content-negotiation/user

# Using query parameter
curl http://localhost:8080/content-negotiation/user?format=json
```

### File Upload
```bash
# Single file
curl -X POST http://localhost:8080/multipart-resolver/upload-single \
  -F "file=@/path/to/file.pdf"

# Multiple files
curl -X POST http://localhost:8080/multipart-resolver/upload-multiple \
  -F "files=@file1.jpg" \
  -F "files=@file2.jpg"

# With form data
curl -X POST http://localhost:8080/multipart-resolver/upload-with-data \
  -F "file=@document.pdf" \
  -F "description=Important document"
```

## Key Concepts

### Request Processing Flow
```
Client Request
    ↓
DispatcherServlet (Front Controller)
    ↓
HandlerMapping (Find handler)
    ↓
Interceptors (preHandle)
    ↓
HandlerAdapter (Execute handler)
    ↓
Controller (Business logic)
    ↓
Interceptors (postHandle)
    ↓
ViewResolver (Resolve view)
    ↓
View (Render)
    ↓
Interceptors (afterCompletion)
    ↓
Client Response
```

### Pattern Relationships

**Front Controller → Dispatcher:**
- DispatcherServlet IS the front controller
- Dispatches to various components

**Handler Mapping → Handler Adapter:**
- HandlerMapping finds handler
- HandlerAdapter executes handler

**View Resolver → Content Negotiation:**
- ViewResolver resolves view name
- ContentNegotiatingViewResolver handles multiple formats

**Redirect → Flash Attributes:**
- Redirect changes URL
- Flash attributes survive redirect

**Forward → Model Attribute:**
- Forward keeps same request
- Model attributes accessible

## Best Practices

### 1. Use Appropriate Pattern
- **@RequestMapping** for most controllers
- **Interceptors** for cross-cutting concerns
- **@ControllerAdvice** for exception handling
- **Flash Attributes** for PRG pattern

### 2. Content Negotiation
- Support multiple formats (JSON, XML, HTML)
- Use proper `@produces` and `@consumes`
- Configure content negotiation strategy

### 3. File Uploads
- Set appropriate size limits
- Validate file types
- Handle errors gracefully
- Use streaming for large files

### 4. Exception Handling
- Use `@ControllerAdvice` for global handling
- Return appropriate HTTP status codes
- Provide meaningful error messages
- Log exceptions properly

### 5. Session Management
- Use `@SessionAttributes` sparingly
- Clear sessions when done (`SessionStatus.setComplete()`)
- Consider security implications
- Use Redis for distributed sessions

### 6. Performance
- Use interceptors for monitoring
- Cache static resources
- Enable compression
- Use async processing for long operations

## Common Pitfalls

### 1. Redirect Loop
```java
// Wrong - creates infinite loop
@GetMapping("/redirect")
public String redirect() {
    return "redirect:/redirect";
}
```

### 2. Session Attribute Not Cleared
```java
// Always clear when done
sessionStatus.setComplete();
```

### 3. File Upload Size
```properties
# Set appropriate limits
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=50MB
```

### 4. Exception Handler Order
```java
// More specific handlers first
@ExceptionHandler(ResourceNotFoundException.class) // Specific
@ExceptionHandler(Exception.class) // Generic (last)
```

## Testing

### Unit Tests
```bash
mvn test
```

### Integration Tests
```bash
mvn verify
```

### Manual Testing
Use Postman collection or curl commands provided above.

## License

MIT License - free to use for learning and projects.
```

This completes the comprehensive implementation of all 15 Web MVC Patterns with working code, configurations, templates, and thorough documentation!