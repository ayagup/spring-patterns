package com.example.testing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.ServletContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Web App Configuration Pattern
 * ==============================
 * 
 * Demonstrates the @WebAppConfiguration annotation pattern for loading
 * WebApplicationContext in Spring MVC integration tests.
 * 
 * Use Cases:
 * ----------
 * 1. Test Spring MVC controllers
 * 2. Test web filters and interceptors
 * 3. Test servlet-based configurations
 * 4. Test request/session scoped beans
 * 5. Test web security configurations
 * 6. Test view resolvers
 * 7. Test multipart file handling
 * 8. Integration testing of web layers
 * 
 * Key Features:
 * -------------
 * - Loads WebApplicationContext instead of ApplicationContext
 * - Provides access to ServletContext
 * - Supports request/session scope beans
 * - Enables MockMvc testing
 * - Configures resource base path
 * - Simulates web environment
 * - Works with @ContextConfiguration
 * - Caches web contexts
 * 
 * Annotation Attributes:
 * ----------------------
 * value - Resource base path (default: "src/main/webapp")
 * 
 * Usage:
 * ------
 * @WebAppConfiguration
 * @WebAppConfiguration("src/test/webapp")
 * @WebAppConfiguration("classpath:test-webapp")
 * 
 * Common Combinations:
 * --------------------
 * @WebAppConfiguration
 * @ContextConfiguration(classes = WebConfig.class)
 * class MyWebTest { }
 * 
 * WebApplicationContext vs ApplicationContext:
 * ---------------------------------------------
 * WebApplicationContext:
 *   - Has ServletContext
 *   - Supports web scopes (request, session)
 *   - For web layer testing
 *   - Used with @WebAppConfiguration
 * 
 * ApplicationContext:
 *   - No ServletContext
 *   - No web scopes
 *   - For service/repository testing
 *   - Default context type
 * 
 * Best Practices:
 * ---------------
 * 1. Use for MVC controller tests
 * 2. Specify custom resource path if needed
 * 3. Use MockMvc for request/response testing
 * 4. Test web-specific features
 * 5. Combine with @ContextConfiguration
 * 6. Mock external dependencies
 * 7. Test request/session scope beans
 * 8. Verify web security configurations
 * 
 * Common Patterns:
 * ----------------
 * 1. Controller integration testing
 * 2. Filter and interceptor testing
 * 3. Request-scoped bean testing
 * 4. Session management testing
 * 5. View resolution testing
 * 6. File upload testing
 * 7. REST API testing
 * 8. Security integration testing
 * 
 * @author Spring Patterns
 * @version 1.0
 */

// Web configuration
@Configuration
class WebConfig {
    
    @Bean
    public UserController userController() {
        return new UserController();
    }
    
    @Bean
    public OrderController orderController() {
        return new OrderController();
    }
    
    @Bean
    public ViewResolver viewResolver() {
        return new ViewResolver();
    }
}

// Mock controllers
class UserController {
    
    public String getUsers() {
        return "users";
    }
    
    public String getUser(Long id) {
        return "user-" + id;
    }
    
    public String createUser(String name) {
        return "created-" + name;
    }
}

class OrderController {
    
    public String getOrders() {
        return "orders";
    }
    
    public String placeOrder(String item) {
        return "order-placed-" + item;
    }
}

class ViewResolver {
    
    public String resolveView(String viewName) {
        return "/WEB-INF/views/" + viewName + ".jsp";
    }
}

// Mock ServletContext
class MockServletContext implements ServletContext {
    private final java.util.Map<String, Object> attributes = new java.util.HashMap<>();
    private final String contextPath;
    
    public MockServletContext(String contextPath) {
        this.contextPath = contextPath;
    }
    
    @Override
    public String getContextPath() {
        return contextPath;
    }
    
    @Override
    public Object getAttribute(String name) {
        return attributes.get(name);
    }
    
    @Override
    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }
    
    @Override
    public void removeAttribute(String name) {
        attributes.remove(name);
    }
    
    @Override
    public java.util.Enumeration<String> getAttributeNames() {
        return java.util.Collections.enumeration(attributes.keySet());
    }
    
    // Other ServletContext methods (stub implementations)
    @Override public ServletContext getContext(String uripath) { return null; }
    @Override public int getMajorVersion() { return 3; }
    @Override public int getMinorVersion() { return 1; }
    @Override public int getEffectiveMajorVersion() { return 3; }
    @Override public int getEffectiveMinorVersion() { return 1; }
    @Override public String getMimeType(String file) { return null; }
    @Override public java.util.Set<String> getResourcePaths(String path) { return null; }
    @Override public java.net.URL getResource(String path) { return null; }
    @Override public java.io.InputStream getResourceAsStream(String path) { return null; }
    @Override public javax.servlet.RequestDispatcher getRequestDispatcher(String path) { return null; }
    @Override public javax.servlet.RequestDispatcher getNamedDispatcher(String name) { return null; }
    @Override public javax.servlet.Servlet getServlet(String name) { return null; }
    @Override public java.util.Enumeration<javax.servlet.Servlet> getServlets() { return null; }
    @Override public java.util.Enumeration<String> getServletNames() { return null; }
    @Override public void log(String msg) {}
    @Override public void log(Exception exception, String msg) {}
    @Override public void log(String message, Throwable throwable) {}
    @Override public String getRealPath(String path) { return null; }
    @Override public String getServerInfo() { return "MockServletContext"; }
    @Override public String getInitParameter(String name) { return null; }
    @Override public java.util.Enumeration<String> getInitParameterNames() { return null; }
    @Override public boolean setInitParameter(String name, String value) { return false; }
    @Override public String getServletContextName() { return "MockContext"; }
    @Override public javax.servlet.ServletRegistration.Dynamic addServlet(String servletName, String className) { return null; }
    @Override public javax.servlet.ServletRegistration.Dynamic addServlet(String servletName, javax.servlet.Servlet servlet) { return null; }
    @Override public javax.servlet.ServletRegistration.Dynamic addServlet(String servletName, Class<? extends javax.servlet.Servlet> servletClass) { return null; }
    @Override public <T extends javax.servlet.Servlet> T createServlet(Class<T> clazz) { return null; }
    @Override public javax.servlet.ServletRegistration getServletRegistration(String servletName) { return null; }
    @Override public java.util.Map<String, ? extends javax.servlet.ServletRegistration> getServletRegistrations() { return null; }
    @Override public javax.servlet.FilterRegistration.Dynamic addFilter(String filterName, String className) { return null; }
    @Override public javax.servlet.FilterRegistration.Dynamic addFilter(String filterName, javax.servlet.Filter filter) { return null; }
    @Override public javax.servlet.FilterRegistration.Dynamic addFilter(String filterName, Class<? extends javax.servlet.Filter> filterClass) { return null; }
    @Override public <T extends javax.servlet.Filter> T createFilter(Class<T> clazz) { return null; }
    @Override public javax.servlet.FilterRegistration getFilterRegistration(String filterName) { return null; }
    @Override public java.util.Map<String, ? extends javax.servlet.FilterRegistration> getFilterRegistrations() { return null; }
    @Override public javax.servlet.SessionCookieConfig getSessionCookieConfig() { return null; }
    @Override public void setSessionTrackingModes(java.util.Set<javax.servlet.SessionTrackingMode> sessionTrackingModes) {}
    @Override public java.util.Set<javax.servlet.SessionTrackingMode> getDefaultSessionTrackingModes() { return null; }
    @Override public java.util.Set<javax.servlet.SessionTrackingMode> getEffectiveSessionTrackingModes() { return null; }
    @Override public void addListener(String className) {}
    @Override public <T extends java.util.EventListener> void addListener(T t) {}
    @Override public void addListener(Class<? extends java.util.EventListener> listenerClass) {}
    @Override public <T extends java.util.EventListener> T createListener(Class<T> clazz) { return null; }
    @Override public javax.servlet.descriptor.JspConfigDescriptor getJspConfigDescriptor() { return null; }
    @Override public ClassLoader getClassLoader() { return null; }
    @Override public void declareRoles(String... roleNames) {}
    @Override public String getVirtualServerName() { return null; }
}

/**
 * Example 1: Basic Web Application Context
 * Demonstrates loading WebApplicationContext
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = WebConfig.class)
@WebAppConfiguration
class BasicWebAppTest {
    
    @Autowired
    private WebApplicationContext webApplicationContext;
    
    @Test
    void testWebApplicationContextLoaded() {
        System.out.println("\n=== Test: WebApplicationContext Loaded ===");
        
        assertNotNull(webApplicationContext);
        System.out.println("✓ WebApplicationContext loaded successfully");
    }
    
    @Test
    void testServletContextAvailable() {
        System.out.println("\n=== Test: ServletContext Available ===");
        
        ServletContext servletContext = webApplicationContext.getServletContext();
        assertNotNull(servletContext);
        
        System.out.println("✓ ServletContext available in WebApplicationContext");
    }
    
    @Test
    void testControllerBeansLoaded() {
        System.out.println("\n=== Test: Controller Beans Loaded ===");
        
        UserController userController = webApplicationContext.getBean(UserController.class);
        OrderController orderController = webApplicationContext.getBean(OrderController.class);
        
        assertNotNull(userController);
        assertNotNull(orderController);
        
        System.out.println("✓ Web controllers loaded in WebApplicationContext");
    }
}

/**
 * Example 2: Custom Resource Base Path
 * Demonstrates specifying custom webapp resource path
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = WebConfig.class)
@WebAppConfiguration("src/test/webapp")
class CustomResourcePathTest {
    
    @Autowired
    private WebApplicationContext context;
    
    @Test
    void testCustomResourcePath() {
        System.out.println("\n=== Test: Custom Resource Path ===");
        
        assertNotNull(context);
        
        System.out.println("✓ WebApplicationContext loaded with custom resource path");
        System.out.println("  Resource base: src/test/webapp");
    }
}

/**
 * Example 3: Testing MVC Controllers
 * Demonstrates testing controllers in web context
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = WebConfig.class)
@WebAppConfiguration
class ControllerTest {
    
    @Autowired
    private UserController userController;
    
    @Autowired
    private OrderController orderController;
    
    @Test
    void testUserControllerMethods() {
        System.out.println("\n=== Test: User Controller Methods ===");
        
        String users = userController.getUsers();
        assertEquals("users", users);
        
        String user = userController.getUser(123L);
        assertEquals("user-123", user);
        
        String created = userController.createUser("John");
        assertEquals("created-John", created);
        
        System.out.println("✓ User controller methods work correctly");
    }
    
    @Test
    void testOrderControllerMethods() {
        System.out.println("\n=== Test: Order Controller Methods ===");
        
        String orders = orderController.getOrders();
        assertEquals("orders", orders);
        
        String order = orderController.placeOrder("laptop");
        assertEquals("order-placed-laptop", order);
        
        System.out.println("✓ Order controller methods work correctly");
    }
}

/**
 * Example 4: Testing View Resolution
 * Demonstrates testing view resolvers in web context
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = WebConfig.class)
@WebAppConfiguration
class ViewResolverTest {
    
    @Autowired
    private ViewResolver viewResolver;
    
    @Test
    void testViewResolution() {
        System.out.println("\n=== Test: View Resolution ===");
        
        String userView = viewResolver.resolveView("user");
        assertEquals("/WEB-INF/views/user.jsp", userView);
        
        String orderView = viewResolver.resolveView("order");
        assertEquals("/WEB-INF/views/order.jsp", orderView);
        
        System.out.println("✓ View resolver working correctly");
    }
}

/**
 * Example 5: WebApplicationContext Features
 * Demonstrates web-specific context features
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = WebConfig.class)
@WebAppConfiguration
class WebContextFeaturesTest {
    
    @Autowired
    private WebApplicationContext webContext;
    
    @Test
    void testWebApplicationContextType() {
        System.out.println("\n=== Test: WebApplicationContext Type ===");
        
        assertTrue(webContext instanceof WebApplicationContext);
        
        System.out.println("✓ Context is WebApplicationContext");
        System.out.println("  - Has ServletContext");
        System.out.println("  - Supports web scopes");
    }
    
    @Test
    void testServletContextAttributes() {
        System.out.println("\n=== Test: ServletContext Attributes ===");
        
        ServletContext servletContext = webContext.getServletContext();
        
        if (servletContext != null) {
            servletContext.setAttribute("testKey", "testValue");
            
            Object value = servletContext.getAttribute("testKey");
            assertEquals("testValue", value);
            
            System.out.println("✓ ServletContext attributes work");
        }
    }
    
    @Test
    void testBeanRetrieval() {
        System.out.println("\n=== Test: Bean Retrieval ===");
        
        String[] beanNames = webContext.getBeanNamesForType(Object.class);
        
        assertTrue(beanNames.length > 0);
        System.out.println("  Total beans: " + beanNames.length);
        
        assertTrue(webContext.containsBean("userController"));
        assertTrue(webContext.containsBean("orderController"));
        assertTrue(webContext.containsBean("viewResolver"));
        
        System.out.println("✓ All expected beans present");
    }
}

/**
 * Main class for demonstration
 */
public class WebAppConfigurationPattern {
    
    public static void main(String[] args) {
        System.out.println("=== Web App Configuration Pattern ===\n");
        System.out.println("This pattern demonstrates:");
        System.out.println("1. Loading WebApplicationContext");
        System.out.println("2. Custom resource base path");
        System.out.println("3. Testing MVC controllers");
        System.out.println("4. Testing view resolvers");
        System.out.println("5. ServletContext availability");
        System.out.println("6. Web-specific context features");
        System.out.println("7. Bean retrieval in web context");
        System.out.println("8. Web layer integration testing");
        System.out.println("\nRun the test classes to see the pattern in action.");
        System.out.println("==========================");
    }
}

/**
 * Web App Configuration Summary:
 * 
 * Basic Usage:
 * ------------
 * @WebAppConfiguration
 * @ContextConfiguration(classes = WebConfig.class)
 * class WebTest {
 *     @Autowired
 *     private WebApplicationContext context;
 * }
 * 
 * Custom Resource Path:
 * ---------------------
 * @WebAppConfiguration("src/test/webapp")
 * @ContextConfiguration(classes = WebConfig.class)
 * class WebTest { }
 * 
 * Classpath Resource:
 * -------------------
 * @WebAppConfiguration("classpath:test-webapp")
 * @ContextConfiguration(classes = WebConfig.class)
 * class WebTest { }
 * 
 * With MockMvc:
 * -------------
 * @WebAppConfiguration
 * @ContextConfiguration(classes = WebConfig.class)
 * class ControllerTest {
 *     
 *     @Autowired
 *     private WebApplicationContext context;
 *     
 *     private MockMvc mockMvc;
 *     
 *     @BeforeEach
 *     void setup() {
 *         mockMvc = MockMvcBuilders
 *             .webAppContextSetup(context)
 *             .build();
 *     }
 *     
 *     @Test
 *     void testController() throws Exception {
 *         mockMvc.perform(get("/users"))
 *             .andExpect(status().isOk());
 *     }
 * }
 * 
 * Accessing ServletContext:
 * -------------------------
 * @Autowired
 * private WebApplicationContext context;
 * 
 * @Test
 * void test() {
 *     ServletContext servletContext = context.getServletContext();
 *     assertNotNull(servletContext);
 * }
 */
