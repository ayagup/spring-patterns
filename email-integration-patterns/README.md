# Email Integration Patterns

This collection demonstrates various Spring email integration patterns for building robust email-sending applications.

## Patterns Overview

### 1. Mail Sender Pattern (`MailSenderPattern.java`)
**Purpose:** Basic email sending with Spring Mail  
**Use Case:** Simple transactional emails, notifications  
**Key Features:**
- JavaMailSender configuration
- Simple text emails with To/CC/BCC
- Email history tracking
- Multiple recipients support
- Email statistics

**Example Usage:**
```java
POST /api/mail/send
{
  "to": "user@example.com",
  "subject": "Welcome",
  "body": "Welcome to our service"
}
```

### 2. MIME Message Pattern (`MIMEMessagePattern.java`)
**Purpose:** Advanced email with HTML, attachments, and inline resources  
**Use Case:** Rich emails with formatting and media  
**Key Features:**
- HTML email content
- File attachments
- Inline images (embedded in HTML)
- Multipart messages
- MimeMessageHelper utilities

**Example Usage:**
```java
POST /api/mime/send-html
{
  "to": "user@example.com",
  "subject": "HTML Newsletter",
  "htmlContent": "<h1>Welcome!</h1><p>Beautiful email content</p>"
}
```

### 3. Template-Based Email Pattern (`TemplateBasedEmailPattern.java`)
**Purpose:** Dynamic email generation using templates  
**Use Case:** Welcome emails, password resets, order confirmations  
**Key Features:**
- Thymeleaf template engine
- Variable substitution
- Reusable email templates
- HTML and text templates
- Template caching

**Example Usage:**
```java
POST /api/template/welcome
{
  "to": "newuser@example.com",
  "name": "John Doe",
  "activationLink": "https://example.com/activate/abc123"
}
```

### 4. Attachment Handling Pattern (`AttachmentHandlingPattern.java`)
**Purpose:** Email attachment management and validation  
**Use Case:** Document sharing, invoice delivery  
**Key Features:**
- File validation (size, type, extension)
- Multiple attachment sources (filesystem, classpath, upload)
- ZIP compression for multiple files
- Mixed content (attachments + inline images)
- Security checks

**Example Usage:**
```java
POST /api/attachments/send-with-files
Content-Type: multipart/form-data
- files: [file1.pdf, file2.docx]
- to: recipient@example.com
- subject: Documents Attached
```

### 5. HTML Email Pattern (`HTMLEmailPattern.java`)
**Purpose:** Rich HTML email construction  
**Use Case:** Marketing emails, newsletters  
**Key Features:**
- Responsive email design
- Inline CSS (email client compatible)
- Media queries for mobile
- Alternative text fallback
- HTMLEmailBuilder (fluent API)

**Example Usage:**
```java
POST /api/html/send-responsive
{
  "to": "subscriber@example.com",
  "subject": "Monthly Newsletter",
  "htmlBuilder": {
    "title": "This Month in Tech",
    "sections": [...]
  }
}
```

### 6. Async Email Pattern (`AsyncEmailPattern.java`)
**Purpose:** Non-blocking email sending with retry logic  
**Use Case:** Bulk emails, background notifications  
**Key Features:**
- @Async execution
- ThreadPoolTaskExecutor configuration
- Priority queue for urgent emails
- Retry mechanism with exponential backoff
- Email queue monitoring
- CompletableFuture for async operations

**Example Usage:**
```java
POST /api/async/send-priority
{
  "to": "urgent@example.com",
  "subject": "Urgent: Account Alert",
  "body": "...",
  "priority": "HIGH"
}
```

## Pattern Comparison Matrix

| Pattern | Complexity | Use Case | Async Support | Template Support | Attachment Support |
|---------|-----------|----------|---------------|------------------|-------------------|
| Mail Sender | Low | Basic emails | No | No | No |
| MIME Message | Medium | Rich content | No | No | Yes |
| Template-Based | Medium | Dynamic emails | No | Yes | No |
| Attachment Handling | Medium | File sharing | No | No | Yes (Advanced) |
| HTML Email | Medium | Marketing | No | No | No |
| Async Email | High | Bulk sending | Yes | No | No |

## When to Use Each Pattern

### Choose Mail Sender Pattern when:
- Sending simple text emails
- No attachments needed
- Synchronous sending is acceptable
- Basic tracking required

### Choose MIME Message Pattern when:
- Need HTML formatting
- Sending attachments
- Require inline images
- Complex email structure

### Choose Template-Based Pattern when:
- Sending repetitive emails with variable data
- Multiple email types (welcome, reset, etc.)
- Centralized template management
- Consistent branding across emails

### Choose Attachment Handling Pattern when:
- File validation is critical
- Multiple attachment sources
- Need compression
- Security-conscious file handling

### Choose HTML Email Pattern when:
- Marketing campaigns
- Responsive design required
- Rich visual content
- Newsletter distribution

### Choose Async Email Pattern when:
- Bulk email sending
- Non-blocking operations required
- Email prioritization needed
- Retry logic essential
- High throughput required

## Configuration

### application.properties
```properties
# Mail Configuration
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# Async Configuration
spring.task.execution.pool.core-size=5
spring.task.execution.pool.max-size=10
spring.task.execution.pool.queue-capacity=100

# Attachment Configuration
mail.attachment.max-size=25MB
mail.attachment.total-max-size=50MB
```

## Dependencies

```xml
<!-- Spring Boot Starter Mail -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>

<!-- Thymeleaf for Template-Based Email -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>

<!-- Validation -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

## Best Practices

1. **Always validate email addresses** before sending
2. **Use templates** for consistent branding
3. **Implement retry logic** for production systems
4. **Monitor email delivery** status
5. **Limit attachment sizes** to avoid SMTP limits
6. **Use async sending** for bulk operations
7. **Test with multiple email clients** (Gmail, Outlook, etc.)
8. **Implement rate limiting** to avoid being flagged as spam
9. **Handle bounces and failures** gracefully
10. **Keep email content accessible** (provide text alternatives)

## Common Integration Scenarios

### Scenario 1: User Registration
Use **Template-Based Email** with **Async Email** for sending welcome emails without blocking registration.

### Scenario 2: Invoice Delivery
Use **Attachment Handling** with **MIME Message** to send PDF invoices securely.

### Scenario 3: Marketing Campaign
Use **HTML Email** with **Async Email** for responsive newsletters to large audiences.

### Scenario 4: System Alerts
Use **Mail Sender** with **Async Email** (high priority) for immediate admin notifications.

## Error Handling

All patterns include comprehensive error handling:
- Connection failures
- Authentication errors
- Invalid recipients
- Attachment size violations
- Template rendering errors

## Performance Considerations

- **Mail Sender**: Synchronous, ~100-200ms per email
- **Async Email**: Non-blocking, can handle 1000+ emails/minute
- **Template-Based**: Add ~50ms for template rendering
- **Attachment**: Add time based on file size (network dependent)

## Security Considerations

1. **Never expose SMTP credentials** in code
2. **Validate all user inputs** (especially email addresses)
3. **Scan attachments** for malware
4. **Implement rate limiting** per user/IP
5. **Use TLS/SSL** for SMTP connections
6. **Sanitize HTML content** to prevent XSS
7. **Implement SPF/DKIM/DMARC** for production

## Testing

Each pattern includes REST endpoints for easy testing:
```bash
# Test basic email
curl -X POST http://localhost:8080/api/mail/send \
  -H "Content-Type: application/json" \
  -d '{"to":"test@example.com","subject":"Test","body":"Hello"}'

# Test async email
curl -X POST http://localhost:8080/api/async/send \
  -H "Content-Type: application/json" \
  -d '{"to":"test@example.com","subject":"Async Test","body":"Hello"}'
```

## License

These patterns are provided as educational examples for Spring Boot email integration.
