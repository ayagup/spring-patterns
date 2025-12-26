package com.example.jmx;

import org.springframework.jmx.export.notification.*;
import org.springframework.jmx.export.MBeanExporter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import javax.management.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Notification Publisher Pattern - JMX Notifications and Listeners
 * 
 * JMX notifications provide a mechanism for MBeans to send asynchronous
 * messages to interested listeners. Spring's NotificationPublisher
 * simplifies the process of sending JMX notifications from Spring beans.
 * 
 * Key Components:
 * 
 * 1. NotificationPublisher:
 *    - Interface for publishing notifications
 *    - Injected by Spring into managed beans
 *    - Simplifies notification sending
 * 
 * 2. NotificationPublisherAware:
 *    - Callback interface
 *    - Receives NotificationPublisher instance
 *    - Enables beans to send notifications
 * 
 * 3. Notification:
 *    - Standard JMX notification class
 *    - Contains: type, message, sequence number, timestamp, user data
 * 
 * 4. NotificationListener:
 *    - Receives notifications
 *    - Handles notification processing
 * 
 * 5. NotificationFilter:
 *    - Filters notifications
 *    - Selective notification delivery
 * 
 * Notification Types:
 * - Attribute change: AttributeChangeNotification
 * - Generic: Notification
 * - Custom: Extend Notification class
 * 
 * Notification Components:
 * - Type: Categorizes notification (e.g., "config.changed")
 * - Message: Human-readable description
 * - Sequence Number: Unique identifier
 * - Timestamp: When notification occurred
 * - Source: MBean that sent notification
 * - User Data: Additional context (optional)
 * 
 * Use Cases:
 * - Configuration changes
 * - State transitions
 * - Threshold violations
 * - Error conditions
 * - Performance alerts
 * - Resource exhaustion
 * - Service status changes
 * 
 * Best Practices:
 * - Use meaningful notification types
 * - Include relevant context in user data
 * - Don't overuse notifications
 * - Filter notifications appropriately
 * - Handle notifications asynchronously
 * - Document notification types
 */
public class NotificationPublisherPattern {

    /**
     * Service that publishes notifications
     */
    @Component
    static class MonitoringService implements NotificationPublisherAware {
        
        private NotificationPublisher notificationPublisher;
        private AtomicLong sequenceNumber = new AtomicLong(0);
        
        private int threshold = 100;
        private int currentValue = 0;
        private String status = "RUNNING";
        
        @Override
        public void setNotificationPublisher(NotificationPublisher notificationPublisher) {
            this.notificationPublisher = notificationPublisher;
        }
        
        public void updateValue(int newValue) {
            int oldValue = this.currentValue;
            this.currentValue = newValue;
            
            // Send notification if threshold exceeded
            if (newValue > threshold && oldValue <= threshold) {
                sendThresholdNotification(newValue);
            }
        }
        
        public void changeStatus(String newStatus) {
            String oldStatus = this.status;
            this.status = newStatus;
            
            // Send status change notification
            sendStatusNotification(oldStatus, newStatus);
        }
        
        private void sendThresholdNotification(int value) {
            Notification notification = new Notification(
                "threshold.exceeded",
                this,
                sequenceNumber.incrementAndGet(),
                System.currentTimeMillis(),
                "Threshold exceeded: " + value + " > " + threshold
            );
            
            // Add user data
            Map<String, Object> userData = new HashMap<>();
            userData.put("threshold", threshold);
            userData.put("currentValue", value);
            userData.put("timestamp", new Date());
            notification.setUserData(userData);
            
            notificationPublisher.sendNotification(notification);
        }
        
        private void sendStatusNotification(String oldStatus, String newStatus) {
            Notification notification = new Notification(
                "status.changed",
                this,
                sequenceNumber.incrementAndGet(),
                System.currentTimeMillis(),
                "Status changed from " + oldStatus + " to " + newStatus
            );
            
            Map<String, Object> userData = new HashMap<>();
            userData.put("oldStatus", oldStatus);
            userData.put("newStatus", newStatus);
            notification.setUserData(userData);
            
            notificationPublisher.sendNotification(notification);
        }
        
        public int getThreshold() {
            return threshold;
        }
        
        public void setThreshold(int threshold) {
            this.threshold = threshold;
        }
        
        public int getCurrentValue() {
            return currentValue;
        }
        
        public String getStatus() {
            return status;
        }
    }

    /**
     * Cache service with attribute change notifications
     */
    static class CacheService implements NotificationPublisherAware {
        
        private NotificationPublisher notificationPublisher;
        private AtomicLong sequenceNumber = new AtomicLong(0);
        
        private long maxSize = 1000;
        private long currentSize = 0;
        private Map<String, Object> cache = new ConcurrentHashMap<>();
        
        @Override
        public void setNotificationPublisher(NotificationPublisher notificationPublisher) {
            this.notificationPublisher = notificationPublisher;
        }
        
        public void put(String key, Object value) {
            cache.put(key, value);
            long oldSize = currentSize;
            currentSize = cache.size();
            
            if (currentSize != oldSize) {
                sendAttributeChangeNotification("currentSize", oldSize, currentSize);
            }
            
            // Check if approaching max size
            if (currentSize > maxSize * 0.9) {
                sendWarningNotification("Cache approaching max size: " + currentSize + "/" + maxSize);
            }
        }
        
        public void clear() {
            long oldSize = currentSize;
            cache.clear();
            currentSize = 0;
            
            sendAttributeChangeNotification("currentSize", oldSize, 0L);
            sendInfoNotification("Cache cleared");
        }
        
        public void setMaxSize(long newMaxSize) {
            long oldMaxSize = this.maxSize;
            this.maxSize = newMaxSize;
            
            sendAttributeChangeNotification("maxSize", oldMaxSize, newMaxSize);
        }
        
        private void sendAttributeChangeNotification(String attributeName, 
                                                     Object oldValue, Object newValue) {
            AttributeChangeNotification notification = new AttributeChangeNotification(
                this,
                sequenceNumber.incrementAndGet(),
                System.currentTimeMillis(),
                "Attribute " + attributeName + " changed",
                attributeName,
                oldValue.getClass().getName(),
                oldValue,
                newValue
            );
            
            notificationPublisher.sendNotification(notification);
        }
        
        private void sendWarningNotification(String message) {
            Notification notification = new Notification(
                "cache.warning",
                this,
                sequenceNumber.incrementAndGet(),
                System.currentTimeMillis(),
                message
            );
            
            notificationPublisher.sendNotification(notification);
        }
        
        private void sendInfoNotification(String message) {
            Notification notification = new Notification(
                "cache.info",
                this,
                sequenceNumber.incrementAndGet(),
                System.currentTimeMillis(),
                message
            );
            
            notificationPublisher.sendNotification(notification);
        }
        
        public long getCurrentSize() {
            return currentSize;
        }
        
        public long getMaxSize() {
            return maxSize;
        }
    }

    /**
     * Custom notification listener
     */
    static class LoggingNotificationListener implements NotificationListener {
        
        private List<Notification> receivedNotifications = new ArrayList<>();
        
        @Override
        public void handleNotification(Notification notification, Object handback) {
            receivedNotifications.add(notification);
            
            System.out.println("\n=== Notification Received ===");
            System.out.println("Type: " + notification.getType());
            System.out.println("Message: " + notification.getMessage());
            System.out.println("Sequence: " + notification.getSequenceNumber());
            System.out.println("Timestamp: " + new Date(notification.getTimeStamp()));
            System.out.println("Source: " + notification.getSource().getClass().getSimpleName());
            
            if (notification.getUserData() != null) {
                System.out.println("User Data: " + notification.getUserData());
            }
            
            if (notification instanceof AttributeChangeNotification) {
                AttributeChangeNotification acn = (AttributeChangeNotification) notification;
                System.out.println("Attribute: " + acn.getAttributeName());
                System.out.println("Old Value: " + acn.getOldValue());
                System.out.println("New Value: " + acn.getNewValue());
            }
        }
        
        public List<Notification> getReceivedNotifications() {
            return new ArrayList<>(receivedNotifications);
        }
        
        public void clear() {
            receivedNotifications.clear();
        }
    }

    /**
     * Notification filter - only allows specific types
     */
    static class TypeBasedNotificationFilter implements NotificationFilter {
        
        private Set<String> allowedTypes;
        
        public TypeBasedNotificationFilter(String... types) {
            this.allowedTypes = new HashSet<>(Arrays.asList(types));
        }
        
        @Override
        public boolean isNotificationEnabled(Notification notification) {
            return allowedTypes.contains(notification.getType());
        }
    }

    /**
     * Configuration for notification publishing
     */
    @Configuration
    static class NotificationConfiguration {
        
        @Bean
        public MBeanExporter notificationExporter() {
            MBeanExporter exporter = new MBeanExporter();
            
            // Register beans
            Map<String, Object> beans = new HashMap<>();
            beans.put("bean:name=monitoringService", new MonitoringService());
            beans.put("bean:name=cacheService", new CacheService());
            exporter.setBeans(beans);
            
            return exporter;
        }
    }

    /**
     * Usage examples
     */
    static class NotificationPublisherExamples {
        
        public void demonstrateBasicNotifications() {
            System.out.println("\n=== Basic Notification Publishing ===");
            
            MonitoringService service = new MonitoringService();
            LoggingNotificationListener listener = new LoggingNotificationListener();
            
            // Simulate notification publisher
            service.setNotificationPublisher(notification -> {
                listener.handleNotification(notification, null);
            });
            
            System.out.println("Updating value to 50 (below threshold):");
            service.updateValue(50);
            
            System.out.println("\nUpdating value to 150 (exceeds threshold):");
            service.updateValue(150);
            
            System.out.println("\nReceived " + listener.getReceivedNotifications().size() + 
                             " notification(s)");
        }
        
        public void demonstrateStatusNotifications() {
            System.out.println("\n=== Status Change Notifications ===");
            
            MonitoringService service = new MonitoringService();
            LoggingNotificationListener listener = new LoggingNotificationListener();
            
            service.setNotificationPublisher(notification -> {
                listener.handleNotification(notification, null);
            });
            
            service.changeStatus("STOPPING");
            service.changeStatus("STOPPED");
            service.changeStatus("STARTING");
            service.changeStatus("RUNNING");
        }
        
        public void demonstrateAttributeChangeNotifications() {
            System.out.println("\n=== Attribute Change Notifications ===");
            
            CacheService cache = new CacheService();
            LoggingNotificationListener listener = new LoggingNotificationListener();
            
            cache.setNotificationPublisher(notification -> {
                listener.handleNotification(notification, null);
            });
            
            System.out.println("Adding items to cache:");
            cache.put("key1", "value1");
            cache.put("key2", "value2");
            
            System.out.println("\nChanging max size:");
            cache.setMaxSize(2000);
            
            System.out.println("\nClearing cache:");
            cache.clear();
        }
        
        public void demonstrateNotificationFiltering() {
            System.out.println("\n=== Notification Filtering ===");
            
            MonitoringService service = new MonitoringService();
            
            // Filter: only threshold notifications
            NotificationFilter filter = new TypeBasedNotificationFilter("threshold.exceeded");
            
            LoggingNotificationListener listener = new LoggingNotificationListener();
            
            service.setNotificationPublisher(notification -> {
                if (filter.isNotificationEnabled(notification)) {
                    listener.handleNotification(notification, null);
                    System.out.println("(Notification passed filter)");
                } else {
                    System.out.println("(Notification filtered out: " + notification.getType() + ")");
                }
            });
            
            service.changeStatus("STOPPING"); // Filtered out
            service.updateValue(150); // Passes filter
        }
    }

    public static void main(String[] args) {
        System.out.println("Notification Publisher Pattern - JMX Notifications and Listeners");
        System.out.println("=================================================================");
        
        NotificationPublisherExamples examples = new NotificationPublisherExamples();
        
        examples.demonstrateBasicNotifications();
        examples.demonstrateStatusNotifications();
        examples.demonstrateAttributeChangeNotifications();
        examples.demonstrateNotificationFiltering();
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println("Notification Types:");
        System.out.println("- Notification: Generic notification");
        System.out.println("- AttributeChangeNotification: Attribute value changed");
        System.out.println("- Custom: Extend Notification for specific needs");
        
        System.out.println("\nNotification Components:");
        System.out.println("- Type: Categorization (e.g., 'threshold.exceeded')");
        System.out.println("- Message: Human-readable description");
        System.out.println("- Sequence Number: Unique identifier");
        System.out.println("- Timestamp: When it occurred");
        System.out.println("- Source: MBean that sent it");
        System.out.println("- User Data: Additional context");
        
        System.out.println("\nUse Cases:");
        System.out.println("- Configuration changes");
        System.out.println("- State transitions");
        System.out.println("- Threshold violations");
        System.out.println("- Error conditions");
        System.out.println("- Performance alerts");
    }
}
