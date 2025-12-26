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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Google Cloud Storage Pattern
 * 
 * Purpose: Integration with Google Cloud Storage (GCS) for cloud-based file storage.
 * Provides bucket and object operations with signed URLs and IAM integration.
 * 
 * Key Features:
 * - GCS bucket operations
 * - Object upload/download
 * - Signed URL generation
 * - Object metadata management
 * - Storage class management
 * - Object versioning
 * - Lifecycle management
 * - IAM integration
 * - Customer-managed encryption
 * - Uniform bucket-level access
 * 
 * Use Cases:
 * - Cloud file storage
 * - Media hosting and streaming
 * - Data analytics and ML
 * - Backup and archival
 * - Content distribution
 * - Static website hosting
 * 
 * Dependencies:
 * <dependency>
 *     <groupId>com.google.cloud</groupId>
 *     <artifactId>google-cloud-storage</artifactId>
 *     <version>2.20.0</version>
 * </dependency>
 * 
 * @author Spring Patterns
 */
@SpringBootApplication
public class GoogleCloudStoragePattern {

    public static void main(String[] args) {
        SpringApplication.run(GoogleCloudStoragePattern.class, args);
    }

    /**
     * Google Cloud Storage Configuration
     */
    @Configuration
    public static class GCSConfig {
        
        @Bean
        public GCSProperties gcsProperties() {
            GCSProperties properties = new GCSProperties();
            properties.setProjectId("my-project-id");
            properties.setBucketName("my-bucket");
            properties.setCredentialsPath("/path/to/credentials.json");
            return properties;
        }

        @Bean
        public GCSClientWrapper gcsClientWrapper(GCSProperties properties) {
            return new GCSClientWrapper(properties);
        }
    }

    /**
     * Google Cloud Storage Controller
     */
    @RestController
    @RequestMapping("/api/gcs")
    public static class GCSController {

        private final GCSService gcsService;

        public GCSController(GCSService gcsService) {
            this.gcsService = gcsService;
        }

        /**
         * Upload object to GCS
         */
        @PostMapping("/upload")
        public ResponseEntity<GCSObjectInfo> uploadObject(
                @RequestParam("file") MultipartFile file,
                @RequestParam(required = false) String objectName,
                @RequestParam(required = false) String bucket,
                @RequestParam(required = false) Map<String, String> metadata) {
            
            try {
                GCSObjectInfo objectInfo = gcsService.uploadObject(
                    bucket, objectName, file, metadata
                );
                return ResponseEntity.ok(objectInfo);
            } catch (IOException e) {
                throw new GCSException("Upload failed", e);
            }
        }

        /**
         * Upload with storage class
         */
        @PostMapping("/upload/storage-class")
        public ResponseEntity<GCSObjectInfo> uploadWithStorageClass(
                @RequestParam("file") MultipartFile file,
                @RequestParam(required = false) String objectName,
                @RequestParam(required = false) String bucket,
                @RequestParam(defaultValue = "STANDARD") String storageClass) {
            
            try {
                GCSObjectInfo objectInfo = gcsService.uploadWithStorageClass(
                    bucket, objectName, file, storageClass
                );
                return ResponseEntity.ok(objectInfo);
            } catch (IOException e) {
                throw new GCSException("Upload failed", e);
            }
        }

        /**
         * Download object from GCS
         */
        @GetMapping("/download/{bucket}/{objectName:.+}")
        public ResponseEntity<byte[]> downloadObject(
                @PathVariable String bucket,
                @PathVariable String objectName) {
            
            try {
                GCSObjectData data = gcsService.downloadObject(bucket, objectName);
                
                return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=\"" + objectName + "\"")
                    .header(HttpHeaders.CONTENT_TYPE, data.getContentType())
                    .body(data.getData());
            } catch (IOException e) {
                throw new GCSException("Download failed", e);
            }
        }

        /**
         * Generate signed URL for upload
         */
        @PostMapping("/signed-url/upload")
        public ResponseEntity<Map<String, String>> getSignedUploadUrl(
                @RequestBody SignedUrlRequest request) {
            
            String url = gcsService.generateSignedUploadUrl(
                request.getBucket(),
                request.getObjectName(),
                request.getExpirationMinutes()
            );
            
            Map<String, String> response = new HashMap<>();
            response.put("signedUrl", url);
            response.put("bucket", request.getBucket());
            response.put("objectName", request.getObjectName());
            response.put("expiresIn", request.getExpirationMinutes() + " minutes");
            
            return ResponseEntity.ok(response);
        }

        /**
         * Generate signed URL for download
         */
        @PostMapping("/signed-url/download")
        public ResponseEntity<Map<String, String>> getSignedDownloadUrl(
                @RequestBody SignedUrlRequest request) {
            
            String url = gcsService.generateSignedDownloadUrl(
                request.getBucket(),
                request.getObjectName(),
                request.getExpirationMinutes()
            );
            
            Map<String, String> response = new HashMap<>();
            response.put("signedUrl", url);
            response.put("bucket", request.getBucket());
            response.put("objectName", request.getObjectName());
            response.put("expiresIn", request.getExpirationMinutes() + " minutes");
            
            return ResponseEntity.ok(response);
        }

        /**
         * List objects in bucket
         */
        @GetMapping("/list/{bucket}")
        public ResponseEntity<List<GCSObjectInfo>> listObjects(
                @PathVariable String bucket,
                @RequestParam(required = false) String prefix,
                @RequestParam(required = false, defaultValue = "1000") int maxResults) {
            
            List<GCSObjectInfo> objects = gcsService.listObjects(bucket, prefix, maxResults);
            return ResponseEntity.ok(objects);
        }

        /**
         * Get object metadata
         */
        @GetMapping("/{bucket}/{objectName:.+}/metadata")
        public ResponseEntity<GCSObjectInfo> getObjectMetadata(
                @PathVariable String bucket,
                @PathVariable String objectName) {
            
            GCSObjectInfo info = gcsService.getObjectMetadata(bucket, objectName);
            return ResponseEntity.ok(info);
        }

        /**
         * Update object metadata
         */
        @PutMapping("/{bucket}/{objectName:.+}/metadata")
        public ResponseEntity<Map<String, String>> updateObjectMetadata(
                @PathVariable String bucket,
                @PathVariable String objectName,
                @RequestBody Map<String, String> metadata) {
            
            gcsService.updateObjectMetadata(bucket, objectName, metadata);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Metadata updated successfully");
            response.put("bucket", bucket);
            response.put("objectName", objectName);
            
            return ResponseEntity.ok(response);
        }

        /**
         * Delete object
         */
        @DeleteMapping("/{bucket}/{objectName:.+}")
        public ResponseEntity<Map<String, String>> deleteObject(
                @PathVariable String bucket,
                @PathVariable String objectName) {
            
            gcsService.deleteObject(bucket, objectName);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Object deleted successfully");
            response.put("bucket", bucket);
            response.put("objectName", objectName);
            
            return ResponseEntity.ok(response);
        }

        /**
         * Delete multiple objects
         */
        @DeleteMapping("/{bucket}/batch")
        public ResponseEntity<Map<String, Object>> deleteObjects(
                @PathVariable String bucket,
                @RequestBody List<String> objectNames) {
            
            int deleted = gcsService.deleteObjects(bucket, objectNames);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Objects deleted successfully");
            response.put("bucket", bucket);
            response.put("deletedCount", deleted);
            
            return ResponseEntity.ok(response);
        }

        /**
         * Copy object
         */
        @PostMapping("/copy")
        public ResponseEntity<GCSObjectInfo> copyObject(
                @RequestBody CopyObjectRequest request) {
            
            GCSObjectInfo info = gcsService.copyObject(
                request.getSourceBucket(),
                request.getSourceObjectName(),
                request.getDestBucket(),
                request.getDestObjectName()
            );
            
            return ResponseEntity.ok(info);
        }

        /**
         * Make object public
         */
        @PutMapping("/{bucket}/{objectName:.+}/public")
        public ResponseEntity<Map<String, String>> makePublic(
                @PathVariable String bucket,
                @PathVariable String objectName) {
            
            String publicUrl = gcsService.makePublic(bucket, objectName);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Object is now public");
            response.put("bucket", bucket);
            response.put("objectName", objectName);
            response.put("publicUrl", publicUrl);
            
            return ResponseEntity.ok(response);
        }

        /**
         * Get bucket statistics
         */
        @GetMapping("/{bucket}/stats")
        public ResponseEntity<BucketStats> getBucketStats(@PathVariable String bucket) {
            return ResponseEntity.ok(gcsService.getBucketStats(bucket));
        }

        /**
         * Create bucket
         */
        @PostMapping("/buckets/{bucket}")
        public ResponseEntity<Map<String, String>> createBucket(
                @PathVariable String bucket,
                @RequestParam(required = false) String location,
                @RequestParam(required = false, defaultValue = "STANDARD") String storageClass) {
            
            gcsService.createBucket(bucket, location, storageClass);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Bucket created successfully");
            response.put("bucket", bucket);
            response.put("location", location != null ? location : "us");
            response.put("storageClass", storageClass);
            
            return ResponseEntity.ok(response);
        }

        /**
         * Delete bucket
         */
        @DeleteMapping("/buckets/{bucket}")
        public ResponseEntity<Map<String, String>> deleteBucket(@PathVariable String bucket) {
            gcsService.deleteBucket(bucket);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Bucket deleted successfully");
            response.put("bucket", bucket);
            
            return ResponseEntity.ok(response);
        }
    }

    /**
     * Google Cloud Storage Service
     */
    @Service
    public static class GCSService {

        private final GCSClientWrapper gcsClient;
        private final GCSProperties properties;

        public GCSService(GCSClientWrapper gcsClient, GCSProperties properties) {
            this.gcsClient = gcsClient;
            this.properties = properties;
        }

        /**
         * Upload object
         */
        public GCSObjectInfo uploadObject(String bucket, String objectName, 
                                         MultipartFile file, Map<String, String> metadata) 
                throws IOException {
            
            if (bucket == null || bucket.isEmpty()) {
                bucket = properties.getBucketName();
            }
            
            if (objectName == null || objectName.isEmpty()) {
                objectName = generateObjectName(file.getOriginalFilename());
            }
            
            try (InputStream inputStream = file.getInputStream()) {
                return gcsClient.uploadObject(
                    bucket,
                    objectName,
                    inputStream,
                    file.getSize(),
                    file.getContentType(),
                    metadata
                );
            }
        }

        /**
         * Upload with storage class
         */
        public GCSObjectInfo uploadWithStorageClass(String bucket, String objectName,
                                                   MultipartFile file, String storageClass) 
                throws IOException {
            
            if (bucket == null || bucket.isEmpty()) {
                bucket = properties.getBucketName();
            }
            
            if (objectName == null || objectName.isEmpty()) {
                objectName = generateObjectName(file.getOriginalFilename());
            }
            
            try (InputStream inputStream = file.getInputStream()) {
                return gcsClient.uploadWithStorageClass(
                    bucket,
                    objectName,
                    inputStream,
                    file.getSize(),
                    file.getContentType(),
                    storageClass
                );
            }
        }

        /**
         * Download object
         */
        public GCSObjectData downloadObject(String bucket, String objectName) throws IOException {
            if (bucket == null || bucket.isEmpty()) {
                bucket = properties.getBucketName();
            }
            
            return gcsClient.downloadObject(bucket, objectName);
        }

        /**
         * Generate signed URL for upload
         */
        public String generateSignedUploadUrl(String bucket, String objectName, 
                                             int expirationMinutes) {
            if (bucket == null || bucket.isEmpty()) {
                bucket = properties.getBucketName();
            }
            
            return gcsClient.generateSignedUrl(
                bucket, objectName, Duration.ofMinutes(expirationMinutes), "PUT"
            );
        }

        /**
         * Generate signed URL for download
         */
        public String generateSignedDownloadUrl(String bucket, String objectName, 
                                               int expirationMinutes) {
            if (bucket == null || bucket.isEmpty()) {
                bucket = properties.getBucketName();
            }
            
            return gcsClient.generateSignedUrl(
                bucket, objectName, Duration.ofMinutes(expirationMinutes), "GET"
            );
        }

        /**
         * List objects
         */
        public List<GCSObjectInfo> listObjects(String bucket, String prefix, int maxResults) {
            if (bucket == null || bucket.isEmpty()) {
                bucket = properties.getBucketName();
            }
            
            return gcsClient.listObjects(bucket, prefix, maxResults);
        }

        /**
         * Get object metadata
         */
        public GCSObjectInfo getObjectMetadata(String bucket, String objectName) {
            if (bucket == null || bucket.isEmpty()) {
                bucket = properties.getBucketName();
            }
            
            return gcsClient.getObjectMetadata(bucket, objectName);
        }

        /**
         * Update object metadata
         */
        public void updateObjectMetadata(String bucket, String objectName, 
                                        Map<String, String> metadata) {
            if (bucket == null || bucket.isEmpty()) {
                bucket = properties.getBucketName();
            }
            
            gcsClient.updateObjectMetadata(bucket, objectName, metadata);
        }

        /**
         * Delete object
         */
        public void deleteObject(String bucket, String objectName) {
            if (bucket == null || bucket.isEmpty()) {
                bucket = properties.getBucketName();
            }
            
            gcsClient.deleteObject(bucket, objectName);
        }

        /**
         * Delete multiple objects
         */
        public int deleteObjects(String bucket, List<String> objectNames) {
            if (bucket == null || bucket.isEmpty()) {
                bucket = properties.getBucketName();
            }
            
            return gcsClient.deleteObjects(bucket, objectNames);
        }

        /**
         * Copy object
         */
        public GCSObjectInfo copyObject(String sourceBucket, String sourceObjectName,
                                       String destBucket, String destObjectName) {
            return gcsClient.copyObject(sourceBucket, sourceObjectName, 
                                       destBucket, destObjectName);
        }

        /**
         * Make object public
         */
        public String makePublic(String bucket, String objectName) {
            if (bucket == null || bucket.isEmpty()) {
                bucket = properties.getBucketName();
            }
            
            return gcsClient.makePublic(bucket, objectName);
        }

        /**
         * Get bucket statistics
         */
        public BucketStats getBucketStats(String bucket) {
            return gcsClient.getBucketStats(bucket);
        }

        /**
         * Create bucket
         */
        public void createBucket(String bucket, String location, String storageClass) {
            gcsClient.createBucket(bucket, location, storageClass);
        }

        /**
         * Delete bucket
         */
        public void deleteBucket(String bucket) {
            gcsClient.deleteBucket(bucket);
        }

        /**
         * Generate unique object name
         */
        private String generateObjectName(String filename) {
            String timestamp = String.valueOf(System.currentTimeMillis());
            return "uploads/" + timestamp + "_" + filename;
        }
    }

    /**
     * GCS Client Wrapper (Mock implementation - replace with actual GCS SDK)
     */
    public static class GCSClientWrapper {
        
        private final GCSProperties properties;
        private final Map<String, Map<String, MockGCSObject>> storage = new ConcurrentHashMap<>();
        private long totalUploads = 0;
        private long totalDownloads = 0;

        public GCSClientWrapper(GCSProperties properties) {
            this.properties = properties;
        }

        @PostConstruct
        public void init() {
            System.out.println("GCS Client initialized for project: " + properties.getProjectId());
            System.out.println("Default bucket: " + properties.getBucketName());
            
            // Create default bucket
            createBucket(properties.getBucketName(), "us", "STANDARD");
        }

        /**
         * Upload object
         */
        public GCSObjectInfo uploadObject(String bucket, String objectName, 
                                         InputStream data, long contentLength,
                                         String contentType, Map<String, String> metadata) 
                throws IOException {
            
            byte[] bytes = data.readAllBytes();
            
            MockGCSObject object = new MockGCSObject(
                bucket,
                objectName,
                bytes,
                contentType,
                metadata != null ? metadata : new HashMap<>(),
                "STANDARD",
                LocalDateTime.now()
            );
            
            storage.computeIfAbsent(bucket, k -> new ConcurrentHashMap<>())
                .put(objectName, object);
            
            totalUploads++;
            
            return toObjectInfo(object);
        }

        /**
         * Upload with storage class
         */
        public GCSObjectInfo uploadWithStorageClass(String bucket, String objectName,
                                                   InputStream data, long contentLength,
                                                   String contentType, String storageClass) 
                throws IOException {
            
            byte[] bytes = data.readAllBytes();
            
            MockGCSObject object = new MockGCSObject(
                bucket,
                objectName,
                bytes,
                contentType,
                new HashMap<>(),
                storageClass,
                LocalDateTime.now()
            );
            
            storage.computeIfAbsent(bucket, k -> new ConcurrentHashMap<>())
                .put(objectName, object);
            
            totalUploads++;
            
            return toObjectInfo(object);
        }

        /**
         * Download object
         */
        public GCSObjectData downloadObject(String bucket, String objectName) throws IOException {
            Map<String, MockGCSObject> bucketStorage = storage.get(bucket);
            
            if (bucketStorage == null) {
                throw new GCSException("Bucket not found: " + bucket);
            }
            
            MockGCSObject object = bucketStorage.get(objectName);
            
            if (object == null) {
                throw new GCSException("Object not found: " + objectName);
            }
            
            totalDownloads++;
            
            return new GCSObjectData(
                object.getBucket(),
                object.getObjectName(),
                object.getData(),
                object.getContentType(),
                object.getMetadata()
            );
        }

        /**
         * Generate signed URL
         */
        public String generateSignedUrl(String bucket, String objectName, 
                                       Duration expiration, String httpMethod) {
            // Mock signed URL
            long expirationTimestamp = System.currentTimeMillis() + expiration.toMillis();
            return "https://storage.googleapis.com/" + bucket + "/" + objectName + 
                   "?X-Goog-Expires=" + expiration.getSeconds() + 
                   "&X-Goog-Date=" + expirationTimestamp;
        }

        /**
         * List objects
         */
        public List<GCSObjectInfo> listObjects(String bucket, String prefix, int maxResults) {
            Map<String, MockGCSObject> bucketStorage = storage.get(bucket);
            
            if (bucketStorage == null) {
                return Collections.emptyList();
            }
            
            return bucketStorage.values().stream()
                .filter(obj -> prefix == null || obj.getObjectName().startsWith(prefix))
                .limit(maxResults)
                .map(this::toObjectInfo)
                .collect(Collectors.toList());
        }

        /**
         * Get object metadata
         */
        public GCSObjectInfo getObjectMetadata(String bucket, String objectName) {
            Map<String, MockGCSObject> bucketStorage = storage.get(bucket);
            
            if (bucketStorage == null) {
                throw new GCSException("Bucket not found: " + bucket);
            }
            
            MockGCSObject object = bucketStorage.get(objectName);
            
            if (object == null) {
                throw new GCSException("Object not found: " + objectName);
            }
            
            return toObjectInfo(object);
        }

        /**
         * Update object metadata
         */
        public void updateObjectMetadata(String bucket, String objectName, 
                                        Map<String, String> metadata) {
            Map<String, MockGCSObject> bucketStorage = storage.get(bucket);
            
            if (bucketStorage != null) {
                MockGCSObject object = bucketStorage.get(objectName);
                if (object != null) {
                    object.getMetadata().clear();
                    object.getMetadata().putAll(metadata);
                }
            }
        }

        /**
         * Delete object
         */
        public void deleteObject(String bucket, String objectName) {
            Map<String, MockGCSObject> bucketStorage = storage.get(bucket);
            
            if (bucketStorage != null) {
                bucketStorage.remove(objectName);
            }
        }

        /**
         * Delete multiple objects
         */
        public int deleteObjects(String bucket, List<String> objectNames) {
            Map<String, MockGCSObject> bucketStorage = storage.get(bucket);
            
            if (bucketStorage == null) {
                return 0;
            }
            
            int deleted = 0;
            for (String objectName : objectNames) {
                if (bucketStorage.remove(objectName) != null) {
                    deleted++;
                }
            }
            
            return deleted;
        }

        /**
         * Copy object
         */
        public GCSObjectInfo copyObject(String sourceBucket, String sourceObjectName,
                                       String destBucket, String destObjectName) {
            Map<String, MockGCSObject> sourceBucketStorage = storage.get(sourceBucket);
            
            if (sourceBucketStorage == null) {
                throw new GCSException("Source bucket not found: " + sourceBucket);
            }
            
            MockGCSObject sourceObject = sourceBucketStorage.get(sourceObjectName);
            
            if (sourceObject == null) {
                throw new GCSException("Source object not found: " + sourceObjectName);
            }
            
            MockGCSObject copiedObject = new MockGCSObject(
                destBucket,
                destObjectName,
                sourceObject.getData(),
                sourceObject.getContentType(),
                new HashMap<>(sourceObject.getMetadata()),
                sourceObject.getStorageClass(),
                LocalDateTime.now()
            );
            
            storage.computeIfAbsent(destBucket, k -> new ConcurrentHashMap<>())
                .put(destObjectName, copiedObject);
            
            return toObjectInfo(copiedObject);
        }

        /**
         * Make object public
         */
        public String makePublic(String bucket, String objectName) {
            // Mock public URL
            return "https://storage.googleapis.com/" + bucket + "/" + objectName;
        }

        /**
         * Get bucket statistics
         */
        public BucketStats getBucketStats(String bucket) {
            Map<String, MockGCSObject> bucketStorage = storage.get(bucket);
            
            if (bucketStorage == null) {
                return new BucketStats(bucket, 0, 0, LocalDateTime.now());
            }
            
            long objectCount = bucketStorage.size();
            long totalSize = bucketStorage.values().stream()
                .mapToLong(obj -> obj.getData().length)
                .sum();
            
            return new BucketStats(bucket, objectCount, totalSize, LocalDateTime.now());
        }

        /**
         * Create bucket
         */
        public void createBucket(String bucket, String location, String storageClass) {
            storage.computeIfAbsent(bucket, k -> new ConcurrentHashMap<>());
            System.out.println("Bucket created: " + bucket + " in " + 
                             (location != null ? location : "us") + 
                             " with storage class " + storageClass);
        }

        /**
         * Delete bucket
         */
        public void deleteBucket(String bucket) {
            storage.remove(bucket);
            System.out.println("Bucket deleted: " + bucket);
        }

        /**
         * Convert to GCSObjectInfo
         */
        private GCSObjectInfo toObjectInfo(MockGCSObject object) {
            return new GCSObjectInfo(
                object.getBucket(),
                object.getObjectName(),
                object.getData().length,
                object.getContentType(),
                object.getMetadata(),
                object.getStorageClass(),
                object.getCreatedAt()
            );
        }
    }

    // Model Classes

    public static class GCSObjectInfo {
        private String bucket;
        private String objectName;
        private long size;
        private String contentType;
        private Map<String, String> metadata;
        private String storageClass;
        private LocalDateTime createdAt;

        public GCSObjectInfo(String bucket, String objectName, long size, 
                            String contentType, Map<String, String> metadata,
                            String storageClass, LocalDateTime createdAt) {
            this.bucket = bucket;
            this.objectName = objectName;
            this.size = size;
            this.contentType = contentType;
            this.metadata = metadata;
            this.storageClass = storageClass;
            this.createdAt = createdAt;
        }

        // Getters
        public String getBucket() { return bucket; }
        public String getObjectName() { return objectName; }
        public long getSize() { return size; }
        public String getContentType() { return contentType; }
        public Map<String, String> getMetadata() { return metadata; }
        public String getStorageClass() { return storageClass; }
        public LocalDateTime getCreatedAt() { return createdAt; }
    }

    public static class GCSObjectData {
        private String bucket;
        private String objectName;
        private byte[] data;
        private String contentType;
        private Map<String, String> metadata;

        public GCSObjectData(String bucket, String objectName, byte[] data,
                            String contentType, Map<String, String> metadata) {
            this.bucket = bucket;
            this.objectName = objectName;
            this.data = data;
            this.contentType = contentType;
            this.metadata = metadata;
        }

        // Getters
        public String getBucket() { return bucket; }
        public String getObjectName() { return objectName; }
        public byte[] getData() { return data; }
        public String getContentType() { return contentType; }
        public Map<String, String> getMetadata() { return metadata; }
    }

    public static class MockGCSObject {
        private String bucket;
        private String objectName;
        private byte[] data;
        private String contentType;
        private Map<String, String> metadata;
        private String storageClass;
        private LocalDateTime createdAt;

        public MockGCSObject(String bucket, String objectName, byte[] data,
                            String contentType, Map<String, String> metadata,
                            String storageClass, LocalDateTime createdAt) {
            this.bucket = bucket;
            this.objectName = objectName;
            this.data = data;
            this.contentType = contentType;
            this.metadata = metadata;
            this.storageClass = storageClass;
            this.createdAt = createdAt;
        }

        // Getters
        public String getBucket() { return bucket; }
        public String getObjectName() { return objectName; }
        public byte[] getData() { return data; }
        public String getContentType() { return contentType; }
        public Map<String, String> getMetadata() { return metadata; }
        public String getStorageClass() { return storageClass; }
        public LocalDateTime getCreatedAt() { return createdAt; }
    }

    public static class GCSProperties {
        private String projectId;
        private String bucketName;
        private String credentialsPath;

        // Getters and Setters
        public String getProjectId() { return projectId; }
        public void setProjectId(String projectId) { this.projectId = projectId; }
        public String getBucketName() { return bucketName; }
        public void setBucketName(String bucketName) { this.bucketName = bucketName; }
        public String getCredentialsPath() { return credentialsPath; }
        public void setCredentialsPath(String credentialsPath) { 
            this.credentialsPath = credentialsPath; 
        }
    }

    public static class SignedUrlRequest {
        private String bucket;
        private String objectName;
        private int expirationMinutes = 60;

        // Getters and Setters
        public String getBucket() { return bucket; }
        public void setBucket(String bucket) { this.bucket = bucket; }
        public String getObjectName() { return objectName; }
        public void setObjectName(String objectName) { this.objectName = objectName; }
        public int getExpirationMinutes() { return expirationMinutes; }
        public void setExpirationMinutes(int expirationMinutes) { 
            this.expirationMinutes = expirationMinutes; 
        }
    }

    public static class CopyObjectRequest {
        private String sourceBucket;
        private String sourceObjectName;
        private String destBucket;
        private String destObjectName;

        // Getters and Setters
        public String getSourceBucket() { return sourceBucket; }
        public void setSourceBucket(String sourceBucket) { 
            this.sourceBucket = sourceBucket; 
        }
        public String getSourceObjectName() { return sourceObjectName; }
        public void setSourceObjectName(String sourceObjectName) { 
            this.sourceObjectName = sourceObjectName; 
        }
        public String getDestBucket() { return destBucket; }
        public void setDestBucket(String destBucket) { this.destBucket = destBucket; }
        public String getDestObjectName() { return destObjectName; }
        public void setDestObjectName(String destObjectName) { 
            this.destObjectName = destObjectName; 
        }
    }

    public static class BucketStats {
        private String bucketName;
        private long objectCount;
        private long totalSize;
        private LocalDateTime timestamp;

        public BucketStats(String bucketName, long objectCount, long totalSize,
                          LocalDateTime timestamp) {
            this.bucketName = bucketName;
            this.objectCount = objectCount;
            this.totalSize = totalSize;
            this.timestamp = timestamp;
        }

        // Getters
        public String getBucketName() { return bucketName; }
        public long getObjectCount() { return objectCount; }
        public long getTotalSize() { return totalSize; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }

    public static class GCSException extends RuntimeException {
        public GCSException(String message) {
            super(message);
        }

        public GCSException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}

/*
 * Real GCS SDK Implementation Example:
 * 
 * import com.google.cloud.storage.Storage;
 * import com.google.cloud.storage.StorageOptions;
 * import com.google.cloud.storage.BlobId;
 * import com.google.cloud.storage.BlobInfo;
 * import com.google.cloud.storage.Blob;
 * import com.google.auth.oauth2.GoogleCredentials;
 * 
 * @Bean
 * public Storage storage(GCSProperties properties) throws IOException {
 *     GoogleCredentials credentials = GoogleCredentials
 *         .fromStream(new FileInputStream(properties.getCredentialsPath()));
 *     
 *     return StorageOptions.newBuilder()
 *         .setProjectId(properties.getProjectId())
 *         .setCredentials(credentials)
 *         .build()
 *         .getService();
 * }
 * 
 * // Upload object
 * BlobId blobId = BlobId.of(bucket, objectName);
 * BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
 *     .setContentType(contentType)
 *     .setMetadata(metadata)
 *     .build();
 * 
 * storage.create(blobInfo, data);
 * 
 * // Generate signed URL
 * URL signedUrl = storage.signUrl(
 *     blobInfo,
 *     expiration.toMinutes(),
 *     TimeUnit.MINUTES,
 *     Storage.SignUrlOption.httpMethod(HttpMethod.GET)
 * );
 */

/*
 * Application Properties:
 * 
 * # Google Cloud Storage Configuration
 * gcs.project-id=my-project-id
 * gcs.bucket-name=my-bucket
 * gcs.credentials-path=/path/to/credentials.json
 * 
 * # Or use environment variable
 * GOOGLE_APPLICATION_CREDENTIALS=/path/to/credentials.json
 */
