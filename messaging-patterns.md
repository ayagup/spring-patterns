# Spring Messaging Patterns

I'll create a comprehensive Spring Boot application demonstrating all 11 messaging patterns.

## Project Structure

```
spring-messaging-patterns/
├── src/main/java/org/example/
│   ├── MessagingPatternsApplication.java
│   ├── config/
│   ├── model/
│   └── patterns/messaging/
│       ├── messagedrivenpojo/
│       ├── listenercontainer/
│       ├── converter/
│       ├── template/
│       ├── handler/
│       ├── gateway/
│       ├── bridge/
│       ├── channeladapter/
│       ├── inbound/
│       ├── outbound/
│       └── messagestore/
├── pom.xml
└── application.properties
```

## 1. Main Application & Configuration

```java
// src/main/java/org/example/MessagingPatternsApplication.java
package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJms
@EnableIntegration
@IntegrationComponentScan
@EnableScheduling
public class MessagingPatternsApplication {
    public static void main(String[] args) {
        SpringApplication.run(MessagingPatternsApplication.class, args);
    }
}
```

## 2. Domain Models

```java
// src/main/java/org/example/model/Order.java
package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private String orderNumber;
    private String customerId;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private LocalDateTime orderDate;
    
    public enum OrderStatus {
        PENDING, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED
    }
}
```

```java
// src/main/java/org/example/model/Notification.java
package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private String recipient;
    private String subject;
    private String message;
    private NotificationType type;
    private LocalDateTime timestamp;
    
    public enum NotificationType {
        EMAIL, SMS, PUSH
    }
}
```

```java
// src/main/java/org/example/model/Event.java
package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Event implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String eventId;
    private String eventType;
    private String source;
    private LocalDateTime timestamp;
    private Map<String, Object> payload;
}
```

## 3. JMS Configuration

```java
// src/main/java/org/example/config/JmsConfig.java
package org.example.config;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

import jakarta.jms.ConnectionFactory;

/**
 * JMS Configuration for messaging patterns.
 */
@Configuration
public class JmsConfig {
    
    @Bean
    public ConnectionFactory connectionFactory() {
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory();
        factory.setBrokerURL("vm://localhost?broker.persistent=false");
        return factory;
    }
    
    @Bean
    public MessageConverter jacksonJmsMessageConverter() {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        return converter;
    }
    
    @Bean
    public JmsTemplate jmsTemplate(ConnectionFactory connectionFactory) {
        JmsTemplate template = new JmsTemplate(connectionFactory);
        template.setMessageConverter(jacksonJmsMessageConverter());
        return template;
    }
    
    @Bean
    public DefaultJmsListenerContainerFactory jmsListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        
        DefaultJmsListenerContainerFactory factory = 
                new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jacksonJmsMessageConverter());
        factory.setConcurrency("3-10");
        factory.setSessionAcknowledgeMode(1); // AUTO_ACKNOWLEDGE
        return factory;
    }
}
```

## 4. Spring Integration Configuration

```java
// src/main/java/org/example/config/IntegrationConfig.java
package org.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.store.MessageGroupStore;
import org.springframework.integration.store.SimpleMessageStore;
import org.springframework.messaging.MessageChannel;

/**
 * Spring Integration configuration.
 */
@Configuration
public class IntegrationConfig {
    
    @Bean
    public MessageChannel inputChannel() {
        return new DirectChannel();
    }
    
    @Bean
    public MessageChannel outputChannel() {
        return new DirectChannel();
    }
    
    @Bean
    public MessageChannel orderChannel() {
        return new DirectChannel();
    }
    
    @Bean
    public MessageChannel notificationChannel() {
        return new PublishSubscribeChannel();
    }
    
    @Bean
    public MessageChannel eventChannel() {
        return new QueueChannel(100);
    }
    
    @Bean
    public MessageChannel errorChannel() {
        return new DirectChannel();
    }
    
    @Bean
    public MessageGroupStore messageStore() {
        return new SimpleMessageStore();
    }
}
```

## 5. Pattern 1: Message-Driven POJO Pattern

```java
// src/main/java/org/example/patterns/messaging/messagedrivenpojo/OrderProcessor.java
package org.example.patterns.messaging.messagedrivenpojo;

import lombok.extern.slf4j.Slf4j;
import org.example.model.Order;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

/**
 * Message-Driven POJO Pattern.
 * Plain POJO that processes messages without implementing JMS interfaces.
 */
@Slf4j
@Component
public class OrderProcessor {
    
    /**
     * Message-driven POJO - no JMS dependencies.
     */
    @JmsListener(destination = "order.queue")
    public void processOrder(Order order) {
        log.info("Message-Driven POJO: Processing order {}", order.getOrderNumber());
        
        // Business logic - no messaging code
        validateOrder(order);
        processPayment(order);
        updateInventory(order);
        
        log.info("Message-Driven POJO: Order {} processed successfully", 
                order.getOrderNumber());
    }
    
    private void validateOrder(Order order) {
        log.debug("Validating order: {}", order.getOrderNumber());
        // Validation logic
    }
    
    private void processPayment(Order order) {
        log.debug("Processing payment for order: {}", order.getOrderNumber());
        // Payment processing
    }
    
    private void updateInventory(Order order) {
        log.debug("Updating inventory for order: {}", order.getOrderNumber());
        // Inventory update
    }
}
```

```java
// src/main/java/org/example/patterns/messaging/messagedrivenpojo/NotificationHandler.java
package org.example.patterns.messaging.messagedrivenpojo;

import lombok.extern.slf4j.Slf4j;
import org.example.model.Notification;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotificationHandler {
    
    @JmsListener(destination = "notification.queue")
    public void handleNotification(Notification notification) {
        log.info("Message-Driven POJO: Handling {} notification to {}", 
                notification.getType(), notification.getRecipient());
        
        switch (notification.getType()) {
            case EMAIL -> sendEmail(notification);
            case SMS -> sendSms(notification);
            case PUSH -> sendPushNotification(notification);
        }
        
        log.info("Message-Driven POJO: Notification sent successfully");
    }
    
    private void sendEmail(Notification notification) {
        log.info("Sending email to: {}", notification.getRecipient());
    }
    
    private void sendSms(Notification notification) {
        log.info("Sending SMS to: {}", notification.getRecipient());
    }
    
    private void sendPushNotification(Notification notification) {
        log.info("Sending push notification to: {}", notification.getRecipient());
    }
}
```

```java
// src/main/java/org/example/patterns/messaging/messagedrivenpojo/MessageDrivenPojoController.java
package org.example.patterns.messaging.messagedrivenpojo;

import lombok.RequiredArgsConstructor;
import org.example.model.Notification;
import org.example.model.Order;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/message-driven-pojo")
@RequiredArgsConstructor
public class MessageDrivenPojoController {
    
    private final JmsTemplate jmsTemplate;
    
    @PostMapping("/order")
    public Map<String, String> sendOrder(@RequestBody Order order) {
        order.setOrderDate(LocalDateTime.now());
        jmsTemplate.convertAndSend("order.queue", order);
        return Map.of("message", "Order sent to message-driven POJO");
    }
    
    @PostMapping("/notification")
    public Map<String, String> sendNotification(@RequestBody Notification notification) {
        notification.setTimestamp(LocalDateTime.now());
        jmsTemplate.convertAndSend("notification.queue", notification);
        return Map.of("message", "Notification sent to message-driven POJO");
    }
}
```

## 6. Pattern 2: Message Listener Container Pattern

```java
// src/main/java/org/example/patterns/messaging/listenercontainer/CustomMessageListener.java
package org.example.patterns.messaging.listenercontainer;

import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import jakarta.jms.TextMessage;
import lombok.extern.slf4j.Slf4j;

/**
 * Message Listener Container Pattern.
 * Manages lifecycle of message listeners.
 */
@Slf4j
public class CustomMessageListener implements MessageListener {
    
    @Override
    public void onMessage(Message message) {
        try {
            if (message instanceof TextMessage textMessage) {
                String text = textMessage.getText();
                log.info("Message Listener Container: Received message: {}", text);
                
                // Process message
                processMessage(text);
                
            } else {
                log.warn("Message Listener Container: Unsupported message type: {}", 
                        message.getClass().getSimpleName());
            }
        } catch (Exception e) {
            log.error("Message Listener Container: Error processing message", e);
        }
    }
    
    private void processMessage(String message) {
        log.info("Processing message in listener container: {}", message);
        // Business logic
    }
}
```

```java
// src/main/java/org/example/patterns/messaging/listenercontainer/ListenerContainerConfig.java
package org.example.patterns.messaging.listenercontainer;

import jakarta.jms.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

/**
 * Configuration for message listener containers.
 */
@Configuration
public class ListenerContainerConfig {
    
    @Bean
    public DefaultMessageListenerContainer messageListenerContainer(
            ConnectionFactory connectionFactory) {
        
        DefaultMessageListenerContainer container = new DefaultMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setDestinationName("listener.container.queue");
        container.setMessageListener(new CustomMessageListener());
        container.setConcurrentConsumers(3);
        container.setMaxConcurrentConsumers(10);
        container.setSessionAcknowledgeMode(1); // AUTO_ACKNOWLEDGE
        container.setSessionTransacted(false);
        
        return container;
    }
}
```

```java
// src/main/java/org/example/patterns/messaging/listenercontainer/ListenerContainerController.java
package org.example.patterns.messaging.listenercontainer;

import lombok.RequiredArgsConstructor;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/listener-container")
@RequiredArgsConstructor
public class ListenerContainerController {
    
    private final JmsTemplate jmsTemplate;
    
    @PostMapping("/send")
    public Map<String, String> sendMessage(@RequestBody Map<String, String> payload) {
        String message = payload.get("message");
        jmsTemplate.convertAndSend("listener.container.queue", message);
        return Map.of("message", "Sent to listener container");
    }
}
```

## 7. Pattern 3: Message Converter Pattern

```java
// src/main/java/org/example/patterns/messaging/converter/CustomMessageConverter.java
package org.example.patterns.messaging.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;
import lombok.extern.slf4j.Slf4j;
import org.example.model.Order;
import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.stereotype.Component;

/**
 * Message Converter Pattern.
 * Converts between domain objects and JMS messages.
 */
@Slf4j
@Component
public class CustomMessageConverter implements MessageConverter {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public Message toMessage(Object object, Session session) 
            throws JMSException, MessageConversionException {
        
        log.info("Message Converter: Converting object to message");
        
        try {
            String json = objectMapper.writeValueAsString(object);
            TextMessage message = session.createTextMessage(json);
            message.setStringProperty("_type", object.getClass().getName());
            
            log.info("Message Converter: Conversion successful");
            return message;
            
        } catch (Exception e) {
            throw new MessageConversionException("Failed to convert object to message", e);
        }
    }
    
    @Override
    public Object fromMessage(Message message) 
            throws JMSException, MessageConversionException {
        
        log.info("Message Converter: Converting message to object");
        
        if (!(message instanceof TextMessage textMessage)) {
            throw new MessageConversionException("Message must be TextMessage");
        }
        
        try {
            String json = textMessage.getText();
            String type = message.getStringProperty("_type");
            
            if (type == null) {
                throw new MessageConversionException("Message type property not found");
            }
            
            Class<?> targetClass = Class.forName(type);
            Object result = objectMapper.readValue(json, targetClass);
            
            log.info("Message Converter: Converted to {}", targetClass.getSimpleName());
            return result;
            
        } catch (Exception e) {
            throw new MessageConversionException("Failed to convert message to object", e);
        }
    }
}
```

```java
// src/main/java/org/example/patterns/messaging/converter/MessageConverterService.java
package org.example.patterns.messaging.converter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.model.Order;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageConverterService {
    
    private final JmsTemplate jmsTemplate;
    
    public void sendOrder(Order order) {
        log.info("Message Converter: Sending order {}", order.getOrderNumber());
        jmsTemplate.convertAndSend("converter.queue", order);
    }
    
    @JmsListener(destination = "converter.queue")
    public void receiveOrder(Order order) {
        log.info("Message Converter: Received order {} (auto-converted)", 
                order.getOrderNumber());
        // Order is automatically converted from JMS message
    }
}
```

```java
// src/main/java/org/example/patterns/messaging/converter/MessageConverterController.java
package org.example.patterns.messaging.converter;

import lombok.RequiredArgsConstructor;
import org.example.model.Order;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/message-converter")
@RequiredArgsConstructor
public class MessageConverterController {
    
    private final MessageConverterService messageConverterService;
    
    @PostMapping("/order")
    public Map<String, String> sendOrder(@RequestBody Order order) {
        order.setOrderDate(LocalDateTime.now());
        messageConverterService.sendOrder(order);
        return Map.of("message", "Order sent with message converter");
    }
}
```

## 8. Pattern 4: Message Template Pattern

```java
// src/main/java/org/example/patterns/messaging/template/MessageTemplateService.java
package org.example.patterns.messaging.template;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.model.Notification;
import org.example.model.Order;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Service;

import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.Session;

/**
 * Message Template Pattern.
 * Simplifies sending and receiving messages.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageTemplateService {
    
    private final JmsTemplate jmsTemplate;
    
    /**
     * Send using convertAndSend (simple).
     */
    public void sendOrder(Order order, String destination) {
        log.info("Message Template: Sending order to {}", destination);
        jmsTemplate.convertAndSend(destination, order);
    }
    
    /**
     * Send with message post-processor.
     */
    public void sendOrderWithPriority(Order order, String destination, int priority) {
        log.info("Message Template: Sending order with priority {}", priority);
        
        jmsTemplate.convertAndSend(destination, order, message -> {
            message.setJMSPriority(priority);
            message.setStringProperty("orderType", "priority");
            return message;
        });
    }
    
    /**
     * Send using MessageCreator for full control.
     */
    public void sendCustomMessage(String destination, String content) {
        log.info("Message Template: Sending custom message");
        
        jmsTemplate.send(destination, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                Message message = session.createTextMessage(content);
                message.setStringProperty("source", "custom-template");
                message.setLongProperty("timestamp", System.currentTimeMillis());
                return message;
            }
        });
    }
    
    /**
     * Receive message synchronously.
     */
    public Order receiveOrder(String destination) {
        log.info("Message Template: Receiving order from {}", destination);
        Object received = jmsTemplate.receiveAndConvert(destination);
        return (Order) received;
    }
    
    /**
     * Receive with timeout.
     */
    public Order receiveOrderWithTimeout(String destination, long timeout) {
        jmsTemplate.setReceiveTimeout(timeout);
        log.info("Message Template: Receiving with timeout {} ms", timeout);
        Object received = jmsTemplate.receiveAndConvert(destination);
        return received != null ? (Order) received : null;
    }
    
    /**
     * Request-Reply pattern.
     */
    public String sendAndReceive(String requestDestination, String message) {
        log.info("Message Template: Send and receive from {}", requestDestination);
        
        Object reply = jmsTemplate.sendAndReceive(requestDestination, session -> {
            return session.createTextMessage(message);
        });
        
        if (reply instanceof jakarta.jms.TextMessage textMessage) {
            try {
                return textMessage.getText();
            } catch (JMSException e) {
                log.error("Failed to extract reply", e);
                return null;
            }
        }
        return null;
    }
}
```

```java
// src/main/java/org/example/patterns/messaging/template/MessageTemplateController.java
package org.example.patterns.messaging/template;

import lombok.RequiredArgsConstructor;
import org.example.model.Order;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/message-template")
@RequiredArgsConstructor
public class MessageTemplateController {
    
    private final MessageTemplateService messageTemplateService;
    
    @PostMapping("/send")
    public Map<String, String> sendOrder(@RequestBody Order order) {
        order.setOrderDate(LocalDateTime.now());
        messageTemplateService.sendOrder(order, "template.queue");
        return Map.of("message", "Order sent using message template");
    }
    
    @PostMapping("/send-priority")
    public Map<String, String> sendOrderWithPriority(@RequestBody Order order,
                                                     @RequestParam int priority) {
        order.setOrderDate(LocalDateTime.now());
        messageTemplateService.sendOrderWithPriority(order, "template.queue", priority);
        return Map.of("message", "Priority order sent");
    }
    
    @PostMapping("/send-custom")
    public Map<String, String> sendCustomMessage(@RequestBody Map<String, String> payload) {
        messageTemplateService.sendCustomMessage("template.queue", payload.get("message"));
        return Map.of("message", "Custom message sent");
    }
    
    @GetMapping("/receive")
    public Order receiveOrder() {
        return messageTemplateService.receiveOrder("template.queue");
    }
}
```

## 9. Pattern 5: Message Handler Pattern

```java
// src/main/java/org/example/patterns/messaging/handler/OrderMessageHandler.java
package org.example.patterns.messaging.handler;

import lombok.extern.slf4j.Slf4j;
import org.example.model.Order;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Message Handler Pattern.
 * Handles messages from integration channels.
 */
@Slf4j
@Component
public class OrderMessageHandler {
    
    /**
     * Service Activator - message handler.
     */
    @ServiceActivator(inputChannel = "orderChannel")
    public void handleOrder(Message<Order> message) {
        Order order = message.getPayload();
        log.info("Message Handler: Processing order {}", order.getOrderNumber());
        
        // Access message headers
        log.info("Message Handler: Headers = {}", message.getHeaders());
        
        // Process order
        processOrder(order);
    }
    
    /**
     * Handler with @Payload and @Header annotations.
     */
    @ServiceActivator(inputChannel = "notificationChannel")
    public void handleNotification(@Payload String content,
                                   @Header("type") String type,
                                   @Header("priority") String priority) {
        
        log.info("Message Handler: Notification - type={}, priority={}, content={}", 
                type, priority, content);
        
        // Handle based on type
        if ("email".equals(type)) {
            sendEmail(content);
        } else if ("sms".equals(type)) {
            sendSms(content);
        }
    }
    
    /**
     * Handler that transforms message.
     */
    @ServiceActivator(inputChannel = "inputChannel", outputChannel = "outputChannel")
    public String transformMessage(String input) {
        log.info("Message Handler: Transforming message: {}", input);
        String transformed = input.toUpperCase();
        log.info("Message Handler: Transformed to: {}", transformed);
        return transformed;
    }
    
    private void processOrder(Order order) {
        log.info("Processing order: {}", order.getOrderNumber());
        order.setStatus(Order.OrderStatus.PROCESSING);
    }
    
    private void sendEmail(String content) {
        log.info("Sending email: {}", content);
    }
    
    private void sendSms(String content) {
        log.info("Sending SMS: {}", content);
    }
}
```

```java
// src/main/java/org/example/patterns/messaging/handler/MessageHandlerService.java
package org.example.patterns.messaging.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.model.Order;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageHandlerService {
    
    private final MessageChannel orderChannel;
    private final MessageChannel notificationChannel;
    private final MessageChannel inputChannel;
    
    public void sendOrder(Order order) {
        log.info("Sending order to handler: {}", order.getOrderNumber());
        
        org.springframework.messaging.Message<Order> message = 
                MessageBuilder.withPayload(order)
                        .setHeader("source", "api")
                        .setHeader("timestamp", System.currentTimeMillis())
                        .build();
        
        orderChannel.send(message);
    }
    
    public void sendNotification(String content, String type, String priority) {
        log.info("Sending notification to handler");
        
        org.springframework.messaging.Message<String> message = 
                MessageBuilder.withPayload(content)
                        .setHeader("type", type)
                        .setHeader("priority", priority)
                        .build();
        
        notificationChannel.send(message);
    }
    
    public void sendForTransformation(String input) {
        log.info("Sending for transformation: {}", input);
        inputChannel.send(MessageBuilder.withPayload(input).build());
    }
}
```

```java
// src/main/java/org/example/patterns/messaging/handler/MessageHandlerController.java
package org.example.patterns.messaging.handler;

import lombok.RequiredArgsConstructor;
import org.example.model.Order;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/message-handler")
@RequiredArgsConstructor
public class MessageHandlerController {
    
    private final MessageHandlerService messageHandlerService;
    
    @PostMapping("/order")
    public Map<String, String> sendOrder(@RequestBody Order order) {
        order.setOrderDate(LocalDateTime.now());
        messageHandlerService.sendOrder(order);
        return Map.of("message", "Order sent to message handler");
    }
    
    @PostMapping("/notification")
    public Map<String, String> sendNotification(@RequestBody Map<String, String> payload) {
        messageHandlerService.sendNotification(
                payload.get("content"),
                payload.get("type"),
                payload.get("priority")
        );
        return Map.of("message", "Notification sent to handler");
    }
    
    @PostMapping("/transform")
    public Map<String, String> transform(@RequestBody Map<String, String> payload) {
        messageHandlerService.sendForTransformation(payload.get("input"));
        return Map.of("message", "Message sent for transformation");
    }
}
```

## 10. Pattern 6: Message Gateway Pattern

```java
// src/main/java/org/example/patterns/messaging/gateway/OrderGateway.java
package org.example.patterns.messaging.gateway;

import org.example.model.Order;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;

/**
 * Message Gateway Pattern.
 * Provides simple interface hiding messaging infrastructure.
 */
@MessagingGateway
public interface OrderGateway {
    
    /**
     * Send order to channel.
     */
    @Gateway(requestChannel = "orderChannel")
    void submitOrder(Order order);
    
    /**
     * Send and receive (request-reply).
     */
    @Gateway(requestChannel = "inputChannel", replyChannel = "outputChannel")
    String processMessage(String input);
    
    /**
     * Send with timeout.
     */
    @Gateway(requestChannel = "orderChannel", requestTimeout = 5000)
    void submitOrderWithTimeout(Order order);
}
```

```java
// src/main/java/org/example/patterns/messaging/gateway/NotificationGateway.java
package org.example.patterns.messaging.gateway;

import org.example.model.Notification;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;

@MessagingGateway
public interface NotificationGateway {
    
    @Gateway(requestChannel = "notificationChannel")
    void sendNotification(Notification notification);
    
```java
    @Gateway(requestChannel = "notificationChannel")
    void sendNotificationWithPayload(String message);
}
```

```java
// src/main/java/org/example/patterns/messaging/gateway/MessageGatewayService.java
package org.example.patterns.messaging.gateway;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.model.Notification;
import org.example.model.Order;
import org.springframework.stereotype.Service;

/**
 * Service using message gateways.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageGatewayService {
    
    private final OrderGateway orderGateway;
    private final NotificationGateway notificationGateway;
    
    public void processOrder(Order order) {
        log.info("Message Gateway: Processing order via gateway");
        orderGateway.submitOrder(order);
    }
    
    public String processAndGetResponse(String input) {
        log.info("Message Gateway: Sending request-reply message");
        String response = orderGateway.processMessage(input);
        log.info("Message Gateway: Received response: {}", response);
        return response;
    }
    
    public void sendNotification(Notification notification) {
        log.info("Message Gateway: Sending notification via gateway");
        notificationGateway.sendNotification(notification);
    }
    
    public void sendOrderWithTimeout(Order order) {
        log.info("Message Gateway: Sending order with timeout");
        try {
            orderGateway.submitOrderWithTimeout(order);
        } catch (Exception e) {
            log.error("Message Gateway: Timeout occurred", e);
        }
    }
}
```

```java
// src/main/java/org/example/patterns/messaging/gateway/MessageGatewayController.java
package org.example.patterns.messaging.gateway;

import lombok.RequiredArgsConstructor;
import org.example.model.Notification;
import org.example.model.Order;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/message-gateway")
@RequiredArgsConstructor
public class MessageGatewayController {
    
    private final MessageGatewayService messageGatewayService;
    
    @PostMapping("/order")
    public Map<String, String> submitOrder(@RequestBody Order order) {
        order.setOrderDate(LocalDateTime.now());
        messageGatewayService.processOrder(order);
        return Map.of("message", "Order submitted via gateway");
    }
    
    @PostMapping("/process")
    public Map<String, String> processMessage(@RequestBody Map<String, String> payload) {
        String response = messageGatewayService.processAndGetResponse(payload.get("input"));
        return Map.of("response", response);
    }
    
    @PostMapping("/notification")
    public Map<String, String> sendNotification(@RequestBody Notification notification) {
        notification.setTimestamp(LocalDateTime.now());
        messageGatewayService.sendNotification(notification);
        return Map.of("message", "Notification sent via gateway");
    }
}
```

## 11. Pattern 7: Message Bridge Pattern

```java
// src/main/java/org/example/patterns/messaging/bridge/MessageBridgeConfig.java
package org.example.patterns.messaging.bridge;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.bridge.BridgeHandler;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.handler.LoggingHandler;
import org.springframework.messaging.MessageChannel;

/**
 * Message Bridge Pattern.
 * Connects two messaging systems or channels.
 */
@Configuration
public class MessageBridgeConfig {
    
    @Bean
    public MessageChannel jmsInputChannel() {
        return new DirectChannel();
    }
    
    @Bean
    public MessageChannel integrationOutputChannel() {
        return new DirectChannel();
    }
    
    /**
     * Bridge between JMS and Spring Integration channels.
     */
    @Bean
    public BridgeHandler messageBridge() {
        BridgeHandler bridge = new BridgeHandler();
        bridge.setOutputChannel(integrationOutputChannel());
        return bridge;
    }
    
    /**
     * Logging handler for bridge output.
     */
    @Bean
    public LoggingHandler bridgeLogger() {
        LoggingHandler handler = new LoggingHandler(LoggingHandler.Level.INFO);
        handler.setLoggerName("MessageBridge");
        return handler;
    }
}
```

```java
// src/main/java/org/example/patterns/messaging/bridge/JmsToIntegrationBridge.java
package org.example.patterns.messaging.bridge;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Component;

/**
 * Bridges JMS messages to Spring Integration channels.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JmsToIntegrationBridge {
    
    private final MessageChannel integrationOutputChannel;
    
    @JmsListener(destination = "bridge.jms.queue")
    public void bridgeFromJms(String message) {
        log.info("Message Bridge: Received from JMS: {}", message);
        
        // Transform to Integration message
        org.springframework.messaging.Message<String> integrationMessage = 
                MessageBuilder.withPayload(message)
                        .setHeader("source", "jms")
                        .setHeader("bridged", true)
                        .build();
        
        // Send to Integration channel
        integrationOutputChannel.send(integrationMessage);
        log.info("Message Bridge: Bridged to Integration channel");
    }
}
```

```java
// src/main/java/org/example/patterns/messaging/bridge/IntegrationToJmsBridge.java
package org.example.patterns.messaging.bridge;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

/**
 * Bridges Spring Integration messages to JMS.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IntegrationToJmsBridge {
    
    private final JmsTemplate jmsTemplate;
    
    @ServiceActivator(inputChannel = "integrationOutputChannel")
    public void bridgeToJms(String message) {
        log.info("Message Bridge: Received from Integration: {}", message);
        
        // Send to JMS
        jmsTemplate.convertAndSend("bridge.output.queue", message);
        log.info("Message Bridge: Bridged to JMS");
    }
}
```

```java
// src/main/java/org/example/patterns/messaging/bridge/MessageBridgeController.java
package org.example.patterns.messaging.bridge;

import lombok.RequiredArgsConstructor;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/message-bridge")
@RequiredArgsConstructor
public class MessageBridgeController {
    
    private final JmsTemplate jmsTemplate;
    
    @PostMapping("/jms-to-integration")
    public Map<String, String> sendToJms(@RequestBody Map<String, String> payload) {
        String message = payload.get("message");
        jmsTemplate.convertAndSend("bridge.jms.queue", message);
        return Map.of("message", "Sent to JMS (will be bridged to Integration)");
    }
}
```

## 12. Pattern 8: Channel Adapter Pattern

```java
// src/main/java/org/example/patterns/messaging/channeladapter/FileChannelAdapter.java
package org.example.patterns.messaging.channeladapter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.file.FileReadingMessageSource;
import org.springframework.integration.file.FileWritingMessageHandler;
import org.springframework.integration.file.filters.SimplePatternFileListFilter;
import org.springframework.messaging.MessageHandler;

import java.io.File;

/**
 * Channel Adapter Pattern.
 * Adapts external system to messaging channel.
 */
@Slf4j
@Configuration
public class FileChannelAdapter {
    
    /**
     * Inbound channel adapter - reads from file system.
     */
    @Bean
    @InboundChannelAdapter(value = "fileInputChannel", 
                          poller = @Poller(fixedDelay = "5000"))
    public MessageSource<File> fileReadingMessageSource() {
        FileReadingMessageSource source = new FileReadingMessageSource();
        source.setDirectory(new File("./input"));
        source.setFilter(new SimplePatternFileListFilter("*.txt"));
        log.info("Channel Adapter: File reading adapter configured");
        return source;
    }
    
    /**
     * Outbound channel adapter - writes to file system.
     */
    @Bean
    @ServiceActivator(inputChannel = "fileOutputChannel")
    public MessageHandler fileWritingMessageHandler() {
        FileWritingMessageHandler handler = 
                new FileWritingMessageHandler(new File("./output"));
        handler.setExpectReply(false);
        handler.setAutoCreateDirectory(true);
        log.info("Channel Adapter: File writing adapter configured");
        return handler;
    }
}
```

```java
// src/main/java/org/example/patterns/messaging/channeladapter/JdbcChannelAdapter.java
package org.example.patterns.messaging.channeladapter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.jdbc.JdbcPollingChannelAdapter;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * JDBC Channel Adapter for database polling.
 */
@Slf4j
@Configuration
public class JdbcChannelAdapter {
    
    @Bean
    @InboundChannelAdapter(value = "jdbcInputChannel", 
                          poller = @Poller(fixedDelay = "10000"))
    public MessageSource<?> jdbcMessageSource(DataSource dataSource) {
        JdbcPollingChannelAdapter adapter = new JdbcPollingChannelAdapter(
                dataSource, 
                "SELECT * FROM pending_messages WHERE processed = false LIMIT 10"
        );
        
        adapter.setUpdateSql(
                "UPDATE pending_messages SET processed = true WHERE id = :id"
        );
        
        log.info("Channel Adapter: JDBC polling adapter configured");
        return adapter;
    }
}
```

```java
// src/main/java/org/example/patterns/messaging/channeladapter/ChannelAdapterHandler.java
package org.example.patterns.messaging.channeladapter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.stereotype.Component;

import java.io.File;

@Slf4j
@Component
public class ChannelAdapterHandler {
    
    @ServiceActivator(inputChannel = "fileInputChannel")
    public void handleFile(File file) {
        log.info("Channel Adapter: Received file: {}", file.getName());
        // Process file
    }
    
    @ServiceActivator(inputChannel = "jdbcInputChannel")
    public void handleDatabaseRecord(java.util.Map<String, Object> record) {
        log.info("Channel Adapter: Received database record: {}", record);
        // Process record
    }
}
```

## 13. Pattern 9: Inbound Channel Adapter Pattern

```java
// src/main/java/org/example/patterns/messaging/inbound/InboundHttpAdapter.java
package org.example.patterns.messaging.inbound;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.http.dsl.Http;

/**
 * Inbound Channel Adapter Pattern.
 * Receives messages from external source into the system.
 */
@Slf4j
@Configuration
public class InboundHttpAdapter {
    
    /**
     * HTTP inbound adapter.
     */
    @Bean
    public IntegrationFlow httpInboundFlow() {
        return IntegrationFlow
                .from(Http.inboundGateway("/api/inbound/message")
                        .requestMapping(m -> m.methods(HttpMethod.POST))
                        .requestPayloadType(String.class))
                .handle(message -> {
                    log.info("Inbound Adapter: Received HTTP message: {}", 
                            message.getPayload());
                })
                .get();
    }
}
```

```java
// src/main/java/org/example/patterns/messaging/inbound/InboundPollingAdapter.java
package org.example.patterns.messaging.inbound;

import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.Poller;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Polling inbound adapter.
 */
@Slf4j
@Component
public class InboundPollingAdapter {
    
    private final AtomicInteger counter = new AtomicInteger(0);
    
    /**
     * Polls external source every 10 seconds.
     */
    @InboundChannelAdapter(value = "pollingChannel", 
                          poller = @Poller(fixedDelay = "10000"))
    public String pollExternalSource() {
        int count = counter.incrementAndGet();
        String message = "Polled message #" + count + " at " + LocalDateTime.now();
        log.info("Inbound Adapter: Polling - {}", message);
        return message;
    }
}
```

```java
// src/main/java/org/example/patterns/messaging/inbound/InboundAdapterHandler.java
package org.example.patterns.messaging.inbound;

import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class InboundAdapterHandler {
    
    @ServiceActivator(inputChannel = "pollingChannel")
    public void handlePolledMessage(String message) {
        log.info("Inbound Adapter: Processing polled message: {}", message);
        // Process incoming data
    }
}
```

## 14. Pattern 10: Outbound Channel Adapter Pattern

```java
// src/main/java/org/example/patterns/messaging/outbound/OutboundEmailAdapter.java
package org.example.patterns.messaging.outbound;

import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.stereotype.Component;

/**
 * Outbound Channel Adapter Pattern.
 * Sends messages from system to external destination.
 */
@Slf4j
@Component
public class OutboundEmailAdapter {
    
    /**
     * Outbound adapter - sends emails.
     */
    @ServiceActivator(inputChannel = "emailChannel")
    public void sendEmail(String emailContent) {
        log.info("Outbound Adapter: Sending email");
        
        // Simulate email sending
        simulateEmailSend(emailContent);
        
        log.info("Outbound Adapter: Email sent successfully");
    }
    
    private void simulateEmailSend(String content) {
        log.info("Email content: {}", content);
        // Actual email sending logic would go here
    }
}
```

```java
// src/main/java/org/example/patterns/messaging/outbound/OutboundRestAdapter.java
package org.example.patterns.messaging.outbound;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.http.dsl.Http;

/**
 * Outbound HTTP/REST adapter.
 */
@Slf4j
@Configuration
public class OutboundRestAdapter {
    
    @Bean
    public IntegrationFlow httpOutboundFlow() {
        return IntegrationFlow
                .from("httpOutboundChannel")
                .handle(Http.outboundGateway("http://external-api/endpoint")
                        .httpMethod(HttpMethod.POST)
                        .expectedResponseType(String.class))
                .handle(message -> {
                    log.info("Outbound Adapter: Received response: {}", 
                            message.getPayload());
                })
                .get();
    }
}
```

```java
// src/main/java/org/example/patterns/messaging/outbound/OutboundFileAdapter.java
package org.example.patterns.messaging.outbound;

import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.file.FileWritingMessageHandler;
import org.springframework.messaging.MessageHandler;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * Outbound file adapter.
 */
@Slf4j
@Component
public class OutboundFileAdapter {
    
    @ServiceActivator(inputChannel = "fileWriteChannel")
    public MessageHandler fileWriter() {
        FileWritingMessageHandler handler = 
                new FileWritingMessageHandler(new File("./outbound"));
        handler.setAutoCreateDirectory(true);
        handler.setExpectReply(false);
        
        log.info("Outbound Adapter: File writer configured");
        return handler;
    }
}
```

```java
// src/main/java/org/example/patterns/messaging/outbound/OutboundAdapterService.java
package org.example.patterns.messaging.outbound;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboundAdapterService {
    
    private final MessageChannel emailChannel;
    
    public void sendEmail(String recipient, String subject, String body) {
        String emailContent = String.format(
                "To: %s\nSubject: %s\n\n%s", recipient, subject, body);
        
        log.info("Outbound Adapter Service: Sending email to {}", recipient);
        emailChannel.send(MessageBuilder.withPayload(emailContent).build());
    }
}
```

```java
// src/main/java/org/example/patterns/messaging/outbound/OutboundAdapterController.java
package org.example.patterns.messaging.outbound;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/outbound-adapter")
@RequiredArgsConstructor
public class OutboundAdapterController {
    
    private final OutboundAdapterService outboundAdapterService;
    
    @PostMapping("/email")
    public Map<String, String> sendEmail(@RequestBody Map<String, String> payload) {
        outboundAdapterService.sendEmail(
                payload.get("recipient"),
                payload.get("subject"),
                payload.get("body")
        );
        return Map.of("message", "Email sent via outbound adapter");
    }
}
```

## 15. Pattern 11: Message Store Pattern

```java
// src/main/java/org/example/patterns/messaging/messagestore/CustomMessageStore.java
package org.example.patterns.messaging.messagestore;

import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.store.MessageGroup;
import org.springframework.integration.store.MessageGroupStore;
import org.springframework.integration.store.MessageMetadata;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Message Store Pattern.
 * Stores messages for later retrieval or processing.
 */
@Slf4j
@Component
public class CustomMessageStore implements MessageGroupStore {
    
    private final Map<UUID, Message<?>> messageStore = new ConcurrentHashMap<>();
    private final Map<Object, MessageGroup> groupStore = new ConcurrentHashMap<>();
    
    @Override
    public MessageGroup getMessageGroup(Object groupId) {
        log.info("Message Store: Getting message group: {}", groupId);
        return groupStore.get(groupId);
    }
    
    @Override
    public MessageGroup addMessageToGroup(Object groupId, Message<?> message) {
        log.info("Message Store: Adding message to group: {}", groupId);
        
        UUID messageId = UUID.randomUUID();
        messageStore.put(messageId, message);
        
        MessageGroup group = groupStore.computeIfAbsent(groupId, 
                k -> new SimpleMessageGroup(groupId));
        
        ((SimpleMessageGroup) group).addMessage(message);
        
        return group;
    }
    
    @Override
    public MessageGroup removeMessageFromGroup(Object groupId, Message<?> message) {
        log.info("Message Store: Removing message from group: {}", groupId);
        
        MessageGroup group = groupStore.get(groupId);
        if (group != null) {
            ((SimpleMessageGroup) group).removeMessage(message);
        }
        
        return group;
    }
    
    @Override
    public void removeMessageGroup(Object groupId) {
        log.info("Message Store: Removing message group: {}", groupId);
        groupStore.remove(groupId);
    }
    
    @Override
    public Iterator<MessageGroup> iterator() {
        return groupStore.values().iterator();
    }
    
    @Override
    public int messageGroupSize(Object groupId) {
        MessageGroup group = groupStore.get(groupId);
        return group != null ? group.size() : 0;
    }
    
    @Override
    public int getMessageCountForAllMessageGroups() {
        return groupStore.values().stream()
                .mapToInt(MessageGroup::size)
                .sum();
    }
    
    @Override
    public Collection<Message<?>> getMessagesForGroup(Object groupId) {
        MessageGroup group = groupStore.get(groupId);
        return group != null ? group.getMessages() : Collections.emptyList();
    }
    
    @Override
    public Message<?> pollMessageFromGroup(Object groupId) {
        MessageGroup group = groupStore.get(groupId);
        if (group != null && group.size() > 0) {
            Collection<Message<?>> messages = group.getMessages();
            Message<?> message = messages.iterator().next();
            removeMessageFromGroup(groupId, message);
            return message;
        }
        return null;
    }
    
    @Override
    public void setLastReleasedSequenceNumberForGroup(Object groupId, int sequenceNumber) {
        log.info("Message Store: Setting last released sequence for group {}: {}", 
                groupId, sequenceNumber);
    }
    
    @Override
    public void completeGroup(Object groupId) {
        log.info("Message Store: Completing group: {}", groupId);
    }
    
    @Override
    public Message<?> getOneMessageFromGroup(Object groupId) {
        MessageGroup group = groupStore.get(groupId);
        if (group != null && group.size() > 0) {
            return group.getMessages().iterator().next();
        }
        return null;
    }
    
    @Override
    public Collection<Message<?>> addMessagesToGroup(Object groupId, Message<?>... messages) {
        for (Message<?> message : messages) {
            addMessageToGroup(groupId, message);
        }
        return Arrays.asList(messages);
    }
    
    @Override
    public void registerMessageGroupExpiryCallback(MessageGroupCallback callback) {
        log.info("Message Store: Registering expiry callback");
    }
    
    @Override
    public int expireMessageGroups(long timeout) {
        log.info("Message Store: Expiring message groups older than {} ms", timeout);
        return 0;
    }
    
    // Simple MessageGroup implementation
    private static class SimpleMessageGroup implements MessageGroup {
        private final Object groupId;
        private final List<Message<?>> messages = new ArrayList<>();
        private final long timestamp = System.currentTimeMillis();
        
        public SimpleMessageGroup(Object groupId) {
            this.groupId = groupId;
        }
        
        public void addMessage(Message<?> message) {
            messages.add(message);
        }
        
        public void removeMessage(Message<?> message) {
            messages.remove(message);
        }
        
        @Override
        public Object getGroupId() {
            return groupId;
        }
        
        @Override
        public long getTimestamp() {
            return timestamp;
        }
        
        @Override
        public int size() {
            return messages.size();
        }
        
        @Override
        public Collection<Message<?>> getMessages() {
            return new ArrayList<>(messages);
        }
        
        @Override
        public Message<?> getOne() {
            return messages.isEmpty() ? null : messages.get(0);
        }
        
        @Override
        public int getLastReleasedMessageSequenceNumber() {
            return 0;
        }
        
        @Override
        public boolean isComplete() {
            return false;
        }
        
        @Override
        public void complete() {
        }
        
        @Override
        public int getSequenceSize() {
            return messages.size();
        }
        
        @Override
        public MessageMetadata getLastModified() {
            return null;
        }
    }
}
```

```java
// src/main/java/org/example/patterns/messaging/messagestore/MessageStoreService.java
package org.example.patterns.messaging.messagestore;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.store.MessageGroup;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageStoreService {
    
    private final CustomMessageStore messageStore;
    
    public void storeMessage(String groupId, String content) {
        log.info("Storing message in group: {}", groupId);
        
        Message<String> message = MessageBuilder
                .withPayload(content)
                .setHeader("groupId", groupId)
                .setHeader("timestamp", System.currentTimeMillis())
                .build();
        
        messageStore.addMessageToGroup(groupId, message);
    }
    
    public Collection<Message<?>> getMessages(String groupId) {
        log.info("Retrieving messages from group: {}", groupId);
        return messageStore.getMessagesForGroup(groupId);
    }
    
    public Message<?> pollMessage(String groupId) {
        log.info("Polling message from group: {}", groupId);
        return messageStore.pollMessageFromGroup(groupId);
    }
    
    public int getGroupSize(String groupId) {
        return messageStore.messageGroupSize(groupId);
    }
    
    public void removeGroup(String groupId) {
        log.info("Removing message group: {}", groupId);
        messageStore.removeMessageGroup(groupId);
    }
}
```

```java
// src/main/java/org/example/patterns/messaging/messagestore/MessageStoreController.java
package org.example.patterns.messaging.messagestore;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Map;

@RestController
@RequestMapping("/api/message-store")
@RequiredArgsConstructor
public class MessageStoreController {
    
    private final MessageStoreService messageStoreService;
    
    @PostMapping("/store/{groupId}")
    public Map<String, String> storeMessage(@PathVariable String groupId,
                                           @RequestBody Map<String, String> payload) {
        messageStoreService.storeMessage(groupId, payload.get("content"));
        return Map.of("message", "Message stored in group " + groupId);
    }
    
    @GetMapping("/messages/{groupId}")
    public Collection<Message<?>> getMessages(@PathVariable String groupId) {
        return messageStoreService.getMessages(groupId);
    }
    
    @GetMapping("/poll/{groupId}")
    public Message<?> pollMessage(@PathVariable String groupId) {
        return messageStoreService.pollMessage(groupId);
    }
    
    @GetMapping("/size/{groupId}")
    public Map<String, Integer> getGroupSize(@PathVariable String groupId) {
        return Map.of("size", messageStoreService.getGroupSize(groupId));
    }
    
    @DeleteMapping("/group/{groupId}")
    public Map<String, String> removeGroup(@PathVariable String groupId) {
        messageStoreService.removeGroup(groupId);
        return Map.of("message", "Group removed");
    }
}
```

## 16. Maven Configuration (pom.xml)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
        <relativePath/>
    </parent>
    
    <groupId>org.example</groupId>
    <artifactId>spring-messaging-patterns</artifactId>
    <version>1.0-SNAPSHOT</version>
    <name>Spring Messaging Patterns</name>
    <description>Demonstration of messaging patterns in Spring</description>
    
    <properties>
        <java.version>17</java.version>
    </properties>
    
    <dependencies>
        <!-- Spring Boot Web -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        
        <!-- Spring Boot Artemis (JMS) -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-artemis</artifactId>
        </dependency>
        
        <!-- Spring Integration -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-integration</artifactId>
        </dependency>
        
        <!-- Spring Integration JMS -->
        <dependency>
            <groupId>org.springframework.integration</groupId>
            <artifactId>spring-integration-jms</artifactId>
        </dependency>
        
        <!-- Spring Integration HTTP -->
        <dependency>
            <groupId>org.springframework.integration</groupId>
            <artifactId>spring-integration-http</artifactId>
        </dependency>
        
        <!-- Spring Integration File -->
        <dependency>
            <groupId>org.springframework.integration</groupId>
            <artifactId>spring-integration-file</artifactId>
        </dependency>
        
        <!-- Spring Integration JDBC -->
        <dependency>
            <groupId>org.springframework.integration</groupId>
            <artifactId>spring-integration-jdbc</artifactId>
        </dependency>
        
        <!-- H2 Database (for JDBC adapter demo) -->
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>runtime</scope>
        </dependency>
        
        <!-- Jackson for JSON -->
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
        </dependency>
        
        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        
        <!-- Spring Boot Test -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        
        <!-- Spring Integration Test -->
        <dependency>
            <groupId>org.springframework.integration</groupId>
            <artifactId>spring-integration-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

## 17. Application Configuration

```properties
# src/main/resources/application.properties
spring.application.name=spring-messaging-patterns

# Server Configuration
server.port=8080

# Artemis JMS Configuration
spring.artemis.mode=embedded
spring.artemis.embedded.enabled=true
spring.artemis.embedded.persistent=false

# JMS Configuration
spring.jms.listener.concurrency=3
spring.jms.listener.max-concurrency=10
spring.jms.template.default-destination=default.queue

# H2 Database Configuration
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.h2.console.enabled=true

# Spring Integration
spring.integration.endpoint.no-auto-startup=false

# Logging
logging.level.root=INFO
logging.level.org.example=DEBUG
logging.level.org.springframework.jms=DEBUG
logging.level.org.springframework.integration=DEBUG
```

## 18. Database Schema (for JDBC Adapter)

```sql
-- src/main/resources/schema.sql
CREATE TABLE IF NOT EXISTS pending_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    message_content VARCHAR(1000),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processed BOOLEAN DEFAULT FALSE
);

-- Insert sample data
INSERT INTO pending_messages (message_content) VALUES 
    ('Message 1'),
    ('Message 2'),
    ('Message 3');
```

## 19. Test Classes

```java
// src/test/java/org/example/patterns/messaging/messagedrivenpojo/MessageDrivenPojoTest.java
package org.example.patterns.messaging.messagedrivenpojo;

import org.example.model.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jms.core.JmsTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
class MessageDrivenPojoTest {
    
    @Autowired
    private JmsTemplate jmsTemplate;
    
    @Test
    void testMessageDrivenPojo() throws InterruptedException {
        Order order = new Order();
        order.setId(1L);
        order.setOrderNumber("TEST-001");
        order.setCustomerId("CUST-001");
        order.setTotalAmount(new BigDecimal("99.99"));
        order.setStatus(Order.OrderStatus.PENDING);
        order.setOrderDate(LocalDateTime.now());
        
        assertDoesNotThrow(() -> {
            jmsTemplate.convertAndSend("order.queue", order);
        });
        
        // Wait for async processing
        Thread.sleep(2000);
    }
}
```

```java
// src/test/java/org/example/patterns/messaging/gateway/MessageGatewayTest.java
package org.example.patterns.messaging.gateway;

import org.example.model.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MessageGatewayTest {
    
    @Autowired
    private OrderGateway orderGateway;
    
    @Test
    void testOrderGateway() {
        Order order = new Order(1L, "TEST-002", "CUST-002", 
                new BigDecimal("199.99"), Order.OrderStatus.PENDING, 
                LocalDateTime.now());
        
        assertDoesNotThrow(() -> {
            orderGateway.submitOrder(order);
        });
    }
    
    @Test
    void testRequestReplyGateway() {
        String input = "test message";
        String response = orderGateway.processMessage(input);
        
        assertNotNull(response);
        assertEquals(input.toUpperCase(), response);
    }
}
```

```java
// src/test/java/org/example/patterns/messaging/messagestore/MessageStoreTest.java
package org.example.patterns.messaging.messagestore;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.Message;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MessageStoreTest {
    
    @Autowired
    private MessageStoreService messageStoreService;
    
    @Test
    void testMessageStore() {
        String groupId = "test-group";
        
        // Store messages
        messageStoreService.storeMessage(groupId, "Message 1");
        messageStoreService.storeMessage(groupId, "Message 2");
        messageStoreService.storeMessage(groupId, "Message 3");
        
        // Verify size
        assertEquals(3, messageStoreService.getGroupSize(groupId));
        
        // Retrieve messages
        Collection<Message<?>> messages = messageStoreService.getMessages(groupId);
        assertEquals(3, messages.size());
        
        // Poll message
        Message<?> polled = messageStoreService.pollMessage(groupId);
        assertNotNull(polled);
        assertEquals(2, messageStoreService.getGroupSize(groupId));
        
        // Remove group
        messageStoreService.removeGroup(groupId);
        assertEquals(0, messageStoreService.getGroupSize(groupId));
    }
}
```

## 20. README.md

```markdown
# Spring Messaging Patterns

Comprehensive demonstration of 11 essential messaging patterns in Spring Framework.

## Patterns Implemented

### 1. Message-Driven POJO Pattern
**Endpoint:** `/api/message-driven-pojo/order`

Plain Java objects that process messages without JMS dependencies.

**Key Features:**
- No JMS interfaces required
- Uses `@JmsListener` annotation
- Focus on business logic, not infrastructure

**Example:**
```java
@Component
public class OrderProcessor {
    @JmsListener(destination = "order.queue")
    public void processOrder(Order order) {
        // Pure business logic
        validateOrder(order);
        processPayment(order);
    }
}
```

**Advantages:**
- ✅ Testable without messaging infrastructure
- ✅ Clean separation of concerns
- ✅ POJO-based programming model

### 2. Message Listener Container Pattern
**Endpoint:** `/api/listener-container/send`

Manages lifecycle and threading of message listeners.

**Configuration:**
```java
@Bean
public DefaultMessageListenerContainer messageListenerContainer() {
    container.setConnectionFactory(connectionFactory);
    container.setDestinationName("listener.container.queue");
    container.setMessageListener(new CustomMessageListener());
    container.setConcurrentConsumers(3);
    container.setMaxConcurrentConsumers(10);
    return container;
}
```

**Features:**
- Concurrent message processing
- Auto-scaling consumers
- Transaction management
- Error handling

### 3. Message Converter Pattern
**Endpoint:** `/api/message-converter/order`

Converts between domain objects and JMS messages.

**Built-in Converters:**
- `SimpleMessageConverter` - Basic types
- `MappingJackson2MessageConverter` - JSON
- `MarshallingMessageConverter` - XML

**Custom Converter:**
```java
@Component
public class CustomMessageConverter implements MessageConverter {
    public Message toMessage(Object object, Session session) {
        // Convert object to JMS message
    }
    
    public Object fromMessage(Message message) {
        // Convert JMS message to object
    }
}
```

**Use Case:** Automatic serialization/deserialization

### 4. Message Template Pattern
**Endpoint:** `/api/message-template/send`

Simplifies JMS operations with template pattern.

**Operations:**
```java
// Send
jmsTemplate.convertAndSend("queue", object);

// Send with post-processor
jmsTemplate.convertAndSend("queue", object, message -> {
    message.setJMSPriority(9);
    return message;
});

// Receive
Order order = (Order) jmsTemplate.receiveAndConvert("queue");

// Request-Reply
Object reply = jmsTemplate.sendAndReceive("queue", message);
```

**Features:**
- Synchronous/asynchronous sending
- Request-reply pattern
- Message post-processing
- Transaction support

### 5. Message Handler Pattern
**Endpoint:** `/api/message-handler/order`

Handles messages in Spring Integration flows.

**Annotations:**
```java
@ServiceActivator(inputChannel = "orderChannel")
public void handleOrder(Message<Order> message) {
    Order order = message.getPayload();
    // Process order
}

@ServiceActivator(inputChannel = "inputChannel", 
                 outputChannel = "outputChannel")
public String transformMessage(String input) {
    return input.toUpperCase();
}
```

**Handler Types:**
- Service Activator
- Transformer
- Filter
- Router
- Splitter
- Aggregator

### 6. Message Gateway Pattern
**Endpoint:** `/api/message-gateway/order`

Hides messaging infrastructure behind simple interface.

**Interface:**
```java
@MessagingGateway
public interface OrderGateway {
    @Gateway(requestChannel = "orderChannel")
    void submitOrder(Order order);
    
    @Gateway(requestChannel = "inputChannel", 
            replyChannel = "outputChannel")
    String processMessage(String input);
}
```

**Advantages:**
- ✅ Clean API
- ✅ No messaging code in business logic
- ✅ Easy to test
- ✅ Type-safe

### 7. Message Bridge Pattern
**Endpoint:** `/api/message-bridge/jms-to-integration`

Connects different messaging systems.

**Use Cases:**
- JMS ↔ Spring Integration
- RabbitMQ ↔ Kafka
- HTTP ↔ JMS
- File System ↔ Message Queue

**Example:**
```java
@JmsListener(destination = "source.queue")
public void bridgeMessage(String message) {
    // Transform if needed
    integrationChannel.send(
        MessageBuilder.withPayload(message).build()
    );
}
```

### 8. Channel Adapter Pattern
**Endpoint:** N/A (Automatic)

Adapts external systems to messaging channels.

**Types:**
- File Channel Adapter
- JDBC Channel Adapter
- HTTP Channel Adapter
- Email Channel Adapter

**Inbound File Adapter:**
```java
@Bean
@InboundChannelAdapter(value = "fileInputChannel", 
                      poller = @Poller(fixedDelay = "5000"))
public MessageSource<File> fileReader() {
    FileReadingMessageSource source = 
        new FileReadingMessageSource();
    source.setDirectory(new File("./input"));
    return source;
}
```

**Outbound File Adapter:**
```java
@Bean
@ServiceActivator(inputChannel = "fileOutputChannel")
public MessageHandler fileWriter() {
    return new FileWritingMessageHandler(new File("./output"));
}
```

### 9. Inbound Channel Adapter Pattern
**Endpoint:** `/api/inbound/message`

Receives messages from external sources into the system.

**HTTP Inbound:**
```java
@Bean
public IntegrationFlow httpInbound() {
    return IntegrationFlow
        .from(Http.inboundGateway("/api/inbound/message")
            .requestMapping(m -> m.methods(HttpMethod.POST)))
        .handle(message -> processMessage(message))
        .get();
}
```

**Polling Inbound:**
```java
@InboundChannelAdapter(value = "pollingChannel", 
                      poller = @Poller(fixedDelay = "10000"))
public String pollExternalSource() {
    return externalApi.fetchData();
}
```

### 10. Outbound Channel Adapter Pattern
**Endpoint:** `/api/outbound-adapter/email`

Sends messages from system to external destinations.

**Email Outbound:**
```java
@ServiceActivator(inputChannel = "emailChannel")
public void sendEmail(String emailContent) {
    emailService.send(emailContent);
}
```

**REST Outbound:**
```java
@Bean
public IntegrationFlow httpOutbound() {
    return IntegrationFlow
        .from("httpOutboundChannel")
        .handle(Http.outboundGateway("http://api/endpoint")
            .httpMethod(HttpMethod.POST))
        .get();
}
```

### 11. Message Store Pattern
**Endpoint:** `/api/message-store/store/{groupId}`

Stores messages for later retrieval or processing.

**Operations:**
```java
// Store message
messageStore.addMessageToGroup(groupId, message);

// Retrieve messages
Collection<Message<?>> messages = 
    messageStore.getMessagesForGroup(groupId);

// Poll message (removes from store)
Message<?> message = messageStore.pollMessageFromGroup(groupId);

// Remove group
messageStore.removeMessageGroup(groupId);
```

**Use Cases:**
- Message aggregation
- Deduplication
- Claim check pattern
- Delayed processing

## Running the Application

### Prerequisites
- Java 17+
- Maven 3.6+

### Build and Run
```bash
mvn clean install
mvn spring-boot:run
```

### Access Application
Base URL: http://localhost:8080

## Testing Patterns

### Message-Driven POJO
```bash
curl -X POST http://localhost:8080/api/message-driven-pojo/order \
  -H "Content-Type: application/json" \
  -d '{
    "id": 1,
    "orderNumber": "ORD-001",
    "customerId": "CUST-001",
    "totalAmount": 99.99,
    "status": "PENDING"
  }'
```

### Message Template
```bash
# Send order
curl -X POST http://localhost:8080/api/message-template/send \
  -H "Content-Type: application/json" \
  -d '{
    "id": 1,
    "orderNumber": "ORD-002",
    "customerId": "CUST-002",
    "totalAmount": 199.99,
    "status": "PENDING"
  }'

# Send with priority
curl -X POST "http://localhost:8080/api/message-template/send-priority?priority=9" \
  -H "Content-Type: application/json" \
  -d '{...}'
```

### Message Gateway
```bash
# Submit order
curl -X POST http://localhost:8080/api/message-gateway/order \
  -H "Content-Type: application/json" \
  -d '{...}'

# Request-reply
curl -X POST http://localhost:8080/api/message-gateway/process \
  -H "Content-Type: application/json" \
  -d '{"input": "hello world"}'
```

### Message Store
```bash
# Store message
curl -X POST http://localhost:8080/api/message-store/store/group1 \
  -H "Content-Type: application/json" \
  -d '{"content": "Test message"}'

# Get messages
curl http://localhost:8080/api/message-store/messages/group1

# Poll message
curl http://localhost:8080/api/message-store/poll/group1

# Get group size
curl http://localhost:8080/api/message-store/size/group1
```

## Architecture

### Message Flow
```
Producer → Message Template → JMS Queue → Message Listener → POJO Handler
Producer → Gateway → Channel → Handler → Downstream System
```

### Integration Flow
```
HTTP Request → Inbound Adapter → Channel → Handler → 
Outbound Adapter → External System
```

## Configuration Examples

### JMS Configuration
```java
@Bean
public JmsTemplate jmsTemplate(ConnectionFactory connectionFactory) {
    JmsTemplate template = new JmsTemplate(connectionFactory);
    template.setMessageConverter(jacksonJmsMessageConverter());
    return template;
}

@Bean
public DefaultJmsListenerContainerFactory jmsListenerContainerFactory() {
    factory.setConnectionFactory(connectionFactory);
    factory.setMessageConverter(jacksonJmsMessageConverter());
    factory.setConcurrency("3-10");
    return factory;
}
```

### Integration Configuration
```java
@Bean
public IntegrationFlow orderFlow() {
    return IntegrationFlow
        .from("orderChannel")
        .transform(message -> transform(message))
        .route(message -> route(message))
        .handle(message -> handle(message))
        .get();
}
```

## Best Practices

### 1. Message-Driven POJOs
- ✅ Keep business logic separate from messaging
- ✅ Use DTOs for message payloads
- ✅ Handle exceptions properly
- ✅ Log message processing

### 2. Message Converters
- Use appropriate converter for format
- Configure type information
- Handle conversion errors
- Version message formats

### 3. Gateways
- Keep interfaces simple
- Use appropriate channels
- Set timeouts
- Handle failures

### 4. Channel Adapters
- Configure polling intervals carefully
- Handle file/database locks
- Implement error channels
- Monitor adapter health

### 5. Message Stores
- Clean up old messages
- Monitor store size
- Use appropriate grouping
- Implement expiration

## Common Pitfalls

### 1. Message Loss
**Problem:** Messages lost on failure

**Solutions:**
- Use persistent queues
- Enable transactions
- Implement retry logic
- Use dead letter queues

### 2. Duplicate Messages
**Problem:** Same message processed multiple times

**Solutions:**
- Idempotent message handlers
- Message deduplication
- Unique message IDs
- Database constraints

### 3. Message Ordering
**Problem:** Messages processed out of order

**Solutions:**
- Single consumer per queue
- Message groups
- Sequence numbers
- Resequencer pattern

### 4. Memory Issues
**Problem:** Message store grows too large

**Solutions:**
- Implement message expiration
- Regular cleanup
- Size limits
- Monitoring

## Performance Tips

### 1. Concurrency
```java
factory.setConcurrency("5-20"); // min-max consumers
```

### 2. Batch Processing
```java
@JmsListener(destination = "queue")
public void processBatch(List<Order> orders) {
    // Process multiple messages
}
```

### 3. Async Processing
```java
@Async
public void processMessage(Message<?> message) {
    // Async processing
}
```

## Monitoring

### JMS Metrics
- Queue depth
- Consumer count
- Message rate
- Error rate

### Integration Metrics
- Channel statistics
- Handler success/failure
- Message throughput
- Latency

## License

MIT License - free to use for learning and projects.
```

This completes the comprehensive implementation of all 11 Messaging Patterns with working code, tests, and thorough documentation!