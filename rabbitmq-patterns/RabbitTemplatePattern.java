package com.example.rabbitmq;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Rabbit Template Pattern
 *
 * Demonstrates the use of `RabbitTemplate` for sending and receiving messages
 * with RabbitMQ. `RabbitTemplate` is the central component in Spring AMQP for
 * message production and synchronous consumption.
 *
 * Key Features:
 * - High-level abstraction for AMQP operations.
 * - `send` methods for fire-and-forget messaging.
 * - `convertAndSend` methods for automatic message conversion (e.g., from an object to JSON).
 * - `receive` and `receiveAndConvert` for synchronous message consumption.
 * - Request-reply messaging support.
 *
 * @author Spring Patterns
 */
@SpringBootApplication
public class RabbitTemplatePattern {

    private static final String QUEUE_NAME = "rabbit-template-queue";

    public static void main(String[] args) {
        SpringApplication.run(RabbitTemplatePattern.class, args);
    }

    @Bean
    public Queue myQueue() {
        return new Queue(QUEUE_NAME, false);
    }
}

class CustomMessage {
    private String text;
    private int priority;

    public CustomMessage() {}
    public CustomMessage(String text, int priority) {
        this.text = text;
        this.priority = priority;
    }

    // Getters and setters
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }

    @Override
    public String toString() {
        return "CustomMessage{" + "text='" + text + '\'' + ", priority=" + priority + '}';
    }
}

@RestController
@RequestMapping("/api/rabbitmq-template")
class MessageController {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private static final String QUEUE_NAME = "rabbit-template-queue";
    private static final String EXCHANGE_NAME = "direct-exchange-example"; // Assuming this exchange exists
    private static final String ROUTING_KEY = "routing.key.example";

    /**
     * Sends a simple string message to the default exchange, which routes to the queue.
     */
    @PostMapping("/send")
    public String sendMessage(@RequestBody String message) {
        rabbitTemplate.convertAndSend(QUEUE_NAME, message);
        return "Sent message: '" + message + "'";
    }

    /**
     * Sends a message to a specific exchange with a routing key.
     */
    @PostMapping("/send-to-exchange")
    public String sendMessageToExchange(@RequestBody String message) {
        rabbitTemplate.convertAndSend(EXCHANGE_NAME, ROUTING_KEY, message);
        return "Sent message to exchange '" + EXCHANGE_NAME + "' with routing key '" + ROUTING_KEY + "'";
    }

    /**
     * Sends a custom object, which will be converted (e.g., to JSON) by the message converter.
     */
    @PostMapping("/send-object")
    public String sendObjectMessage(@RequestBody CustomMessage message) {
        rabbitTemplate.convertAndSend(QUEUE_NAME, message);
        return "Sent object: " + message.toString();
    }

    /**
     * Synchronously receives a message from the queue.
     * Note: This is generally not recommended for high-throughput applications.
     * Asynchronous consumption with @RabbitListener is preferred.
     */
    @GetMapping("/receive")
    public String receiveMessage() {
        String message = (String) rabbitTemplate.receiveAndConvert(QUEUE_NAME);
        if (message != null) {
            return "Received message: '" + message + "'";
        }
        return "No message in the queue.";
    }
    
    /**
     * Sends a message and receives a reply synchronously.
     */
    @PostMapping("/send-and-receive")
    public String sendAndReceive(@RequestBody String message) {
        // The routing key here is the name of the queue to send to
        String reply = (String) rabbitTemplate.convertSendAndReceive(QUEUE_NAME, message);
        return "Sent '" + message + "' and received reply: '" + reply + "'";
    }

    @GetMapping("/info")
    public Map<String, String> getInfo() {
        return Map.of(
            "pattern", "Rabbit Template Pattern",
            "description", "Uses RabbitTemplate for sending and synchronous receiving of messages.",
            "features", "convertAndSend, receiveAndConvert, sendAndReceive, routing to exchanges.",
            "endpoints", "5 endpoints for various messaging scenarios."
        );
    }
}
