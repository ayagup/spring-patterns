package com.example.conditional;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Conditional On Class Pattern
 * ============================
 * 
 * Demonstrates @ConditionalOnClass annotation that creates beans only when
 * specific classes are present on the classpath. This is fundamental to
 * Spring Boot's auto-configuration mechanism.
 * 
 * Key Concepts:
 * ------------
 * 1. @ConditionalOnClass - Bean registration based on class presence
 * 2. Classpath Detection - Check for library availability
 * 3. Auto-Configuration - Automatic feature enablement
 * 4. Conditional Bean Creation - Register beans conditionally
 * 5. Dependency Detection - Detect optional dependencies
 * 
 * How It Works:
 * ------------
 * - Checks if specified classes exist on classpath
 * - If ALL classes present → condition matches → bean created
 * - If ANY class missing → condition fails → bean skipped
 * - Uses ASM to check class presence (no class loading)
 * - Works with class references or string names
 * 
 * Common Use Cases:
 * ----------------
 * - Auto-configure when library present
 * - Enable features based on dependencies
 * - Provide default implementations
 * - Configure integration beans
 * - Support optional dependencies
 * 
 * Syntax:
 * ------
 * @ConditionalOnClass(ClassName.class)
 * @ConditionalOnClass({Class1.class, Class2.class})
 * @ConditionalOnClass(name = "com.example.ClassName")
 * @ConditionalOnClass(name = {"com.pkg.Class1", "com.pkg.Class2"})
 * 
 * Important Notes:
 * ---------------
 * - Condition evaluated at bean registration time
 * - Uses class name strings to avoid loading classes
 * - Can combine with other conditionals
 * - Order matters when multiple conditions present
 * - Commonly used in @Configuration classes
 * 
 * @author Spring Patterns
 * @version 1.0
 */

/**
 * Example 1: DataSource Configuration (when H2 present)
 */
@Configuration
@ConditionalOnClass(name = "org.h2.Driver")
class H2DataSourceConfiguration {
    
    @Bean
    public String h2DataSource() {
        System.out.println("Creating H2 DataSource (H2 driver found on classpath)");
        return "H2 DataSource Bean";
    }
    
    @Bean
    public String h2JdbcTemplate() {
        System.out.println("Creating H2 JDBC Template");
        return "H2 JDBC Template";
    }
}

/**
 * Example 2: Cache Configuration (when Redis present)
 */
@Configuration
@ConditionalOnClass(name = "org.springframework.data.redis.core.RedisTemplate")
class RedisCacheConfiguration {
    
    @Bean
    public String redisCacheManager() {
        System.out.println("Creating Redis Cache Manager (Redis found)");
        return "Redis Cache Manager";
    }
    
    @Bean
    public String redisConnectionFactory() {
        System.out.println("Creating Redis Connection Factory");
        return "Redis Connection Factory";
    }
}

/**
 * Example 3: JPA Configuration (when Hibernate present)
 */
@Configuration
@ConditionalOnClass(name = {
    "org.hibernate.SessionFactory",
    "javax.persistence.EntityManager"
})
class JpaConfiguration {
    
    @Bean
    public String entityManagerFactory() {
        System.out.println("Creating EntityManagerFactory (Hibernate & JPA found)");
        return "EntityManagerFactory Bean";
    }
    
    @Bean
    public String jpaTransactionManager() {
        System.out.println("Creating JPA Transaction Manager");
        return "JPA TransactionManager";
    }
}

/**
 * Example 4: Jackson JSON Configuration
 */
@Configuration
@ConditionalOnClass(name = "com.fasterxml.jackson.databind.ObjectMapper")
class JacksonConfiguration {
    
    @Bean
    public String jacksonObjectMapper() {
        System.out.println("Creating Jackson ObjectMapper (Jackson found)");
        return "Jackson ObjectMapper";
    }
    
    @Bean
    public String jsonMessageConverter() {
        System.out.println("Creating JSON Message Converter");
        return "JSON Message Converter";
    }
}

/**
 * Example 5: Kafka Configuration
 */
@Configuration
@ConditionalOnClass(name = "org.springframework.kafka.core.KafkaTemplate")
class KafkaConfiguration {
    
    @Bean
    public String kafkaTemplate() {
        System.out.println("Creating Kafka Template (Kafka found)");
        return "Kafka Template";
    }
    
    @Bean
    public String kafkaListenerContainerFactory() {
        System.out.println("Creating Kafka Listener Container Factory");
        return "Kafka Listener Container Factory";
    }
}

/**
 * Example 6: RabbitMQ Configuration
 */
@Configuration
@ConditionalOnClass(name = {
    "org.springframework.amqp.rabbit.core.RabbitTemplate",
    "com.rabbitmq.client.Channel"
})
class RabbitMQConfiguration {
    
    @Bean
    public String rabbitTemplate() {
        System.out.println("Creating RabbitTemplate (RabbitMQ found)");
        return "RabbitTemplate";
    }
    
    @Bean
    public String rabbitConnectionFactory() {
        System.out.println("Creating Rabbit Connection Factory");
        return "Rabbit Connection Factory";
    }
}

/**
 * Example 7: Elasticsearch Configuration
 */
@Configuration
@ConditionalOnClass(name = "org.elasticsearch.client.RestHighLevelClient")
class ElasticsearchConfiguration {
    
    @Bean
    public String elasticsearchClient() {
        System.out.println("Creating Elasticsearch Client (Elasticsearch found)");
        return "Elasticsearch Client";
    }
    
    @Bean
    public String elasticsearchTemplate() {
        System.out.println("Creating Elasticsearch Template");
        return "Elasticsearch Template";
    }
}

/**
 * Example 8: MongoDB Configuration
 */
@Configuration
@ConditionalOnClass(name = "com.mongodb.client.MongoClient")
class MongoDBConfiguration {
    
    @Bean
    public String mongoClient() {
        System.out.println("Creating Mongo Client (MongoDB driver found)");
        return "Mongo Client";
    }
    
    @Bean
    public String mongoTemplate() {
        System.out.println("Creating Mongo Template");
        return "Mongo Template";
    }
}

/**
 * Example 9: WebFlux Configuration (Reactive)
 */
@Configuration
@ConditionalOnClass(name = {
    "org.springframework.web.reactive.function.client.WebClient",
    "reactor.core.publisher.Flux"
})
class WebFluxConfiguration {
    
    @Bean
    public String webClient() {
        System.out.println("Creating WebClient (WebFlux & Reactor found)");
        return "WebClient Bean";
    }
    
    @Bean
    public String reactiveWebServerFactory() {
        System.out.println("Creating Reactive Web Server Factory");
        return "Reactive Web Server Factory";
    }
}

/**
 * Example 10: Security Configuration
 */
@Configuration
@ConditionalOnClass(name = "org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter")
class SecurityAutoConfiguration {
    
    @Bean
    public String securityFilterChain() {
        System.out.println("Creating Security Filter Chain (Spring Security found)");
        return "Security Filter Chain";
    }
    
    @Bean
    public String authenticationManager() {
        System.out.println("Creating Authentication Manager");
        return "Authentication Manager";
    }
}

/**
 * Example 11: Actuator Configuration
 */
@Configuration
@ConditionalOnClass(name = "org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointAutoConfiguration")
class ActuatorConfiguration {
    
    @Bean
    public String healthEndpoint() {
        System.out.println("Creating Health Endpoint (Actuator found)");
        return "Health Endpoint";
    }
    
    @Bean
    public String metricsEndpoint() {
        System.out.println("Creating Metrics Endpoint");
        return "Metrics Endpoint";
    }
}

/**
 * Main Pattern Class
 */
@Configuration
public class ConditionalOnClassPattern {
    
    /**
     * Example: Single class condition
     */
    @Bean
    @ConditionalOnClass(name = "org.slf4j.Logger")
    public String slf4jLogger() {
        System.out.println("Creating SLF4J Logger (SLF4J found)");
        return "SLF4J Logger";
    }
    
    /**
     * Example: Multiple classes condition (ALL must be present)
     */
    @Bean
    @ConditionalOnClass(name = {
        "org.springframework.web.servlet.DispatcherServlet",
        "javax.servlet.Servlet"
    })
    public String webMvcConfiguration() {
        System.out.println("Creating Web MVC Config (Servlet & DispatcherServlet found)");
        return "Web MVC Configuration";
    }
}

/**
 * Usage Examples and Best Practices
 */
class ConditionalOnClassUsageExamples {
    
    public static void main(String[] args) {
        System.out.println("Conditional On Class Pattern");
        System.out.println("=============================\n");
        
        System.out.println("Purpose:");
        System.out.println("- Register beans only when specific classes are on classpath");
        System.out.println("- Enable auto-configuration based on dependencies");
        System.out.println("- Support optional features\n");
        
        System.out.println("Syntax Options:");
        System.out.println("1. Class reference: @ConditionalOnClass(Redis.class)");
        System.out.println("2. Class name: @ConditionalOnClass(name = \"com.example.Redis\")");
        System.out.println("3. Multiple classes: @ConditionalOnClass({A.class, B.class})");
        System.out.println("4. Multiple names: @ConditionalOnClass(name = {\"A\", \"B\"})\n");
        
        System.out.println("Common Use Cases:");
        System.out.println("1. Database auto-configuration (H2, MySQL, PostgreSQL)");
        System.out.println("2. Cache providers (Redis, Hazelcast, Caffeine)");
        System.out.println("3. Message brokers (Kafka, RabbitMQ, ActiveMQ)");
        System.out.println("4. Data stores (MongoDB, Elasticsearch, Cassandra)");
        System.out.println("5. Web frameworks (WebFlux, WebMVC)");
        System.out.println("6. Security frameworks (Spring Security)");
        System.out.println("7. Serialization (Jackson, GSON, Protobuf)\n");
        
        System.out.println("Best Practices:");
        System.out.println("- Use class names (strings) to avoid loading classes");
        System.out.println("- Check for key classes that indicate library presence");
        System.out.println("- Combine with @ConditionalOnMissingBean for defaults");
        System.out.println("- Use in @Configuration classes for auto-configuration");
        System.out.println("- Document why specific classes are checked");
        System.out.println("- Order conditions from specific to general\n");
        
        System.out.println("Condition Evaluation:");
        System.out.println("- ALL specified classes must be present");
        System.out.println("- If ANY class is missing, condition fails");
        System.out.println("- Uses ASM to check (no class loading)");
        System.out.println("- Evaluated at configuration parsing time");
    }
}
