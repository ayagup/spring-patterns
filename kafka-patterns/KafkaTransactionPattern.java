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
 * Kafka Transaction Pattern
 *
 * Demonstrates how to use Kafka's transactional capabilities to achieve exactly-once
 * semantics in "consume-transform-produce" message flows. A transaction ensures that
 * consuming a message, processing it, and producing a new message are all performed
 * as a single, atomic operation.
 *
 * Key Features:
 * - Atomicity: All operations within the transaction either succeed together or fail together.
 * - Exactly-Once Guarantees: Prevents duplicate message production in the event of a failure and restart.
 * - Requires a `KafkaTransactionManager` and specific producer/consumer configuration.
 *
 * How it works:
 * 1. A consumer reads a message within a transaction.
 * 2. The business logic processes the message and uses a `KafkaTemplate` to produce one or more outbound messages.
 * 3. The transaction manager commits the transaction, which includes the produced messages and the consumer offset for the inbound message.
 * 4. If any step fails, the entire transaction is rolled back. The produced messages are discarded, and the consumer offset is not committed, allowing the original message to be re-processed.
 *
 * @author Spring Patterns
 */
@SpringBootApplication
public class KafkaTransactionPattern {

    public static void main(String[] args) {
        SpringApplication.run(KafkaTransactionPattern.class, args);
    }

    // A KafkaTransactionManager bean is required for transactional support.
    // The ProducerFactory must be configured for transactions (see application.properties).
    @Bean
    public KafkaTransactionManager kafkaTransactionManager(ProducerFactory<String, String> producerFactory) {
        return new KafkaTransactionManager<>(producerFactory);
    }
}

@Component
class TransactionalMessageProcessor {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public TransactionalMessageProcessor(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * This listener participates in a "consume-transform-produce" transaction.
     * The @Transactional annotation ties the listener execution to the KafkaTransactionManager.
     *
     * The flow:
     * 1. Consume from `transactional-input-topic`.
     * 2. Transform the message (e.g., convert to uppercase).
     * 3. Produce to `transactional-output-topic`.
     * 4. Commit the consumer offset and the produced message together.
     */
    @KafkaListener(topics = "transactional-input-topic", groupId = "transactional-group")
    @Transactional
    public void processMessage(String message) {
        System.out.println("Processing message in transaction: " + message);

        // 1. Transform the message
        String transformedMessage = message.toUpperCase();

        // 2. Produce the transformed message to an output topic
        kafkaTemplate.send("transactional-output-topic", transformedMessage);
        System.out.println("Sent transformed message: " + transformedMessage);

        // 3. Simulate a failure
        if (message.contains("fail")) {
            throw new RuntimeException("Simulating failure during transaction. The produced message should be rolled back.");
        }
    }
}

// --- Configuration via application.properties ---

/*
# To enable Kafka transactions, the following properties are essential:

# Producer configuration
# Must enable idempotence and define a transaction ID prefix.
spring.kafka.producer.enable-idempotence=true
spring.kafka.producer.transaction-id-prefix=tx-

# Consumer configuration
# Must read only committed messages to avoid seeing rolled-back data.
spring.kafka.consumer.isolation-level=read_committed
spring.kafka.consumer.enable-auto-commit=false

# Listener container configuration
# The listener must be configured to use the transaction manager.
spring.kafka.listener.transaction-manager=kafkaTransactionManager

*/

@RestController
@RequestMapping("/api/transaction")
class TransactionInfoController {
    @GetMapping("/info")
    public Map<String, String> getInfo() {
        return Map.of(
            "pattern", "Kafka Transaction Pattern",
            "description", "Ensures atomic 'consume-transform-produce' operations using Kafka transactions.",
            "features", "@Transactional annotation with a Kafka listener, automatic rollback on failure.",
            "note-1", "Produce a message to 'transactional-input-topic'. It will be transformed and sent to 'transactional-output-topic'.",
            "note-2", "Produce a message containing 'fail' to see the transaction roll back (the output message will not be sent)."
        );
    }
}
