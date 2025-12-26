package com.example.conditional;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Conditional On Missing Class Pattern
 * ====================================
 * 
 * Demonstrates @ConditionalOnMissingClass annotation that creates beans only
 * when specific classes are NOT present on the classpath. This is used to
 * provide fallback implementations when optional dependencies are absent.
 * 
 * Key Concepts:
 * ------------
 * 1. @ConditionalOnMissingClass - Bean registration when class absent
 * 2. Fallback Implementations - Provide defaults when library missing
 * 3. Optional Dependency Handling - Work without optional libraries
 * 4. Alternative Providers - Use different implementations
 * 5. Graceful Degradation - Function without advanced features
 * 
 * How It Works:
 * ------------
 * - Checks if specified classes are ABSENT from classpath
 * - If ALL classes missing → condition matches → bean created
 * - If ANY class present → condition fails → bean skipped
 * - Opposite of @ConditionalOnClass
 * - Uses ASM for class detection (no loading)
 * 
 * Common Use Cases:
 * ----------------
 * - Provide in-memory cache when Redis absent
 * - Use H2 database when production DB unavailable
 * - Simple logger when SLF4J missing
 * - Basic serializer when Jackson absent
 * - Default implementations for optional features
 * 
 * Syntax:
 * ------
 * @ConditionalOnMissingClass("com.example.ClassName")
 * @ConditionalOnMissingClass({"com.pkg.Class1", "com.pkg.Class2"})
 * 
 * Important Notes:
 * ---------------
 * - Inverse of @ConditionalOnClass
 * - Use for fallback/default implementations
 * - Combine with @ConditionalOnClass for either/or scenarios
 * - Helpful for testing without optional dependencies
 * - Enables graceful feature degradation
 * 
 * @author Spring Patterns
 * @version 1.0
 */

/**
 * Example 1: In-Memory Cache (when Redis missing)
 */
@Configuration
@ConditionalOnMissingClass("org.springframework.data.redis.core.RedisTemplate")
class InMemoryCacheConfiguration {
    
    @Bean
    public String cacheManager() {
        System.out.println("Creating In-Memory Cache Manager (Redis not found)");
        System.out.println("  Using simple concurrent HashMap for caching");
        return "In-Memory Cache Manager";
    }
    
    @Bean
    public String simpleCacheStore() {
        System.out.println("Creating Simple Cache Store");
        return "Simple Cache Store";
    }
}

/**
 * Example 2: H2 Database (when production DB missing)
 */
@Configuration
@ConditionalOnMissingClass({
    "org.postgresql.Driver",
    "com.mysql.cj.jdbc.Driver",
    "oracle.jdbc.OracleDriver"
})
class H2FallbackConfiguration {
    
    @Bean
    public String h2DataSource() {
        System.out.println("Creating H2 DataSource (No production database found)");
        System.out.println("  Using embedded H2 for development");
        return "H2 DataSource";
    }
    
    @Bean
    public String h2Console() {
        System.out.println("Enabling H2 Console");
        return "H2 Console";
    }
}

/**
 * Example 3: Simple Logger (when SLF4J missing)
 */
@Configuration
@ConditionalOnMissingClass("org.slf4j.Logger")
class SimpleLoggerConfiguration {
    
    @Bean
    public String simpleLogger() {
        System.out.println("Creating Simple Logger (SLF4J not found)");
        System.out.println("  Using System.out for logging");
        return "Simple Console Logger";
    }
}

/**
 * Example 4: Basic JSON Serializer (when Jackson missing)
 */
@Configuration
@ConditionalOnMissingClass("com.fasterxml.jackson.databind.ObjectMapper")
class BasicJsonSerializerConfiguration {
    
    @Bean
    public String basicJsonSerializer() {
        System.out.println("Creating Basic JSON Serializer (Jackson not found)");
        System.out.println("  Using manual JSON serialization");
        return "Basic JSON Serializer";
    }
    
    @Bean
    public String jsonMessageConverter() {
        System.out.println("Creating Basic JSON Message Converter");
        return "Basic JSON Message Converter";
    }
}

/**
 * Example 5: Simple Message Queue (when Kafka/RabbitMQ missing)
 */
@Configuration
@ConditionalOnMissingClass({
    "org.springframework.kafka.core.KafkaTemplate",
    "org.springframework.amqp.rabbit.core.RabbitTemplate"
})
class SimpleMessageQueueConfiguration {
    
    @Bean
    public String inMemoryMessageQueue() {
        System.out.println("Creating In-Memory Message Queue (No message broker found)");
        System.out.println("  Using BlockingQueue for messaging");
        return "In-Memory Message Queue";
    }
    
    @Bean
    public String messageProcessor() {
        System.out.println("Creating Simple Message Processor");
        return "Message Processor";
    }
}

/**
 * Example 6: File-based Session Store (when Redis Session missing)
 */
@Configuration
@ConditionalOnMissingClass("org.springframework.session.data.redis.RedisSessionRepository")
class FileSessionStoreConfiguration {
    
    @Bean
    public String fileSessionRepository() {
        System.out.println("Creating File-based Session Repository (Redis Session not found)");
        System.out.println("  Storing sessions in file system");
        return "File Session Repository";
    }
}

/**
 * Example 7: Basic Security (when Spring Security missing)
 */
@Configuration
@ConditionalOnMissingClass("org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter")
class BasicSecurityConfiguration {
    
    @Bean
    public String basicAuthFilter() {
        System.out.println("Creating Basic Auth Filter (Spring Security not found)");
        System.out.println("  Using simple authentication");
        return "Basic Auth Filter";
    }
}

/**
 * Example 8: Simple Metrics (when Micrometer missing)
 */
@Configuration
@ConditionalOnMissingClass("io.micrometer.core.instrument.MeterRegistry")
class SimpleMetricsConfiguration {
    
    @Bean
    public String simpleMetricsCollector() {
        System.out.println("Creating Simple Metrics Collector (Micrometer not found)");
        System.out.println("  Using basic counters and timers");
        return "Simple Metrics Collector";
    }
}

/**
 * Example 9: Servlet Web (when WebFlux missing)
 */
@Configuration
@ConditionalOnMissingClass({
    "org.springframework.web.reactive.function.client.WebClient",
    "reactor.core.publisher.Flux"
})
class ServletWebConfiguration {
    
    @Bean
    public String servletWebServer() {
        System.out.println("Creating Servlet Web Server (WebFlux not found)");
        System.out.println("  Using traditional Servlet container");
        return "Servlet Web Server";
    }
    
    @Bean
    public String restTemplate() {
        System.out.println("Creating RestTemplate for synchronous HTTP");
        return "RestTemplate";
    }
}

/**
 * Example 10: Simple Template Engine (when Thymeleaf missing)
 */
@Configuration
@ConditionalOnMissingClass("org.thymeleaf.spring5.SpringTemplateEngine")
class SimpleTemplateEngineConfiguration {
    
    @Bean
    public String simpleTemplateResolver() {
        System.out.println("Creating Simple Template Resolver (Thymeleaf not found)");
        System.out.println("  Using basic string replacement");
        return "Simple Template Resolver";
    }
}

/**
 * Example 11: Local File Storage (when Cloud Storage missing)
 */
@Configuration
@ConditionalOnMissingClass({
    "com.amazonaws.services.s3.AmazonS3",
    "com.azure.storage.blob.BlobServiceClient",
    "com.google.cloud.storage.Storage"
})
class LocalFileStorageConfiguration {
    
    @Bean
    public String localFileStorage() {
        System.out.println("Creating Local File Storage (Cloud storage not found)");
        System.out.println("  Storing files on local disk");
        return "Local File Storage";
    }
}

/**
 * Main Pattern Class
 */
@Configuration
public class ConditionalOnMissingClassPattern {
    
    /**
     * Example: Fallback implementation
     */
    @Bean
    @ConditionalOnMissingClass("org.springframework.data.mongodb.core.MongoTemplate")
    public String inMemoryDataStore() {
        System.out.println("Creating In-Memory Data Store (MongoDB not found)");
        System.out.println("  Using ConcurrentHashMap for data storage");
        return "In-Memory Data Store";
    }
    
    /**
     * Example: Multiple missing classes
     */
    @Bean
    @ConditionalOnMissingClass({
        "org.elasticsearch.client.RestHighLevelClient",
        "org.apache.solr.client.solrj.SolrClient"
    })
    public String simpleSearchEngine() {
        System.out.println("Creating Simple Search Engine (No search engine found)");
        System.out.println("  Using basic text matching");
        return "Simple Search Engine";
    }
}

/**
 * Usage Examples and Best Practices
 */
class ConditionalOnMissingClassUsageExamples {
    
    public static void main(String[] args) {
        System.out.println("Conditional On Missing Class Pattern");
        System.out.println("=====================================\n");
        
        System.out.println("Purpose:");
        System.out.println("- Create beans when specific classes are NOT on classpath");
        System.out.println("- Provide fallback/default implementations");
        System.out.println("- Enable graceful degradation\n");
        
        System.out.println("Syntax:");
        System.out.println("@ConditionalOnMissingClass(\"com.example.ClassName\")");
        System.out.println("@ConditionalOnMissingClass({\"Class1\", \"Class2\"})\n");
        
        System.out.println("Common Use Cases:");
        System.out.println("1. In-memory cache when Redis missing");
        System.out.println("2. H2 database when production DB unavailable");
        System.out.println("3. Simple logger when SLF4J absent");
        System.out.println("4. Basic serializer when Jackson missing");
        System.out.println("5. Local storage when cloud storage absent");
        System.out.println("6. Servlet web when WebFlux missing");
        System.out.println("7. Simple messaging when broker absent\n");
        
        System.out.println("Best Practices:");
        System.out.println("- Use for fallback implementations");
        System.out.println("- Provide sensible defaults");
        System.out.println("- Document that it's a fallback");
        System.out.println("- Consider performance implications");
        System.out.println("- Test both with and without dependencies");
        System.out.println("- Combine with @ConditionalOnClass for either/or\n");
        
        System.out.println("Common Patterns:");
        System.out.println("// Prefer Redis if available, fallback to in-memory");
        System.out.println("@ConditionalOnClass(Redis.class) → Redis Cache");
        System.out.println("@ConditionalOnMissingClass(Redis) → In-Memory Cache\n");
        
        System.out.println("Benefits:");
        System.out.println("- Application works without optional dependencies");
        System.out.println("- Easier testing (no external dependencies needed)");
        System.out.println("- Graceful feature degradation");
        System.out.println("- Reduced deployment complexity");
    }
}
