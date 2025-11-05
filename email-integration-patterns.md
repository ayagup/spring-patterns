### [Continue](https://continue.dev) session transcript
 Exported: 11/5/2025, 2:10:54 PM

#### _User_

> generate a java program in spring for each of the below patterns:
> ```
> **Email Integration Patterns**
> - Mail Sender Pattern
> - MIME Message Pattern
> - Template-based Email Pattern
> - Attachment Handling Pattern
> - HTML Email Pattern
> - Async Email Pattern
> ```

#### _Assistant_

> I'll create a comprehensive Spring Boot application demonstrating all these email integration patterns.
> 
> ## Project Structure
> 
> ```
> spring-email-patterns/
> ├── pom.xml
> ├── src/main/java/com/example/emailpatterns/
> │   ├── EmailPatternsApplication.java
> │   ├── config/
> │   │   └── EmailConfig.java
> │   ├── model/
> │   │   ├── EmailRequest.java
> │   │   └── EmailAttachment.java
> │   ├── service/
> │   │   ├── MailSenderPatternService.java
> │   │   ├── MimeMessagePatternService.java
> │   │   ├── TemplateEmailPatternService.java
> │   │   ├── AttachmentHandlingPatternService.java
> │   │   ├── HtmlEmailPatternService.java
> │   │   └── AsyncEmailPatternService.java
> │   └── controller/
> │       └── EmailController.java
> └── src/main/resources/
>     ├── application.yml
>     └── templates/
>         ├── welcome-email.html
>         └── invoice-email.html
> ```
> 
> ## 1. Maven Dependencies
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
>         <version>3.2.0</version>
>         <relativePath/>
>     </parent>
>     
>     <groupId>com.example</groupId>
>     <artifactId>spring-email-patterns</artifactId>
>     <version>1.0.0</version>
>     
>     <properties>
>         <java.version>17</java.version>
>     </properties>
>     
>     <dependencies>
>         <!-- Spring Boot Starter Mail -->
>         <dependency>
>             <groupId>org.springframework.boot</groupId>
>             <artifactId>spring-boot-starter-mail</artifactId>
>         </dependency>
>         
>         <!-- Spring Boot Starter Web -->
>         <dependency>
>             <groupId>org.springframework.boot</groupId>
>             <artifactId>spring-boot-starter-web</artifactId>
>         </dependency>
>         
>         <!-- Thymeleaf for templating -->
>         <dependency>
>             <groupId>org.springframework.boot</groupId>
>             <artifactId>spring-boot-starter-thymeleaf</artifactId>
>         </dependency>
>         
>         <!-- FreeMarker (alternative templating) -->
>         <dependency>
>             <groupId>org.springframework.boot</groupId>
>             <artifactId>spring-boot-starter-freemarker</artifactId>
>         </dependency>
>         
>         <!-- Lombok -->
>         <dependency>
>             <groupId>org.projectlombok</groupId>
>             <artifactId>lombok</artifactId>
>             <optional>true</optional>
>         </dependency>
>         
>         <!-- Validation -->
>         <dependency>
>             <groupId>org.springframework.boot</groupId>
>             <artifactId>spring-boot-starter-validation</artifactId>
>         </dependency>
>     </dependencies>
>     
>     <build>
>         <plugins>
>             <plugin>
>                 <groupId>org.springframework.boot</groupId>
>                 <artifactId>spring-boot-maven-plugin</artifactId>
>             </plugin>
>         </plugins>
>     </build>
> </project>
> ```
> 
> ## 2. Application Configuration
> 
> ```yaml application.yml
> spring:
>   application:
>     name: spring-email-patterns
>   
>   mail:
>     host: smtp.gmail.com
>     port: 587
>     username: ${MAIL_USERNAME:your-email@gmail.com}
>     password: ${MAIL_PASSWORD:your-app-password}
>     properties:
>       mail:
>         smtp:
>           auth: true
>           starttls:
>             enable: true
>             required: true
>         debug: false
>     
>   thymeleaf:
>     cache: false
>     mode: HTML
>     encoding: UTF-8
> 
>   task:
>     execution:
>       pool:
>         core-size: 5
>         max-size: 10
>         queue-capacity: 100
> 
> logging:
>   level:
>     com.example.emailpatterns: DEBUG
>     org.springframework.mail: DEBUG
> ```
> 
> ## 3. Main Application
> 
> ```java src/main/java/com/example/emailpatterns/EmailPatternsApplication.java
> package com.example.emailpatterns;
> 
> import org.springframework.boot.SpringApplication;
> import org.springframework.boot.autoconfigure.SpringBootApplication;
> import org.springframework.scheduling.annotation.EnableAsync;
> 
> @SpringBootApplication
> @EnableAsync
> public class EmailPatternsApplication {
>     public static void main(String[] args) {
>         SpringApplication.run(EmailPatternsApplication.class, args);
>     }
> }
> ```
> 
> ## 4. Email Configuration
> 
> ```java src/main/java/com/example/emailpatterns/config/EmailConfig.java
> package com.example.emailpatterns.config;
> 
> import org.springframework.context.annotation.Bean;
> import org.springframework.context.annotation.Configuration;
> import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
> 
> import java.util.concurrent.Executor;
> 
> @Configuration
> public class EmailConfig {
>     
>     @Bean(name = "emailTaskExecutor")
>     public Executor emailTaskExecutor() {
>         ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
>         executor.setCorePoolSize(5);
>         executor.setMaxPoolSize(10);
>         executor.setQueueCapacity(100);
>         executor.setThreadNamePrefix("email-async-");
>         executor.initialize();
>         return executor;
>     }
> }
> ```
> 
> ## 5. Model Classes
> 
> ```java src/main/java/com/example/emailpatterns/model/EmailRequest.java
> package com.example.emailpatterns.model;
> 
> import jakarta.validation.constraints.Email;
> import jakarta.validation.constraints.NotBlank;
> import lombok.AllArgsConstructor;
> import lombok.Builder;
> import lombok.Data;
> import lombok.NoArgsConstructor;
> 
> import java.util.List;
> import java.util.Map;
> 
> @Data
> @Builder
> @NoArgsConstructor
> @AllArgsConstructor
> public class EmailRequest {
>     
>     @NotBlank(message = "Recipient email is required")
>     @Email(message = "Invalid email format")
>     private String to;
>     
>     private List<String> cc;
>     
>     private List<String> bcc;
>     
>     @NotBlank(message = "Subject is required")
>     private String subject;
>     
>     @NotBlank(message = "Content is required")
>     private String content;
>     
>     private List<EmailAttachment> attachments;
>     
>     private Map<String, Object> templateVariables;
>     
>     private String templateName;
>     
>     private boolean isHtml;
> }
> ```
> 
> ```java src/main/java/com/example/emailpatterns/model/EmailAttachment.java
> package com.example.emailpatterns.model;
> 
> import lombok.AllArgsConstructor;
> import lombok.Builder;
> import lombok.Data;
> import lombok.NoArgsConstructor;
> 
> @Data
> @Builder
> @NoArgsConstructor
> @AllArgsConstructor
> public class EmailAttachment {
>     private String filename;
>     private byte[] content;
>     private String contentType;
> }
> ```
> 
> ## 6. Pattern 1: Mail Sender Pattern
> 
> ```java src/main/java/com/example/emailpatterns/service/MailSenderPatternService.java
> package com.example.emailpatterns.service;
> 
> import com.example.emailpatterns.model.EmailRequest;
> import lombok.RequiredArgsConstructor;
> import lombok.extern.slf4j.Slf4j;
> import org.springframework.mail.MailException;
> import org.springframework.mail.SimpleMailMessage;
> import org.springframework.mail.javamail.JavaMailSender;
> import org.springframework.stereotype.Service;
> 
> /**
>  * Mail Sender Pattern - Simple text email using SimpleMailMessage
>  * Best for: Basic text emails without attachments or HTML
>  */
> @Slf4j
> @Service
> @RequiredArgsConstructor
> public class MailSenderPatternService {
>     
>     private final JavaMailSender mailSender;
>     
>     public void sendSimpleEmail(EmailRequest emailRequest) {
>         try {
>             SimpleMailMessage message = new SimpleMailMessage();
>             message.setTo(emailRequest.getTo());
>             message.setSubject(emailRequest.getSubject());
>             message.setText(emailRequest.getContent());
>             
>             if (emailRequest.getCc() != null && !emailRequest.getCc().isEmpty()) {
>                 message.setCc(emailRequest.getCc().toArray(new String[0]));
>             }
>             
>             if (emailRequest.getBcc() != null && !emailRequest.getBcc().isEmpty()) {
>                 message.setBcc(emailRequest.getBcc().toArray(new String[0]));
>             }
>             
>             mailSender.send(message);
>             log.info("Simple email sent successfully to: {}", emailRequest.getTo());
>             
>         } catch (MailException e) {
>             log.error("Failed to send simple email to: {}", emailRequest.getTo(), e);
>             throw new RuntimeException("Failed to send email", e);
>         }
>     }
>     
>     public void sendBulkSimpleEmails(EmailRequest... emailRequests) {
>         SimpleMailMessage[] messages = new SimpleMailMessage[emailRequests.length];
>         
>         for (int i = 0; i < emailRequests.length; i++) {
>             SimpleMailMessage message = new SimpleMailMessage();
>             message.setTo(emailRequests[i].getTo());
>             message.setSubject(emailRequests[i].getSubject());
>             message.setText(emailRequests[i].getContent());
>             messages[i] = message;
>         }
>         
>         try {
>             mailSender.send(messages);
>             log.info("Bulk emails sent successfully, count: {}", messages.length);
>         } catch (MailException e) {
>             log.error("Failed to send bulk emails", e);
>             throw new RuntimeException("Failed to send bulk emails", e);
>         }
>     }
> }
> ```
> 
> ## 7. Pattern 2: MIME Message Pattern
> 
> ```java src/main/java/com/example/emailpatterns/service/MimeMessagePatternService.java
> package com.example.emailpatterns.service;
> 
> import com.example.emailpatterns.model.EmailRequest;
> import jakarta.mail.MessagingException;
> import jakarta.mail.internet.MimeMessage;
> import lombok.RequiredArgsConstructor;
> import lombok.extern.slf4j.Slf4j;
> import org.springframework.mail.javamail.JavaMailSender;
> import org.springframework.mail.javamail.MimeMessageHelper;
> import org.springframework.stereotype.Service;
> 
> /**
>  * MIME Message Pattern - Advanced email with full MIME support
>  * Best for: Complex emails requiring full control over headers and content
>  */
> @Slf4j
> @Service
> @RequiredArgsConstructor
> public class MimeMessagePatternService {
>     
>     private final JavaMailSender mailSender;
>     
>     public void sendMimeEmail(EmailRequest emailRequest) {
>         try {
>             MimeMessage mimeMessage = mailSender.createMimeMessage();
>             MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
>             
>             helper.setTo(emailRequest.getTo());
>             helper.setSubject(emailRequest.getSubject());
>             helper.setText(emailRequest.getContent(), emailRequest.isHtml());
>             
>             if (emailRequest.getCc() != null && !emailRequest.getCc().isEmpty()) {
>                 helper.setCc(emailRequest.getCc().toArray(new String[0]));
>             }
>             
>             if (emailRequest.getBcc() != null && !emailRequest.getBcc().isEmpty()) {
>                 helper.setBcc(emailRequest.getBcc().toArray(new String[0]));
>             }
>             
>             // Add custom headers
>             mimeMessage.setHeader("X-Priority", "1");
>             mimeMessage.setHeader("X-Mailer", "Spring Email Patterns");
>             
>             mailSender.send(mimeMessage);
>             log.info("MIME email sent successfully to: {}", emailRequest.getTo());
>             
>         } catch (MessagingException e) {
>             log.error("Failed to send MIME email to: {}", emailRequest.getTo(), e);
>             throw new RuntimeException("Failed to send MIME email", e);
>         }
>     }
>     
>     public void sendMultipartEmail(EmailRequest emailRequest) {
>         try {
>             MimeMessage mimeMessage = mailSender.createMimeMessage();
>             MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
>             
>             helper.setTo(emailRequest.getTo());
>             helper.setSubject(emailRequest.getSubject());
>             
>             // Set both text and HTML content
>             String textContent = emailRequest.getContent();
>             String htmlContent = "<html><body><h1>" + emailRequest.getContent() + "</h1></body></html>";
>             
>             helper.setText(textContent, htmlContent);
>             
>             mailSender.send(mimeMessage);
>             log.info("Multipart MIME email sent successfully to: {}", emailRequest.getTo());
>             
>         } catch (MessagingException e) {
>             log.error("Failed to send multipart MIME email", e);
>             throw new RuntimeException("Failed to send multipart MIME email", e);
>         }
>     }
> }
> ```
> 
> ## 8. Pattern 3: Template-based Email Pattern
> 
> ```java src/main/java/com/example/emailpatterns/service/TemplateEmailPatternService.java
> package com.example.emailpatterns.service;
> 
> import com.example.emailpatterns.model.EmailRequest;
> import freemarker.template.Configuration;
> import freemarker.template.Template;
> import jakarta.mail.MessagingException;
> import jakarta.mail.internet.MimeMessage;
> import lombok.RequiredArgsConstructor;
> import lombok.extern.slf4j.Slf4j;
> import org.springframework.mail.javamail.JavaMailSender;
> import org.springframework.mail.javamail.MimeMessageHelper;
> import org.springframework.stereotype.Service;
> import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
> import org.thymeleaf.TemplateEngine;
> import org.thymeleaf.context.Context;
> 
> import java.util.Map;
> 
> /**
>  * Template-based Email Pattern - Using Thymeleaf and FreeMarker
>  * Best for: Professional emails with consistent branding
>  */
> @Slf4j
> @Service
> @RequiredArgsConstructor
> public class TemplateEmailPatternService {
>     
>     private final JavaMailSender mailSender;
>     private final TemplateEngine thymeleafTemplateEngine;
>     private final Configuration freemarkerConfiguration;
>     
>     public void sendThymeleafTemplateEmail(EmailRequest emailRequest) {
>         try {
>             Context context = new Context();
>             context.setVariables(emailRequest.getTemplateVariables());
>             
>             String htmlContent = thymeleafTemplateEngine.process(
>                 emailRequest.getTemplateName(), 
>                 context
>             );
>             
>             MimeMessage mimeMessage = mailSender.createMimeMessage();
>             MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
>             
>             helper.setTo(emailRequest.getTo());
>             helper.setSubject(emailRequest.getSubject());
>             helper.setText(htmlContent, true);
>             
>             mailSender.send(mimeMessage);
>             log.info("Thymeleaf template email sent to: {}", emailRequest.getTo());
>             
>         } catch (MessagingException e) {
>             log.error("Failed to send Thymeleaf template email", e);
>             throw new RuntimeException("Failed to send template email", e);
>         }
>     }
>     
>     public void sendFreeMarkerTemplateEmail(EmailRequest emailRequest) {
>         try {
>             Template template = freemarkerConfiguration.getTemplate(
>                 emailRequest.getTemplateName() + ".ftl"
>             );
>             
>             String htmlContent = FreeMarkerTemplateUtils.processTemplateIntoString(
>                 template, 
>                 emailRequest.getTemplateVariables()
>             );
>             
>             MimeMessage mimeMessage = mailSender.createMimeMessage();
>             MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
>             
>             helper.setTo(emailRequest.getTo());
>             helper.setSubject(emailRequest.getSubject());
>             helper.setText(htmlContent, true);
>             
>             mailSender.send(mimeMessage);
>             log.info("FreeMarker template email sent to: {}", emailRequest.getTo());
>             
>         } catch (Exception e) {
>             log.error("Failed to send FreeMarker template email", e);
>             throw new RuntimeException("Failed to send template email", e);
>         }
>     }
>     
>     public void sendDynamicTemplateEmail(String to, String templateName, Map<String, Object> variables) {
>         EmailRequest request = EmailRequest.builder()
>             .to(to)
>             .subject(variables.getOrDefault("subject", "Email Notification").toString())
>             .templateName(templateName)
>             .templateVariables(variables)
>             .build();
>             
>         sendThymeleafTemplateEmail(request);
>     }
> }
> ```
> 
> ## 9. Pattern 4: Attachment Handling Pattern
> 
> ```java src/main/java/com/example/emailpatterns/service/AttachmentHandlingPatternService.java
> package com.example.emailpatterns.service;
> 
> import com.example.emailpatterns.model.EmailAttachment;
> import com.example.emailpatterns.model.EmailRequest;
> import jakarta.activation.DataSource;
> import jakarta.mail.MessagingException;
> import jakarta.mail.internet.MimeMessage;
> import jakarta.mail.util.ByteArrayDataSource;
> import lombok.RequiredArgsConstructor;
> import lombok.extern.slf4j.Slf4j;
> import org.springframework.core.io.ByteArrayResource;
> import org.springframework.core.io.ClassPathResource;
> import org.springframework.core.io.FileSystemResource;
> import org.springframework.mail.javamail.JavaMailSender;
> import org.springframework.mail.javamail.MimeMessageHelper;
> import org.springframework.stereotype.Service;
> 
> import java.io.File;
> import java.io.IOException;
> import java.nio.file.Files;
> 
> /**
>  * Attachment Handling Pattern - Managing file attachments
>  * Best for: Sending files, reports, documents, and images
>  */
> @Slf4j
> @Service
> @RequiredArgsConstructor
> public class AttachmentHandlingPatternService {
>     
>     private final JavaMailSender mailSender;
>     
>     public void sendEmailWithAttachments(EmailRequest emailRequest) {
>         try {
>             MimeMessage mimeMessage = mailSender.createMimeMessage();
>             MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
>             
>             helper.setTo(emailRequest.getTo());
>             helper.setSubject(emailRequest.getSubject());
>             helper.setText(emailRequest.getContent(), emailRequest.isHtml());
>             
>             // Add attachments
>             if (emailRequest.getAttachments() != null) {
>                 for (EmailAttachment attachment : emailRequest.getAttachments()) {
>                     DataSource dataSource = new ByteArrayDataSource(
>                         attachment.getContent(),
>                         attachment.getContentType()
>                     );
>                     helper.addAttachment(attachment.getFilename(), dataSource);
>                 }
>             }
>             
>             mailSender.send(mimeMessage);
>             log.info("Email with {} attachments sent to: {}", 
>                 emailRequest.getAttachments().size(), emailRequest.getTo());
>             
>         } catch (MessagingException e) {
>             log.error("Failed to send email with attachments", e);
>             throw new RuntimeException("Failed to send email with attachments", e);
>         }
>     }
>     
>     public void sendEmailWithFileSystemAttachment(String to, String subject, 
>                                                    String content, String filePath) {
>         try {
>             MimeMessage mimeMessage = mailSender.createMimeMessage();
>             MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
>             
>             helper.setTo(to);
>             helper.setSubject(subject);
>             helper.setText(content, true);
>             
>             FileSystemResource file = new FileSystemResource(new File(filePath));
>             helper.addAttachment(file.getFilename(), file);
>             
>             mailSender.send(mimeMessage);
>             log.info("Email with file system attachment sent to: {}", to);
>             
>         } catch (MessagingException e) {
>             log.error("Failed to send email with file system attachment", e);
>             throw new RuntimeException("Failed to send email", e);
>         }
>     }
>     
>     public void sendEmailWithClasspathAttachment(String to, String subject, 
>                                                   String content, String resourcePath) {
>         try {
>             MimeMessage mimeMessage = mailSender.createMimeMessage();
>             MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
>             
>             helper.setTo(to);
>             helper.setSubject(subject);
>             helper.setText(content, true);
>             
>             ClassPathResource resource = new ClassPathResource(resourcePath);
>             helper.addAttachment(resource.getFilename(), resource);
>             
>             mailSender.send(mimeMessage);
>             log.info("Email with classpath attachment sent to: {}", to);
>             
>         } catch (MessagingException e) {
>             log.error("Failed to send email with classpath attachment", e);
>             throw new RuntimeException("Failed to send email", e);
>         }
>     }
>     
>     public void sendEmailWithInlineImage(String to, String subject, 
>                                          String htmlContent, String imagePath) {
>         try {
>             MimeMessage mimeMessage = mailSender.createMimeMessage();
>             MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
>             
>             helper.setTo(to);
>             helper.setSubject(subject);
>             
>             // HTML content with inline image reference
>             String html = htmlContent + "<br/><img src='cid:logo'/>";
>             helper.setText(html, true);
>             
>             // Add inline image
>             FileSystemResource res = new FileSystemResource(new File(imagePath));
>             helper.addInline("logo", res);
>             
>             mailSender.send(mimeMessage);
>             log.info("Email with inline image sent to: {}", to);
>             
>         } catch (MessagingException e) {
>             log.error("Failed to send email with inline image", e);
>             throw new RuntimeException("Failed to send email", e);
>         }
>     }
> }
> ```
> 
> ## 10. Pattern 5: HTML Email Pattern
> 
> ```java src/main/java/com/example/emailpatterns/service/HtmlEmailPatternService.java
> package com.example.emailpatterns.service;
> 
> import com.example.emailpatterns.model.EmailRequest;
> import jakarta.mail.MessagingException;
> import jakarta.mail.internet.MimeMessage;
> import lombok.RequiredArgsConstructor;
> import lombok.extern.slf4j.Slf4j;
> import org.springframework.mail.javamail.JavaMailSender;
> import org.springframework.mail.javamail.MimeMessageHelper;
> import org.springframework.stereotype.Service;
> 
> /**
>  * HTML Email Pattern - Rich HTML formatted emails
>  * Best for: Marketing emails, newsletters, and formatted notifications
>  */
> @Slf4j
> @Service
> @RequiredArgsConstructor
> public class HtmlEmailPatternService {
>     
>     private final JavaMailSender mailSender;
>     
>     public void sendHtmlEmail(EmailRequest emailRequest) {
>         try {
>             MimeMessage mimeMessage = mailSender.createMimeMessage();
>             MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
>             
>             helper.setTo(emailRequest.getTo());
>             helper.setSubject(emailRequest.getSubject());
>             helper.setText(emailRequest.getContent(), true); // true = HTML
>             
>             mailSender.send(mimeMessage);
>             log.info("HTML email sent to: {}", emailRequest.getTo());
>             
>         } catch (MessagingException e) {
>             log.error("Failed to send HTML email", e);
>             throw new RuntimeException("Failed to send HTML email", e);
>         }
>     }
>     
>     public void sendRichHtmlEmail(String to, String subject, String userName) {
>         String htmlContent = buildRichHtmlContent(userName);
>         
>         EmailRequest request = EmailRequest.builder()
>             .to(to)
>             .subject(subject)
>             .content(htmlContent)
>             .isHtml(true)
>             .build();
>             
>         sendHtmlEmail(request);
>     }
>     
>     private String buildRichHtmlContent(String userName) {
>         return """
>             <!DOCTYPE html>
>             <html>
>             <head>
>                 <style>
>                     body {
>                         font-family: Arial, sans-serif;
>                         line-height: 1.6;
>                         color: #333;
>                     }
>                     .container {
>                         max-width: 600px;
>                         margin: 0 auto;
>                         padding: 20px;
>                         background-color: #f4f4f4;
>                     }
>                     .header {
>                         background-color: #007bff;
>                         color: white;
>                         padding: 20px;
>                         text-align: center;
>                     }
>                     .content {
>                         background-color: white;
>                         padding: 30px;
>                         border-radius: 5px;
>                     }
>                     .button {
>                         display: inline-block;
>                         padding: 12px 30px;
>                         background-color: #28a745;
>                         color: white;
>                         text-decoration: none;
>                         border-radius: 5px;
>                         margin: 20px 0;
>                     }
>                     .footer {
>                         text-align: center;
>                         padding: 20px;
>                         font-size: 12px;
>                         color: #666;
>                     }
>                 </style>
>             </head>
>             <body>
>                 <div class="container">
>                     <div class="header">
>                         <h1>Welcome to Our Service!</h1>
>                     </div>
>                     <div class="content">
>                         <h2>Hello %s!</h2>
>                         <p>Thank you for joining our platform. We're excited to have you on board.</p>
>                         <p>Here are some quick links to get you started:</p>
>                         <ul>
>                             <li>Complete your profile</li>
>                             <li>Explore our features</li>
>                             <li>Connect with others</li>
>                         </ul>
>                         <center>
>                             <a href="https://example.com/dashboard" class="button">Go to Dashboard</a>
>                         </center>
>                         <p>If you have any questions, feel free to reach out to our support team.</p>
>                     </div>
>                     <div class="footer">
>                         <p>&copy; 2024 Your Company. All rights reserved.</p>
>                         <p>123 Email Street, Tech City, TC 12345</p>
>                     </div>
>                 </div>
>             </body>
>             </html>
>             """.formatted(userName);
>     }
>     
>     public void sendResponsiveHtmlEmail(String to, String subject) {
>         String responsiveHtml = """
>             <!DOCTYPE html>
>             <html>
>             <head>
>                 <meta name="viewport" content="width=device-width, initial-scale=1.0">
>                 <style>
>                     @media only screen and (max-width: 600px) {
>                         .container {
>                             width: 100% !important;
>                         }
>                         .content {
>                             padding: 15px !important;
>                         }
>                     }
>                 </style>
>             </head>
>             <body>
>                 <table width="100%" cellpadding="0" cellspacing="0">
>                     <tr>
>                         <td align="center">
>                             <table class="container" width="600" cellpadding="0" cellspacing="0">
>                                 <tr>
>                                     <td class="content" style="padding: 30px;">
>                                         <h1>Responsive Email</h1>
>                                         <p>This email looks great on all devices!</p>
>                                     </td>
>                                 </tr>
>                             </table>
>                         </td>
>                     </tr>
>                 </table>
>             </body>
>             </html>
>             """;
>             
>         EmailRequest request = EmailRequest.builder()
>             .to(to)
>             .subject(subject)
>             .content(responsiveHtml)
>             .isHtml(true)
>             .build();
>             
>         sendHtmlEmail(request);
>     }
> }
> ```
> 
> ## 11. Pattern 6: Async Email Pattern
> 
> ```java src/main/java/com/example/emailpatterns/service/AsyncEmailPatternService.java
> package com.example.emailpatterns.service;
> 
> import com.example.emailpatterns.model.EmailRequest;
> import jakarta.mail.MessagingException;
> import jakarta.mail.internet.MimeMessage;
> import lombok.RequiredArgsConstructor;
> import lombok.extern.slf4j.Slf4j;
> import org.springframework.mail.javamail.JavaMailSender;
> import org.springframework.mail.javamail.MimeMessageHelper;
> import org.springframework.scheduling.annotation.Async;
> import org.springframework.stereotype.Service;
> 
> import java.util.List;
> import java.util.concurrent.CompletableFuture;
> 
> /**
>  * Async Email Pattern - Non-blocking email sending
>  * Best for: High-volume emails, bulk operations, and better performance
>  */
> @Slf4j
> @Service
> @RequiredArgsConstructor
> public class AsyncEmailPatternService {
>     
>     private final JavaMailSender mailSender;
>     
>     @Async("emailTaskExecutor")
>     public CompletableFuture<Boolean> sendEmailAsync(EmailRequest emailRequest) {
>         log.info("Starting async email send to: {} on thread: {}", 
>             emailRequest.getTo(), Thread.currentThread().getName());
>         
>         try {
>             MimeMessage mimeMessage = mailSender.createMimeMessage();
>             MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
>             
>             helper.setTo(emailRequest.getTo());
>             helper.setSubject(emailRequest.getSubject());
>             helper.setText(emailRequest.getContent(), emailRequest.isHtml());
>             
>             mailSender.send(mimeMessage);
>             
>             log.info("Async email sent successfully to: {}", emailRequest.getTo());
>             return CompletableFuture.completedFuture(true);
>             
>         } catch (MessagingException e) {
>             log.error("Failed to send async email to: {}", emailRequest.getTo(), e);
>             return CompletableFuture.completedFuture(false);
>         }
>     }
>     
>     @Async("emailTaskExecutor")
>     public void sendEmailAsyncFireAndForget(EmailRequest emailRequest) {
>         log.info("Fire-and-forget email send to: {} on thread: {}", 
>             emailRequest.getTo(), Thread.currentThread().getName());
>         
>         try {
>             MimeMessage mimeMessage = mailSender.createMimeMessage();
>             MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
>             
>             helper.setTo(emailRequest.getTo());
>             helper.setSubject(emailRequest.getSubject());
>             helper.setText(emailRequest.getContent(), emailRequest.isHtml());
>             

#### _User_

> Continue your response exactly where you left off:

#### _Assistant_

> ```java src/main/java/com/example/emailpatterns/service/AsyncEmailPatternService.java
>             mailSender.send(mimeMessage);
>             
>             log.info("Fire-and-forget email sent to: {}", emailRequest.getTo());
>             
>         } catch (MessagingException e) {
>             log.error("Failed to send fire-and-forget email", e);
>         }
>     }
>     
>     @Async("emailTaskExecutor")
>     public CompletableFuture<List<Boolean>> sendBulkEmailsAsync(List<EmailRequest> emailRequests) {
>         log.info("Starting bulk async email send for {} emails on thread: {}", 
>             emailRequests.size(), Thread.currentThread().getName());
>         
>         List<Boolean> results = emailRequests.stream()
>             .map(request -> {
>                 try {
>                     MimeMessage mimeMessage = mailSender.createMimeMessage();
>                     MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
>                     
>                     helper.setTo(request.getTo());
>                     helper.setSubject(request.getSubject());
>                     helper.setText(request.getContent(), request.isHtml());
>                     
>                     mailSender.send(mimeMessage);
>                     log.info("Bulk email sent to: {}", request.getTo());
>                     return true;
>                     
>                 } catch (MessagingException e) {
>                     log.error("Failed to send bulk email to: {}", request.getTo(), e);
>                     return false;
>                 }
>             })
>             .toList();
>         
>         log.info("Bulk async email send completed. Success: {}/{}", 
>             results.stream().filter(r -> r).count(), results.size());
>         
>         return CompletableFuture.completedFuture(results);
>     }
>     
>     public void sendEmailsInParallel(List<EmailRequest> emailRequests) {
>         List<CompletableFuture<Boolean>> futures = emailRequests.stream()
>             .map(this::sendEmailAsync)
>             .toList();
>         
>         // Wait for all emails to complete
>         CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
>             .thenRun(() -> {
>                 long successCount = futures.stream()
>                     .map(CompletableFuture::join)
>                     .filter(result -> result)
>                     .count();
>                 log.info("Parallel email sending completed. Success: {}/{}", 
>                     successCount, futures.size());
>             });
>     }
>     
>     @Async("emailTaskExecutor")
>     public CompletableFuture<Void> sendDelayedEmail(EmailRequest emailRequest, long delayMillis) {
>         try {
>             log.info("Scheduling delayed email to: {} with delay: {}ms", 
>                 emailRequest.getTo(), delayMillis);
>             
>             Thread.sleep(delayMillis);
>             
>             MimeMessage mimeMessage = mailSender.createMimeMessage();
>             MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
>             
>             helper.setTo(emailRequest.getTo());
>             helper.setSubject(emailRequest.getSubject());
>             helper.setText(emailRequest.getContent(), emailRequest.isHtml());
>             
>             mailSender.send(mimeMessage);
>             
>             log.info("Delayed email sent to: {}", emailRequest.getTo());
>             return CompletableFuture.completedFuture(null);
>             
>         } catch (Exception e) {
>             log.error("Failed to send delayed email", e);
>             return CompletableFuture.failedFuture(e);
>         }
>     }
> }
> ```
> 
> ## 12. REST Controller
> 
> ```java src/main/java/com/example/emailpatterns/controller/EmailController.java
> package com.example.emailpatterns.controller;
> 
> import com.example.emailpatterns.model.EmailAttachment;
> import com.example.emailpatterns.model.EmailRequest;
> import com.example.emailpatterns.service.*;
> import jakarta.validation.Valid;
> import lombok.RequiredArgsConstructor;
> import lombok.extern.slf4j.Slf4j;
> import org.springframework.http.ResponseEntity;
> import org.springframework.web.bind.annotation.*;
> import org.springframework.web.multipart.MultipartFile;
> 
> import java.io.IOException;
> import java.util.HashMap;
> import java.util.List;
> import java.util.Map;
> import java.util.concurrent.CompletableFuture;
> 
> @Slf4j
> @RestController
> @RequestMapping("/api/email")
> @RequiredArgsConstructor
> public class EmailController {
>     
>     private final MailSenderPatternService mailSenderService;
>     private final MimeMessagePatternService mimeMessageService;
>     private final TemplateEmailPatternService templateEmailService;
>     private final AttachmentHandlingPatternService attachmentService;
>     private final HtmlEmailPatternService htmlEmailService;
>     private final AsyncEmailPatternService asyncEmailService;
>     
>     /**
>      * Pattern 1: Mail Sender Pattern - Simple text email
>      */
>     @PostMapping("/simple")
>     public ResponseEntity<Map<String, String>> sendSimpleEmail(
>             @Valid @RequestBody EmailRequest emailRequest) {
>         
>         mailSenderService.sendSimpleEmail(emailRequest);
>         
>         return ResponseEntity.ok(Map.of(
>             "status", "success",
>             "message", "Simple email sent successfully",
>             "pattern", "Mail Sender Pattern"
>         ));
>     }
>     
>     /**
>      * Pattern 1: Mail Sender Pattern - Bulk simple emails
>      */
>     @PostMapping("/simple/bulk")
>     public ResponseEntity<Map<String, String>> sendBulkSimpleEmails(
>             @RequestBody List<EmailRequest> emailRequests) {
>         
>         mailSenderService.sendBulkSimpleEmails(
>             emailRequests.toArray(new EmailRequest[0])
>         );
>         
>         return ResponseEntity.ok(Map.of(
>             "status", "success",
>             "message", "Bulk emails sent: " + emailRequests.size(),
>             "pattern", "Mail Sender Pattern - Bulk"
>         ));
>     }
>     
>     /**
>      * Pattern 2: MIME Message Pattern - Advanced MIME email
>      */
>     @PostMapping("/mime")
>     public ResponseEntity<Map<String, String>> sendMimeEmail(
>             @Valid @RequestBody EmailRequest emailRequest) {
>         
>         mimeMessageService.sendMimeEmail(emailRequest);
>         
>         return ResponseEntity.ok(Map.of(
>             "status", "success",
>             "message", "MIME email sent successfully",
>             "pattern", "MIME Message Pattern"
>         ));
>     }
>     
>     /**
>      * Pattern 2: MIME Message Pattern - Multipart email
>      */
>     @PostMapping("/mime/multipart")
>     public ResponseEntity<Map<String, String>> sendMultipartEmail(
>             @Valid @RequestBody EmailRequest emailRequest) {
>         
>         mimeMessageService.sendMultipartEmail(emailRequest);
>         
>         return ResponseEntity.ok(Map.of(
>             "status", "success",
>             "message", "Multipart MIME email sent successfully",
>             "pattern", "MIME Message Pattern - Multipart"
>         ));
>     }
>     
>     /**
>      * Pattern 3: Template-based Email Pattern - Thymeleaf
>      */
>     @PostMapping("/template/thymeleaf")
>     public ResponseEntity<Map<String, String>> sendThymeleafEmail(
>             @RequestParam String to,
>             @RequestParam String templateName,
>             @RequestBody Map<String, Object> variables) {
>         
>         EmailRequest request = EmailRequest.builder()
>             .to(to)
>             .subject(variables.getOrDefault("subject", "Template Email").toString())
>             .templateName(templateName)
>             .templateVariables(variables)
>             .build();
>         
>         templateEmailService.sendThymeleafTemplateEmail(request);
>         
>         return ResponseEntity.ok(Map.of(
>             "status", "success",
>             "message", "Thymeleaf template email sent successfully",
>             "pattern", "Template-based Email Pattern - Thymeleaf"
>         ));
>     }
>     
>     /**
>      * Pattern 3: Template-based Email Pattern - FreeMarker
>      */
>     @PostMapping("/template/freemarker")
>     public ResponseEntity<Map<String, String>> sendFreeMarkerEmail(
>             @RequestParam String to,
>             @RequestParam String templateName,
>             @RequestBody Map<String, Object> variables) {
>         
>         EmailRequest request = EmailRequest.builder()
>             .to(to)
>             .subject(variables.getOrDefault("subject", "Template Email").toString())
>             .templateName(templateName)
>             .templateVariables(variables)
>             .build();
>         
>         templateEmailService.sendFreeMarkerTemplateEmail(request);
>         
>         return ResponseEntity.ok(Map.of(
>             "status", "success",
>             "message", "FreeMarker template email sent successfully",
>             "pattern", "Template-based Email Pattern - FreeMarker"
>         ));
>     }
>     
>     /**
>      * Pattern 4: Attachment Handling Pattern - With attachments
>      */
>     @PostMapping("/attachment")
>     public ResponseEntity<Map<String, String>> sendEmailWithAttachment(
>             @RequestParam String to,
>             @RequestParam String subject,
>             @RequestParam String content,
>             @RequestParam("files") List<MultipartFile> files) throws IOException {
>         
>         List<EmailAttachment> attachments = files.stream()
>             .map(file -> {
>                 try {
>                     return EmailAttachment.builder()
>                         .filename(file.getOriginalFilename())
>                         .content(file.getBytes())
>                         .contentType(file.getContentType())
>                         .build();
>                 } catch (IOException e) {
>                     throw new RuntimeException("Failed to process attachment", e);
>                 }
>             })
>             .toList();
>         
>         EmailRequest request = EmailRequest.builder()
>             .to(to)
>             .subject(subject)
>             .content(content)
>             .attachments(attachments)
>             .isHtml(true)
>             .build();
>         
>         attachmentService.sendEmailWithAttachments(request);
>         
>         return ResponseEntity.ok(Map.of(
>             "status", "success",
>             "message", "Email with attachments sent successfully",
>             "attachmentCount", String.valueOf(attachments.size()),
>             "pattern", "Attachment Handling Pattern"
>         ));
>     }
>     
>     /**
>      * Pattern 4: Attachment Handling Pattern - File system attachment
>      */
>     @PostMapping("/attachment/filesystem")
>     public ResponseEntity<Map<String, String>> sendEmailWithFileSystemAttachment(
>             @RequestParam String to,
>             @RequestParam String subject,
>             @RequestParam String content,
>             @RequestParam String filePath) {
>         
>         attachmentService.sendEmailWithFileSystemAttachment(to, subject, content, filePath);
>         
>         return ResponseEntity.ok(Map.of(
>             "status", "success",
>             "message", "Email with file system attachment sent successfully",
>             "pattern", "Attachment Handling Pattern - FileSystem"
>         ));
>     }
>     
>     /**
>      * Pattern 5: HTML Email Pattern - Rich HTML email
>      */
>     @PostMapping("/html")
>     public ResponseEntity<Map<String, String>> sendHtmlEmail(
>             @Valid @RequestBody EmailRequest emailRequest) {
>         
>         emailRequest.setHtml(true);
>         htmlEmailService.sendHtmlEmail(emailRequest);
>         
>         return ResponseEntity.ok(Map.of(
>             "status", "success",
>             "message", "HTML email sent successfully",
>             "pattern", "HTML Email Pattern"
>         ));
>     }
>     
>     /**
>      * Pattern 5: HTML Email Pattern - Rich formatted email
>      */
>     @PostMapping("/html/rich")
>     public ResponseEntity<Map<String, String>> sendRichHtmlEmail(
>             @RequestParam String to,
>             @RequestParam String subject,
>             @RequestParam String userName) {
>         
>         htmlEmailService.sendRichHtmlEmail(to, subject, userName);
>         
>         return ResponseEntity.ok(Map.of(
>             "status", "success",
>             "message", "Rich HTML email sent successfully",
>             "pattern", "HTML Email Pattern - Rich"
>         ));
>     }
>     
>     /**
>      * Pattern 5: HTML Email Pattern - Responsive email
>      */
>     @PostMapping("/html/responsive")
>     public ResponseEntity<Map<String, String>> sendResponsiveHtmlEmail(
>             @RequestParam String to,
>             @RequestParam String subject) {
>         
>         htmlEmailService.sendResponsiveHtmlEmail(to, subject);
>         
>         return ResponseEntity.ok(Map.of(
>             "status", "success",
>             "message", "Responsive HTML email sent successfully",
>             "pattern", "HTML Email Pattern - Responsive"
>         ));
>     }
>     
>     /**
>      * Pattern 6: Async Email Pattern - Asynchronous email
>      */
>     @PostMapping("/async")
>     public ResponseEntity<Map<String, String>> sendAsyncEmail(
>             @Valid @RequestBody EmailRequest emailRequest) {
>         
>         CompletableFuture<Boolean> future = asyncEmailService.sendEmailAsync(emailRequest);
>         
>         return ResponseEntity.accepted().body(Map.of(
>             "status", "accepted",
>             "message", "Email is being sent asynchronously",
>             "pattern", "Async Email Pattern"
>         ));
>     }
>     
>     /**
>      * Pattern 6: Async Email Pattern - Fire and forget
>      */
>     @PostMapping("/async/fire-and-forget")
>     public ResponseEntity<Map<String, String>> sendAsyncEmailFireAndForget(
>             @Valid @RequestBody EmailRequest emailRequest) {
>         
>         asyncEmailService.sendEmailAsyncFireAndForget(emailRequest);
>         
>         return ResponseEntity.accepted().body(Map.of(
>             "status", "accepted",
>             "message", "Email queued for sending (fire-and-forget)",
>             "pattern", "Async Email Pattern - Fire and Forget"
>         ));
>     }
>     
>     /**
>      * Pattern 6: Async Email Pattern - Bulk async emails
>      */
>     @PostMapping("/async/bulk")
>     public ResponseEntity<Map<String, String>> sendBulkAsyncEmails(
>             @RequestBody List<EmailRequest> emailRequests) {
>         
>         CompletableFuture<List<Boolean>> future = 
>             asyncEmailService.sendBulkEmailsAsync(emailRequests);
>         
>         return ResponseEntity.accepted().body(Map.of(
>             "status", "accepted",
>             "message", "Bulk emails are being sent asynchronously",
>             "count", String.valueOf(emailRequests.size()),
>             "pattern", "Async Email Pattern - Bulk"
>         ));
>     }
>     
>     /**
>      * Pattern 6: Async Email Pattern - Delayed email
>      */
>     @PostMapping("/async/delayed")
>     public ResponseEntity<Map<String, String>> sendDelayedEmail(
>             @Valid @RequestBody EmailRequest emailRequest,
>             @RequestParam(defaultValue = "5000") long delayMillis) {
>         
>         asyncEmailService.sendDelayedEmail(emailRequest, delayMillis);
>         
>         return ResponseEntity.accepted().body(Map.of(
>             "status", "accepted",
>             "message", "Email will be sent after delay",
>             "delayMillis", String.valueOf(delayMillis),
>             "pattern", "Async Email Pattern - Delayed"
>         ));
>     }
>     
>     /**
>      * Demo endpoint - Send all pattern examples
>      */
>     @PostMapping("/demo/all-patterns")
>     public ResponseEntity<Map<String, Object>> demonstrateAllPatterns(
>             @RequestParam String to) {
>         
>         Map<String, String> results = new HashMap<>();
>         
>         try {
>             // Pattern 1: Simple Email
>             EmailRequest simple = EmailRequest.builder()
>                 .to(to)
>                 .subject("Pattern 1: Simple Email")
>                 .content("This is a simple text email using Mail Sender Pattern")
>                 .build();
>             mailSenderService.sendSimpleEmail(simple);
>             results.put("pattern1", "Simple Email - Success");
>             
>             // Pattern 2: MIME Email
>             EmailRequest mime = EmailRequest.builder()
>                 .to(to)
>                 .subject("Pattern 2: MIME Email")
>                 .content("This is a MIME email with custom headers")
>                 .isHtml(false)
>                 .build();
>             mimeMessageService.sendMimeEmail(mime);
>             results.put("pattern2", "MIME Email - Success");
>             
>             // Pattern 3: Template Email
>             Map<String, Object> vars = Map.of(
>                 "subject", "Pattern 3: Template Email",
>                 "userName", "Demo User",
>                 "message", "This is a template-based email"
>             );
>             templateEmailService.sendDynamicTemplateEmail(to, "welcome-email", vars);
>             results.put("pattern3", "Template Email - Success");
>             
>             // Pattern 5: HTML Email
>             htmlEmailService.sendRichHtmlEmail(to, "Pattern 5: HTML Email", "Demo User");
>             results.put("pattern5", "HTML Email - Success");
>             
>             // Pattern 6: Async Email
>             EmailRequest async = EmailRequest.builder()
>                 .to(to)
>                 .subject("Pattern 6: Async Email")
>                 .content("<h1>This is an asynchronous email</h1>")
>                 .isHtml(true)
>                 .build();
>             asyncEmailService.sendEmailAsync(async);
>             results.put("pattern6", "Async Email - Queued");
>             
>         } catch (Exception e) {
>             log.error("Error in demo", e);
>             results.put("error", e.getMessage());
>         }
>         
>         return ResponseEntity.ok(Map.of(
>             "status", "completed",
>             "message", "All patterns demonstrated",
>             "results", results
>         ));
>     }
> }
> ```
> 
> ## 13. Email Templates
> 
> ```html src/main/resources/templates/welcome-email.html
> <!DOCTYPE html>
> <html xmlns:th="http://www.thymeleaf.org">
> <head>
>     <meta charset="UTF-8">
>     <meta name="viewport" content="width=device-width, initial-scale=1.0">
>     <title>Welcome Email</title>
>     <style>
>         body {
>             font-family: Arial, sans-serif;
>             line-height: 1.6;
>             color: #333;
>             max-width: 600px;
>             margin: 0 auto;
>             padding: 20px;
>         }
>         .header {
>             background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
>             color: white;
>             padding: 30px;
>             text-align: center;
>             border-radius: 10px 10px 0 0;
>         }
>         .content {
>             background-color: #ffffff;
>             padding: 30px;
>             border: 1px solid #e0e0e0;
>         }
>         .button {
>             display: inline-block;
>             padding: 12px 30px;
>             background-color: #667eea;
>             color: white;
>             text-decoration: none;
>             border-radius: 5px;
>             margin: 20px 0;
>         }
>         .footer {
>             background-color: #f8f9fa;
>             padding: 20px;
>             text-align: center;
>             font-size: 12px;
>             color: #666;
>             border-radius: 0 0 10px 10px;
>         }
>     </style>
> </head>
> <body>
>     <div class="header">
>         <h1>Welcome to Our Platform!</h1>
>     </div>
>     
>     <div class="content">
>         <h2 th:text="'Hello, ' + ${userName} + '!'">Hello, User!</h2>
>         
>         <p th:text="${message}">Thank you for joining us!</p>
>         
>         <p>We're thrilled to have you on board. Here's what you can do next:</p>
>         
>         <ul>
>             <li>Complete your profile</li>
>             <li>Explore our features</li>
>             <li>Connect with the community</li>
>         </ul>
>         
>         <center>
>             <a href="#" class="button" th:href="${actionUrl}">Get Started</a>
>         </center>
>         
>         <p>If you have any questions, our support team is here to help!</p>
>     </div>
>     
>     <div class="footer">
>         <p>&copy; 2024 Spring Email Patterns. All rights reserved.</p>
>         <p>You're receiving this email because you signed up for our service.</p>
>     </div>
> </body>
> </html>
> ```
> 
> ```html src/main/resources/templates/invoice-email.html
> <!DOCTYPE html>
> <html xmlns:th="http://www.thymeleaf.org">
> <head>
>     <meta charset="UTF-8">
>     <title>Invoice</title>
>     <style>
>         .invoice-box {
>             max-width: 800px;
>             margin: auto;
>             padding: 30px;
>             border: 1px solid #eee;
>             font-family: 'Helvetica Neue', 'Helvetica', Arial, sans-serif;
>         }
>         .invoice-box table {
>             width: 100%;
>             line-height: inherit;
>             text-align: left;
>         }
>         .invoice-box table td {
>             padding: 5px;
>             vertical-align: top;
>         }
>         .invoice-box table tr.heading td {
>             background: #eee;
>             border-bottom: 1px solid #ddd;
>             font-weight: bold;
>         }
>         .invoice-box table tr.item td {
>             border-bottom: 1px solid #eee;
>         }
>         .invoice-box table tr.total td:nth-child(2) {
>             border-top: 2px solid #eee;
>             font-weight: bold;
>         }
>     </style>
> </head>
> <body>
>     <div class="invoice-box">
>         <h2>INVOICE</h2>
>         
>         <table>
>             <tr>
>                 <td>
>                     <strong>Invoice #:</strong> <span th:text="${invoiceNumber}">INV-001</span><br>
>                     <strong>Date:</strong> <span th:text="${invoiceDate}">2024-01-01</span>
>                 </td>
>                 <td style="text-align: right;">
>                     <strong>Your Company</strong><br>
>                     123 Business St<br>
>                     City, State 12345
>                 </td>
>             </tr>
>         </table>
>         
>         <br>
>         
>         <table>
>             <tr class="heading">
>                 <td>Item</td>
>                 <td style="text-align: right;">Price</td>
>             </tr>
>             
>             <tr class="item" th:each="item : ${items}">
>                 <td th:text="${item.name}">Product Name</td>
>                 <td style="text-align: right;" th:text="'$' + ${item.price}">$100.00</td>
>             </tr>
>             
>             <tr class="total">
>                 <td></td>
>                 <td style="text-align: right;">
>                     Total: <span th:text="'$' + ${total}">$100.00</span>
>                 </td>
>             </tr>
>         </table>
>     </div>
> </body>
> </html>
> ```
> 
> ## 14. Testing the Application
> 
> ### cURL Examples:
> 
> ```bash
> # Pattern 1: Simple Email
> curl -X POST http://localhost:8080/api/email/simple \
>   -H "Content-Type: application/json" \
>   -d '{
>     "to": "user@example.com",
>     "subject": "Test Simple Email",
>     "content": "This is a test email using Mail Sender Pattern"
>   }'
> 
> # Pattern 2: MIME Email
> curl -X POST http://localhost:8080/api/email/mime \
>   -H "Content-Type: application/json" \
>   -d '{
>     "to": "user@example.com",
>     "subject": "Test MIME Email",
>     "content": "This is a MIME email with advanced features",
>     "isHtml": false
>   }'
> 
> # Pattern 5: HTML Email
> curl -X POST http://localhost:8080/api/email/html/rich \
>   -H "Content-Type: application/json" \
>   -d 'to=user@example.com&subject=Welcome&userName=John'
> 
> # Pattern 6: Async Email
> curl -X POST http://localhost:8080/api/email/async \
>   -H "Content-Type: application/json" \
>   -d '{
>     "to": "user@example.com",
>     "subject": "Async Email Test",
>     "content": "<h1>Asynchronous Email</h1>",
>     "isHtml": true
>   }'
> 
> # Demo All Patterns
> curl -X POST "http://localhost:8080/api/email/demo/all-patterns?to=user@example.com"
> ```
> 
> This comprehensive Spring Boot application demonstrates all six email integration patterns with production-ready code, proper error handling, and RESTful endpoints for testing each pattern!