package com.example.springmodulithpatterns;

import org.springframework.modulith.docs.Documenter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Module Documentation Pattern
 * 
 * Demonstrates Spring Modulith's automatic documentation generation using
 * the Documenter API for creating module diagrams and documentation.
 * 
 * Key Concepts:
 * - Automatic module documentation
 * - C4 diagram generation
 * - Module dependency visualization
 * - AsciiDoc documentation
 * - PlantUML diagrams
 */
@SpringBootApplication
public class ModuleDocumentationPattern {

    public static void main(String[] args) {
        SpringApplication.run(ModuleDocumentationPattern.class, args);
    }

    @Service
    static class DocumentationService {
        
        /**
         * Generate module documentation
         * This would typically be done as part of build process
         */
        public DocumentationInfo generateDocumentation() {
            // In real application, you would use:
            // ApplicationModules modules = ApplicationModules.of(Application.class);
            // new Documenter(modules)
            //     .writeModulesAsPlantUml()
            //     .writeIndividualModulesAsPlantUml()
            //     .writeModuleCanvases();
            
            return new DocumentationInfo(
                "Spring Modulith Documentation",
                List.of(
                    "Module overview diagram",
                    "Individual module diagrams",
                    "Module dependencies graph",
                    "Module canvases",
                    "Component documentation"
                ),
                "Generated using Documenter API"
            );
        }
        
        /**
         * Get available documentation types
         */
        public List<DocumentationType> getDocumentationTypes() {
            return List.of(
                new DocumentationType(
                    "PlantUML Diagrams",
                    "Generates PlantUML diagrams showing module structure",
                    ".puml",
                    "modules.puml"
                ),
                new DocumentationType(
                    "C4 Component Diagrams",
                    "Creates C4 architecture diagrams for modules",
                    ".puml",
                    "components.puml"
                ),
                new DocumentationType(
                    "Module Canvases",
                    "Generates module canvas documentation",
                    ".adoc",
                    "module-canvas.adoc"
                ),
                new DocumentationType(
                    "AsciiDoc Documentation",
                    "Creates comprehensive AsciiDoc documentation",
                    ".adoc",
                    "modules.adoc"
                )
            );
        }
        
        /**
         * Get module information for documentation
         */
        public List<ModuleInfo> getModuleInformation() {
            return List.of(
                new ModuleInfo(
                    "Order Module",
                    "Manages order lifecycle",
                    List.of("Inventory Module", "Payment Module"),
                    List.of("OrderService", "OrderController", "OrderRepository")
                ),
                new ModuleInfo(
                    "Inventory Module",
                    "Manages product inventory",
                    List.of(),
                    List.of("InventoryService", "StockManager")
                ),
                new ModuleInfo(
                    "Payment Module",
                    "Processes payments",
                    List.of(),
                    List.of("PaymentService", "PaymentGateway")
                )
            );
        }
        
        /**
         * Get documentation generation options
         */
        public DocumentationOptions getDocumentationOptions() {
            return new DocumentationOptions(
                "target/modulith-docs",
                List.of("PlantUML", "AsciiDoc", "Module Canvas"),
                true,
                true,
                "Documentation generated at build time"
            );
        }
        
        /**
         * Get example PlantUML diagram
         */
        public String getExamplePlantUML() {
            return """
                @startuml
                !include https://raw.githubusercontent.com/plantuml-stdlib/C4-PlantUML/master/C4_Component.puml
                
                LAYOUT_WITH_LEGEND()
                
                Container_Boundary(app, "Application") {
                    Component(order, "Order Module", "Spring Boot Module", "Manages orders")
                    Component(inventory, "Inventory Module", "Spring Boot Module", "Manages inventory")
                    Component(payment, "Payment Module", "Spring Boot Module", "Processes payments")
                }
                
                Rel(order, inventory, "Checks stock")
                Rel(order, payment, "Processes payment")
                
                @enduml
                """;
        }
        
        /**
         * Get example module canvas
         */
        public ModuleCanvas getExampleModuleCanvas() {
            return new ModuleCanvas(
                "Order Module",
                "Manages the complete order lifecycle from creation to fulfillment",
                List.of("OrderService", "OrderController", "OrderRepository"),
                List.of("Create orders", "Update order status", "Cancel orders"),
                List.of("Inventory Module - Stock validation", "Payment Module - Payment processing"),
                List.of("OrderCreatedEvent", "OrderShippedEvent", "OrderCancelledEvent"),
                List.of("Order aggregate must be consistent", "Payment before shipment")
            );
        }
    }

    @RestController
    @RequestMapping("/api/module-documentation")
    static class ModuleDocumentationController {
        
        private final DocumentationService documentationService;
        
        public ModuleDocumentationController(DocumentationService documentationService) {
            this.documentationService = documentationService;
        }
        
        @PostMapping("/generate")
        public DocumentationInfo generateDocumentation() {
            return documentationService.generateDocumentation();
        }
        
        @GetMapping("/types")
        public DocumentationTypesResponse getDocumentationTypes() {
            List<DocumentationType> types = documentationService.getDocumentationTypes();
            return new DocumentationTypesResponse(types, types.size());
        }
        
        @GetMapping("/modules")
        public ModulesInfoResponse getModuleInformation() {
            List<ModuleInfo> modules = documentationService.getModuleInformation();
            return new ModulesInfoResponse(modules, modules.size());
        }
        
        @GetMapping("/options")
        public DocumentationOptions getDocumentationOptions() {
            return documentationService.getDocumentationOptions();
        }
        
        @GetMapping("/example/plantuml")
        public PlantUMLExample getPlantUMLExample() {
            String diagram = documentationService.getExamplePlantUML();
            return new PlantUMLExample("Module Dependencies Diagram", diagram);
        }
        
        @GetMapping("/example/canvas")
        public ModuleCanvas getModuleCanvasExample() {
            return documentationService.getExampleModuleCanvas();
        }
        
        @GetMapping("/info")
        public Map<String, Object> getInfo() {
            return Map.of(
                "pattern", "Module Documentation Pattern",
                "description", "Automatic module documentation generation with Documenter API",
                "documenter", "org.springframework.modulith.docs.Documenter",
                "outputFormats", List.of("PlantUML", "AsciiDoc", "Module Canvas", "C4 Diagrams"),
                "features", List.of(
                    "Automatic diagram generation",
                    "Module dependency visualization",
                    "C4 component diagrams",
                    "Module canvas documentation",
                    "Build-time documentation"
                ),
                "usage", """
                    ApplicationModules modules = ApplicationModules.of(Application.class);
                    new Documenter(modules)
                        .writeModulesAsPlantUml()
                        .writeIndividualModulesAsPlantUml()
                        .writeModuleCanvases();
                    """,
                "endpoints", List.of(
                    "POST /api/module-documentation/generate",
                    "GET /api/module-documentation/types",
                    "GET /api/module-documentation/modules",
                    "GET /api/module-documentation/options",
                    "GET /api/module-documentation/example/plantuml",
                    "GET /api/module-documentation/example/canvas",
                    "GET /api/module-documentation/info"
                )
            );
        }
    }

    // DTOs
    record DocumentationInfo(
        String title,
        List<String> generatedArtifacts,
        String message
    ) {}

    record DocumentationType(
        String name,
        String description,
        String fileExtension,
        String exampleFileName
    ) {}

    record DocumentationTypesResponse(
        List<DocumentationType> types,
        int count
    ) {}

    record ModuleInfo(
        String name,
        String description,
        List<String> dependencies,
        List<String> components
    ) {}

    record ModulesInfoResponse(
        List<ModuleInfo> modules,
        int count
    ) {}

    record DocumentationOptions(
        String outputDirectory,
        List<String> formats,
        boolean includeDependencies,
        boolean includeComponentDetails,
        String notes
    ) {}

    record PlantUMLExample(
        String title,
        String diagram
    ) {}

    record ModuleCanvas(
        String moduleName,
        String purpose,
        List<String> components,
        List<String> responsibilities,
        List<String> collaborators,
        List<String> events,
        List<String> invariants
    ) {}
}
