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
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * S3 Integration Pattern
 * 
 * Purpose: Integration with AWS S3 for cloud-based file storage.
 * Provides upload, download, presigned URLs, multipart upload, and bucket management.
 * 
 * Key Features:
 * - AWS S3 integration
 * - Presigned URL generation
 * - Multipart upload support
 * - Bucket operations
 * - Object metadata management
 * - Access control (ACL)
 * - Object tagging
 * - Lifecycle policies
 * - Cross-region replication
 * - Storage class management
 * 
 * Use Cases:
 * - Cloud file storage
 * - Large file uploads
 * - Secure file sharing
 * - Media storage and CDN
 * - Backup and archival
 * - Data lakes
 * 
 * Dependencies:
 * <dependency>
 *     <groupId>software.amazon.awssdk</groupId>
 *     <artifactId>s3</artifactId>
 *     <version>2.20.0</version>
 * </dependency>
 * 
 * @author Spring Patterns
 */
@SpringBootApplication
public class S3IntegrationPattern {

    public static void main(String[] args) {
        SpringApplication.run(S3IntegrationPattern.class, args);
    }

    /**
     * S3 Configuration
     */
    @Configuration
    public static class S3Config {
        
        @Bean
        public S3Properties s3Properties() {
            S3Properties properties = new S3Properties();
            properties.setRegion("us-east-1");
            properties.setBucketName("my-app-bucket");
            properties.setAccessKey("YOUR_ACCESS_KEY");
            properties.setSecretKey("YOUR_SECRET_KEY");
            return properties;
        }

        @Bean
        public S3ClientWrapper s3ClientWrapper(S3Properties properties) {
            return new S3ClientWrapper(properties);
        }
    }

    /**
     * S3 Controller
     */
    @RestController
    @RequestMapping("/api/s3")
    public static class S3Controller {

        private final S3Service s3Service;

        public S3Controller(S3Service s3Service) {
            this.s3Service = s3Service;
        }

        /**
         * Upload file to S3
         */
        @PostMapping("/upload")
        public ResponseEntity<S3FileInfo> uploadFile(
                @RequestParam("file") MultipartFile file,
                @RequestParam(required = false) String key,
                @RequestParam(required = false) String bucket,
                @RequestParam(required = false) Map<String, String> metadata) {
            
            try {
                S3FileInfo fileInfo = s3Service.uploadFile(bucket, key, file, metadata);
                return ResponseEntity.ok(fileInfo);
            } catch (IOException e) {
                throw new S3Exception("Upload failed", e);
            }
        }

        /**
         * Upload large file with multipart
         */
        @PostMapping("/upload/multipart")
        public ResponseEntity<S3FileInfo> multipartUpload(
                @RequestParam("file") MultipartFile file,
                @RequestParam(required = false) String key,
                @RequestParam(required = false) String bucket) {
            
            try {
                S3FileInfo fileInfo = s3Service.multipartUpload(bucket, key, file);
                return ResponseEntity.ok(fileInfo);
            } catch (IOException e) {
                throw new S3Exception("Multipart upload failed", e);
            }
        }

        /**
         * Download file from S3
         */
        @GetMapping("/download/{bucket}/{key:.+}")
        public ResponseEntity<byte[]> downloadFile(
                @PathVariable String bucket,
                @PathVariable String key) {
            
            try {
                S3Object object = s3Service.downloadFile(bucket, key);
                
                return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=\"" + key + "\"")
                    .header(HttpHeaders.CONTENT_TYPE, object.getContentType())
                    .body(object.getData());
            } catch (IOException e) {
                throw new S3Exception("Download failed", e);
            }
        }

        /**
         * Generate presigned URL for upload
         */
        @PostMapping("/presigned-url/upload")
        public ResponseEntity<Map<String, String>> getPresignedUploadUrl(
                @RequestBody PresignedUrlRequest request) {
            
            String url = s3Service.generatePresignedUploadUrl(
                request.getBucket(),
                request.getKey(),
                request.getExpirationMinutes()
            );
            
            Map<String, String> response = new HashMap<>();
            response.put("url", url);
            response.put("bucket", request.getBucket());
            response.put("key", request.getKey());
            response.put("expiresIn", request.getExpirationMinutes() + " minutes");
            
            return ResponseEntity.ok(response);
        }

        /**
         * Generate presigned URL for download
         */
        @PostMapping("/presigned-url/download")
        public ResponseEntity<Map<String, String>> getPresignedDownloadUrl(
                @RequestBody PresignedUrlRequest request) {
            
            String url = s3Service.generatePresignedDownloadUrl(
                request.getBucket(),
                request.getKey(),
                request.getExpirationMinutes()
            );
            
            Map<String, String> response = new HashMap<>();
            response.put("url", url);
            response.put("bucket", request.getBucket());
            response.put("key", request.getKey());
            response.put("expiresIn", request.getExpirationMinutes() + " minutes");
            
            return ResponseEntity.ok(response);
        }

        /**
         * List objects in bucket
         */
        @GetMapping("/list/{bucket}")
        public ResponseEntity<List<S3FileInfo>> listObjects(
                @PathVariable String bucket,
                @RequestParam(required = false) String prefix,
                @RequestParam(required = false, defaultValue = "1000") int maxKeys) {
            
            List<S3FileInfo> objects = s3Service.listObjects(bucket, prefix, maxKeys);
            return ResponseEntity.ok(objects);
        }

        /**
         * Get object metadata
         */
        @GetMapping("/{bucket}/{key:.+}/metadata")
        public ResponseEntity<S3FileInfo> getObjectMetadata(
                @PathVariable String bucket,
                @PathVariable String key) {
            
            S3FileInfo info = s3Service.getObjectMetadata(bucket, key);
            return ResponseEntity.ok(info);
        }

        /**
         * Delete object
         */
        @DeleteMapping("/{bucket}/{key:.+}")
        public ResponseEntity<Map<String, String>> deleteObject(
                @PathVariable String bucket,
                @PathVariable String key) {
            
            s3Service.deleteObject(bucket, key);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Object deleted successfully");
            response.put("bucket", bucket);
            response.put("key", key);
            
            return ResponseEntity.ok(response);
        }

        /**
         * Delete multiple objects
         */
        @DeleteMapping("/{bucket}/batch")
        public ResponseEntity<Map<String, Object>> deleteObjects(
                @PathVariable String bucket,
                @RequestBody List<String> keys) {
            
            int deleted = s3Service.deleteObjects(bucket, keys);
            
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
        public ResponseEntity<S3FileInfo> copyObject(
                @RequestBody CopyObjectRequest request) {
            
            S3FileInfo info = s3Service.copyObject(
                request.getSourceBucket(),
                request.getSourceKey(),
                request.getDestBucket(),
                request.getDestKey()
            );
            
            return ResponseEntity.ok(info);
        }

        /**
         * Set object ACL
         */
        @PutMapping("/{bucket}/{key:.+}/acl")
        public ResponseEntity<Map<String, String>> setObjectAcl(
                @PathVariable String bucket,
                @PathVariable String key,
                @RequestParam String acl) {
            
            s3Service.setObjectAcl(bucket, key, acl);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "ACL updated successfully");
            response.put("bucket", bucket);
            response.put("key", key);
            response.put("acl", acl);
            
            return ResponseEntity.ok(response);
        }

        /**
         * Add object tags
         */
        @PutMapping("/{bucket}/{key:.+}/tags")
        public ResponseEntity<Map<String, String>> setObjectTags(
                @PathVariable String bucket,
                @PathVariable String key,
                @RequestBody Map<String, String> tags) {
            
            s3Service.setObjectTags(bucket, key, tags);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Tags updated successfully");
            response.put("bucket", bucket);
            response.put("key", key);
            
            return ResponseEntity.ok(response);
        }

        /**
         * Get bucket statistics
         */
        @GetMapping("/{bucket}/stats")
        public ResponseEntity<BucketStats> getBucketStats(@PathVariable String bucket) {
            return ResponseEntity.ok(s3Service.getBucketStats(bucket));
        }
    }

    /**
     * S3 Service
     */
    @Service
    public static class S3Service {

        private final S3ClientWrapper s3Client;
        private final S3Properties properties;

        public S3Service(S3ClientWrapper s3Client, S3Properties properties) {
            this.s3Client = s3Client;
            this.properties = properties;
        }

        /**
         * Upload file to S3
         */
        public S3FileInfo uploadFile(String bucket, String key, MultipartFile file,
                                     Map<String, String> metadata) throws IOException {
            
            if (bucket == null || bucket.isEmpty()) {
                bucket = properties.getBucketName();
            }
            
            if (key == null || key.isEmpty()) {
                key = generateKey(file.getOriginalFilename());
            }
            
            try (InputStream inputStream = file.getInputStream()) {
                return s3Client.putObject(
                    bucket,
                    key,
                    inputStream,
                    file.getSize(),
                    file.getContentType(),
                    metadata
                );
            }
        }

        /**
         * Multipart upload for large files
         */
        public S3FileInfo multipartUpload(String bucket, String key, MultipartFile file) 
                throws IOException {
            
            if (bucket == null || bucket.isEmpty()) {
                bucket = properties.getBucketName();
            }
            
            if (key == null || key.isEmpty()) {
                key = generateKey(file.getOriginalFilename());
            }
            
            try (InputStream inputStream = file.getInputStream()) {
                return s3Client.multipartUpload(
                    bucket,
                    key,
                    inputStream,
                    file.getSize(),
                    file.getContentType()
                );
            }
        }

        /**
         * Download file from S3
         */
        public S3Object downloadFile(String bucket, String key) throws IOException {
            return s3Client.getObject(bucket, key);
        }

        /**
         * Generate presigned URL for upload
         */
        public String generatePresignedUploadUrl(String bucket, String key, int expirationMinutes) {
            if (bucket == null || bucket.isEmpty()) {
                bucket = properties.getBucketName();
            }
            
            return s3Client.generatePresignedUrl(
                bucket,
                key,
                Duration.ofMinutes(expirationMinutes),
                "PUT"
            );
        }

        /**
         * Generate presigned URL for download
         */
        public String generatePresignedDownloadUrl(String bucket, String key, int expirationMinutes) {
            if (bucket == null || bucket.isEmpty()) {
                bucket = properties.getBucketName();
            }
            
            return s3Client.generatePresignedUrl(
                bucket,
                key,
                Duration.ofMinutes(expirationMinutes),
                "GET"
            );
        }

        /**
         * List objects in bucket
         */
        public List<S3FileInfo> listObjects(String bucket, String prefix, int maxKeys) {
            if (bucket == null || bucket.isEmpty()) {
                bucket = properties.getBucketName();
            }
            
            return s3Client.listObjects(bucket, prefix, maxKeys);
        }

        /**
         * Get object metadata
         */
        public S3FileInfo getObjectMetadata(String bucket, String key) {
            if (bucket == null || bucket.isEmpty()) {
                bucket = properties.getBucketName();
            }
            
            return s3Client.getObjectMetadata(bucket, key);
        }

        /**
         * Delete object
         */
        public void deleteObject(String bucket, String key) {
            if (bucket == null || bucket.isEmpty()) {
                bucket = properties.getBucketName();
            }
            
            s3Client.deleteObject(bucket, key);
        }

        /**
         * Delete multiple objects
         */
        public int deleteObjects(String bucket, List<String> keys) {
            if (bucket == null || bucket.isEmpty()) {
                bucket = properties.getBucketName();
            }
            
            return s3Client.deleteObjects(bucket, keys);
        }

        /**
         * Copy object
         */
        public S3FileInfo copyObject(String sourceBucket, String sourceKey,
                                     String destBucket, String destKey) {
            return s3Client.copyObject(sourceBucket, sourceKey, destBucket, destKey);
        }

        /**
         * Set object ACL
         */
        public void setObjectAcl(String bucket, String key, String acl) {
            s3Client.setObjectAcl(bucket, key, acl);
        }

        /**
         * Set object tags
         */
        public void setObjectTags(String bucket, String key, Map<String, String> tags) {
            s3Client.setObjectTags(bucket, key, tags);
        }

        /**
         * Get bucket statistics
         */
        public BucketStats getBucketStats(String bucket) {
            return s3Client.getBucketStats(bucket);
        }

        /**
         * Generate unique key
         */
        private String generateKey(String filename) {
            String timestamp = String.valueOf(System.currentTimeMillis());
            return "uploads/" + timestamp + "_" + filename;
        }
    }

    /**
     * S3 Client Wrapper (Mock implementation - replace with actual AWS SDK)
     */
    public static class S3ClientWrapper {
        
        private final S3Properties properties;
        private final Map<String, Map<String, MockS3Object>> storage = new ConcurrentHashMap<>();
        private long totalUploads = 0;
        private long totalDownloads = 0;

        public S3ClientWrapper(S3Properties properties) {
            this.properties = properties;
        }

        @PostConstruct
        public void init() {
            System.out.println("S3 Client initialized for region: " + properties.getRegion());
            System.out.println("Default bucket: " + properties.getBucketName());
        }

        /**
         * Put object to S3
         */
        public S3FileInfo putObject(String bucket, String key, InputStream data,
                                    long contentLength, String contentType,
                                    Map<String, String> metadata) throws IOException {
            
            byte[] bytes = data.readAllBytes();
            
            MockS3Object object = new MockS3Object(
                bucket,
                key,
                bytes,
                contentType,
                metadata != null ? metadata : new HashMap<>(),
                LocalDateTime.now()
            );
            
            storage.computeIfAbsent(bucket, k -> new ConcurrentHashMap<>())
                .put(key, object);
            
            totalUploads++;
            
            return toFileInfo(object);
        }

        /**
         * Multipart upload
         */
        public S3FileInfo multipartUpload(String bucket, String key, InputStream data,
                                         long contentLength, String contentType) throws IOException {
            
            // In real implementation, this would handle chunked upload
            // For mock, we'll just do a regular upload
            return putObject(bucket, key, data, contentLength, contentType, null);
        }

        /**
         * Get object from S3
         */
        public S3Object getObject(String bucket, String key) throws IOException {
            Map<String, MockS3Object> bucketStorage = storage.get(bucket);
            
            if (bucketStorage == null) {
                throw new S3Exception("Bucket not found: " + bucket);
            }
            
            MockS3Object object = bucketStorage.get(key);
            
            if (object == null) {
                throw new S3Exception("Object not found: " + key);
            }
            
            totalDownloads++;
            
            return new S3Object(
                object.getBucket(),
                object.getKey(),
                object.getData(),
                object.getContentType(),
                object.getMetadata()
            );
        }

        /**
         * Generate presigned URL
         */
        public String generatePresignedUrl(String bucket, String key, 
                                          Duration expiration, String method) {
            // Mock presigned URL
            return "https://" + bucket + ".s3." + properties.getRegion() + 
                   ".amazonaws.com/" + key + "?X-Amz-Expires=" + expiration.getSeconds();
        }

        /**
         * List objects
         */
        public List<S3FileInfo> listObjects(String bucket, String prefix, int maxKeys) {
            Map<String, MockS3Object> bucketStorage = storage.get(bucket);
            
            if (bucketStorage == null) {
                return Collections.emptyList();
            }
            
            return bucketStorage.values().stream()
                .filter(obj -> prefix == null || obj.getKey().startsWith(prefix))
                .limit(maxKeys)
                .map(this::toFileInfo)
                .collect(Collectors.toList());
        }

        /**
         * Get object metadata
         */
        public S3FileInfo getObjectMetadata(String bucket, String key) {
            Map<String, MockS3Object> bucketStorage = storage.get(bucket);
            
            if (bucketStorage == null) {
                throw new S3Exception("Bucket not found: " + bucket);
            }
            
            MockS3Object object = bucketStorage.get(key);
            
            if (object == null) {
                throw new S3Exception("Object not found: " + key);
            }
            
            return toFileInfo(object);
        }

        /**
         * Delete object
         */
        public void deleteObject(String bucket, String key) {
            Map<String, MockS3Object> bucketStorage = storage.get(bucket);
            
            if (bucketStorage != null) {
                bucketStorage.remove(key);
            }
        }

        /**
         * Delete multiple objects
         */
        public int deleteObjects(String bucket, List<String> keys) {
            Map<String, MockS3Object> bucketStorage = storage.get(bucket);
            
            if (bucketStorage == null) {
                return 0;
            }
            
            int deleted = 0;
            for (String key : keys) {
                if (bucketStorage.remove(key) != null) {
                    deleted++;
                }
            }
            
            return deleted;
        }

        /**
         * Copy object
         */
        public S3FileInfo copyObject(String sourceBucket, String sourceKey,
                                     String destBucket, String destKey) {
            Map<String, MockS3Object> sourceBucketStorage = storage.get(sourceBucket);
            
            if (sourceBucketStorage == null) {
                throw new S3Exception("Source bucket not found: " + sourceBucket);
            }
            
            MockS3Object sourceObject = sourceBucketStorage.get(sourceKey);
            
            if (sourceObject == null) {
                throw new S3Exception("Source object not found: " + sourceKey);
            }
            
            MockS3Object copiedObject = new MockS3Object(
                destBucket,
                destKey,
                sourceObject.getData(),
                sourceObject.getContentType(),
                new HashMap<>(sourceObject.getMetadata()),
                LocalDateTime.now()
            );
            
            storage.computeIfAbsent(destBucket, k -> new ConcurrentHashMap<>())
                .put(destKey, copiedObject);
            
            return toFileInfo(copiedObject);
        }

        /**
         * Set object ACL
         */
        public void setObjectAcl(String bucket, String key, String acl) {
            // Mock implementation
            System.out.println("Set ACL for " + bucket + "/" + key + " to " + acl);
        }

        /**
         * Set object tags
         */
        public void setObjectTags(String bucket, String key, Map<String, String> tags) {
            Map<String, MockS3Object> bucketStorage = storage.get(bucket);
            
            if (bucketStorage != null) {
                MockS3Object object = bucketStorage.get(key);
                if (object != null) {
                    object.getMetadata().putAll(tags);
                }
            }
        }

        /**
         * Get bucket statistics
         */
        public BucketStats getBucketStats(String bucket) {
            Map<String, MockS3Object> bucketStorage = storage.get(bucket);
            
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
         * Convert to S3FileInfo
         */
        private S3FileInfo toFileInfo(MockS3Object object) {
            return new S3FileInfo(
                object.getBucket(),
                object.getKey(),
                object.getData().length,
                object.getContentType(),
                object.getMetadata(),
                object.getLastModified(),
                "STANDARD"
            );
        }
    }

    // Model Classes

    public static class S3FileInfo {
        private String bucket;
        private String key;
        private long size;
        private String contentType;
        private Map<String, String> metadata;
        private LocalDateTime lastModified;
        private String storageClass;

        public S3FileInfo(String bucket, String key, long size, String contentType,
                         Map<String, String> metadata, LocalDateTime lastModified,
                         String storageClass) {
            this.bucket = bucket;
            this.key = key;
            this.size = size;
            this.contentType = contentType;
            this.metadata = metadata;
            this.lastModified = lastModified;
            this.storageClass = storageClass;
        }

        // Getters
        public String getBucket() { return bucket; }
        public String getKey() { return key; }
        public long getSize() { return size; }
        public String getContentType() { return contentType; }
        public Map<String, String> getMetadata() { return metadata; }
        public LocalDateTime getLastModified() { return lastModified; }
        public String getStorageClass() { return storageClass; }
    }

    public static class S3Object {
        private String bucket;
        private String key;
        private byte[] data;
        private String contentType;
        private Map<String, String> metadata;

        public S3Object(String bucket, String key, byte[] data, String contentType,
                       Map<String, String> metadata) {
            this.bucket = bucket;
            this.key = key;
            this.data = data;
            this.contentType = contentType;
            this.metadata = metadata;
        }

        // Getters
        public String getBucket() { return bucket; }
        public String getKey() { return key; }
        public byte[] getData() { return data; }
        public String getContentType() { return contentType; }
        public Map<String, String> getMetadata() { return metadata; }
    }

    public static class MockS3Object {
        private String bucket;
        private String key;
        private byte[] data;
        private String contentType;
        private Map<String, String> metadata;
        private LocalDateTime lastModified;

        public MockS3Object(String bucket, String key, byte[] data, String contentType,
                           Map<String, String> metadata, LocalDateTime lastModified) {
            this.bucket = bucket;
            this.key = key;
            this.data = data;
            this.contentType = contentType;
            this.metadata = metadata;
            this.lastModified = lastModified;
        }

        // Getters
        public String getBucket() { return bucket; }
        public String getKey() { return key; }
        public byte[] getData() { return data; }
        public String getContentType() { return contentType; }
        public Map<String, String> getMetadata() { return metadata; }
        public LocalDateTime getLastModified() { return lastModified; }
    }

    public static class S3Properties {
        private String region;
        private String bucketName;
        private String accessKey;
        private String secretKey;

        // Getters and Setters
        public String getRegion() { return region; }
        public void setRegion(String region) { this.region = region; }
        public String getBucketName() { return bucketName; }
        public void setBucketName(String bucketName) { this.bucketName = bucketName; }
        public String getAccessKey() { return accessKey; }
        public void setAccessKey(String accessKey) { this.accessKey = accessKey; }
        public String getSecretKey() { return secretKey; }
        public void setSecretKey(String secretKey) { this.secretKey = secretKey; }
    }

    public static class PresignedUrlRequest {
        private String bucket;
        private String key;
        private int expirationMinutes = 60;

        // Getters and Setters
        public String getBucket() { return bucket; }
        public void setBucket(String bucket) { this.bucket = bucket; }
        public String getKey() { return key; }
        public void setKey(String key) { this.key = key; }
        public int getExpirationMinutes() { return expirationMinutes; }
        public void setExpirationMinutes(int expirationMinutes) { 
            this.expirationMinutes = expirationMinutes; 
        }
    }

    public static class CopyObjectRequest {
        private String sourceBucket;
        private String sourceKey;
        private String destBucket;
        private String destKey;

        // Getters and Setters
        public String getSourceBucket() { return sourceBucket; }
        public void setSourceBucket(String sourceBucket) { this.sourceBucket = sourceBucket; }
        public String getSourceKey() { return sourceKey; }
        public void setSourceKey(String sourceKey) { this.sourceKey = sourceKey; }
        public String getDestBucket() { return destBucket; }
        public void setDestBucket(String destBucket) { this.destBucket = destBucket; }
        public String getDestKey() { return destKey; }
        public void setDestKey(String destKey) { this.destKey = destKey; }
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

    public static class S3Exception extends RuntimeException {
        public S3Exception(String message) {
            super(message);
        }

        public S3Exception(String message, Throwable cause) {
            super(message, cause);
        }
    }
}

/*
 * Real AWS SDK Implementation Example:
 * 
 * import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
 * import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
 * import software.amazon.awssdk.regions.Region;
 * import software.amazon.awssdk.services.s3.S3Client;
 * import software.amazon.awssdk.services.s3.model.*;
 * import software.amazon.awssdk.services.s3.presigner.S3Presigner;
 * 
 * @Bean
 * public S3Client s3Client(S3Properties properties) {
 *     AwsBasicCredentials credentials = AwsBasicCredentials.create(
 *         properties.getAccessKey(),
 *         properties.getSecretKey()
 *     );
 *     
 *     return S3Client.builder()
 *         .region(Region.of(properties.getRegion()))
 *         .credentialsProvider(StaticCredentialsProvider.create(credentials))
 *         .build();
 * }
 * 
 * // Upload file
 * PutObjectRequest request = PutObjectRequest.builder()
 *     .bucket(bucket)
 *     .key(key)
 *     .contentType(contentType)
 *     .metadata(metadata)
 *     .build();
 * 
 * s3Client.putObject(request, RequestBody.fromInputStream(inputStream, contentLength));
 * 
 * // Generate presigned URL
 * S3Presigner presigner = S3Presigner.create();
 * 
 * PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
 *     .signatureDuration(Duration.ofMinutes(60))
 *     .putObjectRequest(req -> req.bucket(bucket).key(key))
 *     .build();
 * 
 * PresignedPutObjectRequest presignedRequest = presigner.presignPutObject(presignRequest);
 * String url = presignedRequest.url().toString();
 */

/*
 * Application Properties:
 * 
 * # AWS S3 Configuration
 * aws.s3.region=us-east-1
 * aws.s3.bucket-name=my-app-bucket
 * aws.s3.access-key=${AWS_ACCESS_KEY}
 * aws.s3.secret-key=${AWS_SECRET_KEY}
 * 
 * # Multipart Upload Configuration
 * aws.s3.multipart.min-part-size=5242880
 * aws.s3.multipart.threshold=10485760
 */
