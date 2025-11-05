### [Continue](https://continue.dev) session transcript
 Exported: 11/5/2025, 4:04:16 PM

#### _User_

> generate a java program in spring for each of the below patterns:
> ```
> **CORS Patterns**
> - Cross-Origin Resource Sharing Pattern
> - Global CORS Configuration Pattern
> - Controller-level CORS Pattern
> - Method-level CORS Pattern
> - CORS Filter Pattern
> ```

#### _Assistant_

> # Spring Boot CORS Patterns Implementation
> 
> I'll create a comprehensive Spring Boot project demonstrating all CORS patterns.
> 
> ## 1. Project Structure & Dependencies
> 
> ```xml pom.xml
> <?xml version="1.0" encoding="UTF-8"?>
> <project xmlns="http://maven.apache.org/POM/4.0.0"
>          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
>          xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
>          http://maven.apache.org/xsd/maven-4.0.0.xsd">
>     <modelVersion>4.0.0</modelVersion>
>     
>     <parent>
>         <groupId>org.springframework.boot</groupId>
>         <artifactId>spring-boot-starter-parent</artifactId>
>         <version>3.2.0</version>
>         <relativePath/>
>     </parent>
>     
>     <groupId>com.example</groupId>
>     <artifactId>cors-patterns-demo</artifactId>
>     <version>1.0.0</version>
>     <name>CORS Patterns Demo</name>
>     
>     <properties>
>         <java.version>17</java.version>
>     </properties>
>     
>     <dependencies>
>         <dependency>
>             <groupId>org.springframework.boot</groupId>
>             <artifactId>spring-boot-starter-web</artifactId>
>         </dependency>
>         
>         <dependency>
>             <groupId>org.springframework.boot</groupId>
>             <artifactId>spring-boot-starter-security</artifactId>
>         </dependency>
>         
>         <dependency>
>             <groupId>org.projectlombok</groupId>
>             <artifactId>lombok</artifactId>
>             <optional>true</optional>
>         </dependency>
>     </dependencies>
>     
>     <build>
>         <plugins>
>             <plugin>
>                 <groupId>org.springframework.boot</groupId>
>                 <artifactId>spring-boot-maven-plugin</artifactId>
>             </plugin>
>         </plugins>
>     </build>
> </project>
> ```
> 
> ## 2. Main Application
> 
> ```java src/main/java/com/example/cors/CorsPatternsDemoApplication.java
> package com.example.cors;
> 
> import org.springframework.boot.SpringApplication;
> import org.springframework.boot.autoconfigure.SpringBootApplication;
> 
> @SpringBootApplication
> public class CorsPatternsDemoApplication {
>     
>     public static void main(String[] args) {
>         SpringApplication.run(CorsPatternsDemoApplication.class, args);
>     }
> }
> ```
> 
> ## 3. Pattern 1: Basic Cross-Origin Resource Sharing Pattern
> 
> ```java src/main/java/com/example/cors/controller/BasicCorsController.java
> package com.example.cors.controller;
> 
> import org.springframework.http.ResponseEntity;
> import org.springframework.web.bind.annotation.*;
> 
> import java.util.HashMap;
> import java.util.Map;
> 
> /**
>  * Basic CORS Pattern - Uses @CrossOrigin with default settings
>  * This allows requests from any origin
>  */
> @RestController
> @RequestMapping("/api/basic-cors")
> @CrossOrigin // Enables CORS for all methods in this controller with default settings
> public class BasicCorsController {
>     
>     @GetMapping("/data")
>     public ResponseEntity<Map<String, String>> getData() {
>         Map<String, String> data = new HashMap<>();
>         data.put("message", "Basic CORS enabled - accessible from any origin");
>         data.put("pattern", "Basic Cross-Origin Resource Sharing Pattern");
>         return ResponseEntity.ok(data);
>     }
>     
>     @PostMapping("/submit")
>     public ResponseEntity<Map<String, String>> submitData(@RequestBody Map<String, String> payload) {
>         Map<String, String> response = new HashMap<>();
>         response.put("status", "success");
>         response.put("received", payload.toString());
>         response.put("pattern", "Basic CORS Pattern with POST");
>         return ResponseEntity.ok(response);
>     }
> }
> ```
> 
> ## 4. Pattern 2: Global CORS Configuration Pattern
> 
> ```java src/main/java/com/example/cors/config/GlobalCorsConfiguration.java
> package com.example.cors.config;
> 
> import org.springframework.context.annotation.Bean;
> import org.springframework.context.annotation.Configuration;
> import org.springframework.web.cors.CorsConfiguration;
> import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
> import org.springframework.web.filter.CorsFilter;
> import org.springframework.web.servlet.config.annotation.CorsRegistry;
> import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
> 
> import java.util.Arrays;
> 
> /**
>  * Global CORS Configuration Pattern
>  * Configures CORS settings globally for the entire application
>  */
> @Configuration
> public class GlobalCorsConfiguration implements WebMvcConfigurer {
>     
>     /**
>      * Method 1: Using WebMvcConfigurer (Recommended for Spring MVC)
>      */
>     @Override
>     public void addCorsMappings(CorsRegistry registry) {
>         registry.addMapping("/api/global/**")
>                 .allowedOrigins("http://localhost:3000", "http://localhost:4200")
>                 .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
>                 .allowedHeaders("*")
>                 .exposedHeaders("Authorization", "X-Custom-Header")
>                 .allowCredentials(true)
>                 .maxAge(3600); // Cache preflight response for 1 hour
>         
>         // Different configuration for another path pattern
>         registry.addMapping("/api/public/**")
>                 .allowedOrigins("*")
>                 .allowedMethods("GET", "POST")
>                 .allowedHeaders("Content-Type", "Authorization")
>                 .maxAge(1800);
>     }
>     
>     /**
>      * Method 2: Using CorsFilter Bean (Alternative approach)
>      * This bean is commented out to avoid conflict with WebMvcConfigurer
>      * Uncomment if you prefer this approach over WebMvcConfigurer
>      */
>     // @Bean
>     public CorsFilter corsFilterBean() {
>         CorsConfiguration config = new CorsConfiguration();
>         config.setAllowCredentials(true);
>         config.setAllowedOrigins(Arrays.asList(
>             "http://localhost:3000",
>             "http://localhost:4200",
>             "https://example.com"
>         ));
>         config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
>         config.setAllowedHeaders(Arrays.asList("*"));
>         config.setExposedHeaders(Arrays.asList("Authorization", "X-Total-Count"));
>         config.setMaxAge(3600L);
>         
>         UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
>         source.registerCorsConfiguration("/**", config);
>         
>         return new CorsFilter(source);
>     }
> }
> ```
> 
> ```java src/main/java/com/example/cors/controller/GlobalCorsController.java
> package com.example.cors.controller;
> 
> import org.springframework.http.ResponseEntity;
> import org.springframework.web.bind.annotation.*;
> 
> import java.util.HashMap;
> import java.util.Map;
> 
> /**
>  * Controller to demonstrate Global CORS Configuration
>  * This controller doesn't need @CrossOrigin because global config applies
>  */
> @RestController
> @RequestMapping("/api/global")
> public class GlobalCorsController {
>     
>     @GetMapping("/users")
>     public ResponseEntity<Map<String, Object>> getUsers() {
>         Map<String, Object> response = new HashMap<>();
>         response.put("pattern", "Global CORS Configuration");
>         response.put("users", Arrays.asList("Alice", "Bob", "Charlie"));
>         response.put("note", "CORS configured globally via WebMvcConfigurer");
>         return ResponseEntity.ok(response);
>     }
>     
>     @PostMapping("/users")
>     public ResponseEntity<Map<String, String>> createUser(@RequestBody Map<String, String> user) {
>         Map<String, String> response = new HashMap<>();
>         response.put("status", "created");
>         response.put("username", user.get("username"));
>         response.put("pattern", "Global CORS - POST allowed");
>         return ResponseEntity.ok(response);
>     }
> }
> ```
> 
> ## 5. Pattern 3: Controller-level CORS Pattern
> 
> ```java src/main/java/com/example/cors/controller/ControllerLevelCorsController.java
> package com.example.cors.controller;
> 
> import org.springframework.http.ResponseEntity;
> import org.springframework.web.bind.annotation.*;
> 
> import java.util.HashMap;
> import java.util.Map;
> import java.util.Arrays;
> 
> /**
>  * Controller-level CORS Pattern
>  * Applies specific CORS configuration to all endpoints in this controller
>  */
> @RestController
> @RequestMapping("/api/controller-cors")
> @CrossOrigin(
>     origins = {"http://localhost:3000", "https://trusted-domain.com"},
>     methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE},
>     allowedHeaders = {"Content-Type", "Authorization", "X-Requested-With"},
>     exposedHeaders = {"X-Custom-Header", "X-Another-Header"},
>     allowCredentials = "true",
>     maxAge = 7200 // 2 hours
> )
> public class ControllerLevelCorsController {
>     
>     @GetMapping("/products")
>     public ResponseEntity<Map<String, Object>> getProducts() {
>         Map<String, Object> response = new HashMap<>();
>         response.put("pattern", "Controller-level CORS");
>         response.put("products", Arrays.asList(
>             Map.of("id", 1, "name", "Laptop"),
>             Map.of("id", 2, "name", "Mouse"),
>             Map.of("id", 3, "name", "Keyboard")
>         ));
>         response.put("allowedOrigins", Arrays.asList(
>             "http://localhost:3000",
>             "https://trusted-domain.com"
>         ));
>         return ResponseEntity.ok(response);
>     }
>     
>     @PostMapping("/products")
>     public ResponseEntity<Map<String, String>> addProduct(@RequestBody Map<String, String> product) {
>         Map<String, String> response = new HashMap<>();
>         response.put("status", "created");
>         response.put("productName", product.get("name"));
>         response.put("pattern", "Controller-level CORS applies to all methods");
>         return ResponseEntity.ok(response);
>     }
>     
>     @DeleteMapping("/products/{id}")
>     public ResponseEntity<Map<String, String>> deleteProduct(@PathVariable Long id) {
>         Map<String, String> response = new HashMap<>();
>         response.put("status", "deleted");
>         response.put("productId", id.toString());
>         response.put("note", "DELETE method allowed by controller-level CORS");
>         return ResponseEntity.ok(response);
>     }
> }
> ```
> 
> ## 6. Pattern 4: Method-level CORS Pattern
> 
> ```java src/main/java/com/example/cors/controller/MethodLevelCorsController.java
> package com.example.cors.controller;
> 
> import org.springframework.http.ResponseEntity;
> import org.springframework.web.bind.annotation.*;
> 
> import java.util.HashMap;
> import java.util.Map;
> import java.util.Arrays;
> 
> /**
>  * Method-level CORS Pattern
>  * Each method can have its own CORS configuration
>  * This provides fine-grained control over CORS settings
>  */
> @RestController
> @RequestMapping("/api/method-cors")
> public class MethodLevelCorsController {
>     
>     /**
>      * Public endpoint - allows all origins
>      */
>     @CrossOrigin(origins = "*", methods = RequestMethod.GET)
>     @GetMapping("/public-data")
>     public ResponseEntity<Map<String, String>> getPublicData() {
>         Map<String, String> response = new HashMap<>();
>         response.put("pattern", "Method-level CORS - Public");
>         response.put("data", "This endpoint allows requests from any origin");
>         response.put("corsConfig", "origins = *, methods = GET");
>         return ResponseEntity.ok(response);
>     }
>     
>     /**
>      * Restricted endpoint - specific origins only
>      */
>     @CrossOrigin(
>         origins = "http://localhost:3000",
>         methods = {RequestMethod.GET, RequestMethod.POST},
>         allowedHeaders = {"Authorization", "Content-Type"},
>         allowCredentials = "true"
>     )
>     @GetMapping("/restricted-data")
>     public ResponseEntity<Map<String, String>> getRestrictedData() {
>         Map<String, String> response = new HashMap<>();
>         response.put("pattern", "Method-level CORS - Restricted");
>         response.put("data", "Only accessible from localhost:3000");
>         response.put("credentials", "Cookies and credentials allowed");
>         return ResponseEntity.ok(response);
>     }
>     
>     /**
>      * Admin endpoint - very restrictive CORS
>      */
>     @CrossOrigin(
>         origins = "https://admin.example.com",
>         methods = RequestMethod.POST,
>         allowedHeaders = "Authorization",
>         maxAge = 600 // Cache for 10 minutes only
>     )
>     @PostMapping("/admin-action")
>     public ResponseEntity<Map<String, String>> performAdminAction(
>             @RequestBody Map<String, String> request) {
>         Map<String, String> response = new HashMap<>();
>         response.put("pattern", "Method-level CORS - Admin");
>         response.put("status", "Action performed");
>         response.put("corsConfig", "Single origin, POST only, short cache");
>         return ResponseEntity.ok(response);
>     }
>     
>     /**
>      * No CORS annotation - uses global configuration if available
>      */
>     @GetMapping("/default-cors")
>     public ResponseEntity<Map<String, String>> getDefaultCors() {
>         Map<String, String> response = new HashMap<>();
>         response.put("pattern", "No method-level CORS");
>         response.put("note", "Uses global CORS configuration or Spring defaults");
>         return ResponseEntity.ok(response);
>     }
>     
>     /**
>      * Multiple origins with specific methods
>      */
>     @CrossOrigin(
>         origins = {
>             "http://localhost:3000",
>             "http://localhost:4200",
>             "https://app.example.com"
>         },
>         methods = {RequestMethod.GET, RequestMethod.PUT},
>         exposedHeaders = {"X-Total-Count", "X-Page-Number"}
>     )
>     @PutMapping("/update/{id}")
>     public ResponseEntity<Map<String, String>> updateResource(
>             @PathVariable Long id,
>             @RequestBody Map<String, String> updates) {
>         Map<String, String> response = new HashMap<>();
>         response.put("pattern", "Method-level CORS - Multiple Origins");
>         response.put("resourceId", id.toString());
>         response.put("status", "updated");
>         response.put("allowedOrigins", "3 specific origins");
>         return ResponseEntity.ok(response);
>     }
> }
> ```
> 
> ## 7. Pattern 5: CORS Filter Pattern
> 
> ```java src/main/java/com/example/cors/filter/CustomCorsFilter.java
> package com.example.cors.filter;
> 
> import jakarta.servlet.*;
> import jakarta.servlet.http.HttpServletRequest;
> import jakarta.servlet.http.HttpServletResponse;
> import org.springframework.core.Ordered;
> import org.springframework.core.annotation.Order;
> import org.springframework.stereotype.Component;
> 
> import java.io.IOException;
> import java.util.Arrays;
> import java.util.List;
> 
> /**
>  * CORS Filter Pattern - Custom Filter Implementation
>  * This provides maximum flexibility for CORS handling
>  * Useful for complex CORS logic or when you need to handle CORS at the filter level
>  */
> @Component
> @Order(Ordered.HIGHEST_PRECEDENCE)
> public class CustomCorsFilter implements Filter {
>     
>     private static final List<String> ALLOWED_ORIGINS = Arrays.asList(
>         "http://localhost:3000",
>         "http://localhost:4200",
>         "https://app.example.com"
>     );
>     
>     private static final List<String> ALLOWED_METHODS = Arrays.asList(
>         "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
>     );
>     
>     private static final List<String> ALLOWED_HEADERS = Arrays.asList(
>         "Content-Type",
>         "Authorization",
>         "X-Requested-With",
>         "X-Custom-Header"
>     );
>     
>     private static final List<String> EXPOSED_HEADERS = Arrays.asList(
>         "Authorization",
>         "X-Total-Count",
>         "X-Custom-Response-Header"
>     );
>     
>     @Override
>     public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
>             throws IOException, ServletException {
>         
>         HttpServletRequest httpRequest = (HttpServletRequest) request;
>         HttpServletResponse httpResponse = (HttpServletResponse) response;
>         
>         String origin = httpRequest.getHeader("Origin");
>         String requestPath = httpRequest.getRequestURI();
>         
>         // Apply CORS only to specific paths (filter pattern endpoints)
>         if (requestPath.startsWith("/api/filter-cors")) {
>             
>             // Check if origin is allowed
>             if (origin != null && (ALLOWED_ORIGINS.contains(origin) || ALLOWED_ORIGINS.contains("*"))) {
>                 httpResponse.setHeader("Access-Control-Allow-Origin", origin);
>                 httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
>             }
>             
>             // Set allowed methods
>             httpResponse.setHeader("Access-Control-Allow-Methods", 
>                 String.join(", ", ALLOWED_METHODS));
>             
>             // Set allowed headers
>             httpResponse.setHeader("Access-Control-Allow-Headers", 
>                 String.join(", ", ALLOWED_HEADERS));
>             
>             // Set exposed headers
>             httpResponse.setHeader("Access-Control-Expose-Headers", 
>                 String.join(", ", EXPOSED_HEADERS));
>             
>             // Set max age for preflight cache
>             httpResponse.setHeader("Access-Control-Max-Age", "3600");
>             
>             // Handle preflight requests
>             if ("OPTIONS".equalsIgnoreCase(httpRequest.getMethod())) {
>                 httpResponse.setStatus(HttpServletResponse.SC_OK);
>                 return;
>             }
>         }
>         
>         chain.doFilter(request, response);
>     }
>     
>     @Override
>     public void init(FilterConfig filterConfig) throws ServletException {
>         // Initialization logic if needed
>     }
>     
>     @Override
>     public void destroy() {
>         // Cleanup logic if needed
>     }
> }
> ```
> 
> ```java src/main/java/com/example/cors/filter/AdvancedCorsFilter.java
> package com.example.cors.filter;
> 
> import jakarta.servlet.*;
> import jakarta.servlet.http.HttpServletRequest;
> import jakarta.servlet.http.HttpServletResponse;
> import org.slf4j.Logger;
> import org.slf4j.LoggerFactory;
> import org.springframework.stereotype.Component;
> 
> import java.io.IOException;
> import java.util.regex.Pattern;
> 
> /**
>  * Advanced CORS Filter with dynamic origin validation
>  * Demonstrates pattern matching and logging
>  */
> @Component
> public class AdvancedCorsFilter implements Filter {
>     
>     private static final Logger logger = LoggerFactory.getLogger(AdvancedCorsFilter.class);
>     
>     // Pattern to match localhost with any port
>     private static final Pattern LOCALHOST_PATTERN = Pattern.compile("^http://localhost:\\d+$");
>     
>     // Pattern to match specific domain pattern
>     private static final Pattern DOMAIN_PATTERN = Pattern.compile("^https://.*\\.example\\.com$");
>     
>     @Override
>     public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
>             throws IOException, ServletException {
>         
>         HttpServletRequest httpRequest = (HttpServletRequest) request;
>         HttpServletResponse httpResponse = (HttpServletResponse) response;
>         
>         String requestPath = httpRequest.getRequestURI();
>         
>         // Apply to advanced filter endpoints only
>         if (requestPath.startsWith("/api/advanced-filter-cors")) {
>             String origin = httpRequest.getHeader("Origin");
>             
>             if (origin != null && isOriginAllowed(origin)) {
>                 logger.info("CORS request from allowed origin: {}", origin);
>                 
>                 httpResponse.setHeader("Access-Control-Allow-Origin", origin);
>                 httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
>                 httpResponse.setHeader("Access-Control-Allow-Methods", 
>                     "GET, POST, PUT, DELETE, OPTIONS");
>                 httpResponse.setHeader("Access-Control-Allow-Headers", 
>                     "Authorization, Content-Type, X-Custom-Header");
>                 httpResponse.setHeader("Access-Control-Expose-Headers", 
>                     "X-Total-Count, X-Custom-Response-Header");
>                 httpResponse.setHeader("Access-Control-Max-Age", "7200");
>                 
>                 // Custom header to indicate CORS was handled by advanced filter
>                 httpResponse.setHeader("X-CORS-Handler", "AdvancedCorsFilter");
>                 
>                 if ("OPTIONS".equalsIgnoreCase(httpRequest.getMethod())) {
>                     logger.debug("Handling preflight request from: {}", origin);
>                     httpResponse.setStatus(HttpServletResponse.SC_OK);
>                     return;
>                 }
>             } else {
>                 logger.warn("CORS request from disallowed origin: {}", origin);
>             }
>         }
>         
>         chain.doFilter(request, response);
>     }
>     
>     /**
>      * Dynamic origin validation using patterns
>      */
>     private boolean isOriginAllowed(String origin) {
>         if (origin == null) {
>             return false;
>         }
>         
>         // Allow localhost with any port
>         if (LOCALHOST_PATTERN.matcher(origin).matches()) {
>             return true;
>         }
>         
>         // Allow subdomains of example.com
>         if (DOMAIN_PATTERN.matcher(origin).matches()) {
>             return true;
>         }
>         
>         // Add more validation logic as needed
>         return false;
>     }
> }
> ```
> 
> ```java src/main/java/com/example/cors/controller/FilterCorsController.java
> package com.example.cors.controller;
> 
> import org.springframework.http.ResponseEntity;
> import org.springframework.web.bind.annotation.*;
> 
> import java.util.HashMap;
> import java.util.Map;
> 
> /**
>  * Controller to demonstrate CORS Filter Pattern
>  * CORS is handled by CustomCorsFilter, not by annotations
>  */
> @RestController
> @RequestMapping("/api/filter-cors")
> public class FilterCorsController {
>     
>     @GetMapping("/data")
>     public ResponseEntity<Map<String, String>> getData() {
>         Map<String, String> response = new HashMap<>();
>         response.put("pattern", "CORS Filter Pattern");
>         response.put("message", "CORS handled by CustomCorsFilter");
>         response.put("note", "No @CrossOrigin annotation needed");
>         return ResponseEntity.ok(response);
>     }
>     
>     @PostMapping("/submit")
>     public ResponseEntity<Map<String, String>> submitData(@RequestBody Map<String, String> payload) {
>         Map<String, String> response = new HashMap<>();
>         response.put("status", "success");
>         response.put("pattern", "CORS Filter handles all HTTP methods");
>         response.put("received", payload.toString());
>         return ResponseEntity.ok(response);
>     }
> }
> ```
> 
> ```java src/main/java/com/example/cors/controller/AdvancedFilterCorsController.java
> package com.example.cors.controller;
> 
> import org.springframework.http.ResponseEntity;
> import org.springframework.web.bind.annotation.*;
> 
> import java.util.HashMap;
> import java.util.Map;
> 
> /**
>  * Controller demonstrating Advanced CORS Filter with pattern matching
>  */
> @RestController
> @RequestMapping("/api/advanced-filter-cors")
> public class AdvancedFilterCorsController {
>     
>     @GetMapping("/dynamic")
>     public ResponseEntity<Map<String, String>> getDynamicData() {
>         Map<String, String> response = new HashMap<>();
>         response.put("pattern", "Advanced CORS Filter Pattern");
>         response.put("message", "Dynamic origin validation using regex patterns");
>         response.put("feature", "Allows localhost:* and *.example.com");
>         return ResponseEntity.ok(response);
>     }
>     
>     @PostMapping("/validate")
>     public ResponseEntity<Map<String, String>> validateOrigin(@RequestBody Map<String, String> request) {
>         Map<String, String> response = new HashMap<>();
>         response.put("status", "validated");
>         response.put("pattern", "Advanced Filter with logging");
>         response.put("note", "Check server logs for CORS validation details");
>         return ResponseEntity.ok(response);
>     }
> }
> ```
> 
> ## 8. Security Configuration (Optional - if using Spring Security)
> 
> ```java src/main/java/com/example/cors/config/SecurityConfiguration.java
> package com.example.cors.config;
> 
> import org.springframework.context.annotation.Bean;
> import org.springframework.context.annotation.Configuration;
> import org.springframework.security.config.annotation.web.builders.HttpSecurity;
> import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
> import org.springframework.security.web.SecurityFilterChain;
> import org.springframework.web.cors.CorsConfiguration;
> import org.springframework.web.cors.CorsConfigurationSource;
> import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
> 
> import java.util.Arrays;
> 
> /**
>  * Security Configuration that works with CORS
>  * When using Spring Security, CORS must be configured here as well
>  */
> @Configuration
> @EnableWebSecurity
> public class SecurityConfiguration {
>     
>     @Bean
>     public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
>         http
>             .cors(cors -> cors.configurationSource(corsConfigurationSource()))
>             .csrf(csrf -> csrf.disable()) // Disable for demo; enable in production
>             .authorizeHttpRequests(auth -> auth
>                 .requestMatchers("/api/**").permitAll()
>                 .anyRequest().authenticated()
>             );
>         
>         return http.build();
>     }
>     
>     @Bean
>     public CorsConfigurationSource corsConfigurationSource() {
>         CorsConfiguration configuration = new CorsConfiguration();
>         configuration.setAllowedOrigins(Arrays.asList(
>             "http://localhost:3000",
>             "http://localhost:4200"
>         ));
>         configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
>         configuration.setAllowedHeaders(Arrays.asList("*"));
>         configuration.setExposedHeaders(Arrays.asList("Authorization", "X-Total-Count"));
>         configuration.setAllowCredentials(true);
>         configuration.setMaxAge(3600L);
>         
>         UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
>         source.registerCorsConfiguration("/**", configuration);
>         
>         return source;
>     }
> }
> ```
> 
> ## 9. Application Properties
> 
> ```properties src/main/resources/application.properties
> # Server Configuration
> server.port=8080
> spring.application.name=cors-patterns-demo
> 
> # Logging
> logging.level.com.example.cors=DEBUG
> logging.level.org.springframework.web.cors=DEBUG
> 
> # Spring Security (optional)
> spring.security.user.name=admin
> spring.security.user.password=admin123
> ```
> 
> ## 10. Testing Controller
> 
> ```java src/main/java/com/example/cors/controller/CorsTestingController.java
> package com.example.cors.controller;
> 
> import org.springframework.http.ResponseEntity;
> import org.springframework.web.bind.annotation.GetMapping;
> import org.springframework.web.bind.annotation.RequestMapping;
> import org.springframework.web.bind.annotation.RestController;
> 
> import java.util.HashMap;
> import java.util.Map;
> import java.util.Arrays;
> 
> /**
>  * Controller providing information about all CORS patterns
>  */
> @RestController
> @RequestMapping("/api/cors-info")
> public class CorsTestingController {
>     
>     @GetMapping("/patterns")
>     public ResponseEntity<Map<String, Object>> getAllPatterns() {
>         Map<String, Object> patterns = new HashMap<>();
>         
>         patterns.put("1. Basic CORS Pattern", Map.of(
>             "endpoint", "/api/basic-cors/data",
>             "description", "Simple @CrossOrigin with defaults",
>             "allowedOrigins", "All origins (*)"
>         ));
>         
>         patterns.put("2. Global CORS Configuration", Map.of(
>             "endpoint", "/api/global/users",
>             "description", "Configured via WebMvcConfigurer",
>             "allowedOrigins", Arrays.asList("http://localhost:3000", "http://localhost:4200"),
>             "methods", "GET, POST, PUT, DELETE"
>         ));
>         
>         patterns.put("3. Controller-level CORS", Map.of(
>             "endpoint", "/api/controller-cors/products",
>             "description", "@CrossOrigin on controller class",
>             "allowedOrigins", Arrays.asList("http://localhost:3000", "https://trusted-domain.com"),
>             "maxAge", 7200
>         ));
>         
>         patterns.put("4. Method-level CORS", Map.of(
>             "endpoints", Arrays.asList(
>                 "/api/method-cors/public-data",
>                 "/api/method-cors/restricted-data",
>                 "/api/method-cors/admin-action"
>             ),
>             "description", "Different CORS config per method",
>             "feature", "Fine-grained control"
>         ));
>         
>         patterns.put("5. CORS Filter Pattern", Map.of(
>             "endpoints", Arrays.asList(
>                 "/api/filter-cors/data",
>                 "/api/advanced-filter-cors/dynamic"
>             ),
>             "description", "Custom Filter implementation",
>             "features", Arrays.asList("Pattern matching", "Logging", "Dynamic validation")
>         ));
>         
>         return ResponseEntity.ok(patterns);
>     }
> }
> ```
> 
> ---
> 
> ## Summary
> 
> This implementation demonstrates **5 CORS patterns** in Spring Boot:
> 
> 1. **Basic CORS Pattern** - Simple `@CrossOrigin` annotation
> 2. **Global CORS Configuration** - Application-wide settings via `WebMvcConfigurer`
> 3. **Controller-level CORS** - Class-level `@CrossOrigin` with specific settings
> 4. **Method-level CORS** - Per-endpoint CORS configuration
> 5. **CORS Filter Pattern** - Custom filter implementation with advanced logic
> 
> Each pattern serves different use cases:
> - Use **Global** for consistent app-wide CORS
> - Use **Controller/Method-level** for specific endpoint requirements  
> - Use **Filter** for complex validation logic or when you need maximum control
> 
> All patterns can be tested by sending requests from different origins using tools like Postman or a frontend application.