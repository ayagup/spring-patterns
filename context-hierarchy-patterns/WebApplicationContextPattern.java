package com.example.contexthierarchy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContext;
import java.util.HashMap;
import java.util.Map;

/**
 * Web Application Context Pattern
 * 
 * Demonstrates WebApplicationContext which extends ApplicationContext
 * with web-specific functionality like servlet context access.
 * 
 * Key Concepts:
 * - WebApplicationContext interface
 * - ServletContext integration
 * - Root and servlet contexts
 * - Web-aware bean scopes
 * - Web application lifecycle
 * 
 * Use Cases:
 * - Web applications
 * - Servlet-based apps
 * - Multi-servlet configurations
 * - Web resource access
 * - Request/session scopes
 */
@SpringBootApplication
public class WebApplicationContextPattern {

    public static void main(String[] args) {
        SpringApplication.run(WebApplicationContextPattern.class, args);
    }
}

/**
 * Service demonstrating WebApplicationContext usage
 */
@Component
class WebContextService implements org.springframework.web.context.ServletContextAware {

    private ServletContext servletContext;
    private final WebApplicationContext webApplicationContext;

    public WebContextService(ApplicationContext applicationContext) {
        // ApplicationContext in web app is actually WebApplicationContext
        if (applicationContext instanceof WebApplicationContext) {
            this.webApplicationContext = (WebApplicationContext) applicationContext;
        } else {
            this.webApplicationContext = null;
        }
    }

    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
        System.out.println("ServletContext injected via ServletContextAware");
    }

    /**
     * Get web application context information
     */
    public Map<String, Object> getWebContextInfo() {
        Map<String, Object> info = new HashMap<>();

        if (webApplicationContext != null) {
            info.put("isWebContext", true);
            info.put("contextPath", webApplicationContext.getServletContext().getContextPath());
            info.put("serverInfo", webApplicationContext.getServletContext().getServerInfo());
            info.put("servletContextName", webApplicationContext.getServletContext().getServletContextName());
            info.put("namespace", webApplicationContext.getNamespace());
        } else {
            info.put("isWebContext", false);
        }

        return info;
    }

    /**
     * Get servlet context information
     */
    public Map<String, Object> getServletContextInfo() {
        Map<String, Object> info = new HashMap<>();

        if (servletContext != null) {
            info.put("contextPath", servletContext.getContextPath());
            info.put("serverInfo", servletContext.getServerInfo());
            info.put("servletContextName", servletContext.getServletContextName());
            info.put("majorVersion", servletContext.getMajorVersion());
            info.put("minorVersion", servletContext.getMinorVersion());
            info.put("effectiveMajorVersion", servletContext.getEffectiveMajorVersion());
            info.put("effectiveMinorVersion", servletContext.getEffectiveMinorVersion());
        } else {
            info.put("available", false);
        }

        return info;
    }

    /**
     * Access ServletContext from WebApplicationContext
     */
    public String getResourcePath(String resource) {
        if (webApplicationContext != null) {
            return webApplicationContext.getServletContext().getRealPath(resource);
        }
        return null;
    }
}

/**
 * Request-scoped bean (only available in web context)
 */
@Component
@org.springframework.web.context.annotation.RequestScope
class RequestScopedBean {
    
    private final long creationTime = System.currentTimeMillis();
    private String requestData = "Request data";

    public long getCreationTime() {
        return creationTime;
    }

    public String getRequestData() {
        return requestData;
    }

    public void setRequestData(String requestData) {
        this.requestData = requestData;
    }
}

/**
 * Session-scoped bean (only available in web context)
 */
@Component
@org.springframework.web.context.annotation.SessionScope
class SessionScopedBean {
    
    private final long creationTime = System.currentTimeMillis();
    private int requestCount = 0;

    public long getCreationTime() {
        return creationTime;
    }

    public int getRequestCount() {
        return requestCount;
    }

    public void incrementRequestCount() {
        requestCount++;
    }
}

/**
 * Application-scoped bean (web application scope)
 */
@Component
@org.springframework.web.context.annotation.ApplicationScope
class ApplicationScopedBean {
    
    private final long creationTime = System.currentTimeMillis();
    private int totalRequests = 0;

    public long getCreationTime() {
        return creationTime;
    }

    public int getTotalRequests() {
        return totalRequests;
    }

    public void incrementTotalRequests() {
        totalRequests++;
    }
}

/**
 * Controller demonstrating web context features
 */
@RestController
class WebContextController {

    private final WebContextService webContextService;
    private final RequestScopedBean requestScopedBean;
    private final SessionScopedBean sessionScopedBean;
    private final ApplicationScopedBean applicationScopedBean;

    public WebContextController(WebContextService webContextService,
                               RequestScopedBean requestScopedBean,
                               SessionScopedBean sessionScopedBean,
                               ApplicationScopedBean applicationScopedBean) {
        this.webContextService = webContextService;
        this.requestScopedBean = requestScopedBean;
        this.sessionScopedBean = sessionScopedBean;
        this.applicationScopedBean = applicationScopedBean;
    }

    @GetMapping("/web-context/info")
    public Map<String, Object> getWebContextInfo() {
        applicationScopedBean.incrementTotalRequests();
        sessionScopedBean.incrementRequestCount();
        
        return webContextService.getWebContextInfo();
    }

    @GetMapping("/web-context/servlet-info")
    public Map<String, Object> getServletContextInfo() {
        return webContextService.getServletContextInfo();
    }

    @GetMapping("/web-context/scoped-beans")
    public Map<String, Object> getScopedBeansInfo() {
        return Map.of(
                "requestScoped", Map.of(
                        "creationTime", requestScopedBean.getCreationTime(),
                        "data", requestScopedBean.getRequestData()
                ),
                "sessionScoped", Map.of(
                        "creationTime", sessionScopedBean.getCreationTime(),
                        "requestCount", sessionScopedBean.getRequestCount()
                ),
                "applicationScoped", Map.of(
                        "creationTime", applicationScopedBean.getCreationTime(),
                        "totalRequests", applicationScopedBean.getTotalRequests()
                )
        );
    }
}

/**
 * Documentation:
 * 
 * WebApplicationContext:
 * - Extends ApplicationContext
 * - Adds web-specific functionality
 * - Provides ServletContext access
 * - Supports web scopes (request, session, application)
 * - Lifecycle tied to web application
 * 
 * Hierarchy in Web Apps:
 * 
 * ServletContext (Container)
 *   └── Root WebApplicationContext (Parent)
 *       └── Servlet WebApplicationContext (Child)
 * 
 * Root Context:
 * - Loaded by ContextLoaderListener
 * - Contains business services
 * - Shared across servlets
 * - Parent of servlet contexts
 * 
 * Servlet Context:
 * - One per DispatcherServlet
 * - Contains web components (controllers, etc.)
 * - Child of root context
 * - Can access root beans
 * 
 * Configuration:
 * 
 * web.xml (Traditional):
 * <listener>
 *   <listener-class>
 *     org.springframework.web.context.ContextLoaderListener
 *   </listener-class>
 * </listener>
 * 
 * <context-param>
 *   <param-name>contextConfigLocation</param-name>
 *   <param-value>/WEB-INF/root-context.xml</param-value>
 * </context-param>
 * 
 * <servlet>
 *   <servlet-name>dispatcher</servlet-name>
 *   <servlet-class>
 *     org.springframework.web.servlet.DispatcherServlet
 *   </servlet-class>
 *   <init-param>
 *     <param-name>contextConfigLocation</param-name>
 *     <param-value>/WEB-INF/servlet-context.xml</param-value>
 *   </init-param>
 * </servlet>
 * 
 * Spring Boot:
 * - Single context by default
 * - Automatically WebApplicationContext
 * - No web.xml needed
 * - Embedded servlet container
 * 
 * Accessing WebApplicationContext:
 * 
 * 1. Injection:
 *    @Autowired
 *    WebApplicationContext webContext;
 * 
 * 2. From ServletContext:
 *    WebApplicationContext context = 
 *        WebApplicationContextUtils.getWebApplicationContext(servletContext);
 * 
 * 3. From Request:
 *    WebApplicationContext context = 
 *        RequestContextUtils.findWebApplicationContext(request);
 * 
 * Web Scopes:
 * 
 * 1. Request Scope:
 *    @RequestScope
 *    - New instance per HTTP request
 *    - Destroyed after request completes
 * 
 * 2. Session Scope:
 *    @SessionScope
 *    - New instance per HTTP session
 *    - Shared across requests in same session
 * 
 * 3. Application Scope:
 *    @ApplicationScope
 *    - Singleton per ServletContext
 *    - Shared across all sessions
 * 
 * 4. WebSocket Scope:
 *    @Scope("websocket")
 *    - Per WebSocket session
 * 
 * ServletContext Access:
 * 
 * 1. Via WebApplicationContext:
 *    ServletContext sc = webContext.getServletContext();
 * 
 * 2. Via ServletContextAware:
 *    class MyBean implements ServletContextAware {
 *        @Override
 *        public void setServletContext(ServletContext sc) {
 *            // Use ServletContext
 *        }
 *    }
 * 
 * 3. Via @Autowired:
 *    @Autowired
 *    ServletContext servletContext;
 * 
 * Lifecycle:
 * 1. ServletContext created by container
 * 2. ContextLoaderListener creates root context
 * 3. DispatcherServlet creates servlet context
 * 4. Application runs
 * 5. Servlet contexts closed first
 * 6. Root context closed
 * 7. ServletContext destroyed
 * 
 * Use Cases:
 * - Traditional Spring MVC apps
 * - Multi-servlet applications
 * - WAR deployments
 * - Accessing servlet resources
 * - Web-specific configuration
 * 
 * Best Practices:
 * - Separate business and web layers
 * - Put services in root context
 * - Put controllers in servlet context
 * - Use proper scopes for beans
 * - Clean separation of concerns
 * - Document context structure
 * 
 * Common Issues:
 * - Bean visibility between contexts
 * - Scope proxy requirements
 * - Context initialization order
 * - Resource loading paths
 * - Multiple servlet conflicts
 * 
 * Testing:
 * - Use @WebAppConfiguration
 * - MockServletContext available
 * - WebApplicationContext in tests
 * - Request/session scope testing
 * 
 * Spring Boot Differences:
 * - Single embedded context
 * - No separate root/servlet split
 * - Simpler configuration
 * - Auto-configuration
 * - Embedded container
 */
