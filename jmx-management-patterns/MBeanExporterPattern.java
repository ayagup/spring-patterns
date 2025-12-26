package com.example.jmx;

import org.springframework.jmx.export.MBeanExporter;
import org.springframework.jmx.export.assembler.InterfaceBasedMBeanInfoAssembler;
import org.springframework.jmx.export.naming.ObjectNamingStrategy;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.util.HashMap;
import java.util.Map;

/**
 * MBean Exporter Pattern - Exporting Spring Beans as JMX MBeans
 * 
 * MBeanExporter is the central class in Spring's JMX support that exports
 * Spring beans as JMX MBeans, making them manageable via JMX tools like
 * JConsole, VisualVM, or JMX-compliant monitoring systems.
 * 
 * Key Concepts:
 * - MBean Export: Convert Spring beans to JMX MBeans
 * - Object Naming: Define JMX ObjectNames for MBeans
 * - Auto-detection: Automatically discover beans to export
 * - Registration: Register MBeans with MBeanServer
 * - Unregistration: Clean up on shutdown
 * 
 * Configuration Methods:
 * - setBeans(Map): Explicitly set beans to export
 * - setAutodetect(boolean): Enable auto-detection
 * - setNamingStrategy(ObjectNamingStrategy): Set naming strategy
 * - setAssembler(MBeanInfoAssembler): Set info assembler
 * - setServer(MBeanServer): Set MBeanServer to use
 * - setRegistrationPolicy(RegistrationPolicy): Handle registration conflicts
 * 
 * Auto-detection Modes:
 * - AUTODETECT_NONE: No auto-detection
 * - AUTODETECT_MBEAN: Detect beans implementing MBean interfaces
 * - AUTODETECT_ASSEMBLER: Use assembler to detect
 * - AUTODETECT_ALL: Detect both MBean and assembler beans
 * 
 * Registration Policies:
 * - FAIL_ON_EXISTING: Fail if MBean already exists
 * - IGNORE_EXISTING: Skip if MBean already exists
 * - REPLACE_EXISTING: Replace existing MBean
 * 
 * Use Cases:
 * - Application monitoring
 * - Runtime configuration
 * - Performance metrics exposure
 * - Administrative operations
 * - Health checking
 * 
 * Best Practices:
 * - Use meaningful ObjectNames
 * - Document management operations
 * - Implement proper security
 * - Handle registration conflicts
 * - Clean up on shutdown
 * - Use appropriate assemblers
 */
public class MBeanExporterPattern {

    // Management interface
    public interface ApplicationConfigMBean {
        // Attributes
        String getApplicationName();
        void setApplicationName(String name);
        int getMaxConnections();
        void setMaxConnections(int max);
        boolean isDebugEnabled();
        void setDebugEnabled(boolean enabled);
        
        // Operations
        String getStatus();
        void restart();
        void clearCache();
        int getActiveUsers();
    }

    // Managed bean implementation
    static class ApplicationConfig implements ApplicationConfigMBean {
        private String applicationName = "MyApp";
        private int maxConnections = 100;
        private boolean debugEnabled = false;
        
        @Override
        public String getApplicationName() {
            return applicationName;
        }
        
        @Override
        public void setApplicationName(String name) {
            this.applicationName = name;
            System.out.println("Application name changed to: " + name);
        }
        
        @Override
        public int getMaxConnections() {
            return maxConnections;
        }
        
        @Override
        public void setMaxConnections(int max) {
            this.maxConnections = max;
            System.out.println("Max connections changed to: " + max);
        }
        
        @Override
        public boolean isDebugEnabled() {
            return debugEnabled;
        }
        
        @Override
        public void setDebugEnabled(boolean enabled) {
            this.debugEnabled = enabled;
            System.out.println("Debug mode " + (enabled ? "enabled" : "disabled"));
        }
        
        @Override
        public String getStatus() {
            return "Running - " + getActiveUsers() + " active users";
        }
        
        @Override
        public void restart() {
            System.out.println("Restarting application...");
            // Restart logic here
        }
        
        @Override
        public void clearCache() {
            System.out.println("Clearing cache...");
            // Cache clearing logic here
        }
        
        @Override
        public int getActiveUsers() {
            return 42; // Simulated value
        }
    }

    // Another managed bean
    public interface CacheManagerMBean {
        long getCacheSize();
        void evictAll();
        void evict(String key);
        double getHitRate();
        long getHitCount();
        long getMissCount();
    }

    static class CacheManager implements CacheManagerMBean {
        private long hitCount = 1000;
        private long missCount = 100;
        
        @Override
        public long getCacheSize() {
            return 1024 * 1024; // 1MB
        }
        
        @Override
        public void evictAll() {
            System.out.println("Evicting all cache entries");
        }
        
        @Override
        public void evict(String key) {
            System.out.println("Evicting cache entry: " + key);
        }
        
        @Override
        public double getHitRate() {
            long total = hitCount + missCount;
            return total > 0 ? (double) hitCount / total * 100 : 0;
        }
        
        @Override
        public long getHitCount() {
            return hitCount;
        }
        
        @Override
        public long getMissCount() {
            return missCount;
        }
    }

    /**
     * MBeanExporter Configuration Examples
     */
    static class MBeanExporterConfiguration {
        
        // Basic MBeanExporter configuration
        public MBeanExporter basicExporter() {
            MBeanExporter exporter = new MBeanExporter();
            
            // Create beans map
            Map<String, Object> beans = new HashMap<>();
            beans.put("bean:name=applicationConfig", new ApplicationConfig());
            beans.put("bean:name=cacheManager", new CacheManager());
            
            exporter.setBeans(beans);
            
            return exporter;
        }
        
        // Exporter with auto-detection
        public MBeanExporter autodetectExporter() {
            MBeanExporter exporter = new MBeanExporter();
            
            // Enable auto-detection
            exporter.setAutodetect(true);
            
            // Set auto-detection mode
            // AUTODETECT_ALL will detect all eligible beans
            exporter.setAutodetectMode(MBeanExporter.AUTODETECT_ALL);
            
            return exporter;
        }
        
        // Exporter with custom naming strategy
        public MBeanExporter namingStrategyExporter() {
            MBeanExporter exporter = new MBeanExporter();
            
            Map<String, Object> beans = new HashMap<>();
            beans.put("applicationConfig", new ApplicationConfig());
            
            exporter.setBeans(beans);
            
            // Custom naming strategy
            exporter.setNamingStrategy(new ObjectNamingStrategy() {
                @Override
                public ObjectName getObjectName(Object managedBean, String beanKey) 
                        throws MalformedObjectNameException {
                    return new ObjectName(
                        "com.example:type=" + managedBean.getClass().getSimpleName() + 
                        ",name=" + beanKey
                    );
                }
            });
            
            return exporter;
        }
        
        // Exporter with assembler
        public MBeanExporter assemblerExporter() {
            MBeanExporter exporter = new MBeanExporter();
            
            Map<String, Object> beans = new HashMap<>();
            beans.put("bean:name=config", new ApplicationConfig());
            
            exporter.setBeans(beans);
            
            // Use interface-based assembler
            InterfaceBasedMBeanInfoAssembler assembler = 
                new InterfaceBasedMBeanInfoAssembler();
            assembler.setManagedInterfaces(new Class<?>[] { ApplicationConfigMBean.class });
            
            exporter.setAssembler(assembler);
            
            return exporter;
        }
        
        // Exporter with registration policy
        public MBeanExporter registrationPolicyExporter() {
            MBeanExporter exporter = new MBeanExporter();
            
            Map<String, Object> beans = new HashMap<>();
            beans.put("bean:name=config", new ApplicationConfig());
            
            exporter.setBeans(beans);
            
            // Set registration behavior
            exporter.setRegistrationPolicy(MBeanExporter.REGISTRATION_REPLACE_EXISTING);
            
            return exporter;
        }
    }

    /**
     * Usage examples showing MBean operations
     */
    static class MBeanExporterExamples {
        
        public void demonstrateBasicExport() {
            System.out.println("\n=== Basic MBean Export ===");
            
            MBeanExporterConfiguration config = new MBeanExporterConfiguration();
            MBeanExporter exporter = config.basicExporter();
            
            System.out.println("Exported beans:");
            System.out.println("- bean:name=applicationConfig");
            System.out.println("- bean:name=cacheManager");
            System.out.println("\nThese MBeans are now accessible via JMX tools");
            System.out.println("Use JConsole or VisualVM to connect and manage");
        }
        
        public void demonstrateAutoDetection() {
            System.out.println("\n=== Auto-detection Example ===");
            
            MBeanExporterConfiguration config = new MBeanExporterConfiguration();
            MBeanExporter exporter = config.autodetectExporter();
            
            System.out.println("Auto-detection enabled");
            System.out.println("Will automatically export beans implementing:");
            System.out.println("- Standard MBean interfaces (e.g., *MBean)");
            System.out.println("- Beans detected by assembler");
        }
        
        public void demonstrateManagementOperations() {
            System.out.println("\n=== Management Operations ===");
            
            ApplicationConfig config = new ApplicationConfig();
            
            System.out.println("\nReading attributes:");
            System.out.println("Application Name: " + config.getApplicationName());
            System.out.println("Max Connections: " + config.getMaxConnections());
            System.out.println("Debug Enabled: " + config.isDebugEnabled());
            
            System.out.println("\nModifying attributes:");
            config.setApplicationName("UpdatedApp");
            config.setMaxConnections(200);
            config.setDebugEnabled(true);
            
            System.out.println("\nInvoking operations:");
            System.out.println("Status: " + config.getStatus());
            config.clearCache();
            config.restart();
        }
        
        public void demonstrateCacheManagement() {
            System.out.println("\n=== Cache Management ===");
            
            CacheManager cache = new CacheManager();
            
            System.out.println("Cache Statistics:");
            System.out.println("- Size: " + cache.getCacheSize() + " bytes");
            System.out.println("- Hit Rate: " + String.format("%.2f", cache.getHitRate()) + "%");
            System.out.println("- Hits: " + cache.getHitCount());
            System.out.println("- Misses: " + cache.getMissCount());
            
            System.out.println("\nCache Operations:");
            cache.evict("user:123");
            cache.evictAll();
        }
    }

    public static void main(String[] args) {
        System.out.println("MBean Exporter Pattern - Exporting Spring Beans as JMX MBeans");
        System.out.println("==============================================================");
        
        MBeanExporterExamples examples = new MBeanExporterExamples();
        
        examples.demonstrateBasicExport();
        examples.demonstrateAutoDetection();
        examples.demonstrateManagementOperations();
        examples.demonstrateCacheManagement();
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println("Key Features:");
        System.out.println("- setBeans(Map): Export specific beans");
        System.out.println("- setAutodetect(true): Auto-discover beans");
        System.out.println("- setNamingStrategy(): Custom ObjectName generation");
        System.out.println("- setAssembler(): Control MBean metadata");
        System.out.println("- setRegistrationPolicy(): Handle conflicts");
        
        System.out.println("\nRegistration Policies:");
        System.out.println("- FAIL_ON_EXISTING: Throw exception on conflict");
        System.out.println("- IGNORE_EXISTING: Skip if already exists");
        System.out.println("- REPLACE_EXISTING: Replace existing MBean");
        
        System.out.println("\nJMX Tools:");
        System.out.println("- JConsole: Java monitoring and management console");
        System.out.println("- VisualVM: All-in-one Java troubleshooting tool");
        System.out.println("- JMC: Java Mission Control");
        System.out.println("- Custom JMX clients using JMX APIs");
    }
}
