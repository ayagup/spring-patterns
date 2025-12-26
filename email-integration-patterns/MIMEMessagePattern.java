package com.example.email.mime;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MIME Message Pattern
 * 
 * Demonstrates:
 * - MIME (Multipurpose Internet Mail Extensions) message creation
 * - HTML email with embedded images
 * - File attachments (multiple types)
 * - Inline resources (images, CSS)
 * - MimeMessageHelper for complex email construction
 * - Content-Type handling (text/html, multipart/mixed)
 * - Character encoding (UTF-8)
 * - Email headers customization
 * 
 * Dependencies:
 * - spring-boot-starter-mail
 * - spring-boot-starter-web
 * - javax.mail-api
 */

@SpringBootApplication
public class MIMEMessagePattern {
    public static void main(String[] args) {
        SpringApplication.run(MIMEMessagePattern.class, args);
    }
}

// ======================== Configuration ========================

@Configuration
@EnableConfigurationProperties(MimeMailProperties.class)
class MimeMailConfig {
    
    @Bean
    public JavaMailSender javaMailSender(MimeMailProperties properties) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        
        mailSender.setHost(properties.getHost());
        mailSender.setPort(properties.getPort());
        mailSender.setUsername(properties.getUsername());
        mailSender.setPassword(properties.getPassword());
        mailSender.setDefaultEncoding("UTF-8");
        
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "false");
        props.put("mail.mime.charset", "UTF-8");
        
        return mailSender;
    }
}

@ConfigurationProperties(prefix = "mime.mail")
class MimeMailProperties {
    private String host = "smtp.gmail.com";
    private int port = 587;
    private String username;
    private String password;
    private String from = "noreply@example.com";
    
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
}

// ======================== REST Controller ========================

@RestController
@RequestMapping("/api/mime")
class MimeMessageController {
    
    private final MimeMessageService mimeMessageService;
    
    public MimeMessageController(MimeMessageService mimeMessageService) {
        this.mimeMessageService = mimeMessageService;
    }
    
    @PostMapping("/send-html")
    public ResponseEntity<MimeEmailResponse> sendHtmlEmail(
            @Valid @RequestBody HtmlEmailRequest request) throws MessagingException {
        MimeEmailResponse response = mimeMessageService.sendHtmlEmail(request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/send-with-attachment")
    public ResponseEntity<MimeEmailResponse> sendEmailWithAttachment(
            @RequestParam("to") String to,
            @RequestParam("subject") String subject,
            @RequestParam("body") String body,
            @RequestParam("files") List<MultipartFile> files) throws MessagingException, IOException {
        
        AttachmentEmailRequest request = new AttachmentEmailRequest(
            Arrays.asList(to.split(",")),
            subject,
            body,
            files
        );
        
        MimeEmailResponse response = mimeMessageService.sendEmailWithAttachments(request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/send-with-inline-image")
    public ResponseEntity<MimeEmailResponse> sendEmailWithInlineImage(
            @Valid @RequestBody InlineImageRequest request) throws MessagingException, IOException {
        MimeEmailResponse response = mimeMessageService.sendEmailWithInlineImage(request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/send-multipart")
    public ResponseEntity<MimeEmailResponse> sendMultipartEmail(
            @RequestParam("to") String to,
            @RequestParam("subject") String subject,
            @RequestParam("htmlBody") String htmlBody,
            @RequestParam(value = "files", required = false) List<MultipartFile> files,
            @RequestParam(value = "inlineImages", required = false) List<MultipartFile> inlineImages)
            throws MessagingException, IOException {
        
        MultipartEmailRequest request = new MultipartEmailRequest(
            Arrays.asList(to.split(",")),
            subject,
            htmlBody,
            files,
            inlineImages
        );
        
        MimeEmailResponse response = mimeMessageService.sendMultipartEmail(request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/history")
    public ResponseEntity<List<MimeEmailHistory>> getHistory() {
        return ResponseEntity.ok(mimeMessageService.getHistory());
    }
    
    @GetMapping("/stats")
    public ResponseEntity<MimeEmailStats> getStats() {
        return ResponseEntity.ok(mimeMessageService.getStats());
    }
}

// ======================== Service Layer ========================

@Service
class MimeMessageService {
    
    private final JavaMailSender mailSender;
    private final MimeMailProperties properties;
    private final Map<String, MimeEmailHistory> history = new ConcurrentHashMap<>();
    private int totalSent = 0;
    private int totalFailed = 0;
    
    public MimeMessageService(JavaMailSender mailSender, MimeMailProperties properties) {
        this.mailSender = mailSender;
        this.properties = properties;
    }
    
    public MimeEmailResponse sendHtmlEmail(HtmlEmailRequest request) throws MessagingException {
        String emailId = UUID.randomUUID().toString();
        LocalDateTime sentAt = LocalDateTime.now();
        
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setFrom(properties.getFrom());
            helper.setTo(request.getTo().toArray(new String[0]));
            helper.setSubject(request.getSubject());
            helper.setText(request.getHtmlBody(), true); // true = HTML
            
            if (request.getCc() != null && !request.getCc().isEmpty()) {
                helper.setCc(request.getCc().toArray(new String[0]));
            }
            
            mailSender.send(mimeMessage);
            totalSent++;
            
            recordHistory(emailId, request.getTo(), request.getSubject(), 
                         "HTML", sentAt, MimeEmailStatus.SENT, null);
            
            return new MimeEmailResponse(emailId, MimeEmailStatus.SENT, 
                                        "HTML email sent successfully", sentAt);
            
        } catch (MessagingException e) {
            totalFailed++;
            recordHistory(emailId, request.getTo(), request.getSubject(), 
                         "HTML", sentAt, MimeEmailStatus.FAILED, e.getMessage());
            throw e;
        }
    }
    
    public MimeEmailResponse sendEmailWithAttachments(AttachmentEmailRequest request) 
            throws MessagingException, IOException {
        String emailId = UUID.randomUUID().toString();
        LocalDateTime sentAt = LocalDateTime.now();
        
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setFrom(properties.getFrom());
            helper.setTo(request.getTo().toArray(new String[0]));
            helper.setSubject(request.getSubject());
            helper.setText(request.getBody(), request.isHtml());
            
            // Add attachments
            if (request.getAttachments() != null) {
                for (MultipartFile file : request.getAttachments()) {
                    helper.addAttachment(
                        file.getOriginalFilename(),
                        new ByteArrayResource(file.getBytes())
                    );
                }
            }
            
            mailSender.send(mimeMessage);
            totalSent++;
            
            recordHistory(emailId, request.getTo(), request.getSubject(), 
                         "ATTACHMENT", sentAt, MimeEmailStatus.SENT, null);
            
            return new MimeEmailResponse(emailId, MimeEmailStatus.SENT, 
                                        "Email with attachments sent successfully", sentAt);
            
        } catch (MessagingException | IOException e) {
            totalFailed++;
            recordHistory(emailId, request.getTo(), request.getSubject(), 
                         "ATTACHMENT", sentAt, MimeEmailStatus.FAILED, e.getMessage());
            throw e;
        }
    }
    
    public MimeEmailResponse sendEmailWithInlineImage(InlineImageRequest request) 
            throws MessagingException, IOException {
        String emailId = UUID.randomUUID().toString();
        LocalDateTime sentAt = LocalDateTime.now();
        
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setFrom(properties.getFrom());
            helper.setTo(request.getTo().toArray(new String[0]));
            helper.setSubject(request.getSubject());
            
            // HTML body with inline image reference
            String htmlBody = request.getHtmlBody();
            helper.setText(htmlBody, true);
            
            // Add inline images
            if (request.getImageContentId() != null && request.getImagePath() != null) {
                ClassPathResource image = new ClassPathResource(request.getImagePath());
                helper.addInline(request.getImageContentId(), image);
            }
            
            mailSender.send(mimeMessage);
            totalSent++;
            
            recordHistory(emailId, request.getTo(), request.getSubject(), 
                         "INLINE_IMAGE", sentAt, MimeEmailStatus.SENT, null);
            
            return new MimeEmailResponse(emailId, MimeEmailStatus.SENT, 
                                        "Email with inline image sent successfully", sentAt);
            
        } catch (MessagingException | IOException e) {
            totalFailed++;
            recordHistory(emailId, request.getTo(), request.getSubject(), 
                         "INLINE_IMAGE", sentAt, MimeEmailStatus.FAILED, e.getMessage());
            throw e;
        }
    }
    
    public MimeEmailResponse sendMultipartEmail(MultipartEmailRequest request) 
            throws MessagingException, IOException {
        String emailId = UUID.randomUUID().toString();
        LocalDateTime sentAt = LocalDateTime.now();
        
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setFrom(properties.getFrom());
            helper.setTo(request.getTo().toArray(new String[0]));
            helper.setSubject(request.getSubject());
            helper.setText(request.getHtmlBody(), true);
            
            // Add attachments
            if (request.getAttachments() != null) {
                for (MultipartFile file : request.getAttachments()) {
                    helper.addAttachment(
                        file.getOriginalFilename(),
                        new ByteArrayResource(file.getBytes())
                    );
                }
            }
            
            // Add inline images
            if (request.getInlineImages() != null) {
                int imageIndex = 0;
                for (MultipartFile image : request.getInlineImages()) {
                    String contentId = "image" + imageIndex++;
                    helper.addInline(contentId, new ByteArrayResource(image.getBytes()), 
                                   image.getContentType());
                }
            }
            
            mailSender.send(mimeMessage);
            totalSent++;
            
            recordHistory(emailId, request.getTo(), request.getSubject(), 
                         "MULTIPART", sentAt, MimeEmailStatus.SENT, null);
            
            return new MimeEmailResponse(emailId, MimeEmailStatus.SENT, 
                                        "Multipart email sent successfully", sentAt);
            
        } catch (MessagingException | IOException e) {
            totalFailed++;
            recordHistory(emailId, request.getTo(), request.getSubject(), 
                         "MULTIPART", sentAt, MimeEmailStatus.FAILED, e.getMessage());
            throw e;
        }
    }
    
    private void recordHistory(String emailId, List<String> to, String subject, 
                              String type, LocalDateTime sentAt, MimeEmailStatus status, 
                              String error) {
        MimeEmailHistory record = new MimeEmailHistory(
            emailId, properties.getFrom(), to, subject, type, sentAt, status, error
        );
        history.put(emailId, record);
    }
    
    public List<MimeEmailHistory> getHistory() {
        return new ArrayList<>(history.values());
    }
    
    public MimeEmailStats getStats() {
        return new MimeEmailStats(totalSent, totalFailed, history.size());
    }
}

// ======================== Models ========================

class HtmlEmailRequest {
    @Valid
    private List<@Email String> to;
    private List<@Email String> cc;
    
    @NotBlank
    private String subject;
    
    @NotBlank
    private String htmlBody;
    
    // Constructors
    public HtmlEmailRequest() {}
    
    // Getters and Setters
    public List<String> getTo() { return to; }
    public void setTo(List<String> to) { this.to = to; }
    
    public List<String> getCc() { return cc; }
    public void setCc(List<String> cc) { this.cc = cc; }
    
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    
    public String getHtmlBody() { return htmlBody; }
    public void setHtmlBody(String htmlBody) { this.htmlBody = htmlBody; }
}

class AttachmentEmailRequest {
    private List<String> to;
    private String subject;
    private String body;
    private boolean html = false;
    private List<MultipartFile> attachments;
    
    public AttachmentEmailRequest() {}
    
    public AttachmentEmailRequest(List<String> to, String subject, String body, 
                                 List<MultipartFile> attachments) {
        this.to = to;
        this.subject = subject;
        this.body = body;
        this.attachments = attachments;
    }
    
    // Getters and Setters
    public List<String> getTo() { return to; }
    public void setTo(List<String> to) { this.to = to; }
    
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    
    public boolean isHtml() { return html; }
    public void setHtml(boolean html) { this.html = html; }
    
    public List<MultipartFile> getAttachments() { return attachments; }
    public void setAttachments(List<MultipartFile> attachments) { 
        this.attachments = attachments; 
    }
}

class InlineImageRequest {
    @Valid
    private List<@Email String> to;
    
    @NotBlank
    private String subject;
    
    @NotBlank
    private String htmlBody; // Should contain <img src="cid:imageContentId">
    
    private String imageContentId; // e.g., "logo"
    private String imagePath; // e.g., "static/images/logo.png"
    
    // Getters and Setters
    public List<String> getTo() { return to; }
    public void setTo(List<String> to) { this.to = to; }
    
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    
    public String getHtmlBody() { return htmlBody; }
    public void setHtmlBody(String htmlBody) { this.htmlBody = htmlBody; }
    
    public String getImageContentId() { return imageContentId; }
    public void setImageContentId(String imageContentId) { 
        this.imageContentId = imageContentId; 
    }
    
    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
}

class MultipartEmailRequest {
    private List<String> to;
    private String subject;
    private String htmlBody;
    private List<MultipartFile> attachments;
    private List<MultipartFile> inlineImages;
    
    public MultipartEmailRequest() {}
    
    public MultipartEmailRequest(List<String> to, String subject, String htmlBody,
                                List<MultipartFile> attachments, List<MultipartFile> inlineImages) {
        this.to = to;
        this.subject = subject;
        this.htmlBody = htmlBody;
        this.attachments = attachments;
        this.inlineImages = inlineImages;
    }
    
    // Getters and Setters
    public List<String> getTo() { return to; }
    public void setTo(List<String> to) { this.to = to; }
    
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    
    public String getHtmlBody() { return htmlBody; }
    public void setHtmlBody(String htmlBody) { this.htmlBody = htmlBody; }
    
    public List<MultipartFile> getAttachments() { return attachments; }
    public void setAttachments(List<MultipartFile> attachments) { 
        this.attachments = attachments; 
    }
    
    public List<MultipartFile> getInlineImages() { return inlineImages; }
    public void setInlineImages(List<MultipartFile> inlineImages) { 
        this.inlineImages = inlineImages; 
    }
}

class MimeEmailResponse {
    private String emailId;
    private MimeEmailStatus status;
    private String message;
    private LocalDateTime timestamp;
    
    public MimeEmailResponse(String emailId, MimeEmailStatus status, 
                            String message, LocalDateTime timestamp) {
        this.emailId = emailId;
        this.status = status;
        this.message = message;
        this.timestamp = timestamp;
    }
    
    // Getters
    public String getEmailId() { return emailId; }
    public MimeEmailStatus getStatus() { return status; }
    public String getMessage() { return message; }
    public LocalDateTime getTimestamp() { return timestamp; }
}

class MimeEmailHistory {
    private String emailId;
    private String from;
    private List<String> to;
    private String subject;
    private String type; // HTML, ATTACHMENT, INLINE_IMAGE, MULTIPART
    private LocalDateTime sentAt;
    private MimeEmailStatus status;
    private String errorMessage;
    
    public MimeEmailHistory(String emailId, String from, List<String> to, String subject,
                           String type, LocalDateTime sentAt, MimeEmailStatus status, 
                           String errorMessage) {
        this.emailId = emailId;
        this.from = from;
        this.to = to;
        this.subject = subject;
        this.type = type;
        this.sentAt = sentAt;
        this.status = status;
        this.errorMessage = errorMessage;
    }
    
    // Getters
    public String getEmailId() { return emailId; }
    public String getFrom() { return from; }
    public List<String> getTo() { return to; }
    public String getSubject() { return subject; }
    public String getType() { return type; }
    public LocalDateTime getSentAt() { return sentAt; }
    public MimeEmailStatus getStatus() { return status; }
    public String getErrorMessage() { return errorMessage; }
}

class MimeEmailStats {
    private int totalSent;
    private int totalFailed;
    private int totalEmails;
    
    public MimeEmailStats(int totalSent, int totalFailed, int totalEmails) {
        this.totalSent = totalSent;
        this.totalFailed = totalFailed;
        this.totalEmails = totalEmails;
    }
    
    // Getters
    public int getTotalSent() { return totalSent; }
    public int getTotalFailed() { return totalFailed; }
    public int getTotalEmails() { return totalEmails; }
    public double getSuccessRate() {
        return totalEmails == 0 ? 0 : (double) totalSent / totalEmails * 100;
    }
}

enum MimeEmailStatus {
    SENT,
    FAILED,
    PENDING
}

/*
 * Usage Examples:
 * 
 * 1. Send HTML Email:
 * POST /api/mime/send-html
 * {
 *   "to": ["recipient@example.com"],
 *   "subject": "Welcome!",
 *   "htmlBody": "<html><body><h1>Welcome!</h1><p>Thank you for signing up.</p></body></html>"
 * }
 * 
 * 2. Send Email with Attachments:
 * POST /api/mime/send-with-attachment
 * Content-Type: multipart/form-data
 * to=recipient@example.com
 * subject=Invoice
 * body=Please find attached invoice
 * files=<file1.pdf>
 * files=<file2.xlsx>
 * 
 * 3. Send Email with Inline Image:
 * POST /api/mime/send-with-inline-image
 * {
 *   "to": ["recipient@example.com"],
 *   "subject": "Newsletter",
 *   "htmlBody": "<html><body><h1>Our Logo</h1><img src='cid:logo'/></body></html>",
 *   "imageContentId": "logo",
 *   "imagePath": "static/images/logo.png"
 * }
 * 
 * 4. Send Multipart Email (HTML + Attachments + Inline Images):
 * POST /api/mime/send-multipart
 * Content-Type: multipart/form-data
 * to=recipient@example.com
 * subject=Monthly Report
 * htmlBody=<html><body><img src='cid:image0'/><p>Report attached</p></body></html>
 * files=<report.pdf>
 * inlineImages=<chart.png>
 */
