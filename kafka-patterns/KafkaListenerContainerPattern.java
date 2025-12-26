package com.example.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka Listener Container Pattern
 *
 * Demonstrates programmatic management of Kafka listener containers. While @KafkaListener
 * is sufficient for most static use cases, direct interaction with listener containers
 * is necessary for dynamic scenarios, such as starting/stopping listeners at runtime
 * or creating them on the fly.
 *
 * Key Features:
 * - Programmatic control over listener lifecycle (start, stop, pause, resume).
 * - Dynamic creation of listeners for topics determined at runtime.
 * - Access to container metrics and status.
 *
 * Use Cases:
 * - Pausing consumption during maintenance or high load periods.
 * - Starting listeners for new tenants in a multi-tenant application.
 * - Building administrative dashboards to manage Kafka consumers.
 *
 * @author Spring Patterns
 */
@SpringBootApplication
public class KafkaListenerContainerPattern {

    public static void main(String[] args) {
        SpringApplication.run(KafkaListenerContainerPattern.class, args);
    }

    // We need a consumer factory for programmatic container creation
    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "dynamic-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        return new DefaultKafkaConsumerFactory<>(props);
    }
}

@RestController
@RequestMapping("/api/kafka-container")
class ListenerContainerController {

    @Autowired
    private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    @Autowired
    private ConsumerFactory<String, String> consumerFactory;
    
    private ConcurrentMessageListenerContainer<String, String> dynamicContainer;

    /**
     * Stops a listener container identified by its ID.
     * The ID is typically the one assigned in the @KafkaListener annotation.
     */
    @PostMapping("/stop/{listenerId}")
    public String stopListener(@PathVariable String listenerId) {
        ConcurrentMessageListenerContainer<?, ?> container = (ConcurrentMessageListenerContainer<?, ?>) kafkaListenerEndpointRegistry.getListenerContainer(listenerId);
        if (container != null) {
            container.stop();
            return "Listener '" + listenerId + "' stopped.";
        }
        return "Listener '" + listenerId + "' not found.";
    }

    /**
     * Starts a listener container that was previously stopped.
     */
    @PostMapping("/start/{listenerId}")
    public String startListener(@PathVariable String listenerId) {
        ConcurrentMessageListenerContainer<?, ?> container = (ConcurrentMessageListenerListenerContainer<?, ?>) kafkaListenerEndpointRegistry.getListenerContainer(listenerId);
        if (container != null) {
            container.start();
            return "Listener '" + listenerId + "' started.";
        }
        return "Listener '" + listenerId + "' not found.";
    }

    /**
     * Creates and starts a listener container for a topic specified at runtime.
     */
    @PostMapping("/create")
    public String createDynamicListener(@RequestParam String topic) {
        if (dynamicContainer != null && dynamicContainer.isRunning()) {
            return "A dynamic listener is already running for topic: " + dynamicContainer.getContainerProperties().getTopics()[0];
        }
        
        ContainerProperties containerProps = new ContainerProperties(topic);
        containerProps.setMessageListener((MessageListener<String, String>) record -> {
            System.out.println("Dynamically created listener received: " + record.value());
        });

        dynamicContainer = new ConcurrentMessageListenerContainer<>(consumerFactory, containerProps);
        dynamicContainer.setBeanName("dynamicListener");
        dynamicContainer.start();

        return "Dynamically created listener for topic '" + topic + "' started.";
    }

    /**
     * Stops the dynamically created listener.
     */
    @PostMapping("/stop-dynamic")
    public String stopDynamicListener() {
        if (dynamicContainer != null && dynamicContainer.isRunning()) {
            String topic = dynamicContainer.getContainerProperties().getTopics()[0];
            dynamicContainer.stop();
            dynamicContainer = null;
            return "Dynamic listener for topic '" + topic + "' stopped.";
        }
        return "No dynamic listener is currently running.";
    }
    
    @GetMapping("/status/{listenerId}")
    public Map<String, Object> getListenerStatus(@PathVariable String listenerId) {
        ConcurrentMessageListenerContainer<?, ?> container = (ConcurrentMessageListenerContainer<?, ?>) kafkaListenerEndpointRegistry.getListenerContainer(listenerId);
        if (container != null) {
            Map<String, Object> status = new HashMap<>();
            status.put("listenerId", listenerId);
            status.put("running", container.isRunning());
            status.put("paused", container.isContainerPaused());
            status.put("groupId", container.getGroupId());
            return status;
        }
        return Map.of("error", "Listener not found");
    }

    @GetMapping("/info")
    public Map<String, String> getInfo() {
        return Map.of(
            "pattern", "Kafka Listener Container Pattern",
            "description", "Programmatically manages Kafka listener containers for dynamic control.",
            "features", "Start/stop listeners, create listeners at runtime, check container status.",
            "endpoints", "5 REST endpoints for listener container management."
        );
    }
}

// A sample component with a statically defined listener to be controlled
@Component
class ControllableListener {
    @KafkaListener(id = "controllableListener", topics = "controllable-topic", autoStartup = "true")
    public void listen(String message) {
        System.out.println("Controllable listener received: " + message);
    }
}
