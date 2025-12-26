# Notification Patterns

This collection demonstrates comprehensive notification delivery patterns across multiple channels (Push, SMS, Email, In-App, Real-time).

## Patterns Overview

### 1. Push Notification Pattern (`PushNotificationPattern.java`)
**Purpose:** Mobile and web push notifications via FCM/APNS  
**Use Case:** Mobile app alerts, web browser notifications  
**Key Features:**
- Firebase Cloud Messaging (FCM) integration
- Apple Push Notification Service (APNS) support
- Device token management
- Topic-based subscriptions
- Platform-specific payloads (iOS/Android/Web)
- Batch notification sending

**Example Usage:**
```java
// Register device
POST /api/push/devices/register
{
  "deviceToken": "fcm_token_here",
  "platform": "ANDROID",
  "userId": "user123"
}

// Send push notification
POST /api/push/send
{
  "deviceToken": "fcm_token_here",
  "title": "New Message",
  "body": "You have a new message",
  "data": {"messageId": "msg123"}
}
```

### 2. SMS Notification Pattern (`SMSNotificationPattern.java`)
**Purpose:** SMS delivery via Twilio  
**Use Case:** OTP codes, alerts, reminders  
**Key Features:**
- Twilio SDK integration
- International phone number validation
- SMS templates
- Delivery status tracking
- Bulk SMS sending
- Rate limiting

**Example Usage:**
```java
POST /api/sms/send
{
  "to": "+1234567890",
  "message": "Your verification code is: 123456"
}
```

### 3. Email Notification Pattern (`EmailNotificationPattern.java`)
**Purpose:** Unified email notification service  
**Use Case:** System notifications, alerts  
**Key Features:**
- Facade over email integration patterns
- Priority-based handling
- HTML and plain text support
- Recipient grouping
- Notification tracking
- Multi-template support

**Example Usage:**
```java
POST /api/email-notifications/send
{
  "to": "user@example.com",
  "subject": "Account Alert",
  "body": "Your password was changed",
  "priority": "HIGH"
}
```

### 4. In-App Notification Pattern (`InAppNotificationPattern.java`)
**Purpose:** Application notification center  
**Use Case:** User notifications within the app  
**Key Features:**
- Read/unread status tracking
- Notification categories (GENERAL, SOCIAL, SYSTEM, MARKETING, ALERT)
- User preferences
- Notification archiving
- Badge counters
- Action URLs

**Example Usage:**
```java
// Send in-app notification
POST /api/in-app-notifications/send
{
  "userId": "user123",
  "title": "New Comment",
  "message": "John commented on your post",
  "category": "SOCIAL",
  "actionUrl": "/posts/456"
}

// Get unread notifications
GET /api/in-app-notifications/user/user123/unread

// Mark as read
PUT /api/in-app-notifications/{notificationId}/mark-read
```

### 5. Real-Time Notification Pattern (`RealTimeNotificationPattern.java`)
**Purpose:** WebSocket/SSE for real-time push  
**Use Case:** Live updates, chat, dashboards  
**Key Features:**
- Server-Sent Events (SSE)
- WebSocket support
- Connection management
- User-specific channels
- Broadcast capabilities
- Connection status tracking

**Example Usage:**
```javascript
// Client-side subscription
const eventSource = new EventSource('/api/realtime-notifications/subscribe/user123');

eventSource.addEventListener('notification', (event) => {
  const notification = JSON.parse(event.data);
  console.log('Received:', notification);
});

// Server sends notification
POST /api/realtime-notifications/send
{
  "userId": "user123",
  "title": "Live Update",
  "message": "New data available",
  "type": "INFO"
}
```

### 6. Notification Queue Pattern (`NotificationQueuePattern.java`)
**Purpose:** Message queue for reliable delivery  
**Use Case:** High-volume notifications, guaranteed delivery  
**Key Features:**
- Priority queue (PriorityBlockingQueue)
- Retry logic with exponential backoff
- Dead letter queue for failed messages
- Batch processing
- Queue monitoring
- Multi-channel support

**Example Usage:**
```java
// Enqueue notification
POST /api/notification-queue/enqueue
{
  "recipient": "user@example.com",
  "message": "Your order has shipped",
  "channel": "EMAIL",
  "priority": 7
}

// Process queue
POST /api/notification-queue/process

// Check queue status
GET /api/notification-queue/status
```

### 7. Notification Template Pattern (`NotificationTemplatePattern.java`)
**Purpose:** Multi-channel notification templates  
**Use Case:** Reusable notification content  
**Key Features:**
- Template versioning
- Variable substitution ({{variable}})
- Multi-channel templates (EMAIL, SMS, PUSH, IN_APP)
- Localization support
- Template caching
- Dynamic rendering

**Example Usage:**
```java
// Create template
POST /api/notification-templates/create
{
  "name": "order_confirmation",
  "subject": "Order #{{orderId}} Confirmed",
  "body": "Hello {{customerName}}, your order #{{orderId}} totaling ${{amount}} has been confirmed.",
  "channel": "EMAIL",
  "variables": ["orderId", "customerName", "amount"]
}

// Send from template
POST /api/notification-templates/send-from-template
{
  "templateId": "template-uuid",
  "recipient": "customer@example.com",
  "variables": {
    "orderId": "12345",
    "customerName": "John Doe",
    "amount": "99.99"
  }
}
```

## Pattern Comparison Matrix

| Pattern | Channel | Real-time | Queue Support | Template Support | Tracking | Retry Logic |
|---------|---------|-----------|---------------|------------------|----------|-------------|
| Push Notification | Mobile/Web | Yes | No | No | Yes | No |
| SMS Notification | SMS | No | No | Yes | Yes | No |
| Email Notification | Email | No | No | Yes | Yes | No |
| In-App Notification | Application | Yes | No | No | Yes | No |
| Real-Time Notification | WebSocket/SSE | Yes | No | No | Yes | No |
| Notification Queue | Multi-channel | No | Yes | No | Yes | Yes |
| Notification Template | Multi-channel | No | No | Yes | Yes | No |

## When to Use Each Pattern

### Choose Push Notification Pattern when:
- Targeting mobile app users
- Need immediate delivery to devices
- Supporting iOS and Android
- Implementing topic-based broadcasts
- Sending rich notifications with actions

### Choose SMS Notification Pattern when:
- Sending verification codes
- Reaching users without internet
- Time-sensitive alerts (bank transactions)
- High delivery rate is critical
- Supporting global phone numbers

### Choose Email Notification Pattern when:
- Sending detailed information
- Formal communications
- Delivery confirmation needed
- HTML formatting required
- Supporting attachments

### Choose In-App Notification Pattern when:
- User is actively using the application
- Building notification center
- Supporting read/unread status
- Categorizing notifications
- Providing action links

### Choose Real-Time Notification Pattern when:
- Live updates required (dashboards)
- Chat applications
- Collaborative editing
- Real-time feeds
- Event streaming

### Choose Notification Queue Pattern when:
- High volume notifications
- Guaranteed delivery required
- Implementing retry logic
- Priority-based sending
- Rate limiting needed

### Choose Notification Template Pattern when:
- Sending repetitive notifications
- Multiple channels use same content
- Supporting multiple languages
- Version control for notifications
- Consistent messaging across channels

## Multi-Channel Notification Strategy

### Scenario 1: User Registration
```
1. Send Email (confirmation link) - Email Notification Pattern
2. Send SMS (verification code) - SMS Notification Pattern
3. Create In-App welcome - In-App Notification Pattern
```

### Scenario 2: Order Confirmation
```
1. Queue notification - Notification Queue Pattern
2. Send Push - Push Notification Pattern
3. Send Email with template - Notification Template + Email Pattern
4. Create In-App notification - In-App Notification Pattern
```

### Scenario 3: Critical Alert
```
1. Real-time push to active users - Real-Time Notification Pattern
2. Push notification to mobile - Push Notification Pattern
3. SMS to phone - SMS Notification Pattern
4. Email backup - Email Notification Pattern
```

## Configuration

### application.properties
```properties
# Push Notification (Firebase)
firebase.credentials.path=firebase-adminsdk.json
firebase.project.id=your-project-id

# Push Notification (APNS)
apns.key.path=apns-key.p8
apns.team.id=YOUR_TEAM_ID
apns.key.id=YOUR_KEY_ID
apns.sandbox=true

# SMS (Twilio)
sms.account.sid=ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
sms.auth.token=your_auth_token
sms.from.number=+1234567890

# Notification Queue
notification.queue.max-retries=3
notification.queue.retry-delay-ms=5000
notification.queue.batch-size=10
```

## Dependencies

```xml
<!-- Firebase Cloud Messaging -->
<dependency>
    <groupId>com.google.firebase</groupId>
    <artifactId>firebase-admin</artifactId>
    <version>9.1.1</version>
</dependency>

<!-- Twilio SMS -->
<dependency>
    <groupId>com.twilio.sdk</groupId>
    <artifactId>twilio</artifactId>
    <version>9.2.0</version>
</dependency>

<!-- WebSocket Support -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-websocket</artifactId>
</dependency>

<!-- Spring Mail -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>
```

## Best Practices

1. **Choose the right channel** based on urgency and user preference
2. **Implement retry logic** for critical notifications
3. **Respect user preferences** (opt-out, quiet hours)
4. **Track delivery status** for all channels
5. **Use templates** for consistent messaging
6. **Implement rate limiting** to prevent spam
7. **Test across platforms** (iOS, Android, Web)
8. **Handle failures gracefully** with fallback channels
9. **Monitor queue depths** and processing times
10. **Comply with regulations** (GDPR, CAN-SPAM, TCPA)

## Error Handling

Each pattern includes comprehensive error handling:
- Network failures (retry with backoff)
- Invalid device tokens (remove from registry)
- Invalid phone numbers (validation before sending)
- Template rendering errors
- Queue overflow
- Connection timeouts

## Performance Considerations

- **Push Notifications**: ~100-500ms latency
- **SMS**: ~1-5 seconds delivery time
- **Email**: ~2-10 seconds delivery time
- **In-App**: Instant (in-memory)
- **Real-Time**: <100ms latency
- **Queue**: Configurable throughput (batch size dependent)

## Security Considerations

1. **Encrypt sensitive data** in notifications
2. **Validate all inputs** (phone numbers, emails, tokens)
3. **Use HTTPS/TLS** for all external APIs
4. **Implement authentication** for notification endpoints
5. **Sanitize notification content** to prevent injection
6. **Rate limit** per user to prevent abuse
7. **Store credentials securely** (never in code)
8. **Implement token rotation** for push notifications
9. **Audit notification logs** for compliance
10. **Support user opt-out** mechanisms

## Testing

```bash
# Test push notification
curl -X POST http://localhost:8080/api/push/send \
  -H "Content-Type: application/json" \
  -d '{"deviceToken":"test_token","title":"Test","body":"Hello"}'

# Test SMS
curl -X POST http://localhost:8080/api/sms/send \
  -H "Content-Type: application/json" \
  -d '{"to":"+1234567890","message":"Test SMS"}'

# Test real-time notification (SSE)
curl http://localhost:8080/api/realtime-notifications/subscribe/user123
```

## License

These patterns are provided as educational examples for Spring Boot notification systems.
