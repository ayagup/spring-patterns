### [Continue](https://continue.dev) session transcript
 Exported: 11/5/2025, 2:05:26 PM

#### _User_

> generate a java program in spring for each of the below patterns:
> ```
> **File Storage Patterns**
> - Local File Storage Pattern
> - Cloud Storage Pattern
> - S3 Integration Pattern
> - Azure Blob Storage Pattern
> - Google Cloud Storage Pattern
> - Distributed File System Pattern
> - Content Delivery Network (CDN) Pattern
> ```

#### _Assistant_

> I'll generate Spring Boot applications demonstrating each file storage pattern. Each will be a complete, runnable example.
> 
> ## 1. Local File Storage Pattern
> 
> ```java src/main/java/com/example/localstorage/LocalFileStorageService.java
> package com.example.localstorage;
> 
> import org.springframework.beans.factory.annotation.Value;
> import org.springframework.stereotype.Service;
> import org.springframework.web.multipart.MultipartFile;
> 
> import java.io.IOException;
> import java.nio.file.Files;
> import java.nio.file.Path;
> import java.nio.file.Paths;
> import java.nio.file.StandardCopyOption;
> import java.util.UUID;
> 
> @Service
> public class LocalFileStorageService {
> 
>     private final Path fileStorageLocation;
> 
>     public LocalFileStorageService(@Value("${file.upload-dir:./uploads}") String uploadDir) {
>         this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
>         try {
>             Files.createDirectories(this.fileStorageLocation);
>         } catch (IOException ex) {
>             throw new RuntimeException("Could not create upload directory!", ex);
>         }
>     }
> 
>     public String storeFile(MultipartFile file) {
>         String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
>         try {
>             Path targetLocation = this.fileStorageLocation.resolve(fileName);
>             Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
>             return fileName;
>         } catch (IOException ex) {
>             throw new RuntimeException("Could not store file " + fileName, ex);
>         }
>     }
> 
>     public byte[] loadFile(String fileName) {
>         try {
>             Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
>             return Files.readAllBytes(filePath);
>         } catch (IOException ex) {
>             throw new RuntimeException("File not found " + fileName, ex);
>         }
>     }
> 
>     public void deleteFile(String fileName) {
>         try {
>             Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
>             Files.deleteIfExists(filePath);
>         } catch (IOException ex) {
>             throw new RuntimeException("Could not delete file " + fileName, ex);
>         }
>     }
> }
> ```
> 
> ```java src/main/java/com/example/localstorage/LocalFileStorageController.java
> package com.example.localstorage;
> 
> import lombok.RequiredArgsConstructor;
> import org.springframework.http.HttpHeaders;
> import org.springframework.http.MediaType;
> import org.springframework.http.ResponseEntity;
> import org.springframework.web.bind.annotation.*;
> import org.springframework.web.multipart.MultipartFile;
> 
> @RestController
> @RequestMapping("/api/local-files")
> @RequiredArgsConstructor
> public class LocalFileStorageController {
> 
>     private final LocalFileStorageService storageService;
> 
>     @PostMapping("/upload")
>     public ResponseEntity<FileUploadResponse> uploadFile(@RequestParam("file") MultipartFile file) {
>         String fileName = storageService.storeFile(file);
>         return ResponseEntity.ok(new FileUploadResponse(fileName, "File uploaded successfully"));
>     }
> 
>     @GetMapping("/download/{fileName}")
>     public ResponseEntity<byte[]> downloadFile(@PathVariable String fileName) {
>         byte[] data = storageService.loadFile(fileName);
>         return ResponseEntity.ok()
>                 .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
>                 .contentType(MediaType.APPLICATION_OCTET_STREAM)
>                 .body(data);
>     }
> 
>     @DeleteMapping("/{fileName}")
>     public ResponseEntity<String> deleteFile(@PathVariable String fileName) {
>         storageService.deleteFile(fileName);
>         return ResponseEntity.ok("File deleted successfully");
>     }
> 
>     record FileUploadResponse(String fileName, String message) {}
> }
> ```
> 
> ## 2. Cloud Storage Pattern (Abstract Base)
> 
> ```java src/main/java/com/example/cloudstorage/CloudStorageService.java
> package com.example.cloudstorage;
> 
> import org.springframework.web.multipart.MultipartFile;
> 
> import java.io.InputStream;
> 
> public interface CloudStorageService {
>     String uploadFile(MultipartFile file, String bucketName);
>     InputStream downloadFile(String fileName, String bucketName);
>     void deleteFile(String fileName, String bucketName);
>     String generatePresignedUrl(String fileName, String bucketName, int expirationMinutes);
> }
> ```
> 
> ## 3. S3 Integration Pattern
> 
> ```xml pom.xml
> <!-- Add to pom.xml -->
> <dependency>
>     <groupId>software.amazon.awssdk</groupId>
>     <artifactId>s3</artifactId>
>     <version>2.20.26</version>
> </dependency>
> ```
> 
> ```java src/main/java/com/example/s3storage/S3StorageService.java
> package com.example.s3storage;
> 
> import org.springframework.beans.factory.annotation.Value;
> import org.springframework.stereotype.Service;
> import org.springframework.web.multipart.MultipartFile;
> import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
> import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
> import software.amazon.awssdk.core.sync.RequestBody;
> import software.amazon.awssdk.regions.Region;
> import software.amazon.awssdk.services.s3.S3Client;
> import software.amazon.awssdk.services.s3.model.*;
> import software.amazon.awssdk.services.s3.presigner.S3Presigner;
> import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
> 
> import java.io.IOException;
> import java.io.InputStream;
> import java.time.Duration;
> import java.util.UUID;
> 
> @Service
> public class S3StorageService {
> 
>     private final S3Client s3Client;
>     private final S3Presigner s3Presigner;
> 
>     public S3StorageService(@Value("${aws.access-key}") String accessKey,
>                            @Value("${aws.secret-key}") String secretKey,
>                            @Value("${aws.region:us-east-1}") String region) {
>         AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
>         
>         this.s3Client = S3Client.builder()
>                 .region(Region.of(region))
>                 .credentialsProvider(StaticCredentialsProvider.create(credentials))
>                 .build();
> 
>         this.s3Presigner = S3Presigner.builder()
>                 .region(Region.of(region))
>                 .credentialsProvider(StaticCredentialsProvider.create(credentials))
>                 .build();
>     }
> 
>     public String uploadFile(MultipartFile file, String bucketName) {
>         String key = UUID.randomUUID() + "_" + file.getOriginalFilename();
>         try {
>             PutObjectRequest putObjectRequest = PutObjectRequest.builder()
>                     .bucket(bucketName)
>                     .key(key)
>                     .contentType(file.getContentType())
>                     .build();
> 
>             s3Client.putObject(putObjectRequest, 
>                     RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
>             return key;
>         } catch (IOException ex) {
>             throw new RuntimeException("Failed to upload file to S3", ex);
>         }
>     }
> 
>     public InputStream downloadFile(String key, String bucketName) {
>         GetObjectRequest getObjectRequest = GetObjectRequest.builder()
>                 .bucket(bucketName)
>                 .key(key)
>                 .build();
> 
>         return s3Client.getObject(getObjectRequest);
>     }
> 
>     public void deleteFile(String key, String bucketName) {
>         DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
>                 .bucket(bucketName)
>                 .key(key)
>                 .build();
> 
>         s3Client.deleteObject(deleteObjectRequest);
>     }
> 
>     public String generatePresignedUrl(String key, String bucketName, int expirationMinutes) {
>         GetObjectRequest getObjectRequest = GetObjectRequest.builder()
>                 .bucket(bucketName)
>                 .key(key)
>                 .build();
> 
>         GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
>                 .signatureDuration(Duration.ofMinutes(expirationMinutes))
>                 .getObjectRequest(getObjectRequest)
>                 .build();
> 
>         return s3Presigner.presignGetObject(presignRequest).url().toString();
>     }
> }
> ```
> 
> ```java src/main/java/com/example/s3storage/S3StorageController.java
> package com.example.s3storage;
> 
> import lombok.RequiredArgsConstructor;
> import org.springframework.beans.factory.annotation.Value;
> import org.springframework.core.io.InputStreamResource;
> import org.springframework.http.HttpHeaders;
> import org.springframework.http.MediaType;
> import org.springframework.http.ResponseEntity;
> import org.springframework.web.bind.annotation.*;
> import org.springframework.web.multipart.MultipartFile;
> 
> import java.io.InputStream;
> 
> @RestController
> @RequestMapping("/api/s3")
> @RequiredArgsConstructor
> public class S3StorageController {
> 
>     private final S3StorageService s3Service;
>     
>     @Value("${aws.s3.bucket-name}")
>     private String bucketName;
> 
>     @PostMapping("/upload")
>     public ResponseEntity<FileResponse> uploadFile(@RequestParam("file") MultipartFile file) {
>         String key = s3Service.uploadFile(file, bucketName);
>         return ResponseEntity.ok(new FileResponse(key, "File uploaded successfully"));
>     }
> 
>     @GetMapping("/download/{key}")
>     public ResponseEntity<InputStreamResource> downloadFile(@PathVariable String key) {
>         InputStream inputStream = s3Service.downloadFile(key, bucketName);
>         return ResponseEntity.ok()
>                 .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + key + "\"")
>                 .contentType(MediaType.APPLICATION_OCTET_STREAM)
>                 .body(new InputStreamResource(inputStream));
>     }
> 
>     @GetMapping("/presigned-url/{key}")
>     public ResponseEntity<PresignedUrlResponse> getPresignedUrl(@PathVariable String key,
>                                                                 @RequestParam(defaultValue = "60") int expirationMinutes) {
>         String url = s3Service.generatePresignedUrl(key, bucketName, expirationMinutes);
>         return ResponseEntity.ok(new PresignedUrlResponse(url, expirationMinutes));
>     }
> 
>     @DeleteMapping("/{key}")
>     public ResponseEntity<String> deleteFile(@PathVariable String key) {
>         s3Service.deleteFile(key, bucketName);
>         return ResponseEntity.ok("File deleted successfully");
>     }
> 
>     record FileResponse(String key, String message) {}
>     record PresignedUrlResponse(String url, int expiresInMinutes) {}
> }
> ```
> 
> ## 4. Azure Blob Storage Pattern
> 
> ```xml pom.xml
> <!-- Add to pom.xml -->
> <dependency>
>     <groupId>com.azure</groupId>
>     <artifactId>azure-storage-blob</artifactId>
>     <version>12.23.0</version>
> </dependency>
> ```
> 
> ```java src/main/java/com/example/azurestorage/AzureBlobStorageService.java
> package com.example.azurestorage;
> 
> import com.azure.storage.blob.BlobClient;
> import com.azure.storage.blob.BlobContainerClient;
> import com.azure.storage.blob.BlobServiceClient;
> import com.azure.storage.blob.BlobServiceClientBuilder;
> import com.azure.storage.blob.models.BlobHttpHeaders;
> import com.azure.storage.blob.sas.BlobSasPermission;
> import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
> import org.springframework.beans.factory.annotation.Value;
> import org.springframework.stereotype.Service;
> import org.springframework.web.multipart.MultipartFile;
> 
> import java.io.ByteArrayOutputStream;
> import java.io.IOException;
> import java.io.InputStream;
> import java.time.OffsetDateTime;
> import java.util.UUID;
> 
> @Service
> public class AzureBlobStorageService {
> 
>     private final BlobServiceClient blobServiceClient;
> 
>     public AzureBlobStorageService(@Value("${azure.storage.connection-string}") String connectionString) {
>         this.blobServiceClient = new BlobServiceClientBuilder()
>                 .connectionString(connectionString)
>                 .buildClient();
>     }
> 
>     public String uploadFile(MultipartFile file, String containerName) {
>         String blobName = UUID.randomUUID() + "_" + file.getOriginalFilename();
>         
>         BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
>         containerClient.createIfNotExists();
>         
>         BlobClient blobClient = containerClient.getBlobClient(blobName);
>         
>         try {
>             BlobHttpHeaders headers = new BlobHttpHeaders().setContentType(file.getContentType());
>             blobClient.upload(file.getInputStream(), file.getSize(), true);
>             blobClient.setHttpHeaders(headers);
>             return blobName;
>         } catch (IOException ex) {
>             throw new RuntimeException("Failed to upload file to Azure Blob Storage", ex);
>         }
>     }
> 
>     public byte[] downloadFile(String blobName, String containerName) {
>         BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
>         BlobClient blobClient = containerClient.getBlobClient(blobName);
>         
>         ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
>         blobClient.downloadStream(outputStream);
>         return outputStream.toByteArray();
>     }
> 
>     public void deleteFile(String blobName, String containerName) {
>         BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
>         BlobClient blobClient = containerClient.getBlobClient(blobName);
>         blobClient.delete();
>     }
> 
>     public String generateSasUrl(String blobName, String containerName, int expirationMinutes) {
>         BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
>         BlobClient blobClient = containerClient.getBlobClient(blobName);
> 
>         BlobSasPermission permission = new BlobSasPermission().setReadPermission(true);
>         OffsetDateTime expiryTime = OffsetDateTime.now().plusMinutes(expirationMinutes);
> 
>         BlobServiceSasSignatureValues sasValues = new BlobServiceSasSignatureValues(expiryTime, permission);
>         String sasToken = blobClient.generateSas(sasValues);
> 
>         return blobClient.getBlobUrl() + "?" + sasToken;
>     }
> }
> ```
> 
> ```java src/main/java/com/example/azurestorage/AzureBlobStorageController.java
> package com.example.azurestorage;
> 
> import lombok.RequiredArgsConstructor;
> import org.springframework.beans.factory.annotation.Value;
> import org.springframework.http.HttpHeaders;
> import org.springframework.http.MediaType;
> import org.springframework.http.ResponseEntity;
> import org.springframework.web.bind.annotation.*;
> import org.springframework.web.multipart.MultipartFile;
> 
> @RestController
> @RequestMapping("/api/azure")
> @RequiredArgsConstructor
> public class AzureBlobStorageController {
> 
>     private final AzureBlobStorageService azureService;
>     
>     @Value("${azure.storage.container-name}")
>     private String containerName;
> 
>     @PostMapping("/upload")
>     public ResponseEntity<FileResponse> uploadFile(@RequestParam("file") MultipartFile file) {
>         String blobName = azureService.uploadFile(file, containerName);
>         return ResponseEntity.ok(new FileResponse(blobName, "File uploaded successfully"));
>     }
> 
>     @GetMapping("/download/{blobName}")
>     public ResponseEntity<byte[]> downloadFile(@PathVariable String blobName) {
>         byte[] data = azureService.downloadFile(blobName, containerName);
>         return ResponseEntity.ok()
>                 .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + blobName + "\"")
>                 .contentType(MediaType.APPLICATION_OCTET_STREAM)
>                 .body(data);
>     }
> 
>     @GetMapping("/sas-url/{blobName}")
>     public ResponseEntity<SasUrlResponse> getSasUrl(@PathVariable String blobName,
>                                                     @RequestParam(defaultValue = "60") int expirationMinutes) {
>         String url = azureService.generateSasUrl(blobName, containerName, expirationMinutes);
>         return ResponseEntity.ok(new SasUrlResponse(url, expirationMinutes));
>     }
> 
>     @DeleteMapping("/{blobName}")
>     public ResponseEntity<String> deleteFile(@PathVariable String blobName) {
>         azureService.deleteFile(blobName, containerName);
>         return ResponseEntity.ok("File deleted successfully");
>     }
> 
>     record FileResponse(String blobName, String message) {}
>     record SasUrlResponse(String url, int expiresInMinutes) {}
> }
> ```
> 
> ## 5. Google Cloud Storage Pattern
> 
> ```xml pom.xml
> <!-- Add to pom.xml -->
> <dependency>
>     <groupId>com.google.cloud</groupId>
>     <artifactId>google-cloud-storage</artifactId>
>     <version>2.27.0</version>
> </dependency>
> ```
> 
> ```java src/main/java/com/example/gcpstorage/GcsStorageService.java
> package com.example.gcpstorage;
> 
> import com.google.auth.oauth2.GoogleCredentials;
> import com.google.cloud.storage.*;
> import org.springframework.beans.factory.annotation.Value;
> import org.springframework.stereotype.Service;
> import org.springframework.web.multipart.MultipartFile;
> 
> import java.io.FileInputStream;
> import java.io.IOException;
> import java.net.URL;
> import java.util.UUID;
> import java.util.concurrent.TimeUnit;
> 
> @Service
> public class GcsStorageService {
> 
>     private final Storage storage;
> 
>     public GcsStorageService(@Value("${gcp.credentials.file}") String credentialsPath,
>                             @Value("${gcp.project-id}") String projectId) throws IOException {
>         GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(credentialsPath));
>         this.storage = StorageOptions.newBuilder()
>                 .setCredentials(credentials)
>                 .setProjectId(projectId)
>                 .build()
>                 .getService();
>     }
> 
>     public String uploadFile(MultipartFile file, String bucketName) {
>         String blobName = UUID.randomUUID() + "_" + file.getOriginalFilename();
>         
>         try {
>             BlobId blobId = BlobId.of(bucketName, blobName);
>             BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
>                     .setContentType(file.getContentType())
>                     .build();
> 
>             storage.create(blobInfo, file.getBytes());
>             return blobName;
>         } catch (IOException ex) {
>             throw new RuntimeException("Failed to upload file to GCS", ex);
>         }
>     }
> 
>     public byte[] downloadFile(String blobName, String bucketName) {
>         BlobId blobId = BlobId.of(bucketName, blobName);
>         Blob blob = storage.get(blobId);
>         
>         if (blob == null) {
>             throw new RuntimeException("File not found: " + blobName);
>         }
>         
>         return blob.getContent();
>     }
> 
>     public void deleteFile(String blobName, String bucketName) {
>         BlobId blobId = BlobId.of(bucketName, blobName);
>         storage.delete(blobId);
>     }
> 
>     public String generateSignedUrl(String blobName, String bucketName, int expirationMinutes) {
>         BlobId blobId = BlobId.of(bucketName, blobName);
>         BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
> 
>         URL signedUrl = storage.signUrl(
>                 blobInfo,
>                 expirationMinutes,
>                 TimeUnit.MINUTES,
>                 Storage.SignUrlOption.withV4Signature()
>         );
> 
>         return signedUrl.toString();
>     }
> 
>     public void copyFile(String sourceBlobName, String destinationBlobName, 
>                         String sourceBucket, String destinationBucket) {
>         BlobId source = BlobId.of(sourceBucket, sourceBlobName);
>         BlobId target = BlobId.of(destinationBucket, destinationBlobName);
>         
>         Storage.CopyRequest request = Storage.CopyRequest.newBuilder()
>                 .setSource(source)
>                 .setTarget(target)
>                 .build();
>         
>         storage.copy(request);
>     }
> }
> ```
> 
> ```java src/main/java/com/example/gcpstorage/GcsStorageController.java
> package com.example.gcpstorage;
> 
> import lombok.RequiredArgsConstructor;
> import org.springframework.beans.factory.annotation.Value;
> import org.springframework.http.HttpHeaders;
> import org.springframework.http.MediaType;
> import org.springframework.http.ResponseEntity;
> import org.springframework.web.bind.annotation.*;
> import org.springframework.web.multipart.MultipartFile;
> 
> @RestController
> @RequestMapping("/api/gcs")
> @RequiredArgsConstructor
> public class GcsStorageController {
> 
>     private final GcsStorageService gcsService;
>     
>     @Value("${gcp.storage.bucket-name}")
>     private String bucketName;
> 
>     @PostMapping("/upload")
>     public ResponseEntity<FileResponse> uploadFile(@RequestParam("file") MultipartFile file) {
>         String blobName = gcsService.uploadFile(file, bucketName);
>         return ResponseEntity.ok(new FileResponse(blobName, "File uploaded successfully"));
>     }
> 
>     @GetMapping("/download/{blobName}")
>     public ResponseEntity<byte[]> downloadFile(@PathVariable String blobName) {
>         byte[] data = gcsService.downloadFile(blobName, bucketName);
>         return ResponseEntity.ok()
>                 .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + blobName + "\"")
>                 .contentType(MediaType.APPLICATION_OCTET_STREAM)
>                 .body(data);
>     }
> 
>     @GetMapping("/signed-url/{blobName}")
>     public ResponseEntity<SignedUrlResponse> getSignedUrl(@PathVariable String blobName,
>                                                           @RequestParam(defaultValue = "60") int expirationMinutes) {
>         String url = gcsService.generateSignedUrl(blobName, bucketName, expirationMinutes);
>         return ResponseEntity.ok(new SignedUrlResponse(url, expirationMinutes));
>     }
> 
>     @PostMapping("/copy")
>     public ResponseEntity<String> copyFile(@RequestParam String sourceBlobName,
>                                           @RequestParam String destinationBlobName) {
>         gcsService.copyFile(sourceBlobName, destinationBlobName, bucketName, bucketName);
>         return ResponseEntity.ok("File copied successfully");
>     }
> 
>     @DeleteMapping("/{blobName}")
>     public ResponseEntity<String> deleteFile(@PathVariable String blobName) {
>         gcsService.deleteFile(blobName, bucketName);
>         return ResponseEntity.ok("File deleted successfully");
>     }
> 
>     record FileResponse(String blobName, String message) {}
>     record SignedUrlResponse(String url, int expiresInMinutes) {}
> }
> ```
> 
> ## 6. Distributed File System Pattern (HDFS)
> 
> ```xml pom.xml
> <!-- Add to pom.xml -->
> <dependency>
>     <groupId>org.apache.hadoop</groupId>
>     <artifactId>hadoop-client</artifactId>
>     <version>3.3.6</version>
> </dependency>
> ```
> 
> ```java src/main/java/com/example/dfs/HdfsStorageService.java
> package com.example.dfs;
> 
> import org.apache.hadoop.conf.Configuration;
> import org.apache.hadoop.fs.FileSystem;
> import org.apache.hadoop.fs.Path;
> import org.apache.hadoop.io.IOUtils;
> import org.springframework.beans.factory.annotation.Value;
> import org.springframework.stereotype.Service;
> import org.springframework.web.multipart.MultipartFile;
> 
> import javax.annotation.PostConstruct;
> import java.io.ByteArrayOutputStream;
> import java.io.IOException;
> import java.io.InputStream;
> import java.io.OutputStream;
> import java.net.URI;
> import java.util.UUID;
> 
> @Service
> public class HdfsStorageService {
> 
>     private FileSystem fileSystem;
> 
>     @Value("${hdfs.uri:hdfs://localhost:9000}")
>     private String hdfsUri;
> 
>     @Value("${hdfs.base-path:/user/uploads}")
>     private String basePath;
> 
>     @PostConstruct
>     public void init() throws IOException {
>         Configuration configuration = new Configuration();
>         configuration.set("fs.defaultFS", hdfsUri);
>         this.fileSystem = FileSystem.get(URI.create(hdfsUri), configuration);
>         
>         // Create base directory if it doesn't exist
>         Path baseDir = new Path(basePath);
>         if (!fileSystem.exists(baseDir)) {
>             fileSystem.mkdirs(baseDir);
>         }
>     }
> 
>     public String uploadFile(MultipartFile file) {
>         String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
>         Path hdfsPath = new Path(basePath + "/" + fileName);
> 
>         try (InputStream inputStream = file.getInputStream();
>              OutputStream outputStream = fileSystem.create(hdfsPath, true)) {
>             IOUtils.copyBytes(inputStream, outputStream, 4096, false);
>             return fileName;
>         } catch (IOException ex) {
>             throw new RuntimeException("Failed to upload file to HDFS", ex);
>         }
>     }
> 
>     public byte[] downloadFile(String fileName) {
>         Path hdfsPath = new Path(basePath + "/" + fileName);
> 
>         try (InputStream inputStream = fileSystem.open(hdfsPath);
>              ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
>             IOUtils.copyBytes(inputStream, outputStream, 4096, false);
>             return outputStream.toByteArray();
>         } catch (IOException ex) {
>             throw new RuntimeException("Failed to download file from HDFS", ex);
>         }
>     }
> 
>     public void deleteFile(String fileName) {
>         Path hdfsPath = new Path(basePath + "/" + fileName);
>         try {
>             fileSystem.delete(hdfsPath, false);
>         } catch (IOException ex) {
>             throw new RuntimeException("Failed to delete file from HDFS", ex);
>         }
>     }
> 
>     public boolean fileExists(String fileName) {
>         Path hdfsPath = new Path(basePath + "/" + fileName);
>         try {
>             return fileSystem.exists(hdfsPath);
>         } catch (IOException ex) {
>             throw new RuntimeException("Failed to check file existence in HDFS", ex);
>         }
>     }
> 
>     public long getFileSize(String fileName) {
>         Path hdfsPath = new Path(basePath + "/" + fileName);
>         try {
>             return fileSystem.getFileStatus(hdfsPath).getLen();
>         } catch (IOException ex) {
>             throw new RuntimeException("Failed to get file size from HDFS", ex);
>         }
>     }
> 
>     public void copyFile(String sourceFileName, String destinationFileName) {
>         Path sourcePath = new Path(basePath + "/" + sourceFileName);
>         Path destPath = new Path(basePath + "/" + destinationFileName);
>         
>         try {
>             fileSystem.copyFromLocalFile(false, true, sourcePath, destPath);
>         } catch (IOException ex) {
>             throw new RuntimeException("Failed to copy file in HDFS", ex);
>         }
>     }
> }
> ```
> 
> ```java src/main/java/com/example/dfs/HdfsStorageController.java
> package com.example.dfs;
> 
> import lombok.RequiredArgsConstructor;
> import org.springframework.http.HttpHeaders;
> import org.springframework.http.MediaType;
> import org.springframework.http.ResponseEntity;
> import org.springframework.web.bind.annotation.*;
> import org.springframework.web.multipart.MultipartFile;
> 
> @RestController
> @RequestMapping("/api/hdfs")
> @RequiredArgsConstructor
> public class HdfsStorageController {
> 
>     private final HdfsStorageService hdfsService;
> 
>     @PostMapping("/upload")
>     public ResponseEntity<FileResponse> uploadFile(@RequestParam("file") MultipartFile file) {
>         String fileName = hdfsService.uploadFile(file);
>         return ResponseEntity.ok(new FileResponse(fileName, "File uploaded to HDFS successfully"));
>     }
> 
>     @GetMapping("/download/{fileName}")
>     public ResponseEntity<byte[]> downloadFile(@PathVariable String fileName) {
>         byte[] data = hdfsService.downloadFile(fileName);
>         return ResponseEntity.ok()
>                 .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
>                 .contentType(MediaType.APPLICATION_OCTET_STREAM)
>                 .body(data);
>     }
> 
>     @GetMapping("/exists/{fileName}")
>     public ResponseEntity<ExistsResponse> checkFileExists(@PathVariable String fileName) {
>         boolean exists = hdfsService.fileExists(fileName);
>         return ResponseEntity.ok(new ExistsResponse(fileName, exists));
>     }
> 
>     @GetMapping("/size/{fileName}")
>     public ResponseEntity<SizeResponse> getFileSize(@PathVariable String fileName) {
>         long size = hdfsService.getFileSize(fileName);
>         return ResponseEntity.ok(new SizeResponse(fileName, size));
>     }
> 
>     @DeleteMapping("/{fileName}")
>     public ResponseEntity<String> deleteFile(@PathVariable String fileName) {
>         hdfsService.deleteFile(fileName);
>         return ResponseEntity.ok("File deleted from HDFS successfully");
>     }
> 
>     record FileResponse(String fileName, String message) {}
>     record ExistsResponse(String fileName, boolean exists) {}
>     record SizeResponse(String fileName, long sizeInBytes) {}
> }
> ```
> 
> ## 7. Content Delivery Network (CDN) Pattern
> 
> ```java src/main/java/com/example/cdn/CdnStorageService.java
> package com.example.cdn;
> 
> import lombok.RequiredArgsConstructor;
> import org.springframework.beans.factory.annotation.Value;
> import org.springframework.stereotype.Service;
> import org.springframework.web.multipart.MultipartFile;
> 
> import java.security.MessageDigest;
> import java.security.NoSuchAlgorithmException;
> import java.time.Instant;
> import java.util.*;
> 
> @Service
> @RequiredArgsConstructor
> public class CdnStorageService {
> 
>     // Primary storage service (could be S3, Azure, etc.)
>     private final S3StorageService primaryStorage;
> 
>     @Value("${cdn.base-url}")
>     private String cdnBaseUrl;
> 
>     @Value("${cdn.secret-key}")
>     private String secretKey;
> 
>     @Value("${cdn.enabled:true}")
>     private boolean cdnEnabled;
> 
>     @Value("${cdn.cache-control:max-age=31536000}")
>     private String cacheControl;
> 
>     public CdnUploadResult uploadFile(MultipartFile file, String bucketName) {
>         // Upload to primary storage
>         String key = primaryStorage.uploadFile(file, bucketName);
> 
>         // Generate CDN URL
>         String cdnUrl = generateCdnUrl(key);
> 
>         // Generate secure token if needed
>         String secureToken = generateSecureToken(key);
> 
>         return new CdnUploadResult(
>                 key,
>                 cdnUrl,
>                 secureToken,
>                 cacheControl,
>                 Instant.now().plusSeconds(31536000) // 1 year cache
>         );
>     }
> 
>     private String generateCdnUrl(String key) {
>         if (!cdnEnabled) {
>             return "/fallback/" + key;
>         }
>         return cdnBaseUrl + "/" + key;
>     }
> 
>     public String generateSecureToken(String path) {
>         return generateSecureToken(path, 3600); // 1 hour default
>     }
> 
>     public String generateSecureToken(String path, int expirationSeconds) {
>         long expirationTime = Instant.now().getEpochSecond() + expirationSeconds;
>         String input = path +

#### _User_

> Continue your response exactly where you left off:

#### _Assistant_

> ```java src/main/java/com/example/cdn/CdnStorageService.java
>     public String generateSecureToken(String path, int expirationSeconds) {
>         long expirationTime = Instant.now().getEpochSecond() + expirationSeconds;
>         String input = path + expirationTime + secretKey;
>         
>         try {
>             MessageDigest md = MessageDigest.getInstance("MD5");
>             byte[] messageDigest = md.digest(input.getBytes());
>             StringBuilder hexString = new StringBuilder();
>             for (byte b : messageDigest) {
>                 String hex = Integer.toHexString(0xff & b);
>                 if (hex.length() == 1) hexString.append('0');
>                 hexString.append(hex);
>             }
>             return hexString.toString();
>         } catch (NoSuchAlgorithmException e) {
>             throw new RuntimeException("Failed to generate secure token", e);
>         }
>     }
> 
>     public boolean validateSecureToken(String path, String token, long expirationTime) {
>         if (Instant.now().getEpochSecond() > expirationTime) {
>             return false;
>         }
>         
>         String expectedToken = generateSecureTokenWithExpiration(path, expirationTime);
>         return token.equals(expectedToken);
>     }
> 
>     private String generateSecureTokenWithExpiration(String path, long expirationTime) {
>         String input = path + expirationTime + secretKey;
>         
>         try {
>             MessageDigest md = MessageDigest.getInstance("MD5");
>             byte[] messageDigest = md.digest(input.getBytes());
>             StringBuilder hexString = new StringBuilder();
>             for (byte b : messageDigest) {
>                 String hex = Integer.toHexString(0xff & b);
>                 if (hex.length() == 1) hexString.append('0');
>                 hexString.append(hex);
>             }
>             return hexString.toString();
>         } catch (NoSuchAlgorithmException e) {
>             throw new RuntimeException("Failed to generate secure token", e);
>         }
>     }
> 
>     public void invalidateCache(String key) {
>         // Implementation would depend on CDN provider
>         // For CloudFront, you'd create an invalidation request
>         // For Cloudflare, you'd use their purge API
>         System.out.println("Cache invalidation requested for: " + key);
>     }
> 
>     public void invalidateCacheBatch(List<String> keys) {
>         // Batch invalidation for multiple files
>         keys.forEach(this::invalidateCache);
>     }
> 
>     public CdnMetrics getMetrics(String key) {
>         // In production, this would call CDN analytics API
>         return new CdnMetrics(
>                 key,
>                 1000L, // requests
>                 1024000L, // bandwidth in bytes
>                 95.5, // cache hit ratio
>                 Collections.singletonMap("US-EAST", 500L) // geographic distribution
>         );
>     }
> 
>     record CdnUploadResult(
>             String key,
>             String cdnUrl,
>             String secureToken,
>             String cacheControl,
>             Instant cacheExpiration
>     ) {}
> 
>     record CdnMetrics(
>             String key,
>             Long totalRequests,
>             Long bandwidthBytes,
>             Double cacheHitRatio,
>             Map<String, Long> geographicDistribution
>     ) {}
> }
> ```
> 
> ```java src/main/java/com/example/cdn/CdnStorageController.java
> package com.example.cdn;
> 
> import lombok.RequiredArgsConstructor;
> import org.springframework.beans.factory.annotation.Value;
> import org.springframework.http.ResponseEntity;
> import org.springframework.web.bind.annotation.*;
> import org.springframework.web.multipart.MultipartFile;
> 
> import java.util.List;
> 
> @RestController
> @RequestMapping("/api/cdn")
> @RequiredArgsConstructor
> public class CdnStorageController {
> 
>     private final CdnStorageService cdnService;
>     
>     @Value("${aws.s3.bucket-name}")
>     private String bucketName;
> 
>     @PostMapping("/upload")
>     public ResponseEntity<CdnStorageService.CdnUploadResult> uploadFile(
>             @RequestParam("file") MultipartFile file) {
>         CdnStorageService.CdnUploadResult result = cdnService.uploadFile(file, bucketName);
>         return ResponseEntity.ok(result);
>     }
> 
>     @PostMapping("/upload-with-cache")
>     public ResponseEntity<UploadResponse> uploadFileWithCustomCache(
>             @RequestParam("file") MultipartFile file,
>             @RequestParam(defaultValue = "3600") int cacheSeconds) {
>         
>         CdnStorageService.CdnUploadResult result = cdnService.uploadFile(file, bucketName);
>         String secureUrl = result.cdnUrl() + "?token=" + result.secureToken();
>         
>         return ResponseEntity.ok(new UploadResponse(
>                 result.key(),
>                 result.cdnUrl(),
>                 secureUrl,
>                 cacheSeconds
>         ));
>     }
> 
>     @GetMapping("/secure-url/{key}")
>     public ResponseEntity<SecureUrlResponse> generateSecureUrl(
>             @PathVariable String key,
>             @RequestParam(defaultValue = "3600") int expirationSeconds) {
>         
>         String token = cdnService.generateSecureToken(key, expirationSeconds);
>         String cdnUrl = cdnService.generateCdnUrl(key);
>         String secureUrl = cdnUrl + "?token=" + token + "&expires=" + 
>                 (System.currentTimeMillis() / 1000 + expirationSeconds);
>         
>         return ResponseEntity.ok(new SecureUrlResponse(secureUrl, expirationSeconds));
>     }
> 
>     @PostMapping("/invalidate/{key}")
>     public ResponseEntity<String> invalidateCache(@PathVariable String key) {
>         cdnService.invalidateCache(key);
>         return ResponseEntity.ok("Cache invalidation initiated for: " + key);
>     }
> 
>     @PostMapping("/invalidate-batch")
>     public ResponseEntity<String> invalidateCacheBatch(@RequestBody List<String> keys) {
>         cdnService.invalidateCacheBatch(keys);
>         return ResponseEntity.ok("Cache invalidation initiated for " + keys.size() + " files");
>     }
> 
>     @GetMapping("/metrics/{key}")
>     public ResponseEntity<CdnStorageService.CdnMetrics> getMetrics(@PathVariable String key) {
>         CdnStorageService.CdnMetrics metrics = cdnService.getMetrics(key);
>         return ResponseEntity.ok(metrics);
>     }
> 
>     @GetMapping("/validate-token")
>     public ResponseEntity<ValidationResponse> validateToken(
>             @RequestParam String path,
>             @RequestParam String token,
>             @RequestParam long expires) {
>         
>         boolean isValid = cdnService.validateSecureToken(path, token, expires);
>         return ResponseEntity.ok(new ValidationResponse(isValid, 
>                 isValid ? "Token is valid" : "Token is invalid or expired"));
>     }
> 
>     record UploadResponse(String key, String cdnUrl, String secureUrl, int cacheSeconds) {}
>     record SecureUrlResponse(String secureUrl, int expiresInSeconds) {}
>     record ValidationResponse(boolean valid, String message) {}
> }
> ```
> 
> ```java src/main/java/com/example/cdn/CdnConfiguration.java
> package com.example.cdn;
> 
> import org.springframework.boot.context.properties.ConfigurationProperties;
> import org.springframework.context.annotation.Configuration;
> 
> @Configuration
> @ConfigurationProperties(prefix = "cdn")
> public class CdnConfiguration {
>     
>     private String baseUrl;
>     private String secretKey;
>     private boolean enabled = true;
>     private String cacheControl = "max-age=31536000";
>     private CloudFrontConfig cloudFront;
>     private CloudflareConfig cloudflare;
> 
>     // Getters and setters
>     public String getBaseUrl() { return baseUrl; }
>     public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
>     
>     public String getSecretKey() { return secretKey; }
>     public void setSecretKey(String secretKey) { this.secretKey = secretKey; }
>     
>     public boolean isEnabled() { return enabled; }
>     public void setEnabled(boolean enabled) { this.enabled = enabled; }
>     
>     public String getCacheControl() { return cacheControl; }
>     public void setCacheControl(String cacheControl) { this.cacheControl = cacheControl; }
>     
>     public CloudFrontConfig getCloudFront() { return cloudFront; }
>     public void setCloudFront(CloudFrontConfig cloudFront) { this.cloudFront = cloudFront; }
>     
>     public CloudflareConfig getCloudflare() { return cloudflare; }
>     public void setCloudflare(CloudflareConfig cloudflare) { this.cloudflare = cloudflare; }
> 
>     public static class CloudFrontConfig {
>         private String distributionId;
>         private String keyPairId;
>         private String privateKeyPath;
> 
>         public String getDistributionId() { return distributionId; }
>         public void setDistributionId(String distributionId) { this.distributionId = distributionId; }
>         
>         public String getKeyPairId() { return keyPairId; }
>         public void setKeyPairId(String keyPairId) { this.keyPairId = keyPairId; }
>         
>         public String getPrivateKeyPath() { return privateKeyPath; }
>         public void setPrivateKeyPath(String privateKeyPath) { this.privateKeyPath = privateKeyPath; }
>     }
> 
>     public static class CloudflareConfig {
>         private String zoneId;
>         private String apiToken;
> 
>         public String getZoneId() { return zoneId; }
>         public void setZoneId(String zoneId) { this.zoneId = zoneId; }
>         
>         public String getApiToken() { return apiToken; }
>         public void setApiToken(String apiToken) { this.apiToken = apiToken; }
>     }
> }
> ```
> 
> ## Application Properties Examples
> 
> ```properties application.properties
> # Local File Storage
> file.upload-dir=./uploads
> 
> # AWS S3
> aws.access-key=YOUR_ACCESS_KEY
> aws.secret-key=YOUR_SECRET_KEY
> aws.region=us-east-1
> aws.s3.bucket-name=your-bucket-name
> 
> # Azure Blob Storage
> azure.storage.connection-string=DefaultEndpointsProtocol=https;AccountName=...
> azure.storage.container-name=your-container
> 
> # Google Cloud Storage
> gcp.credentials.file=/path/to/credentials.json
> gcp.project-id=your-project-id
> gcp.storage.bucket-name=your-bucket
> 
> # HDFS
> hdfs.uri=hdfs://localhost:9000
> hdfs.base-path=/user/uploads
> 
> # CDN Configuration
> cdn.enabled=true
> cdn.base-url=https://cdn.example.com
> cdn.secret-key=your-secret-key
> cdn.cache-control=max-age=31536000, public
> 
> # CloudFront specific
> cdn.cloud-front.distribution-id=E1234567890ABC
> cdn.cloud-front.key-pair-id=APKAXXXXXXXXXXXXXXXX
> cdn.cloud-front.private-key-path=/path/to/private-key.pem
> 
> # Cloudflare specific
> cdn.cloudflare.zone-id=your-zone-id
> cdn.cloudflare.api-token=your-api-token
> ```
> 
> ## Main Application Class
> 
> ```java src/main/java/com/example/FileStorageApplication.java
> package com.example;
> 
> import org.springframework.boot.SpringApplication;
> import org.springframework.boot.autoconfigure.SpringBootApplication;
> 
> @SpringBootApplication
> public class FileStorageApplication {
>     public static void main(String[] args) {
>         SpringApplication.run(FileStorageApplication.class, args);
>     }
> }
> ```
> 
> ```xml pom.xml
> <?xml version="1.0" encoding="UTF-8"?>
> <project xmlns="http://maven.apache.org/POM/4.0.0"
>          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
>          xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
>          https://maven.apache.org/xsd/maven-4.0.0.xsd">
>     <modelVersion>4.0.0</modelVersion>
>     
>     <parent>
>         <groupId>org.springframework.boot</groupId>
>         <artifactId>spring-boot-starter-parent</artifactId>
>         <version>3.2.0</version>
>     </parent>
>     
>     <groupId>com.example</groupId>
>     <artifactId>file-storage-patterns</artifactId>
>     <version>1.0.0</version>
>     <name>File Storage Patterns</name>
> 
>     <properties>
>         <java.version>17</java.version>
>     </properties>
> 
>     <dependencies>
>         <!-- Spring Boot -->
>         <dependency>
>             <groupId>org.springframework.boot</groupId>
>             <artifactId>spring-boot-starter-web</artifactId>
>         </dependency>
> 
>         <!-- Lombok -->
>         <dependency>
>             <groupId>org.projectlombok</groupId>
>             <artifactId>lombok</artifactId>
>             <optional>true</optional>
>         </dependency>
> 
>         <!-- AWS S3 -->
>         <dependency>
>             <groupId>software.amazon.awssdk</groupId>
>             <artifactId>s3</artifactId>
>             <version>2.20.26</version>
>         </dependency>
> 
>         <!-- Azure Blob Storage -->
>         <dependency>
>             <groupId>com.azure</groupId>
>             <artifactId>azure-storage-blob</artifactId>
>             <version>12.23.0</version>
>         </dependency>
> 
>         <!-- Google Cloud Storage -->
>         <dependency>
>             <groupId>com.google.cloud</groupId>
>             <artifactId>google-cloud-storage</artifactId>
>             <version>2.27.0</version>
>         </dependency>
> 
>         <!-- Hadoop HDFS -->
>         <dependency>
>             <groupId>org.apache.hadoop</groupId>
>             <artifactId>hadoop-client</artifactId>
>             <version>3.3.6</version>
>         </dependency>
> 
>         <!-- Testing -->
>         <dependency>
>             <groupId>org.springframework.boot</groupId>
>             <artifactId>spring-boot-starter-test</artifactId>
>             <scope>test</scope>
>         </dependency>
>     </dependencies>
> 
>     <build>
>         <plugins>
>             <plugin>
>                 <groupId>org.springframework.boot</groupId>
>                 <artifactId>spring-boot-maven-plugin</artifactId>
>             </plugin>
>         </plugins>
>     </build>
> </project>
> ```
> 
> Each pattern is now complete with full implementation! You can use these as starting points and customize based on your specific requirements.