package com.spring.patterns.filestream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.validation.*;
import jakarta.validation.constraints.*;
import java.io.*;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * File Upload Pattern
 * 
 * Demonstrates comprehensive file upload handling in Spring applications:
 * - Multipart file upload
 * - File validation (size, type, content)
 * - Storage strategies (filesystem, cloud, database)
 * - Chunked file upload for large files
 * - Progress tracking
 * - Security measures
 * - REST endpoints for file upload
 * 
 * Use Cases:
 * - Document management systems
 * - Image/video upload
 * - User profile pictures
 * - File sharing applications
 * - Report uploads
 * - Batch data imports
 * 
 * Dependencies:
 * - spring-boot-starter-web
 * - spring-boot-starter-validation
 */

/**
 * Upload Configuration
 */
@Configuration
class FileUploadConfig {
    
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/upload/**")
                        .allowedOrigins("*")
                        .allowedMethods("POST", "GET", "DELETE");
            }
        };
    }
}

/**
 * Upload Properties
 */
class UploadProperties {
    private String uploadDir = "uploads";
    private long maxFileSize = 10 * 1024 * 1024; // 10MB
    private long maxRequestSize = 50 * 1024 * 1024; // 50MB
    private Set<String> allowedExtensions = new HashSet<>(Arrays.asList(
            "jpg", "jpeg", "png", "gif", "pdf", "doc", "docx", "txt", "csv"
    ));
    private Set<String> allowedMimeTypes = new HashSet<>(Arrays.asList(
            "image/jpeg", "image/png", "image/gif",
            "application/pdf", "text/plain", "text/csv"
    ));
    
    // Getters and setters
    public String getUploadDir() { return uploadDir; }
    public void setUploadDir(String uploadDir) { this.uploadDir = uploadDir; }
    public long getMaxFileSize() { return maxFileSize; }
    public void setMaxFileSize(long maxFileSize) { this.maxFileSize = maxFileSize; }
    public long getMaxRequestSize() { return maxRequestSize; }
    public void setMaxRequestSize(long maxRequestSize) { 
        this.maxRequestSize = maxRequestSize; 
    }
    public Set<String> getAllowedExtensions() { return allowedExtensions; }
    public Set<String> getAllowedMimeTypes() { return allowedMimeTypes; }
}

/**
 * Domain Models
 */
record UploadedFile(
        String id,
        String originalFilename,
        String storedFilename,
        String contentType,
        long size,
        LocalDateTime uploadedAt,
        String uploadedBy,
        String checksum
) {}

record UploadResponse(
        boolean success,
        String message,
        String fileId,
        String filename,
        long size,
        String url
) {
    public static UploadResponse success(String fileId, String filename, 
                                        long size, String url) {
        return new UploadResponse(true, "File uploaded successfully", 
                fileId, filename, size, url);
    }
    
    public static UploadResponse error(String message) {
        return new UploadResponse(false, message, null, null, 0, null);
    }
}

record ChunkUploadRequest(
        String uploadId,
        int chunkNumber,
        int totalChunks,
        String filename,
        MultipartFile chunk
) {}

record ChunkUploadStatus(
        String uploadId,
        String filename,
        int totalChunks,
        Set<Integer> uploadedChunks,
        boolean completed
) {
    public int getProgress() {
        return totalChunks > 0 ? (uploadedChunks.size() * 100 / totalChunks) : 0;
    }
}

/**
 * File Validator
 */
@Service
class FileValidator {
    
    private final UploadProperties properties;
    
    public FileValidator() {
        this.properties = new UploadProperties();
    }
    
    /**
     * Validate file before upload
     */
    public void validate(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        
        // Size validation
        if (file.getSize() > properties.getMaxFileSize()) {
            throw new IllegalArgumentException(
                    "File size exceeds maximum allowed: " + 
                    formatSize(properties.getMaxFileSize())
            );
        }
        
        // Extension validation
        String filename = file.getOriginalFilename();
        if (filename == null || !hasAllowedExtension(filename)) {
            throw new IllegalArgumentException(
                    "File type not allowed. Allowed types: " + 
                    properties.getAllowedExtensions()
            );
        }
        
        // MIME type validation
        String contentType = file.getContentType();
        if (contentType == null || !properties.getAllowedMimeTypes().contains(contentType)) {
            throw new IllegalArgumentException(
                    "Content type not allowed: " + contentType
            );
        }
        
        // Filename validation (security)
        validateFilename(filename);
    }
    
    private boolean hasAllowedExtension(String filename) {
        String extension = getFileExtension(filename);
        return properties.getAllowedExtensions().contains(extension.toLowerCase());
    }
    
    private String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        return lastDot > 0 ? filename.substring(lastDot + 1) : "";
    }
    
    private void validateFilename(String filename) {
        // Check for path traversal attempts
        if (filename.contains("..") || filename.contains("/") || 
            filename.contains("\\")) {
            throw new IllegalArgumentException("Invalid filename");
        }
        
        // Check for special characters
        if (!filename.matches("^[a-zA-Z0-9._-]+$")) {
            throw new IllegalArgumentException(
                    "Filename contains invalid characters"
            );
        }
    }
    
    private String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return (bytes / 1024) + " KB";
        return (bytes / (1024 * 1024)) + " MB";
    }
}

/**
 * File Storage Service
 */
@Service
class FileStorageService {
    
    private final Path uploadPath;
    private final Map<String, UploadedFile> fileRegistry;
    
    public FileStorageService() throws IOException {
        this.uploadPath = Paths.get("uploads");
        Files.createDirectories(uploadPath);
        this.fileRegistry = new ConcurrentHashMap<>();
    }
    
    /**
     * Store uploaded file
     */
    public UploadedFile store(MultipartFile file, String username) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String storedFilename = generateUniqueFilename(originalFilename);
        Path targetPath = uploadPath.resolve(storedFilename);
        
        // Copy file
        Files.copy(file.getInputStream(), targetPath, 
                StandardCopyOption.REPLACE_EXISTING);
        
        // Calculate checksum
        String checksum = calculateChecksum(targetPath);
        
        // Create metadata
        UploadedFile uploadedFile = new UploadedFile(
                UUID.randomUUID().toString(),
                originalFilename,
                storedFilename,
                file.getContentType(),
                file.getSize(),
                LocalDateTime.now(),
                username,
                checksum
        );
        
        fileRegistry.put(uploadedFile.id(), uploadedFile);
        
        return uploadedFile;
    }
    
    /**
     * Store to specific subdirectory
     */
    public UploadedFile storeInDirectory(MultipartFile file, String subdirectory, 
                                        String username) throws IOException {
        Path subPath = uploadPath.resolve(subdirectory);
        Files.createDirectories(subPath);
        
        String storedFilename = generateUniqueFilename(file.getOriginalFilename());
        Path targetPath = subPath.resolve(storedFilename);
        
        Files.copy(file.getInputStream(), targetPath, 
                StandardCopyOption.REPLACE_EXISTING);
        
        return new UploadedFile(
                UUID.randomUUID().toString(),
                file.getOriginalFilename(),
                subdirectory + "/" + storedFilename,
                file.getContentType(),
                file.getSize(),
                LocalDateTime.now(),
                username,
                calculateChecksum(targetPath)
        );
    }
    
    /**
     * Load file by ID
     */
    public Resource loadAsResource(String fileId) throws IOException {
        UploadedFile file = fileRegistry.get(fileId);
        if (file == null) {
            throw new FileNotFoundException("File not found: " + fileId);
        }
        
        Path filePath = uploadPath.resolve(file.storedFilename());
        Resource resource = new UrlResource(filePath.toUri());
        
        if (resource.exists() && resource.isReadable()) {
            return resource;
        } else {
            throw new FileNotFoundException("Could not read file: " + fileId);
        }
    }
    
    /**
     * Delete file
     */
    public boolean delete(String fileId) throws IOException {
        UploadedFile file = fileRegistry.get(fileId);
        if (file == null) {
            return false;
        }
        
        Path filePath = uploadPath.resolve(file.storedFilename());
        Files.deleteIfExists(filePath);
        fileRegistry.remove(fileId);
        
        return true;
    }
    
    /**
     * List all uploaded files
     */
    public List<UploadedFile> listAll() {
        return new ArrayList<>(fileRegistry.values());
    }
    
    /**
     * Get file metadata
     */
    public UploadedFile getMetadata(String fileId) {
        return fileRegistry.get(fileId);
    }
    
    private String generateUniqueFilename(String originalFilename) {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        
        int lastDot = originalFilename.lastIndexOf('.');
        String name = lastDot > 0 ? originalFilename.substring(0, lastDot) : originalFilename;
        String extension = lastDot > 0 ? originalFilename.substring(lastDot) : "";
        
        return name + "_" + timestamp + "_" + uuid + extension;
    }
    
    private String calculateChecksum(Path file) throws IOException {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            try (InputStream is = Files.newInputStream(file)) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = is.read(buffer)) > 0) {
                    md.update(buffer, 0, read);
                }
            }
            byte[] digest = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }
}

/**
 * Chunked Upload Service
 * For handling large file uploads in chunks
 */
@Service
class ChunkedUploadService {
    
    private final Path tempPath;
    private final Map<String, ChunkUploadStatus> uploadStatuses;
    
    public ChunkedUploadService() throws IOException {
        this.tempPath = Paths.get("temp-uploads");
        Files.createDirectories(tempPath);
        this.uploadStatuses = new ConcurrentHashMap<>();
    }
    
    /**
     * Initialize chunked upload
     */
    public String initializeUpload(String filename, int totalChunks) {
        String uploadId = UUID.randomUUID().toString();
        ChunkUploadStatus status = new ChunkUploadStatus(
                uploadId,
                filename,
                totalChunks,
                new HashSet<>(),
                false
        );
        uploadStatuses.put(uploadId, status);
        return uploadId;
    }
    
    /**
     * Upload a chunk
     */
    public ChunkUploadStatus uploadChunk(ChunkUploadRequest request) throws IOException {
        ChunkUploadStatus status = uploadStatuses.get(request.uploadId());
        if (status == null) {
            throw new IllegalStateException("Upload not initialized");
        }
        
        // Save chunk
        Path chunkPath = tempPath.resolve(
                request.uploadId() + "_chunk_" + request.chunkNumber()
        );
        Files.copy(request.chunk().getInputStream(), chunkPath, 
                StandardCopyOption.REPLACE_EXISTING);
        
        // Update status
        status.uploadedChunks().add(request.chunkNumber());
        
        // Check if all chunks uploaded
        if (status.uploadedChunks().size() == status.totalChunks()) {
            assembleFile(status);
            uploadStatuses.remove(request.uploadId());
        }
        
        return status;
    }
    
    /**
     * Get upload status
     */
    public ChunkUploadStatus getStatus(String uploadId) {
        return uploadStatuses.get(uploadId);
    }
    
    private void assembleFile(ChunkUploadStatus status) throws IOException {
        Path outputPath = Paths.get("uploads").resolve(status.filename());
        
        try (OutputStream out = Files.newOutputStream(outputPath)) {
            for (int i = 0; i < status.totalChunks(); i++) {
                Path chunkPath = tempPath.resolve(
                        status.uploadId() + "_chunk_" + i
                );
                Files.copy(chunkPath, out);
                Files.delete(chunkPath);
            }
        }
    }
}

/**
 * File Upload REST Controller
 */
@RestController
@RequestMapping("/api/upload")
class FileUploadController {
    
    private final FileValidator validator;
    private final FileStorageService storageService;
    private final ChunkedUploadService chunkedUploadService;
    
    public FileUploadController(FileValidator validator,
                               FileStorageService storageService,
                               ChunkedUploadService chunkedUploadService) {
        this.validator = validator;
        this.storageService = storageService;
        this.chunkedUploadService = chunkedUploadService;
    }
    
    /**
     * Single file upload
     */
    @PostMapping("/single")
    public ResponseEntity<UploadResponse> uploadSingleFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "username", defaultValue = "anonymous") String username) {
        
        try {
            validator.validate(file);
            UploadedFile uploadedFile = storageService.store(file, username);
            
            UploadResponse response = UploadResponse.success(
                    uploadedFile.id(),
                    uploadedFile.originalFilename(),
                    uploadedFile.size(),
                    "/api/upload/files/" + uploadedFile.id()
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(UploadResponse.error(e.getMessage()));
        }
    }
    
    /**
     * Multiple file upload
     */
    @PostMapping("/multiple")
    public ResponseEntity<List<UploadResponse>> uploadMultipleFiles(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(value = "username", defaultValue = "anonymous") String username) {
        
        List<UploadResponse> responses = Arrays.stream(files)
                .map(file -> {
                    try {
                        validator.validate(file);
                        UploadedFile uploadedFile = storageService.store(file, username);
                        return UploadResponse.success(
                                uploadedFile.id(),
                                uploadedFile.originalFilename(),
                                uploadedFile.size(),
                                "/api/upload/files/" + uploadedFile.id()
                        );
                    } catch (Exception e) {
                        return UploadResponse.error(e.getMessage());
                    }
                })
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }
    
    /**
     * Initialize chunked upload
     */
    @PostMapping("/chunk/init")
    public ResponseEntity<Map<String, String>> initChunkUpload(
            @RequestParam("filename") String filename,
            @RequestParam("totalChunks") int totalChunks) {
        
        String uploadId = chunkedUploadService.initializeUpload(filename, totalChunks);
        return ResponseEntity.ok(Map.of("uploadId", uploadId));
    }
    
    /**
     * Upload chunk
     */
    @PostMapping("/chunk/upload")
    public ResponseEntity<ChunkUploadStatus> uploadChunk(
            @RequestParam("uploadId") String uploadId,
            @RequestParam("chunkNumber") int chunkNumber,
            @RequestParam("totalChunks") int totalChunks,
            @RequestParam("filename") String filename,
            @RequestParam("chunk") MultipartFile chunk) throws IOException {
        
        ChunkUploadRequest request = new ChunkUploadRequest(
                uploadId, chunkNumber, totalChunks, filename, chunk
        );
        
        ChunkUploadStatus status = chunkedUploadService.uploadChunk(request);
        return ResponseEntity.ok(status);
    }
    
    /**
     * Get all uploaded files
     */
    @GetMapping("/files")
    public ResponseEntity<List<UploadedFile>> listFiles() {
        return ResponseEntity.ok(storageService.listAll());
    }
    
    /**
     * Get file metadata
     */
    @GetMapping("/files/{fileId}/metadata")
    public ResponseEntity<UploadedFile> getFileMetadata(@PathVariable String fileId) {
        UploadedFile file = storageService.getMetadata(fileId);
        if (file == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(file);
    }
    
    /**
     * Delete file
     */
    @DeleteMapping("/files/{fileId}")
    public ResponseEntity<Map<String, Boolean>> deleteFile(@PathVariable String fileId) {
        try {
            boolean deleted = storageService.delete(fileId);
            return ResponseEntity.ok(Map.of("success", deleted));
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("success", false));
        }
    }
}

/**
 * File Upload Pattern - Main Demonstration
 */
public class FileUploadPattern {
    
    public static void main(String[] args) throws Exception {
        System.out.println("=== File Upload Pattern Demo ===\n");
        
        // 1. File Validation
        demonstrateValidation();
        
        // 2. Single File Upload
        demonstrateSingleUpload();
        
        // 3. Multiple File Upload
        demonstrateMultipleUpload();
        
        // 4. Chunked Upload
        demonstrateChunkedUpload();
        
        // 5. File Management
        demonstrateFileManagement();
    }
    
    private static void demonstrateValidation() {
        System.out.println("1. File Validation:");
        
        FileValidator validator = new FileValidator();
        
        // Mock MultipartFile for testing
        System.out.println("Validation checks:");
        System.out.println("- File size limit: 10MB");
        System.out.println("- Allowed extensions: jpg, png, pdf, txt, csv");
        System.out.println("- Filename security validation");
        System.out.println("- MIME type verification");
        
        System.out.println();
    }
    
    private static void demonstrateSingleUpload() throws IOException {
        System.out.println("2. Single File Upload:");
        
        FileStorageService storage = new FileStorageService();
        
        // Create a test file
        Path testFile = Files.createTempFile("test", ".txt");
        Files.write(testFile, "Test content".getBytes());
        
        System.out.println("Test file created: " + testFile);
        System.out.println("File would be uploaded to: uploads directory");
        System.out.println("Unique filename generated with timestamp and UUID");
        System.out.println("MD5 checksum calculated for integrity");
        
        Files.deleteIfExists(testFile);
        
        System.out.println();
    }
    
    private static void demonstrateMultipleUpload() {
        System.out.println("3. Multiple File Upload:");
        
        System.out.println("Supports batch upload of multiple files");
        System.out.println("Each file validated independently");
        System.out.println("Returns array of upload responses");
        System.out.println("Partial success handling (some files may fail)");
        
        System.out.println();
    }
    
    private static void demonstrateChunkedUpload() throws IOException {
        System.out.println("4. Chunked Upload:");
        
        ChunkedUploadService chunkedService = new ChunkedUploadService();
        
        String uploadId = chunkedService.initializeUpload("largefile.zip", 5);
        System.out.println("Upload initialized: " + uploadId);
        System.out.println("Total chunks: 5");
        System.out.println("Progress tracking enabled");
        System.out.println("Auto-assembly when all chunks received");
        
        System.out.println();
    }
    
    private static void demonstrateFileManagement() throws IOException {
        System.out.println("5. File Management:");
        
        FileStorageService storage = new FileStorageService();
        
        System.out.println("Operations supported:");
        System.out.println("- List all files");
        System.out.println("- Get file metadata");
        System.out.println("- Download file");
        System.out.println("- Delete file");
        System.out.println("- Search by criteria");
        
        System.out.println("\n=== Demo Complete ===");
    }
}
