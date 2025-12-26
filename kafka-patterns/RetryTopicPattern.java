package com.example.kafka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Retry Topic Pattern
 *
 * Demonstrates the use of dedicated retry topics to handle message processing failures
 * without blocking the main topic's consumption. When a message fails, it's sent to a
 * retry topic. A listener on that topic attempts to re-process it after a delay. This
 * can be repeated across several retry topics with increasing backoff periods.
 *
 * Spring for Kafka's @RetryableTopic annotation automates this entire process.
 *
 * Key Features:
 * - Non-blocking retries: The main topic partition is not blocked by a failing message.
 * - Configurable number of retry attempts and backoff policy.
 * - Automatic creation of retry topics and a Dead Letter Topic (DLT).
 * - Fine-grained control over which exceptions trigger a retry.
 *
 * How it works with @RetryableTopic:
 * 1. A message on `source-topic` fails processing.
 * 2. It's sent to `source-topic-retry-0` (with a delay).
 * 3. A listener consumes from `-retry-0`. If it fails again, it's sent to `source-topic-retry-1`.
 * 4. This continues for the configured number of attempts.
 * 5. If all retries fail, the message is sent to `source-topic-dlt`.
 *
 * @author Spring Patterns
 */
@SpringBootApplication
public class RetryTopicPattern {

    public static void main(String[] args) {
        SpringApplication.run(RetryTopicPattern.class, args);
    }
}

@Component
class RetryExampleListener {

    private final List<String> processingLog = new ArrayList<>();
    private final List<String> dltLog = new ArrayList<>();

    /**
     * This listener is configured for 2 retry attempts.
     * Spring will create:
     * - source-topic-retry-1000 (1s delay)
     * - source-topic-retry-2000 (2s delay)
     * - source-topic-dlt (final destination for failures)
     */
    @RetryableTopic(
        attempts = "3", // 1 initial attempt + 2 retries
        backoff = @Backoff(delay = 1000, multiplier = 2.0),
        topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE,
        include = {IllegalStateException.class} // Only retry on this specific exception
    )
    @KafkaListener(topics = "source-topic", groupId = "retry-group")
    public void listen(String message) {
        String log = String.format("Attempting to process message: '%s' at %s", message, Instant.now());
        System.out.println(log);
        processingLog.add(log);

        if (message.contains("retry")) {
            throw new IllegalStateException("Simulating a retryable failure");
        }
        if (message.contains("fatal")) {
            // This exception is not in the 'include' list, so it will go straight to the DLT
            throw new IllegalArgumentException("Simulating a non-retryable failure");
        }

        processingLog.add("Successfully processed: " + message);
    }

    @KafkaListener(topics = "source-topic.dlt", groupId = "retry-dlt-group")
    public void handleDlt(String message) {
        String log = "DLT Received: " + message;
        System.err.println(log);
        dltLog.add(log);
    }

    public List<String> getProcessingLog() {
        return processingLog;
    }

    public List<String> getDltLog() {
        return dltLog;
    }
}

@RestController
@RequestMapping("/api/retry")
class RetryStatusController {

    private final RetryExampleListener listener;

    public RetryStatusController(RetryExampleListener listener) {
        this.listener = listener;
    }

    @GetMapping("/processed")
    public List<String> getProcessingLog() {
        return listener.getProcessingLog();
    }

    @GetMapping("/dlt")
    public List<String> getDltLog() {
        return listener.getDltLog();
    }
    
    @GetMapping("/info")
    public Map<String, String> getInfo() {
        return Map.of(
            "pattern", "Retry Topic Pattern",
            "description", "Uses dedicated topics for non-blocking, delayed message retries.",
            "features", "@RetryableTopic automates creation of retry topics and DLT, with configurable backoff and exception handling.",
            "note-1", "Produce a message with 'retry' to 'source-topic' to see the retry flow.",
            "note-2", "Produce a message with 'fatal' to 'source-topic' to see it go directly to the DLT."
        );
    }
}
