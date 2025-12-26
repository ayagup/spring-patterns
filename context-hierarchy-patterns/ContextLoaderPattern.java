package com.example.contexthierarchy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * ContextLoader Pattern
 * 
 * Demonstrates various ways to load and initialize application contexts,
 * including XML, Java config, programmatic loading, and web context loading.
 * 
 * Key Concepts:
 * - Context loading strategies
 * - ClassPathXmlApplicationContext
 * - AnnotationConfigApplicationContext
 * - GenericApplicationContext
 * - ContextLoaderListener
 * 
 * Use Cases:
 * - Application initialization
 * - Multiple context loading
 * - Programmatic context creation
 * - Web context setup
 * - Test context loading
 */
@SpringBootApplication
public class ContextLoaderPattern {

    public static void main(String[] args) {
        SpringApplication.run(ContextLoaderPattern.class, args);
    }
}

/**
 * Service demonstrating context loading
 */
@Service
class ContextLoaderService {

    private final ApplicationContext mainContext;

    public ContextLoaderService(ApplicationContext applicationContext) {
        this.mainContext = applicationContext;
    }

    /**
     * Load XML-based context
     */
    public Map<String, Object> loadXmlContext() {
        try {
            // Note: Requires XML config file
            ClassPathXmlApplicationContext context = 
                new ClassPathXmlApplicationContext("applicationContext.xml");
            
            Map<String, Object> info = new HashMap<>();
            info.put("contextId", context.getId());
            info.put("displayName", context.getDisplayName());
            info.put("beanDefinitionCount", context.getBeanDefinitionCount());
            info.put("status", "loaded");
            
            context.close();
            return info;
            
        } catch (Exception e) {
            return Map.of(
                    "status", "error",
                    "message", e.getMessage()
            );
        }
    }

    /**
     * Load annotation-based context programmatically
     */
    public Map<String, Object> loadAnnotationContext() {
        try {
            GenericApplicationContext context = new GenericApplicationContext();
            
            // Register configuration class
            context.registerBean("sampleBean", SampleBean.class);
            
            // Refresh to initialize
            context.refresh();
            
            Map<String, Object> info = new HashMap<>();
            info.put("contextId", context.getId());
            info.put("beanDefinitionCount", context.getBeanDefinitionCount());
            info.put("sampleBeanExists", context.containsBean("sampleBean"));
            info.put("status", "loaded");
            
            context.close();
            return info;
            
        } catch (Exception e) {
            return Map.of(
                    "status", "error",
                    "message", e.getMessage()
            );
        }
    }

    /**
     * Get main context information
     */
    public Map<String, Object> getMainContextInfo() {
        Map<String, Object> info = new HashMap<>();
        
        info.put("contextId", mainContext.getId());
        info.put("displayName", mainContext.getDisplayName());
        info.put("beanDefinitionCount", mainContext.getBeanDefinitionCount());
        info.put("contextType", mainContext.getClass().getSimpleName());
        
        if (mainContext instanceof ConfigurableApplicationContext) {
            ConfigurableApplicationContext configurableContext = 
                (ConfigurableApplicationContext) mainContext;
            info.put("isActive", configurableContext.isActive());
            info.put("startupDate", configurableContext.getStartupDate());
        }
        
        if (mainContext instanceof ServletWebServerApplicationContext) {
            ServletWebServerApplicationContext webContext = 
                (ServletWebServerApplicationContext) mainContext;
            info.put("webServer", webContext.getWebServer().getClass().getSimpleName());
        }
        
        return info;
    }
}

/**
 * Sample bean for programmatic registration
 */
class SampleBean {
    
    private String name = "Sample Bean";

    @PostConstruct
    public void init() {
        System.out.println("SampleBean initialized: " + name);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

/**
 * Controller exposing context loader operations
 */
@RestController
class ContextLoaderController {

    private final ContextLoaderService loaderService;

    public ContextLoaderController(ContextLoaderService loaderService) {
        this.loaderService = loaderService;
    }

    @GetMapping("/context-loader/main-context")
    public Map<String, Object> getMainContextInfo() {
        return loaderService.getMainContextInfo();
    }

    @GetMapping("/context-loader/load-xml")
    public Map<String, Object> loadXmlContext() {
        return loaderService.loadXmlContext();
    }

    @GetMapping("/context-loader/load-annotation")
    public Map<String, Object> loadAnnotationContext() {
        return loaderService.loadAnnotationContext();
    }
}

/**
 * Documentation:
 * 
 * Context Loading Methods:
 * 
 * 1. ClassPathXmlApplicationContext:
 *    ApplicationContext context = 
 *        new ClassPathXmlApplicationContext("applicationContext.xml");
 * 
 * 2. FileSystemXmlApplicationContext:
 *    ApplicationContext context = 
 *        new FileSystemXmlApplicationContext("/path/to/config.xml");
 * 
 * 3. AnnotationConfigApplicationContext:
 *    ApplicationContext context = 
 *        new AnnotationConfigApplicationContext(AppConfig.class);
 * 
 * 4. GenericApplicationContext:
 *    GenericApplicationContext context = new GenericApplicationContext();
 *    context.registerBean(MyBean.class);
 *    context.refresh();
 * 
 * 5. WebApplicationContext (Traditional):
 *    <listener>
 *        <listener-class>
 *            org.springframework.web.context.ContextLoaderListener
 *        </listener-class>
 *    </listener>
 *    
 *    <context-param>
 *        <param-name>contextConfigLocation</param-name>
 *        <param-value>/WEB-INF/applicationContext.xml</param-value>
 *    </context-param>
 * 
 * 6. Spring Boot:
 *    SpringApplication.run(Application.class, args);
 * 
 * Programmatic Context Creation:
 * 
 * GenericApplicationContext context = new GenericApplicationContext();
 * 
 * // Register beans
 * context.registerBean("myBean", MyBean.class);
 * context.registerBean("myService", MyService.class, 
 *     () -> new MyService(context.getBean(MyBean.class)));
 * 
 * // Refresh context
 * context.refresh();
 * 
 * // Use context
 * MyService service = context.getBean(MyService.class);
 * 
 * // Close context
 * context.close();
 * 
 * Web Context Loading:
 * 
 * Traditional web.xml:
 * <web-app>
 *     <!-- Root application context -->
 *     <listener>
 *         <listener-class>
 *             org.springframework.web.context.ContextLoaderListener
 *         </listener-class>
 *     </listener>
 *     
 *     <context-param>
 *         <param-name>contextConfigLocation</param-name>
 *         <param-value>
 *             /WEB-INF/spring/root-context.xml
 *         </param-value>
 *     </context-param>
 *     
 *     <!-- Servlet application context -->
 *     <servlet>
 *         <servlet-name>dispatcher</servlet-name>
 *         <servlet-class>
 *             org.springframework.web.servlet.DispatcherServlet
 *         </servlet-class>
 *         <init-param>
 *             <param-name>contextConfigLocation</param-name>
 *             <param-value>
 *                 /WEB-INF/spring/servlet-context.xml
 *             </param-value>
 *         </init-param>
 *     </servlet>
 * </web-app>
 * 
 * Java Config WebApplicationInitializer:
 * public class MyWebAppInitializer 
 *         implements WebApplicationInitializer {
 *     
 *     @Override
 *     public void onStartup(ServletContext container) {
 *         // Create root context
 *         AnnotationConfigWebApplicationContext rootContext = 
 *             new AnnotationConfigWebApplicationContext();
 *         rootContext.register(RootConfig.class);
 *         
 *         // Register ContextLoaderListener
 *         container.addListener(
 *             new ContextLoaderListener(rootContext));
 *         
 *         // Create servlet context
 *         AnnotationConfigWebApplicationContext servletContext = 
 *             new AnnotationConfigWebApplicationContext();
 *         servletContext.register(WebConfig.class);
 *         
 *         // Register DispatcherServlet
 *         ServletRegistration.Dynamic dispatcher = 
 *             container.addServlet("dispatcher", 
 *                 new DispatcherServlet(servletContext));
 *         dispatcher.setLoadOnStartup(1);
 *         dispatcher.addMapping("/");
 *     }
 * }
 * 
 * Spring Boot Auto-Configuration:
 * - Single embedded context
 * - Auto-configures web server
 * - No web.xml needed
 * - Convention over configuration
 * 
 * Best Practices:
 * - Use Spring Boot for new applications
 * - Close contexts when done (try-with-resources)
 * - Register shutdown hooks for standalone apps
 * - Use @Configuration over XML
 * - Keep context creation simple
 * - Don't create unnecessary contexts
 * 
 * Testing:
 * @SpringBootTest
 * class MyTest {
 *     @Autowired
 *     private ApplicationContext context;
 * }
 */
