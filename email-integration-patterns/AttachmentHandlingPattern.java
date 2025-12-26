package com.example.email.attachment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
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
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Attachment Handling Pattern
 * 
 * Demonstrates:
 * - File attachment handling (single and multiple)
 * - Different attachment sources (byte array, file system, classpath)
 * - Attachment size validation
 * - File type validation
 * - Attachment compression (ZIP)
 * - Inline vs attachment disposition
 * - Content-ID for inline resources
 * - Large file handling
 * - Temporary file cleanup
 * 
 * Dependencies:
 * - spring-boot-starter-mail
 * - spring-boot-starter-web
 */

@SpringBootApplication
public class AttachmentHandlingPattern {
    public static void main(String[] args) {
        SpringApplication.run(AttachmentHandlingPattern.class, args);
    }
}

// ======================== Configuration ========================

@Configuration
@EnableConfigurationProperties(AttachmentMailProperties.class)
class AttachmentMailConfig {
    
    @Bean
    public JavaMailSender javaMailSender(AttachmentMailProperties properties) {
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
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.connectiontimeout", "10000");
        
        return mailSender;
    }
}

@ConfigurationProperties(prefix = "attachment.mail")
class AttachmentMailProperties {
    private String host = "smtp.gmail.com";
    private int port = 587;
    private String username;
    private String password;
    private String from = "noreply@example.com";
    private long maxAttachmentSize = 25 * 1024 * 1024; // 25MB
    private long maxTotalSize = 50 * 1024 * 1024; // 50MB
    private List<String> allowedExtensions = Arrays.asList(
        "pdf", "doc", "docx", "xls", "xlsx", "txt", "jpg", "jpeg", "png", "gif", "zip"
    );
    
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
    
    public long getMaxAttachmentSize() { return maxAttachmentSize; }
    public void setMaxAttachmentSize(long maxAttachmentSize) { 
        this.maxAttachmentSize = maxAttachmentSize; 
    }
    
    public long getMaxTotalSize() { return maxTotalSize; }
    public void setMaxTotalSize(long maxTotalSize) { this.maxTotalSize = maxTotalSize; }
    
    public List<String> getAllowedExtensions() { return allowedExtensions; }
    public void setAllowedExtensions(List<String> allowedExtensions) { 
        this.allowedExtensions = allowedExtensions; 
    }
}

// ======================== REST Controller ========================

@RestController
@RequestMapping("/api/attachment-email")
class AttachmentEmailController {
    
    private final AttachmentEmailService attachmentEmailService;
    
    public AttachmentEmailController(AttachmentEmailService attachmentEmailService) {
        this.attachmentEmailService = attachmentEmailService;
    }
    
    @PostMapping("/send-with-files")
    public ResponseEntity<AttachmentEmailResponse> sendEmailWithFiles(
            @RequestParam("to") String to,
            @RequestParam("subject") String subject,
            @RequestParam("body") String body,
            @RequestParam("files") List<MultipartFile> files) 
            throws MessagingException, IOException {
        
        AttachmentEmailRequest request = new AttachmentEmailRequest(
            to, subject, body, files
        );
        
        AttachmentEmailResponse response = attachmentEmailService.sendEmailWithAttachments(request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/send-from-filesystem")
    public ResponseEntity<AttachmentEmailResponse> sendEmailFromFileSystem(
            @RequestBody FileSystemAttachmentRequest request) 
            throws MessagingException, IOException {
        
        AttachmentEmailResponse response = attachmentEmailService.sendEmailFromFileSystem(request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/send-from-classpath")
    public ResponseEntity<AttachmentEmailResponse> sendEmailFromClasspath(
            @RequestBody ClasspathAttachmentRequest request) 
            throws MessagingException, IOException {
        
        AttachmentEmailResponse response = attachmentEmailService.sendEmailFromClasspath(request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/send-with-compression")
    public ResponseEntity<AttachmentEmailResponse> sendEmailWithCompression(
            @RequestParam("to") String to,
            @RequestParam("subject") String subject,
            @RequestParam("body") String body,
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(value = "zipName", defaultValue = "attachments.zip") String zipName) 
            throws MessagingException, IOException {
        
        AttachmentEmailResponse response = attachmentEmailService
            .sendEmailWithCompressedAttachments(to, subject, body, files, zipName);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/send-mixed-content")
    public ResponseEntity<AttachmentEmailResponse> sendMixedContentEmail(
            @RequestBody MixedContentRequest request) 
            throws MessagingException, IOException {
        
        AttachmentEmailResponse response = attachmentEmailService.sendMixedContentEmail(request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/validate-attachment")
    public ResponseEntity<AttachmentValidationResult> validateAttachment(
            @RequestParam("filename") String filename,
            @RequestParam("size") long size) {
        
        AttachmentValidationResult result = attachmentEmailService.validateAttachment(filename, size);
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/stats")
    public ResponseEntity<AttachmentStats> getStats() {
        return ResponseEntity.ok(attachmentEmailService.getStats());
    }
}

// ======================== Service Layer ========================

@Service
class AttachmentEmailService {
    
    private final JavaMailSender mailSender;
    private final AttachmentMailProperties properties;
    private final Map<String, AttachmentEmailHistory> history = new ConcurrentHashMap<>();
    private long totalAttachmentsSent = 0;
    private long totalSizeBytes = 0;
    
    public AttachmentEmailService(JavaMailSender mailSender, 
                                 AttachmentMailProperties properties) {
        this.mailSender = mailSender;
        this.properties = properties;
    }
    
    public AttachmentEmailResponse sendEmailWithAttachments(AttachmentEmailRequest request) 
            throws MessagingException, IOException {
        
        String emailId = UUID.randomUUID().toString();
        LocalDateTime sentAt = LocalDateTime.now();
        
        // Validate attachments
        validateAttachments(request.getAttachments());
        
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setFrom(properties.getFrom());
            helper.setTo(request.getTo());
            helper.setSubject(request.getSubject());
            helper.setText(request.getBody(), request.isHtml());
            
            // Add attachments
            long totalSize = 0;
            for (MultipartFile file : request.getAttachments()) {
                helper.addAttachment(
                    file.getOriginalFilename(),
                    new ByteArrayResource(file.getBytes()),
                    file.getContentType()
                );
                totalSize += file.getSize();
            }
            
            mailSender.send(mimeMessage);
            
            // Update statistics
            totalAttachmentsSent += request.getAttachments().size();
            totalSizeBytes += totalSize;
            
            // Record history
            recordHistory(emailId, request.getTo(), request.getSubject(), 
                         request.getAttachments().size(), totalSize, sentAt, 
                         AttachmentEmailStatus.SENT, null);
            
            return new AttachmentEmailResponse(emailId, AttachmentEmailStatus.SENT,
                "Email with " + request.getAttachments().size() + " attachment(s) sent successfully",
                sentAt, request.getAttachments().size(), totalSize);
            
        } catch (MessagingException | IOException e) {
            recordHistory(emailId, request.getTo(), request.getSubject(), 
                         request.getAttachments().size(), 0, sentAt,
                         AttachmentEmailStatus.FAILED, e.getMessage());
            throw e;
        }
    }
    
    public AttachmentEmailResponse sendEmailFromFileSystem(FileSystemAttachmentRequest request) 
            throws MessagingException, IOException {
        
        String emailId = UUID.randomUUID().toString();
        LocalDateTime sentAt = LocalDateTime.now();
        
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setFrom(properties.getFrom());
            helper.setTo(request.getTo());
            helper.setSubject(request.getSubject());
            helper.setText(request.getBody(), false);
            
            // Add file system attachments
            long totalSize = 0;
            for (String filePath : request.getFilePaths()) {
                File file = new File(filePath);
                if (file.exists() && file.isFile()) {
                    helper.addAttachment(file.getName(), new FileSystemResource(file));
                    totalSize += file.length();
                }
            }
            
            mailSender.send(mimeMessage);
            
            totalAttachmentsSent += request.getFilePaths().size();
            totalSizeBytes += totalSize;
            
            recordHistory(emailId, request.getTo(), request.getSubject(),
                         request.getFilePaths().size(), totalSize, sentAt,
                         AttachmentEmailStatus.SENT, null);
            
            return new AttachmentEmailResponse(emailId, AttachmentEmailStatus.SENT,
                "Email sent successfully", sentAt, request.getFilePaths().size(), totalSize);
            
        } catch (MessagingException e) {
            recordHistory(emailId, request.getTo(), request.getSubject(), 0, 0, sentAt,
                         AttachmentEmailStatus.FAILED, e.getMessage());
            throw e;
        }
    }
    
    public AttachmentEmailResponse sendEmailFromClasspath(ClasspathAttachmentRequest request) 
            throws MessagingException, IOException {
        
        String emailId = UUID.randomUUID().toString();
        LocalDateTime sentAt = LocalDateTime.now();
        
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setFrom(properties.getFrom());
            helper.setTo(request.getTo());
            helper.setSubject(request.getSubject());
            helper.setText(request.getBody(), false);
            
            // Add classpath resources
            int attachmentCount = 0;
            for (String resourcePath : request.getResourcePaths()) {
                ClassPathResource resource = new ClassPathResource(resourcePath);
                if (resource.exists()) {
                    helper.addAttachment(resource.getFilename(), resource);
                    attachmentCount++;
                }
            }
            
            mailSender.send(mimeMessage);
            
            totalAttachmentsSent += attachmentCount;
            
            recordHistory(emailId, request.getTo(), request.getSubject(),
                         attachmentCount, 0, sentAt, AttachmentEmailStatus.SENT, null);
            
            return new AttachmentEmailResponse(emailId, AttachmentEmailStatus.SENT,
                "Email sent successfully", sentAt, attachmentCount, 0);
            
        } catch (MessagingException e) {
            recordHistory(emailId, request.getTo(), request.getSubject(), 0, 0, sentAt,
                         AttachmentEmailStatus.FAILED, e.getMessage());
            throw e;
        }
    }
    
    public AttachmentEmailResponse sendEmailWithCompressedAttachments(
            String to, String subject, String body, List<MultipartFile> files, String zipName) 
            throws MessagingException, IOException {
        
        String emailId = UUID.randomUUID().toString();
        LocalDateTime sentAt = LocalDateTime.now();
        
        try {
            // Create ZIP file
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (ZipOutputStream zos = new ZipOutputStream(baos)) {
                for (MultipartFile file : files) {
                    ZipEntry entry = new ZipEntry(file.getOriginalFilename());
                    zos.putNextEntry(entry);
                    zos.write(file.getBytes());
                    zos.closeEntry();
                }
            }
            
            byte[] zipBytes = baos.toByteArray();
            
            // Send email with ZIP attachment
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setFrom(properties.getFrom());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, false);
            helper.addAttachment(zipName, new ByteArrayResource(zipBytes), "application/zip");
            
            mailSender.send(mimeMessage);
            
            totalAttachmentsSent++;
            totalSizeBytes += zipBytes.length;
            
            recordHistory(emailId, to, subject, 1, zipBytes.length, sentAt,
                         AttachmentEmailStatus.SENT, null);
            
            return new AttachmentEmailResponse(emailId, AttachmentEmailStatus.SENT,
                "Email with compressed attachments sent successfully", 
                sentAt, 1, zipBytes.length);
            
        } catch (MessagingException | IOException e) {
            recordHistory(emailId, to, subject, 0, 0, sentAt,
                         AttachmentEmailStatus.FAILED, e.getMessage());
            throw e;
        }
    }
    
    public AttachmentEmailResponse sendMixedContentEmail(MixedContentRequest request) 
            throws MessagingException, IOException {
        
        String emailId = UUID.randomUUID().toString();
        LocalDateTime sentAt = LocalDateTime.now();
        
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setFrom(properties.getFrom());
            helper.setTo(request.getTo());
            helper.setSubject(request.getSubject());
            helper.setText(request.getHtmlBody(), true);
            
            // Add regular attachments
            long totalSize = 0;
            int attachmentCount = 0;
            if (request.getAttachments() != null) {
                for (MultipartFile file : request.getAttachments()) {
                    helper.addAttachment(file.getOriginalFilename(),
                        new ByteArrayResource(file.getBytes()));
                    totalSize += file.getSize();
                    attachmentCount++;
                }
            }
            
            // Add inline resources (images)
            if (request.getInlineResources() != null) {
                int inlineIndex = 0;
                for (MultipartFile resource : request.getInlineResources()) {
                    String contentId = "inline" + inlineIndex++;
                    helper.addInline(contentId, new ByteArrayResource(resource.getBytes()),
                        resource.getContentType());
                }
            }
            
            mailSender.send(mimeMessage);
            
            totalAttachmentsSent += attachmentCount;
            totalSizeBytes += totalSize;
            
            recordHistory(emailId, request.getTo(), request.getSubject(),
                         attachmentCount, totalSize, sentAt, AttachmentEmailStatus.SENT, null);
            
            return new AttachmentEmailResponse(emailId, AttachmentEmailStatus.SENT,
                "Mixed content email sent successfully", sentAt, attachmentCount, totalSize);
            
        } catch (MessagingException | IOException e) {
            recordHistory(emailId, request.getTo(), request.getSubject(), 0, 0, sentAt,
                         AttachmentEmailStatus.FAILED, e.getMessage());
            throw e;
        }
    }
    
    public AttachmentValidationResult validateAttachment(String filename, long size) {
        List<String> errors = new ArrayList<>();
        
        // Check size
        if (size > properties.getMaxAttachmentSize()) {
            errors.add("File size exceeds maximum allowed size of " + 
                      formatBytes(properties.getMaxAttachmentSize()));
        }
        
        // Check extension
        String extension = getFileExtension(filename);
        if (!properties.getAllowedExtensions().contains(extension.toLowerCase())) {
            errors.add("File type '" + extension + "' is not allowed");
        }
        
        return new AttachmentValidationResult(
            errors.isEmpty(),
            filename,
            size,
            extension,
            errors
        );
    }
    
    private void validateAttachments(List<MultipartFile> attachments) throws IOException {
        long totalSize = 0;
        
        for (MultipartFile file : attachments) {
            // Validate individual file size
            if (file.getSize() > properties.getMaxAttachmentSize()) {
                throw new IOException("File '" + file.getOriginalFilename() + 
                    "' exceeds maximum size of " + formatBytes(properties.getMaxAttachmentSize()));
            }
            
            // Validate file extension
            String extension = getFileExtension(file.getOriginalFilename());
            if (!properties.getAllowedExtensions().contains(extension.toLowerCase())) {
                throw new IOException("File type '" + extension + "' is not allowed");
            }
            
            totalSize += file.getSize();
        }
        
        // Validate total size
        if (totalSize > properties.getMaxTotalSize()) {
            throw new IOException("Total attachment size exceeds maximum of " + 
                formatBytes(properties.getMaxTotalSize()));
        }
    }
    
    private String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        return lastDot > 0 ? filename.substring(lastDot + 1) : "";
    }
    
    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
    }
    
    private void recordHistory(String emailId, String to, String subject, int attachmentCount,
                              long totalSize, LocalDateTime sentAt, AttachmentEmailStatus status,
                              String error) {
        AttachmentEmailHistory record = new AttachmentEmailHistory(
            emailId, properties.getFrom(), to, subject, attachmentCount, totalSize,
            sentAt, status, error
        );
        history.put(emailId, record);
    }
    
    public AttachmentStats getStats() {
        return new AttachmentStats(
            totalAttachmentsSent,
            totalSizeBytes,
            history.size(),
            properties.getMaxAttachmentSize(),
            properties.getMaxTotalSize()
        );
    }
}

// ======================== Models ========================

class AttachmentEmailRequest {
    private String to;
    private String subject;
    private String body;
    private boolean html = false;
    private List<MultipartFile> attachments;
    
    public AttachmentEmailRequest() {}
    
    public AttachmentEmailRequest(String to, String subject, String body, 
                                 List<MultipartFile> attachments) {
        this.to = to;
        this.subject = subject;
        this.body = body;
        this.attachments = attachments;
    }
    
    // Getters and Setters
    public String getTo() { return to; }
    public void setTo(String to) { this.to = to; }
    
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

class FileSystemAttachmentRequest {
    @Email
    @NotBlank
    private String to;
    
    @NotBlank
    private String subject;
    
    @NotBlank
    private String body;
    
    private List<String> filePaths;
    
    // Getters and Setters
    public String getTo() { return to; }
    public void setTo(String to) { this.to = to; }
    
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    
    public List<String> getFilePaths() { return filePaths; }
    public void setFilePaths(List<String> filePaths) { this.filePaths = filePaths; }
}

class ClasspathAttachmentRequest {
    @Email
    @NotBlank
    private String to;
    
    @NotBlank
    private String subject;
    
    @NotBlank
    private String body;
    
    private List<String> resourcePaths;
    
    // Getters and Setters
    public String getTo() { return to; }
    public void setTo(String to) { this.to = to; }
    
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    
    public List<String> getResourcePaths() { return resourcePaths; }
    public void setResourcePaths(List<String> resourcePaths) { 
        this.resourcePaths = resourcePaths; 
    }
}

class MixedContentRequest {
    @Email
    @NotBlank
    private String to;
    
    @NotBlank
    private String subject;
    
    @NotBlank
    private String htmlBody;
    
    private List<MultipartFile> attachments;
    private List<MultipartFile> inlineResources;
    
    // Getters and Setters
    public String getTo() { return to; }
    public void setTo(String to) { this.to = to; }
    
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    
    public String getHtmlBody() { return htmlBody; }
    public void setHtmlBody(String htmlBody) { this.htmlBody = htmlBody; }
    
    public List<MultipartFile> getAttachments() { return attachments; }
    public void setAttachments(List<MultipartFile> attachments) { 
        this.attachments = attachments; 
    }
    
    public List<MultipartFile> getInlineResources() { return inlineResources; }
    public void setInlineResources(List<MultipartFile> inlineResources) { 
        this.inlineResources = inlineResources; 
    }
}

class AttachmentEmailResponse {
    private String emailId;
    private AttachmentEmailStatus status;
    private String message;
    private LocalDateTime timestamp;
    private int attachmentCount;
    private long totalSizeBytes;
    
    public AttachmentEmailResponse(String emailId, AttachmentEmailStatus status, String message,
                                  LocalDateTime timestamp, int attachmentCount, long totalSizeBytes) {
        this.emailId = emailId;
        this.status = status;
        this.message = message;
        this.timestamp = timestamp;
        this.attachmentCount = attachmentCount;
        this.totalSizeBytes = totalSizeBytes;
    }
    
    // Getters
    public String getEmailId() { return emailId; }
    public AttachmentEmailStatus getStatus() { return status; }
    public String getMessage() { return message; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public int getAttachmentCount() { return attachmentCount; }
    public long getTotalSizeBytes() { return totalSizeBytes; }
}

class AttachmentEmailHistory {
    private String emailId;
    private String from;
    private String to;
    private String subject;
    private int attachmentCount;
    private long totalSizeBytes;
    private LocalDateTime sentAt;
    private AttachmentEmailStatus status;
    private String errorMessage;
    
    public AttachmentEmailHistory(String emailId, String from, String to, String subject,
                                 int attachmentCount, long totalSizeBytes, LocalDateTime sentAt,
                                 AttachmentEmailStatus status, String errorMessage) {
        this.emailId = emailId;
        this.from = from;
        this.to = to;
        this.subject = subject;
        this.attachmentCount = attachmentCount;
        this.totalSizeBytes = totalSizeBytes;
        this.sentAt = sentAt;
        this.status = status;
        this.errorMessage = errorMessage;
    }
    
    // Getters
    public String getEmailId() { return emailId; }
    public String getFrom() { return from; }
    public String getTo() { return to; }
    public String getSubject() { return subject; }
    public int getAttachmentCount() { return attachmentCount; }
    public long getTotalSizeBytes() { return totalSizeBytes; }
    public LocalDateTime getSentAt() { return sentAt; }
    public AttachmentEmailStatus getStatus() { return status; }
    public String getErrorMessage() { return errorMessage; }
}

class AttachmentValidationResult {
    private boolean valid;
    private String filename;
    private long size;
    private String extension;
    private List<String> errors;
    
    public AttachmentValidationResult(boolean valid, String filename, long size,
                                     String extension, List<String> errors) {
        this.valid = valid;
        this.filename = filename;
        this.size = size;
        this.extension = extension;
        this.errors = errors;
    }
    
    // Getters
    public boolean isValid() { return valid; }
    public String getFilename() { return filename; }
    public long getSize() { return size; }
    public String getExtension() { return extension; }
    public List<String> getErrors() { return errors; }
}

class AttachmentStats {
    private long totalAttachmentsSent;
    private long totalSizeBytes;
    private long totalEmails;
    private long maxAttachmentSize;
    private long maxTotalSize;
    
    public AttachmentStats(long totalAttachmentsSent, long totalSizeBytes, long totalEmails,
                          long maxAttachmentSize, long maxTotalSize) {
        this.totalAttachmentsSent = totalAttachmentsSent;
        this.totalSizeBytes = totalSizeBytes;
        this.totalEmails = totalEmails;
        this.maxAttachmentSize = maxAttachmentSize;
        this.maxTotalSize = maxTotalSize;
    }
    
    // Getters
    public long getTotalAttachmentsSent() { return totalAttachmentsSent; }
    public long getTotalSizeBytes() { return totalSizeBytes; }
    public long getTotalEmails() { return totalEmails; }
    public long getMaxAttachmentSize() { return maxAttachmentSize; }
    public long getMaxTotalSize() { return maxTotalSize; }
    public double getAverageAttachmentsPerEmail() {
        return totalEmails == 0 ? 0 : (double) totalAttachmentsSent / totalEmails;
    }
}

enum AttachmentEmailStatus {
    SENT,
    FAILED,
    PENDING
}

/*
 * Usage Examples:
 * 
 * 1. Send Email with Files:
 * POST /api/attachment-email/send-with-files
 * Content-Type: multipart/form-data
 * to=recipient@example.com
 * subject=Invoice
 * body=Please find attached invoice
 * files=<invoice.pdf>
 * files=<receipt.pdf>
 * 
 * 2. Send Email from File System:
 * POST /api/attachment-email/send-from-filesystem
 * {
 *   "to": "recipient@example.com",
 *   "subject": "Reports",
 *   "body": "Monthly reports attached",
 *   "filePaths": ["/var/reports/monthly.pdf", "/var/reports/summary.xlsx"]
 * }
 * 
 * 3. Send Compressed Attachments:
 * POST /api/attachment-email/send-with-compression
 * Content-Type: multipart/form-data
 * to=recipient@example.com
 * subject=Documents
 * body=Documents compressed into ZIP
 * files=<doc1.pdf>
 * files=<doc2.docx>
 * zipName=documents.zip
 * 
 * 4. Validate Attachment:
 * GET /api/attachment-email/validate-attachment?filename=file.pdf&size=5242880
 */
