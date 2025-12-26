package com.example.kafka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Dead Letter Topic (DLT) Pattern
 *
 * Demonstrates how to handle messages that consistently fail processing. Instead of
 * blocking the consumer or dropping the message, it's moved to a separate Dead Letter Topic (DLT).
 * This pattern is often used in conjunction with a retry mechanism.
 *
 * Spring for Kafka provides first-class support for this with the @RetryableTopic annotation.
 *
 * Key Features:
 * - Automatic retries with configurable backoff (delay).
 * - After a configured number of attempts, the failing message is sent to a DLT.
 * - The DLT message is enriched with headers containing exception information.
 * - A separate listener can consume from the DLT for monitoring or manual intervention.
 *
 * Use Cases:
 * - Preventing a single "poison pill" message from halting all consumption on a partition.
 * - Isolating failing messages for later analysis without losing them.
 * - Building resilient data processing pipelines.
 *
 * @author Spring Patterns
 */
@SpringBootApplication
public class DeadLetterTopicPattern {

    public static void main(String[] args) {
        SpringApplication.run(DeadLetterTopicPattern.class, args);
    }
}

@Component
class DltExampleListeners {

    private final List<String> processedMessages = new ArrayList<>();
    private final List<String> dltMessages = new ArrayList<>();

    /**
     * This listener will attempt to process a message up to 3 times.
     * If it fails every time, the message will be sent to a topic named
     * "main-topic.dlt".
     */
    @RetryableTopic(
        attempts = "3",
        backoff = @Backoff(delay = 1000, multiplier = 2.0),
        topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE
    )
    @KafkaListener(topics = "main-topic", groupId = "dlt-group")
    public void listen(String message) {
        System.out.println("Processing message: " + message);
        if (message.contains("fail")) {
            System.err.println("Simulating processing failure for message: " + message);
            throw new RuntimeException("Failed to process message");
        }
        processedMessages.add(message);
    }

    /**
     * This listener consumes messages from the Dead Letter Topic.
     * It can be used for logging, alerting, or attempting a different
     * kind of recovery logic.
     */
    @KafkaListener(topics = "main-topic.dlt", groupId = "dlt-handler-group")
    public void handleDlt(String message, @Header(KafkaHeaders.EXCEPTION_MESSAGE) String exceptionMessage) {
        String dltInfo = "Received from DLT: '" + message + "' due to: " + exceptionMessage;
        System.out.println(dltInfo);
        dltMessages.add(dltInfo);
    }
    
    /**
     * An alternative way to define the DLT handler is with @DltHandler.
     * This method will be invoked for the DLT of the corresponding @KafkaListener
     * within the same component.
     */
    // @DltHandler
    // public void handleDltWithAnnotation(String message, @Header(KafkaHeaders.EXCEPTION_MESSAGE) String exceptionMessage) {
    //     String dltInfo = "Received from DLT (via @DltHandler): '" + message + "' due to: " + exceptionMessage;
    //     System.out.println(dltInfo);
    //     dltMessages.add(dltInfo);
    // }

    public List<String> getProcessedMessages() {
        return processedMessages;
    }

    public List<String> getDltMessages() {
        return dltMessages;
    }
}

@RestController
@RequestMapping("/api/dlt")
class DltStatusController {

    private final DltExampleListeners listeners;

    public DltStatusController(DltExampleListeners listeners) {
        this.listeners = listeners;
    }

    @GetMapping("/processed")
    public List<String> getProcessed() {
        return listeners.getProcessedMessages();
    }

    @GetMapping("/dlt")
    public List<String> getDlt() {
        return listeners.getDltMessages();
    }
    
    @GetMapping("/info")
    public Map<String, String> getInfo() {
        return Map.of(
            "pattern", "Dead Letter Topic (DLT) Pattern",
            "description", "Handles message processing failures by redirecting them to a DLT after several retries.",
            "features", "@RetryableTopic for automatic retries and DLT forwarding, @DltHandler for consuming from the DLT.",
            "note", "Produce a message containing 'fail' to the 'main-topic' to trigger this flow."
        );
    }
}

// To trigger this, produce messages to 'main-topic'.
// A message like "hello" will be processed successfully.
// A message like "fail please" will be retried and then sent to 'main-topic.dlt'.
