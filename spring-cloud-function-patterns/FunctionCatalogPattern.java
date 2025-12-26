package com.example.cloudfunction;

import org.springframework.cloud.function.context.FunctionCatalog;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.function.Function;

/**
 * Function Catalog Pattern
 * =========================
 * 
 * Demonstrates the FunctionCatalog for dynamic function lookup
 * and invocation in Spring Cloud Function.
 * 
 * Key Concepts:
 * ------------
 * 1. FunctionCatalog - Registry of all available functions
 * 2. Dynamic Lookup - Find functions by name at runtime
 * 3. Function Composition - Chain multiple functions
 * 4. Type Conversion - Automatic input/output conversion
 * 5. Runtime Invocation - Invoke functions dynamically
 * 
 * How It Works:
 * ------------
 * - Spring auto-discovers all @Bean functions
 * - FunctionCatalog maintains registry
 * - Lookup by name: catalog.lookup("functionName")
 * - Compose functions: catalog.lookup("func1|func2|func3")
 * - Invoke dynamically based on runtime conditions
 * 
 * Benefits:
 * --------
 * - Dynamic function selection
 * - Runtime composition
 * - Flexible routing
 * - Plugin architecture
 * - A/B testing capabilities
 * 
 * @author Spring Patterns
 * @version 1.0
 */

/**
 * Example 1: Basic Function Catalog Usage
 */
@Configuration
class BasicFunctionCatalogExample {
    
    /**
     * Sample functions for catalog
     */
    @Bean
    public Function<String, String> uppercase() {
        return String::toUpperCase;
    }
    
    @Bean
    public Function<String, String> reverse() {
        return str -> new StringBuilder(str).reverse().toString();
    }
    
    @Bean
    public Function<String, Integer> length() {
        return String::length;
    }
    
    /**
     * Demonstrate function catalog usage
     */
    public void demonstrateFunctionCatalog(FunctionCatalog catalog) {
        System.out.println("=== Function Catalog Demo ===");
        
        // Lookup by name
        Function<String, String> upperFunc = catalog.lookup("uppercase");
        String result1 = upperFunc.apply("hello");
        System.out.println("uppercase('hello') = " + result1);
        
        // Lookup reverse function
        Function<String, String> reverseFunc = catalog.lookup("reverse");
        String result2 = reverseFunc.apply("hello");
        System.out.println("reverse('hello') = " + result2);
        
        // Lookup length function
        Function<String, Integer> lengthFunc = catalog.lookup("length");
        Integer result3 = lengthFunc.apply("hello");
        System.out.println("length('hello') = " + result3);
    }
}

/**
 * Example 2: Function Composition via Catalog
 */
@Configuration
class FunctionCompositionCatalogExample {
    
    @Bean
    public Function<String, String> trim() {
        return String::trim;
    }
    
    @Bean
    public Function<String, String> lowercase() {
        return String::toLowerCase;
    }
    
    @Bean
    public Function<String, String> removeSpaces() {
        return str -> str.replaceAll("\\s+", "");
    }
    
    /**
     * Demonstrate function composition
     */
    public void demonstrateComposition(FunctionCatalog catalog) {
        System.out.println("=== Function Composition ===");
        
        // Compose using pipe notation
        // trim -> lowercase -> removeSpaces
        Function<String, String> composed = catalog.lookup("trim|lowercase|removeSpaces");
        
        String input = "  Hello World  ";
        String result = composed.apply(input);
        
        System.out.println("Input: '" + input + "'");
        System.out.println("Composed result: '" + result + "'");
        // Output: "helloworld"
    }
}

/**
 * Example 3: Dynamic Function Selection
 */
@Configuration
class DynamicFunctionSelectionExample {
    
    static class ProcessingRequest {
        private String strategy; // "fast", "accurate", "balanced"
        private String data;
        
        public ProcessingRequest(String strategy, String data) {
            this.strategy = strategy;
            this.data = data;
        }
        
        public String getStrategy() { return strategy; }
        public String getData() { return data; }
    }
    
    @Bean
    public Function<String, String> fastProcessing() {
        return data -> {
            System.out.println("Using FAST processing");
            return data.substring(0, Math.min(10, data.length()));
        };
    }
    
    @Bean
    public Function<String, String> accurateProcessing() {
        return data -> {
            System.out.println("Using ACCURATE processing");
            return data.toUpperCase() + " [VERIFIED]";
        };
    }
    
    @Bean
    public Function<String, String> balancedProcessing() {
        return data -> {
            System.out.println("Using BALANCED processing");
            return data.substring(0, Math.min(20, data.length())).toUpperCase();
        };
    }
    
    /**
     * Select function based on runtime strategy
     */
    public String processWithStrategy(ProcessingRequest request, FunctionCatalog catalog) {
        System.out.println("Processing with strategy: " + request.getStrategy());
        
        // Dynamic function selection
        String functionName = request.getStrategy() + "Processing";
        Function<String, String> processor = catalog.lookup(functionName);
        
        if (processor != null) {
            return processor.apply(request.getData());
        } else {
            System.err.println("Unknown strategy: " + request.getStrategy());
            return request.getData();
        }
    }
}

/**
 * Example 4: Function Router
 */
@Configuration
class FunctionRouterExample {
    
    static class RoutedMessage {
        private String type; // "email", "sms", "push"
        private String recipient;
        private String content;
        
        public RoutedMessage(String type, String recipient, String content) {
            this.type = type;
            this.recipient = recipient;
            this.content = content;
        }
        
        public String getType() { return type; }
        public String getRecipient() { return recipient; }
        public String getContent() { return content; }
    }
    
    @Bean
    public Function<RoutedMessage, String> emailHandler() {
        return msg -> {
            System.out.println("Sending EMAIL to: " + msg.getRecipient());
            return "Email sent: " + msg.getContent();
        };
    }
    
    @Bean
    public Function<RoutedMessage, String> smsHandler() {
        return msg -> {
            System.out.println("Sending SMS to: " + msg.getRecipient());
            return "SMS sent: " + msg.getContent();
        };
    }
    
    @Bean
    public Function<RoutedMessage, String> pushHandler() {
        return msg -> {
            System.out.println("Sending PUSH to: " + msg.getRecipient());
            return "Push sent: " + msg.getContent();
        };
    }
    
    /**
     * Route message to appropriate handler
     */
    public String routeMessage(RoutedMessage message, FunctionCatalog catalog) {
        String handlerName = message.getType() + "Handler";
        Function<RoutedMessage, String> handler = catalog.lookup(handlerName);
        
        if (handler != null) {
            return handler.apply(message);
        } else {
            return "Unknown message type: " + message.getType();
        }
    }
}

/**
 * Example 5: Version Selection
 */
@Configuration
class VersionSelectionExample {
    
    @Bean
    public Function<String, String> processV1() {
        return data -> {
            System.out.println("Using V1 algorithm");
            return "V1: " + data.toUpperCase();
        };
    }
    
    @Bean
    public Function<String, String> processV2() {
        return data -> {
            System.out.println("Using V2 algorithm");
            return "V2: " + data.toLowerCase() + " [enhanced]";
        };
    }
    
    @Bean
    public Function<String, String> processV3() {
        return data -> {
            System.out.println("Using V3 algorithm");
            return "V3: " + new StringBuilder(data).reverse().toString();
        };
    }
    
    /**
     * Select version based on client preference
     */
    public String processWithVersion(String data, String version, FunctionCatalog catalog) {
        String functionName = "process" + version;
        Function<String, String> processor = catalog.lookup(functionName);
        
        if (processor != null) {
            return processor.apply(data);
        } else {
            System.err.println("Version not found, using V1");
            return catalog.lookup("processV1").apply(data);
        }
    }
}

/**
 * Example 6: A/B Testing with Catalog
 */
@Configuration
class ABTestingCatalogExample {
    
    static class ABTestRequest {
        private String userId;
        private String data;
        
        public ABTestRequest(String userId, String data) {
            this.userId = userId;
            this.data = data;
        }
        
        public String getUserId() { return userId; }
        public String getData() { return data; }
    }
    
    @Bean
    public Function<String, String> algorithmA() {
        return data -> {
            System.out.println("Using Algorithm A");
            return "A: " + data.toUpperCase();
        };
    }
    
    @Bean
    public Function<String, String> algorithmB() {
        return data -> {
            System.out.println("Using Algorithm B");
            return "B: " + data.toLowerCase();
        };
    }
    
    /**
     * A/B test based on user ID
     */
    public String processWithABTest(ABTestRequest request, FunctionCatalog catalog) {
        // Simple A/B split based on user ID hash
        boolean useA = request.getUserId().hashCode() % 2 == 0;
        
        String functionName = useA ? "algorithmA" : "algorithmB";
        System.out.println("User " + request.getUserId() + " assigned to: " + functionName);
        
        Function<String, String> algorithm = catalog.lookup(functionName);
        return algorithm.apply(request.getData());
    }
}

/**
 * Example 7: Plugin Architecture
 */
@Configuration
class PluginArchitectureExample {
    
    /**
     * Core plugins
     */
    @Bean
    public Function<String, String> pluginAuthentication() {
        return data -> {
            System.out.println("[Plugin] Authentication");
            return data + " [AUTHENTICATED]";
        };
    }
    
    @Bean
    public Function<String, String> pluginValidation() {
        return data -> {
            System.out.println("[Plugin] Validation");
            return data + " [VALIDATED]";
        };
    }
    
    @Bean
    public Function<String, String> pluginEncryption() {
        return data -> {
            System.out.println("[Plugin] Encryption");
            return data + " [ENCRYPTED]";
        };
    }
    
    /**
     * Execute plugin chain
     */
    public String executePlugins(String data, java.util.List<String> pluginNames, 
                                FunctionCatalog catalog) {
        System.out.println("Executing plugin chain...");
        
        String result = data;
        for (String pluginName : pluginNames) {
            Function<String, String> plugin = catalog.lookup(pluginName);
            if (plugin != null) {
                result = plugin.apply(result);
            } else {
                System.err.println("Plugin not found: " + pluginName);
            }
        }
        
        return result;
    }
}

/**
 * Example 8: Feature Toggle via Catalog
 */
@Configuration
class FeatureToggleCatalogExample {
    
    @Bean
    public Function<String, String> featureStandard() {
        return data -> {
            System.out.println("Using STANDARD feature set");
            return data.toUpperCase();
        };
    }
    
    @Bean
    public Function<String, String> featurePremium() {
        return data -> {
            System.out.println("Using PREMIUM feature set");
            return data.toUpperCase() + " [PREMIUM]";
        };
    }
    
    @Bean
    public Function<String, String> featureEnterprise() {
        return data -> {
            System.out.println("Using ENTERPRISE feature set");
            return data.toUpperCase() + " [ENTERPRISE] [ADVANCED]";
        };
    }
    
    /**
     * Select features based on subscription tier
     */
    public String processWithFeatures(String data, String tier, FunctionCatalog catalog) {
        String functionName = "feature" + tier;
        Function<String, String> feature = catalog.lookup(functionName);
        
        if (feature != null) {
            return feature.apply(data);
        } else {
            // Default to standard
            return catalog.lookup("featureStandard").apply(data);
        }
    }
}

/**
 * Example 9: Fallback Chain
 */
@Configuration
class FallbackChainExample {
    
    /**
     * Try multiple functions in order until one succeeds
     */
    public String processWithFallback(String data, FunctionCatalog catalog) {
        String[] fallbackChain = {"primaryProcessor", "secondaryProcessor", "defaultProcessor"};
        
        for (String functionName : fallbackChain) {
            try {
                Function<String, String> processor = catalog.lookup(functionName);
                if (processor != null) {
                    System.out.println("Trying: " + functionName);
                    return processor.apply(data);
                }
            } catch (Exception e) {
                System.err.println("Failed with " + functionName + ": " + e.getMessage());
            }
        }
        
        return "All processors failed: " + data;
    }
    
    @Bean
    public Function<String, String> defaultProcessor() {
        return data -> {
            System.out.println("Using DEFAULT processor (fallback)");
            return data;
        };
    }
}

/**
 * Example 10: Multi-Tenant Function Selection
 */
@Configuration
class MultiTenantFunctionExample {
    
    static class TenantRequest {
        private String tenantId;
        private String data;
        
        public TenantRequest(String tenantId, String data) {
            this.tenantId = tenantId;
            this.data = data;
        }
        
        public String getTenantId() { return tenantId; }
        public String getData() { return data; }
    }
    
    @Bean
    public Function<String, String> processorTenant1() {
        return data -> "Tenant1: " + data.toUpperCase();
    }
    
    @Bean
    public Function<String, String> processorTenant2() {
        return data -> "Tenant2: " + data.toLowerCase();
    }
    
    @Bean
    public Function<String, String> processorDefault() {
        return data -> "Default: " + data;
    }
    
    /**
     * Select processor based on tenant
     */
    public String processForTenant(TenantRequest request, FunctionCatalog catalog) {
        String functionName = "processorTenant" + request.getTenantId();
        Function<String, String> processor = catalog.lookup(functionName);
        
        if (processor == null) {
            System.out.println("No specific processor for tenant " + request.getTenantId());
            processor = catalog.lookup("processorDefault");
        }
        
        return processor.apply(request.getData());
    }
}

/**
 * Main Pattern Class
 */
@Configuration
public class FunctionCatalogPattern {
    
    /**
     * Core Function Catalog demonstration
     */
    public void demonstrateFunctionCatalog() {
        System.out.println("\n=== Function Catalog Pattern ===");
        System.out.println("Dynamic function lookup and composition");
        System.out.println("\nKey Features:");
        System.out.println("  - Dynamic lookup by name");
        System.out.println("  - Function composition (pipe notation)");
        System.out.println("  - Runtime selection");
        System.out.println("  - Type conversion");
        System.out.println("\nUse Cases:");
        System.out.println("  - Dynamic routing");
        System.out.println("  - Plugin architecture");
        System.out.println("  - A/B testing");
        System.out.println("  - Feature toggles");
        System.out.println("  - Multi-tenancy");
    }
}

/**
 * Usage Examples
 */
class FunctionCatalogUsageExamples {
    
    public static void main(String[] args) {
        System.out.println("Function Catalog Pattern Usage");
        System.out.println("==============================\n");
        
        System.out.println("1. Lookup by Name:");
        System.out.println("Function<String, String> fn = catalog.lookup(\"uppercase\");");
        System.out.println("String result = fn.apply(\"hello\");\n");
        
        System.out.println("2. Function Composition:");
        System.out.println("Function<String, String> composed = catalog.lookup(\"trim|uppercase|reverse\");");
        System.out.println("String result = composed.apply(\"  hello  \");\n");
        
        System.out.println("3. Dynamic Selection:");
        System.out.println("String funcName = determineFunction(request);");
        System.out.println("Function<T, R> fn = catalog.lookup(funcName);");
        System.out.println("R result = fn.apply(input);\n");
        
        System.out.println("Benefits:");
        System.out.println("- Runtime flexibility");
        System.out.println("- Extensible architecture");
        System.out.println("- Clean separation of concerns");
        System.out.println("- Easy A/B testing");
    }
}
