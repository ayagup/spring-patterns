package com.example.storage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Cloud Storage Pattern
 * 
 * Purpose: Abstract cloud storage interface supporting multiple providers
 * (AWS S3, Azure Blob, Google Cloud Storage) with unified API.
 * 
 * Key Features:
 * - Provider abstraction
 * - Unified storage interface
 * - Multiple provider support
 * - Metadata management
 * - Streaming upload/download
 * - Access control
 * - Provider switching
 * - Storage analytics
 * 
 * Use Cases:
 * - Multi-cloud applications
 * - Cloud-agnostic storage
 * - Provider migration
 * - Hybrid cloud storage
 * - Disaster recovery
 * - Geographic distribution
 * 
 * @author Spring Patterns
 */
@SpringBootApplication
public class CloudStoragePattern {

    public static void main(String[] args) {
        SpringApplication.run(CloudStoragePattern.class, args);
    }

    /**
     * Cloud Storage Configuration
     */
    @Configuration
    public static class CloudStorageConfig {
        
        @Bean
        public CloudStorageManager cloudStorageManager() {
            return new CloudStorageManager();
        }

        @Bean
        public CloudStorageService cloudStorageService(CloudStorageManager manager) {
            return new CloudStorageService(manager);
        }
    }

    /**
     * Cloud Storage Controller
     */
    @RestController
    @RequestMapping("/api/cloud-storage")
    public static class CloudStorageController {

        private final CloudStorageService storageService;

        public CloudStorageController(CloudStorageService storageService) {
            this.storageService = storageService;
        }

        /**
         * Upload file to cloud
         */
        @PostMapping("/upload")
        public ResponseEntity<CloudFileInfo> uploadFile(
                @RequestParam("file") MultipartFile file,
                @RequestParam(required = false, defaultValue = "default") String provider,
                @RequestParam(required = false) String bucket,
                @RequestParam(required = false) Map<String, String> metadata) {
            
            try {
                CloudFileInfo fileInfo = storageService.uploadFile(
                    provider, bucket, file, metadata
                );
                return ResponseEntity.ok(fileInfo);
            } catch (IOException e) {
                throw new CloudStorageException("Upload failed", e);
            }
        }

        /**
         * Download file from cloud
         */
        @GetMapping("/download/{provider}/{bucket}/{key:.+}")
        public ResponseEntity<byte[]> downloadFile(
                @PathVariable String provider,
                @PathVariable String bucket,
                @PathVariable String key) {
            
            try {
                CloudFile file = storageService.downloadFile(provider, bucket, key);
                
                return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"" + file.getKey() + "\"")
                    .header("Content-Type", file.getContentType())
                    .body(file.getData());
            } catch (IOException e) {
                throw new CloudStorageException("Download failed", e);
            }
        }

        /**
         * Get file metadata
         */
        @GetMapping("/{provider}/{bucket}/{key:.+}/metadata")
        public ResponseEntity<CloudFileInfo> getMetadata(
                @PathVariable String provider,
                @PathVariable String bucket,
                @PathVariable String key) {
            
            CloudFileInfo info = storageService.getFileMetadata(provider, bucket, key);
            return ResponseEntity.ok(info);
        }

        /**
         * List files in bucket
         */
        @GetMapping("/{provider}/{bucket}/list")
        public ResponseEntity<List<CloudFileInfo>> listFiles(
                @PathVariable String provider,
                @PathVariable String bucket,
                @RequestParam(required = false) String prefix) {
            
            List<CloudFileInfo> files = storageService.listFiles(provider, bucket, prefix);
            return ResponseEntity.ok(files);
        }

        /**
         * Delete file
         */
        @DeleteMapping("/{provider}/{bucket}/{key:.+}")
        public ResponseEntity<Map<String, String>> deleteFile(
                @PathVariable String provider,
                @PathVariable String bucket,
                @PathVariable String key) {
            
            storageService.deleteFile(provider, bucket, key);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "File deleted successfully");
            response.put("provider", provider);
            response.put("bucket", bucket);
            response.put("key", key);
            
            return ResponseEntity.ok(response);
        }

        /**
         * Copy file between providers
         */
        @PostMapping("/copy")
        public ResponseEntity<CloudFileInfo> copyFile(
                @RequestBody CopyRequest request) {
            
            try {
                CloudFileInfo info = storageService.copyFile(
                    request.getSourceProvider(),
                    request.getSourceBucket(),
                    request.getSourceKey(),
                    request.getDestProvider(),
                    request.getDestBucket(),
                    request.getDestKey()
                );
                return ResponseEntity.ok(info);
            } catch (IOException e) {
                throw new CloudStorageException("Copy failed", e);
            }
        }

        /**
         * Get storage statistics
         */
        @GetMapping("/stats/{provider}")
        public ResponseEntity<ProviderStats> getProviderStats(@PathVariable String provider) {
            return ResponseEntity.ok(storageService.getProviderStats(provider));
        }

        /**
         * List all providers
         */
        @GetMapping("/providers")
        public ResponseEntity<List<ProviderInfo>> listProviders() {
            return ResponseEntity.ok(storageService.listProviders());
        }

        /**
         * Configure provider
         */
        @PostMapping("/providers/{provider}/configure")
        public ResponseEntity<Map<String, String>> configureProvider(
                @PathVariable String provider,
                @RequestBody CloudStorageConfig config) {
            
            storageService.configureProvider(provider, config);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Provider configured successfully");
            response.put("provider", provider);
            
            return ResponseEntity.ok(response);
        }
    }

    /**
     * Cloud Storage Service
     */
    @Service
    public static class CloudStorageService {

        private final CloudStorageManager manager;

        public CloudStorageService(CloudStorageManager manager) {
            this.manager = manager;
        }

        /**
         * Upload file
         */
        public CloudFileInfo uploadFile(String providerName, String bucket, 
                                       MultipartFile file, Map<String, String> metadata) throws IOException {
            
            CloudStorageProvider provider = manager.getProvider(providerName);
            
            if (bucket == null || bucket.isEmpty()) {
                bucket = provider.getDefaultBucket();
            }
            
            String key = generateKey(file.getOriginalFilename());
            
            try (InputStream inputStream = file.getInputStream()) {
                return provider.upload(bucket, key, inputStream, 
                    file.getContentType(), metadata);
            }
        }

        /**
         * Download file
         */
        public CloudFile downloadFile(String providerName, String bucket, String key) 
                throws IOException {
            
            CloudStorageProvider provider = manager.getProvider(providerName);
            return provider.download(bucket, key);
        }

        /**
         * Get file metadata
         */
        public CloudFileInfo getFileMetadata(String providerName, String bucket, String key) {
            CloudStorageProvider provider = manager.getProvider(providerName);
            return provider.getMetadata(bucket, key);
        }

        /**
         * List files
         */
        public List<CloudFileInfo> listFiles(String providerName, String bucket, String prefix) {
            CloudStorageProvider provider = manager.getProvider(providerName);
            return provider.listFiles(bucket, prefix);
        }

        /**
         * Delete file
         */
        public void deleteFile(String providerName, String bucket, String key) {
            CloudStorageProvider provider = manager.getProvider(providerName);
            provider.delete(bucket, key);
        }

        /**
         * Copy file between providers
         */
        public CloudFileInfo copyFile(String sourceProvider, String sourceBucket, String sourceKey,
                                     String destProvider, String destBucket, String destKey) 
                throws IOException {
            
            // Download from source
            CloudFile file = downloadFile(sourceProvider, sourceBucket, sourceKey);
            
            // Upload to destination
            CloudStorageProvider provider = manager.getProvider(destProvider);
            
            try (InputStream inputStream = new ByteArrayInputStream(file.getData())) {
                return provider.upload(destBucket, destKey, inputStream, 
                    file.getContentType(), file.getMetadata());
            }
        }

        /**
         * Get provider statistics
         */
        public ProviderStats getProviderStats(String providerName) {
            CloudStorageProvider provider = manager.getProvider(providerName);
            return provider.getStats();
        }

        /**
         * List all providers
         */
        public List<ProviderInfo> listProviders() {
            return manager.listProviders();
        }

        /**
         * Configure provider
         */
        public void configureProvider(String providerName, CloudStorageConfig config) {
            manager.configureProvider(providerName, config);
        }

        /**
         * Generate unique key
         */
        private String generateKey(String filename) {
            String timestamp = String.valueOf(System.currentTimeMillis());
            return timestamp + "_" + filename;
        }
    }

    /**
     * Cloud Storage Manager
     */
    public static class CloudStorageManager {
        
        private final Map<String, CloudStorageProvider> providers = new ConcurrentHashMap<>();

        public CloudStorageManager() {
            // Register default providers
            registerProvider("mock", new MockCloudStorageProvider("mock"));
            registerProvider("default", new MockCloudStorageProvider("default"));
        }

        public void registerProvider(String name, CloudStorageProvider provider) {
            providers.put(name, provider);
        }

        public CloudStorageProvider getProvider(String name) {
            CloudStorageProvider provider = providers.get(name);
            if (provider == null) {
                throw new CloudStorageException("Provider not found: " + name);
            }
            return provider;
        }

        public List<ProviderInfo> listProviders() {
            return providers.entrySet().stream()
                .map(e -> new ProviderInfo(
                    e.getKey(),
                    e.getValue().getClass().getSimpleName(),
                    e.getValue().isConfigured()
                ))
                .collect(Collectors.toList());
        }

        public void configureProvider(String name, CloudStorageConfig config) {
            CloudStorageProvider provider = getProvider(name);
            provider.configure(config);
        }
    }

    /**
     * Cloud Storage Provider Interface
     */
    public interface CloudStorageProvider {
        
        CloudFileInfo upload(String bucket, String key, InputStream data, 
                           String contentType, Map<String, String> metadata) throws IOException;
        
        CloudFile download(String bucket, String key) throws IOException;
        
        CloudFileInfo getMetadata(String bucket, String key);
        
        List<CloudFileInfo> listFiles(String bucket, String prefix);
        
        void delete(String bucket, String key);
        
        String getDefaultBucket();
        
        boolean isConfigured();
        
        void configure(CloudStorageConfig config);
        
        ProviderStats getStats();
    }

    /**
     * Mock Cloud Storage Provider (for demonstration)
     */
    public static class MockCloudStorageProvider implements CloudStorageProvider {
        
        private final String providerName;
        private final Map<String, Map<String, CloudFile>> storage = new ConcurrentHashMap<>();
        private boolean configured = true;
        private long totalUploads = 0;
        private long totalDownloads = 0;
        private long totalDeletes = 0;

        public MockCloudStorageProvider(String providerName) {
            this.providerName = providerName;
        }

        @Override
        public CloudFileInfo upload(String bucket, String key, InputStream data,
                                   String contentType, Map<String, String> metadata) throws IOException {
            
            byte[] bytes = data.readAllBytes();
            
            CloudFile file = new CloudFile(
                bucket,
                key,
                bytes,
                contentType,
                metadata != null ? metadata : new HashMap<>(),
                LocalDateTime.now()
            );
            
            storage.computeIfAbsent(bucket, k -> new ConcurrentHashMap<>())
                .put(key, file);
            
            totalUploads++;
            
            return toFileInfo(file);
        }

        @Override
        public CloudFile download(String bucket, String key) throws IOException {
            Map<String, CloudFile> bucketStorage = storage.get(bucket);
            
            if (bucketStorage == null) {
                throw new CloudStorageException("Bucket not found: " + bucket);
            }
            
            CloudFile file = bucketStorage.get(key);
            
            if (file == null) {
                throw new CloudStorageException("File not found: " + key);
            }
            
            totalDownloads++;
            return file;
        }

        @Override
        public CloudFileInfo getMetadata(String bucket, String key) {
            try {
                CloudFile file = download(bucket, key);
                return toFileInfo(file);
            } catch (IOException e) {
                throw new CloudStorageException("Failed to get metadata", e);
            }
        }

        @Override
        public List<CloudFileInfo> listFiles(String bucket, String prefix) {
            Map<String, CloudFile> bucketStorage = storage.get(bucket);
            
            if (bucketStorage == null) {
                return Collections.emptyList();
            }
            
            return bucketStorage.values().stream()
                .filter(file -> prefix == null || file.getKey().startsWith(prefix))
                .map(this::toFileInfo)
                .collect(Collectors.toList());
        }

        @Override
        public void delete(String bucket, String key) {
            Map<String, CloudFile> bucketStorage = storage.get(bucket);
            
            if (bucketStorage != null) {
                bucketStorage.remove(key);
                totalDeletes++;
            }
        }

        @Override
        public String getDefaultBucket() {
            return "default-bucket";
        }

        @Override
        public boolean isConfigured() {
            return configured;
        }

        @Override
        public void configure(CloudStorageConfig config) {
            this.configured = true;
        }

        @Override
        public ProviderStats getStats() {
            long totalFiles = storage.values().stream()
                .mapToLong(Map::size)
                .sum();
            
            long totalSize = storage.values().stream()
                .flatMap(m -> m.values().stream())
                .mapToLong(f -> f.getData().length)
                .sum();
            
            return new ProviderStats(
                providerName,
                totalFiles,
                totalSize,
                totalUploads,
                totalDownloads,
                totalDeletes,
                storage.size(),
                LocalDateTime.now()
            );
        }

        private CloudFileInfo toFileInfo(CloudFile file) {
            return new CloudFileInfo(
                file.getBucket(),
                file.getKey(),
                file.getData().length,
                file.getContentType(),
                file.getMetadata(),
                file.getUploadedAt(),
                providerName
            );
        }
    }

    // Model Classes

    public static class CloudFileInfo {
        private String bucket;
        private String key;
        private long size;
        private String contentType;
        private Map<String, String> metadata;
        private LocalDateTime uploadedAt;
        private String provider;

        public CloudFileInfo(String bucket, String key, long size, String contentType,
                           Map<String, String> metadata, LocalDateTime uploadedAt, String provider) {
            this.bucket = bucket;
            this.key = key;
            this.size = size;
            this.contentType = contentType;
            this.metadata = metadata;
            this.uploadedAt = uploadedAt;
            this.provider = provider;
        }

        // Getters
        public String getBucket() { return bucket; }
        public String getKey() { return key; }
        public long getSize() { return size; }
        public String getContentType() { return contentType; }
        public Map<String, String> getMetadata() { return metadata; }
        public LocalDateTime getUploadedAt() { return uploadedAt; }
        public String getProvider() { return provider; }
    }

    public static class CloudFile {
        private String bucket;
        private String key;
        private byte[] data;
        private String contentType;
        private Map<String, String> metadata;
        private LocalDateTime uploadedAt;

        public CloudFile(String bucket, String key, byte[] data, String contentType,
                        Map<String, String> metadata, LocalDateTime uploadedAt) {
            this.bucket = bucket;
            this.key = key;
            this.data = data;
            this.contentType = contentType;
            this.metadata = metadata;
            this.uploadedAt = uploadedAt;
        }

        // Getters
        public String getBucket() { return bucket; }
        public String getKey() { return key; }
        public byte[] getData() { return data; }
        public String getContentType() { return contentType; }
        public Map<String, String> metadata() { return metadata; }
        public Map<String, String> getMetadata() { return metadata; }
        public LocalDateTime getUploadedAt() { return uploadedAt; }
    }

    public static class CopyRequest {
        private String sourceProvider;
        private String sourceBucket;
        private String sourceKey;
        private String destProvider;
        private String destBucket;
        private String destKey;

        // Getters and Setters
        public String getSourceProvider() { return sourceProvider; }
        public void setSourceProvider(String sourceProvider) { this.sourceProvider = sourceProvider; }
        public String getSourceBucket() { return sourceBucket; }
        public void setSourceBucket(String sourceBucket) { this.sourceBucket = sourceBucket; }
        public String getSourceKey() { return sourceKey; }
        public void setSourceKey(String sourceKey) { this.sourceKey = sourceKey; }
        public String getDestProvider() { return destProvider; }
        public void setDestProvider(String destProvider) { this.destProvider = destProvider; }
        public String getDestBucket() { return destBucket; }
        public void setDestBucket(String destBucket) { this.destBucket = destBucket; }
        public String getDestKey() { return destKey; }
        public void setDestKey(String destKey) { this.destKey = destKey; }
    }

    public static class CloudStorageConfig {
        private String accessKey;
        private String secretKey;
        private String region;
        private String endpoint;
        private Map<String, String> properties;

        // Getters and Setters
        public String getAccessKey() { return accessKey; }
        public void setAccessKey(String accessKey) { this.accessKey = accessKey; }
        public String getSecretKey() { return secretKey; }
        public void setSecretKey(String secretKey) { this.secretKey = secretKey; }
        public String getRegion() { return region; }
        public void setRegion(String region) { this.region = region; }
        public String getEndpoint() { return endpoint; }
        public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
        public Map<String, String> getProperties() { return properties; }
        public void setProperties(Map<String, String> properties) { this.properties = properties; }
    }

    public static class ProviderInfo {
        private String name;
        private String type;
        private boolean configured;

        public ProviderInfo(String name, String type, boolean configured) {
            this.name = name;
            this.type = type;
            this.configured = configured;
        }

        // Getters
        public String getName() { return name; }
        public String getType() { return type; }
        public boolean isConfigured() { return configured; }
    }

    public static class ProviderStats {
        private String providerName;
        private long totalFiles;
        private long totalSize;
        private long totalUploads;
        private long totalDownloads;
        private long totalDeletes;
        private int bucketCount;
        private LocalDateTime timestamp;

        public ProviderStats(String providerName, long totalFiles, long totalSize,
                           long totalUploads, long totalDownloads, long totalDeletes,
                           int bucketCount, LocalDateTime timestamp) {
            this.providerName = providerName;
            this.totalFiles = totalFiles;
            this.totalSize = totalSize;
            this.totalUploads = totalUploads;
            this.totalDownloads = totalDownloads;
            this.totalDeletes = totalDeletes;
            this.bucketCount = bucketCount;
            this.timestamp = timestamp;
        }

        // Getters
        public String getProviderName() { return providerName; }
        public long getTotalFiles() { return totalFiles; }
        public long getTotalSize() { return totalSize; }
        public long getTotalUploads() { return totalUploads; }
        public long getTotalDownloads() { return totalDownloads; }
        public long getTotalDeletes() { return totalDeletes; }
        public int getBucketCount() { return bucketCount; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }

    public static class CloudStorageException extends RuntimeException {
        public CloudStorageException(String message) {
            super(message);
        }

        public CloudStorageException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}

/*
 * Usage Examples:
 * 
 * // Upload file
 * POST /api/cloud-storage/upload
 * Form Data: file=myfile.pdf, provider=aws, bucket=my-bucket
 * 
 * // Download file
 * GET /api/cloud-storage/download/aws/my-bucket/file-key
 * 
 * // List files
 * GET /api/cloud-storage/aws/my-bucket/list?prefix=documents/
 * 
 * // Copy between providers
 * POST /api/cloud-storage/copy
 * Body: {
 *   "sourceProvider": "aws",
 *   "sourceBucket": "source-bucket",
 *   "sourceKey": "file.pdf",
 *   "destProvider": "azure",
 *   "destBucket": "dest-container",
 *   "destKey": "file.pdf"
 * }
 * 
 * // Get provider statistics
 * GET /api/cloud-storage/stats/aws
 */
