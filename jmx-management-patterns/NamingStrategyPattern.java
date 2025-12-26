package com.example.jmx;

import org.springframework.jmx.export.naming.*;
import org.springframework.jmx.support.ObjectNameManager;
import org.springframework.jmx.export.MBeanExporter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;
import java.util.*;

/**
 * Naming Strategy Pattern - JMX ObjectName Generation Strategies
 * 
 * ObjectNamingStrategy determines how JMX ObjectNames are generated for
 * Spring-managed beans. The ObjectName is the unique identifier for an
 * MBean in the MBeanServer.
 * 
 * Naming Strategy Types:
 * 
 * 1. IdentityNamingStrategy:
 *    - Uses bean identity (memory address)
 *    - Guaranteed unique
 *    - Non-deterministic, changes on restart
 *    - Not suitable for production
 * 
 * 2. KeyNamingStrategy:
 *    - Uses properties to build ObjectName
 *    - Default: type and name properties
 *    - Configurable key mappings
 * 
 * 3. MetadataNamingStrategy:
 *    - Uses annotation metadata
 *    - @ManagedResource objectName attribute
 *    - Falls back to other strategies
 * 
 * 4. Custom ObjectNamingStrategy:
 *    - Implement ObjectNamingStrategy interface
 *    - Full control over naming logic
 *    - Can use bean name, class, properties
 * 
 * ObjectName Format:
 * domain:key1=value1,key2=value2,...
 * 
 * Example:
 * com.example:type=Service,name=UserService,env=production
 * 
 * Components:
 * - Domain: Namespace (e.g., com.example)
 * - Key-value pairs: Properties (type, name, etc.)
 * 
 * Key Considerations:
 * - Uniqueness: Each ObjectName must be unique
 * - Persistence: Should be consistent across restarts
 * - Readability: Should be human-friendly
 * - Organization: Use domain for grouping
 * - Convention: Follow standard patterns
 * 
 * Common Properties:
 * - type: Bean type/category
 * - name: Bean name/instance
 * - service: Service name
 * - component: Component name
 * - environment: Deployment environment
 * - application: Application name
 * - instance: Instance identifier
 * 
 * Use Cases:
 * - Multi-tenant applications
 * - Microservices environments
 * - Environment-specific naming
 * - Hierarchical organization
 * - Custom naming conventions
 * 
 * Best Practices:
 * - Use consistent domain names
 * - Include type and name properties
 * - Add environment/instance info
 * - Avoid special characters
 * - Keep names readable
 * - Document naming conventions
 */
public class NamingStrategyPattern {

    /**
     * Example service beans
     */
    static class UserService {
        private String environment = "production";
        
        public String getEnvironment() {
            return environment;
        }
        
        public int getUserCount() {
            return 1000;
        }
    }

    static class CacheService {
        private String region = "us-east-1";
        
        public String getRegion() {
            return region;
        }
        
        public long getCacheSize() {
            return 1024 * 1024;
        }
    }

    /**
     * Custom naming strategy implementation
     */
    static class CustomObjectNamingStrategy implements ObjectNamingStrategy {
        
        private String domain = "com.example";
        private String environment = "production";
        
        public CustomObjectNamingStrategy() {}
        
        public CustomObjectNamingStrategy(String domain, String environment) {
            this.domain = domain;
            this.environment = environment;
        }
        
        @Override
        public ObjectName getObjectName(Object managedBean, String beanKey) 
                throws MalformedObjectNameException {
            
            // Extract class name
            String className = managedBean.getClass().getSimpleName();
            
            // Build ObjectName with domain, type, name, and environment
            String objectName = String.format(
                "%s:type=%s,name=%s,environment=%s",
                domain,
                className,
                beanKey,
                environment
            );
            
            return ObjectNameManager.getInstance(objectName);
        }
    }

    /**
     * Hierarchical naming strategy
     */
    static class HierarchicalNamingStrategy implements ObjectNamingStrategy {
        
        private String applicationName;
        private String instanceId;
        
        public HierarchicalNamingStrategy(String applicationName, String instanceId) {
            this.applicationName = applicationName;
            this.instanceId = instanceId;
        }
        
        @Override
        public ObjectName getObjectName(Object managedBean, String beanKey) 
                throws MalformedObjectNameException {
            
            String className = managedBean.getClass().getSimpleName();
            
            // Create hierarchical name: app/instance/component/bean
            String objectName = String.format(
                "com.example:application=%s,instance=%s,component=%s,bean=%s",
                applicationName,
                instanceId,
                getComponentName(className),
                beanKey
            );
            
            return ObjectNameManager.getInstance(objectName);
        }
        
        private String getComponentName(String className) {
            // Extract component from class name
            if (className.endsWith("Service")) {
                return "Services";
            } else if (className.endsWith("Repository")) {
                return "Repositories";
            } else if (className.endsWith("Controller")) {
                return "Controllers";
            }
            return "Components";
        }
    }

    /**
     * Environment-aware naming strategy
     */
    static class EnvironmentNamingStrategy implements ObjectNamingStrategy {
        
        private Map<String, String> environmentProperties;
        
        public EnvironmentNamingStrategy(Map<String, String> environmentProperties) {
            this.environmentProperties = environmentProperties;
        }
        
        @Override
        public ObjectName getObjectName(Object managedBean, String beanKey) 
                throws MalformedObjectNameException {
            
            String environment = environmentProperties.getOrDefault("environment", "unknown");
            String region = environmentProperties.getOrDefault("region", "default");
            String cluster = environmentProperties.getOrDefault("cluster", "main");
            
            String className = managedBean.getClass().getSimpleName();
            
            String objectName = String.format(
                "com.example:environment=%s,region=%s,cluster=%s,type=%s,name=%s",
                environment,
                region,
                cluster,
                className,
                beanKey
            );
            
            return ObjectNameManager.getInstance(objectName);
        }
    }

    /**
     * Naming strategy configuration
     */
    @Configuration
    static class NamingStrategyConfiguration {
        
        /**
         * Identity naming strategy (default)
         */
        @Bean
        public MBeanExporter identityNamingExporter() {
            MBeanExporter exporter = new MBeanExporter();
            
            // Default strategy - uses bean identity
            IdentityNamingStrategy strategy = new IdentityNamingStrategy();
            exporter.setNamingStrategy(strategy);
            
            return exporter;
        }
        
        /**
         * Key naming strategy
         */
        @Bean
        public MBeanExporter keyNamingExporter() {
            MBeanExporter exporter = new MBeanExporter();
            
            // Use key properties for naming
            KeyNamingStrategy strategy = new KeyNamingStrategy();
            
            // Configure properties
            Properties mappings = new Properties();
            mappings.setProperty("type", "Service");
            mappings.setProperty("name", "UserService");
            strategy.setMappings(mappings);
            
            exporter.setNamingStrategy(strategy);
            
            return exporter;
        }
        
        /**
         * Metadata naming strategy
         */
        @Bean
        public MBeanExporter metadataNamingExporter() {
            MBeanExporter exporter = new MBeanExporter();
            
            // Use @ManagedResource annotation
            MetadataNamingStrategy strategy = new MetadataNamingStrategy();
            
            // Set default domain
            strategy.setDefaultDomain("com.example");
            
            exporter.setNamingStrategy(strategy);
            
            return exporter;
        }
        
        /**
         * Custom naming strategy
         */
        @Bean
        public MBeanExporter customNamingExporter() {
            MBeanExporter exporter = new MBeanExporter();
            
            // Use custom strategy
            CustomObjectNamingStrategy strategy = 
                new CustomObjectNamingStrategy("com.myapp", "prod");
            
            exporter.setNamingStrategy(strategy);
            
            Map<String, Object> beans = new HashMap<>();
            beans.put("userService", new UserService());
            beans.put("cacheService", new CacheService());
            exporter.setBeans(beans);
            
            return exporter;
        }
        
        /**
         * Hierarchical naming strategy
         */
        @Bean
        public MBeanExporter hierarchicalNamingExporter() {
            MBeanExporter exporter = new MBeanExporter();
            
            HierarchicalNamingStrategy strategy = 
                new HierarchicalNamingStrategy("MyApplication", "instance-01");
            
            exporter.setNamingStrategy(strategy);
            
            return exporter;
        }
        
        /**
         * Environment-aware naming strategy
         */
        @Bean
        public MBeanExporter environmentNamingExporter() {
            MBeanExporter exporter = new MBeanExporter();
            
            Map<String, String> envProps = new HashMap<>();
            envProps.put("environment", "production");
            envProps.put("region", "us-west-2");
            envProps.put("cluster", "web-cluster");
            
            EnvironmentNamingStrategy strategy = new EnvironmentNamingStrategy(envProps);
            exporter.setNamingStrategy(strategy);
            
            return exporter;
        }
    }

    /**
     * Usage examples
     */
    static class NamingStrategyExamples {
        
        public void demonstrateIdentityNaming() {
            System.out.println("\n=== Identity Naming Strategy ===");
            
            IdentityNamingStrategy strategy = new IdentityNamingStrategy();
            
            System.out.println("Uses bean identity (memory address)");
            System.out.println("Example: com.example:identity=UserService@1a2b3c4d");
            System.out.println("\nCharacteristics:");
            System.out.println("+ Guaranteed unique");
            System.out.println("- Non-deterministic (changes on restart)");
            System.out.println("- Not suitable for production monitoring");
        }
        
        public void demonstrateKeyNaming() {
            System.out.println("\n=== Key Naming Strategy ===");
            
            KeyNamingStrategy strategy = new KeyNamingStrategy();
            
            Properties mappings = new Properties();
            mappings.setProperty("type", "Service");
            mappings.setProperty("name", "UserService");
            strategy.setMappings(mappings);
            
            System.out.println("Uses configured key properties");
            System.out.println("Example: com.example:type=Service,name=UserService");
            System.out.println("\nCharacteristics:");
            System.out.println("+ Deterministic");
            System.out.println("+ Configurable");
            System.out.println("+ Production-ready");
        }
        
        public void demonstrateCustomNaming() throws MalformedObjectNameException {
            System.out.println("\n=== Custom Naming Strategy ===");
            
            CustomObjectNamingStrategy strategy = 
                new CustomObjectNamingStrategy("com.myapp", "production");
            
            UserService userService = new UserService();
            ObjectName name = strategy.getObjectName(userService, "userService");
            
            System.out.println("Custom naming logic");
            System.out.println("Generated ObjectName: " + name);
            System.out.println("\nFormat:");
            System.out.println("domain:type=ClassName,name=BeanKey,environment=Environment");
            System.out.println("\nAdvantages:");
            System.out.println("+ Full control over naming");
            System.out.println("+ Can include runtime information");
            System.out.println("+ Supports complex naming schemes");
        }
        
        public void demonstrateHierarchicalNaming() throws MalformedObjectNameException {
            System.out.println("\n=== Hierarchical Naming Strategy ===");
            
            HierarchicalNamingStrategy strategy = 
                new HierarchicalNamingStrategy("OrderProcessing", "instance-03");
            
            UserService userService = new UserService();
            ObjectName name = strategy.getObjectName(userService, "userService");
            
            System.out.println("Hierarchical organization");
            System.out.println("Generated ObjectName: " + name);
            System.out.println("\nHierarchy:");
            System.out.println("Application -> Instance -> Component -> Bean");
            System.out.println("\nBenefits:");
            System.out.println("+ Easy navigation in JMX tools");
            System.out.println("+ Logical grouping");
            System.out.println("+ Scalable for large applications");
        }
        
        public void demonstrateEnvironmentNaming() throws MalformedObjectNameException {
            System.out.println("\n=== Environment-aware Naming Strategy ===");
            
            Map<String, String> envProps = new HashMap<>();
            envProps.put("environment", "staging");
            envProps.put("region", "eu-central-1");
            envProps.put("cluster", "api-cluster");
            
            EnvironmentNamingStrategy strategy = new EnvironmentNamingStrategy(envProps);
            
            CacheService cacheService = new CacheService();
            ObjectName name = strategy.getObjectName(cacheService, "cacheService");
            
            System.out.println("Environment-aware naming");
            System.out.println("Generated ObjectName: " + name);
            System.out.println("\nUse cases:");
            System.out.println("+ Multi-environment deployments");
            System.out.println("+ Cloud deployments (region/AZ)");
            System.out.println("+ Cluster identification");
            System.out.println("+ Cross-region monitoring");
        }
    }

    public static void main(String[] args) throws MalformedObjectNameException {
        System.out.println("Naming Strategy Pattern - JMX ObjectName Generation Strategies");
        System.out.println("================================================================");
        
        NamingStrategyExamples examples = new NamingStrategyExamples();
        
        examples.demonstrateIdentityNaming();
        examples.demonstrateKeyNaming();
        examples.demonstrateCustomNaming();
        examples.demonstrateHierarchicalNaming();
        examples.demonstrateEnvironmentNaming();
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println("ObjectName Format:");
        System.out.println("domain:key1=value1,key2=value2,...");
        
        System.out.println("\nCommon Properties:");
        System.out.println("- type: Bean type/category");
        System.out.println("- name: Bean name");
        System.out.println("- service: Service identifier");
        System.out.println("- environment: Deployment environment");
        System.out.println("- region: Geographic region");
        System.out.println("- cluster: Cluster name");
        System.out.println("- application: Application name");
        System.out.println("- instance: Instance ID");
        
        System.out.println("\nBest Practices:");
        System.out.println("1. Use consistent domain names");
        System.out.println("2. Include type and name properties");
        System.out.println("3. Add environment/instance info for prod");
        System.out.println("4. Avoid special characters");
        System.out.println("5. Keep names human-readable");
        System.out.println("6. Document naming conventions");
    }
}
