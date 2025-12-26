package com.example.jmx;

import org.springframework.jmx.export.assembler.*;
import org.springframework.jmx.export.MBeanExporter;
import org.springframework.jmx.support.MetricType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.management.modelmbean.ModelMBeanAttributeInfo;
import javax.management.modelmbean.ModelMBeanOperationInfo;
import java.lang.reflect.Method;
import java.util.*;

/**
 * MBean Info Assembler Pattern - MBean Metadata Assembly Strategies
 * 
 * MBeanInfoAssembler is responsible for creating metadata that describes
 * which attributes and operations of a bean should be exposed via JMX.
 * Spring provides several strategies for assembling this metadata.
 * 
 * Assembler Types:
 * 
 * 1. InterfaceBasedMBeanInfoAssembler:
 *    - Uses interface to determine exposed members
 *    - Only methods from specified interface are exposed
 *    - Provides strong typing
 * 
 * 2. SimpleReflectiveMBeanInfoAssembler:
 *    - Exposes all public methods as operations
 *    - Exposes all public getters/setters as attributes
 *    - No filtering applied
 * 
 * 3. MethodNameBasedMBeanInfoAssembler:
 *    - Filter by method names
 *    - Specify which methods to expose
 *    - Bean-specific configuration
 * 
 * 4. MethodExclusionMBeanInfoAssembler:
 *    - Blacklist approach
 *    - Exclude specific methods
 *    - All others exposed by default
 * 
 * 5. MetadataMBeanInfoAssembler:
 *    - Uses annotations for metadata
 *    - @ManagedResource, @ManagedAttribute, @ManagedOperation
 *    - Most flexible approach
 * 
 * 6. AutodetectCapableMBeanInfoAssembler:
 *    - Can auto-detect beans
 *    - Used with MBeanExporter autodetection
 * 
 * Key Methods:
 * - getMBeanInfo(): Create MBeanInfo for bean
 * - includeBean(): Determine if bean should be exposed
 * - getAttributeInfo(): Get attribute metadata
 * - getOperationInfo(): Get operation metadata
 * 
 * Use Cases:
 * - Custom metadata assembly
 * - Fine-grained exposure control
 * - Interface-based contracts
 * - Security restrictions
 * - Legacy system integration
 * 
 * Best Practices:
 * - Use interface-based for strong typing
 * - Use metadata for flexibility
 * - Document exposed operations
 * - Apply least privilege principle
 * - Consider security implications
 */
public class MBeanInfoAssemblerPattern {

    /**
     * Management interface for cache
     */
    interface CacheMBean {
        int getSize();
        void clear();
        void evict(String key);
        long getHitCount();
        long getMissCount();
    }

    /**
     * Cache implementation with additional non-exposed methods
     */
    static class Cache implements CacheMBean {
        private Map<String, Object> cacheMap = new HashMap<>();
        private long hitCount = 100;
        private long missCount = 20;
        
        // Exposed via interface
        @Override
        public int getSize() {
            return cacheMap.size();
        }
        
        @Override
        public void clear() {
            cacheMap.clear();
            System.out.println("Cache cleared");
        }
        
        @Override
        public void evict(String key) {
            cacheMap.remove(key);
            System.out.println("Evicted: " + key);
        }
        
        @Override
        public long getHitCount() {
            return hitCount;
        }
        
        @Override
        public long getMissCount() {
            return missCount;
        }
        
        // NOT exposed via interface
        public void internalRebuild() {
            System.out.println("Internal rebuild (not exposed via JMX)");
        }
        
        public void dangerousOperation() {
            System.out.println("Dangerous operation (not exposed)");
        }
    }

    /**
     * Service with selective method exposure
     */
    static class DatabaseService {
        private int maxConnections = 100;
        private int activeConnections = 10;
        private String status = "RUNNING";
        
        // Should be exposed
        public int getMaxConnections() {
            return maxConnections;
        }
        
        public void setMaxConnections(int maxConnections) {
            this.maxConnections = maxConnections;
        }
        
        public int getActiveConnections() {
            return activeConnections;
        }
        
        public String getStatus() {
            return status;
        }
        
        public void restart() {
            System.out.println("Restarting database service");
            status = "RESTARTING";
        }
        
        public void closeIdleConnections() {
            System.out.println("Closing idle connections");
        }
        
        // Should NOT be exposed
        public void internalShutdown() {
            System.out.println("Internal shutdown (should not be exposed)");
        }
        
        public void resetPassword(String password) {
            System.out.println("Reset password (should not be exposed)");
        }
        
        public void deleteAllData() {
            System.out.println("Delete all data (dangerous, should not be exposed)");
        }
    }

    /**
     * MBeanInfoAssembler configuration examples
     */
    @Configuration
    static class AssemblerConfiguration {
        
        /**
         * Interface-based assembler - most restrictive and type-safe
         */
        @Bean
        public MBeanExporter interfaceBasedExporter() {
            MBeanExporter exporter = new MBeanExporter();
            
            // Create interface-based assembler
            InterfaceBasedMBeanInfoAssembler assembler = 
                new InterfaceBasedMBeanInfoAssembler();
            
            // Set management interfaces
            assembler.setManagedInterfaces(new Class<?>[] { CacheMBean.class });
            
            exporter.setAssembler(assembler);
            
            // Register beans
            Map<String, Object> beans = new HashMap<>();
            beans.put("bean:name=cache", new Cache());
            exporter.setBeans(beans);
            
            return exporter;
        }
        
        /**
         * Simple reflective assembler - exposes all public methods
         */
        @Bean
        public MBeanExporter simpleReflectiveExporter() {
            MBeanExporter exporter = new MBeanExporter();
            
            // Use simple reflective assembler (exposes everything)
            SimpleReflectiveMBeanInfoAssembler assembler = 
                new SimpleReflectiveMBeanInfoAssembler();
            
            exporter.setAssembler(assembler);
            
            return exporter;
        }
        
        /**
         * Method name-based assembler - whitelist approach
         */
        @Bean
        public MBeanExporter methodNameBasedExporter() {
            MBeanExporter exporter = new MBeanExporter();
            
            MethodNameBasedMBeanInfoAssembler assembler = 
                new MethodNameBasedMBeanInfoAssembler();
            
            // Define which methods to expose per bean
            Properties methodMappings = new Properties();
            methodMappings.setProperty("bean:name=dbService", 
                "getMaxConnections,setMaxConnections,getActiveConnections,getStatus,restart,closeIdleConnections");
            
            assembler.setMethodMappings(methodMappings);
            exporter.setAssembler(assembler);
            
            // Register beans
            Map<String, Object> beans = new HashMap<>();
            beans.put("bean:name=dbService", new DatabaseService());
            exporter.setBeans(beans);
            
            return exporter;
        }
        
        /**
         * Method exclusion assembler - blacklist approach
         */
        @Bean
        public MBeanExporter methodExclusionExporter() {
            MBeanExporter exporter = new MBeanExporter();
            
            MethodExclusionMBeanInfoAssembler assembler = 
                new MethodExclusionMBeanInfoAssembler();
            
            // Define which methods to EXCLUDE per bean
            Properties ignoredMethods = new Properties();
            ignoredMethods.setProperty("bean:name=dbService", 
                "internalShutdown,resetPassword,deleteAllData");
            
            assembler.setIgnoredMethodMappings(ignoredMethods);
            exporter.setAssembler(assembler);
            
            return exporter;
        }
        
        /**
         * Metadata assembler - annotation-based
         */
        @Bean
        public MBeanExporter metadataExporter() {
            MBeanExporter exporter = new MBeanExporter();
            
            // Use metadata assembler for annotation-based export
            MetadataMBeanInfoAssembler assembler = new MetadataMBeanInfoAssembler();
            
            exporter.setAssembler(assembler);
            
            return exporter;
        }
    }

    /**
     * Custom assembler implementation
     */
    static class CustomMBeanInfoAssembler extends SimpleReflectiveMBeanInfoAssembler {
        
        private Set<String> allowedPrefixes = new HashSet<>(
            Arrays.asList("get", "set", "is", "restart", "close", "clear", "evict")
        );
        
        @Override
        protected boolean includeOperation(Method method, String beanKey) {
            String methodName = method.getName();
            
            // Only include methods with allowed prefixes
            for (String prefix : allowedPrefixes) {
                if (methodName.startsWith(prefix)) {
                    return true;
                }
            }
            
            return false;
        }
        
        @Override
        protected String getOperationDescription(Method method, String beanKey) {
            // Provide custom descriptions
            return "Operation: " + method.getName() + " (auto-generated)";
        }
        
        @Override
        protected String getAttributeDescription(String attributeName, String beanKey) {
            // Provide custom descriptions
            return "Attribute: " + attributeName + " (auto-generated)";
        }
    }

    /**
     * Usage examples
     */
    static class MBeanInfoAssemblerExamples {
        
        public void demonstrateInterfaceBasedAssembler() {
            System.out.println("\n=== Interface-based Assembler ===");
            
            InterfaceBasedMBeanInfoAssembler assembler = 
                new InterfaceBasedMBeanInfoAssembler();
            assembler.setManagedInterfaces(new Class<?>[] { CacheMBean.class });
            
            Cache cache = new Cache();
            
            System.out.println("Exposed methods (from CacheMBean interface only):");
            System.out.println("- getSize()");
            System.out.println("- clear()");
            System.out.println("- evict(String)");
            System.out.println("- getHitCount()");
            System.out.println("- getMissCount()");
            System.out.println("\nNOT exposed:");
            System.out.println("- internalRebuild()");
            System.out.println("- dangerousOperation()");
        }
        
        public void demonstrateMethodNameBasedAssembler() {
            System.out.println("\n=== Method Name-based Assembler ===");
            
            MethodNameBasedMBeanInfoAssembler assembler = 
                new MethodNameBasedMBeanInfoAssembler();
            
            Properties mappings = new Properties();
            mappings.setProperty("dbService", 
                "getMaxConnections,setMaxConnections,getActiveConnections,restart");
            assembler.setMethodMappings(mappings);
            
            System.out.println("Whitelist approach - only specified methods exposed:");
            System.out.println("- getMaxConnections()");
            System.out.println("- setMaxConnections(int)");
            System.out.println("- getActiveConnections()");
            System.out.println("- restart()");
            System.out.println("\nAll other methods excluded");
        }
        
        public void demonstrateMethodExclusionAssembler() {
            System.out.println("\n=== Method Exclusion Assembler ===");
            
            MethodExclusionMBeanInfoAssembler assembler = 
                new MethodExclusionMBeanInfoAssembler();
            
            Properties ignored = new Properties();
            ignored.setProperty("dbService", 
                "internalShutdown,resetPassword,deleteAllData");
            assembler.setIgnoredMethodMappings(ignored);
            
            System.out.println("Blacklist approach - all methods exposed EXCEPT:");
            System.out.println("- internalShutdown()");
            System.out.println("- resetPassword(String)");
            System.out.println("- deleteAllData()");
        }
        
        public void demonstrateCustomAssembler() {
            System.out.println("\n=== Custom Assembler ===");
            
            CustomMBeanInfoAssembler assembler = new CustomMBeanInfoAssembler();
            
            System.out.println("Custom logic: only methods with allowed prefixes:");
            System.out.println("Allowed prefixes: get, set, is, restart, close, clear, evict");
            System.out.println("\nExample inclusions:");
            System.out.println("- getMaxConnections() ✓");
            System.out.println("- setMaxConnections(int) ✓");
            System.out.println("- restart() ✓");
            System.out.println("- clearCache() ✓");
            System.out.println("\nExample exclusions:");
            System.out.println("- internalShutdown() ✗");
            System.out.println("- deleteAllData() ✗");
        }
    }

    public static void main(String[] args) {
        System.out.println("MBean Info Assembler Pattern - MBean Metadata Assembly Strategies");
        System.out.println("==================================================================");
        
        MBeanInfoAssemblerExamples examples = new MBeanInfoAssemblerExamples();
        
        examples.demonstrateInterfaceBasedAssembler();
        examples.demonstrateMethodNameBasedAssembler();
        examples.demonstrateMethodExclusionAssembler();
        examples.demonstrateCustomAssembler();
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println("Assembler Comparison:");
        System.out.println("\nInterfaceBasedMBeanInfoAssembler:");
        System.out.println("  + Type-safe, compile-time checking");
        System.out.println("  + Clear contract");
        System.out.println("  - Requires interface implementation");
        
        System.out.println("\nSimpleReflectiveMBeanInfoAssembler:");
        System.out.println("  + Simple, no configuration");
        System.out.println("  - Exposes all public methods (security risk)");
        
        System.out.println("\nMethodNameBasedMBeanInfoAssembler:");
        System.out.println("  + Fine-grained control");
        System.out.println("  + Whitelist approach (secure)");
        System.out.println("  - Verbose configuration");
        
        System.out.println("\nMethodExclusionMBeanInfoAssembler:");
        System.out.println("  + Easy to exclude sensitive methods");
        System.out.println("  - Blacklist approach (less secure)");
        
        System.out.println("\nMetadataMBeanInfoAssembler:");
        System.out.println("  + Flexible, annotation-based");
        System.out.println("  + Self-documenting");
        System.out.println("  - Requires Spring annotations");
    }
}
