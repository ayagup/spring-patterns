package com.example.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Request-Reply Pattern
 *
 * Demonstrates two ways to implement a request-reply (or synchronous RPC-style)
 * communication over Kafka:
 *
 * 1. @SendTo Annotation:
 *    A simple, declarative approach. A @KafkaListener method processes a request
 *    and returns a value. Spring automatically sends the return value to the topic
 *    specified in the @SendTo annotation or to the topic specified in the message's
 *    `REPLY_TOPIC` header.
 *
 * 2. ReplyingKafkaTemplate:
 *    A more powerful, programmatic approach. The client uses a `ReplyingKafkaTemplate`
 *    to send a request and receives a `ListenableFuture` that will be completed with
 *    the reply message. This template manages correlation IDs and temporary reply topics
 *    under the hood.
 *
 * @author Spring Patterns
 */
@SpringBootApplication
public class RequestReplyPattern {

    public static void main(String[] args) {
        SpringApplication.run(RequestReplyPattern.class, args);
    }

    // --- Configuration for ReplyingKafkaTemplate ---

    // 1. The template needs a producer factory.
    // (Assuming a standard ProducerFactory bean is available)

    // 2. It needs a listener container to receive the replies.
    @Bean
    public ConcurrentMessageListenerContainer<String, String> repliesContainer(
            ConcurrentKafkaListenerContainerFactory<String, String> containerFactory) {
        ConcurrentMessageListenerContainer<String, String> container =
                containerFactory.createContainer("replies");
        container.getContainerProperties().setGroupId("replies-group");
        container.setAutoStartup(false);
        return container;
    }

    // 3. Create the ReplyingKafkaTemplate itself.
    @Bean
    public ReplyingKafkaTemplate<String, String, String> replyingKafkaTemplate(
            ProducerFactory<String, String> pf,
            ConcurrentMessageListenerContainer<String, String> repliesContainer) {
        return new ReplyingKafkaTemplate<>(pf, repliesContainer);
    }
}

@Component
class RequestReplyListeners {

    /**
     * Listener for the @SendTo pattern.
     * It consumes from 'request-topic-sendto', processes the message, and the
     * return value is automatically sent to 'reply-topic-sendto'.
     */
    @KafkaListener(topics = "request-topic-sendto", groupId = "sendto-group")
    @SendTo("reply-topic-sendto")
    public String handleRequestWithSendTo(String request) {
        System.out.println("Handling request with @SendTo: " + request);
        return "Reply for '" + request + "'";
    }

    /**
     * Listener for the ReplyingKafkaTemplate pattern.
     * It consumes from 'request-topic-template', and the return value is sent
     * to the reply topic specified in the message headers by the template.
     * The @SendTo is implicit here.
     */
    @KafkaListener(topics = "request-topic-template", groupId = "template-group")
    @SendTo // No topic specified, uses header
    public String handleRequestWithTemplate(String request) {
        System.out.println("Handling request for ReplyingKafkaTemplate: " + request);
        return "RPC Reply for '" + request + "'";
    }
}

@RestController
@RequestMapping("/api/request-reply")
class RequestReplyController {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ReplyingKafkaTemplate<String, String, String> replyingKafkaTemplate;

    /**
     * Sends a request and waits for a reply using the @SendTo pattern.
     * This requires a separate listener on the reply topic on the client side.
     * (For simplicity, we don't show the client-side listener here, but in a real
     * app, you'd need one to get the reply).
     */
    @PostMapping("/sendto-request")
    public String sendRequestWithSendTo(@RequestBody String request) {
        kafkaTemplate.send("request-topic-sendto", request);
        return "Request sent via @SendTo pattern. A listener on 'reply-topic-sendto' will receive the reply.";
    }

    /**
     * Sends a request and synchronously waits for the reply using ReplyingKafkaTemplate.
     */
    @PostMapping("/template-request")
    public String sendRequestWithTemplate(@RequestBody String request)
            throws ExecutionException, InterruptedException {
        
        RequestReplyFuture<String, String, String> future = replyingKafkaTemplate.sendAndReceive(
            new org.apache.kafka.clients.producer.ProducerRecord<>("request-topic-template", request)
        );

        // Wait for the reply
        org.apache.kafka.clients.consumer.ConsumerRecord<String, String> reply = future.get();

        return "Received reply: " + reply.value();
    }
    
    @GetMapping("/info")
    public Map<String, String> getInfo() {
        return Map.of(
            "pattern", "Request-Reply Pattern",
            "description", "Demonstrates synchronous request/reply over Kafka using @SendTo and ReplyingKafkaTemplate.",
            "features-sendto", "@KafkaListener with @SendTo for simple request-reply.",
            "features-template", "ReplyingKafkaTemplate for programmatic request-reply with futures.",
            "note", "The ReplyingKafkaTemplate provides a true synchronous-style client experience."
        );
    }
}
