package com.example.notification.email;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import javax.mail.internet.MimeMessage;
import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Email Notification Pattern
 * 
 * Unified email notification service that acts as a facade/wrapper
 * for various email sending scenarios.
 * 
 * Demonstrates:
 * - Email notification service
 * - Priority-based email handling
 * - Email notification templates
 * - Recipient grouping
 * - Email notification tracking
 * - Multi-template support
 */

@SpringBootApplication
public class EmailNotificationPattern {
    public static void main(String[] args) {
        SpringApplication.run(EmailNotificationPattern.class, args);
    }
}

@Configuration
class EmailNotificationConfig {}

@RestController
@RequestMapping("/api/email-notifications")
class EmailNotificationController {
    private final EmailNotificationService service;
    
    public EmailNotificationController(EmailNotificationService service) {
        this.service = service;
    }
    
    @PostMapping("/send")
    public ResponseEntity<EmailNotificationResponse> send(@Valid @RequestBody EmailNotificationRequest request) {
        return ResponseEntity.ok(service.sendNotification(request));
    }
    
    @PostMapping("/send-group")
    public ResponseEntity<GroupEmailResponse> sendToGroup(@Valid @RequestBody GroupEmailRequest request) {
        return ResponseEntity.ok(service.sendToGroup(request));
    }
    
    @GetMapping("/history")
    public ResponseEntity<List<EmailNotificationHistory>> getHistory() {
        return ResponseEntity.ok(service.getHistory());
    }
}

@Service
class EmailNotificationService {
    private final JavaMailSender mailSender;
    private final List<EmailNotificationHistory> history = Collections.synchronizedList(new ArrayList<>());
    private int totalSent = 0;
    
    public EmailNotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }
    
    public EmailNotificationResponse sendNotification(EmailNotificationRequest request) {
        String notificationId = UUID.randomUUID().toString();
        
        try {
            if (request.isHtml()) {
                sendHtmlNotification(request);
            } else {
                sendPlainTextNotification(request);
            }
            
            totalSent++;
            history.add(new EmailNotificationHistory(notificationId, request.getTo(), 
                request.getSubject(), "SENT", LocalDateTime.now()));
            
            return new EmailNotificationResponse(notificationId, "Notification sent", "SUCCESS");
        } catch (Exception e) {
            history.add(new EmailNotificationHistory(notificationId, request.getTo(), 
                request.getSubject(), "FAILED", LocalDateTime.now()));
            return new EmailNotificationResponse(notificationId, "Failed: " + e.getMessage(), "FAILED");
        }
    }
    
    private void sendPlainTextNotification(EmailNotificationRequest request) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(request.getTo());
        message.setSubject(request.getSubject());
        message.setText(request.getBody());
        mailSender.send(message);
    }
    
    private void sendHtmlNotification(EmailNotificationRequest request) throws Exception {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
        helper.setTo(request.getTo());
        helper.setSubject(request.getSubject());
        helper.setText(request.getBody(), true);
        mailSender.send(mimeMessage);
    }
    
    public GroupEmailResponse sendToGroup(GroupEmailRequest request) {
        List<EmailNotificationResponse> responses = new ArrayList<>();
        for (String recipient : request.getRecipients()) {
            EmailNotificationRequest emailRequest = new EmailNotificationRequest();
            emailRequest.setTo(recipient);
            emailRequest.setSubject(request.getSubject());
            emailRequest.setBody(request.getBody());
            emailRequest.setHtml(request.isHtml());
            responses.add(sendNotification(emailRequest));
        }
        return new GroupEmailResponse(responses, responses.size());
    }
    
    public List<EmailNotificationHistory> getHistory() {
        return new ArrayList<>(history);
    }
}

class EmailNotificationRequest {
    @NotBlank
    @Email
    private String to;
    
    @NotBlank
    private String subject;
    
    @NotBlank
    private String body;
    
    private boolean html = false;
    private NotificationPriority priority = NotificationPriority.NORMAL;
    
    public String getTo() { return to; }
    public void setTo(String to) { this.to = to; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public boolean isHtml() { return html; }
    public void setHtml(boolean html) { this.html = html; }
    public NotificationPriority getPriority() { return priority; }
    public void setPriority(NotificationPriority priority) { this.priority = priority; }
}

class GroupEmailRequest {
    private List<String> recipients;
    private String subject;
    private String body;
    private boolean html = false;
    
    public List<String> getRecipients() { return recipients; }
    public void setRecipients(List<String> recipients) { this.recipients = recipients; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public boolean isHtml() { return html; }
    public void setHtml(boolean html) { this.html = html; }
}

class EmailNotificationResponse {
    private String notificationId;
    private String message;
    private String status;
    
    public EmailNotificationResponse(String notificationId, String message, String status) {
        this.notificationId = notificationId;
        this.message = message;
        this.status = status;
    }
    
    public String getNotificationId() { return notificationId; }
    public String getMessage() { return message; }
    public String getStatus() { return status; }
}

class GroupEmailResponse {
    private List<EmailNotificationResponse> responses;
    private int totalSent;
    
    public GroupEmailResponse(List<EmailNotificationResponse> responses, int totalSent) {
        this.responses = responses;
        this.totalSent = totalSent;
    }
    
    public List<EmailNotificationResponse> getResponses() { return responses; }
    public int getTotalSent() { return totalSent; }
}

class EmailNotificationHistory {
    private String notificationId;
    private String recipient;
    private String subject;
    private String status;
    private LocalDateTime sentAt;
    
    public EmailNotificationHistory(String notificationId, String recipient, String subject, 
                                   String status, LocalDateTime sentAt) {
        this.notificationId = notificationId;
        this.recipient = recipient;
        this.subject = subject;
        this.status = status;
        this.sentAt = sentAt;
    }
    
    public String getNotificationId() { return notificationId; }
    public String getRecipient() { return recipient; }
    public String getSubject() { return subject; }
    public String getStatus() { return status; }
    public LocalDateTime getSentAt() { return sentAt; }
}

enum NotificationPriority {
    LOW, NORMAL, HIGH, URGENT
}
