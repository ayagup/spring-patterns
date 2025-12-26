package com.example.springmodulithpatterns;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Module Boundary Pattern
 * 
 * Demonstrates Spring Modulith's module boundary enforcement using
 * package visibility and API contracts.
 * 
 * Key Concepts:
 * - API vs Internal package separation
 * - Public API contracts
 * - Internal implementation hiding
 * - Package-private classes
 * - Module encapsulation
 * 
 * Package Structure:
 * - com.example.module.api        (public API - accessible to other modules)
 * - com.example.module.internal   (internal implementation - not accessible)
 */
@SpringBootApplication
public class ModuleBoundaryPattern {

    public static void main(String[] args) {
        SpringApplication.run(ModuleBoundaryPattern.class, args);
    }

    /**
     * PUBLIC API - Exposed to other modules
     * This interface defines the public contract of the module
     */
    public interface CustomerApi {
        Customer findCustomer(String customerId);
        Customer createCustomer(String name, String email);
        List<Customer> findAllCustomers();
    }

    /**
     * PUBLIC DTO - Part of the API contract
     */
    public record Customer(
        String customerId,
        String name,
        String email,
        String status
    ) {}

    /**
     * INTERNAL - Implementation details hidden from other modules
     * This service implements the public API but is package-private
     */
    @Service
    static class CustomerServiceImpl implements CustomerApi {
        
        private final CustomerRepository repository;
        
        public CustomerServiceImpl(CustomerRepository repository) {
            this.repository = repository;
        }
        
        @Override
        public Customer findCustomer(String customerId) {
            CustomerEntity entity = repository.findById(customerId);
            return mapToApi(entity);
        }
        
        @Override
        public Customer createCustomer(String name, String email) {
            // Validate using internal validator
            CustomerValidator.validate(name, email);
            
            // Create entity
            CustomerEntity entity = new CustomerEntity(
                generateId(),
                name,
                email,
                "ACTIVE"
            );
            
            repository.save(entity);
            return mapToApi(entity);
        }
        
        @Override
        public List<Customer> findAllCustomers() {
            return repository.findAll().stream()
                .map(this::mapToApi)
                .toList();
        }
        
        private Customer mapToApi(CustomerEntity entity) {
            return new Customer(
                entity.id(),
                entity.name(),
                entity.email(),
                entity.status()
            );
        }
        
        private String generateId() {
            return "CUST-" + System.currentTimeMillis();
        }
    }

    /**
     * INTERNAL - Repository (not exposed to other modules)
     */
    @Service
    static class CustomerRepository {
        
        private final List<CustomerEntity> storage = new java.util.ArrayList<>();
        
        public CustomerEntity findById(String id) {
            return storage.stream()
                .filter(c -> c.id().equals(id))
                .findFirst()
                .orElse(new CustomerEntity(id, "Unknown", "unknown@example.com", "INACTIVE"));
        }
        
        public void save(CustomerEntity entity) {
            storage.add(entity);
        }
        
        public List<CustomerEntity> findAll() {
            return List.copyOf(storage);
        }
    }

    /**
     * INTERNAL - Entity class (not exposed to other modules)
     */
    record CustomerEntity(
        String id,
        String name,
        String email,
        String status
    ) {}

    /**
     * INTERNAL - Validator (not exposed to other modules)
     */
    static class CustomerValidator {
        
        static void validate(String name, String email) {
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("Name is required");
            }
            
            if (email == null || !email.contains("@")) {
                throw new IllegalArgumentException("Valid email is required");
            }
        }
    }

    /**
     * PUBLIC API - Controller exposing module functionality
     */
    @RestController
    @RequestMapping("/api/module-boundary")
    static class ModuleBoundaryController {
        
        private final CustomerApi customerApi;
        
        public ModuleBoundaryController(CustomerApi customerApi) {
            this.customerApi = customerApi;
        }
        
        @GetMapping("/customers/{customerId}")
        public CustomerResponse getCustomer(@PathVariable String customerId) {
            Customer customer = customerApi.findCustomer(customerId);
            return new CustomerResponse(customer, "success");
        }
        
        @PostMapping("/customers")
        public CustomerResponse createCustomer(@RequestBody CreateCustomerRequest request) {
            Customer customer = customerApi.createCustomer(request.name(), request.email());
            return new CustomerResponse(customer, "Customer created successfully");
        }
        
        @GetMapping("/customers")
        public CustomersResponse getAllCustomers() {
            List<Customer> customers = customerApi.findAllCustomers();
            return new CustomersResponse(customers, customers.size());
        }
        
        @GetMapping("/info")
        public Map<String, Object> getInfo() {
            return Map.of(
                "pattern", "Module Boundary Pattern",
                "description", "Enforce module boundaries with API/Internal separation",
                "packageStructure", Map.of(
                    "api", "Public interfaces and DTOs accessible to other modules",
                    "internal", "Implementation details hidden from other modules"
                ),
                "publicAPI", List.of(
                    "CustomerApi interface",
                    "Customer DTO"
                ),
                "internal", List.of(
                    "CustomerServiceImpl",
                    "CustomerRepository",
                    "CustomerEntity",
                    "CustomerValidator"
                ),
                "features", List.of(
                    "Clear API contracts",
                    "Implementation hiding",
                    "Package-level encapsulation",
                    "Module isolation",
                    "Compile-time boundary enforcement"
                ),
                "endpoints", List.of(
                    "GET /api/module-boundary/customers/{customerId}",
                    "POST /api/module-boundary/customers",
                    "GET /api/module-boundary/customers",
                    "GET /api/module-boundary/info"
                )
            );
        }
    }

    // DTOs for REST endpoints
    record CreateCustomerRequest(String name, String email) {}
    record CustomerResponse(Customer customer, String message) {}
    record CustomersResponse(List<Customer> customers, int count) {}
}
