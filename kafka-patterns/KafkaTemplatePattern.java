package com.example.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * KafkaTemplate Pattern
 *
 * Demonstrates how to use KafkaTemplate to send messages to Kafka topics.
 * KafkaTemplate is the core class in Spring for Kafka integration, providing a
 * high-level abstraction for producing messages.
 *
 * Key Features:
 * - Simple, thread-safe methods for sending messages.
 * - Synchronous and asynchronous send options.
 * - Automatic serialization of message payloads.
 * - Integration with Spring's transaction management.
 *
 * Use Cases:
 * - Producing messages to Kafka topics from any Spring-managed bean.
 * - Sending messages as part of a larger business transaction.
 * - Implementing request-reply patterns with Kafka.
 *
 * @author Spring Patterns
 */
@SpringBootApplication
public class KafkaTemplatePattern {

    public static void main(String[] args) {
        SpringApplication.run(KafkaTemplatePattern.class, args);
    }
}

// A simple message payload class
class SimpleMessage {
    private String content;
    private String sender;

    public SimpleMessage() {}

    public SimpleMessage(String content, String sender) {
        this.content = content;
        this.sender = sender;
    }

    // Getters and setters
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }

    @Override
    public String toString() {
        return "SimpleMessage{" +
                "content='" + content + '\'' +
                ", sender='" + sender + '\'' +
                '}';
    }
}

@RestController
@RequestMapping("/api/kafka-template")
class MessageProducerController {

    private static final String SIMPLE_TOPIC = "simple-topic";
    private static final String KEYED_TOPIC = "keyed-topic";

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private KafkaTemplate<String, SimpleMessage> simpleMessageKafkaTemplate;

    /**
     * Sends a simple string message asynchronously.
     */
    @PostMapping("/send/async")
    public String sendAsyncMessage(@RequestBody String message) {
        ListenableFuture<SendResult<String, String>> future = kafkaTemplate.send(SIMPLE_TOPIC, message);

        future.addCallback(new ListenableFutureCallback<>() {
            @Override
            public void onSuccess(SendResult<String, String> result) {
                System.out.println("Sent message=[" + message +
                        "] with offset=[" + result.getRecordMetadata().offset() + "]");
            }

            @Override
            public void onFailure(Throwable ex) {
                System.err.println("Unable to send message=[" + message + "] due to : " + ex.getMessage());
            }
        });

        return "Message sent asynchronously. Check server logs for confirmation.";
    }

    /**
     * Sends a message with a key, ensuring it goes to a specific partition.
     */
    @PostMapping("/send/keyed")
    public String sendKeyedMessage(@RequestBody Map<String, String> keyedMessage) {
        String key = keyedMessage.get("key");
        String message = keyedMessage.get("message");
        kafkaTemplate.send(KEYED_TOPIC, key, message);
        return "Keyed message sent. Key: " + key;
    }

    /**
     * Sends a custom object as a message, relying on JSON serialization.
     */
    @PostMapping("/send/object")
    public String sendObjectMessage(@RequestBody SimpleMessage message) {
        simpleMessageKafkaTemplate.send(SIMPLE_TOPIC, message);
        return "Object message sent: " + message.toString();
    }

    /**
     * Sends a message synchronously and waits for the result.
     */
    @PostMapping("/send/sync")
    public String sendSyncMessage(@RequestBody String message) {
        try {
            SendResult<String, String> result = kafkaTemplate.send(SIMPLE_TOPIC, message).get();
            return "Message sent synchronously. Offset: " + result.getRecordMetadata().offset();
        } catch (Exception e) {
            Thread.currentThread().interrupt();
            return "Failed to send message synchronously: " + e.getMessage();
        }
    }
    
    @RequestMapping("/info")
    public Map<String, String> getInfo() {
        return Map.of(
            "pattern", "KafkaTemplate Pattern",
            "description", "Demonstrates sending messages to Kafka topics using KafkaTemplate.",
            "features", "Asynchronous send with callback, synchronous send, keyed messages, custom object serialization.",
            "endpoints", "4 REST endpoints for different message sending scenarios."
        );
    }
}
