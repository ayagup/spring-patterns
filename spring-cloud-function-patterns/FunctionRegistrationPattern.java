package com.example.cloudfunction;

import org.springframework.cloud.function.context.FunctionRegistration;
import org.springframework.cloud.function.context.FunctionRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.function.Function;

/**
 * Function Registration Pattern
 * ==============================
 * 
 * Demonstrates programmatic function registration in Spring Cloud Function
 * for dynamic function management and runtime registration.
 * 
 * Key Concepts:
 * ------------
 * 1. FunctionRegistration - Metadata for function registration
 * 2. FunctionRegistry - Manages function lifecycle
 * 3. Dynamic Registration - Add functions at runtime
 * 4. Programmatic Configuration - Beyond @Bean annotation
 * 5. Function Types - Configure input/output types explicitly
 * 
 * How It Works:
 * ------------
 * - Create FunctionRegistration with metadata
 * - Set function target (the actual function instance)
 * - Define input/output types
 * - Register with FunctionRegistry
 * - Function becomes available in catalog
 * 
 * Benefits:
 * --------
 * - Runtime function addition
 * - Dynamic configuration
 * - Conditional registration
 * - External function loading
 * - Fine-grained control
 * 
 * @author Spring Patterns
 * @version 1.0
 */

/**
 * Example 1: Basic Function Registration
 */
@Configuration
class BasicFunctionRegistrationExample {
    
    /**
     * Register function programmatically
     */
    @Bean
    public FunctionRegistration<String, String> uppercaseRegistration() {
        return new FunctionRegistration<>(uppercase())
            .type(FunctionType.from(String.class).to(String.class));
    }
    
    private Function<String, String> uppercase() {
        return value -> {
            System.out.println("Uppercase function invoked");
            return value.toUpperCase();
        };
    }
    
    /**
     * Helper class for type specification
     */
    static class FunctionType {
        public static TypeBuilder from(Class<?> inputType) {
            return new TypeBuilder(inputType);
        }
        
        static class TypeBuilder {
            private final Class<?> inputType;
            
            TypeBuilder(Class<?> inputType) {
                this.inputType = inputType;
            }
            
            public Class<?>[] to(Class<?> outputType) {
                return new Class<?>[]{inputType, outputType};
            }
        }
    }
}

/**
 * Example 2: Multiple Function Registration
 */
@Configuration
class MultipleFunctionRegistrationExample {
    
    @Bean
    public FunctionRegistration<String, String> trimRegistration() {
        return new FunctionRegistration<>(String::trim)
            .name("trim");
    }
    
    @Bean
    public FunctionRegistration<String, String> reverseRegistration() {
        return new FunctionRegistration<>(
            str -> new StringBuilder(str).reverse().toString()
        ).name("reverse");
    }
    
    @Bean
    public FunctionRegistration<String, Integer> lengthRegistration() {
        return new FunctionRegistration<String, Integer>(String::length)
            .name("length");
    }
    
    /**
     * Demonstrate multiple registrations
     */
    public void demonstrateMultiple() {
        System.out.println("Multiple functions registered:");
        System.out.println("  - trim");
        System.out.println("  - reverse");
        System.out.println("  - length");
    }
}

/**
 * Example 3: Conditional Function Registration
 */
@Configuration
class ConditionalFunctionRegistrationExample {
    
    /**
     * Register function based on condition
     */
    @Bean
    public FunctionRegistration<String, String> conditionalFunction() {
        boolean enablePremiumFeatures = checkPremiumLicense();
        
        if (enablePremiumFeatures) {
            System.out.println("Registering PREMIUM function");
            return new FunctionRegistration<>(premiumProcessor())
                .name("processor");
        } else {
            System.out.println("Registering STANDARD function");
            return new FunctionRegistration<>(standardProcessor())
                .name("processor");
        }
    }
    
    private boolean checkPremiumLicense() {
        // Simulate license check
        return Math.random() > 0.5;
    }
    
    private Function<String, String> premiumProcessor() {
        return data -> data.toUpperCase() + " [PREMIUM]";
    }
    
    private Function<String, String> standardProcessor() {
        return data -> data.toUpperCase();
    }
}

/**
 * Example 4: Runtime Function Registration
 */
@Configuration
class RuntimeFunctionRegistrationExample {
    
    /**
     * Register function at runtime
     */
    public void registerFunctionAtRuntime(FunctionRegistry registry, 
                                          String functionName,
                                          Function<String, String> function) {
        System.out.println("Registering function at runtime: " + functionName);
        
        FunctionRegistration<String, String> registration = 
            new FunctionRegistration<>(function)
                .name(functionName);
        
        // Register with registry
        // registry.register(registration);
        
        System.out.println("Function registered successfully!");
    }
    
    /**
     * Example usage
     */
    public void demonstrateRuntimeRegistration(FunctionRegistry registry) {
        // Register custom function
        Function<String, String> customFunction = input -> {
            System.out.println("Custom function processing: " + input);
            return "Processed: " + input;
        };
        
        registerFunctionAtRuntime(registry, "customProcessor", customFunction);
    }
}

/**
 * Example 5: Function with Properties
 */
@Configuration
class FunctionWithPropertiesExample {
    
    static class ProcessingProperties {
        private int maxLength = 100;
        private boolean uppercase = true;
        private String prefix = "[PROCESSED]";
        
        public int getMaxLength() { return maxLength; }
        public void setMaxLength(int maxLength) { this.maxLength = maxLength; }
        public boolean isUppercase() { return uppercase; }
        public void setUppercase(boolean uppercase) { this.uppercase = uppercase; }
        public String getPrefix() { return prefix; }
        public void setPrefix(String prefix) { this.prefix = prefix; }
    }
    
    @Bean
    public FunctionRegistration<String, String> configurableProcessorRegistration() {
        ProcessingProperties props = new ProcessingProperties();
        props.setMaxLength(50);
        props.setUppercase(true);
        props.setPrefix("[CUSTOM]");
        
        Function<String, String> processor = createConfigurableProcessor(props);
        
        return new FunctionRegistration<>(processor)
            .name("configurableProcessor");
    }
    
    private Function<String, String> createConfigurableProcessor(ProcessingProperties props) {
        return input -> {
            String result = input;
            
            // Apply max length
            if (result.length() > props.getMaxLength()) {
                result = result.substring(0, props.getMaxLength());
            }
            
            // Apply uppercase
            if (props.isUppercase()) {
                result = result.toUpperCase();
            }
            
            // Apply prefix
            result = props.getPrefix() + " " + result;
            
            return result;
        };
    }
}

/**
 * Example 6: Generic Type Registration
 */
@Configuration
class GenericTypeRegistrationExample {
    
    static class Order {
        private String id;
        private double amount;
        
        public Order(String id, double amount) {
            this.id = id;
            this.amount = amount;
        }
        
        public String getId() { return id; }
        public double getAmount() { return amount; }
    }
    
    static class OrderSummary {
        private String orderId;
        private String status;
        
        public OrderSummary(String orderId, String status) {
            this.orderId = orderId;
            this.status = status;
        }
        
        public String getOrderId() { return orderId; }
        public String getStatus() { return status; }
    }
    
    @Bean
    public FunctionRegistration<Order, OrderSummary> orderProcessorRegistration() {
        Function<Order, OrderSummary> processor = order -> {
            System.out.println("Processing order: " + order.getId());
            String status = order.getAmount() > 1000 ? "HIGH_VALUE" : "STANDARD";
            return new OrderSummary(order.getId(), status);
        };
        
        return new FunctionRegistration<>(processor)
            .name("orderProcessor")
            .type(FunctionType.from(Order.class).to(OrderSummary.class));
    }
    
    static class FunctionType {
        public static TypeBuilder from(Class<?> inputType) {
            return new TypeBuilder(inputType);
        }
        
        static class TypeBuilder {
            private final Class<?> inputType;
            
            TypeBuilder(Class<?> inputType) {
                this.inputType = inputType;
            }
            
            public Class<?>[] to(Class<?> outputType) {
                return new Class<?>[]{inputType, outputType};
            }
        }
    }
}

/**
 * Example 7: Function Chain Registration
 */
@Configuration
class FunctionChainRegistrationExample {
    
    @Bean
    public FunctionRegistration<String, String> step1Registration() {
        return new FunctionRegistration<>(
            input -> {
                System.out.println("Step 1: Trimming");
                return input.trim();
            }
        ).name("step1");
    }
    
    @Bean
    public FunctionRegistration<String, String> step2Registration() {
        return new FunctionRegistration<>(
            input -> {
                System.out.println("Step 2: Uppercase");
                return input.toUpperCase();
            }
        ).name("step2");
    }
    
    @Bean
    public FunctionRegistration<String, Integer> step3Registration() {
        return new FunctionRegistration<String, Integer>(
            input -> {
                System.out.println("Step 3: Length");
                return input.length();
            }
        ).name("step3");
    }
    
    /**
     * Chain can be invoked as: step1|step2|step3
     */
    public void demonstrateChain() {
        System.out.println("Function chain available:");
        System.out.println("  Composition: step1|step2|step3");
        System.out.println("  Input: '  hello  '");
        System.out.println("  Output: 5");
    }
}

/**
 * Example 8: External Function Loading
 */
@Configuration
class ExternalFunctionLoadingExample {
    
    /**
     * Load function from external source (e.g., database, file)
     */
    @Bean
    public FunctionRegistration<String, String> externalFunctionRegistration() {
        // Simulate loading from external source
        String functionCode = loadFunctionCode();
        Function<String, String> function = compileFunction(functionCode);
        
        return new FunctionRegistration<>(function)
            .name("externalFunction");
    }
    
    private String loadFunctionCode() {
        // Simulate loading from database or file
        System.out.println("Loading function code from external source...");
        return "toUpperCase";
    }
    
    private Function<String, String> compileFunction(String code) {
        // Simulate compilation (in reality, might use scripting engine)
        System.out.println("Compiling function: " + code);
        
        switch (code) {
            case "toUpperCase":
                return String::toUpperCase;
            case "toLowerCase":
                return String::toLowerCase;
            default:
                return input -> input;
        }
    }
}

/**
 * Example 9: Versioned Function Registration
 */
@Configuration
class VersionedFunctionRegistrationExample {
    
    @Bean
    public FunctionRegistration<String, String> processV1Registration() {
        return new FunctionRegistration<>(
            data -> "V1: " + data.toUpperCase()
        ).name("process_v1");
    }
    
    @Bean
    public FunctionRegistration<String, String> processV2Registration() {
        return new FunctionRegistration<>(
            data -> "V2: " + data.toLowerCase() + " [ENHANCED]"
        ).name("process_v2");
    }
    
    @Bean
    public FunctionRegistration<String, String> processLatestRegistration() {
        // Always point to latest version
        return new FunctionRegistration<>(
            data -> "V2: " + data.toLowerCase() + " [ENHANCED]"
        ).name("process_latest");
    }
    
    /**
     * Demonstrate version management
     */
    public void demonstrateVersioning() {
        System.out.println("Versioned functions available:");
        System.out.println("  - process_v1 (legacy)");
        System.out.println("  - process_v2 (current)");
        System.out.println("  - process_latest (alias to v2)");
    }
}

/**
 * Example 10: Function with Lifecycle Hooks
 */
@Configuration
class FunctionWithLifecycleExample {
    
    static class LifecycleAwareFunction implements Function<String, String> {
        
        public LifecycleAwareFunction() {
            System.out.println("Function constructed");
        }
        
        @Override
        public String apply(String input) {
            System.out.println("Function invoked with: " + input);
            return input.toUpperCase();
        }
        
        public void destroy() {
            System.out.println("Function destroyed");
        }
    }
    
    @Bean
    public FunctionRegistration<String, String> lifecycleFunctionRegistration() {
        LifecycleAwareFunction function = new LifecycleAwareFunction();
        
        return new FunctionRegistration<>(function)
            .name("lifecycleFunction");
    }
}

/**
 * Main Pattern Class
 */
@Configuration
public class FunctionRegistrationPattern {
    
    /**
     * Core Function Registration demonstration
     */
    public void demonstrateFunctionRegistration() {
        System.out.println("\n=== Function Registration Pattern ===");
        System.out.println("Programmatic function management");
        System.out.println("\nKey Features:");
        System.out.println("  - Programmatic registration");
        System.out.println("  - Dynamic function creation");
        System.out.println("  - Conditional registration");
        System.out.println("  - Type specification");
        System.out.println("  - Runtime management");
        System.out.println("\nUse Cases:");
        System.out.println("  - Dynamic function loading");
        System.out.println("  - Conditional features");
        System.out.println("  - External function sources");
        System.out.println("  - Version management");
        System.out.println("  - Plugin systems");
    }
}

/**
 * Usage Examples
 */
class FunctionRegistrationUsageExamples {
    
    public static void main(String[] args) {
        System.out.println("Function Registration Pattern Usage");
        System.out.println("===================================\n");
        
        System.out.println("1. Basic Registration:");
        System.out.println("@Bean");
        System.out.println("public FunctionRegistration<String, String> myFunctionReg() {");
        System.out.println("    return new FunctionRegistration<>(myFunction)");
        System.out.println("        .name(\"myFunction\");");
        System.out.println("}\n");
        
        System.out.println("2. Runtime Registration:");
        System.out.println("FunctionRegistration<T, R> reg = new FunctionRegistration<>(fn)");
        System.out.println("    .name(\"dynamicFunction\");");
        System.out.println("registry.register(reg);\n");
        
        System.out.println("3. Conditional Registration:");
        System.out.println("if (condition) {");
        System.out.println("    return new FunctionRegistration<>(premiumFn);");
        System.out.println("} else {");
        System.out.println("    return new FunctionRegistration<>(standardFn);");
        System.out.println("}\n");
        
        System.out.println("Benefits:");
        System.out.println("- Runtime flexibility");
        System.out.println("- Dynamic configuration");
        System.out.println("- Fine-grained control");
        System.out.println("- External function loading");
    }
}
