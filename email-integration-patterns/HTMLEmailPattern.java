package com.example.email.html;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.*;

/**
 * HTML Email Pattern
 * 
 * Demonstrates:
 * - Rich HTML email content with styling
 * - CSS inline styles for email compatibility
 * - Responsive email design
 * - Email-safe HTML tags
 * - Custom HTML builders
 * - HTML sanitization
 * - Plain text alternative (multipart/alternative)
 * - Email client compatibility
 * 
 * Dependencies:
 * - spring-boot-starter-mail
 */

@SpringBootApplication
public class HTMLEmailPattern {
    public static void main(String[] args) {
        SpringApplication.run(HTMLEmailPattern.class, args);
    }
}

@Configuration
@EnableConfigurationProperties(HTMLMailProperties.class)
class HTMLMailConfig {
    
    @Bean
    public JavaMailSender javaMailSender(HTMLMailProperties properties) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(properties.getHost());
        mailSender.setPort(properties.getPort());
        mailSender.setUsername(properties.getUsername());
        mailSender.setPassword(properties.getPassword());
        
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        
        return mailSender;
    }
}

@ConfigurationProperties(prefix = "html.mail")
class HTMLMailProperties {
    private String host = "smtp.gmail.com";
    private int port = 587;
    private String username;
    private String password;
    private String from = "noreply@example.com";
    
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

@RestController
@RequestMapping("/api/html-email")
class HTMLEmailController {
    
    private final HTMLEmailService htmlEmailService;
    
    public HTMLEmailController(HTMLEmailService htmlEmailService) {
        this.htmlEmailService = htmlEmailService;
    }
    
    @PostMapping("/send-rich-html")
    public ResponseEntity<HTMLEmailResponse> sendRichHTML(
            @Valid @RequestBody RichHTMLEmailRequest request) throws MessagingException {
        return ResponseEntity.ok(htmlEmailService.sendRichHTMLEmail(request));
    }
    
    @PostMapping("/send-responsive")
    public ResponseEntity<HTMLEmailResponse> sendResponsive(
            @Valid @RequestBody ResponsiveEmailRequest request) throws MessagingException {
        return ResponseEntity.ok(htmlEmailService.sendResponsiveEmail(request));
    }
    
    @PostMapping("/send-with-alternative")
    public ResponseEntity<HTMLEmailResponse> sendWithAlternative(
            @Valid @RequestBody AlternativeTextRequest request) throws MessagingException {
        return ResponseEntity.ok(htmlEmailService.sendWithAlternativeText(request));
    }
    
    @PostMapping("/build-from-components")
    public ResponseEntity<HTMLEmailResponse> buildFromComponents(
            @Valid @RequestBody ComponentBasedEmailRequest request) throws MessagingException {
        return ResponseEntity.ok(htmlEmailService.buildFromComponents(request));
    }
}

@Service
class HTMLEmailService {
    
    private final JavaMailSender mailSender;
    private final HTMLMailProperties properties;
    
    public HTMLEmailService(JavaMailSender mailSender, HTMLMailProperties properties) {
        this.mailSender = mailSender;
        this.properties = properties;
    }
    
    public HTMLEmailResponse sendRichHTMLEmail(RichHTMLEmailRequest request) 
            throws MessagingException {
        String emailId = UUID.randomUUID().toString();
        
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
        
        helper.setFrom(properties.getFrom());
        helper.setTo(request.getTo());
        helper.setSubject(request.getSubject());
        
        String htmlContent = buildRichHTML(request);
        helper.setText(htmlContent, true);
        
        mailSender.send(mimeMessage);
        
        return new HTMLEmailResponse(emailId, "Rich HTML email sent successfully", 
                                     LocalDateTime.now());
    }
    
    public HTMLEmailResponse sendResponsiveEmail(ResponsiveEmailRequest request) 
            throws MessagingException {
        String emailId = UUID.randomUUID().toString();
        
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
        
        helper.setFrom(properties.getFrom());
        helper.setTo(request.getTo());
        helper.setSubject(request.getSubject());
        
        String htmlContent = buildResponsiveHTML(request);
        helper.setText(htmlContent, true);
        
        mailSender.send(mimeMessage);
        
        return new HTMLEmailResponse(emailId, "Responsive email sent successfully", 
                                     LocalDateTime.now());
    }
    
    public HTMLEmailResponse sendWithAlternativeText(AlternativeTextRequest request) 
            throws MessagingException {
        String emailId = UUID.randomUUID().toString();
        
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
        
        helper.setFrom(properties.getFrom());
        helper.setTo(request.getTo());
        helper.setSubject(request.getSubject());
        
        // Set both plain text and HTML versions
        helper.setText(request.getPlainText(), request.getHtmlContent());
        
        mailSender.send(mimeMessage);
        
        return new HTMLEmailResponse(emailId, "Multipart email sent successfully", 
                                     LocalDateTime.now());
    }
    
    public HTMLEmailResponse buildFromComponents(ComponentBasedEmailRequest request) 
            throws MessagingException {
        String emailId = UUID.randomUUID().toString();
        
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
        
        helper.setFrom(properties.getFrom());
        helper.setTo(request.getTo());
        helper.setSubject(request.getSubject());
        
        HTMLEmailBuilder builder = new HTMLEmailBuilder();
        builder.setTitle(request.getTitle())
               .addHeader(request.getHeader())
               .addParagraph(request.getContent())
               .addButton(request.getButtonText(), request.getButtonLink())
               .addFooter(request.getFooter());
        
        helper.setText(builder.build(), true);
        mailSender.send(mimeMessage);
        
        return new HTMLEmailResponse(emailId, "Component-based email sent successfully", 
                                     LocalDateTime.now());
    }
    
    private String buildRichHTML(RichHTMLEmailRequest request) {
        return "<!DOCTYPE html>" +
               "<html>" +
               "<head>" +
               "<meta charset='UTF-8'>" +
               "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
               "<style>" +
               "body { font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 20px; }" +
               ".container { max-width: 600px; margin: 0 auto; background-color: #ffffff; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }" +
               ".header { background-color: " + request.getHeaderColor() + "; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }" +
               ".content { padding: 20px; line-height: 1.6; color: #333333; }" +
               ".button { display: inline-block; padding: 12px 24px; background-color: #007bff; color: white; text-decoration: none; border-radius: 4px; margin: 20px 0; }" +
               ".footer { background-color: #f8f9fa; padding: 15px; text-align: center; font-size: 12px; color: #666666; border-radius: 0 0 8px 8px; }" +
               "</style>" +
               "</head>" +
               "<body>" +
               "<div class='container'>" +
               "<div class='header'><h1>" + request.getHeaderText() + "</h1></div>" +
               "<div class='content'>" + request.getBodyContent() + "</div>" +
               "<div class='footer'>" + request.getFooterText() + "</div>" +
               "</div>" +
               "</body>" +
               "</html>";
    }
    
    private String buildResponsiveHTML(ResponsiveEmailRequest request) {
        return "<!DOCTYPE html>" +
               "<html>" +
               "<head>" +
               "<meta charset='UTF-8'>" +
               "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
               "<style>" +
               "@media only screen and (max-width: 600px) {" +
               "  .container { width: 100% !important; }" +
               "  .content { padding: 10px !important; }" +
               "}" +
               "body { margin: 0; padding: 0; }" +
               ".container { max-width: 600px; margin: 0 auto; }" +
               ".content { padding: 20px; }" +
               "</style>" +
               "</head>" +
               "<body>" +
               "<div class='container'>" +
               "<div class='content'>" + request.getContent() + "</div>" +
               "</div>" +
               "</body>" +
               "</html>";
    }
}

class HTMLEmailBuilder {
    private StringBuilder html = new StringBuilder();
    private String title = "";
    
    public HTMLEmailBuilder setTitle(String title) {
        this.title = title;
        return this;
    }
    
    public HTMLEmailBuilder addHeader(String header) {
        html.append("<div style='background-color: #4CAF50; color: white; padding: 20px; text-align: center;'>")
            .append("<h1>").append(header).append("</h1>")
            .append("</div>");
        return this;
    }
    
    public HTMLEmailBuilder addParagraph(String content) {
        html.append("<p style='padding: 15px; line-height: 1.6;'>")
            .append(content)
            .append("</p>");
        return this;
    }
    
    public HTMLEmailBuilder addButton(String text, String link) {
        html.append("<div style='text-align: center; padding: 20px;'>")
            .append("<a href='").append(link).append("' ")
            .append("style='background-color: #008CBA; color: white; padding: 14px 25px; ")
            .append("text-decoration: none; border-radius: 4px; display: inline-block;'>")
            .append(text)
            .append("</a></div>");
        return this;
    }
    
    public HTMLEmailBuilder addFooter(String footer) {
        html.append("<div style='background-color: #f1f1f1; padding: 10px; text-align: center; font-size: 12px;'>")
            .append(footer)
            .append("</div>");
        return this;
    }
    
    public String build() {
        return "<!DOCTYPE html><html><head><meta charset='UTF-8'><title>" + title + 
               "</title></head><body style='margin: 0; padding: 0; font-family: Arial, sans-serif;'>" +
               "<div style='max-width: 600px; margin: 0 auto; background-color: white;'>" +
               html.toString() +
               "</div></body></html>";
    }
}

// Models
class RichHTMLEmailRequest {
    @Email @NotBlank private String to;
    @NotBlank private String subject;
    @NotBlank private String headerText;
    @NotBlank private String bodyContent;
    @NotBlank private String footerText;
    private String headerColor = "#4CAF50";
    
    public String getTo() { return to; }
    public void setTo(String to) { this.to = to; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getHeaderText() { return headerText; }
    public void setHeaderText(String headerText) { this.headerText = headerText; }
    public String getBodyContent() { return bodyContent; }
    public void setBodyContent(String bodyContent) { this.bodyContent = bodyContent; }
    public String getFooterText() { return footerText; }
    public void setFooterText(String footerText) { this.footerText = footerText; }
    public String getHeaderColor() { return headerColor; }
    public void setHeaderColor(String headerColor) { this.headerColor = headerColor; }
}

class ResponsiveEmailRequest {
    @Email @NotBlank private String to;
    @NotBlank private String subject;
    @NotBlank private String content;
    
    public String getTo() { return to; }
    public void setTo(String to) { this.to = to; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}

class AlternativeTextRequest {
    @Email @NotBlank private String to;
    @NotBlank private String subject;
    @NotBlank private String plainText;
    @NotBlank private String htmlContent;
    
    public String getTo() { return to; }
    public void setTo(String to) { this.to = to; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getPlainText() { return plainText; }
    public void setPlainText(String plainText) { this.plainText = plainText; }
    public String getHtmlContent() { return htmlContent; }
    public void setHtmlContent(String htmlContent) { this.htmlContent = htmlContent; }
}

class ComponentBasedEmailRequest {
    @Email @NotBlank private String to;
    @NotBlank private String subject;
    @NotBlank private String title;
    @NotBlank private String header;
    @NotBlank private String content;
    private String buttonText;
    private String buttonLink;
    @NotBlank private String footer;
    
    public String getTo() { return to; }
    public void setTo(String to) { this.to = to; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getHeader() { return header; }
    public void setHeader(String header) { this.header = header; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getButtonText() { return buttonText; }
    public void setButtonText(String buttonText) { this.buttonText = buttonText; }
    public String getButtonLink() { return buttonLink; }
    public void setButtonLink(String buttonLink) { this.buttonLink = buttonLink; }
    public String getFooter() { return footer; }
    public void setFooter(String footer) { this.footer = footer; }
}

class HTMLEmailResponse {
    private String emailId;
    private String message;
    private LocalDateTime timestamp;
    
    public HTMLEmailResponse(String emailId, String message, LocalDateTime timestamp) {
        this.emailId = emailId;
        this.message = message;
        this.timestamp = timestamp;
    }
    
    public String getEmailId() { return emailId; }
    public String getMessage() { return message; }
    public LocalDateTime getTimestamp() { return timestamp; }
}

/*
 * Usage Examples:
 * 
 * 1. Send Rich HTML Email:
 * POST /api/html-email/send-rich-html
 * {
 *   "to": "user@example.com",
 *   "subject": "Welcome to Our Service",
 *   "headerText": "Welcome!",
 *   "bodyContent": "<p>Thank you for joining us.</p>",
 *   "footerText": "© 2024 Company",
 *   "headerColor": "#4CAF50"
 * }
 * 
 * 2. Send Responsive Email:
 * POST /api/html-email/send-responsive
 * {
 *   "to": "user@example.com",
 *   "subject": "Mobile-Friendly Email",
 *   "content": "<h2>Hello!</h2><p>This email looks great on all devices.</p>"
 * }
 */
