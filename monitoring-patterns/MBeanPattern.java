package com.example.monitoring.mbean;

import javax.management.*;
import javax.management.openmbean.*;
import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * MBean Pattern - Management Bean Creation and Registration
 * 
 * Demonstrates:
 * 1. Standard MBean interface and implementation
 * 2. Dynamic MBean with runtime attributes
 * 3. Model MBean with metadata
 * 4. MBean notifications
 * 5. MBean registration and lifecycle
 * 6. Composite and Tabular data types
 * 7. MBean operations with parameters
 * 8. Attribute change notifications
 */
public class MBeanPattern {

    public static void main(String[] args) throws Exception {
        System.out.println("=== MBean Pattern Demo ===\n");

        MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();

        // 1. Standard MBean
        demonstrateStandardMBean(mbeanServer);

        // 2. Dynamic MBean
        demonstrateDynamicMBean(mbeanServer);

        // 3. Model MBean
        demonstrateModelMBean(mbeanServer);

        // 4. MBean with Notifications
        demonstrateNotificationMBean(mbeanServer);

        // 5. Composite and Tabular Data
        demonstrateComplexDataTypes(mbeanServer);

        System.out.println("\n=== MBean Pattern Demo Completed ===");
        System.out.println("MBeans remain registered for monitoring (30 seconds)...");
        
        Thread.sleep(30000);

        // Cleanup
        cleanup(mbeanServer);
    }

    private static void demonstrateStandardMBean(MBeanServer mbeanServer) throws Exception {
        System.out.println("1. Standard MBean Example\n");

        // Create and register standard MBean
        ApplicationConfig appConfig = new ApplicationConfig();
        ObjectName objectName = new ObjectName("com.example:type=Config,name=Application");
        mbeanServer.registerMBean(appConfig, objectName);

        System.out.println("   Registered: " + objectName);
        System.out.println("   Application Name: " + appConfig.getApplicationName());
        System.out.println("   Max Connections: " + appConfig.getMaxConnections());
        
        // Modify via MBean
        appConfig.setMaxConnections(200);
        System.out.println("   Updated Max Connections: " + appConfig.getMaxConnections());
        
        // Invoke operation
        String status = appConfig.getStatus();
        System.out.println("   Status: " + status);
        
        appConfig.restart();
        System.out.println("   Application restarted\n");
    }

    private static void demonstrateDynamicMBean(MBeanServer mbeanServer) throws Exception {
        System.out.println("2. Dynamic MBean Example\n");

        DatabaseConnectionPool pool = new DatabaseConnectionPool();
        ObjectName objectName = new ObjectName("com.example:type=ConnectionPool,name=Database");
        mbeanServer.registerMBean(pool, objectName);

        System.out.println("   Registered: " + objectName);
        System.out.println("   Pool Size: " + pool.getAttribute("PoolSize"));
        System.out.println("   Active Connections: " + pool.getAttribute("ActiveConnections"));
        
        // Modify attribute
        pool.setAttribute(new Attribute("PoolSize", 50));
        System.out.println("   Updated Pool Size: " + pool.getAttribute("PoolSize"));
        
        // Invoke operation
        Object result = pool.invoke("getStatistics", null, null);
        System.out.println("   Statistics: " + result + "\n");
    }

    private static void demonstrateModelMBean(MBeanServer mbeanServer) throws Exception {
        System.out.println("3. Model MBean Example\n");

        CacheManager cacheManager = new CacheManager();
        ObjectName objectName = new ObjectName("com.example:type=Cache,name=Manager");
        
        // Create Model MBean
        ModelMBean modelMBean = new RequiredModelMBean();
        modelMBean.setManagedResource(cacheManager, "ObjectReference");

        // Build MBean info
        ModelMBeanInfo mbeanInfo = createCacheManagerMBeanInfo();
        modelMBean.setModelMBeanInfo(mbeanInfo);

        mbeanServer.registerMBean(modelMBean, objectName);

        System.out.println("   Registered: " + objectName);
        System.out.println("   Cache Size: " + mbeanServer.getAttribute(objectName, "Size"));
        
        // Invoke operations
        mbeanServer.invoke(objectName, "put", 
            new Object[]{"key1", "value1"}, 
            new String[]{"java.lang.String", "java.lang.String"});
        
        System.out.println("   Added entry to cache");
        System.out.println("   New Cache Size: " + mbeanServer.getAttribute(objectName, "Size"));
        
        mbeanServer.invoke(objectName, "clear", null, null);
        System.out.println("   Cache cleared\n");
    }

    private static void demonstrateNotificationMBean(MBeanServer mbeanServer) throws Exception {
        System.out.println("4. MBean with Notifications\n");

        SystemMonitor monitor = new SystemMonitor();
        ObjectName objectName = new ObjectName("com.example:type=Monitor,name=System");
        mbeanServer.registerMBean(monitor, objectName);

        // Add notification listener
        NotificationListener listener = new NotificationListener() {
            @Override
            public void handleNotification(Notification notification, Object handback) {
                System.out.println("   Received: " + notification.getType() + 
                                 " - " + notification.getMessage());
            }
        };

        mbeanServer.addNotificationListener(objectName, listener, null, null);

        System.out.println("   Registered: " + objectName);
        System.out.println("   Simulating system events...");
        
        // Trigger notifications
        monitor.checkMemory();
        monitor.checkCpu();
        monitor.alert("Test Alert");
        
        System.out.println();
    }

    private static void demonstrateComplexDataTypes(MBeanServer mbeanServer) throws Exception {
        System.out.println("5. Composite and Tabular Data Types\n");

        ServerStatistics stats = new ServerStatistics();
        ObjectName objectName = new ObjectName("com.example:type=Statistics,name=Server");
        mbeanServer.registerMBean(stats, objectName);

        System.out.println("   Registered: " + objectName);
        
        // Get composite data
        CompositeData memoryInfo = (CompositeData) stats.getAttribute("MemoryInfo");
        System.out.println("   Memory Info:");
        System.out.println("     Total: " + memoryInfo.get("total") + " MB");
        System.out.println("     Used: " + memoryInfo.get("used") + " MB");
        System.out.println("     Free: " + memoryInfo.get("free") + " MB");
        
        // Get tabular data
        TabularData requestStats = (TabularData) stats.getAttribute("RequestStatistics");
        System.out.println("   Request Statistics:");
        for (Object value : requestStats.values()) {
            CompositeData row = (CompositeData) value;
            System.out.println("     " + row.get("endpoint") + ": " + 
                             row.get("count") + " requests, " + 
                             row.get("avgTime") + "ms avg");
        }
        System.out.println();
    }

    private static ModelMBeanInfo createCacheManagerMBeanInfo() throws Exception {
        // Attributes
        ModelMBeanAttributeInfo sizeAttr = new ModelMBeanAttributeInfo(
            "Size", "int", "Cache size",
            true, false, false,
            createDescriptor("getSize", "size")
        );

        // Operations
        ModelMBeanOperationInfo putOp = new ModelMBeanOperationInfo(
            "put", "Put entry in cache",
            new MBeanParameterInfo[] {
                new MBeanParameterInfo("key", "java.lang.String", "Cache key"),
                new MBeanParameterInfo("value", "java.lang.String", "Cache value")
            },
            "void", MBeanOperationInfo.ACTION,
            createDescriptor("put", null)
        );

        ModelMBeanOperationInfo clearOp = new ModelMBeanOperationInfo(
            "clear", "Clear cache",
            null, "void", MBeanOperationInfo.ACTION,
            createDescriptor("clear", null)
        );

        return new ModelMBeanInfoSupport(
            CacheManager.class.getName(),
            "Cache Manager MBean",
            new ModelMBeanAttributeInfo[]{sizeAttr},
            null,
            new ModelMBeanOperationInfo[]{putOp, clearOp},
            null
        );
    }

    private static Descriptor createDescriptor(String name, String role) {
        Descriptor descriptor = new DescriptorSupport();
        descriptor.setField("name", name);
        descriptor.setField("descriptorType", role != null ? "attribute" : "operation");
        if (role != null) {
            descriptor.setField("getMethod", name);
        }
        return descriptor;
    }

    private static void cleanup(MBeanServer mbeanServer) throws Exception {
        Set<ObjectName> mbeans = mbeanServer.queryNames(
            new ObjectName("com.example:*"), null);
        
        for (ObjectName name : mbeans) {
            try {
                mbeanServer.unregisterMBean(name);
            } catch (Exception e) {
                // Ignore
            }
        }
    }

    // ==================== Standard MBean ====================

    /**
     * Standard MBean Interface
     * Must end with "MBean" suffix
     */
    public interface ApplicationConfigMBean {
        String getApplicationName();
        void setApplicationName(String name);
        
        int getMaxConnections();
        void setMaxConnections(int max);
        
        boolean isDebugEnabled();
        void setDebugEnabled(boolean enabled);
        
        String getStatus();
        void restart();
    }

    /**
     * Standard MBean Implementation
     */
    public static class ApplicationConfig implements ApplicationConfigMBean {
        private String applicationName = "MyApplication";
        private int maxConnections = 100;
        private boolean debugEnabled = false;
        private long startTime = System.currentTimeMillis();

        @Override
        public String getApplicationName() {
            return applicationName;
        }

        @Override
        public void setApplicationName(String name) {
            this.applicationName = name;
        }

        @Override
        public int getMaxConnections() {
            return maxConnections;
        }

        @Override
        public void setMaxConnections(int max) {
            if (max < 10 || max > 1000) {
                throw new IllegalArgumentException("Max connections must be between 10 and 1000");
            }
            this.maxConnections = max;
        }

        @Override
        public boolean isDebugEnabled() {
            return debugEnabled;
        }

        @Override
        public void setDebugEnabled(boolean enabled) {
            this.debugEnabled = enabled;
        }

        @Override
        public String getStatus() {
            long uptime = (System.currentTimeMillis() - startTime) / 1000;
            return String.format("Running (uptime: %d seconds)", uptime);
        }

        @Override
        public void restart() {
            this.startTime = System.currentTimeMillis();
            System.out.println("   [ApplicationConfig] Restart executed");
        }
    }

    // ==================== Dynamic MBean ====================

    /**
     * Dynamic MBean - Connection Pool Monitor
     */
    public static class DatabaseConnectionPool implements DynamicMBean {
        private final AtomicInteger poolSize = new AtomicInteger(20);
        private final AtomicInteger activeConnections = new AtomicInteger(0);
        private final AtomicLong totalQueries = new AtomicLong(0);

        @Override
        public Object getAttribute(String attribute) throws AttributeNotFoundException {
            switch (attribute) {
                case "PoolSize":
                    return poolSize.get();
                case "ActiveConnections":
                    return activeConnections.get();
                case "TotalQueries":
                    return totalQueries.get();
                default:
                    throw new AttributeNotFoundException("Attribute not found: " + attribute);
            }
        }

        @Override
        public void setAttribute(Attribute attribute) throws AttributeNotFoundException {
            String name = attribute.getName();
            Object value = attribute.getValue();
            
            switch (name) {
                case "PoolSize":
                    poolSize.set((Integer) value);
                    break;
                case "ActiveConnections":
                    activeConnections.set((Integer) value);
                    break;
                default:
                    throw new AttributeNotFoundException("Cannot set attribute: " + name);
            }
        }

        @Override
        public AttributeList getAttributes(String[] attributes) {
            AttributeList list = new AttributeList();
            for (String attr : attributes) {
                try {
                    list.add(new Attribute(attr, getAttribute(attr)));
                } catch (AttributeNotFoundException e) {
                    // Skip
                }
            }
            return list;
        }

        @Override
        public AttributeList setAttributes(AttributeList attributes) {
            AttributeList list = new AttributeList();
            for (Object obj : attributes) {
                Attribute attr = (Attribute) obj;
                try {
                    setAttribute(attr);
                    list.add(attr);
                } catch (AttributeNotFoundException e) {
                    // Skip
                }
            }
            return list;
        }

        @Override
        public Object invoke(String actionName, Object[] params, String[] signature) 
                throws MBeanException, ReflectionException {
            switch (actionName) {
                case "getStatistics":
                    return String.format("Pool: %d, Active: %d, Queries: %d",
                        poolSize.get(), activeConnections.get(), totalQueries.get());
                case "resetCounters":
                    totalQueries.set(0);
                    return "Counters reset";
                default:
                    throw new ReflectionException(
                        new NoSuchMethodException(actionName));
            }
        }

        @Override
        public MBeanInfo getMBeanInfo() {
            MBeanAttributeInfo[] attributes = new MBeanAttributeInfo[] {
                new MBeanAttributeInfo("PoolSize", "int", "Connection pool size", 
                    true, true, false),
                new MBeanAttributeInfo("ActiveConnections", "int", "Active connections", 
                    true, true, false),
                new MBeanAttributeInfo("TotalQueries", "long", "Total queries executed", 
                    true, false, false)
            };

            MBeanOperationInfo[] operations = new MBeanOperationInfo[] {
                new MBeanOperationInfo("getStatistics", "Get pool statistics",
                    null, "java.lang.String", MBeanOperationInfo.INFO),
                new MBeanOperationInfo("resetCounters", "Reset query counters",
                    null, "java.lang.String", MBeanOperationInfo.ACTION)
            };

            return new MBeanInfo(
                this.getClass().getName(),
                "Database Connection Pool Monitor",
                attributes, null, operations, null
            );
        }
    }

    // ==================== Model MBean Resource ====================

    /**
     * Cache Manager - Resource for Model MBean
     */
    public static class CacheManager {
        private final Map<String, String> cache = new ConcurrentHashMap<>();

        public int getSize() {
            return cache.size();
        }

        public void put(String key, String value) {
            cache.put(key, value);
        }

        public String get(String key) {
            return cache.get(key);
        }

        public void clear() {
            cache.clear();
        }
    }

    // ==================== Notification MBean ====================

    /**
     * System Monitor with Notifications
     */
    public static class SystemMonitor extends NotificationBroadcasterSupport 
            implements DynamicMBean {
        
        private final AtomicLong sequenceNumber = new AtomicLong(0);
        private final AtomicInteger memoryUsage = new AtomicInteger(65);
        private final AtomicInteger cpuUsage = new AtomicInteger(45);

        @Override
        public Object getAttribute(String attribute) throws AttributeNotFoundException {
            switch (attribute) {
                case "MemoryUsage":
                    return memoryUsage.get();
                case "CpuUsage":
                    return cpuUsage.get();
                default:
                    throw new AttributeNotFoundException(attribute);
            }
        }

        @Override
        public void setAttribute(Attribute attribute) throws AttributeNotFoundException {
            throw new AttributeNotFoundException("No writable attributes");
        }

        @Override
        public AttributeList getAttributes(String[] attributes) {
            AttributeList list = new AttributeList();
            for (String attr : attributes) {
                try {
                    list.add(new Attribute(attr, getAttribute(attr)));
                } catch (AttributeNotFoundException e) {
                    // Skip
                }
            }
            return list;
        }

        @Override
        public AttributeList setAttributes(AttributeList attributes) {
            return new AttributeList();
        }

        @Override
        public Object invoke(String actionName, Object[] params, String[] signature) 
                throws MBeanException, ReflectionException {
            switch (actionName) {
                case "checkMemory":
                    return checkMemory();
                case "checkCpu":
                    return checkCpu();
                case "alert":
                    return alert((String) params[0]);
                default:
                    throw new ReflectionException(new NoSuchMethodException(actionName));
            }
        }

        @Override
        public MBeanInfo getMBeanInfo() {
            MBeanAttributeInfo[] attributes = new MBeanAttributeInfo[] {
                new MBeanAttributeInfo("MemoryUsage", "int", "Memory usage percentage", 
                    true, false, false),
                new MBeanAttributeInfo("CpuUsage", "int", "CPU usage percentage", 
                    true, false, false)
            };

            MBeanOperationInfo[] operations = new MBeanOperationInfo[] {
                new MBeanOperationInfo("checkMemory", "Check memory usage",
                    null, "java.lang.String", MBeanOperationInfo.ACTION),
                new MBeanOperationInfo("checkCpu", "Check CPU usage",
                    null, "java.lang.String", MBeanOperationInfo.ACTION),
                new MBeanOperationInfo("alert", "Send alert",
                    new MBeanParameterInfo[] {
                        new MBeanParameterInfo("message", "java.lang.String", "Alert message")
                    }, "java.lang.String", MBeanOperationInfo.ACTION)
            };

            MBeanNotificationInfo[] notifications = new MBeanNotificationInfo[] {
                new MBeanNotificationInfo(
                    new String[] {"system.memory.high", "system.cpu.high", "system.alert"},
                    Notification.class.getName(),
                    "System monitoring notifications"
                )
            };

            return new MBeanInfo(
                this.getClass().getName(),
                "System Monitor with Notifications",
                attributes, null, operations, notifications
            );
        }

        public String checkMemory() {
            int usage = memoryUsage.get();
            if (usage > 80) {
                sendNotification("system.memory.high", 
                    "Memory usage is high: " + usage + "%");
                return "WARNING: High memory usage";
            }
            return "Memory usage normal: " + usage + "%";
        }

        public String checkCpu() {
            int usage = cpuUsage.get();
            if (usage > 70) {
                sendNotification("system.cpu.high", 
                    "CPU usage is high: " + usage + "%");
                return "WARNING: High CPU usage";
            }
            return "CPU usage normal: " + usage + "%";
        }

        public String alert(String message) {
            sendNotification("system.alert", message);
            return "Alert sent: " + message;
        }

        private void sendNotification(String type, String message) {
            Notification notification = new Notification(
                type,
                this,
                sequenceNumber.incrementAndGet(),
                System.currentTimeMillis(),
                message
            );
            sendNotification(notification);
        }
    }

    // ==================== Composite and Tabular Data ====================

    /**
     * Server Statistics with Complex Data Types
     */
    public static class ServerStatistics implements DynamicMBean {

        @Override
        public Object getAttribute(String attribute) throws AttributeNotFoundException {
            try {
                switch (attribute) {
                    case "MemoryInfo":
                        return getMemoryInfo();
                    case "RequestStatistics":
                        return getRequestStatistics();
                    default:
                        throw new AttributeNotFoundException(attribute);
                }
            } catch (OpenDataException e) {
                throw new AttributeNotFoundException(e.getMessage());
            }
        }

        private CompositeData getMemoryInfo() throws OpenDataException {
            String[] itemNames = {"total", "used", "free"};
            String[] itemDescriptions = {"Total memory", "Used memory", "Free memory"};
            OpenType<?>[] itemTypes = {SimpleType.LONG, SimpleType.LONG, SimpleType.LONG};

            CompositeType compositeType = new CompositeType(
                "MemoryInfo", "Memory Information",
                itemNames, itemDescriptions, itemTypes
            );

            Object[] itemValues = {1024L, 650L, 374L};
            return new CompositeDataSupport(compositeType, itemNames, itemValues);
        }

        private TabularData getRequestStatistics() throws OpenDataException {
            String[] itemNames = {"endpoint", "count", "avgTime"};
            String[] itemDescriptions = {"API Endpoint", "Request Count", "Average Time"};
            OpenType<?>[] itemTypes = {SimpleType.STRING, SimpleType.LONG, SimpleType.DOUBLE};

            CompositeType rowType = new CompositeType(
                "RequestStat", "Request Statistics",
                itemNames, itemDescriptions, itemTypes
            );

            TabularType tabularType = new TabularType(
                "RequestStatistics", "Request Statistics Table",
                rowType, new String[]{"endpoint"}
            );

            TabularDataSupport tabularData = new TabularDataSupport(tabularType);

            // Add sample data
            tabularData.put(new CompositeDataSupport(rowType, itemNames, 
                new Object[]{"/api/users", 1250L, 45.3}));
            tabularData.put(new CompositeDataSupport(rowType, itemNames, 
                new Object[]{"/api/orders", 890L, 78.5}));
            tabularData.put(new CompositeDataSupport(rowType, itemNames, 
                new Object[]{"/api/products", 2100L, 32.1}));

            return tabularData;
        }

        @Override
        public void setAttribute(Attribute attribute) throws AttributeNotFoundException {
            throw new AttributeNotFoundException("No writable attributes");
        }

        @Override
        public AttributeList getAttributes(String[] attributes) {
            AttributeList list = new AttributeList();
            for (String attr : attributes) {
                try {
                    list.add(new Attribute(attr, getAttribute(attr)));
                } catch (AttributeNotFoundException e) {
                    // Skip
                }
            }
            return list;
        }

        @Override
        public AttributeList setAttributes(AttributeList attributes) {
            return new AttributeList();
        }

        @Override
        public Object invoke(String actionName, Object[] params, String[] signature) 
                throws MBeanException, ReflectionException {
            throw new ReflectionException(new NoSuchMethodException(actionName));
        }

        @Override
        public MBeanInfo getMBeanInfo() {
            MBeanAttributeInfo[] attributes = new MBeanAttributeInfo[] {
                new MBeanAttributeInfo("MemoryInfo", 
                    CompositeData.class.getName(), 
                    "Memory information (total, used, free)", 
                    true, false, false),
                new MBeanAttributeInfo("RequestStatistics", 
                    TabularData.class.getName(), 
                    "Request statistics by endpoint", 
                    true, false, false)
            };

            return new MBeanInfo(
                this.getClass().getName(),
                "Server Statistics with Complex Data Types",
                attributes, null, null, null
            );
        }
    }
}
