### [Continue](https://continue.dev) session transcript
 Exported: 11/5/2025, 2:14:04 PM

#### _User_

> generate a java program in spring for each of the below patterns:
> ```
> **Notification Patterns**
> - Push Notification Pattern
> - SMS Notification Pattern
> - Email Notification Pattern
> - In-App Notification Pattern
> - Real-time Notification Pattern
> - Notification Queue Pattern
> - Notification Template Pattern
> ```

#### _Assistant_

> # Spring Boot Notification Patterns Implementation
> 
> I'll create a comprehensive Spring Boot application implementing all these notification patterns.
> 
> ## 1. Project Structure & Dependencies
> 
> ```xml pom.xml
> <?xml version="1.0" encoding="UTF-8"?>
> <project xmlns="http://maven.apache.org/POM/4.0.0"
>          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
>          xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
>          http://maven.apache.org/xsd/maven-4.0.0.xsd">
>     <modelVersion>4.0.0</modelVersion>
>     
>     <parent>
>         <groupId>org.springframework.boot</groupId>
>         <artifactId>spring-boot-starter-parent</artifactId>
>         <version>3.1.5</version>
>     </parent>
>     
>     <groupId>com.example</groupId>
>     <artifactId>notification-patterns</artifactId>
>     <version>1.0.0</version>
>     
>     <properties>
>         <java.version>17</java.version>
>     </properties>
>     
>     <dependencies>
>         <!-- Spring Boot Starters -->
>         <dependency>
>             <groupId>org.springframework.boot</groupId>
>             <artifactId>spring-boot-starter-web</artifactId>
>         </dependency>
>         <dependency>
>             <groupId>org.springframework.boot</groupId>
>             <artifactId>spring-boot-starter-mail</artifactId>
>         </dependency>
>         <dependency>
>             <groupId>org.springframework.boot</groupId>
>             <artifactId>spring-boot-starter-websocket</artifactId>
>         </dependency>
>         <dependency>
>             <groupId>org.springframework.boot</groupId>
>             <artifactId>spring-boot-starter-data-jpa</artifactId>
>         </dependency>
>         <dependency>
>             <groupId>org.springframework.boot</groupId>
>             <artifactId>spring-boot-starter-amqp</artifactId>
>         </dependency>
>         
>         <!-- Database -->
>         <dependency>
>             <groupId>com.h2database</groupId>
>             <artifactId>h2</artifactId>
>             <scope>runtime</scope>
>         </dependency>
>         
>         <!-- Lombok -->
>         <dependency>
>             <groupId>org.projectlombok</groupId>
>             <artifactId>lombok</artifactId>
>             <optional>true</optional>
>         </dependency>
>         
>         <!-- Firebase Admin SDK for Push Notifications -->
>         <dependency>
>             <groupId>com.google.firebase</groupId>
>             <artifactId>firebase-admin</artifactId>
>             <version>9.2.0</version>
>         </dependency>
>         
>         <!-- Twilio for SMS -->
>         <dependency>
>             <groupId>com.twilio.sdk</groupId>
>             <artifactId>twilio</artifactId>
>             <version>9.14.1</version>
>         </dependency>
>         
>         <!-- Thymeleaf for Email Templates -->
>         <dependency>
>             <groupId>org.springframework.boot</groupId>
>             <artifactId>spring-boot-starter-thymeleaf</artifactId>
>         </dependency>
>     </dependencies>
> </project>
> ```
> 
> ## 2. Application Configuration
> 
> ```yaml application.yml
> spring:
>   application:
>     name: notification-service
>   
>   mail:
>     host: smtp.gmail.com
>     port: 587
>     username: ${EMAIL_USERNAME}
>     password: ${EMAIL_PASSWORD}
>     properties:
>       mail:
>         smtp:
>           auth: true
>           starttls:
>             enable: true
>   
>   rabbitmq:
>     host: localhost
>     port: 5672
>     username: guest
>     password: guest
>   
>   datasource:
>     url: jdbc:h2:mem:notificationdb
>     driver-class-name: org.h2.Driver
>     username: sa
>     password: password
>   
>   jpa:
>     hibernate:
>       ddl-auto: create-drop
>     show-sql: true
> 
> notification:
>   twilio:
>     account-sid: ${TWILIO_ACCOUNT_SID}
>     auth-token: ${TWILIO_AUTH_TOKEN}
>     phone-number: ${TWILIO_PHONE_NUMBER}
>   
>   firebase:
>     credentials-path: ${FIREBASE_CREDENTIALS_PATH}
> 
> server:
>   port: 8080
> ```
> 
> ## 3. Domain Models
> 
> ```java src/main/java/com/example/notification/model/NotificationType.java
> package com.example.notification.model;
> 
> public enum NotificationType {
>     PUSH,
>     SMS,
>     EMAIL,
>     IN_APP,
>     REAL_TIME
> }
> ```
> 
> ```java src/main/java/com/example/notification/model/NotificationStatus.java
> package com.example.notification.model;
> 
> public enum NotificationStatus {
>     PENDING,
>     SENT,
>     DELIVERED,
>     FAILED,
>     READ
> }
> ```
> 
> ```java src/main/java/com/example/notification/model/Notification.java
> package com.example.notification.model;
> 
> import jakarta.persistence.*;
> import lombok.AllArgsConstructor;
> import lombok.Builder;
> import lombok.Data;
> import lombok.NoArgsConstructor;
> 
> import java.time.LocalDateTime;
> 
> @Entity
> @Table(name = "notifications")
> @Data
> @Builder
> @NoArgsConstructor
> @AllArgsConstructor
> public class Notification {
>     
>     @Id
>     @GeneratedValue(strategy = GenerationType.IDENTITY)
>     private Long id;
>     
>     @Column(nullable = false)
>     private String userId;
>     
>     @Column(nullable = false)
>     private String title;
>     
>     @Column(columnDefinition = "TEXT")
>     private String message;
>     
>     @Enumerated(EnumType.STRING)
>     private NotificationType type;
>     
>     @Enumerated(EnumType.STRING)
>     private NotificationStatus status;
>     
>     private String recipient;
>     
>     private LocalDateTime createdAt;
>     
>     private LocalDateTime sentAt;
>     
>     private LocalDateTime readAt;
>     
>     @Column(columnDefinition = "TEXT")
>     private String metadata;
>     
>     @PrePersist
>     protected void onCreate() {
>         createdAt = LocalDateTime.now();
>         if (status == null) {
>             status = NotificationStatus.PENDING;
>         }
>     }
> }
> ```
> 
> ```java src/main/java/com/example/notification/model/NotificationRequest.java
> package com.example.notification.model;
> 
> import lombok.AllArgsConstructor;
> import lombok.Builder;
> import lombok.Data;
> import lombok.NoArgsConstructor;
> 
> import java.util.Map;
> 
> @Data
> @Builder
> @NoArgsConstructor
> @AllArgsConstructor
> public class NotificationRequest {
>     private String userId;
>     private String title;
>     private String message;
>     private NotificationType type;
>     private String recipient;
>     private Map<String, Object> data;
>     private String templateName;
> }
> ```
> 
> ## 4. Repository
> 
> ```java src/main/java/com/example/notification/repository/NotificationRepository.java
> package com.example.notification.repository;
> 
> import com.example.notification.model.Notification;
> import com.example.notification.model.NotificationStatus;
> import org.springframework.data.jpa.repository.JpaRepository;
> import org.springframework.stereotype.Repository;
> 
> import java.util.List;
> 
> @Repository
> public interface NotificationRepository extends JpaRepository<Notification, Long> {
>     
>     List<Notification> findByUserIdOrderByCreatedAtDesc(String userId);
>     
>     List<Notification> findByUserIdAndStatus(String userId, NotificationStatus status);
>     
>     List<Notification> findByStatus(NotificationStatus status);
>     
>     long countByUserIdAndStatus(String userId, NotificationStatus status);
> }
> ```
> 
> ## 5. Pattern 1: Push Notification Pattern
> 
> ```java src/main/java/com/example/notification/service/PushNotificationService.java
> package com.example.notification.service;
> 
> import com.example.notification.model.Notification;
> import com.example.notification.model.NotificationRequest;
> import com.example.notification.model.NotificationStatus;
> import com.example.notification.repository.NotificationRepository;
> import com.google.firebase.messaging.*;
> import lombok.RequiredArgsConstructor;
> import lombok.extern.slf4j.Slf4j;
> import org.springframework.stereotype.Service;
> 
> import java.time.LocalDateTime;
> import java.util.HashMap;
> import java.util.Map;
> 
> @Service
> @RequiredArgsConstructor
> @Slf4j
> public class PushNotificationService {
>     
>     private final NotificationRepository notificationRepository;
>     
>     public Notification sendPushNotification(NotificationRequest request) {
>         Notification notification = createNotification(request);
>         
>         try {
>             // Build FCM message
>             Message message = Message.builder()
>                     .setToken(request.getRecipient()) // FCM device token
>                     .setNotification(com.google.firebase.messaging.Notification.builder()
>                             .setTitle(request.getTitle())
>                             .setBody(request.getMessage())
>                             .build())
>                     .putAllData(buildDataMap(request))
>                     .setAndroidConfig(AndroidConfig.builder()
>                             .setPriority(AndroidConfig.Priority.HIGH)
>                             .build())
>                     .setApnsConfig(ApnsConfig.builder()
>                             .setAps(Aps.builder()
>                                     .setSound("default")
>                                     .setBadge(1)
>                                     .build())
>                             .build())
>                     .build();
>             
>             // Send message
>             String response = FirebaseMessaging.getInstance().send(message);
>             log.info("Successfully sent push notification: {}", response);
>             
>             notification.setStatus(NotificationStatus.SENT);
>             notification.setSentAt(LocalDateTime.now());
>             
>         } catch (FirebaseMessagingException e) {
>             log.error("Failed to send push notification", e);
>             notification.setStatus(NotificationStatus.FAILED);
>         }
>         
>         return notificationRepository.save(notification);
>     }
>     
>     public void sendMulticastPushNotification(NotificationRequest request, 
>                                              java.util.List<String> tokens) {
>         MulticastMessage message = MulticastMessage.builder()
>                 .addAllTokens(tokens)
>                 .setNotification(com.google.firebase.messaging.Notification.builder()
>                         .setTitle(request.getTitle())
>                         .setBody(request.getMessage())
>                         .build())
>                 .putAllData(buildDataMap(request))
>                 .build();
>         
>         try {
>             BatchResponse response = FirebaseMessaging.getInstance().sendMulticast(message);
>             log.info("Successfully sent {} notifications, {} failures", 
>                     response.getSuccessCount(), response.getFailureCount());
>         } catch (FirebaseMessagingException e) {
>             log.error("Failed to send multicast notification", e);
>         }
>     }
>     
>     private Map<String, String> buildDataMap(NotificationRequest request) {
>         Map<String, String> data = new HashMap<>();
>         if (request.getData() != null) {
>             request.getData().forEach((key, value) -> 
>                 data.put(key, String.valueOf(value)));
>         }
>         return data;
>     }
>     
>     private Notification createNotification(NotificationRequest request) {
>         return Notification.builder()
>                 .userId(request.getUserId())
>                 .title(request.getTitle())
>                 .message(request.getMessage())
>                 .type(request.getType())
>                 .recipient(request.getRecipient())
>                 .status(NotificationStatus.PENDING)
>                 .build();
>     }
> }
> ```
> 
> ## 6. Pattern 2: SMS Notification Pattern
> 
> ```java src/main/java/com/example/notification/service/SmsNotificationService.java
> package com.example.notification.service;
> 
> import com.example.notification.model.Notification;
> import com.example.notification.model.NotificationRequest;
> import com.example.notification.model.NotificationStatus;
> import com.example.notification.repository.NotificationRepository;
> import com.twilio.Twilio;
> import com.twilio.rest.api.v2010.account.Message;
> import com.twilio.type.PhoneNumber;
> import lombok.RequiredArgsConstructor;
> import lombok.extern.slf4j.Slf4j;
> import org.springframework.beans.factory.annotation.Value;
> import org.springframework.stereotype.Service;
> 
> import jakarta.annotation.PostConstruct;
> import java.time.LocalDateTime;
> 
> @Service
> @RequiredArgsConstructor
> @Slf4j
> public class SmsNotificationService {
>     
>     private final NotificationRepository notificationRepository;
>     
>     @Value("${notification.twilio.account-sid}")
>     private String accountSid;
>     
>     @Value("${notification.twilio.auth-token}")
>     private String authToken;
>     
>     @Value("${notification.twilio.phone-number}")
>     private String fromPhoneNumber;
>     
>     @PostConstruct
>     public void init() {
>         Twilio.init(accountSid, authToken);
>     }
>     
>     public Notification sendSms(NotificationRequest request) {
>         Notification notification = createNotification(request);
>         
>         try {
>             Message message = Message.creator(
>                     new PhoneNumber(request.getRecipient()),
>                     new PhoneNumber(fromPhoneNumber),
>                     request.getMessage()
>             ).create();
>             
>             log.info("SMS sent successfully. SID: {}", message.getSid());
>             
>             notification.setStatus(NotificationStatus.SENT);
>             notification.setSentAt(LocalDateTime.now());
>             notification.setMetadata(message.getSid());
>             
>         } catch (Exception e) {
>             log.error("Failed to send SMS notification", e);
>             notification.setStatus(NotificationStatus.FAILED);
>             notification.setMetadata(e.getMessage());
>         }
>         
>         return notificationRepository.save(notification);
>     }
>     
>     public Notification sendSmsWithTemplate(NotificationRequest request, 
>                                            String templateId) {
>         // For Twilio templates, you would use their Content API
>         // This is a simplified version
>         return sendSms(request);
>     }
>     
>     private Notification createNotification(NotificationRequest request) {
>         return Notification.builder()
>                 .userId(request.getUserId())
>                 .title(request.getTitle())
>                 .message(request.getMessage())
>                 .type(request.getType())
>                 .recipient(request.getRecipient())
>                 .status(NotificationStatus.PENDING)
>                 .build();
>     }
> }
> ```
> 
> ## 7. Pattern 3: Email Notification Pattern
> 
> ```java src/main/java/com/example/notification/service/EmailNotificationService.java
> package com.example.notification.service;
> 
> import com.example.notification.model.Notification;
> import com.example.notification.model.NotificationRequest;
> import com.example.notification.model.NotificationStatus;
> import com.example.notification.repository.NotificationRepository;
> import jakarta.mail.MessagingException;
> import jakarta.mail.internet.MimeMessage;
> import lombok.RequiredArgsConstructor;
> import lombok.extern.slf4j.Slf4j;
> import org.springframework.mail.SimpleMailMessage;
> import org.springframework.mail.javamail.JavaMailSender;
> import org.springframework.mail.javamail.MimeMessageHelper;
> import org.springframework.stereotype.Service;
> import org.thymeleaf.context.Context;
> import org.thymeleaf.spring6.SpringTemplateEngine;
> 
> import java.time.LocalDateTime;
> import java.util.Map;
> 
> @Service
> @RequiredArgsConstructor
> @Slf4j
> public class EmailNotificationService {
>     
>     private final JavaMailSender mailSender;
>     private final NotificationRepository notificationRepository;
>     private final SpringTemplateEngine templateEngine;
>     
>     public Notification sendSimpleEmail(NotificationRequest request) {
>         Notification notification = createNotification(request);
>         
>         try {
>             SimpleMailMessage message = new SimpleMailMessage();
>             message.setTo(request.getRecipient());
>             message.setSubject(request.getTitle());
>             message.setText(request.getMessage());
>             
>             mailSender.send(message);
>             
>             notification.setStatus(NotificationStatus.SENT);
>             notification.setSentAt(LocalDateTime.now());
>             
>             log.info("Email sent successfully to: {}", request.getRecipient());
>             
>         } catch (Exception e) {
>             log.error("Failed to send email notification", e);
>             notification.setStatus(NotificationStatus.FAILED);
>             notification.setMetadata(e.getMessage());
>         }
>         
>         return notificationRepository.save(notification);
>     }
>     
>     public Notification sendHtmlEmail(NotificationRequest request) {
>         Notification notification = createNotification(request);
>         
>         try {
>             MimeMessage mimeMessage = mailSender.createMimeMessage();
>             MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
>             
>             helper.setTo(request.getRecipient());
>             helper.setSubject(request.getTitle());
>             helper.setText(request.getMessage(), true);
>             
>             mailSender.send(mimeMessage);
>             
>             notification.setStatus(NotificationStatus.SENT);
>             notification.setSentAt(LocalDateTime.now());
>             
>             log.info("HTML email sent successfully to: {}", request.getRecipient());
>             
>         } catch (MessagingException e) {
>             log.error("Failed to send HTML email notification", e);
>             notification.setStatus(NotificationStatus.FAILED);
>             notification.setMetadata(e.getMessage());
>         }
>         
>         return notificationRepository.save(notification);
>     }
>     
>     public Notification sendTemplatedEmail(NotificationRequest request) {
>         Notification notification = createNotification(request);
>         
>         try {
>             Context context = new Context();
>             if (request.getData() != null) {
>                 request.getData().forEach(context::setVariable);
>             }
>             
>             String htmlContent = templateEngine.process(
>                     request.getTemplateName(), context);
>             
>             MimeMessage mimeMessage = mailSender.createMimeMessage();
>             MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
>             
>             helper.setTo(request.getRecipient());
>             helper.setSubject(request.getTitle());
>             helper.setText(htmlContent, true);
>             
>             mailSender.send(mimeMessage);
>             
>             notification.setStatus(NotificationStatus.SENT);
>             notification.setSentAt(LocalDateTime.now());
>             
>             log.info("Templated email sent successfully to: {}", request.getRecipient());
>             
>         } catch (Exception e) {
>             log.error("Failed to send templated email notification", e);
>             notification.setStatus(NotificationStatus.FAILED);
>             notification.setMetadata(e.getMessage());
>         }
>         
>         return notificationRepository.save(notification);
>     }
>     
>     private Notification createNotification(NotificationRequest request) {
>         return Notification.builder()
>                 .userId(request.getUserId())
>                 .title(request.getTitle())
>                 .message(request.getMessage())
>                 .type(request.getType())
>                 .recipient(request.getRecipient())
>                 .status(NotificationStatus.PENDING)
>                 .build();
>     }
> }
> ```
> 
> ## 8. Pattern 4: In-App Notification Pattern
> 
> ```java src/main/java/com/example/notification/service/InAppNotificationService.java
> package com.example.notification.service;
> 
> import com.example.notification.model.Notification;
> import com.example.notification.model.NotificationRequest;
> import com.example.notification.model.NotificationStatus;
> import com.example.notification.repository.NotificationRepository;
> import lombok.RequiredArgsConstructor;
> import lombok.extern.slf4j.Slf4j;
> import org.springframework.stereotype.Service;
> import org.springframework.transaction.annotation.Transactional;
> 
> import java.time.LocalDateTime;
> import java.util.List;
> 
> @Service
> @RequiredArgsConstructor
> @Slf4j
> public class InAppNotificationService {
>     
>     private final NotificationRepository notificationRepository;
>     
>     @Transactional
>     public Notification createInAppNotification(NotificationRequest request) {
>         Notification notification = Notification.builder()
>                 .userId(request.getUserId())
>                 .title(request.getTitle())
>                 .message(request.getMessage())
>                 .type(request.getType())
>                 .recipient(request.getRecipient())
>                 .status(NotificationStatus.SENT)
>                 .sentAt(LocalDateTime.now())
>                 .build();
>         
>         notification = notificationRepository.save(notification);
>         log.info("In-app notification created for user: {}", request.getUserId());
>         
>         return notification;
>     }
>     
>     public List<Notification> getUserNotifications(String userId) {
>         return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
>     }
>     
>     public List<Notification> getUnreadNotifications(String userId) {
>         return notificationRepository.findByUserIdAndStatus(userId, NotificationStatus.SENT);
>     }
>     
>     @Transactional
>     public Notification markAsRead(Long notificationId) {
>         Notification notification = notificationRepository.findById(notificationId)
>                 .orElseThrow(() -> new RuntimeException("Notification not found"));
>         
>         notification.setStatus(NotificationStatus.READ);
>         notification.setReadAt(LocalDateTime.now());
>         
>         return notificationRepository.save(notification);
>     }
>     
>     @Transactional
>     public void markAllAsRead(String userId) {
>         List<Notification> unreadNotifications = getUnreadNotifications(userId);
>         unreadNotifications.forEach(notification -> {
>             notification.setStatus(NotificationStatus.READ);
>             notification.setReadAt(LocalDateTime.now());
>         });
>         notificationRepository.saveAll(unreadNotifications);
>     }
>     
>     public long getUnreadCount(String userId) {
>         return notificationRepository.countByUserIdAndStatus(userId, NotificationStatus.SENT);
>     }
>     
>     @Transactional
>     public void deleteNotification(Long notificationId) {
>         notificationRepository.deleteById(notificationId);
>     }
> }
> ```
> 
> ## 9. Pattern 5: Real-time Notification Pattern (WebSocket)
> 
> ```java src/main/java/com/example/notification/config/WebSocketConfig.java
> package com.example.notification.config;
> 
> import org.springframework.context.annotation.Configuration;
> import org.springframework.messaging.simp.config.MessageBrokerRegistry;
> import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
> import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
> import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
> 
> @Configuration
> @EnableWebSocketMessageBroker
> public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
>     
>     @Override
>     public void configureMessageBroker(MessageBrokerRegistry config) {
>         config.enableSimpleBroker("/topic", "/queue");
>         config.setApplicationDestinationPrefixes("/app");
>         config.setUserDestinationPrefix("/user");
>     }
>     
>     @Override
>     public void registerStompEndpoints(StompEndpointRegistry registry) {
>         registry.addEndpoint("/ws-notifications")
>                 .setAllowedOriginPatterns("*")
>                 .withSockJS();
>     }
> }
> ```
> 
> ```java src/main/java/com/example/notification/service/RealTimeNotificationService.java
> package com.example.notification.service;
> 
> import com.example.notification.model.Notification;
> import com.example.notification.model.NotificationRequest;
> import lombok.RequiredArgsConstructor;
> import lombok.extern.slf4j.Slf4j;
> import org.springframework.messaging.simp.SimpMessagingTemplate;
> import org.springframework.stereotype.Service;
> 
> @Service
> @RequiredArgsConstructor
> @Slf4j
> public class RealTimeNotificationService {
>     
>     private final SimpMessagingTemplate messagingTemplate;
>     private final InAppNotificationService inAppNotificationService;
>     
>     public void sendRealTimeNotification(NotificationRequest request) {
>         // Save to database
>         Notification notification = inAppNotificationService
>                 .createInAppNotification(request);
>         
>         // Send via WebSocket to specific user
>         messagingTemplate.convertAndSendToUser(
>                 request.getUserId(),
>                 "/queue/notifications",
>                 notification
>         );
>         
>         log.info("Real-time notification sent to user: {}", request.getUserId());
>     }
>     
>     public void broadcastNotification(NotificationRequest request) {
>         // Broadcast to all connected users
>         messagingTemplate.convertAndSend(
>                 "/topic/notifications",
>                 request
>         );
>         
>         log.info("Broadcast notification sent");
>     }
>     
>     public void sendToGroup(String group, NotificationRequest request) {
>         messagingTemplate.convertAndSend(
>                 "/topic/notifications/" + group,
>                 request
>         );
>         
>         log.info("Group notification sent to: {}", group);
>     }
> }
> ```
> 
> ## 10. Pattern 6: Notification Queue Pattern
> 
> ```java src/main/java/com/example/notification/config/RabbitMQConfig.java
> package com.example.notification.config;
> 
> import org.springframework.amqp.core.*;
> import org.springframework.amqp.rabbit.connection.ConnectionFactory;
> import org.springframework.amqp.rabbit.core.RabbitTemplate;
> import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
> import org.springframework.amqp.support.converter.MessageConverter;
> import org.springframework.context.annotation.Bean;
> import org.springframework.context.annotation.Configuration;
> 
> @Configuration
> public class RabbitMQConfig {
>     
>     public static final String NOTIFICATION_QUEUE = "notification.queue";
>     public static final String NOTIFICATION_EXCHANGE = "notification.exchange";
>     public static final String NOTIFICATION_ROUTING_KEY = "notification.routing.key";
>     
>     public static final String EMAIL_QUEUE = "notification.email.queue";
>     public static final String SMS_QUEUE = "notification.sms.queue";
>     public static final String PUSH_QUEUE = "notification.push.queue";
>     
>     @Bean
>     public Queue notificationQueue() {
>         return QueueBuilder.durable(NOTIFICATION_QUEUE)
>                 .withArgument("x-dead-letter-exchange", "dlx.exchange")
>                 .build();
>     }
>     
>     @Bean
>     public Queue emailQueue() {
>         return new Queue(EMAIL_QUEUE, true);
>     }
>     
>     @Bean
>     public Queue smsQueue() {
>         return new Queue(SMS_QUEUE, true);
>     }
>     
>     @Bean
>     public Queue pushQueue() {
>         return new Queue(PUSH_QUEUE, true);
>     }
>     
>     @Bean
>     public TopicExchange notificationExchange() {
>         return new TopicExchange(NOTIFICATION_EXCHANGE);
>     }
>     
>     @Bean
>     public Binding notificationBinding() {
>         return BindingBuilder
>                 .bind(notificationQueue())
>                 .to(notificationExchange())
>                 .with(NOTIFICATION_ROUTING_KEY);
>     }
>     
>     @Bean
>     public Binding emailBinding() {
>         return BindingBuilder
>                 .bind(emailQueue())
>                 .to(notificationExchange())
>                 .with("notification.email");
>     }
>     
>     @Bean
>     public Binding smsBinding() {
>         return BindingBuilder
>                 .bind(smsQueue())
>                 .to(notificationExchange())
>                 .with("notification.sms");
>     }
>     
>     @Bean
>     public Binding pushBinding() {
>         return BindingBuilder
>                 .bind(pushQueue())
>                 .to(notificationExchange())
>                 .with("notification.push");
>     }
>     
>     @Bean
>     public MessageConverter jsonMessageConverter() {
>         return new Jackson2JsonMessageConverter();
>     }
>     
>     @Bean
>     public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
>         RabbitTemplate template = new RabbitTemplate(connectionFactory);
>         template.setMessageConverter(jsonMessageConverter());
>         return template;
>     }
> }
> ```
> 
> ```java src/main/java/com/example/notification/service/NotificationQueueService.java
> package com.example.notification.service;
> 
> import com.example.notification.config.RabbitMQConfig;
> import com.example.notification.model.NotificationRequest;
> import lombok.RequiredArgsConstructor;
> import lombok.extern.slf4j.Slf4j;
> import org.springframework.amqp.rabbit.core.RabbitTemplate;
> import org.springframework.stereotype.Service;
> 
> @Service
> @RequiredArgsConstructor
> @Slf4j
> public class NotificationQueueService {
>     
>     private final RabbitTemplate rabbitTemplate;
>     
>     public void queueNotification(NotificationRequest request) {
>         String routingKey = getRoutingKey(request);
>         
>         rabbitTemplate.convertAndSend(
>                 RabbitMQConfig.NOTIFICATION_EXCHANGE,
>                 routingKey,
>                 request
>         );
>         
>         log.info("Notification queued with routing key: {}", routingKey);
>     }
>     
>     public void queueBulkNotifications(java.util.List<NotificationRequest> requests) {
>         requests.forEach(this::queueNotification);
>     }
>     
>     private String getRoutingKey(NotificationRequest request) {
>         return switch (request.getType()) {
>             case EMAIL -> "notification.email";
>             case SMS -> "notification.sms";
>             case PUSH -> "notification.push";
>             default -> RabbitMQConfig.NOTIFICATION_ROUTING_KEY;
>         };
>     }
> }
> ```
> 
> ```java src/main/java/com/example/notification/consumer/NotificationConsumer.java
> package com.example.notification.consumer;
> 
> import com.example.notification.config.RabbitMQConfig;
> import com.example.notification.model.NotificationRequest;
> import com.example.notification.service.*;
> import lombok.RequiredArgsConstructor;
> import lombok.extern.slf4j.Slf4j;
> import org.springframework.amqp.rabbit.annotation.RabbitListener;
> import org.springframework.stereotype.Component;
> 
> @Component
> @RequiredArgsConstructor
> @Slf4j
> public class NotificationConsumer {
>     
>     private final EmailNotificationService emailService;
>     private final SmsNotificationService smsService;
>     private final PushNotificationService pushService;
>     
>     @RabbitListener(queues = RabbitMQConfig.EMAIL_QUEUE)
>     public void consumeEmailNotification(NotificationRequest request) {
>         log.info("Consuming email notification for: {}", request.getRecipient());
>         try {
>             if (request.getTemplateName() != null) {
>                 emailService.sendTemplatedEmail(request);
>             } else {
>                 emailService.sendSimpleEmail(request);
>             }
>         } catch (Exception e) {
>             log.error("Error processing email notification", e);
>             // Could implement retry logic or dead letter queue handling
>         }
>     }
>     
>     @RabbitListener(queues = RabbitMQConfig.SMS_QUEUE)
>     public void consumeSmsNotification(NotificationRequest request) {
>         log.info("Consuming SMS notification for: {}", request.getRecipient());
>         try {
>             smsService.sendSms(request);
>         } catch (Exception e) {
>             log.error("Error processing SMS notification", e);
>         }
>     }
>     
>     @RabbitListener(queues = RabbitMQConfig.PUSH_QUEUE)
>     public void consumePushNotification(NotificationRequest request) {
>         log.info("Consuming push notification for: {}", request.getRecipient());
>         try {
>             pushService.sendPushNotification(request);
>         } catch (Exception e) {
>             log.error("Error processing push notification", e);
>         }
>     }
> }
> ```
> 
> ## 11. Pattern 7: Notification Template Pattern
> 
> ```java src/main/java/com/example/notification/model/

#### _User_

> Continue your response exactly where you left off:

#### _Assistant_

> ```java src/main/java/com/example/notification/model/NotificationTemplate.java
> package com.example.notification.model;
> 
> import jakarta.persistence.*;
> import lombok.AllArgsConstructor;
> import lombok.Builder;
> import lombok.Data;
> import lombok.NoArgsConstructor;
> 
> import java.time.LocalDateTime;
> 
> @Entity
> @Table(name = "notification_templates")
> @Data
> @Builder
> @NoArgsConstructor
> @AllArgsConstructor
> public class NotificationTemplate {
>     
>     @Id
>     @GeneratedValue(strategy = GenerationType.IDENTITY)
>     private Long id;
>     
>     @Column(unique = true, nullable = false)
>     private String name;
>     
>     @Column(nullable = false)
>     private String subject;
>     
>     @Column(columnDefinition = "TEXT", nullable = false)
>     private String bodyTemplate;
>     
>     @Enumerated(EnumType.STRING)
>     private NotificationType type;
>     
>     private String language;
>     
>     @Column(columnDefinition = "TEXT")
>     private String variables; // JSON string of expected variables
>     
>     private LocalDateTime createdAt;
>     
>     private LocalDateTime updatedAt;
>     
>     private boolean active;
>     
>     @PrePersist
>     protected void onCreate() {
>         createdAt = LocalDateTime.now();
>         updatedAt = LocalDateTime.now();
>         active = true;
>     }
>     
>     @PreUpdate
>     protected void onUpdate() {
>         updatedAt = LocalDateTime.now();
>     }
> }
> ```
> 
> ```java src/main/java/com/example/notification/repository/NotificationTemplateRepository.java
> package com.example.notification.repository;
> 
> import com.example.notification.model.NotificationTemplate;
> import com.example.notification.model.NotificationType;
> import org.springframework.data.jpa.repository.JpaRepository;
> import org.springframework.stereotype.Repository;
> 
> import java.util.List;
> import java.util.Optional;
> 
> @Repository
> public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, Long> {
>     
>     Optional<NotificationTemplate> findByNameAndActive(String name, boolean active);
>     
>     List<NotificationTemplate> findByTypeAndActive(NotificationType type, boolean active);
>     
>     Optional<NotificationTemplate> findByNameAndLanguageAndActive(String name, String language, boolean active);
> }
> ```
> 
> ```java src/main/java/com/example/notification/service/NotificationTemplateService.java
> package com.example.notification.service;
> 
> import com.example.notification.model.NotificationRequest;
> import com.example.notification.model.NotificationTemplate;
> import com.example.notification.model.NotificationType;
> import com.example.notification.repository.NotificationTemplateRepository;
> import lombok.RequiredArgsConstructor;
> import lombok.extern.slf4j.Slf4j;
> import org.springframework.stereotype.Service;
> import org.springframework.transaction.annotation.Transactional;
> 
> import java.util.Map;
> import java.util.regex.Matcher;
> import java.util.regex.Pattern;
> 
> @Service
> @RequiredArgsConstructor
> @Slf4j
> public class NotificationTemplateService {
>     
>     private final NotificationTemplateRepository templateRepository;
>     private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{(.*?)\\}\\}");
>     
>     @Transactional
>     public NotificationTemplate createTemplate(NotificationTemplate template) {
>         log.info("Creating notification template: {}", template.getName());
>         return templateRepository.save(template);
>     }
>     
>     @Transactional
>     public NotificationTemplate updateTemplate(Long id, NotificationTemplate template) {
>         NotificationTemplate existing = templateRepository.findById(id)
>                 .orElseThrow(() -> new RuntimeException("Template not found"));
>         
>         existing.setSubject(template.getSubject());
>         existing.setBodyTemplate(template.getBodyTemplate());
>         existing.setVariables(template.getVariables());
>         existing.setActive(template.isActive());
>         
>         return templateRepository.save(existing);
>     }
>     
>     public NotificationTemplate getTemplate(String name) {
>         return templateRepository.findByNameAndActive(name, true)
>                 .orElseThrow(() -> new RuntimeException("Template not found: " + name));
>     }
>     
>     public NotificationTemplate getTemplate(String name, String language) {
>         return templateRepository.findByNameAndLanguageAndActive(name, language, true)
>                 .orElseGet(() -> getTemplate(name)); // Fallback to default language
>     }
>     
>     public String renderTemplate(String templateName, Map<String, Object> variables) {
>         NotificationTemplate template = getTemplate(templateName);
>         return renderTemplate(template, variables);
>     }
>     
>     public String renderTemplate(NotificationTemplate template, Map<String, Object> variables) {
>         String content = template.getBodyTemplate();
>         
>         Matcher matcher = VARIABLE_PATTERN.matcher(content);
>         StringBuffer result = new StringBuffer();
>         
>         while (matcher.find()) {
>             String variableName = matcher.group(1).trim();
>             Object value = variables.getOrDefault(variableName, "");
>             matcher.appendReplacement(result, Matcher.quoteReplacement(String.valueOf(value)));
>         }
>         matcher.appendTail(result);
>         
>         return result.toString();
>     }
>     
>     public NotificationRequest buildFromTemplate(String templateName, 
>                                                  String userId, 
>                                                  String recipient,
>                                                  Map<String, Object> variables) {
>         NotificationTemplate template = getTemplate(templateName);
>         
>         String renderedMessage = renderTemplate(template, variables);
>         String renderedSubject = renderSubject(template.getSubject(), variables);
>         
>         return NotificationRequest.builder()
>                 .userId(userId)
>                 .recipient(recipient)
>                 .title(renderedSubject)
>                 .message(renderedMessage)
>                 .type(template.getType())
>                 .data(variables)
>                 .templateName(templateName)
>                 .build();
>     }
>     
>     private String renderSubject(String subject, Map<String, Object> variables) {
>         Matcher matcher = VARIABLE_PATTERN.matcher(subject);
>         StringBuffer result = new StringBuffer();
>         
>         while (matcher.find()) {
>             String variableName = matcher.group(1).trim();
>             Object value = variables.getOrDefault(variableName, "");
>             matcher.appendReplacement(result, Matcher.quoteReplacement(String.valueOf(value)));
>         }
>         matcher.appendTail(result);
>         
>         return result.toString();
>     }
>     
>     @Transactional
>     public void deactivateTemplate(Long id) {
>         NotificationTemplate template = templateRepository.findById(id)
>                 .orElseThrow(() -> new RuntimeException("Template not found"));
>         template.setActive(false);
>         templateRepository.save(template);
>     }
> }
> ```
> 
> ## 12. Orchestration Service (Combining All Patterns)
> 
> ```java src/main/java/com/example/notification/service/NotificationOrchestrationService.java
> package com.example.notification.service;
> 
> import com.example.notification.model.NotificationRequest;
> import com.example.notification.model.NotificationType;
> import lombok.RequiredArgsConstructor;
> import lombok.extern.slf4j.Slf4j;
> import org.springframework.stereotype.Service;
> 
> import java.util.List;
> import java.util.Map;
> 
> @Service
> @RequiredArgsConstructor
> @Slf4j
> public class NotificationOrchestrationService {
>     
>     private final PushNotificationService pushService;
>     private final SmsNotificationService smsService;
>     private final EmailNotificationService emailService;
>     private final InAppNotificationService inAppService;
>     private final RealTimeNotificationService realTimeService;
>     private final NotificationQueueService queueService;
>     private final NotificationTemplateService templateService;
>     
>     /**
>      * Send notification with automatic routing based on type
>      */
>     public void sendNotification(NotificationRequest request) {
>         log.info("Sending {} notification to {}", request.getType(), request.getRecipient());
>         
>         switch (request.getType()) {
>             case PUSH -> pushService.sendPushNotification(request);
>             case SMS -> smsService.sendSms(request);
>             case EMAIL -> emailService.sendSimpleEmail(request);
>             case IN_APP -> inAppService.createInAppNotification(request);
>             case REAL_TIME -> realTimeService.sendRealTimeNotification(request);
>             default -> log.warn("Unknown notification type: {}", request.getType());
>         }
>     }
>     
>     /**
>      * Send notification using queue pattern (async)
>      */
>     public void sendNotificationAsync(NotificationRequest request) {
>         log.info("Queueing {} notification", request.getType());
>         queueService.queueNotification(request);
>     }
>     
>     /**
>      * Send multi-channel notification
>      */
>     public void sendMultiChannelNotification(String userId,
>                                             String title,
>                                             String message,
>                                             List<NotificationType> channels,
>                                             Map<String, String> channelRecipients) {
>         channels.forEach(channel -> {
>             NotificationRequest request = NotificationRequest.builder()
>                     .userId(userId)
>                     .title(title)
>                     .message(message)
>                     .type(channel)
>                     .recipient(channelRecipients.get(channel.name()))
>                     .build();
>             
>             sendNotificationAsync(request);
>         });
>     }
>     
>     /**
>      * Send templated notification
>      */
>     public void sendTemplatedNotification(String templateName,
>                                          String userId,
>                                          String recipient,
>                                          NotificationType type,
>                                          Map<String, Object> variables) {
>         NotificationRequest request = templateService.buildFromTemplate(
>                 templateName, userId, recipient, variables);
>         request.setType(type);
>         
>         sendNotification(request);
>     }
>     
>     /**
>      * Send notification with fallback strategy
>      */
>     public void sendWithFallback(NotificationRequest primaryRequest,
>                                  NotificationRequest fallbackRequest) {
>         try {
>             sendNotification(primaryRequest);
>         } catch (Exception e) {
>             log.error("Primary notification failed, using fallback", e);
>             sendNotification(fallbackRequest);
>         }
>     }
>     
>     /**
>      * Send bulk notifications (queued)
>      */
>     public void sendBulkNotifications(List<NotificationRequest> requests) {
>         log.info("Sending bulk notifications: {} items", requests.size());
>         queueService.queueBulkNotifications(requests);
>     }
> }
> ```
> 
> ## 13. REST Controllers
> 
> ```java src/main/java/com/example/notification/controller/NotificationController.java
> package com.example.notification.controller;
> 
> import com.example.notification.model.Notification;
> import com.example.notification.model.NotificationRequest;
> import com.example.notification.service.InAppNotificationService;
> import com.example.notification.service.NotificationOrchestrationService;
> import lombok.RequiredArgsConstructor;
> import org.springframework.http.ResponseEntity;
> import org.springframework.web.bind.annotation.*;
> 
> import java.util.List;
> 
> @RestController
> @RequestMapping("/api/notifications")
> @RequiredArgsConstructor
> public class NotificationController {
>     
>     private final NotificationOrchestrationService orchestrationService;
>     private final InAppNotificationService inAppService;
>     
>     @PostMapping("/send")
>     public ResponseEntity<String> sendNotification(@RequestBody NotificationRequest request) {
>         orchestrationService.sendNotification(request);
>         return ResponseEntity.ok("Notification sent successfully");
>     }
>     
>     @PostMapping("/send-async")
>     public ResponseEntity<String> sendNotificationAsync(@RequestBody NotificationRequest request) {
>         orchestrationService.sendNotificationAsync(request);
>         return ResponseEntity.accepted().body("Notification queued successfully");
>     }
>     
>     @PostMapping("/send-bulk")
>     public ResponseEntity<String> sendBulkNotifications(
>             @RequestBody List<NotificationRequest> requests) {
>         orchestrationService.sendBulkNotifications(requests);
>         return ResponseEntity.accepted()
>                 .body("Bulk notifications queued: " + requests.size());
>     }
>     
>     @GetMapping("/user/{userId}")
>     public ResponseEntity<List<Notification>> getUserNotifications(
>             @PathVariable String userId) {
>         return ResponseEntity.ok(inAppService.getUserNotifications(userId));
>     }
>     
>     @GetMapping("/user/{userId}/unread")
>     public ResponseEntity<List<Notification>> getUnreadNotifications(
>             @PathVariable String userId) {
>         return ResponseEntity.ok(inAppService.getUnreadNotifications(userId));
>     }
>     
>     @GetMapping("/user/{userId}/unread-count")
>     public ResponseEntity<Long> getUnreadCount(@PathVariable String userId) {
>         return ResponseEntity.ok(inAppService.getUnreadCount(userId));
>     }
>     
>     @PutMapping("/{notificationId}/read")
>     public ResponseEntity<Notification> markAsRead(@PathVariable Long notificationId) {
>         return ResponseEntity.ok(inAppService.markAsRead(notificationId));
>     }
>     
>     @PutMapping("/user/{userId}/read-all")
>     public ResponseEntity<String> markAllAsRead(@PathVariable String userId) {
>         inAppService.markAllAsRead(userId);
>         return ResponseEntity.ok("All notifications marked as read");
>     }
>     
>     @DeleteMapping("/{notificationId}")
>     public ResponseEntity<String> deleteNotification(@PathVariable Long notificationId) {
>         inAppService.deleteNotification(notificationId);
>         return ResponseEntity.ok("Notification deleted");
>     }
> }
> ```
> 
> ```java src/main/java/com/example/notification/controller/NotificationTemplateController.java
> package com.example.notification.controller;
> 
> import com.example.notification.model.NotificationRequest;
> import com.example.notification.model.NotificationTemplate;
> import com.example.notification.service.NotificationTemplateService;
> import lombok.RequiredArgsConstructor;
> import org.springframework.http.ResponseEntity;
> import org.springframework.web.bind.annotation.*;
> 
> import java.util.Map;
> 
> @RestController
> @RequestMapping("/api/templates")
> @RequiredArgsConstructor
> public class NotificationTemplateController {
>     
>     private final NotificationTemplateService templateService;
>     
>     @PostMapping
>     public ResponseEntity<NotificationTemplate> createTemplate(
>             @RequestBody NotificationTemplate template) {
>         return ResponseEntity.ok(templateService.createTemplate(template));
>     }
>     
>     @PutMapping("/{id}")
>     public ResponseEntity<NotificationTemplate> updateTemplate(
>             @PathVariable Long id,
>             @RequestBody NotificationTemplate template) {
>         return ResponseEntity.ok(templateService.updateTemplate(id, template));
>     }
>     
>     @GetMapping("/{name}")
>     public ResponseEntity<NotificationTemplate> getTemplate(@PathVariable String name) {
>         return ResponseEntity.ok(templateService.getTemplate(name));
>     }
>     
>     @PostMapping("/{templateName}/render")
>     public ResponseEntity<String> renderTemplate(
>             @PathVariable String templateName,
>             @RequestBody Map<String, Object> variables) {
>         return ResponseEntity.ok(templateService.renderTemplate(templateName, variables));
>     }
>     
>     @PostMapping("/{templateName}/build")
>     public ResponseEntity<NotificationRequest> buildFromTemplate(
>             @PathVariable String templateName,
>             @RequestParam String userId,
>             @RequestParam String recipient,
>             @RequestBody Map<String, Object> variables) {
>         return ResponseEntity.ok(templateService.buildFromTemplate(
>                 templateName, userId, recipient, variables));
>     }
>     
>     @DeleteMapping("/{id}")
>     public ResponseEntity<String> deactivateTemplate(@PathVariable Long id) {
>         templateService.deactivateTemplate(id);
>         return ResponseEntity.ok("Template deactivated");
>     }
> }
> ```
> 
> ## 14. WebSocket Controller
> 
> ```java src/main/java/com/example/notification/controller/WebSocketNotificationController.java
> package com.example.notification.controller;
> 
> import com.example.notification.model.NotificationRequest;
> import com.example.notification.service.RealTimeNotificationService;
> import lombok.RequiredArgsConstructor;
> import org.springframework.messaging.handler.annotation.MessageMapping;
> import org.springframework.messaging.handler.annotation.Payload;
> import org.springframework.messaging.simp.annotation.SendToUser;
> import org.springframework.stereotype.Controller;
> 
> @Controller
> @RequiredArgsConstructor
> public class WebSocketNotificationController {
>     
>     private final RealTimeNotificationService realTimeService;
>     
>     @MessageMapping("/notification")
>     @SendToUser("/queue/notifications")
>     public NotificationRequest sendNotification(@Payload NotificationRequest request) {
>         return request;
>     }
>     
>     @MessageMapping("/notification/broadcast")
>     public void broadcastNotification(@Payload NotificationRequest request) {
>         realTimeService.broadcastNotification(request);
>     }
> }
> ```
> 
> ## 15. Main Application Class
> 
> ```java src/main/java/com/example/notification/NotificationApplication.java
> package com.example.notification;
> 
> import org.springframework.boot.SpringApplication;
> import org.springframework.boot.autoconfigure.SpringBootApplication;
> import org.springframework.scheduling.annotation.EnableAsync;
> 
> @SpringBootApplication
> @EnableAsync
> public class NotificationApplication {
>     
>     public static void main(String[] args) {
>         SpringApplication.run(NotificationApplication.class, args);
>     }
> }
> ```
> 
> ## 16. Email Template Example
> 
> ```html src/main/resources/templates/welcome-email.html
> <!DOCTYPE html>
> <html xmlns:th="http://www.thymeleaf.org">
> <head>
>     <meta charset="UTF-8">
>     <title>Welcome Email</title>
>     <style>
>         body {
>             font-family: Arial, sans-serif;
>             line-height: 1.6;
>             color: #333;
>         }
>         .container {
>             max-width: 600px;
>             margin: 0 auto;
>             padding: 20px;
>         }
>         .header {
>             background-color: #4CAF50;
>             color: white;
>             padding: 20px;
>             text-align: center;
>         }
>         .content {
>             padding: 20px;
>             background-color: #f9f9f9;
>         }
>         .button {
>             display: inline-block;
>             padding: 10px 20px;
>             background-color: #4CAF50;
>             color: white;
>             text-decoration: none;
>             border-radius: 5px;
>         }
>     </style>
> </head>
> <body>
>     <div class="container">
>         <div class="header">
>             <h1>Welcome to Our Service!</h1>
>         </div>
>         <div class="content">
>             <p>Hello <span th:text="${userName}">User</span>,</p>
>             <p>Thank you for joining us. We're excited to have you on board!</p>
>             <p>Your account has been created with the email: <strong th:text="${email}">email@example.com</strong></p>
>             <p>
>                 <a href="#" th:href="${activationLink}" class="button">Activate Your Account</a>
>             </p>
>             <p>If you have any questions, feel free to contact our support team.</p>
>             <p>Best regards,<br>The Team</p>
>         </div>
>     </div>
> </body>
> </html>
> ```
> 
> ## 17. Usage Examples
> 
> ```java src/test/java/com/example/notification/NotificationExamples.java
> package com.example.notification;
> 
> import com.example.notification.model.NotificationRequest;
> import com.example.notification.model.NotificationTemplate;
> import com.example.notification.model.NotificationType;
> import com.example.notification.service.NotificationOrchestrationService;
> import com.example.notification.service.NotificationTemplateService;
> import lombok.RequiredArgsConstructor;
> import org.springframework.stereotype.Component;
> 
> import java.util.HashMap;
> import java.util.List;
> import java.util.Map;
> 
> @Component
> @RequiredArgsConstructor
> public class NotificationExamples {
>     
>     private final NotificationOrchestrationService orchestrationService;
>     private final NotificationTemplateService templateService;
>     
>     // Example 1: Send simple push notification
>     public void sendPushExample() {
>         NotificationRequest request = NotificationRequest.builder()
>                 .userId("user123")
>                 .title("New Message")
>                 .message("You have a new message from John")
>                 .type(NotificationType.PUSH)
>                 .recipient("FCM_DEVICE_TOKEN_HERE")
>                 .build();
>         
>         orchestrationService.sendNotification(request);
>     }
>     
>     // Example 2: Send SMS
>     public void sendSmsExample() {
>         NotificationRequest request = NotificationRequest.builder()
>                 .userId("user123")
>                 .title("Verification Code")
>                 .message("Your verification code is: 123456")
>                 .type(NotificationType.SMS)
>                 .recipient("+1234567890")
>                 .build();
>         
>         orchestrationService.sendNotificationAsync(request);
>     }
>     
>     // Example 3: Send templated email
>     public void sendTemplatedEmailExample() {
>         Map<String, Object> variables = new HashMap<>();
>         variables.put("userName", "John Doe");
>         variables.put("email", "john@example.com");
>         variables.put("activationLink", "https://example.com/activate/token123");
>         
>         orchestrationService.sendTemplatedNotification(
>                 "welcome-email",
>                 "user123",
>                 "john@example.com",
>                 NotificationType.EMAIL,
>                 variables
>         );
>     }
>     
>     // Example 4: Multi-channel notification
>     public void sendMultiChannelExample() {
>         Map<String, String> recipients = new HashMap<>();
>         recipients.put("EMAIL", "user@example.com");
>         recipients.put("SMS", "+1234567890");
>         recipients.put("PUSH", "FCM_TOKEN");
>         
>         orchestrationService.sendMultiChannelNotification(
>                 "user123",
>                 "Important Update",
>                 "Your order has been shipped",
>                 List.of(NotificationType.EMAIL, NotificationType.SMS, NotificationType.PUSH),
>                 recipients
>         );
>     }
>     
>     // Example 5: Create notification template
>     public void createTemplateExample() {
>         NotificationTemplate template = NotificationTemplate.builder()
>                 .name("order-confirmation")
>                 .subject("Order Confirmation - #{{orderNumber}}")
>                 .bodyTemplate("Hello {{customerName}},\n\n" +
>                         "Your order #{{orderNumber}} has been confirmed.\n" +
>                         "Total: ${{totalAmount}}\n\n" +
>                         "Thank you for your purchase!")
>                 .type(NotificationType.EMAIL)
>                 .language("en")
>                 .variables("[\"customerName\", \"orderNumber\", \"totalAmount\"]")
>                 .build();
>         
>         templateService.createTemplate(template);
>     }
> }
> ```
> 
> This complete Spring Boot application demonstrates all seven notification patterns with proper separation of concerns, async processing, template support, and multi-channel delivery capabilities!