package com.example.kafka;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.listener.ErrorHandler;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.listener.SeekToCurrentErrorHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.backoff.FixedBackOff;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Error Handler Pattern
 *
 * Demonstrates how to configure custom error handlers for Kafka listeners. Error handlers
 * provide a way to intercept exceptions that occur during message consumption, such as
 * deserialization errors or exceptions thrown by the listener method itself.
 *
 * Spring for Kafka provides several implementations:
 * - `SeekToCurrentErrorHandler`: (Default since 2.2) Re-seeks the partition to the failed
 *   offset so the message will be redelivered on the next poll. It can be configured with
 *   a backoff policy to add a delay between redelivery attempts. After a configured number
 *   of failures, it can forward the message to a Dead Letter Topic (DLT).
 * - `LoggingErrorHandler`: Simply logs the exception. The offset is committed, and the message is skipped.
 * - Custom `ErrorHandler`: For completely custom logic.
 *
 * This pattern focuses on configuring `SeekToCurrentErrorHandler`.
 *
 * @author Spring Patterns
 */
@SpringBootApplication
public class ErrorHandlerPattern {

    public static void main(String[] args) {
        SpringApplication.run(ErrorHandlerPattern.class, args);
    }

    /**
     * Configures a SeekToCurrentErrorHandler bean. This handler will be applied to
     * listener containers.
     *
     * This configuration specifies:
     * - Retry up to 2 times (3 total attempts).
     * - Wait 1 second between each attempt.
     * - If all attempts fail, the default behavior is to log the error and move to the next message.
     *   (A DeadLetterPublishingRecoverer could be added to send to a DLT).
     */
    @Bean
    public ErrorHandler customErrorHandler() {
        return new SeekToCurrentErrorHandler(new FixedBackOff(1000L, 2L));
    }
}

@Component
class ErrorHandlerListener {

    private final List<String> successfulMessages = new ArrayList<>();
    private int failureCount = 0;

    /**
     * This listener will use the 'customErrorHandler' bean defined in the application context.
     * When a message with "fail" is received, the error handler will intercept the exception
     * and trigger redelivery according to its configuration.
     */
    @KafkaListener(topics = "error-handler-topic", groupId = "error-group", errorHandler = "customErrorHandler")
    public void listen(String message) {
        System.out.println("Attempting to process: " + message);
        if (message.contains("fail")) {
            failureCount++;
            System.err.println("Simulating failure #" + failureCount);
            throw new RuntimeException("Processing failed!");
        }
        successfulMessages.add(message);
        // Reset counter on success
        failureCount = 0;
    }

    public List<String> getSuccessfulMessages() {
        return successfulMessages;
    }

    public int getFailureCount() {
        return failureCount;
    }
}

@RestController
@RequestMapping("/api/error-handler")
class ErrorHandlerStatusController {

    private final ErrorHandlerListener listener;

    public ErrorHandlerStatusController(ErrorHandlerListener listener) {
        this.listener = listener;
    }

    @GetMapping("/successful")
    public List<String> getSuccessfulMessages() {
        return listener.getSuccessfulMessages();
    }

    @GetMapping("/failures")
    public Map<String, Integer> getFailureCount() {
        return Map.of("totalFailuresBeforeSuccess", listener.getFailureCount());
    }

    @GetMapping("/info")
    public Map<String, String> getInfo() {
        return Map.of(
            "pattern", "Error Handler Pattern",
            "description", "Demonstrates using a custom ErrorHandler (SeekToCurrentErrorHandler) for listener exceptions.",
            "features", "Configurable retries with backoff directly within the listener container.",
            "note", "Produce a message with 'fail' to 'error-handler-topic' to see the redelivery attempts in the logs."
        );
    }
}
