package com.example.contexthierarchy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.ServletContextAware;

import javax.servlet.ServletContext;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * ServletContext Pattern
 * 
 * Demonstrates accessing and using ServletContext in Spring applications,
 * including servlet initialization parameters, attributes, and context path.
 * 
 * Key Concepts:
 * - ServletContext access
 * - ServletContextAware interface
 * - Servlet attributes
 * - Initialization parameters
 * - Context path
 * 
 * Use Cases:
 * - Web resource access
 * - Servlet configuration
 * - Shared servlet data
 * - Web application info
 * - File upload paths
 */
@SpringBootApplication
public class ServletContextPattern {

    public static void main(String[] args) {
        SpringApplication.run(ServletContextPattern.class, args);
    }
}

/**
 * Service implementing ServletContextAware
 */
@Service
class ServletContextService implements ServletContextAware {

    private ServletContext servletContext;

    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
        System.out.println("ServletContext injected: " + servletContext.getServletContextName());
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
        }
        
        return info;
    }

    /**
     * Get servlet initialization parameters
     */
    public Map<String, String> getInitParameters() {
        Map<String, String> params = new HashMap<>();
        
        if (servletContext != null) {
            Enumeration<String> paramNames = servletContext.getInitParameterNames();
            while (paramNames.hasMoreElements()) {
                String name = paramNames.nextElement();
                params.put(name, servletContext.getInitParameter(name));
            }
        }
        
        return params;
    }

    /**
     * Get servlet context attributes
     */
    public Map<String, Object> getAttributes() {
        Map<String, Object> attrs = new HashMap<>();
        
        if (servletContext != null) {
            Enumeration<String> attrNames = servletContext.getAttributeNames();
            while (attrNames.hasMoreElements()) {
                String name = attrNames.nextElement();
                attrs.put(name, servletContext.getAttribute(name).toString());
            }
        }
        
        return attrs;
    }

    /**
     * Set servlet context attribute
     */
    public void setAttribute(String name, Object value) {
        if (servletContext != null) {
            servletContext.setAttribute(name, value);
        }
    }

    /**
     * Get real path for a resource
     */
    public String getRealPath(String path) {
        if (servletContext != null) {
            return servletContext.getRealPath(path);
        }
        return null;
    }
}

/**
 * Controller exposing servlet context operations
 */
@RestController
class ServletContextController {

    private final ServletContextService servletContextService;
    private final ApplicationContext applicationContext;

    public ServletContextController(ServletContextService servletContextService,
                                   ApplicationContext applicationContext) {
        this.servletContextService = servletContextService;
        this.applicationContext = applicationContext;
    }

    @GetMapping("/servlet-context/info")
    public Map<String, Object> getServletContextInfo() {
        return servletContextService.getServletContextInfo();
    }

    @GetMapping("/servlet-context/init-params")
    public Map<String, String> getInitParameters() {
        return servletContextService.getInitParameters();
    }

    @GetMapping("/servlet-context/attributes")
    public Map<String, Object> getAttributes() {
        return servletContextService.getAttributes();
    }

    @GetMapping("/servlet-context/real-path")
    public Map<String, String> getRealPath() {
        return Map.of(
                "/", servletContextService.getRealPath("/"),
                "/uploads", servletContextService.getRealPath("/uploads"),
                "/WEB-INF", servletContextService.getRealPath("/WEB-INF")
        );
    }
}

/**
 * Documentation:
 * 
 * ServletContext Access Methods:
 * 
 * 1. ServletContextAware:
 *    @Service
 *    class MyService implements ServletContextAware {
 *        private ServletContext servletContext;
 *        
 *        @Override
 *        public void setServletContext(ServletContext ctx) {
 *            this.servletContext = ctx;
 *        }
 *    }
 * 
 * 2. @Autowired (Spring 4.3+):
 *    @Service
 *    class MyService {
 *        @Autowired
 *        private ServletContext servletContext;
 *    }
 * 
 * 3. WebApplicationContext:
 *    WebApplicationContext wac = ...;
 *    ServletContext ctx = wac.getServletContext();
 * 
 * 4. HttpServletRequest:
 *    @GetMapping("/")
 *    public void handle(HttpServletRequest request) {
 *        ServletContext ctx = request.getServletContext();
 *    }
 * 
 * ServletContext Methods:
 * 
 * Configuration:
 * - getInitParameter(name) - Get init param
 * - getInitParameterNames() - Get all init params
 * - getContextPath() - Get context path
 * - getServerInfo() - Get server info
 * 
 * Attributes:
 * - setAttribute(name, value) - Set attribute
 * - getAttribute(name) - Get attribute
 * - removeAttribute(name) - Remove attribute
 * - getAttributeNames() - Get all attributes
 * 
 * Resources:
 * - getRealPath(path) - Get filesystem path
 * - getResource(path) - Get resource URL
 * - getResourceAsStream(path) - Get resource stream
 * - getResourcePaths(path) - List resources
 * 
 * Servlet Registration:
 * - addServlet(name, servlet) - Add servlet
 * - getServletRegistration(name) - Get registration
 * - getServletRegistrations() - Get all registrations
 * 
 * Filter Registration:
 * - addFilter(name, filter) - Add filter
 * - getFilterRegistration(name) - Get registration
 * - getFilterRegistrations() - Get all registrations
 * 
 * Initialization Parameters (web.xml):
 * <web-app>
 *     <context-param>
 *         <param-name>configLocation</param-name>
 *         <param-value>/WEB-INF/config.xml</param-value>
 *     </context-param>
 * </web-app>
 * 
 * Spring Boot:
 * - Embedded servlet container
 * - ServletContextInitializer for config
 * - @WebServlet, @WebFilter auto-registered
 * 
 * Use Cases:
 * 1. File Upload:
 *    String uploadDir = servletContext.getRealPath("/uploads");
 * 
 * 2. Shared Data:
 *    servletContext.setAttribute("appConfig", config);
 * 
 * 3. Resource Access:
 *    InputStream is = servletContext.getResourceAsStream("/WEB-INF/data.xml");
 * 
 * 4. Dynamic Registration:
 *    ServletRegistration.Dynamic servlet = 
 *        servletContext.addServlet("myServlet", MyServlet.class);
 *    servlet.addMapping("/my/*");
 * 
 * Best Practices:
 * - Use ServletContextAware for cleaner code
 * - Store application-wide data as attributes
 * - Use getRealPath() for file operations
 * - Clean up attributes on shutdown
 * - Don't store request-specific data
 */
