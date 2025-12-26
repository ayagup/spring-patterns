package com.example.springmodulithpatterns;

import org.springframework.modulith.ApplicationModule;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Module Dependency Pattern
 * 
 * Demonstrates Spring Modulith's module dependency management using
 * @ApplicationModule and explicit dependency declarations.
 * 
 * Key Concepts:
 * - Explicit dependency declaration
 * - Dependency validation at startup
 * - Circular dependency prevention
 * - Module isolation
 * - Dependency graph visualization
 */
@SpringBootApplication
public class ModuleDependencyPattern {

    public static void main(String[] args) {
        SpringApplication.run(ModuleDependencyPattern.class, args);
    }

    /**
     * Inventory Module - No external dependencies
     */
    @ApplicationModule(displayName = "Inventory Management")
    static class InventoryModule {
        // This module has no dependencies on other modules
    }

    @Service
    static class InventoryService {
        
        public boolean checkStock(String productId, int quantity) {
            // Simulate stock check
            return quantity <= 100;
        }
        
        public void reserveStock(String productId, int quantity) {
            System.out.println("Reserving stock: " + productId + " x " + quantity);
        }
        
        public StockInfo getStockInfo(String productId) {
            return new StockInfo(productId, 100, 75, true);
        }
    }

    /**
     * Payment Module - No external dependencies
     */
    @ApplicationModule(displayName = "Payment Processing")
    static class PaymentModule {
        // This module has no dependencies on other modules
    }

    @Service
    static class PaymentService {
        
        public PaymentResult processPayment(String orderId, double amount) {
            // Simulate payment processing
            boolean success = amount > 0 && amount < 10000;
            return new PaymentResult(
                "PAY-" + System.currentTimeMillis(),
                orderId,
                amount,
                success ? "SUCCESS" : "FAILED"
            );
        }
        
        public boolean validatePaymentMethod(String paymentMethod) {
            return List.of("CREDIT_CARD", "DEBIT_CARD", "PAYPAL").contains(paymentMethod);
        }
    }

    /**
     * Order Module - Depends on Inventory and Payment modules
     * Dependencies are explicitly declared
     */
    @ApplicationModule(
        displayName = "Order Management",
        allowedDependencies = {"inventory", "payment"}
    )
    static class OrderModule {
        // This module explicitly depends on inventory and payment modules
        // Spring Modulith will validate these dependencies at startup
    }

    @Service
    static class OrderService {
        
        private final InventoryService inventoryService;
        private final PaymentService paymentService;
        
        // Dependencies injected from other modules
        public OrderService(
                InventoryService inventoryService,
                PaymentService paymentService) {
            this.inventoryService = inventoryService;
            this.paymentService = paymentService;
        }
        
        /**
         * Create order using dependencies from other modules
         */
        public OrderResult createOrder(OrderRequest request) {
            // Step 1: Check inventory (using InventoryService)
            boolean inStock = inventoryService.checkStock(
                request.productId(),
                request.quantity()
            );
            
            if (!inStock) {
                return new OrderResult(
                    null,
                    "FAILED",
                    "Insufficient stock",
                    null
                );
            }
            
            // Step 2: Validate payment method (using PaymentService)
            boolean validPayment = paymentService.validatePaymentMethod(
                request.paymentMethod()
            );
            
            if (!validPayment) {
                return new OrderResult(
                    null,
                    "FAILED",
                    "Invalid payment method",
                    null
                );
            }
            
            // Step 3: Reserve inventory (using InventoryService)
            inventoryService.reserveStock(request.productId(), request.quantity());
            
            // Step 4: Process payment (using PaymentService)
            PaymentResult paymentResult = paymentService.processPayment(
                "ORD-" + System.currentTimeMillis(),
                request.amount()
            );
            
            if (!"SUCCESS".equals(paymentResult.status())) {
                return new OrderResult(
                    null,
                    "FAILED",
                    "Payment failed",
                    null
                );
            }
            
            // Step 5: Create order
            String orderId = "ORD-" + System.currentTimeMillis();
            return new OrderResult(
                orderId,
                "SUCCESS",
                "Order created successfully",
                paymentResult.transactionId()
            );
        }
        
        /**
         * Get order dependencies information
         */
        public DependencyInfo getDependencies() {
            return new DependencyInfo(
                "OrderModule",
                List.of("InventoryModule", "PaymentModule"),
                "Order module depends on Inventory and Payment modules"
            );
        }
    }

    @RestController
    @RequestMapping("/api/module-dependency")
    static class ModuleDependencyController {
        
        private final OrderService orderService;
        private final InventoryService inventoryService;
        private final PaymentService paymentService;
        
        public ModuleDependencyController(
                OrderService orderService,
                InventoryService inventoryService,
                PaymentService paymentService) {
            this.orderService = orderService;
            this.inventoryService = inventoryService;
            this.paymentService = paymentService;
        }
        
        @PostMapping("/order")
        public OrderResult createOrder(@RequestBody OrderRequest request) {
            return orderService.createOrder(request);
        }
        
        @GetMapping("/stock/{productId}")
        public StockInfo getStock(@PathVariable String productId) {
            return inventoryService.getStockInfo(productId);
        }
        
        @GetMapping("/dependencies")
        public DependencyInfo getDependencies() {
            return orderService.getDependencies();
        }
        
        @GetMapping("/info")
        public Map<String, Object> getInfo() {
            return Map.of(
                "pattern", "Module Dependency Pattern",
                "description", "Explicit module dependency management and validation",
                "modules", Map.of(
                    "OrderModule", Map.of(
                        "dependencies", List.of("InventoryModule", "PaymentModule"),
                        "description", "Manages orders using inventory and payment services"
                    ),
                    "InventoryModule", Map.of(
                        "dependencies", List.of(),
                        "description", "Manages product inventory"
                    ),
                    "PaymentModule", Map.of(
                        "dependencies", List.of(),
                        "description", "Processes payments"
                    )
                ),
                "features", List.of(
                    "Explicit dependency declaration",
                    "Startup-time validation",
                    "Circular dependency prevention",
                    "Module isolation",
                    "Clear dependency graph"
                ),
                "endpoints", List.of(
                    "POST /api/module-dependency/order",
                    "GET /api/module-dependency/stock/{productId}",
                    "GET /api/module-dependency/dependencies",
                    "GET /api/module-dependency/info"
                )
            );
        }
    }

    // DTOs
    record OrderRequest(
        String productId,
        int quantity,
        double amount,
        String paymentMethod
    ) {}
    
    record OrderResult(
        String orderId,
        String status,
        String message,
        String transactionId
    ) {}
    
    record StockInfo(
        String productId,
        int totalStock,
        int availableStock,
        boolean inStock
    ) {}
    
    record PaymentResult(
        String transactionId,
        String orderId,
        double amount,
        String status
    ) {}
    
    record DependencyInfo(
        String module,
        List<String> dependencies,
        String description
    ) {}
}
