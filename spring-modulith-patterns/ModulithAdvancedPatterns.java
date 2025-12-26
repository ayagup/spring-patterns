package com.example.modulith;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.modulith.ApplicationModule;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.modulith.events.Externalized;
import org.springframework.modulith.test.AssertableApplicationModule;
import org.springframework.modulith.test.PublishedEvents;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

/**
 * Spring Modulith Advanced Patterns
 * 
 * Covers remaining patterns:
 * - Application Module: Module definition and structure
 * - Event Publication Registry: Track published events
 * - Module Dependency: Define and enforce dependencies
 * - Module Boundary: Enforce architectural boundaries
 * - Module Test: Test module interactions
 * - Module Documentation: Generate module documentation
 * 
 * @author Spring Patterns
 */

@Data
class ModuleEvent {
    private String eventId;
    private String type;
    private String module;
    private Instant timestamp;
    private Object payload;
}

@Data
class ModuleDependency {
    private String fromModule;
    private String toModule;
    private String dependencyType; // USES, DEPENDS_ON, ALLOWS
}

/**
 * Application Module Pattern
 * Defines module structure using package-by-feature
 */
@Service
@Slf4j
class ApplicationModuleService {
    
    public String getModuleInfo() {
        return """
                Application Module Pattern
                =========================
                
                Structure:
                com.example.modulith/
                  ├── orders/           (Order Module)
                  │   ├── Order.java
                  │   ├── OrderService.java
                  │   ├── OrderController.java
                  │   └── internal/     (Internal APIs)
                  ├── inventory/        (Inventory Module)
                  ├── payments/         (Payment Module)
                  └── notifications/    (Notification Module)
                
                Module Definition:
                - Package = Module
                - Public API in root package
                - Internal implementation in 'internal' package
                - Events for inter-module communication
                
                Annotations:
                @ApplicationModule(
                    displayName = "Order Management",
                    allowedDependencies = {"inventory", "payments"}
                )
                """;
    }
    
    public List<String> getModuleTypes() {
        return List.of(
                "Open Module: Exposes all types",
                "Named Interface: Explicit API definition",
                "Explicit API: @NamedInterface annotation",
                "Unnamed: package-info.java configuration"
        );
    }
}

/**
 * Event Publication Registry Pattern
 * Tracks and manages published domain events
 */
@Service
@Slf4j
class EventPublicationRegistryService {
    
    /**
     * Publish event with registry tracking
     */
    @Transactional
    public void publishTrackedEvent(ModuleEvent event) {
        log.info("Publishing tracked event: {}", event.getType());
        
        // Event is automatically registered in publication registry
        // Spring Modulith tracks completion status
        // Failed events can be retried
    }
    
    /**
     * Mark event as completed
     */
    public void completeEvent(String eventId) {
        log.info("Marking event as completed: {}", eventId);
        // Spring Modulith automatically completes events
        // After successful listener execution
    }
    
    /**
     * Retry failed events
     */
    public void retryFailedEvents() {
        log.info("Retrying failed events");
        // Spring Modulith can retry incomplete events
        // Useful for resilient event processing
    }
    
    public String getRegistryInfo() {
        return """
                Event Publication Registry
                ==========================
                
                Features:
                1. Automatic Event Tracking
                   - Records published events
                   - Tracks completion status
                   - Enables event replay
                
                2. Transactional Guarantees
                   - Events published within transaction
                   - Guaranteed delivery
                   - At-least-once semantics
                
                3. Failure Handling
                   - Failed events recorded
                   - Automatic retry support
                   - Manual intervention possible
                
                4. Event Log
                   - Audit trail
                   - Debugging support
                   - Event replay capability
                
                Configuration:
                @EnableEventPublication
                @EnableTransactionalEventListeners
                """;
    }
}

/**
 * Module Dependency Pattern
 * Define and enforce module dependencies
 */
@Service
@Slf4j
class ModuleDependencyService {
    
    public List<ModuleDependency> getModuleDependencies() {
        return List.of(
                new ModuleDependency("orders", "inventory", "DEPENDS_ON"),
                new ModuleDependency("orders", "payments", "DEPENDS_ON"),
                new ModuleDependency("inventory", "notifications", "USES"),
                new ModuleDependency("payments", "notifications", "USES")
        );
    }
    
    public String getDependencyInfo() {
        return """
                Module Dependency Management
                ===========================
                
                Dependency Types:
                1. Direct Dependency
                   - @ApplicationModule(allowedDependencies = {"module"})
                   - Explicit compile-time dependency
                
                2. Event-Based (Loose Coupling)
                   - Modules communicate via events
                   - No direct dependency
                   - Asynchronous by default
                
                3. Shared Kernel
                   - Common types/interfaces
                   - Minimal shared code
                
                Rules:
                - Acyclic dependencies
                - Layer enforcement
                - No circular dependencies
                - Explicit dependency declaration
                
                Validation:
                - Compile-time checks
                - Runtime verification
                - Build-time enforcement
                """;
    }
}

/**
 * Module Boundary Pattern
 * Enforce architectural boundaries
 */
@Service
@Slf4j
class ModuleBoundaryService {
    
    public String getBoundaryInfo() {
        return """
                Module Boundary Enforcement
                ===========================
                
                Boundary Rules:
                1. Package Visibility
                   - Only root package is public API
                   - 'internal' package is module-private
                   - No direct access to internal classes
                
                2. API Gateway Pattern
                   - Module exposes service interfaces
                   - Hide implementation details
                   - Use facades for complex operations
                
                3. Event-Driven Communication
                   - Cross-module via events
                   - No direct service calls
                   - Async by default
                
                Enforcement:
                - ArchUnit tests
                - Spring Modulith verification
                - Compile-time checks
                - IDE support
                
                Benefits:
                - Clear module contracts
                - Prevent coupling
                - Enable independent development
                - Facilitate testing
                """;
    }
    
    public List<String> getBoundaryViolations() {
        return List.of(
                "Direct access to internal package",
                "Circular module dependencies",
                "Bypassing module API",
                "Tight coupling between modules"
        );
    }
}

/**
 * Module Test Pattern
 * Test module interactions and boundaries
 */
@Service
@Slf4j
class ModuleTestService {
    
    public String getTestingInfo() {
        return """
                Module Testing Patterns
                ======================
                
                1. Module Integration Tests
                @ApplicationModuleTest
                class OrderModuleTests {
                    @Test
                    void shouldProcessOrder(AssertableApplicationModule module) {
                        // Test module behavior
                        module.verify();
                    }
                }
                
                2. Event Publication Tests
                @Test
                void shouldPublishEvent(PublishedEvents events) {
                    // Trigger action
                    orderService.createOrder(order);
                    
                    // Assert event published
                    assertThat(events)
                        .contains(OrderCreated.class)
                        .matching(e -> e.orderId == orderId);
                }
                
                3. Dependency Verification
                @Test
                void shouldRespectModuleBoundaries() {
                    ApplicationModules.of(Application.class)
                        .verify();
                }
                
                4. Scenario Tests
                @Test
                void completeOrderScenario(Scenario scenario) {
                    scenario
                        .stimulate(() -> createOrder())
                        .andWaitForStateChange(() -> getOrderStatus())
                        .andExpect(status -> status == COMPLETED);
                }
                """;
    }
}

/**
 * Module Documentation Pattern
 * Generate module documentation
 */
@Service
@Slf4j
class ModuleDocumentationService {
    
    public String getDocumentationInfo() {
        return """
                Module Documentation Generation
                ================================
                
                Auto-Generated Documentation:
                1. Module Structure
                   - Module hierarchy
                   - Dependencies graph
                   - Public API
                
                2. PlantUML Diagrams
                   - Component diagrams
                   - Dependency graphs
                   - Event flow diagrams
                
                3. C4 Model Diagrams
                   - System context
                   - Container diagram
                   - Component diagram
                
                Generation:
                @Test
                void generateDocumentation() {
                    ApplicationModules modules = ApplicationModules.of(App.class);
                    
                    // Generate PlantUML
                    modules.forEach(module -> {
                        Documenter.of(modules)
                            .writeModulesAsPlantUml()
                            .writeIndividualModulesAsPlantUml();
                    });
                    
                    // Generate C4 diagrams
                    new Documenter(modules)
                        .writeDocumentation()
                        .writeModuleCanvases();
                }
                
                Output:
                - modules.puml (all modules)
                - module-{name}.puml (per module)
                - module-structure.adoc (AsciiDoc)
                """;
    }
}

/**
 * REST Controller for Module Patterns
 */
@RestController
@RequestMapping("/modulith")
@Slf4j
class ModulithPatternsController {
    
    private final ApplicationModuleService moduleService;
    private final EventPublicationRegistryService registryService;
    private final ModuleDependencyService dependencyService;
    private final ModuleBoundaryService boundaryService;
    private final ModuleTestService testService;
    private final ModuleDocumentationService docService;
    
    public ModulithPatternsController(ApplicationModuleService moduleService,
                                     EventPublicationRegistryService registryService,
                                     ModuleDependencyService dependencyService,
                                     ModuleBoundaryService boundaryService,
                                     ModuleTestService testService,
                                     ModuleDocumentationService docService) {
        this.moduleService = moduleService;
        this.registryService = registryService;
        this.dependencyService = dependencyService;
        this.boundaryService = boundaryService;
        this.testService = testService;
        this.docService = docService;
    }
    
    @GetMapping("/application-module/info")
    public String getModuleInfo() {
        return moduleService.getModuleInfo();
    }
    
    @GetMapping("/application-module/types")
    public List<String> getModuleTypes() {
        return moduleService.getModuleTypes();
    }
    
    @GetMapping("/registry/info")
    public String getRegistryInfo() {
        return registryService.getRegistryInfo();
    }
    
    @GetMapping("/dependencies")
    public List<ModuleDependency> getDependencies() {
        return dependencyService.getModuleDependencies();
    }
    
    @GetMapping("/dependencies/info")
    public String getDependencyInfo() {
        return dependencyService.getDependencyInfo();
    }
    
    @GetMapping("/boundary/info")
    public String getBoundaryInfo() {
        return boundaryService.getBoundaryInfo();
    }
    
    @GetMapping("/boundary/violations")
    public List<String> getBoundaryViolations() {
        return boundaryService.getBoundaryViolations();
    }
    
    @GetMapping("/testing/info")
    public String getTestingInfo() {
        return testService.getTestingInfo();
    }
    
    @GetMapping("/documentation/info")
    public String getDocumentationInfo() {
        return docService.getDocumentationInfo();
    }
}

@SpringBootApplication
public class ModulithAdvancedPatterns {
    public static void main(String[] args) {
        SpringApplication.run(ModulithAdvancedPatterns.class, args);
    }
}
