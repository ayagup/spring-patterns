package com.example.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.Map;

/**
 * Topic Creation and Configuration Pattern
 *
 * Demonstrates how to programmatically define and create Kafka topics when the
 * Spring application starts. This is managed by the `KafkaAdmin` client, which
 * automatically picks up all beans of type `NewTopic` and attempts to create them
 * in the Kafka cluster.
 *
 * This approach is preferable to auto-topic-creation on the broker, as it allows
 * for explicit configuration of partitions, replicas, and other topic-level settings.
 *
 * Key Features:
 * - Declarative topic creation using `NewTopic` beans.
 * - `TopicBuilder` provides a fluent API for constructing `NewTopic` objects.
 * - Configuration of partitions, replicas, and topic-specific properties.
 * - `KafkaAdmin` handles the communication with the Kafka cluster to create/update topics.
 *
 * @author Spring Patterns
 */
@SpringBootApplication
public class TopicCreationPattern {

    public static void main(String[] args) {
        SpringApplication.run(TopicCreationPattern.class, args);
    }
}

@Configuration
class KafkaTopicConfig {

    /**
     * The KafkaAdmin bean is automatically configured by Spring Boot.
     * It is responsible for administering topics on the Kafka cluster.
     * You can customize it if needed, but the default is usually sufficient.
     *
     * @param admin the KafkaAdmin instance
     * @return a Topic-initializing bean
     */
    @Bean
    public KafkaAdmin.NewTopics topics(KafkaAdmin admin) {
        // This is just a way to trigger topic creation, the real work is in the NewTopic beans.
        return new KafkaAdmin.NewTopics(
            simpleTopic(),
            partitionedTopic(),
            compactedTopic()
        );
    }

    /**
     * A simple topic with default partitions and replicas.
     */
    @Bean
    public NewTopic simpleTopic() {
        return TopicBuilder.name("simple-topic-created-by-admin")
                .build();
    }

    /**
     * A topic with a specific number of partitions and replicas.
     */
    @Bean
    public NewTopic partitionedTopic() {
        return TopicBuilder.name("partitioned-topic-created-by-admin")
                .partitions(10)
                .replicas(1) // In a real cluster, this would be > 1
                .build();
    }

    /**
     * A topic configured for log compaction. This is useful for topics that
     * store the latest value for each key, like a database changelog.
     */
    @Bean
    public NewTopic compactedTopic() {
        return TopicBuilder.name("compacted-topic-created-by-admin")
                .partitions(5)
                .replicas(1)
                .compact()
                .build();
    }

    /**
     * You can also create topics with other specific configurations.
     */
    @Bean
    public NewTopic configuredTopic() {
        return TopicBuilder.name("configured-topic-created-by-admin")
                .partitions(3)
                .replicas(1)
                .config("retention.ms", "172800000") // 2 days
                .config("min.insync.replicas", "1")
                .build();
    }
}

@RestController
@RequestMapping("/api/topics")
class TopicInfoController {
    @GetMapping("/info")
    public Map<String, String> getInfo() {
        return Map.of(
            "pattern", "Topic Creation and Configuration Pattern",
            "description", "Programmatically creates and configures Kafka topics on application startup.",
            "features", "Uses KafkaAdmin and NewTopic beans with TopicBuilder for declarative setup.",
            "note", "Topics are created when the application starts. Check your Kafka cluster to see them."
        );
    }
}
