package com.example.cloudfunction;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.function.Supplier;

/**
 * Supplier Pattern
 * ================
 * 
 * Demonstrates the Supplier<T> pattern in Spring Cloud Function
 * for data generation and periodic data production.
 * 
 * Key Concepts:
 * ------------
 * 1. Supplier<T> - Produces output T, accepts no input
 * 2. Data Source - Generate or fetch data
 * 3. Periodic Execution - Scheduled data production
 * 4. Push Model - Proactively send data
 * 5. Event Generation - Create events on schedule
 * 
 * How It Works:
 * ------------
 * - Define @Bean of type Supplier<T>
 * - No input parameters, returns output
 * - Can be invoked on schedule or on-demand
 * - Perfect for data polling, event generation
 * - Works with Spring Cloud Stream for continuous streaming
 * 
 * Use Cases:
 * ---------
 * - Scheduled data polling
 * - Event generation (heartbeat, status)
 * - Data source integration
 * - Sensor data simulation
 * - Metrics generation
 * - Health check pinging
 * - Time-based triggers
 * 
 * Configuration:
 * -------------
 * # Periodic execution with Cloud Stream
 * spring.cloud.stream.poller.fixed-delay=5000
 * spring.cloud.function.definition=dataSupplier
 * 
 * @author Spring Patterns
 * @version 1.0
 */

/**
 * Example 1: Simple Data Supplier
 */
@Configuration
class SimpleDataSupplierExample {
    
    /**
     * Supply current timestamp
     * 
     * Usage (HTTP):
     * GET /currentTime
     * Response: "1638360000000"
     * 
     * Usage (Scheduled with Cloud Stream):
     * Produces timestamp every 5 seconds to message broker
     */
    @Bean
    public Supplier<Long> currentTime() {
        return () -> {
            long timestamp = System.currentTimeMillis();
            System.out.println("Generated timestamp: " + timestamp);
            return timestamp;
        };
    }
}

/**
 * Example 2: Random Data Generator
 */
@Configuration
class RandomDataGeneratorExample {
    
    static class SensorReading {
        private String sensorId;
        private double temperature;
        private double humidity;
        private long timestamp;
        
        public SensorReading(String sensorId, double temperature, double humidity, long timestamp) {
            this.sensorId = sensorId;
            this.temperature = temperature;
            this.humidity = humidity;
            this.timestamp = timestamp;
        }
        
        public String getSensorId() { return sensorId; }
        public double getTemperature() { return temperature; }
        public double getHumidity() { return humidity; }
        public long getTimestamp() { return timestamp; }
    }
    
    /**
     * Generate random sensor readings
     * 
     * Simulates IoT sensor data
     */
    @Bean
    public Supplier<SensorReading> sensorData() {
        return () -> {
            double temperature = 20 + (Math.random() * 15); // 20-35°C
            double humidity = 40 + (Math.random() * 40);    // 40-80%
            
            SensorReading reading = new SensorReading(
                "SENSOR-001",
                Math.round(temperature * 100.0) / 100.0,
                Math.round(humidity * 100.0) / 100.0,
                System.currentTimeMillis()
            );
            
            System.out.println("Generated sensor reading:");
            System.out.println("  Temperature: " + reading.getTemperature() + "°C");
            System.out.println("  Humidity: " + reading.getHumidity() + "%");
            
            return reading;
        };
    }
}

/**
 * Example 3: Event Generator
 */
@Configuration
class EventGeneratorExample {
    
    static class HeartbeatEvent {
        private String serviceId;
        private String status;
        private long timestamp;
        
        public HeartbeatEvent(String serviceId, String status, long timestamp) {
            this.serviceId = serviceId;
            this.status = status;
            this.timestamp = timestamp;
        }
        
        public String getServiceId() { return serviceId; }
        public String getStatus() { return status; }
        public long getTimestamp() { return timestamp; }
    }
    
    /**
     * Generate heartbeat events
     * 
     * Periodic health indicator
     */
    @Bean
    public Supplier<HeartbeatEvent> heartbeat() {
        return () -> {
            HeartbeatEvent event = new HeartbeatEvent(
                "order-service",
                "HEALTHY",
                System.currentTimeMillis()
            );
            
            System.out.println("Heartbeat: " + event.getServiceId() + " - " + event.getStatus());
            return event;
        };
    }
}

/**
 * Example 4: Database Poller
 */
@Configuration
class DatabasePollerExample {
    
    static class Order {
        private String id;
        private String status;
        private long createdAt;
        
        public Order(String id, String status, long createdAt) {
            this.id = id;
            this.status = status;
            this.createdAt = createdAt;
        }
        
        public String getId() { return id; }
        public String getStatus() { return status; }
        public long getCreatedAt() { return createdAt; }
    }
    
    /**
     * Poll database for new orders
     * 
     * Simulates database polling
     */
    @Bean
    public Supplier<java.util.List<Order>> pollOrders() {
        return () -> {
            System.out.println("Polling database for new orders...");
            
            // Simulate database query
            // List<Order> orders = orderRepository.findByStatus("PENDING");
            
            java.util.List<Order> orders = new java.util.ArrayList<>();
            if (Math.random() > 0.5) {
                orders.add(new Order(
                    "ORD-" + System.currentTimeMillis(),
                    "PENDING",
                    System.currentTimeMillis()
                ));
            }
            
            System.out.println("Found " + orders.size() + " new orders");
            return orders;
        };
    }
}

/**
 * Example 5: API Data Fetcher
 */
@Configuration
class ApiDataFetcherExample {
    
    static class WeatherData {
        private String city;
        private double temperature;
        private String condition;
        private long timestamp;
        
        public WeatherData(String city, double temperature, String condition, long timestamp) {
            this.city = city;
            this.temperature = temperature;
            this.condition = condition;
            this.timestamp = timestamp;
        }
        
        public String getCity() { return city; }
        public double getTemperature() { return temperature; }
        public String getCondition() { return condition; }
        public long getTimestamp() { return timestamp; }
    }
    
    /**
     * Fetch weather data from external API
     * 
     * Simulates external API call
     */
    @Bean
    public Supplier<WeatherData> fetchWeather() {
        return () -> {
            System.out.println("Fetching weather data from external API...");
            
            // Simulate API call
            // WeatherData data = weatherClient.getCurrentWeather("New York");
            
            WeatherData data = new WeatherData(
                "New York",
                20 + (Math.random() * 15),
                Math.random() > 0.5 ? "Sunny" : "Cloudy",
                System.currentTimeMillis()
            );
            
            System.out.println("Weather: " + data.getCity() + " - " + 
                data.getTemperature() + "°C - " + data.getCondition());
            
            return data;
        };
    }
}

/**
 * Example 6: Metrics Generator
 */
@Configuration
class MetricsGeneratorExample {
    
    static class SystemMetrics {
        private double cpuUsage;
        private double memoryUsage;
        private int activeThreads;
        private long timestamp;
        
        public SystemMetrics(double cpuUsage, double memoryUsage, int activeThreads, long timestamp) {
            this.cpuUsage = cpuUsage;
            this.memoryUsage = memoryUsage;
            this.activeThreads = activeThreads;
            this.timestamp = timestamp;
        }
        
        public double getCpuUsage() { return cpuUsage; }
        public double getMemoryUsage() { return memoryUsage; }
        public int getActiveThreads() { return activeThreads; }
        public long getTimestamp() { return timestamp; }
    }
    
    /**
     * Generate system metrics
     */
    @Bean
    public Supplier<SystemMetrics> systemMetrics() {
        return () -> {
            // Simulate metrics collection
            Runtime runtime = Runtime.getRuntime();
            
            double cpuUsage = Math.random() * 100;
            double memoryUsage = ((double) (runtime.totalMemory() - runtime.freeMemory()) / 
                runtime.maxMemory()) * 100;
            int activeThreads = Thread.activeCount();
            
            SystemMetrics metrics = new SystemMetrics(
                Math.round(cpuUsage * 100.0) / 100.0,
                Math.round(memoryUsage * 100.0) / 100.0,
                activeThreads,
                System.currentTimeMillis()
            );
            
            System.out.println("System Metrics:");
            System.out.println("  CPU: " + metrics.getCpuUsage() + "%");
            System.out.println("  Memory: " + metrics.getMemoryUsage() + "%");
            System.out.println("  Threads: " + metrics.getActiveThreads());
            
            return metrics;
        };
    }
}

/**
 * Example 7: Queue Message Generator
 */
@Configuration
class QueueMessageGeneratorExample {
    
    static class QueueMessage {
        private String messageId;
        private String body;
        private int priority;
        private long timestamp;
        
        public QueueMessage(String messageId, String body, int priority, long timestamp) {
            this.messageId = messageId;
            this.body = body;
            this.priority = priority;
            this.timestamp = timestamp;
        }
        
        public String getMessageId() { return messageId; }
        public String getBody() { return body; }
        public int getPriority() { return priority; }
        public long getTimestamp() { return timestamp; }
    }
    
    /**
     * Generate queue messages
     */
    @Bean
    public Supplier<QueueMessage> generateQueueMessage() {
        return () -> {
            String messageId = "MSG-" + System.currentTimeMillis();
            String body = "Generated message at " + new java.util.Date();
            int priority = (int) (Math.random() * 10) + 1;
            
            QueueMessage message = new QueueMessage(
                messageId,
                body,
                priority,
                System.currentTimeMillis()
            );
            
            System.out.println("Generated queue message: " + messageId + 
                " (Priority: " + priority + ")");
            
            return message;
        };
    }
}

/**
 * Example 8: Status Reporter
 */
@Configuration
class StatusReporterExample {
    
    static class ServiceStatus {
        private String serviceName;
        private String version;
        private String status;
        private java.util.Map<String, Object> details;
        
        public ServiceStatus(String serviceName, String version, String status, 
                           java.util.Map<String, Object> details) {
            this.serviceName = serviceName;
            this.version = version;
            this.status = status;
            this.details = details;
        }
        
        public String getServiceName() { return serviceName; }
        public String getVersion() { return version; }
        public String getStatus() { return status; }
        public java.util.Map<String, Object> getDetails() { return details; }
    }
    
    /**
     * Report service status
     */
    @Bean
    public Supplier<ServiceStatus> reportStatus() {
        return () -> {
            java.util.Map<String, Object> details = new java.util.HashMap<>();
            details.put("uptime", System.currentTimeMillis());
            details.put("activeConnections", (int) (Math.random() * 100));
            details.put("requestsProcessed", (int) (Math.random() * 10000));
            
            ServiceStatus status = new ServiceStatus(
                "order-service",
                "1.0.0",
                "UP",
                details
            );
            
            System.out.println("Service Status: " + status.getStatus());
            return status;
        };
    }
}

/**
 * Example 9: Configuration Supplier
 */
@Configuration
class ConfigurationSupplierExample {
    
    static class AppConfiguration {
        private java.util.Map<String, String> settings;
        private long refreshedAt;
        
        public AppConfiguration(java.util.Map<String, String> settings, long refreshedAt) {
            this.settings = settings;
            this.refreshedAt = refreshedAt;
        }
        
        public java.util.Map<String, String> getSettings() { return settings; }
        public long getRefreshedAt() { return refreshedAt; }
    }
    
    /**
     * Supply application configuration
     * 
     * Useful for configuration refresh
     */
    @Bean
    public Supplier<AppConfiguration> appConfiguration() {
        return () -> {
            System.out.println("Loading application configuration...");
            
            // Simulate config loading from external source
            java.util.Map<String, String> settings = new java.util.HashMap<>();
            settings.put("feature.newCheckout", "true");
            settings.put("cache.ttl", "3600");
            settings.put("api.timeout", "30000");
            
            AppConfiguration config = new AppConfiguration(
                settings,
                System.currentTimeMillis()
            );
            
            System.out.println("Configuration loaded with " + settings.size() + " settings");
            return config;
        };
    }
}

/**
 * Example 10: Stateful Sequence Generator
 */
@Configuration
class StatefulSequenceGeneratorExample {
    
    private int counter = 0;
    
    static class SequenceEvent {
        private int sequence;
        private String eventType;
        private long timestamp;
        
        public SequenceEvent(int sequence, String eventType, long timestamp) {
            this.sequence = sequence;
            this.eventType = eventType;
            this.timestamp = timestamp;
        }
        
        public int getSequence() { return sequence; }
        public String getEventType() { return eventType; }
        public long getTimestamp() { return timestamp; }
    }
    
    /**
     * Generate sequential events
     * 
     * Maintains state between invocations
     */
    @Bean
    public Supplier<SequenceEvent> sequenceGenerator() {
        return () -> {
            counter++;
            
            SequenceEvent event = new SequenceEvent(
                counter,
                "SEQUENCE_EVENT",
                System.currentTimeMillis()
            );
            
            System.out.println("Generated sequence event #" + counter);
            return event;
        };
    }
}

/**
 * Main Pattern Class
 */
@Configuration
public class SupplierPattern {
    
    /**
     * Core Supplier pattern demonstration
     */
    public void demonstrateSupplierPattern() {
        System.out.println("\n=== Supplier Pattern ===");
        System.out.println("Data generation and periodic production");
        System.out.println("\nKey Characteristics:");
        System.out.println("  - No input");
        System.out.println("  - Produces output");
        System.out.println("  - Can be scheduled");
        System.out.println("  - Push model");
        System.out.println("\nUse Cases:");
        System.out.println("  - Data polling");
        System.out.println("  - Event generation");
        System.out.println("  - Heartbeats");
        System.out.println("  - Metrics collection");
        System.out.println("  - Sensor simulation");
    }
}

/**
 * Usage Examples and Configuration
 */
class SupplierPatternUsageExamples {
    
    public static void main(String[] args) {
        System.out.println("Supplier Pattern Usage");
        System.out.println("=====================\n");
        
        System.out.println("1. Define Supplier:");
        System.out.println("@Bean");
        System.out.println("public Supplier<Long> currentTime() {");
        System.out.println("    return () -> System.currentTimeMillis();");
        System.out.println("}\n");
        
        System.out.println("2. HTTP Invocation:");
        System.out.println("GET /currentTime");
        System.out.println("Response: 1638360000000\n");
        
        System.out.println("3. Scheduled Execution (Cloud Stream):");
        System.out.println("spring.cloud.function.definition=currentTime");
        System.out.println("spring.cloud.stream.bindings.currentTime-out-0.destination=time-events");
        System.out.println("spring.cloud.stream.poller.fixed-delay=5000\n");
        
        System.out.println("4. Manual Invocation:");
        System.out.println("Supplier<Long> supplier = currentTime();");
        System.out.println("Long time = supplier.get();\n");
        
        System.out.println("Best Practices:");
        System.out.println("- Keep generation logic fast");
        System.out.println("- Handle errors gracefully");
        System.out.println("- Consider rate limiting");
        System.out.println("- Monitor generated volume");
        System.out.println("- Use appropriate scheduling intervals");
    }
}
