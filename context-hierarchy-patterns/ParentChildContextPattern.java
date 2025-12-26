package com.example.contexthierarchy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Parent-Child Context Pattern
 * 
 * Demonstrates creating a hierarchical application context structure
 * with parent and child contexts. Child contexts can access parent beans,
 * but parent cannot access child beans.
 * 
 * Key Concepts:
 * - Parent context
 * - Child context
 * - Bean visibility and inheritance
 * - Context hierarchy
 * - Bean overriding in child context
 * 
 * Use Cases:
 * - Multi-module applications
 * - Web application contexts
 * - Plugin architectures
 * - Isolated bean scopes
 * - Testing with custom contexts
 */
@SpringBootApplication
public class ParentChildContextPattern {

    public static void main(String[] args) {
        SpringApplication.run(ParentChildContextPattern.class, args);
    }
}

/**
 * Parent context configuration
 */
@Configuration
class ParentContextConfig {

    @Bean
    public ParentService parentService() {
        return new ParentService();
    }

    @Bean
    public SharedService sharedService() {
        return new SharedService("Parent Implementation");
    }
}

/**
 * Child context configuration
 */
@Configuration
class ChildContextConfig {

    @Bean
    public ChildService childService() {
        return new ChildService();
    }

    // Override parent bean
    @Bean
    public SharedService sharedService() {
        return new SharedService("Child Implementation");
    }
}

/**
 * Service available in parent context
 */
class ParentService {
    
    private final String serviceName = "ParentService";
    
    public String getServiceName() {
        return serviceName;
    }
    
    public String execute() {
        return "Executed by " + serviceName;
    }
}

/**
 * Service available in child context
 */
class ChildService {
    
    private final String serviceName = "ChildService";
    
    @Autowired(required = false)
    private ParentService parentService; // Can access parent bean
    
    public String getServiceName() {
        return serviceName;
    }
    
    public String execute() {
        String result = "Executed by " + serviceName;
        if (parentService != null) {
            result += " (with parent: " + parentService.getServiceName() + ")";
        }
        return result;
    }
}

/**
 * Service available in both contexts (can be overridden)
 */
class SharedService {
    
    private final String implementation;
    
    public SharedService(String implementation) {
        this.implementation = implementation;
    }
    
    public String getImplementation() {
        return implementation;
    }
    
    public String execute() {
        return "Executed by " + implementation;
    }
}

/**
 * Service demonstrating context hierarchy
 */
@org.springframework.stereotype.Service
class ContextHierarchyService {

    private final ApplicationContext applicationContext;

    public ContextHierarchyService(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * Create parent-child context hierarchy programmatically
     */
    public Map<String, Object> createContextHierarchy() {
        // Create parent context
        GenericApplicationContext parentContext = new GenericApplicationContext();
        parentContext.registerBean("parentService", ParentService.class);
        parentContext.registerBean("sharedService", SharedService.class, 
                () -> new SharedService("Parent Implementation"));
        parentContext.refresh();

        // Create child context with parent
        GenericApplicationContext childContext = new GenericApplicationContext(parentContext);
        childContext.registerBean("childService", ChildService.class);
        childContext.registerBean("sharedService", SharedService.class, 
                () -> new SharedService("Child Implementation"));
        childContext.refresh();

        Map<String, Object> result = new HashMap<>();
        result.put("parentBeans", parentContext.getBeanDefinitionCount());
        result.put("childBeans", childContext.getBeanDefinitionCount());
        result.put("childCanAccessParent", childContext.containsBean("parentService"));
        result.put("childOverridesShared", 
                childContext.getBean(SharedService.class).getImplementation());

        // Cleanup
        childContext.close();
        parentContext.close();

        return result;
    }

    /**
     * Get current context hierarchy information
     */
    public Map<String, Object> getContextHierarchyInfo() {
        Map<String, Object> info = new HashMap<>();
        
        ApplicationContext current = applicationContext;
        int level = 0;
        
        while (current != null) {
            info.put("level" + level + "_id", current.getId());
            info.put("level" + level + "_displayName", current.getDisplayName());
            info.put("level" + level + "_beanCount", current.getBeanDefinitionCount());
            
            current = current.getParent();
            level++;
        }
        
        info.put("hierarchyDepth", level);
        info.put("hasParent", applicationContext.getParent() != null);
        
        return info;
    }
}

/**
 * Controller to demonstrate context hierarchy
 */
@RestController
class ContextHierarchyController {

    private final ContextHierarchyService hierarchyService;
    private final ApplicationContext applicationContext;

    public ContextHierarchyController(ContextHierarchyService hierarchyService,
                                     ApplicationContext applicationContext) {
        this.hierarchyService = hierarchyService;
        this.applicationContext = applicationContext;
    }

    @GetMapping("/context-hierarchy/info")
    public Map<String, Object> getHierarchyInfo() {
        return hierarchyService.getContextHierarchyInfo();
    }

    @GetMapping("/context-hierarchy/create-demo")
    public Map<String, Object> createDemoHierarchy() {
        return hierarchyService.createContextHierarchy();
    }

    @GetMapping("/context-hierarchy/parent-info")
    public Map<String, Object> getParentInfo() {
        Map<String, Object> info = new HashMap<>();
        ApplicationContext parent = applicationContext.getParent();
        
        if (parent != null) {
            info.put("hasParent", true);
            info.put("parentId", parent.getId());
            info.put("parentDisplayName", parent.getDisplayName());
            info.put("parentBeanCount", parent.getBeanDefinitionCount());
        } else {
            info.put("hasParent", false);
        }
        
        return info;
    }
}

/**
 * Documentation:
 * 
 * Parent-Child Context Hierarchy:
 * - Parent context created first
 * - Child context has reference to parent
 * - Child can access parent beans
 * - Parent CANNOT access child beans
 * - Child beans can override parent beans
 * 
 * Creating Hierarchy:
 * 
 * 1. Programmatically:
 *    GenericApplicationContext parent = new GenericApplicationContext();
 *    parent.refresh();
 *    
 *    GenericApplicationContext child = new GenericApplicationContext(parent);
 *    child.refresh();
 * 
 * 2. With AnnotationConfigApplicationContext:
 *    AnnotationConfigApplicationContext parent = 
 *        new AnnotationConfigApplicationContext(ParentConfig.class);
 *    
 *    AnnotationConfigApplicationContext child = 
 *        new AnnotationConfigApplicationContext();
 *    child.setParent(parent);
 *    child.register(ChildConfig.class);
 *    child.refresh();
 * 
 * 3. Web Application (Spring MVC):
 *    - Root context (parent): Business services
 *    - Web context (child): Controllers, view resolvers
 * 
 * Bean Lookup Behavior:
 * - Child context searches locally first
 * - Then searches parent context
 * - Parent never searches child
 * - First match wins in hierarchy
 * 
 * Bean Overriding:
 * - Child can override parent beans
 * - Same bean name in child shadows parent
 * - Type-based lookup uses child bean
 * - Explicit parent access possible
 * 
 * Access Parent Beans:
 * 
 * // From child context
 * ParentService service = childContext.getBean(ParentService.class);
 * 
 * // Or with @Autowired in child bean
 * @Autowired
 * ParentService parentService;
 * 
 * Use Cases:
 * 
 * 1. Web Applications:
 *    - Root context: Services, DAOs, infrastructure
 *    - Servlet context: Controllers, interceptors
 *    - Each servlet has own child context
 * 
 * 2. Modular Applications:
 *    - Parent: Core services
 *    - Children: Feature modules
 *    - Plugin architecture
 * 
 * 3. Multi-Tenant:
 *    - Parent: Shared services
 *    - Child per tenant: Tenant-specific beans
 * 
 * 4. Testing:
 *    - Parent: Production beans
 *    - Child: Test overrides and mocks
 * 
 * Best Practices:
 * - Keep parent context minimal
 * - Put shared beans in parent
 * - Put specific beans in child
 * - Document hierarchy structure
 * - Avoid deep hierarchies
 * - Clean up contexts properly
 * - Be explicit about bean sources
 * 
 * Common Patterns:
 * 
 * 1. Shared Infrastructure:
 *    Parent: DataSources, transaction managers
 *    Child: Application-specific services
 * 
 * 2. Multi-Module:
 *    Parent: Common utilities
 *    Children: Module-specific beans
 * 
 * 3. Web Application:
 *    Root: Business layer
 *    Servlet: Web layer
 * 
 * Context Lifecycle:
 * - Parent started first
 * - Child started after parent
 * - Child closed before parent
 * - Parent closed last
 * 
 * Bean Visibility:
 * 
 * Parent beans:
 * - Visible to parent
 * - Visible to all children
 * - Visible to grandchildren
 * 
 * Child beans:
 * - Visible to child only
 * - NOT visible to parent
 * - NOT visible to siblings
 * 
 * Configuration:
 * 
 * @Configuration
 * class ParentConfig {
 *     @Bean
 *     public SharedService sharedService() {
 *         return new SharedService();
 *     }
 * }
 * 
 * @Configuration
 * class ChildConfig {
 *     @Bean
 *     public ChildService childService(SharedService shared) {
 *         return new ChildService(shared); // Injected from parent
 *     }
 * }
 * 
 * Potential Issues:
 * - Bean name conflicts
 * - Circular dependencies across contexts
 * - Complexity in debugging
 * - Memory leaks if not closed properly
 * - Confusion about bean sources
 * 
 * Testing:
 * - Mock parent context for child tests
 * - Test with minimal parent context
 * - Verify bean visibility
 * - Test lifecycle ordering
 * 
 * Spring Boot:
 * - Usually single context
 * - Hierarchy used in web applications
 * - Multiple servlet contexts possible
 * - Test slices may use hierarchy
 * 
 * Alternatives:
 * - Single flat context (simpler)
 * - Module dependencies
 * - Profiles for separation
 * - Conditional beans
 */
