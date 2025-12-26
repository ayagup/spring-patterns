package com.example.kafka;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Produced;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.config.KafkaStreamsConfiguration;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;

import java.util.Arrays;
import java.util.Map;

/**
 * Kafka Streams Pattern
 *
 * Demonstrates how to use Spring for Apache Kafka to build a Kafka Streams application.
 * Kafka Streams is a client library for building applications and microservices where the
 * input and output data are stored in Kafka clusters. It allows for stateful stream
 * processing, including joins, aggregations, and windowing.
 *
 * Key Features:
 * - High-level Streams DSL (Domain-Specific Language).
 * - Low-level Processor API for maximum flexibility.
 * - Stateful processing with local state stores.
 * - Windowing for time-based operations.
 * - Exactly-once processing semantics.
 *
 * Use Cases:
 * - Real-time analytics and dashboards.
 * - Fraud detection.
 * - IoT data processing.
 * - Event-driven systems requiring stateful logic.
 *
 * @author Spring Patterns
 */
@EnableKafkaStreams
@SpringBootApplication
public class KafkaStreamsPattern {

    public static void main(String[] args) {
        SpringApplication.run(KafkaStreamsPattern.class, args);
    }

    private static final String INPUT_TOPIC = "streams-input";
    private static final String OUTPUT_TOPIC = "streams-output";

    @Bean
    public KStream<String, String> kStream(StreamsBuilder streamsBuilder) {
        // Create a KStream from the input topic
        KStream<String, String> stream = streamsBuilder.stream(INPUT_TOPIC);

        // Example 1: Simple transformation (convert to uppercase)
        stream.mapValues(value -> value.toUpperCase())
              .to("uppercase-output-topic");

        // Example 2: Word count (stateful aggregation)
        KTable<String, Long> wordCounts = stream
                // Split each line into words
                .flatMapValues(value -> Arrays.asList(value.toLowerCase().split("\\W+")))
                // Group by word
                .groupBy((key, word) -> word)
                // Count the occurrences of each word
                .count();

        // Write the word counts to the output topic
        wordCounts.toStream()
                  .map((key, value) -> new KeyValue<>(key, value.toString()))
                  .to(OUTPUT_TOPIC, Produced.with(Serdes.String(), Serdes.String()));

        // Example 3: Filtering
        stream.filter((key, value) -> value.length() > 10)
              .to("long-messages-topic");

        return stream;
    }
    
    // Note: To see the results, you would typically use a Kafka consumer to read from
    // 'uppercase-output-topic', 'streams-output', and 'long-messages-topic'.
    // You can also use the Kafka Streams Interactive Queries feature to query the state store.
}

// To use this, you would need to configure the Kafka Streams application ID in application.properties:
// spring.kafka.streams.application-id=my-streams-app

// And you would produce messages to the 'streams-input' topic. For example, using kafka-console-producer:
// > kafka-console-producer.sh --broker-list localhost:9092 --topic streams-input
// > hello world
// > kafka streams example
// > another line of text for the kafka streams world

// Then consume from the output topic:
// > kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic streams-output --from-beginning --property print.key=true
// hello    1
// world    2
// kafka    2
// streams  2
// ...
