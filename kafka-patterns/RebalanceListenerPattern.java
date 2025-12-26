package com.example.kafka;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.common.TopicPartition;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.listener.ConsumerAwareRebalanceListener;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Rebalance Listener Pattern
 *
 * Demonstrates how to implement a `ConsumerRebalanceListener` to execute custom logic
 * when Kafka partitions are assigned to or revoked from a consumer. A rebalance is
 * triggered whenever a consumer joins or leaves a group, or when topic metadata changes.
 *
 * This is critical for stateful consumers that need to commit offsets, clean up resources,
 * or load state before processing begins on a newly assigned partition.
 *
 * Key Features:
 * - `onPartitionsAssigned`: Called after partitions have been assigned to the consumer but before consumption begins.
 * - `onPartitionsRevoked`: Called before a rebalance, just before the consumer stops fetching from its current partitions.
 *
 * Use Cases:
 * - Committing offsets manually before a partition is revoked to prevent duplicate processing.
 * - Loading initial state for a partition from an external store (e.g., a database) when it's assigned.
 * - Cleaning up resources or closing connections associated with a partition when it's revoked.
 *
 * @author Spring Patterns
 */
@SpringBootApplication
public class RebalanceListenerPattern {

    public static void main(String[] args) {
        SpringApplication.run(RebalanceListenerPattern.class, args);
    }
}

@Component
class RebalanceLogger implements ConsumerAwareRebalanceListener {

    private final List<String> rebalanceLog = new ArrayList<>();

    @Override
    public void onPartitionsAssigned(Consumer<?, ?> consumer, Collection<TopicPartition> partitions) {
        String log = "Partitions assigned: " + partitions;
        System.out.println(log);
        rebalanceLog.add(log);
        // Example: Seek to the beginning of the assigned partitions
        // consumer.seekToBeginning(partitions);
    }

    @Override
    public void onPartitionsRevoked(Consumer<?, ?> consumer, Collection<TopicPartition> partitions) {
        String log = "Partitions revoked: " + partitions;
        System.out.println(log);
        rebalanceLog.add(log);
        // Example: Commit offsets synchronously before losing the partitions
        // consumer.commitSync();
    }

    public List<String> getRebalanceLog() {
        return rebalanceLog;
    }
}

@Component
class RebalanceAwareListener {

    /**
     * To associate a rebalance listener with a @KafkaListener, you must configure it
     * on the listener container. This is typically done by defining a
     * ConcurrentKafkaListenerContainerFactory bean and setting the rebalance listener on its
     * ContainerProperties.
     *
     * For this example, we assume a bean named 'rebalanceListener' of type
     * ConsumerAwareRebalanceListener exists and is configured on the container factory.
     *
     * See configuration example in comments below.
     */
    @KafkaListener(topics = "rebalance-topic", groupId = "rebalance-group", containerFactory = "rebalanceContainerFactory")
    public void listen(String message) {
        System.out.println("Received message: " + message);
    }
}

/*
// Example configuration in a @Configuration class:

@Configuration
class KafkaRebalanceConfig {

    private final RebalanceLogger rebalanceLogger;

    public KafkaRebalanceConfig(RebalanceLogger rebalanceLogger) {
        this.rebalanceLogger = rebalanceLogger;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> rebalanceContainerFactory(
            ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
            ConsumerFactory<Object, Object> kafkaConsumerFactory) {
        
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        configurer.configure(factory, kafkaConsumerFactory);
        factory.getContainerProperties().setConsumerRebalanceListener(rebalanceLogger);
        return factory;
    }
}
*/

@RestController
@RequestMapping("/api/rebalance")
class RebalanceStatusController {

    private final RebalanceLogger rebalanceLogger;

    public RebalanceStatusController(RebalanceLogger rebalanceLogger) {
        this.rebalanceLogger = rebalanceLogger;
    }

    @GetMapping("/log")
    public List<String> getRebalanceLog() {
        return rebalanceLogger.getRebalanceLog();
    }

    @GetMapping("/info")
    public Map<String, String> getInfo() {
        return Map.of(
            "pattern", "Rebalance Listener Pattern",
            "description", "Hooks into the consumer rebalance process to manage state or resources.",
            "features", "Implements ConsumerRebalanceListener with onPartitionsAssigned and onPartitionsRevoked.",
            "note", "A rebalance is triggered by starting a new instance of this application (with the same group.id) or stopping an existing one."
        );
    }
}
