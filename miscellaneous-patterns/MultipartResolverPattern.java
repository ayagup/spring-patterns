package com.example.miscellaneous.multipartresolver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

import javax.servlet.MultipartConfigElement;
import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Multipart Resolver Pattern - Demonstrates Spring's file upload handling
 * 
 * This pattern shows how to:
 * 1. Configure MultipartResolver
 * 2. Use StandardServletMultipartResolver
 * 3. Handle single file uploads
 * 4. Handle multiple file uploads
 * 5. Validate file size
 * 6. Validate file types
 * 7. Save files to filesystem
 * 8. Stream large files
 * 9. Handle upload errors
 * 10. Manage temporary files
 * 
 * Key Concepts:
 * - MultipartResolver: Resolves multipart HTTP requests
 * - MultipartFile: Represents uploaded file
 * - StandardServletMultipartResolver: Servlet 3.0+ implementation
 * - File Size Limits: Max file size configuration
 * - File Type Validation: MIME type checking
 * 
 * Dependencies:
 * - spring-web
 * - spring-boot-starter-web
 * - servlet-api
 * 
 * @author Spring Patterns
 * @version 1.0
 */
@SpringBootApplication
public class MultipartResolverPattern {
    
    public static void main(String[] args) {
        SpringApplication.run(MultipartResolverPattern.class, args);
    }
}

// ============================================================================
// File Upload Service
// ============================================================================

/**
 * Service for file upload operations
 */
@org.springframework.stereotype.Service
class FileUploadService {
    
    private final String uploadDir = "uploads/";
    private final long maxFileSize = 10 * 1024 * 1024; // 10MB
    private final Set<String> allowedTypes = new HashSet<>(Arrays.asList(
        "image/jpeg", "image/png", "image/gif", 
        "application/pdf", "text/plain", "application/zip"
    ));
    
    public FileUploadService() {
        try {
            Files.createDirectories(Paths.get(uploadDir));
        } catch (IOException e) {
            System.err.println("Error creating upload directory: " + e.getMessage());
        }
    }
    
    public UploadResult uploadFile(MultipartFile file) throws IOException {
        validateFile(file);
        
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String filename = generateUniqueFilename(originalFilename);
        Path targetPath = Paths.get(uploadDir + filename);
        
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        
        UploadResult result = new UploadResult();
        result.setFilename(filename);
        result.setOriginalFilename(originalFilename);
        result.setSize(file.getSize());
        result.setContentType(file.getContentType());
        result.setUploadTime(LocalDateTime.now());
        result.setPath(targetPath.toString());
        
        return result;
    }
    
    public List<UploadResult> uploadMultipleFiles(List<MultipartFile> files) throws IOException {
        List<UploadResult> results = new ArrayList<>();
        
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                results.add(uploadFile(file));
            }
        }
        
        return results;
    }
    
    public byte[] downloadFile(String filename) throws IOException {
        Path filePath = Paths.get(uploadDir + filename);
        
        if (!Files.exists(filePath)) {
            throw new FileNotFoundException("File not found: " + filename);
        }
        
        return Files.readAllBytes(filePath);
    }
    
    public boolean deleteFile(String filename) {
        try {
            Path filePath = Paths.get(uploadDir + filename);
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            return false;
        }
    }
    
    public List<FileInfo> listFiles() {
        List<FileInfo> fileInfos = new ArrayList<>();
        
        try {
            Files.list(Paths.get(uploadDir))
                .filter(Files::isRegularFile)
                .forEach(path -> {
                    try {
                        FileInfo info = new FileInfo();
                        info.setFilename(path.getFileName().toString());
                        info.setSize(Files.size(path));
                        info.setPath(path.toString());
                        fileInfos.add(info);
                    } catch (IOException e) {
                        // Skip files with errors
                    }
                });
        } catch (IOException e) {
            System.err.println("Error listing files: " + e.getMessage());
        }
        
        return fileInfos;
    }
    
    private void validateFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        
        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException(
                "File size exceeds maximum: " + (maxFileSize / 1024 / 1024) + "MB");
        }
        
        if (!allowedTypes.contains(file.getContentType())) {
            throw new IllegalArgumentException(
                "File type not allowed: " + file.getContentType());
        }
    }
    
    private String generateUniqueFilename(String originalFilename) {
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = originalFilename.substring(dotIndex);
        }
        
        return System.currentTimeMillis() + "_" + UUID.randomUUID().toString() + extension;
    }
}

// ============================================================================
// Domain Models
// ============================================================================

/**
 * Upload result model
 */
class UploadResult {
    private String filename;
    private String originalFilename;
    private long size;
    private String contentType;
    private LocalDateTime uploadTime;
    private String path;
    
    // Getters and setters
    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }
    
    public String getOriginalFilename() { return originalFilename; }
    public void setOriginalFilename(String originalFilename) { 
        this.originalFilename = originalFilename; 
    }
    
    public long getSize() { return size; }
    public void setSize(long size) { this.size = size; }
    
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    
    public LocalDateTime getUploadTime() { return uploadTime; }
    public void setUploadTime(LocalDateTime uploadTime) { this.uploadTime = uploadTime; }
    
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
}

/**
 * File information model
 */
class FileInfo {
    private String filename;
    private long size;
    private String path;
    
    // Getters and setters
    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }
    
    public long getSize() { return size; }
    public void setSize(long size) { this.size = size; }
    
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
}

// ============================================================================
// REST Controller
// ============================================================================

/**
 * Controller for file upload operations
 */
@RestController
@RequestMapping("/api/multipart-resolver")
class MultipartResolverController {
    
    private final FileUploadService fileUploadService;
    
    public MultipartResolverController(FileUploadService fileUploadService) {
        this.fileUploadService = fileUploadService;
    }
    
    /**
     * Upload single file
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            UploadResult result = fileUploadService.uploadFile(file);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (IOException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Upload failed: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * Upload multiple files
     */
    @PostMapping("/upload-multiple")
    public ResponseEntity<?> uploadMultipleFiles(
            @RequestParam("files") List<MultipartFile> files) {
        try {
            List<UploadResult> results = fileUploadService.uploadMultipleFiles(files);
            
            Map<String, Object> response = new HashMap<>();
            response.put("uploadedCount", results.size());
            response.put("files", results);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Upload failed: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * Download file
     */
    @GetMapping("/download/{filename}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable String filename) {
        try {
            byte[] data = fileUploadService.downloadFile(filename);
            return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                .body(data);
        } catch (FileNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Delete file
     */
    @DeleteMapping("/{filename}")
    public ResponseEntity<Map<String, String>> deleteFile(@PathVariable String filename) {
        boolean deleted = fileUploadService.deleteFile(filename);
        
        Map<String, String> response = new HashMap<>();
        if (deleted) {
            response.put("status", "success");
            response.put("message", "File deleted");
            return ResponseEntity.ok(response);
        } else {
            response.put("status", "error");
            response.put("message", "File not found or could not be deleted");
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * List all files
     */
    @GetMapping("/files")
    public ResponseEntity<List<FileInfo>> listFiles() {
        List<FileInfo> files = fileUploadService.listFiles();
        return ResponseEntity.ok(files);
    }
}

// ============================================================================
// Configuration
// ============================================================================

/**
 * Configuration for multipart resolution
 */
@Configuration
class MultipartResolverConfiguration {
    
    /**
     * StandardServletMultipartResolver - uses Servlet 3.0+ multipart support
     */
    @Bean
    public MultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }
    
    /**
     * Multipart configuration
     */
    @Bean
    public MultipartConfigElement multipartConfigElement() {
        return new MultipartConfigElement(
            "uploads/temp",        // Temporary location
            10 * 1024 * 1024,      // Max file size (10MB)
            50 * 1024 * 1024,      // Max request size (50MB)
            0                      // File size threshold (0 = always write to disk)
        );
    }
}
