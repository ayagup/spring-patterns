package com.example.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * KafkaListener Pattern
 *
 * Demonstrates how to consume messages from Kafka topics using the @KafkaListener annotation.
 * This is the primary mechanism for creating message-driven POJOs (Plain Old Java Objects)
 * in a Spring application.
 *
 * Key Features:
 * - Declarative, annotation-based message consumption.
 * - Automatic deserialization of message payloads.
 * - Support for consuming from multiple topics.
 * - Flexible method signatures to access message headers, keys, partitions, etc.
 * - Integration with custom error handlers and filtering.
 *
 * Use Cases:
 * - Processing real-time data streams.
 * - Implementing event-driven microservices.
 * - Offloading tasks to asynchronous workers.
 *
 * @author Spring Patterns
 */
@SpringBootApplication
public class KafkaListenerPattern {

    public static void main(String[] args) {
        SpringApplication.run(KafkaListenerPattern.class, args);
    }
}

// A simple message payload class (can be shared with producer)
class ConsumedMessage {
    private String content;
    private String sender;

    // Getters and setters
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }

    @Override
    public String toString() {
        return "ConsumedMessage{" +
                "content='" + content + '\'' +
                ", sender='" + sender + '\'' +
                '}';
    }
}

@Component
class MessageListeners {

    private final List<String> simpleMessages = new ArrayList<>();
    private final List<String> filteredMessages = new ArrayList<>();
    private final List<ConsumedMessage> objectMessages = new ArrayList<>();
    private final List<String> headerMessages = new ArrayList<>();

    /**
     * Simplest form of a listener.
     */
    @KafkaListener(topics = "simple-topic", groupId = "simple-group")
    public void listenSimple(String message) {
        System.out.println("Received simple message: " + message);
        simpleMessages.add(message);
    }

    /**
     * Listener with a filter. Only messages containing "important" will be processed.
     */
    @KafkaListener(topics = "filtered-topic", groupId = "filter-group", filter = "importantFilter")
    public void listenWithFilter(String message) {
        System.out.println("Received filtered message: " + message);
        filteredMessages.add(message);
    }

    /**
     * Listener that deserializes a JSON payload into a custom object.
     */
    @KafkaListener(topics = "object-topic", groupId = "object-group", containerFactory = "kafkaListenerContainerFactoryForObjects")
    public void listenObject(ConsumedMessage message) {
        System.out.println("Received object message: " + message);
        objectMessages.add(message);
    }

    /**
     * Listener that accesses message headers.
     */
    @KafkaListener(topics = "header-topic", groupId = "header-group")
    public void listenWithHeaders(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_TIMESTAMP) long ts) {
        String logMessage = String.format("Received message '%s' from topic '%s', partition '%d' at '%d'", message, topic, partition, ts);
        System.out.println(logMessage);
        headerMessages.add(logMessage);
    }

    /**
     * Listener that consumes the full ConsumerRecord.
     */
    @KafkaListener(topics = "record-topic", groupId = "record-group")
    public void listenFullRecord(ConsumerRecord<String, String> record) {
        System.out.println("Received full record: " + record.toString());
        // Can access key, value, headers, etc. from the record object
    }

    public List<String> getSimpleMessages() {
        return simpleMessages;
    }

    public List<String> getFilteredMessages() {
        return filteredMessages;
    }

    public List<ConsumedMessage> getObjectMessages() {
        return objectMessages;
    }

    public List<String> getHeaderMessages() {
        return headerMessages;
    }
}

@RestController
@RequestMapping("/api/kafka-listener")
class MessageLogController {

    private final MessageListeners listeners;

    public MessageLogController(MessageListeners listeners) {
        this.listeners = listeners;
    }

    @GetMapping("/simple")
    public List<String> getSimpleMessages() {
        return listeners.getSimpleMessages();
    }

    @GetMapping("/filtered")
    public List<String> getFilteredMessages() {
        return listeners.getFilteredMessages();
    }

    @GetMapping("/object")
    public List<ConsumedMessage> getObjectMessages() {
        return listeners.getObjectMessages();
    }

    @GetMapping("/header")
    public List<String> getHeaderMessages() {
        return listeners.getHeaderMessages();
    }
    
    @GetMapping("/info")
    public Map<String, String> getInfo() {
        return Map.of(
            "pattern", "KafkaListener Pattern",
            "description", "Demonstrates consuming messages from Kafka topics using @KafkaListener.",
            "features", "Simple listener, filtering, custom object deserialization, header access.",
            "consumers", "5 different listener methods for various scenarios.",
            "endpoints", "4 REST endpoints to view consumed messages."
        );
    }
}

// Configuration for filtering and custom object deserialization would be needed.
// For example, in a @Configuration class:
/*
@Bean
public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
    ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
    ConsumerFactory<Object, Object> kafkaConsumerFactory) {
    ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
    configurer.configure(factory, kafkaConsumerFactory);
    factory.setRecordFilterStrategy(record -> !record.value().contains("important"));
    return factory;
}

@Bean
public ConcurrentKafkaListenerContainerFactory<String, ConsumedMessage> kafkaListenerContainerFactoryForObjects(
    ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
    ConsumerFactory<Object, Object> kafkaConsumerFactory) {
    ConcurrentKafkaListenerContainerFactory<String, ConsumedMessage> factory = new ConcurrentKafkaListenerContainerFactory<>();
    configurer.configure(factory, kafkaConsumerFactory);
    return factory;
}
*/
