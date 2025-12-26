package com.example.jmx;

import org.springframework.jmx.support.MBeanServerFactoryBean;
import org.springframework.jmx.support.MBeanServerConnectionFactoryBean;
import org.springframework.jmx.export.MBeanExporter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.management.*;
import javax.management.remote.*;
import java.io.IOException;
import java.util.*;

/**
 * MBean Server Pattern - MBeanServer Access and Management
 * 
 * The MBeanServer is the core component of JMX, acting as a registry
 * for MBeans. Spring provides several ways to access and configure
 * the MBeanServer, both for local and remote JMX connectivity.
 * 
 * Key Components:
 * 
 * 1. MBeanServerFactoryBean:
 *    - Creates/locates MBeanServer
 *    - Configures server behavior
 *    - Manages server lifecycle
 * 
 * 2. MBeanServerConnectionFactoryBean:
 *    - Creates remote MBeanServer connections
 *    - Connects to remote JMX servers
 *    - Supports JMX remoting
 * 
 * 3. MBeanServer:
 *    - Standard JMX MBean registry
 *    - registerMBean/unregisterMBean
 *    - getAttribute/setAttribute
 *    - invoke operations
 *    - query MBeans
 * 
 * 4. JMXConnectorServer:
 *    - Enables remote JMX access
 *    - Supports multiple protocols
 *    - Configurable authentication
 * 
 * MBeanServer Configuration:
 * - locateExistingServerIfPossible: Find platform MBeanServer
 * - registerWithFactory: Register with MBeanServerFactory
 * - defaultDomain: Default domain for MBeans
 * - agentId: Server identifier
 * 
 * Remote JMX:
 * - Service URL format: service:jmx:rmi://host:port/jndi/rmi://host:port/jmxrmi
 * - Protocols: RMI, JMXMP, HTTP
 * - Authentication: password, SSL, custom
 * - Authorization: role-based access
 * 
 * Common Operations:
 * - registerMBean: Register new MBean
 * - unregisterMBean: Remove MBean
 * - getMBeanInfo: Get MBean metadata
 * - getAttribute: Read attribute value
 * - setAttribute: Write attribute value
 * - invoke: Execute operation
 * - queryMBeans: Find MBeans by pattern
 * - queryNames: Get MBean ObjectNames
 * 
 * Use Cases:
 * - Programmatic MBean registration
 * - Dynamic MBean creation
 * - Remote monitoring
 * - Custom JMX tools
 * - MBean queries and filtering
 * - Batch operations
 * 
 * Best Practices:
 * - Use platform MBeanServer
 * - Enable remote JMX securely
 * - Configure authentication
 * - Use SSL for production
 * - Limit exposed operations
 * - Monitor server metrics
 */
public class MBeanServerPattern {

    /**
     * Simple managed bean
     */
    public interface ApplicationMBean {
        String getName();
        void setName(String name);
        String getStatus();
        void restart();
    }

    static class Application implements ApplicationMBean {
        private String name = "MyApp";
        private String status = "RUNNING";
        
        @Override
        public String getName() {
            return name;
        }
        
        @Override
        public void setName(String name) {
            this.name = name;
        }
        
        @Override
        public String getStatus() {
            return status;
        }
        
        @Override
        public void restart() {
            status = "RESTARTING";
            System.out.println("Application restarting...");
            status = "RUNNING";
        }
    }

    /**
     * MBeanServer configuration
     */
    @Configuration
    static class MBeanServerConfiguration {
        
        /**
         * Create/locate platform MBeanServer
         */
        @Bean
        public MBeanServerFactoryBean mbeanServer() {
            MBeanServerFactoryBean factory = new MBeanServerFactoryBean();
            
            // Try to locate existing platform MBeanServer
            factory.setLocateExistingServerIfPossible(true);
            
            // Register with MBeanServerFactory
            factory.setRegisterWithFactory(true);
            
            // Set default domain
            factory.setDefaultDomain("com.example");
            
            return factory;
        }
        
        /**
         * MBeanExporter with custom server
         */
        @Bean
        public MBeanExporter mbeanExporter(MBeanServer mbeanServer) {
            MBeanExporter exporter = new MBeanExporter();
            
            // Use specific MBeanServer
            exporter.setServer(mbeanServer);
            
            // Register beans
            Map<String, Object> beans = new HashMap<>();
            beans.put("com.example:type=Application,name=MyApp", new Application());
            exporter.setBeans(beans);
            
            return exporter;
        }
        
        /**
         * Remote MBeanServer connection
         */
        @Bean
        public MBeanServerConnectionFactoryBean remoteMBeanServerConnection() 
                throws MalformedURLException {
            
            MBeanServerConnectionFactoryBean factory = 
                new MBeanServerConnectionFactoryBean();
            
            // Remote JMX service URL
            JMXServiceURL serviceUrl = new JMXServiceURL(
                "service:jmx:rmi:///jndi/rmi://localhost:9999/jmxrmi"
            );
            factory.setServiceUrl(serviceUrl);
            
            // Optional: authentication
            Map<String, Object> environment = new HashMap<>();
            environment.put(JMXConnector.CREDENTIALS, new String[]{"admin", "password"});
            factory.setEnvironment(environment);
            
            // Connection timeout
            factory.setConnectOnStartup(false);
            
            return factory;
        }
    }

    /**
     * Programmatic MBeanServer usage
     */
    static class MBeanServerOperations {
        
        private MBeanServer mbeanServer;
        
        public MBeanServerOperations(MBeanServer mbeanServer) {
            this.mbeanServer = mbeanServer;
        }
        
        /**
         * Register MBean programmatically
         */
        public ObjectName registerMBean(Object mbean, String objectNameStr) 
                throws Exception {
            
            ObjectName objectName = new ObjectName(objectNameStr);
            mbeanServer.registerMBean(mbean, objectName);
            
            System.out.println("Registered MBean: " + objectName);
            return objectName;
        }
        
        /**
         * Unregister MBean
         */
        public void unregisterMBean(ObjectName objectName) throws Exception {
            mbeanServer.unregisterMBean(objectName);
            System.out.println("Unregistered MBean: " + objectName);
        }
        
        /**
         * Get attribute value
         */
        public Object getAttribute(ObjectName objectName, String attributeName) 
                throws Exception {
            
            Object value = mbeanServer.getAttribute(objectName, attributeName);
            System.out.println("Attribute " + attributeName + " = " + value);
            return value;
        }
        
        /**
         * Set attribute value
         */
        public void setAttribute(ObjectName objectName, String attributeName, Object value) 
                throws Exception {
            
            Attribute attribute = new Attribute(attributeName, value);
            mbeanServer.setAttribute(objectName, attribute);
            System.out.println("Set attribute " + attributeName + " = " + value);
        }
        
        /**
         * Invoke operation
         */
        public Object invokeOperation(ObjectName objectName, String operationName, 
                                     Object[] params, String[] signature) throws Exception {
            
            Object result = mbeanServer.invoke(objectName, operationName, params, signature);
            System.out.println("Invoked " + operationName + ", result: " + result);
            return result;
        }
        
        /**
         * Query MBeans by pattern
         */
        public Set<ObjectName> queryMBeans(String pattern) throws Exception {
            ObjectName queryName = new ObjectName(pattern);
            Set<ObjectName> names = mbeanServer.queryNames(queryName, null);
            
            System.out.println("Found " + names.size() + " MBeans matching: " + pattern);
            for (ObjectName name : names) {
                System.out.println("  - " + name);
            }
            
            return names;
        }
        
        /**
         * Get MBean info
         */
        public MBeanInfo getMBeanInfo(ObjectName objectName) throws Exception {
            MBeanInfo info = mbeanServer.getMBeanInfo(objectName);
            
            System.out.println("\nMBean Info for: " + objectName);
            System.out.println("Class: " + info.getClassName());
            System.out.println("Description: " + info.getDescription());
            
            System.out.println("\nAttributes:");
            for (MBeanAttributeInfo attr : info.getAttributes()) {
                System.out.println("  - " + attr.getName() + " (" + attr.getType() + ")");
            }
            
            System.out.println("\nOperations:");
            for (MBeanOperationInfo op : info.getOperations()) {
                System.out.println("  - " + op.getName() + "()");
            }
            
            return info;
        }
        
        /**
         * Check if MBean is registered
         */
        public boolean isRegistered(ObjectName objectName) {
            boolean registered = mbeanServer.isRegistered(objectName);
            System.out.println("MBean " + objectName + " registered: " + registered);
            return registered;
        }
        
        /**
         * Get MBean count
         */
        public int getMBeanCount() {
            int count = mbeanServer.getMBeanCount();
            System.out.println("Total MBeans: " + count);
            return count;
        }
        
        /**
         * Get all domains
         */
        public String[] getDomains() {
            String[] domains = mbeanServer.getDomains();
            System.out.println("Domains: " + Arrays.toString(domains));
            return domains;
        }
    }

    /**
     * Remote JMX server setup
     */
    static class RemoteJMXServer {
        
        private JMXConnectorServer connectorServer;
        private MBeanServer mbeanServer;
        
        public void start(int port) throws IOException {
            // Get platform MBeanServer
            mbeanServer = ManagementFactory.getPlatformMBeanServer();
            
            // Create service URL
            String serviceUrl = String.format(
                "service:jmx:rmi:///jndi/rmi://localhost:%d/jmxrmi",
                port
            );
            JMXServiceURL url = new JMXServiceURL(serviceUrl);
            
            // Create connector server
            connectorServer = JMXConnectorServerFactory.newJMXConnectorServer(
                url,
                null,
                mbeanServer
            );
            
            // Start server
            connectorServer.start();
            System.out.println("JMX Connector Server started: " + serviceUrl);
        }
        
        public void stop() throws IOException {
            if (connectorServer != null) {
                connectorServer.stop();
                System.out.println("JMX Connector Server stopped");
            }
        }
        
        public void registerMBean(Object mbean, String objectName) throws Exception {
            ObjectName name = new ObjectName(objectName);
            mbeanServer.registerMBean(mbean, name);
            System.out.println("Registered remote MBean: " + objectName);
        }
    }

    /**
     * Remote JMX client
     */
    static class RemoteJMXClient {
        
        private JMXConnector connector;
        private MBeanServerConnection mbeanServerConnection;
        
        public void connect(String host, int port) throws IOException {
            String serviceUrl = String.format(
                "service:jmx:rmi:///jndi/rmi://%s:%d/jmxrmi",
                host,
                port
            );
            
            JMXServiceURL url = new JMXServiceURL(serviceUrl);
            connector = JMXConnectorFactory.connect(url);
            mbeanServerConnection = connector.getMBeanServerConnection();
            
            System.out.println("Connected to remote JMX server: " + serviceUrl);
        }
        
        public void disconnect() throws IOException {
            if (connector != null) {
                connector.close();
                System.out.println("Disconnected from remote JMX server");
            }
        }
        
        public Object getAttribute(String objectName, String attributeName) 
                throws Exception {
            
            ObjectName name = new ObjectName(objectName);
            Object value = mbeanServerConnection.getAttribute(name, attributeName);
            System.out.println("Remote attribute " + attributeName + " = " + value);
            return value;
        }
        
        public void invoke(String objectName, String operationName) throws Exception {
            ObjectName name = new ObjectName(objectName);
            mbeanServerConnection.invoke(name, operationName, null, null);
            System.out.println("Invoked remote operation: " + operationName);
        }
    }

    /**
     * Usage examples
     */
    static class MBeanServerExamples {
        
        public void demonstrateBasicOperations() throws Exception {
            System.out.println("\n=== Basic MBeanServer Operations ===");
            
            // Get platform MBeanServer
            MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
            MBeanServerOperations ops = new MBeanServerOperations(mbeanServer);
            
            // Register MBean
            Application app = new Application();
            ObjectName objectName = ops.registerMBean(app, 
                "com.example:type=Application,name=TestApp");
            
            // Get attribute
            ops.getAttribute(objectName, "Name");
            ops.getAttribute(objectName, "Status");
            
            // Set attribute
            ops.setAttribute(objectName, "Name", "UpdatedApp");
            
            // Invoke operation
            ops.invokeOperation(objectName, "restart", null, null);
            
            // Get MBean info
            ops.getMBeanInfo(objectName);
            
            // Unregister
            ops.unregisterMBean(objectName);
        }
        
        public void demonstrateQuerying() throws Exception {
            System.out.println("\n=== MBean Querying ===");
            
            MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
            MBeanServerOperations ops = new MBeanServerOperations(mbeanServer);
            
            // Query all MBeans in domain
            ops.queryMBeans("java.lang:*");
            
            // Query specific type
            ops.queryMBeans("java.lang:type=Memory");
            
            // Get domains
            ops.getDomains();
            
            // Get count
            ops.getMBeanCount();
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("MBean Server Pattern - MBeanServer Access and Management");
        System.out.println("==========================================================");
        
        MBeanServerExamples examples = new MBeanServerExamples();
        
        examples.demonstrateBasicOperations();
        examples.demonstrateQuerying();
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println("MBeanServer Operations:");
        System.out.println("- registerMBean: Register new MBean");
        System.out.println("- unregisterMBean: Remove MBean");
        System.out.println("- getAttribute: Read attribute");
        System.out.println("- setAttribute: Write attribute");
        System.out.println("- invoke: Execute operation");
        System.out.println("- queryMBeans: Find MBeans");
        System.out.println("- getMBeanInfo: Get metadata");
        
        System.out.println("\nRemote JMX URL Format:");
        System.out.println("service:jmx:rmi:///jndi/rmi://host:port/jmxrmi");
        
        System.out.println("\nSecurity Considerations:");
        System.out.println("- Enable authentication");
        System.out.println("- Use SSL/TLS");
        System.out.println("- Configure firewall");
        System.out.println("- Limit exposed operations");
        System.out.println("- Monitor access logs");
    }
}
