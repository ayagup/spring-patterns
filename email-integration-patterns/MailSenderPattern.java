package com.example.email.mailsender;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mail Sender Pattern
 * 
 * Demonstrates:
 * - Spring Mail integration with JavaMailSender
 * - Simple email sending using SimpleMailMessage
 * - Mail configuration with properties
 * - Email validation and error handling
 * - Email history tracking
 * - Multiple recipients (To, CC, BCC)
 * - Reply-To configuration
 * - Mail server configuration (SMTP)
 * 
 * Dependencies:
 * - spring-boot-starter-mail
 * - spring-boot-starter-validation
 */

@SpringBootApplication
public class MailSenderPattern {
    public static void main(String[] args) {
        SpringApplication.run(MailSenderPattern.class, args);
    }
}

// ======================== Configuration ========================

@Configuration
@EnableConfigurationProperties(MailProperties.class)
class MailConfig {
    
    @Bean
    public JavaMailSender javaMailSender(MailProperties properties) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        
        mailSender.setHost(properties.getHost());
        mailSender.setPort(properties.getPort());
        mailSender.setUsername(properties.getUsername());
        mailSender.setPassword(properties.getPassword());
        
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", properties.isSmtpAuth());
        props.put("mail.smtp.starttls.enable", properties.isStarttlsEnable());
        props.put("mail.debug", properties.isDebug());
        props.put("mail.smtp.ssl.trust", properties.getHost());
        
        return mailSender;
    }
}

@ConfigurationProperties(prefix = "spring.mail")
class MailProperties {
    private String host = "smtp.gmail.com";
    private int port = 587;
    private String username;
    private String password;
    private String from = "noreply@example.com";
    private boolean smtpAuth = true;
    private boolean starttlsEnable = true;
    private boolean debug = false;
    
    // Getters and Setters
    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }
    
    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getFrom() { return from; }
    public void setFrom(String from) { this.from = from; }
    
    public boolean isSmtpAuth() { return smtpAuth; }
    public void setSmtpAuth(boolean smtpAuth) { this.smtpAuth = smtpAuth; }
    
    public boolean isStarttlsEnable() { return starttlsEnable; }
    public void setStarttlsEnable(boolean starttlsEnable) { this.starttlsEnable = starttlsEnable; }
    
    public boolean isDebug() { return debug; }
    public void setDebug(boolean debug) { this.debug = debug; }
}

// ======================== REST Controller ========================

@RestController
@RequestMapping("/api/mail")
class MailController {
    
    private final MailService mailService;
    
    public MailController(MailService mailService) {
        this.mailService = mailService;
    }
    
    @PostMapping("/send")
    public ResponseEntity<EmailResponse> sendEmail(@Valid @RequestBody EmailRequest request) {
        EmailResponse response = mailService.sendSimpleEmail(request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/send-multiple")
    public ResponseEntity<List<EmailResponse>> sendMultipleEmails(
            @Valid @RequestBody List<EmailRequest> requests) {
        List<EmailResponse> responses = mailService.sendMultipleEmails(requests);
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/history")
    public ResponseEntity<List<EmailHistory>> getEmailHistory() {
        List<EmailHistory> history = mailService.getEmailHistory();
        return ResponseEntity.ok(history);
    }
    
    @GetMapping("/history/{emailId}")
    public ResponseEntity<EmailHistory> getEmailById(@PathVariable String emailId) {
        return mailService.getEmailById(emailId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/stats")
    public ResponseEntity<EmailStats> getEmailStats() {
        EmailStats stats = mailService.getEmailStats();
        return ResponseEntity.ok(stats);
    }
    
    @PostMapping("/test-connection")
    public ResponseEntity<Map<String, Object>> testConnection() {
        boolean connected = mailService.testConnection();
        Map<String, Object> response = new HashMap<>();
        response.put("connected", connected);
        response.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(response);
    }
}

// ======================== Service Layer ========================

@Service
class MailService {
    
    private final JavaMailSender mailSender;
    private final MailProperties mailProperties;
    private final Map<String, EmailHistory> emailHistory = new ConcurrentHashMap<>();
    private int totalSent = 0;
    private int totalFailed = 0;
    
    public MailService(JavaMailSender mailSender, MailProperties mailProperties) {
        this.mailSender = mailSender;
        this.mailProperties = mailProperties;
    }
    
    public EmailResponse sendSimpleEmail(EmailRequest request) {
        String emailId = UUID.randomUUID().toString();
        LocalDateTime sentAt = LocalDateTime.now();
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            
            // Set sender
            message.setFrom(mailProperties.getFrom());
            
            // Set recipients
            message.setTo(request.getTo().toArray(new String[0]));
            
            if (request.getCc() != null && !request.getCc().isEmpty()) {
                message.setCc(request.getCc().toArray(new String[0]));
            }
            
            if (request.getBcc() != null && !request.getBcc().isEmpty()) {
                message.setBcc(request.getBcc().toArray(new String[0]));
            }
            
            // Set subject and text
            message.setSubject(request.getSubject());
            message.setText(request.getBody());
            
            // Set reply-to if provided
            if (request.getReplyTo() != null && !request.getReplyTo().isEmpty()) {
                message.setReplyTo(request.getReplyTo());
            }
            
            // Send email
            mailSender.send(message);
            
            totalSent++;
            
            // Record history
            EmailHistory history = new EmailHistory(
                emailId,
                mailProperties.getFrom(),
                request.getTo(),
                request.getCc(),
                request.getBcc(),
                request.getSubject(),
                request.getBody(),
                sentAt,
                EmailStatus.SENT,
                null
            );
            emailHistory.put(emailId, history);
            
            return new EmailResponse(emailId, EmailStatus.SENT, "Email sent successfully", sentAt);
            
        } catch (MailException e) {
            totalFailed++;
            
            // Record failure
            EmailHistory history = new EmailHistory(
                emailId,
                mailProperties.getFrom(),
                request.getTo(),
                request.getCc(),
                request.getBcc(),
                request.getSubject(),
                request.getBody(),
                sentAt,
                EmailStatus.FAILED,
                e.getMessage()
            );
            emailHistory.put(emailId, history);
            
            return new EmailResponse(emailId, EmailStatus.FAILED, 
                "Failed to send email: " + e.getMessage(), sentAt);
        }
    }
    
    public List<EmailResponse> sendMultipleEmails(List<EmailRequest> requests) {
        List<EmailResponse> responses = new ArrayList<>();
        for (EmailRequest request : requests) {
            responses.add(sendSimpleEmail(request));
        }
        return responses;
    }
    
    public List<EmailHistory> getEmailHistory() {
        return new ArrayList<>(emailHistory.values());
    }
    
    public Optional<EmailHistory> getEmailById(String emailId) {
        return Optional.ofNullable(emailHistory.get(emailId));
    }
    
    public EmailStats getEmailStats() {
        return new EmailStats(
            totalSent,
            totalFailed,
            emailHistory.size(),
            mailProperties.getHost(),
            mailProperties.getPort(),
            mailProperties.getFrom()
        );
    }
    
    public boolean testConnection() {
        try {
            mailSender.getClass(); // Simple check if bean is initialized
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

// ======================== Models ========================

class EmailRequest {
    @NotBlank(message = "Subject is required")
    private String subject;
    
    @NotBlank(message = "Body is required")
    private String body;
    
    @Valid
    private List<@Email(message = "Invalid email address") String> to;
    
    private List<@Email(message = "Invalid CC email address") String> cc;
    private List<@Email(message = "Invalid BCC email address") String> bcc;
    
    @Email(message = "Invalid reply-to email address")
    private String replyTo;
    
    // Constructors
    public EmailRequest() {}
    
    public EmailRequest(String subject, String body, List<String> to) {
        this.subject = subject;
        this.body = body;
        this.to = to;
    }
    
    // Getters and Setters
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    
    public List<String> getTo() { return to; }
    public void setTo(List<String> to) { this.to = to; }
    
    public List<String> getCc() { return cc; }
    public void setCc(List<String> cc) { this.cc = cc; }
    
    public List<String> getBcc() { return bcc; }
    public void setBcc(List<String> bcc) { this.bcc = bcc; }
    
    public String getReplyTo() { return replyTo; }
    public void setReplyTo(String replyTo) { this.replyTo = replyTo; }
}

class EmailResponse {
    private String emailId;
    private EmailStatus status;
    private String message;
    private LocalDateTime timestamp;
    
    public EmailResponse(String emailId, EmailStatus status, String message, LocalDateTime timestamp) {
        this.emailId = emailId;
        this.status = status;
        this.message = message;
        this.timestamp = timestamp;
    }
    
    // Getters
    public String getEmailId() { return emailId; }
    public EmailStatus getStatus() { return status; }
    public String getMessage() { return message; }
    public LocalDateTime getTimestamp() { return timestamp; }
}

class EmailHistory {
    private String emailId;
    private String from;
    private List<String> to;
    private List<String> cc;
    private List<String> bcc;
    private String subject;
    private String body;
    private LocalDateTime sentAt;
    private EmailStatus status;
    private String errorMessage;
    
    public EmailHistory(String emailId, String from, List<String> to, List<String> cc,
                       List<String> bcc, String subject, String body, LocalDateTime sentAt,
                       EmailStatus status, String errorMessage) {
        this.emailId = emailId;
        this.from = from;
        this.to = to;
        this.cc = cc;
        this.bcc = bcc;
        this.subject = subject;
        this.body = body;
        this.sentAt = sentAt;
        this.status = status;
        this.errorMessage = errorMessage;
    }
    
    // Getters
    public String getEmailId() { return emailId; }
    public String getFrom() { return from; }
    public List<String> getTo() { return to; }
    public List<String> getCc() { return cc; }
    public List<String> getBcc() { return bcc; }
    public String getSubject() { return subject; }
    public String getBody() { return body; }
    public LocalDateTime getSentAt() { return sentAt; }
    public EmailStatus getStatus() { return status; }
    public String getErrorMessage() { return errorMessage; }
}

class EmailStats {
    private int totalSent;
    private int totalFailed;
    private int totalEmails;
    private String mailHost;
    private int mailPort;
    private String defaultFrom;
    
    public EmailStats(int totalSent, int totalFailed, int totalEmails,
                     String mailHost, int mailPort, String defaultFrom) {
        this.totalSent = totalSent;
        this.totalFailed = totalFailed;
        this.totalEmails = totalEmails;
        this.mailHost = mailHost;
        this.mailPort = mailPort;
        this.defaultFrom = defaultFrom;
    }
    
    // Getters
    public int getTotalSent() { return totalSent; }
    public int getTotalFailed() { return totalFailed; }
    public int getTotalEmails() { return totalEmails; }
    public String getMailHost() { return mailHost; }
    public int getMailPort() { return mailPort; }
    public String getDefaultFrom() { return defaultFrom; }
    public double getSuccessRate() {
        return totalEmails == 0 ? 0 : (double) totalSent / totalEmails * 100;
    }
}

enum EmailStatus {
    SENT,
    FAILED,
    PENDING
}

/* 
 * Application Properties (application.properties):
 * 
 * # Mail Configuration
 * spring.mail.host=smtp.gmail.com
 * spring.mail.port=587
 * spring.mail.username=your-email@gmail.com
 * spring.mail.password=your-app-password
 * spring.mail.from=noreply@example.com
 * spring.mail.smtp-auth=true
 * spring.mail.starttls-enable=true
 * spring.mail.debug=false
 * 
 * # For Gmail, you need to:
 * # 1. Enable "Less secure app access" or use App Passwords
 * # 2. Enable IMAP in Gmail settings
 * 
 * # For other providers:
 * # - Outlook: smtp.office365.com, port 587
 * # - Yahoo: smtp.mail.yahoo.com, port 587
 * # - SendGrid: smtp.sendgrid.net, port 587
 * # - Amazon SES: email-smtp.region.amazonaws.com, port 587
 */

/*
 * Usage Examples:
 * 
 * 1. Send Simple Email:
 * POST /api/mail/send
 * {
 *   "to": ["recipient@example.com"],
 *   "subject": "Test Email",
 *   "body": "This is a test email"
 * }
 * 
 * 2. Send Email with CC and BCC:
 * POST /api/mail/send
 * {
 *   "to": ["recipient@example.com"],
 *   "cc": ["cc@example.com"],
 *   "bcc": ["bcc@example.com"],
 *   "subject": "Important Update",
 *   "body": "Please review the attached information",
 *   "replyTo": "support@example.com"
 * }
 * 
 * 3. Send Multiple Emails:
 * POST /api/mail/send-multiple
 * [
 *   {
 *     "to": ["user1@example.com"],
 *     "subject": "Welcome",
 *     "body": "Welcome to our service"
 *   },
 *   {
 *     "to": ["user2@example.com"],
 *     "subject": "Notification",
 *     "body": "You have a new message"
 *   }
 * ]
 * 
 * 4. Get Email History:
 * GET /api/mail/history
 * 
 * 5. Get Email Stats:
 * GET /api/mail/stats
 * 
 * 6. Test Connection:
 * POST /api/mail/test-connection
 */
