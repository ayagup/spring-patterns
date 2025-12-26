package com.example.fileupload.validation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

/**
 * File Upload Validation Pattern
 * 
 * Validates file type, size, and content before upload.
 */
@SpringBootApplication
public class FileUploadValidationPattern {

    public static void main(String[] args) {
        SpringApplication.run(FileUploadValidationPattern.class, args);
    }

    @RestController
    public static class ValidatedUploadController {

        private static final long MAX_SIZE = 10 * 1024 * 1024; // 10MB
        private static final Set<String> ALLOWED_TYPES = 
            Set.of("image/jpeg", "image/png", "application/pdf");

        @PostMapping("/api/upload")
        public String upload(@RequestParam("file") MultipartFile file) {
            // Validate size
            if (file.getSize() > MAX_SIZE) {
                return "File too large";
            }

            // Validate type
            if (!ALLOWED_TYPES.contains(file.getContentType())) {
                return "Invalid file type";
            }

            // Validate filename
            String filename = file.getOriginalFilename();
            if (filename == null || filename.contains("..")) {
                return "Invalid filename";
            }

            return "File valid: " + filename;
        }
    }
}
