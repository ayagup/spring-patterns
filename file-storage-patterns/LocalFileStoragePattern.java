package com.example.storage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Local File Storage Pattern
 * 
 * Purpose: Manage file storage on the local file system with Spring Boot.
 * Provides file upload, download, deletion, and metadata management.
 * 
 * Key Features:
 * - File upload with validation
 * - File download and streaming
 * - Directory management
 * - File metadata tracking
 * - Multiple storage locations
 * - File size and type validation
 * - Secure file access
 * - Storage statistics
 * 
 * Use Cases:
 * - Document management systems
 * - File upload services
 * - Media storage
 * - Backup systems
 * - Temporary file storage
 * - Development/testing environments
 * 
 * @author Spring Patterns
 */
@SpringBootApplication
public class LocalFileStoragePattern {

    public static void main(String[] args) {
        SpringApplication.run(LocalFileStoragePattern.class, args);
    }

    /**
     * Storage Configuration
     */
    @Configuration
    public static class StorageConfig {
        
        @Bean
        public StorageProperties storageProperties() {
            StorageProperties properties = new StorageProperties();
            properties.setUploadDir("uploads");
            properties.setMaxFileSize(10 * 1024 * 1024L); // 10MB
            properties.setAllowedExtensions(Arrays.asList("jpg", "jpeg", "png", "pdf", "doc", "docx"));
            return properties;
        }
    }

    /**
     * File Storage Controller
     */
    @RestController
    @RequestMapping("/api/files")
    public static class FileStorageController {

        private final LocalFileStorageService storageService;

        public FileStorageController(LocalFileStorageService storageService) {
            this.storageService = storageService;
        }

        /**
         * Upload single file
         */
        @PostMapping("/upload")
        public ResponseEntity<FileInfo> uploadFile(
                @RequestParam("file") MultipartFile file,
                @RequestParam(required = false) String category) {
            
            FileInfo fileInfo = storageService.store(file, category);
            return ResponseEntity.ok(fileInfo);
        }

        /**
         * Upload multiple files
         */
        @PostMapping("/upload/batch")
        public ResponseEntity<List<FileInfo>> uploadFiles(
                @RequestParam("files") MultipartFile[] files,
                @RequestParam(required = false) String category) {
            
            List<FileInfo> fileInfos = new ArrayList<>();
            for (MultipartFile file : files) {
                FileInfo fileInfo = storageService.store(file, category);
                fileInfos.add(fileInfo);
            }
            
            return ResponseEntity.ok(fileInfos);
        }

        /**
         * Download file
         */
        @GetMapping("/download/{filename:.+}")
        public ResponseEntity<Resource> downloadFile(@PathVariable String filename) {
            Resource resource = storageService.loadAsResource(filename);
            
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                    "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
        }

        /**
         * Get file metadata
         */
        @GetMapping("/{filename:.+}/info")
        public ResponseEntity<FileInfo> getFileInfo(@PathVariable String filename) {
            FileInfo fileInfo = storageService.getFileInfo(filename);
            return ResponseEntity.ok(fileInfo);
        }

        /**
         * List all files
         */
        @GetMapping("/list")
        public ResponseEntity<List<FileInfo>> listFiles(
                @RequestParam(required = false) String category) {
            
            List<FileInfo> files = storageService.listFiles(category);
            return ResponseEntity.ok(files);
        }

        /**
         * Delete file
         */
        @DeleteMapping("/{filename:.+}")
        public ResponseEntity<Map<String, String>> deleteFile(@PathVariable String filename) {
            storageService.delete(filename);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "File deleted successfully");
            response.put("filename", filename);
            
            return ResponseEntity.ok(response);
        }

        /**
         * Delete multiple files
         */
        @DeleteMapping("/batch")
        public ResponseEntity<Map<String, Object>> deleteFiles(
                @RequestBody List<String> filenames) {
            
            int deleted = storageService.deleteMultiple(filenames);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Files deleted successfully");
            response.put("deletedCount", deleted);
            
            return ResponseEntity.ok(response);
        }

        /**
         * Get storage statistics
         */
        @GetMapping("/stats")
        public ResponseEntity<StorageStats> getStats() {
            return ResponseEntity.ok(storageService.getStats());
        }

        /**
         * Clean up old files
         */
        @DeleteMapping("/cleanup")
        public ResponseEntity<Map<String, Object>> cleanup(
                @RequestParam(defaultValue = "30") int daysOld) {
            
            int cleaned = storageService.cleanupOldFiles(daysOld);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Cleanup completed");
            response.put("filesDeleted", cleaned);
            
            return ResponseEntity.ok(response);
        }
    }

    /**
     * Local File Storage Service
     */
    @Service
    public static class LocalFileStorageService {

        private final Path rootLocation;
        private final StorageProperties properties;
        private final Map<String, FileMetadata> fileMetadataMap = new HashMap<>();

        public LocalFileStorageService(StorageProperties properties) {
            this.properties = properties;
            this.rootLocation = Paths.get(properties.getUploadDir());
        }

        @PostConstruct
        public void init() {
            try {
                Files.createDirectories(rootLocation);
                System.out.println("Storage location initialized: " + rootLocation.toAbsolutePath());
            } catch (IOException e) {
                throw new StorageException("Could not initialize storage location", e);
            }
        }

        /**
         * Store file
         */
        public FileInfo store(MultipartFile file, String category) {
            if (file.isEmpty()) {
                throw new StorageException("Failed to store empty file");
            }

            // Validate file size
            if (file.getSize() > properties.getMaxFileSize()) {
                throw new StorageException("File size exceeds maximum allowed size");
            }

            // Validate file extension
            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);
            
            if (!properties.getAllowedExtensions().contains(extension.toLowerCase())) {
                throw new StorageException("File type not allowed: " + extension);
            }

            try {
                // Generate unique filename
                String filename = generateUniqueFilename(originalFilename);
                Path destinationPath = resolveDestination(filename, category);
                
                // Create category directory if needed
                Files.createDirectories(destinationPath.getParent());
                
                // Copy file
                try (InputStream inputStream = file.getInputStream()) {
                    Files.copy(inputStream, destinationPath, StandardCopyOption.REPLACE_EXISTING);
                }

                // Store metadata
                FileMetadata metadata = new FileMetadata(
                    filename,
                    originalFilename,
                    file.getContentType(),
                    file.getSize(),
                    category,
                    LocalDateTime.now()
                );
                fileMetadataMap.put(filename, metadata);

                return toFileInfo(destinationPath, metadata);
                
            } catch (IOException e) {
                throw new StorageException("Failed to store file", e);
            }
        }

        /**
         * Load file as resource
         */
        public Resource loadAsResource(String filename) {
            try {
                Path file = load(filename);
                Resource resource = new UrlResource(file.toUri());
                
                if (resource.exists() || resource.isReadable()) {
                    return resource;
                } else {
                    throw new StorageException("Could not read file: " + filename);
                }
            } catch (MalformedURLException e) {
                throw new StorageException("Could not read file: " + filename, e);
            }
        }

        /**
         * Load file path
         */
        public Path load(String filename) {
            FileMetadata metadata = fileMetadataMap.get(filename);
            
            if (metadata != null && metadata.getCategory() != null) {
                return rootLocation.resolve(metadata.getCategory()).resolve(filename);
            }
            
            // Try to find file in root or subdirectories
            try {
                Path filePath = rootLocation.resolve(filename);
                if (Files.exists(filePath)) {
                    return filePath;
                }
                
                // Search in subdirectories
                return findFileInSubdirectories(filename);
                
            } catch (IOException e) {
                throw new StorageException("Could not find file: " + filename, e);
            }
        }

        /**
         * Get file info
         */
        public FileInfo getFileInfo(String filename) {
            Path file = load(filename);
            FileMetadata metadata = fileMetadataMap.get(filename);
            
            return toFileInfo(file, metadata);
        }

        /**
         * List files
         */
        public List<FileInfo> listFiles(String category) {
            try {
                Path searchPath = category != null 
                    ? rootLocation.resolve(category) 
                    : rootLocation;
                
                if (!Files.exists(searchPath)) {
                    return Collections.emptyList();
                }
                
                try (Stream<Path> stream = Files.walk(searchPath, category != null ? 1 : Integer.MAX_VALUE)) {
                    return stream
                        .filter(Files::isRegularFile)
                        .map(path -> {
                            String filename = path.getFileName().toString();
                            FileMetadata metadata = fileMetadataMap.get(filename);
                            return toFileInfo(path, metadata);
                        })
                        .collect(Collectors.toList());
                }
            } catch (IOException e) {
                throw new StorageException("Failed to list files", e);
            }
        }

        /**
         * Delete file
         */
        public void delete(String filename) {
            try {
                Path file = load(filename);
                Files.deleteIfExists(file);
                fileMetadataMap.remove(filename);
            } catch (IOException e) {
                throw new StorageException("Failed to delete file: " + filename, e);
            }
        }

        /**
         * Delete multiple files
         */
        public int deleteMultiple(List<String> filenames) {
            int deleted = 0;
            for (String filename : filenames) {
                try {
                    delete(filename);
                    deleted++;
                } catch (Exception e) {
                    System.err.println("Failed to delete file: " + filename + " - " + e.getMessage());
                }
            }
            return deleted;
        }

        /**
         * Clean up old files
         */
        public int cleanupOldFiles(int daysOld) {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
            int deleted = 0;
            
            List<String> toDelete = new ArrayList<>();
            
            for (Map.Entry<String, FileMetadata> entry : fileMetadataMap.entrySet()) {
                if (entry.getValue().getUploadedAt().isBefore(cutoffDate)) {
                    toDelete.add(entry.getKey());
                }
            }
            
            return deleteMultiple(toDelete);
        }

        /**
         * Get storage statistics
         */
        public StorageStats getStats() {
            try {
                long totalFiles = 0;
                long totalSize = 0;
                Map<String, Integer> filesByCategory = new HashMap<>();
                
                try (Stream<Path> stream = Files.walk(rootLocation)) {
                    List<Path> files = stream
                        .filter(Files::isRegularFile)
                        .collect(Collectors.toList());
                    
                    totalFiles = files.size();
                    
                    for (Path file : files) {
                        totalSize += Files.size(file);
                        
                        // Count by category
                        String filename = file.getFileName().toString();
                        FileMetadata metadata = fileMetadataMap.get(filename);
                        if (metadata != null && metadata.getCategory() != null) {
                            filesByCategory.merge(metadata.getCategory(), 1, Integer::sum);
                        }
                    }
                }
                
                return new StorageStats(
                    totalFiles,
                    totalSize,
                    rootLocation.toString(),
                    filesByCategory,
                    LocalDateTime.now()
                );
                
            } catch (IOException e) {
                throw new StorageException("Failed to get storage stats", e);
            }
        }

        /**
         * Helper: Generate unique filename
         */
        private String generateUniqueFilename(String originalFilename) {
            String extension = getFileExtension(originalFilename);
            String baseName = getBaseName(originalFilename);
            String timestamp = String.valueOf(System.currentTimeMillis());
            return baseName + "_" + timestamp + "." + extension;
        }

        /**
         * Helper: Resolve destination path
         */
        private Path resolveDestination(String filename, String category) {
            if (category != null && !category.trim().isEmpty()) {
                return rootLocation.resolve(category).resolve(filename);
            }
            return rootLocation.resolve(filename);
        }

        /**
         * Helper: Find file in subdirectories
         */
        private Path findFileInSubdirectories(String filename) throws IOException {
            try (Stream<Path> stream = Files.walk(rootLocation)) {
                return stream
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().equals(filename))
                    .findFirst()
                    .orElseThrow(() -> new StorageException("File not found: " + filename));
            }
        }

        /**
         * Helper: Convert to FileInfo
         */
        private FileInfo toFileInfo(Path path, FileMetadata metadata) {
            try {
                String filename = path.getFileName().toString();
                long size = Files.size(path);
                LocalDateTime lastModified = LocalDateTime.ofInstant(
                    Files.getLastModifiedTime(path).toInstant(),
                    java.time.ZoneId.systemDefault()
                );
                
                if (metadata != null) {
                    return new FileInfo(
                        filename,
                        metadata.getOriginalFilename(),
                        metadata.getContentType(),
                        size,
                        metadata.getCategory(),
                        metadata.getUploadedAt(),
                        lastModified,
                        path.toString()
                    );
                } else {
                    return new FileInfo(
                        filename,
                        filename,
                        Files.probeContentType(path),
                        size,
                        null,
                        lastModified,
                        lastModified,
                        path.toString()
                    );
                }
            } catch (IOException e) {
                throw new StorageException("Failed to get file info", e);
            }
        }

        /**
         * Helper: Get file extension
         */
        private String getFileExtension(String filename) {
            if (filename == null || !filename.contains(".")) {
                return "";
            }
            return filename.substring(filename.lastIndexOf(".") + 1);
        }

        /**
         * Helper: Get base name
         */
        private String getBaseName(String filename) {
            if (filename == null || !filename.contains(".")) {
                return filename;
            }
            return filename.substring(0, filename.lastIndexOf("."));
        }
    }

    // Model Classes

    public static class FileInfo {
        private String filename;
        private String originalFilename;
        private String contentType;
        private long size;
        private String category;
        private LocalDateTime uploadedAt;
        private LocalDateTime lastModified;
        private String path;

        public FileInfo(String filename, String originalFilename, String contentType,
                       long size, String category, LocalDateTime uploadedAt,
                       LocalDateTime lastModified, String path) {
            this.filename = filename;
            this.originalFilename = originalFilename;
            this.contentType = contentType;
            this.size = size;
            this.category = category;
            this.uploadedAt = uploadedAt;
            this.lastModified = lastModified;
            this.path = path;
        }

        // Getters
        public String getFilename() { return filename; }
        public String getOriginalFilename() { return originalFilename; }
        public String getContentType() { return contentType; }
        public long getSize() { return size; }
        public String getCategory() { return category; }
        public LocalDateTime getUploadedAt() { return uploadedAt; }
        public LocalDateTime getLastModified() { return lastModified; }
        public String getPath() { return path; }
    }

    public static class FileMetadata {
        private String filename;
        private String originalFilename;
        private String contentType;
        private long size;
        private String category;
        private LocalDateTime uploadedAt;

        public FileMetadata(String filename, String originalFilename, String contentType,
                           long size, String category, LocalDateTime uploadedAt) {
            this.filename = filename;
            this.originalFilename = originalFilename;
            this.contentType = contentType;
            this.size = size;
            this.category = category;
            this.uploadedAt = uploadedAt;
        }

        // Getters
        public String getFilename() { return filename; }
        public String getOriginalFilename() { return originalFilename; }
        public String getContentType() { return contentType; }
        public long getSize() { return size; }
        public String getCategory() { return category; }
        public LocalDateTime getUploadedAt() { return uploadedAt; }
    }

    public static class StorageProperties {
        private String uploadDir;
        private long maxFileSize;
        private List<String> allowedExtensions;

        // Getters and Setters
        public String getUploadDir() { return uploadDir; }
        public void setUploadDir(String uploadDir) { this.uploadDir = uploadDir; }
        public long getMaxFileSize() { return maxFileSize; }
        public void setMaxFileSize(long maxFileSize) { this.maxFileSize = maxFileSize; }
        public List<String> getAllowedExtensions() { return allowedExtensions; }
        public void setAllowedExtensions(List<String> allowedExtensions) { 
            this.allowedExtensions = allowedExtensions; 
        }
    }

    public static class StorageStats {
        private long totalFiles;
        private long totalSize;
        private String storageLocation;
        private Map<String, Integer> filesByCategory;
        private LocalDateTime timestamp;

        public StorageStats(long totalFiles, long totalSize, String storageLocation,
                           Map<String, Integer> filesByCategory, LocalDateTime timestamp) {
            this.totalFiles = totalFiles;
            this.totalSize = totalSize;
            this.storageLocation = storageLocation;
            this.filesByCategory = filesByCategory;
            this.timestamp = timestamp;
        }

        // Getters
        public long getTotalFiles() { return totalFiles; }
        public long getTotalSize() { return totalSize; }
        public String getStorageLocation() { return storageLocation; }
        public Map<String, Integer> getFilesByCategory() { return filesByCategory; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }

    public static class StorageException extends RuntimeException {
        public StorageException(String message) {
            super(message);
        }

        public StorageException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}

/*
 * Application Properties:
 * 
 * # File Upload Configuration
 * spring.servlet.multipart.enabled=true
 * spring.servlet.multipart.max-file-size=10MB
 * spring.servlet.multipart.max-request-size=50MB
 * 
 * # Storage Configuration
 * storage.upload-dir=uploads
 * storage.max-file-size=10485760
 * storage.allowed-extensions=jpg,jpeg,png,pdf,doc,docx,txt
 * 
 * # Server Configuration
 * server.port=8080
 */

/*
 * HTML Upload Form Example:
 * 
 * <!DOCTYPE html>
 * <html>
 * <head>
 *     <title>File Upload</title>
 * </head>
 * <body>
 *     <h1>File Upload</h1>
 *     
 *     <form id="uploadForm" enctype="multipart/form-data">
 *         <input type="file" name="file" id="fileInput" required>
 *         <input type="text" name="category" placeholder="Category (optional)">
 *         <button type="submit">Upload</button>
 *     </form>
 *     
 *     <div id="result"></div>
 *     
 *     <script>
 *         document.getElementById('uploadForm').addEventListener('submit', async (e) => {
 *             e.preventDefault();
 *             
 *             const formData = new FormData(e.target);
 *             
 *             try {
 *                 const response = await fetch('/api/files/upload', {
 *                     method: 'POST',
 *                     body: formData
 *                 });
 *                 
 *                 const result = await response.json();
 *                 document.getElementById('result').innerHTML = 
 *                     `<p>File uploaded: ${result.filename}</p>`;
 *             } catch (error) {
 *                 console.error('Upload failed:', error);
 *             }
 *         });
 *     </script>
 * </body>
 * </html>
 */
