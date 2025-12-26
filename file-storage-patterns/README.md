# File Storage Patterns in Spring Boot

This directory contains comprehensive implementations of **7 File Storage Patterns** for Spring Boot applications, covering local storage, cloud storage providers, distributed systems, and CDN integration.

## 📚 Pattern Overview

### 1. **Local File Storage Pattern**
Traditional file system storage with Spring Boot integration.

**Key Features:**
- File upload/download with validation
- Directory management by category
- File metadata tracking
- Size and extension validation
- Batch operations
- Storage statistics
- Automatic cleanup of old files

**Use Cases:**
- Document management systems
- Development/testing environments
- Small-scale applications
- Temporary file storage
- Local caching

---

### 2. **Cloud Storage Pattern**
Abstract cloud storage interface supporting multiple providers.

**Key Features:**
- Provider abstraction (AWS, Azure, GCP)
- Unified storage interface
- Multi-cloud support
- Metadata management
- Streaming upload/download
- Provider switching
- Storage analytics

**Use Cases:**
- Multi-cloud applications
- Cloud-agnostic storage
- Provider migration
- Hybrid cloud storage
- Disaster recovery

---

### 3. **S3 Integration Pattern**
AWS S3 integration with presigned URLs and multipart upload.

**Key Features:**
- AWS S3 SDK integration
- Presigned URL generation (upload/download)
- Multipart upload for large files
- Bucket operations
- Object metadata management
- ACL (Access Control List)
- Object tagging
- Storage class management

**Use Cases:**
- Cloud file storage
- Large file uploads
- Secure file sharing
- Media storage and CDN
- Backup and archival
- Data lakes

---

### 4. **Azure Blob Storage Pattern**
Azure Blob Storage integration with SAS tokens.

**Key Features:**
- Azure Blob Storage SDK
- Container operations
- SAS token generation
- Blob metadata management
- Access tier management (Hot/Cool/Archive)
- Snapshot support
- Lease management
- Blob versioning

**Use Cases:**
- Cloud file storage
- Media storage and streaming
- Document management
- Backup and disaster recovery
- Data archival
- Static website hosting

---

### 5. **Google Cloud Storage Pattern**
Google Cloud Storage integration with signed URLs.

**Key Features:**
- GCS SDK integration
- Bucket/object operations
- Signed URL generation
- Object metadata management
- Storage class management
- Object versioning
- IAM integration
- Lifecycle management

**Use Cases:**
- Cloud file storage
- Media hosting and streaming
- Data analytics and ML
- Backup and archival
- Content distribution
- Static website hosting

---

### 6. **Distributed File System Pattern**
HDFS integration for big data storage.

**Key Features:**
- HDFS integration
- Distributed file storage
- Data replication
- Fault tolerance
- Block-based storage
- High throughput
- Rack awareness
- Append operations
- File permissions (POSIX-like)

**Use Cases:**
- Big data storage
- Data lake implementations
- Hadoop ecosystem integration
- Large-scale data processing
- Log aggregation
- Machine learning datasets

---

### 7. **Content Delivery Network (CDN) Pattern**
CDN integration for global content distribution.

**Key Features:**
- CDN integration (CloudFront, Cloudflare, Akamai)
- Cache invalidation/purging
- TTL management
- Edge location distribution
- SSL/TLS support
- Geographic restrictions
- Cache hit ratio tracking
- Custom error pages

**Use Cases:**
- Static asset delivery
- Media streaming
- Website acceleration
- API response caching
- Global content distribution
- DDoS protection

---

## 🔄 Pattern Comparison Matrix

| Feature | Local Storage | Cloud Storage | S3 | Azure Blob | GCS | HDFS | CDN |
|---------|--------------|---------------|-----|------------|-----|------|-----|
| **Scalability** | Low | High | Very High | Very High | Very High | Very High | Very High |
| **Durability** | Low | High | 99.999999999% | 99.999999999% | 99.999999999% | High | High |
| **Availability** | Medium | High | 99.99% | 99.9% | 99.95% | Very High | 99.99% |
| **Performance** | High (Local) | Medium | High | High | High | Very High | Very High |
| **Cost** | Very Low | Medium | Medium | Medium | Medium | Low | High |
| **Complexity** | Low | Medium | Medium | Medium | Medium | High | Medium |
| **Replication** | Manual | Auto | Auto (3-6 AZs) | Auto (3 copies) | Auto | Configurable | Edge Caching |
| **Global Distribution** | No | Yes | Yes | Yes | Yes | No | Yes |
| **Best For** | Local dev | Multi-cloud | AWS ecosystem | Azure ecosystem | GCP ecosystem | Big data | Static content |

---

## 📊 When to Use Each Pattern

### ✅ Use Local File Storage When:
- Building development/testing environments
- Need simple file operations without cloud dependencies
- Working with small file volumes
- Temporary file storage is sufficient
- Budget constraints limit cloud usage

### ❌ Don't Use Local File Storage When:
- Need high availability and durability
- Require scalability for large file volumes
- Building distributed applications
- Need global access to files

---

### ✅ Use Cloud Storage (Abstract) When:
- Building multi-cloud applications
- Need flexibility to switch providers
- Require cloud-agnostic architecture
- Implementing disaster recovery across clouds
- Want to avoid vendor lock-in

### ❌ Don't Use Cloud Storage (Abstract) When:
- Using provider-specific features extensively
- Optimizing for single cloud provider
- Need simplest implementation

---

### ✅ Use S3 Integration When:
- Already using AWS ecosystem
- Need presigned URLs for secure uploads
- Storing large files with multipart upload
- Require object versioning and lifecycle policies
- Building data lakes on AWS
- Need integration with CloudFront CDN

### ❌ Don't Use S3 When:
- Committed to Azure or GCP ecosystem
- Need file system semantics (use EFS instead)
- Require frequent small updates (use database)

---

### ✅ Use Azure Blob Storage When:
- Already using Azure ecosystem
- Need tiered storage (Hot/Cool/Archive)
- Require SAS token-based access
- Building on Azure App Services
- Need integration with Azure CDN
- Implementing Azure Data Lake

### ❌ Don't Use Azure Blob When:
- Committed to AWS or GCP ecosystem
- Need file system interface (use Azure Files)
- Require POSIX compliance

---

### ✅ Use Google Cloud Storage When:
- Already using GCP ecosystem
- Need integration with BigQuery/Dataflow
- Require strong consistency
- Building ML pipelines on GCP
- Need multi-region storage
- Want unified API with Google services

### ❌ Don't Use GCS When:
- Committed to AWS or Azure ecosystem
- Need lowest cost (S3 Glacier may be cheaper)
- Require file system mount (use Filestore)

---

### ✅ Use Distributed File System (HDFS) When:
- Processing big data with Hadoop/Spark
- Need high throughput for large files
- Require data locality for compute
- Building data lakes on-premises
- Need fault tolerance with replication
- Processing petabyte-scale datasets

### ❌ Don't Use HDFS When:
- Need low latency random access
- Working with small files (<128MB)
- Require file updates (HDFS is append-only)
- Building cloud-native applications (use S3/GCS)

---

### ✅ Use CDN When:
- Serving static assets globally
- Need low latency worldwide
- High traffic website/API
- Streaming media content
- Require DDoS protection
- Want to reduce origin server load

### ❌ Don't Use CDN When:
- Serving dynamic, personalized content
- Low traffic applications
- Content changes very frequently
- Budget constraints (CDN can be expensive)

---

## 🔧 Configuration

### Maven Dependencies

```xml
<!-- Local File Storage & Cloud Storage (Base) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<!-- AWS S3 -->
<dependency>
    <groupId>software.amazon.awssdk</groupId>
    <artifactId>s3</artifactId>
    <version>2.20.0</version>
</dependency>

<!-- Azure Blob Storage -->
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-storage-blob</artifactId>
    <version>12.20.0</version>
</dependency>

<!-- Google Cloud Storage -->
<dependency>
    <groupId>com.google.cloud</groupId>
    <artifactId>google-cloud-storage</artifactId>
    <version>2.20.0</version>
</dependency>

<!-- Apache Hadoop HDFS -->
<dependency>
    <groupId>org.apache.hadoop</groupId>
    <artifactId>hadoop-client</artifactId>
    <version>3.3.4</version>
</dependency>
<dependency>
    <groupId>org.apache.hadoop</groupId>
    <artifactId>hadoop-hdfs</artifactId>
    <version>3.3.4</version>
</dependency>
```

### Application Properties

```properties
# File Upload Configuration
spring.servlet.multipart.enabled=true
spring.servlet.multipart.max-file-size=100MB
spring.servlet.multipart.max-request-size=500MB

# Local File Storage
storage.upload-dir=uploads
storage.max-file-size=10485760
storage.allowed-extensions=jpg,jpeg,png,pdf,doc,docx,txt

# AWS S3
aws.s3.region=us-east-1
aws.s3.bucket-name=my-app-bucket
aws.s3.access-key=${AWS_ACCESS_KEY}
aws.s3.secret-key=${AWS_SECRET_KEY}

# Azure Blob Storage
azure.storage.connection-string=${AZURE_STORAGE_CONNECTION_STRING}
azure.storage.account-name=myaccount
azure.storage.container-name=mycontainer

# Google Cloud Storage
gcs.project-id=my-project-id
gcs.bucket-name=my-bucket
gcs.credentials-path=/path/to/credentials.json

# HDFS
hdfs.namenode.uri=hdfs://localhost:9000
hdfs.replication.factor=3
hdfs.block.size=134217728

# CDN
cdn.provider=cloudfront
cdn.distribution-id=EXAMPLE123
cdn.domain-name=d123abc.cloudfront.net
cdn.origin-domain=my-bucket.s3.amazonaws.com
cdn.default-ttl=86400
```

---

## 🚀 Usage Examples

### 1. Local File Storage

```java
// Upload file
@PostMapping("/upload")
public ResponseEntity<FileInfo> uploadFile(@RequestParam("file") MultipartFile file) {
    FileInfo fileInfo = localStorageService.store(file, "documents");
    return ResponseEntity.ok(fileInfo);
}

// Download file
@GetMapping("/download/{filename}")
public ResponseEntity<Resource> downloadFile(@PathVariable String filename) {
    Resource resource = localStorageService.loadAsResource(filename);
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, 
            "attachment; filename=\"" + filename + "\"")
        .body(resource);
}
```

### 2. S3 Integration

```java
// Upload to S3
@PostMapping("/s3/upload")
public ResponseEntity<S3FileInfo> uploadToS3(@RequestParam("file") MultipartFile file) {
    S3FileInfo fileInfo = s3Service.uploadFile("my-bucket", null, file, null);
    return ResponseEntity.ok(fileInfo);
}

// Generate presigned URL
@PostMapping("/s3/presigned-url")
public ResponseEntity<String> getPresignedUrl(@RequestBody PresignedUrlRequest request) {
    String url = s3Service.generatePresignedUploadUrl(
        request.getBucket(),
        request.getKey(),
        60 // expires in 60 minutes
    );
    return ResponseEntity.ok(url);
}
```

### 3. Azure Blob Storage

```java
// Upload blob
@PostMapping("/azure/upload")
public ResponseEntity<BlobInfo> uploadBlob(@RequestParam("file") MultipartFile file) {
    BlobInfo blobInfo = azureBlobService.uploadBlob("mycontainer", null, file, null);
    return ResponseEntity.ok(blobInfo);
}

// Generate SAS token
@PostMapping("/azure/sas")
public ResponseEntity<String> getSasToken(@RequestBody SasTokenRequest request) {
    String sasUrl = azureBlobService.generateDownloadSasUrl(
        request.getContainer(),
        request.getBlobName(),
        60
    );
    return ResponseEntity.ok(sasUrl);
}
```

### 4. Google Cloud Storage

```java
// Upload to GCS
@PostMapping("/gcs/upload")
public ResponseEntity<GCSObjectInfo> uploadToGCS(@RequestParam("file") MultipartFile file) {
    GCSObjectInfo objectInfo = gcsService.uploadObject("my-bucket", null, file, null);
    return ResponseEntity.ok(objectInfo);
}

// Make object public
@PutMapping("/gcs/{bucket}/{objectName}/public")
public ResponseEntity<String> makePublic(
        @PathVariable String bucket,
        @PathVariable String objectName) {
    String publicUrl = gcsService.makePublic(bucket, objectName);
    return ResponseEntity.ok(publicUrl);
}
```

### 5. HDFS Operations

```java
// Upload to HDFS
@PostMapping("/hdfs/upload")
public ResponseEntity<HDFSFileInfo> uploadToHDFS(
        @RequestParam("file") MultipartFile file,
        @RequestParam String path) {
    HDFSFileInfo fileInfo = hdfsService.uploadFile(path, file, true);
    return ResponseEntity.ok(fileInfo);
}

// List directory
@GetMapping("/hdfs/list")
public ResponseEntity<List<HDFSFileInfo>> listHDFS(@RequestParam String path) {
    List<HDFSFileInfo> files = hdfsService.listDirectory(path, false);
    return ResponseEntity.ok(files);
}
```

### 6. CDN Integration

```java
// Upload to CDN
@PostMapping("/cdn/upload")
public ResponseEntity<CDNContentInfo> uploadToCDN(@RequestParam("file") MultipartFile file) {
    CDNContentInfo contentInfo = cdnService.uploadContent(file, null, 86400L, null);
    return ResponseEntity.ok(contentInfo);
}

// Invalidate cache
@PostMapping("/cdn/invalidate")
public ResponseEntity<InvalidationResult> invalidate(@RequestBody List<String> paths) {
    InvalidationResult result = cdnService.invalidateCache(paths);
    return ResponseEntity.ok(result);
}
```

---

## 🎯 Best Practices

### 1. **File Validation**
Always validate file size, type, and content before processing:

```java
public void validateFile(MultipartFile file) {
    // Check size
    if (file.getSize() > MAX_FILE_SIZE) {
        throw new ValidationException("File too large");
    }
    
    // Check extension
    String extension = getFileExtension(file.getOriginalFilename());
    if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
        throw new ValidationException("File type not allowed");
    }
    
    // Check content type
    if (!file.getContentType().startsWith("image/") && 
        !file.getContentType().equals("application/pdf")) {
        throw new ValidationException("Invalid content type");
    }
}
```

### 2. **Error Handling**
Implement comprehensive error handling:

```java
@ControllerAdvice
public class FileStorageExceptionHandler {
    
    @ExceptionHandler(StorageException.class)
    public ResponseEntity<ErrorResponse> handleStorageException(StorageException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponse("Storage error: " + ex.getMessage()));
    }
    
    @ExceptionHandler(FileNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleFileNotFound(FileNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse("File not found: " + ex.getMessage()));
    }
}
```

### 3. **Security**
- Use presigned URLs for temporary access
- Implement authentication and authorization
- Validate file content (not just extension)
- Sanitize filenames to prevent directory traversal
- Use HTTPS for all transfers

```java
public String sanitizeFilename(String filename) {
    return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
}
```

### 4. **Performance Optimization**

#### Multipart Upload for Large Files
```java
// For files > 100MB, use multipart upload
if (file.getSize() > 100 * 1024 * 1024) {
    return s3Service.multipartUpload(bucket, key, file);
} else {
    return s3Service.uploadFile(bucket, key, file, null);
}
```

#### Streaming for Downloads
```java
@GetMapping("/download/{filename}")
public ResponseEntity<StreamingResponseBody> downloadStream(@PathVariable String filename) {
    StreamingResponseBody stream = outputStream -> {
        // Stream file in chunks
        try (InputStream inputStream = storageService.getInputStream(filename)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
    };
    
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
        .body(stream);
}
```

### 5. **Metadata Management**
Store and retrieve file metadata:

```java
Map<String, String> metadata = new HashMap<>();
metadata.put("uploadedBy", currentUser.getUsername());
metadata.put("uploadedAt", LocalDateTime.now().toString());
metadata.put("originalName", file.getOriginalFilename());
metadata.put("category", "documents");

s3Service.uploadFile(bucket, key, file, metadata);
```

### 6. **Lifecycle Management**
Implement automatic cleanup:

```java
@Scheduled(cron = "0 0 2 * * ?") // Daily at 2 AM
public void cleanupOldFiles() {
    int daysOld = 30;
    int deleted = localStorageService.cleanupOldFiles(daysOld);
    log.info("Cleaned up {} files older than {} days", deleted, daysOld);
}
```

### 7. **Monitoring and Logging**
Track storage operations:

```java
@Slf4j
@Aspect
@Component
public class StorageMonitoringAspect {
    
    @Around("@annotation(Monitorable)")
    public Object monitorStorage(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String operation = joinPoint.getSignature().getName();
        
        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;
            
            log.info("Storage operation {} completed in {}ms", operation, duration);
            return result;
        } catch (Exception e) {
            log.error("Storage operation {} failed: {}", operation, e.getMessage());
            throw e;
        }
    }
}
```

---

## 🔒 Security Considerations

### 1. **Access Control**
- Implement role-based access control (RBAC)
- Use IAM roles and policies for cloud storage
- Generate time-limited access tokens

### 2. **Encryption**
- Enable encryption at rest (cloud providers)
- Use TLS/HTTPS for data in transit
- Consider client-side encryption for sensitive data

### 3. **Virus Scanning**
Integrate antivirus scanning:

```java
public void scanFile(MultipartFile file) throws VirusFoundException {
    // Use ClamAV or similar
    boolean isClean = antivirusService.scan(file.getInputStream());
    if (!isClean) {
        throw new VirusFoundException("Malware detected in file");
    }
}
```

### 4. **Rate Limiting**
Prevent abuse with rate limiting:

```java
@RateLimiter(name = "fileUpload", fallbackMethod = "uploadFallback")
public FileInfo uploadFile(MultipartFile file) {
    return storageService.store(file);
}
```

---

## 📈 Performance Optimization

### 1. **CDN Integration**
Use CDN for frequently accessed files:

```java
// Upload to origin
s3Service.uploadFile(bucket, key, file, null);

// Distribute via CDN
String cdnUrl = cdnService.getPublicUrl(key);
```

### 2. **Caching**
Cache file metadata to reduce database/API calls:

```java
@Cacheable(value = "fileMetadata", key = "#filename")
public FileInfo getFileInfo(String filename) {
    return storageService.getFileInfo(filename);
}
```

### 3. **Async Operations**
Process large files asynchronously:

```java
@Async
public CompletableFuture<FileInfo> uploadAsync(MultipartFile file) {
    FileInfo fileInfo = storageService.store(file);
    return CompletableFuture.completedFuture(fileInfo);
}
```

### 4. **Batch Operations**
Upload/delete multiple files in batches:

```java
public List<FileInfo> uploadBatch(List<MultipartFile> files) {
    return files.parallelStream()
        .map(file -> storageService.store(file))
        .collect(Collectors.toList());
}
```

---

## 🧪 Testing

### Unit Testing
```java
@SpringBootTest
class LocalFileStorageServiceTest {
    
    @Mock
    private StorageProperties properties;
    
    @InjectMocks
    private LocalFileStorageService storageService;
    
    @Test
    void shouldStoreFile() throws IOException {
        // Given
        MockMultipartFile file = new MockMultipartFile(
            "file", "test.txt", "text/plain", "content".getBytes()
        );
        
        // When
        FileInfo fileInfo = storageService.store(file, "test");
        
        // Then
        assertNotNull(fileInfo);
        assertEquals("test.txt", fileInfo.getOriginalFilename());
    }
}
```

### Integration Testing
```java
@SpringBootTest
@AutoConfigureMockMvc
class FileStorageControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void shouldUploadFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file", "test.pdf", "application/pdf", "content".getBytes()
        );
        
        mockMvc.perform(multipart("/api/files/upload")
                .file(file)
                .param("category", "documents"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.filename").exists());
    }
}
```

---

## 🚨 Troubleshooting

### Common Issues

#### 1. **File Upload Fails**
```
Error: Maximum upload size exceeded
```
**Solution:** Increase file size limits in `application.properties`:
```properties
spring.servlet.multipart.max-file-size=100MB
spring.servlet.multipart.max-request-size=500MB
```

#### 2. **S3 Access Denied**
```
Error: Access Denied (Service: Amazon S3)
```
**Solution:** Verify IAM permissions:
```json
{
  "Version": "2012-10-17",
  "Statement": [{
    "Effect": "Allow",
    "Action": ["s3:PutObject", "s3:GetObject", "s3:DeleteObject"],
    "Resource": "arn:aws:s3:::my-bucket/*"
  }]
}
```

#### 3. **HDFS Connection Timeout**
```
Error: Connection refused to NameNode
```
**Solution:** Check HDFS configuration and NameNode status:
```bash
hdfs dfsadmin -report
```

#### 4. **CDN Cache Not Invalidating**
**Solution:** Ensure wildcard paths are properly formatted:
```java
// Correct
cdnService.invalidateCache(Arrays.asList("/images/*"));

// Incorrect
cdnService.invalidateCache(Arrays.asList("/images*"));
```

---

## 📖 Additional Resources

- [AWS S3 Documentation](https://docs.aws.amazon.com/s3/)
- [Azure Blob Storage Documentation](https://docs.microsoft.com/azure/storage/blobs/)
- [Google Cloud Storage Documentation](https://cloud.google.com/storage/docs)
- [Apache Hadoop HDFS Documentation](https://hadoop.apache.org/docs/stable/hadoop-project-dist/hadoop-hdfs/)
- [Spring Boot File Upload](https://spring.io/guides/gs/uploading-files/)

---

## 🎓 Summary

These 7 file storage patterns provide comprehensive solutions for various storage needs:

1. **Local File Storage** - Simple, cost-effective for development and small-scale applications
2. **Cloud Storage (Abstract)** - Flexibility and vendor independence
3. **S3 Integration** - AWS ecosystem, highly scalable object storage
4. **Azure Blob Storage** - Azure ecosystem, tiered storage options
5. **Google Cloud Storage** - GCP ecosystem, strong consistency and ML integration
6. **Distributed File System (HDFS)** - Big data processing, high throughput
7. **CDN** - Global distribution, low latency, high performance

Choose the pattern that best fits your requirements for scalability, performance, cost, and ecosystem integration.
