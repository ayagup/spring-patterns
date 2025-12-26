package com.example.jmx;

import org.springframework.jmx.export.annotation.*;
import org.springframework.stereotype.Component;

/**
 * Annotation-based JMX Pattern - Using Annotations for JMX Export
 * 
 * Spring provides a set of annotations to simplify JMX bean export without
 * implementing MBean interfaces. This approach uses @ManagedResource,
 * @ManagedAttribute, and @ManagedOperation annotations to declaratively
 * define JMX-exposed functionality.
 * 
 * Key Annotations:
 * - @ManagedResource: Mark class as JMX-managed bean
 * - @ManagedAttribute: Expose getter/setter as JMX attribute
 * - @ManagedOperation: Expose method as JMX operation
 * - @ManagedOperationParameter: Document operation parameters
 * - @ManagedOperationParameters: Group multiple parameter annotations
 * - @ManagedMetric: Expose metric with metadata
 * - @ManagedNotification: Declare JMX notifications
 * 
 * @ManagedResource Attributes:
 * - objectName: JMX ObjectName
 * - description: Bean description
 * - log: Enable logging
 * - logFile: Log file location
 * - currencyTimeLimit: Cache validity time
 * - persistPolicy: Persistence behavior
 * - persistPeriod: Persistence interval
 * - persistLocation: Persistence location
 * - persistName: Persistence name
 * 
 * @ManagedAttribute Attributes:
 * - description: Attribute description
 * - currencyTimeLimit: Cache time
 * - defaultValue: Default value
 * - persistPolicy: Persistence behavior
 * 
 * @ManagedOperation Attributes:
 * - description: Operation description
 * - currencyTimeLimit: Cache time
 * 
 * Advantages:
 * - No interface implementation required
 * - More flexible than interface-based approach
 * - Better separation of concerns
 * - Easier to maintain
 * - Supports metadata and documentation
 * 
 * Use Cases:
 * - Service monitoring
 * - Configuration management
 * - Performance metrics
 * - Administrative operations
 * - Health checking
 * 
 * Best Practices:
 * - Use meaningful descriptions
 * - Document parameters
 * - Group related operations
 * - Use appropriate metric types
 * - Enable notifications when needed
 */
public class AnnotationBasedJmxPattern {

    /**
     * Basic annotation-based managed resource
     */
    @ManagedResource(
        objectName = "com.example:type=Service,name=ApplicationService",
        description = "Application Service Management Bean"
    )
    @Component
    static class ApplicationService {
        
        private String serviceName = "MyService";
        private int port = 8080;
        private boolean running = true;
        private long requestCount = 0;
        
        // Managed attribute (read-write)
        @ManagedAttribute(description = "Service name")
        public String getServiceName() {
            return serviceName;
        }
        
        @ManagedAttribute(description = "Service name")
        public void setServiceName(String serviceName) {
            this.serviceName = serviceName;
        }
        
        // Managed attribute (read-only)
        @ManagedAttribute(description = "Service port (read-only)")
        public int getPort() {
            return port;
        }
        
        // Managed attribute (read-only boolean)
        @ManagedAttribute(description = "Service running status")
        public boolean isRunning() {
            return running;
        }
        
        // Managed operation with no parameters
        @ManagedOperation(description = "Start the service")
        public void start() {
            running = true;
            System.out.println("Service started");
        }
        
        // Managed operation with no parameters
        @ManagedOperation(description = "Stop the service")
        public void stop() {
            running = false;
            System.out.println("Service stopped");
        }
        
        // Managed operation with return value
        @ManagedOperation(description = "Get current request count")
        public long getRequestCount() {
            return requestCount;
        }
        
        // Managed operation with parameters
        @ManagedOperation(description = "Reset request counter")
        @ManagedOperationParameters({
            @ManagedOperationParameter(name = "initialValue", description = "Initial counter value")
        })
        public void resetCounter(long initialValue) {
            requestCount = initialValue;
            System.out.println("Counter reset to: " + initialValue);
        }
    }

    /**
     * Advanced managed resource with metrics
     */
    @ManagedResource(
        objectName = "com.example:type=Metrics,name=PerformanceMetrics",
        description = "Performance Metrics Bean"
    )
    static class PerformanceMetrics {
        
        private long totalRequests = 1000;
        private long failedRequests = 10;
        private double averageResponseTime = 125.5;
        private long peakMemoryUsage = 512 * 1024 * 1024; // 512MB
        
        // Managed metric
        @ManagedMetric(
            description = "Total number of requests",
            currencyTimeLimit = 15,
            persistPolicy = "OnUpdate",
            metricType = MetricType.COUNTER,
            category = "throughput"
        )
        public long getTotalRequests() {
            return totalRequests;
        }
        
        @ManagedMetric(
            description = "Number of failed requests",
            metricType = MetricType.COUNTER,
            category = "utilization"
        )
        public long getFailedRequests() {
            return failedRequests;
        }
        
        @ManagedMetric(
            description = "Average response time in milliseconds",
            metricType = MetricType.GAUGE,
            unit = "milliseconds"
        )
        public double getAverageResponseTime() {
            return averageResponseTime;
        }
        
        @ManagedMetric(
            description = "Peak memory usage",
            metricType = MetricType.GAUGE,
            unit = "bytes"
        )
        public long getPeakMemoryUsage() {
            return peakMemoryUsage;
        }
        
        // Computed metric
        @ManagedMetric(
            description = "Success rate percentage",
            metricType = MetricType.GAUGE,
            unit = "percent"
        )
        public double getSuccessRate() {
            if (totalRequests == 0) return 0;
            return ((double)(totalRequests - failedRequests) / totalRequests) * 100;
        }
        
        @ManagedOperation(description = "Reset all metrics")
        public void resetMetrics() {
            totalRequests = 0;
            failedRequests = 0;
            averageResponseTime = 0;
            peakMemoryUsage = 0;
            System.out.println("All metrics reset");
        }
    }

    /**
     * Configuration manager with complex operations
     */
    @ManagedResource(
        objectName = "com.example:type=Configuration,name=DatabaseConfig",
        description = "Database Configuration Manager"
    )
    static class DatabaseConfiguration {
        
        private String jdbcUrl = "jdbc:mysql://localhost:3306/mydb";
        private int maxPoolSize = 10;
        private int minPoolSize = 2;
        private long connectionTimeout = 30000;
        
        @ManagedAttribute(description = "JDBC connection URL")
        public String getJdbcUrl() {
            return jdbcUrl;
        }
        
        @ManagedAttribute
        public void setJdbcUrl(String jdbcUrl) {
            this.jdbcUrl = jdbcUrl;
            System.out.println("JDBC URL updated: " + jdbcUrl);
        }
        
        @ManagedAttribute(description = "Maximum connection pool size")
        public int getMaxPoolSize() {
            return maxPoolSize;
        }
        
        @ManagedAttribute
        public void setMaxPoolSize(int maxPoolSize) {
            if (maxPoolSize < minPoolSize) {
                throw new IllegalArgumentException("Max pool size must be >= min pool size");
            }
            this.maxPoolSize = maxPoolSize;
        }
        
        @ManagedAttribute(description = "Minimum connection pool size")
        public int getMinPoolSize() {
            return minPoolSize;
        }
        
        @ManagedAttribute
        public void setMinPoolSize(int minPoolSize) {
            if (minPoolSize > maxPoolSize) {
                throw new IllegalArgumentException("Min pool size must be <= max pool size");
            }
            this.minPoolSize = minPoolSize;
        }
        
        @ManagedOperation(description = "Test database connection")
        public String testConnection() {
            System.out.println("Testing connection to: " + jdbcUrl);
            return "Connection successful";
        }
        
        @ManagedOperation(description = "Update connection pool settings")
        @ManagedOperationParameters({
            @ManagedOperationParameter(name = "min", description = "Minimum pool size"),
            @ManagedOperationParameter(name = "max", description = "Maximum pool size"),
            @ManagedOperationParameter(name = "timeout", description = "Connection timeout in ms")
        })
        public void updatePoolSettings(int min, int max, long timeout) {
            if (min > max) {
                throw new IllegalArgumentException("Min must be <= max");
            }
            this.minPoolSize = min;
            this.maxPoolSize = max;
            this.connectionTimeout = timeout;
            System.out.println("Pool settings updated: min=" + min + ", max=" + max + 
                             ", timeout=" + timeout);
        }
    }

    /**
     * Usage examples
     */
    static class AnnotationBasedJmxExamples {
        
        public void demonstrateBasicManagedResource() {
            System.out.println("\n=== Basic Managed Resource ===");
            
            ApplicationService service = new ApplicationService();
            
            System.out.println("Service Name: " + service.getServiceName());
            System.out.println("Port: " + service.getPort());
            System.out.println("Running: " + service.isRunning());
            
            System.out.println("\nPerforming operations:");
            service.stop();
            service.start();
            service.resetCounter(100);
            System.out.println("Request Count: " + service.getRequestCount());
        }
        
        public void demonstrateManagedMetrics() {
            System.out.println("\n=== Managed Metrics ===");
            
            PerformanceMetrics metrics = new PerformanceMetrics();
            
            System.out.println("Performance Metrics:");
            System.out.println("- Total Requests: " + metrics.getTotalRequests());
            System.out.println("- Failed Requests: " + metrics.getFailedRequests());
            System.out.println("- Average Response Time: " + metrics.getAverageResponseTime() + "ms");
            System.out.println("- Success Rate: " + String.format("%.2f", metrics.getSuccessRate()) + "%");
            System.out.println("- Peak Memory: " + (metrics.getPeakMemoryUsage() / 1024 / 1024) + "MB");
            
            metrics.resetMetrics();
        }
        
        public void demonstrateConfigurationManagement() {
            System.out.println("\n=== Configuration Management ===");
            
            DatabaseConfiguration config = new DatabaseConfiguration();
            
            System.out.println("Current Configuration:");
            System.out.println("- JDBC URL: " + config.getJdbcUrl());
            System.out.println("- Pool Size: " + config.getMinPoolSize() + "-" + config.getMaxPoolSize());
            
            System.out.println("\nUpdating configuration:");
            config.setJdbcUrl("jdbc:postgresql://localhost:5432/newdb");
            config.updatePoolSettings(5, 20, 60000);
            
            System.out.println("\nTesting connection:");
            System.out.println(config.testConnection());
        }
    }

    public static void main(String[] args) {
        System.out.println("Annotation-based JMX Pattern - Using Annotations for JMX Export");
        System.out.println("================================================================");
        
        AnnotationBasedJmxExamples examples = new AnnotationBasedJmxExamples();
        
        examples.demonstrateBasicManagedResource();
        examples.demonstrateManagedMetrics();
        examples.demonstrateConfigurationManagement();
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println("Key Annotations:");
        System.out.println("- @ManagedResource: Mark class as JMX bean");
        System.out.println("- @ManagedAttribute: Expose attribute");
        System.out.println("- @ManagedOperation: Expose operation");
        System.out.println("- @ManagedMetric: Expose metric with metadata");
        System.out.println("- @ManagedOperationParameter: Document parameters");
        
        System.out.println("\nMetric Types:");
        System.out.println("- COUNTER: Monotonically increasing value");
        System.out.println("- GAUGE: Current value that can go up/down");
        
        System.out.println("\nConfiguration:");
        System.out.println("Enable with @EnableMBeanExport in Spring Boot");
        System.out.println("Or use <context:mbean-export/> in XML config");
    }
}
