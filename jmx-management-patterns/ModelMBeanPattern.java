package com.example.jmx;

import org.springframework.jmx.export.MBeanExporter;
import org.springframework.jmx.support.MBeanRegistrationSupport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.management.*;
import javax.management.modelmbean.*;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Model MBean Pattern - Dynamic MBean Creation
 * 
 * ModelMBean is a dynamic MBean implementation that allows runtime
 * configuration of management interfaces without implementing specific
 * interfaces. Spring supports ModelMBeans through various mechanisms.
 * 
 * Key Concepts:
 * 
 * 1. ModelMBean:
 *    - Dynamic MBean implementation
 *    - Runtime metadata configuration
 *    - Flexible attribute/operation exposure
 *    - Support for caching and persistence
 * 
 * 2. ModelMBeanInfo:
 *    - Metadata descriptor
 *    - Attributes, operations, notifications
 *    - Descriptors for each element
 * 
 * 3. Descriptors:
 *    - name: Element name
 *    - descriptorType: Type (attribute, operation, mbean, notification)
 *    - displayName: Display name
 *    - getMethod/setMethod: Getter/setter method names
 *    - currencyTimeLimit: Cache validity
 *    - default: Default value
 *    - class: Class name
 *    - role: Attribute role (read, write, readwrite)
 * 
 * ModelMBean vs Standard MBean:
 * 
 * Standard MBean:
 * - Implements specific interface
 * - Compile-time definition
 * - Simpler to implement
 * - Less flexible
 * 
 * Model MBean:
 * - No interface required
 * - Runtime configuration
 * - More complex setup
 * - Highly flexible
 * - Supports caching/persistence
 * 
 * Attribute Descriptors:
 * - name: Attribute name
 * - descriptorType: "attribute"
 * - displayName: Display name
 * - getMethod: Getter method name
 * - setMethod: Setter method name (optional)
 * - currencyTimeLimit: Cache time in seconds
 * - default: Default value
 * 
 * Operation Descriptors:
 * - name: Operation name
 * - descriptorType: "operation"
 * - displayName: Display name
 * - role: operation, getter, setter
 * - class: Class implementing operation
 * 
 * Use Cases:
 * - Dynamic bean management
 * - Legacy system integration
 * - Custom metadata requirements
 * - Complex caching strategies
 * - Runtime MBean definition
 * - Proxying existing objects
 * 
 * Best Practices:
 * - Use standard MBeans when possible
 * - Document descriptor fields
 * - Configure appropriate cache times
 * - Validate metadata
 * - Handle exceptions properly
 * - Test thoroughly
 */
public class ModelMBeanPattern {

    /**
     * Managed resource (no interface required)
     */
    static class ServerConfiguration {
        private String host = "localhost";
        private int port = 8080;
        private int maxConnections = 100;
        private boolean ssl = false;
        
        public String getHost() {
            return host;
        }
        
        public void setHost(String host) {
            this.host = host;
        }
        
        public int getPort() {
            return port;
        }
        
        public void setPort(int port) {
            this.port = port;
        }
        
        public int getMaxConnections() {
            return maxConnections;
        }
        
        public void setMaxConnections(int maxConnections) {
            this.maxConnections = maxConnections;
        }
        
        public boolean isSsl() {
            return ssl;
        }
        
        public void setSsl(boolean ssl) {
            this.ssl = ssl;
        }
        
        public void restart() {
            System.out.println("Restarting server on " + host + ":" + port);
        }
        
        public String getConnectionUrl() {
            String protocol = ssl ? "https" : "http";
            return protocol + "://" + host + ":" + port;
        }
    }

    /**
     * ModelMBean builder
     */
    static class ModelMBeanBuilder {
        
        private Object managedResource;
        private String mbeanName;
        private List<AttributeConfig> attributes = new ArrayList<>();
        private List<OperationConfig> operations = new ArrayList<>();
        
        static class AttributeConfig {
            String name;
            String description;
            String type;
            String getter;
            String setter;
            boolean readable;
            boolean writable;
            int cacheTime = -1; // -1 = always cache, 0 = never cache
            
            AttributeConfig(String name, String type, String getter, String setter) {
                this.name = name;
                this.type = type;
                this.getter = getter;
                this.setter = setter;
                this.readable = getter != null;
                this.writable = setter != null;
            }
        }
        
        static class OperationConfig {
            String name;
            String description;
            String returnType;
            MBeanParameterInfo[] parameters;
            int impact = MBeanOperationInfo.ACTION;
            
            OperationConfig(String name, String returnType) {
                this.name = name;
                this.returnType = returnType;
                this.parameters = new MBeanParameterInfo[0];
            }
        }
        
        public ModelMBeanBuilder(Object managedResource, String mbeanName) {
            this.managedResource = managedResource;
            this.mbeanName = mbeanName;
        }
        
        public ModelMBeanBuilder addAttribute(String name, String type, 
                                              String getter, String setter) {
            attributes.add(new AttributeConfig(name, type, getter, setter));
            return this;
        }
        
        public ModelMBeanBuilder addReadOnlyAttribute(String name, String type, String getter) {
            attributes.add(new AttributeConfig(name, type, getter, null));
            return this;
        }
        
        public ModelMBeanBuilder addOperation(String name, String returnType) {
            operations.add(new OperationConfig(name, returnType));
            return this;
        }
        
        public ModelMBean build() throws Exception {
            // Create ModelMBean instance
            RequiredModelMBean modelMBean = new RequiredModelMBean();
            
            // Build attribute info
            List<ModelMBeanAttributeInfo> attrInfos = new ArrayList<>();
            for (AttributeConfig attr : attributes) {
                Descriptor descriptor = new DescriptorSupport();
                descriptor.setField("name", attr.name);
                descriptor.setField("descriptorType", "attribute");
                
                if (attr.getter != null) {
                    descriptor.setField("getMethod", attr.getter);
                }
                if (attr.setter != null) {
                    descriptor.setField("setMethod", attr.setter);
                }
                
                // Set cache time
                descriptor.setField("currencyTimeLimit", String.valueOf(attr.cacheTime));
                
                ModelMBeanAttributeInfo attrInfo = new ModelMBeanAttributeInfo(
                    attr.name,
                    attr.type,
                    attr.description,
                    attr.readable,
                    attr.writable,
                    attr.type.equals("boolean"),
                    descriptor
                );
                
                attrInfos.add(attrInfo);
            }
            
            // Build operation info
            List<ModelMBeanOperationInfo> opInfos = new ArrayList<>();
            for (OperationConfig op : operations) {
                Descriptor descriptor = new DescriptorSupport();
                descriptor.setField("name", op.name);
                descriptor.setField("descriptorType", "operation");
                descriptor.setField("role", "operation");
                
                ModelMBeanOperationInfo opInfo = new ModelMBeanOperationInfo(
                    op.name,
                    op.description,
                    op.parameters,
                    op.returnType,
                    op.impact,
                    descriptor
                );
                
                opInfos.add(opInfo);
            }
            
            // Create ModelMBeanInfo
            Descriptor mbeanDescriptor = new DescriptorSupport();
            mbeanDescriptor.setField("name", mbeanName);
            mbeanDescriptor.setField("descriptorType", "mbean");
            
            ModelMBeanInfo mbeanInfo = new ModelMBeanInfoSupport(
                managedResource.getClass().getName(),
                "ModelMBean for " + mbeanName,
                attrInfos.toArray(new ModelMBeanAttributeInfo[0]),
                new ModelMBeanConstructorInfo[0],
                opInfos.toArray(new ModelMBeanOperationInfo[0]),
                new ModelMBeanNotificationInfo[0],
                mbeanDescriptor
            );
            
            // Set managed resource and metadata
            modelMBean.setModelMBeanInfo(mbeanInfo);
            modelMBean.setManagedResource(managedResource, "ObjectReference");
            
            return modelMBean;
        }
    }

    /**
     * Spring configuration for ModelMBeans
     */
    @Configuration
    static class ModelMBeanConfiguration {
        
        @Bean
        public MBeanExporter modelMBeanExporter() throws Exception {
            MBeanExporter exporter = new MBeanExporter();
            
            // Create managed resource
            ServerConfiguration config = new ServerConfiguration();
            
            // Build ModelMBean
            ModelMBean modelMBean = new ModelMBeanBuilder(config, "ServerConfig")
                .addAttribute("Host", "java.lang.String", "getHost", "setHost")
                .addAttribute("Port", "int", "getPort", "setPort")
                .addAttribute("MaxConnections", "int", "getMaxConnections", "setMaxConnections")
                .addAttribute("Ssl", "boolean", "isSsl", "setSsl")
                .addReadOnlyAttribute("ConnectionUrl", "java.lang.String", "getConnectionUrl")
                .addOperation("restart", "void")
                .build();
            
            // Register ModelMBean
            Map<String, Object> beans = new HashMap<>();
            beans.put("com.example:type=Configuration,name=ServerConfig", modelMBean);
            exporter.setBeans(beans);
            
            return exporter;
        }
    }

    /**
     * Manual ModelMBean creation
     */
    static class ManualModelMBeanCreation {
        
        public static ModelMBean createModelMBean(Object managedResource, String name) 
                throws Exception {
            
            RequiredModelMBean modelMBean = new RequiredModelMBean();
            
            // Create MBean descriptor
            Descriptor mbeanDescriptor = new DescriptorSupport();
            mbeanDescriptor.setField("name", name);
            mbeanDescriptor.setField("descriptorType", "mbean");
            mbeanDescriptor.setField("displayName", name);
            
            // Create attribute: host
            Descriptor hostDescriptor = new DescriptorSupport();
            hostDescriptor.setField("name", "Host");
            hostDescriptor.setField("descriptorType", "attribute");
            hostDescriptor.setField("getMethod", "getHost");
            hostDescriptor.setField("setMethod", "setHost");
            
            ModelMBeanAttributeInfo hostAttr = new ModelMBeanAttributeInfo(
                "Host",
                "java.lang.String",
                "Server host",
                true, // readable
                true, // writable
                false, // is
                hostDescriptor
            );
            
            // Create operation: restart
            Descriptor restartDescriptor = new DescriptorSupport();
            restartDescriptor.setField("name", "restart");
            restartDescriptor.setField("descriptorType", "operation");
            restartDescriptor.setField("role", "operation");
            
            ModelMBeanOperationInfo restartOp = new ModelMBeanOperationInfo(
                "restart",
                "Restart the server",
                new MBeanParameterInfo[0],
                "void",
                MBeanOperationInfo.ACTION,
                restartDescriptor
            );
            
            // Build ModelMBeanInfo
            ModelMBeanInfo mbeanInfo = new ModelMBeanInfoSupport(
                managedResource.getClass().getName(),
                "Server Configuration MBean",
                new ModelMBeanAttributeInfo[] { hostAttr },
                new ModelMBeanConstructorInfo[0],
                new ModelMBeanOperationInfo[] { restartOp },
                new ModelMBeanNotificationInfo[0],
                mbeanDescriptor
            );
            
            // Set managed resource
            modelMBean.setModelMBeanInfo(mbeanInfo);
            modelMBean.setManagedResource(managedResource, "ObjectReference");
            
            return modelMBean;
        }
    }

    /**
     * Usage examples
     */
    static class ModelMBeanExamples {
        
        public void demonstrateModelMBeanBuilder() throws Exception {
            System.out.println("\n=== ModelMBean Builder ===");
            
            ServerConfiguration config = new ServerConfiguration();
            
            ModelMBean modelMBean = new ModelMBeanBuilder(config, "ServerConfig")
                .addAttribute("Host", "java.lang.String", "getHost", "setHost")
                .addAttribute("Port", "int", "getPort", "setPort")
                .addReadOnlyAttribute("ConnectionUrl", "java.lang.String", "getConnectionUrl")
                .addOperation("restart", "void")
                .build();
            
            System.out.println("ModelMBean created with builder");
            System.out.println("Attributes: Host, Port, ConnectionUrl");
            System.out.println("Operations: restart");
            
            // Get attribute through ModelMBean
            String host = (String) modelMBean.getAttribute("Host");
            System.out.println("\nHost attribute: " + host);
            
            // Set attribute
            modelMBean.setAttribute(new Attribute("Port", 9090));
            System.out.println("Port updated to: " + modelMBean.getAttribute("Port"));
            
            // Invoke operation
            modelMBean.invoke("restart", null, null);
        }
        
        public void demonstrateManualCreation() throws Exception {
            System.out.println("\n=== Manual ModelMBean Creation ===");
            
            ServerConfiguration config = new ServerConfiguration();
            ModelMBean modelMBean = ManualModelMBeanCreation.createModelMBean(
                config, "ManualServerConfig"
            );
            
            System.out.println("Manually created ModelMBean");
            
            // Get MBean info
            ModelMBeanInfo info = (ModelMBeanInfo) modelMBean.getMBeanInfo();
            System.out.println("\nAttributes:");
            for (MBeanAttributeInfo attr : info.getAttributes()) {
                System.out.println("  - " + attr.getName() + " (" + attr.getType() + ")");
            }
            
            System.out.println("\nOperations:");
            for (MBeanOperationInfo op : info.getOperations()) {
                System.out.println("  - " + op.getName() + "()");
            }
        }
        
        public void demonstrateDescriptors() throws Exception {
            System.out.println("\n=== Descriptor Configuration ===");
            
            Descriptor descriptor = new DescriptorSupport();
            descriptor.setField("name", "MyAttribute");
            descriptor.setField("descriptorType", "attribute");
            descriptor.setField("displayName", "My Attribute");
            descriptor.setField("getMethod", "getMyAttribute");
            descriptor.setField("currencyTimeLimit", "30"); // Cache for 30 seconds
            descriptor.setField("default", "defaultValue");
            
            System.out.println("Descriptor fields:");
            for (String fieldName : descriptor.getFieldNames()) {
                System.out.println("  " + fieldName + " = " + 
                                 descriptor.getFieldValue(fieldName));
            }
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Model MBean Pattern - Dynamic MBean Creation");
        System.out.println("=============================================");
        
        ModelMBeanExamples examples = new ModelMBeanExamples();
        
        examples.demonstrateModelMBeanBuilder();
        examples.demonstrateManualCreation();
        examples.demonstrateDescriptors();
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println("ModelMBean vs Standard MBean:");
        
        System.out.println("\nStandard MBean:");
        System.out.println("+ Simple interface-based approach");
        System.out.println("+ Compile-time type safety");
        System.out.println("- Less flexible");
        System.out.println("- Requires interface implementation");
        
        System.out.println("\nModelMBean:");
        System.out.println("+ Runtime configuration");
        System.out.println("+ No interface required");
        System.out.println("+ Supports caching/persistence");
        System.out.println("+ Highly flexible");
        System.out.println("- More complex setup");
        System.out.println("- Requires descriptor configuration");
        
        System.out.println("\nCommon Descriptor Fields:");
        System.out.println("- name: Element name");
        System.out.println("- descriptorType: attribute/operation/mbean");
        System.out.println("- displayName: Display name");
        System.out.println("- getMethod/setMethod: Getter/setter names");
        System.out.println("- currencyTimeLimit: Cache validity");
        System.out.println("- default: Default value");
    }
}
