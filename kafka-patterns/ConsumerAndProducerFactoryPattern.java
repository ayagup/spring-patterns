package com.example.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Consumer and Producer Factory Pattern
 *
 * Demonstrates how to explicitly configure `ConsumerFactory` and `ProducerFactory` beans.
 * While Spring Boot auto-configuration is convenient, manual configuration provides
 * granular control over Kafka client properties, serializers/deserializers, and allows
 * for connecting to multiple clusters or using different configurations within the same application.
 *
 * Key Features:
 * - Centralized configuration for producers and consumers.
 * - Ability to define multiple, distinct factory beans for different use cases.
 * - Customization of serializers, deserializers, interceptors, and other properties.
 *
 * Use Cases:
 * - Connecting to more than one Kafka cluster.
 * - Using different serialization strategies (e.g., String, JSON, Avro) for different topics.
 * - Overriding default Spring Boot properties for fine-tuned performance or behavior.
 * - Setting up factories for specific delivery semantics (e.g., transactional producers).
 *
 * @author Spring Patterns
 */
@SpringBootApplication
public class ConsumerAndProducerFactoryPattern {

    public static void main(String[] args) {
        SpringApplication.run(ConsumerAndProducerFactoryPattern.class, args);
    }
}

class CustomData {
    private String id;
    private String payload;
    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
}

@Configuration
class KafkaFactoryConfig {

    private static final String BOOTSTRAP_SERVERS = "localhost:9092";

    // --- STRING PRODUCER/CONSUMER ---

    @Bean
    public ProducerFactory<String, String> stringProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        // Add any other producer properties here
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, String> stringKafkaTemplate() {
        return new KafkaTemplate<>(stringProducerFactory());
    }

    @Bean
    public ConsumerFactory<String, String> stringConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "string-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    // --- JSON (CUSTOM OBJECT) PRODUCER/CONSUMER ---

    @Bean
    public ProducerFactory<String, CustomData> jsonProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    @Qualifier("jsonKafkaTemplate")
    public KafkaTemplate<String, CustomData> jsonKafkaTemplate() {
        return new KafkaTemplate<>(jsonProducerFactory());
    }

    @Bean
    public ConsumerFactory<String, CustomData> jsonConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "json-group");
        // Configure the JsonDeserializer
        JsonDeserializer<CustomData> deserializer = new JsonDeserializer<>(CustomData.class);
        deserializer.setRemoveTypeHeaders(false);
        deserializer.addTrustedPackages("*");
        deserializer.setUseTypeMapperForKey(true);

        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), deserializer);
    }
    
    // Note: A ConcurrentKafkaListenerContainerFactory would then be defined for each
    // ConsumerFactory to be used with @KafkaListener.
    // e.g., one for string messages and another for JSON messages.
}

// Example of how to use the custom templates
@RestController
@RequestMapping("/api/factories")
class FactoryExampleController {

    private final KafkaTemplate<String, String> stringTemplate;
    private final KafkaTemplate<String, CustomData> jsonTemplate;

    public FactoryExampleController(
            KafkaTemplate<String, String> stringTemplate,
            @Qualifier("jsonKafkaTemplate") KafkaTemplate<String, CustomData> jsonTemplate) {
        this.stringTemplate = stringTemplate;
        this.jsonTemplate = jsonTemplate;
    }

    @PostMapping("/send-string")
    public String sendString(@RequestBody String message) {
        stringTemplate.send("string-topic", message);
        return "Sent string message via custom factory.";
    }

    @PostMapping("/send-json")
    public String sendJson(@RequestBody CustomData data) {
        jsonTemplate.send("json-topic", data);
        return "Sent JSON message via custom factory.";
    }
    
    @GetMapping("/info")
    public Map<String, String> getInfo() {
        return Map.of(
            "pattern", "Consumer and Producer Factory Pattern",
            "description", "Demonstrates explicit configuration of ProducerFactory and ConsumerFactory beans.",
            "features", "Separate factories for String and JSON serialization, custom KafkaTemplate beans.",
            "note", "This pattern is foundational for applications with diverse Kafka configuration needs."
        );
    }
}
