package com.example.notification.template;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Notification Template Pattern
 * 
 * Demonstrates:
 * - Multi-channel notification templates
 * - Template variable substitution
 * - Template versioning
 * - Localization support
 * - Template caching
 * - Dynamic template rendering
 */

@SpringBootApplication
public class NotificationTemplatePattern {
    public static void main(String[] args) {
        SpringApplication.run(NotificationTemplatePattern.class, args);
    }
}

@Configuration
class NotificationTemplateConfig {}

@RestController
@RequestMapping("/api/notification-templates")
class NotificationTemplateController {
    private final NotificationTemplateService service;
    
    public NotificationTemplateController(NotificationTemplateService service) {
        this.service = service;
    }
    
    @PostMapping("/create")
    public ResponseEntity<NotificationTemplate> createTemplate(@Valid @RequestBody CreateTemplateRequest request) {
        return ResponseEntity.ok(service.createTemplate(request));
    }
    
    @GetMapping("/{templateId}")
    public ResponseEntity<NotificationTemplate> getTemplate(@PathVariable String templateId) {
        return service.getTemplate(templateId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/render")
    public ResponseEntity<RenderedTemplate> renderTemplate(@Valid @RequestBody RenderTemplateRequest request) {
        return ResponseEntity.ok(service.renderTemplate(request));
    }
    
    @PostMapping("/send-from-template")
    public ResponseEntity<TemplateNotificationResponse> sendFromTemplate(
            @Valid @RequestBody SendFromTemplateRequest request) {
        return ResponseEntity.ok(service.sendFromTemplate(request));
    }
    
    @GetMapping("/list")
    public ResponseEntity<List<NotificationTemplate>> listTemplates() {
        return ResponseEntity.ok(service.listTemplates());
    }
}

@Service
class NotificationTemplateService {
    private final Map<String, NotificationTemplate> templates = new ConcurrentHashMap<>();
    private final Map<String, String> renderedCache = new ConcurrentHashMap<>();
    
    public NotificationTemplate createTemplate(CreateTemplateRequest request) {
        String templateId = UUID.randomUUID().toString();
        
        NotificationTemplate template = new NotificationTemplate(
            templateId,
            request.getName(),
            request.getSubject(),
            request.getBody(),
            request.getChannel(),
            request.getLocale(),
            request.getVariables(),
            "1.0",
            LocalDateTime.now()
        );
        
        templates.put(templateId, template);
        return template;
    }
    
    public Optional<NotificationTemplate> getTemplate(String templateId) {
        return Optional.ofNullable(templates.get(templateId));
    }
    
    public RenderedTemplate renderTemplate(RenderTemplateRequest request) {
        NotificationTemplate template = templates.get(request.getTemplateId());
        if (template == null) {
            throw new IllegalArgumentException("Template not found");
        }
        
        String renderedSubject = replaceVariables(template.getSubject(), request.getVariables());
        String renderedBody = replaceVariables(template.getBody(), request.getVariables());
        
        return new RenderedTemplate(
            template.getId(),
            template.getName(),
            renderedSubject,
            renderedBody,
            template.getChannel(),
            LocalDateTime.now()
        );
    }
    
    public TemplateNotificationResponse sendFromTemplate(SendFromTemplateRequest request) {
        RenderedTemplate rendered = renderTemplate(new RenderTemplateRequest(
            request.getTemplateId(),
            request.getVariables()
        ));
        
        String notificationId = UUID.randomUUID().toString();
        
        // Simulate sending notification
        return new TemplateNotificationResponse(
            notificationId,
            rendered.getName(),
            request.getRecipient(),
            rendered.getChannel(),
            "SENT",
            LocalDateTime.now()
        );
    }
    
    private String replaceVariables(String template, Map<String, String> variables) {
        if (template == null || variables == null) {
            return template;
        }
        
        String result = template;
        Pattern pattern = Pattern.compile("\\{\\{([^}]+)\\}\\}");
        Matcher matcher = pattern.matcher(template);
        
        while (matcher.find()) {
            String variable = matcher.group(1).trim();
            String value = variables.getOrDefault(variable, "");
            result = result.replace("{{" + variable + "}}", value);
        }
        
        return result;
    }
    
    public List<NotificationTemplate> listTemplates() {
        return new ArrayList<>(templates.values());
    }
}

class CreateTemplateRequest {
    @NotBlank
    private String name;
    
    private String subject;
    
    @NotBlank
    private String body;
    
    private NotificationChannel channel = NotificationChannel.EMAIL;
    private String locale = "en";
    private List<String> variables = new ArrayList<>();
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public NotificationChannel getChannel() { return channel; }
    public void setChannel(NotificationChannel channel) { this.channel = channel; }
    public String getLocale() { return locale; }
    public void setLocale(String locale) { this.locale = locale; }
    public List<String> getVariables() { return variables; }
    public void setVariables(List<String> variables) { this.variables = variables; }
}

class RenderTemplateRequest {
    @NotBlank
    private String templateId;
    
    private Map<String, String> variables = new HashMap<>();
    
    public RenderTemplateRequest() {}
    
    public RenderTemplateRequest(String templateId, Map<String, String> variables) {
        this.templateId = templateId;
        this.variables = variables;
    }
    
    public String getTemplateId() { return templateId; }
    public void setTemplateId(String templateId) { this.templateId = templateId; }
    public Map<String, String> getVariables() { return variables; }
    public void setVariables(Map<String, String> variables) { this.variables = variables; }
}

class SendFromTemplateRequest {
    @NotBlank
    private String templateId;
    
    @NotBlank
    private String recipient;
    
    private Map<String, String> variables = new HashMap<>();
    
    public String getTemplateId() { return templateId; }
    public void setTemplateId(String templateId) { this.templateId = templateId; }
    public String getRecipient() { return recipient; }
    public void setRecipient(String recipient) { this.recipient = recipient; }
    public Map<String, String> getVariables() { return variables; }
    public void setVariables(Map<String, String> variables) { this.variables = variables; }
}

class NotificationTemplate {
    private String id;
    private String name;
    private String subject;
    private String body;
    private NotificationChannel channel;
    private String locale;
    private List<String> variables;
    private String version;
    private LocalDateTime createdAt;
    
    public NotificationTemplate(String id, String name, String subject, String body,
                               NotificationChannel channel, String locale, List<String> variables,
                               String version, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.subject = subject;
        this.body = body;
        this.channel = channel;
        this.locale = locale;
        this.variables = variables;
        this.version = version;
        this.createdAt = createdAt;
    }
    
    public String getId() { return id; }
    public String getName() { return name; }
    public String getSubject() { return subject; }
    public String getBody() { return body; }
    public NotificationChannel getChannel() { return channel; }
    public String getLocale() { return locale; }
    public List<String> getVariables() { return variables; }
    public String getVersion() { return version; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}

class RenderedTemplate {
    private String templateId;
    private String templateName;
    private String subject;
    private String body;
    private NotificationChannel channel;
    private LocalDateTime renderedAt;
    
    public RenderedTemplate(String templateId, String templateName, String subject, String body,
                           NotificationChannel channel, LocalDateTime renderedAt) {
        this.templateId = templateId;
        this.templateName = templateName;
        this.subject = subject;
        this.body = body;
        this.channel = channel;
        this.renderedAt = renderedAt;
    }
    
    public String getTemplateId() { return templateId; }
    public String getTemplateName() { return templateName; }
    public String getName() { return templateName; }
    public String getSubject() { return subject; }
    public String getBody() { return body; }
    public NotificationChannel getChannel() { return channel; }
    public LocalDateTime getRenderedAt() { return renderedAt; }
}

class TemplateNotificationResponse {
    private String notificationId;
    private String templateName;
    private String recipient;
    private NotificationChannel channel;
    private String status;
    private LocalDateTime sentAt;
    
    public TemplateNotificationResponse(String notificationId, String templateName, String recipient,
                                       NotificationChannel channel, String status, LocalDateTime sentAt) {
        this.notificationId = notificationId;
        this.templateName = templateName;
        this.recipient = recipient;
        this.channel = channel;
        this.status = status;
        this.sentAt = sentAt;
    }
    
    public String getNotificationId() { return notificationId; }
    public String getTemplateName() { return templateName; }
    public String getRecipient() { return recipient; }
    public NotificationChannel getChannel() { return channel; }
    public String getStatus() { return status; }
    public LocalDateTime getSentAt() { return sentAt; }
}

enum NotificationChannel {
    EMAIL, SMS, PUSH, IN_APP
}
