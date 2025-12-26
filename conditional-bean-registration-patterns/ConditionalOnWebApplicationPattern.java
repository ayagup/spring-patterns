package com.example.conditional;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Conditional On Web Application Pattern
 * ======================================
 * 
 * Demonstrates @ConditionalOnWebApplication annotation that creates beans only
 * when the application is running as a web application. This differentiates
 * between web and non-web (batch, CLI, etc.) applications.
 * 
 * Key Concepts:
 * ------------
 * 1. @ConditionalOnWebApplication - Bean registration for web apps
 * 2. Application Type Detection - Servlet vs Reactive vs None
 * 3. Web-Specific Beans - Controllers, filters, interceptors
 * 4. Context Type - Determine web context type
 * 5. Web Infrastructure - Enable web-only features
 * 
 * How It Works:
 * ------------
 * - Checks if application is running as a web application
 * - Detects web application context type:
 *   * SERVLET - Traditional servlet-based (Spring MVC)
 *   * REACTIVE - Reactive web (Spring WebFlux)
 *   * ANY - Any type of web application
 * - Skips bean creation for non-web applications
 * - Evaluated at configuration processing time
 * 
 * Web Application Types:
 * ---------------------
 * - SERVLET: Spring MVC, Tomcat, Jetty, Undertow
 * - REACTIVE: Spring WebFlux, Netty, Reactor
 * - ANY: Either SERVLET or REACTIVE
 * 
 * Common Use Cases:
 * ----------------
 * - Web controllers and REST endpoints
 * - Servlet filters and interceptors
 * - Web security configuration
 * - HTTP session management
 * - Request/response handlers
 * - Web-specific error handlers
 * - CORS configuration
 * - Static resource handlers
 * 
 * Syntax:
 * ------
 * @ConditionalOnWebApplication
 * @ConditionalOnWebApplication(type = Type.SERVLET)
 * @ConditionalOnWebApplication(type = Type.REACTIVE)
 * @ConditionalOnWebApplication(type = Type.ANY)
 * 
 * @author Spring Patterns
 * @version 1.0
 */

/**
 * Example 1: General Web Configuration (ANY type)
 */
@Configuration
@ConditionalOnWebApplication
class GeneralWebConfiguration {
    
    /**
     * Create web-specific beans for any web application
     */
    @Bean
    public String webErrorController() {
        System.out.println("Creating Web Error Controller (web application detected)");
        return "Web Error Controller";
    }
    
    @Bean
    public String corsConfiguration() {
        System.out.println("Creating CORS Configuration");
        return "CORS Configuration";
    }
    
    @Bean
    public String webSecurityFilter() {
        System.out.println("Creating Web Security Filter");
        return "Web Security Filter";
    }
}

/**
 * Example 2: Servlet-Specific Configuration
 */
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
class ServletWebConfiguration {
    
    /**
     * Create beans only for servlet-based web applications
     */
    @Bean
    public String servletFilter() {
        System.out.println("Creating Servlet Filter (Servlet web app)");
        System.out.println("  Spring MVC detected");
        return "Servlet Filter";
    }
    
    @Bean
    public String handlerInterceptor() {
        System.out.println("Creating Handler Interceptor");
        return "Handler Interceptor";
    }
    
    @Bean
    public String multipartResolver() {
        System.out.println("Creating Multipart Resolver");
        return "Multipart Resolver";
    }
    
    @Bean
    public String sessionRegistry() {
        System.out.println("Creating Session Registry");
        return "Session Registry";
    }
}

/**
 * Example 3: Reactive Web Configuration
 */
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
class ReactiveWebConfiguration {
    
    /**
     * Create beans only for reactive web applications
     */
    @Bean
    public String webFilter() {
        System.out.println("Creating Web Filter (Reactive web app)");
        System.out.println("  Spring WebFlux detected");
        return "Web Filter";
    }
    
    @Bean
    public String webExceptionHandler() {
        System.out.println("Creating Web Exception Handler");
        return "Web Exception Handler";
    }
    
    @Bean
    public String codecConfigurer() {
        System.out.println("Creating Codec Configurer");
        return "Codec Configurer";
    }
    
    @Bean
    public String reactiveWebServerFactory() {
        System.out.println("Creating Reactive Web Server Factory");
        return "Reactive Web Server Factory";
    }
}

/**
 * Example 4: REST API Configuration
 */
@Configuration
@ConditionalOnWebApplication
class RestAPIConfiguration {
    
    @Bean
    public String restControllerAdvice() {
        System.out.println("Creating REST Controller Advice");
        return "REST Controller Advice";
    }
    
    @Bean
    public String jsonMessageConverter() {
        System.out.println("Creating JSON Message Converter");
        return "JSON Message Converter";
    }
    
    @Bean
    public String contentNegotiationConfigurer() {
        System.out.println("Creating Content Negotiation Configurer");
        return "Content Negotiation Configurer";
    }
}

/**
 * Example 5: Static Resource Configuration
 */
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
class StaticResourceConfiguration {
    
    @Bean
    public String resourceHandlerMapping() {
        System.out.println("Creating Resource Handler Mapping");
        System.out.println("  Serving static resources (CSS, JS, images)");
        return "Resource Handler Mapping";
    }
    
    @Bean
    public String cachingResourceResolver() {
        System.out.println("Creating Caching Resource Resolver");
        return "Caching Resource Resolver";
    }
}

/**
 * Example 6: Request Logging Configuration
 */
@Configuration
@ConditionalOnWebApplication
class RequestLoggingConfiguration {
    
    @Bean
    public String requestLoggingFilter() {
        System.out.println("Creating Request Logging Filter");
        System.out.println("  Logging all HTTP requests");
        return "Request Logging Filter";
    }
    
    @Bean
    public String performanceMonitor() {
        System.out.println("Creating Performance Monitor");
        System.out.println("  Tracking request duration");
        return "Performance Monitor";
    }
}

/**
 * Example 7: Session Management (Servlet only)
 */
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
class SessionManagementConfiguration {
    
    @Bean
    public String httpSessionListener() {
        System.out.println("Creating HTTP Session Listener");
        return "HTTP Session Listener";
    }
    
    @Bean
    public String sessionFixationProtection() {
        System.out.println("Creating Session Fixation Protection");
        return "Session Fixation Protection";
    }
    
    @Bean
    public String concurrentSessionControl() {
        System.out.println("Creating Concurrent Session Control");
        return "Concurrent Session Control";
    }
}

/**
 * Example 8: Error Handling Configuration
 */
@Configuration
@ConditionalOnWebApplication
class ErrorHandlingConfiguration {
    
    @Bean
    public String globalExceptionHandler() {
        System.out.println("Creating Global Exception Handler");
        return "Global Exception Handler";
    }
    
    @Bean
    public String customErrorAttributes() {
        System.out.println("Creating Custom Error Attributes");
        return "Custom Error Attributes";
    }
}

/**
 * Example 9: WebSocket Configuration
 */
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
class WebSocketConfiguration {
    
    @Bean
    public String webSocketHandler() {
        System.out.println("Creating WebSocket Handler");
        return "WebSocket Handler";
    }
    
    @Bean
    public String stompEndpointRegistry() {
        System.out.println("Creating STOMP Endpoint Registry");
        return "STOMP Endpoint Registry";
    }
}

/**
 * Example 10: Health Check Endpoints
 */
@Configuration
@ConditionalOnWebApplication
class WebHealthCheckConfiguration {
    
    @Bean
    public String healthEndpoint() {
        System.out.println("Creating Health Endpoint");
        System.out.println("  Exposing /actuator/health");
        return "Health Endpoint";
    }
    
    @Bean
    public String readinessProbe() {
        System.out.println("Creating Readiness Probe");
        return "Readiness Probe";
    }
    
    @Bean
    public String livenessProbe() {
        System.out.println("Creating Liveness Probe");
        return "Liveness Probe";
    }
}

/**
 * Main Pattern Class
 */
@Configuration
public class ConditionalOnWebApplicationPattern {
    
    /**
     * Example: General web application bean
     */
    @Bean
    @ConditionalOnWebApplication
    public String webApplicationService() {
        System.out.println("Creating Web Application Service");
        System.out.println("  Application Type: WEB");
        System.out.println("  Skipped for batch/CLI applications");
        return "Web Application Service";
    }
    
    /**
     * Example: Servlet-specific bean
     */
    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    public String servletSpecificService() {
        System.out.println("Creating Servlet-Specific Service");
        System.out.println("  Spring MVC / Servlet Container");
        return "Servlet-Specific Service";
    }
    
    /**
     * Example: Reactive-specific bean
     */
    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
    public String reactiveSpecificService() {
        System.out.println("Creating Reactive-Specific Service");
        System.out.println("  Spring WebFlux / Reactive Runtime");
        return "Reactive-Specific Service";
    }
}

/**
 * Usage Examples and Best Practices
 */
class ConditionalOnWebApplicationUsageExamples {
    
    public static void main(String[] args) {
        System.out.println("Conditional On Web Application Pattern");
        System.out.println("========================================\n");
        
        System.out.println("Purpose:");
        System.out.println("- Create beans only for web applications");
        System.out.println("- Differentiate between servlet and reactive");
        System.out.println("- Skip web beans for batch/CLI apps\n");
        
        System.out.println("Web Application Types:");
        System.out.println("1. SERVLET - Spring MVC, servlet-based");
        System.out.println("2. REACTIVE - Spring WebFlux, reactive");
        System.out.println("3. ANY - Either SERVLET or REACTIVE\n");
        
        System.out.println("Syntax:");
        System.out.println("@ConditionalOnWebApplication  // ANY type");
        System.out.println("@ConditionalOnWebApplication(type = Type.SERVLET)");
        System.out.println("@ConditionalOnWebApplication(type = Type.REACTIVE)\n");
        
        System.out.println("Common Use Cases:");
        System.out.println("1. Web controllers and REST APIs");
        System.out.println("2. Servlet filters and interceptors");
        System.out.println("3. Web security configuration");
        System.out.println("4. HTTP session management");
        System.out.println("5. CORS configuration");
        System.out.println("6. Static resource handling");
        System.out.println("7. Error handlers");
        System.out.println("8. WebSocket configuration");
        System.out.println("9. Health check endpoints");
        System.out.println("10. Request logging\n");
        
        System.out.println("Application Types:");
        System.out.println("WEB (create beans):");
        System.out.println("  - Spring MVC application");
        System.out.println("  - Spring WebFlux application");
        System.out.println("  - REST API service");
        System.out.println("\nNON-WEB (skip beans):");
        System.out.println("  - Spring Batch application");
        System.out.println("  - Command-line application");
        System.out.println("  - Scheduled job application");
        System.out.println("  - Message consumer (Kafka, RabbitMQ)\n");
        
        System.out.println("Best Practices:");
        System.out.println("- Use for web-only infrastructure beans");
        System.out.println("- Specify type (SERVLET/REACTIVE) when needed");
        System.out.println("- Use ANY for general web features");
        System.out.println("- Avoid for business logic beans");
        System.out.println("- Combine with other conditionals if needed");
        System.out.println("- Test with both web and non-web contexts\n");
        
        System.out.println("Example Patterns:");
        System.out.println("// Any web application");
        System.out.println("@ConditionalOnWebApplication");
        System.out.println("public WebSecurityConfig security() {...}\n");
        
        System.out.println("// Servlet-specific");
        System.out.println("@ConditionalOnWebApplication(type = SERVLET)");
        System.out.println("public ServletFilter filter() {...}\n");
        
        System.out.println("// Reactive-specific");
        System.out.println("@ConditionalOnWebApplication(type = REACTIVE)");
        System.out.println("public WebFilter webFilter() {...}");
    }
}
