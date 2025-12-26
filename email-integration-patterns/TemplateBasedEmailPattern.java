package com.example.email.template;

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
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Template-based Email Pattern
 * 
 * Demonstrates:
 * - Email template engines (Thymeleaf, FreeMarker, Velocity)
 * - Dynamic content generation from templates
 * - Template variable substitution
 * - Reusable email templates
 * - Template caching for performance
 * - Localization support in templates
 * - Template fragments and layouts
 * - HTML email generation from templates
 * 
 * Dependencies:
 * - spring-boot-starter-mail
 * - spring-boot-starter-thymeleaf
 * - spring-boot-starter-freemarker (optional)
 */

@SpringBootApplication
public class TemplateBasedEmailPattern {
    public static void main(String[] args) {
        SpringApplication.run(TemplateBasedEmailPattern.class, args);
    }
}

// ======================== Configuration ========================

@Configuration
@EnableConfigurationProperties(TemplateMailProperties.class)
class TemplateMailConfig {
    
    @Bean
    public JavaMailSender javaMailSender(TemplateMailProperties properties) {
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
        
        return mailSender;
    }
    
    @Bean
    public TemplateEngine emailTemplateEngine() {
        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.addTemplateResolver(htmlTemplateResolver());
        templateEngine.addTemplateResolver(textTemplateResolver());
        return templateEngine;
    }
    
    private ITemplateResolver htmlTemplateResolver() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("/templates/email/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(true); // Enable caching
        resolver.setOrder(1);
        return resolver;
    }
    
    private ITemplateResolver textTemplateResolver() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("/templates/email/");
        resolver.setSuffix(".txt");
        resolver.setTemplateMode(TemplateMode.TEXT);
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(true);
        resolver.setOrder(2);
        return resolver;
    }
}

@ConfigurationProperties(prefix = "template.mail")
class TemplateMailProperties {
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
@RequestMapping("/api/template-email")
class TemplateEmailController {
    
    private final TemplateEmailService templateEmailService;
    
    public TemplateEmailController(TemplateEmailService templateEmailService) {
        this.templateEmailService = templateEmailService;
    }
    
    @PostMapping("/welcome")
    public ResponseEntity<TemplateEmailResponse> sendWelcomeEmail(
            @Valid @RequestBody WelcomeEmailRequest request) throws MessagingException {
        TemplateEmailResponse response = templateEmailService.sendWelcomeEmail(request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/password-reset")
    public ResponseEntity<TemplateEmailResponse> sendPasswordResetEmail(
            @Valid @RequestBody PasswordResetRequest request) throws MessagingException {
        TemplateEmailResponse response = templateEmailService.sendPasswordResetEmail(request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/order-confirmation")
    public ResponseEntity<TemplateEmailResponse> sendOrderConfirmation(
            @Valid @RequestBody OrderConfirmationRequest request) throws MessagingException {
        TemplateEmailResponse response = templateEmailService.sendOrderConfirmation(request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/newsletter")
    public ResponseEntity<TemplateEmailResponse> sendNewsletter(
            @Valid @RequestBody NewsletterRequest request) throws MessagingException {
        TemplateEmailResponse response = templateEmailService.sendNewsletter(request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/custom-template")
    public ResponseEntity<TemplateEmailResponse> sendCustomTemplateEmail(
            @Valid @RequestBody CustomTemplateRequest request) throws MessagingException {
        TemplateEmailResponse response = templateEmailService.sendCustomTemplateEmail(request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/templates")
    public ResponseEntity<List<EmailTemplate>> getAvailableTemplates() {
        return ResponseEntity.ok(templateEmailService.getAvailableTemplates());
    }
    
    @GetMapping("/history")
    public ResponseEntity<List<TemplateEmailHistory>> getHistory() {
        return ResponseEntity.ok(templateEmailService.getHistory());
    }
}

// ======================== Service Layer ========================

@Service
class TemplateEmailService {
    
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final TemplateMailProperties properties;
    private final Map<String, TemplateEmailHistory> history = new ConcurrentHashMap<>();
    
    public TemplateEmailService(JavaMailSender mailSender, TemplateEngine templateEngine,
                               TemplateMailProperties properties) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.properties = properties;
    }
    
    public TemplateEmailResponse sendWelcomeEmail(WelcomeEmailRequest request) 
            throws MessagingException {
        Context context = new Context();
        context.setVariable("userName", request.getUserName());
        context.setVariable("email", request.getEmail());
        context.setVariable("activationLink", request.getActivationLink());
        context.setVariable("year", LocalDateTime.now().getYear());
        
        return sendTemplateEmail(
            request.getEmail(),
            "Welcome to Our Platform!",
            "welcome",
            context,
            "WELCOME"
        );
    }
    
    public TemplateEmailResponse sendPasswordResetEmail(PasswordResetRequest request) 
            throws MessagingException {
        Context context = new Context();
        context.setVariable("userName", request.getUserName());
        context.setVariable("resetLink", request.getResetLink());
        context.setVariable("expiryTime", request.getExpiryMinutes());
        context.setVariable("year", LocalDateTime.now().getYear());
        
        return sendTemplateEmail(
            request.getEmail(),
            "Password Reset Request",
            "password-reset",
            context,
            "PASSWORD_RESET"
        );
    }
    
    public TemplateEmailResponse sendOrderConfirmation(OrderConfirmationRequest request) 
            throws MessagingException {
        Context context = new Context();
        context.setVariable("customerName", request.getCustomerName());
        context.setVariable("orderNumber", request.getOrderNumber());
        context.setVariable("orderDate", request.getOrderDate().format(
            DateTimeFormatter.ofPattern("MMMM dd, yyyy")
        ));
        context.setVariable("items", request.getItems());
        context.setVariable("total", String.format("%.2f", request.getTotal()));
        context.setVariable("shippingAddress", request.getShippingAddress());
        context.setVariable("year", LocalDateTime.now().getYear());
        
        return sendTemplateEmail(
            request.getEmail(),
            "Order Confirmation #" + request.getOrderNumber(),
            "order-confirmation",
            context,
            "ORDER_CONFIRMATION"
        );
    }
    
    public TemplateEmailResponse sendNewsletter(NewsletterRequest request) 
            throws MessagingException {
        Context context = new Context();
        context.setVariable("subscriberName", request.getSubscriberName());
        context.setVariable("title", request.getNewsletterTitle());
        context.setVariable("articles", request.getArticles());
        context.setVariable("month", LocalDateTime.now().format(
            DateTimeFormatter.ofPattern("MMMM yyyy")
        ));
        context.setVariable("unsubscribeLink", request.getUnsubscribeLink());
        context.setVariable("year", LocalDateTime.now().getYear());
        
        return sendTemplateEmail(
            request.getEmail(),
            request.getNewsletterTitle(),
            "newsletter",
            context,
            "NEWSLETTER"
        );
    }
    
    public TemplateEmailResponse sendCustomTemplateEmail(CustomTemplateRequest request) 
            throws MessagingException {
        Context context = new Context();
        context.setVariables(request.getVariables());
        
        return sendTemplateEmail(
            request.getEmail(),
            request.getSubject(),
            request.getTemplateName(),
            context,
            "CUSTOM"
        );
    }
    
    private TemplateEmailResponse sendTemplateEmail(String to, String subject, 
                                                   String templateName, Context context,
                                                   String emailType) throws MessagingException {
        String emailId = UUID.randomUUID().toString();
        LocalDateTime sentAt = LocalDateTime.now();
        
        try {
            // Process template
            String htmlContent = templateEngine.process(templateName, context);
            
            // Create and send email
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setFrom(properties.getFrom());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            
            mailSender.send(mimeMessage);
            
            // Record history
            recordHistory(emailId, to, subject, templateName, emailType, sentAt, 
                         TemplateEmailStatus.SENT, null);
            
            return new TemplateEmailResponse(emailId, TemplateEmailStatus.SENT,
                "Template email sent successfully", sentAt, templateName);
            
        } catch (MessagingException e) {
            recordHistory(emailId, to, subject, templateName, emailType, sentAt,
                         TemplateEmailStatus.FAILED, e.getMessage());
            throw e;
        }
    }
    
    private void recordHistory(String emailId, String to, String subject, String template,
                              String type, LocalDateTime sentAt, TemplateEmailStatus status,
                              String error) {
        TemplateEmailHistory record = new TemplateEmailHistory(
            emailId, properties.getFrom(), to, subject, template, type, sentAt, status, error
        );
        history.put(emailId, record);
    }
    
    public List<EmailTemplate> getAvailableTemplates() {
        return Arrays.asList(
            new EmailTemplate("welcome", "Welcome Email", "User registration welcome email"),
            new EmailTemplate("password-reset", "Password Reset", "Password reset request email"),
            new EmailTemplate("order-confirmation", "Order Confirmation", "Order confirmation email"),
            new EmailTemplate("newsletter", "Newsletter", "Monthly newsletter email"),
            new EmailTemplate("invoice", "Invoice", "Invoice email template"),
            new EmailTemplate("notification", "Notification", "General notification template")
        );
    }
    
    public List<TemplateEmailHistory> getHistory() {
        return new ArrayList<>(history.values());
    }
}

// ======================== Models ========================

class WelcomeEmailRequest {
    @NotBlank
    private String userName;
    
    @Email
    @NotBlank
    private String email;
    
    @NotBlank
    private String activationLink;
    
    // Getters and Setters
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getActivationLink() { return activationLink; }
    public void setActivationLink(String activationLink) { 
        this.activationLink = activationLink; 
    }
}

class PasswordResetRequest {
    @NotBlank
    private String userName;
    
    @Email
    @NotBlank
    private String email;
    
    @NotBlank
    private String resetLink;
    
    private int expiryMinutes = 30;
    
    // Getters and Setters
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getResetLink() { return resetLink; }
    public void setResetLink(String resetLink) { this.resetLink = resetLink; }
    
    public int getExpiryMinutes() { return expiryMinutes; }
    public void setExpiryMinutes(int expiryMinutes) { this.expiryMinutes = expiryMinutes; }
}

class OrderConfirmationRequest {
    @NotBlank
    private String customerName;
    
    @Email
    @NotBlank
    private String email;
    
    @NotBlank
    private String orderNumber;
    
    private LocalDateTime orderDate = LocalDateTime.now();
    private List<OrderItem> items;
    private double total;
    private String shippingAddress;
    
    // Getters and Setters
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
    
    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }
    
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
    
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
    
    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { 
        this.shippingAddress = shippingAddress; 
    }
}

class OrderItem {
    private String name;
    private int quantity;
    private double price;
    
    public OrderItem() {}
    
    public OrderItem(String name, int quantity, double price) {
        this.name = name;
        this.quantity = quantity;
        this.price = price;
    }
    
    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    
    public double getSubtotal() { return quantity * price; }
}

class NewsletterRequest {
    @NotBlank
    private String subscriberName;
    
    @Email
    @NotBlank
    private String email;
    
    @NotBlank
    private String newsletterTitle;
    
    private List<NewsletterArticle> articles;
    private String unsubscribeLink;
    
    // Getters and Setters
    public String getSubscriberName() { return subscriberName; }
    public void setSubscriberName(String subscriberName) { 
        this.subscriberName = subscriberName; 
    }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getNewsletterTitle() { return newsletterTitle; }
    public void setNewsletterTitle(String newsletterTitle) { 
        this.newsletterTitle = newsletterTitle; 
    }
    
    public List<NewsletterArticle> getArticles() { return articles; }
    public void setArticles(List<NewsletterArticle> articles) { this.articles = articles; }
    
    public String getUnsubscribeLink() { return unsubscribeLink; }
    public void setUnsubscribeLink(String unsubscribeLink) { 
        this.unsubscribeLink = unsubscribeLink; 
    }
}

class NewsletterArticle {
    private String title;
    private String summary;
    private String link;
    private String imageUrl;
    
    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    
    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }
    
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}

class CustomTemplateRequest {
    @Email
    @NotBlank
    private String email;
    
    @NotBlank
    private String subject;
    
    @NotBlank
    private String templateName;
    
    private Map<String, Object> variables = new HashMap<>();
    
    // Getters and Setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    
    public String getTemplateName() { return templateName; }
    public void setTemplateName(String templateName) { this.templateName = templateName; }
    
    public Map<String, Object> getVariables() { return variables; }
    public void setVariables(Map<String, Object> variables) { this.variables = variables; }
}

class TemplateEmailResponse {
    private String emailId;
    private TemplateEmailStatus status;
    private String message;
    private LocalDateTime timestamp;
    private String templateUsed;
    
    public TemplateEmailResponse(String emailId, TemplateEmailStatus status, String message,
                                LocalDateTime timestamp, String templateUsed) {
        this.emailId = emailId;
        this.status = status;
        this.message = message;
        this.timestamp = timestamp;
        this.templateUsed = templateUsed;
    }
    
    // Getters
    public String getEmailId() { return emailId; }
    public TemplateEmailStatus getStatus() { return status; }
    public String getMessage() { return message; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getTemplateUsed() { return templateUsed; }
}

class TemplateEmailHistory {
    private String emailId;
    private String from;
    private String to;
    private String subject;
    private String template;
    private String type;
    private LocalDateTime sentAt;
    private TemplateEmailStatus status;
    private String errorMessage;
    
    public TemplateEmailHistory(String emailId, String from, String to, String subject,
                               String template, String type, LocalDateTime sentAt,
                               TemplateEmailStatus status, String errorMessage) {
        this.emailId = emailId;
        this.from = from;
        this.to = to;
        this.subject = subject;
        this.template = template;
        this.type = type;
        this.sentAt = sentAt;
        this.status = status;
        this.errorMessage = errorMessage;
    }
    
    // Getters
    public String getEmailId() { return emailId; }
    public String getFrom() { return from; }
    public String getTo() { return to; }
    public String getSubject() { return subject; }
    public String getTemplate() { return template; }
    public String getType() { return type; }
    public LocalDateTime getSentAt() { return sentAt; }
    public TemplateEmailStatus getStatus() { return status; }
    public String getErrorMessage() { return errorMessage; }
}

class EmailTemplate {
    private String name;
    private String displayName;
    private String description;
    
    public EmailTemplate(String name, String displayName, String description) {
        this.name = name;
        this.displayName = displayName;
        this.description = description;
    }
    
    // Getters
    public String getName() { return name; }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
}

enum TemplateEmailStatus {
    SENT,
    FAILED,
    PENDING
}

/*
 * Example Template Files:
 * 
 * src/main/resources/templates/email/welcome.html:
 * <!DOCTYPE html>
 * <html xmlns:th="http://www.thymeleaf.org">
 * <head>
 *     <meta charset="UTF-8">
 *     <title>Welcome</title>
 * </head>
 * <body>
 *     <h1>Welcome, <span th:text="${userName}">User</span>!</h1>
 *     <p>Thank you for registering with us.</p>
 *     <p>Please click the link below to activate your account:</p>
 *     <a th:href="${activationLink}">Activate Account</a>
 *     <p>&copy; <span th:text="${year}">2024</span> Our Company</p>
 * </body>
 * </html>
 * 
 * src/main/resources/templates/email/password-reset.html:
 * <!DOCTYPE html>
 * <html xmlns:th="http://www.thymeleaf.org">
 * <head>
 *     <meta charset="UTF-8">
 *     <title>Password Reset</title>
 * </head>
 * <body>
 *     <h1>Password Reset Request</h1>
 *     <p>Hello <span th:text="${userName}">User</span>,</p>
 *     <p>We received a request to reset your password.</p>
 *     <p>Click the link below to reset your password:</p>
 *     <a th:href="${resetLink}">Reset Password</a>
 *     <p>This link will expire in <span th:text="${expiryTime}">30</span> minutes.</p>
 *     <p>If you didn't request this, please ignore this email.</p>
 * </body>
 * </html>
 */

/*
 * Usage Examples:
 * 
 * 1. Send Welcome Email:
 * POST /api/template-email/welcome
 * {
 *   "userName": "John Doe",
 *   "email": "john@example.com",
 *   "activationLink": "https://example.com/activate?token=abc123"
 * }
 * 
 * 2. Send Password Reset:
 * POST /api/template-email/password-reset
 * {
 *   "userName": "John Doe",
 *   "email": "john@example.com",
 *   "resetLink": "https://example.com/reset?token=xyz789",
 *   "expiryMinutes": 30
 * }
 * 
 * 3. Send Order Confirmation:
 * POST /api/template-email/order-confirmation
 * {
 *   "customerName": "John Doe",
 *   "email": "john@example.com",
 *   "orderNumber": "ORD-12345",
 *   "items": [
 *     {"name": "Product A", "quantity": 2, "price": 29.99},
 *     {"name": "Product B", "quantity": 1, "price": 49.99}
 *   ],
 *   "total": 109.97,
 *   "shippingAddress": "123 Main St, City, State 12345"
 * }
 */
