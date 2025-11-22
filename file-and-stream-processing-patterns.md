### [Continue](https://continue.dev) session transcript
 Exported: 11/5/2025, 1:13:40 PM

#### _User_

> generate a java program in spring for each of the below patterns:
> ```
> **File and Stream Processing Patterns**
> - Stream Processing Pattern
> - Batch File Processing Pattern
> - File Upload Pattern
> - File Download Pattern
> - Multipart File Handling Pattern
> - Resource Handling Pattern
> - Input Stream Pattern
> - Output Stream Pattern
> - File System Integration Pattern
> - FTP Integration Pattern
> - SFTP Integration Pattern
> ```

#### _Assistant_

> # Spring Boot File and Stream Processing Patterns
> 
> I'll create a comprehensive Spring Boot application demonstrating all these patterns.
> 
> ## 1. Project Structure and Dependencies
> 
> ```xml pom.xml
> <?xml version="1.0" encoding="UTF-8"?>
> <project xmlns="http://maven.apache.org/POM/4.0.0"
>          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
>          xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
>          http://maven.apache.org/xsd/maven-4.0.0.xsd">
>     <modelVersion>4.0.0</modelVersion>
>     
>     <parent>
>         <groupId>org.springframework.boot</groupId>
>         <artifactId>spring-boot-starter-parent</artifactId>
>         <version>3.2.0</version>
>     </parent>
>     
>     <groupId>com.example</groupId>
>     <artifactId>file-stream-patterns</artifactId>
>     <version>1.0.0</version>
>     
>     <properties>
>         <java.version>17</java.version>
>     </properties>
>     
>     <dependencies>
>         <!-- Spring Boot Starters -->
>         <dependency>
>             <groupId>org.springframework.boot</groupId>
>             <artifactId>spring-boot-starter-web</artifactId>
>         </dependency>
>         
>         <dependency>
>             <groupId>org.springframework.boot</groupId>
>             <artifactId>spring-boot-starter-batch</artifactId>
>         </dependency>
>         
>         <dependency>
>             <groupId>org.springframework.boot</groupId>
>             <artifactId>spring-boot-starter-integration</artifactId>
>         </dependency>
>         
>         <!-- Spring Integration FTP/SFTP -->
>         <dependency>
>             <groupId>org.springframework.integration</groupId>
>             <artifactId>spring-integration-ftp</artifactId>
>         </dependency>
>         
>         <dependency>
>             <groupId>org.springframework.integration</groupId>
>             <artifactId>spring-integration-sftp</artifactId>
>         </dependency>
>         
>         <dependency>
>             <groupId>org.springframework.integration</groupId>
>             <artifactId>spring-integration-file</artifactId>
>         </dependency>
>         
>         <!-- Apache Commons -->
>         <dependency>
>             <groupId>commons-io</groupId>
>             <artifactId>commons-io</artifactId>
>             <version>2.15.0</version>
>         </dependency>
>         
>         <dependency>
>             <groupId>org.apache.commons</groupId>
>             <artifactId>commons-lang3</artifactId>
>         </dependency>
>         
>         <!-- Lombok -->
>         <dependency>
>             <groupId>org.projectlombok</groupId>
>             <artifactId>lombok</artifactId>
>             <optional>true</optional>
>         </dependency>
>         
>         <!-- H2 Database for Batch -->
>         <dependency>
>             <groupId>com.h2database</groupId>
>             <artifactId>h2</artifactId>
>             <scope>runtime</scope>
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
> ## 2. Application Configuration
> 
> ```yaml application.yml
> spring:
>   application:
>     name: file-stream-patterns
>   servlet:
>     multipart:
>       max-file-size: 10MB
>       max-request-size: 10MB
>       enabled: true
>   batch:
>     jdbc:
>       initialize-schema: always
>     job:
>       enabled: false
> 
> server:
>   port: 8080
> 
> file:
>   upload:
>     directory: ${java.io.tmpdir}/uploads
>   download:
>     directory: ${java.io.tmpdir}/downloads
>   batch:
>     input-directory: ${java.io.tmpdir}/batch/input
>     output-directory: ${java.io.tmpdir}/batch/output
>     archive-directory: ${java.io.tmpdir}/batch/archive
> 
> ftp:
>   host: localhost
>   port: 21
>   username: ftpuser
>   password: ftppass
>   remote-directory: /upload
>   local-directory: ${java.io.tmpdir}/ftp
> 
> sftp:
>   host: localhost
>   port: 22
>   username: sftpuser
>   password: sftppass
>   remote-directory: /upload
>   local-directory: ${java.io.tmpdir}/sftp
>   private-key: ${user.home}/.ssh/id_rsa
> ```
> 
> ## 3. Main Application Class
> 
> ```java src/main/java/com/example/filestream/FileStreamPatternsApplication.java
> package com.example.filestream;
> 
> import org.springframework.boot.SpringApplication;
> import org.springframework.boot.autoconfigure.SpringBootApplication;
> import org.springframework.integration.annotation.IntegrationComponentScan;
> import org.springframework.integration.config.EnableIntegration;
> import org.springframework.scheduling.annotation.EnableScheduling;
> 
> @SpringBootApplication
> @EnableIntegration
> @IntegrationComponentScan
> @EnableScheduling
> public class FileStreamPatternsApplication {
>     
>     public static void main(String[] args) {
>         SpringApplication.run(FileStreamPatternsApplication.class, args);
>     }
> }
> ```
> 
> ## 4. Stream Processing Pattern
> 
> ```java src/main/java/com/example/filestream/pattern/StreamProcessingService.java
> package com.example.filestream.pattern;
> 
> import lombok.extern.slf4j.Slf4j;
> import org.springframework.stereotype.Service;
> 
> import java.io.*;
> import java.nio.charset.StandardCharsets;
> import java.util.stream.Stream;
> 
> @Slf4j
> @Service
> public class StreamProcessingService {
>     
>     /**
>      * Process large files using Java Streams for memory efficiency
>      */
>     public long processLargeFile(InputStream inputStream, OutputStream outputStream) throws IOException {
>         long lineCount = 0;
>         
>         try (BufferedReader reader = new BufferedReader(
>                 new InputStreamReader(inputStream, StandardCharsets.UTF_8));
>              BufferedWriter writer = new BufferedWriter(
>                 new OutputStreamWriter(outputStream, StandardCharsets.UTF_8))) {
>             
>             Stream<String> lines = reader.lines();
>             
>             // Process stream lazily
>             lineCount = lines
>                 .filter(line -> !line.trim().isEmpty())
>                 .map(String::toUpperCase)
>                 .peek(line -> {
>                     try {
>                         writer.write(line);
>                         writer.newLine();
>                     } catch (IOException e) {
>                         throw new UncheckedIOException(e);
>                     }
>                 })
>                 .count();
>             
>             writer.flush();
>         }
>         
>         log.info("Processed {} lines", lineCount);
>         return lineCount;
>     }
>     
>     /**
>      * Process data in chunks using streaming
>      */
>     public void processInChunks(InputStream inputStream, int chunkSize) throws IOException {
>         byte[] buffer = new byte[chunkSize];
>         int bytesRead;
>         long totalBytes = 0;
>         
>         while ((bytesRead = inputStream.read(buffer)) != -1) {
>             // Process each chunk
>             processChunk(buffer, bytesRead);
>             totalBytes += bytesRead;
>         }
>         
>         log.info("Processed {} bytes in chunks of {}", totalBytes, chunkSize);
>     }
>     
>     private void processChunk(byte[] data, int length) {
>         // Custom chunk processing logic
>         log.debug("Processing chunk of {} bytes", length);
>     }
> }
> ```
> 
> ## 5. Batch File Processing Pattern
> 
> ```java src/main/java/com/example/filestream/pattern/batch/BatchFileProcessingConfig.java
> package com.example.filestream.pattern.batch;
> 
> import lombok.RequiredArgsConstructor;
> import org.springframework.batch.core.Job;
> import org.springframework.batch.core.Step;
> import org.springframework.batch.core.job.builder.JobBuilder;
> import org.springframework.batch.core.repository.JobRepository;
> import org.springframework.batch.core.step.builder.StepBuilder;
> import org.springframework.batch.item.file.FlatFileItemReader;
> import org.springframework.batch.item.file.FlatFileItemWriter;
> import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
> import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
> import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
> import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
> import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
> import org.springframework.beans.factory.annotation.Value;
> import org.springframework.context.annotation.Bean;
> import org.springframework.context.annotation.Configuration;
> import org.springframework.core.io.FileSystemResource;
> import org.springframework.transaction.PlatformTransactionManager;
> 
> @Configuration
> @RequiredArgsConstructor
> public class BatchFileProcessingConfig {
>     
>     @Value("${file.batch.input-directory}")
>     private String inputDirectory;
>     
>     @Value("${file.batch.output-directory}")
>     private String outputDirectory;
>     
>     @Bean
>     public FlatFileItemReader<CustomerRecord> customerReader() {
>         return new FlatFileItemReaderBuilder<CustomerRecord>()
>             .name("customerReader")
>             .resource(new FileSystemResource(inputDirectory + "/customers.csv"))
>             .delimited()
>             .names("id", "name", "email", "age")
>             .fieldSetMapper(new BeanWrapperFieldSetMapper<>() {{
>                 setTargetType(CustomerRecord.class);
>             }})
>             .build();
>     }
>     
>     @Bean
>     public CustomerRecordProcessor customerProcessor() {
>         return new CustomerRecordProcessor();
>     }
>     
>     @Bean
>     public FlatFileItemWriter<CustomerRecord> customerWriter() {
>         BeanWrapperFieldExtractor<CustomerRecord> fieldExtractor = new BeanWrapperFieldExtractor<>();
>         fieldExtractor.setNames(new String[]{"id", "name", "email", "age", "processed"});
>         
>         DelimitedLineAggregator<CustomerRecord> lineAggregator = new DelimitedLineAggregator<>();
>         lineAggregator.setDelimiter(",");
>         lineAggregator.setFieldExtractor(fieldExtractor);
>         
>         return new FlatFileItemWriterBuilder<CustomerRecord>()
>             .name("customerWriter")
>             .resource(new FileSystemResource(outputDirectory + "/processed-customers.csv"))
>             .lineAggregator(lineAggregator)
>             .build();
>     }
>     
>     @Bean
>     public Step processCustomerStep(JobRepository jobRepository,
>                                    PlatformTransactionManager transactionManager) {
>         return new StepBuilder("processCustomerStep", jobRepository)
>             .<CustomerRecord, CustomerRecord>chunk(100, transactionManager)
>             .reader(customerReader())
>             .processor(customerProcessor())
>             .writer(customerWriter())
>             .build();
>     }
>     
>     @Bean
>     public Job batchFileProcessingJob(JobRepository jobRepository,
>                                      Step processCustomerStep) {
>         return new JobBuilder("batchFileProcessingJob", jobRepository)
>             .start(processCustomerStep)
>             .build();
>     }
> }
> ```
> 
> ```java src/main/java/com/example/filestream/pattern/batch/CustomerRecord.java
> package com.example.filestream.pattern.batch;
> 
> import lombok.Data;
> 
> @Data
> public class CustomerRecord {
>     private Long id;
>     private String name;
>     private String email;
>     private Integer age;
>     private Boolean processed;
> }
> ```
> 
> ```java src/main/java/com/example/filestream/pattern/batch/CustomerRecordProcessor.java
> package com.example.filestream.pattern.batch;
> 
> import lombok.extern.slf4j.Slf4j;
> import org.springframework.batch.item.ItemProcessor;
> 
> @Slf4j
> public class CustomerRecordProcessor implements ItemProcessor<CustomerRecord, CustomerRecord> {
>     
>     @Override
>     public CustomerRecord process(CustomerRecord item) {
>         // Business logic processing
>         item.setName(item.getName().toUpperCase());
>         item.setEmail(item.getEmail().toLowerCase());
>         item.setProcessed(true);
>         
>         log.debug("Processed record: {}", item.getId());
>         return item;
>     }
> }
> ```
> 
> ## 6. File Upload Pattern
> 
> ```java src/main/java/com/example/filestream/pattern/FileUploadService.java
> package com.example.filestream.pattern;
> 
> import lombok.extern.slf4j.Slf4j;
> import org.apache.commons.io.FileUtils;
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
> @Slf4j
> @Service
> public class FileUploadService {
>     
>     @Value("${file.upload.directory}")
>     private String uploadDirectory;
>     
>     /**
>      * Upload a single file
>      */
>     public String uploadFile(MultipartFile file) throws IOException {
>         validateFile(file);
>         
>         // Create upload directory if not exists
>         Path uploadPath = Paths.get(uploadDirectory);
>         if (!Files.exists(uploadPath)) {
>             Files.createDirectories(uploadPath);
>         }
>         
>         // Generate unique filename
>         String originalFilename = file.getOriginalFilename();
>         String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
>         String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
>         
>         // Save file
>         Path filePath = uploadPath.resolve(uniqueFilename);
>         Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
>         
>         log.info("File uploaded: {}", uniqueFilename);
>         return uniqueFilename;
>     }
>     
>     /**
>      * Upload file with validation and metadata
>      */
>     public FileUploadResult uploadFileWithMetadata(MultipartFile file) throws IOException {
>         String filename = uploadFile(file);
>         
>         return FileUploadResult.builder()
>             .filename(filename)
>             .originalFilename(file.getOriginalFilename())
>             .size(file.getSize())
>             .contentType(file.getContentType())
>             .uploadPath(uploadDirectory)
>             .build();
>     }
>     
>     private void validateFile(MultipartFile file) {
>         if (file.isEmpty()) {
>             throw new IllegalArgumentException("File is empty");
>         }
>         
>         // Add more validations as needed
>         long maxSize = 10 * 1024 * 1024; // 10MB
>         if (file.getSize() > maxSize) {
>             throw new IllegalArgumentException("File size exceeds maximum limit");
>         }
>     }
> }
> ```
> 
> ```java src/main/java/com/example/filestream/pattern/FileUploadResult.java
> package com.example.filestream.pattern;
> 
> import lombok.Builder;
> import lombok.Data;
> 
> @Data
> @Builder
> public class FileUploadResult {
>     private String filename;
>     private String originalFilename;
>     private Long size;
>     private String contentType;
>     private String uploadPath;
> }
> ```
> 
> ## 7. File Download Pattern
> 
> ```java src/main/java/com/example/filestream/pattern/FileDownloadService.java
> package com.example.filestream.pattern;
> 
> import lombok.extern.slf4j.Slf4j;
> import org.springframework.beans.factory.annotation.Value;
> import org.springframework.core.io.Resource;
> import org.springframework.core.io.UrlResource;
> import org.springframework.stereotype.Service;
> 
> import java.io.IOException;
> import java.net.MalformedURLException;
> import java.nio.file.Files;
> import java.nio.file.Path;
> import java.nio.file.Paths;
> 
> @Slf4j
> @Service
> public class FileDownloadService {
>     
>     @Value("${file.download.directory}")
>     private String downloadDirectory;
>     
>     /**
>      * Load file as Resource for download
>      */
>     public Resource loadFileAsResource(String filename) {
>         try {
>             Path filePath = Paths.get(downloadDirectory).resolve(filename).normalize();
>             Resource resource = new UrlResource(filePath.toUri());
>             
>             if (resource.exists() && resource.isReadable()) {
>                 log.info("File loaded for download: {}", filename);
>                 return resource;
>             } else {
>                 throw new FileNotFoundException("File not found: " + filename);
>             }
>         } catch (MalformedURLException e) {
>             throw new FileNotFoundException("File not found: " + filename, e);
>         }
>     }
>     
>     /**
>      * Get file metadata
>      */
>     public FileMetadata getFileMetadata(String filename) throws IOException {
>         Path filePath = Paths.get(downloadDirectory).resolve(filename).normalize();
>         
>         if (!Files.exists(filePath)) {
>             throw new FileNotFoundException("File not found: " + filename);
>         }
>         
>         return FileMetadata.builder()
>             .filename(filename)
>             .size(Files.size(filePath))
>             .contentType(Files.probeContentType(filePath))
>             .lastModified(Files.getLastModifiedTime(filePath).toMillis())
>             .build();
>     }
> }
> ```
> 
> ```java src/main/java/com/example/filestream/pattern/FileNotFoundException.java
> package com.example.filestream.pattern;
> 
> public class FileNotFoundException extends RuntimeException {
>     public FileNotFoundException(String message) {
>         super(message);
>     }
>     
>     public FileNotFoundException(String message, Throwable cause) {
>         super(message, cause);
>     }
> }
> ```
> 
> ```java src/main/java/com/example/filestream/pattern/FileMetadata.java
> package com.example.filestream.pattern;
> 
> import lombok.Builder;
> import lombok.Data;
> 
> @Data
> @Builder
> public class FileMetadata {
>     private String filename;
>     private Long size;
>     private String contentType;
>     private Long lastModified;
> }
> ```
> 
> ## 8. Multipart File Handling Pattern
> 
> ```java src/main/java/com/example/filestream/pattern/MultipartFileHandlingService.java
> package com.example.filestream.pattern;
> 
> import lombok.RequiredArgsConstructor;
> import lombok.extern.slf4j.Slf4j;
> import org.springframework.beans.factory.annotation.Value;
> import org.springframework.stereotype.Service;
> import org.springframework.web.multipart.MultipartFile;
> 
> import java.io.IOException;
> import java.nio.file.Files;
> import java.nio.file.Path;
> import java.nio.file.Paths;
> import java.util.ArrayList;
> import java.util.List;
> import java.util.UUID;
> import java.util.stream.Collectors;
> 
> @Slf4j
> @Service
> @RequiredArgsConstructor
> public class MultipartFileHandlingService {
>     
>     @Value("${file.upload.directory}")
>     private String uploadDirectory;
>     
>     private final FileUploadService fileUploadService;
>     
>     /**
>      * Handle multiple file uploads
>      */
>     public List<FileUploadResult> uploadMultipleFiles(MultipartFile[] files) {
>         List<FileUploadResult> results = new ArrayList<>();
>         
>         for (MultipartFile file : files) {
>             try {
>                 FileUploadResult result = fileUploadService.uploadFileWithMetadata(file);
>                 results.add(result);
>             } catch (IOException e) {
>                 log.error("Failed to upload file: {}", file.getOriginalFilename(), e);
>                 // Continue with other files
>             }
>         }
>         
>         log.info("Uploaded {} out of {} files", results.size(), files.length);
>         return results;
>     }
>     
>     /**
>      * Upload files with different processing logic per type
>      */
>     public List<FileUploadResult> uploadFilesWithTypeHandling(MultipartFile[] files) {
>         return List.of(files).stream()
>             .map(this::processFileByType)
>             .collect(Collectors.toList());
>     }
>     
>     private FileUploadResult processFileByType(MultipartFile file) {
>         try {
>             String contentType = file.getContentType();
>             
>             if (contentType != null && contentType.startsWith("image/")) {
>                 return handleImageUpload(file);
>             } else if (contentType != null && contentType.equals("application/pdf")) {
>                 return handlePdfUpload(file);
>             } else {
>                 return fileUploadService.uploadFileWithMetadata(file);
>             }
>         } catch (IOException e) {
>             log.error("Error processing file: {}", file.getOriginalFilename(), e);
>             throw new RuntimeException("File processing failed", e);
>         }
>     }
>     
>     private FileUploadResult handleImageUpload(MultipartFile file) throws IOException {
>         log.info("Processing image file: {}", file.getOriginalFilename());
>         // Add image-specific processing (resize, compress, etc.)
>         return fileUploadService.uploadFileWithMetadata(file);
>     }
>     
>     private FileUploadResult handlePdfUpload(MultipartFile file) throws IOException {
>         log.info("Processing PDF file: {}", file.getOriginalFilename());
>         // Add PDF-specific processing
>         return fileUploadService.uploadFileWithMetadata(file);
>     }
> }
> ```
> 
> ## 9. Resource Handling Pattern
> 
> ```java src/main/java/com/example/filestream/pattern/ResourceHandlingService.java
> package com.example.filestream.pattern;
> 
> import lombok.extern.slf4j.Slf4j;
> import org.apache.commons.io.IOUtils;
> import org.springframework.core.io.ClassPathResource;
> import org.springframework.core.io.Resource;
> import org.springframework.core.io.ResourceLoader;
> import org.springframework.stereotype.Service;
> 
> import java.io.IOException;
> import java.io.InputStream;
> import java.nio.charset.StandardCharsets;
> 
> @Slf4j
> @Service
> public class ResourceHandlingService {
>     
>     private final ResourceLoader resourceLoader;
>     
>     public ResourceHandlingService(ResourceLoader resourceLoader) {
>         this.resourceLoader = resourceLoader;
>     }
>     
>     /**
>      * Load resource from classpath
>      */
>     public String loadClasspathResource(String path) throws IOException {
>         Resource resource = new ClassPathResource(path);
>         
>         try (InputStream inputStream = resource.getInputStream()) {
>             String content = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
>             log.info("Loaded classpath resource: {}", path);
>             return content;
>         }
>     }
>     
>     /**
>      * Load resource using ResourceLoader (supports multiple protocols)
>      */
>     public Resource loadResource(String location) {
>         // Supports: classpath:, file:, http:, etc.
>         Resource resource = resourceLoader.getResource(location);
>         
>         if (!resource.exists()) {
>             throw new FileNotFoundException("Resource not found: " + location);
>         }
>         
>         log.info("Loaded resource: {}", location);
>         return resource;
>     }
>     
>     /**
>      * Read resource content
>      */
>     public byte[] readResourceContent(String location) throws IOException {
>         Resource resource = loadResource(location);
>         
>         try (InputStream inputStream = resource.getInputStream()) {
>             return IOUtils.toByteArray(inputStream);
>         }
>     }
> }
> ```
> 
> ## 10. Input/Output Stream Patterns
> 
> ```java src/main/java/com/example/filestream/pattern/InputStreamPatternService.java
> package com.example.filestream.pattern;
> 
> import lombok.extern.slf4j.Slf4j;
> import org.springframework.stereotype.Service;
> 
> import java.io.*;
> import java.util.zip.GZIPInputStream;
> 
> @Slf4j
> @Service
> public class InputStreamPatternService {
>     
>     /**
>      * Read file using BufferedInputStream for efficiency
>      */
>     public byte[] readFileBuffered(File file) throws IOException {
>         try (FileInputStream fis = new FileInputStream(file);
>              BufferedInputStream bis = new BufferedInputStream(fis);
>              ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
>             
>             byte[] buffer = new byte[8192];
>             int bytesRead;
>             
>             while ((bytesRead = bis.read(buffer)) != -1) {
>                 baos.write(buffer, 0, bytesRead);
>             }
>             
>             log.info("Read {} bytes from file", baos.size());
>             return baos.toByteArray();
>         }
>     }
>     
>     /**
>      * Read compressed file
>      */
>     public String readGzipFile(File file) throws IOException {
>         try (FileInputStream fis = new FileInputStream(file);
>              GZIPInputStream gis = new GZIPInputStream(fis);
>              InputStreamReader isr = new InputStreamReader(gis);
>              BufferedReader reader = new BufferedReader(isr)) {
>             
>             StringBuilder content = new StringBuilder();
>             String line;
>             
>             while ((line = reader.readLine()) != null) {
>                 content.append(line).append("\n");
>             }
>             
>             log.info("Read compressed file: {}", file.getName());
>             return content.toString();
>         }
>     }
>     
>     /**
>      * Read with mark and reset support
>      */
>     public void readWithMarkReset(InputStream inputStream) throws IOException {
>         try (BufferedInputStream bis = new BufferedInputStream(inputStream)) {
>             bis.mark(1024); // Mark position
>             
>             byte[] header = new byte[10];
>             bis.read(header);
>             
>             // Process header...
>             
>             bis.reset(); // Reset to marked position
>             
>             // Read full stream
>             byte[] fullContent = bis.readAllBytes();
>             
>             log.info("Read {} bytes with mark/reset", fullContent.length);
>         }
>     }
> }
> ```
> 
> ```java src/main/java/com/example/filestream/pattern/OutputStreamPatternService.java
> package com.example.filestream.pattern;
> 
> import lombok.extern.slf4j.Slf4j;
> import org.springframework.stereotype.Service;
> 
> import java.io.*;
> import java.util.zip.GZIPOutputStream;
> 
> @Slf4j
> @Service
> public class OutputStreamPatternService {
>     
>     /**
>      * Write file using BufferedOutputStream
>      */
>     public void writeFileBuffered(File file, byte[] data) throws IOException {
>         try (FileOutputStream fos = new FileOutputStream(file);
>              BufferedOutputStream bos = new BufferedOutputStream(fos)) {
>             
>             bos.write(data);
>             bos.flush();
>             
>             log.info("Wrote {} bytes to file", data.length);
>         }
>     }
>     
>     /**
>      * Write compressed file
>      */
>     public void writeGzipFile(File file, String content) throws IOException {
>         try (FileOutputStream fos = new FileOutputStream(file);
>              GZIPOutputStream gos = new GZIPOutputStream(fos);
>              OutputStreamWriter osw = new OutputStreamWriter(gos);
>              BufferedWriter writer = new BufferedWriter(osw)) {
>             
>             writer.write(content);
>             writer.flush();
>             
>             log.info("Wrote compressed file: {}", file.getName());
>         }
>     }
>     
>     /**
>      * Write using DataOutputStream for primitive types
>      */
>     public void writeStructuredData(File file, StructuredData data) throws IOException {
>         try (FileOutputStream fos = new FileOutputStream(file);
>              DataOutputStream dos = new DataOutputStream(fos)) {
>             
>             dos.writeInt(data.getId());
>             dos.writeUTF(data.getName());
>             dos.writeDouble(data.getValue());
>             dos.flush();
>             
>             log.info("Wrote structured data to file");
>         }
>     }
>     
>     public static class StructuredData {
>         private int id;
>         private String name;
>         private double value;
>         
>         public StructuredData(int id, String name, double value) {
>             this.id = id;
>             this.name = name;
>             this.value = value;
>         }
>         
>         public int getId() { return id; }
>         public String getName() { return name; }
>         public double getValue() { return value; }
>     }
> }
> ```
> 
> ## 11. File System Integration Pattern
> 
> ```java src/main/java/com/example/filestream/pattern/integration/FileSystemIntegrationConfig.java
> package com.example.filestream.pattern.integration;
> 
> import lombok.extern.slf4j.Slf4j;
> import org.springframework.beans.factory.annotation.Value;
> import org.springframework.context.annotation.Bean;
> import org.springframework.context.annotation.Configuration;
> import org.springframework.integration.annotation.ServiceActivator;
> import org.springframework.integration.channel.DirectChannel;
> import org.springframework.integration.core.MessageSource;
> import org.springframework.integration.file.FileReadingMessageSource;
> import org.springframework.integration.file.FileWritingMessageHandler;
> import org.springframework.integration.file.filters.SimplePatternFileListFilter;
> import org.springframework.integration.file.support.FileExistsMode;
> import org.springframework.messaging.MessageChannel;
> import org.springframework.messaging.MessageHandler;
> 
> import java.io.File;
> 
> @Slf4j
> @Configuration
> public class FileSystemIntegrationConfig {
>     
>     @Value("${file.batch.input-directory}")
>     private String inputDirectory;
>     
>     @Value("${file.batch.output-directory}")
>     private String outputDirectory;
>     
>     @Bean
>     public MessageChannel fileInputChannel() {
>         return new DirectChannel();
>     }
>     
>     @Bean
>     public MessageChannel fileOutputChannel() {
>         return new DirectChannel();
>     }
>     
>     /**
>      * File reading message source
>      */
>     @Bean
>     public MessageSource<File> fileReadingMessageSource() {
>         FileReadingMessageSource source = new FileReadingMessageSource();
>         source.setDirectory(new File(inputDirectory));
>         source.setFilter(new SimplePatternFileListFilter("*.txt"));
>         source.setAutoCreateDirectory(true);
>         
>         log.info("Configured file reading source for: {}", inputDirectory);
>         return source;
>     }
>     
>     /**
>      * File writing message handler
>      */
>     @Bean
>     @ServiceActivator(inputChannel = "fileOutputChannel")
>     public MessageHandler fileWritingMessageHandler() {
>         FileWritingMessageHandler handler = new FileWritingMessageHandler(new File(outputDirectory));
>         handler.setFileExistsMode(FileExistsMode.REPLACE);
>         handler.setExpectReply(false);
>         handler.setAutoCreateDirectory(true);
>         
>         log.info("Configured file writing handler for: {}", outputDirectory);
>         return handler;
>     }
> }
> ```
> 
> ```java src/main/java/com/example/filestream/pattern/integration/FileSystemProcessor.java
> package com.example.filestream.pattern.integration;
> 
> import lombok.extern.slf4j.Slf4j;
> import org.springframework.integration.annotation.Transformer;
> import org.springframework.messaging.handler.annotation.Header;
> import org.springframework.stereotype.Component;
> 
> import java.io.File;
> import java.io.IOException;
> import java.nio.file.Files;
> 
> @Slf4j
> @Component
> public class FileSystemProcessor {
>     
>     @Transformer(inputChannel = "fileInputChannel", outputChannel = "fileOutputChannel")
>     public String processFile(File inputFile, @Header("file_name") String filename) throws IOException {
>         log.info("Processing file: {}", filename);
>         
>         String content = Files.readString(inputFile.toPath());
>         String processed = content.toUpperCase();
>         
>         log.info("File processed: {}", filename);
>         return processed;
>     }
> }
> ```
> 
> ## 12. FTP Integration Pattern
> 
> ```java src/main/java/com/example/filestream/pattern/integration/FtpIntegrationConfig.java
> package com.example.filestream.pattern.integration;
> 
> import org.apache.commons.net.ftp.FTPFile;
> import org.

#### _User_

> continue exactly where you left off

#### _Assistant_

> ```java src/main/java/com/example/filestream/pattern/integration/FtpIntegrationConfig.java
> package com.example.filestream.pattern.integration;
> 
> import org.apache.commons.net.ftp.FTPFile;
> import org.springframework.beans.factory.annotation.Value;
> import org.springframework.context.annotation.Bean;
> import org.springframework.context.annotation.Configuration;
> import org.springframework.integration.annotation.InboundChannelAdapter;
> import org.springframework.integration.annotation.Poller;
> import org.springframework.integration.annotation.ServiceActivator;
> import org.springframework.integration.channel.DirectChannel;
> import org.springframework.integration.core.MessageSource;
> import org.springframework.integration.file.remote.session.CachingSessionFactory;
> import org.springframework.integration.file.remote.session.SessionFactory;
> import org.springframework.integration.ftp.inbound.FtpInboundFileSynchronizer;
> import org.springframework.integration.ftp.inbound.FtpInboundFileSynchronizingMessageSource;
> import org.springframework.integration.ftp.outbound.FtpMessageHandler;
> import org.springframework.integration.ftp.session.DefaultFtpSessionFactory;
> import org.springframework.messaging.MessageChannel;
> import org.springframework.messaging.MessageHandler;
> 
> import java.io.File;
> 
> @Configuration
> public class FtpIntegrationConfig {
>     
>     @Value("${ftp.host}")
>     private String ftpHost;
>     
>     @Value("${ftp.port}")
>     private int ftpPort;
>     
>     @Value("${ftp.username}")
>     private String ftpUsername;
>     
>     @Value("${ftp.password}")
>     private String ftpPassword;
>     
>     @Value("${ftp.remote-directory}")
>     private String ftpRemoteDirectory;
>     
>     @Value("${ftp.local-directory}")
>     private String ftpLocalDirectory;
>     
>     @Bean
>     public SessionFactory<FTPFile> ftpSessionFactory() {
>         DefaultFtpSessionFactory factory = new DefaultFtpSessionFactory();
>         factory.setHost(ftpHost);
>         factory.setPort(ftpPort);
>         factory.setUsername(ftpUsername);
>         factory.setPassword(ftpPassword);
>         
>         return new CachingSessionFactory<>(factory);
>     }
>     
>     @Bean
>     public MessageChannel ftpInboundChannel() {
>         return new DirectChannel();
>     }
>     
>     @Bean
>     public MessageChannel ftpOutboundChannel() {
>         return new DirectChannel();
>     }
>     
>     /**
>      * FTP Inbound - Download files from FTP server
>      */
>     @Bean
>     public FtpInboundFileSynchronizer ftpInboundFileSynchronizer() {
>         FtpInboundFileSynchronizer synchronizer = new FtpInboundFileSynchronizer(ftpSessionFactory());
>         synchronizer.setDeleteRemoteFiles(false);
>         synchronizer.setRemoteDirectory(ftpRemoteDirectory);
>         synchronizer.setFilter(new org.springframework.integration.ftp.filters.FtpSimplePatternFileListFilter("*.txt"));
>         
>         return synchronizer;
>     }
>     
>     @Bean
>     @InboundChannelAdapter(channel = "ftpInboundChannel", poller = @Poller(fixedDelay = "5000"))
>     public MessageSource<File> ftpMessageSource() {
>         FtpInboundFileSynchronizingMessageSource source = 
>             new FtpInboundFileSynchronizingMessageSource(ftpInboundFileSynchronizer());
>         source.setLocalDirectory(new File(ftpLocalDirectory));
>         source.setAutoCreateLocalDirectory(true);
>         
>         return source;
>     }
>     
>     /**
>      * FTP Outbound - Upload files to FTP server
>      */
>     @Bean
>     @ServiceActivator(inputChannel = "ftpOutboundChannel")
>     public MessageHandler ftpOutboundMessageHandler() {
>         FtpMessageHandler handler = new FtpMessageHandler(ftpSessionFactory());
>         handler.setRemoteDirectoryExpression(
>             new org.springframework.expression.common.LiteralExpression(ftpRemoteDirectory));
>         handler.setFileNameGenerator(message -> {
>             if (message.getPayload() instanceof File) {
>                 return ((File) message.getPayload()).getName();
>             }
>             return message.getHeaders().get("file_name", String.class);
>         });
>         
>         return handler;
>     }
> }
> ```
> 
> ```java src/main/java/com/example/filestream/pattern/integration/FtpService.java
> package com.example.filestream.pattern.integration;
> 
> import lombok.RequiredArgsConstructor;
> import lombok.extern.slf4j.Slf4j;
> import org.springframework.integration.support.MessageBuilder;
> import org.springframework.messaging.MessageChannel;
> import org.springframework.stereotype.Service;
> 
> import java.io.File;
> 
> @Slf4j
> @Service
> @RequiredArgsConstructor
> public class FtpService {
>     
>     private final MessageChannel ftpOutboundChannel;
>     
>     /**
>      * Upload file to FTP server
>      */
>     public void uploadToFtp(File file) {
>         ftpOutboundChannel.send(
>             MessageBuilder.withPayload(file)
>                 .setHeader("file_name", file.getName())
>                 .build()
>         );
>         
>         log.info("Sent file to FTP: {}", file.getName());
>     }
>     
>     /**
>      * Upload file content to FTP server
>      */
>     public void uploadContentToFtp(String filename, byte[] content) {
>         ftpOutboundChannel.send(
>             MessageBuilder.withPayload(content)
>                 .setHeader("file_name", filename)
>                 .build()
>         );
>         
>         log.info("Sent content to FTP: {}", filename);
>     }
> }
> ```
> 
> ## 13. SFTP Integration Pattern
> 
> ```java src/main/java/com/example/filestream/pattern/integration/SftpIntegrationConfig.java
> package com.example.filestream.pattern.integration;
> 
> import com.jcraft.jsch.ChannelSftp;
> import org.springframework.beans.factory.annotation.Value;
> import org.springframework.context.annotation.Bean;
> import org.springframework.context.annotation.Configuration;
> import org.springframework.core.io.FileSystemResource;
> import org.springframework.integration.annotation.InboundChannelAdapter;
> import org.springframework.integration.annotation.Poller;
> import org.springframework.integration.annotation.ServiceActivator;
> import org.springframework.integration.channel.DirectChannel;
> import org.springframework.integration.core.MessageSource;
> import org.springframework.integration.file.remote.session.CachingSessionFactory;
> import org.springframework.integration.file.remote.session.SessionFactory;
> import org.springframework.integration.sftp.filters.SftpSimplePatternFileListFilter;
> import org.springframework.integration.sftp.inbound.SftpInboundFileSynchronizer;
> import org.springframework.integration.sftp.inbound.SftpInboundFileSynchronizingMessageSource;
> import org.springframework.integration.sftp.outbound.SftpMessageHandler;
> import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;
> import org.springframework.messaging.MessageChannel;
> import org.springframework.messaging.MessageHandler;
> 
> import java.io.File;
> 
> @Configuration
> public class SftpIntegrationConfig {
>     
>     @Value("${sftp.host}")
>     private String sftpHost;
>     
>     @Value("${sftp.port}")
>     private int sftpPort;
>     
>     @Value("${sftp.username}")
>     private String sftpUsername;
>     
>     @Value("${sftp.password}")
>     private String sftpPassword;
>     
>     @Value("${sftp.private-key:#{null}}")
>     private String privateKey;
>     
>     @Value("${sftp.remote-directory}")
>     private String sftpRemoteDirectory;
>     
>     @Value("${sftp.local-directory}")
>     private String sftpLocalDirectory;
>     
>     @Bean
>     public SessionFactory<ChannelSftp.LsEntry> sftpSessionFactory() {
>         DefaultSftpSessionFactory factory = new DefaultSftpSessionFactory(true);
>         factory.setHost(sftpHost);
>         factory.setPort(sftpPort);
>         factory.setUser(sftpUsername);
>         
>         if (privateKey != null && !privateKey.isEmpty()) {
>             factory.setPrivateKey(new FileSystemResource(privateKey));
>         } else {
>             factory.setPassword(sftpPassword);
>         }
>         
>         factory.setAllowUnknownKeys(true);
>         
>         return new CachingSessionFactory<>(factory);
>     }
>     
>     @Bean
>     public MessageChannel sftpInboundChannel() {
>         return new DirectChannel();
>     }
>     
>     @Bean
>     public MessageChannel sftpOutboundChannel() {
>         return new DirectChannel();
>     }
>     
>     /**
>      * SFTP Inbound - Download files from SFTP server
>      */
>     @Bean
>     public SftpInboundFileSynchronizer sftpInboundFileSynchronizer() {
>         SftpInboundFileSynchronizer synchronizer = new SftpInboundFileSynchronizer(sftpSessionFactory());
>         synchronizer.setDeleteRemoteFiles(false);
>         synchronizer.setRemoteDirectory(sftpRemoteDirectory);
>         synchronizer.setFilter(new SftpSimplePatternFileListFilter("*.csv"));
>         synchronizer.setPreserveTimestamp(true);
>         
>         return synchronizer;
>     }
>     
>     @Bean
>     @InboundChannelAdapter(channel = "sftpInboundChannel", poller = @Poller(fixedDelay = "10000"))
>     public MessageSource<File> sftpMessageSource() {
>         SftpInboundFileSynchronizingMessageSource source = 
>             new SftpInboundFileSynchronizingMessageSource(sftpInboundFileSynchronizer());
>         source.setLocalDirectory(new File(sftpLocalDirectory));
>         source.setAutoCreateLocalDirectory(true);
>         source.setLocalFilter(new org.springframework.integration.file.filters.AcceptOnceFileListFilter<>());
>         
>         return source;
>     }
>     
>     /**
>      * SFTP Outbound - Upload files to SFTP server
>      */
>     @Bean
>     @ServiceActivator(inputChannel = "sftpOutboundChannel")
>     public MessageHandler sftpOutboundMessageHandler() {
>         SftpMessageHandler handler = new SftpMessageHandler(sftpSessionFactory());
>         handler.setRemoteDirectoryExpression(
>             new org.springframework.expression.common.LiteralExpression(sftpRemoteDirectory));
>         handler.setAutoCreateDirectory(true);
>         handler.setFileNameGenerator(message -> {
>             if (message.getPayload() instanceof File) {
>                 return ((File) message.getPayload()).getName();
>             }
>             return message.getHeaders().get("file_name", String.class);
>         });
>         
>         return handler;
>     }
> }
> ```
> 
> ```java src/main/java/com/example/filestream/pattern/integration/SftpService.java
> package com.example.filestream.pattern.integration;
> 
> import com.jcraft.jsch.ChannelSftp;
> import lombok.RequiredArgsConstructor;
> import lombok.extern.slf4j.Slf4j;
> import org.springframework.integration.file.remote.session.Session;
> import org.springframework.integration.file.remote.session.SessionFactory;
> import org.springframework.integration.support.MessageBuilder;
> import org.springframework.messaging.MessageChannel;
> import org.springframework.stereotype.Service;
> 
> import java.io.ByteArrayInputStream;
> import java.io.File;
> import java.io.IOException;
> import java.io.InputStream;
> 
> @Slf4j
> @Service
> @RequiredArgsConstructor
> public class SftpService {
>     
>     private final MessageChannel sftpOutboundChannel;
>     private final SessionFactory<ChannelSftp.LsEntry> sftpSessionFactory;
>     
>     /**
>      * Upload file to SFTP server
>      */
>     public void uploadToSftp(File file) {
>         sftpOutboundChannel.send(
>             MessageBuilder.withPayload(file)
>                 .setHeader("file_name", file.getName())
>                 .build()
>         );
>         
>         log.info("Sent file to SFTP: {}", file.getName());
>     }
>     
>     /**
>      * Upload content to SFTP server
>      */
>     public void uploadContentToSftp(String filename, byte[] content) {
>         sftpOutboundChannel.send(
>             MessageBuilder.withPayload(content)
>                 .setHeader("file_name", filename)
>                 .build()
>         );
>         
>         log.info("Sent content to SFTP: {}", filename);
>     }
>     
>     /**
>      * Direct SFTP operations using session
>      */
>     public void uploadUsingSftpSession(String remoteDirectory, String filename, InputStream inputStream) {
>         Session<ChannelSftp.LsEntry> session = null;
>         try {
>             session = sftpSessionFactory.getSession();
>             
>             if (!session.exists(remoteDirectory)) {
>                 session.mkdir(remoteDirectory);
>             }
>             
>             String remotePath = remoteDirectory + "/" + filename;
>             session.write(inputStream, remotePath);
>             
>             log.info("Uploaded file to SFTP: {}", remotePath);
>             
>         } catch (IOException e) {
>             log.error("Error uploading to SFTP", e);
>             throw new RuntimeException("SFTP upload failed", e);
>         } finally {
>             if (session != null) {
>                 session.close();
>             }
>         }
>     }
>     
>     /**
>      * Download file from SFTP server
>      */
>     public InputStream downloadFromSftp(String remoteDirectory, String filename) {
>         Session<ChannelSftp.LsEntry> session = null;
>         try {
>             session = sftpSessionFactory.getSession();
>             String remotePath = remoteDirectory + "/" + filename;
>             
>             return session.readRaw(remotePath);
>             
>         } catch (IOException e) {
>             log.error("Error downloading from SFTP", e);
>             throw new RuntimeException("SFTP download failed", e);
>         } finally {
>             if (session != null) {
>                 session.close();
>             }
>         }
>     }
>     
>     /**
>      * List files on SFTP server
>      */
>     public ChannelSftp.LsEntry[] listFiles(String remoteDirectory) {
>         Session<ChannelSftp.LsEntry> session = null;
>         try {
>             session = sftpSessionFactory.getSession();
>             return session.list(remoteDirectory);
>             
>         } catch (IOException e) {
>             log.error("Error listing SFTP files", e);
>             throw new RuntimeException("SFTP list failed", e);
>         } finally {
>             if (session != null) {
>                 session.close();
>             }
>         }
>     }
> }
> ```
> 
> ## 14. REST Controllers
> 
> ```java src/main/java/com/example/filestream/controller/FileUploadController.java
> package com.example.filestream.controller;
> 
> import com.example.filestream.pattern.FileUploadResult;
> import com.example.filestream.pattern.FileUploadService;
> import com.example.filestream.pattern.MultipartFileHandlingService;
> import lombok.RequiredArgsConstructor;
> import lombok.extern.slf4j.Slf4j;
> import org.springframework.http.HttpStatus;
> import org.springframework.http.ResponseEntity;
> import org.springframework.web.bind.annotation.*;
> import org.springframework.web.multipart.MultipartFile;
> 
> import java.io.IOException;
> import java.util.List;
> 
> @Slf4j
> @RestController
> @RequestMapping("/api/upload")
> @RequiredArgsConstructor
> public class FileUploadController {
>     
>     private final FileUploadService fileUploadService;
>     private final MultipartFileHandlingService multipartFileHandlingService;
>     
>     /**
>      * Upload single file
>      */
>     @PostMapping("/single")
>     public ResponseEntity<FileUploadResult> uploadSingleFile(
>             @RequestParam("file") MultipartFile file) {
>         try {
>             FileUploadResult result = fileUploadService.uploadFileWithMetadata(file);
>             return ResponseEntity.ok(result);
>         } catch (IOException e) {
>             log.error("Error uploading file", e);
>             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
>         }
>     }
>     
>     /**
>      * Upload multiple files
>      */
>     @PostMapping("/multiple")
>     public ResponseEntity<List<FileUploadResult>> uploadMultipleFiles(
>             @RequestParam("files") MultipartFile[] files) {
>         List<FileUploadResult> results = multipartFileHandlingService.uploadMultipleFiles(files);
>         return ResponseEntity.ok(results);
>     }
>     
>     /**
>      * Upload files with type-specific handling
>      */
>     @PostMapping("/typed")
>     public ResponseEntity<List<FileUploadResult>> uploadTypedFiles(
>             @RequestParam("files") MultipartFile[] files) {
>         List<FileUploadResult> results = 
>             multipartFileHandlingService.uploadFilesWithTypeHandling(files);
>         return ResponseEntity.ok(results);
>     }
> }
> ```
> 
> ```java src/main/java/com/example/filestream/controller/FileDownloadController.java
> package com.example.filestream.controller;
> 
> import com.example.filestream.pattern.FileDownloadService;
> import com.example.filestream.pattern.FileMetadata;
> import lombok.RequiredArgsConstructor;
> import lombok.extern.slf4j.Slf4j;
> import org.springframework.core.io.Resource;
> import org.springframework.http.HttpHeaders;
> import org.springframework.http.MediaType;
> import org.springframework.http.ResponseEntity;
> import org.springframework.web.bind.annotation.*;
> 
> import java.io.IOException;
> 
> @Slf4j
> @RestController
> @RequestMapping("/api/download")
> @RequiredArgsConstructor
> public class FileDownloadController {
>     
>     private final FileDownloadService fileDownloadService;
>     
>     /**
>      * Download file
>      */
>     @GetMapping("/{filename:.+}")
>     public ResponseEntity<Resource> downloadFile(@PathVariable String filename) {
>         Resource resource = fileDownloadService.loadFileAsResource(filename);
>         
>         return ResponseEntity.ok()
>             .contentType(MediaType.APPLICATION_OCTET_STREAM)
>             .header(HttpHeaders.CONTENT_DISPOSITION, 
>                 "attachment; filename=\"" + resource.getFilename() + "\"")
>             .body(resource);
>     }
>     
>     /**
>      * Get file metadata
>      */
>     @GetMapping("/metadata/{filename:.+}")
>     public ResponseEntity<FileMetadata> getFileMetadata(@PathVariable String filename) {
>         try {
>             FileMetadata metadata = fileDownloadService.getFileMetadata(filename);
>             return ResponseEntity.ok(metadata);
>         } catch (IOException e) {
>             log.error("Error getting file metadata", e);
>             return ResponseEntity.notFound().build();
>         }
>     }
> }
> ```
> 
> ```java src/main/java/com/example/filestream/controller/StreamProcessingController.java
> package com.example.filestream.controller;
> 
> import com.example.filestream.pattern.StreamProcessingService;
> import lombok.RequiredArgsConstructor;
> import lombok.extern.slf4j.Slf4j;
> import org.springframework.http.ResponseEntity;
> import org.springframework.web.bind.annotation.*;
> import org.springframework.web.multipart.MultipartFile;
> 
> import java.io.ByteArrayOutputStream;
> import java.io.IOException;
> 
> @Slf4j
> @RestController
> @RequestMapping("/api/stream")
> @RequiredArgsConstructor
> public class StreamProcessingController {
>     
>     private final StreamProcessingService streamProcessingService;
>     
>     /**
>      * Process file using streams
>      */
>     @PostMapping("/process")
>     public ResponseEntity<String> processStream(@RequestParam("file") MultipartFile file) {
>         try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
>             long lineCount = streamProcessingService.processLargeFile(
>                 file.getInputStream(), 
>                 outputStream
>             );
>             
>             return ResponseEntity.ok("Processed " + lineCount + " lines");
>         } catch (IOException e) {
>             log.error("Error processing stream", e);
>             return ResponseEntity.internalServerError().body("Processing failed");
>         }
>     }
> }
> ```
> 
> ```java src/main/java/com/example/filestream/controller/FtpSftpController.java
> package com.example.filestream.controller;
> 
> import com.example.filestream.pattern.integration.FtpService;
> import com.example.filestream.pattern.integration.SftpService;
> import com.jcraft.jsch.ChannelSftp;
> import lombok.RequiredArgsConstructor;
> import lombok.extern.slf4j.Slf4j;
> import org.springframework.http.ResponseEntity;
> import org.springframework.web.bind.annotation.*;
> import org.springframework.web.multipart.MultipartFile;
> 
> import java.io.File;
> import java.io.IOException;
> import java.nio.file.Files;
> import java.util.Arrays;
> import java.util.List;
> import java.util.stream.Collectors;
> 
> @Slf4j
> @RestController
> @RequestMapping("/api/remote")
> @RequiredArgsConstructor
> public class FtpSftpController {
>     
>     private final FtpService ftpService;
>     private final SftpService sftpService;
>     
>     /**
>      * Upload to FTP server
>      */
>     @PostMapping("/ftp/upload")
>     public ResponseEntity<String> uploadToFtp(@RequestParam("file") MultipartFile file) {
>         try {
>             File tempFile = File.createTempFile("upload-", file.getOriginalFilename());
>             file.transferTo(tempFile);
>             
>             ftpService.uploadToFtp(tempFile);
>             
>             tempFile.delete();
>             return ResponseEntity.ok("File uploaded to FTP");
>         } catch (IOException e) {
>             log.error("Error uploading to FTP", e);
>             return ResponseEntity.internalServerError().body("Upload failed");
>         }
>     }
>     
>     /**
>      * Upload to SFTP server
>      */
>     @PostMapping("/sftp/upload")
>     public ResponseEntity<String> uploadToSftp(@RequestParam("file") MultipartFile file) {
>         try {
>             sftpService.uploadContentToSftp(file.getOriginalFilename(), file.getBytes());
>             return ResponseEntity.ok("File uploaded to SFTP");
>         } catch (IOException e) {
>             log.error("Error uploading to SFTP", e);
>             return ResponseEntity.internalServerError().body("Upload failed");
>         }
>     }
>     
>     /**
>      * List files on SFTP server
>      */
>     @GetMapping("/sftp/list")
>     public ResponseEntity<List<String>> listSftpFiles(
>             @RequestParam(defaultValue = "/") String directory) {
>         try {
>             ChannelSftp.LsEntry[] files = sftpService.listFiles(directory);
>             List<String> filenames = Arrays.stream(files)
>                 .map(ChannelSftp.LsEntry::getFilename)
>                 .filter(name -> !name.equals(".") && !name.equals(".."))
>                 .collect(Collectors.toList());
>             
>             return ResponseEntity.ok(filenames);
>         } catch (Exception e) {
>             log.error("Error listing SFTP files", e);
>             return ResponseEntity.internalServerError().build();
>         }
>     }
> }
> ```
> 
> ## 15. Exception Handler
> 
> ```java src/main/java/com/example/filestream/exception/GlobalExceptionHandler.java
> package com.example.filestream.exception;
> 
> import com.example.filestream.pattern.FileNotFoundException;
> import lombok.extern.slf4j.Slf4j;
> import org.springframework.http.HttpStatus;
> import org.springframework.http.ResponseEntity;
> import org.springframework.web.bind.annotation.ExceptionHandler;
> import org.springframework.web.bind.annotation.RestControllerAdvice;
> import org.springframework.web.multipart.MaxUploadSizeExceededException;
> 
> import java.io.IOException;
> import java.util.HashMap;
> import java.util.Map;
> 
> @Slf4j
> @RestControllerAdvice
> public class GlobalExceptionHandler {
>     
>     @ExceptionHandler(FileNotFoundException.class)
>     public ResponseEntity<Map<String, String>> handleFileNotFound(FileNotFoundException ex) {
>         log.error("File not found", ex);
>         Map<String, String> error = new HashMap<>();
>         error.put("error", "File not found");
>         error.put("message", ex.getMessage());
>         return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
>     }
>     
>     @ExceptionHandler(MaxUploadSizeExceededException.class)
>     public ResponseEntity<Map<String, String>> handleMaxSizeException(MaxUploadSizeExceededException ex) {
>         log.error("File size exceeded", ex);
>         Map<String, String> error = new HashMap<>();
>         error.put("error", "File too large");
>         error.put("message", "Maximum upload size exceeded");
>         return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(error);
>     }
>     
>     @ExceptionHandler(IOException.class)
>     public ResponseEntity<Map<String, String>> handleIOException(IOException ex) {
>         log.error("IO error", ex);
>         Map<String, String> error = new HashMap<>();
>         error.put("error", "IO error");
>         error.put("message", ex.getMessage());
>         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
>     }
>     
>     @ExceptionHandler(Exception.class)
>     public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {
>         log.error("Unexpected error", ex);
>         Map<String, String> error = new HashMap<>();
>         error.put("error", "Internal server error");
>         error.put("message", "An unexpected error occurred");
>         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
>     }
> }
> ```
> 
> ## 16. Integration Test Example
> 
> ```java src/test/java/com/example/filestream/FileUploadIntegrationTest.java
> package com.example.filestream;
> 
> import org.junit.jupiter.api.Test;
> import org.springframework.beans.factory.annotation.Autowired;
> import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
> import org.springframework.boot.test.context.SpringBootTest;
> import org.springframework.mock.web.MockMultipartFile;
> import org.springframework.test.web.servlet.MockMvc;
> 
> import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
> import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
> import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
> 
> @SpringBootTest
> @AutoConfigureMockMvc
> public class FileUploadIntegrationTest {
>     
>     @Autowired
>     private MockMvc mockMvc;
>     
>     @Test
>     public void testSingleFileUpload() throws Exception {
>         MockMultipartFile file = new MockMultipartFile(
>             "file",
>             "test.txt",
>             "text/plain",
>             "Test content".getBytes()
>         );
>         
>         mockMvc.perform(multipart("/api/upload/single").file(file))
>             .andExpect(status().isOk())
>             .andExpect(jsonPath("$.originalFilename").value("test.txt"))
>             .andExpect(jsonPath("$.size").value(12));
>     }
> }
> ```
> 
> This comprehensive Spring Boot application demonstrates all the requested file and stream processing patterns with production-ready code including error handling, logging, and REST APIs.