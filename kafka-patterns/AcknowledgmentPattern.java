package com.example.kafka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Acknowledgment Pattern
 *
 * Demonstrates how to manually control message acknowledgment in a Kafka consumer.
 * By default, Spring for Kafka uses `BATCH` acknowledgment mode, where offsets are
 * committed for a batch of records once they are all processed. However, for certain
 * use cases, manual control is required.
 *
 * To enable manual acknowledgment, you must:
 * 1. Set the listener container's ack mode to `MANUAL` or `MANUAL_IMMEDIATE`.
 *    - `MANUAL_IMMEDIATE`: `ack.acknowledge()` commits the offset for the single record immediately.
 *    - `MANUAL`: `ack.acknowledge()` commits the offset for the record, and all preceding records in the batch, when the batch is finished.
 * 2. Include an `Acknowledgment` object in your `@KafkaListener` method signature.
 *
 * Use Cases:
 * - Acknowledging a message only after it has been successfully written to a database or external system.
 * - Implementing complex, stateful processing where acknowledgment depends on business logic.
 * - Preventing message redelivery during long-running tasks by acknowledging upfront (with risks).
 *
 * @author Spring Patterns
 */
@SpringBootApplication
public class AcknowledgmentPattern {

    public static void main(String[] args) {
        SpringApplication.run(AcknowledgmentPattern.class, args);
    }
}

// Configuration in application.properties is needed to set the ack mode:
// spring.kafka.listener.ack-mode=manual_immediate

@Component
class ManualAckListener {

    private final List<String> processedMessages = new ArrayList<>();
    private final List<String> failedMessages = new ArrayList<>();

    /**
     * This listener demonstrates manual acknowledgment.
     * It receives the Acknowledgment object and must call `acknowledge()` to commit the offset.
     * If `acknowledge()` is not called, the message will be redelivered after the session timeout.
     */
    @KafkaListener(topics = "manual-ack-topic", groupId = "manual-ack-group")
    public void listenWithManualAck(String message, Acknowledgment acknowledgment) {
        System.out.println("Received message for manual acknowledgment: " + message);

        try {
            // Simulate processing that might fail
            if (message.contains("process")) {
                // Business logic goes here
                System.out.println("Processing and acknowledging message: " + message);
                processedMessages.add(message);
                // Acknowledge the message, committing the offset
                acknowledgment.acknowledge();
            } else {
                // If the message is not what we expect, we might choose not to acknowledge it,
                // causing it to be redelivered later. This is a "negative acknowledgment".
                System.err.println("Message will not be acknowledged and will be redelivered: " + message);
                failedMessages.add(message);
                // We simply don't call acknowledgment.acknowledge()
            }
        } catch (Exception e) {
            System.err.println("Exception during processing. Message will not be acknowledged.");
            failedMessages.add(message + " (Exception: " + e.getMessage() + ")");
            // On exception, acknowledgment is also not called.
        }
    }

    public List<String> getProcessedMessages() {
        return processedMessages;
    }

    public List<String> getFailedMessages() {
        return failedMessages;
    }
}

@RestController
@RequestMapping("/api/ack")
class AckStatusController {

    private final ManualAckListener listener;

    public AckStatusController(ManualAckListener listener) {
        this.listener = listener;
    }

    @GetMapping("/processed")
    public List<String> getProcessed() {
        return listener.getProcessedMessages();
    }

    @GetMapping("/failed")
    public List<String> getFailed() {
        return listener.getFailedMessages();
    }

    @GetMapping("/info")
    public Map<String, String> getInfo() {
        return Map.of(
            "pattern", "Acknowledgment Pattern",
            "description", "Demonstrates manual control over message acknowledgment.",
            "features", "Using the Acknowledgment object in a @KafkaListener method.",
            "note-1", "Requires `spring.kafka.listener.ack-mode=manual_immediate` in application.properties.",
            "note-2", "Produce a message with 'process' to 'manual-ack-topic' to see successful acknowledgment.",
            "note-3", "Produce a message without 'process' to see it negatively acknowledged and redelivered."
        );
    }
}
