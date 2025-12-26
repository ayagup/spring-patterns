package com.example.storage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Azure Blob Storage Pattern
 * 
 * Purpose: Integration with Azure Blob Storage for cloud-based file storage.
 * Provides blob upload, download, SAS token generation, and container management.
 * 
 * Key Features:
 * - Azure Blob Storage integration
 * - Container operations
 * - Blob upload/download
 * - SAS token generation
 * - Blob metadata management
 * - Access tier management (Hot/Cool/Archive)
 * - Snapshot support
 * - Lease management
 * - Blob versioning
 * - Lifecycle policies
 * 
 * Use Cases:
 * - Cloud file storage
 * - Media storage and streaming
 * - Document management
 * - Backup and disaster recovery
 * - Data archival
 * - Static website hosting
 * 
 * Dependencies:
 * <dependency>
 *     <groupId>com.azure</groupId>
 *     <artifactId>azure-storage-blob</artifactId>
 *     <version>12.20.0</version>
 * </dependency>
 * 
 * @author Spring Patterns
 */
@SpringBootApplication
public class AzureBlobStoragePattern {

    public static void main(String[] args) {
        SpringApplication.run(AzureBlobStoragePattern.class, args);
    }

    /**
     * Azure Blob Storage Configuration
     */
    @Configuration
    public static class AzureBlobConfig {
        
        @Bean
        public AzureBlobProperties azureBlobProperties() {
            AzureBlobProperties properties = new AzureBlobProperties();
            properties.setConnectionString("YOUR_CONNECTION_STRING");
            properties.setAccountName("myaccount");
            properties.setAccountKey("YOUR_ACCOUNT_KEY");
            properties.setContainerName("mycontainer");
            return properties;
        }

        @Bean
        public AzureBlobClientWrapper azureBlobClientWrapper(AzureBlobProperties properties) {
            return new AzureBlobClientWrapper(properties);
        }
    }

    /**
     * Azure Blob Storage Controller
     */
    @RestController
    @RequestMapping("/api/azure-blob")
    public static class AzureBlobController {

        private final AzureBlobService blobService;

        public AzureBlobController(AzureBlobService blobService) {
            this.blobService = blobService;
        }

        /**
         * Upload blob
         */
        @PostMapping("/upload")
        public ResponseEntity<BlobInfo> uploadBlob(
                @RequestParam("file") MultipartFile file,
                @RequestParam(required = false) String blobName,
                @RequestParam(required = false) String container,
                @RequestParam(required = false) Map<String, String> metadata) {
            
            try {
                BlobInfo blobInfo = blobService.uploadBlob(container, blobName, file, metadata);
                return ResponseEntity.ok(blobInfo);
            } catch (IOException e) {
                throw new AzureBlobException("Upload failed", e);
            }
        }

        /**
         * Upload with access tier
         */
        @PostMapping("/upload/tier")
        public ResponseEntity<BlobInfo> uploadBlobWithTier(
                @RequestParam("file") MultipartFile file,
                @RequestParam(required = false) String blobName,
                @RequestParam(required = false) String container,
                @RequestParam(defaultValue = "HOT") String accessTier) {
            
            try {
                BlobInfo blobInfo = blobService.uploadBlobWithTier(
                    container, blobName, file, accessTier
                );
                return ResponseEntity.ok(blobInfo);
            } catch (IOException e) {
                throw new AzureBlobException("Upload failed", e);
            }
        }

        /**
         * Download blob
         */
        @GetMapping("/download/{container}/{blobName:.+}")
        public ResponseEntity<byte[]> downloadBlob(
                @PathVariable String container,
                @PathVariable String blobName) {
            
            try {
                BlobData data = blobService.downloadBlob(container, blobName);
                
                return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=\"" + blobName + "\"")
                    .header(HttpHeaders.CONTENT_TYPE, data.getContentType())
                    .body(data.getData());
            } catch (IOException e) {
                throw new AzureBlobException("Download failed", e);
            }
        }

        /**
         * Generate SAS token for upload
         */
        @PostMapping("/sas/upload")
        public ResponseEntity<Map<String, String>> generateUploadSasToken(
                @RequestBody SasTokenRequest request) {
            
            String sasUrl = blobService.generateUploadSasUrl(
                request.getContainer(),
                request.getBlobName(),
                request.getExpirationMinutes()
            );
            
            Map<String, String> response = new HashMap<>();
            response.put("sasUrl", sasUrl);
            response.put("container", request.getContainer());
            response.put("blobName", request.getBlobName());
            response.put("expiresIn", request.getExpirationMinutes() + " minutes");
            
            return ResponseEntity.ok(response);
        }

        /**
         * Generate SAS token for download
         */
        @PostMapping("/sas/download")
        public ResponseEntity<Map<String, String>> generateDownloadSasToken(
                @RequestBody SasTokenRequest request) {
            
            String sasUrl = blobService.generateDownloadSasUrl(
                request.getContainer(),
                request.getBlobName(),
                request.getExpirationMinutes()
            );
            
            Map<String, String> response = new HashMap<>();
            response.put("sasUrl", sasUrl);
            response.put("container", request.getContainer());
            response.put("blobName", request.getBlobName());
            response.put("expiresIn", request.getExpirationMinutes() + " minutes");
            
            return ResponseEntity.ok(response);
        }

        /**
         * List blobs in container
         */
        @GetMapping("/list/{container}")
        public ResponseEntity<List<BlobInfo>> listBlobs(
                @PathVariable String container,
                @RequestParam(required = false) String prefix) {
            
            List<BlobInfo> blobs = blobService.listBlobs(container, prefix);
            return ResponseEntity.ok(blobs);
        }

        /**
         * Get blob metadata
         */
        @GetMapping("/{container}/{blobName:.+}/metadata")
        public ResponseEntity<BlobInfo> getBlobMetadata(
                @PathVariable String container,
                @PathVariable String blobName) {
            
            BlobInfo info = blobService.getBlobMetadata(container, blobName);
            return ResponseEntity.ok(info);
        }

        /**
         * Set blob metadata
         */
        @PutMapping("/{container}/{blobName:.+}/metadata")
        public ResponseEntity<Map<String, String>> setBlobMetadata(
                @PathVariable String container,
                @PathVariable String blobName,
                @RequestBody Map<String, String> metadata) {
            
            blobService.setBlobMetadata(container, blobName, metadata);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Metadata updated successfully");
            response.put("container", container);
            response.put("blobName", blobName);
            
            return ResponseEntity.ok(response);
        }

        /**
         * Set blob access tier
         */
        @PutMapping("/{container}/{blobName:.+}/tier")
        public ResponseEntity<Map<String, String>> setBlobAccessTier(
                @PathVariable String container,
                @PathVariable String blobName,
                @RequestParam String accessTier) {
            
            blobService.setBlobAccessTier(container, blobName, accessTier);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Access tier updated successfully");
            response.put("container", container);
            response.put("blobName", blobName);
            response.put("accessTier", accessTier);
            
            return ResponseEntity.ok(response);
        }

        /**
         * Delete blob
         */
        @DeleteMapping("/{container}/{blobName:.+}")
        public ResponseEntity<Map<String, String>> deleteBlob(
                @PathVariable String container,
                @PathVariable String blobName) {
            
            blobService.deleteBlob(container, blobName);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Blob deleted successfully");
            response.put("container", container);
            response.put("blobName", blobName);
            
            return ResponseEntity.ok(response);
        }

        /**
         * Delete multiple blobs
         */
        @DeleteMapping("/{container}/batch")
        public ResponseEntity<Map<String, Object>> deleteBlobs(
                @PathVariable String container,
                @RequestBody List<String> blobNames) {
            
            int deleted = blobService.deleteBlobs(container, blobNames);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Blobs deleted successfully");
            response.put("container", container);
            response.put("deletedCount", deleted);
            
            return ResponseEntity.ok(response);
        }

        /**
         * Copy blob
         */
        @PostMapping("/copy")
        public ResponseEntity<BlobInfo> copyBlob(
                @RequestBody CopyBlobRequest request) {
            
            BlobInfo info = blobService.copyBlob(
                request.getSourceContainer(),
                request.getSourceBlobName(),
                request.getDestContainer(),
                request.getDestBlobName()
            );
            
            return ResponseEntity.ok(info);
        }

        /**
         * Create snapshot
         */
        @PostMapping("/{container}/{blobName:.+}/snapshot")
        public ResponseEntity<Map<String, String>> createSnapshot(
                @PathVariable String container,
                @PathVariable String blobName) {
            
            String snapshotId = blobService.createSnapshot(container, blobName);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Snapshot created successfully");
            response.put("container", container);
            response.put("blobName", blobName);
            response.put("snapshotId", snapshotId);
            
            return ResponseEntity.ok(response);
        }

        /**
         * Get container statistics
         */
        @GetMapping("/{container}/stats")
        public ResponseEntity<ContainerStats> getContainerStats(
                @PathVariable String container) {
            
            return ResponseEntity.ok(blobService.getContainerStats(container));
        }

        /**
         * Create container
         */
        @PostMapping("/containers/{container}")
        public ResponseEntity<Map<String, String>> createContainer(
                @PathVariable String container) {
            
            blobService.createContainer(container);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Container created successfully");
            response.put("container", container);
            
            return ResponseEntity.ok(response);
        }

        /**
         * Delete container
         */
        @DeleteMapping("/containers/{container}")
        public ResponseEntity<Map<String, String>> deleteContainer(
                @PathVariable String container) {
            
            blobService.deleteContainer(container);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Container deleted successfully");
            response.put("container", container);
            
            return ResponseEntity.ok(response);
        }
    }

    /**
     * Azure Blob Service
     */
    @Service
    public static class AzureBlobService {

        private final AzureBlobClientWrapper blobClient;
        private final AzureBlobProperties properties;

        public AzureBlobService(AzureBlobClientWrapper blobClient, 
                               AzureBlobProperties properties) {
            this.blobClient = blobClient;
            this.properties = properties;
        }

        /**
         * Upload blob
         */
        public BlobInfo uploadBlob(String container, String blobName, 
                                   MultipartFile file, Map<String, String> metadata) 
                throws IOException {
            
            if (container == null || container.isEmpty()) {
                container = properties.getContainerName();
            }
            
            if (blobName == null || blobName.isEmpty()) {
                blobName = generateBlobName(file.getOriginalFilename());
            }
            
            try (InputStream inputStream = file.getInputStream()) {
                return blobClient.uploadBlob(
                    container,
                    blobName,
                    inputStream,
                    file.getSize(),
                    file.getContentType(),
                    metadata
                );
            }
        }

        /**
         * Upload blob with access tier
         */
        public BlobInfo uploadBlobWithTier(String container, String blobName, 
                                          MultipartFile file, String accessTier) 
                throws IOException {
            
            if (container == null || container.isEmpty()) {
                container = properties.getContainerName();
            }
            
            if (blobName == null || blobName.isEmpty()) {
                blobName = generateBlobName(file.getOriginalFilename());
            }
            
            try (InputStream inputStream = file.getInputStream()) {
                return blobClient.uploadBlobWithTier(
                    container,
                    blobName,
                    inputStream,
                    file.getSize(),
                    file.getContentType(),
                    accessTier
                );
            }
        }

        /**
         * Download blob
         */
        public BlobData downloadBlob(String container, String blobName) throws IOException {
            if (container == null || container.isEmpty()) {
                container = properties.getContainerName();
            }
            
            return blobClient.downloadBlob(container, blobName);
        }

        /**
         * Generate SAS URL for upload
         */
        public String generateUploadSasUrl(String container, String blobName, 
                                          int expirationMinutes) {
            if (container == null || container.isEmpty()) {
                container = properties.getContainerName();
            }
            
            return blobClient.generateSasUrl(
                container, blobName, Duration.ofMinutes(expirationMinutes), "w"
            );
        }

        /**
         * Generate SAS URL for download
         */
        public String generateDownloadSasUrl(String container, String blobName, 
                                            int expirationMinutes) {
            if (container == null || container.isEmpty()) {
                container = properties.getContainerName();
            }
            
            return blobClient.generateSasUrl(
                container, blobName, Duration.ofMinutes(expirationMinutes), "r"
            );
        }

        /**
         * List blobs
         */
        public List<BlobInfo> listBlobs(String container, String prefix) {
            if (container == null || container.isEmpty()) {
                container = properties.getContainerName();
            }
            
            return blobClient.listBlobs(container, prefix);
        }

        /**
         * Get blob metadata
         */
        public BlobInfo getBlobMetadata(String container, String blobName) {
            if (container == null || container.isEmpty()) {
                container = properties.getContainerName();
            }
            
            return blobClient.getBlobMetadata(container, blobName);
        }

        /**
         * Set blob metadata
         */
        public void setBlobMetadata(String container, String blobName, 
                                   Map<String, String> metadata) {
            if (container == null || container.isEmpty()) {
                container = properties.getContainerName();
            }
            
            blobClient.setBlobMetadata(container, blobName, metadata);
        }

        /**
         * Set blob access tier
         */
        public void setBlobAccessTier(String container, String blobName, String accessTier) {
            if (container == null || container.isEmpty()) {
                container = properties.getContainerName();
            }
            
            blobClient.setBlobAccessTier(container, blobName, accessTier);
        }

        /**
         * Delete blob
         */
        public void deleteBlob(String container, String blobName) {
            if (container == null || container.isEmpty()) {
                container = properties.getContainerName();
            }
            
            blobClient.deleteBlob(container, blobName);
        }

        /**
         * Delete multiple blobs
         */
        public int deleteBlobs(String container, List<String> blobNames) {
            if (container == null || container.isEmpty()) {
                container = properties.getContainerName();
            }
            
            return blobClient.deleteBlobs(container, blobNames);
        }

        /**
         * Copy blob
         */
        public BlobInfo copyBlob(String sourceContainer, String sourceBlobName,
                                String destContainer, String destBlobName) {
            return blobClient.copyBlob(sourceContainer, sourceBlobName, 
                                      destContainer, destBlobName);
        }

        /**
         * Create snapshot
         */
        public String createSnapshot(String container, String blobName) {
            if (container == null || container.isEmpty()) {
                container = properties.getContainerName();
            }
            
            return blobClient.createSnapshot(container, blobName);
        }

        /**
         * Get container statistics
         */
        public ContainerStats getContainerStats(String container) {
            return blobClient.getContainerStats(container);
        }

        /**
         * Create container
         */
        public void createContainer(String container) {
            blobClient.createContainer(container);
        }

        /**
         * Delete container
         */
        public void deleteContainer(String container) {
            blobClient.deleteContainer(container);
        }

        /**
         * Generate unique blob name
         */
        private String generateBlobName(String filename) {
            String timestamp = String.valueOf(System.currentTimeMillis());
            return "uploads/" + timestamp + "_" + filename;
        }
    }

    /**
     * Azure Blob Client Wrapper (Mock implementation - replace with actual Azure SDK)
     */
    public static class AzureBlobClientWrapper {
        
        private final AzureBlobProperties properties;
        private final Map<String, Map<String, MockBlob>> storage = new ConcurrentHashMap<>();
        private long totalUploads = 0;
        private long totalDownloads = 0;

        public AzureBlobClientWrapper(AzureBlobProperties properties) {
            this.properties = properties;
        }

        @PostConstruct
        public void init() {
            System.out.println("Azure Blob Storage Client initialized");
            System.out.println("Account: " + properties.getAccountName());
            System.out.println("Default container: " + properties.getContainerName());
            
            // Create default container
            createContainer(properties.getContainerName());
        }

        /**
         * Upload blob
         */
        public BlobInfo uploadBlob(String container, String blobName, InputStream data,
                                   long contentLength, String contentType,
                                   Map<String, String> metadata) throws IOException {
            
            byte[] bytes = data.readAllBytes();
            
            MockBlob blob = new MockBlob(
                container,
                blobName,
                bytes,
                contentType,
                metadata != null ? metadata : new HashMap<>(),
                "HOT",
                LocalDateTime.now()
            );
            
            storage.computeIfAbsent(container, k -> new ConcurrentHashMap<>())
                .put(blobName, blob);
            
            totalUploads++;
            
            return toBlobInfo(blob);
        }

        /**
         * Upload blob with access tier
         */
        public BlobInfo uploadBlobWithTier(String container, String blobName, 
                                          InputStream data, long contentLength,
                                          String contentType, String accessTier) 
                throws IOException {
            
            byte[] bytes = data.readAllBytes();
            
            MockBlob blob = new MockBlob(
                container,
                blobName,
                bytes,
                contentType,
                new HashMap<>(),
                accessTier,
                LocalDateTime.now()
            );
            
            storage.computeIfAbsent(container, k -> new ConcurrentHashMap<>())
                .put(blobName, blob);
            
            totalUploads++;
            
            return toBlobInfo(blob);
        }

        /**
         * Download blob
         */
        public BlobData downloadBlob(String container, String blobName) throws IOException {
            Map<String, MockBlob> containerStorage = storage.get(container);
            
            if (containerStorage == null) {
                throw new AzureBlobException("Container not found: " + container);
            }
            
            MockBlob blob = containerStorage.get(blobName);
            
            if (blob == null) {
                throw new AzureBlobException("Blob not found: " + blobName);
            }
            
            totalDownloads++;
            
            return new BlobData(
                blob.getContainer(),
                blob.getBlobName(),
                blob.getData(),
                blob.getContentType(),
                blob.getMetadata()
            );
        }

        /**
         * Generate SAS URL
         */
        public String generateSasUrl(String container, String blobName, 
                                     Duration expiration, String permissions) {
            // Mock SAS URL
            return "https://" + properties.getAccountName() + 
                   ".blob.core.windows.net/" + container + "/" + blobName + 
                   "?sp=" + permissions + "&se=" + 
                   OffsetDateTime.now().plus(expiration).toString();
        }

        /**
         * List blobs
         */
        public List<BlobInfo> listBlobs(String container, String prefix) {
            Map<String, MockBlob> containerStorage = storage.get(container);
            
            if (containerStorage == null) {
                return Collections.emptyList();
            }
            
            return containerStorage.values().stream()
                .filter(blob -> prefix == null || blob.getBlobName().startsWith(prefix))
                .map(this::toBlobInfo)
                .collect(Collectors.toList());
        }

        /**
         * Get blob metadata
         */
        public BlobInfo getBlobMetadata(String container, String blobName) {
            Map<String, MockBlob> containerStorage = storage.get(container);
            
            if (containerStorage == null) {
                throw new AzureBlobException("Container not found: " + container);
            }
            
            MockBlob blob = containerStorage.get(blobName);
            
            if (blob == null) {
                throw new AzureBlobException("Blob not found: " + blobName);
            }
            
            return toBlobInfo(blob);
        }

        /**
         * Set blob metadata
         */
        public void setBlobMetadata(String container, String blobName, 
                                   Map<String, String> metadata) {
            Map<String, MockBlob> containerStorage = storage.get(container);
            
            if (containerStorage != null) {
                MockBlob blob = containerStorage.get(blobName);
                if (blob != null) {
                    blob.getMetadata().clear();
                    blob.getMetadata().putAll(metadata);
                }
            }
        }

        /**
         * Set blob access tier
         */
        public void setBlobAccessTier(String container, String blobName, String accessTier) {
            Map<String, MockBlob> containerStorage = storage.get(container);
            
            if (containerStorage != null) {
                MockBlob blob = containerStorage.get(blobName);
                if (blob != null) {
                    blob.setAccessTier(accessTier);
                }
            }
        }

        /**
         * Delete blob
         */
        public void deleteBlob(String container, String blobName) {
            Map<String, MockBlob> containerStorage = storage.get(container);
            
            if (containerStorage != null) {
                containerStorage.remove(blobName);
            }
        }

        /**
         * Delete multiple blobs
         */
        public int deleteBlobs(String container, List<String> blobNames) {
            Map<String, MockBlob> containerStorage = storage.get(container);
            
            if (containerStorage == null) {
                return 0;
            }
            
            int deleted = 0;
            for (String blobName : blobNames) {
                if (containerStorage.remove(blobName) != null) {
                    deleted++;
                }
            }
            
            return deleted;
        }

        /**
         * Copy blob
         */
        public BlobInfo copyBlob(String sourceContainer, String sourceBlobName,
                                String destContainer, String destBlobName) {
            Map<String, MockBlob> sourceContainerStorage = storage.get(sourceContainer);
            
            if (sourceContainerStorage == null) {
                throw new AzureBlobException("Source container not found: " + sourceContainer);
            }
            
            MockBlob sourceBlob = sourceContainerStorage.get(sourceBlobName);
            
            if (sourceBlob == null) {
                throw new AzureBlobException("Source blob not found: " + sourceBlobName);
            }
            
            MockBlob copiedBlob = new MockBlob(
                destContainer,
                destBlobName,
                sourceBlob.getData(),
                sourceBlob.getContentType(),
                new HashMap<>(sourceBlob.getMetadata()),
                sourceBlob.getAccessTier(),
                LocalDateTime.now()
            );
            
            storage.computeIfAbsent(destContainer, k -> new ConcurrentHashMap<>())
                .put(destBlobName, copiedBlob);
            
            return toBlobInfo(copiedBlob);
        }

        /**
         * Create snapshot
         */
        public String createSnapshot(String container, String blobName) {
            // Mock snapshot ID
            return UUID.randomUUID().toString();
        }

        /**
         * Get container statistics
         */
        public ContainerStats getContainerStats(String container) {
            Map<String, MockBlob> containerStorage = storage.get(container);
            
            if (containerStorage == null) {
                return new ContainerStats(container, 0, 0, LocalDateTime.now());
            }
            
            long blobCount = containerStorage.size();
            long totalSize = containerStorage.values().stream()
                .mapToLong(blob -> blob.getData().length)
                .sum();
            
            return new ContainerStats(container, blobCount, totalSize, LocalDateTime.now());
        }

        /**
         * Create container
         */
        public void createContainer(String container) {
            storage.computeIfAbsent(container, k -> new ConcurrentHashMap<>());
            System.out.println("Container created: " + container);
        }

        /**
         * Delete container
         */
        public void deleteContainer(String container) {
            storage.remove(container);
            System.out.println("Container deleted: " + container);
        }

        /**
         * Convert to BlobInfo
         */
        private BlobInfo toBlobInfo(MockBlob blob) {
            return new BlobInfo(
                blob.getContainer(),
                blob.getBlobName(),
                blob.getData().length,
                blob.getContentType(),
                blob.getMetadata(),
                blob.getAccessTier(),
                blob.getLastModified()
            );
        }
    }

    // Model Classes

    public static class BlobInfo {
        private String container;
        private String blobName;
        private long size;
        private String contentType;
        private Map<String, String> metadata;
        private String accessTier;
        private LocalDateTime lastModified;

        public BlobInfo(String container, String blobName, long size, String contentType,
                       Map<String, String> metadata, String accessTier, 
                       LocalDateTime lastModified) {
            this.container = container;
            this.blobName = blobName;
            this.size = size;
            this.contentType = contentType;
            this.metadata = metadata;
            this.accessTier = accessTier;
            this.lastModified = lastModified;
        }

        // Getters
        public String getContainer() { return container; }
        public String getBlobName() { return blobName; }
        public long getSize() { return size; }
        public String getContentType() { return contentType; }
        public Map<String, String> getMetadata() { return metadata; }
        public String getAccessTier() { return accessTier; }
        public LocalDateTime getLastModified() { return lastModified; }
    }

    public static class BlobData {
        private String container;
        private String blobName;
        private byte[] data;
        private String contentType;
        private Map<String, String> metadata;

        public BlobData(String container, String blobName, byte[] data, 
                       String contentType, Map<String, String> metadata) {
            this.container = container;
            this.blobName = blobName;
            this.data = data;
            this.contentType = contentType;
            this.metadata = metadata;
        }

        // Getters
        public String getContainer() { return container; }
        public String getBlobName() { return blobName; }
        public byte[] getData() { return data; }
        public String getContentType() { return contentType; }
        public Map<String, String> getMetadata() { return metadata; }
    }

    public static class MockBlob {
        private String container;
        private String blobName;
        private byte[] data;
        private String contentType;
        private Map<String, String> metadata;
        private String accessTier;
        private LocalDateTime lastModified;

        public MockBlob(String container, String blobName, byte[] data, 
                       String contentType, Map<String, String> metadata,
                       String accessTier, LocalDateTime lastModified) {
            this.container = container;
            this.blobName = blobName;
            this.data = data;
            this.contentType = contentType;
            this.metadata = metadata;
            this.accessTier = accessTier;
            this.lastModified = lastModified;
        }

        // Getters and Setters
        public String getContainer() { return container; }
        public String getBlobName() { return blobName; }
        public byte[] getData() { return data; }
        public String getContentType() { return contentType; }
        public Map<String, String> getMetadata() { return metadata; }
        public String getAccessTier() { return accessTier; }
        public void setAccessTier(String accessTier) { this.accessTier = accessTier; }
        public LocalDateTime getLastModified() { return lastModified; }
    }

    public static class AzureBlobProperties {
        private String connectionString;
        private String accountName;
        private String accountKey;
        private String containerName;

        // Getters and Setters
        public String getConnectionString() { return connectionString; }
        public void setConnectionString(String connectionString) { 
            this.connectionString = connectionString; 
        }
        public String getAccountName() { return accountName; }
        public void setAccountName(String accountName) { this.accountName = accountName; }
        public String getAccountKey() { return accountKey; }
        public void setAccountKey(String accountKey) { this.accountKey = accountKey; }
        public String getContainerName() { return containerName; }
        public void setContainerName(String containerName) { 
            this.containerName = containerName; 
        }
    }

    public static class SasTokenRequest {
        private String container;
        private String blobName;
        private int expirationMinutes = 60;

        // Getters and Setters
        public String getContainer() { return container; }
        public void setContainer(String container) { this.container = container; }
        public String getBlobName() { return blobName; }
        public void setBlobName(String blobName) { this.blobName = blobName; }
        public int getExpirationMinutes() { return expirationMinutes; }
        public void setExpirationMinutes(int expirationMinutes) { 
            this.expirationMinutes = expirationMinutes; 
        }
    }

    public static class CopyBlobRequest {
        private String sourceContainer;
        private String sourceBlobName;
        private String destContainer;
        private String destBlobName;

        // Getters and Setters
        public String getSourceContainer() { return sourceContainer; }
        public void setSourceContainer(String sourceContainer) { 
            this.sourceContainer = sourceContainer; 
        }
        public String getSourceBlobName() { return sourceBlobName; }
        public void setSourceBlobName(String sourceBlobName) { 
            this.sourceBlobName = sourceBlobName; 
        }
        public String getDestContainer() { return destContainer; }
        public void setDestContainer(String destContainer) { this.destContainer = destContainer; }
        public String getDestBlobName() { return destBlobName; }
        public void setDestBlobName(String destBlobName) { this.destBlobName = destBlobName; }
    }

    public static class ContainerStats {
        private String containerName;
        private long blobCount;
        private long totalSize;
        private LocalDateTime timestamp;

        public ContainerStats(String containerName, long blobCount, long totalSize,
                             LocalDateTime timestamp) {
            this.containerName = containerName;
            this.blobCount = blobCount;
            this.totalSize = totalSize;
            this.timestamp = timestamp;
        }

        // Getters
        public String getContainerName() { return containerName; }
        public long getBlobCount() { return blobCount; }
        public long getTotalSize() { return totalSize; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }

    public static class AzureBlobException extends RuntimeException {
        public AzureBlobException(String message) {
            super(message);
        }

        public AzureBlobException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}

/*
 * Real Azure SDK Implementation Example:
 * 
 * import com.azure.storage.blob.BlobClient;
 * import com.azure.storage.blob.BlobContainerClient;
 * import com.azure.storage.blob.BlobServiceClient;
 * import com.azure.storage.blob.BlobServiceClientBuilder;
 * import com.azure.storage.blob.models.BlobHttpHeaders;
 * import com.azure.storage.blob.sas.BlobSasPermission;
 * import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
 * 
 * @Bean
 * public BlobServiceClient blobServiceClient(AzureBlobProperties properties) {
 *     return new BlobServiceClientBuilder()
 *         .connectionString(properties.getConnectionString())
 *         .buildClient();
 * }
 * 
 * // Upload blob
 * BlobClient blobClient = containerClient.getBlobClient(blobName);
 * blobClient.upload(inputStream, contentLength, true);
 * 
 * // Generate SAS token
 * BlobSasPermission permission = new BlobSasPermission().setReadPermission(true);
 * BlobServiceSasSignatureValues values = new BlobServiceSasSignatureValues(
 *     OffsetDateTime.now().plusHours(1), permission
 * );
 * String sasToken = blobClient.generateSas(values);
 */

/*
 * Application Properties:
 * 
 * # Azure Blob Storage Configuration
 * azure.storage.connection-string=${AZURE_STORAGE_CONNECTION_STRING}
 * azure.storage.account-name=myaccount
 * azure.storage.account-key=${AZURE_STORAGE_ACCOUNT_KEY}
 * azure.storage.container-name=mycontainer
 */
