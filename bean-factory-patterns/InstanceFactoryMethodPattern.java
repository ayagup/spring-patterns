package com.spring.patterns.factory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Instance Factory Method Pattern
 * 
 * Instance factory methods require a factory bean instance to create other beans.
 * Unlike static factory methods, these require the factory object to exist first.
 * 
 * Characteristics:
 * - Requires factory bean instance
 * - Factory bean is created first
 * - Instance methods create product beans
 * - Can maintain state in factory
 * - Factory can be injected with dependencies
 * - More flexible than static factories
 * 
 * Use Cases:
 * - Stateful bean creation
 * - Complex bean families
 * - Factory with dependencies
 * - Dynamic configuration
 * - Protocol implementations
 * - Strategy pattern factories
 */
@SpringBootApplication
public class InstanceFactoryMethodPattern {

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(InstanceFactoryMethodPattern.class, args);
        
        System.out.println("\n=== Instance Factory Method Pattern ===");
        
        // Example 1: Message sender created by factory
        MessageSender emailSender = context.getBean("emailMessageSender", MessageSender.class);
        emailSender.send("test@example.com", "Hello Email");
        
        MessageSender smsSender = context.getBean("smsMessageSender", MessageSender.class);
        smsSender.send("1234567890", "Hello SMS");
        
        // Example 2: Database client from factory
        DatabaseClient postgresClient = context.getBean("postgresClient", DatabaseClient.class);
        postgresClient.query("SELECT * FROM users");
        
        DatabaseClient mongoClient = context.getBean("mongoClient", DatabaseClient.class);
        mongoClient.query("db.users.find()");
        
        // Example 3: HTTP client from configurable factory
        HttpClient restClient = context.getBean("restHttpClient", HttpClient.class);
        restClient.get("https://api.example.com/users");
        
        // Example 4: Report generator
        ReportGenerator pdfReport = context.getBean("pdfReportGenerator", ReportGenerator.class);
        pdfReport.generate("Monthly Sales");
    }
}

/**
 * Configuration with Instance Factory Methods
 */
@Configuration
class InstanceFactoryConfig {
    
    /**
     * Factory Bean 1: Message Sender Factory
     */
    @Bean
    public MessageSenderFactory messageSenderFactory() {
        System.out.println("Creating MessageSenderFactory");
        MessageSenderFactory factory = new MessageSenderFactory();
        factory.setDefaultTimeout(5000);
        factory.setRetryAttempts(3);
        return factory;
    }
    
    /**
     * Products from MessageSenderFactory
     */
    @Bean
    public MessageSender emailMessageSender(MessageSenderFactory factory) {
        System.out.println("Creating Email MessageSender via instance factory");
        return factory.createEmailSender("smtp.gmail.com", 587);
    }
    
    @Bean
    public MessageSender smsMessageSender(MessageSenderFactory factory) {
        System.out.println("Creating SMS MessageSender via instance factory");
        return factory.createSmsSender("api.twilio.com", "API_KEY_123");
    }
    
    @Bean
    public MessageSender pushMessageSender(MessageSenderFactory factory) {
        return factory.createPushSender("fcm.googleapis.com");
    }
    
    /**
     * Factory Bean 2: Database Client Factory
     */
    @Bean
    public DatabaseClientFactory databaseClientFactory() {
        System.out.println("Creating DatabaseClientFactory");
        return new DatabaseClientFactory();
    }
    
    /**
     * Products from DatabaseClientFactory
     */
    @Bean
    public DatabaseClient postgresClient(DatabaseClientFactory factory) {
        System.out.println("Creating PostgreSQL client via instance factory");
        return factory.createPostgresClient("localhost", 5432, "mydb");
    }
    
    @Bean
    public DatabaseClient mongoClient(DatabaseClientFactory factory) {
        System.out.println("Creating MongoDB client via instance factory");
        return factory.createMongoClient("localhost", 27017, "mydb");
    }
    
    @Bean
    public DatabaseClient redisClient(DatabaseClientFactory factory) {
        return factory.createRedisClient("localhost", 6379);
    }
    
    /**
     * Factory Bean 3: HTTP Client Factory (with configuration)
     */
    @Bean
    public HttpClientFactory httpClientFactory(HttpConfiguration httpConfig) {
        System.out.println("Creating HttpClientFactory with configuration");
        return new HttpClientFactory(httpConfig);
    }
    
    @Bean
    public HttpConfiguration httpConfiguration() {
        HttpConfiguration config = new HttpConfiguration();
        config.setConnectTimeout(10000);
        config.setReadTimeout(30000);
        config.setMaxRetries(3);
        return config;
    }
    
    /**
     * Products from HttpClientFactory
     */
    @Bean
    public HttpClient restHttpClient(HttpClientFactory factory) {
        System.out.println("Creating REST HTTP client via instance factory");
        return factory.createRestClient();
    }
    
    @Bean
    public HttpClient soapHttpClient(HttpClientFactory factory) {
        return factory.createSoapClient();
    }
    
    /**
     * Factory Bean 4: Report Generator Factory
     */
    @Bean
    public ReportGeneratorFactory reportGeneratorFactory() {
        System.out.println("Creating ReportGeneratorFactory");
        return new ReportGeneratorFactory();
    }
    
    /**
     * Products from ReportGeneratorFactory
     */
    @Bean
    public ReportGenerator pdfReportGenerator(ReportGeneratorFactory factory) {
        System.out.println("Creating PDF ReportGenerator via instance factory");
        return factory.createPdfGenerator();
    }
    
    @Bean
    public ReportGenerator excelReportGenerator(ReportGeneratorFactory factory) {
        return factory.createExcelGenerator();
    }
    
    @Bean
    public ReportGenerator csvReportGenerator(ReportGeneratorFactory factory) {
        return factory.createCsvGenerator();
    }
}

/**
 * Example 1: Message Sender Factory and Products
 */
class MessageSenderFactory {
    private int defaultTimeout;
    private int retryAttempts;
    
    public void setDefaultTimeout(int defaultTimeout) {
        this.defaultTimeout = defaultTimeout;
    }
    
    public void setRetryAttempts(int retryAttempts) {
        this.retryAttempts = retryAttempts;
    }
    
    /**
     * Instance factory method for Email
     */
    public MessageSender createEmailSender(String host, int port) {
        EmailMessageSender sender = new EmailMessageSender(host, port);
        sender.setTimeout(defaultTimeout);
        sender.setRetryAttempts(retryAttempts);
        return sender;
    }
    
    /**
     * Instance factory method for SMS
     */
    public MessageSender createSmsSender(String apiUrl, String apiKey) {
        SmsMessageSender sender = new SmsMessageSender(apiUrl, apiKey);
        sender.setTimeout(defaultTimeout);
        sender.setRetryAttempts(retryAttempts);
        return sender;
    }
    
    /**
     * Instance factory method for Push
     */
    public MessageSender createPushSender(String fcmUrl) {
        PushMessageSender sender = new PushMessageSender(fcmUrl);
        sender.setTimeout(defaultTimeout);
        sender.setRetryAttempts(retryAttempts);
        return sender;
    }
}

interface MessageSender {
    void send(String recipient, String message);
}

class EmailMessageSender implements MessageSender {
    private final String host;
    private final int port;
    private int timeout;
    private int retryAttempts;
    
    public EmailMessageSender(String host, int port) {
        this.host = host;
        this.port = port;
        System.out.println("   EmailMessageSender created: " + host + ":" + port);
    }
    
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
    
    public void setRetryAttempts(int retryAttempts) {
        this.retryAttempts = retryAttempts;
    }
    
    @Override
    public void send(String recipient, String message) {
        System.out.println("\n1. Email sent via " + host);
        System.out.println("   To: " + recipient);
        System.out.println("   Message: " + message);
        System.out.println("   Timeout: " + timeout + "ms, Retries: " + retryAttempts);
    }
}

class SmsMessageSender implements MessageSender {
    private final String apiUrl;
    private final String apiKey;
    private int timeout;
    private int retryAttempts;
    
    public SmsMessageSender(String apiUrl, String apiKey) {
        this.apiUrl = apiUrl;
        this.apiKey = apiKey;
        System.out.println("   SmsMessageSender created: " + apiUrl);
    }
    
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
    
    public void setRetryAttempts(int retryAttempts) {
        this.retryAttempts = retryAttempts;
    }
    
    @Override
    public void send(String recipient, String message) {
        System.out.println("\n1. SMS sent via " + apiUrl);
        System.out.println("   To: " + recipient);
        System.out.println("   Message: " + message);
        System.out.println("   API Key: " + apiKey);
    }
}

class PushMessageSender implements MessageSender {
    private final String fcmUrl;
    private int timeout;
    private int retryAttempts;
    
    public PushMessageSender(String fcmUrl) {
        this.fcmUrl = fcmUrl;
        System.out.println("   PushMessageSender created: " + fcmUrl);
    }
    
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
    
    public void setRetryAttempts(int retryAttempts) {
        this.retryAttempts = retryAttempts;
    }
    
    @Override
    public void send(String recipient, String message) {
        System.out.println("   Push notification sent via " + fcmUrl);
    }
}

/**
 * Example 2: Database Client Factory and Products
 */
class DatabaseClientFactory {
    private final Map<String, DatabaseClient> clientCache = new HashMap<>();
    
    /**
     * Instance factory method for PostgreSQL
     */
    public DatabaseClient createPostgresClient(String host, int port, String database) {
        String key = "postgres:" + host + ":" + port + ":" + database;
        return clientCache.computeIfAbsent(key, k -> 
            new PostgresClient(host, port, database)
        );
    }
    
    /**
     * Instance factory method for MongoDB
     */
    public DatabaseClient createMongoClient(String host, int port, String database) {
        String key = "mongo:" + host + ":" + port + ":" + database;
        return clientCache.computeIfAbsent(key, k -> 
            new MongoClient(host, port, database)
        );
    }
    
    /**
     * Instance factory method for Redis
     */
    public DatabaseClient createRedisClient(String host, int port) {
        String key = "redis:" + host + ":" + port;
        return clientCache.computeIfAbsent(key, k -> 
            new RedisClient(host, port)
        );
    }
}

interface DatabaseClient {
    void query(String query);
    void connect();
    void disconnect();
}

class PostgresClient implements DatabaseClient {
    private final String host;
    private final int port;
    private final String database;
    
    public PostgresClient(String host, int port, String database) {
        this.host = host;
        this.port = port;
        this.database = database;
        System.out.println("   PostgresClient created: " + host + ":" + port + "/" + database);
    }
    
    @Override
    public void query(String query) {
        System.out.println("\n2. PostgreSQL query executed:");
        System.out.println("   Database: " + database);
        System.out.println("   Query: " + query);
    }
    
    @Override
    public void connect() {
        System.out.println("   Connected to PostgreSQL: " + host);
    }
    
    @Override
    public void disconnect() {
        System.out.println("   Disconnected from PostgreSQL");
    }
}

class MongoClient implements DatabaseClient {
    private final String host;
    private final int port;
    private final String database;
    
    public MongoClient(String host, int port, String database) {
        this.host = host;
        this.port = port;
        this.database = database;
        System.out.println("   MongoClient created: " + host + ":" + port + "/" + database);
    }
    
    @Override
    public void query(String query) {
        System.out.println("\n2. MongoDB query executed:");
        System.out.println("   Database: " + database);
        System.out.println("   Query: " + query);
    }
    
    @Override
    public void connect() {
        System.out.println("   Connected to MongoDB: " + host);
    }
    
    @Override
    public void disconnect() {
        System.out.println("   Disconnected from MongoDB");
    }
}

class RedisClient implements DatabaseClient {
    private final String host;
    private final int port;
    
    public RedisClient(String host, int port) {
        this.host = host;
        this.port = port;
        System.out.println("   RedisClient created: " + host + ":" + port);
    }
    
    @Override
    public void query(String query) {
        System.out.println("   Redis command: " + query);
    }
    
    @Override
    public void connect() {
        System.out.println("   Connected to Redis: " + host);
    }
    
    @Override
    public void disconnect() {
        System.out.println("   Disconnected from Redis");
    }
}

/**
 * Example 3: HTTP Client Factory with Configuration
 */
class HttpConfiguration {
    private int connectTimeout;
    private int readTimeout;
    private int maxRetries;
    
    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }
    
    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }
    
    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }
    
    public int getConnectTimeout() {
        return connectTimeout;
    }
    
    public int getReadTimeout() {
        return readTimeout;
    }
    
    public int getMaxRetries() {
        return maxRetries;
    }
}

class HttpClientFactory {
    private final HttpConfiguration config;
    
    public HttpClientFactory(HttpConfiguration config) {
        this.config = config;
        System.out.println("   HttpClientFactory configured: timeout=" + 
                         config.getConnectTimeout());
    }
    
    /**
     * Instance factory method for REST client
     */
    public HttpClient createRestClient() {
        RestHttpClient client = new RestHttpClient();
        client.setConnectTimeout(config.getConnectTimeout());
        client.setReadTimeout(config.getReadTimeout());
        client.setMaxRetries(config.getMaxRetries());
        return client;
    }
    
    /**
     * Instance factory method for SOAP client
     */
    public HttpClient createSoapClient() {
        SoapHttpClient client = new SoapHttpClient();
        client.setConnectTimeout(config.getConnectTimeout());
        client.setReadTimeout(config.getReadTimeout());
        return client;
    }
}

interface HttpClient {
    void get(String url);
    void post(String url, String body);
}

class RestHttpClient implements HttpClient {
    private int connectTimeout;
    private int readTimeout;
    private int maxRetries;
    
    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }
    
    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }
    
    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }
    
    @Override
    public void get(String url) {
        System.out.println("\n3. REST GET request:");
        System.out.println("   URL: " + url);
        System.out.println("   Timeout: " + connectTimeout + "ms");
        System.out.println("   Max Retries: " + maxRetries);
    }
    
    @Override
    public void post(String url, String body) {
        System.out.println("   REST POST: " + url);
    }
}

class SoapHttpClient implements HttpClient {
    private int connectTimeout;
    private int readTimeout;
    
    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }
    
    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }
    
    @Override
    public void get(String url) {
        System.out.println("   SOAP request: " + url);
    }
    
    @Override
    public void post(String url, String body) {
        System.out.println("   SOAP POST: " + url);
    }
}

/**
 * Example 4: Report Generator Factory
 */
class ReportGeneratorFactory {
    
    public ReportGenerator createPdfGenerator() {
        System.out.println("   Creating PDF generator with default settings");
        return new PdfReportGenerator();
    }
    
    public ReportGenerator createExcelGenerator() {
        return new ExcelReportGenerator();
    }
    
    public ReportGenerator createCsvGenerator() {
        return new CsvReportGenerator();
    }
}

interface ReportGenerator {
    void generate(String reportName);
}

class PdfReportGenerator implements ReportGenerator {
    @Override
    public void generate(String reportName) {
        System.out.println("\n4. Generating PDF report: " + reportName);
        System.out.println("   Format: PDF");
    }
}

class ExcelReportGenerator implements ReportGenerator {
    @Override
    public void generate(String reportName) {
        System.out.println("   Generating Excel report: " + reportName);
    }
}

class CsvReportGenerator implements ReportGenerator {
    @Override
    public void generate(String reportName) {
        System.out.println("   Generating CSV report: " + reportName);
    }
}

/**
 * REST Controller
 */
@RestController
@RequestMapping("/api/instance-factory")
class InstanceFactoryController {
    
    private final MessageSender emailSender;
    private final DatabaseClient postgresClient;
    
    public InstanceFactoryController(MessageSender emailMessageSender,
                                    DatabaseClient postgresClient) {
        this.emailSender = emailMessageSender;
        this.postgresClient = postgresClient;
    }
    
    @GetMapping("/send-email/{recipient}")
    public String sendEmail(@PathVariable String recipient) {
        emailSender.send(recipient, "Test message");
        return "Email sent to: " + recipient;
    }
    
    @GetMapping("/query")
    public String executeQuery() {
        postgresClient.query("SELECT * FROM users");
        return "Query executed";
    }
}

/**
 * Key Points:
 * 
 * 1. Instance Factory Method Pattern:
 *    - Factory bean is created first
 *    - Instance methods create product beans
 *    - Factory can have state and dependencies
 * 
 * 2. Configuration:
 *    @Bean
 *    public Factory factory() {
 *        return new Factory();
 *    }
 *    
 *    @Bean
 *    public Product product(Factory factory) {
 *        return factory.createProduct();
 *    }
 * 
 * 3. Advantages:
 *    ✓ Factory can maintain state
 *    ✓ Factory can have dependencies
 *    ✓ More flexible than static
 *    ✓ Can cache instances
 *    ✓ Can apply common configuration
 *    ✓ Can create bean families
 * 
 * 4. Use Cases:
 *    ✓ Stateful bean creation
 *    ✓ Related beans (families)
 *    ✓ Common configuration sharing
 *    ✓ Complex initialization
 *    ✓ Instance caching
 *    ✓ Protocol implementations
 * 
 * 5. Comparison:
 *    Static: MyClass.getInstance()
 *    Instance: factory.createInstance()
 * 
 * 6. Best Practices:
 *    ✓ Share configuration via factory
 *    ✓ Cache instances when appropriate
 *    ✓ Clear factory method names
 *    ✓ Document factory dependencies
 *    ✓ Consider thread safety
 */
