package com.example.modulith.patterns;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 💡 SPRING MODULITH - MODULE PATTERN 💡
 * =======================================
 * 
 * Spring Modulith provides structural verification and documentation
 * for modular monoliths with explicit module boundaries and dependencies.
 * 
 * 🎯 KEY FEATURES:
 * - Module boundaries verification
 * - Package-based modules
 * - Dependency rules enforcement
 * - Module documentation generation
 * - Event-driven inter-module communication
 * - Module testing support
 * 
 * 📦 MODULE STRUCTURE:
 * ====================
 * 
 * src/main/java/com/example/
 * ├── order/              # Order module
 * │   ├── Order.java
 * │   ├── OrderService.java
 * │   └── internal/       # Internal (package-private)
 * │       └── OrderRepository.java
 * ├── inventory/          # Inventory module
 * │   ├── Inventory.java
 * │   └── InventoryService.java
 * └── customer/           # Customer module
 *     ├── Customer.java
 *     └── CustomerService.java
 * 
 * 🔧 MODULE VERIFICATION:
 * =======================
 * 
 * @Test
 * class ModuleStructureTest {
 *     @Test
 *     fun verifyModuleStructure() {
 *         ApplicationModules.of(Application::class.java)
 *             .verify()  // Verifies module boundaries
 *     }
 * }
 * 
 * 💡 INTER-MODULE COMMUNICATION:
 * ==============================
 * 
 * // Event publication
 * @Service
 * class OrderService(
 *     private val events: ApplicationEventPublisher
 * ) {
 *     fun createOrder(order: Order) {
 *         // Save order
 *         events.publishEvent(OrderCreated(order.id))
 *     }
 * }
 * 
 * // Event listener in another module
 * @Service
 * class InventoryService {
 *     @ApplicationModuleListener
 *     fun on(event: OrderCreated) {
 *         // Reserve inventory
 *     }
 * }
 * 
 * @author Spring Patterns
 * @since 2024-01-20
 */
@SpringBootApplication
public class ModulePattern {
    public static void main(String[] args) {
        SpringApplication.run(ModulePattern.class, args);
    }
}

@Service
class ModuleService {
    public Map<String, Object> getModuleInfo() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("pattern", "Module Pattern");
        info.put("purpose", "Modular monolith structure");
        info.put("features", Arrays.asList(
            "Package-based modules",
            "Boundary verification",
            "Dependency enforcement",
            "Event-driven communication",
            "Documentation generation",
            "Module testing"
        ));
        info.put("benefits", Arrays.asList(
            "Clear boundaries",
            "Maintainable structure",
            "Testable modules",
            "Migration to microservices",
            "Documentation as code"
        ));
        return info;
    }
    
    public List<String> getModuleRules() {
        return Arrays.asList(
            "✅ Modules are top-level packages",
            "✅ 'internal' packages are module-private",
            "✅ Only public APIs can be used by other modules",
            "✅ No cyclic dependencies between modules",
            "✅ Inter-module communication via events",
            "⚠️ Direct dependency injection across modules discouraged"
        );
    }
}

@RestController
@RequestMapping("/api/modulith/module")
class ModuleController {
    private final ModuleService service;
    
    public ModuleController(ModuleService service) {
        this.service = service;
    }
    
    @GetMapping("/info")
    public Map<String, Object> getInfo() {
        return service.getModuleInfo();
    }
    
    @GetMapping("/rules")
    public List<String> getRules() {
        return service.getModuleRules();
    }
}
