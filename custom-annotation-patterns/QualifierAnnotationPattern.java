package com.example.customannotation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.lang.annotation.*;

/**
 * Qualifier Annotation Pattern
 * 
 * Demonstrates creating custom qualifier annotations for dependency injection disambiguation.
 * When multiple beans of the same type exist, qualifiers specify which one to inject:
 * - Bean selection by name/type
 * - Custom qualifier annotations
 * - Meta-qualifier patterns
 * - Database/cache selection
 * - Feature toggle qualifiers
 * 
 * Key Features:
 * - Bean disambiguation
 * - Type-safe qualifiers
 * - Custom qualifier attributes
 * - Meta-qualifiers
 * - Conditional injection
 * 
 * Use Cases:
 * - Multiple database connections
 * - Cache provider selection
 * - Strategy pattern implementation
 * - Multi-tenant applications
 * - Feature-specific beans
 * 
 * @author Spring Patterns
 */
@SpringBootApplication
public class QualifierAnnotationPattern {

    public static void main(String[] args) {
        SpringApplication.run(QualifierAnnotationPattern.class, args);
    }

    // =========================================================================
    // CUSTOM QUALIFIER ANNOTATIONS
    // =========================================================================

    /**
     * Database qualifier for primary/secondary databases
     */
    @Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Qualifier
    public @interface Database {
        
        /**
         * Database type
         */
        DatabaseType value() default DatabaseType.PRIMARY;
    }

    public enum DatabaseType {
        PRIMARY,
        SECONDARY,
        READONLY,
        ANALYTICS
    }

    /**
     * Cache provider qualifier
     */
    @Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Qualifier
    public @interface CacheProvider {
        
        /**
         * Cache type
         */
        CacheType value();
    }

    public enum CacheType {
        REDIS,
        MEMCACHED,
        EHCACHE,
        CAFFEINE
    }

    /**
     * Payment method qualifier
     */
    @Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Qualifier
    public @interface PaymentMethod {
        
        /**
         * Payment type
         */
        PaymentType value();
    }

    public enum PaymentType {
        CREDIT_CARD,
        DEBIT_CARD,
        PAYPAL,
        BANK_TRANSFER,
        CRYPTOCURRENCY
    }

    /**
     * Notification channel qualifier
     */
    @Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Qualifier
    public @interface NotificationChannel {
        
        /**
         * Channel type
         */
        ChannelType value();
    }

    public enum ChannelType {
        EMAIL,
        SMS,
        PUSH,
        WEBHOOK
    }

    /**
     * Storage provider qualifier
     */
    @Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Qualifier
    public @interface Storage {
        
        /**
         * Storage type
         */
        StorageType value();
    }

    public enum StorageType {
        LOCAL,
        S3,
        AZURE_BLOB,
        GOOGLE_CLOUD
    }

    /**
     * Region qualifier for multi-region deployment
     */
    @Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Qualifier
    public @interface Region {
        
        /**
         * Region code
         */
        String value();
    }

    /**
     * Environment qualifier
     */
    @Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Qualifier
    public @interface Environment {
        
        /**
         * Environment type
         */
        EnvironmentType value();
    }

    public enum EnvironmentType {
        DEVELOPMENT,
        STAGING,
        PRODUCTION
    }

    // =========================================================================
    // BEAN CONFIGURATIONS
    // =========================================================================

    @Configuration
    public static class DatabaseConfig {
        
        @Bean
        @Database(DatabaseType.PRIMARY)
        public DataSource primaryDataSource() {
            System.out.println("Creating primary data source");
            return new DataSource("jdbc:mysql://primary:3306/db", "primary");
        }
        
        @Bean
        @Database(DatabaseType.SECONDARY)
        public DataSource secondaryDataSource() {
            System.out.println("Creating secondary data source");
            return new DataSource("jdbc:mysql://secondary:3306/db", "secondary");
        }
        
        @Bean
        @Database(DatabaseType.READONLY)
        public DataSource readOnlyDataSource() {
            System.out.println("Creating read-only data source");
            return new DataSource("jdbc:mysql://readonly:3306/db", "readonly");
        }
        
        @Bean
        @Database(DatabaseType.ANALYTICS)
        public DataSource analyticsDataSource() {
            System.out.println("Creating analytics data source");
            return new DataSource("jdbc:mysql://analytics:3306/db", "analytics");
        }
    }

    @Configuration
    public static class CacheConfig {
        
        @Bean
        @CacheProvider(CacheType.REDIS)
        @Primary
        public CacheService redisCache() {
            System.out.println("Creating Redis cache");
            return new CacheService("Redis", "localhost:6379");
        }
        
        @Bean
        @CacheProvider(CacheType.MEMCACHED)
        public CacheService memcachedCache() {
            System.out.println("Creating Memcached cache");
            return new CacheService("Memcached", "localhost:11211");
        }
        
        @Bean
        @CacheProvider(CacheType.CAFFEINE)
        public CacheService caffeineCache() {
            System.out.println("Creating Caffeine cache");
            return new CacheService("Caffeine", "in-memory");
        }
    }

    @Configuration
    public static class PaymentConfig {
        
        @Bean
        @PaymentMethod(PaymentType.CREDIT_CARD)
        public PaymentProcessor creditCardProcessor() {
            System.out.println("Creating credit card processor");
            return new PaymentProcessor("Credit Card Gateway");
        }
        
        @Bean
        @PaymentMethod(PaymentType.PAYPAL)
        public PaymentProcessor paypalProcessor() {
            System.out.println("Creating PayPal processor");
            return new PaymentProcessor("PayPal Gateway");
        }
        
        @Bean
        @PaymentMethod(PaymentType.BANK_TRANSFER)
        public PaymentProcessor bankTransferProcessor() {
            System.out.println("Creating bank transfer processor");
            return new PaymentProcessor("Bank Transfer Gateway");
        }
    }

    @Configuration
    public static class NotificationConfig {
        
        @Bean
        @NotificationChannel(ChannelType.EMAIL)
        public NotificationService emailNotification() {
            System.out.println("Creating email notification service");
            return new NotificationService("Email", "smtp.example.com");
        }
        
        @Bean
        @NotificationChannel(ChannelType.SMS)
        public NotificationService smsNotification() {
            System.out.println("Creating SMS notification service");
            return new NotificationService("SMS", "sms-gateway.com");
        }
        
        @Bean
        @NotificationChannel(ChannelType.PUSH)
        public NotificationService pushNotification() {
            System.out.println("Creating push notification service");
            return new NotificationService("Push", "push-service.com");
        }
    }

    @Configuration
    public static class StorageConfig {
        
        @Bean
        @Storage(StorageType.S3)
        public StorageService s3Storage() {
            System.out.println("Creating S3 storage service");
            return new StorageService("S3", "s3://my-bucket");
        }
        
        @Bean
        @Storage(StorageType.LOCAL)
        public StorageService localStorage() {
            System.out.println("Creating local storage service");
            return new StorageService("Local", "/var/storage");
        }
        
        @Bean
        @Storage(StorageType.AZURE_BLOB)
        public StorageService azureStorage() {
            System.out.println("Creating Azure storage service");
            return new StorageService("Azure Blob", "https://account.blob.core.windows.net");
        }
    }

    // =========================================================================
    // SERVICE IMPLEMENTATIONS
    // =========================================================================

    /**
     * Service using database qualifiers
     */
    @Service
    public static class UserService {
        
        private final DataSource primaryDb;
        private final DataSource secondaryDb;
        private final DataSource readOnlyDb;

        @Autowired
        public UserService(
                @Database(DatabaseType.PRIMARY) DataSource primaryDb,
                @Database(DatabaseType.SECONDARY) DataSource secondaryDb,
                @Database(DatabaseType.READONLY) DataSource readOnlyDb) {
            this.primaryDb = primaryDb;
            this.secondaryDb = secondaryDb;
            this.readOnlyDb = readOnlyDb;
        }

        public void createUser(String username) {
            System.out.println("Creating user in primary DB: " + username);
            primaryDb.execute("INSERT INTO users...");
        }

        public void replicateUser(String username) {
            System.out.println("Replicating user to secondary DB: " + username);
            secondaryDb.execute("INSERT INTO users...");
        }

        public String getUser(String username) {
            System.out.println("Reading user from read-only DB: " + username);
            return readOnlyDb.query("SELECT * FROM users WHERE username = " + username);
        }
    }

    /**
     * Service using cache qualifiers
     */
    @Service
    public static class ProductService {
        
        private final CacheService redisCache;
        private final CacheService caffeineCache;

        @Autowired
        public ProductService(
                @CacheProvider(CacheType.REDIS) CacheService redisCache,
                @CacheProvider(CacheType.CAFFEINE) CacheService caffeineCache) {
            this.redisCache = redisCache;
            this.caffeineCache = caffeineCache;
        }

        public Product getProduct(String productId) {
            System.out.println("Getting product: " + productId);
            // Try local cache first
            Product product = caffeineCache.get("product:" + productId);
            if (product == null) {
                // Fallback to distributed cache
                product = redisCache.get("product:" + productId);
            }
            return product;
        }

        public void cacheProduct(Product product) {
            System.out.println("Caching product: " + product.getId());
            redisCache.put("product:" + product.getId(), product);
            caffeineCache.put("product:" + product.getId(), product);
        }
    }

    /**
     * Service using payment method qualifiers
     */
    @Service
    public static class OrderService {
        
        private final PaymentProcessor creditCardProcessor;
        private final PaymentProcessor paypalProcessor;

        @Autowired
        public OrderService(
                @PaymentMethod(PaymentType.CREDIT_CARD) PaymentProcessor creditCardProcessor,
                @PaymentMethod(PaymentType.PAYPAL) PaymentProcessor paypalProcessor) {
            this.creditCardProcessor = creditCardProcessor;
            this.paypalProcessor = paypalProcessor;
        }

        public String processOrder(String orderId, String paymentMethod, double amount) {
            System.out.println("Processing order: " + orderId);
            
            if ("CREDIT_CARD".equals(paymentMethod)) {
                return creditCardProcessor.process(orderId, amount);
            } else if ("PAYPAL".equals(paymentMethod)) {
                return paypalProcessor.process(orderId, amount);
            }
            
            throw new IllegalArgumentException("Unknown payment method: " + paymentMethod);
        }
    }

    /**
     * Service using notification channel qualifiers
     */
    @Service
    public static class NotificationManager {
        
        private final NotificationService emailService;
        private final NotificationService smsService;
        private final NotificationService pushService;

        @Autowired
        public NotificationManager(
                @NotificationChannel(ChannelType.EMAIL) NotificationService emailService,
                @NotificationChannel(ChannelType.SMS) NotificationService smsService,
                @NotificationChannel(ChannelType.PUSH) NotificationService pushService) {
            this.emailService = emailService;
            this.smsService = smsService;
            this.pushService = pushService;
        }

        public void sendNotification(String userId, String message, String channel) {
            System.out.println("Sending notification to: " + userId);
            
            switch (channel) {
                case "EMAIL":
                    emailService.send(userId, message);
                    break;
                case "SMS":
                    smsService.send(userId, message);
                    break;
                case "PUSH":
                    pushService.send(userId, message);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown channel: " + channel);
            }
        }

        public void sendMultiChannel(String userId, String message) {
            System.out.println("Sending multi-channel notification");
            emailService.send(userId, message);
            smsService.send(userId, message);
            pushService.send(userId, message);
        }
    }

    /**
     * Service using storage qualifiers
     */
    @Service
    public static class FileService {
        
        private final StorageService s3Storage;
        private final StorageService localStorage;

        @Autowired
        public FileService(
                @Storage(StorageType.S3) StorageService s3Storage,
                @Storage(StorageType.LOCAL) StorageService localStorage) {
            this.s3Storage = s3Storage;
            this.localStorage = localStorage;
        }

        public void saveFile(String filename, byte[] content, boolean cloud) {
            System.out.println("Saving file: " + filename);
            
            if (cloud) {
                s3Storage.save(filename, content);
            } else {
                localStorage.save(filename, content);
            }
        }

        public byte[] loadFile(String filename, boolean cloud) {
            System.out.println("Loading file: " + filename);
            
            if (cloud) {
                return s3Storage.load(filename);
            } else {
                return localStorage.load(filename);
            }
        }
    }

    // Helper Classes

    public static class DataSource {
        private final String url;
        private final String name;

        public DataSource(String url, String name) {
            this.url = url;
            this.name = name;
        }

        public void execute(String sql) {
            System.out.println("[" + name + "] Executing: " + sql);
        }

        public String query(String sql) {
            System.out.println("[" + name + "] Querying: " + sql);
            return "Result from " + name;
        }
    }

    public static class CacheService {
        private final String type;
        private final String connection;

        public CacheService(String type, String connection) {
            this.type = type;
            this.connection = connection;
        }

        public <T> T get(String key) {
            System.out.println("[" + type + "] Getting: " + key);
            return null;
        }

        public <T> void put(String key, T value) {
            System.out.println("[" + type + "] Putting: " + key);
        }
    }

    public static class PaymentProcessor {
        private final String gateway;

        public PaymentProcessor(String gateway) {
            this.gateway = gateway;
        }

        public String process(String orderId, double amount) {
            System.out.println("[" + gateway + "] Processing payment: " + orderId + ", $" + amount);
            return "PAYMENT_" + orderId;
        }
    }

    public static class NotificationService {
        private final String channel;
        private final String endpoint;

        public NotificationService(String channel, String endpoint) {
            this.channel = channel;
            this.endpoint = endpoint;
        }

        public void send(String userId, String message) {
            System.out.println("[" + channel + "] Sending to " + userId + ": " + message);
        }
    }

    public static class StorageService {
        private final String type;
        private final String location;

        public StorageService(String type, String location) {
            this.type = type;
            this.location = location;
        }

        public void save(String filename, byte[] content) {
            System.out.println("[" + type + "] Saving: " + filename);
        }

        public byte[] load(String filename) {
            System.out.println("[" + type + "] Loading: " + filename);
            return new byte[0];
        }
    }

    public static class Product {
        private String id;
        private String name;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
}

/**
 * DOCUMENTATION
 * 
 * Qualifier Annotation Pattern:
 * 
 * 1. Purpose:
 *    - Disambiguate beans of same type
 *    - Select specific bean for injection
 *    - Type-safe bean selection
 *    - Alternative to bean names
 * 
 * 2. Built-in @Qualifier:
 *    - @Qualifier("beanName"): Select by name
 *    - @Primary: Mark default bean
 *    - Custom qualifiers: Meta-annotated with @Qualifier
 * 
 * 3. Creating Custom Qualifiers:
 *    - Annotate with @Qualifier
 *    - Add @Target for allowed locations
 *    - Add @Retention(RUNTIME)
 *    - Define attributes (often enums)
 * 
 * 4. Common Patterns:
 *    - Database selection (@Database)
 *    - Cache provider (@CacheProvider)
 *    - Payment method (@PaymentMethod)
 *    - Notification channel (@NotificationChannel)
 *    - Storage backend (@Storage)
 *    - Region/environment (@Region, @Environment)
 * 
 * 5. Usage Locations:
 *    - Field injection: @Autowired @Database(PRIMARY)
 *    - Constructor injection: @Database(PRIMARY) DataSource ds
 *    - Method injection: @Autowired void setDs(@Database(PRIMARY) DataSource ds)
 *    - Bean definition: @Bean @Database(PRIMARY) DataSource primaryDs()
 * 
 * 6. Benefits:
 *    - Type safety (vs string names)
 *    - Better IDE support
 *    - Refactoring safety
 *    - Clear intent
 *    - Compile-time checks
 * 
 * 7. Best Practices:
 *    - Use enums for qualifier values
 *    - Create semantic qualifiers
 *    - Document qualifier purpose
 *    - Combine with @Primary for defaults
 *    - Use TARGET for appropriate locations
 * 
 * 8. Alternative: Bean Names:
 *    - @Qualifier("beanName")
 *    - Less type-safe
 *    - String-based
 *    - Harder to refactor
 *    - Custom qualifiers preferred
 * 
 * 9. Multiple Qualifiers:
 *    - Can apply multiple qualifiers
 *    - All must match for injection
 *    - Example: @Database(PRIMARY) @Region("US-EAST")
 * 
 * 10. Testing:
 *     - Use @MockBean with qualifiers
 *     - Verify correct bean injection
 *     - Test qualifier combinations
 */
