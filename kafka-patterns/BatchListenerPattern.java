package com.example.kafka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Batch Listener Pattern
 *
 * Demonstrates how to configure a @KafkaListener to consume a batch of messages
 * from Kafka in a single method invocation. This is highly efficient for use cases
 * where messages can be processed in bulk, such as inserting multiple records into a database.
 *
 * To enable batch listening, you must:
 * 1. Set the listener container factory's `batchListener` property to `true`.
 *    In `application.properties`: `spring.kafka.listener.type=batch`
 * 2. The listener method signature must accept a `List` of payloads.
 *
 * Key Features:
 * - High throughput due to reduced method invocation overhead.
 * - Ability to process multiple messages in a single transaction.
 * - Access to headers for the entire batch or for individual messages.
 *
 * @author Spring Patterns
 */
@SpringBootApplication
public class BatchListenerPattern {

    public static void main(String[] args) {
        SpringApplication.run(BatchListenerPattern.class, args);
    }
}

// Configuration in application.properties is needed to enable batch listening:
// spring.kafka.listener.type=batch
// spring.kafka.consumer.max-poll-records=10 # Example: poll up to 10 records at a time

@Component
class BatchMessageListener {

    private final List<String> processedBatches = new ArrayList<>();
    private int totalMessagesProcessed = 0;

    /**
     * This listener receives a List of messages.
     * The size of the list depends on consumer polling configuration like
     * `max.poll.records` and `fetch.max.wait.ms`.
     */
    @KafkaListener(topics = "batch-topic", groupId = "batch-group")
    public void listenInBatch(List<String> messages) {
        String log = "Received a batch of " + messages.size() + " messages.";
        System.out.println(log);
        processedBatches.add(log);
        totalMessagesProcessed += messages.size();

        for (String message : messages) {
            // Process each message in the batch
            System.out.println("  - Processing: " + message);
        }
    }

    /**
     * A batch listener can also access headers, which will be lists corresponding
     * to the messages in the payload list.
     */
    @KafkaListener(topics = "batch-header-topic", groupId = "batch-header-group")
    public void listenInBatchWithHeaders(
            List<String> messages,
            @Header(KafkaHeaders.RECEIVED_PARTITION_ID) List<Integer> partitions,
            @Header(KafkaHeaders.OFFSET) List<Long> offsets) {
        
        String log = "Received batch with headers. Size: " + messages.size();
        System.out.println(log);
        processedBatches.add(log);
        totalMessagesProcessed += messages.size();

        for (int i = 0; i < messages.size(); i++) {
            System.out.println(String.format("  - Message: %s, Partition: %d, Offset: %d",
                messages.get(i), partitions.get(i), offsets.get(i)));
        }
    }

    public List<String> getProcessedBatches() {
        return processedBatches;
    }

    public int getTotalMessagesProcessed() {
        return totalMessagesProcessed;
    }
}

@RestController
@RequestMapping("/api/batch")
class BatchStatusController {

    private final BatchMessageListener listener;

    public BatchStatusController(BatchMessageListener listener) {
        this.listener = listener;
    }

    @GetMapping("/logs")
    public List<String> getBatchLogs() {
        return listener.getProcessedBatches();
    }

    @GetMapping("/total")
    public Map<String, Integer> getTotalProcessed() {
        return Map.of("totalMessagesProcessed", listener.getTotalMessagesProcessed());
    }

    @GetMapping("/info")
    public Map<String, String> getInfo() {
        return Map.of(
            "pattern", "Batch Listener Pattern",
            "description", "Consumes messages from Kafka in batches for higher throughput.",
            "features", "Listener method accepts a List of payloads, enabled via `spring.kafka.listener.type=batch`.",
            "note", "Produce multiple messages to 'batch-topic' in quick succession to observe batching."
        );
    }
}
