package com.example.contexthierarchy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Root Application Context Pattern
 * 
 * Demonstrates the root application context which serves as the parent
 * context containing shared infrastructure beans accessible to all
 * child contexts (like servlet contexts).
 * 
 * Key Concepts:
 * - Root context as parent
 * - Shared infrastructure beans
 * - Business service layer
 * - Multi-servlet sharing
 * - Context hierarchy foundation
 * 
 * Use Cases:
 * - Shared services across servlets
 * - Common infrastructure
 * - Database connections
 * - Transaction managers
 * - Business logic layer
 */
@SpringBootApplication
public class RootApplicationContextPattern {

    public static void main(String[] args) {
        SpringApplication.run(RootApplicationContextPattern.class, args);
    }
}

/**
 * Root context configuration containing shared infrastructure
 */
@Configuration
class RootContextConfig {

    @Bean
    public DataAccessService dataAccessService() {
        return new DataAccessService();
    }

    @Bean
    public BusinessService businessService() {
        return new BusinessService();
    }

    @Bean
    public SharedInfrastructure sharedInfrastructure() {
        return new SharedInfrastructure();
    }
}

/**
 * Data access service in root context (shared)
 */
class DataAccessService {
    
    public String fetchData(String id) {
        return "Data for ID: " + id;
    }

    public Map<String, String> getAllData() {
        return Map.of(
                "data1", "value1",
                "data2", "value2",
                "data3", "value3"
        );
    }
}

/**
 * Business service in root context (shared)
 */
class BusinessService {
    
    public String processBusinessLogic(String input) {
        return "Processed: " + input;
    }

    public Map<String, Object> getBusinessMetrics() {
        return Map.of(
                "activeTransactions", 42,
                "processedItems", 1000,
                "queueSize", 5
        );
    }
}

/**
 * Shared infrastructure in root context
 */
class SharedInfrastructure {
    
    private final long initTime = System.currentTimeMillis();
    private int usageCount = 0;

    public long getInitTime() {
        return initTime;
    }

    public synchronized int incrementUsage() {
        return ++usageCount;
    }

    public int getUsageCount() {
        return usageCount;
    }
}

/**
 * Service demonstrating root context access
 */
@Service
class RootContextService {

    private final ApplicationContext applicationContext;
    private final DataAccessService dataAccessService;
    private final BusinessService businessService;

    public RootContextService(ApplicationContext applicationContext,
                             DataAccessService dataAccessService,
                             BusinessService businessService) {
        this.applicationContext = applicationContext;
        this.dataAccessService = dataAccessService;
        this.businessService = businessService;
    }

    /**
     * Get root context information
     */
    public Map<String, Object> getRootContextInfo() {
        Map<String, Object> info = new HashMap<>();
        
        info.put("contextId", applicationContext.getId());
        info.put("displayName", applicationContext.getDisplayName());
        info.put("beanDefinitionCount", applicationContext.getBeanDefinitionCount());
        info.put("startupDate", applicationContext.getStartupDate());
        info.put("hasParent", applicationContext.getParent() != null);
        
        // List some important beans
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        info.put("totalBeans", beanNames.length);
        
        return info;
    }

    /**
     * Use shared services from root context
     */
    public Map<String, Object> useSharedServices() {
        return Map.of(
                "dataService", dataAccessService.fetchData("123"),
                "businessService", businessService.processBusinessLogic("test"),
                "businessMetrics", businessService.getBusinessMetrics()
        );
    }
}

/**
 * Controller demonstrating root context beans
 */
@RestController
class RootContextController {

    private final RootContextService rootContextService;
    private final SharedInfrastructure sharedInfrastructure;

    public RootContextController(RootContextService rootContextService,
                                SharedInfrastructure sharedInfrastructure) {
        this.rootContextService = rootContextService;
        this.sharedInfrastructure = sharedInfrastructure;
    }

    @GetMapping("/root-context/info")
    public Map<String, Object> getRootContextInfo() {
        return rootContextService.getRootContextInfo();
    }

    @GetMapping("/root-context/shared-services")
    public Map<String, Object> getSharedServices() {
        return rootContextService.useSharedServices();
    }

    @PostMapping("/root-context/use-infrastructure")
    public Map<String, Object> useSharedInfrastructure() {
        int count = sharedInfrastructure.incrementUsage();
        return Map.of(
                "initTime", sharedInfrastructure.getInitTime(),
                "usageCount", count,
                "message", "Shared infrastructure used"
        );
    }
}

/**
 * Documentation:
 * 
 * Root Application Context:
 * - Parent of all servlet contexts
 * - Contains shared beans
 * - Loaded by ContextLoaderListener
 * - Lives entire application lifetime
 * - Closed last during shutdown
 * 
 * Traditional Configuration (web.xml):
 * 
 * <listener>
 *   <listener-class>
 *     org.springframework.web.context.ContextLoaderListener
 *   </listener-class>
 * </listener>
 * 
 * <context-param>
 *   <param-name>contextConfigLocation</param-name>
 *   <param-value>
 *     /WEB-INF/spring/root-context.xml
 *     /WEB-INF/spring/services.xml
 *   </param-value>
 * </context-param>
 * 
 * Java Configuration:
 * 
 * public class WebAppInitializer 
 *         implements WebApplicationInitializer {
 *     
 *     @Override
 *     public void onStartup(ServletContext sc) {
 *         // Create root context
 *         AnnotationConfigWebApplicationContext rootContext = 
 *             new AnnotationConfigWebApplicationContext();
 *         rootContext.register(RootConfig.class);
 *         
 *         // Register ContextLoaderListener
 *         sc.addListener(new ContextLoaderListener(rootContext));
 *         
 *         // Create dispatcher servlet with child context
 *         AnnotationConfigWebApplicationContext servletContext = 
 *             new AnnotationConfigWebApplicationContext();
 *         servletContext.register(WebConfig.class);
 *         
 *         ServletRegistration.Dynamic dispatcher = 
 *             sc.addServlet("dispatcher", 
 *                 new DispatcherServlet(servletContext));
 *         dispatcher.setLoadOnStartup(1);
 *         dispatcher.addMapping("/");
 *     }
 * }
 * 
 * What Goes in Root Context:
 * 
 * 1. Data Access Layer:
 *    - DataSource beans
 *    - JPA EntityManagerFactory
 *    - Transaction managers
 *    - DAOs and repositories
 * 
 * 2. Service Layer:
 *    - Business services
 *    - Domain services
 *    - Application services
 * 
 * 3. Infrastructure:
 *    - Cache managers
 *    - Task executors
 *    - JMS connections
 *    - Mail senders
 * 
 * 4. Configuration:
 *    - Property sources
 *    - Profiles
 *    - Environment beans
 * 
 * What Goes in Servlet Context:
 * - Controllers
 * - View resolvers
 * - Handler mappings
 * - Interceptors
 * - Web-specific beans
 * 
 * Benefits:
 * - Separation of concerns
 * - Shared resources
 * - Reusability across servlets
 * - Clear architecture
 * - Memory efficiency
 * - Centralized configuration
 * 
 * Best Practices:
 * - Keep web layer out of root
 * - Put business logic in root
 * - Share infrastructure beans
 * - Document what goes where
 * - Use component scanning carefully
 * - Avoid circular dependencies
 * 
 * Example Structure:
 * 
 * RootContext:
 * ├── DataSource
 * ├── EntityManagerFactory
 * ├── TransactionManager
 * ├── Services
 * └── Repositories
 * 
 * ServletContext (child):
 * ├── Controllers
 * ├── ViewResolvers
 * ├── Interceptors
 * └── Validators
 * 
 * Multiple Servlets Scenario:
 * 
 * RootContext (shared)
 * ├── Servlet1Context (child)
 * │   └── Controllers for /api/*
 * └── Servlet2Context (child)
 *     └── Controllers for /admin/*
 * 
 * Each servlet has access to root beans
 * but not to each other's beans.
 * 
 * Spring Boot:
 * - Single context by default
 * - No separate root context needed
 * - Can create manually if needed
 * - Simpler for most use cases
 * 
 * Accessing Root Context:
 * 
 * // From servlet context
 * WebApplicationContext rootContext = 
 *     WebApplicationContextUtils
 *         .getWebApplicationContext(servletContext);
 * 
 * // Beans automatically available
 * @Autowired
 * DataAccessService service; // From root context
 * 
 * Lifecycle:
 * 1. ServletContext created
 * 2. ContextLoaderListener triggered
 * 3. Root context created and refreshed
 * 4. Servlets initialized
 * 5. Servlet contexts created (with root as parent)
 * 6. Application runs
 * 7. Servlet contexts closed
 * 8. Root context closed
 * 9. ServletContext destroyed
 * 
 * Common Issues:
 * - Component scanning conflicts
 * - Bean definition overrides
 * - Wrong beans in wrong context
 * - Circular dependencies
 * - Transaction propagation
 * 
 * Testing:
 * - Test root context in isolation
 * - Mock child contexts
 * - Verify bean availability
 * - Test lifecycle events
 * 
 * Migration to Spring Boot:
 * - Usually don't need root context
 * - Single context works for most cases
 * - Can preserve if needed for compatibility
 * - Simplify when possible
 */
