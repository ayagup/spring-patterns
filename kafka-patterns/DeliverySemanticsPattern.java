package com.example.kafka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.transaction.KafkaTransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Delivery Semantics Pattern
 *
 * Demonstrates how to configure Kafka producers and consumers for different
 * message delivery guarantees: At-Most-Once, At-Least-Once, and Exactly-Once.
 *
 * 1. At-Most-Once:
 *    - Messages may be lost but are never redelivered.
 *    - Achieved by disabling retries on the producer and committing offsets on the consumer
 *      before processing the message.
 *    - Producer config: `retries=0`
 *    - Consumer config: `enable.auto.commit=true`, `auto.commit.interval.ms` (low value)
 *
 * 2. At-Least-Once (Default):
 *    - Messages are never lost but may be redelivered.
 *    - This is the default behavior for Kafka.
 *    - Producer config: `retries > 0` (default is Integer.MAX_VALUE), `acks=all`
 *    - Consumer config: `enable.auto.commit=false`, and commit offsets after processing.
 *      Spring for Kafka does this by default with `@KafkaListener`.
 *
 * 3. Exactly-Once:
 *    - Messages are delivered exactly once and are never lost or redelivered.
 *    - Requires transactional support in both producer and consumer.
 *    - Producer config: `enable.idempotence=true`, `transactional.id=my-tx-id`
 *    - Consumer config: `isolation.level=read_committed`
 *    - In Spring, this is often managed using a KafkaTransactionManager.
 *
 * @author Spring Patterns
 */
@SpringBootApplication
public class DeliverySemanticsPattern {

    public static void main(String[] args) {
        SpringApplication.run(DeliverySemanticsPattern.class, args);
    }

    /**
     * For Exactly-Once semantics, a KafkaTransactionManager is required.
     * The ProducerFactory must be configured to support transactions.
     */
    @Bean
    public KafkaTransactionManager kafkaTransactionManager(ProducerFactory<String, String> producerFactory) {
        return new KafkaTransactionManager<>(producerFactory);
    }
}

@Component
class ExactlyOnceProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public ExactlyOnceProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * This method demonstrates a transactional send. If an exception occurs
     * after the first send but before the method completes, the transaction
     * will be rolled back, and the first message will not be committed.
     */
    @Transactional
    public void sendMessagesInTransaction(String topic1, String message1, String topic2, String message2) {
        kafkaTemplate.send(topic1, message1);
        // If something goes wrong here, the first message send will be rolled back.
        if (message2.contains("fail")) {
            throw new RuntimeException("Simulating failure in transaction");
        }
        kafkaTemplate.send(topic2, message2);
    }
}

@Component
class SemanticsListeners {

    /**
     * At-Least-Once Listener (Default Behavior)
     * Spring for Kafka commits the offset after the method successfully executes.
     * If an exception is thrown, the offset is not committed, and the message
     * will be redelivered (based on retry configuration).
     */
    @KafkaListener(topics = "at-least-once-topic", groupId = "at-least-once-group")
    public void listenAtLeastOnce(String message) {
        System.out.println("At-Least-Once listener received: " + message);
        if (message.contains("fail")) {
            throw new RuntimeException("Simulating processing failure for redelivery");
        }
    }

    /**
     * Exactly-Once Listener
     * This listener operates within a transaction. It will only see messages
     * from producers that have been committed.
     * The `isolation.level=read_committed` property is key here.
     */
    @KafkaListener(topics = "exactly-once-topic", groupId = "exactly-once-group",
                   containerFactory = "kafkaListenerContainerFactory") // Assumes factory is configured for transactions
    public void listenExactlyOnce(String message) {
        System.out.println("Exactly-Once listener received: " + message);
    }
}

// --- Configuration via application.properties ---

/*
# ===================================================================
# Kafka Delivery Semantics Configuration
# ===================================================================

# --- AT-MOST-ONCE ---
# Producer: Disable retries.
# spring.kafka.producer.retries=0
# Consumer: Auto-commit frequently.
# spring.kafka.consumer.group-id=at-most-once-group
# spring.kafka.consumer.enable-auto-commit=true
# spring.kafka.consumer.auto-commit-interval=100ms

# --- AT-LEAST-ONCE (Default) ---
# Producer: Acks all replicas, retries enabled.
spring.kafka.producer.acks=all
# spring.kafka.producer.retries is > 0 by default.
# Consumer: Disable auto-commit, let the listener container handle it.
spring.kafka.consumer.group-id=at-least-once-group
spring.kafka.consumer.enable-auto-commit=false

# --- EXACTLY-ONCE ---
# Producer: Enable idempotence and transactions.
spring.kafka.producer.acks=all
spring.kafka.producer.enable-idempotence=true
spring.kafka.producer.transaction-id-prefix=tx-
# Consumer: Read only committed messages.
spring.kafka.consumer.group-id=exactly-once-group
spring.kafka.consumer.enable-auto-commit=false
spring.kafka.consumer.isolation-level=read_committed
# Listener container needs to be aware of the transaction manager
spring.kafka.listener.transaction-manager=kafkaTransactionManager

*/

// A simple controller to trigger the producer
@RestController
@RequestMapping("/api/semantics")
class SemanticsController {
    private final ExactlyOnceProducer producer;

    public SemanticsController(ExactlyOnceProducer producer) {
        this.producer = producer;
    }

    @PostMapping("/transactional-send")
    public String sendTransactional(@RequestParam String msg1, @RequestParam String msg2) {
        try {
            producer.sendMessagesInTransaction("exactly-once-topic", msg1, "another-topic", msg2);
            return "Messages sent successfully in a transaction.";
        } catch (Exception e) {
            return "Transaction failed: " + e.getMessage();
        }
    }
    
    @GetMapping("/info")
    public Map<String, String> getInfo() {
        return Map.of(
            "pattern", "Delivery Semantics Pattern",
            "description", "Demonstrates configuration for At-Most-Once, At-Least-Once, and Exactly-Once delivery.",
            "features", "Transactional producer, idempotent producer config, consumer isolation levels.",
            "note", "This pattern is primarily about configuration, shown in comments and application.properties examples."
        );
    }
}
