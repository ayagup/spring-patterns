package com.example.fileupload.presigned;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Pre-signed URL Upload Pattern
 * 
 * Generates pre-signed URLs for direct client uploads.
 * Commonly used with cloud storage (S3, Azure Blob).
 */
@SpringBootApplication
public class PresignedURLUploadPattern {

    public static void main(String[] args) {
        SpringApplication.run(PresignedURLUploadPattern.class, args);
    }

    @RestController
    public static class PresignedController {

        @PostMapping("/api/presigned-url")
        public PresignedResponse generatePresignedUrl(@RequestParam String filename) {
            String uploadId = UUID.randomUUID().toString();
            String presignedUrl = "https://storage.example.com/upload/" + uploadId;
            
            // In real implementation, generate actual presigned URL
            // using AWS SDK or similar
            
            return new PresignedResponse(
                uploadId,
                presignedUrl,
                System.currentTimeMillis() + 3600000 // 1 hour expiry
            );
        }

        @PostMapping("/api/confirm-upload")
        public String confirmUpload(@RequestParam String uploadId) {
            // Verify upload completed
            return "Upload " + uploadId + " confirmed";
        }
    }

    public record PresignedResponse(String uploadId, String url, long expiresAt) {}
}
