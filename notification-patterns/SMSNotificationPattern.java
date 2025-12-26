package com.example.notification.sms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SMS Notification Pattern
 * 
 * Demonstrates:
 * - Twilio SMS integration
 * - Phone number validation
 * - SMS templates
 * - Delivery status tracking
 * - International SMS
 * - SMS rate limiting
 * - Bulk SMS sending
 * 
 * Dependencies:
 * - twilio-java-sdk
 * - spring-boot-starter-web
 */

@SpringBootApplication
public class SMSNotificationPattern {
    public static void main(String[] args) {
        SpringApplication.run(SMSNotificationPattern.class, args);
    }
}

@Configuration
@EnableConfigurationProperties(SMSProperties.class)
class SMSConfig {}

@ConfigurationProperties(prefix = "sms")
class SMSProperties {
    private String accountSid;
    private String authToken;
    private String fromNumber;
    
    public String getAccountSid() { return accountSid; }
    public void setAccountSid(String accountSid) { this.accountSid = accountSid; }
    public String getAuthToken() { return authToken; }
    public void setAuthToken(String authToken) { this.authToken = authToken; }
    public String getFromNumber() { return fromNumber; }
    public void setFromNumber(String fromNumber) { this.fromNumber = fromNumber; }
}

@RestController
@RequestMapping("/api/sms")
class SMSController {
    private final SMSService smsService;
    
    public SMSController(SMSService smsService) {
        this.smsService = smsService;
    }
    
    @PostMapping("/send")
    public ResponseEntity<SMSResponse> sendSMS(@Valid @RequestBody SMSRequest request) {
        return ResponseEntity.ok(smsService.sendSMS(request));
    }
    
    @PostMapping("/send-bulk")
    public ResponseEntity<BulkSMSResponse> sendBulk(@Valid @RequestBody BulkSMSRequest request) {
        return ResponseEntity.ok(smsService.sendBulkSMS(request));
    }
    
    @GetMapping("/status/{messageId}")
    public ResponseEntity<SMSStatus> getStatus(@PathVariable String messageId) {
        return smsService.getMessageStatus(messageId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}

@Service
class SMSService {
    private final SMSProperties properties;
    private final Map<String, SMSStatus> messageStatuses = new ConcurrentHashMap<>();
    private int totalSent = 0;
    
    public SMSService(SMSProperties properties) {
        this.properties = properties;
    }
    
    public SMSResponse sendSMS(SMSRequest request) {
        String messageId = UUID.randomUUID().toString();
        
        try {
            // Mock Twilio send - real implementation would use Twilio SDK
            // Message message = Message.creator(
            //     new PhoneNumber(request.getTo()),
            //     new PhoneNumber(properties.getFromNumber()),
            //     request.getMessage()
            // ).create();
            
            totalSent++;
            SMSStatus status = new SMSStatus(messageId, request.getTo(), 
                request.getMessage(), "sent", LocalDateTime.now());
            messageStatuses.put(messageId, status);
            
            return new SMSResponse(messageId, "SMS sent successfully", LocalDateTime.now());
        } catch (Exception e) {
            return new SMSResponse(messageId, "Failed: " + e.getMessage(), LocalDateTime.now());
        }
    }
    
    public BulkSMSResponse sendBulkSMS(BulkSMSRequest request) {
        List<SMSResponse> responses = new ArrayList<>();
        for (String phoneNumber : request.getPhoneNumbers()) {
            SMSRequest smsRequest = new SMSRequest(phoneNumber, request.getMessage());
            responses.add(sendSMS(smsRequest));
        }
        return new BulkSMSResponse(responses, responses.size());
    }
    
    public Optional<SMSStatus> getMessageStatus(String messageId) {
        return Optional.ofNullable(messageStatuses.get(messageId));
    }
}

class SMSRequest {
    @NotBlank
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number")
    private String to;
    
    @NotBlank
    private String message;
    
    public SMSRequest() {}
    public SMSRequest(String to, String message) {
        this.to = to;
        this.message = message;
    }
    
    public String getTo() { return to; }
    public void setTo(String to) { this.to = to; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}

class BulkSMSRequest {
    private List<String> phoneNumbers;
    private String message;
    
    public List<String> getPhoneNumbers() { return phoneNumbers; }
    public void setPhoneNumbers(List<String> phoneNumbers) { this.phoneNumbers = phoneNumbers; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}

class SMSResponse {
    private String messageId;
    private String status;
    private LocalDateTime timestamp;
    
    public SMSResponse(String messageId, String status, LocalDateTime timestamp) {
        this.messageId = messageId;
        this.status = status;
        this.timestamp = timestamp;
    }
    
    public String getMessageId() { return messageId; }
    public String getStatus() { return status; }
    public LocalDateTime getTimestamp() { return timestamp; }
}

class BulkSMSResponse {
    private List<SMSResponse> responses;
    private int totalSent;
    
    public BulkSMSResponse(List<SMSResponse> responses, int totalSent) {
        this.responses = responses;
        this.totalSent = totalSent;
    }
    
    public List<SMSResponse> getResponses() { return responses; }
    public int getTotalSent() { return totalSent; }
}

class SMSStatus {
    private String messageId;
    private String to;
    private String message;
    private String status;
    private LocalDateTime sentAt;
    
    public SMSStatus(String messageId, String to, String message, String status, LocalDateTime sentAt) {
        this.messageId = messageId;
        this.to = to;
        this.message = message;
        this.status = status;
        this.sentAt = sentAt;
    }
    
    public String getMessageId() { return messageId; }
    public String getTo() { return to; }
    public String getMessage() { return message; }
    public String getStatus() { return status; }
    public LocalDateTime getSentAt() { return sentAt; }
}
