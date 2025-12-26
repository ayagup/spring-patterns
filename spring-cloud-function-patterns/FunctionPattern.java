package com.example.cloudfunction;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.function.Function;

/**
 * Function Pattern
 * ================
 * 
 * Demonstrates the Function<T, R> pattern in Spring Cloud Function
 * for serverless and event-driven applications.
 * 
 * Key Concepts:
 * ------------
 * 1. Function<T, R> - Transform input T to output R
 * 2. Serverless - Deploy as AWS Lambda, Azure Functions, Google Cloud Functions
 * 3. Portable - Same code, multiple platforms
 * 4. Event-driven - Triggered by events
 * 5. Composable - Chain multiple functions
 * 
 * How It Works:
 * ------------
 * - Define @Bean of type Function<T, R>
 * - Spring Cloud Function discovers and registers it
 * - Deploy to serverless platforms (AWS Lambda, Azure, GCP)
 * - Invoke via HTTP, messaging, or platform-specific triggers
 * - Automatic input/output conversion
 * 
 * Deployment Options:
 * ------------------
 * - AWS Lambda (serverless)
 * - Azure Functions (serverless)
 * - Google Cloud Functions (serverless)
 * - Standalone (Spring Boot web)
 * - Spring Cloud Stream (messaging)
 * - Knative (Kubernetes serverless)
 * 
 * Benefits:
 * --------
 * - Platform independence
 * - Testable (plain Java functions)
 * - Composable (chain functions)
 * - Type-safe
 * - Reusable across platforms
 * 
 * @author Spring Patterns
 * @version 1.0
 */

/**
 * Example 1: Simple String Function
 */
@Configuration
class SimpleStringFunctionExample {
    
    /**
     * Simple string transformation function
     * 
     * Input: String
     * Output: String (uppercase)
     * 
     * Usage (HTTP):
     * POST /uppercase
     * Body: "hello world"
     * Response: "HELLO WORLD"
     * 
     * Usage (AWS Lambda):
     * Event: "hello world"
     * Result: "HELLO WORLD"
     */
    @Bean
    public Function<String, String> uppercase() {
        return value -> {
            System.out.println("Input: " + value);
            String result = value.toUpperCase();
            System.out.println("Output: " + result);
            return result;
        };
    }
}

/**
 * Example 2: Object Transformation Function
 */
@Configuration
class ObjectTransformationFunctionExample {
    
    static class Order {
        private String id;
        private double amount;
        
        public Order() {}
        public Order(String id, double amount) {
            this.id = id;
            this.amount = amount;
        }
        
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public double getAmount() { return amount; }
        public void setAmount(double amount) { this.amount = amount; }
    }
    
    static class OrderSummary {
        private String orderId;
        private double total;
        private String status;
        
        public OrderSummary(String orderId, double total, String status) {
            this.orderId = orderId;
            this.total = total;
            this.status = status;
        }
        
        public String getOrderId() { return orderId; }
        public double getTotal() { return total; }
        public String getStatus() { return status; }
    }
    
    /**
     * Transform Order to OrderSummary
     * 
     * Automatic JSON conversion:
     * - Input: JSON -> Order object
     * - Output: OrderSummary -> JSON
     */
    @Bean
    public Function<Order, OrderSummary> processOrder() {
        return order -> {
            System.out.println("Processing order: " + order.getId());
            
            // Business logic
            double total = order.getAmount() * 1.1; // Add 10% tax
            String status = total > 1000 ? "HIGH_VALUE" : "STANDARD";
            
            return new OrderSummary(order.getId(), total, status);
        };
    }
}

/**
 * Example 3: Collection Processing Function
 */
@Configuration
class CollectionProcessingFunctionExample {
    
    /**
     * Process collection of items
     * 
     * Input: List<Integer>
     * Output: Statistics
     */
    @Bean
    public Function<java.util.List<Integer>, java.util.Map<String, Object>> calculateStats() {
        return numbers -> {
            if (numbers.isEmpty()) {
                return java.util.Map.of("count", 0);
            }
            
            int sum = numbers.stream().mapToInt(Integer::intValue).sum();
            double average = numbers.stream().mapToInt(Integer::intValue).average().orElse(0);
            int max = numbers.stream().mapToInt(Integer::intValue).max().orElse(0);
            int min = numbers.stream().mapToInt(Integer::intValue).min().orElse(0);
            
            return java.util.Map.of(
                "count", numbers.size(),
                "sum", sum,
                "average", average,
                "max", max,
                "min", min
            );
        };
    }
}

/**
 * Example 4: Conditional Processing Function
 */
@Configuration
class ConditionalProcessingFunctionExample {
    
    static class PaymentRequest {
        private String type; // "CREDIT" or "DEBIT"
        private double amount;
        
        public PaymentRequest() {}
        public PaymentRequest(String type, double amount) {
            this.type = type;
            this.amount = amount;
        }
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public double getAmount() { return amount; }
        public void setAmount(double amount) { this.amount = amount; }
    }
    
    static class PaymentResponse {
        private String status;
        private double fee;
        private String message;
        
        public PaymentResponse(String status, double fee, String message) {
            this.status = status;
            this.fee = fee;
            this.message = message;
        }
        
        public String getStatus() { return status; }
        public double getFee() { return fee; }
        public String getMessage() { return message; }
    }
    
    /**
     * Process payment with conditional logic
     */
    @Bean
    public Function<PaymentRequest, PaymentResponse> processPayment() {
        return request -> {
            double fee;
            String status;
            String message;
            
            if ("CREDIT".equals(request.getType())) {
                fee = request.getAmount() * 0.03; // 3% for credit
                status = "APPROVED";
                message = "Credit payment processed";
            } else if ("DEBIT".equals(request.getType())) {
                fee = request.getAmount() * 0.01; // 1% for debit
                status = "APPROVED";
                message = "Debit payment processed";
            } else {
                fee = 0;
                status = "REJECTED";
                message = "Unknown payment type";
            }
            
            return new PaymentResponse(status, fee, message);
        };
    }
}

/**
 * Example 5: Data Enrichment Function
 */
@Configuration
class DataEnrichmentFunctionExample {
    
    static class Customer {
        private String id;
        private String name;
        
        public Customer() {}
        public Customer(String id, String name) {
            this.id = id;
            this.name = name;
        }
        
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
    
    static class EnrichedCustomer {
        private String id;
        private String name;
        private String tier;
        private double discount;
        private long timestamp;
        
        public EnrichedCustomer(String id, String name, String tier, double discount, long timestamp) {
            this.id = id;
            this.name = name;
            this.tier = tier;
            this.discount = discount;
            this.timestamp = timestamp;
        }
        
        public String getId() { return id; }
        public String getName() { return name; }
        public String getTier() { return tier; }
        public double getDiscount() { return discount; }
        public long getTimestamp() { return timestamp; }
    }
    
    /**
     * Enrich customer data with additional information
     */
    @Bean
    public Function<Customer, EnrichedCustomer> enrichCustomer() {
        return customer -> {
            // Simulate database lookup or external service call
            String tier = customer.getId().hashCode() % 2 == 0 ? "GOLD" : "SILVER";
            double discount = "GOLD".equals(tier) ? 0.15 : 0.05;
            long timestamp = System.currentTimeMillis();
            
            return new EnrichedCustomer(
                customer.getId(),
                customer.getName(),
                tier,
                discount,
                timestamp
            );
        };
    }
}

/**
 * Example 6: Validation Function
 */
@Configuration
class ValidationFunctionExample {
    
    static class ValidationRequest {
        private String email;
        private String phoneNumber;
        
        public ValidationRequest() {}
        public ValidationRequest(String email, String phoneNumber) {
            this.email = email;
            this.phoneNumber = phoneNumber;
        }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    }
    
    static class ValidationResult {
        private boolean valid;
        private java.util.List<String> errors;
        
        public ValidationResult(boolean valid, java.util.List<String> errors) {
            this.valid = valid;
            this.errors = errors;
        }
        
        public boolean isValid() { return valid; }
        public java.util.List<String> getErrors() { return errors; }
    }
    
    /**
     * Validate input data
     */
    @Bean
    public Function<ValidationRequest, ValidationResult> validateInput() {
        return request -> {
            java.util.List<String> errors = new java.util.ArrayList<>();
            
            // Email validation
            if (request.getEmail() == null || !request.getEmail().contains("@")) {
                errors.add("Invalid email format");
            }
            
            // Phone validation
            if (request.getPhoneNumber() == null || request.getPhoneNumber().length() < 10) {
                errors.add("Phone number must be at least 10 digits");
            }
            
            return new ValidationResult(errors.isEmpty(), errors);
        };
    }
}

/**
 * Example 7: Aggregation Function
 */
@Configuration
class AggregationFunctionExample {
    
    static class SalesData {
        private String product;
        private int quantity;
        private double price;
        
        public SalesData() {}
        public SalesData(String product, int quantity, double price) {
            this.product = product;
            this.quantity = quantity;
            this.price = price;
        }
        
        public String getProduct() { return product; }
        public void setProduct(String product) { this.product = product; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public double getPrice() { return price; }
        public void setPrice(double price) { this.price = price; }
    }
    
    /**
     * Aggregate sales data
     */
    @Bean
    public Function<java.util.List<SalesData>, java.util.Map<String, Object>> aggregateSales() {
        return salesList -> {
            int totalQuantity = salesList.stream()
                .mapToInt(SalesData::getQuantity)
                .sum();
            
            double totalRevenue = salesList.stream()
                .mapToDouble(s -> s.getQuantity() * s.getPrice())
                .sum();
            
            java.util.Map<String, Double> byProduct = salesList.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                    SalesData::getProduct,
                    java.util.stream.Collectors.summingDouble(s -> s.getQuantity() * s.getPrice())
                ));
            
            return java.util.Map.of(
                "totalQuantity", totalQuantity,
                "totalRevenue", totalRevenue,
                "byProduct", byProduct
            );
        };
    }
}

/**
 * Example 8: Error Handling Function
 */
@Configuration
class ErrorHandlingFunctionExample {
    
    /**
     * Function with error handling
     */
    @Bean
    public Function<String, String> safeProcess() {
        return input -> {
            try {
                // Potentially failing operation
                if (input == null || input.isEmpty()) {
                    throw new IllegalArgumentException("Input cannot be empty");
                }
                
                // Process
                return "Processed: " + input.toUpperCase();
                
            } catch (Exception e) {
                System.err.println("Error processing input: " + e.getMessage());
                return "Error: " + e.getMessage();
            }
        };
    }
}

/**
 * Example 9: Multi-step Processing Function
 */
@Configuration
class MultiStepProcessingFunctionExample {
    
    /**
     * Multi-step processing pipeline
     */
    @Bean
    public Function<String, String> processPipeline() {
        return input -> {
            // Step 1: Clean
            String cleaned = input.trim();
            System.out.println("Step 1 - Cleaned: " + cleaned);
            
            // Step 2: Transform
            String transformed = cleaned.toUpperCase();
            System.out.println("Step 2 - Transformed: " + transformed);
            
            // Step 3: Enrich
            String enriched = "[PROCESSED] " + transformed;
            System.out.println("Step 3 - Enriched: " + enriched);
            
            // Step 4: Validate
            if (enriched.length() > 1000) {
                return "ERROR: Result too large";
            }
            
            return enriched;
        };
    }
}

/**
 * Example 10: Function Composition
 */
@Configuration
class FunctionCompositionExample {
    
    /**
     * Individual functions that can be composed
     */
    @Bean
    public Function<String, String> trim() {
        return String::trim;
    }
    
    @Bean
    public Function<String, String> uppercase() {
        return String::toUpperCase;
    }
    
    @Bean
    public Function<String, Integer> length() {
        return String::length;
    }
    
    /**
     * Composed function
     * 
     * Usage in application.properties:
     * spring.cloud.function.definition=trim|uppercase|length
     * 
     * This chains: trim -> uppercase -> length
     */
    public void demonstrateComposition() {
        // Programmatic composition
        Function<String, String> trimAndUpper = trim().andThen(uppercase());
        String result = trimAndUpper.apply("  hello  ");
        System.out.println("Composed result: " + result); // "HELLO"
    }
}

/**
 * Main Pattern Class
 */
@Configuration
public class FunctionPattern {
    
    /**
     * Core Function pattern demonstration
     */
    public void demonstrateFunctionPattern() {
        System.out.println("\n=== Function Pattern ===");
        System.out.println("Serverless event-driven functions");
        System.out.println("\nKey Benefits:");
        System.out.println("  - Platform independent");
        System.out.println("  - Testable (plain Java)");
        System.out.println("  - Composable");
        System.out.println("  - Type-safe");
        System.out.println("  - Reusable");
        System.out.println("\nDeployment:");
        System.out.println("  - AWS Lambda");
        System.out.println("  - Azure Functions");
        System.out.println("  - Google Cloud Functions");
        System.out.println("  - Standalone (Spring Boot)");
        System.out.println("  - Messaging (Cloud Stream)");
    }
}

/**
 * Usage Examples and Configuration
 */
class FunctionPatternUsageExamples {
    
    public static void main(String[] args) {
        System.out.println("Function Pattern Usage");
        System.out.println("=====================\n");
        
        System.out.println("1. Define Function:");
        System.out.println("@Bean");
        System.out.println("public Function<String, String> uppercase() {");
        System.out.println("    return String::toUpperCase;");
        System.out.println("}\n");
        
        System.out.println("2. HTTP Invocation:");
        System.out.println("POST /uppercase");
        System.out.println("Body: \"hello\"");
        System.out.println("Response: \"HELLO\"\n");
        
        System.out.println("3. AWS Lambda Deployment:");
        System.out.println("Handler: org.springframework.cloud.function.adapter.aws.FunctionInvoker");
        System.out.println("Environment: FUNCTION_NAME=uppercase\n");
        
        System.out.println("4. Function Composition:");
        System.out.println("spring.cloud.function.definition=trim|uppercase|length\n");
        
        System.out.println("5. Testing:");
        System.out.println("Function<String, String> fn = uppercase();");
        System.out.println("assertEquals(\"HELLO\", fn.apply(\"hello\"));");
    }
}
